package org.pepfar.pdma.app.data.repository;

import org.pepfar.pdma.app.data.domain.AdminUnitEditTable;
import org.pepfar.pdma.app.data.dto.AdminUnitEditTableDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;

@Repository
public interface AdminUnitEditTableRepository extends PagingAndSortingRepository<AdminUnitEditTable, Serializable>, QueryDslPredicateExecutor<AdminUnitEditTable>
{
	@Query("select new org.pepfar.pdma.app.data.dto.AdminUnitEditTableDto(entity) from AdminUnitEditTable entity where entity.id = ?1 ")
	AdminUnitEditTableDto getAdminUnitEditTableById(Long id);
//new org.pepfar.pdma.app.data.dto.AdminUnitEditTableDto(entity) from
	@Query("select new org.pepfar.pdma.app.data.dto.AdminUnitEditTableDto(entity) from AdminUnitEditTable entity where entity.adminUnit.id = ?1 ")
	List<AdminUnitEditTableDto> getAdminUnitEditTableByAdminUnit(Long id);
}