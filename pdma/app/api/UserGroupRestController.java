package org.pepfar.pdma.app.api;

import java.util.List;

import org.pepfar.pdma.app.data.dto.UserGroupDto;
import org.pepfar.pdma.app.data.service.UserGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/usergroup")
public class UserGroupRestController
{

	@Autowired
	private UserGroupService ugService;

	@RequestMapping(path = "/{id}", method = RequestMethod.GET)
	public ResponseEntity<UserGroupDto> getUserGroup(@PathVariable("id") Long id) {

		UserGroupDto dto = ugService.findById(id);

		if (dto == null) {
			return new ResponseEntity<UserGroupDto>(new UserGroupDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<UserGroupDto>(dto, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<UserGroupDto> newUserGroup(@RequestBody UserGroupDto dto) {

		if (dto == null) {
			return new ResponseEntity<UserGroupDto>(new UserGroupDto(), HttpStatus.BAD_REQUEST);
		}

		dto = ugService.saveOne(dto);

		return new ResponseEntity<UserGroupDto>(dto, HttpStatus.CREATED);
	}

	@RequestMapping(method = RequestMethod.PUT)
	public ResponseEntity<UserGroupDto> updateUserGroup(@RequestBody UserGroupDto dto) {

		if (dto == null) {
			return new ResponseEntity<UserGroupDto>(new UserGroupDto(), HttpStatus.BAD_REQUEST);
		}

		dto = ugService.saveOne(dto);

		return new ResponseEntity<UserGroupDto>(dto, HttpStatus.OK);
	}

	@RequestMapping(path = "/delete", method = RequestMethod.DELETE)
	public void deleteUserGroups(@RequestBody UserGroupDto[] dtos) {
		ugService.deleteMultiple(dtos);
	}

	@RequestMapping(path = "/list/{pageIndex}/{pageSize}", method = RequestMethod.GET)
	public ResponseEntity<Page<UserGroupDto>> getList(@PathVariable("pageIndex") int pageIndex,
			@PathVariable("pageSize") int pageSize) {

		return new ResponseEntity<Page<UserGroupDto>>(ugService.findAll(pageIndex, pageSize), HttpStatus.OK);
	}

	@RequestMapping(path = "/all", method = RequestMethod.GET)
	public ResponseEntity<List<UserGroupDto>> getList() {
		return new ResponseEntity<List<UserGroupDto>>(ugService.findAll(), HttpStatus.OK);
	}
}
