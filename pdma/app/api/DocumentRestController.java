package org.pepfar.pdma.app.api;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.util.WorkbookUtil;
import org.pepfar.pdma.app.data.dto.DocumentDto;
import org.pepfar.pdma.app.data.dto.DocumentFilterDto;
import org.pepfar.pdma.app.data.dto.DocumentTypeDto;
import org.pepfar.pdma.app.data.service.DocumentService;
import org.pepfar.pdma.app.data.service.DocumentTypeService;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.FileUtils;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path = "/api/v1/document")
public class DocumentRestController
{

	@Autowired
	private DocumentService service;

	@Autowired
	private DocumentTypeService docTypeService;

	@RequestMapping(path = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DocumentDto> getDocument(@PathVariable("id") Long id) {
		DocumentDto dto = service.findById(id, false);

		if (dto == null) {
			return new ResponseEntity<DocumentDto>(new DocumentDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<DocumentDto>(dto, HttpStatus.OK);
	}

	@PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<DocumentDto>> getAllDocuments(@RequestBody DocumentFilterDto filter) {
		return new ResponseEntity<Page<DocumentDto>>(service.findAll(filter), HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DocumentDto> saveDocument(@RequestBody DocumentDto dto) {
		if (dto == null) {
			return new ResponseEntity<DocumentDto>(new DocumentDto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.saveOne(dto);

		return new ResponseEntity<DocumentDto>(dto, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteDocuments(@RequestBody DocumentDto[] dtos) {
		service.deleteMultiple(dtos);
	}

	@RequestMapping(path = "/upload/{docTypeId}", method = RequestMethod.POST)
	public ResponseEntity<DocumentDto> uploadDocument(@PathVariable("docTypeId") Long docTypeId,
			@RequestParam("file") MultipartFile file) {

		DocumentDto document = null;
		DocumentTypeDto docType = null;

		if (CommonUtils.isPositive(docTypeId, true)) {
			docType = docTypeService.findById(docTypeId);
		}

		if (docType == null) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}

		try {
			if (!file.isEmpty()) {
				document = new DocumentDto();
				byte[] data = file.getBytes();

				document.setContent(data);
				document.setContentLength((long) data.length);
				document.setContentType(file.getContentType());

				document.setDocType(docType);

				String fileName = file.getOriginalFilename();
				document.setExtension(FileUtils.getFileExtension(fileName));
				document.setTitle(fileName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (document != null) {
			document = service.saveOne(document);
		}

		return new ResponseEntity<>(document, HttpStatus.OK);
	}

	@RequestMapping(value = "/download/{documentId}", method = RequestMethod.GET)
	public void downloadDocument(@PathVariable("documentId") Long documentId, HttpServletResponse response) {

		if (!CommonUtils.isPositive(documentId, true)) {
			throw new RuntimeException();
		}

		DocumentDto document = service.findById(documentId, true);

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
