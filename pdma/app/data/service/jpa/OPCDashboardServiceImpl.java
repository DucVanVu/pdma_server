package org.pepfar.pdma.app.data.service.jpa;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.domain.QOrganization;
import org.pepfar.pdma.app.data.dto.DateRangeDto;
import org.pepfar.pdma.app.data.dto.OPCDashboardChartData;
import org.pepfar.pdma.app.data.dto.OPCDashboardFilterDto;
import org.pepfar.pdma.app.data.dto.OPCDashboardMMDChartData;
import org.pepfar.pdma.app.data.dto.OPCDashboardPatientChartData;
import org.pepfar.pdma.app.data.dto.OPCDashboardPatientChartData2;
import org.pepfar.pdma.app.data.dto.OPCDashboardPatientChartData3;
import org.pepfar.pdma.app.data.dto.OPCDashboardRiskGroupChartData;
import org.pepfar.pdma.app.data.dto.OPCDashboardRiskGroupChartData2;
import org.pepfar.pdma.app.data.dto.OPCDashboardSummaryData;
import org.pepfar.pdma.app.data.dto.OPCDashboardTBScreeningChartData;
import org.pepfar.pdma.app.data.dto.OPCDashboardTBScreeningChartData2;
import org.pepfar.pdma.app.data.dto.OPCDashboardTLDChartData;
import org.pepfar.pdma.app.data.dto.OPCDashboardTLDChartData2;
import org.pepfar.pdma.app.data.dto.OPCDashboardVLChartData;
import org.pepfar.pdma.app.data.dto.OPCDashboardVLChartData2;
import org.pepfar.pdma.app.data.repository.AppointmentRepository;
import org.pepfar.pdma.app.data.repository.CaseRepository;
import org.pepfar.pdma.app.data.repository.LabTestRepository;
import org.pepfar.pdma.app.data.repository.OrganizationRepository;
import org.pepfar.pdma.app.data.service.OPCDashboardService;
import org.pepfar.pdma.app.data.types.Permission;
import org.pepfar.pdma.app.utils.AuthorizationUtils;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

@Service
public class OPCDashboardServiceImpl implements OPCDashboardService {

	@Autowired
	private OrganizationRepository orgRepos;

	@Autowired
	private LabTestRepository labRepos;

	@Autowired
	private CaseRepository caseRepos;

	@Autowired
	private AppointmentRepository appRepos;

	@Autowired
	private AuthorizationUtils authUtils;

	@Override
	@Transactional(readOnly = true)
	public OPCDashboardSummaryData getSummaryData(OPCDashboardFilterDto filter) {

		if (filter == null) {
			return new OPCDashboardSummaryData();
		}

		OPCDashboardSummaryData summary = new OPCDashboardSummaryData();
		List<Long> actualOrgIds = getGrantedOrganizationIds(filter);

		if (actualOrgIds == null || actualOrgIds.size() <= 0) {
			return summary;
		}

		AtomicInteger todayAppCount = new AtomicInteger(0);
		AtomicInteger lateCount = new AtomicInteger(0);
		AtomicInteger pendingEnrollmentCount = new AtomicInteger(0);
		AtomicInteger riskAssmtNeededCount = new AtomicInteger(0);
		AtomicInteger vlMissingResultCount = new AtomicInteger(0);
		AtomicInteger cd4MissingResultCount = new AtomicInteger(0);

		// today
		Timestamp todayStart = CommonUtils.toTimestamp(CommonUtils.hanoiTodayStart());
		Timestamp todayEnd = CommonUtils.toTimestamp(CommonUtils.hanoiTodayEnd());

		actualOrgIds.parallelStream().forEach(orgId -> {
			List<Long> orgIdList = Lists.newArrayList(orgId);

			todayAppCount.getAndAdd(appRepos.countAppointments4TxDashboard(orgIdList, todayStart, todayEnd).intValue());
			lateCount.getAndAdd(appRepos.countLateAppointments4TxDashboard(orgIdList, 84).intValue());
			pendingEnrollmentCount.getAndAdd(caseRepos.countPendingEnrollmentPatients(orgIdList).intValue());
			riskAssmtNeededCount.getAndAdd(caseRepos.countRiskAssessmentNeeded(orgIdList).intValue());
			vlMissingResultCount.getAndAdd(labRepos.countTestsMissingResult(orgIdList, "VIRAL_LOAD").intValue());
			cd4MissingResultCount.getAndAdd(labRepos.countTestsMissingResult(orgIdList, "CD4").intValue());
		});

		summary.setTodayAppointments(todayAppCount.get());
		summary.setGt84LateDays(lateCount.get());
		summary.setPendingEnrollments(pendingEnrollmentCount.get());
		summary.setRiskAssessmentNeeded(riskAssmtNeededCount.get());
		summary.setVlMissingResults(vlMissingResultCount.get());
		summary.setCd4MissingResults(cd4MissingResultCount.get());

		return summary;
	}

	@Override
	@Transactional(readOnly = true)
	public OPCDashboardChartData getChartData(OPCDashboardFilterDto filter) {

		if (filter == null || !CommonUtils.isPositive(filter.getTargetChart(), true)) {
			return new OPCDashboardChartData();
		}

		OPCDashboardChartData chartData = new OPCDashboardChartData();
		List<Long> actualOrgIds = getGrantedOrganizationIds(filter);

		if (actualOrgIds == null || actualOrgIds.size() <= 0) {
			return chartData;
		}

		switch (filter.getTargetChart()) {
		case 1:
			// Biểu đồ: Tình hình quản lý bệnh nhân
			chartData = getChartData_Target_1(actualOrgIds);
			break;

		case 2:
			// Biểu đồ: Hoạt động xét nghiệm TLVR
			chartData = getChartData_Target_2(actualOrgIds);
			break;

		case 3:
			// Biểu đồ: Tình hình cấp ARV nhiều tháng
			chartData = getChartData_Target_3(actualOrgIds);
			break;

		case 4:
			// Biểu đồ: Tình hình cấp ARV phác đồ TLD
			chartData = getChartData_Target_4(actualOrgIds);
			break;

		case 5:
			// Biểu đồ: Đánh giá nhóm nguy cơ
			chartData = getChartData_Target_5(actualOrgIds);
			break;

		case 6:
			// Biểu đồ: Hoạt động sàng lọc lao
			chartData = getChartData_Target_6(actualOrgIds);
			break;

		default:
			break;
		}

		return chartData;
	}

	/**
	 * Tình hình quản lý bệnh nhân
	 * 
	 * @param filter
	 * @return
	 */
	private OPCDashboardChartData getChartData_Target_1(List<Long> orgIds) {
		// list of 3 months
		int MONTH_COUNT = 3;
		List<DateRangeDto> ranges = CommonUtils.getBackwardMonths(LocalDateTime.now(), MONTH_COUNT);
		List<OPCDashboardPatientChartData3> data = new ArrayList<>();

		for (int i = MONTH_COUNT - 1; i >= 0; i--) {
			DateRangeDto r = ranges.get(i);

			Timestamp fromDate = CommonUtils.toTimestamp(r.getFromDate());
			Timestamp toDate = CommonUtils.toTimestamp(r.getToDate());

			OPCDashboardPatientChartData3 d = new OPCDashboardPatientChartData3();

			AtomicInteger activePatientCount = new AtomicInteger(0);
			AtomicInteger newlyEnrolledCount = new AtomicInteger(0);
			AtomicInteger transInCount = new AtomicInteger(0);
			AtomicInteger returnedCount = new AtomicInteger(0);
			AtomicInteger transedOutCount = new AtomicInteger(0);
			AtomicInteger ltfuCount = new AtomicInteger(0);
			AtomicInteger deadCount = new AtomicInteger(0);

			LocalDateTime date = r.getFromDate();
			String month = "Th." + date.getMonthValue() + "/" + date.getYear();

			orgIds.parallelStream().forEach(orgId -> {
				List<Long> orgIdList = Lists.newArrayList(orgId);

				activePatientCount.getAndAdd(caseRepos.getPatientChartPart0(orgIdList, toDate).intValue());

				List<OPCDashboardPatientChartData> list = caseRepos.getPatientChartPart1(orgIdList, fromDate, toDate);
				List<OPCDashboardPatientChartData2> list2 = caseRepos.getPatientChartPart2(orgIdList, fromDate, toDate);

				// upper X-axis data
				for (OPCDashboardPatientChartData obj : list) {

					String status = obj.getStatus();
					if (status == null) {
						continue;
					}

					switch (status) {
					case "TRANSFERRED_OUT":
						transedOutCount.getAndAdd(-obj.getPcount());
						break;

					case "LTFU":
						ltfuCount.getAndAdd(-obj.getPcount());
						break;

					case "DEAD":
						deadCount.getAndAdd(-obj.getPcount());
						break;

					default:
						break;
					}
				}

				// lower X-axis data
				for (OPCDashboardPatientChartData2 obj : list2) {

					String etype = obj.getVariable1();

					if (etype == null) {
						continue;
					}

					switch (etype) {
					case "NEWLY_ENROLLED":
						newlyEnrolledCount.getAndAdd(obj.getVariable2());
						break;

					case "RETURNED":
						returnedCount.getAndAdd(obj.getVariable2());
						break;

					case "TRANSFERRED_IN":
						transInCount.getAndAdd(obj.getVariable2());
						break;

					default:
						break;
					}
				}
			});

			d.setActivePatientCount(activePatientCount.get());
			d.setTransedOutCount(transedOutCount.get());
			d.setLtfuCount(ltfuCount.get());
			d.setDeadCount(deadCount.get());
			d.setNewlyEnrolledCount(newlyEnrolledCount.get());
			d.setReturnedCount(returnedCount.get());
			d.setTransInCount(transInCount.get());

			d.setMonth(month);

			data.add(d);
		}

		OPCDashboardChartData chartData = new OPCDashboardChartData();
		chartData.setPatientData(data);

		return chartData;
	}

	/**
	 * Hoạt động XN TLVR
	 * 
	 * @param filter
	 * @return
	 */
	private OPCDashboardChartData getChartData_Target_2(List<Long> orgIds) {
		// list of 4 quarters
		int QUARTER_COUNT = 4;
		List<DateRangeDto> ranges = CommonUtils.getBackwardQuarters(LocalDateTime.now(), QUARTER_COUNT);
		List<OPCDashboardVLChartData2> data = new ArrayList<>();

		for (int i = QUARTER_COUNT - 1; i >= 0; i--) {
			DateRangeDto r = ranges.get(i);

			Timestamp fromDate = CommonUtils.toTimestamp(r.getFromDate());
			Timestamp toDate = CommonUtils.toTimestamp(r.getToDate());

			AtomicInteger testCount = new AtomicInteger(0);
			AtomicInteger noResultCount = new AtomicInteger(0);
			AtomicInteger undetectableCount = new AtomicInteger(0);
			AtomicInteger lt200Count = new AtomicInteger(0);
			AtomicInteger lt1000Count = new AtomicInteger(0);
			AtomicInteger ge1000Count = new AtomicInteger(0);
			LocalDateTime date = r.getFromDate();

			double startingMonth = (double) date.getMonthValue();
			String quarter = startingMonth / 3 <= 1 ? "1"
					: startingMonth / 3 <= 2 ? "2" : startingMonth / 3 <= 3 ? "3" : "4";

			quarter = "Q." + quarter + "/" + date.getYear();

			OPCDashboardVLChartData2 d = new OPCDashboardVLChartData2();
			orgIds.parallelStream().forEach(orgId -> {
				List<OPCDashboardVLChartData> list = labRepos.getVLChartData(Lists.newArrayList(orgId), fromDate,
						toDate);

				for (OPCDashboardVLChartData obj : list) {
					testCount.getAndAdd(obj.getTcount());
					String classification = obj.getTresult();

					if (classification == null) {
						noResultCount.getAndAdd(obj.getTcount());
					} else {
						switch (classification) {
						case "Không phát hiện":
							undetectableCount.getAndAdd(obj.getTcount());
							break;

						case "KPH - <200 bản sao/ml":
							lt200Count.getAndAdd(obj.getTcount());
							break;

						case "200 - <1000 bản sao/ml":
							lt1000Count.getAndAdd(obj.getTcount());
							break;

						case ">=1000 bản sao/ml":
							ge1000Count.getAndAdd(obj.getTcount());
							break;

						default:
							break;
						}
					}
				}
			});

			d.setTestCount(testCount.get());
			d.setUndetectableCount(undetectableCount.get());
			d.setLt200Count(lt200Count.get());
			d.setLt1000Count(lt1000Count.get());
			d.setGe1000Count(ge1000Count.get());
			d.setNoResultCount(noResultCount.get());

			d.setQuarter(quarter);

			data.add(d);
		}

		OPCDashboardChartData chartData = new OPCDashboardChartData();
		chartData.setVlData(data);

		return chartData;
	}

	/**
	 * Tình hình cấp ARV nhiều tháng
	 * 
	 * @param filter
	 * @return
	 */
	private OPCDashboardChartData getChartData_Target_3(List<Long> orgIds) {
		OPCDashboardMMDChartData data = new OPCDashboardMMDChartData();

		AtomicInteger mmdCount = new AtomicInteger(0);
		AtomicInteger activeARVCount = new AtomicInteger(0);

		orgIds.parallelStream().forEach(orgId -> {
			List<Long> orgIdList = Lists.newArrayList(orgId);

			mmdCount.getAndAdd(caseRepos.countOnMMD(orgIdList).intValue());
			activeARVCount.getAndAdd(caseRepos.countActivePatientOnARV(orgIdList).intValue());
		});

		data.setOnMMDCount(mmdCount.get());
		data.setNonMMDCount(activeARVCount.get() - mmdCount.get());

		OPCDashboardChartData chartData = new OPCDashboardChartData();
		chartData.setMmdData(data);

		return chartData;
	}

	/**
	 * Tình hình cấp ARV phác đồ TLD
	 * 
	 * @param filter
	 * @return
	 */
	private OPCDashboardChartData getChartData_Target_4(List<Long> orgIds) {

		OPCDashboardTLDChartData2 data = new OPCDashboardTLDChartData2();

		AtomicInteger tldCount = new AtomicInteger(0);
		AtomicInteger activeARVCount = new AtomicInteger(0);

		orgIds.parallelStream().forEach(orgId -> {
			List<Long> orgIdList = Lists.newArrayList(orgId);
			List<OPCDashboardTLDChartData> list = caseRepos.getTLDChartData(orgIdList);

			activeARVCount.getAndAdd(caseRepos.countActivePatientOnARV(orgIdList).intValue());

			for (OPCDashboardTLDChartData obj : list) {
				String s = obj.getRname();
				if (s != null && s.toLowerCase().indexOf("dtg") >= 0) {
					tldCount.getAndAdd(obj.getPcount());
				}
			}
		});

		data.setTldCount(tldCount.get());
		data.setNonTldCount(activeARVCount.get() - tldCount.get());

		OPCDashboardChartData chartData = new OPCDashboardChartData();
		chartData.setTldData(data);

		return chartData;
	}

	/**
	 * Đánh giá phân nhóm nguy cơ
	 * 
	 * @param filter
	 * @return
	 */
	private OPCDashboardChartData getChartData_Target_5(List<Long> orgIds) {
		List<String> riskNames = Lists.newArrayList("Tiêm chích ma tuý", "Quan hệ đồng giới nam", "Mại dâm",
				"Chuyển giới", "Phạm nhân", "Bạn tình người nhiễm", "Bạn tình của người TCMT", "Khách mua dâm");
		List<Long> riskIds = Lists.newArrayList(208l, 209l, 210l, 211l, 212l, 213l, 214l, 215l); // IDs of the risks in
																									// the dictionary
																									// table
		AtomicInteger[] arr = new AtomicInteger[8];
		for (int i = 0; i < 8; i++) {
			arr[i] = new AtomicInteger(0);
		}

		List<OPCDashboardRiskGroupChartData2> data = new ArrayList<>();

		orgIds.parallelStream().forEach(orgId -> {
			List<OPCDashboardRiskGroupChartData> list = caseRepos.getRiskGroupChartData(Lists.newArrayList(orgId));

			for (OPCDashboardRiskGroupChartData d : list) {
				int index = riskIds.indexOf(d.getRid());
				arr[index].getAndAdd(d.getPcount());
			}
		});

		for (int i = 0; i < 8; i++) {
			OPCDashboardRiskGroupChartData2 d = new OPCDashboardRiskGroupChartData2();
			d.setPcount(arr[i].get());
			d.setRname(riskNames.get(i));

			data.add(d);
		}

		OPCDashboardChartData chartData = new OPCDashboardChartData();
		chartData.setRiskGroupData(data);

		return chartData;
	}

	/**
	 * Hoạt động sàng lọc lao
	 * 
	 * @param filter
	 * @return
	 */
	private OPCDashboardChartData getChartData_Target_6(List<Long> orgIds) {
		// list of 5 months
		int MONTH_COUNT = 5;
		List<DateRangeDto> ranges = CommonUtils.getBackwardMonths(LocalDateTime.now(), MONTH_COUNT);
		List<OPCDashboardTBScreeningChartData2> data = new ArrayList<>();

		for (int i = MONTH_COUNT - 1; i >= 0; i--) {
			DateRangeDto r = ranges.get(i);

			Timestamp fromDate = CommonUtils.toTimestamp(r.getFromDate());
			Timestamp toDate = CommonUtils.toTimestamp(r.getToDate());

			OPCDashboardTBScreeningChartData2 d = new OPCDashboardTBScreeningChartData2();

			AtomicInteger posCount = new AtomicInteger(0);
			AtomicInteger negCount = new AtomicInteger(0);
			LocalDateTime date = r.getFromDate();
			String month = "Th." + date.getMonthValue() + "/" + date.getYear();

			orgIds.parallelStream().forEach(orgId -> {
				List<OPCDashboardTBScreeningChartData> list = caseRepos
						.getTBScreeningChartData(Lists.newArrayList(orgId), fromDate, toDate);

				for (OPCDashboardTBScreeningChartData obj : list) {
					if (obj.getTbresult() == null) {
						continue;
					}

					if (obj.getTbresult() == 1) {
						// positive
						posCount.getAndAdd(obj.getPcount());
					} else {
						// negative
						negCount.getAndAdd(obj.getPcount());
					}
				}
			});

			d.setPosCount(posCount.get());
			d.setTotalScreened(negCount.get() + posCount.get());
			d.setMonth(month);

			data.add(d);
		}

		OPCDashboardChartData chartData = new OPCDashboardChartData();
		chartData.setTbScreenData(data);

		return chartData;
	}

	/**
	 * Get a list of IDs of the organizations that the current user has READ ACCESS
	 * to
	 * 
	 * @param filter
	 * @return
	 */
	private List<Long> getGrantedOrganizationIds(OPCDashboardFilterDto filter) {

		Long provinceId = null;
		if (filter.getProvince() != null) {
			provinceId = filter.getProvince().getId();
		}

		List<Long> actualOrgIds = new ArrayList<>();
		List<Long> grantedOrgIds = authUtils.getGrantedOrgIds(Permission.READ_ACCESS, provinceId, true);

		if (grantedOrgIds == null || grantedOrgIds.size() <= 0) {
			return actualOrgIds;
		}

		if (CommonUtils.isPositive(filter.getOrganizationId(), true)) {
			actualOrgIds.add(filter.getOrganizationId());
		} else if (filter.getOrganizationId() != null && filter.getOrganizationId() == 0l) {

			// Check the province
			if (filter.getProvince() != null && CommonUtils.isPositive(filter.getProvince().getId(), true)) {
				long provId = filter.getProvince().getId();
				Iterator<Organization> orgs = orgRepos
						.findAll(QOrganization.organization.id.longValue().in(grantedOrgIds)).iterator();

				while (orgs.hasNext()) {
					Organization org = orgs.next();
					if (org.getAddress() != null && org.getAddress().getProvince() != null
							&& org.getAddress().getProvince().getId() == provId) {
						actualOrgIds.add(org.getId());
					}
				}

			} else {
				actualOrgIds = grantedOrgIds.stream().collect(Collectors.toList());
			}
		}

		return actualOrgIds;
	}
}
