package org.pepfar.pdma.app.data.types;

public enum TBClassification2 {

	// Lao hạch
	LAO_HACH(0),

	// Lao màng phổi
	LAO_MANGPHOI(1),

	// Lao màng bụng
	LAO_MANGBUNG(2),

	// Lao màng não
	LAO_MANGNAO(3),

	// Lao đa màng
	LAO_DAMANG(4),

	// Lao ruột
	LAO_RUOT(5),

	// Lao xương/khớp, Lao gan, Lao lách, Lao kê, Khác
	LAO_XUONG(6),

	// Lao gan
	LAO_GAN(7),

	// Lao lách
	LAO_LACH(8),

	// Lao kê
	LAO_KE(9),

	OTHER(10);

	private final int number;

	private TBClassification2(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}
}
