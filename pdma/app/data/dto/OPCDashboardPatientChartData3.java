package org.pepfar.pdma.app.data.dto;

public class OPCDashboardPatientChartData3 {

	private String month;

	private int activePatientCount;

	private int newlyEnrolledCount;

	private int transInCount;

	private int returnedCount;

	private int transedOutCount;

	private int ltfuCount;

	private int deadCount;

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public int getActivePatientCount() {
		return activePatientCount;
	}

	public void setActivePatientCount(int activePatientCount) {
		this.activePatientCount = activePatientCount;
	}

	public int getNewlyEnrolledCount() {
		return newlyEnrolledCount;
	}

	public void setNewlyEnrolledCount(int newlyEnrolledCount) {
		this.newlyEnrolledCount = newlyEnrolledCount;
	}

	public int getTransInCount() {
		return transInCount;
	}

	public void setTransInCount(int transInCount) {
		this.transInCount = transInCount;
	}

	public int getReturnedCount() {
		return returnedCount;
	}

	public void setReturnedCount(int returnedCount) {
		this.returnedCount = returnedCount;
	}

	public int getTransedOutCount() {
		return transedOutCount;
	}

	public void setTransedOutCount(int transedOutCount) {
		this.transedOutCount = transedOutCount;
	}

	public int getLtfuCount() {
		return ltfuCount;
	}

	public void setLtfuCount(int ltfuCount) {
		this.ltfuCount = ltfuCount;
	}

	public int getDeadCount() {
		return deadCount;
	}

	public void setDeadCount(int deadCount) {
		this.deadCount = deadCount;
	}

}
