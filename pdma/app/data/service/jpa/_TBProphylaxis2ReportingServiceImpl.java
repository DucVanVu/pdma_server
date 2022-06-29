package org.pepfar.pdma.app.data.service.jpa;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pepfar.pdma.app.data.domain.Case;
import org.pepfar.pdma.app.data.domain.CaseOrg;
import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.domain.QOrganization;
import org.pepfar.pdma.app.data.domain.TBProphylaxis2;
import org.pepfar.pdma.app.data.domain.TBProphylaxis2Dispense;
import org.pepfar.pdma.app.data.domain.TBTreatment2;
import org.pepfar.pdma.app.data.dto.CaseReportFilterDto;
import org.pepfar.pdma.app.data.dto.PersonDto;
import org.pepfar.pdma.app.data.repository.CaseOrgRepository;
import org.pepfar.pdma.app.data.repository.CaseRepository;
import org.pepfar.pdma.app.data.repository.OrganizationRepository;
import org.pepfar.pdma.app.data.repository.TBProphylaxis2Repository;
import org.pepfar.pdma.app.data.repository.TBTreatment2Repository;
import org.pepfar.pdma.app.data.service._ReportingService;
import org.pepfar.pdma.app.data.types.Gender;
import org.pepfar.pdma.app.data.types.Permission;
import org.pepfar.pdma.app.utils.AuthorizationUtils;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.ExcelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

@Service("_TBProphylaxis2ReportingServiceImpl")
public class _TBProphylaxis2ReportingServiceImpl implements _ReportingService {

    @Autowired private AuthorizationUtils authUtils;

    @Autowired private OrganizationRepository orgRepos;

    @Autowired private CaseRepository caseRepos;

    @Autowired private CaseOrgRepository coRepos;

    @Autowired private TBProphylaxis2Repository tbProphylaixs2Repository;

    @Autowired private TBTreatment2Repository tbTreatment2Repository;

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
                || filter.getReportType() == null) {
            return blankBook;
        }

        LocalDateTime adjFromDate =
                filter.getFromDate().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime adjToDate =
                filter.getToDate().withHour(23).withMinute(59).withSecond(59).withNano(0);

        // Set adjusted date and time
        filter.setFromDate(adjFromDate);
        filter.setToDate(adjToDate);

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
                                qOrg.opcSite.isTrue().and(qOrg.id.longValue().in(orgIds))));

        Workbook wbook = null;
        boolean multiSite = orgs.size() > 1;
        boolean multiProvince = ExcelUtils.isMultiProvince(orgs);

        switch (filter.getReportType()) {
            case QUARTERLY_TB_TREATMENT:
                if (filter != null && filter.getSelQuarter() == 4) {
                    wbook = createPreciousTBTreatmentReport4(filter);
                } else wbook = createPreciousTBTreatmentReport(filter);

                Sheet sheet1 = wbook.getSheetAt(0);
                wbook.setSheetName(0, "BC quy" + filter.getSelQuarter());
                //				Sheet sheet2 = wbook.getSheetAt(1);

                // insert period data

                String title = "BÁO CÁO QUÝ HOẠT ĐỘNG LAO/HIV \n";
                title += " Từ ngày ";
                title += filter.getFromDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                title += " đến ngày ";
                title += filter.getToDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                ExcelUtils.writeInCell(sheet1, 3, 0, title);

                // insert administrative data
                if (!multiProvince
                        && orgs.get(0).getAddress() != null
                        && orgs.get(0).getAddress().getProvince() != null) {
                    ExcelUtils.writeInCell(
                            sheet1,
                            0,
                            0,
                            "BQLTDA tỉnh: " + orgs.get(0).getAddress().getProvince().getName());
                } else {
                    ExcelUtils.writeInCell(
                            sheet1, 0, 0, "Báo cáo của nhiều tỉnh (danh sách tỉnh ở phía dưới)");
                    // List all provice
                    if (multiProvince) {
                        if (filter != null && filter.getSelQuarter() == 4) {
                            ExcelUtils.createAndWriteInCell(
                                    sheet1, 46, 0, "Danh sách các tỉnh báo cáo:");
                        } else
                            ExcelUtils.createAndWriteInCell(
                                    sheet1, 25, 0, "Danh sách các tỉnh báo cáo:");
                        int i = 0;

                        String text = "";
                        Hashtable<Long, String> hasProvice = new Hashtable<Long, String>();
                        for (Organization org : orgs) {
                            if (org.getAddress() != null
                                    && org.getAddress().getProvince() != null
                                    && org.getAddress().getProvince().getId() != null) {
                                String name =
                                        hasProvice.get(org.getAddress().getProvince().getId());
                                if (name == null) {
                                    if (text != null && text.length() > 0) {
                                        text += ", " + org.getAddress().getProvince().getName();
                                    } else {
                                        text += org.getAddress().getProvince().getName();
                                    }
                                    hasProvice.put(
                                            org.getAddress().getProvince().getId(),
                                            org.getAddress().getProvince().getName());
                                }
                            }
                        }
                        if (filter != null && filter.getSelQuarter() == 4) {
                            ExcelUtils.createAndWriteInCell(sheet1, 47 + i, 1, text);
                        } else ExcelUtils.createAndWriteInCell(sheet1, 26 + i, 1, text);
                    }
                }
                if (!multiSite) {
                    ExcelUtils.writeInCell(sheet1, 1, 0, "Cơ sở: " + orgs.get(0).getName());
                } else {
                    if (!multiProvince
                            && orgs.get(0).getAddress() != null
                            && orgs.get(0).getAddress().getProvince() != null) {
                        ExcelUtils.writeInCell(
                                sheet1,
                                1,
                                0,
                                "Tỉnh/thành phố: "
                                        + orgs.get(0).getAddress().getProvince().getName()
                                        + " (xem danh sách cơ sở phía dưới)");
                    } else {
                        ExcelUtils.writeInCell(
                                sheet1,
                                1,
                                0,
                                "Báo cáo của nhiều cơ sở (danh sách cơ sở phía dưới)");
                    }

                    // List all sites beneath
                    if (filter != null && filter.getSelQuarter() == 4) {
                        ExcelUtils.createAndWriteInCell(
                                sheet1, 48, 0, "Danh sách các cơ sở báo cáo:");
                    } else
                        ExcelUtils.createAndWriteInCell(
                                sheet1, 27, 0, "Danh sách các cơ sở báo cáo:");
                    int i = 0;
                    String text = "";
                    for (Organization org : orgs) {
                        if (org != null && org.getName() != null) {
                            //							if(text!=null && text.length()>0) {
                            //								text += ", " ;
                            //							}
                            text = org.getName();
                            if (filter != null && filter.getSelQuarter() == 4) {
                                ExcelUtils.createAndWriteInCell(sheet1, 49 + i, 1, text);
                            } else ExcelUtils.createAndWriteInCell(sheet1, 28 + i, 1, text);
                            i++;
                        }
                    }
                }

                break;
            case MONTHLY_TB_TREATMENT:
                wbook = createMonthTBTreatmentReport(filter);

                sheet1 = wbook.getSheetAt(0);
                wbook.setSheetName(0, "BC 6 tháng lần " + filter.getSelQuarter());
                //				Sheet sheet2 = wbook.getSheetAt(1);

                // insert period data

                title = "BÁO CÁO 6 THÁNG HOẠT ĐỘNG LAO/HIV  \n";
                title += " Từ ngày ";
                title += filter.getFromDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                title += " đến ngày ";
                title += filter.getToDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                title += " (Năm tài chính của PEPFAR)";
                ExcelUtils.writeInCell(sheet1, 3, 0, title);

                // insert administrative data
                if (!multiProvince
                        && orgs.get(0).getAddress() != null
                        && orgs.get(0).getAddress().getProvince() != null) {
                    ExcelUtils.writeInCell(
                            sheet1,
                            0,
                            0,
                            "BQLTDA tỉnh: " + orgs.get(0).getAddress().getProvince().getName());
                } else {
                    ExcelUtils.writeInCell(
                            sheet1, 0, 0, "Báo cáo của nhiều tỉnh (danh sách tỉnh ở phía dưới)");
                    // List all provice
                    if (multiProvince) {

                        ExcelUtils.createAndWriteInCell(
                                sheet1, 33, 0, "Danh sách các tỉnh báo cáo:");
                        int i = 0;

                        String text = "";
                        Hashtable<Long, String> hasProvice = new Hashtable<Long, String>();
                        for (Organization org : orgs) {
                            if (org.getAddress() != null
                                    && org.getAddress().getProvince() != null
                                    && org.getAddress().getProvince().getId() != null) {
                                String name =
                                        hasProvice.get(org.getAddress().getProvince().getId());
                                if (name == null) {
                                    if (text != null && text.length() > 0) {
                                        text += ", " + org.getAddress().getProvince().getName();
                                    } else {
                                        text += org.getAddress().getProvince().getName();
                                    }
                                    hasProvice.put(
                                            org.getAddress().getProvince().getId(),
                                            org.getAddress().getProvince().getName());
                                }
                            }
                        }

                        ExcelUtils.createAndWriteInCell(sheet1, 34 + i, 1, text);
                    }
                }
                if (!multiSite) {
                    ExcelUtils.writeInCell(sheet1, 1, 0, "Cơ sở: " + orgs.get(0).getName());
                } else {
                    if (!multiProvince
                            && orgs.get(0).getAddress() != null
                            && orgs.get(0).getAddress().getProvince() != null) {
                        ExcelUtils.writeInCell(
                                sheet1,
                                1,
                                0,
                                "Tỉnh/thành phố: "
                                        + orgs.get(0).getAddress().getProvince().getName()
                                        + " (xem danh sách cơ sở phía dưới)");
                    } else {
                        ExcelUtils.writeInCell(
                                sheet1,
                                1,
                                0,
                                "Báo cáo của nhiều cơ sở (danh sách cơ sở phía dưới)");
                    }

                    // List all sites beneath

                    ExcelUtils.createAndWriteInCell(sheet1, 35, 0, "Danh sách các cơ sở báo cáo:");
                    int i = 0;
                    String text = "";
                    for (Organization org : orgs) {
                        if (org != null && org.getName() != null) {
                            //							if(text!=null && text.length()>0) {
                            //								text += ", " ;
                            //							}
                            text = org.getName();

                            ExcelUtils.createAndWriteInCell(sheet1, 36 + i, 1, text);
                            i++;
                        }
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
     * Create and return a workbook that contains tb treatment report form for TB Treatment program
     *
     * @param filter
     * @return
     */
    public Workbook createPreciousTBTreatmentReport(CaseReportFilterDto filter) {
        Workbook wbook = null;
        try (InputStream template =
                context.getResource("classpath:templates/bcao_laohiv_quy.xlsx").getInputStream()) {
            wbook = new XSSFWorkbook(template);
            //			wbook = new SXSSFWorkbook(new XSSFWorkbook(template), 100);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (wbook == null) {
            return blankBook;
        }

        Sheet sheet1 = wbook.getSheetAt(0);
        //		Sheet sheet2 = wbook.getSheetAt(1);

        // Variables
        AtomicInteger[][] indicators = new AtomicInteger[13][5];
        for (int i = 0; i < 13; i++) {
            for (int j = 0; j < 5; j++) {
                indicators[i][j] = new AtomicInteger(0);
            }
        }

        // -------------------------------------------------
        // -> 1.1 Số bệnh nhân HIV mới đăng ký CSĐT trong năm (bao gồm BN chưa điều trị
        // ARV và đã điều trị ARV)
        // -------------------------------------------------

        for (Long orgId : filter.getActualOrganizations()) {
            List<CaseOrg> caseOrgs =
                    coRepos.findPatientsByEnrollmentTypes(
                            orgId,
                            CommonUtils.toTimestamp(filter.getFromYearDate()),
                            CommonUtils.toTimestamp(filter.getToYearDate()),
                            Lists.newArrayList("NEWLY_ENROLLED", "RETURNED", "TRANSFERRED_IN"),
                            false);

            caseOrgs.parallelStream()
                    .forEach(
                            e -> {
                                Case theCase = e.getTheCase();
                                if (theCase.getPerson() != null) {
                                    PersonDto person = new PersonDto(theCase.getPerson(), false);
                                    increasePeriod(person, indicators, 1);
                                }
                            });
        }

        // -------------------------------------------------
        // -> 2.1 Số bệnh nhân HIV mới đăng ký CSĐT HIV trong năm được bắt đầu dự phòng
        // lao trong kỳ báo cáo
        // -------------------------------------------------

        for (Long orgId : filter.getActualOrganizations()) {
            List<TBProphylaxis2> cases =
                    tbProphylaixs2Repository.findTBProphylaxis2StartDate(
                            orgId,
                            CommonUtils.toTimestamp(filter.getFromDate()),
                            CommonUtils.toTimestamp(filter.getToDate()),
                            CommonUtils.toTimestamp(filter.getFromYearDate()),
                            CommonUtils.toTimestamp(filter.getToYearDate()),
                            Lists.newArrayList("NEWLY_ENROLLED", "RETURNED", "TRANSFERRED_IN"));

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getTheCase().getPerson(), false);
                                increasePeriod(person, indicators, 2);
                            });
        }

        // -------------------------------------------------
        // -> 2.2 Số bệnh nhân HIV mới đăng ký CSĐT HIV trong năm được tiếp tục dự phòng
        // lao tại cơ sở trong kỳ báo cáo
        // (nhóm BN chuyển tới đã bắt đầu dự phòng lao trước đó)
        // -------------------------------------------------
        for (Long orgId : filter.getActualOrganizations()) {
            List<TBProphylaxis2Dispense> cases =
                    tbProphylaixs2Repository.findTBProphylaxis2RecordDate(
                            orgId,
                            CommonUtils.toTimestamp(filter.getFromDate()),
                            CommonUtils.toTimestamp(filter.getToDate()),
                            CommonUtils.toTimestamp(filter.getFromYearDate()),
                            CommonUtils.toTimestamp(filter.getToYearDate()),
                            Lists.newArrayList("TRANSFERRED_IN")); // nhóm bệnh nhân chuyển tới

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getRound() == null
                                        || e.getRound().getTheCase() == null
                                        || e.getRound().getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person =
                                        new PersonDto(e.getRound().getTheCase().getPerson(), false);
                                increasePeriod(person, indicators, 3);
                            });
        }

        // -------------------------------------------------
        // -> 3.1 Số bệnh nhân HIV đăng ký CSĐT từ những năm trước được bắt đầu dự phòng
        // lao trong kỳ báo cáo
        // -------------------------------------------------

        for (Long orgId : filter.getActualOrganizations()) {
            List<TBProphylaxis2> cases =
                    tbProphylaixs2Repository.findTBProphylaxis2StartDateByEnrollmentTypesPreYear(
                            orgId,
                            CommonUtils.toTimestamp(filter.getFromDate()),
                            CommonUtils.toTimestamp(filter.getToDate()),
                            CommonUtils.toTimestamp(filter.getFromYearDate()),
                            Lists.newArrayList("NEWLY_ENROLLED", "RETURNED", "TRANSFERRED_IN"));

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getTheCase().getPerson(), false);
                                increasePeriod(person, indicators, 4);
                            });
        }

        // -------------------------------------------------
        // ->3.2 Số bệnh nhân HIV đăng ký CSĐT từ những năm trước được tiếp tục dự phòng
        // lao trong kỳ báo cáo
        // -------------------------------------------------
        for (Long orgId : filter.getActualOrganizations()) {
            List<TBProphylaxis2Dispense> cases =
                    tbProphylaixs2Repository.findTBProphylaxis2RecordDateByEnrollmentTypesPreYear(
                            orgId,
                            CommonUtils.toTimestamp(filter.getFromDate()),
                            CommonUtils.toTimestamp(filter.getToDate()),
                            CommonUtils.toTimestamp(filter.getFromYearDate()),
                            Lists.newArrayList("NEWLY_ENROLLED", "RETURNED", "TRANSFERRED_IN"));

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getRound() == null
                                        || e.getRound().getTheCase() == null
                                        || e.getRound().getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person =
                                        new PersonDto(e.getRound().getTheCase().getPerson(), false);
                                increasePeriod(person, indicators, 5);
                            });
        }

        // -------------------------------------------------
        // ->5.1 Số bệnh nhân HIV hiện quản lý cuối kỳ báo cáo
        // -------------------------------------------------

        for (Long orgId : filter.getActualOrganizations()) {
            List<Case> cases = new ArrayList<>();
//                    caseRepos.findPatientsByEnrollmentTypesEndOfReportPeriod(
//                            orgId,
//                            CommonUtils.toTimestamp(filter.getToDate()),
//                            Lists.newArrayList("NEWLY_ENROLLED", "RETURNED", "TRANSFERRED_IN"),
//                            false);

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getPerson(), false);
                                increasePeriod(person, indicators, 9);
                            });
        }

        // -------------------------------------------------
        // ->5.2: Số bệnh nhân HIV hiện quản lý đã và đang được dự phòng lao từ khi đăng
        // ký CSĐT HIV đến cuối kỳ báo cáo
        // -------------------------------------------------

        for (Long orgId : filter.getActualOrganizations()) {
            List<TBProphylaxis2> cases =
                    tbProphylaixs2Repository.findTBProphylaxis2StartDateEndOfReportPeriod(
                            orgId,
                            CommonUtils.toTimestamp(filter.getToDate()),
                            Lists.newArrayList("NEWLY_ENROLLED", "RETURNED", "TRANSFERRED_IN"));

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getTheCase().getPerson(), false);
                                increasePeriod(person, indicators, 10);
                            });
        }

        // -------------------------------------------------
        // ->5.3: Số bệnh nhân HIV hiện quản lý đã hoàn thành dự phòng lao (ít nhất 180
        // liều -phác đồ INH, đủ 12 liều -phác đồ 3HP) tính đến cuối kỳ báo cáo
        // -------------------------------------------------

        for (Long orgId : filter.getActualOrganizations()) {
            List<TBProphylaxis2> cases =
                    tbProphylaixs2Repository.findTBProphylaxis2CompleteEndOfReportPeriod(
                            orgId,
                            CommonUtils.toTimestamp(filter.getToDate()),
                            Lists.newArrayList("NEWLY_ENROLLED", "RETURNED", "TRANSFERRED_IN"));

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getTheCase().getPerson(), false);
                                increasePeriod(person, indicators, 11);
                            });
        }

        // --> Fill out results
        Row row = null;
        Cell cell = null;

        // for worksheet #1
        for (int i = 1; i < 13; i++) {
            //			if (i > 5) {
            //				row = sheet1.getRow(i + 11);
            //			}
            //			if (i > 3) {
            //				row = sheet1.getRow(i + 9);
            //			}
            if (i > 1) {
                row = sheet1.getRow(i + 8);
            } else {
                row = sheet1.getRow(i + 7);
            }

            for (int j = 1; j < 5; j++) {
                cell = row.getCell(j + 1);
                cell.setCellValue(indicators[i][j].get());
            }
        }

        // Evaluate the formulas
        XSSFFormulaEvaluator.evaluateAllFormulaCells(wbook);

        return wbook;
    }

    /**
     * Create and return a workbook that contains tb treatment report form for TB Treatment quy IV
     * program
     *
     * @param filter
     * @return
     */
    public Workbook createPreciousTBTreatmentReport4(CaseReportFilterDto filter) {
        Workbook wbook = null;
        try (InputStream template =
                context.getResource("classpath:templates/bcao_laohiv_quy4.xlsx").getInputStream()) {
            wbook = new XSSFWorkbook(template);
            //			wbook = new SXSSFWorkbook(new XSSFWorkbook(template), 100);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (wbook == null) {
            return blankBook;
        }

        Sheet sheet1 = wbook.getSheetAt(0);
        //		Sheet sheet2 = wbook.getSheetAt(1);

        // Variables I
        AtomicInteger[][] indicators = new AtomicInteger[13][5];
        for (int i = 0; i < 13; i++) {
            for (int j = 0; j < 5; j++) {
                indicators[i][j] = new AtomicInteger(0);
            }
        }

        // Variables II
        AtomicInteger[][] indicators1 = new AtomicInteger[3][5];
        for (int i = 1; i < 3; i++) {
            for (int j = 0; j < 5; j++) {
                indicators1[i][j] = new AtomicInteger(0);
            }
        }

        // Variables III
        AtomicInteger[][] indicators2 = new AtomicInteger[7][5];
        for (int i = 1; i < 7; i++) {
            for (int j = 0; j < 5; j++) {
                indicators2[i][j] = new AtomicInteger(0);
            }
        }

        // -------------------------------------------------
        // -> 1.1 Số bệnh nhân HIV mới đăng ký CSĐT trong năm (bao gồm BN chưa điều trị
        // ARV và đã điều trị ARV)
        // -------------------------------------------------

        for (Long orgId : filter.getActualOrganizations()) {
            List<CaseOrg> cases =
                    coRepos.findPatientsByEnrollmentTypes(
                            orgId,
                            CommonUtils.toTimestamp(filter.getFromYearDate()),
                            CommonUtils.toTimestamp(filter.getToYearDate()),
                            Lists.newArrayList("NEWLY_ENROLLED", "RETURNED", "TRANSFERRED_IN"),
                            false);

            cases.parallelStream()
                    .forEach(
                            e -> {
                                Case theCase = e.getTheCase();
                                if (theCase.getPerson() != null) {
                                    PersonDto person = new PersonDto(theCase.getPerson(), false);
                                    increasePeriod(person, indicators, 1);
                                }
                            });
        }

        // -------------------------------------------------
        // -> 2.1 Số bệnh nhân HIV mới đăng ký CSĐT HIV trong năm được bắt đầu dự phòng
        // lao trong kỳ báo cáo
        // -------------------------------------------------

        for (Long orgId : filter.getActualOrganizations()) {
            List<TBProphylaxis2> cases =
                    tbProphylaixs2Repository.findTBProphylaxis2StartDate(
                            orgId,
                            CommonUtils.toTimestamp(filter.getFromDate()),
                            CommonUtils.toTimestamp(filter.getToDate()),
                            CommonUtils.toTimestamp(filter.getFromYearDate()),
                            CommonUtils.toTimestamp(filter.getToYearDate()),
                            Lists.newArrayList("NEWLY_ENROLLED", "RETURNED", "TRANSFERRED_IN"));

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getTheCase().getPerson(), false);
                                increasePeriod(person, indicators, 2);
                            });
        }

        // -------------------------------------------------
        // -> 2.2 Số bệnh nhân HIV mới đăng ký CSĐT HIV trong năm được tiếp tục dự phòng
        // lao tại cơ sở trong kỳ báo cáo
        // (nhóm BN chuyển tới đã bắt đầu dự phòng lao trước đó)
        // -------------------------------------------------
        for (Long orgId : filter.getActualOrganizations()) {
            List<TBProphylaxis2Dispense> cases =
                    tbProphylaixs2Repository.findTBProphylaxis2RecordDate(
                            orgId,
                            CommonUtils.toTimestamp(filter.getFromDate()),
                            CommonUtils.toTimestamp(filter.getToDate()),
                            CommonUtils.toTimestamp(filter.getFromYearDate()),
                            CommonUtils.toTimestamp(filter.getToYearDate()),
                            Lists.newArrayList("NEWLY_ENROLLED", "RETURNED", "TRANSFERRED_IN"));

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getRound() == null
                                        || e.getRound().getTheCase() == null
                                        || e.getRound().getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person =
                                        new PersonDto(e.getRound().getTheCase().getPerson(), false);
                                increasePeriod(person, indicators, 3);
                            });
        }

        // -------------------------------------------------
        // -> 3.1 Số bệnh nhân HIV đăng ký CSĐT từ những năm trước được bắt đầu dự phòng
        // lao trong kỳ báo cáo
        // -------------------------------------------------

        for (Long orgId : filter.getActualOrganizations()) {
            List<TBProphylaxis2> cases =
                    tbProphylaixs2Repository.findTBProphylaxis2StartDateByEnrollmentTypesPreYear(
                            orgId,
                            CommonUtils.toTimestamp(filter.getFromDate()),
                            CommonUtils.toTimestamp(filter.getToDate()),
                            CommonUtils.toTimestamp(filter.getFromYearDate()),
                            Lists.newArrayList("NEWLY_ENROLLED", "RETURNED", "TRANSFERRED_IN"));

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getTheCase().getPerson(), false);
                                increasePeriod(person, indicators, 4);
                            });
        }

        // -------------------------------------------------
        // ->3.2 Số bệnh nhân HIV đăng ký CSĐT từ những năm trước được tiếp tục dự phòng
        // lao trong kỳ báo cáo
        // -------------------------------------------------
        for (Long orgId : filter.getActualOrganizations()) {
            List<TBProphylaxis2Dispense> cases =
                    tbProphylaixs2Repository.findTBProphylaxis2RecordDateByEnrollmentTypesPreYear(
                            orgId,
                            CommonUtils.toTimestamp(filter.getFromDate()),
                            CommonUtils.toTimestamp(filter.getToDate()),
                            CommonUtils.toTimestamp(filter.getFromYearDate()),
                            Lists.newArrayList("NEWLY_ENROLLED", "RETURNED", "TRANSFERRED_IN"));

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getRound() == null
                                        || e.getRound().getTheCase() == null
                                        || e.getRound().getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person =
                                        new PersonDto(e.getRound().getTheCase().getPerson(), false);
                                increasePeriod(person, indicators, 5);
                            });
        }

        // -------------------------------------------------
        // ->5.1 Số bệnh nhân HIV hiện quản lý cuối kỳ báo cáo
        // -------------------------------------------------

        for (Long orgId : filter.getActualOrganizations()) {
            List<Case> cases = new ArrayList<>();
//                    caseRepos.findPatientsByEnrollmentTypesEndOfReportPeriod(
//                            orgId,
//                            CommonUtils.toTimestamp(filter.getToDate()),
//                            Lists.newArrayList("NEWLY_ENROLLED", "RETURNED", "TRANSFERRED_IN"),
//                            false);

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getPerson(), false);
                                increasePeriod(person, indicators, 9);
                            });
        }

        // -------------------------------------------------
        // ->5.2: Số bệnh nhân HIV hiện quản lý đã và đang được dự phòng lao từ khi đăng
        // ký CSĐT HIV đến cuối kỳ báo cáo
        // -------------------------------------------------

        for (Long orgId : filter.getActualOrganizations()) {
            List<TBProphylaxis2> cases =
                    tbProphylaixs2Repository.findTBProphylaxis2StartDateEndOfReportPeriod(
                            orgId,
                            CommonUtils.toTimestamp(filter.getToDate()),
                            Lists.newArrayList("NEWLY_ENROLLED", "RETURNED", "TRANSFERRED_IN"));

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getTheCase().getPerson(), false);
                                increasePeriod(person, indicators, 10);
                            });
        }

        // -------------------------------------------------
        // ->5.3: Số bệnh nhân HIV hiện quản lý đã hoàn thành dự phòng lao (ít nhất 180
        // liều -phác đồ INH, đủ 12 liều -phác đồ 3HP) tính đến cuối kỳ báo cáo
        // -------------------------------------------------

        for (Long orgId : filter.getActualOrganizations()) {
            List<TBProphylaxis2> cases =
                    tbProphylaixs2Repository.findTBProphylaxis2CompleteEndOfReportPeriod(
                            orgId,
                            CommonUtils.toTimestamp(filter.getToDate()),
                            Lists.newArrayList("NEWLY_ENROLLED", "RETURNED", "TRANSFERRED_IN"));

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getTheCase().getPerson(), false);
                                increasePeriod(person, indicators, 11);
                            });
        }

        // ---------------------------------------------------------------------------------------------------
        // II.ĐIỀU TRỊ ARV CHO BỆNH NHÂN LAO NHIỄM HIV (Báo cáo theo năm - Điền thông
        // tin trong báo cáo quý 4)
        // ----------------------------------------------------------------------------------------------------
        // -------------------------------------------------
        // ->1.1: Số bệnh nhân lao/HIV đang điều trị lao, mới được tiếp nhận đăng ký
        // CSĐT HIV tại cơ sở trong kỳ báo cáo
        // -------------------------------------------------

        for (Long orgId : filter.getActualOrganizations()) {
            List<TBTreatment2> cases =
                    tbTreatment2Repository.findTBTreatment2TBScreeningType(
                            orgId,
                            Lists.newArrayList(
                                    "HIV_infected_TB_patient_transferred_from_a_TB_facility"),
                            CommonUtils.toTimestamp(filter.getFromYearDate()),
                            CommonUtils.toTimestamp(filter.getToYearDate()),
                            Lists.newArrayList("TRANSFERRED_IN"),
                            false);

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getTheCase().getPerson(), false);
                                increasePeriod(person, indicators1, 1);
                            });
        }

        // -------------------------------------------------
        // ->1.2: Số bệnh nhân lao/HIV đang điều trị lao, mới đăng ký CSĐT HIV từ đầu
        // năm và bắt đầu được điều trị ARV trong kỳ báo cáo
        // -------------------------------------------------
        // fix lỗi báo cáo quí 4 1.2 phần II
        for (Long orgId : filter.getActualOrganizations()) {
            List<TBTreatment2> cases =
                    tbTreatment2Repository.findTBTreatment2TBScreeningType_2(
                            orgId,
                            CommonUtils.toTimestamp(filter.getFromYearDate()),
                            CommonUtils.toTimestamp(filter.getToYearDate()));

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getTheCase().getPerson(), false);
                                increasePeriod(person, indicators1, 2);
                            });
        }

        //		for (Long orgId : filter.getActualOrganizations()) {
        //			List<TBTreatment2> cases =
        // tbTreatment2Repository.findTBTreatment2TBScreeningType(orgId,
        //					Lists.newArrayList("HIV_infected_TB_patient_transferred_from_a_TB_facility"),
        //					CommonUtils.toTimestamp(filter.getFromYearDate()),
        // CommonUtils.toTimestamp(filter.getToYearDate()),
        //					Lists.newArrayList("TRANSFERRED_IN"), true);
        //
        //			cases.parallelStream().forEach(e -> {
        //				if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
        //					return;
        //				}
        //
        //				PersonDto person = new PersonDto(e.getTheCase().getPerson(), false);
        //				increasePeriod(person, indicators1, 2);
        //			});
        //		}

        // ---------------------------------------------------------------------------------------------------
        // III. ĐIỀU TRỊ LAO CHO BỆNH NHÂN ĐIỀU TRỊ ARV MẮC LAO (Báo cáo theo năm - Điền
        // thông tin trong báo cáo quý 4)
        // ----------------------------------------------------------------------------------------------------
        // -------------------------------------------------
        // ->1.1: Số bệnh nhân đang điều trị ARV cuối kỳ báo cáo trước (tính đến quý
        // 4 năm trước)
        // -------------------------------------------------
        //		LocalDateTime toDatePre=LocalDateTime.of(filter.getSelYear()-1, 12, 31, 13, 59, 59, 0);;
        LocalDateTime toDatePre = filter.getFromYearDate().minus(Period.ofDays(1));

        for (Long orgId : filter.getActualOrganizations()) {
            List<Case> cases = new ArrayList<>();
            //			caseRepos.findPatientsByEnrollmentTypesEndOfReportPeriodPrevious(orgId,
            //					CommonUtils.toTimestamp(toDatePre),
            //					Lists.newArrayList("NEWLY_ENROLLED", "RETURNED", "TRANSFERRED_IN"), true);

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getPerson(), false);
                                increasePeriod(person, indicators2, 1);
                            });
        }

        // -------------------------------------------------
        // ->1.2: Số bệnh nhân ARV mới quản lý trong kỳ báo cáo
        // (bao gồm BN điều trị lần đầu, BN điều trị lại và BN chuyển đến để tiếp tục
        // điều trị)
        // -------------------------------------------------
        for (Long orgId : filter.getActualOrganizations()) {
            List<CaseOrg> caseOrgs =
                    coRepos.findPatientsByEnrollmentTypes(
                            orgId,
                            CommonUtils.toTimestamp(filter.getFromYearDate()),
                            CommonUtils.toTimestamp(filter.getToYearDate()),
                            Lists.newArrayList("NEWLY_ENROLLED", "RETURNED", "TRANSFERRED_IN"),
                            true);

            caseOrgs.parallelStream()
                    .forEach(
                            e -> {
                                Case theCase = e.getTheCase();
                                if (theCase.getPerson() != null) {
                                    PersonDto person = new PersonDto(theCase.getPerson(), false);
                                    increasePeriod(person, indicators2, 2);
                                }
                            });
        }

        // -------------------------------------------------
        // ->2: Số bệnh nhân điều trị ARV nghi lao đi khám lao trong kỳ báo cáo (do cơ
        // sở điều trị HIV chuyển gửi/tự BN đi khám lao)
        // -------------------------------------------------
        for (Long orgId : filter.getActualOrganizations()) {
            List<TBTreatment2> cases =
                    tbTreatment2Repository.findTBTreatment2TBScreeningDateAndType(
                            orgId,
                            Lists.newArrayList("patients_with_suspected_TB_visit_a_TB_facility"),
                            CommonUtils.toTimestamp(filter.getFromYearDate()),
                            CommonUtils.toTimestamp(filter.getToYearDate()),
                            true);

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getTheCase().getPerson(), false);
                                increasePeriod(person, indicators2, 3);
                            });
        }

        // -------------------------------------------------
        // ->3: Số bệnh nhân điều trị ARV nghi lao và được khám lao trong kỳ báo cáo
        // (tại cơ sở lao hoặc cơ sở ngoài CT chống lao)
        // -------------------------------------------------
        for (Long orgId : filter.getActualOrganizations()) {
            List<TBTreatment2> cases =
                    tbTreatment2Repository.findTBTreatment2TBDiagnosed(
                            orgId,
                            Lists.newArrayList("YES", "NO"),
                            CommonUtils.toTimestamp(filter.getFromYearDate()),
                            CommonUtils.toTimestamp(filter.getToYearDate()),
                            true);

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getTheCase().getPerson(), false);
                                increasePeriod(person, indicators2, 4);
                            });
        }

        // -------------------------------------------------
        // ->4: Số bệnh nhân điều trị ARV được chẩn đoán mắc lao trong kỳ báo cáo
        // -------------------------------------------------
        for (Long orgId : filter.getActualOrganizations()) {
            List<TBTreatment2> cases =
                    tbTreatment2Repository.findTBTreatment2TBDiagnosed(
                            orgId,
                            Lists.newArrayList("YES"),
                            CommonUtils.toTimestamp(filter.getFromYearDate()),
                            CommonUtils.toTimestamp(filter.getToYearDate()),
                            true);

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getTheCase().getPerson(), false);
                                increasePeriod(person, indicators2, 5);
                            });
        }

        // -------------------------------------------------
        // ->5: Số bệnh nhân điều trị ARV được chuẩn đoán và điều trị Lao trong kỳ báo
        // cáo
        // -------------------------------------------------
        for (Long orgId : filter.getActualOrganizations()) {
            List<TBTreatment2> cases =
                    tbTreatment2Repository.findTBTreatment2TBStartDate(
                            orgId,
                            CommonUtils.toTimestamp(filter.getFromYearDate()),
                            CommonUtils.toTimestamp(filter.getToYearDate()),
                            true);

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getTheCase().getPerson(), false);
                                increasePeriod(person, indicators2, 6);
                            });
        }

        // --> Fill out results
        Row row = null;
        Cell cell = null;

        // for worksheet #1
        for (int i = 1; i < 13; i++) {

            if (i > 1) {
                row = sheet1.getRow(i + 8);
            } else {
                row = sheet1.getRow(i + 7);
            }

            for (int j = 1; j < 5; j++) {
                cell = row.getCell(j + 1);
                cell.setCellValue(indicators[i][j].get());
            }
        }
        // II
        for (int i = 1; i < 3; i++) {
            row = sheet1.getRow(i + 25);

            for (int j = 1; j < 5; j++) {
                cell = row.getCell(j + 1);
                cell.setCellValue(indicators1[i][j].get());
            }
        }
        // III
        for (int i = 1; i < 7; i++) {
            row = sheet1.getRow(i + 31);

            for (int j = 1; j < 5; j++) {
                cell = row.getCell(j + 1);
                cell.setCellValue(indicators2[i][j].get());
            }
        }

        // Evaluate the formulas
        XSSFFormulaEvaluator.evaluateAllFormulaCells(wbook);

        return wbook;
    }

    /**
     * Create and return a workbook that contains tb treatment report form for TB Treatment program
     *
     * @param filter
     * @return
     */
    public Workbook createMonthTBTreatmentReport(CaseReportFilterDto filter) {
        Workbook wbook = null;
        try (InputStream template =
                context.getResource("classpath:templates/bcao_laohiv_thang.xlsx")
                        .getInputStream()) {
            wbook = new XSSFWorkbook(template);
            //			wbook = new SXSSFWorkbook(new XSSFWorkbook(template), 100);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (wbook == null) {
            return blankBook;
        }

        Sheet sheet1 = wbook.getSheetAt(0);
        //		Sheet sheet2 = wbook.getSheetAt(1);

        // Variables
        AtomicInteger[][] indicators1 = new AtomicInteger[3][9];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                indicators1[i][j] = new AtomicInteger(0);
            }
        }

        AtomicInteger[][] indicators = new AtomicInteger[3][5];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 5; j++) {
                indicators[i][j] = new AtomicInteger(0);
            }
        }

        // -------------------------------------------------
        // -> 1.1 Số bệnh nhân điều trị ARV bắt đầu dự phòng Lao trong kỳ báo cáo 6
        // tháng trước. (DPL từ 1/10/2019 - 31/3/2020)
        // -------------------------------------------------
        // Ví dụ chọn kỳ báo cáo 6 tháng từ (1/4/2020 đến 30/09/2020)

        // BN bắt đầu điều trị ARV trước kỳ báo cáo 6 tháng trước (BN ARV bắt đầu điều
        // trị trước ngày 1/10/2019)
        for (Long orgId : filter.getActualOrganizations()) {
            List<TBProphylaxis2> cases =
                    tbProphylaixs2Repository.findTBProphylaxis2PrePeriod6MonthByStartDate(
                            orgId,
                            CommonUtils.toTimestamp(filter.getPreFromDate()),
                            CommonUtils.toTimestamp(filter.getPreToDate()),
                            true);

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getTheCase().getPerson(), false);
                                increasePeriod1(person, indicators1, 1);
                            });
        }

        // BN bắt đầu điều trị ARV trong kỳ báo cáo 6 tháng trước (BN ARV bắt đầu điều
        // trị từ 1/10/2019 - 31/3/2020)
        for (Long orgId : filter.getActualOrganizations()) {
            List<TBProphylaxis2> cases =
                    tbProphylaixs2Repository.findTBProphylaxis26MonthByStartDate(
                            orgId,
                            CommonUtils.toTimestamp(filter.getPreFromDate()),
                            CommonUtils.toTimestamp(filter.getPreToDate()),
                            true);

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getTheCase().getPerson(), false);
                                increasePeriod(person, indicators, 1);
                            });
        }

        // -------------------------------------------------
        // -> Số bệnh nhân điều trị ARV bắt đầu dự phòng Lao trong kỳ báo cáo 6 tháng
        // trước nay đã hoàn thành dự phòng lao (ít nhất 180 liều -phác đồ INH, đủ 12
        // liều -phác đồ 3HP) trong kỳ báo cáo
        // -------------------------------------------------
        // Ví dụ chọn kỳ báo cáo 6 tháng từ (1/4/2020 đến 30/09/2020)

        // BN bắt đầu điều trị ARV trước kỳ báo cáo 6 tháng trước (BN ARV bắt đầu điều
        // trị trước ngày 1/10/2019)
        for (Long orgId : filter.getActualOrganizations()) {
            List<TBProphylaxis2> cases =
                    tbProphylaixs2Repository.findTBProphylaxis2PrePeriod6MonthByStartDateComplete(
                            orgId,
                            CommonUtils.toTimestamp(filter.getFromDate()),
                            CommonUtils.toTimestamp(filter.getToDate()),
                            CommonUtils.toTimestamp(filter.getPreFromDate()),
                            CommonUtils.toTimestamp(filter.getPreToDate()),
                            true);

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getTheCase().getPerson(), false);
                                increasePeriod1(person, indicators1, 2);
                            });
        }

        // BN bắt đầu điều trị ARV trong kỳ báo cáo 6 tháng trước (BN ARV bắt đầu điều
        // trị từ 1/10/2019 - 31/3/2020)
        for (Long orgId : filter.getActualOrganizations()) {
            List<TBProphylaxis2> cases =
                    tbProphylaixs2Repository.findTBProphylaxis26MonthByStartDateComplete(
                            orgId,
                            CommonUtils.toTimestamp(filter.getFromDate()),
                            CommonUtils.toTimestamp(filter.getToDate()),
                            CommonUtils.toTimestamp(filter.getPreFromDate()),
                            CommonUtils.toTimestamp(filter.getPreToDate()),
                            true);

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getTheCase().getPerson(), false);
                                increasePeriod(person, indicators, 2);
                            });
        }

        // Variables II. ĐIỀU TRỊ LAO CHO BỆNH NHÂN ĐIỀU TRỊ ARV MẮC LAO
        AtomicInteger[][] indicators2 = new AtomicInteger[15][9];
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 9; j++) {
                indicators2[i][j] = new AtomicInteger(0);
            }
        }

        AtomicInteger[][] indicators21 = new AtomicInteger[15][5];
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 5; j++) {
                indicators21[i][j] = new AtomicInteger(0);
            }
        }

        // -------------------------------------------------
        // 1.1 Số bệnh nhân đang điều trị ARV cuối kỳ báo cáo trước
        // -------------------------------------------------
        // BN bắt đầu điều trị ARV trước kỳ báo cáo

        for (Long orgId : filter.getActualOrganizations()) {
            List<Case> cases = new ArrayList<>();
//                    caseRepos.findPatientsByEnrollmentTypesEndOfReportPeriod6MonthPre(
//                            orgId,
//                            CommonUtils.toTimestamp(filter.getPreFromDate()),
//                            CommonUtils.toTimestamp(filter.getPreToDate()),
//                            true);

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getPerson(), false);
                                increasePeriod1(person, indicators2, 1);
                            });
        }

        // BN bắt đầu ARV trong kỳ báo cáo
        for (Long orgId : filter.getActualOrganizations()) {
            List<Case> cases = new ArrayList<>();
//                    caseRepos.findPatientsByEnrollmentTypesEndOfReportPeriod6Month(
//                            orgId,
//                            CommonUtils.toTimestamp(filter.getPreFromDate()),
//                            CommonUtils.toTimestamp(filter.getPreToDate()),
//                            true);

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getPerson(), false);
                                increasePeriod(person, indicators21, 1);
                            });
        }

        // -------------------------------------------------
        // ->1.2: Số bệnh nhân ARV mới quản lý trong kỳ báo cáo
        // (bao gồm BN điều trị lần đầu, BN điều trị lại và BN chuyển đến để tiếp tục
        // điều trị)
        // -------------------------------------------------
        // BN bắt đầu điều trị ARV trước kỳ báo cáo
        for (Long orgId : filter.getActualOrganizations()) {
            List<Case> cases = new ArrayList<>();
//                    caseRepos.findPatientsByEnrollmentTypesARVPreReport6Month(
//                            orgId,
//                            CommonUtils.toTimestamp(filter.getFromDate()),
//                            CommonUtils.toTimestamp(filter.getToDate()),
//                            Lists.newArrayList("RETURNED", "TRANSFERRED_IN"),
//                            true);

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getPerson(), false);
                                increasePeriod1(person, indicators2, 2);
                            });
        }

        // BN bắt đầu ARV trong kỳ báo cáo
        for (Long orgId : filter.getActualOrganizations()) {
            List<Case> cases = new ArrayList<>();
//                    caseRepos.findPatientsByEnrollmentTypesEndOfReportPeriod6Month(
//                            orgId,
//                            CommonUtils.toTimestamp(filter.getFromDate()),
//                            CommonUtils.toTimestamp(filter.getToDate()),
//                            true);

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getPerson(), false);
                                increasePeriod(person, indicators21, 2);
                            });
        }

        // -------------------------------------------------
        // ->2.1:Số bệnh nhân điều trị ARV sàng lọc lao dương tính (có ít nhất 1/4 triệu
        // chứng/yếu tố nghi lao) trong kỳ báo cáo
        // -------------------------------------------------
        // BN bắt đầu điều trị ARV trước kỳ báo cáo
        for (Long orgId : filter.getActualOrganizations()) {
            List<TBTreatment2> cases =
                    tbTreatment2Repository.findTBTreatment2TBScreeningARVPreReportPeriod(
                            orgId,
                            Lists.newArrayList(
                                    "upon_registration_of_the_treatment_facility",
                                    "during_the_management_of_the_Treatment_Facility"),
                            CommonUtils.toTimestamp(filter.getFromDate()),
                            CommonUtils.toTimestamp(filter.getToDate()),
                            true);

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getTheCase().getPerson(), false);
                                increasePeriod1(person, indicators2, 3);
                            });
        }
        // BN bắt đầu ARV trong kỳ báo cáo
        for (Long orgId : filter.getActualOrganizations()) {
            List<TBTreatment2> cases =
                    tbTreatment2Repository.findTBTreatment2TBScreeningARVInReportPeriod(
                            orgId,
                            Lists.newArrayList(
                                    "upon_registration_of_the_treatment_facility",
                                    "during_the_management_of_the_Treatment_Facility"),
                            CommonUtils.toTimestamp(filter.getFromDate()),
                            CommonUtils.toTimestamp(filter.getToDate()),
                            true);

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getTheCase().getPerson(), false);
                                increasePeriod(person, indicators21, 3);
                            });
        }
        // -------------------------------------------------
        // ->3:Số bệnh nhân điều trị ARV nghi lao đi khám lao trong kỳ báo cáo (do cơ sở
        // điều trị ARV chuyển gửi/tự BN đi khám lao)
        // -------------------------------------------------
        // BN bắt đầu điều trị ARV trước kỳ báo cáo
        for (Long orgId : filter.getActualOrganizations()) {
            List<TBTreatment2> cases =
                    tbTreatment2Repository.findTBTreatment2TBScreeningARVPreReportPeriod(
                            orgId,
                            Lists.newArrayList("patients_with_suspected_TB_visit_a_TB_facility"),
                            CommonUtils.toTimestamp(filter.getFromDate()),
                            CommonUtils.toTimestamp(filter.getToDate()),
                            true);

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getTheCase().getPerson(), false);
                                increasePeriod1(person, indicators2, 4);
                            });
        }
        // BN bắt đầu ARV trong kỳ báo cáo
        for (Long orgId : filter.getActualOrganizations()) {
            List<TBTreatment2> cases =
                    tbTreatment2Repository.findTBTreatment2TBScreeningARVInReportPeriod(
                            orgId,
                            Lists.newArrayList("patients_with_suspected_TB_visit_a_TB_facility"),
                            CommonUtils.toTimestamp(filter.getFromDate()),
                            CommonUtils.toTimestamp(filter.getToDate()),
                            true);

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getTheCase().getPerson(), false);
                                increasePeriod(person, indicators21, 4);
                            });
        }
        // -------------------------------------------------
        // ->4:Số bệnh nhân điều trị ARV nghi lao và được khám lao (tại cơ sở lao hoặc
        // cơ sở ngoài CT chống lao) trong kỳ báo cáo
        // -------------------------------------------------
        // BN bắt đầu điều trị ARV trước kỳ báo cáo
        for (Long orgId : filter.getActualOrganizations()) {
            List<TBTreatment2> cases =
                    tbTreatment2Repository.findTBTreatment2TBDiagnosedARVPreReportPeriod(
                            orgId,
                            Lists.newArrayList("YES", "NO"),
                            CommonUtils.toTimestamp(filter.getFromDate()),
                            CommonUtils.toTimestamp(filter.getToDate()),
                            true);

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getTheCase().getPerson(), false);
                                increasePeriod1(person, indicators2, 5);
                            });
        }
        // BN bắt đầu ARV trong kỳ báo cáo
        for (Long orgId : filter.getActualOrganizations()) {
            List<TBTreatment2> cases =
                    tbTreatment2Repository.findTBTreatment2TBDiagnosedARVInReportPeriod(
                            orgId,
                            Lists.newArrayList("YES", "NO"),
                            CommonUtils.toTimestamp(filter.getFromDate()),
                            CommonUtils.toTimestamp(filter.getToDate()),
                            true);

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getTheCase().getPerson(), false);
                                increasePeriod(person, indicators21, 5);
                            });
        }
        // -------------------------------------------------
        // ->4.1 Có xét nghiệm soi đờm trực tiếp
        // -------------------------------------------------
        // BN bắt đầu điều trị ARV trước kỳ báo cáo
        for (Long orgId : filter.getActualOrganizations()) {
            List<TBTreatment2> cases =
                    tbTreatment2Repository.findTBTreatment2TBDiagnosed4PreReportPeriod(
                            orgId,
                            Lists.newArrayList("YES", "NO"),
                            true,
                            false,
                            false,
                            false,
                            CommonUtils.toTimestamp(filter.getFromDate()),
                            CommonUtils.toTimestamp(filter.getToDate()),
                            true);

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getTheCase().getPerson(), false);
                                increasePeriod1(person, indicators2, 6);
                            });
        }
        // BN bắt đầu ARV trong kỳ báo cáo
        for (Long orgId : filter.getActualOrganizations()) {
            List<TBTreatment2> cases =
                    tbTreatment2Repository.findTBTreatment2TBDiagnosed4InReportPeriod(
                            orgId,
                            Lists.newArrayList("YES", "NO"),
                            true,
                            false,
                            false,
                            false,
                            CommonUtils.toTimestamp(filter.getFromDate()),
                            CommonUtils.toTimestamp(filter.getToDate()),
                            true);

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getTheCase().getPerson(), false);
                                increasePeriod(person, indicators21, 6);
                            });
        }

        // -------------------------------------------------
        // ->4.2 Có xét nghiệm Xpert
        // -------------------------------------------------
        // BN bắt đầu điều trị ARV trước kỳ báo cáo
        for (Long orgId : filter.getActualOrganizations()) {
            List<TBTreatment2> cases =
                    tbTreatment2Repository.findTBTreatment2TBDiagnosed4PreReportPeriod(
                            orgId,
                            Lists.newArrayList("YES", "NO"),
                            false,
                            true,
                            false,
                            false,
                            CommonUtils.toTimestamp(filter.getFromDate()),
                            CommonUtils.toTimestamp(filter.getToDate()),
                            true);

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getTheCase().getPerson(), false);
                                increasePeriod1(person, indicators2, 7);
                            });
        }
        // BN bắt đầu ARV trong kỳ báo cáo
        for (Long orgId : filter.getActualOrganizations()) {
            List<TBTreatment2> cases =
                    tbTreatment2Repository.findTBTreatment2TBDiagnosed4InReportPeriod(
                            orgId,
                            Lists.newArrayList("YES", "NO"),
                            false,
                            true,
                            false,
                            false,
                            CommonUtils.toTimestamp(filter.getFromDate()),
                            CommonUtils.toTimestamp(filter.getToDate()),
                            true);

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getTheCase().getPerson(), false);
                                increasePeriod(person, indicators21, 7);
                            });
        }

        // -------------------------------------------------
        // ->4.3 Có chụp X quang ngực
        // -------------------------------------------------
        // BN bắt đầu điều trị ARV trước kỳ báo cáo
        for (Long orgId : filter.getActualOrganizations()) {
            List<TBTreatment2> cases =
                    tbTreatment2Repository.findTBTreatment2TBDiagnosed4PreReportPeriod(
                            orgId,
                            Lists.newArrayList("YES", "NO"),
                            false,
                            false,
                            true,
                            false,
                            CommonUtils.toTimestamp(filter.getFromDate()),
                            CommonUtils.toTimestamp(filter.getToDate()),
                            true);

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getTheCase().getPerson(), false);
                                increasePeriod1(person, indicators2, 8);
                            });
        }
        // BN bắt đầu ARV trong kỳ báo cáo
        for (Long orgId : filter.getActualOrganizations()) {
            List<TBTreatment2> cases =
                    tbTreatment2Repository.findTBTreatment2TBDiagnosed4InReportPeriod(
                            orgId,
                            Lists.newArrayList("YES", "NO"),
                            false,
                            false,
                            true,
                            false,
                            CommonUtils.toTimestamp(filter.getFromDate()),
                            CommonUtils.toTimestamp(filter.getToDate()),
                            true);

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getTheCase().getPerson(), false);
                                increasePeriod(person, indicators21, 8);
                            });
        }

        // -------------------------------------------------
        // ->4.4 Có làm các xét nghiệm khác (hạch đồ, dịch màng phổi, bụng……)
        // -------------------------------------------------
        // BN bắt đầu điều trị ARV trước kỳ báo cáo
        for (Long orgId : filter.getActualOrganizations()) {
            List<TBTreatment2> cases =
                    tbTreatment2Repository.findTBTreatment2TBDiagnosed4PreReportPeriod(
                            orgId,
                            Lists.newArrayList("YES", "NO"),
                            false,
                            false,
                            false,
                            true,
                            CommonUtils.toTimestamp(filter.getFromDate()),
                            CommonUtils.toTimestamp(filter.getToDate()),
                            true);

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getTheCase().getPerson(), false);
                                increasePeriod1(person, indicators2, 9);
                            });
        }
        // BN bắt đầu ARV trong kỳ báo cáo
        for (Long orgId : filter.getActualOrganizations()) {
            List<TBTreatment2> cases =
                    tbTreatment2Repository.findTBTreatment2TBDiagnosed4InReportPeriod(
                            orgId,
                            Lists.newArrayList("YES", "NO"),
                            false,
                            false,
                            false,
                            true,
                            CommonUtils.toTimestamp(filter.getFromDate()),
                            CommonUtils.toTimestamp(filter.getToDate()),
                            true);

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getTheCase().getPerson(), false);
                                increasePeriod(person, indicators21, 9);
                            });
        }

        // -------------------------------------------------
        // ->5.Số bệnh nhân điều trị ARV được chẩn đoán mắc lao trong kỳ báo cáo
        // -------------------------------------------------
        // BN bắt đầu điều trị ARV trước kỳ báo cáo
        for (Long orgId : filter.getActualOrganizations()) {
            List<TBTreatment2> cases =
                    tbTreatment2Repository.findTBTreatment2TBDiagnosedARVPreReportPeriod(
                            orgId,
                            Lists.newArrayList("YES"),
                            CommonUtils.toTimestamp(filter.getFromDate()),
                            CommonUtils.toTimestamp(filter.getToDate()),
                            true);

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getTheCase().getPerson(), false);
                                increasePeriod1(person, indicators2, 10);
                            });
        }
        // BN bắt đầu ARV trong kỳ báo cáo
        for (Long orgId : filter.getActualOrganizations()) {
            List<TBTreatment2> cases =
                    tbTreatment2Repository.findTBTreatment2TBDiagnosedARVInReportPeriod(
                            orgId,
                            Lists.newArrayList("YES"),
                            CommonUtils.toTimestamp(filter.getFromDate()),
                            CommonUtils.toTimestamp(filter.getToDate()),
                            true);

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getTheCase().getPerson(), false);
                                increasePeriod(person, indicators21, 10);
                            });
        }

        // -------------------------------------------------
        // ->6.Số bệnh nhân điều trị ARV được chẩn đoán và điều trị Lao trong kỳ báo cáo
        // -------------------------------------------------
        // BN bắt đầu điều trị ARV trước kỳ báo cáo
        for (Long orgId : filter.getActualOrganizations()) {
            List<TBTreatment2> cases =
                    tbTreatment2Repository.findTBTreatment2TBStartDateARVPreReportPeriod(
                            orgId,
                            CommonUtils.toTimestamp(filter.getFromDate()),
                            CommonUtils.toTimestamp(filter.getToDate()),
                            true);

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getTheCase().getPerson(), false);
                                increasePeriod1(person, indicators2, 11);
                            });
        }
        // BN bắt đầu ARV trong kỳ báo cáo
        for (Long orgId : filter.getActualOrganizations()) {
            List<TBTreatment2> cases =
                    tbTreatment2Repository.findTBTreatment2TBStartDateARVInReportPeriod(
                            orgId,
                            CommonUtils.toTimestamp(filter.getFromDate()),
                            CommonUtils.toTimestamp(filter.getToDate()),
                            true);

            cases.parallelStream()
                    .forEach(
                            e -> {
                                if (e.getTheCase() == null || e.getTheCase().getPerson() == null) {
                                    return;
                                }

                                PersonDto person = new PersonDto(e.getTheCase().getPerson(), false);
                                increasePeriod(person, indicators21, 11);
                            });
        }
        // --> Fill out results
        Row row = null;
        Cell cell = null;

        // for worksheet #1
        for (int i = 1; i < 3; i++) {

            row = sheet1.getRow(i + 8);

            for (int j = 5; j < 9; j++) {
                cell = row.getCell(j + 1);
                cell.setCellValue(indicators1[i][j].get());
            }
            for (int j = 1; j < 5; j++) {
                cell = row.getCell(j + 1);
                cell.setCellValue(indicators[i][j].get());
            }
        }

        // Mục II
        for (int i = 1; i < 14; i++) {
            if (i > 3) {
                row = sheet1.getRow(i + 17);
            } else if (i == 3) {
                row = sheet1.getRow(i + 16);
            } else row = sheet1.getRow(i + 15);

            for (int j = 5; j < 9; j++) {
                cell = row.getCell(j + 1);
                cell.setCellValue(indicators2[i][j].get());
            }
            for (int j = 1; j < 5; j++) {
                cell = row.getCell(j + 1);
                cell.setCellValue(indicators21[i][j].get());
            }
        }

        // Evaluate the formulas
        XSSFFormulaEvaluator.evaluateAllFormulaCells(wbook);

        return wbook;
    }

    public static void increasePeriod(PersonDto person, AtomicInteger[][] arr, int i) {

        LocalDateTime dob = person.getDob();
        Gender gender = person.getGender();

        if (dob == null || gender == null) {
            return;
        }

        long age = CommonUtils.dateDiff(ChronoUnit.YEARS, dob, CommonUtils.hanoiNow());
        int j = -1;

        if (age < 15) {
            switch (gender) {
                case MALE:
                    j = 1;
                    break;
                case FEMALE:
                    j = 2;
                    break;
                default:
                    break;
            }
        } else {
            switch (gender) {
                case MALE:
                    j = 3;
                    break;
                case FEMALE:
                    j = 4;
                    break;
                default:
                    break;
            }
        }

        if (i >= 0 && j >= 0) {
            arr[i][j].incrementAndGet();
        }
    }

    public static void increasePeriod1(PersonDto person, AtomicInteger[][] arr, int i) {

        LocalDateTime dob = person.getDob();
        Gender gender = person.getGender();

        if (dob == null || gender == null) {
            return;
        }

        long age = CommonUtils.dateDiff(ChronoUnit.YEARS, dob, CommonUtils.hanoiNow());
        int j = -1;

        if (age < 15) {
            switch (gender) {
                case MALE:
                    j = 5;
                    break;
                case FEMALE:
                    j = 6;
                    break;
                default:
                    break;
            }
        } else {
            switch (gender) {
                case MALE:
                    j = 7;
                    break;
                case FEMALE:
                    j = 8;
                    break;
                default:
                    break;
            }
        }

        if (i >= 0 && j >= 0) {
            arr[i][j].incrementAndGet();
        }
    }
}
