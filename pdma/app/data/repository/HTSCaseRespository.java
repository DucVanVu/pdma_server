package org.pepfar.pdma.app.data.repository;

import java.io.Serializable;
import java.util.UUID;

import org.pepfar.pdma.app.data.domain.HTSCase;
import org.pepfar.pdma.app.data.domain.PECase;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HTSCaseRespository extends PagingAndSortingRepository<HTSCase, Serializable>, QueryDslPredicateExecutor<HTSCase>{
  @Query("SELECT h FROM HTSCase h WHERE h.uid = ?1")
  public HTSCase findByUid(UUID uid);
  
  @Query("select h from HTSCase h where h.c6 =?1")
  HTSCase findByC6(String c6);
}
