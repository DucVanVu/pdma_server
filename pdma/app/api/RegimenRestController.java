package org.pepfar.pdma.app.api;

import java.util.List;

import org.pepfar.pdma.app.data.dto.RegimenDto;
import org.pepfar.pdma.app.data.dto.RegimenFilterDto;
import org.pepfar.pdma.app.data.service.RegimenService;
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
@RequestMapping(path = "/api/v1/regimen")
public class RegimenRestController
{

	@Autowired
	private RegimenService service;

	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RegimenDto> getRegimen(@PathVariable("id") Long id) {
		RegimenDto dto = service.findById(id);

		if (dto == null) {
			return new ResponseEntity<RegimenDto>(new RegimenDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<RegimenDto>(dto, HttpStatus.OK);
	}

	@PostMapping(path = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RegimenDto>> getAllRegimens(@RequestBody RegimenFilterDto filter) {
		List<RegimenDto> services = service.findAll(filter);

		return new ResponseEntity<>(services, HttpStatus.OK);
	}

	@PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<RegimenDto>> getRegimensPageable(@RequestBody RegimenFilterDto filter) {
		Page<RegimenDto> services = service.findAllPageable(filter);

		return new ResponseEntity<Page<RegimenDto>>(services, HttpStatus.OK);
	}

	@RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RegimenDto> saveRegimen(@RequestBody RegimenDto dto) {

		if (dto == null) {
			return new ResponseEntity<RegimenDto>(new RegimenDto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.saveOne(dto);

		return new ResponseEntity<RegimenDto>(dto, HttpStatus.OK);
	}

	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteRegimens(@RequestBody RegimenDto[] dtos) {
		service.deleteMultiple(dtos);
	}

}
