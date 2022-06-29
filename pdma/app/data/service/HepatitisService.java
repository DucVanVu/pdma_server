package org.pepfar.pdma.app.data.service;

import java.util.List;

import org.pepfar.pdma.app.data.dto.HepatitisDto;
import org.pepfar.pdma.app.data.dto.HepatitisFilterDto;
import org.springframework.data.domain.Page;

public interface HepatitisService {

	public HepatitisDto findById(Long id);

	public List<HepatitisDto> findAll(HepatitisFilterDto filter);
	
	public Page<HepatitisDto> findAllPageable(HepatitisFilterDto filter);

	public HepatitisDto saveOne(HepatitisDto dto);

	public void deleteMultiple(HepatitisDto[] dtos);

}
