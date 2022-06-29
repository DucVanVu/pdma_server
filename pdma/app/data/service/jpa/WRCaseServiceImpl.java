package org.pepfar.pdma.app.data.service.jpa;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.pepfar.pdma.app.data.aop.Loggable;
import org.pepfar.pdma.app.data.domain.AdminUnit;
import org.pepfar.pdma.app.data.domain.HIVConfirmLab;
import org.pepfar.pdma.app.data.domain.Location;
import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.domain.QAdminUnit;
import org.pepfar.pdma.app.data.domain.QWRCase;
import org.pepfar.pdma.app.data.domain.WRCase;
import org.pepfar.pdma.app.data.dto.LocationDto;
import org.pepfar.pdma.app.data.dto.OrganizationDto;
import org.pepfar.pdma.app.data.dto.WRCaseDto;
import org.pepfar.pdma.app.data.dto.WRCaseFilterDto;
import org.pepfar.pdma.app.data.repository.AdminUnitRepository;
import org.pepfar.pdma.app.data.repository.HIVConfirmLabRepository;
import org.pepfar.pdma.app.data.repository.LocationRepository;
import org.pepfar.pdma.app.data.repository.OrganizationRepository;
import org.pepfar.pdma.app.data.repository.WRCaseRepository;
import org.pepfar.pdma.app.data.service.WRCaseService;
import org.pepfar.pdma.app.data.types.Permission;
import org.pepfar.pdma.app.utils.AuthorizationUtils;
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

import com.google.common.collect.Lists;
import com.querydsl.core.types.dsl.BooleanExpression;

@Service
public class WRCaseServiceImpl implements WRCaseService {

	@Autowired
	private WRCaseRepository repos;

	@Autowired
	private AdminUnitRepository auRepos;

	@Autowired
	private LocationRepository locRepos;

	@Autowired
	private OrganizationRepository orgRepos;

	@Autowired
	private HIVConfirmLabRepository confirmLabRepos;

	@Autowired
	private AuthorizationUtils authorizationUtils;

	@Override
	@Transactional(readOnly = true)
	public WRCaseDto findById(Long id) {

		if (!CommonUtils.isPositive(id, true)) {
			return null;
		}

		WRCase entity = repos.findOne(id);

		if (entity != null) {
			return new WRCaseDto(entity);
		}

		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public List<WRCaseDto> findAny(WRCaseFilterDto filter) {

		if (filter == null) {
			return new ArrayList<>();
		}

		if (filter.getOwner() == null) {
			OrganizationDto owner = new OrganizationDto();
			owner.setId(0l);
			filter.setOwner(owner);
		}

		QWRCase q = QWRCase.wRCase;
		BooleanExpression be = q.id.isNotNull();

		List<WRCase> entities = new ArrayList<>();

		if (!CommonUtils.isEmpty(filter.getHivConfirmId())) {
			be = be.and(q.hivConfirmId.isNotNull().and(q.hivConfirmId.equalsIgnoreCase(filter.getHivConfirmId())));
			be = be.and(q.vct.isNotNull().and(q.vct.id.longValue().eq(filter.getOwner().getId())));

			entities = Lists.newArrayList(repos.findAll(be));
		} else if (!CommonUtils.isEmpty(filter.getPatientChartId())) {
			be = be.and(
					q.patientChartId.isNotNull().and(q.patientChartId.equalsIgnoreCase(filter.getPatientChartId())));
			be = be.and(q.opc.isNotNull().and(q.opc.id.longValue().eq(filter.getOwner().getId())));

			entities = Lists.newArrayList(repos.findAll(be));
		}

		List<WRCaseDto> dtos = new ArrayList<>();
		entities.forEach(e -> {
			dtos.add(new WRCaseDto(e));
		});

		return dtos;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<WRCaseDto> findAllPageable(WRCaseFilterDto filter) {

		if (filter == null) {
			filter = new WRCaseFilterDto();
		}

		if (filter.getPageIndex() < 0) {
			filter.setPageIndex(0);
		}

		if (filter.getPageSize() <= 0) {
			filter.setPageSize(25);
		}

		if (filter.getOwner() == null || !CommonUtils.isPositive(filter.getOwner().getId(), true)) {
			OrganizationDto owner = new OrganizationDto();
			owner.setId(0L);

			filter.setOwner(owner);
		}

		QWRCase q = QWRCase.wRCase;
		BooleanExpression be = q.id.isNotNull();

		if (!CommonUtils.isEmpty(filter.getKeyword())) {
			be = be.and(q.fullname.containsIgnoreCase(filter.getKeyword()))
					.or(q.nationalId.containsIgnoreCase(filter.getKeyword()));
		}

		be = be.and(q.vct.id.longValue().eq(filter.getOwner().getId())
				.or(q.opc.id.longValue().eq(filter.getOwner().getId())));

		Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize(),
				new Sort(new Order(Direction.ASC, "fullname")));
		Page<WRCase> page = repos.findAll(be, pageable);
		List<WRCaseDto> content = new ArrayList<>();

		page.getContent().parallelStream().forEachOrdered(e -> {
			content.add(new WRCaseDto(e));
		});

		return new PageImpl<>(content, pageable, page.getTotalElements());
	}

	@Override
	@Transactional(readOnly = true)
	public List<WRCaseDto> findTreatmentCases(WRCaseFilterDto filter) {

		if (filter.getOwner() == null || !CommonUtils.isPositive(filter.getOwner().getId(), true)
				|| CommonUtils.isEmpty(filter.getKeyword(), true)) {
			return new ArrayList<>();
		}

		List<Long> orgIds = authorizationUtils.getGrantedOrgIds(Permission.WRITE_ACCESS);

		QWRCase q = QWRCase.wRCase;
		BooleanExpression be = q.opc.id.in(orgIds) //.eq(filter.getOwner().getId().longValue())
				.and(q.linked2OpcAssist.isNull().or(q.linked2OpcAssist.isFalse())).and(q.patientChartId.isNotNull())
				.and(q.fullname.containsIgnoreCase(filter.getKeyword())
						.or(q.patientChartId.containsIgnoreCase(filter.getKeyword())));

		List<WRCaseDto> ret = new ArrayList<>();
		repos.findAll(be, q.fullname.asc()).forEach(c -> {
			if (ret.size() >= 10) {
				return;
			}

			WRCaseDto cdto = new WRCaseDto(c);

			cdto.setOpc(null);
			cdto.setVct(null);
			cdto.setConfirmLab(null);

			ret.add(cdto);
		});

		return ret;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<WRCaseDto> findUntreatedPageable(WRCaseFilterDto filter) {

		if (filter == null) {
			filter = new WRCaseFilterDto();
		}

		if (filter.getPageIndex() < 0) {
			filter.setPageIndex(0);
		}

		if (filter.getPageSize() <= 0) {
			filter.setPageSize(25);
		}

		if (filter.getOwner() == null || !CommonUtils.isPositive(filter.getOwner().getId(), true)) {
			OrganizationDto owner = new OrganizationDto();
			owner.setId(0l);

			filter.setOwner(owner);
		}

		QWRCase q = QWRCase.wRCase;
		BooleanExpression be = q.opc.id.isNull().and(q.vct.id.longValue().eq(filter.getOwner().getId()));

		if (!CommonUtils.isEmpty(filter.getKeyword())) {
			be = be.and(q.fullname.containsIgnoreCase(filter.getKeyword()))
					.or(q.nationalId.containsIgnoreCase(filter.getKeyword()));
		}

		Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize(),
				new Sort(new Order(Direction.ASC, "id")));

		Page<WRCase> page = repos.findAll(be, pageable);
		List<WRCaseDto> content = new ArrayList<>();

		page.getContent().parallelStream().forEachOrdered(e -> {
			content.add(new WRCaseDto(e));
		});

		return new PageImpl<>(content, pageable, page.getTotalElements());
	}

	@Override
	@Loggable
	@Transactional(rollbackFor = Exception.class)
	public WRCaseDto saveOne(WRCaseDto dto) {

		if (dto == null) {
			throw new IllegalArgumentException("Cannot save a null WRCase instance!");
		}

		WRCase entity = null;
		if (CommonUtils.isPositive(dto.getId(), true)) {
			entity = repos.findOne(dto.getId());
		}

		if (entity == null) {
			entity = dto.toEntity();
			entity.setHtsCaseStatus(0);
			entity.setTxCaseStatus(0);
		} else {
			entity.setFullname(dto.getFullname());
			entity.setDob(dto.getDob());
			entity.setGender(dto.getGender());
			entity.setNationalId(dto.getNationalId());
			entity.setHivConfirmId(dto.getHivConfirmId());
			entity.setScreeningDate(dto.getScreeningDate());
			entity.setConfirmDate(dto.getConfirmDate());
			entity.setEnrollmentDate(dto.getEnrollmentDate());
			entity.setArvInitiationDate(dto.getArvInitiationDate());
			entity.setPatientChartId(dto.getPatientChartId());
			entity.setHtsCaseStatus(dto.getHtsCaseStatus());
			entity.setTxCaseStatus(dto.getTxCaseStatus());
			entity.setNote(dto.getNote());

			entity.setRtriPos(dto.getRtriPos());
			entity.setOfferedPns(dto.getOfferedPns());
		}

		// Locations
		List<Location> locations = new ArrayList<>();
		for (LocationDto loc : dto.getLocations()) {
			Location address = null;

			if (CommonUtils.isPositive(loc.getId(), true)) {
				address = locRepos.findOne(loc.getId());
			}

			if (address == null && CommonUtils.isPositive(dto.getId(), true) && loc.getAddressType() != null) {
				List<Location> locs = locRepos.findForWRCase(dto.getId(), loc.getAddressType());

				if (locs != null && locs.size() > 0) {
					address = locs.get(0);
				}
			}

			if (address == null) {
				address = new Location();
			}

			address.setAddressType(loc.getAddressType());
			address.setStreetAddress(loc.getStreetAddress());
			address.setAccuracy(loc.getAccuracy());
			address.setLatitude(loc.getLatitude());
			address.setLongitude(loc.getLongitude());

			AdminUnit commune = null;
			AdminUnit district = null;
			AdminUnit province = null;
			AdminUnit country = null;

			if (loc.getCommune() != null && CommonUtils.isPositive(loc.getCommune().getId(), true)) {
				commune = auRepos.findOne(loc.getCommune().getId());
			}

			if (loc.getDistrict() != null && CommonUtils.isPositive(loc.getDistrict().getId(), true)) {
				district = auRepos.findOne(loc.getDistrict().getId());
			}

			if (loc.getProvince() != null && CommonUtils.isPositive(loc.getProvince().getId(), true)) {
				province = auRepos.findOne(loc.getProvince().getId());
			}

			if (loc.getCountry() != null) {
				if (CommonUtils.isPositive(loc.getCountry().getId(), true)) {
					country = auRepos.findOne(loc.getCountry().getId());
				}
				if (country == null && !CommonUtils.isEmpty(loc.getCountry().getCode())) {
					country = auRepos.findOne(QAdminUnit.adminUnit.code.equalsIgnoreCase(loc.getCountry().getCode()));
				}
			}

			address.setCommune(commune);
			address.setDistrict(district);
			address.setProvince(province);
			address.setCountry(country);
			address.setWrCase(entity);

			locations.add(address);
		}

		entity.getLocations().clear();
		entity.getLocations().addAll(locations);

		HIVConfirmLab confirmLab = null;
		Organization vct = null;
		Organization opc = null;

		if (dto.getVct() != null && CommonUtils.isPositive(dto.getVct().getId(), true)) {
			vct = orgRepos.findOne(dto.getVct().getId());
		}

		if (dto.getOpc() != null && CommonUtils.isPositive(dto.getOpc().getId(), true)) {
			opc = orgRepos.findOne(dto.getOpc().getId());
		}

		if (dto.getConfirmLab() != null && CommonUtils.isPositive(dto.getConfirmLab().getId(), true)) {
			confirmLab = confirmLabRepos.findOne(dto.getConfirmLab().getId());
		}

		entity.setConfirmLab(confirmLab);
		entity.setVct(vct);
		entity.setOpc(opc);

		entity = repos.save(entity);

		if (entity != null) {
			return new WRCaseDto(entity);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	@Loggable
	@Transactional(rollbackFor = Exception.class)
	public void markAsLinked2OPCAssist(WRCaseDto dto) {
		if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
			return;
		}

		WRCase entity = repos.findOne(dto.getId());

		if (entity == null) {
			return;
		}

		entity.setLinked2OpcAssist(true);
		repos.save(entity);
	}

	@Override
	@Loggable
	@Transactional(rollbackFor = Exception.class)
	public void deleteMultiple(WRCaseDto[] dtos) {
		if (CommonUtils.isEmpty(dtos)) {
			return;
		}

		for (WRCaseDto dto : dtos) {
			if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
				continue;
			}

			WRCase entity = repos.findOne(dto.getId());

			if (entity != null) {
				repos.delete(entity);
			}
		}
	}

}
