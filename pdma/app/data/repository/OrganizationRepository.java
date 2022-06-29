package org.pepfar.pdma.app.data.repository;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import org.pepfar.pdma.app.data.domain.LabTest;
import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.types.ClinicalTestingType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository
		extends PagingAndSortingRepository<Organization, Serializable>, QueryDslPredicateExecutor<Organization>
{
	@Query("SELECT o FROM Organization o WHERE o.code = ?1")
	public Organization findByOrgCode(String orgCode);

	@Query("SELECT o FROM Organization o WHERE o.name = ?1")
	public Organization findByOrgName(String orgName);
	
	@Query("SELECT o.id  FROM Organization o WHERE o.address IS NOT NULL AND o.address.province IS NOT NULL AND o.address.province.id = ?1")
	List<Long> findAllIdByProvinceId(Long provinceId);
}
