package org.pepfar.pdma.app.api;

import java.util.List;

import org.pepfar.pdma.app.data.dto.TBProphylaxis2DispenseDto;
import org.pepfar.pdma.app.data.dto.TBProphylaxis2Dto;
import org.pepfar.pdma.app.data.dto.TBProphylaxisDispenseFilterDto;
import org.pepfar.pdma.app.data.service.TBProphylaxis2DispenseService;
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
@RequestMapping(path = "/api/v1/tb_prophylaxis2dispense")
public class TBProphylaxis2DispenseRestController {

	@Autowired
	private TBProphylaxis2DispenseService service;

	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TBProphylaxis2DispenseDto> getRound(@PathVariable("id") Long id) {
		TBProphylaxis2DispenseDto dto = service.findById(id);

		if (dto == null) {
			return new ResponseEntity<TBProphylaxis2DispenseDto>(new TBProphylaxis2DispenseDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<TBProphylaxis2DispenseDto>(dto, HttpStatus.OK);
	}
	@GetMapping(path = "/latest/{roundId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public TBProphylaxis2DispenseDto getLatestRound(@PathVariable("roundId") Long roundId) {
		TBProphylaxis2DispenseDto dto = service.findLatest(roundId);

		if (dto == null) {
			return null;
		}

		return dto;
	}
	
	@PostMapping(path = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<TBProphylaxis2DispenseDto>> getAllRounds(@RequestBody TBProphylaxisDispenseFilterDto filter) {

		List<TBProphylaxis2DispenseDto> services = service.findAll(filter);

		return new ResponseEntity<List<TBProphylaxis2DispenseDto>>(services, HttpStatus.OK);
	}

	@PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<TBProphylaxis2DispenseDto>> getAllRoundsPageable(@RequestBody TBProphylaxisDispenseFilterDto filter) {

		Page<TBProphylaxis2DispenseDto> services = service.findAllPageable(filter);

		return new ResponseEntity<Page<TBProphylaxis2DispenseDto>>(services, HttpStatus.OK);
	}
	
	@RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TBProphylaxis2DispenseDto> saveRound(@RequestBody TBProphylaxis2DispenseDto dto) {

		if (dto == null) {
			return new ResponseEntity<TBProphylaxis2DispenseDto>(new TBProphylaxis2DispenseDto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.saveOne(dto);

		return new ResponseEntity<TBProphylaxis2DispenseDto>(dto, HttpStatus.OK);
	}

	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteRounds(@RequestBody TBProphylaxis2DispenseDto[] dtos) {
		service.deleteMultiple(dtos);
	}

}
