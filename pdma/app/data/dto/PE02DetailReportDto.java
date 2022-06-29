package org.pepfar.pdma.app.data.dto;

public class PE02DetailReportDto {
	//STT
	private String seq;
	
//	CHỈ SỐ
	private String title;
	
//	Tiếp cận
	private Integer approach;

//	Đã biết tình trạng (+) khi tiếp cận
	private Integer positiveHivStatus;
	
//	Đồng ý và đã làm xét nghiệm
	private Integer agreeToBeTested;
	
//	Xét nghiệm có phản ứng (+)
	private Integer positiveHivRapidTesting;
	
//	Xét nghiệm khẳng định (+)
	private Integer positiveHivConfirmTesting;
	
//	Chuyển gửi điều trị ARV thành công
	private Integer sentToArvTreatment;
	
//	Chuyển gửi điều trị PrEP thành công
	private Integer sentToPrEP;
	
	private int orderNumber=0;
	
	public String getSeq() {
		return seq;
	}
	public void setSeq(String seq) {
		this.seq = seq;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Integer getApproach() {
		return approach;
	}
	public void setApproach(Integer approach) {
		this.approach = approach;
	}
	public Integer getPositiveHivStatus() {
		return positiveHivStatus;
	}
	public void setPositiveHivStatus(Integer positiveHivStatus) {
		this.positiveHivStatus = positiveHivStatus;
	}
	public Integer getAgreeToBeTested() {
		return agreeToBeTested;
	}
	public void setAgreeToBeTested(Integer agreeToBeTested) {
		this.agreeToBeTested = agreeToBeTested;
	}
	public Integer getPositiveHivRapidTesting() {
		return positiveHivRapidTesting;
	}
	public void setPositiveHivRapidTesting(Integer positiveHivRapidTesting) {
		this.positiveHivRapidTesting = positiveHivRapidTesting;
	}
	public Integer getPositiveHivConfirmTesting() {
		return positiveHivConfirmTesting;
	}
	public void setPositiveHivConfirmTesting(Integer positiveHivConfirmTesting) {
		this.positiveHivConfirmTesting = positiveHivConfirmTesting;
	}
	public Integer getSentToArvTreatment() {
		return sentToArvTreatment;
	}
	public void setSentToArvTreatment(Integer sentToArvTreatment) {
		this.sentToArvTreatment = sentToArvTreatment;
	}
	public Integer getSentToPrEP() {
		return sentToPrEP;
	}
	public void setSentToPrEP(Integer sentToPrEP) {
		this.sentToPrEP = sentToPrEP;
	}
	public int getOrderNumber() {
		return orderNumber;
	}
	public void setOrderNumber(int orderNumber) {
		this.orderNumber = orderNumber;
	}
}
