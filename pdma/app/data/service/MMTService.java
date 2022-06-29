package org.pepfar.pdma.app.data.service;

import org.pepfar.pdma.app.data.dto.MMTDto;
import org.pepfar.pdma.app.data.dto.MMTFilterDto;
import org.springframework.data.domain.Page;

public interface MMTService {

	public MMTDto findById(Long id);

	public Page<MMTDto> findAllPageable(MMTFilterDto filter);

	public MMTDto saveOne(MMTDto dto);

	public void deleteMultiple(MMTDto[] dtos);

}
