package org.pepfar.pdma.app.data.dto;

import org.pepfar.pdma.app.data.domain.UserGroup;

public class UserGroupDto extends AuditableEntityDto
{

	private Long id;

	private String name;

	private String description;

	private int userCount;

	public UserGroupDto() {
		super();
	}

	public UserGroupDto(UserGroup entity) {
		super(entity);

		if (entity == null) {
			return;
		}

		this.id = entity.getId();
		this.name = entity.getName();
		this.description = entity.getDescription();

		if (entity.getUsers() != null) {
			this.userCount = entity.getUsers().size();
		} else {
			this.userCount = 0;
		}
	}

	public UserGroup toEntity() {
		UserGroup entity = new UserGroup();
		entity = (UserGroup) super.toEntity(entity);

		entity.setId(this.id);
		entity.setName(this.name);
		entity.setDescription(this.description);

		return entity;
	}

	// Getters/Setters

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getUserCount() {
		return userCount;
	}

	public void setUserCount(int userCount) {
		this.userCount = userCount;
	}

}
