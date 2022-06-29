package org.pepfar.pdma.app.data.service;

import java.util.List;

import org.pepfar.pdma.app.data.dto.DictionaryDto;
import org.pepfar.pdma.app.data.dto.DictionaryFilterDto;
import org.pepfar.pdma.app.data.dto.DictionaryTypeDto;
import org.pepfar.pdma.app.data.types.DictionaryType;
import org.springframework.data.domain.Page;

public interface DictionaryService
{

	public DictionaryDto findById(Long id);

	public DictionaryDto findByCode(String code);

	public List<DictionaryDto> findAll(DictionaryFilterDto filter);

	public Page<DictionaryDto> findAllPageable(DictionaryFilterDto filter);

	public List<DictionaryTypeDto> findMultiple(List<DictionaryType> types);

	public boolean codeExists(DictionaryDto dto);

	public DictionaryDto saveOne(DictionaryDto dto);

	public boolean saveSortOrder(DictionaryDto[] dtos);

	public void deleteMultiple(DictionaryDto[] dtos);
}
