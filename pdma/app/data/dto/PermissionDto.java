package org.pepfar.pdma.app.data.dto;

import org.pepfar.pdma.app.data.domain.Permission;
import org.springframework.security.core.GrantedAuthority;

public class PermissionDto implements GrantedAuthority
{

	private static final long serialVersionUID = -1960996615591493812L;

	private Long id;

	private String name;

	public PermissionDto() {
	}

	public PermissionDto(Permission entity) {
		if (entity == null) {
			return;
		}

		this.id = entity.getId();
		this.name = entity.getName();
	}

	public Permission toEntity() {
		Permission entity = new Permission();

		entity.setId(this.id);
		entity.setName(this.name);

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

	public String getAuthority() {
		return this.name;
	}

}
