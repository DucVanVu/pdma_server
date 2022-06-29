package org.pepfar.pdma.app.data.repository;

import org.pepfar.pdma.app.data.domain.CoviVac;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.io.Serializable;

@Repository
public interface CoviVacRepository
		extends PagingAndSortingRepository<CoviVac, Serializable>, QueryDslPredicateExecutor<CoviVac> {
}
