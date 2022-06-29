package org.pepfar.pdma.app.data.dto;

import java.time.LocalDateTime;

import org.pepfar.pdma.app.data.domain.MMT;
import org.pepfar.pdma.app.utils.CustomLocalDateTimeDeserializer2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class MMTDto extends AuditableEntityDto {

	private Long id;

	private OrganizationDto organization;

	private CaseDto theCase;

	private boolean onMMT;

	private boolean stoppedMMT;

	private String mmtPatientCode;

	private String facilityName;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime startDate;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime endDate;

	private int steadyDose;

	private int doseBeforeStop;

	private DictionaryDto reasonForStop;

	private String note;

	public MMTDto() {

	}

	public MMTDto(MMT entity) {
		super(entity);

		if (entity == null) {
			return;
		}

		this.id = entity.getId();
		this.onMMT = entity.isOnMMT();
		this.stoppedMMT = entity.isStoppedMMT();
		this.facilityName = entity.getFacilityName();
		this.mmtPatientCode = entity.getMmtPatientCode();
		this.startDate = entity.getStartDate();
		this.endDate = entity.getEndDate();
		this.steadyDose = entity.getSteadyDose();
		this.doseBeforeStop = entity.getDoseBeforeStop();
		this.note = entity.getNote();

		if (entity.getOrganization() != null) {
			this.organization = new OrganizationDto();
			this.organization.setId(entity.getOrganization().getId());
			this.organization.setName(entity.getOrganization().getName());
		}

		if (entity.getTheCase() != null) {
			this.theCase = new CaseDto();
			this.theCase.setId(entity.getTheCase().getId());
		}

		if (entity.getReasonForStop() != null) {
			this.reasonForStop = new DictionaryDto(entity.getReasonForStop());
		}
	}

	public MMT toEntity() {
		MMT entity = new MMT();

		entity.setId(id);
		entity.setOnMMT(onMMT);
		entity.setStoppedMMT(stoppedMMT);
		entity.setFacilityName(facilityName);
		entity.setMmtPatientCode(mmtPatientCode);
		entity.setStartDate(startDate);
		entity.setEndDate(endDate);
		entity.setSteadyDose(steadyDose);
		entity.setDoseBeforeStop(doseBeforeStop);
		entity.setNote(note);

		if (organization != null) {
			entity.setOrganization(organization.toEntity());
		}

		if (theCase != null) {
			entity.setTheCase(theCase.toEntity());
		}

		if (reasonForStop != null) {
			entity.setReasonForStop(reasonForStop.toEntity());
		}

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

	public CaseDto getTheCase() {
		return theCase;
	}

	public void setTheCase(CaseDto theCase) {
		this.theCase = theCase;
	}

	public boolean isOnMMT() {
		return onMMT;
	}

	public void setOnMMT(boolean onMMT) {
		this.onMMT = onMMT;
	}

	public boolean isStoppedMMT() {
		return stoppedMMT;
	}

	public void setStoppedMMT(boolean stoppedMMT) {
		this.stoppedMMT = stoppedMMT;
	}

	public String getMmtPatientCode() {
		return mmtPatientCode;
	}

	public void setMmtPatientCode(String mmtPatientCode) {
		this.mmtPatientCode = mmtPatientCode;
	}

	public String getFacilityName() {
		return facilityName;
	}

	public void setFacilityName(String facilityName) {
		this.facilityName = facilityName;
	}

	public LocalDateTime getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDateTime startDate) {
		this.startDate = startDate;
	}

	public LocalDateTime getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDateTime endDate) {
		this.endDate = endDate;
	}

	public int getSteadyDose() {
		return steadyDose;
	}

	public void setSteadyDose(int steadyDose) {
		this.steadyDose = steadyDose;
	}

	public int getDoseBeforeStop() {
		return doseBeforeStop;
	}

	public void setDoseBeforeStop(int doseBeforeStop) {
		this.doseBeforeStop = doseBeforeStop;
	}

	public DictionaryDto getReasonForStop() {
		return reasonForStop;
	}

	public void setReasonForStop(DictionaryDto reasonForStop) {
		this.reasonForStop = reasonForStop;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

}
