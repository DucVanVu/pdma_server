package org.pepfar.pdma.app.data.dto;

import org.pepfar.pdma.app.data.types.ReportingIndicator;

public class SLTargetFilterDto
{

	private AdminUnitDto province;

	private int fiscalYear;

	private ReportingIndicator indicator;

	private OrganizationDto[] sites;

	private String keyword;

	private int pageIndex;

	private int pageSize;

	public int getFiscalYear() {
		return fiscalYear;
	}

	public void setFiscalYear(int fiscalYear) {
		this.fiscalYear = fiscalYear;
	}

	public ReportingIndicator getIndicator() {
		return indicator;
	}

	public void setIndicator(ReportingIndicator indicator) {
		this.indicator = indicator;
	}

	public OrganizationDto[] getSites() {
		return sites;
	}

	public void setSites(OrganizationDto[] sites) {
		this.sites = sites;
	}

	public AdminUnitDto getProvince() {
		return province;
	}

	public void setProvince(AdminUnitDto province) {
		this.province = province;
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
