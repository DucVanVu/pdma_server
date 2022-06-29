package org.pepfar.pdma.app.data.dto;

import org.pepfar.pdma.app.data.domain.PECaseRiskGroup;
import org.pepfar.pdma.app.data.types.PERiskGroupEnum;

public class PECaseRiskGroupDto {
	
	private Long id;
	
	private PECaseDto peCase;
	
	private String name;

	private PERiskGroupEnum val;

	private Boolean isMainRisk;
	
	public PECaseRiskGroupDto() {
		super();
	}
	
	public PECaseRiskGroupDto(PECaseRiskGroup entity) {
		if (entity == null) {
			return;
		}		
		this.id = entity.getId();		
		if(entity.getPeCase() != null) {
			this.peCase = new PECaseDto();
			this.peCase.setId(entity.getPeCase().getId());
			
		}
		this.name = entity.getName();
		this.val = entity.getVal();
		this.isMainRisk = entity.getIsMainRisk();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public PECaseDto getPeCase() {
		return peCase;
	}

	public void setPeCase(PECaseDto peCase) {
		this.peCase = peCase;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PERiskGroupEnum getVal() {
		return val;
	}

	public void setVal(PERiskGroupEnum val) {
		this.val = val;
	}

	public Boolean getIsMainRisk() {
		return isMainRisk;
	}

	public void setIsMainRisk(Boolean isMainRisk) {
		this.isMainRisk = isMainRisk;
	}
}
