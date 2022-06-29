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
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.pepfar.pdma.app.data.domain.auditing.AuditableEntity;
import org.pepfar.pdma.app.utils.LocalDateTimeAttributeConverter;
import org.pepfar.pdma.app.utils.UUIDAttributeConverter;

@Entity
@Table(name = "tbl_multimonth_dispensing")
public class MMDispensing extends AuditableEntity {

	@Transient
	private static final long serialVersionUID = -4648410761566377336L;

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

	// Is adult (>= 15 yo) when evaluated?
	@Column(name = "is_adult", nullable = true)
	private boolean adult;

	// Start ARV >= 12 months
	// or if returned to treatment, date of ARV when returning >= 12 months when
	// evaluated
	@Column(name = "is_arv_12month", nullable = true)
	private boolean arvGt12Month;

	// Latest VL test < 200 when evaluated
	@Column(name = "is_vl_lt200", nullable = true)
	private boolean vlLt200;

	// No OI when evaluated
	@Column(name = "is_oi_free", nullable = true)
	private boolean noOIs;

	// No drug adverse events
	@Column(name = "is_drug_ae_free", nullable = true)
	private boolean noDrugAdvEvent;

	// No pregnancy/breast feeding?
	@Column(name = "is_pregnance_free", nullable = true)
	private boolean noPregnancy;

	// Good adherance
	@Column(name = "is_good_adherance", nullable = true)
	private boolean goodAdherence;

	// Conclusion
	@Column(name = "eligible", nullable = true)
	private boolean eligible;

	// Ngày đánh giá tiêu chuẩn cấp thuốc 3 tháng
	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "eval_date", nullable = true)
	private LocalDateTime evaluationDate;

	@Column(name = "on_mmd", nullable = true)
	private boolean onMmd;

	// Ngày bắt đầu được nhận thuốc nhiều tháng
//	@Convert(converter = LocalDateTimeAttributeConverter.class)
//	@Column(name = "start_date", nullable = true)
//	private LocalDateTime startDate;

	// Ngày kết thúc nhận thuốc nhiều tháng
//	@Convert(converter = LocalDateTimeAttributeConverter.class)
//	@Column(name = "end_date", nullable = true)
//	private LocalDateTime endDate;

	@Column(name = "stop_reason", nullable = true)
	private String stopReason;

	@Column(name = "deleted", nullable = true)
	private Boolean deleted;

	// Associated appointment entry ID
	@Column(name = "correspond_appointment_id", nullable = true)
	private Long appointmentId;

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

	public void setGoodAdherence(boolean goodAdherance) {
		this.goodAdherence = goodAdherance;
	}

	public boolean isEligible() {
		return eligible;
	}

	public void setEligible(boolean eligible) {
		this.eligible = eligible;
	}

	public boolean isOnMmd() {
		return onMmd;
	}

	public void setOnMmd(boolean onMmd) {
		this.onMmd = onMmd;
	}

	public LocalDateTime getEvaluationDate() {
		return evaluationDate;
	}

	public void setEvaluationDate(LocalDateTime evaluationDate) {
		this.evaluationDate = evaluationDate;
	}

//	public LocalDateTime getStartDate() {
//		return startDate;
//	}
//
//	public void setStartDate(LocalDateTime startDate) {
//		this.startDate = startDate;
//	}
//
//	public LocalDateTime getEndDate() {
//		return endDate;
//	}
//
//	public void setEndDate(LocalDateTime endDate) {
//		this.endDate = endDate;
//	}

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

}
