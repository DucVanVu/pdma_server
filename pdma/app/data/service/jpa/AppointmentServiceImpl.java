package org.pepfar.pdma.app.data.service.jpa;

import com.google.common.collect.Lists;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pepfar.pdma.app.Constants;
import org.pepfar.pdma.app.data.domain.*;
import org.pepfar.pdma.app.data.dto.*;
import org.pepfar.pdma.app.data.repository.*;
import org.pepfar.pdma.app.data.service.AppointmentService;
import org.pepfar.pdma.app.data.service.CaseService;
import org.pepfar.pdma.app.data.types.ClinicalTestingType;
import org.pepfar.pdma.app.data.types.PatientStatus;
import org.pepfar.pdma.app.data.types.Permission;
import org.pepfar.pdma.app.utils.AuthorizationUtils;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.ExcelUtils;
import org.pepfar.pdma.app.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private AppointmentRepository repos;

    @Autowired
    private CaseRepository caseRepos;

    @Autowired
    private OrganizationRepository orgRepos;

    @Autowired
    private ClinicalStageRepository csRepos;

    @Autowired
    private LabTestRepository labRepos;

    @Autowired
    private HepatitisRepository hepRepos;

    @Autowired
    private ShiInterviewRepository shiRepos;

    @Autowired
    private MMDispensingRepository mmdRepos;

    @Autowired
    private AuthorizationUtils authorUtils;

    @Autowired
    private EntityManager em;

    @Autowired
    private AuthorizationUtils authUtils;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private CaseService caseService;

    private Workbook blankBook;

//	@Autowired
//	private TBTreatment2Repository tBTreatment2Repository;
//
//	@Autowired
//	private TBProphylaxis2Repository tBProphylaxis2Repository;

    @Override
    @Transactional(readOnly = true)
    public AppointmentDto findById(Long id) {

        if (!CommonUtils.isPositive(id, true)) {
            return null;
        }

        Appointment entity = repos.findOne(id);
        AppointmentDto dto = null;

        if (entity != null) {
            dto = new AppointmentDto(entity);

            // calculate late days since the appointment after the latest arrival date
            int lateDays = repos.countLateDays(dto.getTheCase().getId(), dto.getOrganization().getId()).intValue();
            dto.setLateDays(lateDays);

            Case c = entity.getTheCase();

            // --------------------------------------------------------------------------
            // Other data associated with the current encounter
            // --------------------------------------------------------------------------

            if (dto.getArrivalDate() != null) {

                // Clinical stage associated with the arrival date
                ClinicalStageDto csDto = queryForClinicalStage(c, dto.getArrivalDate());
                if (csDto != null) {
                    csDto.setTheCase(null);
                    csDto.setOrganization(null);
                }
                dto.setClinicalStage(csDto);

                // Viral load test associated with the arrival date
                if (CommonUtils.isTrue(dto.getVlTested())) {
                    LabTestDto vlTest = queryForLabTest(c, dto.getArrivalDate(), ClinicalTestingType.VIRAL_LOAD);
                    dto.setVlTest(vlTest);
                }

                // CD4 test associated with the arrival date
                if (CommonUtils.isTrue(dto.getCd4Tested())) {
                    LabTestDto cd4Test = queryForLabTest(c, dto.getArrivalDate(), ClinicalTestingType.CD4);
                    dto.setCd4Test(cd4Test);
                }

                // Hepatitis data associated with the arrival date
                if (CommonUtils.isTrue(dto.getHepScreened())) {
                    HepatitisDto hepData = queryForHepData(c, dto.getArrivalDate());
                    dto.setHep(hepData);
                }

                // SHI data associated with the arrival date
                ShiInterviewDto shiData = queryForShiData(c, dto.getArrivalDate());
                dto.setShi(shiData);
            }

            // in case SHI data associated with the arrival date is null, get the last SHI
            // data for pre-populate in the entry form
            LocalDateTime cutpoint = dto.getAppointmentDate();
            if (dto.getArrivalDate() != null) {
                cutpoint = dto.getArrivalDate();
            }
            ShiInterviewDto prevShiData = queryForPrevShiData(c, cutpoint);
            dto.setPrevShi(prevShiData);

            // --------------------------------------------------------------------------
            // Previous encounter: Looking for the previous appointment encounter (arrived
            // appointment before
            // this appointment)
            // --------------------------------------------------------------------------
            Appointment lastArrivedAppointment = null;
            Iterator<Appointment> apps = c.getAppointments().iterator();

            while (apps.hasNext()) {
                Appointment app = apps.next();
                if (app.getArrivalDate() != null
                        && CommonUtils.dateDiff(ChronoUnit.DAYS, app.getArrivalDate(), dto.getAppointmentDate()) > 0) {
                    lastArrivedAppointment = app;
                    break;
                }
            }

            if (lastArrivedAppointment != null) {

                // Drug source set for previous appointment encounter
                dto.setPrevDrugSource(lastArrivedAppointment.getDrugSource());

                // MMD information in the previous appointment encounter
                LocalDateTime cutpointDate = dto.getAppointmentDate();
                if (dto.getArrivalDate() != null) {
                    cutpointDate = dto.getArrivalDate();
                }

                MMDispensingDto prevMmd = queryForPrevMmdData(c, cutpointDate);
                dto.setPrevMmdEval(prevMmd);
            }

        }

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentCalendarEventDto> findAll4Calendar(AppointmentFilterDto filter) {
        if (filter == null || filter.getOrganization() == null
                || !CommonUtils.isPositive(filter.getOrganization().getId(), true) || filter.getFromDate() == null
                || filter.getToDate() == null) {
            return new ArrayList<>();
        }

        List<Long> grantedOrgIds = authorUtils.getGrantedOrgIds(Permission.READ_ACCESS);
        if (CommonUtils.isEmpty(grantedOrgIds)
                || !grantedOrgIds.contains(filter.getOrganization().getId().longValue())) {
            return new ArrayList<>();
        }

        // Adjust the from/to dates
        LocalDateTime fromDate = LocalDateTime.of(filter.getFromDate().getYear(), filter.getFromDate().getMonthValue(),
                filter.getFromDate().getDayOfMonth(), 0, 0, 0);
        LocalDateTime toDate = LocalDateTime.of(filter.getToDate().getYear(), filter.getToDate().getMonthValue(),
                filter.getToDate().getDayOfMonth(), 23, 59, 59);

        return repos.getCalendarEvents(fromDate, toDate, filter.getOrganization().getId().longValue());
    }

    @Override
    //@Transactional(readOnly = true)
    public CustomList<AppointmentDto> findAll(AppointmentFilterDto filter) {

        if (filter == null) {
            throw new IllegalArgumentException();
        }

        if (filter.getFromDate() == null || filter.getToDate() == null || filter.getOrganization() == null
                || !CommonUtils.isPositive(filter.getOrganization().getId(), true)) {
            return new CustomList<>();
        }

        return getAppointmentsForDates(filter);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomList<AppointmentDto> findLateAppointments(AppointmentFilterDto filter) {
        if (filter == null || filter.getFromDate() == null || filter.getToDate() == null
                || filter.getOrganization() == null
                || !CommonUtils.isPositive(filter.getOrganization().getId(), true)) {
            return new CustomList<>();
        }

        List<Long> grantedOrgIds = authorUtils.getGrantedOrgIds(Permission.READ_ACCESS);
        if (CommonUtils.isEmpty(grantedOrgIds)
                || !grantedOrgIds.contains(filter.getOrganization().getId().longValue())) {
            return new CustomList<>();
        }

        if (filter.getPageIndex() < 0) {
            filter.setPageIndex(0);
        }

        if (filter.getPageSize() <= 0) {
            filter.setPageSize(25);
        }

        final List<AppointmentDto> content = new ArrayList<>();
        long totalElements = 0;

        int offset = filter.getPageIndex() * filter.getPageSize();
        int limit = filter.getPageSize();

        Timestamp cutpoint = CommonUtils.toTimestamp(CommonUtils.hanoiTodayEnd().minusDays(1));

        totalElements = repos.countLateAppointments(filter.getOrganization().getId(), filter.getKeyword(),
                filter.getLateDays(), cutpoint).longValue();
        repos.findLateAppointments(filter.getOrganization().getId(), filter.getKeyword(), filter.getSortField(),
                filter.getLateDays(), cutpoint, offset, limit).forEach(e -> {
            content.add(new AppointmentDto(e));
        });

        // count of appointments for the selected date
        Timestamp fromDate = CommonUtils.toTimestamp(CommonUtils.dateStart(filter.getFromDate()));
        Timestamp toDate = CommonUtils.toTimestamp(CommonUtils.dateEnd(filter.getToDate()));
        long extraCount = repos
                .countAppointments4Date(filter.getOrganization().getId(), filter.getKeyword(), fromDate, toDate)
                .longValue();

        return new CustomList<>(content, extraCount, totalElements);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> findAppointments4OneCase(AppointmentFilterDto filter) {
        if (filter == null || filter.getTheCase() == null || !CommonUtils.isPositive(filter.getTheCase().getId(), true)
                || filter.getFromDate() == null || filter.getToDate() == null) {
            return new ArrayList<>();
        }

        return repos
                .findAppointmentsInDateRange(filter.getFromDate(), filter.getToDate(), filter.getTheCase().getId(),
                        filter.getOrganization().getId())
                .parallelStream().map(e -> new AppointmentDto(e)).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> findAllAppointments4OneCase(Long caseId) {

        List<Long> grantedOrgIds = authorUtils.getGrantedOrgIds(Permission.READ_ACCESS);

        if (!CommonUtils.isPositive(caseId, true) || CommonUtils.isEmpty(grantedOrgIds)) {
            return new ArrayList<>();
        }

        QAppointment q = QAppointment.appointment;
        BooleanExpression be = q.id.isNotNull();

        be = q.organization.id.longValue().in(grantedOrgIds);
        be = be.and(q.theCase.id.longValue().eq(caseId));

        List<AppointmentDto> list = new ArrayList<>();
        repos.findAll(be, q.appointmentDate.desc()).forEach(e -> {
            list.add(new AppointmentDto(e));
        });

        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public Workbook exportLatePatients(AppointmentFilterDto filter) {
        if (filter == null) {
            filter = new AppointmentFilterDto();
        }

        return createExcelWorkbook4LatePatients(filter);
    }

    @Override
    @Transactional(readOnly = true)
    public Workbook exportDailyAppointments(AppointmentFilterDto filter) {
        Workbook blankBook = new XSSFWorkbook();
        blankBook.createSheet();

        if (filter == null || filter.getOrganization() == null
                || !CommonUtils.isPositive(filter.getOrganization().getId(), true) || filter.getFromDate() == null
                || filter.getToDate() == null) {
            return blankBook;
        }

        List<AppointmentDto> list = getAppointmentsForDates(filter).getContent();

        Organization org = orgRepos.findOne(filter.getOrganization().getId());
        if (org == null) {
            return blankBook;
        }

        Workbook wbook = null;
        try (InputStream template = context.getResource("classpath:templates/daily-appointment-template.xlsx")
                .getInputStream()) {
            XSSFWorkbook tmp = new XSSFWorkbook(template);
            Sheet sheet = tmp.getSheetAt(0);

            // title
            String title = "";
            String subTitle = "Ngày xuất dữ liệu: "
                    + CommonUtils.hanoiToday().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            if (org.getAddress() != null) {
                AdminUnit au = org.getAddress().getProvince();

                if (au != null) {
                    title += au.getName();
                    title += " - ";
                }
            }

            title += org.getName();
            title += ": ";
            title += "Lịch hẹn tái khám theo ngày";

            ExcelUtils.createAndWriteInCell(sheet, 0, 1, title, 30, 16, true);
            ExcelUtils.createAndWriteInCell(sheet, 1, 1, subTitle, 22, 12, false);

            wbook = new SXSSFWorkbook(tmp, 100);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (wbook == null) {
            return blankBook;
        }

        Sheet sheet = wbook.getSheetAt(0);

        Font font = wbook.createFont();
        CellStyle cellStyle = wbook.createCellStyle();

        font.setFontName("Times New Roman");
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.BLACK.getIndex());

        cellStyle.setFont(font);
        cellStyle.setWrapText(true);
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        ExcelUtils.setBorders4Style(cellStyle);

        CellStyle dateCellStyle = wbook.createCellStyle();
        DataFormat format = wbook.createDataFormat();

        dateCellStyle.cloneStyleFrom(cellStyle);
        dateCellStyle.setDataFormat(format.getFormat("dd/MM/yyyy"));
        ExcelUtils.setBorders4Style(dateCellStyle);

        // Fill out patient data
        int rowIndex = 5;

        for (AppointmentDto dto : list) {
            int colIndex = 0;
            CaseDto theCase = dto.getTheCase();

            if (theCase == null || theCase.getPerson() == null) {
                continue; // something wrong happened to this patient
            }

            Row row = sheet.createRow(rowIndex++);

            CaseOrgDto currentCO = null; // latest case-org with the org in the granted org list
            Iterator<CaseOrgDto> caseOrgs = theCase.getCaseOrgs().iterator();

            while (caseOrgs.hasNext()) {
                CaseOrgDto co = caseOrgs.next();

                if (org.getId().longValue() == co.getOrganization().getId().longValue()
                        && co.getStatus() != PatientStatus.PENDING_ENROLLMENT
                        && co.getStatus() != PatientStatus.CANCELLED_ENROLLMENT) {
                    currentCO = co;
                    break;
                }
            }

            if (currentCO == null) {
                continue; // something wrong happened to this patient
            }

            // Patient ID
            Cell cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(theCase.getId());

            // Patient chart ID
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(currentCO.getPatientChartId());

            // Full name
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(theCase.getPerson().getFullname());

            // ARV start date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getArvStartDate()));

            // Appointment date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            cell.setCellValue(CommonUtils.fromLocalDateTime(dto.getAppointmentDate()));

            // Status
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (dto.getArrivalDate() == null) {
                if (CommonUtils.isTrue(dto.getMissed())) {
                    cell.setCellValue("Bỏ khám");
                } else {
                    cell.setCellValue("Chưa tới khám");
                }
            } else {
                long dateDiff = CommonUtils.dateDiff(ChronoUnit.DAYS, dto.getAppointmentDate(), dto.getArrivalDate());
                if (dateDiff == 0) {
                    cell.setCellValue("Tái khám đúng hẹn");
                } else if (dateDiff > 0) {
                    cell.setCellValue("Tái khám muộn");
                } else {
                    cell.setCellValue("Tái khám trước hẹn");
                }
            }

            // Arrival date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (dto.getArrivalDate() == null) {
                cell.setCellValue("-");
            } else {
                cell.setCellValue(dto.getArrivalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }

            // Next appointment date
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (dto.getNextAppointmentDate() == null) {
                cell.setCellValue("-");
            } else {
                cell.setCellValue(dto.getNextAppointmentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }

            // Clinical stage
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (dto.getClinicalStage() == null) {
                cell.setCellValue("-");
            } else {
                cell.setCellValue("GĐLS " + dto.getClinicalStage().getStage());
            }

            // TB screening result
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isPositive(dto.getTbScreenResult(), true)) {
                cell.setCellValue("-");
            } else if (dto.getTbScreenResult() == 1) {
                cell.setCellValue("Dương tính");
            } else {
                cell.setCellValue("Âm tính");
            }

            // Phác đồ ARV được sử dụng
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (dto.getArrivalDate() != null && !CommonUtils.isEmpty(dto.getArvRegimenName(), true)) {
                cell.setCellValue(dto.getArvRegimenName());
            } else {
                cell.setCellValue("-");
            }

            // Số ngày thuốc được cấp ở lần này
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(cellStyle);
            if (dto.getArrivalDate() != null && CommonUtils.isPositive(dto.getDrugDays(), false)) {
                cell.setCellValue(dto.getDrugDays());
            } else {
                cell.setCellValue("-");
            }

            // Nguồn thuốc ARV được cấp ở lần này
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (dto.getArrivalDate() != null && dto.getDrugSource() != null) {
                cell.setCellValue(dto.getDrugSource().toString());
            } else {
                cell.setCellValue("-");
            }

            // Cấp thuốc nhiều tháng
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (dto.getArrivalDate() != null && dto.getMmdEval() != null) {

                MMDispensingDto mmd = dto.getMmdEval();

                if (mmd != null && CommonUtils.isPositive(mmd.getId(), true)) {
                    if (CommonUtils.isTrue(mmd.getOnMmd())) {
                        cell.setCellValue("Bệnh nhân đang được cấp nhiều tháng.");
                    } else if (CommonUtils.isTrue(mmd.getEligible())) {
                        cell.setCellValue("Bệnh nhân được đánh giá ổn định.");
                    } else {
                        cell.setCellValue("Bệnh nhân chưa ổn định.");
                    }
                } else {
                    cell.setCellValue("Không có thông tin đánh giá.");
                }
            } else {
                cell.setCellValue("-");
            }

            // Xét nghiệm TLVR ở lần khám này
            if (dto.getArrivalDate() != null && dto.getVlTest() != null) {
                LabTestDto test = dto.getVlTest();

                // Lý do xét nghiệm
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(test.getReasonForTesting().toString());

                // Kết quả xét nghiệm
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (test.getResultDate() != null && CommonUtils.isPositive(test.getResultNumber(), false)) {
                    if (test.getResultNumber() == 0) {
                        cell.setCellValue("Không phát hiện");
                    } else {
                        cell.setCellValue(test.getResultNumber() + " bản sao/ml");
                    }
                } else {
                    cell.setCellValue("-");
                }

                // Ngày có kết quả
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                if (test.getResultDate() != null) {
                    cell.setCellValue(test.getResultDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                } else {
                    cell.setCellValue("-");
                }
            } else {
                for (int i = 0; i < 3; i++) {
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue("-");
                }
            }

            // Xét nghiệm CD4 ở lần khám này
            if (dto.getArrivalDate() != null && dto.getCd4Test() != null) {
                LabTestDto test = dto.getCd4Test();

                // Lý do xét nghiệm
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(test.getReasonForTesting().toString());

                // Kết quả xét nghiệm
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (test.getResultDate() != null && !CommonUtils.isEmpty(test.getResultText(), true)) {
                    cell.setCellValue(test.getResultText());
                } else {
                    cell.setCellValue("-");
                }

                // Ngày có kết quả
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                if (test.getResultDate() != null) {
                    cell.setCellValue(test.getResultDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                } else {
                    cell.setCellValue("-");
                }
            } else {
                for (int i = 0; i < 3; i++) {
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue("-");
                }
            }

            // Xét nghiệm sàng lọc viêm gan ở lần khám này
            if (dto.getArrivalDate() != null && dto.getHep() != null) {
                HepatitisDto test = dto.getHep();

                // Loại xét nghiệm
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (test.getTestType() != null) {
                    if (test.getTestType() == ClinicalTestingType.HEP_B) {
                        cell.setCellValue("Xét nghiệm HbsAg");
                    } else if (test.getTestType() == ClinicalTestingType.HEP_C) {
                        cell.setCellValue("Xét nghiệm Anti-HepC");
                    } else {
                        cell.setCellValue(test.getTestType().toString());
                    }
                } else {
                    cell.setCellValue("-");
                }

                // Kết quả xét nghiệm
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(test.isTestPositive() ? "Dương tính" : "Âm tính");

                // Ngày có kết quả
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                if (test.getTestDate() != null) {
                    cell.setCellValue(test.getTestDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                } else {
                    cell.setCellValue("-");
                }
            } else {
                for (int i = 0; i < 3; i++) {
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue("-");
                }
            }

            // Thông tin BHYT ở lần khám này
            if (dto.getArrivalDate() != null && dto.getShi() != null) {
                ShiInterviewDto shi = dto.getShi();

                // Mã thẻ BHYT
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (!CommonUtils.isEmpty(shi.getShiCardNumber(), true)) {
                    cell.setCellValue(shi.getShiCardNumber());
                } else {
                    cell.setCellValue("-");
                }

                // Ngày hết hạn
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                if (shi.getShiExpiryDate() != null) {
                    cell.setCellValue(shi.getShiExpiryDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                } else {
                    cell.setCellValue("-");
                }
            } else {
                for (int i = 0; i < 2; i++) {
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue("-");
                }
            }
        }

        // Hide ID columns
        sheet.setColumnHidden(0, true);

        // Auto-filter
        if (rowIndex >= 5) {
            sheet.setAutoFilter(CellRangeAddress.valueOf("A5:Y" + rowIndex));
        }

        return wbook;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppointmentDto saveOne(AppointmentDto dto) {

        if (dto == null || dto.getOrganization() == null || dto.getTheCase() == null
                || !CommonUtils.isPositive(dto.getOrganization().getId(), true)
                || !CommonUtils.isPositive(dto.getTheCase().getId(), true) || dto.getAppointmentDate() == null) {
            throw new RuntimeException();
        }

        // adjust appointment date and arrival date
        LocalDateTime appDate = dto.getAppointmentDate();
        LocalDateTime arrDate = dto.getArrivalDate();
        LocalDateTime nextAppDate = dto.getNextAppointmentDate();

        if (appDate != null) {
            dto.setAppointmentDate(CommonUtils.dateStart(appDate));
        }

        if (arrDate != null) {
            dto.setArrivalDate(CommonUtils.dateStart(arrDate));
        }

        if (nextAppDate != null) {
            dto.setNextAppointmentDate(CommonUtils.dateStart(nextAppDate));
        }

        // Moving on..
        Case theCase = caseRepos.findOne(dto.getTheCase().getId());
        Organization organization = orgRepos.findOne(dto.getOrganization().getId());
        if (theCase == null || organization == null) {
            throw new RuntimeException();
        }

        // check if the case is still ACTIVE
        CaseOrgDto currentCo = dto.getCurrentCaseOrg();
        PatientStatus curStatus = currentCo != null ? currentCo.getStatus() : null;

//        Iterator<CaseOrg> cos = theCase.getCaseOrgs().iterator();
//        while (cos.hasNext()) {
//            CaseOrg co = cos.next();
//            if (co.getOrganization().getId() == organization.getId().longValue()) {
//                curStatus = co.getStatus();
//                break;
//            }
//        }

        List<PatientStatus> acceptedStatus = Lists.newArrayList(PatientStatus.ACTIVE, PatientStatus.DEAD, PatientStatus.LTFU, PatientStatus.TRANSFERRED_OUT);

        if (curStatus == null || !acceptedStatus.contains(curStatus)) {
            throw new RuntimeException();
        }

        List<Long> grantedOrgIds = authorUtils.getGrantedOrgIds(Permission.WRITE_ACCESS);
        if (CommonUtils.isEmpty(grantedOrgIds) || !grantedOrgIds.contains(organization.getId().longValue())) {
            throw new RuntimeException();
        }

        // mark missed appointment
        repos.updateMissedAppointment(theCase.getId(), organization.getId());

        Appointment entity = null;

        if (CommonUtils.isPositive(dto.getId(), true)) {
            entity = repos.findOne(dto.getId());
        }

        if (entity == null) {
            // OK. Create a new appointment, but first
            // try to look for one of the same appointment date
            LocalDateTime fromDate = CommonUtils.dateStart(dto.getAppointmentDate());
            LocalDateTime toDate = CommonUtils.dateEnd(dto.getAppointmentDate());

            List<Appointment> appointments = repos.findAppointmentsInDateRange(fromDate, toDate, theCase.getId(),
                    organization.getId());
            if (appointments != null && appointments.size() > 0) {
                entity = appointments.get(0);
            }
        }

        if (entity == null) {
            entity = dto.toEntity();
            entity.setUid(UUID.randomUUID());
            entity.setTheCase(theCase);
            entity.setOrganization(organization);
        } else {
            entity.setAppointmentDate(dto.getAppointmentDate());
            entity.setArrived(dto.getArrived());
            entity.setArrivalDate(CommonUtils.isTrue(dto.getArrived()) ? dto.getArrivalDate() : null);
            entity.setDrugDispensed(dto.getArrived());

            entity.setVlTested(dto.getVlTested());
            entity.setCd4Tested(dto.getCd4Tested());
            entity.setArvDrTested(dto.getArvDrTested());
            entity.setHepScreened(dto.getHepScreened());
            entity.setShiChanged(dto.getShiChanged());

            // ARV drugs
            entity.setDrugDays(dto.getDrugDays());
            entity.setDrugSource(dto.getDrugSource());
            entity.setDrugSourceAlt(dto.getDrugSourceAlt());
            entity.setArvRegimenName(dto.getArvRegimenName());
            entity.setArvRegimenLine(dto.getArvRegimenLine());

            // TB screening
            entity.setTbScreenResult(dto.getTbScreenResult());

            // for MMD
            entity.setGoodAdherence(dto.getGoodAdherence());
            entity.setHasOI(dto.getHasOI());
            entity.setHasDrugAE(dto.getHasDrugAE());
            entity.setPregnant(dto.getPregnant());

            // next appointment date
            entity.setNextAppointmentDate(dto.getNextAppointmentDate());

            // Note
            entity.setNote(dto.getNote());
        }

        if (CommonUtils.isTrue(entity.getArrived())) {
            entity.setMissed(null);
        }

        entity = repos.save(entity);

        if (entity == null) {
            throw new RuntimeException();
        }

        // -------------------
        // Clinical stage
        // -------------------

        // Save clinical stage evaluation of the same day
        if (dto.getClinicalStage() != null && dto.getClinicalStage().getStage() > 0
                && entity.getArrivalDate() != null) {

            ClinicalStageDto csDto = dto.getClinicalStage();
            ClinicalStageDto csDto2 = queryForClinicalStage(theCase, entity.getArrivalDate());
            if (csDto2 != null) {
                // update just one of the records found. For the others, users need to manually
                // delete
                csDto2.setEvalDate(csDto.getEvalDate());
                csDto2.setStage(csDto.getStage());

                saveClinicalStage(csDto2, organization, theCase);
            } else {
                // create one
                saveClinicalStage(csDto, organization, theCase);
            }
        }

        // -------------------
        // Multi-month dispensing
        // -------------------
        if (entity.getArrivalDate() != null) {
            saveMmdRecord(entity);
        }

        // Should create next appointment?
        if (dto.isAutoGenNextAppointment()) {

            if (entity != null && dto.getArrivalDate() != null && dto.getNextAppointmentDate() != null
                    && CommonUtils.dateDiff(ChronoUnit.DAYS, dto.getAppointmentDate(), dto.getNextAppointmentDate()) > 0) {
                // @formatter:off
                // If there is any appointment after the arrival date of this appointment, then
                // skip.
                // Otherwise, create the appointment
                // @formatter:on

                LocalDateTime startDate = null;

                if (CommonUtils.dateDiff(ChronoUnit.DAYS, dto.getArrivalDate(), dto.getAppointmentDate()) > 0) {
                    startDate = dto.getAppointmentDate();
                } else {
                    startDate = dto.getArrivalDate();
                }

                startDate = CommonUtils.dateStart(startDate.plusDays(1));
                List<Appointment> appointments = repos.findAppointmentsInDateRange(startDate, null, theCase.getId(),
                        organization.getId());

                if (appointments == null || appointments.size() <= 0) {
                    // Create the next appointment
                    Appointment nextEntity = new Appointment();

                    nextEntity.setUid(UUID.randomUUID());
                    nextEntity.setAppointmentDate(dto.getNextAppointmentDate());
                    nextEntity.setTheCase(theCase);
                    nextEntity.setOrganization(organization);

                    nextEntity = repos.save(nextEntity);
                }
            }
        }

        return new AppointmentDto(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMissedAppointments(Long caseId, Long orgId) {
        repos.updateMissedAppointment(caseId, orgId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMultiple(AppointmentDto[] dtos) {
        if (CommonUtils.isEmpty(dtos)) {
            return;
        }

        List<Long> grantedOrgIds = authorUtils.getGrantedOrgIds(Permission.DELETE_ACCESS);

        for (AppointmentDto dto : dtos) {
            if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
                continue;
            }

            Appointment entity = repos.findOne(dto.getId());

            if (entity != null) {

                // only deleteable if the entry is not marked as arrived
                if (CommonUtils.isTrue(entity.getArrived())) {
                    return;
                }

                if (grantedOrgIds.contains(entity.getOrganization().getId().longValue())) {
                    repos.delete(entity);
                }
            }
        }
    }

    /**
     * Get list of appointments for a specific date range
     *
     * @param filter
     * @return
     */
    private CustomList<AppointmentDto> getAppointmentsForDates(AppointmentFilterDto filter) {

        if (filter == null || filter.getOrganization() == null
                || !CommonUtils.isPositive(filter.getOrganization().getId(), true) || filter.getFromDate() == null
                || filter.getToDate() == null) {
            return new CustomList<>();
        }

        List<Long> grantedOrgIds = authorUtils.getGrantedOrgIds(Permission.READ_ACCESS);
        if (CommonUtils.isEmpty(grantedOrgIds)
                || !grantedOrgIds.contains(filter.getOrganization().getId().longValue())) {
            return new CustomList<>();
        }

        // Adjust the from/to dates
        Timestamp fromDate = CommonUtils.toTimestamp(CommonUtils.dateStart(filter.getFromDate()));
        Timestamp toDate = CommonUtils.toTimestamp(CommonUtils.dateEnd(filter.getToDate()));
        Timestamp nextAppDate = new Timestamp(fromDate.getTime() + (24 * 60 * 60 * 1000));

        CustomList<AppointmentDto> ret = new CustomList<>();
        List<AppointmentDto> content = new ArrayList<>();
//        List<AppointmentDto> nextApp = new ArrayList<>();
//
//		repos.findNextAppointments4Date(filter.getOrganization().getId(), filter.getKeyword(), filter.getSortField(),
//				nextAppDate, null).forEach(e -> {
//					nextApp.add(new AppointmentDto(e));
//				});

        repos.findAppointments4Date(filter.getOrganization().getId(), filter.getKeyword(), filter.getSortField(),
                fromDate, toDate).forEach(e -> {
//					if (nextApp != null && nextApp.size() > 0) {
//						LocalDateTime minAppointmentDate = null;
//						for (AppointmentDto appointmentDto : nextApp) {
//							if (e.getTheCase() != null && e.getTheCase().getId() != null
//									&& appointmentDto.getTheCase() != null
//									&& appointmentDto.getTheCase().getId() != null
//									&& appointmentDto.getTheCase().getId().equals(e.getTheCase().getId())) {
//								if (appointmentDto.getAppointmentDate() != null && (minAppointmentDate == null
//										|| minAppointmentDate.isAfter(appointmentDto.getAppointmentDate()))) {
//									minAppointmentDate = appointmentDto.getAppointmentDate();
//								}
//							}
//						}
//						e.setNextAppointmentDate(minAppointmentDate);
//					}
            content.add(new AppointmentDto(e));
        });

        // Count late appointments
//        Timestamp cutpoint = CommonUtils.toTimestamp(CommonUtils.hanoiTodayEnd().minusDays(1));
//        long extraCount = repos.countLateAppointments(filter.getOrganization().getId(), filter.getKeyword(),
//                filter.getLateDays(), cutpoint).longValue();

//        ret.setExtraCount(extraCount);
        ret.setContent(content);

        return ret;
    }

    /**
     * Create patient worksheet for patients late to appointment
     *
     * @param filter
     * @return
     */
    private Workbook createExcelWorkbook4LatePatients(AppointmentFilterDto filter) {
        User user = SecurityUtils.getCurrentUser();
        boolean confidentialRequired = false;

        for (Role r : user.getRoles()) {
            if (r.getName().equalsIgnoreCase(Constants.ROLE_ADMIN) || r.getName().equalsIgnoreCase(Constants.ROLE_DONOR)
                    || r.getName().equalsIgnoreCase(Constants.ROLE_NATIONAL_MANAGER)) {
                confidentialRequired = true;
            }
        }

        Workbook blankBook = new XSSFWorkbook();
        blankBook.createSheet();

        List<Long> orgIds = authUtils.getGrantedOrgIds(Permission.READ_ACCESS);

        if (CommonUtils.isEmpty(orgIds) || filter == null || filter.getLateDays() <= 0) {
            return blankBook;
        }

        Workbook wbook = null;
        try (InputStream template = context.getResource("classpath:templates/late-appointment-template.xlsx")
                .getInputStream()) {
            wbook = new XSSFWorkbook(template);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (wbook == null) {
            return blankBook;
        }

        Sheet patientSheet = wbook.getSheetAt(0);

        QCase qc = QCase.case$;
        QCaseOrg qco = QCaseOrg.caseOrg;
        QAppointment qa = QAppointment.appointment;

        JPAQuery<Appointment> query = new JPAQuery<Appointment>(em).from(qa).innerJoin(qa.theCase, qc)
                .innerJoin(qa.theCase.caseOrgs, qco);

        LocalDateTime todayEnd = CommonUtils.hanoiTodayStart();

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(qa.organization.id.longValue().in(orgIds));
        predicates.add(qa.appointmentDate.loe(todayEnd.minusDays(filter.getLateDays())));
        predicates.add(qa.arrived.isNull().or(qa.arrived.isFalse()));

        predicates.add(qc.deleted.isFalse());
        predicates.add(qco.current.isTrue());
        predicates.add(qco.latestRelationship.isTrue());
        predicates.add(qco.refTrackingOnly.isFalse());
        predicates.add(qco.status.eq(PatientStatus.ACTIVE));

        query = query.where(predicates.toArray(new Predicate[0]));

        List<Appointment> appointments = query.orderBy(qa.appointmentDate.asc()).fetch();

        createPatientWorksheet4LatePatients(appointments, patientSheet, wbook, orgIds, confidentialRequired);

        return wbook;
    }

    /**
     * Create list of patients late to appointments
     *
     * @param cases
     * @param sheet
     * @param wbook
     * @param grantedOrgIds
     * @param confidential
     */
    private void createPatientWorksheet4LatePatients(List<Appointment> appointments, Sheet sheet, Workbook wbook,
                                                     List<Long> grantedOrgIds, boolean confidential) {

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

        int rowIndex = 2;
        int colIndex = 0;

        Row row = null;
        Cell cell = null;

        // Start filling out data...
        for (Appointment entity : appointments) {
            Case theCase = entity.getTheCase();
            Organization currentOrg = entity.getOrganization();

            if (theCase == null) {
                continue;
            }

            colIndex = 0;

            row = sheet.createRow(rowIndex++);
            row.setHeightInPoints(22);

            // Mã UUID
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(theCase.getUid().toString());

            // Tỉnh - thành phố của cơ sở
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);

            CaseOrg currentCO = null; // latest case-org with the org in the granted org list
            Iterator<CaseOrg> caseOrgs = theCase.getCaseOrgs().iterator();
            while (caseOrgs.hasNext()) {
                CaseOrg co = caseOrgs.next();

                if (currentOrg.getId().longValue() == co.getOrganization().getId().longValue()) {
                    currentCO = co;
                    break;
                }
            }

            if (currentOrg != null && currentOrg.getAddress() != null
                    && currentOrg.getAddress().getProvince() != null) {
                cell.setCellValue(currentOrg.getAddress().getProvince().getName());
            } else {
                cell.setCellValue("-");
            }

            // Cơ sở điều trị
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (currentOrg != null) {
                cell.setCellValue(currentOrg.getName());
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

            // Ngày khám cuối
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            List<LocalDateTime> latestArrivedDates = repos.findLatestArrivedAppointments(theCase.getId(),
                    currentOrg.getId());
            if (latestArrivedDates != null && latestArrivedDates.size() > 0) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(latestArrivedDates.get(0)));
            } else {
                cell.setCellValue("-");
            }

            // Ngày hẹn khám
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            cell.setCellValue(CommonUtils.fromLocalDateTime(entity.getAppointmentDate()));

            // Số ngày muộn khám
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(
                    CommonUtils.dateDiff(ChronoUnit.DAYS, entity.getAppointmentDate(), CommonUtils.hanoiTodayStart()));

            // Mã bệnh án
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (currentCO.getPatientChartId() != null) {
                cell.setCellValue(currentCO.getPatientChartId());
            } else {
                cell.setCellValue("-");
            }

            // Họ tên bệnh nhân
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (confidential) {
                cell.setCellValue("-");
            } else {
                cell.setCellValue(confidential ? "-" : theCase.getPerson().getFullname());
            }

            // Giới tính
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(theCase.getPerson().getGender().toString());

            // Ngày sinh
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getPerson().getDob()));
            cell.setCellStyle(dateCellStyle);

            // Điện thoại liên lạc
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(theCase.getPerson().getMobilePhone());

            // Addresses
            Location rAddress = null;
            Location cAddress = null;

            Set<Location> locs = theCase.getPerson().getLocations();
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
                // R address - details
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (!CommonUtils.isEmpty(rAddress.getStreetAddress())) {
                    cell.setCellValue(confidential ? "-" : rAddress.getStreetAddress());
                } else {
                    cell.setCellValue("-");
                }

                // R address - commune
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (rAddress.getCommune() != null) {
                    cell.setCellValue(confidential ? "-" : rAddress.getCommune().getName());
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
                    cell.setCellValue(confidential ? "-" : cAddress.getStreetAddress());
                } else {
                    cell.setCellValue("-");
                }

                // C address - commune
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (cAddress.getCommune() != null) {
                    cell.setCellValue(confidential ? "-" : cAddress.getCommune().getName());
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
            cell.setCellValue(!CommonUtils.isEmpty(theCase.getConfirmLabName()) ? theCase.getConfirmLabName() : "-");

            // Ngày bắt đầu ARV
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (theCase.getArvStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getArvStartDate()));
            } else {
                cell.setCellValue("-");
            }

            // Ngày bắt đầu ARV tại cơ sở hiện tại
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (currentCO.getArvStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(currentCO.getArvStartDate()));
            } else {
                cell.setCellValue("-");
            }

            // Phác đồ thuốc hiện tại
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(
                    !CommonUtils.isEmpty(theCase.getCurrentArvRegimenName()) ? theCase.getCurrentArvRegimenName()
                            : "-");

            // Ngày bắt đầu phác đồ hiện tại
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (theCase.getCurrentArvRegimenStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getCurrentArvRegimenStartDate()));
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

            // Tình trạng bệnh nhân
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue((currentCO.getStatus() != null) ? currentCO.getStatus().toString() : "-");

            // Ngày thay đổi tình trạng
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (currentCO.getStatus() == PatientStatus.ACTIVE) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(currentCO.getStartDate()));
            } else if (currentCO.getEndDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(currentCO.getEndDate()));
            }
        }

        // Hide ID columns
        sheet.setColumnHidden(0, true);
        sheet.setColumnHidden(3, true);

        // Auto-filter
        if (rowIndex >= 2) {
            sheet.setAutoFilter(CellRangeAddress.valueOf("A2:AB" + rowIndex));
        }
    }

    /**
     * Create/update a clinical stage record
     *
     * @param dto
     * @return
     */
    private ClinicalStageDto saveClinicalStage(ClinicalStageDto dto, Organization organization, Case theCase) {

        if (dto.getEvalDate() == null || dto.getStage() <= 0) {
            return null;
        }

        ClinicalStage cs = null;

        if (dto.getId() != null) {
            cs = csRepos.findOne(dto.getId());
        }

        if (cs == null) {
            cs = dto.toEntity();
            cs.setUid(UUID.randomUUID());
        } else {
            cs.setStage(dto.getStage());
            cs.setEvalDate(dto.getEvalDate());
        }

        cs.setOrganization(organization);
        cs.setTheCase(theCase);

        cs = csRepos.save(cs);

        if (cs != null) {

            // Update the case if this is latest record
            Case ce = caseRepos.findOne(theCase.getId());
            Iterator<ClinicalStage> itr = ce.getWhoStages().iterator();

            if (itr.hasNext()) {
                ClinicalStage _cs = itr.next();

                if (_cs.getId().longValue() == cs.getId()) {
                    ce.setWhoStage(cs.getStage());
                    ce.setWhoStageEvalDate(cs.getEvalDate());

                    ce = caseRepos.save(ce);
                }
            }

            return new ClinicalStageDto(cs);
        } else {
            throw new RuntimeException();
        }
    }

    /**
     * Query for clinical stages +- 5 days from arrival date
     *
     * @param theCase
     * @param arrivalDate
     * @return
     */
    private ClinicalStageDto queryForClinicalStage(Case theCase, LocalDateTime arrivalDate) {
        if (theCase == null || !CommonUtils.isPositive(theCase.getId(), true) || arrivalDate == null) {
            return null;
        }

        LocalDateTime fromDate = CommonUtils.dateStart(arrivalDate);
        LocalDateTime toDate = CommonUtils.dateEnd(arrivalDate);

        List<ClinicalStage> list = csRepos.getByEvalDate(theCase.getId(), fromDate, toDate);

        if (list != null && list.size() > 0) {
            return new ClinicalStageDto(list.get(0));
        }

        return null;
    }

    /**
     * Query for lab tests of a patient based on the arrival date for an appointment
     *
     * @param theCase
     * @param arrivalDate
     * @param testType
     * @return
     */
    private LabTestDto queryForLabTest(Case theCase, LocalDateTime arrivalDate, ClinicalTestingType testType) {
        if (theCase == null || !CommonUtils.isPositive(theCase.getId(), true) || arrivalDate == null) {
            return null;
        }

        LocalDateTime fromDate = CommonUtils.dateStart(arrivalDate);
        LocalDateTime toDate = CommonUtils.dateEnd(arrivalDate);

        List<LabTest> list = labRepos.findBySampleDate(theCase.getId(), fromDate, toDate, testType);

        if (list != null && list.size() > 0) {
            LabTestDto dto = new LabTestDto(list.get(0));
            dto.setTheCase(null);
            dto.setOrganization(null);

            return dto;
        }

        return null;
    }

    /**
     * Find hep tests in the range of +-5 days in relative to arrival date
     *
     * @param theCase
     * @param arrivalDate
     * @return
     */
    private HepatitisDto queryForHepData(Case theCase, LocalDateTime arrivalDate) {
        if (theCase == null || !CommonUtils.isPositive(theCase.getId(), true) || arrivalDate == null) {
            return null;
        }

        // find hep tests in the range of +-5 days in relative to arrival date
        LocalDateTime fromDate = CommonUtils.dateStart(arrivalDate.minusDays(5));
        LocalDateTime toDate = CommonUtils.dateEnd(arrivalDate.plusDays(5));

        List<Hepatitis> list = hepRepos.findByTestDate(theCase.getId(), fromDate, toDate);

        if (list != null && list.size() > 0) {
            HepatitisDto dto = new HepatitisDto(list.get(0));
            dto.setTheCase(null);
            dto.setOrganization(null);

            return dto;
        }

        return null;
    }

    /**
     * Find SHI interview data on the date of arrival
     *
     * @param theCase
     * @param arrivalDate
     * @return
     */
    private ShiInterviewDto queryForShiData(Case theCase, LocalDateTime arrivalDate) {
        if (theCase == null || !CommonUtils.isPositive(theCase.getId(), true) || arrivalDate == null) {
            return null;
        }

        List<ShiInterview> list = shiRepos.findInstance(theCase.getId(), arrivalDate);

        if (list != null && list.size() > 0) {
            ShiInterviewDto dto = new ShiInterviewDto(list.get(0));
            dto.setTheCase(null);
            dto.setOrganization(null);

            return dto;
        }

        return null;
    }

    /**
     * Query for the previous SHI data
     *
     * @param theCase
     * @param arrivalDate
     * @return
     */
    private ShiInterviewDto queryForPrevShiData(Case theCase, LocalDateTime arrivalDate) {
        if (theCase == null || !CommonUtils.isPositive(theCase.getId(), true) || arrivalDate == null) {
            return null;
        }

        List<ShiInterview> list = shiRepos.findPreviousInstance(theCase.getId(), arrivalDate);

        if (list != null && list.size() > 0) {
            ShiInterviewDto dto = new ShiInterviewDto(list.get(0));
            dto.setTheCase(null);
            dto.setOrganization(null);

            return dto;
        }

        return null;
    }

    /**
     * Query for previous MMD data (in compare with the current encounter)
     *
     * @param theCase
     * @param arrivalDate
     * @return
     */
    private MMDispensingDto queryForPrevMmdData(Case theCase, LocalDateTime arrivalDate) {
        if (theCase == null || !CommonUtils.isPositive(theCase.getId(), true) || arrivalDate == null) {
            return null;
        }

        List<MMDispensing> list = mmdRepos.findPreviousInstance(theCase.getId(), arrivalDate);

        if (list != null && list.size() > 0) {
            MMDispensingDto dto = new MMDispensingDto(list.get(0));
            dto.setTheCase(null);
            dto.setOrganization(null);

            return dto;
        }

        return null;
    }

    /**
     * Query for MMD data
     *
     * @param theCase
     * @param arrivalDate
     * @return
     */
    private MMDispensingDto queryForMmdData(Case theCase, LocalDateTime arrivalDate) {

        if (theCase == null || !CommonUtils.isPositive(theCase.getId(), true) || arrivalDate == null) {
            return null;
        }

        LocalDateTime fromDate = CommonUtils.dateStart(arrivalDate);
        LocalDateTime toDate = CommonUtils.dateEnd(arrivalDate);

        List<MMDispensing> list = mmdRepos.findInstance(theCase.getId(), fromDate, toDate);

        if (list != null && list.size() > 0) {
            MMDispensingDto dto = new MMDispensingDto(list.get(0));
            dto.setTheCase(null);
            dto.setOrganization(null);

            return dto;
        }

        return null;
    }

    /**
     * Save MMD record
     *
     * @param entity Appointment entity
     * @return
     */
    private MMDispensingDto saveMmdRecord(Appointment entity) {

        if (entity == null || entity.getArrivalDate() == null || entity.getOrganization() == null
                || entity.getTheCase() == null || !CommonUtils.isPositive(entity.getId(), true)
                || !CommonUtils.isPositive(entity.getOrganization().getId(), true)
                || !CommonUtils.isPositive(entity.getTheCase().getId(), true)) {
            return null;
        }

        Case theCase = entity.getTheCase();
        Organization organization = entity.getOrganization();

        if (theCase == null || organization == null) {
            throw new RuntimeException();
        }

        MMDispensing mmd = null;

        // MMD of the current encounter
        MMDispensingDto mmdDto = queryForMmdData(theCase, entity.getArrivalDate());
        if (mmdDto != null && CommonUtils.isPositive(mmdDto.getId(), true)) {
            mmd = mmdDto.toEntity();
        } else {
            mmd = new MMDispensing();
            mmd.setUid(UUID.randomUUID());
        }

        // MMD of the previous encounter
        MMDispensingDto prevMmdDto = queryForPrevMmdData(theCase, entity.getArrivalDate());

        LocalDateTime selectedCutpoint = null;
        if (entity.getArrivalDate() != null) {
            selectedCutpoint = entity.getArrivalDate();
        } else {
            selectedCutpoint = entity.getAppointmentDate();
        }

        Timestamp cutpoint = CommonUtils.toTimestamp(selectedCutpoint);

        // query for hard eligibility
        boolean isAdult = CommonUtils.dateDiff(ChronoUnit.YEARS, theCase.getPerson().getDob(), selectedCutpoint) >= 15;
        boolean isVlLt50CD4Ge200_350 = caseRepos.checkVlLt50CD4Ge200_350(Lists.newArrayList(theCase.getId()), cutpoint)
                .size() > 0;
        boolean isOnARVGt6Months = caseRepos
                .checkOnARVGt6Months(organization.getId(), Lists.newArrayList(theCase.getId()), cutpoint).size() > 0;

        mmd.setAdult(isAdult);
        mmd.setVlLt200(isVlLt50CD4Ge200_350);
        mmd.setArvGt12Month(isOnARVGt6Months);

        // Soft eligibility
        boolean goodAdherence = CommonUtils.boolValue(entity.getGoodAdherence());
        boolean hasOI = CommonUtils.boolValue(entity.getHasOI());
        boolean hasDrugAE = CommonUtils.boolValue(entity.getHasDrugAE());
        boolean pregnant = CommonUtils.boolValue(entity.getPregnant());

        mmd.setGoodAdherence(goodAdherence);
        mmd.setNoOIs(!hasOI);
        mmd.setNoDrugAdvEvent(!hasDrugAE);
        mmd.setNoPregnancy(!pregnant);

        if (!isAdult || !isVlLt50CD4Ge200_350 || !isOnARVGt6Months || !goodAdherence || hasOI || hasDrugAE || pregnant) {
            mmd.setEligible(false);
        } else {
            mmd.setEligible(true);
        }

        // Evaluation date
        mmd.setEvaluationDate(entity.getArrivalDate());

        // On MMD?
        if (!mmd.isEligible()) {
            mmd.setOnMmd(false);
            mmd.setStopReason(null);
        } else {
            if (prevMmdDto != null && CommonUtils.isTrue(prevMmdDto.getOnMmd())) {
                mmd.setOnMmd(prevMmdDto.getOnMmd());
            }
        }

        // Other info
        mmd.setOrganization(entity.getOrganization());
        mmd.setTheCase(entity.getTheCase());

        // appointment ID
        mmd.setAppointmentId(entity.getId());

        // Forcefully
        if (entity.getDrugDays() >= 84) {
            mmd.setEligible(true);
            mmd.setOnMmd(true);
        } else {
            mmd.setOnMmd(false);
        }

        mmd = mmdRepos.save(mmd);

        if (mmd != null) {
            return new MMDispensingDto(mmd);
        } else {
            throw new RuntimeException();
        }
    }

    public static final class CustomList<T> {

        private long extraCount;

        private List<T> content;

        private long totalElements;

        public CustomList() {
            this.extraCount = 0;
            this.content = new ArrayList<>();
            this.totalElements = 0;
        }

        public CustomList(List<T> content, long extraCount) {
            this.content = content;
            this.extraCount = extraCount;
        }

        public CustomList(List<T> content, long extraCount, long totalElements) {
            this.content = content;
            this.extraCount = extraCount;
            this.totalElements = totalElements;
        }

        public long getExtraCount() {
            return extraCount;
        }

        public void setExtraCount(long extraCount) {
            this.extraCount = extraCount;
        }

        public List<T> getContent() {
            return content;
        }

        public void setContent(List<T> content) {
            this.content = content;
        }

        public long getTotalElements() {
            return totalElements;
        }

        public void setTotalElements(long totalElements) {
            this.totalElements = totalElements;
        }

    }

    @Override
    public Workbook exportLateAppointments(AppointmentFilterDto filter) {
        User user = SecurityUtils.getCurrentUser();
        boolean confidential = false;

        for (Role r : user.getRoles()) {
            if (r.getName().equalsIgnoreCase(Constants.ROLE_ADMIN) || r.getName().equalsIgnoreCase(Constants.ROLE_DONOR)
                    || r.getName().equalsIgnoreCase(Constants.ROLE_NATIONAL_MANAGER)) {
                confidential = true;
            }
        }

        List<Appointment> list = findListLateAppointments(filter);

        Workbook wbook = null;
        try (InputStream template = context.getResource("classpath:templates/late-appointment-template.xlsx")
                .getInputStream()) {
            XSSFWorkbook tmp = new XSSFWorkbook(template);
            Sheet sheet = tmp.getSheetAt(0);
            ExcelUtils.createAndWriteInCell(sheet, 0, 1, "", 30, 16, true);
            ExcelUtils.createAndWriteInCell(sheet, 1, 1, "", 22, 12, false);
            wbook = new SXSSFWorkbook(tmp, 100);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (wbook == null) {
            Workbook blankBook = new XSSFWorkbook();
            blankBook.createSheet();

            return blankBook;
        }

        Sheet sheet = wbook.getSheetAt(0);

        Font font = wbook.createFont();
        CellStyle cellStyle = wbook.createCellStyle();

        font.setFontName("Times New Roman");
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.BLACK.getIndex());

        cellStyle.setFont(font);
        cellStyle.setWrapText(true);
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        ExcelUtils.setBorders4Style(cellStyle);

        CellStyle dateCellStyle = wbook.createCellStyle();
        DataFormat format = wbook.createDataFormat();

        dateCellStyle.cloneStyleFrom(cellStyle);
        dateCellStyle.setDataFormat(format.getFormat("dd/MM/yyyy"));
        ExcelUtils.setBorders4Style(dateCellStyle);

        // Fill out patient data
        int rowIndex = 2;
        int colIndex = 0;

        Row row = null;
        Cell cell = null;

        // Start filling out data...
        for (Appointment entity : list) {
            Case theCase = entity.getTheCase();
            Organization currentOrg = entity.getOrganization();

            if (theCase == null) {
                continue;
            }

            colIndex = 0;
            row = sheet.createRow(rowIndex++);
            row.setHeightInPoints(22);

            // Mã UUID
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (theCase.getUid() != null)
                cell.setCellValue(theCase.getUid().toString());
            else {
                cell.setCellValue("-");
            }

            // Tỉnh - thành phố của cơ sở
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);

            CaseOrg currentCO = null; // latest case-org with the org in the granted org list
            Iterator<CaseOrg> caseOrgs = theCase.getCaseOrgs().iterator();
            while (caseOrgs.hasNext()) {
                CaseOrg co = caseOrgs.next();

                if (currentOrg.getId().longValue() == co.getOrganization().getId().longValue()) {
                    currentCO = co;
                    break;
                }
            }

            if (currentOrg != null && currentOrg.getAddress() != null
                    && currentOrg.getAddress().getProvince() != null) {
                cell.setCellValue(currentOrg.getAddress().getProvince().getName());
            } else {
                cell.setCellValue("-");
            }

            // Cơ sở điều trị
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (currentOrg != null) {
                cell.setCellValue(currentOrg.getName());
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

            // Ngày khám cuối
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            List<LocalDateTime> latestArrivedDates = repos.findLatestArrivedAppointments(theCase.getId(),
                    currentOrg.getId());
            if (latestArrivedDates != null && latestArrivedDates.size() > 0) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(latestArrivedDates.get(0)));
            } else {
                cell.setCellValue("-");
            }

            // Ngày hẹn khám
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            cell.setCellValue(CommonUtils.fromLocalDateTime(entity.getAppointmentDate()));

            // Số ngày muộn khám
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(
                    CommonUtils.dateDiff(ChronoUnit.DAYS, entity.getAppointmentDate(), CommonUtils.hanoiTodayStart()));

            // Mã bệnh án
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (currentCO.getPatientChartId() != null) {
                cell.setCellValue(currentCO.getPatientChartId());
            } else {
                cell.setCellValue("-");
            }

            // Họ tên bệnh nhân
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (confidential) {
                cell.setCellValue("-");
            } else {
                cell.setCellValue(confidential ? "-" : theCase.getPerson().getFullname());
            }

            // Giới tính
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(theCase.getPerson().getGender().toString());

            // Ngày sinh
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getPerson().getDob()));
            cell.setCellStyle(dateCellStyle);

            // Điện thoại liên lạc
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(theCase.getPerson().getMobilePhone());

            // Addresses
            Location rAddress = null;
            Location cAddress = null;

            Set<Location> locs = theCase.getPerson().getLocations();
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
                // R address - details
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (!CommonUtils.isEmpty(rAddress.getStreetAddress())) {
                    cell.setCellValue(confidential ? "-" : rAddress.getStreetAddress());
                } else {
                    cell.setCellValue("-");
                }

                // R address - commune
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (rAddress.getCommune() != null) {
                    cell.setCellValue(confidential ? "-" : rAddress.getCommune().getName());
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
                    cell.setCellValue(confidential ? "-" : cAddress.getStreetAddress());
                } else {
                    cell.setCellValue("-");
                }

                // C address - commune
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (cAddress.getCommune() != null) {
                    cell.setCellValue(confidential ? "-" : cAddress.getCommune().getName());
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
            cell.setCellValue(!CommonUtils.isEmpty(theCase.getConfirmLabName()) ? theCase.getConfirmLabName() : "-");

            // Ngày bắt đầu ARV
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (theCase.getArvStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getArvStartDate()));
            } else {
                cell.setCellValue("-");
            }

            // Ngày bắt đầu ARV tại cơ sở hiện tại
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (currentCO.getArvStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(currentCO.getArvStartDate()));
            } else {
                cell.setCellValue("-");
            }

            // Phác đồ thuốc hiện tại
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(
                    !CommonUtils.isEmpty(theCase.getCurrentArvRegimenName()) ? theCase.getCurrentArvRegimenName()
                            : "-");

            // Ngày bắt đầu phác đồ hiện tại
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (theCase.getCurrentArvRegimenStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getCurrentArvRegimenStartDate()));
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

            // Tình trạng bệnh nhân
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue((currentCO.getStatus() != null) ? currentCO.getStatus().toString() : "-");

            // Ngày thay đổi tình trạng
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (currentCO.getStatus() == PatientStatus.ACTIVE) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(currentCO.getStartDate()));
            } else if (currentCO.getEndDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(currentCO.getEndDate()));
            }
        }

        // Hide ID columns
        sheet.setColumnHidden(0, true);
        sheet.setColumnHidden(3, true);

        // Auto-filter
        if (rowIndex >= 2) {
            sheet.setAutoFilter(CellRangeAddress.valueOf("A2:AB" + rowIndex));
        }
        return wbook;
    }

    /**
     * Get list late - appointment
     */
    public List<Appointment> findListLateAppointments(AppointmentFilterDto filter) {
        if (filter == null || filter.getFromDate() == null || filter.getToDate() == null
                || filter.getOrganization() == null
                || !CommonUtils.isPositive(filter.getOrganization().getId(), true)) {
            return new ArrayList<>();
        }

        List<Long> grantedOrgIds = authorUtils.getGrantedOrgIds(Permission.READ_ACCESS);
        if (CommonUtils.isEmpty(grantedOrgIds)
                || !grantedOrgIds.contains(filter.getOrganization().getId().longValue())) {
            return new ArrayList<>();
        }

        List<Appointment> content = new ArrayList<Appointment>();
        Timestamp cutpoint = CommonUtils.toTimestamp(CommonUtils.hanoiTodayEnd().minusDays(1));

        content = repos.findListLateAppointments(filter.getOrganization().getId(), filter.getKeyword(),
                filter.getSortField(), filter.getLateDays(), cutpoint);

        return content;
    }

    @Override
    @Transactional(readOnly = true)
    public CustomList<CaseOrgDto> getPatientHasNoAppointment(AppointmentFilterDto filter) {

        if (filter == null) {
            throw new IllegalArgumentException();
        }

        if (filter.getPageIndex() < 0) {
            filter.setPageIndex(0);
        }

        if (filter.getPageSize() <= 0) {
            filter.setPageSize(25);
        }
        long totalElements = 0;
        int offset = filter.getPageIndex() * filter.getPageSize();
        int limit = filter.getPageSize();

        if (filter.getOrganization() == null
                || !CommonUtils.isPositive(filter.getOrganization().getId(), true)) {
            return new CustomList<>();
        }

        CustomList<CaseOrgDto> ret = new CustomList<>();
        List<Long> grantedOrgIds = authorUtils.getGrantedOrgIds(Permission.READ_ACCESS);
        final List<Long> writableOrgIds = authUtils.getGrantedOrgIds(Permission.WRITE_ACCESS);
        if (CommonUtils.isEmpty(grantedOrgIds)
                || !grantedOrgIds.contains(filter.getOrganization().getId().longValue())) {
            return new CustomList<>();
        }

        // Adjust the from/to dates
        Timestamp fromDate = CommonUtils.toTimestamp(CommonUtils.dateStart(filter.getFromDate()));
        Timestamp toDate = CommonUtils.toTimestamp(CommonUtils.dateEnd(filter.getToDate()));

        List<CaseOrgDto> content = new ArrayList<>();

        List<CaseOrg> datas = repos.findPatientHasNoAppointment(filter.getOrganization().getId(), filter.getKeyword(), filter.getSortField(), offset, limit);
        datas.forEach(e -> {
            CaseOrgDto dto = caseService.checkCaseOrgEditable(writableOrgIds, e, false);
            content.add(dto);
        });

        totalElements = repos.countPatientHasNoAppointment(filter.getOrganization().getId(), filter.getKeyword()).longValue();
        ret.setContent(content);
        ret.setTotalElements(totalElements);

        return ret;
    }

    @Override
    public Workbook exportPatientHasNoAppointment(AppointmentFilterDto filter) {

        // Query for patients
        //List<CaseOrg> contents = new ArrayList<>();
        List<CaseOrg> caseOrgs = repos.findPatientHasNoAppointment(filter.getOrganization().getId(), filter.getKeyword(), filter.getSortField(), 0, Integer.MAX_VALUE);

        Workbook wbook = null;
        String filename = "classpath:templates/danh-sach-benh-nhan-chua-co-lich-kham.xlsx";
        try (InputStream template = context.getResource(filename).getInputStream()) {

            wbook = new XSSFWorkbook(template);
            // Write title and period information

            //Sheet sheet = wbook.getSheetAt(0);

            //wbook = new SXSSFWorkbook(tmp, 100);
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
        Integer colIndex = 0;

        Timestamp fromDate = null;
        Timestamp toDate = null;
        String toDateString = "-";

        Row row = null;
        Cell cell = null;

        if (!CollectionUtils.isEmpty(caseOrgs)) {
            for (CaseOrg caseOrg : caseOrgs) {
                row = sheet.createRow(rowIndex++);
                if (row != null) {
                    colIndex = 0;

                    //tỉnh-thành phố
                    cell = row.createCell(colIndex++);
                    cell.setCellStyle(cellStyle);
                    try {
                        cell.setCellValue(caseOrg.getOrganization().getAddress().getProvince().getName());
                    } catch (Exception e) {
                        cell.setCellValue("");
                    }

                    //cơ sở điều trị
                    cell = row.createCell(colIndex++);
                    cell.setCellStyle(cellStyle);
                    try {
                        cell.setCellValue(caseOrg.getOrganization().getName());
                    } catch (Exception e) {
                        cell.setCellValue("");
                    }

                    //mã bệnh án
                    cell = row.createCell(colIndex++);
                    cell.setCellStyle(cellStyle);
                    try {
                        cell.setCellValue(caseOrg.getPatientChartId());
                    } catch (Exception e) {
                        cell.setCellValue("");
                    }
                    //họ tên
                    cell = row.createCell(colIndex++);
                    cell.setCellStyle(cellStyle);
                    try {
                        cell.setCellValue(caseOrg.getTheCase().getPerson().getFullname());
                    } catch (Exception e) {
                        cell.setCellValue("");
                    }
                    //giới tính
                    cell = row.createCell(colIndex++);
                    cell.setCellStyle(cellStyle);
                    try {
                        if (caseOrg.getTheCase().getPerson().getGender().toString().equalsIgnoreCase("MALE")) {
                            cell.setCellValue("Nam");
                        } else if (caseOrg.getTheCase().getPerson().getGender().toString().equalsIgnoreCase("FEMALE")) {
                            cell.setCellValue("Nữ");
                        } else {
                            cell.setCellValue("Không rõ");
                        }
                    } catch (Exception e) {
                        cell.setCellValue("");
                    }
                    //ngày sinh
                    cell = row.createCell(colIndex++);
                    cell.setCellStyle(dateCellStyle);
                    try {
                        cell.setCellValue(Date
                                .from(caseOrg.getTheCase().getPerson().getDob().atZone(ZoneId.systemDefault())
                                        .toInstant()));
                    } catch (Exception e) {
                        cell.setCellValue("");
                    }

                    //cmnd
                    cell = row.createCell(colIndex++);
                    cell.setCellStyle(cellStyle);
                    try {
                        cell.setCellValue(caseOrg.getTheCase().getPerson().getNidNumber());
                    } catch (Exception e) {
                        cell.setCellValue("");
                    }

                    //nghề nghiệp
                    cell = row.createCell(colIndex++);
                    cell.setCellStyle(cellStyle);
                    try {
                        cell.setCellValue(caseOrg.getTheCase().getPerson().getProfessional().getValue());
                    } catch (Exception e) {
                        cell.setCellValue("");
                    }

                    //nghề nghiệp
                    cell = row.createCell(colIndex++);
                    cell.setCellStyle(cellStyle);
                    try {
                        cell.setCellValue(caseOrg.getTheCase().getPerson().getEthnic().getValue());
                    } catch (Exception e) {
                        cell.setCellValue("");
                    }

                    //sdt
                    cell = row.createCell(colIndex++);
                    cell.setCellStyle(cellStyle);
                    try {
                        cell.setCellValue(caseOrg.getTheCase().getPerson().getMobilePhone());
                    } catch (Exception e) {
                        cell.setCellValue("");
                    }

                    //địa chỉ
                    cell = row.createCell(colIndex++);
                    cell.setCellStyle(cellStyle);
                    try {
                        for (Location lo : caseOrg.getTheCase().getPerson().getLocations()) {
                            if (lo.getAddressType().toString().equalsIgnoreCase("CURRENT_ADDRESS")) {
                                cell.setCellValue(lo.getStreetAddress());
                                break;
                            }
                        }

                    } catch (Exception e) {
                        cell.setCellValue("");
                    }

                    cell = row.createCell(colIndex++);
                    cell.setCellStyle(cellStyle);
                    try {
                        for (Location lo : caseOrg.getTheCase().getPerson().getLocations()) {
                            if (lo.getAddressType().toString().equalsIgnoreCase("CURRENT_ADDRESS")) {
                                cell.setCellValue(lo.getCommune().getName());
                                break;
                            }
                        }

                    } catch (Exception e) {
                        cell.setCellValue("");
                    }

                    cell = row.createCell(colIndex++);
                    cell.setCellStyle(cellStyle);
                    try {
                        for (Location lo : caseOrg.getTheCase().getPerson().getLocations()) {
                            if (lo.getAddressType().toString().equalsIgnoreCase("CURRENT_ADDRESS")) {
                                cell.setCellValue(lo.getDistrict().getName());
                                break;
                            }
                        }

                    } catch (Exception e) {
                        cell.setCellValue("");
                    }

                    cell = row.createCell(colIndex++);
                    cell.setCellStyle(cellStyle);
                    try {
                        for (Location lo : caseOrg.getTheCase().getPerson().getLocations()) {
                            if (lo.getAddressType().toString().equalsIgnoreCase("CURRENT_ADDRESS")) {
                                cell.setCellValue(lo.getProvince().getName());
                                break;
                            }
                        }

                    } catch (Exception e) {
                        cell.setCellValue("");
                    }

                    //địa chỉ
                    cell = row.createCell(colIndex++);
                    cell.setCellStyle(cellStyle);
                    try {
                        for (Location lo : caseOrg.getTheCase().getPerson().getLocations()) {
                            if (lo.getAddressType().toString().equalsIgnoreCase("RESIDENT_ADDRESS")) {
                                cell.setCellValue(lo.getStreetAddress());
                                break;
                            }
                        }

                    } catch (Exception e) {
                        cell.setCellValue("");
                    }

                    cell = row.createCell(colIndex++);
                    cell.setCellStyle(cellStyle);
                    try {
                        for (Location lo : caseOrg.getTheCase().getPerson().getLocations()) {
                            if (lo.getAddressType().toString().equalsIgnoreCase("RESIDENT_ADDRESS")) {
                                cell.setCellValue(lo.getCommune().getName());
                                break;
                            }
                        }

                    } catch (Exception e) {
                        cell.setCellValue("");
                    }

                    cell = row.createCell(colIndex++);
                    cell.setCellStyle(cellStyle);
                    try {
                        for (Location lo : caseOrg.getTheCase().getPerson().getLocations()) {
                            if (lo.getAddressType().toString().equalsIgnoreCase("RESIDENT_ADDRESS")) {
                                cell.setCellValue(lo.getDistrict().getName());
                                break;
                            }
                        }

                    } catch (Exception e) {
                        cell.setCellValue("");
                    }

                    cell = row.createCell(colIndex++);
                    cell.setCellStyle(cellStyle);
                    try {
                        for (Location lo : caseOrg.getTheCase().getPerson().getLocations()) {
                            if (lo.getAddressType().toString().equalsIgnoreCase("RESIDENT_ADDRESS")) {
                                cell.setCellValue(lo.getProvince().getName());
                                break;
                            }
                        }

                    } catch (Exception e) {
                        cell.setCellValue("");
                    }

                    //ngày kdhiv
                    cell = row.createCell(colIndex++);
                    cell.setCellStyle(dateCellStyle);
                    try {
                        cell.setCellValue(Date
                                .from(caseOrg.getTheCase().getHivConfirmDate().atZone(ZoneId.systemDefault())
                                        .toInstant()));
                    } catch (Exception e) {
                        cell.setCellValue("");
                    }

                    //ngày bat dau arv
                    cell = row.createCell(colIndex++);
                    cell.setCellStyle(dateCellStyle);
                    try {
                        cell.setCellValue(Date
                                .from(caseOrg.getTheCase().getArvStartDate().atZone(ZoneId.systemDefault())
                                        .toInstant()));
                    } catch (Exception e) {
                        cell.setCellValue("");
                    }

                    //ngày đk
                    cell = row.createCell(colIndex++);
                    cell.setCellStyle(dateCellStyle);
                    try {
                        cell.setCellValue(Date
                                .from(caseOrg.getTheCase().getHivScreenDate().atZone(ZoneId.systemDefault())
                                        .toInstant()));
                    } catch (Exception e) {
                        cell.setCellValue("");
                    }

                    //ngày bat dau arv
                    cell = row.createCell(colIndex++);
                    cell.setCellStyle(cellStyle);
                    try {

                        cell.setCellValue(caseOrg.getEnrollmentType().toString());

                    } catch (Exception e) {
                        cell.setCellValue("");
                    }
                    cell = row.createCell(colIndex++);
                    cell.setCellStyle(cellStyle);
                    try {
                        cell.setCellValue(caseOrg.getNote());
                    } catch (Exception e) {
                        cell.setCellValue("");
                    }

                }
            }
        }

        return wbook;
    }
}
