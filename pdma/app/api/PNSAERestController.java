package org.pepfar.pdma.app.api;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Workbook;
import org.pepfar.pdma.app.data.dto.PNSAEDto;
import org.pepfar.pdma.app.data.dto.PNSAEFilterDto;
import org.pepfar.pdma.app.data.service.PNSAEService;
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
@RequestMapping(path = "/api/v1/pns_ae")
public class PNSAERestController {

	@Autowired
	private PNSAEService service;

	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PNSAEDto> getEntry(@PathVariable("id") Long id) {
		PNSAEDto dto = service.findById(id);

		if (dto == null) {
			return new ResponseEntity<PNSAEDto>(new PNSAEDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<PNSAEDto>(dto, HttpStatus.OK);
	}

	@PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<PNSAEDto>> getAllEntriesPageable(@RequestBody PNSAEFilterDto filter) {

		Page<PNSAEDto> tests = service.findAllPageable(filter);

		return new ResponseEntity<Page<PNSAEDto>>(tests, HttpStatus.OK);
	}

	@RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PNSAEDto> saveEntry(@RequestBody PNSAEDto dto) {

		if (dto == null) {
			return new ResponseEntity<PNSAEDto>(new PNSAEDto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.saveOne(dto);

		return new ResponseEntity<PNSAEDto>(dto, HttpStatus.OK);
	}

	@RequestMapping(path = "/submit", method = { RequestMethod.POST }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PNSAEDto> submitEntry(@RequestBody PNSAEDto dto) {

		if (dto == null) {
			return new ResponseEntity<PNSAEDto>(new PNSAEDto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.submit(dto);

		return new ResponseEntity<PNSAEDto>(dto, HttpStatus.OK);
	}

	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteEntries(@RequestBody PNSAEDto[] dtos) {
		service.deleteMultiple(dtos);
	}

	@PostMapping(path = "/report")
	public void exportReport(@RequestBody PNSAEFilterDto filter, HttpServletResponse response) {

		Workbook wbook = service.exportData(filter);

		if (wbook == null) {
			throw new RuntimeException();
		}

		String filename = "baocao-tinhhuong-";
		filename += filter.getFromDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
		filename += "-";
		filename += filter.getToDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
		filename += ".xlsx";

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
