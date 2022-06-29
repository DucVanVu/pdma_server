package org.pepfar.pdma.app.data.service.jpa;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.pepfar.pdma.app.data.aop.Loggable;
import org.pepfar.pdma.app.data.domain.Dictionary;
import org.pepfar.pdma.app.data.domain.QDictionary;
import org.pepfar.pdma.app.data.dto.DictionaryDto;
import org.pepfar.pdma.app.data.dto.DictionaryFilterDto;
import org.pepfar.pdma.app.data.dto.DictionaryTypeDto;
import org.pepfar.pdma.app.data.repository.DictionaryRepository;
import org.pepfar.pdma.app.data.service.DictionaryService;
import org.pepfar.pdma.app.data.types.DictionaryType;
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
public class DictionaryServiceImpl implements DictionaryService {

	@Autowired
	private DictionaryRepository repos;

	@Override
	@Transactional(readOnly = true)
	public DictionaryDto findById(Long id) {

		if (!CommonUtils.isPositive(id, true)) {
			return null;
		}

		Dictionary entity = repos.findOne(id);

		if (entity != null) {
			return new DictionaryDto(entity);
		}

		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public DictionaryDto findByCode(String code) {

		if (CommonUtils.isEmpty(code)) {
			return null;
		}

		Dictionary entity = repos.findOne(QDictionary.dictionary.code.equalsIgnoreCase(code));

		if (entity != null) {
			return new DictionaryDto(entity);
		}

		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public List<DictionaryDto> findAll(DictionaryFilterDto filter) {

		List<DictionaryDto> list = new ArrayList<>();

		QDictionary q = QDictionary.dictionary;
		BooleanExpression be = q.id.isNotNull();

		if (filter.getType() != null) {
			be = be.and(q.type.eq(filter.getType()));
		}

		if (!CommonUtils.isEmpty(filter.getKeyword())) {
			be = be.and(q.value.containsIgnoreCase(filter.getKeyword())
					.or(q.valueEn.containsIgnoreCase(filter.getKeyword())));
		}

		Iterator<Dictionary> itr = repos.findAll(be, new Sort(new Order(Direction.ASC, "order"))).iterator();

		while (itr.hasNext()) {
			list.add(new DictionaryDto(itr.next()));
		}

		return list;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<DictionaryDto> findAllPageable(DictionaryFilterDto filter) {

		if (filter.getPageIndex() < 0) {
			filter.setPageIndex(0);
		}

		if (filter.getPageSize() <= 0) {
			filter.setPageSize(25);
		}

		QDictionary q = QDictionary.dictionary;
		BooleanExpression be = q.id.isNotNull();

		if (filter.getType() != null) {
			be = be.and(q.type.eq(filter.getType()));
		}

		if (!CommonUtils.isEmpty(filter.getKeyword())) {
			be = be.and(q.value.containsIgnoreCase(filter.getKeyword())
					.or(q.valueEn.containsIgnoreCase(filter.getKeyword())));
		}

		Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize(),
				new Sort(new Order(Direction.ASC, "order")));
		Page<Dictionary> _page = repos.findAll(be, pageable);
		List<DictionaryDto> content = new ArrayList<>();

		_page.getContent().stream().parallel().forEachOrdered(d -> {
			content.add(new DictionaryDto(d));
		});

		return new PageImpl<DictionaryDto>(content, pageable, _page.getTotalElements());
	}

	@Override
	@Transactional(readOnly = true)
	public List<DictionaryTypeDto> findMultiple(List<DictionaryType> types) {
		if (types == null || types.size() <= 0) {
			return new ArrayList<>();
		}

		List<DictionaryTypeDto> list = new ArrayList<>();
		types.forEach(t -> {
			DictionaryTypeDto dto = new DictionaryTypeDto();
			Set<DictionaryDto> set = new LinkedHashSet<>();

			Iterator<Dictionary> itr = repos
					.findAll(QDictionary.dictionary.type.eq(t), new Sort(new Order(Direction.ASC, "order"))).iterator();

			while (itr.hasNext()) {
				set.add(new DictionaryDto(itr.next()));
			}

			dto.setType(t);
			dto.setData(set);

			list.add(dto);
		});

		return list;
	}

	@Override
	@Transactional(readOnly = true)
	public boolean codeExists(DictionaryDto dto) {

		if (dto == null || CommonUtils.isEmpty(dto.getCode())) {
			return false;
		}

		QDictionary q = QDictionary.dictionary;
		BooleanExpression be = q.code.toLowerCase().equalsIgnoreCase(dto.getCode());

		if (CommonUtils.isPositive(dto.getId(), true) && repos.exists(dto.getId())) {
			be = q.id.ne(dto.getId().longValue()).and(q.code.equalsIgnoreCase(dto.getCode()));
		}

		return repos.exists(be);
	}

	@Override
	@Loggable
	@Transactional(rollbackFor = Exception.class)
	public DictionaryDto saveOne(DictionaryDto dto) {

		if (CommonUtils.isNull(dto)) {
			throw new RuntimeException();
		}

		Dictionary entity = null;

		if (CommonUtils.isPositive(dto.getId(), true)) {
			entity = repos.findOne(dto.getId());
		}

		if (entity == null) {
			entity = dto.toEntity();

			int nextOrder = repos.getMaxSortOrder(dto.getType()) + 1;
			entity.setOrder(nextOrder);
		} else {
			entity.setActive(dto.getActive());
			entity.setCode(dto.getCode());
			entity.setDescription(dto.getDescription());
			entity.setOrder(dto.getOrder());
			entity.setType(dto.getType());
			entity.setValue(dto.getValue());
			entity.setValueEn(dto.getValueEn());
		}

		entity = repos.save(entity);

		if (entity != null) {
			return new DictionaryDto(entity);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	@Loggable
	@Transactional(rollbackFor = Exception.class)
	public boolean saveSortOrder(DictionaryDto[] dtos) {
		if (CommonUtils.isEmpty(dtos)) {
			return false;
		}

		for (DictionaryDto dto : dtos) {
			if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
				continue;
			}

			Dictionary entity = repos.findOne(dto.getId());
			if (entity != null) {
				entity.setOrder(dto.getOrder());
				repos.save(entity);
			}
		}

		return true;
	}

	@Override
	@Loggable
	@Transactional(rollbackFor = Exception.class)
	public void deleteMultiple(DictionaryDto[] dtos) {

		if (CommonUtils.isEmpty(dtos)) {
			return;
		}

		for (DictionaryDto dto : dtos) {
			if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
				continue;
			}

			Dictionary entity = repos.findOne(dto.getId());

			if (CommonUtils.isNotNull(entity)) {
				repos.delete(entity);
			}
		}
	}

}
