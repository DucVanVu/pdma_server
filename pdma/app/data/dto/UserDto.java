package org.pepfar.pdma.app.data.dto;

import java.util.HashSet;
import java.util.Set;

import org.pepfar.pdma.app.data.domain.Role;
import org.pepfar.pdma.app.data.domain.User;

public class UserDto extends AuditableEntityDto {

	private Long id;

	private String username;

	private String password;

	private Boolean justCreated;

	private String email;

	private String fullname;

	private byte[] photo;

	private Boolean hasPhoto;

	private Boolean photoCropped;

	private UserGroupDto userGroup;

	private Set<RoleDto> roles = new HashSet<>();

	/* Spring Security fields */

	private Boolean active;

	private Boolean pnsOnly;

	private Boolean opcAssistOnly;

	public UserDto() {
	}

	public UserDto(User entity, boolean withPassword) {
		super(entity);

		if (entity == null) {
			return;
		}

		if (withPassword) {
			this.password = entity.getPassword();
		}

		this.id = entity.getId();
		this.username = entity.getUsername();
		this.justCreated = entity.getJustCreated();
		this.email = entity.getEmail();
		this.fullname = entity.getFullname();
		this.active = entity.getActive();
		this.photoCropped = entity.getPhotoCropped();
		this.pnsOnly = entity.getPnsOnly();
		this.opcAssistOnly = entity.getOpcAssistOnly();

		if (entity.getUserGroup() != null) {
			this.userGroup = new UserGroupDto(entity.getUserGroup());
		}

		if (entity.getRoles() != null) {
			for (Role role : entity.getRoles()) {
				this.roles.add(new RoleDto(role));
			}
		}

		this.setHasPhoto(entity.getPhoto() != null);
	}

	public User toEntity() {
		User entity = new User();
		entity = (User) super.toEntity(entity);

		entity.setId(id);
		entity.setUsername(username);
		entity.setPassword(password);
		entity.setJustCreated(justCreated);
		entity.setEmail(email);
		entity.setFullname(fullname);
		entity.setActive(active);
		entity.setPhotoCropped(photoCropped);
		entity.setPnsOnly(pnsOnly);
		entity.setOpcAssistOnly(opcAssistOnly);

		if (userGroup != null) {
			entity.setUserGroup(userGroup.toEntity());
		}

		if (roles != null) {
			Set<Role> entities = new HashSet<>();
			roles.parallelStream().filter(r -> r.getId() != null).forEach(r -> {
				entities.add(r.toEntity());
			});

			entity.getRoles().addAll(entities);
		}

		return entity;
	}

	public Set<PermissionDto> getPermissions() {
		Set<PermissionDto> perms = new HashSet<PermissionDto>();

		if (roles != null) {
			roles.parallelStream().filter(r -> r != null).forEach(r -> {
				perms.addAll(r.getPermissions());
			});
		}

		return perms;
	}

	// Getters/Setters

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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public UserGroupDto getUserGroup() {
		return userGroup;
	}

	public void setUserGroup(UserGroupDto userGroup) {
		this.userGroup = userGroup;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public byte[] getPhoto() {
		return photo;
	}

	public void setPhoto(byte[] photo) {
		this.photo = photo;
	}

	public Boolean getHasPhoto() {
		return hasPhoto;
	}

	public void setHasPhoto(Boolean hasPhoto) {
		this.hasPhoto = hasPhoto;
	}

	public Boolean getPhotoCropped() {
		return photoCropped;
	}

	public void setPhotoCropped(Boolean photoCropped) {
		this.photoCropped = photoCropped;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Set<RoleDto> getRoles() {

		if (roles == null) {
			roles = new HashSet<RoleDto>();
		}

		return roles;
	}

	public void setRoles(Set<RoleDto> roles) {
		this.roles = roles;
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

}
