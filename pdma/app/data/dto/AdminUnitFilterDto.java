
package org.pepfar.pdma.app.data.dto;

public class AdminUnitFilterDto
{

	private Long parentId;

	private String parentCode;

	private boolean excludeVoided;

	private String keyword;

	private int pageIndex;

	private int pageSize;

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public String getParentCode() {
		return parentCode;
	}

	public void setParentCode(String parentCode) {
		this.parentCode = parentCode;
	}

	public boolean isExcludeVoided() {
		return excludeVoided;
	}

	public void setExcludeVoided(boolean excludeVoided) {
		this.excludeVoided = excludeVoided;
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
