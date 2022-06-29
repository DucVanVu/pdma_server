package org.pepfar.pdma.app.data.repository;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.pepfar.pdma.app.data.domain.TBTreatment2;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TBTreatment2Repository
		extends PagingAndSortingRepository<TBTreatment2, Serializable>, QueryDslPredicateExecutor<TBTreatment2> {
	@Query("FROM TBTreatment2 WHERE uid = :uid")
	public List<TBTreatment2> findByUID(@Param("uid") UUID uid);

	/**
	 * @formatter:off
	 * Chi so 1.1 trong mục II (quy 4): Số bệnh nhân lao/HIV đang điều trị lao, mới được tiếp nhận đăng ký CSĐT HIV tại cơ sở trong kỳ báo cáo.
	 * Chi so 1.2 trong mục II (quy 4): Số bệnh nhân lao/HIV đang điều trị lao, mới đăng ký CSĐT HIV từ đầu năm và bắt đầu được điều trị ARV trong kỳ báo cáo (arvOnly=true)
	 
	 * @formatter:on
	 *
	 * @param orgId
	 * @param screeningTypes
	 * @param fromDate
	 * @param toDate
	 * @param fromYearDate in year
	 * @param toYearDate in year
	 * @param enrollmentTypes
	 * @param arvOnly
	 * @return
	 */
	@Query(nativeQuery = true)
	public List<TBTreatment2> findTBTreatment2TBScreeningType(@Param("organizationId") Long orgId,
			@Param("screeningTypes") List<String> screeningTypes, @Param("fromYearDate") Timestamp fromYearDate,
			@Param("toYearDate") Timestamp toYearDate, @Param("enrollmentTypes") List<String> enrollmentTypes,
			@Param("arvOnly") boolean arvOnly);
	
//	hàm sửa lỗi báo cáo quí 4 1.2 phần II
	@Query(nativeQuery = true)
	public List<TBTreatment2> findTBTreatment2TBScreeningType_2(@Param("organizationId") Long orgId, @Param("fromYearDate") Timestamp fromYearDate,
			@Param("toYearDate") Timestamp toYearDate
			);
	

	/**
	 * @formatter:off
	 * Chi so 2 trong mục III (quy 4): Số bệnh nhân điều trị ARV nghi lao đi khám lao  trong kỳ báo cáo (do cơ sở điều trị HIV chuyển gửi/tự BN đi khám lao)
		 
	 * @formatter:on
	 *
	 * @param orgId
	 * @param screeningTypes
	 
	 * @param fromYearDate in year
	 * @param toYearDate in year
	 * @param arvOnly
	 * @return
	 */
	@Query(nativeQuery = true)
	public List<TBTreatment2> findTBTreatment2TBScreeningDateAndType(@Param("organizationId") Long orgId,
			@Param("screeningTypes") List<String> screeningTypes, @Param("fromYearDate") Timestamp fromYearDate,
			@Param("toYearDate") Timestamp toYearDate, @Param("arvOnly") boolean arvOnly);

	/**
	 * @formatter:off
	 * Chi so 3 trong mục III (quy 4):Số bệnh nhân điều trị ARV nghi lao và được khám lao trong kỳ báo cáo (tại cơ sở lao hoặc cơ sở ngoài CT chống lao) 
	 * Chi so 4 trong mục III (quy 4):Số bệnh nhân điều trị ARV được chẩn đoán mắc lao trong kỳ báo cáo
	 * @formatter:on
	 *
	 * @param orgId
	 * @param screeningTypes
	 
	 * @param fromYearDate in year
	 * @param toYearDate in year
	 * @param arvOnly
	 * @return
	 */
	@Query(nativeQuery = true)
	public List<TBTreatment2> findTBTreatment2TBDiagnosed(@Param("organizationId") Long orgId,
			@Param("tbDiagnoseds") List<String> tbDiagnoseds, @Param("fromYearDate") Timestamp fromYearDate,
			@Param("toYearDate") Timestamp toYearDate, @Param("arvOnly") boolean arvOnly);

	/**
	 * @formatter:off
	 * Chi so 5 trong mục III (quy 4):Số bệnh nhân điều trị ARV được chẩn đoán và điều trị Lao trong kỳ báo cáo
	
	 * @formatter:on
	 *
	 * @param orgId
	
	 * @param fromYearDate in year
	 * @param toYearDate in year
	 * @param arvOnly
	 * @return
	 */
	@Query(nativeQuery = true)
	public List<TBTreatment2> findTBTreatment2TBStartDate(@Param("organizationId") Long orgId,
			@Param("fromYearDate") Timestamp fromYearDate, @Param("toYearDate") Timestamp toYearDate,
			@Param("arvOnly") boolean arvOnly);

	/**
	 * @formatter:off
	 * Chi so 3 trong mục II (6 tháng): 3.Số bệnh nhân điều trị ARV nghi lao đi khám lao  trong kỳ báo cáo (do cơ sở điều trị ARV chuyển gửi/tự BN đi khám lao)
	 * <!-- BN bắt đầu ARV trong kỳ báo cáo -->
	 * @formatter:on
	 *
	 * @param orgId
	 * @param screeningTypes
	 
	 * @param fromDate 
	 * @param toDate 
	 * @param arvOnly
	 * @return
	 */
	@Query(nativeQuery = true)
	public List<TBTreatment2> findTBTreatment2TBScreeningARVInReportPeriod(@Param("organizationId") Long orgId,
			@Param("screeningTypes") List<String> screeningTypes, @Param("fromDate") Timestamp fromDate,
			@Param("toDate") Timestamp toDate, @Param("arvOnly") boolean arvOnly);

	/**
	 * @formatter:off
	 * Chi so 3 trong mục II (6 tháng): 3.Số bệnh nhân điều trị ARV nghi lao đi khám lao  trong kỳ báo cáo (do cơ sở điều trị ARV chuyển gửi/tự BN đi khám lao)
	 * <!--BN bắt đầu điều trị ARV trước kỳ báo cáo-->
	 * @formatter:on
	 *
	 * @param orgId
	 * @param screeningTypes
	 
	 * @param fromDate 
	 * @param toDate 
	 * @param arvOnly
	 * @return
	 */
	@Query(nativeQuery = true)
	public List<TBTreatment2> findTBTreatment2TBScreeningARVPreReportPeriod(@Param("organizationId") Long orgId,
			@Param("screeningTypes") List<String> screeningTypes, @Param("fromDate") Timestamp fromDate,
			@Param("toDate") Timestamp toDate, @Param("arvOnly") boolean arvOnly);

	/**
	 * @formatter:off
	 * Chi so 4,5 trong mục II (6 tháng): Số bệnh nhân điều trị ARV nghi lao và được khám lao (tại cơ sở lao hoặc cơ sở ngoài CT chống lao) trong kỳ báo cáo
	 * Số bệnh nhân điều trị ARV được chẩn đoán mắc lao trong kỳ báo cáo
	 * <!-- BN bắt đầu ARV trong kỳ báo cáo -->
	 * @formatter:on
	 *
	 * @param orgId
	 * @param screeningTypes
	 
	 * @param fromDate 
	 * @param toDate 
	 * @param arvOnly
	 * @return
	 */
	@Query(nativeQuery = true)
	public List<TBTreatment2> findTBTreatment2TBDiagnosedARVInReportPeriod(@Param("organizationId") Long orgId,
			@Param("tbDiagnoseds") List<String> tbDiagnoseds, @Param("fromDate") Timestamp fromDate,
			@Param("toDate") Timestamp toDate, @Param("arvOnly") boolean arvOnly);

	/**
	 * @formatter:off
	 * Chi so 4,5 trong mục II (6 tháng): Số bệnh nhân điều trị ARV nghi lao và được khám lao (tại cơ sở lao hoặc cơ sở ngoài CT chống lao) trong kỳ báo cáo
	 * Số bệnh nhân điều trị ARV được chẩn đoán mắc lao trong kỳ báo cáo
	 * <!--BN bắt đầu điều trị ARV trước kỳ báo cáo-->
	 * @formatter:on
	 *
	 * @param orgId
	 * @param screeningTypes
	 
	 * @param fromDate 
	 * @param toDate 
	 * @param arvOnly
	 * @return
	 */
	@Query(nativeQuery = true)
	public List<TBTreatment2> findTBTreatment2TBDiagnosedARVPreReportPeriod(@Param("organizationId") Long orgId,
			@Param("tbDiagnoseds") List<String> tbDiagnoseds, @Param("fromDate") Timestamp fromDate,
			@Param("toDate") Timestamp toDate, @Param("arvOnly") boolean arvOnly);

	/**
	 * @formatter:off
	 * Chi so 4.1, 4.2, 4.3, 4.4 trong mục II (6 tháng): 
	 * 4.1 Có xét nghiệm soi đờm trực tiếp - sputumSmear
	 * 4.2 Có xét nghiệm Xpert -xpert
	 * 4.3 Có chụp X quang ngực -xray
	 * 4.4 Có làm các xét nghiệm khác (hạch đồ, dịch màng phổi, bụng……) -otherTest
	 * <!--BN bắt đầu điều trị ARV trong kỳ báo cáo-->
	 * @formatter:on
	 *
	 * @param orgId
	 * @param screeningTypes
	 
	 * @param fromDate 
	 * @param toDate 
	 * @param arvOnly
	 * @return
	 */
	@Query(nativeQuery = true)
	public List<TBTreatment2> findTBTreatment2TBDiagnosed4InReportPeriod(@Param("organizationId") Long orgId,
			@Param("tbDiagnoseds") List<String> tbDiagnoseds, @Param("sputumSmear") boolean sputumSmear,
			@Param("xpert") boolean xpert, @Param("xray") boolean xray, @Param("otherTest") boolean otherTest,
			@Param("fromDate") Timestamp fromDate, @Param("toDate") Timestamp toDate,
			@Param("arvOnly") boolean arvOnly);

	/**
	 * @formatter:off
	 * Chi so 4.1, 4.2, 4.3, 4.4 trong mục II (6 tháng): 
	 * 4.1 Có xét nghiệm soi đờm trực tiếp - sputumSmear
	 * 4.2 Có xét nghiệm Xpert -xpert
	 * 4.3 Có chụp X quang ngực -xray
	 * 4.4 Có làm các xét nghiệm khác (hạch đồ, dịch màng phổi, bụng……) -otherTest
	 * <!--BN bắt đầu điều trị ARV trước kỳ báo cáo-->
	 * @formatter:on
	 *
	 * @param orgId
	 * @param screeningTypes
	 
	 * @param fromDate 
	 * @param toDate 
	 * @param arvOnly
	 * @return
	 */
	@Query(nativeQuery = true)
	public List<TBTreatment2> findTBTreatment2TBDiagnosed4PreReportPeriod(@Param("organizationId") Long orgId,
			@Param("tbDiagnoseds") List<String> tbDiagnoseds, @Param("sputumSmear") boolean sputumSmear,
			@Param("xpert") boolean xpert, @Param("xray") boolean xray, @Param("otherTest") boolean otherTest,
			@Param("fromDate") Timestamp fromDate, @Param("toDate") Timestamp toDate,
			@Param("arvOnly") boolean arvOnly);

	/**
	 * @formatter:off
	 * Chi so 6 trong mục II (6 tháng): Số bệnh nhân điều trị ARV được chẩn đoán và điều trị Lao trong kỳ báo cáo
	 * <!-- BN bắt đầu ARV trong kỳ báo cáo -->
	 * @formatter:on
	 *
	 * @param orgId
	 * @param screeningTypes
	 
	 * @param fromDate 
	 * @param toDate 
	 * @param arvOnly
	 * @return
	 */
	@Query(nativeQuery = true)
	public List<TBTreatment2> findTBTreatment2TBStartDateARVInReportPeriod(@Param("organizationId") Long orgId,
			@Param("fromDate") Timestamp fromDate, @Param("toDate") Timestamp toDate,
			@Param("arvOnly") boolean arvOnly);

	/**
	 * @formatter:off
	 * Chi so 3 trong mục II (6 tháng): Số bệnh nhân điều trị ARV được chẩn đoán và điều trị Lao trong kỳ báo cáo
	 * <!--BN bắt đầu điều trị ARV trước kỳ báo cáo-->
	 * @formatter:on
	 *
	 * @param orgId
	 * @param screeningTypes
	 
	 * @param fromDate 
	 * @param toDate 
	 * @param arvOnly
	 * @return
	 */
	@Query(nativeQuery = true)
	public List<TBTreatment2> findTBTreatment2TBStartDateARVPreReportPeriod(@Param("organizationId") Long orgId,
			@Param("fromDate") Timestamp fromDate, @Param("toDate") Timestamp toDate,
			@Param("arvOnly") boolean arvOnly);
	
	@Query("FROM TBTreatment2 tb2 WHERE tb2.theCase.id = :caseId and tb2.screeningDate <= :toDate order by tb2.screeningDate DESC  ")
	public List<TBTreatment2> findTBTreatment2ByCaseId(@Param("caseId") Long caseId, @Param("toDate") LocalDateTime toDate);
}
