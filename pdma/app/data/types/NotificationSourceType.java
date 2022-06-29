package org.pepfar.pdma.app.data.types;

public enum NotificationSourceType {

	WEEKLY_REPORT_COMMENT(0), // district/province/national comment -> notify site

	WEEKLY_REPORT_UPDATED(1), // updated by district/province -> notify site
	
	WEEKLY_REPORT_APPROVED(2), // approved by province -> notify site
	
	WEEKLY_REPORT_RETURNED(3), // returned -> notify site
	
	PDMA_VERSION_CHANGED(4), // new version of PDMA -> notify all users

	NEW_ANNOUNCEMENT(5), // new announcement -> notify all users

	NEW_DOCUMENT(6); // new document added -> notify all users

	private final int number;

	private NotificationSourceType(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}
}
