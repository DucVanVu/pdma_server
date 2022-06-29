package org.pepfar.pdma.app.data.dto;

import java.math.BigInteger;

public class HTSTSTDto {
    private BigInteger orgId;
    private String orgCode;
    private String orgName;
    private String provinceName;
    private String districtName;
    private String support;
    private String modality;

    private BigInteger pPWID = new BigInteger(String.valueOf(0));
    private BigInteger nPWID = new BigInteger(String.valueOf(0));
    private BigInteger pMSM = new BigInteger(String.valueOf(0));
    private BigInteger nMSM = new BigInteger(String.valueOf(0));
    private BigInteger pTG = new BigInteger(String.valueOf(0));
    private BigInteger nTG = new BigInteger(String.valueOf(0));
    private BigInteger pFSW = new BigInteger(String.valueOf(0));
    private BigInteger nFSW = new BigInteger(String.valueOf(0));
    private BigInteger pOther = new BigInteger(String.valueOf(0));
    private BigInteger nOther = new BigInteger(String.valueOf(0));

    private BigInteger pFU = new BigInteger(String.valueOf(0));
    private BigInteger pF0 = new BigInteger(String.valueOf(0));
    private BigInteger pF1 = new BigInteger(String.valueOf(0));
    private BigInteger pF5 = new BigInteger(String.valueOf(0));
    private BigInteger pF10 = new BigInteger(String.valueOf(0));
    private BigInteger pF15 = new BigInteger(String.valueOf(0));
    private BigInteger pF20 = new BigInteger(String.valueOf(0));
    private BigInteger pF25 = new BigInteger(String.valueOf(0));
    private BigInteger pF30 = new BigInteger(String.valueOf(0));
    private BigInteger pF35 = new BigInteger(String.valueOf(0));
    private BigInteger pF40 = new BigInteger(String.valueOf(0));
    private BigInteger pF45 = new BigInteger(String.valueOf(0));
    private BigInteger pF50 = new BigInteger(String.valueOf(0));

    private BigInteger pMU = new BigInteger(String.valueOf(0));
    private BigInteger pM0 = new BigInteger(String.valueOf(0));
    private BigInteger pM1 = new BigInteger(String.valueOf(0));
    private BigInteger pM5 = new BigInteger(String.valueOf(0));
    private BigInteger pM10 = new BigInteger(String.valueOf(0));
    private BigInteger pM15 = new BigInteger(String.valueOf(0));
    private BigInteger pM20 = new BigInteger(String.valueOf(0));
    private BigInteger pM25 = new BigInteger(String.valueOf(0));
    private BigInteger pM30 = new BigInteger(String.valueOf(0));
    private BigInteger pM35 = new BigInteger(String.valueOf(0));
    private BigInteger pM40 = new BigInteger(String.valueOf(0));
    private BigInteger pM45 = new BigInteger(String.valueOf(0));
    private BigInteger pM50 = new BigInteger(String.valueOf(0));

    private BigInteger nFU = new BigInteger(String.valueOf(0));
    private BigInteger nF0 = new BigInteger(String.valueOf(0));
    private BigInteger nF1 = new BigInteger(String.valueOf(0));
    private BigInteger nF5 = new BigInteger(String.valueOf(0));
    private BigInteger nF10 = new BigInteger(String.valueOf(0));
    private BigInteger nF15 = new BigInteger(String.valueOf(0));
    private BigInteger nF20 = new BigInteger(String.valueOf(0));
    private BigInteger nF25 = new BigInteger(String.valueOf(0));
    private BigInteger nF30 = new BigInteger(String.valueOf(0));
    private BigInteger nF35 = new BigInteger(String.valueOf(0));
    private BigInteger nF40 = new BigInteger(String.valueOf(0));
    private BigInteger nF45 = new BigInteger(String.valueOf(0));
    private BigInteger nF50 = new BigInteger(String.valueOf(0));

    private BigInteger nMU = new BigInteger(String.valueOf(0));
    private BigInteger nM0 = new BigInteger(String.valueOf(0));
    private BigInteger nM1 = new BigInteger(String.valueOf(0));
    private BigInteger nM5 = new BigInteger(String.valueOf(0));
    private BigInteger nM10 = new BigInteger(String.valueOf(0));
    private BigInteger nM15 = new BigInteger(String.valueOf(0));
    private BigInteger nM20 = new BigInteger(String.valueOf(0));
    private BigInteger nM25 = new BigInteger(String.valueOf(0));
    private BigInteger nM30 = new BigInteger(String.valueOf(0));
    private BigInteger nM35 = new BigInteger(String.valueOf(0));
    private BigInteger nM40 = new BigInteger(String.valueOf(0));
    private BigInteger nM45 = new BigInteger(String.valueOf(0));
    private BigInteger nM50 = new BigInteger(String.valueOf(0));

    private Integer total;

    public BigInteger getOrgId() {return orgId;}

    public void setOrgId(BigInteger orgId) {this.orgId = orgId;}

    public BigInteger getpM50() {return pM50;}

    public void setpM50(BigInteger pM50) {this.pM50 = pM50;}

    public String getOrgCode() {return orgCode;}

    public void setOrgCode(String orgCode) {this.orgCode = orgCode;}

    public String getOrgName() {return orgName;}

    public void setOrgName(String orgName) {this.orgName = orgName;}

    public String getProvinceName() {return provinceName;}

    public void setProvinceName(String provinceName) {this.provinceName = provinceName;}

    public String getDistrictName() {return districtName;}

    public void setDistrictName(String districtName) {this.districtName = districtName;}

    public String getSupport() {return support;}

    public void setSupport(String support) {this.support = support;}

    public String getModality() {return modality;}

    public void setModality(String modality) {this.modality = modality;}

    public BigInteger getpPWID() {return pPWID;}

    public void setpPWID(BigInteger pPWID) {this.pPWID = pPWID;}

    public BigInteger getnPWID() {return nPWID;}

    public void setnPWID(BigInteger nPWID) {this.nPWID = nPWID;}

    public BigInteger getpMSM() {return pMSM;}

    public void setpMSM(BigInteger pMSM) {this.pMSM = pMSM;}

    public BigInteger getnMSM() {return nMSM;}

    public void setnMSM(BigInteger nMSM) {this.nMSM = nMSM;}

    public BigInteger getpTG() {return pTG;}

    public void setpTG(BigInteger pTG) {this.pTG = pTG;}

    public BigInteger getnTG() {return nTG;}

    public void setnTG(BigInteger nTG) {this.nTG = nTG;}

    public BigInteger getpFSW() {return pFSW;}

    public void setpFSW(BigInteger pFSW) {this.pFSW = pFSW;}

    public BigInteger getnFSW() {return nFSW;}

    public void setnFSW(BigInteger nFSW) {this.nFSW = nFSW;}

    public BigInteger getpOther() {return pOther;}

    public void setpOther(BigInteger pOther) {this.pOther = pOther;}

    public BigInteger getnOther() {return nOther;}

    public void setnOther(BigInteger nOther) {this.nOther = nOther;}

    public BigInteger getpFU() {return pFU;}

    public void setpFU(BigInteger pFU) {this.pFU = pFU;}

    public BigInteger getpF0() {return pF0;}

    public void setpF0(BigInteger pF0) {this.pF0 = pF0;}

    public BigInteger getpF1() {return pF1;}

    public void setpF1(BigInteger pF1) {this.pF1 = pF1;}

    public BigInteger getpF5() {return pF5;}

    public void setpF5(BigInteger pF5) {this.pF5 = pF5;}

    public BigInteger getpF10() {return pF10;}

    public void setpF10(BigInteger pF10) {this.pF10 = pF10;}

    public BigInteger getpF15() {return pF15;}

    public void setpF15(BigInteger pF15) {this.pF15 = pF15;}

    public BigInteger getpF20() {return pF20;}

    public void setpF20(BigInteger pF20) {this.pF20 = pF20;}

    public BigInteger getpF25() {return pF25;}

    public void setpF25(BigInteger pF25) {this.pF25 = pF25;}

    public BigInteger getpF30() {return pF30;}

    public void setpF30(BigInteger pF30) {this.pF30 = pF30;}

    public BigInteger getpF35() {return pF35;}

    public void setpF35(BigInteger pF35) {this.pF35 = pF35;}

    public BigInteger getpF40() {return pF40;}

    public void setpF40(BigInteger pF40) {this.pF40 = pF40;}

    public BigInteger getpF45() {return pF45;}

    public void setpF45(BigInteger pF45) {this.pF45 = pF45;}

    public BigInteger getpF50() {return pF50;}

    public void setpF50(BigInteger pF50) {this.pF50 = pF50;}

    public BigInteger getpMU() {return pMU;}

    public void setpMU(BigInteger pMU) {this.pMU = pMU;}

    public BigInteger getpM0() {return pM0;}

    public void setpM0(BigInteger pM0) {this.pM0 = pM0;}

    public BigInteger getpM1() {return pM1;}

    public void setpM1(BigInteger pM1) {this.pM1 = pM1;}

    public BigInteger getpM5() {return pM5;}

    public void setpM5(BigInteger pM5) {this.pM5 = pM5;}

    public BigInteger getpM10() {return pM10;}

    public void setpM10(BigInteger pM10) {this.pM10 = pM10;}

    public BigInteger getpM15() {return pM15;}

    public void setpM15(BigInteger pM15) {this.pM15 = pM15;}

    public BigInteger getpM20() {return pM20;}

    public void setpM20(BigInteger pM20) {this.pM20 = pM20;}

    public BigInteger getpM25() {return pM25;}

    public void setpM25(BigInteger pM25) {this.pM25 = pM25;}

    public BigInteger getpM30() {return pM30;}

    public void setpM30(BigInteger pM30) {this.pM30 = pM30;}

    public BigInteger getpM35() {return pM35;}

    public void setpM35(BigInteger pM35) {this.pM35 = pM35;}

    public BigInteger getpM40() {return pM40;}

    public void setpM40(BigInteger pM40) {this.pM40 = pM40;}

    public BigInteger getpM45() {return pM45;}

    public void setpM45(BigInteger pM45) {this.pM45 = pM45;}

    public BigInteger getnFU() {return nFU;}

    public void setnFU(BigInteger nFU) {this.nFU = nFU;}

    public BigInteger getnF0() {return nF0;}

    public void setnF0(BigInteger nF0) {this.nF0 = nF0;}

    public BigInteger getnF1() {return nF1;}

    public void setnF1(BigInteger nF1) {this.nF1 = nF1;}

    public BigInteger getnF5() {return nF5;}

    public void setnF5(BigInteger nF5) {this.nF5 = nF5;}

    public BigInteger getnF10() {return nF10;}

    public void setnF10(BigInteger nF10) {this.nF10 = nF10;}

    public BigInteger getnF15() {return nF15;}

    public void setnF15(BigInteger nF15) {this.nF15 = nF15;}

    public BigInteger getnF20() {return nF20;}

    public void setnF20(BigInteger nF20) {this.nF20 = nF20;}

    public BigInteger getnF25() {return nF25;}

    public void setnF25(BigInteger nF25) {this.nF25 = nF25;}

    public BigInteger getnF30() {return nF30;}

    public void setnF30(BigInteger nF30) {this.nF30 = nF30;}

    public BigInteger getnF35() {return nF35;}

    public void setnF35(BigInteger nF35) {this.nF35 = nF35;}

    public BigInteger getnF40() {return nF40;}

    public void setnF40(BigInteger nF40) {this.nF40 = nF40;}

    public BigInteger getnF45() {return nF45;}

    public void setnF45(BigInteger nF45) {this.nF45 = nF45;}

    public BigInteger getnF50() {return nF50;}

    public void setnF50(BigInteger nF50) {this.nF50 = nF50;}

    public BigInteger getnMU() {return nMU;}

    public void setnMU(BigInteger nMU) {this.nMU = nMU;}

    public BigInteger getnM0() {return nM0;}

    public void setnM0(BigInteger nM0) {this.nM0 = nM0;}

    public BigInteger getnM1() {return nM1;}

    public void setnM1(BigInteger nM1) {this.nM1 = nM1;}

    public BigInteger getnM5() {return nM5;}

    public void setnM5(BigInteger nM5) {this.nM5 = nM5;}

    public BigInteger getnM10() {return nM10;}

    public void setnM10(BigInteger nM10) {this.nM10 = nM10;}

    public BigInteger getnM15() {return nM15;}

    public void setnM15(BigInteger nM15) {this.nM15 = nM15;}

    public BigInteger getnM20() {return nM20;}

    public void setnM20(BigInteger nM20) {this.nM20 = nM20;}

    public BigInteger getnM25() {return nM25;}

    public void setnM25(BigInteger nM25) {this.nM25 = nM25;}

    public BigInteger getnM30() {return nM30;}

    public void setnM30(BigInteger nM30) {this.nM30 = nM30;}

    public BigInteger getnM35() {return nM35;}

    public void setnM35(BigInteger nM35) {this.nM35 = nM35;}

    public BigInteger getnM40() {return nM40;}

    public void setnM40(BigInteger nM40) {this.nM40 = nM40;}

    public BigInteger getnM45() {return nM45;}

    public void setnM45(BigInteger nM45) {this.nM45 = nM45;}

    public BigInteger getnM50() {return nM50;}

    public void setnM50(BigInteger nM50) {this.nM50 = nM50;}

    public Integer getTotal() {
        total = 0;

        if (pFU != null) {
            total += pFU.intValue();
        }
        if (pF0 != null) {
            total += pF0.intValue();
        }
        if (pF1 != null) {
            total += pF1.intValue();
        }
        if (pF5 != null) {
            total += pF5.intValue();
        }
        if (pF10 != null) {
            total += pF10.intValue();
        }
        if (pF15 != null) {
            total += pF15.intValue();
        }
        if (pF20 != null) {
            total += pF20.intValue();
        }
        if (pF25 != null) {
            total += pF25.intValue();
        }
        if (pF30 != null) {
            total += pF30.intValue();
        }
        if (pF35 != null) {
            total += pF35.intValue();
        }
        if (pF40 != null) {
            total += pF40.intValue();
        }
        if (pF45 != null) {
            total += pF45.intValue();
        }
        if (pF50 != null) {
            total += pF50.intValue();
        }

        if (pMU != null) {
            total += pMU.intValue();
        }
        if (pM0 != null) {
            total += pM0.intValue();
        }
        if (pM1 != null) {
            total += pM1.intValue();
        }
        if (pM5 != null) {
            total += pM5.intValue();
        }
        if (pM10 != null) {
            total += pM10.intValue();
        }
        if (pM15 != null) {
            total += pM15.intValue();
        }
        if (pM20 != null) {
            total += pM20.intValue();
        }
        if (pM25 != null) {
            total += pM25.intValue();
        }
        if (pM30 != null) {
            total += pM30.intValue();
        }
        if (pM35 != null) {
            total += pM35.intValue();
        }
        if (pM40 != null) {
            total += pM40.intValue();
        }
        if (pM45 != null) {
            total += pM45.intValue();
        }
        if (pM50 != null) {
            total += pM50.intValue();
        }

        if (nFU != null) {
            total += nFU.intValue();
        }
        if (nF0 != null) {
            total += nF0.intValue();
        }
        if (nF1 != null) {
            total += nF1.intValue();
        }
        if (nF5 != null) {
            total += nF5.intValue();
        }
        if (nF10 != null) {
            total += nF10.intValue();
        }
        if (nF15 != null) {
            total += nF15.intValue();
        }
        if (nF20 != null) {
            total += nF20.intValue();
        }
        if (nF25 != null) {
            total += nF25.intValue();
        }
        if (nF30 != null) {
            total += nF30.intValue();
        }
        if (nF35 != null) {
            total += nF35.intValue();
        }
        if (nF40 != null) {
            total += nF40.intValue();
        }
        if (nF45 != null) {
            total += nF45.intValue();
        }
        if (nF50 != null) {
            total += nF50.intValue();
        }

        if (nMU != null) {
            total += nMU.intValue();
        }
        if (nM0 != null) {
            total += nM0.intValue();
        }
        if (nM1 != null) {
            total += nM1.intValue();
        }
        if (nM5 != null) {
            total += nM5.intValue();
        }
        if (nM10 != null) {
            total += nM10.intValue();
        }
        if (nM15 != null) {
            total += nM15.intValue();
        }
        if (nM20 != null) {
            total += nM20.intValue();
        }
        if (nM25 != null) {
            total += nM25.intValue();
        }
        if (nM30 != null) {
            total += nM30.intValue();
        }
        if (nM35 != null) {
            total += nM35.intValue();
        }
        if (nM40 != null) {
            total += nM40.intValue();
        }
        if (nM45 != null) {
            total += nM45.intValue();
        }
        if (nM50 != null) {
            total += nM50.intValue();
        }

        return total;
    }

    public void setTotal(Integer total) {this.total = total;}
}
