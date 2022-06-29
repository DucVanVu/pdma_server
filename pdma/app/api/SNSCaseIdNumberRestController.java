package org.pepfar.pdma.app.api;

import java.util.List;

import org.pepfar.pdma.app.data.dto.DocumentTypeDto;
import org.pepfar.pdma.app.data.dto.SNSCaseIdNumberDto;
import org.pepfar.pdma.app.data.repository.SNSCaseIdNumberRepository;
import org.pepfar.pdma.app.data.service.DocumentTypeService;
import org.pepfar.pdma.app.data.service.SNSCaseIdNumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/sns_case_id_number")
public class SNSCaseIdNumberRestController
{

	@Autowired
	private SNSCaseIdNumberService service;
	@Autowired
	private SNSCaseIdNumberRepository repository;

	@RequestMapping(path = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SNSCaseIdNumberDto> getSNSCaseIdNumber(@PathVariable("id") Long id) {
		SNSCaseIdNumberDto dto = service.findById(id);

		if (dto == null) {
			return new ResponseEntity<SNSCaseIdNumberDto>(new SNSCaseIdNumberDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<SNSCaseIdNumberDto>(dto, HttpStatus.OK);
	}

	@RequestMapping(path = "/all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<SNSCaseIdNumberDto>> getAllSNSCaseIdNumbers() {
		return new ResponseEntity<List<SNSCaseIdNumberDto>>(service.findAll(), HttpStatus.OK);
	}

	@RequestMapping(path = "/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<SNSCaseIdNumberDto>> getAllSNSCaseIdNumbers(
			@RequestParam(name = "page", required = true, defaultValue = "0") int pageIndex,
			@RequestParam(name = "size", required = true, defaultValue = "20") int pageSize) {

		return new ResponseEntity<Page<SNSCaseIdNumberDto>>(service.findAll(pageIndex, pageSize), HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SNSCaseIdNumberDto> saveSNSCaseIdNumber(@RequestBody SNSCaseIdNumberDto dto) {
		if (dto == null) {
			return new ResponseEntity<SNSCaseIdNumberDto>(new SNSCaseIdNumberDto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.saveOne(dto);

		return new ResponseEntity<SNSCaseIdNumberDto>(dto, HttpStatus.OK);
	}

	@RequestMapping(path = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteSNSCaseIdNumber(@PathVariable Long id) {
		repository.delete(id);
	}
	
	@RequestMapping(path = "/deleteMulti", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteSNSCaseIdNumbers(@RequestBody SNSCaseIdNumberDto[] dtos) {
		service.deleteMultiple(dtos);
	}

}
