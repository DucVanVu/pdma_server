package org.pepfar.pdma.app.data.service.jpa;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.pepfar.pdma.app.data.domain.Case;
import org.pepfar.pdma.app.data.domain.Dictionary;
import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.domain.QRiskInterview;
import org.pepfar.pdma.app.data.domain.RiskInterview;
import org.pepfar.pdma.app.data.dto.RiskInterviewDto;
import org.pepfar.pdma.app.data.dto.RiskInterviewFilterDto;
import org.pepfar.pdma.app.data.repository.CaseRepository;
import org.pepfar.pdma.app.data.repository.DictionaryRepository;
import org.pepfar.pdma.app.data.repository.OrganizationRepository;
import org.pepfar.pdma.app.data.repository.RiskInterviewRepository;
import org.pepfar.pdma.app.data.service.RiskInterviewService;
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
public class RiskInterviewServiceImpl implements RiskInterviewService {

	@Autowired
	private RiskInterviewRepository repos;

	@Autowired
	private OrganizationRepository orgRepos;

	@Autowired
	private CaseRepository caseRepos;

	@Autowired
	private DictionaryRepository dicRepos;

	@Override
	@Transactional(readOnly = true)
	public RiskInterviewDto findById(Long id) {

		if (!CommonUtils.isPositive(id, true)) {
			return null;
		}

		RiskInterview entity = repos.findOne(id);

		if (entity != null) {
			return new RiskInterviewDto(entity);
		}

		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<RiskInterviewDto> findAllPageable(RiskInterviewFilterDto filter) {

		if (filter == null) {
			filter = new RiskInterviewFilterDto();
		}

		if (filter.getPageIndex() < 0) {
			filter.setPageIndex(0);
		}

		if (filter.getPageSize() <= 0) {
			filter.setPageSize(25);
		}

		Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize(),
				new Sort(new Order(Direction.DESC, "interviewDate")));

		if (filter.getTheCase() == null || !CommonUtils.isPositive(filter.getTheCase().getId(), true)) {
			return new PageImpl<>(new ArrayList<>(), pageable, 0);
		}

		QRiskInterview q = QRiskInterview.riskInterview;
		BooleanExpression be = q.theCase.id.eq(filter.getTheCase().getId().longValue());

		if (!CommonUtils.isEmpty(filter.getKeyword())) {
			be = be.and(q.risks.any().value.containsIgnoreCase(filter.getKeyword()));
		}

		Page<RiskInterview> page = repos.findAll(be, pageable);

		List<RiskInterviewDto> content = new ArrayList<>();

		page.getContent().parallelStream().forEachOrdered(r -> {
			content.add(new RiskInterviewDto(r));
		});

		return new PageImpl<>(content, pageable, page.getTotalElements());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public RiskInterviewDto saveOne(RiskInterviewDto dto) {

		if (dto == null) {
			throw new IllegalArgumentException("Risk interview is null.");
		}

		if (dto.getTheCase() == null || dto.getOrganization() == null
				|| !CommonUtils.isPositive(dto.getTheCase().getId(), true)
				|| !caseRepos.exists(dto.getTheCase().getId())
				|| !CommonUtils.isPositive(dto.getOrganization().getId(), true)
				|| !orgRepos.exists(dto.getOrganization().getId())) {
			throw new IllegalArgumentException("Case/organization cannot be null.");
		}

		RiskInterview entity = null;

		if (CommonUtils.isPositive(dto.getId(), true)) {
			entity = repos.findOne(dto.getId());
		}

		if (entity == null) {
			entity = dto.toEntity();
			entity.setUid(UUID.randomUUID());
		} else {
			entity.setInterviewDate(dto.getInterviewDate());
			entity.setRiskIdentified(dto.getRiskIdentified());
			entity.setOtherRiskGroupText(dto.getOtherRiskGroupText());
		}

		Case theCase = caseRepos.findOne(dto.getTheCase().getId());
		Organization org = orgRepos.findOne(dto.getOrganization().getId());

		Set<Dictionary> risks = new HashSet<>();
		if (dto.getRisks() != null) {
			dto.getRisks().parallelStream().filter(r -> (r != null) && CommonUtils.isPositive(r.getId(), true))
					.forEach(r -> {
						Dictionary d = dicRepos.findOne(r.getId());
						if (d != null) {
							risks.add(d);
						}
					});
		}

		entity.setTheCase(theCase);
		entity.setOrganization(org);

		entity.getRisks().clear();
		entity.getRisks().addAll(risks);

		entity = repos.save(entity);

		if (entity != null) {
			return new RiskInterviewDto(entity);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public RiskInterviewDto updateRisks(RiskInterviewDto dto) {
		if (dto == null) {
			throw new IllegalArgumentException("Risk interview is null.");
		}

		RiskInterview entity = null;

		if (CommonUtils.isPositive(dto.getId(), true)) {
			entity = repos.findOne(dto.getId());
		}

		if (entity == null) {
			throw new NullPointerException("Risk interview is not found.");
		}

		Set<Dictionary> risks = new HashSet<>();
		if (dto.getRisks() != null) {
			dto.getRisks().parallelStream().filter(r -> (r != null) && CommonUtils.isPositive(r.getId(), true))
					.forEach(r -> {
						Dictionary d = dicRepos.findOne(r.getId());
						if (d != null) {
							risks.add(d);
						}
					});
		}

		entity.getRisks().clear();
		entity.getRisks().addAll(risks);

		entity = repos.save(entity);

		if (entity != null) {
			return new RiskInterviewDto(entity);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteMultiple(RiskInterviewDto[] dtos) {

		if (CommonUtils.isEmpty(dtos)) {
			return;
		}

		for (RiskInterviewDto dto : dtos) {
			if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
				continue;
			}

			RiskInterview entity = repos.findOne(dto.getId());

			if (entity != null) {
				repos.delete(entity);
			}
		}
	}

}
