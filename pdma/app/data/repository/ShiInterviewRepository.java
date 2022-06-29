package org.pepfar.pdma.app.data.repository;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.pepfar.pdma.app.data.domain.ShiInterview;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ShiInterviewRepository
		extends PagingAndSortingRepository<ShiInterview, Serializable>, QueryDslPredicateExecutor<ShiInterview> {

	@Query("FROM ShiInterview e WHERE e.theCase.id = :caseId AND e.interviewDate = :interviewDate")
	public List<ShiInterview> findInstance(@Param("caseId") Long caseId,
			@Param("interviewDate") LocalDateTime interviewDate);

	@Query("FROM ShiInterview e WHERE e.theCase.id = :caseId AND e.interviewDate = (SELECT MAX(e2.interviewDate) FROM ShiInterview e2 WHERE e2.theCase.id = :caseId AND e2.interviewDate < :cutpointDate)")
	public List<ShiInterview> findPreviousInstance(@Param("caseId") Long caseId,
			@Param("cutpointDate") LocalDateTime cutpointDate);

	@Query("FROM ShiInterview e WHERE e.uid = :uid")
	public List<ShiInterview> findByUID(@Param("uid") UUID uid);
}
