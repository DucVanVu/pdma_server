package org.pepfar.pdma.app.data.service.jpa;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.poi.ss.usermodel.*;
import org.pepfar.pdma.app.Constants;
import org.pepfar.pdma.app.data.domain.*;
import org.pepfar.pdma.app.data.dto.CaseDto;
import org.pepfar.pdma.app.data.dto.CaseReportFilterDto;
import org.pepfar.pdma.app.data.dto.LabTestDto;
import org.pepfar.pdma.app.data.repository.CaseOrgRepository;
import org.pepfar.pdma.app.data.repository.LabTestRepository;
import org.pepfar.pdma.app.data.service.ReportingWorker;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.ExcelUtils;
import org.pepfar.pdma.app.utils.MerReportingUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.beust.jcommander.internal.Lists;
import org.springframework.util.CollectionUtils;

@Service
public class _MerOpcTxPvlsWorker implements ReportingWorker<Boolean> {

    //	@Autowired
    //	private ApplicationContext context;

    @Autowired private MerReportingUtils merUtils;

    @Autowired private LabTestRepository labRepos;

    @Autowired private CaseOrgRepository coRepos;

    private CaseReportFilterDto filter;

    private AtomicInteger[][] indicators;

    private static final int ROWS = 4;

    private static final int COLS = 38;

    @Override
    public Boolean call() throws Exception {

        if (filter == null
                || !CommonUtils.isPositive(filter.getOrganization(), true)
                || filter.getFromDate() == null
                || filter.getToDate() == null) {
            return false;
        }

        //        List<Long> organizationIds = Lists.newArrayList(filter.getOrganization());
        //        LocalDateTime adjFromDate = filter.getFromDate().minusMonths(9);

        Timestamp fromDate = CommonUtils.toTimestamp(filter.getFromDate());
        Timestamp toDate = CommonUtils.toTimestamp(filter.getToDate());

        //        _MerOpcReportingServiceImpl reportingService =
        //                context.getBean(_MerOpcReportingServiceImpl.class);

        //        final List<String> RESULTS_BELOW_1000 =
        //                Lists.newArrayList(
        //                        Constants.VL_RESULT_UNDECTECTED,
        //                        Constants.VL_RESULT_UNDETECT_LT200,
        //                        Constants.VL_RESULT_200_LT1000);

        // -------------------------------------------------
        // -> numerator
        // -------------------------------------------------
        for (int i = 0; i < 2; i++) {
            List<LabTest> list =
                    labRepos.calculate_TX_PVLS_MER26(
                            filter.getOrganization(), fromDate, toDate, (i + 1), true);

            final int finalI = i;
            list.stream()
                    .map(LabTestDto::new)
                    .forEach(
                            e -> {
                                calculateIndicators(e.getTheCase(), finalI);
                            });
        }

        // -------------------------------------------------
        // -> denominator
        // -------------------------------------------------
        for (int i = 0; i < 2; i++) {
            List<LabTest> list =
                    labRepos.calculate_TX_PVLS_MER26(
                            filter.getOrganization(), fromDate, toDate, (i + 1), false);

            final int finalI = i + 2;
            list.stream()
                    .map(LabTestDto::new)
                    .forEach(
                            e -> {
                                calculateIndicators(e.getTheCase(), finalI);
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

        Timestamp fromDate = CommonUtils.toTimestamp(filter.getFromDate());
        Timestamp toDate = CommonUtils.toTimestamp(filter.getToDate());
        List<LabTest> labTests = new ArrayList<>();

        if (filter.getNumerator() != null && filter.getNumerator()) {
            for (int i = 0; i < 2; i++) {
                labTests =
                        labRepos.calculate_TX_PVLS_MER26(
                                filter.getOrganization(), fromDate, toDate, (i + 1), true);
            }
        }

        // -------------------------------------------------
        // -> denominator
        // -------------------------------------------------
        if (filter.getNumerator() != null && !filter.getNumerator()) {
            for (int i = 0; i < 2; i++) {
                labTests =
                        labRepos.calculate_TX_PVLS_MER26(
                                filter.getOrganization(), fromDate, toDate, (i + 1), false);
            }
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

        for (LabTest labTest : labTests) {
            Organization org = labTest.getOrganization();
            Case theCase = labTest.getTheCase();
            Person person = theCase.getPerson();

            CaseOrg co = null;
            List<CaseOrg> cos = coRepos.findByCaseAndOrg(theCase.getId(), org.getId());
            if (cos.size() > 0) {
                co = cos.get(0);
            }

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
            if (co != null && co.getPatientChartId() != null) {
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
            if (theCase != null && theCase.getArvStartDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getArvStartDate()));
            } else {
                cell.setCellValue("-");
            }

            // Ngày lấy mẫu xn tlvr
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(dateCellStyle);
            if (labTest != null && labTest.getSampleDate() != null) {
                cell.setCellValue(CommonUtils.fromLocalDateTime(labTest.getSampleDate()));
            } else {
                cell.setCellValue("-");
            }

            // Kết quả xet nghiệm
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellStyle(cellStyle);
            if (labTest != null
                    && (labTest.getResultNumber() != null || labTest.getResultText() != null)) {

                if (CommonUtils.isPositive(labTest.getResultNumber(), false)) {
                    cell.setCellValue(labTest.getResultNumber());
                } else if (labTest.getResultText() != null) {
                    cell.setCellValue(labTest.getResultText());
                }

            } else {
                cell.setCellValue("-");
            }

            // lý do xét nghiệm tlvr
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyle);
            if (labTest != null && labTest.getReasonForTesting() != null) {
                cell.setCellValue(labTest.getReasonForTesting().toString());
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

        int row, col;

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {

                row = (i > 1) ? i + 28 : i + 27;
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

    /**
     * Calculate the indicators
     *
     * @param reportingService
     * @param theCase
     * @param row
     */
    private void calculateIndicators(CaseDto theCase, int row) {
        // ---------------------------------------
        // Calculate dis-aggregation by risk group
        // ---------------------------------------
        merUtils.calculateIndicatorsByRiskgroup(theCase, indicators[row]);

        // ---------------------------------------
        // Calculate dis-aggregation by age and sex
        // ---------------------------------------
        merUtils.calculateIndicatorsByAgeRange(filter.getToDate(), theCase, indicators[row]);
    }
}
