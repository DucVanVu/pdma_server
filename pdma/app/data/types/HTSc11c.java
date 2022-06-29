package org.pepfar.pdma.app.data.types;

public enum HTSc11c {
	// UNKNOW
	UNKNOWN(-1,"Không rõ"),	
	answer1(1,"Tự làm xét nghiệm"),
	answer2(2,"Xét nghiệm do nhóm cộng đồng thực hiện"),
	answer3(3,"Xét nghiệm do y tế xã/phường thực hiện");

	private final int number;
	private final String description;

	private HTSc11c(int number,String description) {
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
