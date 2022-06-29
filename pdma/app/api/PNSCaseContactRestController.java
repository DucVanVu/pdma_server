package org.pepfar.pdma.app.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Workbook;
import org.pepfar.pdma.app.data.dto.*;
import org.pepfar.pdma.app.data.service.PNSCaseContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path = "/api/v1/pnscase_contact")
public class PNSCaseContactRestController {
	@Autowired
	private PNSCaseContactService  pnsCaseContactService;

	@Autowired
	private ApplicationContext context;

	@PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<PNSCaseContactDto>> getAllPNSCases(
			@RequestBody PreventionFilterDto searchDto) {

		Page<PNSCaseContactDto> services = pnsCaseContactService.findAllPageable(searchDto);

		return new ResponseEntity<Page<PNSCaseContactDto>>(services, HttpStatus.OK);
	}	

	
	@RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PNSCaseContactDto> saveOrUpdatePNSCases(@RequestBody PNSCaseContactDto dto) {
		if (dto == null) {
			return new ResponseEntity<>(new PNSCaseContactDto(), HttpStatus.BAD_REQUEST);
		}
		dto = pnsCaseContactService.saveOrUpdate(dto);
		return new ResponseEntity<>(dto, HttpStatus.OK);
	}
	
	@RequestMapping(path = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseDto<PNSCaseContactDto>> deletePNSCaseById(@PathVariable("id") Long id) {
		ResponseDto<PNSCaseContactDto> services = pnsCaseContactService.deleteById(id);
		return new ResponseEntity<>(services,HttpStatus.OK);
	}
	
	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PNSCaseContactDto> getPNSCaseContact(@PathVariable("id") Long id) {
		PNSCaseContactDto dto = pnsCaseContactService.findById(id);
		if (dto == null) {
			return new ResponseEntity<PNSCaseContactDto>(new PNSCaseContactDto(), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<PNSCaseContactDto>(dto, HttpStatus.OK);
	}
	
	@PostMapping(path = "/export", produces = MediaType.APPLICATION_JSON_VALUE)
	public void export(@RequestBody PreventionFilterDto searchDto, HttpServletResponse response) {
		Workbook wbook = this.pnsCaseContactService.exportPNSCase(searchDto);
		if (wbook == null) {
			throw new RuntimeException();
		}

		String filename = "hts-list";
		if (filename == null) {
			throw new RuntimeException();
		}

		CacheControl cc = CacheControl.maxAge(360, TimeUnit.DAYS).cachePublic();

		response.addHeader("Access-Control-Expose-Headers", "x-filename");
		response.addHeader("Content-disposition", "inline; filename=" + filename);
		response.addHeader("x-filename", filename);
		response.setHeader("Cache-Control", cc.getHeaderValue());
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

		try {
			wbook.write(response.getOutputStream());
			response.flushBuffer();
			response.getOutputStream().close();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}	

//	@PostMapping(path = "/checkCode", produces = MediaType.APPLICATION_JSON_VALUE)
//	public ResponseEntity<CheckCodeDto> checkDuplicateCode(@RequestBody CheckCodeDto dto) {
//		CheckCodeDto ret = pnsCaseContactService.checkDuplicateCode(dto);
//		return new ResponseEntity<CheckCodeDto>(ret, HttpStatus.OK);
//	}

	@PostMapping(path = "/importExcel", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ImportResultDto<PNSCaseContactDto>> importFromExcel(
					@RequestParam("uploadfile") MultipartFile uploadfile) throws IOException {
		InputStream is = null;
		try {
			is = uploadfile.getInputStream();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		ImportResultDto<PNSCaseContactDto> ret = new ImportResultDto<PNSCaseContactDto>();
		try {
			ret = pnsCaseContactService.importFromExcel(is);
		} catch (IOException exception) {
			exception.printStackTrace();
		}
		System.out.println("ret result: "+ret);
		return new ResponseEntity<ImportResultDto<PNSCaseContactDto>>(ret, HttpStatus.OK);
	}
}
