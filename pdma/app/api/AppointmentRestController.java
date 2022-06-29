package org.pepfar.pdma.app.api;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Workbook;
import org.pepfar.pdma.app.data.dto.AppointmentCalendarEventDto;
import org.pepfar.pdma.app.data.dto.AppointmentDto;
import org.pepfar.pdma.app.data.dto.AppointmentFilterDto;
import org.pepfar.pdma.app.data.dto.CaseOrgDto;
import org.pepfar.pdma.app.data.service.AppointmentService;
import org.pepfar.pdma.app.data.service.jpa.AppointmentServiceImpl.CustomList;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/appointment")
public class AppointmentRestController {

	@Autowired
	private AppointmentService service;

	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<AppointmentDto> getAppointment(@PathVariable("id") Long id) {
		AppointmentDto dto = service.findById(id);

		if (dto == null) {
			return new ResponseEntity<AppointmentDto>(new AppointmentDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<AppointmentDto>(dto, HttpStatus.OK);
	}

	@PostMapping(path = "/cal-events")
	public ResponseEntity<List<AppointmentCalendarEventDto>> getCalendarEvents(
			@RequestBody AppointmentFilterDto filter) {
		if (filter == null) {
			return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
		}

		List<AppointmentCalendarEventDto> ret = service.findAll4Calendar(filter);

		return new ResponseEntity<>(ret, HttpStatus.OK);
	}

	@PostMapping(path = "/late")
	public ResponseEntity<CustomList<AppointmentDto>> getLateList(@RequestBody AppointmentFilterDto filter) {
		CustomList<AppointmentDto> ret = service.findLateAppointments(filter);

		return new ResponseEntity<>(ret, HttpStatus.OK);
	}

	@PostMapping(path = "/list")
	public ResponseEntity<CustomList<AppointmentDto>> getList(@RequestBody AppointmentFilterDto filter) {
		CustomList<AppointmentDto> ret = service.findAll(filter);

		return new ResponseEntity<>(ret, HttpStatus.OK);
	}

	@PostMapping(path = "/patient-has-no-appointment")
	public ResponseEntity<CustomList<CaseOrgDto>> getPatientHasNoAppointment(@RequestBody AppointmentFilterDto filter) {
		CustomList<CaseOrgDto> ret = service.getPatientHasNoAppointment(filter);

		return new ResponseEntity<CustomList<CaseOrgDto>>(ret, HttpStatus.OK);
	}

	@PostMapping(path = "/individual-list")
	public ResponseEntity<List<AppointmentDto>> getList4OneIndividual(@RequestBody AppointmentFilterDto filter) {
		if (filter == null) {
			return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
		}

		List<AppointmentDto> ret = service.findAppointments4OneCase(filter);

		return new ResponseEntity<>(ret, HttpStatus.OK);
	}

	@PostMapping(path = "/individual-list-all/{caseId}")
	public ResponseEntity<List<AppointmentDto>> getAll4OneIndividual(@PathVariable("caseId") Long caseId) {
		List<AppointmentDto> ret = service.findAllAppointments4OneCase(caseId);
		return new ResponseEntity<>(ret, HttpStatus.OK);
	}

	@RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<AppointmentDto> saveAppointment(@RequestBody AppointmentDto dto) {

		if (dto == null) {
			return new ResponseEntity<AppointmentDto>(new AppointmentDto(), HttpStatus.BAD_REQUEST);
		}

		dto = service.saveOne(dto);

		return new ResponseEntity<AppointmentDto>(dto, HttpStatus.OK);
	}

	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteAppointments(@RequestBody AppointmentDto[] dtos) {
		service.deleteMultiple(dtos);
	}

	@PostMapping(path = "/excel-4-late")
	public void exportLatePatients(@RequestBody AppointmentFilterDto filter, HttpServletResponse response) {

		Workbook wbook = service.exportLatePatients(filter);

		if (wbook == null) {
			throw new RuntimeException();
		}

		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

		String filename = "bn-kham-muon-" + sdf.format(CommonUtils.fromLocalDateTime(CommonUtils.hanoiNow())) + ".xlsx";
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

	@PostMapping(path = "/excel-daily-appointments")
	public void exportDailyAppointmentTemplate(@RequestBody AppointmentFilterDto filter, HttpServletResponse response) {

		if (filter == null || filter.getFromDate() == null || filter.getToDate() == null) {
			try {
				response.sendError(409);
			} catch (IOException e) {
				e.printStackTrace();
			}

			return;
		}

		Workbook wbook = service.exportDailyAppointments(filter);

		if (wbook == null) {
			throw new RuntimeException();
		}

		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

		String filename = "lichtaikham-";
		filename += sdf.format(CommonUtils.fromLocalDateTime(filter.getFromDate()));
		filename += "_";
		filename += sdf.format(CommonUtils.fromLocalDateTime(filter.getToDate()));
		filename += ".xlsx";

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

	@PostMapping(path = "/excel-late-appointments")
	public void exportLateAppointmentTemplate(@RequestBody AppointmentFilterDto filter, HttpServletResponse response) {

		Workbook wbook = service.exportLateAppointments(filter);

		if (wbook == null) {
			throw new RuntimeException();
		}

		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

		String filename = "bn-kham-muon-" + sdf.format(CommonUtils.fromLocalDateTime(CommonUtils.hanoiNow())) + ".xlsx";
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

	@PostMapping(path = "/excel-patient-has-no-appointment")
	public void exportPatientHasNoAppointment(@RequestBody AppointmentFilterDto filter, HttpServletResponse response) {

		Workbook wbook = service.exportPatientHasNoAppointment(filter);

		if (wbook == null) {
			throw new RuntimeException();
		}

		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

		String filename = "bn-khong-co-lich-hen-kham-" + sdf.format(CommonUtils.fromLocalDateTime(CommonUtils.hanoiNow())) + ".xlsx";
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
