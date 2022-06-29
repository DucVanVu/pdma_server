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

@Entity
@Table(name = "tbl_user_organization")
@IdClass(value = UserOrganizationPK.class)
public class UserOrganization implements Serializable
{

	@Transient
	private static final long serialVersionUID = -8045934263126850974L;

	@Id
	@ManyToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	private User user;

	@Id
	@ManyToOne
	@JoinColumn(name = "organization_id", referencedColumnName = "id")
	private Organization organization;

	@Column(name = "read_access", nullable = false)
	private Boolean readAccess;

	@Column(name = "write_access", nullable = false)
	private Boolean writeAccess;

	@Column(name = "delete_access", nullable = false)
	private Boolean deleteAccess;

	@Column(name = "hts_role", nullable = true)
	private Boolean htsRole;
	
	@Column(name = "pe_role", nullable = true)
	private Boolean peRole;
	
	@Column(name = "pns_role", nullable = true)
	private Boolean pnsRole;
	
	@Column(name = "sns_role", nullable = true)
	private Boolean snsRole;
	
	@Column(name = "self_test_role", nullable = true)
	private Boolean selfTestRole;
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
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
