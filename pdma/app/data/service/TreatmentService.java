package org.pepfar.pdma.app.data.service;

import org.pepfar.pdma.app.data.dto.TreatmentDto;
import org.pepfar.pdma.app.data.dto.TreatmentFilterDto;
import org.springframework.data.domain.Page;

public interface TreatmentService {

	public TreatmentDto findById(Long id);

	public Page<TreatmentDto> findAllPageable(TreatmentFilterDto filter);

	public boolean hasMultipleMissingEndDate(TreatmentFilterDto filter);

	public TreatmentDto saveOne(TreatmentDto dto);

	public void deleteOne(TreatmentDto dto);

	public void deleteMultiple(TreatmentDto[] dtos);

}
