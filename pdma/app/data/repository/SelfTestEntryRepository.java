package org.pepfar.pdma.app.data.repository;

import java.io.Serializable;

import org.pepfar.pdma.app.data.domain.SelfTestEntry;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SelfTestEntryRepository
		extends PagingAndSortingRepository<SelfTestEntry, Serializable>, QueryDslPredicateExecutor<SelfTestEntry>
{

}
