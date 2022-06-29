package org.pepfar.pdma.app.data.dto;

import java.sql.Date;
import java.sql.Timestamp;

public interface PatientExtraData {

    public Long get_1_1_CaseId();

    public Timestamp get_1_2_VlSampleDate();

    public Timestamp get_1_3_VlResultDate();

    public String get_1_4_VlReasonForTesting();

    public Integer get_1_5_VlResult();

    public String get_1_51_VlResultText();

    // latest risk assessment

    public Date get_1_6_RiskInterviewDate();

    public Long get_1_7_RiskId();

    public String get_1_8_RiskName();

    public String get_1_9_OtherRiskGroup();

    // Clinical stage

//    public Integer get_2_1_ClinicalStage();
//
//    public Timestamp get_2_2_ClinicalStageEvalDate();

    // Oldest risk assessment
    public Date get_2_3_RiskInterviewDate();

    public Long get_2_4_RiskId();

    public String get_2_5_RiskName();

    public String get_2_6_OtherRiskGroup();

    // SHI data
    public String get_2_7_shiCardNumber();

    public Timestamp get_2_8_shiExpiryDate();

    // MMD
    public Timestamp get_2_9_mmdEvalDate();

    public Boolean get_3_1_mmdEligible();

    public Boolean get_3_2_mmdOnMmd();

    // TB prophylaxis
//    public Timestamp get_3_3_tbProStartDate();
//
//    public Timestamp get_3_4_tbProEndDate();
//
//    public Integer get_3_5_tbProResult();

    // TB tx
//    public Timestamp get_3_6_tbtxDiagnoseDate();
//
//    public Timestamp get_3_7_tbtxStartDate();
//
//    public Timestamp get_3_8_tbtxEndDate();
//
//    public String get_3_9_tbtxFacility();

    // Hep B
//    public Timestamp get_4_1_hepbTestDate();
//
//    public Boolean get_4_2_hepbTestPos();

    // Hep C
//    public Timestamp get_4_3_hepcTestDate();
//
//    public Boolean get_4_4_hepcTestPos();

}
