package org.pepfar.pdma.app.data.dto;

import org.pepfar.pdma.app.data.types.NotificationSourceType;

public class WRCommentFilterDto
{

	private WeeklyReportDto report;

	private String keyword;

	private int pageIndex;

	private int pageSize;

	private NotificationSourceType type;

	public WeeklyReportDto getReport() {
		return report;
	}

	public void setReport(WeeklyReportDto report) {
		this.report = report;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public NotificationSourceType getType() {
		return type;
	}

	public void setType(NotificationSourceType type) {
		this.type = type;
	}

}
