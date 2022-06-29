package org.pepfar.pdma.app.data.repository;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.pepfar.pdma.app.data.domain.TBProphylaxis;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TBProphylaxisRepository
		extends PagingAndSortingRepository<TBProphylaxis, Serializable>, QueryDslPredicateExecutor<TBProphylaxis> {

	@Query("FROM TBProphylaxis WHERE uid = :uid")
	public List<TBProphylaxis> findByUUID(@Param("uid") UUID uid);
	
}
