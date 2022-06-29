package org.pepfar.pdma.app.data.dto;

public class STIDetailReportDto {

	//	Khách hàng làm xét nghiệm Giang mai hoặc không
	private String titleSTI;

	//	Khách hàng làm xét nghiệm HIV
	//	Âm tính
	private Integer negativeHTS;

	//	Dương tính
	private Integer positiveHTS;

	//	Không xác định
	private Integer unknownHTS;

	//	Khách hàng không làm xét nghiệm HIV
	private Integer notHTS;

	private int orderNumber=0;

	public int getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(int orderNumber) {
		this.orderNumber = orderNumber;
	}

	public String getTitleSTI() {
		return titleSTI;
	}

	public void setTitleSTI(String titleSTI) {
		this.titleSTI = titleSTI;
	}

	public Integer getNegativeHTS() {
		return negativeHTS;
	}

	public void setNegativeHTS(Integer negativeHTS) {
		this.negativeHTS = negativeHTS;
	}

	public Integer getPositiveHTS() {
		return positiveHTS;
	}

	public void setPositiveHTS(Integer positiveHTS) {
		this.positiveHTS = positiveHTS;
	}

	public Integer getUnknownHTS() {
		return unknownHTS;
	}

	public void setUnknownHTS(Integer unknownHTS) {
		this.unknownHTS = unknownHTS;
	}

	public Integer getNotHTS() {
		return notHTS;
	}

	public void setNotHTS(Integer notHTS) {
		this.notHTS = notHTS;
	}
}