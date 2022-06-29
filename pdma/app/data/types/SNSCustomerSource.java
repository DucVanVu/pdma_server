package org.pepfar.pdma.app.data.types;

public enum SNSCustomerSource {
	// UNKNOW
	UNKNOWN(-1),
	
	// SNS
	SNS(1),

	// VCT_OPC
	VCT_OPC(2),

	// CBOs
	CBOs(3),
		
	// OTHER
	OTHER(4);
	
	private final int number;

	private SNSCustomerSource(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}
}
