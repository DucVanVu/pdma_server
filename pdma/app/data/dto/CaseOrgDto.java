package org.pepfar.pdma.app.data.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.pepfar.pdma.app.data.domain.*;
import org.pepfar.pdma.app.data.types.EnrollmentType;
import org.pepfar.pdma.app.data.types.PatientStatus;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.CustomLocalDateTimeDeserializer2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class CaseOrgDto extends AuditableEntityDto {

    private Long id;

    private UUID uid;

    private CaseDto theCase;

    private OrganizationDto organization;

    private String organizationName;

    private String patientChartId;

    private PatientStatus prevStatus;

    private PatientStatus status;

    private Integer enrollmentStatus;

    private boolean latestRelationship;

    private EnrollmentType enrollmentType;

    private String arvGroup;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
    private LocalDateTime arvStartDate;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
    private LocalDateTime startDate;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
    private LocalDateTime endDate;

    private String endingReason;

    private Boolean current;

    private boolean refTrackingOnly;

    private String note;

    // Check if when a case is transferred out, the sending facility is able to
    // update the referral status
    private boolean updateable;

    // Check if when a case was transferred out and after sometime return to the
    // current OPC for continuum of care
    private boolean reEnrollable;

    // Check if the case org is editable (TRUE when the current logged in user is a
    // site manager and is granted EDIT access to the org in the case-org record)
    private boolean editable;

    // Check if the case org is deletable (TRUE when the current logged in user is a
    // site manager and is granted EDIT access to the org in the case-org record,
    // and the case-org record is not the latest one.
    private boolean deletable;

    // Check if the case record is editable (TRUE if the case is not deleted AND
    // (the associated STATUS is active OR there is only one record in the status
    // history), or the current case-org is the second one, and the first one is for
    // reference only (1st[ref=1] --> 2nd[current record] --> ....).
    private boolean caseEditable;

    public CaseOrgDto() {}

    public CaseOrgDto(CaseOrg entity, boolean needDetails) {
        super(entity);

        if (entity == null) {
            return;
        }

        id = entity.getId();
        uid = entity.getUid();
        organizationName = entity.getOrganizationName();
        patientChartId = entity.getPatientChartId();
        arvStartDate = entity.getArvStartDate();
        startDate = entity.getStartDate();
        endDate = entity.getEndDate();
        endingReason = entity.getEndingReason();
        prevStatus = entity.getPrevStatus();
        status = entity.getStatus();
        enrollmentStatus = entity.getEnrollmentStatus();
        latestRelationship = entity.isLatestRelationship();
        enrollmentType = entity.getEnrollmentType();
        arvGroup = entity.getArvGroup();
        current = entity.getCurrent();
        refTrackingOnly = entity.isRefTrackingOnly();
        note = entity.getNote();

        if (entity.getTheCase() != null) {
            Case c = entity.getTheCase();

            theCase = new CaseDto();
            theCase.setId(c.getId());
            theCase.setArvStartDate(c.getArvStartDate());
            theCase.setHivScreenDate(c.getHivScreenDate());
            theCase.setHivConfirmDate(c.getHivConfirmDate());
            theCase.setHivInfoId(c.getHivInfoID());
            theCase.setConfirmLab(new HIVConfirmLabDto(c.getConfirmLab()));
            theCase.setConfirmLabName(c.getConfirmLabName());
            theCase.setSecondLineStartDate(c.getSecondLineStartDate());
            theCase.setCurrentArvRegimen(new RegimenDto(c.getCurrentArvRegimen()));
            theCase.setCurrentArvRegimenLine(c.getCurrentArvRegimenLine());
            theCase.setCurrentArvRegimenName(c.getCurrentArvRegimenName());
            theCase.setCurrentArvRegimenStartDate(c.getCurrentArvRegimenStartDate());
            theCase.setDeleted(c.isDeleted());
            theCase.setPerson(new PersonDto(c.getPerson(), false));
            theCase.setWhoStage(c.getWhoStage());
            theCase.setWhoStageEvalDate(c.getWhoStageEvalDate());
            theCase.setModifyDate(c.getModifyDate());

            // get max 6 VL & 6 CD4
            int vlCount = 0;
            int cd4Count = 0;

            List<LabTest> tests = c.getLabTests().parallelStream().collect(Collectors.toList());
            for (LabTest test : tests) {

                if (vlCount >= 6 && cd4Count >= 6) {
                    break;
                }

                switch (test.getTestType()) {
                    case VIRAL_LOAD:
                        if (vlCount < 6) {
                            LabTestDto testDto = new LabTestDto(test);
                            testDto.setTheCase(null);
                            testDto.setOrganization(null);

                            theCase.getLabTests().add(testDto);
                        }

                        vlCount++;
                        break;

                    case CD4:
                        if (cd4Count < 6) {
                            LabTestDto testDto = new LabTestDto(test);
                            testDto.setTheCase(null);
                            testDto.setOrganization(null);

                            theCase.getLabTests().add(testDto);
                        }

                        cd4Count++;
                        break;

                    case ARV_DR:
                        LabTestDto testDto = new LabTestDto(test);
                        theCase.getLabTests().add(testDto);
                        break;

                    default:
                        break;
                }
            }

            if (!CommonUtils.isEmpty(c.getWhoStages())) {
                Iterator<ClinicalStage> itr = c.getWhoStages().iterator();
                int stageCount = 0;

                while (itr.hasNext()) {
                    if (stageCount > 6) {
                        break;
                    }

                    ClinicalStage e = itr.next();
                    ClinicalStageDto dto = new ClinicalStageDto(e);
                    dto.setTheCase(null);
                    dto.setOrganization(null);

                    theCase.getWhoStages().add(dto);
                    stageCount++;
                }
            }

            if (!CommonUtils.isEmpty(c.getMmdEvals())) {
                Iterator<MMDispensing> itr = c.getMmdEvals().iterator();

                MMDispensing e = null;
                while (itr.hasNext()) {
                    e = itr.next();

                    if (!CommonUtils.isTrue(e.getDeleted())) {
                        break;
                    }
                }

                if (e != null) {
                    MMDispensingDto dto = new MMDispensingDto();

                    dto.setId(e.getId());
                    dto.setEvaluationDate(e.getEvaluationDate());
                    dto.setAdult(e.isAdult());
                    dto.setArvGt12Month(e.isArvGt12Month());
                    dto.setVlLt200(e.isVlLt200());
                    dto.setNoOIs(e.isNoOIs());
                    dto.setNoDrugAdvEvent(e.isNoDrugAdvEvent());
                    dto.setNoPregnancy(e.isNoPregnancy());
                    dto.setGoodAdherence(e.isGoodAdherence());
                    dto.setEligible(e.isEligible());
                    dto.setOnMmd(e.isOnMmd());
                    dto.setStopReason(e.getStopReason());

                    theCase.getMmdEvals().add(dto);
                }
            }

            // latest appointment
            Iterator<Appointment> apps = c.getAppointments().iterator();
            LocalDateTime todayStart = CommonUtils.hanoiTodayStart();
            while (apps.hasNext()) {
                Appointment app = apps.next();
                LocalDateTime appDate = app.getAppointmentDate();

                if (todayStart.isEqual(appDate) || todayStart.isAfter(appDate)) {
                    AppointmentDto appDto = new AppointmentDto();
                    appDto.setId(app.getId());
                    appDto.setAppointmentDate(appDate);

                    theCase.getAppointments().clear();
                    theCase.getAppointments().add(appDto);
                    break;
                }
            }

            // latest treatment
            Iterator<Treatment> txs = c.getTreatments().iterator();
            if (txs.hasNext()) {
                Treatment tx = txs.next();
                TreatmentDto txDto = new TreatmentDto();
                txDto.setId(tx.getId());
                if (tx.getRegimen() != null) {
                    txDto.setRegimen(new RegimenDto(tx.getRegimen()));
                }
                txDto.setRegimenName(tx.getRegimenName());
                txDto.setRegimenLine(tx.getRegimenLine());
                txDto.setStartDate(tx.getStartDate());
                txDto.setEndDate(tx.getEndDate());
                txDto.setEndingReason(tx.getEndingReason());

                theCase.getTreatments().clear();
                theCase.getTreatments().add(txDto);
            }

            if (needDetails) {
                // Latest SHI interview
                if (!CommonUtils.isEmpty(c.getShiInterviews())) {
                    Iterator<ShiInterview> itr = c.getShiInterviews().iterator();

                    if (itr.hasNext()) {
                        ShiInterview e = itr.next();
                        ShiInterviewDto dto = new ShiInterviewDto();

                        dto.setId(e.getId());
                        dto.setHasShiCard(e.getHasShiCard());
                        dto.setShiCardNumber(e.getShiCardNumber());
                        dto.setShiExpiryDate(e.getShiExpiryDate());
                        dto.setUsedShiForArv(e.getUsedShiForArv());
                        dto.setInterviewDate(e.getInterviewDate());

                        theCase.getShiInterviews().add(dto);
                    }
                }

                // Latest risk assessment
                if (!CommonUtils.isEmpty(c.getRiskInterviews())) {
                    Iterator<RiskInterview> itr = c.getRiskInterviews().iterator();

                    if (itr.hasNext()) {
                        RiskInterview e = itr.next();
                        RiskInterviewDto dto = new RiskInterviewDto();

                        dto.setId(e.getId());
                        dto.setInterviewDate(e.getInterviewDate());
                        dto.setRiskIdentified(e.getRiskIdentified());
                        dto.setOtherRiskGroupText(e.getOtherRiskGroupText());

                        List<DictionaryDto> risks = new ArrayList<>();
                        for (Dictionary r : e.getRisks()) {
                            risks.add(new DictionaryDto(r));
                        }

                        dto.getRisks().addAll(risks);
                        theCase.getRiskInterviews().add(dto);
                    }
                }

                // TB treatments
                if (!CommonUtils.isEmpty(c.getTbtxs())) {
                    Iterator<TBTreatment> tbTxs = c.getTbtxs().iterator();

                    if (tbTxs.hasNext()) {
                        TBTreatment e = tbTxs.next();
                        TBTreatmentDto dto = new TBTreatmentDto();

                        dto.setId(e.getId());
                        dto.setTestDate(e.getTestDate());
                        dto.setTxStartDate(e.getTxStartDate());
                        dto.setTxEndDate(e.getTxEndDate());
                        dto.setFacilityName(e.getFacilityName());

                        theCase.getTbtxs().add(dto);
                    }
                }

                // TB prophylaxis
                if (!CommonUtils.isEmpty(c.getTbpros())) {
                    Iterator<TBProphylaxis> tbPros = c.getTbpros().iterator();

                    if (tbPros.hasNext()) {
                        TBProphylaxis e = tbPros.next();
                        TBProphylaxisDto dto = new TBProphylaxisDto();

                        dto.setId(e.getId());
                        dto.setStartDate(e.getStartDate());
                        dto.setEndDate(e.getEndDate());
                        dto.setResult(e.getResult());

                        theCase.getTbpros().add(dto);
                    }
                }

                // Hepatitis
                if (!CommonUtils.isEmpty(c.getHepatitises())) {
                    Iterator<Hepatitis> heps = c.getHepatitises().iterator();

                    while (heps.hasNext()) {
                        Hepatitis e = heps.next();
                        HepatitisDto dto = new HepatitisDto();

                        dto.setId(e.getId());
                        dto.setTestType(e.getTestType());
                        dto.setTestDate(e.getTestDate());
                        dto.setTestPositive(e.isTestPositive());

                        theCase.getHepatitises().add(dto);
                    }
                }

                // MMT
                if (c.getMmt() != null) {
                    MMT e = c.getMmt();
                    MMTDto dto = new MMTDto();

                    dto.setId(e.getId());
                    dto.setStartDate(e.getStartDate());
                    dto.setEndDate(e.getEndDate());

                    theCase.setMmt(dto);
                }

                // latest pregnancy
                if (!CommonUtils.isEmpty(c.getPregnancies())) {
                    Iterator<Pregnancy> itr = c.getPregnancies().iterator();

                    if (itr.hasNext()) {
                        Pregnancy e = itr.next();
                        PregnancyDto dto = new PregnancyDto();

                        dto.setId(e.getId());
                        dto.setPregResult(e.getPregResult());
                        dto.setDueDate(e.getDueDate());
                        dto.setChildDob(e.getChildDob());

                        theCase.getPregnancies().add(dto);
                    }
                }
            }
        }

        if (entity.getOrganization() != null) {
            organization = new OrganizationDto();
            organization.setId(entity.getOrganization().getId());
            organization.setCode(entity.getOrganization().getCode());
            organization.setName(entity.getOrganization().getName());
            if (entity.getOrganization().getAddress() != null
                    && entity.getOrganization().getAddress().getProvince() != null
                    && entity.getOrganization().getAddress().getProvince().getName() != null) {
                organization.setProvinceName(
                        entity.getOrganization().getAddress().getProvince().getName());
            }
        }
    }

    public CaseOrg toEntity() {

        CaseOrg entity = new CaseOrg();
        entity = (CaseOrg) super.toEntity(entity);

        entity.setId(id);
        entity.setOrganizationName(organizationName);
        entity.setPatientChartId(patientChartId);
        entity.setArvStartDate(arvStartDate);
        entity.setStartDate(startDate);
        entity.setEndDate(endDate);
        entity.setEndingReason(endingReason);
        entity.setPrevStatus(prevStatus);
        entity.setStatus(status);
        entity.setEnrollmentStatus(enrollmentStatus);
        entity.setLatestRelationship(latestRelationship);
        entity.setEnrollmentType(enrollmentType);
        entity.setArvGroup(arvGroup);
        entity.setCurrent(current);
        entity.setRefTrackingOnly(refTrackingOnly);
        entity.setNote(note);

        if (theCase != null) {
            entity.setTheCase(theCase.toEntity());
        }

        if (organization != null) {
            entity.setOrganization(organization.toEntity());
        }

        return entity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CaseDto getTheCase() {
        return theCase;
    }

    public void setTheCase(CaseDto theCase) {
        this.theCase = theCase;
    }

    public OrganizationDto getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationDto organization) {
        this.organization = organization;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getPatientChartId() {
        return patientChartId;
    }

    public void setPatientChartId(String patientChartId) {
        this.patientChartId = patientChartId;
    }

    public LocalDateTime getArvStartDate() {
        return arvStartDate;
    }

    public void setArvStartDate(LocalDateTime arvStartDate) {
        this.arvStartDate = arvStartDate;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public String getEndingReason() {
        return endingReason;
    }

    public void setEndingReason(String endingReason) {
        this.endingReason = endingReason;
    }

    public PatientStatus getPrevStatus() {
        return prevStatus;
    }

    public void setPrevStatus(PatientStatus prevStatus) {
        this.prevStatus = prevStatus;
    }

    public PatientStatus getStatus() {
        return status;
    }

    public void setStatus(PatientStatus status) {
        this.status = status;
    }

    public Integer getEnrollmentStatus() {
        return enrollmentStatus;
    }

    public void setEnrollmentStatus(Integer enrollmentStatus) {
        this.enrollmentStatus = enrollmentStatus;
    }

    public boolean isLatestRelationship() {
        return latestRelationship;
    }

    public void setLatestRelationship(boolean latestRelationship) {
        this.latestRelationship = latestRelationship;
    }

    public boolean isRefTrackingOnly() {
        return refTrackingOnly;
    }

    public void setRefTrackingOnly(boolean refTrackingOnly) {
        this.refTrackingOnly = refTrackingOnly;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public EnrollmentType getEnrollmentType() {
        return enrollmentType;
    }

    public void setEnrollmentType(EnrollmentType enrollmentType) {
        this.enrollmentType = enrollmentType;
    }

    public String getArvGroup() {
        return arvGroup;
    }

    public void setArvGroup(String arvGroup) {
        this.arvGroup = arvGroup;
    }

    public Boolean getCurrent() {
        return current;
    }

    public void setCurrent(Boolean current) {
        this.current = current;
    }

    public boolean isUpdateable() {
        return updateable;
    }

    public void setUpdateable(boolean updateable) {
        this.updateable = updateable;
    }

    public boolean isReEnrollable() {
        return reEnrollable;
    }

    public void setReEnrollable(boolean reEnrollable) {
        this.reEnrollable = reEnrollable;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean isDeletable() {
        return deletable;
    }

    public void setDeletable(boolean deletable) {
        this.deletable = deletable;
    }

    public boolean isCaseEditable() {
        return caseEditable;
    }

    public void setCaseEditable(boolean caseEditable) {
        this.caseEditable = caseEditable;
    }

    public UUID getUid() {
        return uid;
    }
}
