package org.pepfar.pdma.app.data.dto;

import java.time.LocalDateTime;

import org.pepfar.pdma.app.data.domain.Notification;
import org.pepfar.pdma.app.data.domain.User;
import org.pepfar.pdma.app.data.types.NotificationSourceType;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class NotificationDto extends AuditableEntityDto
{

	private Long id;

	private UserDto user;

	private String title;

	private String content;

	private NotificationSourceType type;

	private Long sourceId;

	private Object source;

	private Boolean seen;

	@JsonSerialize(using = ToStringSerializer.class)
	private LocalDateTime since;

	public NotificationDto() {
	}

	public NotificationDto(Notification entity, Object source) {
		if (entity == null) {
			return;
		}

		id = entity.getId();
		title = entity.getTitle();
		content = entity.getContent();
		type = entity.getType();
		seen = entity.getSeen();
		sourceId = entity.getSourceId();
		since = entity.getCreateDate();

		if (entity.getUser() != null) {
			user = new UserDto(entity.getUser(), false);
		}

		if (source != null) {
			this.source = source;
		}

		setCreateDate(entity.getCreateDate());
		setCreatedBy(entity.getCreatedBy());
		setModifyDate(entity.getModifyDate());
		setModifiedBy(entity.getModifiedBy());
	}

	public Notification toEntity() {
		Notification entity = new Notification();
		entity = (Notification) super.toEntity(entity);

		entity.setId(id);
		entity.setTitle(title);
		entity.setContent(content);
		entity.setType(type);
		entity.setSeen(seen);
		entity.setSourceId(sourceId);

		if (user != null) {
			User ue = new User();
			ue.setId(user.getId());
			ue.setUsername(user.getUsername());

			entity.setUser(ue);
		}

		return entity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public NotificationSourceType getType() {
		return type;
	}

	public void setType(NotificationSourceType type) {
		this.type = type;
	}

	public UserDto getUser() {
		return user;
	}

	public void setUser(UserDto user) {
		this.user = user;
	}

	public Boolean getSeen() {
		return seen;
	}

	public void setSeen(Boolean seen) {
		this.seen = seen;
	}

	public Long getSourceId() {
		return sourceId;
	}

	public void setSourceId(Long sourceId) {
		this.sourceId = sourceId;
	}

	public Object getSource() {
		return source;
	}

	public void setSource(Object source) {
		this.source = source;
	}

}
