package org.pepfar.pdma.app.data.dto;

import org.pepfar.pdma.app.data.domain.Document;

public class DocumentDto extends AuditableEntityDto {

	private Long id;

	private String title;

	private String contentType;

	private Long contentLength;

	private String extension;

	private byte[] content;

	private DocumentTypeDto docType;

//	private TaEventDto event;

	public DocumentDto() {
		super();
	}

	public DocumentDto(Document entity, boolean includeContent) {
		super(entity);

		if (entity == null) {
			return;
		}

		this.id = entity.getId();
		this.title = entity.getTitle();
		this.contentType = entity.getContentType();
		this.contentLength = entity.getContentLength();
		this.extension = entity.getExtension();

		if (entity.getDocType() != null) {
			this.docType = new DocumentTypeDto(entity.getDocType());
		}

		if (includeContent) {
			this.content = entity.getContent();
		}
	}

	public Document toEntity() {
		Document entity = new Document();
		entity = (Document) super.toEntity(entity);

		entity.setId(id);
		entity.setTitle(title);
		entity.setContentType(contentType);
		entity.setContentLength(contentLength);
		entity.setExtension(extension);

		if (docType != null) {
			entity.setDocType(docType.toEntity());
		}
//
//		if (event != null) {
//			TaEvent _event = new TaEvent();
//			_event.setId(event.getId());
//
//			entity.setTaEvent(_event);
//		}

		if (content != null) {
			entity.setContent(content);
		}

		entity.setCreateDate(getCreateDate());
		entity.setCreatedBy(getCreatedBy());
		entity.setModifyDate(getModifyDate());
		entity.setModifiedBy(getModifiedBy());

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

	public DocumentTypeDto getDocType() {
		return docType;
	}

	public void setDocType(DocumentTypeDto docType) {
		this.docType = docType;
	}
}
