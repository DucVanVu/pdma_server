package org.pepfar.pdma.app.data.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.pepfar.pdma.app.data.domain.auditing.AuditableEntity;
import org.pepfar.pdma.app.utils.LocalDateTimeAttributeConverter;

@Entity
@Table(name = "tbl_announcement")
public class Announcement extends AuditableEntity {

	@Transient
	private static final long serialVersionUID = -8750925747830147667L;

	@Id
	@GenericGenerator(name = "native", strategy = "native")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "native")
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "title", length = 512, nullable = false)
	private String title;

	@Column(name = "content", columnDefinition = "LONGTEXT", nullable = true)
	private String content;

	@Column(name = "status", nullable = false)
	private int status; // 0 = drafting, 1 = published

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "publish_date", nullable = true)
	private LocalDateTime publishDate;

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

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31).append(id).append(title).append(content).append(status).append(publishDate)
				.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null) {
			return false;
		}

		if (!(obj instanceof Announcement)) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		Announcement that = (Announcement) obj;

		return new EqualsBuilder().append(id, that.id).append(title, that.title).append(content, that.content)
				.append(status, that.status).append(publishDate, that.publishDate).isEquals();
	}
}
