package org.pepfar.pdma.app.data.service;

import org.pepfar.pdma.app.data.dto.OPCDashboardChartData;
import org.pepfar.pdma.app.data.dto.OPCDashboardFilterDto;
import org.pepfar.pdma.app.data.dto.OPCDashboardSummaryData;

public interface OPCDashboardService {

	public OPCDashboardSummaryData getSummaryData(OPCDashboardFilterDto filter);

	public OPCDashboardChartData getChartData(OPCDashboardFilterDto filter);

}
