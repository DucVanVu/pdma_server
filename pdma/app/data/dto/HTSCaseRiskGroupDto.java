package org.pepfar.pdma.app.data.dto;

import org.pepfar.pdma.app.data.domain.HTSCaseRiskGroup;
import org.pepfar.pdma.app.data.types.HTSRiskGroupEnum;

public class HTSCaseRiskGroupDto {
	private Long id;
	
	private HTSCaseDto htsCase;
	
	private HTSRiskGroupEnum val;
	
	private String name;
	
	private Boolean isMainRisk;

	public HTSCaseRiskGroupDto() {
			
	}
	
	public HTSCaseRiskGroupDto(HTSCaseRiskGroup entity) {
		
		if (entity == null) {
			return;
		}
		
		this.id = entity.getId();
		
		if(entity.getHtsCase() != null) {
			this.htsCase = new HTSCaseDto();
			this.htsCase.setId(entity.getHtsCase().getId());
			
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

	public HTSCaseDto getHtsCase() {
		return htsCase;
	}

	public void setHtsCase(HTSCaseDto htsCase) {
		this.htsCase = htsCase;
	}

	public HTSRiskGroupEnum getVal() {
		return val;
	}

	public void setVal(HTSRiskGroupEnum val) {
		this.val = val;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getIsMainRisk() {
		return isMainRisk;
	}

	public void setIsMainRisk(Boolean isMainRisk) {
		this.isMainRisk = isMainRisk;
	}
}
