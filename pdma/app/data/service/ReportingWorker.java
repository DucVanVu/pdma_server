package org.pepfar.pdma.app.data.service;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.pepfar.pdma.app.data.dto.CaseReportFilterDto;

import java.util.concurrent.Callable;

public interface ReportingWorker<T> extends Callable<T> {

	public Sheet calcAndUpdateRawdata(Workbook wbook, Sheet sheet);

	public Sheet updateResultSheet(Sheet sheet);

	public void setFilter(CaseReportFilterDto filter);

}
