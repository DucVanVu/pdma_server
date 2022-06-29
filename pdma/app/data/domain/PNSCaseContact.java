package org.pepfar.pdma.app.data.domain;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.pepfar.pdma.app.data.domain.auditing.AuditableEntity;
import org.pepfar.pdma.app.data.types.Gender;
import org.pepfar.pdma.app.data.types.PNSc11;
import org.pepfar.pdma.app.data.types.PNSc5Reason;
import org.pepfar.pdma.app.data.types.PNSc8;
import org.pepfar.pdma.app.data.types.PNSc9;
import org.pepfar.pdma.app.data.types.HTSYesNoNone;
import org.pepfar.pdma.app.data.types.PNSHivStatus;
import org.pepfar.pdma.app.data.types.PNSSucceededContactMethod;
import org.pepfar.pdma.app.data.types.PNSSucceededMethod;
import org.pepfar.pdma.app.utils.LocalDateTimeAttributeConverter;

@Entity
@Table(name = "tbl_pns_case_contact")
public class PNSCaseContact extends AuditableEntity {

	@Transient
	private static final long serialVersionUID = -300384104809700661L;

	@Id
	@GenericGenerator(name = "native", strategy = "native")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "native")
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "pns_case_id", nullable = true)
	private PNSCase pnsCase;
	/*
	 * Họ tên BT/BC/con đẻ phơi nhiễm:
	 * Năm sinh:
	 * Giới tính khi sinh:
	 * - Số điện thoại:
	 * Lấy từ Person
	 */	
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "person_id", nullable = true)
	private Person person;
	
//	Nơi cư trú: - Tỉnh/TP 
	@ManyToOne
	@JoinColumn(name = "province_id", nullable = true)
	private AdminUnit province;
//	- Quận/huyện: 
	@ManyToOne
	@JoinColumn(name = "district_id", nullable = true)
	private AdminUnit district;
	
//	- Địa chỉ cụ thể:
	@Column(name = "address_detail", nullable = true)
	private String addressDetail;		
	
//	C1. Quan hệ với người có HIV?
	@OneToMany(mappedBy = "pnsCaseContact", cascade = CascadeType.ALL, fetch = FetchType.EAGER,orphanRemoval = true)
	private Set<PNSCaseContactRelationship> c1 = new LinkedHashSet<PNSCaseContactRelationship>();
	
//	Ngày khai thác được thông tin
	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "c1_received_info_date", nullable = true)
	private LocalDateTime c1receivedInfoDate;
	
//	C2. Tình trạng HIV 
	@Column(name = "c2_hiv_status", nullable = true)
	@Enumerated(value = EnumType.STRING)
	private PNSHivStatus c2;
	
//	C3. Nguy cơ bạo lực:
	@Column(name = "c3_violence_risk", nullable = true)
	@Enumerated(value = EnumType.STRING)
	private HTSYesNoNone c3;
	
//	C4. Đã liên lạc thông báo - Lần 1:
	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "c4_first", nullable = true)
	private LocalDateTime c4First;
//	 - Lần 2:
	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "c4_second", nullable = true)
	private LocalDateTime c4Second;
//	 - Lần 3:
	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "c4_third", nullable = true)
	private LocalDateTime c4Third;
	
//	C5. Đã liên lạc được?
	@Column(name = "c5_contacted", nullable = true)
	@Enumerated(value = EnumType.STRING)
	private HTSYesNoNone c5;
//	- Ghi rõ lý do 
	@Column(name = "c5_reason", nullable = true)
	@Enumerated(value = EnumType.STRING)
	private PNSc5Reason c5Reason;
	
//	C6. Biện pháp đã thành công 
	@Column(name = "c6_succeeded_method", nullable = true)
	@Enumerated(value = EnumType.STRING)
	private PNSSucceededMethod c6;
	
//	C7. Cách đã liên lạc thành công 
	@Column(name = "c7_succeeded_contact_method", nullable = true)
	@Enumerated(value = EnumType.STRING)
	private PNSSucceededContactMethod c7;
	
//	- Ghi rõ cách liên lạc khác:
	@Column(name = "c7_note", nullable = true)
	private String c7Note;
	
//	C8. Xét nghiệm HIV
	@Column(name = "c8_hiv_labtest_status", nullable = true)
	@Enumerated(value = EnumType.STRING)
	private PNSc8 c8;
	
//	- Mã số KH TVXN HIV:
	@Column(name = "c8_labtest_code", nullable = true)
	private String c8LabtestCode;
	
	// HTSCase
	@OneToOne 
	@JoinColumn(name = "c8_hts_case", nullable = true)
	private HTSCase c8HTSCase;
	
//	- Ngày XN HIV:
	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "c8_labtest_date", nullable = true)
	private LocalDateTime c8LabtestDate;
	
//	- Tên cơ sở TVXN HIV:
	
	@Column(name = "c8_labtest_org", nullable = true)
	private String c8LabtestOrg;
	
//	C9. Kết quả XN HIV 
	@Column(name = "c9", nullable = true)
	@Enumerated(value = EnumType.STRING)
	private PNSc9 c9;
	
	@Column(name = "c9_joined_prep", nullable = true)
	@Enumerated(value = EnumType.STRING)
	private HTSYesNoNone c9JoinedPrEP;
	
	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "c9_prep_date", nullable = true)
	private LocalDateTime c9PrEPDate;
	
	@Column(name = "c9_joined_arv", nullable = true)
	@Enumerated(value = EnumType.STRING)
	private HTSYesNoNone c9JoinedARV;
	
	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "c9_arv_date", nullable = true)
	private LocalDateTime c9ARVDate;
		
	public PNSCaseContact() {
		super();
	}

	public PNSCaseContact(Long id, PNSCase pnsCase, Person person, AdminUnit province, AdminUnit district,
			String addressDetail, Set<PNSCaseContactRelationship> relationships, LocalDateTime receivedInfoDate,
			PNSHivStatus c2, HTSYesNoNone c3, LocalDateTime c4First, LocalDateTime c4Second, LocalDateTime c4Third,
			HTSYesNoNone c5, PNSc5Reason c5Reason, PNSSucceededMethod c6, PNSSucceededContactMethod c7, String c7Note,
			PNSc8 c8, String c8LabtestCode, HTSCase c8HTSCase, LocalDateTime c8LabtestDate, String c8LabtestOrg, PNSc9 c9,
			HTSYesNoNone c9JoinedPrEP, LocalDateTime c9PrEPDate, HTSYesNoNone c9JoinedARV, LocalDateTime c9arvDate) {
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
		this.c3 = c3;
		this.c4First = c4First;
		this.c4Second = c4Second;
		this.c4Third = c4Third;
		this.c5 = c5;
		this.c5Reason = c5Reason;
		this.c6 = c6;
		this.c7 = c7;
		this.c7Note = c7Note;
		this.c8 = c8;
		this.c8LabtestCode = c8LabtestCode;
		this.c8HTSCase= c8HTSCase;
		this.c8LabtestDate = c8LabtestDate;
		this.c8LabtestOrg = c8LabtestOrg;
		this.c9 = c9;
		this.c9JoinedPrEP = c9JoinedPrEP;
		this.c9PrEPDate = c9PrEPDate;
		this.c9JoinedARV = c9JoinedARV;
		this.c9ARVDate = c9arvDate;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public PNSCase getPnsCase() {
		return pnsCase;
	}

	public void setPnsCase(PNSCase pnsCase) {
		this.pnsCase = pnsCase;
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public AdminUnit getProvince() {
		return province;
	}

	public void setProvince(AdminUnit province) {
		this.province = province;
	}

	public AdminUnit getDistrict() {
		return district;
	}

	public void setDistrict(AdminUnit district) {
		this.district = district;
	}

	public String getAddressDetail() {
		return addressDetail;
	}

	public void setAddressDetail(String addressDetail) {
		this.addressDetail = addressDetail;
	}
	
	public Set<PNSCaseContactRelationship> getC1() {
		if(c1==null) {
			c1 = new LinkedHashSet<PNSCaseContactRelationship>();
		}
		return c1;
	}

	public void setC1(Set<PNSCaseContactRelationship> c1Relationships) {
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

	public void setC5Reason(PNSc5Reason c5Reason){
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

	public HTSCase getC8HTSCase() {
		return c8HTSCase;
	}

	public void setC8HTSCase(HTSCase c8htsCase) {
		c8HTSCase = c8htsCase;
	}	
	
}
