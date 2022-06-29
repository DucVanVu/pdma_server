package org.pepfar.pdma.app.data.types;

public enum ClinicalReferralType {

	OUTREACH_VCT(0),

	VCT_OPC(1),

	OPC_OPC(2),

	OPC_TB(3),

	TB_OPC(4),

	OTHER(5);

	private final int number;

	private ClinicalReferralType(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}
}
