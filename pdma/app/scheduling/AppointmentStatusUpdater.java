package org.pepfar.pdma.app.scheduling;

import org.pepfar.pdma.app.data.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AppointmentStatusUpdater {

	private static boolean executed = false;

	@Autowired
	private AppointmentService service;

	@Scheduled(fixedRate = 360000, initialDelay = 5000)
	public void initiate() {
		if (executed) {
			return;
		}

		service.updateMissedAppointments(null, null);
		executed = true;
	}

	/**
	 * At 01:00:00 in the morning every day, run the house-keeping job to scan for
	 * appointments that is late >= 28 days
	 */
	@Scheduled(cron = "0 0 1 * * ?")
	public void markMissedAppointments() {
		service.updateMissedAppointments(null, null);
	}
}
