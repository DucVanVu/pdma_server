package org.pepfar.pdma.app.data.repository;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import org.pepfar.pdma.app.data.domain.Case;
import org.pepfar.pdma.app.data.dto.OPCDashboardPatientChartData;
import org.pepfar.pdma.app.data.dto.OPCDashboardPatientChartData2;
import org.pepfar.pdma.app.data.dto.OPCDashboardRiskGroupChartData;
import org.pepfar.pdma.app.data.dto.OPCDashboardTBScreeningChartData;
import org.pepfar.pdma.app.data.dto.OPCDashboardTLDChartData;
import org.pepfar.pdma.app.data.dto.PatientExtraData;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CaseRepository
        extends PagingAndSortingRepository<Case, Serializable>, QueryDslPredicateExecutor<Case> {

    @Query("FROM Case c WHERE c.uid = :uid")
    public List<Case> findByUID(@Param("uid") UUID uid);

    @Query("SELECT c FROM Case c WHERE c.uid = ?1")
    public Case findOneByUID(UUID uid);

    // --------------------------------------------------
    // BEGIN: HOUSE KEEPING
    // --------------------------------------------------

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE tbl_case c LEFT JOIN tbl_clinical_stage cs ON c.id = cs.case_id SET c.who_stage = NULL, c" +
            ".who_stage_eval_date = NULL WHERE cs.id IS NULL AND (c.who_stage IS NOT NULL OR c.who_stage_eval_date IS" +
            " NOT NULL)", nativeQuery = true)
    public int housekeeping4ClinicalStage();

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE tbl_case c LEFT JOIN tbl_treatment tx ON c.id = tx.case_id SET c.arv_regimen_name = NULL, " +
            "c.arv_regimen_start_date = NULL, c.arv_regimen_id = NULL, c.arv_regimen_line = NULL WHERE tx.id IS NULL " +
            "AND c.arv_start_date IS NULL AND (c.arv_regimen_start_date IS NOT NULL OR c.arv_regimen_name IS NOT NULL" +
            " OR c.arv_regimen_id IS NOT NULL OR c.arv_regimen_line IS NOT NULL)", nativeQuery = true)
    public int housekeeping4ART();

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE tbl_case_org co INNER JOIN tbl_case c ON c.id = co.case_id INNER JOIN tbl_person p ON p.id" +
            " = c.person_id INNER JOIN tbl_wr_case wrc ON wrc.patient_chart_id = co.patient_chart_id AND wrc.opc_id =" +
            " co.org_id AND YEAR(p.dob) = YEAR(wrc.dob) AND p.fullname = wrc.fullname SET co.enrollment_status = wrc" +
            ".tx_case_status WHERE co.patient_chart_id IS NOT NULL AND co.enrollment_type = 'NEWLY_ENROLLED' AND co" +
            ".enrollment_status IS NULL", nativeQuery = true)
    public int housekeepingCoEnrollmentStatus();

    // --------------------------------------------------
    // END: HOUSE KEEPING
    // --------------------------------------------------

    // --------------------------------------------------
    // BEGIN: CUSTOM DATA EXPORT
    // --------------------------------------------------
    @Query(value = "SELECT DISTINCT * FROM tbl_case WHERE deleted = FALSE", nativeQuery = true)
    public List<Case> getDistinctCases();

    @Query(value = "SELECT DISTINCT c.* FROM tbl_case c INNER JOIN tbl_case_org co ON c.id = co.case_id INNER JOIN " +
            "tbl_organization_unit ou ON ou.id = co.org_id INNER JOIN tbl_location loc ON loc.id = ou.address_id " +
            "WHERE loc.province_id IN (2, 4099, 10165, 9728) AND c.hivinfo_id IS NOT NULL", nativeQuery = true)
    public List<Case> findData4LySep2021_2();
    // --------------------------------------------------
    // END: CUSTOM DATA EXPORT
    // --------------------------------------------------

    /**
     * MMD status check
     */

    @Query(nativeQuery = true)
    public List<Number> checkOnARVGt6Months(@Param("organizationId") Long orgId, @Param("caseIds") List<Long> caseIds,
                                             @Param("cutpoint") Timestamp cutpoint);

    @Query(nativeQuery = true)
    public List<Number> checkVlLt50CD4Ge200_350(@Param("caseIds") List<Long> caseIds,
                                             @Param("cutpoint") Timestamp cutpoint);

    // ----------------------------------
    // BEGIN: QUERY FOR EXTRA DATA
    // ----------------------------------
    @Query(nativeQuery = true)
    public List<PatientExtraData> queryCaseExtraData(@Param("case_id") Long caseId,
                                                     @Param("cutpoint") Timestamp cutpoint);
    // ----------------------------------
    // END: QUERY FOR EXTRA DATA
    // ----------------------------------

    // ----------------------------------
    // BEGIN: QUERY FOR TX DASHBOARD
    // ----------------------------------

    @Query(nativeQuery = true)
    public Number countPendingEnrollmentPatients(@Param("organizationIds") List<Long> organizationIds);

    @Query(nativeQuery = true)
    public Number countRiskAssessmentNeeded(@Param("organizationIds") List<Long> organizationIds);

    /**
     * For active patient only
     *
     * @param organizationIds
     * @return
     */
    @Query(nativeQuery = true)
    public List<OPCDashboardRiskGroupChartData> getRiskGroupChartData(
            @Param("organizationIds") List<Long> organizationIds);

    /**
     * For active patient only
     *
     * @param organizationIds
     * @return
     */
    @Query(nativeQuery = true)
    public List<OPCDashboardTLDChartData> getTLDChartData(@Param("organizationIds") List<Long> organizationIds);

    /**
     * For active patient only
     *
     * @param organizationIds
     * @return
     */
    @Query(nativeQuery = true)
    public Number countOnMMD(@Param("organizationIds") List<Long> organizationIds);

    /**
     * For active patient only
     *
     * @param organizationIds
     * @return
     */
    @Query(nativeQuery = true)
    public Number countActivePatientOnARV(@Param("organizationIds") List<Long> organizationIds);

    @Query(nativeQuery = true)
    public List<OPCDashboardTBScreeningChartData> getTBScreeningChartData(
            @Param("organizationIds") List<Long> organizationIds, @Param("fromDate") Timestamp fromDate,
            @Param("toDate") Timestamp toDate);

    @Query(nativeQuery = true)
    public Number getPatientChartPart0(@Param("organizationIds") List<Long> organizationIds,
                                       @Param("cutPoint") Timestamp toDate);

    @Query(nativeQuery = true)
    public List<OPCDashboardPatientChartData> getPatientChartPart1(@Param("organizationIds") List<Long> organizationIds,
                                                                   @Param("fromDate") Timestamp fromDate, @Param(
            "toDate") Timestamp toDate);

    @Query(nativeQuery = true)
    public List<OPCDashboardPatientChartData2> getPatientChartPart2(
            @Param("organizationIds") List<Long> organizationIds, @Param("fromDate") Timestamp fromDate,
            @Param("toDate") Timestamp toDate);

    /**
     * For active patient only
     *
     * @param organizationIds
     * @param regimenName
     * @return
     */
    @Query(nativeQuery = true)
    public List<Case> getListPatientTLD(@Param("organizationIds") List<Long> organizationIds,
                                        @Param("regimenName") String regimenName);

    /**
     * For active patient only
     *
     * @param organizationIds
     * @return
     */
    @Query(nativeQuery = true)
    public List<Case> getListPatientRiskAssessmentNeeded(@Param("organizationIds") List<Long> organizationIds);

    /**
     * For active patient only
     *
     * @param organizationIds
     * @return
     * @Param fromDate
     * @Param toDate
     */
    @Query(nativeQuery = true)
    public List<Case> getListPatientTBScreening(@Param("organizationIds") List<Long> organizationIds,
                                                @Param("fromDate") Timestamp fromDate,
                                                @Param("toDate") Timestamp toDate);

    // ----------------------------------
    // END: QUERY FOR TX DASHBOARD
    // ----------------------------------

}
