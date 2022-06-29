package org.pepfar.pdma.app.data.types;

public enum Gender {

	MALE(0),

	FEMALE(1),

	OTHER(2),

	NOT_DISCLOSED(3),

	TRANSGENDER(4),

	// UNKNOWN
	UNKNOWN(5);

	private int number;

	Gender(int number) {
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
				ret = "Nam";
				break;
			case 1:
				ret = "Nữ";
				break;
			case 2:
				ret = "Khác";
				break;
			case 3:
				ret = "Không tiết lộ";
				break;
			case 4:
				ret = "Chuyển giới";
				break;
			default:
				break;
		}

		return ret;
	}
}
