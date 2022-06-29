package org.pepfar.pdma.app.api;

import org.pepfar.pdma.app.Constants;
import org.pepfar.pdma.app.data.domain.User;
import org.pepfar.pdma.app.data.dto.AnnouncementDto;
import org.pepfar.pdma.app.data.dto.AnnouncementFilterDto;
import org.pepfar.pdma.app.data.dto.RoleDto;
import org.pepfar.pdma.app.data.dto.UserDto;
import org.pepfar.pdma.app.data.service.AnnouncementService;
import org.pepfar.pdma.app.data.service.UserService;
import org.pepfar.pdma.app.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/announcement")
public class AnnouncementRestController
{

	@Autowired
	private AnnouncementService service;

	@Autowired
	private UserService uService;

	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<AnnouncementDto> getAnnouncement(@PathVariable("id") Long id) {
		AnnouncementDto dto = service.findById(id);

		if (dto == null) {
			return new ResponseEntity<>(new AnnouncementDto(), HttpStatus.BAD_REQUEST);
		}

		// Make sure only published announcement can be queried by non-editor users
		if (!editorFound() && dto.getStatus() == 0) {
			return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
		}

		return new ResponseEntity<>(dto, HttpStatus.OK);
	}

	@PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<AnnouncementDto>> getAllAnnouncements(@RequestBody AnnouncementFilterDto filter) {

		if (filter == null) {
			filter = new AnnouncementFilterDto();
		}

		// Make sure only published announcement can be queried by non-editor users
		if (!editorFound()) {
			filter.setPublishedOnly(true);
		}

		Page<AnnouncementDto> services = service.findAllPageable(filter);

		return new ResponseEntity<>(services, HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ROLE_DONOR') or hasRole('ROLE_NATIONAL_MANAGER')")
	@RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<AnnouncementDto> saveAnnouncement(@RequestBody AnnouncementDto dto) {

		if (dto == null) {
			return new ResponseEntity<>(new AnnouncementDto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.saveOne(dto);

		return new ResponseEntity<>(dto, HttpStatus.OK);
	}

	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('ROLE_DONOR') or hasRole('ROLE_NATIONAL_MANAGER')")
	public void deleteAnnouncements(@RequestBody AnnouncementDto[] dtos) {
		service.deleteMultiple(dtos);
	}

	private boolean editorFound() {
		User currentUser = SecurityUtils.getCurrentUser();
		UserDto uDto = uService.findById(currentUser.getId());

		boolean foundEditor = false;
		for (RoleDto r : uDto.getRoles()) {
			if (r.getName().equalsIgnoreCase(Constants.ROLE_NATIONAL_MANAGER)
					|| r.getName().equalsIgnoreCase(Constants.ROLE_DONOR)) {
				foundEditor = true;
				break;
			}
		}

		return foundEditor;
	}
}
