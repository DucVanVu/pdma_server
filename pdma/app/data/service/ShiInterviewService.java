package org.pepfar.pdma.app.data.service;

import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.pepfar.pdma.app.data.dto.ShiInterviewDto;
import org.pepfar.pdma.app.data.dto.ShiInterviewFilterDto;
import org.springframework.data.domain.Page;

public interface ShiInterviewService {

	public ShiInterviewDto findById(Long id);

	public ShiInterviewDto findLatestEntry(Long caseId);

	public List<ShiInterviewDto.DataCaptureInstance> getInstances(Long caseId);

	public List<ShiInterviewDto> findAll(ShiInterviewFilterDto filter);

	public Page<ShiInterviewDto> findAllPageable(ShiInterviewFilterDto filter);

	public ShiInterviewDto saveOne(ShiInterviewDto dto);

	public void deleteMultiple(ShiInterviewDto[] dtos);

	public Workbook generateReport(ShiInterviewFilterDto filter);
	
}
