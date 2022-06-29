package org.pepfar.pdma.app.data.dto;

import java.time.LocalDateTime;

import org.pepfar.pdma.app.data.domain.Case;
import org.pepfar.pdma.app.utils.CustomLocalDateTimeDeserializer2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class CaseDto4Search {

	private Long id;

	private String patientChartId;

	private String fullname;

	private String gender;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime dob;

	public CaseDto4Search() {

	}

	public CaseDto4Search(Case theCase) {
		if (theCase == null) {
			return;
		}

		this.id = theCase.getId();
//		this.patientChartId = theCase.getPatientChartId();
		this.fullname = theCase.getPerson().getFullname();
		this.gender = theCase.getPerson().getGender().toString();
		this.dob = theCase.getPerson().getDob();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPatientChartId() {
		return patientChartId;
	}

	public void setPatientChartId(String patientChartId) {
		this.patientChartId = patientChartId;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public LocalDateTime getDob() {
		return dob;
	}

	public void setDob(LocalDateTime dob) {
		this.dob = dob;
	}

}
