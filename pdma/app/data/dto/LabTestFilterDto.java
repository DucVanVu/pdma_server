package org.pepfar.pdma.app.data.dto;

import java.time.LocalDateTime;

import org.pepfar.pdma.app.data.types.ClinicalTestingType;
import org.pepfar.pdma.app.utils.CustomLocalDateTimeDeserializer2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class LabTestFilterDto {

	private String keyword;

	private int pageIndex;

	private int pageSize;

	private OrganizationDto organization;

	private CaseDto theCase;

	// for scheduling
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime cutpoint;

	// for scheduling
	private boolean checkTestExistance;

	private ClinicalTestingType testType;

	private ClinicalTestingType[] testTypes;

	// for reporting
	// 0 = PEPFAR VIRAL LOAD
	// 1 = VAAC VIRAL LOAD - MONTHLY
	// 2 = VAAC VIRAL LOAD - QUARTERLY
	// 3 = VAAC VIRAL LOAD - ANNUALLY
	// 4 = List of patients requiring VL test in a specified month
	private int reportType;

	private Long provinceId;

	// For getting a list of tests that don't have results
	private boolean noResultOnly;

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

	public CaseDto getTheCase() {
		return theCase;
	}

	public void setTheCase(CaseDto theCase) {
		this.theCase = theCase;
	}

	public ClinicalTestingType getTestType() {
		return testType;
	}

	public void setTestType(ClinicalTestingType testType) {
		this.testType = testType;
	}

	public ClinicalTestingType[] getTestTypes() {
		return testTypes;
	}

	public void setTestTypes(ClinicalTestingType[] testTypes) {
		this.testTypes = testTypes;
	}

	public int getReportType() {
		return reportType;
	}

	public void setReportType(int reportType) {
		this.reportType = reportType;
	}

	public Long getProvinceId() {
		return provinceId;
	}

	public void setProvinceId(Long provinceId) {
		this.provinceId = provinceId;
	}

	public OrganizationDto getOrganization() {
		return organization;
	}

	public void setOrganization(OrganizationDto organization) {
		this.organization = organization;
	}

	public LocalDateTime getCutpoint() {
		return cutpoint;
	}

	public void setCutpoint(LocalDateTime cutpoint) {
		this.cutpoint = cutpoint;
	}

	public boolean isCheckTestExistance() {
		return checkTestExistance;
	}

	public void setCheckTestExistance(boolean checkTestExistance) {
		this.checkTestExistance = checkTestExistance;
	}

	public boolean isNoResultOnly() {
		return noResultOnly;
	}

	public void setNoResultOnly(boolean noResultOnly) {
		this.noResultOnly = noResultOnly;
	}

}
