package org.pepfar.pdma.app.data.service.jpa;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import org.pepfar.pdma.app.data.domain.Location;
import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.domain.Person;
import org.pepfar.pdma.app.data.domain.QOrganization;
import org.pepfar.pdma.app.data.domain.RiskInterview;
import org.pepfar.pdma.app.data.domain.User;
import org.pepfar.pdma.app.data.dto.CaseReportFilterDto;
import org.pepfar.pdma.app.data.repository.CaseOrgRepository;
import org.pepfar.pdma.app.data.repository.OrganizationRepository;
import org.pepfar.pdma.app.data.service._ReportingService;
import org.pepfar.pdma.app.data.types.Permission;
import org.pepfar.pdma.app.data.types.ReportType;
import org.pepfar.pdma.app.utils.AuthorizationUtils;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.ExcelUtils;
import org.pepfar.pdma.app.utils.RiskGroupUtils;
import org.pepfar.pdma.app.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

@Service("_RiskGroupReportingServiceImpl")
public class _RiskGroupReportingServiceImpl implements _ReportingService {

	@Autowired
	private AuthorizationUtils authUtils;

	@Autowired
	private OrganizationRepository orgRepos;

	@Autowired
	private CaseOrgRepository coRepos;

	@Autowired
	private ApplicationContext context;
	
	private Workbook blankBook;

	@Override
	@Transactional(readOnly = true)
	public Workbook exportReport(CaseReportFilterDto filter) {
		List<ReportType> acceptedTypes = Lists.newArrayList(ReportType.RISKGROUP_REPORT);

		blankBook = new XSSFWorkbook();
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
		LocalDateTime adjAtDate = null;

		if (filter.getAtDate() != null) {
			adjAtDate = CommonUtils.dateEnd(filter.getAtDate());
		}

		// Set adjusted date and time
		filter.setAtDate(adjAtDate);

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

		// Get list of actual OPCs
		QOrganization qOrg = QOrganization.organization;
		List<Organization> orgs = Lists
				.newArrayList(orgRepos.findAll(qOrg.opcSite.isTrue().and(qOrg.id.longValue().in(orgIds))));

		Workbook wbook = null;
		boolean multiSite = orgs.size() > 1;

		String periodStr = "";

		if (!multiSite) {
			periodStr = orgs.get(0).getName() + " - ";
		} else {
			periodStr = "Nhiều cơ sở (danh sách ở worksheet kế bên) - ";
		}

		periodStr += "Ngày xuất dữ liệu: ";
		periodStr += CommonUtils.hanoiNow().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

		wbook = createRiskgroupWorkbook(filter, periodStr);

		int indx = 0;

		if (orgs.size() > 1) {

			Sheet sheet = wbook.createSheet("Cơ sở báo cáo");

			ExcelUtils.createAndWriteInCell(sheet, indx, 1, "Danh sách các cơ sở báo cáo", 22, 12, true);
			indx++;

			for (Organization org : orgs) {
				String s = indx + ". " + org.getName();
				if (org.getAddress() != null && org.getAddress().getProvince() != null) {
					s += ", ";
					s += org.getAddress().getProvince().getName();
				}

				ExcelUtils.createAndWriteInCell(sheet, indx, 1, s, 22, 12, false);
				indx++;
			}
		}

		return wbook;
	}

	/**
	 * Fill out the workbook
	 * 
	 * @param filter
	 * @param periodStr
	 * @return
	 */
	private Workbook createRiskgroupWorkbook(CaseReportFilterDto filter, String periodStr) {
		Workbook wbook = null;

		try (InputStream template = context.getResource("classpath:templates/risk-group-template.xlsx")
				.getInputStream()) {

			XSSFWorkbook tmp = new XSSFWorkbook(template);
			// Write title and period information

			Sheet sheet = tmp.getSheetAt(0);

			ExcelUtils.createAndWriteInCell(sheet, 1, 1, periodStr, 22, 12, false);

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

		// Query for patients
		for (Long orgId : filter.getActualOrganizations()) {

			List<CaseOrg> cos = coRepos.findCaseOrgs(orgId);

			if (CommonUtils.isEmpty(cos)) {
				continue;
			}

			// Start filling out data...
			for (CaseOrg co : cos) {

				Case theCase = co.getTheCase();
				Organization org = co.getOrganization();
				Person person = theCase.getPerson();
				RiskInterview riskInterview = null;

				// get the latest risk interview entity
				if (theCase.getRiskInterviews() != null) {
					Iterator<RiskInterview> risks = theCase.getRiskInterviews().iterator();
					if (risks.hasNext()) {
						riskInterview = risks.next();
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

				// Cơ sở điều trị
				cell = row.createCell(colIndex++, CellType.STRING);
				cell.setCellStyle(cellStyle);
				if (org != null) {
					cell.setCellValue(org.getName());
				}

				// Mã bệnh án tại cơ sở điều trị
				cell = row.createCell(colIndex++, CellType.STRING);
				cell.setCellStyle(cellStyle);
				if (co != null && co.getPatientChartId() != null) {
					cell.setCellValue(co.getPatientChartId());
				} else {
					cell.setCellValue("-");
				}

				// Loại hình đăng ký
				cell = row.createCell(colIndex++, CellType.STRING);
				cell.setCellStyle(cellStyle);
				if (co != null && co.getEnrollmentType() != null) {
					cell.setCellValue(co.getEnrollmentType().toString());
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

				// Nhóm nguy cơ gần nhất
				String riskName = RiskGroupUtils.getPrioritizedRiskName(riskInterview);

				cell = row.createCell(colIndex++, CellType.STRING);
				cell.setCellStyle(cellStyle);
				if (riskName != null) {
					cell.setCellValue(riskName);
				} else {
					cell.setCellValue("-");
				}

				// Ngày đánh giá nhóm nguy cơ
				cell = row.createCell(colIndex++, CellType.NUMERIC);
				cell.setCellStyle(dateCellStyle);
				if (riskInterview != null && riskInterview.getInterviewDate() != null) {
					cell.setCellValue(CommonUtils.fromLocalDateTime(riskInterview.getInterviewDate()));
				} else {
					cell.setCellValue("-");
				}

				// Đơn vị đánh giá nhóm nguy cơ
				cell = row.createCell(colIndex++, CellType.STRING);
				cell.setCellStyle(cellStyle);
				if (riskInterview != null && riskInterview.getOrganization() != null) {
					cell.setCellValue(riskInterview.getOrganization().getName());
				} else {
					cell.setCellValue("-");
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
				if (co.getStartDate() != null) {
					cell.setCellValue(CommonUtils.fromLocalDateTime(co.getStartDate()));
				} else {
					cell.setCellValue("-");
				}

				// Trạng thái điều trị của bệnh nhân tại cơ sở
				cell = row.createCell(colIndex++, CellType.STRING);
				cell.setCellStyle(cellStyle);
				cell.setCellValue(co.getStatus() != null ? co.getStatus().toString() : "-");

				// Ngày kết thúc đợt điều trị (nếu ko còn điều trị)
				cell = row.createCell(colIndex++, CellType.NUMERIC);
				cell.setCellStyle(dateCellStyle);
				if (co.getEndDate() != null) {
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
			sheet.setAutoFilter(CellRangeAddress.valueOf("A4:U" + rowIndex));
		}

		return wbook;
	}
}
