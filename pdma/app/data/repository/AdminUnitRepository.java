package org.pepfar.pdma.app.data.repository;

import java.io.Serializable;
import java.util.List;

import org.pepfar.pdma.app.data.domain.AdminUnit;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminUnitRepository extends PagingAndSortingRepository<AdminUnit, Serializable>, QueryDslPredicateExecutor<AdminUnit>
{
	@Query("SELECT a FROM AdminUnit a WHERE a.codeGso = ?1")
	public AdminUnit findByProvinceOrDistrict(String provinceCode);
	
	@Query("Select a from AdminUnit a where lower(a.name) = lower(?1)")
	List<AdminUnit> findByName(String name);
}