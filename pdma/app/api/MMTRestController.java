package org.pepfar.pdma.app.api;

import org.pepfar.pdma.app.data.dto.MMTDto;
import org.pepfar.pdma.app.data.dto.MMTFilterDto;
import org.pepfar.pdma.app.data.service.MMTService;
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
@RequestMapping(path = "/api/v1/mmt")
public class MMTRestController
{

	@Autowired
	private MMTService service;

	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<MMTDto> getTest(@PathVariable("id") Long id) {
		MMTDto dto = service.findById(id);

		if (dto == null) {
			return new ResponseEntity<MMTDto>(new MMTDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<MMTDto>(dto, HttpStatus.OK);
	}

	@PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<MMTDto>> getAllTests(@RequestBody MMTFilterDto filter) {

		Page<MMTDto> services = service.findAllPageable(filter);

		return new ResponseEntity<Page<MMTDto>>(services, HttpStatus.OK);
	}

	@RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<MMTDto> saveTest(@RequestBody MMTDto dto) {

		if (dto == null) {
			return new ResponseEntity<MMTDto>(new MMTDto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.saveOne(dto);

		return new ResponseEntity<MMTDto>(dto, HttpStatus.OK);
	}

	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteTests(@RequestBody MMTDto[] dtos) {
		service.deleteMultiple(dtos);
	}

}
