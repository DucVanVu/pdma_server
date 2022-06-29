package org.pepfar.pdma.app.data.service.jpa;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

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
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.docx4j.wml.P;
import org.pepfar.pdma.app.Constants;
import org.pepfar.pdma.app.data.domain.Case;
import org.pepfar.pdma.app.data.domain.CaseOrg;
import org.pepfar.pdma.app.data.domain.ClinicalStage;
import org.pepfar.pdma.app.data.domain.Hepatitis;
import org.pepfar.pdma.app.data.domain.LabTest;
import org.pepfar.pdma.app.data.domain.Location;
import org.pepfar.pdma.app.data.domain.MMT;
import org.pepfar.pdma.app.data.domain.Pregnancy;
import org.pepfar.pdma.app.data.domain.QCase;
import org.pepfar.pdma.app.data.domain.QCaseOrg;
import org.pepfar.pdma.app.data.domain.QClinicalStage;
import org.pepfar.pdma.app.data.domain.QHepatitis;
import org.pepfar.pdma.app.data.domain.QLabTest;
import org.pepfar.pdma.app.data.domain.QMMT;
import org.pepfar.pdma.app.data.domain.QPregnancy;
import org.pepfar.pdma.app.data.domain.QShiInterview;
import org.pepfar.pdma.app.data.domain.QTBProphylaxis;
import org.pepfar.pdma.app.data.domain.QTBTreatment;
import org.pepfar.pdma.app.data.domain.QTreatment;
import org.pepfar.pdma.app.data.domain.ShiInterview;
import org.pepfar.pdma.app.data.domain.TBProphylaxis;
import org.pepfar.pdma.app.data.domain.TBTreatment;
import org.pepfar.pdma.app.data.domain.Treatment;
import org.pepfar.pdma.app.data.domain.User;
import org.pepfar.pdma.app.data.dto.CaseReportFilterDto;
import org.pepfar.pdma.app.data.dto.PatientExtraData;
import org.pepfar.pdma.app.data.repository.CaseRepository;
import org.pepfar.pdma.app.data.service._ReportingService;
import org.pepfar.pdma.app.data.types.ClinicalTestingType;
import org.pepfar.pdma.app.data.types.LabTestReason;
import org.pepfar.pdma.app.data.types.PatientStatus;
import org.pepfar.pdma.app.data.types.Permission;
import org.pepfar.pdma.app.data.types.ReportType;
import org.pepfar.pdma.app.utils.AuthorizationUtils;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.ExcelUtils;
import org.pepfar.pdma.app.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;

@Service("_CBSReportingServiceImpl")
public class _CBSReportingServiceImpl implements _ReportingService {

    @Autowired
    private EntityManager em;

    @Autowired
    private AuthorizationUtils authUtils;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private CaseRepository cRepos;

    @Autowired
    private ProjectionFactory projectionFactory;

    @Override
    @Transactional(readOnly = true)
    public Workbook exportReport(CaseReportFilterDto filter) {

        Workbook blankBook = new XSSFWorkbook();
        blankBook.createSheet();

        if (filter == null || filter.getFromDate() == null || filter.getToDate() == null
                || filter.getReportType() == null || filter.getReportType() != ReportType.CBS_REPORT) {
            return blankBook;
        }

        LocalDateTime adjFromDate = filter.getFromDate().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime adjToDate = filter.getToDate().withHour(23).withMinute(59).withSecond(59).withNano(0);

        // Set adjusted date and time
        filter.setFromDate(adjFromDate);
        filter.setToDate(adjToDate);

        // Set actual organizations
        List<Long> grantedOrgIds = authUtils.getGrantedOrgIds(Permission.READ_ACCESS, filter.getProvince(), true);

        if (filter.getOrganization() == 0l) {
            filter.setActualOrganizations(grantedOrgIds);
        } else if (grantedOrgIds.contains(filter.getOrganization().longValue())) {
            filter.setActualOrganizations(Lists.newArrayList(filter.getOrganization()));
        } else {
            return blankBook;
        }

        // Read the workbook template
        Workbook wbook = null;
        try (InputStream template = context.getResource("classpath:templates/cbs-report-template.xlsx")
                .getInputStream()) {
            wbook = new SXSSFWorkbook(new XSSFWorkbook(template), 100);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (wbook == null) {
            return blankBook;
        }

        return generateCBSReport(filter, wbook);
    }

    /**
     * CBS report/monthly
     *
     * @param filter
     * @return
     */
    private Workbook generateCBSReport(CaseReportFilterDto filter, Workbook wbook) {
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

        CellStyle dateCellStyle2 = wbook.createCellStyle();
        dateCellStyle2.cloneStyleFrom(cellStyle);
        dateCellStyle2.setDataFormat(format.getFormat("dd/MM/yyyy hh:mm:ss"));

        // --------------------------------
        // For the first two worksheets
        // --------------------------------
        QCase q = QCase.case$;
        QCaseOrg qco = QCaseOrg.caseOrg;

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(qco.organization.id.longValue().in(filter.getActualOrganizations()));
        predicates.add(qco.latestRelationship.isTrue());
        predicates.add(qco.refTrackingOnly.isFalse());
        predicates.add(qco.status.notIn(PatientStatus.CANCELLED_ENROLLMENT, PatientStatus.PENDING_ENROLLMENT));
        predicates.add(q.deleted.isFalse());
        predicates.add(q.modifyDate.goe(filter.getFromDate()).and(q.modifyDate.loe(filter.getToDate())));

        List<Case> cases = new JPAQuery<Case>(em).distinct().from(q).innerJoin(q.caseOrgs, qco)
                .where(predicates.toArray(new Predicate[0])).orderBy(q.modifyDate.desc()).fetch();

        // Thông tin định danh
        createCaseInfoSheet(wbook, cellStyle, dateCellStyle, dateCellStyle2, cases, filter);

        // Xét nghiệm HIV
        createHIVDiagSheet(wbook, cellStyle, dateCellStyle, dateCellStyle2, cases);

        // Giai đoạn lâm sàng
        createClinicalStageSheet(wbook, cellStyle, dateCellStyle, dateCellStyle2, filter);

        // Phác đồ thuốc ARV
        createARVHistorySheet(wbook, cellStyle, dateCellStyle, dateCellStyle2, filter);

        // Quá trình điều trị
        createTreatmentHistorySheet(wbook, cellStyle, dateCellStyle, dateCellStyle2, filter);

        // Xét nghiệm CD4
        createCD4Sheet(wbook, cellStyle, dateCellStyle, dateCellStyle2, filter);

        // Xét nghiệm TLVR
        createVLSheet(wbook, cellStyle, dateCellStyle, dateCellStyle2, filter);

        // Xét nghiệm kháng thuốc
        createARVDRSheet(wbook, cellStyle, dateCellStyle, dateCellStyle2, filter);

        // Xét nghiệm nhiễm mới
        createRecencySheet(wbook, cellStyle, dateCellStyle, dateCellStyle2, filter);

        // ĐT dự phòng lao
        createTBProSheet(wbook, cellStyle, dateCellStyle, dateCellStyle2, filter);

        // Chẩn đoán, điều trị lao
        createTBTxSheet(wbook, cellStyle, dateCellStyle, dateCellStyle2, filter);

        // Viêm gan B, C
        createHepSheet(wbook, cellStyle, dateCellStyle, dateCellStyle2, filter);

        // Điều trị Methadone
        createMMTSheet(wbook, cellStyle, dateCellStyle, dateCellStyle2, filter);

        // Thẻ BHYT
        createShiSheet(wbook, cellStyle, dateCellStyle, dateCellStyle2, filter);

        // Mang thai
        createPregnancySheet(wbook, cellStyle, dateCellStyle, dateCellStyle2, filter);

        // Tử vong
        createDeadSheet(wbook, cellStyle, dateCellStyle, dateCellStyle2, filter);

        return wbook;
    }

    /**
     * Create case information worksheet
     *
     * @param wbook
     */
    private void createCaseInfoSheet(Workbook wbook, CellStyle cellStyle, CellStyle dateCellStyle,
                                     CellStyle dateCellStyle2, List<Case> cases, CaseReportFilterDto filter) {

        User user = SecurityUtils.getCurrentUser();
        boolean confidential = true;

        if (SecurityUtils.isUserInRoles(user, Constants.ROLE_SITE_MANAGER, Constants.ROLE_PROVINCIAL_MANAGER)) {
            confidential = false;
        }

        if (cases.size() <= 0) {
            return;
        }

        Sheet sheet = wbook.getSheetAt(0);

        Timestamp cutpoint = CommonUtils.toTimestamp(filter.getToDate());

        int rowIndex = 2;

        for (Case c : cases) {

            int colIndex = 0;

            Row row = sheet.createRow(rowIndex++);
            row.setHeightInPoints(22);

            // Case UUID
            Cell cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(c.getUid().toString());

            // HIVInfo ID
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isEmpty(c.getHivInfoID())) {
                cell.setCellValue(c.getHivInfoID());
            } else {
                cell.setCellValue("");
            }

            // National ID
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!confidential && !CommonUtils.isEmpty(c.getPerson().getNidNumber())) {
                cell.setCellValue(c.getPerson().getNidNumber());
            } else {
                cell.setCellValue("");
            }

            // Fullname
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!confidential) {
                cell.setCellValue(c.getPerson().getFullname());
            } else {
                cell.setCellValue("");
            }

            // DOB
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            cell.setCellValue(CommonUtils.fromLocalDateTime(c.getPerson().getDob()));

            // Gender
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(c.getPerson().getGender().toString());

            // Addresses
            Location rAddress = null;
            Location cAddress = null;

            Set<Location> locs = c.getPerson().getLocations();
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

            // Residential address
            if (rAddress != null) {
                // R address - province ID
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (rAddress.getProvince() != null) {
                    cell.setCellValue(rAddress.getProvince().getCodeGso());
                } else {
                    cell.setCellValue("");
                }

                // R address - province
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (rAddress.getProvince() != null) {
                    cell.setCellValue(rAddress.getProvince().getName());
                } else {
                    cell.setCellValue("");
                }

                // R address - district ID
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (rAddress.getDistrict() != null) {
                    cell.setCellValue(rAddress.getDistrict().getCodeGso());
                } else {
                    cell.setCellValue("");
                }

                // R address - district
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (rAddress.getDistrict() != null) {
                    cell.setCellValue(rAddress.getDistrict().getName());
                } else {
                    cell.setCellValue("");
                }

                // R address - commune ID
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (rAddress.getCommune() != null) {
                    cell.setCellValue(rAddress.getCommune().getCodeGso());
                } else {
                    cell.setCellValue("");
                }

                // R address - commune
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (rAddress.getCommune() != null) {
                    cell.setCellValue(rAddress.getCommune().getName());
                } else {
                    cell.setCellValue("");
                }

                // R address - details
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (!confidential && !CommonUtils.isEmpty(rAddress.getStreetAddress())) {
                    cell.setCellValue(rAddress.getStreetAddress());
                } else {
                    cell.setCellValue("");
                }
            } else {
                // create empty residential address cells
                for (int i = 0; i < 7; i++) {
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue("");
                }
            }

            // Current address
            if (cAddress != null) {
                // C address - province ID
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (cAddress.getProvince() != null) {
                    cell.setCellValue(cAddress.getProvince().getCodeGso());
                } else {
                    cell.setCellValue("");
                }

                // C address - province
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (cAddress.getProvince() != null) {
                    cell.setCellValue(cAddress.getProvince().getName());
                } else {
                    cell.setCellValue("");
                }

                // C address - district ID
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (cAddress.getDistrict() != null) {
                    cell.setCellValue(cAddress.getDistrict().getCodeGso());
                } else {
                    cell.setCellValue("");
                }

                // C address - district
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (cAddress.getDistrict() != null) {
                    cell.setCellValue(cAddress.getDistrict().getName());
                } else {
                    cell.setCellValue("");
                }

                // C address - commune ID
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (cAddress.getCommune() != null) {
                    cell.setCellValue(cAddress.getCommune().getCodeGso());
                } else {
                    cell.setCellValue("");
                }

                // C address - commune
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (cAddress.getCommune() != null) {
                    cell.setCellValue(cAddress.getCommune().getName());
                } else {
                    cell.setCellValue("");
                }

                // C address - details
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (!confidential && !CommonUtils.isEmpty(cAddress.getStreetAddress())) {
                    cell.setCellValue(cAddress.getStreetAddress());
                } else {
                    cell.setCellValue("");
                }
            } else {
                // create empty current address cells
                for (int i = 0; i < 7; i++) {
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue("");
                }
            }

            // Screening date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            cell.setCellValue(CommonUtils.fromLocalDateTime(c.getHivScreenDate()));

            // HIV confirmation date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            cell.setCellValue(CommonUtils.fromLocalDateTime(c.getHivConfirmDate()));

            // ARV start date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            cell.setCellValue(CommonUtils.fromLocalDateTime(c.getArvStartDate()));

            int i = colIndex;
            int nextColIndex = i + 7;

            while (i <= nextColIndex) {
                cell = row.createCell(i++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue("");
            }

            colIndex = i;

            // Create date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle2);
            cell.setCellValue(CommonUtils.fromLocalDateTime(c.getCreateDate()));

            // Modified date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle2);
            cell.setCellValue(CommonUtils.fromLocalDateTime(c.getModifyDate()));

            List<PatientExtraData> extraData = cRepos.queryCaseExtraData(c.getId(), cutpoint)
                    .parallelStream()
                    .map(obj -> projectionFactory.createProjection(PatientExtraData.class, obj))
                    .collect(Collectors.toList());

            if (extraData.size() <= 0) {
                for (int j = 0; j < 4; j++) {
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue("-");
                }
            } else {
                PatientExtraData extra = extraData.get(0);

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
            }
        }
    }

    /**
     * HIV diagnosis worksheet
     *
     * @param wbook
     */
    private void createHIVDiagSheet(Workbook wbook, CellStyle cellStyle, CellStyle dateCellStyle,
                                    CellStyle dateCellStyle2, List<Case> cases) {

        if (cases.size() <= 0) {
            return;
        }

        Sheet sheet = wbook.getSheetAt(1);

        int rowIndex = 1;

        for (Case c : cases) {

            int colIndex = 0;

            Row row = sheet.createRow(rowIndex++);
            row.setHeightInPoints(22);

            // Patient UUID
            Cell cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(c.getUid().toString());

            // HIVInfo ID
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isEmpty(c.getHivInfoID())) {
                cell.setCellValue(c.getHivInfoID());
            } else {
                cell.setCellValue("");
            }

            // Sample/Screen date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (c.getHivScreenDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(c.getHivScreenDate()));
            } else {
                cell.setCellValue("");
            }

            // Test date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (c.getHivConfirmDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(c.getHivConfirmDate()));
            } else {
                cell.setCellValue("");
            }

            // ID of confirm lab
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue("");

            // Name of confirm lab
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isEmpty(c.getConfirmLabName())) {
                cell.setCellValue(c.getConfirmLabName());
            } else {
                cell.setCellValue("");
            }

            // ID of sample site
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue("");

            // Name of sample site
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue("");

            // Create date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle2);
            cell.setCellValue(CommonUtils.fromLocalDateTime(c.getCreateDate()));

            // Modified date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle2);
            cell.setCellValue(CommonUtils.fromLocalDateTime(c.getModifyDate()));
        }
    }

    /**
     * Clinical stage history workbook
     *
     * @param wbook
     */
    private void createClinicalStageSheet(Workbook wbook, CellStyle cellStyle, CellStyle dateCellStyle,
                                          CellStyle dateCellStyle2, CaseReportFilterDto filter) {
        Sheet sheet = wbook.getSheetAt(2);

        QClinicalStage q = QClinicalStage.clinicalStage;
        QCase qc = QCase.case$;

        JPAQuery<ClinicalStage> query = new JPAQuery<ClinicalStage>(em).distinct().from(q).innerJoin(q.theCase, qc);

        // predicates
        List<Predicate> predicates = new LinkedList<>();
        predicates.add(q.organization.id.longValue().in(filter.getActualOrganizations()));
        predicates.add(q.modifyDate.goe(filter.getFromDate()).and(q.modifyDate.loe(filter.getToDate())));
        predicates.add(qc.deleted.isFalse());

        // Query
        query.where(predicates.toArray(new Predicate[0]));

        List<ClinicalStage> stages = query.orderBy(qc.person.fullname.asc()).orderBy(q.evalDate.desc()).fetch();

        int rowIndex = 1;

        for (ClinicalStage r : stages) {

            Case c = r.getTheCase();
            int colIndex = 0;

            Row row = sheet.createRow(rowIndex++);
            row.setHeightInPoints(22);

            // Patient UUID
            Cell cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(c.getUid().toString());

            // Record UUID
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(r.getUid().toString());

            // HIVInfo ID
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isEmpty(c.getHivInfoID())) {
                cell.setCellValue(c.getHivInfoID());
            } else {
                cell.setCellValue("");
            }

            // Evaluation date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            cell.setCellValue(CommonUtils.fromLocalDateTime(r.getEvalDate()));

            // ID of clinical stage
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(r.getStage());

            // Create date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle2);
            cell.setCellValue(CommonUtils.fromLocalDateTime(r.getCreateDate()));

            // Modified date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle2);
            cell.setCellValue(CommonUtils.fromLocalDateTime(r.getModifyDate()));
        }
    }

    /**
     * ARV regimen history worksheet
     *
     * @param wbook
     */
    private void createARVHistorySheet(Workbook wbook, CellStyle cellStyle, CellStyle dateCellStyle,
                                       CellStyle dateCellStyle2, CaseReportFilterDto filter) {
        Sheet sheet = wbook.getSheetAt(3);

        QTreatment q = QTreatment.treatment;
        QCase qc = QCase.case$;

        JPAQuery<Treatment> query = new JPAQuery<Treatment>(em).distinct().from(q).innerJoin(q.theCase, qc);

        // predicates
        List<Predicate> predicates = new LinkedList<>();
        predicates.add(q.organization.id.longValue().in(filter.getActualOrganizations()));
        predicates.add(q.modifyDate.goe(filter.getFromDate()).and(q.modifyDate.loe(filter.getToDate())));
        predicates.add(qc.deleted.isFalse());

        // Query
        query.where(predicates.toArray(new Predicate[0]));

        List<Treatment> entities = query.orderBy(qc.person.fullname.asc()).orderBy(q.startDate.desc()).fetch();

        int rowIndex = 1;

        for (Treatment r : entities) {
            Case c = r.getTheCase();
            int colIndex = 0;

            Row row = sheet.createRow(rowIndex++);
            row.setHeightInPoints(22);

            // Patient UUID
            Cell cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(c.getUid().toString());

            // Record UUID
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(r.getUid().toString());

            // HIVInfo ID
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isEmpty(c.getHivInfoID())) {
                cell.setCellValue(c.getHivInfoID());
            } else {
                cell.setCellValue("");
            }

            // Start date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (r.getStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(r.getStartDate()));
            } else {
                cell.setCellValue("");
            }

            // End date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (r.getEndDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(r.getEndDate()));
            } else {
                cell.setCellValue("");
            }

            // ID of regimen
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue("");

            // Name of regimen
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(r.getRegimenName());

            // Create date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle2);
            cell.setCellValue(CommonUtils.fromLocalDateTime(r.getCreateDate()));

            // Modified date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle2);
            cell.setCellValue(CommonUtils.fromLocalDateTime(r.getModifyDate()));
        }
    }

    /**
     * Treatment history worksheet
     *
     * @param wbook
     */
    private void createTreatmentHistorySheet(Workbook wbook, CellStyle cellStyle, CellStyle dateCellStyle,
                                             CellStyle dateCellStyle2, CaseReportFilterDto filter) {
        Sheet sheet = wbook.getSheetAt(4);

        QCaseOrg q = QCaseOrg.caseOrg;
        QCase qc = QCase.case$;

        JPAQuery<CaseOrg> query = new JPAQuery<CaseOrg>(em).distinct().from(q).innerJoin(q.theCase, qc);

        // predicates
        List<Predicate> predicates = new LinkedList<>();
        predicates.add(q.organization.id.longValue().in(filter.getActualOrganizations()));
        predicates.add(q.modifyDate.goe(filter.getFromDate()).and(q.modifyDate.loe(filter.getToDate())));
        predicates.add(q.status.notIn(PatientStatus.PENDING_ENROLLMENT, PatientStatus.CANCELLED_ENROLLMENT));
        predicates.add(qc.deleted.isFalse());

        // Query
        query.where(predicates.toArray(new Predicate[0]));

        List<CaseOrg> entities = query.orderBy(qc.person.fullname.asc()).orderBy(q.startDate.desc()).fetch();

        int rowIndex = 1;

        for (CaseOrg r : entities) {
            Case c = r.getTheCase();

            int colIndex = 0;

            Row row = sheet.createRow(rowIndex++);
            row.setHeightInPoints(22);

            // Patient UUID
            Cell cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(c.getUid().toString());

            // Record UUID
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(r.getUid().toString());

            // HIVInfo ID
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isEmpty(c.getHivInfoID())) {
                cell.setCellValue(c.getHivInfoID());
            } else {
                cell.setCellValue("");
            }

            // Start date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (r.getStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(r.getStartDate()));
            } else {
                cell.setCellValue("");
            }

            // End date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (r.getEndDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(r.getEndDate()));
            } else {
                cell.setCellValue("");
            }

            // Registration type ID
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(r.getEnrollmentType().getNumber());

            // Registration type name
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(r.getEnrollmentType().toString());

            // Treatment status ID
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            switch (r.getStatus()) {
                case ACTIVE:
                    cell.setCellValue(3);
                    break;
                case DEAD:
                    cell.setCellValue(7);
                    break;
                case LTFU:
                    cell.setCellValue(4);
                    break;
                case TRANSFERRED_OUT:
                    cell.setCellValue(5);
                    break;
                default:
                    break;
            }

            // Registration type name
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(r.getStatus().toString());

            // ID of OPC
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(cellStyle);
            cell.setCellValue("");

            // Name of OPC
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(r.getOrganization().getName());

            // Create date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle2);
            cell.setCellValue(CommonUtils.fromLocalDateTime(r.getCreateDate()));

            // Modified date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle2);
            cell.setCellValue(CommonUtils.fromLocalDateTime(r.getModifyDate()));

            // Province of the OPC
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);

            Location loc = r.getOrganization().getAddress();
            if (loc != null && loc.getProvince() != null && loc.getProvince().getName() != null) {
                cell.setCellValue(loc.getProvince().getName());
            } else {
                cell.setCellValue("-");
            }
        }
    }

    /**
     * CD4 worksheet
     *
     * @param wbook
     */
    private void createCD4Sheet(Workbook wbook, CellStyle cellStyle, CellStyle dateCellStyle, CellStyle dateCellStyle2,
                                CaseReportFilterDto filter) {
        Sheet sheet = wbook.getSheetAt(5);

        QLabTest q = QLabTest.labTest;
        QCase qc = QCase.case$;

        JPAQuery<LabTest> query = new JPAQuery<LabTest>(em).distinct().from(q).innerJoin(q.theCase, qc);

        // predicates
        List<Predicate> predicates = new LinkedList<>();
        predicates.add(q.organization.id.longValue().in(filter.getActualOrganizations()));
        predicates.add(q.modifyDate.goe(filter.getFromDate()).and(q.modifyDate.loe(filter.getToDate())));
        predicates.add(q.testType.eq(ClinicalTestingType.CD4));
        predicates.add(q.resultDate.isNotNull());
        predicates.add(qc.deleted.isFalse());

        // Query
        query.where(predicates.toArray(new Predicate[0]));

        List<LabTest> entities = query.orderBy(qc.person.fullname.asc()).orderBy(q.sampleDate.desc()).fetch();

        int rowIndex = 1;

        for (LabTest r : entities) {
            Case c = r.getTheCase();
            int colIndex = 0;

            Row row = sheet.createRow(rowIndex++);
            row.setHeightInPoints(22);

            // Patient UUID
            Cell cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(c.getUid().toString());

            // Record UUID
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(r.getUid().toString());

            // HIVInfo ID
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isEmpty(c.getHivInfoID())) {
                cell.setCellValue(c.getHivInfoID());
            } else {
                cell.setCellValue("");
            }

            // Sample date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            cell.setCellValue(CommonUtils.fromLocalDateTime(r.getSampleDate()));

            // Test date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (r.getResultDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(r.getResultDate()));
            } else {
                cell.setCellValue("");
            }

            // Reason for testing
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(r.getReasonForTesting() == LabTestReason.CD4_BASELINE ? 1 : 2);

            // Result
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(r.getResultNumber());

            // Create date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle2);
            cell.setCellValue(CommonUtils.fromLocalDateTime(r.getCreateDate()));

            // Modified date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle2);
            cell.setCellValue(CommonUtils.fromLocalDateTime(r.getModifyDate()));

            // Co so lay mau
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isEmpty(r.getSampleSite(), true)) {
                cell.setCellValue(r.getSampleSite());
            } else {
                cell.setCellValue("-");
            }
        }
    }

    /**
     * Viral load worksheet
     *
     * @param wbook
     */
    private void createVLSheet(Workbook wbook, CellStyle cellStyle, CellStyle dateCellStyle, CellStyle dateCellStyle2,
                               CaseReportFilterDto filter) {
        Sheet sheet = wbook.getSheetAt(6);

        QLabTest q = QLabTest.labTest;
        QCase qc = QCase.case$;

        JPAQuery<LabTest> query = new JPAQuery<LabTest>(em).distinct().from(q).innerJoin(q.theCase, qc);

        // predicates
        List<Predicate> predicates = new LinkedList<>();
        predicates.add(q.organization.id.longValue().in(filter.getActualOrganizations()));
        predicates.add(q.modifyDate.goe(filter.getFromDate()).and(q.modifyDate.loe(filter.getToDate())));
        predicates.add(q.testType.eq(ClinicalTestingType.VIRAL_LOAD));
        predicates.add(q.resultDate.isNotNull());
        predicates.add(qc.deleted.isFalse());

        // Query
        query.where(predicates.toArray(new Predicate[0]));

        List<LabTest> entities = query.orderBy(qc.person.fullname.asc()).orderBy(q.sampleDate.desc()).fetch();

        int rowIndex = 1;

        for (LabTest r : entities) {

            Case c = r.getTheCase();
            int colIndex = 0;

            Row row = sheet.createRow(rowIndex++);
            row.setHeightInPoints(22);

            // Patient UUID
            Cell cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(c.getUid().toString());

            // Record UUID
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(r.getUid().toString());

            // HIVInfo ID
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isEmpty(c.getHivInfoID())) {
                cell.setCellValue(c.getHivInfoID());
            } else {
                cell.setCellValue("");
            }

            // Sample date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            cell.setCellValue(CommonUtils.fromLocalDateTime(r.getSampleDate()));

            // Test date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (r.getResultDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(r.getResultDate()));
            } else {
                cell.setCellValue("");
            }

            // Reason for testing
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(cellStyle);
            if (r.getReasonForTesting() != null) {
                cell.setCellValue(r.getReasonForTesting().getNumber() + 1);
            } else {
                cell.setCellValue("");
            }

            // Result
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(cellStyle);
            if (CommonUtils.isPositive(r.getResultNumber(), false)) {
                cell.setCellValue(r.getResultNumber());
            } else {
                cell.setCellValue("");
            }

            // Create date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle2);
            cell.setCellValue(CommonUtils.fromLocalDateTime(r.getCreateDate()));

            // Modified date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle2);
            cell.setCellValue(CommonUtils.fromLocalDateTime(r.getModifyDate()));

            // Co so lay mau
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isEmpty(r.getSampleSite(), true)) {
                cell.setCellValue(r.getSampleSite());
            } else {
                cell.setCellValue("-");
            }
        }
    }

    /**
     * Viral load worksheet
     *
     * @param wbook
     */
    private void createARVDRSheet(Workbook wbook, CellStyle cellStyle, CellStyle dateCellStyle,
                                  CellStyle dateCellStyle2, CaseReportFilterDto filter) {
        Sheet sheet = wbook.getSheetAt(7);

        QLabTest q = QLabTest.labTest;
        QCase qc = QCase.case$;

        JPAQuery<LabTest> query = new JPAQuery<LabTest>(em).distinct().from(q).innerJoin(q.theCase, qc);

        // predicates
        List<Predicate> predicates = new LinkedList<>();
        predicates.add(q.organization.id.longValue().in(filter.getActualOrganizations()));
        predicates.add(q.modifyDate.goe(filter.getFromDate()).and(q.modifyDate.loe(filter.getToDate())));
        predicates.add(q.testType.eq(ClinicalTestingType.ARV_DR));
        predicates.add(q.resultDate.isNotNull());
        predicates.add(qc.deleted.isFalse());

        // Query
        query.where(predicates.toArray(new Predicate[0]));

        List<LabTest> entities = query.orderBy(qc.person.fullname.asc()).orderBy(q.sampleDate.desc()).fetch();

        int rowIndex = 1;

        for (LabTest r : entities) {

            Case c = r.getTheCase();
            int colIndex = 0;

            Row row = sheet.createRow(rowIndex++);
            row.setHeightInPoints(22);

            // Patient UUID
            Cell cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(c.getUid().toString());

            // Record UUID
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(r.getUid().toString());

            // HIVInfo ID
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isEmpty(c.getHivInfoID())) {
                cell.setCellValue(c.getHivInfoID());
            } else {
                cell.setCellValue("");
            }

            // Sample date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            cell.setCellValue(CommonUtils.fromLocalDateTime(r.getSampleDate()));

            // Test date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (r.getResultDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(r.getResultDate()));
            } else {
                cell.setCellValue("");
            }

            // Result
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isEmpty(r.getResultText())) {
                cell.setCellValue(r.getResultText().replace("$$", ", "));
            } else {
                if (r.getResultDate() != null) {
                    cell.setCellValue("Không kháng thuốc ARV");
                } else {
                    cell.setCellValue("");
                }
            }

            // Create date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle2);
            cell.setCellValue(CommonUtils.fromLocalDateTime(r.getCreateDate()));

            // Modified date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle2);
            cell.setCellValue(CommonUtils.fromLocalDateTime(r.getModifyDate()));

            // Co so lay mau
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isEmpty(r.getSampleSite(), true)) {
                cell.setCellValue(r.getSampleSite());
            } else {
                cell.setCellValue("-");
            }
        }

    }

    /**
     * Recency worksheet - No content need to be generated
     *
     * @param wbook
     */
    private void createRecencySheet(Workbook wbook, CellStyle cellStyle, CellStyle dateCellStyle,
                                    CellStyle dateCellStyle2, CaseReportFilterDto filter) {
    }

    /**
     * TB prophylaxis worksheet
     *
     * @param wbook
     */
    private void createTBProSheet(Workbook wbook, CellStyle cellStyle, CellStyle dateCellStyle,
                                  CellStyle dateCellStyle2, CaseReportFilterDto filter) {
        Sheet sheet = wbook.getSheetAt(9);

        QTBProphylaxis q = QTBProphylaxis.tBProphylaxis;
        QCase qc = QCase.case$;

        JPAQuery<TBProphylaxis> query = new JPAQuery<TBProphylaxis>(em).distinct().from(q).innerJoin(q.theCase, qc);

        // predicates
        List<Predicate> predicates = new LinkedList<>();
        predicates.add(q.organization.id.longValue().in(filter.getActualOrganizations()));
        predicates.add(q.modifyDate.goe(filter.getFromDate()).and(q.modifyDate.loe(filter.getToDate())));
        predicates.add(qc.deleted.isFalse());

        // Query
        query.where(predicates.toArray(new Predicate[0]));

        List<TBProphylaxis> entities = query.orderBy(qc.person.fullname.asc()).fetch();

        int rowIndex = 1;

        for (TBProphylaxis r : entities) {
            Case c = r.getTheCase();
            int colIndex = 0;

            Row row = sheet.createRow(rowIndex++);
            row.setHeightInPoints(22);

            // Patient UUID
            Cell cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(c.getUid().toString());

            // Record UUID
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(r.getUid().toString());

            // HIVInfo ID
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isEmpty(c.getHivInfoID())) {
                cell.setCellValue(c.getHivInfoID());
            } else {
                cell.setCellValue("");
            }

            // Start date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (r.getStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(r.getStartDate()));
            } else {
                cell.setCellValue("");
            }

            // End date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (r.getEndDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(r.getEndDate()));
            } else {
                cell.setCellValue("");
            }

            // Complete?
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(r.getResult() == 3 ? 1 : 0);

            // Create date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle2);
            cell.setCellValue(CommonUtils.fromLocalDateTime(r.getCreateDate()));

            // Modified date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle2);
            cell.setCellValue(CommonUtils.fromLocalDateTime(r.getModifyDate()));
        }
    }

    /**
     * TB diagnosis and treatment worksheet
     *
     * @param wbook
     */
    private void createTBTxSheet(Workbook wbook, CellStyle cellStyle, CellStyle dateCellStyle, CellStyle dateCellStyle2,
                                 CaseReportFilterDto filter) {
        Sheet sheet = wbook.getSheetAt(10);

        QTBTreatment q = QTBTreatment.tBTreatment;
        QCase qc = QCase.case$;

        JPAQuery<TBTreatment> query = new JPAQuery<TBTreatment>(em).distinct().from(q).innerJoin(q.theCase, qc);

        // predicates
        List<Predicate> predicates = new LinkedList<>();
        predicates.add(q.organization.id.longValue().in(filter.getActualOrganizations()));
        predicates.add(q.modifyDate.goe(filter.getFromDate()).and(q.modifyDate.loe(filter.getToDate())));
        predicates.add(qc.deleted.isFalse());

        // Query
        query.where(predicates.toArray(new Predicate[0]));

        List<TBTreatment> entities = query.orderBy(qc.person.fullname.asc()).fetch();

        int rowIndex = 1;

        for (TBTreatment r : entities) {

            Case c = r.getTheCase();
            int colIndex = 0;

            Row row = sheet.createRow(rowIndex++);
            row.setHeightInPoints(22);

            // Patient UUID
            Cell cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(c.getUid().toString());

            // Record UUID
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(r.getUid().toString());

            // HIVInfo ID
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isEmpty(c.getHivInfoID())) {
                cell.setCellValue(c.getHivInfoID());
            } else {
                cell.setCellValue("");
            }

            // Has TB?
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(r.getDiagnoseDate() != null ? 1 : 0);

            // Diagnosis date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (r.getDiagnoseDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(r.getDiagnoseDate()));
            } else {
                cell.setCellValue("");
            }

            // Started treatment?
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(r.getTxStartDate() != null ? 1 : 0);

            // Tx Start date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (r.getTxStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(r.getTxStartDate()));
            } else {
                cell.setCellValue("");
            }

            // Tx End date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (r.getTxEndDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(r.getTxEndDate()));
            } else {
                cell.setCellValue("");
            }

            // TB treatment facility name
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isEmpty(r.getFacilityName())) {
                cell.setCellValue(r.getFacilityName());
            } else {
                cell.setCellValue("");
            }

            // Create date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle2);
            cell.setCellValue(CommonUtils.fromLocalDateTime(r.getCreateDate()));

            // Modified date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle2);
            cell.setCellValue(CommonUtils.fromLocalDateTime(r.getModifyDate()));
        }
    }

    /**
     * Hep worksheet
     *
     * @param wbook
     */
    private void createHepSheet(Workbook wbook, CellStyle cellStyle, CellStyle dateCellStyle, CellStyle dateCellStyle2,
                                CaseReportFilterDto filter) {
        Sheet sheet = wbook.getSheetAt(11);

        QHepatitis q = QHepatitis.hepatitis;
        QCase qc = QCase.case$;

        JPAQuery<Hepatitis> query = new JPAQuery<Hepatitis>(em).distinct().from(q).innerJoin(q.theCase, qc);

        // predicates
        List<Predicate> predicates = new LinkedList<>();
        predicates.add(q.organization.id.longValue().in(filter.getActualOrganizations()));
        predicates.add(q.modifyDate.goe(filter.getFromDate()).and(q.modifyDate.loe(filter.getToDate())));
        predicates.add(qc.deleted.isFalse());

        // Query
        query.where(predicates.toArray(new Predicate[0]));

        List<Hepatitis> entities = query.orderBy(qc.person.fullname.asc()).fetch();

        int rowIndex = 1;

        for (Hepatitis r : entities) {

            Case c = r.getTheCase();
            int colIndex = 0;

            Row row = sheet.createRow(rowIndex++);
            row.setHeightInPoints(22);

            // Patient UUID
            Cell cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(c.getUid().toString());

            // Record UUID
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(r.getUid().toString());

            // HIVInfo ID
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isEmpty(c.getHivInfoID())) {
                cell.setCellValue(c.getHivInfoID());
            } else {
                cell.setCellValue("");
            }

            // hep type
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(cellStyle);
            if (r.getTestType() == ClinicalTestingType.HEP_B) {
                cell.setCellValue("B");
            } else if (r.getTestType() == ClinicalTestingType.HEP_C) {
                cell.setCellValue("C");
            } else {
                cell.setCellValue("");
            }

            // Test date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (r.getTestDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(r.getTestDate()));
            } else {
                cell.setCellValue("");
            }

            // Test result
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(r.isTestPositive() ? 1 : 2);

            // On treatment?
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(r.isOnTreatment() ? 1 : 2);

            // Tx Start date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (r.getTxStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(r.getTxStartDate()));
            } else {
                cell.setCellValue("");
            }

            // Tx End date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (r.getTxEndDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(r.getTxEndDate()));
            } else {
                cell.setCellValue("");
            }

            // Create date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle2);
            cell.setCellValue(CommonUtils.fromLocalDateTime(r.getCreateDate()));

            // Modified date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle2);
            cell.setCellValue(CommonUtils.fromLocalDateTime(r.getModifyDate()));
        }
    }

    /**
     * MMT worksheet
     *
     * @param wbook
     */
    private void createMMTSheet(Workbook wbook, CellStyle cellStyle, CellStyle dateCellStyle, CellStyle dateCellStyle2,
                                CaseReportFilterDto filter) {
        Sheet sheet = wbook.getSheetAt(12);

        QMMT q = QMMT.mMT;
        QCase qc = QCase.case$;

        JPAQuery<MMT> query = new JPAQuery<MMT>(em).distinct().from(q).innerJoin(q.theCase, qc);

        // predicates
        List<Predicate> predicates = new LinkedList<>();
        predicates.add(q.organization.id.longValue().in(filter.getActualOrganizations()));
        predicates.add(q.modifyDate.goe(filter.getFromDate()).and(q.modifyDate.loe(filter.getToDate())));
        predicates.add(qc.deleted.isFalse());

        // Query
        query.where(predicates.toArray(new Predicate[0]));

        List<MMT> entities = query.orderBy(qc.person.fullname.asc()).fetch();

        int rowIndex = 1;

        for (MMT r : entities) {

            Case c = r.getTheCase();
            int colIndex = 0;

            Row row = sheet.createRow(rowIndex++);
            row.setHeightInPoints(22);

            // Patient UUID
            Cell cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(c.getUid().toString());

            // Record UUID
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(r.getUid().toString());

            // HIVInfo ID
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isEmpty(c.getHivInfoID())) {
                cell.setCellValue(c.getHivInfoID());
            } else {
                cell.setCellValue("");
            }

            // Start date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (r.getStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(r.getStartDate()));
            } else {
                cell.setCellValue("");
            }

            // End date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (r.getEndDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(r.getEndDate()));
            } else {
                cell.setCellValue("");
            }

            // Treatment facility name
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(r.getFacilityName());

            // Create date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle2);
            cell.setCellValue(CommonUtils.fromLocalDateTime(r.getCreateDate()));

            // Modified date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle2);
            cell.setCellValue(CommonUtils.fromLocalDateTime(r.getModifyDate()));
        }
    }

    /**
     * Shi worksheet
     *
     * @param wbook
     */
    private void createShiSheet(Workbook wbook, CellStyle cellStyle, CellStyle dateCellStyle, CellStyle dateCellStyle2,
                                CaseReportFilterDto filter) {
        Sheet sheet = wbook.getSheetAt(13);

        QShiInterview q = QShiInterview.shiInterview;
        QCase qc = QCase.case$;

        JPAQuery<ShiInterview> query = new JPAQuery<ShiInterview>(em).distinct().from(q).innerJoin(q.theCase, qc);

        // predicates
        List<Predicate> predicates = new LinkedList<>();
        predicates.add(q.organization.id.longValue().in(filter.getActualOrganizations()));
        predicates.add(q.modifyDate.goe(filter.getFromDate()).and(q.modifyDate.loe(filter.getToDate())));
        predicates.add(qc.deleted.isFalse());

        // Query
        query.where(predicates.toArray(new Predicate[0]));

        List<ShiInterview> entities = query.orderBy(qc.person.fullname.asc()).fetch();

        int rowIndex = 1;

        for (ShiInterview r : entities) {

            if (CommonUtils.isEmpty(r.getShiCardNumber())) {
                continue;
            }

            Case c = r.getTheCase();
            int colIndex = 0;

            Row row = sheet.createRow(rowIndex++);
            row.setHeightInPoints(22);

            // Patient UUID
            Cell cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(c.getUid().toString());

            // Record UUID
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(r.getUid().toString());

            // HIVInfo ID
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isEmpty(c.getHivInfoID())) {
                cell.setCellValue(c.getHivInfoID());
            } else {
                cell.setCellValue("");
            }

            // SHI card number
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isEmpty(r.getShiCardNumber())) {
                cell.setCellValue(r.getShiCardNumber());
            } else {
                cell.setCellValue("");
            }

            // Expiration date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (r.getShiExpiryDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(r.getShiExpiryDate()));
            } else {
                cell.setCellValue("");
            }

            // Create date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle2);
            cell.setCellValue(CommonUtils.fromLocalDateTime(r.getCreateDate()));

            // Modified date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle2);
            cell.setCellValue(CommonUtils.fromLocalDateTime(r.getModifyDate()));
        }
    }

    /**
     * Pregnancy worksheet
     *
     * @param wbook
     */
    private void createPregnancySheet(Workbook wbook, CellStyle cellStyle, CellStyle dateCellStyle,
                                      CellStyle dateCellStyle2, CaseReportFilterDto filter) {
        Sheet sheet = wbook.getSheetAt(14);

        QPregnancy q = QPregnancy.pregnancy;
        QCase qc = QCase.case$;

        JPAQuery<Pregnancy> query = new JPAQuery<Pregnancy>(em).distinct().from(q).innerJoin(q.theCase, qc);

        // predicates
        List<Predicate> predicates = new LinkedList<>();
        predicates.add(q.organization.id.longValue().in(filter.getActualOrganizations()));
        predicates.add(q.modifyDate.goe(filter.getFromDate()).and(q.modifyDate.loe(filter.getToDate())));
        predicates.add(qc.deleted.isFalse());

        // Query
        query.where(predicates.toArray(new Predicate[0]));

        List<Pregnancy> entities = query.orderBy(qc.person.fullname.asc()).fetch();

        int rowIndex = 1;

        for (Pregnancy r : entities) {

            Case c = r.getTheCase();
            int colIndex = 0;

            Row row = sheet.createRow(rowIndex++);
            row.setHeightInPoints(22);

            // Patient UUID
            Cell cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(c.getUid().toString());

            // Record UUID
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(r.getUid().toString());

            // HIVInfo ID
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isEmpty(c.getHivInfoID())) {
                cell.setCellValue(c.getHivInfoID());
            } else {
                cell.setCellValue("");
            }

            // Last menstrual date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (r.getLastMenstrualPeriod() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(r.getLastMenstrualPeriod()));
            } else {
                cell.setCellValue("");
            }

            // Pregnancy result
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(cellStyle);
            switch (r.getPregResult()) {
                case STILLBIRTH:
                    cell.setCellValue(1);
                    break;
                case GAVEBIRTH:
                    cell.setCellValue(2);
                    break;
                case MISCARRIAGE:
                    cell.setCellValue(3);
                    break;
                case ABORTION:
                    cell.setCellValue(4);
                    break;
                case UNKNOWN:
                    cell.setCellValue(5);
                    break;
            }

            // Birth date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (r.getChildDob() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(r.getChildDob()));
            } else {
                cell.setCellValue("");
            }

            // Is child get prophylaxis?
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(cellStyle);
            if (CommonUtils.isPositive(r.getChildProphylaxis(), true)) {
                if (r.getChildProphylaxis() == 1) {
                    cell.setCellValue(1);
                } else if (r.getChildProphylaxis() == 2) {
                    cell.setCellValue(0);
                }
            } else {
                cell.setCellValue("");
            }

            // Child HIV test date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (r.getChildDiagnosedDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(r.getChildDiagnosedDate()));
            } else {
                cell.setCellValue("");
            }

            // Child HIV test result
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(r.getChildHIVStatus());

            // Is child initiated on ART
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(r.isChildInitiatedOnART() ? 1 : 0);

            // ID of OPC for child
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue("");

            // Name of OPC for child
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(r.getChildOpc());

            // Create date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle2);
            cell.setCellValue(CommonUtils.fromLocalDateTime(r.getCreateDate()));

            // Modified date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle2);
            cell.setCellValue(CommonUtils.fromLocalDateTime(r.getModifyDate()));
        }
    }

    /**
     * Dead worksheet
     *
     * @param wbook
     */
    private void createDeadSheet(Workbook wbook, CellStyle cellStyle, CellStyle dateCellStyle, CellStyle dateCellStyle2,
                                 CaseReportFilterDto filter) {
        Sheet sheet = wbook.getSheetAt(15);

        QCaseOrg q = QCaseOrg.caseOrg;
        QCase qc = QCase.case$;

        JPAQuery<CaseOrg> query = new JPAQuery<CaseOrg>(em).distinct().from(q).innerJoin(q.theCase, qc);

        // predicates
        List<Predicate> predicates = new LinkedList<>();
        predicates.add(q.organization.id.longValue().in(filter.getActualOrganizations()));
        predicates.add(q.modifyDate.goe(filter.getFromDate()).and(q.modifyDate.loe(filter.getToDate())));
        predicates.add(q.status.eq(PatientStatus.DEAD).and(q.endDate.isNotNull()));
        predicates.add(qc.deleted.isFalse());

        // Query
        query.where(predicates.toArray(new Predicate[0]));

        List<CaseOrg> entities = query.orderBy(qc.person.fullname.asc()).orderBy(q.endDate.desc()).fetch();

        int rowIndex = 1;

        for (CaseOrg r : entities) {
            Case c = r.getTheCase();

            int colIndex = 0;

            Row row = sheet.createRow(rowIndex++);
            row.setHeightInPoints(22);

            // Patient UUID
            Cell cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(c.getUid().toString());

            // Record UUID
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(r.getUid().toString());

            // HIVInfo ID
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isEmpty(c.getHivInfoID())) {
                cell.setCellValue(c.getHivInfoID());
            } else {
                cell.setCellValue("");
            }

            // Dead date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            cell.setCellValue(CommonUtils.fromLocalDateTime(r.getEndDate()));

            // Dead reason ID
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(cellStyle);
            cell.setCellValue("");

            // Dead reason
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(r.getEndingReason());

            // Create date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle2);
            cell.setCellValue(CommonUtils.fromLocalDateTime(r.getCreateDate()));

            // Modified date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle2);
            cell.setCellValue(CommonUtils.fromLocalDateTime(r.getModifyDate()));
        }
    }
}
