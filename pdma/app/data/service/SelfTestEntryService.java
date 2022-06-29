package org.pepfar.pdma.app.data.service;

import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.pepfar.pdma.app.data.dto.OrganizationDto;
import org.pepfar.pdma.app.data.dto.PreventionFilterDto;
import org.pepfar.pdma.app.data.dto.SelfTestEntryDto;
import org.pepfar.pdma.app.data.dto.SelfTestFilterDto;
import org.pepfar.pdma.app.data.dto.SelfTestSpecimenDto;
import org.springframework.data.domain.Page;

public interface SelfTestEntryService {

	public SelfTestEntryDto findById(Long id);

	public SelfTestSpecimenDto findSpecimenById(Long id);

	public Page<SelfTestEntryDto> findAll(SelfTestFilterDto filter);

	public Workbook exportSearchResults(SelfTestFilterDto filter);

	public Workbook exportReportSeftTestCase(PreventionFilterDto filter);

	public SelfTestEntryDto saveOne(SelfTestEntryDto dto);

	public SelfTestEntryDto saveSpecimen2Entry(SelfTestSpecimenDto dto);

	public void deleteMultiple(SelfTestEntryDto[] dtos);

	public void deleteSpecimens(SelfTestSpecimenDto[] dtos);

	List<OrganizationDto> getListSelfTestWriteAble();

	public Workbook exportReportSelfTestNew(PreventionFilterDto filter,Workbook workbook);

}
