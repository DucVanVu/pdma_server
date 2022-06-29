package org.pepfar.pdma.app.data.dto;

import java.time.LocalDateTime;
import java.util.*;

import org.pepfar.pdma.app.data.domain.PNSCase;
import org.pepfar.pdma.app.data.domain.PNSCaseContact;
import org.pepfar.pdma.app.data.domain.PNSCaseRiskGroup;
import org.pepfar.pdma.app.data.types.Gender;
import org.pepfar.pdma.app.data.types.HTSYesNoNone;
import org.pepfar.pdma.app.data.types.PNSc11;
import org.pepfar.pdma.app.utils.CustomLocalDateTimeDeserializer2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class PNSCaseDto {
	private Long id;

	private UUID uid;

	private OrganizationDto c2;
	
//	C3. Họ tên tư vấn viên:
	private StaffDto c3;

	private HTSCaseDto htsCase;
	
	private CaseDto hivCase;
	
//	C4. Mã số khách hàng:
	private String c4;
	
	//C5. Ngày tư vấn dịch vụ TBXNBT/BC
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime c5;
		
	//C6. Đồng ý nhận dịch vụ TBXNBT/BC?
	private HTSYesNoNone c6;
	
//	- Ngày đồng ý nhận dịch vụ:
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime c6Date;
	
	//C7. Họ tên người có HIV:
	private String c7;
	
	//C8. Giới tính khi sinh:
	private Gender c8;
	
//	C9. Năm sinh:
	private Integer c9;
	
//	C10. Ngày XN khẳng định HIV+:
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime c10;
	
//	C11. Tình trạng ĐT ARV?
	private PNSc11 c11;
	
	private String c11Des;
	
	//C12. Khách hàng thuộc nhóm nguy cơ nào?
	private List<PNSCaseRiskGroupDto> c12 = new ArrayList<>();
	
	//Phần ghi rõ của 12
	private String c12Note;
	
	private List<PNSCaseContactDto> contacts = new ArrayList<>();

	private int numberErrorContent;

	private String errorContent = null;

	private Boolean saved;

	private boolean isReadAble;

	private boolean isWritAble;

	private boolean isDeleteAble;
	
	public PNSCaseDto() {
		
	}
	
	public PNSCaseDto(PNSCase pnsCase, int isViewPII,
					  boolean isReadAble,
					  boolean isWritAble,
					  boolean isDeleteAble) {
		super();
		this.isDeleteAble = isDeleteAble;
		this.isWritAble = isWritAble;
		this.isReadAble = isReadAble;
		if(!isReadAble) {
			return;
		}
		if(pnsCase!=null) {
			this.id = pnsCase.getId();
			this.uid = pnsCase.getUid();
			if(pnsCase.getC2()!=null) {
				this.c2 = new OrganizationDto(pnsCase.getC2());
				this.c2.setId(pnsCase.getC2().getId());
				this.c2.setName(pnsCase.getC2().getName());
				this.c2.setCode(pnsCase.getC2().getCode());
			}

			if(pnsCase.getC3()!=null) {
				this.c3 = new StaffDto();
				this.c3.setId(pnsCase.getC3().getId());
				this.c3.setFullName(pnsCase.getC3().getPerson().getFullname());
			}
			if(pnsCase.getHtsCase()!=null) {
				this.htsCase = new HTSCaseDto();
				this.htsCase.setId(pnsCase.getHtsCase().getId());				
			}
			if(pnsCase.getHivCase()!=null) {
				this.hivCase = new CaseDto();
				this.hivCase.setId(pnsCase.getHivCase().getId());				
			}
			this.c4 = pnsCase.getC4();
			this.c5 = pnsCase.getC5();
			this.c6 = pnsCase.getC6();
			this.c6Date = pnsCase.getC6Date();
			if(isViewPII==1) {
				this.c7 = pnsCase.getC7();
			} else {
				this.c7 = "-";
			}
			this.c8 = pnsCase.getC8();
			if(pnsCase.getC9()!=null) {
				this.c9 = pnsCase.getC9().getYear();
			}			
			this.c10 = pnsCase.getC10();
			this.c11 = pnsCase.getC11();
			if(pnsCase.getC12()!=null && pnsCase.getC12().size()>0) {
				for (PNSCaseRiskGroup pnsCaseRiskGroup : pnsCase.getC12()) {
					PNSCaseRiskGroupDto pnsCaseRiskGroupDto = new PNSCaseRiskGroupDto(pnsCaseRiskGroup);
					this.c12.add(pnsCaseRiskGroupDto);
				}
			}
			this.c12Note = pnsCase.getC12Note();
			if(pnsCase.getContacts()!=null && pnsCase.getContacts().size()>0) {
				for (PNSCaseContact pnsCaseContact : pnsCase.getContacts()) {
					if(isViewPII==1) {
						this.contacts.add(new PNSCaseContactDto(pnsCaseContact, true,isReadAble,isWritAble,isDeleteAble));
					} else {
						this.contacts.add(new PNSCaseContactDto(pnsCaseContact, false,isReadAble,isWritAble,isDeleteAble));
					}
				}
			}
		}
	}
	public PNSCaseDto(PNSCase pnsCase,boolean simple) {
		super();
		if(pnsCase!=null) {
			this.id = pnsCase.getId();
			this.uid = pnsCase.getUid();
			if(pnsCase.getC2()!=null) {
				this.c2 = new OrganizationDto();
				this.c2.setId(pnsCase.getC2().getId());
				this.c2.setName(pnsCase.getC2().getName());
				this.c2.setCode(pnsCase.getC2().getCode());
			}
			if(pnsCase.getC3()!=null) {
				this.c3 = new StaffDto();
				this.c3.setId(pnsCase.getC3().getId());
				this.c3.setFullName(pnsCase.getC3().getPerson().getFullname());
			}
			if(pnsCase.getHtsCase()!=null) {
				this.htsCase = new HTSCaseDto();
				this.htsCase.setId(pnsCase.getHtsCase().getId());				
			}
			if(pnsCase.getHivCase()!=null) {
				this.hivCase = new CaseDto();
				this.hivCase.setId(pnsCase.getHivCase().getId());
			}
			this.c4 = pnsCase.getC4();
			this.c5 = pnsCase.getC5();
			this.c6 = pnsCase.getC6();
			this.c6Date = pnsCase.getC6Date();
			this.c7 = pnsCase.getC7();
			this.c8 = pnsCase.getC8();
			if(pnsCase.getC9()!=null) {
				this.c9 = pnsCase.getC9().getYear();
			}
			this.c10 = pnsCase.getC10();
			this.c11 = pnsCase.getC11();
			
			this.c12Note = pnsCase.getC12Note();
			if(!simple) {
				if(pnsCase.getC12()!=null && pnsCase.getC12().size()>0) {
					for (PNSCaseRiskGroup pnsCaseRiskGroup : pnsCase.getC12()) {
						PNSCaseRiskGroupDto pnsCaseRiskGroupDto = new PNSCaseRiskGroupDto(pnsCaseRiskGroup);
						this.c12.add(pnsCaseRiskGroupDto);
					}
				}
			}
		}
	}

	public PNSCaseDto(Long id, UUID uuid, OrganizationDto c2, StaffDto c3, HTSCaseDto htsCase, CaseDto hivCase, String c4,
			LocalDateTime c5, HTSYesNoNone c6, LocalDateTime c6Date, String c7, Gender c8, Integer c9,
			LocalDateTime c10, PNSc11 c11, List<PNSCaseRiskGroupDto> c12, String c12Note) {
		super();
		this.id = id;
		this.uid = uuid;
		this.c2 = c2;
		this.c3 = c3;
		this.htsCase = htsCase;
		this.hivCase = hivCase;
		this.c4 = c4;
		this.c5 = c5;
		this.c6 = c6;
		this.c6Date = c6Date;
		this.c7 = c7;
		this.c8 = c8;
		this.c9 = c9;
		this.c10 = c10;
		this.c11 = c11;
		this.c12 = c12;
		this.c12Note = c12Note;
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

	public HTSCaseDto getHtsCase() {
		return htsCase;
	}

	public void setHtsCase(HTSCaseDto htsCase) {
		this.htsCase = htsCase;
	}

	public CaseDto getHivCase() {
		return hivCase;
	}

	public void setHivCase(CaseDto hivCase) {
		this.hivCase = hivCase;
	}

	public String getC4() {
		return c4;
	}

	public void setC4(String c4) {
		this.c4 = c4;
	}

	public LocalDateTime getC5() {
		return c5;
	}

	public void setC5(LocalDateTime c5) {
		this.c5 = c5;
	}

	public HTSYesNoNone getC6() {
		return c6;
	}

	public void setC6(HTSYesNoNone c6) {
		this.c6 = c6;
	}

	public LocalDateTime getC6Date() {
		return c6Date;
	}

	public void setC6Date(LocalDateTime c6Date) {
		this.c6Date = c6Date;
	}

	public String getC7() {
		return c7;
	}

	public void setC7(String c7) {
		this.c7 = c7;
	}

	public Gender getC8() {
		return c8;
	}

	public void setC8(Gender c8) {
		this.c8 = c8;
	}

	public Integer getC9() {
		return c9;
	}

	public void setC9(Integer c9) {
		this.c9 = c9;
	}

	public LocalDateTime getC10() {
		return c10;
	}

	public void setC10(LocalDateTime c10) {
		this.c10 = c10;
	}

	public PNSc11 getC11() {
		return c11;
	}

	public void setC11(PNSc11 c11) {
		this.c11 = c11;
	}

	public String getC11Des() {
		if(this.c11!=null) {
			c11Des = this.c11.getDescription();
		}
		return c11Des;
	}

	public void setC11Des(String c11Des) {
		this.c11Des = c11Des;
	}

	public List<PNSCaseRiskGroupDto> getC12() {
		return c12;
	}

	public void setC12(List<PNSCaseRiskGroupDto> c12) {
		this.c12 = c12;
	}

	public String getC12Note() {
		return c12Note;
	}

	public void setC12Note(String c12Note) {
		this.c12Note = c12Note;
	}

	public List<PNSCaseContactDto> getContacts() {
		return contacts;
	}

	public void setContacts(List<PNSCaseContactDto> contacts) {
		this.contacts = contacts;
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
}
