package org.pepfar.pdma.app.data.service;

import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.pepfar.pdma.app.data.domain.CaseOrg;
import org.pepfar.pdma.app.data.dto.CaseDeleteFilterDto;
import org.pepfar.pdma.app.data.dto.CaseDto;
import org.pepfar.pdma.app.data.dto.CaseDto4Search;
import org.pepfar.pdma.app.data.dto.CaseFilterDto;
import org.pepfar.pdma.app.data.dto.CaseOrgDto;
import org.pepfar.pdma.app.data.dto.CaseOrgUpdateDto;
import org.pepfar.pdma.app.data.dto.CaseReferralResultDto;
import org.pepfar.pdma.app.data.dto.CheckNationalIdDto;
import org.pepfar.pdma.app.data.dto.OPCDashboardFilterDto;
import org.springframework.data.domain.Page;

public interface CaseService {

    public CaseOrgDto findByCaseOrgId(Long id);

    public Page<CaseOrgDto> findAllPageable(CaseFilterDto filter);

    public Page<CaseDto4Search> findAll4Appointment(CaseFilterDto filter);

    public Workbook exportReferralSheet(Long caseOrgId, String urlPrefix);

    public Workbook exportData(CaseFilterDto filter);

    public Workbook exportSearchResults(CaseFilterDto filter);

    public List<CaseOrgDto> getCaseStatusHistory(Long caseOrgId);

    public int hivInfoIdExists(CaseOrgDto dto);

    public CheckNationalIdDto nationalIdExists(CaseOrgDto dto);

    public CaseOrgDto patientChartIdExists(CaseOrgDto dto);

    public CaseOrgDto patientRecordExists(CaseOrgDto dto);

    public CaseOrgDto saveOne(CaseOrgDto dto);

    public CaseDto updateHivInfoID(CaseDto dto);

    public CaseDto removeHivInfoID(CaseDto dto);

    public CaseOrgDto softDelete(CaseOrgDto dto);

    public CaseOrgDto restoreCase(CaseOrgDto dto);

    public CaseOrgDto findCaseOrgById(Long id);

    public CaseOrgDto updateCaseOrg(CaseOrgDto dto);

    public void deleteCaseOrg(CaseOrgDto dto);

    public int updateTreatmentStatus(CaseOrgUpdateDto dto);

    public boolean cancelReferral(CaseReferralResultDto dto);

    public boolean updateReferralResult(CaseReferralResultDto dto);

    public boolean reEnrollPatient(CaseOrgUpdateDto dto);

    public void updateOldNewCaseStatus();

    public void deleteMultiple(CaseDto[] dtos);

    public void deleteByOrganization(CaseDeleteFilterDto filter);

    public void keepCaseEntityUp2Date();

    public String createRedirectPath(Long caseId);

    public Workbook exportListPatientOnMMD(OPCDashboardFilterDto filter);

    public Workbook exportListPatientTLD(OPCDashboardFilterDto filter);

    public Workbook exportListPatientRiskGroup(OPCDashboardFilterDto filter);

    public Workbook exportListPatientTBScreening(OPCDashboardFilterDto filter);

    /// custom export for ad hoc data request
    public Workbook exportListOfPatients();

    public CaseOrgDto checkCaseOrgEditable(List<Long> writableOrgIds, CaseOrg entity, boolean needDetails);
}
