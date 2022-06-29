package org.pepfar.pdma.app.data.repository;

import java.io.Serializable;

import org.pepfar.pdma.app.data.domain.PNSAE;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PNSAERepository
		extends PagingAndSortingRepository<PNSAE, Serializable>, QueryDslPredicateExecutor<PNSAE> {

}
