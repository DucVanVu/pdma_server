package org.pepfar.pdma.app.data.types;

public enum PNSSucceededContactMethod {
	// UNKNOW
	UNKNOWN(-1,"Không rõ"),	
	answer1(1,"Gọi điện thoại"),
	answer2(2,"Gửi tin nhắn"),
	answer3(3,"Internet (Facebook, Viber, Zalo, Email,...)"),
	answer4(4,"Gặp trực tiếp"),
	answer5(5,"Khác");

	private final int number;
	private final String description;

	private PNSSucceededContactMethod(int number,String description) {
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
