package org.pepfar.pdma.app.data.repository;

import java.io.Serializable;
import java.util.List;

import org.pepfar.pdma.app.data.domain.Location;
import org.pepfar.pdma.app.data.types.AddressType;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface LocationRepository
		extends PagingAndSortingRepository<Location, Serializable>, QueryDslPredicateExecutor<Location> {

	@Query("FROM Location loc LEFT JOIN loc.person person WHERE person.id = :personId AND loc.addressType = :addressType")
	public List<Location> findForPerson(@Param("personId") Long personId,
			@Param("addressType") AddressType addressType);

	@Query("FROM Location loc LEFT JOIN loc.wrCase wrCase WHERE wrCase.id = :wrCaseId AND loc.addressType = :addressType")
	public List<Location> findForWRCase(@Param("wrCaseId") Long wrCaseId,
			@Param("addressType") AddressType addressType);

	@Modifying
	@Transactional
	@Query("DELETE FROM Location WHERE person.id = :personId AND addressType = :addressType AND id != :addressId")
	public void deleteUnwantedRecords(@Param("personId") Long personId, @Param("addressId") Long addressId,
			@Param("addressType") AddressType addressType);
}
