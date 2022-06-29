package org.pepfar.pdma.app.data.service;

import java.util.List;

import org.pepfar.pdma.app.data.dto.TBTreatmentDto;
import org.pepfar.pdma.app.data.dto.TBTreatmentFilterDto;
import org.springframework.data.domain.Page;

public interface TBTreatmentService {

	public TBTreatmentDto findById(Long id);
	
	public TBTreatmentDto findLatest(Long caseId);

	public List<TBTreatmentDto> findAll(TBTreatmentFilterDto filter);
	
	public Page<TBTreatmentDto> findAllPageable(TBTreatmentFilterDto filter);

	public TBTreatmentDto saveOne(TBTreatmentDto dto);

	public void deleteMultiple(TBTreatmentDto[] dtos);

}
