package org.pepfar.pdma.app.data.service;

import org.pepfar.pdma.app.data.dto.CoviVacDto;
import org.pepfar.pdma.app.data.dto.CoviVacFilterDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CoviVacService {

    public CoviVacDto findById(Long id);

    public List<CoviVacDto> findAll(CoviVacFilterDto filter);

    public Page<CoviVacDto> findAllPageable(CoviVacFilterDto filter);

    public CoviVacDto saveOne(CoviVacDto dto);

    public void deleteMultiple(CoviVacDto[] dtos);
}
