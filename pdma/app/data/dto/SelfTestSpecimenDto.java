package org.pepfar.pdma.app.data.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.pepfar.pdma.app.data.domain.SelfTestEntry;
import org.pepfar.pdma.app.data.domain.SelfTestSpecimen;
import org.pepfar.pdma.app.data.types.Gender;
import org.pepfar.pdma.app.utils.CustomLocalDateTimeDeserializer2;

import java.time.LocalDateTime;

public class SelfTestSpecimenDto {

	private Long id;

	private String code; // mã sinh phẩm xét nghiệm (vd: ORAQUICK, ...)

	private String name; // tên sinh phẩm xét nghiệm (vd: Bộ xét nghiệm ORAQUICK, ...)

	private String support;

	private String client;

	private Integer clientYob;

	private Gender clientGender;

	private String clientRiskGroup;

	private String screenResult;

	private String confirmResult;

	private SelfTestEntryDto selfTest;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime dispensingDate;

	public SelfTestSpecimenDto() {

	}

	public SelfTestSpecimenDto(SelfTestSpecimen entity) {

		if (entity == null) {
			return;
		}

		this.id = entity.getId();
		this.code = entity.getCode();
		this.name = entity.getName();
		this.support = entity.getSupport();
		this.client = entity.getClient();
		this.clientYob = entity.getClientYob();
		this.clientGender = entity.getClientGender();
		this.clientRiskGroup = entity.getClientRiskGroup();
		this.screenResult = entity.getScreenResult();
		this.confirmResult = entity.getConfirmResult();
		this.dispensingDate = entity.getDispensingDate();

		if (entity.getSelfTest() != null) {
			this.selfTest = new SelfTestEntryDto();
			this.selfTest.setId(entity.getSelfTest().getId());
		}
	}

	public SelfTestSpecimen toEntity() {

		SelfTestSpecimen entity = new SelfTestSpecimen();

		entity.setId(id);
		entity.setCode(code);
		entity.setName(name);
		entity.setSupport(support);
		entity.setClient(client);
		entity.setClientYob(clientYob);
		entity.setClientGender(clientGender);
		entity.setClientRiskGroup(clientRiskGroup);
		entity.setScreenResult(screenResult);
		entity.setConfirmResult(confirmResult);
		entity.setDispensingDate(dispensingDate);

		if (selfTest != null) {
			SelfTestEntry ste = new SelfTestEntry();
			ste.setId(selfTest.getId());

			entity.setSelfTest(ste);
		}

		return entity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSupport() {
		return support;
	}

	public void setSupport(String support) {
		this.support = support;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public LocalDateTime getDispensingDate() {
		return dispensingDate;
	}

	public void setDispensingDate(LocalDateTime dispensingDate) {
		this.dispensingDate = dispensingDate;
	}

	public Integer getClientYob() {
		return clientYob;
	}

	public void setClientYob(Integer clientYob) {
		this.clientYob = clientYob;
	}

	public Gender getClientGender() {
		return clientGender;
	}

	public void setClientGender(Gender clientGender) {
		this.clientGender = clientGender;
	}

	public String getClientRiskGroup() {
		return clientRiskGroup;
	}

	public void setClientRiskGroup(String clientRiskGroup) {
		this.clientRiskGroup = clientRiskGroup;
	}

	public String getScreenResult() {
		return screenResult;
	}

	public void setScreenResult(String screenResult) {
		this.screenResult = screenResult;
	}

	public String getConfirmResult() {
		return confirmResult;
	}

	public void setConfirmResult(String confirmResult) {
		this.confirmResult = confirmResult;
	}

	public SelfTestEntryDto getSelfTest() {
		return selfTest;
	}

	public void setSelfTest(SelfTestEntryDto selfTest) {
		this.selfTest = selfTest;
	}

}
