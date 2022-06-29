package org.pepfar.pdma.app.data.service.jpa;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pepfar.pdma.app.Constants;
import org.pepfar.pdma.app.data.domain.Case;
import org.pepfar.pdma.app.data.domain.CaseOrg;
import org.pepfar.pdma.app.data.domain.LabTest;
import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.domain.Person;
import org.pepfar.pdma.app.data.domain.QOrganization;
import org.pepfar.pdma.app.data.dto.CaseDto;
import org.pepfar.pdma.app.data.dto.CaseReportFilterDto;
import org.pepfar.pdma.app.data.dto.LabTestDto;
import org.pepfar.pdma.app.data.dto.PersonDto;
import org.pepfar.pdma.app.data.repository.CaseOrgRepository;
import org.pepfar.pdma.app.data.repository.LabTestRepository;
import org.pepfar.pdma.app.data.repository.OrganizationRepository;
import org.pepfar.pdma.app.data.repository.RiskInterviewRepository;
import org.pepfar.pdma.app.data.service._ReportingService;
import org.pepfar.pdma.app.data.types.Gender;
import org.pepfar.pdma.app.data.types.Permission;
import org.pepfar.pdma.app.data.types.ReportType;
import org.pepfar.pdma.app.utils.AuthorizationUtils;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.ExcelUtils;
import org.pepfar.pdma.app.utils.RiskGroupUtils;
import org.pepfar.pdma.app.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

@Service("_ViralLoadReportingServiceImpl")
public class _ViralLoadReportingServiceImpl implements _ReportingService {

    @Autowired private AuthorizationUtils authUtils;

    @Autowired private OrganizationRepository orgRepos;

    @Autowired private CaseOrgRepository coRepos;

    @Autowired private LabTestRepository labRepos;

    @Autowired private RiskInterviewRepository riskRepos;

    @Autowired private ApplicationContext context;

    private Workbook blankBook;

    @Override
    @Transactional(readOnly = true)
    public Workbook exportReport(CaseReportFilterDto filter) {

        blankBook = new XSSFWorkbook();
        blankBook.createSheet();

        if (filter == null
                || filter.getFromDate() == null
                || filter.getToDate() == null
                || filter.getReportType() == null
                || (filter.getReportType() != ReportType.VL_PEPFAR_REPORT
                        && filter.getReportType() != ReportType.VL_PEPFAR_REPORT_OLD
                        && filter.getReportType() != ReportType.VL_VAAC_REPORT)) {
            return blankBook;
        }

        if (filter.getFromDate() != null && filter.getToDate() != null) {
            LocalDateTime adjFromDate =
                    filter.getFromDate().withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime adjToDate =
                    filter.getToDate().withHour(23).withMinute(59).withSecond(59).withNano(0);

            // Set adjusted date and time
            filter.setFromDate(adjFromDate);
            filter.setToDate(adjToDate);
        }

        // Set actual organizations
        List<Long> grantedOrgIds =
                authUtils.getGrantedOrgIds(Permission.READ_ACCESS, filter.getProvince(), true);
        List<Long> orgIds = new ArrayList<>();

        if (filter.getOrganization() == 0l) {
            // Get report for all granted organizations
            grantedOrgIds.forEach(
                    id -> {
                        orgIds.add(id);
                    });
        } else if (grantedOrgIds.contains(filter.getOrganization().longValue())) {
            orgIds.add(filter.getOrganization());
        } else {
            return blankBook;
        }

        filter.setActualOrganizations(orgIds);

        // Get list of actual OPCs
        QOrganization qOrg = QOrganization.organization;
        List<Organization> orgs =
                Lists.newArrayList(
                        orgRepos.findAll(
                                qOrg.opcSite.isTrue().and(qOrg.id.longValue().in(orgIds)),
                                qOrg.address.province.name.asc(),
                                qOrg.name.asc()));

        Workbook wbook = null;
        boolean multiSite = orgs.size() > 1;
        boolean multiProvince = ExcelUtils.isMultiProvince(orgs);

        switch (filter.getReportType()) {
            case VL_PEPFAR_REPORT_OLD:
                LocalDateTime adjFromDate = filter.getFromDate().minusMonths(9);

                wbook = createPepfarReportOld(filter);

                Sheet sheet1 = wbook.getSheetAt(0);

                // Period information
                String periodStr = "Q" + filter.getSelQuarter() + "/FY" + filter.getSelYear();
                ExcelUtils.writeInCell(sheet1, 1, 7, periodStr);

                periodStr = " (Từ ngày ";
                periodStr += filter.getFromDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                periodStr += " - ";
                periodStr += filter.getToDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                periodStr += ")";
                ExcelUtils.writeInCell(sheet1, 2, 7, periodStr);

                // Timestamp information
                String timestamp = "Báo cáo được lập bằng phần mềm OPC-Assist Online vào ngày ";
                timestamp +=
                        CommonUtils.hanoiNow()
                                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss"));
                ExcelUtils.writeInCell(sheet1, 3, 8, timestamp);

                // Insert administrative unit information
                if (!multiSite) {
                    Organization org = orgs.get(0);
                    String provinceName = null;

                    if (org.getAddress() != null && org.getAddress().getProvince() != null) {
                        provinceName = org.getAddress().getProvince().getName();
                    }

                    if (provinceName != null) {
                        ExcelUtils.writeInCell(sheet1, 1, 2, provinceName);
                    }
                    ExcelUtils.writeInCell(sheet1, 2, 2, org.getName());
                } else {

                    ExcelUtils.writeInCell(sheet1, 2, 2, "Danh sách ở phía dưới");

                    if (!multiProvince) {
                        Organization org = orgs.get(0);

                        if (org.getAddress() != null && org.getAddress().getProvince() != null) {
                            ExcelUtils.writeInCell(
                                    sheet1, 1, 3, orgs.get(0).getAddress().getProvince().getName());
                        }
                    } else {
                        ExcelUtils.writeInCell(sheet1, 1, 2, "Báo cáo của nhiều tỉnh");
                    }

                    // List all sites beneath
                    ExcelUtils.writeInCell(sheet1, 15, 1, "Danh sách các cơ sở báo cáo:");
                    int i = 0;
                    for (Organization org : orgs) {
                        String text = "Không có thông tin";

                        if (org.getAddress() != null && org.getAddress().getProvince() != null) {
                            text = org.getAddress().getProvince().getName();
                        }

                        text += " - ";
                        text += org.getName();

                        ExcelUtils.createAndWriteInCell(sheet1, 16 + i, 1, text);
                        i++;
                    }
                }

                break;

            case VL_PEPFAR_REPORT:
                wbook = createPepfarReport(filter);

                sheet1 = wbook.getSheetAt(0);

                // Period information
                periodStr = "Q" + filter.getSelQuarter() + "/FY" + filter.getSelYear();
                ExcelUtils.writeInCell(sheet1, 1, 9, periodStr);

                periodStr = " (Từ ngày ";
                periodStr += filter.getFromDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                periodStr += " đến ngày ";
                periodStr += filter.getToDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                periodStr += ")";
                ExcelUtils.writeInCell(sheet1, 2, 8, periodStr);

                // Timestamp information
                timestamp = "Báo cáo được lập bằng phần mềm OPC-Assist Online vào ngày ";
                timestamp +=
                        CommonUtils.hanoiNow()
                                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss"));
                timestamp += " bởi người dùng ";
                timestamp += SecurityUtils.getCurrentUser().getFullname();
                ExcelUtils.writeInCell(sheet1, 4, 10, timestamp);

                // insert administrative unit information
                if (!multiSite) {
                    Organization org = orgs.get(0);
                    String provinceName = null;

                    if (org.getAddress() != null && org.getAddress().getProvince() != null) {
                        provinceName = org.getAddress().getProvince().getName();
                    }

                    if (provinceName != null) {
                        ExcelUtils.writeInCell(sheet1, 1, 3, provinceName);
                    }
                    ExcelUtils.writeInCell(sheet1, 2, 3, org.getName());
                } else {

                    ExcelUtils.writeInCell(sheet1, 2, 3, "Danh sách ở phía dưới");

                    if (!multiProvince) {
                        Organization org = orgs.get(0);

                        if (org.getAddress() != null && org.getAddress().getProvince() != null) {
                            ExcelUtils.writeInCell(
                                    sheet1, 1, 3, orgs.get(0).getAddress().getProvince().getName());
                        }
                    } else {
                        ExcelUtils.writeInCell(sheet1, 1, 3, "Báo cáo của nhiều tỉnh");
                    }

                    // List all sites beneath
                    ExcelUtils.writeInCell(sheet1, 47, 1, "Danh sách các cơ sở báo cáo:");
                    int i = 0;
                    for (Organization org : orgs) {
                        String text = "Không có thông tin";

                        if (org.getAddress() != null && org.getAddress().getProvince() != null) {
                            text = org.getAddress().getProvince().getName();
                        }

                        text += " - ";
                        text += org.getName();

                        ExcelUtils.createAndWriteInCell(sheet1, 48 + i, 1, text);
                        i++;
                    }
                }

                break;
            case VL_VAAC_REPORT:
                wbook = createVaacReport(filter);

                sheet1 = wbook.getSheetAt(0);

                // Period information
                periodStr = filter.getFromDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                periodStr += " - ";
                periodStr += filter.getToDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                ExcelUtils.writeInCell(sheet1, 1, 8, periodStr);

                // Timestamp information
                timestamp = "Báo cáo được lập bằng phần mềm OPC-Assist Online vào ngày ";
                timestamp +=
                        CommonUtils.hanoiNow()
                                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss"));
                timestamp += " bởi người dùng ";
                timestamp += SecurityUtils.getCurrentUser().getFullname();
                ExcelUtils.writeInCell(sheet1, 4, 10, timestamp);

                // insert administrative unit information
                if (!multiSite) {
                    Organization org = orgs.get(0);
                    String provinceName = null;

                    if (org.getAddress() != null && org.getAddress().getProvince() != null) {
                        provinceName = org.getAddress().getProvince().getName();
                    }

                    if (provinceName != null) {
                        ExcelUtils.writeInCell(sheet1, 1, 3, provinceName);
                    }
                    ExcelUtils.writeInCell(sheet1, 2, 3, org.getName());
                } else {

                    ExcelUtils.writeInCell(sheet1, 2, 3, "Danh sách ở phía dưới");

                    if (!multiProvince) {
                        Organization org = orgs.get(0);

                        if (org.getAddress() != null && org.getAddress().getProvince() != null) {
                            ExcelUtils.writeInCell(
                                    sheet1, 1, 3, orgs.get(0).getAddress().getProvince().getName());
                        }
                    } else {
                        ExcelUtils.writeInCell(sheet1, 1, 3, "Báo cáo của nhiều tỉnh");
                    }

                    // List all sites beneath
                    ExcelUtils.writeInCell(sheet1, 26, 1, "Danh sách các cơ sở báo cáo:");
                    int i = 0;
                    for (Organization org : orgs) {
                        String text = "Không có thông tin";

                        if (org.getAddress() != null && org.getAddress().getProvince() != null) {
                            text = org.getAddress().getProvince().getName();
                        }

                        text += " - ";
                        text += org.getName();

                        ExcelUtils.createAndWriteInCell(sheet1, 27 + i, 1, text);
                        i++;
                    }
                }

                break;

            default:
                wbook = blankBook;
                break;
        }

        return wbook;
    }

    /**
     * Create and return a workbook that contains fulfilled viral load report form for PEPFAR
     * program
     *
     * @param filter
     * @return
     */
    public Workbook createPepfarReportOld(CaseReportFilterDto filter) {
        Workbook wbook = null;
        try (InputStream template =
                context.getResource("classpath:templates/pepfar-tlvr-report.xlsx")
                        .getInputStream()) {
            wbook = new XSSFWorkbook(template);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (wbook == null) {
            return blankBook;
        }

        Sheet sheet1 = wbook.getSheetAt(0);
        Sheet sheet2 = wbook.getSheetAt(1);

        // Variables
        AtomicInteger[][] indicators = new AtomicInteger[6][4];
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 4; j++) {
                indicators[i][j] = new AtomicInteger(0);
            }
        }

        AtomicInteger[][] indicators2 = new AtomicInteger[24][2];
        for (int i = 0; i < 24; i++) {
            for (int j = 0; j < 2; j++) {
                indicators2[i][j] = new AtomicInteger(0);
            }
        }

        // -------------------------------------------------
        // -> Tổng số BN được làm XNTL trong kỳ
        // -------------------------------------------------
        List<LabTest> list =
                labRepos.findLatestVLTestsInDateRange(
                        filter.getActualOrganizations(),
                        CommonUtils.toTimestamp(filter.getFromDate()),
                        CommonUtils.toTimestamp(filter.getToDate()),
                        0,
                        null,
                        false);

        list.parallelStream()
                .map(LabTestDto::new)
                .forEach(
                        e -> {
                            if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
                                return;
                            }

                            PersonDto person = e.getTheCase().getPerson();
                            ExcelUtils.increase(filter.getToDate(), person, indicators, 0);

                            // -------------------------------------------------
                            // -> BN có kết quả XN TLVR trong kỳ
                            // -------------------------------------------------

                            switch (e.getResultText()) {
                                case Constants.VL_RESULT_UNDECTECTED:
                                    // fall through
                                case Constants.VL_RESULT_UNDETECT_LT200:
                                    ExcelUtils.increase(filter.getToDate(), person, indicators, 2);
                                    break;
                                case Constants.VL_RESULT_200_LT1000:
                                    ExcelUtils.increase(filter.getToDate(), person, indicators, 3);
                                    break;
                                case Constants.VL_RESULT_GE1000:
                                    ExcelUtils.increase(filter.getToDate(), person, indicators, 4);
                                    break;
                                default:
                                    break;
                            }

                            // For sheet #2 - detailed breakdowns
                            increase2(e, indicators2);
                        });

        // -------------------------------------------------
        // -> Tổng số BN đang được điều trị ở 6 tháng trước
        // -------------------------------------------------
        LocalDateTime _adjLast6Months = filter.getToDate().minusMonths(6);
        _adjLast6Months = _adjLast6Months.minusMinutes(1);

        boolean leapYear = _adjLast6Months.toLocalDate().isLeapYear();
        _adjLast6Months =
                _adjLast6Months.withDayOfMonth(_adjLast6Months.getMonth().length(leapYear));

        Timestamp last6Months = CommonUtils.toTimestamp(_adjLast6Months);

        LocalDateTime final_adjLast6Months = _adjLast6Months;

        for (Long orgId : filter.getActualOrganizations()) {
            List<CaseOrg> caseOrgs = coRepos.findPartiallyActivePatients(orgId, last6Months, true);

            caseOrgs.parallelStream()
                    .forEach(
                            e -> {
                                Case theCase = e.getTheCase();
                                if (theCase.getPerson() != null) {
                                    PersonDto person = new PersonDto(theCase.getPerson(), false);
                                    ExcelUtils.increase(
                                            final_adjLast6Months, person, indicators, 1);
                                }
                            });

            // In the last 6 months, don't need to care about those who transferred out but
            // still pending
            // caseOrgs = coRepos.findTransferredOutPendingPatients(orgId, null, null,
            // true);
        }

        // -------------------------------------------------
        // -> BN được làm XN từ nguồn BHYT
        // -------------------------------------------------
        list =
                labRepos.findLatestVLTestsInDateRange(
                        filter.getActualOrganizations(),
                        CommonUtils.toTimestamp(filter.getFromDate()),
                        CommonUtils.toTimestamp(filter.getToDate()),
                        0,
                        null,
                        true);

        list.parallelStream()
                .map(LabTestDto::new)
                .forEach(
                        e -> {
                            if (e.getResultText() == null
                                    || e.getTheCase() == null
                                    || e.getTheCase().getPerson() == null) {
                                return;
                            }

                            PersonDto person = e.getTheCase().getPerson();
                            ExcelUtils.increase(filter.getToDate(), person, indicators, 5);
                        });

        // --> Fill out results
        Row row = null;
        Cell cell = null;

        // for worksheet #1
        for (int i = 0; i < 6; i++) {

            if (i > 1) {
                row = sheet1.getRow(i + 8);
            } else {
                row = sheet1.getRow(i + 6);
            }

            for (int j = 0; j < 4; j++) {
                cell = row.getCell(j + 3);
                cell.setCellValue(indicators[i][j].get());
            }
        }

        // for worksheet #2
        for (int i = 0; i < 24; i++) {
            row = sheet2.getRow(i + 3);

            for (int j = 0; j < 2; j++) {
                cell = row.getCell(j + 3);
                cell.setCellValue(indicators2[i][j].get());
            }
        }

        // Evaluate the formulas
        XSSFFormulaEvaluator.evaluateAllFormulaCells(wbook);

        return wbook;
    }

    /**
     * Create and return a workbook that contains fulfilled viral load report form for PEPFAR
     * program
     *
     * @param filter
     * @return
     */
    public Workbook createPepfarReport(CaseReportFilterDto filter) {
        Workbook wbook = null;
        try (InputStream template =
                context.getResource("classpath:templates/pepfar-tlvr-report-alt.xlsx")
                        .getInputStream()) {
            wbook = new XSSFWorkbook(template);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (wbook == null) {
            return blankBook;
        }

        Sheet sheet1 = wbook.getSheetAt(0);

        // Variables
        AtomicInteger[][] indicators = new AtomicInteger[35][5];
        for (int i = 0; i < 35; i++) {
            for (int j = 0; j < 5; j++) {
                indicators[i][j] = new AtomicInteger(0);
            }
        }

        Timestamp fromDate = CommonUtils.toTimestamp(filter.getFromDate());
        Timestamp toDate = CommonUtils.toTimestamp(filter.getToDate());

        // -------------------------------------------------
        // -> Tổng số BN được làm XNTL lần 1 trong kỳ
        // -------------------------------------------------

        List<LabTest> list =
                labRepos.findLatest1stVLTestsInDateRange(
                        filter.getActualOrganizations(), fromDate, toDate);

        list.parallelStream()
                .map(e -> new LabTestDto(e))
                .forEach(
                        e -> {
                            if (e.getSampleDate() == null
                                    || e.getTheCase() == null
                                    || e.getTheCase().getPerson() == null) {
                                return;
                            }

                            // ---------------------------------------
                            // Calculate dis-aggregation by risk group
                            // ---------------------------------------
                            calculatePepfarVlIndicatorsByRiskgroup(e, indicators);

                            // ---------------------------------------
                            // Calculate dis-aggregation by age and sex
                            // ---------------------------------------
                            calculatePepfarVlIndicatorsByAgeAndSex(e, indicators);
                        });

        // -------------------------------------------------
        // -> Tổng số BN được làm XNTL lần 2 trong kỳ
        // -------------------------------------------------
        list =
                labRepos.findLatest2ndVLTestsInDateRange(
                        filter.getActualOrganizations(), fromDate, toDate);

        list.parallelStream()
                .map(e -> new LabTestDto(e))
                .forEach(
                        e -> {
                            if (e.getSampleDate() == null
                                    || e.getTheCase() == null
                                    || e.getTheCase().getPerson() == null) {
                                return;
                            }

                            // ---------------------------------------
                            // Calculate dis-aggregation by risk group
                            // ---------------------------------------
                            calculatePepfarVlIndicatorsByRiskgroup(e, indicators);

                            // ---------------------------------------
                            // Calculate dis-aggregation by age and sex
                            // ---------------------------------------
                            calculatePepfarVlIndicatorsByAgeAndSex(e, indicators);
                        });

        // -------------------------------------------------
        // -> Tổng số BN đang điều trị ở 6 tháng trước
        // -------------------------------------------------
        LocalDateTime _adjLast6Months = CommonUtils.dateEnd(filter.getToDate().minusMonths(6));
        _adjLast6Months = _adjLast6Months.minusMinutes(1);

        boolean leapYear = _adjLast6Months.toLocalDate().isLeapYear();
        _adjLast6Months =
                _adjLast6Months.withDayOfMonth(_adjLast6Months.getMonth().length(leapYear));

        System.out.println("End of last 6 month");
        System.out.println(_adjLast6Months.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        Timestamp last6Months = CommonUtils.toTimestamp(_adjLast6Months);

        for (Long orgId : filter.getActualOrganizations()) {
            List<CaseOrg> caseOrgs = coRepos.findPartiallyActivePatients(orgId, last6Months, true);

            caseOrgs.parallelStream()
                    .map(c -> c.getTheCase())
                    .forEach(
                            c -> {
                                // calculate dis-aggregation by risk group
                                calculateActivePatientsByRiskgroup(c.getId(), indicators);

                                // calculate dis-aggregation by age and sex
                                Person pe = c.getPerson();
                                int i = calculateIndexByAgeAndSex(pe.getGender(), pe.getDob());

                                if (i >= 0) {
                                    indicators[i][4].getAndAdd(1);
                                }
                            });

            // In the last 6 months, don't need to care about those who transferred out but
            // still pending
            // caseOrgs = coRepos.findTransferredOutPendingPatients(orgId, null, null,
            // true);
        }

        // --> Fill out results
        Row row = null;
        Cell cell = null;

        // for worksheet #1
        for (int i = 0; i < 35; i++) {
            row = sheet1.getRow(i + 8);

            for (int j = 0; j < 5; j++) {
                int k = (j < 4) ? j : j + 1;
                cell = row.getCell(k + 4);

                cell.setCellValue(indicators[i][j].get());
            }
        }

        // Evaluate the formulas
        XSSFFormulaEvaluator.evaluateAllFormulaCells(wbook);

        return wbook;
    }

    /**
     * Create and return a workbook that contains fulfilled viral load report form as per VAAC
     * requirement
     *
     * @param filter
     * @return
     */
    public Workbook createVaacReport(CaseReportFilterDto filter) {
        Workbook wbook = null;
        try (InputStream template =
                context.getResource("classpath:templates/vaac-vl-report.xlsx").getInputStream()) {
            wbook = new XSSFWorkbook(template);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (wbook == null) {
            return blankBook;
        }

        Sheet sheet = wbook.getSheetAt(0);

        // Variables
        AtomicInteger[][] indicators = new AtomicInteger[11][4];
        for (int i = 0; i < 11; i++) {
            for (int j = 0; j < 4; j++) {
                indicators[i][j] = new AtomicInteger(0);
            }
        }

        // -------------------------------------------------
        // -> Test lần 1 trong kỳ
        // -------------------------------------------------
        List<LabTest> list =
                labRepos.findLatest1stVLTestsInDateRange(
                        filter.getActualOrganizations(),
                        CommonUtils.toTimestamp(filter.getFromDate()),
                        CommonUtils.toTimestamp(filter.getToDate()));

        list.parallelStream()
                .map(e -> new LabTestDto(e))
                .forEach(
                        e -> {
                            if (e.getResultText() == null
                                    || e.getTheCase() == null
                                    || e.getTheCase().getPerson() == null) {
                                return;
                            }

                            PersonDto person = e.getTheCase().getPerson();

                            switch (e.getResultText()) {
                                case Constants.VL_RESULT_UNDECTECTED:
                                    ExcelUtils.increase(filter.getToDate(), person, indicators, 0);
                                    break;
                                case Constants.VL_RESULT_UNDETECT_LT200:
                                    ExcelUtils.increase(filter.getToDate(), person, indicators, 1);
                                    break;
                                case Constants.VL_RESULT_200_LT1000:
                                    ExcelUtils.increase(filter.getToDate(), person, indicators, 2);
                                    break;
                                case Constants.VL_RESULT_GE1000:
                                    ExcelUtils.increase(filter.getToDate(), person, indicators, 3);
                                    break;
                                default:
                                    break;
                            }
                        });

        // -------------------------------------------------
        // -> Test lần 2 trong kỳ
        // -------------------------------------------------
        list =
                labRepos.findLatest2ndVLTestsInDateRange(
                        filter.getActualOrganizations(),
                        CommonUtils.toTimestamp(filter.getFromDate()),
                        CommonUtils.toTimestamp(filter.getToDate()));

        list.parallelStream()
                .map(e -> new LabTestDto(e))
                .forEach(
                        e -> {
                            if (e.getResultText() == null
                                    || e.getTheCase() == null
                                    || e.getTheCase().getPerson() == null) {
                                return;
                            }

                            PersonDto person = e.getTheCase().getPerson();

                            switch (e.getResultText()) {
                                case Constants.VL_RESULT_UNDECTECTED:
                                    ExcelUtils.increase(filter.getToDate(), person, indicators, 6);
                                    break;
                                case Constants.VL_RESULT_UNDETECT_LT200:
                                    ExcelUtils.increase(filter.getToDate(), person, indicators, 7);
                                    break;
                                case Constants.VL_RESULT_200_LT1000:
                                    ExcelUtils.increase(filter.getToDate(), person, indicators, 8);
                                    break;
                                case Constants.VL_RESULT_GE1000:
                                    ExcelUtils.increase(filter.getToDate(), person, indicators, 9);
                                    break;
                                default:
                                    break;
                            }
                        });

        // -------------------------------------------------
        // -> BN điều trị phác đồ bậc 2 đc làm XN trong kỳ
        // -------------------------------------------------
        list =
                labRepos.findLatestVLTestsFor2ndLinePatientsInDateRange(
                        filter.getActualOrganizations(),
                        CommonUtils.toTimestamp(filter.getFromDate()),
                        CommonUtils.toTimestamp(filter.getToDate()));

        list.parallelStream()
                .map(e -> new LabTestDto(e))
                .forEach(
                        e -> {
                            if (e.getResultText() == null
                                    || e.getTheCase() == null
                                    || e.getTheCase().getPerson() == null) {
                                return;
                            }

                            PersonDto person = e.getTheCase().getPerson();

                            switch (e.getResultText()) {
                                case Constants.VL_RESULT_UNDECTECTED:
                                case Constants.VL_RESULT_UNDETECT_LT200:
                                case Constants.VL_RESULT_200_LT1000:
                                    ExcelUtils.increase(filter.getToDate(), person, indicators, 4);
                                    break;
                                case Constants.VL_RESULT_GE1000:
                                    ExcelUtils.increase(filter.getToDate(), person, indicators, 5);
                                    break;
                                default:
                                    break;
                            }
                        });

        // -------------------------------------------------
        // -> Test sử dụng nguồn bảo hiểm
        // -------------------------------------------------
        list =
                labRepos.findLatestVLTestsUsingShiInDateRange(
                        filter.getActualOrganizations(),
                        CommonUtils.toTimestamp(filter.getFromDate()),
                        CommonUtils.toTimestamp(filter.getToDate()));

        list.parallelStream()
                .map(e -> new LabTestDto(e))
                .forEach(
                        e -> {
                            if (e.getResultText() == null
                                    || e.getTheCase() == null
                                    || e.getTheCase().getPerson() == null) {
                                return;
                            }

                            PersonDto person = e.getTheCase().getPerson();
                            ExcelUtils.increase(filter.getToDate(), person, indicators, 10);
                        });

        // --> Fill out results
        Row row = null;
        Cell cell = null;

        for (int i = 0; i < 11; i++) {

            if (i > 3 && i <= 5) {
                row = sheet.getRow(i + 10);
            } else if (i > 5) {
                row = sheet.getRow(i + 11);
            } else {
                row = sheet.getRow(i + 9);
            }

            for (int j = 0; j < 4; j++) {
                cell = row.getCell(j + 6);
                cell.setCellValue(indicators[i][j].get());
            }
        }

        // Evaluate the formulas
        XSSFFormulaEvaluator.evaluateAllFormulaCells(wbook);

        return wbook;
    }

    private void increase(LabTestDto labTest, AtomicInteger[][] arr, int row) {
        if (labTest == null || arr == null || row < 0) {
            return;
        }

        String result = labTest.getResultText();
        if (CommonUtils.isEmpty(result, true) || labTest.getResultDate() == null) {
            // denominator
            switch (labTest.getReasonForTesting()) {
                case VL_FOLLOWUP_3MONTH:
                    arr[row][3].getAndAdd(1);
                    break;

                default:
                    arr[row][2].getAndAdd(1);
                    break;
            }

            // no numerator in this case

        } else {
            switch (labTest.getReasonForTesting()) {
                case VL_FOLLOWUP_3MONTH:
                    // denominator
                    arr[row][3].getAndAdd(1);

                    // numerator
                    if (!result.equalsIgnoreCase(Constants.VL_RESULT_GE1000)) {
                        arr[row][1].getAndAdd(1);
                    }

                    break;

                default:
                    // denominator
                    arr[row][2].getAndAdd(1);

                    // numerator
                    if (!result.equalsIgnoreCase(Constants.VL_RESULT_GE1000)) {
                        arr[row][0].getAndAdd(1);
                    }
                    break;
            }
        }
    }

    private void increase2(LabTestDto labTest, AtomicInteger[][] arr) {

        PersonDto person = labTest.getTheCase().getPerson();
        LocalDateTime dob = person.getDob();
        Gender gender = person.getGender();

        if (dob == null || gender == null) {
            return;
        }

        long age = CommonUtils.dateDiff(ChronoUnit.YEARS, dob, CommonUtils.hanoiNow());
        int i = -1;

        if (age < 1) {
            switch (gender) {
                case MALE:
                    i = 0;
                    break;
                case FEMALE:
                    i = 1;
                    break;
                default:
                    i = 1; // to avoid missing patients
                    break;
            }
        } else {
            for (int k = 0; k < 11; k++) {
                int lbound = 1;
                int ubound = 4;

                if (k > 0) {
                    lbound = k * 5;

                    if (k < 10) {
                        ubound = lbound + 4;
                    } else {
                        ubound = Integer.MAX_VALUE;
                    }
                }

                if (age >= lbound && age <= ubound) {
                    switch (gender) {
                        case MALE:
                            i = k * 2 + 2;
                            break;
                        case FEMALE:
                            i = k * 2 + 3;
                            break;
                        default:
                            i = k * 2 + 3; // to avoid missing patients
                            break;
                    }
                    break;
                }
            }
        }

        if (i >= 0) {

            String result = labTest.getResultText();

            switch (result) {
                case Constants.VL_RESULT_UNDECTECTED:
                case Constants.VL_RESULT_UNDETECT_LT200:
                case Constants.VL_RESULT_200_LT1000:
                    arr[i][0].incrementAndGet();
                    break;
            }

            arr[i][1].incrementAndGet();
        }
    }

    /**
     * Calculate for risk group
     *
     * @param labTest
     * @param arr
     */
    private void calculatePepfarVlIndicatorsByRiskgroup(LabTestDto labTest, AtomicInteger[][] arr) {
        CaseDto theCase = labTest.getTheCase();

        List<String> codes = riskRepos.findRiskCodesInMostRecentInterview(theCase.getId());
        String riskCode = RiskGroupUtils.getPrioritizedRiskCode(codes);

        if (riskCode == null) {
            // other risk
            increase(labTest, arr, 8);
        } else {
            // one of 8 groups
            switch (riskCode) {
                case RiskGroupUtils.RISK_PWID:
                    increase(labTest, arr, 0);
                    break;

                case RiskGroupUtils.RISK_MSM:
                    increase(labTest, arr, 1);
                    break;

                case RiskGroupUtils.RISK_SW:
                    increase(labTest, arr, 2);
                    break;

                case RiskGroupUtils.RISK_TG:
                    increase(labTest, arr, 3);
                    break;

                case RiskGroupUtils.RISK_PARTNER:
                    increase(labTest, arr, 4);
                    break;

                case RiskGroupUtils.RISK_CLIENT:
                    increase(labTest, arr, 5);
                    break;

                case RiskGroupUtils.RISK_PWID_PARTNER:
                    increase(labTest, arr, 6);
                    break;

                case RiskGroupUtils.RISK_PRISONER:
                    increase(labTest, arr, 7);
                    break;

                default:
                    increase(labTest, arr, 8);
                    break;
            }
        }
    }

    /**
     * Calculate the active patients disaggregated by risk group
     *
     * @param theCase
     * @param arr
     */
    private void calculateActivePatientsByRiskgroup(Long caseId, AtomicInteger[][] arr) {
        List<String> codes = riskRepos.findRiskCodesInMostRecentInterview(caseId);
        String riskCode = RiskGroupUtils.getPrioritizedRiskCode(codes);

        if (riskCode == null) {
            // other risk
            arr[8][4].getAndAdd(1);
        } else {
            // one of 8 groups
            switch (riskCode) {
                case RiskGroupUtils.RISK_PWID:
                    arr[0][4].getAndAdd(1);
                    break;

                case RiskGroupUtils.RISK_MSM:
                    arr[1][4].getAndAdd(1);
                    break;

                case RiskGroupUtils.RISK_SW:
                    arr[2][4].getAndAdd(1);
                    break;

                case RiskGroupUtils.RISK_TG:
                    arr[3][4].getAndAdd(1);
                    break;

                case RiskGroupUtils.RISK_PARTNER:
                    arr[4][4].getAndAdd(1);
                    break;

                case RiskGroupUtils.RISK_CLIENT:
                    arr[5][4].getAndAdd(1);
                    break;

                case RiskGroupUtils.RISK_PWID_PARTNER:
                    arr[6][4].getAndAdd(1);
                    break;

                case RiskGroupUtils.RISK_PRISONER:
                    arr[7][4].getAndAdd(1);
                    break;

                default:
                    arr[8][4].getAndAdd(1);
                    break;
            }
        }
    }

    /**
     * Calculate for age and sex dis-aggregation
     *
     * @param labTest
     * @param arr
     */
    private void calculatePepfarVlIndicatorsByAgeAndSex(LabTestDto labTest, AtomicInteger[][] arr) {

        PersonDto person = labTest.getTheCase().getPerson();
        int i = calculateIndexByAgeAndSex(person.getGender(), person.getDob());

        if (i >= 0) {
            increase(labTest, arr, i);
        }
    }

    /**
     * Calculate the array index for increasement
     *
     * @param person
     * @return
     */
    private int calculateIndexByAgeAndSex(Gender gender, LocalDateTime dob) {
        int i = -1;

        if (gender == null) {
            return i;
        }

        if (dob == null) {
            switch (gender) {
                case MALE:
                    i = 22;
                    break;
                case FEMALE:
                    i = 9;
                    break;
                default:
                    i = 9; // to avoid missing patients
                    break;
            }
        } else {
            long age = CommonUtils.dateDiff(ChronoUnit.YEARS, dob, CommonUtils.hanoiNow());

            if (age < 1) {
                switch (gender) {
                    case MALE:
                        i = 23;
                        break;
                    case FEMALE:
                        i = 10;
                        break;
                    default:
                        i = 10; // to avoid missing patients
                        break;
                }
            } else {
                for (int k = 0; k < 11; k++) {
                    int lbound = 1;
                    int ubound = 4;

                    if (k > 0) {
                        lbound = k * 5;

                        if (k < 10) {
                            ubound = lbound + 4;
                        } else {
                            ubound = Integer.MAX_VALUE;
                        }
                    }

                    if (age >= lbound && age <= ubound) {
                        switch (gender) {
                            case MALE:
                                i = k + 24;
                                break;
                            case FEMALE:
                                i = k + 11;
                                break;
                            default:
                                i = k + 11; // to avoid missing patients
                                break;
                        }
                    }
                }
            }
        }

        return i;
    }
}
