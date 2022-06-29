package org.pepfar.pdma.app.data.dto;

public class CheckNationalIdDto {

    private int result;

    private CaseOrgDto co;

    public CaseOrgDto getCo() {
        return co;
    }

    public void setCo(CaseOrgDto co) {
        this.co = co;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }
}
