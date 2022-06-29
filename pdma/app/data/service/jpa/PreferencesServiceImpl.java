package org.pepfar.pdma.app.data.service.jpa;

import java.util.List;
import java.util.stream.Collectors;

import org.pepfar.pdma.app.data.domain.Preferences;
import org.pepfar.pdma.app.data.domain.QPreferences;
import org.pepfar.pdma.app.data.dto.PreferencesDto;
import org.pepfar.pdma.app.data.repository.PreferencesRepository;
import org.pepfar.pdma.app.data.service.PreferencesService;
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

@Service
public class PreferencesServiceImpl implements PreferencesService {

	@Autowired
	private PreferencesRepository repos;

	private Sort defaultSort = new Sort(new Order(Direction.ASC, "name"));

	@Override
	@Transactional(readOnly = true)
	public PreferencesDto findById(Long id) {
		if (!CommonUtils.isPositive(id, true)) {
			return null;
		}

		Preferences entity = repos.findOne(id);

		if (entity != null) {
			return new PreferencesDto(entity);
		} else {
			return null;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public PreferencesDto findByName(String name) {
		if (CommonUtils.isEmpty(name)) {
			return null;
		}

		QPreferences q = QPreferences.preferences;

		Preferences entity = repos.findOne(q.name.eq(name));

		if (entity == null) {
			return null;
		} else {
			return new PreferencesDto(entity);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Page<PreferencesDto> findAll(int pageIndex, int pageSize) {

		if (pageIndex < 0) {
			pageIndex = 0;
		}

		if (pageSize <= 0) {
			pageSize = 25;
		}

		Pageable pageable = new PageRequest(pageIndex, pageSize, defaultSort);
		Page<Preferences> _page = repos.findAll(pageable);

		List<PreferencesDto> content = _page.getContent().stream().map(p -> new PreferencesDto(p))
				.collect(Collectors.toList());

		return new PageImpl<>(content, pageable, _page.getTotalElements());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public PreferencesDto saveOne(PreferencesDto dto) {
		if (CommonUtils.isNull(dto)) {
			throw new RuntimeException();
		}

		Preferences entity = null;

		if (CommonUtils.isPositive(dto.getId(), true)) {
			entity = repos.findOne(dto.getId());
		}

		if (entity == null) {
			entity = repos.findOne(QPreferences.preferences.name.equalsIgnoreCase(dto.getName()));
		}

		if (entity == null) {
			entity = dto.toEntity();
		} else {
			entity.setName(dto.getName());
			entity.setValue(dto.getValue());
		}

		entity = repos.save(entity);

		if (entity != null) {
			return new PreferencesDto(entity);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteMultiple(PreferencesDto[] dtos) {

		if (CommonUtils.isEmpty(dtos)) {
			return;
		}

		for (PreferencesDto dto : dtos) {

			Preferences entity = repos.findOne(dto.getId());

			if (CommonUtils.isNull(entity)) {
				throw new RuntimeException();
			}

			repos.delete(entity);
		}
	}

}
