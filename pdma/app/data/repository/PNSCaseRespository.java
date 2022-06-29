package org.pepfar.pdma.app.data.repository;

import java.io.Serializable;
import java.util.UUID;

import org.pepfar.pdma.app.data.domain.PNSCase;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PNSCaseRespository extends PagingAndSortingRepository<PNSCase, Serializable>, QueryDslPredicateExecutor<PNSCase>{
  @Query("SELECT p FROM PNSCase p WHERE p.uid = ?1")
  public PNSCase findByUid(UUID uid);
}
