package org.pepfar.pdma.app.data.service.jpa;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class _OpcMmdWorker implements ReportingWorker<Boolean> {

    @Autowired private CaseOrgRepository coRepos;

    private AtomicInteger[] indicators;

    private CaseReportFilterDto filter;

    private static final int ROWS = 3;

    @Override
    public Boolean call() throws Exception {
        if (filter == null
                || !CommonUtils.isPositive(filter.getOrganization(), true)
                || filter.getFromDate() == null
                || filter.getToDate() == null) {
            return false;
        }

        Timestamp toDate = CommonUtils.toTimestamp(filter.getToDate());

        // Eligible
        List<CaseOrg> cos =
                coRepos.findCases4MMDIndicators(filter.getOrganization(), 1, false, toDate);
        indicators[0] = new AtomicInteger(cos.size());

        // On MMD
        cos = coRepos.findCases4MMDIndicators(filter.getOrganization(), 2, false, toDate);
        indicators[1] = new AtomicInteger(cos.size());

        // On MMD - SHI drugs
        cos = coRepos.findCases4MMDIndicators(filter.getOrganization(), 2, true, toDate);
        indicators[2] = new AtomicInteger(cos.size());

        return true;
    }

    @Override
    public Sheet calcAndUpdateRawdata(Workbook wbook, Sheet sheet) {
        Timestamp toDate = CommonUtils.toTimestamp(filter.getToDate());

        // Eligible
        List<CaseOrg> cos =
                coRepos.findCases4MMDIndicators(filter.getOrganization(), 1, false, toDate);
        filloutSheet(wbook, sheet, cos, 0);

        // On MMD
        cos = coRepos.findCases4MMDIndicators(filter.getOrganization(), 2, false, toDate);
        filloutSheet(wbook, sheet, cos, 1);

        // On MMD - SHI drugs
        cos = coRepos.findCases4MMDIndicators(filter.getOrganization(), 2, true, toDate);
        filloutSheet(wbook, sheet, cos, 2);

        return sheet;
    }

    @Override
    public Sheet updateResultSheet(Sheet sheet) {
        if (sheet == null) {
            return null;
        }

        for (int i = 0; i < ROWS; i++) {
            ExcelUtils.writeInCell(sheet, 23 + i, 9, indicators[i].get());
        }

        return sheet;
    }

    @Override
    public void setFilter(CaseReportFilterDto filter) {
        this.indicators = new AtomicInteger[ROWS];

        for (int i = 0; i < ROWS; i++) {
            this.indicators[i] = new AtomicInteger(0);
        }

        this.filter = filter;
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

            // Kh??a ch??nh
            Cell cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(theCase.getId().toString());

            // T???nh - th??nh ph??? c???a c?? s???
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (org.getAddress() != null && org.getAddress().getProvince() != null) {
                cell.setCellValue(org.getAddress().getProvince().getName());
            } else {
                cell.setCellValue("-");
            }

            // C?? s??? ??i???u tr???
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (org != null) {
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

            int targetColIndex = colIndex + type;

            for (int i = 0; i < 3; i++) {
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(cellStyle);
                cell.setCellValue("");
            }

            cell = row.getCell(targetColIndex);
            cell.setCellValue("x");

            // Ng??y XN kh???ng ?????nh
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (theCase.getHivConfirmDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getHivConfirmDate()));
            } else {
                cell.setCellValue("-");
            }

            // Ng??y b???t ?????u ARV
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (theCase.getArvStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getArvStartDate()));
            } else {
                cell.setCellValue("-");
            }

            // Ng??y b???t ?????u ?????t ??i???u tr??? trong k??? b??o c??o
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (co.getStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(co.getStartDate()));
            } else {
                cell.setCellValue("-");
            }

            // Lo???i ????ng k?? t???i c?? s??? trong k??? b??o c??o
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (co.getEnrollmentType() != null) {
                cell.setCellValue(co.getEnrollmentType().toString());
            } else {
                cell.setCellValue("-");
            }

            // Tr???ng th??i ??i???u tr??? c???a b???nh nh??n t???i c?? s???
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            cell.setCellValue((co.getStatus() != null) ? co.getStatus().toString() : "-");

            // Ng??y thay ?????i t??nh tr???ng
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

        // Auto-filter
        if (rowIndex >= 2) {
            sheet.setAutoFilter(CellRangeAddress.valueOf("A2:P" + rowIndex));
        }
    }
}
