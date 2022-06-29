package org.pepfar.pdma.app.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.pepfar.pdma.app.data.dto.*;
import org.pepfar.pdma.app.data.service.HTSCaseService;
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
@RequestMapping(path = "/api/v1/htscase")
public class HTSCaseRestController {
	@Autowired
	private HTSCaseService  htsCaseService;

	@Autowired
	private ApplicationContext context;
	
	@PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<HTSCaseDto>> getAllHTSCases(
			@RequestBody PreventionFilterDto searchDto) {

		Page<HTSCaseDto> services = htsCaseService.findAllPageable(searchDto);

		return new ResponseEntity<Page<HTSCaseDto>>(services, HttpStatus.OK);
	}	

	
	@RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<HTSCaseDto> saveOrUpdateHTSCases(@RequestBody HTSCaseDto dto) {

		if (dto == null) {
			return new ResponseEntity<>(new HTSCaseDto(), HttpStatus.BAD_REQUEST);
		}

		dto = htsCaseService.saveOrUpdate(dto);

		return new ResponseEntity<>(dto, HttpStatus.OK);
	}
	
	@RequestMapping(path = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseDto<HTSCaseDto>> deleteHTSCaseById(@PathVariable("id") Long id) {

		ResponseDto<HTSCaseDto> services = htsCaseService.deleteById(id);

		return new ResponseEntity<>(services,HttpStatus.OK);
	}
	
	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<HTSCaseDto> getHTSCase(@PathVariable("id") Long id) {
		HTSCaseDto dto = htsCaseService.findById(id);

		if (dto == null) {
			return new ResponseEntity<HTSCaseDto>(new HTSCaseDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<HTSCaseDto>(dto, HttpStatus.OK);
	}
	
	@GetMapping(path = "/get_list_hts_write_able", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<OrganizationDto>> getListHTSWriteAble() {
		List<OrganizationDto> ret = htsCaseService.getListHTSWriteAble();
		return new ResponseEntity<List<OrganizationDto>>(ret, HttpStatus.OK);
	}
	
	@PostMapping(path = "/export", produces = MediaType.APPLICATION_JSON_VALUE)
	public void export(@RequestBody PreventionFilterDto searchDto, HttpServletResponse response) {

		SXSSFWorkbook wbook = this.htsCaseService.exportHTSCase(searchDto);

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

	@PostMapping(path = "/exportReportHTS")
	public void exportReportHTS(@RequestBody PreventionFilterDto filter, HttpServletResponse response) {
		Workbook wbook = this.htsCaseService.exportReportHTSCase(filter);

		if (wbook == null) {
			throw new RuntimeException();
		}

		String filename = "report-hts";
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

	@PostMapping(path = "/exportReportSTI")
	public void exportReportSTI(@RequestBody PreventionFilterDto filter, HttpServletResponse response) {
		Workbook wbook = this.htsCaseService.exportReportSTICase(filter);

		if (wbook == null) {
			throw new RuntimeException();
		}

		String filename = "report-sti";
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
		PreventionCheckCodeDto ret = htsCaseService.checkDuplicateCode(dto);
		return new ResponseEntity<PreventionCheckCodeDto>(ret, HttpStatus.OK);
	}
	
	@RequestMapping(path = "/updateC24",method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<HTSCaseDto> updateC24(@RequestBody HTSCaseDto dto) {
		if (dto == null) {
			return new ResponseEntity<>(new HTSCaseDto(), HttpStatus.BAD_REQUEST);
		}
		dto = htsCaseService.updateC24(dto);
		return new ResponseEntity<>(dto, HttpStatus.OK);
	}

	@PostMapping(path = "/importExcel", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ImportResultDto<HTSCaseDto>> importFromExcel(
					@RequestParam("uploadfile") MultipartFile uploadfile) throws IOException {
		InputStream is = null;
		try {
			is = uploadfile.getInputStream();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		ImportResultDto<HTSCaseDto> ret = new ImportResultDto<HTSCaseDto>();
		try {
			ret = htsCaseService.importFromExcel(is);
		} catch (IOException exception) {
			exception.printStackTrace();
		}
		System.out.println("ret result: "+ret);
		return new ResponseEntity<ImportResultDto<HTSCaseDto>>(ret, HttpStatus.OK);
	}

	@PostMapping(path = "/importExcelToUpdateIdentityCard", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ImportResultDto<HTSCaseDto>> importFromExcelToUpdateIdentityCard (
					@RequestParam("uploadfile") MultipartFile uploadfile) throws IOException {
		InputStream is = null;
		try {
			is = uploadfile.getInputStream();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		ImportResultDto<HTSCaseDto> ret = new ImportResultDto<HTSCaseDto>();
		try {
			ret = htsCaseService.importFromExcelToUpdateIdentityCard(is);
		} catch (IOException exception) {
			exception.printStackTrace();
		}
		System.out.println("ret result: "+ret);
		return new ResponseEntity<ImportResultDto<HTSCaseDto>>(ret, HttpStatus.OK);
	}
	
	@PostMapping(path = "/importExcelNew")
	public void importFromExcelNew(
					@RequestParam("uploadfile") MultipartFile uploadfile,HttpServletResponse response) throws IOException {
		InputStream is = null;
		try {
			is = uploadfile.getInputStream();
		} catch (FileNotFoundException e) {
			//e.printStackTrace();
		}
		try {
			Workbook wbook = this.htsCaseService.importFromExcelNew(is);

			if (wbook == null) {
				throw new RuntimeException();
			}
			String filename = "hts_import_result";

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
			
			
		} catch (IOException exception) {
			//exception.printStackTrace();
		}
		
	}
}
