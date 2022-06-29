package org.pepfar.pdma.app.data.service;

import java.util.List;

import org.pepfar.pdma.app.data.dto.MMDispensingDto;
import org.pepfar.pdma.app.data.dto.MMDispensingFilterDto;
import org.pepfar.pdma.app.data.dto.MMDispensingHardEligibilityDto;

public interface MMDispensingService {

	public MMDispensingDto findById(Long id);

	// Is hard eligible (adult patient, CD4>=500/VL<200, ARV >= 12 months)
	public boolean isHardEligible(MMDispensingFilterDto filter);

	public MMDispensingHardEligibilityDto getHardEligibility(MMDispensingFilterDto filter);

	// Find history for one case
	public List<MMDispensingDto> findAll(MMDispensingFilterDto filter);

	public MMDispensingDto softDeleteRestore(MMDispensingDto dto);

	public MMDispensingDto saveOne(MMDispensingDto dto);

	public void deleteMultiple(MMDispensingDto[] dtos);

}
