package org.pepfar.pdma.app.data.dto;

import java.util.ArrayList;
import java.util.List;

public class ImportResultDto<T> {
    private int totalRow = 0;
    private int totalErr = 0;
    private int totalSuccess = 0;
    private List<T> listErr = new ArrayList<T>();
    private List<T> listDuplicateCode = new ArrayList<T>();
    private List<T> listSuccess = new ArrayList<T>();

    public int getTotalRow() {
        return totalRow;
    }

    public void setTotalRow(int totalRow) {
        this.totalRow = totalRow;
    }

    public int getTotalErr() {
        if (listErr != null && listErr.size() > 0) {
            totalErr = listErr.size();
        }
        return totalErr;
    }

    public void setTotalErr(int totalErr) {
        this.totalErr = totalErr;
    }

    public List<T> getListSuccess() {return listSuccess;}

    public void setListSuccess(List<T> listSuccess) {this.listSuccess = listSuccess;}

    public int getTotalSuccess() {
        return totalSuccess;
    }

    public void setTotalSuccess(int totalSuccess) {
        this.totalSuccess = totalSuccess;
    }

    public List<T> getListErr() {
        return listErr;
    }

    public void setListErr(List<T> listErr) {
        this.listErr = listErr;
    }

    public List<T> getListDuplicateCode() {
        return listDuplicateCode;
    }

    public void setListDuplicateCode(List<T> listDuplicateCode) {
        this.listDuplicateCode = listDuplicateCode;
    }
}
