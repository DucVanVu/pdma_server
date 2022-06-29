package org.pepfar.pdma.app.data.repository;

import java.io.Serializable;

import org.pepfar.pdma.app.data.domain.SNSCase;
import org.pepfar.pdma.app.data.dto.SNSCaseDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface SNSCaseRepository
		extends PagingAndSortingRepository<SNSCase, Serializable>, QueryDslPredicateExecutor<SNSCase> {
	@Query("SELECT new org.pepfar.pdma.app.data.dto.SNSCaseDto(s, 1,true,false,false) FROM SNSCase s WHERE s.couponCode=?1 ")
	public SNSCaseDto findByCode(String couponCode);
}
