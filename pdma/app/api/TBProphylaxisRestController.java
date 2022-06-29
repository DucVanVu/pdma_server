package org.pepfar.pdma.app.api;

import java.util.List;

import org.pepfar.pdma.app.data.dto.TBProphylaxisDto;
import org.pepfar.pdma.app.data.dto.TBProphylaxisFilterDto;
import org.pepfar.pdma.app.data.service.TBProphylaxisService;
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
@RequestMapping(path = "/api/v1/tb_prophylaxis")
public class TBProphylaxisRestController {

	@Autowired
	private TBProphylaxisService service;

	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TBProphylaxisDto> getRound(@PathVariable("id") Long id) {
		TBProphylaxisDto dto = service.findById(id);

		if (dto == null) {
			return new ResponseEntity<TBProphylaxisDto>(new TBProphylaxisDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<TBProphylaxisDto>(dto, HttpStatus.OK);
	}
	
	@GetMapping(path = "/latest/{caseId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TBProphylaxisDto> getLatestRound(@PathVariable("caseId") Long caseId) {
		TBProphylaxisDto dto = service.findLatest(caseId);

		if (dto == null) {
			return new ResponseEntity<TBProphylaxisDto>(new TBProphylaxisDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<TBProphylaxisDto>(dto, HttpStatus.OK);
	}

	@PostMapping(path = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<TBProphylaxisDto>> getAllRounds(@RequestBody TBProphylaxisFilterDto filter) {

		List<TBProphylaxisDto> services = service.findAll(filter);

		return new ResponseEntity<List<TBProphylaxisDto>>(services, HttpStatus.OK);
	}

	@PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<TBProphylaxisDto>> getAllRoundsPageable(@RequestBody TBProphylaxisFilterDto filter) {

		Page<TBProphylaxisDto> services = service.findAllPageable(filter);

		return new ResponseEntity<Page<TBProphylaxisDto>>(services, HttpStatus.OK);
	}

	@RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TBProphylaxisDto> saveRound(@RequestBody TBProphylaxisDto dto) {

		if (dto == null) {
			return new ResponseEntity<TBProphylaxisDto>(new TBProphylaxisDto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.saveOne(dto);

		return new ResponseEntity<TBProphylaxisDto>(dto, HttpStatus.OK);
	}

	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteRounds(@RequestBody TBProphylaxisDto[] dtos) {
		service.deleteMultiple(dtos);
	}

}
