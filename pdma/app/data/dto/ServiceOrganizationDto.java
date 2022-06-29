package org.pepfar.pdma.app.data.dto;

import java.time.LocalDateTime;

import org.pepfar.pdma.app.data.domain.ServiceOrganization;
import org.pepfar.pdma.app.utils.CustomLocalDateTimeDeserializer2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class ServiceOrganizationDto
{

	private ServiceDto service;

	private OrganizationDto organization;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime startDate;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime endDate;

	private Boolean active;

	private String endingReason;

	public ServiceOrganizationDto() {
	}

	public ServiceOrganizationDto(ServiceOrganization entity) {
		if (entity == null) {
			return;
		}

		this.startDate = entity.getStartDate();
		this.endDate = entity.getEndDate();
		this.active = entity.getActive();
		this.endingReason = entity.getEndingReason();

		if (entity.getService() != null) {
			this.service = new ServiceDto(entity.getService());
		}

		if (entity.getOrganization() != null) {
			this.organization = new OrganizationDto(entity.getOrganization());
		}
	}

	public ServiceOrganization toEntity() {
		ServiceOrganization entity = new ServiceOrganization();

		entity.setStartDate(startDate);
		entity.setEndDate(endDate);
		entity.setActive(active);
		entity.setEndingReason(endingReason);

		if (service != null) {
			entity.setService(service.toEntity());
		}

		if (organization != null) {
			entity.setOrganization(organization.toEntity());
		}

		return entity;
	}

	public ServiceDto getService() {
		return service;
	}

	public void setService(ServiceDto service) {
		this.service = service;
	}

	public OrganizationDto getOrganization() {
		return organization;
	}

	public void setOrganization(OrganizationDto organization) {
		this.organization = organization;
	}

	public LocalDateTime getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDateTime startDate) {
		this.startDate = startDate;
	}

	public LocalDateTime getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDateTime endDate) {
		this.endDate = endDate;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getEndingReason() {
		return endingReason;
	}

	public void setEndingReason(String endingReason) {
		this.endingReason = endingReason;
	}

}
