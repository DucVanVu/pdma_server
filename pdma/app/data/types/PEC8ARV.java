package org.pepfar.pdma.app.data.types;

public enum PEC8ARV {
	UNKNOWN(-1,"Không rõ"),	
	answer1(1,"Đang điều trị ARV"),
	answer2(2,"Chưa từng điều trị ARV"),
	answer3(3,"Bỏ điều trị ARV");

	private final int number;
	private final String description;

	private PEC8ARV(int number,String description) {
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
