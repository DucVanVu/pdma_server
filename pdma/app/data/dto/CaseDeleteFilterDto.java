package org.pepfar.pdma.app.data.dto;

/**
 * For deletion of cases by organization, used as a away to prevent other Admin
 * user from accidentally delete data
 * 
 * @author bizic
 *
 */
public class CaseDeleteFilterDto {

	public Long orgId;

	public String passcode;

	public Long getOrgId() {
		return orgId;
	}

	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}

	public String getPasscode() {
		return passcode;
	}

	public void setPasscode(String passcode) {
		this.passcode = passcode;
	}

}
