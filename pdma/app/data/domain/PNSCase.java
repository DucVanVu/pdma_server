package org.pepfar.pdma.app.data.domain;

import java.time.LocalDateTime;
import java.util.HashSet;
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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.pepfar.pdma.app.data.domain.auditing.AuditableEntity;
import org.pepfar.pdma.app.data.types.Gender;
import org.pepfar.pdma.app.data.types.PNSc11;
import org.pepfar.pdma.app.data.types.HTSYesNoNone;
import org.pepfar.pdma.app.utils.LocalDateTimeAttributeConverter;
import org.pepfar.pdma.app.utils.UUIDAttributeConverter;

@Entity
@Table(name = "tbl_pns_case")
public class PNSCase extends AuditableEntity {

	@Transient
	private static final long serialVersionUID = -300384104809700661L;

	@Id
	@GenericGenerator(name = "native", strategy = "native")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "native")
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Convert(converter = UUIDAttributeConverter.class)
	@Column(name = "uuid", unique = true, nullable = false, updatable = false, columnDefinition = "char(36)")
	private UUID uid;

	@ManyToOne
	@JoinColumn(name = "c2_org_id", nullable = true)
	private Organization c2;
	
//	C3. Họ tên tư vấn viên:
	@ManyToOne
	@JoinColumn(name = "c3_staff_id", nullable = true)
	private Staff c3;
	
	@OneToOne(cascade = CascadeType.DETACH, orphanRemoval = false)
	@JoinColumn(name = "hts_case_id", nullable = true)
	private HTSCase htsCase;
	
	@OneToOne(cascade = CascadeType.DETACH, orphanRemoval = false)
	@JoinColumn(name = "case_id", nullable = true)
	private Case hivCase;
	
//	C4. Mã số khách hàng:
	@Column(name = "c4_client_code", nullable = true)
	private String c4;
	
	//C5. Ngày tư vấn dịch vụ TBXNBT/BC
	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "c5_date_counselling", nullable = true)
	private LocalDateTime c5;
		
	//C6. Đồng ý nhận dịch vụ TBXNBT/BC?
	@Column(name = "c6_accept_service", nullable = true)
	@Enumerated(value = EnumType.STRING)
	private HTSYesNoNone c6;
	
//	- Ngày đồng ý nhận dịch vụ:
	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "c6_date_service", nullable = true)
	private LocalDateTime c6Date;
	
	//C7. Họ tên người có HIV:
	@Column(name = "c7", nullable = true)
	private String c7;
	
	//C8. Giới tính khi sinh:
	@Column(name = "c8_gender", nullable = true)
	@Enumerated(value = EnumType.STRING)
	private Gender c8;
	
//	C9. Năm sinh:
	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "c6_dob", nullable = true)
	private LocalDateTime c9;
	
//	C10. Ngày XN khẳng định HIV+:
	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "c10_hiv_confirm_date", nullable = true)
	private LocalDateTime c10;
	
//	C11. Tình trạng ĐT ARV?
	@Column(name = "c11_arv_treatment_status", nullable = true)
	@Enumerated(value = EnumType.STRING)
	private PNSc11 c11;
		
	//C12. Khách hàng thuộc nhóm nguy cơ nào?
	@OneToMany(mappedBy = "pnsCase", cascade = CascadeType.ALL, fetch = FetchType.EAGER,orphanRemoval = true)
//	@OrderBy("seqByOrganization")
 	private Set<PNSCaseRiskGroup> c12 = new LinkedHashSet<>();
	
	//Phần ghi rõ của 12
	@Column(name = "c12_note", nullable = true)
	private String c12Note;
	
	@OneToMany(mappedBy = "pnsCase", fetch = FetchType.EAGER)
//	@OrderBy("seqByOrganization")
 	private Set<PNSCaseContact> contacts = new LinkedHashSet<>();
	
	public PNSCase() {
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

	public Organization getC2() {
		return c2;
	}

	public void setC2(Organization c2) {
		this.c2 = c2;
	}

	public HTSCase getHtsCase() {
		return htsCase;
	}

	public void setHtsCase(HTSCase htsCase) {
		this.htsCase = htsCase;
	}

	public Case getHivCase() {
		return hivCase;
	}

	public void setHivCase(Case hivCase) {
		this.hivCase = hivCase;
	}

	public Staff getC3() {
		return c3;
	}

	public void setC3(Staff c3) {
		this.c3 = c3;
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

	public LocalDateTime getC9() {
		return c9;
	}

	public void setC9(LocalDateTime c9) {
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

	public Set<PNSCaseRiskGroup> getC12() {
		if(this.c12==null) {
			this.c12 = new LinkedHashSet<PNSCaseRiskGroup>();
		}
		return c12;
	}

	public void setC12(Set<PNSCaseRiskGroup> c12) {
		this.c12 = c12;
	}

	public String getC12Note() {
		return c12Note;
	}

	public void setC12Note(String c12Note) {
		this.c12Note = c12Note;
	}

	public Set<PNSCaseContact> getContacts() {
		return contacts;
	}

	public void setContacts(Set<PNSCaseContact> contacts) {
		this.contacts = contacts;
	}
	
}
