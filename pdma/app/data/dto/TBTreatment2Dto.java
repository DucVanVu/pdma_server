package org.pepfar.pdma.app.data.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import org.pepfar.pdma.app.data.domain.TBTreatment2;
import org.pepfar.pdma.app.data.types.TBClassification1;
import org.pepfar.pdma.app.data.types.TBClassification2;
import org.pepfar.pdma.app.data.types.TBClassification3;
import org.pepfar.pdma.app.data.types.TBClassification4;
import org.pepfar.pdma.app.data.types.TBExtraDiagnosis;
import org.pepfar.pdma.app.data.types.TBScreeningType;
import org.pepfar.pdma.app.data.types.YesNoNone;
import org.pepfar.pdma.app.utils.CustomLocalDateTimeDeserializer2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class TBTreatment2Dto extends AuditableEntityDto {
	
	private Long id;

	private UUID uid;

	private OrganizationDto organization;

	private CaseDto theCase;
	// Tình huống sàng lọc lao
	private TBScreeningType screeningType;
	// Ngày sàng lọc lao
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime screeningDate;
	// Xét nghiệm soi đờm	
	private YesNoNone sputumSmear;

	// Xét nghiệm Xpert
	private YesNoNone xpert;

	// Chụp phổi
	private YesNoNone xray;

	// XN chẩn đoán lao khác
	private TBExtraDiagnosis otherTest;

	// Chẩn đoán mắc lao?
	private YesNoNone tbDiagnosed;

	// Tên cơ sở chẩn đoán Lao
	private String tbDiagnosisFacility;

	// Thời điểm chẩn đoán Lao
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime tbDiagnosisDate;

	// Phân loại bệnh lao theo vị trí giải phẫu
	private TBClassification1 classification1;

	// Phân loại bệnh lao (chi tiết theo vị trí giải phẫu, nếu có)
	private TBClassification2 classification2;

	// Phân loại bệnh lao (chi tiết theo tiền sử bệnh, nếu có)
	private TBClassification3 classification3;

	// Phân loại bệnh lao (chi tiết theo kết quả xét nghiệm vi khuẩn, nếu có)
	private TBClassification4 classification4;

	private String tbTxFacility;

	private String tbTxPatientCode;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime tbTxStartDate;
	
	public TBTreatment2Dto() {
		super();
	}
	
	public TBTreatment2Dto(TBTreatment2 entity,boolean collapse) {
		super(entity);
		this.id = entity.getId();
		this.uid = entity.getUid();
		this.screeningType = entity.getScreeningType();
		this.screeningDate = entity.getScreeningDate();
		this.sputumSmear = entity.getSputumSmear();
		this.xpert = entity.getXpert();
		this.xray = entity.getXray();
		this.otherTest = entity.getOtherTest();
		this.tbDiagnosed = entity.getTbDiagnosed();
		this.tbDiagnosisFacility = entity.getTbDiagnosisFacility();
		this.tbDiagnosisDate = entity.getTbDiagnosisDate();
		this.classification1 = entity.getClassification1();
		this.classification2 = entity.getClassification2();
		this.classification3 = entity.getClassification3();
		this.classification4 = entity.getClassification4();
		this.tbTxFacility = entity.getTbTxFacility();
		this.tbTxPatientCode = entity.getTbTxPatientCode();
		this.tbTxStartDate = entity.getTbTxStartDate();
		if(!collapse) {
			if(entity.getOrganization()!=null) {
				this.organization = new OrganizationDto(entity.getOrganization(),true);
			}
			if(entity.getTheCase()!=null) {
				this.theCase = new CaseDto(entity.getTheCase(), true);
			}
		}
	}
	public TBTreatment2 toEntity() {
		TBTreatment2 entity = new TBTreatment2(); 
		entity = (TBTreatment2)super.toEntity(entity);		
		entity.setId(id);
		entity.setUid(uid);
		if(this.organization!=null) {
			entity.setOrganization(this.organization.toEntity());
		}
		if(this.theCase!=null) {
			entity.setTheCase(this.theCase.toEntity());
		}
		entity.setScreeningType(screeningType);
		entity.setScreeningDate(screeningDate);
		entity.setSputumSmear(sputumSmear);
		entity.setXpert(xpert);
		entity.setXray(xray);
		entity.setOtherTest(otherTest);
		entity.setTbDiagnosed(tbDiagnosed);
		entity.setTbDiagnosisFacility(tbDiagnosisFacility);
		entity.setTbDiagnosisDate(tbDiagnosisDate);
		entity.setClassification1(classification1);
		entity.setClassification2(classification2);
		entity.setClassification3(classification3);
		entity.setClassification4(classification4);
		entity.setTbTxFacility(tbTxFacility);
		entity.setTbTxPatientCode(tbTxPatientCode);
		entity.setTbTxStartDate(tbTxStartDate);
		return entity;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public UUID getUid() {
		return uid;
	}
	public void setUid(UUID uid) {
		this.uid = uid;
	}
	public OrganizationDto getOrganization() {
		return organization;
	}
	public void setOrganization(OrganizationDto organization) {
		this.organization = organization;
	}
	public CaseDto getTheCase() {
		return theCase;
	}
	public void setTheCase(CaseDto theCase) {
		this.theCase = theCase;
	}
	
	public TBScreeningType getScreeningType() {
		return screeningType;
	}

	public void setScreeningType(TBScreeningType screeningType) {
		this.screeningType = screeningType;
	}

	public LocalDateTime getScreeningDate() {
		return screeningDate;
	}

	public void setScreeningDate(LocalDateTime screeningDate) {
		this.screeningDate = screeningDate;
	}

	public YesNoNone getSputumSmear() {
		return sputumSmear;
	}
	public void setSputumSmear(YesNoNone sputumSmear) {
		this.sputumSmear = sputumSmear;
	}
	public YesNoNone getXpert() {
		return xpert;
	}
	public void setXpert(YesNoNone xpert) {
		this.xpert = xpert;
	}
	public YesNoNone getXray() {
		return xray;
	}
	public void setXray(YesNoNone xray) {
		this.xray = xray;
	}
	public TBExtraDiagnosis getOtherTest() {
		return otherTest;
	}
	public void setOtherTest(TBExtraDiagnosis otherTest) {
		this.otherTest = otherTest;
	}
	public YesNoNone getTbDiagnosed() {
		return tbDiagnosed;
	}
	public void setTbDiagnosed(YesNoNone tbDiagnosed) {
		this.tbDiagnosed = tbDiagnosed;
	}
	public String getTbDiagnosisFacility() {
		return tbDiagnosisFacility;
	}
	public void setTbDiagnosisFacility(String tbDiagnosisFacility) {
		this.tbDiagnosisFacility = tbDiagnosisFacility;
	}
	public LocalDateTime getTbDiagnosisDate() {
		return tbDiagnosisDate;
	}
	public void setTbDiagnosisDate(LocalDateTime tbDiagnosisDate) {
		this.tbDiagnosisDate = tbDiagnosisDate;
	}
	public TBClassification1 getClassification1() {
		return classification1;
	}
	public void setClassification1(TBClassification1 classification1) {
		this.classification1 = classification1;
	}
	public TBClassification2 getClassification2() {
		return classification2;
	}
	public void setClassification2(TBClassification2 classification2) {
		this.classification2 = classification2;
	}
	public TBClassification3 getClassification3() {
		return classification3;
	}
	public void setClassification3(TBClassification3 classification3) {
		this.classification3 = classification3;
	}
	public TBClassification4 getClassification4() {
		return classification4;
	}
	public void setClassification4(TBClassification4 classification4) {
		this.classification4 = classification4;
	}
	public String getTbTxFacility() {
		return tbTxFacility;
	}
	public void setTbTxFacility(String tbTxFacility) {
		this.tbTxFacility = tbTxFacility;
	}
	public String getTbTxPatientCode() {
		return tbTxPatientCode;
	}
	public void setTbTxPatientCode(String tbTxPatientCode) {
		this.tbTxPatientCode = tbTxPatientCode;
	}
	public LocalDateTime getTbTxStartDate() {
		return tbTxStartDate;
	}
	public void setTbTxStartDate(LocalDateTime tbTxStartDate) {
		this.tbTxStartDate = tbTxStartDate;
	}

}
