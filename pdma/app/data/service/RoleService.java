package org.pepfar.pdma.app.data.service;

import java.util.List;

import org.pepfar.pdma.app.data.dto.RoleDto;

public interface RoleService
{

	public List<RoleDto> findAll();

	public RoleDto findOne(Long id);

	public RoleDto findOne(String roleName);

	public RoleDto saveOne(RoleDto dto);

}
