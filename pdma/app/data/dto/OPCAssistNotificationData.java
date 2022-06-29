package org.pepfar.pdma.app.data.dto;

public class OPCAssistNotificationData {

	private long pendingEnrollmentCount;

	private long todayAppointmentCount;

	public long getPendingEnrollmentCount() {
		return pendingEnrollmentCount;
	}

	public void setPendingEnrollmentCount(long pendingEnrollmentCount) {
		this.pendingEnrollmentCount = pendingEnrollmentCount;
	}

	public long getTodayAppointmentCount() {
		return todayAppointmentCount;
	}

	public void setTodayAppointmentCount(long todayAppointmentCount) {
		this.todayAppointmentCount = todayAppointmentCount;
	}

}
