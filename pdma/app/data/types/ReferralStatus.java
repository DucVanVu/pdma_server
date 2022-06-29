package org.pepfar.pdma.app.data.types;

public enum ReferralStatus {

	PENDING_ARRIVAL(0),

	ARRIVED(1),

	CANCELLED(2);

	private final int number;

	private ReferralStatus(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}
}
