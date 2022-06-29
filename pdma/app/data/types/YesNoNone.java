package org.pepfar.pdma.app.data.types;

public enum YesNoNone {

	YES(0),

	NO(1),

	NO_INFORMATION(2);

	private final int number;

	private YesNoNone(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}

}
