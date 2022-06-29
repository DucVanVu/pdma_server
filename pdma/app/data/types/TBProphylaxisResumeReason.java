package org.pepfar.pdma.app.data.types;

public enum TBProphylaxisResumeReason {

	// BN chuyển tới tiếp tục DPL
	TRANSFERRED_IN(0),

	// Có thuốc DPL trở lại
	DRUG_STOCKIN(1),

	// Hết tác dụng phụ của thuốc
	SIDE_EFFECT_FREE(2),

	// BN tuân thủ trở lại
	GOOD_ADHERENCE(3),

	// Khác (ghi rõ)
	OTHER(4),

	// Không có thông tin
	NO_INFORMATION(5);

	private final int number;

	private TBProphylaxisResumeReason(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}
}
