package org.pepfar.pdma.app.data.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.pepfar.pdma.app.data.dto.HTSCaseDto;
import org.pepfar.pdma.app.data.dto.HTSReportDetailsDto;
import org.pepfar.pdma.app.data.dto.ImportResultDto;
import org.pepfar.pdma.app.data.dto.OrganizationDto;
import org.pepfar.pdma.app.data.dto.PreventionCheckCodeDto;
import org.pepfar.pdma.app.data.dto.PreventionFilterDto;
import org.pepfar.pdma.app.data.dto.PreventionReportDto;
import org.pepfar.pdma.app.data.dto.ResponseDto;
import org.pepfar.pdma.app.data.dto.STIDetailReportDto;
import org.springframework.data.domain.Page;

public interface HTSCaseService {
	public Page<HTSCaseDto> findAllPageable(PreventionFilterDto searchDto);
	public HTSCaseDto saveOrUpdate(HTSCaseDto htsCaseDto);
	public ResponseDto<HTSCaseDto> deleteById(Long id);
	public HTSCaseDto findById(long id);
	public SXSSFWorkbook exportHTSCase(PreventionFilterDto searchDto);
//	public HTSCaseReportDto getReport(List<Long> orgIds, LocalDateTime from, LocalDateTime to);
	public PreventionReportDto<STIDetailReportDto> getSTIReport(PreventionFilterDto filter);
	public Workbook exportReportHTSCase(PreventionFilterDto filter);
	public Workbook exportReportSTICase(PreventionFilterDto filter);
	public PreventionCheckCodeDto checkDuplicateCode(PreventionCheckCodeDto dto);
	public HTSCaseDto updateC24(HTSCaseDto dto);
	public ImportResultDto<HTSCaseDto> importFromExcel(InputStream is) throws IOException;
	PreventionReportDto<HTSReportDetailsDto> getReportDetail(PreventionFilterDto filter);
	public List<OrganizationDto> getListHTSWriteAble();
	public Workbook importFromExcelNew(InputStream is) throws IOException;
	public Workbook exportHTSTST(PreventionFilterDto filter,Workbook workbook);
	public Workbook exportHTSIndex(PreventionFilterDto filter,Workbook workbook);
	public Workbook exportHTSRecent(PreventionFilterDto filter,Workbook workbook);
	public ImportResultDto<HTSCaseDto> importFromExcelToUpdateIdentityCard(InputStream is) throws IOException;
}
