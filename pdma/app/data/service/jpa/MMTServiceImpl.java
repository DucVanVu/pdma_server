package org.pepfar.pdma.app.data.service.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.pepfar.pdma.app.data.domain.Case;
import org.pepfar.pdma.app.data.domain.Dictionary;
import org.pepfar.pdma.app.data.domain.MMT;
import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.domain.QMMT;
import org.pepfar.pdma.app.data.dto.MMTDto;
import org.pepfar.pdma.app.data.dto.MMTFilterDto;
import org.pepfar.pdma.app.data.repository.CaseRepository;
import org.pepfar.pdma.app.data.repository.DictionaryRepository;
import org.pepfar.pdma.app.data.repository.MMTRepository;
import org.pepfar.pdma.app.data.repository.OrganizationRepository;
import org.pepfar.pdma.app.data.service.MMTService;
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
public class MMTServiceImpl implements MMTService {

	@Autowired
	private MMTRepository repos;

	@Autowired
	private CaseRepository caseRepos;

	@Autowired
	private OrganizationRepository orgRepos;

	@Autowired
	private DictionaryRepository dicRepos;

	@Override
	@Transactional(readOnly = true)
	public MMTDto findById(Long id) {
		if (!CommonUtils.isPositive(id, true)) {
			return null;
		}

		MMT entity = repos.findOne(id);

		if (entity != null) {
			return new MMTDto(entity);
		}

		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<MMTDto> findAllPageable(MMTFilterDto filter) {

		if (filter == null) {
			filter = new MMTFilterDto();
		}

		if (filter.getPageIndex() < 0) {
			filter.setPageIndex(0);
		}

		if (filter.getPageSize() <= 0) {
			filter.setPageSize(25);
		}

		QMMT q = QMMT.mMT;
		Sort defaultSort = new Sort(new Order(Direction.DESC, "modifyDate"));
		Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize(), defaultSort);

		if (filter.getTheCase() == null || !CommonUtils.isPositive(filter.getTheCase().getId(), true)) {
			return new PageImpl<>(new ArrayList<>(), pageable, 0);
		}

		BooleanExpression be = q.theCase.id.eq(filter.getTheCase().getId().longValue());

		Page<MMT> page = repos.findAll(be, pageable);
		List<MMTDto> content = new ArrayList<>();

		page.getContent().parallelStream().forEachOrdered(v -> {
			content.add(new MMTDto(v));
		});

		return new PageImpl<>(content, pageable, page.getTotalElements());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public MMTDto saveOne(MMTDto dto) {

		if (dto == null) {
			throw new IllegalArgumentException("MMT data could not be null.");
		}

		MMT entity = null;
		if (CommonUtils.isPositive(dto.getId(), true)) {
			entity = repos.findOne(dto.getId());
		}

		if (entity == null) {
			entity = dto.toEntity();
			entity.setUid(UUID.randomUUID());
		} else {
			entity.setOnMMT(dto.isOnMMT());
			entity.setStoppedMMT(dto.isStoppedMMT());
			entity.setFacilityName(dto.getFacilityName());
			entity.setMmtPatientCode(dto.getMmtPatientCode());
			entity.setStartDate(dto.getStartDate());
			entity.setEndDate(dto.getEndDate());
			entity.setSteadyDose(dto.getSteadyDose());
			entity.setDoseBeforeStop(dto.getDoseBeforeStop());
			entity.setNote(dto.getNote());
		}

		Dictionary reasonForStop = null;
		Organization organization = null;
		Case theCase = null;

		if (dto.getOrganization() != null && CommonUtils.isPositive(dto.getOrganization().getId(), true)) {
			organization = orgRepos.findOne(dto.getOrganization().getId());
		}

		if (dto.getTheCase() != null && CommonUtils.isPositive(dto.getTheCase().getId(), true)) {
			theCase = caseRepos.findOne(dto.getTheCase().getId());
		}

		if (dto.getReasonForStop() != null && CommonUtils.isPositive(dto.getReasonForStop().getId(), true)) {
			reasonForStop = dicRepos.findOne(dto.getReasonForStop().getId());
		}

		entity.setOrganization(organization);
		entity.setTheCase(theCase);
		entity.setReasonForStop(reasonForStop);

		entity = repos.save(entity);

		if (entity != null) {
			return new MMTDto(entity);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteMultiple(MMTDto[] dtos) {
		if (CommonUtils.isEmpty(dtos)) {
			return;
		}

		for (MMTDto dto : dtos) {
			if (dto == null || !CommonUtils.isPositive(dto.getId(), true) || dto.getTheCase() == null
					|| !CommonUtils.isPositive(dto.getTheCase().getId(), true)) {
				continue;
			}

			Case theCase = caseRepos.findOne(dto.getTheCase().getId());
			MMT entity = repos.findOne(dto.getId());

			if (entity != null && theCase != null) {
				repos.delete(entity);

				theCase.setMmt(null);
				caseRepos.save(theCase);
			}
		}
	}

}
