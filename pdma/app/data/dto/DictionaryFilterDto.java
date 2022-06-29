package org.pepfar.pdma.app.data.dto;

import org.pepfar.pdma.app.data.types.DictionaryType;

public class DictionaryFilterDto
{

	private DictionaryType type;
	
	private String keyword;

	private int pageIndex;

	private int pageSize;

	public DictionaryType getType() {
		return type;
	}

	public void setType(DictionaryType type) {
		this.type = type;
	}

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

}
