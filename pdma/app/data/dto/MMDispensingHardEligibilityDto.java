package org.pepfar.pdma.app.data.dto;

public class MMDispensingHardEligibilityDto {

	private boolean adult;

	private boolean vlLt200CD4Ge500;

	private boolean onARVGt12Months;

	public boolean isAdult() {
		return adult;
	}

	public void setAdult(boolean adult) {
		this.adult = adult;
	}

	public boolean isVlLt200CD4Ge500() {
		return vlLt200CD4Ge500;
	}

	public void setVlLt200CD4Ge500(boolean vlLt200CD4Ge500) {
		this.vlLt200CD4Ge500 = vlLt200CD4Ge500;
	}

	public boolean isOnARVGt12Months() {
		return onARVGt12Months;
	}

	public void setOnARVGt12Months(boolean onARVGt12Months) {
		this.onARVGt12Months = onARVGt12Months;
	}

}
