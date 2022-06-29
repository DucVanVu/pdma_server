package org.pepfar.pdma.app.api;

import java.util.List;

import org.pepfar.pdma.app.data.dto.TBTreatmentDto;
import org.pepfar.pdma.app.data.dto.TBTreatmentFilterDto;
import org.pepfar.pdma.app.data.service.TBTreatmentService;
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
@RequestMapping(path = "/api/v1/tb_treatment")
public class TBTreatmentRestController {

	@Autowired
	private TBTreatmentService service;

	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TBTreatmentDto> getTreatment(@PathVariable("id") Long id) {
		TBTreatmentDto dto = service.findById(id);

		if (dto == null) {
			return new ResponseEntity<TBTreatmentDto>(new TBTreatmentDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<TBTreatmentDto>(dto, HttpStatus.OK);
	}

	@GetMapping(path = "/latest/{caseId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TBTreatmentDto> getLatestTreatment(@PathVariable("caseId") Long caseId) {
		TBTreatmentDto dto = service.findLatest(caseId);

		if (dto == null) {
			return new ResponseEntity<TBTreatmentDto>(new TBTreatmentDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<TBTreatmentDto>(dto, HttpStatus.OK);
	}

	@PostMapping(path = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<TBTreatmentDto>> getAllTreatments(@RequestBody TBTreatmentFilterDto filter) {

		List<TBTreatmentDto> services = service.findAll(filter);

		return new ResponseEntity<List<TBTreatmentDto>>(services, HttpStatus.OK);
	}

	@PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<TBTreatmentDto>> getAllTreatmentsPageable(@RequestBody TBTreatmentFilterDto filter) {

		Page<TBTreatmentDto> services = service.findAllPageable(filter);

		return new ResponseEntity<Page<TBTreatmentDto>>(services, HttpStatus.OK);
	}

	@RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TBTreatmentDto> saveTreatment(@RequestBody TBTreatmentDto dto) {

		if (dto == null) {
			return new ResponseEntity<TBTreatmentDto>(new TBTreatmentDto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.saveOne(dto);

		return new ResponseEntity<TBTreatmentDto>(dto, HttpStatus.OK);
	}

	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteTreatments(@RequestBody TBTreatmentDto[] dtos) {
		service.deleteMultiple(dtos);
	}

}
