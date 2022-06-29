package org.pepfar.pdma.app.data.dto;

public class SNSCaseReportDetailDto {
	private String name;
	private Integer total;
	private Integer riskGroupMSM;
	private Integer riskGroupNCH;
	private Integer riskGroupTCMT;
	private Integer riskGroupMD;
	private Integer riskGroupOTHER;
	
	private Integer customerSourceCBOs;
	private Integer customerSourceOTHER;
	private Integer customerSourceSNS;
	private Integer customerSourceVCTOPC;
	
	private Integer approachMethodDirect;
	private Integer approachMethodOnline;
	
	private Integer orderNumber;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getTotal() {
		return total;
	}
	public void setTotal(Integer total) {
		this.total = total;
	}
	public Integer getRiskGroupMSM() {
		return riskGroupMSM;
	}
	public void setRiskGroupMSM(Integer riskGroupMSM) {
		this.riskGroupMSM = riskGroupMSM;
	}
	public Integer getRiskGroupNCH() {
		return riskGroupNCH;
	}
	public void setRiskGroupNCH(Integer riskGroupNCH) {
		this.riskGroupNCH = riskGroupNCH;
	}
	public Integer getRiskGroupTCMT() {
		return riskGroupTCMT;
	}
	public void setRiskGroupTCMT(Integer riskGroupTCMT) {
		this.riskGroupTCMT = riskGroupTCMT;
	}
	public Integer getRiskGroupMD() {
		return riskGroupMD;
	}
	public void setRiskGroupMD(Integer riskGroupMD) {
		this.riskGroupMD = riskGroupMD;
	}
	public Integer getRiskGroupOTHER() {
		return riskGroupOTHER;
	}
	public void setRiskGroupOTHER(Integer riskGroupOTHER) {
		this.riskGroupOTHER = riskGroupOTHER;
	}
	public Integer getOrderNumber() {
		return orderNumber;
	}
	public void setOrderNumber(Integer orderNumber) {
		this.orderNumber = orderNumber;
	}
	public Integer getCustomerSourceCBOs() {
		return customerSourceCBOs;
	}
	public void setCustomerSourceCBOs(Integer customerSourceCBOs) {
		this.customerSourceCBOs = customerSourceCBOs;
	}
	public Integer getCustomerSourceOTHER() {
		return customerSourceOTHER;
	}
	public void setCustomerSourceOTHER(Integer customerSourceOTHER) {
		this.customerSourceOTHER = customerSourceOTHER;
	}
	public Integer getCustomerSourceSNS() {
		return customerSourceSNS;
	}
	public void setCustomerSourceSNS(Integer customerSourceSNS) {
		this.customerSourceSNS = customerSourceSNS;
	}
	public Integer getCustomerSourceVCTOPC() {
		return customerSourceVCTOPC;
	}
	public void setCustomerSourceVCTOPC(Integer customerSourceVCTOPC) {
		this.customerSourceVCTOPC = customerSourceVCTOPC;
	}
	public Integer getApproachMethodDirect() {
		return approachMethodDirect;
	}
	public void setApproachMethodDirect(Integer approachMethodDirect) {
		this.approachMethodDirect = approachMethodDirect;
	}
	public Integer getApproachMethodOnline() {
		return approachMethodOnline;
	}
	public void setApproachMethodOnline(Integer approachMethodOnline) {
		this.approachMethodOnline = approachMethodOnline;
	}
	
}
