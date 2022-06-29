package org.pepfar.pdma.app.api;

import java.util.List;

import org.pepfar.pdma.app.data.dto.HepatitisDto;
import org.pepfar.pdma.app.data.dto.HepatitisFilterDto;
import org.pepfar.pdma.app.data.service.HepatitisService;
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

@RestController
@RequestMapping(path = "/api/v1/hepatitis")
public class HepatitisRestController {

	@Autowired
	private HepatitisService service;

	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<HepatitisDto> getTest(@PathVariable("id") Long id) {
		HepatitisDto dto = service.findById(id);

		if (dto == null) {
			return new ResponseEntity<HepatitisDto>(new HepatitisDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<HepatitisDto>(dto, HttpStatus.OK);
	}

	@PostMapping(path = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<HepatitisDto>> getAllTests(@RequestBody HepatitisFilterDto filter) {

		List<HepatitisDto> services = service.findAll(filter);

		return new ResponseEntity<List<HepatitisDto>>(services, HttpStatus.OK);
	}

	@PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<HepatitisDto>> getAllTestsPageable(@RequestBody HepatitisFilterDto filter) {

		Page<HepatitisDto> services = service.findAllPageable(filter);

		return new ResponseEntity<Page<HepatitisDto>>(services, HttpStatus.OK);
	}

	@RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<HepatitisDto> saveTest(@RequestBody HepatitisDto dto) {

		if (dto == null) {
			return new ResponseEntity<HepatitisDto>(new HepatitisDto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.saveOne(dto);

		return new ResponseEntity<HepatitisDto>(dto, HttpStatus.OK);
	}

	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteTests(@RequestBody HepatitisDto[] dtos) {
		service.deleteMultiple(dtos);
	}

}
