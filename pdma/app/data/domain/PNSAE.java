package org.pepfar.pdma.app.data.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.pepfar.pdma.app.data.domain.auditing.AuditableEntity;
import org.pepfar.pdma.app.data.types.FacilityType;
import org.pepfar.pdma.app.data.types.Gender;
import org.pepfar.pdma.app.utils.LocalDateTimeAttributeConverter;

@Entity
@Table(name = "tbl_pns_ae")
public class PNSAE extends AuditableEntity {

	@Transient
	private static final long serialVersionUID = 3971072587892089640L;

	@Id
	@GenericGenerator(name = "native", strategy = "native")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "native")
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "facility_id", nullable = false)
	private Organization facility;

	@Column(name = "facility_type", nullable = false)
	@Enumerated(EnumType.STRING)
	private FacilityType facilityType;

	/**
	 * @formatter:off
	 * "1. Đe dọa tổn thương người có HIV, BT/BC và các thành viên gia đình, hoặc
	 * người cung cấp dịch vụ TBXNBT/BC về thể chất, tình dục hoặc kinh tế 
	 * 2. Có sự cố gây tổn thương về thể chất, tình dục hoặc kinh tế của người có HIV, BT/BC
	 * và các thành viên gia đình, hoặc người cung cấp dịch vụ TBXNBT/BC 
	 * 3. Phải dừng điều trị ARV hoặc dừng sử dụng các dịch vụ có liên quan khác 
	 * 4. Người có HIV bị ép buộc tiết lộ thông tin cá nhân của họ hay của BT/BC 
	 * 5. Trẻ em < 15 tuổi bị bỏ rơi hoặc bị đuổi ra khỏi nhà 
	 * 6. Người có HIV không đồng ý tham gia TB BT/BC 
	 * 7. Người có HIV bị kỳ thị tại cơ sở hoặc bị coi như tội phạm
	 * @formatter:on
	 */
	@Column(name = "event_type", nullable = false)
	private int eventType;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "report_date", nullable = false)
	private LocalDateTime reportDate;

	/**
	 * Người có HIV bị tổn thương? 1. Có 2. Không
	 */
	@Column(name = "is_hurt", nullable = true)
	private int hurt;

	/**
	 * TBXNBT/BC là nguyên nhân trực tiếp? "1. Có 2. Có thể 3. Không"
	 * 
	 */
	@Column(name = "is_pns_root_causess", nullable = true)
	private int pnsRootCause;

	@Column(name = "age", nullable = true)
	private int age;

	/**
	 * Chỉ chọn 1: "1. Nam 2. Nữ 3. Khác"
	 * 
	 */
	@Column(name = "gender", nullable = true)
	@Enumerated(EnumType.STRING)
	private Gender gender;

	/**
	 * Chỉ chọn 1: Phân nhóm người có HIV "1. Khách hàng TVXNHIV 2. Bệnh nhân ARV 3.
	 * NCH trong cộng đồng 4. Khác"
	 * 
	 */
	@Column(name = "plhiv_group", nullable = true)
	private int group;

	/**
	 * Có hành động ngay khi tình huống xảy ra? "1. Có 2. Không"
	 * 
	 */
	@Column(name = "action_taken", nullable = true)
	private int actionTaken;

	/**
	 * Điều tra tình huống? "1. Có 2. Không"
	 * 
	 */
	@Column(name = "investigated", nullable = true)
	private int investigated;

	/**
	 * Ngày điều tra
	 */
	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "investigate_date", nullable = true)
	private LocalDateTime investigateDate;

	/**
	 * Thảo luận tìm giải pháp? "1. Có 2. Không"
	 * 
	 */
	@Column(name = "discussed", nullable = true)
	private int discussed;

	/**
	 * Tình huống được xử trí? "1. Có 2. Không"
	 * 
	 */
	@Column(name = "addressed", nullable = true)
	private int addressed;

	/**
	 * Ngày xử trí
	 * 
	 */
	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "address_date", nullable = true)
	private LocalDateTime addressDate;

	@Column(name = "submitted", nullable = true)
	private boolean submitted;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Organization getFacility() {
		return facility;
	}

	public void setFacility(Organization facility) {
		this.facility = facility;
	}

	public FacilityType getFacilityType() {
		return facilityType;
	}

	public void setFacilityType(FacilityType facilityType) {
		this.facilityType = facilityType;
	}

	public int getEventType() {
		return eventType;
	}

	public void setEventType(int eventType) {
		this.eventType = eventType;
	}

	public LocalDateTime getReportDate() {
		return reportDate;
	}

	public void setReportDate(LocalDateTime reportDate) {
		this.reportDate = reportDate;
	}

	public int getHurt() {
		return hurt;
	}

	public void setHurt(int hurt) {
		this.hurt = hurt;
	}

	public int getPnsRootCause() {
		return pnsRootCause;
	}

	public void setPnsRootCause(int pnsRootCause) {
		this.pnsRootCause = pnsRootCause;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public int getGroup() {
		return group;
	}

	public void setGroup(int group) {
		this.group = group;
	}

	public int getActionTaken() {
		return actionTaken;
	}

	public void setActionTaken(int actionTaken) {
		this.actionTaken = actionTaken;
	}

	public int getInvestigated() {
		return investigated;
	}

	public void setInvestigated(int investigated) {
		this.investigated = investigated;
	}

	public LocalDateTime getInvestigateDate() {
		return investigateDate;
	}

	public void setInvestigateDate(LocalDateTime investigateDate) {
		this.investigateDate = investigateDate;
	}

	public int getDiscussed() {
		return discussed;
	}

	public void setDiscussed(int discussed) {
		this.discussed = discussed;
	}

	public int getAddressed() {
		return addressed;
	}

	public void setAddressed(int addressed) {
		this.addressed = addressed;
	}

	public LocalDateTime getAddressDate() {
		return addressDate;
	}

	public void setAddressDate(LocalDateTime addressDate) {
		this.addressDate = addressDate;
	}

	public boolean isSubmitted() {
		return submitted;
	}

	public void setSubmitted(boolean submitted) {
		this.submitted = submitted;
	}

}
