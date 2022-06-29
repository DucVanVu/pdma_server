package org.pepfar.pdma.app.data.repository;

import java.io.Serializable;

import org.pepfar.pdma.app.data.domain.Dictionary;
import org.pepfar.pdma.app.data.types.DictionaryType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DictionaryRepository
		extends PagingAndSortingRepository<Dictionary, Serializable>, QueryDslPredicateExecutor<Dictionary>
{

	@Query("SELECT coalesce(max(d.order), 0) FROM Dictionary d WHERE d.type = :type")
	public int getMaxSortOrder(@Param("type") DictionaryType type);
}
