package org.pepfar.pdma.app.api;

import java.util.List;

import org.pepfar.pdma.app.data.domain.UserOrganizationPK;
import org.pepfar.pdma.app.data.dto.UserOrganizationDto;
import org.pepfar.pdma.app.data.service.UserOrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/user_org")
public class UserOrganizationRestController
{

	@Autowired
	private UserOrganizationService service;

	@GetMapping(path = "/{userId}/{orgId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<UserOrganizationDto> getUserOrganization(@PathVariable("userId") Long userId,
			@PathVariable("orgId") Long orgId) {
		UserOrganizationDto dto = service.findById(new UserOrganizationPK(userId, orgId));

		if (dto == null) {
			return new ResponseEntity<>(new UserOrganizationDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(dto, HttpStatus.OK);
	}

	@GetMapping(path = "/list/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<UserOrganizationDto>> getAllUserOrganizations(@PathVariable("userId") Long userId) {

		List<UserOrganizationDto> services = service.findAll(userId);

		return new ResponseEntity<>(services, HttpStatus.OK);
	}

	@RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean> saveUserOrganizations(@RequestBody UserOrganizationDto[] dtos) {

		if (dtos == null) {
			return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
		}

		service.saveMultiple(dtos);

		return new ResponseEntity<>(true, HttpStatus.OK);
	}

	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteUserOrganizations(@RequestBody UserOrganizationDto[] dtos) {
		service.deleteMultiple(dtos);
	}
}
