package org.pepfar.pdma.app.api;

import org.apache.poi.ss.usermodel.Workbook;
import org.pepfar.pdma.app.data.dto.PreventionChartDto;
import org.pepfar.pdma.app.data.dto.PreventionFilterDto;
import org.pepfar.pdma.app.data.service.PreventionReportingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

//@CrossOrigin(origins = {})
@RestController
@RequestMapping(path = "/api/v1/prev_chart")
public class PreventionRestController {

	@Autowired
	private PreventionReportingService service;

	@RequestMapping(path="/chart1",method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public PreventionChartDto getChart1(@RequestBody PreventionFilterDto dto) {
		return service.getChart1(dto);		
	}
	
	@RequestMapping(path="/chart2",method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public PreventionChartDto getChart2(@RequestBody PreventionFilterDto dto) {
		return service.getChart2(dto);		
	}
	
	@RequestMapping(path="/chart5",method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public PreventionChartDto getChart5(@RequestBody PreventionFilterDto dto) {
		return service.getChart5(dto);		
	}
	
	@RequestMapping(path="/chart6",method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public PreventionChartDto getChart6(@RequestBody PreventionFilterDto dto) {
		return service.getChart6(dto);		
	}
	
	@RequestMapping(path="/chart7",method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public PreventionChartDto getChart7(@RequestBody PreventionFilterDto dto) {
		return service.getChart7(dto);		
	}
	
	@RequestMapping(path="/chart8",method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public PreventionChartDto getChart8(@RequestBody PreventionFilterDto dto) {
		return service.getChart8(dto);		
	}
	
	@RequestMapping(path="/chart3",method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public PreventionChartDto getChart3(@RequestBody PreventionFilterDto dto) {
		return service.getChart3(dto);		
	}
	
	@RequestMapping(path="/chart4",method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public PreventionChartDto getChart4(@RequestBody PreventionFilterDto dto) {
		return service.getChart4(dto);		
	}
	
	@RequestMapping(path="/totalsPE",method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public PreventionChartDto getTotalsPE(@RequestBody PreventionFilterDto dto) {
		return service.getTotalsPE(dto);		
	}
	
	@RequestMapping(path="/totalsHTS",method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public PreventionChartDto getTotalsHTS(@RequestBody PreventionFilterDto dto) {
		return service.getTotalsHTS(dto);		
	}
	
	@RequestMapping(path="/totalsHIV",method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public PreventionChartDto getTotalsHIV(@RequestBody PreventionFilterDto dto) {
		return service.getTotalsHIV(dto);		
	}
	
	@RequestMapping(path="/totalsARV",method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public PreventionChartDto getTotalsARV(@RequestBody PreventionFilterDto dto) {
		return service.getTotalsARV(dto);		
	}
	
	@RequestMapping(path="/totalsPrEP",method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public PreventionChartDto getTotalsPrEP(@RequestBody PreventionFilterDto dto) {
		return service.getTotalsPrEP(dto);		
	}
	
	@RequestMapping(path="/chart9",method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public PreventionChartDto getChart9(@RequestBody PreventionFilterDto dto) {
		return service.getChart9(dto);		
	}
	
	@RequestMapping(path="/chart10",method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public PreventionChartDto getChart10(@RequestBody PreventionFilterDto dto) {
		return service.getChart10(dto);		
	}
	
	@RequestMapping(path="/chart11",method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public PreventionChartDto getChart11(@RequestBody PreventionFilterDto dto) {
		return service.getChart11(dto);		
	}
	
	@RequestMapping(path="/chart12",method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public PreventionChartDto getChart12(@RequestBody PreventionFilterDto dto) {
		return service.getChart12(dto);		
	}
	
	@RequestMapping(path="/toltalsSection2",method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public PreventionChartDto getToltalsSection2(@RequestBody PreventionFilterDto dto) {
		return service.getToltalsSection2(dto);		
	}
	
	@RequestMapping(path="/toltalsSection3",method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public PreventionChartDto getToltalsSection3(@RequestBody PreventionFilterDto dto) {
		return service.getToltalsSection3(dto);		
	}
	
	@RequestMapping(path="/chart13",method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public PreventionChartDto getChart13(@RequestBody PreventionFilterDto dto) {
		return service.getChart13(dto);		
	}

	@RequestMapping(path="/chart14",method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
	public PreventionChartDto getChart14(@RequestBody PreventionFilterDto dto) {
		return service.getChart14(dto);		
	}

	@PostMapping(path = "/exportReport")
	public void exportReportNew(@RequestBody PreventionFilterDto filter, HttpServletResponse response) {
		Workbook wbook = service.exportMERReport(filter);

		if (wbook == null) {
			throw new RuntimeException();
		}

		String filename = "Report_MER";

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
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@PostMapping(path = "/export-report-mer-pe")
	public void exportReportMerPe(@RequestBody PreventionFilterDto filter, HttpServletResponse response) {
		Workbook wbook = service.exportMERPEReport(filter);

		if (wbook == null) {
			throw new RuntimeException();
		}

		String filename = "Report_MER_PE";

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
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}
