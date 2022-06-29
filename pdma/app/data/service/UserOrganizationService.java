package org.pepfar.pdma.app.data.service;

import java.util.List;

import org.pepfar.pdma.app.data.domain.UserOrganizationPK;
import org.pepfar.pdma.app.data.dto.UserOrganizationDto;

public interface UserOrganizationService
{

	public UserOrganizationDto findById(UserOrganizationPK id);

	public List<UserOrganizationDto> findAll(Long userId);

	public UserOrganizationDto saveOne(UserOrganizationDto dto);

	public void saveMultiple(UserOrganizationDto[] dtos);

	public void deleteMultiple(UserOrganizationDto[] dtos);
}
