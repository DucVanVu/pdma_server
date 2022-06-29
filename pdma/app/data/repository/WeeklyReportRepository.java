package org.pepfar.pdma.app.data.repository;

import java.io.Serializable;

import org.pepfar.pdma.app.data.domain.WeeklyReport;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WeeklyReportRepository
		extends PagingAndSortingRepository<WeeklyReport, Serializable>, QueryDslPredicateExecutor<WeeklyReport>
{

	@Query("SELECT count(wr.id) FROM WeeklyReport wr WHERE wr.status = :status")
	public int countByStatus(@Param("status") int status);
}
