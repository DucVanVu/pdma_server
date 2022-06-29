package org.pepfar.pdma.app.data.service.jpa;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.dto.CaseReportFilterDto;
import org.pepfar.pdma.app.data.repository.OrganizationRepository;
import org.pepfar.pdma.app.data.service.ReportingWorker;
import org.pepfar.pdma.app.data.service._ReportingService;
import org.pepfar.pdma.app.data.types.Permission;
import org.pepfar.pdma.app.data.types.ReportType;
import org.pepfar.pdma.app.utils.AuthorizationUtils;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.ExcelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("_MerOpcReportingServiceImpl")
public class _MerOpcReportingServiceImpl implements _ReportingService {

    @Autowired private AuthorizationUtils authUtils;

    @Autowired private ApplicationContext context;

    @Autowired private OrganizationRepository orgRepos;

    private final Workbook blankBook = new XSSFWorkbook();

    @Override
    @Transactional(readOnly = true)
    public Workbook exportReport(CaseReportFilterDto filter) {

        blankBook.createSheet();

        if (filter == null
                || filter.getFromDate() == null
                || filter.getToDate() == null
                || filter.getReportType() == null
                || filter.getReportType() != ReportType.MER_OPC) {
            return blankBook;
        }

        LocalDateTime adjFromDate = CommonUtils.dateStart(filter.getFromDate());
        LocalDateTime adjToDate = CommonUtils.dateEnd(filter.getToDate());

        // Set adjusted date and time
        filter.setFromDate(adjFromDate);
        filter.setToDate(adjToDate);

        // Only allow generating report per one facility
        List<Long> grantedOrgIds = authUtils.getGrantedOrgIds(Permission.READ_ACCESS);
        if (!CommonUtils.isPositive(filter.getOrganization(), true)) {
            return blankBook;
        } else if (grantedOrgIds.size() <= 0
                || !grantedOrgIds.contains(filter.getOrganization().longValue())) {
            return blankBook;
        }

        if (CommonUtils.isTrue(filter.getRawDataOnly())) {
            return exportLinelistReport(filter);
        } else {
            return exportIndicatorReport(filter);
        }
    }

    /**
     * Export line list report
     *
     * @param filter
     * @return
     */
    private Workbook exportLinelistReport(CaseReportFilterDto filter) {
        // Read the workbook template
        Workbook wbook = null;
        try (InputStream template =
                context.getResource("classpath:templates/opc_mer_2_6_rawdata-template.xlsx")
                        .getInputStream()) {
            wbook = new XSSFWorkbook(template);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (wbook == null) {
            return blankBook;
        }

        int i = 0;
        for (Class<?> worker : WORKERS) {
            int sheetIndex = SHEET_INDEX[i++];
            Sheet sheet = wbook.getSheetAt(sheetIndex);

            ReportingWorker<Boolean> wrkInstance =
                    (ReportingWorker<Boolean>) context.getBean(worker);

            if (wrkInstance != null) {
                filter.setNumerator(true);
                wrkInstance.setFilter(filter);
                wrkInstance.calcAndUpdateRawdata(wbook, sheet);

                if (worker.equals(_MerOpcTxPvlsWorker.class)
                        || worker.equals(_MerOpcTbPrevWorker.class)
                        || worker.equals(_MerOpcTxTbWorker.class)) {

                    sheetIndex = SHEET_INDEX[i++];
                    sheet = wbook.getSheetAt(sheetIndex);
                    filter.setNumerator(false);
                    wrkInstance.setFilter(filter);
                    wrkInstance.calcAndUpdateRawdata(wbook, sheet);
                }
            }
        }

        // return generateReport(wbook, filter);
        return wbook;
    }

    /**
     * Export indicator report
     *
     * @param filter
     * @return
     */
    private Workbook exportIndicatorReport(CaseReportFilterDto filter) {
        // Which quarter and fiscal year are these?
        LocalDateTime midDate = filter.getFromDate().plusMonths(1);
        int year = midDate.getYear();
        int quarter = midDate.get(IsoFields.QUARTER_OF_YEAR);

        if (quarter == 4) {
            quarter = 1;
            year += 1;
        } else {
            quarter += 1;
        }

        String templateFile =
                quarter % 2 == 0 ? "opc_mer_2_6-template.xlsx" : "opc_mer_2_6-template_2.xlsx";

        // Read the workbook template
        Workbook wbook = null;
        try (InputStream template =
                context.getResource("classpath:templates/" + templateFile).getInputStream()) {
            wbook = new XSSFWorkbook(template);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (wbook == null || filter.getAlgorithm() == null) {
            return blankBook;
        }

        Sheet sheet = wbook.getSheetAt(0);

        // Fill out Administrative information
        Organization organization = orgRepos.findOne(filter.getOrganization());
        if (organization != null && !CommonUtils.isEmpty(organization.getName())) {
            ExcelUtils.writeInCell(sheet, 2, 7, organization.getName());

            if (organization.getAddress() != null
                    && organization.getAddress().getProvince() != null) {
                ExcelUtils.writeInCell(
                        sheet, 2, 2, organization.getAddress().getProvince().getName());
            }
        }

        // Fill out Period information
        String yearPlusDetails = year + " (";
        yearPlusDetails += filter.getFromDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        yearPlusDetails += " - ";
        yearPlusDetails += filter.getToDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        yearPlusDetails += ")";

        ExcelUtils.writeInCell(sheet, 2, 17, quarter);
        ExcelUtils.writeInCell(sheet, 2, 19, yearPlusDetails);

        return generateReport(wbook, filter);
    }

    /**
     * Generate the report based on the inputs
     *
     * @param filter
     * @return
     */
    private Workbook generateReport(Workbook wbook, CaseReportFilterDto filter) {

        ExecutorService executor = Executors.newFixedThreadPool(PARALLEL_THREAD_COUNT);

        Sheet sheet = wbook.getSheetAt(0);

        List<Future<?>> submissions = new ArrayList<>();
        List<ReportingWorker<?>> workers = new ArrayList<>();

        for (Class<?> aClass : WORKERS) {
            try {
                ReportingWorker<?> worker = (ReportingWorker<?>) context.getBean(aClass);
                worker.setFilter(filter);

                Future<?> submission = executor.submit(worker);

                workers.add(worker);
                submissions.add(submission);
            } catch (SecurityException | IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        // await for callables to complete (blocking)
        for (Future<?> submission : submissions) {
            try {
                submission.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        // fill out results
        // ----------------
        // this cannot be grouped in the above loop to avoid write conflict on the same
        // work sheet
        for (ReportingWorker<?> worker : workers) {
            worker.updateResultSheet(sheet);
        }

        // Evaluate the formulas
        XSSFFormulaEvaluator.evaluateAllFormulaCells(wbook);

        // DONE. Now shutdown the executor...
        executor.shutdown();

        try {
            if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

        return wbook;
    }

    /** constants */

    // for running callable workers in parallel
    private static final int PARALLEL_THREAD_COUNT = 7;

    // Workers
    private static final Class<?>[] WORKERS = {
        _MerOpcTxNewWorker.class,
        _MerOpcTxCurrWorker.class,
        _MerOpcTxRttWorker.class,
        _MerOpcTxMlWorker.class,
        _MerOpcTxPvlsWorker.class,
        _MerOpcTbPrevWorker.class,
        _MerOpcTxTbWorker.class
    };

    private static final Integer[] SHEET_INDEX = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
}
