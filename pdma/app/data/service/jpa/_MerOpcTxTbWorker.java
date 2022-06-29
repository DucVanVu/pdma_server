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
public class _MerOpcTxTbWorker implements ReportingWorker<Boolean> {

    //    @Autowired private ApplicationContext context;

    @Autowired private MerReportingUtils merUtils;

    @Autowired private CaseOrgRepository coRepos;

    private CaseReportFilterDto filter;

    private AtomicInteger[][] indicators;

    private static final int ROWS = 6;

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

        //        _MerOpcReportingServiceImpl reportingService =
        //                context.getBean(_MerOpcReportingServiceImpl.class);

        Timestamp fromDate = CommonUtils.toTimestamp(filter.getFromDate());
        Timestamp toDate = CommonUtils.toTimestamp(filter.getToDate());

        // numerator - diag and tx started for art during reporting period
        List<CaseOrg> cos =
                coRepos.calculate_TX_TB_Numerator_MER26(
                        filter.getOrganization(), fromDate, toDate, true);
        calDisaggregates(cos, 0);

        // nominator - diag and tx started for art before reporting period
        cos =
                coRepos.calculate_TX_TB_Numerator_MER26(
                        filter.getOrganization(), fromDate, toDate, false);
        calDisaggregates(cos, 1);

        // denominator - screened pos, start arv during reporting period
        cos =
                coRepos.calculate_TX_TB_Denominator_MER26(
                        filter.getOrganization(), fromDate, toDate, true, true);
        calDisaggregates(cos, 2);

        // denominator - screened neg, start arv during reporting period
        cos =
                coRepos.calculate_TX_TB_Denominator_MER26(
                        filter.getOrganization(), fromDate, toDate, false, true);
        calDisaggregates(cos, 3);

        // denominator - screened pos, start arv before reporting period
        cos =
                coRepos.calculate_TX_TB_Denominator_MER26(
                        filter.getOrganization(), fromDate, toDate, true, false);
        calDisaggregates(cos, 4);

        // denominator - screened neg, start arv before reporting period
        cos =
                coRepos.calculate_TX_TB_Denominator_MER26(
                        filter.getOrganization(), fromDate, toDate, false, false);
        calDisaggregates(cos, 5);

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

        String[] disaggStr = new String[0];
        List<List<CaseOrg>> lists = new ArrayList<>();

        if (filter.getNumerator() != null && filter.getNumerator()) {
            disaggStr = new String[] {""};

            // numerator - diag and tx started for art during reporting period
            List<CaseOrg> cos1 =
                    coRepos.calculate_TX_TB_Numerator_MER26(
                            filter.getOrganization(), fromDate, toDate, true);
            lists.add(cos1);

            // nominator - diag and tx started for art before reporting period
            cos1 =
                    coRepos.calculate_TX_TB_Numerator_MER26(
                            filter.getOrganization(), fromDate, toDate, false);
            lists.add(cos1);
        }
        // -------------------------------------------------
        // -> denominator
        // -------------------------------------------------
        if (filter.getNumerator() != null && !filter.getNumerator()) {
            disaggStr =
                    new String[] {
                        "Bệnh nhân bắt đầu điều trị ARV trong kỳ báo cáo và có kết quả sàng lọc Lao Dương tính",
                        "Bệnh nhân bắt đầu điều trị ARV trong kỳ báo cáo và có kết quả sàng lọc Lao Âm tính",
                        "Bệnh nhân bắt đầu điều trị ARV trước kỳ báo cáo và có kết quả sàng lọc Lao Dương tính",
                        "Bệnh nhân bắt đầu điều trị ARV trước kỳ báo cáo và có kết quả sàng lọc Lao Âm tính"
                    };

            // denominator - screened pos, start arv during reporting period
            List<CaseOrg> cos1 =
                    coRepos.calculate_TX_TB_Denominator_MER26(
                            filter.getOrganization(), fromDate, toDate, true, true);
            lists.add(cos1);

            // denominator - screened neg, start arv during reporting period
            cos1 =
                    coRepos.calculate_TX_TB_Denominator_MER26(
                            filter.getOrganization(), fromDate, toDate, false, true);
            lists.add(cos1);

            // denominator - screened pos, start arv before reporting period
            cos1 =
                    coRepos.calculate_TX_TB_Denominator_MER26(
                            filter.getOrganization(), fromDate, toDate, true, false);
            lists.add(cos1);

            // denominator - screened neg, start arv before reporting period
            cos1 =
                    coRepos.calculate_TX_TB_Denominator_MER26(
                            filter.getOrganization(), fromDate, toDate, false, false);
            lists.add(cos1);
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

        for (List<CaseOrg> list : lists) {
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

                if (CommonUtils.isTrue(filter.getNumerator())) {
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    if (indx == 0) {
                        cell.setCellValue("x");
                    }

                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    if (indx == 1) {
                        cell.setCellValue("x");
                    }
                } else {
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    if (disaggStr.length > indx) {
                        cell.setCellValue(disaggStr[indx]);
                    }
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

                row = (i > 1) ? i + 43 : i + 42;
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
    private void calDisaggregates(List<CaseOrg> cos, int row) {
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

                    merUtils.calculateIndicatorsByAgeRange(
                            filter.getToDate(), theCaseDto, indicators[row]);
                });
    }
}
