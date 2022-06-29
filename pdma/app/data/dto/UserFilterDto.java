package org.pepfar.pdma.app.data.dto;

public class UserFilterDto {
	
	private Boolean active;
	
	private AdminUnitDto provinceId;

	private String keyword;

	private int pageIndex;

	private int pageSize;

	private boolean activeOnly;

	private boolean pnsOnly;
	
	

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public AdminUnitDto getProvinceId() {
		return provinceId;
	}

	public void setProvinceId(AdminUnitDto provinceId) {
		this.provinceId = provinceId;
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

	public boolean isActiveOnly() {
		return activeOnly;
	}

	public void setActiveOnly(boolean activeOnly) {
		this.activeOnly = activeOnly;
	}

	public boolean isPnsOnly() {
		return pnsOnly;
	}

	public void setPnsOnly(boolean pnsOnly) {
		this.pnsOnly = pnsOnly;
	}

}
