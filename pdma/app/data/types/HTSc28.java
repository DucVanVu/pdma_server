package org.pepfar.pdma.app.data.types;

public enum HTSc28 {
	
	UNKNOWN(-1,"Không rõ"),	
	answer1(1,"Để XN HIV"),
	answer2(2,"Để XN giang mai"),
	answer3(3,"Để XN cả HIV và giang mai"),
	answer4(4,"Do dịch vụ PrEP chuyển gửi XN");

	private final int number;
	private final String description;

	private HTSc28(int number,String description) {
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
