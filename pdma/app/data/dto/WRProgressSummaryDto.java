package org.pepfar.pdma.app.data.dto;

import org.springframework.data.domain.Page;

public class WRProgressSummaryDto
{
	private Page<WRProgressSummaryDetailsDto> details;

	private long[] summary;

	private long[] results;

	public Page<WRProgressSummaryDetailsDto> getDetails() {
		return details;
	}

	public void setDetails(Page<WRProgressSummaryDetailsDto> details) {
		this.details = details;
	}

	public long[] getSummary() {
		if (summary == null) {
			summary = new long[5];
		}

		return summary;
	}

	public void setSummary(long[] summary) {
		this.summary = summary;
	}

	public long[] getResults() {
		if (results == null) {
			results = new long[3];
		}

		return results;
	}

	public void setResults(long[] results) {
		this.results = results;
	}

}
