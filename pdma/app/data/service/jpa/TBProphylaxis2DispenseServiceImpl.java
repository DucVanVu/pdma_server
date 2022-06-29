package org.pepfar.pdma.app.data.service.jpa;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.pepfar.pdma.app.data.domain.QTBProphylaxis2Dispense;
import org.pepfar.pdma.app.data.domain.TBProphylaxis2;
import org.pepfar.pdma.app.data.domain.TBProphylaxis2Dispense;
import org.pepfar.pdma.app.data.dto.TBProphylaxis2DispenseDto;
import org.pepfar.pdma.app.data.dto.TBProphylaxisDispenseFilterDto;
import org.pepfar.pdma.app.data.repository.TBProphylaxis2DispenseRepository;
import org.pepfar.pdma.app.data.repository.TBProphylaxis2Repository;
import org.pepfar.pdma.app.data.service.TBProphylaxis2DispenseService;
import org.pepfar.pdma.app.data.service.TBProphylaxis2Service;
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
public class TBProphylaxis2DispenseServiceImpl implements TBProphylaxis2DispenseService {

	@Autowired
	TBProphylaxis2DispenseRepository repos;

	@Autowired
	TBProphylaxis2Repository laxisRepos;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private TBProphylaxis2Service tbProphylaxis2Service;

	@Override
	@Transactional(readOnly = true)
	public TBProphylaxis2DispenseDto findById(Long id) {
		if (CommonUtils.isPositive(id, true)) {
			TBProphylaxis2Dispense entity = repos.findOne(id);
			if (entity != null) {
				return new TBProphylaxis2DispenseDto(entity, false);
			}
		}
		return null;
	}

	@Override
	public TBProphylaxis2DispenseDto saveOne(TBProphylaxis2DispenseDto dto) {
		if (dto == null) {
			throw new IllegalArgumentException("TBProphylaxis2Dispense data could not be null.");
		}
		TBProphylaxis2Dispense entity = null;
		if (CommonUtils.isPositive(dto.getId(), true)) {
			entity = repos.findOne(dto.getId());
		}

		if (entity == null) {
			// kiểm tra xem recordDate đã có chưa. Nếu có rồi thì không cho thêm
			if (dto.getRecordDate() != null && dto.getRound() != null && dto.getRound().getId() != null) {
				TBProphylaxis2DispenseDto ret = this.checkRecordate(dto.getRecordDate(), dto.getRound().getId(), null);
				if (ret != null) {
					return ret;
				}
			}

			entity = dto.toEntity();
			entity.setUid(UUID.randomUUID());
		} else {
			// kiểm tra xem recordDate đã có chưa. Nếu có rồi thì không cho thêm
			if (dto.getRecordDate() != null && dto.getRound() != null && dto.getRound().getId() != null) {
				TBProphylaxis2DispenseDto ret = this.checkRecordate(dto.getRecordDate(), dto.getRound().getId(),
						dto.getId());
				if (ret != null) {
					return ret;
				}
			}

			entity.setDispensed(dto.isDispensed());
			entity.setDispensedDoses(dto.getDispensedDoses());
			entity.setNote(dto.getNote());
			entity.setRecordDate(dto.getRecordDate());
			entity.setResumeReason(dto.getResumeReason());
			TBProphylaxis2 tbProphy = null;

			if (dto.getRound() != null && CommonUtils.isPositive(dto.getRound().getId(), true)) {
				tbProphy = laxisRepos.findOne(dto.getRound().getId());
			}
			entity.setRound(tbProphy);
			entity.setStopReason(dto.getStopReason());
		}

		entity = repos.save(entity);
		if (entity.getRound() != null && entity.getRound().getId() != null) {
			// xử lý trạng thái hoàn thành điều trị theo đợt
			tbProphylaxis2Service.checkComplete(entity.getRound().getId(), null, null, null, true);
			// không hoàn thành điều trị theo đợt
			tbProphylaxis2Service.checkNotComplete(entity.getRound().getId(), null, null, null, true);
		}

		return new TBProphylaxis2DispenseDto(entity, true);
	}

	// hàm kiểm tra xem ngày cấp thuốc hay dừng thuốc đã có tồn tại chưa. Nếu có rồi
	// thì không cho phép thêm nữa
	public TBProphylaxis2DispenseDto checkRecordate(LocalDateTime date, Long roundId, Long id) {
		if (date != null && roundId != null) {
			List<TBProphylaxis2Dispense> list = repos.getListByDate(date, roundId);
			if (list != null && list.size() > 0) {
				if (id != null && id > 0) {
					for (int i = 0; i < list.size(); i++) {
						if (list.get(i).getId().equals(id)) {
							list.remove(i);
						}
					}
				}
				if (list != null && list.size() > 0) {
					TBProphylaxis2DispenseDto ret = new TBProphylaxis2DispenseDto();
					String strDate = "";
					try {
						strDate = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(date);
					} catch (Exception e) {
						strDate = date.toString();
					}
					String note = "Ngày " + strDate + " đã ";
					if (list.get(0).getDispensedDoses() != null && list.get(0).getDispensedDoses() > 0) {
						note = note + " cấp thuốc rồi.";
					} else if (list.get(0).getResumeReason() != null) {
						note = note + " tiếp tục điều trị dự phòng rồi.";
					} else if (list.get(0).getStopReason() != null) {
						note = note + " ngưng điều trị dự phòng rồi.";
					}
					ret.setNote(note);
					ret.setCode("-1");
					return ret;
				}
			}
		}
		return null;
	}

	@Override
	public void deleteMultiple(TBProphylaxis2DispenseDto[] dtos) {
		if (CommonUtils.isEmpty(dtos)) {
			return;
		}

		for (TBProphylaxis2DispenseDto dto : dtos) {
			if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
				continue;
			}

			TBProphylaxis2Dispense entity = repos.findOne(dto.getId());

			if (entity != null) {
				repos.delete(entity);
				if (entity.getRound() != null && entity.getRound().getId() != null) {
					// xử lý trạng thái hoàn thành điều trị theo đợt
					tbProphylaxis2Service.checkComplete(entity.getRound().getId(), null, null, null, true);
					// không hoàn thành điều trị theo đợt
					tbProphylaxis2Service.checkNotComplete(entity.getRound().getId(), null, null, null, true);
				}
			}
		}
	}

	@Override
	public void deletaOneById(Long id) {
		if (CommonUtils.isPositive(id, true)) {
			TBProphylaxis2Dispense entity = repos.findOne(id);
			if (entity != null) {
				repos.delete(entity);
				if (entity.getRound() != null && entity.getRound().getId() != null) {
					// xử lý trạng thái hoàn thành điều trị theo đợt
					tbProphylaxis2Service.checkComplete(entity.getRound().getId(), null, null, null, true);
					// không hoàn thành điều trị theo đợt
					tbProphylaxis2Service.checkNotComplete(entity.getRound().getId(), null, null, null, true);
				}
			}
		}
	}

	@Override
	public List<TBProphylaxis2DispenseDto> findAll(TBProphylaxisDispenseFilterDto filter) {
		if (filter == null || filter.getRound() == null) {
			return new ArrayList<>();
		}

		List<TBProphylaxis2DispenseDto> ret = new ArrayList<>();

		repos.findAll(QTBProphylaxis2Dispense.tBProphylaxis2Dispense.round.id.longValue().eq(filter.getRound().getId()),
				new Sort(new Order(Direction.DESC, "recordDate"))).forEach(e -> {
					ret.add(new TBProphylaxis2DispenseDto(e, true));
				});
		if (filter.getRound() != null && filter.getRound().getDispensed() != null && filter.getRound().getDispensed()
				&& ret != null && ret.size() > 0) {
			for (int i = 0; i < ret.size(); i++) {
				if (!ret.get(i).isDispensed()) {
					ret.remove(i);
				}
			}
		}
		return ret;
	}

	@Override
	public Page<TBProphylaxis2DispenseDto> findAllPageable(TBProphylaxisDispenseFilterDto filter) {
		if (filter == null) {
			filter = new TBProphylaxisDispenseFilterDto();
		}

		if (filter.getPageIndex() < 0) {
			filter.setPageIndex(0);
		}

		if (filter.getPageSize() <= 0) {
			filter.setPageSize(25);
		}

		QTBProphylaxis2Dispense q = QTBProphylaxis2Dispense.tBProphylaxis2Dispense;
		Sort defaultSort = new Sort(new Order(Direction.DESC, "recordDate"));
		Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize(), defaultSort);

		if (filter.getRound() == null || !CommonUtils.isPositive(filter.getRound().getId(), true)) {
			return new PageImpl<>(new ArrayList<>(), pageable, 0);
		}

		BooleanExpression be = q.round.id.eq(filter.getRound().getId().longValue());
		Page<TBProphylaxis2Dispense> page = repos.findAll(be, pageable);
		List<TBProphylaxis2DispenseDto> content = new ArrayList<>();

		page.getContent().parallelStream().forEachOrdered(v -> {
			content.add(new TBProphylaxis2DispenseDto(v, true));
		});

		return new PageImpl<>(content, pageable, page.getTotalElements());
	}

	@Override
	public TBProphylaxis2DispenseDto findLatest(Long roundId) {
		if (!CommonUtils.isPositive(roundId, true)) {
			return null;
		}

		List<TBProphylaxis2Dispense> entities = entityManager
				.createQuery("SELECT e FROM TBProphylaxis2Dispense e where e.round.id = ?1 ORDER BY e.recordDate DESC",
						TBProphylaxis2Dispense.class)
				.setParameter(1, roundId).setMaxResults(1).getResultList();

		if (entities.size() > 0) {
			return new TBProphylaxis2DispenseDto(entities.get(0), false);
		} else {
			return null;
		}
	}

}
