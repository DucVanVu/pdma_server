package org.pepfar.pdma.app.data.types;

public enum TBProphylaxisStopReason {

	// Mắc lao
	CONFIRMED_TB(0),

	// Có thai
	PREGNANT(1),

	// Đồng nhiễm VGVR B/C
	HEP_COINFECTION(2),

	// Hết thuốc DPL
	DRUG_STOCKOUT(3),

	// Dị ứng
	ALLERGIC(4),

	// Men gan tăng
	HIGH_ALT(5),

	// Chuyển đi,
	TRANSFERRED_OUT(6),

	// Bỏ trị
	LTFU(7),

	// Tử vong
	DEAD(8),

	// Không tuân thủ
	BAD_ADHERENCE(9),

	// Bị bắt
	ARRESTED(10),

	// Khác (ghi rõ)
	OTHER(11),

	// Không có thông tin
	NO_INFORMATION(12);

	private final int number;

	private TBProphylaxisStopReason(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}
}
