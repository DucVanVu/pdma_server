package org.pepfar.pdma.app.data.service.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.pepfar.pdma.app.data.domain.Case;
import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.domain.QTBTreatment2;
import org.pepfar.pdma.app.data.domain.TBTreatment2;
import org.pepfar.pdma.app.data.dto.TBTreatment2Dto;
import org.pepfar.pdma.app.data.dto.TBTreatmentFilterDto;
import org.pepfar.pdma.app.data.repository.CaseRepository;
import org.pepfar.pdma.app.data.repository.OrganizationRepository;
import org.pepfar.pdma.app.data.repository.TBTreatment2Repository;
import org.pepfar.pdma.app.data.service.TBTreatment2Service;
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
public class TBTreatment2ServiceImpl implements TBTreatment2Service {

	@Autowired
	private TBTreatment2Repository repos;

	@Autowired
	private CaseRepository caseRepos;

	@Autowired
	private OrganizationRepository orgRepos;

	@Autowired
	private EntityManager entityManager;

	@Override
	@Transactional(readOnly = true)
	public TBTreatment2Dto findById(Long id) {
		if (!CommonUtils.isPositive(id, true)) {
			return null;
		}

		TBTreatment2 entity = repos.findOne(id);

		if (entity != null) {
			return new TBTreatment2Dto(entity, true);
		}

		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public TBTreatment2Dto findLatest(Long caseId) {
		if (!CommonUtils.isPositive(caseId, true)) {
			return null;
		}

		List<TBTreatment2> entities = entityManager
				.createQuery("SELECT e FROM TBTreatment2 e where e.theCase.id = ?1 ORDER BY e.tbTxStartDate DESC",
						TBTreatment2.class)
				.setParameter(1, caseId).setMaxResults(1).getResultList();

		if (entities.size() > 0) {
			return new TBTreatment2Dto(entities.get(0), true);
		} else {
			return null;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<TBTreatment2Dto> findAll(TBTreatmentFilterDto filter) {

		if (filter == null || filter.getTheCase() == null) {
			return new ArrayList<>();
		}

		List<TBTreatment2Dto> ret = new ArrayList<>();

		repos.findAll(QTBTreatment2.tBTreatment2.theCase.id.longValue().eq(filter.getTheCase().getId()),
				new Sort(new Order(Direction.DESC, "tbTxStartDate"))).forEach(e -> {
					ret.add(new TBTreatment2Dto(e, true));
				});

		return ret;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<TBTreatment2Dto> findAllPageable(TBTreatmentFilterDto filter) {

		if (filter == null) {
			filter = new TBTreatmentFilterDto();
		}

		if (filter.getPageIndex() < 0) {
			filter.setPageIndex(0);
		}

		if (filter.getPageSize() <= 0) {
			filter.setPageSize(25);
		}

		QTBTreatment2 q = QTBTreatment2.tBTreatment2;
		Sort defaultSort = new Sort(new Order(Direction.DESC, "tbTxStartDate"));
		Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize(), defaultSort);

		if (filter.getTheCase() == null || !CommonUtils.isPositive(filter.getTheCase().getId(), true)) {
			return new PageImpl<>(new ArrayList<>(), pageable, 0);
		}

		BooleanExpression be = q.theCase.id.eq(filter.getTheCase().getId().longValue());
		Page<TBTreatment2> page = repos.findAll(be, pageable);
		List<TBTreatment2Dto> content = new ArrayList<>();

		page.getContent().parallelStream().forEachOrdered(v -> {
			content.add(new TBTreatment2Dto(v, true));
		});

		return new PageImpl<>(content, pageable, page.getTotalElements());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public TBTreatment2Dto saveOne(TBTreatment2Dto dto) {

		if (dto == null) {
			throw new IllegalArgumentException("TBTreatment2 data could not be null.");
		}

		TBTreatment2 entity = null;
		if (CommonUtils.isPositive(dto.getId(), true)) {
			entity = repos.findOne(dto.getId());
		}

		if (entity == null) {
			entity = dto.toEntity();
			entity.setUid(UUID.randomUUID());
		} else {
			entity.setClassification1(dto.getClassification1());
			entity.setClassification2(dto.getClassification2());
			entity.setClassification3(dto.getClassification3());
			entity.setClassification4(dto.getClassification4());
			entity.setOtherTest(dto.getOtherTest());
			entity.setScreeningType(dto.getScreeningType());
			entity.setScreeningDate(dto.getScreeningDate());
			entity.setSputumSmear(dto.getSputumSmear());
			entity.setTbDiagnosed(dto.getTbDiagnosed());
			entity.setTbDiagnosisDate(dto.getTbDiagnosisDate());
			entity.setTbDiagnosisFacility(dto.getTbDiagnosisFacility());
			entity.setTbTxFacility(dto.getTbTxFacility());
			entity.setTbTxPatientCode(dto.getTbTxPatientCode());
			entity.setTbTxStartDate(dto.getTbTxStartDate());
			entity.setXpert(dto.getXpert());
			entity.setXray(dto.getXray());
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
			return new TBTreatment2Dto(entity, true);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteMultiple(TBTreatment2Dto[] dtos) {
		if (CommonUtils.isEmpty(dtos)) {
			return;
		}

		for (TBTreatment2Dto dto : dtos) {
			if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
				continue;
			}

			TBTreatment2 entity = repos.findOne(dto.getId());

			if (entity != null) {
				repos.delete(entity);
			}
		}
	}

	@Override
	public void deletaOneById(Long id) {
		if (CommonUtils.isPositive(id, true)) {
			TBTreatment2 entity = repos.findOne(id);
			if (entity != null) {
				repos.delete(entity);
			}
		}
	}

}
