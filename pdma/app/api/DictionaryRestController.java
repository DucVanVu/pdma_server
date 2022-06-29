package org.pepfar.pdma.app.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.pepfar.pdma.app.data.dto.DictionaryDto;
import org.pepfar.pdma.app.data.dto.DictionaryFilterDto;
import org.pepfar.pdma.app.data.dto.DictionaryTypeDto;
import org.pepfar.pdma.app.data.service.DictionaryService;
import org.pepfar.pdma.app.data.types.DictionaryType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;

@RestController
@RequestMapping(path = "/api/v1/dictionary")
public class DictionaryRestController
{

	@Autowired
	private DictionaryService service;

	@Autowired
	private MessageSource msgSource;

	@GetMapping(path = "/types")
	public ResponseEntity<List<DictionaryTypeDto>> getAllDictionaryTypes(Locale locale) {
		List<DictionaryTypeDto> list = new ArrayList<>();
		Lists.newArrayList(DictionaryType.values()).forEach(e -> {
			DictionaryTypeDto dto = new DictionaryTypeDto();
			dto.setType(e);
			dto.setTitle(msgSource.getMessage("pdma.dictionary." + e.name().toLowerCase(), null, locale));

			list.add(dto);
		});

		return new ResponseEntity<>(list, HttpStatus.OK);
	}

	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DictionaryDto> getDictionaryEntry(@PathVariable("id") Long id) {
		DictionaryDto dto = service.findById(id);

		if (dto == null) {
			return new ResponseEntity<DictionaryDto>(new DictionaryDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<DictionaryDto>(dto, HttpStatus.OK);
	}

	@PostMapping(path = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<DictionaryDto>> getAllDictionaryEntries(@RequestBody DictionaryFilterDto filter) {
		return new ResponseEntity<List<DictionaryDto>>(service.findAll(filter), HttpStatus.OK);
	}

	@PostMapping(path = "/multiple")
	public ResponseEntity<List<DictionaryTypeDto>> getMultipleDictionaryEntries(@RequestBody DictionaryDto[] dtos) {

		if (dtos == null || dtos.length <= 0) {
			return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
		}

		List<DictionaryType> types = new ArrayList<>();
		Lists.newArrayList(dtos).forEach(d -> {
			types.add(d.getType());
		});

		return new ResponseEntity<>(service.findMultiple(types), HttpStatus.OK);
	}

	@PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<DictionaryDto>> getAllDictionaryEntriesPageable(
			@RequestBody DictionaryFilterDto filter) {

		Page<DictionaryDto> dictionaryEntries = service.findAllPageable(filter);

		return new ResponseEntity<Page<DictionaryDto>>(dictionaryEntries, HttpStatus.OK);
	}

	@RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DictionaryDto> saveDictionaryEntry(@RequestBody DictionaryDto dto) {

		if (dto == null) {
			return new ResponseEntity<DictionaryDto>(new DictionaryDto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.saveOne(dto);

		return new ResponseEntity<DictionaryDto>(dto, HttpStatus.OK);
	}

	@PutMapping(path = "/reorder", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean> saveDictionaryEntries(@RequestBody DictionaryDto[] dtos) {
		Boolean successful = service.saveSortOrder(dtos);

		if (successful) {
			return new ResponseEntity<Boolean>(Boolean.TRUE, HttpStatus.OK);
		} else {
			return new ResponseEntity<Boolean>(Boolean.FALSE, HttpStatus.BAD_REQUEST);
		}
	}

	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteDictionaryEntries(@RequestBody DictionaryDto[] dtos) {
		service.deleteMultiple(dtos);
	}

}
