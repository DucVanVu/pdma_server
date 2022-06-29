package org.pepfar.pdma.app.data.types;

public enum PEC8 {
	UNKNOWN(-1,"Không rõ"),	
	answer1(1,"HIV dương tính"),
	answer2(2,"HIV âm tính trong lần XN gần nhất"),
	answer3(3,"Không biết");

	private final int number;
	private final String description;

	private PEC8(int number,String description) {
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
