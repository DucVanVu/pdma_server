package org.pepfar.pdma.app.data.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.pepfar.pdma.app.data.domain.auditing.AuditableEntity;
import org.pepfar.pdma.app.utils.LocalDateTimeAttributeConverter;
import org.pepfar.pdma.app.utils.UUIDAttributeConverter;

@Entity
@Table(name = "tbl_mmt")
public class MMT extends AuditableEntity {

	@Transient
	private static final long serialVersionUID = -716994943886259628L;

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

	@OneToOne
	@JoinColumn(name = "case_id", nullable = false)
	private Case theCase;

	@Column(name = "on_mmt", nullable = false)
	private boolean onMMT;

	@Column(name = "stopped_mmt", nullable = true)
	private boolean stoppedMMT;

	@Column(name = "mmt_patient_code", length = 100, nullable = true)
	private String mmtPatientCode;

	@Column(name = "facility_name", length = 255, nullable = true)
	private String facilityName;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "start_date", nullable = true)
	private LocalDateTime startDate;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "end_date", nullable = true)
	private LocalDateTime endDate;

	@Column(name = "steady_dose", nullable = true)
	private int steadyDose;

	@Column(name = "dose_before_stop", nullable = true)
	private int doseBeforeStop;

	@ManyToOne()
	@JoinColumn(name = "reason_for_stop", nullable = true)
	private Dictionary reasonForStop;

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

	public Dictionary getReasonForStop() {
		return reasonForStop;
	}

	public void setReasonForStop(Dictionary reasonForStop) {
		this.reasonForStop = reasonForStop;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

}
