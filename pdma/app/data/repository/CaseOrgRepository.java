package org.pepfar.pdma.app.data.repository;

import org.pepfar.pdma.app.data.domain.CaseOrg;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CaseOrgRepository
        extends PagingAndSortingRepository<CaseOrg, Serializable>,
                QueryDslPredicateExecutor<CaseOrg> {

    @Transactional
    @Modifying()
    @Query(
            "UPDATE CaseOrg co SET co.latestRelationship = false WHERE co"
                    + ".theCase.id = :caseId AND co.organization.id = :orgId")
    public void setLatestRelToFalse(@Param("caseId") Long caseId, @Param("orgId") Long orgId);

    @Transactional
    @Modifying()
    @Query(
            "UPDATE CaseOrg co SET co.patientChartId = :patientChartId WHERE "
                    + "co.theCase.id = :caseId AND co.organization.id = :orgId")
    public void updatePatientChartId(
            @Param("patientChartId") String patientChartId,
            @Param("caseId") Long caseId,
            @Param("orgId") Long orgId);

    @Transactional
    @Modifying()
    @Query(
            "UPDATE CaseOrg co SET co.startDate = :startDate WHERE co.theCase"
                    + ".id = :caseId AND co.organization.id = :orgId")
    public void updateStartDate(
            @Param("startDate") LocalDateTime startDate,
            @Param("caseId") Long caseId,
            @Param("orgId") Long orgId);

    @Query("FROM CaseOrg co WHERE co.theCase.id = :caseId AND co.organization" + ".id = :orgId")
    public List<CaseOrg> findByCaseAndOrg(@Param("caseId") Long caseId, @Param("orgId") Long orgId);

    // BEGIN: NATIVE QUERIES -->

    // ----------------------------------
    // BEGIN: VL SCHEDULING
    // ----------------------------------

    @Query(nativeQuery = true)
    public List<CaseOrg> findPatientsEligible4VLAt6thMonth(
            @Param("organizationId") Long orgId,
            @Param("caseId") Long caseId,
            @Param("atDate") Timestamp atDate,
            @Param("checkTestExistance") boolean checkTestExistance);

    @Query(nativeQuery = true)
    public List<CaseOrg> findPatientsEligible4VLAt12thMonth(
            @Param("organizationId") Long orgId,
            @Param("caseId") Long caseId,
            @Param("atDate") Timestamp atDate,
            @Param("checkTestExistance") boolean checkTestExistance);

    @Query(nativeQuery = true)
    public List<CaseOrg> findPatientsEligible4RoutineVL(
            @Param("organizationId") Long orgId,
            @Param("caseId") Long caseId,
            @Param("atDate") Timestamp atDate,
            @Param("checkTestExistance") boolean checkTestExistance);

    @Query(nativeQuery = true)
    public List<CaseOrg> findPatientsRequiringFollowupVL(
            @Param("organizationId") Long orgId,
            @Param("caseId") Long caseId,
            @Param("atDate") Timestamp atDate,
            @Param("checkTestExistance") boolean checkTestExistance);

    // ----------------------------------
    // END: VL SCHEDULING
    // ----------------------------------

    // ----------------------------------
    // BEGIN: WEEKLY REPORT CHECK
    // ----------------------------------
    @Query(
            "FROM CaseOrg co WHERE co.patientChartId LIKE :patientChartId AND co.organization.id = :orgId")
    public List<CaseOrg> checkWeeklyPatientEnrollment(
            @Param("patientChartId") String patientChartId, @Param("orgId") Long orgId);
    // ----------------------------------
    // END: WEEKLY REPORT CHECK
    // ----------------------------------

    // ----------------------------------
    // BEGIN: SHI RELATED
    // ----------------------------------
    @Query(nativeQuery = true)
    public List<CaseOrg> findPatientsWithExpiredSHI(@Param("organizationId") Long orgId);

    @Query(nativeQuery = true)
    public List<CaseOrg> findPatientsWithoutSHI(@Param("organizationId") Long orgId);
    // ----------------------------------
    // END: SHI RELATED
    // ----------------------------------

    // ----------------------------------
    // BEGIN: MER REPORT
    // ----------------------------------
    @Query(nativeQuery = true)
    public List<CaseOrg> calculate_TX_NEW_MER26(
            @Param("organizationId") Long organizationId,
            @Param("fromDate") Timestamp fromDate,
            @Param("toDate") Timestamp toDate);

    @Query(nativeQuery = true)
    public List<CaseOrg> calculate_TX_CURR_1_MER26(
            @Param("organizationId") Long organizationId,
            @Param("fromDate") Timestamp fromDate,
            @Param("toDate") Timestamp toDate,
            @Param("mmdDisaggregation") Integer mmdDisaggregation);

    @Query(nativeQuery = true)
    public List<CaseOrg> calculate_TX_CURR_2_MER26(
            @Param("organizationId") Long organizationId,
            @Param("fromDate") Timestamp fromDate,
            @Param("toDate") Timestamp toDate,
            @Param("mmdDisaggregation") Integer mmdDisaggregation);

    @Query(nativeQuery = true)
    public List<Number> calculate_TX_CURR_1_1_MER26(
            @Param("organizationId") Long organizationId,
            @Param("fromDate") Timestamp fromDate,
            @Param("toDate") Timestamp toDate);

    @Query(nativeQuery = true)
    public List<Number> calculate_TX_CURR_2_1_MER26(
            @Param("organizationId") Long organizationId,
            @Param("fromDate") Timestamp fromDate,
            @Param("toDate") Timestamp toDate);

    @Query(nativeQuery = true)
    public List<CaseOrg> calculate_TX_ML_DEAD_STOPPED_MER26(
            @Param("organizationId") Long organizationId,
            @Param("fromDate") Timestamp fromDate,
            @Param("toDate") Timestamp toDate,
            @Param("status") String status);

    @Query(nativeQuery = true)
    public List<CaseOrg> calculate_TX_ML_TRANSOUT_1_MER26(
            @Param("organizationId") Long organizationId,
            @Param("fromDate") Timestamp fromDate,
            @Param("toDate") Timestamp toDate);

    @Query(nativeQuery = true)
    public List<CaseOrg> calculate_TX_ML_TRANSOUT_2_MER26(
            @Param("organizationId") Long organizationId,
            @Param("fromDate") Timestamp fromDate,
            @Param("toDate") Timestamp toDate);

    @Query(nativeQuery = true)
    public List<CaseOrg> calculate_TX_ML_IIT_MER26(
            @Param("organizationId") Long organizationId,
            @Param("fromDate") Timestamp fromDate,
            @Param("toDate") Timestamp toDate,
            @Param("disaggregation") Integer disaggregation);

    @Query(nativeQuery = true)
    public List<CaseOrg> calculate_TX_RTT_MER26(
            @Param("organizationId") Long organizationId,
            @Param("fromDate") Timestamp fromDate,
            @Param("toDate") Timestamp toDate,
            @Param("disaggregation") Integer disaggregation,
            @Param("activeCaseIdsLastPeriodEnd") List<Number> activeCaseIdsLastPeriodEnd,
            @Param("activeCaseIdsThisPeriodEnd") List<Number> activeCaseIdsThisPeriodEnd);

    @Query(nativeQuery = true)
    public List<CaseOrg> calculate_TB_PREV_MER26(
            @Param("organizationId") Long organizationId,
            @Param("fromDate") Timestamp fromDate,
            @Param("toDate") Timestamp toDate,
            @Param("within6Month") Boolean within6Month,
            @Param("numerator") Boolean numerator);

    @Query(nativeQuery = true)
    public List<CaseOrg> calculate_TX_TB_Numerator_MER26(
            @Param("organizationId") Long organizationId,
            @Param("fromDate") Timestamp fromDate,
            @Param("toDate") Timestamp toDate,
            @Param("arvInitationDuringPeriod") Boolean arvInitationDuringPeriod);

    @Query(nativeQuery = true)
    public List<CaseOrg> calculate_TX_TB_Denominator_MER26(
            @Param("organizationId") Long organizationId,
            @Param("fromDate") Timestamp fromDate,
            @Param("toDate") Timestamp toDate,
            @Param("screenedPos") Boolean screenedPos,
            @Param("arvInitationDuringPeriod") Boolean arvInitationDuringPeriod);

    // ----------------------------------
    // END: MER REPORT
    // ----------------------------------

    // ----------------------------------
    // BEGIN: OPC-REPORT
    // ----------------------------------

    @Query(nativeQuery = true)
    public List<CaseOrg> findPartiallyActivePatients(
            @Param("organizationId") Long orgId,
            @Param("cutpoint") Timestamp cutpoint,
            @Param("arvOnly") boolean arvOnly);

    @Query(nativeQuery = true)
    public List<CaseOrg> findTransferredOutPatients(
            @Param("organizationId") Long orgId,
            @Param("fromDate") Timestamp fromDate,
            @Param("toDate") Timestamp toDate,
            @Param("arvOnly") boolean arvOnly);

    @Query(nativeQuery = true)
    public List<CaseOrg> findTransferredOutPatients_Circular03(
            @Param("organizationId") Long orgId,
            @Param("fromDate") Timestamp fromDate,
            @Param("toDate") Timestamp toDate,
            @Param("arvOnly") boolean arvOnly);

    @Query(nativeQuery = true)
    public List<CaseOrg> findTransferredOutPendingPatients(
            @Param("organizationId") Long orgId,
            @Param("fromDate") Timestamp fromDate,
            @Param("toDate") Timestamp toDate,
            @Param("arvOnly") boolean arvOnly);

    @Query(nativeQuery = true)
    public List<CaseOrg> findTransferredOutCompensate(
            @Param("organizationId") Long orgId,
            @Param("fromDate") Timestamp fromDate,
            @Param("toDate") Timestamp toDate,
            @Param("followingFromDate") Timestamp followingFromDate,
            @Param("followingToDate") Timestamp followingToDate,
            @Param("arvOnly") boolean arvOnly);

    @Query(nativeQuery = true)
    public List<CaseOrg> findDeadAndLTFUPatients(
            @Param("organizationId") Long orgId,
            @Param("fromDate") Timestamp fromDate,
            @Param("toDate") Timestamp toDate,
            @Param("statuses") List<String> statuses,
            @Param("arvOnly") boolean arvOnly);

    @Query(nativeQuery = true)
    public List<CaseOrg> findPatientsByEnrollmentTypes(
            @Param("organizationId") Long orgId,
            @Param("fromDate") Timestamp fromDate,
            @Param("toDate") Timestamp toDate,
            @Param("enrollmentTypes") List<String> enrollmentTypes,
            @Param("arvOnly") boolean arvOnly);

    @Query(nativeQuery = true)
    public List<CaseOrg> findCaseOrgsSuccessfulReferred(
            @Param("organizationId") Long orgId,
            @Param("fromDate") Timestamp fromDate,
            @Param("toDate") Timestamp toDate);

    // --> begin by ARV regimen line

    // regimenLine = 1: line 1
    // regimenLine = 2: line 2
    @Query(nativeQuery = true)
    public List<CaseOrg> findCaseOrgsByRegimenLine(
            @Param("caseIds") List<Long> caseIds,
            @Param("organizationId") Long orgId,
            @Param("toDate") Timestamp toDate,
            @Param("regimenLine") int regimenLine);
    // <-- end by ARV regimen line

    // --> begin SDA (same day ART)
    // indicator = 0: SDA indicator#1
    // indicator = 1: SDA indicator#2
    // indicator = 2: SDA indicator#3
    // indicator = 3: SDA indicator#4
    @Query(nativeQuery = true)
    public List<CaseOrg> findCases4SameDayART(
            @Param("organizationId") Long orgId,
            @Param("indicator") int indicator,
            @Param("fromDate") Timestamp fromDate,
            @Param("toDate") Timestamp toDate);
    // <-- end SDA

    // --> begin MMD
    // indicator = 1: Eligible for MMD
    // indicator = 2: On MMD
    @Query(nativeQuery = true)
    public List<CaseOrg> findCases4MMDIndicators(
            @Param("organizationId") Long orgId,
            @Param("indicator") int indicator,
            @Param("shiDrugSource") boolean shiDrugSource,
            @Param("toDate") Timestamp toDate);
    // <-- end MMD

    // --> begin TLD
    @Query(nativeQuery = true)
    public List<CaseOrg> findNewCasesUsingTLD(
            @Param("organizationId") Long orgId,
            @Param("fromDate") Timestamp fromDate,
            @Param("toDate") Timestamp toDate);

    @Query(nativeQuery = true)
    public List<CaseOrg> findCasesTransition2TLD(
            @Param("organizationId") Long orgId,
            @Param("fromDate") Timestamp fromDate,
            @Param("toDate") Timestamp toDate);

    @Query(nativeQuery = true)
    public List<CaseOrg> findCasesOnTLDAtCutpoint_C03(
            @Param("organizationId") Long orgId, @Param("toDate") Timestamp toDate);

    @Query(nativeQuery = true)
    public List<CaseOrg> findCasesOnTLDAtCutpoint_MER(
            @Param("organizationId") Long orgId,
            @Param("use28DaysRule") boolean use28DaysRule,
            @Param("fromDate") Timestamp fromDate,
            @Param("toDate") Timestamp toDate);

    @Query(nativeQuery = true)
    public List<CaseOrg> findCasesOnTLDAtCutpoint_MER_TransedOutPendingEnrollment(
            @Param("organizationId") Long orgId,
            @Param("fromDate") Timestamp fromDate,
            @Param("toDate") Timestamp toDate);
    // <-- end TLD

    // --> begin COHORT
    @Query(nativeQuery = true)
    public List<CaseOrg> cohortInitialPatients(
            @Param("organizationId") Long orgId,
            @Param("lastYearFromDate") Timestamp lastYearFromDate,
            @Param("lastYearToDate") Timestamp lastYearToDate);

    @Query(nativeQuery = true)
    public List<CaseOrg> cohortTransferredInPatients(
            @Param("organizationId") Long orgId,
            @Param("lastYearFromDate") Timestamp lastYearFromDate,
            @Param("lastYearToDate") Timestamp lastYearToDate,
            @Param("toDate") Timestamp toDate);

    @Query(nativeQuery = true)
    public List<CaseOrg> cohortTransferredOutPatients(
            @Param("organizationId") Long orgId,
            @Param("lastYearFromDate") Timestamp lastYearFromDate,
            @Param("lastYearToDate") Timestamp lastYearToDate,
            @Param("toDate") Timestamp toDate);

    @Query(nativeQuery = true)
    public List<CaseOrg> cohortDeadLTFUPatients(
            @Param("organizationId") Long orgId,
            @Param("lastYearFromDate") Timestamp lastYearFromDate,
            @Param("lastYearToDate") Timestamp lastYearToDate,
            @Param("toDate") Timestamp toDate,
            @Param("status") String status);

    // <-- end COHORT

    // ----------------------------------
    // BEGIN: OPC-REPORT
    // ----------------------------------

    // ----------------------------------
    // BEGIN: OTHER NATIVE QUERIES
    // ----------------------------------

    /**
     * Find all active patient currently on MMD
     *
     * @param orgIds
     * @return
     */
    public List<CaseOrg> findActivePatientsOnMMD(@Param("organizationIds") List<Long> orgIds);

    /**
     * Find all latest case-orgs for one organization including all statusesin (ACTIVE, LTFU, DEAD,
     * TRANSFERRED_OUT) and excluding deleted cases
     *
     * @param orgId
     * @return
     */
    @Query(nativeQuery = true)
    public List<CaseOrg> findCaseOrgs(@Param("organizationId") Long orgId);

    @Query(nativeQuery = true)
    public List<CaseOrg> checkPatientChartIdExistance(
            @Param("organizationId") Long orgId,
            @Param("patientChartId") String patientChartId,
            @Param("coId") Long coId);

    @Query(nativeQuery = true)
    public List<CaseOrg> checkNationalIdExists(
            @Param("organizationId") Long orgId,
            @Param("nationalIdNumber") String nationalIdNumber,
            @Param("caseId") Long caseId);

    @Query(nativeQuery = true)
    public List<CaseOrg> findMatchedPatientRecords(
            @Param("caseId") Long caseId,
            @Param("nationalId") String nationalId,
            @Param("passportNumber") String passportNumber,
            @Param("hivinfoId") String hivinfoId,
            @Param("fullname") String fullname,
            @Param("dob") Timestamp dob,
            @Param("gender") String gender,
            @Param("provinceId") Long provinceId,
            @Param("districtId") Long districtId);

    // Count Case-Orgs pageable
    @Query(nativeQuery = true)
    public Number countCaseOrgs_WAppointment_Total(
            @Param("orgIds") List<Long> orgIds,
            @Param("keyword") String keyword,
            @Param("hivConfirmDateFrom") Timestamp hivConfirmDateFrom,
            @Param("hivConfirmDateTo") Timestamp hivConfirmDateTo,
            @Param("arvStartDateFrom") Timestamp arvStartDateFrom,
            @Param("arvStartDateTo") Timestamp arvStartDateTo,
            @Param("dobFrom") Timestamp dobFrom,
            @Param("dobTo") Timestamp dobTo,
            @Param("gender") String gender,
            @Param("enrollmentType") String enrollmentType,
            @Param("txStatus") String txStatus,
            @Param("includeDeleted") boolean includeDeleted,
            @Param("includeOnART") boolean includeOnART,
            @Param("includePreART") boolean includePreART,
            @Param("appResult") String appResult,
            @Param("appFromDate") Timestamp appFromDate,
            @Param("appToDate") Timestamp appToDate);

    @Query(nativeQuery = true)
    public List<CaseOrg> findCaseOrgs_WAppointment_Pageable(
            @Param("orgIds") List<Long> orgIds,
            @Param("keyword") String keyword,
            @Param("hivConfirmDateFrom") Timestamp hivConfirmDateFrom,
            @Param("hivConfirmDateTo") Timestamp hivConfirmDateTo,
            @Param("arvStartDateFrom") Timestamp arvStartDateFrom,
            @Param("arvStartDateTo") Timestamp arvStartDateTo,
            @Param("dobFrom") Timestamp dobFrom,
            @Param("dobTo") Timestamp dobTo,
            @Param("gender") String gender,
            @Param("enrollmentType") String enrollmentType,
            @Param("txStatus") String txStatus,
            @Param("includeDeleted") boolean includeDeleted,
            @Param("includeOnART") boolean includeOnART,
            @Param("includePreART") boolean includePreART,
            @Param("appResult") String appResult,
            @Param("appFromDate") Timestamp appFromDate,
            @Param("appToDate") Timestamp appToDate,
            @Param("sortField") int sortField,
            @Param("pageIndex") int offset,
            @Param("pageSize") int pageSize);

    @Query(nativeQuery = true)
    public Number countCaseOrgs_WoAppointment_Total(
            @Param("orgIds") List<Long> orgIds,
            @Param("keyword") String keyword,
            @Param("arvGroup") String arvGroup,
            @Param("hivConfirmDateFrom") Timestamp hivConfirmDateFrom,
            @Param("hivConfirmDateTo") Timestamp hivConfirmDateTo,
            @Param("arvStartDateFrom") Timestamp arvStartDateFrom,
            @Param("arvStartDateTo") Timestamp arvStartDateTo,
            @Param("dobFrom") Timestamp dobFrom,
            @Param("dobTo") Timestamp dobTo,
            @Param("gender") String gender,
            @Param("enrollmentType") String enrollmentType,
            @Param("txStatus") String txStatus,
            @Param("includeDeleted") boolean includeDeleted,
            @Param("includeOnART") boolean includeOnART,
            @Param("includePreART") boolean includePreART);

    @Query(nativeQuery = true)
    public List<CaseOrg> findCaseOrgs_WoAppointment_Pageable(
            @Param("orgIds") List<Long> orgIds,
            @Param("keyword") String keyword,
            @Param("arvGroup") String arvGroup,
            @Param("hivConfirmDateFrom") Timestamp hivConfirmDateFrom,
            @Param("hivConfirmDateTo") Timestamp hivConfirmDateTo,
            @Param("arvStartDateFrom") Timestamp arvStartDateFrom,
            @Param("arvStartDateTo") Timestamp arvStartDateTo,
            @Param("dobFrom") Timestamp dobFrom,
            @Param("dobTo") Timestamp dobTo,
            @Param("gender") String gender,
            @Param("enrollmentType") String enrollmentType,
            @Param("txStatus") String txStatus,
            @Param("includeDeleted") boolean includeDeleted,
            @Param("includeOnART") boolean includeOnART,
            @Param("includePreART") boolean includePreART,
            @Param("sortField") int sortField,
            @Param("pageIndex") int offset,
            @Param("pageSize") int pageSize);

    // ----------------------------------
    // END: OTHER NATIVE QUERIES
    // ----------------------------------

}
