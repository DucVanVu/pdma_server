package org.pepfar.pdma.app.data.dto;

import org.pepfar.pdma.app.data.domain.AdminUnit;

public class AdminUnitDto extends AuditableEntityDto
{

	private Long id;
	
	private String codeGso;

	private String code;

	private String name;

	private Integer level;

	private Boolean voided;

	private AdminUnitDto parent;

	private Integer orderNumber;

	public AdminUnitDto() {
	}

	public AdminUnitDto(AdminUnit entity) {
		super(entity);

		if (entity == null) {
			return;
		}

		this.id = entity.getId();
		this.codeGso = entity.getCodeGso();
		this.code = entity.getCode();
		this.name = entity.getName();
		this.level = entity.getLevel();
		this.voided = entity.getVoided();
		this.orderNumber = entity.getOrderNumber();

		if (entity.getParent() != null) {
			AdminUnit parent = entity.getParent();
			this.parent = new AdminUnitDto();
			this.parent.id = parent.getId();
			this.parent.name = parent.getName();
			this.parent.level = parent.getLevel();
			this.parent.voided = parent.getVoided();
			this.parent.orderNumber = parent.getOrderNumber();
		}
	}
	public AdminUnitDto(AdminUnit entity,Boolean simple) {
		super(entity);

		if (entity == null) {
			return;
		}
		this.id = entity.getId();
		this.codeGso = entity.getCodeGso();
		this.code = entity.getCode();
		this.name = entity.getName();
		this.level = entity.getLevel();
		this.voided = entity.getVoided();
		this.orderNumber = entity.getOrderNumber();
		if(!simple) {
			if (entity.getParent() != null) {
				AdminUnit parent = entity.getParent();
				this.parent = new AdminUnitDto();
				this.parent.id = parent.getId();
				this.parent.name = parent.getName();
				this.parent.level = parent.getLevel();
				this.parent.voided = parent.getVoided();
				this.parent.orderNumber = parent.getOrderNumber();
			}
		}		
	}

	public AdminUnit toEntity() {
		AdminUnit entity = new AdminUnit();
		entity = (AdminUnit) super.toEntity(entity);

		entity.setId(id);
		entity.setCodeGso(codeGso);
		entity.setCode(code);
		entity.setName(name);
		entity.setLevel(level);
		entity.setVoided(voided);
		entity.setOrderNumber(orderNumber);

		if (parent != null) {
			AdminUnit pEntity = new AdminUnit();
			pEntity.setId(parent.getId());

			entity.setParent(pEntity);
		}

		return entity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCodeGso() {
		return codeGso;
	}

	public void setCodeGso(String codeGso) {
		this.codeGso = codeGso;
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

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public Boolean getVoided() {
		return voided;
	}

	public void setVoided(Boolean voided) {
		this.voided = voided;
	}

	public AdminUnitDto getParent() {
		return parent;
	}

	public void setParent(AdminUnitDto parent) {
		this.parent = parent;
	}

	public Integer getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(Integer orderNumber) {
		this.orderNumber = orderNumber;
	}
}
