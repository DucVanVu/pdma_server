package org.pepfar.pdma.app.data.dto;

import java.math.BigInteger;

public class ReportMERPEDto {
    private BigInteger orgId;
    private String orgCode;
    private String orgName;
    private String provinceName;
    private String districtName;
    private String support;

    private BigInteger pwid = new BigInteger(String.valueOf(0));
    private BigInteger msm = new BigInteger(String.valueOf(0));
    private BigInteger tg = new BigInteger(String.valueOf(0));
    private BigInteger fsw = new BigInteger(String.valueOf(0));
    private BigInteger other = new BigInteger(String.valueOf(0));

    private BigInteger pwidC8Positives = new BigInteger(String.valueOf(0));
    private BigInteger msmC8Positives = new BigInteger(String.valueOf(0));
    private BigInteger tgC8Positives = new BigInteger(String.valueOf(0));
    private BigInteger fswC8Positives = new BigInteger(String.valueOf(0));
    private BigInteger otherC8Positives = new BigInteger(String.valueOf(0));

    private BigInteger pwidC11Yes = new BigInteger(String.valueOf(0));
    private BigInteger msmC11Yes = new BigInteger(String.valueOf(0));
    private BigInteger tgC11Yes = new BigInteger(String.valueOf(0));
    private BigInteger fswC11Yes = new BigInteger(String.valueOf(0));
    private BigInteger otherC11Yes = new BigInteger(String.valueOf(0));

    private BigInteger pwidC11No = new BigInteger(String.valueOf(0));
    private BigInteger msmC11No = new BigInteger(String.valueOf(0));
    private BigInteger tgC11No = new BigInteger(String.valueOf(0));
    private BigInteger fswC11No = new BigInteger(String.valueOf(0));
    private BigInteger otherC11No = new BigInteger(String.valueOf(0));

    private Integer totalKPPREV;

    private BigInteger testingC8Positives = new BigInteger(String.valueOf(0));
    private BigInteger testingC11Yes = new BigInteger(String.valueOf(0));
    private BigInteger testingC11No = new BigInteger(String.valueOf(0));
    private BigInteger testingNotRequired = new BigInteger(String.valueOf(0));

    private BigInteger priorityClientOfSex = new BigInteger(String.valueOf(0));
    private BigInteger priorityMilitary = new BigInteger(String.valueOf(0));
    private BigInteger priorityMobile = new BigInteger(String.valueOf(0));
    private BigInteger priorityNonInjectDrug = new BigInteger(String.valueOf(0));
    private BigInteger priorityOther = new BigInteger(String.valueOf(0));

    private BigInteger fu = new BigInteger(String.valueOf(0));
    private BigInteger f10 = new BigInteger(String.valueOf(0));
    private BigInteger f15 = new BigInteger(String.valueOf(0));
    private BigInteger f20 = new BigInteger(String.valueOf(0));
    private BigInteger f25 = new BigInteger(String.valueOf(0));
    private BigInteger f30 = new BigInteger(String.valueOf(0));
    private BigInteger f35 = new BigInteger(String.valueOf(0));
    private BigInteger f40 = new BigInteger(String.valueOf(0));
    private BigInteger f45 = new BigInteger(String.valueOf(0));
    private BigInteger f50 = new BigInteger(String.valueOf(0));

    private BigInteger mu = new BigInteger(String.valueOf(0));
    private BigInteger m10 = new BigInteger(String.valueOf(0));
    private BigInteger m15 = new BigInteger(String.valueOf(0));
    private BigInteger m20 = new BigInteger(String.valueOf(0));
    private BigInteger m25 = new BigInteger(String.valueOf(0));
    private BigInteger m30 = new BigInteger(String.valueOf(0));
    private BigInteger m35 = new BigInteger(String.valueOf(0));
    private BigInteger m40 = new BigInteger(String.valueOf(0));
    private BigInteger m45 = new BigInteger(String.valueOf(0));
    private BigInteger m50 = new BigInteger(String.valueOf(0));

    private Integer totalPPPREV;

    public BigInteger getOrgId() {return orgId;}

    public void setOrgId(BigInteger orgId) {this.orgId = orgId;}

    public String getOrgCode() {return orgCode;}

    public void setOrgCode(String orgCode) {this.orgCode = orgCode;}

    public String getOrgName() {return orgName;}

    public void setOrgName(String orgName) {this.orgName = orgName;}

    public String getProvinceName() {return provinceName;}

    public void setProvinceName(String provinceName) {this.provinceName = provinceName;}

    public String getDistrictName() {return districtName;}

    public void setDistrictName(String districtName) {this.districtName = districtName;}

    public String getSupport() {return "DSD";}

    public void setSupport(String support) {this.support = support;}

    public BigInteger getPwid() {return pwid;}

    public void setPwid(BigInteger pwid) {this.pwid = pwid;}

    public BigInteger getMsm() {return msm;}

    public void setMsm(BigInteger msm) {this.msm = msm;}

    public BigInteger getTg() {return tg;}

    public void setTg(BigInteger tg) {this.tg = tg;}

    public BigInteger getFsw() {return fsw;}

    public void setFsw(BigInteger fsw) {this.fsw = fsw;}

    public BigInteger getOther() {return other;}

    public void setOther(BigInteger other) {this.other = other;}

    public BigInteger getPwidC8Positives() {return pwidC8Positives;}

    public void setPwidC8Positives(BigInteger pwidC8Positives) {this.pwidC8Positives = pwidC8Positives;}

    public BigInteger getMsmC8Positives() {return msmC8Positives;}

    public void setMsmC8Positives(BigInteger msmC8Positives) {this.msmC8Positives = msmC8Positives;}

    public BigInteger getTgC8Positives() {return tgC8Positives;}

    public void setTgC8Positives(BigInteger tgC8Positives) {this.tgC8Positives = tgC8Positives;}

    public BigInteger getFswC8Positives() {return fswC8Positives;}

    public void setFswC8Positives(BigInteger fswC8Positives) {this.fswC8Positives = fswC8Positives;}

    public BigInteger getOtherC8Positives() {return otherC8Positives;}

    public void setOtherC8Positives(BigInteger otherC8Positives) {this.otherC8Positives = otherC8Positives;}

    public BigInteger getPwidC11Yes() {return pwidC11Yes;}

    public void setPwidC11Yes(BigInteger pwidC11Yes) {this.pwidC11Yes = pwidC11Yes;}

    public BigInteger getMsmC11Yes() {return msmC11Yes;}

    public void setMsmC11Yes(BigInteger msmC11Yes) {this.msmC11Yes = msmC11Yes;}

    public BigInteger getTgC11Yes() {return tgC11Yes;}

    public void setTgC11Yes(BigInteger tgC11Yes) {this.tgC11Yes = tgC11Yes;}

    public BigInteger getFswC11Yes() {return fswC11Yes;}

    public void setFswC11Yes(BigInteger fswC11Yes) {this.fswC11Yes = fswC11Yes;}

    public BigInteger getOtherC11Yes() {return otherC11Yes;}

    public void setOtherC11Yes(BigInteger otherC11Yes) {this.otherC11Yes = otherC11Yes;}

    public BigInteger getPwidC11No() {return pwidC11No;}

    public void setPwidC11No(BigInteger pwidC11No) {this.pwidC11No = pwidC11No;}

    public BigInteger getMsmC11No() {return msmC11No;}

    public void setMsmC11No(BigInteger msmC11No) {this.msmC11No = msmC11No;}

    public BigInteger getTgC11No() {return tgC11No;}

    public void setTgC11No(BigInteger tgC11No) {this.tgC11No = tgC11No;}

    public BigInteger getFswC11No() {return fswC11No;}

    public void setFswC11No(BigInteger fswC11No) {this.fswC11No = fswC11No;}

    public BigInteger getOtherC11No() {return otherC11No;}

    public void setOtherC11No(BigInteger otherC11No) {this.otherC11No = otherC11No;}

    public Integer getTotalKPPREV() {
        totalKPPREV = 0;
        if (this.pwid != null) {
            totalKPPREV += this.pwid.intValue();
        }
        if (this.msm != null) {
            totalKPPREV += this.msm.intValue();
        }
        if (this.tg != null) {
            totalKPPREV += this.tg.intValue();
        }
        if (this.fsw != null) {
            totalKPPREV += this.fsw.intValue();
        }
        if (this.other != null) {
            totalKPPREV += this.other.intValue();
        }
        return totalKPPREV;
    }

    public void setTotalKPPREV(Integer totalKPPREV) {this.totalKPPREV = totalKPPREV;}

    public BigInteger getTestingC8Positives() {return testingC8Positives;}

    public void setTestingC8Positives(BigInteger testingC8Positives) {this.testingC8Positives = testingC8Positives;}

    public BigInteger getTestingC11Yes() {return testingC11Yes;}

    public void setTestingC11Yes(BigInteger testingC11Yes) {this.testingC11Yes = testingC11Yes;}

    public BigInteger getTestingC11No() {return testingC11No;}

    public void setTestingC11No(BigInteger testingC11No) {this.testingC11No = testingC11No;}

    public BigInteger getTestingNotRequired() {return testingNotRequired;}

    public void setTestingNotRequired(BigInteger testingNotRequired) {this.testingNotRequired = testingNotRequired;}

    public BigInteger getPriorityClientOfSex() {return priorityClientOfSex;}

    public void setPriorityClientOfSex(BigInteger priorityClientOfSex) {this.priorityClientOfSex = priorityClientOfSex;}

    public BigInteger getPriorityMilitary() {return priorityMilitary;}

    public void setPriorityMilitary(BigInteger priorityMilitary) {this.priorityMilitary = priorityMilitary;}

    public BigInteger getPriorityMobile() {return priorityMobile;}

    public void setPriorityMobile(BigInteger priorityMobile) {this.priorityMobile = priorityMobile;}

    public BigInteger getPriorityNonInjectDrug() {return priorityNonInjectDrug;}

    public void setPriorityNonInjectDrug(BigInteger priorityNonInjectDrug) {this.priorityNonInjectDrug = priorityNonInjectDrug;}

    public BigInteger getPriorityOther() {return priorityOther;}

    public void setPriorityOther(BigInteger priorityOther) {this.priorityOther = priorityOther;}

    public BigInteger getFu() {return fu;}

    public void setFu(BigInteger fu) {this.fu = fu;}

    public BigInteger getF10() {return f10;}

    public void setF10(BigInteger f10) {this.f10 = f10;}

    public BigInteger getF15() {return f15;}

    public void setF15(BigInteger f15) {this.f15 = f15;}

    public BigInteger getF20() {return f20;}

    public void setF20(BigInteger f20) {this.f20 = f20;}

    public BigInteger getF25() {return f25;}

    public void setF25(BigInteger f25) {this.f25 = f25;}

    public BigInteger getF30() {return f30;}

    public void setF30(BigInteger f30) {this.f30 = f30;}

    public BigInteger getF35() {return f35;}

    public void setF35(BigInteger f35) {this.f35 = f35;}

    public BigInteger getF40() {return f40;}

    public void setF40(BigInteger f40) {this.f40 = f40;}

    public BigInteger getF45() {return f45;}

    public void setF45(BigInteger f45) {this.f45 = f45;}

    public BigInteger getF50() {return f50;}

    public void setF50(BigInteger f50) {this.f50 = f50;}

    public BigInteger getMu() {return mu;}

    public void setMu(BigInteger mu) {this.mu = mu;}

    public BigInteger getM10() {return m10;}

    public void setM10(BigInteger m10) {this.m10 = m10;}

    public BigInteger getM15() {return m15;}

    public void setM15(BigInteger m15) {this.m15 = m15;}

    public BigInteger getM20() {return m20;}

    public void setM20(BigInteger m20) {this.m20 = m20;}

    public BigInteger getM25() {return m25;}

    public void setM25(BigInteger m25) {this.m25 = m25;}

    public BigInteger getM30() {return m30;}

    public void setM30(BigInteger m30) {this.m30 = m30;}

    public BigInteger getM35() {return m35;}

    public void setM35(BigInteger m35) {this.m35 = m35;}

    public BigInteger getM40() {return m40;}

    public void setM40(BigInteger m40) {this.m40 = m40;}

    public BigInteger getM45() {return m45;}

    public void setM45(BigInteger m45) {this.m45 = m45;}

    public BigInteger getM50() {return m50;}

    public void setM50(BigInteger m50) {this.m50 = m50;}

    public Integer getTotalPPPREV() {
        totalPPPREV = 0;
        if (this.priorityOther != null) {
            totalPPPREV += priorityOther.intValue();
        }
        return totalPPPREV;
    }

    public void setTotalPPPREV(Integer totalPPPREV) {this.totalPPPREV = totalPPPREV;}
}
