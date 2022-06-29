package org.pepfar.pdma.app.data.service;

import java.util.List;

import org.pepfar.pdma.app.data.dto.ServiceDto;
import org.springframework.data.domain.Page;

public interface ServiceService
{

	public ServiceDto findById(Long id);

	public ServiceDto findByCode(String code);

	public List<ServiceDto> findAll();

	public Page<ServiceDto> findAllPageable(int pageIndex, int pageSize);

	public boolean codeExists(ServiceDto dto);

	public ServiceDto saveOne(ServiceDto dto);

	public void deleteMultiple(ServiceDto[] dtos);

}
