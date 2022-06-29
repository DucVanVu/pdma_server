package org.pepfar.pdma.app.data.opcassistimport;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.pepfar.pdma.app.data.domain.AdminUnit;
import org.pepfar.pdma.app.data.domain.Appointment;
import org.pepfar.pdma.app.data.domain.Case;
import org.pepfar.pdma.app.data.domain.CaseOrg;
import org.pepfar.pdma.app.data.domain.ClinicalStage;
import org.pepfar.pdma.app.data.domain.Dictionary;
import org.pepfar.pdma.app.data.domain.LabTest;
import org.pepfar.pdma.app.data.domain.Location;
import org.pepfar.pdma.app.data.domain.MMT;
import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.domain.Person;
import org.pepfar.pdma.app.data.domain.QAdminUnit;
import org.pepfar.pdma.app.data.domain.QCase;
import org.pepfar.pdma.app.data.domain.QDictionary;
import org.pepfar.pdma.app.data.domain.QRegimen;
import org.pepfar.pdma.app.data.domain.Regimen;
import org.pepfar.pdma.app.data.domain.RiskInterview;
import org.pepfar.pdma.app.data.domain.ShiInterview;
import org.pepfar.pdma.app.data.domain.TBProphylaxis;
import org.pepfar.pdma.app.data.domain.Treatment;
import org.pepfar.pdma.app.data.dto.RegimenDto;
import org.pepfar.pdma.app.data.repository.AdminUnitRepository;
import org.pepfar.pdma.app.data.repository.AppointmentRepository;
import org.pepfar.pdma.app.data.repository.CaseOrgRepository;
import org.pepfar.pdma.app.data.repository.CaseRepository;
import org.pepfar.pdma.app.data.repository.DictionaryRepository;
import org.pepfar.pdma.app.data.repository.HIVConfirmLabRepository;
import org.pepfar.pdma.app.data.repository.LabTestRepository;
import org.pepfar.pdma.app.data.repository.LocationRepository;
import org.pepfar.pdma.app.data.repository.MMTRepository;
import org.pepfar.pdma.app.data.repository.OrganizationRepository;
import org.pepfar.pdma.app.data.repository.PersonRepository;
import org.pepfar.pdma.app.data.repository.RegimenRepository;
import org.pepfar.pdma.app.data.repository.RiskInterviewRepository;
import org.pepfar.pdma.app.data.repository.ShiInterviewRepository;
import org.pepfar.pdma.app.data.repository.TBProphylaxisRepository;
import org.pepfar.pdma.app.data.repository.TreatmentRepository;
import org.pepfar.pdma.app.data.repository.WRCaseRepository;
import org.pepfar.pdma.app.data.types.AddressType;
import org.pepfar.pdma.app.data.types.ClinicalTestingType;
import org.pepfar.pdma.app.data.types.EnrollmentType;
import org.pepfar.pdma.app.data.types.Gender;
import org.pepfar.pdma.app.data.types.LabTestFundingSource;
import org.pepfar.pdma.app.data.types.LabTestReason;
import org.pepfar.pdma.app.data.types.PatientStatus;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.querydsl.jpa.impl.JPAQuery;

@Service
public class Importer {

    @Autowired private CaseRepository caseRepos;

    @Autowired private ShiInterviewRepository shiRepos;

    @Autowired private LabTestRepository labRepos;

    @Autowired private AdminUnitRepository auRepos;

    @Autowired private OrganizationRepository orgRepos;

    @Autowired private CaseOrgRepository coRepos;

    @Autowired private RegimenRepository regimenRepos;

    @Autowired private DictionaryRepository dicRepos;

    @Autowired private RiskInterviewRepository riskRepos;

    @Autowired private WRCaseRepository wrCaseRepos;

    @Autowired private PersonRepository personRepos;

    @Autowired private LocationRepository locRepos;

    @Autowired private AppointmentRepository appRepos;

    @Autowired private HIVConfirmLabRepository confirmLabRepos;

    @Autowired private TBProphylaxisRepository tbProRepos;

    @Autowired private TreatmentRepository txRepos;

    @Autowired private MMTRepository mmtRepos;

    @Autowired private EntityManager em;

    private static final String SHEET_PATIENT = "Patient-1";

    private static final String SHEET_TB_PRO = "TBProphylaxis-1";

    private static final String SHEET_VIRALLOAD = "VLData-1";

    private static final String SHEET_SHI = "SHIData-1";

    private static final String SHEET_RISK = "RiskGroup-1";

    @Transactional(rollbackFor = Exception.class)
    public void updateAppointmentARVRegimenData() {
        QCase q = QCase.case$;
        List<Case> cases =
                new JPAQuery<Case>(em).distinct().from(q).where(q.deleted.isFalse()).fetch();

        System.out.println("Start: Updating appointments");
        int count = 0;
        for (Case c : cases) {
            List<Treatment> treatments = Lists.newArrayList(c.getTreatments());
            Iterator<Appointment> apps = c.getAppointments().iterator();

            while (apps.hasNext()) {
                Appointment app = apps.next();

                if (app.getArrivalDate() == null
                        || !CommonUtils.isEmpty(app.getArvRegimenName(), true)) {
                    continue;
                }

                RegimenDto regimen = getRegimen(app.getArrivalDate(), treatments);
                if (regimen != null) {
                    app.setArvRegimenName(regimen.getName());
                    app.setArvRegimenLine(regimen.getLine());

                    appRepos.save(app);
                    count++;
                    System.out.println("--> saved appointment with ID = " + app.getId());
                }
            }
        }
        System.out.println("Completed updating appointments. Updated " + count + " appointments.");
    }

    private RegimenDto getRegimen(LocalDateTime arrivalDate, List<Treatment> treatments) {

        boolean found = false;
        RegimenDto regimen = null;

        for (Treatment tx : treatments) {
            if (tx.getEndDate() != null) {
                if (CommonUtils.dateInRange(
                        arrivalDate, tx.getStartDate(), tx.getEndDate().minusDays(1))) {
                    found = true;
                }
            } else {
                if (arrivalDate.isAfter(tx.getStartDate())
                        || arrivalDate.isEqual(tx.getStartDate())) {
                    found = true;
                }
            }

            if (found) {
                regimen = new RegimenDto();
                regimen.setName(tx.getRegimenName());
                regimen.setLine(tx.getRegimenLine());
                break;
            }
        }

        return regimen;
    }

    @Transactional(rollbackFor = Exception.class)
    public void clinicalStageAndOccupationUpdate() {
        QCase q = QCase.case$;
        List<Case> cases =
                new JPAQuery<Case>(em).distinct().from(q).where(q.deleted.isFalse()).fetch();

        System.out.println("Start: Updating clinical stage & occupation.");
        for (Case c : cases) {
            Iterator<ClinicalStage> itr1 = c.getWhoStages().iterator();
            if (itr1.hasNext()) {
                ClinicalStage e = itr1.next();

                c.setWhoStage(e.getStage());
                c.setWhoStageEvalDate(e.getEvalDate());
            }

            boolean updatedShi = false;
            Iterator<ShiInterview> itr2 = c.getShiInterviews().iterator();
            Person p = c.getPerson();

            while (itr2.hasNext()) {
                ShiInterview e = itr2.next();

                if (!updatedShi) {
                    if (!CommonUtils.isEmpty(e.getShiCardNumber(), true)) {
                        c.setShiNumber(e.getShiCardNumber());
                        updatedShi = true;
                    } else {
                        c.setShiNumber(null);
                    }
                }

                if (CommonUtils.isPositive(e.getOccupation(), true)) {
                    System.out.println("--> found occupation: " + e.getOccupationName());
                    p.setOccupation(e.getOccupation());
                    p.setOccupationName(e.getOccupationName());
                    break;
                }
            }

            c.setPerson(p);

            caseRepos.save(c);
            System.out.println("Updated case with UID = " + c.getUid());
        }
        System.out.println("Finish: Updating clinical stage & occupation.");
    }

    @Transactional(rollbackFor = Exception.class)
    public void importHIVInfoID(Workbook wbook) {

        if (wbook == null) {
            throw new RuntimeException("Invalid workbook!");
        }

        Sheet sheet = wbook.getSheetAt(0);

        if (sheet == null) {
            throw new RuntimeException("Patient data not found!");
        }

        // Loop thru the patient sheet
        Iterator<Row> rows = sheet.iterator();
        DataFormatter formatter = new DataFormatter();

        int rowNumber = 1;
        int countSuccess = 0;

        while (rows.hasNext()) {
            Row row = rows.next();

            System.out.println("Processing row # " + rowNumber + " on patient worksheet.");

            // Skip the first row (header)
            if (row.getRowNum() < 2) {
                rowNumber++;
                continue;
            }

            // Check exit point
            if (row.getCell(0) != null
                    && CommonUtils.isEmpty(formatter.formatCellValue(row.getCell(0)))) {
                break;
            }

            String val = formatter.formatCellValue(row.getCell(0));
            Long id = 0l;

            try {
                id = Long.parseLong(val);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            //			UUID uid = UUID.fromString(val);

            if (!CommonUtils.isPositive(id, true)) {
                rowNumber++;
                System.out.println("Invalid ID: " + val + ". Skip the current row.");
                continue;
            }

            // Check if a patient with the same UID exists?
            Case entity = caseRepos.findOne(id);
            if (entity == null) {
                rowNumber++;
                System.out.println(
                        "A patient with ID " + val + " does not exist. Skip the current row.");
                continue;
            }

            // HIVInfo ID
            if (row.getCell(5) == null) {
                rowNumber++;
                continue;
            }

            val = formatter.formatCellValue(row.getCell(5));

            if (CommonUtils.isEmpty(val)
                    || val.trim().length() <= 0) { // để có thể import được mã cho các ca ngoại
                // tỉnh.
                rowNumber++;
                System.out.println(
                        "A HIVInfo ID is not valid for patient with ID =  "
                                + id.toString()
                                + ". Skip the current row.");
                continue;
            }

            val = val.trim();
            val = val.replace(" ", "");

            entity.setHivInfoID(val.trim());
            entity = caseRepos.save(entity);

            rowNumber++;
            countSuccess++;
        }

        System.out.println("DONE: Importing HIVInfo ID.");
        System.out.println("Total number of IDs imported = " + countSuccess);
    }

    @Transactional(rollbackFor = Exception.class)
    public void importMMTData(Workbook wbook) {

        if (wbook == null) {
            throw new RuntimeException("Invalid workbook!");
        }

        Sheet sheet = wbook.getSheetAt(0);

        if (sheet == null) {
            throw new RuntimeException("Patient data not found!");
        }

        // Loop thru the patient sheet
        Iterator<Row> rows = sheet.iterator();
        DataFormatter formatter = new DataFormatter();

        int rowNumber = 1;
        int countSuccess = 0;

        while (rows.hasNext()) {
            Row row = rows.next();

            System.out.println("Processing row # " + rowNumber + " on patient worksheet.");

            // Skip the first row (header)
            if (row.getRowNum() < 1) {
                rowNumber++;
                continue;
            }

            // Check exit point
            if (row.getCell(0) != null
                    && CommonUtils.isEmpty(formatter.formatCellValue(row.getCell(0)))) {
                break;
            }

            String val = formatter.formatCellValue(row.getCell(0));
            UUID uid = UUID.fromString(val);

            // Check if a patient with the same UID exists?
            List<Case> checkList = caseRepos.findByUID(uid);
            if (checkList == null || checkList.size() <= 0) {
                rowNumber++;
                System.out.println(
                        "A patient with UID " + val + " does not exist. Skip the current row.");
                continue;
            }

            Case theCase = checkList.get(0);
            MMT mmt = theCase.getMmt();

            Organization latestOrg = null;

            for (CaseOrg co : theCase.getCaseOrgs()) {
                if (latestOrg == null) {
                    latestOrg = co.getOrganization();
                    break;
                }
            }

            // Start date
            if (latestOrg == null || row.getCell(7) == null) {
                rowNumber++;
                continue;
            }

            LocalDateTime dateVal = ImportUtils.fromCellValue(row.getCell(7));
            if (dateVal == null) {
                rowNumber++;
                continue;
            }

            System.out.println(
                    "Start date = " + dateVal.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            if (mmt == null) {
                mmt = new MMT();
                mmt.setUid(UUID.randomUUID());
            }

            mmt.setTheCase(theCase);
            mmt.setOrganization(latestOrg);
            mmt.setOnMMT(true);

            mmt.setStartDate(dateVal);

            if (row.getCell(8) != null) {
                dateVal = ImportUtils.fromCellValue(row.getCell(8));
                if (dateVal != null) {
                    mmt.setStoppedMMT(true);
                    mmt.setEndDate(dateVal);

                    System.out.println(
                            "-> End date = "
                                    + dateVal.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
            }

            val = row.getCell(5) == null ? null : formatter.formatCellValue(row.getCell(5));
            mmt.setMmtPatientCode(val);

            val = row.getCell(4) == null ? null : formatter.formatCellValue(row.getCell(4));
            mmt.setFacilityName(val);

            mmtRepos.save(mmt);

            rowNumber++;
            countSuccess++;
        }

        System.out.println("DONE: Importing MMT data.");
        System.out.println("Total number of records imported = " + countSuccess);
    }

    /**
     * Second round of import
     *
     * @param wbook
     * @param opcId
     * @param importTBData
     */
    @Transactional(rollbackFor = Exception.class)
    public void importDataRevised(Workbook wbook, Long opcId, boolean importTBData) {

        if (wbook == null || !CommonUtils.isPositive(opcId, true)) {
            throw new RuntimeException("Invalid workbook and/or invalid OPC ID!");
        }

        Organization opc = orgRepos.findOne(opcId);

        if (opc == null) {
            throw new RuntimeException("OPC not found!");
        }

        Sheet patientSheet = wbook.getSheet(SHEET_PATIENT);
        Sheet tbProSheet = wbook.getSheet(SHEET_TB_PRO);

        if (patientSheet == null || tbProSheet == null) {
            throw new RuntimeException("Invalid workbook. Expected worksheets not found!");
        }

        // -----------
        // region Step 1: Import data from patient sheet
        // -----------
        System.out.println("BEGIN: Importing from patient worksheet...");

        DataFormatter formatter = new DataFormatter();
        Iterator<Row> rows = patientSheet.iterator();

        int rowNumber = 1;
        while (rows.hasNext()) {
            Row row = rows.next();

            System.out.println("Processing row # " + rowNumber + " on patient worksheet.");

            // Skip the first row (header)
            if (row.getRowNum() == 0) {
                rowNumber++;
                continue;
            }

            // Check exit point
            if (row.getCell(0) != null
                    && CommonUtils.isEmpty(formatter.formatCellValue(row.getCell(0)))) {
                break;
            }

            // UID
            String val = row.getCell(0) != null ? formatter.formatCellValue(row.getCell(0)) : null;

            if (CommonUtils.isEmpty(val)) {
                rowNumber++;
                continue;
            }

            UUID uid = UUID.fromString(val);

            // Check if a patient with the same UID already exists?
            List<Case> checkList = caseRepos.findByUID(uid);
            if (checkList == null || checkList.size() <= 0) {
                rowNumber++;
                System.out.println(
                        "Could not find patient with UID " + val + ". Skip the current row.");
                continue;
            }

            // ---------------------------
            // BEGIN: Case-organization
            // ---------------------------

            // Find the original Case-Org
            Case theCase = checkList.get(0);

            if (theCase == null) {
                rowNumber++;
                System.out.println(
                        "Could not find patient with UID " + val + ". Skip the current row.");
                continue;
            }

            CaseOrg co = null;
            Iterator<CaseOrg> cos = theCase.getCaseOrgs().iterator();
            while (cos.hasNext()) {
                CaseOrg _co = cos.next();

                if (_co.getOrganization().getId().equals(opc.getId().longValue())) {
                    co = _co;
                }
            }

            if (co != null) {
                LocalDateTime dateVal = ImportUtils.fromCellValue(row.getCell(33));

                if (dateVal != null) {
                    co.setStartDate(dateVal);
                }

                val = row.getCell(37) != null ? formatter.formatCellValue(row.getCell(37)) : null;
                if (!CommonUtils.isEmpty(val, true)) {
                    co.setArvGroup(val.trim());
                }

                val = row.getCell(3) != null ? formatter.formatCellValue(row.getCell(3)) : null;
                if (!CommonUtils.isEmpty(val, true)
                        && CommonUtils.isEmpty(co.getPatientChartId())) {
                    co.setPatientChartId(val);
                }

                coRepos.save(co);
            }

            rowNumber++;
        }

        System.out.println("END: Importing from patient sheet.");

        // endregion Step 1: Import data from patient sheet

        // -----------
        // region Step 2 - Import data from TB prophylaxis sheet
        // -----------
        if (!importTBData) {
            return;
        }

        System.out.println("BEGIN: Importing from TB prophylaxis worksheet...");

        QCase qCase = QCase.case$;

        rows = tbProSheet.iterator();
        rowNumber = 1;

        while (rows.hasNext()) {
            Row row = rows.next();

            System.out.println("Processing row #" + rowNumber + " on TB prophylaxis worksheet.");

            // Skip the first row (header)
            if (row.getRowNum() == 0) {
                rowNumber++;
                continue;
            }

            // Check exit point
            if (row.getCell(0) != null
                    && CommonUtils.isEmpty(formatter.formatCellValue(row.getCell(0)))) {
                break;
            }

            // Case UID
            String val = row.getCell(1) != null ? formatter.formatCellValue(row.getCell(1)) : null;
            if (CommonUtils.isEmpty(val)) {
                System.out.println(
                        "Invalid patient UID on row #"
                                + rowNumber
                                + " of the TB pro worksheet --> skipped the TB pro record.");
                rowNumber++;
                continue;
            }

            Case theCase = caseRepos.findOne(qCase.uid.eq(UUID.fromString(val)));
            if (theCase == null) {
                System.out.println(
                        "Could not find the corresponding patient for TB pro record on row #"
                                + rowNumber
                                + " --> skipped the TB pro record.");
                rowNumber++;
                continue;
            }

            TBProphylaxis tbPro = new TBProphylaxis();

            // UID
            val = row.getCell(0) != null ? formatter.formatCellValue(row.getCell(0)) : null;
            if (CommonUtils.isEmpty(val)) {
                System.out.println(
                        "Invalid UID on row #"
                                + rowNumber
                                + " of the TB pro worksheet --> skipped the TB pro record.");
                rowNumber++;
                continue;
            }

            tbPro.setUid(UUID.fromString(val));

            // Check if a TB pro record with the same UUID already exists
            List<TBProphylaxis> checkList = tbProRepos.findByUUID(tbPro.getUid());

            if (checkList != null && checkList.size() > 0) {
                rowNumber++;
                System.out.println(
                        "A lab test with UID " + val + " already exists. Skipped the current row.");
                continue;
            }

            // Start date
            LocalDateTime dateVal = ImportUtils.fromCellValue(row.getCell(2));
            tbPro.setStartDate(dateVal);

            // Count the CL cells
            int count = 0;
            int lastIndex = 0;

            for (int i = 0; i < 12; i++) {
                val =
                        row.getCell(i + 3) != null
                                ? formatter.formatCellValue(row.getCell(i + 3))
                                : null;

                if (val != null && val.equalsIgnoreCase("CL")) {
                    lastIndex = i;
                    count++;
                }
            }

            if (count == 9) {
                tbPro.setResult(3);

                // estimate end date
                if (tbPro.getStartDate() != null) {
                    dateVal = tbPro.getStartDate().plusMonths(lastIndex + 1);
                    tbPro.setEndDate(dateVal);
                }
            } else {
                tbPro.setResult(2);
            }

            // Note
            val = row.getCell(15) != null ? formatter.formatCellValue(row.getCell(15)) : null;

            tbPro.setOrganization(opc);
            tbPro.setTheCase(theCase);

            tbPro = tbProRepos.save(tbPro);

            rowNumber++;
        }

        System.out.println("END: Importing from TB prophylaxis worksheet.");

        // -----------
        // endregion Step 2 - Import data from TB prophylaxis sheet
        // -----------
    }

    /**
     * Third round of import -> correct ARV regimen (Phacdokhac)
     *
     * @param wbook
     * @param opcId
     * @param importTBData
     */
    @Transactional(rollbackFor = Exception.class)
    public void importDataRevised2(Workbook wbook) {

        if (wbook == null) {
            throw new RuntimeException("Invalid workbook!");
        }

        Sheet patientSheet = wbook.getSheet(SHEET_PATIENT);

        if (patientSheet == null) {
            throw new RuntimeException("Invalid workbook. Expected worksheets not found!");
        }

        // -----------
        // region Step 1: Import data from patient sheet
        // -----------
        System.out.println("BEGIN: Importing from patient worksheet...");

        DataFormatter formatter = new DataFormatter();
        Iterator<Row> rows = patientSheet.iterator();

        while (rows.hasNext()) {
            Row row = rows.next();

            // Skip the first row (header)
            if (row.getRowNum() == 0) {
                continue;
            }

            // Check exit point
            if (row.getCell(0) != null
                    && CommonUtils.isEmpty(formatter.formatCellValue(row.getCell(0)))) {
                break;
            }

            // UID
            String val = row.getCell(0) != null ? formatter.formatCellValue(row.getCell(0)) : null;

            if (CommonUtils.isEmpty(val)) {
                continue;
            }

            UUID uid = UUID.fromString(val);

            // Check if a patient with the same UID already exists?
            List<Case> checkList = caseRepos.findByUID(uid);
            if (checkList == null || checkList.size() <= 0) {
                continue;
            }

            Case theCase = checkList.get(0);

            if (theCase == null) {
                continue;
            }

            val = row.getCell(10) != null ? formatter.formatCellValue(row.getCell(10)) : null;

            if (CommonUtils.isEmpty(val, true)) {
                continue;
            }

            if ("Phác đồ khác".equalsIgnoreCase(val)) {
                // check the case and see if the current regimen is still "Phacdokhac"

                String val2 =
                        row.getCell(11) != null ? formatter.formatCellValue(row.getCell(11)) : null;

                if (CommonUtils.isEmpty(val2, true)) {
                    continue;
                }

                val2 = val2.replace(" ", "");

                System.out.println("Found: " + val2);

                if ("Phácđồkhác".equalsIgnoreCase(theCase.getCurrentArvRegimenName())) {
                    theCase.setCurrentArvRegimenName(val2);
                    caseRepos.save(theCase);
                    System.out.println("--> replaced in case.");
                }

                Iterator<Treatment> treatments = theCase.getTreatments().iterator();
                while (treatments.hasNext()) {
                    Treatment tx = treatments.next();

                    if (tx != null && "Phácđồkhác".equalsIgnoreCase(tx.getRegimenName())) {
                        tx.setRegimenName(val2);
                        txRepos.save(tx);
                        System.out.println("--> replaced in treatment.");
                    }
                }
            }
        }

        System.out.println("END: Importing from patient sheet.");

        // endregion Step 1: Import data from patient sheet
    }

    /**
     * First round of import
     *
     * @param wbook
     * @param opcId
     */
    @Transactional(rollbackFor = Exception.class)
    public void importData(Workbook wbook, Long opcId) {

        if (wbook == null || !CommonUtils.isPositive(opcId, true)) {
            throw new RuntimeException("Invalid workbook and/or invalid OPC ID!");
        }

        Organization opc = orgRepos.findOne(opcId);

        if (opc == null) {
            throw new RuntimeException("OPC not found!");
        }

        Sheet patientSheet = wbook.getSheet(SHEET_PATIENT);
        Sheet vlSheet = wbook.getSheet(SHEET_VIRALLOAD);
        Sheet shiSheet = wbook.getSheet(SHEET_SHI);
        Sheet riskSheet = wbook.getSheet(SHEET_RISK);
        Sheet tbProSheet = wbook.getSheet(SHEET_TB_PRO);

        if (patientSheet == null
                || vlSheet == null
                || shiSheet == null
                || riskSheet == null
                || tbProSheet == null) {
            throw new RuntimeException("Invalid workbook. Expected worksheets not found!");
        }

        // -----------
        // region Step 1: Import data from patient sheet
        // -----------

        System.out.println("BEGIN: Importing from patient worksheet...");

        // Country admin unit
        QRegimen qRegimen = QRegimen.regimen;
        QAdminUnit qAu = QAdminUnit.adminUnit;
        AdminUnit country = auRepos.findOne(qAu.code.equalsIgnoreCase("country_1"));
        DataFormatter formatter = new DataFormatter();

        Iterator<Row> rows = patientSheet.iterator();
        int rowNumber = 1;
        while (rows.hasNext()) {
            Row row = rows.next();

            System.out.println("Processing row # " + rowNumber + " on patient worksheet.");

            // Skip the first row (header)
            if (row.getRowNum() == 0) {
                rowNumber++;
                continue;
            }

            // Check exit point
            if (row.getCell(0) != null
                    && CommonUtils.isEmpty(formatter.formatCellValue(row.getCell(0)))) {
                break;
            }

            Case entity = new Case();

            // UID
            String val = row.getCell(0) != null ? formatter.formatCellValue(row.getCell(0)) : null;

            if (CommonUtils.isEmpty(val)) {
                rowNumber++;
                continue;
            }

            UUID uid = UUID.fromString(val);

            // Check if a patient with the same UID already exists?
            List<Case> checkList = caseRepos.findByUID(uid);
            if (checkList != null && checkList.size() > 0) {
                rowNumber++;
                System.out.println(
                        "A patient with UID " + val + " already exists. Skip the current row.");
                continue;
            }

            entity.setUid(uid);
            entity.setDeleted(false);

            // HIV confirm date
            LocalDateTime dateVal = ImportUtils.fromCellValue(row.getCell(34));

            if (dateVal != null) {
                entity.setHivConfirmDate(dateVal);
            }

            // HIV confirm lab name
            val = row.getCell(36) != null ? formatter.formatCellValue(row.getCell(36)) : null;
            if (!CommonUtils.isEmpty(val)) {
                entity.setConfirmLabName(val);
            }

            // ARV start date
            dateVal = ImportUtils.fromCellValue(row.getCell(9));
            if (dateVal != null) {
                entity.setArvStartDate(dateVal);
            } else {
                System.out.println(
                        "Invalid ARV start date of patient on row #"
                                + rowNumber
                                + " --> skipped the patient record.");
                rowNumber++;
                continue;
            }

            // ------------------------
            // BEGIN: Person
            // ------------------------
            Person person = new Person();

            // Fullname
            val = row.getCell(4) != null ? formatter.formatCellValue(row.getCell(4)) : null;
            if (!CommonUtils.isEmpty(val)) {
                person.setFullname(val);
            } else {
                System.out.println(
                        "Invalid full name of patient on row #"
                                + rowNumber
                                + " --> skipped the patient record.");
                rowNumber++;
                continue;
            }

            // DOB
            dateVal = row.getCell(5) != null ? ImportUtils.fromCellValue(row.getCell(5)) : null;
            if (dateVal != null) {
                person.setDob(dateVal);
            } else {
                System.out.println(
                        "Invalid DOB of patient on row #"
                                + rowNumber
                                + " --> skipped the patient record.");
                rowNumber++;
                continue;
            }

            // Gender
            val = row.getCell(6) != null ? formatter.formatCellValue(row.getCell(6)) : null;
            switch (val) {
                case "Nam":
                    person.setGender(Gender.MALE);
                    break;

                case "Nữ":
                    person.setGender(Gender.FEMALE);
                    break;

                default:
                    person.setGender(Gender.OTHER);
                    break;
            }

            // National ID
            val = row.getCell(35) != null ? formatter.formatCellValue(row.getCell(35)) : null;
            if (!CommonUtils.isEmpty(val)) {
                person.setNidNumber(val);
            }

            // Phone number
            val = row.getCell(40) != null ? formatter.formatCellValue(row.getCell(40)) : null;
            if (!CommonUtils.isEmpty(val)) {
                person.setMobilePhone(val);
            }

            List<Location> locations = new ArrayList<>();

            // Current address
            String[] addressCodes = ImportUtils.getAddressCodes(row.getCell(42));
            val = row.getCell(16) != null ? formatter.formatCellValue(row.getCell(16)) : null;

            if (addressCodes != null) {
                AdminUnit commune = null;
                AdminUnit district = null;
                AdminUnit province = null;

                int length = addressCodes.length;

                if (length == 1) {
                    province = auRepos.findOne(qAu.code.equalsIgnoreCase(addressCodes[0]));
                } else if (length == 2) {
                    district = auRepos.findOne(qAu.code.equalsIgnoreCase(addressCodes[0]));
                    province = auRepos.findOne(qAu.code.equalsIgnoreCase(addressCodes[1]));
                } else if (length == 3) {
                    commune = auRepos.findOne(qAu.code.equalsIgnoreCase(addressCodes[0]));
                    district = auRepos.findOne(qAu.code.equalsIgnoreCase(addressCodes[1]));
                    province = auRepos.findOne(qAu.code.equalsIgnoreCase(addressCodes[2]));
                }

                Location address = new Location();
                address.setAddressType(AddressType.CURRENT_ADDRESS);
                address.setCountry(country);
                address.setProvince(province);
                address.setDistrict(district);
                address.setCommune(commune);
                address.setStreetAddress(val);
                address.setPerson(person);

                locations.add(address);

            } else {
                System.out.println(
                        "Invalid current address codes of patient on row #"
                                + rowNumber
                                + " --> skipped the patient record.");
                rowNumber++;
                continue;
            }

            // Resident address
            addressCodes = ImportUtils.getAddressCodes(row.getCell(41));
            val = row.getCell(12) != null ? formatter.formatCellValue(row.getCell(12)) : null;

            if (addressCodes != null) {
                AdminUnit commune = null;
                AdminUnit district = null;
                AdminUnit province = null;

                int length = addressCodes.length;

                if (length == 1) {
                    province = auRepos.findOne(qAu.code.equalsIgnoreCase(addressCodes[0]));
                } else if (length == 2) {
                    district = auRepos.findOne(qAu.code.equalsIgnoreCase(addressCodes[0]));
                    province = auRepos.findOne(qAu.code.equalsIgnoreCase(addressCodes[1]));
                } else if (length == 3) {
                    commune = auRepos.findOne(qAu.code.equalsIgnoreCase(addressCodes[0]));
                    district = auRepos.findOne(qAu.code.equalsIgnoreCase(addressCodes[1]));
                    province = auRepos.findOne(qAu.code.equalsIgnoreCase(addressCodes[2]));
                }

                Location address = new Location();
                address.setAddressType(AddressType.RESIDENT_ADDRESS);
                address.setCountry(country);
                address.setProvince(province);
                address.setDistrict(district);
                address.setCommune(commune);
                address.setStreetAddress(val);
                address.setPerson(person);

                locations.add(address);

            } else {
                System.out.println(
                        "Invalid resident address codes of patient on row #"
                                + rowNumber
                                + " --> skipped the patient record.");
                rowNumber++;
                continue;
            }

            person.getLocations().clear();
            person.getLocations().addAll(locations);

            entity.setPerson(person);
            // ------------------------
            // END: Person
            // ------------------------
            dateVal = ImportUtils.fromCellValue(row.getCell(9));
            LocalDateTime dateVal2 = ImportUtils.fromCellValue(row.getCell(39)); // ARV switch date

            if (dateVal2 != null) {
                entity.setCurrentArvRegimenStartDate(dateVal2);
            } else {
                entity.setCurrentArvRegimenStartDate(dateVal);
            }

            val = row.getCell(38) != null ? formatter.formatCellValue(row.getCell(38)) : null;

            // Phác đồ bậc 2
            if (val != null && val.endsWith("2")) {
                entity.setCurrentArvRegimenLine(2);
                entity.setSecondLineStartDate(dateVal2);
            } else {
                entity.setCurrentArvRegimenLine(1);
            }

            val = row.getCell(10) != null ? formatter.formatCellValue(row.getCell(10)) : null;
            if (!CommonUtils.isEmpty(val)) {
                val = val.replace(" ", "");

                if ("Phácđồkhác".equalsIgnoreCase(val)) {
                    val =
                            row.getCell(11) != null
                                    ? formatter.formatCellValue(row.getCell(11))
                                    : null;
                    if (val != null) {
                        val = val.replace(" ", "");
                    }
                } else {

                    if ("AZT+3TC/FTC+LVP/r(ATV/r)".equalsIgnoreCase(val)) {
                        val = "AZT+3TC+LPV/r";
                    } else if ("TDF+3TC/FTC+LVP/r(ATV/r)".equalsIgnoreCase(val)) {
                        val = "TDF+3TC+LPV/r";
                    }

                    Regimen regimen =
                            regimenRepos.findOne(qRegimen.shortName.equalsIgnoreCase(val));

                    if (regimen != null) {
                        entity.setCurrentArvRegimen(regimen);
                    }
                }

                entity.setCurrentArvRegimenName(val);
            }

            // ---------------------------
            // BEGIN: Case-organization
            // ---------------------------
            CaseOrg co = new CaseOrg();

            co.setUid(UUID.randomUUID());
            co.setTheCase(entity);
            co.setOrganization(opc);

            // Patient chart ID
            val = row.getCell(3) != null ? formatter.formatCellValue(row.getCell(3)) : null;
            if (!CommonUtils.isEmpty(val)) {
                co.setPatientChartId(val);
            }

            // Start date
            dateVal = ImportUtils.fromCellValue(row.getCell(33));
            if (dateVal != null) {
                co.setStartDate(dateVal);
                co.setArvStartDate(dateVal);
            } else {
                co.setStartDate(entity.getArvStartDate());
                co.setArvStartDate(entity.getArvStartDate());
            }

            // ARV cohort
            val = row.getCell(37) != null ? formatter.formatCellValue(row.getCell(37)) : null;
            if (!CommonUtils.isEmpty(val)) {
                co.setArvGroup(val);
            }

            val = row.getCell(31) != null ? formatter.formatCellValue(row.getCell(31)) : null;
            dateVal = ImportUtils.fromCellValue(row.getCell(32));

            // by default
            co.setEnrollmentType(EnrollmentType.NEWLY_ENROLLED);

            if (val.equalsIgnoreCase("Bệnh nhân đang điều trị")) {
                if (dateVal != null && dateVal.isAfter(co.getStartDate())) {
                    co.setStartDate(dateVal);
                    co.setArvStartDate(dateVal);
                    co.setEnrollmentType(EnrollmentType.RETURNED);
                    co.setPrevStatus(PatientStatus.LTFU);
                } else {
                    co.setPrevStatus(PatientStatus.NULL);
                }

                co.setEndDate(null);
                co.setCurrent(true);
                co.setStatus(PatientStatus.ACTIVE);
                co.setEndingReason(null);
            } else if (val.equalsIgnoreCase("Bệnh nhân đã bỏ trị")) {
                co.setEndDate(dateVal);
                co.setCurrent(true);
                co.setPrevStatus(PatientStatus.ACTIVE);
                co.setStatus(PatientStatus.LTFU);
                co.setEndingReason("Bệnh nhân bỏ trị.");
            } else if (val.equalsIgnoreCase("Bệnh nhân đã chuyển đi")) {
                co.setEndDate(dateVal);
                co.setCurrent(false);
                co.setPrevStatus(PatientStatus.ACTIVE);
                co.setStatus(PatientStatus.TRANSFERRED_OUT);
                co.setEndingReason("Bệnh nhân chuyển đi.");
            } else if (val.equalsIgnoreCase("Bệnh nhân đã tử vong")) {
                co.setEndDate(dateVal);
                co.setCurrent(true);
                co.setPrevStatus(PatientStatus.ACTIVE);
                co.setStatus(PatientStatus.DEAD);
                co.setEndingReason("Bệnh nhân tử vong.");
            }

            co.setLatestRelationship(true);
            co.setRefTrackingOnly(false);

            entity.getCaseOrgs().clear();
            entity.getCaseOrgs().add(co);
            // ---------------------------
            // BEGIN: Case-organization
            // ---------------------------

            // ----------------------
            // BEGIN: Treatment
            // ----------------------
            if (entity.getCurrentArvRegimenName() != null) {
                Dictionary hiv =
                        dicRepos.findOne(QDictionary.dictionary.code.equalsIgnoreCase("HIV"));
                Treatment treatment = new Treatment();
                treatment.setUid(UUID.randomUUID());
                treatment.setDisease(hiv);
                treatment.setTheCase(entity);
                treatment.setOrganization(opc);
                treatment.setRegimen(entity.getCurrentArvRegimen());
                treatment.setRegimenName(entity.getCurrentArvRegimenName());
                treatment.setRegimenLine(entity.getCurrentArvRegimenLine());
                treatment.setStartDate(entity.getCurrentArvRegimenStartDate());

                entity.getTreatments().clear();
                entity.getTreatments().add(treatment);
            }
            // ----------------------
            // END: Treatment
            // ----------------------

            entity = caseRepos.save(entity);

            rowNumber++;
        }

        System.out.println("END: Importing from patient sheet.");

        // endregion Step 1: Import data from patient sheet

        // -----------
        // region Step 2 - Import data from viral load sheet
        // -----------

        System.out.println("BEGIN: Importing from viral load worksheet...");

        QCase qCase = QCase.case$;

        rows = vlSheet.iterator();
        rowNumber = 1;

        while (rows.hasNext()) {
            Row row = rows.next();

            System.out.println("Processing row #" + rowNumber + " on viral load worksheet.");

            // Skip the first row (header)
            if (row.getRowNum() == 0) {
                rowNumber++;
                continue;
            }

            // Check exit point
            if (row.getCell(0) != null
                    && CommonUtils.isEmpty(formatter.formatCellValue(row.getCell(0)))) {
                break;
            }

            // Case UID
            String val = row.getCell(1) != null ? formatter.formatCellValue(row.getCell(1)) : null;
            if (CommonUtils.isEmpty(val)) {
                System.out.println(
                        "Invalid patient UID on row #"
                                + rowNumber
                                + " of the VL worksheet --> skipped the viral load record.");
                rowNumber++;
                continue;
            }

            Case theCase = caseRepos.findOne(qCase.uid.eq(UUID.fromString(val)));
            if (theCase == null) {
                System.out.println(
                        "Could not find the corresponding patient for VL record on row #"
                                + rowNumber
                                + " --> skipped the viral load record.");
                rowNumber++;
                continue;
            }

            LabTest entity = new LabTest();

            // Test type
            entity.setTestType(ClinicalTestingType.VIRAL_LOAD);

            // UID
            val = row.getCell(0) != null ? formatter.formatCellValue(row.getCell(0)) : null;
            if (CommonUtils.isEmpty(val)) {
                System.out.println(
                        "Invalid UID on row #"
                                + rowNumber
                                + " of the VL worksheet --> skipped the viral load record.");
                rowNumber++;
                continue;
            }

            entity.setUid(UUID.fromString(val));

            // Check if a viral load record with the same UUID already exists
            List<LabTest> checkList = labRepos.findByUID(entity.getUid());

            if (checkList != null && checkList.size() > 0) {
                rowNumber++;
                System.out.println(
                        "A lab test with UID " + val + " already exists. Skipped the current row.");
                continue;
            }

            // Sample date
            LocalDateTime dateVal = ImportUtils.fromCellValue(row.getCell(3));
            entity.setSampleDate(dateVal);

            // Reason for testing
            val = row.getCell(4) != null ? formatter.formatCellValue(row.getCell(4)) : null;
            if (CommonUtils.isEmpty(val)) {
                System.out.println(
                        "Empty testing reason on row #"
                                + rowNumber
                                + " of the VL worksheet --> skipped the viral load record.");
                rowNumber++;
                continue;
            }

            if (val.equalsIgnoreCase("Tại thời điểm 6 tháng")) {
                entity.setReasonForTesting(LabTestReason.VL_AT_6MONTH);
            } else if (val.equalsIgnoreCase("Tại thời điểm 12 tháng")) {
                entity.setReasonForTesting(LabTestReason.VL_AT_12MONTH);
            } else if (val.equalsIgnoreCase("XN định kỳ sau 12 tháng")) {
                entity.setReasonForTesting(LabTestReason.VL_ROUTINE_12MONTH);
            } else if (val.equalsIgnoreCase("Có biểu hiện nghi ngờ TBĐT")) {
                entity.setReasonForTesting(LabTestReason.VL_FOLLOWUP_3MONTH);
            } else if (val.equalsIgnoreCase("Phụ nữ mang thai cho con bú")) {
                entity.setReasonForTesting(LabTestReason.VL_PREGNANCY);
            } else {
                System.out.println(
                        "Invalid testing reason on row #"
                                + rowNumber
                                + " of the VL worksheet --> skipped the viral load record.");
                rowNumber++;
                continue;
            }

            // Results
            dateVal = ImportUtils.fromCellValue(row.getCell(10));
            val = row.getCell(2) != null ? formatter.formatCellValue(row.getCell(2)) : null;
            if (dateVal != null && !CommonUtils.isEmpty(val)) {

                if (val.equalsIgnoreCase("< 20 copies/ml")) {
                    entity.setResultNumber(0l);
                    entity.setResultText("Không phát hiện");
                } else if (val.equalsIgnoreCase("20 - <200 copies/ml")) {
                    entity.setResultText("KPH - <200 bản sao/ml");
                } else if (val.equalsIgnoreCase("200 - <1000 copies/ml")) {
                    entity.setResultText("200 - <1000 bản sao/ml");
                } else if (val.equalsIgnoreCase(">1000 copies/ml")) {
                    entity.setResultText(">=1000 bản sao/ml");
                }

                if (!CommonUtils.isEmpty(entity.getResultText())) {
                    entity.setResultDate(dateVal);
                }
            }

            // Funding source
            val = row.getCell(12) != null ? formatter.formatCellValue(row.getCell(12)) : null;
            if (!CommonUtils.isEmpty(val)) {
                if (val.equalsIgnoreCase("Bảo hiểm y tế")) {
                    entity.setFundingSource(LabTestFundingSource.SHI);
                } else if (val.equalsIgnoreCase("Quỹ toàn cầu")) {
                    entity.setFundingSource(LabTestFundingSource.GF);
                } else if (val.equalsIgnoreCase("Tự chi trả")) {
                    entity.setFundingSource(LabTestFundingSource.SELF);
                } else {
                    entity.setFundingSource(LabTestFundingSource.OTHER);
                }
            } else {
                entity.setFundingSource(LabTestFundingSource.OTHER);
            }

            // Sample site
            entity.setSampleSite(opc.getName());

            // Note
            val = row.getCell(11) != null ? formatter.formatCellValue(row.getCell(11)) : null;
            if (!CommonUtils.isEmpty(val)) {
                entity.setNote(val);
            }

            entity.setTheCase(theCase);
            entity.setOrganization(opc);

            entity = labRepos.save(entity);

            rowNumber++;
        }

        System.out.println("END: Importing from viral load worksheet.");
        // -----------
        // endregion Step 2 - Import data from viral load sheet
        // -----------

        // -----------
        // region Step 3 - Import data from risk group sheet
        // -----------
        System.out.println("BEGIN: Importing from risk group worksheet...");

        final HashMap<String, String> codeMap = new HashMap<>();
        codeMap.put("SW", "risk_3");
        codeMap.put("PWID", "risk_1");
        codeMap.put("TG", "risk_4");
        codeMap.put("MSM", "risk_2");
        codeMap.put("PARTNER", "risk_6");
        codeMap.put("PWID_PARTNER", "risk_7");
        codeMap.put("PRISONER", "risk_5");
        codeMap.put("SW_CLIENT", "risk_8");

        QDictionary qDic = QDictionary.dictionary;

        rows = riskSheet.iterator();
        rowNumber = 1;

        while (rows.hasNext()) {
            Row row = rows.next();

            System.out.println("Processing row #" + rowNumber + " on risk group worksheet.");

            // Skip the first row (header)
            if (row.getRowNum() == 0) {
                rowNumber++;
                continue;
            }

            // Check exit point
            if (row.getCell(0) != null
                    && CommonUtils.isEmpty(formatter.formatCellValue(row.getCell(0)))) {
                break;
            }

            // Case UID
            String val = row.getCell(1) != null ? formatter.formatCellValue(row.getCell(1)) : null;
            if (CommonUtils.isEmpty(val)) {
                System.out.println(
                        "Invalid patient UID on row #"
                                + rowNumber
                                + " of the risk group worksheet --> skipped the risk group record.");
                rowNumber++;
                continue;
            }

            Case theCase = caseRepos.findOne(qCase.uid.eq(UUID.fromString(val)));
            if (theCase == null) {
                System.out.println(
                        "Could not find the corresponding patient for risk group on row #"
                                + rowNumber
                                + " --> skipped the risk group record.");
                rowNumber++;
                continue;
            }

            RiskInterview entity = new RiskInterview();

            val = row.getCell(0) != null ? formatter.formatCellValue(row.getCell(0)) : null;
            if (CommonUtils.isEmpty(val)) {
                System.out.println(
                        "Invalid UID on row #"
                                + rowNumber
                                + " of the risk group worksheet --> skipped the risk group record.");
                rowNumber++;
                continue;
            }
            entity.setUid(UUID.fromString(val));

            // Check if the risk group record with UID already exists.
            List<RiskInterview> checkList = riskRepos.findByUID(entity.getUid());
            if (checkList != null && checkList.size() > 0) {
                rowNumber++;
                System.out.println(
                        "A risk interview record with UID "
                                + val
                                + " already exists. Skipped the current row.");
                continue;
            }

            entity.setTheCase(theCase);
            entity.setOrganization(opc);

            // Interview date
            LocalDateTime dateVal = ImportUtils.fromCellValue(row.getCell(3));
            if (dateVal != null) {
                entity.setInterviewDate(dateVal);
            } else {
                System.out.println(
                        "Invalid interview date on row #"
                                + rowNumber
                                + " of the risk group worksheet --> skipped the risk group record.");
                rowNumber++;
                continue;
            }

            val = row.getCell(2) != null ? formatter.formatCellValue(row.getCell(2)) : null;
            String val2 = row.getCell(4) != null ? formatter.formatCellValue(row.getCell(4)) : null;

            if (CommonUtils.isEmpty(val) && CommonUtils.isEmpty(val2)) {
                entity.setRiskIdentified(false);
            } else {
                entity.setRiskIdentified(true);
                entity.setOtherRiskGroupText(val2);

                List<Dictionary> risks = new ArrayList<>();

                if (!CommonUtils.isEmpty(val)) {
                    String[] riskCodes = val.split("\\|");

                    for (String riskCode : riskCodes) {
                        String rc = null;

                        if (riskCode != null) {
                            rc = codeMap.get(riskCode);
                        }

                        if (rc == null) {
                            continue;
                        }

                        Dictionary risk = dicRepos.findOne(qDic.code.equalsIgnoreCase(rc));

                        if (risk != null) {
                            risks.add(risk);
                        }
                    }

                    entity.getRisks().clear();
                    entity.getRisks().addAll(risks);
                }
            }

            entity = riskRepos.save(entity);
            rowNumber++;
        }

        System.out.println("END: Importing from risk group worksheet.");

        // -----------
        // endregion Step 3 - Import data from risk group sheet
        // -----------

        // -----------
        // region Step 4 - Import data from SHI sheet
        // -----------

        System.out.println("BEGIN: Importing from shi worksheet...");

        String[] services = {
            "service_used_paidby_shi_3",
            "service_used_paidby_shi_6",
            "service_used_paidby_shi_4",
            "service_used_paidby_shi_5",
            "service_used_paidby_shi_7",
            "service_used_paidby_shi_2",
            "service_used_paidby_shi_1",
            "service_used_paidby_shi_8",
            "service_used_paidby_shi_9"
        };

        String[] occupations = {
            "Dưới 6 tuổi",
            "Học sinh/Sinh viên",
            "Nghỉ học",
            "Lực lượng vũ trang",
            "Công nhân viên chức",
            "Đi làm công ty có hợp đồng",
            "Làm nghề tự do",
            "Thất nghiệp",
            "Nghề khác"
        };

        String[] incomes = {
            "< 500.000 đồng",
            "500.000 - 1 triệu đồng",
            "> 1 - 2 triệu đồng",
            "> 2 - 4 triệu đồng",
            "> 4 - 10 triệu đồng",
            "> 10 triệu đồng"
        };

        String[] wealths = {
            "Hộ nghèo", "Cận nghèo", "Khó khăn được địa phương hỗ trợ", "Tình trạng khác"
        };

        rows = shiSheet.iterator();
        rowNumber = 1;

        String curFacilityName = "";

        while (rows.hasNext()) {
            Row row = rows.next();

            System.out.println("Processing row #" + rowNumber + " on shi worksheet.");

            // Skip the first row (header)
            if (row.getRowNum() == 0) {
                curFacilityName = formatter.formatCellValue(row.getCell(28));
                rowNumber++;
                continue;
            }

            // Check exit point
            if (row.getCell(0) != null
                    && CommonUtils.isEmpty(formatter.formatCellValue(row.getCell(0)))) {
                break;
            }

            // Case UID
            String val = row.getCell(1) != null ? formatter.formatCellValue(row.getCell(1)) : null;
            if (CommonUtils.isEmpty(val)) {
                System.out.println(
                        "Invalid the corresponding patient UID on row #"
                                + rowNumber
                                + " of the shi worksheet --> skipped the shi record.");
                rowNumber++;
                continue;
            }

            Case theCase = caseRepos.findOne(qCase.uid.eq(UUID.fromString(val)));
            if (theCase == null) {
                System.out.println(
                        "Could not find the corresponding patient with ID on row #"
                                + rowNumber
                                + " of the shi worksheet --> skipped the shi record.");
                rowNumber++;
                continue;
            }

            ShiInterview entity = new ShiInterview();
            val = row.getCell(0) != null ? formatter.formatCellValue(row.getCell(0)) : null;
            if (CommonUtils.isEmpty(val)) {
                System.out.println(
                        "Invalid record UID on row #"
                                + rowNumber
                                + " of the shi worksheet --> skipped the shi record.");
                rowNumber++;
                continue;
            }
            entity.setUid(UUID.fromString(val));

            // Check if the SHI record with UID already exist.
            List<ShiInterview> checkList = shiRepos.findByUID(entity.getUid());
            if (checkList != null && checkList.size() > 0) {
                rowNumber++;
                System.out.println(
                        "An SHI record with UID "
                                + val
                                + " already exists. Skipped the current row.");
                continue;
            }

            // Interview date
            LocalDateTime dateVal = ImportUtils.fromCellValue(row.getCell(2));
            if (dateVal != null) {
                entity.setInterviewDate(dateVal);
            } else {
                System.out.println(
                        "Invalid interview date on row #"
                                + rowNumber
                                + " of the shi worksheet --> skipped the shi record.");
                rowNumber++;
                continue;
            }

            // Has SHI card?
            val = row.getCell(3) != null ? formatter.formatCellValue(row.getCell(3)) : null;
            if (val == null) {
                System.out.println(
                        "Invalid indication of SHI card ownership on row #"
                                + rowNumber
                                + " of the shi worksheet --> skipped the shi record.");
                rowNumber++;
                continue;
            }

            if (val.equalsIgnoreCase("Có")) {

                entity.setHasShiCard(true);

                // SHI number
                String val2 =
                        row.getCell(4) != null ? formatter.formatCellValue(row.getCell(4)) : null;
                if (val2 == null || val2.trim().length() <= 0) {
                    System.out.println(
                            "Empty SHI card number on row #"
                                    + rowNumber
                                    + " of the shi worksheet --> skipped the shi record.");
                    rowNumber++;
                    continue;
                }

                val2 = val2.replace(" ", "");
                if (val2.length() != 15) {
                    System.out.println(
                            "Invalid SHI card number on row #"
                                    + rowNumber
                                    + " of the shi worksheet --> skipped the shi record.");
                    rowNumber++;
                    continue;
                }

                entity.setShiCardNumber(val2);

                // Expiration date
                dateVal = ImportUtils.fromCellValue(row.getCell(5));
                if (dateVal == null) {
                    System.out.println(
                            "Invalid expiration date on row #"
                                    + rowNumber
                                    + " of the shi worksheet --> skipped the shi record.");
                    rowNumber++;
                    continue;
                }

                entity.setShiExpiryDate(dateVal);

                // Primary care facility
                val = row.getCell(7) != null ? formatter.formatCellValue(row.getCell(7)) : null;
                if (CommonUtils.isEmpty(val)) {
                    System.out.println(
                            "Invalid primary care facility on row #"
                                    + rowNumber
                                    + " of the shi worksheet --> skipped the shi record.");
                    rowNumber++;
                    continue;
                }

                if (val.equalsIgnoreCase(curFacilityName)) {
                    // Primary care facility is this current facility
                    entity.setPrimaryCareFacility(opc);
                    entity.setPrimaryCareFacilityName(opc.getName());
                } else if (val.equalsIgnoreCase("Cơ sở khác")) {

                    val2 =
                            row.getCell(8) != null
                                    ? formatter.formatCellValue(row.getCell(8))
                                    : null;
                    if (CommonUtils.isEmpty(val2)) {
                        entity.setPrimaryCareFacilityName(val);
                    } else {
                        entity.setPrimaryCareFacilityName(val2);
                    }

                } else {
                    entity.setPrimaryCareFacilityName(val);
                }

                // Used SHI for ARV treatment?
                val = row.getCell(12) != null ? formatter.formatCellValue(row.getCell(12)) : null;

                if (!CommonUtils.isEmpty(val) && val.equalsIgnoreCase("TRUE")) {
                    entity.setUsedShiForArv(true);
                } else {
                    entity.setUsedShiForArv(false);
                }

                // Services used in the latest visit
                val = row.getCell(13) != null ? formatter.formatCellValue(row.getCell(13)) : null;
                String[] arrVal = {
                    "false", "false", "false", "false", "false", "false", "false", "false", "false"
                };

                if (val != null && val.trim().length() > 0) {
                    arrVal = val.split("\\|");
                }

                if (arrVal.length == 8) {
                    List<String> list = Lists.newArrayList(arrVal);
                    list.add("false");

                    arrVal = list.toArray(new String[0]);
                }

                List<Dictionary> usedServices = new ArrayList<>();
                for (int i = 0; i < arrVal.length; i++) {
                    boolean bool = Boolean.valueOf(arrVal[i]);

                    if (bool) {
                        Dictionary dic = dicRepos.findOne(qDic.code.equalsIgnoreCase(services[i]));

                        if (dic != null) {
                            usedServices.add(dic);
                        }
                    }
                }

                entity.getUsedShiServices().clear();
                entity.getUsedShiServices().addAll(usedServices);

                // Other services used and paid by SHI
                val = row.getCell(14) != null ? formatter.formatCellValue(row.getCell(14)) : null;
                if (!CommonUtils.isEmpty(val)) {
                    entity.setOtherUsedShiService(val);
                }

                // Right route?
                val = row.getCell(15) != null ? formatter.formatCellValue(row.getCell(15)) : null;
                if (!CommonUtils.isEmpty(val)) {
                    if (val.equalsIgnoreCase("Đúng tuyến")) {
                        entity.setShiRoute(1);
                    } else if (val.equalsIgnoreCase("Không đúng tuyến (nội tỉnh)")) {
                        entity.setShiRoute(2);
                    } else if (val.equalsIgnoreCase("Không đúng tuyến (tỉnh khác)")) {
                        entity.setShiRoute(3);
                    }
                }

                // Continuing facility
                val = row.getCell(18) != null ? formatter.formatCellValue(row.getCell(18)) : null;

                if (!CommonUtils.isEmpty(val)) {
                    if (val.equalsIgnoreCase(curFacilityName)) {
                        // Continuing facility is this current OPC
                        entity.setContinuingFacility(opc);
                        entity.setContinuingFacilityName(opc.getName());
                    } else if (val.equalsIgnoreCase("Cơ sở khác")) {
                        entity.setContinuingFacilityName(
                                row.getCell(19) != null
                                        ? formatter.formatCellValue(row.getCell(19))
                                        : val);
                    } else {
                        entity.setContinuingFacilityName(val);
                    }
                }

                // Nhu cau muon su dung SHI for ARV
                val = row.getCell(16) != null ? formatter.formatCellValue(row.getCell(16)) : null;
                val2 = row.getCell(17) != null ? formatter.formatCellValue(row.getCell(17)) : null;
                String val3 =
                        row.getCell(18) != null ? formatter.formatCellValue(row.getCell(18)) : null;

                if (entity.getContinuingFacility() != null
                        || "Tiếp tục điều trị tại cơ sở hiện tại".equalsIgnoreCase(val)) {
                    entity.setShiForArvPref(1);
                } else if ("Về đúng tuyến".equalsIgnoreCase(val)
                        || (!"Tỉnh khác".equalsIgnoreCase(val2)
                                && val3 != null
                                && !val3.equalsIgnoreCase(curFacilityName))) {
                    entity.setShiForArvPref(2);
                } else if ("Về đúng tỉnh".equalsIgnoreCase(val)
                        || ("Tỉnh khác".equalsIgnoreCase(val2))) {
                    entity.setShiForArvPref(3);
                }

            } else {
                // Don't have SHI card

                entity.setHasShiCard(false);

                // reasons?
                val = row.getCell(9) != null ? formatter.formatCellValue(row.getCell(9)) : null;
                String[] arrVal = {"false", "false", "false", "false", "false", "false", "false"};

                if (!CommonUtils.isEmpty(val)) {
                    arrVal = val.split("\\|");
                }

                List<Dictionary> reasons = new ArrayList<>();
                for (int i = 0; i < arrVal.length; i++) {
                    boolean bool = Boolean.valueOf(arrVal[i]);

                    if (bool) {
                        Dictionary dic =
                                dicRepos.findOne(
                                        qDic.code.equalsIgnoreCase("no_shi_reason_" + (i + 1)));

                        if (dic != null) {
                            reasons.add(dic);
                        }
                    }
                }

                entity.getNoShiReasons().clear();
                entity.getNoShiReasons().addAll(reasons);

                // Other reason
                val = row.getCell(10) != null ? formatter.formatCellValue(row.getCell(10)) : null;
                if (!CommonUtils.isEmpty(val)) {
                    entity.setOtherNoShiReason(val);
                }
            }

            // Want to use SHI for ARV?
            val = row.getCell(11) != null ? formatter.formatCellValue(row.getCell(11)) : null;
            if (!CommonUtils.isEmpty(val) && Boolean.valueOf(val)) {
                entity.setWantShiForArv(true);
            } else {
                entity.setWantShiForArv(false);
            }

            // Buy SHI next period?
            val = row.getCell(20) != null ? formatter.formatCellValue(row.getCell(20)) : null;
            if (!CommonUtils.isEmpty(val) && Boolean.valueOf(val)) {
                entity.setBuyShiNextQuarter(true);
            } else {
                entity.setBuyShiNextQuarter(false);
            }

            // Need support for SHI?
            val = row.getCell(21) != null ? formatter.formatCellValue(row.getCell(21)) : null;
            if (!CommonUtils.isEmpty(val) && Boolean.valueOf(val)) {
                entity.setNeedSupportForShi(true);
            } else {
                entity.setNeedSupportForShi(false);
            }

            // Support details?
            val = row.getCell(24) != null ? formatter.formatCellValue(row.getCell(24)) : null;
            if (!CommonUtils.isEmpty(val)) {
                entity.setNeedSupportDetails(val);
            }

            // ARV treatment prefs if not using SHI?
            val = row.getCell(22) != null ? formatter.formatCellValue(row.getCell(22)) : null;
            if (!CommonUtils.isEmpty(val)) {

                entity.setArvTreatmentPrefName(val);

                if (val.equalsIgnoreCase("Điều trị ARV tự túc tại cơ sở công")) {
                    entity.setArvTreatmentPref(1);
                } else if (val.equalsIgnoreCase("Điều trị ARV tự túc tại cơ sở tư")) {
                    entity.setArvTreatmentPref(2);
                } else if (val.equalsIgnoreCase("Tự tìm phương án điều trị ARV")) {
                    entity.setArvTreatmentPref(3);
                } else if (val.equalsIgnoreCase("Hình thức khác")) {
                    entity.setArvTreatmentPref(4);

                    String val2 =
                            row.getCell(23) != null
                                    ? formatter.formatCellValue(row.getCell(23))
                                    : null;
                    if (!CommonUtils.isEmpty(val2)) {
                        entity.setArvTreatmentPrefName(val2);
                    }
                }
            }

            // Residential status & other information
            val = row.getCell(1) != null ? formatter.formatCellValue(row.getCell(1)) : null;
            Iterator<Row> patientRows = patientSheet.iterator();
            while (patientRows.hasNext()) {
                Row prow = patientRows.next();

                if (prow.getRowNum() == 0) {
                    continue;
                }

                if (prow.getCell(0) != null
                        && val.equalsIgnoreCase(formatter.formatCellValue(prow.getCell(0)))) {

                    // Set data for entity
                    // -->
                    // residential status
                    String val2 =
                            prow.getCell(20) != null
                                    ? formatter.formatCellValue(prow.getCell(20))
                                    : null;
                    if (!CommonUtils.isEmpty(val2)) {
                        if (val2.equalsIgnoreCase("Thường trú có hộ khẩu (KT1)")) {
                            entity.setResidentStatus(1);
                        } else if (val2.equalsIgnoreCase("Tạm trú có đăng ký (KT3, KT2)")) {
                            entity.setResidentStatus(2);
                        } else if (val2.equalsIgnoreCase("Tạm trú không đăng ký")) {
                            entity.setResidentStatus(3);
                        }
                    }

                    // Occupation
                    val2 =
                            prow.getCell(21) != null
                                    ? formatter.formatCellValue(prow.getCell(21))
                                    : null;
                    if (!CommonUtils.isEmpty(val2)) {
                        for (int i = 0; i < occupations.length; i++) {
                            if (val2.trim().equalsIgnoreCase(occupations[i])) {
                                entity.setOccupation(i + 1);
                                entity.setOccupationName(occupations[i]);
                                break;
                            }
                        }
                    }

                    // Occupation (other)
                    val2 =
                            prow.getCell(22) != null
                                    ? formatter.formatCellValue(prow.getCell(22))
                                    : null;
                    if (!CommonUtils.isEmpty(val2)) {
                        entity.setOccupationName(val2);
                    }

                    // Monthly income
                    val2 =
                            prow.getCell(23) != null
                                    ? formatter.formatCellValue(prow.getCell(23))
                                    : null;
                    if (!CommonUtils.isEmpty(val2)) {
                        for (int i = 0; i < incomes.length; i++) {
                            if (val2.trim().equalsIgnoreCase(incomes[i])) {
                                entity.setMonthlyIncome(i + 1);
                                break;
                            }
                        }
                    }

                    // Wealth status
                    val2 =
                            prow.getCell(24) != null
                                    ? formatter.formatCellValue(prow.getCell(24))
                                    : null;
                    if (!CommonUtils.isEmpty(val2)) {
                        for (int i = 0; i < wealths.length; i++) {
                            if (val2.trim().equalsIgnoreCase(wealths[i])) {
                                entity.setWealthStatus(i + 1);
                                entity.setWealthStatusName(val2);
                                break;
                            }
                        }
                    }

                    // Wealth status name (other)
                    val2 =
                            prow.getCell(25) != null
                                    ? formatter.formatCellValue(prow.getCell(25))
                                    : null;
                    if (!CommonUtils.isEmpty(val2)) {
                        entity.setWealthStatusName(val2);
                    }

                    // -> time to] exit the patient sheet

                    break;
                }
            }

            entity.setTheCase(theCase);
            entity.setOrganization(opc);

            List<ShiInterview> existingInstances =
                    shiRepos.findInstance(theCase.getId(), entity.getInterviewDate());
            if (existingInstances != null && existingInstances.size() > 0) {
                System.out.println(
                        "Skipping row "
                                + rowNumber
                                + " because the case already has an interview on the same date.");
                rowNumber++;
                continue;
            }

            entity = shiRepos.save(entity);
            rowNumber++;
        }

        System.out.println("END: Importing from shi worksheet.");
        // -----------
        // endregion Step 4 - Import data from SHI sheet
        // -----------
    }

    /**
     * Many OPCs did not capture the MMD status while still prescribing multiple month of ARV for
     * patients
     */
    @Transactional(rollbackFor = Exception.class)
    public void correctMMDBasedOnDrugDays() {
        // select all
    }
}
