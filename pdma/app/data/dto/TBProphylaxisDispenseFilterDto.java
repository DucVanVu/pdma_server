package org.pepfar.pdma.app.data.dto;

public class TBProphylaxisDispenseFilterDto {

	private String keyword;

	private int pageIndex;

	private int pageSize;

	private TBProphylaxis2Dto round;

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public TBProphylaxis2Dto getRound() {
		return round;
	}

	public void setRound(TBProphylaxis2Dto round) {
		this.round = round;
	}	

}
