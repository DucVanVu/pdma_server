package org.pepfar.pdma.app.data.service.jpa;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.collect.Lists;
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
import org.pepfar.pdma.app.data.types.EnrollmentType;
import org.pepfar.pdma.app.data.types.PatientStatus;
import org.pepfar.pdma.app.data.types.ReportingAlgorithm;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.ExcelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * Tính số bệnh nhân điều trị ARV cuối kỳ báo cáo trước
 *
 * @author bizic
 */
@Service
public class _OpcTxCurrLastPeriodWorker implements ReportingWorker<Boolean> {

    @Autowired private ApplicationContext context;

    @Autowired private CaseOrgRepository coRepos;

    private CaseReportFilterDto filter;

    private AtomicInteger[] indicators;

    private static final int COLS = 5;

    @Override
    public Boolean call() throws Exception {

        if (filter == null
                || !CommonUtils.isPositive(filter.getOrganization(), true)
                || filter.getFromDate() == null
                || filter.getToDate() == null
                || filter.getAlgorithm() == null) {
            return false;
        }

        Timestamp cutpoint = CommonUtils.toTimestamp(filter.getFromDate().plusMinutes(-1));

        _OPCReportingServiceImpl reportingService = context.getBean(_OPCReportingServiceImpl.class);
        List<CaseOrg> caseOrgs = null;

        // Tìm số bệnh nhân điều trị ARV cuối kỳ báo cáo trước, về cơ bản thì cách tính
        // báo cáo sẽ không ảnh hưởng đến số này kể cả ở tháng hiện tại, tuy nhiên vẫn
        // cứ kiểm tra cách tính để phòng trường hợp bệnh nhân chuyển đi được cấp nhiều
        // hơn 1 tháng thuốc

        if (filter.getAlgorithm() == ReportingAlgorithm.CIRCULAR_03) {
            caseOrgs =
                    coRepos.findPartiallyActivePatients(filter.getOrganization(), cutpoint, false);
        } else {
            // PEPFAR algorithm
            LocalDateTime prevMonthBegin = filter.getFromDate().minusDays(15);
            prevMonthBegin = prevMonthBegin.withDayOfMonth(1);

            Timestamp tsPrevMonthBegin = CommonUtils.toTimestamp(prevMonthBegin);

            caseOrgs =
                    coRepos.findPartiallyActivePatients(filter.getOrganization(), cutpoint, false);
            List<CaseOrg> caseOrgs2 =
                    coRepos.findTransferredOutPendingPatients(
                            filter.getOrganization(), tsPrevMonthBegin, cutpoint, false);

            if (caseOrgs2.size() > 0) {
                caseOrgs.addAll(caseOrgs2);
            }
        }

        int size = caseOrgs.size();
        AtomicLong[] caseIds = new AtomicLong[size];

        AtomicInteger indx = new AtomicInteger(0);

        caseOrgs.parallelStream()
                .forEach(
                        co -> {
                            Case theCase = co.getTheCase();
                            Person person = theCase.getPerson();

                            // disaggregate by age and gender
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
            caseOrgs = coRepos.findCaseOrgsByRegimenLine(caseIdList, filter.getOrganization(), cutpoint, 2);
            indicators[4] = new AtomicInteger(caseOrgs.size());
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

        Timestamp cutpoint = CommonUtils.toTimestamp(filter.getFromDate().plusMinutes(-1));

        List<CaseOrg> cos;

        if (filter.getAlgorithm() == ReportingAlgorithm.CIRCULAR_03) {
            cos = coRepos.findPartiallyActivePatients(filter.getOrganization(), cutpoint, false);
        } else {
            // PEPFAR algorithm
            LocalDateTime prevMonthBegin = filter.getFromDate().minusDays(15);
            prevMonthBegin = prevMonthBegin.withDayOfMonth(1);

            Timestamp tsPrevMonthBegin = CommonUtils.toTimestamp(prevMonthBegin);

            cos = coRepos.findPartiallyActivePatients(filter.getOrganization(), cutpoint, false);
            List<CaseOrg> caseOrgs2 =
                    coRepos.findTransferredOutPendingPatients(
                            filter.getOrganization(), tsPrevMonthBegin, cutpoint, false);

            if (caseOrgs2.size() > 0) {
                cos.addAll(caseOrgs2);
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

        // Auto-filter
        if (rowIndex >= 2) {
            sheet.setAutoFilter(CellRangeAddress.valueOf("A2:N" + rowIndex));
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
                ExcelUtils.writeInCell(sheet, 7, i + 4, indicators[i].get());
            } else {
                ExcelUtils.writeInCell(sheet, 7, i + 3, indicators[i].get());
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
