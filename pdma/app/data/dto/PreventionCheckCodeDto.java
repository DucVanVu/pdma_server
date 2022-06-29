package org.pepfar.pdma.app.data.dto;

public class PreventionCheckCodeDto {
	private Long id;
	private String code;
	private String couponCode;
	private String note;
	private boolean isDup=false;
	private Long orgId;
	
	private Long idHTS;
	private Long idOPC;
	private Integer type;
	
	
	
	public Long getIdHTS() {
		return idHTS;
	}
	public void setIdHTS(Long idHTS) {
		this.idHTS = idHTS;
	}
	public Long getIdOPC() {
		return idOPC;
	}
	public void setIdOPC(Long idOPC) {
		this.idOPC = idOPC;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getCouponCode() {
		return couponCode;
	}
	public void setCouponCode(String couponCode) {
		this.couponCode = couponCode;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public boolean getIsDup() {
		return isDup;
	}
	public void setIsDup(boolean isDup) {
		this.isDup = isDup;
	}
	public Long getOrgId() {
		return orgId;
	}
	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}
}
