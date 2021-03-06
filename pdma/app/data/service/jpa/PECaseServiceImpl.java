package org.pepfar.pdma.app.data.service.jpa;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.Join;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pepfar.pdma.app.Constants;
import org.pepfar.pdma.app.data.domain.*;
import org.pepfar.pdma.app.data.dto.*;
import org.pepfar.pdma.app.data.repository.*;
import org.pepfar.pdma.app.data.service.PECaseService;
import org.pepfar.pdma.app.data.service.ReportMerService;
import org.pepfar.pdma.app.data.types.*;
import org.pepfar.pdma.app.data.types.Permission;
import org.pepfar.pdma.app.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service("PECaseServiceImpl")
public class PECaseServiceImpl implements PECaseService {

  @Autowired
  private PECaseRepository peCaseRepository;

  @Autowired
  private PECaseRiskGroupRepository peCaseRiskGroupRepository;

  @Autowired
  private AuthorizationUtils authUtils;

  @Autowired
  private OrganizationRepository organizationRepository;

  @Autowired
  private StaffRepository staffRepository;

  @Autowired
  private AdminUnitRepository adminUnitRepository;

  @Autowired
  public EntityManager manager;

  @Autowired
  private ApplicationContext context;

  @Autowired
  private UserOrganizationRepository userOrganizationRepository;

  @Autowired
  private ReportMerService reportMerService;

  @Override
  public Page<PECaseDto> findAllPageable(PreventionFilterDto searchDto) {
    User currentUser = SecurityUtils.getCurrentUser();
    Boolean isAdministrator = SecurityUtils.isUserInRole(currentUser, "ROLE_ADMIN");
	Boolean isProvince = SecurityUtils.isUserInRole(currentUser, "ROLE_PROVINCIAL_MANAGER");
	Boolean isSiteManagement = SecurityUtils.isUserInRole(currentUser, "ROLE_SITE_MANAGER");
	
	Boolean isDONOR = SecurityUtils.isUserInRole(currentUser, "ROLE_DONOR");
	Boolean isNATIONAL = SecurityUtils.isUserInRole(currentUser, "ROLE_NATIONAL_MANAGER");
	Boolean isDISTRICT = SecurityUtils.isUserInRole(currentUser, "ROLE_DISTRICT_MANAGER");

    Boolean isViewPII = isSiteManagement || isProvince;
    //Check ORG
    final List<Long> orgIds = authUtils.getGrantedOrgIds(Permission.READ_ACCESS);
    final List<Long> writableOrgIds = authUtils.getGrantedOrgIds(Permission.WRITE_ACCESS);
    final List<Long> lstPeOrgIds = authUtils.getGrantedOrgIds(Permission.PE_ACCESS);

    if (isAdministrator || (searchDto != null && ((orgIds != null && orgIds.size() > 0) || (writableOrgIds != null && writableOrgIds.size() > 0) || (lstPeOrgIds != null && lstPeOrgIds.size() > 0)))) {
      //Check ORG
      List<Long> lstOrgIds = new ArrayList<Long>();
      //Check pe

      if (orgIds != null && orgIds.size() > 0) {
        lstOrgIds.addAll(orgIds);
      }
      if (writableOrgIds != null && writableOrgIds.size() > 0) {
        lstOrgIds.addAll(writableOrgIds);
      }

      if(CollectionUtils.isEmpty(lstOrgIds) && !isAdministrator) {
        return null;
      }
      if(CollectionUtils.isEmpty(lstPeOrgIds) && !isAdministrator && !isDONOR && !isNATIONAL && !isProvince && !isDISTRICT) {
        return null;
      }

      String SQL = "";
      if (isViewPII) {
        SQL = " SELECT new org.pepfar.pdma.app.data.dto.PECaseDto(s, true, true, false, false) from PECase s WHERE 1=1 ";
      } else {
        SQL = " SELECT new org.pepfar.pdma.app.data.dto.PECaseDto(s, false, true, false, false) from PECase s WHERE 1=1 ";
      }
//      String countSQL = " SELECT COUNT(s.id) from PECase s WHERE 1=1 ";
      String countSQL = " SELECT count(s.id) from PECase s WHERE 1=1 ";
      String whereClause = " ";
      if(!isAdministrator) {
        whereClause += " AND s.c1Org.id in (:lstOrgIds) ";
        if(isSiteManagement && !isDONOR && !isNATIONAL && !isProvince && !isDISTRICT) {
          whereClause += " AND s.c1Org.id in (:lstPeOrgIds) ";
        }
      }
      String orderByClause = "";
      whereClause += " AND (s.parent is null OR s.c7 != :c7) ";
      if (searchDto.getKeyword() != null && searchDto.getKeyword().length() > 0) {
        whereClause += " AND ("
                + " (s.c2 like :keyword) "
//								+ " OR (s.c23FullName like :keyword) "
//								+ " OR (s.c23IdNumber like :keyword) "
//								+ " OR (s.c23HealthNumber like :keyword) "
//								+ " OR (s.c23PhoneNumber like :keyword) "
                + ")";
      }
      if (searchDto.getFromDate() != null) {
        whereClause += " AND (s.c1 >= :from)";
      }
      if (searchDto.getToDate() != null) {
        whereClause += " AND (s.c1 < :to)";
      }
      if (searchDto.getOrg() != null && searchDto.getOrg().getId() != null) {
        whereClause += " AND (s.c1Org.id = :orgId)";
      }
      if (searchDto.getStaff() != null && searchDto.getStaff().getId() != null) {
        whereClause += " AND (s.c1Staff.id = :staffId)";
      }
      if (searchDto.getType() != null && searchDto.getType() != 9) {

        switch (searchDto.getType()) {
          case 1:
            whereClause += " AND (s.c8 = :types)";
            break;
          case 2:
            whereClause += " AND (s.c10 = :types)";
            break;
          case 3:
            whereClause += " AND (s.c11 = :types)";
            break;
          case 4:
            whereClause += " AND (s.c13 = :types)";
            break;
          case 5:
            whereClause += " AND (s.c131Result = :types)";
            break;
          case 6:
//								whereClause += " AND (s.c8 = :types)";
            break;
          case 7:
            whereClause += " AND (s.c15 = :types)";
            break;
          case 8:
            whereClause += " AND (s.c14 = :types)";
            break;
        }
      }

      if(searchDto.getSortField() != null) {
        if(searchDto.getSortField() == 1) {
          whereClause += " ORDER BY s.c1 ASC ";
        } else if(searchDto.getSortField() == 2) {
          whereClause += " ORDER BY s.c1 DESC ";
        } else if(searchDto.getSortField() == 3) {
          whereClause += " ORDER BY s.c1Staff ASC ";
        } else if(searchDto.getSortField() == 4) {
          whereClause += " ORDER BY s.c1Staff DESC ";
        } else if(searchDto.getSortField() == 5) {
          whereClause += " ORDER BY s.c13 ASC ";
        } else if(searchDto.getSortField() == 6) {
          whereClause += " ORDER BY s.c13 DESC ";
        }
      }

      if(searchDto.getProvinceId() != null) {
        whereClause += " AND (s.c1Org.address.province.id = :provinceId) ";
      }
//      if(!isAdministrator && !isProvince) {
//        whereClause += " AND (u.peRole = TRUE) ";
//      }

//					if (searchDto.getFromDate() != null) {
//						whereClause += " AND (s.c4 >= :from)";
//					}
//					if (searchDto.getToDate() != null) {
//						whereClause += " AND (s.c4 <= :to)";
//					}

//					if(searchDto.getC15()!=null) {
//						whereClause += "AND (s.c15 = :c15)";
//					}
//
//					if(searchDto.getSkipHTS()!=null) {
//						whereClause += "AND (s.id != :skipHTS)";
//					}
//
//					if(searchDto.getC14()!=null) {
//						whereClause += "AND (s.c14 = :c14)";
//					}

      Query q = manager.createQuery(SQL + whereClause + orderByClause, PECaseDto.class);
      Query qCount = manager.createQuery(countSQL + whereClause);
      if(!isAdministrator) {
        q.setParameter("lstOrgIds", lstOrgIds);
        qCount.setParameter("lstOrgIds", lstOrgIds);
        if(isSiteManagement && !isDONOR && !isNATIONAL && !isProvince && !isDISTRICT) {
          q.setParameter("lstPeOrgIds", lstPeOrgIds);
          qCount.setParameter("lstPeOrgIds", lstPeOrgIds);
        }
      }

      q.setParameter("c7", PEApproachMethod.answer5);
      qCount.setParameter("c7", PEApproachMethod.answer5);

      if (searchDto.getKeyword() != null && searchDto.getKeyword().length() > 0) {
        q.setParameter("keyword", "%" + searchDto.getKeyword() + "%");
        qCount.setParameter("keyword", "%" + searchDto.getKeyword() + "%");
      }
      if (searchDto.getOrg() != null && searchDto.getOrg().getId() != null) {
        q.setParameter("orgId", searchDto.getOrg().getId());
        qCount.setParameter("orgId", searchDto.getOrg().getId());
      }
      if (searchDto.getFromDate() != null) {
        q.setParameter("from", searchDto.getFromDate());
        qCount.setParameter("from", searchDto.getFromDate());
      }
      if (searchDto.getToDate() != null) {
        q.setParameter("to", searchDto.getToDate());
        qCount.setParameter("to", searchDto.getToDate());
      }
      if (searchDto.getStaff() != null && searchDto.getStaff().getId() != null) {
        q.setParameter("staffId", searchDto.getStaff().getId());
        qCount.setParameter("staffId", searchDto.getStaff().getId());
      }
      if (searchDto.getType() != null && searchDto.getType() != 0) {
        switch (searchDto.getType()) {
          case 1:
            q.setParameter("types", PEC8.answer1);
            qCount.setParameter("types", PEC8.answer1);
            break;
          case 2:
            q.setParameter("types", HTSYesNoNone.YES);
            qCount.setParameter("types", HTSYesNoNone.YES);
            break;
          case 3:
            q.setParameter("types", HTSYesNoNone.YES);
            qCount.setParameter("types", HTSYesNoNone.YES);
            break;
          case 4:
            q.setParameter("types", PEc13.answer1);
            qCount.setParameter("types", PEc13.answer1);
            break;
          case 5:
            q.setParameter("types", PEC131Result.answer2);
            qCount.setParameter("types", PEC131Result.answer2);
            break;
          case 6:
//								whereClause += " AND (s.c8 = :types)";
            break;
          case 7:
            q.setParameter("types", HTSYesNoNone.YES);
            qCount.setParameter("types", HTSYesNoNone.YES);
            break;
          case 8:
            q.setParameter("types", HTSYesNoNone.YES);
            qCount.setParameter("types", HTSYesNoNone.YES);
            break;
        }
      }
      if(searchDto.getProvinceId() != null) {
        q.setParameter("provinceId", searchDto.getProvinceId());
        qCount.setParameter("provinceId", searchDto.getProvinceId());
      }
//					if ()
//					if (searchDto.getFromDate() != null) {
//						q.setParameter("from",searchDto.getFromDate());
//						qCount.setParameter("from",searchDto.getFromDate());
//					}
//					if (searchDto.getToDate() != null) {
//						q.setParameter("to",searchDto.getToDate());
//						qCount.setParameter("to",searchDto.getToDate());
//					}
//					if( searchDto.getC15()!=null) {
//						q.setParameter("c15",searchDto.getC15());
//						qCount.setParameter("c15",searchDto.getC15());
//					}
//					if( searchDto.getSkipHTS()!=null) {
//						q.setParameter("skipHTS",searchDto.getSkipHTS());
//						qCount.setParameter("skipHTS",searchDto.getSkipHTS());
//					}
//					if( searchDto.getC14()!=null) {
//						q.setParameter("c14",searchDto.getC14());
//						qCount.setParameter("c14",searchDto.getC14());
//					}

      Long count = (long) qCount.getSingleResult();


      if (searchDto.getDisablePaging() == null || searchDto.getDisablePaging() == false) {
        int startPosition = searchDto.getPageIndex() * searchDto.getPageSize();
        q.setFirstResult(startPosition);
        q.setMaxResults(searchDto.getPageSize());
      } else {
        q.setFirstResult(0);
        if (count != null && count > 0) {
          q.setMaxResults(count.intValue());
          searchDto.setPageSize(count.intValue());
        } else {
          q.setMaxResults(10);
          searchDto.setPageSize(10);
        }
      }

      @SuppressWarnings("unchecked")
      List<PECaseDto> entities = q.getResultList();
      Pageable pageable = new PageRequest(searchDto.getPageIndex(), searchDto.getPageSize());
      Page<PECaseDto> result = new PageImpl<PECaseDto>(entities, pageable, count);

      return result;
    }
    return null;
  }

  @Override
  public PECaseDto saveOrUpdate(PECaseDto dto) {
    User currentUser = SecurityUtils.getCurrentUser();
//    Boolean isSiteManagement = SecurityUtils.isUserInRole(currentUser, "ROLE_SITE_MANAGER");
//    if(!isSiteManagement) {
//      isSiteManagement = SecurityUtils.isUserInRole(currentUser, "ROLE_PROVINCIAL_MANAGER");
//    }
    Boolean isSite = SecurityUtils.isUserInRole(currentUser, "ROLE_SITE_MANAGER");
    Boolean isProvince = SecurityUtils.isUserInRole(currentUser, "ROLE_PROVINCIAL_MANAGER");
    Boolean isAdministrator = SecurityUtils.isUserInRole(currentUser, "ROLE_ADMIN");
    List<UserOrganization> listUO = null;
    if (!isAdministrator) {
      listUO = userOrganizationRepository.getListByUserId(currentUser.getId());
    }

    final List<Long> writableOrgIds = authUtils.getGrantedOrgIds(Permission.WRITE_ACCESS);
    if ((writableOrgIds == null || writableOrgIds.size() == 0) && isAdministrator == false) {
      return null;
    }
    if (dto == null) {
      throw new IllegalArgumentException("Cannot save a null instance of SNSCase.");
    }
    if (!DateTimeUtil.checkEditableByMonth(Constants.NUMBER_OF_MONTH_FOR_EDIT,dto.getC1()) && dto.getId()!=null){
      PECaseDto rs = new PECaseDto();
      rs.setEditAble(false);
      return  rs;
    }

    PECase entity = null;

    if (CommonUtils.isPositive(dto.getId(), true)) {
      entity = peCaseRepository.findOne(dto.getId());
    } else if (dto.getUid() != null) {
      entity = peCaseRepository.findByUid(dto.getUid());
    }

    if (entity == null) {
      entity = new PECase();
    }

    // b???t ?????u x??t c??c tr?????ng c???a entity
    if (dto.getUid() != null) {
      entity.setUid(dto.getUid());
    }
    entity.setC1(dto.getC1());
//		entity.setC1Report(dto.getC1Report());
//		entity.setC1ReportYear(dto.getC1ReportYear());

    if (dto.getParent() != null && dto.getParent().getId() != null && dto.getParent().getId() > 0L) {
      PECase parent = peCaseRepository.findOne(dto.getParent().getId());
      entity.setParent(parent);
    }

    Organization c1Org = null;
    if (dto.getC1Org() != null && dto.getC1Org().getId() != null) {
      c1Org = organizationRepository.findOne(dto.getC1Org().getId());
//      if (c1Org != null && !writableOrgIds.contains(c1Org.getId())) {
//        return null;
//      }
      entity.setC1Org(c1Org);
    }

    Staff c1Staff = null;
    if (dto.getC1Staff() != null && dto.getC1Staff().getId() != null) {
      c1Staff = staffRepository.findOne(dto.getC1Staff().getId());
      entity.setC1Staff(c1Staff);
    }

    entity.setC2(dto.getC2());
    entity.setC3(dto.getC3());

    if (dto.getC4() != null && dto.getC4() > 0) {
      LocalDateTime dob = LocalDateTime.of(dto.getC4(), 6, 15, 0, 0);
      entity.setC4(dob);
    }

    if (dto.getC5Province() != null && dto.getC5Province().getId() != null) {
      entity.setC5Province(adminUnitRepository.findOne(dto.getC5Province().getId()));
    } else {
      entity.setC5Province(null);
    }
    if (dto.getC5District() != null && dto.getC5District().getId() != null) {
      entity.setC5District(adminUnitRepository.findOne(dto.getC5District().getId()));
    } else {
      entity.setC5District(null);
    }

    entity.setC5Ward(dto.getC5Ward());
    Set<PECaseRiskGroup> c6 = new LinkedHashSet<>();
    if (dto.getC6() != null && dto.getC6().size() > 0) {
      for (PECaseRiskGroupDto peCaseRiskDto : dto.getC6()) {
        PECaseRiskGroup rg = null;
        if (peCaseRiskDto.getId() != null) {
          rg = peCaseRiskGroupRepository.findOne(peCaseRiskDto.getId());
        }
        if (rg == null) {
          rg = new PECaseRiskGroup();
        }
        rg.setPeCase(entity);
        rg.setVal(peCaseRiskDto.getVal());
        rg.setName(peCaseRiskDto.getVal().getDescription());
        c6.add(rg);
      }
    }
    entity.getC6().clear();
    entity.getC6().addAll(c6);

    if (entity.getC6() != null && entity.getC6().size() > 0) {
      int minRisk = 6;
      PECaseRiskGroup peCaseMinRiskGroup = null;
      for (PECaseRiskGroup peCaseRiskGroup : entity.getC6()) {
        peCaseRiskGroup.setIsMainRisk(false);
        if (peCaseRiskGroup.getVal().getPriority() > 0 && peCaseRiskGroup.getVal().getPriority() <= minRisk) {
          minRisk = peCaseRiskGroup.getVal().getPriority();
          peCaseMinRiskGroup = peCaseRiskGroup;
        }
      }
      if (peCaseMinRiskGroup != null) {
        peCaseMinRiskGroup.setIsMainRisk(true);
      }
    }


    entity.setC7(dto.getC7());
    entity.setC8(dto.getC8());
    entity.setC8ARV(dto.getC8ARV());

    entity.setC9(dto.getC9());
    entity.setC9Date(dto.getC9Date());

    entity.setC10(dto.getC10());

    entity.setC11(dto.getC11());
    entity.setC11Date(dto.getC11Date());

    entity.setC12(dto.getC12());
    entity.setC12Code(dto.getC12Code());
    entity.setC12Note(dto.getC12Note());

    entity.setC13(dto.getC13());
    entity.setC131(dto.getC131());
    entity.setC131Code(dto.getC131Code());
    entity.setC131Result(dto.getC131Result());
    entity.setC132(dto.getC132());

    entity.setC14(dto.getC14());
    entity.setC14Code(dto.getC14Code());
    entity.setC14Date(dto.getC14Date());
    entity.setC14Name(dto.getC14Name());

    entity.setC15(dto.getC15());
    entity.setC15Code(dto.getC15Code());
    entity.setC15Date(dto.getC15Date());
    entity.setC15Name(dto.getC15Name());

    entity.setC16(dto.getC16());

    entity.setC17(dto.getC17());

    entity = peCaseRepository.save(entity);

    if (entity != null) {
      return newPeCaseDto(entity, isSite, isProvince, isAdministrator, listUO);
    } else {
      throw new RuntimeException();
    }

  }

  @Override
  public ResponseDto<PECaseDto> deleteById(Long id) {
    ResponseDto<PECaseDto> ret = new ResponseDto<PECaseDto>();
    final List<Long> writableOrgIds = authUtils.getGrantedOrgIds(Permission.WRITE_ACCESS);
    PECase entity = peCaseRepository.findOne(id);
    if (entity != null) {
      if (entity.getC1Org() != null && !writableOrgIds.contains(entity.getC1Org().getId())) {
        ret.setCode(YesNoNone.NO);
        ret.setMessage("B???n kh??ng c?? quy???n x??a b???n ghi n??y");
        return ret;
      }
      if (entity.getChilds() != null && entity.getChilds().size() > 0) {
        ret.setCode(YesNoNone.NO);
        ret.setMessage("Kh??ch h??ng n??y ???? khai b??o b???n t??nh, b???n ch??ch, kh??ng ???????c ph??p x??a");
        return ret;
      }
      peCaseRepository.delete(entity);
      ret.setCode(YesNoNone.YES);
      ret.setMessage("X??a th??nh c??ng");
      return ret;
    } else {
      ret.setCode(YesNoNone.NO);
      ret.setMessage("Kh??ng t??m th???y b???n ghi t????ng ???ng");
      return ret;
    }
  }

  public Workbook exportReportPECase(PreventionFilterDto filter) {
    Workbook blankBook = new XSSFWorkbook();
    blankBook.createSheet();
    if(filter.getProvinceId()!=null && CollectionUtils.isEmpty(filter.getOrgIds())) {
		filter.setOrgIds(organizationRepository.findAllIdByProvinceId(filter.getProvinceId()));
	}
    PreventionReportDto<PE02DetailReportDto> preventionReportDto = this.getReport(filter);
//		if (htsCaseReportDto == null || htsCaseReportDto.getListDetail().size() == 0) {
//			return blankBook;
//		}
//		else {
//    XSSFWorkbook wb = null;
    XSSFWorkbook wbook = null;
    try (InputStream template = context.getResource("classpath:templates/pe-report.xlsx")
            .getInputStream()) {
//				XSSFWorkbook tmp = new XSSFWorkbook(template);
//				Sheet sheet = tmp.getSheetAt(0);
    	wbook = new XSSFWorkbook(template);
//    	wbook = new SXSSFWorkbook(wb);
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (wbook == null) {
      return blankBook;
    }
    int rowIndex = 7;

    Row row = null;
    Cell cell = null;
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
//    ExcelUtils.setBorders4Style(dateTimeStyle);
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");


    //T??n c?? s??? b??o c??o
    row = sheet.getRow(3);
    cell = row.getCell(3);
    cell.setCellValue(preventionReportDto.getOrgName());

    //T???nh/Th??nh ph???
    row = sheet.getRow(3);
    cell = row.getCell(9);
    cell.setCellValue(preventionReportDto.getProvinceName());

    // K??? b??o c??o
    row = sheet.getRow(4);
    cell = row.getCell(2);
    if (preventionReportDto.getFromDate() != null) {
      cell.setCellValue(formatter.format(Date.from(preventionReportDto.getFromDate().toInstant(ZoneOffset.of("+7")))));
    }
    cell = row.getCell(5);
    if (preventionReportDto.getToDate() != null) {
      cell.setCellValue(formatter.format(Date.from(preventionReportDto.getToDate().toInstant(ZoneOffset.of("+7")))));
    }
    //Qu???n/huy???n
    cell = row.getCell(9);
    cell.setCellValue(preventionReportDto.getDistrictName());

    for (PE02DetailReportDto detail : preventionReportDto.getListDetail()) {
      if (rowIndex == 18) {
        rowIndex += 2;
      }
      if (rowIndex == 8 || rowIndex == 11 || rowIndex == 20 || rowIndex == 25) {
        rowIndex++;
      }

      row = sheet.getRow(rowIndex++);
//				row.setHeightInPoints(22);

      //Ti???p c???n
      if (rowIndex < 26) {
        cell = row.getCell(7);
        if (detail.getApproach() != null) {
          cell.setCellValue(detail.getApproach());
        }
        //				cell.setCellStyle(cellStyle);

        //???? bi???t t??nh tr???ng (+) khi ti???p c???n
        cell = row.getCell(8);
        if (detail.getPositiveHivStatus() != null) {
          cell.setCellValue(detail.getPositiveHivStatus());
        }
//				cell.setCellStyle(cellStyle);
      }


      //?????ng ?? v?? ???? l??m x??t nghi???m
      cell = row.getCell(9);
      if (detail.getAgreeToBeTested() != null) {
        cell.setCellValue(detail.getAgreeToBeTested());
      }
//				cell.setCellStyle(cellStyle);

      //X??t nghi???m c?? ph???n ???ng (+)
      cell = row.getCell(10);
      if (detail.getPositiveHivRapidTesting() != null) {
        cell.setCellValue(detail.getPositiveHivRapidTesting());
      }
//				cell.setCellStyle(cellStyle);

      //X??t nghi???m kh???ng ?????nh (+)
      cell = row.getCell(11);
      if (detail.getPositiveHivConfirmTesting() != null) {
        cell.setCellValue(detail.getPositiveHivConfirmTesting());
      }
//				cell.setCellStyle(cellStyle);

      //Chuy???n g???i ??i???u tr??? ARV th??nh c??ng
      cell = row.getCell(12);
      if (detail.getSentToArvTreatment() != null) {
        cell.setCellValue(detail.getSentToArvTreatment());
      }
//				cell.setCellStyle(cellStyle);

      //Chuy???n g???i ??i???u tr??? PrEP th??nh c??ng
      cell = row.getCell(13);
      if (detail.getSentToPrEP() != null) {
        cell.setCellValue(detail.getSentToPrEP());
      }
//				cell.setCellStyle(cellStyle);
    }
    return wbook;
  }

    public SXSSFWorkbook exportPECase(PreventionFilterDto searchDto) {
        SXSSFWorkbook blankBook = new SXSSFWorkbook();
    blankBook.createSheet();
    searchDto.setDisablePaging(true);
    Page<PECaseDto> peCaseDtos = this.findAllPageable(searchDto);
    if (peCaseDtos == null) {
      return blankBook;
    } else {
        XSSFWorkbook wb = null;
        SXSSFWorkbook wbook = null;
      //Workbook wbook = null;
      try (InputStream template = context.getResource("classpath:templates/pe-list.xlsx")
              .getInputStream()) {
        //wbook = new XSSFWorkbook(template);
          wb = new XSSFWorkbook(template);
          wbook = new SXSSFWorkbook(wb);
      } catch (IOException e) {
        e.printStackTrace();
      }
      if (wbook == null) {
        return blankBook;
      }
      int rowIndex = 2;
      int colIndex = 0;

      Row row = null;
      Cell cell = null;
      Sheet sheet = wbook.getSheetAt(0);

      int seq = 0;
      CellStyle cellStyle = wbook.createCellStyle();
//      ExcelUtils.setBorders4Style(cellStyle);
//      cellStyle.setWrapText(true);
      cellStyle.setAlignment(HorizontalAlignment.LEFT);
      cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
      CellStyle dateTimeStyle = wbook.createCellStyle();
      DataFormat format = wbook.createDataFormat();
//			dateTimeStyle.cloneStyleFrom(templateRow.getCell(0).getCellStyle());
      dateTimeStyle.setDataFormat(format.getFormat("dd/MM/yyyy"));
      dateTimeStyle.setVerticalAlignment(VerticalAlignment.CENTER);
      dateTimeStyle.setAlignment(HorizontalAlignment.LEFT);
//      ExcelUtils.setBorders4Style(dateTimeStyle);

      for (PECaseDto peCaseDto : peCaseDtos) {
        row = sheet.createRow(rowIndex++);

        // M?? s??? NCH
        cell = row.createCell(colIndex++);
        try {
          cell.setCellValue(peCaseDto.getParent().getId());
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        // K??? b??o c??o
        cell = row.createCell(colIndex++);
        cell.setCellStyle(dateTimeStyle);
        try {
          cell.setCellValue(peCaseDto.getC1().getMonthValue() + "/" + peCaseDto.getC1().getYear());
        } catch (Exception e) {
          cell.setCellValue("");
        }

        // T???nh/th??nh ph???
        cell = row.createCell(colIndex++);
        try {
          cell.setCellValue(peCaseDto.getC5Province().getName());
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);
        
        cell = row.createCell(colIndex++);
        try {
          cell.setCellValue(peCaseDto.getC1Org().getAddress().getProvince().getName());
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        // C?? s??? b??o c??o
        cell = row.createCell(colIndex++);
        try {
          cell.setCellValue(peCaseDto.getC1Org().getName());
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        // Ng?????i b??o c??o/CBO
        cell = row.createCell(colIndex++);
        try {
          cell.setCellValue(peCaseDto.getC1Staff().getFullName());
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        // H??? t??n kh??ch h??ng
        cell = row.createCell(colIndex++);
        try {
          cell.setCellValue(peCaseDto.getC2());
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        // Gi???i t??nh
        cell = row.createCell(colIndex++);
        try {
          if (peCaseDto.getC3().name().equals("MALE")) {
            cell.setCellValue("Nam");
          }
          if (peCaseDto.getC3().name().equals("FEMALE")) {
            cell.setCellValue("N???");
          }
          if (peCaseDto.getC3().name().equals("OTHER")) {
            cell.setCellValue("Kh??c");
          }
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        // N??m sinh
        cell = row.createCell(colIndex++);
        try {
          cell.setCellValue(peCaseDto.getC4());
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        // ?????a ch??? n??i c??? tr??
        cell = row.createCell(colIndex++);
        try {
          cell.setCellValue(peCaseDto.getC5Ward());
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        // Qu???n/huy???n
        cell = row.createCell(colIndex++);
        try {
          cell.setCellValue(peCaseDto.getC5District().getName() + " ");
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        // Nh??m nguy c?? c???a kh??ch h??ng
        cell = row.createCell(colIndex++);
        try {
          String c6 = "";
          if (peCaseDto.getC6() != null && peCaseDto.getC6().size() > 0) {
            for (PECaseRiskGroupDto risk : peCaseDto.getC6()) {
              if (c6.length() > 0) {
                c6 += "," + risk.getName();
              } else {
                c6 += risk.getName();
              }

            }
          }
          cell.setCellValue(c6);
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        // C??ch ti???p c???n
        cell = row.createCell(colIndex++);
        try {
          cell.setCellValue(peCaseDto.getC7Des());
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        //T??nh tr???ng HIV
        cell = row.createCell(colIndex++);
        try {
          cell.setCellValue(peCaseDto.getC8Des());
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        // T??nh tr???ng ??i???u tr??? ARV
        cell = row.createCell(colIndex++);
        try {
          cell.setCellValue(peCaseDto.getC8ARVDes());
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        // T?? v???n TBXNBT/BC
        cell = row.createCell(colIndex++);
        try {
          if (peCaseDto.getC9().name().equals("YES")) {
            cell.setCellValue("C??");
          }
          if (peCaseDto.getC9().name().equals("NO")) {
            cell.setCellValue("Kh??ng");
          }
          if (peCaseDto.getC9().name().equals("NO_INFORMATION")) {
            cell.setCellValue("Kh??ng c?? th??ng tin");
          }
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        // Ng??y t?? v???n
        cell = row.createCell(colIndex++);
        cell.setCellStyle(dateTimeStyle);
        try {
          cell.setCellValue(Date.from(peCaseDto.getC9Date().toInstant(ZoneOffset.of("+7"))));
        } catch (Exception e) {
          cell.setCellValue("");
        }

        // Cung c???p t??n BT/BC
        cell = row.createCell(colIndex++);
        try {
          if (peCaseDto.getC10().name().equals("YES")) {
            cell.setCellValue("C??");
          }
          if (peCaseDto.getC10().name().equals("NO")) {
            cell.setCellValue("Kh??ng");
          }
          if (peCaseDto.getC10().name().equals("NO_INFORMATION")) {
            cell.setCellValue("Kh??ng c?? th??ng tin");
          }
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        // ?????ng ?? XN HIV
        cell = row.createCell(colIndex++);
        try {
          if (peCaseDto.getC11().name().equals("YES")) {
            cell.setCellValue("C??");
          }
          if (peCaseDto.getC11().name().equals("NO")) {
            cell.setCellValue("Kh??ng");
          }
          if (peCaseDto.getC11().name().equals("NO_INFORMATION")) {
            cell.setCellValue("Kh??ng c?? th??ng tin");
          }
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        // Ng??y x??t nghi???m HIV
        cell = row.createCell(colIndex++);
        cell.setCellStyle(dateTimeStyle);
        try {
          cell.setCellValue(Date.from(peCaseDto.getC11Date().toInstant(ZoneOffset.of("+7"))));
        } catch (Exception e) {
          cell.setCellValue("");
        }

        // Lo???i h??nh x??t nghi???m
        cell = row.createCell(colIndex++);
        try {
          cell.setCellValue(peCaseDto.getC12Des());
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        // M?? x??t nghi???m
        cell = row.createCell(colIndex++);
        try {
          cell.setCellValue(peCaseDto.getC12Code());
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        // T??? x??t nghi???m cho
        cell = row.createCell(colIndex++);
        try {
          cell.setCellValue(peCaseDto.getC12NoteDes());
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        // K???t qu??? XN HIV l???n n??y
        cell = row.createCell(colIndex++);
        try {
          cell.setCellValue(peCaseDto.getC13Des());
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        // Chuy???n XN kh???ng ?????nh
        cell = row.createCell(colIndex++);
        try {
//          System.out.println(peCaseDto.getC13());
          if (peCaseDto.getC131().name().equals("YES")) {
            cell.setCellValue("C??");
          }
          if (peCaseDto.getC131().name().equals("NO")) {
            cell.setCellValue("Kh??ng");
          }
          if (peCaseDto.getC131().name().equals("NO_INFORMATION")) {
            cell.setCellValue("Kh??ng c?? th??ng tin");
          }
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        // M?? s??? XN kh???ng ?????nh
        cell = row.createCell(colIndex++);
        try {
          cell.setCellValue(peCaseDto.getC131Code());
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        // K???t qu??? XN kh???ng ?????nh
        cell = row.createCell(colIndex++);
        try {
          cell.setCellValue(peCaseDto.getC131ResultDes());
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        // ??i???u tr??? PrEP
        cell = row.createCell(colIndex++);
        try {
          if (peCaseDto.getC14().name().equals("YES")) {
            cell.setCellValue("C??");
          }
          if (peCaseDto.getC14().name().equals("NO")) {
            cell.setCellValue("Kh??ng");
          }
          if (peCaseDto.getC14().name().equals("NO_INFORMATION")) {
            cell.setCellValue("Kh??ng c?? th??ng tin");
          }
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        // Ng??y ??i???u tr??? PrEP
        cell = row.createCell(colIndex++);
        cell.setCellStyle(dateTimeStyle);
        try {
          cell.setCellValue(Date.from(peCaseDto.getC14Date().toInstant(ZoneOffset.of("+7"))));
        } catch (Exception e) {
          cell.setCellValue("");
        }

        // M?? kh??ch h??ng
        cell = row.createCell(colIndex++);
        try {
          cell.setCellValue(peCaseDto.getC14Code());
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        // T??n c?? s??? ??i???u tr???
        cell = row.createCell(colIndex++);
        try {
          cell.setCellValue(peCaseDto.getC14Name());
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        // ??i???u tr??? ARV
        cell = row.createCell(colIndex++);
        try {
          if (peCaseDto.getC15().name().equals("YES")) {
            cell.setCellValue("C??");
          }
          if (peCaseDto.getC15().name().equals("NO")) {
            cell.setCellValue("Kh??ng");
          }
          if (peCaseDto.getC15().name().equals("NO_INFORMATION")) {
            cell.setCellValue("Kh??ng c?? th??ng tin");
          }
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        // Ng??y ??i???u tr??? ARV
        cell = row.createCell(colIndex++);
        cell.setCellStyle(dateTimeStyle);
        try {
          cell.setCellValue(Date.from(peCaseDto.getC15Date().toInstant(ZoneOffset.of("+7"))));
        } catch (Exception e) {
          cell.setCellValue("");
        }

        // M?? kh??ch h??ng
        cell = row.createCell(colIndex++);
        try {
          cell.setCellValue(peCaseDto.getC15Code());
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        // T??n c?? s??? ??i???u tr???
        cell = row.createCell(colIndex++);
        try {
          cell.setCellValue(peCaseDto.getC15Name());
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        // K???t qu??? x??c minh
        cell = row.createCell(colIndex++);
        try {
          cell.setCellValue(peCaseDto.getC16Des());
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        // KQXN SL Giang mai
        cell = row.createCell(colIndex++);
        try {
          cell.setCellValue(peCaseDto.getC132Des());
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        // Ghi ch??
        cell = row.createCell(colIndex++);
        try {
          cell.setCellValue(peCaseDto.getC17());
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        colIndex = 0;
      }
      return wbook;
    }
  }

  @Override
  public PECaseDto findById(long id) {
    User currentUser = SecurityUtils.getCurrentUser();
    Boolean isSite = SecurityUtils.isUserInRole(currentUser, "ROLE_SITE_MANAGER");
    Boolean isProvince = SecurityUtils.isUserInRole(currentUser, "ROLE_PROVINCIAL_MANAGER");
    Boolean isAdministrator = SecurityUtils.isUserInRole(currentUser, "ROLE_ADMIN");
    final List<Long> readableOrgIds = authUtils.getGrantedOrgIds(Permission.READ_ACCESS);
    final List<Long> writableOrgIds = authUtils.getGrantedOrgIds(Permission.WRITE_ACCESS);
    List<Long> orgIds = new ArrayList<Long>();
    orgIds.addAll(readableOrgIds);
    orgIds.addAll(writableOrgIds);

    if (orgIds.size() == 0 && !isAdministrator) {
      return null;
    }

    if (!CommonUtils.isPositive(id, true)) {
      return null;
    }
    PECase entity = peCaseRepository.findOne(id);
    List<UserOrganization> listUO = null;
    if (!isAdministrator) {
      listUO = userOrganizationRepository.getListByUserId(currentUser.getId());
    }
    if (isAdministrator || (entity != null && entity.getC1Org() != null && orgIds.contains(entity.getC1Org().getId()))) {
      return newPeCaseDto(entity, isSite, isProvince, isAdministrator, listUO);
    } else {
      return null;
    }
  }

  private PECaseDto newPeCaseDto(PECase entity, boolean isSite, boolean isProvince,
                                   boolean isAdministrator, List<UserOrganization> listUO) {
    if (entity != null) {
      // Ch??? Quy???n c?? s??? v?? t???nh c?? quy???n xem chi ti???t
      boolean isViewPII = isSite || isProvince;
      // N???u l?? admin th?? m???c ?????nh c?? quy???n s???a - x??a - xem, n???u kh??ng th?? c??ng s??? =
      // false v?? x??t ??? b?????c k??? ti???p
      boolean isReadAble = isAdministrator;
      boolean isWritAble = isAdministrator;
      boolean isDeleteAble = isAdministrator;
      if (!isAdministrator) {
        if (listUO != null && listUO.size() > 0) {
          for (UserOrganization userOrganization : listUO) {
            if (userOrganization.getOrganization() != null && entity.getC1Org() != null
                && userOrganization.getOrganization().getId().equals(entity.getC1Org().getId())
                && userOrganization.getPeRole() != null && userOrganization.getPeRole()) {
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
      return new PECaseDto(entity, isViewPII, isReadAble, isWritAble, isDeleteAble);
    } else {
      return null;
    }
  }

	@Override
	public PreventionReportDto<PE02DetailReportDto> getReport(PreventionFilterDto filter) {
		if (filter != null && filter.getFromDate() != null && filter.getToDate() != null
				&& filter.getFromDate().isBefore(filter.getToDate()) && filter.getOrgIds() != null
				&& filter.getOrgIds().size() > 0) {

			User currentUser = SecurityUtils.getCurrentUser();
			Boolean isAdministrator = SecurityUtils.isUserInRole(currentUser, "ROLE_ADMIN");

			PreventionReportDto<PE02DetailReportDto> ret = new PreventionReportDto<PE02DetailReportDto>();
			ret.setFromDate(filter.getFromDate());
			ret.setToDate(filter.getToDate());
			String orgName = "";
			String provinceName = "";
			String districtName = "";
			final List<Long> readableOrgIds = authUtils.getGrantedOrgIds(Permission.READ_ACCESS);
			final List<Long> writableOrgIds = authUtils.getGrantedOrgIds(Permission.WRITE_ACCESS);
			Set<Long> orgIds = new HashSet<Long>();
			orgIds.addAll(readableOrgIds);
			orgIds.addAll(writableOrgIds);

			//N???u l?? admin th?? kh??ng c???n x??t quy???n n??y
			if (!isAdministrator && orgIds.size() == 0) {
				return null;
			}
			if(orgIds.size()>0) {
				for (int i = 0; i < filter.getOrgIds().size(); i++) {
					if (!orgIds.contains(filter.getOrgIds().get(i))) {
						filter.getOrgIds().remove(i);
						if (i > 0) {
							i--;
						}
					}
				}
			}
			if (filter.getOrgIds().size() == 0) {
				return null;
			} else {
				for (Long id : filter.getOrgIds()) {
					Organization org = organizationRepository.findOne(id);
					if (org != null) {
						orgName += org.getName() + ";";
						provinceName += org.getAddress().getProvince().getName() + ";";
						districtName += org.getAddress().getDistrict().getName() + ";";
					}
				}
			}
			ret.setOrgName(orgName);
			if (orgName.split(";").length <= 1) {
				ret.setProvinceName(provinceName);
				ret.setDistrictName(districtName);
			}

//			I	T???ng s???
			PE02DetailReportDto detail = this.getReportDetail(filter, "I", "T???ng s???", 0);
			ret.getListDetail().add(detail);
//			II	Theo gi???i t??nh
//			1		Nam
			detail = this.getReportDetail(filter, "II.1", "Nam", 1);
			ret.getListDetail().add(detail);
//			2		N???
			detail = this.getReportDetail(filter, "II.2", "N???", 2);
			ret.getListDetail().add(detail);
//			III	Theo nh??m nguy c??
//			1		Ng?????i nghi???n ch??ch ma t??y
			detail = this.getReportDetail(filter, "III.1", "Ng?????i nghi???n ch??ch ma t??y", 3);
			ret.getListDetail().add(detail);
//			2		Nam c?? quan h??? t??nh d???c ?????ng gi???i
			detail = this.getReportDetail(filter, "III.2", "Nam c?? quan h??? t??nh d???c ?????ng gi???i", 4);
			ret.getListDetail().add(detail);
//			3		Ng?????i b??n d??m
			detail = this.getReportDetail(filter, "III.3", "Ng?????i b??n d??m", 5);
			ret.getListDetail().add(detail);
//			4		Ng?????i chuy???n gi???i
			detail = this.getReportDetail(filter, "III.4", "Ng?????i chuy???n gi???i", 6);
			ret.getListDetail().add(detail);
//			5		V???/ch???ng/b???n t??nh/con ????? ??? 15 tu???i c???a ng?????i c?? HIV
			detail = this.getReportDetail(filter, "III.5", "V???/ch???ng/b???n t??nh/con ????? ??? 15 tu???i c???a ng?????i c?? HIV", 7);
			ret.getListDetail().add(detail);
//			6		B???n ch??ch chung c???a ng?????i c?? HIV
			detail = this.getReportDetail(filter, "III.6", "B???n ch??ch chung c???a ng?????i c?? HIV", 8);
			ret.getListDetail().add(detail);
//			IV	Theo c??ch th???c ti???p c???n
//			1		Tr???c ti???p
			detail = this.getReportDetail(filter, "IV.1", "Tr???c ti???p", 9);
			ret.getListDetail().add(detail);
//			2		M???ng x?? h???i (Online)
			detail = this.getReportDetail(filter, "IV.2", "M???ng x?? h???i (Online)", 10);
			ret.getListDetail().add(detail);
//			3		M???ng l?????i PDI
			detail = this.getReportDetail(filter, "IV.3", "M???ng l?????i PDI", 11);
			ret.getListDetail().add(detail);
//			4		Ng?????i c?? HIV
			detail = this.getReportDetail(filter, "IV.4", "Ng?????i c?? HIV", 12);
			ret.getListDetail().add(detail);
//			V	Theo h??nh th???c x??t nghi???m
//			1		Nh??n vi??n c???ng ?????ng th???c hi???n
			detail = this.getReportDetail(filter, "V.1", "Nh??n vi??n c???ng ?????ng th???c hi???n", 13);
			ret.getListDetail().add(detail);
//			2		T??? x??t nghi???m HIV c?? h??? tr???
			detail = this.getReportDetail(filter, "V.2", "T??? x??t nghi???m HIV c?? h??? tr???", 14);
			ret.getListDetail().add(detail);
//			3		T??? x??t nghi???m HIV kh??ng c?? h??? tr???
			detail = this.getReportDetail(filter, "V.3", "T??? x??t nghi???m HIV kh??ng c?? h??? tr???", 15);
			ret.getListDetail().add(detail);
//			4		C?? s??? y t??? th???c hi???n
			detail = this.getReportDetail(filter, "V.4", "T??? x??t nghi???m HIV kh??ng c?? h??? tr???", 15);
			ret.getListDetail().add(detail);

			return ret;
		}
		return null;
	}

  public PE02DetailReportDto getReportDetail(PreventionFilterDto filter, String rowCode, String rowName, int orderNumber) {
    PE02DetailReportDto detail = new PE02DetailReportDto();
    detail.setSeq(rowCode);
    detail.setTitle(rowName);
    detail.setOrderNumber(orderNumber);
//			Ti???p c???n
    Integer approach = 0;
    approach = this.queryReport(filter, rowCode, 1).intValue();
    detail.setApproach(approach);
//			???? bi???t t??nh tr???ng (+) khi ti???p c???n
    Integer positiveHivStatus = 0;
    positiveHivStatus = this.queryReport(filter, rowCode, 2).intValue();
    detail.setPositiveHivStatus(positiveHivStatus);

//			?????ng ?? v?? ???? l??m x??t nghi???m
    Integer agreeToBeTested = 0;
    agreeToBeTested = this.queryReport(filter, rowCode, 3).intValue();
    detail.setAgreeToBeTested(agreeToBeTested);

//			X??t nghi???m c?? ph???n ???ng (+)
    Integer positiveHivRapidTesting = 0;
    positiveHivRapidTesting = this.queryReport(filter, rowCode, 4).intValue();
    detail.setPositiveHivRapidTesting(positiveHivRapidTesting);

//			X??t nghi???m kh???ng ?????nh (+)
    Integer positiveHivConfirmTesting = 0;
    positiveHivConfirmTesting = this.queryReport(filter, rowCode, 5).intValue();
    detail.setPositiveHivConfirmTesting(positiveHivConfirmTesting);

//			Chuy???n g???i ??i???u tr??? ARV th??nh c??ng
    Integer sentToArvTreatment = 0;
    sentToArvTreatment = this.queryReport(filter, rowCode, 6).intValue();
    detail.setSentToArvTreatment(sentToArvTreatment);

//			Chuy???n g???i ??i???u tr??? PrEP th??nh c??ng
    Integer sentToPrEP = 0;
    sentToPrEP = this.queryReport(filter, rowCode, 7).intValue();
    detail.setSentToPrEP(sentToPrEP);

    return detail;
  }

  public Integer queryReport(PreventionFilterDto filter, String row, int col) {
    String SQL = " SELECT COUNT(s.id) from PECase s WHERE s.c1Org.id in (:listOrg) ";
    String whereClause = "";
//		Ti???p c???n
    if (col == 1) {
      whereClause += " AND s.c1 >=:fromDate AND s.c1<=:toDate ";
//			I	T???ng s???
      if (row .equals("I")) {
        whereClause += " AND s.c7 != :c7answer5 ";
      }
//			II	Theo gi???i t??nh
//			1 Nam
      if (row .equals("II.1")) {
        whereClause += " AND s.c7 != :c7answer5 AND c3=:c3MALE ";
      }
//			2 N???
      if (row .equals("II.2")) {
        whereClause += " AND s.c7 != :c7answer5 AND c3=:c3FEMALE ";
      }
//			III	Theo nh??m nguy c??
//			1		Ng?????i nghi???n ch??ch ma t??y
      if (row .equals("III.1")) {
        whereClause += " AND s.c7 != :c7answer5 AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer1 AND r.isMainRisk=true ) ";
      }
//			2		Nam c?? quan h??? t??nh d???c ?????ng gi???i
      if (row .equals("III.2")) {
        whereClause += " AND s.c7 != :c7answer5 AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer2 AND r.isMainRisk=true ) ";
      }
//			3		Ng?????i b??n d??m
      if (row .equals("III.3")) {
        whereClause += " AND s.c7 != :c7answer5 AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer3 AND r.isMainRisk=true ) ";
      }
//			4		Ng?????i chuy???n gi???i
      if (row .equals("III.4")) {
        whereClause += " AND s.c7 != :c7answer5 AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer4 AND r.isMainRisk=true ) ";
      }
//			5		V???/ch???ng/b???n t??nh/con ????? ??? 15 tu???i c???a ng?????i c?? HIV
      if (row .equals("III.5")) {
        whereClause += " AND s.c7 != :c7answer5 AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer5 AND r.isMainRisk=true ) ";
      }
//			6		B???n ch??ch chung c???a ng?????i c?? HIV
      if (row .equals("III.6")) {
        whereClause += " AND s.c7 != :c7answer5 AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer6 AND r.isMainRisk=true ) ";
      }
//			IV	Theo c??ch th???c ti???p c???n
//			1		Tr???c ti???p
      if (row .equals("IV.1")) {
        whereClause += " AND s.c7 = :c7answer1 ";
      }
//			2		M???ng x?? h???i (Online)
      if (row .equals("IV.2")) {
        whereClause += " AND s.c7 = :c7answer2 ";
      }
//			3		M???ng l?????i PDI
      if (row .equals("IV.3")) {
        whereClause += " AND s.c7 = :c7answer3 ";
      }
//			4		Ng?????i c?? HIV
      if (row .equals("IV.4")) {
        whereClause += " AND s.c7 = :c7answer4 ";
      }
    }
//		???? bi???t t??nh tr???ng (+) khi ti???p c???n
    if (col == 2) {
      whereClause += " AND s.c1 >=:fromDate AND s.c1<=:toDate AND s.c8 = :c8answer1 ";
//			I	T???ng s???
      if (row .equals("I")) {
        whereClause += "";
      }
//			II	Theo gi???i t??nh
//			1 Nam
      if (row .equals("II.1")) {
        whereClause += " AND c3=:c3MALE ";
      }
//			2 N???
      if (row .equals("II.2")) {
        whereClause += " AND c3=:c3FEMALE ";
      }
//			III	Theo nh??m nguy c??
//			1		Ng?????i nghi???n ch??ch ma t??y
      if (row .equals("III.1")) {
        whereClause += " AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer1 AND r.isMainRisk=true ) ";
      }
//			2		Nam c?? quan h??? t??nh d???c ?????ng gi???i
      if (row .equals("III.2")) {
        whereClause += " AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer2 AND r.isMainRisk=true ) ";
      }
//			3		Ng?????i b??n d??m
      if (row .equals("III.3")) {
        whereClause += " AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer3 AND r.isMainRisk=true ) ";
      }
//			4		Ng?????i chuy???n gi???i
      if (row .equals("III.4")) {
        whereClause += " AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer4 AND r.isMainRisk=true ) ";
      }
//			5		V???/ch???ng/b???n t??nh/con ????? ??? 15 tu???i c???a ng?????i c?? HIV
      if (row .equals("III.5")) {
        whereClause += " AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer5 AND r.isMainRisk=true ) ";
      }
//			6		B???n ch??ch chung c???a ng?????i c?? HIV
      if (row .equals("III.6")) {
        whereClause += " AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer6 AND r.isMainRisk=true ) ";
      }
//			IV	Theo c??ch th???c ti???p c???n
//			1		Tr???c ti???p
      if (row .equals("IV.1")) {
        whereClause += " AND s.c7 = :c7answer1 ";
      }
//			2		M???ng x?? h???i (Online)
      if (row .equals("IV.2")) {
        whereClause += " AND s.c7 = :c7answer2 ";
      }
//			3		M???ng l?????i PDI
      if (row .equals("IV.3")) {
        whereClause += " AND s.c7 = :c7answer3 ";
      }
//			4		Ng?????i c?? HIV
      if (row .equals("IV.4")) {
        whereClause += " AND s.c7 = :c7answer4 ";
      }
    }
//		?????ng ?? v?? ???? l??m x??t nghi???m
    if (col == 3) {
      whereClause += " AND s.c11Date is not null AND s.c11Date >=:fromDate AND s.c11Date<=:toDate ";
//			I	T???ng s???
      if (row .equals("I")) {
        whereClause += " ";
      }
//			II	Theo gi???i t??nh
//			1 Nam
      if (row .equals("II.1")) {
        whereClause += " AND c3=:c3MALE ";
      }
//			2 N???
      if (row .equals("II.2")) {
        whereClause += " AND c3=:c3FEMALE ";
      }
//			III	Theo nh??m nguy c??
//			1		Ng?????i nghi???n ch??ch ma t??y
      if (row .equals("III.1")) {
        whereClause += " AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer1 AND r.isMainRisk=true ) ";
      }
//			2		Nam c?? quan h??? t??nh d???c ?????ng gi???i
      if (row .equals("III.2")) {
        whereClause += " AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer2 AND r.isMainRisk=true ) ";
      }
//			3		Ng?????i b??n d??m
      if (row .equals("III.3")) {
        whereClause += " AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer3 AND r.isMainRisk=true ) ";
      }
//			4		Ng?????i chuy???n gi???i
      if (row .equals("III.4")) {
        whereClause += " AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer4 AND r.isMainRisk=true ) ";
      }
//			5		V???/ch???ng/b???n t??nh/con ????? ??? 15 tu???i c???a ng?????i c?? HIV
      if (row .equals("III.5")) {
        whereClause += " AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer5 AND r.isMainRisk=true ) ";
      }
//			6		B???n ch??ch chung c???a ng?????i c?? HIV
      if (row .equals("III.6")) {
        whereClause += " AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer6 AND r.isMainRisk=true ) ";
      }
//			IV	Theo c??ch th???c ti???p c???n
//			1		Tr???c ti???p
      if (row .equals("IV.1")) {
        whereClause += " AND s.c7 = :c7answer1 ";
      }
//			2		M???ng x?? h???i (Online)
      if (row .equals("IV.2")) {
        whereClause += " AND s.c7 = :c7answer2 ";
      }
//			3		M???ng l?????i PDI
      if (row .equals("IV.3")) {
        whereClause += " AND s.c7 = :c7answer3 ";
      }
//			4		Ng?????i c?? HIV
      if (row .equals("IV.4")) {
        whereClause += " AND s.c7 = :c7answer4 ";
      }
//			V	Theo h??nh th???c x??t nghi???m
//			1		Nh??n vi??n c???ng ?????ng th???c hi???n
      if (row .equals("V.1")) {
        whereClause += " AND s.c12=:c12answer1 ";
      }
//			2		T??? x??t nghi???m HIV c?? h??? tr???
      if (row .equals("V.2")) {
        whereClause += " AND s.c12=:c12answer2 ";
      }
//			3		T??? x??t nghi???m HIV kh??ng c?? h??? tr???
      if (row .equals("V.3")) {
        whereClause += " AND s.c12=:c12answer3 ";
      }
//			4		C?? s??? y t??? th???c hi???n
      if (row .equals("V.4")) {
        whereClause += " AND s.c12=:c12answer4 ";
      }
    }
//		X??t nghi???m c?? ph???n ???ng (+)
    if (col == 4) {
      whereClause += " AND s.c11Date is not null AND s.c11Date >=:fromDate AND s.c11Date<=:toDate AND s.c13=:c13answer1 ";
//			I	T???ng s???
      if (row .equals("I")) {
        whereClause += " ";
      }
//			II	Theo gi???i t??nh
//			1 Nam
      if (row .equals("II.1")) {
        whereClause += " AND c3=:c3MALE ";
      }
//			2 N???
      if (row .equals("II.2")) {
        whereClause += " AND c3=:c3FEMALE ";
      }
//			III	Theo nh??m nguy c??
//			1		Ng?????i nghi???n ch??ch ma t??y
      if (row .equals("III.1")) {
        whereClause += " AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer1 AND r.isMainRisk=true ) ";
      }
//			2		Nam c?? quan h??? t??nh d???c ?????ng gi???i
      if (row .equals("III.2")) {
        whereClause += " AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer2 AND r.isMainRisk=true ) ";
      }
//			3		Ng?????i b??n d??m
      if (row .equals("III.3")) {
        whereClause += " AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer3 AND r.isMainRisk=true ) ";
      }
//			4		Ng?????i chuy???n gi???i
      if (row .equals("III.4")) {
        whereClause += " AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer4 AND r.isMainRisk=true ) ";
      }
//			5		V???/ch???ng/b???n t??nh/con ????? ??? 15 tu???i c???a ng?????i c?? HIV
      if (row .equals("III.5")) {
        whereClause += " AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer5 AND r.isMainRisk=true ) ";
      }
//			6		B???n ch??ch chung c???a ng?????i c?? HIV
      if (row .equals("III.6")) {
        whereClause += " AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer6 AND r.isMainRisk=true ) ";
      }
//			IV	Theo c??ch th???c ti???p c???n
//			1		Tr???c ti???p
      if (row .equals("IV.1")) {
        whereClause += " AND s.c7 = :c7answer1 ";
      }
//			2		M???ng x?? h???i (Online)
      if (row .equals("IV.2")) {
        whereClause += " AND s.c7 = :c7answer2 ";
      }
//			3		M???ng l?????i PDI
      if (row .equals("IV.3")) {
        whereClause += " AND s.c7 = :c7answer3 ";
      }
//			4		Ng?????i c?? HIV
      if (row .equals("IV.4")) {
        whereClause += " AND s.c7 = :c7answer4 ";
      }
//			V	Theo h??nh th???c x??t nghi???m
//			1		Nh??n vi??n c???ng ?????ng th???c hi???n
      if (row .equals("V.1")) {
        whereClause += " AND s.c12=:c12answer1 ";
      }
//			2		T??? x??t nghi???m HIV c?? h??? tr???
      if (row .equals("V.2")) {
        whereClause += " AND s.c12=:c12answer2 ";
      }
//			3		T??? x??t nghi???m HIV kh??ng c?? h??? tr???
      if (row .equals("V.3")) {
        whereClause += " AND s.c12=:c12answer3 ";
      }
//			4		C?? s??? y t??? th???c hi???n
      if (row .equals("V.4")) {
        whereClause += " AND s.c12=:c12answer4 ";
      }
    }
//		X??t nghi???m kh???ng ?????nh (+)
    if (col == 5) {
      whereClause += " AND s.c11Date is not null AND s.c11Date >=:fromDate AND s.c11Date<=:toDate AND s.c131Result is not null AND s.c131Result=:c131ResultAnswer2 ";
//			I	T???ng s???
      if (row .equals("I")) {
        whereClause += " ";
      }
//			II	Theo gi???i t??nh
//			1 Nam
      if (row .equals("II.1")) {
        whereClause += " AND c3=:c3MALE ";
      }
//			2 N???
      if (row .equals("II.2")) {
        whereClause += " AND c3=:c3FEMALE ";
      }
//			III	Theo nh??m nguy c??
//			1		Ng?????i nghi???n ch??ch ma t??y
      if (row .equals("III.1")) {
        whereClause += " AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer1 AND r.isMainRisk=true ) ";
      }
//			2		Nam c?? quan h??? t??nh d???c ?????ng gi???i
      if (row .equals("III.2")) {
        whereClause += " AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer2 AND r.isMainRisk=true ) ";
      }
//			3		Ng?????i b??n d??m
      if (row .equals("III.3")) {
        whereClause += " AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer3 AND r.isMainRisk=true ) ";
      }
//			4		Ng?????i chuy???n gi???i
      if (row .equals("III.4")) {
        whereClause += " AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer4 AND r.isMainRisk=true ) ";
      }
//			5		V???/ch???ng/b???n t??nh/con ????? ??? 15 tu???i c???a ng?????i c?? HIV
      if (row .equals("III.5")) {
        whereClause += " AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer5 AND r.isMainRisk=true ) ";
      }
//			6		B???n ch??ch chung c???a ng?????i c?? HIV
      if (row .equals("III.6")) {
        whereClause += " AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer6 AND r.isMainRisk=true ) ";
      }
//			IV	Theo c??ch th???c ti???p c???n
//			1		Tr???c ti???p
      if (row .equals("IV.1")) {
        whereClause += " AND s.c7 = :c7answer1 ";
      }
//			2		M???ng x?? h???i (Online)
      if (row .equals("IV.2")) {
        whereClause += " AND s.c7 = :c7answer2 ";
      }
//			3		M???ng l?????i PDI
      if (row .equals("IV.3")) {
        whereClause += " AND s.c7 = :c7answer3 ";
      }
//			4		Ng?????i c?? HIV
      if (row .equals("IV.4")) {
        whereClause += " AND s.c7 = :c7answer4 ";
      }
//			V	Theo h??nh th???c x??t nghi???m
//			1		Nh??n vi??n c???ng ?????ng th???c hi???n
      if (row .equals("V.1")) {
        whereClause += " AND s.c12=:c12answer1 ";
      }
//			2		T??? x??t nghi???m HIV c?? h??? tr???
      if (row .equals("V.2")) {
        whereClause += " AND s.c12=:c12answer2 ";
      }
//			3		T??? x??t nghi???m HIV kh??ng c?? h??? tr???
      if (row .equals("V.3")) {
        whereClause += " AND s.c12=:c12answer3 ";
      }
//			4		C?? s??? y t??? th???c hi???n
      if (row .equals("V.4")) {
        whereClause += " AND s.c12=:c12answer4 ";
      }
    }
//		Chuy???n g???i ??i???u tr??? ARV th??nh c??ng
    if (col == 6) {
      whereClause += " AND s.c15Date is not null AND s.c15Date >=:fromDate AND s.c15Date<=:toDate";
//			I	T???ng s???
      if (row .equals("I")) {
        whereClause += " ";
      }
//			II	Theo gi???i t??nh
//			1 Nam
      if (row .equals("II.1")) {
        whereClause += " AND c3=:c3MALE ";
      }
//			2 N???
      if (row .equals("II.2")) {
        whereClause += " AND c3=:c3FEMALE ";
      }
//			III	Theo nh??m nguy c??
//			1		Ng?????i nghi???n ch??ch ma t??y
      if (row .equals("III.1")) {
        whereClause += " AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer1 AND r.isMainRisk=true ) ";
      }
//			2		Nam c?? quan h??? t??nh d???c ?????ng gi???i
      if (row .equals("III.2")) {
        whereClause += " AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer2 AND r.isMainRisk=true ) ";
      }
//			3		Ng?????i b??n d??m
      if (row .equals("III.3")) {
        whereClause += " AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer3 AND r.isMainRisk=true ) ";
      }
//			4		Ng?????i chuy???n gi???i
      if (row .equals("III.4")) {
        whereClause += " AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer4 AND r.isMainRisk=true ) ";
      }
//			5		V???/ch???ng/b???n t??nh/con ????? ??? 15 tu???i c???a ng?????i c?? HIV
      if (row .equals("III.5")) {
        whereClause += " AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer5 AND r.isMainRisk=true ) ";
      }
//			6		B???n ch??ch chung c???a ng?????i c?? HIV
      if (row .equals("III.6")) {
        whereClause += " AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer6 AND r.isMainRisk=true ) ";
      }
//			IV	Theo c??ch th???c ti???p c???n
//			1		Tr???c ti???p
      if (row .equals("IV.1")) {
        whereClause += " AND s.c7 = :c7answer1 ";
      }
//			2		M???ng x?? h???i (Online)
      if (row .equals("IV.2")) {
        whereClause += " AND s.c7 = :c7answer2 ";
      }
//			3		M???ng l?????i PDI
      if (row .equals("IV.3")) {
        whereClause += " AND s.c7 = :c7answer3 ";
      }
//			4		Ng?????i c?? HIV
      if (row .equals("IV.4")) {
        whereClause += " AND s.c7 = :c7answer4 ";
      }
//			V	Theo h??nh th???c x??t nghi???m
//			1		Nh??n vi??n c???ng ?????ng th???c hi???n
      if (row .equals("V.1")) {
        whereClause += " AND s.c12=:c12answer1 ";
      }
//			2		T??? x??t nghi???m HIV c?? h??? tr???
      if (row .equals("V.2")) {
        whereClause += " AND s.c12=:c12answer2 ";
      }
//			3		T??? x??t nghi???m HIV kh??ng c?? h??? tr???
      if (row .equals("V.3")) {
        whereClause += " AND s.c12=:c12answer3 ";
      }
//			4		C?? s??? y t??? th???c hi???n
      if (row .equals("V.4")) {
        whereClause += " AND s.c12=:c12answer4 ";
      }
    }
//		Chuy???n g???i ??i???u tr??? PrEP th??nh c??ng
    if (col == 7) {
      whereClause += " AND s.c14Date is not null AND s.c14Date >=:fromDate AND s.c14Date<=:toDate";
//			I	T???ng s???
      if (row .equals("I")) {
        whereClause += " ";
      }
//			II	Theo gi???i t??nh
//			1 Nam
      if (row .equals("II.1")) {
        whereClause += " AND c3=:c3MALE ";
      }
//			2 N???
      if (row .equals("II.2")) {
        whereClause += " AND c3=:c3FEMALE ";
      }
//			III	Theo nh??m nguy c??
//			1		Ng?????i nghi???n ch??ch ma t??y
      if (row .equals("III.1")) {
        whereClause += " AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer1 AND r.isMainRisk=true ) ";
      }
//			2		Nam c?? quan h??? t??nh d???c ?????ng gi???i
      if (row .equals("III.2")) {
        whereClause += " AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer2 AND r.isMainRisk=true ) ";
      }
//			3		Ng?????i b??n d??m
      if (row .equals("III.3")) {
        whereClause += " AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer3 AND r.isMainRisk=true ) ";
      }
//			4		Ng?????i chuy???n gi???i
      if (row .equals("III.4")) {
        whereClause += " AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer4 AND r.isMainRisk=true ) ";
      }
//			5		V???/ch???ng/b???n t??nh/con ????? ??? 15 tu???i c???a ng?????i c?? HIV
      if (row .equals("III.5")) {
        whereClause += " AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer5 AND r.isMainRisk=true ) ";
      }
//			6		B???n ch??ch chung c???a ng?????i c?? HIV
      if (row .equals("III.6")) {
        whereClause += " AND s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer6 AND r.isMainRisk=true ) ";
      }
//			IV	Theo c??ch th???c ti???p c???n
//			1		Tr???c ti???p
      if (row .equals("IV.1")) {
        whereClause += " AND s.c7 = :c7answer1 ";
      }
//			2		M???ng x?? h???i (Online)
      if (row .equals("IV.2")) {
        whereClause += " AND s.c7 = :c7answer2 ";
      }
//			3		M???ng l?????i PDI
      if (row .equals("IV.3")) {
        whereClause += " AND s.c7 = :c7answer3 ";
      }
//			4		Ng?????i c?? HIV
      if (row .equals("IV.4")) {
        whereClause += " AND s.c7 = :c7answer4 ";
      }
//			V	Theo h??nh th???c x??t nghi???m
//			1		Nh??n vi??n c???ng ?????ng th???c hi???n
      if (row .equals("V.1")) {
        whereClause += " AND s.c12=:c12answer1 ";
      }
//			2		T??? x??t nghi???m HIV c?? h??? tr???
      if (row .equals("V.2")) {
        whereClause += " AND s.c12=:c12answer2 ";
      }
//			3		T??? x??t nghi???m HIV kh??ng c?? h??? tr???
      if (row .equals("V.3")) {
        whereClause += " AND s.c12=:c12answer3 ";
      }
//			4		C?? s??? y t??? th???c hi???n
      if (row .equals("V.4")) {
        whereClause += " AND s.c12=:c12answer4 ";
      }
    }
    Query q = manager.createQuery(SQL + whereClause);
    q.setParameter("listOrg", filter.getOrgIds());

//		Ti???p c???n
    if (col == 1) {
      q.setParameter("fromDate", filter.getFromDate());
      q.setParameter("toDate", filter.getToDate());
//			I	T???ng s???
      if (row .equals("I")) {
        q.setParameter("c7answer5", PEApproachMethod.answer5);
      }
//			II	Theo gi???i t??nh
//			1 Nam
      if (row .equals("II.1")) {
        q.setParameter("c3MALE", Gender.MALE);
        q.setParameter("c7answer5", PEApproachMethod.answer5);
      }
//			2 N???
      if (row .equals("II.2")) {
        q.setParameter("c3FEMALE", Gender.FEMALE);
        q.setParameter("c7answer5", PEApproachMethod.answer5);
      }
//			III	Theo nh??m nguy c??
//			1		Ng?????i nghi???n ch??ch ma t??y
      if (row .equals("III.1")) {
        q.setParameter("c6answer1", PERiskGroupEnum.answer1);
        q.setParameter("c7answer5", PEApproachMethod.answer5);
      }
//			2		Nam c?? quan h??? t??nh d???c ?????ng gi???i
      if (row .equals("III.2")) {
        q.setParameter("c6answer2", PERiskGroupEnum.answer2);
        q.setParameter("c7answer5", PEApproachMethod.answer5);
      }
//			3		Ng?????i b??n d??m
      if (row .equals("III.3")) {
        q.setParameter("c6answer3", PERiskGroupEnum.answer3);
        q.setParameter("c7answer5", PEApproachMethod.answer5);
      }
//			4		Ng?????i chuy???n gi???i
      if (row .equals("III.4")) {
        q.setParameter("c6answer4", PERiskGroupEnum.answer4);
        q.setParameter("c7answer5", PEApproachMethod.answer5);
      }
//			5		V???/ch???ng/b???n t??nh/con ????? ??? 15 tu???i c???a ng?????i c?? HIV
      if (row .equals("III.5")) {
        q.setParameter("c6answer5", PERiskGroupEnum.answer5);
        q.setParameter("c7answer5", PEApproachMethod.answer5);
      }
//			6		B???n ch??ch chung c???a ng?????i c?? HIV
      if (row .equals("III.6")) {
        q.setParameter("c6answer6", PERiskGroupEnum.answer6);
        q.setParameter("c7answer5", PEApproachMethod.answer5);
      }
//			IV	Theo c??ch th???c ti???p c???n
//			1		Tr???c ti???p
      if (row .equals("IV.1")) {
        q.setParameter("c7answer1", PEApproachMethod.answer1);
      }
//			2		M???ng x?? h???i (Online)
      if (row .equals("IV.2")) {
        q.setParameter("c7answer2", PEApproachMethod.answer2);
      }
//			3		M???ng l?????i PDI
      if (row .equals("IV.3")) {
        q.setParameter("c7answer3", PEApproachMethod.answer3);
      }
//			4		Ng?????i c?? HIV
      if (row .equals("IV.4")) {
        q.setParameter("c7answer4", PEApproachMethod.answer4);
      }
    }
//		???? bi???t t??nh tr???ng (+) khi ti???p c???n
    if (col == 2) {
      q.setParameter("fromDate", filter.getFromDate());
      q.setParameter("toDate", filter.getToDate());
      q.setParameter("c8answer1", PEC8.answer1);
//			I	T???ng s???
      if (row .equals("I")) {
      }
//			II	Theo gi???i t??nh
//			1 Nam
      if (row .equals("II.1")) {
        q.setParameter("c3MALE", Gender.MALE);
      }
//			2 N???
      if (row .equals("II.2")) {
        q.setParameter("c3FEMALE", Gender.FEMALE);
      }
//			III	Theo nh??m nguy c??
//			1		Ng?????i nghi???n ch??ch ma t??y
      if (row .equals("III.1")) {
        q.setParameter("c6answer1", PERiskGroupEnum.answer1);
      }
//			2		Nam c?? quan h??? t??nh d???c ?????ng gi???i
      if (row .equals("III.2")) {
        q.setParameter("c6answer2", PERiskGroupEnum.answer2);
      }
//			3		Ng?????i b??n d??m
      if (row .equals("III.3")) {
        q.setParameter("c6answer3", PERiskGroupEnum.answer3);
      }
//			4		Ng?????i chuy???n gi???i
      if (row .equals("III.4")) {
        q.setParameter("c6answer4", PERiskGroupEnum.answer4);
      }
//			5		V???/ch???ng/b???n t??nh/con ????? ??? 15 tu???i c???a ng?????i c?? HIV
      if (row .equals("III.5")) {
        q.setParameter("c6answer5", PERiskGroupEnum.answer5);
      }
//			6		B???n ch??ch chung c???a ng?????i c?? HIV
      if (row .equals("III.6")) {
        q.setParameter("c6answer6", PERiskGroupEnum.answer6);
      }
//			IV	Theo c??ch th???c ti???p c???n
//			1		Tr???c ti???p
      if (row .equals("IV.1")) {
        q.setParameter("c7answer1", PEApproachMethod.answer1);
      }
//			2		M???ng x?? h???i (Online)
      if (row .equals("IV.2")) {
        q.setParameter("c7answer2", PEApproachMethod.answer2);
      }
//			3		M???ng l?????i PDI
      if (row .equals("IV.3")) {
        q.setParameter("c7answer3", PEApproachMethod.answer3);
      }
//			4		Ng?????i c?? HIV
      if (row .equals("IV.4")) {
        q.setParameter("c7answer4", PEApproachMethod.answer4);
      }
    }
//		?????ng ?? v?? ???? l??m x??t nghi???m
    if (col == 3) {
//			whereClause+=" AND s.c11Date is not null AND s.c11Date >=:fromDate AND s.c11Date<=:toDate ";
      q.setParameter("fromDate", filter.getFromDate());
      q.setParameter("toDate", filter.getToDate());
//			I	T???ng s???
      if (row .equals("I")) {
//				whereClause+=" ";
      }
//			II	Theo gi???i t??nh
//			1 Nam
      if (row .equals("II.1")) {
        q.setParameter("c3MALE", Gender.MALE);
      }
//			2 N???
      if (row .equals("II.2")) {
        q.setParameter("c3FEMALE", Gender.FEMALE);
      }
//			III	Theo nh??m nguy c??
//			1		Ng?????i nghi???n ch??ch ma t??y
      if (row .equals("III.1")) {
        q.setParameter("c6answer1", PERiskGroupEnum.answer1);
      }
//			2		Nam c?? quan h??? t??nh d???c ?????ng gi???i
      if (row .equals("III.2")) {
        q.setParameter("c6answer2", PERiskGroupEnum.answer2);
      }
//			3		Ng?????i b??n d??m
      if (row .equals("III.3")) {
        q.setParameter("c6answer3", PERiskGroupEnum.answer3);
      }
//			4		Ng?????i chuy???n gi???i
      if (row .equals("III.4")) {
        q.setParameter("c6answer4", PERiskGroupEnum.answer4);
      }
//			5		V???/ch???ng/b???n t??nh/con ????? ??? 15 tu???i c???a ng?????i c?? HIV
      if (row .equals("III.5")) {
        q.setParameter("c6answer5", PERiskGroupEnum.answer5);
      }
//			6		B???n ch??ch chung c???a ng?????i c?? HIV
      if (row .equals("III.6")) {
        q.setParameter("c6answer6", PERiskGroupEnum.answer6);
      }
//			IV	Theo c??ch th???c ti???p c???n
//			1		Tr???c ti???p
      if (row .equals("IV.1")) {
        q.setParameter("c7answer1", PEApproachMethod.answer1);
      }
//			2		M???ng x?? h???i (Online)
      if (row .equals("IV.2")) {
        q.setParameter("c7answer2", PEApproachMethod.answer2);
      }
//			3		M???ng l?????i PDI
      if (row .equals("IV.3")) {
        q.setParameter("c7answer3", PEApproachMethod.answer3);
      }
//			4		Ng?????i c?? HIV
      if (row .equals("IV.4")) {
        q.setParameter("c7answer4", PEApproachMethod.answer4);
      }
//			V	Theo h??nh th???c x??t nghi???m
//			1		Nh??n vi??n c???ng ?????ng th???c hi???n
      if (row .equals("V.1")) {
        q.setParameter("c12answer1", PEC12.answer1);
      }
//			2		T??? x??t nghi???m HIV c?? h??? tr???
      if (row .equals("V.2")) {
        q.setParameter("c12answer2", PEC12.answer2);
      }
//			3		T??? x??t nghi???m HIV kh??ng c?? h??? tr???
      if (row .equals("V.3")) {
        q.setParameter("c12answer3", PEC12.answer3);
      }
//			4		C?? s??? y t??? th???c hi???n
      if (row .equals("V.4")) {
        q.setParameter("c12answer4", PEC12.answer4);
      }
    }
//		X??t nghi???m c?? ph???n ???ng (+)
    if (col == 4) {
      whereClause += " AND s.c11Date is not null AND s.c11Date >=:fromDate AND s.c11Date<=:toDate AND s.c13=:c13answer1 ";
      q.setParameter("fromDate", filter.getFromDate());
      q.setParameter("toDate", filter.getToDate());
      q.setParameter("c13answer1", PEc13.answer1);
//			I	T???ng s???
      if (row .equals("I")) {
        whereClause += " ";
      }
//			II	Theo gi???i t??nh
//			1 Nam
      if (row .equals("II.1")) {
        q.setParameter("c3MALE", Gender.MALE);
      }
//			2 N???
      if (row .equals("II.2")) {
        q.setParameter("c3FEMALE", Gender.FEMALE);
      }
//			III	Theo nh??m nguy c??
//			1		Ng?????i nghi???n ch??ch ma t??y
      if (row .equals("III.1")) {
        q.setParameter("c6answer1", PERiskGroupEnum.answer1);
      }
//			2		Nam c?? quan h??? t??nh d???c ?????ng gi???i
      if (row .equals("III.2")) {
        q.setParameter("c6answer2", PERiskGroupEnum.answer2);
      }
//			3		Ng?????i b??n d??m
      if (row .equals("III.3")) {
        q.setParameter("c6answer3", PERiskGroupEnum.answer3);
      }
//			4		Ng?????i chuy???n gi???i
      if (row .equals("III.4")) {
        q.setParameter("c6answer4", PERiskGroupEnum.answer4);
      }
//			5		V???/ch???ng/b???n t??nh/con ????? ??? 15 tu???i c???a ng?????i c?? HIV
      if (row .equals("III.5")) {
        q.setParameter("c6answer5", PERiskGroupEnum.answer5);
      }
//			6		B???n ch??ch chung c???a ng?????i c?? HIV
      if (row .equals("III.6")) {
        q.setParameter("c6answer6", PERiskGroupEnum.answer6);
      }
//			IV	Theo c??ch th???c ti???p c???n
//			1		Tr???c ti???p
      if (row .equals("IV.1")) {
        q.setParameter("c7answer1", PEApproachMethod.answer1);
      }
//			2		M???ng x?? h???i (Online)
      if (row .equals("IV.2")) {
        q.setParameter("c7answer2", PEApproachMethod.answer2);
      }
//			3		M???ng l?????i PDI
      if (row .equals("IV.3")) {
        q.setParameter("c7answer3", PEApproachMethod.answer3);
      }
//			4		Ng?????i c?? HIV
      if (row .equals("IV.4")) {
        q.setParameter("c7answer4", PEApproachMethod.answer4);
      }
//			V	Theo h??nh th???c x??t nghi???m
//			1		Nh??n vi??n c???ng ?????ng th???c hi???n
      if (row .equals("V.1")) {
        q.setParameter("c12answer1", PEC12.answer1);
      }
//			2		T??? x??t nghi???m HIV c?? h??? tr???
      if (row .equals("V.2")) {
        q.setParameter("c12answer2", PEC12.answer2);
      }
//			3		T??? x??t nghi???m HIV kh??ng c?? h??? tr???
      if (row .equals("V.3")) {
        q.setParameter("c12answer3", PEC12.answer3);
      }
//			4		C?? s??? y t??? th???c hi???n
      if (row .equals("V.4")) {
        q.setParameter("c12answer4", PEC12.answer4);
      }
    }
//		X??t nghi???m kh???ng ?????nh (+)
    if (col == 5) {
      q.setParameter("fromDate", filter.getFromDate());
      q.setParameter("toDate", filter.getToDate());
      q.setParameter("c131ResultAnswer2", PEC131Result.answer2);
//			I	T???ng s???
      if (row .equals("I")) {
        whereClause += " ";
      }
//			II	Theo gi???i t??nh
//			1 Nam
      if (row .equals("II.1")) {
        q.setParameter("c3MALE", Gender.MALE);
      }
//			2 N???
      if (row .equals("II.2")) {
        q.setParameter("c3FEMALE", Gender.FEMALE);
      }
//			III	Theo nh??m nguy c??
//			1		Ng?????i nghi???n ch??ch ma t??y
      if (row .equals("III.1")) {
        q.setParameter("c6answer1", PERiskGroupEnum.answer1);
      }
//			2		Nam c?? quan h??? t??nh d???c ?????ng gi???i
      if (row .equals("III.2")) {
        q.setParameter("c6answer2", PERiskGroupEnum.answer2);
      }
//			3		Ng?????i b??n d??m
      if (row .equals("III.3")) {
        q.setParameter("c6answer3", PERiskGroupEnum.answer3);
      }
//			4		Ng?????i chuy???n gi???i
      if (row .equals("III.4")) {
        q.setParameter("c6answer4", PERiskGroupEnum.answer4);
      }
//			5		V???/ch???ng/b???n t??nh/con ????? ??? 15 tu???i c???a ng?????i c?? HIV
      if (row .equals("III.5")) {
        q.setParameter("c6answer5", PERiskGroupEnum.answer5);
      }
//			6		B???n ch??ch chung c???a ng?????i c?? HIV
      if (row .equals("III.6")) {
        q.setParameter("c6answer6", PERiskGroupEnum.answer6);
      }
//			IV	Theo c??ch th???c ti???p c???n
//			1		Tr???c ti???p
      if (row .equals("IV.1")) {
        q.setParameter("c7answer1", PEApproachMethod.answer1);
      }
//			2		M???ng x?? h???i (Online)
      if (row .equals("IV.2")) {
        q.setParameter("c7answer2", PEApproachMethod.answer2);
      }
//			3		M???ng l?????i PDI
      if (row .equals("IV.3")) {
        q.setParameter("c7answer3", PEApproachMethod.answer3);
      }
//			4		Ng?????i c?? HIV
      if (row .equals("IV.4")) {
        q.setParameter("c7answer4", PEApproachMethod.answer4);
      }
//			V	Theo h??nh th???c x??t nghi???m
//			1		Nh??n vi??n c???ng ?????ng th???c hi???n
      if (row .equals("V.1")) {
        q.setParameter("c12answer1", PEC12.answer1);
      }
//			2		T??? x??t nghi???m HIV c?? h??? tr???
      if (row .equals("V.2")) {
        q.setParameter("c12answer2", PEC12.answer2);
      }
//			3		T??? x??t nghi???m HIV kh??ng c?? h??? tr???
      if (row .equals("V.3")) {
        q.setParameter("c12answer3", PEC12.answer3);
      }
//			4		C?? s??? y t??? th???c hi???n
      if (row .equals("V.4")) {
        q.setParameter("c12answer4", PEC12.answer4);
      }
    }
//		Chuy???n g???i ??i???u tr??? ARV th??nh c??ng
    if (col == 6) {
      q.setParameter("fromDate", filter.getFromDate());
      q.setParameter("toDate", filter.getToDate());
//			I	T???ng s???
      if (row .equals("I")) {
        whereClause += " ";
      }
//			II	Theo gi???i t??nh
//			1 Nam
      if (row .equals("II.1")) {
        q.setParameter("c3MALE", Gender.MALE);
      }
//			2 N???
      if (row .equals("II.2")) {
        q.setParameter("c3FEMALE", Gender.FEMALE);
      }
//			III	Theo nh??m nguy c??
//			1		Ng?????i nghi???n ch??ch ma t??y
      if (row .equals("III.1")) {
        q.setParameter("c6answer1", PERiskGroupEnum.answer1);
      }
//			2		Nam c?? quan h??? t??nh d???c ?????ng gi???i
      if (row .equals("III.2")) {
        q.setParameter("c6answer2", PERiskGroupEnum.answer2);
      }
//			3		Ng?????i b??n d??m
      if (row .equals("III.3")) {
        q.setParameter("c6answer3", PERiskGroupEnum.answer3);
      }
//			4		Ng?????i chuy???n gi???i
      if (row .equals("III.4")) {
        q.setParameter("c6answer4", PERiskGroupEnum.answer4);
      }
//			5		V???/ch???ng/b???n t??nh/con ????? ??? 15 tu???i c???a ng?????i c?? HIV
      if (row .equals("III.5")) {
        q.setParameter("c6answer5", PERiskGroupEnum.answer5);
      }
//			6		B???n ch??ch chung c???a ng?????i c?? HIV
      if (row .equals("III.6")) {
        q.setParameter("c6answer6", PERiskGroupEnum.answer6);
      }
//			IV	Theo c??ch th???c ti???p c???n
//			1		Tr???c ti???p
      if (row .equals("IV.1")) {
        q.setParameter("c7answer1", PEApproachMethod.answer1);
      }
//			2		M???ng x?? h???i (Online)
      if (row .equals("IV.2")) {
        q.setParameter("c7answer2", PEApproachMethod.answer2);
      }
//			3		M???ng l?????i PDI
      if (row .equals("IV.3")) {
        q.setParameter("c7answer3", PEApproachMethod.answer3);
      }
//			4		Ng?????i c?? HIV
      if (row .equals("IV.4")) {
        q.setParameter("c7answer4", PEApproachMethod.answer4);
      }
//			V	Theo h??nh th???c x??t nghi???m
//			1		Nh??n vi??n c???ng ?????ng th???c hi???n
      if (row .equals("V.1")) {
        q.setParameter("c12answer1", PEC12.answer1);
      }
//			2		T??? x??t nghi???m HIV c?? h??? tr???
      if (row .equals("V.2")) {
        q.setParameter("c12answer2", PEC12.answer2);
      }
//			3		T??? x??t nghi???m HIV kh??ng c?? h??? tr???
      if (row .equals("V.3")) {
        q.setParameter("c12answer3", PEC12.answer3);
      }
//			4		C?? s??? y t??? th???c hi???n
      if (row .equals("V.4")) {
        q.setParameter("c12answer4", PEC12.answer4);
      }
    }
//		Chuy???n g???i ??i???u tr??? PrEP th??nh c??ng
    if (col == 7) {
      q.setParameter("fromDate", filter.getFromDate());
      q.setParameter("toDate", filter.getToDate());
//			I	T???ng s???
      if (row .equals("I")) {
        whereClause += " ";
      }
//			II	Theo gi???i t??nh
//			1 Nam
      if (row .equals("II.1")) {
        q.setParameter("c3MALE", Gender.MALE);
      }
//			2 N???
      if (row .equals("II.2")) {
        q.setParameter("c3FEMALE", Gender.FEMALE);
      }
//			III	Theo nh??m nguy c??
//			1		Ng?????i nghi???n ch??ch ma t??y
      if (row .equals("III.1")) {
        q.setParameter("c6answer1", PERiskGroupEnum.answer1);
      }
//			2		Nam c?? quan h??? t??nh d???c ?????ng gi???i
      if (row .equals("III.2")) {
        q.setParameter("c6answer2", PERiskGroupEnum.answer2);
      }
//			3		Ng?????i b??n d??m
      if (row .equals("III.3")) {
        q.setParameter("c6answer3", PERiskGroupEnum.answer3);
      }
//			4		Ng?????i chuy???n gi???i
      if (row .equals("III.4")) {
        q.setParameter("c6answer4", PERiskGroupEnum.answer4);
      }
//			5		V???/ch???ng/b???n t??nh/con ????? ??? 15 tu???i c???a ng?????i c?? HIV
      if (row .equals("III.5")) {
        q.setParameter("c6answer5", PERiskGroupEnum.answer5);
      }
//			6		B???n ch??ch chung c???a ng?????i c?? HIV
      if (row .equals("III.6")) {
        q.setParameter("c6answer6", PERiskGroupEnum.answer6);
      }
//			IV	Theo c??ch th???c ti???p c???n
//			1		Tr???c ti???p
      if (row .equals("IV.1")) {
        q.setParameter("c7answer1", PEApproachMethod.answer1);
      }
//			2		M???ng x?? h???i (Online)
      if (row .equals("IV.2")) {
        q.setParameter("c7answer2", PEApproachMethod.answer2);
      }
//			3		M???ng l?????i PDI
      if (row .equals("IV.3")) {
        q.setParameter("c7answer3", PEApproachMethod.answer3);
      }
//			4		Ng?????i c?? HIV
      if (row .equals("IV.4")) {
        q.setParameter("c7answer4", PEApproachMethod.answer4);
      }
//			V	Theo h??nh th???c x??t nghi???m
//			1		Nh??n vi??n c???ng ?????ng th???c hi???n
      if (row .equals("V.1")) {
        q.setParameter("c12answer1", PEC12.answer1);
      }
//			2		T??? x??t nghi???m HIV c?? h??? tr???
      if (row .equals("V.2")) {
        q.setParameter("c12answer2", PEC12.answer2);
      }
//			3		T??? x??t nghi???m HIV kh??ng c?? h??? tr???
      if (row .equals("V.3")) {
        q.setParameter("c12answer3", PEC12.answer3);
      }
//			4		C?? s??? y t??? th???c hi???n
      if (row .equals("V.4")) {
        q.setParameter("c12answer4", PEC12.answer4);
      }
    }
    Long ret = (Long) q.getSingleResult();
    if (ret != null) {
      return ret.intValue();
    }
    return 0;
  }

  @Override
  public ImportResultDto<PECaseDto> importFromExcel(InputStream is) throws IOException {
    Workbook workbook = new XSSFWorkbook(is);
    Sheet datatypeSheet = workbook.getSheetAt(0);
    // Iterator<Row> iterator = datatypeSheet.iterator();
    int rowIndex = 1;
    int rowIndexParent = 1;
    int num = datatypeSheet.getLastRowNum();
    ImportResultDto<PECaseDto> ret = new ImportResultDto<PECaseDto>();
    while (rowIndex <= num) {
      try {
        System.out.println(rowIndex);
        Row currentRow = datatypeSheet.getRow(rowIndex);
        Cell currentCell = null;
        String err = "";
        if (currentRow != null) {
          PECaseDto dto = new PECaseDto();
          String c1Str = null;
          try {
//						c1
            currentCell = currentRow.getCell(0);
            if (currentCell != null) {
              if (currentCell.getCellType() == CellType.STRING) {
                c1Str = currentCell.getStringCellValue();
                UUID uid = UUID.fromString(currentCell.getStringCellValue());
                dto.setUid(uid);
              }
            }
            if (!StringUtils.hasText(c1Str)) {
              rowIndex += 1;
              continue;
            }
          } catch (Exception e) {
            dto.setUid(null);
            err += "C1 - Kh??ng r??; ";
          }
//          try {
//            // 						c2 - parent_id
//            currentCell = currentRow.getCell(1);
//            if (currentCell != null) {
//              String c2ParentId = "";
//              if (currentCell.getCellType() == CellType.STRING) {
//                String parentId = currentCell.getStringCellValue();
//                if(StringUtils.hasText(parentId)) {
//                  rowIndex += 1;
//                  continue;
//                }
//              }
//            }
//          } catch (Exception e) {
//            e.printStackTrace();
//          }
          try {
            //						c3 - c1_report_period
            currentCell = currentRow.getCell(2);
            if (currentCell != null) {
              LocalDateTime c1ReportPeriod = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-yyyy");
                c1ReportPeriod = LocalDateTime.parse(currentCell.getStringCellValue(), formatter);
              } else if (currentCell.getCellType() == CellType.NUMERIC
                      && currentCell.getDateCellValue() != null) {
                c1ReportPeriod = currentCell.getDateCellValue().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime();
              }
              dto.setC1(c1ReportPeriod);
            }
          } catch (Exception e) {
            dto.setC3(null);
            err += "C3 - Kh??ng r?? k??? b??o c??o; ";
          }
          //						c4 - c1_org_id
          try {
            currentCell = currentRow.getCell(3);
            if (currentCell != null) {
              String orgCode = "";
              if (currentCell.getCellType() == CellType.STRING) {
                orgCode = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                orgCode = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              Organization c1OrgId = new Organization();
              c1OrgId = organizationRepository.findByOrgCode(orgCode);
              if (!orgCode.equals("")) {
                if (c1OrgId != null) {
                  dto.setC1Org(new OrganizationDto(c1OrgId));
                } else {
                  dto.setC1Org(null);
                  err += "C4 - Kh??ng t??m th???y c?? s??? b??o c??o; ";
                }
              }
            }
          } catch (Exception e) {
            dto.setC1Org(null);
            err += "C4 - Kh??ng r??; ";
          }
          //						c5 - c1_staff_id
          try {
            currentCell = currentRow.getCell(4);
            if (currentCell != null) {
              String staffCode = "";
              if (currentCell.getCellType() == CellType.STRING) {
                staffCode = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                staffCode = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              if (!staffCode.equals("")) {
                Staff staff = staffRepository.findByStaffCode(staffCode);
                if (staff != null) {
                  dto.setC1Staff(new StaffDto(staff, true));
                } else {
                  dto.setC1Staff(null);
                  err += "C5 - Kh??ng t??m th???y nh??n vi??n; ";
                }
              }
            }
          } catch (Exception e) {
            dto.setC1Staff(null);
            err += "C5 - Kh??ng r??; ";
          }
          try {
            //						c6 - c2
            currentCell = currentRow.getCell(5);
            if (currentCell != null) {
              String c2 = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c2 = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c2 = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              dto.setC2(c2);
            }
          } catch (Exception e) {
            dto.setC2(null);
            err += "C6 - Kh??ng r??; ";
          }

          try {
            //						c7 - c3
            currentCell = currentRow.getCell(6);
            if (currentCell != null) {
              String c3 = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c3 = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c3 = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              if (c3 != null) {
                if (c3.equals("6")) {
                  dto.setC3(Gender.MALE);
                } else if (c3.equals("7")) {
                  dto.setC3(Gender.FEMALE);
                } else {
                  dto.setC3(null);
                  err += "C7 kh??ng ????ng (C7 ph???i b???ng 6 ho???c 7); ";
                }
              } else {
                dto.setC3(null);
              }
            }
          } catch (Exception e) {
            dto.setC3(null);
            err += "C7 - Kh??ng r??; ";
          }
          try {
            //						c8 - c4_dob
            currentCell = currentRow.getCell(7);
            if (currentCell != null) {
              Integer c4Dob = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c4Dob = Integer.valueOf(currentCell.getStringCellValue());
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c4Dob = Double.valueOf(currentCell.getNumericCellValue()).intValue();
              }
              dto.setC4(c4Dob);
            }
          } catch (Exception e) {
            dto.setC4(null);
            err += "C8 - Kh??ng r??; ";
          }
          try {
            //						c9 - c5_ward
            currentCell = currentRow.getCell(8);
            if (currentCell != null) {
              String c5Ward = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c5Ward = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c5Ward = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              dto.setC5Ward(c5Ward);
            }
          } catch (Exception e) {
            dto.setC5Ward(null);
            err += "C9 - Kh??ng r??; ";
          }
          try {
            //						c10 - c5_province_id
            currentCell = currentRow.getCell(9);
            if (currentCell != null) {
              Long c5ProvinceId = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c5ProvinceId = Long.valueOf(currentCell.getStringCellValue());
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c5ProvinceId = Long.valueOf((long) currentCell.getNumericCellValue()).longValue();
              }
              AdminUnit entity = null;
              AdminUnitDto adminUnitDto = null;
              String c5ProvinceIdConvert = String.valueOf(c5ProvinceId);
              if (c5ProvinceIdConvert.length() == 1) {
                c5ProvinceIdConvert = "0" + c5ProvinceIdConvert;
              }
              if (c5ProvinceId != null) {
                entity = adminUnitRepository.findByProvinceOrDistrict(c5ProvinceIdConvert);
                if (entity != null) {
                  adminUnitDto = new AdminUnitDto(entity);
                } else {
                  dto.setC5Province(null);
                  err += "C10 - Kh??ng t??m th???y; ";
                }
              }
              dto.setC5Province(adminUnitDto);
            }
          } catch (Exception e) {
            dto.setC5Province(null);
            err += "C10 - Kh??ng r??; ";
          }
          try {
            //						c11 - c5_district_id
            currentCell = currentRow.getCell(10);
            if (currentCell != null) {
              Long c5DistrictId = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c5DistrictId = Long.valueOf(currentCell.getStringCellValue());
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c5DistrictId = Long.valueOf((long) currentCell.getNumericCellValue()).longValue();
              }

              AdminUnit entity = null;
              AdminUnitDto adminUnitDto = null;
              String c5DistrictIdConvert = String.valueOf(c5DistrictId);
              if (c5DistrictIdConvert.length() == 1) {
                c5DistrictIdConvert = "00" + c5DistrictIdConvert;
              } else if (c5DistrictIdConvert.length() == 2) {
                c5DistrictIdConvert = "0" + c5DistrictIdConvert;
              }
              if (c5DistrictId != null) {
                entity = adminUnitRepository.findByProvinceOrDistrict(c5DistrictIdConvert);
                if (entity != null) {
                  adminUnitDto = new AdminUnitDto(entity);
                } else {
                  dto.setC5District(null);
                  err += "C11 - Kh??ng t??m th???y; ";
                }
              }
              dto.setC5District(adminUnitDto);
            }
          } catch (Exception e) {
            dto.setC5District(null);
            err += "C11 - Kh??ng r??; ";
          }
          try {
            //				c12 - val
            currentCell = currentRow.getCell(11);
            if (currentCell != null) {
              String c6RiskGroups = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c6RiskGroups = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c6RiskGroups = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              Set<PECaseRiskGroupDto> c6 = new LinkedHashSet<>();
              String[] risks = c6RiskGroups.split("\\|");
              for (String ri : risks) {
                PECaseRiskGroupDto r = new PECaseRiskGroupDto();
                if (ri != null) {
                  if (ri.equals("8")) {
                    r.setVal(PERiskGroupEnum.answer1);
                  } else if (ri.equals("9")) {
                    r.setVal(PERiskGroupEnum.answer2);
                  } else if (ri.equals("10")) {
                    r.setVal(PERiskGroupEnum.answer3);
                  } else if (ri.equals("11")) {
                    r.setVal(PERiskGroupEnum.answer4);
                  } else if (ri.equals("12")) {
                    r.setVal(PERiskGroupEnum.answer5);
                  } else if (ri.equals("13")) {
                    r.setVal(PERiskGroupEnum.answer6);
                  } else {
                    r.setVal(null);
                    err += "C12 kh??ng ????ng (C12 ph???i l???n h??n 7 v?? nh??? h??n 14); ";
                  }
                } else {
                  r.setVal(null);
                }
                r.setName(r.getVal().getDescription());
                c6.add(r);
              }
              dto.setC6(c6);
            }
          } catch (Exception e) {
            dto.setC6(null);
            err += "C12 - Kh??ng r??; ";
          }
          try {
            //						c13 - c7
            currentCell = currentRow.getCell(12);
            if (currentCell != null) {
              String c7 = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c7 = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c7 = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              if (c7 != null) {
                if (c7.equals("24")) {
                  dto.setC7(PEApproachMethod.answer1);
                } else if (c7.equals("25")) {
                  dto.setC7(PEApproachMethod.answer2);
                } else if (c7.equals("26")) {
                  dto.setC7(PEApproachMethod.answer3);
                } else if (c7.equals("27")) {
                  dto.setC7(PEApproachMethod.answer4);
                } else if (c7.equals("28")) {
                  dto.setC7(PEApproachMethod.answer5);
                } else {
                  dto.setC7(null);
                  err += "C13 kh??ng ????ng (C13 ph???i l???n h??n 23 v?? nh??? h??n 29); ";
                }
              } else {
                dto.setC7(null);
              }
            }
          } catch (Exception e) {
            dto.setC7(null);
            err += "C13 - Kh??ng r??; ";
          }
          try {
            //						c14 - c8
            currentCell = currentRow.getCell(13);
            if (currentCell != null) {
              String c8 = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c8 = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c8 = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              if (c8 != null) {
                if (c8.equals("29")) {
                  dto.setC8(PEC8.answer1);
                } else if (c8.equals("30")) {
                  dto.setC8(PEC8.answer2);
                } else if (c8.equals("31")) {
                  dto.setC8(PEC8.answer3);
                } else {
                  dto.setC8(null);
                  err += "C14 kh??ng ????ng (C14 ph???i l???n h??n 28 v?? nh??? h??n 32); ";
                }
              } else {
                dto.setC8(null);
              }
            }
          } catch (Exception e) {
            dto.setC8(null);
            err += "C14 - Kh??ng r??; ";
          }
          try {
            //						c15 - c8_arv
            currentCell = currentRow.getCell(14);
            if (currentCell != null) {
              String c8Arv = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c8Arv = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c8Arv = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              if (c8Arv != null) {
                if (c8Arv.equals("32")) {
                  dto.setC8ARV(PEC8ARV.answer1);
                } else if (c8Arv.equals("33")) {
                  dto.setC8ARV(PEC8ARV.answer2);
                } else if (c8Arv.equals("34")) {
                  dto.setC8ARV(PEC8ARV.answer3);
                } else {
                  dto.setC8ARV(null);
                  err += "C15 kh??ng ????ng (C15 ph???i l???n h??n 31 v?? nh??? h??n 35); ";
                }
              } else {
                dto.setC8ARV(null);
              }
            }
          } catch (Exception e) {
            dto.setC8ARV(null);
            err += "C15 - Kh??ng r??; ";
          }
          try {
            //						c16 - c9
            currentCell = currentRow.getCell(15);
            if (currentCell != null) {
              String c9 = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c9 = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c9 = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              if (c9 != null) {
                if (c9.equals("4")) {
                  dto.setC9(HTSYesNoNone.YES);
                } else if (c9.equals("3")) {
                  dto.setC9(HTSYesNoNone.NO);
                } else {
                  dto.setC9(null);
                  err += "C16 kh??ng ????ng (C16 ph???i b???ng 3 ho???c 4); ";
                }
              } else {
                dto.setC9(null);
              }
            }
          } catch (Exception e) {
            dto.setC9(null);
            err += "C16 - Kh??ng r??; ";
          }
          try {
            //						c17 - c9_date
            currentCell = currentRow.getCell(16);
            if (currentCell != null) {
              LocalDateTime c9Date = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                c9Date = LocalDateTime.parse(currentCell.getStringCellValue(), formatter);
              } else if (currentCell.getCellType() == CellType.NUMERIC
                      && currentCell.getDateCellValue() != null) {
                c9Date = currentCell.getDateCellValue().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime();
              }
              dto.setC9Date(c9Date);
            }
          } catch (Exception e) {
            dto.setC9Date(null);
            err += "C17 - Kh??ng r??; ";
          }
          try {
            //						c18 - c10
            currentCell = currentRow.getCell(17);
            if (currentCell != null) {
              String c10 = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c10 = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c10 = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              if (c10 != null) {
                if (c10.equals("4")) {
                  dto.setC10(HTSYesNoNone.YES);
                } else if (c10.equals("3")) {
                  dto.setC10(HTSYesNoNone.NO);
                } else {
                  dto.setC10(null);
                  err += "C18 kh??ng ????ng (C18 ph???i b???ng 3 ho???c 4); ";
                }
              } else {
                dto.setC10(null);
              }
            }
          } catch (Exception e) {
            dto.setC10(null);
            err += "C18 - Kh??ng r??; ";
          }
          try {
            //						c19 - c11
            currentCell = currentRow.getCell(18);
            if (currentCell != null) {
              String c11 = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c11 = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c11 = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              if (c11 != null) {
                if (c11.equals("4")) {
                  dto.setC11(HTSYesNoNone.YES);
                } else if (c11.equals("3")) {
                  dto.setC11(HTSYesNoNone.NO);
                } else {
                  dto.setC11(null);
                  err += "C19 kh??ng ????ng (C19 ph???i b???ng 3 ho???c 4); ";
                }
              } else {
                dto.setC11(null);
              }
            }
          } catch (Exception e) {
            dto.setC11(null);
            err += "C19 - Kh??ng r??; ";
          }
          try {
            //						c20 - c11_date
            currentCell = currentRow.getCell(19);
            if (currentCell != null) {
              LocalDateTime c11Date = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                c11Date = LocalDateTime.parse(currentCell.getStringCellValue(), formatter);
              } else if (currentCell.getCellType() == CellType.NUMERIC
                      && currentCell.getDateCellValue() != null) {
                c11Date = currentCell.getDateCellValue().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime();
              }
              dto.setC11Date(c11Date);
            }
          } catch (Exception e) {
            dto.setC11Date(null);
            err += "C20 - Kh??ng r??; ";
          }
          try {
            //						c21 - c12
            currentCell = currentRow.getCell(20);
            if (currentCell != null) {
              String c12 = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c12 = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c12 = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              if (c12 != null) {
                if (c12.equals("35")) {
                  dto.setC12(PEC12.answer1);
                } else if (c12.equals("36")) {
                  dto.setC12(PEC12.answer2);
                } else if (c12.equals("37")) {
                  dto.setC12(PEC12.answer3);
                } else if (c12.equals("38")) {
                  dto.setC12(PEC12.answer4);
                } else {
                  dto.setC12(null);
                  err += "C21 kh??ng ????ng (C21 ph???i l???n h??n 34 v?? nh??? h??n 39); ";
                }
              } else {
                dto.setC12(null);
              }
            }
          } catch (Exception e) {
            dto.setC12(null);
            err += "C21 - Kh??ng r??; ";
          }
          try {
            //						c22 - c12_code
            currentCell = currentRow.getCell(21);
            if (currentCell != null) {
              String c12Code = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c12Code = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c12Code = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              dto.setC12Code(c12Code);
            }
          } catch (Exception e) {
            dto.setC12Code(null);
            err += "C22 - Kh??ng r??; ";
          }
          try {
            //						c23 - c12_note
            currentCell = currentRow.getCell(22);
            if (currentCell != null) {
              String c12Note = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c12Note = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c12Note = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              if (c12Note != null) {
                if (c12Note.equals("39")) {
                  dto.setC12Note(PEC12BTBC.answer1);
                } else if (c12Note.equals("40")) {
                  dto.setC12Note(PEC12BTBC.answer2);
                } else if (c12Note.equals("41")) {
                  dto.setC12Note(PEC12BTBC.answer3);
                } else {
                  dto.setC12Note(null);
                  err += "C23 kh??ng ????ng (C23 ph???i l???n h??n 38 v?? nh??? h??n 42); ";
                }
              } else {
                dto.setC12Note(null);
              }
            }
          } catch (Exception e) {
            dto.setC12Note(null);
            err += "C23 - Kh??ng r??; ";
          }
          try {
            //						c24 - c13
            currentCell = currentRow.getCell(23);
            if (currentCell != null) {
              String c13 = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c13 = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c13 = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              if (c13 != null) {
                if (c13.equals("43")) {
                  dto.setC13(PEc13.answer1);
                } else if (c13.equals("44")) {
                  dto.setC13(PEc13.answer2);
                } else if (c13.equals("46")) {
                  dto.setC13(PEc13.answer3);
                } else if (c13.equals("47")) {
                  dto.setC13(PEc13.answer4);
                } else {
                  dto.setC13(null);
                  err += "C24 kh??ng ????ng (C24 ph???i l???n h??n 42 v?? nh??? h??n 48); ";
                }
              } else {
                dto.setC13(null);
              }
            }
          } catch (Exception e) {
            dto.setC13(null);
            err += "C24 - Kh??ng r??; ";
          }
          try {
            //						c25 - c131
            currentCell = currentRow.getCell(24);
            if (currentCell != null) {
              String c131 = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c131 = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c131 = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              if (c131 != null) {
                if (c131.equals("4")) {
                  dto.setC131(HTSYesNoNone.YES);
                } else if (c131.equals("3")) {
                  dto.setC131(HTSYesNoNone.NO);
                } else {
                  dto.setC131(null);
                  err += "C25 kh??ng ????ng (C25 ph???i b???ng 3 ho???c 4); ";
                }
              } else {
                dto.setC131(null);
              }
            }
          } catch (Exception e) {
            dto.setC131(null);
            err += "C25 - Kh??ng r??; ";
          }
          try {
            //						c27 - c131_code
            currentCell = currentRow.getCell(26);
            if (currentCell != null) {
              String c131Code = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c131Code = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c131Code = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              dto.setC131Code(c131Code);
            }
          } catch (Exception e) {
            dto.setC131Code(null);
            err += "C27 - Kh??ng r??; ";
          }
          try {
            //						c26 - c131_result
            currentCell = currentRow.getCell(25);
            if (currentCell != null) {
              String c131Result = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c131Result = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c131Result = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              if (c131Result != null) {
                if (c131Result.equals("43")) {
                  dto.setC131Result(PEC131Result.answer1);
                } else if (c131Result.equals("45")) {
                  dto.setC131Result(PEC131Result.answer2);
                } else if (c131Result.equals("46")) {
                  dto.setC131Result(PEC131Result.answer3);
                } else if (c131Result.equals("47")) {
                  dto.setC131Result(PEC131Result.answer4);
                } else {
                  dto.setC131Result(null);
                  err += "C26 kh??ng ????ng (C26 ph???i l???n h??n 42 v?? nh??? h??n 48); ";
                }
              } else {
                dto.setC131Result(null);
              }
            }
          } catch (Exception e) {
            dto.setC131Result(null);
            err += "C26 - Kh??ng r??; ";
          }
          try {
            //						c41 - c132
            currentCell = currentRow.getCell(40);
            if (currentCell != null) {
              String c132 = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c132 = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c132 = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              if (c132 != null) {
                if (c132.equals("193")) {
                  dto.setC132(PEC132.answer1);
                } else if (c132.equals("194")) {
                  dto.setC132(PEC132.answer2);
                } else if (c132.equals("195")) {
                  dto.setC132(PEC132.answer3);
                } else {
                  dto.setC132(null);
                  err += "C41 kh??ng ????ng (C41 ph???i l???n h??n 192 v?? nh??? h??n 196); ";
                }
              } else {
                dto.setC132(null);
              }
            }
          } catch (Exception e) {
            dto.setC132(null);
            err += "C41 - Kh??ng r??; ";
          }
          try {
            //						c28 - c14
            currentCell = currentRow.getCell(27);
            if (currentCell != null) {
              String c14 = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c14 = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c14 = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              if (c14 != null) {
                if (c14.equals("4")) {
                  dto.setC14(HTSYesNoNone.YES);
                } else if (c14.equals("3")) {
                  dto.setC14(HTSYesNoNone.NO);
                } else {
                  dto.setC14(null);
                  err += "C28 kh??ng ????ng (C28 ph???i b???ng 3 ho???c 4); ";
                }
              } else {
                dto.setC14(null);
              }
            }
          } catch (Exception e) {
            dto.setC14(null);
            err += "C28 - Kh??ng r??; ";
          }
          try {
            //						c29 - c14_date
            currentCell = currentRow.getCell(28);
            if (currentCell != null) {
              LocalDateTime c14Date = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                c14Date = LocalDateTime.parse(currentCell.getStringCellValue(), formatter);
              } else if (currentCell.getCellType() == CellType.NUMERIC
                      && currentCell.getDateCellValue() != null) {
                c14Date = currentCell.getDateCellValue().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime();
              }
              dto.setC14Date(c14Date);
            }
          } catch (Exception e) {
            dto.setC14Date(null);
            err += "C29 - Kh??ng r??; ";
          }
          try {
            //						c30 - c14_code
            currentCell = currentRow.getCell(29);
            if (currentCell != null) {
              String c14Code = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c14Code = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c14Code = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              dto.setC14Code(c14Code);
            }
          } catch (Exception e) {
            dto.setC14Code(null);
            err += "C30 - Kh??ng r??; ";
          }
          try {
            //						c31 - c14_name
            currentCell = currentRow.getCell(30);
            if (currentCell != null) {
              String c14Name = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c14Name = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c14Name = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              dto.setC14Name(c14Name);
            }
          } catch (Exception e) {
            dto.setC14Name(null);
            err += "C31 - Kh??ng r??; ";
          }
          try {
            //						c32 - c15
            currentCell = currentRow.getCell(31);
            if (currentCell != null) {
              String c15 = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c15 = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c15 = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              if (c15 != null) {
                if (c15.equals("4")) {
                  dto.setC15(HTSYesNoNone.YES);
                } else if (c15.equals("3")) {
                  dto.setC15(HTSYesNoNone.NO);
                } else {
                  dto.setC15(null);
                  err += "C32 kh??ng ????ng (C32 ph???i b???ng 3 ho???c 4); ";
                }
              } else {
                dto.setC15(null);
              }
            }
          } catch (Exception e) {
            dto.setC15(null);
            err += "C32 - Kh??ng r??; ";
          }
          try {
            //						c33 - c15_date
            currentCell = currentRow.getCell(32);
            if (currentCell != null) {
              LocalDateTime c15Date = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                c15Date = LocalDateTime.parse(currentCell.getStringCellValue(), formatter);
              } else if (currentCell.getCellType() == CellType.NUMERIC
                      && currentCell.getDateCellValue() != null) {
                c15Date = currentCell.getDateCellValue().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime();
              }
              dto.setC15Date(c15Date);
            }
          } catch (Exception e) {
            dto.setC15Date(null);
            err += "C33 - Kh??ng r??; ";
          }
          try {
            //						c34 - c15_code
            currentCell = currentRow.getCell(33);
            if (currentCell != null) {
              String c15Code = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c15Code = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c15Code = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              dto.setC15Code(c15Code);
            }
          } catch (Exception e) {
            dto.setC15Code(null);
            err += "C34 - Kh??ng r??; ";
          }
          try {
            //						c35 - c15_name
            currentCell = currentRow.getCell(34);
            if (currentCell != null) {
              String c15Name = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c15Name = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c15Name = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              dto.setC15Name(c15Name);
            }
          } catch (Exception e) {
            dto.setC15Name(null);
            err += "C35 - Kh??ng r??; ";
          }
          try {
            //						c40 - c16
            currentCell = currentRow.getCell(39);
            if (currentCell != null) {
              String c16 = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c16 = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c16 = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              if (c16 != null) {
                if (c16.equals("81")) {
                  dto.setC16(PEC16.answer1);
                } else if (c16.equals("82")) {
                  dto.setC16(PEC16.answer2);
                } else if (c16.equals("83")) {
                  dto.setC16(PEC16.answer3);
                } else {
                  dto.setC16(null);
                  err += "C40 kh??ng ????ng (C40 ph???i l???n h??n 80 v?? nh??? h??n 84); ";
                }
              } else {
                dto.setC16(null);
              }
            }
          } catch (Exception e) {
            dto.setC6(null);
            err += "C40 - Kh??ng r??; ";
          }
          try {
            //						c36 - c17
            currentCell = currentRow.getCell(35);
            if (currentCell != null) {
              String c17 = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c17 = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c17 = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              dto.setC17(c17);
            }
          } catch (Exception e) {
            dto.setC17(null);
            err += "C36 - Kh??ng r??; ";
          }
//          try {
//            saveOrUpdate(dto);
//            dto.setSaved(true);
//          } catch (Exception e) {
//            err += e.getMessage();
//            dto.setSaved(false);
//            dto.setNumberErrorContent(rowIndex + 1);
//
//          }
//          dto.setErrorContent(err);
//          if (dto.getErrorContent() != null) {
//            ret.setTotalErr(ret.getTotalErr() + 1);
//            ret.getListErr().add(dto);
//          }
//          dto.setErrorContent(err);
//          if (!dto.getErrorContent().equals("")) {
//            dto.setNumberErrorContent(rowIndex + 1);
//            ret.setTotalErr(ret.getTotalErr() + 1);
//            ret.getListErr().add(dto);
//          }
//          try {
//            if(saveOrUpdate(dto) != null) {
//              dto.setSaved(true);
//            } else {
//              dto.setSaved(false);
//              if (dto.getErrorContent().equals("")) {
//                dto.setErrorContent("L??u th???t b???i");
//                dto.setNumberErrorContent(rowIndex + 1);
//                ret.setTotalErr(ret.getTotalErr() + 1);
//                ret.getListErr().add(dto);
//              } else {
//                dto.setErrorContent(err+"L??u th???t b???i");
//              }
//            }
//          } catch (Exception e) {
//            dto.setSaved(false);
//            if (dto.getErrorContent().equals("")) {
//              dto.setErrorContent("Kh??ng r??");
//              dto.setNumberErrorContent(rowIndex + 1);
//              ret.setTotalErr(ret.getTotalErr() + 1);
//              ret.getListErr().add(dto);
//            } else {
//              dto.setErrorContent(err+"L??u th???t b???i");
//            }
//          }
          try {
            dto = saveOrUpdate(dto);
            dto.setSaved(true);
          } catch (Exception e) {
            err += "L??u th???t b???i:" + CommonUtils.getStackTrace(e);
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
      } catch (Exception e2) {
        e2.printStackTrace();
        ret.setTotalErr(ret.getTotalErr() + 1);
        // TODO: handle exception
      }
      rowIndex += 1;
    }

    while (rowIndexParent <= num) {
      try {
        System.out.println(rowIndexParent);
        Row currentRow = datatypeSheet.getRow(rowIndexParent);
        Cell currentCell = null;
        String err = "";
        if (currentRow != null) {
          PECaseDto dto = new PECaseDto();
          String c1Str = null;
          try {
//						c1
            currentCell = currentRow.getCell(0);
            if (currentCell != null) {
              if (currentCell.getCellType() == CellType.STRING) {
                c1Str = currentCell.getStringCellValue();
                UUID uid = UUID.fromString(currentCell.getStringCellValue());
                if (uid != null) {
                  PECase peCase = peCaseRepository.findByUid(uid);
                  if (peCase != null) {
                    dto = new PECaseDto(peCase, true, true, false, false);
                  }
                }
              }
            }
            if (!StringUtils.hasText(c1Str)) {
              rowIndexParent += 1;
              continue;
            }
          } catch (Exception e) {
            dto.setUid(null);
            err += "C1 - Kh??ng r??; ";
          }
          try {
// 						c2 - parent_id
            currentCell = currentRow.getCell(1);
            String parentId = null;
            if (currentCell != null) {
              if (currentCell.getCellType() == CellType.STRING) {
                parentId = currentCell.getStringCellValue();
                UUID uid = null;
                uid = UUID.fromString(currentCell.getStringCellValue());
                if(uid != null) {
                  PECase peCase = peCaseRepository.findByUid(uid);
                  if (peCase != null) {
                    dto.setParent(new PECaseDto(peCase, true, true, false, false));
                  } else {
                    dto.setParent(null);
                    err += "C2 - Kh??ng t??m th???y; ";
                  }
                }
              }
            }
            if (!StringUtils.hasText(parentId)) {
              rowIndexParent += 1;
              continue;
            }
          } catch (Exception e) {
            System.out.println(e.getMessage());
            dto.setParent(null);
            err += "C2 - Kh??ng r??; ";
          }

          try {
            dto = saveOrUpdate(dto);
            dto.setSaved(true);
          } catch (Exception e) {
            err += "L??u th???t b???i:" + CommonUtils.getStackTrace(e);
            ret.setTotalErr(ret.getTotalErr() + 1);
            dto.setSaved(false);
          }
          dto.setErrorContent(err);
          if (!dto.getErrorContent().equals("")) {
            dto.setNumberErrorContent(rowIndexParent + 1);
            ret.setTotalErr(ret.getTotalErr() + 1);
            ret.getListErr().add(dto);
          }
        }
      } catch (Exception e2) {
        e2.printStackTrace();
        ret.setTotalErr(ret.getTotalErr() + 1);
        // TODO: handle exception
      }
      rowIndexParent += 1;
    }
    return ret;
  }

  @Override
  public List<OrganizationDto> getListPEWriteAble() {
    User currentUser = SecurityUtils.getCurrentUser();
    if (currentUser == null) {
      return null;
    }
    List<OrganizationDto> ret = new ArrayList<OrganizationDto>();
    List<UserOrganization> listUO = userOrganizationRepository.getListByUserId(currentUser.getId());
    for (UserOrganization userOrganization : listUO) {
      if (userOrganization.getWriteAccess() != null && userOrganization.getWriteAccess() &&
          userOrganization.getPeRole()!=null && userOrganization.getPeRole()) {
        ret.add(new OrganizationDto(userOrganization.getOrganization()));
      }
    }
    return ret;
  }

  @Override
  public List<ReportMERPEDto> getDataReportMERPE(PreventionFilterDto filter) {
    Hashtable<BigInteger,ReportMERPEDto> hashtable = new Hashtable<>();
    List<ReportMERPEDto> kpPREV = reportMerService.getDataKPPREV(filter);
    List<ReportMERPEDto> kpPREVC8Positives = reportMerService.getDataKPPREVC8Positives(filter);
    List<ReportMERPEDto> kpPREVC11Yes = reportMerService.getDataKPPREVC11Yes(filter);
    List<ReportMERPEDto> kpPREVC11No = reportMerService.getDataKPPREVC11No(filter);

    List<ReportMERPEDto> ppPREVTesting = reportMerService.getDataPPPREVTesting(filter);
    List<ReportMERPEDto> ppPREVPriority = reportMerService.getDataPPPREVPriority(filter);
    List<ReportMERPEDto> ppPREVSexAndAge = reportMerService.getDataPPPREVAgeAndSex(filter);
    ReportMERPEDto reportMERPEDto = null;
    for (ReportMERPEDto dto : kpPREV){
      if(hashtable.containsKey(dto.getOrgId())){
        reportMERPEDto  = hashtable.get(dto.getOrgId());

        reportMERPEDto.setPwid(reportMERPEDto.getPwid().add(dto.getPwid()));
        reportMERPEDto.setMsm(reportMERPEDto.getMsm().add(dto.getMsm()));
        reportMERPEDto.setTg(reportMERPEDto.getTg().add(dto.getTg()));
        reportMERPEDto.setFsw(reportMERPEDto.getFsw().add(dto.getFsw()));
        reportMERPEDto.setOther(reportMERPEDto.getOther().add(dto.getOther()));

        hashtable.put(dto.getOrgId(),reportMERPEDto);
      }else{
        hashtable.put(dto.getOrgId(),dto);
      }
    }
    for (ReportMERPEDto dto : kpPREVC8Positives){
      if(hashtable.containsKey(dto.getOrgId())){
        reportMERPEDto  = hashtable.get(dto.getOrgId());

        reportMERPEDto.setPwidC8Positives(reportMERPEDto.getPwidC8Positives().add(dto.getPwidC8Positives()));
        reportMERPEDto.setMsmC8Positives(reportMERPEDto.getMsmC8Positives().add(dto.getMsmC8Positives()));
        reportMERPEDto.setTgC8Positives(reportMERPEDto.getTgC8Positives().add(dto.getTgC8Positives()));
        reportMERPEDto.setFswC8Positives(reportMERPEDto.getFswC8Positives().add(dto.getFswC8Positives()));
        reportMERPEDto.setOtherC8Positives(reportMERPEDto.getOtherC8Positives().add(dto.getOtherC8Positives()));

        hashtable.put(dto.getOrgId(),reportMERPEDto);
      }else{
        hashtable.put(dto.getOrgId(),dto);
      }
    }

    for (ReportMERPEDto dto : kpPREVC11Yes){
      if(hashtable.containsKey(dto.getOrgId())){
        reportMERPEDto  = hashtable.get(dto.getOrgId());

        reportMERPEDto.setPwidC11Yes(reportMERPEDto.getPwidC11Yes().add(dto.getPwidC11Yes()));
        reportMERPEDto.setMsmC11Yes(reportMERPEDto.getMsmC11Yes().add(dto.getMsmC11Yes()));
        reportMERPEDto.setTgC11Yes(reportMERPEDto.getTgC11Yes().add(dto.getTgC11Yes()));
        reportMERPEDto.setFswC11Yes(reportMERPEDto.getFswC11Yes().add(dto.getFswC11Yes()));
        reportMERPEDto.setOtherC11Yes(reportMERPEDto.getOtherC11Yes().add(dto.getOtherC11Yes()));

        hashtable.put(dto.getOrgId(),reportMERPEDto);
      }else{
        hashtable.put(dto.getOrgId(),dto);
      }
    }

    for (ReportMERPEDto dto : kpPREVC11No){
      if(hashtable.containsKey(dto.getOrgId())){
        reportMERPEDto  = hashtable.get(dto.getOrgId());

        reportMERPEDto.setPwidC11No(reportMERPEDto.getPwidC11No().add(dto.getPwidC11No()));
        reportMERPEDto.setMsmC11No(reportMERPEDto.getMsmC11No().add(dto.getMsmC11No()));
        reportMERPEDto.setTgC11No(reportMERPEDto.getTgC11No().add(dto.getTgC11No()));
        reportMERPEDto.setFswC11No(reportMERPEDto.getFswC11No().add(dto.getFswC11No()));
        reportMERPEDto.setOtherC11No(reportMERPEDto.getOtherC11No().add(dto.getOtherC11No()));

        hashtable.put(dto.getOrgId(),reportMERPEDto);
      }else{
        hashtable.put(dto.getOrgId(),dto);
      }
    }

    for (ReportMERPEDto dto : ppPREVTesting){
      if(hashtable.containsKey(dto.getOrgId())){
        reportMERPEDto  = hashtable.get(dto.getOrgId());

        reportMERPEDto.setTestingC8Positives(reportMERPEDto.getTestingC8Positives().add(dto.getTestingC8Positives()));
        reportMERPEDto.setTestingC11Yes(reportMERPEDto.getTestingC11Yes().add(dto.getTestingC11Yes()));
        reportMERPEDto.setTestingC11No(reportMERPEDto.getTestingC11No().add(dto.getTestingC11No()));
        reportMERPEDto.setTestingNotRequired(reportMERPEDto.getTestingNotRequired().add(dto.getTestingNotRequired()));

        hashtable.put(dto.getOrgId(),reportMERPEDto);
      }else{
        hashtable.put(dto.getOrgId(),dto);
      }
    }
    for (ReportMERPEDto dto : ppPREVPriority){
      if(hashtable.containsKey(dto.getOrgId())){
        reportMERPEDto  = hashtable.get(dto.getOrgId());

        reportMERPEDto.setPriorityOther(reportMERPEDto.getPriorityOther().add(dto.getPriorityOther()));

        hashtable.put(dto.getOrgId(),reportMERPEDto);
      }else{
        hashtable.put(dto.getOrgId(),dto);
      }
    }
    for (ReportMERPEDto dto : ppPREVSexAndAge){
      if(hashtable.containsKey(dto.getOrgId())){
        reportMERPEDto  = hashtable.get(dto.getOrgId());

        reportMERPEDto.setFu(reportMERPEDto.getFu().add(dto.getFu()));
        reportMERPEDto.setF10(reportMERPEDto.getF10().add(dto.getF10()));
        reportMERPEDto.setF15(reportMERPEDto.getF15().add(dto.getF15()));
        reportMERPEDto.setF20(reportMERPEDto.getF20().add(dto.getF20()));
        reportMERPEDto.setF25(reportMERPEDto.getF25().add(dto.getF25()));
        reportMERPEDto.setF30(reportMERPEDto.getF30().add(dto.getF30()));
        reportMERPEDto.setF35(reportMERPEDto.getF35().add(dto.getF35()));
        reportMERPEDto.setF40(reportMERPEDto.getF40().add(dto.getF40()));
        reportMERPEDto.setF45(reportMERPEDto.getF45().add(dto.getF45()));
        reportMERPEDto.setF50(reportMERPEDto.getF50().add(dto.getF50()));

        reportMERPEDto.setMu(reportMERPEDto.getMu().add(dto.getMu()));
        reportMERPEDto.setM10(reportMERPEDto.getM10().add(dto.getM10()));
        reportMERPEDto.setM15(reportMERPEDto.getM15().add(dto.getM15()));
        reportMERPEDto.setM20(reportMERPEDto.getM20().add(dto.getM20()));
        reportMERPEDto.setM25(reportMERPEDto.getM25().add(dto.getM25()));
        reportMERPEDto.setM30(reportMERPEDto.getM30().add(dto.getM30()));
        reportMERPEDto.setM35(reportMERPEDto.getM35().add(dto.getM35()));
        reportMERPEDto.setM40(reportMERPEDto.getM40().add(dto.getM40()));
        reportMERPEDto.setM45(reportMERPEDto.getM45().add(dto.getM45()));
        reportMERPEDto.setM50(reportMERPEDto.getM50().add(dto.getM50()));

        hashtable.put(dto.getOrgId(),reportMERPEDto);
      }else{
        hashtable.put(dto.getOrgId(),dto);
      }
    }


    return new ArrayList<>(hashtable.values());
  }
}