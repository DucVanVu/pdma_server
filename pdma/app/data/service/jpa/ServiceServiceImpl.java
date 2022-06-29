package org.pepfar.pdma.app.data.service.jpa;

import java.util.ArrayList;
import java.util.List;

import org.pepfar.pdma.app.data.domain.QService;
import org.pepfar.pdma.app.data.domain.Service;
import org.pepfar.pdma.app.data.dto.ServiceDto;
import org.pepfar.pdma.app.data.repository.ServiceRepository;
import org.pepfar.pdma.app.data.service.ServiceService;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.dsl.BooleanExpression;

@org.springframework.stereotype.Service
public class ServiceServiceImpl implements ServiceService
{

	@Autowired
	private ServiceRepository repos;

	@Override
	@Transactional(readOnly = true)
	public ServiceDto findById(Long id) {

		if (!CommonUtils.isPositive(id, true)) {
			return null;
		}

		Service entity = repos.findOne(id);

		if (entity != null) {
			return new ServiceDto(entity);
		}

		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public ServiceDto findByCode(String code) {

		if (CommonUtils.isEmpty(code)) {
			return null;
		}

		Service entity = repos.findOne(QService.service.code.equalsIgnoreCase(code));

		if (entity != null) {
			return new ServiceDto(entity);
		}

		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public List<ServiceDto> findAll() {
		List<ServiceDto> list = new ArrayList<>();

		repos.findAll(QService.service.createDate.desc()).forEach(s -> {
			list.add(new ServiceDto(s));
		});

		return list;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<ServiceDto> findAllPageable(int pageIndex, int pageSize) {

		if (pageIndex < 0) {
			pageIndex = 0;
		}

		if (pageSize <= 0) {
			pageSize = 25;
		}

		Pageable pageable = new PageRequest(pageIndex, pageSize, new Sort(new Order(Direction.DESC, "createDate")));
		Page<Service> page = repos.findAll(pageable);
		List<ServiceDto> content = new ArrayList<>();

		page.getContent().parallelStream().forEachOrdered(s -> {
			content.add(new ServiceDto(s));
		});

		return new PageImpl<>(content, pageable, page.getTotalElements());
	}

	@Override
	@Transactional(readOnly = true)
	public boolean codeExists(ServiceDto dto) {

		if (dto == null || CommonUtils.isEmpty(dto.getCode())) {
			return false;
		}

		QService q = QService.service;
		BooleanExpression be = q.code.toLowerCase().equalsIgnoreCase(dto.getCode());

		if (CommonUtils.isPositive(dto.getId(), true) && repos.exists(dto.getId())) {
			be = q.id.ne(dto.getId().longValue()).and(q.code.equalsIgnoreCase(dto.getCode()));
		}

		return repos.exists(be);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ServiceDto saveOne(ServiceDto dto) {

		if (dto == null) {
			throw new IllegalArgumentException("Service could not be null.");
		}

		Service entity = null;
		if (CommonUtils.isPositive(dto.getId(), true)) {
			entity = repos.findOne(dto.getId());
		}

		if (entity == null) {
			entity = dto.toEntity();
		} else {
			entity.setCode(dto.getCode());
			entity.setName(dto.getName());
			entity.setDescription(dto.getDescription());
		}

		entity = repos.save(entity);

		if (entity != null) {
			return new ServiceDto(entity);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteMultiple(ServiceDto[] dtos) {

		if (CommonUtils.isEmpty(dtos)) {
			return;
		}

		for (ServiceDto dto : dtos) {
			if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
				continue;
			}

			Service entity = repos.findOne(dto.getId());

			if (entity != null) {
				repos.delete(entity);
			}
		}
	}

}
