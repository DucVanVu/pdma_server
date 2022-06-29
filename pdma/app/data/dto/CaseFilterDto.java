package org.pepfar.pdma.app.data.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.pepfar.pdma.app.data.types.AppointmentResult;
import org.pepfar.pdma.app.data.types.EnrollmentType;
import org.pepfar.pdma.app.data.types.Gender;
import org.pepfar.pdma.app.data.types.PatientStatus;
import org.pepfar.pdma.app.utils.CustomLocalDateTimeDeserializer2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class CaseFilterDto implements Serializable {

    private static final long serialVersionUID = -7553177457015644996L;

    private String keyword;

    private UserDto user;

    private OrganizationDto organization;

    private boolean includeDeleted;

    private boolean includeOnART;

    private boolean includePreART;

    private boolean missingData;

    private Long serviceId;

    private Long diseaseId;

    private PatientStatus patientStatus;

    private EnrollmentType enrollmentType;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
    private LocalDateTime hivConfirmDateFrom;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
    private LocalDateTime hivConfirmDateTo;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
    private LocalDateTime arvStartDateFrom;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
    private LocalDateTime arvStartDateTo;

    private String arvGroup;

    private Gender gender;

    private Integer ageFrom;

    private Integer ageTo;

    private AppointmentResult appointmentResult;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
    private LocalDateTime appointmentMonth;

    private int pageIndex;

    private int pageSize;

    /** 1 = patient name; 2 = patient chart ID */
    private int sortField;

    // For data export
    private boolean exportPatient = true; // --> this should be true by default

    private boolean exportTreatmentHistory;

    private boolean exportArvHistory;

    private boolean exportViralLoad;

    private boolean exportCD4;

    private boolean exportShi;

    private boolean exportRisk;

    private boolean exportTbProphylaxis;

    private boolean exportTbTreatment;

    private boolean exportRecency;

    private boolean exportHivDr;

    private boolean exportHepatitis;

    private boolean exportWhoStage;

    private boolean exportMmt;

    private boolean exportPregnancy;

    // Either: 'whole' or 'scoped'
    private String exportScope;

    private boolean skipPNS;

    public boolean isSkipPNS() {
        return skipPNS;
    }

    public void setSkipPNS(boolean skipPNS) {
        this.skipPNS = skipPNS;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }

    public OrganizationDto getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationDto organization) {
        this.organization = organization;
    }

    public boolean isIncludeDeleted() {
        return includeDeleted;
    }

    public void setIncludeDeleted(boolean includeDeleted) {
        this.includeDeleted = includeDeleted;
    }

    public boolean isIncludeOnART() {
        return includeOnART;
    }

    public void setIncludeOnART(boolean includeOnART) {
        this.includeOnART = includeOnART;
    }

    public boolean isIncludePreART() {
        return includePreART;
    }

    public void setIncludePreART(boolean includePreART) {
        this.includePreART = includePreART;
    }

    public boolean isMissingData() {
        return missingData;
    }

    public void setMissingData(boolean missingData) {
        this.missingData = missingData;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public Long getDiseaseId() {
        return diseaseId;
    }

    public void setDiseaseId(Long diseaseId) {
        this.diseaseId = diseaseId;
    }

    public PatientStatus getPatientStatus() {
        return patientStatus;
    }

    public void setPatientStatus(PatientStatus patientStatus) {
        this.patientStatus = patientStatus;
    }

    public EnrollmentType getEnrollmentType() {
        return enrollmentType;
    }

    public void setEnrollmentType(EnrollmentType enrollmentType) {
        this.enrollmentType = enrollmentType;
    }

    public LocalDateTime getHivConfirmDateFrom() {
        return hivConfirmDateFrom;
    }

    public void setHivConfirmDateFrom(LocalDateTime hivConfirmDateFrom) {
        this.hivConfirmDateFrom = hivConfirmDateFrom;
    }

    public LocalDateTime getHivConfirmDateTo() {
        return hivConfirmDateTo;
    }

    public void setHivConfirmDateTo(LocalDateTime hivConfirmDateTo) {
        this.hivConfirmDateTo = hivConfirmDateTo;
    }

    public LocalDateTime getArvStartDateFrom() {
        return arvStartDateFrom;
    }

    public void setArvStartDateFrom(LocalDateTime arvStartDateFrom) {
        this.arvStartDateFrom = arvStartDateFrom;
    }

    public LocalDateTime getArvStartDateTo() {
        return arvStartDateTo;
    }

    public void setArvStartDateTo(LocalDateTime arvStartDateTo) {
        this.arvStartDateTo = arvStartDateTo;
    }

    public String getArvGroup() {
        return arvGroup;
    }

    public void setArvGroup(String arvGroup) {
        this.arvGroup = arvGroup;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Integer getAgeFrom() {
        return ageFrom;
    }

    public void setAgeFrom(Integer ageFrom) {
        this.ageFrom = ageFrom;
    }

    public Integer getAgeTo() {
        return ageTo;
    }

    public void setAgeTo(Integer ageTo) {
        this.ageTo = ageTo;
    }

    public AppointmentResult getAppointmentResult() {
        return appointmentResult;
    }

    public void setAppointmentResult(AppointmentResult appointmentResult) {
        this.appointmentResult = appointmentResult;
    }

    public LocalDateTime getAppointmentMonth() {
        return appointmentMonth;
    }

    public void setAppointmentMonth(LocalDateTime appointmentMonth) {
        this.appointmentMonth = appointmentMonth;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getSortField() {

        if (sortField <= 0) {
            sortField = 1;
        }

        return sortField;
    }

    public void setSortField(int sortField) {
        this.sortField = sortField;
    }

    // For data export only

    public boolean isExportPatient() {
        return exportPatient;
    }

    public void setExportPatient(boolean exportPatient) {
        this.exportPatient = exportPatient;
    }

    public boolean isExportTreatmentHistory() {
        return exportTreatmentHistory;
    }

    public void setExportTreatmentHistory(boolean exportTreatmentHistory) {
        this.exportTreatmentHistory = exportTreatmentHistory;
    }

    public boolean isExportArvHistory() {
        return exportArvHistory;
    }

    public void setExportArvHistory(boolean exportArvHistory) {
        this.exportArvHistory = exportArvHistory;
    }

    public boolean isExportViralLoad() {
        return exportViralLoad;
    }

    public void setExportViralLoad(boolean exportViralLoad) {
        this.exportViralLoad = exportViralLoad;
    }

    public boolean isExportCD4() {
        return exportCD4;
    }

    public void setExportCD4(boolean exportCD4) {
        this.exportCD4 = exportCD4;
    }

    public boolean isExportShi() {
        return exportShi;
    }

    public void setExportShi(boolean exportShi) {
        this.exportShi = exportShi;
    }

    public boolean isExportRisk() {
        return exportRisk;
    }

    public void setExportRisk(boolean exportRisk) {
        this.exportRisk = exportRisk;
    }

    public boolean isExportTbProphylaxis() {
        return exportTbProphylaxis;
    }

    public void setExportTbProphylaxis(boolean exportTbProphylaxis) {
        this.exportTbProphylaxis = exportTbProphylaxis;
    }

    public boolean isExportTbTreatment() {
        return exportTbTreatment;
    }

    public void setExportTbTreatment(boolean exportTbTreatment) {
        this.exportTbTreatment = exportTbTreatment;
    }

    public boolean isExportRecency() {
        return exportRecency;
    }

    public void setExportRecency(boolean exportRecency) {
        this.exportRecency = exportRecency;
    }

    public boolean isExportHivDr() {
        return exportHivDr;
    }

    public void setExportHivDr(boolean exportHivDr) {
        this.exportHivDr = exportHivDr;
    }

    public boolean isExportHepatitis() {
        return exportHepatitis;
    }

    public void setExportHepatitis(boolean exportHepatitis) {
        this.exportHepatitis = exportHepatitis;
    }

    public boolean isExportWhoStage() {
        return exportWhoStage;
    }

    public void setExportWhoStage(boolean exportWhoStage) {
        this.exportWhoStage = exportWhoStage;
    }

    public boolean isExportMmt() {
        return exportMmt;
    }

    public void setExportMmt(boolean exportMmt) {
        this.exportMmt = exportMmt;
    }

    public boolean isExportPregnancy() {
        return exportPregnancy;
    }

    public void setExportPregnancy(boolean exportPregnancy) {
        this.exportPregnancy = exportPregnancy;
    }

    public String getExportScope() {
        return exportScope;
    }

    public void setExportScope(String exportScope) {
        this.exportScope = exportScope;
    }
}
