package org.pepfar.pdma.app.data.service;

import java.util.List;

import org.pepfar.pdma.app.data.dto.TBTreatment2Dto;
import org.pepfar.pdma.app.data.dto.TBTreatmentFilterDto;
import org.springframework.data.domain.Page;

public interface TBTreatment2Service {

	public TBTreatment2Dto findById(Long id);
	
	public TBTreatment2Dto findLatest(Long caseId);

	public List<TBTreatment2Dto> findAll(TBTreatmentFilterDto filter);
	
	public Page<TBTreatment2Dto> findAllPageable(TBTreatmentFilterDto filter);

	public TBTreatment2Dto saveOne(TBTreatment2Dto dto);

	public void deleteMultiple(TBTreatment2Dto[] dtos);
	
	public void deletaOneById(Long id);
}
