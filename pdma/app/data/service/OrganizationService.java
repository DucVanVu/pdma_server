package org.pepfar.pdma.app.data.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.pepfar.pdma.app.data.dto.*;
import org.springframework.data.domain.Page;

public interface OrganizationService {

	public OrganizationDto findById(Long id);

	public OrganizationDto findByCode(String code);

	public long count();

	public List<OrganizationDto> findAll(OrganizationFilterDto filter);

	public Page<OrganizationDto> findAllPageable(OrganizationFilterDto filter);

	public OrganizationDto saveOne(OrganizationDto dto);

	public OrganizationDto attachService(ServiceOrganizationDto dto);

	public OrganizationDto detachService(ServiceOrganizationDto dto);

	public void deleteMultiple(OrganizationDto[] dtos);

	public ImportResultDto<OrganizationDto> importFromExcel(InputStream is) throws IOException;

	public List<OrganizationDto> newImportFromExcel(InputStream is) throws IOException;

}
