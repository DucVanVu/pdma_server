package org.pepfar.pdma.app.data.service;

import org.apache.poi.ss.usermodel.Workbook;
import org.pepfar.pdma.app.data.dto.*;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface PNSCaseContactService {
	public Page<PNSCaseContactDto> findAllPageable(PreventionFilterDto searchDto);
	public PNSCaseContactDto saveOrUpdate(PNSCaseContactDto htsCaseDto);
	public ResponseDto<PNSCaseContactDto> deleteById(Long id);
	public PNSCaseContactDto findById(long id);
	public Workbook exportPNSCase(PreventionFilterDto searchDto);
	public ImportResultDto<PNSCaseContactDto> importFromExcel(InputStream is) throws IOException;
//	public CheckCodeDto checkDuplicateCode(CheckCodeDto dto);
}
