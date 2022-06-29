package org.pepfar.pdma.app.data.dto;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import org.pepfar.pdma.app.Constants;
import org.pepfar.pdma.app.data.domain.PECase;
import org.pepfar.pdma.app.data.domain.PECaseRiskGroup;
import org.pepfar.pdma.app.data.types.Gender;
import org.pepfar.pdma.app.data.types.HTSYesNoNone;
import org.pepfar.pdma.app.data.types.PEC12;
import org.pepfar.pdma.app.data.types.PEC12BTBC;
import org.pepfar.pdma.app.data.types.PEC131Result;
import org.pepfar.pdma.app.data.types.PEC132;
import org.pepfar.pdma.app.data.types.PEC16;
import org.pepfar.pdma.app.data.types.PEC8;
import org.pepfar.pdma.app.data.types.PEC8ARV;
import org.pepfar.pdma.app.data.types.PEApproachMethod;
import org.pepfar.pdma.app.data.types.PEc13;
import org.pepfar.pdma.app.utils.CustomLocalDateTimeDeserializer2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.pepfar.pdma.app.utils.DateTimeUtil;

public class PECaseDto {

	private Long id;

	private UUID uid;

	private PECaseDto parent;

	private String errorContent = null;

	private int numberErrorContent;

	private Boolean saved;

	private Set<PECaseDto> childs = new LinkedHashSet<PECaseDto>();

	// kì báo cáo gồm tháng 1 đến tháng 12
//	private Integer c1Report;

	// năm của kì báo cáo
//	private Integer c1ReportYear;
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime c1;

	// cơ sở báo cáo
	private OrganizationDto c1Org;

	// nhân viên báo cáo
	private StaffDto c1Staff;

	// họ tên người được tiếp cận
	private String c2;

	// giới tính người được tiếp cận
	private Gender c3;

	// Năm sinh:
//	@JsonSerialize(using = ToStringSerializer.class)
//	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
//	private LocalDateTime c4;

	private Integer c4;

//	7. Hộ khẩu: - Tỉnh/TP
	private AdminUnitDto c5Province;

//	- Quận/huyện:
	private AdminUnitDto c5District;

//	- phường xã String
	private String c5Ward;

	// C6. Khách hàng thuộc nhóm nguy cơ nào?
	private Set<PECaseRiskGroupDto> c6 = new LinkedHashSet<>();

	// cách thức tiếp cận
	private PEApproachMethod c7;
	private String c7Des;

	// tình trạng hiv khi tiếp cận
	private PEC8 c8;
	private String c8Des;

	// Tình trạng điều trị ARV khi tiếp cận:
	private PEC8ARV c8ARV;
	private String c8ARVDes;

	// Khách hàng được tư vấn dịch vụ TBXNBT/BC:
	private HTSYesNoNone c9;

	// Nếu c9 có , ghi rõ ngày tư vấn:
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime c9Date;

	// Khách hàng đồng ý cung cấp tên BT/BC
	private HTSYesNoNone c10;

	// Khách hàng có xét nghiệm HIV lần này:
	private HTSYesNoNone c11;

	// Nếu c11 có , ghi rõ ngày xét nghiệm:
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime c11Date;

	// Loại hình xét nghiệm:
	private PEC12 c12;
	private String c12Des;

	// Mã số XN tại cộng đồng/CSYT:
	private String c12Code;

	// Nếu c12 chọn tự XNHIV không có hỗ trợ, Tự XN HIV không có hỗ trợ cho:
	private PEC12BTBC c12Note;
	private String c12NoteDes;

	// Kết quả XN HIV lần này:
	private PEc13 c13;
	private String c13Des;

	// Nếu c13 có phản ứng HIV dương tính , Chuyển đi XN khẳng định HIV:
	private HTSYesNoNone c131;

	// Nếu c131 có , ghi rõ Mã số xét nghiệm khẳng định:
	private String c131Code;

	// Kết quả XN khẳng định HIV:
	private PEC131Result c131Result;
	private String c131ResultDes;

	// KQXN sàng lọc Giang mai:
	private PEC132 c132;
	private String c132Des;

	// Khách hàng sử dụng dịch vụ điều trị PrEP:
	private HTSYesNoNone c14;

	// Nếu c14 có , ghi rõ Ngày nhận dịch vụ:
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime c14Date;

	// Nếu c14 có , ghi rõ mã số điều trị
	private String c14Code;

	// Nếu c14 có , ghi rõ tên cơ sở điều trị
	private String c14Name;

	// Khách hàng sử dụng dịch vụ điều trị ARV:
	private HTSYesNoNone c15;

	// Nếu c15 có , ghi rõ Ngày nhận dịch vụ:
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime c15Date;

	// Nếu c15 có , ghi rõ mã số điều trị
	private String c15Code;

	// Nếu c15 có , ghi rõ tên cơ sở điều trị
	private String c15Name;

	// Kết quả xác minh ca HIV dương tính?
	private PEC16 c16;
	private String c16Des;

	// c17 ghi chú
	private String c17;

	private boolean isReadAble;

	private boolean isWritAble;

	private boolean isDeleteAble;

	private boolean isEditAble;

	public PECaseDto() {
		super();
	}

	public PECaseDto(PECase entity, Boolean isViewPII,
					 boolean isReadAble,
					 boolean isWritAble,
					 boolean isDeleteAble) {

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
		if (entity.getParent() != null) {
			this.parent = new PECaseDto();
			this.parent.setId(entity.getParent().getId());
		}
		if(entity.getChilds()!=null) {
			this.childs = new LinkedHashSet<PECaseDto>();
			for (PECase child : entity.getChilds()) {
				if(isViewPII) {
					this.childs.add(new PECaseDto(child, true, true, false, false));
				} else {
					this.childs.add(new PECaseDto(child, false,true, false, false));
				}
			}
		}
//		this.c1Report = entity.getC1Report();
		this.c1 = entity.getC1();
		if(isViewPII) {
			this.c2 = entity.getC2();
		} else {
			this.c2 = "-";
		}

		this.c3 = entity.getC3();
		if (entity.getC1Org() != null) {
			this.c1Org = new OrganizationDto(entity.getC1Org());
			this.c1Org.setId(entity.getC1Org().getId());
			this.c1Org.setName(entity.getC1Org().getName());
			this.c1Org.setCode(entity.getC1Org().getCode());
		}
		if (entity.getC1Staff() != null) {
			this.c1Staff = new StaffDto();
			this.c1Staff.setId(entity.getC1Staff().getId());
			this.c1Staff.setFullName(entity.getC1Staff().getPerson().getFullname());
		}

		if (entity.getC4() != null) {
			this.c4 = entity.getC4().getYear();
		}

		if (entity.getC5Province() != null) {
			this.c5Province = new AdminUnitDto();
			this.c5Province.setId(entity.getC5Province().getId());
			this.c5Province.setName(entity.getC5Province().getName());
			this.c5Province.setCode(entity.getC5Province().getCode());
			this.c5Province.setCodeGso(entity.getC5Province().getCodeGso());
		}
		if (entity.getC5District() != null) {
			this.c5District = new AdminUnitDto();
			this.c5District.setId(entity.getC5District().getId());
			this.c5District.setName(entity.getC5District().getName());
			this.c5District.setCode(entity.getC5District().getCode());
			this.c5District.setCodeGso(entity.getC5District().getCodeGso());
		}

		if(isViewPII) {
			this.c5Ward = entity.getC5Ward();
		} else {
			this.c5Ward = "-";
		}

		if (entity.getC6() != null) {
			for (PECaseRiskGroup peCaseRiskGroup : entity.getC6()) {
				this.c6.add(new PECaseRiskGroupDto(peCaseRiskGroup));
			}
		}

		this.c7 = entity.getC7();
		this.c8 = entity.getC8();
		this.c8ARV = entity.getC8ARV();
		this.c9 = entity.getC9();
		this.c9Date = entity.getC9Date();
		this.c10 = entity.getC10();
		this.c11 = entity.getC11();
		this.c11Date = entity.getC11Date();
		this.c12 = entity.getC12();
		this.c12Code = entity.getC12Code();
		this.c12Note = entity.getC12Note();
		this.c13 = entity.getC13();
		this.c131 = entity.getC131();
		this.c131Code = entity.getC131Code();
		this.c131Result = entity.getC131Result();
		this.c132 = entity.getC132();
		this.c14 = entity.getC14();
		this.c14Code = entity.getC14Code();
		this.c14Date = entity.getC14Date();
		this.c14Name = entity.getC14Name();
		this.c15 = entity.getC15();
		this.c15Code = entity.getC15Code();
		this.c15Date = entity.getC15Date();
		this.c15Name = entity.getC15Name();
		this.c16 = entity.getC16();
		this.c17 = entity.getC17();
//		this.c1ReportYear = entity.getC1ReportYear();
//		this.c1 = entity.getC1();
	}

	public boolean isEditAble() {return DateTimeUtil.checkEditableByMonth(Constants.NUMBER_OF_MONTH_FOR_EDIT,this.getC1());}

	public void setEditAble(boolean editAble) {isEditAble = editAble;}

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

	public UUID getUid() {
		return uid;
	}

	public void setUid(UUID uid) {
		this.uid = uid;
	}

	public PECaseDto getParent() {
		return parent;
	}

	public void setParent(PECaseDto parent) {
		this.parent = parent;
	}

//	public Integer getC1Report() {
//		return c1Report;
//	}

	public String getErrorContent() {
		return errorContent;
	}

	public void setErrorContent(String errorContent) {
		this.errorContent = errorContent;
	}

	public int getNumberErrorContent() {
		return numberErrorContent;
	}

	public void setNumberErrorContent(int numberErrorContent) {
		this.numberErrorContent = numberErrorContent;
	}

	public Set<PECaseDto> getChilds() {
		return childs;
	}

	public void setChilds(Set<PECaseDto> childs) {
		this.childs = childs;
	}

//	public void setC1Report(Integer c1Report) {
//		this.c1Report = c1Report;
//	}

	public LocalDateTime getC1() {
		return c1;
	}

	public void setC1(LocalDateTime c1) {
		this.c1 = c1;
	}

	public OrganizationDto getC1Org() {
		return c1Org;
	}

	public void setC1Org(OrganizationDto c1Org) {
		this.c1Org = c1Org;
	}

	public StaffDto getC1Staff() {
		return c1Staff;
	}

	public void setC1Staff(StaffDto c1Staff) {
		this.c1Staff = c1Staff;
	}

	public String getC2() {
		return c2;
	}

	public void setC2(String c2) {
		this.c2 = c2;
	}

	public Gender getC3() {
		return c3;
	}

	public void setC3(Gender c3) {
		this.c3 = c3;
	}

	public Integer getC4() {
		return c4;
	}

	public void setC4(Integer c4) {
		this.c4 = c4;
	}

	public AdminUnitDto getC5Province() {
		return c5Province;
	}

	public void setC5Province(AdminUnitDto c5Province) {
		this.c5Province = c5Province;
	}

	public AdminUnitDto getC5District() {
		return c5District;
	}

	public void setC5District(AdminUnitDto c5District) {
		this.c5District = c5District;
	}

	public String getC5Ward() {
		return c5Ward;
	}

	public void setC5Ward(String c5Ward) {
		this.c5Ward = c5Ward;
	}

	public Set<PECaseRiskGroupDto> getC6() {
		return c6;
	}

	public void setC6(Set<PECaseRiskGroupDto> c6) {
		this.c6 = c6;
	}

	public PEApproachMethod getC7() {
		return c7;
	}

	public void setC7(PEApproachMethod c7) {
		this.c7 = c7;
	}

	public PEC8 getC8() {
		return c8;
	}

	public void setC8(PEC8 c8) {
		this.c8 = c8;
	}

	public PEC8ARV getC8ARV() {
		return c8ARV;
	}

	public void setC8ARV(PEC8ARV c8arv) {
		c8ARV = c8arv;
	}

	public HTSYesNoNone getC9() {
		return c9;
	}

	public void setC9(HTSYesNoNone c9) {
		this.c9 = c9;
	}

	public LocalDateTime getC9Date() {
		return c9Date;
	}

	public void setC9Date(LocalDateTime c9Date) {
		this.c9Date = c9Date;
	}

	public HTSYesNoNone getC10() {
		return c10;
	}

	public void setC10(HTSYesNoNone c10) {
		this.c10 = c10;
	}

	public HTSYesNoNone getC11() {
		return c11;
	}

	public void setC11(HTSYesNoNone c11) {
		this.c11 = c11;
	}

	public LocalDateTime getC11Date() {
		return c11Date;
	}

	public void setC11Date(LocalDateTime c11Date) {
		this.c11Date = c11Date;
	}

	public PEC12 getC12() {
		return c12;
	}

	public void setC12(PEC12 c12) {
		this.c12 = c12;
	}

	public String getC12Code() {
		return c12Code;
	}

	public void setC12Code(String c12Code) {
		this.c12Code = c12Code;
	}

	public PEC12BTBC getC12Note() {
		return c12Note;
	}

	public void setC12Note(PEC12BTBC c12Note) {
		this.c12Note = c12Note;
	}

	public PEc13 getC13() {
		return c13;
	}

	public void setC13(PEc13 c13) {
		this.c13 = c13;
	}

	public HTSYesNoNone getC131() {
		return c131;
	}

	public void setC131(HTSYesNoNone c131) {
		this.c131 = c131;
	}

	public String getC131Code() {
		return c131Code;
	}

	public void setC131Code(String c131Code) {
		this.c131Code = c131Code;
	}

	public PEC131Result getC131Result() {
		return c131Result;
	}

	public void setC131Result(PEC131Result c131Result) {
		this.c131Result = c131Result;
	}

	public PEC132 getC132() {
		return c132;
	}

	public void setC132(PEC132 c132) {
		this.c132 = c132;
	}

	public HTSYesNoNone getC14() {
		return c14;
	}

	public void setC14(HTSYesNoNone c14) {
		this.c14 = c14;
	}

	public LocalDateTime getC14Date() {
		return c14Date;
	}

	public void setC14Date(LocalDateTime c14Date) {
		this.c14Date = c14Date;
	}

	public String getC14Code() {
		return c14Code;
	}

	public void setC14Code(String c14Code) {
		this.c14Code = c14Code;
	}

	public String getC14Name() {
		return c14Name;
	}

	public void setC14Name(String c14Name) {
		this.c14Name = c14Name;
	}

	public HTSYesNoNone getC15() {
		return c15;
	}

	public void setC15(HTSYesNoNone c15) {
		this.c15 = c15;
	}

	public LocalDateTime getC15Date() {
		return c15Date;
	}

	public void setC15Date(LocalDateTime c15Date) {
		this.c15Date = c15Date;
	}

	public String getC15Code() {
		return c15Code;
	}

	public void setC15Code(String c15Code) {
		this.c15Code = c15Code;
	}

	public String getC15Name() {
		return c15Name;
	}

	public void setC15Name(String c15Name) {
		this.c15Name = c15Name;
	}

	public PEC16 getC16() {
		return c16;
	}

	public void setC16(PEC16 c16) {
		this.c16 = c16;
	}

	public String getC17() {
		return c17;
	}

	public void setC17(String c17) {
		this.c17 = c17;
	}

//	public Integer getC1ReportYear() {
//		return c1ReportYear;
//	}
//
//	public void setC1ReportYear(Integer c1ReportYear) {
//		this.c1ReportYear = c1ReportYear;
//	}

	public String getC7Des() {
		if (c7 != null) {
			c7Des = c7.getDescription();
		}
		return c7Des;
	}

	public String getC8Des() {
		if (c8 != null) {
			c8Des = c8.getDescription();
		}
		return c8Des;
	}

	public String getC8ARVDes() {
		if (c8ARV != null) {
			c8ARVDes = c8ARV.getDescription();
		}
		return c8ARVDes;
	}

	public String getC12Des() {
		if (c12 != null) {
			c12Des = c12.getDescription();
		}
		return c12Des;
	}

	public String getC12NoteDes() {
		if (c12Note != null) {
			c12NoteDes = c12Note.getDescription();
		}
		return c12NoteDes;
	}

	public String getC13Des() {
		if (c13 != null) {
			c13Des = c13.getDescription();
		}
		return c13Des;
	}

	public String getC131ResultDes() {
		if (c131Result != null) {
			c131ResultDes = c131Result.getDescription();
		}
		return c131ResultDes;
	}

	public String getC132Des() {
		if (c132 != null) {
			c132Des = c132.getDescription();
		}
		return c132Des;
	}

	public String getC16Des() {
		if (c16 != null) {
			c16Des = c16.getDescription();
		}
		return c16Des;
	}

	public boolean isReadAble() {
		return isReadAble;
	}

	public void setReadAble(boolean readAble) {
		isReadAble = readAble;
	}

	public boolean isWritAble() {
		return isWritAble;
	}

	public void setWritAble(boolean writAble) {
		isWritAble = writAble;
	}

	public boolean isDeleteAble() {
		return isDeleteAble;
	}

	public void setDeleteAble(boolean deleteAble) {
		isDeleteAble = deleteAble;
	}
}
