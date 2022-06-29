package org.pepfar.pdma.app.api;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.pepfar.pdma.app.data.dto.ObjectDto;
import org.pepfar.pdma.app.data.dto.TBProphylaxis2Dto;
import org.pepfar.pdma.app.data.dto.TBProphylaxisFilterDto;
import org.pepfar.pdma.app.data.service.TBProphylaxis2Service;
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
@RequestMapping(path = "/api/v1/tb_prophylaxis2")
public class TBProphylaxis2RestController {

	@Autowired
	private TBProphylaxis2Service service;

	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TBProphylaxis2Dto> getRound(@PathVariable("id") Long id) {
		TBProphylaxis2Dto dto = service.findById(id);

		if (dto == null) {
			return new ResponseEntity<TBProphylaxis2Dto>(new TBProphylaxis2Dto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<TBProphylaxis2Dto>(dto, HttpStatus.OK);
	}
	
	@GetMapping(path = "/latest/{caseId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TBProphylaxis2Dto> getLatestRound(@PathVariable("caseId") Long caseId) {
		TBProphylaxis2Dto dto = service.findLatest(caseId);

		if (dto == null) {
			return new ResponseEntity<TBProphylaxis2Dto>(new TBProphylaxis2Dto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<TBProphylaxis2Dto>(dto, HttpStatus.OK);
	}

	@PostMapping(path = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<TBProphylaxis2Dto>> getAllRounds(@RequestBody TBProphylaxisFilterDto filter) {

		List<TBProphylaxis2Dto> services = service.findAll(filter);

		return new ResponseEntity<List<TBProphylaxis2Dto>>(services, HttpStatus.OK);
	}

	@PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<TBProphylaxis2Dto>> getAllRoundsPageable(@RequestBody TBProphylaxisFilterDto filter) {

		Page<TBProphylaxis2Dto> services = service.findAllPageable(filter);

		return new ResponseEntity<Page<TBProphylaxis2Dto>>(services, HttpStatus.OK);
	}

	@RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TBProphylaxis2Dto> saveRound(@RequestBody TBProphylaxis2Dto dto) {

		if (dto == null) {
			return new ResponseEntity<TBProphylaxis2Dto>(new TBProphylaxis2Dto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.saveOne(dto);

		return new ResponseEntity<TBProphylaxis2Dto>(dto, HttpStatus.OK);
	}

	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteRounds(@RequestBody TBProphylaxis2Dto[] dtos) {
		service.deleteMultiple(dtos);
	}
	@GetMapping(path = "/checkCompleteOrNotComplete/{tbProphylaxis2Id}/{tbProphylaxis2DispenseId}/{dose}/{recordDate}", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ObjectDto> checkComplete(@PathVariable("tbProphylaxis2Id") Long tbProphylaxis2Id,@PathVariable("tbProphylaxis2DispenseId") Long tbProphylaxis2DispenseId,@PathVariable("dose") Integer dose,@PathVariable("recordDate") LocalDateTime recordDate) {
		List<ObjectDto> ret=new ArrayList<ObjectDto>();
		ObjectDto dto=service.checkComplete(tbProphylaxis2Id, tbProphylaxis2DispenseId, dose, recordDate, false);
		if(dto!=null) {
			ret.add(dto);
		}
//		dto=service.checkNotComplete(tbProphylaxis2Id, tbProphylaxis2DispenseId, dose, recordDate, false);
//		if(dto!=null) {
//			ret.add(dto);
//		}
		return ret;
	}
	
	@GetMapping(path = "/checkAgeByRegimen/{caseId}/{regimen}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ObjectDto checkAgeByRegimen(@PathVariable("caseId") Long caseId,@PathVariable("regimen") String regimen) {
		
		ObjectDto dto=service.checkAgeByRegimen(caseId, regimen);
		
		return dto;
	}
	
	@GetMapping(path = "/setStatus/{tbProphylaxis2Id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public TBProphylaxis2Dto setStatus(@PathVariable("tbProphylaxis2Id") Long tbProphylaxis2Id) {
		
		TBProphylaxis2Dto dto=service.setStatus(tbProphylaxis2Id);
		 
		return dto;
	}

}
