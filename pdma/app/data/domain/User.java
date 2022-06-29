package org.pepfar.pdma.app.data.domain;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.pepfar.pdma.app.data.domain.auditing.AuditableEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "tbl_user", indexes = { @Index(columnList = "username", unique = true) })
@AttributeOverrides({ @AttributeOverride(name = "create_date", column = @Column(nullable = true)),
		@AttributeOverride(name = "created_by", column = @Column(nullable = true)) })
public class User extends AuditableEntity implements UserDetails {

	@Transient
	private static final long serialVersionUID = 5467396512939915793L;

	@Id
	@GenericGenerator(name = "native", strategy = "native")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "native")
	@Column(name = "id", unique = true, nullable = false, updatable = false)
	private Long id;

	@Column(name = "username", length = 100, nullable = false, unique = true)
	private String username;

	@Column(name = "password", length = 256, nullable = false)
	private String password;

	@Column(name = "just_created", nullable = false)
	private Boolean justCreated;

	@Column(name = "email", length = 256, nullable = false)
	private String email;

	@Column(name = "fullname", length = 100, nullable = false)
	private String fullname;

	@Column(name = "photo", nullable = true, columnDefinition = "LONGBLOB NULL")
	@Basic(fetch = FetchType.LAZY)
	private byte[] photo;

	@Column(name = "photo_cropped", nullable = false)
	private Boolean photoCropped;

	@ManyToOne
	@JoinColumn(name = "manager_id", nullable = true)
	private User manager;

	@ManyToOne
	@JoinColumn(name = "group_id", nullable = true)
	private UserGroup userGroup;

	// The PNS module can be used for PEPFAR-wide, and those that are created for
	// USAID facilities will be marked as for PNS only, who will then can only see
	// PNS module
	@Column(name = "pns_only", nullable = true)
	private Boolean pnsOnly;

	// There are requests that some user can only access to OPC-Assist module
	@Column(name = "opc_assist_only", nullable = true)
	private Boolean opcAssistOnly;

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@JoinTable(
			name = "tbl_user_preferences",
			indexes = {},
			joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
			inverseJoinColumns = @JoinColumn(name = "preferences_id", referencedColumnName = "id"))
	private Set<Preferences> preferences = new HashSet<Preferences>();

	/* Spring Security fields */

	@Column(name = "active", nullable = false)
	private Boolean active;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
			name = "tbl_user_role",
			joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
			inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
	private Set<Role> roles = new HashSet<Role>();

	@Column(name = "account_non_expired", nullable = true)
	private Boolean accountNonExpired = true;

	@Column(name = "account_non_locked", nullable = true)
	private Boolean accountNonLocked = true;

	@Column(name = "credentials_non_expired", nullable = true)
	private Boolean credentialsNonExpired = true;

	// --------------------------------------
	// GETTERS/SETTERS
	// --------------------------------------

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Boolean getJustCreated() {
		return justCreated;
	}

	public void setJustCreated(Boolean justCreated) {
		this.justCreated = justCreated;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public byte[] getPhoto() {
		return photo;
	}

	public void setPhoto(byte[] photo) {
		this.photo = photo;
	}

	public Boolean getPhotoCropped() {
		return photoCropped;
	}

	public void setPhotoCropped(Boolean photoCropped) {
		this.photoCropped = photoCropped;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Set<Role> getRoles() {

		if (roles == null) {
			roles = new HashSet<>();
		}

		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	public User getManager() {
		return manager;
	}

	public void setManager(User manager) {
		this.manager = manager;
	}

	public UserGroup getUserGroup() {
		return userGroup;
	}

	public void setUserGroup(UserGroup userGroup) {
		this.userGroup = userGroup;
	}

	public Set<Preferences> getPreferences() {

		if (preferences == null) {
			preferences = new HashSet<>();
		}

		return preferences;
	}

	public void setPreferences(Set<Preferences> preferences) {
		this.preferences = preferences;
	}

	public void setAccountNonExpired(Boolean accountNonExpired) {
		this.accountNonExpired = accountNonExpired;
	}

	public void setAccountNonLocked(Boolean accountNonLocked) {
		this.accountNonLocked = accountNonLocked;
	}

	public void setCredentialsNonExpired(Boolean credentialsNonExpired) {
		this.credentialsNonExpired = credentialsNonExpired;
	}

	public Boolean getAccountNonExpired() {
		return accountNonExpired;
	}

	public Boolean getAccountNonLocked() {
		return accountNonLocked;
	}

	public Boolean getCredentialsNonExpired() {
		return credentialsNonExpired;
	}

	public boolean isAccountNonExpired() {
		return this.accountNonExpired;
	}

	public boolean isAccountNonLocked() {
		return this.accountNonLocked;
	}

	public boolean isCredentialsNonExpired() {
		return this.credentialsNonExpired;
	}

	public boolean isEnabled() {
		return this.active;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public Boolean getPnsOnly() {
		return pnsOnly;
	}

	public void setPnsOnly(Boolean pnsOnly) {
		this.pnsOnly = pnsOnly;
	}

	public Boolean getOpcAssistOnly() {
		return opcAssistOnly;
	}

	public void setOpcAssistOnly(Boolean opcAssistOnly) {
		this.opcAssistOnly = opcAssistOnly;
	}

	@Transient
	public Set<Permission> getPermissions() {
		Set<Permission> perms = new HashSet<Permission>();

		for (Role role : roles) {
			perms.addAll(role.getPermissions());
		}

		return perms;
	}

	@Transient
	public Collection<GrantedAuthority> getAuthorities() {
		Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();

		authorities.addAll(roles);
		authorities.addAll(getPermissions());

		return authorities;
	}

	/**
	 * Returns {@code true} if the supplied object is a {@code User} instance with
	 * the same {@code username} value.
	 * <p>
	 * In other words, the objects are equal if they have the same username,
	 * representing the same principal.
	 */
	@Override
	public boolean equals(Object rhs) {
		if (rhs instanceof User) {
			return username.equals(((User) rhs).username);
		}
		return false;
	}

	/**
	 * Returns the hashcode of the {@code username}.
	 */
	@Override
	public int hashCode() {
		return username.hashCode();
	}

}
