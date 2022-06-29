package org.pepfar.pdma.app.data.dto;

import org.pepfar.pdma.app.data.domain.SavedReport;
import org.pepfar.pdma.app.data.types.ReportType;

public class SavedReportDto extends AuditableEntityDto {

	private Long id;

	private OrganizationDto organization;
	
	private String title;

	private ReportType reportType;

	private String contentType;

	private Long contentLength;

	private String extension;

	private byte[] content;

	private String createdByName; // Convert username into user full name

	public SavedReportDto() {
		super();
	}

	public SavedReportDto(SavedReport entity, boolean includeContent) {
		super(entity);

		if (entity == null) {
			return;
		}

		this.id = entity.getId();
		this.title = entity.getTitle();
		this.reportType = entity.getReportType();
		this.contentType = entity.getContentType();
		this.contentLength = entity.getContentLength();
		this.extension = entity.getExtension();

		if (entity.getOrganization() != null) {
			this.organization = new OrganizationDto();
			this.organization.setId(entity.getOrganization().getId());
			this.organization.setActive(entity.getOrganization().getActive());
			this.organization.setName(entity.getOrganization().getName());
		}

		if (includeContent) {
			this.content = entity.getContent();
		}
	}

	public SavedReport toEntity() {
		SavedReport entity = new SavedReport();
		entity = (SavedReport) super.toEntity(entity);

		entity.setId(id);
		entity.setTitle(title);
		entity.setReportType(reportType);
		entity.setContentType(contentType);
		entity.setContentLength(contentLength);
		entity.setExtension(extension);

		if (organization != null) {
			entity.setOrganization(organization.toEntity());
		}

		if (content != null) {
			entity.setContent(content);
		}

		return entity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public OrganizationDto getOrganization() {
		return organization;
	}

	public void setOrganization(OrganizationDto organization) {
		this.organization = organization;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public ReportType getReportType() {
		return reportType;
	}

	public void setReportType(ReportType reportType) {
		this.reportType = reportType;
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

	public String getCreatedByName() {
		return createdByName;
	}

	public void setCreatedByName(String createdByName) {
		this.createdByName = createdByName;
	}

}
