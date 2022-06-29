package org.pepfar.pdma.app.data.types;

public enum HTSc11a {
	// UNKNOW
	UNKNOWN(-1,"Không rõ"),	
	answer1(1,"Dịch miệng"),
	answer2(2,"Máu mao mạch đầu ngón tay"),
	answer3(3,"Khác");

	private final int number;
	private final String description;

	private HTSc11a(int number,String description) {
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
