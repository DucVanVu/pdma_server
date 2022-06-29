package org.pepfar.pdma.app.data.dto;

import java.util.List;

public class OPCDashboardChartData {

	private List<OPCDashboardPatientChartData3> patientData;

	private List<OPCDashboardVLChartData2> vlData;

	private List<OPCDashboardRiskGroupChartData2> riskGroupData;

	private OPCDashboardTLDChartData2 tldData;

	private OPCDashboardMMDChartData mmdData;

	private List<OPCDashboardTBScreeningChartData2> tbScreenData;

	public List<OPCDashboardPatientChartData3> getPatientData() {
		return patientData;
	}

	public void setPatientData(List<OPCDashboardPatientChartData3> patientData) {
		this.patientData = patientData;
	}

	public List<OPCDashboardVLChartData2> getVlData() {
		return vlData;
	}

	public void setVlData(List<OPCDashboardVLChartData2> vlData) {
		this.vlData = vlData;
	}

	public List<OPCDashboardRiskGroupChartData2> getRiskGroupData() {
		return riskGroupData;
	}

	public void setRiskGroupData(List<OPCDashboardRiskGroupChartData2> riskGroupData) {
		this.riskGroupData = riskGroupData;
	}

	public OPCDashboardTLDChartData2 getTldData() {
		return tldData;
	}

	public void setTldData(OPCDashboardTLDChartData2 tldData) {
		this.tldData = tldData;
	}

	public OPCDashboardMMDChartData getMmdData() {
		return mmdData;
	}

	public void setMmdData(OPCDashboardMMDChartData mmdData) {
		this.mmdData = mmdData;
	}

	public List<OPCDashboardTBScreeningChartData2> getTbScreenData() {
		return tbScreenData;
	}

	public void setTbScreenData(List<OPCDashboardTBScreeningChartData2> tbScreenData) {
		this.tbScreenData = tbScreenData;
	}

}
