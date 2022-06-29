package org.pepfar.pdma.app.api;

import java.util.List;

import org.pepfar.pdma.app.data.dto.ServiceDto;
import org.pepfar.pdma.app.data.service.ServiceService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/service")
public class ServiceRestController
{

	@Autowired
	private ServiceService service;

	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceDto> getService(@PathVariable("id") Long id) {
		ServiceDto dto = service.findById(id);

		if (dto == null) {
			return new ResponseEntity<>(new ServiceDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(dto, HttpStatus.OK);
	}

	@RequestMapping(path = "/all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<ServiceDto>> getAllDocumentTypes() {
		return new ResponseEntity<List<ServiceDto>>(service.findAll(), HttpStatus.OK);
	}

	@GetMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<ServiceDto>> getAllServices(
			@RequestParam(name = "page", required = true, defaultValue = "0") int pageIndex,
			@RequestParam(name = "size", required = true, defaultValue = "25") int pageSize) {

		Page<ServiceDto> services = service.findAllPageable(pageIndex, pageSize);

		return new ResponseEntity<>(services, HttpStatus.OK);
	}

	@RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceDto> saveService(@RequestBody ServiceDto dto) {

		if (dto == null) {
			return new ResponseEntity<>(new ServiceDto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.saveOne(dto);

		return new ResponseEntity<>(dto, HttpStatus.OK);
	}

	@PostMapping(path = "/exists")
	public ResponseEntity<Boolean> codeExists(@RequestBody ServiceDto dto) {
		return new ResponseEntity<>(service.codeExists(dto), HttpStatus.OK);
	}

	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteServices(@RequestBody ServiceDto[] dtos) {
		service.deleteMultiple(dtos);
	}

}
