package org.pepfar.pdma.app.data.dto;

import java.time.LocalDateTime;

import org.pepfar.pdma.app.utils.CustomLocalDateTimeDeserializer2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class SelfTestFilterDto {

	private String keyword;

	private OrganizationDto organization;

	private StaffDto staff;

	private String specimen;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime dispensingDateFrom;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime dispensingDateTo;

	private int pageIndex;

	private int pageSize;

	private Long provinceId;

	private Boolean disablePaging;

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public OrganizationDto getOrganization() {
		return organization;
	}

	public void setOrganization(OrganizationDto organization) {
		this.organization = organization;
	}

	public StaffDto getStaff() {
		return staff;
	}

	public void setStaff(StaffDto staff) {
		this.staff = staff;
	}

	public String getSpecimen() {
		return specimen;
	}

	public void setSpecimen(String specimen) {
		this.specimen = specimen;
	}

	public LocalDateTime getDispensingDateFrom() {
		return dispensingDateFrom;
	}

	public void setDispensingDateFrom(LocalDateTime dispensingDateFrom) {
		this.dispensingDateFrom = dispensingDateFrom;
	}

	public LocalDateTime getDispensingDateTo() {
		return dispensingDateTo;
	}

	public void setDispensingDateTo(LocalDateTime dispensingDateTo) {
		this.dispensingDateTo = dispensingDateTo;
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

	public Long getProvinceId() {
		return provinceId;
	}

	public void setProvinceId(Long provinceId) {
		this.provinceId = provinceId;
	}

	public Boolean getDisablePaging() {
		return disablePaging;
	}

	public void setDisablePaging(Boolean disablePaging) {
		this.disablePaging = disablePaging;
	}
}
