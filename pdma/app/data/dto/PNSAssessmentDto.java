package org.pepfar.pdma.app.data.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.pepfar.pdma.app.data.domain.PNSAssessment;
import org.pepfar.pdma.app.utils.CustomLocalDateTimeDeserializer2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class PNSAssessmentDto extends AuditableEntityDto {

	private Long id;

	// General information

	private String ip;

	private OrganizationDto facility;

	private String assessorName;

	private String assessorEmail;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime assessmentDate;

	private String facilityPocName;

	private int counselorCount;

	private int counselor4Pns;

	private Boolean submitted;

	private int assessmentType;

	// Section 1

	// --> Question 1.1

	private int q1_1a;

	private String q1_1a_text;

	private int q1_1b;

	private String q1_1b_text;

	private int q1_1c;

	private String q1_1c_text;

	private int q1_1d;

	private String q1_1d_text;

	// --> Question 1.2

	private int q1_2a;

	private String q1_2a_text;

	private int q1_2b;

	private String q1_2b_text;

	private int q1_2c;

	private String q1_2c_text;

	private int q1_2d;

	private String q1_2d_text;

	private Integer q1_2e;

	private String q1_2e_text;

	// --> Question 1.3

	private int q1_3a;

	private String q1_3a_text;

	private int q1_3b;

	private String q1_3b_text;

	private int q1_3c;

	private String q1_3c_text;

	private int q1_3d;

	private String q1_3d_text;

	private int q1_3e;

	private String q1_3e_text;

	private Integer q1_3f;

	private String q1_3f_text;

	// --> Question 1.4

	private int q1_4a;

	private String q1_4a_text;

	private int q1_4b;

	private String q1_4b_text;

	// --> Question 1.5

	private int q1_5a;

	private String q1_5a_text;

	private int q1_5b;

	private String q1_5b_text;

	private int q1_5c;

	private String q1_5c_text;

	private int q1_5d;

	private String q1_5d_text;

	private int q1_5e;

	private String q1_5e_text;

	private int q1_5f;

	private String q1_5f_text;

	// --> Question 1.6

	private int q1_6a;

	private String q1_6a_text;

	private int q1_6b;

	private String q1_6b_text;

	private byte[] q1_6b_file;

	private Long q1_6b_file_contentLength;

	private String q1_6b_file_name;

	private int q1_6c;

	private String q1_6c_text;

	private int q1_6d;

	private String q1_6d_text;

	// --> Question 1.7

	private int q1_7a;

	private String q1_7a_text;

	private int q1_7b;

	private String q1_7b_text;

	private int q1_7c;

	private String q1_7c_text;

	private String q1_7c_text2;

	private int q1_7d;

	private String q1_7d_text;

	private int q1_7e;

	private String q1_7e_text;

	private String q1_7e_text2;

	private int q1_7f;

	private String q1_7f_text;

	private byte[] q1_7_file;

	private Long q1_7_file_contentLength;

	private String q1_7_file_name;

	// Section 2

	private int q2_1;

	private String q2_2;

	private byte[] q2_2_file;

	private Long q2_2_file_contentLength;

	private String q2_2_file_name;

	// Delimited string to contain multiple answers
	private String q2_3;

	private String q2_3_text;

	private int q2_4;

	private int q2_5;

	// Delimited string to contain multiple answers
	private String q2_6;

	private String q2_6_text;

	private byte[] q2_6_file;

	private Long q2_6_file_contentLength;

	private String q2_6_file_name;

	// Section 3

	private int q3_1;

	private int q3_2;

	private String q3_3;

	private byte[] q3_3_file;

	private Long q3_3_file_contentLength;

	private String q3_3_file_name;

	// Delimited string to contain multiple answers
	private String q3_4;

	private String q3_4_text;

	private int q3_5;

	// Section 4

	// Delimited string to contain multiple answers
	private String q4_1;

	private String q4_2;

	private int q4_3;

	private String q4_3_text;

	// Section 5

	// Delimited string to contain multiple answers
	private String q5_1;

	// Delimited string to contain multiple answers
	private String q5_2;

	private int q5_3;

	// Final score

	private int finalScore;

	private String correctionPlan;

	private String additionalGaps;

	private List<String> failedCEEs;

	public PNSAssessmentDto() {

	}

	public PNSAssessmentDto(PNSAssessment entity, String propName, boolean hasAttachment) {

		super(entity);

		if (entity == null) {
			return;
		}

		id = entity.getId();

		// General information
		ip = entity.getIp();
		assessorName = entity.getAssessorName();
		assessorEmail = entity.getAssessorEmail();
		assessmentDate = entity.getAssessmentDate();
		facilityPocName = entity.getFacilityPocName();
		counselorCount = entity.getCounselorCount();
		counselor4Pns = entity.getCounselor4Pns();

		submitted = entity.getSubmitted();
		assessmentType = entity.getAssessmentType();

		// Section 1

		// --> Question 1.1
		q1_1a = entity.getQ1_1a();
		q1_1a_text = entity.getQ1_1a_text();
		q1_1b = entity.getQ1_1b();
		q1_1b_text = entity.getQ1_1b_text();
		q1_1c = entity.getQ1_1c();
		q1_1c_text = entity.getQ1_1c_text();
		q1_1d = entity.getQ1_1d();
		q1_1d_text = entity.getQ1_1d_text();

		// --> Question 1.2
		q1_2a = entity.getQ1_2a();
		q1_2a_text = entity.getQ1_2a_text();
		q1_2b = entity.getQ1_2b();
		q1_2b_text = entity.getQ1_2b_text();
		q1_2c = entity.getQ1_2c();
		q1_2c_text = entity.getQ1_2c_text();
		q1_2d = entity.getQ1_2d();
		q1_2d_text = entity.getQ1_2d_text();
		q1_2e = entity.getQ1_2e();
		q1_2e_text = entity.getQ1_2e_text();

		// --> Question 1.3
		q1_3a = entity.getQ1_3a();
		q1_3a_text = entity.getQ1_3a_text();
		q1_3b = entity.getQ1_3b();
		q1_3b_text = entity.getQ1_3b_text();
		q1_3c = entity.getQ1_3c();
		q1_3c_text = entity.getQ1_3c_text();
		q1_3d = entity.getQ1_3d();
		q1_3d_text = entity.getQ1_3d_text();
		q1_3e = entity.getQ1_3e();
		q1_3e_text = entity.getQ1_3e_text();
		q1_3f = entity.getQ1_3f();
		q1_3f_text = entity.getQ1_3f_text();

		// --> Question 1.4
		q1_4a = entity.getQ1_4a();
		q1_4a_text = entity.getQ1_4a_text();
		q1_4b = entity.getQ1_4b();
		q1_4b_text = entity.getQ1_4b_text();

		// --> Question 1.5
		q1_5a = entity.getQ1_5a();
		q1_5a_text = entity.getQ1_5a_text();
		q1_5b = entity.getQ1_5b();
		q1_5b_text = entity.getQ1_5b_text();
		q1_5c = entity.getQ1_5c();
		q1_5c_text = entity.getQ1_5c_text();
		q1_5d = entity.getQ1_5d();
		q1_5d_text = entity.getQ1_5d_text();
		q1_5e = entity.getQ1_5e();
		q1_5e_text = entity.getQ1_5e_text();
		q1_5f = entity.getQ1_5f();
		q1_5f_text = entity.getQ1_5f_text();

		// --> Question 1.6
		q1_6a = entity.getQ1_6a();
		q1_6a_text = entity.getQ1_6a_text();
		q1_6b = entity.getQ1_6b();
		q1_6b_text = entity.getQ1_6b_text();
		q1_6b_file_contentLength = entity.getQ1_6b_file_contentLength();
		q1_6b_file_name = entity.getQ1_6b_file_name();
		q1_6c = entity.getQ1_6c();
		q1_6c_text = entity.getQ1_6c_text();
		q1_6d = entity.getQ1_6d();
		q1_6d_text = entity.getQ1_6d_text();

		// --> Question 1.7
		q1_7a = entity.getQ1_7a();
		q1_7a_text = entity.getQ1_7a_text();
		q1_7b = entity.getQ1_7b();
		q1_7b_text = entity.getQ1_7b_text();
		q1_7c = entity.getQ1_7c();
		q1_7c_text = entity.getQ1_7c_text();
		q1_7c_text2 = entity.getQ1_7c_text2();
		q1_7d = entity.getQ1_7d();
		q1_7d_text = entity.getQ1_7d_text();
		q1_7e = entity.getQ1_7e();
		q1_7e_text = entity.getQ1_7e_text();
		q1_7e_text2 = entity.getQ1_7e_text2();
		q1_7f = entity.getQ1_7f();
		q1_7f_text = entity.getQ1_7f_text();
		q1_7_file_contentLength = entity.getQ1_7_file_contentLength();
		q1_7_file_name = entity.getQ1_7_file_name();

		// Section 2
		q2_1 = entity.getQ2_1();
		q2_2 = entity.getQ2_2();
		q2_2_file_contentLength = entity.getQ2_2_file_contentLength();
		q2_2_file_name = entity.getQ2_2_file_name();

		// Delimited to contain multiple answers
		q2_3 = entity.getQ2_3();
		q2_3_text = entity.getQ2_3_text();
		q2_4 = entity.getQ2_4();
		q2_5 = entity.getQ2_5();

		// Delimited to contain multiple answers
		q2_6 = entity.getQ2_6();
		q2_6_text = entity.getQ2_6_text();
		q2_6_file_name = entity.getQ2_6_file_name();
		q2_6_file_contentLength = entity.getQ2_2_file_contentLength();

		// Section 3
		q3_1 = entity.getQ3_1();
		q3_2 = entity.getQ3_2();
		q3_3 = entity.getQ3_3();
		q3_3_file_contentLength = entity.getQ3_3_file_contentLength();
		q3_3_file_name = entity.getQ3_3_file_name();

		// Delimited to contain multiple answers
		q3_4 = entity.getQ3_4();
		q3_4_text = entity.getQ3_4_text();
		q3_5 = entity.getQ3_5();

		// Section 4

		// Delimited to contain multiple answers
		q4_1 = entity.getQ4_1();
		q4_2 = entity.getQ4_2();
		q4_3 = entity.getQ4_3();
		q4_3_text = entity.getQ4_3_text();

		// Section 5

		// Delimited to contain multiple answers
		q5_1 = entity.getQ5_1();
		// Delimited to contain multiple answers
		q5_2 = entity.getQ5_2();
		q5_3 = entity.getQ5_3();

		// Attachment
		if (hasAttachment) {
			switch (propName) {
				case "q1_6b_file":
					q1_6b_file = entity.getQ1_6b_file();
					break;
				case "q1_7_file":
					q1_7_file = entity.getQ1_7_file();
					break;
				case "q2_2_file":
					q2_2_file = entity.getQ2_2_file();
					break;
				case "q2_6_file":
					q2_6_file = entity.getQ2_6_file();
					break;
				case "q3_3_file":
					q3_3_file = entity.getQ3_3_file();
					break;
			}
		}

		// Final score

		finalScore = entity.getFinalScore();
		correctionPlan = entity.getCorrectionPlan();
		additionalGaps = entity.getAdditionalGaps();

		if (entity.getFacility() != null) {
			facility = new OrganizationDto(entity.getFacility());
		}

		// Calculate score & identify failed CEEs
		summarize();
	}

	public PNSAssessment toEntity() {

		PNSAssessment entity = new PNSAssessment();
		entity = (PNSAssessment) super.toEntity(entity);

		entity.setId(id);

		// General information
		entity.setIp(ip);
		entity.setAssessorName(assessorName);
		entity.setAssessorEmail(assessorEmail);
		entity.setAssessmentDate(assessmentDate);
		entity.setFacilityPocName(facilityPocName);
		entity.setCounselorCount(counselorCount);
		entity.setCounselor4Pns(counselor4Pns);
		entity.setAssessmentType(assessmentType);

		// Section 1

		// --> Question 1.1
		entity.setQ1_1a(q1_1a);
		entity.setQ1_1a_text(q1_1a_text);
		entity.setQ1_1b(q1_1b);
		entity.setQ1_1b_text(q1_1b_text);
		entity.setQ1_1c(q1_1c);
		entity.setQ1_1c_text(q1_1c_text);
		entity.setQ1_1d(q1_1d);
		entity.setQ1_1d_text(q1_1d_text);

		// --> Question 1.2
		entity.setQ1_2a(q1_2a);
		entity.setQ1_2a_text(q1_2a_text);
		entity.setQ1_2b(q1_2b);
		entity.setQ1_2b_text(q1_2b_text);
		entity.setQ1_2c(q1_2c);
		entity.setQ1_2c_text(q1_2c_text);
		entity.setQ1_2d(q1_2d);
		entity.setQ1_2d_text(q1_2d_text);
		entity.setQ1_2e(q1_2e);
		entity.setQ1_2e_text(q1_2e_text);

		// --> Question 1.3
		entity.setQ1_3a(q1_3a);
		entity.setQ1_3a_text(q1_3a_text);
		entity.setQ1_3b(q1_3b);
		entity.setQ1_3b_text(q1_3b_text);
		entity.setQ1_3c(q1_3c);
		entity.setQ1_3c_text(q1_3c_text);
		entity.setQ1_3d(q1_3d);
		entity.setQ1_3d_text(q1_3d_text);
		entity.setQ1_3e(q1_3e);
		entity.setQ1_3e_text(q1_3e_text);
		entity.setQ1_3f(q1_3f);
		entity.setQ1_3f_text(q1_3f_text);

		// --> Question 1.4
		entity.setQ1_4a(q1_4a);
		entity.setQ1_4a_text(q1_4a_text);
		entity.setQ1_4b(q1_4b);
		entity.setQ1_4b_text(q1_4b_text);

		// --> Question 1.5
		entity.setQ1_5a(q1_5a);
		entity.setQ1_5a_text(q1_5a_text);
		entity.setQ1_5b(q1_5b);
		entity.setQ1_5b_text(q1_5b_text);
		entity.setQ1_5c(q1_5c);
		entity.setQ1_5c_text(q1_5c_text);
		entity.setQ1_5d(q1_5d);
		entity.setQ1_5d_text(q1_5d_text);
		entity.setQ1_5e(q1_5e);
		entity.setQ1_5e_text(q1_5e_text);
		entity.setQ1_5f(q1_5f);
		entity.setQ1_5f_text(q1_5f_text);

		// --> Question 1.6
		entity.setQ1_6a(q1_6a);
		entity.setQ1_6a_text(q1_6a_text);
		entity.setQ1_6b(q1_6b);
		entity.setQ1_6b_text(q1_6b_text);
		entity.setQ1_6c(q1_6c);
		entity.setQ1_6c_text(q1_6c_text);
		entity.setQ1_6d(q1_6d);
		entity.setQ1_6d_text(q1_6d_text);

		// --> Question 1.7
		entity.setQ1_7a(q1_7a);
		entity.setQ1_7a_text(q1_7a_text);
		entity.setQ1_7b(q1_7b);
		entity.setQ1_7b_text(q1_7b_text);
		entity.setQ1_7c(q1_7c);
		entity.setQ1_7c_text(q1_7c_text);
		entity.setQ1_7c_text2(q1_7c_text2);
		entity.setQ1_7d(q1_7d);
		entity.setQ1_7d_text(q1_7d_text);
		entity.setQ1_7e(q1_7e);
		entity.setQ1_7e_text(q1_7e_text);
		entity.setQ1_7e_text2(q1_7e_text2);
		entity.setQ1_7f(q1_7f);
		entity.setQ1_7f_text(q1_7f_text);

		// Section 2
		entity.setQ2_1(q2_1);
		entity.setQ2_2(q2_2);

		// Delimited to contain multiple answers
		entity.setQ2_3(q2_3);
		entity.setQ2_3_text(q2_3_text);
		entity.setQ2_4(q2_4);
		entity.setQ2_5(q2_5);

		// Delimited to contain multiple answers
		entity.setQ2_6(q2_6);
		entity.setQ2_6_text(q2_6_text);

		// Section 3
		entity.setQ3_1(q3_1);
		entity.setQ3_2(q3_2);
		entity.setQ3_3(q3_3);

		// Delimited to contain multiple answers
		entity.setQ3_4(q3_4);
		entity.setQ3_4_text(q3_4_text);
		entity.setQ3_5(q3_5);

		// Section 4

		// Delimited to contain multiple answers
		entity.setQ4_1(q4_1);
		entity.setQ4_2(q4_2);
		entity.setQ4_3(q4_3);
		entity.setQ4_3_text(q4_3_text);

		// Section 5

		// Delimited to contain multiple answers
		entity.setQ5_1(q5_1);

		// Delimited to contain multiple answers
		entity.setQ5_2(q5_2);
		entity.setQ5_3(q5_3);

		// Final score
		entity.setFinalScore(finalScore);
		entity.setCorrectionPlan(correctionPlan);
		entity.setAdditionalGaps(additionalGaps);

		if (facility != null) {
			entity.setFacility(facility.toEntity());
		}

		return entity;
	}

	public void summarize() {
		finalScore = 0;
		failedCEEs = new ArrayList<>();

		// question #1
		if (q1_1a == 1) {
			finalScore++;
		} else {
			failedCEEs.add("q1_1a");
		}

		if (q1_1b == 1) {
			finalScore++;
		} else {
			failedCEEs.add("q1_1b");
		}

		if (q1_1c == 1) {
			finalScore++;
		} else {
			failedCEEs.add("q1_1c");
		}

		if (q1_1d == 1) {
			finalScore++;
		} else {
			failedCEEs.add("q1_1d");
		}

		// question #2
		if (q1_2a == 1) {
			finalScore++;
		} else {
			failedCEEs.add("q1_2a");
		}

		if (q1_2b == 1) {
			finalScore++;
		} else {
			failedCEEs.add("q1_2b");
		}

		if (q1_2c == 1) {
			finalScore++;
		} else {
			failedCEEs.add("q1_2c");
		}

		if (q1_2d == 1) {
			finalScore++;
		} else {
			failedCEEs.add("q1_2d");
		}

		if (q1_2e != null && q1_2e == 1) {
			finalScore++;
		} else {
			failedCEEs.add("q1_2e");
		}

		// question #3
		if (q1_3a == 1) {
			finalScore++;
		} else {
			failedCEEs.add("q1_3a");
		}

		if (q1_3b == 1) {
			finalScore++;
		} else {
			failedCEEs.add("q1_3b");
		}

		if (q1_3c == 1) {
			finalScore++;
		} else {
			failedCEEs.add("q1_3c");
		}

		if (q1_3d == 1) {
			finalScore++;
		} else {
			failedCEEs.add("q1_3d");
		}

		if (q1_3e == 1) {
			finalScore++;
		} else {
			failedCEEs.add("q1_3e");
		}

		if (q1_3f != null && q1_3f == 1) {
			finalScore++;
		} else {
			failedCEEs.add("q1_3f");
		}

		// question #4
		if (q1_4a == 1) {
			finalScore++;
		} else {
			failedCEEs.add("q1_4a");
		}

		if (q1_4b == 1) {
			finalScore++;
		} else {
			failedCEEs.add("q1_4b");
		}

		// question #5
		if (q1_5a == 1) {
			finalScore++;
		} else {
			failedCEEs.add("q1_5a");
		}

		if (q1_5b == 1) {
			finalScore++;
		} else {
			failedCEEs.add("q1_5b");
		}

		if (q1_5c == 1) {
			finalScore++;
		} else {
			failedCEEs.add("q1_5c");
		}

		if (q1_5d == 1) {
			finalScore++;
		} else {
			failedCEEs.add("q1_5d");
		}

		if (q1_5e == 1) {
			finalScore++;
		} else {
			failedCEEs.add("q1_5e");
		}

		if (q1_5f == 1) {
			finalScore++;
		} else {
			failedCEEs.add("q1_5f");
		}

		// question #6
		if (q1_6a == 1) {
			finalScore++;
		} else {
			failedCEEs.add("q1_6a");
		}

		if (q1_6b == 1) {
			finalScore++;
		} else {
			failedCEEs.add("q1_6b");
		}

		if (q1_6c == 1) {
			finalScore++;
		} else {
			failedCEEs.add("q1_6c");
		}

		if (q1_6d == 1) {
			finalScore++;
		} else {
			failedCEEs.add("q1_6d");
		}

		// question #7
		if (q1_7a == 1) {
			finalScore++;
		} else {
			failedCEEs.add("q1_7a");
		}

		if (q1_7b == 1) {
			finalScore++;
		} else {
			failedCEEs.add("q1_7b");
		}

		if (q1_7c == 1) {
			finalScore++;
		} else {
			failedCEEs.add("q1_7c");
		}

		if (q1_7d == 1) {
			finalScore++;
		} else {
			failedCEEs.add("q1_7d");
		}

		if (q1_7e == 1) {
			finalScore++;
		} else {
			failedCEEs.add("q1_7e");
		}

		if (q1_7f == 1) {
			finalScore++;
		} else {
			failedCEEs.add("q1_7f");
		}
	}

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

	public OrganizationDto getFacility() {
		return facility;
	}

	public void setFacility(OrganizationDto facility) {
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

	@JsonIgnore
	public String getAssessmentTypeText() {
		String ret = "";
		switch (assessmentType) {
			case 0:
				ret = "????nh gi?? ?????u v??o";
				break;
			case 1:
				ret = "????nh gi?? gi???a k???";
				break;
			case 2:
				ret = "????nh gi?? sau can thi???p";
				break;
			case 3:
				ret = "????nh gi?? ?????nh k???";
				break;
		}

		return ret;
	}

	@JsonIgnore
	public String getIpName() {
		String ret = "";
		switch (ip) {
			case "EPIC":
				ret = "D??? ??n EPIC";
				break;
			case "HCMC DOH":
				ret = "S??? y t??? TP. H??? Ch?? Minh";
				break;
			case "HMU":
				ret = "Tr?????ng ?????i h???c Y H?? N???i";
				break;
			case "HAIVN":
				ret = "T??? ch???c HAIVN";
				break;
		}

		return ret;
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

	public int getFinalScore() {
		return finalScore;
	}

	public String getConclusion() {
		if (finalScore == 33) {
			return "C?? s??? ?????t 33/33 ti??u chu???n t???i thi???u.";
		} else {
			return "C?? s??? ?????t " + finalScore + "/33 ti??u chu???n t???i thi???u.";
		}
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

	public List<String> getFailedCEEs() {

		if (failedCEEs == null) {
			failedCEEs = new ArrayList<>();
		}

		return failedCEEs;
	}

	public void setFailedCEEs(List<String> failedCEEs) {
		this.failedCEEs = failedCEEs;
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

	@JsonIgnore
	public List<FailedCEE4Report> getFailedCEE4Reports() {
		List<FailedCEE4Report> failedCEE4Reports = new ArrayList<>();

		if (failedCEEs == null || failedCEEs.size() <= 0) {
			return failedCEE4Reports;
		}

		//@formatter:off
		for (String failedCEE : failedCEEs) {
			switch (failedCEE) {
				case "q1_1a":
					failedCEE4Reports.add(new FailedCEE4Report("1.1A: T???i bu???i t?? v???n tr?????c x??t nghi???m HIV, t?? v???n vi??n c?? trao ?????i v???i t???t c??? kh??ch h??ng v??? TBXNBT/BC v?? t???m quan tr???ng c???a vi???c x??t nghi???m HIV cho BT/BC v?? con c???a h??? kh??ng?", q1_1a_text));
					break;
				case "q1_1b":
					failedCEE4Reports.add(new FailedCEE4Report("1.1B: T?? v???n vi??n c?? trao ?????i v???i t???t c??? kh??ch h??ng l?? ng?????i c?? HIV (NCH) v??? l???i ??ch v?? r???i ro khi nh???n d???ch v??? TBXNBT/BC kh??ng?", q1_1b_text));
					break;
				case "q1_1c":
					failedCEE4Reports.add(new FailedCEE4Report("1.1C: T?? v???n vi??n c?? gi???i thi???u v???i NCH t???t c??? 4 bi???n ph??p th???c hi???n TBXNBT/BC ?????ng th???i th???o lu???n l???i ??ch v?? r???i ro c???a t???ng bi???n ph??p n??y kh??ng?", q1_1c_text));
					break;
				case "q1_1d":
					failedCEE4Reports.add(new FailedCEE4Report("1.1D: T?? v???n vi??n c?? n??i r???ng NCH kh??ng nh???t thi???t ph???i l?? ng?????i th??ng b??o cho BT/BC c???a h??? v?? nh??n vi??n ch????ng tr??nh s??? gi??p ?????ng th???i kh??ng ti???t l??? danh t??nh c???a NCH khi li??n h??? v???i BT/BC?", q1_1d_text));
					break;
				case "q1_2a":
					failedCEE4Reports.add(new FailedCEE4Report("1.2A: T?? v???n vi??n th??ng b??o r???ng t???t c??? NCH c?? th??? t??? ch???i nh???n d???ch v??? TBXNBT/BC b???t k??? khi n??o v?? ??i???u n??y kh??ng ???nh h?????ng ?????n c??c d???ch v??? m?? h??? nh???n ???????c (v?? d??? nh?? h??? c???m th???y c?? ??p l???c ph???i ti???t l??? danh t??nh)?", q1_2a_text));
					break;
				case "q1_2b":
					failedCEE4Reports.add(new FailedCEE4Report("1.2B: Khi ???? ?????ng thu???n v?? tr?????c khi th???o lu???n c??? th??? danh s??ch BT/BC v?? con ph??i nhi???m HIV, t?? v???n vi??n c?? ????? ngh??? t???t c??? NCH k?? v??o phi???u ?????ng ?? th??ng b??o BT/BC kh??ng?", q1_2b_text));
					break;
				case "q1_2c":
					failedCEE4Reports.add(new FailedCEE4Report("1.2C: N???u NCH tr?????c ???? ???? th???ng nh???t ch???n bi???n ph??p nh??n vi??n th???c hi???n (k??? c??? khi chuy???n t??? bi???n ph??p th???a thu???n th???c hi???n ho???c bi???n ph??p NCH t??? th???c hi???n), t?? v???n vi??n c???n kh???ng ?????nh l???i v???i NCH li???u h??? v???n ?????ng ?? tr?????c khi t?? v???n vi??n tr???c ti???p li??n l???c v???i BT/BC kh??ng?", q1_2c_text));
					break;
				case "q1_2d":
					failedCEE4Reports.add(new FailedCEE4Report("1.2D: T???i c?? s???, c?? t??i li???u truy???n th??ng cho NCH ????? n??ng cao nh???n th???c v??? quy???n kh??ch h??ng r???ng h??? kh??ng b??? ??p bu???c ph???i nh???n d???ch v??? TBXNBT/BC v?? nh???n d???ch v??? l?? ho??n to??n t??? nguy???n?", q1_2d_text));
					break;
				case "q1_2e":
					failedCEE4Reports.add(new FailedCEE4Report("1.2E: T??i li???u truy???n th??ng cho NCH c?? c??c th??ng tin v??? c??c c??ch NCH c?? th??? b??o c??o vi???c vi ph???m quy???n kh??ch h??ng v?? c??ch nh???n ???????c s??? h??? tr??? n???u BT/BC c?? ph???n ???ng ti??u c???c sau khi li??n l???c kh??ng?", q1_2e_text));
					break;
				case "q1_3a":
					failedCEE4Reports.add(new FailedCEE4Report("1.3A: T?? v???n v??? d???ch v??? TBXNBT/BC v?? ????nh gi?? nguy c?? b???o l???c t??? t???ng BT/BC c?? ???????c th???c hi???n trong kh??ng gian ri??ng t?? kh??ng?", q1_3a_text));
					break;
				case "q1_3b":
					failedCEE4Reports.add(new FailedCEE4Report("1.3B: C??? hai bi???n ph??p sau ????y ??ang ???????c ??p d???ng ????? b???o v??? t??n, ?????a ch???, s??? ??i???n tho???i v.v. c???a NCH v?? BT/BC: 1). h??? s?? c???a h??? ???????c l??u gi??? ??? t??? c?? kh??a; v?? 2). c?? s??? d??? li???u ???????c b???o v???, v?? ch??? c?? nh??n vi??n ch???u tr??ch nhi???m c?? quy???n truy c???p?", q1_3b_text));
					break;
				case "q1_3c":
					failedCEE4Reports.add(new FailedCEE4Report("1.3C: M???i nh??n vi??n cung c???p d???ch v??? TBXNBT/BC ???? k?? 01 b???n \"Th???a thu???n cam k???t b???o m???t\" v?? l??u gi??? t???i c?? s????", q1_3c_text));
					break;
				case "q1_3d":
					failedCEE4Reports.add(new FailedCEE4Report("1.3D: N???u c?? s??? ph???i h???p v???i m???t b??n th??? ba ????? ti???n h??nh vi???c th??ng b??o, ti???p c???n/x??t nghi???m BT/BC c???a NCH (b?????c 5-6 trong quy tr??nh): t?? v???n vi??n ???? trao ?????i v?? nh???n ???????c s??? ?????ng thu???n c???a NCH ?????ng th???i c?? s??? c?? k?? v?? l??u gi??? b???n \"Th???a thu???n chia s??? d??? li???u\" v???i b??n th??? ba ???? ????? c??ng b???o m???t v??? t??n v?? th??ng tin li??n l???c c???a BT/BC v?? con?", q1_3d_text));
					break;
				case "q1_3e":
					failedCEE4Reports.add(new FailedCEE4Report("1.3E: Hi???n nay c?? s??? c?? b???n h?????ng d???n ho???c quy tr??nh th???c hi???n c??ch li??n l???c, th??ng b??o BT/BC qua g???i tin nh???n, g???i ??i???n tho???i, ?????n nh?? ho???c g???p tr???c ti???p, c??c ???ng d???ng m???ng x?? h???i ph?? h???p v?? ?????m b???o ri??ng t??, b???o m???t, v?? danh?", q1_3e_text));
					break;
				case "q1_3f":
					failedCEE4Reports.add(new FailedCEE4Report("1.3F: f. S??? cung c???p d???ch v??? TBXNBT/BC c?? ghi nh???n th??ng tin v??? ???????ng ph??i nhi???m HIV c???a c??c BT/BC kh??ng? (n???u tr??? l???i \"C??\", c???n thay ?????i s???)", q1_3f_text));
					break;
				case "q1_4a":
					failedCEE4Reports.add(new FailedCEE4Report("1.4A: C?? s??? c?? quy tr??nh ????? chuy???n ti???p t???t c??? BT/BC v?? con ????? c?? KQXN kh???ng ?????nh HIV d????ng t??nh t???i c??c d???ch v??? ??i???u tr??? ARV?", q1_4a_text));
					break;
				case "q1_4b":
					failedCEE4Reports.add(new FailedCEE4Report("1.4B: C?? s??? c?? quy tr??nh ????? chuy???n ti???p BT/BC c?? KQXN HIV ??m t??nh t???i c??c d???ch v??? d??? ph??ng HIV nh?? PrEP, BCS, BKT, MMT, v.v.?", q1_4b_text));
					break;
				case "q1_5a":
					failedCEE4Reports.add(new FailedCEE4Report("1.5A: T?? v???n vi??n c?? s??? d???ng 3 c??u h???i chu???n ???? ???????c h?????ng d???n ????? x??c ?????nh nguy c?? b???o l???c t??? t???ng BT/BC kh??ng?", q1_5a_text));
					break;
				case "q1_5b":
					failedCEE4Reports.add(new FailedCEE4Report("1.5B: T?? v???n vi??n ghi nh???n k???t qu??? ????nh gi?? nguy c?? b???o l???c cho t???ng BT/BC ???????c NCH chia s??? kh??ng?", q1_5b_text));
					break;
				case "q1_5c":
					failedCEE4Reports.add(new FailedCEE4Report("1.5C: ????? ?????m b???o an to??n cho NCH, BT/BC c?? nguy c?? bao l???c, c?? s??? c?? h?????ng d???n ho???c quy tr??nh v???: 1). l???a ch???n bi???n ph??p li??n l???c, th??ng b??o ph?? h???p; ho???c 2). c??ch l??m gi???m/lo???i tr??? nguy c?? b???o l???c; ho???c 3). l???a ch???n bi???n ph??p thay th???; ho???c 4). kh??ng li??n l???c/th??ng b??o cho BT/BC?", q1_5c_text));
					break;
				case "q1_5d":
					failedCEE4Reports.add(new FailedCEE4Report("1.5D: T?? v???n vi??n cung c???p d???ch v??? TBXNBT/BC ???? ???????c t???p hu???n v??? c??ng t??c h??? tr??? ban ?????u khi x??c ?????nh c?? nguy c?? ho???c x???y ra b???o l???c t??? BT/BC?", q1_5d_text));
					break;
				case "q1_5e":
					failedCEE4Reports.add(new FailedCEE4Report("1.5E: T?? v???n vi??n c?? danh s??ch c??c d???ch v??? h??? tr??? th??n thi???n hi???n nay v??? ph??ng ch???ng b???o l???c t???i ?????a ph????ng kh??ng?", q1_5e_text));
					break;
				case "q1_5f":
					failedCEE4Reports.add(new FailedCEE4Report("1.5F: C?? s??? c?? h??? th???ng chuy???n ti???p NCH, BT/BC ???????c x??c ?????nh c?? nguy c?? ho???c b??? b???o l???c t???i c??c d???ch v??? h??? tr??? th??n thi???n li??n quan?", q1_5d_text));
					break;
				case "q1_6a":
					failedCEE4Reports.add(new FailedCEE4Report("1.6A: T???t c??? nh??n vi??n cung c???p d???ch v??? TBXNBT/BC ???? ???????c t???p hu???n v??? c??ch ti???n h??nh d???ch v??? n??y m???t c??ch an to??n, ph?? h???p v???i chu???n m???c ?????o ?????c, s??? d???ng ch????ng tr??nh v?? t??i li???u t???p hu???n chu???n ????p ???ng t???t c??? c??c ti??u chu???n trong h?????ng d???n qu???c gia/PEPFAR v??? TBXNBT/BC?", q1_6a_text));
					break;
				case "q1_6b":
					failedCEE4Reports.add(new FailedCEE4Report("1.6B: N???i dung c??c l???p t???p hu???n TBXNBT/BC c?? nh???n m???nh vi???c b???o m???t, t??n tr???ng quy???n c???a kh??ch h??ng, s??? ?????ng thu???n nh???n d???ch v??? v?? \"kh??ng l??m ai t???n th????ng\"?", q1_6b_text));
					break;
				case "q1_6c":
					failedCEE4Reports.add(new FailedCEE4Report("1.6C: M???i t?? v???n vi??n cung c???p d???ch v??? TBXNBT/BC ???? ???????c gi??m s??t h??? tr??? ??t nh???t m???t ?????t (g???m quan s??t ph???n h???i tr???c ti???p v?? c?? bi??n b???n ghi nh???n k???t qu???) trong v??ng 12 th??ng qua?", q1_6c_text));
					break;
				case "q1_6d":
					failedCEE4Reports.add(new FailedCEE4Report("1.6D: C?? s??? c?? k??? ho???ch ?????nh k??? th???c hi???n quan s??t ph???n h???i v?? h?????ng d???n theo c??ch c???m tay ch??? vi???c cho c??c t?? v???n vi??n cung c???p d???ch v??? TBXNBT/BC kh??ng?", q1_6d_text));
					break;
				case "q1_7a":
					failedCEE4Reports.add(new FailedCEE4Report("1.7A: T???t c??? nh??n vi??n cung c???p d???ch v??? TBXNBT/BC ???? ???????c t???p hu???n v??? theo d??i, b??o c??o v?? x??? tr?? t??nh hu???ng kh??ng mong mu???n ch??a?", q1_7a_text));
					break;
				case "q1_7b":
					failedCEE4Reports.add(new FailedCEE4Report("1.7B: C?? s??? c?? h??? th???ng theo d??i v?? ??i???u tra t???t c??? c??c b??o c??o v??? t??nh hu???ng kh??ng mong mu???n nghi??m tr???ng v?? r???t nghi??m tr???ng nh?? h?????ng d???n c???a PEPFAR kh??ng?", q1_7b_text));
					break;
				case "q1_7c":
					failedCEE4Reports.add(new FailedCEE4Report("1.7C: Trong c??c l???n t??i kh??m/g???i ??i???n tho???i/li??n l???c, nh??n vi??n cung c???p d???ch v??? TBXNBT/BC th?????ng xuy??n h???i NCH v??? b???t k??? t??nh hu???ng kh??ng mong mu???n h??? g???p ph???i li??n quan t???i d???ch v??? TBXNBT/BC sau khi BT/BC ???????c th??ng b??o/x??t nghi???m HIV?", q1_7c_text));
					break;
				case "q1_7d":
					failedCEE4Reports.add(new FailedCEE4Report("1.7D: C?? s??? c?? s??? d???ng h???p th?? g??p ?? c???a kh??ch h??ng ho???c ???????ng d??y n??ng, v.v. ????? NCH th??ng b??o c??c t??nh hu???ng kh??ng mong mu???n d?????i h??nh th???c gi???u t??n kh??ng?", q1_7d_text));
					break;
				case "q1_7e":
					failedCEE4Reports.add(new FailedCEE4Report("1.7E: C?? s??? c?? c?? ch??? ????? nh??n vi??n cung c???p d???ch v??? TBXNBT/BC th??ng b??o v??? tr???i nghi???m c???a b???n th??n ho???c nh???ng l???n h??? quan s??t ???????c t??nh hu???ng kh??ng mong mu???n nh?? nh??n vi??n l???m d???ng, b???t gi???, vi ph???m b???o m???t, ti???t l??? t??nh tr???ng HIV, v.v. d?????i h??nh th???c gi???u t??n kh??ng?", q1_7e_text));
					break;
				case "q1_7f":
					failedCEE4Reports.add(new FailedCEE4Report("1.7F: C?? s??? c?? quy tr??nh ????? x??c ?????nh, ??i???u tra v?? x??? tr?? c??c t??nh hu???ng kh??ng mong mu???n li??n quan tr???c ti???p ?????n TBXNBT/BC kh??ng?", q1_7f_text));
					break;
			}
		}
		//@formatter:on

		return failedCEE4Reports;
	}

	// For facility level report
	public static final class FailedCEE4Report {
		private String cee;

		private String explanation;

		public FailedCEE4Report() {

		}

		public FailedCEE4Report(String cee, String exp) {
			this.cee = cee;
			this.explanation = exp;
		}

		public String getCee() {
			return cee;
		}

		public void setCee(String cee) {
			this.cee = cee;
		}

		public String getExplanation() {
			return explanation;
		}

		public void setExplanation(String explanation) {
			this.explanation = explanation;
		}

	}
}
