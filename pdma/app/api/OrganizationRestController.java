package org.pepfar.pdma.app.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.pepfar.pdma.app.data.dto.ImportResultDto;
import org.pepfar.pdma.app.data.dto.OrganizationDto;
import org.pepfar.pdma.app.data.dto.OrganizationFilterDto;
import org.pepfar.pdma.app.data.dto.ServiceOrganizationDto;
import org.pepfar.pdma.app.data.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path = "/api/v1/organization")
public class OrganizationRestController {

	@Autowired
	private OrganizationService service;

	@Autowired
	private ApplicationContext context;

	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<OrganizationDto> getOrganization(@PathVariable("id") Long id) {
		OrganizationDto dto = service.findById(id);

		if (dto == null) {
			return new ResponseEntity<>(null, HttpStatus.OK);
		}

		return new ResponseEntity<>(dto, HttpStatus.OK);
	}

	@GetMapping(path = "/code/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<OrganizationDto> getOrganizationByCode(@PathVariable("code") String code) {
		OrganizationDto dto = service.findByCode(code);

		if (dto == null) {
			return new ResponseEntity<>(null, HttpStatus.OK);
		}

		return new ResponseEntity<>(dto, HttpStatus.OK);
	}

	@PostMapping(path = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<OrganizationDto>> getAllDictionaryEntries(@RequestBody OrganizationFilterDto filter) {
		return new ResponseEntity<List<OrganizationDto>>(service.findAll(filter), HttpStatus.OK);
	}

	@PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<OrganizationDto>> getAllOrganizations(@RequestBody OrganizationFilterDto filter) {

		Page<OrganizationDto> organizations = service.findAllPageable(filter);

		return new ResponseEntity<Page<OrganizationDto>>(organizations, HttpStatus.OK);
	}

	@RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<OrganizationDto> saveOrganization(@RequestBody OrganizationDto dto) {

		if (dto == null) {
			return new ResponseEntity<>(null, HttpStatus.OK);
		}

		dto = service.saveOne(dto);

		return new ResponseEntity<OrganizationDto>(dto, HttpStatus.OK);
	}

	@PutMapping(path = "/attach_service", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<OrganizationDto> attachServices(@RequestBody ServiceOrganizationDto[] dtos) {

		if (dtos == null) {
			return new ResponseEntity<OrganizationDto>(new OrganizationDto(), HttpStatus.BAD_REQUEST);
		}

		OrganizationDto org = null;

		for (ServiceOrganizationDto so : dtos) {
			org = service.attachService(so);
		}

		return new ResponseEntity<OrganizationDto>(org, HttpStatus.OK);
	}

	@DeleteMapping(path = "/detach_service", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<OrganizationDto> detachServices(@RequestBody ServiceOrganizationDto[] dtos) {

		if (dtos == null) {
			return new ResponseEntity<OrganizationDto>(new OrganizationDto(), HttpStatus.BAD_REQUEST);
		}

		OrganizationDto org = null;

		for (ServiceOrganizationDto so : dtos) {
			org = service.detachService(so);
		}

		return new ResponseEntity<OrganizationDto>(org, HttpStatus.OK);
	}

	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteOrganizations(@RequestBody OrganizationDto[] dtos) {
		service.deleteMultiple(dtos);
	}

	@PostMapping(path = "/importExcel", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ImportResultDto<OrganizationDto>> importFromExcel(
					@RequestParam("uploadfile") MultipartFile uploadfile) throws IOException {
		InputStream is = null;
		try {
			is = uploadfile.getInputStream();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		ImportResultDto<OrganizationDto> ret = new ImportResultDto<OrganizationDto>();
		try {
			ret = service.importFromExcel(is);
		} catch (IOException exception) {
			exception.printStackTrace();
		}
		System.out.println("ret result: "+ret);
		return new ResponseEntity<ImportResultDto<OrganizationDto>>(ret, HttpStatus.OK);
	}

	@PostMapping(path = "/newImportExcel", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<OrganizationDto>> newImportFromExcel() throws IOException {
		File initialFile = new File("D:\\cty\\pdma\\nhap\\OrganizationUnits.xlsx");
		InputStream is = new FileInputStream(initialFile);
		List<OrganizationDto> ret = service.newImportFromExcel(is);
		System.out.println("ret result: "+ret.size());
		return new ResponseEntity<List<OrganizationDto>>(ret, HttpStatus.OK);
	}
	
}
