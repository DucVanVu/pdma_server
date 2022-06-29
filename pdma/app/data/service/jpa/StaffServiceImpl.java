package org.pepfar.pdma.app.data.service.jpa;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pepfar.pdma.app.data.domain.AdminUnit;
import org.pepfar.pdma.app.data.domain.Dictionary;
import org.pepfar.pdma.app.data.domain.Location;
import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.domain.Person;
import org.pepfar.pdma.app.data.domain.QAdminUnit;
import org.pepfar.pdma.app.data.domain.QStaff;
import org.pepfar.pdma.app.data.domain.Staff;
import org.pepfar.pdma.app.data.dto.*;
import org.pepfar.pdma.app.data.repository.AdminUnitRepository;
import org.pepfar.pdma.app.data.repository.DictionaryRepository;
import org.pepfar.pdma.app.data.repository.LocationRepository;
import org.pepfar.pdma.app.data.repository.OrganizationRepository;
import org.pepfar.pdma.app.data.repository.PersonRepository;
import org.pepfar.pdma.app.data.repository.StaffRepository;
import org.pepfar.pdma.app.data.service.StaffService;
import org.pepfar.pdma.app.data.types.*;
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
import org.springframework.util.StringUtils;

@Service
public class StaffServiceImpl implements StaffService {

  @Autowired
  private StaffRepository repos;

  @Autowired
  private OrganizationRepository orgRepos;

  @Autowired
  private PersonRepository personRepos;

  @Autowired
  private LocationRepository locationRepos;

  @Autowired
  private AdminUnitRepository adminUnitRepos;

  @Autowired
  private DictionaryRepository dicRepos;

  @Override
  @Transactional(readOnly = true)
  public StaffDto findById(Long id) {

    if (!CommonUtils.isPositive(id, true)) {
      return null;
    }

    Staff entity = repos.findOne(id);

    if (entity != null) {
      return new StaffDto(entity, false);
    }

    return null;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<StaffDto> findAllPageable(StaffFilterDto filter) {

    if (filter == null) {
      filter = new StaffFilterDto();
    }

    if (filter.getPageIndex() < 0) {
      filter.setPageIndex(0);
    }

    if (filter.getPageSize() <= 0) {
      filter.setPageSize(25);
    }

    final Sort defaultSort = new Sort(new Order(Direction.DESC, "createDate"));
    final Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize(), defaultSort);

    QStaff q = QStaff.staff;
    BooleanExpression be = q.id.isNotNull();

    if (filter.getOrganization() != null && CommonUtils.isPositive(filter.getOrganization().getId(), true)) {
      be = be.and(q.organization.id.longValue().eq(filter.getOrganization().getId()));
    } else {
      be = be.and(q.organization.id.eq(0l)); // prevent loading staff
    }

    if (!CommonUtils.isEmpty(filter.getKeyword())) {
      be = be.and(q.organization.name.containsIgnoreCase(filter.getKeyword()));
    }

    final Page<Staff> page = repos.findAll(be, pageable);
    final List<StaffDto> content = page.getContent().parallelStream().map(d -> new StaffDto(d, false))
            .collect(Collectors.toList());

    return new PageImpl<StaffDto>(content, pageable, page.getTotalElements());
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public StaffDto saveOne(StaffDto dto) {

    if (dto == null) {
      throw new IllegalArgumentException("Staff to save could not be null.");
    }

    if (dto.getOrganization() == null || !CommonUtils.isPositive(dto.getOrganization().getId(), true)) {
      throw new IllegalArgumentException("Staff needs to be attached to an organization.");
    }

    Organization org = null;
    org = orgRepos.findOne(dto.getOrganization().getId());
    if (org == null) {
      throw new IllegalArgumentException("The organization associated is not found.");
    }

    Staff entity = null;
    if (CommonUtils.isPositive(dto.getId(), true)) {
      entity = repos.findOne(dto.getId());
    } else if (repos.findByStaffCode(dto.getStaffCode()) != null) {
      entity = repos.findByStaffCode(dto.getStaffCode());
    }

    if (entity == null) {
      entity = dto.toEntity();
    } else {
      entity.setJobTitle(dto.getJobTitle());
    }

    // Person
    PersonDto personDto = dto.getPerson();
    Person person = null;

    if (entity.getPerson() != null && CommonUtils.isPositive(entity.getPerson().getId(), true)) {
      person = personRepos.findOne(entity.getPerson().getId());
    }

    if (person == null) {
      person = new Person();
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
      if (personDto.getGender() != null) {
        person.setGender(personDto.getGender());
      }
      person.setMaritalStatus(personDto.getMaritalStatus());
      person.setMobilePhone(personDto.getMobilePhone());
      person.setHomePhone(personDto.getHomePhone());
      person.setEmailAddress(personDto.getEmailAddress());
      person.setHeight(personDto.getHeight());
      person.setWeight(personDto.getWeight());
      person.setImage(personDto.getImage());

      if (personDto.getEthnic() != null && CommonUtils.isPositive(personDto.getEthnic().getId(), true)) {
        Dictionary ethnic = dicRepos.findOne(personDto.getEthnic().getId());
        person.setEthnic(ethnic);
      }

      if (personDto.getReligion() != null && CommonUtils.isPositive(personDto.getReligion().getId(), true)) {
        Dictionary religion = dicRepos.findOne(personDto.getReligion().getId());
        person.setReligion(religion);
      }

      if (personDto.getNationality() != null
              && CommonUtils.isPositive(personDto.getNationality().getId(), true)) {
        Dictionary nationality = dicRepos.findOne(personDto.getNationality().getId());
        person.setNationality(nationality);
      }

      if (personDto.getEducation() != null && CommonUtils.isPositive(personDto.getEducation().getId(), true)) {
        Dictionary education = dicRepos.findOne(personDto.getEducation().getId());
        person.setEducation(education);
      }

      if (personDto.getWealthStatus() != null
              && CommonUtils.isPositive(personDto.getWealthStatus().getId(), true)) {
        Dictionary wealthStatus = dicRepos.findOne(personDto.getWealthStatus().getId());
        person.setWealthStatus(wealthStatus);
      }

      if (personDto.getMonthlyIncome() != null
              && CommonUtils.isPositive(personDto.getMonthlyIncome().getId(), true)) {
        Dictionary monthlyIncome = dicRepos.findOne(personDto.getMonthlyIncome().getId());
        person.setMonthlyIncome(monthlyIncome);
      }

      if (personDto.getProfessional() != null
              && CommonUtils.isPositive(personDto.getProfessional().getId(), true)) {
        Dictionary professional = dicRepos.findOne(personDto.getProfessional().getId());
        person.setProfessional(professional);
      }

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

    entity.setPerson(person);

    // Organization
    entity.setOrganization(org);

    // --> save the entity
    entity = repos.save(entity);

    if (entity != null) {
      return new StaffDto(entity, false);
    } else {
      throw new RuntimeException();
    }
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void deleteMultiple(StaffDto[] dtos) {

    if (CommonUtils.isEmpty(dtos)) {
      return;
    }

    for (StaffDto dto : dtos) {
      if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
        continue;
      }

      Staff entity = repos.findOne(dto.getId());

      if (entity != null) {
        repos.delete(entity);
      }
    }
  }

  @Override
  public ImportResultDto<StaffDto> importFromExcel(InputStream is) throws IOException {
    Workbook workbook = new XSSFWorkbook(is);
    Sheet datatypeSheet = workbook.getSheetAt(0);
    // Iterator<Row> iterator = datatypeSheet.iterator();
    int rowIndex = 1;
    int num = datatypeSheet.getLastRowNum();
    ImportResultDto<StaffDto> ret = new ImportResultDto<StaffDto>();
    while (rowIndex <= num) {
      try {
        System.out.println(rowIndex);
        Row currentRow = datatypeSheet.getRow(rowIndex);
        Cell currentCell = null;
        if (currentRow != null) {
          StaffDto dto = new StaffDto();
          String err = "";
          try {
//						Mã số
            currentCell = currentRow.getCell(0);
            if (currentCell != null) {
              String staffCode = "";
              if (currentCell.getCellType() == CellType.STRING) {
                staffCode = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                staffCode = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              if(staffCode.equals("")) {
                break;
              }
              dto.setStaffCode(staffCode);
            }
          } catch (Exception e) {
            dto.setStaffCode(null);
            err += "Mã số - Không rõ; ";
          }
          try {
//						Mã cơ sở báo cáo
            currentCell = currentRow.getCell(1);
            if (currentCell != null) {
              String orgCode = "";
              if (currentCell.getCellType() == CellType.STRING) {
                orgCode = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                orgCode = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              if(!orgCode.equals("")) {
                Organization c1OrgId = orgRepos.findByOrgCode(orgCode);
                if (c1OrgId != null) {
                  dto.setOrganization(new OrganizationDto(c1OrgId));
                } else {
                  dto.setOrganization(null);
                  err += "Không tìm thấy mã cơ sở báo cáo; ";
                }
              }
            }
          } catch (Exception e) {
            dto.setOrganization(null);
            err += "Mã cơ sở báo cáo - Không rõ; ";
          }
          try {
//						Tên người báo cáo/CBO
            currentCell = currentRow.getCell(2);
            if (currentCell != null) {
              String namePerson = "";
              if (currentCell.getCellType() == CellType.STRING) {
                namePerson = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                namePerson = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              if(!namePerson.equals("")) {
                PersonDto person = new PersonDto();
                person.setGender(Gender.MALE);
                person.setFullname(namePerson);
                dto.setPerson(person);
              }
            }
          } catch (Exception e) {
            dto.setPerson(null);
            err += "Tên người báo cáo/CBO - Không rõ; ";
          }
          
//          if (dto.getErrorContent() != null) {
//            dto.setNumberErrorContent(rowIndex + 1);
//            ret.setTotalErr(ret.getTotalErr() + 1);
//            ret.getListErr().add(dto);
//          } else {
//            if(dto.getOrganization() != null && dto.getPerson() != null) {
//              try {
//                saveOne(dto);
//              } catch (Exception e) {
//                dto.setErrorContent(e.getMessage());
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
          if(dto.getOrganization() != null && dto.getPerson() != null) {
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
              err += "Lưu thất bại:"+CommonUtils.getStackTrace(e);
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
        } catch(Exception e){
          ret.setTotalErr(ret.getTotalErr() + 1);
          e.printStackTrace();
          // TODO: handle exception
        }
        rowIndex += 1;
      }

      return ret;
    }
  }
