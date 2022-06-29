package org.pepfar.pdma.app.data.repository;

import java.io.Serializable;

import org.pepfar.pdma.app.data.domain.HTSCaseRiskGroup;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface HTSCaseRiskGroupRepository  extends PagingAndSortingRepository<HTSCaseRiskGroup, Serializable>, QueryDslPredicateExecutor<HTSCaseRiskGroup>{

}
