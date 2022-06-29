package org.pepfar.pdma.app.data.types;

public enum TBScreeningType {

	//Sàng lọc lao dương tính khi đăng ký CSĐT
	upon_registration_of_the_treatment_facility(0),

	//Sàng lọc lao dương tính trong quá trình quản lý CSĐT
	during_the_management_of_the_Treatment_Facility(1),

	//BN nghi lao đến khám cơ sở lao (do cơ sở HIV chuyển hoặc BN tự đến)
	patients_with_suspected_TB_visit_a_TB_facility(2),

	//BN lao nhiễm HIV chuyển từ cơ sở lao đến
	HIV_infected_TB_patient_transferred_from_a_TB_facility(3);

	private final int number;

	private TBScreeningType(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}
}
