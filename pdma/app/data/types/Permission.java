package org.pepfar.pdma.app.data.types;

public enum Permission {
	
	READ_ACCESS(0),

	WRITE_ACCESS(1),

	DELETE_ACCESS(2),

	PE_ACCESS(3),

	HTS_ACCESS(4),

	PNS_ACCESS(5),

	SNS_ACCESS(6),

	SELFTEST_ACCESS(7);

	private final int number;

	private Permission(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}

}
