package org.pepfar.pdma.app.data.service;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.pepfar.pdma.app.data.dto.ResponseDto;
import org.pepfar.pdma.app.data.dto.SLTargetDto;
import org.pepfar.pdma.app.data.dto.SLTargetFilterDto;
import org.pepfar.pdma.app.data.dto.SNSCaseDto;
import org.pepfar.pdma.app.data.dto.SNSCaseFilterDto;
import org.pepfar.pdma.app.data.dto.SNSCaseReportDto;
import org.pepfar.pdma.app.data.dto.OrganizationDto;
import org.pepfar.pdma.app.data.dto.PreventionCheckCodeDto;
import org.springframework.data.domain.Page;

public interface SNSCaseService
{
	public SNSCaseDto findById(Long id);

//	public List<SNSCaseDto> findAll(SNSCaseFilterDto filter);
	public ResponseDto<SNSCaseDto> deleteById (Long id);

//	public Page<SNSCaseDto> findAllPageable(SLTargetFilterDto filter);

	public SNSCaseDto saveOne(SNSCaseDto dto);

	Page<SNSCaseDto> findAllPageable(SNSCaseFilterDto filter);

	Integer getMaxSEQbyOrg(Long orgId);

	PreventionCheckCodeDto checkDuplicateCode(PreventionCheckCodeDto dto);

	SNSCaseDto findByCode(String couponCode);

	Workbook exportSNSCase(SNSCaseFilterDto filter);

	SNSCaseReportDto getReport(List<Long> orgIds, LocalDateTime from, LocalDateTime to);
	
	Workbook exportReportSNSCase(SNSCaseFilterDto filter);

//	public SNSCaseDto[] saveMultiple(SNSCaseDto[] dtos);

//	public void deleteMultiple(SNSCaseDto[] dtos);
	
	List<OrganizationDto> getListSNSWriteAble();
}
