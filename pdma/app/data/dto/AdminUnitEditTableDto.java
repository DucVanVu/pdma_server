package org.pepfar.pdma.app.data.dto;

import org.pepfar.pdma.app.data.domain.AdminUnitEditTable;

import java.util.ArrayList;
import java.util.List;

public class AdminUnitEditTableDto extends AuditableEntityDto
{

	private Long id;

	private String roles; //các role cách nhau bằng dấu ,

	private Boolean editTable;

	private Integer quarter;

	private AdminUnitDto adminUnit;

	private List<AdminUnitDto> listSelectedAdminUnit = new ArrayList<AdminUnitDto>();

	private Integer year;

	public AdminUnitEditTableDto() {
		super();
	}

	public AdminUnitEditTableDto(AdminUnitEditTable entity) {
		super();
		this.id = entity.getId();
		if (entity.getAdminUnit() != null) {
			this.adminUnit = new AdminUnitDto(entity.getAdminUnit());
		}
		this.roles = entity.getRoles();
		this.editTable = entity.getEditTable();
		this.year = entity.getYear();
		this.quarter = entity.getQuarter();
	}


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<AdminUnitDto> getListSelectedAdminUnit() {
		return listSelectedAdminUnit;
	}

	public void setListSelectedAdminUnit(List<AdminUnitDto> listSelectedAdminUnit) {
		this.listSelectedAdminUnit = listSelectedAdminUnit;
	}

	public AdminUnitDto getAdminUnit() {
		return adminUnit;
	}

	public void setAdminUnit(AdminUnitDto adminUnit) {
		this.adminUnit = adminUnit;
	}

	public String getRoles() {
		return roles;
	}

	public void setRoles(String roles) {
		this.roles = roles;
	}

	public Boolean getEditTable() {
		return editTable;
	}

	public void setEditTable(Boolean editTable) {
		this.editTable = editTable;
	}

	public Integer getQuarter() {
		return quarter;
	}

	public void setQuarter(Integer quarter) {
		this.quarter = quarter;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}
}
