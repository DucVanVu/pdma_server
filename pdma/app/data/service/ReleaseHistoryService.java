package org.pepfar.pdma.app.data.service;

import java.util.List;

import org.pepfar.pdma.app.data.dto.ReleaseHistoryDto;

public interface ReleaseHistoryService
{

	public ReleaseHistoryDto findById(Long id);

	public List<ReleaseHistoryDto> findAll();

	public ReleaseHistoryDto saveOne(ReleaseHistoryDto dto);

	public void deleteMultiple(ReleaseHistoryDto[] dtos);

}
