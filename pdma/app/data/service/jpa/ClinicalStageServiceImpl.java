package org.pepfar.pdma.app.data.service.jpa;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.pepfar.pdma.app.data.domain.Case;
import org.pepfar.pdma.app.data.domain.ClinicalStage;
import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.domain.QClinicalStage;
import org.pepfar.pdma.app.data.dto.ClinicalStageDto;
import org.pepfar.pdma.app.data.dto.ClinicalStageFilterDto;
import org.pepfar.pdma.app.data.repository.CaseRepository;
import org.pepfar.pdma.app.data.repository.ClinicalStageRepository;
import org.pepfar.pdma.app.data.repository.OrganizationRepository;
import org.pepfar.pdma.app.data.service.ClinicalStageService;
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

@Service
public class ClinicalStageServiceImpl implements ClinicalStageService {

	@Autowired
	private ClinicalStageRepository repos;

	@Autowired
	private OrganizationRepository orgRepos;

	@Autowired
	private CaseRepository caseRepos;

	@Override
	@Transactional(readOnly = true)
	public ClinicalStageDto findById(Long id) {
		if (!CommonUtils.isPositive(id, true)) {
			return null;
		}

		ClinicalStage entity = repos.findOne(id);

		if (entity != null) {
			return new ClinicalStageDto(entity);
		}

		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public List<ClinicalStageDto> findAll(ClinicalStageFilterDto filter) {

		if (filter == null || !CommonUtils.isPositive(filter.getCaseId(), true)) {
			return new ArrayList<>();
		}

		QClinicalStage q = QClinicalStage.clinicalStage;
		List<ClinicalStageDto> ret = new ArrayList<>();

		Iterator<ClinicalStage> itr = repos.findAll(q.theCase.id.longValue().eq(filter.getCaseId()),
				new Sort(new Order(Direction.DESC, "evalDate"))).iterator();

		itr.forEachRemaining(s -> {
			ret.add(new ClinicalStageDto(s));
		});

		return ret;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<ClinicalStageDto> findAllPageable(ClinicalStageFilterDto filter) {

		if (filter == null) {
			filter = new ClinicalStageFilterDto();
		}

		if (filter.getPageIndex() < 0) {
			filter.setPageIndex(0);
		}

		if (filter.getPageSize() <= 0) {
			filter.setPageSize(25);
		}

		QClinicalStage q = QClinicalStage.clinicalStage;
		Sort defaultSort = new Sort(new Order(Direction.DESC, "evalDate"));
		Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize(), defaultSort);

		if (!CommonUtils.isPositive(filter.getCaseId(), true)) {
			return new PageImpl<>(new ArrayList<>(), pageable, 0);
		}

		Page<ClinicalStage> page = repos.findAll(q.theCase.id.eq(filter.getCaseId().longValue()), pageable);
		List<ClinicalStageDto> content = new ArrayList<>();

		page.getContent().parallelStream().forEachOrdered(v -> {
			content.add(new ClinicalStageDto(v));
		});

		return new PageImpl<>(content, pageable, page.getTotalElements());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ClinicalStageDto saveOne(ClinicalStageDto dto) {

		if (dto == null) {
			throw new IllegalArgumentException("Clinical stage data could not be null.");
		}

		ClinicalStage entity = null;
		if (CommonUtils.isPositive(dto.getId(), true)) {
			entity = repos.findOne(dto.getId());
		}

		if (entity == null) {
			entity = dto.toEntity();
			entity.setUid(UUID.randomUUID());
		} else {
			entity.setStage(dto.getStage());
			entity.setEvalDate(dto.getEvalDate());
			entity.setNote(dto.getNote());
		}

		Organization organization = null;
		Case theCase = null;

		if (dto.getOrganization() != null && CommonUtils.isPositive(dto.getOrganization().getId(), true)) {
			organization = orgRepos.findOne(dto.getOrganization().getId());
		}

		if (dto.getTheCase() != null && CommonUtils.isPositive(dto.getTheCase().getId(), true)) {
			theCase = caseRepos.findOne(dto.getTheCase().getId());
		}

		if (organization == null || theCase == null) {
			throw new RuntimeException("Could not find the case and/or the organization to save clinical stage.");
		}

		entity.setOrganization(organization);
		entity.setTheCase(theCase);

		entity = repos.save(entity);

		if (!CommonUtils.isPositive(dto.getId(), true) && entity != null) {
			theCase.setWhoStage(entity.getStage());
			theCase.setWhoStageEvalDate(entity.getEvalDate());

			theCase = caseRepos.save(theCase);
		}

		if (entity != null && theCase != null) {
			return new ClinicalStageDto(entity);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteMultiple(ClinicalStageDto[] dtos) {
		if (CommonUtils.isEmpty(dtos)) {
			return;
		}

		for (ClinicalStageDto dto : dtos) {
			if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
				continue;
			}

			ClinicalStage entity = repos.findOne(dto.getId());

			if (entity != null) {
				repos.delete(entity);
			}
		}
	}

}
