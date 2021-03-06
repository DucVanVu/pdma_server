package org.pepfar.pdma.app.data.service.jpa;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
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

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class _MerOpcTxNewWorker implements ReportingWorker<Boolean> {

    @Autowired private CaseOrgRepository coRepos;

    @Autowired private MerReportingUtils merUtils;

    //    @Autowired private ApplicationContext context;

    private AtomicInteger[] indicators;

    private CaseReportFilterDto filter;

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

        //        // to use some common functions in this reporting service bean
        //        _MerOpcReportingServiceImpl reportingService =
        //                context.getBean(_MerOpcReportingServiceImpl.class);

        Timestamp fromDate = CommonUtils.toTimestamp(filter.getFromDate());
        Timestamp toDate = CommonUtils.toTimestamp(filter.getToDate());

        List<CaseOrg> cos =
                coRepos.calculate_TX_NEW_MER26(filter.getOrganization(), fromDate, toDate);

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
                    merUtils.calculateIndicatorsByRiskgroup(theCaseDto, indicators);

                    // calculate by age range
                    merUtils.calculateIndicatorsByAgeRange(
                            filter.getToDate(), theCaseDto, indicators);
                });

        return null;
    }

    @Override
    public Sheet calcAndUpdateRawdata(Workbook wbook, Sheet sheet) {
        if (filter == null
                || !CommonUtils.isPositive(filter.getOrganization(), true)
                || filter.getFromDate() == null
                || filter.getToDate() == null) {
            return sheet;
        }
        Timestamp fromDate = CommonUtils.toTimestamp(filter.getFromDate());
        Timestamp toDate = CommonUtils.toTimestamp(filter.getToDate());
        List<CaseOrg> cos =
                coRepos.calculate_TX_NEW_MER26(filter.getOrganization(), fromDate, toDate);

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

            // Kh??a ch??nh
            Cell cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(theCase.getId().toString());

            // T??n c?? s??? ??i???u tr???
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (!CommonUtils.isEmpty(org.getName(), true)) {
                cell.setCellValue(org.getName());
            } else {
                cell.setCellValue("-");
            }

            // M?? b???nh ??n t???i c?? s??? ??i???u tr???
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (co.getPatientChartId() != null) {
                cell.setCellValue(co.getPatientChartId());
            } else {
                cell.setCellValue("-");
            }

            // H??? t??n b???nh nh??n
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (person == null || CommonUtils.isEmpty(person.getFullname())) {
                cell.setCellValue("-");
            } else {
                cell.setCellValue(filter.isConfidentialRequired() ? "-" : person.getFullname());
            }

            // Gi???i t??nh
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (person == null || person.getGender() == null) {
                cell.setCellValue("-");
            } else {
                cell.setCellValue(person.getGender().toString());
            }

            // Ng??y sinh
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (person != null && person.getDob() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(person.getDob()));
            } else {
                cell.setCellValue("-");
            }

            // Ng??y XNKDHIV
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (theCase != null && theCase.getHivConfirmDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getHivConfirmDate()));
            } else {
                cell.setCellValue("-");
            }

            // Ng??y b???t ?????u arv
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (co != null && co.getArvStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(co.getArvStartDate()));
            } else {
                cell.setCellValue("-");
            }

            // Ng??y ????ng k?? t???i co so dieu tri
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (co != null && co.getStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(co.getStartDate()));
            } else {
                cell.setCellValue("-");
            }

            // loai dang ki
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (co != null && co.getEnrollmentType() != null) {
                cell.setCellValue(co.getEnrollmentType().toString());
            } else {
                cell.setCellValue("-");
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
            ExcelUtils.writeInCell(sheet, 9, 3 + i, indicators[i].get());
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
