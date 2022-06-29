package org.pepfar.pdma.app.data.domain;

import java.io.Serializable;

import javax.persistence.Embeddable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Embeddable
public class AnnouncementUserPK implements Serializable {
	private static final long serialVersionUID = 3593497715320299479L;

	private Long announcement;

	private Long user;

	public AnnouncementUserPK() {

	}

	public AnnouncementUserPK(Long announcement, Long user) {
		this.announcement = announcement;
		this.user = user;
	}

	public Long getAnnouncement() {
		return announcement;
	}

	public void setAnnouncement(Long announcement) {
		this.announcement = announcement;
	}

	public Long getUser() {
		return user;
	}

	public void setUser(Long user) {
		this.user = user;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31).append(announcement).append(user).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null) {
			return false;
		}

		if (!(obj instanceof AnnouncementUserPK)) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		AnnouncementUserPK that = (AnnouncementUserPK) obj;

		return new EqualsBuilder().append(announcement, that.announcement).append(user, that.user).isEquals();
	}
}
