package org.pepfar.pdma.app.data.types;

public enum PNSc5Reason {
	// UNKNOW
	UNKNOWN(-1,"Không rõ"),	
	answer1(1,"Không ở địa phương"),
	answer2(2,"Đã tử vong"),
	answer3(3,"Không liên lạc được, từ chối");

	private final int number;
	private final String description;

	private PNSc5Reason(int number,String description) {
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
