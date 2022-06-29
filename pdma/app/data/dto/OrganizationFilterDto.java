package org.pepfar.pdma.app.data.dto;

import java.util.List;

public class OrganizationFilterDto {

	private Long parentId;

	private Boolean activeOnly;

	private Boolean pepfarSiteOnly;

	private Boolean htsSiteOnly;

	private Boolean opcSiteOnly;

	private Boolean prepSiteOnly;

	private Boolean pnsSiteOnly;

	/**
	 * If TRUE, only select the facilities the current user is assigned to
	 */
	private Boolean checkUserPermission;

	/**
	 * For selection of facilities across provinces. Just added.
	 */
	private List<Long> provinceIds;

	private AdminUnitDto province;

	private AdminUnitDto district;

	private String keyword;

	private int pageIndex;

	private int pageSize;

	private boolean compact;

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public Boolean getActiveOnly() {
		return activeOnly;
	}

	public void setActiveOnly(Boolean activeOnly) {
		this.activeOnly = activeOnly;
	}

	public Boolean getPepfarSiteOnly() {
		return pepfarSiteOnly;
	}

	public void setPepfarSiteOnly(Boolean pepfarSiteOnly) {
		this.pepfarSiteOnly = pepfarSiteOnly;
	}

	public Boolean getHtsSiteOnly() {
		return htsSiteOnly;
	}

	public void setHtsSiteOnly(Boolean htsSiteOnly) {
		this.htsSiteOnly = htsSiteOnly;
	}

	public Boolean getOpcSiteOnly() {
		return opcSiteOnly;
	}

	public void setOpcSiteOnly(Boolean opcSiteOnly) {
		this.opcSiteOnly = opcSiteOnly;
	}

	public Boolean getPrepSiteOnly() {
		return prepSiteOnly;
	}

	public void setPrepSiteOnly(Boolean prepSiteOnly) {
		this.prepSiteOnly = prepSiteOnly;
	}

	public Boolean getPnsSiteOnly() {
		return pnsSiteOnly;
	}

	public void setPnsSiteOnly(Boolean pnsSiteOnly) {
		this.pnsSiteOnly = pnsSiteOnly;
	}

	public Boolean getCheckUserPermission() {
		return checkUserPermission;
	}

	public void setCheckUserPermission(Boolean checkUserPermission) {
		this.checkUserPermission = checkUserPermission;
	}

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

	public AdminUnitDto getDistrict() {
		return district;
	}

	public void setDistrict(AdminUnitDto district) {
		this.district = district;
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

	public boolean isCompact() {
		return compact;
	}

	public void setCompact(boolean compact) {
		this.compact = compact;
	}

}
