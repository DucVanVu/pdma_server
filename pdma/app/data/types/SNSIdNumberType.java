package org.pepfar.pdma.app.data.types;

public enum SNSIdNumberType {
	// UNKNOW
	UNKNOWN(-1),
	// Chứng minh thư
	CMND(1),

	// Căn cước công dân
	CCCD(2),

	// Số thẻ bảo hiểm
	THE_BH(3),
	
	// Bằng lái xe
	BANG_LAI(4),
	
	// Số điện thoại
	SDT(5),
	// Sổ hộ khẩu
	HO_KHAU(6);

	private final int number;

	private SNSIdNumberType(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}
}
