package org.pepfar.pdma.app.data.service;

import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.pepfar.pdma.app.data.dto.PNSAssessmentDto;
import org.pepfar.pdma.app.data.dto.PNSAssessmentFilterDto;
import org.pepfar.pdma.app.data.dto.PNSAssessmentPreferencesDto;
import org.springframework.data.domain.Page;

public interface PNSAssessmentService {

	public PNSAssessmentDto findById(Long id, String propName, boolean hasAttachment);

	public List<PNSAssessmentDto> findAll(PNSAssessmentFilterDto filter);

	public Page<PNSAssessmentDto> findAllPageable(PNSAssessmentFilterDto filter);

	public boolean hasBaseline(Long facilityId);

	public boolean hasPost(Long facilityId);

	public PNSAssessmentDto saveOne(PNSAssessmentDto dto);

	public PNSAssessmentDto submit(PNSAssessmentDto dto);

	public Workbook generateFacilityReport(Long id);

	public PNSAssessmentDto saveAttachment(PNSAssessmentDto dto, String propName);

	public void deleteMultiple(PNSAssessmentDto[] dtos);

	public Workbook exportData(PNSAssessmentFilterDto filter);
	
	public Workbook exportDataDetailed(Integer type);

	public PNSAssessmentPreferencesDto getPreferences();

	public PNSAssessmentPreferencesDto setPreferences(PNSAssessmentPreferencesDto pref);
}
