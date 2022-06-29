package org.pepfar.pdma.app.data.types;

public enum VitalStatus {

	ALIVE(0),

	DEAD(1),

	UNKNOWN(2);

	private final int number;

	private VitalStatus(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}
}
