package org.pepfar.pdma.app.api;

import java.util.List;

import org.pepfar.pdma.app.data.dto.ClinicalStageDto;
import org.pepfar.pdma.app.data.dto.ClinicalStageFilterDto;
import org.pepfar.pdma.app.data.service.ClinicalStageService;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping(path = "/api/v1/cstage_history")
public class ClinicalStageRestController {

	@Autowired
	private ClinicalStageService service;

	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ClinicalStageDto> getStage(@PathVariable("id") Long id) {
		ClinicalStageDto dto = service.findById(id);

		if (dto == null) {
			return new ResponseEntity<ClinicalStageDto>(new ClinicalStageDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<ClinicalStageDto>(dto, HttpStatus.OK);
	}

	@PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<ClinicalStageDto>> getAllStages(@RequestBody ClinicalStageFilterDto filter) {

		List<ClinicalStageDto> services = service.findAll(filter);

		return new ResponseEntity<List<ClinicalStageDto>>(services, HttpStatus.OK);
	}

	@RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ClinicalStageDto> saveStage(@RequestBody ClinicalStageDto dto) {

		if (dto == null) {
			return new ResponseEntity<ClinicalStageDto>(new ClinicalStageDto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.saveOne(dto);

		return new ResponseEntity<ClinicalStageDto>(dto, HttpStatus.OK);
	}

	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteStages(@RequestBody ClinicalStageDto[] dtos) {
		service.deleteMultiple(dtos);
	}

}
