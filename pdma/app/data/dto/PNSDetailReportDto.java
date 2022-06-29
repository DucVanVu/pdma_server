package org.pepfar.pdma.app.data.dto;

public class PNSDetailReportDto {
	//STT
	private String seq;
	
//	CHỈ SỐ
	private String title;
	
//	Cộng đồng
	private Integer community;

	//	Tại cơ sở cung cấp dịch vụ
	private Integer medicalFacility;
	
//	Tại cơ sở khác
	private Integer otherMedicalFacility;
	
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

	public Integer getCommunity() {
		return community;
	}

	public void setCommunity(Integer community) {
		this.community = community;
	}

	public Integer getMedicalFacility() {
		return medicalFacility;
	}

	public void setMedicalFacility(Integer medicalFacility) {
		this.medicalFacility = medicalFacility;
	}

	public Integer getOtherMedicalFacility() {
		return otherMedicalFacility;
	}

	public void setOtherMedicalFacility(Integer otherMedicalFacility) {
		this.otherMedicalFacility = otherMedicalFacility;
	}

	public int getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(int orderNumber) {
		this.orderNumber = orderNumber;
	}
}
