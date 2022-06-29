package org.pepfar.pdma.app.data.dto;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import org.pepfar.pdma.app.data.domain.Person;
import org.pepfar.pdma.app.data.types.Gender;
import org.pepfar.pdma.app.data.types.MaritalStatus;
import org.pepfar.pdma.app.utils.CustomLocalDateTimeDeserializer2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class PersonDto {

	private Long id;

	private UUID uid;

	private String nidNumber;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime nidIssuedDate;

	private String nidIssuedBy;

	private String noNidReason;

	private String passportNumber;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime passportIssuedDate;

	private String passportIssuedBy;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime passportExpiryDate;

	private DictionaryDto ethnic;

	private DictionaryDto religion;

	private DictionaryDto nationality;

	private DictionaryDto education;

	private DictionaryDto wealthStatus;

	private DictionaryDto monthlyIncome;

	private DictionaryDto professional;

	private Integer occupation;

	private String occupationName;

	private String fullname;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime dob;

	private Gender gender;

	private MaritalStatus maritalStatus;

	private String mobilePhone;

	private String homePhone;

	private String emailAddress;

	private Integer height;

	private Integer weight;

	private byte[] image;

	private Set<LocationDto> locations = new LinkedHashSet<>();

	public PersonDto() {
	}

	public PersonDto(Person entity, boolean includePhoto) {

		if (entity == null) {
			return;
		}

		this.id = entity.getId();
		this.uid = entity.getUid();
		this.nidNumber = entity.getNidNumber();
		this.nidIssuedDate = entity.getNidIssuedDate();
		this.nidIssuedBy = entity.getNidIssuedBy();
		this.noNidReason = entity.getNoNidReason();
		this.passportNumber = entity.getPassportNumber();
		this.passportIssuedDate = entity.getPassportIssuedDate();
		this.passportIssuedBy = entity.getPassportIssuedBy();
		this.passportExpiryDate = entity.getPassportExpiryDate();

		this.fullname = entity.getFullname();
		this.dob = entity.getDob();
		this.gender = entity.getGender();
		this.maritalStatus = entity.getMaritalStatus();
		this.mobilePhone = entity.getMobilePhone();
		this.homePhone = entity.getHomePhone();
		this.emailAddress = entity.getEmailAddress();
		this.height = entity.getHeight();
		this.weight = entity.getWeight();

		this.occupation = entity.getOccupation();
		this.occupationName = entity.getOccupationName();

		if (includePhoto) {
			this.image = entity.getImage();
		}

		if (entity.getEthnic() != null) {
			this.ethnic = new DictionaryDto(entity.getEthnic());
		}

		if (entity.getReligion() != null) {
			this.religion = new DictionaryDto(entity.getReligion());
		}

		if (entity.getNationality() != null) {
			this.nationality = new DictionaryDto(entity.getNationality());
		}

		if (entity.getEducation() != null) {
			this.education = new DictionaryDto(entity.getEducation());
		}

		if (entity.getWealthStatus() != null) {
			this.wealthStatus = new DictionaryDto(entity.getWealthStatus());
		}

		if (entity.getMonthlyIncome() != null) {
			this.monthlyIncome = new DictionaryDto(entity.getMonthlyIncome());
		}

		if (entity.getProfessional() != null) {
			this.professional = new DictionaryDto(entity.getProfessional());
		}

		if (entity.getLocations() != null) {
			entity.getLocations().forEach(l -> {
				locations.add(new LocationDto(l));
			});
		}
	}

	public Person toEntity() {
		Person entity = new Person();

		entity.setId(id);
		entity.setUid(uid);
		entity.setNidNumber(nidNumber);
		entity.setNidIssuedDate(nidIssuedDate);
		entity.setNidIssuedBy(nidIssuedBy);
		entity.setNoNidReason(noNidReason);
		entity.setPassportNumber(passportNumber);
		entity.setPassportIssuedDate(passportIssuedDate);
		entity.setPassportIssuedBy(passportIssuedBy);
		entity.setPassportExpiryDate(passportExpiryDate);

		entity.setFullname(fullname);
		entity.setDob(dob);
		entity.setGender(gender);
		entity.setMaritalStatus(maritalStatus);
		entity.setMobilePhone(mobilePhone);
		entity.setHomePhone(homePhone);
		entity.setEmailAddress(emailAddress);
		entity.setHeight(height);
		entity.setWeight(weight);
		entity.setImage(image);

		entity.setOccupation(occupation);
		entity.setOccupationName(occupationName);

		if (ethnic != null) {
			entity.setEthnic(ethnic.toEntity());
		}

		if (religion != null) {
			entity.setReligion(religion.toEntity());
		}

		if (nationality != null) {
			entity.setNationality(nationality.toEntity());
		}

		if (education != null) {
			entity.setEducation(education.toEntity());
		}

		if (wealthStatus != null) {
			entity.setWealthStatus(wealthStatus.toEntity());
		}

		if (monthlyIncome != null) {
			entity.setMonthlyIncome(monthlyIncome.toEntity());
		}

		if (professional != null) {
			entity.setProfessional(professional.toEntity());
		}

		return entity;
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

	public DictionaryDto getEthnic() {
		return ethnic;
	}

	public void setEthnic(DictionaryDto ethnic) {
		this.ethnic = ethnic;
	}

	public DictionaryDto getReligion() {
		return religion;
	}

	public void setReligion(DictionaryDto religion) {
		this.religion = religion;
	}

	public DictionaryDto getNationality() {
		return nationality;
	}

	public void setNationality(DictionaryDto nationality) {
		this.nationality = nationality;
	}

	public DictionaryDto getEducation() {
		return education;
	}

	public void setEducation(DictionaryDto education) {
		this.education = education;
	}

	public DictionaryDto getWealthStatus() {
		return wealthStatus;
	}

	public void setWealthStatus(DictionaryDto wealthStatus) {
		this.wealthStatus = wealthStatus;
	}

	public DictionaryDto getMonthlyIncome() {
		return monthlyIncome;
	}

	public void setMonthlyIncome(DictionaryDto monthlyIncome) {
		this.monthlyIncome = monthlyIncome;
	}

	public DictionaryDto getProfessional() {
		return professional;
	}

	public void setProfessional(DictionaryDto professional) {
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

	public Set<LocationDto> getLocations() {
		if (locations == null) {
			locations = new LinkedHashSet<>();
		}

		return locations;
	}

	public void setLocations(Set<LocationDto> locations) {
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
