package org.pepfar.pdma.app.api;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Workbook;
import org.pepfar.pdma.app.data.dto.ShiInterviewDto;
import org.pepfar.pdma.app.data.dto.ShiInterviewFilterDto;
import org.pepfar.pdma.app.data.service.ShiInterviewService;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/shi_interview")
public class ShiInterviewRestController {

	@Autowired
	private ShiInterviewService service;

	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ShiInterviewDto> getShiInterview(@PathVariable("id") Long id) {
		ShiInterviewDto dto = service.findById(id);

		if (dto == null) {
			return new ResponseEntity<ShiInterviewDto>(new ShiInterviewDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<ShiInterviewDto>(dto, HttpStatus.OK);
	}

	@GetMapping(path = "/latest/{caseId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ShiInterviewDto> getLatestEntry(@PathVariable("caseId") Long caseId) {
		return new ResponseEntity<>(service.findLatestEntry(caseId), HttpStatus.OK);
	}

	@GetMapping(path = "/instances/{caseId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<ShiInterviewDto.DataCaptureInstance>> getAllInstances(
			@PathVariable("caseId") Long caseId) {
		return new ResponseEntity<>(service.getInstances(caseId), HttpStatus.OK);
	}

	@PostMapping(path = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<ShiInterviewDto>> getAllShiInterviews(@RequestBody ShiInterviewFilterDto filter) {

		List<ShiInterviewDto> services = service.findAll(filter);

		return new ResponseEntity<List<ShiInterviewDto>>(services, HttpStatus.OK);
	}

	@PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<ShiInterviewDto>> getAllShiInterviewsPageable(
			@RequestBody ShiInterviewFilterDto filter) {

		Page<ShiInterviewDto> services = service.findAllPageable(filter);

		return new ResponseEntity<Page<ShiInterviewDto>>(services, HttpStatus.OK);
	}

	@RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ShiInterviewDto> saveShiInterview(@RequestBody ShiInterviewDto dto) {

		if (dto == null) {
			return new ResponseEntity<ShiInterviewDto>(new ShiInterviewDto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.saveOne(dto);

		return new ResponseEntity<ShiInterviewDto>(dto, HttpStatus.OK);
	}

	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteShiInterviews(@RequestBody ShiInterviewDto[] dtos) {
		service.deleteMultiple(dtos);
	}

	@PostMapping(path = "/report")
	public void exportExcel(@RequestBody ShiInterviewFilterDto filter, HttpServletResponse response) {

		Workbook wbook = service.generateReport(filter);

		if (wbook == null) {
			throw new RuntimeException();
		}

		String filename = "baocao-" + System.currentTimeMillis() + ".xlsx";
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
}
