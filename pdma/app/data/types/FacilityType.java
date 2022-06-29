package org.pepfar.pdma.app.data.types;

public enum FacilityType {
	
	PUBLIC_FACILITY(0), // cơ sở y tế công

	PRIVATE_FACILITY(1), // cơ sở y tế tư nhân

	COMMUNITY(2), // nhóm cộng đồng (CBO, nhân viên tiếp cận cộng đồng)

	OTHER(3);

	private final int number;

	private FacilityType(int number) {
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
				ret = "Cơ sở y tế công";
				break;
			case 1:
				ret = "Cơ sở y tế tư nhân";
				break;
			case 2:
				ret = "Nhóm cộng đồng (CBO, nhân viên tiếp cận cộng đồng)";
				break;
			case 3:
				ret = "Loại cơ sở khác";
				break;
			default:
				break;
		}

		return ret;
	}
}
