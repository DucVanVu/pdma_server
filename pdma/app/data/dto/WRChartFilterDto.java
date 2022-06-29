package org.pepfar.pdma.app.data.dto;

import java.time.LocalDateTime;

import org.pepfar.pdma.app.utils.CustomLocalDateTimeDeserializer2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class WRChartFilterDto
{

	private OrganizationDto org;

	private AdminUnitDto province;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime toDate;

	private UserDto user;

	public OrganizationDto getOrg() {
		return org;
	}

	public void setOrg(OrganizationDto org) {
		this.org = org;
	}

	public AdminUnitDto getProvince() {
		return province;
	}

	public void setProvince(AdminUnitDto province) {
		this.province = province;
	}

	public LocalDateTime getToDate() {
		return toDate;
	}

	public void setToDate(LocalDateTime toDate) {
		this.toDate = toDate;
	}

	public UserDto getUser() {
		return user;
	}

	public void setUser(UserDto user) {
		this.user = user;
	}

}
