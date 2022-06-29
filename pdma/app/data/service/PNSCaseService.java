package org.pepfar.pdma.app.data.service;

import org.apache.poi.ss.usermodel.Workbook;
import org.pepfar.pdma.app.data.dto.*;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface PNSCaseService {
	public Page<PNSCaseDto> findAllPageable(PreventionFilterDto searchDto);
	public PNSCaseDto saveOrUpdate(PNSCaseDto htsCaseDto);
	public ResponseDto<PNSCaseDto> deleteById(Long id);
	public PNSCaseDto findById(long id);
	public Workbook exportPNSCase(PreventionFilterDto searchDto);
	public Workbook exportReportPNSCase(PreventionFilterDto filter);
	public PreventionCheckCodeDto checkDuplicateCode(PreventionCheckCodeDto dto);
	public ResponseDto<PNSCaseDto> checkHTS(PreventionCheckCodeDto dto);
	public ImportResultDto<PNSCaseDto> importFromExcel(InputStream is) throws IOException;
	public PreventionReportDto<PNSDetailReportDto> getReport(PreventionFilterDto filter);
	public List<OrganizationDto> getListPNSWriteAble();
}
