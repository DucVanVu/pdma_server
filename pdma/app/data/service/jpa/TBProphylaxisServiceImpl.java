package org.pepfar.pdma.app.data.service.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.pepfar.pdma.app.data.domain.Case;
import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.domain.QTBProphylaxis;
import org.pepfar.pdma.app.data.domain.TBProphylaxis;
import org.pepfar.pdma.app.data.dto.TBProphylaxisDto;
import org.pepfar.pdma.app.data.dto.TBProphylaxisFilterDto;
import org.pepfar.pdma.app.data.repository.CaseRepository;
import org.pepfar.pdma.app.data.repository.OrganizationRepository;
import org.pepfar.pdma.app.data.repository.TBProphylaxisRepository;
import org.pepfar.pdma.app.data.service.TBProphylaxisService;
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
public class TBProphylaxisServiceImpl implements TBProphylaxisService {

	@Autowired
	private TBProphylaxisRepository repos;

	@Autowired
	private CaseRepository caseRepos;

	@Autowired
	private OrganizationRepository orgRepos;

	@Autowired
	private EntityManager entityManager;

	@Override
	@Transactional(readOnly = true)
	public TBProphylaxisDto findById(Long id) {
		if (!CommonUtils.isPositive(id, true)) {
			return null;
		}

		TBProphylaxis entity = repos.findOne(id);

		if (entity != null) {
			return new TBProphylaxisDto(entity);
		}

		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public TBProphylaxisDto findLatest(Long caseId) {
		if (!CommonUtils.isPositive(caseId, true)) {
			return null;
		}

		List<TBProphylaxis> entities = entityManager
				.createQuery("SELECT e FROM TBProphylaxis e where e.theCase.id = ?1 ORDER BY e.startDate DESC",
						TBProphylaxis.class)
				.setParameter(1, caseId).setMaxResults(1).getResultList();

		if (entities.size() > 0) {
			return new TBProphylaxisDto(entities.get(0));
		} else {
			return null;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<TBProphylaxisDto> findAll(TBProphylaxisFilterDto filter) {

		if (filter == null || filter.getTheCase() == null) {
			return new ArrayList<>();
		}

		List<TBProphylaxisDto> ret = new ArrayList<>();

		repos.findAll(QTBProphylaxis.tBProphylaxis.theCase.id.longValue().eq(filter.getTheCase().getId()),
				new Sort(new Order(Direction.DESC, "startDate"))).forEach(e -> {
					ret.add(new TBProphylaxisDto(e));
				});

		return ret;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<TBProphylaxisDto> findAllPageable(TBProphylaxisFilterDto filter) {

		if (filter == null) {
			filter = new TBProphylaxisFilterDto();
		}

		if (filter.getPageIndex() < 0) {
			filter.setPageIndex(0);
		}

		if (filter.getPageSize() <= 0) {
			filter.setPageSize(25);
		}

		QTBProphylaxis q = QTBProphylaxis.tBProphylaxis;
		Sort defaultSort = new Sort(new Order(Direction.DESC, "startDate"));
		Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize(), defaultSort);

		if (filter.getTheCase() == null || !CommonUtils.isPositive(filter.getTheCase().getId(), true)) {
			return new PageImpl<>(new ArrayList<>(), pageable, 0);
		}

		BooleanExpression be = q.theCase.id.eq(filter.getTheCase().getId().longValue());
		Page<TBProphylaxis> page = repos.findAll(be, pageable);
		List<TBProphylaxisDto> content = new ArrayList<>();

		page.getContent().parallelStream().forEachOrdered(v -> {
			content.add(new TBProphylaxisDto(v));
		});

		return new PageImpl<>(content, pageable, page.getTotalElements());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public TBProphylaxisDto saveOne(TBProphylaxisDto dto) {

		if (dto == null) {
			throw new IllegalArgumentException("TBProphylaxis data could not be null.");
		}

		TBProphylaxis entity = null;
		if (CommonUtils.isPositive(dto.getId(), true)) {
			entity = repos.findOne(dto.getId());
		}

		if (entity == null) {
			entity = dto.toEntity();
			entity.setUid(UUID.randomUUID());
		} else {
			entity.setStartDate(dto.getStartDate());
			entity.setEndDate(dto.getEndDate());
			entity.setRegimen(dto.getRegimen());
			entity.setResult(dto.getResult());
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
			return new TBProphylaxisDto(entity);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteMultiple(TBProphylaxisDto[] dtos) {
		if (CommonUtils.isEmpty(dtos)) {
			return;
		}

		for (TBProphylaxisDto dto : dtos) {
			if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
				continue;
			}

			TBProphylaxis entity = repos.findOne(dto.getId());

			if (entity != null) {
				repos.delete(entity);
			}
		}
	}

}
