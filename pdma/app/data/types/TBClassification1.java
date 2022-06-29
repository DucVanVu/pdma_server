package org.pepfar.pdma.app.data.types;

public enum TBClassification1 {

	// Lao phổi
	LAO_PHOI(0),

	// Lao ngoài phổi
	LAO_NGOAIPHOI(1),

	// Lao phổi & ngoài phổi
	LAO_PHOI_NGOAIPHOI(2),

	// Khác (không rõ phân loại/Lâm sàng)
	OTHER(3);

	private final int number;

	private TBClassification1(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}
}
