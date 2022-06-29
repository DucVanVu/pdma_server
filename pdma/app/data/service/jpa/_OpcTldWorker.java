package org.pepfar.pdma.app.data.service.jpa;

import com.beust.jcommander.internal.Lists;
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
import org.pepfar.pdma.app.data.types.ReportingAlgorithm;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.ExcelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
public class _OpcTldWorker implements ReportingWorker<Boolean> {

    @Autowired private CaseOrgRepository coRepos;

    private Integer[] indicators;

    private CaseReportFilterDto filter;

    private static final int rows = 3;

    @Override
    public Boolean call() throws Exception {
        if (filter == null
                || !CommonUtils.isPositive(filter.getOrganization(), true)
                || filter.getFromDate() == null
                || filter.getToDate() == null) {
            return false;
        }

        Timestamp fromDate = CommonUtils.toTimestamp(filter.getFromDate());
        Timestamp toDate = CommonUtils.toTimestamp(filter.getToDate());

        // Newly enrolled patients using TLD regimen
        List<CaseOrg> cos =
                coRepos.findNewCasesUsingTLD(filter.getOrganization(), fromDate, toDate);
        indicators[0] = cos.size();

        // Patients transitioning to TLD
        cos = coRepos.findCasesTransition2TLD(filter.getOrganization(), fromDate, toDate);
        indicators[1] = cos.size();

        // Patients receiving TLD at the end of the reporting period
        if (filter.getAlgorithm() == ReportingAlgorithm.PEPFAR) {
            List<CaseOrg> cos1 =
                    coRepos.findCasesOnTLDAtCutpoint_MER(
                            filter.getOrganization(), false, fromDate, toDate);
            List<CaseOrg> cos2 =
                    coRepos.findCasesOnTLDAtCutpoint_MER_TransedOutPendingEnrollment(
                            filter.getOrganization(), fromDate, toDate);

            indicators[2] = cos1.size() + cos2.size();
        } else {
            cos = coRepos.findCasesOnTLDAtCutpoint_C03(filter.getOrganization(), toDate);
            indicators[2] = cos.size();
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

        Timestamp fromDate = CommonUtils.toTimestamp(filter.getFromDate());
        Timestamp toDate = CommonUtils.toTimestamp(filter.getToDate());

        // Newly enrolled patients using TLD regimen
        List<CaseOrg> cos =
                coRepos.findNewCasesUsingTLD(filter.getOrganization(), fromDate, toDate);
        filloutSheet(wbook, sheet, cos, 0);

        // Patients transitioning to TLD
        cos = coRepos.findCasesTransition2TLD(filter.getOrganization(), fromDate, toDate);
        filloutSheet(wbook, sheet, cos, 1);

        // Patients receiving TLD at the end of the reporting period
        if (filter.getAlgorithm() == ReportingAlgorithm.PEPFAR) {
            List<CaseOrg> cos1 =
                    coRepos.findCasesOnTLDAtCutpoint_MER(
                            filter.getOrganization(), false, fromDate, toDate);
            List<CaseOrg> cos2 =
                    coRepos.findCasesOnTLDAtCutpoint_MER_TransedOutPendingEnrollment(
                            filter.getOrganization(), fromDate, toDate);

            cos = new ArrayList<>();
            cos.addAll(cos1);
            cos.addAll(cos2);

            filloutSheet(wbook, sheet, cos, 2);
        } else {
            cos = coRepos.findCasesOnTLDAtCutpoint_C03(filter.getOrganization(), toDate);
            filloutSheet(wbook, sheet, cos, 2);
        }

        return sheet;
    }

    @Override
    public Sheet updateResultSheet(Sheet sheet) {
        if (sheet == null) {
            return null;
        }

        for (int i = 0; i < rows; i++) {
            ExcelUtils.writeInCell(sheet, 27 + i, 9, indicators[i]);
        }

        return sheet;
    }

    @Override
    public void setFilter(CaseReportFilterDto filter) {
        this.indicators = new Integer[rows];

        for (int i = 0; i < rows; i++) {
            this.indicators[i] = 0;
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

            for (int i = 0; i < 3; i++) {
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(cellStyle);
                cell.setCellValue("");
            }

            cell = row.getCell(targetColIndex);
            cell.setCellValue("x");

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

        // Auto-filter
        if (rowIndex >= 2) {
            sheet.setAutoFilter(CellRangeAddress.valueOf("A2:P" + rowIndex));
        }
    }
}
