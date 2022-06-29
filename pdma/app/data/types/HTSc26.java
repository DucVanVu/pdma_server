package org.pepfar.pdma.app.data.types;

public enum HTSc26 {
	// UNKNOW
	UNKNOWN(-1,"Không rõ"),	
	answer1(1,"Âm tính"),
	answer2(2,"Dương tính"),
	answer3(3,"Không làm xét nghiệm Giang mai");

	private final int number;
	private final String description;

	private HTSc26(int number,String description) {
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
