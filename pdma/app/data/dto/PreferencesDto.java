package org.pepfar.pdma.app.data.dto;

import org.pepfar.pdma.app.data.domain.Preferences;

public class PreferencesDto extends AuditableEntityDto
{

	private Long id;

	private String name;

	private String value;

	public PreferencesDto() {
		super();
	}

	public PreferencesDto(Preferences entity) {
		super(entity);

		if (entity == null) {
			return;
		}

		this.id = entity.getId();
		this.name = entity.getName();
		this.value = entity.getValue();
	}

	public Preferences toEntity() {
		Preferences entity = new Preferences();
		entity = (Preferences) super.toEntity(entity);

		entity.setId(this.id);
		entity.setName(this.name);
		entity.setValue(this.value);

		return entity;
	}

	// Getters/Setters

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
