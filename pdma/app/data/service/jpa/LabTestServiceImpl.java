package org.pepfar.pdma.app.data.service.jpa;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pepfar.pdma.app.Constants;
import org.pepfar.pdma.app.data.domain.Case;
import org.pepfar.pdma.app.data.domain.CaseOrg;
import org.pepfar.pdma.app.data.domain.LabTest;
import org.pepfar.pdma.app.data.domain.Location;
import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.domain.Person;
import org.pepfar.pdma.app.data.domain.QLabTest;
import org.pepfar.pdma.app.data.domain.QOrganization;
import org.pepfar.pdma.app.data.domain.User;
import org.pepfar.pdma.app.data.dto.AdminUnitDto;
import org.pepfar.pdma.app.data.dto.CaseReportFilterDto;
import org.pepfar.pdma.app.data.dto.DateRangeDto;
import org.pepfar.pdma.app.data.dto.LabTestDto;
import org.pepfar.pdma.app.data.dto.LabTestFilterDto;
import org.pepfar.pdma.app.data.dto.OPCDashboardFilterDto;
import org.pepfar.pdma.app.data.repository.CaseOrgRepository;
import org.pepfar.pdma.app.data.repository.CaseRepository;
import org.pepfar.pdma.app.data.repository.LabTestRepository;
import org.pepfar.pdma.app.data.repository.OrganizationRepository;
import org.pepfar.pdma.app.data.service.LabTestService;
import org.pepfar.pdma.app.data.types.ClinicalTestingType;
import org.pepfar.pdma.app.data.types.PatientStatus;
import org.pepfar.pdma.app.data.types.Permission;
import org.pepfar.pdma.app.data.types.ReportType;
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
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

@Service
public class LabTestServiceImpl implements LabTestService {

	@Autowired
	private LabTestRepository repos;

	@Autowired
	private OrganizationRepository orgRepos;

	@Autowired
	private CaseRepository caseRepos;

	@Autowired
	private CaseOrgRepository coRepos;

	@Autowired
	private ApplicationContext context;

	@Autowired
	private AuthorizationUtils authUtils;

	@Override
	@Transactional(readOnly = true)
	public LabTestDto findById(Long id) {
		if (!CommonUtils.isPositive(id, true)) {
			return null;
		}

		LabTest entity = repos.findOne(id);

		if (entity != null) {
			return new LabTestDto(entity);
		}

		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public List<LabTestDto> findAll(LabTestFilterDto filter) {

		if (filter == null || filter.getTheCase() == null || CommonUtils.isEmpty(filter.getTestTypes())) {
			return new ArrayList<>();
		}

		QLabTest q = QLabTest.labTest;
		List<LabTestDto> ret = new ArrayList<>();

		Iterator<LabTest> itr = repos.findAll(
				q.theCase.id.longValue().eq(filter.getTheCase().getId()).and(q.testType.in(filter.getTestTypes())),
				new Sort(new Order(Direction.DESC, "sampleDate"))).iterator();

		itr.forEachRemaining(s -> {
			ret.add(new LabTestDto(s));
		});

		return ret;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<LabTestDto> findAllPageable(LabTestFilterDto filter) {

		if (filter == null) {
			filter = new LabTestFilterDto();
		}

		if (filter.getPageIndex() < 0) {
			filter.setPageIndex(0);
		}

		if (filter.getPageSize() <= 0) {
			filter.setPageSize(25);
		}

		List<Long> orgIds = authUtils.getGrantedOrgIds(Permission.READ_ACCESS);
		List<LabTestDto> content = new ArrayList<>();

		QLabTest q = QLabTest.labTest;
		Sort defaultSort = new Sort(new Order(Direction.DESC, "sampleDate"));
		Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize(), defaultSort);

		@SuppressWarnings({ "unchecked", "rawtypes" })
		Page<LabTest> page = new PageImpl(new ArrayList<>());

		// get CD4 tests that don't have results
		if (filter.isNoResultOnly()) {
			if (filter.getTestType() == null) {
				return new PageImpl<>(new ArrayList<>(), pageable, 0);
			}

			// only look for tests created by the current user's granted organizations
			page = repos.findAll(q.testType.eq(filter.getTestType()).and(q.organization.id.longValue().in(orgIds))
					.and(q.resultDate.isNull().and(q.resultNumber.isNull())), pageable);

		} else {
			if (filter.getTestType() == null || filter.getTheCase() == null
					|| !CommonUtils.isPositive(filter.getTheCase().getId(), true)) {
				return new PageImpl<>(new ArrayList<>(), pageable, 0);
			}

			// find all regardless of organization, for this patient
			page = repos.findAll(
					q.testType.eq(filter.getTestType()).and(q.theCase.id.eq(filter.getTheCase().getId().longValue())),
					pageable);
		}

		if (filter.isNoResultOnly()) {
			page.getContent().parallelStream().forEachOrdered(o -> {
				LabTestDto odto = new LabTestDto(o);
				Case c = o.getTheCase();
				if (c != null && c.getPerson() != null) {
					odto.setPatientFullname(c.getPerson().getFullname());
				}

				Iterator<CaseOrg> cos = c.getCaseOrgs().iterator();
				while (cos.hasNext()) {
					CaseOrg co = cos.next();

					if (orgIds.contains(co.getOrganization().getId().longValue())) {
						odto.setPatientChartId(co.getPatientChartId());
						break;
					}
				}

				content.add(odto);
			});
		} else {
			page.getContent().parallelStream().forEachOrdered(o -> {
				content.add(new LabTestDto(o));
			});
		}

		return new PageImpl<>(content, pageable, page.getTotalElements());
	}

	@Override
	@Transactional(readOnly = true)
	public int checkVLEligibility(LabTestFilterDto filter) {
		final int NOT_ELIGIBLE = 0;
		final int AT_6TH_MONTH = 1;
		final int AT_12TH_MONTH = 2;
		final int ROUTINE = 3;
		final int FOLLOW_UP = 4;

		int result = NOT_ELIGIBLE;

		if (filter == null || filter.getOrganization() == null
				|| !CommonUtils.isPositive(filter.getOrganization().getId(), true) || filter.getCutpoint() == null) {
			return result;
		}

		Timestamp atDate = CommonUtils.toTimestamp(filter.getCutpoint());
		Long caseId = (filter.getTheCase() != null) ? filter.getTheCase().getId() : null;

		List<CaseOrg> listEntities = coRepos.findPatientsEligible4VLAt6thMonth(filter.getOrganization().getId(), caseId,
				atDate, filter.isCheckTestExistance());

		if (listEntities.size() > 0) {
			return AT_6TH_MONTH;
		} else {
			listEntities = coRepos.findPatientsEligible4VLAt12thMonth(filter.getOrganization().getId(), caseId, atDate,
					filter.isCheckTestExistance());

			if (listEntities.size() > 0) {
				return AT_12TH_MONTH;
			} else {
				listEntities = coRepos.findPatientsEligible4RoutineVL(filter.getOrganization().getId(), caseId, atDate,
						filter.isCheckTestExistance());

				if (listEntities.size() > 0) {
					return ROUTINE;
				} else {
					listEntities = coRepos.findPatientsRequiringFollowupVL(filter.getOrganization().getId(), caseId,
							atDate, filter.isCheckTestExistance());

					if (listEntities.size() > 0) {
						return FOLLOW_UP;
					}
				}
			}
		}

		return NOT_ELIGIBLE;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public LabTestDto saveOne(LabTestDto dto) {

		if (dto == null) {
			throw new IllegalArgumentException("Lab testing data could not be null.");
		}

		LabTest entity = null;
		if (CommonUtils.isPositive(dto.getId(), true)) {
			entity = repos.findOne(dto.getId());
		}

		if (entity == null) {
			entity = dto.toEntity();
			entity.setUid(UUID.randomUUID());
		} else {
			entity.setTestType(dto.getTestType());
			entity.setSampleDate(dto.getSampleDate());
			entity.setSampleSite(dto.getSampleSite());
			entity.setLabName(dto.getLabName());
			entity.setReasonForTesting(dto.getReasonForTesting());
			entity.setFundingSource(dto.getFundingSource());
			entity.setResultDate(dto.getResultDate());
			entity.setResultNumber(dto.getResultNumber());
			entity.setResultText(dto.getResultText());
			entity.setNeedConsultation(dto.getNeedConsultation());
			entity.setConsultation1(dto.getConsultation1());
			entity.setConsultation2(dto.getConsultation2());
			entity.setNote(dto.getNote());
		}

		Organization organization = null;
		Case theCase = null;

		if (dto.getOrganization() != null && CommonUtils.isPositive(dto.getOrganization().getId(), true)) {
			organization = orgRepos.findOne(dto.getOrganization().getId());
		}

		if (dto.getTheCase() != null && CommonUtils.isPositive(dto.getTheCase().getId(), true)) {
			theCase = caseRepos.findOne(dto.getTheCase().getId());
		}

		entity.setOrganization(organization);
		entity.setTheCase(theCase);

		entity = repos.save(entity);

		if (entity != null) {
			return new LabTestDto(entity);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteMultiple(LabTestDto[] dtos) {
		if (CommonUtils.isEmpty(dtos)) {
			return;
		}

		for (LabTestDto dto : dtos) {
			if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
				continue;
			}

			LabTest entity = repos.findOne(dto.getId());

			if (entity != null) {
				repos.delete(entity);
			}
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Workbook generateReport(LabTestFilterDto filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public Workbook getPatientsRequiringVL(CaseReportFilterDto filter) {

		if (filter == null) {
			filter = new CaseReportFilterDto();
		}

		OPCDashboardFilterDto dFilter = new OPCDashboardFilterDto();
		dFilter.setOrganizationId(filter.getOrganization());

		if (CommonUtils.isPositive(filter.getProvince(), true)) {
			AdminUnitDto province = new AdminUnitDto();
			province.setId(filter.getProvince());

			dFilter.setProvince(province);
		}

		return getPatientsRequiringVL(dFilter);
	}

	@Override
	@Transactional(readOnly = true)
	public Workbook getPatientsRequiringVL(OPCDashboardFilterDto filter) {
		Workbook blankBook = new XSSFWorkbook();
		blankBook.createSheet();

		if (filter == null) {
			return blankBook;
		}

		List<Long> actualOrgIds = getGrantedOrganizationIds(filter);
		if (actualOrgIds == null || actualOrgIds.size() <= 0) {
			return blankBook;
		}

		Workbook wbook = null;
		try (InputStream template = context.getResource("classpath:templates/bn-can-xn-tlvr-template.xlsx")
				.getInputStream()) {
			XSSFWorkbook tmp = new XSSFWorkbook(template);
			Sheet sheet = tmp.getSheetAt(0);

			String subTitle = "Ngày đánh giá: ";
			subTitle += CommonUtils.hanoiToday().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

			// ExcelUtils.createAndWriteInCell(sheet, 0, 1, "", 30, 16, true);
			ExcelUtils.createAndWriteInCell(sheet, 1, 1, subTitle, 22, 12, false);
			wbook = new SXSSFWorkbook(tmp, 100);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (wbook == null) {
			return blankBook;
		}

		Sheet sheet = wbook.getSheetAt(0);

		Font font = wbook.createFont();
		CellStyle cellStyle = wbook.createCellStyle();

		font.setFontName("Times New Roman");
		font.setFontHeightInPoints((short) 12);
		font.setColor(IndexedColors.BLACK.getIndex());

		cellStyle.setFont(font);
		cellStyle.setWrapText(true);
		cellStyle.setAlignment(HorizontalAlignment.LEFT);
		cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		ExcelUtils.setBorders4Style(cellStyle);

		CellStyle dateCellStyle = wbook.createCellStyle();
		DataFormat format = wbook.createDataFormat();

		dateCellStyle.cloneStyleFrom(cellStyle);
		dateCellStyle.setDataFormat(format.getFormat("dd/MM/yyyy"));
		ExcelUtils.setBorders4Style(dateCellStyle);

		// Fill out patient data
		int rowIndex = 4;
		int colIndex = 0;

		Row row = null;
		Cell cell = null;
		boolean confidential = confidentialRequired();
		Timestamp cutpoint = CommonUtils.toTimestamp(CommonUtils.hanoiTodayEnd());
		String[] testingReasons = { "Tại thời điểm 6 tháng sau ART", "Tại thời điểm 12 tháng sau ART",
				"Định kỳ sau 12 tháng", "Nghi ngờ thất bại điều trị" };

		for (long orgId : actualOrgIds) {

			List<CaseOrg> _at6thMonth = coRepos.findPatientsEligible4VLAt6thMonth(orgId, null, cutpoint, true);
			List<CaseOrg> _at12thMonth = coRepos.findPatientsEligible4VLAt12thMonth(orgId, null, cutpoint, true);
			List<CaseOrg> _12MonthRoutine = coRepos.findPatientsEligible4RoutineVL(orgId, null, cutpoint, true);
			List<CaseOrg> _3MonthFollowup = coRepos.findPatientsRequiringFollowupVL(orgId, null, cutpoint, true);

			@SuppressWarnings("unchecked")
			List<List<CaseOrg>> lists = Lists.newArrayList(_at6thMonth, _at12thMonth, _12MonthRoutine, _3MonthFollowup);

			int indx = 0;
			for (List<CaseOrg> list : lists) {

				String testingReason = testingReasons[indx++];

				if (list.isEmpty()) {
					continue;
				}

				// Start filling out data...
				for (CaseOrg co : list) {

					if (co.getStatus() != PatientStatus.ACTIVE) {
						continue;
					}

					Case theCase = co.getTheCase();
					Organization org = co.getOrganization();
					Person person = theCase.getPerson();

					colIndex = 0;
					row = sheet.createRow(rowIndex++);
					row.setHeightInPoints(22);

					// Khoá chính
					cell = row.createCell(colIndex++, CellType.STRING);
					cell.setCellStyle(cellStyle);
					cell.setCellValue(theCase.getId().toString());

					// Tỉnh - thành phố của cơ sở
					cell = row.createCell(colIndex++, CellType.STRING);
					cell.setCellStyle(cellStyle);
					if (org != null && org.getAddress() != null && org.getAddress().getProvince() != null) {
						cell.setCellValue(org.getAddress().getProvince().getName());
					} else {
						cell.setCellValue("-");
					}

					// Cơ sở điều trị
					cell = row.createCell(colIndex++, CellType.STRING);
					cell.setCellStyle(cellStyle);
					if (org != null) {
						cell.setCellValue(org.getName());
					} else {
						cell.setCellValue("-");
					}

					// Mã bệnh án
					cell = row.createCell(colIndex++, CellType.STRING);
					cell.setCellStyle(cellStyle);
					if (co.getPatientChartId() != null) {
						cell.setCellValue(co.getPatientChartId());
					} else {
						cell.setCellValue("-");
					}

					// Họ tên bệnh nhân
					cell = row.createCell(colIndex++, CellType.STRING);
					cell.setCellStyle(cellStyle);
					if (confidential) {
						cell.setCellValue("-");
					} else {
						cell.setCellValue(confidential ? "-" : theCase.getPerson().getFullname());
					}

					// Giới tính
					cell = row.createCell(colIndex++, CellType.STRING);
					cell.setCellStyle(cellStyle);
					if (person != null && person.getGender() != null) {
						cell.setCellValue(person.getGender().toString());
					} else {
						cell.setCellValue("-");
					}

					// Ngày sinh
					cell = row.createCell(colIndex++, CellType.NUMERIC);
					cell.setCellStyle(dateCellStyle);
					if (person != null && person.getDob() != null) {
						cell.setCellValue(CommonUtils.fromLocalDateTime(person.getDob()));
					} else {
						cell.setCellValue("");
					}

					// Ngày bắt đầu ARV
					cell = row.createCell(colIndex++, CellType.NUMERIC);
					cell.setCellStyle(dateCellStyle);
					if (theCase.getArvStartDate() != null) {
						cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getArvStartDate()));
					} else {
						cell.setCellValue("-");
					}

					// Ngày chuyển phác đồ bậc 2
					cell = row.createCell(colIndex++, CellType.NUMERIC);
					cell.setCellStyle(dateCellStyle);
					if (theCase.getSecondLineStartDate() != null) {
						cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getSecondLineStartDate()));
					} else {
						cell.setCellValue("-");
					}

					// lý do xét nghiệm ở lần này
					cell = row.createCell(colIndex++, CellType.STRING);
					cell.setCellStyle(cellStyle);
					cell.setCellValue(testingReason);

					// Most recent VL test
					List<LabTest> vlTests = theCase.getLabTests().parallelStream()
							.filter(e -> e.getTestType() == ClinicalTestingType.VIRAL_LOAD)
							.collect(Collectors.toList());

					LabTest latestVl = null;
					if (vlTests != null && vlTests.size() > 0) {
						latestVl = vlTests.get(0);
					}

					// ngày lấy mẫu gần nhất
					cell = row.createCell(colIndex++, CellType.NUMERIC);
					cell.setCellStyle(dateCellStyle);
					if (latestVl != null && latestVl.getSampleDate() != null) {
						cell.setCellValue(CommonUtils.fromLocalDateTime(latestVl.getSampleDate()));
					} else {
						cell.setCellValue("-");
					}

					// Kết quả xét nghiệm
					cell = row.createCell(colIndex++, CellType.NUMERIC);
					cell.setCellStyle(cellStyle);
					if (latestVl != null) {
						if (latestVl.getResultNumber() != null && latestVl.getResultNumber() <= 0) {
							cell.setCellValue("Không phát hiện");
						} else if (latestVl.getResultNumber() != null && latestVl.getResultNumber() > 0) {
							cell.setCellValue(latestVl.getResultNumber() + " bản sao/ml");
						} else if (!CommonUtils.isEmpty(latestVl.getResultText(), true)) {
							cell.setCellValue(latestVl.getResultText());
						} else {
							cell.setCellValue("Chưa có kết quả");
						}
					} else {
						cell.setCellValue("-");
					}

					// Lý do xét nghiệm ở lần xét nghiệm gần nhất
					cell = row.createCell(colIndex++, CellType.NUMERIC);
					cell.setCellStyle(dateCellStyle);
					if (latestVl != null && latestVl.getReasonForTesting() != null) {
						cell.setCellValue(latestVl.getReasonForTesting().toString());
					} else {
						cell.setCellValue("-");
					}

					// Addresses
					Location rAddress = null;
					Location cAddress = null;

					Set<Location> locs = theCase.getPerson().getLocations();
					for (Location loc : locs) {
						if (loc == null) {
							continue;
						}

						switch (loc.getAddressType()) {
						case RESIDENT_ADDRESS:
							rAddress = loc;
							break;
						case CURRENT_ADDRESS:
							cAddress = loc;
							break;
						default:
							break;
						}
					}

					// Residential address
					if (rAddress != null) {
						// R address - details
						cell = row.createCell(colIndex++, CellType.STRING);
						cell.setCellStyle(cellStyle);
						if (!CommonUtils.isEmpty(rAddress.getStreetAddress())) {
							cell.setCellValue(confidential ? "-" : rAddress.getStreetAddress());
						} else {
							cell.setCellValue("-");
						}

						// R address - commune
						cell = row.createCell(colIndex++, CellType.STRING);
						cell.setCellStyle(cellStyle);
						if (rAddress.getCommune() != null) {
							cell.setCellValue(confidential ? "-" : rAddress.getCommune().getName());
						} else {
							cell.setCellValue("-");
						}

						// R address - district
						cell = row.createCell(colIndex++, CellType.STRING);
						cell.setCellStyle(cellStyle);
						if (rAddress.getDistrict() != null) {
							cell.setCellValue(rAddress.getDistrict().getName());
						} else {
							cell.setCellValue("-");
						}

						// R address - province
						cell = row.createCell(colIndex++, CellType.STRING);
						cell.setCellStyle(cellStyle);
						if (rAddress.getProvince() != null) {
							cell.setCellValue(rAddress.getProvince().getName());
						} else {
							cell.setCellValue("-");
						}
					} else {
						// create empty residential address cells
						for (int i = 0; i < 4; i++) {
							cell = row.createCell(colIndex++, CellType.STRING);
							cell.setCellStyle(cellStyle);
							cell.setCellValue("-");
						}
					}

					// Current address
					if (cAddress != null) {
						// C address - details
						cell = row.createCell(colIndex++, CellType.STRING);
						cell.setCellStyle(cellStyle);
						if (!CommonUtils.isEmpty(cAddress.getStreetAddress())) {
							cell.setCellValue(confidential ? "-" : cAddress.getStreetAddress());
						} else {
							cell.setCellValue("-");
						}

						// C address - commune
						cell = row.createCell(colIndex++, CellType.STRING);
						cell.setCellStyle(cellStyle);
						if (cAddress.getCommune() != null) {
							cell.setCellValue(confidential ? "-" : cAddress.getCommune().getName());
						} else {
							cell.setCellValue("-");
						}

						// C address - district
						cell = row.createCell(colIndex++, CellType.STRING);
						cell.setCellStyle(cellStyle);
						if (cAddress.getDistrict() != null) {
							cell.setCellValue(cAddress.getDistrict().getName());
						} else {
							cell.setCellValue("-");
						}

						// C address - province
						cell = row.createCell(colIndex++, CellType.STRING);
						cell.setCellStyle(cellStyle);
						if (cAddress.getProvince() != null) {
							cell.setCellValue(cAddress.getProvince().getName());
						} else {
							cell.setCellValue("-");
						}
					} else {
						// create empty current address cells
						for (int i = 0; i < 4; i++) {
							cell = row.createCell(colIndex++, CellType.STRING);
							cell.setCellStyle(cellStyle);
							cell.setCellValue("-");
						}
					}

					// Ngày bắt đầu đợt điều trị gần nhất
					cell = row.createCell(colIndex++, CellType.NUMERIC);
					cell.setCellStyle(dateCellStyle);
					if (co.getStartDate() != null) {
						cell.setCellValue(CommonUtils.fromLocalDateTime(co.getStartDate()));
					} else {
						cell.setCellValue("-");
					}

					// Tình trạng bệnh nhân
					cell = row.createCell(colIndex++, CellType.STRING);
					cell.setCellStyle(cellStyle);
					if (co != null && co.getStatus() != null)
						cell.setCellValue(co.getStatus().toString());
					else {
						cell.setCellValue("-");
					}
				}
			}
		}

		// Hide ID columns
		sheet.setColumnHidden(0, true);

		// Auto-filter
		if (rowIndex >= 5) {
			sheet.setAutoFilter(CellRangeAddress.valueOf("A4:V" + rowIndex));
		}

		return wbook;
	}

	@Override
	@Transactional(readOnly = true)
	public Workbook exportListVLChartData(OPCDashboardFilterDto filter) {
		Workbook blankBook = new XSSFWorkbook();
		blankBook.createSheet();

		List<Long> actualOrgIds = getGrantedOrganizationIds(filter);
		if (actualOrgIds == null || actualOrgIds.size() <= 0) {
			return blankBook;
		}

		// list of 5 months
		int QUARTER_COUNT = 4;
		List<DateRangeDto> ranges = CommonUtils.getBackwardQuarters(LocalDateTime.now(), QUARTER_COUNT);
		List<LabTest> data = new ArrayList<LabTest>();

		for (int i = QUARTER_COUNT - 1; i >= 0; i--) {
			DateRangeDto r = ranges.get(i);

			Timestamp fromDate = CommonUtils.toTimestamp(r.getFromDate());
			Timestamp toDate = CommonUtils.toTimestamp(r.getToDate());

			actualOrgIds.parallelStream().forEach(orgId -> {
				List<LabTest> list = repos.getListVLChartData(Lists.newArrayList(orgId), fromDate, toDate);

				if (list != null && list.size() > 0) {
					data.addAll(list);
				}
			});

		}

		Workbook wbook = null;
		try (InputStream template = context.getResource("classpath:templates/bn-xetnghiem-tlvr-4quy-template.xlsx")
				.getInputStream()) {
			XSSFWorkbook tmp = new XSSFWorkbook(template);
			Sheet sheet = tmp.getSheetAt(0);
			ExcelUtils.createAndWriteInCell(sheet, 0, 1, "", 30, 16, true);
			ExcelUtils.createAndWriteInCell(sheet, 1, 1, "", 22, 12, false);
			wbook = new SXSSFWorkbook(tmp, 100);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (wbook == null) {
			return blankBook;
		}

		Sheet sheet = wbook.getSheetAt(0);

		Font font = wbook.createFont();
		CellStyle cellStyle = wbook.createCellStyle();

		font.setFontName("Times New Roman");
		font.setFontHeightInPoints((short) 12);
		font.setColor(IndexedColors.BLACK.getIndex());

		cellStyle.setFont(font);
		cellStyle.setWrapText(true);
		cellStyle.setAlignment(HorizontalAlignment.LEFT);
		cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		ExcelUtils.setBorders4Style(cellStyle);

		CellStyle dateCellStyle = wbook.createCellStyle();
		DataFormat format = wbook.createDataFormat();

		dateCellStyle.cloneStyleFrom(cellStyle);
		dateCellStyle.setDataFormat(format.getFormat("dd/MM/yyyy"));
		ExcelUtils.setBorders4Style(dateCellStyle);

		// Fill out patient data
		int rowIndex = 4;
		int colIndex = 0;

		Row row = null;
		Cell cell = null;
		boolean confidential = confidentialRequired();
//		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
//		row = sheet.createRow(1);	
//		cell = row.createCell(0);
//		cell.setCellValue("Ngày xuất dữ liệu: "+ sdf.format(CommonUtils.fromLocalDateTime(CommonUtils.hanoiNow())));		

		// Start filling out data...
		for (LabTest entity : data) {
			if (entity == null) {
				continue;
			}
			Case theCase = entity.getTheCase();
			Organization currentOrg = null;

			colIndex = 0;
			row = sheet.createRow(rowIndex++);
			row.setHeightInPoints(22);

			// Khoá chính
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(theCase.getId().toString());

			// Tỉnh - thành phố của cơ sở
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);

			CaseOrg currentCO = null; // latest case-org with the org in the granted org list
			try {
				Iterator<CaseOrg> caseOrgs = theCase.getCaseOrgs().iterator();
				while (caseOrgs.hasNext()) {
					CaseOrg co = caseOrgs.next();

//					if (currentOrg.getId().longValue() == co.getOrganization().getId().longValue()) {
//						currentCO = co;
//						break;
//					}
					if (co.getCurrent() != null && co.getCurrent() && !co.isRefTrackingOnly()
							&& co.isLatestRelationship()) {
						currentCO = co;
						currentOrg = co.getOrganization();
						break;
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
				theCase = caseRepos.findOne(theCase.getId());
				Iterator<CaseOrg> caseOrgs = theCase.getCaseOrgs().iterator();
				while (caseOrgs.hasNext()) {
					CaseOrg co = caseOrgs.next();

					if (co.getCurrent() != null && co.getCurrent() && !co.isRefTrackingOnly()
							&& co.isLatestRelationship()) {
						currentCO = co;
						currentOrg = co.getOrganization();
						break;
					}
				}
			}

			if (currentOrg != null && currentOrg.getAddress() != null
					&& currentOrg.getAddress().getProvince() != null) {
				cell.setCellValue(currentOrg.getAddress().getProvince().getName());
			} else {
				cell.setCellValue("-");
			}

			// Cơ sở điều trị
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			if (currentOrg != null) {
				cell.setCellValue(currentOrg.getName());
			} else {
				cell.setCellValue("-");
			}

			// Mã bệnh nhân hivinfo
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);

			if (theCase.getHivInfoID() != null) {
				cell.setCellValue(theCase.getHivInfoID());
			} else {
				cell.setCellValue("-");
			}

			// Mã bệnh án
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			if (currentCO != null && currentCO.getPatientChartId() != null) {
				cell.setCellValue(currentCO.getPatientChartId());
			} else {
				cell.setCellValue("-");
			}

			// Họ tên bệnh nhân
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			if (confidential) {
				cell.setCellValue("-");
			} else {
				cell.setCellValue(confidential ? "-" : theCase.getPerson().getFullname());
			}

			// Giới tính
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(theCase.getPerson().getGender().toString());

			// Ngày sinh
			cell = row.createCell(colIndex++, CellType.NUMERIC);
			if (theCase != null && theCase.getPerson() != null && theCase.getPerson().getDob() != null) {
				cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getPerson().getDob()));
			} else {
				cell.setCellValue("");
			}

			cell.setCellStyle(dateCellStyle);
			// lý do xét nghiệm
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			if (entity.getReasonForTesting() != null) {
				cell.setCellValue(entity.getReasonForTesting().toString());
			} else {
				cell.setCellValue("");
			}

			// ngày lấy mẫu
			cell = row.createCell(colIndex++, CellType.NUMERIC);
			if (entity.getSampleDate() != null) {
				cell.setCellValue(CommonUtils.fromLocalDateTime(entity.getSampleDate()));
				cell.setCellStyle(dateCellStyle);
			} else {
				cell.setCellValue("");
			}

			// ngày có kết quả
			cell = row.createCell(colIndex++, CellType.NUMERIC);
			if (entity.getResultDate() != null) {
				cell.setCellValue(CommonUtils.fromLocalDateTime(entity.getResultDate()));
				cell.setCellStyle(dateCellStyle);
			} else {
				cell.setCellValue("");
			}

			// kết quả xét nghiệm
			cell = row.createCell(colIndex++, CellType.NUMERIC);
			if (entity.getResultNumber() != null && entity.getResultNumber() <= 0) {
				cell.setCellValue("Không phát hiện");
			} else if (entity.getResultNumber() != null && entity.getResultNumber() > 0) {
				cell.setCellValue(entity.getResultNumber() + " bản sao/ml");
			} else {
				cell.setCellValue("Chưa nhập kết quả");
			}
			cell.setCellStyle(cellStyle);

			// nguồn kinh phí
			cell = row.createCell(colIndex++, CellType.NUMERIC);
			if (entity.getFundingSource() != null) {
				cell.setCellValue(entity.getFundingSource().toString());
				cell.setCellStyle(cellStyle);
			} else {
				cell.setCellValue("");
			}

			// Addresses
			Location rAddress = null;
			Location cAddress = null;

			Set<Location> locs = theCase.getPerson().getLocations();
			for (Location loc : locs) {
				if (loc == null) {
					continue;
				}

				switch (loc.getAddressType()) {
				case RESIDENT_ADDRESS:
					rAddress = loc;
					break;
				case CURRENT_ADDRESS:
					cAddress = loc;
					break;
				default:
					break;
				}
			}

			// Residential address
			if (rAddress != null) {
				// R address - details
				cell = row.createCell(colIndex++, CellType.STRING);
				cell.setCellStyle(cellStyle);
				if (!CommonUtils.isEmpty(rAddress.getStreetAddress())) {
					cell.setCellValue(confidential ? "-" : rAddress.getStreetAddress());
				} else {
					cell.setCellValue("-");
				}

				// R address - commune
				cell = row.createCell(colIndex++, CellType.STRING);
				cell.setCellStyle(cellStyle);
				if (rAddress.getCommune() != null) {
					cell.setCellValue(confidential ? "-" : rAddress.getCommune().getName());
				} else {
					cell.setCellValue("-");
				}

				// R address - district
				cell = row.createCell(colIndex++, CellType.STRING);
				cell.setCellStyle(cellStyle);
				if (rAddress.getDistrict() != null) {
					cell.setCellValue(rAddress.getDistrict().getName());
				} else {
					cell.setCellValue("-");
				}

				// R address - province
				cell = row.createCell(colIndex++, CellType.STRING);
				cell.setCellStyle(cellStyle);
				if (rAddress.getProvince() != null) {
					cell.setCellValue(rAddress.getProvince().getName());
				} else {
					cell.setCellValue("-");
				}
			} else {
				// create empty residential address cells
				for (int i = 0; i < 4; i++) {
					cell = row.createCell(colIndex++, CellType.STRING);
					cell.setCellStyle(cellStyle);
					cell.setCellValue("-");
				}
			}

			// Current address
			if (cAddress != null) {
				// C address - details
				cell = row.createCell(colIndex++, CellType.STRING);
				cell.setCellStyle(cellStyle);
				if (!CommonUtils.isEmpty(cAddress.getStreetAddress())) {
					cell.setCellValue(confidential ? "-" : cAddress.getStreetAddress());
				} else {
					cell.setCellValue("-");
				}

				// C address - commune
				cell = row.createCell(colIndex++, CellType.STRING);
				cell.setCellStyle(cellStyle);
				if (cAddress.getCommune() != null) {
					cell.setCellValue(confidential ? "-" : cAddress.getCommune().getName());
				} else {
					cell.setCellValue("-");
				}

				// C address - district
				cell = row.createCell(colIndex++, CellType.STRING);
				cell.setCellStyle(cellStyle);
				if (cAddress.getDistrict() != null) {
					cell.setCellValue(cAddress.getDistrict().getName());
				} else {
					cell.setCellValue("-");
				}

				// C address - province
				cell = row.createCell(colIndex++, CellType.STRING);
				cell.setCellStyle(cellStyle);
				if (cAddress.getProvince() != null) {
					cell.setCellValue(cAddress.getProvince().getName());
				} else {
					cell.setCellValue("-");
				}
			} else {
				// create empty current address cells
				for (int i = 0; i < 4; i++) {
					cell = row.createCell(colIndex++, CellType.STRING);
					cell.setCellStyle(cellStyle);
					cell.setCellValue("-");
				}
			}

			// Ngày xét nghiệm sàng lọc HIV
			cell = row.createCell(colIndex++, CellType.NUMERIC);
			cell.setCellStyle(dateCellStyle);
			if (theCase.getHivScreenDate() != null) {
				cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getHivScreenDate()));
			} else {
				cell.setCellValue("-");
			}

			// Ngày XN khẳng định
			cell = row.createCell(colIndex++, CellType.NUMERIC);
			cell.setCellStyle(dateCellStyle);
			if (theCase.getHivConfirmDate() != null) {
				cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getHivConfirmDate()));
			} else {
				cell.setCellValue("-");
			}

			// Cơ sở XN khẳng định
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(!CommonUtils.isEmpty(theCase.getConfirmLabName()) ? theCase.getConfirmLabName() : "-");

			// Ngày bắt đầu ARV
			cell = row.createCell(colIndex++, CellType.NUMERIC);
			cell.setCellStyle(dateCellStyle);
			if (theCase.getArvStartDate() != null) {
				cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getArvStartDate()));
			} else {
				cell.setCellValue("-");
			}
			// Phác đồ thuốc hiện tại
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(
					!CommonUtils.isEmpty(theCase.getCurrentArvRegimenName()) ? theCase.getCurrentArvRegimenName()
							: "-");

			// Ngày bắt đầu phác đồ hiện tại
			cell = row.createCell(colIndex++, CellType.NUMERIC);
			cell.setCellStyle(dateCellStyle);
			if (theCase.getCurrentArvRegimenStartDate() != null) {
				cell.setCellValue(CommonUtils.fromLocalDateTime(theCase.getCurrentArvRegimenStartDate()));
			} else {
				cell.setCellValue("-");
			}
			// Tình trạng bệnh nhân
			cell = row.createCell(colIndex++, CellType.STRING);
			cell.setCellStyle(cellStyle);
			if (currentCO != null)
				cell.setCellValue((currentCO.getStatus() != null) ? currentCO.getStatus().toString() : "-");
			else {
				cell.setCellValue("-");
			}
			// Ngày thay đổi tình trạng
			cell = row.createCell(colIndex++, CellType.NUMERIC);
			cell.setCellStyle(dateCellStyle);
			if (currentCO != null) {
				if (currentCO.getStatus() == PatientStatus.ACTIVE) {
					cell.setCellValue(CommonUtils.fromLocalDateTime(currentCO.getStartDate()));
				} else if (currentCO.getEndDate() != null) {
					cell.setCellValue(CommonUtils.fromLocalDateTime(currentCO.getEndDate()));
				} else {
					cell.setCellValue("-");
				}
			} else {
				cell.setCellValue("-");
			}

		}

		// Hide ID columns
		sheet.setColumnHidden(0, true);

		// Auto-filter
		if (rowIndex >= 5) {
			sheet.setAutoFilter(CellRangeAddress.valueOf("A4:AB" + rowIndex));
		}

		return wbook;
	}

	@Override
	@Transactional(readOnly = true)
	public Workbook exportAllLabTestsData(CaseReportFilterDto filter) {

		List<ReportType> acceptedTypes = Lists.newArrayList(ReportType.VL_DATA, ReportType.CD4_DATA);

		Workbook blankBook = new XSSFWorkbook();
		blankBook.createSheet();

		if (filter == null || filter.getReportType() == null || !acceptedTypes.contains(filter.getReportType())) {
			return blankBook;
		}

		// Confidentiality info
		User user = SecurityUtils.getCurrentUser();
		boolean confidentialRequired = false;

		if (SecurityUtils.isUserInRoles(user, Constants.ROLE_ADMIN, Constants.ROLE_DONOR,
				Constants.ROLE_NATIONAL_MANAGER)) {
			confidentialRequired = true;
		}

		filter.setConfidentialRequired(confidentialRequired);

		// Date/time adjustment
		LocalDateTime adjFromDate = null;
		LocalDateTime adjToDate = null;

		if (filter.getFromDate() != null) {
			adjFromDate = CommonUtils.dateStart(filter.getFromDate());
		}

		if (filter.getToDate() != null) {
			adjToDate = CommonUtils.dateEnd(filter.getToDate());
		}

		// Set adjusted date and time
		filter.setFromDate(adjFromDate);
		filter.setToDate(adjToDate);

		// Set actual organizations
		List<Long> grantedOrgIds = authUtils.getGrantedOrgIds(Permission.READ_ACCESS, filter.getProvince(), true);
		List<Long> orgIds = new ArrayList<>();

		if (filter.getOrganization() == 0l) {
			// Get report for all granted organizations
			grantedOrgIds.forEach(id -> {
				orgIds.add(id);
			});
		} else if (grantedOrgIds.contains(filter.getOrganization().longValue())) {
			orgIds.add(filter.getOrganization());
		} else {
			return blankBook;
		}

		filter.setActualOrganizations(orgIds);

		String sheetName = "";
		String listTitle = "";
		String periodStr = "";
		List<String> testTypes = new ArrayList<>();

		switch (filter.getReportType()) {
		case VL_DATA:
			sheetName = "Dữ liệu TLVR HIV";
			listTitle = "Dữ liệu xét nghiệm tải lượng virus HIV";
			testTypes.add("VIRAL_LOAD");
			break;

		case CD4_DATA:
			sheetName = "Dữ liệu CD4";
			listTitle = "Dữ liệu xét nghiệm CD4";
			testTypes.add("CD4");
			break;

		default:
			break;
		}

		periodStr += "Giai đoạn báo cáo: ";
		periodStr += filter.getFromDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		periodStr += " - ";
		periodStr += filter.getToDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

		Workbook wbook = null;

		try (InputStream template = context.getResource("classpath:templates/dulieu-xn-template.xlsx")
				.getInputStream()) {

			XSSFWorkbook tmp = new XSSFWorkbook(template);
			// Write title and period information
			Sheet sheet = tmp.getSheetAt(0);

			ExcelUtils.createAndWriteInCell(sheet, 0, 1, listTitle, 30, 16, true);
			ExcelUtils.createAndWriteInCell(sheet, 1, 1, periodStr, 22, 12, false);

			wbook = new SXSSFWorkbook(tmp, 100);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (wbook == null) {
			return blankBook;
		}

		wbook.setSheetName(0, sheetName);

		Sheet sheet = wbook.getSheetAt(0);

		Font font = wbook.createFont();
		CellStyle cellStyle = wbook.createCellStyle();

		font.setFontName("Times New Roman");
		font.setFontHeightInPoints((short) 12);
		font.setColor(IndexedColors.BLACK.getIndex());

		cellStyle.setFont(font);
		cellStyle.setWrapText(true);
		cellStyle.setAlignment(HorizontalAlignment.LEFT);
		cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		cellStyle.setIndention((short) 1);
		ExcelUtils.setBorders4Style(cellStyle);

		CellStyle dateCellStyle = wbook.createCellStyle();
		dateCellStyle.cloneStyleFrom(cellStyle);

		DataFormat format = wbook.createDataFormat();
		dateCellStyle.setDataFormat(format.getFormat("dd/MM/yyyy"));

		int colIndex = 0;
		int rowIndex = 4;

		Row row = null;
		Cell cell = null;

		Timestamp fromDate = CommonUtils.toTimestamp(filter.getFromDate());
		Timestamp toDate = CommonUtils.toTimestamp(filter.getToDate());

		// Query for lab test data
		List<Integer> acceptedStatuses = Lists.newArrayList(PatientStatus.ACTIVE.getNumber(),
				PatientStatus.DEAD.getNumber(), PatientStatus.LTFU.getNumber(),
				PatientStatus.TRANSFERRED_OUT.getNumber());

		for (Long orgId : filter.getActualOrganizations()) {

			List<LabTest> labTests = repos.findLabtestData(Lists.newArrayList(orgId), testTypes, fromDate, toDate);

			for (LabTest test : labTests) {

				Organization org = null;
				Case theCase = test.getTheCase();
				Person person = theCase.getPerson();

				// latest case-organization the case currently belongs to
				// this is to fill out the columns of administrative data to reflect the current
				// status
				Iterator<CaseOrg> cos = theCase.getCaseOrgs().iterator();
				CaseOrg co = null;
				while (cos.hasNext()) {
					CaseOrg _co = cos.next();
					if (!_co.isRefTrackingOnly() && orgId == _co.getOrganization().getId().longValue()
							&& acceptedStatuses.contains(_co.getStatus().getNumber())) {
						co = _co;
						org = co.getOrganization();
						break;
					}
				}

				colIndex = 0;
				row = sheet.createRow(rowIndex++);
				row.setHeightInPoints(22);

				// Khóa chính
				cell = row.createCell(colIndex++, CellType.STRING);
				cell.setCellStyle(cellStyle);
				cell.setCellValue(theCase.getId().toString());

				// Tỉnh - thành phố của cơ sở
				cell = row.createCell(colIndex++, CellType.STRING);
				cell.setCellStyle(cellStyle);

				if (org != null && org.getAddress() != null && org.getAddress().getProvince() != null) {
					cell.setCellValue(org.getAddress().getProvince().getName());
				} else {
					cell.setCellValue("-");
				}

				// Cơ sở điều trị HIV/AIDS thực hiện chuyển gửi bệnh nhân
				cell = row.createCell(colIndex++, CellType.STRING);
				cell.setCellStyle(cellStyle);
				if (org != null && !CommonUtils.isEmpty(org.getName())) {
					cell.setCellValue(org.getName());
				} else {
					cell.setCellValue("-");
				}

				// Mã bệnh án tại cơ sở điều trị
				cell = row.createCell(colIndex++, CellType.STRING);
				cell.setCellStyle(cellStyle);
				if (co != null && !CommonUtils.isEmpty(co.getPatientChartId(), true)) {
					cell.setCellValue(co.getPatientChartId());
				} else {
					cell.setCellValue("-");
				}

				// Họ tên bệnh nhân
				cell = row.createCell(colIndex++, CellType.STRING);
				cell.setCellStyle(cellStyle);
				if (person == null || CommonUtils.isEmpty(person.getFullname())) {
					cell.setCellValue("-");
				} else {
					cell.setCellValue(filter.isConfidentialRequired() ? "-" : person.getFullname());
				}

				// Giới tính
				cell = row.createCell(colIndex++, CellType.STRING);
				cell.setCellStyle(cellStyle);
				if (person == null || person.getGender() == null) {
					cell.setCellValue("-");
				} else {
					cell.setCellValue(person.getGender().toString());
				}

				// Ngày sinh
				cell = row.createCell(colIndex++, CellType.NUMERIC);
				cell.setCellStyle(dateCellStyle);
				if (person != null && person.getDob() != null) {
					cell.setCellValue(CommonUtils.fromLocalDateTime(person.getDob()));
				} else {
					cell.setCellValue("-");
				}

				// Ngày lấy mẫu xét nghiệm
				cell = row.createCell(colIndex++, CellType.NUMERIC);
				cell.setCellValue(CommonUtils.fromLocalDateTime(test.getSampleDate()));
				cell.setCellStyle(dateCellStyle);

				// Ngày có kết quả
				cell = row.createCell(colIndex++, CellType.NUMERIC);
				cell.setCellStyle(dateCellStyle);
				if (test.getResultDate() != null) {
					cell.setCellValue(CommonUtils.fromLocalDateTime(test.getResultDate()));
				} else {
					cell.setCellValue("-");
				}

				// kết quả xét nghiệm
				cell = row.createCell(colIndex++, CellType.STRING);
				cell.setCellStyle(cellStyle);

				switch (filter.getReportType()) {
				case VL_DATA:
					if (test.getResultNumber() != null && test.getResultNumber() <= 0) {
						cell.setCellValue("Không phát hiện");
					} else if (test.getResultNumber() != null && test.getResultNumber() > 0) {
						cell.setCellValue(test.getResultNumber() + " bản sao/ml");
					} else {
						if (test.getResultDate() == null) {
							cell.setCellValue("Chưa nhập kết quả");
						} else {
							cell.setCellValue(test.getResultText() + " (Chưa hồi cứu kết quả)");
						}
					}

					break;
				case CD4_DATA:
					if (!CommonUtils.isEmpty(test.getResultText(), true)) {
						cell.setCellValue(test.getResultText().trim());
					} else {
						cell.setCellValue("Chưa nhập kết quả");
					}

					break;
				default:
					break;
				}

				// nguồn kinh phí
				cell = row.createCell(colIndex++, CellType.STRING);
				cell.setCellStyle(cellStyle);
				if (test.getFundingSource() != null) {
					cell.setCellValue(test.getFundingSource().toString());
				} else {
					cell.setCellValue("");
				}

				// Lý do xét nghiệm
				cell = row.createCell(colIndex++, CellType.STRING);
				cell.setCellStyle(cellStyle);
				if (test.getReasonForTesting() != null) {
					cell.setCellValue(test.getReasonForTesting().toString());
				} else {
					cell.setCellValue("");
				}

				// Addresses
				Location rAddress = null;
				Location cAddress = null;

				if (person != null) {
					Set<Location> locs = person.getLocations();
					for (Location loc : locs) {
						if (loc == null) {
							continue;
						}

						switch (loc.getAddressType()) {
						case RESIDENT_ADDRESS:
							rAddress = loc;
							break;
						case CURRENT_ADDRESS:
							cAddress = loc;
							break;
						default:
							break;
						}
					}
				}

				// Residential address
				if (rAddress != null) {
					// R address - details
					cell = row.createCell(colIndex++, CellType.STRING);
					cell.setCellStyle(cellStyle);
					if (!CommonUtils.isEmpty(rAddress.getStreetAddress())) {
						cell.setCellValue(filter.isConfidentialRequired() ? "-" : rAddress.getStreetAddress());
					} else {
						cell.setCellValue("-");
					}

					// R address - commune
					cell = row.createCell(colIndex++, CellType.STRING);
					cell.setCellStyle(cellStyle);
					if (rAddress.getCommune() != null) {
						cell.setCellValue(filter.isConfidentialRequired() ? "-" : rAddress.getCommune().getName());
					} else {
						cell.setCellValue("-");
					}

					// R address - district
					cell = row.createCell(colIndex++, CellType.STRING);
					cell.setCellStyle(cellStyle);
					if (rAddress.getDistrict() != null) {
						cell.setCellValue(rAddress.getDistrict().getName());
					} else {
						cell.setCellValue("-");
					}

					// R address - province
					cell = row.createCell(colIndex++, CellType.STRING);
					cell.setCellStyle(cellStyle);
					if (rAddress.getProvince() != null) {
						cell.setCellValue(rAddress.getProvince().getName());
					} else {
						cell.setCellValue("-");
					}
				} else {
					// create empty residential address cells
					for (int i = 0; i < 4; i++) {
						cell = row.createCell(colIndex++, CellType.STRING);
						cell.setCellStyle(cellStyle);
						cell.setCellValue("-");
					}
				}

				// Current address
				if (cAddress != null) {
					// C address - details
					cell = row.createCell(colIndex++, CellType.STRING);
					cell.setCellStyle(cellStyle);
					if (!CommonUtils.isEmpty(cAddress.getStreetAddress())) {
						cell.setCellValue(filter.isConfidentialRequired() ? "-" : cAddress.getStreetAddress());
					} else {
						cell.setCellValue("-");
					}

					// C address - commune
					cell = row.createCell(colIndex++, CellType.STRING);
					cell.setCellStyle(cellStyle);
					if (cAddress.getCommune() != null) {
						cell.setCellValue(filter.isConfidentialRequired() ? "-" : cAddress.getCommune().getName());
					} else {
						cell.setCellValue("-");
					}

					// C address - district
					cell = row.createCell(colIndex++, CellType.STRING);
					cell.setCellStyle(cellStyle);
					if (cAddress.getDistrict() != null) {
						cell.setCellValue(cAddress.getDistrict().getName());
					} else {
						cell.setCellValue("-");
					}

					// C address - province
					cell = row.createCell(colIndex++, CellType.STRING);
					cell.setCellStyle(cellStyle);
					if (cAddress.getProvince() != null) {
						cell.setCellValue(cAddress.getProvince().getName());
					} else {
						cell.setCellValue("-");
					}
				} else {
					// create empty current address cells
					for (int i = 0; i < 4; i++) {
						cell = row.createCell(colIndex++, CellType.STRING);
						cell.setCellStyle(cellStyle);
						cell.setCellValue("-");
					}
				}

				// Ngày bắt đầu đợt điều trị gần đây nhất tại cơ sở
				cell = row.createCell(colIndex++, CellType.NUMERIC);
				cell.setCellStyle(dateCellStyle);
				if (co != null && co.getStartDate() != null) {
					cell.setCellValue(CommonUtils.fromLocalDateTime(co.getStartDate()));
				} else {
					cell.setCellValue("-");
				}

				// Trạng thái điều trị của bệnh nhân tại cơ sở
				cell = row.createCell(colIndex++, CellType.STRING);
				cell.setCellStyle(cellStyle);
				if (co != null && co.getStatus() != null) {
					cell.setCellValue(co.getStatus().toString());
				} else {
					cell.setCellValue("-");
				}

				// Ngày kết thúc đợt điều trị gần đây nhất tại cơ sở
				cell = row.createCell(colIndex++, CellType.NUMERIC);
				cell.setCellStyle(dateCellStyle);
				if (co != null && co.getEndDate() != null) {
					cell.setCellValue(CommonUtils.fromLocalDateTime(co.getEndDate()));
				} else {
					cell.setCellValue("-");
				}
			}
		}

		// Hide ID columns
		sheet.setColumnHidden(0, true);

		// Auto-filter
		if (rowIndex >= 4) {
			sheet.setAutoFilter(CellRangeAddress.valueOf("A4:W" + rowIndex));
		}

		return wbook;
	}

	/**
	 * Check if confidentiality is required
	 * 
	 * @return
	 */
	private boolean confidentialRequired() {
		// Confidentiality info
		User user = SecurityUtils.getCurrentUser();
		boolean confidentialRequired = false;

		if (SecurityUtils.isUserInRoles(user, Constants.ROLE_ADMIN, Constants.ROLE_DONOR,
				Constants.ROLE_NATIONAL_MANAGER)) {
			confidentialRequired = true;
		}

		return confidentialRequired;
	}

	/**
	 * Get a list of IDs of the organizations that the current user has READ ACCESS
	 * to
	 * 
	 * @param filter
	 * @return
	 */
	private List<Long> getGrantedOrganizationIds(OPCDashboardFilterDto filter) {

		Long provinceId = null;
		if (filter.getProvince() != null) {
			provinceId = filter.getProvince().getId();
		}

		List<Long> actualOrgIds = new ArrayList<>();
		List<Long> grantedOrgIds = authUtils.getGrantedOrgIds(Permission.READ_ACCESS, provinceId, true);

		if (grantedOrgIds == null || grantedOrgIds.size() <= 0) {
			return actualOrgIds;
		}

		if (CommonUtils.isPositive(filter.getOrganizationId(), true)) {
			actualOrgIds.add(filter.getOrganizationId());
		} else if (filter.getOrganizationId() != null && filter.getOrganizationId() == 0l) {

			// Check the province
			if (filter.getProvince() != null && CommonUtils.isPositive(filter.getProvince().getId(), true)) {
				long provId = filter.getProvince().getId();
				Iterator<Organization> orgs = orgRepos
						.findAll(QOrganization.organization.id.longValue().in(grantedOrgIds)).iterator();

				while (orgs.hasNext()) {
					Organization org = orgs.next();
					if (org.getAddress() != null && org.getAddress().getProvince() != null
							&& org.getAddress().getProvince().getId() == provId) {
						actualOrgIds.add(org.getId());
					}
				}

			} else {
				actualOrgIds = grantedOrgIds.stream().collect(Collectors.toList());
			}
		}

		return actualOrgIds;
	}

}
