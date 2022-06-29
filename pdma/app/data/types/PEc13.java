package org.pepfar.pdma.app.data.types;

public enum PEc13 {
	UNKNOWN(-1,"Không rõ"),	
	answer1(1,"Có phản ứng HIV dương tính"),
	answer2(2,"HIV âm tính"),
	answer3(3,"Không xác định"),
	answer4(4,"Không biết");

	private final int number;
	private final String description;

	private PEc13(int number,String description) {
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
