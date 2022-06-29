package org.pepfar.pdma.app.data.domain;

import java.util.HashSet;
import java.util.Set;

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
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.pepfar.pdma.app.data.domain.auditing.AuditableEntity;
import org.springframework.security.core.GrantedAuthority;

@Entity
@Table(name = "tbl_role", indexes = { @Index(columnList = "role_name", unique = true) })
public class Role extends AuditableEntity implements GrantedAuthority
{

	@Transient
	private static final long serialVersionUID = 9092774599340607981L;

	@Id
	@GenericGenerator(name = "native", strategy = "native")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "native")
	@Column(name = "id", unique = true, nullable = false, updatable = false)
	private Long id;

	@Column(name = "role_name", length = 100, nullable = false)
	private String name;

	@ManyToMany(mappedBy = "roles")
	private Set<User> users = new HashSet<User>();

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
			name = "tbl_role_permission",
			joinColumns = @JoinColumn(name = "role_id", nullable = false, referencedColumnName = "id"),
			inverseJoinColumns = @JoinColumn(name = "permission_id", nullable = false, referencedColumnName = "id"))
	private Set<Permission> permissions = new HashSet<Permission>();

	// --------------------------------------
	// GrantedAuthority implementation
	// --------------------------------------

	public String getAuthority() {
		return this.name;
	}

	// --------------------------------------
	// GETTERS/SETTERS
	// --------------------------------------

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<User> getUsers() {
		return users;
	}

	public void setUsers(Set<User> users) {
		this.users = users;
	}

	public Set<Permission> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<Permission> permissions) {
		this.permissions = permissions;
	}

	@Transient
	@Override
	public String toString() {
		return String.format("Role: %s", this.name);
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31).append(id).append(name).append(permissions).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null) {
			return false;
		}

		if (!(obj instanceof Role)) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		Role that = (Role) obj;

		return new EqualsBuilder().append(id, that.id).append(name, that.name).append(permissions, that.permissions)
				.isEquals();
	}

}
