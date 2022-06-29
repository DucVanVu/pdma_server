package org.pepfar.pdma.app.data.types;

public enum TBClassification4 {

	// VK (+)-Xpert
	VK_XPERT(0),

	// VK (+)-AFB
	VK_AFB(1),

	// VK (+)-Cấy
	VK_CAY(2),

	// VK (+)-LF LAM
	VK_LF_LAM(3),

	// VK (+) - Kháng thuốc
	VK_KHANGTHUOC(4),

	// Khác (VK (+) không cụ thể loại XN…)
	OTHER(5);

	private final int number;

	private TBClassification4(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}
}
