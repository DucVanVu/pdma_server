package org.pepfar.pdma.app.data.service;

import java.util.List;

import org.pepfar.pdma.app.data.dto.AdminUnitDto;
import org.pepfar.pdma.app.data.dto.AdminUnitFilterDto;
import org.springframework.data.domain.Page;

public interface AdminUnitService
{

	public AdminUnitDto findById(Long id);

	public AdminUnitDto findByGsoCode(String code);
	
	public AdminUnitDto findByCode(String code);

	public List<AdminUnitDto> findAll(AdminUnitFilterDto filter);

	public Page<AdminUnitDto> findAllPageable(AdminUnitFilterDto filter);

	public boolean codeExists(AdminUnitDto dto);

	public AdminUnitDto saveOne(AdminUnitDto dto);
	
	public void updateGsoCode();

	public void deleteMultiple(AdminUnitDto[] dtos);

	public List<AdminUnitDto> findAllProvinceByUser();

}
