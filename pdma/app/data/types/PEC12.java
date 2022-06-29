package org.pepfar.pdma.app.data.types;

public enum PEC12 {
	UNKNOWN(-1,"Không rõ"),	
	answer1(1,"Nhân viên cộng đồng thực hiện"),
	answer2(2,"Tự xét nghiệm HIV có hỗ trợ"),
	answer3(3,"Tự xét nghiệm HIV không có hỗ trợ"),
	answer4(4,"Cơ sở y tế thực hiện");

	private final int number;
	private final String description;

	private PEC12(int number,String description) {
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
