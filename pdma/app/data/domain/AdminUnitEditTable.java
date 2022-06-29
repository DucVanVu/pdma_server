package org.pepfar.pdma.app.data.domain;

import org.hibernate.annotations.GenericGenerator;
import org.pepfar.pdma.app.data.domain.auditing.AuditableEntity;

import javax.persistence.*;

@Entity
@Table(name = "tbl_admin_unit_edit_table")
public class AdminUnitEditTable extends AuditableEntity {
	@Transient
	private static final long serialVersionUID = -8045934263126850974L;

	@Id
	@GenericGenerator(name = "native", strategy = "native")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "native")
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "role_string")
	private String roles; //các role cách nhau bằng dấu ,

	@Column(name = "edit_table", nullable = true)
	private Boolean editTable;

	@Column(name = "year", nullable = true)
	private Integer year;

	@Column(name = "quarter", nullable = true)
	private Integer quarter;

	@ManyToOne
	@JoinColumn(name = "admin_unit_id")
	private AdminUnit adminUnit;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public Integer getQuarter() {
		return quarter;
	}

	public void setQuarter(Integer quarter) {
		this.quarter = quarter;
	}

	public AdminUnit getAdminUnit() {
		return adminUnit;
	}

	public void setAdminUnit(AdminUnit adminUnit) {
		this.adminUnit = adminUnit;
	}
}
