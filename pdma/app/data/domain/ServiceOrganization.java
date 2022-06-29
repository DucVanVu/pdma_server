package org.pepfar.pdma.app.data.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.pepfar.pdma.app.utils.LocalDateTimeAttributeConverter;

@Entity
@Table(name = "tbl_organization_service")
@IdClass(value = ServiceOrganizationPK.class)
public class ServiceOrganization implements Serializable
{

	@Transient
	private static final long serialVersionUID = -3514930632377536615L;

	@Id
	@ManyToOne
	@JoinColumn(name = "service_id", referencedColumnName = "id")
	private Service service;

	@Id
	@ManyToOne
	@JoinColumn(name = "org_id", referencedColumnName = "id")
	private Organization organization;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "start_date", nullable = false)
	private LocalDateTime startDate;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "end_date", nullable = true)
	private LocalDateTime endDate;

	@Column(name = "is_active", nullable = false)
	private Boolean active;

	@Column(name = "reason_for_ending", length = 200, nullable = true)
	private String endingReason;

	public Service getService() {
		return service;
	}

	public void setService(Service service) {
		this.service = service;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
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
