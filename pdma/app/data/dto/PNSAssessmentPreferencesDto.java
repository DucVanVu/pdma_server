package org.pepfar.pdma.app.data.dto;

import java.time.LocalDateTime;

import org.pepfar.pdma.app.utils.CustomLocalDateTimeDeserializer2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class PNSAssessmentPreferencesDto {

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime baselineToDate;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime postFromDate;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime postToDate;

	public LocalDateTime getBaselineToDate() {
		return baselineToDate;
	}

	public void setBaselineToDate(LocalDateTime baselineToDate) {
		this.baselineToDate = baselineToDate;
	}

	public LocalDateTime getPostFromDate() {
		return postFromDate;
	}

	public void setPostFromDate(LocalDateTime postFromDate) {
		this.postFromDate = postFromDate;
	}

	public LocalDateTime getPostToDate() {
		return postToDate;
	}

	public void setPostToDate(LocalDateTime postToDate) {
		this.postToDate = postToDate;
	}

}
