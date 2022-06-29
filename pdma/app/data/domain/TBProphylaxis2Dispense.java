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
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.pepfar.pdma.app.data.domain.auditing.AuditableEntity;
import org.pepfar.pdma.app.data.types.TBProphylaxisResumeReason;
import org.pepfar.pdma.app.data.types.TBProphylaxisStopReason;
import org.pepfar.pdma.app.utils.LocalDateTimeAttributeConverter;
import org.pepfar.pdma.app.utils.UUIDAttributeConverter;
import org.springframework.data.annotation.Transient;
/*
 * Cấp phát thuốc dự phòng lao
 */
@Entity
@Table(name = "tbl_prophylaxis_2_dispense")
public class TBProphylaxis2Dispense extends AuditableEntity {

	@Transient
	private static final long serialVersionUID = -4821666803768046236L;

	@Id
	@GenericGenerator(name = "native", strategy = "native")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "native")
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Convert(converter = UUIDAttributeConverter.class)
	@Column(name = "uuid", unique = true, nullable = false, updatable = false, columnDefinition = "char(36)")
	private UUID uid;

	@ManyToOne
	@JoinColumn(name = "round_id", nullable = false)
	private TBProphylaxis2 round;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "record_date", nullable = false)
	private LocalDateTime recordDate;

	@Column(name = "dispensed", nullable = false)
	private boolean dispensed;

	@Column(name = "stop_reason", nullable = true)
	@Enumerated(EnumType.STRING)
	private TBProphylaxisStopReason stopReason;

	@Column(name = "dispensed_doses", nullable = true)//sửa lại vì có trường hợp ngưng điều trị thì không có
	private Integer dispensedDoses;

	@Column(name = "resume_reason", nullable = true)
	@Enumerated(EnumType.STRING)
	private TBProphylaxisResumeReason resumeReason;

	@Column(name = "note", length = 1024, nullable = true)
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

	public TBProphylaxis2 getRound() {
		return round;
	}

	public void setRound(TBProphylaxis2 round) {
		this.round = round;
	}

	public LocalDateTime getRecordDate() {
		return recordDate;
	}

	public void setRecordDate(LocalDateTime recordDate) {
		this.recordDate = recordDate;
	}

	public boolean isDispensed() {
		return dispensed;
	}

	public void setDispensed(boolean dispensed) {
		this.dispensed = dispensed;
	}

	public TBProphylaxisStopReason getStopReason() {
		return stopReason;
	}

	public void setStopReason(TBProphylaxisStopReason stopReason) {
		this.stopReason = stopReason;
	}

	public Integer getDispensedDoses() {
		return dispensedDoses;
	}

	public void setDispensedDoses(Integer dispensedDoses) {
		this.dispensedDoses = dispensedDoses;
	}

	public TBProphylaxisResumeReason getResumeReason() {
		return resumeReason;
	}

	public void setResumeReason(TBProphylaxisResumeReason resumeReason) {
		this.resumeReason = resumeReason;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

}
