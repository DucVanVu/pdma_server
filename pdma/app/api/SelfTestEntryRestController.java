package org.pepfar.pdma.app.api;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Workbook;
import org.pepfar.pdma.app.data.dto.OrganizationDto;
import org.pepfar.pdma.app.data.dto.PreventionFilterDto;
import org.pepfar.pdma.app.data.dto.PreventionReportDto;
import org.pepfar.pdma.app.data.dto.SelfTestDetailReportDto;
import org.pepfar.pdma.app.data.dto.SelfTestEntryDto;
import org.pepfar.pdma.app.data.dto.SelfTestFilterDto;
import org.pepfar.pdma.app.data.dto.SelfTestSpecimenDto;
import org.pepfar.pdma.app.data.service.SelfTestEntryService;
import org.pepfar.pdma.app.data.service.SelfTestSpecimenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/self_test")
public class SelfTestEntryRestController {

	@Autowired
	private SelfTestEntryService service;
	
	@Autowired
	private SelfTestSpecimenService specService;

	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SelfTestEntryDto> getSelfTestEntry(@PathVariable("id") Long id) {
		SelfTestEntryDto dto = service.findById(id);

		return new ResponseEntity<>(dto, HttpStatus.OK);
	}

	@GetMapping(path = "/specimen/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SelfTestSpecimenDto> getSpecimenEntry(@PathVariable("id") Long id) {
		SelfTestSpecimenDto dto = service.findSpecimenById(id);

		return new ResponseEntity<>(dto, HttpStatus.OK);
	}

	@PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<SelfTestEntryDto>> getAllSelfTestEntries(@RequestBody SelfTestFilterDto filter) {
		return new ResponseEntity<Page<SelfTestEntryDto>>(service.findAll(filter), HttpStatus.OK);
	}

	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SelfTestEntryDto> saveSelfTestEntry(@RequestBody SelfTestEntryDto dto) {
		if (dto == null) {
			return new ResponseEntity<>(new SelfTestEntryDto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.saveOne(dto);

		return new ResponseEntity<>(dto, HttpStatus.OK);
	}

	@PostMapping(path = "/specimen", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SelfTestEntryDto> saveSelfTestSpecimen(@RequestBody SelfTestSpecimenDto dto) {
		if (dto == null) {
			return new ResponseEntity<>(new SelfTestEntryDto(), HttpStatus.BAD_REQUEST);
		}

		SelfTestEntryDto stDto = service.saveSpecimen2Entry(dto);

		return new ResponseEntity<>(stDto, HttpStatus.OK);
	}

	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteSelfTestEntries(@RequestBody SelfTestEntryDto[] dtos) {
		service.deleteMultiple(dtos);
	}

	@DeleteMapping(path = "/specimen", produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteSpecimens(@RequestBody SelfTestSpecimenDto[] dtos) {
		service.deleteSpecimens(dtos);
	}
	
	@PostMapping(path = "/reportSpecimen", produces = MediaType.APPLICATION_JSON_VALUE)
	public PreventionReportDto<SelfTestDetailReportDto> reportSpecimen(@RequestBody PreventionFilterDto filter) {
		return specService.getReport(filter);
	}

	@PostMapping(path = "/exportReport")
	public void exportReport(@RequestBody PreventionFilterDto filter, HttpServletResponse response) {
		Workbook wbook = service.exportReportSeftTestCase(filter);

		if (wbook == null) {
			throw new RuntimeException();
		}

		String filename = "report-self-test";
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

	@PostMapping(path = "/export")
	public void exportExcel(@RequestBody SelfTestFilterDto filter, HttpServletResponse response) {

		Workbook wbook = service.exportSearchResults(filter);

		if (wbook == null) {
			throw new RuntimeException();
		}

		String filename = "dulieu-tuxetnghiem-" + System.currentTimeMillis() + ".xlsx";
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
	
	@GetMapping(path = "/get_list_self_test_write_able", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<OrganizationDto>> getListHTSWriteAble() {
		List<OrganizationDto> ret = service.getListSelfTestWriteAble();
		return new ResponseEntity<List<OrganizationDto>>(ret, HttpStatus.OK);
	}

//	@PostMapping(path = "/exportReportNew")
//	public void exportReportNew(@RequestBody PreventionFilterDto filter, HttpServletResponse response) {
//		Workbook wbook = service.exportReportSelfTestNew(filter);
//
//		if (wbook == null) {
//			throw new RuntimeException();
//		}
//
//		String filename = "Report";
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
//			wbook.write(response.getOutputStream());
//			response.flushBuffer();
//			response.getOutputStream().close();
//		} catch (Exception ex) {
//			throw new RuntimeException(ex);
//		}
//	}
}
