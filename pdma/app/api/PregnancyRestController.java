package org.pepfar.pdma.app.api;

import org.pepfar.pdma.app.data.dto.PregnancyDto;
import org.pepfar.pdma.app.data.dto.PregnancyFilterDto;
import org.pepfar.pdma.app.data.service.PregnancyService;
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
@RequestMapping(path = "/api/v1/pregnancy")
public class PregnancyRestController {

	@Autowired
	private PregnancyService service;

	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PregnancyDto> getTest(@PathVariable("id") Long id) {
		PregnancyDto dto = service.findById(id);

		if (dto == null) {
			return new ResponseEntity<PregnancyDto>(new PregnancyDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<PregnancyDto>(dto, HttpStatus.OK);
	}

	@PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<PregnancyDto>> getAllTests(@RequestBody PregnancyFilterDto filter) {

		Page<PregnancyDto> services = service.findAllPageable(filter);

		return new ResponseEntity<Page<PregnancyDto>>(services, HttpStatus.OK);
	}

	@RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PregnancyDto> saveTest(@RequestBody PregnancyDto dto) {

		if (dto == null) {
			return new ResponseEntity<PregnancyDto>(new PregnancyDto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.saveOne(dto);

		return new ResponseEntity<PregnancyDto>(dto, HttpStatus.OK);
	}

	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteTests(@RequestBody PregnancyDto[] dtos) {
		service.deleteMultiple(dtos);
	}

}
