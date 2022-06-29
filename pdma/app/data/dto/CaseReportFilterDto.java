package org.pepfar.pdma.app.data.dto;

import java.time.LocalDateTime;
import java.util.List;

import org.pepfar.pdma.app.data.types.ReportType;
import org.pepfar.pdma.app.data.types.ReportingAlgorithm;
import org.pepfar.pdma.app.utils.CustomLocalDateTimeDeserializer2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class CaseReportFilterDto {

    private ReportType reportType;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
    private LocalDateTime fromDate;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
    private LocalDateTime toDate;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
    private LocalDateTime atDate;

    // ---------------------------------------------
    // for PEPFAR viral load report period selection
    // ---------------------------------------------
    private int selQuarter;

    private int selYear;
    // ---------------------------------------------
    // for PEPFAR viral load report period selection
    // ---------------------------------------------

    // Province ID
    private Long province;

    // Organization ID.
    // 0 = All granted organizations
    private Long organization;

    // Actual list of organization Ids
    // Can be empty, one or multiple IDs depending on the variable organization
    // above
    private List<Long> actualOrganizations;

    private boolean confidentialRequired = false;

    private String password; // password for the output Excel file

    private Boolean rawDataOnly;

    private Boolean includeExtra; // Include latest viral load test, latest risk group assessment, latest clinical
    // stage in the current list of patients

    private Boolean includeAppointmentInfo; // Include the latest arrived date, latest appointment date right after
    // the latest arrived date at an organization

    private ReportingAlgorithm algorithm; // tính chỉ số theo TT03 hay PEPFAR; 'CIRCULA_03' = TT03, 'PEPFAR' = PEPFAR

    // -------------------------------------------
    // for TB Prophylaxis2 report period selection
    // -------------------------------------------
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
    private LocalDateTime fromYearDate;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
    private LocalDateTime toYearDate;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
    private LocalDateTime preFromDate;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
    private LocalDateTime preToDate;
    // -------------------------------------------
    // for TB Prophylaxis2 report period selection
    // -------------------------------------------

    private Boolean numerator = true; // true là tử số // false là mãu số

    public Boolean getNumerator() {return numerator;}

    public void setNumerator(Boolean numerator) {this.numerator = numerator;}

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }

    public LocalDateTime getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDateTime fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDateTime getToDate() {
        return toDate;
    }

    public void setToDate(LocalDateTime toDate) {
        this.toDate = toDate;
    }

    public LocalDateTime getAtDate() {
        return atDate;
    }

    public void setAtDate(LocalDateTime atDate) {
        this.atDate = atDate;
    }

    public int getSelQuarter() {
        return selQuarter;
    }

    public void setSelQuarter(int selQuarter) {
        this.selQuarter = selQuarter;
    }

    public int getSelYear() {
        return selYear;
    }

    public void setSelYear(int selYear) {
        this.selYear = selYear;
    }

    public Long getProvince() {
        return province;
    }

    public void setProvince(Long province) {
        this.province = province;
    }

    public Long getOrganization() {
        return organization;
    }

    public void setOrganization(Long organization) {
        this.organization = organization;
    }

    public List<Long> getActualOrganizations() {
        return actualOrganizations;
    }

    public void setActualOrganizations(List<Long> actualOrganizations) {
        this.actualOrganizations = actualOrganizations;
    }

    public boolean isConfidentialRequired() {
        return confidentialRequired;
    }

    public void setConfidentialRequired(boolean confidentialRequired) {
        this.confidentialRequired = confidentialRequired;
    }

    public LocalDateTime getFromYearDate() {
        return fromYearDate;
    }

    public void setFromYearDate(LocalDateTime fromYearDate) {
        this.fromYearDate = fromYearDate;
    }

    public LocalDateTime getToYearDate() {
        return toYearDate;
    }

    public void setToYearDate(LocalDateTime toYearDate) {
        this.toYearDate = toYearDate;
    }

    public LocalDateTime getPreFromDate() {
        return preFromDate;
    }

    public void setPreFromDate(LocalDateTime preFromDate) {
        this.preFromDate = preFromDate;
    }

    public LocalDateTime getPreToDate() {
        return preToDate;
    }

    public void setPreToDate(LocalDateTime preToDate) {
        this.preToDate = preToDate;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getRawDataOnly() {
        return rawDataOnly;
    }

    public void setRawDataOnly(Boolean rawDataOnly) {
        this.rawDataOnly = rawDataOnly;
    }

    public ReportingAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(ReportingAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public Boolean getIncludeExtra() {
        return includeExtra;
    }

    public void setIncludeExtra(Boolean includeExtra) {
        this.includeExtra = includeExtra;
    }

    public Boolean getIncludeAppointmentInfo() {
        return includeAppointmentInfo;
    }

    public void setIncludeAppointmentInfo(Boolean includeAppointmentInfo) {
        this.includeAppointmentInfo = includeAppointmentInfo;
    }
}
