package org.pepfar.pdma.app.data.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class WRChartDataDto {

	private LocalDateTime fromDate;

	private LocalDateTime toDate;

	private List<OrganizationDto> organizations;

	private long htsTstAnnTarget;

	private long htsPosAnnTarget;

	private long txNewAnnTarget;

	private long[] htsTstData;

	private long[] htsPosData;

	private long[] txNewData;

	private long[] txNewNDiagData; // TX_NEW: Dieu tri moi

	private long[] txNewODiagData; // TX_NEW: Cu chua dieu tri

	private long[] txNewOProvData; // TX_NEW: Ngoai tinh

	private long[] txNewOLTFData; // TX_NEW: Cu bo tri

	private long[] txNewIndeterminate; // TX_NEW: Chưa xác định

	private long[] txNewSameDayData;

	private long[] posNewData; // HTS_POS: Duong tinh moi

	private long[] posOldData; // HTS_POS: Duong tinh cu

	private long[] posOProvData; // HTS_POS: Ngoai tinh

	private long[] posIndeterminate; // HTS_POS: Chua xac dinh

	private double[] posRate;

	private long[] posRtriPos; // POS_RTRI+

	private long[] posOfferedPns; // POS_offered PNS

	public long[] htsTstCumData;

	private long[] htsPosCumData;

	private long[] txNewCumData;

	private int[] summary;

	private String lastUpdate;

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

	public List<OrganizationDto> getOrganizations() {

		if (organizations == null) {
			organizations = new ArrayList<>();
		}

		return organizations;
	}

	public void setOrganizations(List<OrganizationDto> organizations) {
		this.organizations = organizations;
	}

	public long getHtsTstAnnTarget() {
		return htsTstAnnTarget;
	}

	public void setHtsTstAnnTarget(long htsTstAnnTarget) {
		this.htsTstAnnTarget = htsTstAnnTarget;
	}

	public long getHtsPosAnnTarget() {
		return htsPosAnnTarget;
	}

	public void setHtsPosAnnTarget(long htsPosAnnTarget) {
		this.htsPosAnnTarget = htsPosAnnTarget;
	}

	public long getTxNewAnnTarget() {
		return txNewAnnTarget;
	}

	public void setTxNewAnnTarget(long txNewAnnTarget) {
		this.txNewAnnTarget = txNewAnnTarget;
	}

	public long[] getHtsTstData() {
		if (htsTstData == null) {
			htsTstData = new long[8];
		}
		return htsTstData;
	}

	public void setHtsTstData(long[] htsTstData) {
		this.htsTstData = htsTstData;
	}

	public long[] getHtsPosData() {
		if (htsPosData == null) {
			htsPosData = new long[8];
		}

		return htsPosData;
	}

	public void setHtsPosData(long[] htsPosData) {
		this.htsPosData = htsPosData;
	}

	public long[] getTxNewData() {
		if (txNewData == null) {
			txNewData = new long[8];
		}

		return txNewData;
	}

	public void setTxNewData(long[] txNewData) {
		this.txNewData = txNewData;
	}

	public long[] getTxNewNDiagData() {
		if (txNewNDiagData == null) {
			txNewNDiagData = new long[8];
		}

		return txNewNDiagData;
	}

	public void setTxNewNDiagData(long[] txNewNDiagData) {
		this.txNewNDiagData = txNewNDiagData;
	}

	public long[] getTxNewODiagData() {
		if (txNewODiagData == null) {
			txNewODiagData = new long[8];
		}

		return txNewODiagData;
	}

	public void setTxNewODiagData(long[] txNewODiagData) {
		this.txNewODiagData = txNewODiagData;
	}

	public long[] getTxNewOProvData() {
		if (txNewOProvData == null) {
			txNewOProvData = new long[8];
		}

		return txNewOProvData;
	}

	public void setTxNewOProvData(long[] txNewOProvData) {
		this.txNewOProvData = txNewOProvData;
	}

	public long[] getTxNewOLTFData() {
		if (txNewOLTFData == null) {
			txNewOLTFData = new long[8];
		}

		return txNewOLTFData;
	}

	public void setTxNewOLTFData(long[] txNewOLTFData) {
		this.txNewOLTFData = txNewOLTFData;
	}

	public long[] getTxNewSameDayData() {
		if (txNewSameDayData == null) {
			txNewSameDayData = new long[8];
		}

		return txNewSameDayData;
	}

	public void setTxNewSameDayData(long[] txNewSameDayData) {
		this.txNewSameDayData = txNewSameDayData;
	}

	public double[] getPosRate() {
		if (posRate == null) {
			posRate = new double[8];
		}

		return posRate;
	}

	public void setPosRate(double[] posRate) {
		this.posRate = posRate;
	}

	public long[] getPosRtriPos() {
		if (posRtriPos == null) {
			posRtriPos = new long[8];
		}
		return posRtriPos;
	}

	public void setPosRtriPos(long[] posRtriPos) {
		this.posRtriPos = posRtriPos;
	}

	public long[] getPosOfferedPns() {
		if (posOfferedPns == null) {
			posOfferedPns = new long[8];
		}
		return posOfferedPns;
	}

	public void setPosOfferedPns(long[] posOfferedPns) {
		this.posOfferedPns = posOfferedPns;
	}

	public long[] getTxNewIndeterminate() {
		if (txNewIndeterminate == null) {
			txNewIndeterminate = new long[8];
		}
		return txNewIndeterminate;
	}

	public void setTxNewIndeterminate(long[] txNewIndeterminate) {
		this.txNewIndeterminate = txNewIndeterminate;
	}

	public long[] getPosNewData() {
		if (posNewData == null) {
			posNewData = new long[8];
		}
		return posNewData;
	}

	public void setPosNewData(long[] posNewData) {
		this.posNewData = posNewData;
	}

	public long[] getPosOldData() {
		if (posOldData == null) {
			posOldData = new long[8];
		}
		return posOldData;
	}

	public void setPosOldData(long[] posOldData) {
		this.posOldData = posOldData;
	}

	public long[] getPosOProvData() {
		if (posOProvData == null) {
			posOProvData = new long[8];
		}
		return posOProvData;
	}

	public void setPosOProvData(long[] posOProvData) {
		this.posOProvData = posOProvData;
	}

	public long[] getPosIndeterminate() {
		if (posIndeterminate == null) {
			posIndeterminate = new long[8];
		}
		return posIndeterminate;
	}

	public void setPosIndeterminate(long[] posIndeterminate) {
		this.posIndeterminate = posIndeterminate;
	}

	public long[] getHtsTstCumData() {
		if (htsTstCumData == null) {
			htsTstCumData = new long[12];
		}

		return htsTstCumData;
	}

	public void setHtsTstCumData(long[] htsTstCumData) {
		this.htsTstCumData = htsTstCumData;
	}

	public long[] getHtsPosCumData() {
		if (htsPosCumData == null) {
			htsPosCumData = new long[12];
		}

		return htsPosCumData;
	}

	public void setHtsPosCumData(long[] htsPosCumData) {
		this.htsPosCumData = htsPosCumData;
	}

	public long[] getTxNewCumData() {
		if (txNewCumData == null) {
			txNewCumData = new long[12];
		}

		return txNewCumData;
	}

	public void setTxNewCumData(long[] txNewCumData) {
		this.txNewCumData = txNewCumData;
	}

	public int[] getSummary() {
		if (summary == null) {
			summary = new int[4];
		}
		return summary;
	}

	public void setSummary(int[] summary) {
		this.summary = summary;
	}

	public String getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(String lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
}
