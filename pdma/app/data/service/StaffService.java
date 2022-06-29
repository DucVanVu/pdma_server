package org.pepfar.pdma.app.data.service;

import org.pepfar.pdma.app.data.dto.HTSCaseDto;
import org.pepfar.pdma.app.data.dto.ImportResultDto;
import org.pepfar.pdma.app.data.dto.StaffDto;
import org.pepfar.pdma.app.data.dto.StaffFilterDto;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface StaffService
{

	public StaffDto findById(Long id);

	public Page<StaffDto> findAllPageable(StaffFilterDto filter);

	public StaffDto saveOne(StaffDto dto);

	public void deleteMultiple(StaffDto[] dtos);

	public ImportResultDto<StaffDto> importFromExcel(InputStream is) throws IOException;
}
