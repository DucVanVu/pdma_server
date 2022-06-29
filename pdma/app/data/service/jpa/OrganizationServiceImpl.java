package org.pepfar.pdma.app.data.service.jpa;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pepfar.pdma.app.Constants;
import org.pepfar.pdma.app.data.domain.AdminUnit;
import org.pepfar.pdma.app.data.domain.Location;
import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.domain.QAdminUnit;
import org.pepfar.pdma.app.data.domain.QOrganization;
import org.pepfar.pdma.app.data.domain.Service;
import org.pepfar.pdma.app.data.domain.ServiceOrganization;
import org.pepfar.pdma.app.data.domain.ServiceOrganizationPK;
import org.pepfar.pdma.app.data.dto.AdminUnitDto;
import org.pepfar.pdma.app.data.dto.ImportResultDto;
import org.pepfar.pdma.app.data.dto.LocationDto;
import org.pepfar.pdma.app.data.dto.OrganizationDto;
import org.pepfar.pdma.app.data.dto.OrganizationFilterDto;
import org.pepfar.pdma.app.data.dto.ServiceOrganizationDto;
import org.pepfar.pdma.app.data.repository.AdminUnitRepository;
import org.pepfar.pdma.app.data.repository.LocationRepository;
import org.pepfar.pdma.app.data.repository.OrganizationRepository;
import org.pepfar.pdma.app.data.repository.ServiceOrganizationRepository;
import org.pepfar.pdma.app.data.repository.ServiceRepository;
import org.pepfar.pdma.app.data.service.OrganizationService;
import org.pepfar.pdma.app.data.types.AddressType;
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
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.dsl.BooleanExpression;

@org.springframework.stereotype.Service
public class OrganizationServiceImpl implements OrganizationService {

    @Autowired
    private OrganizationRepository repos;

    @Autowired
    private ServiceRepository serviceRepos;

    @Autowired
    private ServiceOrganizationRepository soRepos;

    @Autowired
    private LocationRepository locationRepos;

    @Autowired
    private AdminUnitRepository adminUnitRepos;

    @Autowired
    private AuthorizationUtils authUtils;

    @Override
    @Transactional(readOnly = true)
    public OrganizationDto findById(Long id) {

        if (!CommonUtils.isPositive(id, true)) {
            return null;
        }

        Organization entity = repos.findOne(id);

        if (entity != null) {
            return new OrganizationDto(entity);
        }

        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationDto findByCode(String code) {
        if (CommonUtils.isEmpty(code)) {
            return null;
        }

        QOrganization q = QOrganization.organization;
        Organization entity = repos.findOne(q.code.isNotNull().and(q.code.equalsIgnoreCase(code)));

        if (entity != null) {
            return new OrganizationDto(entity);
        }

        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return repos.count();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationDto> findAll(OrganizationFilterDto filter) {

        QOrganization q = QOrganization.organization;
        BooleanExpression be = q.id.isNotNull();

        if (filter == null) {
            filter = new OrganizationFilterDto();
        }

        if (filter.getActiveOnly() != null && filter.getActiveOnly()) {
            be = be.and(q.active.isTrue());
        }

        if (filter.getPepfarSiteOnly() != null && filter.getPepfarSiteOnly()) {
            be = be.and(q.pepfarSite.isTrue());
        }

        if (filter.getHtsSiteOnly() != null && filter.getHtsSiteOnly()) {
            be = be.and(q.htsSite.isTrue());
        }

        if (filter.getOpcSiteOnly() != null && filter.getOpcSiteOnly()) {
            be = be.and(q.opcSite.isTrue());
        }

        if (filter.getPrepSiteOnly() != null && filter.getPrepSiteOnly()) {
            be = be.and(q.prepSite.isTrue());
        }

        if (filter.getPnsSiteOnly() != null && filter.getPnsSiteOnly()) {
            be = be.and(q.pnsSite.isTrue());
        }

        if (filter.getProvince() != null && CommonUtils.isPositive(filter.getProvince().getId(), true)) {
            be = be.and(q.address.province.id.longValue().eq(filter.getProvince().getId()));
        }

        if (filter.getDistrict() != null && CommonUtils.isPositive(filter.getDistrict().getId(), true)) {
            be = be.and(q.address.district.id.longValue().eq(filter.getDistrict().getId()));
        }

        if (!CommonUtils.isEmpty(filter.getKeyword())) {
            be = be.and(q.name.containsIgnoreCase(filter.getKeyword())
                    .or(q.address.streetAddress.containsIgnoreCase(filter.getKeyword())
                            .or(q.address.province.name.containsIgnoreCase(filter.getKeyword())
                                    .or(q.address.district.name.containsIgnoreCase(filter.getKeyword())))));
        }

        if (!CommonUtils.isEmpty(filter.getProvinceIds())) {
            be = be.and(q.address.province.id.longValue().in(filter.getProvinceIds()));
        }

        // Consider also user permission on the organizations
        if (CommonUtils.isTrue(filter.getCheckUserPermission())) {
            List<Long> grantedOrgIds = authUtils.getGrantedOrgIds(Permission.READ_ACCESS);
            if (grantedOrgIds.size() > 0) {
                be = be.and(q.id.longValue().in(grantedOrgIds));
            } else {
                // make sure the returned list is empty
                be = be.and(q.id.eq(0l));
            }
        }

        List<OrganizationDto> dtos = new ArrayList<>();
        Iterator<Organization> itr = repos.findAll(be, new Sort(new Order(Direction.ASC, "name"))).iterator();

        while (itr.hasNext()) {
            Organization e = itr.next();

            // Avoid returning other org
            if (!CommonUtils.isEmpty(e.getCode()) && Constants.CODE_ORGANIZATION_OTHER.equalsIgnoreCase(e.getCode())) {
                continue;
            }

            if (filter.isCompact()) {
                OrganizationDto dto = new OrganizationDto();
                dto.setId(e.getId());
                dto.setName(e.getName());

                Location loc = e.getAddress();
                LocationDto locDto = new LocationDto();
                locDto.setId(loc.getId());

                if (loc.getProvince() != null) {
                    AdminUnitDto auDto = new AdminUnitDto();
                    auDto.setId(loc.getProvince().getId());
                    auDto.setName(loc.getProvince().getName());
                    locDto.setProvince(auDto);
                }

                if (loc.getDistrict() != null) {
                    AdminUnitDto auDto = new AdminUnitDto();
                    auDto.setId(loc.getDistrict().getId());
                    auDto.setName(loc.getDistrict().getName());
                    locDto.setDistrict(auDto);
                }

                dto.setAddress(locDto);
                dtos.add(dto);
            } else {
                dtos.add(new OrganizationDto(e));
            }

        }

        return dtos;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrganizationDto> findAllPageable(OrganizationFilterDto filter) {

        if (filter == null) {
            filter = new OrganizationFilterDto();
        }

        if (filter.getPageIndex() < 0) {
            filter.setPageIndex(0);
        }

        if (filter.getPageSize() <= 0) {
            filter.setPageSize(25);
        }

        QOrganization q = QOrganization.organization;
        BooleanExpression be = q.id.isNotNull();

        if (filter.getActiveOnly() != null && filter.getActiveOnly()) {
            be = be.and(q.active.isTrue());
        }

        if (filter.getPepfarSiteOnly() != null && filter.getPepfarSiteOnly()) {
            be = be.and(q.pepfarSite.isTrue());
        }

        if (filter.getHtsSiteOnly() != null && filter.getHtsSiteOnly()) {
            be = be.and(q.htsSite.isTrue());
        }

        if (filter.getOpcSiteOnly() != null && filter.getOpcSiteOnly()) {
            be = be.and(q.opcSite.isTrue());
        }

        if (filter.getPrepSiteOnly() != null && filter.getPrepSiteOnly()) {
            be = be.and(q.prepSite.isTrue());
        }

        if (filter.getPnsSiteOnly() != null && filter.getPnsSiteOnly()) {
            be = be.and(q.pnsSite.isTrue());
        }

        if (filter.getProvince() != null && CommonUtils.isPositive(filter.getProvince().getId(), true)) {
            be = be.and(q.address.province.id.longValue().eq(filter.getProvince().getId()));
        }

        if (filter.getDistrict() != null && CommonUtils.isPositive(filter.getDistrict().getId(), true)) {
            be = be.and(q.address.district.id.longValue().eq(filter.getDistrict().getId()));
        }

        if (!CommonUtils.isEmpty(filter.getKeyword())) {
            be = be.and(q.name.containsIgnoreCase(filter.getKeyword())
                    .or(q.address.streetAddress.containsIgnoreCase(filter.getKeyword())
                            .or(q.address.province.name.containsIgnoreCase(filter.getKeyword())
                                    .or(q.address.district.name.containsIgnoreCase(filter.getKeyword())))));
        }

        Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize(),
                new Sort(new Order(Direction.ASC, "name")));
        Page<Organization> page = repos.findAll(be, pageable);
        List<OrganizationDto> content = new ArrayList<>();

        page.getContent().parallelStream().forEachOrdered(o -> {

            if (!CommonUtils.isEmpty(o.getCode()) && Constants.CODE_ORGANIZATION_OTHER.equalsIgnoreCase(o.getCode())) {
                return;
            }

            content.add(new OrganizationDto(o));
        });

        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrganizationDto saveOne(OrganizationDto dto) {

        if (dto == null) {
            throw new IllegalArgumentException("Organization could not be null.");
        }

        Organization entity = null;

        if (CommonUtils.isPositive(dto.getId(), true)) {
            entity = repos.findOne(dto.getId());
        }
        if (dto.getCode() != null) {
            entity = repos.findByOrgCode(dto.getCode());
        }

        if (entity == null) {
            entity = dto.toEntity();
        } else {
            entity.setCode(dto.getCode());
            entity.setName(dto.getName());
            entity.setActive(dto.getActive());
            entity.setPepfarSite(dto.getPepfarSite());
            entity.setHtsSite(dto.getHtsSite());
            entity.setOpcSite(dto.getOpcSite());
            entity.setPrepSite(dto.getPrepSite());
            entity.setPnsSite(dto.getPnsSite());
            entity.setDescription(dto.getDescription());
            entity.setEmailAddress(dto.getEmailAddress());
            entity.setFaxNumber1(dto.getFaxNumber1());
            entity.setFaxNumber2(dto.getFaxNumber2());
            entity.setPhoneNumber1(dto.getPhoneNumber1());
            entity.setPhoneNumber2(dto.getPhoneNumber2());
            entity.setWebsiteAddress(dto.getWebsiteAddress());
        }
        entity.setCanNotBeCompleted(dto.getCanNotBeCompleted());
        // Address
        Location address = null;
        if (dto.getAddress() != null) {
            LocationDto addressDto = dto.getAddress();

            if (CommonUtils.isPositive(addressDto.getId(), true)) {
                address = locationRepos.findOne(addressDto.getId());
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

            if (addressDto.getDistrict() != null) {
                if (CommonUtils.isPositive(addressDto.getDistrict().getId(), true)) {
                    district = adminUnitRepos.findOne(addressDto.getDistrict().getId());
                } else if (!CommonUtils.isEmpty(addressDto.getDistrict().getCode())) {
                    district = adminUnitRepos.findOne(QAdminUnit.adminUnit.code.eq(addressDto.getDistrict().getCode()));
                }
            }

            if (addressDto.getProvince() != null) {
                if (CommonUtils.isPositive(addressDto.getProvince().getId(), true)) {
                    province = adminUnitRepos.findOne(addressDto.getProvince().getId());
                } else if (!CommonUtils.isEmpty(addressDto.getProvince().getCode())) {
                    province = adminUnitRepos.findOne(QAdminUnit.adminUnit.code.eq(addressDto.getProvince().getCode()));
                }
            }

            if (addressDto.getCountry() != null) {
                if (CommonUtils.isPositive(addressDto.getCountry().getId(), true)) {
                    country = adminUnitRepos.findOne(addressDto.getCountry().getId());
                } else if (!CommonUtils.isEmpty(addressDto.getCountry().getCode())) {
                    country = adminUnitRepos.findOne(QAdminUnit.adminUnit.code.eq(addressDto.getCountry().getCode()));
                }
            }

            address.setDistrict(district);
            address.setProvince(province);
            address.setCountry(country);
            address.setAddressType(AddressType.OFFICE_ADDRESS);
        }

        entity.setAddress(address);

        // Parent
        Organization parent = null;
        if (dto.getParent() != null && CommonUtils.isPositive(dto.getParent().getId(), true)) {
            parent = repos.findOne(dto.getParent().getId());
        }

        entity.setParent(parent);

        // Level
        if (parent != null) {
            entity.setLevel(parent.getLevel() + 1);
        } else {
            entity.setLevel(1);
        }

        entity = repos.save(entity);

        if (entity != null) {
            return new OrganizationDto(entity);
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrganizationDto attachService(ServiceOrganizationDto dto) {

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Expecting a non-null argument when attaching a service to an organization.");
        }

        if (dto.getOrganization() == null || dto.getService() == null) {
            return dto.getOrganization();
        }

        Organization orgE = null;
        Service serviceE = null;

        if (CommonUtils.isPositive(dto.getOrganization().getId(), true)) {
            orgE = repos.findOne(dto.getOrganization().getId());
        }

        if (CommonUtils.isPositive(dto.getService().getId(), true)) {
            serviceE = serviceRepos.findOne(dto.getService().getId());
        }

        if (orgE == null || serviceE == null) {
            throw new IllegalArgumentException("Organization and/or service not found.");
        }

        ServiceOrganization entity = new ServiceOrganization();
        entity.setService(serviceE);
        entity.setOrganization(orgE);

        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setEndingReason(dto.getEndingReason());
        entity.setActive(dto.getActive());

        entity = soRepos.save(entity);

        if (entity != null) {
            return new OrganizationDto(entity.getOrganization());
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrganizationDto detachService(ServiceOrganizationDto dto) {

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Expecting a non-null argument when attaching a service to an organization.");
        }

        if (dto.getOrganization() == null || dto.getService() == null) {
            return dto.getOrganization();
        }

        Organization orgE = null;
        Service serviceE = null;

        if (CommonUtils.isPositive(dto.getOrganization().getId(), true)) {
            orgE = repos.findOne(dto.getOrganization().getId());
        }

        if (CommonUtils.isPositive(dto.getService().getId(), true)) {
            serviceE = serviceRepos.findOne(dto.getService().getId());
        }

        if (orgE == null || serviceE == null) {
            throw new IllegalArgumentException("Organization and/or service not found.");
        }

        ServiceOrganizationPK pk = new ServiceOrganizationPK();
        pk.setOrganization(orgE.getId());
        pk.setService(serviceE.getId());

        try {
            soRepos.delete(pk);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        orgE = repos.findOne(orgE.getId());

        if (orgE != null) {
            return new OrganizationDto(orgE);
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMultiple(OrganizationDto[] dtos) {

        if (CommonUtils.isEmpty(dtos)) {
            return;
        }

        for (OrganizationDto dto : dtos) {
            if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
                continue;
            }

            Organization entity = repos.findOne(dto.getId());

            if (entity != null) {
                repos.delete(entity);
            }
        }
    }

    @Override
    public ImportResultDto<OrganizationDto> importFromExcel(InputStream is) throws IOException {
        Workbook workbook = new XSSFWorkbook(is);
        Sheet datatypeSheet = workbook.getSheetAt(0);
        // Iterator<Row> iterator = datatypeSheet.iterator();
        int rowIndex = 1;
        int num = datatypeSheet.getLastRowNum();
        ImportResultDto<OrganizationDto> ret = new ImportResultDto<OrganizationDto>();
        while (rowIndex <= num) {
            try {
                System.out.println(rowIndex);
                Row currentRow = datatypeSheet.getRow(rowIndex);
                Cell currentCell = null;
                if (currentRow != null) {
                    OrganizationDto dto = new OrganizationDto();
                    String err = "";
                    try {
//						Mã số
                        currentCell = currentRow.getCell(0);
                        if (currentCell != null) {
                            String orgCode = "";
                            if (currentCell.getCellType() == CellType.STRING) {
                                orgCode = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                orgCode = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            if (orgCode.equals("")) {
                                break;
                            }
                            dto.setCode(orgCode);
                        }
                    } catch (Exception e) {
                        dto.setCode(null);
                        err += "Mã số - Không rõ; ";
                    }
                    try {
//						Tên
                        currentCell = currentRow.getCell(1);
                        if (currentCell != null) {
                            String orgName = "";
                            if (currentCell.getCellType() == CellType.STRING) {
                                orgName = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                orgName = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            dto.setName(orgName);
                        }
                    } catch (Exception e) {
                        dto.setName(null);
                        err += "Tên - Không rõ; ";
                    }
                    try {
//						Tình trạng
                        currentCell = currentRow.getCell(12);
                        if (currentCell != null) {
                            String active = "";
                            if (currentCell.getCellType() == CellType.STRING) {
                                active = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                active = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            if (!active.equals("")) {
                                if (active.equals("1")) {
                                    dto.setActive(true);
                                } else if (active.equals("0")) {
                                    dto.setActive(false);
                                } else {
                                    dto.setActive(null);
                                    err += "Tình trạng phải bằng 1 hoặc 0; ";
                                }
                            }
                        }
                    } catch (Exception e) {
                        dto.setActive(null);
                        err += "Tình trạng - Không rõ; ";
                    }
//						Mã Quận/huyện
                    LocationDto location = new LocationDto();
                    currentCell = currentRow.getCell(2);
                    if (currentCell != null) {
                        String districtCode = "";
                        if (currentCell.getCellType() == CellType.STRING) {
                            districtCode = currentCell.getStringCellValue();
                        } else if (currentCell.getCellType() == CellType.NUMERIC) {
                            districtCode = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                        }
                        try {
                            if (!districtCode.equals("")) {
                                if (districtCode.length() == 1) {
                                    districtCode = "00" + districtCode;
                                } else if (districtCode.length() == 2) {
                                    districtCode = "0" + districtCode;
                                }
                                AdminUnit adminUnit = adminUnitRepos.findByProvinceOrDistrict(districtCode);
                                if (adminUnit != null) {
                                    location.setDistrict(new AdminUnitDto(adminUnit));
                                } else {
                                    location.setDistrict(null);
                                    err += "Không tìm thấy mã quận/huyện; ";
                                }
                            }
                        } catch (Exception e) {
                            location.setDistrict(null);
                            err += "Mã quận/huyện - Không rõ; ";
                        }
                    }
//						Mã tỉnh
                    currentCell = currentRow.getCell(16);
                    if (currentCell != null) {
                        String provinceCode = "";
                        if (currentCell.getCachedFormulaResultType() == CellType.STRING) {
                            provinceCode = currentCell.getStringCellValue();
                        } else if (currentCell.getCachedFormulaResultType() == CellType.NUMERIC) {
                            provinceCode = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                        }
                        try {
                            if (!provinceCode.equals("")) {
                                if (provinceCode.length() == 1) {
                                    provinceCode = "0" + provinceCode;
                                }
                                AdminUnit adminUnit = adminUnitRepos.findByProvinceOrDistrict(provinceCode);
                                if (adminUnit != null) {
                                    location.setProvince(new AdminUnitDto(adminUnit));
                                } else {
                                    location.setProvince(null);
                                    err += "Không tìm thấy mã tỉnh; ";
                                }
                            }
                        } catch (Exception e) {
                            dto.setAddress(null);
                            err += "Mã tỉnh - Không rõ; ";
                        }
                    }
                    dto.setAddress(location);
// 				is_pepfar_site (tạm thời để false)
                    dto.setPepfarSite(false);

//          if (dto.getErrorContent() != null) {
//            dto.setNumberErrorContent(rowIndex + 1);
//            ret.setTotalErr(ret.getTotalErr() + 1);
//            ret.getListErr().add(dto);
//          } else {
//            if(dto.getActive() != null) {
//              try {
//                saveOne(dto);
//              } catch (Exception e) {
//                dto.setErrorContent("Không rõ");
//                dto.setNumberErrorContent(rowIndex + 1);
//                ret.setTotalErr(ret.getTotalErr() + 1);
//                ret.getListErr().add(dto);
//              }
//            }
//          }
//          dto.setErrorContent(err);
//          if (!dto.getErrorContent().equals("")) {
//            dto.setNumberErrorContent(rowIndex + 1);
//            ret.setTotalErr(ret.getTotalErr() + 1);
//            ret.getListErr().add(dto);
//          }
                    if (dto.getActive() != null) {
//            try {
//              if (saveOne(dto) != null) {
//                dto.setSaved(true);
//              } else {
//                dto.setSaved(false);
//                if (dto.getErrorContent().equals("")) {
//                  dto.setErrorContent("Lưu thất bại");
//                  dto.setNumberErrorContent(rowIndex + 1);
//                  ret.setTotalErr(ret.getTotalErr() + 1);
//                  ret.getListErr().add(dto);
//                } else {
//                  dto.setErrorContent(err + "Lưu thất bại");
//                }
//              }
//            } catch (Exception e) {
//              dto.setSaved(false);
//              if (dto.getErrorContent().equals("")) {
//                dto.setErrorContent("Lưu thất bại");
//                dto.setNumberErrorContent(rowIndex + 1);
//                ret.setTotalErr(ret.getTotalErr() + 1);
//                ret.getListErr().add(dto);
//              } else {
//                dto.setErrorContent(err + "Lưu thất bại");
//              }
//            }
                        try {
                            dto = saveOne(dto);
                            dto.setSaved(true);
                        } catch (Exception e) {
                            err += "Lưu thất bại:" + CommonUtils.getStackTrace(e);
                            ret.setTotalErr(ret.getTotalErr() + 1);
                            dto.setSaved(false);
                        }
                        dto.setErrorContent(err);
                        if (!dto.getErrorContent().equals("")) {
                            dto.setNumberErrorContent(rowIndex + 1);
                            ret.setTotalErr(ret.getTotalErr() + 1);
                            ret.getListErr().add(dto);
                        }
                    }
                }
            } catch (Exception e) {
                ret.setTotalErr(ret.getTotalErr() + 1);
                e.printStackTrace();
                // TODO: handle exception
            }
            rowIndex += 1;
        }

        return ret;
    }

    @Override
    public List<OrganizationDto> newImportFromExcel(InputStream is) throws IOException {
        Workbook workbook = new XSSFWorkbook(is);
        Sheet datatypeSheet = workbook.getSheetAt(0);
        int rowIndex = 1;
        int rowIndex1 = 1;
        int num = datatypeSheet.getLastRowNum();
        List<OrganizationDto> ret = new ArrayList<>();
        while (rowIndex <= num) {
            try {
                System.out.println(rowIndex);
                Row currentRow = datatypeSheet.getRow(rowIndex);
                Cell currentCell = null;
                if (currentRow != null) {
                    OrganizationDto dto = new OrganizationDto();
//						Mã số
                    currentCell = currentRow.getCell(11);
                    if (currentCell != null) {
                        String orgCode = "";
                        if (currentCell.getCellType() == CellType.STRING) {
                            orgCode = currentCell.getStringCellValue();
                        } else if (currentCell.getCellType() == CellType.NUMERIC) {
                            orgCode = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                        }
                        dto.setCode(orgCode);
                    }
                    if (dto.getCode() != null) {
                        if (dto.getCode().trim().equals("")) {
                            rowIndex += 1;
                            continue;
                        } else if (dto.getCode().trim().equals("-")) {
                            rowIndex += 1;
                            continue;
                        }
                    } else {
                        rowIndex += 1;
                        continue;
                    }
//						Tên
                    currentCell = currentRow.getCell(12);
                    if (currentCell != null) {
                        String orgName = "";
                        if (currentCell.getCellType() == CellType.STRING) {
                            orgName = currentCell.getStringCellValue();
                        } else if (currentCell.getCellType() == CellType.NUMERIC) {
                            orgName = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                        }
                        if (!orgName.trim().equals("")) {
                            Organization org = repos.findByOrgName(orgName);
                            if (org != null) {
                                org.setCode(dto.getCode());
                                dto = new OrganizationDto(org);
                            } else {
                                rowIndex += 1;
                                continue;
                            }
                        }
                    }
                    ret.add(dto);
                    saveOne(dto);
                }
            } catch (Exception e) {
                e.printStackTrace();
                // TODO: handle exception
            }
            rowIndex += 1;
        }
        return ret;
    }
}
//    }
//    List<String> sa = new ArrayList<String>();
//    for(int i = 1; i<=num; i++) {
//      Row currentRow = datatypeSheet.getRow(i);
//      Cell currentCell = null;
//      if (currentRow != null) {
//        currentCell = currentRow.getCell(11);
//        String orgCode = "";
//        if (currentCell == null) continue;
//        if (currentCell.getCellType() == CellType.STRING) {
//          orgCode = currentCell.getStringCellValue();
//        } else if (currentCell.getCellType() == CellType.NUMERIC) {
//          orgCode = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
//        }
//        if(orgCode.trim().equals("") || orgCode.trim().equals("-")) {
//          continue;
//        } else {
//          System.out.println("duc"+i);
//          for(int j = i+1; j<=num; j++) {
//            Row currentRow1 = datatypeSheet.getRow(j);
//            Cell currentCell1 = null;
//            if (currentRow1 != null) {
//              currentCell1 = currentRow1.getCell(11);
//              if (currentCell1 == null) continue;
//              String orgCode1 = "";
//              if (currentCell1.getCellType() == CellType.STRING) {
//                orgCode1 = currentCell1.getStringCellValue();
//              } else if (currentCell1.getCellType() == CellType.NUMERIC) {
//                orgCode1 = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
//              }
//              if(orgCode1.trim().equals("") || orgCode1.trim().equals("-")) {
//                continue;
//              } else {
//                if(orgCode.equals(orgCode1)) {
//                  sa.add(orgCode);
//                }
//              }
//            } else {
//              continue;
//            }
//          }
//        }
//      } else {
//        continue;
//      }
//    }
//    for(int x = 0; x<sa.size(); x++) {
//      System.out.println(sa.get(x));
//    }