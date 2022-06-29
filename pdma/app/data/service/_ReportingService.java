package org.pepfar.pdma.app.data.service;

import org.apache.poi.ss.usermodel.Workbook;
import org.pepfar.pdma.app.data.dto.CaseReportFilterDto;

public interface _ReportingService {
	
	public Workbook exportReport(CaseReportFilterDto dto);

}
