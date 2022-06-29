package org.pepfar.pdma.app.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Workbook;
import org.pepfar.pdma.app.data.dto.*;
import org.pepfar.pdma.app.data.service.HTSCaseService;
import org.pepfar.pdma.app.data.service.PNSCaseService;
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
@RequestMapping(path = "/api/v1/pnscase")
public class PNSCaseRestController {
	@Autowired
	private PNSCaseService pnsCaseService;

	@Autowired
	private ApplicationContext context;
	
	@PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<PNSCaseDto>> getAllPNSCases(
			@RequestBody PreventionFilterDto searchDto) {

		Page<PNSCaseDto> services = pnsCaseService.findAllPageable(searchDto);

		return new ResponseEntity<Page<PNSCaseDto>>(services, HttpStatus.OK);
	}	

	
	@RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PNSCaseDto> saveOrUpdatePNSCases(@RequestBody PNSCaseDto dto) {

		if (dto == null) {
			return new ResponseEntity<>(new PNSCaseDto(), HttpStatus.BAD_REQUEST);
		}

		dto = pnsCaseService.saveOrUpdate(dto);

		return new ResponseEntity<>(dto, HttpStatus.OK);
	}
	
	@RequestMapping(path = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseDto<PNSCaseDto>> deletePNSCaseById(@PathVariable("id") Long id) {
		ResponseDto<PNSCaseDto> services = pnsCaseService.deleteById(id);
		return new ResponseEntity<>(services,HttpStatus.OK);
	}
	
	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PNSCaseDto> getPNSCase(@PathVariable("id") Long id) {
		PNSCaseDto dto = pnsCaseService.findById(id);

		if (dto == null) {
			return new ResponseEntity<PNSCaseDto>(new PNSCaseDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<PNSCaseDto>(dto, HttpStatus.OK);
	}
	
	@PostMapping(path = "/export", produces = MediaType.APPLICATION_JSON_VALUE)
	public void export(@RequestBody PreventionFilterDto searchDto, HttpServletResponse response) {

		Workbook wbook = this.pnsCaseService.exportPNSCase(searchDto);

		if (wbook == null) {
			throw new RuntimeException();
		}

		String filename = "pns-list";
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

	@PostMapping(path = "/exportReport")
	public void exportReport(@RequestBody PreventionFilterDto filter, HttpServletResponse response) {
		Workbook wbook = this.pnsCaseService.exportReportPNSCase(filter);

		if (wbook == null) {
			throw new RuntimeException();
		}

		String filename = "report-pns";
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

	@PostMapping(path = "/checkCode", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PreventionCheckCodeDto> checkDuplicateCode(@RequestBody PreventionCheckCodeDto dto) {
		PreventionCheckCodeDto ret = pnsCaseService.checkDuplicateCode(dto);
		return new ResponseEntity<PreventionCheckCodeDto>(ret, HttpStatus.OK);
	}
	
	@PostMapping(path = "/checkHTSOrOpc", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseDto<PNSCaseDto>> checkHTSOrOPC(@RequestBody PreventionCheckCodeDto dto) {
		ResponseDto<PNSCaseDto> ret = pnsCaseService.checkHTS(dto);
		return new ResponseEntity<>(ret, HttpStatus.OK);
	}

	@PostMapping(path = "/importExcel", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ImportResultDto<PNSCaseDto>> importFromExcel(
					@RequestParam("uploadfile") MultipartFile uploadfile) throws IOException {
		InputStream is = null;
		try {
			is = uploadfile.getInputStream();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		ImportResultDto<PNSCaseDto> ret = new ImportResultDto<PNSCaseDto>();
		try {
			ret = pnsCaseService.importFromExcel(is);
		} catch (IOException exception) {
			exception.printStackTrace();
		}
		System.out.println("ret result: "+ret);
		return new ResponseEntity<ImportResultDto<PNSCaseDto>>(ret, HttpStatus.OK);
	}

	@GetMapping(path = "/get_list_pns_write_able", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<OrganizationDto>> getListPNSWriteAble() {
		List<OrganizationDto> ret = pnsCaseService.getListPNSWriteAble();
		return new ResponseEntity<List<OrganizationDto>>(ret, HttpStatus.OK);
	}
}
