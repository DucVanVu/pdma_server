package org.pepfar.pdma.app.data.service;

import java.util.List;

import org.pepfar.pdma.app.data.dto.HIVConfirmLabDto;
import org.pepfar.pdma.app.data.dto.HIVConfirmLabFilterDto;
import org.springframework.data.domain.Page;

public interface HIVConfirmLabService
{

	public HIVConfirmLabDto findById(Long id);

	public HIVConfirmLabDto findByCode(String code);

	public List<HIVConfirmLabDto> findAll(HIVConfirmLabFilterDto filter);

	public Page<HIVConfirmLabDto> findAllPageable(HIVConfirmLabFilterDto filter);

	public HIVConfirmLabDto saveOne(HIVConfirmLabDto dto);

	public void deleteMultiple(HIVConfirmLabDto[] dtos);

}
