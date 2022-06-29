package org.pepfar.pdma.app.data.repository;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import org.pepfar.pdma.app.data.domain.ClinicalStage;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClinicalStageRepository
		extends PagingAndSortingRepository<ClinicalStage, Serializable>, QueryDslPredicateExecutor<ClinicalStage> {

	@Query("SELECT cs FROM ClinicalStage cs WHERE cs.theCase.id = :caseId AND cs.evalDate >= :evalDateFrom AND cs.evalDate <= :evalDateTo")
	public List<ClinicalStage> getByEvalDate(@Param("caseId") Long caseId,
			@Param("evalDateFrom") LocalDateTime evalDateFrom, @Param("evalDateTo") LocalDateTime evalDateTo);

	@Modifying
	@Query("DELETE FROM ClinicalStage WHERE theCase.id = :caseId AND evalDate >= :evalDateFrom AND evalDate <= :evalDateTo")
	public void deleteByEvalDate(@Param("caseId") Long caseId, @Param("evalDateFrom") LocalDateTime evalDateFrom,
			@Param("evalDateTo") LocalDateTime evalDateTo);
}
