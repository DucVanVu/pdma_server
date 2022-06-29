package org.pepfar.pdma.app.data.types;

public enum ARVFundingSource {

	SHI(0),

	PEPFAR(1),

	GF(2),

	NATIONAL(3),

	SELF(4),

	OTHER(5);

	private final int number;

	private ARVFundingSource(int number) {
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
				ret = "Bảo hiểm y tế";
				break;
			case 1:
				ret = "PEPFAR";
				break;
			case 2:
				ret = "Quỹ toàn cầu";
				break;
			case 3:
				ret = "Ngân sách nhà nước";
				break;
			case 4:
				ret = "Tự chi trả";
				break;
			case 5:
				ret = "Nguồn khác";
				break;
			default:
				break;
		}

		return ret;
	}
}
