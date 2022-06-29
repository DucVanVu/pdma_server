package org.pepfar.pdma.app.data.domain;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Embeddable
public class ServiceOrganizationPK implements Serializable {

	@Transient
	private static final long serialVersionUID = 3116281150942605344L;

	private Long service;

	private Long organization;

	public ServiceOrganizationPK() {

	}

	public ServiceOrganizationPK(Long service, Long organization) {
		this.service = service;
		this.organization = organization;
	}

	public Long getService() {
		return service;
	}

	public void setService(Long service) {
		this.service = service;
	}

	public Long getOrganization() {
		return organization;
	}

	public void setOrganization(Long organization) {
		this.organization = organization;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31).append(service).append(organization).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null) {
			return false;
		}

		if (!(obj instanceof ServiceOrganizationPK)) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		ServiceOrganizationPK that = (ServiceOrganizationPK) obj;

		return new EqualsBuilder().append(service, that.service).append(organization, that.organization).isEquals();
	}
}
