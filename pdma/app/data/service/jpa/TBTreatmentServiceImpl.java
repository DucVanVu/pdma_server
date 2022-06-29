package org.pepfar.pdma.app.data.service.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.pepfar.pdma.app.data.domain.Case;
import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.domain.QTBTreatment;
import org.pepfar.pdma.app.data.domain.TBTreatment;
import org.pepfar.pdma.app.data.dto.TBTreatmentDto;
import org.pepfar.pdma.app.data.dto.TBTreatmentFilterDto;
import org.pepfar.pdma.app.data.repository.CaseRepository;
import org.pepfar.pdma.app.data.repository.OrganizationRepository;
import org.pepfar.pdma.app.data.repository.TBTreatmentRepository;
import org.pepfar.pdma.app.data.service.TBTreatmentService;
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
public class TBTreatmentServiceImpl implements TBTreatmentService {

	@Autowired
	private TBTreatmentRepository repos;

	@Autowired
	private CaseRepository caseRepos;

	@Autowired
	private OrganizationRepository orgRepos;

	@Autowired
	private EntityManager entityManager;

	@Override
	@Transactional(readOnly = true)
	public TBTreatmentDto findById(Long id) {
		if (!CommonUtils.isPositive(id, true)) {
			return null;
		}

		TBTreatment entity = repos.findOne(id);

		if (entity != null) {
			return new TBTreatmentDto(entity);
		}

		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public TBTreatmentDto findLatest(Long caseId) {
		if (!CommonUtils.isPositive(caseId, true)) {
			return null;
		}

		List<TBTreatment> entities = entityManager
				.createQuery("SELECT e FROM TBTreatment e where e.theCase.id = ?1 ORDER BY e.txStartDate DESC",
						TBTreatment.class)
				.setParameter(1, caseId).setMaxResults(1).getResultList();

		if (entities.size() > 0) {
			return new TBTreatmentDto(entities.get(0));
		} else {
			return null;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<TBTreatmentDto> findAll(TBTreatmentFilterDto filter) {

		if (filter == null || filter.getTheCase() == null) {
			return new ArrayList<>();
		}

		List<TBTreatmentDto> ret = new ArrayList<>();

		repos.findAll(QTBTreatment.tBTreatment.theCase.id.longValue().eq(filter.getTheCase().getId()),
				new Sort(new Order(Direction.DESC, "txStartDate"))).forEach(e -> {
					ret.add(new TBTreatmentDto(e));
				});

		return ret;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<TBTreatmentDto> findAllPageable(TBTreatmentFilterDto filter) {

		if (filter == null) {
			filter = new TBTreatmentFilterDto();
		}

		if (filter.getPageIndex() < 0) {
			filter.setPageIndex(0);
		}

		if (filter.getPageSize() <= 0) {
			filter.setPageSize(25);
		}

		QTBTreatment q = QTBTreatment.tBTreatment;
		Sort defaultSort = new Sort(new Order(Direction.DESC, "txStartDate"));
		Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize(), defaultSort);

		if (filter.getTheCase() == null || !CommonUtils.isPositive(filter.getTheCase().getId(), true)) {
			return new PageImpl<>(new ArrayList<>(), pageable, 0);
		}

		BooleanExpression be = q.theCase.id.eq(filter.getTheCase().getId().longValue());
		Page<TBTreatment> page = repos.findAll(be, pageable);
		List<TBTreatmentDto> content = new ArrayList<>();

		page.getContent().parallelStream().forEachOrdered(v -> {
			content.add(new TBTreatmentDto(v));
		});

		return new PageImpl<>(content, pageable, page.getTotalElements());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public TBTreatmentDto saveOne(TBTreatmentDto dto) {

		if (dto == null) {
			throw new IllegalArgumentException("TBTreatment data could not be null.");
		}

		TBTreatment entity = null;
		if (CommonUtils.isPositive(dto.getId(), true)) {
			entity = repos.findOne(dto.getId());
		}

		if (entity == null) {
			entity = dto.toEntity();
			entity.setUid(UUID.randomUUID());
		} else {
//			entity.setTestDate(dto.getTestDate());
			entity.setTestDate(dto.getDiagnoseDate());
			entity.setLabName(dto.getLabName());
			entity.setDiagnoseDate(dto.getDiagnoseDate());
			entity.setTxStartDate(dto.getTxStartDate());
			entity.setTxEndDate(dto.getTxEndDate());
			entity.setFacilityName(dto.getFacilityName());
		}

		Organization organization = null;
		Case theCase = null;

		if (dto.getOrganization() != null && CommonUtils.isPositive(dto.getOrganization().getId(), true)) {
			organization = orgRepos.findOne(dto.getOrganization().getId());
		}

		if (dto.getTheCase() != null && CommonUtils.isPositive(dto.getTheCase().getId(), true)) {
			theCase = caseRepos.findOne(dto.getTheCase().getId());
		}

		entity.setOrganization(organization);
		entity.setTheCase(theCase);

		entity = repos.save(entity);

		if (entity != null) {
			return new TBTreatmentDto(entity);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteMultiple(TBTreatmentDto[] dtos) {
		if (CommonUtils.isEmpty(dtos)) {
			return;
		}

		for (TBTreatmentDto dto : dtos) {
			if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
				continue;
			}

			TBTreatment entity = repos.findOne(dto.getId());

			if (entity != null) {
				repos.delete(entity);
			}
		}
	}

}
