package org.pepfar.pdma.app.api;

import org.pepfar.pdma.app.data.dto.RiskInterviewDto;
import org.pepfar.pdma.app.data.dto.RiskInterviewFilterDto;
import org.pepfar.pdma.app.data.service.RiskInterviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/risk_interview")
public class RiskInterviewRestController {

	@Autowired
	private RiskInterviewService service;

	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RiskInterviewDto> getRiskInterview(@PathVariable("id") Long id) {
		RiskInterviewDto dto = service.findById(id);

		if (dto == null) {
			return new ResponseEntity<RiskInterviewDto>(new RiskInterviewDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<RiskInterviewDto>(dto, HttpStatus.OK);
	}

	@PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<RiskInterviewDto>> getAllRiskInterviews(@RequestBody RiskInterviewFilterDto filter) {

		Page<RiskInterviewDto> services = service.findAllPageable(filter);

		return new ResponseEntity<Page<RiskInterviewDto>>(services, HttpStatus.OK);
	}

	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RiskInterviewDto> saveRiskInterview(@RequestBody RiskInterviewDto dto) {

		if (dto == null) {
			return new ResponseEntity<RiskInterviewDto>(new RiskInterviewDto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.saveOne(dto);

		return new ResponseEntity<RiskInterviewDto>(dto, HttpStatus.OK);
	}

	@PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RiskInterviewDto> updateRisks(@RequestBody RiskInterviewDto dto) {

		if (dto == null) {
			return new ResponseEntity<RiskInterviewDto>(new RiskInterviewDto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.updateRisks(dto);

		return new ResponseEntity<RiskInterviewDto>(dto, HttpStatus.OK);
	}

	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteRiskInterviews(@RequestBody RiskInterviewDto[] dtos) {
		service.deleteMultiple(dtos);
	}

}
