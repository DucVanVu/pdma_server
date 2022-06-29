package org.pepfar.pdma.app.data.dto;

import org.pepfar.pdma.app.data.domain.DocumentType;

public class DocumentTypeDto
{
	
	private Long id;

	private String code;

	private String name;

	private Boolean active;

	public DocumentTypeDto() {
	}

	public DocumentTypeDto(DocumentType entity) {
		if (entity == null) {
			return;
		}

		id = entity.getId();
		code = entity.getCode();
		name = entity.getName();
		active = entity.getActive();
	}

	public DocumentType toEntity() {

		DocumentType entity = new DocumentType();

		entity.setId(id);
		entity.setCode(code);
		entity.setName(name);
		entity.setActive(active);

		return entity;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

}
