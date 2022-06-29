package org.pepfar.pdma.app.data.service.jpa;

import java.util.ArrayList;
import java.util.List;

import org.pepfar.pdma.app.data.domain.Dictionary;
import org.pepfar.pdma.app.data.domain.QDictionary;
import org.pepfar.pdma.app.data.domain.QRegimen;
import org.pepfar.pdma.app.data.domain.Regimen;
import org.pepfar.pdma.app.data.dto.RegimenDto;
import org.pepfar.pdma.app.data.dto.RegimenFilterDto;
import org.pepfar.pdma.app.data.repository.DictionaryRepository;
import org.pepfar.pdma.app.data.repository.RegimenRepository;
import org.pepfar.pdma.app.data.service.RegimenService;
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
public class RegimenServiceImpl implements RegimenService {

	@Autowired
	private RegimenRepository repos;

	@Autowired
	private DictionaryRepository dicRepos;

	@Override
	@Transactional(readOnly = true)
	public RegimenDto findById(Long id) {

		if (!CommonUtils.isPositive(id, true)) {
			return null;
		}

		Regimen entity = repos.findOne(id);

		if (entity != null) {
			return new RegimenDto(entity);
		}

		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public long count() {
		return repos.count();
	}

	@Override
	@Transactional(readOnly = true)
	public RegimenDto findByShortName(String shortName) {
		if (CommonUtils.isEmpty(shortName)) {
			return null;
		}

		QRegimen q = QRegimen.regimen;
		Regimen entity = repos.findOne(q.shortName.equalsIgnoreCase(shortName));

		if (entity != null) {
			return new RegimenDto(entity);
		}

		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public List<RegimenDto> findAll(RegimenFilterDto filter) {

		if (filter == null) {
			filter = new RegimenFilterDto();
		}

		QRegimen q = QRegimen.regimen;
		BooleanExpression be = q.id.isNotNull();

		if (filter.getDisease() != null) {

			if (CommonUtils.isPositive(filter.getDisease().getId(), true)) {
				be = be.and(q.disease.id.eq(filter.getDisease().getId().longValue()));
			}

			if (!CommonUtils.isEmpty(filter.getDisease().getCode())) {
				QDictionary qDic = QDictionary.dictionary;
				Dictionary dic = dicRepos
						.findOne(qDic.code.isNotNull().and(qDic.code.equalsIgnoreCase(filter.getDisease().getCode())));

				if (dic != null) {
					be = be.and(q.disease.id.eq(dic.getId().longValue()));
				}
			}
		}

		if (!CommonUtils.isEmpty(filter.getKeyword())) {
			be = be.and(q.name.containsIgnoreCase(filter.getKeyword())
					.or(q.shortName.containsIgnoreCase(filter.getKeyword()))
					.or(q.description.containsIgnoreCase(filter.getKeyword())));
		}

		List<RegimenDto> list = new ArrayList<>();
		repos.findAll(be, q.line.asc(), q.name.asc()).forEach(r -> {
			list.add(new RegimenDto(r));
		});

		return list;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<RegimenDto> findAllPageable(RegimenFilterDto filter) {

		if (filter == null) {
			filter = new RegimenFilterDto();
		}

		if (filter.getPageIndex() < 0) {
			filter.setPageIndex(0);
		}

		if (filter.getPageSize() <= 0) {
			filter.setPageSize(25);
		}

		QRegimen q = QRegimen.regimen;
		BooleanExpression be = q.id.isNotNull();
		Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize(),
				new Sort(new Order(Direction.ASC, "name")));

		if (filter.getDisease() != null) {

			if (CommonUtils.isPositive(filter.getDisease().getId(), true)) {
				be = be.and(q.disease.id.eq(filter.getDisease().getId().longValue()));
			}

			if (!CommonUtils.isEmpty(filter.getDisease().getCode())) {
				QDictionary qDic = QDictionary.dictionary;
				Dictionary dic = dicRepos
						.findOne(qDic.code.isNotNull().and(qDic.code.equalsIgnoreCase(filter.getDisease().getCode())));

				if (dic != null) {
					be = be.and(q.disease.id.eq(filter.getDisease().getId().longValue()));
				}
			}
		}

		if (!CommonUtils.isEmpty(filter.getKeyword())) {
			be = be.and(q.name.containsIgnoreCase(filter.getKeyword())
					.or(q.shortName.containsIgnoreCase(filter.getKeyword()))
					.or(q.description.containsIgnoreCase(filter.getKeyword())));
		}

		Page<Regimen> page = repos.findAll(be, pageable);
		List<RegimenDto> content = new ArrayList<>();

		page.getContent().parallelStream().forEachOrdered(r -> {
			content.add(new RegimenDto(r));
		});

		return new PageImpl<>(content, pageable, page.getTotalElements());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public RegimenDto saveOne(RegimenDto dto) {

		if (dto == null || dto.getDisease() == null) {
			throw new IllegalArgumentException("Regimen could not be null.");
		}

		Regimen entity = null;
		if (CommonUtils.isPositive(dto.getId(), true)) {
			entity = repos.findOne(dto.getId());
		}

		if (entity == null) {
			entity = dto.toEntity();
		} else {
			entity.setShortName(dto.getShortName());
			entity.setName(dto.getName());
			entity.setLine(dto.getLine());
			entity.setDescription(dto.getDescription());
		}

		Dictionary disease = null;
		if (CommonUtils.isPositive(dto.getDisease().getId(), true)) {
			disease = dicRepos.findOne(dto.getDisease().getId());
		}

		entity.setDisease(disease);
		entity = repos.save(entity);

		if (entity != null) {
			return new RegimenDto(entity);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteMultiple(RegimenDto[] dtos) {

		if (CommonUtils.isEmpty(dtos)) {
			return;
		}

		for (RegimenDto dto : dtos) {
			if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
				continue;
			}

			Regimen entity = repos.findOne(dto.getId());

			if (entity != null) {
				repos.delete(entity);
			}
		}
	}

}
