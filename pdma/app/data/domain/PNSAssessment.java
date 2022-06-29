package org.pepfar.pdma.app.data.domain;

import java.time.LocalDateTime;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.pepfar.pdma.app.data.domain.auditing.AuditableEntity;
import org.pepfar.pdma.app.utils.LocalDateTimeAttributeConverter;

@Entity
@Table(name = "tbl_pns_assessment")
public class PNSAssessment extends AuditableEntity {

	@Transient
	private static final long serialVersionUID = 5898613579347303210L;

	@Id
	@GenericGenerator(name = "native", strategy = "native")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "native")
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	// General information

	@Column(name = "implementing_partner", length = 250, nullable = false)
	private String ip;

	@ManyToOne
	@JoinColumn(name = "facility_id", nullable = false)
	private Organization facility;

	@Column(name = "assessor_name", length = 100, nullable = false)
	private String assessorName;

	@Column(name = "assessor_email", length = 250, nullable = false)
	private String assessorEmail;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "assessment_date", nullable = false)
	private LocalDateTime assessmentDate;

	@Column(name = "facility_poc_name", length = 100, nullable = false)
	private String facilityPocName;

	@Column(name = "counselor_count", nullable = false)
	private int counselorCount;

	@Column(name = "counselor_for_pns", nullable = false)
	private int counselor4Pns;

	@Column(name = "submitted", nullable = false)
	private Boolean submitted;

	// 0 = Baseline, 1 = Mid-term, 2 = Post-intervention, 3 = Routine
	@Column(name = "assessment_type", nullable = false)
	private int assessmentType;

	// Section 1

	// --> Question 1.1
	// 1 - Yes/All/Always
	// 2 - Some/Sometimes
	// 3 - No/None/Never
	@Column(name = "q_1_1a", nullable = true)
	private int q1_1a;

	@Lob
	@Column(name = "q_1_1a_text", nullable = true)
	private String q1_1a_text;

	// 1 - Yes/All/Always
	// 2 - Some/Sometimes
	// 3 - No/None/Never
	@Column(name = "q_1_1b", nullable = true)
	private int q1_1b;

	@Lob
	@Column(name = "q_1_1b_text", nullable = true)
	private String q1_1b_text;

	// 1 - Yes/All/Always
	// 2 - Some/Sometimes
	// 3 - No/None/Never
	@Column(name = "q_1_1c", nullable = true)
	private int q1_1c;

	@Lob
	@Column(name = "q_1_1c_text", nullable = true)
	private String q1_1c_text;

	// 1 - Yes/All/Always
	// 2 - Some/Sometimes
	// 3 - No/None/Never
	@Column(name = "q_1_1d", nullable = true)
	private int q1_1d;

	@Lob
	@Column(name = "q_1_1d_text", nullable = true)
	private String q1_1d_text;

	// --> Question 1.2
	// 1 - Yes/All/Always
	// 2 - Some/Sometimes
	// 3 - No/None/Never
	@Column(name = "q_1_2a", nullable = true)
	private int q1_2a;

	@Lob
	@Column(name = "q_1_2a_text", nullable = true)
	private String q1_2a_text;

	// 1 - Yes/All/Always
	// 2 - Some/Sometimes
	// 3 - No/None/Never
	@Column(name = "q_1_2b", nullable = true)
	private int q1_2b;

	@Lob
	@Column(name = "q_1_2b_text", nullable = true)
	private String q1_2b_text;

	// 1 - Yes/All/Always
	// 2 - Some/Sometimes
	// 3 - No/None/Never
	@Column(name = "q_1_2c", nullable = true)
	private int q1_2c;

	@Lob
	@Column(name = "q_1_2c_text", nullable = true)
	private String q1_2c_text;

	// 1 - Yes/All/Always
	// 2 - Some/Sometimes
	// 3 - No/None/Never
	@Column(name = "q_1_2d", nullable = true)
	private int q1_2d;

	@Lob
	@Column(name = "q_1_2d_text", nullable = true)
	private String q1_2d_text;

	// 1 - Yes/All/Always
	// 2 - Some/Sometimes
	// 3 - No/None/Never
	@Column(name = "q_1_2e", nullable = true)
	private Integer q1_2e;

	@Lob
	@Column(name = "q_1_2e_text", nullable = true)
	private String q1_2e_text;

	// --> Question 1.3
	// 1 - Yes/All/Always
	// 2 - Some/Sometimes
	// 3 - No/None/Never
	@Column(name = "q_1_3a", nullable = true)
	private int q1_3a;

	@Lob
	@Column(name = "q_1_3a_text", nullable = true)
	private String q1_3a_text;

	// 1 - Yes/All/Always
	// 2 - Some/Sometimes
	// 3 - No/None/Never
	@Column(name = "q_1_3b", nullable = true)
	private int q1_3b;

	@Lob
	@Column(name = "q_1_3b_text", nullable = true)
	private String q1_3b_text;

	// 1 - Yes/All/Always
	// 2 - Some/Sometimes
	// 3 - No/None/Never
	@Column(name = "q_1_3c", nullable = true)
	private int q1_3c;

	@Lob
	@Column(name = "q_1_3c_text", nullable = true)
	private String q1_3c_text;

	// 1 - Yes/All/Always
	// 2 - Some/Sometimes
	// 3 - No/None/Never
	@Column(name = "q_1_3d", nullable = true)
	private int q1_3d;

	@Lob
	@Column(name = "q_1_3d_text", nullable = true)
	private String q1_3d_text;

	// 1 - Yes/All/Always
	// 2 - Some/Sometimes
	// 3 - No/None/Never
	@Column(name = "q_1_3e", nullable = true)
	private int q1_3e;

	@Lob
	@Column(name = "q_1_3e_text", nullable = true)
	private String q1_3e_text;

	// 1 - Yes/All/Always
	// 2 - Some/Sometimes
	// 3 - No/None/Never
	@Column(name = "q_1_3f", nullable = true)
	private Integer q1_3f;

	@Lob
	@Column(name = "q_1_3f_text", nullable = true)
	private String q1_3f_text;

	// --> Question 1.4
	// 1 - Yes/All/Always
	// 2 - Some/Sometimes
	// 3 - No/None/Never
	@Column(name = "q_1_4a", nullable = true)
	private int q1_4a;

	@Lob
	@Column(name = "q_1_4a_text", nullable = true)
	private String q1_4a_text;

	// 1 - Yes/All/Always
	// 2 - Some/Sometimes
	// 3 - No/None/Never
	@Column(name = "q_1_4b", nullable = true)
	private int q1_4b;

	@Lob
	@Column(name = "q_1_4b_text", nullable = true)
	private String q1_4b_text;

	// --> Question 1.5
	// 1 - Yes/All/Always
	// 2 - Some/Sometimes
	// 3 - No/None/Never
	@Column(name = "q_1_5a", nullable = true)
	private int q1_5a;

	@Lob
	@Column(name = "q_1_5a_text", nullable = true)
	private String q1_5a_text;

	// 1 - Yes/All/Always
	// 2 - Some/Sometimes
	// 3 - No/None/Never
	@Column(name = "q_1_5b", nullable = true)
	private int q1_5b;

	@Lob
	@Column(name = "q_1_5b_text", nullable = true)
	private String q1_5b_text;

	// 1 - Yes/All/Always
	// 2 - Some/Sometimes
	// 3 - No/None/Never
	@Column(name = "q_1_5c", nullable = true)
	private int q1_5c;

	@Lob
	@Column(name = "q_1_5c_text", nullable = true)
	private String q1_5c_text;

	// 1 - Yes/All/Always
	// 2 - Some/Sometimes
	// 3 - No/None/Never
	@Column(name = "q_1_5d", nullable = true)
	private int q1_5d;

	@Lob
	@Column(name = "q_1_5d_text", nullable = true)
	private String q1_5d_text;

	// 1 - Yes/All/Always
	// 2 - Some/Sometimes
	// 3 - No/None/Never
	@Column(name = "q_1_5e", nullable = true)
	private int q1_5e;

	@Lob
	@Column(name = "q_1_5e_text", nullable = true)
	private String q1_5e_text;

	// 1 - Yes/All/Always
	// 2 - Some/Sometimes
	// 3 - No/None/Never
	@Column(name = "q_1_5f", nullable = true)
	private int q1_5f;

	@Lob
	@Column(name = "q_1_5f_text", nullable = true)
	private String q1_5f_text;

	// --> Question 1.6
	// 1 - Yes/All/Always
	// 2 - Some/Sometimes
	// 3 - No/None/Never
	@Column(name = "q_1_6a", nullable = true)
	private int q1_6a;

	@Lob
	@Column(name = "q_1_6a_text", nullable = true)
	private String q1_6a_text;

	// 1 - Yes/All/Always
	// 2 - Some/Sometimes
	// 3 - No/None/Never
	@Column(name = "q_1_6b", nullable = true)
	private int q1_6b;

	@Lob
	@Column(name = "q_1_6b_text", nullable = true)
	private String q1_6b_text;

	@Column(name = "q_1_6b_file", nullable = true, columnDefinition = "LONGBLOB NULL")
	@Basic(fetch = FetchType.LAZY)
	private byte[] q1_6b_file;

	@Column(name = "q_1_6b_file_length", nullable = true)
	private Long q1_6b_file_contentLength;

	@Column(name = "q_1_6b_file_name", length = 100, nullable = true)
	private String q1_6b_file_name;

	// 1 - Yes/All/Always
	// 2 - Some/Sometimes
	// 3 - No/None/Never
	@Column(name = "q_1_6c", nullable = true)
	private int q1_6c;

	@Lob
	@Column(name = "q_1_6c_text", nullable = true)
	private String q1_6c_text;

	// 1 - Yes/All/Always
	// 2 - Some/Sometimes
	// 3 - No/None/Never
	@Column(name = "q_1_6d", nullable = true)
	private int q1_6d;

	@Lob
	@Column(name = "q_1_6d_text", nullable = true)
	private String q1_6d_text;

	// --> Question 1.7
	// 1 - Yes/All/Always
	// 2 - Some/Sometimes
	// 3 - No/None/Never
	@Column(name = "q_1_7a", nullable = true)
	private int q1_7a;

	@Lob
	@Column(name = "q_1_7a_text", nullable = true)
	private String q1_7a_text;

	// 1 - Yes/All/Always
	// 2 - Some/Sometimes
	// 3 - No/None/Never
	@Column(name = "q_1_7b", nullable = true)
	private int q1_7b;

	@Lob
	@Column(name = "q_1_7b_text", nullable = true)
	private String q1_7b_text;

	// 1 - Yes/All/Always
	// 2 - Some/Sometimes
	// 3 - No/None/Never
	@Column(name = "q_1_7c", nullable = true)
	private int q1_7c;

	@Lob
	@Column(name = "q_1_7c_text", nullable = true)
	private String q1_7c_text;

	@Lob
	@Column(name = "q_1_7c_text_2", nullable = true)
	private String q1_7c_text2;

	// 1 - Yes/All/Always
	// 2 - Some/Sometimes
	// 3 - No/None/Never
	@Column(name = "q_1_7d", nullable = true)
	private int q1_7d;

	@Lob
	@Column(name = "q_1_7d_text", nullable = true)
	private String q1_7d_text;

	// 1 - Yes/All/Always
	// 2 - Some/Sometimes
	// 3 - No/None/Never
	@Column(name = "q_1_7e", nullable = true)
	private int q1_7e;

	@Lob
	@Column(name = "q_1_7e_text", nullable = true)
	private String q1_7e_text;

	@Lob
	@Column(name = "q_1_7e_text_2", nullable = true)
	private String q1_7e_text2;

	// 1 - Yes
	// 0 - No
	@Column(name = "q_1_7f", nullable = true)
	private int q1_7f;

	@Lob
	@Column(name = "q_1_7f_text", nullable = true)
	private String q1_7f_text;

	@Column(name = "q_1_7_file", nullable = true, columnDefinition = "LONGBLOB NULL")
	@Basic(fetch = FetchType.LAZY)
	private byte[] q1_7_file;

	@Column(name = "q_1_7_file_length", nullable = true)
	private Long q1_7_file_contentLength;

	@Column(name = "q_1_7_file_name", length = 100, nullable = true)
	private String q1_7_file_name;

	// Section 2

	@Column(name = "q_2_1", nullable = true)
	private int q2_1;

	@Column(name = "q_2_2", length = 1024, nullable = true)
	private String q2_2;

	@Column(name = "q_2_2_file", nullable = true, columnDefinition = "LONGBLOB NULL")
	@Basic(fetch = FetchType.LAZY)
	private byte[] q2_2_file;

	@Column(name = "q_2_2_file_length", nullable = true)
	private Long q2_2_file_contentLength;

	@Column(name = "q_2_2_file_name", length = 100, nullable = true)
	private String q2_2_file_name;

	// Delimited string to contain multiple answers
	// 1 - In-person group
	// 2 - In-person individual
	// 3 - Online individual
	// 4 - Online group
	// 5 - Other
	@Column(name = "q_2_3", length = 100, nullable = true)
	private String q2_3;

	@Lob
	@Column(name = "q_2_3_text", nullable = true)
	private String q2_3_text;

	@Column(name = "q_2_4", nullable = true)
	private int q2_4;

	@Column(name = "q_2_5", nullable = true)
	private int q2_5;

	// Delimited string to contain multiple answers
	// 1 - Direct observation immediately following index testing training
	// 2 - Ongoing observations (e.g. monthly or quarterly)
	// 3 - Provide refresher training
	// 4 - Provide tailored mentoring/remediation
	// 5 - Other
	@Column(name = "q_2_6", length = 100, nullable = true)
	private String q2_6;

	@Lob
	@Column(name = "q_2_6_text", nullable = true)
	private String q2_6_text;

	@Column(name = "q_2_6_file", nullable = true, columnDefinition = "LONGBLOB NULL")
	@Basic(fetch = FetchType.LAZY)
	private byte[] q2_6_file;

	@Column(name = "q_2_6_file_length", nullable = true)
	private Long q2_6_file_contentLength;

	@Column(name = "q_2_6_file_name", length = 100, nullable = true)
	private String q2_6_file_name;

	// Section 3
	// 1 - Yes
	// 0 - No
	@Column(name = "q_3_1", nullable = true)
	private int q3_1;

	@Column(name = "q_3_2", nullable = true)
	private int q3_2;

	@Column(name = "q_3_3", length = 1024, nullable = true)
	private String q3_3;

	@Column(name = "q_3_3_file", nullable = true, columnDefinition = "LONGBLOB NULL")
	@Basic(fetch = FetchType.LAZY)
	private byte[] q3_3_file;

	@Column(name = "q_3_3_file_length", nullable = true)
	private Long q3_3_file_contentLength;

	@Column(name = "q_3_3_file_name", length = 100, nullable = true)
	private String q3_3_file_name;

	// Delimited string to contain multiple answers
	// 1 - In-person group
	// 2 - In-person individual
	// 3 - Online individual
	// 4 - Online group
	// 5 - Other
	@Column(name = "q_3_4", length = 100, nullable = true)
	private String q3_4;

	@Column(name = "q_3_4_text", length = 1024, nullable = true)
	private String q3_4_text;

	@Column(name = "q_3_5", nullable = true)
	private int q3_5;

	// Section 4

	// Delimited string to contain multiple answers
	// 1 - KP group
	// 2 - PLHIV group
	// 3 - Other
	@Column(name = "q_4_1", length = 100, nullable = true)
	private String q4_1;

	@Lob
	@Column(name = "q_4_2", nullable = true)
	private String q4_2;

	// 1 - Yes
	// 0 - No
	@Column(name = "q_4_3", nullable = true)
	private int q4_3;

	@Lob
	@Column(name = "q_4_3_text", nullable = true)
	private String q4_3_text;

	// Section 5

	// Delimited string to contain multiple answers
	// 1 - Men who have sex with men
	// 2 - Sex workers
	// 3 - Transgender women
	// 4 - People who inject drugs
	// 5 - People in prisons or other closed settings
	@Column(name = "q_5_1", length = 100, nullable = true)
	private String q5_1;

	// Delimited string to contain multiple answers
	// 1 - Men who have sex with men
	// 2 - Sex workers
	// 3 - Transgender women
	// 4 - People who inject drugs
	@Column(name = "q_5_2", length = 100, nullable = true)
	private String q5_2;

	// 1 - Yes
	// 2 - No
	// 3 - Unknown
	@Column(name = "q_5_3", nullable = true)
	private int q5_3;

	// Final score

	@Column(name = "final_score", nullable = true)
	private int finalScore;

	@Lob
	@Column(name = "correction_plan", nullable = true)
	private String correctionPlan;

	@Lob
	@Column(name = "additional_gaps", nullable = true)
	private String additionalGaps;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Organization getFacility() {
		return facility;
	}

	public void setFacility(Organization facility) {
		this.facility = facility;
	}

	public String getAssessorName() {
		return assessorName;
	}

	public void setAssessorName(String assessorName) {
		this.assessorName = assessorName;
	}

	public String getAssessorEmail() {
		return assessorEmail;
	}

	public void setAssessorEmail(String assessorEmail) {
		this.assessorEmail = assessorEmail;
	}

	public LocalDateTime getAssessmentDate() {
		return assessmentDate;
	}

	public void setAssessmentDate(LocalDateTime assessmentDate) {
		this.assessmentDate = assessmentDate;
	}

	public String getFacilityPocName() {
		return facilityPocName;
	}

	public void setFacilityPocName(String facilityPocName) {
		this.facilityPocName = facilityPocName;
	}

	public int getCounselorCount() {
		return counselorCount;
	}

	public void setCounselorCount(int counselorCount) {
		this.counselorCount = counselorCount;
	}

	public int getCounselor4Pns() {
		return counselor4Pns;
	}

	public void setCounselor4Pns(int counselor4Pns) {
		this.counselor4Pns = counselor4Pns;
	}

	public Boolean getSubmitted() {
		return submitted;
	}

	public void setSubmitted(Boolean submitted) {
		this.submitted = submitted;
	}

	public int getAssessmentType() {
		return assessmentType;
	}

	public void setAssessmentType(int assessmentType) {
		this.assessmentType = assessmentType;
	}

	public int getQ1_1a() {
		return q1_1a;
	}

	public void setQ1_1a(int q1_1a) {
		this.q1_1a = q1_1a;
	}

	public String getQ1_1a_text() {
		return q1_1a_text;
	}

	public void setQ1_1a_text(String q1_1a_text) {
		this.q1_1a_text = q1_1a_text;
	}

	public int getQ1_1b() {
		return q1_1b;
	}

	public void setQ1_1b(int q1_1b) {
		this.q1_1b = q1_1b;
	}

	public String getQ1_1b_text() {
		return q1_1b_text;
	}

	public void setQ1_1b_text(String q1_1b_text) {
		this.q1_1b_text = q1_1b_text;
	}

	public int getQ1_1c() {
		return q1_1c;
	}

	public void setQ1_1c(int q1_1c) {
		this.q1_1c = q1_1c;
	}

	public String getQ1_1c_text() {
		return q1_1c_text;
	}

	public void setQ1_1c_text(String q1_1c_text) {
		this.q1_1c_text = q1_1c_text;
	}

	public int getQ1_1d() {
		return q1_1d;
	}

	public void setQ1_1d(int q1_1d) {
		this.q1_1d = q1_1d;
	}

	public String getQ1_1d_text() {
		return q1_1d_text;
	}

	public void setQ1_1d_text(String q1_1d_text) {
		this.q1_1d_text = q1_1d_text;
	}

	public int getQ1_2a() {
		return q1_2a;
	}

	public void setQ1_2a(int q1_2a) {
		this.q1_2a = q1_2a;
	}

	public String getQ1_2a_text() {
		return q1_2a_text;
	}

	public void setQ1_2a_text(String q1_2a_text) {
		this.q1_2a_text = q1_2a_text;
	}

	public int getQ1_2b() {
		return q1_2b;
	}

	public void setQ1_2b(int q1_2b) {
		this.q1_2b = q1_2b;
	}

	public String getQ1_2b_text() {
		return q1_2b_text;
	}

	public void setQ1_2b_text(String q1_2b_text) {
		this.q1_2b_text = q1_2b_text;
	}

	public int getQ1_2c() {
		return q1_2c;
	}

	public void setQ1_2c(int q1_2c) {
		this.q1_2c = q1_2c;
	}

	public String getQ1_2c_text() {
		return q1_2c_text;
	}

	public void setQ1_2c_text(String q1_2c_text) {
		this.q1_2c_text = q1_2c_text;
	}

	public int getQ1_2d() {
		return q1_2d;
	}

	public void setQ1_2d(int q1_2d) {
		this.q1_2d = q1_2d;
	}

	public String getQ1_2d_text() {
		return q1_2d_text;
	}

	public void setQ1_2d_text(String q1_2d_text) {
		this.q1_2d_text = q1_2d_text;
	}

	public Integer getQ1_2e() {
		return q1_2e;
	}

	public void setQ1_2e(Integer q1_2e) {
		this.q1_2e = q1_2e;
	}

	public String getQ1_2e_text() {
		return q1_2e_text;
	}

	public void setQ1_2e_text(String q1_2e_text) {
		this.q1_2e_text = q1_2e_text;
	}

	public int getQ1_3a() {
		return q1_3a;
	}

	public void setQ1_3a(int q1_3a) {
		this.q1_3a = q1_3a;
	}

	public String getQ1_3a_text() {
		return q1_3a_text;
	}

	public void setQ1_3a_text(String q1_3a_text) {
		this.q1_3a_text = q1_3a_text;
	}

	public int getQ1_3b() {
		return q1_3b;
	}

	public void setQ1_3b(int q1_3b) {
		this.q1_3b = q1_3b;
	}

	public String getQ1_3b_text() {
		return q1_3b_text;
	}

	public void setQ1_3b_text(String q1_3b_text) {
		this.q1_3b_text = q1_3b_text;
	}

	public int getQ1_3c() {
		return q1_3c;
	}

	public void setQ1_3c(int q1_3c) {
		this.q1_3c = q1_3c;
	}

	public String getQ1_3c_text() {
		return q1_3c_text;
	}

	public void setQ1_3c_text(String q1_3c_text) {
		this.q1_3c_text = q1_3c_text;
	}

	public int getQ1_3d() {
		return q1_3d;
	}

	public void setQ1_3d(int q1_3d) {
		this.q1_3d = q1_3d;
	}

	public String getQ1_3d_text() {
		return q1_3d_text;
	}

	public void setQ1_3d_text(String q1_3d_text) {
		this.q1_3d_text = q1_3d_text;
	}

	public int getQ1_3e() {
		return q1_3e;
	}

	public void setQ1_3e(int q1_3e) {
		this.q1_3e = q1_3e;
	}

	public String getQ1_3e_text() {
		return q1_3e_text;
	}

	public void setQ1_3e_text(String q1_3e_text) {
		this.q1_3e_text = q1_3e_text;
	}

	public Integer getQ1_3f() {
		return q1_3f;
	}

	public void setQ1_3f(Integer q1_3f) {
		this.q1_3f = q1_3f;
	}

	public String getQ1_3f_text() {
		return q1_3f_text;
	}

	public void setQ1_3f_text(String q1_3f_text) {
		this.q1_3f_text = q1_3f_text;
	}

	public int getQ1_4a() {
		return q1_4a;
	}

	public void setQ1_4a(int q1_4a) {
		this.q1_4a = q1_4a;
	}

	public String getQ1_4a_text() {
		return q1_4a_text;
	}

	public void setQ1_4a_text(String q1_4a_text) {
		this.q1_4a_text = q1_4a_text;
	}

	public int getQ1_4b() {
		return q1_4b;
	}

	public void setQ1_4b(int q1_4b) {
		this.q1_4b = q1_4b;
	}

	public String getQ1_4b_text() {
		return q1_4b_text;
	}

	public void setQ1_4b_text(String q1_4b_text) {
		this.q1_4b_text = q1_4b_text;
	}

	public int getQ1_5a() {
		return q1_5a;
	}

	public void setQ1_5a(int q1_5a) {
		this.q1_5a = q1_5a;
	}

	public String getQ1_5a_text() {
		return q1_5a_text;
	}

	public void setQ1_5a_text(String q1_5a_text) {
		this.q1_5a_text = q1_5a_text;
	}

	public int getQ1_5b() {
		return q1_5b;
	}

	public void setQ1_5b(int q1_5b) {
		this.q1_5b = q1_5b;
	}

	public String getQ1_5b_text() {
		return q1_5b_text;
	}

	public void setQ1_5b_text(String q1_5b_text) {
		this.q1_5b_text = q1_5b_text;
	}

	public int getQ1_5c() {
		return q1_5c;
	}

	public void setQ1_5c(int q1_5c) {
		this.q1_5c = q1_5c;
	}

	public String getQ1_5c_text() {
		return q1_5c_text;
	}

	public void setQ1_5c_text(String q1_5c_text) {
		this.q1_5c_text = q1_5c_text;
	}

	public int getQ1_5d() {
		return q1_5d;
	}

	public void setQ1_5d(int q1_5d) {
		this.q1_5d = q1_5d;
	}

	public String getQ1_5d_text() {
		return q1_5d_text;
	}

	public void setQ1_5d_text(String q1_5d_text) {
		this.q1_5d_text = q1_5d_text;
	}

	public int getQ1_5e() {
		return q1_5e;
	}

	public void setQ1_5e(int q1_5e) {
		this.q1_5e = q1_5e;
	}

	public String getQ1_5e_text() {
		return q1_5e_text;
	}

	public void setQ1_5e_text(String q1_5e_text) {
		this.q1_5e_text = q1_5e_text;
	}

	public int getQ1_5f() {
		return q1_5f;
	}

	public void setQ1_5f(int q1_5f) {
		this.q1_5f = q1_5f;
	}

	public String getQ1_5f_text() {
		return q1_5f_text;
	}

	public void setQ1_5f_text(String q1_5f_text) {
		this.q1_5f_text = q1_5f_text;
	}

	public int getQ1_6a() {
		return q1_6a;
	}

	public void setQ1_6a(int q1_6a) {
		this.q1_6a = q1_6a;
	}

	public String getQ1_6a_text() {
		return q1_6a_text;
	}

	public void setQ1_6a_text(String q1_6a_text) {
		this.q1_6a_text = q1_6a_text;
	}

	public int getQ1_6b() {
		return q1_6b;
	}

	public void setQ1_6b(int q1_6b) {
		this.q1_6b = q1_6b;
	}

	public String getQ1_6b_text() {
		return q1_6b_text;
	}

	public void setQ1_6b_text(String q1_6b_text) {
		this.q1_6b_text = q1_6b_text;
	}

	public byte[] getQ1_6b_file() {
		return q1_6b_file;
	}

	public void setQ1_6b_file(byte[] q1_6b_file) {
		this.q1_6b_file = q1_6b_file;
	}

	public int getQ1_6c() {
		return q1_6c;
	}

	public void setQ1_6c(int q1_6c) {
		this.q1_6c = q1_6c;
	}

	public String getQ1_6c_text() {
		return q1_6c_text;
	}

	public void setQ1_6c_text(String q1_6c_text) {
		this.q1_6c_text = q1_6c_text;
	}

	public int getQ1_6d() {
		return q1_6d;
	}

	public void setQ1_6d(int q1_6d) {
		this.q1_6d = q1_6d;
	}

	public String getQ1_6d_text() {
		return q1_6d_text;
	}

	public void setQ1_6d_text(String q1_6d_text) {
		this.q1_6d_text = q1_6d_text;
	}

	public int getQ1_7a() {
		return q1_7a;
	}

	public void setQ1_7a(int q1_7a) {
		this.q1_7a = q1_7a;
	}

	public String getQ1_7a_text() {
		return q1_7a_text;
	}

	public void setQ1_7a_text(String q1_7a_text) {
		this.q1_7a_text = q1_7a_text;
	}

	public int getQ1_7b() {
		return q1_7b;
	}

	public void setQ1_7b(int q1_7b) {
		this.q1_7b = q1_7b;
	}

	public String getQ1_7b_text() {
		return q1_7b_text;
	}

	public void setQ1_7b_text(String q1_7b_text) {
		this.q1_7b_text = q1_7b_text;
	}

	public int getQ1_7c() {
		return q1_7c;
	}

	public void setQ1_7c(int q1_7c) {
		this.q1_7c = q1_7c;
	}

	public String getQ1_7c_text() {
		return q1_7c_text;
	}

	public void setQ1_7c_text(String q1_7c_text) {
		this.q1_7c_text = q1_7c_text;
	}

	public String getQ1_7c_text2() {
		return q1_7c_text2;
	}

	public void setQ1_7c_text2(String q1_7c_text2) {
		this.q1_7c_text2 = q1_7c_text2;
	}

	public int getQ1_7d() {
		return q1_7d;
	}

	public void setQ1_7d(int q1_7d) {
		this.q1_7d = q1_7d;
	}

	public String getQ1_7d_text() {
		return q1_7d_text;
	}

	public void setQ1_7d_text(String q1_7d_text) {
		this.q1_7d_text = q1_7d_text;
	}

	public int getQ1_7e() {
		return q1_7e;
	}

	public void setQ1_7e(int q1_7e) {
		this.q1_7e = q1_7e;
	}

	public String getQ1_7e_text() {
		return q1_7e_text;
	}

	public void setQ1_7e_text(String q1_7e_text) {
		this.q1_7e_text = q1_7e_text;
	}

	public String getQ1_7e_text2() {
		return q1_7e_text2;
	}

	public void setQ1_7e_text2(String q1_7e_text2) {
		this.q1_7e_text2 = q1_7e_text2;
	}

	public int getQ1_7f() {
		return q1_7f;
	}

	public void setQ1_7f(int q1_7f) {
		this.q1_7f = q1_7f;
	}

	public String getQ1_7f_text() {
		return q1_7f_text;
	}

	public void setQ1_7f_text(String q1_7f_text) {
		this.q1_7f_text = q1_7f_text;
	}

	public byte[] getQ1_7_file() {
		return q1_7_file;
	}

	public void setQ1_7_file(byte[] q1_7_file) {
		this.q1_7_file = q1_7_file;
	}

	public int getQ2_1() {
		return q2_1;
	}

	public void setQ2_1(int q2_1) {
		this.q2_1 = q2_1;
	}

	public String getQ2_2() {
		return q2_2;
	}

	public void setQ2_2(String q2_2) {
		this.q2_2 = q2_2;
	}

	public byte[] getQ2_2_file() {
		return q2_2_file;
	}

	public void setQ2_2_file(byte[] q2_2_file) {
		this.q2_2_file = q2_2_file;
	}

	public String getQ2_3() {
		return q2_3;
	}

	public void setQ2_3(String q2_3) {
		this.q2_3 = q2_3;
	}

	public String getQ2_3_text() {
		return q2_3_text;
	}

	public void setQ2_3_text(String q2_3_text) {
		this.q2_3_text = q2_3_text;
	}

	public int getQ2_4() {
		return q2_4;
	}

	public void setQ2_4(int q2_4) {
		this.q2_4 = q2_4;
	}

	public int getQ2_5() {
		return q2_5;
	}

	public void setQ2_5(int q2_5) {
		this.q2_5 = q2_5;
	}

	public String getQ2_6() {
		return q2_6;
	}

	public void setQ2_6(String q2_6) {
		this.q2_6 = q2_6;
	}

	public String getQ2_6_text() {
		return q2_6_text;
	}

	public void setQ2_6_text(String q2_6_text) {
		this.q2_6_text = q2_6_text;
	}

	public byte[] getQ2_6_file() {
		return q2_6_file;
	}

	public void setQ2_6_file(byte[] q2_6_file) {
		this.q2_6_file = q2_6_file;
	}

	public int getQ3_1() {
		return q3_1;
	}

	public void setQ3_1(int q3_1) {
		this.q3_1 = q3_1;
	}

	public int getQ3_2() {
		return q3_2;
	}

	public void setQ3_2(int q3_2) {
		this.q3_2 = q3_2;
	}

	public String getQ3_3() {
		return q3_3;
	}

	public void setQ3_3(String q3_3) {
		this.q3_3 = q3_3;
	}

	public byte[] getQ3_3_file() {
		return q3_3_file;
	}

	public void setQ3_3_file(byte[] q3_3_file) {
		this.q3_3_file = q3_3_file;
	}

	public String getQ3_4() {
		return q3_4;
	}

	public void setQ3_4(String q3_4) {
		this.q3_4 = q3_4;
	}

	public String getQ3_4_text() {
		return q3_4_text;
	}

	public void setQ3_4_text(String q3_4_text) {
		this.q3_4_text = q3_4_text;
	}

	public int getQ3_5() {
		return q3_5;
	}

	public void setQ3_5(int q3_5) {
		this.q3_5 = q3_5;
	}

	public String getQ4_1() {
		return q4_1;
	}

	public void setQ4_1(String q4_1) {
		this.q4_1 = q4_1;
	}

	public String getQ4_2() {
		return q4_2;
	}

	public void setQ4_2(String q4_2) {
		this.q4_2 = q4_2;
	}

	public int getQ4_3() {
		return q4_3;
	}

	public void setQ4_3(int q4_3) {
		this.q4_3 = q4_3;
	}

	public String getQ4_3_text() {
		return q4_3_text;
	}

	public void setQ4_3_text(String q4_3_text) {
		this.q4_3_text = q4_3_text;
	}

	public String getQ5_1() {
		return q5_1;
	}

	public void setQ5_1(String q5_1) {
		this.q5_1 = q5_1;
	}

	public String getQ5_2() {
		return q5_2;
	}

	public void setQ5_2(String q5_2) {
		this.q5_2 = q5_2;
	}

	public int getQ5_3() {
		return q5_3;
	}

	public void setQ5_3(int q5_3) {
		this.q5_3 = q5_3;
	}

	public Long getQ1_6b_file_contentLength() {
		return q1_6b_file_contentLength;
	}

	public void setQ1_6b_file_contentLength(Long q1_6b_file_contentLength) {
		this.q1_6b_file_contentLength = q1_6b_file_contentLength;
	}

	public String getQ1_6b_file_name() {
		return q1_6b_file_name;
	}

	public void setQ1_6b_file_name(String q1_6b_file_name) {
		this.q1_6b_file_name = q1_6b_file_name;
	}

	public Long getQ1_7_file_contentLength() {
		return q1_7_file_contentLength;
	}

	public void setQ1_7_file_contentLength(Long q1_7_file_contentLength) {
		this.q1_7_file_contentLength = q1_7_file_contentLength;
	}

	public String getQ1_7_file_name() {
		return q1_7_file_name;
	}

	public void setQ1_7_file_name(String q1_7_file_name) {
		this.q1_7_file_name = q1_7_file_name;
	}

	public Long getQ2_2_file_contentLength() {
		return q2_2_file_contentLength;
	}

	public void setQ2_2_file_contentLength(Long q2_2_file_contentLength) {
		this.q2_2_file_contentLength = q2_2_file_contentLength;
	}

	public String getQ2_2_file_name() {
		return q2_2_file_name;
	}

	public void setQ2_2_file_name(String q2_2_file_name) {
		this.q2_2_file_name = q2_2_file_name;
	}

	public Long getQ2_6_file_contentLength() {
		return q2_6_file_contentLength;
	}

	public void setQ2_6_file_contentLength(Long q2_6_file_contentLength) {
		this.q2_6_file_contentLength = q2_6_file_contentLength;
	}

	public String getQ2_6_file_name() {
		return q2_6_file_name;
	}

	public void setQ2_6_file_name(String q2_6_file_name) {
		this.q2_6_file_name = q2_6_file_name;
	}

	public Long getQ3_3_file_contentLength() {
		return q3_3_file_contentLength;
	}

	public void setQ3_3_file_contentLength(Long q3_3_file_contentLength) {
		this.q3_3_file_contentLength = q3_3_file_contentLength;
	}

	public String getQ3_3_file_name() {
		return q3_3_file_name;
	}

	public void setQ3_3_file_name(String q3_3_file_name) {
		this.q3_3_file_name = q3_3_file_name;
	}

	public int getFinalScore() {
		return finalScore;
	}

	public void setFinalScore(int finalScore) {
		this.finalScore = finalScore;
	}

	public String getCorrectionPlan() {
		return correctionPlan;
	}

	public void setCorrectionPlan(String correctionPlan) {
		this.correctionPlan = correctionPlan;
	}

	public String getAdditionalGaps() {
		return additionalGaps;
	}

	public void setAdditionalGaps(String additionalGaps) {
		this.additionalGaps = additionalGaps;
	}

}
