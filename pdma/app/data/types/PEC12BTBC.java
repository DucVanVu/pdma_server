package org.pepfar.pdma.app.data.types;

public enum PEC12BTBC {
	UNKNOWN(-1,"Không rõ"),	
	answer1(1,"Bản thân"),
	answer2(2,"Bạn tình"),
	answer3(3,"Bạn chích");

	private final int number;
	private final String description;

	private PEC12BTBC(int number,String description) {
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
