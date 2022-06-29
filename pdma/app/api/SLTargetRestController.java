package org.pepfar.pdma.app.api;

import java.util.List;

import org.pepfar.pdma.app.data.dto.SLTargetDto;
import org.pepfar.pdma.app.data.dto.SLTargetFilterDto;
import org.pepfar.pdma.app.data.service.SLTargetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/sltarget")
public class SLTargetRestController
{

	@Autowired
	private SLTargetService service;

	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SLTargetDto> getSLTarget(@PathVariable("id") Long id) {
		SLTargetDto dto = service.findById(id);

		if (dto == null) {
			return new ResponseEntity<>(new SLTargetDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(dto, HttpStatus.OK);
	}

	@PostMapping(path = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<SLTargetDto>> getAllSLTargets(@RequestBody SLTargetFilterDto filter) {

		List<SLTargetDto> services = service.findAll(filter);

		return new ResponseEntity<>(services, HttpStatus.OK);
	}

	@PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<SLTargetDto>> getAllSLTargetsPageable(@RequestBody SLTargetFilterDto filter) {

		Page<SLTargetDto> services = service.findAllPageable(filter);

		return new ResponseEntity<>(services, HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ROLE_DONOR') or hasRole('ROLE_NATIONAL_MANAGER')")
	@RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SLTargetDto> saveSLTarget(@RequestBody SLTargetDto dto) {

		if (dto == null) {
			return new ResponseEntity<>(new SLTargetDto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.saveOne(dto);

		return new ResponseEntity<>(dto, HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ROLE_DONOR') or hasRole('ROLE_NATIONAL_MANAGER')")
	@RequestMapping(
			path = "/multiple",
			method = { RequestMethod.POST, RequestMethod.PUT },
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SLTargetDto[]> saveSLTargets(@RequestBody SLTargetDto[] dtos) {

		if (dtos == null) {
			return new ResponseEntity<>(new SLTargetDto[0], HttpStatus.BAD_REQUEST);
		}

		dtos = service.saveMultiple(dtos);

		return new ResponseEntity<>(dtos, HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ROLE_DONOR') or hasRole('ROLE_NATIONAL_MANAGER')")
	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteSLTargets(@RequestBody SLTargetDto[] dtos) {
		service.deleteMultiple(dtos);
	}

}
