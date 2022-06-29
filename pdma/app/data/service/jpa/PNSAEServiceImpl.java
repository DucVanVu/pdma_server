package org.pepfar.pdma.app.data.service.jpa;

import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pepfar.pdma.app.Constants;
import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.domain.PNSAE;
import org.pepfar.pdma.app.data.domain.QOrganization;
import org.pepfar.pdma.app.data.domain.QPNSAE;
import org.pepfar.pdma.app.data.domain.User;
import org.pepfar.pdma.app.data.dto.PNSAEDto;
import org.pepfar.pdma.app.data.dto.PNSAEFilterDto;
import org.pepfar.pdma.app.data.repository.OrganizationRepository;
import org.pepfar.pdma.app.data.repository.PNSAERepository;
import org.pepfar.pdma.app.data.service.PNSAEService;
import org.pepfar.pdma.app.data.types.Gender;
import org.pepfar.pdma.app.data.types.Permission;
import org.pepfar.pdma.app.utils.AuthorizationUtils;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.ExcelUtils;
import org.pepfar.pdma.app.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.querydsl.core.types.dsl.BooleanExpression;

@Service
public class PNSAEServiceImpl implements PNSAEService {

	@Autowired
	private PNSAERepository repos;

	@Autowired
	private OrganizationRepository orgRepos;

	@Autowired
	private AuthorizationUtils authUtils;

	@Autowired
	private ApplicationContext context;

	@Override
	@Transactional(readOnly = true)
	public PNSAEDto findById(Long id) {

		if (!CommonUtils.isPositive(id, true)) {
			return null;
		}

		PNSAE entity = repos.findOne(id);

		if (entity != null) {
			return new PNSAEDto(entity);
		} else {
			return null;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Page<PNSAEDto> findAllPageable(PNSAEFilterDto filter) {

		User currentUser = SecurityUtils.getCurrentUser();

		if (filter == null) {
			filter = new PNSAEFilterDto();
		}

		if (filter.getPageIndex() < 0) {
			filter.setPageIndex(0);
		}

		if (filter.getPageSize() <= 0) {
			filter.setPageSize(25);
		}

		Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize(),
				new Sort(Direction.DESC, "reportDate"));
		QPNSAE q = QPNSAE.pNSAE;
		BooleanExpression be = q.id.isNotNull();

		// Unauthorized
		List<Long> grantedOrgIds = authUtils.getGrantedOrgIds(Permission.READ_ACCESS);
		if (CommonUtils.isEmpty(grantedOrgIds)) {
			return new PageImpl<>(new ArrayList<>(), pageable, 0);
		}

		be = be.and(q.facility.id.longValue().in(grantedOrgIds));

		if (!SecurityUtils.isUserInRole(currentUser, Constants.ROLE_SITE_MANAGER)) {
			be = be.and(q.submitted.isTrue());
		}

		Page<PNSAE> page = repos.findAll(be, pageable);
		List<PNSAEDto> content = new ArrayList<>();

		page.getContent().forEach(e -> {
			content.add(new PNSAEDto(e));
		});

		return new PageImpl<>(content, pageable, page.getTotalElements());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public PNSAEDto saveOne(PNSAEDto dto) {

		if (dto == null || dto.getFacility() == null || !CommonUtils.isPositive(dto.getFacility().getId(), true)) {
			throw new RuntimeException("Illegal arguments passed to save method.");
		}

		List<Long> grantedOrgIds = authUtils.getGrantedOrgIds(Permission.WRITE_ACCESS);
		if (CommonUtils.isEmpty(grantedOrgIds) || !grantedOrgIds.contains(dto.getFacility().getId().longValue())) {
			throw new RuntimeException("Unauthorized permission to save entry.");
		}

		PNSAE entity = null;

		if (CommonUtils.isPositive(dto.getId(), true)) {
			entity = repos.findOne(dto.getId());
		}

		if (entity == null) {
			entity = dto.toEntity();
		} else {
			entity.setFacilityType(dto.getFacilityType());
			entity.setEventType(dto.getEventType());
			entity.setReportDate(dto.getReportDate());
			entity.setHurt(dto.getHurt());
			entity.setPnsRootCause(dto.getPnsRootCause());
			entity.setAge(dto.getAge());
			entity.setGender(dto.getGender());
			entity.setGroup(dto.getGroup());
			entity.setActionTaken(dto.getActionTaken());
			entity.setInvestigated(dto.getInvestigated());
			entity.setInvestigateDate(dto.getInvestigateDate());
			entity.setDiscussed(dto.getDiscussed());
			entity.setAddressed(dto.getAddressed());
			entity.setAddressDate(dto.getAddressDate());
		}

		Organization facility = orgRepos.findOne(dto.getFacility().getId());

		if (facility == null) {
			throw new RuntimeException("Facility cannot be null!");
		}

		entity.setFacility(facility);

		entity = repos.save(entity);

		if (entity == null) {
			throw new RuntimeException();
		}

		return new PNSAEDto(entity);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public PNSAEDto submit(PNSAEDto dto) {
		if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
			throw new RuntimeException("Illegal arguments passed to submit method.");
		}

		PNSAE entity = null;

		if (CommonUtils.isPositive(dto.getId(), true)) {
			entity = repos.findOne(dto.getId());
		}

		if (entity == null) {
			throw new RuntimeException("Entity not found.");
		}

		List<Long> grantedOrgIds = authUtils.getGrantedOrgIds(Permission.WRITE_ACCESS);
		if (CommonUtils.isEmpty(grantedOrgIds) || !grantedOrgIds.contains(dto.getFacility().getId().longValue())) {
			throw new RuntimeException("Unauthorized permission to save entry.");
		}

		entity.setSubmitted(true);
		entity = repos.save(entity);

		if (entity == null) {
			throw new RuntimeException();
		}

		return new PNSAEDto(entity);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteMultiple(PNSAEDto[] dtos) {
		if (CommonUtils.isEmpty(dtos)) {
			return;
		}

		for (PNSAEDto dto : dtos) {
			if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
				continue;
			}

			PNSAE entity = repos.findOne(dto.getId());

			if (entity != null) {
				repos.delete(entity);
			}
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Workbook exportData(PNSAEFilterDto filter) {

		Workbook blankBook = new XSSFWorkbook();
		blankBook.createSheet();

		Workbook wbook = null;
		try (InputStream template = context.getResource("classpath:templates/pns-ae-report-template.xlsx")
				.getInputStream()) {
			wbook = new XSSFWorkbook(template);
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<Long> grantedOrgIds = authUtils.getGrantedOrgIds(Permission.READ_ACCESS);

		if (CommonUtils.isEmpty(grantedOrgIds) || wbook == null || filter == null || filter.getFromDate() == null
				|| filter.getToDate() == null) {
			return blankBook;
		}

		Sheet sheet = wbook.getSheetAt(0);

		// Indicators
		AtomicInteger[][] indicators = new AtomicInteger[14][4];
		for (int i = 0; i < 14; i++) {
			for (int j = 0; j < 4; j++) {
				indicators[i][j] = new AtomicInteger(0);
			}
		}

		// Calculation
		QPNSAE q = QPNSAE.pNSAE;
		List<PNSAE> list = Lists.newArrayList(repos.findAll(q.facility.id.longValue().in(grantedOrgIds)
				.and(q.reportDate.between(filter.getFromDate(), filter.getToDate()))));

		list.parallelStream().forEach(e -> {
			// total events
			increase(e.getAge(), e.getGender(), indicators, 0);

			// by facility type
			switch (e.getFacilityType()) {
				case PUBLIC_FACILITY:
					increase(e.getAge(), e.getGender(), indicators, 1);
					break;
				case PRIVATE_FACILITY:
					increase(e.getAge(), e.getGender(), indicators, 2);
					break;
				case COMMUNITY:
					increase(e.getAge(), e.getGender(), indicators, 3);
					break;
				case OTHER:
					increase(e.getAge(), e.getGender(), indicators, 4);
					break;
				default:
					break;

			}

			// by client type
			switch (e.getGroup()) {
				case 1:
					increase(e.getAge(), e.getGender(), indicators, 5);
					break;
				case 2:
					increase(e.getAge(), e.getGender(), indicators, 6);
					break;
				case 3:
					increase(e.getAge(), e.getGender(), indicators, 7);
					break;
				case 4:
					increase(e.getAge(), e.getGender(), indicators, 8);
					break;
				default:
					break;
			}

			// If hurt the client
			if (e.getHurt() == 1) {
				increase(e.getAge(), e.getGender(), indicators, 9);
			}

			// if PNS is root cause
			if (e.getPnsRootCause() == 1) {
				increase(e.getAge(), e.getGender(), indicators, 10);
			}

			// if action taken immediately
			if (e.getActionTaken() == 1) {
				increase(e.getAge(), e.getGender(), indicators, 11);
			}

			// if investigated
			if (e.getInvestigated() == 1) {
				increase(e.getAge(), e.getGender(), indicators, 12);
			}

			// if addressed
			if (e.getAddressed() == 1) {
				increase(e.getAge(), e.getGender(), indicators, 13);
			}
		});

		// Fill out the report
		Row row = null;
		Cell cell = null;
		for (int i = 0; i < 14; i++) {
			if (i > 4) {
				row = sheet.getRow(i + 8);
			} else if (i > 0) {
				row = sheet.getRow(i + 7);
			} else {
				row = sheet.getRow(i + 6);
			}

			for (int j = 0; j < 4; j++) {
				cell = row.getCell(j + 2);
				cell.setCellValue(indicators[i][j].get());
			}
		}

		// Fill out facility and period data
		List<Organization> orgs = Lists
				.newArrayList(orgRepos.findAll(QOrganization.organization.id.longValue().in(grantedOrgIds)));

		if (orgs.size() == 1) {
			ExcelUtils.writeInCell(sheet, 1, 0, orgs.get(0).getName());
		} else {
			ExcelUtils.writeInCell(sheet, 1, 0, "Nhiều cơ sở (xem danh sách phía dưới)");
			ExcelUtils.writeInCell(sheet, 23, 1, "Nhiều cơ sở (xem danh sách phía dưới)");

			int i = 0;
			for (Organization org : orgs) {
				String text = org.getName();

				if (org.getAddress() != null && org.getAddress().getProvince() != null) {
					text += ", " + org.getAddress().getProvince().getName();
				}

				ExcelUtils.createAndWriteInCell(sheet, 24 + i, 1, text);
			}
		}

		String periodStr = "Giai đoạn báo cáo: ";
		periodStr += filter.getFromDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		periodStr += " - ";
		periodStr += filter.getToDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

		ExcelUtils.writeInCell(sheet, 2, 0, periodStr);

		// Evaluate formulas
		XSSFFormulaEvaluator.evaluateAllFormulaCells(wbook);

		return wbook;
	}

	/**
	 * Increase the indicator
	 * 
	 * @param age
	 * @param gender
	 * @param arr
	 * @param i
	 */
	private void increase(int age, Gender gender, AtomicInteger[][] arr, int i) {

		if (gender == null) {
			return;
		}

		int j = -1;

		if (age < 15) {
			switch (gender) {
				case MALE:
					j = 0;
					break;
				case FEMALE:
					j = 1;
					break;
				default:
					break;
			}
		} else {
			switch (gender) {
				case MALE:
					j = 2;
					break;
				case FEMALE:
					j = 3;
					break;
				default:
					break;
			}
		}

		if (i >= 0 && j >= 0) {
			arr[i][j].incrementAndGet();
		}
	}
}
