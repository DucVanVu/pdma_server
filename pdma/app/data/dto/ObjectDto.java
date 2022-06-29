package org.pepfar.pdma.app.data.dto;

public class ObjectDto extends AuditableEntityDto {
	
	private String code;

	private String note;
	
	private Integer number;
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	
}
