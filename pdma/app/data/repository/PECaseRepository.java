package org.pepfar.pdma.app.data.repository;

import org.pepfar.pdma.app.data.domain.PECase;
import org.pepfar.pdma.app.data.domain.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PECaseRepository extends JpaRepository<PECase, Long>{
  @Query("SELECT p FROM PECase p WHERE p.uid = ?1")
  public PECase findByUid(UUID uid);
}
