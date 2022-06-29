package org.pepfar.pdma.app.data.types;

public enum TBClassification3 {

	// Mới 
	MOI(0),

	// Tái phát
	TAI_PHAT(1),

	// Điều trị lại sau bỏ trị
	DIEUTRI_LAI(2),

	// Thất bại điều trị
	THAT_BAI_DIEUTRI(3),

	OTHER(4);

	private final int number;

	private TBClassification3(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}
}
