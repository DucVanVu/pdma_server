package org.pepfar.pdma.app.data.service;

import org.pepfar.pdma.app.data.dto.PregnancyDto;
import org.pepfar.pdma.app.data.dto.PregnancyFilterDto;
import org.springframework.data.domain.Page;

public interface PregnancyService {

	public PregnancyDto findById(Long id);

	public Page<PregnancyDto> findAllPageable(PregnancyFilterDto filter);

	public PregnancyDto saveOne(PregnancyDto dto);

	public void deleteMultiple(PregnancyDto[] dtos);

}
