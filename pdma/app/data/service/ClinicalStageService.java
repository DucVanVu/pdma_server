package org.pepfar.pdma.app.data.service;

import java.util.List;

import org.pepfar.pdma.app.data.dto.ClinicalStageDto;
import org.pepfar.pdma.app.data.dto.ClinicalStageFilterDto;
import org.springframework.data.domain.Page;

public interface ClinicalStageService {

	public ClinicalStageDto findById(Long id);

	public List<ClinicalStageDto> findAll(ClinicalStageFilterDto filter);

	public Page<ClinicalStageDto> findAllPageable(ClinicalStageFilterDto filter);

	public ClinicalStageDto saveOne(ClinicalStageDto dto);

	public void deleteMultiple(ClinicalStageDto[] dtos);

}
