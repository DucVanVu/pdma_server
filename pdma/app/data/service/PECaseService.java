package org.pepfar.pdma.app.data.service;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.pepfar.pdma.app.data.dto.*;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface PECaseService {
	public Page<PECaseDto> findAllPageable(PreventionFilterDto searchDto);
	public PECaseDto saveOrUpdate(PECaseDto dto);
	public ResponseDto<PECaseDto> deleteById(Long id);
	public SXSSFWorkbook exportPECase(PreventionFilterDto searchDto);
	public Workbook exportReportPECase(PreventionFilterDto filter);
	public PECaseDto findById(long id);
	public PreventionReportDto<PE02DetailReportDto> getReport(PreventionFilterDto filter);
	public ImportResultDto<PECaseDto> importFromExcel(InputStream is) throws IOException;
	public List<OrganizationDto> getListPEWriteAble();

    List<ReportMERPEDto> getDataReportMERPE(PreventionFilterDto filter);
}
