package org.pepfar.pdma.app.data.dto;

import org.pepfar.pdma.app.data.domain.User;

public class PNSAssessmentFilterDto {

	private String keyword;

	private Long[] facilityIds;

	private int pageIndex;

	private int pageSize;

	private User user;
	
	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public Long[] getFacilityIds() {
		return facilityIds;
	}

	public void setFacilityIds(Long[] facilityIds) {
		this.facilityIds = facilityIds;
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

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
