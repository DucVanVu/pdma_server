package org.pepfar.pdma.app.data.dto;

public class AnnouncementFilterDto
{

	private String keyword;

	private boolean publishedOnly;

	private int pageIndex;

	private int pageSize;

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public boolean isPublishedOnly() {
		return publishedOnly;
	}

	public void setPublishedOnly(boolean publishedOnly) {
		this.publishedOnly = publishedOnly;
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
