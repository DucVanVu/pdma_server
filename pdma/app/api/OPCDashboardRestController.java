package org.pepfar.pdma.app.api;

import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Workbook;
import org.pepfar.pdma.app.data.dto.OPCDashboardChartData;
import org.pepfar.pdma.app.data.dto.OPCDashboardFilterDto;
import org.pepfar.pdma.app.data.dto.OPCDashboardSummaryData;
import org.pepfar.pdma.app.data.service.CaseService;
import org.pepfar.pdma.app.data.service.LabTestService;
import org.pepfar.pdma.app.data.service.OPCDashboardService;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/opc-dashboard")
public class OPCDashboardRestController {

	@Autowired
	private OPCDashboardService service;

	@Autowired
	private CaseService caseService;
	@Autowired
	private LabTestService labTestService;

	@PostMapping(path = "summary")
	public ResponseEntity<OPCDashboardSummaryData> getSummary(@RequestBody OPCDashboardFilterDto filter) {
		if (filter == null) {
			filter = new OPCDashboardFilterDto();
		}

		return new ResponseEntity<>(service.getSummaryData(filter), HttpStatus.OK);
	}

	@PostMapping(path = "chart-data")
	public ResponseEntity<OPCDashboardChartData> getChartData(@RequestBody OPCDashboardFilterDto filter) {
		if (filter == null) {
			filter = new OPCDashboardFilterDto();
		}

		return new ResponseEntity<>(service.getChartData(filter), HttpStatus.OK);
	}

	@PostMapping(path = "/export")
	public void generateReport(@RequestBody OPCDashboardFilterDto filter, HttpServletResponse response) {

		if (filter == null || filter.getTargetChart() <= 0) {
			throw new RuntimeException();
		}

		Workbook wbook = null;

		switch (filter.getTargetChart()) {
		case 2:
			wbook = labTestService.exportListVLChartData(filter);
			break;
		case 3:
			wbook = caseService.exportListPatientOnMMD(filter);
			break;
		case 4:
			wbook = caseService.exportListPatientTLD(filter);
			break;
		case 5:
			wbook = caseService.exportListPatientRiskGroup(filter);
			break;
		case 6:
			wbook = caseService.exportListPatientTBScreening(filter);
			break;
		case 7:
			wbook = labTestService.getPatientsRequiringVL(filter);
			break;
		default:
			break;
		}

		if (wbook == null) {
			throw new RuntimeException();
		}

		String filename = getFilename(filter);
		if (filename == null) {
			throw new RuntimeException();
		}

		CacheControl cc = CacheControl.maxAge(360, TimeUnit.DAYS).cachePublic();

		response.addHeader("Access-Control-Expose-Headers", "x-filename");
		response.addHeader("Content-disposition", "inline; filename=" + filename);
		response.addHeader("x-filename", filename);
		response.setHeader("Cache-Control", cc.getHeaderValue());
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

		try {
			wbook.write(response.getOutputStream());
			response.flushBuffer();
			response.getOutputStream().close();
			wbook.close();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private String getFilename(OPCDashboardFilterDto filter) {

		if (filter == null || filter.getTargetChart() <= 0) {
			return null;
		}

		String filename = "";
		switch (filter.getTargetChart()) {
		case 2:
			filename = "bcao_xetnghiem_tlvr_4quy_";
			break;
		case 3:
			filename = "bcao_arv_nhieuthang_";
			break;
		case 4:
			filename = "bcao_phacdo_tld_";
			break;
		case 5:
			filename = "bcao_đanhgia_nhomnguyco_";
			break;
		case 6:
			filename = "bcao_sangloclao_5thang_";
			break;
		case 7:
			filename = "bcao_bn_canlam_tlvr_";
			break;
		default:
			break;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
		filename = filename + sdf.format(CommonUtils.fromLocalDateTime(CommonUtils.hanoiNow())) + ".xlsx";

		return filename;
	}
}
