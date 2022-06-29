package org.pepfar.pdma.app.api;

import org.pepfar.pdma.app.data.dto.TreatmentDto;
import org.pepfar.pdma.app.data.dto.TreatmentFilterDto;
import org.pepfar.pdma.app.data.service.TreatmentService;
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
@RequestMapping(path = "/api/v1/treatment")
public class TreatmentRestController {

	@Autowired
	private TreatmentService service;

	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TreatmentDto> getTreatment(@PathVariable("id") Long id) {
		TreatmentDto dto = service.findById(id);

		if (dto == null) {
			return new ResponseEntity<TreatmentDto>(new TreatmentDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<TreatmentDto>(dto, HttpStatus.OK);
	}

	@PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<TreatmentDto>> getAllTreatments(@RequestBody TreatmentFilterDto filter) {

		Page<TreatmentDto> services = service.findAllPageable(filter);

		return new ResponseEntity<Page<TreatmentDto>>(services, HttpStatus.OK);
	}

	@PostMapping(path = "/missing-end-date", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean> hasMultipleMissingEndDates(@RequestBody TreatmentFilterDto filter) {
		return new ResponseEntity<>(service.hasMultipleMissingEndDate(filter), HttpStatus.OK);
	}

	@RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TreatmentDto> saveTreatment(@RequestBody TreatmentDto dto) {

		if (dto == null) {
			return new ResponseEntity<TreatmentDto>(new TreatmentDto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.saveOne(dto);

		return new ResponseEntity<TreatmentDto>(dto, HttpStatus.OK);
	}

	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteTreatment(@RequestBody TreatmentDto dto) {
		service.deleteOne(dto);
	}

	@DeleteMapping(path = "/multiple", produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteTreatments(@RequestBody TreatmentDto[] dtos) {
		service.deleteMultiple(dtos);
	}

}
