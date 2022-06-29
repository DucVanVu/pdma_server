package org.pepfar.pdma.app.data.service.jpa;

import org.apache.poi.ss.usermodel.*;
import org.docx4j.wml.Numbering;
import org.pepfar.pdma.app.data.domain.Case;
import org.pepfar.pdma.app.data.domain.CaseOrg;
import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.domain.Person;
import org.pepfar.pdma.app.data.dto.CaseDto;
import org.pepfar.pdma.app.data.dto.CaseReportFilterDto;
import org.pepfar.pdma.app.data.dto.PersonDto;
import org.pepfar.pdma.app.data.repository.AppointmentRepository;
import org.pepfar.pdma.app.data.repository.CaseOrgRepository;
import org.pepfar.pdma.app.data.service.ReportingWorker;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.ExcelUtils;
import org.pepfar.pdma.app.utils.MerReportingUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class _MerOpcTxRttWorker implements ReportingWorker<Boolean> {

    @Autowired private CaseOrgRepository coRepos;

    @Autowired private AppointmentRepository appRepos;

    @Autowired private MerReportingUtils merUtils;

    //    @Autowired private ApplicationContext context;

    private CaseReportFilterDto filter;

    private AtomicInteger[][] indicators;

    private static final int ROWS = 3;

    private static final int COLS = 38;

    @Override
    @Transactional(readOnly = true)
    public Boolean call() throws Exception {

        if (filter == null
                || !CommonUtils.isPositive(filter.getOrganization(), true)
                || filter.getFromDate() == null
                || filter.getToDate() == null) {
            return false;
        }

        // to use some common functions in this reporting service bean
        //        _MerOpcReportingServiceImpl reportingService =
        //                context.getBean(_MerOpcReportingServiceImpl.class);

        Timestamp fromDate = CommonUtils.toTimestamp(filter.getFromDate());
        Timestamp toDate = CommonUtils.toTimestamp(filter.getToDate());

        List<Number> activeCaseIdsLastPeriodEnd = new ArrayList<>();
        List<Number> activeCaseIdsThisPeriodEnd = new ArrayList<>();

        LocalDateTime _lastFromDate = filter.getFromDate().minusMonths(3);
        LocalDateTime _lastToDate = filter.getToDate().minusMonths(3);

        boolean leapYear = _lastToDate.toLocalDate().isLeapYear();
        _lastToDate = _lastToDate.withDayOfMonth(_lastToDate.getMonth().length(leapYear));

        System.out.println(
                _lastFromDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss")));
        System.out.println(_lastToDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss")));

        Timestamp lastFromDate = CommonUtils.toTimestamp(_lastFromDate);
        Timestamp lastToDate = CommonUtils.toTimestamp(_lastToDate);

        List<Number> lastPeriod1 =
                coRepos.calculate_TX_CURR_1_1_MER26(
                        filter.getOrganization(), lastFromDate, lastToDate);
        List<Number> lastPeriod2 =
                coRepos.calculate_TX_CURR_2_1_MER26(
                        filter.getOrganization(), lastFromDate, lastToDate);

        if (lastPeriod1.size() > 0) {
            activeCaseIdsLastPeriodEnd.addAll(lastPeriod1);
        }

        if (lastPeriod2.size() > 0) {
            activeCaseIdsLastPeriodEnd.addAll(lastPeriod2);
        }

        List<Number> thisPeriod1 =
                coRepos.calculate_TX_CURR_1_1_MER26(filter.getOrganization(), fromDate, toDate);
        List<Number> thisPeriod2 =
                coRepos.calculate_TX_CURR_2_1_MER26(filter.getOrganization(), fromDate, toDate);

        if (thisPeriod1.size() > 0) {
            activeCaseIdsThisPeriodEnd.addAll(thisPeriod1);
        }

        if (thisPeriod2.size() > 0) {
            activeCaseIdsThisPeriodEnd.addAll(thisPeriod2);
        }

        if (activeCaseIdsLastPeriodEnd.size() <= 0 || activeCaseIdsThisPeriodEnd.size() <= 0) {
            return true;
        }

        for (int i = 0; i < 3; i++) {
            final int disaggregate = i + 1;
            List<CaseOrg> cos =
                    coRepos.calculate_TX_RTT_MER26(
                            filter.getOrganization(),
                            fromDate,
                            toDate,
                            disaggregate,
                            activeCaseIdsLastPeriodEnd,
                            activeCaseIdsThisPeriodEnd);

            final int finalI = i;
            cos.forEach(
                    co -> {
                        Case theCase = co.getTheCase();
                        Person person = theCase.getPerson();

                        CaseDto theCaseDto = new CaseDto();
                        theCaseDto.setId(theCase.getId());

                        PersonDto personDto = new PersonDto();
                        personDto.setId(person.getId());
                        personDto.setDob(person.getDob());
                        personDto.setGender(person.getGender());

                        theCaseDto.setPerson(personDto);

                        // calculate by risk groups
                        merUtils.calculateIndicatorsByRiskgroup(theCaseDto, indicators[finalI]);

                        // calculate by age range
                        merUtils.calculateIndicatorsByAgeRange(
                                filter.getToDate(), theCaseDto, indicators[finalI]);
                    });
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

        // to use some common functions in this reporting service bean
        //        _MerOpcReportingServiceImpl reportingService =
        //                context.getBean(_MerOpcReportingServiceImpl.class);

        Timestamp fromDate = CommonUtils.toTimestamp(filter.getFromDate());
        Timestamp toDate = CommonUtils.toTimestamp(filter.getToDate());

        List<Number> activeCaseIdsLastPeriodEnd = new ArrayList<>();
        List<Number> activeCaseIdsThisPeriodEnd = new ArrayList<>();

        LocalDateTime _lastFromDate = filter.getFromDate().minusMonths(3);
        LocalDateTime _lastToDate = filter.getToDate().minusMonths(3);

        boolean leapYear = _lastToDate.toLocalDate().isLeapYear();
        _lastToDate = _lastToDate.withDayOfMonth(_lastToDate.getMonth().length(leapYear));

        System.out.println(
                _lastFromDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss")));
        System.out.println(_lastToDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss")));

        Timestamp lastFromDate = CommonUtils.toTimestamp(_lastFromDate);
        Timestamp lastToDate = CommonUtils.toTimestamp(_lastToDate);

        List<Number> lastPeriod1 =
                coRepos.calculate_TX_CURR_1_1_MER26(
                        filter.getOrganization(), lastFromDate, lastToDate);
        List<Number> lastPeriod2 =
                coRepos.calculate_TX_CURR_2_1_MER26(
                        filter.getOrganization(), lastFromDate, lastToDate);

        if (lastPeriod1.size() > 0) {
            activeCaseIdsLastPeriodEnd.addAll(lastPeriod1);
        }

        if (lastPeriod2.size() > 0) {
            activeCaseIdsLastPeriodEnd.addAll(lastPeriod2);
        }

        List<Number> thisPeriod1 =
                coRepos.calculate_TX_CURR_1_1_MER26(filter.getOrganization(), fromDate, toDate);
        List<Number> thisPeriod2 =
                coRepos.calculate_TX_CURR_2_1_MER26(filter.getOrganization(), fromDate, toDate);

        if (thisPeriod1.size() > 0) {
            activeCaseIdsThisPeriodEnd.addAll(thisPeriod1);
        }

        if (thisPeriod2.size() > 0) {
            activeCaseIdsThisPeriodEnd.addAll(thisPeriod2);
        }

        if (activeCaseIdsLastPeriodEnd.size() <= 0 || activeCaseIdsThisPeriodEnd.size() <= 0) {
            return sheet;
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

        int rowIndex = sheet.getLastRowNum() + 1;

        String[] disagString = {
            "Thời gian gián đoạn < 3 tháng",
            "Thời gian gián đoạn từ 3 - <6 tháng",
            "Thời gian gián đoạn >= 6 tháng"
        };

        for (int i = 0; i < 3; i++) {
            final int disaggregate = i + 1;
            List<CaseOrg> cos =
                    coRepos.calculate_TX_RTT_MER26(
                            filter.getOrganization(),
                            fromDate,
                            toDate,
                            disaggregate,
                            activeCaseIdsLastPeriodEnd,
                            activeCaseIdsThisPeriodEnd);

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

                // Tên cơ sở điều trị
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (!CommonUtils.isEmpty(org.getName(), true)) {
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

                // Ngày XNKDHIV
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                if (theCase != null && theCase.getHivConfirmDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getHivConfirmDate()));
                } else {
                    cell.setCellValue("-");
                }

                // Ngày bắt đầu arv
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                if (co != null && co.getArvStartDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(co.getArvStartDate()));
                } else {
                    cell.setCellValue("-");
                }

                // Ngày đăng kí tại co so dieu tri
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                if (co != null && co.getStartDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(co.getStartDate()));
                } else {
                    cell.setCellValue("-");
                }

                // loai dang ki
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(dateCellStyle);
                if (co != null && co.getEnrollmentType() != null) {
                    cell.setCellValue(co.getEnrollmentType().toString());
                } else {
                    cell.setCellValue("-");
                }

                // Phân loại
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(dateCellStyle);
                cell.setCellValue(disagString[i]);
            }
        }

        return sheet;
    }

    @Override
    public Sheet updateResultSheet(Sheet sheet) {
        if (sheet == null) {
            return null;
        }

        int row, col;

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {

                row = i + 15;
                col = j + 3;

                ExcelUtils.writeInCell(sheet, row, col, indicators[i][j].get());
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
}
