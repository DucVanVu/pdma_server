package org.pepfar.pdma.app.data.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.pepfar.pdma.app.Constants;
import org.pepfar.pdma.app.data.domain.CaseOrg;
import org.pepfar.pdma.app.data.domain.HTSCase;
import org.pepfar.pdma.app.data.domain.HTSCaseRiskGroup;
import org.pepfar.pdma.app.data.types.Gender;
import org.pepfar.pdma.app.data.types.HTSYesNoNone;
import org.pepfar.pdma.app.data.types.HTSc10;
import org.pepfar.pdma.app.data.types.HTSc11a;
import org.pepfar.pdma.app.data.types.HTSc11b;
import org.pepfar.pdma.app.data.types.HTSc12;
import org.pepfar.pdma.app.data.types.HTSc131;
import org.pepfar.pdma.app.data.types.HTSc132;
import org.pepfar.pdma.app.data.types.HTSc14;
import org.pepfar.pdma.app.data.types.HTSc17;
import org.pepfar.pdma.app.data.types.HTSc18;
import org.pepfar.pdma.app.data.types.HTSc20;
import org.pepfar.pdma.app.data.types.HTSc24;
import org.pepfar.pdma.app.data.types.HTSc26;
import org.pepfar.pdma.app.data.types.HTSc28;
import org.pepfar.pdma.app.data.types.HTSc5;
import org.pepfar.pdma.app.data.types.HTSc5Note;
import org.pepfar.pdma.app.utils.CustomLocalDateTimeDeserializer2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.pepfar.pdma.app.utils.DateTimeUtil;

import javax.persistence.Column;

public class HTSCaseDto {
	private Long id;
	
	private UUID uid;
	
	private OrganizationDto c2;
	
	private StaffDto c3;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime c4;
	
	private HTSc5 c5;
	
	private HTSc5Note c5Note;
	
	private String c6;
	
	private Gender c7;
	
//	@JsonSerialize(using = ToStringSerializer.class)
//	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
//	private LocalDateTime c8;
	
	private Integer c8;
	
	private List<HTSCaseRiskGroupDto> c9 = new ArrayList<HTSCaseRiskGroupDto>();
	
	private String c9Note;
	
	private HTSc10 c10;
	
	private String c10Note;
	
	private HTSYesNoNone c11;
	
	private HTSc11a c11a;
	
	private String c11aNote;
	
	private HTSc11b c11b;
	
	private HTSYesNoNone c11c;
	
	private String c11cNote;
	
	private HTSc12 c12;
	
	private HTSc131 c131;
	
	private HTSc132 c132;
	
	private HTSc14 c14;
	
	private HTSYesNoNone c15;
	
	private String c15Note;
	
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime c15Date;
	
	private HTSYesNoNone c1627;
	
	private String c1627Note;
	
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime c1627Date;
	
	private HTSc17 c17;	
	
	private HTSc18 c18;
	
	private String c19;
	
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime c19Date;
	
	private String c19Note;
	
	private HTSc20 c20;
	
	private String c20Reason;
	
	private String c20Org;
	
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime c20RegDate;
	
	private String c20Code;
	
	private HTSYesNoNone c21;
	
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime c21Date;
	
	private HTSYesNoNone c22;
	
	private String c23FullName;
	
	private DictionaryDto c23Ethnic;
	
	private DictionaryDto c23Profession;
	
	private String c23IdNumber;
	
	private String c23HealthNumber;
	
	private String c23PhoneNumber;
	
	private String c23Note;
	
	private AdminUnitDto c23ResidentAddressProvince;
	
	private AdminUnitDto c23ResidentAddressDistrict;
	
	private AdminUnitDto c23ResidentAddressCommune;
	
	private String c23ResidentAddressWard;
	
	private String c23ResidentAddressDetail;
	
	private AdminUnitDto c23CurrentAddressProvince;
	
	private AdminUnitDto c23CurrentAddressDistrict;
	
	private AdminUnitDto c23CurrentAddressCommune;
	
	private String c23CurrentAddressWard;
	
	private String c23CurrentAddressDetail;
	
	private HTSc24 c24;
	
	private HTSYesNoNone c25;
	
	private HTSc26 c26;	
	
	private HTSc28 c28;

	private Integer totalIsTestedHiv;

	private Integer totalHivPositiveResult;

	private String note;

	private Boolean isImportedFromHtsNew;

	private Boolean notComplete;

	private int numberErrorContent;

	private String errorContent = null;

	private Boolean saved;
	
	private boolean isReadAble;
	
	private boolean isWritAble;
	
	private boolean isDeleteAble;

	private boolean canNotComplete;

	private boolean isEditAble;

	public HTSCaseDto() {
		
	}
	
	public HTSCaseDto(HTSCase entity, Boolean isViewPII,
						boolean isReadAble,
						boolean isWritAble,
						boolean isDeleteAble,
						boolean isSimple) {
		
		if (entity == null) {
			return;
		}
		this.isDeleteAble = isDeleteAble;
		this.isWritAble = isWritAble;
		this.isReadAble = isReadAble;
		if(!isReadAble) {
			return;
		}
		this.id = entity.getId();
		this.uid = entity.getUid();
		if(!isSimple && entity.getC2() != null) {
			this.c2 = new OrganizationDto(entity.getC2());
			this.c2.setId(entity.getC2().getId());
			this.c2.setName(entity.getC2().getName());
			this.c2.setCode(entity.getC2().getCode());
			this.c2.setCanNotBeCompleted(entity.getC2().getCanNotBeCompleted());
		}
		if(!isSimple && entity.getC3() != null) {
			this.c3 = new StaffDto();
			this.c3.setId(entity.getC3().getId());
			this.c3.setFullName(entity.getC3().getPerson().getFullname());
		}
		this.c4 = entity.getC4();
		this.c5 = entity.getC5();
		this.c5Note = entity.getC5Note();
		this.c6 = entity.getC6();
		this.c7 = entity.getC7();
		if(entity.getC8()!=null) {
			this.c8 = entity.getC8().getYear();
		}		
		if (!isSimple && entity.getC9() != null) {
			for (HTSCaseRiskGroup htsCaseRiskGroup : entity.getC9()) {
				this.c9.add(new HTSCaseRiskGroupDto(htsCaseRiskGroup));
			}
		}
		this.c9Note= entity.getC9Note();
		this.c10 = entity.getC10();
		this.c10Note = entity.getC10Note();
		this.c11 = entity.getC11();
		this.c11a = entity.getC11a();
		this.c11aNote = entity.getC11aNote();
		this.c11b= entity.getC11b();
		this.c11c = entity.getC11c();
		this.c11cNote = entity.getC11cNote();
		this.c12 = entity.getC12();
		this.c131 = entity.getC131();
		this.c132 = entity.getC132();
		this.c14 = entity.getC14();
		this.c15 = entity.getC15();
		this.c15Note = entity.getC15Note();
		this.c15Date= entity.getC15Date();
		this.c1627 = entity.getC1627();
		this.c1627Note = entity.getC1627Note();
		this.c1627Date = entity.getC1627Date();
		this.c17 = entity.getC17();
		this.c18 = entity.getC18();
		this.c19 = entity.getC19();
		this.c19Date = entity.getC19Date();
		this.c19Note = entity.getC19Note();
		this.c20 = entity.getC20();
		this.c20Reason = entity.getC20Reason();
		this.c20Org = entity.getC20Org();
		this.c20RegDate = entity.getC20RegDate();
		this.c20Code = entity.getC20Code();
		this.c21 = entity.getC21();
		this.c21Date = entity.getC21Date();
		this.c22 = entity.getC22();
		if(isViewPII) {
			this.c23FullName = entity.getC23FullName();
		} else {
			this.c23FullName = "-";
		}
		
		if(!isSimple && entity.getC23Ethnic() != null) {
			this.c23Ethnic = new DictionaryDto();
			this.c23Ethnic.setId(entity.getC23Ethnic().getId());
			this.c23Ethnic.setCode(entity.getC23Ethnic().getCode());
			this.c23Ethnic.setValue(entity.getC23Ethnic().getValue());
			this.c23Ethnic.setValueEn(entity.getC23Ethnic().getValueEn());
		}
		if(!isSimple && entity.getC23Profession() != null) {
			this.c23Profession = new DictionaryDto();
			this.c23Profession.setId(entity.getC23Profession().getId());
			this.c23Profession.setCode(entity.getC23Profession().getCode());
			this.c23Profession.setValue(entity.getC23Profession().getValue());
			this.c23Profession.setValueEn(entity.getC23Profession().getValueEn());
		}

		if(isViewPII) {
			this.c23IdNumber = entity.getC23IdNumber();
			this.c23HealthNumber = entity.getC23HealthNumber();
			this.c23PhoneNumber = entity.getC23PhoneNumber();
		} else {
			this.c23IdNumber = "-";
			this.c23HealthNumber = "-";
			this.c23PhoneNumber = "-";
		}

		this.c23Note = entity.getC23Note();
		
		
		if(!isSimple && entity.getC23ResidentAddressProvince() != null) {
			this.c23ResidentAddressProvince = new AdminUnitDto();
			this.c23ResidentAddressProvince.setId(entity.getC23ResidentAddressProvince().getId());
			this.c23ResidentAddressProvince.setCode(entity.getC23ResidentAddressProvince().getCode());
			this.c23ResidentAddressProvince.setName(entity.getC23ResidentAddressProvince().getName());
			this.c23ResidentAddressProvince.setCodeGso(entity.getC23ResidentAddressProvince().getCodeGso());
		}
		if(!isSimple && entity.getC23ResidentAddressDistrict() != null) {
			this.c23ResidentAddressDistrict = new AdminUnitDto();
			this.c23ResidentAddressDistrict.setId(entity.getC23ResidentAddressDistrict().getId());
			this.c23ResidentAddressDistrict.setName(entity.getC23ResidentAddressDistrict().getName());
			this.c23ResidentAddressDistrict.setCode(entity.getC23ResidentAddressDistrict().getCode());
			this.c23ResidentAddressDistrict.setCodeGso(entity.getC23ResidentAddressDistrict().getCodeGso());
		}
		
		if(isViewPII) {
			if(entity.getC23ResidentAddressCommune() != null) {
				this.c23ResidentAddressCommune = new AdminUnitDto();
				this.c23ResidentAddressCommune.setId(entity.getC23ResidentAddressCommune().getId());
				this.c23ResidentAddressCommune.setName(entity.getC23ResidentAddressCommune().getName());
				this.c23ResidentAddressCommune.setCode(entity.getC23ResidentAddressCommune().getCode());
				this.c23ResidentAddressCommune.setCodeGso(entity.getC23ResidentAddressCommune().getCodeGso());
			}
		}
		else {
			this.c23ResidentAddressCommune = new AdminUnitDto();
			this.c23ResidentAddressCommune.setId(0L);
			this.c23ResidentAddressCommune.setName("-");
			this.c23ResidentAddressCommune.setCode("-");
			this.c23ResidentAddressCommune.setCodeGso("-");
		}
		if(isViewPII) {
			this.c23ResidentAddressWard = entity.getC23ResidentAddressWard();
			this.c23ResidentAddressDetail = entity.getC23ResidentAddressDetail();
		} else {
			this.c23ResidentAddressWard = "-";
			this.c23ResidentAddressDetail = "-";
		}

		if(!isSimple && entity.getC23CurrentAddressProvince() != null) {
			this.c23CurrentAddressProvince = new AdminUnitDto();
			this.c23CurrentAddressProvince.setId(entity.getC23CurrentAddressProvince().getId());
			this.c23CurrentAddressProvince.setName(entity.getC23CurrentAddressProvince().getName());
			this.c23CurrentAddressProvince.setCode(entity.getC23CurrentAddressProvince().getCode());
			this.c23CurrentAddressProvince.setCodeGso(entity.getC23CurrentAddressProvince().getCodeGso());
		}
		if(!isSimple && entity.getC23CurrentAddressDistrict() != null) {
			this.c23CurrentAddressDistrict = new AdminUnitDto();
			this.c23CurrentAddressDistrict.setId(entity.getC23CurrentAddressDistrict().getId());
			this.c23CurrentAddressDistrict.setName(entity.getC23CurrentAddressDistrict().getName());
			this.c23CurrentAddressDistrict.setCode(entity.getC23CurrentAddressDistrict().getCode());
			this.c23CurrentAddressDistrict.setCodeGso(entity.getC23CurrentAddressDistrict().getCodeGso());
		}
		if(!isSimple && entity.getC23CurrentAddressCommune() != null) {
			this.c23CurrentAddressCommune = new AdminUnitDto();
			this.c23CurrentAddressCommune.setId(entity.getC23CurrentAddressCommune().getId());
			this.c23CurrentAddressCommune.setName(entity.getC23CurrentAddressCommune().getName());
			this.c23CurrentAddressCommune.setCode(entity.getC23CurrentAddressCommune().getCode());
			this.c23CurrentAddressCommune.setCodeGso(entity.getC23CurrentAddressCommune().getCodeGso());
		}
		if(isViewPII) {
			this.c23CurrentAddressWard= entity.getC23CurrentAddressWard();
			this.c23CurrentAddressDetail = entity.getC23CurrentAddressDetail();
		} else {
			this.c23CurrentAddressWard = "-";
			this.c23CurrentAddressDetail = "-";
		}
		this.c24 = entity.getC24();
		this.c25 = entity.getC25();
		this.c26 = entity.getC26();
		this.c28 = entity.getC28();
		this.totalIsTestedHiv = entity.getTotalIsTestedHiv();
		this.isImportedFromHtsNew = entity.getImportedFromHtsNew();
		this.totalHivPositiveResult = entity.getTotalHivPositiveResult();
		this.note = entity.getNote();
		if(entity.getNotComplete()!=null) {
			this.notComplete=entity.getNotComplete();
		}
		if(entity.getCanNotComplete()!=null) {
			this.canNotComplete = entity.getCanNotComplete();
		}
	}

	public boolean isEditAble() {return DateTimeUtil.checkEditableByMonth(Constants.NUMBER_OF_MONTH_FOR_EDIT,this.getC4());}

	public void setEditAble(boolean editAble) {isEditAble = editAble;}

	public Boolean getNotComplete() {
		return notComplete;
	}

	public void setNotComplete(Boolean notComplete) {
		this.notComplete = notComplete;
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

	public OrganizationDto getC2() {
		return c2;
	}

	public void setC2(OrganizationDto c2) {
		this.c2 = c2;
	}

	public StaffDto getC3() {
		return c3;
	}

	public void setC3(StaffDto c3) {
		this.c3 = c3;
	}

	public int getNumberErrorContent() {
		return numberErrorContent;
	}

	public void setNumberErrorContent(int numberErrorContent) {
		this.numberErrorContent = numberErrorContent;
	}

	public String getErrorContent() {
		return errorContent;
	}

	public void setErrorContent(String errorContent) {
		this.errorContent = errorContent;
	}

	public LocalDateTime getC4() {
		return c4;
	}

	public void setC4(LocalDateTime c4) {
		this.c4 = c4;
	}

	public HTSc5 getC5() {
		return c5;
	}

	public void setC5(HTSc5 c5) {
		this.c5 = c5;
	}

	public HTSc5Note getC5Note() {
		return c5Note;
	}

	public void setC5Note(HTSc5Note c5Note) {
		this.c5Note = c5Note;
	}

	public String getC6() {
		return c6;
	}

	public void setC6(String c6) {
		this.c6 = c6;
	}

	public Gender getC7() {
		return c7;
	}

	public void setC7(Gender c7) {
		this.c7 = c7;
	}

	public Integer getC8() {
		return c8;
	}

	public void setC8(Integer c8) {
		this.c8 = c8;
	}

	public List<HTSCaseRiskGroupDto> getC9() {
		return c9;
	}

	public void setC9(List<HTSCaseRiskGroupDto> c9) {
		this.c9 = c9;
	}

	public HTSc10 getC10() {
		return c10;
	}

	public void setC10(HTSc10 c10) {
		this.c10 = c10;
	}

	public String getC10Note() {
		return c10Note;
	}

	public void setC10Note(String c10Note) {
		this.c10Note = c10Note;
	}

	public HTSYesNoNone getC11() {
		return c11;
	}

	public void setC11(HTSYesNoNone c11) {
		this.c11 = c11;
	}

	public HTSc11a getC11a() {
		return c11a;
	}

	public void setC11a(HTSc11a c11a) {
		this.c11a = c11a;
	}

	public String getC11aNote() {
		return c11aNote;
	}

	public void setC11aNote(String c11aNote) {
		this.c11aNote = c11aNote;
	}

	public HTSc11b getC11b() {
		return c11b;
	}

	public void setC11b(HTSc11b c11b) {
		this.c11b = c11b;
	}

	public HTSYesNoNone getC11c() {
		return c11c;
	}

	public void setC11c(HTSYesNoNone c11c) {
		this.c11c = c11c;
	}

	public String getC11cNote() {
		return c11cNote;
	}

	public void setC11cNote(String c11cNote) {
		this.c11cNote = c11cNote;
	}

	public HTSc12 getC12() {
		return c12;
	}

	public void setC12(HTSc12 c12) {
		this.c12 = c12;
	}

	public HTSc131 getC131() {
		return c131;
	}

	public void setC131(HTSc131 c131) {
		this.c131 = c131;
	}

	public HTSc132 getC132() {
		return c132;
	}

	public void setC132(HTSc132 c132) {
		this.c132 = c132;
	}

	public HTSc14 getC14() {
		return c14;
	}

	public void setC14(HTSc14 c14) {
		this.c14 = c14;
	}

	public HTSYesNoNone getC15() {
		return c15;
	}

	public void setC15(HTSYesNoNone c15) {
		this.c15 = c15;
	}

	public String getC15Note() {
		return c15Note;
	}

	public void setC15Note(String c15Note) {
		this.c15Note = c15Note;
	}

	public HTSYesNoNone getC1627() {
		return c1627;
	}

	public void setC1627(HTSYesNoNone c1627) {
		this.c1627 = c1627;
	}

	public String getC1627Note() {
		return c1627Note;
	}

	public void setC1627Note(String c1627Note) {
		this.c1627Note = c1627Note;
	}

	public LocalDateTime getC1627Date() {
		return c1627Date;
	}

	public void setC1627Date(LocalDateTime c1627Date) {
		this.c1627Date = c1627Date;
	}

	public HTSc17 getC17() {
		return c17;
	}

	public void setC17(HTSc17 c17) {
		this.c17 = c17;
	}

	public HTSc18 getC18() {
		return c18;
	}

	public void setC18(HTSc18 c18) {
		this.c18 = c18;
	}

	public String getC19() {
		return c19;
	}

	public void setC19(String c19) {
		this.c19 = c19;
	}

	public LocalDateTime getC19Date() {
		return c19Date;
	}

	public void setC19Date(LocalDateTime c19Date) {
		this.c19Date = c19Date;
	}

	public String getC19Note() {
		return c19Note;
	}

	public void setC19Note(String c19Note) {
		this.c19Note = c19Note;
	}

	public HTSc20 getC20() {
		return c20;
	}

	public void setC20(HTSc20 c20) {
		this.c20 = c20;
	}

	public String getC20Reason() {
		return c20Reason;
	}

	public void setC20Reason(String c20Reason) {
		this.c20Reason = c20Reason;
	}

	public String getC20Org() {
		return c20Org;
	}

	public void setC20Org(String c20Org) {
		this.c20Org = c20Org;
	}

	public LocalDateTime getC20RegDate() {
		return c20RegDate;
	}

	public void setC20RegDate(LocalDateTime c20RegDate) {
		this.c20RegDate = c20RegDate;
	}

	public String getC20Code() {
		return c20Code;
	}

	public void setC20Code(String c20Code) {
		this.c20Code = c20Code;
	}

	public HTSYesNoNone getC21() {
		return c21;
	}

	public void setC21(HTSYesNoNone c21) {
		this.c21 = c21;
	}

	public LocalDateTime getC21Date() {
		return c21Date;
	}

	public void setC21Date(LocalDateTime c21Date) {
		this.c21Date = c21Date;
	}

	public HTSYesNoNone getC22() {
		return c22;
	}

	public void setC22(HTSYesNoNone c22) {
		this.c22 = c22;
	}

	public String getC23FullName() {
		return c23FullName;
	}

	public void setC23FullName(String c23FullName) {
		this.c23FullName = c23FullName;
	}

	public DictionaryDto getC23Ethnic() {
		return c23Ethnic;
	}

	public void setC23Ethnic(DictionaryDto c23Ethnic) {
		this.c23Ethnic = c23Ethnic;
	}

	public DictionaryDto getC23Profession() {
		return c23Profession;
	}

	public void setC23Profession(DictionaryDto c23Profession) {
		this.c23Profession = c23Profession;
	}

	public String getC23IdNumber() {
		return c23IdNumber;
	}

	public void setC23IdNumber(String c23IdNumber) {
		this.c23IdNumber = c23IdNumber;
	}

	public String getC23PhoneNumber() {
		return c23PhoneNumber;
	}

	public void setC23PhoneNumber(String c23PhoneNumber) {
		this.c23PhoneNumber = c23PhoneNumber;
	}

	public String getC23Note() {
		return c23Note;
	}

	public void setC23Note(String c23Note) {
		this.c23Note = c23Note;
	}

	public AdminUnitDto getC23ResidentAddressProvince() {
		return c23ResidentAddressProvince;
	}

	public void setC23ResidentAddressProvince(AdminUnitDto c23ResidentAddressProvince) {
		this.c23ResidentAddressProvince = c23ResidentAddressProvince;
	}

	public AdminUnitDto getC23ResidentAddressDistrict() {
		return c23ResidentAddressDistrict;
	}

	public void setC23ResidentAddressDistrict(AdminUnitDto c23ResidentAddressDistrict) {
		this.c23ResidentAddressDistrict = c23ResidentAddressDistrict;
	}

	public AdminUnitDto getC23ResidentAddressCommune() {
		return c23ResidentAddressCommune;
	}

	public void setC23ResidentAddressCommune(AdminUnitDto c23ResidentAddressCommune) {
		this.c23ResidentAddressCommune = c23ResidentAddressCommune;
	}

	public String getC23ResidentAddressDetail() {
		return c23ResidentAddressDetail;
	}

	public void setC23ResidentAddressDetail(String c23ResidentAddressDetail) {
		this.c23ResidentAddressDetail = c23ResidentAddressDetail;
	}

	public AdminUnitDto getC23CurrentAddressProvince() {
		return c23CurrentAddressProvince;
	}

	public void setC23CurrentAddressProvince(AdminUnitDto c23CurrentAddressProvince) {
		this.c23CurrentAddressProvince = c23CurrentAddressProvince;
	}

	public AdminUnitDto getC23CurrentAddressDistrict() {
		return c23CurrentAddressDistrict;
	}

	public void setC23CurrentAddressDistrict(AdminUnitDto c23CurrentAddressDistrict) {
		this.c23CurrentAddressDistrict = c23CurrentAddressDistrict;
	}

	public AdminUnitDto getC23CurrentAddressCommune() {
		return c23CurrentAddressCommune;
	}

	public void setC23CurrentAddressCommune(AdminUnitDto c23CurrentAddressCommune) {
		this.c23CurrentAddressCommune = c23CurrentAddressCommune;
	}

	public String getC23CurrentAddressDetail() {
		return c23CurrentAddressDetail;
	}

	public void setC23CurrentAddressDetail(String c23CurrentAddressDetail) {
		this.c23CurrentAddressDetail = c23CurrentAddressDetail;
	}

	public HTSc24 getC24() {
		return c24;
	}

	public void setC24(HTSc24 c24) {
		this.c24 = c24;
	}

	public HTSYesNoNone getC25() {
		return c25;
	}

	public void setC25(HTSYesNoNone c25) {
		this.c25 = c25;
	}

	public HTSc26 getC26() {
		return c26;
	}

	public void setC26(HTSc26 c26) {
		this.c26 = c26;
	}

	public String getC23ResidentAddressWard() {
		return c23ResidentAddressWard;
	}

	public void setC23ResidentAddressWard(String c23ResidentAddressWard) {
		this.c23ResidentAddressWard = c23ResidentAddressWard;
	}

	public String getC23CurrentAddressWard() {
		return c23CurrentAddressWard;
	}

	public void setC23CurrentAddressWard(String c23CurrentAddressWard) {
		this.c23CurrentAddressWard = c23CurrentAddressWard;
	}

	public String getC9Note() {
		return c9Note;
	}

	public void setC9Note(String c9Note) {
		this.c9Note = c9Note;
	}

	public LocalDateTime getC15Date() {
		return c15Date;
	}

	public void setC15Date(LocalDateTime c15Date) {
		this.c15Date = c15Date;
	}

	public String getC23HealthNumber() {
		return c23HealthNumber;
	}

	public void setC23HealthNumber(String c23HealthNumber) {
		this.c23HealthNumber = c23HealthNumber;
	}

	public HTSc28 getC28() {
		return c28;
	}

	public void setC28(HTSc28 c28) {
		this.c28 = c28;
	}

	public Integer getTotalIsTestedHiv() {
		return totalIsTestedHiv;
	}

	public void setTotalIsTestedHiv(Integer totalIsTestedHiv) {
		this.totalIsTestedHiv = totalIsTestedHiv;
	}

	public Integer getTotalHivPositiveResult() {
		return totalHivPositiveResult;
	}

	public void setTotalHivPositiveResult(Integer totalHivPositiveResult) {
		this.totalHivPositiveResult = totalHivPositiveResult;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public Boolean getImportedFromHtsNew() {
		return isImportedFromHtsNew;
	}

	public void setImportedFromHtsNew(Boolean importedFromHtsNew) {
		isImportedFromHtsNew = importedFromHtsNew;
	}

	public Boolean getSaved() {
		return saved;
	}

	public void setSaved(Boolean saved) {
		this.saved = saved;
	}

	public boolean isReadAble() {
		return isReadAble;
	}

	public void setReadAble(boolean isReadAble) {
		this.isReadAble = isReadAble;
	}

	public boolean isWritAble() {
		return isWritAble;
	}

	public void setWritAble(boolean isWritAble) {
		this.isWritAble = isWritAble;
	}

	public boolean isDeleteAble() {
		return isDeleteAble;
	}

	public void setDeleteAble(boolean isDeleteAble) {
		this.isDeleteAble = isDeleteAble;
	}

	public boolean getCanNotComplete() {
		return canNotComplete;
	}

	public void setCanNotComplete(boolean canNotComplete) {
		this.canNotComplete = canNotComplete;
	}
}
