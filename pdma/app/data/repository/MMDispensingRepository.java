package org.pepfar.pdma.app.data.repository;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import org.pepfar.pdma.app.data.domain.MMDispensing;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MMDispensingRepository
		extends PagingAndSortingRepository<MMDispensing, Serializable>, QueryDslPredicateExecutor<MMDispensing> {

	@Query("FROM MMDispensing WHERE theCase.id = :caseId AND evaluationDate >= :fromDate AND evaluationDate <= :toDate")
	public List<MMDispensing> findInstance(@Param("caseId") Long caseId, @Param("fromDate") LocalDateTime fromDate,
			@Param("toDate") LocalDateTime toDate);

	@Query("FROM MMDispensing e WHERE e.theCase.id = :caseId AND e.evaluationDate = (SELECT MAX(e2.evaluationDate) FROM MMDispensing e2 WHERE e2.deleted = FALSE AND e2.theCase.id = :caseId AND e2.evaluationDate < :cutpointDate)")
	public List<MMDispensing> findPreviousInstance(@Param("caseId") Long caseId,
			@Param("cutpointDate") LocalDateTime cutpointDate);

	@Query(nativeQuery = true)
	public List<MMDispensing> findAllEvaluations(@Param("caseIds") List<Long> caseIds,
			@Param("includeDeleted") boolean includeDeleted);

}
