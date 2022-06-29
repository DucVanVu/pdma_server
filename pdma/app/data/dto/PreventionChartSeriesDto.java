package org.pepfar.pdma.app.data.dto;

import java.math.BigInteger;

public class PreventionChartSeriesDto {
	private String category;
	private BigInteger firstQuantity = BigInteger.valueOf(0);
	private BigInteger secondQuantity = BigInteger.valueOf(0);
	private BigInteger thirdQuantity = BigInteger.valueOf(0);
	private BigInteger fourQuantity = BigInteger.valueOf(0);
	private BigInteger fiveQuantity = BigInteger.valueOf(0);
	private BigInteger sixQuantity = BigInteger.valueOf(0);
	private BigInteger sevenQuantity = BigInteger.valueOf(0);
	private Double percent = 0D;

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public BigInteger getFirstQuantity() {
		if (firstQuantity == null) {
			firstQuantity = BigInteger.valueOf(0);
		}
		return firstQuantity;
	}

	public void setFirstQuantity(BigInteger firstQuantity) {
		this.firstQuantity = firstQuantity;
	}

	public BigInteger getSecondQuantity() {
		if (secondQuantity == null) {
			secondQuantity = BigInteger.valueOf(0);
		}
		return secondQuantity;
	}

	public void setSecondQuantity(BigInteger secondQuantity) {
		this.secondQuantity = secondQuantity;
	}

	public Double getPercent() {
		if(this.getSecondQuantity()!=BigInteger.valueOf(0)) {
			percent = this.getSecondQuantity().doubleValue()/this.getFirstQuantity().doubleValue()*100;
		}
		else {
			percent=0D;
		}
		return percent;
	}

	public BigInteger getThirdQuantity() {
		if(this.thirdQuantity==null) {
			this.thirdQuantity=BigInteger.valueOf(0);
		}
		return thirdQuantity;
	}

	public void setThirdQuantity(BigInteger thirdQuantity) {
		this.thirdQuantity = thirdQuantity;
	}

	public BigInteger getFourQuantity() {
		if(this.fourQuantity==null) {
			this.fourQuantity=BigInteger.valueOf(0);
		}
		return fourQuantity;
	}

	public void setFourQuantity(BigInteger fourQuantity) {
		this.fourQuantity = fourQuantity;
	}

	public BigInteger getFiveQuantity() {
		if(this.fiveQuantity==null) {
			this.fiveQuantity=BigInteger.valueOf(0);
		}
		return fiveQuantity;
	}

	public void setFiveQuantity(BigInteger fiveQuantity) {
		this.fiveQuantity = fiveQuantity;
	}

	public BigInteger getSixQuantity() {
		if(this.sixQuantity==null) {
			this.sixQuantity=BigInteger.valueOf(0);
		}
		return sixQuantity;
	}

	public void setSixQuantity(BigInteger sixQuantity) {
		this.sixQuantity = sixQuantity;
	}

	public BigInteger getSevenQuantity() {
		if(this.sevenQuantity==null) {
			this.sevenQuantity=BigInteger.valueOf(0);
		}
		return sevenQuantity;
	}

	public void setSevenQuantity(BigInteger sevenQuantity) {
		this.sevenQuantity = sevenQuantity;
	}
	
	
}
