package org.pepfar.pdma.app.data.domain;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.pepfar.pdma.app.data.domain.auditing.AuditableEntity;
import org.pepfar.pdma.app.utils.LocalDateTimeAttributeConverter;
import org.pepfar.pdma.app.utils.UUIDAttributeConverter;

@Entity
@Table(name = "tbl_shi_interview")
public class ShiInterview extends AuditableEntity {

	@Transient
	private static final long serialVersionUID = -553913924263739380L;

	@Id
	@GenericGenerator(name = "native", strategy = "native")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "native")
	@Column(name = "id", unique = true, nullable = false)
	private Long id;
	
	@Convert(converter = UUIDAttributeConverter.class)
	@Column(name = "uuid", unique = true, nullable = false, updatable = false, columnDefinition = "char(36)")
	private UUID uid;

	@ManyToOne
	@JoinColumn(name = "org_id", nullable = false)
	private Organization organization;

	@ManyToOne
	@JoinColumn(name = "case_id", nullable = false)
	private Case theCase;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "interview_date", nullable = false)
	private LocalDateTime interviewDate;

	//@formatter:off
	/**
	 * 1 = Thường trú có hộ khẩu
	 * 2 = Tạm trú có đăng ký (KT3, KT2)
	 * 3 = Tạm trú không đăng ký
	 */
	//@formatter:on
	@Column(name = "resident_status", nullable = true)
	private Integer residentStatus;

	//@formatter:off
	/**
	 * 1 = Dưới 6 tuổi
	 * 2 = Học sinh/sinh viên
	 * 3 = Nghỉ học
	 * 4 = Lực lượng vũ trang
	 * 5 = Công nhân viên chức
	 * 6 = Đi làm công ty có hợp đồng
	 * 7 = Làm nghề tự do
	 * 8 = Thất nghiệp
	 * 9 = Nghề khác
	 */
	//@formatter:on
	@Column(name = "occupation", nullable = true)
	private Integer occupation;

	@Column(name = "occupation_name", nullable = true)
	private String occupationName;

	//@formatter:off
	/**
	 * 1 = <500.000 đồng
	 * 2 = 500.000 - 1 triệu đồng
	 * 3 = >1 - 2 triệu đồng
	 * 4 = >2 - 4 triệu đồng
	 * 5 = >4 - 10 triệu đồng
	 * 6 = >10 triệu đồng
	 */
	//@formatter:on
	@Column(name = "monthly_income", nullable = true)
	private Integer monthlyIncome;

	//@formatter:off
	/**
	 * 1 = Hộ nghèo
	 * 2 = Cận nghèo
	 * 3 = Khó khăn được địa phương hỗ trợ
	 * 4 = Tình trạng khác
	 */
	//@formatter:on
	@Column(name = "wealth_status", nullable = true)
	private Integer wealthStatus;

	@Column(name = "wealth_status_name", nullable = true)
	private String wealthStatusName;

	@Column(name = "has_shi_card", nullable = true)
	private Boolean hasShiCard;

	@Column(name = "shi_card_number", length = 15, nullable = true)
	private String shiCardNumber;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "shi_expiry_date", nullable = true)
	private LocalDateTime shiExpiryDate;

	@ManyToOne
	@JoinColumn(name = "primary_care_facility", nullable = true)
	private Organization primaryCareFacility;

	@Column(name = "prim_care_facility_name", length = 200, nullable = true)
	private String primaryCareFacilityName;

	@ManyToMany
	@JoinTable(
			name = "tbl_shiinterview_noshireasons",
			joinColumns = @JoinColumn(name = "interview_id", referencedColumnName = "id"),
			inverseJoinColumns = @JoinColumn(name = "reason_id", referencedColumnName = "id"))
	private Set<Dictionary> noShiReasons = new HashSet<>(); // Ly do khong co the bao hiem y te

	@Column(name = "other_noshireason", nullable = true)
	private String otherNoShiReason;

	@Column(name = "want_shi_for_arv", nullable = true)
	private Boolean wantShiForArv;

	@Column(name = "used_shi_for_arv", nullable = true)
	private Boolean usedShiForArv;

	@ManyToMany
	@JoinTable(
			name = "tbl_shiinterview_usedservices",
			joinColumns = @JoinColumn(name = "interview_id", referencedColumnName = "id"),
			inverseJoinColumns = @JoinColumn(name = "service_id", referencedColumnName = "id"))
	private Set<Dictionary> usedShiServices = new HashSet<>(); // Cac dich vu da su dung the bao hiem y te

	@Column(name = "other_shi_usedservice", nullable = true)
	private String otherUsedShiService;

	//@formatter:off
	/**
	 * The co dung tuyen khong? 
	 * 
	 * 1 = Dung tuyen 
	 * 2 = Khong dung tuyen (noi tinh) 
	 * 3 = Khong dung tuyen (ngoai tinh)
	 */
	//@formatter:on
	@Column(name = "shi_route", nullable = true)
	private Integer shiRoute;

	//@formatter:off
	/**
	 * Nhu cau muon dieu tri ARV bang the BHYT
	 * 
	 * 1 = Tiep tuc dieu trị tai co so hien tai 
	 * 2 = Ve dung tuyen 
	 * 3 = Ve dung tinh
	 */
	//@formatter:on
	@Column(name = "shi_for_arv_prefs", nullable = true)
	private Integer shiForArvPref;

	@ManyToOne
	@JoinColumn(name = "continuing_facility", nullable = true)
	private Organization continuingFacility;

	@Column(name = "continuing_facility_name", length = 200, nullable = true)
	private String continuingFacilityName; // Co so muon tiep tuc dieu tri ARV bang BHYT

	// @formatter:off
	/**
	 * Hinh thuc muon dieu tri ARV khi khong dung BHYT
	 * 1 = Điều trị ARV tự túc tại cơ sở công
	 * 2 = Điều trị ARV tự túc tại cơ sở tư
	 * 3 = Tự tìm phương án điều trị ARV
	 * 4 = Hình thức khác
	 */
	//@formatter:on
	@Column(name = "arv_treatment_prefs", nullable = true)
	private Integer arvTreatmentPref;

	@Column(name = "arv_treatment_prefs_name", nullable = true)
	private String arvTreatmentPrefName;

	@Column(name = "buy_shi_next_quarter", nullable = true)
	private Boolean buyShiNextQuarter;

	@Column(name = "need_support_for_shi", nullable = true)
	private Boolean needSupportForShi;

	@Column(name = "need_support_details", length = 200, nullable = true)
	private String needSupportDetails;

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

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public Case getTheCase() {
		return theCase;
	}

	public void setTheCase(Case theCase) {
		this.theCase = theCase;
	}

	public LocalDateTime getInterviewDate() {
		return interviewDate;
	}

	public void setInterviewDate(LocalDateTime interviewDate) {
		this.interviewDate = interviewDate;
	}

	public Integer getResidentStatus() {
		return residentStatus;
	}

	public void setResidentStatus(Integer residentStatus) {
		this.residentStatus = residentStatus;
	}

	public Integer getOccupation() {
		return occupation;
	}

	public void setOccupation(Integer occupation) {
		this.occupation = occupation;
	}

	public Integer getMonthlyIncome() {
		return monthlyIncome;
	}

	public void setMonthlyIncome(Integer monthlyIncome) {
		this.monthlyIncome = monthlyIncome;
	}

	public Integer getWealthStatus() {
		return wealthStatus;
	}

	public void setWealthStatus(Integer wealthStatus) {
		this.wealthStatus = wealthStatus;
	}

	public String getOtherNoShiReason() {
		return otherNoShiReason;
	}

	public void setOtherNoShiReason(String otherNoShiReason) {
		this.otherNoShiReason = otherNoShiReason;
	}

	public String getOtherUsedShiService() {
		return otherUsedShiService;
	}

	public void setOtherUsedShiService(String otherUsedShiService) {
		this.otherUsedShiService = otherUsedShiService;
	}

	public String getOccupationName() {
		return occupationName;
	}

	public void setOccupationName(String occupationName) {
		this.occupationName = occupationName;
	}

	public String getWealthStatusName() {
		return wealthStatusName;
	}

	public void setWealthStatusName(String wealthStatusName) {
		this.wealthStatusName = wealthStatusName;
	}

	public Boolean getHasShiCard() {
		return hasShiCard;
	}

	public void setHasShiCard(Boolean hasShiCard) {
		this.hasShiCard = hasShiCard;
	}

	public String getShiCardNumber() {
		return shiCardNumber;
	}

	public void setShiCardNumber(String shiCardNumber) {
		this.shiCardNumber = shiCardNumber;
	}

	public LocalDateTime getShiExpiryDate() {
		return shiExpiryDate;
	}

	public void setShiExpiryDate(LocalDateTime shiExpiryDate) {
		this.shiExpiryDate = shiExpiryDate;
	}

	public Set<Dictionary> getNoShiReasons() {

		if (noShiReasons == null) {
			noShiReasons = new HashSet<>();
		}

		return noShiReasons;
	}

	public void setNoShiReasons(Set<Dictionary> noShiReasons) {
		this.noShiReasons = noShiReasons;
	}

	public Boolean getWantShiForArv() {
		return wantShiForArv;
	}

	public void setWantShiForArv(Boolean wantShiForArv) {
		this.wantShiForArv = wantShiForArv;
	}

	public Boolean getUsedShiForArv() {
		return usedShiForArv;
	}

	public void setUsedShiForArv(Boolean usedShiForArv) {
		this.usedShiForArv = usedShiForArv;
	}

	public Set<Dictionary> getUsedShiServices() {

		if (usedShiServices == null) {
			usedShiServices = new HashSet<>();
		}

		return usedShiServices;
	}

	public void setUsedShiServices(Set<Dictionary> usedShiServices) {
		this.usedShiServices = usedShiServices;
	}

	public Integer getShiRoute() {
		return shiRoute;
	}

	public void setShiRoute(Integer shiRoute) {
		this.shiRoute = shiRoute;
	}

	public String getArvTreatmentPrefName() {
		return arvTreatmentPrefName;
	}

	public void setArvTreatmentPrefName(String arvTreatmentPrefName) {
		this.arvTreatmentPrefName = arvTreatmentPrefName;
	}

	public Integer getShiForArvPref() {
		return shiForArvPref;
	}

	public void setShiForArvPref(Integer shiForArvPref) {
		this.shiForArvPref = shiForArvPref;
	}

	public Organization getPrimaryCareFacility() {
		return primaryCareFacility;
	}

	public void setPrimaryCareFacility(Organization primaryCareFacility) {
		this.primaryCareFacility = primaryCareFacility;
	}

	public String getPrimaryCareFacilityName() {
		return primaryCareFacilityName;
	}

	public void setPrimaryCareFacilityName(String primaryCareFacilityName) {
		this.primaryCareFacilityName = primaryCareFacilityName;
	}

	public Organization getContinuingFacility() {
		return continuingFacility;
	}

	public void setContinuingFacility(Organization continuingFacility) {
		this.continuingFacility = continuingFacility;
	}

	public String getContinuingFacilityName() {
		return continuingFacilityName;
	}

	public void setContinuingFacilityName(String continuingFacilityName) {
		this.continuingFacilityName = continuingFacilityName;
	}

	public Integer getArvTreatmentPref() {
		return arvTreatmentPref;
	}

	public void setArvTreatmentPref(Integer arvTreatmentPref) {
		this.arvTreatmentPref = arvTreatmentPref;
	}

	public Boolean getBuyShiNextQuarter() {
		return buyShiNextQuarter;
	}

	public void setBuyShiNextQuarter(Boolean buyShiNextQuarter) {
		this.buyShiNextQuarter = buyShiNextQuarter;
	}

	public Boolean getNeedSupportForShi() {
		return needSupportForShi;
	}

	public void setNeedSupportForShi(Boolean needSupportForShi) {
		this.needSupportForShi = needSupportForShi;
	}

	public String getNeedSupportDetails() {
		return needSupportDetails;
	}

	public void setNeedSupportDetails(String needSupportDetails) {
		this.needSupportDetails = needSupportDetails;
	}

}
