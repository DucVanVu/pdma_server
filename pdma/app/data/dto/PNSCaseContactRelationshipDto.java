package org.pepfar.pdma.app.data.dto;

import org.pepfar.pdma.app.data.domain.PNSCaseContact;
import org.pepfar.pdma.app.data.domain.PNSCaseContactRelationship;
import org.pepfar.pdma.app.data.types.PNSCaseContactRelationshipType;

public class PNSCaseContactRelationshipDto {
	private Long id;

	private PNSCaseContact pnsCaseContact;
	
	private PNSCaseContactRelationshipType val;
	
	private String name;

	private Boolean isMainRisk;
	
	public PNSCaseContactRelationshipDto() {
		super();
	}
	
	public PNSCaseContactRelationshipDto(PNSCaseContactRelationship entity,boolean simple) {
		super();
		if(entity!=null) {
			this.id = entity.getId();		
			this.val = entity.getVal();
			this.name = entity.getName();
			if(!simple) {
				if(entity.getPnsCaseContact()!=null) {
					this.pnsCaseContact = new PNSCaseContact();
					this.pnsCaseContact.setId(entity.getPnsCaseContact().getId());
				}
			}
		}		
	}
	
	public PNSCaseContactRelationshipDto(Long id, PNSCaseContact pnsCaseContact, PNSCaseContactRelationshipType val,
			String name) {
		super();
		this.id = id;
		this.pnsCaseContact = pnsCaseContact;
		this.val = val;
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public PNSCaseContact getPnsCaseContact() {
		return pnsCaseContact;
	}

	public void setPnsCaseContact(PNSCaseContact pnsCaseContact) {
		this.pnsCaseContact = pnsCaseContact;
	}

	public PNSCaseContactRelationshipType getVal() {
		return val;
	}

	public void setVal(PNSCaseContactRelationshipType val) {
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
