package org.pepfar.pdma.app.api;

import java.util.List;

import org.pepfar.pdma.app.data.dto.WRCaseDto;
import org.pepfar.pdma.app.data.dto.WRCaseFilterDto;
import org.pepfar.pdma.app.data.service.WRCaseService;
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
@RequestMapping(path = "/api/v1/wrcase")
public class WRCaseRestController {

	@Autowired
	private WRCaseService service;

	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<WRCaseDto> getWRCase(@PathVariable("id") Long id) {
		WRCaseDto dto = service.findById(id);

		if (dto == null) {
			return new ResponseEntity<>(new WRCaseDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(dto, HttpStatus.OK);
	}

	@PostMapping(path = "/any", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<WRCaseDto>> getAnyWRCases(@RequestBody WRCaseFilterDto filter) {

		List<WRCaseDto> services = service.findAny(filter);

		return new ResponseEntity<>(services, HttpStatus.OK);
	}

	@PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<WRCaseDto>> getAllWRCases(@RequestBody WRCaseFilterDto filter) {

		Page<WRCaseDto> services = service.findAllPageable(filter);

		return new ResponseEntity<>(services, HttpStatus.OK);
	}

	@PostMapping(path = "/tx_cases", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<WRCaseDto>> getAllTreatmentWRCases(@RequestBody WRCaseFilterDto filter) {

		List<WRCaseDto> services = service.findTreatmentCases(filter);

		return new ResponseEntity<>(services, HttpStatus.OK);
	}

	@PostMapping(path = "/link2_opcassist", produces = MediaType.APPLICATION_JSON_VALUE)
	public void markAsLinked2OPCAssist(@RequestBody WRCaseDto dto) {
		service.markAsLinked2OPCAssist(dto);
	}

	@PostMapping(path = "/untreated", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<WRCaseDto>> getAllUntreatedWRCases(@RequestBody WRCaseFilterDto filter) {

		Page<WRCaseDto> services = service.findUntreatedPageable(filter);

		return new ResponseEntity<>(services, HttpStatus.OK);
	}

	@RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<WRCaseDto> saveWRCase(@RequestBody WRCaseDto dto) {

		if (dto == null) {
			return new ResponseEntity<>(new WRCaseDto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.saveOne(dto);

		return new ResponseEntity<>(dto, HttpStatus.OK);
	}

	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteWRCases(@RequestBody WRCaseDto[] dtos) {
		service.deleteMultiple(dtos);
	}

}
