package org.pepfar.pdma.app.data.service.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.pepfar.pdma.app.data.domain.Case;
import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.domain.QRecency;
import org.pepfar.pdma.app.data.domain.Recency;
import org.pepfar.pdma.app.data.dto.RecencyDto;
import org.pepfar.pdma.app.data.dto.RecencyFilterDto;
import org.pepfar.pdma.app.data.repository.CaseRepository;
import org.pepfar.pdma.app.data.repository.OrganizationRepository;
import org.pepfar.pdma.app.data.repository.RecencyRepository;
import org.pepfar.pdma.app.data.service.RecencyService;
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
public class RecencyServiceImpl implements RecencyService {

	@Autowired
	private RecencyRepository repos;

	@Autowired
	private CaseRepository caseRepos;

	@Autowired
	private OrganizationRepository orgRepos;

	@Autowired
	private EntityManager entityManager;

	@Override
	@Transactional(readOnly = true)
	public RecencyDto findById(Long id) {
		if (!CommonUtils.isPositive(id, true)) {
			return null;
		}

		Recency entity = repos.findOne(id);

		if (entity != null) {
			return new RecencyDto(entity);
		}

		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public RecencyDto findLatest(Long caseId) {

		if (!CommonUtils.isPositive(caseId, true)) {
			return null;
		}

		List<Recency> entities = entityManager
				.createQuery("SELECT e FROM Recency e where e.theCase.id = ?1 ORDER BY e.screenSampleDate DESC",
						Recency.class)
				.setParameter(1, caseId).setMaxResults(1).getResultList();

		if (entities.size() > 0) {
			return new RecencyDto(entities.get(0));
		} else {
			return null;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<RecencyDto> findAll(RecencyFilterDto filter) {

		if (filter == null || filter.getTheCase() == null) {
			return new ArrayList<>();
		}

		List<RecencyDto> ret = new ArrayList<>();

		repos.findAll(QRecency.recency.theCase.id.longValue().eq(filter.getTheCase().getId()),
				new Sort(new Order(Direction.DESC, "screenSampleDate"))).forEach(e -> {
					ret.add(new RecencyDto(e));
				});

		return ret;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<RecencyDto> findAllPageable(RecencyFilterDto filter) {

		if (filter == null) {
			filter = new RecencyFilterDto();
		}

		if (filter.getPageIndex() < 0) {
			filter.setPageIndex(0);
		}

		if (filter.getPageSize() <= 0) {
			filter.setPageSize(25);
		}

		QRecency q = QRecency.recency;
		Sort defaultSort = new Sort(new Order(Direction.DESC, "screenSampleDate"));
		Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize(), defaultSort);

		if (filter.getTheCase() == null || !CommonUtils.isPositive(filter.getTheCase().getId(), true)) {
			return new PageImpl<>(new ArrayList<>(), pageable, 0);
		}

		BooleanExpression be = q.theCase.id.eq(filter.getTheCase().getId().longValue());

		Page<Recency> page = repos.findAll(be, pageable);
		List<RecencyDto> content = new ArrayList<>();

		page.getContent().parallelStream().forEachOrdered(v -> {
			content.add(new RecencyDto(v));
		});

		return new PageImpl<>(content, pageable, page.getTotalElements());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public RecencyDto saveOne(RecencyDto dto) {

		if (dto == null) {
			throw new IllegalArgumentException("Recency data could not be null.");
		}

		Recency entity = null;
		if (CommonUtils.isPositive(dto.getId(), true)) {
			entity = repos.findOne(dto.getId());
		}

		if (entity == null) {
			entity = dto.toEntity();
			entity.setUid(UUID.randomUUID());
		} else {
			entity.setScreenSampleDate(dto.getScreenSampleDate());
			entity.setScreenTestDate(dto.getScreenTestDate());
			entity.setScreenResult(dto.getScreenResult());

			entity.setVlTestDate(dto.getVlTestDate());
			entity.setVlResult(dto.getVlResult());

			entity.setConfirmResult(dto.getConfirmResult());
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
			return new RecencyDto(entity);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteMultiple(RecencyDto[] dtos) {
		if (CommonUtils.isEmpty(dtos)) {
			return;
		}

		for (RecencyDto dto : dtos) {
			if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
				continue;
			}

			Recency entity = repos.findOne(dto.getId());

			if (entity != null) {
				repos.delete(entity);
			}
		}
	}

}
