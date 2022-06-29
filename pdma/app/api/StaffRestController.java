package org.pepfar.pdma.app.api;

import org.pepfar.pdma.app.data.dto.HTSCaseDto;
import org.pepfar.pdma.app.data.dto.ImportResultDto;
import org.pepfar.pdma.app.data.dto.StaffDto;
import org.pepfar.pdma.app.data.dto.StaffFilterDto;
import org.pepfar.pdma.app.data.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/staff")
public class StaffRestController {

	@Autowired
	private StaffService service;

	@Autowired
	private ApplicationContext context;

	@PreAuthorize("isAuthenticated()")
	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<StaffDto> getStaffMember(@PathVariable("id") Long id) {
		StaffDto dto = service.findById(id);

		if (dto == null) {
			return new ResponseEntity<StaffDto>(new StaffDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<StaffDto>(dto, HttpStatus.OK);
	}
	
	@PreAuthorize("isAuthenticated()")
	@PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<StaffDto>> getAllStaffMembers(@RequestBody StaffFilterDto filter) {

		Page<StaffDto> services = service.findAllPageable(filter);

		return new ResponseEntity<Page<StaffDto>>(services, HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_PROVINCIAL_MANAGER')")
	@RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<StaffDto> saveStaffMember(@RequestBody StaffDto dto) {

		if (dto == null) {
			return new ResponseEntity<StaffDto>(new StaffDto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.saveOne(dto);

		return new ResponseEntity<StaffDto>(dto, HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_PROVINCIAL_MANAGER')")
	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteStaffMembers(@RequestBody StaffDto[] dtos) {
		service.deleteMultiple(dtos);
	}

	@PostMapping(path = "/importExcel", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ImportResultDto<StaffDto>> importFromExcel(
					@RequestParam("uploadfile") MultipartFile uploadfile) throws IOException {
		InputStream is = null;
		try {
			is = uploadfile.getInputStream();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		ImportResultDto<StaffDto> ret = new ImportResultDto<StaffDto>();
		try {
			ret = service.importFromExcel(is);
		} catch (IOException exception) {
			exception.printStackTrace();
		}
		System.out.println("ret result: "+ret);
		return new ResponseEntity<ImportResultDto<StaffDto>>(ret, HttpStatus.OK);
	}
}
