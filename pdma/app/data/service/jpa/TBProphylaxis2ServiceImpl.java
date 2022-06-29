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
			// Nếu trạng thái là Hoàn thành điều trị mới lưu ngày kết thúc
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
				// xóa luôn bản ghi dispense
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
				// xóa luôn bản ghi dispense
				tbProphylaxis2DispenseRepository.delete(entity.getDispenses());
			}
			if (entity != null) {
				repos.delete(entity);
			}
		}
	}

	/*
	 * tbProphylaxis2Id: id Đợt điều trị dự phòng TB Prophylaxis2.
	 * tbProphylaxis2DispenseId: id cấp thuốc dose: liều lượng được cấp thuốc date:
	 * ngày cấp thuốc isSave: lưu trạng thái hoàn thành điều trị dợt dự phòng isNew:
	 * thêm mới hay cập nhật lần phát thuốc
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
				 * Hoàn thành điều trị: trẻ dùng đủ 180 liều trong 6 tháng liên tục hoặc trong
				 * thời gian không quá 9 tháng (trong đó không có lần nào bỏ điều trị quá 8
				 * tuần)
				 */
				int dose = tbProphylaxis2DispenseRepository.countdispensedDoses(tbProphylaxis2.getId());
				if (dose >= 180) {
					if (!isSave && tbProphylaxis2DispenseId <= 0) {
						ObjectDto ret = new ObjectDto();
						ret.setCode("-2");
						ret.setNote("Bạn đã dùng đủ 180 liều thuốc theo quy định của phác đồ ." + regmine);
						return ret;
					}
					// kiểm tra xem đã dùng trong bao nhiêu tháng

					if (list != null && list.size() > 0 && isSave) {
						TBProphylaxis2Dispense first = list.get(0);
						LocalDateTime fromDate = first.getRecordDate();// ngày đầu tiên bắt đầu dùng thuốc
						TBProphylaxis2Dispense end = list.get(list.size() - 1);
						LocalDateTime toDate = end.getRecordDate();// ngày kết thúc dùng thuốc (nhớ là ngày cấp phát +
																	// số liều-1)
						toDate = toDate.plusDays(end.getDispensedDoses() - 1);
						long daysBetween = Duration.between(fromDate, toDate).toDays();
						if (daysBetween <= 270) {// không quá 9 tháng
							// kiểm tra tiếp không có lần nào bỏ điều trị quá 8 tuần
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
										break;// không hoàn thành đợt điều trị
									}
								}
								index = index + 1;
								tb2 = list.get(i);
							}
							if (!isDispense) {
								// đã hoàn thành đợt điều trị
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
					// cảnh báo xem còn bao nhiêu liều để được hoàn thành
					Integer dispenseDoseReally = 180 - dose;
					if (tbProphylaxis2DispenseId != null && tbProphylaxis2DispenseId > 0L) {
						TBProphylaxis2Dispense tbDispense = tbProphylaxis2DispenseRepository
								.findOne(tbProphylaxis2DispenseId);
						if (tbDispense != null && tbDispense.getDispensedDoses() != null) {
							dispenseDoseReally = 180 - (dose - tbDispense.getDispensedDoses());
						}
					}
					if (dispenseDoseReally < dispenseDose) {
						// nếu số liều lớn hơn số liều thực thế
						ObjectDto ret = new ObjectDto();
						ret.setCode("-1");
						ret.setNote("Số liều thuốc thực tế còn " + dispenseDoseReally + " liều");
						ret.setNumber(dispenseDoseReally);
						return ret;
					} else if (dispenseDoseReally > dispenseDose) {
						// số liều phát ra ít hơn số liều thực tế
					}

				}

			} else if (regmine.equals(TBProphylaxisRegimen._9H)) {

				/*
				 * Hoàn thành điều trị: dùng đủ 270 liều thuốc trong 9 tháng liên tục hoặc trong
				 * thời gian không quá 12 tháng (trong đó không có lần nào bỏ điều trị quá 8
				 * tuần).
				 */
				int dose = tbProphylaxis2DispenseRepository.countdispensedDoses(tbProphylaxis2.getId());
				if (dose >= 270) {
					if (!isSave && tbProphylaxis2DispenseId <= 0) {
						ObjectDto ret = new ObjectDto();
						ret.setCode("-2");
						ret.setNote("Bạn đã dùng đủ 270 liều thuốc theo quy định của phác đồ ." + regmine);
						return ret;
					}
					// kiểm tra xem đã dùng trong bao nhiêu tháng
//					List<TBProphylaxis2Dispense> list=repos.getListByRound(entity.getRound().getId());
					if (list != null && list.size() > 0 && isSave) {
						TBProphylaxis2Dispense first = list.get(0);
						LocalDateTime fromDate = first.getRecordDate();// ngày đầu tiên bắt đầu dùng thuốc
						TBProphylaxis2Dispense end = list.get(list.size() - 1);
						LocalDateTime toDate = end.getRecordDate();// ngày kết thúc dùng thuốc (nhớ là ngày cấp phát +
																	// số liều-1)
						toDate = toDate.plusDays(end.getDispensedDoses() - 1);
						long daysBetween = Duration.between(fromDate, toDate).toDays();
						if (daysBetween <= 365) {// không quá 12 tháng
							// kiểm tra tiếp không có lần nào bỏ điều trị quá 8 tuần
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
										break;// không hoàn thành đợt điều trị
									}
								}
								index = index + 1;
								tb2 = list.get(i);
							}
							if (!isDispense) {
								// đã hoàn thành đợt điều trị
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
					// cảnh báo xem còn bao nhiêu liều để được hoàn thành
					Integer dispenseDoseReally = 270 - dose;
					if (tbProphylaxis2DispenseId != null && tbProphylaxis2DispenseId > 0L) {
						TBProphylaxis2Dispense tbDispense = tbProphylaxis2DispenseRepository
								.findOne(tbProphylaxis2DispenseId);
						if (tbDispense != null && tbDispense.getDispensedDoses() != null) {
							dispenseDoseReally = 270 - (dose - tbDispense.getDispensedDoses());
						}
					}
					if (dispenseDoseReally < dispenseDose) {
						// nếu số liều lớn hơn số liều thực thế
						ObjectDto ret = new ObjectDto();
						ret.setCode("-1");
						ret.setNote("Số liều thuốc thực tế còn " + dispenseDoseReally + " liều");
						ret.setNumber(dispenseDoseReally);
						return ret;
					} else if (dispenseDoseReally > dispenseDose) {
						// số liều phát ra ít hơn số liều thực tế
					}

				}
			} else if (regmine.equals(TBProphylaxisRegimen._3HP)) {

				/*
				 * Hoàn thành điều trị: dùng đủ 12 liều trong 12 tuần liên tục hoặc trong thời
				 * gian không quá 16 tuần.
				 */
				int dose = tbProphylaxis2DispenseRepository.countdispensedDoses(tbProphylaxis2.getId());
				if (dose >= 12) {
					if (!isSave && tbProphylaxis2DispenseId <= 0) {
						ObjectDto ret = new ObjectDto();
						ret.setCode("-2");
						ret.setNote("Bạn đã dùng đủ 12 liều thuốc theo quy định của phác đồ ." + regmine);
						return ret;
					}
//					List<TBProphylaxis2Dispense> list=repos.getListByRound(entity.getRound().getId());
					if (list != null && list.size() > 0 && isSave) {
						TBProphylaxis2Dispense first = list.get(0);
						LocalDateTime fromDate = first.getRecordDate();// ngày đầu tiên bắt đầu dùng thuốc
						TBProphylaxis2Dispense end = list.get(list.size() - 1);
						LocalDateTime toDate = end.getRecordDate();// ngày kết thúc dùng thuốc (nhớ là ngày cấp phát +
																	// số liều-1)
						toDate = toDate.plusDays(end.getDispensedDoses() - 1);
						long daysBetween = Duration.between(fromDate, toDate).toDays();
						if (daysBetween <= 112) {// không quá 16 tuần
							// đã hoàn thành đợt điều trị
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
					// cảnh báo xem còn bao nhiêu liều để được hoàn thành
					Integer dispenseDoseReally = 12 - dose;
					if (tbProphylaxis2DispenseId != null && tbProphylaxis2DispenseId > 0L) {
						TBProphylaxis2Dispense tbDispense = tbProphylaxis2DispenseRepository
								.findOne(tbProphylaxis2DispenseId);
						if (tbDispense != null && tbDispense.getDispensedDoses() != null) {
							dispenseDoseReally = 12 - (dose - tbDispense.getDispensedDoses());
						}
					}
					if (dispenseDoseReally < dispenseDose) {
						// nếu số liều lớn hơn số liều thực thế
						ObjectDto ret = new ObjectDto();
						ret.setCode("-1");
						ret.setNote("Số liều thuốc thực tế còn " + dispenseDoseReally + " liều");
						ret.setNumber(dispenseDoseReally);
						return ret;
					} else if (dispenseDoseReally > dispenseDose) {
						// số liều phát ra ít hơn số liều thực tế
					}

				}

			} else if (regmine.equals(TBProphylaxisRegimen._3RH)) {
				/*
				 * Hoàn thành điều trị: dùng đủ 90 liều trong 3 tháng liên tục hoặc trong thời
				 * gian không quá 4 tháng
				 */
				int dose = tbProphylaxis2DispenseRepository.countdispensedDoses(tbProphylaxis2.getId());
				if (dose >= 90) {
					if (!isSave && tbProphylaxis2DispenseId <= 0) {
						ObjectDto ret = new ObjectDto();
						ret.setCode("-2");
						ret.setNote("Bạn đã dùng đủ 90 liều thuốc theo quy định của phác đồ ." + regmine);
						return ret;
					}
//					List<TBProphylaxis2Dispense> list=repos.getListByRound(entity.getRound().getId());
					if (list != null && list.size() > 0) {
						TBProphylaxis2Dispense first = list.get(0);
						LocalDateTime fromDate = first.getRecordDate();// ngày đầu tiên bắt đầu dùng thuốc
						TBProphylaxis2Dispense end = list.get(list.size() - 1);
						LocalDateTime toDate = end.getRecordDate();// ngày kết thúc dùng thuốc (nhớ là ngày cấp phát +
																	// số liều-1)
						toDate = toDate.plusDays(end.getDispensedDoses() - 1);
						long daysBetween = Duration.between(fromDate, toDate).toDays();
						if (daysBetween <= 120) {// không quá 4 tháng
							// đã hoàn thành đợt điều trị
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
					// cảnh báo xem còn bao nhiêu liều để được hoàn thành
					Integer dispenseDoseReally = 90 - dose;
					if (tbProphylaxis2DispenseId != null && tbProphylaxis2DispenseId > 0L) {
						TBProphylaxis2Dispense tbDispense = tbProphylaxis2DispenseRepository
								.findOne(tbProphylaxis2DispenseId);
						if (tbDispense != null && tbDispense.getDispensedDoses() != null) {
							dispenseDoseReally = 90 - (dose - tbDispense.getDispensedDoses());
						}
					}
					if (dispenseDoseReally < dispenseDose) {
						// nếu số liều lớn hơn số liều thực thế
						ObjectDto ret = new ObjectDto();
						ret.setCode("-1");
						ret.setNote("Số liều thuốc thực tế còn " + dispenseDoseReally + " liều");
						ret.setNumber(dispenseDoseReally);
						return ret;
					} else if (dispenseDoseReally > dispenseDose) {
						// số liều phát ra ít hơn số liều thực tế
					}

				}
			} else if (regmine.equals(TBProphylaxisRegimen._4R)) {
				/*
				 * Hoàn thành điều trị: Dùng đủ 120 liều trong 4 tháng hoặc trong thời gian
				 * không quá 5 tháng
				 */
				int dose = tbProphylaxis2DispenseRepository.countdispensedDoses(tbProphylaxis2.getId());
				if (dose >= 120) {
					if (!isSave && tbProphylaxis2DispenseId <= 0) {
						ObjectDto ret = new ObjectDto();
						ret.setCode("-2");
						ret.setNote("Bạn đã dùng đủ 120 liều thuốc theo quy định của phác đồ ." + regmine);
						return ret;
					}
//					List<TBProphylaxis2Dispense> list=repos.getListByRound(entity.getRound().getId());
					if (list != null && list.size() > 0 && isSave) {
						TBProphylaxis2Dispense first = list.get(0);
						LocalDateTime fromDate = first.getRecordDate();// ngày đầu tiên bắt đầu dùng thuốc
						TBProphylaxis2Dispense end = list.get(list.size() - 1);
						LocalDateTime toDate = end.getRecordDate();// ngày kết thúc dùng thuốc (nhớ là ngày cấp phát +
																	// số liều-1)
						toDate = toDate.plusDays(end.getDispensedDoses() - 1);
						long daysBetween = Duration.between(fromDate, toDate).toDays();
						if (daysBetween <= 150) {// không quá 5 tháng
							// đã hoàn thành đợt điều trị
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
					// cảnh báo xem còn bao nhiêu liều để được hoàn thành
					Integer dispenseDoseReally = 120 - dose;
					if (tbProphylaxis2DispenseId != null && tbProphylaxis2DispenseId > 0L) {
						TBProphylaxis2Dispense tbDispense = tbProphylaxis2DispenseRepository
								.findOne(tbProphylaxis2DispenseId);
						if (tbDispense != null && tbDispense.getDispensedDoses() != null) {
							dispenseDoseReally = 120 - (dose - tbDispense.getDispensedDoses());
						}
					}
					if (dispenseDoseReally < dispenseDose) {
						// nếu số liều lớn hơn số liều thực thế
						ObjectDto ret = new ObjectDto();
						ret.setCode("-1");
						ret.setNote("Số liều thuốc thực tế còn " + dispenseDoseReally + " liều");
						ret.setNumber(dispenseDoseReally);
						return ret;
					} else if (dispenseDoseReally > dispenseDose) {
						// số liều phát ra ít hơn số liều thực tế
					}

				}

			} else if (regmine.equals(TBProphylaxisRegimen._1HP)) {

				/*
				 * Hoàn thành điều trị: dùng đủ 28 liều trong 1 tháng hoặc trong thời gian không
				 * quá 40 ngày
				 */
				int dose = tbProphylaxis2DispenseRepository.countdispensedDoses(tbProphylaxis2.getId());
				if (dose >= 28) {
					if (!isSave && tbProphylaxis2DispenseId <= 0) {
						ObjectDto ret = new ObjectDto();
						ret.setCode("-2");
						ret.setNote("Bạn đã dùng đủ 28 liều thuốc theo quy định của phác đồ ." + regmine);
						return ret;
					}
//					List<TBProphylaxis2Dispense> list=repos.getListByRound(entity.getRound().getId());
					if (list != null && list.size() > 0 && isSave) {
						TBProphylaxis2Dispense first = list.get(0);
						LocalDateTime fromDate = first.getRecordDate();// ngày đầu tiên bắt đầu dùng thuốc
						TBProphylaxis2Dispense end = list.get(list.size() - 1);
						LocalDateTime toDate = end.getRecordDate();// ngày kết thúc dùng thuốc (nhớ là ngày cấp phát +
																	// số liều-1)
						toDate = toDate.plusDays(end.getDispensedDoses() - 1);
						long daysBetween = Duration.between(fromDate, toDate).toDays();
						if (daysBetween <= 40) {// không quá 40 ngày
							// đã hoàn thành đợt điều trị
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
					// cảnh báo xem còn bao nhiêu liều để được hoàn thành
					Integer dispenseDoseReally = 28 - dose;
					if (tbProphylaxis2DispenseId != null && tbProphylaxis2DispenseId > 0L) {
						TBProphylaxis2Dispense tbDispense = tbProphylaxis2DispenseRepository
								.findOne(tbProphylaxis2DispenseId);
						if (tbDispense != null && tbDispense.getDispensedDoses() != null) {
							dispenseDoseReally = 28 - (dose - tbDispense.getDispensedDoses());
						}
					}
					if (dispenseDoseReally < dispenseDose) {
						// nếu số liều lớn hơn số liều thực thế
						ObjectDto ret = new ObjectDto();
						ret.setCode("-1");
						ret.setNote("Số liều thuốc thực tế còn " + dispenseDoseReally + " liều");
						ret.setNumber(dispenseDoseReally);
						return ret;
					} else if (dispenseDoseReally > dispenseDose) {
						// số liều phát ra ít hơn số liều thực tế
					}

				}
			}
		} else {
			// trường hợp chưa có lần phát thuốc nào
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
					// đang điều trị
					if (tbProphylaxis2 != null) {
						tbProphylaxis2.setStatus(TBProphylaxis2Status.BeingTreated.getNumber());
						tbProphylaxis2.setEndDate(null);
						repos.save(tbProphylaxis2);
					}
				} else {
					// ngưng điều trị
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
	 * tbProphylaxis2Id: id Đợt điều trị dự phòng TB Prophylaxis2.
	 * tbProphylaxis2DispenseId: id cấp thuốc dose: liều lượng được cấp thuốc date:
	 * ngày cấp thuốc isSave: lưu trạng thái không hoàn thành điều trị dợt dự phòng
	 * isNew: thêm mới hay cập nhật lần phát thuốc
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
				 * Bỏ trị: bỏ thuốc từ 2 tháng liên tục trở lên hoặc trong 9 tháng không uống
				 * hết 180 liều thuốc.
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
								// không hoàn thành đợt điều trị
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
					// trong 9 tháng không uống hết 180 liều thuốc
					TBProphylaxis2Dispense first = list.get(0);
					LocalDateTime fromDate = first.getRecordDate();// ngày đầu tiên bắt đầu dùng thuốc
					TBProphylaxis2Dispense end = list.get(list.size() - 1);
					LocalDateTime toDate = end.getRecordDate();// ngày kết thúc dùng thuốc (nhớ là ngày cấp phát + số
																// liều-1)
					toDate = toDate.plusDays(end.getDispensedDoses() - 1);
					long daysBetween = Duration.between(fromDate, toDate).toDays();
					// chỗ này cần xử lý lại chưa chuẩn
					if (daysBetween >= 270) {// trong 9 tháng không uống hết 180 liều thuốc
						int dose = tbProphylaxis2DispenseRepository.countdispensedDoses(tbProphylaxis2.getId());
						if (dose < 180) {
							// không hoàn thành đợt điều trị
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
				 * Bỏ trị: bỏ thuốc từ 2 tháng liên tục trở lên hoặc trong 12 tháng không uống
				 * hết 270 liều thuốc
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
								// không hoàn thành đợt điều trị
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
					// trong 12 tháng không uống hết 270 liều thuốc
					TBProphylaxis2Dispense first = list.get(0);
					LocalDateTime fromDate = first.getRecordDate();// ngày đầu tiên bắt đầu dùng thuốc
					TBProphylaxis2Dispense end = list.get(list.size() - 1);
					LocalDateTime toDate = end.getRecordDate();// ngày kết thúc dùng thuốc (nhớ là ngày cấp phát + số
																// liều-1)
					toDate = toDate.plusDays(end.getDispensedDoses() - 1);
					long daysBetween = Duration.between(fromDate, toDate).toDays();
					// chỗ này cần xử lý lại chưa chuẩn
					if (daysBetween >= 365) {// trong 12 tháng không uống hết 270 liều thuốc
						int dose = tbProphylaxis2DispenseRepository.countdispensedDoses(tbProphylaxis2.getId());
						if (dose < 270) {
							// không hoàn thành đợt điều trị
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
				 * Bỏ trị: bỏ thuốc từ 4 tuần liên tục trở lên hoặc trong 16 tuần không uống hết
				 * 12 liều thuốc
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
								// không hoàn thành đợt điều trị
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
					// trong 16 tuần không uống hết 12 liều thuốc
					TBProphylaxis2Dispense first = list.get(0);
					LocalDateTime fromDate = first.getRecordDate();// ngày đầu tiên bắt đầu dùng thuốc
					TBProphylaxis2Dispense end = list.get(list.size() - 1);
					LocalDateTime toDate = end.getRecordDate();// ngày kết thúc dùng thuốc (nhớ là ngày cấp phát + số
																// liều-1)
					toDate = toDate.plusDays(end.getDispensedDoses() - 1);
					long daysBetween = Duration.between(fromDate, toDate).toDays();
					// chỗ này cần xử lý lại chưa chuẩn
					if (daysBetween >= 112) {// trong 16 tuần không uống hết 12 liều thuốc
						int dose = tbProphylaxis2DispenseRepository.countdispensedDoses(tbProphylaxis2.getId());
						if (dose < 12) {
							// không hoàn thành đợt điều trị
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
				 * Bỏ trị: bỏ thuốc từ 4 tuần liên tục trở lên hoặc trong 4 tháng không uống hết
				 * 90 liều thuốc
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
								// không hoàn thành đợt điều trị
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
					// trong 4 tháng không uống hết 90 liều thuốc
					TBProphylaxis2Dispense first = list.get(0);
					LocalDateTime fromDate = first.getRecordDate();// ngày đầu tiên bắt đầu dùng thuốc
					TBProphylaxis2Dispense end = list.get(list.size() - 1);
					LocalDateTime toDate = end.getRecordDate();// ngày kết thúc dùng thuốc (nhớ là ngày cấp phát + số
																// liều-1)
					toDate = toDate.plusDays(end.getDispensedDoses() - 1);
					long daysBetween = Duration.between(fromDate, toDate).toDays();
					// chỗ này cần xử lý lại chưa chuẩn
					if (daysBetween >= 120) {// trong 4 tháng không uống hết 90 liều thuốc
						int dose = tbProphylaxis2DispenseRepository.countdispensedDoses(tbProphylaxis2.getId());
						if (dose < 90) {
							// không hoàn thành đợt điều trị
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
				 * Bỏ trị: bỏ thuốc từ 4 tuần liên tục trở lên hoặc trong 5 tháng không uống hết
				 * 120 liều thuốc
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
								// không hoàn thành đợt điều trị
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
					// trong 5 tháng không uống hết 120 liều thuốc
					TBProphylaxis2Dispense first = list.get(0);
					LocalDateTime fromDate = first.getRecordDate();// ngày đầu tiên bắt đầu dùng thuốc
					TBProphylaxis2Dispense end = list.get(list.size() - 1);
					LocalDateTime toDate = end.getRecordDate();// ngày kết thúc dùng thuốc (nhớ là ngày cấp phát + số
																// liều-1)
					toDate = toDate.plusDays(end.getDispensedDoses() - 1);
					long daysBetween = Duration.between(fromDate, toDate).toDays();
					// chỗ này cần xử lý lại chưa chuẩn
					if (daysBetween >= 150) {// trong 5 tháng không uống hết 120 liều thuốc
						int dose = tbProphylaxis2DispenseRepository.countdispensedDoses(tbProphylaxis2.getId());
						if (dose < 120) {
							// không hoàn thành đợt điều trị
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
				 * Bỏ trị: bỏ thuốc từ 10 ngày liên tục trở lên hoặc trong 40 ngày không uống
				 * hết 28 liều thuốc
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
								// không hoàn thành đợt điều trị
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
					// trong 40 ngày không uống hết 28 liều thuốc
					TBProphylaxis2Dispense first = list.get(0);
					LocalDateTime fromDate = first.getRecordDate();// ngày đầu tiên bắt đầu dùng thuốc
					TBProphylaxis2Dispense end = list.get(list.size() - 1);
					LocalDateTime toDate = end.getRecordDate();// ngày kết thúc dùng thuốc (nhớ là ngày cấp phát + số
																// liều-1)
					toDate = toDate.plusDays(end.getDispensedDoses() - 1);
					long daysBetween = Duration.between(fromDate, toDate).toDays();
					if (daysBetween >= 40) {// trong 40 ngày không uống hết 28 liều thuốc
						int dose = tbProphylaxis2DispenseRepository.countdispensedDoses(tbProphylaxis2.getId());
						if (dose < 28) {
							// không hoàn thành đợt điều trị
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
//					//đang điều trị
//					if(tbProphylaxis2!=null) {
//						tbProphylaxis2.setStatus(TBProphylaxis2Status.BeingTreated.getNumber());						
//						repos.save(tbProphylaxis2);
//					}
//				}else {
//					//ngưng điều trị
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
//		// chưa bắt đầu
//		if(entity.getDispenses() == null || entity.getDispenses().size() == 0) {
//			entity.setStatus(0);
//		}
//		// đang điều trị
//		if(entity.getDispenses() != null && entity.getDispenses().size() > 0) {
//			entity.setStatus(1);
//		}
//		// ngưng điều trị
//		if(entity.getDispenses() != null && entity.getDispenses().size() > 0) {
//			for(TBProphylaxis2Dispense dispense : entity.getDispenses()) {
//				if(dispense.isDispensed() == false) {
//					entity.setStatus(2);
//					break;
//				}
//			}
//		}
//		
//		// hoàn thành điều trị
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
				if (age >= 15) {// người lớn không dùng phác đồ này, chỉ dùng cho trẻ em
					ObjectDto ret = new ObjectDto();
					ret.setCode("AGE");
					ret.setNote("Phác đồ " + regimen + " được chỉ định cho trẻ em.");
					return ret;
				}
			} else if (regimen.equals("_9H")) {
				if (age < 15) {// trẻ em không dùng phác đồ này, chỉ dùng cho người lớn
					ObjectDto ret = new ObjectDto();
					ret.setCode("AGE");
					ret.setNote("Phác đồ " + regimen
							+ " được chỉ định cho người lớn. Bạn chưa đủ tuổi để dùng phác đồ này.");
					return ret;
				}

			} else if (regimen.equals("_3HP")) {
				if (age < 2) {// trẻ em dưới 2 tuổi không được dùng phác đồ này
					ObjectDto ret = new ObjectDto();
					ret.setCode("AGE");
					ret.setNote("Phác đồ " + regimen + " không dùng cho trẻ em dưới 2 tuổi.");
					return ret;
				}

			} else if (regimen.equals("_3RH")) {

			} else if (regimen.equals("_4R")) {

			} else if (regimen.equals("_1HP")) {
				if (age < 13) {// chống chỉ định trẻ em dưới 13 tuổi
					ObjectDto ret = new ObjectDto();
					ret.setCode("AGE");
					ret.setNote("Phác đồ " + regimen + " chống chỉ định trẻ em dưới 13 tuổi.");
					return ret;
				}
			}
		}
		return null;
	}
}
