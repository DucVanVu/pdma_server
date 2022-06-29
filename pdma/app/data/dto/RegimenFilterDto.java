package org.pepfar.pdma.app.data.dto;

public class RegimenFilterDto
{
	private String keyword;

	private DictionaryDto disease;

	private int pageIndex;

	private int pageSize;

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public DictionaryDto getDisease() {
		return disease;
	}

	public void setDisease(DictionaryDto disease) {
		this.disease = disease;
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

}
