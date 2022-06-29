package org.pepfar.pdma.app.api;

import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Workbook;
import org.pepfar.pdma.app.Constants;
import org.pepfar.pdma.app.data.domain.User;
import org.pepfar.pdma.app.data.dto.CaseReportFilterDto;
import org.pepfar.pdma.app.data.service.LabTestService;
import org.pepfar.pdma.app.data.service._ReportingService;
import org.pepfar.pdma.app.data.types.ReportType;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.CacheControl;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(path = "/api/v1/reporting")
public class ReportingRestController {

    @Autowired
    @Qualifier(value = "_CBSReportingServiceImpl")
    private _ReportingService cbsReportService;

    @Autowired
    @Qualifier(value = "_PatientExportServiceImpl")
    private _ReportingService patientExportService;

    @Autowired
    @Qualifier(value = "_RiskGroupReportingServiceImpl")
    private _ReportingService riskReportService;

    @Autowired
    @Qualifier(value = "_SHIReportingServiceImpl")
    private _ReportingService shiReportService;

    @Autowired
    @Qualifier(value = "_ViralLoadReportingServiceImpl")
    private _ReportingService vlReportService;

    @Autowired
    @Qualifier(value = "_MerOpcReportingServiceImpl")
    private _ReportingService merOpcReportService;

    @Autowired
    @Qualifier(value = "_OpcReportingServiceImpl")
    private _ReportingService opcReportingService;

    @Autowired
    @Qualifier(value = "_TBProphylaxis2ReportingServiceImpl")
    private _ReportingService tbProphylaxis2ReportService;

    @Autowired
    private LabTestService labService;

    @PostMapping()
    public void generateReport(@RequestBody CaseReportFilterDto filter, HttpServletResponse response) throws Exception {

        if (filter == null || filter.getReportType() == null) {
            throw new RuntimeException();
        }

        Workbook wbook = null;

        switch (filter.getReportType()) {
            case CBS_REPORT:
                wbook = cbsReportService.exportReport(filter);
                break;

            case SHI_EXPIRED:
            case NO_SHI:
                wbook = shiReportService.exportReport(filter);
                break;

            case ACTIVE_PATIENT_REPORT:
            case NEW_PATIENT_REPORT:
            case NEWLY_ENROLLED_PATIENT_REPORT:
            case RETURNED_PATIENT_REPORT:
            case TRANSEDIN_PATIENT_REPORT:
            case DEAD_LTFU_PATIENT_REPORT:
            case TRANSOUT_PATIENT_REPORT:
            case TX_TABULAR_SUMMARY:
            case MMD_LINELIST_DATA:
                wbook = patientExportService.exportReport(filter);
                break;

            case SHI_REPORT:
            case CV556_REPORT:
                wbook = shiReportService.exportReport(filter);
                break;

            case VL_PEPFAR_REPORT:
            case VL_PEPFAR_REPORT_OLD:
            case VL_VAAC_REPORT:
                wbook = vlReportService.exportReport(filter);
                break;

            case RISKGROUP_REPORT:
                wbook = riskReportService.exportReport(filter);
                break;

            case QUARTERLY_TB_TREATMENT:
            case MONTHLY_TB_TREATMENT:
                wbook = tbProphylaxis2ReportService.exportReport(filter);
                break;

            case VL_SCHEDULE:
                wbook = labService.getPatientsRequiringVL(filter);
                break;

            case VL_DATA:
            case CD4_DATA:
                wbook = labService.exportAllLabTestsData(filter);
                break;

            case MER_OPC:
                wbook = merOpcReportService.exportReport(filter);
                break;

            case OPC_REPORT_MONTHLY:
                wbook = opcReportingService.exportReport(filter);
                break;

            case LY_REQUEST_SEP_2021:
            case LY_REQUEST_SEP_2021_2:
                wbook = patientExportService.exportReport(filter);
                break;

            default:
                break;
        }

        if (wbook == null) {
            throw new RuntimeException();
        }

        String filename = getFilename(filter);
        if (filename == null) {
            throw new RuntimeException();
        }

        CacheControl cc = CacheControl.maxAge(360, TimeUnit.DAYS).cachePublic();

        response.addHeader("Access-Control-Expose-Headers", "x-filename");
        response.addHeader("Content-disposition", "inline; filename=" + filename);
        response.addHeader("x-filename", filename);
        response.setHeader("Cache-Control", cc.getHeaderValue());
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        // password for output file?
        if (passwordProtected(filter)) {
            POIFSFileSystem fs = new POIFSFileSystem();
            EncryptionInfo info = new EncryptionInfo(EncryptionMode.agile);

            Encryptor enc = info.getEncryptor();
            enc.confirmPassword(filter.getPassword());

            // write the workbook into the encrypted OutputStream
            OutputStream encos = enc.getDataStream(fs);
            wbook.write(encos);
            wbook.close();
            encos.close(); // this is necessary before writing out the FileSystem

            OutputStream os = response.getOutputStream();
            fs.writeFilesystem(os);
            os.close();
            fs.close();
        } else {
            wbook.write(response.getOutputStream());
            response.flushBuffer();
            response.getOutputStream().close();
            wbook.close();
        }
    }

    /**
     * Does the report require setting password for the output file?
     *
     * @param filter
     * @return
     */
    private boolean passwordProtected(CaseReportFilterDto filter) {

        User currentUser = SecurityUtils.getCurrentUser();
        boolean passwordReq = SecurityUtils.isUserInRoles(currentUser, Constants.ROLE_SITE_MANAGER,
                Constants.ROLE_DISTRICT_MANAGER, Constants.ROLE_PROVINCIAL_MANAGER);

        return !CommonUtils.isEmpty(filter.getPassword()) && passwordReq
                && (filter.getReportType() == ReportType.CBS_REPORT);
    }

    /**
     * Get file name for the file returned to client
     *
     * @param filter
     * @return
     */
    private String getFilename(CaseReportFilterDto filter) {

        if (filter == null || filter.getReportType() == null) {
            return null;
        }

        String filename = filter.getReportType().getFilenamePrefix();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

        if (filter.getReportType() != ReportType.ACTIVE_PATIENT_REPORT && filter.getReportType() != ReportType.MMD_LINELIST_DATA) {
            if (filter.getReportType() == ReportType.RISKGROUP_REPORT
                    || filter.getReportType() == ReportType.VL_SCHEDULE
                    || filter.getReportType() == ReportType.SHI_EXPIRED
                    || filter.getReportType() == ReportType.NO_SHI
                    || filter.getReportType() == ReportType.LY_REQUEST_SEP_2021_2) {
                filename += sdf.format(CommonUtils.fromLocalDateTime(CommonUtils.hanoiNow()));
                filename += ".xlsx";
            } else {
                if (filter.getFromDate() == null || filter.getToDate() == null) {
                    return null;
                } else {
                    filename += sdf.format(CommonUtils.fromLocalDateTime(filter.getFromDate()));
                    filename += "-";
                    filename += sdf.format(CommonUtils.fromLocalDateTime(filter.getToDate()));
                    filename += ".xlsx";
                }
            }
        } else {
            if (filter.getAtDate() == null) {
                return null;
            } else {
                filename += sdf.format(CommonUtils.fromLocalDateTime(filter.getAtDate()));
                filename += ".xlsx";
            }
        }

        return filename;
    }
}
