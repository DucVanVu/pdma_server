package org.pepfar.pdma.app.data.service.jpa;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.pepfar.pdma.app.data.domain.Case;
import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.domain.QTBProphylaxis2;
import org.pepfar.pdma.app.data.domain.TBProphylaxis2;
import org.pepfar.pdma.app.data.domain.TBProphylaxis2Dispense;
import org.pepfar.pdma.app.data.dto.ObjectDto;
import org.pepfar.pdma.app.data.dto.TBProphylaxis2DispenseDto;
import org.pepfar.pdma.app.data.dto.TBProphylaxis2Dto;
import org.pepfar.pdma.app.data.dto.TBProphylaxisFilterDto;
import org.pepfar.pdma.app.data.repository.CaseRepository;
import org.pepfar.pdma.app.data.repository.OrganizationRepository;
import org.pepfar.pdma.app.data.repository.TBProphylaxis2DispenseRepository;
import org.pepfar.pdma.app.data.repository.TBProphylaxis2Repository;
import org.pepfar.pdma.app.data.service.TBProphylaxis2Service;
import org.pepfar.pdma.app.data.types.TBProphylaxis2Status;
import org.pepfar.pdma.app.data.types.TBProphylaxisRegimen;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.dsl.BooleanExpression;

@Service
public class TBProphylaxis2ServiceImpl implements TBProphylaxis2Service {

	@Autowired
	private TBProphylaxis2Repository repos;

	@Autowired
	private CaseRepository caseRepos;

	@Autowired
	private OrganizationRepository orgRepos;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private TBProphylaxis2DispenseRepository tbProphylaxis2DispenseRepository;

	@Override
	@Transactional(readOnly = true)
	public TBProphylaxis2Dto findById(Long id) {
		if (!CommonUtils.isPositive(id, true)) {
			return null;
		}

		TBProphylaxis2 entity = repos.findOne(id);

		if (entity != null) {
			return new TBProphylaxis2Dto(entity, false);
		}

		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public TBProphylaxis2Dto findLatest(Long caseId) {
		if (!CommonUtils.isPositive(caseId, true)) {
			return null;
		}

		List<TBProphylaxis2> entities = entityManager
				.createQuery("SELECT e FROM TBProphylaxis2 e where e.theCase.id = ?1 ORDER BY e.startDate DESC",
						TBProphylaxis2.class)
				.setParameter(1, caseId).setMaxResults(1).getResultList();

		if (entities.size() > 0) {
			return new TBProphylaxis2Dto(entities.get(0), false);
		} else {
			return null;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<TBProphylaxis2Dto> findAll(TBProphylaxisFilterDto filter) {
		if (filter == null || filter.getTheCase() == null) {
			return new ArrayList<>();
		}

		List<TBProphylaxis2Dto> ret = new ArrayList<>();

		repos.findAll(QTBProphylaxis2.tBProphylaxis2.theCase.id.longValue().eq(filter.getTheCase().getId()),
				new Sort(new Order(Direction.DESC, "startDate"))).forEach(e -> {
					ret.add(new TBProphylaxis2Dto(e, true));
				});

		return ret;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<TBProphylaxis2Dto> findAllPageable(TBProphylaxisFilterDto filter) {

		if (filter == null) {
			filter = new TBProphylaxisFilterDto();
		}

		if (filter.getPageIndex() < 0) {
			filter.setPageIndex(0);
		}

		if (filter.getPageSize() <= 0) {
			filter.setPageSize(25);
		}

		QTBProphylaxis2 q = QTBProphylaxis2.tBProphylaxis2;
		Sort defaultSort = new Sort(new Order(Direction.DESC, "startDate"));
		Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize(), defaultSort);

		if (filter.getTheCase() == null || !CommonUtils.isPositive(filter.getTheCase().getId(), true)) {
			return new PageImpl<>(new ArrayList<>(), pageable, 0);
		}

		BooleanExpression be = q.theCase.id.eq(filter.getTheCase().getId().longValue());
		Page<TBProphylaxis2> page = repos.findAll(be, pageable);
		List<TBProphylaxis2Dto> content = new ArrayList<>();

		page.getContent().parallelStream().forEachOrdered(v -> {
			content.add(new TBProphylaxis2Dto(v, true));
		});

		return new PageImpl<>(content, pageable, page.getTotalElements());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public TBProphylaxis2Dto saveOne(TBProphylaxis2Dto dto) {
		if (dto == null) {
			throw new IllegalArgumentException("TBProphylaxis data could not be null.");
		}
		TBProphylaxis2 entity = null;
		if (CommonUtils.isPositive(dto.getId(), true)) {
			entity = repos.findOne(dto.getId());
		}

		if (entity == null) {
			entity = dto.toEntity();
			entity.setUid(UUID.randomUUID());
		} else {
			entity.setStartDate(dto.getStartDate());
			entity.setRegimen(dto.getRegimen());
			entity.setNote(dto.getNote());
			entity.setComplete(dto.getComplete());
			entity.setStatus(dto.getStatus());
			// N???u tr???ng th??i l?? Ho??n th??nh ??i???u tr??? m???i l??u ng??y k???t th??c
			if (dto.getStatus() == 3) {
				entity.setEndDate(dto.getEndDate());
			} else {
				entity.setEndDate(null);
			}
		}

		Organization organization = null;
		Case theCase = null;

		if (dto.getOrganization() != null && CommonUtils.isPositive(dto.getOrganization().getId(), true)) {
			organization = orgRepos.findOne(dto.getOrganization().getId());
		}

		if (dto.getTheCase() != null && CommonUtils.isPositive(dto.getTheCase().getId(), true)) {
			theCase = caseRepos.findOne(dto.getTheCase().getId());
		}

		Set<TBProphylaxis2Dispense> dispenses = new LinkedHashSet<>();
		if (dto.getDispenses() != null && dto.getDispenses().size() > 0) {
			for (TBProphylaxis2DispenseDto disDto : dto.getDispenses()) {
				TBProphylaxis2Dispense dispense = null;
				if (CommonUtils.isPositive(disDto.getId(), true)) {
					dispense = tbProphylaxis2DispenseRepository.findOne(disDto.getId());
				}
				if (dispense != null) {
					dispense = disDto.toEntity();
					dispense.setUid(UUID.randomUUID());
					dispenses.add(dispense);
				} else {
					dispense = new TBProphylaxis2Dispense();

					dispense.setDispensed(disDto.isDispensed());
					dispense.setDispensedDoses(disDto.getDispensedDoses());
					dispense.setNote(disDto.getNote());
					dispense.setRecordDate(disDto.getRecordDate());
					dispense.setResumeReason(disDto.getResumeReason());
					dispense.setStopReason(disDto.getStopReason());
					dispense.setRound(entity);
					dispenses.add(dispense);
				}
			}
		}
		if (entity.getDispenses() != null) {
			entity.getDispenses().clear();
			entity.getDispenses().addAll(dispenses);
		} else {
			entity.setDispenses(dispenses);
		}
		entity.setOrganization(organization);
		entity.setTheCase(theCase);
		entity = repos.save(entity);
		if (entity != null) {
			return new TBProphylaxis2Dto(entity, true);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	public void deleteMultiple(TBProphylaxis2Dto[] dtos) {
		if (CommonUtils.isEmpty(dtos)) {
			return;
		}

		for (TBProphylaxis2Dto dto : dtos) {
			if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
				continue;
			}

			TBProphylaxis2 entity = repos.findOne(dto.getId());
			if (entity != null && entity.getDispenses() != null && entity.getDispenses().size() > 0) {
				// x??a lu??n b???n ghi dispense
				tbProphylaxis2DispenseRepository.delete(entity.getDispenses());
			}
			if (entity != null) {
				repos.delete(entity);
			}
		}
	}

	@Override
	public void deletaOneById(Long id) {
		if (CommonUtils.isPositive(id, true)) {
			TBProphylaxis2 entity = repos.findOne(id);
			if (entity != null && entity.getDispenses() != null && entity.getDispenses().size() > 0) {
				// x??a lu??n b???n ghi dispense
				tbProphylaxis2DispenseRepository.delete(entity.getDispenses());
			}
			if (entity != null) {
				repos.delete(entity);
			}
		}
	}

	/*
	 * tbProphylaxis2Id: id ?????t ??i???u tr??? d??? ph??ng TB Prophylaxis2.
	 * tbProphylaxis2DispenseId: id c???p thu???c dose: li???u l?????ng ???????c c???p thu???c date:
	 * ng??y c???p thu???c isSave: l??u tr???ng th??i ho??n th??nh ??i???u tr??? d???t d??? ph??ng isNew:
	 * th??m m???i hay c???p nh???t l???n ph??t thu???c
	 */
	@Override
	public ObjectDto checkComplete(Long tbProphylaxis2Id, Long tbProphylaxis2DispenseId, Integer dispenseDose,
			LocalDateTime date, boolean isSave) {
		TBProphylaxis2 tbProphylaxis2 = repos.findOne(tbProphylaxis2Id);
		List<TBProphylaxis2Dispense> list = new ArrayList<TBProphylaxis2Dispense>();
		LocalDateTime dateNow = LocalDateTime.now();
		boolean save = false;
		if (tbProphylaxis2 != null && tbProphylaxis2.getDispenses() != null
				&& tbProphylaxis2.getDispenses().size() > 0) {
			list.addAll(tbProphylaxis2.getDispenses());
			TBProphylaxisRegimen regmine = tbProphylaxis2.getRegimen();
			if (regmine.equals(TBProphylaxisRegimen._6H)) {

				/*
				 * Ho??n th??nh ??i???u tr???: tr??? d??ng ????? 180 li???u trong 6 th??ng li??n t???c ho???c trong
				 * th???i gian kh??ng qu?? 9 th??ng (trong ???? kh??ng c?? l???n n??o b??? ??i???u tr??? qu?? 8
				 * tu???n)
				 */
				int dose = tbProphylaxis2DispenseRepository.countdispensedDoses(tbProphylaxis2.getId());
				if (dose >= 180) {
					if (!isSave && tbProphylaxis2DispenseId <= 0) {
						ObjectDto ret = new ObjectDto();
						ret.setCode("-2");
						ret.setNote("B???n ???? d??ng ????? 180 li???u thu???c theo quy ?????nh c???a ph??c ????? ." + regmine);
						return ret;
					}
					// ki???m tra xem ???? d??ng trong bao nhi??u th??ng

					if (list != null && list.size() > 0 && isSave) {
						TBProphylaxis2Dispense first = list.get(0);
						LocalDateTime fromDate = first.getRecordDate();// ng??y ?????u ti??n b???t ?????u d??ng thu???c
						TBProphylaxis2Dispense end = list.get(list.size() - 1);
						LocalDateTime toDate = end.getRecordDate();// ng??y k???t th??c d??ng thu???c (nh??? l?? ng??y c???p ph??t +
																	// s??? li???u-1)
						toDate = toDate.plusDays(end.getDispensedDoses() - 1);
						long daysBetween = Duration.between(fromDate, toDate).toDays();
						if (daysBetween <= 270) {// kh??ng qu?? 9 th??ng
							// ki???m tra ti???p kh??ng c?? l???n n??o b??? ??i???u tr??? qu?? 8 tu???n
							boolean isDispense = false;
							TBProphylaxis2Dispense tb2 = list.get(0);
							int index = 0;
							for (int i = 1; i < list.size(); i++) {
								if (!list.get(i).isDispensed()) {
									LocalDateTime fromDate1 = tb2.getRecordDate();
									LocalDateTime toDate1 = list.get(i).getRecordDate();
									long daysBetween1 = Duration.between(fromDate1, toDate1).toDays();
									if (daysBetween1 > 56) {
										isDispense = true;
										break;// kh??ng ho??n th??nh ?????t ??i???u tr???
									}
								}
								index = index + 1;
								tb2 = list.get(i);
							}
							if (!isDispense) {
								// ???? ho??n th??nh ?????t ??i???u tr???
//								TBProphylaxis2 tbProphylaxis2=laxisRepos.findOne(entity.getRound().getId());
								if (tbProphylaxis2 != null) {
									tbProphylaxis2.setStatus(TBProphylaxis2Status.CompleteTreatment.getNumber());
									tbProphylaxis2.setEndDate(dateNow);
									save = true;
									repos.save(tbProphylaxis2);
								}
							}
						}
					}

				} else if (dose < 180 && !isSave) {
					// c???nh b??o xem c??n bao nhi??u li???u ????? ???????c ho??n th??nh
					Integer dispenseDoseReally = 180 - dose;
					if (tbProphylaxis2DispenseId != null && tbProphylaxis2DispenseId > 0L) {
						TBProphylaxis2Dispense tbDispense = tbProphylaxis2DispenseRepository
								.findOne(tbProphylaxis2DispenseId);
						if (tbDispense != null && tbDispense.getDispensedDoses() != null) {
							dispenseDoseReally = 180 - (dose - tbDispense.getDispensedDoses());
						}
					}
					if (dispenseDoseReally < dispenseDose) {
						// n???u s??? li???u l???n h??n s??? li???u th???c th???
						ObjectDto ret = new ObjectDto();
						ret.setCode("-1");
						ret.setNote("S??? li???u thu???c th???c t??? c??n " + dispenseDoseReally + " li???u");
						ret.setNumber(dispenseDoseReally);
						return ret;
					} else if (dispenseDoseReally > dispenseDose) {
						// s??? li???u ph??t ra ??t h??n s??? li???u th???c t???
					}

				}

			} else if (regmine.equals(TBProphylaxisRegimen._9H)) {

				/*
				 * Ho??n th??nh ??i???u tr???: d??ng ????? 270 li???u thu???c trong 9 th??ng li??n t???c ho???c trong
				 * th???i gian kh??ng qu?? 12 th??ng (trong ???? kh??ng c?? l???n n??o b??? ??i???u tr??? qu?? 8
				 * tu???n).
				 */
				int dose = tbProphylaxis2DispenseRepository.countdispensedDoses(tbProphylaxis2.getId());
				if (dose >= 270) {
					if (!isSave && tbProphylaxis2DispenseId <= 0) {
						ObjectDto ret = new ObjectDto();
						ret.setCode("-2");
						ret.setNote("B???n ???? d??ng ????? 270 li???u thu???c theo quy ?????nh c???a ph??c ????? ." + regmine);
						return ret;
					}
					// ki???m tra xem ???? d??ng trong bao nhi??u th??ng
//					List<TBProphylaxis2Dispense> list=repos.getListByRound(entity.getRound().getId());
					if (list != null && list.size() > 0 && isSave) {
						TBProphylaxis2Dispense first = list.get(0);
						LocalDateTime fromDate = first.getRecordDate();// ng??y ?????u ti??n b???t ?????u d??ng thu???c
						TBProphylaxis2Dispense end = list.get(list.size() - 1);
						LocalDateTime toDate = end.getRecordDate();// ng??y k???t th??c d??ng thu???c (nh??? l?? ng??y c???p ph??t +
																	// s??? li???u-1)
						toDate = toDate.plusDays(end.getDispensedDoses() - 1);
						long daysBetween = Duration.between(fromDate, toDate).toDays();
						if (daysBetween <= 365) {// kh??ng qu?? 12 th??ng
							// ki???m tra ti???p kh??ng c?? l???n n??o b??? ??i???u tr??? qu?? 8 tu???n
							boolean isDispense = false;
							TBProphylaxis2Dispense tb2 = list.get(0);
							int index = 0;
							for (int i = 1; i < list.size(); i++) {
								if (!list.get(i).isDispensed()) {
									LocalDateTime fromDate1 = tb2.getRecordDate();
									LocalDateTime toDate1 = list.get(i).getRecordDate();
									long daysBetween1 = Duration.between(fromDate1, toDate1).toDays();
									if (daysBetween1 > 56) {
										isDispense = true;
										break;// kh??ng ho??n th??nh ?????t ??i???u tr???
									}
								}
								index = index + 1;
								tb2 = list.get(i);
							}
							if (!isDispense) {
								// ???? ho??n th??nh ?????t ??i???u tr???
//								TBProphylaxis2 tbProphylaxis2=laxisRepos.findOne(entity.getRound().getId());
								if (tbProphylaxis2 != null) {
									tbProphylaxis2.setStatus(TBProphylaxis2Status.CompleteTreatment.getNumber());
									tbProphylaxis2.setEndDate(dateNow);
									save = true;
									repos.save(tbProphylaxis2);
								}
							}
						}
					}

				} else if (dose < 270 && !isSave) {
					// c???nh b??o xem c??n bao nhi??u li???u ????? ???????c ho??n th??nh
					Integer dispenseDoseReally = 270 - dose;
					if (tbProphylaxis2DispenseId != null && tbProphylaxis2DispenseId > 0L) {
						TBProphylaxis2Dispense tbDispense = tbProphylaxis2DispenseRepository
								.findOne(tbProphylaxis2DispenseId);
						if (tbDispense != null && tbDispense.getDispensedDoses() != null) {
							dispenseDoseReally = 270 - (dose - tbDispense.getDispensedDoses());
						}
					}
					if (dispenseDoseReally < dispenseDose) {
						// n???u s??? li???u l???n h??n s??? li???u th???c th???
						ObjectDto ret = new ObjectDto();
						ret.setCode("-1");
						ret.setNote("S??? li???u thu???c th???c t??? c??n " + dispenseDoseReally + " li???u");
						ret.setNumber(dispenseDoseReally);
						return ret;
					} else if (dispenseDoseReally > dispenseDose) {
						// s??? li???u ph??t ra ??t h??n s??? li???u th???c t???
					}

				}
			} else if (regmine.equals(TBProphylaxisRegimen._3HP)) {

				/*
				 * Ho??n th??nh ??i???u tr???: d??ng ????? 12 li???u trong 12 tu???n li??n t???c ho???c trong th???i
				 * gian kh??ng qu?? 16 tu???n.
				 */
				int dose = tbProphylaxis2DispenseRepository.countdispensedDoses(tbProphylaxis2.getId());
				if (dose >= 12) {
					if (!isSave && tbProphylaxis2DispenseId <= 0) {
						ObjectDto ret = new ObjectDto();
						ret.setCode("-2");
						ret.setNote("B???n ???? d??ng ????? 12 li???u thu???c theo quy ?????nh c???a ph??c ????? ." + regmine);
						return ret;
					}
//					List<TBProphylaxis2Dispense> list=repos.getListByRound(entity.getRound().getId());
					if (list != null && list.size() > 0 && isSave) {
						TBProphylaxis2Dispense first = list.get(0);
						LocalDateTime fromDate = first.getRecordDate();// ng??y ?????u ti??n b???t ?????u d??ng thu???c
						TBProphylaxis2Dispense end = list.get(list.size() - 1);
						LocalDateTime toDate = end.getRecordDate();// ng??y k???t th??c d??ng thu???c (nh??? l?? ng??y c???p ph??t +
																	// s??? li???u-1)
						toDate = toDate.plusDays(end.getDispensedDoses() - 1);
						long daysBetween = Duration.between(fromDate, toDate).toDays();
						if (daysBetween <= 112) {// kh??ng qu?? 16 tu???n
							// ???? ho??n th??nh ?????t ??i???u tr???
//							TBProphylaxis2 tbProphylaxis2=laxisRepos.findOne(entity.getRound().getId());
							if (tbProphylaxis2 != null) {
								tbProphylaxis2.setStatus(TBProphylaxis2Status.CompleteTreatment.getNumber());
								tbProphylaxis2.setEndDate(dateNow);
								save = true;
								repos.save(tbProphylaxis2);
							}
						}
					}

				} else if (dose < 12 && !isSave) {
					// c???nh b??o xem c??n bao nhi??u li???u ????? ???????c ho??n th??nh
					Integer dispenseDoseReally = 12 - dose;
					if (tbProphylaxis2DispenseId != null && tbProphylaxis2DispenseId > 0L) {
						TBProphylaxis2Dispense tbDispense = tbProphylaxis2DispenseRepository
								.findOne(tbProphylaxis2DispenseId);
						if (tbDispense != null && tbDispense.getDispensedDoses() != null) {
							dispenseDoseReally = 12 - (dose - tbDispense.getDispensedDoses());
						}
					}
					if (dispenseDoseReally < dispenseDose) {
						// n???u s??? li???u l???n h??n s??? li???u th???c th???
						ObjectDto ret = new ObjectDto();
						ret.setCode("-1");
						ret.setNote("S??? li???u thu???c th???c t??? c??n " + dispenseDoseReally + " li???u");
						ret.setNumber(dispenseDoseReally);
						return ret;
					} else if (dispenseDoseReally > dispenseDose) {
						// s??? li???u ph??t ra ??t h??n s??? li???u th???c t???
					}

				}

			} else if (regmine.equals(TBProphylaxisRegimen._3RH)) {
				/*
				 * Ho??n th??nh ??i???u tr???: d??ng ????? 90 li???u trong 3 th??ng li??n t???c ho???c trong th???i
				 * gian kh??ng qu?? 4 th??ng
				 */
				int dose = tbProphylaxis2DispenseRepository.countdispensedDoses(tbProphylaxis2.getId());
				if (dose >= 90) {
					if (!isSave && tbProphylaxis2DispenseId <= 0) {
						ObjectDto ret = new ObjectDto();
						ret.setCode("-2");
						ret.setNote("B???n ???? d??ng ????? 90 li???u thu???c theo quy ?????nh c???a ph??c ????? ." + regmine);
						return ret;
					}
//					List<TBProphylaxis2Dispense> list=repos.getListByRound(entity.getRound().getId());
					if (list != null && list.size() > 0) {
						TBProphylaxis2Dispense first = list.get(0);
						LocalDateTime fromDate = first.getRecordDate();// ng??y ?????u ti??n b???t ?????u d??ng thu???c
						TBProphylaxis2Dispense end = list.get(list.size() - 1);
						LocalDateTime toDate = end.getRecordDate();// ng??y k???t th??c d??ng thu???c (nh??? l?? ng??y c???p ph??t +
																	// s??? li???u-1)
						toDate = toDate.plusDays(end.getDispensedDoses() - 1);
						long daysBetween = Duration.between(fromDate, toDate).toDays();
						if (daysBetween <= 120) {// kh??ng qu?? 4 th??ng
							// ???? ho??n th??nh ?????t ??i???u tr???
//							TBProphylaxis2 tbProphylaxis2=laxisRepos.findOne(entity.getRound().getId());
							if (tbProphylaxis2 != null) {
								tbProphylaxis2.setStatus(TBProphylaxis2Status.CompleteTreatment.getNumber());
								tbProphylaxis2.setEndDate(dateNow);
								save = true;
								repos.save(tbProphylaxis2);
							}
						}
					}

				} else if (dose < 90 && !isSave) {
					// c???nh b??o xem c??n bao nhi??u li???u ????? ???????c ho??n th??nh
					Integer dispenseDoseReally = 90 - dose;
					if (tbProphylaxis2DispenseId != null && tbProphylaxis2DispenseId > 0L) {
						TBProphylaxis2Dispense tbDispense = tbProphylaxis2DispenseRepository
								.findOne(tbProphylaxis2DispenseId);
						if (tbDispense != null && tbDispense.getDispensedDoses() != null) {
							dispenseDoseReally = 90 - (dose - tbDispense.getDispensedDoses());
						}
					}
					if (dispenseDoseReally < dispenseDose) {
						// n???u s??? li???u l???n h??n s??? li???u th???c th???
						ObjectDto ret = new ObjectDto();
						ret.setCode("-1");
						ret.setNote("S??? li???u thu???c th???c t??? c??n " + dispenseDoseReally + " li???u");
						ret.setNumber(dispenseDoseReally);
						return ret;
					} else if (dispenseDoseReally > dispenseDose) {
						// s??? li???u ph??t ra ??t h??n s??? li???u th???c t???
					}

				}
			} else if (regmine.equals(TBProphylaxisRegimen._4R)) {
				/*
				 * Ho??n th??nh ??i???u tr???: D??ng ????? 120 li???u trong 4 th??ng ho???c trong th???i gian
				 * kh??ng qu?? 5 th??ng
				 */
				int dose = tbProphylaxis2DispenseRepository.countdispensedDoses(tbProphylaxis2.getId());
				if (dose >= 120) {
					if (!isSave && tbProphylaxis2DispenseId <= 0) {
						ObjectDto ret = new ObjectDto();
						ret.setCode("-2");
						ret.setNote("B???n ???? d??ng ????? 120 li???u thu???c theo quy ?????nh c???a ph??c ????? ." + regmine);
						return ret;
					}
//					List<TBProphylaxis2Dispense> list=repos.getListByRound(entity.getRound().getId());
					if (list != null && list.size() > 0 && isSave) {
						TBProphylaxis2Dispense first = list.get(0);
						LocalDateTime fromDate = first.getRecordDate();// ng??y ?????u ti??n b???t ?????u d??ng thu???c
						TBProphylaxis2Dispense end = list.get(list.size() - 1);
						LocalDateTime toDate = end.getRecordDate();// ng??y k???t th??c d??ng thu???c (nh??? l?? ng??y c???p ph??t +
																	// s??? li???u-1)
						toDate = toDate.plusDays(end.getDispensedDoses() - 1);
						long daysBetween = Duration.between(fromDate, toDate).toDays();
						if (daysBetween <= 150) {// kh??ng qu?? 5 th??ng
							// ???? ho??n th??nh ?????t ??i???u tr???
//							TBProphylaxis2 tbProphylaxis2=laxisRepos.findOne(entity.getRound().getId());
							if (tbProphylaxis2 != null) {
								tbProphylaxis2.setStatus(TBProphylaxis2Status.CompleteTreatment.getNumber());
								tbProphylaxis2.setEndDate(dateNow);
								save = true;
								repos.save(tbProphylaxis2);
							}
						}
					}

				} else if (dose < 120 && !isSave) {
					// c???nh b??o xem c??n bao nhi??u li???u ????? ???????c ho??n th??nh
					Integer dispenseDoseReally = 120 - dose;
					if (tbProphylaxis2DispenseId != null && tbProphylaxis2DispenseId > 0L) {
						TBProphylaxis2Dispense tbDispense = tbProphylaxis2DispenseRepository
								.findOne(tbProphylaxis2DispenseId);
						if (tbDispense != null && tbDispense.getDispensedDoses() != null) {
							dispenseDoseReally = 120 - (dose - tbDispense.getDispensedDoses());
						}
					}
					if (dispenseDoseReally < dispenseDose) {
						// n???u s??? li???u l???n h??n s??? li???u th???c th???
						ObjectDto ret = new ObjectDto();
						ret.setCode("-1");
						ret.setNote("S??? li???u thu???c th???c t??? c??n " + dispenseDoseReally + " li???u");
						ret.setNumber(dispenseDoseReally);
						return ret;
					} else if (dispenseDoseReally > dispenseDose) {
						// s??? li???u ph??t ra ??t h??n s??? li???u th???c t???
					}

				}

			} else if (regmine.equals(TBProphylaxisRegimen._1HP)) {

				/*
				 * Ho??n th??nh ??i???u tr???: d??ng ????? 28 li???u trong 1 th??ng ho???c trong th???i gian kh??ng
				 * qu?? 40 ng??y
				 */
				int dose = tbProphylaxis2DispenseRepository.countdispensedDoses(tbProphylaxis2.getId());
				if (dose >= 28) {
					if (!isSave && tbProphylaxis2DispenseId <= 0) {
						ObjectDto ret = new ObjectDto();
						ret.setCode("-2");
						ret.setNote("B???n ???? d??ng ????? 28 li???u thu???c theo quy ?????nh c???a ph??c ????? ." + regmine);
						return ret;
					}
//					List<TBProphylaxis2Dispense> list=repos.getListByRound(entity.getRound().getId());
					if (list != null && list.size() > 0 && isSave) {
						TBProphylaxis2Dispense first = list.get(0);
						LocalDateTime fromDate = first.getRecordDate();// ng??y ?????u ti??n b???t ?????u d??ng thu???c
						TBProphylaxis2Dispense end = list.get(list.size() - 1);
						LocalDateTime toDate = end.getRecordDate();// ng??y k???t th??c d??ng thu???c (nh??? l?? ng??y c???p ph??t +
																	// s??? li???u-1)
						toDate = toDate.plusDays(end.getDispensedDoses() - 1);
						long daysBetween = Duration.between(fromDate, toDate).toDays();
						if (daysBetween <= 40) {// kh??ng qu?? 40 ng??y
							// ???? ho??n th??nh ?????t ??i???u tr???
//							TBProphylaxis2 tbProphylaxis2=laxisRepos.findOne(entity.getRound().getId());
							if (tbProphylaxis2 != null) {
								tbProphylaxis2.setStatus(TBProphylaxis2Status.CompleteTreatment.getNumber());
								tbProphylaxis2.setEndDate(dateNow);
								save = true;
								repos.save(tbProphylaxis2);
							}
						}
					}

				} else if (dose < 28 && !isSave) {
					// c???nh b??o xem c??n bao nhi??u li???u ????? ???????c ho??n th??nh
					Integer dispenseDoseReally = 28 - dose;
					if (tbProphylaxis2DispenseId != null && tbProphylaxis2DispenseId > 0L) {
						TBProphylaxis2Dispense tbDispense = tbProphylaxis2DispenseRepository
								.findOne(tbProphylaxis2DispenseId);
						if (tbDispense != null && tbDispense.getDispensedDoses() != null) {
							dispenseDoseReally = 28 - (dose - tbDispense.getDispensedDoses());
						}
					}
					if (dispenseDoseReally < dispenseDose) {
						// n???u s??? li???u l???n h??n s??? li???u th???c th???
						ObjectDto ret = new ObjectDto();
						ret.setCode("-1");
						ret.setNote("S??? li???u thu???c th???c t??? c??n " + dispenseDoseReally + " li???u");
						ret.setNumber(dispenseDoseReally);
						return ret;
					} else if (dispenseDoseReally > dispenseDose) {
						// s??? li???u ph??t ra ??t h??n s??? li???u th???c t???
					}

				}
			}
		} else {
			// tr?????ng h???p ch??a c?? l???n ph??t thu???c n??o
		}
		if (!save && isSave && tbProphylaxis2 != null) {
			if (tbProphylaxis2.getDispenses() != null && tbProphylaxis2.getDispenses().size() > 0) {
				LocalDateTime date1 = tbProphylaxis2.getDispenses().iterator().next().getRecordDate();
				boolean dispensed = tbProphylaxis2.getDispenses().iterator().next().isDispensed();
				for (TBProphylaxis2Dispense tbProphylaxis2Dispense : tbProphylaxis2.getDispenses()) {
					if (date1.isBefore(tbProphylaxis2Dispense.getRecordDate())) {
						date1 = tbProphylaxis2Dispense.getRecordDate();
						dispensed = tbProphylaxis2Dispense.isDispensed();
					}
				}
				if (dispensed) {
					// ??ang ??i???u tr???
					if (tbProphylaxis2 != null) {
						tbProphylaxis2.setStatus(TBProphylaxis2Status.BeingTreated.getNumber());
						tbProphylaxis2.setEndDate(null);
						repos.save(tbProphylaxis2);
					}
				} else {
					// ng??ng ??i???u tr???
					if (tbProphylaxis2 != null) {
						tbProphylaxis2.setStatus(TBProphylaxis2Status.DiscontinueTreatment.getNumber());
						tbProphylaxis2.setEndDate(null);
						repos.save(tbProphylaxis2);
					}
				}
			} else {
				tbProphylaxis2.setStatus(TBProphylaxis2Status.NotYetStart.getNumber());
				tbProphylaxis2.setEndDate(null);
				repos.save(tbProphylaxis2);
			}
		}
		return null;
	}

	/*
	 * tbProphylaxis2Id: id ?????t ??i???u tr??? d??? ph??ng TB Prophylaxis2.
	 * tbProphylaxis2DispenseId: id c???p thu???c dose: li???u l?????ng ???????c c???p thu???c date:
	 * ng??y c???p thu???c isSave: l??u tr???ng th??i kh??ng ho??n th??nh ??i???u tr??? d???t d??? ph??ng
	 * isNew: th??m m???i hay c???p nh???t l???n ph??t thu???c
	 */
	@Override
	public ObjectDto checkNotComplete(Long tbProphylaxis2Id, Long tbProphylaxis2DispenseId, Integer dispenseDose,
			LocalDateTime date, boolean isSave) {
		TBProphylaxis2 tbProphylaxis2 = repos.findOne(tbProphylaxis2Id);
		List<TBProphylaxis2Dispense> list = new ArrayList<TBProphylaxis2Dispense>();
		if (tbProphylaxis2 != null && tbProphylaxis2.getDispenses() != null
				&& tbProphylaxis2.getDispenses().size() > 0) {
			list.addAll(tbProphylaxis2.getDispenses());
			TBProphylaxisRegimen regmine = tbProphylaxis2.getRegimen();
			if (regmine.equals(TBProphylaxisRegimen._6H)) {
				/*
				 * B??? tr???: b??? thu???c t??? 2 th??ng li??n t???c tr??? l??n ho???c trong 9 th??ng kh??ng u???ng
				 * h???t 180 li???u thu???c.
				 */
//				List<TBProphylaxis2Dispense> list=repos.getListByRound(entity.getRound().getId());
				if (list != null && list.size() > 0 && isSave) {
					TBProphylaxis2Dispense tb2 = list.get(0);
					int index = 0;
					for (int i = 1; i < list.size(); i++) {
						if (!list.get(i).isDispensed()) {
							LocalDateTime fromDate1 = tb2.getRecordDate();
							LocalDateTime toDate1 = list.get(i).getRecordDate();
							long daysBetween1 = Duration.between(fromDate1, toDate1).toDays();
							if (daysBetween1 >= 60) {
								// kh??ng ho??n th??nh ?????t ??i???u tr???
//								TBProphylaxis2 tbProphylaxis2=laxisRepos.findOne(entity.getRound().getId());
								if (tbProphylaxis2 != null) {
									tbProphylaxis2.setStatus(TBProphylaxis2Status.QuitTreatment.getNumber());
									repos.save(tbProphylaxis2);
								}
								break;
							}
						}
						index = index + 1;
						tb2 = list.get(i);
					}
					// trong 9 th??ng kh??ng u???ng h???t 180 li???u thu???c
					TBProphylaxis2Dispense first = list.get(0);
					LocalDateTime fromDate = first.getRecordDate();// ng??y ?????u ti??n b???t ?????u d??ng thu???c
					TBProphylaxis2Dispense end = list.get(list.size() - 1);
					LocalDateTime toDate = end.getRecordDate();// ng??y k???t th??c d??ng thu???c (nh??? l?? ng??y c???p ph??t + s???
																// li???u-1)
					toDate = toDate.plusDays(end.getDispensedDoses() - 1);
					long daysBetween = Duration.between(fromDate, toDate).toDays();
					// ch??? n??y c???n x??? l?? l???i ch??a chu???n
					if (daysBetween >= 270) {// trong 9 th??ng kh??ng u???ng h???t 180 li???u thu???c
						int dose = tbProphylaxis2DispenseRepository.countdispensedDoses(tbProphylaxis2.getId());
						if (dose < 180) {
							// kh??ng ho??n th??nh ?????t ??i???u tr???
//							TBProphylaxis2 tbProphylaxis2=laxisRepos.findOne(entsity.getRound().getId());
							if (tbProphylaxis2 != null) {
								tbProphylaxis2.setStatus(TBProphylaxis2Status.QuitTreatment.getNumber());
								repos.save(tbProphylaxis2);
							}
						}

					}

				}

			} else if (regmine.equals(TBProphylaxisRegimen._9H)) {
				/*
				 * B??? tr???: b??? thu???c t??? 2 th??ng li??n t???c tr??? l??n ho???c trong 12 th??ng kh??ng u???ng
				 * h???t 270 li???u thu???c
				 */
//				List<TBProphylaxis2Dispense> list=repos.getListByRound(entity.getRound().getId());
				if (list != null && list.size() > 0) {
					TBProphylaxis2Dispense tb2 = list.get(0);
					int index = 0;
					for (int i = 1; i < list.size(); i++) {
						if (!list.get(i).isDispensed()) {
							LocalDateTime fromDate1 = tb2.getRecordDate();
							LocalDateTime toDate1 = list.get(i).getRecordDate();
							long daysBetween1 = Duration.between(fromDate1, toDate1).toDays();
							if (daysBetween1 >= 60) {
								// kh??ng ho??n th??nh ?????t ??i???u tr???
//								TBProphylaxis2 tbProphylaxis2=laxisRepos.findOne(entity.getRound().getId());
								if (tbProphylaxis2 != null && isSave) {
									tbProphylaxis2.setStatus(TBProphylaxis2Status.QuitTreatment.getNumber());
									repos.save(tbProphylaxis2);
								}
								break;
							}
						}
						index = index + 1;
						tb2 = list.get(i);
					}
					// trong 12 th??ng kh??ng u???ng h???t 270 li???u thu???c
					TBProphylaxis2Dispense first = list.get(0);
					LocalDateTime fromDate = first.getRecordDate();// ng??y ?????u ti??n b???t ?????u d??ng thu???c
					TBProphylaxis2Dispense end = list.get(list.size() - 1);
					LocalDateTime toDate = end.getRecordDate();// ng??y k???t th??c d??ng thu???c (nh??? l?? ng??y c???p ph??t + s???
																// li???u-1)
					toDate = toDate.plusDays(end.getDispensedDoses() - 1);
					long daysBetween = Duration.between(fromDate, toDate).toDays();
					// ch??? n??y c???n x??? l?? l???i ch??a chu???n
					if (daysBetween >= 365) {// trong 12 th??ng kh??ng u???ng h???t 270 li???u thu???c
						int dose = tbProphylaxis2DispenseRepository.countdispensedDoses(tbProphylaxis2.getId());
						if (dose < 270) {
							// kh??ng ho??n th??nh ?????t ??i???u tr???
//							TBProphylaxis2 tbProphylaxis2=laxisRepos.findOne(entity.getRound().getId());
							if (tbProphylaxis2 != null && isSave) {
								tbProphylaxis2.setStatus(TBProphylaxis2Status.QuitTreatment.getNumber());
								repos.save(tbProphylaxis2);
							}
						}

					}

				}
			} else if (regmine.equals(TBProphylaxisRegimen._3HP)) {
				/*
				 * B??? tr???: b??? thu???c t??? 4 tu???n li??n t???c tr??? l??n ho???c trong 16 tu???n kh??ng u???ng h???t
				 * 12 li???u thu???c
				 */

//				List<TBProphylaxis2Dispense> list=repos.getListByRound(entity.getRound().getId());
				if (list != null && list.size() > 0) {
					TBProphylaxis2Dispense tb2 = list.get(0);
					int index = 0;
					for (int i = 1; i < list.size(); i++) {
						if (!list.get(i).isDispensed()) {
							LocalDateTime fromDate1 = tb2.getRecordDate();
							LocalDateTime toDate1 = list.get(i).getRecordDate();
							long daysBetween1 = Duration.between(fromDate1, toDate1).toDays();
							if (daysBetween1 >= 28) {
								// kh??ng ho??n th??nh ?????t ??i???u tr???
//								TBProphylaxis2 tbProphylaxis2=laxisRepos.findOne(entity.getRound().getId());
								if (tbProphylaxis2 != null && isSave) {
									tbProphylaxis2.setStatus(TBProphylaxis2Status.QuitTreatment.getNumber());
									repos.save(tbProphylaxis2);
								}
								break;
							}
						}
						index = index + 1;
						tb2 = list.get(i);
					}
					// trong 16 tu???n kh??ng u???ng h???t 12 li???u thu???c
					TBProphylaxis2Dispense first = list.get(0);
					LocalDateTime fromDate = first.getRecordDate();// ng??y ?????u ti??n b???t ?????u d??ng thu???c
					TBProphylaxis2Dispense end = list.get(list.size() - 1);
					LocalDateTime toDate = end.getRecordDate();// ng??y k???t th??c d??ng thu???c (nh??? l?? ng??y c???p ph??t + s???
																// li???u-1)
					toDate = toDate.plusDays(end.getDispensedDoses() - 1);
					long daysBetween = Duration.between(fromDate, toDate).toDays();
					// ch??? n??y c???n x??? l?? l???i ch??a chu???n
					if (daysBetween >= 112) {// trong 16 tu???n kh??ng u???ng h???t 12 li???u thu???c
						int dose = tbProphylaxis2DispenseRepository.countdispensedDoses(tbProphylaxis2.getId());
						if (dose < 12) {
							// kh??ng ho??n th??nh ?????t ??i???u tr???
//							TBProphylaxis2 tbProphylaxis2=laxisRepos.findOne(entity.getRound().getId());
							if (tbProphylaxis2 != null && isSave) {
								tbProphylaxis2.setStatus(TBProphylaxis2Status.QuitTreatment.getNumber());
								repos.save(tbProphylaxis2);
							}
						}

					}

				}
			} else if (regmine.equals(TBProphylaxisRegimen._3RH)) {
				/*
				 * B??? tr???: b??? thu???c t??? 4 tu???n li??n t???c tr??? l??n ho???c trong 4 th??ng kh??ng u???ng h???t
				 * 90 li???u thu???c
				 */
//				List<TBProphylaxis2Dispense> list=repos.getListByRound(entity.getRound().getId());
				if (list != null && list.size() > 0) {
					TBProphylaxis2Dispense tb2 = list.get(0);
					int index = 0;
					for (int i = 1; i < list.size(); i++) {
						if (!list.get(i).isDispensed()) {
							LocalDateTime fromDate1 = tb2.getRecordDate();
							LocalDateTime toDate1 = list.get(i).getRecordDate();
							long daysBetween1 = Duration.between(fromDate1, toDate1).toDays();
							if (daysBetween1 >= 28) {
								// kh??ng ho??n th??nh ?????t ??i???u tr???
//								TBProphylaxis2 tbProphylaxis2=laxisRepos.findOne(entity.getRound().getId());
								if (tbProphylaxis2 != null && isSave) {
									tbProphylaxis2.setStatus(TBProphylaxis2Status.QuitTreatment.getNumber());
									repos.save(tbProphylaxis2);
								}
								break;
							}
						}
						index = index + 1;
						tb2 = list.get(i);
					}
					// trong 4 th??ng kh??ng u???ng h???t 90 li???u thu???c
					TBProphylaxis2Dispense first = list.get(0);
					LocalDateTime fromDate = first.getRecordDate();// ng??y ?????u ti??n b???t ?????u d??ng thu???c
					TBProphylaxis2Dispense end = list.get(list.size() - 1);
					LocalDateTime toDate = end.getRecordDate();// ng??y k???t th??c d??ng thu???c (nh??? l?? ng??y c???p ph??t + s???
																// li???u-1)
					toDate = toDate.plusDays(end.getDispensedDoses() - 1);
					long daysBetween = Duration.between(fromDate, toDate).toDays();
					// ch??? n??y c???n x??? l?? l???i ch??a chu???n
					if (daysBetween >= 120) {// trong 4 th??ng kh??ng u???ng h???t 90 li???u thu???c
						int dose = tbProphylaxis2DispenseRepository.countdispensedDoses(tbProphylaxis2.getId());
						if (dose < 90) {
							// kh??ng ho??n th??nh ?????t ??i???u tr???
//							TBProphylaxis2 tbProphylaxis2=laxisRepos.findOne(entity.getRound().getId());
							if (tbProphylaxis2 != null && isSave) {
								tbProphylaxis2.setStatus(TBProphylaxis2Status.QuitTreatment.getNumber());
								repos.save(tbProphylaxis2);
							}
						}

					}

				}
			} else if (regmine.equals(TBProphylaxisRegimen._4R)) {
				/*
				 * B??? tr???: b??? thu???c t??? 4 tu???n li??n t???c tr??? l??n ho???c trong 5 th??ng kh??ng u???ng h???t
				 * 120 li???u thu???c
				 */
//				List<TBProphylaxis2Dispense> list=repos.getListByRound(entity.getRound().getId());
				if (list != null && list.size() > 0) {
					TBProphylaxis2Dispense tb2 = list.get(0);
					int index = 0;
					for (int i = 1; i < list.size(); i++) {
						if (!list.get(i).isDispensed()) {
							LocalDateTime fromDate1 = tb2.getRecordDate();
							LocalDateTime toDate1 = list.get(i).getRecordDate();
							long daysBetween1 = Duration.between(fromDate1, toDate1).toDays();
							if (daysBetween1 >= 28) {
								// kh??ng ho??n th??nh ?????t ??i???u tr???
//								TBProphylaxis2 tbProphylaxis2=laxisRepos.findOne(entity.getRound().getId());
								if (tbProphylaxis2 != null && isSave) {
									tbProphylaxis2.setStatus(TBProphylaxis2Status.QuitTreatment.getNumber());
									repos.save(tbProphylaxis2);
								}
								break;
							}
						}
						index = index + 1;
						tb2 = list.get(i);
					}
					// trong 5 th??ng kh??ng u???ng h???t 120 li???u thu???c
					TBProphylaxis2Dispense first = list.get(0);
					LocalDateTime fromDate = first.getRecordDate();// ng??y ?????u ti??n b???t ?????u d??ng thu???c
					TBProphylaxis2Dispense end = list.get(list.size() - 1);
					LocalDateTime toDate = end.getRecordDate();// ng??y k???t th??c d??ng thu???c (nh??? l?? ng??y c???p ph??t + s???
																// li???u-1)
					toDate = toDate.plusDays(end.getDispensedDoses() - 1);
					long daysBetween = Duration.between(fromDate, toDate).toDays();
					// ch??? n??y c???n x??? l?? l???i ch??a chu???n
					if (daysBetween >= 150) {// trong 5 th??ng kh??ng u???ng h???t 120 li???u thu???c
						int dose = tbProphylaxis2DispenseRepository.countdispensedDoses(tbProphylaxis2.getId());
						if (dose < 120) {
							// kh??ng ho??n th??nh ?????t ??i???u tr???
//							TBProphylaxis2 tbProphylaxis2=laxisRepos.findOne(entity.getRound().getId());
							if (tbProphylaxis2 != null && isSave) {
								tbProphylaxis2.setStatus(TBProphylaxis2Status.QuitTreatment.getNumber());
								repos.save(tbProphylaxis2);
							}
						}

					}

				}

			} else if (regmine.equals(TBProphylaxisRegimen._1HP)) {
				/*
				 * B??? tr???: b??? thu???c t??? 10 ng??y li??n t???c tr??? l??n ho???c trong 40 ng??y kh??ng u???ng
				 * h???t 28 li???u thu???c
				 */
//				List<TBProphylaxis2Dispense> list=repos.getListByRound(entity.getRound().getId());
				if (list != null && list.size() > 0) {
					TBProphylaxis2Dispense tb2 = list.get(0);
					int index = 0;
					for (int i = 1; i < list.size(); i++) {
						if (!list.get(i).isDispensed()) {
							LocalDateTime fromDate1 = tb2.getRecordDate();
							LocalDateTime toDate1 = list.get(i).getRecordDate();
							long daysBetween1 = Duration.between(fromDate1, toDate1).toDays();
							if (daysBetween1 >= 10) {
								// kh??ng ho??n th??nh ?????t ??i???u tr???
//								TBProphylaxis2 tbProphylaxis2=laxisRepos.findOne(entity.getRound().getId());
								if (tbProphylaxis2 != null && isSave) {
									tbProphylaxis2.setStatus(TBProphylaxis2Status.QuitTreatment.getNumber());
									repos.save(tbProphylaxis2);
								}
								break;
							}
						}
						index = index + 1;
						tb2 = list.get(i);
					}
					// trong 40 ng??y kh??ng u???ng h???t 28 li???u thu???c
					TBProphylaxis2Dispense first = list.get(0);
					LocalDateTime fromDate = first.getRecordDate();// ng??y ?????u ti??n b???t ?????u d??ng thu???c
					TBProphylaxis2Dispense end = list.get(list.size() - 1);
					LocalDateTime toDate = end.getRecordDate();// ng??y k???t th??c d??ng thu???c (nh??? l?? ng??y c???p ph??t + s???
																// li???u-1)
					toDate = toDate.plusDays(end.getDispensedDoses() - 1);
					long daysBetween = Duration.between(fromDate, toDate).toDays();
					if (daysBetween >= 40) {// trong 40 ng??y kh??ng u???ng h???t 28 li???u thu???c
						int dose = tbProphylaxis2DispenseRepository.countdispensedDoses(tbProphylaxis2.getId());
						if (dose < 28) {
							// kh??ng ho??n th??nh ?????t ??i???u tr???
//							TBProphylaxis2 tbProphylaxis2=laxisRepos.findOne(entity.getRound().getId());
							if (tbProphylaxis2 != null && isSave) {
								tbProphylaxis2.setStatus(TBProphylaxis2Status.QuitTreatment.getNumber());
								repos.save(tbProphylaxis2);
							}
						}

					}

				}
			}
		}
//		if(!save&& isSave) {
//			if(tbProphylaxis2!=null && tbProphylaxis2.getDispenses()!=null && tbProphylaxis2.getDispenses().size()>0) {
//				LocalDateTime date1=tbProphylaxis2.getDispenses().iterator().next().getRecordDate();
//				boolean dispensed=tbProphylaxis2.getDispenses().iterator().next().isDispensed();
//				for (TBProphylaxis2Dispense tbProphylaxis2Dispense : tbProphylaxis2.getDispenses()) {
//					if(date1.isBefore(tbProphylaxis2Dispense.getRecordDate())) {
//						date1=tbProphylaxis2Dispense.getRecordDate();
//						dispensed=tbProphylaxis2Dispense.isDispensed();
//					}				
//				}
//				if(dispensed) {
//					//??ang ??i???u tr???
//					if(tbProphylaxis2!=null) {
//						tbProphylaxis2.setStatus(TBProphylaxis2Status.BeingTreated.getNumber());						
//						repos.save(tbProphylaxis2);
//					}
//				}else {
//					//ng??ng ??i???u tr???
//					if(tbProphylaxis2!=null) {
//						tbProphylaxis2.setStatus(TBProphylaxis2Status.DiscontinueTreatment.getNumber());						
//						repos.save(tbProphylaxis2);
//					}
//				}
//			}
//		}
		return null;
	}

	@Override
	public TBProphylaxis2Dto setStatus(Long id) {
//		
//		// TODO Auto-generated method stub
//		if(id == null) {
//			return null;
//		}
//		TBProphylaxis2 entity=repos.findOne(id);
//		if(entity == null) {
//			return null;
//		}
//		// ch??a b???t ?????u
//		if(entity.getDispenses() == null || entity.getDispenses().size() == 0) {
//			entity.setStatus(0);
//		}
//		// ??ang ??i???u tr???
//		if(entity.getDispenses() != null && entity.getDispenses().size() > 0) {
//			entity.setStatus(1);
//		}
//		// ng??ng ??i???u tr???
//		if(entity.getDispenses() != null && entity.getDispenses().size() > 0) {
//			for(TBProphylaxis2Dispense dispense : entity.getDispenses()) {
//				if(dispense.isDispensed() == false) {
//					entity.setStatus(2);
//					break;
//				}
//			}
//		}
//		
//		// ho??n th??nh ??i???u tr???
//		if(entity.getComplete() != null && entity.getComplete() == true) {
//			entity.setStatus(3);
//		}
//		
//		repos.save(entity);
		return null;
	}

	@Override
	public ObjectDto checkAgeByRegimen(Long caseId, String regimen) {
		Case theCase = caseRepos.findOne(caseId);
		if (theCase != null && theCase.getPerson() != null && theCase.getPerson().getDob() != null) {
			LocalDateTime dob = theCase.getPerson().getDob();
			long age = CommonUtils.dateDiff(ChronoUnit.YEARS, dob, CommonUtils.hanoiNow());

			if (regimen.equals("_6H")) {
				if (age >= 15) {// ng?????i l???n kh??ng d??ng ph??c ????? n??y, ch??? d??ng cho tr??? em
					ObjectDto ret = new ObjectDto();
					ret.setCode("AGE");
					ret.setNote("Ph??c ????? " + regimen + " ???????c ch??? ?????nh cho tr??? em.");
					return ret;
				}
			} else if (regimen.equals("_9H")) {
				if (age < 15) {// tr??? em kh??ng d??ng ph??c ????? n??y, ch??? d??ng cho ng?????i l???n
					ObjectDto ret = new ObjectDto();
					ret.setCode("AGE");
					ret.setNote("Ph??c ????? " + regimen
							+ " ???????c ch??? ?????nh cho ng?????i l???n. B???n ch??a ????? tu???i ????? d??ng ph??c ????? n??y.");
					return ret;
				}

			} else if (regimen.equals("_3HP")) {
				if (age < 2) {// tr??? em d?????i 2 tu???i kh??ng ???????c d??ng ph??c ????? n??y
					ObjectDto ret = new ObjectDto();
					ret.setCode("AGE");
					ret.setNote("Ph??c ????? " + regimen + " kh??ng d??ng cho tr??? em d?????i 2 tu???i.");
					return ret;
				}

			} else if (regimen.equals("_3RH")) {

			} else if (regimen.equals("_4R")) {

			} else if (regimen.equals("_1HP")) {
				if (age < 13) {// ch???ng ch??? ?????nh tr??? em d?????i 13 tu???i
					ObjectDto ret = new ObjectDto();
					ret.setCode("AGE");
					ret.setNote("Ph??c ????? " + regimen + " ch???ng ch??? ?????nh tr??? em d?????i 13 tu???i.");
					return ret;
				}
			}
		}
		return null;
	}
}
