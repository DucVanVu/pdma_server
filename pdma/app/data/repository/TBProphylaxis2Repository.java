package org.pepfar.pdma.app.data.repository;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.pepfar.pdma.app.data.domain.TBProphylaxis2;
import org.pepfar.pdma.app.data.domain.TBProphylaxis2Dispense;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TBProphylaxis2Repository
		extends PagingAndSortingRepository<TBProphylaxis2, Serializable>, QueryDslPredicateExecutor<TBProphylaxis2> {
	@Query("FROM TBProphylaxis2 WHERE uid = :uid")
	public List<TBProphylaxis2> findByUUID(@Param("uid") UUID uid);

	/**
	 * @formatter:off
	 * Chi so 2.1: Số bệnh nhân HIV mới đăng ký CSĐT HIV trong năm được bắt đầu dự phòng lao trong  kỳ báo cáo 
	 
	 * @formatter:on
	 *
	 * @param orgId
	 * @param fromDate
	 * @param toDate
	 * @param fromYearDate in year
	 * @param toYearDate in year
	 * @param enrollmentTypes
	 * @return
	 */
	@Query(nativeQuery = true)
	public List<TBProphylaxis2> findTBProphylaxis2StartDate(@Param("organizationId") Long orgId,
			@Param("fromDate") Timestamp fromDate, @Param("toDate") Timestamp toDate,
			@Param("fromYearDate") Timestamp fromYearDate, @Param("toYearDate") Timestamp toYearDate,
			@Param("enrollmentTypes") List<String> enrollmentTypes);

	/**
	 * @formatter:off
	 * Chi so 2.2: Số bệnh nhân HIV mới đăng ký CSĐT HIV trong năm được tiếp tục dự phòng lao tại cơ sở trong kỳ báo cáo 
	 * (nhóm BN chuyển tới đã bắt đầu dự phòng lao trước đó)
	 
	 * @formatter:on
	 *
	 * @param orgId
	 * @param fromDate
	 * @param toDate
	 * @param fromYearDate in year
	 * @param toYearDate in year
	 * @param enrollmentTypes
	 * @return
	 */
	@Query(nativeQuery = true)
	public List<TBProphylaxis2Dispense> findTBProphylaxis2RecordDate(@Param("organizationId") Long orgId,
			@Param("fromDate") Timestamp fromDate, @Param("toDate") Timestamp toDate,
			@Param("fromYearDate") Timestamp fromYearDate, @Param("toYearDate") Timestamp toYearDate,
			@Param("enrollmentTypes") List<String> enrollmentTypes);

	/**
	 * @formatter:off
	 * Chi so 3.1: Số bệnh nhân HIV đăng ký CSĐT từ những năm trước được bắt đầu dự phòng lao trong kỳ báo cáo
	 
	 * @formatter:on
	 *
	 * @param orgId
	 * @param fromDate
	 * @param toDate
	 * @param fromYearDate in year
	 * @param enrollmentTypes
	 * @return
	 */
	@Query(nativeQuery = true)
	public List<TBProphylaxis2> findTBProphylaxis2StartDateByEnrollmentTypesPreYear(@Param("organizationId") Long orgId,
			@Param("fromDate") Timestamp fromDate, @Param("toDate") Timestamp toDate,
			@Param("fromYearDate") Timestamp fromYearDate, @Param("enrollmentTypes") List<String> enrollmentTypes);

	/**
	 * @formatter:off
	 * Chi so 3.2: Số bệnh nhân HIV đăng ký CSĐT từ những năm trước được tiếp tục dự phòng lao trong kỳ báo cáo
	 
	 * @formatter:on
	 *
	 * @param orgId
	 * @param fromDate
	 * @param toDate
	 * @param fromYearDate in year
	 * @param enrollmentTypes
	 * @return
	 */
	@Query(nativeQuery = true)
	public List<TBProphylaxis2Dispense> findTBProphylaxis2RecordDateByEnrollmentTypesPreYear(
			@Param("organizationId") Long orgId, @Param("fromDate") Timestamp fromDate,
			@Param("toDate") Timestamp toDate, @Param("fromYearDate") Timestamp fromYearDate,
			@Param("enrollmentTypes") List<String> enrollmentTypes);

	/**
	 * @formatter:off
	 * Chi so 5.2: Số bệnh nhân HIV hiện quản lý đã và đang được dự phòng lao từ khi đăng ký CSĐT HIV đến cuối kỳ báo cáo
	 
	 * @formatter:on
	 *
	 * @param orgId
	 * @param toDate
	 * @param enrollmentTypes
	 * @return
	 */
	@Query(nativeQuery = true)
	public List<TBProphylaxis2> findTBProphylaxis2StartDateEndOfReportPeriod(@Param("organizationId") Long orgId,
			@Param("toDate") Timestamp toDate, @Param("enrollmentTypes") List<String> enrollmentTypes);

	/**
	 * @formatter:off
	 * Chi so 5.3: Số bệnh nhân HIV hiện quản lý đã hoàn thành dự phòng lao (ít nhất 180 liều -phác đồ INH, đủ 12 liều -phác đồ 3HP) tính đến cuối kỳ báo cáo
	 
	 * @formatter:on
	 *
	 * @param orgId
	 * @param toDate
	 * @param enrollmentTypes
	 * @return
	 */
	@Query(nativeQuery = true)
	public List<TBProphylaxis2> findTBProphylaxis2CompleteEndOfReportPeriod(@Param("organizationId") Long orgId,
			@Param("toDate") Timestamp toDate, @Param("enrollmentTypes") List<String> enrollmentTypes);

	/*************** Bắt đầu Query báo cáo hoạt động lao/HIV 6 tháng **************/
	/**
	 * @formatter:off
	 * Chi so 1.1 Số bệnh nhân điều trị ARV bắt đầu dự phòng Lao trong kỳ báo cáo 6 tháng trước. 
	 * BN bắt đầu điều trị ARV trong kỳ báo cáo 6 tháng trước.
	 
	 * @formatter:on
	 *
	 * @param orgId
	 * @param preFromDate
	 * @param preToDate
	 * @param arvOnly=true điều trị arv
	 * @return
	 */
	@Query(nativeQuery = true)
	public List<TBProphylaxis2> findTBProphylaxis26MonthByStartDate(@Param("organizationId") Long orgId,
			@Param("preFromDate") Timestamp preFromDate, @Param("preToDate") Timestamp preToDate,
			@Param("arvOnly") boolean arvOnly);

	/**
	 * @formatter:off
	 * Chi so 1.1 Số bệnh nhân điều trị ARV bắt đầu dự phòng Lao trong kỳ báo cáo 6 tháng trước. 
	 * BN bắt đầu điều trị ARV trước kỳ báo cáo 6 tháng trước.
	 
	 * @formatter:on
	 *
	 * @param orgId
	 * @param preFromDate
	 * @param preToDate
	 * @param arvOnly=true điều trị arv
	 * @return
	 */
	@Query(nativeQuery = true)
	public List<TBProphylaxis2> findTBProphylaxis2PrePeriod6MonthByStartDate(@Param("organizationId") Long orgId,
			@Param("preFromDate") Timestamp preFromDate, @Param("preToDate") Timestamp preToDate,
			@Param("arvOnly") boolean arvOnly);

	/**
	 * @formatter:off
	 * Chi so 1.2 Số bệnh nhân điều trị ARV bắt đầu dự phòng Lao trong kỳ báo cáo 6 tháng trước nay đã hoàn thành dự phòng lao (ít nhất 180 liều -phác đồ INH, đủ 12 liều -phác đồ 3HP) trong kỳ báo cáo. 
	 * BN bắt đầu điều trị ARV trong kỳ báo cáo 6 tháng trước. (fromDate-toDate) trong kỳ báo cáo, (preFromDate - preToDate) kỳ báo cáo 6 tháng trước
	 
	 * @formatter:on
	 *
	 * @param orgId
	 * @param fromDate
	 * @param toDate
	 * @param preFromDate
	 * @param preToDate
	 * @param arvOnly=true điều trị arv
	 * @return
	 */
	@Query(nativeQuery = true)
	public List<TBProphylaxis2> findTBProphylaxis26MonthByStartDateComplete(@Param("organizationId") Long orgId,
			@Param("fromDate") Timestamp fromDate, @Param("toDate") Timestamp toDate,
			@Param("preFromDate") Timestamp preFromDate, @Param("preToDate") Timestamp preToDate,
			@Param("arvOnly") boolean arvOnly);

	/**
	 * @formatter:off
	 * Chi so 1.2 Số bệnh nhân điều trị ARV bắt đầu dự phòng Lao trong kỳ báo cáo 6 tháng trước nay đã hoàn thành dự phòng lao (ít nhất 180 liều -phác đồ INH, đủ 12 liều -phác đồ 3HP) trong kỳ báo cáo.
	 * BN bắt đầu điều trị ARV trước kỳ báo cáo 6 tháng trước.(fromDate-toDate) trong kỳ báo cáo, (preFromDate - preToDate) kỳ báo cáo 6 tháng trước
	 
	 * @formatter:on
	 *
	 * @param orgId
	 * @param preFromDate
	 * @param preToDate
	 * @param arvOnly=true điều trị arv
	 * @return
	 */
	@Query(nativeQuery = true)
	public List<TBProphylaxis2> findTBProphylaxis2PrePeriod6MonthByStartDateComplete(
			@Param("organizationId") Long orgId, @Param("fromDate") Timestamp fromDate,
			@Param("toDate") Timestamp toDate, @Param("preFromDate") Timestamp preFromDate,
			@Param("preToDate") Timestamp preToDate, @Param("arvOnly") boolean arvOnly);

	/****************
	 * Kết thúc query báo cáo hoạt động lao/HIV 6 tháng
	 *************/

	@Query("FROM TBProphylaxis2 tb2 WHERE tb2.theCase.id = :caseId and tb2.startDate <= :toDate order by tb2.startDate DESC  ")
	public List<TBProphylaxis2> findTBProphylaxis2ByCaseId(@Param("caseId") Long caseId,
			@Param("toDate") LocalDateTime toDate);
}