package org.pepfar.pdma.app.data.service.jpa;

import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pepfar.pdma.app.Constants;
import org.pepfar.pdma.app.data.domain.*;
import org.pepfar.pdma.app.data.dto.PNSAssessmentDto;
import org.pepfar.pdma.app.data.dto.PNSAssessmentFilterDto;
import org.pepfar.pdma.app.data.dto.PNSAssessmentPreferencesDto;
import org.pepfar.pdma.app.data.repository.OrganizationRepository;
import org.pepfar.pdma.app.data.repository.PNSAssessmentRepository;
import org.pepfar.pdma.app.data.repository.PreferencesRepository;
import org.pepfar.pdma.app.data.repository.UserOrganizationRepository;
import org.pepfar.pdma.app.data.repository.UserRepository;
import org.pepfar.pdma.app.data.service.PNSAssessmentService;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.ExcelUtils;
import org.pepfar.pdma.app.utils.HtmlUtils;
import org.pepfar.pdma.app.utils.SecurityUtils;
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
public class PNSAssessmentServiceImpl implements PNSAssessmentService {

	@Autowired
	private PNSAssessmentRepository repos;

	@Autowired
	private UserOrganizationRepository uoRepos;

	@Autowired
	private OrganizationRepository orgRepos;

	@Autowired
	private HtmlUtils htmlUtils;

	@Autowired
	private PreferencesRepository prefRepos;

	@Autowired
	private UserRepository userRepos;

	@Override
	@Transactional(readOnly = true)
	public PNSAssessmentDto findById(Long id, String propName, boolean hasAttachment) {
		if (!CommonUtils.isPositive(id, true)) {
			return null;
		}

		PNSAssessment entity = repos.findOne(id);

		if (entity != null) {
			return new PNSAssessmentDto(entity, propName, hasAttachment);
		} else {
			return null;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<PNSAssessmentDto> findAll(PNSAssessmentFilterDto filter) {

		if (filter == null || filter.getUser() == null || CommonUtils.isEmpty(filter.getFacilityIds())) {
			return new ArrayList<>();
		}

		QUser qUser = QUser.user;
		QPNSAssessment q = QPNSAssessment.pNSAssessment;
		BooleanExpression be = q.id.isNotNull();

		if (!CommonUtils.isEmpty(filter.getKeyword())) {
			be = be.and(q.ip.containsIgnoreCase(filter.getKeyword())
					.or(q.assessorName.containsIgnoreCase(filter.getKeyword()))
					.or(q.facility.name.containsIgnoreCase(filter.getKeyword()))
					.or(q.facility.address.province.name.containsIgnoreCase(filter.getKeyword())));
		}

		List<Long> ids = new ArrayList<>();
		Iterable<UserOrganization> ous = uoRepos.findAll(qUser.id.longValue().eq(filter.getUser().getId()));

		for (long id : filter.getFacilityIds()) {
			ous.forEach(uo -> {
				if (id == uo.getOrganization().getId()) {
					ids.add(uo.getOrganization().getId());
				}
			});
		}

		if (ids.size() <= 0) {
			ids.add(0l);
		}

		be = be.and(q.facility.id.longValue().in(ids));

		boolean includeOnlySubmitted = false;

		for (Role r : filter.getUser().getRoles()) {

			String roleName = r.getName();

			if (roleName.equalsIgnoreCase(Constants.ROLE_DONOR)
					|| r.getName().equalsIgnoreCase(Constants.ROLE_NATIONAL_MANAGER)
					|| r.getName().equalsIgnoreCase(Constants.ROLE_PROVINCIAL_MANAGER)
					|| r.getName().equalsIgnoreCase(Constants.ROLE_DISTRICT_MANAGER)) {
				includeOnlySubmitted = true;
			}
		}

		if (includeOnlySubmitted) {
			be = be.and(q.submitted.isTrue());
		}

		List<PNSAssessmentDto> ret = new ArrayList<>();
		repos.findAll(be, new Sort(new Order(Direction.DESC, "assessmentDate"))).forEach(e -> {
			ret.add(new PNSAssessmentDto(e, null, false));
		});

		return ret;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<PNSAssessmentDto> findAllPageable(PNSAssessmentFilterDto filter) {

		if (filter == null || filter.getUser() == null || CommonUtils.isEmpty(filter.getFacilityIds())) {
			return new PageImpl<>(new ArrayList<>());
		}

		if (filter.getPageIndex() < 0) {
			filter.setPageIndex(0);
		}

		if (filter.getPageSize() <= 0) {
			filter.setPageSize(25);
		}

		QUser qUser = QUser.user;
		QPNSAssessment q = QPNSAssessment.pNSAssessment;
		BooleanExpression be = q.id.isNotNull();
		Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize(),
				new Sort(new Order(Direction.DESC, "assessmentDate")));

		if (!CommonUtils.isEmpty(filter.getKeyword())) {
			be = be.and(q.ip.containsIgnoreCase(filter.getKeyword())
					.or(q.assessorName.containsIgnoreCase(filter.getKeyword()))
					.or(q.facility.name.containsIgnoreCase(filter.getKeyword()))
					.or(q.facility.address.province.name.containsIgnoreCase(filter.getKeyword())));
		}

		List<Long> ids = new ArrayList<>();
		Iterable<UserOrganization> ous = uoRepos.findAll(qUser.id.longValue().eq(filter.getUser().getId()));

		for (long id : filter.getFacilityIds()) {
			ous.forEach(uo -> {
				if (id == uo.getOrganization().getId()) {
					ids.add(uo.getOrganization().getId());
				}
			});
		}

		if (ids.size() <= 0) {
			ids.add(0l);
		}

		be = be.and(q.facility.id.longValue().in(ids));

		boolean includeOnlySubmitted = false;

		for (Role r : filter.getUser().getRoles()) {

			String roleName = r.getName();

			if (roleName.equalsIgnoreCase(Constants.ROLE_DONOR)
					|| r.getName().equalsIgnoreCase(Constants.ROLE_NATIONAL_MANAGER)
					|| r.getName().equalsIgnoreCase(Constants.ROLE_PROVINCIAL_MANAGER)
					|| r.getName().equalsIgnoreCase(Constants.ROLE_DISTRICT_MANAGER)) {
				includeOnlySubmitted = true;
			}
		}

		if (includeOnlySubmitted) {
			be = be.and(q.submitted.isTrue());
		}

		List<PNSAssessmentDto> content = new ArrayList<>();
		Page<PNSAssessment> page = repos.findAll(be, pageable);

		page.getContent().forEach(e -> {
			content.add(new PNSAssessmentDto(e, null, false));
		});

		return new PageImpl<>(content, pageable, page.getTotalElements());
	}

	@Override
	@Transactional(readOnly = true)
	public Workbook exportData(PNSAssessmentFilterDto filter) {

		if (filter == null || filter.getUser() == null || CommonUtils.isEmpty(filter.getFacilityIds())) {
			throw new RuntimeException();
		}

		QUser qUser = QUser.user;
		QPNSAssessment q = QPNSAssessment.pNSAssessment;
		BooleanExpression be = q.id.isNotNull();

		if (!CommonUtils.isEmpty(filter.getKeyword())) {
			be = be.and(q.ip.containsIgnoreCase(filter.getKeyword())
					.or(q.assessorName.containsIgnoreCase(filter.getKeyword()))
					.or(q.facility.name.containsIgnoreCase(filter.getKeyword()))
					.or(q.facility.address.province.name.containsIgnoreCase(filter.getKeyword())));
		}

		List<Long> ids = new ArrayList<>();
		Iterable<UserOrganization> ous = uoRepos.findAll(qUser.id.longValue().eq(filter.getUser().getId()));

		for (long id : filter.getFacilityIds()) {
			ous.forEach(uo -> {
				if (id == uo.getOrganization().getId()) {
					ids.add(uo.getOrganization().getId());
				}
			});
		}

		if (ids.size() <= 0) {
			return new XSSFWorkbook();
		}

		be = be.and(q.facility.id.longValue().in(ids));

		boolean includeOnlySubmitted = false;

		for (Role r : filter.getUser().getRoles()) {

			String roleName = r.getName();

			if (roleName.equalsIgnoreCase(Constants.ROLE_DONOR)
					|| r.getName().equalsIgnoreCase(Constants.ROLE_NATIONAL_MANAGER)
					|| r.getName().equalsIgnoreCase(Constants.ROLE_PROVINCIAL_MANAGER)
					|| r.getName().equalsIgnoreCase(Constants.ROLE_DISTRICT_MANAGER)) {
				includeOnlySubmitted = true;
			}
		}

		if (includeOnlySubmitted) {
			be = be.and(q.submitted.isTrue());
		}

		// TODO

		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public Workbook exportDataDetailed(Integer type) {

		User user = SecurityUtils.getCurrentUser();

		if (user != null && CommonUtils.isPositive(user.getId(), true)) {
			user = userRepos.findOne(user.getId());
		}

		if (user == null || !CommonUtils.isPositive(user.getId(), true)) {
			return new XSSFWorkbook();
		}

		QUser qUser = QUser.user;
		QPNSAssessment q = QPNSAssessment.pNSAssessment;
		BooleanExpression be = q.id.isNotNull();

		List<Long> ids = new ArrayList<>();
		Iterable<UserOrganization> ous = uoRepos.findAll(qUser.id.longValue().eq(user.getId()));

		ous.forEach(uo -> {
			ids.add(uo.getOrganization().getId());
		});

		if (ids.size() <= 0) {
			return new XSSFWorkbook();
		}

		be = be.and(q.facility.id.longValue().in(ids));

		boolean includeOnlySubmitted = false;

		for (Role r : user.getRoles()) {

			String roleName = r.getName();

			if (roleName.equalsIgnoreCase(Constants.ROLE_DONOR)
					|| r.getName().equalsIgnoreCase(Constants.ROLE_NATIONAL_MANAGER)
					|| r.getName().equalsIgnoreCase(Constants.ROLE_PROVINCIAL_MANAGER)
					|| r.getName().equalsIgnoreCase(Constants.ROLE_DISTRICT_MANAGER)) {
				includeOnlySubmitted = true;
			}
		}

		if (includeOnlySubmitted) {
			be = be.and(q.submitted.isTrue());
		}

		if (CommonUtils.isPositive(type, false)) {
			be = be.and(q.assessmentType.intValue().eq(type));
		}

		Iterator<PNSAssessment> entities = repos.findAll(be, new Sort(Direction.ASC, "facility.name")).iterator();

		// Generate excel file

		Workbook wbook = new SXSSFWorkbook(100);// new XSSFWorkbook();
		Sheet sheet = wbook.createSheet("Tổng hợp");

		// Display
		sheet.setZoom(100);

		//@formatter:off
		int[] colWidths = { 125, 235, 220, 200, 132, 120, 120, 120};
		//@formatter:on

		for (int i = 0; i < colWidths.length; i++) {
			sheet.setColumnWidth(i, ExcelUtils.pixel2WidthUnits(colWidths[i]));
		}

		// column width for all 33 criteria
		for (int i = 7; i < 41; i++) {
			sheet.setColumnWidth(i, ExcelUtils.pixel2WidthUnits(100));
		}

		Font font = wbook.createFont();
		CellStyle cellStyle = wbook.createCellStyle();
		CellStyle dateCellStyle = wbook.createCellStyle();

		font.setFontName("Calibri");
		font.setFontHeightInPoints((short) 11);
		font.setBold(true);
		font.setColor(IndexedColors.BLACK.getIndex());

		cellStyle.setFont(font);
		cellStyle.setWrapText(true);
		cellStyle.setAlignment(HorizontalAlignment.LEFT);
		cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		setBorders4Style(cellStyle);

		int rowIndex = 0;
		Row row = sheet.createRow(rowIndex);
		row.setHeightInPoints(22);

		//@formatter:off
		String[] arr = new String[] {"a,b,c,d", "a,b,c,d,e", "a,b,c,d,e,f", "a,b", "a,b,c,d,e,f", "a,b,c,d", "a,b,c,d,e,f"};
		StringBuffer sb = new StringBuffer("Tỉnh/thành phố,Đối tác triển khai,Cơ sở,Loại hình đánh giá,Ngày đánh giá,Điểm đạt được,Số tư vấn viên cho xét nghiệm HIV,Số tư vấn viên cho TBXNBT/BC");
		//@formatter:on

		int criteriaIndex = 0;
		for (String s : arr) {
			String[] arr2 = s.split(",");

			criteriaIndex++;

			for (String s2 : arr2) {
				sb.append(",Tiêu chí 1_" + criteriaIndex + s2);
			}
		}

		arr = sb.toString().split(",");

		Cell cell = null;

		for (int i = 0; i < arr.length; i++) {
			cell = row.createCell(i, CellType.STRING);
			cell.setCellValue(arr[i]);
			cell.setCellStyle(cellStyle);
		}

		// No data...
		if (!entities.hasNext()) {
			return wbook;
		}

		font = wbook.createFont();
		cellStyle = wbook.createCellStyle();

		font.setFontName("Calibri");
		font.setFontHeightInPoints((short) 12);
		font.setColor(IndexedColors.BLACK.getIndex());

		cellStyle.setFont(font);
		cellStyle.setWrapText(true);
		cellStyle.setAlignment(HorizontalAlignment.LEFT);
		cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		cellStyle.setIndention((short) 1);
		setBorders4Style(cellStyle);

		dateCellStyle.cloneStyleFrom(cellStyle);

		DataFormat format = wbook.createDataFormat();
		dateCellStyle.setDataFormat(format.getFormat("dd/MM/yyyy"));

		while (entities.hasNext()) {
			PNSAssessment entity = entities.next();
			PNSAssessmentDto dto = new PNSAssessmentDto(entity, null, false);
			int colIndex = 0;

			rowIndex++;
			row = sheet.createRow(rowIndex);
			row.setHeightInPoints(22);

			// Tỉnh/thành phố
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			if (entity.getFacility() != null && entity.getFacility().getAddress() != null
					&& entity.getFacility().getAddress().getProvince() != null) {
				cell.setCellValue(entity.getFacility().getAddress().getProvince().getName());
			} else {
				cell.setCellValue("-");
			}

			// Đối tác triển khai
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(dto.getIpName());

			// Cơ sở
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			if (entity.getFacility() != null) {
				cell.setCellValue(entity.getFacility().getName());
			} else {
				cell.setCellValue("-");
			}

			// Loại hình đánh giá
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(dto.getAssessmentTypeText());

			// Ngày đánh giá
			cell = row.createCell(colIndex++, CellType.NUMERIC);
			cell.setCellStyle(dateCellStyle);
			cell.setCellValue(CommonUtils.fromLocalDateTime(dto.getAssessmentDate()));

			// Điểm đạt được
			cell = row.createCell(colIndex++, CellType.NUMERIC);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(dto.getFinalScore());

			// Số tư vấn viên làm HTS
			cell = row.createCell(colIndex++, CellType.NUMERIC);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(entity.getCounselorCount());

			// Số tư vấn viên làm TBXNBTBC
			cell = row.createCell(colIndex++, CellType.NUMERIC);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(entity.getCounselor4Pns());

			// Tiêu chí 1a
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(entity.getQ1_1a() == 1 ? "x" : "");

			// Tiêu chí 1b
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(entity.getQ1_1b() == 1 ? "x" : "");

			// Tiêu chí 1c
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(entity.getQ1_1c() == 1 ? "x" : "");

			// Tiêu chí 1d
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(entity.getQ1_1d() == 1 ? "x" : "");

			// Tiêu chí 2a
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(entity.getQ1_2a() == 1 ? "x" : "");

			// Tiêu chí 2b
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(entity.getQ1_2b() == 1 ? "x" : "");

			// Tiêu chí 2c
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(entity.getQ1_2c() == 1 ? "x" : "");

			// Tiêu chí 2d
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(entity.getQ1_2d() == 1 ? "x" : "");

			// Tiêu chí 2e
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue((entity.getQ1_2e() != null && entity.getQ1_2e() == 1) ? "x" : "");

			// Tiêu chí 3a
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(entity.getQ1_3a() == 1 ? "x" : "");

			// Tiêu chí 3b
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(entity.getQ1_3b() == 1 ? "x" : "");

			// Tiêu chí 3c
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(entity.getQ1_3c() == 1 ? "x" : "");

			// Tiêu chí 3d
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(entity.getQ1_3d() == 1 ? "x" : "");

			// Tiêu chí 3e
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(entity.getQ1_3e() == 1 ? "x" : "");

			// Tiêu chí 3f
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue((entity.getQ1_3f() != null && entity.getQ1_3f() == 1) ? "x" : "");

			// Tiêu chí 4a
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(entity.getQ1_4a() == 1 ? "x" : "");

			// Tiêu chí 4b
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(entity.getQ1_4b() == 1 ? "x" : "");

			// Tiêu chí 5a
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(entity.getQ1_5a() == 1 ? "x" : "");

			// Tiêu chí 5b
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(entity.getQ1_5b() == 1 ? "x" : "");

			// Tiêu chí 5c
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(entity.getQ1_5c() == 1 ? "x" : "");

			// Tiêu chí 5d
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(entity.getQ1_5d() == 1 ? "x" : "");

			// Tiêu chí 5e
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(entity.getQ1_5e() == 1 ? "x" : "");

			// Tiêu chí 5f
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(entity.getQ1_5f() == 1 ? "x" : "");

			// Tiêu chí 6a
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(entity.getQ1_6a() == 1 ? "x" : "");

			// Tiêu chí 6b
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(entity.getQ1_6b() == 1 ? "x" : "");

			// Tiêu chí 6c
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(entity.getQ1_6c() == 1 ? "x" : "");

			// Tiêu chí 6d
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(entity.getQ1_6d() == 1 ? "x" : "");

			// Tiêu chí 7a
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(entity.getQ1_7a() == 1 ? "x" : "");

			// Tiêu chí 7b
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(entity.getQ1_7b() == 1 ? "x" : "");

			// Tiêu chí 7c
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(entity.getQ1_7c() == 1 ? "x" : "");

			// Tiêu chí 7d
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(entity.getQ1_7d() == 1 ? "x" : "");

			// Tiêu chí 7e
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(entity.getQ1_7e() == 1 ? "x" : "");

			// Tiêu chí 7f
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(entity.getQ1_7f() == 1 ? "x" : "");
		}

		// Auto-filter
		if (rowIndex > 1) {
			sheet.setAutoFilter(CellRangeAddress.valueOf("A1:AO" + (rowIndex + 1)));
		}

		return wbook;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Workbook generateFacilityReport(Long id) {

		PNSAssessment entity = repos.findOne(id);

		if (entity == null) {
			throw new RuntimeException("Invalid arguments!");
		}

		PNSAssessmentDto dto = new PNSAssessmentDto(entity, null, false);
		Workbook wbook = new XSSFWorkbook();
		Sheet sheet = wbook.createSheet("Báo cáo");

		// Display
		sheet.setZoom(100);
		sheet.setFitToPage(true);

		// Printer settings
		PrintSetup ps = sheet.getPrintSetup();
		ps.setPaperSize(PrintSetup.A4_PAPERSIZE);
		ps.setLandscape(false);
		ps.setFitWidth((short) 1);
		ps.setFitHeight((short) 0);
		ps.setScale((short) 100);

		sheet.setColumnWidth(0, ExcelUtils.pixel2WidthUnits(306));
		sheet.setColumnWidth(1, ExcelUtils.pixel2WidthUnits(306));

		Font font = wbook.createFont();
		Font font2 = wbook.createFont();
		Font font3 = wbook.createFont();
		CellStyle cellStyle = wbook.createCellStyle();
		CellStyle cs = wbook.createCellStyle();

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		font.setFontName("Times New Roman");
		font.setFontHeightInPoints((short) 12);
		font.setBold(true);
		font.setColor(IndexedColors.BLACK.getIndex());

		font2.setFontName("Times New Roman");
		font2.setFontHeightInPoints((short) 12);
		font2.setBold(false);
		font2.setColor(IndexedColors.BLACK.getIndex());

		font3.setFontName("Times New Roman");
		font3.setFontHeightInPoints((short) 12);
		font3.setBold(true);
		font3.setColor(IndexedColors.WHITE.getIndex());

		cellStyle.setFont(font);
		cellStyle.setWrapText(true);
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		cellStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		int rowIndex = 0;

		Row row = sheet.createRow(rowIndex);
		row.setHeightInPoints(30);

		Cell cell = row.createCell(0, CellType.STRING);
		cell.setCellValue("KẾT QUẢ ĐÁNH GIÁ CƠ SỞ CUNG CẤP DỊCH VỤ TBXNBT/BC");
		sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 1));

		cs.cloneStyleFrom(cellStyle);
		cell.setCellStyle(cs);

		// blank row
		rowIndex++;
		row = sheet.createRow(rowIndex);
		row.setHeightInPoints(15);

		// Facility name
		rowIndex++;
		row = sheet.createRow(rowIndex);
		row.setHeightInPoints(22);

		cs = wbook.createCellStyle();
		cs.cloneStyleFrom(cellStyle);
		cs.setFont(font2);
		cs.setAlignment(HorizontalAlignment.LEFT);

		cell = row.createCell(0, CellType.STRING);
		cell.setCellValue("Tên cơ sở: " + dto.getFacility().getName());
		sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 1));
		cell.setCellStyle(cs);

		// IP
		rowIndex++;
		row = sheet.createRow(rowIndex);
		row.setHeightInPoints(22);

		cell = row.createCell(0, CellType.STRING);
		cell.setCellValue("Đối tác triển khai: " + dto.getIpName());
		sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 1));
		cell.setCellStyle(cs);

		// Assessment date
		rowIndex++;
		row = sheet.createRow(rowIndex);
		row.setHeightInPoints(22);

		cell = row.createCell(0, CellType.STRING);
		cell.setCellValue("Ngày đánh giá: " + sdf.format(CommonUtils.fromLocalDateTime(dto.getAssessmentDate())));
		sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 1));
		cell.setCellStyle(cs);

		// Type of assessment
		rowIndex++;
		row = sheet.createRow(rowIndex);
		row.setHeightInPoints(22);

		cell = row.createCell(0, CellType.STRING);
		cell.setCellValue("Loại hình đánh giá: " + dto.getAssessmentTypeText());
		sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 1));
		cell.setCellStyle(cs);

		// # of staff providing HTS services
		rowIndex++;
		row = sheet.createRow(rowIndex);
		row.setHeightInPoints(22);

		cell = row.createCell(0, CellType.STRING);
		cell.setCellValue("Số nhân viên tư vấn xét nghiệm HIV tại cơ sở: " + dto.getCounselorCount());
		sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 1));
		cell.setCellStyle(cs);

		// # of staff providing PNS services
		rowIndex++;
		row = sheet.createRow(rowIndex);
		row.setHeightInPoints(22);

		cell = row.createCell(0, CellType.STRING);
		cell.setCellValue("Số nhân viên tư vấn dịch vụ TBXNBT/BC tại cơ sở: " + dto.getCounselor4Pns());
		sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 1));
		cell.setCellStyle(cs);

		// conclusion
		cs = wbook.createCellStyle();
		cs.cloneStyleFrom(cellStyle);
		cs.setAlignment(HorizontalAlignment.LEFT);
		cs.setFont(font);

		rowIndex++;
		row = sheet.createRow(rowIndex);
		row.setHeightInPoints(22);

		cell = row.createCell(0, CellType.STRING);
		cell.setCellValue("Kết luận: " + dto.getConclusion());
		sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 1));
		cell.setCellStyle(cs);

		cs = wbook.createCellStyle();
		cs.cloneStyleFrom(cellStyle);
		cs.setAlignment(HorizontalAlignment.LEFT);
		cs.setFont(font);

		if (dto.getFinalScore() < 33) {
			// List of failed CEEs
			rowIndex++;
			row = sheet.createRow(rowIndex);
			row.setHeightInPoints(30);

			cell = row.createCell(0, CellType.STRING);
			cell.setCellValue("Danh sách các tiêu chuẩn không đạt");
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 1));
			cell.setCellStyle(cs);

			rowIndex++;
			row = sheet.createRow(rowIndex);
			row.setHeightInPoints(22);

			cs = wbook.createCellStyle();
			cs.cloneStyleFrom(cellStyle);
			cs.setFont(font3);
			cs.setAlignment(HorizontalAlignment.LEFT);
			cs.setFillForegroundColor(IndexedColors.BLACK.getIndex());
			cs.setIndention((short) 1);

			cell = row.createCell(0, CellType.STRING);
			cell.setCellValue("Tiêu chuẩn không đạt");
			cell.setCellStyle(cs);

			cell = row.createCell(1, CellType.STRING);
			cell.setCellValue("Mô tả thêm");
			cell.setCellStyle(cs);

			cs = wbook.createCellStyle();
			cs.cloneStyleFrom(cellStyle);
			cs.setFont(font2);
			cs.setAlignment(HorizontalAlignment.LEFT);
			cs.setIndention((short) 1);

			// --> List of failed CEEs
			List<PNSAssessmentDto.FailedCEE4Report> failedCEEs = dto.getFailedCEE4Reports();
			for (PNSAssessmentDto.FailedCEE4Report cee : failedCEEs) {
				rowIndex++;
				row = sheet.createRow(rowIndex);
				row.setHeightInPoints(-1);

				cell = row.createCell(0, CellType.STRING);
				cell.setCellValue(cee.getCee());
				cell.setCellStyle(cs);

				cell = row.createCell(1, CellType.STRING);
				cell.setCellValue(!CommonUtils.isEmpty(cee.getExplanation()) ? cee.getExplanation() : "");
				cell.setCellStyle(cs);

				setBorders4Region("A" + (rowIndex + 1) + ":" + "A" + (rowIndex + 1), sheet);
				setBorders4Region("B" + (rowIndex + 1) + ":" + "B" + (rowIndex + 1), sheet);
			}

			// Correction plan
			//@formatter:off
			cs = wbook.createCellStyle();
			cs.cloneStyleFrom(cellStyle);
			cs.setAlignment(HorizontalAlignment.LEFT);
			cs.setFont(font);

			rowIndex++;
			row = sheet.createRow(rowIndex);
			row.setHeightInPoints(30);
			
			cell = row.createCell(0, CellType.STRING);
			cell.setCellValue("Kế hoạch và khung thời gian cải thiện và thay đổi để đạt được tất cả 33 tiêu chuẩn tối thiểu:");
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 1));
			cell.setCellStyle(cs);
			//@formatter:on

			// Content of correction plan
			cs = wbook.createCellStyle();
			cs.cloneStyleFrom(cellStyle);
			cs.setAlignment(HorizontalAlignment.LEFT);
			cs.setFont(font2);

			rowIndex++;
			row = sheet.createRow(rowIndex);
			row.setHeightInPoints(-1);

			cell = row.createCell(0, CellType.STRING);
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 1));
			cell.setCellStyle(cs);

			if (!CommonUtils.isEmpty(dto.getCorrectionPlan())) {
				cell.setCellValue(dto.getCorrectionPlan());
				adjustRowHeightForRowWithMergedCells(row, dto.getCorrectionPlan());
			} else {
				cell.setCellValue("-");
			}
		}

		// Other recommendations
		cs = wbook.createCellStyle();
		cs.cloneStyleFrom(cellStyle);
		cs.setAlignment(HorizontalAlignment.LEFT);
		cs.setFont(font);

		rowIndex++;
		row = sheet.createRow(rowIndex);
		row.setHeightInPoints(30);

		cell = row.createCell(0, CellType.STRING);
		cell.setCellValue("Các nội dung cần bổ sung khác:");
		sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 1));
		cell.setCellStyle(cs);

		// Content of other recommendations
		cs = wbook.createCellStyle();
		cs.cloneStyleFrom(cellStyle);
		cs.setAlignment(HorizontalAlignment.LEFT);
		cs.setFont(font2);

		rowIndex++;
		row = sheet.createRow(rowIndex);
		row.setHeightInPoints(-1);

		cell = row.createCell(0, CellType.STRING);
		sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 1));
		cell.setCellStyle(cs);

		if (!CommonUtils.isEmpty(dto.getAdditionalGaps())) {
			cell.setCellValue(dto.getAdditionalGaps());
			adjustRowHeightForRowWithMergedCells(row, dto.getAdditionalGaps());
		} else {
			cell.setCellValue("-");
		}

		// blank row
		rowIndex++;
		row = sheet.createRow(rowIndex);
		row.setHeightInPoints(15);

		// Signatures
		rowIndex++;
		row = sheet.createRow(rowIndex);
		row.setHeightInPoints(22);

		cs = wbook.createCellStyle();
		cs.cloneStyleFrom(cellStyle);
		cs.setFont(font);
		cs.setAlignment(HorizontalAlignment.CENTER);

		cell = row.createCell(0, CellType.STRING);
		cell.setCellValue("Cán bộ đánh giá");
		cell.setCellStyle(cs);

		cell = row.createCell(1, CellType.STRING);
		cell.setCellValue("Đại diện cơ sở");
		cell.setCellStyle(cs);

		cs = wbook.createCellStyle();
		cs.cloneStyleFrom(cellStyle);
		cs.setFont(font2);

		rowIndex++;
		row = sheet.createRow(rowIndex);
		row.setHeightInPoints(22);

		cell = row.createCell(0, CellType.STRING);
		cell.setCellValue("(Ký và gi rõ họ tên)");
		cell.setCellStyle(cs);

		cell = row.createCell(1, CellType.STRING);
		cell.setCellValue("(Ký và ghi rõ họ tên)");
		cell.setCellStyle(cs);

		return wbook;
	}

	@Override
	@Transactional(readOnly = true)
	public PNSAssessmentPreferencesDto getPreferences() {

		PNSAssessmentPreferencesDto dto = new PNSAssessmentPreferencesDto();
		QPreferences q = QPreferences.preferences;

		Preferences entity = prefRepos.findOne(q.name.equalsIgnoreCase(Constants.PROP_PNS_BASELINE_TO_DATE));
		if (entity != null && !CommonUtils.isEmpty(entity.getValue())) {
			LocalDateTime date = LocalDateTime.parse(entity.getValue());
			dto.setBaselineToDate(date);
		}

		entity = prefRepos.findOne(q.name.equalsIgnoreCase(Constants.PROP_PNS_POST_FROM_DATE));
		if (entity != null && !CommonUtils.isEmpty(entity.getValue())) {
			LocalDateTime date = LocalDateTime.parse(entity.getValue());
			dto.setPostFromDate(date);
		}

		entity = prefRepos.findOne(q.name.equalsIgnoreCase(Constants.PROP_PNS_POST_TO_DATE));
		if (entity != null && !CommonUtils.isEmpty(entity.getValue())) {
			LocalDateTime date = LocalDateTime.parse(entity.getValue());
			dto.setPostToDate(date);
		}

		return dto;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public PNSAssessmentPreferencesDto setPreferences(PNSAssessmentPreferencesDto pref) {
		if (pref == null || pref.getBaselineToDate() == null || pref.getPostFromDate() == null
				|| pref.getPostToDate() == null) {
			throw new RuntimeException();
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		QPreferences q = QPreferences.preferences;

		Preferences entity = prefRepos.findOne(q.name.equalsIgnoreCase(Constants.PROP_PNS_BASELINE_TO_DATE));
		if (entity == null) {
			entity = new Preferences();
			entity.setName(Constants.PROP_PNS_BASELINE_TO_DATE);
		}

		entity.setValue(sdf.format(CommonUtils.fromLocalDateTime(pref.getBaselineToDate())));
		prefRepos.save(entity);

		entity = prefRepos.findOne(q.name.equalsIgnoreCase(Constants.PROP_PNS_POST_FROM_DATE));
		if (entity == null) {
			entity = new Preferences();
			entity.setName(Constants.PROP_PNS_POST_FROM_DATE);
		}

		entity.setValue(sdf.format(CommonUtils.fromLocalDateTime(pref.getPostFromDate())));
		prefRepos.save(entity);

		entity = prefRepos.findOne(q.name.equalsIgnoreCase(Constants.PROP_PNS_POST_TO_DATE));
		if (entity == null) {
			entity = new Preferences();
			entity.setName(Constants.PROP_PNS_POST_TO_DATE);
		}

		entity.setValue(sdf.format(CommonUtils.fromLocalDateTime(pref.getPostToDate())));
		prefRepos.save(entity);

		return pref;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public PNSAssessmentDto saveAttachment(PNSAssessmentDto dto, String propName) {
		if (dto == null || !CommonUtils.isPositive(dto.getId(), true) || CommonUtils.isEmpty(propName)) {
			throw new RuntimeException();
		}

		PNSAssessment entity = repos.findOne(dto.getId());

		if (entity == null) {
			throw new RuntimeException();
		}

		switch (propName) {
			case "q1_6b_file":
				entity.setQ1_6b_file(dto.getQ1_6b_file());
				entity.setQ1_6b_file_contentLength(dto.getQ1_6b_file_contentLength());
				entity.setQ1_6b_file_name(dto.getQ1_6b_file_name());
				break;
			case "q1_7_file":
				entity.setQ1_7_file(dto.getQ1_7_file());
				entity.setQ1_7_file_contentLength(dto.getQ1_7_file_contentLength());
				entity.setQ1_7_file_name(dto.getQ1_7_file_name());
				break;
			case "q2_2_file":
				entity.setQ2_2_file(dto.getQ2_2_file());
				entity.setQ2_2_file_contentLength(dto.getQ2_2_file_contentLength());
				entity.setQ2_2_file_name(dto.getQ2_2_file_name());
				break;
			case "q2_6_file":
				entity.setQ2_6_file(dto.getQ2_6_file());
				entity.setQ2_6_file_contentLength(dto.getQ2_6_file_contentLength());
				entity.setQ2_6_file_name(dto.getQ2_6_file_name());
				break;
			case "q3_3_file":
				entity.setQ3_3_file(dto.getQ3_3_file());
				entity.setQ3_3_file_contentLength(dto.getQ3_3_file_contentLength());
				entity.setQ3_3_file_name(dto.getQ3_3_file_name());
				break;
		}

		entity = repos.save(entity);

		if (entity != null) {
			return new PNSAssessmentDto(entity, null, false);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	@Transactional(readOnly = true)
	public boolean hasBaseline(Long facilityId) {

		if (!CommonUtils.isPositive(facilityId, true)) {
			throw new RuntimeException("Cannot check for baseline data!");
		}

		QPNSAssessment q = QPNSAssessment.pNSAssessment;
		QPreferences qPref = QPreferences.preferences;
		boolean hasBaseline = false;

		String baselineToString = prefRepos.findOne(qPref.name.equalsIgnoreCase(Constants.PROP_PNS_BASELINE_TO_DATE))
				.getValue();

		LocalDateTime baselineFrom = LocalDateTime.of(2020, 05, 18, 0, 0, 0);
		LocalDateTime baselineTo = LocalDateTime.parse(baselineToString);

		// avoid counting (demo) entries that were created before official date
		Iterator<PNSAssessment> itr = repos.findAll(q.facility.id.longValue().eq(facilityId)
				.and(q.assessmentType.eq(0).and(q.createDate.goe(baselineFrom)))).iterator();
		hasBaseline = itr.hasNext();

		LocalDateTime todayStart = CommonUtils.hanoiTodayStart();

		// --> Already has baseline assessment before the expired date
		if (hasBaseline) {
			if ((todayStart.isEqual(baselineFrom) || todayStart.isAfter(baselineFrom))
					&& (todayStart.isEqual(baselineTo) || todayStart.isBefore(baselineTo))) {
				return true;
			}
		}

		return false;
	}

	@Override
	@Transactional(readOnly = true)
	public boolean hasPost(Long facilityId) {

		if (!CommonUtils.isPositive(facilityId, true)) {
			throw new RuntimeException("Cannot check for post intervention data!");
		}

		QPNSAssessment q = QPNSAssessment.pNSAssessment;
		QPreferences qPref = QPreferences.preferences;
		boolean hasPost = false;

		String postFromString = prefRepos.findOne(qPref.name.equalsIgnoreCase(Constants.PROP_PNS_POST_FROM_DATE))
				.getValue();
		String postToString = prefRepos.findOne(qPref.name.equalsIgnoreCase(Constants.PROP_PNS_POST_TO_DATE))
				.getValue();

		LocalDateTime postFrom = LocalDateTime.parse(postFromString);
		LocalDateTime postTo = LocalDateTime.parse(postToString);

		Iterator<PNSAssessment> itr = repos
				.findAll(q.facility.id.longValue().eq(facilityId).and(q.assessmentType.eq(2))).iterator();
		hasPost = itr.hasNext();

		LocalDateTime todayStart = CommonUtils.hanoiTodayStart();

		// --> Already has post assessment during the set dates
		if (hasPost) {
			if ((todayStart.isEqual(postFrom) || todayStart.isAfter(postFrom))
					&& (todayStart.isEqual(postTo) || todayStart.isBefore(postTo))) {
				return true;
			}
		}

		return false;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public PNSAssessmentDto saveOne(PNSAssessmentDto dto) {

		//@formatter:off
		if (dto == null 
			|| dto.getFacility() == null 
			|| !CommonUtils.isPositive(dto.getFacility().getId(), true)
			|| CommonUtils.isEmpty(dto.getIp())
			|| CommonUtils.isEmpty(dto.getAssessorName())
			|| CommonUtils.isEmpty(dto.getAssessorEmail())
			|| CommonUtils.isNull(dto.getAssessmentDate())
			|| CommonUtils.isEmpty(dto.getFacilityPocName())
			|| (dto.getCounselorCount() < 0)
			|| (dto.getCounselor4Pns() < 0)) {
			
			throw new RuntimeException("Invalid arguments!");
		}
		//@formatter:on

		User currentUser = SecurityUtils.getCurrentUser();
		if (currentUser == null) {
			throw new RuntimeException("Invalid user session!");
		}

		QPreferences qPref = QPreferences.preferences;
		QPNSAssessment q = QPNSAssessment.pNSAssessment;
		PNSAssessment entity = null;

		if (CommonUtils.isPositive(dto.getId(), true)) {
			entity = repos.findOne(dto.getId());
		}

		//@formatter:off
		boolean hasBaseline = false;
		boolean hasPost = false;
		
		String baselineToString = prefRepos.findOne(qPref.name.equalsIgnoreCase(Constants.PROP_PNS_BASELINE_TO_DATE)).getValue();
		String postFromString =  prefRepos.findOne(qPref.name.equalsIgnoreCase(Constants.PROP_PNS_POST_FROM_DATE)).getValue();
		String postToString =  prefRepos.findOne(qPref.name.equalsIgnoreCase(Constants.PROP_PNS_POST_TO_DATE)).getValue();
		
		LocalDateTime baselineFrom = LocalDateTime.of(2020, 05, 18, 0, 0, 0);
		LocalDateTime baselineTo = LocalDateTime.parse(baselineToString);
		LocalDateTime postFrom = LocalDateTime.parse(postFromString);
		LocalDateTime postTo = LocalDateTime.parse(postToString);

		// avoid counting (demo) entries that were created before official date 
		Iterator<PNSAssessment> itr = repos.findAll(q.facility.id.longValue().eq(dto.getFacility().getId()).and(q.assessmentType.eq(0).and(q.createDate.goe(baselineFrom)))).iterator();
		hasBaseline = itr.hasNext();

		itr = repos.findAll(q.facility.id.longValue().eq(dto.getFacility().getId()).and(q.assessmentType.eq(2))).iterator();
		hasPost = itr.hasNext();

		LocalDateTime todayStart = CommonUtils.hanoiTodayStart();
		
		// --> Already has baseline assessment before the expired date
		if (hasBaseline && entity == null) {
			if ((todayStart.isEqual(baselineFrom) || todayStart.isAfter(baselineFrom)) && (todayStart.isEqual(baselineTo) || todayStart.isBefore(baselineTo))) {
				return new PNSAssessmentDto();
			}
		}
		
		// --> Already has post assessment during the set dates
		if (hasPost && entity == null) {
			if ((todayStart.isEqual(postFrom) || todayStart.isAfter(postFrom)) && (todayStart.isEqual(postTo) || todayStart.isBefore(postTo))) {
				return new PNSAssessmentDto();
			}
		}
		
		//@formatter:on

		if (entity == null) {
			entity = dto.toEntity();
			entity.setSubmitted(false);

			// assessment type
			if (hasBaseline) {

				if (todayStart.isBefore(postFrom)) {
					entity.setAssessmentType(1); // mid-term
				} else {
					if (!hasPost) {
						entity.setAssessmentType(2); // post
					} else {
						entity.setAssessmentType(3); // routine
					}
				}
			} // else -> fall thru, because the assessment type is by default 0

		} else {
			// General information
			entity.setIp(dto.getIp());
			entity.setAssessorName(dto.getAssessorName());
			entity.setAssessorEmail(dto.getAssessorEmail());
			entity.setAssessmentDate(dto.getAssessmentDate());
			entity.setFacilityPocName(dto.getFacilityPocName());
			entity.setCounselorCount(dto.getCounselorCount());
			entity.setCounselor4Pns(dto.getCounselor4Pns());

			// Section 1

			// --> Question 1.1
			entity.setQ1_1a(dto.getQ1_1a());
			entity.setQ1_1a_text(dto.getQ1_1a_text());
			entity.setQ1_1b(dto.getQ1_1b());
			entity.setQ1_1b_text(dto.getQ1_1b_text());
			entity.setQ1_1c(dto.getQ1_1c());
			entity.setQ1_1c_text(dto.getQ1_1c_text());
			entity.setQ1_1d(dto.getQ1_1d());
			entity.setQ1_1d_text(dto.getQ1_1d_text());

			// --> Question 1.2
			entity.setQ1_2a(dto.getQ1_2a());
			entity.setQ1_2a_text(dto.getQ1_2a_text());
			entity.setQ1_2b(dto.getQ1_2b());
			entity.setQ1_2b_text(dto.getQ1_2b_text());
			entity.setQ1_2c(dto.getQ1_2c());
			entity.setQ1_2c_text(dto.getQ1_2c_text());
			entity.setQ1_2d(dto.getQ1_2d());
			entity.setQ1_2d_text(dto.getQ1_2d_text());
			entity.setQ1_2e(dto.getQ1_2e());
			entity.setQ1_2e_text(dto.getQ1_2e_text());

			// --> Question 1.3
			entity.setQ1_3a(dto.getQ1_3a());
			entity.setQ1_3a_text(dto.getQ1_3a_text());
			entity.setQ1_3b(dto.getQ1_3b());
			entity.setQ1_3b_text(dto.getQ1_3b_text());
			entity.setQ1_3c(dto.getQ1_3c());
			entity.setQ1_3c_text(dto.getQ1_3c_text());
			entity.setQ1_3d(dto.getQ1_3d());
			entity.setQ1_3d_text(dto.getQ1_3d_text());
			entity.setQ1_3e(dto.getQ1_3e());
			entity.setQ1_3e_text(dto.getQ1_3e_text());
			entity.setQ1_3f(dto.getQ1_3f());
			entity.setQ1_3f_text(dto.getQ1_3f_text());

			// --> Question 1.4
			entity.setQ1_4a(dto.getQ1_4a());
			entity.setQ1_4a_text(dto.getQ1_4a_text());
			entity.setQ1_4b(dto.getQ1_4b());
			entity.setQ1_4b_text(dto.getQ1_4b_text());

			// --> Question 1.5
			entity.setQ1_5a(dto.getQ1_5a());
			entity.setQ1_5a_text(dto.getQ1_5a_text());
			entity.setQ1_5b(dto.getQ1_5b());
			entity.setQ1_5b_text(dto.getQ1_5b_text());
			entity.setQ1_5c(dto.getQ1_5c());
			entity.setQ1_5c_text(dto.getQ1_5c_text());
			entity.setQ1_5d(dto.getQ1_5d());
			entity.setQ1_5d_text(dto.getQ1_5d_text());
			entity.setQ1_5e(dto.getQ1_5e());
			entity.setQ1_5e_text(dto.getQ1_5e_text());
			entity.setQ1_5f(dto.getQ1_5f());
			entity.setQ1_5f_text(dto.getQ1_5f_text());

			// --> Question 1.6
			entity.setQ1_6a(dto.getQ1_6a());
			entity.setQ1_6a_text(dto.getQ1_6a_text());
			entity.setQ1_6b(dto.getQ1_6b());
			entity.setQ1_6b_text(dto.getQ1_6b_text());
			entity.setQ1_6c(dto.getQ1_6c());
			entity.setQ1_6c_text(dto.getQ1_6c_text());
			entity.setQ1_6d(dto.getQ1_6d());
			entity.setQ1_6d_text(dto.getQ1_6d_text());

			// --> Question 1.7
			entity.setQ1_7a(dto.getQ1_7a());
			entity.setQ1_7a_text(dto.getQ1_7a_text());
			entity.setQ1_7b(dto.getQ1_7b());
			entity.setQ1_7b_text(dto.getQ1_7b_text());
			entity.setQ1_7c(dto.getQ1_7c());
			entity.setQ1_7c_text(dto.getQ1_7c_text());
			entity.setQ1_7c_text2(dto.getQ1_7c_text2());
			entity.setQ1_7d(dto.getQ1_7d());
			entity.setQ1_7d_text(dto.getQ1_7d_text());
			entity.setQ1_7e(dto.getQ1_7e());
			entity.setQ1_7e_text(dto.getQ1_7e_text());
			entity.setQ1_7e_text2(dto.getQ1_7e_text2());
			entity.setQ1_7f(dto.getQ1_7f());
			entity.setQ1_7f_text(dto.getQ1_7f_text());

			// Section 2
			entity.setQ2_1(dto.getQ2_1());
			entity.setQ2_2(dto.getQ2_2());

			// Delimited to contain multiple answers
			entity.setQ2_3(dto.getQ2_3());
			entity.setQ2_3_text(dto.getQ2_3_text());
			entity.setQ2_4(dto.getQ2_4());
			entity.setQ2_5(dto.getQ2_5());

			// Delimited to contain multiple answers
			entity.setQ2_6(dto.getQ2_6());
			entity.setQ2_6_text(dto.getQ2_6_text());

			// Section 3
			entity.setQ3_1(dto.getQ3_1());
			entity.setQ3_2(dto.getQ3_2());
			entity.setQ3_3(dto.getQ3_3());

			// Delimited to contain multiple answers
			entity.setQ3_4(dto.getQ3_4());
			entity.setQ3_4_text(dto.getQ3_4_text());
			entity.setQ3_5(dto.getQ3_5());

			// Section 4

			// Delimited to contain multiple answers
			entity.setQ4_1(dto.getQ4_1());
			entity.setQ4_2(dto.getQ4_2());
			entity.setQ4_3(dto.getQ4_3());
			entity.setQ4_3_text(dto.getQ4_3_text());

			// Section 5

			// Delimited to contain multiple answers
			entity.setQ5_1(dto.getQ5_1());

			// Delimited to contain multiple answers
			entity.setQ5_2(dto.getQ5_2());
			entity.setQ5_3(dto.getQ5_3());

			// Final score
			entity.setFinalScore(dto.getFinalScore());
			//@formatter:off
			entity.setCorrectionPlan(!CommonUtils.isEmpty(dto.getCorrectionPlan()) ? htmlUtils.removeXSSThreatsAlt(dto.getCorrectionPlan()) : "");
			entity.setAdditionalGaps(!CommonUtils.isEmpty(dto.getAdditionalGaps()) ? htmlUtils.removeXSSThreatsAlt(dto.getAdditionalGaps()) : "");
			//@formatter:on
		}

		Organization facility = orgRepos.findOne(dto.getFacility().getId());

		if (facility == null) {
			throw new RuntimeException();
		}

		entity.setFacility(facility);

		entity = repos.save(entity);

		if (entity != null) {
			return new PNSAssessmentDto(entity, null, false);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public PNSAssessmentDto submit(PNSAssessmentDto dto) {

		if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
			throw new RuntimeException();
		}

		PNSAssessment entity = repos.findOne(dto.getId());

		if (entity == null) {
			throw new RuntimeException();
		}

		entity.setSubmitted(dto.getSubmitted());

		entity = repos.save(entity);

		if (entity != null) {
			return new PNSAssessmentDto(entity, null, false);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteMultiple(PNSAssessmentDto[] dtos) {
		if (CommonUtils.isEmpty(dtos)) {
			return;
		}

		for (PNSAssessmentDto dto : dtos) {
			if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
				continue;
			}
			PNSAssessment entity = repos.findOne(dto.getId());

			if (entity != null) {
				repos.delete(entity);
			}
		}
	}

	private void adjustRowHeightForRowWithMergedCells(Row row, String cellValue) {
		java.awt.Font currFont = new java.awt.Font("Times New Roman", 0, 12);
		AttributedString attrStr = new AttributedString(cellValue);
		attrStr.addAttribute(TextAttribute.FONT, currFont);

		FontRenderContext frc = new FontRenderContext(null, true, true);
		LineBreakMeasurer measurer = new LineBreakMeasurer(attrStr.getIterator(), frc);
		int nextPos = 0;
		int lineCnt = 1;

		while (measurer.getPosition() < cellValue.length()) {
			nextPos = measurer.nextOffset(612);
			lineCnt++;
			measurer.setPosition(nextPos);
		}

		row.setHeight((short) (row.getHeight() * lineCnt));
	}

	/**
	 * Set general border for a cell
	 * 
	 * @param cell
	 */
	private void setBorders4Style(CellStyle style) {
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);

		style.setTopBorderColor(IndexedColors.BLACK.getIndex());
		style.setRightBorderColor(IndexedColors.BLACK.getIndex());
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
	}

	/**
	 * Set border for merged cells/regions
	 * 
	 * @param regionAddress
	 * @param sheet
	 */
	private void setBorders4Region(String regionAddress, Sheet sheet) {
		CellRangeAddress region = CellRangeAddress.valueOf(regionAddress);
		RegionUtil.setBorderBottom(BorderStyle.THIN, region, sheet);
		RegionUtil.setBorderTop(BorderStyle.THIN, region, sheet);
		RegionUtil.setBorderLeft(BorderStyle.THIN, region, sheet);
		RegionUtil.setBorderRight(BorderStyle.THIN, region, sheet);
		RegionUtil.setBottomBorderColor(IndexedColors.BLACK.getIndex(), region, sheet);
		RegionUtil.setTopBorderColor(IndexedColors.BLACK.getIndex(), region, sheet);
		RegionUtil.setLeftBorderColor(IndexedColors.BLACK.getIndex(), region, sheet);
		RegionUtil.setRightBorderColor(IndexedColors.BLACK.getIndex(), region, sheet);
	}
}
