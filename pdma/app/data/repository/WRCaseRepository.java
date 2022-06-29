package org.pepfar.pdma.app.data.repository;

import java.io.Serializable;
import java.util.List;

import org.pepfar.pdma.app.data.domain.WRCase;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WRCaseRepository
		extends PagingAndSortingRepository<WRCase, Serializable>, QueryDslPredicateExecutor<WRCase>
{

	/**
	 * Look for the equivalent record in the weekly report
	 */
	@Query(nativeQuery = true)
	public List<WRCase> findEquivWRCase(@Param("organizationId") Long orgId,
			@Param("patientChartId") String patientChartId);

}
