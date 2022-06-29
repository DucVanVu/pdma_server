package org.pepfar.pdma.app.data.service.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.pepfar.pdma.app.data.domain.UserGroup;
import org.pepfar.pdma.app.data.dto.UserGroupDto;
import org.pepfar.pdma.app.data.repository.UserGroupRepository;
import org.pepfar.pdma.app.data.service.UserGroupService;
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
public class UserGroupServiceImpl implements UserGroupService
{

	@Autowired
	private UserGroupRepository repos;

	private Sort defaultSort = new Sort(new Order(Direction.ASC, "name"));

	@Override
	@Transactional(readOnly = true)
	public Page<UserGroupDto> findAll(int pageIndex, int pageSize) {

		if (pageIndex < 0) {
			pageIndex = 0;
		}

		if (pageSize <= 0) {
			pageSize = 25;
		}

		Pageable pageable = new PageRequest(pageIndex, pageSize, defaultSort);
		Page<UserGroup> _page = repos.findAll(pageable);

		List<UserGroupDto> content = _page.getContent().stream().map(ug -> new UserGroupDto(ug))
				.collect(Collectors.toList());

		return new PageImpl<>(content, pageable, _page.getTotalElements());
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserGroupDto> findAll() {
		List<UserGroupDto> ret = new ArrayList<UserGroupDto>();

		repos.findAll(defaultSort).forEach(ug -> {
			ret.add(new UserGroupDto(ug));
		});

		return ret;
	}

	@Override
	@Transactional(readOnly = true)
	public UserGroupDto findById(Long id) {

		if (!CommonUtils.isPositive(id, true)) {
			return null;
		}

		UserGroup ug = repos.findOne(id);

		if (ug != null) {
			return new UserGroupDto(ug);
		} else {
			return null;
		}
	}

	@Override
	@Transactional(rollbackFor = { Exception.class })
	public UserGroupDto saveOne(UserGroupDto dto) {

		if (dto == null) {
			throw new RuntimeException();
		}

		UserGroup ug = new UserGroup();

		if (CommonUtils.isPositive(dto.getId(), true)) {
			ug = repos.findOne(dto.getId());

			if (ug == null) {
				ug = new UserGroup();
			}
		}

		ug.setName(dto.getName());
		ug.setDescription(dto.getDescription());

		ug = repos.save(ug);

		if (ug != null) {
			return new UserGroupDto(ug);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	@Transactional(rollbackFor = { Exception.class })
	public void deleteMultiple(UserGroupDto[] dtos) {

		if (dtos == null || dtos.length <= 0) {
			return;
		}

		for (UserGroupDto dto : dtos) {
			UserGroup ug = repos.findOne(dto.getId());

			if (ug == null) {
				throw new RuntimeException();
			}

			repos.delete(ug);
		}
	}

}
