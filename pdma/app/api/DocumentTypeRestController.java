package org.pepfar.pdma.app.api;

import java.util.List;

import org.pepfar.pdma.app.data.dto.DocumentTypeDto;
import org.pepfar.pdma.app.data.service.DocumentTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/document_type")
public class DocumentTypeRestController
{

	@Autowired
	private DocumentTypeService service;

	@RequestMapping(path = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DocumentTypeDto> getDocumentType(@PathVariable("id") Long id) {
		DocumentTypeDto dto = service.findById(id);

		if (dto == null) {
			return new ResponseEntity<DocumentTypeDto>(new DocumentTypeDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<DocumentTypeDto>(dto, HttpStatus.OK);
	}

	@RequestMapping(path = "/all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<DocumentTypeDto>> getAllDocumentTypes() {
		return new ResponseEntity<List<DocumentTypeDto>>(service.findAll(), HttpStatus.OK);
	}

	@RequestMapping(path = "/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<DocumentTypeDto>> getAllDocumentTypes(
			@RequestParam(name = "page", required = true, defaultValue = "0") int pageIndex,
			@RequestParam(name = "size", required = true, defaultValue = "20") int pageSize) {

		return new ResponseEntity<Page<DocumentTypeDto>>(service.findAll(pageIndex, pageSize), HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DocumentTypeDto> saveDocumentType(@RequestBody DocumentTypeDto dto) {
		if (dto == null) {
			return new ResponseEntity<DocumentTypeDto>(new DocumentTypeDto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.saveOne(dto);

		return new ResponseEntity<DocumentTypeDto>(dto, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteDocumentTypes(@RequestBody DocumentTypeDto[] dtos) {
		service.deleteMultiple(dtos);
	}

}
