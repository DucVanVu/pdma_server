package org.pepfar.pdma.app.api;

import java.util.List;

import org.pepfar.pdma.app.data.dto.MMDispensingDto;
import org.pepfar.pdma.app.data.dto.MMDispensingFilterDto;
import org.pepfar.pdma.app.data.dto.MMDispensingHardEligibilityDto;
import org.pepfar.pdma.app.data.service.MMDispensingService;
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
@RequestMapping(path = "/api/v1/mmdispensing")
public class MMDispensingRestController {

	@Autowired
	private MMDispensingService service;

	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<MMDispensingDto> getMMDispensing(@PathVariable("id") Long id) {
		MMDispensingDto dto = service.findById(id);

		if (dto == null) {
			return new ResponseEntity<MMDispensingDto>(new MMDispensingDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<MMDispensingDto>(dto, HttpStatus.OK);
	}

	@PostMapping(path = "/hard_eligible_vals", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<MMDispensingHardEligibilityDto> getHardEligibility(
			@RequestBody MMDispensingFilterDto filter) {
		return new ResponseEntity<>(service.getHardEligibility(filter), HttpStatus.OK);
	}

	@PostMapping(path = "/hard_eligible", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean> checkHardEligibility(@RequestBody MMDispensingFilterDto filter) {
		return new ResponseEntity<Boolean>(service.isHardEligible(filter), HttpStatus.OK);
	}

	@PostMapping(path = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<MMDispensingDto>> getMMDispensings(@RequestBody MMDispensingFilterDto filter) {

		List<MMDispensingDto> services = service.findAll(filter);

		return new ResponseEntity<List<MMDispensingDto>>(services, HttpStatus.OK);
	}

	@PostMapping(path = "/soft_del_restore", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<MMDispensingDto> softDeleteRestore(@RequestBody MMDispensingDto dto) {

		if (dto == null) {
			return new ResponseEntity<MMDispensingDto>(new MMDispensingDto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.softDeleteRestore(dto);

		return new ResponseEntity<MMDispensingDto>(dto, HttpStatus.OK);
	}

	@RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<MMDispensingDto> saveMMDispensing(@RequestBody MMDispensingDto dto) {

		if (dto == null) {
			return new ResponseEntity<MMDispensingDto>(new MMDispensingDto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.saveOne(dto);

		return new ResponseEntity<MMDispensingDto>(dto, HttpStatus.OK);
	}

	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteMMDispensings(@RequestBody MMDispensingDto[] dtos) {
		service.deleteMultiple(dtos);
	}

}
