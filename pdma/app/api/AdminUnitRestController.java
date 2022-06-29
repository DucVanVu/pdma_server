package org.pepfar.pdma.app.api;

import java.util.List;

import org.pepfar.pdma.app.data.dto.AdminUnitDto;
import org.pepfar.pdma.app.data.dto.AdminUnitFilterDto;
import org.pepfar.pdma.app.data.service.AdminUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

//@CrossOrigin(origins = {})
@RestController
@RequestMapping(path = "/api/v1/admin_unit")
public class AdminUnitRestController {

	@Autowired
	private AdminUnitService service;

	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<AdminUnitDto> getAdminUnit(@PathVariable("id") Long id) {
		AdminUnitDto dto = service.findById(id);

		if (dto == null) {
			return new ResponseEntity<AdminUnitDto>(new AdminUnitDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<AdminUnitDto>(dto, HttpStatus.OK);
	}

	@GetMapping(path = "/code/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<AdminUnitDto> getAdminUnit(@PathVariable("code") String code) {
		AdminUnitDto dto = service.findByCode(code);
		return new ResponseEntity<AdminUnitDto>(dto, HttpStatus.OK);
	}
	
	@GetMapping(path = "/gsocode/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<AdminUnitDto> getAdminUnitByGsoCode(@PathVariable("code") String code) {
		AdminUnitDto dto = service.findByGsoCode(code);
		return new ResponseEntity<AdminUnitDto>(dto, HttpStatus.OK);
	}

	@PostMapping(path = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<AdminUnitDto>> getAllAdminUnits(@RequestBody AdminUnitFilterDto filter) {
		return new ResponseEntity<List<AdminUnitDto>>(service.findAll(filter), HttpStatus.OK);
	}

	@PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<AdminUnitDto>> getAllAdminUnitsPageable(@RequestBody AdminUnitFilterDto filter) {
		Page<AdminUnitDto> adminUnits = service.findAllPageable(filter);
		return new ResponseEntity<Page<AdminUnitDto>>(adminUnits, HttpStatus.OK);
	}

	@RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<AdminUnitDto> saveAdminUnit(@RequestBody AdminUnitDto dto) {

		if (dto == null) {
			return new ResponseEntity<AdminUnitDto>(new AdminUnitDto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.saveOne(dto);

		return new ResponseEntity<AdminUnitDto>(dto, HttpStatus.OK);
	}

	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteAdminUnits(@RequestBody AdminUnitDto[] dtos) {
		service.deleteMultiple(dtos);
	}
	
	@GetMapping(path = "/provinceByUser")
	public ResponseEntity<List<AdminUnitDto>> getAllProvinceByUser() {
		return new ResponseEntity<List<AdminUnitDto>>(service.findAllProvinceByUser(), HttpStatus.OK);
	}

}
