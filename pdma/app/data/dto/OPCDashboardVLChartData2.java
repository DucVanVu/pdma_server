package org.pepfar.pdma.app.data.dto;

public class OPCDashboardVLChartData2 {

	private int testCount;

	private int noResultCount;

	private int undetectableCount;

	private int lt200Count;

	private int lt1000Count;

	private int ge1000Count;
	
	private String quarter;

	public int getTestCount() {
		return testCount;
	}

	public void setTestCount(int testCount) {
		this.testCount = testCount;
	}

	public int getNoResultCount() {
		return noResultCount;
	}

	public void setNoResultCount(int noResultCount) {
		this.noResultCount = noResultCount;
	}

	public int getUndetectableCount() {
		return undetectableCount;
	}

	public void setUndetectableCount(int undetectableCount) {
		this.undetectableCount = undetectableCount;
	}

	public int getLt200Count() {
		return lt200Count;
	}

	public void setLt200Count(int lt200Count) {
		this.lt200Count = lt200Count;
	}

	public int getLt1000Count() {
		return lt1000Count;
	}

	public void setLt1000Count(int lt1000Count) {
		this.lt1000Count = lt1000Count;
	}

	public int getGe1000Count() {
		return ge1000Count;
	}

	public void setGe1000Count(int ge1000Count) {
		this.ge1000Count = ge1000Count;
	}

	public String getQuarter() {
		return quarter;
	}

	public void setQuarter(String quarter) {
		this.quarter = quarter;
	}

}
