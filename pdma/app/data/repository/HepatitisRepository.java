package org.pepfar.pdma.app.data.repository;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import org.pepfar.pdma.app.data.domain.Hepatitis;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HepatitisRepository
		extends PagingAndSortingRepository<Hepatitis, Serializable>, QueryDslPredicateExecutor<Hepatitis> {

	@Query("FROM Hepatitis WHERE theCase.id = :caseId AND testDate >= :fromDate AND testDate <= :toDate")
	public List<Hepatitis> findByTestDate(@Param("caseId") Long caseId, @Param("fromDate") LocalDateTime fromDate,
			@Param("toDate") LocalDateTime toDate);
	
}
