package org.pepfar.pdma.app.data.domain;

import java.time.LocalDateTime;
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
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.GenericGenerator;
import org.pepfar.pdma.app.data.domain.auditing.AuditableEntity;
import org.pepfar.pdma.app.data.types.Gender;
import org.pepfar.pdma.app.data.types.HIVStatus;
import org.pepfar.pdma.app.data.types.SNSApproachMethod;
import org.pepfar.pdma.app.data.types.SNSCustomerSource;
import org.pepfar.pdma.app.data.types.SNSIdNumberType;
import org.pepfar.pdma.app.data.types.SNSRiskGroup;
import org.pepfar.pdma.app.utils.LocalDateTimeAttributeConverter;

@Entity
@Table(name = "tbl_sns_case",
		uniqueConstraints = @UniqueConstraint(columnNames = { "seq_by_organization", "org_id"}))
public class SNSCase extends AuditableEntity {

	@Transient
	private static final long serialVersionUID = 4491294808808746383L;

	@Id
	@GenericGenerator(name = "native", strategy = "native")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "native")
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "coupon_code", length = 50, nullable = false, unique = true)
	private String couponCode;

	@Column(name = "name", length = 100, nullable = false)
	private String name;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "dob", nullable = true)
	private LocalDateTime dob;

	@Column(name = "gender", nullable = false)
	@Enumerated(value = EnumType.STRING)
	private Gender gender;

	@Column(name = "risk_group", nullable = true)
	@Enumerated(value = EnumType.STRING)
	private SNSRiskGroup riskGroup;

	// CMND/CCCD/số thẻ BH/Bằng LX/Số ĐT/Số hộ khẩu
	@Column(name = "id_number", nullable = true)
	private String idNumber;
	
	@Column(name = "id_number_type", nullable = true)
	@Enumerated(value = EnumType.STRING)
	private SNSIdNumberType idNumberType;

	// Tình trạng HIV
	@Column(name = "hiv_status", nullable = true)
	@Enumerated(value = EnumType.STRING)
	private HIVStatus hivStatus;
	
	//Ngày xét nghiệm
	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "test_date", nullable = true)
	private LocalDateTime testDate;

	// Nguồn khách hàng
	@Column(name = "customer_source", nullable = false)
	@Enumerated(value = EnumType.STRING)
	private SNSCustomerSource customerSource;

	// Hình thức tiếp cận
	@Column(name = "approach_method", nullable = true)
	@Enumerated(value = EnumType.STRING)
	private SNSApproachMethod approachMethod;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "issue_date", nullable = false)
	private LocalDateTime issueDate;
	
	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "first_time_visit", nullable = true)
	private LocalDateTime firstTimeVisit;

	@Column(name = "total_coupon", nullable = false)
	private Integer totalCoupon;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "prep_date", nullable = true)
	private LocalDateTime prepDate;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "arv_date", nullable = true)
	private LocalDateTime arvDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id", nullable = true)
	private SNSCase parent;

	@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
	@OrderBy("seqByOrganization")
	private Set<SNSCase> children = new LinkedHashSet<>();
	
	//Cơ sở phát Coupon cho người khách hàng
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "org_id", referencedColumnName = "id", nullable = false)
	private Organization organization;
	
	//Cơ sở khách hàng quay lại để làm xét nghiệm
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "test_org_id", nullable = true)
	private Organization testOrganization;

//	@Convert(converter = UUIDAttributeConverter.class)
//	@Column(name = "uuid", unique = true, nullable = true, updatable = false, columnDefinition = "char(36)")
//	private UUID uuid;

//	@Column(name = "is_return", nullable = true)
//	private boolean isReturn;
	
	@Column(name = "seq_by_organization", nullable = false)
	private Integer seqByOrganization;
	//giấy tờ
	@OneToMany(mappedBy = "snsCase", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private Set<SNSCaseIdNumber> snsCaseIdNumbers = new LinkedHashSet<>();
	
	//Ghi chú
	@Column(name = "note", length = 255, nullable = true)
	private String note;

	//Số thứ tự thẻ mời
	@Column(name = "orderCoupon", length = 255, nullable = true)
	private String orderCoupon;
	
	public SNSCase() {
		super();
//		uuid = UUID.randomUUID();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCouponCode() {
		return couponCode;
	}

	public void setCouponCode(String couponCode) {
		this.couponCode = couponCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LocalDateTime getDob() {
		return dob;
	}

	public void setDob(LocalDateTime birthOfDate) {
		this.dob = birthOfDate;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public SNSRiskGroup getRiskGroup() {
		return riskGroup;
	}

	public void setRiskGroup(SNSRiskGroup riskGroup) {
		this.riskGroup = riskGroup;
	}

	public String getIdNumber() {
		return idNumber;
	}

	public void setIdNumber(String idNumber) {
		this.idNumber = idNumber;
	}

	public SNSIdNumberType getIdNumberType() {
		return idNumberType;
	}

	public void setIdNumberType(SNSIdNumberType idNumberType) {
		this.idNumberType = idNumberType;
	}

	public HIVStatus getHivStatus() {
		return hivStatus;
	}

	public void setHivStatus(HIVStatus hivStatus) {
		this.hivStatus = hivStatus;
	}

	public LocalDateTime getTestDate() {
		return testDate;
	}

	public void setTestDate(LocalDateTime testDate) {
		this.testDate = testDate;
	}

	public SNSCustomerSource getCustomerSource() {
		return customerSource;
	}

	public void setCustomerSource(SNSCustomerSource customerSource) {
		this.customerSource = customerSource;
	}

	public SNSApproachMethod getApproachMethod() {
		return approachMethod;
	}

	public void setApproachMethod(SNSApproachMethod approachMethod) {
		this.approachMethod = approachMethod;
	}

	public LocalDateTime getIssueDate() {
		return issueDate;
	}

	public void setIssueDate(LocalDateTime issueDate) {
		this.issueDate = issueDate;
	}

	public LocalDateTime getFirstTimeVisit() {
		return firstTimeVisit;
	}

	public void setFirstTimeVisit(LocalDateTime firstTimeVisit) {
		this.firstTimeVisit = firstTimeVisit;
	}

	public Integer getTotalCoupon() {
		return totalCoupon;
	}

	public void setTotalCoupon(Integer totalCoupon) {
		this.totalCoupon = totalCoupon;
	}

	public LocalDateTime getPrepDate() {
		return prepDate;
	}

	public void setPrepDate(LocalDateTime prepDate) {
		this.prepDate = prepDate;
	}

	public LocalDateTime getArvDate() {
		return arvDate;
	}

	public void setArvDate(LocalDateTime arvDate) {
		this.arvDate = arvDate;
	}

	public SNSCase getParent() {
		return parent;
	}

	public void setParent(SNSCase parent) {
		this.parent = parent;
	}

	public Set<SNSCase> getChildren() {
		return children;
	}

	public void setChildren(Set<SNSCase> children) {
		this.children = children;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public Organization getTestOrganization() {
		return testOrganization;
	}

	public void setTestOrganization(Organization testOrganization) {
		this.testOrganization = testOrganization;
	}

	public Integer getSeqByOrganization() {
		return seqByOrganization;
	}

	public void setSeqByOrganization(Integer seqByOrganization) {
		this.seqByOrganization = seqByOrganization;
	}

	public Set<SNSCaseIdNumber> getSnsCaseIdNumbers() {
		return snsCaseIdNumbers;
	}

	public void setSnsCaseIdNumbers(Set<SNSCaseIdNumber> snsCaseIdNumbers) {
		this.snsCaseIdNumbers = snsCaseIdNumbers;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getOrderCoupon() {
		return orderCoupon;
	}

	public void setOrderCoupon(String orderCoupon) {
		this.orderCoupon = orderCoupon;
	}
}
