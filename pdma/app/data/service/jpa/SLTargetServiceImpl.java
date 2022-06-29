package org.pepfar.pdma.app.data.service.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.pepfar.pdma.app.data.aop.Loggable;
import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.domain.QSLTarget;
import org.pepfar.pdma.app.data.domain.SLTarget;
import org.pepfar.pdma.app.data.dto.SLTargetDto;
import org.pepfar.pdma.app.data.dto.SLTargetFilterDto;
import org.pepfar.pdma.app.data.repository.OrganizationRepository;
import org.pepfar.pdma.app.data.repository.SLTargetRepository;
import org.pepfar.pdma.app.data.service.SLTargetService;
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
public class SLTargetServiceImpl implements SLTargetService
{

	@Autowired
	private SLTargetRepository repos;

	@Autowired
	private OrganizationRepository orgRepos;

	@Override
	@Transactional(readOnly = true)
	public SLTargetDto findById(Long id) {
		if (!CommonUtils.isPositive(id, true)) {
			return null;
		}

		SLTarget entity = repos.findOne(id);

		if (entity != null) {
			return new SLTargetDto(entity);
		}

		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public List<SLTargetDto> findAll(SLTargetFilterDto filter) {

		if (filter == null) {
			filter = new SLTargetFilterDto();
		}

		if (filter.getPageIndex() < 0) {
			filter.setPageIndex(0);
		}

		if (filter.getPageSize() <= 0) {
			filter.setPageSize(25);
		}

		QSLTarget q = QSLTarget.sLTarget;
		BooleanExpression be = q.id.isNotNull();

		if (filter.getIndicator() != null) {
			be = be.and(q.indicator.eq(filter.getIndicator()));
		}

		if (filter.getFiscalYear() > 0) {
			be = be.and(q.fiscalYear.eq(filter.getFiscalYear()));
		}

		if (!CommonUtils.isEmpty(filter.getKeyword())) {
			be = be.and(q.site.name.containsIgnoreCase(filter.getKeyword()));
		}

		if (!CommonUtils.isEmpty(filter.getSites())) {
			if (!CommonUtils.isEmpty(filter.getSites())) {
				List<Long> ids = new ArrayList<>();
				Stream.of(filter.getSites()).parallel()
						.filter(o -> (o != null) && CommonUtils.isPositive(o.getId(), true)).forEach(o -> {
							ids.add(o.getId());
						});

				be = q.id.longValue().in(ids);
			}
		}

		List<SLTargetDto> dtos = new ArrayList<>();
		Iterable<SLTarget> entities = repos.findAll(be, q.site.name.asc());
		entities.forEach(e -> {
			dtos.add(new SLTargetDto(e));
		});

		return dtos;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<SLTargetDto> findAllPageable(SLTargetFilterDto filter) {

		if (filter == null) {
			filter = new SLTargetFilterDto();
		}

		if (filter.getPageIndex() < 0) {
			filter.setPageIndex(0);
		}

		if (filter.getPageSize() <= 0) {
			filter.setPageSize(25);
		}

		QSLTarget q = QSLTarget.sLTarget;
		BooleanExpression be = q.id.isNotNull();

		if (filter.getFiscalYear() > 0) {
			be = be.and(q.fiscalYear.eq(filter.getFiscalYear()));
		}

		if (!CommonUtils.isEmpty(filter.getKeyword())) {
			be = be.and(q.site.name.containsIgnoreCase(filter.getKeyword()));
		}

		if (!CommonUtils.isEmpty(filter.getSites())) {
			if (!CommonUtils.isEmpty(filter.getSites())) {
				List<Long> ids = new ArrayList<>();
				Stream.of(filter.getSites()).parallel()
						.filter(o -> (o != null) && CommonUtils.isPositive(o.getId(), true)).forEach(o -> {
							ids.add(o.getId());
						});

				be = q.id.longValue().in(ids);
			}
		}

		Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize(),
				new Sort(new Order(Direction.ASC, "site.name")));

		List<SLTargetDto> dtos = new ArrayList<>();
		Page<SLTarget> page = repos.findAll(be, pageable);
		page.getContent().parallelStream().forEachOrdered(e -> {
			dtos.add(new SLTargetDto(e));
		});

		return new PageImpl<>(dtos, pageable, page.getTotalElements());
	}

	@Override
	@Loggable
	@Transactional(rollbackFor = Exception.class)
	public SLTargetDto saveOne(SLTargetDto dto) {

		if (dto == null) {
			throw new IllegalArgumentException("Cannot save a null instance of SLTarget.");
		}

		if (dto.getSite() == null || !CommonUtils.isPositive(dto.getId(), true) || !orgRepos.exists(dto.getId())) {
			throw new RuntimeException("An invalid organization unit is found.");
		}

		SLTarget entity = null;
		if (CommonUtils.isPositive(dto.getId(), true)) {
			entity = repos.findOne(dto.getId());
		}

		if (entity == null) {
			entity = dto.toEntity();
		} else {
			entity.setIndicator(dto.getIndicator());
			entity.setFiscalYear(dto.getFiscalYear());
			entity.setTarget(dto.getTarget());
		}

		Organization org = orgRepos.findOne(dto.getSite().getId());
		entity.setSite(org);

		entity = repos.save(entity);

		if (entity != null) {
			return new SLTargetDto(entity);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	@Loggable
	@Transactional(rollbackFor = Exception.class)
	public SLTargetDto[] saveMultiple(SLTargetDto[] dtos) {

		if (CommonUtils.isEmpty(dtos)) {
			return new SLTargetDto[0];
		}

		QSLTarget q = QSLTarget.sLTarget;
		List<SLTargetDto> savedOnes = new ArrayList<>();

		for (SLTargetDto dto : dtos) {

			if (dto.getSite() == null || !CommonUtils.isPositive(dto.getSite().getId(), true)
					|| !CommonUtils.isPositive(dto.getFiscalYear(), true)
					|| !CommonUtils.isPositive(dto.getTarget(), false)) {
				continue;
			}

			SLTarget entity = null;
			if (CommonUtils.isPositive(dto.getId(), true)) {
				entity = repos.findOne(dto.getId());
			}

			if (entity == null) {
				entity = repos.findOne(q.site.id.longValue().eq(dto.getSite().getId())
						.and(q.fiscalYear.eq(dto.getFiscalYear())).and(q.indicator.eq(dto.getIndicator())));
			}

			if (entity == null) {
				entity = dto.toEntity();
			} else {
				entity.setIndicator(dto.getIndicator());
				entity.setFiscalYear(dto.getFiscalYear());
				entity.setTarget(dto.getTarget());
			}

			Organization org = orgRepos.findOne(dto.getSite().getId());
			entity.setSite(org);

			entity = repos.save(entity);

			if (entity != null) {
				savedOnes.add(new SLTargetDto(entity));
			}
		}

		return savedOnes.toArray(new SLTargetDto[0]);
	}

	@Override
	@Loggable
	@Transactional(rollbackFor = Exception.class)
	public void deleteMultiple(SLTargetDto[] dtos) {
		if (CommonUtils.isEmpty(dtos)) {
			return;
		}

		for (SLTargetDto dto : dtos) {
			if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
				continue;
			}

			SLTarget entity = repos.findOne(dto.getId());

			if (entity != null) {
				repos.delete(entity);
			}
		}
	}

}
