package org.pepfar.pdma.app.api;

import java.util.List;

import org.pepfar.pdma.app.data.dto.RecencyDto;
import org.pepfar.pdma.app.data.dto.RecencyFilterDto;
import org.pepfar.pdma.app.data.service.RecencyService;
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
@RequestMapping(path = "/api/v1/recency")
public class RecencyRestController {

	@Autowired
	private RecencyService service;

	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RecencyDto> getEntry(@PathVariable("id") Long id) {
		RecencyDto dto = service.findById(id);

		if (dto == null) {
			return new ResponseEntity<RecencyDto>(new RecencyDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<RecencyDto>(dto, HttpStatus.OK);
	}
	
	@GetMapping(path = "/latest/{caseId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RecencyDto> getLatestEntry(@PathVariable("caseId") Long caseId) {
		RecencyDto dto = service.findLatest(caseId);

		if (dto == null) {
			return new ResponseEntity<RecencyDto>(new RecencyDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<RecencyDto>(dto, HttpStatus.OK);
	}

	@PostMapping(path = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RecencyDto>> getAllEntriess(@RequestBody RecencyFilterDto filter) {

		List<RecencyDto> services = service.findAll(filter);

		return new ResponseEntity<List<RecencyDto>>(services, HttpStatus.OK);
	}

	@PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<RecencyDto>> getAllEntriesPageable(@RequestBody RecencyFilterDto filter) {

		Page<RecencyDto> services = service.findAllPageable(filter);

		return new ResponseEntity<Page<RecencyDto>>(services, HttpStatus.OK);
	}

	@RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RecencyDto> saveEntry(@RequestBody RecencyDto dto) {

		if (dto == null) {
			return new ResponseEntity<RecencyDto>(new RecencyDto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.saveOne(dto);

		return new ResponseEntity<RecencyDto>(dto, HttpStatus.OK);
	}

	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteEntries(@RequestBody RecencyDto[] dtos) {
		service.deleteMultiple(dtos);
	}

}
