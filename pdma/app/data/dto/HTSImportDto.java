package org.pepfar.pdma.app.data.dto;

import java.util.Date;

public class HTSImportDto {

	private String codeOrg;//mã số cơ sở tư vấn xét nghiệm hiv
	private Date c4;
	private String c6;//mã số khách hàng
	private String c23FullName;
	private String gender;
	private Integer c8;
	
	private String c23IdOrHealth;
	private String c23IdNumber;
	private String c23HealthNumber;
	
	private String c23CurrentAddressProvince;
	
	private String c23CurrentAddressDistrict;
	
	private String c23CurrentAddressCommune;
	
	private String c23CurrentAddressDetail;
	
	private String risk1;//tiêm chích ma túy;
	private String risk2;//mại dâm
	private String risk3;//QHTD với nhiều ngư
	private String risk4;//Nam QHTD đồng giới
	private String risk5;//mặc bệnh LTQDTD
	private String risk6;//truyền máu
	private String risk7;//rủi ro nghề nghiệp
	private String risk8;//qua đường máu khác
	private String risk9;//vợ, chồng, bạn tình có HIV
	private String risk10;//vợ, chồng, bạn tình thuộc nhóm nguy cơ cao
	private String risk11;//bệnh nhận lao
	private String risk12;//không rõ nguy cơ
	private String risk13;//phụ nư mang thai
	private String HIVResult1;
	private String c14;
	private String c17;
	private String c18;
	private Date c15Date;
	private Date ARV;
	private String placeARV;
	private String isPNS;
	private Integer contactPNS;
	private Integer contactPNSHIV;
	private String note;
	private String err;
	
	
	public String getIsPNS() {
		return isPNS;
	}
	public void setIsPNS(String isPNS) {
		this.isPNS = isPNS;
	}
	public Integer getContactPNS() {
		return contactPNS;
	}
	public void setContactPNS(Integer contactPNS) {
		this.contactPNS = contactPNS;
	}
	public Integer getContactPNSHIV() {
		return contactPNSHIV;
	}
	public void setContactPNSHIV(Integer contactPNSHIV) {
		this.contactPNSHIV = contactPNSHIV;
	}
	public String getCodeOrg() {
		return codeOrg;
	}
	public void setCodeOrg(String codeOrg) {
		this.codeOrg = codeOrg;
	}
	public Date getC4() {
		return c4;
	}
	public void setC4(Date c4) {
		this.c4 = c4;
	}
	public String getC6() {
		return c6;
	}
	public void setC6(String c6) {
		this.c6 = c6;
	}
	public String getC23FullName() {
		return c23FullName;
	}
	public void setC23FullName(String c23FullName) {
		this.c23FullName = c23FullName;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public Integer getC8() {
		return c8;
	}
	public void setC8(Integer c8) {
		this.c8 = c8;
	}
	public String getC23IdOrHealth() {
		return c23IdOrHealth;
	}
	public void setC23IdOrHealth(String c23IdOrHealth) {
		this.c23IdOrHealth = c23IdOrHealth;
	}
	public String getC23IdNumber() {
		return c23IdNumber;
	}
	public void setC23IdNumber(String c23IdNumber) {
		this.c23IdNumber = c23IdNumber;
	}
	public String getC23HealthNumber() {
		return c23HealthNumber;
	}
	public void setC23HealthNumber(String c23HealthNumber) {
		this.c23HealthNumber = c23HealthNumber;
	}
	public String getC23CurrentAddressProvince() {
		return c23CurrentAddressProvince;
	}
	public void setC23CurrentAddressProvince(String c23CurrentAddressProvince) {
		this.c23CurrentAddressProvince = c23CurrentAddressProvince;
	}
	public String getC23CurrentAddressDistrict() {
		return c23CurrentAddressDistrict;
	}
	public void setC23CurrentAddressDistrict(String c23CurrentAddressDistrict) {
		this.c23CurrentAddressDistrict = c23CurrentAddressDistrict;
	}
	public String getC23CurrentAddressCommune() {
		return c23CurrentAddressCommune;
	}
	public void setC23CurrentAddressCommune(String c23CurrentAddressCommune) {
		this.c23CurrentAddressCommune = c23CurrentAddressCommune;
	}
	public String getC23CurrentAddressDetail() {
		return c23CurrentAddressDetail;
	}
	public void setC23CurrentAddressDetail(String c23CurrentAddressDetail) {
		this.c23CurrentAddressDetail = c23CurrentAddressDetail;
	}
	public String getRisk1() {
		return risk1;
	}
	public void setRisk1(String risk1) {
		this.risk1 = risk1;
	}
	public String getRisk2() {
		return risk2;
	}
	public void setRisk2(String risk2) {
		this.risk2 = risk2;
	}
	public String getRisk3() {
		return risk3;
	}
	public void setRisk3(String risk3) {
		this.risk3 = risk3;
	}
	public String getRisk4() {
		return risk4;
	}
	public void setRisk4(String risk4) {
		this.risk4 = risk4;
	}
	public String getRisk5() {
		return risk5;
	}
	public void setRisk5(String risk5) {
		this.risk5 = risk5;
	}
	public String getRisk6() {
		return risk6;
	}
	public void setRisk6(String risk6) {
		this.risk6 = risk6;
	}
	public String getRisk7() {
		return risk7;
	}
	public void setRisk7(String risk7) {
		this.risk7 = risk7;
	}
	public String getRisk8() {
		return risk8;
	}
	public void setRisk8(String risk8) {
		this.risk8 = risk8;
	}
	public String getRisk9() {
		return risk9;
	}
	public void setRisk9(String risk9) {
		this.risk9 = risk9;
	}
	public String getRisk10() {
		return risk10;
	}
	public void setRisk10(String risk10) {
		this.risk10 = risk10;
	}
	public String getRisk11() {
		return risk11;
	}
	public void setRisk11(String risk11) {
		this.risk11 = risk11;
	}
	public String getRisk12() {
		return risk12;
	}
	public void setRisk12(String risk12) {
		this.risk12 = risk12;
	}
	public String getRisk13() {
		return risk13;
	}
	public void setRisk13(String risk13) {
		this.risk13 = risk13;
	}
	public String getHIVResult1() {
		return HIVResult1;
	}
	public void setHIVResult1(String hIVResult1) {
		HIVResult1 = hIVResult1;
	}
	public String getC14() {
		return c14;
	}
	public void setC14(String c14) {
		this.c14 = c14;
	}
	public String getC17() {
		return c17;
	}
	public void setC17(String c17) {
		this.c17 = c17;
	}
	public String getC18() {
		return c18;
	}
	public void setC18(String c18) {
		this.c18 = c18;
	}
	public Date getC15Date() {
		return c15Date;
	}
	public void setC15Date(Date c15Date) {
		this.c15Date = c15Date;
	}
	public Date getARV() {
		return ARV;
	}
	public void setARV(Date aRV) {
		ARV = aRV;
	}
	public String getPlaceARV() {
		return placeARV;
	}
	public void setPlaceARV(String placeARV) {
		this.placeARV = placeARV;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public String getErr() {
		return err;
	}
	public void setErr(String err) {
		this.err = err;
	}
	
	
}
