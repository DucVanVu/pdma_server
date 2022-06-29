package org.pepfar.pdma.app.data.service.jpa;

import java.util.ArrayList;
import java.util.List;

import org.pepfar.pdma.app.data.domain.QRole;
import org.pepfar.pdma.app.data.domain.Role;
import org.pepfar.pdma.app.data.dto.RoleDto;
import org.pepfar.pdma.app.data.repository.RoleRepository;
import org.pepfar.pdma.app.data.service.RoleService;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleServiceImpl implements RoleService
{

	@Autowired
	private RoleRepository repos;

	@Override
	@Transactional(readOnly = true)
	public RoleDto findOne(Long id) {

		if (id == null || id <= 0) {
			return null;
		}

		Role role = repos.findOne(id);

		if (role != null) {
			return new RoleDto(role);
		} else {
			return null;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public RoleDto findOne(String roleName) {

		if (CommonUtils.isEmpty(roleName)) {
			return null;
		}

		QRole q = QRole.role;
		Role role = repos.findOne(q.name.equalsIgnoreCase(roleName));

		if (role != null) {
			return new RoleDto(role);
		} else {
			return null;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<RoleDto> findAll() {
		List<RoleDto> roles = new ArrayList<RoleDto>();
		repos.findAll().iterator().forEachRemaining(r -> {
			roles.add(new RoleDto(r));
		});

		return roles;
	}

	@Override
	@Transactional(rollbackFor = { Exception.class })
	public RoleDto saveOne(RoleDto dto) {

		if (dto == null) {
			throw new RuntimeException();
		}

		Role role = null;

		if (CommonUtils.isPositive(dto.getId(), true)) {
			role = repos.findOne(dto.getId());
		}

		if (role == null) {
			role = dto.toEntity();
		} else {
			role.setName(dto.getName());
		}

		role = repos.save(role);

		if (role != null) {
			return new RoleDto(role);
		} else {
			throw new RuntimeException();
		}
	}

}
