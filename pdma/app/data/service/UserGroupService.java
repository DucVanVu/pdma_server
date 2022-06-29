package org.pepfar.pdma.app.data.service;

import java.util.List;

import org.pepfar.pdma.app.data.dto.UserGroupDto;
import org.springframework.data.domain.Page;

public interface UserGroupService
{

	public Page<UserGroupDto> findAll(int pageIndex, int pageSize);

	public List<UserGroupDto> findAll();

	public UserGroupDto findById(Long id);

	public UserGroupDto saveOne(UserGroupDto dto);

	public void deleteMultiple(UserGroupDto[] dtos);
}
