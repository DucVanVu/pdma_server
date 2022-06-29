package org.pepfar.pdma.app.api;

import java.util.List;

import org.pepfar.pdma.app.data.dto.HIVConfirmLabDto;
import org.pepfar.pdma.app.data.dto.HIVConfirmLabFilterDto;
import org.pepfar.pdma.app.data.service.HIVConfirmLabService;
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
@RequestMapping(path = "/api/v1/confirm_lab")
public class HIVConfirmLabRestController
{

	@Autowired
	private HIVConfirmLabService service;

	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<HIVConfirmLabDto> getConfirmLab(@PathVariable("id") Long id) {
		HIVConfirmLabDto dto = service.findById(id);

		if (dto == null) {
			return new ResponseEntity<>(new HIVConfirmLabDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(dto, HttpStatus.OK);
	}

	@PostMapping(path = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<HIVConfirmLabDto>> getAllConfirmLabs(@RequestBody HIVConfirmLabFilterDto filter) {

		List<HIVConfirmLabDto> services = service.findAll(filter);

		return new ResponseEntity<>(services, HttpStatus.OK);
	}

	@PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<HIVConfirmLabDto>> getAllConfirmLabsPageable(
			@RequestBody HIVConfirmLabFilterDto filter) {

		Page<HIVConfirmLabDto> services = service.findAllPageable(filter);

		return new ResponseEntity<>(services, HttpStatus.OK);
	}

	@RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<HIVConfirmLabDto> saveConfirmLab(@RequestBody HIVConfirmLabDto dto) {

		if (dto == null) {
			return new ResponseEntity<>(new HIVConfirmLabDto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.saveOne(dto);

		return new ResponseEntity<>(dto, HttpStatus.OK);
	}

	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteConfirmLabs(@RequestBody HIVConfirmLabDto[] dtos) {
		service.deleteMultiple(dtos);
	}

}
