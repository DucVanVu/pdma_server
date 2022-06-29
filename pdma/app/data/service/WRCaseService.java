package org.pepfar.pdma.app.data.service;

import java.util.List;

import org.pepfar.pdma.app.data.dto.WRCaseDto;
import org.pepfar.pdma.app.data.dto.WRCaseFilterDto;
import org.springframework.data.domain.Page;

public interface WRCaseService
{

	public WRCaseDto findById(Long id);

	public List<WRCaseDto> findAny(WRCaseFilterDto filter);

	public Page<WRCaseDto> findAllPageable(WRCaseFilterDto filter);

	public List<WRCaseDto> findTreatmentCases(WRCaseFilterDto filter);
	
	public Page<WRCaseDto> findUntreatedPageable(WRCaseFilterDto filter);
	
	public void markAsLinked2OPCAssist(WRCaseDto dto);

	public WRCaseDto saveOne(WRCaseDto dto);

	public void deleteMultiple(WRCaseDto[] dtos);

}
