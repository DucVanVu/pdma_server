package org.pepfar.pdma.app.data.service;

import java.util.List;

import org.pepfar.pdma.app.data.dto.DocumentTypeDto;
import org.springframework.data.domain.Page;

public interface DocumentTypeService
{

	public DocumentTypeDto findById(Long id);

	public DocumentTypeDto findByCode(String code);

	public List<DocumentTypeDto> findAll();

	public Page<DocumentTypeDto> findAll(int pageIndex, int pageSize);

	public DocumentTypeDto saveOne(DocumentTypeDto dto);

	public void deleteMultiple(DocumentTypeDto[] dtos);
}
