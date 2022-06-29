package org.pepfar.pdma.app.data.service.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.pepfar.pdma.app.data.domain.Case;
import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.domain.Pregnancy;
import org.pepfar.pdma.app.data.domain.QPregnancy;
import org.pepfar.pdma.app.data.dto.PregnancyDto;
import org.pepfar.pdma.app.data.dto.PregnancyFilterDto;
import org.pepfar.pdma.app.data.repository.CaseRepository;
import org.pepfar.pdma.app.data.repository.OrganizationRepository;
import org.pepfar.pdma.app.data.repository.PregnancyRepository;
import org.pepfar.pdma.app.data.service.PregnancyService;
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
public class PregnancyServiceImpl implements PregnancyService {

	@Autowired
	private PregnancyRepository repos;

	@Autowired
	private CaseRepository caseRepos;

	@Autowired
	private OrganizationRepository orgRepos;

	@Override
	@Transactional(readOnly = true)
	public PregnancyDto findById(Long id) {
		if (!CommonUtils.isPositive(id, true)) {
			return null;
		}

		Pregnancy entity = repos.findOne(id);

		if (entity != null) {
			return new PregnancyDto(entity);
		}

		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<PregnancyDto> findAllPageable(PregnancyFilterDto filter) {

		if (filter == null) {
			filter = new PregnancyFilterDto();
		}

		if (filter.getPageIndex() < 0) {
			filter.setPageIndex(0);
		}

		if (filter.getPageSize() <= 0) {
			filter.setPageSize(25);
		}

		QPregnancy q = QPregnancy.pregnancy;
		Sort defaultSort = new Sort(new Order(Direction.DESC, "createDate"));
		Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize(), defaultSort);

		if (filter.getTheCase() == null || !CommonUtils.isPositive(filter.getTheCase().getId(), true)) {
			return new PageImpl<>(new ArrayList<>(), pageable, 0);
		}

		BooleanExpression be = q.theCase.id.eq(filter.getTheCase().getId().longValue());

		Page<Pregnancy> page = repos.findAll(be, pageable);
		List<PregnancyDto> content = new ArrayList<>();

		page.getContent().parallelStream().forEachOrdered(v -> {
			content.add(new PregnancyDto(v));
		});

		return new PageImpl<>(content, pageable, page.getTotalElements());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public PregnancyDto saveOne(PregnancyDto dto) {

		if (dto == null) {
			throw new IllegalArgumentException("Pregnancy data could not be null.");
		}

		Pregnancy entity = null;
		if (CommonUtils.isPositive(dto.getId(), true)) {
			entity = repos.findOne(dto.getId());
		}

		if (entity == null) {
			entity = dto.toEntity();
			entity.setUid(UUID.randomUUID());
		} else {
			entity.setPregnant(dto.isPregnant());
			entity.setLastMenstrualPeriod(dto.getLastMenstrualPeriod());
			entity.setDueDate(dto.getDueDate());
			entity.setAttendedAnc(dto.isAttendedAnc());
			entity.setPregResult(dto.getPregResult());
			entity.setChildDob(dto.getChildDob());
			entity.setBirthWeight(dto.getBirthWeight());
			entity.setChildProphylaxis(dto.getChildProphylaxis());
			entity.setChildHIVStatus(dto.getChildHIVStatus());
			entity.setChildDiagnosedDate(dto.getChildDiagnosedDate());
			entity.setChildInitiatedOnART(dto.isChildInitiatedOnART());
			entity.setChildOpc(dto.getChildOpc());
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

		entity.setOrganization(organization);
		entity.setTheCase(theCase);

		entity = repos.save(entity);

		if (entity != null) {
			return new PregnancyDto(entity);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteMultiple(PregnancyDto[] dtos) {
		if (CommonUtils.isEmpty(dtos)) {
			return;
		}

		for (PregnancyDto dto : dtos) {
			if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
				continue;
			}

			Pregnancy entity = repos.findOne(dto.getId());

			if (entity != null) {
				repos.delete(entity);
			}
		}
	}

}
