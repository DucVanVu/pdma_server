package org.pepfar.pdma.app.data.service;

import org.pepfar.pdma.app.data.dto.SavedReportDto;
import org.pepfar.pdma.app.data.dto.SavedReportFilterDto;
import org.springframework.data.domain.Page;

public interface SavedReportService
{

	public SavedReportDto findById(Long id, boolean includeContent);

	public Page<SavedReportDto> findAll(SavedReportFilterDto filter);

	public SavedReportDto saveOne(SavedReportDto dto);

	public void deleteMultiple(SavedReportDto[] dtos);
}
