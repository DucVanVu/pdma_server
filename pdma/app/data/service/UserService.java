package org.pepfar.pdma.app.data.service;

import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.pepfar.pdma.app.data.dto.PreventionFilterDto;
import org.pepfar.pdma.app.data.dto.UserDto;
import org.pepfar.pdma.app.data.dto.UserFilterDto;
import org.springframework.data.domain.Page;

public interface UserService
{

	public UserDto findById(Long id);

	public UserDto findByUsername(String username);

	public Page<UserDto> findAllPageable(UserFilterDto filter);

	public List<UserDto> findAll(UserFilterDto filter);

	public byte[] getProfilePhoto(String username);

	public UserDto saveOne(UserDto dto);

	public UserDto savePhoto(UserDto dto);

	public boolean passwordMatch(UserDto dto);

	public UserDto changePassword(UserDto dto);

	public UserDto uploadPhoto(UserDto dto);

	public void deleteMultiple(UserDto[] dtos);

	public boolean foundDuplicateUsername(Long userId, String username);

	public boolean foundDuplicateEmail(Long userId, String email);

	public void resetPasswordForAllUsersExceptAdmin(String password);

	public void createOrUpdateDemoUsers();
	
	Workbook exportUser(UserFilterDto filter);
}
