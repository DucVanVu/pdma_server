package org.pepfar.pdma.app.data.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Basic;
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
import org.pepfar.pdma.app.data.types.MaritalStatus;
import org.pepfar.pdma.app.utils.LocalDateTimeAttributeConverter;
import org.pepfar.pdma.app.utils.UUIDAttributeConverter;

@Entity
@Table(name = "tbl_person")
public class Person extends AuditableEntity {

	@Transient
	private static final long serialVersionUID = 6667595228228996480L;

	@Id
	@GenericGenerator(name = "native", strategy = "native")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "native")
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Convert(converter = UUIDAttributeConverter.class)
	@Column(name = "uuid", unique = true, nullable = false, updatable = false, columnDefinition = "char(36)")
	private UUID uid;

//	@Convert(converter = CryptoConverter.class)
	@Column(name = "national_id_number", length = 20, nullable = true)
	private String nidNumber;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "national_id_issue_date", nullable = true)
	private LocalDateTime nidIssuedDate;

	@Column(name = "national_id_issue_by", length = 100, nullable = true)
	private String nidIssuedBy;

	@Column(name = "no_nid_reason", length = 200, nullable = true)
	private String noNidReason;

//	@Convert(converter = CryptoConverter.class)
	@Column(name = "passport_number", length = 20, nullable = true)
	private String passportNumber;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "passport_issue_date", nullable = true)
	private LocalDateTime passportIssuedDate;

	@Column(name = "passport_issue_by", length = 100, nullable = true)
	private String passportIssuedBy;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "passport_expiry_date", nullable = true)
	private LocalDateTime passportExpiryDate;

	@ManyToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "ethnic_id", nullable = true)
	private Dictionary ethnic;

	@ManyToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "religion_id", nullable = true)
	private Dictionary religion;

	@ManyToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "nationality_id", nullable = true)
	private Dictionary nationality;

	@ManyToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "education_id", nullable = true)
	private Dictionary education;

	@Deprecated
	@ManyToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "wealth_status_id", nullable = true)
	private Dictionary wealthStatus;

	@Deprecated
	@ManyToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "monthly_income_id", nullable = true)
	private Dictionary monthlyIncome;

	@Deprecated
	@ManyToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "professional_id", nullable = true)
	private Dictionary professional;

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

	@Column(name = "occupation_name", length = 1024, nullable = true)
	private String occupationName;

//	@Convert(converter = CryptoConverter.class)
	@Column(name = "fullname", length = 255, nullable = false)
	private String fullname;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "dob", nullable = true)
	private LocalDateTime dob;

	@Column(name = "gender", nullable = false)
	@Enumerated(value = EnumType.STRING)
	private Gender gender;

	@Column(name = "marital_status", nullable = true)
	@Enumerated(value = EnumType.STRING)
	private MaritalStatus maritalStatus;

//	@Convert(converter = CryptoConverter.class)
	@Column(name = "mobile_phone", length = 100, nullable = true)
	private String mobilePhone;

//	@Convert(converter = CryptoConverter.class)
	@Column(name = "home_phone", length = 100, nullable = true)
	private String homePhone;

//	@Convert(converter = CryptoConverter.class)
	@Column(name = "email_address", length = 255, nullable = true)
	private String emailAddress;

	@Column(name = "height", nullable = true)
	private Integer height;

	@Column(name = "weight", nullable = true)
	private Integer weight;

	@Column(name = "image", nullable = true, columnDefinition = "LONGBLOB NULL")
	@Basic(fetch = FetchType.LAZY)
	private byte[] image;

	/**
	 * For place of birth, current address and resident address
	 */
	@OneToMany(mappedBy = "person", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private Set<Location> locations = new LinkedHashSet<>();

	@Column(name = "current_address_id", nullable = true)
	private Long currentAddressId;

	@Column(name = "resident_address_id", nullable = true)
	private Long residentAddressId;

	public Person() {
		super();
		this.uid = UUID.randomUUID();
	}
	
	public Long getCurrentAddressId() {
		return currentAddressId;
	}

	public void setCurrentAddressId(Long currentAddressId) {
		this.currentAddressId = currentAddressId;
	}

	public Long getResidentAddressId() {
		return residentAddressId;
	}

	public void setResidentAddressId(Long residentAddressId) {
		this.residentAddressId = residentAddressId;
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

	public String getNidNumber() {
		return nidNumber;
	}

	public void setNidNumber(String nidNumber) {
		this.nidNumber = nidNumber;
	}

	public LocalDateTime getNidIssuedDate() {
		return nidIssuedDate;
	}

	public void setNidIssuedDate(LocalDateTime nidIssuedDate) {
		this.nidIssuedDate = nidIssuedDate;
	}

	public String getNidIssuedBy() {
		return nidIssuedBy;
	}

	public void setNidIssuedBy(String nidIssuedBy) {
		this.nidIssuedBy = nidIssuedBy;
	}

	public String getNoNidReason() {
		return noNidReason;
	}

	public void setNoNidReason(String noNidReason) {
		this.noNidReason = noNidReason;
	}

	public String getPassportNumber() {
		return passportNumber;
	}

	public void setPassportNumber(String passportNumber) {
		this.passportNumber = passportNumber;
	}

	public LocalDateTime getPassportIssuedDate() {
		return passportIssuedDate;
	}

	public void setPassportIssuedDate(LocalDateTime passportIssuedDate) {
		this.passportIssuedDate = passportIssuedDate;
	}

	public String getPassportIssuedBy() {
		return passportIssuedBy;
	}

	public void setPassportIssuedBy(String passportIssuedBy) {
		this.passportIssuedBy = passportIssuedBy;
	}

	public LocalDateTime getPassportExpiryDate() {
		return passportExpiryDate;
	}

	public void setPassportExpiryDate(LocalDateTime passportExpiryDate) {
		this.passportExpiryDate = passportExpiryDate;
	}

	public Dictionary getEthnic() {
		return ethnic;
	}

	public void setEthnic(Dictionary ethnic) {
		this.ethnic = ethnic;
	}

	public Dictionary getReligion() {
		return religion;
	}

	public void setReligion(Dictionary religion) {
		this.religion = religion;
	}

	public Dictionary getNationality() {
		return nationality;
	}

	public void setNationality(Dictionary nationality) {
		this.nationality = nationality;
	}

	public Dictionary getEducation() {
		return education;
	}

	public void setEducation(Dictionary education) {
		this.education = education;
	}

	public Dictionary getWealthStatus() {
		return wealthStatus;
	}

	public void setWealthStatus(Dictionary wealthStatus) {
		this.wealthStatus = wealthStatus;
	}

	public Dictionary getMonthlyIncome() {
		return monthlyIncome;
	}

	public void setMonthlyIncome(Dictionary monthlyIncome) {
		this.monthlyIncome = monthlyIncome;
	}

	public Dictionary getProfessional() {
		return professional;
	}

	public void setProfessional(Dictionary professional) {
		this.professional = professional;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public LocalDateTime getDob() {
		return dob;
	}

	public void setDob(LocalDateTime dob) {
		this.dob = dob;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public MaritalStatus getMaritalStatus() {
		return maritalStatus;
	}

	public void setMaritalStatus(MaritalStatus maritalStatus) {
		this.maritalStatus = maritalStatus;
	}

	public String getMobilePhone() {
		return mobilePhone;
	}

	public void setMobilePhone(String mobilePhone) {
		this.mobilePhone = mobilePhone;
	}

	public String getHomePhone() {
		return homePhone;
	}

	public void setHomePhone(String homePhone) {
		this.homePhone = homePhone;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	public Integer getWeight() {
		return weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	public byte[] getImage() {
		return image;
	}

	public void setImage(byte[] image) {
		this.image = image;
	}

	public Set<Location> getLocations() {
		if (locations == null) {
			locations = new LinkedHashSet<>();
		}

		return locations;
	}

	public void setLocations(Set<Location> locations) {
		this.locations = locations;
	}

	public Integer getOccupation() {
		return occupation;
	}

	public void setOccupation(Integer occupation) {
		this.occupation = occupation;
	}

	public String getOccupationName() {
		return occupationName;
	}

	public void setOccupationName(String occupationName) {
		this.occupationName = occupationName;
	}

}
