package org.pepfar.pdma.app.data.dto;

import org.pepfar.pdma.app.data.domain.Location;
import org.pepfar.pdma.app.data.domain.Person;
import org.pepfar.pdma.app.data.domain.WRCase;
import org.pepfar.pdma.app.data.types.AddressType;
import org.pepfar.pdma.app.data.types.LocationAccuracy;

public class LocationDto extends AuditableEntityDto {

	private Long id;

	private AddressType addressType;

	private String streetAddress;

	private Double longitude;

	private Double latitude;

	private LocationAccuracy accuracy;

	private AdminUnitDto commune;

	private AdminUnitDto district;

	private AdminUnitDto province;

	private AdminUnitDto country;

	private PersonDto person;

	private WRCaseDto wrCase;

	public LocationDto() {
	}

	public LocationDto(Location entity) {
		super(entity);

		if (entity == null) {
			return;
		}

		this.id = entity.getId();
		this.addressType = entity.getAddressType();
		this.streetAddress = entity.getStreetAddress();
		this.longitude = entity.getLongitude();
		this.latitude = entity.getLatitude();
		this.accuracy = entity.getAccuracy();

		if (entity.getCommune() != null) {
			this.commune = new AdminUnitDto(entity.getCommune());
		}

		if (entity.getDistrict() != null) {
			this.district = new AdminUnitDto(entity.getDistrict());
		}

		if (entity.getProvince() != null) {
			this.province = new AdminUnitDto(entity.getProvince());
		}

		if (entity.getCountry() != null) {
			this.country = new AdminUnitDto(entity.getCountry());
		}

		if (entity.getPerson() != null) {
			this.person = new PersonDto();
			this.person.setId(entity.getPerson().getId());
		}

		if (entity.getWrCase() != null) {
			this.wrCase = new WRCaseDto();
			this.wrCase.setId(entity.getWrCase().getId());
		}
	}

	public Location toEntity() {
		Location entity = new Location();
		entity = (Location) super.toEntity(entity);

		entity.setId(id);
		entity.setAddressType(addressType);
		entity.setStreetAddress(streetAddress);
		entity.setLongitude(longitude);
		entity.setLatitude(latitude);
		entity.setAccuracy(accuracy);

		if (commune != null) {
			entity.setCommune(commune.toEntity());
		}

		if (district != null) {
			entity.setDistrict(district.toEntity());
		}

		if (province != null) {
			entity.setProvince(province.toEntity());
		}

		if (country != null) {
			entity.setCountry(country.toEntity());
		}

		if (person != null) {
			Person _person = new Person();
			_person.setId(person.getId());
			entity.setPerson(_person);
		}

		if (wrCase != null) {
			WRCase _wrCase = new WRCase();
			_wrCase.setId(wrCase.getId());
			entity.setWrCase(_wrCase);
		}

		return entity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStreetAddress() {
		return streetAddress;
	}

	public void setStreetAddress(String streetAddress) {
		this.streetAddress = streetAddress;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public LocationAccuracy getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(LocationAccuracy accuracy) {
		this.accuracy = accuracy;
	}

	public AdminUnitDto getCommune() {
		return commune;
	}

	public void setCommune(AdminUnitDto commune) {
		this.commune = commune;
	}

	public AdminUnitDto getDistrict() {
		return district;
	}

	public void setDistrict(AdminUnitDto district) {
		this.district = district;
	}

	public AdminUnitDto getProvince() {
		return province;
	}

	public void setProvince(AdminUnitDto province) {
		this.province = province;
	}

	public AdminUnitDto getCountry() {
		return country;
	}

	public void setCountry(AdminUnitDto country) {
		this.country = country;
	}

	public AddressType getAddressType() {
		return addressType;
	}

	public void setAddressType(AddressType addressType) {
		this.addressType = addressType;
	}

	public PersonDto getPerson() {
		return person;
	}

	public void setPerson(PersonDto person) {
		this.person = person;
	}

	public WRCaseDto getWrCase() {
		return wrCase;
	}

	public void setWrCase(WRCaseDto wrCase) {
		this.wrCase = wrCase;
	}

}
