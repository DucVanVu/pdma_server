package org.pepfar.pdma.app.data.types;

public enum PEApproachMethod {
	UNKNOWN(-1,"Không rõ"),	
	answer1(1,"Trực tiếp"),
	answer2(2,"Thông qua mạng xã hội (online)"),
	answer3(3,"Thông qua mạng lưới (PDI)"),
	answer4(4,"Người có HIV"),
	answer5(5,"Chưa tiếp cận được");

	private final int number;
	private final String description;

	private PEApproachMethod(int number,String description) {
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
