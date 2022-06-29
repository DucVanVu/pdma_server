package org.pepfar.pdma.app.data.service.jpa;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pepfar.pdma.app.Constants;
import org.pepfar.pdma.app.data.domain.*;
import org.pepfar.pdma.app.data.dto.*;
import org.pepfar.pdma.app.data.repository.*;
import org.pepfar.pdma.app.data.service.SelfTestEntryService;
import org.pepfar.pdma.app.data.service.SelfTestSpecimenService;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.*;

@Service
public class SelfTestEntryServiceImpl implements SelfTestEntryService {

    @Autowired
    public EntityManager manager;

    @Autowired
    private SelfTestEntryRepository repos;

    @Autowired
    private SelfTestSpecimenRepository specRepos;

    @Autowired
    private OrganizationRepository orgRepos;

    @Autowired
    private StaffRepository staffRepos;

    @Autowired
    private PersonRepository personRepos;

    @Autowired
    private LocationRepository locationRepos;

    @Autowired
    private AdminUnitRepository adminUnitRepos;

    @Autowired
    private AuthorizationUtils authUtils;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private SelfTestSpecimenService selfTestSpecimenService;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserOrganizationRepository userOrganizationRepository;

    @Override
    @Transactional(readOnly = true)
    public SelfTestEntryDto findById(Long id) {
        if (!CommonUtils.isPositive(id, true)) {
            return null;
        }
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return null;
        }
        SelfTestEntry entity = repos.findOne(id);
        if (entity != null) {
            Boolean isSite = SecurityUtils.isUserInRole(currentUser, "ROLE_SITE_MANAGER");
            Boolean isProvince = SecurityUtils.isUserInRole(currentUser, "ROLE_PROVINCIAL_MANAGER");
            Boolean isAdministrator = SecurityUtils.isUserInRole(currentUser, "ROLE_ADMIN");
            List<UserOrganization> listUO = null;
            if (!isAdministrator) {
                listUO = userOrganizationRepository.getListByUserId(currentUser.getId());
            }
            return newSelfTestEntryDto(entity, currentUser, isSite, isProvince, isAdministrator, listUO);
        } else {
            return null;
        }

    }

    private SelfTestEntryDto newSelfTestEntryDto(SelfTestEntry entity, User currentUser, boolean isSite,
                                                 boolean isProvince, boolean isAdministrator, List<UserOrganization> listUO) {
        if (entity != null) {
            // Chỉ Quyền cơ sở và tỉnh có quyền xem chi tiết
            boolean isViewPII = isSite || isProvince;
            // Nếu là admin thì mặc định có quyền sửa - xóa - xem, nếu không thì cũng sẽ =
            // false và xét ở bước kế tiếp
            boolean isReadAble = isAdministrator;
            boolean isWritAble = isAdministrator;
            boolean isDeleteAble = isAdministrator;
            if (!isAdministrator) {
                if (listUO != null && listUO.size() > 0) {
                    for (UserOrganization userOrganization : listUO) {
                        if (userOrganization.getOrganization() != null && entity.getOrganization() != null
                                && userOrganization.getOrganization().getId().equals(entity.getOrganization().getId())
                                && userOrganization.getSelfTestRole() != null && userOrganization.getSelfTestRole()) {
                            if (userOrganization.getReadAccess() != null && userOrganization.getReadAccess()) {
                                isReadAble = true;
                            }
                            if (userOrganization.getWriteAccess() != null && userOrganization.getWriteAccess()) {
                                isWritAble = true;
                            }
                            if (userOrganization.getDeleteAccess() != null && userOrganization.getDeleteAccess()) {
                                isDeleteAble = true;
                            }
                        }
                    }
                }
            }
            return new SelfTestEntryDto(entity, isViewPII, isReadAble, isWritAble, isDeleteAble);
        } else {
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SelfTestSpecimenDto findSpecimenById(Long id) {
        if (!CommonUtils.isPositive(id, true)) {
            return null;
        }

        SelfTestSpecimen entity = specRepos.findOne(id);

        if (entity != null) {
            return new SelfTestSpecimenDto(entity);
        }

        return null;
    }

//	@Override
//	@Transactional(readOnly = true)
//	public Page<SelfTestEntryDto> findAll(SelfTestFilterDto filter) {
//		User currentUser = SecurityUtils.getCurrentUser();
//		Boolean isSite = SecurityUtils.isUserInRole(currentUser, "ROLE_SITE_MANAGER");
//		Boolean isProvince = SecurityUtils.isUserInRole(currentUser, "ROLE_PROVINCIAL_MANAGER");
//		Boolean isAdministrator = SecurityUtils.isUserInRole(currentUser, "ROLE_ADMIN");
//		List<Long> grantedIds = authUtils.getGrantedOrgIds(Permission.READ_ACCESS);
//		List<Long> grantedSelfTestIds = authUtils.getGrantedOrgIds(Permission.SELFTEST_ACCESS);
//		Boolean isViewPII = isSite || isProvince;
//
//		if(CollectionUtils.isEmpty(grantedIds) && !isAdministrator) {
//			return null;
//		}
//		if(CollectionUtils.isEmpty(grantedSelfTestIds) && !isAdministrator && !isProvince) {
//			return null;
//		}
//		// Check ORG
//		List<UserOrganization> listUO = null;
//		if (!isAdministrator) {
//			listUO = userOrganizationRepository.getListByUserId(currentUser.getId());
//		}
//
//		if (filter == null) {
//			filter = new SelfTestFilterDto();
//		}
//
//		if (filter.getPageIndex() < 0) {
//			filter.setPageIndex(0);
//		}
//
//		if (filter.getPageSize() <= 0) {
//			filter.setPageSize(25);
//		}
//
//		BooleanExpression be = createSearchCriteria(filter);
//
//		PageRequest pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize(),
//				new Sort(Direction.DESC, "dispensingDate"));
//		Page<SelfTestEntry> page = repos.findAll(be, pageable);
//		final List<SelfTestEntryDto> content = new ArrayList<>();
//
//		for(SelfTestEntry e : page.getContent()) {
//			content.add(newSelfTestEntryDto(e, currentUser, isSite, isProvince, isAdministrator, listUO));
//		}
////		page.getContent().forEach(e -> {
////			content.add(newSelfTestEntryDto(e, currentUser, isSite, isProvince, isAdministrator, listUO));
////		});
//
//		return new PageImpl<>(content, pageable, page.getTotalElements());
//	}

    @Override
    @Transactional(readOnly = true)
    public Page<SelfTestEntryDto> findAll(SelfTestFilterDto filter) {
        User currentUser = SecurityUtils.getCurrentUser();

        Boolean isAdministrator = SecurityUtils.isUserInRole(currentUser, "ROLE_ADMIN");
        Boolean isProvince = SecurityUtils.isUserInRole(currentUser, "ROLE_PROVINCIAL_MANAGER");
        Boolean isSiteManagement = SecurityUtils.isUserInRole(currentUser, "ROLE_SITE_MANAGER");

        Boolean isDONOR = SecurityUtils.isUserInRole(currentUser, "ROLE_DONOR");
        Boolean isNATIONAL = SecurityUtils.isUserInRole(currentUser, "ROLE_NATIONAL_MANAGER");
        Boolean isDISTRICT = SecurityUtils.isUserInRole(currentUser, "ROLE_DISTRICT_MANAGER");

        Boolean isViewPII = isProvince || isSiteManagement;
        final List<Long> orgIds = authUtils.getGrantedOrgIds(Permission.READ_ACCESS);
        final List<Long> writableOrgIds = authUtils.getGrantedOrgIds(Permission.WRITE_ACCESS);
        final List<Long> selfTestableOrgIds = authUtils.getGrantedOrgIds(Permission.SELFTEST_ACCESS);

        List<Long> lstOrgIds = new ArrayList<Long>();
        List<Long> staffIds = new ArrayList<>();
        if (orgIds != null && orgIds.size() > 0) {
            lstOrgIds.addAll(orgIds);
        }
        if (writableOrgIds != null && writableOrgIds.size() > 0) {
            lstOrgIds.addAll(writableOrgIds);
        }
        //Nếu là admin thì không cần xét OrgId
        if (CollectionUtils.isEmpty(lstOrgIds) && !isAdministrator) {
            return null;
        }
        //Nếu có các quyền như code (chỉ có ROLE_SITE_MANAGER) này thì phải xét đến selfTestableOrgIds
        if (CollectionUtils.isEmpty(selfTestableOrgIds) && !isAdministrator && !isDONOR && !isNATIONAL && !isProvince && !isDISTRICT) {
            return null;
        }

        if (filter == null) {
            filter = new SelfTestFilterDto();
        }

        if (filter.getPageIndex() < 0) {
            filter.setPageIndex(0);
        }

        if (filter.getPageSize() <= 0) {
            filter.setPageSize(25);
        }

        String SQL = "";
        if (isViewPII) {
            SQL = " SELECT new org.pepfar.pdma.app.data.dto.SelfTestEntryDto(s, true,true,false,false) from SelfTestEntry s, SelfTestSpecimen st WHERE s.id = st.selfTest.id and 1=1 ";
        } else {
            SQL = " SELECT new org.pepfar.pdma.app.data.dto.SelfTestEntryDto(s, false,true,false,false) from SelfTestEntry s, SelfTestSpecimen st WHERE s.id = st.selfTest.id and 1=1 ";
        }
        String countSQL = " SELECT count(s.id) from SelfTestEntry s, SelfTestSpecimen st WHERE s.id = st.selfTest.id and 1=1 ";
        String whereClause = " ";
        if (!isAdministrator) {
            whereClause += " AND s.organization.id in (:lstOrgIds) ";
            if (isSiteManagement && !isDONOR && !isNATIONAL && !isProvince && !isDISTRICT) {
                whereClause += " AND s.organization.id in (:lstSelfTestOrgIds) ";
            }
        }
        String orderByClause = "";

        if (filter.getKeyword() != null && filter.getKeyword().length() > 0) {
            whereClause += " AND (" + " (s.person.fullname like :keyword) " + " OR (s.dispensingStaff.person.fullname like :keyword)) ";
        }
        if (filter.getOrganization() != null && filter.getOrganization().getId() != null && filter.getOrganization().getId() > 0L) {
            whereClause += " AND (s.organization.id = :orgId)";
        }
        if (filter.getStaff() != null) {
            Long id = filter.getStaff().getId();
            if (CommonUtils.isPositive(id, true)) {
                staffIds.add(id);
            }
        }
        if (!CommonUtils.isEmpty(staffIds)) {
            whereClause += " AND (s.dispensingStaff.id in :staffIds)";
        }

        if (filter.getDispensingDateFrom() != null) {
            whereClause += " AND (s.dispensingDate >= :from)";
        }

        if (filter.getDispensingDateTo() != null) {
            whereClause += " AND (s.dispensingDate <= :to)";
        }

        if (!CommonUtils.isEmpty(filter.getSpecimen())) {
            whereClause += " AND (st.code = :specimen)";
        }

        if (filter.getProvinceId() != null) {
            whereClause += " AND (s.organization.address.province.id = :provinceId)";
        }

        Query q = manager.createQuery(SQL + whereClause + orderByClause, SelfTestEntryDto.class);
        Query qCount = manager.createQuery(countSQL + whereClause);
        if (!isAdministrator) {
            q.setParameter("lstOrgIds", lstOrgIds);
            qCount.setParameter("lstOrgIds", lstOrgIds);
            if (isSiteManagement && !isDONOR && !isNATIONAL && !isProvince && !isDISTRICT) {
                q.setParameter("lstSelfTestOrgIds", selfTestableOrgIds);
                qCount.setParameter("lstSelfTestOrgIds", selfTestableOrgIds);
            }
        }
        if (filter.getKeyword() != null && filter.getKeyword().length() > 0) {
            q.setParameter("keyword", "%" + filter.getKeyword() + "%");
            qCount.setParameter("keyword", "%" + filter.getKeyword() + "%");
        }
        if (filter.getOrganization() != null && filter.getOrganization().getId() != null && filter.getOrganization().getId() > 0L) {
            q.setParameter("orgId", filter.getOrganization().getId());
            qCount.setParameter("orgId", filter.getOrganization().getId());
        }
        if (!CommonUtils.isEmpty(staffIds)) {
            q.setParameter("staffIds", staffIds);
            qCount.setParameter("staffIds", staffIds);
        }

        if (filter.getDispensingDateFrom() != null) {
            q.setParameter("from", filter.getDispensingDateFrom());
            qCount.setParameter("from", filter.getDispensingDateFrom());
        }

        if (filter.getDispensingDateTo() != null) {
            q.setParameter("to", filter.getDispensingDateTo());
            qCount.setParameter("to", filter.getDispensingDateTo());
        }

        if (!CommonUtils.isEmpty(filter.getSpecimen())) {
            q.setParameter("specimen", filter.getSpecimen());
            qCount.setParameter("specimen", filter.getSpecimen());
        }

        if (filter.getProvinceId() != null) {
            q.setParameter("provinceId", filter.getProvinceId());
            qCount.setParameter("provinceId", filter.getProvinceId());
        }

        Long count = (long) qCount.getSingleResult();

        if (filter.getDisablePaging() == null || filter.getDisablePaging() == false) {
            int startPosition = filter.getPageIndex() * filter.getPageSize();
            q.setFirstResult(startPosition);
            q.setMaxResults(filter.getPageSize());
        } else {
            q.setFirstResult(0);
            if (count != null && count > 0) {
                q.setMaxResults(count.intValue());
                filter.setPageSize(count.intValue());
            } else {
                q.setMaxResults(10);
                filter.setPageSize(10);
            }
        }

//			@SuppressWarnings("unchecked")
        List<SelfTestEntryDto> entities = q.getResultList();
        Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize());
        Page<SelfTestEntryDto> result = new PageImpl<SelfTestEntryDto>(entities, pageable, count);

        return result;
    }

    public Workbook exportReportSeftTestCase(PreventionFilterDto filter) {
        Workbook blankBook = new XSSFWorkbook();
        blankBook.createSheet();
        if (filter.getProvinceId() != null && CollectionUtils.isEmpty(filter.getOrgIds())) {
            filter.setOrgIds(organizationRepository.findAllIdByProvinceId(filter.getProvinceId()));
        }
        PreventionReportDto<SelfTestDetailReportDto> result = selfTestSpecimenService.getReport(filter);
//		HTSCaseReportDto htsCaseReportDto = this.getReport(filter.getOrgIds(), filter.getFromDate(), filter.getToDate());
//		if (htsCaseReportDto == null || htsCaseReportDto.getListDetail().size() == 0) {
//			return blankBook;
//		}
//		else {
//		XSSFWorkbook wb = null;
        XSSFWorkbook wbook = null;
        try (InputStream template = context.getResource("classpath:templates/self-test-report.xlsx").getInputStream()) {
//				XSSFWorkbook tmp = new XSSFWorkbook(template);
//				Sheet sheet = tmp.getSheetAt(0);
            wbook = new XSSFWorkbook(template);
//			wbook = new SXSSFWorkbook(wb);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (wbook == null) {
            return blankBook;
        }
        int colIndex = 1;

        Row row = null;
        Row row1 = null;
        Row row2 = null;
        Cell cell = null;
        Cell cell1 = null;
        Cell cell2 = null;
        Sheet sheet = wbook.getSheetAt(0);

        int seq = 0;
        CellStyle cellStyle = wbook.createCellStyle();
        ExcelUtils.setBorders4Style(cellStyle);
        cellStyle.setWrapText(true);
        cellStyle.setAlignment(HorizontalAlignment.RIGHT);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        CellStyle dateTimeStyle = wbook.createCellStyle();
        DataFormat format = wbook.createDataFormat();
//			dateTimeStyle.cloneStyleFrom(templateRow.getCell(0).getCellStyle());
        dateTimeStyle.setDataFormat(format.getFormat("dd/MM/yyyy"));
        ExcelUtils.setBorders4Style(dateTimeStyle);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
//
        row = sheet.getRow(3);

        cell = row.getCell(1);
        cell.setCellValue(result.getOrgName());

        cell = row.getCell(9);
        cell.setCellValue(result.getProvinceName());

        row = sheet.getRow(4);

        cell = row.getCell(2);
        if (result.getFromDate() != null) {
            cell.setCellValue(formatter.format(Date.from(result.getFromDate().toInstant(ZoneOffset.of("+7")))));
        }
        cell = row.getCell(5);
        if (result.getToDate() != null) {
            cell.setCellValue(formatter.format(Date.from(result.getToDate().toInstant(ZoneOffset.of("+7")))));
        }
        cell = row.getCell(9);
        cell.setCellValue(result.getDistrictName());
        Integer total = 0;
        for (SelfTestDetailReportDto details : result.getListDetail()) {
//            if (rowIndex == 8 || rowIndex == 15 || rowIndex == 20) {
//                rowIndex++;
//            }
//            if (rowIndex == 7) {
//                total = details.getSupported();
//            }
//            row = sheet.getRow(rowIndex++);
//            if (rowIndex == 17) {
//                cell = row.getCell(8);
//                cell.setCellValue(total);
//                cell = row.getCell(9);
//                cell.setCellValue(details.getUnsupported());
//                cell = row.getCell(10);
//                cell.setCellValue(total + details.getUnsupported());
//            } else {
//                if (rowIndex == 18 || rowIndex == 19 || rowIndex == 20) {
//                    cell = row.getCell(8);
//                    cell.setCellValue("");
//                    cell = row.getCell(9);
//                    cell.setCellValue(details.getUnsupported());
//                    cell = row.getCell(10);
//                    cell.setCellValue(details.getUnsupported());
//                } else {
//                    cell = row.getCell(8);
//                    cell.setCellValue(details.getSupported());
//                    cell = row.getCell(9);
//                    cell.setCellValue(details.getUnsupported());
//                    cell = row.getCell(10);
//                    cell.setCellValue(details.getTotal());
//                }
//
//            }
            if (colIndex == 8 || colIndex == 15 || colIndex == 20) {
                colIndex++;
            }

            if (colIndex == 2) {
                colIndex+=7;
            }
            row = sheet.getRow(8);
            row1 = sheet.getRow(9);
            row2 = sheet.getRow(10);

            cell = row.getCell(colIndex);
            cell1 = row1.getCell(colIndex);
            cell2 = row2.getCell(colIndex);

            cell.setCellValue(details.getSupported());
            cell1.setCellValue(details.getUnsupported());
            cell2.setCellValue(details.getTotal());

            colIndex++;
        }
        return wbook;

    }

    @Override
    @Transactional(readOnly = true)
    public Workbook exportSearchResults(SelfTestFilterDto filter) {
        if (filter == null) {
            filter = new SelfTestFilterDto();
        }

        if (filter.getPageIndex() < 0) {
            filter.setPageIndex(0);
        }

        if (filter.getPageSize() <= 0) {
            filter.setPageSize(25);
        }

        boolean confidentialRequired = false;
        User currentUser = SecurityUtils.getCurrentUser();
        Boolean isAdministrator = SecurityUtils.isUserInRole(currentUser, "ROLE_ADMIN");
        for (Role r : currentUser.getRoles()) {
            if (r.getName().equalsIgnoreCase(Constants.ROLE_ADMIN) || r.getName().equalsIgnoreCase(Constants.ROLE_DONOR)
                    || r.getName().equalsIgnoreCase(Constants.ROLE_NATIONAL_MANAGER)) {
                confidentialRequired = true;
            }
        }

        Workbook blankBook = new XSSFWorkbook();
        blankBook.createSheet();

        List<Long> orgIds = authUtils.getGrantedOrgIds(Permission.READ_ACCESS);

        if (!isAdministrator && CommonUtils.isEmpty(orgIds)) {
            // this user is not granted access to any organization
            return blankBook;
        }

        QSelfTestEntry q = QSelfTestEntry.selfTestEntry;
        BooleanExpression be = createSearchCriteria(filter);
        Iterator<SelfTestEntry> entries = repos.findAll(be, q.organization.name.asc(), q.dispensingDate.desc())
                .iterator();

        XSSFWorkbook wb = null;
        SXSSFWorkbook wbook = null;
        try (InputStream template = context.getResource("classpath:templates/self-test-data-template.xlsx")
                .getInputStream()) {
            wbook = new SXSSFWorkbook(new XSSFWorkbook(template), 100);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
        int rowIndex = 3;

        Sheet sheet = wbook.getSheetAt(0);
        Row row = null;
        Cell cell = null;

        // Start filling out data...
        while (entries.hasNext()) {

            SelfTestEntry ste = entries.next();
            Iterator<SelfTestSpecimen> itr = ste.getSpecimens().iterator();

            Organization org = ste.getOrganization();
            Staff staff = ste.getDispensingStaff();

            while (itr.hasNext()) {

                colIndex = 0;

                SelfTestSpecimen sts = itr.next();

                row = sheet.createRow(rowIndex++);
                row.setHeightInPoints(22);

                // Tỉnh - thành phố của cơ sở
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);

                if (org != null && org.getAddress() != null && org.getAddress().getProvince() != null) {
                    cell.setCellValue(org.getAddress().getProvince().getName());
                } else {
                    cell.setCellValue("-");
                }

                // Cơ sở cấp phát
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (org != null && org.getName() != null) {
                    cell.setCellValue(org.getName());
                } else {
                    cell.setCellValue("-");
                }

                // Nhân viên cấp phát
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (staff != null && staff.getPerson() != null && staff.getPerson().getFullname() != null) {
                    cell.setCellValue(staff.getPerson().getFullname());
                } else {
                    cell.setCellValue("-");
                }

                // Ngày cấp phát
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(dateCellStyle);
                if (ste.getDispensingDate() != null) {
                    cell.setCellValue(CommonUtils.fromLocalDateTime(ste.getDispensingDate()));
                } else {
                    cell.setCellValue("-");
                }

                // Tên khách hàng
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (ste.getPerson() != null && ste.getPerson().getFullname() != null) {
                    cell.setCellValue(confidentialRequired ? "-" : ste.getPerson().getFullname());
                } else {
                    cell.setCellValue("-");
                }

                // ĐT khách hàng
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (ste.getPerson() != null && ste.getPerson().getMobilePhone() != null) {
                    cell.setCellValue(confidentialRequired ? "-" : ste.getPerson().getMobilePhone());
                } else {
                    cell.setCellValue("-");
                }

                // Nguồn selfTest
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (ste.getSelfTestSource() != null) {
                    cell.setCellValue(ste.getSelfTestSource().getDescription());
                } else {
                    cell.setCellValue("-");
                }

                // Addresses
                Location cAddress = null;

                if (ste.getPerson() != null) {
                    Set<Location> locs = ste.getPerson().getLocations();
                    for (Location loc : locs) {
                        if (loc == null) {
                            continue;
                        }

                        switch (loc.getAddressType()) {
                            case CURRENT_ADDRESS:
                                cAddress = loc;
                                break;
                            default:
                                break;
                        }
                    }
                }

                // Current address
                if (cAddress != null) {

                    // C address - province
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    if (cAddress.getProvince() != null) {
                        cell.setCellValue(cAddress.getProvince().getName());
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

                    // C address - commune
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    if (cAddress.getCommune() != null) {
                        cell.setCellValue(confidentialRequired ? "-" : cAddress.getCommune().getName());
                    } else {
                        cell.setCellValue("-");
                    }

                    // C address - details
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    if (cAddress.getStreetAddress() != null) {
                        cell.setCellValue(confidentialRequired ? "-" : cAddress.getStreetAddress());
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

                // Tên sinh phẩm
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (sts.getName() != null) {
                    cell.setCellValue(sts.getName());
                } else {
                    cell.setCellValue("-");
                }

                // Sử dụng sinh phẩm có sự hỗ trợ trực tiếp -> Năm sinh
                cell = row.createCell(colIndex++, CellType.NUMERIC);
                cell.setCellStyle(cellStyle);
                if (sts.getClientYob() != null) {
                    cell.setCellValue(sts.getClientYob());
                } else {
                    cell.setCellValue("-");
                }

                // Sử dụng sinh phẩm có sự hỗ trợ trực tiếp -> Giới tính
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);
                if (sts.getClientGender() != null) {
                    cell.setCellValue(sts.getClientGender().toString());
                } else {
                    cell.setCellValue("-");
                }

                // Sử dụng sinh phẩm không có hỗ trợ
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);

                if (sts.getClient() != null) {
                    switch (sts.getClient()) {
                        case "SELF":
                            cell.setCellValue("Bản thân");
                            break;

                        case "SEXUAL_PARTNER":
                            cell.setCellValue("Bạn tình");
                            break;

                        case "IDU_PARTNER":
                            cell.setCellValue("Bạn chích chung");
                            break;

                        case "OTHER":
                            cell.setCellValue("Khác");
                            break;

                        default:
                            break;
                    }

                } else {
                    cell.setCellValue("-");
                }

                // Nhóm nguy cơ
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);

                if (sts.getClientRiskGroup() != null) {
                    switch (sts.getClientRiskGroup()) {
                        case "PWID":
                            cell.setCellValue("Tiêm chích ma tuý");
                            break;

                        case "MSM":
                            cell.setCellValue("Nam quan hệ đồng giới");
                            break;

                        case "TG":
                            cell.setCellValue("Chuyển giới");
                            break;

                        case "FSW":
                            cell.setCellValue("Nữ bán dâm");
                            break;

                        case "PLHIV_PARTNER":
                            cell.setCellValue("BT/BC của người có HIV");
                            break;

                        case "OTHER":
                            cell.setCellValue("Khác");
                            break;

                        default:
                            break;
                    }

                } else {
                    cell.setCellValue("-");
                }

                // Kết quả tự xét nghiệm
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);

                if (sts.getScreenResult() != null) {
                    switch (sts.getScreenResult()) {
                        case "NONE_REACTIVE":
                            cell.setCellValue("Không phản ứng");
                            break;

                        case "REACTIVE":
                            cell.setCellValue("Có phản ứng");
                            break;

                        case "OTHER":
                            cell.setCellValue("Khác");
                            break;

                        default:
                            break;
                    }

                } else {
                    cell.setCellValue("-");
                }

                // Kết quả xét nghiệm khẳng định
                cell = row.createCell(colIndex++, CellType.STRING);
                cell.setCellStyle(cellStyle);

                if (sts.getConfirmResult() != null) {
                    switch (sts.getConfirmResult()) {
                        case "POSITIVE":
                            cell.setCellValue("Dương tính");
                            break;

                        case "NEGATIVE":
                            cell.setCellValue("Âm tính");
                            break;

                        case "OTHER":
                            cell.setCellValue("Khác");
                            break;

                        default:
                            break;
                    }

                } else {
                    cell.setCellValue("-");
                }
            }
        }

        // Auto-filter
        if (rowIndex >= 3) {
            sheet.setAutoFilter(CellRangeAddress.valueOf("A3:Q" + rowIndex));
        }

        return wbook;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SelfTestEntryDto saveOne(SelfTestEntryDto dto) {
        User currentUser = SecurityUtils.getCurrentUser();

        Boolean isSite = SecurityUtils.isUserInRole(currentUser, "ROLE_SITE_MANAGER");
        Boolean isProvince = SecurityUtils.isUserInRole(currentUser, "ROLE_PROVINCIAL_MANAGER");
        Boolean isAdministrator = SecurityUtils.isUserInRole(currentUser, "ROLE_ADMIN");
        List<UserOrganization> listUO = null;
        if (!isAdministrator) {
            listUO = userOrganizationRepository.getListByUserId(currentUser.getId());
        }

        if (!isSite && !isProvince && !isAdministrator) {
            return null;
        }
        final List<Long> writableOrgIds = authUtils.getGrantedOrgIds(Permission.WRITE_ACCESS);

        if ((writableOrgIds == null || writableOrgIds.size() == 0) && isAdministrator == false) {
            return null;
        }
        if (dto == null) {
            throw new IllegalArgumentException("Cannot save a null instance of selfTest.");
        }


        if (dto.getOrganization() == null || dto.getDispensingStaff() == null
                || !CommonUtils.isPositive(dto.getOrganization().getId(), true)
                || !CommonUtils.isPositive(dto.getDispensingStaff().getId(), true)) {
            throw new IllegalArgumentException("Invalid organization/dispensing staff when saving a selftest entry.");
        }

        List<Long> grantedOrgIds = authUtils.getGrantedOrgIds(Permission.WRITE_ACCESS);

        if (CommonUtils.isEmpty(grantedOrgIds) || !grantedOrgIds.contains(dto.getOrganization().getId().longValue())) {
            throw new AccessDeniedException("Unauthorized access to organization data when saving a selftest entry.");
        }

        Organization org = orgRepos.findOne(dto.getOrganization().getId());
        Staff staff = staffRepos.findOne(dto.getDispensingStaff().getId());

        if (org == null || staff == null) {
            throw new RuntimeException("Invalid organization/staff data when saving a selftest entry.");
        }

        SelfTestEntry entity = null;
        PersonDto personDto = dto.getPerson();
        Person person = null;
        boolean addingNewEntry = false;

        if (personDto != null && CommonUtils.isPositive(personDto.getId(), true)) {
            person = personRepos.findOne(dto.getPerson().getId());
        }

        if (person == null) {
            person = new Person();
        }

        if (CommonUtils.isPositive(dto.getId(), true)) {
            entity = repos.findOne(dto.getId());
        }

        if (entity == null) {
            entity = dto.toEntity();
            addingNewEntry = true;
        } else {
            entity.setDispensingDate(dto.getDispensingDate());
        }

        if (personDto != null) {
            person.setNidNumber(personDto.getNidNumber());
            person.setNidIssuedDate(personDto.getNidIssuedDate());
            person.setNidIssuedBy(personDto.getNidIssuedBy());
            person.setNoNidReason(personDto.getNoNidReason());
            person.setPassportNumber(personDto.getPassportNumber());
            person.setPassportIssuedDate(personDto.getPassportIssuedDate());
            person.setPassportIssuedBy(personDto.getPassportIssuedBy());
            person.setPassportExpiryDate(personDto.getPassportExpiryDate());
            person.setFullname(personDto.getFullname());
            person.setDob(personDto.getDob());
            person.setGender(personDto.getGender());
            person.setMaritalStatus(personDto.getMaritalStatus());
            person.setMobilePhone(personDto.getMobilePhone());
            person.setHomePhone(personDto.getHomePhone());
            person.setEmailAddress(personDto.getEmailAddress());
            person.setHeight(personDto.getHeight());
            person.setWeight(personDto.getWeight());
            person.setImage(personDto.getImage());

            // Locations
            List<Location> locations = new ArrayList<>();
            for (LocationDto loc : personDto.getLocations()) {
                Location address = null;

                if (CommonUtils.isPositive(loc.getId(), true)) {
                    address = locationRepos.findOne(loc.getId());
                }

                if (address == null && CommonUtils.isPositive(personDto.getId(), true)
                        && loc.getAddressType() != null) {
                    List<Location> locs = locationRepos.findForWRCase(personDto.getId(), loc.getAddressType());

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
                    commune = adminUnitRepos.findOne(loc.getCommune().getId());
                }

                if (loc.getDistrict() != null && CommonUtils.isPositive(loc.getDistrict().getId(), true)) {
                    district = adminUnitRepos.findOne(loc.getDistrict().getId());
                }

                if (loc.getProvince() != null && CommonUtils.isPositive(loc.getProvince().getId(), true)) {
                    province = adminUnitRepos.findOne(loc.getProvince().getId());
                }

                if (loc.getCountry() != null) {
                    if (CommonUtils.isPositive(loc.getCountry().getId(), true)) {
                        country = adminUnitRepos.findOne(loc.getCountry().getId());
                    }
                    if (country == null && !CommonUtils.isEmpty(loc.getCountry().getCode())) {
                        country = adminUnitRepos
                                .findOne(QAdminUnit.adminUnit.code.equalsIgnoreCase(loc.getCountry().getCode()));
                    }
                }

                address.setCommune(commune);
                address.setDistrict(district);
                address.setProvince(province);
                address.setCountry(country);
                address.setPerson(person);

                locations.add(address);
            }

            person.getLocations().clear();
            person.getLocations().addAll(locations);
        }

        entity.setOrganization(org);
        entity.setDispensingStaff(staff);
        entity.setPerson(person);
        entity.setSelfTestSource(dto.getSelfTestSource());

        entity = repos.save(entity);

        if (entity != null) {

            // add the first specimen record when adding a new entry
            if (addingNewEntry) {
                Iterator<SelfTestSpecimenDto> specimenDtos = dto.getSpecimens().iterator();
                SelfTestSpecimenDto specimenDto = null;

                if (specimenDtos.hasNext()) {
                    specimenDto = specimenDtos.next();
                }

                if (specimenDto == null) {
                    throw new RuntimeException("When saving a new self test entry, specimen data is required.");
                }

                // first specimen record
                SelfTestSpecimen specimen = new SelfTestSpecimen();

                specimen.setCode(specimenDto.getCode());
                specimen.setName(specimenDto.getName());
                specimen.setSupport(specimenDto.getSupport());
                specimen.setClient(specimenDto.getClient());
                specimen.setClientGender(specimenDto.getClientGender());
                specimen.setClientYob(specimenDto.getClientYob());
                specimen.setClientRiskGroup(specimenDto.getClientRiskGroup());
                specimen.setScreenResult(specimenDto.getScreenResult());
                specimen.setConfirmResult(specimen.getConfirmResult());
                specimen.setDispensingDate(specimenDto.getDispensingDate());

                specimen.setSelfTest(entity);
                entity.getSpecimens().add(specimen);

                entity = repos.save(entity);
            }

            return newSelfTestEntryDto(entity, currentUser, isSite, isProvince, isAdministrator, listUO);
        } else {
            throw new RuntimeException("Error saving a selftest entity.");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SelfTestEntryDto saveSpecimen2Entry(SelfTestSpecimenDto dto) {


        User currentUser = SecurityUtils.getCurrentUser();

        Boolean isSite = SecurityUtils.isUserInRole(currentUser, "ROLE_SITE_MANAGER");
        Boolean isProvince = SecurityUtils.isUserInRole(currentUser, "ROLE_PROVINCIAL_MANAGER");
        Boolean isAdministrator = SecurityUtils.isUserInRole(currentUser, "ROLE_ADMIN");
        List<UserOrganization> listUO = null;
        if (!isAdministrator) {
            listUO = userOrganizationRepository.getListByUserId(currentUser.getId());
        }

        if (!isSite && !isProvince && !isAdministrator) {
            return null;
        }
        final List<Long> writableOrgIds = authUtils.getGrantedOrgIds(Permission.WRITE_ACCESS);

        if ((writableOrgIds == null || writableOrgIds.size() == 0) && isAdministrator == false) {
            return null;
        }
        if (dto == null || dto.getSelfTest() == null || !CommonUtils.isPositive(dto.getSelfTest().getId(), true)) {
            throw new IllegalArgumentException("Invalid self-test specimen data for saving.");
        }


        SelfTestEntry ste = repos.findOne(dto.getSelfTest().getId());

        if (ste == null) {
            throw new RuntimeException("Could not find a selftest entry to add a specimen record.");
        }

        List<Long> grantedOrgIds = authUtils.getGrantedOrgIds(Permission.WRITE_ACCESS);

        if (CommonUtils.isEmpty(grantedOrgIds) || !grantedOrgIds.contains(ste.getOrganization().getId().longValue())) {
            throw new AccessDeniedException("Unauthorized access to organization data when saving a selftest entry.");
        }

        SelfTestSpecimen entity = null;

        if (CommonUtils.isPositive(dto.getId(), true)) {
            entity = specRepos.findOne(dto.getId());
        }

        if (entity == null) {
            entity = dto.toEntity();
        } else {
            entity.setClient(dto.getClient());
            entity.setName(dto.getName());
            entity.setSupport(dto.getSupport());
            entity.setClient(dto.getClient());
            entity.setClientGender(dto.getClientGender());
            entity.setClientYob(dto.getClientYob());
            entity.setClientRiskGroup(dto.getClientRiskGroup());
            entity.setScreenResult(dto.getScreenResult());
            entity.setConfirmResult(dto.getConfirmResult());
            entity.setDispensingDate(dto.getDispensingDate());
        }

        entity.setSelfTest(ste);

        entity = specRepos.save(entity);

        if (entity != null) {
            ste = repos.findOne(ste.getId());

            return newSelfTestEntryDto(ste, currentUser, isSite, isProvince, isAdministrator, listUO);
        } else {
            throw new RuntimeException("Error saving a specimen entry.");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMultiple(SelfTestEntryDto[] dtos) {
        User currentUser = SecurityUtils.getCurrentUser();
        Boolean isSiteManagement = SecurityUtils.isUserInRole(currentUser, "ROLE_SITE_MANAGER");
        if (!isSiteManagement) {
            return;
        }
        if (CommonUtils.isEmpty(dtos)) {
            return;
        }

        for (SelfTestEntryDto dto : dtos) {
            if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
                continue;
            }

            SelfTestEntry entity = repos.findOne(dto.getId());

            if (entity != null) {
                repos.delete(entity);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSpecimens(SelfTestSpecimenDto[] dtos) {
        User currentUser = SecurityUtils.getCurrentUser();
        Boolean isSiteManagement = SecurityUtils.isUserInRole(currentUser, "ROLE_SITE_MANAGER");
        if (!isSiteManagement) {
            return;
        }
        if (CommonUtils.isEmpty(dtos)) {
            return;
        }

        for (SelfTestSpecimenDto dto : dtos) {
            if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
                continue;
            }

            SelfTestSpecimen entity = specRepos.findOne(dto.getId());

            if (entity != null) {
                specRepos.delete(entity);
            }
        }
    }

    /**
     * Create self test search criteria
     *
     * @param filter
     * @return
     */
    private BooleanExpression createSearchCriteria(SelfTestFilterDto filter) {
        List<Long> grantedIds = authUtils.getGrantedOrgIds(Permission.READ_ACCESS);
        List<Long> grantedSelfTestIds = authUtils.getGrantedOrgIds(Permission.SELFTEST_ACCESS);
        List<Long> orgIds = new ArrayList<>();
        List<Long> staffIds = new ArrayList<>();

        User currentUser = SecurityUtils.getCurrentUser();
        Boolean isAdministrator = SecurityUtils.isUserInRole(currentUser, "ROLE_ADMIN");
        Boolean isProvince = SecurityUtils.isUserInRole(currentUser, "ROLE_PROVINCIAL_MANAGER");
        Boolean isSite = SecurityUtils.isUserInRole(currentUser, "ROLE_SITE_MANAGER");

        Boolean isDONOR = SecurityUtils.isUserInRole(currentUser, "ROLE_DONOR");
        Boolean isNATIONAL = SecurityUtils.isUserInRole(currentUser, "ROLE_NATIONAL_MANAGER");
        Boolean isDISTRICT = SecurityUtils.isUserInRole(currentUser, "ROLE_DISTRICT_MANAGER");

        if (filter.getOrganization() != null) {
            Long id = filter.getOrganization().getId();
            if ((CommonUtils.isPositive(id, true) && grantedIds.contains(id.longValue())) || isAdministrator) {
                orgIds.add(id);
            }
        } else {
            orgIds = grantedIds;
        }

        if (filter.getStaff() != null) {
            Long id = filter.getStaff().getId();
            if (CommonUtils.isPositive(id, true)) {
                staffIds.add(id);
            }
        }

        QSelfTestEntry q = QSelfTestEntry.selfTestEntry;
        BooleanExpression be = q.id.isNotNull();

        if (!CommonUtils.isEmpty(filter.getKeyword())) {
            be = be.and(q.person.fullname.containsIgnoreCase(filter.getKeyword())
                    .or(q.dispensingStaff.person.fullname.containsIgnoreCase(filter.getKeyword())));
        }
        if (!isAdministrator) {
            if (CommonUtils.isEmpty(orgIds)) {
                be = be.and(q.organization.id.eq(0l)); // Return empty result if don't supply with a valid organization
                // ID
            } else {
                be = be.and(q.organization.id.longValue().in(orgIds));
            }

            if (isSite && !isAdministrator && !isDONOR && !isNATIONAL && !isProvince && !isDISTRICT) {
                if (CommonUtils.isEmpty(grantedSelfTestIds)) {
                    be = be.and(q.organization.id.eq(0l)); // Return empty result if don't supply with a valid organization
                    // ID
                } else {
                    be = be.and(q.organization.id.longValue().in(grantedSelfTestIds));
                }
            }
        }

        if (filter.getOrganization() != null && filter.getOrganization().getId() != null && filter.getOrganization().getId() > 0L) {
            be = be.and(q.organization.id.eq(filter.getOrganization().getId()));
        }

        if (!CommonUtils.isEmpty(staffIds)) {
            be = be.and(q.dispensingStaff.id.longValue().in(staffIds));
        }

        if (filter.getDispensingDateFrom() != null) {
            be = be.and(q.dispensingDate.goe(filter.getDispensingDateFrom()));
        }

        if (filter.getDispensingDateTo() != null) {
            be = be.and(q.dispensingDate.loe(filter.getDispensingDateTo()));
        }

        if (!CommonUtils.isEmpty(filter.getSpecimen())) {
            be = be.and(q.specimens.any().code.equalsIgnoreCase(filter.getSpecimen()));
        }

        if (filter.getProvinceId() != null) {
        	if(q.organization.address.province!=null && q.organization.address.province.id!=null) {
        		be = be.and(q.organization.address.province.id.eq(filter.getProvinceId()));
        	}
        }

        return be;
    }

    @Override
    public List<OrganizationDto> getListSelfTestWriteAble() {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return null;
        }
        List<OrganizationDto> ret = new ArrayList<OrganizationDto>();
        List<UserOrganization> listUO = userOrganizationRepository.getListByUserId(currentUser.getId());
        for (UserOrganization userOrganization : listUO) {
            if (userOrganization.getWriteAccess() != null && userOrganization.getWriteAccess()
                    && userOrganization.getSelfTestRole() && userOrganization.getSelfTestRole()) {
                ret.add(new OrganizationDto(userOrganization.getOrganization()));
            }
        }
        return ret;
    }

    @Override
    public Workbook exportReportSelfTestNew(PreventionFilterDto filter, Workbook workbook) {
//        Workbook blankBook = new XSSFWorkbook();
//        blankBook.createSheet();
//        if (filter.getProvinceId() != null && CollectionUtils.isEmpty(filter.getOrgIds())) {
//            filter.setOrgIds(organizationRepository.findAllIdByProvinceId(filter.getProvinceId()));
//        }
        List<SelfTestReportNewDto> result = this.selfTestSpecimenService.getReportNew(filter);
        if (result == null) {
            return workbook;
        }
        XSSFWorkbook wbook = (XSSFWorkbook) workbook;
//        try (InputStream template = context.getResource("classpath:templates/Template20211228.xlsx").getInputStream()) {
//            wbook = new XSSFWorkbook(template);
//        } catch (IOException e) {
//            System.out.println("Lỗi đọc file excel template Template20211228.xlsx");
//        }
//        if (wbook == null) {
//            return blankBook;
//        }

        int rowIndex = 5;
        int colIndex = 0;

        Row row = null;
        Cell cell = null;
        Sheet sheet = wbook.getSheet("HTS_SELF");
        CellStyle cellStyle = wbook.createCellStyle();
        ExcelUtils.setBorders4Style(cellStyle);
        cellStyle.setWrapText(true);
        cellStyle.setAlignment(HorizontalAlignment.RIGHT);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        for (SelfTestReportNewDto dto : result) {
            row = sheet.createRow(rowIndex++);
            colIndex = 0;
            if (row != null) {
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getOrgCode());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getOrgName());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getProvinceName());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getDistrictName());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue("");
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getpWIDAssisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getmSMAssisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.gettGAssisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getfSWAssisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getOtherAssisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getFuAssisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF10Assisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF15Assisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF20Assisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF25Assisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF30Assisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF35Assisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF40Assisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF45Assisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF50Assisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getMuAssisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM10Assisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM15Assisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM20Assisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM25Assisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM30Assisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM35Assisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM40Assisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM45Assisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM50Assisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getTotalAssisted());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getpWIDNotAssisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getmSMNotAssisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.gettGNotAssisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getfSWNotAssisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getOtherNotAssisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getTestSelfNotAssisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getTestPartnerNotAssisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getTestOtherNotAssisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getuSANotAssisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getFuNotAssisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF10NotAssisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF15NotAssisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF20NotAssisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF25NotAssisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }


                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF30NotAssisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF35NotAssisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF40NotAssisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF45NotAssisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF50NotAssisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getMuNotAssisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM10NotAssisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM15NotAssisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM20NotAssisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM25NotAssisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM30NotAssisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM35NotAssisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM40NotAssisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM45NotAssisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM50NotAssisted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getTotalNotAssisted());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getTotal());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

            }
        }

        return wbook;
    }
}
