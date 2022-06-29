package org.pepfar.pdma.app.data.types;

public enum SNSApproachMethod {
	
	// Trực tiếp
	direct(1),

	// Trực tuyến
	online(2),
	unknown(-1);

	private final int number;

	private SNSApproachMethod(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}
}
