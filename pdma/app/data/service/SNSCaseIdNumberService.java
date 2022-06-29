package org.pepfar.pdma.app.data.service;

import java.util.List;

import org.pepfar.pdma.app.data.dto.SNSCaseIdNumberDto;
import org.springframework.data.domain.Page;

public interface SNSCaseIdNumberService {

	public SNSCaseIdNumberDto findById(Long id);

//	public SNSCaseIdNumberDto findByIdNumberType(String code);

	public List<SNSCaseIdNumberDto> findAll();

	public Page<SNSCaseIdNumberDto> findAll(int pageIndex, int pageSize);

	public SNSCaseIdNumberDto saveOne(SNSCaseIdNumberDto dto);

	public void deleteMultiple(SNSCaseIdNumberDto[] dtos);
}
