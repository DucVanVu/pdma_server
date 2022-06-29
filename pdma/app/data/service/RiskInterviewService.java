package org.pepfar.pdma.app.data.service;

import org.pepfar.pdma.app.data.dto.RiskInterviewDto;
import org.pepfar.pdma.app.data.dto.RiskInterviewFilterDto;
import org.springframework.data.domain.Page;

public interface RiskInterviewService
{

	public RiskInterviewDto findById(Long id);

	public Page<RiskInterviewDto> findAllPageable(RiskInterviewFilterDto filter);

	public RiskInterviewDto saveOne(RiskInterviewDto dto);

	public RiskInterviewDto updateRisks(RiskInterviewDto dto);
	
	public void deleteMultiple(RiskInterviewDto[] dtos);

}
