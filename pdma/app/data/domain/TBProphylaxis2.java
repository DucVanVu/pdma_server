package org.pepfar.pdma.app.data.domain;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
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
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.pepfar.pdma.app.data.domain.auditing.AuditableEntity;
import org.pepfar.pdma.app.data.types.TBProphylaxisRegimen;
import org.pepfar.pdma.app.utils.LocalDateTimeAttributeConverter;
import org.pepfar.pdma.app.utils.UUIDAttributeConverter;
import org.springframework.data.annotation.Transient;
/*
 * Điều trị dự phòng lao
 */
@Entity
@Table(name = "tbl_tb_prophylaxis_2")
public class TBProphylaxis2 extends AuditableEntity {

	@Transient
	private static final long serialVersionUID = -2249977535672871227L;

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

	@Column(name = "regimen", nullable = false)
	@Enumerated(EnumType.STRING)
	private TBProphylaxisRegimen regimen;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "start_date", nullable = false)
	private LocalDateTime startDate;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "end_date", nullable = true)
	private LocalDateTime endDate;

	@Column(name = "note", length = 1024, nullable = true)
	private String note;
	
	@OneToMany(mappedBy = "round")
	@OrderBy("recordDate DESC")
	private Set<TBProphylaxis2Dispense> dispenses = new LinkedHashSet<>();
	
	@Column(name = "complete", nullable = false)
	private Boolean complete;//hoàn thành điều trị( =true: hoàn thành điều trị; =false : bỏ trị; =null chưa hoàn thành)
	
	@Column(name = "status", nullable = false)
	private int status;//trạng thái =0 Chưa bắt đầu (khi chưa cấp thuốc);=1 Đang điều trị; =2 :Ngưng điều trị; =3: Hoàn thành điều trị; =4 Bỏ điều trị

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

	public TBProphylaxisRegimen getRegimen() {
		return regimen;
	}

	public void setRegimen(TBProphylaxisRegimen regimen) {
		this.regimen = regimen;
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

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public Set<TBProphylaxis2Dispense> getDispenses() {
		return dispenses;
	}

	public void setDispenses(Set<TBProphylaxis2Dispense> dispenses) {
		this.dispenses = dispenses;
	}

	public Boolean getComplete() {
		return complete;
	}

	public void setComplete(Boolean complete) {
		this.complete = complete;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
	
}
