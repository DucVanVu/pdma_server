package org.pepfar.pdma.app.data.dto;

import org.pepfar.pdma.app.data.types.PatientStatus;

public class CaseOrgUpdateDto {

	// For updating patient status
	public PatientStatus targetStatus;

	// Used when self-updating patient status back to ACTIVE, we need to capture how
	// the patient left the origin facility (where OPC-Assist may not be used)
	public PatientStatus originStatus;

	public CaseOrgDto currentObj;

	public CaseOrgDto newObj;

	public PatientStatus getTargetStatus() {
		return targetStatus;
	}

	public void setTargetStatus(PatientStatus targetStatus) {
		this.targetStatus = targetStatus;
	}

	public PatientStatus getOriginStatus() {
		return originStatus;
	}

	public void setOriginStatus(PatientStatus originStatus) {
		this.originStatus = originStatus;
	}

	public CaseOrgDto getCurrentObj() {
		return currentObj;
	}

	public void setCurrentObj(CaseOrgDto currentObj) {
		this.currentObj = currentObj;
	}

	public CaseOrgDto getNewObj() {
		return newObj;
	}

	public void setNewObj(CaseOrgDto newObj) {
		this.newObj = newObj;
	}

}
