package org.pepfar.pdma.app.api;

import java.util.List;

import org.pepfar.pdma.app.data.dto.RoleDto;
import org.pepfar.pdma.app.data.service.RoleService;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/role")
public class RoleRestController
{

	@Autowired
	private RoleService roleService;

	@RequestMapping(path = "/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RoleDto>> getRoles() {
		return new ResponseEntity<List<RoleDto>>(roleService.findAll(), HttpStatus.OK);
	}

	@RequestMapping(path = "/name/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RoleDto> getRole(@PathVariable("name") String name) {

		if (CommonUtils.isEmpty(name)) {
			throw new RuntimeException("Invalid role name found.");
		}

		return new ResponseEntity<RoleDto>(roleService.findOne(name), HttpStatus.OK);
	}

	@RequestMapping(path = "/id/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RoleDto> getRole(long id) {

		if (!CommonUtils.isPositive(id, true)) {
			throw new RuntimeException("Invalid role id found.");
		}

		return new ResponseEntity<RoleDto>(roleService.findOne(id), HttpStatus.OK);
	}

}
