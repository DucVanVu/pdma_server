package org.pepfar.pdma.app.data.dto;

import java.time.LocalDateTime;

import org.pepfar.pdma.app.data.domain.Announcement;
import org.pepfar.pdma.app.utils.CustomLocalDateTimeDeserializer2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class AnnouncementDto extends AuditableEntityDto
{

	private Long id;

	private String title;

	private String content;

	private int status;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime publishDate;

	public AnnouncementDto() {
	}

	public AnnouncementDto(Announcement entity) {
		super(entity);

		if (entity == null) {
			return;
		}

		this.id = entity.getId();
		this.title = entity.getTitle();
		this.content = entity.getContent();
		this.status = entity.getStatus();
		this.publishDate = entity.getPublishDate();
	}

	public Announcement toEntity() {
		Announcement entity = new Announcement();
		entity = (Announcement) super.toEntity(entity);

		entity.setId(id);
		entity.setTitle(title);
		entity.setContent(content);
		entity.setStatus(status);
		entity.setPublishDate(publishDate);

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

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public LocalDateTime getPublishDate() {
		return publishDate;
	}

	public void setPublishDate(LocalDateTime publishDate) {
		this.publishDate = publishDate;
	}

}
