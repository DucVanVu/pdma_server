package org.pepfar.pdma.app.data.service;

import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.pepfar.pdma.app.data.dto.CaseReportFilterDto;
import org.pepfar.pdma.app.data.dto.LabTestDto;
import org.pepfar.pdma.app.data.dto.LabTestFilterDto;
import org.pepfar.pdma.app.data.dto.OPCDashboardFilterDto;
import org.springframework.data.domain.Page;

public interface LabTestService {

	public LabTestDto findById(Long id);

	public List<LabTestDto> findAll(LabTestFilterDto filter);

	public Page<LabTestDto> findAllPageable(LabTestFilterDto filter);

	/**
	 * @formatter:off
	 * Check VL test eligibility
	 * 
	 * @param filter
	 * @return
	 * 		0 = Not eligible
	 * 		1 = At 6 month
	 * 		2 = At 12 month
	 * 		3 = Routine 12 month
	 * 		4 = 3-month follow-up
	 * @formatter:on
	 */
	public int checkVLEligibility(LabTestFilterDto filter);

	public LabTestDto saveOne(LabTestDto dto);

	public void deleteMultiple(LabTestDto[] dtos);

	public Workbook generateReport(LabTestFilterDto filter);

	public Workbook getPatientsRequiringVL(OPCDashboardFilterDto filter);
	
	public Workbook getPatientsRequiringVL(CaseReportFilterDto filter);

	public Workbook exportListVLChartData(OPCDashboardFilterDto filter);
	
	public Workbook exportAllLabTestsData(CaseReportFilterDto filter);
}
