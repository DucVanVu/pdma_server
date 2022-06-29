package org.pepfar.pdma.app.data.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import org.pepfar.pdma.app.data.domain.MMDispensing;
import org.pepfar.pdma.app.utils.CustomLocalDateTimeDeserializer2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class MMDispensingDto extends AuditableEntityDto {

	private Long id;

	private UUID uid;

	private OrganizationDto organization;

	private CaseDto theCase;

	private boolean adult;

	private boolean arvGt12Month;

	private boolean vlLt200;

	private boolean noOIs;

	private boolean noDrugAdvEvent;

	private boolean noPregnancy;

	private boolean goodAdherence;

	private boolean eligible;

	// Ngày đánh giá đủ tiêu chuẩn
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime evaluationDate;

	private boolean onMmd;

	private String stopReason;

	private Boolean deleted;

	private Long appointmentId;

	// to mark the latest row
	private boolean latest;

	public MMDispensingDto() {

	}

	public MMDispensingDto(MMDispensing entity) {
		super(entity);

		if (entity == null) {
			return;
		}

		this.id = entity.getId();
		this.uid = entity.getUid();
		this.adult = entity.isAdult();
		this.arvGt12Month = entity.isArvGt12Month();
		this.vlLt200 = entity.isVlLt200();
		this.noOIs = entity.isNoOIs();
		this.noDrugAdvEvent = entity.isNoDrugAdvEvent();
		this.noPregnancy = entity.isNoPregnancy();
		this.goodAdherence = entity.isGoodAdherence();
		this.eligible = entity.isEligible();
		this.evaluationDate = entity.getEvaluationDate();
		this.onMmd = entity.isOnMmd();
		this.stopReason = entity.getStopReason();
		this.deleted = entity.getDeleted();
		this.appointmentId = entity.getAppointmentId();

		if (entity.getTheCase() != null) {
			this.theCase = new CaseDto();
			this.theCase.setId(entity.getTheCase().getId());
		}

		if (entity.getOrganization() != null) {
			this.organization = new OrganizationDto();
			this.organization.setId(entity.getOrganization().getId());
		}
	}

	public MMDispensing toEntity() {

		MMDispensing entity = new MMDispensing();
		entity = (MMDispensing) super.toEntity(entity);

		entity.setId(id);
		entity.setUid(uid);
		entity.setAdult(adult);
		entity.setArvGt12Month(arvGt12Month);
		entity.setVlLt200(vlLt200);
		entity.setNoOIs(noOIs);
		entity.setNoDrugAdvEvent(noDrugAdvEvent);
		entity.setNoPregnancy(noPregnancy);
		entity.setGoodAdherence(goodAdherence);
		entity.setEligible(eligible);
		entity.setEvaluationDate(evaluationDate);
		entity.setOnMmd(onMmd);
		entity.setStopReason(stopReason);
		entity.setDeleted(deleted);
		entity.setAppointmentId(appointmentId);

		if (theCase != null) {
			entity.setTheCase(theCase.toEntity());
		}

		if (organization != null) {
			entity.setOrganization(organization.toEntity());
		}

		return entity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public UUID getUid() {
		return uid;
	}

	public void setUid(UUID uid) {
		this.uid = uid;
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

	public boolean isAdult() {
		return adult;
	}

	public void setAdult(boolean adult) {
		this.adult = adult;
	}

	public boolean isArvGt12Month() {
		return arvGt12Month;
	}

	public void setArvGt12Month(boolean arvGt12Month) {
		this.arvGt12Month = arvGt12Month;
	}

	public boolean isVlLt200() {
		return vlLt200;
	}

	public void setVlLt200(boolean vlLt200) {
		this.vlLt200 = vlLt200;
	}

	public boolean isNoOIs() {
		return noOIs;
	}

	public void setNoOIs(boolean noOIs) {
		this.noOIs = noOIs;
	}

	public boolean isNoDrugAdvEvent() {
		return noDrugAdvEvent;
	}

	public void setNoDrugAdvEvent(boolean noDrugAdvEvent) {
		this.noDrugAdvEvent = noDrugAdvEvent;
	}

	public boolean isNoPregnancy() {
		return noPregnancy;
	}

	public void setNoPregnancy(boolean noPregnancy) {
		this.noPregnancy = noPregnancy;
	}

	public boolean isGoodAdherence() {
		return goodAdherence;
	}

	public void setGoodAdherence(boolean goodAdherence) {
		this.goodAdherence = goodAdherence;
	}

	public void setEligible(boolean eligible) {
		this.eligible = eligible;
	}

	public void setOnMmd(boolean onMmd) {
		this.onMmd = onMmd;
	}

	public Boolean getEligible() {
		return eligible;
	}

	public void setEligible(Boolean eligible) {
		this.eligible = eligible;
	}

	public LocalDateTime getEvaluationDate() {
		return evaluationDate;
	}

	public void setEvaluationDate(LocalDateTime evaluationDate) {
		this.evaluationDate = evaluationDate;
	}

	public Boolean getOnMmd() {
		return onMmd;
	}

	public void setOnMmd(Boolean onMmd) {
		this.onMmd = onMmd;
	}

	public String getStopReason() {
		return stopReason;
	}

	public void setStopReason(String stopReason) {
		this.stopReason = stopReason;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public Long getAppointmentId() {
		return appointmentId;
	}

	public void setAppointmentId(Long appointmentId) {
		this.appointmentId = appointmentId;
	}

	public boolean isLatest() {
		return latest;
	}

	public void setLatest(boolean latest) {
		this.latest = latest;
	}

}
