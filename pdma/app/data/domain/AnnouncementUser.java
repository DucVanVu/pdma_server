package org.pepfar.pdma.app.data.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
@Table(name = "tbl_announcement_user")
@IdClass(value = AnnouncementUserPK.class)
public class AnnouncementUser implements Serializable {

	@Transient
	private static final long serialVersionUID = 4724519537401810689L;

	@Id
	@ManyToOne()
	@JoinColumn(name = "announcement_id", referencedColumnName = "id")
	private Announcement announcement;

	@Id
	@ManyToOne()
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	private User user;

	@Column(name = "seen", nullable = false)
	private Boolean seen;

	public Announcement getAnnouncement() {
		return announcement;
	}

	public void setAnnouncement(Announcement announcement) {
		this.announcement = announcement;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Boolean getSeen() {
		return seen;
	}

	public void setSeen(Boolean read) {
		this.seen = read;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31).append(announcement).append(user).append(seen).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null) {
			return false;
		}

		if (!(obj instanceof AnnouncementUser)) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		AnnouncementUser that = (AnnouncementUser) obj;

		return new EqualsBuilder().append(announcement, that.announcement).append(user, that.user)
				.append(seen, that.seen).isEquals();
	}
}
