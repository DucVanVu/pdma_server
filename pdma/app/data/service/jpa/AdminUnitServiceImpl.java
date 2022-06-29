package org.pepfar.pdma.app.data.service.jpa;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jsoup.helper.StringUtil;
import org.pepfar.pdma.app.data.aop.Loggable;
import org.pepfar.pdma.app.data.domain.AdminUnit;
import org.pepfar.pdma.app.data.domain.QAdminUnit;
import org.pepfar.pdma.app.data.domain.Role;
import org.pepfar.pdma.app.data.domain.User;
import org.pepfar.pdma.app.data.dto.AdminUnitDto;
import org.pepfar.pdma.app.data.dto.AdminUnitFilterDto;
import org.pepfar.pdma.app.data.repository.AdminUnitRepository;
import org.pepfar.pdma.app.data.repository.UserOrganizationRepository;
import org.pepfar.pdma.app.data.service.AdminUnitService;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.dsl.BooleanExpression;

@Service
public class AdminUnitServiceImpl implements AdminUnitService {

	@Autowired
	private AdminUnitRepository repos;
	
	@Autowired
	private UserOrganizationRepository userOrganizationRepository;

	@Override
	@Transactional(readOnly = true)
	public AdminUnitDto findById(Long id) {

		if (!CommonUtils.isPositive(id, true)) {
			return null;
		}

		AdminUnit entity = repos.findOne(id);

		if (entity != null) {
			return new AdminUnitDto(entity);
		}

		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public AdminUnitDto findByGsoCode(String code) {

		AdminUnit entity = repos.findOne(QAdminUnit.adminUnit.codeGso.equalsIgnoreCase(code));

		if (entity != null) {
			return new AdminUnitDto(entity);
		}

		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public AdminUnitDto findByCode(String code) {

		if (CommonUtils.isEmpty(code)) {
			return null;
		}

		AdminUnit entity = repos.findOne(QAdminUnit.adminUnit.code.equalsIgnoreCase(code));

		if (entity != null) {
			return new AdminUnitDto(entity);
		}

		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public List<AdminUnitDto> findAll(AdminUnitFilterDto filter) {

		QAdminUnit q = QAdminUnit.adminUnit;
		BooleanExpression be = q.id.isNotNull();

		if (CommonUtils.isPositive(filter.getParentId(), true)) {
			be = be.and(q.parent.isNotNull().and(q.parent.id.eq(filter.getParentId().longValue())));
		}

		if (!CommonUtils.isEmpty(filter.getParentCode())) {
			be = be.and(
					q.parent.isNotNull().and(q.parent.code.isNotNull().and(q.parent.code.eq(filter.getParentCode()))));
		}

		if (filter.isExcludeVoided()) {
			be = be.and(q.voided.isFalse());
		}

		if (!CommonUtils.isEmpty(filter.getKeyword())) {
			be = be.and(q.name.containsIgnoreCase(filter.getKeyword())
					.or(q.parent.isNotNull().and(q.parent.name.containsIgnoreCase(filter.getKeyword()))));
		}

//		be.and(q.orderNumber.isNotNull().and(q.orderNumber.desc().));

		List<AdminUnitDto> list = new ArrayList<>();

		repos.findAll(be, q.orderNumber.desc(), q.name.asc()).forEach(a -> {
			list.add(new AdminUnitDto(a));
		});

		return list;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<AdminUnitDto> findAllPageable(AdminUnitFilterDto filter) {

		if (filter == null) {
			filter = new AdminUnitFilterDto();
		}

		if (filter.getPageIndex() < 0) {
			filter.setPageIndex(0);
		}

		if (filter.getPageSize() <= 0) {
			filter.setPageSize(25);
		}

		QAdminUnit q = QAdminUnit.adminUnit;
		BooleanExpression be = q.id.isNotNull();
		Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize(),
				new Sort(new Order(Direction.ASC, "name")));

		if (CommonUtils.isPositive(filter.getParentId(), true)) {
			be = be.and(q.parent.isNotNull().and(q.parent.id.eq(filter.getParentId().longValue())));
		}

		if (!CommonUtils.isEmpty(filter.getParentCode())) {
			be = be.and(
					q.parent.isNotNull().and(q.parent.code.isNotNull().and(q.parent.code.eq(filter.getParentCode()))));
		}

		if (filter.isExcludeVoided()) {
			be = be.and(q.voided.isFalse());
		}

		if (!CommonUtils.isEmpty(filter.getKeyword())) {
			be = be.and(q.name.containsIgnoreCase(filter.getKeyword())
					.or(q.parent.isNotNull().and(q.parent.name.containsIgnoreCase(filter.getKeyword()))));
		}

		Page<AdminUnit> page = repos.findAll(be, pageable);
		List<AdminUnitDto> content = new ArrayList<>();

		page.getContent().parallelStream().forEachOrdered(a -> {
			content.add(new AdminUnitDto(a));
		});

		return new PageImpl<>(content, pageable, page.getTotalElements());
	}

	@Override
	@Transactional(readOnly = true)
	public boolean codeExists(AdminUnitDto dto) {

		if (dto == null || CommonUtils.isEmpty(dto.getCode())) {
			return false;
		}

		QAdminUnit q = QAdminUnit.adminUnit;

		if (!CommonUtils.isPositive(dto.getId(), true)) {
			return repos.exists(q.code.equalsIgnoreCase(dto.getCode()));
		}

		return repos.exists(q.code.equalsIgnoreCase(dto.getCode()).and(q.id.ne(dto.getId())));
	}

	@Override
	@Loggable
	@Transactional(rollbackFor = Exception.class)
	public AdminUnitDto saveOne(AdminUnitDto dto) {

		if (dto == null) {
			throw new IllegalArgumentException("Admin Unit could not be null.");
		}

		AdminUnit entity = null;

		if (CommonUtils.isPositive(dto.getId(), true)) {
			entity = repos.findOne(dto.getId());
		}

		if (entity == null) {
			entity = dto.toEntity();
		} else {
			entity.setLevel(dto.getLevel());
			entity.setCodeGso(dto.getCodeGso());
			entity.setCode(dto.getCode());
			entity.setName(dto.getName());
			entity.setVoided(dto.getVoided());
			entity.setOrderNumber(dto.getOrderNumber());
		}

		// parent
		AdminUnit parent = null;
		if (dto.getParent() != null && CommonUtils.isPositive(dto.getParent().getId(), true)) {
			parent = repos.findOne(dto.getParent().getId());
		}

		if (!CommonUtils.isPositive(entity.getLevel(), true)) {
			if (parent != null) {
				entity.setLevel(parent.getLevel() + 1);
			} else {
				entity.setLevel(1);
			}
		}

		entity.setParent(parent);
		entity = repos.save(entity);

		if (entity != null) {
			return new AdminUnitDto(entity);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	@Loggable
	@Transactional(rollbackFor = Exception.class)
	public void updateGsoCode() {
		Iterator<AdminUnit> list = repos.findAll().iterator();
		System.out.println("Updating GSO code for admin units.");
		System.out.println("---------------------------------");
		list.forEachRemaining(e -> {
			if (CommonUtils.isEmpty(e.getCodeGso())) {
				System.out.print(".");
				String codeGso = e.getCode().substring(e.getCode().indexOf("_") + 1);
				switch (e.getLevel()) {
					case 1:
						break;
					case 2:
						codeGso = zeroCompensate(codeGso, 2);
						break;
					case 3:
						codeGso = zeroCompensate(codeGso, 3);
						break;
					case 4:
						codeGso = zeroCompensate(codeGso, 5);
						break;
				}

				e.setCodeGso(codeGso);
				e = repos.save(e);

			}
		});

		System.out.println("Updating GSO code completed.");
	}

	@Override
	@Loggable
	@Transactional(rollbackFor = Exception.class)
	public void deleteMultiple(AdminUnitDto[] dtos) {

		if (CommonUtils.isEmpty(dtos)) {
			return;
		}

		for (AdminUnitDto dto : dtos) {
			if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
				continue;
			}

			AdminUnit entity = repos.findOne(dto.getId());

			if (entity != null) {
				repos.delete(entity);
			}
		}
	}

	private String zeroCompensate(String input, int max) {
		if (CommonUtils.isEmpty(input)) {
			return input;
		}

		while (input.length() < max) {
			input = "0" + input;
		}

		return input;
	}

//	@Override
//	public List<AdminUnitDto> findAllProvinceByUser() {
//
//		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//		User user = null;
//		if(auth!=null) {
//			user = (User) auth.getPrincipal();
//		}
//		if(user == null) {
//			return null;
//		}
//		Boolean isProvince = false;
//		if(user.getRoles()!=null) {
//			for(Role role: user.getRoles()) {
//				if(role.getName().toUpperCase().equals("ROLE_PROVINCIAL_MANAGER")) {
//					isProvince = true;
//					break;
//				}
//			}
//		}
//		List<Long> provinceIds = null;
//		if(user.getId()!=null && isProvince) {
//			provinceIds = userOrganizationRepository.getListProvinceByUserId(user.getId());
//		}
//		QAdminUnit q = QAdminUnit.adminUnit;
//		BooleanExpression be = q.id.isNotNull();
//
//		if(provinceIds!=null) {
//			be = be.and(q.id.in(provinceIds));
//		}
//		be = be.and(q.parent.isNotNull().and(q.parent.id.eq(1L)));
//
//		List<AdminUnitDto> list = new ArrayList<>();
//
//		repos.findAll(be, q.orderNumber.desc(), q.name.asc()).forEach(a -> {
//			list.add(new AdminUnitDto(a));
//		});
//
//
//		return list;
//	}

	@Override
	public List<AdminUnitDto> findAllProvinceByUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = null;
		if(auth!=null) {
			user = (User) auth.getPrincipal();
		}
		if(user == null) {
			return null;
		}
//		Boolean isProvince = false;
//		if(user.getRoles()!=null) {
//			for(Role role: user.getRoles()) {
//				if(role.getName().toUpperCase().equals("ROLE_PROVINCIAL_MANAGER")) {
//					isProvince = true;
//					break;
//				}
//			}
//		}
		List<Long> provinceIds = null;
		Boolean isAdministrator = SecurityUtils.isUserInRole(SecurityUtils.getCurrentUser(), "ROLE_ADMIN");
		if(isAdministrator) {
			provinceIds = userOrganizationRepository.getListProvinceByAdmin();
		} else {
			if (user.getId() != null) {
				provinceIds = userOrganizationRepository.getListProvinceByUserId(user.getId());
			}
		}
		QAdminUnit q = QAdminUnit.adminUnit;
		BooleanExpression be = q.id.isNotNull();

		if(provinceIds!=null) {
			be = be.and(q.id.in(provinceIds));
		}
		be = be.and(q.parent.isNotNull().and(q.parent.id.eq(1L)));

		List<AdminUnitDto> list = new ArrayList<>();

		repos.findAll(be, q.orderNumber.desc(), q.name.asc()).forEach(a -> {
			list.add(new AdminUnitDto(a));
		});


		return list;
	}
}
