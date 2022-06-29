package org.pepfar.pdma.app.data.dto;

import org.pepfar.pdma.app.data.domain.UserOrganization;

import javax.persistence.Column;

public class UserOrganizationDto
{

	private UserDto user;

	private OrganizationDto organization;

	private Boolean readAccess;

	private Boolean writeAccess;

	private Boolean deleteAccess;

	private Boolean htsRole;

	private Boolean peRole;

	private Boolean pnsRole;

	private Boolean snsRole;

	private Boolean selfTestRole;

	public UserOrganizationDto() {
	}

	public UserOrganizationDto(UserOrganization entity) {
		if (entity == null) {
			return;
		}

		this.readAccess = entity.getReadAccess();
		this.writeAccess = entity.getWriteAccess();
		this.deleteAccess = entity.getDeleteAccess();
		this.htsRole = entity.getHtsRole();
		this.peRole = entity.getPeRole();
		this.pnsRole = entity.getPnsRole();
		this.snsRole = entity.getSnsRole();
		this.selfTestRole = entity.getSelfTestRole();

		if (entity.getUser() != null) {
			this.user = new UserDto(entity.getUser(), false);
		}

		if (entity.getOrganization() != null) {
			this.organization = new OrganizationDto(entity.getOrganization());
		}
	}

	public UserOrganization toEntity() {
		UserOrganization entity = new UserOrganization();

		entity.setReadAccess(readAccess);
		entity.setWriteAccess(writeAccess);
		entity.setDeleteAccess(deleteAccess);
		entity.setHtsRole(htsRole);
		entity.setPeRole(peRole);
		entity.setPnsRole(pnsRole);
		entity.setSnsRole(snsRole);
		entity.setSelfTestRole(selfTestRole);


		if (user != null) {
			entity.setUser(user.toEntity());
		}

		if (organization != null) {
			entity.setOrganization(organization.toEntity());
		}

		return entity;
	}

	public UserDto getUser() {
		return user;
	}

	public void setUser(UserDto user) {
		this.user = user;
	}

	public OrganizationDto getOrganization() {
		return organization;
	}

	public void setOrganization(OrganizationDto organization) {
		this.organization = organization;
	}

	public Boolean getReadAccess() {
		return readAccess;
	}

	public void setReadAccess(Boolean readAccess) {
		this.readAccess = readAccess;
	}

	public Boolean getWriteAccess() {
		return writeAccess;
	}

	public void setWriteAccess(Boolean writeAccess) {
		this.writeAccess = writeAccess;
	}

	public Boolean getDeleteAccess() {
		return deleteAccess;
	}

	public void setDeleteAccess(Boolean deleteAccess) {
		this.deleteAccess = deleteAccess;
	}

	public Boolean getHtsRole() {
		return htsRole;
	}

	public void setHtsRole(Boolean htsRole) {
		this.htsRole = htsRole;
	}

	public Boolean getPeRole() {
		return peRole;
	}

	public void setPeRole(Boolean peRole) {
		this.peRole = peRole;
	}

	public Boolean getPnsRole() {
		return pnsRole;
	}

	public void setPnsRole(Boolean pnsRole) {
		this.pnsRole = pnsRole;
	}

	public Boolean getSnsRole() {
		return snsRole;
	}

	public void setSnsRole(Boolean snsRole) {
		this.snsRole = snsRole;
	}

	public Boolean getSelfTestRole() {
		return selfTestRole;
	}

	public void setSelfTestRole(Boolean selfTestRole) {
		this.selfTestRole = selfTestRole;
	}
}
