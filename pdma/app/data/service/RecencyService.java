package org.pepfar.pdma.app.data.service;

import java.util.List;

import org.pepfar.pdma.app.data.dto.RecencyDto;
import org.pepfar.pdma.app.data.dto.RecencyFilterDto;
import org.springframework.data.domain.Page;

public interface RecencyService {

	public RecencyDto findById(Long id);
	
	public RecencyDto findLatest(Long caseId);

	public List<RecencyDto> findAll(RecencyFilterDto filter);
	
	public Page<RecencyDto> findAllPageable(RecencyFilterDto filter);

	public RecencyDto saveOne(RecencyDto dto);

	public void deleteMultiple(RecencyDto[] dtos);

}
