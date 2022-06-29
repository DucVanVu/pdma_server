package org.pepfar.pdma.app.data.dto;

import java.time.LocalDateTime;

import org.pepfar.pdma.app.utils.CustomLocalDateTimeDeserializer2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class CaseReferralResultDto {

	private CaseDto theCase;

	/**
	 * 1 = Đã tới 2 = Đã hủy/mất dấu 3 = Đã tới cơ sở mới
	 */
	private int result;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime resultDate;

	private OrganizationDto newOrg;
	
	private CaseOrgDto currentCaseOrg;

	private String newOrgName;

	public CaseDto getTheCase() {
		return theCase;
	}

	public void setTheCase(CaseDto theCase) {
		this.theCase = theCase;
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public LocalDateTime getResultDate() {
		return resultDate;
	}

	public void setResultDate(LocalDateTime resultDate) {
		this.resultDate = resultDate;
	}

	public OrganizationDto getNewOrg() {
		return newOrg;
	}

	public void setNewOrg(OrganizationDto newOrg) {
		this.newOrg = newOrg;
	}

	public CaseOrgDto getCurrentCaseOrg() {
		return currentCaseOrg;
	}

	public void setCurrentCaseOrg(CaseOrgDto currentCaseOrg) {
		this.currentCaseOrg = currentCaseOrg;
	}

	public String getNewOrgName() {
		return newOrgName;
	}

	public void setNewOrgName(String newOrgName) {
		this.newOrgName = newOrgName;
	}

}
