package org.pepfar.pdma.app.scheduling;

import org.pepfar.pdma.app.data.service.CaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OPCAssistHouseKeeping {

	private static boolean executed = false;

	@Autowired
	private CaseService service;

	/**
	 * Run the first synthesis when starting the app
	 */
	@Scheduled(fixedRate = 360000, initialDelay = 5000)
	public void initiate() {
		if (executed) {
			return;
		}

		keepCaseEntityUp2Date();
		executed = true;
	}

	/**
	 * Run the next synthesis every evening at 23:00:00
	 */
	@Scheduled(cron = "0 0 23 * * ?")
	public void process() {
		keepCaseEntityUp2Date();
	}

	/**
	 * @formatter:off
	 * Keep the case entity data up to date, including: 
	 * 
	 * 1. The WHO stage and ARV treatment (ARV regimen) data. It's because for some reasons or others they
	 * are messed up.
	 * 2. Update new/old case status from weekly report 
	 * 
	 * @formatter:on
	 */
	private void keepCaseEntityUp2Date() {
		service.keepCaseEntityUp2Date();
	}
}
