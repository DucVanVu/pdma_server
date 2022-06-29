package org.pepfar.pdma.app.data.service;

import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.pepfar.pdma.app.data.dto.OrganizationDto;
import org.pepfar.pdma.app.data.dto.WRChartDataDto;
import org.pepfar.pdma.app.data.dto.WRChartFilterDto;
import org.pepfar.pdma.app.data.dto.WRExportExcelFilterDto;
import org.pepfar.pdma.app.data.dto.WRProgressSummaryDto;
import org.pepfar.pdma.app.data.dto.WRProgressSummaryFilterDto;
import org.pepfar.pdma.app.data.dto.WeeklyReportDto;
import org.pepfar.pdma.app.data.dto.WeeklyReportFilterDto;
import org.springframework.data.domain.Page;

public interface WeeklyReportService {

	public WeeklyReportDto findById(Long id);

	public WeeklyReportDto findByWeekAndOrg(WeeklyReportDto dto);

	public List<WeeklyReportDto> findAll(OrganizationDto[] orgs);

	public Page<WeeklyReportDto> findAllPageable(WeeklyReportFilterDto filter);

	public WeeklyReportDto saveStatus(WeeklyReportDto dto);

	public WeeklyReportDto saveDApproval(WeeklyReportDto dto);

	public WeeklyReportDto saveOne(WeeklyReportDto dto);

	public WeeklyReportDto saveCases(WeeklyReportDto dto);

	public WeeklyReportDto deleteCases(WeeklyReportDto dto, boolean deletePositiveCase);

	public Workbook createExcelFile(WRExportExcelFilterDto filter);

	public void synthesize(boolean syncAll);

	public WRProgressSummaryDto findProgressSummary(WRProgressSummaryFilterDto filter);

	public void adjustReportingPeriod();

	public WRChartDataDto getChartData(WRChartFilterDto filter);

	public void deleteMultiple(WeeklyReportDto[] dtos);

	public void cleanDemoData();
}
