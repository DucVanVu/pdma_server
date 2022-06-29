package org.pepfar.pdma.app.data.types;

public enum HTSc10 {
	// UNKNOW
	UNKNOWN(-1,"Không rõ"),
	// Khách hàng tự đến
	answer1(1,"Khách hàng tự đến"),
	// Dịch vụ TBXNBT/BC của người có HIV
	answer2(2,"Dịch vụ TBXNBT/BC của người có HIV"),
	// Chương trình tiếp cận cộng đồng
	answer3(3,"Chương trình tiếp cận cộng đồng"),
	// Khác
	answer4(4,"Khác");

	private final int number;
	private final String description;

	private HTSc10(int number,String description) {
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
