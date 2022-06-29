package org.pepfar.pdma.app.data.service;

import org.pepfar.pdma.app.data.dto.AdminUnitEditTableDto;
import org.pepfar.pdma.app.data.dto.PreventionFilterDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AdminUnitEditTableService {

//	Page<AdminUnitEditTableDto> findPage(PreventionFilterDto dto);
	AdminUnitEditTableDto saveOrUpdate(AdminUnitEditTableDto dto);
//	AdminUnitEditTableDto getAdminUnitEditTableById(Long id);
//	List<AdminUnitEditTableDto> getAdminUnitEditTableByAdminUnit(Long id);
//	boolean deleteById(Long id);
}
