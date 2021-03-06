package org.pepfar.pdma.app.data.dto;

import org.pepfar.pdma.app.data.domain.Service;

public class ServiceDto extends AuditableEntityDto
{

	private Long id;

	private String code;

	private String name;

	private String description;

	public ServiceDto() {
	}

	public ServiceDto(Service entity) {
		super(entity);

		if (entity == null) {
			return;
		}

		this.id = entity.getId();
		this.code = entity.getCode();
		this.name = entity.getName();
		this.description = entity.getDescription();
	}

	public Service toEntity() {
		Service entity = new Service();
		entity = (Service) super.toEntity(entity);

		entity.setId(id);
		entity.setCode(code);
		entity.setName(name);
		entity.setDescription(description);

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
