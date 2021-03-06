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

                String title = "B??O C??O QU?? HO???T ?????NG LAO/HIV \n";
                title += " T??? ng??y ";
                title += filter.getFromDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                title += " ?????n ng??y ";
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
                            "BQLTDA t???nh: " + orgs.get(0).getAddress().getProvince().getName());
                } else {
                    ExcelUtils.writeInCell(
                            sheet1, 0, 0, "B??o c??o c???a nhi???u t???nh (danh s??ch t???nh ??? ph??a d?????i)");
                    // List all provice
                    if (multiProvince) {
                        if (filter != null && filter.getSelQuarter() == 4) {
                            ExcelUtils.createAndWriteInCell(
                                    sheet1, 46, 0, "Danh s??ch c??c t???nh b??o c??o:");
                        } else
                            ExcelUtils.createAndWriteInCell(
                                    sheet1, 25, 0, "Danh s??ch c??c t???nh b??o c??o:");
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
                    ExcelUtils.writeInCell(sheet1, 1, 0, "C?? s???: " + orgs.get(0).getName());
                } else {
                    if (!multiProvince
                            && orgs.get(0).getAddress() != null
                            && orgs.get(0).getAddress().getProvince() != null) {
                        ExcelUtils.writeInCell(
                                sheet1,
                                1,
                                0,
                                "T???nh/th??nh ph???: "
                                        + orgs.get(0).getAddress().getProvince().getName()
                                        + " (xem danh s??ch c?? s??? ph??a d?????i)");
                    } else {
                        ExcelUtils.writeInCell(
                                sheet1,
                                1,
                                0,
                                "B??o c??o c???a nhi???u c?? s??? (danh s??ch c?? s??? ph??a d?????i)");
                    }

                    // List all sites beneath
                    if (filter != null && filter.getSelQuarter() == 4) {
                        ExcelUtils.createAndWriteInCell(
                                sheet1, 48, 0, "Danh s??ch c??c c?? s??? b??o c??o:");
                    } else
                        ExcelUtils.createAndWriteInCell(
                                sheet1, 27, 0, "Danh s??ch c??c c?? s??? b??o c??o:");
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
                wbook.setSheetName(0, "BC 6 th??ng l???n " + filter.getSelQuarter());
                //				Sheet sheet2 = wbook.getSheetAt(1);

                // insert period data

                title = "B??O C??O 6 TH??NG HO???T ?????NG LAO/HIV  \n";
                title += " T??? ng??y ";
                title += filter.getFromDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                title += " ?????n ng??y ";
                title += filter.getToDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                title += " (N??m t??i ch??nh c???a PEPFAR)";
                ExcelUtils.writeInCell(sheet1, 3, 0, title);

                // insert administrative data
                if (!multiProvince
                        && orgs.get(0).getAddress() != null
                        && orgs.get(0).getAddress().getProvince() != null) {
                    ExcelUtils.writeInCell(
                            sheet1,
                            0,
                            0,
                            "BQLTDA t???nh: " + orgs.get(0).getAddress().getProvince().getName());
                } else {
                    ExcelUtils.writeInCell(
                            sheet1, 0, 0, "B??o c??o c???a nhi???u t???nh (danh s??ch t???nh ??? ph??a d?????i)");
                    // List all provice
                    if (multiProvince) {

                        ExcelUtils.createAndWriteInCell(
                                sheet1, 33, 0, "Danh s??ch c??c t???nh b??o c??o:");
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
                    ExcelUtils.writeInCell(sheet1, 1, 0, "C?? s???: " + orgs.get(0).getName());
                } else {
                    if (!multiProvince
                            && orgs.get(0).getAddress() != null
                            && orgs.get(0).getAddress().getProvince() != null) {
                        ExcelUtils.writeInCell(
                                sheet1,
                                1,
                                0,
                                "T???nh/th??nh ph???: "
                                        + orgs.get(0).getAddress().getProvince().getName()
                                        + " (xem danh s??ch c?? s??? ph??a d?????i)");
                    } else {
                        ExcelUtils.writeInCell(
                                sheet1,
                                1,
                                0,
                                "B??o c??o c???a nhi???u c?? s??? (danh s??ch c?? s??? ph??a d?????i)");
                    }

                    // List all sites beneath

                    ExcelUtils.createAndWriteInCell(sheet1, 35, 0, "Danh s??ch c??c c?? s??? b??o c??o:");
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
        // -> 1.1 S??? b???nh nh??n HIV m???i ????ng k?? CS??T trong n??m (bao g???m BN ch??a ??i???u tr???
        // ARV v?? ???? ??i???u tr??? ARV)
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
        // -> 2.1 S??? b???nh nh??n HIV m???i ????ng k?? CS??T HIV trong n??m ???????c b???t ?????u d??? ph??ng
        // lao trong k??? b??o c??o
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
        // -> 2.2 S??? b???nh nh??n HIV m???i ????ng k?? CS??T HIV trong n??m ???????c ti???p t???c d??? ph??ng
        // lao t???i c?? s??? trong k??? b??o c??o
        // (nh??m BN chuy???n t???i ???? b???t ?????u d??? ph??ng lao tr?????c ????)
        // -------------------------------------------------
        for (Long orgId : filter.getActualOrganizations()) {
            List<TBProphylaxis2Dispense> cases =
                    tbProphylaixs2Repository.findTBProphylaxis2RecordDate(
                            orgId,
                            CommonUtils.toTimestamp(filter.getFromDate()),
                            CommonUtils.toTimestamp(filter.getToDate()),
                            CommonUtils.toTimestamp(filter.getFromYearDate()),
                            CommonUtils.toTimestamp(filter.getToYearDate()),
                            Lists.newArrayList("TRANSFERRED_IN")); // nh??m b???nh nh??n chuy???n t???i

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
        // -> 3.1 S??? b???nh nh??n HIV ????ng k?? CS??T t??? nh???ng n??m tr?????c ???????c b???t ?????u d??? ph??ng
        // lao trong k??? b??o c??o
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
        // ->3.2 S??? b???nh nh??n HIV ????ng k?? CS??T t??? nh???ng n??m tr?????c ???????c ti???p t???c d??? ph??ng
        // lao trong k??? b??o c??o
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
        // ->5.1 S??? b???nh nh??n HIV hi???n qu???n l?? cu???i k??? b??o c??o
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
        // ->5.2: S??? b???nh nh??n HIV hi???n qu???n l?? ???? v?? ??ang ???????c d??? ph??ng lao t??? khi ????ng
        // k?? CS??T HIV ?????n cu???i k??? b??o c??o
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
        // ->5.3: S??? b???nh nh??n HIV hi???n qu???n l?? ???? ho??n th??nh d??? ph??ng lao (??t nh???t 180
        // li???u -ph??c ????? INH, ????? 12 li???u -ph??c ????? 3HP) t??nh ?????n cu???i k??? b??o c??o
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
        // -> 1.1 S??? b???nh nh??n HIV m???i ????ng k?? CS??T trong n??m (bao g???m BN ch??a ??i???u tr???
        // ARV v?? ???? ??i???u tr??? ARV)
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
        // -> 2.1 S??? b???nh nh??n HIV m???i ????ng k?? CS??T HIV trong n??m ???????c b???t ?????u d??? ph??ng
        // lao trong k??? b??o c??o
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
        // -> 2.2 S??? b???nh nh??n HIV m???i ????ng k?? CS??T HIV trong n??m ???????c ti???p t???c d??? ph??ng
        // lao t???i c?? s??? trong k??? b??o c??o
        // (nh??m BN chuy???n t???i ???? b???t ?????u d??? ph??ng lao tr?????c ????)
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
        // -> 3.1 S??? b???nh nh??n HIV ????ng k?? CS??T t??? nh???ng n??m tr?????c ???????c b???t ?????u d??? ph??ng
        // lao trong k??? b??o c??o
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
        // ->3.2 S??? b???nh nh??n HIV ????ng k?? CS??T t??? nh???ng n??m tr?????c ???????c ti???p t???c d??? ph??ng
        // lao trong k??? b??o c??o
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
        // ->5.1 S??? b???nh nh??n HIV hi???n qu???n l?? cu???i k??? b??o c??o
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
        // ->5.2: S??? b???nh nh??n HIV hi???n qu???n l?? ???? v?? ??ang ???????c d??? ph??ng lao t??? khi ????ng
        // k?? CS??T HIV ?????n cu???i k??? b??o c??o
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
        // ->5.3: S??? b???nh nh??n HIV hi???n qu???n l?? ???? ho??n th??nh d??? ph??ng lao (??t nh???t 180
        // li???u -ph??c ????? INH, ????? 12 li???u -ph??c ????? 3HP) t??nh ?????n cu???i k??? b??o c??o
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
        // II.??I???U TR??? ARV CHO B???NH NH??N LAO NHI???M HIV (B??o c??o theo n??m - ??i???n th??ng
        // tin trong b??o c??o qu?? 4)
        // ----------------------------------------------------------------------------------------------------
        // -------------------------------------------------
        // ->1.1: S??? b???nh nh??n lao/HIV ??ang ??i???u tr??? lao, m???i ???????c ti???p nh???n ????ng k??
        // CS??T HIV t???i c?? s??? trong k??? b??o c??o
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
        // ->1.2: S??? b???nh nh??n lao/HIV ??ang ??i???u tr??? lao, m???i ????ng k?? CS??T HIV t??? ?????u
        // n??m v?? b???t ?????u ???????c ??i???u tr??? ARV trong k??? b??o c??o
        // -------------------------------------------------
        // fix l???i b??o c??o qu?? 4 1.2 ph???n II
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
        // III. ??I???U TR??? LAO CHO B???NH NH??N ??I???U TR??? ARV M???C LAO (B??o c??o theo n??m - ??i???n
        // th??ng tin trong b??o c??o qu?? 4)
        // ----------------------------------------------------------------------------------------------------
        // -------------------------------------------------
        // ->1.1: S???? b???nh nh??n ??ang ??i????u tri?? ARV cu???i k??? b??o c??o tr?????c (t??nh ?????n qu??
        // 4 n??m tr?????c)
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
        // ->1.2: S???? b???nh nh??n ARV m???i qu???n l?? trong k??? b??o c??o
        // (bao g???m BN ??i???u tr??? l???n ?????u, BN ??i???u tr??? l???i v?? BN chuy???n ?????n ????? ti???p t???c
        // ??i???u tr???)
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
        // ->2: S??? b???nh nh??n ??i???u tr??? ARV nghi lao ??i kh??m lao trong k??? b??o c??o (do c??
        // s??? ??i???u tr??? HIV chuy???n g???i/t??? BN ??i kh??m lao)
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
        // ->3: S??? b???nh nh??n ??i???u tr??? ARV nghi lao v?? ???????c kh??m lao trong k??? b??o c??o
        // (t???i c?? s??? lao ho???c c?? s??? ngo??i CT ch???ng lao)
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
        // ->4: S??? b???nh nh??n ??i???u tr??? ARV ???????c ch???n ??o??n m???c lao trong k??? b??o c??o
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
        // ->5: S??? b???nh nh??n ??i???u tr??? ARV ???????c chu???n ??o??n v?? ??i???u tr??? Lao trong k??? b??o
        // c??o
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
        // -> 1.1 S??? b???nh nh??n ??i???u tr??? ARV b???t ?????u d??? ph??ng Lao trong k??? b??o c??o 6
        // th??ng tr?????c. (DPL t??? 1/10/2019 - 31/3/2020)
        // -------------------------------------------------
        // V?? d??? ch???n k??? b??o c??o 6 th??ng t??? (1/4/2020 ?????n 30/09/2020)

        // BN b???t ?????u ??i???u tr??? ARV tr?????c k??? b??o c??o 6 th??ng tr?????c (BN ARV b???t ?????u ??i???u
        // tr??? tr?????c ng??y 1/10/2019)
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

        // BN b???t ?????u ??i???u tr??? ARV trong k??? b??o c??o 6 th??ng tr?????c (BN ARV b???t ?????u ??i???u
        // tr??? t??? 1/10/2019 - 31/3/2020)
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
        // -> S??? b???nh nh??n ??i???u tr??? ARV b???t ?????u d??? ph??ng Lao trong k??? b??o c??o 6 th??ng
        // tr?????c nay ???? ho??n th??nh d??? ph??ng lao (??t nh???t 180 li???u -ph??c ????? INH, ????? 12
        // li???u -ph??c ????? 3HP) trong k??? b??o c??o
        // -------------------------------------------------
        // V?? d??? ch???n k??? b??o c??o 6 th??ng t??? (1/4/2020 ?????n 30/09/2020)

        // BN b???t ?????u ??i???u tr??? ARV tr?????c k??? b??o c??o 6 th??ng tr?????c (BN ARV b???t ?????u ??i???u
        // tr??? tr?????c ng??y 1/10/2019)
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

        // BN b???t ?????u ??i???u tr??? ARV trong k??? b??o c??o 6 th??ng tr?????c (BN ARV b???t ?????u ??i???u
        // tr??? t??? 1/10/2019 - 31/3/2020)
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

        // Variables II. ??I???U TR??? LAO CHO B???NH NH??N ??I???U TR??? ARV M???C LAO
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
        // 1.1 S???? b???nh nh??n ??ang ??i????u tri?? ARV cu???i k??? b??o c??o tr?????c
        // -------------------------------------------------
        // BN b???t ?????u ??i???u tr??? ARV tr?????c k??? b??o c??o

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

        // BN b???t ?????u ARV trong k??? b??o c??o
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
        // ->1.2: S???? b???nh nh??n ARV m???i qu???n l?? trong k??? b??o c??o
        // (bao g???m BN ??i???u tr??? l???n ?????u, BN ??i???u tr??? l???i v?? BN chuy???n ?????n ????? ti???p t???c
        // ??i???u tr???)
        // -------------------------------------------------
        // BN b???t ?????u ??i???u tr??? ARV tr?????c k??? b??o c??o
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

        // BN b???t ?????u ARV trong k??? b??o c??o
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
        // ->2.1:S??? b???nh nh??n ??i???u tr??? ARV s??ng l???c lao d????ng t??nh (c?? ??t nh???t 1/4 tri???u
        // ch???ng/y???u t??? nghi lao) trong k??? b??o c??o
        // -------------------------------------------------
        // BN b???t ?????u ??i???u tr??? ARV tr?????c k??? b??o c??o
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
        // BN b???t ?????u ARV trong k??? b??o c??o
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
        // ->3:S??? b???nh nh??n ??i???u tr??? ARV nghi lao ??i kh??m lao trong k??? b??o c??o (do c?? s???
        // ??i???u tr??? ARV chuy???n g???i/t??? BN ??i kh??m lao)
        // -------------------------------------------------
        // BN b???t ?????u ??i???u tr??? ARV tr?????c k??? b??o c??o
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
        // BN b???t ?????u ARV trong k??? b??o c??o
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
        // ->4:S??? b???nh nh??n ??i???u tr??? ARV nghi lao v?? ???????c kh??m lao (t???i c?? s??? lao ho???c
        // c?? s??? ngo??i CT ch???ng lao) trong k??? b??o c??o
        // -------------------------------------------------
        // BN b???t ?????u ??i???u tr??? ARV tr?????c k??? b??o c??o
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
        // BN b???t ?????u ARV trong k??? b??o c??o
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
        // ->4.1 C?? x??t nghi???m soi ?????m tr???c ti???p
        // -------------------------------------------------
        // BN b???t ?????u ??i???u tr??? ARV tr?????c k??? b??o c??o
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
        // BN b???t ?????u ARV trong k??? b??o c??o
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
        // ->4.2 C?? x??t nghi???m Xpert
        // -------------------------------------------------
        // BN b???t ?????u ??i???u tr??? ARV tr?????c k??? b??o c??o
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
        // BN b???t ?????u ARV trong k??? b??o c??o
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
        // ->4.3 C?? ch???p X quang ng???c
        // -------------------------------------------------
        // BN b???t ?????u ??i???u tr??? ARV tr?????c k??? b??o c??o
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
        // BN b???t ?????u ARV trong k??? b??o c??o
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
        // ->4.4 C?? l??m c??c x??t nghi???m kh??c (h???ch ?????, d???ch m??ng ph???i, b???ng??????)
        // -------------------------------------------------
        // BN b???t ?????u ??i???u tr??? ARV tr?????c k??? b??o c??o
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
        // BN b???t ?????u ARV trong k??? b??o c??o
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
        // ->5.S??? b???nh nh??n ??i???u tr??? ARV ???????c ch???n ??o??n m???c lao trong k??? b??o c??o
        // -------------------------------------------------
        // BN b???t ?????u ??i???u tr??? ARV tr?????c k??? b??o c??o
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
        // BN b???t ?????u ARV trong k??? b??o c??o
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
        // ->6.S??? b???nh nh??n ??i???u tr??? ARV ???????c ch???n ??o??n v?? ??i???u tr??? Lao trong k??? b??o c??o
        // -------------------------------------------------
        // BN b???t ?????u ??i???u tr??? ARV tr?????c k??? b??o c??o
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
        // BN b???t ?????u ARV trong k??? b??o c??o
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

        // M???c II
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
