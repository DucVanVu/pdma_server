package org.pepfar.pdma.app.data.repository;

import java.io.Serializable;

import org.pepfar.pdma.app.data.domain.PNSCaseRiskGroup;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PNSCaseRiskGroupRepository  extends PagingAndSortingRepository<PNSCaseRiskGroup, Serializable>, QueryDslPredicateExecutor<PNSCaseRiskGroup>{

}
