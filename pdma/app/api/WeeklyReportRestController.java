package org.pepfar.pdma.app.api;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Workbook;
import org.pepfar.pdma.app.Constants;
import org.pepfar.pdma.app.data.domain.User;
import org.pepfar.pdma.app.data.dto.OrganizationDto;
import org.pepfar.pdma.app.data.dto.RoleDto;
import org.pepfar.pdma.app.data.dto.UserDto;
import org.pepfar.pdma.app.data.dto.UserOrganizationDto;
import org.pepfar.pdma.app.data.dto.WRChartDataDto;
import org.pepfar.pdma.app.data.dto.WRChartFilterDto;
import org.pepfar.pdma.app.data.dto.WRExportExcelFilterDto;
import org.pepfar.pdma.app.data.dto.WRProgressSummaryDto;
import org.pepfar.pdma.app.data.dto.WRProgressSummaryFilterDto;
import org.pepfar.pdma.app.data.dto.WeeklyReportDto;
import org.pepfar.pdma.app.data.dto.WeeklyReportFilterDto;
import org.pepfar.pdma.app.data.service.UserOrganizationService;
import org.pepfar.pdma.app.data.service.UserService;
import org.pepfar.pdma.app.data.service.WeeklyReportService;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.CacheControl;
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
@RequestMapping(path = "/api/v1/weekly_report")
public class WeeklyReportRestController {

	@Autowired
	private WeeklyReportService service;

	@Autowired
	private UserService userService;

	@Autowired
	private UserOrganizationService uoService;

	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<WeeklyReportDto> getWeeklyReport(@PathVariable("id") Long id) {
		WeeklyReportDto dto = service.findById(id);

		if (dto == null) {
			return new ResponseEntity<>(new WeeklyReportDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(dto, HttpStatus.OK);
	}

	@PostMapping(path = "/query", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<WeeklyReportDto> getWeeklyReportByWeekAndOrg(@RequestBody WeeklyReportDto dto) {
		return new ResponseEntity<>(service.findByWeekAndOrg(dto), HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@PostMapping(path = "/synthesis", produces = MediaType.APPLICATION_JSON_VALUE)
	public void forceSynthesis() {
		service.synthesize(true);
	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping(path = "/progress", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<WRProgressSummaryDto> getProgressSummary(@RequestBody WRProgressSummaryFilterDto filter) {

		if (filter == null) {
			filter = new WRProgressSummaryFilterDto();
		}

		User currentUser = SecurityUtils.getCurrentUser();
		filter.setUser(userService.findById(currentUser.getId()));

		return new ResponseEntity<>(service.findProgressSummary(filter), HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@DeleteMapping(path = "/demo", produces = MediaType.APPLICATION_JSON_VALUE)
	public void cleanDemoData() {
		service.cleanDemoData();
	}

	@PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<WeeklyReportDto>> getAllWeeklyReports(@RequestBody WeeklyReportFilterDto filter) {

		if (filter == null) {
			filter = new WeeklyReportFilterDto();
		}

		int role = 0; // 1 = SITE, 2 = PROV, 3 = NAT, 4 = DONOR

		User currentUser = SecurityUtils.getCurrentUser();
		UserDto userDto = userService.findById(currentUser.getId());

		for (RoleDto r : userDto.getRoles()) {

			if (r.getName().equalsIgnoreCase(Constants.ROLE_DONOR)) {
				role = 5;
				break;
			}

			if (r.getName().equalsIgnoreCase(Constants.ROLE_NATIONAL_MANAGER)) {
				role = role > 4 ? role : 4;
			}

			if (r.getName().equalsIgnoreCase(Constants.ROLE_PROVINCIAL_MANAGER)) {
				role = role > 3 ? role : 3;
			}

			if (r.getName().equalsIgnoreCase(Constants.ROLE_DISTRICT_MANAGER)) {
				role = role > 2 ? role : 2;
			}

			if (r.getName().equalsIgnoreCase(Constants.ROLE_SITE_MANAGER)) {
				role = role > 1 ? role : 1;
			}
		}

		filter.setRole(role == 5 ? Constants.ROLE_DONOR
				: role == 4 ? Constants.ROLE_NATIONAL_MANAGER
						: role == 3 ? Constants.ROLE_PROVINCIAL_MANAGER
								: role == 2 ? Constants.ROLE_DISTRICT_MANAGER
										: role == 1 ? Constants.ROLE_SITE_MANAGER : Constants.ROLE_USER);

		Page<WeeklyReportDto> services = service.findAllPageable(filter);

		return new ResponseEntity<>(services, HttpStatus.OK);
	}

//	@PreAuthorize("hasRole('ROLE_SITE_MANAGER') or hasRole('ROLE_DISTRICT_MANAGER') or hasRole('ROLE_PROVINCIAL_MANAGER')")
	@PreAuthorize("hasRole('ROLE_SITE_MANAGER')")
	@RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<WeeklyReportDto> saveWeeklyReport(@RequestBody WeeklyReportDto dto) {

		if (dto == null) {
			return new ResponseEntity<>(new WeeklyReportDto(), HttpStatus.BAD_REQUEST);
		}

		// Check permissions
//		checkEditingPermission(dto.getOrganization(), true, false);

		WeeklyReportDto existingDto = service.findByWeekAndOrg(dto);

		if (existingDto != null && !CommonUtils.isPositive(dto.getId(), true)) {
			// A report already found for this facility in the specified week
			return new ResponseEntity<>(dto, HttpStatus.OK);
		}

		dto = service.saveOne(dto);

		return new ResponseEntity<>(dto, HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ROLE_SITE_MANAGER') or hasRole('ROLE_DISTRICT_MANAGER') or hasRole('ROLE_PROVINCIAL_MANAGER') or hasRole('ROLE_NATIONAL_MANAGER')")
	@RequestMapping(
			path = "/status",
			method = { RequestMethod.POST, RequestMethod.PUT },
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<WeeklyReportDto> saveWeeklyReportStatus(@RequestBody WeeklyReportDto dto) {

		if (dto == null) {
			return new ResponseEntity<>(new WeeklyReportDto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.saveStatus(dto);

		return new ResponseEntity<>(dto, HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ROLE_DISTRICT_MANAGER')")
	@RequestMapping(
			path = "/dapproval",
			method = { RequestMethod.POST, RequestMethod.PUT },
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<WeeklyReportDto> saveDistrictApproval(@RequestBody WeeklyReportDto dto) {

		if (dto == null) {
			return new ResponseEntity<>(new WeeklyReportDto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.saveDApproval(dto);

		return new ResponseEntity<>(dto, HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ROLE_SITE_MANAGER') or hasRole('ROLE_DISTRICT_MANAGER') or hasRole('ROLE_PROVINCIAL_MANAGER')")
//	@PreAuthorize("hasRole('ROLE_SITE_MANAGER')")
	@RequestMapping(
			path = "/cases",
			method = { RequestMethod.POST, RequestMethod.PUT },
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<WeeklyReportDto> saveCases(@RequestBody WeeklyReportDto dto) {

		if (dto == null) {
			return new ResponseEntity<>(new WeeklyReportDto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.saveCases(dto);

		return new ResponseEntity<>(dto, HttpStatus.OK);
	}

//	@PreAuthorize("hasRole('ROLE_SITE_MANAGER') or hasRole('ROLE_DISTRICT_MANAGER')  or hasRole('ROLE_PROVINCIAL_MANAGER')")
	@PreAuthorize("hasRole('ROLE_SITE_MANAGER')")
	@DeleteMapping(path = "/cases/{delPosCase}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<WeeklyReportDto> deleteCases(@RequestBody WeeklyReportDto dto,
			@PathVariable Boolean delPosCase) {

		if (dto == null) {
			return new ResponseEntity<>(new WeeklyReportDto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.deleteCases(dto, delPosCase);

		return new ResponseEntity<>(dto, HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ROLE_SITE_MANAGER')")
	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteWeeklyReports(@RequestBody WeeklyReportDto[] dtos) {

		// Check permissions
//		for (WeeklyReportDto dto : dtos) {
//			checkEditingPermission(dto.getOrganization(), false, true);
//		}

		service.deleteMultiple(dtos);
	}

	@PostMapping(path = "/chart")
	public ResponseEntity<WRChartDataDto> getDataForChart(@RequestBody WRChartFilterDto filter) {

		User currentUser = SecurityUtils.getCurrentUser();

		if (filter == null) {
			filter = new WRChartFilterDto();
		}

		filter.setUser(userService.findByUsername(currentUser.getUsername()));

		return new ResponseEntity<>(service.getChartData(filter), HttpStatus.OK);
	}

	@PostMapping(path = "/excel")
	public void exportExcel(@RequestBody WRExportExcelFilterDto filter, HttpServletResponse response) {

		User user = SecurityUtils.getCurrentUser();
		UserDto userDto = userService.findById(user.getId());

		if (filter == null) {
			filter = new WRExportExcelFilterDto();
		}
		filter.setUser(userDto);

		Workbook wbook = service.createExcelFile(filter);

		if (wbook == null) {
			throw new RuntimeException();
		}

		String filename = "baocaotuan-" + System.currentTimeMillis() + ".xlsx";
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
			wbook.close();
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
	@SuppressWarnings("unused")
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
