package org.pepfar.pdma.app.data.types;

public enum TBExtraDiagnosis {

	// Hạch đồ,
	HACH_DO(0),

	// Cấy đờm
	SPUTUM_CULTURE(1),

	// Soi/cấy dịch màng bụng
	SOICAY_DICH_MANG_BUNG(2),

	// Soi/cấy dịch màng phổi
	SOICAY_DICH_MANG_PHOI(3),

	// Soi/cấy dịch màng não
	SOICAY_DICH_MANG_LAO(4),

	// TB-LAM
	TB_LAM(5),

	OTHER(6),

	NO_INFORMATION(7);

	private final int number;

	private TBExtraDiagnosis(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}

}
