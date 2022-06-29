package org.pepfar.pdma.app.data.repository;

import java.io.Serializable;

import org.pepfar.pdma.app.data.domain.HIVConfirmLab;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HIVConfirmLabRepository
		extends PagingAndSortingRepository<HIVConfirmLab, Serializable>, QueryDslPredicateExecutor<HIVConfirmLab>
{

}
