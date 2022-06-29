package org.pepfar.pdma.app.scheduling;

import org.pepfar.pdma.app.data.service.WeeklyReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WeeklyReportHouseKeeping {

	@Autowired
	private WeeklyReportService wrService;

	/**
	 * Run the next adjustment every evening at 03:00:00 AM
	 */
	@Scheduled(cron = "0 0 3 * * ?")
	public void process() {
		adjustReportingPeriod();
	}

	private void adjustReportingPeriod() {
		System.out.println("Started: weekly report period adjustment.");
		wrService.adjustReportingPeriod();
		System.out.println("Completed: weekly report period adjustment.");
	}

}
