package org.pepfar.pdma.app.data.types;

public enum PEC131Result {
	UNKNOWN(-1,"Không rõ"),	
	answer1(1,"HIV âm tính"),
	answer2(2,"Khẳng định HIV dương tính"),
	answer3(3,"Không xác định"),
	answer4(4,"Không biết");
	private final int number;
	private final String description;

	private PEC131Result(int number,String description) {
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
