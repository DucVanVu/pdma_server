package org.pepfar.pdma.app.data.types;

public enum SelfTestSource {
	answer1(1,"Nhân viên cộng đồng thực hiện"),
	answer2(2,"Cơ sở y tế thực hiện");

	private final int number;
	private final String description;

	private SelfTestSource(int number, String description) {
		this.number = number;
		this.description = description;
	}

	public int getNumber() {
		return number;
	}

	public String getDescription() {
		return description;
	}
}
