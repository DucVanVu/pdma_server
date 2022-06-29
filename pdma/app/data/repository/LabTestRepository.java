package org.pepfar.pdma.app.data.repository;

import org.pepfar.pdma.app.data.domain.LabTest;
import org.pepfar.pdma.app.data.dto.OPCDashboardVLChartData;
import org.pepfar.pdma.app.data.types.ClinicalTestingType;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface LabTestRepository
        extends PagingAndSortingRepository<LabTest, Serializable>,
                QueryDslPredicateExecutor<LabTest> {

    @Query("FROM LabTest WHERE uid = :uid")
    public List<LabTest> findByUID(@Param("uid") UUID uid);

    @Query(
            "FROM LabTest WHERE theCase.id = :caseId AND sampleDate >= :fromDate AND sampleDate <= :toDate AND testType = :testType")
    public List<LabTest> findBySampleDate(
            @Param("caseId") Long caseId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("testType") ClinicalTestingType testType);

    @Modifying
    @Query(
            "DELETE FROM LabTest WHERE theCase.id = :caseId AND sampleDate >= :fromDate AND sampleDate <= :toDate")
    public void deleteBySampleDate(
            @Param("caseId") Long caseId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);

    // --------------------------------------
    // BEGIN: Export lab test data
    // --------------------------------------

    @Query(nativeQuery = true)
    public List<LabTest> findLabtestData(
            @Param("organizationIds") List<Long> orgIds,
            @Param("testTypes") List<String> testTypes,
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate);

    @Query(
            value =
                    "SELECT * FROM tbl_lab_test WHERE test_type='VIRAL_LOAD' AND sample_date <= :endDate AND case_id IN (:caseIds)",
            nativeQuery = true)
    public List<LabTest> findViralLoadLabTests4LySep2021(
            @Param("caseIds") List<Long> caseIds, @Param("endDate") Timestamp endDate);

    // --------------------------------------
    // END: Export lab test data
    // --------------------------------------

    // ----------------------------------
    // BEGIN: VL REPORTING
    // ----------------------------------
    @Query(nativeQuery = true)
    public List<LabTest> calculate_TX_PVLS_MER26(
            @Param("organizationId") Long orgIds,
            @Param("fromDate") Timestamp startDate,
            @Param("toDate") Timestamp endDate,
            @Param("reason") Integer reason,
            @Param("numerator") Boolean numerator);

    @Query(nativeQuery = true)
    public List<LabTest> findLatestVLTestsInDateRange(
            @Param("organizationIds") List<Long> orgIds,
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate,
            @Param("reason") Integer reason,
            @Param("resultRange") String resultRange,
            @Param("onlyShi") Boolean onlyShi);

    @Query(nativeQuery = true)
    public List<LabTest> findLatest1stVLTestsInDateRange(
            @Param("organizationIds") List<Long> orgIds,
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate);

    @Query(nativeQuery = true)
    public List<LabTest> findLatest2ndVLTestsInDateRange(
            @Param("organizationIds") List<Long> orgIds,
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate);

    @Query(nativeQuery = true)
    public List<LabTest> findLatestVLTestsFor2ndLinePatientsInDateRange(
            @Param("organizationIds") List<Long> orgIds,
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate);

    @Query(nativeQuery = true)
    public List<LabTest> findLatestVLTestsUsingShiInDateRange(
            @Param("organizationIds") List<Long> orgIds,
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate);

    // ----------------------------------
    // END: VL REPORTING
    // ----------------------------------

    // ----------------------------------
    // BEGIN: OPC DASHBOARD QUERIES
    // ----------------------------------

    @Query(nativeQuery = true)
    public Number countTestsMissingResult(
            @Param("organizationIds") List<Long> orgIds, @Param("testType") String testType);

    @Query(nativeQuery = true)
    public List<OPCDashboardVLChartData> getVLChartData(
            @Param("organizationIds") List<Long> orgIds,
            @Param("fromDate") Timestamp fromDate,
            @Param("toDate") Timestamp toDate);

    @Query(nativeQuery = true)
    public List<LabTest> getListVLChartData(
            @Param("organizationIds") List<Long> orgIds,
            @Param("fromDate") Timestamp fromDate,
            @Param("toDate") Timestamp toDate);
    // ----------------------------------
    // END: OPC DASHBOARD QUERIES
    // ----------------------------------
}
