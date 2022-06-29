package org.pepfar.pdma.app.data.types;

public enum PNSSucceededMethod {
	// UNKNOW
	UNKNOWN(-1,"Không rõ"),	
	answer1(1,"Người có HIV	"),
	answer2(2,"Thảo thuận thực hiện"),
	answer3(3,"Cùng thực hiện"),
	answer4(4,"Nhân viên y tế");

	private final int number;
	private final String description;

	private PNSSucceededMethod(int number,String description) {
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
