package org.pepfar.pdma.app.data.service;

import org.pepfar.pdma.app.data.dto.DocumentDto;
import org.pepfar.pdma.app.data.dto.DocumentFilterDto;
import org.springframework.data.domain.Page;

public interface DocumentService
{

	public DocumentDto findById(Long id, boolean includeContent);

	public Page<DocumentDto> findAll(DocumentFilterDto filter);

	public DocumentDto saveOne(DocumentDto dto);

	public void deleteMultiple(DocumentDto[] dtos);
}
