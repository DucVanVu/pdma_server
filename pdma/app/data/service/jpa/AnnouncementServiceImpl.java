package org.pepfar.pdma.app.data.service.jpa;

import java.util.ArrayList;
import java.util.List;

import org.pepfar.pdma.app.data.aop.Loggable;
import org.pepfar.pdma.app.data.domain.Announcement;
import org.pepfar.pdma.app.data.domain.QAnnouncement;
import org.pepfar.pdma.app.data.dto.AnnouncementDto;
import org.pepfar.pdma.app.data.dto.AnnouncementFilterDto;
import org.pepfar.pdma.app.data.repository.AnnouncementRepository;
import org.pepfar.pdma.app.data.service.AnnouncementService;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.HtmlUtils;
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
public class AnnouncementServiceImpl implements AnnouncementService {

	@Autowired
	private AnnouncementRepository repos;

	@Autowired
	private HtmlUtils htmlUtils;

	@Override
	@Transactional(readOnly = true)
	public AnnouncementDto findById(Long id) {

		if (!CommonUtils.isPositive(id, true)) {
			return null;
		}

		Announcement entity = repos.findOne(id);

		if (entity != null) {
			return new AnnouncementDto(entity);
		}

		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<AnnouncementDto> findAllPageable(AnnouncementFilterDto filter) {

		if (filter == null) {
			filter = new AnnouncementFilterDto();
		}

		if (filter.getPageIndex() < 0) {
			filter.setPageIndex(0);
		}

		if (filter.getPageSize() <= 0) {
			filter.setPageSize(25);
		}

		QAnnouncement q = QAnnouncement.announcement;
		BooleanExpression be = q.id.isNotNull();

		if (!CommonUtils.isEmpty(filter.getKeyword())) {
			be = be.and(q.title.containsIgnoreCase(filter.getKeyword())
					.or(q.content.containsIgnoreCase(filter.getKeyword())));
		}

		if (filter.isPublishedOnly()) {
			be = be.and(q.status.eq(1));
		}

		Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize(),
				new Sort(new Order(Direction.DESC, "createDate")));
		Page<Announcement> page = repos.findAll(be, pageable);
		List<AnnouncementDto> content = new ArrayList<>();

		page.getContent().parallelStream().forEachOrdered(e -> {
			content.add(new AnnouncementDto(e));
		});

		return new PageImpl<>(content, pageable, page.getTotalElements());
	}

	@Override
	@Loggable
	@Transactional(rollbackFor = Exception.class)
	public AnnouncementDto saveOne(AnnouncementDto dto) {

		if (dto == null) {
			throw new IllegalArgumentException("Cannot save a null instance of Announcement class.");
		}

		Announcement entity = null;

		if (CommonUtils.isPositive(dto.getId(), true)) {
			entity = repos.findOne(dto.getId());
		}

		// remove XSS threats
		if (dto.getContent() != null) {
			dto.setContent(htmlUtils.removeXSSThreatsAlt(dto.getContent()));
		}

		if (entity == null) {
			entity = dto.toEntity();

			if (entity.getStatus() == 1) {
				entity.setPublishDate(CommonUtils.hanoiNow());
			}
		} else {
			entity.setTitle(dto.getTitle());
			entity.setContent(dto.getContent());

			if (entity.getStatus() == 0 && dto.getStatus() == 1) {
				entity.setPublishDate(CommonUtils.hanoiNow());
			} else if (entity.getStatus() == 1 && dto.getStatus() == 0) {
				entity.setPublishDate(null);
			}

			entity.setStatus(dto.getStatus());
		}

		entity = repos.save(entity);

		if (entity != null) {
			return new AnnouncementDto(entity);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	@Loggable
	@Transactional(rollbackFor = Exception.class)
	public void deleteMultiple(AnnouncementDto[] dtos) {
		if (CommonUtils.isEmpty(dtos)) {
			return;
		}

		for (AnnouncementDto dto : dtos) {

			if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
				continue;
			}

			Announcement entity = repos.findOne(dto.getId());

			if (entity != null) {
				repos.delete(entity);
			}
		}
	}

}
