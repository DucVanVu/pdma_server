package org.pepfar.pdma.app.data.service.jpa;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.poi.ss.usermodel.*;
import org.pepfar.pdma.app.data.domain.Case;
import org.pepfar.pdma.app.data.domain.CaseOrg;
import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.domain.Person;
import org.pepfar.pdma.app.data.dto.CaseDto;
import org.pepfar.pdma.app.data.dto.CaseReportFilterDto;
import org.pepfar.pdma.app.data.dto.PersonDto;
import org.pepfar.pdma.app.data.repository.CaseOrgRepository;
import org.pepfar.pdma.app.data.service.ReportingWorker;
import org.pepfar.pdma.app.data.types.EnrollmentType;
import org.pepfar.pdma.app.data.types.PatientStatus;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.ExcelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

@Service
public class _OpcTxNewReturnedWorker implements ReportingWorker<Boolean> {

    @Autowired private CaseOrgRepository coRepos;

    @Autowired private ApplicationContext context;

    private AtomicInteger[] indicators;

    private CaseReportFilterDto filter;

    private static final int COLS = 5;

    @Override
    public Boolean call() throws Exception {
        if (filter == null
                || !CommonUtils.isPositive(filter.getOrganization(), true)
                || filter.getFromDate() == null
                || filter.getToDate() == null) {
            return false;
        }

        // to use some common functions in this reporting service bean
        _OPCReportingServiceImpl reportingService = context.getBean(_OPCReportingServiceImpl.class);

        List<String> enrollmentTypes = Lists.newArrayList(EnrollmentType.RETURNED.name());

        Timestamp fromDate = CommonUtils.toTimestamp(filter.getFromDate());
        Timestamp toDate = CommonUtils.toTimestamp(filter.getToDate());

        List<CaseOrg> cos =
                coRepos.findPatientsByEnrollmentTypes(
                        filter.getOrganization(), fromDate, toDate, enrollmentTypes, false);

        // For regimen line #2 calculation
        int size = cos.size();
        AtomicLong[] caseIds = new AtomicLong[size];
        AtomicInteger indx = new AtomicInteger(0);

        cos.parallelStream()
                .forEach(
                        co -> {
                            Case theCase = co.getTheCase();
                            Person person = theCase.getPerson();

                            // calculate by age range
                            ExcelUtils.increase(filter.getToDate(), person, indicators);

                            // adding to the list
                            caseIds[indx.getAndIncrement()] = new AtomicLong(theCase.getId());
                        });

        // find case-orgs by regimen line
        List<Long> caseIdList = new ArrayList<Long>();
        for (int i = 0; i < size; i++) {
            caseIdList.add(caseIds[i].get());
        }

        if (caseIdList.size() > 0) {
            cos = coRepos.findCaseOrgsByRegimenLine(caseIdList, filter.getOrganization(), toDate, 2);
            indicators[4] = new AtomicInteger(cos.size());
        }

        return true;
    }

    @Override
    public Sheet calcAndUpdateRawdata(Workbook wbook, Sheet sheet) {
        if (filter == null
                || !CommonUtils.isPositive(filter.getOrganization(), true)
                || filter.getFromDate() == null
                || filter.getToDate() == null) {
            return sheet;
        }

        List<String> enrollmentTypes = Lists.newArrayList(EnrollmentType.RETURNED.name());

        Timestamp fromDate = CommonUtils.toTimestamp(filter.getFromDate());
        Timestamp toDate = CommonUtils.toTimestamp(filter.getToDate());

        List<CaseOrg> cos =
                coRepos.findPatientsByEnrollmentTypes(
                        filter.getOrganization(), fromDate, toDate, enrollmentTypes, false);

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

        int rowIndex = sheet.getLastRowNum() + 1;

        for (CaseOrg co : cos) {
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
            } else {
                cell.setCellValue("-");
            }

            // Cơ sở điều trị
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (org != null) {
                cell.setCellValue(org.getName());
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

            int targetColIndex = colIndex + 1;

            for (int i = 0; i < 7; i++) {
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(cellStyle);
                cell.setCellValue("");
            }

            cell = row.getCell(targetColIndex);
            cell.setCellValue("x");

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

        return sheet;
    }

    @Override
    public Sheet updateResultSheet(Sheet sheet) {
        if (sheet == null) {
            return null;
        }

        for (int i = 0; i < COLS; i++) {
            if (i == 4) {
                ExcelUtils.writeInCell(sheet, 10, 4 + i, indicators[i].get());
            } else {
                ExcelUtils.writeInCell(sheet, 10, 3 + i, indicators[i].get());
            }
        }

        return sheet;
    }

    @Override
    public void setFilter(CaseReportFilterDto filter) {
        this.indicators = new AtomicInteger[COLS];

        for (int i = 0; i < COLS; i++) {
            this.indicators[i] = new AtomicInteger(0);
        }

        this.filter = filter;
    }
}
