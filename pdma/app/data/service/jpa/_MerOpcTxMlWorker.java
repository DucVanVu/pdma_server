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
import org.pepfar.pdma.app.data.types.PatientStatus;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.ExcelUtils;
import org.pepfar.pdma.app.utils.MerReportingUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class _MerOpcTxMlWorker implements ReportingWorker<Boolean> {

    @Autowired private CaseOrgRepository coRepos;

    //    @Autowired
    //    private ApplicationContext context;

    @Autowired private MerReportingUtils merUtils;

    private CaseReportFilterDto filter;

    private AtomicInteger[][] indicators;

    private static final int ROWS = 6;

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
        // context.getBean(_MerOpcReportingServiceImpl.class);

        Timestamp fromDate = CommonUtils.toTimestamp(filter.getFromDate());
        Timestamp toDate = CommonUtils.toTimestamp(filter.getToDate());

        // Dead
        List<CaseOrg> cos =
                coRepos.calculate_TX_ML_DEAD_STOPPED_MER26(
                        filter.getOrganization(), fromDate, toDate, PatientStatus.DEAD.name());
        calcDisaggregation(cos, 0);

        // IIT of those who started ARV < 3 months
        cos = coRepos.calculate_TX_ML_IIT_MER26(filter.getOrganization(), fromDate, toDate, 1);
        calcDisaggregation(cos, 1);

        // IIT of those who started ARV 3 - 5 months
        cos = coRepos.calculate_TX_ML_IIT_MER26(filter.getOrganization(), fromDate, toDate, 2);
        calcDisaggregation(cos, 2);

        // IIT of those who started ARV >= 6 months
        cos = coRepos.calculate_TX_ML_IIT_MER26(filter.getOrganization(), fromDate, toDate, 3);
        calcDisaggregation(cos, 3);

        // Transferred out
        List<CaseOrg> cos1 =
                coRepos.calculate_TX_ML_TRANSOUT_1_MER26(
                        filter.getOrganization(), fromDate, toDate);
        List<CaseOrg> cos2 =
                coRepos.calculate_TX_ML_TRANSOUT_2_MER26(
                        filter.getOrganization(), fromDate, toDate);
        cos = new ArrayList<>();

        if (cos1.size() > 0) {
            cos.addAll(cos1);
        }

        if (cos2.size() > 0) {
            cos.addAll(cos2);
        }

        calcDisaggregation(cos, 4);

        // Stopped/refused treatment
        cos =
                coRepos.calculate_TX_ML_DEAD_STOPPED_MER26(
                        filter.getOrganization(), fromDate, toDate, PatientStatus.LTFU.name());
        calcDisaggregation(cos, 5);

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

        String[] disagString = {
            "Tử vong",
            "Gián đoạn khi đã điều trị < 3 tháng",
            "Gián đoạn khi đã điều trị từ 3 - <6 tháng",
            "Gián đoạn khi đã điều trị >= 6 tháng",
            "Chuyển đi trong kỳ",
            "Chuyển đi ở kỳ trước, nhưng còn thuốc đến kỳ này",
            "Từ chối (dừng) điều trị"
        };

        List<List<CaseOrg>> lists = new ArrayList<>();

        // DEAD
        List<CaseOrg> cos1 =
                coRepos.calculate_TX_ML_DEAD_STOPPED_MER26(
                        filter.getOrganization(), fromDate, toDate, PatientStatus.DEAD.name());
        lists.add(cos1);

        // IIT of those who started ARV < 3 months
        cos1 = coRepos.calculate_TX_ML_IIT_MER26(filter.getOrganization(), fromDate, toDate, 1);
        lists.add(cos1);

        // IIT of those who started ARV 3 - 5 months
        cos1 = coRepos.calculate_TX_ML_IIT_MER26(filter.getOrganization(), fromDate, toDate, 2);
        lists.add(cos1);

        // IIT of those who started ARV >= 6 months
        cos1 = coRepos.calculate_TX_ML_IIT_MER26(filter.getOrganization(), fromDate, toDate, 3);
        lists.add(cos1);

        // Transferred out
        // --> in reporting period
        cos1 = coRepos.calculate_TX_ML_TRANSOUT_1_MER26(filter.getOrganization(), fromDate, toDate);
        lists.add(cos1);

        // --> in last reporting period but was still counted as active
        cos1 = coRepos.calculate_TX_ML_TRANSOUT_2_MER26(filter.getOrganization(), fromDate, toDate);
        lists.add(cos1);

        // Stopped/refused treatment
        cos1 =
                coRepos.calculate_TX_ML_DEAD_STOPPED_MER26(
                        filter.getOrganization(), fromDate, toDate, PatientStatus.LTFU.name());
        lists.add(cos1);

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

        for (List<CaseOrg> cos : lists) {
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
                if (theCase != null && theCase.getHivScreenDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getHivScreenDate()));
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

                // Ngày kết thúc tại co so dieu tri
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                if (co != null && co.getEndDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(co.getEndDate()));
                } else {
                    cell.setCellValue("-");
                }

                // Phân loại
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(dateCellStyle);
                cell.setCellValue(disagString[indx]);

                // ghi chú
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                if (co != null && co.getEndingReason() != null) {
                    cell.setCellValue(co.getEndingReason());
                } else {
                    cell.setCellValue("-");
                }
            }

            indx++;
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
                row = i + 19;
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
     * Calculate dis-aggregation
     *
     * @param reportingService
     * @param cos
     * @param row
     */
    private void calcDisaggregation(List<CaseOrg> cos, int row) {

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
