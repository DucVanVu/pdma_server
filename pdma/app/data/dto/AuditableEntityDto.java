package org.pepfar.pdma.app.data.dto;

import java.time.LocalDateTime;

import org.pepfar.pdma.app.data.domain.auditing.AuditableEntity;
import org.pepfar.pdma.app.utils.CustomLocalDateTimeDeserializer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class AuditableEntityDto
{

	@JsonDeserialize(using = CustomLocalDateTimeDeserializer.class)
	private LocalDateTime createDate;

	private String createdBy;

	@JsonDeserialize(using = CustomLocalDateTimeDeserializer.class)
	private LocalDateTime modifyDate;

	private String modifiedBy;

	// -----------------------------
	// GETTERS/SETTERS
	// -----------------------------

	public AuditableEntityDto() {

	}

	public AuditableEntityDto(AuditableEntity entity) {
		if (entity == null) {
			return;
		}

		createDate = entity.getCreateDate();
		createdBy = entity.getCreatedBy();

		modifyDate = entity.getModifyDate();
		modifiedBy = entity.getModifiedBy();
	}

	public AuditableEntity toEntity(AuditableEntity entity) {

		entity.setCreateDate(createDate);
		entity.setCreatedBy(createdBy);
		entity.setModifyDate(modifyDate);
		entity.setModifiedBy(modifiedBy);

		return entity;
	}

	/**
	 * @return the createDate
	 */
	public LocalDateTime getCreateDate() {
		return createDate;
	}

	/**
	 * @param createDate the createDate to set
	 */
	public void setCreateDate(LocalDateTime createDate) {
		this.createDate = createDate;
	}

	/**
	 * @return the createdBy
	 */
	public String getCreatedBy() {
		return createdBy;
	}

	/**
	 * @param createdBy the createdBy to set
	 */
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	/**
	 * @return the modifyDate
	 */
	public LocalDateTime getModifyDate() {
		return modifyDate;
	}

	/**
	 * @param modifyDate the modifyDate to set
	 */
	public void setModifyDate(LocalDateTime modifyDate) {
		this.modifyDate = modifyDate;
	}

	/**
	 * @return the modifiedBy
	 */
	public String getModifiedBy() {
		return modifiedBy;
	}

	/**
	 * @param modifiedBy the modifiedBy to set
	 */
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}
}
