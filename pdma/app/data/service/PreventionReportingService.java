package org.pepfar.pdma.app.data.service;

import org.apache.poi.ss.usermodel.Workbook;
import org.pepfar.pdma.app.data.dto.PreventionChartDto;
import org.pepfar.pdma.app.data.dto.PreventionFilterDto;

public interface PreventionReportingService {
	
	public PreventionChartDto getChart1(PreventionFilterDto dto);

	PreventionChartDto getChart2(PreventionFilterDto dto);
	
	PreventionChartDto getChart5(PreventionFilterDto dto);
	
	PreventionChartDto getChart6(PreventionFilterDto dto);
	
	PreventionChartDto getChart7(PreventionFilterDto dto);
	
	PreventionChartDto getChart8(PreventionFilterDto dto);
	
	PreventionChartDto getChart3(PreventionFilterDto dto);
	
	PreventionChartDto getChart4(PreventionFilterDto dto);
	
	PreventionChartDto getTotalsPE(PreventionFilterDto dto);
	
	PreventionChartDto getTotalsHTS(PreventionFilterDto dto);
	
	PreventionChartDto getTotalsHIV(PreventionFilterDto dto);
	
	PreventionChartDto getTotalsARV(PreventionFilterDto dto);
	
	PreventionChartDto getTotalsPrEP(PreventionFilterDto dto);
	
	PreventionChartDto getChart9(PreventionFilterDto dto);
	
	PreventionChartDto getChart10(PreventionFilterDto dto);
	
	PreventionChartDto getChart11(PreventionFilterDto dto);
	
	PreventionChartDto getChart12(PreventionFilterDto dto);
	
	PreventionChartDto getToltalsSection2(PreventionFilterDto dto);
	
	PreventionChartDto getToltalsSection3(PreventionFilterDto dto);
	
	PreventionChartDto getChart13(PreventionFilterDto dto);
	
	PreventionChartDto getChart14(PreventionFilterDto dto);

	Workbook exportMERReport(PreventionFilterDto filter);
	Workbook exportMERPEReport(PreventionFilterDto filter);
	
}
