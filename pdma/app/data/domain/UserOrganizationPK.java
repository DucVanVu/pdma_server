package org.pepfar.pdma.app.data.domain;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Embeddable
public class UserOrganizationPK implements Serializable {

	@Transient
	private static final long serialVersionUID = 7166324664122022437L;

	private Long user;

	private Long organization;

	public UserOrganizationPK() {

	}

	public UserOrganizationPK(Long user, Long organization) {
		this.user = user;
		this.organization = organization;
	}

	public Long getUser() {
		return user;
	}

	public void setUser(Long user) {
		this.user = user;
	}

	public Long getOrganization() {
		return organization;
	}

	public void setOrganization(Long organization) {
		this.organization = organization;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31).append(user).append(organization).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null) {
			return false;
		}

		if (!(obj instanceof UserOrganizationPK)) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		UserOrganizationPK that = (UserOrganizationPK) obj;

		return new EqualsBuilder().append(user, that.user).append(organization, that.organization).isEquals();
	}
}
