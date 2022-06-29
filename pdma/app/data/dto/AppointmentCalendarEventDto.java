package org.pepfar.pdma.app.data.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class AppointmentCalendarEventDto {

	private Boolean arrived;

	private Long count;

	@JsonSerialize(using = ToStringSerializer.class)
	private LocalDateTime appointmentDate;

	public AppointmentCalendarEventDto() {

	}

	public AppointmentCalendarEventDto(Boolean arrived, Long count, LocalDateTime appointmentDate) {
		this.arrived = arrived;
		this.count = count;
		this.appointmentDate = appointmentDate;
	}

	public Boolean isArrived() {
		return arrived;
	}

	public void setArrived(Boolean arrived) {
		this.arrived = arrived;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	public LocalDateTime getAppointmentDate() {
		return appointmentDate;
	}

	public void setAppointmentDate(LocalDateTime appointmentDate) {
		this.appointmentDate = appointmentDate;
	}

}
