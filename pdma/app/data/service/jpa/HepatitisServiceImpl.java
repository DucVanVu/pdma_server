package org.pepfar.pdma.app.data.service.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.pepfar.pdma.app.data.domain.Case;
import org.pepfar.pdma.app.data.domain.Hepatitis;
import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.domain.QHepatitis;
import org.pepfar.pdma.app.data.dto.HepatitisDto;
import org.pepfar.pdma.app.data.dto.HepatitisFilterDto;
import org.pepfar.pdma.app.data.repository.CaseRepository;
import org.pepfar.pdma.app.data.repository.HepatitisRepository;
import org.pepfar.pdma.app.data.repository.OrganizationRepository;
import org.pepfar.pdma.app.data.service.HepatitisService;
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
public class HepatitisServiceImpl implements HepatitisService {

	@Autowired
	private HepatitisRepository repos;

	@Autowired
	private CaseRepository caseRepos;

	@Autowired
	private OrganizationRepository orgRepos;

	@Override
	@Transactional(readOnly = true)
	public HepatitisDto findById(Long id) {
		if (!CommonUtils.isPositive(id, true)) {
			return null;
		}

		Hepatitis entity = repos.findOne(id);

		if (entity != null) {
			return new HepatitisDto(entity);
		}

		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public List<HepatitisDto> findAll(HepatitisFilterDto filter) {

		if (filter == null || filter.getTheCase() == null) {
			return new ArrayList<>();
		}

		List<HepatitisDto> ret = new ArrayList<>();

		repos.findAll(QHepatitis.hepatitis.theCase.id.longValue().eq(filter.getTheCase().getId()),
				new Sort(new Order(Direction.DESC, "testDate"))).forEach(e -> {
					ret.add(new HepatitisDto(e));
				});

		return ret;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<HepatitisDto> findAllPageable(HepatitisFilterDto filter) {

		if (filter == null) {
			filter = new HepatitisFilterDto();
		}

		if (filter.getPageIndex() < 0) {
			filter.setPageIndex(0);
		}

		if (filter.getPageSize() <= 0) {
			filter.setPageSize(25);
		}

		QHepatitis q = QHepatitis.hepatitis;
		Sort defaultSort = new Sort(new Order(Direction.DESC, "testDate"));
		Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize(), defaultSort);

		if (filter.getTheCase() == null || !CommonUtils.isPositive(filter.getTheCase().getId(), true)) {
			return new PageImpl<>(new ArrayList<>(), pageable, 0);
		}

		BooleanExpression be = q.theCase.id.eq(filter.getTheCase().getId().longValue());

		if (filter.getTestType() != null) {
			be = be.and(q.testType.eq(filter.getTestType()));
		}

		Page<Hepatitis> page = repos.findAll(be, pageable);
		List<HepatitisDto> content = new ArrayList<>();

		page.getContent().parallelStream().forEachOrdered(v -> {
			content.add(new HepatitisDto(v));
		});

		return new PageImpl<>(content, pageable, page.getTotalElements());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public HepatitisDto saveOne(HepatitisDto dto) {

		if (dto == null) {
			throw new IllegalArgumentException("Hepatitis data could not be null.");
		}

		Hepatitis entity = null;
		if (CommonUtils.isPositive(dto.getId(), true)) {
			entity = repos.findOne(dto.getId());
		}

		if (entity == null) {
			entity = dto.toEntity();
			entity.setUid(UUID.randomUUID());
		} else {
			entity.setTestType(dto.getTestType());
			entity.setTestDate(dto.getTestDate());
			entity.setTestPositive(dto.isTestPositive());
			entity.setOnTreatment(dto.isOnTreatment());
			entity.setTxStartDate(dto.getTxStartDate());
			entity.setTxEndDate(dto.getTxEndDate());
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
			return new HepatitisDto(entity);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteMultiple(HepatitisDto[] dtos) {
		if (CommonUtils.isEmpty(dtos)) {
			return;
		}

		for (HepatitisDto dto : dtos) {
			if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
				continue;
			}

			Hepatitis entity = repos.findOne(dto.getId());

			if (entity != null) {
				repos.delete(entity);
			}
		}
	}

}
