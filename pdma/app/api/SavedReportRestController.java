package org.pepfar.pdma.app.api;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.util.WorkbookUtil;
import org.pepfar.pdma.app.data.dto.SavedReportDto;
import org.pepfar.pdma.app.data.dto.SavedReportFilterDto;
import org.pepfar.pdma.app.data.service.SavedReportService;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/saved_report")
public class SavedReportRestController
{

	@Autowired
	private SavedReportService service;

	@RequestMapping(path = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SavedReportDto> getSavedReport(@PathVariable("id") Long id) {
		SavedReportDto dto = service.findById(id, false);

		if (dto == null) {
			return new ResponseEntity<SavedReportDto>(new SavedReportDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<SavedReportDto>(dto, HttpStatus.OK);
	}

	@PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<SavedReportDto>> getAllSavedReports(@RequestBody SavedReportFilterDto filter) {
		return new ResponseEntity<Page<SavedReportDto>>(service.findAll(filter), HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SavedReportDto> saveSavedReport(@RequestBody SavedReportDto dto) {
		if (dto == null) {
			return new ResponseEntity<SavedReportDto>(new SavedReportDto(), HttpStatus.BAD_REQUEST);
		}
		
		dto = service.saveOne(dto);

		return new ResponseEntity<SavedReportDto>(dto, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteSavedReports(@RequestBody SavedReportDto[] dtos) {
		service.deleteMultiple(dtos);
	}

	@RequestMapping(value = "/download/{id}", method = RequestMethod.GET)
	public void downloadSavedReport(@PathVariable("id") Long reportId, HttpServletResponse response) {

		if (!CommonUtils.isPositive(reportId, true)) {
			throw new RuntimeException();
		}

		SavedReportDto document = service.findById(reportId, true);

		if (document == null) {
			throw new RuntimeException();
		}

		String filename = document.getTitle();
		String extension = "." + document.getExtension();

		if (!CommonUtils.isEmpty(filename) && !filename.toLowerCase().endsWith(extension.toLowerCase())) {
			filename = WorkbookUtil.createSafeSheetName(filename, '_');
			filename = filename + extension;
		}

		CacheControl cc = CacheControl.maxAge(360, TimeUnit.DAYS).cachePublic();

		response.addHeader("Access-Control-Expose-Headers", "x-filename");
		response.addHeader("Content-disposition", "inline; filename=" + filename);
		response.addHeader("x-filename", filename);
		response.setHeader("Cache-Control", cc.getHeaderValue());
		response.setContentType(document.getContentType());

		try {
			response.getOutputStream().write(document.getContent());
			response.flushBuffer();
			response.getOutputStream().close();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}

	}
}
