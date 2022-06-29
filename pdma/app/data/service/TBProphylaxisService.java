package org.pepfar.pdma.app.data.service;

import java.util.List;

import org.pepfar.pdma.app.data.dto.TBProphylaxisDto;
import org.pepfar.pdma.app.data.dto.TBProphylaxisFilterDto;
import org.springframework.data.domain.Page;

public interface TBProphylaxisService {

	public TBProphylaxisDto findById(Long id);
	
	public TBProphylaxisDto findLatest(Long caseId);

	public List<TBProphylaxisDto> findAll(TBProphylaxisFilterDto filter);
	
	public Page<TBProphylaxisDto> findAllPageable(TBProphylaxisFilterDto filter);

	public TBProphylaxisDto saveOne(TBProphylaxisDto dto);

	public void deleteMultiple(TBProphylaxisDto[] dtos);

}
