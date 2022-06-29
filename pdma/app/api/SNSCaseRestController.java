package org.pepfar.pdma.app.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Workbook;
import org.pepfar.pdma.app.data.domain.User;
import org.pepfar.pdma.app.data.dto.ResponseDto;
import org.pepfar.pdma.app.data.dto.SNSCaseDto;
import org.pepfar.pdma.app.data.dto.SNSCaseFilterDto;
import org.pepfar.pdma.app.data.dto.SNSCaseReportDto;
import org.pepfar.pdma.app.data.dto.OrganizationDto;
import org.pepfar.pdma.app.data.dto.PreventionCheckCodeDto;
import org.pepfar.pdma.app.data.dto.UserDto;
import org.pepfar.pdma.app.data.service.SNSCaseService;
import org.pepfar.pdma.app.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/sns")
public class SNSCaseRestController {

	@Autowired
	private SNSCaseService service;
	
	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SNSCaseDto> getSNSCase(@PathVariable("id") Long id) {
		SNSCaseDto dto = service.findById(id);

		if (dto == null) {
			return new ResponseEntity<>(new SNSCaseDto(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(dto, HttpStatus.OK);
	}
	
	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE )
	public ResponseEntity<SNSCaseDto> save(@RequestBody SNSCaseDto dto){
		SNSCaseDto dtoCase = this.service.saveOne(dto);
		return new ResponseEntity<SNSCaseDto>( dtoCase, HttpStatus.OK);
		
	}
	@DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseDto<SNSCaseDto>> deleteById(@PathVariable ("id") Long id){
//		try {
			ResponseDto<SNSCaseDto> ret = service.deleteById(id);
			return new ResponseEntity<ResponseDto<SNSCaseDto>>(ret, HttpStatus.OK);
//		} catch (Exception e) {
//			return new ResponseEntity<ResponseDto<SNSCaseDto>>(null, HttpStatus.BAD_REQUEST);
//		}
	}
	
	@PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<SNSCaseDto>> getAllEntriesPageable(@RequestBody SNSCaseFilterDto filter) {

		User user = SecurityUtils.getCurrentUser();
		filter.setUser(new UserDto(user, false));		
		
		Page<SNSCaseDto> services = service.findAllPageable(filter);

		return new ResponseEntity<Page<SNSCaseDto>>(services, HttpStatus.OK);
	}
	@GetMapping(path = "/maxseq/{orgId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Integer> getAllEntriesPageable(@PathVariable Long orgId) {
		Integer maxseq = service.getMaxSEQbyOrg(orgId);
		return new ResponseEntity<Integer>(maxseq, HttpStatus.OK);
	}
	@PostMapping(path = "/checkCode", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PreventionCheckCodeDto> checkDuplicateCode(@RequestBody PreventionCheckCodeDto dto) {
		PreventionCheckCodeDto ret = service.checkDuplicateCode(dto);
		return new ResponseEntity<PreventionCheckCodeDto>(ret, HttpStatus.OK);
	}
	@PostMapping(path = "/checkCodeMulti", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<PreventionCheckCodeDto>> checkDuplicateCodeMulti(@RequestBody List<PreventionCheckCodeDto> dtos) {
		List<PreventionCheckCodeDto> rets = new ArrayList<PreventionCheckCodeDto>();
		for (PreventionCheckCodeDto dto : dtos) {
			PreventionCheckCodeDto ret = service.checkDuplicateCode(dto);
			rets.add(ret);
		}
		
		return new ResponseEntity<List<PreventionCheckCodeDto>>(rets, HttpStatus.OK);
	}
	@GetMapping(path = "/findByCode/{couponCode}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SNSCaseDto> getAllEntriesPageable(@PathVariable String couponCode) {
		SNSCaseDto dto = service.findByCode(couponCode);
		return new ResponseEntity<SNSCaseDto>(dto, HttpStatus.OK);
	}

	@PostMapping(path = "/export")
	public void generateReport(@RequestBody SNSCaseFilterDto filter, HttpServletResponse response) {
		Workbook wbook = this.service.exportSNSCase(filter);

		if (wbook == null) {
			throw new RuntimeException();
		}

		String filename = "sns-list";
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
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	@PostMapping(path = "/report")
	public SNSCaseReportDto getReport(@RequestBody SNSCaseFilterDto filter) {
		return service.getReport(filter.getOrgIds(), filter.getFromDate(), filter.getToDate()); 
	}
	
	@PostMapping(path = "/exportReport")
	public void exportReport(@RequestBody SNSCaseFilterDto filter, HttpServletResponse response) {
		Workbook wbook = this.service.exportReportSNSCase(filter);

		if (wbook == null) {
			throw new RuntimeException();
		}

		String filename = "report-sns";
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
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	@GetMapping(path = "/get_list_sns_write_able", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<OrganizationDto>> getListHTSWriteAble() {
		List<OrganizationDto> ret = service.getListSNSWriteAble();
		return new ResponseEntity<List<OrganizationDto>>(ret, HttpStatus.OK);
	}
	
	
//	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
//	public ResponseEntity<RecencyDto> getEntry(@PathVariable("id") Long id) {
//		RecencyDto dto = service.findById(id);
//
//		if (dto == null) {
//			return new ResponseEntity<RecencyDto>(new RecencyDto(), HttpStatus.BAD_REQUEST);
//		}
//
//		return new ResponseEntity<RecencyDto>(dto, HttpStatus.OK);
//	}
//	
//	@GetMapping(path = "/latest/{caseId}", produces = MediaType.APPLICATION_JSON_VALUE)
//	public ResponseEntity<RecencyDto> getLatestEntry(@PathVariable("caseId") Long caseId) {
//		RecencyDto dto = service.findLatest(caseId);
//
//		if (dto == null) {
//			return new ResponseEntity<RecencyDto>(new RecencyDto(), HttpStatus.BAD_REQUEST);
//		}
//
//		return new ResponseEntity<RecencyDto>(dto, HttpStatus.OK);
//	}
//
//	@PostMapping(path = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
//	public ResponseEntity<List<RecencyDto>> getAllEntriess(@RequestBody RecencyFilterDto filter) {
//
//		List<RecencyDto> services = service.findAll(filter);
//
//		return new ResponseEntity<List<RecencyDto>>(services, HttpStatus.OK);
//	}
//
//	@PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
//	public ResponseEntity<Page<RecencyDto>> getAllEntriesPageable(@RequestBody RecencyFilterDto filter) {
//
//		Page<RecencyDto> services = service.findAllPageable(filter);
//
//		return new ResponseEntity<Page<RecencyDto>>(services, HttpStatus.OK);
//	}
//
//	@RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
//	public ResponseEntity<RecencyDto> saveEntry(@RequestBody RecencyDto dto) {
//
//		if (dto == null) {
//			return new ResponseEntity<RecencyDto>(new RecencyDto(), HttpStatus.BAD_REQUEST);
//		}
//
//		dto = service.saveOne(dto);
//
//		return new ResponseEntity<RecencyDto>(dto, HttpStatus.OK);
//	}
//
//	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
//	public void deleteEntries(@RequestBody RecencyDto[] dtos) {
//		service.deleteMultiple(dtos);
//	}
}

