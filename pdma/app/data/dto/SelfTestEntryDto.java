package org.pepfar.pdma.app.data.dto;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.pepfar.pdma.app.data.domain.AdminUnit;
import org.pepfar.pdma.app.data.domain.Location;
import org.pepfar.pdma.app.data.domain.SelfTestEntry;
import org.pepfar.pdma.app.data.domain.SelfTestSpecimen;
import org.pepfar.pdma.app.data.types.PEC8;
import org.pepfar.pdma.app.data.types.SelfTestSource;
import org.pepfar.pdma.app.utils.CustomLocalDateTimeDeserializer2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class SelfTestEntryDto extends AuditableEntityDto {

	private Long id;

	private OrganizationDto organization;

	private StaffDto dispensingStaff;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime dispensingDate;

	private PersonDto person;

	private Set<SelfTestSpecimenDto> specimens = new LinkedHashSet<>();

	private SelfTestSource selfTestSource;
	private String selfTestSourceDes;
	
	private boolean isReadAble;
	
	private boolean isWritAble;
	
	private boolean isDeleteAble;

	public SelfTestEntryDto() {

	}

	public SelfTestEntryDto(SelfTestEntry entity, Boolean isViewPII,boolean isReadAble,
			boolean isWritAble,
			boolean isDeleteAble) {

		super(entity);

		if (entity == null) {
			return;
		}
		this.isDeleteAble = isDeleteAble;
		this.isWritAble = isWritAble;
		this.isReadAble = isReadAble;
		if(!isReadAble) {
			return;
		}
		this.id = entity.getId();
		this.dispensingDate = entity.getDispensingDate();
		this.selfTestSource = entity.getSelfTestSource();

		if (entity.getOrganization() != null) {
			this.organization = new OrganizationDto();
			this.organization.setId(entity.getOrganization().getId());
			this.organization.setName(entity.getOrganization().getName());
		}

		if (entity.getDispensingStaff() != null) {
			this.dispensingStaff = new StaffDto();
			this.dispensingStaff.setId(entity.getDispensingStaff().getId());

			if (entity.getDispensingStaff().getPerson() != null) {
				this.dispensingStaff.setFullName(entity.getDispensingStaff().getPerson().getFullname());
			}
		}

		if (entity.getPerson() != null) {
			this.person = new PersonDto(entity.getPerson(), false);
			if(!isViewPII) {
				String notPermission = "-";
				this.person.setFullname(notPermission);
				this.person.setMobilePhone(notPermission);
				Set<LocationDto> locations = new LinkedHashSet<>();
				for(Location location: entity.getPerson().getLocations()) {
					AdminUnit adminUnit = new AdminUnit();
					adminUnit.setName(notPermission);
					location.setStreetAddress(notPermission);
					locations.add(new LocationDto(location));
				}
				this.person.setLocations(locations);
//				Set<LocationDto> a = new LinkedHashSet<>();
//				a.add(new LocationDto(entity.getPerson().getLocations()));

			}
		}

		if (entity.getSpecimens() != null) {
			Iterator<SelfTestSpecimen> itr = entity.getSpecimens().iterator();

			while (itr.hasNext()) {
				this.specimens.add(new SelfTestSpecimenDto(itr.next()));
			}
		}
	}

	public SelfTestEntry toEntity() {
		SelfTestEntry entity = new SelfTestEntry();
		entity = (SelfTestEntry) super.toEntity(entity);

		entity.setId(id);
		entity.setDispensingDate(dispensingDate);

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

	public StaffDto getDispensingStaff() {
		return dispensingStaff;
	}

	public void setDispensingStaff(StaffDto dispensingStaff) {
		this.dispensingStaff = dispensingStaff;
	}

	public LocalDateTime getDispensingDate() {
		return dispensingDate;
	}

	public void setDispensingDate(LocalDateTime dispensingDate) {
		this.dispensingDate = dispensingDate;
	}

	public PersonDto getPerson() {
		return person;
	}

	public void setPerson(PersonDto person) {
		this.person = person;
	}

	public Set<SelfTestSpecimenDto> getSpecimens() {
		return specimens;
	}

	public void setSpecimens(Set<SelfTestSpecimenDto> specimens) {
		this.specimens = specimens;
	}

	public SelfTestSource getSelfTestSource() {
		return selfTestSource;
	}

	public void setSelfTestSource(SelfTestSource selfTestSource) {
		this.selfTestSource = selfTestSource;
	}

	public String getSelfTestSourceDes() {
		if (selfTestSource != null) {
			selfTestSourceDes = selfTestSource.getDescription();
		}
		return selfTestSourceDes;
	}

	public boolean isReadAble() {
		return isReadAble;
	}

	public void setReadAble(boolean isReadAble) {
		this.isReadAble = isReadAble;
	}

	public boolean isWritAble() {
		return isWritAble;
	}

	public void setWritAble(boolean isWritAble) {
		this.isWritAble = isWritAble;
	}

	public boolean isDeleteAble() {
		return isDeleteAble;
	}

	public void setDeleteAble(boolean isDeleteAble) {
		this.isDeleteAble = isDeleteAble;
	}
	
}
