package org.pepfar.pdma.app.data.dto;

import static org.mockito.Matchers.endsWith;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.pepfar.pdma.app.data.domain.SNSCase;
import org.pepfar.pdma.app.data.domain.SNSCaseIdNumber;
import org.pepfar.pdma.app.data.types.Gender;
import org.pepfar.pdma.app.data.types.HIVStatus;
import org.pepfar.pdma.app.data.types.SNSApproachMethod;
import org.pepfar.pdma.app.data.types.SNSCustomerSource;
import org.pepfar.pdma.app.data.types.SNSIdNumberType;
import org.pepfar.pdma.app.data.types.SNSRiskGroup;
import org.pepfar.pdma.app.utils.CustomLocalDateTimeDeserializer2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class SNSCaseDto extends AuditableEntityDto {

	private Long id;

	private String couponCode;

	private String name;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime dob;

	private Integer yearOfBirth;

	private Gender gender;

	private SNSRiskGroup riskGroup;

	// Mã định danh
	private String idNumber;
	// Loại giấy tờ: CMND/CCCD/số thẻ BH/Bằng LX/Số ĐT/Số hộ khẩu
	private SNSIdNumberType idNumberType;
	// Tình trạng HIV
	private HIVStatus hivStatus;
	
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime testDate;

	// Nguồn khách hàng
	private SNSCustomerSource customerSource;

	// Hình thức tiếp cận
	private SNSApproachMethod approachMethod;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime issueDate;
	
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime firstTimeVisit;
	
	private Integer totalCoupon;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime prepDate;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime arvDate;

	private SNSCaseDto parent;

	private List<SNSCaseDto> children = new ArrayList<>();

	private OrganizationDto organization;

	private OrganizationDto testOrganization;

	private boolean isReturn;

	private Integer seqByOrganization;

	private List<SNSCaseIdNumberDto> snsCaseIdNumers = new ArrayList<>();

	private String orderCoupon;

	private String note;
	
	private boolean isReadAble;
	
	private boolean isWritAble;
	
	private boolean isDeleteAble;
	
	public SNSCaseDto() {

	}

	public SNSCaseDto(SNSCase entity, int isViewPII, boolean isReadAble,
			boolean isWritAble,
			boolean isDeleteAble) {
		super(entity);
		if (entity != null) {
			this.isDeleteAble = isDeleteAble;
			this.isWritAble = isWritAble;
			this.isReadAble = isReadAble;
			if(!isReadAble) {
				return;
			}
			this.id = entity.getId();
			this.approachMethod = entity.getApproachMethod();
			this.arvDate = entity.getArvDate();
			this.dob = entity.getDob();
			if (entity.getDob() != null) {
				this.yearOfBirth = entity.getDob().getYear();
			}
			this.couponCode = entity.getCouponCode();
			this.customerSource = entity.getCustomerSource();
			this.gender = entity.getGender();
			this.hivStatus = entity.getHivStatus();
			this.idNumberType = entity.getIdNumberType();
			this.issueDate = entity.getIssueDate();
			this.prepDate = entity.getPrepDate();
			this.riskGroup = entity.getRiskGroup();
			this.totalCoupon = entity.getTotalCoupon();
			this.seqByOrganization = entity.getSeqByOrganization();
			this.idNumber = entity.getIdNumber();
			this.name = entity.getName();
			if(isViewPII==0) {
				this.name = "-";
				this.idNumber = "-";
			}
			this.note = entity.getNote();
			this.firstTimeVisit = entity.getFirstTimeVisit();
			this.testDate = entity.getTestDate();
			if (entity.getOrganization() != null) {
				this.organization = new OrganizationDto(entity.getOrganization());
			}
			if (entity.getTestOrganization() != null) {
				this.testOrganization = new OrganizationDto(entity.getTestOrganization(), true);
			}
			if (entity.getParent() != null) {
				this.parent = new SNSCaseDto(entity.getParent(), true, 1);
			}
			if (entity.getSnsCaseIdNumbers() != null && entity.getSnsCaseIdNumbers().size() > 0) {
				this.snsCaseIdNumers = new ArrayList<SNSCaseIdNumberDto>();
				for (SNSCaseIdNumber snsCaseIN : entity.getSnsCaseIdNumbers()) {
					SNSCaseIdNumberDto item = new SNSCaseIdNumberDto(snsCaseIN);
					this.snsCaseIdNumers.add(item);
				}
			}
			this.orderCoupon = entity.getOrderCoupon();
			this.children = getListChild(entity);
			if(entity.getParent()!=null){
				SNSCaseDto parent = new SNSCaseDto();
				parent.setCouponCode(entity.getParent().getCouponCode());
				parent.setId(entity.getParent().getId());
				this.parent = parent;
			}
		}
	}

	public SNSCaseDto(SNSCase entity, boolean collapse, int isViewPII) {
		super(entity);
		if (entity != null) {
			this.id = entity.getId();
			this.approachMethod = entity.getApproachMethod();
			this.arvDate = entity.getArvDate();
			this.dob = entity.getDob();
			if (entity.getDob() != null) {
				this.yearOfBirth = entity.getDob().getYear();
			}
			this.couponCode = entity.getCouponCode();
			this.customerSource = entity.getCustomerSource();
			this.gender = entity.getGender();
			this.hivStatus = entity.getHivStatus();
			this.idNumber = entity.getIdNumber();
			this.idNumberType = entity.getIdNumberType();
			this.issueDate = entity.getIssueDate();
			this.prepDate = entity.getPrepDate();
			this.riskGroup = entity.getRiskGroup();
			this.totalCoupon = entity.getTotalCoupon();
			this.seqByOrganization = entity.getSeqByOrganization();
			this.name = entity.getName();
			if(isViewPII==0) {
				this.name = "-";
				this.idNumber = "-";
			}
			this.orderCoupon = entity.getOrderCoupon();
			this.note = entity.getNote();
			this.firstTimeVisit = entity.getFirstTimeVisit();
			this.testDate = entity.getTestDate();
			if (entity.getTestOrganization() != null) {
				this.testOrganization = new OrganizationDto(entity.getTestOrganization(), true);
				this.testOrganization.setId(entity.getTestOrganization().getId());
				this.testOrganization.setName(entity.getTestOrganization().getName());
				this.testOrganization.setCode(entity.getTestOrganization().getCode());
			}
			if (entity.getOrganization() != null) {
				this.organization = new OrganizationDto();
				this.organization.setId(entity.getOrganization().getId());
				this.organization.setName(entity.getOrganization().getName());
				this.organization.setCode(entity.getOrganization().getCode());
			}
			if (!collapse) {
				if (entity.getParent() != null) {
					this.parent = new SNSCaseDto();
					this.parent.setId(entity.getParent().getId());
					this.parent.setCouponCode(entity.getParent().getCouponCode());
				}
			}
			if(entity.getParent()!=null){
				SNSCaseDto parent = new SNSCaseDto();
				parent.setCouponCode(entity.getParent().getCouponCode());
				parent.setId(entity.getParent().getId());
				this.parent = parent;
			}
		}
	}

	public List<SNSCaseDto> getListChild(SNSCase entity) {
		List<SNSCaseDto> childs = new ArrayList<SNSCaseDto>();
		if (entity.getChildren() != null && entity.getChildren().size() > 0) {
			for (SNSCase child : entity.getChildren()) {
				SNSCaseDto childDto = new SNSCaseDto(child, true, 1);
				childDto.setChildren(getListChild(child));
				childs.add(childDto);
			}
		}
		return childs;
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

	public void setDob(LocalDateTime dob) {
		this.dob = dob;
	}

	public Integer getYearOfBirth() {
		return yearOfBirth;
	}

	public void setYearOfBirth(Integer yearOfBirth) {
		this.yearOfBirth = yearOfBirth;
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

	public SNSCaseDto getParent() {
		return parent;
	}

	public void setParent(SNSCaseDto parent) {
		this.parent = parent;
	}

	public List<SNSCaseDto> getChildren() {
		return children;
	}

	public void setChildren(List<SNSCaseDto> children) {
		this.children = children;
	}

	public OrganizationDto getOrganization() {
		return organization;
	}

	public void setOrganization(OrganizationDto organization) {
		this.organization = organization;
	}

	public OrganizationDto getTestOrganization() {
		return testOrganization;
	}

	public void setTestOrganization(OrganizationDto testOrganization) {
		this.testOrganization = testOrganization;
	}

	public boolean isReturn() {
		return isReturn;
	}

	public void setReturn(boolean isReturn) {
		this.isReturn = isReturn;
	}

	public Integer getSeqByOrganization() {
		return seqByOrganization;
	}

	public void setSeqByOrganization(Integer seqByOrganization) {
		this.seqByOrganization = seqByOrganization;
	}

	public List<SNSCaseIdNumberDto> getSnsCaseIdNumers() {
		return snsCaseIdNumers;
	}

	public void setSnsCaseIdNumers(List<SNSCaseIdNumberDto> snsCaseIdNumers) {
		this.snsCaseIdNumers = snsCaseIdNumers;
	}

	public LocalDateTime getTestDate() {
		return testDate;
	}

	public void setTestDate(LocalDateTime testDate) {
		this.testDate = testDate;
	}

	public LocalDateTime getFirstTimeVisit() {
		return firstTimeVisit;
	}

	public void setFirstTimeVisit(LocalDateTime firstTimeVisit) {
		this.firstTimeVisit = firstTimeVisit;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public boolean isReadAble() {
		return isReadAble;
	}

	public void setReadAble(boolean isReadAble) {
		this.isReadAble = isReadAble;
	}

	public boolean isWritAble() {
		return isWritAble;
	}

	public void setWritAble(boolean isWritAble) {
		this.isWritAble = isWritAble;
	}

	public boolean isDeleteAble() {
		return isDeleteAble;
	}

	public void setDeleteAble(boolean isDeleteAble) {
		this.isDeleteAble = isDeleteAble;
	}

	public String getOrderCoupon() {
		return orderCoupon;
	}

	public void setOrderCoupon(String orderCoupon) {
		this.orderCoupon = orderCoupon;
	}
}
