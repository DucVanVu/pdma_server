package org.pepfar.pdma.app.data.dto;

public class WRCaseFilterDto
{

	private OrganizationDto owner;

	private String hivConfirmId;

	private String patientChartId;

	private String keyword;

	private int pageIndex;

	private int pageSize;

	public OrganizationDto getOwner() {
		return owner;
	}

	public void setOwner(OrganizationDto owner) {
		this.owner = owner;
	}

	public String getHivConfirmId() {
		return hivConfirmId;
	}

	public void setHivConfirmId(String hivConfirmId) {
		this.hivConfirmId = hivConfirmId;
	}

	public String getPatientChartId() {
		return patientChartId;
	}

	public void setPatientChartId(String patientChartId) {
		this.patientChartId = patientChartId;
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

}
