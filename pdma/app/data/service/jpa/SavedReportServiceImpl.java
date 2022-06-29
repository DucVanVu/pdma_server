package org.pepfar.pdma.app.data.service.jpa;

import java.util.ArrayList;
import java.util.List;

import org.pepfar.pdma.app.data.domain.*;
import org.pepfar.pdma.app.data.dto.SavedReportDto;
import org.pepfar.pdma.app.data.dto.SavedReportFilterDto;
import org.pepfar.pdma.app.data.repository.SavedReportRepository;
import org.pepfar.pdma.app.data.repository.UserOrganizationRepository;
import org.pepfar.pdma.app.data.service.SavedReportService;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.SecurityUtils;
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
public class SavedReportServiceImpl implements SavedReportService {

	@Autowired
	private SavedReportRepository repos;

	@Autowired
	private UserOrganizationRepository uoRepos;

	private Sort defaultSort = new Sort(new Order(Direction.ASC, "createDate"));

	@Override
	@Transactional(readOnly = true)
	public SavedReportDto findById(Long id, boolean includeContent) {

		if (!CommonUtils.isPositive(id, true)) {
			return null;
		}

		SavedReport entity = repos.findOne(id);

		if (entity != null) {
			return new SavedReportDto(entity, includeContent);
		} else {
			return null;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Page<SavedReportDto> findAll(SavedReportFilterDto filter) {

		if (filter == null) {
			filter = new SavedReportFilterDto();
		}

		if (filter.getPageIndex() < 0) {
			filter.setPageIndex(0);
		}

		if (filter.getPageSize() <= 0) {
			filter.setPageSize(25);
		}

		QUser qUser = QUser.user;
		QSavedReport q = QSavedReport.savedReport;
		BooleanExpression be = q.id.isNotNull();

		if (!CommonUtils.isEmpty(filter.getKeyword())) {
			be = be.and(q.title.containsIgnoreCase(filter.getKeyword()));
		}

		// The user must have read permission on at least one organization
		User user = SecurityUtils.getCurrentUser();

		List<Long> ids = new ArrayList<>();
		Iterable<UserOrganization> ous = uoRepos.findAll(qUser.id.longValue().eq(user.getId()));
		ous.forEach(uo -> {
			ids.add(uo.getOrganization().getId());
		});

		if (CommonUtils.isEmpty(ids)) {
			ids.add(0l);
		}

		if (!CommonUtils.isEmpty(ids)) {
			be = be.and(q.organization.id.longValue().in(ids));
		}

		Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize(), defaultSort);
		Page<SavedReport> _page = repos.findAll(be, pageable);
		List<SavedReportDto> content = new ArrayList<>();
		
		_page.getContent().parallelStream().forEachOrdered(e -> {
			content.add(new SavedReportDto(e, false));
		});

		return new PageImpl<>(content, pageable, _page.getTotalElements());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public SavedReportDto saveOne(SavedReportDto dto) {

		if (CommonUtils.isNull(dto)) {
			throw new RuntimeException();
		}

		SavedReport entity = null;

		if (CommonUtils.isPositive(dto.getId(), true)) {
			entity = repos.findOne(dto.getId());
		}

		if (entity != null) {
			entity.setTitle(dto.getTitle());
		} else {
			entity = dto.toEntity();
		}

		entity = repos.save(entity);

		if (entity != null) {
			return new SavedReportDto(entity, false);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteMultiple(SavedReportDto[] dtos) {

		if (CommonUtils.isEmpty(dtos)) {
			return;
		}

		for (SavedReportDto dto : dtos) {
			if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
				continue;
			}

			SavedReport entity = repos.findOne(dto.getId());

			if (CommonUtils.isNotNull(entity)) {
				repos.delete(entity);
			}
		}
	}

}
