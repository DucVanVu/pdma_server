package org.pepfar.pdma.app.api;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Workbook;
import org.pepfar.pdma.app.data.domain.User;
import org.pepfar.pdma.app.data.dto.PasswordChangeDto;
import org.pepfar.pdma.app.data.dto.PhotoCropperDto;
import org.pepfar.pdma.app.data.dto.UserDto;
import org.pepfar.pdma.app.data.dto.UserFilterDto;
import org.pepfar.pdma.app.data.service.UserService;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.ImageUtils;
import org.pepfar.pdma.app.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UserRestController {

	@Autowired
	private UserService userService;

	@PreAuthorize("isAuthenticated()")
	@RequestMapping(path = "/api/v1/user/current", method = RequestMethod.GET)
	public ResponseEntity<UserDto> currentUser() {
		User currentUser = SecurityUtils.getCurrentUser();
		UserDto user = userService.findByUsername(currentUser.getUsername());

		if (user != null) {
			user.setPassword(null);
		} else {
			user = new UserDto();
		}

		return new ResponseEntity<UserDto>(user, HttpStatus.OK);
	}

	@PreAuthorize("isAuthenticated()")
	@RequestMapping(path = "/api/v1/user/id/{id}", method = RequestMethod.GET)
	public ResponseEntity<UserDto> getUser(@PathVariable("id") long userId) {
		UserDto user = userService.findById(userId);

		if (user != null) {
			user.setPassword(null);
		} else {
			user = new UserDto();
		}

		return new ResponseEntity<UserDto>(user, HttpStatus.OK);
	}

	@PreAuthorize("isAuthenticated()")
	@RequestMapping(path = "/api/v1/user/username/{username}", method = RequestMethod.GET)
	public ResponseEntity<UserDto> getUser(@PathVariable("username") String username) {

		UserDto user = userService.findByUsername(username);

		if (user != null) {
			user.setPassword(null);
		} else {
			user = new UserDto();
		}

		return new ResponseEntity<UserDto>(user, HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ROLE_ADMIN') and isFullyAuthenticated()")
	@PutMapping(path = "/api/v1/user/dangerous_reset")
	public void resetPassword4AllUsersExceptAdmin(@RequestBody UserDto dto) {
		if (CommonUtils.isEmpty(dto.getPassword())) {
			return;
		}

		userService.resetPasswordForAllUsersExceptAdmin(dto.getPassword());
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@PutMapping(path = "/api/v1/user/demo")
	public void createOrUpdateDemoUsers() {
//		userService.createOrUpdateDemoUsers();
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@PostMapping(path = "/api/v1/user/all")
	public ResponseEntity<List<UserDto>> findAll(@RequestBody UserFilterDto filter) {
		return new ResponseEntity<List<UserDto>>(userService.findAll(filter), HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@PostMapping(path = "/api/v1/user/list")
	public ResponseEntity<Page<UserDto>> getAllUsers(@RequestBody UserFilterDto filter) {
		return new ResponseEntity<Page<UserDto>>(userService.findAllPageable(filter), HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ROLE_ADMIN') and isFullyAuthenticated()")
	@RequestMapping(path = "/api/v1/user", method = RequestMethod.POST)
	public ResponseEntity<UserDto> insertUser(@RequestBody UserDto dto) {

		if (CommonUtils.isNull(dto)) {
			return new ResponseEntity<UserDto>(new UserDto(), HttpStatus.BAD_REQUEST);
		}

		dto = userService.saveOne(dto);

		if (dto == null) {
			return new ResponseEntity<UserDto>(new UserDto(), HttpStatus.BAD_REQUEST);
		} else {
			return new ResponseEntity<UserDto>(dto, HttpStatus.OK);
		}
	}

	@PreAuthorize("hasRole('ROLE_ADMIN') and isFullyAuthenticated()")
	@RequestMapping(path = "/api/v1/user", method = RequestMethod.DELETE)
	public void deleteUsers(@RequestBody UserDto[] dtos) {
		userService.deleteMultiple(dtos);
	}

	@PreAuthorize("hasRole('ROLE_ADMIN') and isFullyAuthenticated()")
	@RequestMapping(path = "/api/v1/user/password", method = RequestMethod.PUT)
	public ResponseEntity<UserDto> changePassword(@RequestBody UserDto user) {
		return new ResponseEntity<UserDto>(userService.changePassword(user), HttpStatus.OK);
	}

	@PreAuthorize("isAuthenticated()")
	@RequestMapping(path = "/api/v1/user/password/self", method = RequestMethod.PUT)
	public ResponseEntity<UserDto> changeMyPassword(@RequestBody UserDto dto) {

		User user = SecurityUtils.getCurrentUser();
		if (user == null) {
			return new ResponseEntity<UserDto>(new UserDto(), HttpStatus.FORBIDDEN);
		}

		if (!user.getUsername().equals(dto.getUsername()) || user.getId().longValue() != dto.getId()) {
			return new ResponseEntity<UserDto>(new UserDto(), HttpStatus.FORBIDDEN);
		}

		return new ResponseEntity<UserDto>(userService.changePassword(dto), HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(path = "/api/v1/user/duplicate/email", method = RequestMethod.POST)
	public ResponseEntity<Boolean> emailDuplicate(@RequestBody UserDto user) {

		if (user == null) {
			return new ResponseEntity<Boolean>(false, HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<Boolean>(userService.foundDuplicateEmail(user.getId(), user.getEmail()),
				HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(path = "/api/v1/user/duplicate/username", method = RequestMethod.POST)
	public ResponseEntity<Boolean> usernameDuplicate(@RequestBody UserDto user) {

		if (user == null) {
			return new ResponseEntity<Boolean>(false, HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<Boolean>(userService.foundDuplicateUsername(user.getId(), user.getUsername()),
				HttpStatus.OK);
	}

	@PreAuthorize("isAuthenticated()")
	@RequestMapping(path = "/api/v1/user/password/valid", method = RequestMethod.POST)
	public ResponseEntity<Boolean> passwordValid(@RequestBody PasswordChangeDto dto) {
		if (dto == null) {
			return new ResponseEntity<Boolean>(false, HttpStatus.BAD_REQUEST);
		}

		User user = SecurityUtils.getCurrentUser();
		if (user == null) {
			return new ResponseEntity<Boolean>(false, HttpStatus.FORBIDDEN);
		}

		Boolean matched = SecurityUtils.passwordsMatch(user.getPassword(), dto.getPassword());

		return new ResponseEntity<Boolean>(matched, HttpStatus.OK);
	}

	@PreAuthorize("isAuthenticated()")
	@RequestMapping(path = "/api/v1/user/photo/upload", method = RequestMethod.POST)
	public ResponseEntity<UserDto> uploadProfilePhoto(@RequestParam("file") MultipartFile file) {

		User user = SecurityUtils.getCurrentUser();
		UserDto userDto = new UserDto();

		userDto.setId(user.getId());

		try {
			if (!file.isEmpty()) {
				byte[] data = file.getBytes();

				if (data != null && data.length > 0) {
					userDto.setPhoto(data);
					userDto.setPhotoCropped(false);

					userDto = userService.savePhoto(userDto);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new ResponseEntity<UserDto>(userDto, HttpStatus.OK);
	}

	@PreAuthorize("isAuthenticated()")
	@RequestMapping(path = "/api/v1/user/photo/crop", method = RequestMethod.POST)
	public ResponseEntity<UserDto> cropProfilePhoto(@RequestBody PhotoCropperDto dto) {

		User user = SecurityUtils.getCurrentUser();

		if (dto.getUser() == null || !user.getUsername().equals(dto.getUser().getUsername())) {
			return new ResponseEntity<UserDto>(new UserDto(), HttpStatus.FORBIDDEN);
		}

		byte[] userPhoto = userService.getProfilePhoto(dto.getUser().getUsername());

		if (userPhoto == null || userPhoto.length <= 0 || dto.getX() < 0 || dto.getY() < 0 || dto.getW() <= 0
				|| dto.getH() <= 0) {
			return new ResponseEntity<UserDto>(new UserDto(), HttpStatus.BAD_REQUEST);
		}

		userPhoto = ImageUtils.crop(userPhoto, dto.getX(), dto.getY(), dto.getW(), dto.getH());

		if (userPhoto == null) {
			return new ResponseEntity<UserDto>(new UserDto(), HttpStatus.BAD_REQUEST);
		}

		UserDto userDto = new UserDto();
		userDto.setId(user.getId());

		try {
			userDto.setPhoto(userPhoto);
			userDto.setPhotoCropped(true);

			userDto = userService.savePhoto(userDto);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new ResponseEntity<UserDto>(userDto, HttpStatus.OK);
	}

	@PreAuthorize("isAuthenticated()")
	@RequestMapping(value = "/api/v1/user/heartbeat", method = RequestMethod.GET)
	public void ping() {
		System.out.println("User session monitored...");
	}

	@RequestMapping(value = "/public/user/photo/{username}", method = RequestMethod.GET)
	public void getProfilePhoto(HttpServletResponse response, @PathVariable("username") String username)
			throws ServletException, IOException {

		byte[] data = userService.getProfilePhoto(username);

		// CacheControl cc = CacheControl.maxAge(360, TimeUnit.DAYS).cachePublic();

		// response.setHeader("Cache-Control", cc.getHeaderValue());
		response.setContentType("image/jpg");

		try {
			response.getOutputStream().write(data);
			response.flushBuffer();
			response.getOutputStream().close();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(path = "/api/v1/user/export", method = RequestMethod.POST)
	public void export(@RequestBody UserFilterDto filter, HttpServletResponse response) {

		Workbook wbook = this.userService.exportUser(filter);

		if (wbook == null) {
			throw new RuntimeException();
		}

		String filename = "user-list";

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
