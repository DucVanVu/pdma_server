package org.pepfar.pdma.app.data.repository;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.pepfar.pdma.app.data.domain.Appointment;
import org.pepfar.pdma.app.data.domain.CaseOrg;
import org.pepfar.pdma.app.data.dto.AppointmentCalendarEventDto;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AppointmentRepository
		extends PagingAndSortingRepository<Appointment, Serializable>, QueryDslPredicateExecutor<Appointment> {

	@Query("SELECT a FROM Appointment a WHERE (:startDate IS NULL OR a.appointmentDate >= :startDate) AND (:endDate IS NULL OR a.appointmentDate <= :endDate) AND a.theCase.id = :caseId AND a.organization.id = :orgId")
	public List<Appointment> findAppointmentsInDateRange(@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate, @Param("caseId") Long caseId, @Param("orgId") Long orgId);

	@Query("SELECT a.appointmentDate FROM Appointment a WHERE a.arrived = TRUE AND a.theCase.id = :caseId and a.organization.id = :orgId ORDER BY a.appointmentDate DESC")
	public List<LocalDateTime> findLatestArrivedAppointments(@Param("caseId") Long caseId, @Param("orgId") Long orgId);

	@Query("SELECT a FROM Appointment a WHERE a.arrived = TRUE AND a.theCase.id = :caseId and a.organization.id = :orgId AND a.arrivalDate <= :cutpoint ORDER BY a.appointmentDate DESC")
	public List<Appointment> findLatestArrivedAppointmentByCutpoint(@Param("caseId") Long caseId,
			@Param("orgId") Long orgId, @Param("cutpoint") LocalDateTime cutpoint);

	@Query("SELECT a FROM Appointment a WHERE a.arrived = TRUE AND a.theCase.id = :caseId and a.organization.id = :orgId AND a.arrivalDate BETWEEN :startDate AND :endDate ORDER BY a.arrivalDate ASC")
	public List<Appointment> findEarliestArrivedAppointmentInDateRange(@Param("caseId") Long caseId,
			@Param("orgId") Long orgId, @Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate);

	@Query("SELECT new org.pepfar.pdma.app.data.dto.AppointmentCalendarEventDto(a.arrived, COUNT(a.id), a.appointmentDate) FROM Appointment a JOIN a.theCase c JOIN c.caseOrgs co WHERE c.deleted = FALSE AND co.current = TRUE AND co.latestRelationship = TRUE AND co.refTrackingOnly = FALSE AND co.organization.id = :organizationId AND a.appointmentDate >= :startDate AND a.appointmentDate <= :endDate AND a.organization.id = :organizationId GROUP BY DATE(a.appointmentDate), a.arrived")
	public List<AppointmentCalendarEventDto> getCalendarEvents(@Param("startDate") LocalDateTime from,
			@Param("endDate") LocalDateTime to, @Param("organizationId") long orgId);

	@Modifying
	@Transactional(rollbackFor = Exception.class)
	@Query(value = "UPDATE tbl_appointment SET missed = TRUE WHERE arrival_date IS NULL AND TIMESTAMPDIFF(DAY, appointment_date, NOW()) >= 28 AND (CASE WHEN :caseId IS NOT NULL THEN case_id = :caseId ELSE TRUE END) AND (CASE WHEN :organizationId IS NOT NULL THEN org_id = :organizationId ELSE TRUE END)", nativeQuery = true)
	public void updateMissedAppointment(@Param("caseId") Long caseId, @Param("organizationId") Long orgId);

	@Query(nativeQuery = true)
	public Number countLateDays(@Param("caseId") Long caseId, @Param("orgId") Long orgId);

	@Query(nativeQuery = true)
	public Number countAppointments4Date(@Param("organizationId") Long organizationId, @Param("keyword") String keyword,
			@Param("fromDate") Timestamp fromDate, @Param("toDate") Timestamp toDate);

	@Query(nativeQuery = true)
	public List<Appointment> findAppointments4Date(@Param("organizationId") Long organizationId,
			@Param("keyword") String keyword, @Param("sortField") int sortField, @Param("fromDate") Timestamp fromDate,
			@Param("toDate") Timestamp toDate);

	@Query(nativeQuery = true)
	public Number countLateAppointments(@Param("organizationId") Long organizationId, @Param("keyword") String keyword,
			@Param("lateDays") int lateDays, @Param("cutpoint") Timestamp cutpoint);

	@Query(nativeQuery = true)
	public List<Appointment> findLateAppointments(@Param("organizationId") Long organizationId,
			@Param("keyword") String keyword, @Param("sortField") int sortField, @Param("lateDays") int lateDays,
			@Param("cutpoint") Timestamp cutpoint, @Param("pageIndex") int pageInde, @Param("pageSize") int pageSize);

	@Query(nativeQuery = true)
	public List<Appointment> findListLateAppointments(@Param("organizationId") Long organizationId,
			@Param("keyword") String keyword, @Param("sortField") int sortField, @Param("lateDays") int lateDays,
			@Param("cutpoint") Timestamp cutpoint);

	//// ------------------------------------
	//// BEGIN: Query for OPC Dashboard
	//// ------------------------------------
	@Query(nativeQuery = true)
	public Number countAppointments4TxDashboard(@Param("organizationIds") List<Long> organizationIds,
			@Param("startTime") Timestamp startTime, @Param("endTime") Timestamp endTime);

	@Query(nativeQuery = true)
	public Number countLateAppointments4TxDashboard(@Param("organizationIds") List<Long> organizationIds,
			@Param("lateDays") int lateDays);
	//// ------------------------------------
	//// END: Query for OPC Dashboard
	//// ------------------------------------

	@Query("SELECT a.appointmentDate FROM Appointment a WHERE a.arrived = TRUE AND a.theCase.id = :caseId AND a.tbScreenResult=:tbScreenResult ORDER BY a.appointmentDate DESC")
	public List<LocalDateTime> findLatestArrivedAppointmentsByTbScreenResult(@Param("caseId") Long caseId,
			@Param("tbScreenResult") Integer tbScreenResult);

	@Query(nativeQuery = true)
	public List<Appointment> findNextAppointments4Date(@Param("organizationId") Long organizationId,
			@Param("keyword") String keyword, @Param("sortField") int sortField, @Param("fromDate") Timestamp fromDate,
			@Param("toDate") Timestamp toDate);

	@Query(nativeQuery = true)
	public List<CaseOrg> findPatientHasNoAppointment(@Param("organizationId") Long organizationId,
			@Param("keyword") String keyword, @Param("sortField") int sortField, @Param("pageIndex") int pageIndex, @Param("pageSize") int pageSize);

	@Query(nativeQuery = true)
	public Number countPatientHasNoAppointment(@Param("organizationId") Long organizationId, @Param("keyword") String keyword);
}