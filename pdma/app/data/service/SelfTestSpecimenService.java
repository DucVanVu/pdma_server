package org.pepfar.pdma.app.data.service;

import org.pepfar.pdma.app.data.dto.PreventionFilterDto;
import org.pepfar.pdma.app.data.dto.PreventionReportDto;
import org.pepfar.pdma.app.data.dto.SelfTestDetailReportDto;
import org.pepfar.pdma.app.data.dto.SelfTestReportNewDto;

import java.util.List;

public interface SelfTestSpecimenService {
	PreventionReportDto<SelfTestDetailReportDto> getReport(PreventionFilterDto filter);

	List<SelfTestReportNewDto> getReportNew(PreventionFilterDto filter);
}
