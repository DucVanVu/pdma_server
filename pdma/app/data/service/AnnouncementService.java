package org.pepfar.pdma.app.data.service;

import org.pepfar.pdma.app.data.dto.AnnouncementDto;
import org.pepfar.pdma.app.data.dto.AnnouncementFilterDto;
import org.springframework.data.domain.Page;

public interface AnnouncementService
{

	public AnnouncementDto findById(Long id);

	public Page<AnnouncementDto> findAllPageable(AnnouncementFilterDto filter);

	public AnnouncementDto saveOne(AnnouncementDto dto);

	public void deleteMultiple(AnnouncementDto[] dtos);

}
