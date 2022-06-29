package org.pepfar.pdma.app.data.repository;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.pepfar.pdma.app.data.domain.RiskInterview;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RiskInterviewRepository
		extends PagingAndSortingRepository<RiskInterview, Serializable>, QueryDslPredicateExecutor<RiskInterview> {

	@Query("FROM RiskInterview ri WHERE ri.uid = :uid")
	public List<RiskInterview> findByUID(@Param("uid") UUID uid);

	@Query(value = "SELECT dic.code FROM tbl_risk_interview interview INNER JOIN tbl_riskinterview_risks risk ON risk.interview_id = interview.id INNER JOIN tbl_dictionary dic ON dic.id = risk.risk_id WHERE risk.interview_id = (SELECT MAX(interview2.id) max_id FROM tbl_risk_interview interview2 WHERE interview2.risk_identified = TRUE AND interview2.case_id = :caseId)", nativeQuery = true)
	public List<String> findRiskCodesInMostRecentInterview(@Param("caseId") Long caseId);

//	@Query(value = "SELECT risk.* FROM tbl_risk_interview risk INNER JOIN (SELECT MAX(risk2.id) max_id, risk2.case_id FROM tbl_risk_interview risk2 GROUP BY risk2.case_id) max_interview ON max_interview.max_id = risk.id INNER JOIN tbl_case c ON max_interview.case_id = c.id INNER JOIN tbl_case_org co ON c.id = co.case_id WHERE c.deleted = FALSE AND co.is_latest_rel = TRUE AND co.ref_tracking_only = FALSE AND co.org_id = :organizationId AND risk.risk_identified = TRUE", nativeQuery = true)
//	public List<RiskInterview> findMostRecentInterviews(@Param("organizationId") Long orgId);
}
