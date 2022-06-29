package org.pepfar.pdma.app.data.dto;

import org.pepfar.pdma.app.data.types.EnrollmentType;

public class CaseWithEnrollmentTypeDto {

	private Long caseId;

	private EnrollmentType enrollmentType;

	public CaseWithEnrollmentTypeDto() {

	}

	public CaseWithEnrollmentTypeDto(Long caseId, EnrollmentType enrollmentType) {
		this.caseId = caseId;
		this.enrollmentType = enrollmentType;
	}

	public Long getCaseId() {
		return caseId;
	}

	public void setCaseId(Long caseId) {
		this.caseId = caseId;
	}

	public EnrollmentType getEnrollmentType() {
		return enrollmentType;
	}

	public void setEnrollmentType(EnrollmentType enrollmentType) {
		this.enrollmentType = enrollmentType;
	}

}
