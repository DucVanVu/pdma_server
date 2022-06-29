package org.pepfar.pdma.app.api;

import org.pepfar.pdma.app.data.dto.PreferencesDto;
import org.pepfar.pdma.app.data.service.PreferencesService;
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
@RequestMapping(path = "/api/v1/preferences")
public class PreferencesRestController
{

	@Autowired
	private PreferencesService service;

	@RequestMapping(path = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PreferencesDto> getPreference(@PathVariable("id") Long id) {
		PreferencesDto dto = service.findById(id);

		if (dto == null) {
			return new ResponseEntity<PreferencesDto>(new PreferencesDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<PreferencesDto>(dto, HttpStatus.OK);
	}
	
	@RequestMapping(path = "/name/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PreferencesDto> getPreference(@PathVariable("name") String name) {
		PreferencesDto dto = service.findByName(name);

		if (dto == null) {
			return new ResponseEntity<PreferencesDto>(new PreferencesDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<PreferencesDto>(dto, HttpStatus.OK);
	}

	@RequestMapping(path = "/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<PreferencesDto>> getAllPreferences(
			@RequestParam(name = "page", required = true, defaultValue = "0") int pageIndex,
			@RequestParam(name = "size", required = true, defaultValue = "20") int pageSize) {

		return new ResponseEntity<Page<PreferencesDto>>(service.findAll(pageIndex, pageSize), HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PreferencesDto> savePreference(@RequestBody PreferencesDto dto) {
		if (dto == null) {
			return new ResponseEntity<PreferencesDto>(new PreferencesDto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.saveOne(dto);

		return new ResponseEntity<PreferencesDto>(dto, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public void deletePreferences(@RequestBody PreferencesDto[] dtos) {
		service.deleteMultiple(dtos);
	}

}
