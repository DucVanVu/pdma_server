package org.pepfar.pdma.app.data.dto;

import org.pepfar.pdma.app.data.domain.SNSCaseIdNumber;
import org.pepfar.pdma.app.data.types.SNSIdNumberType;

public class SNSCaseIdNumberDto extends AuditableEntityDto {

	private Long id;

	private SNSIdNumberType idNumberType;

	private String value;
	private Boolean primary;
	private SNSCaseDto snsCase;;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Boolean getPrimary() {
		return primary;
	}

	public void setPrimary(Boolean primary) {
		this.primary = primary;
	}

	public SNSCaseDto getSnsCase() {
		return snsCase;
	}

	public void setSnsCase(SNSCaseDto snsCase) {
		this.snsCase = snsCase;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public SNSIdNumberType getIdNumberType() {
		return idNumberType;
	}

	public void setIdNumberType(SNSIdNumberType idNumberType) {
		this.idNumberType = idNumberType;
	}

	public SNSCaseIdNumberDto() {

	}

	public SNSCaseIdNumberDto(SNSCaseIdNumber entity) {
//		super(entity);
		if (entity != null) {
			this.id = entity.getId();
			this.idNumberType = entity.getIdNumberType();
			this.value = entity.getValue();
			this.primary = entity.getPrimary();
			if (entity.getSnsCase() != null) {
				this.snsCase = new SNSCaseDto();
				this.snsCase.setId(entity.getSnsCase().getId());
				this.snsCase.setCouponCode(entity.getSnsCase().getCouponCode());
				this.snsCase.setName(entity.getSnsCase().getName());
			}

		}
	}

}
