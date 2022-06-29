package org.pepfar.pdma.app.data.types;

public enum HIVStatus {
	// UNKNOW
		UNKNOWN(-1),
	// Dương tính
	positive(1),

	// Âm tính
	negative(2),
	// không xác định
	undefined(3),
	// không xét nghiệm
	notest(4);

	private final int number;

	private HIVStatus(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}
}
