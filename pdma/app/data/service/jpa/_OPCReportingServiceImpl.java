package org.pepfar.pdma.app.data.service.jpa;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.beust.jcommander.internal.Maps;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.dto.CaseDto;
import org.pepfar.pdma.app.data.dto.CaseReportFilterDto;
import org.pepfar.pdma.app.data.dto.PersonDto;
import org.pepfar.pdma.app.data.repository.OrganizationRepository;
import org.pepfar.pdma.app.data.repository.RiskInterviewRepository;
import org.pepfar.pdma.app.data.service.ReportingWorker;
import org.pepfar.pdma.app.data.service._ReportingService;
import org.pepfar.pdma.app.data.types.Gender;
import org.pepfar.pdma.app.data.types.Permission;
import org.pepfar.pdma.app.data.types.ReportType;
import org.pepfar.pdma.app.utils.AuthorizationUtils;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.ExcelUtils;
import org.pepfar.pdma.app.utils.RiskGroupUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("_OpcReportingServiceImpl")
public class _OPCReportingServiceImpl implements _ReportingService {

    @Autowired private AuthorizationUtils authUtils;

    @Autowired private ApplicationContext context;

    @Autowired private RiskInterviewRepository riskRepos;

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
                || filter.getReportType() != ReportType.OPC_REPORT_MONTHLY) {
            return blankBook;
        }

        LocalDateTime adjFromDate = CommonUtils.dateStart(filter.getFromDate());
        LocalDateTime adjToDate = CommonUtils.dateEnd(filter.getToDate());

        LocalDate adjToDateLD = adjToDate.toLocalDate();
        int monthLength = adjToDateLD.getMonth().length(adjToDateLD.isLeapYear());

        if (adjFromDate.getMonthValue() != adjToDate.getMonthValue()
                || adjFromDate.getYear() != adjToDate.getYear()
                || adjFromDate.getDayOfMonth() != 1
                || adjToDate.getDayOfMonth() != monthLength) {
            return blankBook;
        }

        System.out.println(adjFromDate);
        System.out.println(adjToDate);

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
        }

        return exportIndicatorReport(filter);
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
                context.getResource("classpath:templates/opc-monthly-report-linelist-template.xlsx")
                        .getInputStream()) {
            wbook = new XSSFWorkbook(template);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (wbook == null || filter.getAlgorithm() == null) {
            return blankBook;
        }

        int i = 0;
        for (Class<?> worker : WORKERS) {
            int sheetIndex = SHEET_INDEX[i++];
            Sheet sheet = wbook.getSheetAt(sheetIndex);

            ReportingWorker<Boolean> wrkInstance =
                    (ReportingWorker<Boolean>) context.getBean(worker);
            if (wrkInstance != null) {
                wrkInstance.setFilter(filter);
                wrkInstance.calcAndUpdateRawdata(wbook, sheet);
            }
        }

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
        LocalDateTime midDate = filter.getFromDate().plusDays(15);
        int year = midDate.getYear();
        int month = midDate.getMonthValue();

        // Read the workbook template
        Workbook wbook = null;
        try (InputStream template =
                context.getResource("classpath:templates/opc-monthly-report-template-2.xlsx")
                        .getInputStream()) {
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
            ExcelUtils.writeInCell(sheet, 1, 1, "Cơ sở điều trị: " + organization.getName());

            if (organization.getAddress() != null
                    && organization.getAddress().getProvince() != null) {
                String s = "BQLTDA Tỉnh/Thành phố: ";
                s += organization.getAddress().getProvince().getName();

                ExcelUtils.writeInCell(sheet, 0, 1, s);
            }
        }

        // Fill out Period information
        String s = "Tháng " + month + " năm " + year;
        ExcelUtils.writeInCell(sheet, 3, 1, s);

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

        for (int i = 0; i < WORKERS.length; i++) {
            try {
                ReportingWorker<?> worker = (ReportingWorker<?>) context.getBean(WORKERS[i]);
                worker.setFilter(filter);

                Future<?> submission = executor.submit(worker);

                workers.add(worker);
                submissions.add(submission);
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        // await for callables to complete (blocking)
        for (int i = 0; i < submissions.size(); i++) {
            try {
                submissions.get(i).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        // fill out results
        // ----------------
        // this cannot be grouped in the above loop to avoid write conflict on the same
        // work sheet
        for (int i = 0; i < workers.size(); i++) {
            workers.get(i).updateResultSheet(sheet);
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

    /**
     * Calculate indicators by riskgroup
     *
     * @param theCase
     * @param arr
     */
    public void calculateIndicatorsByRiskgroup(CaseDto theCase, AtomicInteger[] arr) {
        List<String> codes = riskRepos.findRiskCodesInMostRecentInterview(theCase.getId());
        String riskCode = RiskGroupUtils.getPrioritizedRiskCode(codes);

        if (riskCode == null) {
            // other risk
            arr[5].incrementAndGet();
        } else {
            // one of 6 groups
            switch (riskCode) {
                case RiskGroupUtils.RISK_PWID:
                    arr[0].incrementAndGet();
                    break;

                case RiskGroupUtils.RISK_MSM:
                    arr[1].incrementAndGet();
                    break;

                case RiskGroupUtils.RISK_TG:
                    arr[2].incrementAndGet();
                    break;

                case RiskGroupUtils.RISK_SW:
                    arr[3].incrementAndGet();
                    break;

                case RiskGroupUtils.RISK_PRISONER:
                    arr[4].incrementAndGet();
                    break;

                default:
                    arr[5].incrementAndGet();
                    break;
            }
        }
    }

    /**
     * Calculate indicators by age range
     *
     * @param theCase
     * @param arr
     */
    public AtomicInteger[] calculateIndicatorsByAgeRange(
            LocalDateTime cutpoint, CaseDto theCase, AtomicInteger[] arr) {
        PersonDto person = theCase.getPerson();
        LocalDateTime dob = person.getDob();
        Gender gender = person.getGender();

        if (dob == null || gender == null) {
            return arr;
        }

        long age = CommonUtils.dateDiff(ChronoUnit.YEARS, dob, cutpoint);
        int i = -1;

        if (age < 1) {
            switch (gender) {
                case MALE:
                    i = 20;
                    break;
                case FEMALE:
                    i = 7;
                    break;
                default:
                    i = 7; // to avoid missing patients
                    break;
            }
        } else {
            for (int k = 0; k < 11; k++) {
                int lbound = 1;
                int ubound = 4;

                if (k > 0) {
                    lbound = k * 5;

                    if (k < 10) {
                        ubound = lbound + 4;
                    } else {
                        ubound = Integer.MAX_VALUE;
                    }
                }

                if (age >= lbound && age <= ubound) {
                    switch (gender) {
                        case MALE:
                            i = k + 21;
                            break;
                        case FEMALE:
                            i = k + 8;
                            break;
                        default:
                            i = k + 8; // to avoid missing patients
                            break;
                    }
                }
            }
        }

        if (i >= 0) {
            arr[i].incrementAndGet();
        }

        return arr;
    }

    /** constants */

    // for running callable workers in parallel
    private static final int PARALLEL_THREAD_COUNT = 6;

    // Workers
    private static final Class<?>[] WORKERS = {
        _OpcTxCurrLastPeriodWorker.class,
        _OpcTxNewNewlyEnrolledWorker.class,
        _OpcTxNewReturnedWorker.class,
        _OpcTxNewTransInWorker.class,
        _OpcTransOutWorker.class,
        _OpcSuccessfulTransOutWorker.class,
        _OpcLtfuWorker.class,
        _OpcDeadWorker.class,
        _OpcSdaWorker.class,
        _OpcMmdWorker.class,
        _OpcTldWorker.class,
        _OpcCohortWorker.class
    };

    private static final Integer[] SHEET_INDEX = {0, 1, 1, 1, 1, 1, 1, 1, 2, 3, 4, 5};
}
