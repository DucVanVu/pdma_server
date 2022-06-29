package org.pepfar.pdma.app.data.dto;

import org.pepfar.pdma.app.data.domain.PNSCaseRiskGroup;
import org.pepfar.pdma.app.data.types.HTSRiskGroupEnum;

public class PNSCaseRiskGroupDto {
	private Long id;
	
	private PNSCaseDto pnsCase;
	
	private HTSRiskGroupEnum val;
	
	private String name;
	
	private Boolean isMainRisk;
	
	public PNSCaseRiskGroupDto() {			
	}
	
	public PNSCaseRiskGroupDto(PNSCaseRiskGroup entity) {
		super();
		this.id = entity.getId();
		if(entity.getPnsCase()!=null) {
			this.pnsCase = new PNSCaseDto();
			this.pnsCase.setId(entity.getPnsCase().getId());
		}
		this.val = entity.getVal();
		this.name = entity.getName();
		this.isMainRisk = entity.getIsMainRisk();
	}

	public PNSCaseRiskGroupDto(Long id, PNSCaseDto pnsCase, HTSRiskGroupEnum val, String name) {
		super();
		this.id = id;
		this.pnsCase = pnsCase;
		this.val = val;
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public PNSCaseDto getPnsCase() {
		return pnsCase;
	}

	public void setPnsCase(PNSCaseDto pnsCase) {
		this.pnsCase = pnsCase;
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
