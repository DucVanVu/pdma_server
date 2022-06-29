package org.pepfar.pdma.app.api;

import java.util.List;

import org.pepfar.pdma.app.data.dto.TBTreatment2Dto;
import org.pepfar.pdma.app.data.dto.TBTreatmentFilterDto;
import org.pepfar.pdma.app.data.service.TBTreatment2Service;
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
@RequestMapping(path = "/api/v1/tb_treatment2")
public class TBTreatment2RestController {

	@Autowired
	private TBTreatment2Service service;

	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TBTreatment2Dto> getTreatment(@PathVariable("id") Long id) {
		TBTreatment2Dto dto = service.findById(id);

		if (dto == null) {
			return new ResponseEntity<TBTreatment2Dto>(new TBTreatment2Dto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<TBTreatment2Dto>(dto, HttpStatus.OK);
	}

	@GetMapping(path = "/latest/{caseId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TBTreatment2Dto> getLatestTreatment(@PathVariable("caseId") Long caseId) {
		TBTreatment2Dto dto = service.findLatest(caseId);

		if (dto == null) {
			return new ResponseEntity<TBTreatment2Dto>(new TBTreatment2Dto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<TBTreatment2Dto>(dto, HttpStatus.OK);
	}

	@PostMapping(path = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<TBTreatment2Dto>> getAllTreatments(@RequestBody TBTreatmentFilterDto filter) {

		List<TBTreatment2Dto> services = service.findAll(filter);

		return new ResponseEntity<List<TBTreatment2Dto>>(services, HttpStatus.OK);
	}

	@PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<TBTreatment2Dto>> getAllTreatmentsPageable(@RequestBody TBTreatmentFilterDto filter) {

		Page<TBTreatment2Dto> services = service.findAllPageable(filter);

		return new ResponseEntity<Page<TBTreatment2Dto>>(services, HttpStatus.OK);
	}

	@RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TBTreatment2Dto> saveTreatment(@RequestBody TBTreatment2Dto dto) {

		if (dto == null) {
			return new ResponseEntity<TBTreatment2Dto>(new TBTreatment2Dto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.saveOne(dto);

		return new ResponseEntity<TBTreatment2Dto>(dto, HttpStatus.OK);
	}

	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteTreatments(@RequestBody TBTreatment2Dto[] dtos) {
		service.deleteMultiple(dtos);
	}
	@DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public void delete(@PathVariable("id") Long id) {
		service.deletaOneById(id);		
	}

}
