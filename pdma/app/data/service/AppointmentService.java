package org.pepfar.pdma.app.data.service;

import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.pepfar.pdma.app.data.dto.AppointmentCalendarEventDto;
import org.pepfar.pdma.app.data.dto.AppointmentDto;
import org.pepfar.pdma.app.data.dto.AppointmentFilterDto;
import org.pepfar.pdma.app.data.dto.CaseOrgDto;
import org.pepfar.pdma.app.data.service.jpa.AppointmentServiceImpl.CustomList;
import org.springframework.data.domain.Page;

public interface AppointmentService {

	public AppointmentDto findById(Long id);

	public List<AppointmentCalendarEventDto> findAll4Calendar(AppointmentFilterDto filter);

	public CustomList<AppointmentDto> findAll(AppointmentFilterDto filter);

	public CustomList<AppointmentDto> findLateAppointments(AppointmentFilterDto filter);

	public List<AppointmentDto> findAppointments4OneCase(AppointmentFilterDto filter);

	public List<AppointmentDto> findAllAppointments4OneCase(Long caseId);

	public Workbook exportLatePatients(AppointmentFilterDto filter);

	public Workbook exportDailyAppointments(AppointmentFilterDto filter);

	public AppointmentDto saveOne(AppointmentDto dto);

	public void updateMissedAppointments(Long caseId, Long orgId);

	public void deleteMultiple(AppointmentDto[] dtos);

//	public Workbook exportDailyAppointmentTemplate(AppointmentFilterDto filter);
	public Workbook exportLateAppointments(AppointmentFilterDto filter);

	public CustomList<CaseOrgDto> getPatientHasNoAppointment(AppointmentFilterDto filter);

	public Workbook exportPatientHasNoAppointment(AppointmentFilterDto filter);

}
