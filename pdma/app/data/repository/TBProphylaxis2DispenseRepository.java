package org.pepfar.pdma.app.data.repository;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import org.pepfar.pdma.app.data.domain.TBProphylaxis2Dispense;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TBProphylaxis2DispenseRepository
		extends PagingAndSortingRepository<TBProphylaxis2Dispense, Serializable>,
		QueryDslPredicateExecutor<TBProphylaxis2Dispense> {
	@Query("select sum(d.dispensedDoses) from TBProphylaxis2Dispense d where d.round.id=?1 and d.dispensed=true")
	int countdispensedDoses(Long roundId);

	@Query("FROM TBProphylaxis2Dispense d where d.round.id=?1 and d.dispensed=true order by d.recordDate asc ")
	List<TBProphylaxis2Dispense> getListByRound(Long roundId);

	@Query("FROM TBProphylaxis2Dispense d where d.recordDate=?1 and d.round.id=?2 ")
	List<TBProphylaxis2Dispense> getListByDate(LocalDateTime recordDate, Long roundId);
}
