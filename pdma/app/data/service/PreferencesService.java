package org.pepfar.pdma.app.data.service;

import org.pepfar.pdma.app.data.dto.PreferencesDto;
import org.springframework.data.domain.Page;

public interface PreferencesService
{

	public PreferencesDto findById(Long id);

	public PreferencesDto findByName(String name);

	public Page<PreferencesDto> findAll(int pageIndex, int pageSize);

	public PreferencesDto saveOne(PreferencesDto entity);

	public void deleteMultiple(PreferencesDto[] dtos);

}
