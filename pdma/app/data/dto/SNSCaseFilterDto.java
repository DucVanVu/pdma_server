package org.pepfar.pdma.app.data.dto;

import java.time.LocalDateTime;
import java.util.List;

import org.pepfar.pdma.app.data.types.HIVStatus;
import org.pepfar.pdma.app.data.types.ReportingIndicator;
import org.pepfar.pdma.app.data.types.SNSApproachMethod;
import org.pepfar.pdma.app.data.types.SNSCustomerSource;
import org.pepfar.pdma.app.data.types.SNSRiskGroup;
import org.pepfar.pdma.app.utils.CustomLocalDateTimeDeserializer2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class SNSCaseFilterDto
{
	private String name;

	private String couponCode;

	private String keyword;

	private int pageIndex;

	private int pageSize;
	
	private List<SNSRiskGroup> riskGroup;

	private List<HIVStatus> hivStatus;
	
	private List<SNSCustomerSource> customerSource;
	
	private List<SNSApproachMethod> approachMethod;
	
	private Integer fromYear;
	
	private Integer toYear;
	
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime prepDateFrom;
	
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime prepDateTo;
	
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime arvDateFrom;
	
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime arvDateTo;
	
	private List<Long> orgIds;
	
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime fromDate;
	
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime toDate;
	
	private UserDto user;
	
	private Boolean disablePaging;
	
	private Boolean isFullDto;
	
	private Long provinceId;
	
	
	
	public Long getProvinceId() {
		return provinceId;
	}

	public void setProvinceId(Long provinceId) {
		this.provinceId = provinceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCouponCode() {
		return couponCode;
	}

	public void setCouponCode(String couponCode) {
		this.couponCode = couponCode;
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

	public List<SNSRiskGroup> getRiskGroup() {
		return riskGroup;
	}

	public void setRiskGroup(List<SNSRiskGroup> riskGroup) {
		this.riskGroup = riskGroup;
	}

	public List<HIVStatus> getHivStatus() {
		return hivStatus;
	}

	public void setHivStatus(List<HIVStatus> hivStatus) {
		this.hivStatus = hivStatus;
	}

	public List<SNSCustomerSource> getCustomerSource() {
		return customerSource;
	}

	public void setCustomerSource(List<SNSCustomerSource> customerSource) {
		this.customerSource = customerSource;
	}

	public List<SNSApproachMethod> getApproachMethod() {
		return approachMethod;
	}

	public void setApproachMethod(List<SNSApproachMethod> approachMethod) {
		this.approachMethod = approachMethod;
	}

	public Integer getFromYear() {
		return fromYear;
	}

	public void setFromYear(Integer fromYear) {
		this.fromYear = fromYear;
	}

	public Integer getToYear() {
		return toYear;
	}

	public void setToYear(Integer toYear) {
		this.toYear = toYear;
	}

	public LocalDateTime getPrepDateFrom() {
		return prepDateFrom;
	}

	public void setPrepDateFrom(LocalDateTime prepDateFrom) {
		this.prepDateFrom = prepDateFrom;
	}

	public LocalDateTime getPrepDateTo() {
		return prepDateTo;
	}

	public void setPrepDateTo(LocalDateTime prepDateTo) {
		this.prepDateTo = prepDateTo;
	}

	public LocalDateTime getArvDateFrom() {
		return arvDateFrom;
	}

	public void setArvDateFrom(LocalDateTime arvDateFrom) {
		this.arvDateFrom = arvDateFrom;
	}

	public LocalDateTime getArvDateTo() {
		return arvDateTo;
	}

	public void setArvDateTo(LocalDateTime arvDateTo) {
		this.arvDateTo = arvDateTo;
	}

	public UserDto getUser() {
		return user;
	}

	public void setUser(UserDto user) {
		this.user = user;
	}

	public List<Long> getOrgIds() {
		return orgIds;
	}

	public void setOrgIds(List<Long> orgIds) {
		this.orgIds = orgIds;
	}
	
	public LocalDateTime getFromDate() {
		return fromDate;
	}

	public void setFromDate(LocalDateTime fromDate) {
		this.fromDate = fromDate;
	}

	public LocalDateTime getToDate() {
		return toDate;
	}

	public void setToDate(LocalDateTime toDate) {
		this.toDate = toDate;
	}

	public Boolean getDisablePaging() {
		return disablePaging;
	}

	public void setDisablePaging(Boolean disablePaging) {
		this.disablePaging = disablePaging;
	}

	public Boolean getIsFullDto() {
		return isFullDto;
	}

	public void setIsFullDto(Boolean isFullDto) {
		this.isFullDto = isFullDto;
	}
}
