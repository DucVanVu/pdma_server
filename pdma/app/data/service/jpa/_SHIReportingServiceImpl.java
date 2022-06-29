package org.pepfar.pdma.app.data.service.jpa;

import java.io.IOException;
import java.io.InputStream;
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
import org.pepfar.pdma.app.data.domain.ShiInterview;
import org.pepfar.pdma.app.data.domain.User;
import org.pepfar.pdma.app.data.dto.CaseReportFilterDto;
import org.pepfar.pdma.app.data.repository.CaseOrgRepository;
import org.pepfar.pdma.app.data.service._ReportingService;
import org.pepfar.pdma.app.data.types.Permission;
import org.pepfar.pdma.app.data.types.ReportType;
import org.pepfar.pdma.app.utils.AuthorizationUtils;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.ExcelUtils;
import org.pepfar.pdma.app.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

@Service("_SHIReportingServiceImpl")
public class _SHIReportingServiceImpl implements _ReportingService {

	@Autowired
	private AuthorizationUtils authUtils;

	@Autowired
	private CaseOrgRepository coRepos;

	@Autowired
	private ApplicationContext context;

	private Workbook blankBook;

	@Override
	@Transactional(readOnly = true)
	public Workbook exportReport(CaseReportFilterDto filter) {

		List<ReportType> acceptedTypes = Lists.newArrayList(ReportType.NO_SHI, ReportType.SHI_EXPIRED);

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

		Workbook wbook = null;

		switch (filter.getReportType()) {
		case SHI_EXPIRED:
			// SHI expired patient list
			wbook = createShiExpiredPatientsWorkbook(filter);
			break;

		case NO_SHI:
			wbook = createNoShiPatientsWorkbook(filter);
			break;

		default:
			break;
		}

		return wbook;
	}

	/**
	 * Create a workbook containing patients whose SHI already expired
	 * 
	 * @param filter
	 * @return
	 */
	private Workbook createShiExpiredPatientsWorkbook(CaseReportFilterDto filter) {
		Workbook wbook = null;
		try (InputStream template = context.getResource("classpath:templates/patients-with-expired-shi.xlsx")
				.getInputStream()) {

			XSSFWorkbook tmp = new XSSFWorkbook(template);
			// Write title and period information
			Sheet sheet = tmp.getSheetAt(0);

			// Timestamp
			String period = "Thời điểm xuất dữ liệu: ";
			period += CommonUtils.hanoiNow().format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss"));

			ExcelUtils.createAndWriteInCell(sheet, 1, 1, period, 22, 12, false);

			wbook = new SXSSFWorkbook(tmp, 100);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (wbook == null) {
			return blankBook;
		}

		Sheet sheet = wbook.getSheetAt(0);

		// Patient sheet - Table content
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
		List<CaseOrg> caseOrgs = null;

		for (Long orgId : filter.getActualOrganizations()) {

			caseOrgs = coRepos.findPatientsWithExpiredSHI(orgId);

			if (CommonUtils.isEmpty(caseOrgs)) {
				continue;
			}

			// Start filling out data...
			for (CaseOrg co : caseOrgs) {

				Organization org = co.getOrganization();
				Case theCase = co.getTheCase();
				Person person = theCase.getPerson();

				ShiInterview latestInterview = null;
				Iterator<ShiInterview> interviews = theCase.getShiInterviews().iterator();
				if (interviews.hasNext()) {
					latestInterview = interviews.next();
				}

				if (latestInterview == null) {
					continue;
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
				if (org.getAddress() != null && org.getAddress().getProvince() != null) {
					cell.setCellValue(org.getAddress().getProvince().getName());
				}

				// Cơ sở điều trị
				cell = row.createCell(colIndex++, CellType.STRING);
				cell.setCellStyle(cellStyle);
				if (!CommonUtils.isEmpty(org.getName(), true)) {
					cell.setCellValue(org.getName());
				} else {
					cell.setCellValue("-");
				}

				// Mã bệnh án tại cơ sở điều trị
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

				// Số CMTND
				cell = row.createCell(colIndex++, CellType.STRING);
				cell.setCellStyle(cellStyle);
				if (person != null && person.getNidNumber() != null) {
					cell.setCellValue(filter.isConfidentialRequired() ? "-" : person.getNidNumber());
				} else {
					cell.setCellValue("-");
				}
				
				// Nghề nghiệp
				cell = row.createCell(colIndex++, CellType.STRING);
				cell.setCellStyle(cellStyle);
				if (!CommonUtils.isEmpty(person.getOccupationName(), true)) {
					cell.setCellValue(person.getOccupationName());
				} else {
					cell.setCellValue("-");
				}

				// Dân tộc
				cell = row.createCell(colIndex++, CellType.STRING);
				cell.setCellStyle(cellStyle);
				if (person.getEthnic() != null) {
					cell.setCellValue(person.getEthnic().getValue());
				} else {
					cell.setCellValue("-");
				}

				// Số thẻ BHYT
				cell = row.createCell(colIndex++, CellType.STRING);
				cell.setCellStyle(cellStyle);
				if (latestInterview != null && latestInterview.getShiCardNumber() != null) {
					cell.setCellValue(filter.isConfidentialRequired() ? "-" : latestInterview.getShiCardNumber());
				} else {
					cell.setCellValue("-");
				}

				// Ngày hết hạn thẻ
				cell = row.createCell(colIndex++, CellType.NUMERIC);
				cell.setCellStyle(dateCellStyle);
				if (latestInterview != null && latestInterview.getShiExpiryDate() != null) {
					cell.setCellValue(CommonUtils.fromLocalDateTime(latestInterview.getShiExpiryDate()));
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

				// Ngày bắt đầu đợt đăng ký tại cơ sở trong kỳ báo cáo
				cell = row.createCell(colIndex++, CellType.NUMERIC);
				cell.setCellStyle(dateCellStyle);
				if (co.getStartDate() != null) {
					cell.setCellValue(CommonUtils.fromLocalDateTime(co.getStartDate()));
				} else {
					cell.setCellValue("-");
				}

				// Loại đăng ký tại cơ sở trong kỳ báo cáo
				cell = row.createCell(colIndex++, CellType.STRING);
				cell.setCellStyle(cellStyle);
				if (co.getEnrollmentType() != null) {
					cell.setCellValue(co.getEnrollmentType().toString());
				} else {
					cell.setCellValue("-");
				}

				// Trạng thái điều trị của bệnh nhân tại cơ sở
				cell = row.createCell(colIndex++, CellType.STRING);
				cell.setCellStyle(cellStyle);
				cell.setCellValue((co.getStatus() != null) ? co.getStatus().toString() : "-");
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
	 * Create a workbook containing patients whose SHI already expired
	 * 
	 * @param filter
	 * @return
	 */
	private Workbook createNoShiPatientsWorkbook(CaseReportFilterDto filter) {
		Workbook wbook = null;
		try (InputStream template = context.getResource("classpath:templates/patients-with-no-shi.xlsx")
				.getInputStream()) {

			XSSFWorkbook tmp = new XSSFWorkbook(template);
			// Write title and period information
			Sheet sheet = tmp.getSheetAt(0);

			// Timestamp
			String period = "Thời điểm xuất dữ liệu: ";
			period += CommonUtils.hanoiNow().format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss"));

			ExcelUtils.createAndWriteInCell(sheet, 1, 1, period, 22, 12, false);

			wbook = new SXSSFWorkbook(tmp, 100);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (wbook == null) {
			return blankBook;
		}

		Sheet sheet = wbook.getSheetAt(0);

		// Patient sheet - Table content
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
		List<CaseOrg> caseOrgs = null;

		for (Long orgId : filter.getActualOrganizations()) {

			caseOrgs = coRepos.findPatientsWithoutSHI(orgId);

			if (CommonUtils.isEmpty(caseOrgs)) {
				continue;
			}

			// Start filling out data...
			for (CaseOrg co : caseOrgs) {

				Organization org = co.getOrganization();
				Case theCase = co.getTheCase();
				Person person = theCase.getPerson();

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
				if (org.getAddress() != null && org.getAddress().getProvince() != null) {
					cell.setCellValue(org.getAddress().getProvince().getName());
				}

				// Cơ sở điều trị
				cell = row.createCell(colIndex++, CellType.STRING);
				cell.setCellStyle(cellStyle);
				if (!CommonUtils.isEmpty(org.getName(), true)) {
					cell.setCellValue(org.getName());
				} else {
					cell.setCellValue("-");
				}

				// Mã bệnh án tại cơ sở điều trị
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

				// Số CMTND
				cell = row.createCell(colIndex++, CellType.STRING);
				cell.setCellStyle(cellStyle);
				if (person != null && person.getNidNumber() != null) {
					cell.setCellValue(filter.isConfidentialRequired() ? "-" : person.getNidNumber());
				} else {
					cell.setCellValue("-");
				}

				// Nghề nghiệp
				cell = row.createCell(colIndex++, CellType.STRING);
				cell.setCellStyle(cellStyle);
				if (!CommonUtils.isEmpty(person.getOccupationName(), true)) {
					cell.setCellValue(person.getOccupationName());
				} else {
					cell.setCellValue("-");
				}

				// Dân tộc
				cell = row.createCell(colIndex++, CellType.STRING);
				cell.setCellStyle(cellStyle);
				if (person.getEthnic() != null) {
					cell.setCellValue(person.getEthnic().getValue());
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

				// Ngày bắt đầu đợt đăng ký tại cơ sở trong kỳ báo cáo
				cell = row.createCell(colIndex++, CellType.NUMERIC);
				cell.setCellStyle(dateCellStyle);
				if (co.getStartDate() != null) {
					cell.setCellValue(CommonUtils.fromLocalDateTime(co.getStartDate()));
				} else {
					cell.setCellValue("-");
				}

				// Loại đăng ký tại cơ sở trong kỳ báo cáo
				cell = row.createCell(colIndex++, CellType.STRING);
				cell.setCellStyle(cellStyle);
				if (co.getEnrollmentType() != null) {
					cell.setCellValue(co.getEnrollmentType().toString());
				} else {
					cell.setCellValue("-");
				}

				// Trạng thái điều trị của bệnh nhân tại cơ sở
				cell = row.createCell(colIndex++, CellType.STRING);
				cell.setCellStyle(cellStyle);
				cell.setCellValue((co.getStatus() != null) ? co.getStatus().toString() : "-");
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
