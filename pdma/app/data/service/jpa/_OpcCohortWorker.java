package org.pepfar.pdma.app.data.service.jpa;

import com.beust.jcommander.internal.Lists;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.hibernate.jdbc.Work;
import org.pepfar.pdma.app.data.domain.Case;
import org.pepfar.pdma.app.data.domain.CaseOrg;
import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.domain.Person;
import org.pepfar.pdma.app.data.dto.CaseReportFilterDto;
import org.pepfar.pdma.app.data.repository.CaseOrgRepository;
import org.pepfar.pdma.app.data.service.ReportingWorker;
import org.pepfar.pdma.app.data.types.PatientStatus;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.ExcelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class _OpcCohortWorker implements ReportingWorker<Boolean> {

    @Autowired private CaseOrgRepository coRepos;

    private AtomicInteger[][] indicators;

    private CaseReportFilterDto filter;

    private static final int ROWS = 5;

    private static final int COLS = 4;

    private Timestamp tsLastYearFromDate;

    private Timestamp tsLastYearToDate;

    private Timestamp fromDate;

    private Timestamp toDate;

    @Override
    public Boolean call() throws Exception {

        // Date time calculation
        calculateDates();

        // Initial patients
        List<CaseOrg> cos =
                coRepos.cohortInitialPatients(
                        filter.getOrganization(), tsLastYearFromDate, tsLastYearToDate);

        cos.parallelStream()
                .forEach(
                        co -> {
                            Case theCase = co.getTheCase();
                            Person person = theCase.getPerson();

                            ExcelUtils.increase(filter.getToDate(), person, indicators[0]);
                        });

        // Transed in
        cos =
                coRepos.cohortTransferredInPatients(
                        filter.getOrganization(), tsLastYearFromDate, tsLastYearToDate, toDate);

        cos.parallelStream()
                .forEach(
                        co -> {
                            Case theCase = co.getTheCase();
                            Person person = theCase.getPerson();

                            ExcelUtils.increase(filter.getToDate(), person, indicators[1]);
                        });

        // Transed out
        cos =
                coRepos.cohortTransferredOutPatients(
                        filter.getOrganization(), tsLastYearFromDate, tsLastYearToDate, toDate);

        cos.parallelStream()
                .forEach(
                        co -> {
                            Case theCase = co.getTheCase();
                            Person person = theCase.getPerson();

                            ExcelUtils.increase(filter.getToDate(), person, indicators[2]);
                        });

        // Ltfu
        cos =
                coRepos.cohortDeadLTFUPatients(
                        filter.getOrganization(),
                        tsLastYearFromDate,
                        tsLastYearToDate,
                        toDate,
                        "LTFU");

        cos.parallelStream()
                .forEach(
                        co -> {
                            Case theCase = co.getTheCase();
                            Person person = theCase.getPerson();

                            ExcelUtils.increase(filter.getToDate(), person, indicators[3]);
                        });

        // Dead
        cos =
                coRepos.cohortDeadLTFUPatients(
                        filter.getOrganization(),
                        tsLastYearFromDate,
                        tsLastYearToDate,
                        toDate,
                        "DEAD");

        cos.parallelStream()
                .forEach(
                        co -> {
                            Case theCase = co.getTheCase();
                            Person person = theCase.getPerson();

                            ExcelUtils.increase(filter.getToDate(), person, indicators[4]);
                        });

        return true;
    }

    @Override
    public Sheet updateResultSheet(Sheet sheet) {
        if (sheet == null) {
            return null;
        }

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (i > 2) {
                    ExcelUtils.writeInCell(sheet, 35 + i, 3 + j, indicators[i][j].get());
                } else {
                    ExcelUtils.writeInCell(sheet, 34 + i, 3 + j, indicators[i][j].get());
                }
            }
        }

        return sheet;
    }

    @Override
    public void setFilter(CaseReportFilterDto filter) {
        this.indicators = new AtomicInteger[ROWS][COLS];

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                this.indicators[i][j] = new AtomicInteger(0);
            }
        }

        this.filter = filter;
    }

    @Override
    public Sheet calcAndUpdateRawdata(Workbook wbook, Sheet sheet) {

        // Calculate date/time
        calculateDates();

        // Initial patients
        List<CaseOrg> cos =
                coRepos.cohortInitialPatients(
                        filter.getOrganization(), tsLastYearFromDate, tsLastYearToDate);
        filloutSheet(wbook, sheet, cos, 0);

        // Transed in
        cos =
                coRepos.cohortTransferredInPatients(
                        filter.getOrganization(), tsLastYearFromDate, tsLastYearToDate, toDate);
        filloutSheet(wbook, sheet, cos, 1);

        // Transed out
        cos =
                coRepos.cohortTransferredOutPatients(
                        filter.getOrganization(), tsLastYearFromDate, tsLastYearToDate, toDate);
        filloutSheet(wbook, sheet, cos, 2);

        // Ltfu
        cos =
                coRepos.cohortDeadLTFUPatients(
                        filter.getOrganization(),
                        tsLastYearFromDate,
                        tsLastYearToDate,
                        toDate,
                        "LTFU");
        filloutSheet(wbook, sheet, cos, 3);

        // Dead
        cos =
                coRepos.cohortDeadLTFUPatients(
                        filter.getOrganization(),
                        tsLastYearFromDate,
                        tsLastYearToDate,
                        toDate,
                        "DEAD");
        filloutSheet(wbook, sheet, cos, 4);

        int rowIndex = sheet.getLastRowNum();

        // Auto-filter
        if (rowIndex >= 2) {
            sheet.setAutoFilter(CellRangeAddress.valueOf("A2:O" + rowIndex));
        }

        return sheet;
    }

    private void filloutSheet(Workbook wbook, Sheet sheet, List<CaseOrg> cos, int type) {
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

            int targetColIndex = colIndex + type;

            for (int i = 0; i < 5; i++) {
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(cellStyle);
                cell.setCellValue("");
            }

            cell = row.getCell(targetColIndex);
            cell.setCellValue("x");

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
        }
    }

    private void calculateDates() {
        LocalDateTime thisMonthLastYear =
                filter.getFromDate()
                        .withDayOfMonth(15)
                        .withYear(filter.getFromDate().getYear() - 1);
        LocalDate lastMonthDate = thisMonthLastYear.toLocalDate();

        LocalDateTime lastYearFromDate = CommonUtils.dateStart(thisMonthLastYear.withDayOfMonth(1));
        LocalDateTime lastYearToDate =
                CommonUtils.dateEnd(
                        thisMonthLastYear.withDayOfMonth(
                                lastMonthDate.getMonth().length(lastMonthDate.isLeapYear())));

        tsLastYearFromDate = CommonUtils.toTimestamp(lastYearFromDate);
        tsLastYearToDate = CommonUtils.toTimestamp(lastYearToDate);

        fromDate = CommonUtils.toTimestamp(filter.getFromDate());
        toDate = CommonUtils.toTimestamp(filter.getToDate());
    }
}
