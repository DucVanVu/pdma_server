package org.pepfar.pdma.app.api;

import java.util.List;

import org.pepfar.pdma.app.data.dto.NotificationDto;
import org.pepfar.pdma.app.data.dto.WRCommentFilterDto;
import org.pepfar.pdma.app.data.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping(path = "/api/v1/wrcomment")
public class WRCommentRestController
{

	@Autowired
	private NotificationService service;

	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<NotificationDto> getWRComment(@PathVariable("id") Long id) {
		NotificationDto dto = service.findById(id);

		if (dto == null) {
			return new ResponseEntity<>(new NotificationDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(dto, HttpStatus.OK);
	}

	@PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<NotificationDto>> getAllWRComments(@RequestBody WRCommentFilterDto filter) {

		List<NotificationDto> services = service.findList(filter);

		return new ResponseEntity<>(services, HttpStatus.OK);
	}

	@RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<NotificationDto> saveWRComment(@RequestBody NotificationDto dto) {

		if (dto == null) {
			return new ResponseEntity<>(new NotificationDto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.saveOne(dto);

		return new ResponseEntity<>(dto, HttpStatus.OK);
	}

	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteWRComments(@RequestBody NotificationDto[] dtos) {
		service.deleteMultiple(dtos);
	}

}
