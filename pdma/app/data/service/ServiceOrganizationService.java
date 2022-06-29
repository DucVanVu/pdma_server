package org.pepfar.pdma.app.data.service;

import java.util.List;

import org.pepfar.pdma.app.data.dto.ServiceOrganizationDto;

public interface ServiceOrganizationService
{

	public ServiceOrganizationDto findById(Long serviceId, Long organizationId);

	public List<ServiceOrganizationDto> findByServiceId(Long serviceId);

	public List<ServiceOrganizationDto> findByOrganizationId(Long organizationId);

	public ServiceOrganizationDto saveOne(ServiceOrganizationDto dto);

	public void deleteMultiple(ServiceOrganizationDto[] dtos);
}
