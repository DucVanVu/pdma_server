package org.pepfar.pdma.app.data.service.jpa;

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
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.ExcelUtils;
import org.pepfar.pdma.app.utils.MerReportingUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class _MerOpcTbPrevWorker implements ReportingWorker<Boolean> {

    //	@Autowired
    //	private ApplicationContext context;

    @Autowired private MerReportingUtils merUtils;

    @Autowired private CaseOrgRepository coRepos;

    private CaseReportFilterDto filter;

    private AtomicInteger[][] indicators;

    private static final int ROWS = 4;

    private static final int COLS = 38;

    private boolean shouldBeIncluded = true;

    @Override
    @Transactional(readOnly = true)
    public Boolean call() throws Exception {

        if (filter == null
                || !CommonUtils.isPositive(filter.getOrganization(), true)
                || filter.getFromDate() == null
                || filter.getToDate() == null) {
            return false;
        }

        // figure out the quarter
        LocalDateTime midDate = filter.getFromDate().plusMonths(1);
        int calendarQuarter = midDate.get(IsoFields.QUARTER_OF_YEAR);

        // 4 will be quarter of the previous calendar year
        // and equals fiscal year quarter #1 of current
        // year.

        shouldBeIncluded = true;
        if (calendarQuarter != 1 && calendarQuarter != 3) {
            shouldBeIncluded = false;
            return true;
        }

        Timestamp fromDate = CommonUtils.toTimestamp(filter.getFromDate());
        Timestamp toDate = CommonUtils.toTimestamp(filter.getToDate());

        //        _MerOpcReportingServiceImpl reportingService =
        //                context.getBean(_MerOpcReportingServiceImpl.class);

        // ---------------------------------------------------------------------
        // numerator -> start prophylaxis in reporting period and within 6 month after ARV
        // initiation, and completed prophylaxis
        // ---------------------------------------------------------------------
        List<CaseOrg> cos =
                coRepos.calculate_TB_PREV_MER26(
                        filter.getOrganization(), fromDate, toDate, true, true);

        calDisaggregates(filter.getToDate(), cos, 0);

        // ---------------------------------------------------------------------
        // numerator -> start prophylaxis in reporting period and after 6 months since ARV
        // initiation, and completed prophylaxis
        // ---------------------------------------------------------------------
        cos =
                coRepos.calculate_TB_PREV_MER26(
                        filter.getOrganization(), fromDate, toDate, false, true);
        calDisaggregates(filter.getToDate(), cos, 1);

        // ---------------------------------------------------------------------
        // denominator -> start prophylaxis in reporting period and within 6 month after ARV
        // initiation
        // ---------------------------------------------------------------------
        cos =
                coRepos.calculate_TB_PREV_MER26(
                        filter.getOrganization(), fromDate, toDate, true, false);
        calDisaggregates(filter.getToDate(), cos, 2);

        // ---------------------------------------------------------------------
        // denominator -> start prophylaxis in reporting period and after 6 months since ARV
        // initiation
        // ---------------------------------------------------------------------
        cos =
                coRepos.calculate_TB_PREV_MER26(
                        filter.getOrganization(), fromDate, toDate, false, false);
        calDisaggregates(filter.getToDate(), cos, 3);

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

        // figure out the quarter
        LocalDateTime midDate = filter.getFromDate().plusMonths(1);
        int calendarQuarter = midDate.get(IsoFields.QUARTER_OF_YEAR);

        // 4 will be quarter of the previous calendar year
        // and equals fiscal year quarter #1 of current
        // year.
        shouldBeIncluded = true;
        if (calendarQuarter != 1 && calendarQuarter != 3) {
            shouldBeIncluded = false;
            wbook.setSheetHidden(wbook.getSheetIndex(sheet), true);
            return sheet;
        }

        Timestamp fromDate = CommonUtils.toTimestamp(filter.getFromDate());
        Timestamp toDate = CommonUtils.toTimestamp(filter.getToDate());

        List<List<CaseOrg>> cos = new ArrayList<>();

        if (filter.getNumerator() != null && filter.getNumerator()) {
            // ---------------------------------------------------------------------
            // numerator -> start prophylaxis in reporting period and within 6 month after ARV
            // initiation, and completed prophylaxis
            // ---------------------------------------------------------------------
            List<CaseOrg> cos1 =
                    coRepos.calculate_TB_PREV_MER26(
                            filter.getOrganization(), fromDate, toDate, true, true);

            cos.add(cos1);
            // ---------------------------------------------------------------------
            // numerator -> start prophylaxis in reporting period and after 6 months since ARV
            // initiation, and completed prophylaxis
            // ---------------------------------------------------------------------
            cos1 =
                    coRepos.calculate_TB_PREV_MER26(
                            filter.getOrganization(), fromDate, toDate, false, true);
            cos.add(cos1);
        }
        // -------------------------------------------------
        // -> denominator
        // -------------------------------------------------
        if (filter.getNumerator() != null && !filter.getNumerator()) {
            // ---------------------------------------------------------------------
            // denominator -> start prophylaxis in reporting period and within 6 month after ARV
            // initiation
            // ---------------------------------------------------------------------
            List<CaseOrg> cos1 =
                    coRepos.calculate_TB_PREV_MER26(
                            filter.getOrganization(), fromDate, toDate, true, false);
            cos.add(cos1);

            // ---------------------------------------------------------------------
            // denominator -> start prophylaxis in reporting period and after 6 months since ARV
            // initiation
            // ---------------------------------------------------------------------
            cos1 =
                    coRepos.calculate_TB_PREV_MER26(
                            filter.getOrganization(), fromDate, toDate, false, false);
            cos.add(cos1);
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
        int indx = 0;
        for (List<CaseOrg> list : cos) {
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

                // Bắt đầu dự phòng lao trong vòng 6 tháng kể từ khi khởi liều ARV?
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (indx == 0) {
                    cell.setCellValue("x");
                }

                // Bắt đầu dự phòng lao sau 6 tháng kể từ khi khởi liều ARV?
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (indx == 1) {
                    cell.setCellValue("x");
                }
            }

            indx++;
        }

        return sheet;
    }

    @Override
    public Sheet updateResultSheet(Sheet sheet) {
        if (sheet == null || !shouldBeIncluded) {
            return null;
        }

        int row, col;

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {

                row = (i > 1) ? i + 36 : i + 35;
                col = j + 3;

                if (j < 6) {
                    continue;
                }

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

    /**
     * Calculate disaggregations
     *
     * @param reportingService
     * @param cos
     * @param row
     */
    private void calDisaggregates(LocalDateTime toDate, List<CaseOrg> cos, int row) {
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

                    merUtils.calculateIndicatorsByAgeRange(toDate, theCaseDto, indicators[row]);
                });
    }
}
