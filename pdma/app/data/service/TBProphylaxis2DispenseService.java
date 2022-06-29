package org.pepfar.pdma.app.data.service;

import java.util.List;

import org.pepfar.pdma.app.data.domain.TBProphylaxis2Dispense;
import org.pepfar.pdma.app.data.dto.ObjectDto;
import org.pepfar.pdma.app.data.dto.TBProphylaxis2DispenseDto;
import org.pepfar.pdma.app.data.dto.TBProphylaxis2Dto;
import org.pepfar.pdma.app.data.dto.TBProphylaxisDispenseFilterDto;
import org.pepfar.pdma.app.data.dto.TBProphylaxisFilterDto;
import org.springframework.data.domain.Page;

public interface TBProphylaxis2DispenseService {

	public TBProphylaxis2DispenseDto findById(Long id);
	
	public TBProphylaxis2DispenseDto findLatest(Long roundId);
	
	public List<TBProphylaxis2DispenseDto> findAll(TBProphylaxisDispenseFilterDto filter);
	
	public Page<TBProphylaxis2DispenseDto> findAllPageable(TBProphylaxisDispenseFilterDto filter);

	public TBProphylaxis2DispenseDto saveOne(TBProphylaxis2DispenseDto dto);

	public void deleteMultiple(TBProphylaxis2DispenseDto[] dtos);

	public void deletaOneById(Long id);
	
}
