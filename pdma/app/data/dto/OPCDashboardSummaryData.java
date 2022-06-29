package org.pepfar.pdma.app.data.dto;

public class OPCDashboardSummaryData {

	// Số bệnh nhân được hẹn khám hôm nay
	private int todayAppointments;

	// Số bệnh nhân muộn khám trên 84 ngày
	private int gt84LateDays;

	// Số bệnh nhân muộn khám trên 28 ngày
	private int gt28LateDays;

	// Số bệnh nhân được chuyển tới đang chờ tiếp nhận
	private int pendingEnrollments;

	// Số bệnh nhân chưa được phân nhóm nguy cơ
	private int riskAssessmentNeeded;

	// Số bệnh nhân đến lịch làm TLVR
	private int vlTestRequired;

	// Số xét nghiệm TLVR chưa có kết quả
	private int vlMissingResults;

	// Số xét nghiệm CD4 chưa có kết quả
	private int cd4MissingResults;

	public int getTodayAppointments() {
		return todayAppointments;
	}

	public void setTodayAppointments(int todayAppointments) {
		this.todayAppointments = todayAppointments;
	}

	public int getGt84LateDays() {
		return gt84LateDays;
	}

	public void setGt84LateDays(int gt84LateDays) {
		this.gt84LateDays = gt84LateDays;
	}

	public int getGt28LateDays() {
		return gt28LateDays;
	}

	public void setGt28LateDays(int gt28LateDays) {
		this.gt28LateDays = gt28LateDays;
	}

	public int getPendingEnrollments() {
		return pendingEnrollments;
	}

	public void setPendingEnrollments(int pendingEnrollments) {
		this.pendingEnrollments = pendingEnrollments;
	}

	public int getRiskAssessmentNeeded() {
		return riskAssessmentNeeded;
	}

	public void setRiskAssessmentNeeded(int riskAssessmentNeeded) {
		this.riskAssessmentNeeded = riskAssessmentNeeded;
	}

	public int getVlTestRequired() {
		return vlTestRequired;
	}

	public void setVlTestRequired(int vlTestRequired) {
		this.vlTestRequired = vlTestRequired;
	}

	public int getVlMissingResults() {
		return vlMissingResults;
	}

	public void setVlMissingResults(int vlMissingResults) {
		this.vlMissingResults = vlMissingResults;
	}

	public int getCd4MissingResults() {
		return cd4MissingResults;
	}

	public void setCd4MissingResults(int cd4MissingResults) {
		this.cd4MissingResults = cd4MissingResults;
	}

}
