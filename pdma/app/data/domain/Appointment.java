package org.pepfar.pdma.app.data.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.pepfar.pdma.app.data.domain.auditing.AuditableEntity;
import org.pepfar.pdma.app.data.types.ARVFundingSource;
import org.pepfar.pdma.app.utils.LocalDateTimeAttributeConverter;
import org.pepfar.pdma.app.utils.UUIDAttributeConverter;

@Entity
@Table(name = "tbl_appointment")
public class Appointment extends AuditableEntity {

	@Transient
	private static final long serialVersionUID = 7156137548510772081L;

	@Id
	@GenericGenerator(name = "native", strategy = "native")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "native")
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Convert(converter = UUIDAttributeConverter.class)
	@Column(name = "uuid", unique = true, nullable = false, updatable = false, columnDefinition = "char(36)")
	private UUID uid;

	@ManyToOne
	@JoinColumn(name = "org_id", nullable = false)
	private Organization organization;

	@ManyToOne
	@JoinColumn(name = "case_id", nullable = false)
	private Case theCase;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "appointment_date", nullable = false)
	private LocalDateTime appointmentDate;

	// Bệnh nhân có tới không?
	@Column(name = "arrived", nullable = true)
	private Boolean arrived;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "arrival_date", nullable = true)
	private LocalDateTime arrivalDate;

	// Bệnh nhân không tới khám sau 28 ngày hẹn?
	@Column(name = "missed", nullable = true)
	private Boolean missed;

	// Có cấp thuốc không?
	@Column(name = "drug_dispensed", nullable = true)
	private Boolean drugDispensed;

	// Có làm xét nghiệm CD4 không?
	@Column(name = "cd4_tested", nullable = true)
	private Boolean cd4Tested;

	// Có làm xét nghiệm TLVR không?
	@Column(name = "vl_tested", nullable = true)
	private Boolean vlTested;

	// Có làm xét nghiệm kháng thuốc ARV không?
	@Column(name = "arv_dr_tested", nullable = true)
	private Boolean arvDrTested;

	// K/qủa sàng lọc lao
	// 1 = Dương tính
	// 2 = Âm tính
	@Column(name = "tb_screen_result", nullable = true)
	private Integer tbScreenResult;

	// Có sàng lọc viêm gan không?
	@Column(name = "hep_screened", nullable = true)
	private Boolean hepScreened;

	// Có thay đổi thông tin thẻ bảo hiểm y tế không?
	@Column(name = "shi_changed", nullable = true)
	private Boolean shiChanged;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "next_appointment_date", nullable = true)
	private LocalDateTime nextAppointmentDate;

	@Column(name = "drug_days", nullable = true)
	private Integer drugDays;

	@Enumerated(EnumType.STRING)
	@Column(name = "drug_source", nullable = true)
	private ARVFundingSource drugSource;

	@Enumerated(EnumType.STRING)
	@Column(name = "drug_source_alt", nullable = true)
	private ARVFundingSource drugSourceAlt;
	
	@Column(name = "arv_regimen_name", length = 100, nullable = true)
	private String arvRegimenName;

	@Column(name = "arv_regimen_line", nullable = true)
	private Integer arvRegimenLine;

	@Column(name = "good_adherence", nullable = true)
	private Boolean goodAdherence;

	@Column(name = "has_oi", nullable = true)
	private Boolean hasOI;

	@Column(name = "has_drug_ae", nullable = true)
	private Boolean hasDrugAE;

	@Column(name = "pregnant", nullable = true)
	private Boolean pregnant;

	@Lob
	@Column(name = "note", nullable = true)
	private String note;

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

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public Case getTheCase() {
		return theCase;
	}

	public void setTheCase(Case theCase) {
		this.theCase = theCase;
	}

	public LocalDateTime getAppointmentDate() {
		return appointmentDate;
	}

	public void setAppointmentDate(LocalDateTime appointmentDate) {
		this.appointmentDate = appointmentDate;
	}

	public Boolean getArrived() {
		return arrived;
	}

	public void setArrived(Boolean arrived) {
		this.arrived = arrived;
	}

	public LocalDateTime getArrivalDate() {
		return arrivalDate;
	}

	public void setArrivalDate(LocalDateTime arrivalDate) {
		this.arrivalDate = arrivalDate;
	}

	public Boolean getMissed() {
		return missed;
	}

	public void setMissed(Boolean missed) {
		this.missed = missed;
	}

	public Boolean getDrugDispensed() {
		return drugDispensed;
	}

	public void setDrugDispensed(Boolean drugDispensed) {
		this.drugDispensed = drugDispensed;
	}

	public Boolean getCd4Tested() {
		return cd4Tested;
	}

	public void setCd4Tested(Boolean cd4Tested) {
		this.cd4Tested = cd4Tested;
	}

	public Boolean getVlTested() {
		return vlTested;
	}

	public void setVlTested(Boolean vlTested) {
		this.vlTested = vlTested;
	}

	public Boolean getArvDrTested() {
		return arvDrTested;
	}

	public void setArvDrTested(Boolean arvDrTested) {
		this.arvDrTested = arvDrTested;
	}

	public Integer getTbScreenResult() {
		return tbScreenResult;
	}

	public void setTbScreenResult(Integer tbScreenResult) {
		this.tbScreenResult = tbScreenResult;
	}

	public Boolean getHepScreened() {
		return hepScreened;
	}

	public void setHepScreened(Boolean hepScreened) {
		this.hepScreened = hepScreened;
	}

	public Boolean getShiChanged() {
		return shiChanged;
	}

	public void setShiChanged(Boolean shiChanged) {
		this.shiChanged = shiChanged;
	}

	public LocalDateTime getNextAppointmentDate() {
		return nextAppointmentDate;
	}

	public void setNextAppointmentDate(LocalDateTime nextAppointmentDate) {
		this.nextAppointmentDate = nextAppointmentDate;
	}

	public Integer getDrugDays() {
		return drugDays;
	}

	public void setDrugDays(Integer drugDays) {
		this.drugDays = drugDays;
	}

	public ARVFundingSource getDrugSource() {
		return drugSource;
	}

	public void setDrugSource(ARVFundingSource drugSource) {
		this.drugSource = drugSource;
	}

	public ARVFundingSource getDrugSourceAlt() {
		return drugSourceAlt;
	}

	public void setDrugSourceAlt(ARVFundingSource drugSourceAlt) {
		this.drugSourceAlt = drugSourceAlt;
	}

	public String getArvRegimenName() {
		return arvRegimenName;
	}

	public void setArvRegimenName(String arvRegimenName) {
		this.arvRegimenName = arvRegimenName;
	}

	public Integer getArvRegimenLine() {
		return arvRegimenLine;
	}

	public void setArvRegimenLine(Integer arvRegimenLine) {
		this.arvRegimenLine = arvRegimenLine;
	}

	public Boolean getGoodAdherence() {
		return goodAdherence;
	}

	public void setGoodAdherence(Boolean goodAdherence) {
		this.goodAdherence = goodAdherence;
	}

	public Boolean getHasOI() {
		return hasOI;
	}

	public void setHasOI(Boolean hasOI) {
		this.hasOI = hasOI;
	}

	public Boolean getHasDrugAE() {
		return hasDrugAE;
	}

	public void setHasDrugAE(Boolean hasDrugAE) {
		this.hasDrugAE = hasDrugAE;
	}

	public Boolean getPregnant() {
		return pregnant;
	}

	public void setPregnant(Boolean pregnant) {
		this.pregnant = pregnant;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31).append(id).append(uid).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null) {
			return false;
		}

		if (!(obj instanceof Appointment)) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		Appointment that = (Appointment) obj;

		return new EqualsBuilder().append(id, that.id).append(uid, that.uid).isEquals();
	}
}
