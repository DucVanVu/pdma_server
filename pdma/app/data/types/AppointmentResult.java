package org.pepfar.pdma.app.data.types;

public enum AppointmentResult {

	HAS_APPOINTMENT(0),

	HAS_VL_TEST(1),
	
	HAS_CD4_TEST(2),
	
	HAS_ARV_DR_TEST(3),

	NOT_ARRIVED(4);

	private final int number;

	private AppointmentResult(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}

	@Override
	public String toString() {
		String ret = "";
		switch (this.number) {
			case 0:
				ret = "Bệnh nhân có hẹn khám trong tháng";
				break;
			case 1:
				ret = "Bệnh nhân được làm TLVR trong tháng";
				break;
			case 2:
				ret = "Bệnh nhân được làm CD4 trong tháng";
				break;
			case 3:
				ret = "Bệnh nhân được làm kháng thuốc ARV trong tháng";
				break;
			case 4:
				ret = "Bệnh nhân chưa tới khám";
				break;
			default:
				break;
		}

		return ret;
	}
}
