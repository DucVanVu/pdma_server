package org.pepfar.pdma.app.data.service;

import org.apache.poi.ss.usermodel.Workbook;
import org.pepfar.pdma.app.data.dto.PNSAEDto;
import org.pepfar.pdma.app.data.dto.PNSAEFilterDto;
import org.springframework.data.domain.Page;

public interface PNSAEService {

	public PNSAEDto findById(Long id);

	public Page<PNSAEDto> findAllPageable(PNSAEFilterDto filter);

	public PNSAEDto saveOne(PNSAEDto dto);
	
	public PNSAEDto submit(PNSAEDto dto);

	public void deleteMultiple(PNSAEDto[] dtos);

	public Workbook exportData(PNSAEFilterDto filter);
	
}
