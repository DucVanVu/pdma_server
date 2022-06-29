package org.pepfar.pdma.app.data.types;

public enum PEC16 {
	UNKNOWN(-1,"Không rõ"),	
	answer1(1,"Khách hàng dương tính mới"),
	answer2(2,"Khách hàng dương tính cũ"),
	answer3(3,"Đang chờ kết quả xác minh");

	private final int number;
	private final String description;

	private PEC16(int number,String description) {
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
