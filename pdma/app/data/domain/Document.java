package org.pepfar.pdma.app.data.domain;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.pepfar.pdma.app.data.domain.auditing.AuditableEntity;

@Entity
@Table(name = "tbl_document")
public class Document extends AuditableEntity {

	@Transient
	private static final long serialVersionUID = 6139764073477206098L;

	@Id
	@GenericGenerator(name = "native", strategy = "native")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "native")
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "title", length = 255, nullable = false)
	private String title;

	@Column(name = "content_type", length = 100, nullable = false)
	private String contentType;

	@Column(name = "content_length", nullable = false)
	private Long contentLength;

	@Column(name = "extension", length = 100, nullable = true)
	private String extension;

	@Column(name = "content", nullable = false, columnDefinition = "LONGBLOB NOT NULL")
	@Basic(fetch = FetchType.LAZY)
	private byte[] content;

	@ManyToOne
	@JoinColumn(name = "doc_type_id", nullable = false)
	private DocumentType docType;

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

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public Long getContentLength() {
		return contentLength;
	}

	public void setContentLength(Long contentLength) {
		this.contentLength = contentLength;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public DocumentType getDocType() {
		return docType;
	}

	public void setDocType(DocumentType docType) {
		this.docType = docType;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31).append(id).append(title).append(contentType).append(contentLength)
				.append(extension).append(content).append(docType).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null) {
			return false;
		}

		if (!(obj instanceof Document)) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		Document that = (Document) obj;

		return new EqualsBuilder().append(id, that.id).append(title, that.title).append(contentType, that.contentType)
				.append(contentLength, that.contentLength).append(extension, that.extension)
				.append(content, that.content).append(docType, that.docType).isEquals();
	}
}
