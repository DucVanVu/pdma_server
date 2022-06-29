package org.pepfar.pdma.app.scheduling;

import org.pepfar.pdma.app.data.service.WeeklyReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WeeklyReportSynthesizerJob {

	private static boolean executed = false;

	@Autowired
	private WeeklyReportService wrService;

	/**
	 * Run the first synthesis when starting the app
	 */
	@Scheduled(fixedRate = 360000, initialDelay = 5000)
	public void initiate() {
		if (executed) {
			return;
		}

		synthesize();
		executed = true;
	}

	/**
	 * Run the next synthesis every evening at 23:00:00
	 */
	@Scheduled(cron = "0 0 23 * * ?")
	public void process() {
		synthesize();
	}

	private void synthesize() {
		System.out.println("Started: weekly report synthesis.");
		wrService.synthesize(false);
		System.out.println("Completed: weekly report synthesis.");
	}
}
