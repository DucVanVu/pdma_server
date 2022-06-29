package org.pepfar.pdma.app.data.dto;

import java.util.List;

public class HIVConfirmLabFilterDto
{

	private AdminUnitDto province;

	private String keyword;

	private int pageIndex;

	private int pageSize;
	
	private List<Long> provinceIds;
	
	

	public List<Long> getProvinceIds() {
		return provinceIds;
	}

	public void setProvinceIds(List<Long> provinceIds) {
		this.provinceIds = provinceIds;
	}

	public AdminUnitDto getProvince() {
		return province;
	}

	public void setProvince(AdminUnitDto province) {
		this.province = province;
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
