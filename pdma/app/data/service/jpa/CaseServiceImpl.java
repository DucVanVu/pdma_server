package org.pepfar.pdma.app.data.service.jpa;

import com.google.common.collect.Lists;
import com.google.zxing.BarcodeFormat;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;

import org.apache.commons.lang.WordUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
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
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pepfar.pdma.app.Constants;
import org.pepfar.pdma.app.data.aop.Loggable;
import org.pepfar.pdma.app.data.domain.AdminUnit;
import org.pepfar.pdma.app.data.domain.Appointment;
import org.pepfar.pdma.app.data.domain.Case;
import org.pepfar.pdma.app.data.domain.CaseOrg;
import org.pepfar.pdma.app.data.domain.ClinicalStage;
import org.pepfar.pdma.app.data.domain.Dictionary;
import org.pepfar.pdma.app.data.domain.HIVConfirmLab;
import org.pepfar.pdma.app.data.domain.Hepatitis;
import org.pepfar.pdma.app.data.domain.LabTest;
import org.pepfar.pdma.app.data.domain.Location;
import org.pepfar.pdma.app.data.domain.MMDispensing;
import org.pepfar.pdma.app.data.domain.MMT;
import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.domain.Person;
import org.pepfar.pdma.app.data.domain.Pregnancy;
import org.pepfar.pdma.app.data.domain.QAdminUnit;
import org.pepfar.pdma.app.data.domain.QAppointment;
import org.pepfar.pdma.app.data.domain.QCase;
import org.pepfar.pdma.app.data.domain.QCaseOrg;
import org.pepfar.pdma.app.data.domain.QDictionary;
import org.pepfar.pdma.app.data.domain.QOrganization;
import org.pepfar.pdma.app.data.domain.Regimen;
import org.pepfar.pdma.app.data.domain.RiskInterview;
import org.pepfar.pdma.app.data.domain.ShiInterview;
import org.pepfar.pdma.app.data.domain.TBProphylaxis;
import org.pepfar.pdma.app.data.domain.TBTreatment;
import org.pepfar.pdma.app.data.domain.Treatment;
import org.pepfar.pdma.app.data.domain.User;
import org.pepfar.pdma.app.data.dto.CaseDeleteFilterDto;
import org.pepfar.pdma.app.data.dto.CaseDto;
import org.pepfar.pdma.app.data.dto.CaseDto4Search;
import org.pepfar.pdma.app.data.dto.CaseFilterDto;
import org.pepfar.pdma.app.data.dto.CaseOrgDto;
import org.pepfar.pdma.app.data.dto.CaseOrgUpdateDto;
import org.pepfar.pdma.app.data.dto.CaseReferralResultDto;
import org.pepfar.pdma.app.data.dto.CheckNationalIdDto;
import org.pepfar.pdma.app.data.dto.DateRangeDto;
import org.pepfar.pdma.app.data.dto.DictionaryDto;
import org.pepfar.pdma.app.data.dto.LocationDto;
import org.pepfar.pdma.app.data.dto.OPCDashboardFilterDto;
import org.pepfar.pdma.app.data.dto.PersonDto;
import org.pepfar.pdma.app.data.dto.RoleDto;
import org.pepfar.pdma.app.data.dto.ShiInterviewDto;
import org.pepfar.pdma.app.data.dto.UserDto;
import org.pepfar.pdma.app.data.repository.AdminUnitRepository;
import org.pepfar.pdma.app.data.repository.AppointmentRepository;
import org.pepfar.pdma.app.data.repository.CaseOrgRepository;
import org.pepfar.pdma.app.data.repository.CaseRepository;
import org.pepfar.pdma.app.data.repository.DictionaryRepository;
import org.pepfar.pdma.app.data.repository.HIVConfirmLabRepository;
import org.pepfar.pdma.app.data.repository.LocationRepository;
import org.pepfar.pdma.app.data.repository.OrganizationRepository;
import org.pepfar.pdma.app.data.repository.PersonRepository;
import org.pepfar.pdma.app.data.repository.RegimenRepository;
import org.pepfar.pdma.app.data.service.CaseService;
import org.pepfar.pdma.app.data.types.AddressType;
import org.pepfar.pdma.app.data.types.AppointmentResult;
import org.pepfar.pdma.app.data.types.ClinicalTestingType;
import org.pepfar.pdma.app.data.types.EnrollmentType;
import org.pepfar.pdma.app.data.types.PatientStatus;
import org.pepfar.pdma.app.data.types.Permission;
import org.pepfar.pdma.app.utils.AuthorizationUtils;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.ExcelUtils;
import org.pepfar.pdma.app.utils.RiskGroupUtils;
import org.pepfar.pdma.app.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

@org.springframework.stereotype.Service
public class CaseServiceImpl implements CaseService {

    @Autowired private CaseRepository repos;

    @Autowired private PersonRepository personRepos;

    @Autowired private OrganizationRepository orgRepos;

    @Autowired private HIVConfirmLabRepository confirmLabRepos;

    @Autowired private RegimenRepository regimenRepos;

    @Autowired private DictionaryRepository dicRepos;

    @Autowired private LocationRepository locationRepos;

    @Autowired private AdminUnitRepository adminUnitRepos;

    @Autowired private CaseOrgRepository coRepos;

    @Autowired private EntityManager em;

    @Autowired private AuthorizationUtils authUtils;

    @Autowired private ApplicationContext context;

    @Autowired private AppointmentRepository appRepos;

    @Override
    @Transactional(readOnly = true)
    public CaseOrgDto findByCaseOrgId(Long coId) {

        if (!CommonUtils.isPositive(coId, true)) {
            return null;
        }

        CaseOrg entity = coRepos.findOne(coId);

        if (entity == null) {
            return null;
        }

        // Make sure the current user has access to this patient
        List<Long> orgIds = authUtils.getGrantedOrgIds(Permission.READ_ACCESS);
        final List<Long> writableOrgIds = authUtils.getGrantedOrgIds(Permission.WRITE_ACCESS);

        if (CommonUtils.isEmpty(orgIds)
                || !orgIds.contains(entity.getOrganization().getId().longValue())) {
            return null;
        }

        return checkCaseOrgEditable(
                writableOrgIds, entity, false); // no need details when change the view patient form
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CaseOrgDto> findAllPageable(CaseFilterDto filter) {

        if (filter == null) {
            filter = new CaseFilterDto();
        }

        if (filter.getPageIndex() < 0) {
            filter.setPageIndex(0);
        }

        if (filter.getPageSize() <= 0) {
            filter.setPageSize(25);
        }

        Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize());
        final List<Long> orgIds = authUtils.getGrantedOrgIds(Permission.READ_ACCESS);
        final List<Long> writableOrgIds = authUtils.getGrantedOrgIds(Permission.WRITE_ACCESS);

        long totalElements = 0;
        List<CaseOrg> caseOrgs = new ArrayList<>();

        // This requires a native query search
        QueryResult queryResult = queryCaseOrgs(filter, orgIds);
        totalElements = queryResult.getTotalElements();
        caseOrgs = queryResult.getContent();

        List<CaseOrgDto> content = new ArrayList<>();
        caseOrgs.parallelStream()
                .forEachOrdered(
                        co -> {
                            CaseOrgDto dto = checkCaseOrgEditable(writableOrgIds, co, false);
                            content.add(dto);
                        });

        return new PageImpl<>(content, pageable, totalElements);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CaseDto4Search> findAll4Appointment(CaseFilterDto filter) {

        if (filter == null) {
            filter = new CaseFilterDto();
        }

        if (filter.getPageIndex() < 0) {
            filter.setPageIndex(0);
        }

        if (filter.getPageSize() <= 0) {
            filter.setPageSize(5);
        }

        Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize());
        List<CaseDto4Search> list = new ArrayList<>();
        final List<Long> orgIds = authUtils.getGrantedOrgIds(Permission.WRITE_ACCESS);

        if (CommonUtils.isEmpty(filter.getKeyword(), true) || orgIds.size() <= 0) {
            return new PageImpl<>(new ArrayList<CaseDto4Search>(), pageable, 0);
        }

        if (filter.getOrganization() != null
                && CommonUtils.isPositive(filter.getOrganization().getId(), true)) {
            orgIds.clear();
            orgIds.add(filter.getOrganization().getId());
        }

        int offset = filter.getPageIndex() * filter.getPageSize();
        int limit = filter.getPageSize();

        long totalElements =
                coRepos.countCaseOrgs_WoAppointment_Total(
                                orgIds,
                                filter.getKeyword(),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "ACTIVE",
                                false,
                                true,
                                true)
                        .longValue();
        List<CaseOrg> caseOrgs =
                coRepos.findCaseOrgs_WoAppointment_Pageable(
                        orgIds,
                        filter.getKeyword(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "ACTIVE",
                        false,
                        true,
                        true,
                        1,
                        offset,
                        limit);

        caseOrgs.forEach(
                co -> {
                    Case theCase = co.getTheCase();
                    CaseDto4Search dto = new CaseDto4Search(theCase);
                    dto.setPatientChartId(co.getPatientChartId());

                    list.add(dto);
                });

        return new PageImpl<>(list, pageable, totalElements);
    }

    @Override
    @Transactional(readOnly = true)
    public String createRedirectPath(Long caseId) {

        if (!CommonUtils.isPositive(caseId, true)) {
            return null;
        }

        Case theCase = repos.findOne(caseId);

        if (theCase == null || theCase.isDeleted()) {
            return null;
        }

        List<Long> grantedOrgIds = authUtils.getGrantedOrgIds(Permission.READ_ACCESS);
        if (grantedOrgIds == null || grantedOrgIds.isEmpty()) {
            return null;
        }

        List<CaseOrg> cos = sortCaseOrgs(theCase);
        List<PatientStatus> acceptedStatuses =
                Lists.newArrayList(
                        PatientStatus.ACTIVE,
                        PatientStatus.TRANSFERRED_OUT,
                        PatientStatus.DEAD,
                        PatientStatus.LTFU);
        CaseOrg selectedCo = null;

        for (CaseOrg co : cos) {
            if (grantedOrgIds.contains(co.getId().longValue())
                    && !co.isRefTrackingOnly()
                    && acceptedStatuses.contains(co.getStatus())) {
                selectedCo = co;
                break;
            }
        }

        if (selectedCo == null) {
            return null;
        }

        return "/#/opc/view-patient/" + selectedCo.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public Workbook exportReferralSheet(Long caseOrgId, String urlPrefix) {

        Workbook blankBook = new XSSFWorkbook();
        blankBook.createSheet();

        if (!CommonUtils.isPositive(caseOrgId, true)) {
            return blankBook;
        }

        CaseOrg co = coRepos.findOne(caseOrgId);
        Case theCase = co.getTheCase();
        Person person = theCase.getPerson();
        Organization org = co.getOrganization();

        boolean isSiteManager =
                SecurityUtils.isUserInRole(
                        SecurityUtils.getCurrentUser(), Constants.ROLE_SITE_MANAGER);
        List<Long> orgIds = authUtils.getGrantedOrgIds(Permission.WRITE_ACCESS);

        if (co == null
                || co.getStatus() != PatientStatus.TRANSFERRED_OUT
                || co.getEndDate() == null
                || !isSiteManager
                || org == null
                || CommonUtils.isEmpty(orgIds)
                || !orgIds.contains(org.getId().longValue())
                || theCase.isDeleted()) {
            return blankBook;
        }

        Workbook wbook = null;
        try (InputStream template =
                context.getResource("classpath:templates/referral-sheet.xlsx").getInputStream()) {
            wbook = new XSSFWorkbook(template);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (wbook == null) {
            return blankBook;
        }

        List<CaseOrg> cos = Lists.newArrayList(theCase.getCaseOrgs());
        CaseOrg nextCo = null;
        int i = 0;

        for (CaseOrg _co : cos) {
            if (_co.getId().longValue() == co.getId() && i > 0) {
                nextCo = cos.get(i - 1);
                break;
            }

            i++;
        }

        String provinceName = "";
        if (org.getAddress() != null && org.getAddress().getProvince() != null) {
            provinceName = org.getAddress().getProvince().getName();
        }

        // Start filling out excel file
        Sheet sheet = wbook.getSheetAt(0);

        // Tỉnh/thành phố & tên cơ sở
        ExcelUtils.writeInCell(sheet, 1, 9, provinceName);
        ExcelUtils.writeInCell(sheet, 2, 9, org.getName());

        // Thông tin chung bệnh nhân
        if (nextCo != null) {
            if (Constants.CODE_ORGANIZATION_OTHER.equalsIgnoreCase(
                    nextCo.getOrganization().getCode())) {
                ExcelUtils.writeInCell(sheet, 6, 3, nextCo.getOrganizationName());
            } else {
                ExcelUtils.writeInCell(sheet, 6, 3, nextCo.getOrganization().getName());
            }
        }

        if (person != null && !CommonUtils.isEmpty(person.getFullname(), true)) {
            ExcelUtils.writeInCell(sheet, 7, 16, person.getFullname());
        }

        if (person != null && person.getDob() != null) {
            ExcelUtils.writeInCell(
                    sheet,
                    7,
                    33,
                    person.getDob().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }

        if (!CommonUtils.isEmpty(co.getPatientChartId())) {
            ExcelUtils.writeInCell(sheet, 8, 16, co.getPatientChartId());
        }

        if (person != null && person.getLocations() != null) {

            Location currentAddress = null;
            for (Location loc : person.getLocations()) {
                if (loc.getAddressType() == AddressType.CURRENT_ADDRESS) {
                    currentAddress = loc;
                    break;
                }
            }

            if (currentAddress != null) {
                String address = "";

                if (!CommonUtils.isEmpty(currentAddress.getStreetAddress(), true)) {
                    address += currentAddress.getStreetAddress();
                    address += ", ";
                }

                if (currentAddress.getCommune() != null) {
                    address += currentAddress.getCommune().getName();
                    address += ", ";
                }

                if (currentAddress.getDistrict() != null) {
                    address += currentAddress.getDistrict().getName();
                    address += ", ";
                }

                if (currentAddress.getProvince() != null) {
                    address += currentAddress.getProvince().getName();
                }

                ExcelUtils.writeInCell(sheet, 9, 9, address);
            }
        }

        if (!CommonUtils.isEmpty(co.getEndingReason(), true)) {
            ExcelUtils.writeInCell(sheet, 10, 9, co.getEndingReason());
        }

        // Tóm tắt bệnh án
        if (theCase.getHivConfirmDate() != null) {
            String s =
                    theCase.getHivConfirmDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            if (theCase.getConfirmLab() != null) {
                s += " (Nơi KĐ: ";
                s += theCase.getConfirmLab().getName();
                s += ")";
            } else if (!CommonUtils.isEmpty(theCase.getConfirmLabName(), true)) {
                s += " (Nơi KĐ: ";
                s += theCase.getConfirmLabName();
                s += ")";
            }

            ExcelUtils.writeInCell(sheet, 14, 16, s);
        }

        ExcelUtils.writeInCell(
                sheet, 15, 30, co.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        // WHO stage
        Iterator<ClinicalStage> whoStages = theCase.getWhoStages().iterator();
        while (whoStages.hasNext()) {
            ClinicalStage cs = whoStages.next();
            LocalDateTime csEvalDate = cs.getEvalDate();
            if (CommonUtils.dateInRange(csEvalDate, null, co.getEndDate())) {
                String s = "GĐLS " + cs.getStage();
                s += " (";
                s += csEvalDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                s += ")";

                ExcelUtils.writeInCell(sheet, 16, 26, s);
            }
        }

        LabTest cd4 = null;
        List<LabTest> tests =
                theCase.getLabTests().parallelStream()
                        .filter(e -> e.getTestType() == ClinicalTestingType.CD4)
                        .filter(
                                e ->
                                        CommonUtils.dateInRange(
                                                e.getSampleDate(), null, co.getEndDate()))
                        .collect(Collectors.toList());

        for (LabTest test : tests) {
            if (test.getResultDate() != null) {
                cd4 = test;
                break;
            }
        }

        if (cd4 != null) {
            String s = cd4.getResultNumber().toString();
            s += " tế bào/uL ";
            s += " (";
            s += cd4.getSampleDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            s += ")";

            ExcelUtils.writeInCell(sheet, 17, 12, s);
        }

        int[][] vlPos = {{19, 3}, {20, 3}, {21, 3}, {19, 31}, {20, 31}, {21, 31}};
        tests =
                theCase.getLabTests().parallelStream()
                        .filter(e -> e.getTestType() == ClinicalTestingType.VIRAL_LOAD)
                        .filter(
                                e ->
                                        CommonUtils.dateInRange(
                                                e.getSampleDate(), null, co.getEndDate()))
                        .collect(Collectors.toList());
        int indx = 0;
        int max = tests.size();
        if (max > 6) {
            max = 5;
        } else if (max > 0) {
            max = max - 1;
        }

        if (tests.size() > 0) {
            while (indx <= max) {
                LabTest test = tests.get(max - indx);
                String s = test.getResultText();
                s += " (";
                s += test.getSampleDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                s += ")";

                ExcelUtils.writeInCell(sheet, vlPos[indx][0], vlPos[indx][1], s);

                indx++;
            }
        }

        if (theCase.getArvStartDate() != null
                && CommonUtils.dateInRange(theCase.getArvStartDate(), null, co.getEndDate())) {
            ExcelUtils.writeInCell(
                    sheet,
                    22,
                    31,
                    theCase.getArvStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        } else {
            ExcelUtils.writeInCell(sheet, 22, 31, "Chưa điều trị ARV");
        }

        List<Treatment> treatments = Lists.newArrayList(theCase.getTreatments());
        indx = 0;
        max = treatments.size();
        if (max > 4) {
            max = 3;
        } else if (max > 0) {
            max = max - 1;
        }

        for (Treatment tx : treatments) {

            if (tx.getStartDate() == null
                    || CommonUtils.dateDiff(ChronoUnit.DAYS, tx.getStartDate(), co.getEndDate())
                            < 0) {
                continue;
            }

            if (indx > max) {
                break;
            }

            String s = tx.getRegimenName();

            if (indx == 0) {
                if (tx.getEndDate() == null
                        || (tx.getEndDate() != null
                                && CommonUtils.dateDiff(
                                                ChronoUnit.DAYS, tx.getEndDate(), co.getEndDate())
                                        < 0)) {
                    s += " (Bắt đầu: ";
                    if (tx.getStartDate() != null) {
                        s += tx.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    } else {
                        s += "//";
                    }
                    s += "; phác đồ hiện tại)";
                }

                ExcelUtils.writeInCell(sheet, 24, 3, s);
            } else {
                s += " (Bắt đầu: ";
                if (tx.getStartDate() != null) {
                    s += tx.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                } else {
                    s += "//";
                }
                if (!CommonUtils.isEmpty(tx.getEndingReason(), true)) {
                    s += "; ";
                    s += tx.getEndingReason().trim();
                }
                s += ")";

                // Insert new row
                sheet.shiftRows(24 + indx, sheet.getLastRowNum(), 1, true, false);

                // ---------------------------
                // Start bug correcting in POI
                // ---------------------------
                if (sheet instanceof XSSFSheet) {
                    XSSFSheet xSSFSheet = (XSSFSheet) sheet;
                    for (int r = xSSFSheet.getFirstRowNum(); r < sheet.getLastRowNum() + 1; r++) {
                        XSSFRow row = xSSFSheet.getRow(r);
                        if (row != null) {
                            long rRef = row.getCTRow().getR();
                            for (Cell cell : row) {
                                String cRef = ((XSSFCell) cell).getCTCell().getR();
                                ((XSSFCell) cell)
                                        .getCTCell()
                                        .setR(cRef.replaceAll("[0-9]", "") + rRef);
                            }
                        }
                    }
                }
                // ---------------------------
                // End bug correcting in POI
                // ---------------------------

                Row row = sheet.createRow(24 + indx);

                // apply cell styles
                for (int j = 0; j < 34; j++) {
                    Cell cell = sheet.getRow(24).getCell(j);
                    CellStyle cellStyle = null;
                    if (cell != null) {
                        cellStyle = cell.getCellStyle();
                    }

                    if (cellStyle != null) {
                        cell = row.createCell(j);
                        cell.setCellStyle(cellStyle);
                    }
                }

                ExcelUtils.writeInCell(sheet, 24 + indx, 1, "PĐ " + (indx + 1) + ":");
                ExcelUtils.writeInCell(sheet, 24 + indx, 3, s);
            }

            indx++;
        }

        indx = indx - 1;

        // Drug days
        Iterator<Appointment> apps = theCase.getAppointments().iterator();
        while (apps.hasNext()) {
            Appointment app = apps.next(); // the latest one

            LocalDateTime arrivalDate = app.getArrivalDate();
            Integer drugDays = app.getDrugDays();

            if (CommonUtils.isTrue(app.getArrived())
                    && arrivalDate != null
                    && CommonUtils.dateInRange(arrivalDate, null, co.getEndDate())
                    && CommonUtils.isPositive(drugDays, true)) {
                // re-calculate the next appointment date
                LocalDateTime drugAvailableTo = arrivalDate.plusDays(drugDays);

                //                if (CommonUtils.dateDiff(ChronoUnit.DAYS, drugAvailableTo,
                // co.getEndDate()) < 0) {
                String s = "Bệnh nhân được cấp ";
                s += drugDays;
                s += " ngày thuốc, dùng đến ";
                s += drugAvailableTo.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                ExcelUtils.writeInCell(sheet, 27 + indx, 14, s);
                //                }

                break;
            }
        }

        // INH
        Iterator<TBProphylaxis> tbPros = theCase.getTbpros().iterator();
        if (tbPros.hasNext()) {
            TBProphylaxis tbPro = tbPros.next();

            if (tbPro != null) {
                String s = "Phác đồ: ";

                if (tbPro.getRegimen() != null) {
                    s += tbPro.getRegimen().toString();
                } else {
                    s += "-";
                }

                s += "; ";

                if (tbPro.getStartDate() != null) {
                    s += tbPro.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                } else {
                    s += "//";
                }

                s += " - ";

                if (tbPro.getEndDate() != null) {
                    s += tbPro.getEndDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                } else {
                    s += "//";
                }

                s += "; ";

                switch (tbPro.getResult()) {
                    case 1:
                        s += "Bỏ điều trị";
                        break;
                    case 2:
                        s += "Chưa hoàn thành";
                        break;
                    case 3:
                        s += "Đã hoàn thành";
                        break;
                }

                ExcelUtils.writeInCell(sheet, 29 + indx, 12, s);
            }
        }

        // Barcode
        if (!CommonUtils.isEmpty(urlPrefix)) {
            urlPrefix += "/#/api/v1/case/redirect4referral/" + theCase.getId();
            byte[] barcodeImage =
                    ExcelUtils.generateBarcode(
                            urlPrefix,
                            new String[] {"THÔNG TIN BỆNH NHÂN"},
                            BarcodeFormat.QR_CODE,
                            150,
                            150);
            if (barcodeImage != null) {
                ExcelUtils.insertImage(wbook, sheet, barcodeImage, 33, 1, 34, 7);
            }
        }

        // Ngày tháng năm...
        LocalDateTime endDate = co.getEndDate();
        String s = "Ngày ";
        s += endDate.getDayOfMonth();
        s += " Tháng ";
        s += endDate.getMonthValue();
        s += " Năm ";
        s += endDate.getYear();

        ExcelUtils.writeInCell(sheet, 31 + indx, 31, s);

        return wbook;
    }

    @Override
    @Transactional(readOnly = true)
    public Workbook exportData(CaseFilterDto filter) {
        if (filter == null) {
            filter = new CaseFilterDto();
        }

        if (filter.getUser() == null) {
            filter.setUser(new UserDto(SecurityUtils.getCurrentUser(), false));
        }

        return createExcelWorkbook(filter);
    }

    @Override
    @Transactional(readOnly = true)
    public Workbook exportSearchResults(CaseFilterDto filter) {
        if (filter == null) {
            filter = new CaseFilterDto();
        }

        if (filter.getUser() == null) {
            filter.setUser(new UserDto(SecurityUtils.getCurrentUser(), false));
        }

        return createExcelWorkbook4SearchResults(filter);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CaseOrgDto> getCaseStatusHistory(Long caseOrgId) {
        if (!CommonUtils.isPositive(caseOrgId, true)) {
            return new ArrayList<>();
        }

        CaseOrg coE = coRepos.findOne(caseOrgId);
        Case theCase = coE.getTheCase();

        boolean everInManageableFacility =
                false; // check if the patient has ever been enrolled in one of the
        // facilities in the granted list
        List<Long> grantedOrgIds = authUtils.getGrantedOrgIds(Permission.READ_ACCESS);
        if (coE == null || grantedOrgIds.size() <= 0) {
            return new ArrayList<>();
        }

        List<Long> editGrantedOrgIds = authUtils.getGrantedOrgIds(Permission.WRITE_ACCESS);
        boolean isSiteManager =
                SecurityUtils.isUserInRole(
                        SecurityUtils.getCurrentUser(), Constants.ROLE_SITE_MANAGER);
        boolean isProvincialManager =
                SecurityUtils.isUserInRole(
                        SecurityUtils.getCurrentUser(), Constants.ROLE_PROVINCIAL_MANAGER);

        CaseOrgDto firstCaseOrg = null;
        CaseOrgDto firstEditableCaseOrg = null;
        List<CaseOrgDto> list = new ArrayList<>();

        List<CaseOrg> entities = sortCaseOrgs(theCase);

        // Wrapping the DTOs
        for (CaseOrg e : entities) {
            if (firstCaseOrg == null) {
                firstCaseOrg = new CaseOrgDto(e, false);

                if (editGrantedOrgIds.contains(e.getOrganization().getId().longValue())) {
                    if (firstEditableCaseOrg == null) {
                        firstEditableCaseOrg = firstCaseOrg;
                    }
                }

            } else {
                CaseOrgDto dto = new CaseOrgDto(e, false);

                // Editable
                if (!isSiteManager || CommonUtils.isEmpty(editGrantedOrgIds)) {
                    dto.setEditable(false);
                } else {
                    dto.setEditable(
                            e.getStatus() != PatientStatus.PENDING_ENROLLMENT
                                    && e.getStatus() != PatientStatus.CANCELLED_ENROLLMENT
                                    && editGrantedOrgIds.contains(
                                            e.getOrganization().getId().longValue()));

                    if (dto.isEditable()) {
                        if (firstEditableCaseOrg == null) {
                            firstEditableCaseOrg = dto;
                        }
                    }
                }

                // Deletable
                if (!isSiteManager || CommonUtils.isEmpty(editGrantedOrgIds)) {
                    dto.setDeletable(false);
                } else {
                    dto.setDeletable(
                            e.getStatus() != PatientStatus.PENDING_ENROLLMENT
                                    && e.getStatus() != PatientStatus.CANCELLED_ENROLLMENT
                                    && editGrantedOrgIds.contains(
                                            e.getOrganization().getId().longValue())
                                    && firstEditableCaseOrg != null
                                    && firstEditableCaseOrg.getId().longValue() != dto.getId());
                }

                dto.setUpdateable(false);
                list.add(dto);

                // avoid the first case-org
                if (editGrantedOrgIds.contains(e.getOrganization().getId().longValue())) {
                    everInManageableFacility = true;
                }
            }
        }

        if (firstCaseOrg != null) {
            // Check if the sending facility is able to update the referral status
            // -- only check the first record as they are sorted descendingly
            if (CommonUtils.isTrue(firstCaseOrg.getCurrent())
                    && firstCaseOrg.getStatus() == PatientStatus.PENDING_ENROLLMENT) {

                if (list.size() > 0) {
                    CaseOrgDto secondCaseOrg = list.get(0);

                    if (secondCaseOrg.getStatus() == PatientStatus.TRANSFERRED_OUT
                            && editGrantedOrgIds.contains(
                                    secondCaseOrg.getOrganization().getId().longValue())) {
                        firstCaseOrg.setUpdateable(true);
                    } else {
                        firstCaseOrg.setUpdateable(false);
                    }
                }
            }

            // Check if the sending facility is able to update the current status of the
            // patient to ACTIVE. This is the special case when a patient was referred to
            // another OPC where may not be using OPC-Assist, the patient then returned to
            // the current OPC for continuum of care. The current OPC then need to
            // enroll/update status back to ACTIVE for the patient by themselves
            if (list.size() <= 0
                    && firstCaseOrg != null
                    && firstCaseOrg.getStatus() == PatientStatus.TRANSFERRED_OUT) {
                firstCaseOrg.setReEnrollable(true);
            } else {
                if (isProvincialManager) {
                    // if this is the provincial account
                    boolean reEnrollable =
                            grantedOrgIds.contains(
                                    firstCaseOrg.getOrganization().getId().longValue());
                    reEnrollable =
                            reEnrollable
                                    || (!grantedOrgIds.contains(
                                                    firstCaseOrg
                                                            .getOrganization()
                                                            .getId()
                                                            .longValue())
                                            && firstCaseOrg.isRefTrackingOnly())
                                    || (!grantedOrgIds.contains(
                                                    firstCaseOrg
                                                            .getOrganization()
                                                            .getId()
                                                            .longValue())
                                            && Constants.CODE_ORGANIZATION_OTHER.equalsIgnoreCase(
                                                    firstCaseOrg.getOrganization().getCode()));

                    firstCaseOrg.setReEnrollable(reEnrollable);
                } else if (isSiteManager) {
                    // if this is the site level account
                    boolean reEnrollable = everInManageableFacility;
                    reEnrollable =
                            reEnrollable
                                    && !editGrantedOrgIds.contains(
                                            firstCaseOrg.getOrganization().getId().longValue());
                    reEnrollable = reEnrollable && firstCaseOrg.isRefTrackingOnly();

                    firstCaseOrg.setReEnrollable(reEnrollable);
                }
            }

            // Editable
            if (!isSiteManager || CommonUtils.isEmpty(editGrantedOrgIds)) {
                firstCaseOrg.setEditable(false);
            } else {
                firstCaseOrg.setEditable(
                        firstCaseOrg.getStatus() != PatientStatus.PENDING_ENROLLMENT
                                && firstCaseOrg.getStatus() != PatientStatus.CANCELLED_ENROLLMENT
                                && editGrantedOrgIds.contains(
                                        firstCaseOrg.getOrganization().getId().longValue()));
            }

            list.add(0, firstCaseOrg);
        }

        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public int hivInfoIdExists(CaseOrgDto dto) {

        if (dto == null
                || dto.getTheCase() == null
                || CommonUtils.isEmpty(dto.getTheCase().getHivInfoId())) {
            return 0;
        }

        QCaseOrg q = QCaseOrg.caseOrg;
        BooleanExpression be =
                q.theCase
                        .hivInfoID
                        .isNotNull()
                        .and(q.theCase.hivInfoID.equalsIgnoreCase(dto.getTheCase().getHivInfoId()));

        if (CommonUtils.isPositive(dto.getId(), true)) {
            be = be.and(q.id.longValue().ne(dto.getId()));
        }

        List<CaseOrg> entities = Lists.newArrayList(coRepos.findAll(be));

        return entities.size();
    }

    @Override
    @Transactional(readOnly = true)
    public CheckNationalIdDto nationalIdExists(CaseOrgDto dto) {
        CheckNationalIdDto res = new CheckNationalIdDto();
        res.setResult(-1);
        res.setCo(null);

        if (dto == null
                || dto.getOrganization() == null
                || !CommonUtils.isPositive(dto.getOrganization().getId(), true)
                || dto.getTheCase() == null
                || dto.getTheCase().getPerson() == null
                || CommonUtils.isEmpty(dto.getTheCase().getPerson().getNidNumber(), true)) {
            return res;
        }

        res.setResult(0);

        Long orgId = dto.getOrganization().getId();
        Long caseId = dto.getTheCase().getId();
        String nationalId = dto.getTheCase().getPerson().getNidNumber();

        // check if a case exist in the same opc
        List<CaseOrg> foundList = coRepos.checkNationalIdExists(orgId, nationalId, caseId);
        if (foundList != null && foundList.size() > 0) {
            res.setResult(1);

            CaseOrgDto coDto = new CaseOrgDto(foundList.get(0), false);
            res.setCo(coDto);
        } else {
            foundList = coRepos.checkNationalIdExists(null, nationalId, caseId);

            if (foundList != null && foundList.size() > 0) {
                res.setResult(2);

                CaseOrgDto coDto = new CaseOrgDto(foundList.get(0), false);
                res.setCo(coDto);
            }
        }

        return res;
    }

    @Override
    @Transactional(readOnly = true)
    public CaseOrgDto patientChartIdExists(CaseOrgDto dto) {

        if (dto == null
                || dto.getOrganization() == null
                || !CommonUtils.isPositive(dto.getOrganization().getId(), true)
                || CommonUtils.isEmpty(dto.getPatientChartId(), true)) {
            return null;
        }

        List<Long> grantedIds = authUtils.getGrantedOrgIds(Permission.READ_ACCESS);
        CaseOrgDto ret = null;

        Organization org = orgRepos.findOne(dto.getOrganization().getId());

        if (org == null) {
            return null;
        }

        if (!grantedIds.contains(org.getId().longValue())) {
            throw new RuntimeException(
                    "User is not authorized to check existance of patient chart ID in this OPC!");
        }

        List<CaseOrg> cos =
                coRepos.checkPatientChartIdExistance(
                        org.getId(), dto.getPatientChartId(), dto.getId());

        if (cos.size() > 0) {
            ret = new CaseOrgDto(cos.get(0), false);
        }

        return ret;
    }

    @Override
    @Transactional(readOnly = true)
    public CaseOrgDto patientRecordExists(CaseOrgDto dto) {
        if (dto == null || dto.getTheCase() == null || dto.getTheCase().getPerson() == null) {
            return null;
        }

        PersonDto person = dto.getTheCase().getPerson();
        Timestamp dob = null;
        String gender = null;
        Long resProvinceId = null;
        Long resDistrictId = null;

        if (person.getDob() != null) {
            dob = CommonUtils.toTimestamp(person.getDob());
        }

        if (person.getGender() != null) {
            gender = person.getGender().name();
        }

        if (person.getLocations() != null) {
            Iterator<LocationDto> itr = person.getLocations().iterator();
            while (itr.hasNext()) {
                LocationDto loc = itr.next();

                if (loc.getAddressType() == AddressType.RESIDENT_ADDRESS) {
                    if (loc.getProvince() != null) {
                        resProvinceId = loc.getProvince().getId();
                    }

                    if (loc.getDistrict() != null) {
                        resDistrictId = loc.getDistrict().getId();
                    }

                    break;
                }
            }
        }

        List<CaseOrg> cos =
                coRepos.findMatchedPatientRecords(
                        dto.getTheCase().getId(),
                        person.getNidNumber(),
                        person.getPassportNumber(),
                        dto.getTheCase().getHivInfoId(),
                        person.getFullname(),
                        dob,
                        gender,
                        resProvinceId,
                        resDistrictId);

        if (cos.size() > 0) {
            return new CaseOrgDto(cos.get(0), false);
        } else {
            return null;
        }
    }

    @Override
    @Loggable
    @Transactional(rollbackFor = Exception.class)
    public CaseOrgDto saveOne(CaseOrgDto dto) {

        if (dto == null
                || dto.getTheCase() == null
                || dto.getTheCase().getPerson() == null
                || dto.getOrganization() == null
                || !CommonUtils.isPositive(dto.getOrganization().getId(), true)) {
            throw new IllegalArgumentException("Case to save could not be null.");
        }

        List<Long> grantedOrgIds = authUtils.getGrantedOrgIds(Permission.WRITE_ACCESS);
        if (grantedOrgIds.size() <= 0
                || !grantedOrgIds.contains(dto.getOrganization().getId().longValue())) {
            throw new IllegalArgumentException("Unauthorized.");
        }

        UUID coUID = UUID.randomUUID();
        Case ce = null;
        Organization oe = orgRepos.findOne(dto.getOrganization().getId());
        boolean isOnArt = false;

        if (oe == null) {
            throw new RuntimeException(
                    "Could not find organization with given ID for saving the case.");
        }

        CaseDto cdto = dto.getTheCase();

        if (CommonUtils.isPositive(cdto.getId(), true)) {
            ce = repos.findOne(cdto.getId());
        }

        if (ce == null) {
            ce = cdto.toEntity();
            ce.setUid(UUID.randomUUID());

            isOnArt = cdto.getArvStartDate() != null;

            if (CommonUtils.isPositive(cdto.getCurrentArvRegimenLine(), true)) {
                // second line start date
                if (cdto.getCurrentArvRegimenLine() == 2) {
                    ce.setSecondLineStartDate(cdto.getCurrentArvRegimenStartDate());
                }

                // third line start date
                if (cdto.getCurrentArvRegimenLine() == 3) {
                    ce.setThirdLineStartDate(cdto.getCurrentArvRegimenStartDate());
                }
            }

        } else {

            // Make sure to add treatment data if this patient is not on ART and is now set
            // as on ART
            isOnArt = ce.getArvStartDate() != null;

            ce.setUid(cdto.getUid());
            ce.setHivScreenDate(cdto.getHivScreenDate());
            ce.setHivConfirmDate(cdto.getHivConfirmDate());
            ce.setConfirmLabName(cdto.getConfirmLabName());
            ce.setHivConfirmId(cdto.getHivConfirmId());
            ce.setHivInfoID(cdto.getHivInfoId());
            ce.setNationalHealthId(cdto.getNationalHealthId());
            ce.setShiNumber(cdto.getShiNumber());
            ce.setArvStartDate(cdto.getArvStartDate());
            ce.setSecondLineStartDate(cdto.getSecondLineStartDate());
            ce.setThirdLineStartDate(cdto.getThirdLineStartDate());
            ce.setFourthLineStartDate(cdto.getFourthLineStartDate());
            ce.setCurrentArvRegimenName(cdto.getCurrentArvRegimenName());
            ce.setCurrentArvRegimenLine(cdto.getCurrentArvRegimenLine());
            ce.setCurrentArvRegimenStartDate(cdto.getCurrentArvRegimenStartDate());
            ce.setNote(cdto.getNote());
        }

        Person person = null;

        // Person
        PersonDto personDto = dto.getTheCase().getPerson();

        if (ce.getPerson() != null && CommonUtils.isPositive(ce.getPerson().getId(), true)) {
            person = personRepos.findOne(ce.getPerson().getId());
        }

        if (person == null) {
            person = new Person();
        }

        if (personDto != null) {
            person.setNidNumber(personDto.getNidNumber());
            person.setNidIssuedDate(personDto.getNidIssuedDate());
            person.setNidIssuedBy(personDto.getNidIssuedBy());
            person.setNoNidReason(personDto.getNoNidReason());
            person.setPassportNumber(personDto.getPassportNumber());
            person.setPassportIssuedDate(personDto.getPassportIssuedDate());
            person.setPassportIssuedBy(personDto.getPassportIssuedBy());
            person.setPassportExpiryDate(personDto.getPassportExpiryDate());
            person.setFullname(WordUtils.capitalizeFully(personDto.getFullname()));
            person.setDob(personDto.getDob());
            person.setGender(personDto.getGender());
            person.setMaritalStatus(personDto.getMaritalStatus());
            person.setMobilePhone(personDto.getMobilePhone());
            person.setHomePhone(personDto.getHomePhone());
            person.setEmailAddress(personDto.getEmailAddress());
            person.setHeight(personDto.getHeight());
            person.setWeight(personDto.getWeight());
            person.setOccupation(personDto.getOccupation());
            person.setOccupationName(personDto.getOccupationName());

            if (personDto.getEthnic() != null
                    && CommonUtils.isPositive(personDto.getEthnic().getId(), true)) {
                Dictionary ethnic = dicRepos.findOne(personDto.getEthnic().getId());
                person.setEthnic(ethnic);
            }

            if (personDto.getReligion() != null
                    && CommonUtils.isPositive(personDto.getReligion().getId(), true)) {
                Dictionary religion = dicRepos.findOne(personDto.getReligion().getId());
                person.setReligion(religion);
            }

            if (personDto.getNationality() != null
                    && CommonUtils.isPositive(personDto.getNationality().getId(), true)) {
                Dictionary nationality = dicRepos.findOne(personDto.getNationality().getId());
                person.setNationality(nationality);
            }

            if (personDto.getEducation() != null
                    && CommonUtils.isPositive(personDto.getEducation().getId(), true)) {
                Dictionary education = dicRepos.findOne(personDto.getEducation().getId());
                person.setEducation(education);
            }

            if (personDto.getWealthStatus() != null
                    && CommonUtils.isPositive(personDto.getWealthStatus().getId(), true)) {
                Dictionary wealthStatus = dicRepos.findOne(personDto.getWealthStatus().getId());
                person.setWealthStatus(wealthStatus);
            }

            if (personDto.getMonthlyIncome() != null
                    && CommonUtils.isPositive(personDto.getMonthlyIncome().getId(), true)) {
                Dictionary monthlyIncome = dicRepos.findOne(personDto.getMonthlyIncome().getId());
                person.setMonthlyIncome(monthlyIncome);
            }

            if (personDto.getProfessional() != null
                    && CommonUtils.isPositive(personDto.getProfessional().getId(), true)) {
                Dictionary professional = dicRepos.findOne(personDto.getProfessional().getId());
                person.setProfessional(professional);
            }

            // Locations
            List<Location> locations = new ArrayList<>();
            for (LocationDto loc : personDto.getLocations()) {
                Location address = null;

                if (CommonUtils.isPositive(loc.getId(), true)) {
                    address = locationRepos.findOne(loc.getId());
                }

                if (CommonUtils.isPositive(personDto.getId(), true)
                        && loc.getAddressType() != null) {
                    List<Location> locs =
                            locationRepos.findForPerson(personDto.getId(), loc.getAddressType());

                    if (locs != null && locs.size() > 0) {

                        if (address == null) {
                            address = locs.get(0);
                        }

                        if (locs.size() > 1) {
                            // delete the rest. Keep only one!
                            locationRepos.deleteUnwantedRecords(
                                    personDto.getId(), address.getId(), loc.getAddressType());
                        }
                    }
                }

                if (address == null) {
                    address = new Location();
                }

                address.setAddressType(loc.getAddressType());
                address.setStreetAddress(loc.getStreetAddress());
                address.setAccuracy(loc.getAccuracy());
                address.setLatitude(loc.getLatitude());
                address.setLongitude(loc.getLongitude());

                AdminUnit commune = null;
                AdminUnit district = null;
                AdminUnit province = null;
                AdminUnit country = null;

                if (loc.getCommune() != null
                        && CommonUtils.isPositive(loc.getCommune().getId(), true)) {
                    commune = adminUnitRepos.findOne(loc.getCommune().getId());
                }

                if (loc.getDistrict() != null
                        && CommonUtils.isPositive(loc.getDistrict().getId(), true)) {
                    district = adminUnitRepos.findOne(loc.getDistrict().getId());
                }

                if (loc.getProvince() != null
                        && CommonUtils.isPositive(loc.getProvince().getId(), true)) {
                    province = adminUnitRepos.findOne(loc.getProvince().getId());
                }

                if (loc.getCountry() != null) {
                    if (CommonUtils.isPositive(loc.getCountry().getId(), true)) {
                        country = adminUnitRepos.findOne(loc.getCountry().getId());
                    }
                    if (country == null && !CommonUtils.isEmpty(loc.getCountry().getCode())) {
                        country =
                                adminUnitRepos.findOne(
                                        QAdminUnit.adminUnit.code.equalsIgnoreCase(
                                                loc.getCountry().getCode()));
                    }
                }

                address.setCommune(commune);
                address.setDistrict(district);
                address.setProvince(province);
                address.setCountry(country);
                address.setPerson(person);

                locations.add(address);
            }

            person.getLocations().clear();
            person.getLocations().addAll(locations);
        }

        ce.setPerson(person);

        // Confirm lab
        HIVConfirmLab confirmLab = null;
        if (cdto.getConfirmLab() != null
                && CommonUtils.isPositive(cdto.getConfirmLab().getId(), true)) {
            confirmLab = confirmLabRepos.findOne(cdto.getConfirmLab().getId());
        }
        ce.setConfirmLab(confirmLab);

        // Current organization
        if (!CommonUtils.isPositive(ce.getId(), true)) {

            CaseOrg co = new CaseOrg();

            co.setUid(coUID);

            co.setTheCase(ce);
            co.setOrganization(oe);

            co.setEnrollmentType(dto.getEnrollmentType());
            co.setArvGroup(dto.getArvGroup());
            co.setStartDate(dto.getStartDate()); // enrollment date
            co.setArvStartDate(
                    dto.getArvStartDate()); // arv start date at this OPC for this treatment period
            co.setNote(dto.getNote());

            // when adding a patient
            co.setPatientChartId(dto.getPatientChartId());
            co.setPrevStatus(PatientStatus.NULL);
            co.setStatus(dto.getStatus());
            co.setEndDate(dto.getEndDate());
            co.setEndingReason(null);
            co.setCurrent(dto.getStatus() == PatientStatus.TRANSFERRED_OUT ? false : true);
            co.setLatestRelationship(true);
            co.setRefTrackingOnly(false);

            ce.getCaseOrgs().clear();
            ce.getCaseOrgs().add(co);
        } else {
            coUID = dto.getUid();
        }

        // -----------------
        // ARV treatment
        // -----------------
        if (!isOnArt || !CommonUtils.isPositive(ce.getId(), true)) {

            // create a treatment record for ARV treatment the first time a case is created
            // if this patient is on ART
            if (cdto.getArvStartDate() != null
                    && cdto.getCurrentArvRegimenStartDate() != null
                    && !CommonUtils.isEmpty(cdto.getCurrentArvRegimenName())
                    && CommonUtils.isPositive(cdto.getCurrentArvRegimenLine(), true)) {

                // Regimen
                Regimen regimen = null;
                if (cdto.getCurrentArvRegimen() != null
                        && CommonUtils.isPositive(cdto.getCurrentArvRegimen().getId(), true)) {
                    regimen = regimenRepos.findOne(cdto.getCurrentArvRegimen().getId());
                }
                ce.setCurrentArvRegimen(regimen);

                // First ARV treatment record
                Dictionary hiv =
                        dicRepos.findOne(QDictionary.dictionary.code.equalsIgnoreCase("HIV"));

                Treatment treatment = new Treatment();
                treatment.setUid(UUID.randomUUID());
                treatment.setDisease(hiv);
                treatment.setTheCase(ce);
                treatment.setOrganization(oe);
                treatment.setRegimen(regimen);
                treatment.setRegimenName(cdto.getCurrentArvRegimenName());
                treatment.setRegimenLine(cdto.getCurrentArvRegimenLine());
                treatment.setStartDate(cdto.getCurrentArvRegimenStartDate());

                if (CommonUtils.isPositive(cdto.getCurrentArvRegimenLine(), true)) {
                    switch (cdto.getCurrentArvRegimenLine()) {
                        case 1:
                            ce.setSecondLineStartDate(null);
                            ce.setThirdLineStartDate(null);
                            ce.setFourthLineStartDate(null);
                            break;
                        case 2:
                            ce.setSecondLineStartDate(cdto.getCurrentArvRegimenStartDate());
                            break;
                        case 3:
                            ce.setThirdLineStartDate(cdto.getCurrentArvRegimenStartDate());
                            break;
                        case 4:
                            ce.setFourthLineStartDate(cdto.getCurrentArvRegimenStartDate());
                            break;
                    }
                }

                ce.getTreatments().clear();
                ce.getTreatments().add(treatment);
            }
        }

        // Save the entity
        ce = repos.save(ce);

        if (ce != null) {
            //			return new CaseDto(ce, grantedOrgIds);
            CaseOrg co = coRepos.findOne(QCaseOrg.caseOrg.uid.eq(coUID));

            return new CaseOrgDto(co, false);
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    @Loggable
    @Transactional(rollbackFor = Exception.class)
    public CaseDto updateHivInfoID(CaseDto dto) {
        if (dto == null
                || !CommonUtils.isPositive(dto.getId(), true)
                || CommonUtils.isEmpty(dto.getHivInfoId())
                || dto.getHivInfoId().trim().length() != 20) {
            throw new IllegalArgumentException("Invalid Case object or HIVInfo ID!");
        }

        Case entity = repos.findOne(dto.getId());

        if (entity == null) {
            throw new RuntimeException("Case not found for updating HIVInfo ID!");
        }

        List<Long> grantedOrgIds = authUtils.getGrantedOrgIds(Permission.READ_ACCESS);

        entity.setHivInfoID(dto.getHivInfoId());
        entity.setHivInfoIdLocked(true);
        entity = repos.save(entity);

        if (entity != null) {
            return new CaseDto(entity, grantedOrgIds);
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    @Loggable
    @Transactional(rollbackFor = Exception.class)
    public CaseDto removeHivInfoID(CaseDto dto) {
        if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
            throw new IllegalArgumentException("Invalid Case object or HIVInfo ID!");
        }

        Case entity = repos.findOne(dto.getId());

        if (entity == null) {
            throw new RuntimeException("Case not found for removing HIVInfo ID!");
        }

        List<Long> grantedOrgIds = authUtils.getGrantedOrgIds(Permission.READ_ACCESS);

        entity.setHivInfoID(null);
        entity.setHivInfoIdLocked(false);
        entity = repos.save(entity);

        if (entity != null) {
            return new CaseDto(entity, grantedOrgIds);
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    @Loggable
    @Transactional(rollbackFor = Exception.class)
    public CaseOrgDto softDelete(CaseOrgDto dto) {

        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null
                || !SecurityUtils.isUserInRole(currentUser, Constants.ROLE_SITE_MANAGER)) {
            throw new RuntimeException("Only site manager can perform delete.");
        }

        if (dto == null
                || !CommonUtils.isPositive(dto.getId(), true)
                || dto.getTheCase() == null
                || !CommonUtils.isPositive(dto.getTheCase().getId(), true)) {
            throw new IllegalArgumentException();
        }

        CaseOrg co = coRepos.findOne(dto.getId());
        Case theCase = repos.findOne(dto.getTheCase().getId());

        if (co == null || theCase == null) {
            throw new RuntimeException("Case not found for delete!");
        }

        /** Only site manager can delete */
        List<Long> grantedOrgIds = authUtils.getGrantedOrgIds(Permission.DELETE_ACCESS);
        if (grantedOrgIds.size() <= 0
                || !grantedOrgIds.contains(co.getOrganization().getId().longValue())) {
            throw new RuntimeException("Unauthorized!");
        }

        theCase.setDeleted(true);
        theCase = repos.save(theCase);

        if (theCase != null) {
            co.setTheCase(theCase); // update with the deleted case
            return new CaseOrgDto(co, false);
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    @Loggable
    @Transactional(rollbackFor = Exception.class)
    public CaseOrgDto restoreCase(CaseOrgDto dto) {

        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null
                || !SecurityUtils.isUserInRole(currentUser, Constants.ROLE_PROVINCIAL_MANAGER)) {
            throw new RuntimeException("Only provincial manager can perform restore.");
        }

        if (dto == null
                || !CommonUtils.isPositive(dto.getId(), true)
                || dto.getTheCase() == null
                || !CommonUtils.isPositive(dto.getTheCase().getId(), true)) {
            throw new IllegalArgumentException();
        }

        CaseOrg co = coRepos.findOne(dto.getId());
        Case entity = repos.findOne(dto.getTheCase().getId());

        if (entity == null || co == null) {
            throw new RuntimeException("Not found!");
        }

        List<Long> grantedOrgIds = authUtils.getGrantedOrgIds(Permission.READ_ACCESS);

        if (grantedOrgIds.size() <= 0
                || !grantedOrgIds.contains(co.getOrganization().getId().longValue())) {
            throw new RuntimeException("Unauthorized!");
        }

        entity.setDeleted(false);
        entity = repos.save(entity);

        if (entity != null) {
            return new CaseOrgDto(co, false);
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    @Loggable
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelReferral(CaseReferralResultDto dto) {

        if (dto == null
                || dto.getTheCase() == null
                || !CommonUtils.isPositive(dto.getTheCase().getId(), true)) {
            return false;
        }

        Case theCase = null;

        theCase = repos.findOne(dto.getTheCase().getId());
        if (theCase == null) {
            return false;
        }

        CaseOrg[] txHistory = theCase.getCaseOrgs().toArray(new CaseOrg[0]);

        if (txHistory == null || txHistory.length < 2) {
            return false;
        }

        CaseOrg latestCaseOrg = txHistory[0];
        CaseOrg prevCaseOrg = txHistory[1];

        if (latestCaseOrg == null
                || prevCaseOrg == null
                || !CommonUtils.isPositive(latestCaseOrg.getId(), true)
                || !CommonUtils.isPositive(prevCaseOrg.getId(), true)) {
            return false;
        }

        if (latestCaseOrg == null || prevCaseOrg == null) {
            return false;
        }

        switch (prevCaseOrg.getEnrollmentType()) {
            case NEWLY_ENROLLED:
                prevCaseOrg.setPrevStatus(PatientStatus.NULL);
                break;
            case RETURNED:
                prevCaseOrg.setPrevStatus(PatientStatus.LTFU);
                break;
            case TRANSFERRED_IN:
                prevCaseOrg.setPrevStatus(PatientStatus.PENDING_ENROLLMENT);
                break;
        }

        prevCaseOrg.setStatus(PatientStatus.ACTIVE);
        prevCaseOrg.setEndingReason(null);
        prevCaseOrg.setEndDate(null);
        prevCaseOrg.setRefTrackingOnly(false);
        prevCaseOrg.setCurrent(true);
        prevCaseOrg.setLatestRelationship(true);

        List<CaseOrg> cos = new ArrayList<>();

        for (CaseOrg co : theCase.getCaseOrgs()) {
            if (co.getId().longValue() == latestCaseOrg.getId()) {
                continue;
            }

            if (co.getId().longValue() == prevCaseOrg.getId()) {
                cos.add(prevCaseOrg);
            } else {
                cos.add(co);
            }
        }

        coRepos.delete(latestCaseOrg);

        theCase.getCaseOrgs().clear();
        theCase.getCaseOrgs().addAll(cos);

        theCase = repos.save(theCase);

        return true;
    }

    @Override
    @Loggable
    @Transactional(rollbackFor = Exception.class)
    public boolean updateReferralResult(CaseReferralResultDto dto) {

        if (dto == null
                || dto.getTheCase() == null
                || !CommonUtils.isPositive(dto.getTheCase().getId(), true)
                || dto.getResult() < 1
                || dto.getResult() > 5
                || dto.getResultDate() == null) {
            return false;
        }

        Case theCase = null;
        Organization newOrg = null;

        theCase = repos.findOne(dto.getTheCase().getId());
        if (theCase == null) {
            return false;
        }

        CaseOrg[] txHistory = theCase.getCaseOrgs().toArray(new CaseOrg[0]);

        if (txHistory == null || txHistory.length < 2) {
            return false;
        }

        CaseOrg latestCaseOrg = txHistory[0];
        CaseOrg prevCaseOrg = txHistory[1];

        if (latestCaseOrg == null
                || prevCaseOrg == null
                || !CommonUtils.isPositive(latestCaseOrg.getId(), true)
                || !CommonUtils.isPositive(prevCaseOrg.getId(), true)) {
            return false;
        } else {
            latestCaseOrg = coRepos.findOne(latestCaseOrg.getId());
            prevCaseOrg = coRepos.findOne(prevCaseOrg.getId());
        }

        if (latestCaseOrg == null || prevCaseOrg == null) {
            return false;
        }

        switch (dto.getResult()) {
            case 1:
                // Update 'latestRelationship' of all case-org for this case/currentObj to false
                coRepos.setLatestRelToFalse(
                        theCase.getId(), latestCaseOrg.getOrganization().getId());

                // -> Patient enrolled at the receiving facility
                if (dto.getCurrentCaseOrg() != null
                        && !CommonUtils.isEmpty(dto.getCurrentCaseOrg().getPatientChartId())) {
                    latestCaseOrg.setPatientChartId(dto.getCurrentCaseOrg().getPatientChartId());
                }

                latestCaseOrg.setStartDate(dto.getResultDate());
                latestCaseOrg.setArvStartDate(
                        dto.getResultDate()); // by default patient receive ARV the first day of
                // arrival
                latestCaseOrg.setPrevStatus(PatientStatus.PENDING_ENROLLMENT);
                latestCaseOrg.setStatus(PatientStatus.ACTIVE);
                latestCaseOrg.setEnrollmentType(EnrollmentType.TRANSFERRED_IN);
                latestCaseOrg.setEndDate(null);
                latestCaseOrg.setEndingReason(null);

                latestCaseOrg.setCurrent(true);
                latestCaseOrg.setLatestRelationship(true);
                latestCaseOrg.setRefTrackingOnly(true);
                //				latestCaseOrg.setRefTrackingOnly(false);

                latestCaseOrg = coRepos.save(latestCaseOrg);
                break;
            case 2:
                // -> Patient cancelled the referral or is ltfu after being referred
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

                prevCaseOrg.setPrevStatus(PatientStatus.TRANSFERRED_OUT);
                prevCaseOrg.setStatus(PatientStatus.LTFU);
                prevCaseOrg.setEndingReason(
                        "Bệnh nhân được chuyển đi ngày "
                                + sdf.format(
                                        CommonUtils.fromLocalDateTime(prevCaseOrg.getEndDate()))
                                + " nhưng được ghi nhận mất dấu sau đó.");
                prevCaseOrg.setEndDate(dto.getResultDate());

                prevCaseOrg.setCurrent(true);

                List<CaseOrg> cos = new ArrayList<>();

                for (CaseOrg co : theCase.getCaseOrgs()) {
                    if (co.getId().longValue() == latestCaseOrg.getId()) {
                        continue;
                    }

                    if (co.getId().longValue() == prevCaseOrg.getId()) {
                        cos.add(prevCaseOrg);
                    } else {
                        cos.add(co);
                    }
                }

                coRepos.delete(latestCaseOrg);

                theCase.getCaseOrgs().clear();
                theCase.getCaseOrgs().addAll(cos);

                theCase = repos.save(theCase);
                break;
            case 3:
                // -> Patient decided to enroll in a totally new facility
                // fall through
            case 4:
                // -> Patient return to the sending facility to continue receiving treatment
                if (dto.getNewOrg() == null
                        || !CommonUtils.isPositive(dto.getNewOrg().getId(), true)) {
                    return false;
                } else {
                    newOrg = orgRepos.findOne(dto.getNewOrg().getId());
                }

                if (newOrg == null) {
                    return false;
                }

                // -> cancelled enrollment for the receiving facility

                // Update 'latestRelationship' of all case-org for this case/currentObj to false
                coRepos.setLatestRelToFalse(
                        theCase.getId(), latestCaseOrg.getOrganization().getId());

                latestCaseOrg.setPrevStatus(PatientStatus.PENDING_ENROLLMENT);
                latestCaseOrg.setStatus(PatientStatus.CANCELLED_ENROLLMENT);
                latestCaseOrg.setEnrollmentType(EnrollmentType.TRANSFERRED_IN);
                if (dto.getResult() == 3) {
                    latestCaseOrg.setEndingReason(
                            "Bệnh nhân đã tới đăng ký điều trị ở một cơ sở khác.");
                } else if (dto.getResult() == 4) {
                    latestCaseOrg.setEndingReason(
                            "Bệnh nhân không tới cơ sở tiếp nhận mà quay lại cơ sở chuyển gửi để tiếp tục điều trị.");
                }
                latestCaseOrg.setCurrent(false);
                latestCaseOrg.setLatestRelationship(true);
                latestCaseOrg.setRefTrackingOnly(true);

                latestCaseOrg = coRepos.save(latestCaseOrg);

                // Create a new case/org to reflect the actual receiving facility

                // Update 'latestRelationship' of all case-org for this case/currentObj to false
                coRepos.setLatestRelToFalse(theCase.getId(), newOrg.getId());

                CaseOrg newCaseOrg = new CaseOrg();
                newCaseOrg.setUid(UUID.randomUUID());
                newCaseOrg.setOrganization(newOrg);
                newCaseOrg.setTheCase(theCase);

                if (dto.getCurrentCaseOrg() != null
                        && !CommonUtils.isEmpty(dto.getCurrentCaseOrg().getPatientChartId())) {
                    newCaseOrg.setPatientChartId(dto.getCurrentCaseOrg().getPatientChartId());
                }

                newCaseOrg.setOrganizationName(dto.getNewOrgName());
                newCaseOrg.setStartDate(dto.getResultDate());
                newCaseOrg.setArvStartDate(dto.getResultDate());
                newCaseOrg.setEndDate(null);
                newCaseOrg.setEndingReason(null);
                if (dto.getResult() == 3) {
                    newCaseOrg.setPrevStatus(PatientStatus.PENDING_ENROLLMENT);
                    newCaseOrg.setRefTrackingOnly(true);
                } else if (dto.getResult() == 4) {
                    newCaseOrg.setPrevStatus(PatientStatus.TRANSFERRED_OUT);
                    newCaseOrg.setRefTrackingOnly(false);
                }

                newCaseOrg.setStatus(PatientStatus.ACTIVE);
                newCaseOrg.setEnrollmentType(EnrollmentType.RETURNED);

                newCaseOrg.setCurrent(true);
                newCaseOrg.setLatestRelationship(true);

                newCaseOrg = coRepos.save(newCaseOrg);

                break;
            case 5:
                // -> Patient return to the sending facility to continue receiving treatment
                if (dto.getNewOrg() == null
                        || !CommonUtils.isPositive(dto.getNewOrg().getId(), true)) {
                    return false;
                } else {
                    newOrg = orgRepos.findOne(dto.getNewOrg().getId());
                }

                if (newOrg == null) {
                    return false;
                }

                latestCaseOrg.setOrganization(newOrg);
                latestCaseOrg.setOrganizationName(dto.getNewOrgName());
                latestCaseOrg.setStartDate(dto.getResultDate());
                latestCaseOrg.setArvStartDate(
                        dto.getResultDate()); // by default patient receive ARV the first day of
                // arrival
                latestCaseOrg = coRepos.save(latestCaseOrg);
                break;
        }

        return true;
    }

    @Override
    @Loggable
    @Transactional(rollbackFor = Exception.class)
    public boolean reEnrollPatient(CaseOrgUpdateDto dto) {

        if (dto == null
                || dto.getOriginStatus() == null
                || dto.getNewObj() == null
                || dto.getNewObj().getOrganization() == null
                || !CommonUtils.isPositive(dto.getNewObj().getOrganization().getId(), true)
                || dto.getNewObj().getTheCase() == null
                || !CommonUtils.isPositive(dto.getNewObj().getTheCase().getId(), true)
                || dto.getNewObj().getStartDate() == null) {
            return false;
        }

        // The patient is either dropped out of the service, was transferred out from
        // the prev facility and go to this current facility for enrollment
        if (dto.getOriginStatus() != PatientStatus.LTFU
                && dto.getOriginStatus() != PatientStatus.TRANSFERRED_OUT) {
            return false;
        }

        Case theCase = null;
        Organization newOrg = null;

        theCase = repos.findOne(dto.getNewObj().getTheCase().getId());
        if (theCase == null) {
            return false;
        }

        newOrg = orgRepos.findOne(dto.getNewObj().getOrganization().getId());

        if (newOrg == null) {
            return false;
        }

        CaseOrg[] txHistory = theCase.getCaseOrgs().toArray(new CaseOrg[0]);

        if (txHistory == null || txHistory.length < 1) {
            return false;
        }

        if (txHistory.length >= 1) {
            CaseOrg latestCaseOrg = txHistory[0];

            if (latestCaseOrg == null || !CommonUtils.isPositive(latestCaseOrg.getId(), true)) {
                return false;
            } else {
                latestCaseOrg = coRepos.findOne(latestCaseOrg.getId());
            }

            if (latestCaseOrg == null) {
                return false;
            }

            // Update the relationship of the case with the previous facility

            // Update 'latestRelationship' of all case-org for this case/currentObj to false
            coRepos.setLatestRelToFalse(theCase.getId(), latestCaseOrg.getOrganization().getId());

            latestCaseOrg.setPrevStatus(latestCaseOrg.getStatus());
            latestCaseOrg.setStatus(dto.getOriginStatus());

            if (dto.getCurrentObj() != null && dto.getCurrentObj().getEndDate() != null) {
                latestCaseOrg.setEndDate(dto.getCurrentObj().getEndDate());
            }

            if (latestCaseOrg.getPrevStatus() != PatientStatus.PENDING_ENROLLMENT
                    && latestCaseOrg.getEndDate() == null) {
                latestCaseOrg.setEndDate(dto.getNewObj().getStartDate());
            }

            if (dto.getOriginStatus() == PatientStatus.LTFU) {
                latestCaseOrg.setEndingReason("Bệnh nhân bỏ trị/không tới đăng ký.");
            } else if (dto.getOriginStatus() == PatientStatus.TRANSFERRED_OUT) {
                latestCaseOrg.setEndingReason("Bệnh nhân được chuyển đi.");
            }
            latestCaseOrg.setCurrent(false);

            // if the latest org is the same with the new org -> set latest rel to FALSE,
            // otherwise set to TRUE
            latestCaseOrg.setLatestRelationship(
                    newOrg.getId() != latestCaseOrg.getOrganization().getId().longValue());

            latestCaseOrg = coRepos.save(latestCaseOrg);
        }

        // Create a new relationship with the new (current) facility

        // Update 'latestRelationship' of all case-org for this case/currentObj to false
        coRepos.setLatestRelToFalse(theCase.getId(), newOrg.getId());

        CaseOrg newCaseOrg = new CaseOrg();
        newCaseOrg.setUid(UUID.randomUUID());
        newCaseOrg.setOrganization(newOrg);
        newCaseOrg.setTheCase(theCase);

        newCaseOrg.setPatientChartId(dto.getNewObj().getPatientChartId());
        newCaseOrg.setStartDate(dto.getNewObj().getStartDate());
        newCaseOrg.setArvStartDate(dto.getNewObj().getStartDate()); // by default
        newCaseOrg.setEndDate(null);
        newCaseOrg.setEndingReason(null);

        if (!CommonUtils.isEmpty(dto.getNewObj().getPatientChartId())) {
            newCaseOrg.setPatientChartId(dto.getNewObj().getPatientChartId());
        }

        if (dto.getOriginStatus() == PatientStatus.LTFU) {
            newCaseOrg.setPrevStatus(PatientStatus.NULL);
            newCaseOrg.setEnrollmentType(EnrollmentType.RETURNED);
        } else if (dto.getOriginStatus() == PatientStatus.TRANSFERRED_OUT) {
            newCaseOrg.setPrevStatus(PatientStatus.PENDING_ENROLLMENT);
            newCaseOrg.setEnrollmentType(EnrollmentType.TRANSFERRED_IN);
        }

        newCaseOrg.setStatus(PatientStatus.ACTIVE);

        newCaseOrg.setCurrent(true);
        newCaseOrg.setLatestRelationship(true);
        newCaseOrg.setRefTrackingOnly(false);

        newCaseOrg = coRepos.save(newCaseOrg);

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public CaseOrgDto findCaseOrgById(Long id) {

        if (!CommonUtils.isPositive(id, true)) {
            return null;
        }

        CaseOrg entity = coRepos.findOne(id);

        if (entity != null) {
            CaseOrgDto dto = new CaseOrgDto(entity, false);

            // set default ending reason
            if (dto.getEndDate() != null && CommonUtils.isEmpty(dto.getEndingReason())) {
                switch (dto.getStatus()) {
                    case DEAD:
                        dto.setEndingReason("Bệnh nhân tử vong");
                        break;

                    case LTFU:
                        dto.setEndingReason("Bệnh nhân bỏ trị");
                        break;

                    case TRANSFERRED_OUT:
                        dto.setEndingReason("Bệnh nhân được chuyển đi");
                        break;

                    default:
                        break;
                }
            }

            return dto;
        }

        return null;
    }

    @Override
    @Loggable
    @Transactional(rollbackFor = Exception.class)
    public CaseOrgDto updateCaseOrg(CaseOrgDto dto) {

        if (dto == null) {
            throw new IllegalArgumentException("Case-Org to update must not be null!");
        }

        CaseOrg entity = null;

        if (CommonUtils.isPositive(dto.getId(), true)) {
            entity = coRepos.findOne(dto.getId());
        }

        if (dto.getOrganization() == null
                || dto.getTheCase() == null
                || !CommonUtils.isPositive(dto.getOrganization().getId(), true)
                || !CommonUtils.isPositive(dto.getTheCase().getId(), true)) {
            throw new IllegalArgumentException(
                    "Case-Org to add must contain valid organization and case data!");
        }

        Organization org = orgRepos.findOne(dto.getOrganization().getId());
        Case theCase = repos.findOne(dto.getTheCase().getId());

        if (org == null || theCase == null) {
            throw new RuntimeException("Invalid organization/case ID!");
        }

        if (entity == null) {
            // create a case-org (for retrospective data entry)
            entity = dto.toEntity();
            entity.setUid(UUID.randomUUID());

            PatientStatus prevStatus = PatientStatus.NULL;
            switch (entity.getEnrollmentType()) {
                case NEWLY_ENROLLED:
                    prevStatus = PatientStatus.NULL;
                    break;
                case RETURNED:
                    prevStatus = PatientStatus.LTFU;
                    break;
                case TRANSFERRED_IN:
                    prevStatus = PatientStatus.PENDING_ENROLLMENT;
                    break;
            }

            entity.setCurrent(false);
            entity.setLatestRelationship(false);
            entity.setPrevStatus(prevStatus);
            entity.setRefTrackingOnly(false);
            entity.setEndingReason(entity.getStatus().toString());
        } else {
            // update a case-org
            entity.setStatus(dto.getStatus());
            entity.setEnrollmentType(dto.getEnrollmentType());

            entity.setStartDate(dto.getStartDate());
            entity.setPatientChartId(dto.getPatientChartId());
            entity.setArvStartDate(dto.getArvStartDate());
            entity.setEndDate(dto.getEndDate());
            entity.setEndingReason(dto.getEndingReason());
            entity.setArvGroup(dto.getArvGroup());
        }

        if (entity.getEndDate() != null && CommonUtils.isEmpty(entity.getEndingReason(), true)) {
            switch (entity.getStatus()) {
                case DEAD:
                    entity.setEndingReason("Bệnh nhân đã tử vong.");
                    break;
                case LTFU:
                    entity.setEndingReason("Bệnh nhân đã bỏ trị.");
                    break;
                case TRANSFERRED_OUT:
                    entity.setEndingReason("Bệnh nhân đã chuyển đi.");
                    break;
                default:
                    break;
            }
        }

        entity.setOrganization(org);
        entity.setTheCase(theCase);

        // Check and see if the patient has only one CaseOrg relationship
        // If yes then need to set CURRENT = 1 AND LATEST_REL = 1
        List<CaseOrg> cos = Lists.newArrayList(theCase.getCaseOrgs());
        if (cos != null && cos.size() == 1) {
            entity.setCurrent(true);
            entity.setLatestRelationship(true);
        }

        entity = coRepos.save(entity);

        if (entity != null) {
            return new CaseOrgDto(entity, false);
        } else {
            throw new RuntimeException("Error updating Case-Org!");
        }
    }

    @Override
    @Loggable
    @Transactional(rollbackFor = Exception.class)
    public void deleteCaseOrg(CaseOrgDto dto) {
        if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
            throw new IllegalArgumentException("Case-Org to delete must not be null!");
        }

        CaseOrg co = coRepos.findOne(dto.getId());

        if (co == null) {
            throw new IllegalArgumentException("Case-Org to delete not found!");
        }

        Case theCase = co.getTheCase();
        List<CaseOrg> cos = Lists.newArrayList(theCase.getCaseOrgs());
        if (cos == null || cos.size() <= 1) {
            throw new IllegalArgumentException(
                    "There must be at least one case-org relationship maintained. Delete is not allowed for patient "
                            + "ID = "
                            + theCase.getId()
                            + ".!");
        }

        theCase.getCaseOrgs().remove(co);

        coRepos.delete(co);

        // Check and see if the patient has only one CaseOrg relationship
        // If yes then need to set CURRENT = 1 AND LATEST_REL = 1
        if (cos != null && cos.size() == 1) {
            co = cos.get(0);

            co.setCurrent(true);
            co.setLatestRelationship(true);

            coRepos.save(co);
        }
    }

    @Override
    @Loggable
    @Transactional(rollbackFor = Exception.class)
    public int updateTreatmentStatus(CaseOrgUpdateDto dto) {

        // -1 - update error
        // 0 - update success
        // 1 - is patient is not editable
        final int UPDATE_ERROR = -1;
        final int UPDATE_SUCCESS = 0;
        final int NOT_EDITABLE = 1;

        List<Long> grantedOrgIds = authUtils.getGrantedOrgIds(Permission.WRITE_ACCESS);

        if (CommonUtils.isEmpty(grantedOrgIds)) {
            return UPDATE_ERROR;
        }

        if (dto.getTargetStatus() == null
                || dto.getCurrentObj() == null
                || !CommonUtils.isTrue(dto.getCurrentObj().getCurrent())
                || !CommonUtils.isPositive(dto.getCurrentObj().getId(), true)
                || dto.getCurrentObj().getOrganization() == null
                || dto.getCurrentObj().getTheCase() == null
                || !CommonUtils.isPositive(dto.getCurrentObj().getOrganization().getId(), true)
                || !CommonUtils.isPositive(dto.getCurrentObj().getTheCase().getId(), true)) {
            return UPDATE_ERROR;
        }

        if (Lists.newArrayList(PatientStatus.TRANSFERRED_OUT)
                .contains(dto.getCurrentObj().getStatus())) {
            return NOT_EDITABLE;
        }

        if (PatientStatus.PENDING_ENROLLMENT == dto.getCurrentObj().getStatus()
                && PatientStatus.ACTIVE != dto.getTargetStatus()) {
            // Not enrolling a patient
            return NOT_EDITABLE;
        }

        Case theCase = repos.findOne(dto.getCurrentObj().getTheCase().getId());
        Organization currentOrg = orgRepos.findOne(dto.getCurrentObj().getOrganization().getId());

        // either the case does not exist, or the current org in the dto does not exist
        // or the current user does not
        // have write privilege to the current org
        if (theCase == null
                || currentOrg == null
                || !grantedOrgIds.contains(currentOrg.getId().longValue())) {
            throw new RuntimeException();
        }

        CaseOrg currentObj = coRepos.findOne(dto.getCurrentObj().getId());

        if (currentObj == null) {
            throw new RuntimeException();
        }

        // Update 'latestRelationship' of all case-org for this case/currentObj to false
        coRepos.setLatestRelToFalse(theCase.getId(), currentOrg.getId());

        if (dto.getTargetStatus() == PatientStatus.TRANSFERRED_OUT) {
            if (dto.getNewObj() == null
                    || dto.getNewObj().getOrganization() == null
                    || !CommonUtils.isPositive(dto.getNewObj().getOrganization().getId(), true)) {
                throw new RuntimeException();
            }

            Organization newOrg = orgRepos.findOne(dto.getNewObj().getOrganization().getId());

            // either the new org does not exist or the two orgs are the same one in a
            // TRANSFERR OUT request
            if (newOrg == null || newOrg.getId().longValue() == currentOrg.getId()) {
                throw new RuntimeException();
            }

            // -> otherwise, update the current obj AND the new obj
            // 1. update the current obj
            currentObj.setEndDate(dto.getCurrentObj().getEndDate());
            currentObj.setEndingReason(dto.getCurrentObj().getEndingReason());
            currentObj.setPrevStatus(dto.getCurrentObj().getStatus());
            currentObj.setStatus(dto.getTargetStatus());
            currentObj.setCurrent(false);

            currentObj.setLatestRelationship(true);
            currentObj.setRefTrackingOnly(false);

            currentObj = coRepos.save(currentObj);

            // 2. update the new obj

            // Update 'latestRelationship' of all case-org for this case/newObj to false
            coRepos.setLatestRelToFalse(theCase.getId(), newOrg.getId());

            CaseOrg newObj = new CaseOrg();
            newObj.setUid(UUID.randomUUID());
            newObj.setOrganization(newOrg);
            newObj.setTheCase(theCase);
            newObj.setOrganizationName(dto.getNewObj().getOrganizationName());
            newObj.setStartDate(dto.getNewObj().getStartDate());
            newObj.setEndDate(null);
            newObj.setEndingReason(null);
            newObj.setPrevStatus(
                    newObj.getStatus() == null ? PatientStatus.NULL : newObj.getStatus());
            newObj.setStatus(dto.getNewObj().getStatus());
            newObj.setEnrollmentType(EnrollmentType.TRANSFERRED_IN);
            newObj.setCurrent(true);

            // This need to wait until confirm arrival to set to true
            newObj.setLatestRelationship(true);
            newObj.setRefTrackingOnly(false);

            newObj = coRepos.save(newObj);

        } else {
            // update the current obj only
            if (dto.getTargetStatus() == PatientStatus.ACTIVE) {
                // --> back to treatment
                if (dto.getCurrentObj().getStatus() == PatientStatus.PENDING_ENROLLMENT) {
                    // If referred-in then
                    if (dto.getCurrentObj().isRefTrackingOnly()) {
                        currentObj.setRefTrackingOnly(true);
                    } else {
                        currentObj.setPatientChartId(dto.getCurrentObj().getPatientChartId());
                        currentObj.setStartDate(dto.getCurrentObj().getStartDate());
                    }

                    currentObj.setEndDate(null);
                    currentObj.setEndingReason(null);
                    currentObj.setPrevStatus(PatientStatus.PENDING_ENROLLMENT);
                    currentObj.setStatus(PatientStatus.ACTIVE);
                    currentObj.setCurrent(true);
                } else {
                    // Else (LTFU -> resume, or DEAD -> resume)
                    currentObj.setCurrent(false);
                    currentObj.setLatestRelationship(false);
                    currentObj.setRefTrackingOnly(false);
                    currentObj = coRepos.save(currentObj);

                    String existingPatientChardId = currentObj.getPatientChartId();

                    currentObj = new CaseOrg();
                    currentObj.setUid(UUID.randomUUID());
                    currentObj.setOrganization(currentOrg);
                    currentObj.setTheCase(theCase);

                    currentObj.setPatientChartId(existingPatientChardId);
                    currentObj.setStartDate(dto.getCurrentObj().getStartDate());
                    currentObj.setEndDate(null);
                    currentObj.setEndingReason(null);
                    currentObj.setPrevStatus(dto.getCurrentObj().getStatus());
                    currentObj.setStatus(PatientStatus.ACTIVE);
                    currentObj.setEnrollmentType(EnrollmentType.RETURNED);
                    currentObj.setCurrent(true);
                    currentObj.setRefTrackingOnly(false);
                }
            } else {
                // --> LTFU or DEAD
                currentObj.setEndDate(dto.getCurrentObj().getEndDate());
                currentObj.setEndingReason(dto.getCurrentObj().getEndingReason());
                currentObj.setPrevStatus(dto.getCurrentObj().getStatus());
                currentObj.setStatus(dto.getTargetStatus());
                currentObj.setRefTrackingOnly(false);
            }

            currentObj.setLatestRelationship(true);
            currentObj = coRepos.save(currentObj);
        }

        return UPDATE_SUCCESS;
    }

    @Override
    @Loggable
    @Transactional(rollbackFor = Exception.class)
    public void updateOldNewCaseStatus() {}

    @Override
    @Loggable
    @Transactional(rollbackFor = Exception.class)
    public void deleteMultiple(CaseDto[] dtos) {

        if (CommonUtils.isEmpty(dtos)) {
            return;
        }

        for (CaseDto dto : dtos) {
            if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
                continue;
            }

            Case entity = repos.findOne(dto.getId());

            if (entity != null) {
                repos.delete(entity);
            }
        }
    }

    @Override
    @Loggable
    @Transactional(rollbackFor = Exception.class)
    public void deleteByOrganization(CaseDeleteFilterDto filter) {

        if (filter == null
                || CommonUtils.isEmpty(filter.getPasscode())
                || !"2019!2020".equals(filter.getPasscode())
                || !CommonUtils.isPositive(filter.getOrgId(), true)) {
            System.out.println("Invalid passcode and/or organization ID!");
            return;
        }

        QCase q = QCase.case$;
        QCaseOrg qco = QCaseOrg.caseOrg;

        JPAQuery<Case> query =
                new JPAQuery<Case>(em)
                        .from(q)
                        .innerJoin(q.caseOrgs, qco)
                        .where(qco.organization.id.longValue().eq(filter.getOrgId()));

        List<Case> cases = query.fetch();

        if (cases == null) {
            return;
        }

        for (Case c : cases) {
            System.out.println("Deleting case with UID = " + c.getUid().toString());
            repos.delete(c);
        }
    }

    @Override
    @Loggable
    @Transactional(rollbackFor = Exception.class)
    public void keepCaseEntityUp2Date() {
        System.out.println("BEGIN: House keeping for cases' clinical stage.");
        int affectedRows = repos.housekeeping4ClinicalStage();
        System.out.println(
                "END: House keeping for cases' clinical stage --> "
                        + affectedRows
                        + " rows affected.");

        System.out.println("BEGIN: House keeping for cases' ART data.");
        affectedRows = repos.housekeeping4ART();
        System.out.println(
                "END: House keeping for cases' ART data --> " + affectedRows + " rows affected.");

        System.out.println(
                "BEGIN: House keeping for cases enrollment status (from weekly reports).");
        affectedRows = repos.housekeepingCoEnrollmentStatus();
        System.out.println(
                "END: House keeping for cases' enrollment status --> "
                        + affectedRows
                        + " rows affected.");
    }

    /// custom export for adhoc data request
    @Override
    @Transactional(readOnly = true)
    public Workbook exportListOfPatients() {
        List<Case> cases = repos.getDistinctCases();

        Workbook wbook = null;
        try (InputStream template =
                context.getResource(
                                "classpath:templates/_distinct-cases-with-latest-vltests-template.xlsx")
                        .getInputStream()) {
            wbook = new SXSSFWorkbook(new XSSFWorkbook(template), 100);
        } catch (IOException e) {
            e.printStackTrace();
        }

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

        int colIndex = 0;
        int rowIndex = 4;

        List<PatientStatus> acceptedStatuses =
                Lists.newArrayList(
                        PatientStatus.ACTIVE,
                        PatientStatus.DEAD,
                        PatientStatus.LTFU,
                        PatientStatus.TRANSFERRED_OUT);
        LocalDateTime cutpoint = LocalDateTime.of(2021, 3, 31, 23, 59, 59);

        Sheet sheet = wbook.getSheetAt(0);
        Row row = null;
        Cell cell = null;

        // Start filling out data...
        for (Case c : cases) {
            colIndex = 0;

            // find latest case-org prior to March 31, 2021
            CaseOrg co = null;
            Iterator<CaseOrg> itr = c.getCaseOrgs().iterator();
            while (itr.hasNext()) {
                CaseOrg _co = itr.next();

                boolean shouldPick = false;

                if (acceptedStatuses.contains(_co.getStatus())) {
                    if (_co.getStatus() == PatientStatus.ACTIVE) {
                        if (CommonUtils.dateDiff(ChronoUnit.DAYS, _co.getStartDate(), cutpoint)
                                >= 0) {
                            shouldPick = true;
                        }
                    } else if (_co.getEndDate() != null
                            && CommonUtils.dateDiff(ChronoUnit.DAYS, _co.getEndDate(), cutpoint)
                                    >= 0) {
                        shouldPick = true;
                    }
                }

                if (shouldPick) {
                    co = _co;
                    break;
                }
            }

            if (co == null) {
                continue;
            }

            row = sheet.createRow(rowIndex++);
            row.setHeightInPoints(22);

            Person person = c.getPerson();

            // Mã ID
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(c.getId().toString());

            // Tỉnh - thành phố của cơ sở
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);

            Organization facility = co.getOrganization();
            if (facility != null
                    && facility.getAddress() != null
                    && facility.getAddress().getProvince() != null) {
                cell.setCellValue(facility.getAddress().getProvince().getName());
            }

            // Cơ sở điều trị
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (facility != null) {
                cell.setCellValue(facility.getName());
            }

            // Mã bệnh án tại cơ sở điều trị
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (co.getPatientChartId() != null) {
                cell.setCellValue(co.getPatientChartId());
            } else {
                cell.setCellValue("-");
            }

            // Mã HIVInfo
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (c.getHivInfoID() != null) {
                cell.setCellValue(c.getHivInfoID());
            } else {
                cell.setCellValue("-");
            }

            // Giới tính
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(person.getGender().toString());

            // Ngày sinh
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellValue(CommonUtils.fromLocalDateTime(person.getDob()));
            cell.setCellStyle(dateCellStyle);

            // Nhóm nguy cơ gần nhất
            RiskInterview riskInterview = null;

            // get the latest risk interview entity
            if (c.getRiskInterviews() != null) {
                Iterator<RiskInterview> risks = c.getRiskInterviews().iterator();
                if (risks.hasNext()) {
                    riskInterview = risks.next();
                }
            }

            String riskName = RiskGroupUtils.getPrioritizedRiskName(riskInterview);

            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (riskName != null) {
                cell.setCellValue(riskName);
            } else {
                cell.setCellValue("-");
            }

            // Addresses
            Location rAddress = null;
            Location cAddress = null;

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

            // Residential address
            if (rAddress != null) {
                // R address - details
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue("-");

                // R address - commune
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue("-");

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
                cell.setCellValue("-");

                // C address - commune
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue("-");

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
            if (c.getHivConfirmDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(c.getHivConfirmDate()));
            } else {
                cell.setCellValue("-");
            }

            // Ngày bắt đầu ARV
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (c.getArvStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(c.getArvStartDate()));
            } else {
                cell.setCellValue("-");
            }

            // Ngày đăng ký tại cơ sở điều trị
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (co.getArvStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(co.getStartDate()));
            } else {
                cell.setCellValue("-");
            }

            // Loại đăng ký tại cơ sở hiện tại
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
            cell.setCellValue(
                    !CommonUtils.isEmpty(c.getCurrentArvRegimenName())
                            ? c.getCurrentArvRegimenName()
                            : "-");

            // Ngày bắt đầu phác đồ hiện tại
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (c.getCurrentArvRegimenStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(c.getCurrentArvRegimenStartDate()));
            } else {
                cell.setCellValue("-");
            }

            // Ngày chuyển phác đồ bậc 2
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (c.getSecondLineStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(c.getSecondLineStartDate()));
            } else {
                cell.setCellValue("-");
            }

            List<LabTest> vlTests =
                    c.getLabTests().parallelStream()
                            .filter(
                                    t ->
                                            t.getTestType() == ClinicalTestingType.VIRAL_LOAD
                                                    && CommonUtils.dateDiff(
                                                                    ChronoUnit.DAYS,
                                                                    t.getSampleDate(),
                                                                    cutpoint)
                                                            >= 0)
                            .collect(Collectors.toList());
            if (vlTests != null && vlTests.size() > 0) {
                LabTest latestTest = vlTests.get(0);

                // Ngày lấy mẫu XN TLVR gần đây nhất
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                cell.setCellValue(CommonUtils.fromLocalDateTime(latestTest.getSampleDate()));

                // Ngày có kết quả XN TLVR gần đây nhất
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                if (latestTest.getResultDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(latestTest.getResultDate()));
                } else {
                    cell.setCellValue("-");
                }

                // Kết quả TLVR gần đây nhất
                if (CommonUtils.isPositive(latestTest.getResultNumber(), false)) {

                    if (latestTest.getResultNumber() == 0) {
                        cell = row.createCell(colIndex++, CellType.STRING);
                        cell.setCellValue("Không phát hiện");
                    } else {
                        cell = row.createCell(colIndex++, CellType.NUMERIC);
                        cell.setCellValue(latestTest.getResultNumber());
                    }
                } else {
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellValue("-");
                }
                cell.setCellStyle(cellStyle);

                // Lý do xét nghiệm TLVR gần đây nhất
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(latestTest.getReasonForTesting().toString());

            } else {
                for (int i = 0; i < 4; i++) {
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue("-");
                }
            }

            // Tình trạng bệnh nhân
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue((co.getStatus() != null) ? co.getStatus().toString() : "-");

            // Ngày kết thúc điều trị
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (co.getStatus() == PatientStatus.ACTIVE) {
                cell.setCellValue("-");
            } else if (co.getEndDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(co.getEndDate()));
            }
        }

        // Auto-filter
        if (rowIndex >= 4) {
            sheet.setAutoFilter(CellRangeAddress.valueOf("A4:AC" + rowIndex));
        }

        return wbook;
    }

    /**
     * Query for case-orgs
     *
     * @param filter
     * @param orgIds
     * @return
     */
    private QueryResult queryCaseOrgs(CaseFilterDto filter, List<Long> orgIds) {

        QueryResult queryResult = new QueryResult(0, new ArrayList<>());

        if (CommonUtils.isEmpty(orgIds)) {
            return queryResult;
        }

        int offset = filter.getPageIndex() * filter.getPageSize();
        int limit = filter.getPageSize();

        if (filter.getOrganization() != null
                && CommonUtils.isPositive(filter.getOrganization().getId(), true)
                && orgIds.contains(filter.getOrganization().getId().longValue())) {
            orgIds.clear();
            orgIds.add(filter.getOrganization().getId());
        }

        long totalElements = 0;
        List<CaseOrg> content = new ArrayList<>();

        Timestamp confirmDateFrom = null;
        Timestamp confirmDateTo = null;
        Timestamp arvStartDateFrom = null;
        Timestamp arvStartDateTo = null;
        Timestamp dobFrom = null;
        Timestamp dobTo = null;
        Timestamp appFromDate = null;
        Timestamp appToDate = null;

        if (filter.getHivConfirmDateFrom() != null) {
            confirmDateFrom = CommonUtils.toTimestamp(filter.getHivConfirmDateFrom());
        }

        if (filter.getHivConfirmDateTo() != null) {
            confirmDateTo = CommonUtils.toTimestamp(filter.getHivConfirmDateTo());
        }

        if (filter.getArvStartDateFrom() != null) {
            arvStartDateFrom = CommonUtils.toTimestamp(filter.getArvStartDateFrom());
        }

        if (filter.getArvStartDateTo() != null) {
            arvStartDateTo = CommonUtils.toTimestamp(filter.getArvStartDateTo());
        }

        if (filter.getAgeFrom() != null) {
            dobFrom =
                    CommonUtils.toTimestamp(CommonUtils.hanoiNow().minusYears(filter.getAgeFrom()));
        }

        if (filter.getAgeTo() != null) {
            dobTo = CommonUtils.toTimestamp(CommonUtils.hanoiNow().minusYears(filter.getAgeTo()));
        }

        if (filter.getAppointmentMonth() != null) {
            LocalDateTime tmp = filter.getAppointmentMonth();
            appFromDate = CommonUtils.toTimestamp(tmp);
            appToDate =
                    CommonUtils.toTimestamp(
                            CommonUtils.dateEnd(
                                    tmp.withDayOfMonth(
                                            tmp.getMonth()
                                                    .length(tmp.toLocalDate().isLeapYear()))));
        }

        String gender = filter.getGender() != null ? filter.getGender().name() : null;
        String enrollmentType =
                filter.getEnrollmentType() != null ? filter.getEnrollmentType().name() : null;
        String txStatus =
                filter.getPatientStatus() != null ? filter.getPatientStatus().name() : null;
        boolean includeDeleted = filter.isIncludeDeleted();
        boolean includeOnART = filter.isIncludeOnART();
        boolean includePreART = filter.isIncludePreART();
        String appResult =
                filter.getAppointmentResult() != null ? filter.getAppointmentResult().name() : null;

        if (appResult == null) {
            totalElements =
                    coRepos.countCaseOrgs_WoAppointment_Total(
                                    orgIds,
                                    filter.getKeyword(),
                                    filter.getArvGroup(),
                                    confirmDateFrom,
                                    confirmDateTo,
                                    arvStartDateFrom,
                                    arvStartDateTo,
                                    dobFrom,
                                    dobTo,
                                    gender,
                                    enrollmentType,
                                    txStatus,
                                    includeDeleted,
                                    includeOnART,
                                    includePreART)
                            .longValue();

            content =
                    coRepos.findCaseOrgs_WoAppointment_Pageable(
                            orgIds,
                            filter.getKeyword(),
                            filter.getArvGroup(),
                            confirmDateFrom,
                            confirmDateTo,
                            arvStartDateFrom,
                            arvStartDateTo,
                            dobFrom,
                            dobTo,
                            gender,
                            enrollmentType,
                            txStatus,
                            includeDeleted,
                            includeOnART,
                            includePreART,
                            filter.getSortField(),
                            offset,
                            limit);
        } else {
            totalElements =
                    coRepos.countCaseOrgs_WAppointment_Total(
                                    orgIds,
                                    filter.getKeyword(),
                                    confirmDateFrom,
                                    confirmDateTo,
                                    arvStartDateFrom,
                                    arvStartDateTo,
                                    dobFrom,
                                    dobTo,
                                    gender,
                                    enrollmentType,
                                    txStatus,
                                    includeDeleted,
                                    includeOnART,
                                    includePreART,
                                    appResult,
                                    appFromDate,
                                    appToDate)
                            .longValue();

            content =
                    coRepos.findCaseOrgs_WAppointment_Pageable(
                            orgIds,
                            filter.getKeyword(),
                            confirmDateFrom,
                            confirmDateTo,
                            arvStartDateFrom,
                            arvStartDateTo,
                            dobFrom,
                            dobTo,
                            gender,
                            enrollmentType,
                            txStatus,
                            includeDeleted,
                            includeOnART,
                            includePreART,
                            appResult,
                            appFromDate,
                            appToDate,
                            filter.getSortField(),
                            offset,
                            limit);
        }

        queryResult.setTotalElements(totalElements);
        queryResult.setContent(content);

        return queryResult;
    }

    /**
     * Create query for listing patients
     *
     * @param filter
     * @param orgIds
     * @return
     */
    @SuppressWarnings("unused")
    @Deprecated
    private JPAQuery<CaseOrg> createSearchQuery(CaseFilterDto filter, final List<Long> orgIds) {

        QCase q = QCase.case$;
        QCaseOrg qco = QCaseOrg.caseOrg;
        QAppointment qa = QAppointment.appointment;

        // predicates
        List<Predicate> predicates = new LinkedList<>();
        predicates.add(qco.organization.id.longValue().in(orgIds));
        predicates.add(qco.latestRelationship.isTrue());
        predicates.add(qco.refTrackingOnly.isFalse());

        JPAQuery<CaseOrg> query = new JPAQuery<CaseOrg>(em).from(qco).innerJoin(qco.theCase, q);

        if (filter.getOrganization() != null
                && CommonUtils.isPositive(filter.getOrganization().getId(), true)
                && orgIds.contains(filter.getOrganization().getId().longValue())) {
            predicates.add(qco.organization.id.longValue().eq(filter.getOrganization().getId()));
        }

        if (!CommonUtils.isEmpty(filter.getKeyword())) {
            predicates.add(
                    qco.patientChartId
                            .containsIgnoreCase(filter.getKeyword())
                            .or(q.person.fullname.containsIgnoreCase(filter.getKeyword()))
                            .or(
                                    q.person
                                            .mobilePhone
                                            .isNotNull()
                                            .and(
                                                    q.person.mobilePhone.containsIgnoreCase(
                                                            filter.getKeyword()))));
        }

        if (filter.getHivConfirmDateFrom() != null) {
            predicates.add(q.hivConfirmDate.goe(filter.getHivConfirmDateFrom()));
        }

        if (filter.getHivConfirmDateTo() != null) {
            predicates.add(q.hivConfirmDate.loe(filter.getHivConfirmDateTo()));
        }

        if (filter.getArvStartDateFrom() != null) {
            predicates.add(q.arvStartDate.goe(filter.getArvStartDateFrom()));
        }

        if (filter.getArvStartDateTo() != null) {
            predicates.add(q.arvStartDate.loe(filter.getArvStartDateTo()));
        }

        //		if (!CommonUtils.isEmpty(filter.getArvCohort())) {
        //			predicates.add(q.arvCohort.containsIgnoreCase(filter.getArvCohort()));
        //		}

        if (CommonUtils.isPositive(filter.getAgeFrom(), false)) {
            LocalDateTime left = CommonUtils.hanoiNow().minusYears(filter.getAgeFrom());
            predicates.add(q.person.dob.isNotNull().and(q.person.dob.loe(left)));
        }

        if (CommonUtils.isPositive(filter.getAgeTo(), false)) {
            LocalDateTime right = CommonUtils.hanoiNow().minusYears(filter.getAgeTo());
            predicates.add(q.person.dob.isNotNull().and(q.person.dob.goe(right)));
        }

        if (filter.getGender() != null) {
            predicates.add(q.person.gender.eq(filter.getGender()));
        }

        //		if (filter.isMissingData()) {
        //			// includes only records that are missing some 'required' data elements
        //
        //	predicates.add(q.hivInfoID.isNull().or(q.hivInfoID.isEmpty()).or(q.hivScreenDate.isNull())
        //					.or(q.currentArvRegimenLine.isNull()).or(q.currentArvRegimenName.isNull())
        //					.or(q.arvStartDate.isNull()));
        //		}

        if (filter.getEnrollmentType() != null) {
            predicates.add(qco.enrollmentType.eq(filter.getEnrollmentType()));
        }

        if (!filter.isIncludeDeleted()) {
            predicates.add(q.deleted.isFalse());
        }

        if (filter.getPatientStatus() != null) {
            predicates.add(qco.status.eq(filter.getPatientStatus()));
        }

        if (filter.getAppointmentResult() != null) {
            if (filter.getAppointmentResult() == AppointmentResult.NOT_ARRIVED) {
                LocalDateTime toDate = CommonUtils.hanoiTodayEnd().minusDays(1l);
                predicates.add(
                        qa.appointmentDate
                                .loe(toDate)
                                .and(qa.arrived.isFalse().or(qa.arrived.isNull())));
            } else {
                LocalDateTime fromDate = filter.getAppointmentMonth();
                LocalDateTime toDate =
                        CommonUtils.dateEnd(
                                fromDate.withDayOfMonth(
                                        fromDate.getMonth()
                                                .length(fromDate.toLocalDate().isLeapYear())));

                switch (filter.getAppointmentResult()) {
                    case HAS_APPOINTMENT:
                        predicates.add(
                                qa.appointmentDate
                                        .goe(fromDate)
                                        .and(qa.appointmentDate.loe(toDate)));
                        break;

                    case HAS_VL_TEST:
                        predicates.add(
                                qa.arrivalDate
                                        .goe(fromDate)
                                        .and(qa.arrivalDate.loe(toDate))
                                        .and(qa.vlTested.isTrue()));
                        break;

                    case HAS_CD4_TEST:
                        predicates.add(
                                qa.arrivalDate
                                        .goe(fromDate)
                                        .and(qa.arrivalDate.loe(toDate))
                                        .and(qa.cd4Tested.isTrue()));
                        break;

                    case HAS_ARV_DR_TEST:
                        predicates.add(
                                qa.arrivalDate
                                        .goe(fromDate)
                                        .and(qa.arrivalDate.loe(toDate))
                                        .and(qa.arvDrTested.isTrue()));
                        break;

                    default:
                        break;
                }
            }

            query = query.innerJoin(q.appointments, qa).distinct();
        }

        // Query
        query.where(predicates.toArray(new Predicate[0]));

        return query;
    }

    /**
     * Create patient worksheet for search result
     *
     * @param filter
     * @return
     */
    private Workbook createExcelWorkbook4SearchResults(CaseFilterDto filter) {
        UserDto user = filter.getUser();
        boolean confidentialRequired = false;

        for (RoleDto r : user.getRoles()) {
            if (r.getName().equalsIgnoreCase(Constants.ROLE_ADMIN)
                    || r.getName().equalsIgnoreCase(Constants.ROLE_DONOR)
                    || r.getName().equalsIgnoreCase(Constants.ROLE_NATIONAL_MANAGER)) {
                confidentialRequired = true;
            }
        }

        Workbook blankBook = new XSSFWorkbook();
        blankBook.createSheet();

        List<Long> orgIds = authUtils.getGrantedOrgIds(Permission.READ_ACCESS);

        if (CommonUtils.isEmpty(orgIds)) {
            // this user is not granted access to any organization
            return blankBook;
        }

        Workbook wbook = null;
        try (InputStream template =
                context.getResource("classpath:templates/search-results-template.xlsx")
                        .getInputStream()) {
            wbook = new SXSSFWorkbook(new XSSFWorkbook(template), 100);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (wbook == null) {
            return blankBook;
        }

        Sheet patientSheet = wbook.getSheetAt(0);
        List<CaseOrg> caseOrgs = new ArrayList<>();

        filter.setPageIndex(0);
        filter.setPageSize(Integer.MAX_VALUE);

        QueryResult queryResult = queryCaseOrgs(filter, orgIds);
        caseOrgs = queryResult.getContent();

        // By default the patient work sheet is generated
        createPatientWorksheet(caseOrgs, patientSheet, wbook, orgIds, confidentialRequired);

        return wbook;
    }

    /**
     * Start exporting data...
     *
     * @return
     */
    private Workbook createExcelWorkbook(CaseFilterDto filter) {
        UserDto user = filter.getUser();
        boolean confidentialRequired = false;

        for (RoleDto r : user.getRoles()) {
            if (r.getName().equalsIgnoreCase(Constants.ROLE_ADMIN)
                    || r.getName().equalsIgnoreCase(Constants.ROLE_DONOR)
                    || r.getName().equalsIgnoreCase(Constants.ROLE_NATIONAL_MANAGER)) {
                confidentialRequired = true;
            }
        }

        // Get all applicable organizations
        List<Long> grantedOrgIds = authUtils.getGrantedOrgIds(Permission.READ_ACCESS);

        Workbook blankBook = new XSSFWorkbook();

        blankBook.createSheet();

        if (CommonUtils.isEmpty(grantedOrgIds)) {
            return blankBook;
        }

        Workbook wbook = null;
        try (InputStream template =
                context.getResource("classpath:templates/raw-data-template.xlsx")
                        .getInputStream()) {
            wbook = new SXSSFWorkbook(new XSSFWorkbook(template), 100);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (wbook == null) {
            return blankBook;
        }

        Sheet patientSheet = wbook.getSheet("Bệnh nhân");

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

        // Query for patients
        QCase q = QCase.case$;
        QCaseOrg qco = QCaseOrg.caseOrg;
        JPAQuery<CaseOrg> query = new JPAQuery<CaseOrg>(em).from(qco).innerJoin(qco.theCase, q);
        JPAQuery<Case> query2 =
                new JPAQuery<Case>(em).distinct().from(q).innerJoin(q.caseOrgs, qco);

        // predicates
        List<Predicate> predicates = new LinkedList<>();
        predicates.add(qco.organization.id.longValue().in(grantedOrgIds));
        predicates.add(qco.latestRelationship.isTrue());
        predicates.add(qco.refTrackingOnly.isFalse());
        predicates.add(
                qco.status.notIn(
                        PatientStatus.CANCELLED_ENROLLMENT, PatientStatus.PENDING_ENROLLMENT));
        predicates.add(q.deleted.isFalse());

        // Query
        query.where(predicates.toArray(new Predicate[0]));
        query2.where(predicates.toArray(new Predicate[0]));

        List<CaseOrg> caseOrgs = query.orderBy(q.person.fullname.asc()).fetch();
        List<Case> distinctCases = query2.orderBy(q.person.fullname.asc()).fetch();

        // By default the patient work sheet is generated
        createPatientWorksheet(caseOrgs, patientSheet, wbook, grantedOrgIds, confidentialRequired);

        if (CommonUtils.isTrue(filter.isExportTreatmentHistory())) {
            Sheet txHistorySheet = wbook.getSheet("Lịch sử điều trị");
            createTxHistoryWorksheet(
                    distinctCases,
                    txHistorySheet,
                    wbook,
                    grantedOrgIds,
                    confidentialRequired,
                    cellStyle,
                    dateCellStyle);
        } else {
            wbook.removeSheetAt(wbook.getSheetIndex("Lịch sử điều trị"));
        }

        if (CommonUtils.isTrue(filter.isExportTreatmentHistory())) {
            Sheet arvHistorySheet = wbook.getSheet("Phác đồ ARV đã dùng");
            createArvHistoryWorksheet(
                    distinctCases,
                    arvHistorySheet,
                    wbook,
                    grantedOrgIds,
                    confidentialRequired,
                    cellStyle,
                    dateCellStyle);
        } else {
            wbook.removeSheetAt(wbook.getSheetIndex("Phác đồ ARV đã dùng"));
        }

        if (CommonUtils.isTrue(filter.isExportViralLoad())) {
            Sheet viralLoadSheet = wbook.getSheet("Xét nghiệm TLVR HIV");
            createViralLoadWorksheet(
                    distinctCases,
                    viralLoadSheet,
                    wbook,
                    grantedOrgIds,
                    confidentialRequired,
                    cellStyle,
                    dateCellStyle);
        } else {
            wbook.removeSheetAt(wbook.getSheetIndex("Xét nghiệm TLVR HIV"));
        }

        if (CommonUtils.isTrue(filter.isExportCD4())) {
            Sheet cd4Sheet = wbook.getSheet("Xét nghiệm CD4");
            createCd4Worksheet(
                    distinctCases,
                    cd4Sheet,
                    wbook,
                    grantedOrgIds,
                    confidentialRequired,
                    cellStyle,
                    dateCellStyle);
        } else {
            wbook.removeSheetAt(wbook.getSheetIndex("Xét nghiệm CD4"));
        }

        if (CommonUtils.isTrue(filter.isExportShi())) {
            Sheet shiSheet = wbook.getSheet("Bảo hiểm y tế");
            createShiWorksheet(
                    distinctCases,
                    shiSheet,
                    wbook,
                    grantedOrgIds,
                    confidentialRequired,
                    cellStyle,
                    dateCellStyle);
        } else {
            wbook.removeSheetAt(wbook.getSheetIndex("Bảo hiểm y tế"));
        }

        if (CommonUtils.isTrue(filter.isExportRisk())) {
            Sheet riskSheet = wbook.getSheet("Phân nhóm nguy cơ");
            createRiskWorksheet(
                    distinctCases,
                    riskSheet,
                    wbook,
                    grantedOrgIds,
                    confidentialRequired,
                    cellStyle,
                    dateCellStyle);
        } else {
            wbook.removeSheetAt(wbook.getSheetIndex("Phân nhóm nguy cơ"));
        }

        if (CommonUtils.isTrue(filter.isExportTbProphylaxis())) {
            Sheet tbProSheet = wbook.getSheet("Dự phòng lao");
            createTbProWorksheet(
                    distinctCases,
                    tbProSheet,
                    wbook,
                    grantedOrgIds,
                    confidentialRequired,
                    cellStyle,
                    dateCellStyle);
        } else {
            wbook.removeSheetAt(wbook.getSheetIndex("Dự phòng lao"));
        }

        if (CommonUtils.isTrue(filter.isExportTbTreatment())) {
            Sheet tbTreatmentSheet = wbook.getSheet("Điều trị lao");
            createTbTreatmentWorksheet(
                    distinctCases,
                    tbTreatmentSheet,
                    wbook,
                    grantedOrgIds,
                    confidentialRequired,
                    cellStyle,
                    dateCellStyle);
        } else {
            wbook.removeSheetAt(wbook.getSheetIndex("Điều trị lao"));
        }

        if (CommonUtils.isTrue(filter.isExportHivDr())) {
            Sheet hivDrSheet = wbook.getSheet("Xét nghiệm kháng thuốc");
            createHivDrWorksheet(
                    distinctCases,
                    hivDrSheet,
                    wbook,
                    grantedOrgIds,
                    confidentialRequired,
                    cellStyle,
                    dateCellStyle);
        } else {
            wbook.removeSheetAt(wbook.getSheetIndex("Xét nghiệm kháng thuốc"));
        }

        if (CommonUtils.isTrue(filter.isExportHepatitis())) {
            Sheet hepatitisSheet = wbook.getSheet("Viêm gan B-C");
            createHepatitisWorksheet(
                    distinctCases,
                    hepatitisSheet,
                    wbook,
                    grantedOrgIds,
                    confidentialRequired,
                    cellStyle,
                    dateCellStyle);
        } else {
            wbook.removeSheetAt(wbook.getSheetIndex("Viêm gan B-C"));
        }

        if (CommonUtils.isTrue(filter.isExportWhoStage())) {
            Sheet whoStageSheet = wbook.getSheet("Giai đoạn lâm sàng");
            createWhoStageWorksheet(
                    distinctCases,
                    whoStageSheet,
                    wbook,
                    grantedOrgIds,
                    confidentialRequired,
                    cellStyle,
                    dateCellStyle);
        } else {
            wbook.removeSheetAt(wbook.getSheetIndex("Giai đoạn lâm sàng"));
        }

        if (CommonUtils.isTrue(filter.isExportMmt())) {
            Sheet mmtSheet = wbook.getSheet("Điều trị Methadone");
            createMmtWorksheet(
                    distinctCases,
                    mmtSheet,
                    wbook,
                    grantedOrgIds,
                    confidentialRequired,
                    cellStyle,
                    dateCellStyle);
        } else {
            wbook.removeSheetAt(wbook.getSheetIndex("Điều trị Methadone"));
        }

        if (CommonUtils.isTrue(filter.isExportPregnancy())) {
            Sheet pregnancySheet = wbook.getSheet("Mang thai - Sinh đẻ");
            createPregnancyWorksheet(
                    distinctCases,
                    pregnancySheet,
                    wbook,
                    grantedOrgIds,
                    confidentialRequired,
                    cellStyle,
                    dateCellStyle);
        } else {
            wbook.removeSheetAt(wbook.getSheetIndex("Mang thai - Sinh đẻ"));
        }

        return wbook;
    }

    /** Patient work sheet */
    private void createPatientWorksheet(
            List<CaseOrg> caseOrgs,
            Sheet sheet,
            Workbook wbook,
            List<Long> grantedOrgIds,
            boolean confidential) {

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

        int colIndex = 0;
        int rowIndex = 2;

        Row row = null;
        Cell cell = null;

        // Start filling out data...
        for (CaseOrg co : caseOrgs) {
            colIndex = 0;
            row = sheet.createRow(rowIndex++);
            row.setHeightInPoints(22);

            Case theCase = co.getTheCase();
            Person person = theCase.getPerson();

            // Mã UUID
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(theCase.getId().toString());

            // Tỉnh - thành phố của cơ sở
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);

            Organization facility = co.getOrganization();
            if (facility != null
                    && facility.getAddress() != null
                    && facility.getAddress().getProvince() != null) {
                cell.setCellValue(facility.getAddress().getProvince().getName());
            }

            // Cơ sở điều trị
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (facility != null) {
                cell.setCellValue(facility.getName());
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
            if (confidential) {
                cell.setCellValue("-");
            } else {
                cell.setCellValue(confidential ? "-" : person.getFullname());
            }

            // Giới tính
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(person.getGender().toString());

            // Ngày sinh
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellValue(CommonUtils.fromLocalDateTime(person.getDob()));
            cell.setCellStyle(dateCellStyle);

            // Số CMTND
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (theCase.getPerson().getNidNumber() != null) {
                cell.setCellValue(confidential ? "-" : person.getNidNumber());
            } else {
                cell.setCellValue("-");
            }

            // Số hộ chiếu
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (theCase.getPerson().getPassportNumber() != null) {
                cell.setCellValue(confidential ? "-" : person.getPassportNumber());
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

            // Ngày bắt đầu ARV tại cơ sở điều trị
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (co.getArvStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(co.getStartDate()));
            } else {
                cell.setCellValue("-");
            }

            // Loại đăng ký tại cơ sở hiện tại
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
            cell.setCellValue(
                    !CommonUtils.isEmpty(theCase.getCurrentArvRegimenName())
                            ? theCase.getCurrentArvRegimenName()
                            : "-");

            // Bậc của phác đồ hiện tại
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(cellStyle);
            if (theCase.getArvStartDate() != null
                    && CommonUtils.isPositive(theCase.getCurrentArvRegimenLine(), true)) {
                cell.setCellValue(theCase.getCurrentArvRegimenLine());
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

            // Nhóm điều trị ARV
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (co.getArvGroup() != null) {
                cell.setCellValue(co.getArvGroup());
            } else {
                cell.setCellValue("-");
            }

            // Đủ điều kiện làm XN TLVR?
            boolean vlEligible = false;
            LocalDateTime secondlineDate = theCase.getSecondLineStartDate();
            if (secondlineDate != null) {
                long monthDiff =
                        CommonUtils.dateDiff(
                                ChronoUnit.MONTHS, secondlineDate, CommonUtils.hanoiTodayStart());

                if (monthDiff >= 6) {
                    vlEligible = true;
                }
            } else {
                LocalDateTime arvStartDate = theCase.getArvStartDate();
                if (arvStartDate != null) {
                    long monthDiff =
                            CommonUtils.dateDiff(
                                    ChronoUnit.MONTHS, arvStartDate, CommonUtils.hanoiTodayStart());

                    if (monthDiff >= 6) {
                        vlEligible = true;
                    }
                }
            }

            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(vlEligible ? "Đúng" : "Sai");

            List<LabTest> vlTests =
                    theCase.getLabTests().parallelStream()
                            .filter(c -> c.getTestType() == ClinicalTestingType.VIRAL_LOAD)
                            .collect(Collectors.toList());
            if (vlTests != null && vlTests.size() > 0) {
                LabTest latestTest = vlTests.get(0);

                // Ngày XN TLVR gần đây nhất
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                cell.setCellValue(CommonUtils.fromLocalDateTime(latestTest.getSampleDate()));

                // Kết quả TLVR gần đây nhất
                if (CommonUtils.isPositive(latestTest.getResultNumber(), false)) {

                    if (latestTest.getResultNumber() == 0) {
                        cell = row.createCell(colIndex++, CellType.STRING);
                        cell.setCellValue("Không phát hiện");
                    } else {
                        cell = row.createCell(colIndex++, CellType.NUMERIC);
                        cell.setCellValue(latestTest.getResultNumber());
                    }
                } else {
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellValue("-");
                }
                cell.setCellStyle(cellStyle);
            } else {
                for (int i = 0; i < 2; i++) {
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue("-");
                }
            }

            // Sàng lọc Viêm gan B gần nhất
            List<Hepatitis> hepBs =
                    theCase.getHepatitises().parallelStream()
                            .filter(c -> c.getTestType() == ClinicalTestingType.HEP_B)
                            .collect(Collectors.toList());
            if (hepBs != null && hepBs.size() > 0) {
                Hepatitis latestTest = hepBs.get(0);

                // Ngày XN gần đây nhất
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                if (latestTest.getTestDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(latestTest.getTestDate()));
                } else {
                    cell.setCellValue("-");
                }

                // Kết quả gần đây nhất
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellValue(latestTest.isTestPositive() ? "Dương tính" : "Âm tính");
                cell.setCellStyle(cellStyle);
            } else {
                for (int i = 0; i < 2; i++) {
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue("-");
                }
            }

            // Sàng lọc Viêm gan C gần nhất
            List<Hepatitis> hepCs =
                    theCase.getHepatitises().parallelStream()
                            .filter(c -> c.getTestType() == ClinicalTestingType.HEP_C)
                            .collect(Collectors.toList());
            if (hepCs != null && hepCs.size() > 0) {
                Hepatitis latestTest = hepCs.get(0);

                // Ngày XN gần đây nhất
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                cell.setCellValue(CommonUtils.fromLocalDateTime(latestTest.getTestDate()));

                // Kết quả gần đây nhất
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellValue(latestTest.isTestPositive() ? "Dương tính" : "Âm tính");
                cell.setCellStyle(cellStyle);
            } else {
                for (int i = 0; i < 2; i++) {
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue("-");
                }
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
            cell.setCellValue((co.getStatus() != null) ? co.getStatus().toString() : "-");

            // Ngày thay đổi tình trạng
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (co.getStatus() == PatientStatus.ACTIVE) {
                cell.setCellValue("-");
            } else if (co.getEndDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(co.getEndDate()));
            }
        }

        // Hide ID columns
        sheet.setColumnHidden(0, true);
        sheet.setColumnHidden(3, true);

        // Auto-filter
        if (rowIndex >= 2) {
            sheet.setAutoFilter(CellRangeAddress.valueOf("A2:AO" + rowIndex));
        }
    }

    private void createTxHistoryWorksheet(
            List<Case> distinctCases,
            Sheet sheet,
            Workbook wbook,
            List<Long> grantedOrgIds,
            boolean confidential,
            CellStyle cellStyle,
            CellStyle dateCellStyle) {

        int rowIndex = 1;
        for (Case c : distinctCases) {

            Iterator<CaseOrg> records = c.getCaseOrgs().iterator();

            while (records.hasNext()) {
                CaseOrg r = records.next();

                int colIndex = 0;

                Row row = sheet.createRow(rowIndex++);
                row.setHeightInPoints(22);

                // Patient UUID
                Cell cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(c.getId().toString());

                // Họ tên bệnh nhân
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (CommonUtils.isEmpty(c.getPerson().getFullname())) {
                    cell.setCellValue("-");
                } else {
                    cell.setCellValue(confidential ? "-" : c.getPerson().getFullname());
                }

                // Record UUID
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(r.getUid().toString());

                // Registration type name
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(r.getEnrollmentType().toString());

                // Patient status
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(r.getStatus().toString());

                // Tỉnh - thành phố
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (r.getOrganization().getAddress() != null
                        && r.getOrganization().getAddress().getProvince() != null) {
                    cell.setCellValue(r.getOrganization().getAddress().getProvince().getName());
                } else {
                    cell.setCellValue("-");
                }

                // Name of OPC
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(r.getOrganization().getName());

                // Mã bệnh án
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(
                        CommonUtils.isEmpty(r.getPatientChartId()) ? "-" : r.getPatientChartId());

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
            }
        }

        // Hide ID columns
        sheet.setColumnHidden(0, true);
        sheet.setColumnHidden(2, true);

        // Auto-filter
        if (rowIndex >= 1) {
            sheet.setAutoFilter(CellRangeAddress.valueOf("A1:J" + rowIndex));
        }
    }

    private void createArvHistoryWorksheet(
            List<Case> distinctCases,
            Sheet sheet,
            Workbook wbook,
            List<Long> grantedOrgIds,
            boolean confidential,
            CellStyle cellStyle,
            CellStyle dateCellStyle) {

        int rowIndex = 1;
        for (Case c : distinctCases) {

            List<Treatment> treatments =
                    c.getTreatments().parallelStream().collect(Collectors.toList());

            for (Treatment tx : treatments) {
                int colIndex = 0;

                Row row = sheet.createRow(rowIndex++);
                row.setHeightInPoints(22);

                // Patient UUID
                Cell cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(c.getId().toString());

                Organization org = tx.getOrganization();

                // Tỉnh - thành phố
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (org.getAddress() != null && org.getAddress().getProvince() != null) {
                    cell.setCellValue(org.getAddress().getProvince().getName());
                } else {
                    cell.setCellValue("-");
                }

                // Tên cơ sở
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(org != null ? org.getName() : "-");

                // Patient chart ID
                CaseOrg co = null;
                List<CaseOrg> cos = coRepos.findByCaseAndOrg(c.getId(), org.getId());

                if (cos.size() > 0) {
                    co = cos.get(0);
                }

                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (co != null && !CommonUtils.isEmpty(co.getPatientChartId())) {
                    cell.setCellValue(co.getPatientChartId());
                } else {
                    cell.setCellValue("-");
                }

                // Patient full name
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (!CommonUtils.isEmpty(c.getPerson().getFullname())) {
                    cell.setCellValue(confidential ? "-" : c.getPerson().getFullname());
                } else {
                    cell.setCellValue("-");
                }

                // Record UUID
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(tx.getUid().toString());

                // Name of regimen
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(tx.getRegimenName());

                // Line of regimen
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(tx.getRegimenLine());

                // Start date
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                if (tx.getStartDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(tx.getStartDate()));
                } else {
                    cell.setCellValue("-");
                }

                // End date
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                if (tx.getEndDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(tx.getEndDate()));
                } else {
                    cell.setCellValue("-");
                }

                // Lý do kết thúc
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (!CommonUtils.isEmpty(tx.getEndingReason())) {
                    cell.setCellValue(tx.getEndingReason());
                } else {
                    cell.setCellValue("-");
                }
            }
        }

        // Hide ID columns
        sheet.setColumnHidden(0, true);
        sheet.setColumnHidden(5, true);

        // Auto-filter
        if (rowIndex >= 1) {
            sheet.setAutoFilter(CellRangeAddress.valueOf("A1:K" + rowIndex));
        }
    }

    /** Viral load work sheet */
    private void createViralLoadWorksheet(
            List<Case> distinctCases,
            Sheet sheet,
            Workbook wbook,
            List<Long> grantedOrgIds,
            boolean confidential,
            CellStyle cellStyle,
            CellStyle dateCellStyle) {

        int rowIndex = 1;
        for (Case c : distinctCases) {

            List<LabTest> tests =
                    c.getLabTests().parallelStream()
                            .filter(e -> e.getTestType() == ClinicalTestingType.VIRAL_LOAD)
                            .collect(Collectors.toList());

            for (LabTest test : tests) {
                int colIndex = 0;

                Row row = sheet.createRow(rowIndex++);
                row.setHeightInPoints(22);

                // Mã UUID của bệnh nhân
                Cell cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(c.getId().toString());

                Organization org = test.getOrganization();

                // Tỉnh - thành phố
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (org.getAddress() != null && org.getAddress().getProvince() != null) {
                    cell.setCellValue(org.getAddress().getProvince().getName());
                } else {
                    cell.setCellValue("-");
                }

                // Tên cơ sở
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(org != null ? org.getName() : "-");

                // Mã bệnh án
                CaseOrg co = null;
                List<CaseOrg> cos = coRepos.findByCaseAndOrg(c.getId(), org.getId());

                if (cos.size() > 0) {
                    co = cos.get(0);
                }

                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (co != null && !CommonUtils.isEmpty(co.getPatientChartId())) {
                    cell.setCellValue(co.getPatientChartId());
                } else {
                    cell.setCellValue("-");
                }

                // Họ tên bệnh nhân
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (CommonUtils.isEmpty(c.getPerson().getFullname())) {
                    cell.setCellValue("-");
                } else {
                    cell.setCellValue(confidential ? "-" : c.getPerson().getFullname());
                }

                // Mã UUID của bản ghi
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(test.getUid().toString());

                // Ngày lấy mẫu
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellValue(CommonUtils.fromLocalDateTime(test.getSampleDate()));
                cell.setCellStyle(dateCellStyle);

                // Cơ sở lấy mẫu
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (!CommonUtils.isEmpty(test.getSampleSite())) {
                    cell.setCellValue(test.getSampleSite());
                } else {
                    cell.setCellValue("-");
                }

                // Ngày xét nghiệm
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                if (test.getResultDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(test.getResultDate()));
                } else {
                    cell.setCellValue("-");
                }

                // Kết quả
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(cellStyle);
                if (CommonUtils.isPositive(test.getResultNumber(), false)) {
                    cell.setCellValue(test.getResultNumber());
                } else {
                    cell.setCellValue("-");
                }

                // Dải kết quả
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (!CommonUtils.isEmpty(test.getResultText())) {
                    cell.setCellValue(test.getResultText());
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

                // Nguồn kinh phí
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (test.getFundingSource() != null) {
                    cell.setCellValue(test.getFundingSource().toString());
                } else {
                    cell.setCellValue("-");
                }

                // Ghi chú
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(!CommonUtils.isEmpty(test.getNote()) ? test.getNote() : "-");
            }
        }

        // Hide ID columns
        sheet.setColumnHidden(0, true);
        sheet.setColumnHidden(5, true);

        // Auto-filter
        if (rowIndex >= 1) {
            sheet.setAutoFilter(CellRangeAddress.valueOf("A1:N" + rowIndex));
        }
    }

    /** CD4 work sheet */
    private void createCd4Worksheet(
            List<Case> distinctCases,
            Sheet sheet,
            Workbook wbook,
            List<Long> grantedOrgIds,
            boolean confidential,
            CellStyle cellStyle,
            CellStyle dateCellStyle) {
        int rowIndex = 1;
        for (Case c : distinctCases) {
            List<LabTest> tests =
                    c.getLabTests().parallelStream()
                            .filter(e -> e.getTestType() == ClinicalTestingType.CD4)
                            .collect(Collectors.toList());
            for (LabTest test : tests) {

                int colIndex = 0;

                Row row = sheet.createRow(rowIndex++);
                row.setHeightInPoints(22);

                // Mã UUID của bệnh nhân
                Cell cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(c.getId().toString());

                Organization org = test.getOrganization();

                // Tỉnh - thành phố
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (org.getAddress() != null && org.getAddress().getProvince() != null) {
                    cell.setCellValue(org.getAddress().getProvince().getName());
                } else {
                    cell.setCellValue("-");
                }

                // Tên cơ sở
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(org != null ? org.getName() : "-");

                // Mã bệnh án
                CaseOrg co = null;
                List<CaseOrg> cos = coRepos.findByCaseAndOrg(c.getId(), org.getId());

                if (cos.size() > 0) {
                    co = cos.get(0);
                }

                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (co != null && !CommonUtils.isEmpty(co.getPatientChartId())) {
                    cell.setCellValue(co.getPatientChartId());
                } else {
                    cell.setCellValue("-");
                }

                // Họ tên bệnh nhân
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (CommonUtils.isEmpty(c.getPerson().getFullname())) {
                    cell.setCellValue("-");
                } else {
                    cell.setCellValue(confidential ? "-" : c.getPerson().getFullname());
                }

                // Mã UUID của bản ghi
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(test.getUid().toString());

                // Ngày lấy mẫu
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellValue(CommonUtils.fromLocalDateTime(test.getSampleDate()));
                cell.setCellStyle(dateCellStyle);

                // Cơ sở lấy mẫu
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (!CommonUtils.isEmpty(test.getSampleSite())) {
                    cell.setCellValue(test.getSampleSite());
                } else {
                    cell.setCellValue("-");
                }

                // Ngày xét nghiệm
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                if (test.getResultDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(test.getResultDate()));
                } else {
                    cell.setCellValue("-");
                }

                // Kết quả
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(test.getResultNumber());

                // Lý do xét nghiệm
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(test.getReasonForTesting().toString());

                // Ghi chú
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(!CommonUtils.isEmpty(test.getNote()) ? test.getNote() : "-");
            }
        }

        // Hide ID columns
        sheet.setColumnHidden(0, true);
        sheet.setColumnHidden(5, true);

        // Auto-filter
        if (rowIndex >= 1) {
            sheet.setAutoFilter(CellRangeAddress.valueOf("A1:L" + rowIndex));
        }
    }

    /** SHI work sheet */
    private void createShiWorksheet(
            List<Case> distinctCases,
            Sheet sheet,
            Workbook wbook,
            List<Long> grantedOrgIds,
            boolean confidential,
            CellStyle cellStyle,
            CellStyle dateCellStyle) {

        CellStyle percentStyle = wbook.createCellStyle();
        percentStyle.cloneStyleFrom(cellStyle);
        percentStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("0%"));

        int rowIndex = 1;
        for (Case c : distinctCases) {

            List<ShiInterview> interviews =
                    c.getShiInterviews().parallelStream().collect(Collectors.toList());

            for (ShiInterview interview : interviews) {
                ShiInterviewDto dto = new ShiInterviewDto(interview);

                int colIndex = 0;

                Row row = sheet.createRow(rowIndex++);
                row.setHeightInPoints(22);

                // Mã UUID của bệnh nhân
                Cell cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(c.getId().toString());

                Organization org = interview.getOrganization();

                // Tỉnh - thành phố
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (org.getAddress() != null && org.getAddress().getProvince() != null) {
                    cell.setCellValue(org.getAddress().getProvince().getName());
                } else {
                    cell.setCellValue("-");
                }

                // Tên cơ sở
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(org != null ? org.getName() : "-");

                // Mã bệnh án
                CaseOrg co = null;
                List<CaseOrg> cos = coRepos.findByCaseAndOrg(c.getId(), org.getId());

                if (cos.size() > 0) {
                    co = cos.get(0);
                }

                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (co != null && !CommonUtils.isEmpty(co.getPatientChartId())) {
                    cell.setCellValue(co.getPatientChartId());
                } else {
                    cell.setCellValue("-");
                }

                // Họ tên bệnh nhân
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (CommonUtils.isEmpty(c.getPerson().getFullname())) {
                    cell.setCellValue("-");
                } else {
                    cell.setCellValue(confidential ? "-" : c.getPerson().getFullname());
                }

                // Mã UUID của bản ghi
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(interview.getUid().toString());

                // Tình trạng đăng ký chỗ ở
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellValue(dto.getResidentStatusName());
                cell.setCellStyle(cellStyle);

                // Công việc hiện tại
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellValue(dto.getOccupationName());
                cell.setCellStyle(cellStyle);

                // Thu nhập trung bình tháng
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(dto.getMonthlyIncomeName());

                // Tình trạng kinh tế
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(dto.getWealthStatusName());

                // Tình trạng sở hữu thẻ BHYT
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(
                        CommonUtils.isTrue(dto.getHasShiCard())
                                ? "Có thẻ BHYT"
                                : "Không có thẻ BHYT");

                // Mã thẻ BHYT
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(
                        CommonUtils.isEmpty(dto.getShiCardNumber()) ? "-" : dto.getShiCardNumber());

                // Hạn dùng thẻ BHYT
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                if (dto.getShiExpiryDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(dto.getShiExpiryDate()));
                } else {
                    cell.setCellValue("-");
                }

                // Tỉ lệ % của thẻ
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(percentStyle);
                if (!CommonUtils.isEmpty(dto.getShiCardNumber())
                        && dto.getShiCardNumber().length() > 3) {
                    String firstTwo = dto.getShiCardNumber().substring(0, 2).toUpperCase();
                    int third = Integer.parseInt(dto.getShiCardNumber().substring(2, 3));

                    switch (third) {
                        case 1:
                            if ("CC,TE".indexOf(firstTwo) >= 0) {
                                cell.setCellValue(1);
                            } else {
                                cell.setCellValue("-");
                            }
                            break;

                        case 2:
                            if ("CK,CB,KC,HN,DT,DK,XD,BT,TS".indexOf(firstTwo) >= 0) {
                                cell.setCellValue(1);
                            } else {
                                cell.setCellValue("-");
                            }
                            break;

                        case 3:
                            if ("HT,TC,CN".indexOf(firstTwo) >= 0) {
                                cell.setCellValue(0.95);
                            } else {
                                cell.setCellValue("-");
                            }
                            break;

                        case 4:
                            if ("DN,HX,CH,NN,TK,HC,XK,TB,NO,CT,XB,TN,CS,XN,MS,HD,TQ,TA,TY,HG,LS,PV,HS,SV,GB,GD"
                                            .indexOf(firstTwo)
                                    >= 0) {
                                cell.setCellValue(0.8);
                            } else {
                                cell.setCellValue("-");
                            }
                            break;

                        case 5:
                            if ("QN,CA,CY".indexOf(firstTwo) >= 0) {
                                cell.setCellValue(1);
                            } else {
                                cell.setCellValue("-");
                            }
                            break;

                        default:
                            cell.setCellValue("-");
                            break;
                    }
                } else {
                    cell.setCellValue("-");
                }

                // Tuyến thẻ BHYT
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(dto.getShiRouteName());

                // Nơi đăng ký khám chữa bệnh ban đầu
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (!CommonUtils.isEmpty(dto.getPrimaryCareFacilityName())) {
                    cell.setCellValue(dto.getPrimaryCareFacilityName());
                } else {
                    cell.setCellValue("-");
                }

                // Muốn sử dụng BHYT cho ART?
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(CommonUtils.isTrue(dto.getWantShiForArv()) ? "Có" : "Không");

                // Đã sử dụng BHYT cho ART?
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(
                        CommonUtils.isTrue(dto.getUsedShiForArv()) ? "Đã sử dụng" : "Chưa sử dụng");

                // Các dịch vụ đã được chi trả qua BHYT trong lần khám gần nhất
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);

                Set<DictionaryDto> usedServices = dto.getUsedShiServices();
                if (usedServices != null) {
                    String val = "";
                    for (DictionaryDto us : usedServices) {
                        val += us.getValue() + "\n";
                    }

                    cell.setCellValue(val);
                } else {
                    cell.setCellValue("-");
                }

                // Lý do không có thẻ BHYT
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);

                Set<DictionaryDto> reasons = dto.getNoShiReasons();
                if (reasons != null) {
                    String val = "";
                    for (DictionaryDto r : reasons) {
                        val += r.getValue() + "\n";
                    }

                    cell.setCellValue(val);
                } else {
                    cell.setCellValue("-");
                }

                // Nhu cầu sử dụng BHYT
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(dto.getShiForArvPrefName());

                // Nơi điều trị ARV mong muốn
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (!CommonUtils.isEmpty(dto.getContinuingFacilityName())) {
                    cell.setCellValue(dto.getContinuingFacilityName());
                } else {
                    cell.setCellValue("-");
                }

                // Điều trị ARV không qua BHYT
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (!CommonUtils.isEmpty(dto.getArvTreatmentPrefName())) {
                    cell.setCellValue(dto.getArvTreatmentPrefName());
                } else {
                    cell.setCellValue("-");
                }

                // Sẽ mua BHYT vào kỳ tới
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(CommonUtils.isTrue(dto.getBuyShiNextQuarter()) ? "Có" : "Không");

                // Cần hỗ trợ mua BHYT
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(CommonUtils.isTrue(dto.getNeedSupportForShi()) ? "Có" : "Không");

                // Nhu cầu cần hỗ trợ
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (!CommonUtils.isEmpty(dto.getNeedSupportDetails())) {
                    cell.setCellValue(dto.getNeedSupportDetails());
                } else {
                    cell.setCellValue("-");
                }
            }
        }

        // Hide ID columns
        sheet.setColumnHidden(0, true);
        sheet.setColumnHidden(5, true);

        // Auto-filter
        if (rowIndex >= 1) {
            sheet.setAutoFilter(CellRangeAddress.valueOf("A1:Z" + rowIndex));
        }
    }

    /** Risk identification work sheet */
    private void createRiskWorksheet(
            List<Case> distinctCases,
            Sheet sheet,
            Workbook wbook,
            List<Long> grantedOrgIds,
            boolean confidential,
            CellStyle cellStyle,
            CellStyle dateCellStyle) {

        int rowIndex = 2;
        for (Case c : distinctCases) {

            List<RiskInterview> interviews =
                    c.getRiskInterviews().parallelStream().collect(Collectors.toList());

            for (RiskInterview interview : interviews) {
                int colIndex = 0;

                Row row = sheet.createRow(rowIndex++);
                row.setHeightInPoints(22);

                // Mã UUID của bệnh nhân
                Cell cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(c.getId().toString());

                Organization org = interview.getOrganization();

                // Tỉnh - thành phố
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (org.getAddress() != null && org.getAddress().getProvince() != null) {
                    cell.setCellValue(org.getAddress().getProvince().getName());
                } else {
                    cell.setCellValue("-");
                }

                // Tên cơ sở
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(org != null ? org.getName() : "-");

                // Mã bệnh án
                CaseOrg co = null;
                List<CaseOrg> cos = coRepos.findByCaseAndOrg(c.getId(), org.getId());

                if (cos.size() > 0) {
                    co = cos.get(0);
                }

                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (co != null && !CommonUtils.isEmpty(co.getPatientChartId())) {
                    cell.setCellValue(co.getPatientChartId());
                } else {
                    cell.setCellValue("-");
                }

                // Họ tên bệnh nhân
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (CommonUtils.isEmpty(c.getPerson().getFullname())) {
                    cell.setCellValue("-");
                } else {
                    cell.setCellValue(confidential ? "-" : c.getPerson().getFullname());
                }

                // Tên nhóm nguy cơ (đưa chung vào 1 cột để cơ sở sử dụng cho tiện)
                int tempIndex = colIndex;
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue("");

                // Mã UUID của bản ghi
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(interview.getUid().toString());

                // Ngày phỏng vấn
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                if (interview.getInterviewDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(interview.getInterviewDate()));
                } else {
                    cell.setCellValue("-");
                }
                cell.setCellStyle(dateCellStyle);

                for (int i = 8; i <= 16; i++) {
                    cell = row.createCell(i, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                }

                StringJoiner combinedNames = new StringJoiner(",");
                Set<Dictionary> risks = interview.getRisks();
                if (risks != null) {
                    for (Dictionary r : risks) {
                        combinedNames.add(r.getValue());

                        switch (r.getCode()) {
                            case "risk_1":
                                cell = row.getCell(8);
                                cell.setCellValue("x");
                                break;
                            case "risk_2":
                                cell = row.getCell(9);
                                cell.setCellValue("x");
                                break;
                            case "risk_3":
                                cell = row.getCell(10);
                                cell.setCellValue("x");
                                break;
                            case "risk_4":
                                cell = row.getCell(11);
                                cell.setCellValue("x");
                                break;
                            case "risk_5":
                                cell = row.getCell(12);
                                cell.setCellValue("x");
                                break;
                            case "risk_6":
                                cell = row.getCell(13);
                                cell.setCellValue("x");
                                break;
                            case "risk_7":
                                cell = row.getCell(14);
                                cell.setCellValue("x");
                                break;
                            case "risk_8":
                                cell = row.getCell(15);
                                cell.setCellValue("x");
                                break;
                        }
                    }
                }

                if (!CommonUtils.isEmpty(interview.getOtherRiskGroupText())) {
                    cell = row.getCell(16);
                    cell.setCellValue(interview.getOtherRiskGroupText());
                }

                // update the risk names
                row.getCell(tempIndex).setCellValue(combinedNames.toString());
            }
        }

        // Hide ID columns
        sheet.setColumnHidden(0, true);
        //        sheet.setColumnHidden(5, true);

        // Auto-filter
        if (rowIndex >= 2) {
            sheet.setAutoFilter(CellRangeAddress.valueOf("A2:Q" + rowIndex));
        }
    }

    /**
     * TB prophylaxis work sheet
     *
     * @param entities
     * @param sheet
     * @param wbook
     */
    private void createTbProWorksheet(
            List<Case> distinctCases,
            Sheet sheet,
            Workbook wbook,
            List<Long> grantedOrgIds,
            boolean confidential,
            CellStyle cellStyle,
            CellStyle dateCellStyle) {

        int rowIndex = 1;
        for (Case c : distinctCases) {
            List<TBProphylaxis> pros = c.getTbpros().parallelStream().collect(Collectors.toList());

            for (TBProphylaxis pro : pros) {
                int colIndex = 0;

                Row row = sheet.createRow(rowIndex++);
                row.setHeightInPoints(22);

                // Mã UUID của bệnh nhân
                Cell cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(c.getId().toString());

                Organization org = pro.getOrganization();

                // Tỉnh - thành phố
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (org.getAddress() != null && org.getAddress().getProvince() != null) {
                    cell.setCellValue(org.getAddress().getProvince().getName());
                } else {
                    cell.setCellValue("-");
                }

                // Tên cơ sở
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(org != null ? org.getName() : "-");

                // Mã bệnh án
                CaseOrg co = null;
                List<CaseOrg> cos = coRepos.findByCaseAndOrg(c.getId(), org.getId());

                if (cos.size() > 0) {
                    co = cos.get(0);
                }

                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (co != null && !CommonUtils.isEmpty(co.getPatientChartId())) {
                    cell.setCellValue(co.getPatientChartId());
                } else {
                    cell.setCellValue("-");
                }

                // Họ tên bệnh nhân
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (CommonUtils.isEmpty(c.getPerson().getFullname())) {
                    cell.setCellValue("-");
                } else {
                    cell.setCellValue(confidential ? "-" : c.getPerson().getFullname());
                }

                // Mã UUID của bản ghi
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(pro.getUid().toString());

                // Ngày bắt đầu
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                if (pro.getStartDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(pro.getStartDate()));
                } else {
                    cell.setCellValue("-");
                }
                cell.setCellStyle(dateCellStyle);

                // Ngày kết thúc
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                if (pro.getEndDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(pro.getEndDate()));
                } else {
                    cell.setCellValue("-");
                }
                cell.setCellStyle(dateCellStyle);

                // Tình trạng
                cell = row.createCell(colIndex++, CellType.STRING);
                switch (pro.getResult()) {
                    case 1:
                        cell.setCellValue("Bỏ trị");
                        break;
                    case 2:
                        cell.setCellValue("Chưa hoàn thành");
                        break;
                    case 3:
                        cell.setCellValue("Đã hoàn thành");
                        break;
                    default:
                        break;
                }
                cell.setCellStyle(cellStyle);

                // Ghi chú
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(!CommonUtils.isEmpty(pro.getNote()) ? pro.getNote() : "-");
            }
        }

        // Hide ID columns
        sheet.setColumnHidden(0, true);
        sheet.setColumnHidden(5, true);

        // Auto-filter
        if (rowIndex >= 1) {
            sheet.setAutoFilter(CellRangeAddress.valueOf("A1:J" + rowIndex));
        }
    }

    /**
     * TB treatment work sheet
     *
     * @param entities
     * @param sheet
     * @param wbook
     */
    private void createTbTreatmentWorksheet(
            List<Case> distinctCases,
            Sheet sheet,
            Workbook wbook,
            List<Long> grantedOrgIds,
            boolean confidential,
            CellStyle cellStyle,
            CellStyle dateCellStyle) {

        int rowIndex = 1;
        for (Case c : distinctCases) {
            Iterator<TBTreatment> treatments = c.getTbtxs().iterator();

            while (treatments.hasNext()) {
                TBTreatment tx = treatments.next();

                int colIndex = 0;

                Row row = sheet.createRow(rowIndex++);
                row.setHeightInPoints(22);

                // Mã UUID của bệnh nhân
                Cell cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(c.getId().toString());

                Organization org = tx.getOrganization();

                // Tỉnh - thành phố
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (org.getAddress() != null && org.getAddress().getProvince() != null) {
                    cell.setCellValue(org.getAddress().getProvince().getName());
                } else {
                    cell.setCellValue("-");
                }

                // Tên cơ sở
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(org != null ? org.getName() : "-");

                // Mã bệnh án
                CaseOrg co = null;
                List<CaseOrg> cos = coRepos.findByCaseAndOrg(c.getId(), org.getId());

                if (cos.size() > 0) {
                    co = cos.get(0);
                }

                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (co != null && !CommonUtils.isEmpty(co.getPatientChartId())) {
                    cell.setCellValue(co.getPatientChartId());
                } else {
                    cell.setCellValue("-");
                }

                // Họ tên bệnh nhân
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (CommonUtils.isEmpty(c.getPerson().getFullname())) {
                    cell.setCellValue("-");
                } else {
                    cell.setCellValue(confidential ? "-" : c.getPerson().getFullname());
                }

                // Mã UUID của bản ghi
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(tx.getUid().toString());

                // Ngày chẩn đoán lao
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                if (tx.getDiagnoseDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(tx.getDiagnoseDate()));
                } else {
                    cell.setCellValue("-");
                }
                cell.setCellStyle(dateCellStyle);

                // Ngày bắt đầu điều trị lao
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                if (tx.getTxStartDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(tx.getTxStartDate()));
                } else {
                    cell.setCellValue("-");
                }
                cell.setCellStyle(dateCellStyle);

                // Ngày kết thúc điều trị lao
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                if (tx.getTxEndDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(tx.getTxEndDate()));
                } else {
                    cell.setCellValue("-");
                }
                cell.setCellStyle(dateCellStyle);

                // Cơ sở điều trị lao
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(
                        !CommonUtils.isEmpty(tx.getFacilityName()) ? tx.getFacilityName() : "-");
            }
        }

        // Hide ID columns
        sheet.setColumnHidden(0, true);
        sheet.setColumnHidden(5, true);

        // Auto-filter
        if (rowIndex >= 1) {
            sheet.setAutoFilter(CellRangeAddress.valueOf("A1:J" + rowIndex));
        }
    }

    /**
     * HIV DR work sheet
     *
     * @param entities
     * @param sheet
     * @param wbook
     */
    private void createHivDrWorksheet(
            List<Case> distinctCases,
            Sheet sheet,
            Workbook wbook,
            List<Long> grantedOrgIds,
            boolean confidential,
            CellStyle cellStyle,
            CellStyle dateCellStyle) {
        int rowIndex = 1;
        for (Case c : distinctCases) {

            List<LabTest> tests =
                    c.getLabTests().parallelStream()
                            .filter(e -> e.getTestType() == ClinicalTestingType.ARV_DR)
                            .collect(Collectors.toList());

            for (LabTest test : tests) {
                int colIndex = 0;

                Row row = sheet.createRow(rowIndex++);
                row.setHeightInPoints(22);

                // Mã UUID của bệnh nhân
                Cell cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(c.getId().toString());

                Organization org = test.getOrganization();

                // Tỉnh - thành phố
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (org.getAddress() != null && org.getAddress().getProvince() != null) {
                    cell.setCellValue(org.getAddress().getProvince().getName());
                } else {
                    cell.setCellValue("-");
                }

                // Tên cơ sở
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(org != null ? org.getName() : "-");

                // Mã bệnh án
                CaseOrg co = null;
                List<CaseOrg> cos = coRepos.findByCaseAndOrg(c.getId(), org.getId());

                if (cos.size() > 0) {
                    co = cos.get(0);
                }

                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (co != null && !CommonUtils.isEmpty(co.getPatientChartId())) {
                    cell.setCellValue(co.getPatientChartId());
                } else {
                    cell.setCellValue("-");
                }

                // Họ tên bệnh nhân
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (CommonUtils.isEmpty(c.getPerson().getFullname())) {
                    cell.setCellValue("-");
                } else {
                    cell.setCellValue(confidential ? "-" : c.getPerson().getFullname());
                }

                // Mã UUID của bản ghi
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(test.getUid().toString());

                // Ngày lấy mẫu
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                if (test.getSampleDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(test.getSampleDate()));
                } else {
                    cell.setCellValue("-");
                }

                // Ngày xét nghiệm
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                if (test.getResultDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(test.getResultDate()));
                } else {
                    cell.setCellValue("-");
                }

                // Kết quả xét nghiệm
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (!CommonUtils.isEmpty(test.getResultText())) {
                    cell.setCellValue(test.getResultText().replace("$$", ", "));
                } else {
                    if (test.getResultDate() != null) {
                        cell.setCellValue("Không kháng thuốc ARV");
                    } else {
                        cell.setCellValue("-");
                    }
                }

                // Ghi chú
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(!CommonUtils.isEmpty(test.getNote()) ? test.getNote() : "-");
            }
        }

        // Hide ID columns
        sheet.setColumnHidden(0, true);
        sheet.setColumnHidden(5, true);

        // Auto-filter
        if (rowIndex >= 1) {
            sheet.setAutoFilter(CellRangeAddress.valueOf("A1:J" + rowIndex));
        }
    }

    /**
     * Hepatitis work sheet
     *
     * @param entities
     * @param sheet
     * @param wbook
     */
    private void createHepatitisWorksheet(
            List<Case> distinctCases,
            Sheet sheet,
            Workbook wbook,
            List<Long> grantedOrgIds,
            boolean confidential,
            CellStyle cellStyle,
            CellStyle dateCellStyle) {
        int rowIndex = 1;
        for (Case c : distinctCases) {
            Iterator<Hepatitis> tests = c.getHepatitises().iterator();

            while (tests.hasNext()) {
                Hepatitis test = tests.next();

                int colIndex = 0;

                Row row = sheet.createRow(rowIndex++);
                row.setHeightInPoints(22);

                // Mã UUID của bệnh nhân
                Cell cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(c.getId().toString());

                Organization org = test.getOrganization();

                // Tỉnh - thành phố
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (org.getAddress() != null && org.getAddress().getProvince() != null) {
                    cell.setCellValue(org.getAddress().getProvince().getName());
                } else {
                    cell.setCellValue("-");
                }

                // Tên cơ sở
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(org != null ? org.getName() : "-");

                // Mã bệnh án
                CaseOrg co = null;
                List<CaseOrg> cos = coRepos.findByCaseAndOrg(c.getId(), org.getId());

                if (cos.size() > 0) {
                    co = cos.get(0);
                }

                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (co != null && !CommonUtils.isEmpty(co.getPatientChartId())) {
                    cell.setCellValue(co.getPatientChartId());
                } else {
                    cell.setCellValue("-");
                }

                // Họ tên bệnh nhân
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (CommonUtils.isEmpty(c.getPerson().getFullname())) {
                    cell.setCellValue("-");
                } else {
                    cell.setCellValue(confidential ? "-" : c.getPerson().getFullname());
                }

                // Tình trạng của bệnh nhân tại cơ sở
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (co.getStatus() != null) {
                    cell.setCellValue(co.getStatus().toString());
                } else {
                    cell.setCellValue("-");
                }

                // Mã UUID của bản ghi
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(test.getUid().toString());

                // Loại viêm gan
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(test.getTestType().toString());

                // Ngày xét nghiệm
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                if (test.getTestDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(test.getTestDate()));
                } else {
                    cell.setCellValue("-");
                }

                // Kết quả XN
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (test.isTestPositive()) {
                    cell.setCellValue("Dương tính");
                } else {
                    cell.setCellValue("Âm tính");
                }

                // Ngày bắt đầu điều trị
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                if (test.getTxStartDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(test.getTxStartDate()));
                } else {
                    cell.setCellValue("-");
                }

                // Ngày kết thúc điều trị
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                if (test.getTxEndDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(test.getTxEndDate()));
                } else {
                    cell.setCellValue("-");
                }
            }
        }

        // Hide ID columns
        sheet.setColumnHidden(0, true);
        sheet.setColumnHidden(6, true);

        // Auto-filter
        if (rowIndex >= 1) {
            sheet.setAutoFilter(CellRangeAddress.valueOf("A1:L" + rowIndex));
        }
    }

    /**
     * WHO stage work sheet
     *
     * @param entities
     * @param sheet
     * @param wbook
     */
    private void createWhoStageWorksheet(
            List<Case> distinctCases,
            Sheet sheet,
            Workbook wbook,
            List<Long> grantedOrgIds,
            boolean confidential,
            CellStyle cellStyle,
            CellStyle dateCellStyle) {
        int rowIndex = 1;
        for (Case c : distinctCases) {
            Iterator<ClinicalStage> stages = c.getWhoStages().iterator();

            while (stages.hasNext()) {
                ClinicalStage stage = stages.next();

                int colIndex = 0;

                Row row = sheet.createRow(rowIndex++);
                row.setHeightInPoints(22);

                // Mã UUID của bệnh nhân
                Cell cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(c.getId().toString());

                Organization org = stage.getOrganization();

                // Tỉnh - thành phố
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (org.getAddress() != null && org.getAddress().getProvince() != null) {
                    cell.setCellValue(org.getAddress().getProvince().getName());
                } else {
                    cell.setCellValue("-");
                }

                // Tên cơ sở
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(org != null ? org.getName() : "-");

                // Mã bệnh án
                CaseOrg co = null;
                List<CaseOrg> cos = coRepos.findByCaseAndOrg(c.getId(), org.getId());

                if (cos.size() > 0) {
                    co = cos.get(0);
                }

                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (co != null && !CommonUtils.isEmpty(co.getPatientChartId())) {
                    cell.setCellValue(co.getPatientChartId());
                } else {
                    cell.setCellValue("-");
                }

                // Họ tên bệnh nhân
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (CommonUtils.isEmpty(c.getPerson().getFullname())) {
                    cell.setCellValue("-");
                } else {
                    cell.setCellValue(confidential ? "-" : c.getPerson().getFullname());
                }

                // Mã UUID của bản ghi
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(stage.getUid().toString());

                // Giai đoạn lâm sàng
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(stage.getStage());

                // Ngày đánh giá
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellValue(CommonUtils.fromLocalDateTime(stage.getEvalDate()));
                cell.setCellStyle(dateCellStyle);

                // Ghi chú
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (!CommonUtils.isEmpty(stage.getNote())) {
                    cell.setCellValue(stage.getNote());
                } else {
                    cell.setCellValue("-");
                }
            }
        }

        // Hide ID columns
        sheet.setColumnHidden(0, true);
        sheet.setColumnHidden(5, true);

        // Auto-filter
        if (rowIndex >= 1) {
            sheet.setAutoFilter(CellRangeAddress.valueOf("A1:I" + rowIndex));
        }
    }

    /**
     * MMT treatment work sheet
     *
     * @param entities
     * @param sheet
     * @param wbook
     */
    private void createMmtWorksheet(
            List<Case> distinctCases,
            Sheet sheet,
            Workbook wbook,
            List<Long> grantedOrgIds,
            boolean confidential,
            CellStyle cellStyle,
            CellStyle dateCellStyle) {

        int rowIndex = 1;
        for (Case entity : distinctCases) {
            MMT mmt = entity.getMmt();

            if (mmt == null || !CommonUtils.isPositive(mmt.getId(), true)) {
                continue;
            }

            int colIndex = 0;

            Row row = sheet.createRow(rowIndex++);
            row.setHeightInPoints(22);

            // Mã UUID của bệnh nhân
            Cell cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(entity.getId().toString());

            Organization org = null;
            CaseOrg latestco = null; // latest case-org with the org in the granted org list
            Iterator<CaseOrg> caseOrgs = entity.getCaseOrgs().iterator();

            if (caseOrgs.hasNext()) {
                latestco = caseOrgs.next();
                org = latestco.getOrganization();
            }

            // Tỉnh - thành phố
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (org != null && org.getAddress() != null && org.getAddress().getProvince() != null) {
                cell.setCellValue(org.getAddress().getProvince().getName());
            } else {
                cell.setCellValue("-");
            }

            // Tên cơ sở
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(org != null ? org.getName() : "-");

            // Mã bệnh án
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(
                    CommonUtils.isEmpty(latestco.getPatientChartId())
                            ? "-"
                            : latestco.getPatientChartId());

            // Họ tên bệnh nhân
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (CommonUtils.isEmpty(entity.getPerson().getFullname())) {
                cell.setCellValue("-");
            } else {
                cell.setCellValue(confidential ? "-" : entity.getPerson().getFullname());
            }

            // Mã UUID của bản ghi
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(mmt.getUid().toString());

            // Ngày bắt đầu điều trị
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (mmt.getStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(mmt.getStartDate()));
            } else {
                cell.setCellValue("-");
            }

            // Ngày kết thúc điều trị
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (mmt.getEndDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(mmt.getEndDate()));
            } else {
                cell.setCellValue("-");
            }

            // Tên cơ sở điều trị Methadone
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isEmpty(mmt.getFacilityName())) {
                cell.setCellValue(mmt.getFacilityName());
            } else {
                cell.setCellValue("-");
            }

            // Ghi chú
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isEmpty(mmt.getNote())) {
                cell.setCellValue(mmt.getNote());
            } else {
                cell.setCellValue("-");
            }
        }

        // Hide ID columns
        sheet.setColumnHidden(0, true);
        sheet.setColumnHidden(5, true);

        // Auto-filter
        if (rowIndex >= 1) {
            sheet.setAutoFilter(CellRangeAddress.valueOf("A1:J" + rowIndex));
        }
    }

    /**
     * Pregnancy work sheet
     *
     * @param entities
     * @param sheet
     * @param wbook
     */
    private void createPregnancyWorksheet(
            List<Case> distinctCases,
            Sheet sheet,
            Workbook wbook,
            List<Long> grantedOrgIds,
            boolean confidential,
            CellStyle cellStyle,
            CellStyle dateCellStyle) {
        int rowIndex = 1;
        for (Case c : distinctCases) {
            Iterator<Pregnancy> pregs = c.getPregnancies().iterator();

            while (pregs.hasNext()) {
                Pregnancy preg = pregs.next();

                int colIndex = 0;

                Row row = sheet.createRow(rowIndex++);
                row.setHeightInPoints(22);

                // Mã UUID của bệnh nhân
                Cell cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(c.getId().toString());

                Organization org = preg.getOrganization();

                // Tỉnh - thành phố
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (org.getAddress() != null && org.getAddress().getProvince() != null) {
                    cell.setCellValue(org.getAddress().getProvince().getName());
                } else {
                    cell.setCellValue("-");
                }

                // Tên cơ sở
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(org != null ? org.getName() : "-");

                // Mã bệnh án
                CaseOrg co = null;
                List<CaseOrg> cos = coRepos.findByCaseAndOrg(c.getId(), org.getId());

                if (cos.size() > 0) {
                    co = cos.get(0);
                }

                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (co != null && !CommonUtils.isEmpty(co.getPatientChartId())) {
                    cell.setCellValue(co.getPatientChartId());
                } else {
                    cell.setCellValue("-");
                }

                // Họ tên bệnh nhân
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (CommonUtils.isEmpty(c.getPerson().getFullname())) {
                    cell.setCellValue("-");
                } else {
                    cell.setCellValue(confidential ? "-" : c.getPerson().getFullname());
                }

                // Mã UUID của bản ghi
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(preg.getUid().toString());

                // Ngày của kỳ kinh cuối
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                if (preg.getLastMenstrualPeriod() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(preg.getLastMenstrualPeriod()));
                } else {
                    cell.setCellValue("-");
                }

                // Ngày dự sinh
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                if (preg.getDueDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(preg.getDueDate()));
                } else {
                    cell.setCellValue("-");
                }

                // Kết quả mang thai
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(
                        preg.getPregResult() != null ? preg.getPregResult().toString() : "-");

                // Ngày sinh
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                if (preg.getChildDob() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(preg.getChildDob()));
                } else {
                    cell.setCellValue("-");
                }

                // Con có được điều trị dự phòng?
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (preg.getChildProphylaxis() != null) {
                    switch (preg.getChildProphylaxis()) {
                        case 1:
                            cell.setCellValue("Có");
                            break;
                        case 2:
                            cell.setCellValue("Không");
                            break;
                        case 3:
                            cell.setCellValue("Không biết");
                            break;
                        default:
                            break;
                    }
                } else {
                    cell.setCellValue("-");
                }

                // Ngày chẩn đoán HIV của con
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                if (preg.getChildDiagnosedDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(preg.getChildDiagnosedDate()));
                } else {
                    cell.setCellValue("-");
                }

                // Tình trạng nhiễm của con
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                switch (preg.getChildHIVStatus()) {
                    case 0:
                        cell.setCellValue("-");
                        break;
                    case 1:
                        cell.setCellValue("Không nhiễm HIV");
                        break;
                    case 2:
                        cell.setCellValue("Nhiễm HIV");
                        break;
                    case 3:
                        cell.setCellValue("Không rõ");
                        break;
                }

                // Con được điều trị ARV?
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (preg.getChildHIVStatus() == 2) {
                    cell.setCellValue(
                            preg.isChildInitiatedOnART() ? "Đã điều trị" : "Chưa điều trị");
                } else {
                    cell.setCellValue("-");
                }

                // Tên cơ sở điều trị ARV cho con
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (!CommonUtils.isEmpty(preg.getChildOpc())) {
                    cell.setCellValue(preg.getChildOpc());
                } else {
                    cell.setCellValue("-");
                }

                // Ghi chú
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (!CommonUtils.isEmpty(preg.getNote())) {
                    cell.setCellValue(preg.getNote());
                } else {
                    cell.setCellValue("-");
                }
            }
        }

        // Hide ID columns
        sheet.setColumnHidden(0, true);
        sheet.setColumnHidden(5, true);

        // Auto-filter
        if (rowIndex >= 1) {
            sheet.setAutoFilter(CellRangeAddress.valueOf("A1:P" + rowIndex));
        }
    }

    /**
     * Native query result
     *
     * @author bizic
     */
    private static class QueryResult {
        private long totalElements;

        private List<CaseOrg> content;

        private QueryResult() {}

        private QueryResult(long totalElement, List<CaseOrg> content) {
            this.totalElements = totalElement;
            this.content = content;
        }

        public long getTotalElements() {
            return totalElements;
        }

        public void setTotalElements(long totalElements) {
            this.totalElements = totalElements;
        }

        public List<CaseOrg> getContent() {
            return content;
        }

        public void setContent(List<CaseOrg> content) {
            this.content = content;
        }
    }

    @Override
    public Workbook exportListPatientOnMMD(OPCDashboardFilterDto filter) {

        Workbook wbook = null;
        try (InputStream template =
                context.getResource("classpath:templates/bn-arv-nhieu-thang-template.xlsx")
                        .getInputStream()) {
            XSSFWorkbook tmp = new XSSFWorkbook(template);
            Sheet sheet = tmp.getSheetAt(0);
            ExcelUtils.createAndWriteInCell(sheet, 0, 1, "", 30, 16, true);
            ExcelUtils.createAndWriteInCell(sheet, 1, 1, "", 22, 12, false);
            wbook = new SXSSFWorkbook(tmp, 100);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Workbook blankBook = new XSSFWorkbook();
        blankBook.createSheet();

        if (wbook == null) {
            return blankBook;
        }

        List<Long> actualOrgIds = getGrantedOrganizationIds(filter);
        if (actualOrgIds == null || actualOrgIds.size() <= 0) {
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
        int rowIndex = 4;
        int colIndex = 0;

        Row row = null;
        Cell cell = null;
        boolean confidential = confidentialRequired();
        //		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        //		row = sheet.createRow(1);
        //		cell = row.createCell(0);
        //		cell.setCellValue("Ngày xuất dữ liệu: "+
        // sdf.format(CommonUtils.fromLocalDateTime(CommonUtils.hanoiNow())));

        Timestamp cutpoint = CommonUtils.toTimestamp(CommonUtils.hanoiTodayEnd());

        for (Long orgId : actualOrgIds) {

            List<CaseOrg> list = coRepos.findCases4MMDIndicators(orgId, 2, false, cutpoint);

            // Start filling out data...
            for (CaseOrg co : list) {

                if (co == null) {
                    continue;
                }

                Organization currentOrg = co.getOrganization();
                Case theCase = co.getTheCase();

                colIndex = 0;
                row = sheet.createRow(rowIndex++);
                row.setHeightInPoints(22);

                // Khoá chính
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(theCase.getId().toString());

                // Tỉnh - thành phố của cơ sở
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);

                if (currentOrg != null
                        && currentOrg.getAddress() != null
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

                // Mã bệnh nhân hivinfo
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);

                if (theCase.getHivInfoID() != null) {
                    cell.setCellValue(theCase.getHivInfoID());
                } else {
                    cell.setCellValue("-");
                }

                // Mã bệnh án
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

                // Ngày bắt đầu ARV
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                if (theCase.getArvStartDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getArvStartDate()));
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

                // ngày bắt đầu nhận arv nhiều tháng
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                Iterator<MMDispensing> mmdEvals = theCase.getMmdEvals().iterator();
                MMDispensing mmdEval = null;

                while (mmdEvals.hasNext()) {
                    MMDispensing temp = mmdEvals.next();
                    if (temp.isOnMmd() && !CommonUtils.isTrue(temp.getDeleted())) {
                        mmdEval = temp;
                        break;
                    }
                }

                if (mmdEval != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(mmdEval.getEvaluationDate()));
                } else {
                    cell.setCellValue("-");
                }

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

                // Ngày xét nghiệm sàng lọc HIV
                cell = row.createCell(colIndex++, CellType.NUMERIC);
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

                // Tình trạng bệnh nhân
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue((co.getStatus() != null) ? co.getStatus().toString() : "-");

                // Ngày thay đổi tình trạng
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                if (co.getStatus() == PatientStatus.ACTIVE) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(co.getStartDate()));
                } else if (co.getEndDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(co.getEndDate()));
                }
            }
        }

        // Hide ID columns
        //		sheet.setColumnHidden(0, true);
        //		sheet.setColumnHidden(3, true);

        // Auto-filter
        if (rowIndex >= 5) {
            sheet.setAutoFilter(CellRangeAddress.valueOf("A4:AB" + rowIndex));
        }
        return wbook;
    }

    @Override
    public Workbook exportListPatientTLD(OPCDashboardFilterDto filter) {

        Workbook wbook = null;
        try (InputStream template =
                context.getResource("classpath:templates/bn-arv-dung-tld-template.xlsx")
                        .getInputStream()) {
            XSSFWorkbook tmp = new XSSFWorkbook(template);
            Sheet sheet = tmp.getSheetAt(0);
            ExcelUtils.createAndWriteInCell(sheet, 0, 1, "", 30, 16, true);
            ExcelUtils.createAndWriteInCell(sheet, 1, 1, "", 22, 12, false);
            wbook = new SXSSFWorkbook(tmp, 100);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Workbook blankBook = new XSSFWorkbook();
        blankBook.createSheet();

        if (wbook == null) {
            return blankBook;
        }

        List<Long> actualOrgIds = getGrantedOrganizationIds(filter);
        if (actualOrgIds == null || actualOrgIds.size() <= 0) {
            return blankBook;
        }

        List<Case> list = repos.getListPatientTLD(actualOrgIds, "dtg");

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
        int rowIndex = 4;
        int colIndex = 0;

        Row row = null;
        Cell cell = null;
        boolean confidential = confidentialRequired();
        //		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        //		row = sheet.createRow(1);
        //		cell = row.createCell(0);
        //		cell.setCellValue("Ngày xuất dữ liệu: "+
        // sdf.format(CommonUtils.fromLocalDateTime(CommonUtils.hanoiNow())));

        // Start filling out data...
        for (Case entity : list) {

            Organization currentOrg = null;

            if (entity == null) {
                continue;
            }

            colIndex = 0;
            row = sheet.createRow(rowIndex++);
            row.setHeightInPoints(22);

            // Khoá chính
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(entity.getId().toString());

            // Tỉnh - thành phố của cơ sở
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);

            CaseOrg currentCO = null; // latest case-org with the org in the granted org list
            try {
                Iterator<CaseOrg> caseOrgs = entity.getCaseOrgs().iterator();
                while (caseOrgs.hasNext()) {
                    CaseOrg co = caseOrgs.next();

                    //					if (currentOrg.getId().longValue() ==
                    // co.getOrganization().getId().longValue()) {
                    //						currentCO = co;
                    //						break;
                    //					}
                    if (co.getCurrent() != null
                            && co.getCurrent()
                            && !co.isRefTrackingOnly()
                            && co.isLatestRelationship()) {
                        currentCO = co;
                        currentOrg = co.getOrganization();
                        break;
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
                entity = repos.findOne(entity.getId());
                Iterator<CaseOrg> caseOrgs = entity.getCaseOrgs().iterator();
                while (caseOrgs.hasNext()) {
                    CaseOrg co = caseOrgs.next();

                    if (co.getCurrent() != null
                            && co.getCurrent()
                            && !co.isRefTrackingOnly()
                            && co.isLatestRelationship()) {
                        currentCO = co;
                        currentOrg = co.getOrganization();
                        break;
                    }
                }
            }

            if (currentOrg != null
                    && currentOrg.getAddress() != null
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

            // Mã bệnh nhân hivinfo
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);

            if (entity.getHivInfoID() != null) {
                cell.setCellValue(entity.getHivInfoID());
            } else {
                cell.setCellValue("-");
            }

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
                cell.setCellValue(confidential ? "-" : entity.getPerson().getFullname());
            }

            // Giới tính
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(entity.getPerson().getGender().toString());

            // Ngày sinh
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellValue(CommonUtils.fromLocalDateTime(entity.getPerson().getDob()));
            cell.setCellStyle(dateCellStyle);

            // Ngày bắt đầu ARV
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (entity.getArvStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(entity.getArvStartDate()));
            } else {
                cell.setCellValue("-");
            }
            // Phác đồ thuốc hiện tại
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(
                    !CommonUtils.isEmpty(entity.getCurrentArvRegimenName())
                            ? entity.getCurrentArvRegimenName()
                            : "-");

            // Ngày bắt đầu phác đồ hiện tại
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (entity.getCurrentArvRegimenStartDate() != null) {
                cell.setCellValue(
                        CommonUtils.fromLocalDateTime(entity.getCurrentArvRegimenStartDate()));
            } else {
                cell.setCellValue("-");
            }

            // Addresses
            Location rAddress = null;
            Location cAddress = null;

            Set<Location> locs = entity.getPerson().getLocations();
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

            // Ngày xét nghiệm sàng lọc HIV
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (entity.getHivScreenDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(entity.getHivScreenDate()));
            } else {
                cell.setCellValue("-");
            }

            // Ngày XN khẳng định
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (entity.getHivConfirmDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(entity.getHivConfirmDate()));
            } else {
                cell.setCellValue("-");
            }

            // Cơ sở XN khẳng định
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(
                    !CommonUtils.isEmpty(entity.getConfirmLabName())
                            ? entity.getConfirmLabName()
                            : "-");

            // Tình trạng bệnh nhân
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(
                    (currentCO.getStatus() != null) ? currentCO.getStatus().toString() : "-");

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
        //		sheet.setColumnHidden(0, true);
        //		sheet.setColumnHidden(3, true);

        // Auto-filter
        if (rowIndex >= 5) {
            sheet.setAutoFilter(CellRangeAddress.valueOf("A4:AB" + rowIndex));
        }
        return wbook;
    }

    @Override
    public Workbook exportListPatientRiskGroup(OPCDashboardFilterDto filter) {
        Workbook wbook = null;
        try (InputStream template =
                context.getResource("classpath:templates/bn-can-danhgia-nhom-nguyco-template.xlsx")
                        .getInputStream()) {
            XSSFWorkbook tmp = new XSSFWorkbook(template);
            Sheet sheet = tmp.getSheetAt(0);
            ExcelUtils.createAndWriteInCell(sheet, 0, 1, "", 30, 16, true);
            ExcelUtils.createAndWriteInCell(sheet, 1, 1, "", 22, 12, false);
            wbook = new SXSSFWorkbook(tmp, 100);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Workbook blankBook = new XSSFWorkbook();
        blankBook.createSheet();

        if (wbook == null) {
            return blankBook;
        }

        List<Long> actualOrgIds = getGrantedOrganizationIds(filter);
        if (actualOrgIds == null || actualOrgIds.size() <= 0) {
            return blankBook;
        }

        List<Case> list = repos.getListPatientRiskAssessmentNeeded(actualOrgIds);

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
        int rowIndex = 4;
        int colIndex = 0;

        Row row = null;
        Cell cell = null;
        boolean confidential = confidentialRequired();
        //		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        //		row = sheet.createRow(1);
        //		cell = row.createCell(0);
        //		cell.setCellValue("Ngày xuất dữ liệu: "+
        // sdf.format(CommonUtils.fromLocalDateTime(CommonUtils.hanoiNow())));

        // Start filling out data...
        for (Case entity : list) {

            Organization currentOrg = null;

            if (entity == null) {
                continue;
            }

            colIndex = 0;
            row = sheet.createRow(rowIndex++);
            row.setHeightInPoints(22);

            // Khoá chính
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(entity.getId().toString());

            // Tỉnh - thành phố của cơ sở
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);

            CaseOrg currentCO = null; // latest case-org with the org in the granted org list
            try {
                Iterator<CaseOrg> caseOrgs = entity.getCaseOrgs().iterator();
                while (caseOrgs.hasNext()) {
                    CaseOrg co = caseOrgs.next();

                    //					if (currentOrg.getId().longValue() ==
                    // co.getOrganization().getId().longValue()) {
                    //						currentCO = co;
                    //						break;
                    //					}
                    if (co.getCurrent() != null
                            && co.getCurrent()
                            && !co.isRefTrackingOnly()
                            && co.isLatestRelationship()) {
                        currentCO = co;
                        currentOrg = co.getOrganization();
                        break;
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
                entity = repos.findOne(entity.getId());
                Iterator<CaseOrg> caseOrgs = entity.getCaseOrgs().iterator();
                while (caseOrgs.hasNext()) {
                    CaseOrg co = caseOrgs.next();

                    if (co.getCurrent() != null
                            && co.getCurrent()
                            && !co.isRefTrackingOnly()
                            && co.isLatestRelationship()) {
                        currentCO = co;
                        currentOrg = co.getOrganization();
                        break;
                    }
                }
            }

            if (currentOrg != null
                    && currentOrg.getAddress() != null
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

            // Mã bệnh nhân hivinfo
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);

            if (entity.getHivInfoID() != null) {
                cell.setCellValue(entity.getHivInfoID());
            } else {
                cell.setCellValue("-");
            }

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
                cell.setCellValue(confidential ? "-" : entity.getPerson().getFullname());
            }

            // Giới tính
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(entity.getPerson().getGender().toString());

            // Ngày sinh
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellValue(CommonUtils.fromLocalDateTime(entity.getPerson().getDob()));
            cell.setCellStyle(dateCellStyle);

            // Addresses
            Location rAddress = null;
            Location cAddress = null;

            Set<Location> locs = entity.getPerson().getLocations();
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

            // Ngày xét nghiệm sàng lọc HIV
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (entity.getHivScreenDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(entity.getHivScreenDate()));
            } else {
                cell.setCellValue("-");
            }

            // Ngày XN khẳng định
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (entity.getHivConfirmDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(entity.getHivConfirmDate()));
            } else {
                cell.setCellValue("-");
            }

            // Cơ sở XN khẳng định
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(
                    !CommonUtils.isEmpty(entity.getConfirmLabName())
                            ? entity.getConfirmLabName()
                            : "-");

            // Ngày bắt đầu ARV
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (entity.getArvStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(entity.getArvStartDate()));
            } else {
                cell.setCellValue("-");
            }
            // Phác đồ thuốc hiện tại
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(
                    !CommonUtils.isEmpty(entity.getCurrentArvRegimenName())
                            ? entity.getCurrentArvRegimenName()
                            : "-");

            // Ngày bắt đầu phác đồ hiện tại
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (entity.getCurrentArvRegimenStartDate() != null) {
                cell.setCellValue(
                        CommonUtils.fromLocalDateTime(entity.getCurrentArvRegimenStartDate()));
            } else {
                cell.setCellValue("-");
            }

            // Tình trạng bệnh nhân
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(
                    (currentCO.getStatus() != null) ? currentCO.getStatus().toString() : "-");

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
        //		sheet.setColumnHidden(0, true);
        //		sheet.setColumnHidden(3, true);

        // Auto-filter
        if (rowIndex >= 5) {
            sheet.setAutoFilter(CellRangeAddress.valueOf("A4:AB" + rowIndex));
        }
        return wbook;
    }

    @Override
    public Workbook exportListPatientTBScreening(OPCDashboardFilterDto filter) {
        Workbook blankBook = new XSSFWorkbook();
        blankBook.createSheet();

        List<Long> actualOrgIds = getGrantedOrganizationIds(filter);
        if (actualOrgIds == null || actualOrgIds.size() <= 0) {
            return blankBook;
        }

        // list of 5 months
        int MONTH_COUNT = 5;
        List<DateRangeDto> ranges = CommonUtils.getBackwardMonths(LocalDateTime.now(), MONTH_COUNT);
        List<Case> data = new ArrayList<Case>();

        for (int i = MONTH_COUNT - 1; i >= 0; i--) {
            DateRangeDto r = ranges.get(i);

            Timestamp fromDate = CommonUtils.toTimestamp(r.getFromDate());
            Timestamp toDate = CommonUtils.toTimestamp(r.getToDate());

            actualOrgIds.parallelStream()
                    .forEach(
                            orgId -> {
                                List<Case> list =
                                        repos.getListPatientTBScreening(
                                                Lists.newArrayList(orgId), fromDate, toDate);

                                if (list != null && list.size() > 0) {
                                    data.addAll(list);
                                }
                            });
        }

        Workbook wbook = null;
        try (InputStream template =
                context.getResource("classpath:templates/bn-sang-loc-lao-duongtinh-template.xlsx")
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
        int rowIndex = 4;
        int colIndex = 0;

        Row row = null;
        Cell cell = null;
        boolean confidential = confidentialRequired();
        //		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        //		row = sheet.createRow(1);
        //		cell = row.createCell(0);
        //		cell.setCellValue("Ngày xuất dữ liệu: " +
        // sdf.format(CommonUtils.fromLocalDateTime(CommonUtils.hanoiNow())));

        // Start filling out data...
        for (Case entity : data) {

            Organization currentOrg = null;

            if (entity == null) {
                continue;
            }

            colIndex = 0;
            row = sheet.createRow(rowIndex++);
            row.setHeightInPoints(22);

            // Khoá chính
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(entity.getId().toString());

            // Tỉnh - thành phố của cơ sở
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);

            CaseOrg currentCO = null; // latest case-org with the org in the granted org list
            try {
                Iterator<CaseOrg> caseOrgs = entity.getCaseOrgs().iterator();
                while (caseOrgs.hasNext()) {
                    CaseOrg co = caseOrgs.next();

                    //					if (currentOrg.getId().longValue() ==
                    // co.getOrganization().getId().longValue()) {
                    //						currentCO = co;
                    //						break;
                    //					}
                    if (co.getCurrent() != null
                            && co.getCurrent()
                            && !co.isRefTrackingOnly()
                            && co.isLatestRelationship()) {
                        currentCO = co;
                        currentOrg = co.getOrganization();
                        break;
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
                entity = repos.findOne(entity.getId());
                Iterator<CaseOrg> caseOrgs = entity.getCaseOrgs().iterator();
                while (caseOrgs.hasNext()) {
                    CaseOrg co = caseOrgs.next();

                    if (co.getCurrent() != null
                            && co.getCurrent()
                            && !co.isRefTrackingOnly()
                            && co.isLatestRelationship()) {
                        currentCO = co;
                        currentOrg = co.getOrganization();
                        break;
                    }
                }
            }

            if (currentOrg != null
                    && currentOrg.getAddress() != null
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

            // Mã bệnh nhân hivinfo
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);

            if (entity.getHivInfoID() != null) {
                cell.setCellValue(entity.getHivInfoID());
            } else {
                cell.setCellValue("-");
            }

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
                cell.setCellValue(confidential ? "-" : entity.getPerson().getFullname());
            }

            // Giới tính
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(entity.getPerson().getGender().toString());

            // Ngày sinh
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellValue(CommonUtils.fromLocalDateTime(entity.getPerson().getDob()));
            cell.setCellStyle(dateCellStyle);
            // Ngày sàng loc lao dương tính gần nhất
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            List<LocalDateTime> latestArrivedDates =
                    appRepos.findLatestArrivedAppointmentsByTbScreenResult(entity.getId(), 1);
            if (latestArrivedDates != null && latestArrivedDates.size() > 0) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(latestArrivedDates.get(0)));
            } else {
                cell.setCellValue("-");
            }

            // Addresses
            Location rAddress = null;
            Location cAddress = null;

            Set<Location> locs = entity.getPerson().getLocations();
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

            // Ngày xét nghiệm sàng lọc HIV
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (entity.getHivScreenDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(entity.getHivScreenDate()));
            } else {
                cell.setCellValue("-");
            }

            // Ngày XN khẳng định
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (entity.getHivConfirmDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(entity.getHivConfirmDate()));
            } else {
                cell.setCellValue("-");
            }

            // Cơ sở XN khẳng định
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(
                    !CommonUtils.isEmpty(entity.getConfirmLabName())
                            ? entity.getConfirmLabName()
                            : "-");

            // Ngày bắt đầu ARV
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (entity.getArvStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(entity.getArvStartDate()));
            } else {
                cell.setCellValue("-");
            }
            // Phác đồ thuốc hiện tại
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(
                    !CommonUtils.isEmpty(entity.getCurrentArvRegimenName())
                            ? entity.getCurrentArvRegimenName()
                            : "-");

            // Ngày bắt đầu phác đồ hiện tại
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (entity.getCurrentArvRegimenStartDate() != null) {
                cell.setCellValue(
                        CommonUtils.fromLocalDateTime(entity.getCurrentArvRegimenStartDate()));
            } else {
                cell.setCellValue("-");
            }
            // Tình trạng bệnh nhân
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(
                    (currentCO.getStatus() != null) ? currentCO.getStatus().toString() : "-");

            // Ngày thay đổi tình trạng
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (currentCO.getStatus() == PatientStatus.ACTIVE) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(currentCO.getStartDate()));
            } else if (currentCO.getEndDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(currentCO.getEndDate()));
            }
        }

        // Auto-filter
        if (rowIndex >= 5) {
            sheet.setAutoFilter(CellRangeAddress.valueOf("A4:AB" + rowIndex));
        }
        return wbook;
    }

    /**
     * Check if confidentiality is required
     *
     * @return
     */
    private boolean confidentialRequired() {
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

        return confidentialRequired;
    }

    /**
     * Get a list of IDs of the organizations that the current user has READ ACCESS to
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
        List<Long> grantedOrgIds =
                authUtils.getGrantedOrgIds(Permission.READ_ACCESS, provinceId, true);

        if (grantedOrgIds == null || grantedOrgIds.size() <= 0) {
            return actualOrgIds;
        }

        if (CommonUtils.isPositive(filter.getOrganizationId(), true)) {
            actualOrgIds.add(filter.getOrganizationId());
        } else if (filter.getOrganizationId() != null && filter.getOrganizationId() == 0l) {

            // Check the province
            if (filter.getProvince() != null
                    && CommonUtils.isPositive(filter.getProvince().getId(), true)) {
                long provId = filter.getProvince().getId();
                Iterator<Organization> orgs =
                        orgRepos.findAll(
                                        QOrganization.organization.id.longValue().in(grantedOrgIds))
                                .iterator();

                while (orgs.hasNext()) {
                    Organization org = orgs.next();
                    if (org.getAddress() != null
                            && org.getAddress().getProvince() != null
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

    /**
     * Sort the case-orgs to avoid wrong ordering due to back-log entries by clinics making sure the
     * history is correctly ordered. The most current (ACTIVE) record must be the first one in the
     * list
     *
     * @param theCase
     * @return
     */
    private List<CaseOrg> sortCaseOrgs(Case theCase) {
        List<CaseOrg> entities = Lists.newArrayList(theCase.getCaseOrgs());

        int activeIndex = -1;
        boolean foundActive = false;
        for (CaseOrg e : entities) {
            activeIndex++;
            if (e.getStatus() == PatientStatus.ACTIVE) {
                foundActive = true;
                break;
            }
        }

        if (foundActive && activeIndex > 0) {
            CaseOrg activeCo = entities.remove(activeIndex);
            entities.add(0, activeCo);
            //			Collections.swap(entities, activeIndex, 0);
        }

        return entities;
    }

    /**
     * Is this case editable by the current user?
     *
     * @param entity
     * @return
     */
    @Override
    public CaseOrgDto checkCaseOrgEditable(
            List<Long> writableOrgIds, CaseOrg entity, boolean needDetails) {
        CaseOrg[] coArr = sortCaseOrgs(entity.getTheCase()).toArray(new CaseOrg[0]);

        boolean caseEditable = !entity.getTheCase().isDeleted();

        boolean singleEditable = coArr != null && coArr.length == 1;
        if (singleEditable) {
            caseEditable =
                    caseEditable
                            && entity.getStatus() != PatientStatus.CANCELLED_ENROLLMENT
                            && entity.getStatus() != PatientStatus.PENDING_ENROLLMENT;
        }

        boolean secondPosEditable =
                coArr != null && coArr.length > 1; // (1st[ref=1] --> 2nd[current record] --> ....).
        if (secondPosEditable) {
            if (coArr[0].getId() != entity.getId().longValue()) {
                caseEditable = caseEditable && coArr[0].isRefTrackingOnly();
                caseEditable = caseEditable && coArr[1].getId() == entity.getId().longValue();
            } else {
                caseEditable =
                        caseEditable && entity.getStatus() == PatientStatus.ACTIVE
                                || entity.getStatus() == PatientStatus.DEAD
                                || entity.getStatus() == PatientStatus.LTFU;
            }
        }

        boolean writable =
                writableOrgIds != null
                        && writableOrgIds.contains(entity.getOrganization().getId().longValue());

        caseEditable = caseEditable && writable;

        CaseOrgDto dto = new CaseOrgDto(entity, needDetails);
        dto.setCaseEditable(caseEditable);

        return dto;
    }
}
