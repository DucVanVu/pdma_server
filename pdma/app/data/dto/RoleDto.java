package org.pepfar.pdma.app.data.dto;

import java.util.HashSet;
import java.util.Set;

import org.pepfar.pdma.app.data.domain.Permission;
import org.pepfar.pdma.app.data.domain.Role;

public class RoleDto extends AuditableEntityDto
{

	private Long id;

	private String name;

	public Set<PermissionDto> permissions = new HashSet<PermissionDto>();

	public RoleDto() {
		super();
	}

	public RoleDto(Role entity) {
		super(entity);

		if (entity == null) {
			return;
		}

		this.id = entity.getId();
		this.name = entity.getName();

		if (entity.getPermissions() != null) {
			entity.getPermissions().parallelStream().forEach(p -> {
				this.permissions.add(new PermissionDto(p));
			});
		}
	}

	public Role toEntity() {
		Role entity = new Role();
		entity = (Role) super.toEntity(entity);

		entity.setId(id);
		entity.setName(name);

		if (this.permissions != null) {
			Set<Permission> perms = new HashSet<>();

			this.permissions.parallelStream().filter(p -> p.getId() != null).forEach(p -> {
				perms.add(p.toEntity());
			});

			entity.getPermissions().addAll(perms);
		}

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

	public Set<PermissionDto> getPermissions() {

		if (permissions == null) {
			permissions = new HashSet<>();
		}

		return permissions;
	}

	public void setPermissions(Set<PermissionDto> permissions) {
		this.permissions = permissions;
	}

}
