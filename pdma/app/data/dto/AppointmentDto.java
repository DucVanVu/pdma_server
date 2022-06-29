package org.pepfar.pdma.app.data.dto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.UUID;

import org.assertj.core.util.Sets;
import org.pepfar.pdma.app.data.domain.Appointment;
import org.pepfar.pdma.app.data.domain.Case;
import org.pepfar.pdma.app.data.domain.CaseOrg;
import org.pepfar.pdma.app.data.domain.ClinicalStage;
import org.pepfar.pdma.app.data.domain.Hepatitis;
import org.pepfar.pdma.app.data.domain.LabTest;
import org.pepfar.pdma.app.data.domain.MMDispensing;
import org.pepfar.pdma.app.data.domain.ShiInterview;
import org.pepfar.pdma.app.data.types.ARVFundingSource;
import org.pepfar.pdma.app.data.types.ClinicalTestingType;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.CustomLocalDateTimeDeserializer2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class AppointmentDto extends AuditableEntityDto {

    private Long id;

    private UUID uid;

    private OrganizationDto organization;

    private CaseDto theCase;

    // added the current case org the appointment is associated with
    private CaseOrgDto currentCaseOrg;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
    private LocalDateTime appointmentDate;

    // Bệnh nhân có tới không?
    private Boolean arrived;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
    private LocalDateTime arrivalDate;

    private Boolean missed;

    // Có cấp thuốc không?
    private Boolean drugDispensed;

    // Có làm xét nghiệm CD4 không?
    private Boolean cd4Tested;

    // Có làm xét nghiệm TLVR không?
    private Boolean vlTested;

    // Có làm xét nghiệm kháng thuốc ARV không?
    private Boolean arvDrTested;

    // K/qua sàng lọc lao
    private Integer tbScreenResult;

    // Có sàng lọc viêm gan không?
    private Boolean hepScreened;

    // Có thay đổi thông tin thẻ bảo hiểm y tế không?
    private Boolean shiChanged;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
    private LocalDateTime nextAppointmentDate;

    private Integer drugDays;

    private ARVFundingSource drugSource;

    private ARVFundingSource drugSourceAlt;

    private String arvRegimenName;

    private Integer arvRegimenLine;

    private Boolean goodAdherence;

    private Boolean hasOI;

    private Boolean hasDrugAE;

    private Boolean pregnant;

    private String note;

    // ------------------------
    // For display and view manipulation only
    // ------------------------

    // so ngay muon kham tu lan hen kham cuoi
    private int lateDays;

    // lan kham cuoi
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
    private LocalDateTime latestArrivalDate;

    private LabTestDto latestVlTest;

    // Clinical stage associated with the encounter
    private ClinicalStageDto clinicalStage;

    // Viral load test associated with the encounter
    private LabTestDto vlTest;

    // CD4 test associated with the encounter
    private LabTestDto cd4Test;

    // Hepatitis data associated with the encounter
    private HepatitisDto hep;

    // SHI interview data associated with the encounter
    private ShiInterviewDto shi;

    // Latest SHI interview data
    private ShiInterviewDto prevShi;

    // Auto generate next appointment
    // should auto generate the next appointment?
    private boolean autoGenNextAppointment;

    // -------------------
    // MMD
    // -------------------
    // Check age, viral load/CD4 result and ARV start date
    private boolean mmdPartiallyEligible;

    // MMD evaluation associated with this encounter
    private MMDispensingDto mmdEval;

    // MMD evaluation for the previous encounter
    private MMDispensingDto prevMmdEval;

    // Drug source for the previous encounter
    private ARVFundingSource prevDrugSource;

    // TBTreatment(điều trị lao)
    private TBTreatment2Dto tbtx2;

    // TBProphylaxis(dự phòng lao)
    private TBProphylaxis2Dto tbpro2;

    public AppointmentDto() {

    }

    /**
     * Short version of appointment
     *
     * @param id
     * @param appDate
     * @param caseId
     * @param fullname
     */
    public AppointmentDto(Long id, LocalDateTime appDate, Long caseId, String fullname) {
        this.id = id;
        this.appointmentDate = appDate;

        this.theCase = new CaseDto();
        this.theCase.setId(caseId);

        PersonDto person = new PersonDto();
        person.setFullname(fullname);
        this.theCase.setPerson(person);
    }

    public AppointmentDto(Appointment entity) {
        super(entity);

        if (entity == null) {
            return;
        }

        this.id = entity.getId();
        this.uid = entity.getUid();
        this.appointmentDate = entity.getAppointmentDate();
        this.arrived = entity.getArrived();
        this.arrivalDate = entity.getArrivalDate();
        this.missed = entity.getMissed();
        this.drugDispensed = entity.getDrugDispensed();
        this.cd4Tested = entity.getCd4Tested();
        this.vlTested = entity.getVlTested();
        this.arvDrTested = entity.getArvDrTested();
        this.tbScreenResult = entity.getTbScreenResult();
        this.hepScreened = entity.getHepScreened();
        this.shiChanged = entity.getShiChanged();
        this.nextAppointmentDate = entity.getNextAppointmentDate();
        this.drugDays = entity.getDrugDays();
        this.drugSource = entity.getDrugSource();
        this.drugSourceAlt = entity.getDrugSourceAlt();
        this.arvRegimenName = entity.getArvRegimenName();
        this.arvRegimenLine = entity.getArvRegimenLine();
        this.goodAdherence = entity.getGoodAdherence();
        this.hasOI = entity.getHasOI();
        this.hasDrugAE = entity.getHasDrugAE();
        this.pregnant = entity.getPregnant();
        this.note = entity.getNote();

        if (entity.getTheCase() != null) {
            Case c = entity.getTheCase();

            Iterator<CaseOrg> cos = c.getCaseOrgs().iterator();
            CaseOrg co = null;
            if (entity.getOrganization() != null) {
                while (cos.hasNext()) {
                    CaseOrg _co = cos.next();

                    if (_co.getOrganization().getId().longValue() == entity.getOrganization().getId()) {
                        co = _co;
                        break;
                    }
                }
            }

            this.theCase = new CaseDto();
            if (co != null) {
                this.theCase.setCaseOrgs(Sets.newLinkedHashSet(new CaseOrgDto(co, false)));
            } else {
                this.theCase.setCaseOrgs(Sets.newLinkedHashSet());
            }
            this.theCase.setId(c.getId());
            this.theCase.setUid(c.getUid());
            this.theCase.setCurrentArvRegimenName(c.getCurrentArvRegimenName());
            this.theCase.setArvStartDate(c.getArvStartDate());
            this.theCase.setPerson(new PersonDto(c.getPerson(), false));

            // Latest arrival date?
            this.latestArrivalDate = null;
            Iterator<Appointment> apps = c.getAppointments().iterator();

            while (apps.hasNext()) {
                Appointment app = apps.next();

                if (app.getArrivalDate() != null && this.latestArrivalDate == null) {
                    this.latestArrivalDate = app.getArrivalDate();
                }
            }

            // Latest vl test?
            Iterator<LabTest> allVlTests = c.getLabTests().parallelStream()
                    .filter(lt -> lt.getTestType() == ClinicalTestingType.VIRAL_LOAD).iterator();
            if (allVlTests.hasNext()) {
                LabTest lt = allVlTests.next();

                this.latestVlTest = new LabTestDto(lt);
            }

            if (arrivalDate != null) {

                // Clinical stage associated with the encounter
                Iterator<ClinicalStage> allCStages = c.getWhoStages().iterator();
                while (allCStages.hasNext()) {
                    ClinicalStage cs = allCStages.next();

                    if (CommonUtils.dateDiff(ChronoUnit.DAYS, cs.getEvalDate(), arrivalDate) == 0) {
                        this.clinicalStage = new ClinicalStageDto(cs);
                        break;
                    }
                }

                // Viral load test associated with the appointment
                if (latestVlTest != null && latestVlTest.getSampleDate() != null
                        && CommonUtils.dateDiff(ChronoUnit.DAYS, latestVlTest.getSampleDate(), arrivalDate) == 0) {
                    this.vlTest = this.latestVlTest;
                } else {
                    while (allVlTests.hasNext()) {
                        LabTest lt = allVlTests.next();

                        if (lt.getSampleDate().isEqual(arrivalDate)) {
                            this.vlTest = new LabTestDto(lt);
                            break;
                        }
                    }
                }

                // CD4 test
                Iterator<LabTest> allCD4Tests = c.getLabTests().parallelStream()
                        .filter(lt -> lt.getTestType() == ClinicalTestingType.CD4).iterator();
                while (allCD4Tests.hasNext()) {
                    LabTest lt = allCD4Tests.next();

                    if (CommonUtils.dateDiff(ChronoUnit.DAYS, lt.getSampleDate(), arrivalDate) == 0) {
                        this.cd4Test = new LabTestDto(lt);
                        break;
                    }
                }

                // Hep data associated with this encounter
                Iterator<Hepatitis> allHepData = c.getHepatitises().iterator();
                while (allHepData.hasNext()) {
                    Hepatitis _hep = allHepData.next();

                    if (_hep.getTestDate() != null
                            && CommonUtils.dateDiff(ChronoUnit.DAYS, _hep.getTestDate(), arrivalDate) == 0) {
                        this.hep = new HepatitisDto(_hep);
                        break;
                    }
                }

                // SHI data associated with this encounter
                Iterator<ShiInterview> allShiData = c.getShiInterviews().iterator();
                while (allShiData.hasNext()) {
                    ShiInterview _shi = allShiData.next();

                    // if the current encounter does not capture SHI data change -> get the
                    // previously captured SHI data
                    if (_shi.getInterviewDate() != null
                            && CommonUtils.dateDiff(ChronoUnit.DAYS, _shi.getInterviewDate(), arrivalDate) >= 0) {
                        this.shi = new ShiInterviewDto(_shi);
                        break;
                    }
                }

                // MMD data
                Iterator<MMDispensing> mmds = c.getMmdEvals().iterator();
                while (mmds.hasNext()) {
                    MMDispensing mmd = mmds.next();

                    if (mmd.getEvaluationDate() != null
                            && CommonUtils.dateDiff(ChronoUnit.DAYS, mmd.getEvaluationDate(), arrivalDate) == 0) {
                        this.mmdEval = new MMDispensingDto(mmd);
                        this.mmdEval.setOrganization(null);
                        this.mmdEval.setTheCase(null);
                        break;
                    }
                }
            }
        }

        if (entity.getOrganization() != null) {
            this.organization = new OrganizationDto();
            this.organization.setId(entity.getOrganization().getId());
        }
    }

    public Appointment toEntity() {
        Appointment entity = new Appointment();
        entity = (Appointment) super.toEntity(entity);

        entity.setId(id);
        entity.setUid(uid);
        entity.setAppointmentDate(appointmentDate);
        entity.setArrived(arrived);
        entity.setArrivalDate(arrivalDate);
        entity.setMissed(missed);
        entity.setDrugDispensed(drugDispensed);
        entity.setCd4Tested(cd4Tested);
        entity.setVlTested(vlTested);
        entity.setArvDrTested(arvDrTested);
        entity.setTbScreenResult(tbScreenResult);
        entity.setHepScreened(hepScreened);
        entity.setShiChanged(shiChanged);
        entity.setNextAppointmentDate(nextAppointmentDate);
        entity.setDrugDays(drugDays);
        entity.setDrugSource(drugSource);
        entity.setDrugSourceAlt(drugSourceAlt);
        entity.setArvRegimenName(arvRegimenName);
        entity.setArvRegimenLine(arvRegimenLine);
        entity.setGoodAdherence(goodAdherence);
        entity.setHasOI(hasOI);
        entity.setHasDrugAE(hasDrugAE);
        entity.setPregnant(pregnant);
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

    public UUID getUid() {
        return uid;
    }

    public void setUid(UUID uid) {
        this.uid = uid;
    }

    public OrganizationDto getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationDto organization) {
        this.organization = organization;
    }

    public CaseDto getTheCase() {
        return theCase;
    }

    public void setTheCase(CaseDto theCase) {
        this.theCase = theCase;
    }

    public LocalDateTime getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDateTime appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public Boolean getArrived() {
        return arrived;
    }

    public void setArrived(Boolean arrived) {
        this.arrived = arrived;
    }

    public LocalDateTime getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(LocalDateTime arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public Boolean getMissed() {
        return missed;
    }

    public void setMissed(Boolean missed) {
        this.missed = missed;
    }

    public Boolean getDrugDispensed() {
        return drugDispensed;
    }

    public void setDrugDispensed(Boolean drugDispensed) {
        this.drugDispensed = drugDispensed;
    }

    public Boolean getCd4Tested() {
        return cd4Tested;
    }

    public void setCd4Tested(Boolean cd4Tested) {
        this.cd4Tested = cd4Tested;
    }

    public Boolean getVlTested() {
        return vlTested;
    }

    public void setVlTested(Boolean vlTested) {
        this.vlTested = vlTested;
    }

    public Boolean getArvDrTested() {
        return arvDrTested;
    }

    public void setArvDrTested(Boolean arvDrTested) {
        this.arvDrTested = arvDrTested;
    }

    public Integer getTbScreenResult() {
        return tbScreenResult;
    }

    public void setTbScreenResult(Integer tbScreenResult) {
        this.tbScreenResult = tbScreenResult;
    }

    public Boolean getHepScreened() {
        return hepScreened;
    }

    public void setHepScreened(Boolean hepScreened) {
        this.hepScreened = hepScreened;
    }

    public Boolean getShiChanged() {
        return shiChanged;
    }

    public void setShiChanged(Boolean shiChanged) {
        this.shiChanged = shiChanged;
    }

    public LocalDateTime getNextAppointmentDate() {
        return nextAppointmentDate;
    }

    public void setNextAppointmentDate(LocalDateTime nextAppointmentDate) {
        this.nextAppointmentDate = nextAppointmentDate;
    }

    public Integer getDrugDays() {
        return drugDays;
    }

    public void setDrugDays(Integer drugDays) {
        this.drugDays = drugDays;
    }

    public ARVFundingSource getDrugSource() {
        return drugSource;
    }

    public void setDrugSource(ARVFundingSource drugSource) {
        this.drugSource = drugSource;
    }

    public ARVFundingSource getDrugSourceAlt() {
        return drugSourceAlt;
    }

    public void setDrugSourceAlt(ARVFundingSource drugSourceAlt) {
        this.drugSourceAlt = drugSourceAlt;
    }

    public String getArvRegimenName() {
        return arvRegimenName;
    }

    public void setArvRegimenName(String arvRegimenName) {
        this.arvRegimenName = arvRegimenName;
    }

    public Integer getArvRegimenLine() {
        return arvRegimenLine;
    }

    public void setArvRegimenLine(Integer arvRegimenLine) {
        this.arvRegimenLine = arvRegimenLine;
    }

    public Boolean getGoodAdherence() {
        return goodAdherence;
    }

    public void setGoodAdherence(Boolean goodAdherence) {
        this.goodAdherence = goodAdherence;
    }

    public Boolean getHasOI() {
        return hasOI;
    }

    public void setHasOI(Boolean hasOI) {
        this.hasOI = hasOI;
    }

    public Boolean getHasDrugAE() {
        return hasDrugAE;
    }

    public void setHasDrugAE(Boolean hasDrugAE) {
        this.hasDrugAE = hasDrugAE;
    }

    public Boolean getPregnant() {
        return pregnant;
    }

    public void setPregnant(Boolean pregnant) {
        this.pregnant = pregnant;
    }

    public LocalDateTime getLatestArrivalDate() {
        return latestArrivalDate;
    }

    public void setLatestArrivalDate(LocalDateTime latestArrivalDate) {
        this.latestArrivalDate = latestArrivalDate;
    }

    public LabTestDto getLatestVlTest() {
        return latestVlTest;
    }

    public void setLatestVlTest(LabTestDto latestVlTest) {
        this.latestVlTest = latestVlTest;
    }

    public ClinicalStageDto getClinicalStage() {
        return clinicalStage;
    }

    public void setClinicalStage(ClinicalStageDto clinicalStage) {
        this.clinicalStage = clinicalStage;
    }

    public LabTestDto getVlTest() {
        return vlTest;
    }

    public void setVlTest(LabTestDto vlTest) {
        this.vlTest = vlTest;
    }

    public LabTestDto getCd4Test() {
        return cd4Test;
    }

    public void setCd4Test(LabTestDto cd4Test) {
        this.cd4Test = cd4Test;
    }

    public HepatitisDto getHep() {
        return hep;
    }

    public void setHep(HepatitisDto hep) {
        this.hep = hep;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public ShiInterviewDto getShi() {
        return shi;
    }

    public void setShi(ShiInterviewDto shi) {
        this.shi = shi;
    }

    public ShiInterviewDto getPrevShi() {
        return prevShi;
    }

    public void setPrevShi(ShiInterviewDto prevShi) {
        this.prevShi = prevShi;
    }

    public boolean isAutoGenNextAppointment() {
        return autoGenNextAppointment;
    }

    public void setAutoGenNextAppointment(boolean autoGenNextAppointment) {
        this.autoGenNextAppointment = autoGenNextAppointment;
    }

    public boolean isMmdPartiallyEligible() {
        return mmdPartiallyEligible;
    }

    public void setMmdPartiallyEligible(boolean mmdPartiallyEligible) {
        this.mmdPartiallyEligible = mmdPartiallyEligible;
    }

    public MMDispensingDto getMmdEval() {
        return mmdEval;
    }

    public void setMmdEval(MMDispensingDto mmdEval) {
        this.mmdEval = mmdEval;
    }

    public MMDispensingDto getPrevMmdEval() {
        return prevMmdEval;
    }

    public void setPrevMmdEval(MMDispensingDto prevMmdEval) {
        this.prevMmdEval = prevMmdEval;
    }

    public int getLateDays() {
        return lateDays;
    }

    public void setLateDays(int lateDays) {
        this.lateDays = lateDays;
    }

    public ARVFundingSource getPrevDrugSource() {
        return prevDrugSource;
    }

    public void setPrevDrugSource(ARVFundingSource prevDrugSource) {
        this.prevDrugSource = prevDrugSource;
    }

    public TBTreatment2Dto getTbtx2() {
        return tbtx2;
    }

    public void setTbtx2(TBTreatment2Dto tbtx2) {
        this.tbtx2 = tbtx2;
    }

    public TBProphylaxis2Dto getTbpro2() {
        return tbpro2;
    }

    public void setTbpro2(TBProphylaxis2Dto tbpro2) {
        this.tbpro2 = tbpro2;
    }

    public CaseOrgDto getCurrentCaseOrg() {
        return currentCaseOrg;
    }

    public void setCurrentCaseOrg(CaseOrgDto currentCaseOrg) {
        this.currentCaseOrg = currentCaseOrg;
    }
}
