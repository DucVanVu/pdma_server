package org.pepfar.pdma.app.data.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.pepfar.pdma.app.data.domain.HTSCase;
import org.pepfar.pdma.app.data.domain.PNSCaseContact;
import org.pepfar.pdma.app.data.domain.PNSCaseContactRelationship;
import org.pepfar.pdma.app.data.types.Gender;
import org.pepfar.pdma.app.data.types.HTSYesNoNone;
import org.pepfar.pdma.app.data.types.PNSHivStatus;
import org.pepfar.pdma.app.data.types.PNSSucceededContactMethod;
import org.pepfar.pdma.app.data.types.PNSSucceededMethod;
import org.pepfar.pdma.app.data.types.PNSc5Reason;
import org.pepfar.pdma.app.data.types.PNSc8;
import org.pepfar.pdma.app.data.types.PNSc9;
import org.pepfar.pdma.app.utils.CustomLocalDateTimeDeserializer2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class PNSCaseContactDto {
	private Long id;

	private PNSCaseDto pnsCase;
	/*
	 * Họ tên BT/BC/con đẻ phơi nhiễm:
	 * Năm sinh:
	 * Giới tính khi sinh:
	 * - Số điện thoại:
	 * Lấy từ Person
	 */	
	private PersonDto person;
	private String fullName;
	private Integer yearOfBirth;
	private String phoneNumber;
	private Gender gender;
//	Nơi cư trú: - Tỉnh/TP 
	private AdminUnitDto province;
//	- Quận/huyện: 
	private AdminUnitDto district;
	
//	- Địa chỉ cụ thể:
	private String addressDetail;		
	
//	C1. Quan hệ với người có HIV?
	private List<PNSCaseContactRelationshipDto> c1;
	
//	Ngày khai thác được thông tin
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime c1receivedInfoDate;
	
//	C2. Tình trạng HIV 
	private PNSHivStatus c2;
	private String c2Des;
	
//	C3. Nguy cơ bạo lực:
	private HTSYesNoNone c3;
	private String c3Des;
	
//	C4. Đã liên lạc thông báo - Lần 1:
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime c4First;
//	 - Lần 2:
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime c4Second;
//	 - Lần 3:
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime c4Third;
	
//	C5. Đã liên lạc được?
	private HTSYesNoNone c5;
	private String c5Des;
	
//	- Ghi rõ lý do
	private PNSc5Reason c5Reason;
	private String c5ReasonDes;
	
//	C6. Biện pháp đã thành công 
	private PNSSucceededMethod c6;
	private String c6Des;
	
//	C7. Cách đã liên lạc thành công 
	private PNSSucceededContactMethod c7;
	private String c7Des;
//	- Ghi rõ cách liên lạc khác:
	private String c7Note;
	
//	C8. Xét nghiệm HIV
	private PNSc8 c8;
	private String c8Des;
	
//	- Mã số KH TVXN HIV:
	private String c8LabtestCode;
	
	private HTSCaseDto c8HTSCase;
	
//	- Ngày XN HIV:
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime c8LabtestDate;
	
//	- Tên cơ sở TVXN HIV:
	private String c8LabtestOrg;
	
//	C9. Kết quả XN HIV 
	private PNSc9 c9;
	private String c9Des;
	
	private HTSYesNoNone c9JoinedPrEP;
	private String c9JoinedPrEPDes;
	
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime c9PrEPDate;
	
	private HTSYesNoNone c9JoinedARV;
	private String c9JoinedARVDes;
	
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime c9ARVDate;

	private int numberErrorContent;
	private Boolean saved;
	private String errorContent = null;
	
	private boolean isReadAble;

	private boolean isWritAble;

	private boolean isDeleteAble;
	
	public PNSCaseContactDto() {
		super();
	}

	public PNSCaseContactDto(PNSCaseContact entity, Boolean isViewPII, boolean isReadAble,
			boolean isWritAble,
			boolean isDeleteAble) {
		super();
		if(entity!=null) {
			this.isDeleteAble = isDeleteAble;
			this.isWritAble = isWritAble;
			this.isReadAble = isReadAble;
			if(!isReadAble) {
				return;
			}
			this.id = entity.getId();
			if(entity.getPnsCase()!=null) {
				this.pnsCase = new PNSCaseDto();
				this.pnsCase.setId(entity.getPnsCase().getId());
				this.pnsCase.setC4(entity.getPnsCase().getC4());
				this.pnsCase.setC7(entity.getPnsCase().getC7());
				if(entity.getPnsCase().getHtsCase()!=null)
				this.pnsCase.setHtsCase(new HTSCaseDto(entity.getPnsCase().getHtsCase(),isViewPII,true,false,false,true));
			}
			if(entity.getPerson()!=null) {
				this.person = new PersonDto();
				this.person.setId(entity.getPerson().getId());
				this.person.setFullname(entity.getPerson().getFullname());
				if(isViewPII) {
					this.fullName = entity.getPerson().getFullname();
				} else {
					this.fullName = "-";
				}
				this.person.setDob(entity.getPerson().getDob());
				this.yearOfBirth = entity.getPerson().getDob().getYear();
				this.person.setMobilePhone(entity.getPerson().getMobilePhone());
				if(isViewPII) {
					this.phoneNumber = entity.getPerson().getMobilePhone();
				} else {
					this.phoneNumber = "-";
				}
				this.person.setGender(entity.getPerson().getGender());
				this.gender = entity.getPerson().getGender();
			}
			this.province = new AdminUnitDto(entity.getProvince(),true);			
			this.district = new AdminUnitDto(entity.getDistrict(),true);
			if(isViewPII) {
				this.addressDetail = entity.getAddressDetail();
			} else {
				this.addressDetail = "-";
			}
			if(entity.getC1()!=null && entity.getC1().size()>0) {
				this.c1 = new ArrayList<PNSCaseContactRelationshipDto>();
				for (PNSCaseContactRelationship pnsCaseContactRelationship : entity.getC1()) {
					this.c1.add(new PNSCaseContactRelationshipDto(pnsCaseContactRelationship,true));
				}
			}
			
			this.c1receivedInfoDate = entity.getC1receivedInfoDate();
			this.c2 = entity.getC2();
			this.c3 = entity.getC3();
			this.c4First =  entity.getC4First();
			this.c4Second = entity.getC4Second();
			this.c4Third = entity.getC4Third();
			this.c5 = entity.getC5();
			this.c5Reason = entity.getC5Reason();
			this.c6 = entity.getC6();
			this.c7 = entity.getC7();
			this.c7Note = entity.getC7Note();
			this.c8 = entity.getC8();			
			this.c8LabtestCode = entity.getC8LabtestCode();
			this.c8LabtestDate = entity.getC8LabtestDate();
			this.c8LabtestOrg = entity.getC8LabtestOrg();
			this.c9 = entity.getC9();
			this.c9JoinedPrEP = entity.getC9JoinedPrEP();
			this.c9PrEPDate = entity.getC9PrEPDate();
			this.c9JoinedARV = entity.getC9JoinedARV();
			this.c9ARVDate = entity.getC9ARVDate();
			if(entity.getC8HTSCase()!=null) {
				this.c8HTSCase= new HTSCaseDto(entity.getC8HTSCase(), isViewPII,true,false,false,true);
			}
		}
	}
		
	public PNSCaseContactDto(Long id, PNSCaseDto pnsCase, PersonDto person, AdminUnitDto province, AdminUnitDto district,
			String addressDetail, List<PNSCaseContactRelationshipDto> relationships, LocalDateTime receivedInfoDate,
			PNSHivStatus c2, String c2Des, HTSYesNoNone c3, String c3Des, LocalDateTime c4First, LocalDateTime c4Second,
			LocalDateTime c4Third, HTSYesNoNone c5, String c5Des, PNSc5Reason c5Reason, String c5ReasonDes, PNSSucceededMethod c6, String c6Des,
			PNSSucceededContactMethod c7, String c7Des, String c7Note, PNSc8 c8, String c8Des,HTSCaseDto c8HTSCase, String c8LabtestCode,
			LocalDateTime c8LabtestDate, String c8LabtestOrg, PNSc9 c9, String c9Des, HTSYesNoNone c9JoinedPrEP, String c9JoinedPrEPDes,
			LocalDateTime c9PrEPDate, HTSYesNoNone c9JoinedARV,String c9JoinedARVDes, LocalDateTime c9arvDate) {
		super();
		this.id = id;
		this.pnsCase = pnsCase;
		this.person = person;
		this.province = province;
		this.district = district;
		this.addressDetail = addressDetail;
		this.c1 = relationships;
		this.c1receivedInfoDate = receivedInfoDate;
		this.c2 = c2;
		this.c2Des = c2Des;
		this.c3 = c3;
		this.c3Des = c3Des;
		this.c4First = c4First;
		this.c4Second = c4Second;
		this.c4Third = c4Third;
		this.c5 = c5;
		this.c5Des = c5Des;
		this.c5Reason = c5Reason;
		this.c6 = c6;
		this.c6Des = c6Des;
		this.c7 = c7;
		this.c7Des = c7Des;
		this.c7Note = c7Note;
		this.c8 = c8;
		this.c8Des = c8Des;
		this.c8LabtestCode = c8LabtestCode;
		this.c8LabtestDate = c8LabtestDate;
		this.c8LabtestOrg = c8LabtestOrg;
		this.c9 = c9;
		this.c9Des = c9Des;
		this.c9JoinedPrEP = c9JoinedPrEP;
		this.c9PrEPDate = c9PrEPDate;
		this.c9JoinedARV = c9JoinedARV;
		this.c9ARVDate = c9arvDate;
		this.c5ReasonDes = c5ReasonDes;
		this.c9JoinedARVDes = c9JoinedARVDes;
		this.c9JoinedPrEPDes = c9JoinedPrEPDes;
		this.c8HTSCase= c8HTSCase;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getSaved() {
		return saved;
	}

	public void setSaved(Boolean saved) {
		this.saved = saved;
	}

	public PNSCaseDto getPnsCase() {
		return pnsCase;
	}

	public void setPnsCase(PNSCaseDto pnsCase) {
		this.pnsCase = pnsCase;
	}

	public PersonDto getPerson() {
		return person;
	}

	public void setPerson(PersonDto person) {
		this.person = person;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public Integer getYearOfBirth() {
		return yearOfBirth;
	}

	public void setYearOfBirth(Integer yearOfBirth) {
		this.yearOfBirth = yearOfBirth;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public AdminUnitDto getProvince() {
		return province;
	}

	public void setProvince(AdminUnitDto province) {
		this.province = province;
	}

	public AdminUnitDto getDistrict() {
		return district;
	}

	public void setDistrict(AdminUnitDto district) {
		this.district = district;
	}

	public String getAddressDetail() {
		return addressDetail;
	}

	public void setAddressDetail(String addressDetail) {
		this.addressDetail = addressDetail;
	}

	public List<PNSCaseContactRelationshipDto> getC1() {
		return c1;
	}

	public void setC1(List<PNSCaseContactRelationshipDto> c1Relationships) {
		this.c1 = c1Relationships;
	}

	public LocalDateTime getC1receivedInfoDate() {
		return c1receivedInfoDate;
	}

	public void setC1receivedInfoDate(LocalDateTime c1receivedInfoDate) {
		this.c1receivedInfoDate = c1receivedInfoDate;
	}

	public PNSHivStatus getC2() {
		return c2;
	}

	public void setC2(PNSHivStatus c2) {
		this.c2 = c2;
	}

	public HTSYesNoNone getC3() {
		return c3;
	}

	public void setC3(HTSYesNoNone c3) {
		this.c3 = c3;
	}

	public LocalDateTime getC4First() {
		return c4First;
	}

	public void setC4First(LocalDateTime c4First) {
		this.c4First = c4First;
	}

	public LocalDateTime getC4Second() {
		return c4Second;
	}

	public void setC4Second(LocalDateTime c4Second) {
		this.c4Second = c4Second;
	}

	public LocalDateTime getC4Third() {
		return c4Third;
	}

	public void setC4Third(LocalDateTime c4Third) {
		this.c4Third = c4Third;
	}

	public HTSYesNoNone getC5() {
		return c5;
	}

	public void setC5(HTSYesNoNone c5) {
		this.c5 = c5;
	}

	public PNSc5Reason getC5Reason() {
		return c5Reason;
	}

	public void setC5Reason(PNSc5Reason c5Reason) {
		this.c5Reason = c5Reason;
	}

	public PNSSucceededMethod getC6() {
		return c6;
	}

	public void setC6(PNSSucceededMethod c6) {
		this.c6 = c6;
	}

	public PNSSucceededContactMethod getC7() {
		return c7;
	}

	public void setC7(PNSSucceededContactMethod c7) {
		this.c7 = c7;
	}

	public String getC7Note() {
		return c7Note;
	}

	public void setC7Note(String c7Note) {
		this.c7Note = c7Note;
	}

	public PNSc8 getC8() {
		return c8;
	}

	public void setC8(PNSc8 c8) {
		this.c8 = c8;
	}

	public String getC8LabtestCode() {
		return c8LabtestCode;
	}

	public void setC8LabtestCode(String c8LabtestCode) {
		this.c8LabtestCode = c8LabtestCode;
	}

	public LocalDateTime getC8LabtestDate() {
		return c8LabtestDate;
	}

	public void setC8LabtestDate(LocalDateTime c8LabtestDate) {
		this.c8LabtestDate = c8LabtestDate;
	}

	public String getC8LabtestOrg() {
		return c8LabtestOrg;
	}

	public void setC8LabtestOrg(String c8LabtestOrg) {
		this.c8LabtestOrg = c8LabtestOrg;
	}

	public PNSc9 getC9() {
		return c9;
	}

	public void setC9(PNSc9 c9) {
		this.c9 = c9;
	}

	public HTSYesNoNone getC9JoinedPrEP() {
		return c9JoinedPrEP;
	}

	public void setC9JoinedPrEP(HTSYesNoNone c9JoinedPrEP) {
		this.c9JoinedPrEP = c9JoinedPrEP;
	}

	public LocalDateTime getC9PrEPDate() {
		return c9PrEPDate;
	}

	public void setC9PrEPDate(LocalDateTime c9PrEPDate) {
		this.c9PrEPDate = c9PrEPDate;
	}

	public HTSYesNoNone getC9JoinedARV() {
		return c9JoinedARV;
	}

	public void setC9JoinedARV(HTSYesNoNone c9JoinedARV) {
		this.c9JoinedARV = c9JoinedARV;
	}

	public LocalDateTime getC9ARVDate() {
		return c9ARVDate;
	}

	public void setC9ARVDate(LocalDateTime c9arvDate) {
		c9ARVDate = c9arvDate;
	}

	public HTSCaseDto getC8HTSCase() {
		return c8HTSCase;
	}

	public void setC8HTSCase(HTSCaseDto c8htsCase) {
		c8HTSCase = c8htsCase;
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

	public String getC2Des() {
		if(c2!=null) {
			c2Des = c2.getDescription();
		}
		return c2Des;
	}

	public String getC3Des() {
		if(c3!=null) {
			if(c3==HTSYesNoNone.NO) {
				c3Des="Không";
			}
			else if(c3==HTSYesNoNone.YES) {
				c3Des="Có";
			}
			else if(c3==HTSYesNoNone.NO_INFORMATION) {
				c3Des="Không có thông tin";
			}
		}
		return c3Des;
	}

	public String getC5Des() {
		if(c5!=null) {
			if(c5==HTSYesNoNone.NO) {
				c5Des="Không";
			}
			else if(c5==HTSYesNoNone.YES) {
				c5Des="Có";
			}
			else if(c5==HTSYesNoNone.NO_INFORMATION) {
				c5Des="Không có thông tin";
			}
		}
		return c5Des;
	}

	public String getC6Des() {
		if(c6!=null) {
			c6Des = c6.getDescription();
		}
		return c6Des;
	}
	
	public String getC7Des() {
		if(c7!=null) {
			c7Des = c7.getDescription();
		}
		return c7Des;
	}

	public String getC8Des() {
		if(c8!=null) {
			c8Des = c8.getDescription();
		}
		return c8Des;
	}

	public String getC9Des() {
		if(c9!=null) {
			c9Des = c9.getDescription();
		}
		return c9Des;
	}
	
	public String getC5ReasonDes() {
		if(c5Reason!=null) {
			c5ReasonDes = c5Reason.getDescription();
		}
		return c5ReasonDes;
	}
	
	public String getC9JoinedPrEPDes() {
		if(c3!=null) {
			if(c9JoinedPrEP==HTSYesNoNone.NO) {
				c9JoinedPrEPDes="Không";
			}
			else if(c9JoinedPrEP==HTSYesNoNone.YES) {
				c9JoinedPrEPDes="Có";
			}
			else if(c9JoinedPrEP==HTSYesNoNone.NO_INFORMATION) {
				c9JoinedPrEPDes="Không có thông tin";
			}
		}
		return c9JoinedPrEPDes;
	}
	
	public String getC9JoinedARVDes() {
		if(c3!=null) {
			if(c9JoinedARV==HTSYesNoNone.NO) {
				c9JoinedARVDes="Không";
			}
			else if(c9JoinedARV==HTSYesNoNone.YES) {
				c9JoinedARVDes="Có";
			}
			else if(c9JoinedARV==HTSYesNoNone.NO_INFORMATION) {
				c9JoinedARVDes="Không có thông tin";
			}
		}
		return c9JoinedARVDes;
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
	
	
}
