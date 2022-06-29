package org.pepfar.pdma.app.api;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Workbook;
import org.pepfar.pdma.app.Constants;
import org.pepfar.pdma.app.data.domain.User;
import org.pepfar.pdma.app.data.dto.OrganizationDto;
import org.pepfar.pdma.app.data.dto.PNSAssessmentDto;
import org.pepfar.pdma.app.data.dto.PNSAssessmentFilterDto;
import org.pepfar.pdma.app.data.dto.PNSAssessmentPreferencesDto;
import org.pepfar.pdma.app.data.dto.RoleDto;
import org.pepfar.pdma.app.data.dto.UserDto;
import org.pepfar.pdma.app.data.dto.UserOrganizationDto;
import org.pepfar.pdma.app.data.service.PNSAssessmentService;
import org.pepfar.pdma.app.data.service.UserOrganizationService;
import org.pepfar.pdma.app.data.service.UserService;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.FileUtils;
import org.pepfar.pdma.app.utils.SecurityUtils;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path = "/api/v1/pns_assessment")
public class PNSAssessmentRestController {

	@Autowired
	private PNSAssessmentService service;

	@Autowired
	private UserService userService;

	@Autowired
	private UserOrganizationService uoService;

	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PNSAssessmentDto> getAssessment(@PathVariable("id") Long id) {
		PNSAssessmentDto dto = service.findById(id, null, false);

		if (dto == null) {
			return new ResponseEntity<PNSAssessmentDto>(new PNSAssessmentDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<PNSAssessmentDto>(dto, HttpStatus.OK);
	}

	@GetMapping(path = "/has-baseline/{facilityId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean> hasBaseline(@PathVariable("facilityId") Long facilityId) {
		boolean hasBaseline = service.hasBaseline(facilityId);
		return new ResponseEntity<>(hasBaseline, HttpStatus.OK);
	}

	@GetMapping(path = "/has-post/{facilityId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean> hasPost(@PathVariable("facilityId") Long facilityId) {
		boolean hasPost = service.hasPost(facilityId);
		return new ResponseEntity<>(hasPost, HttpStatus.OK);
	}

	@PostMapping(path = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<PNSAssessmentDto>> getAllAssessments(@RequestBody PNSAssessmentFilterDto filter) {

		User user = SecurityUtils.getCurrentUser();
		if (filter != null) {
			filter.setUser(user);
		}

		List<PNSAssessmentDto> services = service.findAll(filter);

		return new ResponseEntity<List<PNSAssessmentDto>>(services, HttpStatus.OK);
	}

	@PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<PNSAssessmentDto>> getAllAssessmentsPageable(
			@RequestBody PNSAssessmentFilterDto filter) {

		User user = SecurityUtils.getCurrentUser();
		if (filter != null) {
			filter.setUser(user);
		}

		Page<PNSAssessmentDto> services = service.findAllPageable(filter);

		return new ResponseEntity<Page<PNSAssessmentDto>>(services, HttpStatus.OK);
	}

	@RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PNSAssessmentDto> saveAssessment(@RequestBody PNSAssessmentDto dto) {

		if (dto == null) {
			return new ResponseEntity<PNSAssessmentDto>(new PNSAssessmentDto(), HttpStatus.BAD_REQUEST);
		}

		// check permissions
		checkEditingPermission(dto.getFacility(), true, false);

		dto = service.saveOne(dto);

		return new ResponseEntity<PNSAssessmentDto>(dto, HttpStatus.OK);
	}

	@GetMapping(path = "/prefs", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PNSAssessmentPreferencesDto> getPreferences() {
		PNSAssessmentPreferencesDto dto = service.getPreferences();
		return new ResponseEntity<PNSAssessmentPreferencesDto>(dto, HttpStatus.OK);
	}

	@PostMapping(path = "/prefs", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PNSAssessmentPreferencesDto> savePreferences(@RequestBody PNSAssessmentPreferencesDto dto) {

		if (dto == null) {
			return new ResponseEntity<PNSAssessmentPreferencesDto>(new PNSAssessmentPreferencesDto(),
					HttpStatus.BAD_REQUEST);
		}

		service.setPreferences(dto);
		return new ResponseEntity<PNSAssessmentPreferencesDto>(dto, HttpStatus.OK);
	}

	@RequestMapping(
			path = "/submit",
			method = { RequestMethod.POST, RequestMethod.PUT },
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PNSAssessmentDto> submitAssessment(@RequestBody PNSAssessmentDto dto) {

		if (dto == null) {
			return new ResponseEntity<PNSAssessmentDto>(new PNSAssessmentDto(), HttpStatus.BAD_REQUEST);
		}

		// check permissions
		checkEditingPermission(dto.getFacility(), true, false);

		dto = service.submit(dto);

		return new ResponseEntity<PNSAssessmentDto>(dto, HttpStatus.OK);
	}

	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteAssessments(@RequestBody PNSAssessmentDto[] dtos) {

		// check permissions
		for (PNSAssessmentDto dto : dtos) {
			checkEditingPermission(dto.getFacility(), false, true);
		}

		service.deleteMultiple(dtos);
	}

	@RequestMapping(path = "/upload/{pnsId}/{prop}", method = RequestMethod.POST)
	public ResponseEntity<PNSAssessmentDto> uploadDocument(@PathVariable("pnsId") Long pnsAssessmentId,
			@PathVariable("prop") String propName, @RequestParam("file") MultipartFile file) {

		PNSAssessmentDto dto = null;

		if (CommonUtils.isEmpty(propName) || !CommonUtils.isPositive(pnsAssessmentId, true)) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}

		dto = service.findById(pnsAssessmentId, null, false);
		if (dto == null) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}

		// check permissions
		checkEditingPermission(dto.getFacility(), true, false);

		try {
			if (!file.isEmpty()) {
				byte[] data = file.getBytes();
				String filename = String.valueOf(System.currentTimeMillis()) + "."
						+ FileUtils.getFileExtension(file.getOriginalFilename());

				switch (propName) {
					case "q1_6b_file":
						dto.setQ1_6b_file(data);
						dto.setQ1_6b_file_contentLength((long) data.length);
						dto.setQ1_6b_file_name(filename);
						break;
					case "q1_7_file":
						dto.setQ1_7_file(data);
						dto.setQ1_7_file_contentLength((long) data.length);
						dto.setQ1_7_file_name(filename);
						break;
					case "q2_2_file":
						dto.setQ2_2_file(data);
						dto.setQ2_2_file_contentLength((long) data.length);
						dto.setQ2_2_file_name(filename);
						break;
					case "q2_6_file":
						dto.setQ2_6_file(data);
						dto.setQ2_6_file_contentLength((long) data.length);
						dto.setQ2_6_file_name(filename);
						break;
					case "q3_3_file":
						dto.setQ3_3_file(data);
						dto.setQ3_3_file_contentLength((long) data.length);
						dto.setQ3_3_file_name(filename);
						break;
				}

				dto = service.saveAttachment(dto, propName);

				if (dto != null) {
					return new ResponseEntity<>(dto, HttpStatus.OK);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
	}

	@RequestMapping(value = "/download/{pnsId}/{prop}", method = RequestMethod.GET)
	public void downloadDocument(@PathVariable("pnsId") Long pnsAssessmentId, @PathVariable("prop") String propName,
			HttpServletResponse response) {

		if (!CommonUtils.isPositive(pnsAssessmentId, true) || CommonUtils.isEmpty(propName)) {
			throw new RuntimeException();
		}

		PNSAssessmentDto dto = service.findById(pnsAssessmentId, propName, true);

		if (dto == null) {
			throw new RuntimeException();
		}

		String filename = "_";
		byte[] fileContent = null;

		switch (propName) {
			case "q1_6b_file":
				filename = dto.getQ1_6b_file_name();
				fileContent = dto.getQ1_6b_file();
				break;
			case "q1_7_file":
				filename = dto.getQ1_7_file_name();
				fileContent = dto.getQ1_7_file();
				break;
			case "q2_2_file":
				filename = dto.getQ2_2_file_name();
				fileContent = dto.getQ2_2_file();
				break;
			case "q2_6_file":
				filename = dto.getQ2_6_file_name();
				fileContent = dto.getQ2_6_file();
				break;
			case "q3_3_file":
				filename = dto.getQ3_3_file_name();
				fileContent = dto.getQ3_3_file();
				break;
		}

		CacheControl cc = CacheControl.maxAge(360, TimeUnit.DAYS).cachePublic();

		response.addHeader("Access-Control-Expose-Headers", "x-filename");
		response.addHeader("Content-disposition", "inline; filename=" + filename);
		response.addHeader("x-filename", filename);
		response.setHeader("Cache-Control", cc.getHeaderValue());
//		response.setContentType(document.getContentType());

		try {
			response.getOutputStream().write(fileContent);
			response.flushBuffer();
			response.getOutputStream().close();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}

	}

	@GetMapping(path = "/facility-report/{id}")
	public void exportFacilityReport(@PathVariable Long id, HttpServletResponse response) {

		if (id == null) {
			id = 0l;
		}

		Workbook wbook = service.generateFacilityReport(id);

		if (wbook == null) {
			throw new RuntimeException();
		}

		String filename = "ketqua-danhgia-" + System.currentTimeMillis() + ".xlsx";
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

	@PostMapping(path = "/excel")
	public void exportExcel(@RequestBody PNSAssessmentFilterDto filter, HttpServletResponse response) {

		User user = SecurityUtils.getCurrentUser();

		if (filter == null) {
			filter = new PNSAssessmentFilterDto();
		}

		filter.setUser(user);

		Workbook wbook = service.exportData(filter);

		if (wbook == null) {
			throw new RuntimeException();
		}

		String filename = "dulieu_danhgia-" + System.currentTimeMillis() + ".xlsx";
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
	
	@PostMapping(path = "/excel_detailed/{type}")
	public void exportExcel(@PathVariable("type") Integer type, HttpServletResponse response) {

		Workbook wbook = service.exportDataDetailed(type);

		if (wbook == null) {
			throw new RuntimeException();
		}

		String filename = "dulieu-danhgia-chitiet-" + System.currentTimeMillis() + ".xlsx";
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

	/**
	 * Check if the current user is a site manager and has the edit/delete
	 * permission on the current org
	 * 
	 * @param org
	 */
	private void checkEditingPermission(OrganizationDto org, boolean checkEdit, boolean checkDelete) {

		User currentUser = SecurityUtils.getCurrentUser();
		UserDto userDto = userService.findById(currentUser.getId());

		boolean isSiteManager = false;

		for (RoleDto role : userDto.getRoles()) {
			if (role.getName().equals(Constants.ROLE_SITE_MANAGER)) {
				isSiteManager = true;
			}
		}

		if (!isSiteManager) {
			throw new RuntimeException("Only a site manager can edit/delete an assessment.");
		}

		boolean hasWriteAccess = false;
		boolean hasDeleteAccess = false;

		List<UserOrganizationDto> uos = uoService.findAll(currentUser.getId());
		for (UserOrganizationDto uo : uos) {
			if (uo.getOrganization().getId().longValue() == org.getId()) {
				if (uo.getWriteAccess() != null && uo.getWriteAccess() == true) {
					hasWriteAccess = true;
				}

				if (uo.getDeleteAccess() != null && uo.getDeleteAccess() == true) {
					hasDeleteAccess = true;
				}
			}
		}

		if (checkEdit && !hasWriteAccess) {
			throw new RuntimeException("This user does not has write permission on the selected organization.");
		}

		if (checkDelete && !hasDeleteAccess) {
			throw new RuntimeException("This user does not has delete permission on the selected organization.");
		}
	}

}
