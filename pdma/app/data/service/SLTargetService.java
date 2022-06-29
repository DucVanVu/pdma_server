package org.pepfar.pdma.app.data.service;

import java.util.List;

import org.pepfar.pdma.app.data.dto.SLTargetDto;
import org.pepfar.pdma.app.data.dto.SLTargetFilterDto;
import org.springframework.data.domain.Page;

public interface SLTargetService
{

	public SLTargetDto findById(Long id);

	public List<SLTargetDto> findAll(SLTargetFilterDto filter);

	public Page<SLTargetDto> findAllPageable(SLTargetFilterDto filter);

	public SLTargetDto saveOne(SLTargetDto dto);

	public SLTargetDto[] saveMultiple(SLTargetDto[] dtos);

	public void deleteMultiple(SLTargetDto[] dtos);
}
