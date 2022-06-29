package org.pepfar.pdma.app.data.service;

import java.util.List;

import org.pepfar.pdma.app.data.dto.RegimenDto;
import org.pepfar.pdma.app.data.dto.RegimenFilterDto;
import org.springframework.data.domain.Page;

public interface RegimenService
{

	public RegimenDto findById(Long id);

	public long count();
	
	public RegimenDto findByShortName(String shortName);

	public List<RegimenDto> findAll(RegimenFilterDto filter);

	public Page<RegimenDto> findAllPageable(RegimenFilterDto filter);

	public RegimenDto saveOne(RegimenDto dto);

	public void deleteMultiple(RegimenDto[] dtos);

}
