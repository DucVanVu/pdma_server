package org.pepfar.pdma.app.data.types;

public enum HTSc11b {
	// UNKNOW
	UNKNOWN(-1,"Không rõ"),	
	answer1(1,"Tự làm xét nghiệm"),
	answer2(2,"XN do nhóm cộng đồng thực hiện bao gồm tự XN có hỗ trợ"),
	answer3(3,"Xét nghiệm do y tế xã/phường thực hiện");

	private final int number;
	private final String description;

	private HTSc11b(int number,String description) {
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
