package org.pepfar.pdma.app.data.types;

public enum MaritalStatus {

	SINGLE(0),

	MARRIED(1),

	WIDOWED(2),

	UNKNOWN(3),

	DIVORCED(4);

	private final int number;

	private MaritalStatus(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}
}
