package org.pepfar.pdma.app.data.dto;

import org.pepfar.pdma.app.data.domain.HIVConfirmLab;

public class HIVConfirmLabDto extends AuditableEntityDto
{

	private Long id;

	private String code;

	private String name;

	private LocationDto address;

	public HIVConfirmLabDto() {
	}

	public HIVConfirmLabDto(HIVConfirmLab entity) {
		super(entity);

		if (entity == null) {
			return;
		}

		this.id = entity.getId();
		this.code = entity.getCode();
		this.name = entity.getName();

		if (entity.getAddress() != null) {
			this.address = new LocationDto(entity.getAddress());
		}
	}

	public HIVConfirmLab toEntity() {
		HIVConfirmLab entity = new HIVConfirmLab();
		entity = (HIVConfirmLab) super.toEntity(entity);

		entity.setId(id);
		entity.setCode(code);
		entity.setName(name);

		if (address != null) {
			entity.setAddress(address.toEntity());
		}

		return entity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LocationDto getAddress() {
		return address;
	}

	public void setAddress(LocationDto address) {
		this.address = address;
	}

}
