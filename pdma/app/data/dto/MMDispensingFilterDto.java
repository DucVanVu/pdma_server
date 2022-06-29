package org.pepfar.pdma.app.data.dto;

import java.time.LocalDateTime;

import org.pepfar.pdma.app.utils.CustomLocalDateTimeDeserializer2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class MMDispensingFilterDto {

	private int pageIndex;

	private int pageSize;

	private OrganizationDto organization;

	private CaseDto theCase;

	private String keyword;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime startMmdFrom;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime startMmdTo;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime cutpoint;

	private boolean includeDeleted;

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

	public OrganizationDto getOrganization() {
		return organization;
	}

	public void setOrganization(OrganizationDto organization) {
		this.organization = organization;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public LocalDateTime getStartMmdFrom() {
		return startMmdFrom;
	}

	public void setStartMmdFrom(LocalDateTime startMmdFrom) {
		this.startMmdFrom = startMmdFrom;
	}

	public LocalDateTime getStartMmdTo() {
		return startMmdTo;
	}

	public void setStartMmdTo(LocalDateTime startMmdTo) {
		this.startMmdTo = startMmdTo;
	}

	public LocalDateTime getCutpoint() {
		return cutpoint;
	}

	public void setCutpoint(LocalDateTime cutpoint) {
		this.cutpoint = cutpoint;
	}

	public CaseDto getTheCase() {
		return theCase;
	}

	public void setTheCase(CaseDto theCase) {
		this.theCase = theCase;
	}

	public boolean isIncludeDeleted() {
		return includeDeleted;
	}

	public void setIncludeDeleted(boolean includeDeleted) {
		this.includeDeleted = includeDeleted;
	}

}
