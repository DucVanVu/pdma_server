package org.pepfar.pdma.app.data.dto;

import java.time.LocalDateTime;

import org.pepfar.pdma.app.data.domain.PNSAE;
import org.pepfar.pdma.app.data.types.FacilityType;
import org.pepfar.pdma.app.data.types.Gender;
import org.pepfar.pdma.app.utils.CustomLocalDateTimeDeserializer2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class PNSAEDto extends AuditableEntityDto {

	private Long id;

	private OrganizationDto facility;

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
	private int eventType;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime reportDate;

	/**
	 * Người có HIV bị tổn thương? 1. Có 2. Không
	 */
	private int hurt;

	/**
	 * TBXNBT/BC là nguyên nhân trực tiếp? "1. Có 2. Có thể 3. Không"
	 * 
	 */
	private int pnsRootCause;

	private int age;

	/**
	 * Chỉ chọn 1: "1. Nam 2. Nữ 3. Khác"
	 * 
	 */
	private Gender gender;

	/**
	 * Chỉ chọn 1: Phân nhóm người có HIV "1. Khách hàng TVXNHIV 2. Bệnh nhân ARV 3.
	 * NCH trong cộng đồng 4. Khác"
	 * 
	 */
	private int group;

	/**
	 * Có hành động ngay khi tình huống xảy ra? "1. Có 2. Không"
	 * 
	 */
	private int actionTaken;

	/**
	 * Điều tra tình huống? "1. Có 2. Không"
	 * 
	 */
	private int investigated;

	/**
	 * Ngày điều tra
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime investigateDate;

	/**
	 * Thảo luận tìm giải pháp? "1. Có 2. Không"
	 * 
	 */
	private int discussed;

	/**
	 * Tình huống được xử trí? "1. Có 2. Không"
	 * 
	 */
	private int addressed;

	/**
	 * Ngày xử trí
	 * 
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime addressDate;

	private boolean submitted;

	public PNSAEDto() {

	}

	public PNSAEDto(PNSAE entity) {
		super(entity);

		if (entity == null) {
			return;
		}

		this.id = entity.getId();

		if (entity.getFacility() != null) {
			this.facility = new OrganizationDto();
			this.facility.setId(entity.getFacility().getId());
			this.facility.setName(entity.getFacility().getName());
		}

		this.facilityType = entity.getFacilityType();
		this.eventType = entity.getEventType();
		this.reportDate = entity.getReportDate();
		this.hurt = entity.getHurt();
		this.pnsRootCause = entity.getPnsRootCause();
		this.age = entity.getAge();
		this.gender = entity.getGender();
		this.group = entity.getGroup();
		this.actionTaken = entity.getActionTaken();
		this.investigated = entity.getInvestigated();
		this.investigateDate = entity.getInvestigateDate();
		this.discussed = entity.getDiscussed();
		this.addressed = entity.getAddressed();
		this.addressDate = entity.getAddressDate();
		this.submitted = entity.isSubmitted();
	}

	public PNSAE toEntity() {
		PNSAE entity = new PNSAE();
		entity = (PNSAE) super.toEntity(entity);

		entity.setId(id);
		entity.setFacilityType(facilityType);
		entity.setEventType(eventType);
		entity.setReportDate(reportDate);
		entity.setHurt(hurt);
		entity.setPnsRootCause(pnsRootCause);
		entity.setAge(age);
		entity.setGender(gender);
		entity.setGroup(group);
		entity.setActionTaken(actionTaken);
		entity.setInvestigated(investigated);
		entity.setInvestigateDate(investigateDate);
		entity.setDiscussed(discussed);
		entity.setAddressed(addressed);
		entity.setAddressDate(addressDate);
		entity.setSubmitted(submitted);

		if (facility != null) {
			entity.setFacility(facility.toEntity());
		}

		return entity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public OrganizationDto getFacility() {
		return facility;
	}

	public void setFacility(OrganizationDto facility) {
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
