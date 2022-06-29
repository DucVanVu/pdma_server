package org.pepfar.pdma.app.data.types;

public enum HTSc17 {
	// UNKNOW
	UNKNOWN(-1,"Không rõ"),	
	answer1(1,"Mới nhiễm HIV"),
	answer2(2,"Nhiễm HIV đã lâu"),
	answer3(3,"KH không đủ tiêu chuẩn làm XN nhiễm mới"),
	answer4(4,"KH từ chối không làm XN nhiễm mới"),
	answer5(5,"Cơ sở không thực hiện XN nhiễm mới");

	private final int number;
	private final String description;

	private HTSc17(int number,String description) {
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
