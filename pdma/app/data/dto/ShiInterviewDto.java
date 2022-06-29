package org.pepfar.pdma.app.data.dto;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.pepfar.pdma.app.data.domain.Dictionary;
import org.pepfar.pdma.app.data.domain.ShiInterview;
import org.pepfar.pdma.app.utils.CustomLocalDateTimeDeserializer2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class ShiInterviewDto extends AuditableEntityDto {

	private Long id;

	private OrganizationDto organization;

	private CaseDto theCase;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime interviewDate;

	private Integer residentStatus;

	private Integer occupation;

	private String occupationName;

	private Integer monthlyIncome;

	private Integer wealthStatus;

	private String wealthStatusName;

	private Boolean hasShiCard;

	private String shiCardNumber;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime shiExpiryDate;

	private OrganizationDto primaryCareFacility;

	private String primaryCareFacilityName;

	private Set<DictionaryDto> noShiReasons = new HashSet<>(); // Ly do khong co the bao hiem y te

	private String otherNoShiReason;

	private Boolean wantShiForArv;

	private Boolean usedShiForArv;

	private Set<DictionaryDto> usedShiServices = new HashSet<>(); // Cac dich vu da su dung the bao hiem y te

	private String otherUsedShiService;

	private Integer shiRoute; // The co dung tuyen khong?

	private Integer shiForArvPref; // Nhu cau muon dieu tri ARV bang the BHYT

	private OrganizationDto continuingFacility;

	private String continuingFacilityName; // Co so muon tiep tuc dieu tri ARV bang BHYT

	private Integer arvTreatmentPref; // Hinh thuc muon dieu tri ARV khi khong dung BHYT

	private String arvTreatmentPrefName;

	private Boolean buyShiNextQuarter;

	private Boolean needSupportForShi;

	private String needSupportDetails;

	public ShiInterviewDto() {
	}

	public ShiInterviewDto(ShiInterview entity) {
		super(entity);

		if (entity == null) {
			return;
		}

		this.id = entity.getId();
		this.interviewDate = entity.getInterviewDate();
		this.hasShiCard = entity.getHasShiCard();
		this.shiCardNumber = entity.getShiCardNumber();
		this.shiExpiryDate = entity.getShiExpiryDate();
		this.primaryCareFacilityName = entity.getPrimaryCareFacilityName();
		this.wantShiForArv = entity.getWantShiForArv();
		this.usedShiForArv = entity.getUsedShiForArv();
		this.shiRoute = entity.getShiRoute();
		this.shiForArvPref = entity.getShiForArvPref();
		this.continuingFacilityName = entity.getContinuingFacilityName();
		this.arvTreatmentPref = entity.getArvTreatmentPref();
		this.buyShiNextQuarter = entity.getBuyShiNextQuarter();
		this.needSupportForShi = entity.getNeedSupportForShi();
		this.needSupportDetails = entity.getNeedSupportDetails();

		this.otherNoShiReason = entity.getOtherNoShiReason();
		this.otherUsedShiService = entity.getOtherUsedShiService();

		this.residentStatus = entity.getResidentStatus();
		this.occupation = entity.getOccupation();
		this.occupationName = entity.getOccupationName();
		this.monthlyIncome = entity.getMonthlyIncome();
		this.wealthStatus = entity.getWealthStatus();
		this.wealthStatusName = entity.getWealthStatusName();

		if (entity.getOrganization() != null) {
			this.organization = new OrganizationDto();
			this.organization.setId(entity.getOrganization().getId());
			this.organization.setName(entity.getOrganization().getName());
		}

		if (entity.getTheCase() != null) {
			this.theCase = new CaseDto();
			this.theCase.setId(entity.getTheCase().getId());
		}

		if (entity.getNoShiReasons() != null) {
			entity.getNoShiReasons().parallelStream().forEach(d -> {
				this.noShiReasons.add(new DictionaryDto(d));
			});
		}

		if (entity.getUsedShiServices() != null) {
			entity.getUsedShiServices().parallelStream().forEach(d -> {
				this.usedShiServices.add(new DictionaryDto(d));
			});
		}

		if (entity.getPrimaryCareFacility() != null) {
			this.primaryCareFacility = new OrganizationDto(entity.getPrimaryCareFacility());
		}

		if (entity.getContinuingFacility() != null) {
			this.continuingFacility = new OrganizationDto(entity.getContinuingFacility());
		}
	}

	public ShiInterview toEntity() {
		ShiInterview entity = new ShiInterview();

		entity.setId(id);
		entity.setInterviewDate(interviewDate);
		entity.setHasShiCard(hasShiCard);
		entity.setShiCardNumber(shiCardNumber);
		entity.setShiExpiryDate(shiExpiryDate);
		entity.setPrimaryCareFacilityName(primaryCareFacilityName);
		entity.setWantShiForArv(wantShiForArv);
		entity.setUsedShiForArv(usedShiForArv);
		entity.setShiRoute(shiRoute);
		entity.setShiForArvPref(shiForArvPref);
		entity.setContinuingFacilityName(continuingFacilityName);
		entity.setArvTreatmentPref(arvTreatmentPref);
		entity.setBuyShiNextQuarter(buyShiNextQuarter);
		entity.setNeedSupportForShi(needSupportForShi);
		entity.setNeedSupportDetails(needSupportDetails);

		entity.setOtherNoShiReason(otherNoShiReason);
		entity.setOtherUsedShiService(otherUsedShiService);

		entity.setResidentStatus(residentStatus);
		entity.setOccupation(occupation);
		entity.setOccupationName(occupationName);
		entity.setMonthlyIncome(monthlyIncome);
		entity.setWealthStatus(wealthStatus);
		entity.setWealthStatusName(wealthStatusName);

		if (organization != null) {
			entity.setOrganization(organization.toEntity());
		}

		if (theCase != null) {
			entity.setTheCase(theCase.toEntity());
		}

		if (noShiReasons != null) {
			Set<Dictionary> _reasons = new HashSet<>();

			noShiReasons.parallelStream().forEach(dto -> {
				_reasons.add(dto.toEntity());
			});

			entity.getNoShiReasons().addAll(_reasons);
		}

		if (usedShiServices != null) {
			Set<Dictionary> _services = new HashSet<>();

			usedShiServices.parallelStream().forEach(dto -> {
				_services.add(dto.toEntity());
			});

			entity.getUsedShiServices().addAll(_services);
		}

		if (primaryCareFacility != null) {
			entity.setPrimaryCareFacility(primaryCareFacility.toEntity());
		}

		if (continuingFacility != null) {
			entity.setContinuingFacility(continuingFacility.toEntity());
		}

		return entity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public OrganizationDto getOrganization() {
		return organization;
	}

	public void setOrganization(OrganizationDto organization) {
		this.organization = organization;
	}

	public CaseDto getTheCase() {
		return theCase;
	}

	public void setTheCase(CaseDto theCase) {
		this.theCase = theCase;
	}

	public LocalDateTime getInterviewDate() {
		return interviewDate;
	}

	public void setInterviewDate(LocalDateTime interviewDate) {
		this.interviewDate = interviewDate;
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

	public Set<DictionaryDto> getNoShiReasons() {

		if (noShiReasons == null) {
			noShiReasons = new HashSet<>();
		}

		return noShiReasons;
	}

	public void setNoShiReasons(Set<DictionaryDto> noShiReasons) {
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

	public Set<DictionaryDto> getUsedShiServices() {

		if (usedShiServices == null) {
			usedShiServices = new HashSet<>();
		}

		return usedShiServices;
	}

	public void setUsedShiServices(Set<DictionaryDto> usedShiServices) {
		this.usedShiServices = usedShiServices;
	}

	public Integer getResidentStatus() {
		return residentStatus;
	}

	@JsonIgnore
	public String getResidentStatusName() {
		if (residentStatus == null) {
			return "-";
		}

		String ret = "";
		switch (residentStatus) {
			case 1:
				ret = "Thường trú có hộ khẩu";
				break;
			case 2:
				ret = "Tạm trú có đăng ký (KT3, KT2)";
				break;
			case 3:
				ret = "Tạm trú không đăng ký";
				break;
		}

		return ret;
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

	@JsonIgnore
	public String getMonthlyIncomeName() {
		if (monthlyIncome == null) {
			return "-";
		}

		String ret = "";

		switch (monthlyIncome) {
			case 1:
				ret = "<500.000 đồng";
				break;
			case 2:
				ret = "500.000 - 1 triệu đồng";
				break;
			case 3:
				ret = ">1 - 2 triệu đồng";
				break;
			case 4:
				ret = ">2 - 4 triệu đồng";
				break;
			case 5:
				ret = ">4 - 10 triệu đồng";
				break;
			case 6:
				ret = ">10 triệu đồng";
				break;
		}

		return ret;
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

	public Integer getShiRoute() {
		return shiRoute;
	}

	@JsonIgnore
	public String getShiRouteName() {
		if (shiRoute == null) {
			return "-";
		}

		String ret = "";
		switch (shiRoute) {
			case 1:
				ret = "Đúng tuyến";
				break;
			case 2:
				ret = "Không đúng tuyến (nội tỉnh)";
				break;
			case 3:
				ret = "Không đúng tuyến (ngoại tỉnh)";
				break;
		}

		return ret;
	}

	public void setShiRoute(Integer shiRoute) {
		this.shiRoute = shiRoute;
	}

	public Integer getShiForArvPref() {
		return shiForArvPref;
	}

	@JsonIgnore
	public String getShiForArvPrefName() {
		if (shiForArvPref == null) {
			return "-";
		}

		String ret = "";
		switch (shiForArvPref) {
			case 1:
				ret = "Tiếp tục điều trị tại cơ sở hiện tại";
				break;
			case 2:
				ret = "Về đúng tuyến";
				break;
			case 3:
				ret = "Về đúng tỉnh";
				break;
		}

		return ret;
	}

	public void setShiForArvPref(Integer shiForArvPref) {
		this.shiForArvPref = shiForArvPref;
	}

	public OrganizationDto getPrimaryCareFacility() {
		return primaryCareFacility;
	}

	public void setPrimaryCareFacility(OrganizationDto primaryCareFacility) {
		this.primaryCareFacility = primaryCareFacility;
	}

	public String getPrimaryCareFacilityName() {
		return primaryCareFacilityName;
	}

	public void setPrimaryCareFacilityName(String primaryCareFacilityName) {
		this.primaryCareFacilityName = primaryCareFacilityName;
	}

	public OrganizationDto getContinuingFacility() {
		return continuingFacility;
	}

	public void setContinuingFacility(OrganizationDto continuingFacility) {
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

	public String getArvTreatmentPrefName() {
		return arvTreatmentPrefName;
	}

	public void setArvTreatmentPrefName(String arvTreatmentPrefName) {
		this.arvTreatmentPrefName = arvTreatmentPrefName;
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

	/**
	 * To capture all data update instances for this patient
	 * 
	 * @author bizic
	 *
	 */
	public static class DataCaptureInstance {
		private Long id;

		private LocalDateTime interviewDate;

		private Long patientId;

		private boolean hasShiCard;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public LocalDateTime getInterviewDate() {
			return interviewDate;
		}

		public void setInterviewDate(LocalDateTime interviewDate) {
			this.interviewDate = interviewDate;
		}

		public Long getPatientId() {
			return patientId;
		}

		public void setPatientId(Long patientId) {
			this.patientId = patientId;
		}

		public boolean isHasShiCard() {
			return hasShiCard;
		}

		public void setHasShiCard(boolean hasShiCard) {
			this.hasShiCard = hasShiCard;
		}

	}

}
