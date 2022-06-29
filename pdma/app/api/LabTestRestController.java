package org.pepfar.pdma.app.api;

import java.util.List;

import org.pepfar.pdma.app.data.dto.LabTestDto;
import org.pepfar.pdma.app.data.dto.LabTestFilterDto;
import org.pepfar.pdma.app.data.service.LabTestService;
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
@RequestMapping(path = "/api/v1/labtest")
public class LabTestRestController {

	@Autowired
	private LabTestService service;

	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LabTestDto> getTest(@PathVariable("id") Long id) {
		LabTestDto dto = service.findById(id);

		if (dto == null) {
			return new ResponseEntity<LabTestDto>(new LabTestDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<LabTestDto>(dto, HttpStatus.OK);
	}

	@PostMapping(path = "/multiple", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<LabTestDto>> getAllTests(@RequestBody LabTestFilterDto filter) {

		List<LabTestDto> tests = service.findAll(filter);

		return new ResponseEntity<List<LabTestDto>>(tests, HttpStatus.OK);
	}

	@PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<LabTestDto>> getAllTestsPageable(@RequestBody LabTestFilterDto filter) {

		Page<LabTestDto> tests = service.findAllPageable(filter);

		return new ResponseEntity<Page<LabTestDto>>(tests, HttpStatus.OK);
	}

	@PostMapping(path = "/eligibility", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Integer> checkVLEligibility(@RequestBody LabTestFilterDto filter) {
		return new ResponseEntity<Integer>(service.checkVLEligibility(filter), HttpStatus.OK);
	}

	@RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LabTestDto> saveTest(@RequestBody LabTestDto dto) {

		if (dto == null) {
			return new ResponseEntity<LabTestDto>(new LabTestDto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.saveOne(dto);

		return new ResponseEntity<LabTestDto>(dto, HttpStatus.OK);
	}

	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteTests(@RequestBody LabTestDto[] dtos) {
		service.deleteMultiple(dtos);
	}

}
