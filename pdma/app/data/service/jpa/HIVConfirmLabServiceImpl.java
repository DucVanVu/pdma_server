package org.pepfar.pdma.app.data.service.jpa;

import java.util.ArrayList;
import java.util.List;

import org.pepfar.pdma.app.data.aop.Loggable;
import org.pepfar.pdma.app.data.domain.AdminUnit;
import org.pepfar.pdma.app.data.domain.HIVConfirmLab;
import org.pepfar.pdma.app.data.domain.Location;
import org.pepfar.pdma.app.data.domain.QHIVConfirmLab;
import org.pepfar.pdma.app.data.dto.HIVConfirmLabDto;
import org.pepfar.pdma.app.data.dto.HIVConfirmLabFilterDto;
import org.pepfar.pdma.app.data.dto.LocationDto;
import org.pepfar.pdma.app.data.repository.AdminUnitRepository;
import org.pepfar.pdma.app.data.repository.HIVConfirmLabRepository;
import org.pepfar.pdma.app.data.repository.LocationRepository;
import org.pepfar.pdma.app.data.service.HIVConfirmLabService;
import org.pepfar.pdma.app.data.types.AddressType;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.dsl.BooleanExpression;

@Service
public class HIVConfirmLabServiceImpl implements HIVConfirmLabService {

	@Autowired
	private HIVConfirmLabRepository repos;

	@Autowired
	private AdminUnitRepository auRepos;

	@Autowired
	private LocationRepository locRepos;

	@Override
	@Transactional(readOnly = true)
	public HIVConfirmLabDto findById(Long id) {

		if (!CommonUtils.isPositive(id, true)) {
			return null;
		}

		HIVConfirmLab entity = repos.findOne(id);

		if (entity != null) {
			return new HIVConfirmLabDto(entity);
		}

		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public HIVConfirmLabDto findByCode(String code) {
		if (CommonUtils.isEmpty(code)) {
			return null;
		}

		HIVConfirmLab entity = repos.findOne(QHIVConfirmLab.hIVConfirmLab.code.equalsIgnoreCase(code));

		if (entity != null) {
			return new HIVConfirmLabDto(entity);
		}

		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public List<HIVConfirmLabDto> findAll(HIVConfirmLabFilterDto filter) {

		QHIVConfirmLab q = QHIVConfirmLab.hIVConfirmLab;
		BooleanExpression be = q.id.isNotNull();

		if (filter == null) {
			filter = new HIVConfirmLabFilterDto();
		}

		if (!CommonUtils.isEmpty(filter.getKeyword())) {
			be = be.and(q.name.containsIgnoreCase(filter.getKeyword()))
					.or(q.address.province.name.containsIgnoreCase(filter.getKeyword()));
		}

		if (filter.getProvince() != null && CommonUtils.isPositive(filter.getProvince().getId(), true)) {
			be = be.and(q.address.province.id.longValue().eq(filter.getProvince().getId()));
		}

		List<HIVConfirmLabDto> dtos = new ArrayList<>();

		repos.findAll(be, q.name.asc()).forEach(e -> {
			dtos.add(new HIVConfirmLabDto(e));
		});
		;

		return dtos;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<HIVConfirmLabDto> findAllPageable(HIVConfirmLabFilterDto filter) {

		if (filter == null) {
			filter = new HIVConfirmLabFilterDto();
		}

		if (filter.getPageIndex() < 0) {
			filter.setPageIndex(0);
		}

		if (filter.getPageSize() <= 0) {
			filter.setPageSize(25);
		}

		QHIVConfirmLab q = QHIVConfirmLab.hIVConfirmLab;
		BooleanExpression be = q.id.isNotNull();

		if (!CommonUtils.isEmpty(filter.getKeyword())) {
			be = be.and(q.name.containsIgnoreCase(filter.getKeyword())
					.or(q.address.province.name.containsIgnoreCase(filter.getKeyword())));
		}

		if (filter.getProvince() != null && CommonUtils.isPositive(filter.getProvince().getId(), true)) {
			be = be.and(q.address.province.id.longValue().eq(filter.getProvince().getId()));
		}
		
		if (filter.getProvinceIds()!=null) {
			be = be.and(q.address.province.id.longValue().in(filter.getProvinceIds()));
		}

		Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize(),
				new Sort(new Order(Direction.ASC, "name")));
		Page<HIVConfirmLab> page = repos.findAll(be, pageable);
		List<HIVConfirmLabDto> content = new ArrayList<>();

		page.getContent().parallelStream().forEachOrdered(e -> {
			content.add(new HIVConfirmLabDto(e));
		});

		return new PageImpl<>(content, pageable, page.getTotalElements());
	}

	@Override
	@Loggable
	@Transactional(rollbackFor = Exception.class)
	public HIVConfirmLabDto saveOne(HIVConfirmLabDto dto) {

		if (dto == null) {
			throw new IllegalArgumentException("Cannot save a null HIVConfirmLab instance!");
		}

		HIVConfirmLab entity = null;
		if (CommonUtils.isPositive(dto.getId(), true)) {
			entity = repos.findOne(dto.getId());
		}

		if (entity == null) {
			entity = dto.toEntity();
		} else {
			entity.setCode(dto.getCode());
			entity.setName(dto.getName());
		}

		// Address
		Location address = null;
		if (dto.getAddress() != null) {
			LocationDto addressDto = dto.getAddress();

			if (CommonUtils.isPositive(addressDto.getId(), true)) {
				address = locRepos.findOne(addressDto.getId());
			}

			if (address == null) {
				address = new Location();
			}

			address.setStreetAddress(addressDto.getStreetAddress());
			address.setAccuracy(addressDto.getAccuracy());
			address.setLatitude(addressDto.getLatitude());
			address.setLongitude(addressDto.getLongitude());

			AdminUnit district = null;
			AdminUnit province = null;
			AdminUnit country = null;

			if (addressDto.getDistrict() != null && CommonUtils.isPositive(addressDto.getDistrict().getId(), true)) {
				district = auRepos.findOne(addressDto.getDistrict().getId());
			}

			if (addressDto.getProvince() != null && CommonUtils.isPositive(addressDto.getProvince().getId(), true)) {
				province = auRepos.findOne(addressDto.getProvince().getId());
			}

			if (addressDto.getCountry() != null && CommonUtils.isPositive(addressDto.getCountry().getId(), true)) {
				country = auRepos.findOne(addressDto.getCountry().getId());
			}

			address.setDistrict(district);
			address.setProvince(province);
			address.setCountry(country);
			address.setAddressType(AddressType.OFFICE_ADDRESS);
		}

		entity.setAddress(address);

		entity = repos.save(entity);

		if (entity != null) {
			return new HIVConfirmLabDto(entity);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	@Loggable
	@Transactional(rollbackFor = Exception.class)
	public void deleteMultiple(HIVConfirmLabDto[] dtos) {
		if (CommonUtils.isEmpty(dtos)) {
			return;
		}

		for (HIVConfirmLabDto dto : dtos) {
			if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
				continue;
			}

			HIVConfirmLab entity = repos.findOne(dto.getId());
			if (entity != null) {
				repos.delete(entity);
			}
		}
	}

}
