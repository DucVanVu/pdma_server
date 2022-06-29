package org.pepfar.pdma.app.data.dto;

import org.pepfar.pdma.app.data.domain.Staff;

public class StaffDto extends AuditableEntityDto
{

	private Long id;

	private String jobTitle;
	
	private String fullName;

	private PersonDto person;

	private OrganizationDto organization;
	
	private String staffCode;

	private String errorContent = null;

	private int numberErrorContent;

	private Boolean saved;

	public StaffDto() {
	}

	public StaffDto(Staff entity, boolean includePhoto) {
		super(entity);

		if (entity == null) {
			return;
		}

		this.id = entity.getId();
		this.jobTitle = entity.getJobTitle();
		this.staffCode = entity.getStaffCode();
		if (entity.getPerson() != null) {
			this.person = new PersonDto(entity.getPerson(), includePhoto);
			this.fullName = entity.getPerson().getFullname();
		}

		if (entity.getOrganization() != null) {
			this.organization = new OrganizationDto(entity.getOrganization(),true);
		}
	}

	public Staff toEntity() {
		Staff entity = new Staff();
		entity = (Staff) super.toEntity(entity);

		entity.setId(id);
		entity.setJobTitle(jobTitle);
		entity.setStaffCode(staffCode);
		if (person != null) {
			entity.setPerson(person.toEntity());
			this.fullName = entity.getPerson().getFullname();
		}

		if (organization != null) {
			entity.setOrganization(organization.toEntity());
		}

		return entity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public PersonDto getPerson() {
		return person;
	}

	public void setPerson(PersonDto person) {
		this.person = person;
	}

	public OrganizationDto getOrganization() {
		return organization;
	}

	public void setOrganization(OrganizationDto organization) {
		this.organization = organization;
	}

	public String getStaffCode() {
		return staffCode;
	}

	public void setStaffCode(String staffCode) {
		this.staffCode = staffCode;
	}

	public String getErrorContent() {
		return errorContent;
	}

	public void setErrorContent(String errorContent) {
		this.errorContent = errorContent;
	}

	public int getNumberErrorContent() {
		return numberErrorContent;
	}

	public void setNumberErrorContent(int numberErrorContent) {
		this.numberErrorContent = numberErrorContent;
	}

	public Boolean getSaved() {
		return saved;
	}

	public void setSaved(Boolean saved) {
		this.saved = saved;
	}
}
