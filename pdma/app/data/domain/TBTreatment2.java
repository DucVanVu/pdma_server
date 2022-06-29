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
import org.pepfar.pdma.app.data.types.TBClassification1;
import org.pepfar.pdma.app.data.types.TBClassification2;
import org.pepfar.pdma.app.data.types.TBClassification3;
import org.pepfar.pdma.app.data.types.TBClassification4;
import org.pepfar.pdma.app.data.types.TBExtraDiagnosis;
import org.pepfar.pdma.app.data.types.TBScreeningType;
import org.pepfar.pdma.app.data.types.YesNoNone;
import org.pepfar.pdma.app.utils.LocalDateTimeAttributeConverter;
import org.pepfar.pdma.app.utils.UUIDAttributeConverter;
import org.springframework.data.annotation.Transient;
/*
 * Danh sách các lần Sàng lọc, chẩn đoán, điều trị Lao
 */
@Entity
@Table(name = "tbl_tb_treatment_2")
public class TBTreatment2 extends AuditableEntity {

	@Transient
	private static final long serialVersionUID = 1077800544898023218L;

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

	// Tình huống sàng lọc lao
	@Column(name = "screening_type", nullable = true)
	@Enumerated(EnumType.STRING)
	private TBScreeningType screeningType;

	// Ngày sàng lọc lao
	@Column(name = "screening_date", nullable = true)
	@Convert(converter = LocalDateTimeAttributeConverter.class)
	private LocalDateTime screeningDate;

	// Xét nghiệm soi đờm
	@Column(name = "sputum_smear", nullable = true)
	@Enumerated(EnumType.STRING)
	private YesNoNone sputumSmear;

	// Xét nghiệm Xpert
	@Column(name = "xpert", nullable = true)
	@Enumerated(EnumType.STRING)
	private YesNoNone xpert;

	// Chụp phổi
	@Column(name = "xray", nullable = true)
	@Enumerated(EnumType.STRING)
	private YesNoNone xray;

	// XN chẩn đoán lao khác
	@Column(name = "other_test", nullable = true)
	@Enumerated(EnumType.STRING)
	private TBExtraDiagnosis otherTest;

	// Chẩn đoán mắc lao?
	@Column(name = "tb_diagnosed", nullable = true)
	@Enumerated(EnumType.STRING)
	private YesNoNone tbDiagnosed;

	// Tên cơ sở chẩn đoán Lao
	@Column(name = "tb_diag_facility", length = 255, nullable = true)
	private String tbDiagnosisFacility;

	// Thời điểm chẩn đoán Lao
	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "tb_diagnosis_date", nullable = true)
	private LocalDateTime tbDiagnosisDate;

	// Phân loại bệnh lao theo vị trí giải phẫu
	@Column(name = "classification_1", nullable = true)
	@Enumerated(EnumType.STRING)
	private TBClassification1 classification1;

	// Phân loại bệnh lao (chi tiết theo vị trí giải phẫu, nếu có)
	@Column(name = "classification_2", nullable = true)
	@Enumerated(EnumType.STRING)
	private TBClassification2 classification2;

	// Phân loại bệnh lao (chi tiết theo tiền sử bệnh, nếu có)
	@Column(name = "classification_3", nullable = true)
	@Enumerated(EnumType.STRING)
	private TBClassification3 classification3;

	// Phân loại bệnh lao (chi tiết theo kết quả xét nghiệm vi khuẩn, nếu có)
	@Column(name = "classification_4", nullable = true)
	@Enumerated(EnumType.STRING)
	private TBClassification4 classification4;

	@Column(name = "tb_tx_facility", length = 255, nullable = true)
	private String tbTxFacility;

	@Column(name = "tb_tx_patient_code", length = 100, nullable = true)
	private String tbTxPatientCode;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "tb_tx_start_date", nullable = true)
	private LocalDateTime tbTxStartDate;

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

	public TBScreeningType getScreeningType() {
		return screeningType;
	}

	public void setScreeningType(TBScreeningType screeningType) {
		this.screeningType = screeningType;
	}

	public LocalDateTime getScreeningDate() {
		return screeningDate;
	}

	public void setScreeningDate(LocalDateTime screeningDate) {
		this.screeningDate = screeningDate;
	}

	public YesNoNone getSputumSmear() {
		return sputumSmear;
	}

	public void setSputumSmear(YesNoNone sputumSmear) {
		this.sputumSmear = sputumSmear;
	}

	public YesNoNone getXpert() {
		return xpert;
	}

	public void setXpert(YesNoNone xpert) {
		this.xpert = xpert;
	}

	public YesNoNone getXray() {
		return xray;
	}

	public void setXray(YesNoNone xray) {
		this.xray = xray;
	}

	public TBExtraDiagnosis getOtherTest() {
		return otherTest;
	}

	public void setOtherTest(TBExtraDiagnosis otherTest) {
		this.otherTest = otherTest;
	}

	public YesNoNone getTbDiagnosed() {
		return tbDiagnosed;
	}

	public void setTbDiagnosed(YesNoNone tbDiagnosed) {
		this.tbDiagnosed = tbDiagnosed;
	}

	public String getTbDiagnosisFacility() {
		return tbDiagnosisFacility;
	}

	public void setTbDiagnosisFacility(String tbDiagnosisFacility) {
		this.tbDiagnosisFacility = tbDiagnosisFacility;
	}

	public LocalDateTime getTbDiagnosisDate() {
		return tbDiagnosisDate;
	}

	public void setTbDiagnosisDate(LocalDateTime tbDiagnosisDate) {
		this.tbDiagnosisDate = tbDiagnosisDate;
	}

	public TBClassification1 getClassification1() {
		return classification1;
	}

	public void setClassification1(TBClassification1 classification1) {
		this.classification1 = classification1;
	}

	public TBClassification2 getClassification2() {
		return classification2;
	}

	public void setClassification2(TBClassification2 classification2) {
		this.classification2 = classification2;
	}

	public TBClassification3 getClassification3() {
		return classification3;
	}

	public void setClassification3(TBClassification3 classification3) {
		this.classification3 = classification3;
	}

	public TBClassification4 getClassification4() {
		return classification4;
	}

	public void setClassification4(TBClassification4 classification4) {
		this.classification4 = classification4;
	}

	public String getTbTxFacility() {
		return tbTxFacility;
	}

	public void setTbTxFacility(String tbTxFacility) {
		this.tbTxFacility = tbTxFacility;
	}

	public String getTbTxPatientCode() {
		return tbTxPatientCode;
	}

	public void setTbTxPatientCode(String tbTxPatientCode) {
		this.tbTxPatientCode = tbTxPatientCode;
	}

	public LocalDateTime getTbTxStartDate() {
		return tbTxStartDate;
	}

	public void setTbTxStartDate(LocalDateTime tbTxStartDate) {
		this.tbTxStartDate = tbTxStartDate;
	}

}
