package org.pepfar.pdma.app.api;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pepfar.pdma.app.Constants;
import org.pepfar.pdma.app.data.dto.*;
import org.pepfar.pdma.app.data.service.PECaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(path = "/api/v1/pecase")
public class PECaseRestController {

	@Autowired
	private PECaseService  peCaseService;

	@Autowired
	private ApplicationContext context;
	
	@PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<PECaseDto>> getAllPECases(
			@RequestBody PreventionFilterDto searchDto) {

		Page<PECaseDto> services = peCaseService.findAllPageable(searchDto);

		return new ResponseEntity<Page<PECaseDto>>(services, HttpStatus.OK);
	}

	@PostMapping(path = "/export", produces = MediaType.APPLICATION_JSON_VALUE)
	public void export(@RequestBody PreventionFilterDto searchDto, HttpServletResponse response) {
		SXSSFWorkbook wbook = this.peCaseService.exportPECase(searchDto);

		if (wbook == null) {
			throw new RuntimeException();
		}

		String filename = "pe-list";
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





//		SXSSFWorkbook wbook = new SXSSFWorkbook((XSSFWorkbook) this.peCaseService.exportPECase(searchDto)) ;
//
//		if (wbook == null) {
//			throw new RuntimeException();
//		}
//
//		String filename = "pe-list";
//		if (filename == null) {
//			throw new RuntimeException();
//		}
//
//		CacheControl cc = CacheControl.maxAge(360, TimeUnit.DAYS).cachePublic();
//
//		response.addHeader("Access-Control-Expose-Headers", "x-filename");
//		response.addHeader("Content-disposition", "inline; filename=" + filename);
//		response.addHeader("x-filename", filename);
//		response.setHeader("Cache-Control", cc.getHeaderValue());
//		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//
//		try {
//			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//			wbook.write(outputStream);
//			ByteArrayInputStream in = new ByteArrayInputStream(outputStream.toByteArray());
//			IOUtils.copy(in, response.getOutputStream());
//			response.flushBuffer();
//			response.getOutputStream().close();
////			wbook.write(response.getOutputStream());
////			response.flushBuffer();
////			response.getOutputStream().close();
//		} catch (Exception ex) {
//			throw new RuntimeException(ex);
//		}
	}

	@Secured({Constants.ROLE_SITE_MANAGER,Constants.ROLE_PROVINCIAL_MANAGER,Constants.ROLE_NATIONAL_MANAGER})
	@RequestMapping(value="/exportReport", method = RequestMethod.POST)
	public void exportReport(@RequestBody PreventionFilterDto filter, HttpServletResponse response) {
		Workbook wbook = this.peCaseService.exportReportPECase(filter);

		if (wbook == null) {
			throw new RuntimeException();
		}

		String filename = "report-pe";
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

	@RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PECaseDto> saveOrUpdatePECases(@RequestBody PECaseDto dto) {

		if (dto == null) {
			return new ResponseEntity<>(new PECaseDto(), HttpStatus.BAD_REQUEST);
		}

		dto = peCaseService.saveOrUpdate(dto);

		return new ResponseEntity<>(dto, HttpStatus.OK);
	}
	
	@RequestMapping(path = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseDto<PECaseDto>> deletePECaseById(@PathVariable("id") Long id) {

		ResponseDto<PECaseDto> services = peCaseService.deleteById(id);

		return new ResponseEntity<>(services,HttpStatus.OK);
	}
	
	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PECaseDto> getPECase(@PathVariable("id") Long id) {
		PECaseDto dto = peCaseService.findById(id);

		if (dto == null) {
			return new ResponseEntity<PECaseDto>(new PECaseDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<PECaseDto>(dto, HttpStatus.OK);
	}

	@PostMapping(path = "/importExcel", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ImportResultDto<PECaseDto>> importFromExcel(
					@RequestParam("uploadfile") MultipartFile uploadfile) throws IOException {
		InputStream is = null;
		try {
			is = uploadfile.getInputStream();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		ImportResultDto<PECaseDto> ret = new ImportResultDto<PECaseDto>();
		try {
			ret = peCaseService.importFromExcel(is);
		} catch (IOException exception) {
			exception.printStackTrace();
		}
		System.out.println("ret result: "+ret);
		return new ResponseEntity<ImportResultDto<PECaseDto>>(ret, HttpStatus.OK);
	}

	@GetMapping(path = "/get_list_pe_write_able", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<OrganizationDto>> getListPEWriteAble() {
		List<OrganizationDto> ret = peCaseService.getListPEWriteAble();
		return new ResponseEntity<List<OrganizationDto>>(ret, HttpStatus.OK);
	}
}
