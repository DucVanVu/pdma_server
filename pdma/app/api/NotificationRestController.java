package org.pepfar.pdma.app.api;

import org.pepfar.pdma.app.Constants;
import org.pepfar.pdma.app.data.domain.User;
import org.pepfar.pdma.app.data.dto.NotificationDto;
import org.pepfar.pdma.app.data.dto.OPCAssistNotificationData;
import org.pepfar.pdma.app.data.service.NotificationService;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/notification")
public class NotificationRestController {

	@Autowired
	private NotificationService service;

	@GetMapping(path = "/{id}")
	public ResponseEntity<NotificationDto> findById(@PathVariable("id") Long id) {
		return new ResponseEntity<>(service.findById(id), HttpStatus.OK);
	}

	@GetMapping(path = "/unread")
	public ResponseEntity<Long> countUnread() {
		User currentUser = SecurityUtils.getCurrentUser();
		return new ResponseEntity<>(service.countUnread(currentUser), HttpStatus.OK);
	}

	@GetMapping(path = "/list")
	public ResponseEntity<Page<NotificationDto>> findPageable() {
		User currentUser = SecurityUtils.getCurrentUser();
		return new ResponseEntity<>(service.findNotificationsPageable(currentUser), HttpStatus.OK);
	}

	@PutMapping(path = "/mark_read")
	public void markAsRead() {
		User currentUser = SecurityUtils.getCurrentUser();
		service.markAsRead(currentUser);
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@PostMapping(path = "/broadcast_all")
	public ResponseEntity<Boolean> broadcastAll(@RequestBody NotificationDto dto) {
		User currentUser = SecurityUtils.getCurrentUser();
		return new ResponseEntity<>(service.broadcastAll(dto, currentUser), HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ROLE_NATIONAL_MANAGER') or hasRole('ROLE_PROVINCIAL_MANAGER')")
	@PostMapping(path = "/broadcast")
	public ResponseEntity<Boolean> broadcast(@RequestBody NotificationDto dto) {
		User currentUser = SecurityUtils.getCurrentUser();
		return new ResponseEntity<>(service.broadcast(dto, currentUser), HttpStatus.OK);
	}

	///
	// ====> For OPC Assist minimal notification data
	///
	@GetMapping(path = "/opc-notificaton/{orgId}")
	public ResponseEntity<OPCAssistNotificationData> findOPCAssistNotiData(@PathVariable("orgId") Long orgId) {
		User currentUser = SecurityUtils.getCurrentUser();

		if (currentUser == null || !SecurityUtils.isUserInRole(currentUser, Constants.ROLE_SITE_MANAGER)
				|| !CommonUtils.isPositive(orgId, true)) {
			return new ResponseEntity<OPCAssistNotificationData>(new OPCAssistNotificationData(), HttpStatus.OK);
		}

		return new ResponseEntity<OPCAssistNotificationData>(service.getOPCNotificationData(orgId), HttpStatus.OK);
	}
}
