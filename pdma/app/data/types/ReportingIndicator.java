package org.pepfar.pdma.app.data.types;

public enum ReportingIndicator {

	HTS_TST(0),

	HTS_POS(1),

	TX_NEW(2);

	private final int number;

	private ReportingIndicator(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}
}
