package org.pepfar.pdma.app.data.domain;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.pepfar.pdma.app.data.domain.auditing.AuditableEntity;
import org.pepfar.pdma.app.data.types.Gender;
import org.pepfar.pdma.app.data.types.HTSYesNoNone;
import org.pepfar.pdma.app.data.types.PEC12;
import org.pepfar.pdma.app.data.types.PEC12BTBC;
import org.pepfar.pdma.app.data.types.PEc13;
import org.pepfar.pdma.app.data.types.PEC131Result;
import org.pepfar.pdma.app.data.types.PEC132;
import org.pepfar.pdma.app.data.types.PEC16;
import org.pepfar.pdma.app.data.types.PEC8;
import org.pepfar.pdma.app.data.types.PEC8ARV;
import org.pepfar.pdma.app.data.types.PEApproachMethod;
import org.pepfar.pdma.app.utils.LocalDateTimeAttributeConverter;
import org.pepfar.pdma.app.utils.UUIDAttributeConverter;

@Entity
@Table(name = "tbl_pe_case")
public class PECase extends AuditableEntity{

	@Transient
	private static final long serialVersionUID = -3477783361124277148L;

	@Id
	@GenericGenerator(name = "native", strategy = "native")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "native")
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Convert(converter = UUIDAttributeConverter.class)
	@Column(name = "uuid", unique = true, nullable = false, updatable = false, columnDefinition = "char(36)")
	private UUID uid;

	@ManyToOne
	@JoinColumn(name = "parent_id", nullable = true)
	private PECase parent;
	
	@OneToMany(mappedBy = "parent", fetch = FetchType.EAGER)
	private Set<PECase> childs = new LinkedHashSet<PECase>();
	
	// kì báo cáo gồm tháng 1 đến tháng 12
//	@Column(name="c1_report_period")
//	private Integer c1Report;

	// năm của kì báo cáo
//	@Column(name="c1_report_period_year")
//	private Integer c1ReportYear;
	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "c1", nullable = true)
	private LocalDateTime c1;

	// cơ sở báo cáo
	@ManyToOne
	@JoinColumn(name = "c1_org_id", nullable = true)
	private Organization c1Org;
	
	// nhân viên báo cáo
	@ManyToOne
	@JoinColumn(name = "c1_staff_id", nullable = true)
	private Staff c1Staff;
	
	//họ tên người được tiếp cận
	@Column(name = "c2", nullable = true)
	private String c2;
	
	// giới tính người được tiếp cận
	@Column(name = "c3", nullable = true)
	@Enumerated(value = EnumType.STRING)
	private Gender c3;
	
	// Năm sinh:
	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "c4_dob", nullable = true)
	private LocalDateTime c4;
	
//	7. Hộ khẩu: - Tỉnh/TP
	@ManyToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "c5_province_id", nullable = true)
	private AdminUnit c5Province;
	
//	- Quận/huyện:
	@ManyToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "c5_district_id", nullable = true)
	private AdminUnit c5District;
	
//	- phường xã String:
	@Column(name = "c5_ward", nullable = true)
	private String c5Ward;
	
	//C6. Khách hàng thuộc nhóm nguy cơ nào?
	@OneToMany(mappedBy = "peCase", cascade = CascadeType.ALL, fetch = FetchType.EAGER,orphanRemoval = true)
	private Set<PECaseRiskGroup> c6 = new LinkedHashSet<>();
	
	// cách thức tiếp cận
	@Column(name = "c7", nullable = true)
	@Enumerated(value = EnumType.STRING)
	private PEApproachMethod c7;
	
	//tình trạng hiv khi tiếp cận
	@Column(name = "c8", nullable = true)
	@Enumerated(value = EnumType.STRING)
	private PEC8 c8;
	
	//Tình trạng điều trị ARV khi tiếp cận:
	@Column(name = "c8_arv", nullable = true)
	@Enumerated(value = EnumType.STRING)
	private PEC8ARV c8ARV;
	
	//Khách hàng được tư vấn dịch vụ TBXNBT/BC:
	@Column(name = "c9", nullable = true)
	@Enumerated(value = EnumType.STRING)
	private HTSYesNoNone c9;
	
	// Nếu c9 có , ghi rõ ngày tư vấn:
	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "c9_date", nullable = true)
	private LocalDateTime c9Date;
	
	//Khách hàng đồng ý cung cấp tên BT/BC:
	@Column(name = "c10", nullable = true)
	@Enumerated(value = EnumType.STRING)
	private HTSYesNoNone c10;
	
	// Khách hàng có xét nghiệm HIV lần này:
	@Column(name = "c11", nullable = true)
	@Enumerated(value = EnumType.STRING)
	private HTSYesNoNone c11;
	
	//Nếu c11 có , ghi rõ ngày xét nghiệm:
	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "c11_date", nullable = true)
	private LocalDateTime c11Date;
	
	//Loại hình xét nghiệm:
	@Column(name = "c12", nullable = true)
	@Enumerated(value = EnumType.STRING)
	private PEC12 c12;
	
	//Mã số XN tại cộng đồng/CSYT:
	@Column(name = "c12_code", nullable = true)
	private String c12Code;
	
	//Nếu c12 chọn tự XNHIV không có hỗ trợ, Tự XN HIV không có hỗ trợ cho:
	@Column(name = "c12_note", nullable = true)
	@Enumerated(value = EnumType.STRING)
	private PEC12BTBC c12Note;
	
	//Kết quả XN HIV lần này:
	@Column(name = "c13", nullable = true)
	@Enumerated(value = EnumType.STRING)
	private PEc13 c13;
	
	//Nếu c13 có phản ứng HIV dương tính , Chuyển đi XN khẳng định HIV:
	@Column(name = "c131", nullable = true)
	@Enumerated(value = EnumType.STRING)
	private HTSYesNoNone c131;
	
	//Nếu c131 có , ghi rõ Mã số xét nghiệm khẳng định:
	@Column(name = "c131_code", nullable = true)
	private String c131Code;
	
	//Kết quả XN khẳng định HIV:
	@Column(name = "c131_result", nullable = true)
	@Enumerated(value = EnumType.STRING)
	private PEC131Result c131Result;
	
	//KQXN sàng lọc Giang mai:
	@Column(name = "c132", nullable = true)
	@Enumerated(value = EnumType.STRING)
	private PEC132 c132;
	
	
	//Khách hàng sử dụng dịch vụ điều trị PrEP:
	@Column(name = "c14", nullable = true)
	@Enumerated(value = EnumType.STRING)
	private HTSYesNoNone c14;
	
	//Nếu c14 có , ghi rõ Ngày nhận dịch vụ:
	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "c14_date", nullable = true)
	private LocalDateTime c14Date;
		
	//Nếu c14 có , ghi rõ mã số điều trị
	@Column(name = "c14_code", nullable = true)
	private String c14Code;
	
	//Nếu c14 có , ghi rõ tên cơ sở điều trị
	@Column(name = "c14_name", nullable = true)
	private String c14Name;
	
	//Khách hàng sử dụng dịch vụ điều trị ARV:
	@Column(name = "c15", nullable = true)
	@Enumerated(value = EnumType.STRING)
	private HTSYesNoNone c15;
	
	//Nếu c15 có , ghi rõ Ngày nhận dịch vụ:
	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "c15_date", nullable = true)
	private LocalDateTime c15Date;
	
	//Nếu c15 có , ghi rõ mã số điều trị
	@Column(name = "c15_code", nullable = true)
	private String c15Code;
	
	//Nếu c15 có , ghi rõ tên cơ sở điều trị
	@Column(name = "c15_name", nullable = true)
	private String c15Name;
	
	//Kết quả xác minh ca HIV dương tính?
	@Column(name = "c16", nullable = true)
	@Enumerated(value = EnumType.STRING)
	private PEC16 c16;
	
	//c17 ghi chú
	@Column(name = "c17", nullable = true)
	private String c17;

	public PECase() {
		super();
		this.uid = UUID.randomUUID();
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

	public PECase getParent() {
		return parent;
	}

	public void setParent(PECase parent) {
		this.parent = parent;
	}

	public Set<PECase> getChilds() {
		if(childs==null) {
			childs = new LinkedHashSet<PECase>();
		}
		return childs;
	}

	public void setChilds(Set<PECase> childs) {
		this.childs = childs;
	}

//	public Integer getC1Report() {
//		return c1Report;
//	}
//
//	public void setC1Report(Integer c1Report) {
//		this.c1Report = c1Report;
//	}


	public Organization getC1Org() {
		return c1Org;
	}

	public void setC1Org(Organization c1Org) {
		this.c1Org = c1Org;
	}

	public Staff getC1Staff() {
		return c1Staff;
	}

	public void setC1Staff(Staff c1Staff) {
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

	public LocalDateTime getC4() {
		return c4;
	}

	public void setC4(LocalDateTime c4) {
		this.c4 = c4;
	}

	public AdminUnit getC5Province() {
		return c5Province;
	}

	public void setC5Province(AdminUnit c5Province) {
		this.c5Province = c5Province;
	}

	public AdminUnit getC5District() {
		return c5District;
	}

	public void setC5District(AdminUnit c5District) {
		this.c5District = c5District;
	}

	public String getC5Ward() {
		return c5Ward;
	}

	public void setC5Ward(String c5Ward) {
		this.c5Ward = c5Ward;
	}

	public Set<PECaseRiskGroup> getC6() {
		if(c6==null) {
			c6 = new LinkedHashSet<PECaseRiskGroup>();
		}
		return c6;
	}

	public void setC6(Set<PECaseRiskGroup> c6) {
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

	public LocalDateTime getC1() {
		return c1;
	}

	public void setC1(LocalDateTime c1) {
		this.c1 = c1;
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
	
	
	
}
