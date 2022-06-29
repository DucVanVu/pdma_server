package org.pepfar.pdma.app.data.repository;

import java.io.Serializable;
import java.util.List;

import org.pepfar.pdma.app.data.domain.UserOrganization;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserOrganizationRepository
		extends PagingAndSortingRepository<UserOrganization, Serializable>, QueryDslPredicateExecutor<UserOrganization>
{
	@Query("select distinct(uo.user.id) from UserOrganization uo where uo.organization.address.province.id = ?1")
	List<Long> getListUserByProvinceId(Long provinceId);
	
	@Query("select uo from UserOrganization uo where uo.user.id = ?1")
	List<UserOrganization> getListByUserId(Long userId);
	
	@Query("select distinct(uo.organization.address.province.id) from UserOrganization uo where uo.user.id = ?1 and uo.organization "
			+ " is not null and uo.organization.address is not null and uo.organization.address.province is not null")
	List<Long> getListProvinceByUserId(Long userId);

	@Query("select distinct(uo.organization.address.province.id) from UserOrganization uo where uo.organization "
			+ " is not null and uo.organization.address is not null and uo.organization.address.province is not null")
	List<Long> getListProvinceByAdmin();
}
