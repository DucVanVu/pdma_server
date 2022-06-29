package org.pepfar.pdma.app.data.service.jpa;

import com.google.common.collect.Lists;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pepfar.pdma.app.Constants;
import org.pepfar.pdma.app.data.domain.*;
import org.pepfar.pdma.app.data.dto.CaseReportFilterDto;
import org.pepfar.pdma.app.data.dto.PatientExtraData;
import org.pepfar.pdma.app.data.repository.CaseOrgRepository;
import org.pepfar.pdma.app.data.repository.CaseRepository;
import org.pepfar.pdma.app.data.repository.LabTestRepository;
import org.pepfar.pdma.app.data.repository.OrganizationRepository;
import org.pepfar.pdma.app.data.service._ReportingService;
import org.pepfar.pdma.app.data.types.PatientStatus;
import org.pepfar.pdma.app.data.types.Permission;
import org.pepfar.pdma.app.data.types.ReportType;
import org.pepfar.pdma.app.data.types.ReportingAlgorithm;
import org.pepfar.pdma.app.utils.AuthorizationUtils;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.ExcelUtils;
import org.pepfar.pdma.app.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service("_PatientExportServiceImpl")
public class _PatientExportServiceImpl implements _ReportingService {

    @Autowired private AuthorizationUtils authUtils;

    @Autowired private OrganizationRepository orgRepos;

    @Autowired private CaseOrgRepository coRepos;

    @Autowired private CaseRepository cRepos;

    @Autowired private LabTestRepository labRepos;

    @Autowired private ApplicationContext context;

    private Workbook blankBook;

    @Autowired private ProjectionFactory projectionFactory;

    @Override
    @Transactional(readOnly = true)
    public Workbook exportReport(CaseReportFilterDto filter) {

        List<ReportType> acceptedTypes =
                Lists.newArrayList(
                        ReportType.ACTIVE_PATIENT_REPORT,
                        ReportType.NEW_PATIENT_REPORT,
                        ReportType.NEWLY_ENROLLED_PATIENT_REPORT,
                        ReportType.RETURNED_PATIENT_REPORT,
                        ReportType.TRANSEDIN_PATIENT_REPORT,
                        ReportType.DEAD_LTFU_PATIENT_REPORT,
                        ReportType.TRANSOUT_PATIENT_REPORT,
                        ReportType.TX_TABULAR_SUMMARY,
                        ReportType.MMD_LINELIST_DATA,
                        ReportType.LY_REQUEST_SEP_2021,
                        ReportType.LY_REQUEST_SEP_2021_2);

        blankBook = new XSSFWorkbook();
        blankBook.createSheet();

        if (filter == null
                || filter.getReportType() == null
                || !acceptedTypes.contains(filter.getReportType())) {
            return blankBook;
        }

        // Confidentiality info
        User user = SecurityUtils.getCurrentUser();
        boolean confidentialRequired = false;

        if (SecurityUtils.isUserInRoles(
                user,
                Constants.ROLE_ADMIN,
                Constants.ROLE_DONOR,
                Constants.ROLE_NATIONAL_MANAGER)) {
            confidentialRequired = true;
        }

        filter.setConfidentialRequired(confidentialRequired);

        // Date/time adjustment
        LocalDateTime adjAtDate = null;
        LocalDateTime adjFromDate = null;
        LocalDateTime adjToDate = null;

        if (filter.getFromDate() != null) {
            adjFromDate = CommonUtils.dateStart(filter.getFromDate());
        }

        if (filter.getToDate() != null) {
            adjToDate = CommonUtils.dateEnd(filter.getToDate());
        }

        if (filter.getAtDate() != null) {
            adjAtDate = CommonUtils.dateEnd(filter.getAtDate());
        }

        // Set adjusted date and time
        filter.setAtDate(adjAtDate);
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

        if (filter.getReportType() == ReportType.TX_TABULAR_SUMMARY) {
            return createTxSummaryWorkbook(filter);
        } else if (filter.getReportType() == ReportType.LY_REQUEST_SEP_2021) {
            return createLySep2021Workbook(filter);
        } else if (filter.getReportType() == ReportType.LY_REQUEST_SEP_2021_2) {
            return createLySep2021_2_Workbook(filter);
        }

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

        String sheetName = "";
        String listTitle = "";
        String periodStr = "";

        if (!multiSite) {
            periodStr = orgs.get(0).getName() + " - ";
        } else {
            periodStr = "Nhiều cơ sở (danh sách ở worksheet kế bên) - ";
        }

        if (filter.getReportType() == ReportType.ACTIVE_PATIENT_REPORT
                || filter.getReportType() == ReportType.MMD_LINELIST_DATA) {
            periodStr += "Ngày chốt báo cáo: ";
            periodStr += filter.getAtDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } else {
            periodStr += "Giai đoạn báo cáo: ";
            periodStr += filter.getFromDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            periodStr += " - ";
            periodStr += filter.getToDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }

        if (filter.getReportType() == ReportType.MMD_LINELIST_DATA) {
            return createMmdLinelist(filter, periodStr);
        }

        switch (filter.getReportType()) {
            case ACTIVE_PATIENT_REPORT:
                sheetName = "Bệnh nhân đang quản lý";
                listTitle = "Danh sách bệnh nhân đang quản lý";
                wbook = createActiveOrNewPatientsWorkbook(filter, listTitle, periodStr);
                break;
            case NEW_PATIENT_REPORT:
                sheetName = "Bệnh nhân mới đăng ký điều trị";
                listTitle = "Danh sách bệnh nhân mới đăng ký điều trị";
                wbook = createActiveOrNewPatientsWorkbook(filter, listTitle, periodStr);
                break;
            case NEWLY_ENROLLED_PATIENT_REPORT:
                sheetName = "Bệnh nhân điều trị lần đầu";
                listTitle = "Danh sách bệnh nhân điều trị lần đầu";
                wbook = createActiveOrNewPatientsWorkbook(filter, listTitle, periodStr);
                break;
            case RETURNED_PATIENT_REPORT:
                sheetName = "Bệnh nhân quay lại điều trị";
                listTitle = "Danh sách bệnh nhân quay lại điều trị";
                wbook = createActiveOrNewPatientsWorkbook(filter, listTitle, periodStr);
                break;
            case TRANSEDIN_PATIENT_REPORT:
                sheetName = "Bệnh nhân chuyển tới";
                listTitle = "Danh sách bệnh nhân được chuyển tới từ cơ sở điều trị khác";
                wbook = createActiveOrNewPatientsWorkbook(filter, listTitle, periodStr);
                break;
            case DEAD_LTFU_PATIENT_REPORT:
                sheetName = "Bệnh nhân tử vong, bỏ trị";
                listTitle = "Danh sách bệnh nhân tử vong, bỏ trị";
                wbook = createDeadLTFUPatientWorkbook(filter, listTitle, periodStr);
                break;
            case TRANSOUT_PATIENT_REPORT:
                sheetName = "Bệnh nhân chuyển đi";
                listTitle = "Danh sách bệnh nhân được chuyển tiếp điều trị";
                wbook = createTransedOutPatientWorkbook(filter, listTitle, periodStr);
                break;
            default:
                break;
        }

        wbook.setSheetName(0, sheetName);

        int indx = 0;

        if (orgs.size() > 1) {

            Sheet sheet = wbook.createSheet("Cơ sở báo cáo");

            ExcelUtils.createAndWriteInCell(
                    sheet, indx, 1, "Danh sách các cơ sở báo cáo", 22, 12, true);
            indx++;

            for (Organization org : orgs) {

                String s = indx + ". ";

                if (org.getAddress() != null && org.getAddress().getProvince() != null) {
                    s += org.getAddress().getProvince().getName();
                } else {
                    s += "Không có thông tin";
                }

                s += " - ";
                s += org.getName();

                ExcelUtils.createAndWriteInCell(sheet, indx, 1, s, 22, 12, false);
                indx++;
            }
        }

        return wbook;
    }

    /**
     * Create data for Ly on Sep 2021
     *
     * @return
     */
    private Workbook createLySep2021Workbook(CaseReportFilterDto filter) {
        // Loading the template
        Workbook wbook = null;

        try (InputStream template =
                context.getResource("classpath:templates/_ly-request-sep2021.xlsx")
                        .getInputStream()) {
            XSSFWorkbook tmp = new XSSFWorkbook(template);
            wbook = new SXSSFWorkbook(tmp, 100);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (wbook == null) {
            return blankBook;
        }

        // Patient sheet - Table content
        Font font = wbook.createFont();
        CellStyle cellStyle = wbook.createCellStyle();

        font.setFontName("Times New Roman");
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.BLACK.getIndex());

        cellStyle.setFont(font);
        cellStyle.setWrapText(true);
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setIndention((short) 1);
        ExcelUtils.setBorders4Style(cellStyle);

        CellStyle dateCellStyle = wbook.createCellStyle();
        dateCellStyle.cloneStyleFrom(cellStyle);

        DataFormat format = wbook.createDataFormat();
        dateCellStyle.setDataFormat(format.getFormat("dd/MM/yyyy"));

        LocalDateTime ldFromDate = LocalDateTime.of(2020, 07, 01, 0, 0, 0);
        LocalDateTime ldToDate = LocalDateTime.of(2021, 06, 30, 23, 59, 59);

        Timestamp fromDate = CommonUtils.toTimestamp(ldFromDate);
        Timestamp toDate = CommonUtils.toTimestamp(ldToDate);

        Integer rowIndex = 2;
        List<Long> caseIds = new ArrayList<>();
        Sheet sheet = wbook.getSheetAt(0);

        for (Long orgId : filter.getActualOrganizations()) {
            List<CaseOrg> caseOrgs = coRepos.findPartiallyActivePatients(orgId, toDate, false);
            List<CaseOrg> caseOrgs2 =
                    coRepos.findDeadAndLTFUPatients(
                            orgId, fromDate, toDate, Lists.newArrayList("LTFU", "DEAD"), false);
            List<CaseOrg> caseOrgs3 =
                    coRepos.findTransferredOutPatients_Circular03(orgId, fromDate, toDate, false);

            if (CommonUtils.isEmpty(caseOrgs)
                    && CommonUtils.isEmpty(caseOrgs2)
                    && CommonUtils.isEmpty(caseOrgs3)) {
                continue;
            }

            caseOrgs.addAll(caseOrgs2);
            caseOrgs.addAll(caseOrgs3);

            rowIndex =
                    _filloutLySep2021Patients(
                            filter,
                            rowIndex,
                            sheet,
                            cellStyle,
                            dateCellStyle,
                            caseOrgs,
                            caseIds,
                            toDate);
        }

        if (rowIndex >= 2) {
            sheet.setAutoFilter(CellRangeAddress.valueOf("A2:AG" + rowIndex));
        }

        // TLVR
        rowIndex = 1;
        sheet = wbook.getSheetAt(1);

        if (!caseIds.isEmpty()) {
            List<LabTest> tests = labRepos.findViralLoadLabTests4LySep2021(caseIds, toDate);
            for (LabTest test : tests) {
                Case theCase = test.getTheCase();

                int colIndex = 0;
                Row row = sheet.createRow(rowIndex++);
                row.setHeightInPoints(22);

                // Khóa chính bệnh nhân
                Cell cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(theCase.getId().toString());

                // Ngày lấy mẫu
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                cell.setCellValue(CommonUtils.fromLocalDateTime(test.getSampleDate()));

                // Ngày có kết quả
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                if (test.getResultDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(test.getResultDate()));
                } else {
                    cell.setCellValue("-");
                }

                // Kết quả (nồng độ)
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(cellStyle);
                if (CommonUtils.isPositive(test.getResultNumber(), false)) {
                    if (test.getResultNumber() == 0) {
                        cell.setCellValue(Constants.VL_RESULT_UNDECTECTED);
                    } else {
                        cell.setCellValue(test.getResultNumber());
                    }
                } else {
                    cell.setCellValue("-");
                }

                // Kết quả (nồng độ)
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (test.getResultText() != null) {
                    cell.setCellValue(test.getResultText());
                } else {
                    cell.setCellValue("-");
                }

                // Nguồn kinh phí
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (test.getFundingSource() != null) {
                    cell.setCellValue(test.getFundingSource().toString());
                } else {
                    cell.setCellValue("-");
                }

                // Lý do xét nghiệm
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (test.getReasonForTesting() != null) {
                    cell.setCellValue(test.getReasonForTesting().toString());
                } else {
                    cell.setCellValue("-");
                }
            }
        }

        if (rowIndex >= 1) {
            sheet.setAutoFilter(CellRangeAddress.valueOf("A1:AG" + rowIndex));
        }

        return wbook;
    }

    private Workbook createLySep2021_2_Workbook(CaseReportFilterDto filter) {
        // Loading the template
        Workbook wbook = null;

        try (InputStream template =
                context.getResource("classpath:templates/_ly-request-sep2021_2.xlsx")
                        .getInputStream()) {
            XSSFWorkbook tmp = new XSSFWorkbook(template);
            wbook = new SXSSFWorkbook(tmp, 100);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (wbook == null) {
            return blankBook;
        }

        // Patient sheet - Table content
        Font font = wbook.createFont();
        CellStyle cellStyle = wbook.createCellStyle();

        font.setFontName("Times New Roman");
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.BLACK.getIndex());

        cellStyle.setFont(font);
        cellStyle.setWrapText(true);
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setIndention((short) 1);
        ExcelUtils.setBorders4Style(cellStyle);

        CellStyle dateCellStyle = wbook.createCellStyle();
        dateCellStyle.cloneStyleFrom(cellStyle);

        DataFormat format = wbook.createDataFormat();
        dateCellStyle.setDataFormat(format.getFormat("dd/MM/yyyy"));

        Sheet sheet = wbook.getSheetAt(0);

        List<Case> cases = cRepos.findData4LySep2021_2();
        _filloutLySep2021_2_Patients(filter, sheet, cellStyle, dateCellStyle, cases);

        return wbook;
    }

    /**
     * Create data for MMD
     *
     * @param filter
     * @param periodStr
     * @return
     */
    private Workbook createMmdLinelist(CaseReportFilterDto filter, String periodStr) {
        Workbook wbook = null;
        try (InputStream template =
                context.getResource("classpath:templates/mmd-data-template.xlsx")
                        .getInputStream()) {

            XSSFWorkbook tmp = new XSSFWorkbook(template);
            // Write title and period information
            Sheet sheet = tmp.getSheetAt(0);

            ExcelUtils.createAndWriteInCell(sheet, 1, 1, periodStr, 22, 12, false);

            wbook = new SXSSFWorkbook(tmp, 100);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (wbook == null) {
            return blankBook;
        }

        Sheet sheet = wbook.getSheetAt(0);

        // Patient sheet - Table content
        Font font = wbook.createFont();
        CellStyle cellStyle = wbook.createCellStyle();

        font.setFontName("Times New Roman");
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.BLACK.getIndex());

        cellStyle.setFont(font);
        cellStyle.setWrapText(true);
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setIndention((short) 1);
        ExcelUtils.setBorders4Style(cellStyle);

        CellStyle dateCellStyle = wbook.createCellStyle();
        dateCellStyle.cloneStyleFrom(cellStyle);

        DataFormat format = wbook.createDataFormat();
        dateCellStyle.setDataFormat(format.getFormat("dd/MM/yyyy"));

        Integer rowIndex = 4;

        Timestamp atDate = CommonUtils.toTimestamp(filter.getAtDate());

        // Query for patients
        List<CaseOrg> caseOrgs1 =
                coRepos.findCases4MMDIndicators(filter.getOrganization(), 1, false, atDate);
        List<CaseOrg> caseOrgs2 =
                coRepos.findCases4MMDIndicators(filter.getOrganization(), 2, false, atDate);
        List<CaseOrg> caseOrgs3 =
                coRepos.findCases4MMDIndicators(filter.getOrganization(), 2, false, atDate);

        for (CaseOrg co : caseOrgs1) {
            boolean onMMD =
                    (caseOrgs2.stream()
                                    .filter(obj -> obj.getId() == co.getId().longValue())
                                    .findAny()
                                    .orElse(null))
                            != null;
            boolean onMMDSHI =
                    (caseOrgs3.stream()
                                    .filter(obj -> obj.getId() == co.getId().longValue())
                                    .findAny()
                                    .orElse(null))
                            != null;

            Organization org = co.getOrganization();
            Case theCase = co.getTheCase();
            Person person = theCase.getPerson();

            int colIndex = 0;
            Row row = sheet.createRow(rowIndex++);
            row.setHeightInPoints(22);

            // Khóa chính
            Cell cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(theCase.getId().toString());

            // Tỉnh - thành phố của cơ sở
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (org.getAddress() != null && org.getAddress().getProvince() != null) {
                cell.setCellValue(org.getAddress().getProvince().getName());
            }

            // Cơ sở điều trị
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (org != null) {
                cell.setCellValue(org.getName());
            }

            // Mã bệnh án tại cơ sở điều trị
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (co.getPatientChartId() != null) {
                cell.setCellValue(co.getPatientChartId());
            } else {
                cell.setCellValue("-");
            }

            // Họ tên bệnh nhân
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (person == null || CommonUtils.isEmpty(person.getFullname())) {
                cell.setCellValue("-");
            } else {
                cell.setCellValue(filter.isConfidentialRequired() ? "-" : person.getFullname());
            }

            // Giới tính
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (person == null || person.getGender() == null) {
                cell.setCellValue("-");
            } else {
                cell.setCellValue(person.getGender().toString());
            }

            // Ngày sinh
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (person != null && person.getDob() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(person.getDob()));
            } else {
                cell.setCellValue("-");
            }

            // Bệnh nhân ổn định
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue("x");

            // Bệnh nhân đang được cấp nhiều tháng
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (onMMD) {
                cell.setCellValue("x");
            } else {
                cell.setCellValue("");
            }

            // Cấp nhiều tháng qua nguồn bảo hiểm
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (onMMDSHI) {
                cell.setCellValue("x");
            } else {
                cell.setCellValue("");
            }

            // Addresses
            Location rAddress = null;
            Location cAddress = null;

            if (person != null) {
                Set<Location> locs = person.getLocations();
                for (Location loc : locs) {
                    if (loc == null) {
                        continue;
                    }

                    switch (loc.getAddressType()) {
                        case RESIDENT_ADDRESS:
                            rAddress = loc;
                            break;
                        case CURRENT_ADDRESS:
                            cAddress = loc;
                            break;
                        default:
                            break;
                    }
                }
            }

            // Residential address
            if (rAddress != null) {
                // R address - details
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (!CommonUtils.isEmpty(rAddress.getStreetAddress())) {
                    cell.setCellValue(
                            filter.isConfidentialRequired() ? "-" : rAddress.getStreetAddress());
                } else {
                    cell.setCellValue("-");
                }

                // R address - commune
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (rAddress.getCommune() != null) {
                    cell.setCellValue(
                            filter.isConfidentialRequired()
                                    ? "-"
                                    : rAddress.getCommune().getName());
                } else {
                    cell.setCellValue("-");
                }

                // R address - district
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (rAddress.getDistrict() != null) {
                    cell.setCellValue(rAddress.getDistrict().getName());
                } else {
                    cell.setCellValue("-");
                }

                // R address - province
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (rAddress.getProvince() != null) {
                    cell.setCellValue(rAddress.getProvince().getName());
                } else {
                    cell.setCellValue("-");
                }
            } else {
                // create empty residential address cells
                for (int i = 0; i < 4; i++) {
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue("-");
                }
            }

            // Current address
            if (cAddress != null) {
                // C address - details
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (!CommonUtils.isEmpty(cAddress.getStreetAddress())) {
                    cell.setCellValue(
                            filter.isConfidentialRequired() ? "-" : cAddress.getStreetAddress());
                } else {
                    cell.setCellValue("-");
                }

                // C address - commune
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (cAddress.getCommune() != null) {
                    cell.setCellValue(
                            filter.isConfidentialRequired()
                                    ? "-"
                                    : cAddress.getCommune().getName());
                } else {
                    cell.setCellValue("-");
                }

                // C address - district
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (cAddress.getDistrict() != null) {
                    cell.setCellValue(cAddress.getDistrict().getName());
                } else {
                    cell.setCellValue("-");
                }

                // C address - province
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (cAddress.getProvince() != null) {
                    cell.setCellValue(cAddress.getProvince().getName());
                } else {
                    cell.setCellValue("-");
                }
            } else {
                // create empty current address cells
                for (int i = 0; i < 4; i++) {
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue("-");
                }
            }

            // Ngày xét nghiệm sàng lọc
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(dateCellStyle);
            if (theCase.getHivScreenDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getHivScreenDate()));
            } else {
                cell.setCellValue("-");
            }

            // Ngày XN khẳng định
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (theCase.getHivConfirmDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getHivConfirmDate()));
            } else {
                cell.setCellValue("-");
            }

            // Ngày bắt đầu ARV
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (theCase.getArvStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getArvStartDate()));
            } else {
                cell.setCellValue("-");
            }

            // Ngày bắt đầu đợt điều trị trong kỳ báo cáo
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (co.getStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(co.getStartDate()));
            } else {
                cell.setCellValue("-");
            }

            // Loại đăng ký tại cơ sở trong kỳ báo cáo
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (co.getEnrollmentType() != null) {
                cell.setCellValue(co.getEnrollmentType().toString());
            } else {
                cell.setCellValue("-");
            }

            // Phác đồ thuốc hiện tại
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isEmpty(theCase.getCurrentArvRegimenName())) {
                cell.setCellValue(theCase.getCurrentArvRegimenName());
            } else {
                cell.setCellValue("-");
            }

            // Ngày bắt đầu phác đồ hiện tại
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (theCase.getCurrentArvRegimenStartDate() != null) {
                cell.setCellValue(
                        CommonUtils.fromLocalDateTime(theCase.getCurrentArvRegimenStartDate()));
            } else {
                cell.setCellValue("-");
            }

            // Ngày chuyển phác đồ bậc 2
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (theCase.getSecondLineStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getSecondLineStartDate()));
            } else {
                cell.setCellValue("-");
            }

            // Trạng thái điều trị của bệnh nhân tại cơ sở
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue((co.getStatus() != null) ? co.getStatus().toString() : "-");

            // Ngày thay đổi tình trạng
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (co.getStatus() == PatientStatus.ACTIVE) {
                if (co.getStartDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(co.getStartDate()));
                } else {
                    cell.setCellValue("-");
                }
            } else if (co.getEndDate() != null) {
                if (co.getEndDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(co.getEndDate()));
                } else {
                    cell.setCellValue("-");
                }
            }
        }

        // Hide ID columns
        sheet.setColumnHidden(0, true);

        // Auto-filter
        if (rowIndex >= 4) {
            sheet.setAutoFilter(CellRangeAddress.valueOf("A4:AB" + rowIndex));
        }

        return wbook;
    }

    /**
     * Create the summary in tabular format.
     *
     * @param filter
     * @return
     */
    private Workbook createTxSummaryWorkbook(CaseReportFilterDto filter) {
        // Validate
        if (filter.getFromDate() == null || filter.getToDate() == null) {
            return blankBook;
        }

        // Loading the template
        Workbook wbook = null;

        try (InputStream template =
                context.getResource("classpath:templates/tx-summary-patients-template.xlsx")
                        .getInputStream()) {

            XSSFWorkbook tmp = new XSSFWorkbook(template);
            // Write title and period information

            // Period
            Sheet sheet = tmp.getSheetAt(0);
            String periodStr = "Ngày chốt báo cáo: ";
            periodStr += filter.getToDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            ExcelUtils.createAndWriteInCell(sheet, 1, 1, periodStr, 22, 12, false);

            periodStr = "Giai đoạn báo cáo: ";
            periodStr += filter.getFromDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            periodStr += " - ";
            periodStr += filter.getToDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            for (int i = 1; i < 4; i++) {
                sheet = tmp.getSheetAt(i);
                ExcelUtils.createAndWriteInCell(sheet, 1, 1, periodStr, 22, 12, false);
            }

            wbook = new SXSSFWorkbook(tmp, 100);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (wbook == null || filter.getAlgorithm() == null) {
            return blankBook;
        }

        // Patient sheet - Table content
        Font font = wbook.createFont();
        CellStyle cellStyle = wbook.createCellStyle();

        font.setFontName("Times New Roman");
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.BLACK.getIndex());

        cellStyle.setFont(font);
        cellStyle.setWrapText(true);
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setIndention((short) 1);
        ExcelUtils.setBorders4Style(cellStyle);

        CellStyle dateCellStyle = wbook.createCellStyle();
        dateCellStyle.cloneStyleFrom(cellStyle);

        DataFormat format = wbook.createDataFormat();
        dateCellStyle.setDataFormat(format.getFormat("dd/MM/yyyy"));

        Timestamp fromDate = CommonUtils.toTimestamp(filter.getFromDate());
        Timestamp toDate = CommonUtils.toTimestamp(filter.getToDate());
        String toDateString = filter.getToDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        // 1. Fill out active patient sheet
        Integer rowIndex = 4;
        Sheet sheet = wbook.getSheetAt(0);

        for (Long orgId : filter.getActualOrganizations()) {
            List<CaseOrg> caseOrgs = coRepos.findPartiallyActivePatients(orgId, toDate, false);
            List<CaseOrg> caseOrgs2 = new ArrayList<>();

            if (filter.getAlgorithm() == ReportingAlgorithm.PEPFAR) {
                caseOrgs2 =
                        coRepos.findTransferredOutPendingPatients(orgId, fromDate, toDate, false);
            }

            if (CommonUtils.isEmpty(caseOrgs) && CommonUtils.isEmpty(caseOrgs2)) {
                continue;
            }

            rowIndex =
                    _filloutActiveOrNewPatients(
                            filter,
                            rowIndex,
                            sheet,
                            cellStyle,
                            dateCellStyle,
                            caseOrgs,
                            caseOrgs2,
                            toDateString,
                            false,
                            false);
        }

        if (rowIndex >= 4) {
            sheet.setAutoFilter(CellRangeAddress.valueOf("A4:AJ" + rowIndex));
        }

        // 2. Fill out new patients
        rowIndex = 4;
        sheet = wbook.getSheetAt(1);

        for (Long orgId : filter.getActualOrganizations()) {
            List<CaseOrg> caseOrgs =
                    coRepos.findPatientsByEnrollmentTypes(
                            orgId,
                            fromDate,
                            toDate,
                            Lists.newArrayList("NEWLY_ENROLLED", "RETURNED", "TRANSFERRED_IN"),
                            false);

            rowIndex =
                    _filloutActiveOrNewPatients(
                            filter,
                            rowIndex,
                            sheet,
                            cellStyle,
                            dateCellStyle,
                            caseOrgs,
                            null,
                            null,
                            false,
                            false);
        }

        if (rowIndex >= 4) {
            sheet.setAutoFilter(CellRangeAddress.valueOf("A4:AJ" + rowIndex));
        }

        // 3. Fill out transferred-out patients
        rowIndex = 4;
        sheet = wbook.getSheetAt(2);

        for (Long orgId : filter.getActualOrganizations()) {
            List<CaseOrg> caseOrgs = null;

            if (filter.getAlgorithm() == ReportingAlgorithm.PEPFAR) {
                caseOrgs = coRepos.findTransferredOutPatients(orgId, fromDate, toDate, false);

                // get transferred out pending patients from previous month
                LocalDateTime lastMonthBegin =
                        filter.getFromDate().withDayOfMonth(15).minusMonths(1).withDayOfMonth(1);
                LocalDate lastMonthDate = lastMonthBegin.toLocalDate();
                LocalDateTime lastMonthEnd =
                        CommonUtils.dateEnd(
                                lastMonthBegin.withDayOfMonth(
                                        lastMonthDate
                                                .getMonth()
                                                .length(lastMonthDate.isLeapYear())));

                Timestamp lastFromDate = CommonUtils.toTimestamp(lastMonthBegin);
                Timestamp lastToDate = CommonUtils.toTimestamp(lastMonthEnd);

                List<CaseOrg> cos1 =
                        coRepos.findTransferredOutCompensate(
                                filter.getOrganization(),
                                lastFromDate,
                                lastToDate,
                                fromDate,
                                toDate,
                                false);

                if (cos1.size() > 0) {
                    caseOrgs.addAll(cos1);
                }
            } else {
                caseOrgs =
                        coRepos.findTransferredOutPatients_Circular03(
                                orgId, fromDate, toDate, false);
            }

            if (CommonUtils.isEmpty(caseOrgs)) {
                continue;
            }

            rowIndex =
                    _filloutTransedOutPatients(
                            filter, rowIndex, sheet, cellStyle, dateCellStyle, caseOrgs);
        }

        if (rowIndex >= 4) {
            sheet.setAutoFilter(CellRangeAddress.valueOf("A4:AJ" + rowIndex));
        }

        // 4. Fill out dead & ltfu patients
        rowIndex = 4;
        sheet = wbook.getSheetAt(3);

        for (Long orgId : filter.getActualOrganizations()) {
            List<CaseOrg> caseOrgs =
                    coRepos.findDeadAndLTFUPatients(
                            orgId, fromDate, toDate, Lists.newArrayList("DEAD", "LTFU"), false);

            if (CommonUtils.isEmpty(caseOrgs)) {
                continue;
            }

            rowIndex =
                    _filloutDeadLTFUPatients(
                            filter, rowIndex, sheet, cellStyle, dateCellStyle, caseOrgs);
        }

        if (rowIndex >= 4) {
            sheet.setAutoFilter(CellRangeAddress.valueOf("A4:AI" + rowIndex));
        }

        // Hide ID columns
        for (int i = 0; i < 4; i++) {
            sheet = wbook.getSheetAt(i);

            sheet.setColumnHidden(0, true);
            sheet.setColumnHidden(3, true);
        }

        return wbook;
    }

    /**
     * Create new/active patient workbook
     *
     * @param filter
     * @return
     */
    private Workbook createActiveOrNewPatientsWorkbook(
            CaseReportFilterDto filter, String listTitle, String periodStr) {
        Workbook wbook = null;

        boolean includeExtra = CommonUtils.isTrue(filter.getIncludeExtra());
        boolean includeAppInfo = CommonUtils.isTrue(filter.getIncludeAppointmentInfo());

        String filename = "classpath:templates/new-active-patients-template.xlsx";
        if (includeExtra) {
            filename = "classpath:templates/active-wextra-patients-template.xlsx";
        } else if (includeAppInfo) {
            filename = "classpath:templates/active-wapp-patients-template.xlsx";
        }

        try (InputStream template = context.getResource(filename).getInputStream()) {

            XSSFWorkbook tmp = new XSSFWorkbook(template);
            // Write title and period information

            Sheet sheet = tmp.getSheetAt(0);

            ExcelUtils.createAndWriteInCell(sheet, 0, 1, listTitle, 30, 16, true);
            ExcelUtils.createAndWriteInCell(sheet, 1, 1, periodStr, 22, 12, false);

            wbook = new SXSSFWorkbook(tmp, 100);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (wbook == null) {
            return blankBook;
        }

        Sheet sheet = wbook.getSheetAt(0);

        // Patient sheet - Table content
        Font font = wbook.createFont();
        CellStyle cellStyle = wbook.createCellStyle();

        font.setFontName("Times New Roman");
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.BLACK.getIndex());

        cellStyle.setFont(font);
        cellStyle.setWrapText(true);
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setIndention((short) 1);
        ExcelUtils.setBorders4Style(cellStyle);

        CellStyle dateCellStyle = wbook.createCellStyle();
        dateCellStyle.cloneStyleFrom(cellStyle);

        DataFormat format = wbook.createDataFormat();
        dateCellStyle.setDataFormat(format.getFormat("dd/MM/yyyy"));

        // algorithm
        ReportingAlgorithm algorithm = filter.getAlgorithm();
        if (filter.getReportType() == ReportType.ACTIVE_PATIENT_REPORT && algorithm == null) {
            return blankBook;
        }

        Integer rowIndex = 4;

        // Query for patients
        List<CaseOrg> caseOrgs = null;
        List<CaseOrg> caseOrgs2 = null;

        Timestamp fromDate = null;
        Timestamp toDate = null;
        String toDateString = "-";

        if (filter.getReportType() == ReportType.ACTIVE_PATIENT_REPORT) {
            fromDate =
                    CommonUtils.toTimestamp(
                            CommonUtils.dateStart(filter.getAtDate().minusMonths(1)));
            toDate = CommonUtils.toTimestamp(filter.getAtDate());
            toDateString = filter.getAtDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } else {
            fromDate = CommonUtils.toTimestamp(filter.getFromDate());
            toDate = CommonUtils.toTimestamp(filter.getToDate());
        }

        for (Long orgId : filter.getActualOrganizations()) {

            caseOrgs = new ArrayList<>();

            switch (filter.getReportType()) {
                case ACTIVE_PATIENT_REPORT:
                    caseOrgs = coRepos.findPartiallyActivePatients(orgId, toDate, false);
                    if (algorithm == ReportingAlgorithm.PEPFAR) {
                        caseOrgs2 =
                                coRepos.findTransferredOutPendingPatients(
                                        orgId, fromDate, toDate, false);
                    } else {
                        // Circular 03 -- don't need to care about transferred out patients
                        caseOrgs2 = new ArrayList<>();
                    }

                    break;
                case NEW_PATIENT_REPORT:
                    caseOrgs =
                            coRepos.findPatientsByEnrollmentTypes(
                                    orgId,
                                    fromDate,
                                    toDate,
                                    Lists.newArrayList(
                                            "NEWLY_ENROLLED", "RETURNED", "TRANSFERRED_IN"),
                                    false);
                    break;
                case NEWLY_ENROLLED_PATIENT_REPORT:
                    caseOrgs =
                            coRepos.findPatientsByEnrollmentTypes(
                                    orgId,
                                    fromDate,
                                    toDate,
                                    Lists.newArrayList("NEWLY_ENROLLED"),
                                    false);
                    break;
                case RETURNED_PATIENT_REPORT:
                    caseOrgs =
                            coRepos.findPatientsByEnrollmentTypes(
                                    orgId, fromDate, toDate, Lists.newArrayList("RETURNED"), false);
                    break;
                case TRANSEDIN_PATIENT_REPORT:
                    caseOrgs =
                            coRepos.findPatientsByEnrollmentTypes(
                                    orgId,
                                    fromDate,
                                    toDate,
                                    Lists.newArrayList("TRANSFERRED_IN"),
                                    false);
                    break;
                default:
                    break;
            }

            if (CommonUtils.isEmpty(caseOrgs) && CommonUtils.isEmpty(caseOrgs2)) {
                continue;
            }

            // fill out sheet
            rowIndex =
                    _filloutActiveOrNewPatients(
                            filter,
                            rowIndex,
                            sheet,
                            cellStyle,
                            dateCellStyle,
                            caseOrgs,
                            caseOrgs2,
                            toDateString,
                            includeExtra,
                            includeAppInfo);
        }

        // Hide ID columns
        sheet.setColumnHidden(0, true);
        sheet.setColumnHidden(3, true);

        // Auto-filter
        if (rowIndex >= 4) {
            if (includeAppInfo) {
                sheet.setAutoFilter(CellRangeAddress.valueOf("A4:AM" + rowIndex));
            } else if (includeExtra) {
                sheet.setAutoFilter(CellRangeAddress.valueOf("A4:AX" + rowIndex));
            } else {
                sheet.setAutoFilter(CellRangeAddress.valueOf("A4:AJ" + rowIndex));
            }
        }

        return wbook;
    }

    /**
     * Fill out active/new patient sheets
     *
     * @param filter
     * @param rowIndex
     * @param sheet
     * @param cellStyle
     * @param dateCellStyle
     * @param caseOrgs
     * @param caseOrgs2
     * @param toDateString
     */
    @SuppressWarnings("unchecked")
    private Integer _filloutActiveOrNewPatients(
            CaseReportFilterDto filter,
            int rowIndex,
            Sheet sheet,
            CellStyle cellStyle,
            CellStyle dateCellStyle,
            List<CaseOrg> caseOrgs,
            List<CaseOrg> caseOrgs2,
            String toDateString,
            boolean includeExtra,
            boolean includeAppInfo) {

        // Create the array of two
        if (caseOrgs2 == null) {
            caseOrgs2 = new ArrayList<>();
        }

        Timestamp cutpoint = CommonUtils.toTimestamp(filter.getAtDate());

        List<List<CaseOrg>> lists = Lists.newArrayList(caseOrgs, caseOrgs2);
        String[] notes = {
            "-",
            "Bệnh nhân đã chuyển đi nhưng chưa được tiếp nhận, và số thuốc cấp ở lần khám cuối có thể dùng tới sau "
                    + toDateString
        };

        int indx = 0;

        int count = 0;
        int max = caseOrgs.size() + caseOrgs2.size();

        for (List<CaseOrg> list : lists) {

            // Start filling out data...
            for (CaseOrg co : list) {
                Organization org = co.getOrganization();
                Case theCase = co.getTheCase();
                Person person = theCase.getPerson();

                int colIndex = 0;
                Row row = sheet.createRow(rowIndex++);
                row.setHeightInPoints(22);

                // Khóa chính
                Cell cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(theCase.getId().toString());

                // Tỉnh - thành phố của cơ sở
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);

                CaseOrg prevCaseOrg = null;
                List<CaseOrg> cos = Lists.newArrayList(theCase.getCaseOrgs());

                int i = 0;
                for (CaseOrg _co : cos) {

                    // prev org
                    if (_co.getId() == co.getId().longValue() && i < cos.size() - 1) {
                        for (int j = i + 1; j < cos.size(); j++) {
                            prevCaseOrg = cos.get(j);
                            if (prevCaseOrg.getStatus() != PatientStatus.CANCELLED_ENROLLMENT
                                    && prevCaseOrg.getStatus()
                                            != PatientStatus.PENDING_ENROLLMENT) {
                                break;
                            }
                        }
                    }

                    i++;
                }

                if (org.getAddress() != null && org.getAddress().getProvince() != null) {
                    cell.setCellValue(org.getAddress().getProvince().getName());
                }

                // Cơ sở điều trị
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (org != null) {
                    cell.setCellValue(org.getName());
                }

                // Mã bệnh nhân duy nhất
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);

                if (theCase.getNationalHealthId() != null) {
                    cell.setCellValue(theCase.getNationalHealthId());
                } else {
                    cell.setCellValue("-");
                }

                // Mã HIVInfo
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (theCase.getHivInfoID() != null) {
                    cell.setCellValue(theCase.getHivInfoID());
                } else {
                    cell.setCellValue("-");
                }

                // Mã bệnh án tại cơ sở điều trị
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (co.getPatientChartId() != null) {
                    cell.setCellValue(co.getPatientChartId());
                } else {
                    cell.setCellValue("-");
                }

                // Họ tên bệnh nhân
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (person == null || CommonUtils.isEmpty(person.getFullname())) {
                    cell.setCellValue("-");
                } else {
                    cell.setCellValue(filter.isConfidentialRequired() ? "-" : person.getFullname());
                }

                // Giới tính
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (person == null || person.getGender() == null) {
                    cell.setCellValue("-");
                } else {
                    cell.setCellValue(person.getGender().toString());
                }

                // Ngày sinh
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                if (person != null && person.getDob() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(person.getDob()));
                } else {
                    cell.setCellValue("-");
                }

                // Số CMTND
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (person != null && person.getNidNumber() != null) {
                    cell.setCellValue(
                            filter.isConfidentialRequired() ? "-" : person.getNidNumber());
                } else {
                    cell.setCellValue("-");
                }

                // Số ho chieu
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (person != null && person.getPassportNumber() != null) {
                    cell.setCellValue(
                            filter.isConfidentialRequired() ? "-" : person.getPassportNumber());
                } else {
                    cell.setCellValue("-");
                }

                // Nghề nghiệp
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (!CommonUtils.isEmpty(person.getOccupationName(), true)) {
                    cell.setCellValue(person.getOccupationName());
                } else {
                    cell.setCellValue("-");
                }

                // Dân tộc
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (person.getEthnic() != null) {
                    cell.setCellValue(person.getEthnic().getValue());
                } else {
                    cell.setCellValue("-");
                }

                // Số ĐT liên lạc
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                String phone = "";
                if (!CommonUtils.isEmpty(person.getHomePhone(), true)) {
                    phone += person.getHomePhone();
                    phone += "/ ";
                }

                if (!CommonUtils.isEmpty(person.getMobilePhone(), true)) {
                    phone += person.getMobilePhone();
                }

                if (phone.isEmpty()) {
                    phone = "-";
                } else if (phone.endsWith("/ ")) {
                    phone = phone.substring(1, phone.length() - 2);
                }
                cell.setCellValue(phone);

                // Addresses
                Location rAddress = null;
                Location cAddress = null;

                if (person != null) {
                    Set<Location> locs = person.getLocations();
                    for (Location loc : locs) {
                        if (loc == null) {
                            continue;
                        }

                        switch (loc.getAddressType()) {
                            case RESIDENT_ADDRESS:
                                rAddress = loc;
                                break;
                            case CURRENT_ADDRESS:
                                cAddress = loc;
                                break;
                            default:
                                break;
                        }
                    }
                }

                // Residential address
                if (rAddress != null) {
                    // R address - details
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    if (!CommonUtils.isEmpty(rAddress.getStreetAddress())) {
                        cell.setCellValue(
                                filter.isConfidentialRequired()
                                        ? "-"
                                        : rAddress.getStreetAddress());
                    } else {
                        cell.setCellValue("-");
                    }

                    // R address - commune
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    if (rAddress.getCommune() != null) {
                        cell.setCellValue(
                                filter.isConfidentialRequired()
                                        ? "-"
                                        : rAddress.getCommune().getName());
                    } else {
                        cell.setCellValue("-");
                    }

                    // R address - district
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    if (rAddress.getDistrict() != null) {
                        cell.setCellValue(rAddress.getDistrict().getName());
                    } else {
                        cell.setCellValue("-");
                    }

                    // R address - province
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    if (rAddress.getProvince() != null) {
                        cell.setCellValue(rAddress.getProvince().getName());
                    } else {
                        cell.setCellValue("-");
                    }
                } else {
                    // create empty residential address cells
                    for (i = 0; i < 4; i++) {
                        cell = row.createCell(colIndex++, CellType.STRING);
                        cell.setCellStyle(cellStyle);
                        cell.setCellValue("-");
                    }
                }

                // Current address
                if (cAddress != null) {
                    // C address - details
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    if (!CommonUtils.isEmpty(cAddress.getStreetAddress())) {
                        cell.setCellValue(
                                filter.isConfidentialRequired()
                                        ? "-"
                                        : cAddress.getStreetAddress());
                    } else {
                        cell.setCellValue("-");
                    }

                    // C address - commune
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    if (cAddress.getCommune() != null) {
                        cell.setCellValue(
                                filter.isConfidentialRequired()
                                        ? "-"
                                        : cAddress.getCommune().getName());
                    } else {
                        cell.setCellValue("-");
                    }

                    // C address - district
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    if (cAddress.getDistrict() != null) {
                        cell.setCellValue(cAddress.getDistrict().getName());
                    } else {
                        cell.setCellValue("-");
                    }

                    // C address - province
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    if (cAddress.getProvince() != null) {
                        cell.setCellValue(cAddress.getProvince().getName());
                    } else {
                        cell.setCellValue("-");
                    }
                } else {
                    // create empty current address cells
                    for (i = 0; i < 4; i++) {
                        cell = row.createCell(colIndex++, CellType.STRING);
                        cell.setCellStyle(cellStyle);
                        cell.setCellValue("-");
                    }
                }

                // Ngày xét nghiệm sàng lọc
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(dateCellStyle);
                if (theCase.getHivScreenDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getHivScreenDate()));
                } else {
                    cell.setCellValue("-");
                }

                // Ngày XN khẳng định
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                if (theCase.getHivConfirmDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getHivConfirmDate()));
                } else {
                    cell.setCellValue("-");
                }

                // Cơ sở XN khẳng định
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(
                        !CommonUtils.isEmpty(theCase.getConfirmLabName())
                                ? theCase.getConfirmLabName()
                                : "-");

                // Ngày bắt đầu ARV
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                if (theCase.getArvStartDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getArvStartDate()));
                } else {
                    cell.setCellValue("-");
                }

                // Ngày bắt đầu đợt điều trị trong kỳ báo cáo
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                if (co.getStartDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(co.getStartDate()));
                } else {
                    cell.setCellValue("-");
                }

                // Loại đăng ký tại cơ sở trong kỳ báo cáo
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (co.getEnrollmentType() != null) {
                    cell.setCellValue(co.getEnrollmentType().toString());
                } else {
                    cell.setCellValue("-");
                }

                // Nhóm điều trị ARV
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (!CommonUtils.isEmpty(co.getArvGroup())) {
                    cell.setCellValue(co.getArvGroup());
                } else {
                    cell.setCellValue("-");
                }

                // Phác đồ thuốc hiện tại
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (!CommonUtils.isEmpty(theCase.getCurrentArvRegimenName())) {
                    cell.setCellValue(theCase.getCurrentArvRegimenName());
                } else {
                    cell.setCellValue("-");
                }

                // Ngày bắt đầu phác đồ hiện tại
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                if (theCase.getCurrentArvRegimenStartDate() != null) {
                    cell.setCellValue(
                            CommonUtils.fromLocalDateTime(theCase.getCurrentArvRegimenStartDate()));
                } else {
                    cell.setCellValue("-");
                }

                // Ngày chuyển phác đồ bậc 2
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                if (theCase.getSecondLineStartDate() != null) {
                    cell.setCellValue(
                            CommonUtils.fromLocalDateTime(theCase.getSecondLineStartDate()));
                } else {
                    cell.setCellValue("-");
                }

                // Cơ sở điều trị trước đó
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (prevCaseOrg != null && prevCaseOrg.getOrganization() != null) {
                    if (Constants.CODE_ORGANIZATION_OTHER.equalsIgnoreCase(
                            prevCaseOrg.getOrganization().getCode())) {
                        cell.setCellValue(prevCaseOrg.getOrganizationName());
                    } else {
                        cell.setCellValue(prevCaseOrg.getOrganization().getName());
                    }
                } else {
                    cell.setCellValue("-");
                }

                // Trạng thái điều trị của bệnh nhân tại cơ sở
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue((co.getStatus() != null) ? co.getStatus().toString() : "-");

                // Ngày thay đổi tình trạng
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                if (co.getStatus() == PatientStatus.ACTIVE) {
                    if (co.getStartDate() != null) {
                        cell.setCellValue(CommonUtils.fromLocalDateTime(co.getStartDate()));
                    } else {
                        cell.setCellValue("-");
                    }
                } else if (co.getEndDate() != null) {
                    if (co.getEndDate() != null) {
                        cell.setCellValue(CommonUtils.fromLocalDateTime(co.getEndDate()));
                    } else {
                        cell.setCellValue("-");
                    }
                }

                // Ghi chú
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(notes[indx]);

                // Extra columns
                if (includeAppInfo) {
                    filloutAppointmentData(org, theCase, row, colIndex, cellStyle, dateCellStyle);
                } else if (includeExtra) {
                    filloutExtra(theCase, cutpoint, row, colIndex, cellStyle, dateCellStyle);
                    System.out.println("Processed " + (++count) + "/" + max + " records.");
                }
            }

            indx++;
        }

        return rowIndex;
    }

    /**
     * Create dead/ltfu patient workbook
     *
     * @param filter
     * @return
     */
    private Workbook createDeadLTFUPatientWorkbook(
            CaseReportFilterDto filter, String listTitle, String periodStr) {
        Workbook wbook = null;
        boolean includeExtra = CommonUtils.isTrue(filter.getIncludeExtra());
        boolean includeAppInfo = CommonUtils.isTrue(filter.getIncludeAppointmentInfo());

        String filename = "classpath:templates/dead-ltfu-patients-template.xlsx";
        if (includeAppInfo) {
            filename = "classpath:templates/dead-ltfu-wapp-patients-template.xlsx";
        } else if (includeExtra) {
            filename = "classpath:templates/dead-ltfu-wextra-patients-template.xlsx";
        }

        try (InputStream template = context.getResource(filename).getInputStream()) {

            XSSFWorkbook tmp = new XSSFWorkbook(template);
            // Write title and period information
            Sheet sheet = tmp.getSheetAt(0);

            ExcelUtils.createAndWriteInCell(sheet, 0, 1, listTitle, 30, 16, true);
            ExcelUtils.createAndWriteInCell(sheet, 1, 1, periodStr, 22, 12, false);

            wbook = new SXSSFWorkbook(tmp, 100);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (wbook == null) {
            return blankBook;
        }

        Sheet sheet = wbook.getSheetAt(0);

        // Patient sheet - Table content
        Font font = wbook.createFont();
        CellStyle cellStyle = wbook.createCellStyle();

        font.setFontName("Times New Roman");
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.BLACK.getIndex());

        cellStyle.setFont(font);
        cellStyle.setWrapText(true);
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setIndention((short) 1);
        ExcelUtils.setBorders4Style(cellStyle);

        CellStyle dateCellStyle = wbook.createCellStyle();
        dateCellStyle.cloneStyleFrom(cellStyle);

        DataFormat format = wbook.createDataFormat();
        dateCellStyle.setDataFormat(format.getFormat("dd/MM/yyyy"));

        Integer rowIndex = 4;

        Timestamp fromDate = CommonUtils.toTimestamp(filter.getFromDate());
        Timestamp toDate = CommonUtils.toTimestamp(filter.getToDate());

        // Query for patients
        List<CaseOrg> caseOrgs = null;

        for (Long orgId : filter.getActualOrganizations()) {

            caseOrgs =
                    coRepos.findDeadAndLTFUPatients(
                            orgId, fromDate, toDate, Lists.newArrayList("DEAD", "LTFU"), false);

            if (CommonUtils.isEmpty(caseOrgs)) {
                continue;
            }

            // Start filling out data...
            rowIndex =
                    _filloutDeadLTFUPatients(
                            filter, rowIndex, sheet, cellStyle, dateCellStyle, caseOrgs);
        }

        // Hide ID columns
        sheet.setColumnHidden(0, true);
        sheet.setColumnHidden(3, true);

        // Auto-filter
        if (rowIndex >= 4) {
            if (includeAppInfo) {
                sheet.setAutoFilter(CellRangeAddress.valueOf("A4:AL" + rowIndex));
            } else if (includeExtra) {
                sheet.setAutoFilter(CellRangeAddress.valueOf("A4:AW" + rowIndex));
            } else {
                sheet.setAutoFilter(CellRangeAddress.valueOf("A4:AI" + rowIndex));
            }
        }

        return wbook;
    }

    /**
     * @param filter
     * @param rowIndex
     * @param sheet
     * @param cellStyle
     * @param dateCellStyle
     * @param caseOrgs
     */
    private Integer _filloutDeadLTFUPatients(
            CaseReportFilterDto filter,
            int rowIndex,
            Sheet sheet,
            CellStyle cellStyle,
            CellStyle dateCellStyle,
            List<CaseOrg> caseOrgs) {

        Timestamp cutpoint = CommonUtils.toTimestamp(filter.getToDate());
        boolean includeExtra = CommonUtils.boolValue(filter.getIncludeExtra());
        boolean includeAppInfo = CommonUtils.boolValue(filter.getIncludeAppointmentInfo());

        for (CaseOrg co : caseOrgs) {

            Organization org = co.getOrganization();
            Case theCase = co.getTheCase();
            Person person = theCase.getPerson();

            int colIndex = 0;
            Row row = sheet.createRow(rowIndex++);
            row.setHeightInPoints(22);

            // Khóa chính
            Cell cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(theCase.getId().toString());

            // Tỉnh - thành phố của cơ sở
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (org.getAddress() != null && org.getAddress().getProvince() != null) {
                cell.setCellValue(org.getAddress().getProvince().getName());
            }

            // Cơ sở điều trị
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isEmpty(org.getName(), true)) {
                cell.setCellValue(org.getName());
            } else {
                cell.setCellValue("-");
            }

            // Mã bệnh nhân duy nhất
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);

            if (theCase.getNationalHealthId() != null) {
                cell.setCellValue(theCase.getNationalHealthId());
            } else {
                cell.setCellValue("-");
            }

            // Mã HIVInfo
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (theCase.getHivInfoID() != null) {
                cell.setCellValue(theCase.getHivInfoID());
            } else {
                cell.setCellValue("-");
            }

            // Mã bệnh án tại cơ sở điều trị
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (co.getPatientChartId() != null) {
                cell.setCellValue(co.getPatientChartId());
            } else {
                cell.setCellValue("-");
            }

            // Họ tên bệnh nhân
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (person == null || CommonUtils.isEmpty(person.getFullname())) {
                cell.setCellValue("-");
            } else {
                cell.setCellValue(filter.isConfidentialRequired() ? "-" : person.getFullname());
            }

            // Giới tính
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (person == null || person.getGender() == null) {
                cell.setCellValue("-");
            } else {
                cell.setCellValue(person.getGender().toString());
            }

            // Ngày sinh
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            if (person != null && person.getDob() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(person.getDob()));
            } else {
                cell.setCellValue("-");
            }
            cell.setCellStyle(dateCellStyle);

            // Số CMTND
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (person != null && person.getNidNumber() != null) {
                cell.setCellValue(filter.isConfidentialRequired() ? "-" : person.getNidNumber());
            } else {
                cell.setCellValue("-");
            }

            // Số passport
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (person != null && person.getPassportNumber() != null) {
                cell.setCellValue(
                        filter.isConfidentialRequired() ? "-" : person.getPassportNumber());
            } else {
                cell.setCellValue("-");
            }

            // Nghề nghiệp
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isEmpty(person.getOccupationName(), true)) {
                cell.setCellValue(person.getOccupationName());
            } else {
                cell.setCellValue("-");
            }

            // Dân tộc
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (person.getEthnic() != null) {
                cell.setCellValue(person.getEthnic().getValue());
            } else {
                cell.setCellValue("-");
            }

            // Số ĐT liên lạc
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            String phone = "";
            if (!CommonUtils.isEmpty(person.getHomePhone(), true)) {
                phone += person.getHomePhone();
                phone += "/ ";
            }

            if (!CommonUtils.isEmpty(person.getMobilePhone(), true)) {
                phone += person.getMobilePhone();
            }

            if (phone.isEmpty()) {
                phone = "-";
            } else if (phone.endsWith("/ ")) {
                phone = phone.substring(1, phone.length() - 2);
            }
            cell.setCellValue(phone);

            // Addresses
            Location rAddress = null;
            Location cAddress = null;

            if (person != null) {
                Set<Location> locs = person.getLocations();
                for (Location loc : locs) {
                    if (loc == null) {
                        continue;
                    }

                    switch (loc.getAddressType()) {
                        case RESIDENT_ADDRESS:
                            rAddress = loc;
                            break;
                        case CURRENT_ADDRESS:
                            cAddress = loc;
                            break;
                        default:
                            break;
                    }
                }
            }

            // Residential address
            if (rAddress != null) {
                // R address - details
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (!CommonUtils.isEmpty(rAddress.getStreetAddress())) {
                    cell.setCellValue(
                            filter.isConfidentialRequired() ? "-" : rAddress.getStreetAddress());
                } else {
                    cell.setCellValue("-");
                }

                // R address - commune
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (rAddress.getCommune() != null) {
                    cell.setCellValue(
                            filter.isConfidentialRequired()
                                    ? "-"
                                    : rAddress.getCommune().getName());
                } else {
                    cell.setCellValue("-");
                }

                // R address - district
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (rAddress.getDistrict() != null) {
                    cell.setCellValue(rAddress.getDistrict().getName());
                } else {
                    cell.setCellValue("-");
                }

                // R address - province
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (rAddress.getProvince() != null) {
                    cell.setCellValue(rAddress.getProvince().getName());
                } else {
                    cell.setCellValue("-");
                }
            } else {
                // create empty residential address cells
                for (int i = 0; i < 4; i++) {
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue("-");
                }
            }

            // Current address
            if (cAddress != null) {
                // C address - details
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (!CommonUtils.isEmpty(cAddress.getStreetAddress())) {
                    cell.setCellValue(
                            filter.isConfidentialRequired() ? "-" : cAddress.getStreetAddress());
                } else {
                    cell.setCellValue("-");
                }

                // C address - commune
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (cAddress.getCommune() != null) {
                    cell.setCellValue(
                            filter.isConfidentialRequired()
                                    ? "-"
                                    : cAddress.getCommune().getName());
                } else {
                    cell.setCellValue("-");
                }

                // C address - district
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (cAddress.getDistrict() != null) {
                    cell.setCellValue(cAddress.getDistrict().getName());
                } else {
                    cell.setCellValue("-");
                }

                // C address - province
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (cAddress.getProvince() != null) {
                    cell.setCellValue(cAddress.getProvince().getName());
                } else {
                    cell.setCellValue("-");
                }
            } else {
                // create empty current address cells
                for (int i = 0; i < 4; i++) {
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue("-");
                }
            }

            // Ngày xét nghiệm sàng lọc
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(dateCellStyle);
            if (theCase.getHivScreenDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getHivScreenDate()));
            } else {
                cell.setCellValue("-");
            }

            // Ngày XN khẳng định
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (theCase.getHivConfirmDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getHivConfirmDate()));
            } else {
                cell.setCellValue("-");
            }

            // Cơ sở XN khẳng định
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(
                    !CommonUtils.isEmpty(theCase.getConfirmLabName())
                            ? theCase.getConfirmLabName()
                            : "-");

            // Ngày bắt đầu ARV
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (theCase.getArvStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getArvStartDate()));
            } else {
                cell.setCellValue("-");
            }

            // Ngày bắt đầu đợt đăng ký tại cơ sở trong kỳ báo cáo
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (co.getStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(co.getStartDate()));
            } else {
                cell.setCellValue("-");
            }

            // Loại đăng ký tại cơ sở trong kỳ báo cáo
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (co.getEnrollmentType() != null) {
                cell.setCellValue(co.getEnrollmentType().toString());
            } else {
                cell.setCellValue("-");
            }

            // Nhóm điều trị ARV tại cơ sở
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (co.getArvGroup() != null) {
                cell.setCellValue(co.getArvGroup());
            } else {
                cell.setCellValue("-");
            }

            // Phác đồ thuốc hiện tại
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(
                    !CommonUtils.isEmpty(theCase.getCurrentArvRegimenName())
                            ? theCase.getCurrentArvRegimenName()
                            : "-");

            // Ngày bắt đầu phác đồ hiện tại
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (theCase.getCurrentArvRegimenStartDate() != null) {
                cell.setCellValue(
                        CommonUtils.fromLocalDateTime(theCase.getCurrentArvRegimenStartDate()));
            } else {
                cell.setCellValue("-");
            }

            // Ngày chuyển phác đồ bậc 2
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (theCase.getSecondLineStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getSecondLineStartDate()));
            } else {
                cell.setCellValue("-");
            }

            // Trạng thái điều trị của bệnh nhân tại cơ sở
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue((co.getStatus() != null) ? co.getStatus().toString() : "-");

            // Ngày thay đổi tình trạng
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (co.getStatus() == PatientStatus.ACTIVE) {
                if (co.getStartDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(co.getStartDate()));
                } else {
                    cell.setCellValue("-");
                }
            } else if (co.getEndDate() != null) {
                if (co.getEndDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(co.getEndDate()));
                } else {
                    cell.setCellValue("-");
                }
            }

            // Lý do thay đổi trạng thái điều trị
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(
                    !CommonUtils.isEmpty(co.getEndingReason()) ? co.getEndingReason() : "-");

            // Extra columns
            if (includeAppInfo) {
                filloutAppointmentData(org, theCase, row, colIndex, cellStyle, dateCellStyle);
            } else if (includeExtra) {
                filloutExtra(theCase, cutpoint, row, colIndex, cellStyle, dateCellStyle);
            }
        }

        return rowIndex;
    }

    /**
     * Create transed-out patient workbook
     *
     * @param filter
     * @return
     */
    private Workbook createTransedOutPatientWorkbook(
            CaseReportFilterDto filter, String listTitle, String periodStr) {
        Workbook wbook = null;
        boolean includeExtra = CommonUtils.isTrue(filter.getIncludeExtra());
        boolean includeAppInfo = CommonUtils.isTrue(filter.getIncludeAppointmentInfo());

        String filename = "classpath:templates/transedout-patients-template.xlsx";
        if (includeAppInfo) {
            filename = "classpath:templates/transedout-wapp-patients-template.xlsx";
        } else if (includeExtra) {
            filename = "classpath:templates/transedout-wextra-patients-template.xlsx";
        }

        try (InputStream template = context.getResource(filename).getInputStream()) {

            XSSFWorkbook tmp = new XSSFWorkbook(template);
            // Write title and period information
            Sheet sheet = tmp.getSheetAt(0);

            ExcelUtils.createAndWriteInCell(sheet, 0, 1, listTitle, 30, 16, true);
            ExcelUtils.createAndWriteInCell(sheet, 1, 1, periodStr, 22, 12, false);

            wbook = new SXSSFWorkbook(tmp, 100);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (wbook == null || filter.getAlgorithm() == null) {
            return blankBook;
        }

        Sheet sheet = wbook.getSheetAt(0);

        // Patient sheet - Table content
        Font font = wbook.createFont();
        CellStyle cellStyle = wbook.createCellStyle();

        font.setFontName("Times New Roman");
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.BLACK.getIndex());

        cellStyle.setFont(font);
        cellStyle.setWrapText(true);
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setIndention((short) 1);
        ExcelUtils.setBorders4Style(cellStyle);

        CellStyle dateCellStyle = wbook.createCellStyle();
        dateCellStyle.cloneStyleFrom(cellStyle);

        DataFormat format = wbook.createDataFormat();
        dateCellStyle.setDataFormat(format.getFormat("dd/MM/yyyy"));

        Integer rowIndex = 4;

        // Query for patients
        List<CaseOrg> caseOrgs = null;

        Timestamp fromDate = CommonUtils.toTimestamp(CommonUtils.dateStart(filter.getFromDate()));
        Timestamp toDate = CommonUtils.toTimestamp(CommonUtils.dateEnd(filter.getToDate()));

        for (Long orgId : filter.getActualOrganizations()) {

            if (filter.getAlgorithm() == ReportingAlgorithm.PEPFAR) {
                caseOrgs = coRepos.findTransferredOutPatients(orgId, fromDate, toDate, false);

                // get transferred out pending patients from previous month
                LocalDateTime lastMonthBegin =
                        filter.getFromDate().withDayOfMonth(15).minusMonths(1).withDayOfMonth(1);
                LocalDate lastMonthDate = lastMonthBegin.toLocalDate();
                LocalDateTime lastMonthEnd =
                        CommonUtils.dateEnd(
                                lastMonthBegin.withDayOfMonth(
                                        lastMonthDate
                                                .getMonth()
                                                .length(lastMonthDate.isLeapYear())));

                Timestamp lastFromDate = CommonUtils.toTimestamp(lastMonthBegin);
                Timestamp lastToDate = CommonUtils.toTimestamp(lastMonthEnd);

                List<CaseOrg> cos1 =
                        coRepos.findTransferredOutCompensate(
                                filter.getOrganization(),
                                lastFromDate,
                                lastToDate,
                                fromDate,
                                toDate,
                                false);

                if (cos1.size() > 0) {
                    caseOrgs.addAll(cos1);
                }
            } else {
                caseOrgs =
                        coRepos.findTransferredOutPatients_Circular03(
                                orgId, fromDate, toDate, false);
            }

            if (CommonUtils.isEmpty(caseOrgs)) {
                continue;
            }

            // Start filling out data...
            rowIndex =
                    _filloutTransedOutPatients(
                            filter, rowIndex, sheet, cellStyle, dateCellStyle, caseOrgs);
        }

        // Hide ID columns
        sheet.setColumnHidden(0, true);
        sheet.setColumnHidden(3, true);

        // Auto-filter
        if (rowIndex >= 4) {
            if (includeAppInfo) {
                sheet.setAutoFilter(CellRangeAddress.valueOf("A4:AO" + rowIndex));
            } else if (includeExtra) {
                sheet.setAutoFilter(CellRangeAddress.valueOf("A4:AX" + rowIndex));
            } else {
                sheet.setAutoFilter(CellRangeAddress.valueOf("A4:AL" + rowIndex));
            }
        }

        return wbook;
    }

    /**
     * Fill out transferred out patients
     *
     * @param filter
     * @param rowIndex
     * @param sheet
     * @param cellStyle
     * @param dateCellStyle
     * @param caseOrgs
     */
    private Integer _filloutTransedOutPatients(
            CaseReportFilterDto filter,
            int rowIndex,
            Sheet sheet,
            CellStyle cellStyle,
            CellStyle dateCellStyle,
            List<CaseOrg> caseOrgs) {

        Timestamp cutpoint = CommonUtils.toTimestamp(filter.getToDate());
        boolean includeExtra = CommonUtils.boolValue(filter.getIncludeExtra());
        boolean includeAppInfo = CommonUtils.boolValue(filter.getIncludeAppointmentInfo());

        for (CaseOrg co : caseOrgs) {

            Organization org = co.getOrganization();
            Case theCase = co.getTheCase();
            Person person = theCase.getPerson();

            int colIndex = 0;
            Row row = sheet.createRow(rowIndex++);
            row.setHeightInPoints(22);

            // Khóa chính
            Cell cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(theCase.getId().toString());

            // Tỉnh - thành phố của cơ sở
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);

            CaseOrg nextCaseOrg = null;

            List<CaseOrg> cos = Lists.newArrayList(theCase.getCaseOrgs());
            int i = 0;

            for (CaseOrg _co : cos) {
                if (_co.getId() == co.getId().longValue()) {
                    if (i > 0) {
                        // next org
                        int j = i;
                        while (j > 0) {
                            j--;
                            nextCaseOrg = cos.get(j);

                            if (nextCaseOrg.getStatus() != PatientStatus.CANCELLED_ENROLLMENT) {
                                break;
                            }
                        }

                        break;
                    } else {
                        break;
                    }
                }

                i++;
            }

            if (org.getAddress() != null && org.getAddress().getProvince() != null) {
                cell.setCellValue(org.getAddress().getProvince().getName());
            }

            // Cơ sở điều trị HIV/AIDS thực hiện chuyển gửi bệnh nhân
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isEmpty(org.getName())) {
                cell.setCellValue(org.getName());
            }

            // Mã bệnh nhân duy nhất
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);

            if (theCase.getNationalHealthId() != null) {
                cell.setCellValue(theCase.getNationalHealthId());
            } else {
                cell.setCellValue("-");
            }

            // Mã HIVInfo
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (theCase.getHivInfoID() != null) {
                cell.setCellValue(theCase.getHivInfoID());
            } else {
                cell.setCellValue("-");
            }

            // Mã bệnh án tại cơ sở điều trị thực hiện chuyển gửi bệnh nhân
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isEmpty(co.getPatientChartId(), true)) {
                cell.setCellValue(co.getPatientChartId());
            } else {
                cell.setCellValue("-");
            }

            // Họ tên bệnh nhân
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (person == null || CommonUtils.isEmpty(person.getFullname())) {
                cell.setCellValue("-");
            } else {
                cell.setCellValue(filter.isConfidentialRequired() ? "-" : person.getFullname());
            }

            // Giới tính
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (person == null || person.getGender() == null) {
                cell.setCellValue("-");
            } else {
                cell.setCellValue(person.getGender().toString());
            }

            // Ngày sinh
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellValue(CommonUtils.fromLocalDateTime(person.getDob()));
            cell.setCellStyle(dateCellStyle);

            // Số CMTND
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (person.getNidNumber() != null) {
                cell.setCellValue(filter.isConfidentialRequired() ? "-" : person.getNidNumber());
            } else {
                cell.setCellValue("-");
            }

            // Số CMTND
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (person.getPassportNumber() != null) {
                cell.setCellValue(
                        filter.isConfidentialRequired() ? "-" : person.getPassportNumber());
            } else {
                cell.setCellValue("-");
            }

            // Nghề nghiệp
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isEmpty(person.getOccupationName(), true)) {
                cell.setCellValue(person.getOccupationName());
            } else {
                cell.setCellValue("-");
            }

            // Dân tộc
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (person.getEthnic() != null) {
                cell.setCellValue(person.getEthnic().getValue());
            } else {
                cell.setCellValue("-");
            }

            // Số ĐT liên lạc
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            String phone = "";
            if (!CommonUtils.isEmpty(person.getHomePhone(), true)) {
                phone += person.getHomePhone();
                phone += "/ ";
            }

            if (!CommonUtils.isEmpty(person.getMobilePhone(), true)) {
                phone += person.getMobilePhone();
            }

            if (phone.isEmpty()) {
                phone = "-";
            } else if (phone.endsWith("/ ")) {
                phone = phone.substring(1, phone.length() - 2);
            }
            cell.setCellValue(phone);

            // Addresses
            Location rAddress = null;
            Location cAddress = null;

            if (person != null) {
                Set<Location> locs = person.getLocations();
                for (Location loc : locs) {
                    if (loc == null) {
                        continue;
                    }

                    switch (loc.getAddressType()) {
                        case RESIDENT_ADDRESS:
                            rAddress = loc;
                            break;
                        case CURRENT_ADDRESS:
                            cAddress = loc;
                            break;
                        default:
                            break;
                    }
                }
            }

            // Residential address
            if (rAddress != null) {
                // R address - details
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (!CommonUtils.isEmpty(rAddress.getStreetAddress())) {
                    cell.setCellValue(
                            filter.isConfidentialRequired() ? "-" : rAddress.getStreetAddress());
                } else {
                    cell.setCellValue("-");
                }

                // R address - commune
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (rAddress.getCommune() != null) {
                    cell.setCellValue(
                            filter.isConfidentialRequired()
                                    ? "-"
                                    : rAddress.getCommune().getName());
                } else {
                    cell.setCellValue("-");
                }

                // R address - district
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (rAddress.getDistrict() != null) {
                    cell.setCellValue(rAddress.getDistrict().getName());
                } else {
                    cell.setCellValue("-");
                }

                // R address - province
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (rAddress.getProvince() != null) {
                    cell.setCellValue(rAddress.getProvince().getName());
                } else {
                    cell.setCellValue("-");
                }
            } else {
                // create empty residential address cells
                for (i = 0; i < 4; i++) {
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue("-");
                }
            }

            // Current address
            if (cAddress != null) {
                // C address - details
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (!CommonUtils.isEmpty(cAddress.getStreetAddress())) {
                    cell.setCellValue(
                            filter.isConfidentialRequired() ? "-" : cAddress.getStreetAddress());
                } else {
                    cell.setCellValue("-");
                }

                // C address - commune
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (cAddress.getCommune() != null) {
                    cell.setCellValue(
                            filter.isConfidentialRequired()
                                    ? "-"
                                    : cAddress.getCommune().getName());
                } else {
                    cell.setCellValue("-");
                }

                // C address - district
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (cAddress.getDistrict() != null) {
                    cell.setCellValue(cAddress.getDistrict().getName());
                } else {
                    cell.setCellValue("-");
                }

                // C address - province
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (cAddress.getProvince() != null) {
                    cell.setCellValue(cAddress.getProvince().getName());
                } else {
                    cell.setCellValue("-");
                }
            } else {
                // create empty current address cells
                for (i = 0; i < 4; i++) {
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue("-");
                }
            }

            // Ngày xét nghiệm sàng lọc
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(dateCellStyle);
            if (theCase.getHivScreenDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getHivScreenDate()));
            } else {
                cell.setCellValue("-");
            }

            // Ngày XN khẳng định
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (theCase.getHivConfirmDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getHivConfirmDate()));
            } else {
                cell.setCellValue("-");
            }

            // Cơ sở XN khẳng định
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(
                    !CommonUtils.isEmpty(theCase.getConfirmLabName())
                            ? theCase.getConfirmLabName()
                            : "-");

            // Ngày khởi liều ARV
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (theCase.getArvStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getArvStartDate()));
            } else {
                cell.setCellValue("-");
            }

            // Ngày bắt đầu đợt điều trị gần nhất
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (co.getStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(co.getStartDate()));
            } else {
                cell.setCellValue("-");
            }

            // Loại đăng ký của đợt điều trị gần nhất
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (co.getEnrollmentType() != null) {
                cell.setCellValue(co.getEnrollmentType().toString());
            } else {
                cell.setCellValue("-");
            }

            // Nhóm điều trị ARV
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (co.getArvGroup() != null) {
                cell.setCellValue(co.getArvGroup());
            } else {
                cell.setCellValue("-");
            }

            // Ngày thực hiện chuyển gửi bệnh nhân
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (co.getEndDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(co.getEndDate()));
            } else {
                cell.setCellValue("-");
            }

            // Lý do chuyển gửi bệnh nhân
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isEmpty(co.getEndingReason(), true)) {
                cell.setCellValue(co.getEndingReason());
            } else {
                cell.setCellValue("-");
            }

            // Phác đồ thuốc hiện tại
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(
                    !CommonUtils.isEmpty(theCase.getCurrentArvRegimenName())
                            ? theCase.getCurrentArvRegimenName()
                            : "-");

            // Ngày bắt đầu phác đồ hiện tại
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (theCase.getCurrentArvRegimenStartDate() != null) {
                cell.setCellValue(
                        CommonUtils.fromLocalDateTime(theCase.getCurrentArvRegimenStartDate()));
            } else {
                cell.setCellValue("-");
            }

            // Ngày chuyển phác đồ bậc 2
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (theCase.getSecondLineStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getSecondLineStartDate()));
            } else {
                cell.setCellValue("-");
            }

            // Cơ sở điều trị HIV/AIDS tiếp nhận bệnh nhân
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (nextCaseOrg != null && nextCaseOrg.getOrganization() != null) {
                if (Constants.CODE_ORGANIZATION_OTHER.equalsIgnoreCase(
                        nextCaseOrg.getOrganization().getCode())) {
                    cell.setCellValue(nextCaseOrg.getOrganizationName());
                } else {
                    cell.setCellValue(nextCaseOrg.getOrganization().getName());
                }
            } else {
                cell.setCellValue("-");
            }

            // Trạng thái điều trị của bệnh nhân tại cơ sở tiếp nhận
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (nextCaseOrg != null) {
                cell.setCellValue(
                        (nextCaseOrg.getStatus() != null)
                                ? nextCaseOrg.getStatus().toString()
                                : "-");
            } else {
                cell.setCellValue("-");
            }

            // Ngày cập nhật trạng thái điều trị tại cơ sở tiếp nhận
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (nextCaseOrg != null) {
                if (nextCaseOrg.getStatus() == PatientStatus.ACTIVE) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(nextCaseOrg.getStartDate()));
                } else if (nextCaseOrg.getEndDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(nextCaseOrg.getEndDate()));
                } else {
                    cell.setCellValue("-");
                }
            } else {
                cell.setCellValue("-");
            }

            // Mã bệnh án của bệnh nhân tại cơ sở tiếp nhận
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (nextCaseOrg != null) {
                cell.setCellValue(
                        (nextCaseOrg.getPatientChartId() != null)
                                ? nextCaseOrg.getPatientChartId()
                                : "-");
            } else {
                cell.setCellValue("-");
            }

            // Extra columns
            if (includeAppInfo) {
                filloutAppointmentData(org, theCase, row, colIndex, cellStyle, dateCellStyle);
            } else if (includeExtra) {
                filloutExtra(theCase, cutpoint, row, colIndex, cellStyle, dateCellStyle);
            }
        }

        return rowIndex;
    }

    /**
     * Fill out workbook for Ly Sep 2021
     *
     * @param filter
     * @param rowIndex
     * @param sheet
     * @param cellStyle
     * @param dateCellStyle
     * @param caseOrgs
     * @param caseIds
     * @param cutpoint
     * @return
     */
    private Integer _filloutLySep2021Patients(
            CaseReportFilterDto filter,
            int rowIndex,
            Sheet sheet,
            CellStyle cellStyle,
            CellStyle dateCellStyle,
            List<CaseOrg> caseOrgs,
            List<Long> caseIds,
            Timestamp cutpoint) {

        // Start filling out data...
        for (CaseOrg co : caseOrgs) {
            Organization org = co.getOrganization();
            Case theCase = co.getTheCase();
            Person person = theCase.getPerson();

            // record the case Ids
            caseIds.add(theCase.getId());

            int colIndex = 0;
            Row row = sheet.createRow(rowIndex++);
            row.setHeightInPoints(22);

            // Khóa chính
            Cell cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(theCase.getId().toString());

            // Tỉnh - thành phố của cơ sở
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (org.getAddress() != null && org.getAddress().getProvince() != null) {
                cell.setCellValue(org.getAddress().getProvince().getName());
            }

            // Cơ sở điều trị
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (org != null) {
                cell.setCellValue(org.getName());
            }

            // Mã HIVInfo
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (theCase.getHivInfoID() != null) {
                cell.setCellValue(theCase.getHivInfoID());
            } else {
                cell.setCellValue("-");
            }

            // Mã bệnh án tại cơ sở điều trị
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (co.getPatientChartId() != null) {
                cell.setCellValue(co.getPatientChartId());
            } else {
                cell.setCellValue("-");
            }

            // Họ tên bệnh nhân
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (person == null || CommonUtils.isEmpty(person.getFullname())) {
                cell.setCellValue("-");
            } else {
                cell.setCellValue(filter.isConfidentialRequired() ? "-" : person.getFullname());
            }

            // Giới tính
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (person == null || person.getGender() == null) {
                cell.setCellValue("-");
            } else {
                cell.setCellValue(person.getGender().toString());
            }

            // Ngày sinh
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (person != null && person.getDob() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(person.getDob()));
            } else {
                cell.setCellValue("-");
            }

            // Số CMTND
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (person != null && person.getNidNumber() != null) {
                cell.setCellValue(filter.isConfidentialRequired() ? "-" : person.getNidNumber());
            } else {
                cell.setCellValue("-");
            }

            // Nghề nghiệp
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isEmpty(person.getOccupationName(), true)) {
                cell.setCellValue(person.getOccupationName());
            } else {
                cell.setCellValue("-");
            }

            // Dân tộc
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (person.getEthnic() != null) {
                cell.setCellValue(person.getEthnic().getValue());
            } else {
                cell.setCellValue("-");
            }

            // Addresses
            Location rAddress = null;
            Location cAddress = null;

            if (person != null) {
                Set<Location> locs = person.getLocations();
                for (Location loc : locs) {
                    if (loc == null) {
                        continue;
                    }

                    switch (loc.getAddressType()) {
                        case RESIDENT_ADDRESS:
                            rAddress = loc;
                            break;
                        case CURRENT_ADDRESS:
                            cAddress = loc;
                            break;
                        default:
                            break;
                    }
                }
            }

            // Residential address
            if (rAddress != null) {
                // R address - details
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (!CommonUtils.isEmpty(rAddress.getStreetAddress())) {
                    cell.setCellValue(
                            filter.isConfidentialRequired() ? "-" : rAddress.getStreetAddress());
                } else {
                    cell.setCellValue("-");
                }

                // R address - commune
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (rAddress.getCommune() != null) {
                    cell.setCellValue(
                            filter.isConfidentialRequired()
                                    ? "-"
                                    : rAddress.getCommune().getName());
                } else {
                    cell.setCellValue("-");
                }

                // R address - district
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (rAddress.getDistrict() != null) {
                    cell.setCellValue(rAddress.getDistrict().getName());
                } else {
                    cell.setCellValue("-");
                }

                // R address - province
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (rAddress.getProvince() != null) {
                    cell.setCellValue(rAddress.getProvince().getName());
                } else {
                    cell.setCellValue("-");
                }
            } else {
                // create empty residential address cells
                for (int i = 0; i < 4; i++) {
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue("-");
                }
            }

            // Current address
            if (cAddress != null) {
                // C address - details
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (!CommonUtils.isEmpty(cAddress.getStreetAddress())) {
                    cell.setCellValue(
                            filter.isConfidentialRequired() ? "-" : cAddress.getStreetAddress());
                } else {
                    cell.setCellValue("-");
                }

                // C address - commune
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (cAddress.getCommune() != null) {
                    cell.setCellValue(
                            filter.isConfidentialRequired()
                                    ? "-"
                                    : cAddress.getCommune().getName());
                } else {
                    cell.setCellValue("-");
                }

                // C address - district
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (cAddress.getDistrict() != null) {
                    cell.setCellValue(cAddress.getDistrict().getName());
                } else {
                    cell.setCellValue("-");
                }

                // C address - province
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (cAddress.getProvince() != null) {
                    cell.setCellValue(cAddress.getProvince().getName());
                } else {
                    cell.setCellValue("-");
                }
            } else {
                // create empty current address cells
                for (int i = 0; i < 4; i++) {
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue("-");
                }
            }

            // Ngày xét nghiệm sàng lọc
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(dateCellStyle);
            if (theCase.getHivScreenDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getHivScreenDate()));
            } else {
                cell.setCellValue("-");
            }

            // Ngày XN khẳng định
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (theCase.getHivConfirmDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getHivConfirmDate()));
            } else {
                cell.setCellValue("-");
            }

            // Ngày bắt đầu ARV
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (theCase.getArvStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getArvStartDate()));
            } else {
                cell.setCellValue("-");
            }

            // Ngày bắt đầu đợt điều trị trong kỳ báo cáo
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (co.getStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(co.getStartDate()));
            } else {
                cell.setCellValue("-");
            }

            // Loại đăng ký tại cơ sở trong kỳ báo cáo
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (co.getEnrollmentType() != null) {
                cell.setCellValue(co.getEnrollmentType().toString());
            } else {
                cell.setCellValue("-");
            }

            // Phác đồ thuốc hiện tại
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isEmpty(theCase.getCurrentArvRegimenName())) {
                cell.setCellValue(theCase.getCurrentArvRegimenName());
            } else {
                cell.setCellValue("-");
            }

            // Ngày bắt đầu phác đồ hiện tại
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (theCase.getCurrentArvRegimenStartDate() != null) {
                cell.setCellValue(
                        CommonUtils.fromLocalDateTime(theCase.getCurrentArvRegimenStartDate()));
            } else {
                cell.setCellValue("-");
            }

            // Ngày chuyển phác đồ bậc 2
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (theCase.getSecondLineStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getSecondLineStartDate()));
            } else {
                cell.setCellValue("-");
            }

            // Trạng thái điều trị của bệnh nhân tại cơ sở
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue((co.getStatus() != null) ? co.getStatus().toString() : "-");

            // Ngày thay đổi tình trạng
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (co.getStatus() == PatientStatus.ACTIVE) {
                if (co.getStartDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(co.getStartDate()));
                } else {
                    cell.setCellValue("-");
                }
            } else if (co.getEndDate() != null) {
                if (co.getEndDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(co.getEndDate()));
                } else {
                    cell.setCellValue("-");
                }
            }

            // ---------------------------------------------------------
            // Extra columns
            // ---------------------------------------------------------

            List<PatientExtraData> extraData =
                    cRepos.queryCaseExtraData(theCase.getId(), cutpoint).parallelStream()
                            .map(
                                    obj ->
                                            projectionFactory.createProjection(
                                                    PatientExtraData.class, obj))
                            .collect(Collectors.toList());

            if (extraData.size() <= 0) {
                for (int j = 0; j < 9; j++) {
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue("-");
                }

                continue;
            }

            PatientExtraData extra = extraData.get(0);

            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (extra.get_1_2_VlSampleDate() != null) {
                cell.setCellValue(CommonUtils.fromTimestamp(extra.get_1_2_VlSampleDate()));
            } else {
                cell.setCellValue("-");
            }

            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (extra.get_1_2_VlSampleDate() != null) {
                cell.setCellValue(CommonUtils.fromTimestamp(extra.get_1_3_VlResultDate()));
            } else {
                cell.setCellValue("-");
            }

            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isEmpty(extra.get_1_4_VlReasonForTesting(), true)) {
                cell.setCellValue(extra.get_1_4_VlReasonForTesting());
            } else {
                cell.setCellValue("-");
            }

            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(cellStyle);
            if (extra.get_1_5_VlResult() != null) {
                if (extra.get_1_5_VlResult() == 0) {
                    cell.setCellValue("Không phát hiện");
                } else {
                    cell.setCellValue(extra.get_1_5_VlResult());
                }
            } else {
                cell.setCellValue("-");
            }

            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (extra.get_1_51_VlResultText() != null) {
                cell.setCellValue(extra.get_1_51_VlResultText());
            } else {
                cell.setCellValue("-");
            }

            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isEmpty(extra.get_1_8_RiskName(), true)
                    || !CommonUtils.isEmpty(extra.get_1_9_OtherRiskGroup(), true)) {
                if (!CommonUtils.isEmpty(extra.get_1_8_RiskName(), true)) {
                    cell.setCellValue(extra.get_1_8_RiskName());
                } else if (!CommonUtils.isEmpty(extra.get_1_9_OtherRiskGroup(), true)) {
                    cell.setCellValue(extra.get_1_9_OtherRiskGroup());
                }
            } else {
                cell.setCellValue("-");
            }

            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (extra.get_1_6_RiskInterviewDate() != null) {
                cell.setCellValue(extra.get_1_6_RiskInterviewDate());
            } else {
                cell.setCellValue("-");
            }

            //            cell = row.createCell(colIndex++, CellType.NUMERIC);
            //            cell.setCellStyle(cellStyle);
            //            if (extra.get_2_1_ClinicalStage() != null) {
            //                cell.setCellValue(extra.get_2_1_ClinicalStage());
            //            } else {
            //                cell.setCellValue("-");
            //            }
            //
            //            cell = row.createCell(colIndex++, CellType.NUMERIC);
            //            cell.setCellStyle(dateCellStyle);
            //            if (extra.get_2_2_ClinicalStageEvalDate() != null) {
            //                cell.setCellValue(extra.get_2_2_ClinicalStageEvalDate());
            //            } else {
            //                cell.setCellValue("-");
            //            }
        }

        return rowIndex;
    }

    private void _filloutLySep2021_2_Patients(
            CaseReportFilterDto filter,
            Sheet sheet,
            CellStyle cellStyle,
            CellStyle dateCellStyle,
            List<Case> cases) {

        int rowIndex = 2;
        LocalDateTime ldt = LocalDateTime.of(2021, 9, 20, 0, 0, 0);
        Timestamp cutpoint = CommonUtils.toTimestamp(ldt);

        // Start filling out data...
        for (Case theCase : cases) {
            Person person = theCase.getPerson();

            int colIndex = 0;
            Row row = sheet.createRow(rowIndex++);
            row.setHeightInPoints(22);

            // Khóa chính
            Cell cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(theCase.getId().toString());

            // Mã HIVInfo
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (theCase.getHivInfoID() != null) {
                cell.setCellValue(theCase.getHivInfoID());
            } else {
                cell.setCellValue("-");
            }

            // Addresses
            Location rAddress = null;
            Location cAddress = null;

            if (person != null) {
                Set<Location> locs = person.getLocations();
                for (Location loc : locs) {
                    if (loc == null) {
                        continue;
                    }

                    switch (loc.getAddressType()) {
                        case RESIDENT_ADDRESS:
                            rAddress = loc;
                            break;
                        case CURRENT_ADDRESS:
                            cAddress = loc;
                            break;
                        default:
                            break;
                    }
                }
            }

            // Residential address
            if (rAddress != null) {
                // R address - details
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (!CommonUtils.isEmpty(rAddress.getStreetAddress())) {
                    cell.setCellValue(
                            filter.isConfidentialRequired() ? "-" : rAddress.getStreetAddress());
                } else {
                    cell.setCellValue("-");
                }

                // R address - commune
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (rAddress.getCommune() != null) {
                    cell.setCellValue(
                            filter.isConfidentialRequired()
                                    ? "-"
                                    : rAddress.getCommune().getName());
                } else {
                    cell.setCellValue("-");
                }

                // R address - district
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (rAddress.getDistrict() != null) {
                    cell.setCellValue(rAddress.getDistrict().getName());
                } else {
                    cell.setCellValue("-");
                }

                // R address - province
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (rAddress.getProvince() != null) {
                    cell.setCellValue(rAddress.getProvince().getName());
                } else {
                    cell.setCellValue("-");
                }
            } else {
                // create empty residential address cells
                for (int i = 0; i < 4; i++) {
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue("-");
                }
            }

            // Current address
            if (cAddress != null) {
                // C address - details
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (!CommonUtils.isEmpty(cAddress.getStreetAddress())) {
                    cell.setCellValue(
                            filter.isConfidentialRequired() ? "-" : cAddress.getStreetAddress());
                } else {
                    cell.setCellValue("-");
                }

                // C address - commune
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (cAddress.getCommune() != null) {
                    cell.setCellValue(
                            filter.isConfidentialRequired()
                                    ? "-"
                                    : cAddress.getCommune().getName());
                } else {
                    cell.setCellValue("-");
                }

                // C address - district
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (cAddress.getDistrict() != null) {
                    cell.setCellValue(cAddress.getDistrict().getName());
                } else {
                    cell.setCellValue("-");
                }

                // C address - province
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (cAddress.getProvince() != null) {
                    cell.setCellValue(cAddress.getProvince().getName());
                } else {
                    cell.setCellValue("-");
                }
            } else {
                // create empty current address cells
                for (int i = 0; i < 4; i++) {
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue("-");
                }
            }

            // ---------------------------------------------------------
            // Extra columns
            // ---------------------------------------------------------

            List<PatientExtraData> extraData =
                    cRepos.queryCaseExtraData(theCase.getId(), cutpoint).parallelStream()
                            .map(
                                    obj ->
                                            projectionFactory.createProjection(
                                                    PatientExtraData.class, obj))
                            .collect(Collectors.toList());

            if (extraData.size() <= 0) {
                for (int j = 0; j < 2; j++) {
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue("-");
                }

                continue;
            }

            PatientExtraData extra = extraData.get(0);

            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isEmpty(extra.get_1_8_RiskName(), true)
                    || !CommonUtils.isEmpty(extra.get_1_9_OtherRiskGroup(), true)) {
                if (!CommonUtils.isEmpty(extra.get_1_8_RiskName(), true)) {
                    cell.setCellValue(extra.get_1_8_RiskName());
                } else if (!CommonUtils.isEmpty(extra.get_1_9_OtherRiskGroup(), true)) {
                    cell.setCellValue(extra.get_1_9_OtherRiskGroup());
                }
            } else {
                cell.setCellValue("-");
            }

            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (extra.get_1_6_RiskInterviewDate() != null) {
                cell.setCellValue(extra.get_1_6_RiskInterviewDate());
            } else {
                cell.setCellValue("-");
            }
        }

        if (rowIndex >= 2) {
            sheet.setAutoFilter(CellRangeAddress.valueOf("A2:AG" + rowIndex));
        }
    }

    private void filloutAppointmentData(
            Organization org,
            Case theCase,
            Row row,
            int colIndex,
            CellStyle cellStyle,
            CellStyle dateCellStyle) {
        Set<Appointment> apps = theCase.getAppointments();
        Appointment latestArrived =
                apps.stream()
                        .filter(a -> a.getOrganization().getId() == org.getId().longValue())
                        .filter(a -> CommonUtils.isTrue(a.getArrived()))
                        .findFirst()
                        .orElse(null);
        Appointment latestNotArrivedRightAfterArrived =
                apps.stream()
                        .filter(a -> a.getOrganization().getId() == org.getId().longValue())
                        .filter(
                                a ->
                                        !CommonUtils.isTrue(a.getArrived())
                                                && (latestArrived != null
                                                        ? a.getAppointmentDate()
                                                                .isAfter(
                                                                        latestArrived
                                                                                .getAppointmentDate())
                                                        : true))
                        .min(
                                Comparator.comparing(
                                        Appointment::getAppointmentDate,
                                        (a1, a2) -> {
                                            return a1.isBefore(a2) ? 0 : 1;
                                        }))
                        .orElse(null);

        Cell cell = row.createCell(colIndex++, CellType.NUMERIC);
        cell.setCellStyle(dateCellStyle);
        if (latestArrived != null && latestArrived.getAppointmentDate() != null) {
            cell.setCellValue(CommonUtils.fromLocalDateTime(latestArrived.getAppointmentDate()));
        } else {
            cell.setCellValue("-");
        }

        cell = row.createCell(colIndex++, CellType.NUMERIC);
        cell.setCellStyle(cellStyle);
        if (latestArrived != null && latestArrived.getDrugDays() != null) {
            cell.setCellValue(latestArrived.getDrugDays());
        } else {
            cell.setCellValue("-");
        }

        cell = row.createCell(colIndex++, CellType.NUMERIC);
        cell.setCellStyle(dateCellStyle);
        if (latestNotArrivedRightAfterArrived != null
                && latestNotArrivedRightAfterArrived.getAppointmentDate() != null) {
            cell.setCellValue(
                    CommonUtils.fromLocalDateTime(
                            latestNotArrivedRightAfterArrived.getAppointmentDate()));
        } else {
            cell.setCellValue("-");
        }
    }

    private void filloutExtra(
            Case theCase,
            Timestamp cutpoint,
            Row row,
            int colIndex,
            CellStyle cellStyle,
            CellStyle dateCellStyle) {
        List<PatientExtraData> extraData =
                cRepos.queryCaseExtraData(theCase.getId(), cutpoint).parallelStream()
                        .map(obj -> projectionFactory.createProjection(PatientExtraData.class, obj))
                        .collect(Collectors.toList());

        Cell cell = null;

        if (extraData.size() <= 0) {
            for (int j = 0; j < 14; j++) {
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue("-");
            }

            return;
        }

        PatientExtraData extra = extraData.get(0);

        cell = row.createCell(colIndex++, CellType.NUMERIC);
        cell.setCellStyle(dateCellStyle);
        if (extra.get_1_2_VlSampleDate() != null) {
            cell.setCellValue(CommonUtils.fromTimestamp(extra.get_1_2_VlSampleDate()));
        } else {
            cell.setCellValue("-");
        }

        cell = row.createCell(colIndex++, CellType.NUMERIC);
        cell.setCellStyle(dateCellStyle);
        if (extra.get_1_2_VlSampleDate() != null) {
            cell.setCellValue(CommonUtils.fromTimestamp(extra.get_1_3_VlResultDate()));
        } else {
            cell.setCellValue("-");
        }

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellStyle(cellStyle);
        if (!CommonUtils.isEmpty(extra.get_1_4_VlReasonForTesting(), true)) {
            cell.setCellValue(extra.get_1_4_VlReasonForTesting());
        } else {
            cell.setCellValue("-");
        }

        cell = row.createCell(colIndex++, CellType.NUMERIC);
        cell.setCellStyle(cellStyle);
        if (extra.get_1_5_VlResult() != null) {
            if (extra.get_1_5_VlResult() == 0) {
                cell.setCellValue("Không phát hiện");
            } else {
                cell.setCellValue(extra.get_1_5_VlResult());
            }
        } else {
            cell.setCellValue("-");
        }

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellStyle(cellStyle);
        if (extra.get_1_51_VlResultText() != null) {
            cell.setCellValue(extra.get_1_51_VlResultText());
        } else {
            cell.setCellValue("-");
        }

        // Nhóm nguy cơ xa nhất

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellStyle(cellStyle);
        if (!CommonUtils.isEmpty(extra.get_2_5_RiskName(), true)
                || !CommonUtils.isEmpty(extra.get_2_6_OtherRiskGroup(), true)) {
            if (!CommonUtils.isEmpty(extra.get_2_5_RiskName(), true)) {
                cell.setCellValue(extra.get_2_5_RiskName());
            } else if (!CommonUtils.isEmpty(extra.get_2_6_OtherRiskGroup(), true)) {
                cell.setCellValue(extra.get_2_6_OtherRiskGroup());
            } else {
                cell.setCellValue("-");
            }
        } else {
            cell.setCellValue("-");
        }

        cell = row.createCell(colIndex++, CellType.NUMERIC);
        cell.setCellStyle(dateCellStyle);
        if (extra.get_2_3_RiskInterviewDate() != null) {
            cell.setCellValue(extra.get_2_3_RiskInterviewDate());
        } else {
            cell.setCellValue("-");
        }

        // Nhóm nguy cơ gần nhất

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellStyle(cellStyle);
        if (!CommonUtils.isEmpty(extra.get_1_8_RiskName(), true)
                || !CommonUtils.isEmpty(extra.get_1_9_OtherRiskGroup(), true)) {
            if (!CommonUtils.isEmpty(extra.get_1_8_RiskName(), true)) {
                cell.setCellValue(extra.get_1_8_RiskName());
            } else if (!CommonUtils.isEmpty(extra.get_1_9_OtherRiskGroup(), true)) {
                cell.setCellValue(extra.get_1_9_OtherRiskGroup());
            } else {
                cell.setCellValue("-");
            }
        } else {
            cell.setCellValue("-");
        }

        cell = row.createCell(colIndex++, CellType.NUMERIC);
        cell.setCellStyle(dateCellStyle);
        if (extra.get_1_6_RiskInterviewDate() != null) {
            cell.setCellValue(extra.get_1_6_RiskInterviewDate());
        } else {
            cell.setCellValue("-");
        }

        // Giai đoạn lâm sàng gần nhất

        //        cell = row.createCell(colIndex++, CellType.NUMERIC);
        //        cell.setCellStyle(cellStyle);
        //        if (extra.get_2_1_ClinicalStage() != null) {
        //            cell.setCellValue(extra.get_2_1_ClinicalStage());
        //        } else {
        //            cell.setCellValue("-");
        //        }
        //
        //        cell = row.createCell(colIndex++, CellType.NUMERIC);
        //        cell.setCellStyle(dateCellStyle);
        //        if (extra.get_2_2_ClinicalStageEvalDate() != null) {
        //            cell.setCellValue(extra.get_2_2_ClinicalStageEvalDate());
        //        } else {
        //            cell.setCellValue("-");
        //        }

        // Thông tin bảo hiểm y tế

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellStyle(cellStyle);
        if (!CommonUtils.isEmpty(extra.get_2_7_shiCardNumber(), true)) {
            cell.setCellValue(extra.get_2_7_shiCardNumber());
        } else {
            cell.setCellValue("-");
        }

        cell = row.createCell(colIndex++, CellType.NUMERIC);
        cell.setCellStyle(dateCellStyle);
        if (extra.get_2_8_shiExpiryDate() != null) {
            cell.setCellValue(extra.get_2_8_shiExpiryDate());
        } else {
            cell.setCellValue("-");
        }

        // TB prophylaxis

        //        cell = row.createCell(colIndex++, CellType.NUMERIC);
        //        cell.setCellStyle(dateCellStyle);
        //        if (extra.get_3_3_tbProStartDate() != null) {
        //            cell.setCellValue(extra.get_3_3_tbProStartDate());
        //        } else {
        //            cell.setCellValue("-");
        //        }
        //
        //        cell = row.createCell(colIndex++, CellType.NUMERIC);
        //        cell.setCellStyle(dateCellStyle);
        //        if (extra.get_3_4_tbProEndDate() != null) {
        //            cell.setCellValue(extra.get_3_4_tbProEndDate());
        //        } else {
        //            cell.setCellValue("-");
        //        }
        //
        //        cell = row.createCell(colIndex++, CellType.STRING);
        //        cell.setCellStyle(cellStyle);
        //        if (extra.get_3_5_tbProResult() != null) {
        //            switch (extra.get_3_5_tbProResult()) {
        //
        //                case 1:
        //                    cell.setCellValue("Bỏ trị");
        //                    break;
        //
        //                case 2:
        //                    cell.setCellValue("Chưa hoàn thành");
        //                    break;
        //
        //                case 3:
        //                    cell.setCellValue("Đã hoàn thành");
        //                    break;
        //
        //                default:
        //                    cell.setCellValue("-");
        //                    break;
        //            }
        //        } else {
        //            cell.setCellValue("-");
        //        }

        // Điều trị lao

        //        cell = row.createCell(colIndex++, CellType.NUMERIC);
        //        cell.setCellStyle(dateCellStyle);
        //        if (extra.get_3_6_tbtxDiagnoseDate() != null) {
        //            cell.setCellValue(extra.get_3_6_tbtxDiagnoseDate());
        //        } else {
        //            cell.setCellValue("-");
        //        }
        //
        //        cell = row.createCell(colIndex++, CellType.NUMERIC);
        //        cell.setCellStyle(dateCellStyle);
        //        if (extra.get_3_7_tbtxStartDate() != null) {
        //            cell.setCellValue(extra.get_3_7_tbtxStartDate());
        //        } else {
        //            cell.setCellValue("-");
        //        }
        //
        //        cell = row.createCell(colIndex++, CellType.NUMERIC);
        //        cell.setCellStyle(dateCellStyle);
        //        if (extra.get_3_8_tbtxEndDate() != null) {
        //            cell.setCellValue(extra.get_3_8_tbtxEndDate());
        //        } else {
        //            cell.setCellValue("-");
        //        }

        // Sang loc viem gan B

        //        cell = row.createCell(colIndex++, CellType.NUMERIC);
        //        cell.setCellStyle(dateCellStyle);
        //        if (extra.get_4_1_hepbTestDate() != null) {
        //            cell.setCellValue(extra.get_4_1_hepbTestDate());
        //        } else {
        //            cell.setCellValue("-");
        //        }
        //
        //        cell = row.createCell(colIndex++, CellType.STRING);
        //        cell.setCellStyle(cellStyle);
        //        if (extra.get_4_2_hepbTestPos() != null) {
        //            cell.setCellValue(CommonUtils.isTrue(extra.get_4_2_hepbTestPos()) ? "Dương
        // tính" : "Âm tính");
        //        } else {
        //            cell.setCellValue("-");
        //        }

        // Sang loc viem gan C

        //        cell = row.createCell(colIndex++, CellType.NUMERIC);
        //        cell.setCellStyle(dateCellStyle);
        //        if (extra.get_4_3_hepcTestDate() != null) {
        //            cell.setCellValue(extra.get_4_3_hepcTestDate());
        //        } else {
        //            cell.setCellValue("-");
        //        }
        //
        //        cell = row.createCell(colIndex++, CellType.STRING);
        //        cell.setCellStyle(cellStyle);
        //        if (extra.get_4_4_hepcTestPos() != null) {
        //            cell.setCellValue(CommonUtils.isTrue(extra.get_4_4_hepcTestPos()) ? "Dương
        // tính" : "Âm tính");
        //        } else {
        //            cell.setCellValue("-");
        //        }

        // Cap thuoc nhieu thang

        cell = row.createCell(colIndex++, CellType.NUMERIC);
        cell.setCellStyle(dateCellStyle);
        if (extra.get_2_9_mmdEvalDate() != null) {
            cell.setCellValue(extra.get_2_9_mmdEvalDate());
        } else {
            cell.setCellValue("-");
        }

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellStyle(cellStyle);
        if (extra.get_3_1_mmdEligible() != null) {
            cell.setCellValue(
                    CommonUtils.isTrue(extra.get_3_1_mmdEligible())
                            ? "Bệnh nhân ổn định"
                            : "Chưa ổn định");
        } else {
            cell.setCellValue("-");
        }

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellStyle(cellStyle);
        if (extra.get_3_2_mmdOnMmd() != null) {
            cell.setCellValue(
                    CommonUtils.isTrue(extra.get_3_2_mmdOnMmd())
                            ? "Đang cấp nhiều tháng"
                            : "Chưa cấp nhiều tháng");
        } else {
            cell.setCellValue("-");
        }
    }
}
