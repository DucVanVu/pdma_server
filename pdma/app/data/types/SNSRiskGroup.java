package org.pepfar.pdma.app.data.types;

public enum SNSRiskGroup {
	// UNKNOW
	UNKNOWN(-1),
	// MSM
	MSM(1),

	// Bạn tình/bạn chích NCH
	NCH(2),

	// TCMT
	TCMT(3),
	
	// MD
	MD(4),
	
	// Khác
	OTHER(5);

	private final int number;

	private SNSRiskGroup(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}
}
