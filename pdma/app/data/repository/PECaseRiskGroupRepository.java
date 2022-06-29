package org.pepfar.pdma.app.data.repository;

import org.pepfar.pdma.app.data.domain.PECaseRiskGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PECaseRiskGroupRepository extends JpaRepository<PECaseRiskGroup, Long>{

}
