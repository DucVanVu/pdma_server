package org.pepfar.pdma.app.data.service.jpa;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pepfar.pdma.app.Constants;
import org.pepfar.pdma.app.data.domain.*;
import org.pepfar.pdma.app.data.dto.*;
import org.pepfar.pdma.app.data.repository.*;
import org.pepfar.pdma.app.data.service.PNSCaseService;
import org.pepfar.pdma.app.data.types.*;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service("PNSCaseServiceImpl")
public class PNSCaseServiceImpl implements PNSCaseService {

  @Autowired
  public EntityManager manager;

  @Autowired
  private AuthorizationUtils authUtils;

  @Autowired
  private HTSCaseRespository htsCaseRepository;

  @Autowired
  private PNSCaseRespository pnsCaseRepository;

  @Autowired
  private OrganizationRepository organizationRepository;

  @Autowired
  private StaffRepository staffRepository;

  @Autowired
  private DictionaryRepository dictionaryRepository;

  @Autowired
  private AdminUnitRepository adminUnitRepository;

  @Autowired
  private PNSCaseRiskGroupRepository pnsCaseRiskGroupRepository;

  @Autowired
  private CaseRepository caseRepository;

  @Autowired
  private ApplicationContext context;

  @Autowired
  private UserOrganizationRepository userOrganizationRepository;

  @Override
  public Page<PNSCaseDto> findAllPageable(PreventionFilterDto filter) {
    User currentUser = SecurityUtils.getCurrentUser();
    Boolean isAdministrator = SecurityUtils.isUserInRole(currentUser, "ROLE_ADMIN");
	Boolean isProvince = SecurityUtils.isUserInRole(currentUser, "ROLE_PROVINCIAL_MANAGER");
	Boolean isSite = SecurityUtils.isUserInRole(currentUser, "ROLE_SITE_MANAGER");
	
	Boolean isDONOR = SecurityUtils.isUserInRole(currentUser, "ROLE_DONOR");
	Boolean isNATIONAL = SecurityUtils.isUserInRole(currentUser, "ROLE_NATIONAL_MANAGER");
	Boolean isDISTRICT = SecurityUtils.isUserInRole(currentUser, "ROLE_DISTRICT_MANAGER");
	
    Boolean isViewPII = isSite || isProvince;
    //Check ORG
    final List<Long> orgIds = authUtils.getGrantedOrgIds(Permission.READ_ACCESS);
    final List<Long> writableOrgIds = authUtils.getGrantedOrgIds(Permission.WRITE_ACCESS);
    final List<Long> pnsableOrgIds = authUtils.getGrantedOrgIds(Permission.PNS_ACCESS);

    if (isAdministrator || (filter != null && ((orgIds != null && orgIds.size() > 0) || (writableOrgIds != null && writableOrgIds.size() > 0) || (pnsableOrgIds != null && pnsableOrgIds.size() > 0)))) {
      //Check ORG
      List<Long> lstOrgIds = new ArrayList<Long>();
      List<Long> lstPnsOrgIds = new ArrayList<Long>();
      if (orgIds != null && orgIds.size() > 0) {
        lstOrgIds.addAll(orgIds);
      }
      if (writableOrgIds != null && writableOrgIds.size() > 0) {
        lstOrgIds.addAll(writableOrgIds);
      }
      if (pnsableOrgIds != null && pnsableOrgIds.size() > 0) {
        lstPnsOrgIds.addAll(pnsableOrgIds);
      }

      if(CollectionUtils.isEmpty(lstOrgIds) && !isAdministrator) {
        return null;
      }
      if(CollectionUtils.isEmpty(lstPnsOrgIds) && !isAdministrator && !isDONOR && !isNATIONAL && !isProvince && !isDISTRICT) {
        return null;
      }

      String SQL = "";
      if (isViewPII) {
        SQL = " SELECT new org.pepfar.pdma.app.data.dto.PNSCaseDto(s, 1, true, false, false) from PNSCase s WHERE 1=1 ";
      } else {
        SQL = " SELECT new org.pepfar.pdma.app.data.dto.PNSCaseDto(s, 0, true, false, false) from PNSCase s WHERE 1=1 ";
      }
      String countSQL = " SELECT COUNT(s.id) from PNSCase s WHERE 1=1 ";
      String whereClause = " ";
      if(!isAdministrator) {
        whereClause += " AND s.c2.id in (:lstOrgIds) ";
        if(isSite && !isDONOR && !isNATIONAL && !isProvince && !isDISTRICT) {
          whereClause += " AND s.c2.id in (:lstPnsOrgIds) ";
        }
      }

      String orderByClause = "";

      if (filter.getKeyword() != null && filter.getKeyword().length() > 0) {
        whereClause += " AND ("
                + " (s.c4 like :keyword) "
                + " OR (s.c7 like :keyword) "
                + " )";
      }
      if (filter.getOrg() != null && filter.getOrg().getId() != null) {
        whereClause += " AND (s.c2.id = :orgId)";
      }
      if (filter.getStaff() != null && filter.getStaff().getId() != null) {
        whereClause += " AND (s.c3.id = :staffId)";
      }
      if (filter.getFromDate() != null) {
        whereClause += " AND (s.c5 >= :from)";
      }
      if (filter.getToDate() != null) {
        whereClause += " AND (s.c5 <= :to)";
      }
      if (filter.getType() != null && filter.getType() != 2) {
        if (filter.getType() == 1) {
          whereClause += " AND (s.c6 = :types)";
        }
      }
      if(filter.getProvinceId() != null) {
        whereClause += " AND (s.c2.address.province.id = :provinceId) ";
      }

      Query q = manager.createQuery(SQL + whereClause + orderByClause, PNSCaseDto.class);
      Query qCount = manager.createQuery(countSQL + whereClause);
      if(!isAdministrator) {
        q.setParameter("lstOrgIds", lstOrgIds);
        qCount.setParameter("lstOrgIds", lstOrgIds);
        if(isSite && !isDONOR && !isNATIONAL && !isProvince && !isDISTRICT) {
          q.setParameter("lstPnsOrgIds", lstPnsOrgIds);
          qCount.setParameter("lstPnsOrgIds", lstPnsOrgIds);
        }
      }
      if (filter.getKeyword() != null && filter.getKeyword().length() > 0) {
        q.setParameter("keyword", "%" + filter.getKeyword() + "%");
        qCount.setParameter("keyword", "%" + filter.getKeyword() + "%");
      }
      if (filter.getOrg() != null && filter.getOrg().getId() != null) {
        q.setParameter("orgId", filter.getOrg().getId());
        qCount.setParameter("orgId", filter.getOrg().getId());
      }
      if (filter.getStaff() != null && filter.getStaff().getId() != null) {
        q.setParameter("staffId", filter.getStaff().getId());
        qCount.setParameter("staffId", filter.getStaff().getId());
      }
      if (filter.getFromDate() != null) {
        q.setParameter("from", filter.getFromDate());
        qCount.setParameter("from", filter.getFromDate());
      }
      if (filter.getToDate() != null) {
        q.setParameter("to", filter.getToDate());
        qCount.setParameter("to", filter.getToDate());
      }
      if (filter.getType() != null && filter.getType() != 0) {
        if (filter.getType() == 1) {
          q.setParameter("types", HTSYesNoNone.YES);
          qCount.setParameter("types", HTSYesNoNone.YES);
        }
      }
      if(filter.getProvinceId() != null) {
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

      @SuppressWarnings("unchecked")
      List<PNSCaseDto> entities = q.getResultList();
      Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize());
      Page<PNSCaseDto> result = new PageImpl<PNSCaseDto>(entities, pageable, count);

      return result;
    }
    return null;
  }

  @SuppressWarnings("unlikely-arg-type")
  @Override
  public PNSCaseDto saveOrUpdate(PNSCaseDto pnsCaseDto) {
    User currentUser = SecurityUtils.getCurrentUser();
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
    if (pnsCaseDto == null) {
      throw new IllegalArgumentException("Cannot save a null instance of PNSCase.");
    }

    PNSCase entity = null;

    if (CommonUtils.isPositive(pnsCaseDto.getId(), true)) {
      entity = pnsCaseRepository.findOne(pnsCaseDto.getId());
    }

    if (entity == null) {
      entity = new PNSCase();
    }
    
    //bắt đầu xét các trường của entity
  	if (pnsCaseDto.getUid() != null) {
  		entity.setUid(pnsCaseDto.getUid());
  	}
    Organization c2 = null;
    if (pnsCaseDto.getC2() != null && pnsCaseDto.getC2().getId() != null) {
      c2 = organizationRepository.findOne(pnsCaseDto.getC2().getId());
//      if (c2 != null && !writableOrgIds.contains(c2.getId())) {
//        return null;
//      }
      entity.setC2(c2);
    }

    Staff c3 = null;
    if (pnsCaseDto.getC3() != null && pnsCaseDto.getC3().getId() != null) {
      c3 = staffRepository.findOne(pnsCaseDto.getC3().getId());
      entity.setC3(c3);
    }

    entity.setC4(pnsCaseDto.getC4());
    entity.setC5(pnsCaseDto.getC5());
    if (pnsCaseDto.getHivCase() != null && pnsCaseDto.getHivCase().getId() != null && pnsCaseDto.getHivCase().getId() > 0L) {
      Case hivCase = caseRepository.findOne(pnsCaseDto.getHivCase().getId());
      entity.setHivCase(hivCase);
    }
    if (pnsCaseDto.getHtsCase() != null && pnsCaseDto.getHtsCase().getId() != null && pnsCaseDto.getHtsCase().getId() > 0L) {
      HTSCase htsCase = htsCaseRepository.findOne(pnsCaseDto.getHtsCase().getId());
      entity.setHtsCase(htsCase);
    }


    entity.setC6(pnsCaseDto.getC6());
    entity.setC6Date(pnsCaseDto.getC6Date());
    entity.setC7(pnsCaseDto.getC7());
    entity.setC8(pnsCaseDto.getC8());
    if (pnsCaseDto.getC9() != null && pnsCaseDto.getC9() > 0) {
      LocalDateTime dob = LocalDateTime.of(pnsCaseDto.getC9(), 6, 15, 0, 0);
      entity.setC9(dob);
    }
    entity.setC10(pnsCaseDto.getC10());
    entity.setC11(pnsCaseDto.getC11());

    Set<PNSCaseRiskGroup> c12 = new LinkedHashSet<>();
    if (pnsCaseDto.getC12() != null && pnsCaseDto.getC12().size() > 0) {
      for (PNSCaseRiskGroupDto pnsCaseRiskDto : pnsCaseDto.getC12()) {
        PNSCaseRiskGroup rg = null;
        if (pnsCaseRiskDto.getId() != null) {
          rg = pnsCaseRiskGroupRepository.findOne(pnsCaseRiskDto.getId());
        }
        if (rg == null) {
          rg = new PNSCaseRiskGroup();
        }
        rg.setPnsCase(entity);
        rg.setVal(pnsCaseRiskDto.getVal());
        if (pnsCaseRiskDto.getVal() != null) {
          rg.setName(pnsCaseRiskDto.getVal().getDescription());
        }
        c12.add(rg);
      }
    }
    if(entity.getC12()!=null && entity.getC12().size()>0) {
    	int minRisk =16;
    	PNSCaseRiskGroup pnsCaseMinRiskGroup=null;
    	for (PNSCaseRiskGroup pnsCaseRiskGroup : entity.getC12()) {
    		pnsCaseRiskGroup.setIsMainRisk(false);;
			if(pnsCaseRiskGroup.getVal().getPriority() > 0 && pnsCaseRiskGroup.getVal().getPriority() <= minRisk) {
				minRisk = pnsCaseRiskGroup.getVal().getPriority();
				pnsCaseMinRiskGroup = pnsCaseRiskGroup;
			}
		}
    	if(pnsCaseMinRiskGroup!=null) {
    		pnsCaseMinRiskGroup.setIsMainRisk(true);
    	}
    }
    
    entity.getC12().clear();
    entity.getC12().addAll(c12);
    entity.setC12Note(pnsCaseDto.getC12Note());
    entity = pnsCaseRepository.save(entity);

    if (entity != null) {
//      if (isSiteManagement) {
      return newPNSCaseDto(entity, isSite, isProvince, isAdministrator, listUO);
//      } else {
//        return new PNSCaseDto(entity, 0);
//      }
    } else {
      throw new RuntimeException();
    }
  }

  @Override
  public ResponseDto<PNSCaseDto> deleteById(Long id) {
    ResponseDto<PNSCaseDto> ret = new ResponseDto<PNSCaseDto>();
    final List<Long> writableOrgIds = authUtils.getGrantedOrgIds(Permission.WRITE_ACCESS);
    PNSCase entity = pnsCaseRepository.findOne(id);
    if (entity != null) {
      if (entity.getC2() != null && !writableOrgIds.contains(entity.getC2().getId())) {
        ret.setCode(YesNoNone.NO);
        ret.setMessage("Bạn không có quyền xóa bản ghi này");
        return ret;
      }
      pnsCaseRepository.delete(entity);
      ret.setCode(YesNoNone.YES);
      ret.setMessage("Xóa thành công");
      return ret;
    } else {
      ret.setCode(YesNoNone.NO);
      ret.setMessage("Không tìm thấy bản ghi tương ứng");
      return ret;
    }

  }

  @Override
  public PNSCaseDto findById(long id) {
    User currentUser = SecurityUtils.getCurrentUser();
    Boolean isSiteManagement = SecurityUtils.isUserInRole(currentUser, "ROLE_SITE_MANAGER");
    if(!isSiteManagement) {
      isSiteManagement = SecurityUtils.isUserInRole(currentUser, "ROLE_PROVINCIAL_MANAGER");
    }
    if (!CommonUtils.isPositive(id, true)) {
      return null;
    }
    PNSCase entity = pnsCaseRepository.findOne(id);
    if (entity != null) {
      Boolean isSite = SecurityUtils.isUserInRole(currentUser, "ROLE_SITE_MANAGER");
      Boolean isProvince = SecurityUtils.isUserInRole(currentUser, "ROLE_PROVINCIAL_MANAGER");
      Boolean isAdministrator = SecurityUtils.isUserInRole(currentUser, "ROLE_ADMIN");
      List<UserOrganization> listUO = null;
      if (!isAdministrator) {
        listUO = userOrganizationRepository.getListByUserId(currentUser.getId());
      }
      return newPNSCaseDto(entity, isSite, isProvince, isAdministrator, listUO);
    } else {
      return null;
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Workbook exportReportPNSCase(PreventionFilterDto filter) {
    Workbook blankBook = new XSSFWorkbook();
    blankBook.createSheet();
    if(filter.getProvinceId()!=null && CollectionUtils.isEmpty(filter.getOrgIds())) {
		filter.setOrgIds(organizationRepository.findAllIdByProvinceId(filter.getProvinceId()));
	}
    PreventionReportDto<PNSDetailReportDto> preventionReportDto = this.getReport(filter);
//		if (htsCaseReportDto == null || htsCaseReportDto.getListDetail().size() == 0) {
//			return blankBook;
//		}
//		else {
    XSSFWorkbook wbook = null;
//    SXSSFWorkbook wbook = null;
    
    try (InputStream template = context.getResource("classpath:templates/pns-report.xlsx")
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
    int rowIndex = 8;

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
    ExcelUtils.setBorders4Style(dateTimeStyle);
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

    row = sheet.getRow(3);
    cell = row.getCell(3);
    cell.setCellValue(preventionReportDto.getOrgName());
    cell = row.getCell(9);
    cell.setCellValue(preventionReportDto.getProvinceName());

    row = sheet.getRow(4);
    cell = row.getCell(2);
//		cell.setCellStyle(dateTimeStyle);
    if (preventionReportDto.getFromDate() != null) {
      cell.setCellValue(formatter.format(Date.from(preventionReportDto.getFromDate().toInstant(ZoneOffset.of("+7")))));
    }
    cell = row.getCell(5);
//		cell.setCellStyle(dateTimeStyle);
    if (preventionReportDto.getToDate() != null) {
      cell.setCellValue(formatter.format(Date.from(preventionReportDto.getToDate().toInstant(ZoneOffset.of("+7")))));
    }
    cell = row.getCell(9);
    cell.setCellValue(preventionReportDto.getDistrictName());

    for (PNSDetailReportDto pnsDetailReportDto : preventionReportDto.getListDetail()) {
      if (rowIndex == 16) {
        rowIndex++;
      }
      row = sheet.getRow(rowIndex++);
//				row.setHeightInPoints(22);

      //Cộng đồng
      cell = row.getCell(8);
      if (pnsDetailReportDto.getCommunity() != null) {
        cell.setCellValue(pnsDetailReportDto.getCommunity());
      }

      //Tại cơ sở cung cấp dịch vụ
      cell = row.getCell(9);
      if (pnsDetailReportDto.getMedicalFacility() != null) {
        cell.setCellValue(pnsDetailReportDto.getMedicalFacility());
      }

      //Tại cơ sở khác
      if (rowIndex == 13 || rowIndex == 14 || rowIndex == 16) {
        cell = row.getCell(10);
        if (pnsDetailReportDto.getOtherMedicalFacility() != null) {
          cell.setCellValue(pnsDetailReportDto.getOtherMedicalFacility());
        }
      }

      //TỔNG SỐ
      cell = row.getCell(11);
      if (rowIndex == 13 || rowIndex == 14 || rowIndex == 16) {
        if (pnsDetailReportDto.getOtherMedicalFacility() != null && pnsDetailReportDto.getMedicalFacility() != null && pnsDetailReportDto.getCommunity() != null) {
          cell.setCellValue(pnsDetailReportDto.getOtherMedicalFacility() + pnsDetailReportDto.getMedicalFacility() + pnsDetailReportDto.getCommunity());
        }
      } else {
        if (pnsDetailReportDto.getMedicalFacility() != null && pnsDetailReportDto.getCommunity() != null) {
          cell.setCellValue(pnsDetailReportDto.getMedicalFacility() + pnsDetailReportDto.getCommunity());
        }
      }

    }
    return wbook;
  }

	@Override
	public PreventionReportDto<PNSDetailReportDto> getReport(PreventionFilterDto filter) {
		if (filter != null && filter.getFromDate() != null && filter.getToDate() != null
				&& filter.getFromDate().isBefore(filter.getToDate()) && filter.getOrgIds() != null
				&& filter.getOrgIds().size() > 0) {
			User currentUser = SecurityUtils.getCurrentUser();
			Boolean isAdministrator = SecurityUtils.isUserInRole(currentUser, "ROLE_ADMIN");
			PreventionReportDto<PNSDetailReportDto> ret = new PreventionReportDto<PNSDetailReportDto>();
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

			// Nếu là admin thì không cần xét quyền này
			if (!isAdministrator && orgIds.size() == 0) {
				return null;
			}
			if (orgIds.size() > 0) {
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

//			I	Mô hình đa bậc dịch vụ Thông báo xét nghiệm bạn tình, bạn chích của người có HIV
//			1	Số người nhiễm HIV được giới thiệu về dịch vụ TBXNBT/BC
			PNSDetailReportDto detail = this.getReportDetail(filter, "I.1",
					"Số người nhiễm HIV được giới thiệu về dịch vụ TBXNBT/BC", 1);
			ret.getListDetail().add(detail);
//			2	Số người nhiễm HIV đồng ý tham gia và đã được tư vấn về dịch vụ TBXNBT/BC
			detail = this.getReportDetail(filter, "I.2",
					"Số người nhiễm HIV đồng ý tham gia và đã được tư vấn về dịch vụ TBXNBT/BC", 2);
			ret.getListDetail().add(detail);
//			3	Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm được người nhiễm HIV chia sẻ
			detail = this.getReportDetail(filter, "I.3",
					"Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm được người nhiễm HIV chia sẻ",
					3);
			ret.getListDetail().add(detail);
//			4	Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm được người nhiễm HIV chia sẻ biết tình trạng HIV của mình, trong đó:
			detail = this.getReportDetail(filter, "I.4",
					"Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm được người nhiễm HIV chia sẻ biết tình trạng HIV của mình, trong đó:",
					4);
			ret.getListDetail().add(detail);
//			4 a) Được giới thiệu làm HIV XN HIV và nhận KQXN, trong đó:
			detail = this.getReportDetail(filter, "I.4.a", "Được giới thiệu làm HIV XN HIV và nhận KQXN, trong đó:", 5);
			ret.getListDetail().add(detail);
//			4	a) 1 Nhận KQXN khẳng định HIV dương tính
			detail = this.getReportDetail(filter, "I.4.a.1", "Nhận KQXN khẳng định HIV dương tính", 6);
			ret.getListDetail().add(detail);
//			4	b) Đã có kết quả XN HIV (+) từ trước khi được tiếp cận
			detail = this.getReportDetail(filter, "I.4.b", "Đã có kết quả XN HIV (+) từ trước khi được tiếp cận", 7);
			ret.getListDetail().add(detail);
//			5	Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm biết tình trạng HIV dương tính đã được kết nối thành công đến dịch vụ điều trị ARV
			detail = this.getReportDetail(filter, "I.5",
					"Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm biết tình trạng HIV dương tính đã được kết nối thành công đến dịch vụ điều trị ARV",
					8);
			ret.getListDetail().add(detail);

//			II Xét nghiệm tìm ca - chuyển gửi điều trị cho bạn tình, bạn chích của người có HIV
//			1 Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm của người nhiễm HIV XN HIV và nhận KQXN
			detail = this.getReportDetail(filter, "II.1",
					"Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm của người nhiễm HIV XN HIV và nhận KQXN",
					10);
			ret.getListDetail().add(detail);
//			1.1	Số khách hàng được tính vào báo cáo MER
			detail = this.getReportDetail(filter, "II.1.1", "Số khách hàng được tính vào báo cáo MER", 11);
			ret.getListDetail().add(detail);
//			2 Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm của người nhiễm HIV đã XN HIV và nhận KQXN khẳng định HIV dương tính
			detail = this.getReportDetail(filter, "II.2",
					"Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm của người nhiễm HIV đã XN HIV và nhận KQXN khẳng định HIV dương tính",
					12);
			ret.getListDetail().add(detail);
//			2.1 Số khách hàng được tính vào báo cáo MER
			detail = this.getReportDetail(filter, "II.2.1", "Số khách hàng được tính vào báo cáo MER", 13);
			ret.getListDetail().add(detail);
//			3 Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm biết tình trạng HIV dương tính đã được kết nối thành công đến dịch vụ điều trị ARV
			detail = this.getReportDetail(filter, "II.3",
					"Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm biết tình trạng HIV dương tính đã được kết nối thành công đến dịch vụ điều trị ARV",
					14);
			ret.getListDetail().add(detail);
//			3.1 Số khách hàng được tính vào báo cáo MER
			detail = this.getReportDetail(filter, "II.3.1", "Số khách hàng được tính vào báo cáo MER", 15);
			ret.getListDetail().add(detail);
//			4	Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm có kết quả xét nghiệm HIV âm tính đã kết nối điều trị PrEP thành công
			detail = this.getReportDetail(filter, "II.4",
					"Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm có kết quả xét nghiệm HIV âm tính đã kết nối điều trị PrEP thành công",
					15);
			ret.getListDetail().add(detail);

			return ret;
		}
		return null;
	}

  public PNSDetailReportDto getReportDetail(PreventionFilterDto filter, String rowCode, String rowName, int orderNumber) {
    PNSDetailReportDto detail = new PNSDetailReportDto();
    detail.setSeq(rowCode);
    detail.setTitle(rowName);
    detail.setOrderNumber(orderNumber);

//			Cộng đồng
    Integer community = 0;
    community = this.queryReport(filter, rowCode, 1, orderNumber).intValue();
    detail.setCommunity(community);

//			Tại cơ sở cung cấp dịch vụ
    Integer medicalFacility = 0;
    medicalFacility = this.queryReport(filter, rowCode, 2, orderNumber).intValue();
    detail.setMedicalFacility(medicalFacility);

//			Tại cơ sở khác
    Integer otherMedicalFacility = 0;
    otherMedicalFacility = this.queryReport(filter, rowCode, 3, orderNumber).intValue();
    detail.setOtherMedicalFacility(otherMedicalFacility);

    return detail;
  }

  public Integer queryReport(PreventionFilterDto filter, String row, int col, int orderNumber) {
//		Cộng đồng
    if (col == 1) {
      String SQLOfPE02 = " SELECT COUNT(s.id) from PECase s WHERE s.c1Org.id in (:listOrg) ";
      String whereClauseOfPE02 = "";
// 			Mô hình đa bậc dịch vụ Thông báo xét nghiệm bạn tình, bạn chích của người có HIV
//			1	Số người nhiễm HIV được giới thiệu về dịch vụ TBXNBT/BC
      if (row .equals("I.1")) {
        whereClauseOfPE02 += " AND s.c9Date >= :fromDate AND s.c9Date <= :toDate ";
        whereClauseOfPE02 += " AND c9=:c9Yes ";
      }
//			2 Số người nhiễm HIV đồng ý tham gia và đã được tư vấn về dịch vụ TBXNBT/BC
      if (row .equals("I.2")) {
        whereClauseOfPE02 += " AND s.c9Date >= :fromDate AND s.c9Date <= :toDate ";
        whereClauseOfPE02 += " AND c10=:c10Yes ";
      }
//			3	Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm được người nhiễm HIV chia sẻ
      if (row .equals("I.3")) {
        whereClauseOfPE02 += " AND s.c1 >= :fromDate AND s.c1 <= :toDate ";
        whereClauseOfPE02 += " AND s.parent is not null ";
      }
//			4	Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm được người nhiễm HIV chia sẻ biết tình trạng HIV của mình, trong đó:
      if (row .equals("I.4")) {
        whereClauseOfPE02 += " AND ((s.c11Date >= :fromDate AND s.c11Date <= :toDate AND s.parent is not null) OR (s.c1 >= :fromDate AND s.c1 <= :toDate AND s.c8=:c8answer1 AND s.parent is not null)) ";
      }
//			4 a) Được giới thiệu làm HIV XN HIV và nhận KQXN, trong đó:
      if (row .equals("I.4.a")) {
        whereClauseOfPE02 += " AND s.c11Date >= :fromDate AND s.c11Date <= :toDate";
        whereClauseOfPE02 += " AND s.parent is not null ";
      }
//			4 a) 1) Nhận KQXN khẳng định HIV dương tính
      if (row .equals("I.4.a.1")) {
        whereClauseOfPE02 += " AND s.c11Date >= :fromDate AND s.c11Date <= :toDate";
        whereClauseOfPE02 += " AND s.c131Result=:c131ResultAnswer2 AND s.parent is not null ";
      }
//			4 b) Đã có kết quả XN HIV (+) từ trước khi được tiếp cận
      if (row .equals("I.4.b")) {
        whereClauseOfPE02 += " AND s.c1 >= :fromDate AND s.c1 <= :toDate ";
        whereClauseOfPE02 += " AND s.c8=:c8answer1 AND s.parent is not null ";
      }
//			5 Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm biết tình trạng HIV dương tính đã được kết nối thành công đến dịch vụ điều trị ARV
      if (row .equals("I.5")) {
        whereClauseOfPE02 += " AND s.c15Date >= :fromDate AND s.c15Date <= :toDate ";
        whereClauseOfPE02 += " AND s.parent is not null ";
      }

//			II	Xét nghiệm tìm ca - chuyển gửi điều trị cho bạn tình, bạn chích của người có HIV
//			1		Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm của người nhiễm HIV XN HIV và nhận KQXN
      if (row .equals("II.1")) {
        whereClauseOfPE02 += " AND s.c11Date >= :fromDate AND s.c11Date <= :toDate ";
        whereClauseOfPE02 += " AND (s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer5) OR s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer6)) ";
      }
//			1.1	Số khách hàng được tính vào báo cáo MER
      if (row .equals("II.1.1")) {
        whereClauseOfPE02 += " AND s.c11Date >= :fromDate AND s.c11Date <= :toDate ";
        whereClauseOfPE02 += " AND (s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer5) OR s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer6)) AND ((s.c16 is null OR s.c16!=:c16a2) AND (s.c12=:c12answer1 OR s.c12 is null)) ";
      }
//			2 Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm của người nhiễm HIV đã XN HIV và nhận KQXN khẳng định HIV dương tính
      if (row .equals("II.2")) {
        whereClauseOfPE02 += " AND s.c11Date >= :fromDate AND s.c11Date <= :toDate";
        whereClauseOfPE02 += " AND s.c131Result=:c131ResultAnswer2 AND (s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer5) OR s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer6)) ";
      }
//			2.1 Số khách hàng được tính vào báo cáo MER
      if (row .equals("II.2.1")) {
        whereClauseOfPE02 += " AND s.c11Date >= :fromDate AND s.c11Date <= :toDate";
        whereClauseOfPE02 += " AND s.c131Result=:c131ResultAnswer2 AND (s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer5) OR s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer6)) AND ((s.c16 is null OR s.c16!=:c16a2) AND (s.c12=:c12answer1 OR s.c12 is null)) ";
      }
//			3	Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm biết tình trạng HIV dương tính đã được kết nối thành công đến dịch vụ điều trị ARV
      if (row .equals("II.3")) {
        whereClauseOfPE02 += " AND s.c15Date >= :fromDate AND s.c15Date <= :toDate ";
        whereClauseOfPE02 += " AND (s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer5 ) OR s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer6 )) ";
      }
//			3.1	Số khách hàng được tính vào báo cáo MER
      if (row .equals("II.3.1")) {
        whereClauseOfPE02 += " AND s.c15Date >= :fromDate AND s.c15Date <= :toDate ";
        whereClauseOfPE02 += " AND (s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer5) OR s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer6)) AND ((s.c16 is null OR s.c16 != :c16a2) AND (s.c12=:c12answer1 OR s.c12 is null)) ";
      }
//			4 Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm có kết quả xét nghiệm HIV âm tính đã kết nối điều trị PrEP thành công
      if (row .equals("II.4")) {
        whereClauseOfPE02 += " AND s.c14Date >= :fromDate AND s.c14Date <= :toDate ";
        whereClauseOfPE02 += " AND (s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer5) OR s.id in ( SELECT r.peCase.id FROM PECaseRiskGroup r WHERE r.val=:c6answer6)) ";
      }

      Query qPE = manager.createQuery(SQLOfPE02 + whereClauseOfPE02);
      qPE.setParameter("listOrg", filter.getOrgIds());
      qPE.setParameter("fromDate", filter.getFromDate());
      qPE.setParameter("toDate", filter.getToDate());
//			Mô hình đa bậc dịch vụ Thông báo xét nghiệm bạn tình, bạn chích của người có HIV
//			1	Số người nhiễm HIV được giới thiệu về dịch vụ TBXNBT/BC
      if (row .equals("I.1")) {
        qPE.setParameter("c9Yes", HTSYesNoNone.YES);
      }
//			2 Số người nhiễm HIV đồng ý tham gia và đã được tư vấn về dịch vụ TBXNBT/BC
      if (row .equals("I.2")) {
        qPE.setParameter("c10Yes", HTSYesNoNone.YES);
      }
//			3	Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm được người nhiễm HIV chia sẻ
      if (row .equals("I.3")) {

      }
//			4	Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm được người nhiễm HIV chia sẻ biết tình trạng HIV của mình, trong đó:
      if (row .equals("I.4")) {
        qPE.setParameter("c8answer1", PEC8.answer1);
      }
//			4 a) Được giới thiệu làm HIV XN HIV và nhận KQXN, trong đó:
      if (row .equals("I.4.a")) {

      }
//			4 a) 1) Nhận KQXN khẳng định HIV dương tính
      if (row .equals("I.4.a.1")) {
        qPE.setParameter("c131ResultAnswer2", PEC131Result.answer2);
      }
//			4 b) Đã có kết quả XN HIV (+) từ trước khi được tiếp cận
      if (row .equals("I.4.b")) {
        qPE.setParameter("c8answer1", PEC8.answer1);
      }
//			5 Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm biết tình trạng HIV dương tính đã được kết nối thành công đến dịch vụ điều trị ARV
      if (row .equals("I.5")) {

      }

//			II	Xét nghiệm tìm ca - chuyển gửi điều trị cho bạn tình, bạn chích của người có HIV
//			1		Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm của người nhiễm HIV XN HIV và nhận KQXN
      if (row .equals("II.1")) {
        qPE.setParameter("c6answer5", PERiskGroupEnum.answer5);
        qPE.setParameter("c6answer6", PERiskGroupEnum.answer6);
      }
//			1.1	Số khách hàng được tính vào báo cáo MER
      if (row .equals("II.1.1")) {
        qPE.setParameter("c6answer5", PERiskGroupEnum.answer5);
        qPE.setParameter("c6answer6", PERiskGroupEnum.answer6);
//				qPE.setParameter("c131CodeAnswer3", "3b"));
        qPE.setParameter("c12answer1", PEC12.answer1);
        qPE.setParameter("c16a2", PEC16.answer2);
      }
//			2 Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm của người nhiễm HIV đã XN HIV và nhận KQXN khẳng định HIV dương tính
      if (row .equals("II.2")) {
        qPE.setParameter("c131ResultAnswer2", PEC131Result.answer2);
        qPE.setParameter("c6answer5", PERiskGroupEnum.answer5);
        qPE.setParameter("c6answer6", PERiskGroupEnum.answer6);
      }
//			2.1 Số khách hàng được tính vào báo cáo MER
      if (row .equals("II.2.1")) {
        qPE.setParameter("c6answer5", PERiskGroupEnum.answer5);
        qPE.setParameter("c6answer6", PERiskGroupEnum.answer6);
        qPE.setParameter("c131ResultAnswer2", PEC131Result.answer2);
        qPE.setParameter("c12answer1", PEC12.answer1);
        qPE.setParameter("c16a2", PEC16.answer2);
      }
//			3	Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm biết tình trạng HIV dương tính đã được kết nối thành công đến dịch vụ điều trị ARV
      if (row .equals("II.3")) {
        qPE.setParameter("c6answer5", PERiskGroupEnum.answer5);
        qPE.setParameter("c6answer6", PERiskGroupEnum.answer6);
      }
//			3.1	Số khách hàng được tính vào báo cáo MER
      if (row .equals("II.3.1")) {
        qPE.setParameter("c6answer5", PERiskGroupEnum.answer5);
        qPE.setParameter("c6answer6", PERiskGroupEnum.answer6);
        qPE.setParameter("c12answer1", PEC12.answer1);
        qPE.setParameter("c16a2", PEC16.answer2);
      }
//			4 Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm có kết quả xét nghiệm HIV âm tính đã kết nối điều trị PrEP thành công
      if (row .equals("II.4")) {
        qPE.setParameter("c6answer5", PERiskGroupEnum.answer5);
        qPE.setParameter("c6answer6", PERiskGroupEnum.answer6);
      }
      Long ret = (Long) qPE.getSingleResult();
      if (ret != null) {
        return ret.intValue();
      }
      return 0;
    }

//		Tại cơ sở cung cấp dịch vụ
    if (col == 2) {
      if (orderNumber < 10) {
        if(orderNumber < 3) {
          String SQLOfPNS = " SELECT COUNT(s.id) from PNSCase s WHERE s.c2.id in (:listOrg) ";
          String whereClauseOfPNS = "";
          //			1	Số người nhiễm HIV được giới thiệu về dịch vụ TBXNBT/BC
          if (row .equals("I.1")) {
            whereClauseOfPNS += " AND s.c5 >= :fromDate AND s.c5 <= :toDate ";
          }
          //			2 Số người nhiễm HIV đồng ý tham gia và đã được tư vấn về dịch vụ TBXNBT/BC
          if (row .equals("I.2")) {
            whereClauseOfPNS += " AND s.c6Date >= :fromDate AND s.c6Date <= :toDate ";
          }
          Query qPNS = manager.createQuery(SQLOfPNS + whereClauseOfPNS);
          qPNS.setParameter("listOrg", filter.getOrgIds());
          qPNS.setParameter("fromDate", filter.getFromDate());
          qPNS.setParameter("toDate", filter.getToDate());
          Long ret = (Long) qPNS.getSingleResult();
          if (ret != null) {
            return ret.intValue();
          }
          return 0;
        } else {
          String SQLOfPNSContact = " SELECT COUNT(s.id) from PNSCaseContact s WHERE s.pnsCase.c2.id in (:listOrg) ";
          String whereClauseOfPNSContact = "";
          //			I	Mô hình đa bậc dịch vụ Thông báo xét nghiệm bạn tình, bạn chích của người có HIV
          //			3	Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm được người nhiễm HIV chia sẻ
          if (row .equals("I.3")) {
            whereClauseOfPNSContact += " AND s.c1receivedInfoDate >= :fromDate AND s.c1receivedInfoDate <= :toDate ";
          }
          //			4	Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm được người nhiễm HIV chia sẻ biết tình trạng HIV của mình, trong đó:
          if (row .equals("I.4")) {
            //					whereClauseOfPNSContact += " AND ((s.c8LabtestDate >= :fromDate AND s.c8LabtestDate <= :toDate) OR (s.c1receivedInfoDate >= :fromDate AND s.c1receivedInfoDate <= :toDate)) ";
            //					whereClauseOfPNSContact += " AND (s.c8=:c8answer1 OR s.c8=:c8answer2 OR s.c2=:c2answer1) ";
            whereClauseOfPNSContact += " AND ((s.c8LabtestDate >= :fromDate AND s.c8LabtestDate <= :toDate AND s.c8=:c8answer1) OR (s.c1receivedInfoDate >= :fromDate AND s.c1receivedInfoDate <= :toDate AND s.c2=:c2answer1) OR (s.c8LabtestDate >= :fromDate AND s.c8LabtestDate <= :toDate AND s.c8=:c8answer2)) ";
          }
          //			4 a) Được giới thiệu làm HIV XN HIV và nhận KQXN, trong đó:
          if (row .equals("I.4.a")) {
            whereClauseOfPNSContact += " AND s.c8LabtestDate >= :fromDate AND s.c8LabtestDate <= :toDate";
            whereClauseOfPNSContact += " AND s.c8=:c8answer1 ";
          }
          //			4 a) 1) Nhận KQXN khẳng định HIV dương tính
          if (row .equals("I.4.a.1")) {
            whereClauseOfPNSContact += " AND s.c8LabtestDate >= :fromDate AND s.c8LabtestDate <= :toDate";
            whereClauseOfPNSContact += " AND s.c9=:c9answer2 AND s.c8=:c8answer1 ";
          }
          //			4 b) Đã có kết quả XN HIV (+) từ trước khi được tiếp cận
          if (row .equals("I.4.b")) {
            whereClauseOfPNSContact += " AND s.c1receivedInfoDate >= :fromDate AND s.c1receivedInfoDate <= :toDate ";
            whereClauseOfPNSContact += " AND s.c2=:c2answer1 ";
          }
          //			5 Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm biết tình trạng HIV dương tính đã được kết nối thành công đến dịch vụ điều trị ARV
          if (row .equals("I.5")) {
            whereClauseOfPNSContact += " AND s.c9ARVDate >= :fromDate AND s.c9ARVDate <= :toDate ";
            whereClauseOfPNSContact += " AND s.c8=:c8answer1 ";
          }

          Query qPNSContact = manager.createQuery(SQLOfPNSContact + whereClauseOfPNSContact);
          qPNSContact.setParameter("listOrg", filter.getOrgIds());
          qPNSContact.setParameter("fromDate", filter.getFromDate());
          qPNSContact.setParameter("toDate", filter.getToDate());
          //			I	Mô hình đa bậc dịch vụ Thông báo xét nghiệm bạn tình, bạn chích của người có HIV
          //			3	Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm được người nhiễm HIV chia sẻ
          if (row .equals("I.3")) {

          }
          //			4	Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm được người nhiễm HIV chia sẻ biết tình trạng HIV của mình, trong đó:
          if (row .equals("I.4")) {
            qPNSContact.setParameter("c8answer1", PNSc8.answer1);
            qPNSContact.setParameter("c8answer2", PNSc8.answer2);
            qPNSContact.setParameter("c2answer1", PNSHivStatus.answer1);
          }
          //			4 a) Được giới thiệu làm HIV XN HIV và nhận KQXN, trong đó:
          if (row .equals("I.4.a")) {
            qPNSContact.setParameter("c8answer1", PNSc8.answer1);
          }
          //			4 a) 1) Nhận KQXN khẳng định HIV dương tính
          if (row .equals("I.4.a.1")) {
            qPNSContact.setParameter("c9answer2", PNSc9.answer2);
            qPNSContact.setParameter("c8answer1", PNSc8.answer1);
          }
          //			4 b) Đã có kết quả XN HIV (+) từ trước khi được tiếp cận
          if (row .equals("I.4.b")) {
            qPNSContact.setParameter("c2answer1", PNSHivStatus.answer1);
          }
          //			5 Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm biết tình trạng HIV dương tính đã được kết nối thành công đến dịch vụ điều trị ARV
          if (row .equals("I.5")) {
            qPNSContact.setParameter("c8answer1", PNSc8.answer1);
          }
          Long ret = (Long) qPNSContact.getSingleResult();
          if (ret != null) {
            return ret.intValue();
          }
          return 0;
        }
      } else {
        String SQLOfHTS = " SELECT COUNT(s.id) from HTSCase s WHERE s.c2.id in (:listOrg) ";
        String whereClauseOfHTS = "";
        //			II	Xét nghiệm tìm ca - chuyển gửi điều trị cho bạn tình, bạn chích của người có HIV
        //			1		Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm của người nhiễm HIV XN HIV và nhận KQXN
        if (row .equals("II.1")) {
          whereClauseOfHTS += " AND s.c15Date >= :fromDate AND s.c15Date <= :toDate ";
          whereClauseOfHTS += " AND (s.c10=:c10answer2 OR (s.id in ( SELECT r.htsCase.id FROM HTSCaseRiskGroup r WHERE r.val=:c9answer6 )) OR (s.id in ( SELECT r.htsCase.id FROM HTSCaseRiskGroup r WHERE r.val=:c9answer5 ))) ";
        }
        //			1.1	Số khách hàng được tính vào báo cáo MER
        if (row .equals("II.1.1")) {
          whereClauseOfHTS += " AND s.c15Date >= :fromDate AND s.c15Date <= :toDate ";
          whereClauseOfHTS += " AND (s.c10=:c10answer2 OR (s.id in ( SELECT r.htsCase.id FROM HTSCaseRiskGroup r WHERE r.val=:c9answer6)) OR (s.id in ( SELECT r.htsCase.id FROM HTSCaseRiskGroup r WHERE r.val=:c9answer5))) AND (s.c24 is null OR s.c24 != :c24answer2 AND (s.c11c is null OR s.c11c != :c11cAnswer2 OR s.c11b = :c11bAnswer1)) ";
        }
        //			2 Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm của người nhiễm HIV đã XN HIV và nhận KQXN khẳng định HIV dương tính
        if (row .equals("II.2")) {
          whereClauseOfHTS += " AND s.c15Date >= :fromDate AND s.c15Date <= :toDate ";
          whereClauseOfHTS += " AND s.c14=:c14answer2 AND (s.c10=:c10answer2 OR (s.id in ( SELECT r.htsCase.id FROM HTSCaseRiskGroup r WHERE r.val=:c9answer6)) OR (s.id in ( SELECT r.htsCase.id FROM HTSCaseRiskGroup r WHERE r.val=:c9answer5))) ";
        }
        //			2.1 Số khách hàng được tính vào báo cáo MER
        if (row .equals("II.2.1")) {
          whereClauseOfHTS += " AND s.c15Date >= :fromDate AND s.c15Date <= :toDate ";
          whereClauseOfHTS += " AND s.c14=:c14answer2 AND (s.c10=:c10answer2 OR (s.id in ( SELECT r.htsCase.id FROM HTSCaseRiskGroup r WHERE r.val=:c9answer6)) OR (s.id in ( SELECT r.htsCase.id FROM HTSCaseRiskGroup r WHERE r.val=:c9answer5))) AND (s.c24 is null OR s.c24 != :c24answer2 AND (s.c11c is null OR s.c11c != :c11cAnswer2 OR s.c11b = :c11bAnswer1)) ";
        }
        //			3	Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm biết tình trạng HIV dương tính đã được kết nối thành công đến dịch vụ điều trị ARV
        if (row .equals("II.3")) {
          whereClauseOfHTS += " AND s.c20RegDate >= :fromDate AND s.c20RegDate <= :toDate ";
          whereClauseOfHTS += " AND (s.c10=:c10answer2 OR (s.id in ( SELECT r.htsCase.id FROM HTSCaseRiskGroup r WHERE r.val=:c9answer6)) OR (s.id in ( SELECT r.htsCase.id FROM HTSCaseRiskGroup r WHERE r.val=:c9answer5))) ";
        }
        //			3.1	Số khách hàng được tính vào báo cáo MER
        if (row .equals("II.3.1")) {
          whereClauseOfHTS += " AND s.c20RegDate >= :fromDate AND s.c20RegDate <= :toDate ";
          whereClauseOfHTS += " AND (s.c10=:c10answer2 OR (s.id in ( SELECT r.htsCase.id FROM HTSCaseRiskGroup r WHERE r.val=:c9answer6)) OR (s.id in ( SELECT r.htsCase.id FROM HTSCaseRiskGroup r WHERE r.val=:c9answer5))) AND (s.c24 is null OR s.c24 != :c24answer2 AND (s.c11c is null OR s.c11c != :c11cAnswer2 OR s.c11b = :c11bAnswer1)) ";
        }
        //			4 Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm có kết quả xét nghiệm HIV âm tính đã kết nối điều trị PrEP thành công
        if (row .equals("II.4")) {
          whereClauseOfHTS += " AND s.c1627Date >= :fromDate AND s.c1627Date <= :toDate ";
          whereClauseOfHTS += " AND (s.c10=:c10answer2 OR (s.id in ( SELECT r.htsCase.id FROM HTSCaseRiskGroup r WHERE r.val=:c9answer6)) OR (s.id in ( SELECT r.htsCase.id FROM HTSCaseRiskGroup r WHERE r.val=:c9answer5)))";
        }
        Query qHTS = manager.createQuery(SQLOfHTS + whereClauseOfHTS);
        qHTS.setParameter("listOrg", filter.getOrgIds());
        qHTS.setParameter("fromDate", filter.getFromDate());
        qHTS.setParameter("toDate", filter.getToDate());
//			II	Xét nghiệm tìm ca - chuyển gửi điều trị cho bạn tình, bạn chích của người có HIV
        //			1		Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm của người nhiễm HIV XN HIV và nhận KQXN
        if (row .equals("II.1")) {
          qHTS.setParameter("c10answer2", HTSc10.answer2);
          qHTS.setParameter("c9answer5", HTSRiskGroupEnum.answer5);
          qHTS.setParameter("c9answer6", HTSRiskGroupEnum.answer6);
        }
        //			1.1	Số khách hàng được tính vào báo cáo MER
        if (row .equals("II.1.1")) {
          qHTS.setParameter("c10answer2", HTSc10.answer2);
          qHTS.setParameter("c9answer5", HTSRiskGroupEnum.answer5);
          qHTS.setParameter("c9answer6", HTSRiskGroupEnum.answer6);
          qHTS.setParameter("c24answer2", HTSc24.answer2);
          qHTS.setParameter("c11cAnswer2", HTSYesNoNone.YES);
          qHTS.setParameter("c11bAnswer1", HTSc11b.answer1);
        }
        //			2 Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm của người nhiễm HIV đã XN HIV và nhận KQXN khẳng định HIV dương tính
        if (row .equals("II.2")) {
          qHTS.setParameter("c14answer2", HTSc14.answer2);
          qHTS.setParameter("c10answer2", HTSc10.answer2);
          qHTS.setParameter("c9answer5", HTSRiskGroupEnum.answer5);
          qHTS.setParameter("c9answer6", HTSRiskGroupEnum.answer6);
        }
        //			2.1 Số khách hàng được tính vào báo cáo MER
        if (row .equals("II.2.1")) {
          qHTS.setParameter("c14answer2", HTSc14.answer2);
          qHTS.setParameter("c9answer5", HTSRiskGroupEnum.answer5);
          qHTS.setParameter("c9answer6", HTSRiskGroupEnum.answer6);
          qHTS.setParameter("c24answer2", HTSc24.answer2);
          qHTS.setParameter("c10answer2", HTSc10.answer2);
          qHTS.setParameter("c11cAnswer2", HTSYesNoNone.YES);
          qHTS.setParameter("c11bAnswer1", HTSc11b.answer1);
        }
        //			3	Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm biết tình trạng HIV dương tính đã được kết nối thành công đến dịch vụ điều trị ARV
        if (row .equals("II.3")) {
          qHTS.setParameter("c10answer2", HTSc10.answer2);
          qHTS.setParameter("c9answer5", HTSRiskGroupEnum.answer5);
          qHTS.setParameter("c9answer6", HTSRiskGroupEnum.answer6);
        }
        //			3.1	Số khách hàng được tính vào báo cáo MER
        if (row .equals("II.3.1")) {
          qHTS.setParameter("c10answer2", HTSc10.answer2);
          qHTS.setParameter("c9answer5", HTSRiskGroupEnum.answer5);
          qHTS.setParameter("c9answer6", HTSRiskGroupEnum.answer6);
          qHTS.setParameter("c24answer2", HTSc24.answer2);
          qHTS.setParameter("c11cAnswer2", HTSYesNoNone.YES);
          qHTS.setParameter("c11bAnswer1", HTSc11b.answer1);
        }
        //			4 Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm có kết quả xét nghiệm HIV âm tính đã kết nối điều trị PrEP thành công
        if (row .equals("II.4")) {
          qHTS.setParameter("c10answer2", HTSc10.answer2);
          qHTS.setParameter("c9answer5", HTSRiskGroupEnum.answer5);
          qHTS.setParameter("c9answer6", HTSRiskGroupEnum.answer6);
        }
        Long ret = (Long) qHTS.getSingleResult();
        if (ret != null) {
          return ret.intValue();
        }
        return 0;
      }
    }

//		Tại cơ sở khác
    if (col == 3) {
      String SQLOfPNSContact = " SELECT COUNT(s.id) from PNSCaseContact s WHERE s.pnsCase.c2.id in (:listOrg) ";
      String whereClauseOfPNSContact = "";
// 			Mô hình đa bậc dịch vụ Thông báo xét nghiệm bạn tình, bạn chích của người có HIV
//			4 a) Được giới thiệu làm HIV XN HIV và nhận KQXN, trong đó:
      if (row .equals("I.4.a")) {
        whereClauseOfPNSContact += " AND s.c8LabtestDate >= :fromDate AND s.c8LabtestDate <= :toDate";
        whereClauseOfPNSContact += " AND s.c8=:c8answer2 ";
      }
//			4 a) 1) Nhận KQXN khẳng định HIV dương tính
      if (row .equals("I.4.a.1")) {
        whereClauseOfPNSContact += " AND s.c8LabtestDate >= :fromDate AND s.c8LabtestDate <= :toDate";
        whereClauseOfPNSContact += " AND s.c9=:c9answer2 AND s.c8=:c8answer2 ";
      }
//			5 Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm biết tình trạng HIV dương tính đã được kết nối thành công đến dịch vụ điều trị ARV
      if (row .equals("I.5")) {
        whereClauseOfPNSContact += " AND s.c9ARVDate >= :fromDate AND s.c9ARVDate <= :toDate ";
        whereClauseOfPNSContact += " AND c8=:c8answer2 ";
      }

      Query qPNSContact = manager.createQuery(SQLOfPNSContact + whereClauseOfPNSContact);
      qPNSContact.setParameter("listOrg", filter.getOrgIds());
      if (!whereClauseOfPNSContact.equals("")) {
        qPNSContact.setParameter("fromDate", filter.getFromDate());
        qPNSContact.setParameter("toDate", filter.getToDate());
      }
//			Mô hình đa bậc dịch vụ Thông báo xét nghiệm bạn tình, bạn chích của người có HIV
//			4 a) Được giới thiệu làm HIV XN HIV và nhận KQXN, trong đó:
      if (row .equals("I.4.a")) {
        qPNSContact.setParameter("c8answer2", PNSc8.answer2);
      }
//			4 a) 1) Nhận KQXN khẳng định HIV dương tính
      if (row .equals("I.4.a.1")) {
        qPNSContact.setParameter("c9answer2", PNSc9.answer2);
        qPNSContact.setParameter("c8answer2", PNSc8.answer2);
      }
//			5 Số vợ/chồng/ bạn tình/ bạn chích chung/ con dưới 15 tuổi phơi nhiễm biết tình trạng HIV dương tính đã được kết nối thành công đến dịch vụ điều trị ARV
      if (row .equals("I.5")) {
        qPNSContact.setParameter("c8answer2", PNSc8.answer2);
      }
      Long ret = (Long) qPNSContact.getSingleResult();
      if (ret != null) {
        return ret.intValue();
      }
      return 0;
    }

    return 0;
  }

  @Override
  public PreventionCheckCodeDto checkDuplicateCode(PreventionCheckCodeDto dto) {
    if (dto != null && StringUtils.hasText(dto.getCode()) && dto.getOrgId() != null && dto.getOrgId() > 0L) {
      String SQL = " SELECT COUNT(s.id) FROM PNSCase s WHERE s.c4=:code AND s.c2.id=:orgId ";
      if (dto.getId() != null && dto.getId() > 0L) {
        SQL += " AND s.id!=:id ";
      }
      Query q = manager.createQuery(SQL);
      q.setParameter("code", dto.getCode());
      q.setParameter("orgId", dto.getOrgId());
      if (dto.getId() != null && dto.getId() > 0L) {
        q.setParameter("id", dto.getId());
      }
      Long count = (Long) q.getSingleResult();
      dto.setIsDup(count != null && count > 0);
      if (dto.getIsDup()) {
        dto.setNote("Mã số " + dto.getCode() + " đã bị trùng");
      }
      return dto;
    }
    return null;
  }

  @Override
  public Workbook exportPNSCase(PreventionFilterDto searchDto) {
    Workbook blankBook = new XSSFWorkbook();
    blankBook.createSheet();
    searchDto.setDisablePaging(true);
    Page<PNSCaseDto> pnsCaseDtos = this.findAllPageable(searchDto);
    if (pnsCaseDtos == null) {
      return blankBook;
    } else {
      Workbook wbook = null;
      try (InputStream template = context.getResource("classpath:templates/pns-list.xlsx")
              .getInputStream()) {
        wbook = new XSSFWorkbook(template);
      } catch (IOException e) {
        e.printStackTrace();
      }
      if (wbook == null) {
        return blankBook;
      }
      int rowIndex = 2;
      int rowIndexSheet2 = 2;
      int colIndex = 0;
      int colIndexSheet2 = 0;

      Row row = null;
      Cell cell = null;
      Row rowSheet2 = null;
      Cell cellSheet2 = null;
      Sheet sheet = wbook.getSheetAt(0);
      Sheet sheet2 = wbook.getSheetAt(1);

      int seq = 0;
      int seqSheet2 = 0;
      CellStyle cellStyle = wbook.createCellStyle();
//      ExcelUtils.setBorders4Style(cellStyle);
//      cellStyle.setWrapText(true);
      cellStyle.setAlignment(HorizontalAlignment.LEFT);
      cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
      CellStyle dateTimeStyle = wbook.createCellStyle();
      DataFormat format = wbook.createDataFormat();
//			dateTimeStyle.cloneStyleFrom(templateRow.getCell(0).getCellStyle());
      dateTimeStyle.setDataFormat(format.getFormat("dd/MM/yyyy"));
      dateTimeStyle.setAlignment(HorizontalAlignment.LEFT);
      dateTimeStyle.setVerticalAlignment(VerticalAlignment.CENTER);
//      ExcelUtils.setBorders4Style(dateTimeStyle);

//      if (searchDto.getOrg() != null) {
//        row = sheet.getRow(2);
//        cell = row.createCell(2);
//        cell.setCellValue(searchDto.getOrg().getName());
//      }
//
//      if (searchDto.getFromDate() != null) {
//        row = sheet.getRow(3);
//        cell = row.createCell(2);
//        cell.setCellStyle(dateTimeStyle);
//        cell.setCellValue(Date.from(searchDto.getFromDate().toInstant(ZoneOffset.of("+7"))));
//      }
//
//      if (searchDto.getToDate() != null) {
//        row = sheet.getRow(4);
//        cell = row.createCell(2);
//        cell.setCellStyle(dateTimeStyle);
//        cell.setCellValue(Date.from(searchDto.getToDate().toInstant(ZoneOffset.of("+7"))));
//      }

      for (PNSCaseDto htsCaseDto : pnsCaseDtos) {
        row = sheet.createRow(rowIndex++);

        //STT
        cell = row.createCell(colIndex++);
        cell.setCellValue(seq += 1);
        cell.setCellStyle(cellStyle);

        //Ho ten tu van vien
        cell = row.createCell(colIndex++);
        try {
          cell.setCellValue(htsCaseDto.getC3().getFullName());
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        //Ho ten khach hang
        cell = row.createCell(colIndex++);
        try {
          cell.setCellValue(htsCaseDto.getC7());
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        //gioi tinh
        cell = row.createCell(colIndex++);
        try {
          if (htsCaseDto.getC8().name().equals("MALE")) {
            cell.setCellValue("Nam");
          }
          if (htsCaseDto.getC8().name().equals("FEMALE")) {
            cell.setCellValue("Nữ");
          }
          if (htsCaseDto.getC8().name().equals("OTHER")) {
            cell.setCellValue("Khác");
          }
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        //Năm sinh
        cell = row.createCell(colIndex++);
        try {
          cell.setCellValue(htsCaseDto.getC9());
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        //Ngày tư vấn dịch vụ
        cell = row.createCell(colIndex++);
        cell.setCellStyle(dateTimeStyle);
        try {
          cell.setCellValue(Date.from(htsCaseDto.getC5().toInstant(ZoneOffset.of("+7"))));
        } catch (Exception e) {
          cell.setCellValue("");
        }

        // đồng ý nhận dịch vụ TBXNBT/BC?
        cell = row.createCell(colIndex++);
        try {
          if (htsCaseDto.getC6().name().equals("YES")) {
            cell.setCellValue("Có");
          }
          if (htsCaseDto.getC6().name().equals("NO")) {
            cell.setCellValue("Không");
          }
          if (htsCaseDto.getC6().name().equals("NO_INFORMATION")) {
            cell.setCellValue("Không có thông tin");
          }
          //cell.setCellValue(htsCaseDto.getC6().name());
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        //Ngày đồng ý nhận dịch vụ
        cell = row.createCell(colIndex++);
        cell.setCellStyle(dateTimeStyle);
        try {
          cell.setCellValue(Date.from(htsCaseDto.getC6Date().toInstant(ZoneOffset.of("+7"))));
        } catch (Exception e) {
          cell.setCellValue("");
        }

        //Ngày XN khẳng định HIV+
        cell = row.createCell(colIndex++);
        cell.setCellStyle(dateTimeStyle);
        try {
          cell.setCellValue(Date.from(htsCaseDto.getC10().toInstant(ZoneOffset.of("+7"))));
        } catch (Exception e) {
          cell.setCellValue("");
        }

        //Tình trạng điều trị HIV
        cell = row.createCell(colIndex++);
        try {
          cell.setCellValue(htsCaseDto.getC11Des());
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        //mã số khách hàng
        cell = row.createCell(colIndex++);
        try {
          cell.setCellValue(htsCaseDto.getC4());
        } catch (Exception e) {
          cell.setCellValue("");
        }
        cell.setCellStyle(cellStyle);

        colIndex = 0;
      }

      for (PNSCaseDto pnsCaseDto : pnsCaseDtos) {
        for (PNSCaseContactDto pnsCaseContactDto : pnsCaseDto.getContacts()) {
          rowSheet2 = sheet2.createRow(rowIndexSheet2++);
          //STT
          cellSheet2 = rowSheet2.createCell(colIndexSheet2++);
          cellSheet2.setCellValue(seqSheet2 += 1);
          cellSheet2.setCellStyle(cellStyle);

          //tỉnh thành org
          cellSheet2 = rowSheet2.createCell(colIndexSheet2++);
          try {
            cellSheet2.setCellValue(pnsCaseDto.getC2().getAddress().getProvince().getName());
          } catch (Exception e) {
            cellSheet2.setCellValue("");
          }
          cellSheet2.setCellStyle(cellStyle);
          
          cellSheet2 = rowSheet2.createCell(colIndexSheet2++);
          try {
            cellSheet2.setCellValue(pnsCaseDto.getC2().getName());
          } catch (Exception e) {
            cellSheet2.setCellValue("");
          }
          cellSheet2.setCellStyle(cellStyle);
          
          
          //Ho ten khach hang co HIV
          cellSheet2 = rowSheet2.createCell(colIndexSheet2++);
          try {
            cellSheet2.setCellValue(pnsCaseDto.getC7());
          } catch (Exception e) {
            cellSheet2.setCellValue("");
          }
          cellSheet2.setCellStyle(cellStyle);

          //Mã khach hang co HIV
          cellSheet2 = rowSheet2.createCell(colIndexSheet2++);
          try {
            cellSheet2.setCellValue(pnsCaseDto.getC4());
          } catch (Exception e) {
            cellSheet2.setCellValue("");
          }
          cellSheet2.setCellStyle(cellStyle);

          //Ngay khai thac thong tin
          cellSheet2 = rowSheet2.createCell(colIndexSheet2++);
          cellSheet2.setCellStyle(dateTimeStyle);
          try {
            cellSheet2.setCellValue(Date.from(pnsCaseContactDto.getC1receivedInfoDate().toInstant(ZoneOffset.of("+7"))));
          } catch (Exception e) {
            cellSheet2.setCellValue("");
          }

          //Ho ten khach hang BT/BC
          cellSheet2 = rowSheet2.createCell(colIndexSheet2++);
          try {
            cellSheet2.setCellValue(pnsCaseContactDto.getFullName());
          } catch (Exception e) {
            cellSheet2.setCellValue("");
          }
          cellSheet2.setCellStyle(cellStyle);

          //gioi tinh
          cellSheet2 = rowSheet2.createCell(colIndexSheet2++);
          try {
            if (pnsCaseContactDto.getGender().name().equals("MALE")) {
              cellSheet2.setCellValue("Nam");
            }
            if (pnsCaseContactDto.getGender().name().equals("FEMALE")) {
              cellSheet2.setCellValue("Nữ");
            }
            if (pnsCaseContactDto.getGender().name().equals("OTHER")) {
              cellSheet2.setCellValue("Khác");
            }
          } catch (Exception e) {
            cellSheet2.setCellValue("");
          }
          cellSheet2.setCellStyle(cellStyle);

          //Năm sinh
          cellSheet2 = rowSheet2.createCell(colIndexSheet2++);
          try {
            cellSheet2.setCellValue(pnsCaseContactDto.getYearOfBirth());
          } catch (Exception e) {
            cellSheet2.setCellValue("");
          }
          cellSheet2.setCellStyle(cellStyle);

          //Dia chi noi cu tru
          cellSheet2 = rowSheet2.createCell(colIndexSheet2++);
          try {
            cellSheet2.setCellValue(pnsCaseContactDto.getAddressDetail());
          } catch (Exception e) {
            cellSheet2.setCellValue("");
          }
          cellSheet2.setCellStyle(cellStyle);

          //Quan/huyen
          cellSheet2 = rowSheet2.createCell(colIndexSheet2++);
          try {
            cellSheet2.setCellValue(pnsCaseContactDto.getDistrict().getName());
          } catch (Exception e) {
            cellSheet2.setCellValue("");
          }
          cellSheet2.setCellStyle(cellStyle);

          //Tinh/thanh pho
          cellSheet2 = rowSheet2.createCell(colIndexSheet2++);
          try {
            cellSheet2.setCellValue(pnsCaseContactDto.getProvince().getName());
          } catch (Exception e) {
            cellSheet2.setCellValue("");
          }
          cellSheet2.setCellStyle(cellStyle);

          //So dien thoai
          cellSheet2 = rowSheet2.createCell(colIndexSheet2++);
          try {
            cellSheet2.setCellValue(pnsCaseContactDto.getPhoneNumber());
          } catch (Exception e) {
            cellSheet2.setCellValue("");
          }
          cellSheet2.setCellStyle(cellStyle);

          //Quan he voi nguoi co HIV
          cellSheet2 = rowSheet2.createCell(colIndexSheet2++);
          try {
            for (PNSCaseContactRelationshipDto dto : pnsCaseContactDto.getC1()) {
              cellSheet2.setCellValue(dto.getName());
            }
          } catch (Exception e) {
            cellSheet2.setCellValue("");
          }
          cellSheet2.setCellStyle(cellStyle);

          //Tinh trang HIV
          cellSheet2 = rowSheet2.createCell(colIndexSheet2++);
          try {
            cellSheet2.setCellValue(pnsCaseContactDto.getC2Des());
          } catch (Exception e) {
            cellSheet2.setCellValue("");
          }
          cellSheet2.setCellStyle(cellStyle);

          //Nguy co bao luc
          cellSheet2 = rowSheet2.createCell(colIndexSheet2++);
          try {
            cellSheet2.setCellValue(pnsCaseContactDto.getC3Des());
          } catch (Exception e) {
            cellSheet2.setCellValue("");
          }
          cellSheet2.setCellStyle(cellStyle);

          //Lien lac lan 1
          cellSheet2 = rowSheet2.createCell(colIndexSheet2++);
          cellSheet2.setCellStyle(dateTimeStyle);

          try {
            cellSheet2.setCellValue(Date.from(pnsCaseContactDto.getC4First().toInstant(ZoneOffset.of("+7"))));
          } catch (Exception e) {
            cellSheet2.setCellValue("");
          }

          //Lien lac lan 2
          cellSheet2 = rowSheet2.createCell(colIndexSheet2++);
          cellSheet2.setCellStyle(dateTimeStyle);
          try {
            cellSheet2.setCellValue(Date.from(pnsCaseContactDto.getC4Second().toInstant(ZoneOffset.of("+7"))));
          } catch (Exception e) {
            cellSheet2.setCellValue("");
          }

          //Lien lac lan 3
          cellSheet2 = rowSheet2.createCell(colIndexSheet2++);
          cellSheet2.setCellStyle(dateTimeStyle);
          try {
            cellSheet2.setCellValue(Date.from(pnsCaseContactDto.getC4Third().toInstant(ZoneOffset.of("+7"))));
          } catch (Exception e) {
            cellSheet2.setCellValue("");
          }

          //Da lien lac duoc
          cellSheet2 = rowSheet2.createCell(colIndexSheet2++);
          try {
            cellSheet2.setCellValue(pnsCaseContactDto.getC5Des());
          } catch (Exception e) {
            cellSheet2.setCellValue("");
          }
          cellSheet2.setCellStyle(cellStyle);

          //Ly do khong lien lac duoc
          cellSheet2 = rowSheet2.createCell(colIndexSheet2++);
          try {
            cellSheet2.setCellValue(pnsCaseContactDto.getC5ReasonDes());
          } catch (Exception e) {
            cellSheet2.setCellValue("");
          }
          cellSheet2.setCellStyle(cellStyle);

          //Bien phap lien lac
          cellSheet2 = rowSheet2.createCell(colIndexSheet2++);
          try {
            cellSheet2.setCellValue(pnsCaseContactDto.getC6Des());
          } catch (Exception e) {
            cellSheet2.setCellValue("");
          }
          cellSheet2.setCellStyle(cellStyle);

          //Cach lien lac thanh cong
          cellSheet2 = rowSheet2.createCell(colIndexSheet2++);
          try {
            cellSheet2.setCellValue(pnsCaseContactDto.getC7Des());
          } catch (Exception e) {
            cellSheet2.setCellValue("");
          }
          cellSheet2.setCellStyle(cellStyle);

          //Cach lien lac khac
          cellSheet2 = rowSheet2.createCell(colIndexSheet2++);
          try {
            cellSheet2.setCellValue(pnsCaseContactDto.getC7Note());
          } catch (Exception e) {
            cellSheet2.setCellValue("");
          }
          cellSheet2.setCellStyle(cellStyle);

          //Xét nghiệm HIV
          cellSheet2 = rowSheet2.createCell(colIndexSheet2++);
          try {
            cellSheet2.setCellValue(pnsCaseContactDto.getC8Des());
          } catch (Exception e) {
            cellSheet2.setCellValue("");
          }
          cellSheet2.setCellStyle(cellStyle);

          //Ngay xet nghiem HIV
          cellSheet2 = rowSheet2.createCell(colIndexSheet2++);
          cellSheet2.setCellStyle(dateTimeStyle);
          try {
            cellSheet2.setCellValue(Date.from(pnsCaseContactDto.getC8LabtestDate().toInstant(ZoneOffset.of("+7"))));
          } catch (Exception e) {
            cellSheet2.setCellValue("");
          }

          //Ket qua xét nghiệm HIV
          cellSheet2 = rowSheet2.createCell(colIndexSheet2++);
          try {
            cellSheet2.setCellValue(pnsCaseContactDto.getC9Des());
          } catch (Exception e) {
            cellSheet2.setCellValue("");
          }
          cellSheet2.setCellStyle(cellStyle);

          //Ten co so XN HIV
          cellSheet2 = rowSheet2.createCell(colIndexSheet2++);
          try {
            cellSheet2.setCellValue(pnsCaseContactDto.getC8LabtestOrg());
          } catch (Exception e) {
            cellSheet2.setCellValue("");
          }
          cellSheet2.setCellStyle(cellStyle);

          //Dieu tri PrEP
          cellSheet2 = rowSheet2.createCell(colIndexSheet2++);
          try {
            cellSheet2.setCellValue(pnsCaseContactDto.getC9JoinedPrEPDes());
          } catch (Exception e) {
            cellSheet2.setCellValue("");
          }
          cellSheet2.setCellStyle(cellStyle);

          //Ngay dieu tri PrEP
          cellSheet2 = rowSheet2.createCell(colIndexSheet2++);
          cellSheet2.setCellStyle(dateTimeStyle);
          try {
            cellSheet2.setCellValue(Date.from(pnsCaseContactDto.getC9PrEPDate().toInstant(ZoneOffset.of("+7"))));
          } catch (Exception e) {
            cellSheet2.setCellValue("");
          }

          //Dieu tri ARV
          cellSheet2 = rowSheet2.createCell(colIndexSheet2++);
          try {
            cellSheet2.setCellValue(pnsCaseContactDto.getC9JoinedARVDes());
          } catch (Exception e) {
            cellSheet2.setCellValue("");
          }
          cellSheet2.setCellStyle(cellStyle);

          //Ngay dieu tri ARV
          cellSheet2 = rowSheet2.createCell(colIndexSheet2++);
          cellSheet2.setCellStyle(dateTimeStyle);
          try {
            cellSheet2.setCellValue(Date.from(pnsCaseContactDto.getC9ARVDate().toInstant(ZoneOffset.of("+7"))));
          } catch (Exception e) {
            cellSheet2.setCellValue("");
          }

          colIndexSheet2 = 0;
        }
      }
      return wbook;
    }
  }

  @Override
  public ResponseDto<PNSCaseDto> checkHTS(PreventionCheckCodeDto dto) {
    User currentUser = SecurityUtils.getCurrentUser();
    Boolean isSiteManagement = SecurityUtils.isUserInRole(currentUser, "ROLE_SITE_MANAGER");
    if(!isSiteManagement) {
      isSiteManagement = SecurityUtils.isUserInRole(currentUser, "ROLE_PROVINCIAL_MANAGER");
    }
    ResponseDto<PNSCaseDto> ret = new ResponseDto<PNSCaseDto>();
    if (dto != null) {
      String SQL = "";
      if (isSiteManagement) {
        SQL = " SELECT new org.pepfar.pdma.app.data.dto.PNSCaseDto(s, 1, true, false, false) from PNSCase s WHERE 1=1  ";
      } else {
        SQL = " SELECT new org.pepfar.pdma.app.data.dto.PNSCaseDto(s, 0, true, false, false) from PNSCase s WHERE 1=1  ";
      }
      String whereClause = " ";
      if (dto.getIdHTS() != null && dto.getType() == 1) {
        whereClause += " AND s.htsCase.id= :idHTS ";
      }
      if (dto.getIdOPC() != null && dto.getType() == 2) {
        whereClause += " AND s.hivCase.id= :idOPC ";
      }
      Query q = manager.createQuery(SQL + whereClause);
      if (dto.getIdHTS() != null && dto.getType() == 1) {
        q.setParameter("idHTS", dto.getIdHTS());
      }
      if (dto.getIdOPC() != null && dto.getType() == 2) {
        q.setParameter("idOPC", dto.getIdOPC());
      }

      @SuppressWarnings("unchecked")
      List<PNSCaseDto> entities = q.getResultList();
      if (entities.size() > 0) {
        ret.setResponseObject(entities.get(0));
        ret.setCode(YesNoNone.YES);
        ret.setMessage("Đã tồn tại PNS");
      } else {
        ret.setResponseObject(null);
        ret.setCode(YesNoNone.NO);
        ret.setMessage("Chưa tồn tại PNS");
      }
    }
    return ret;

  }

  @Override
  public ImportResultDto<PNSCaseDto> importFromExcel(InputStream is) throws IOException {
    Workbook workbook = new XSSFWorkbook(is);
    Sheet datatypeSheet = workbook.getSheetAt(0);
    // Iterator<Row> iterator = datatypeSheet.iterator();
    int rowIndex = 1;
    int num = datatypeSheet.getLastRowNum();
    ImportResultDto<PNSCaseDto> ret = new ImportResultDto<PNSCaseDto>();
    while (rowIndex <= num) {
      try {
        System.out.println(rowIndex);
        Row currentRow = datatypeSheet.getRow(rowIndex);
        Cell currentCell = null;
        if (currentRow != null) {
          PNSCaseDto dto = new PNSCaseDto();
          String err = "";
          String c1Str=null;
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
          } catch (Exception e) {
            dto.setUid(null);
            err += "C1 - Không rõ; ";
          }

          if(c1Str==null || StringUtils.isEmpty(c1Str)) {
            rowIndex+=1;
            continue;
          }
          try {
// 						c2 - c2_org_id
            currentCell = currentRow.getCell(1);
            if (currentCell != null) {
              String orgCode = "";
              if (currentCell.getCellType() == CellType.STRING) {
                orgCode = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                orgCode = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              if(!orgCode.equals("")) {
                Organization organization = organizationRepository.findByOrgCode(orgCode);
                if(organization != null) {
                  dto.setC2(new OrganizationDto(organization));
                } else {
                  dto.setC2(null);
                  err += "C2 - Không tìm thấy cơ sở báo cáo; ";
                }
              } else {
                dto.setC2(null);
              }
            }
          } catch (Exception e) {
            dto.setC2(null);
            err += "C2 - Không rõ; ";
          }
          try {
//						c3 - c3_staff_id
            currentCell = currentRow.getCell(2);
            if (currentCell != null) {
              String staffCode = "";
              if (currentCell.getCellType() == CellType.STRING) {
                staffCode = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                staffCode = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              if(!staffCode.equals("")) {
                Staff entity = staffRepository.findByStaffCode(staffCode);
                if (entity != null) {
                  dto.setC3(new StaffDto(entity, true));
                } else {
                  dto.setC3(null);
                  err += "C3 - Không tìm thấy nhân viên; ";
                }
              } else {
                dto.setC3(null);
              }
            }
          } catch (Exception e) {
            dto.setC3(null);
            err += "C3 - Không rõ; ";
          }
          try {
//						c1 - case_id
            currentCell = currentRow.getCell(0);
            if (currentCell != null) {
              if (currentCell.getCellType() == CellType.STRING) {
                UUID uid = UUID.fromString(currentCell.getStringCellValue());
                if(uid != null) {
                  Case c = new Case();
                  if(c != null) {
                    c = caseRepository.findOneByUID(uid);
                    dto.setHivCase(new CaseDto(c));
                  } else {
                    dto.setHivCase(null);
                    err += "C1 - Không tìm thấy; ";
                  }
                } else {
                  dto.setHivCase(null);
                }
              }
            }
          } catch (Exception e) {
            dto.setHivCase(null);
            err += "C1 - Không rõ; ";
          }
          try {
//						c4 - c4_client_code
            currentCell = currentRow.getCell(3);
            if (currentCell != null) {
              String c4ClientCode = "";
              if (currentCell.getCellType() == CellType.STRING) {
                c4ClientCode = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c4ClientCode = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              dto.setC4(c4ClientCode);
            }
          } catch (Exception e) {
            dto.setC4(null);
            err += "C4 - Không rõ; ";
          }
          try {
//						c5 - c5_date_counselling
            currentCell = currentRow.getCell(4);
            if (currentCell != null) {
              LocalDateTime c5DateCounselling = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                c5DateCounselling = LocalDateTime.parse(currentCell.getStringCellValue(), formatter);
              } else if (currentCell.getCellType() == CellType.NUMERIC
                      && currentCell.getDateCellValue() != null) {
                c5DateCounselling = currentCell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
              }
              dto.setC5(c5DateCounselling);
            }
          } catch (Exception e) {
            dto.setC5(null);
            err += "C5 - Không rõ; ";
          }
          try {
//						c6 - c6_accept_service
            currentCell = currentRow.getCell(5);
            if (currentCell != null) {
              String c6AcceptService = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c6AcceptService = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c6AcceptService = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              if(c6AcceptService != null) {
                if (c6AcceptService.equals("4")){
                  dto.setC6(HTSYesNoNone.YES);
                } else if (c6AcceptService.equals("3")) {
                  dto.setC6(HTSYesNoNone.NO);
                } else {
                  dto.setC6(null);
                  err += "C6 không đúng (C6 phải bằng 3 hoặc 4); ";
                }
              } else {
                dto.setC6(null);
              }
            }
          } catch (Exception e) {
            dto.setC6(null);
            err += "C6 - Không rõ; ";
          }
          try {
//						c7 - c6_date_service
            currentCell = currentRow.getCell(6);
            if (currentCell != null) {
              LocalDateTime c6DateService = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                c6DateService = LocalDateTime.parse(currentCell.getStringCellValue(), formatter);
              } else if (currentCell.getCellType() == CellType.NUMERIC
                      && currentCell.getDateCellValue() != null) {
                c6DateService = currentCell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
              }
              dto.setC6Date(c6DateService);
            }
          } catch (Exception e) {
            dto.setC6Date(null);
            err += "C7 - Không rõ; ";
          }
          try {
//						c8 - c7
            currentCell = currentRow.getCell(7);
            if (currentCell != null) {
              String c7 = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c7 = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c7 = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              dto.setC7(c7);
            }
          } catch (Exception e) {
            dto.setC7(null);
            err += "C8 - Không rõ; ";
          }
          try {
//						c9 - c8_gender
            currentCell = currentRow.getCell(8);
            if (currentCell != null) {
              String c8Gender = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c8Gender = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c8Gender = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              if(c8Gender != null) {
                if (c8Gender.equals("6")) {
                  dto.setC8(Gender.MALE);
                } else if (c8Gender.equals("7")) {
                  dto.setC8(Gender.FEMALE);
                } else {
                  dto.setC8(null);
                  err += "C9 không đúng (C9 phải bằng 6 hoặc 7); ";
                }
              } else {
                dto.setC8(null);
              }
            }
          } catch (Exception e) {
            dto.setC8(null);
            err += "C9 - Không rõ; ";
          }
          try {
//						c10 - c6_dob
            currentCell = currentRow.getCell(9);
            if (currentCell != null) {
              Integer c6Dob = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c6Dob = Integer.valueOf(currentCell.getStringCellValue());
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c6Dob = Double.valueOf(currentCell.getNumericCellValue()).intValue();
              }
              dto.setC9(c6Dob);
            }
          } catch (Exception e) {
            dto.setC9(null);
            err += "C10 - Không rõ; ";
          }
          try {
//						c11 - c10_hiv_confirm_date
            currentCell = currentRow.getCell(10);
            if (currentCell != null) {
              LocalDateTime c10HivConfirmDate = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                c10HivConfirmDate = LocalDateTime.parse(currentCell.getStringCellValue(), formatter);
              } else if (currentCell.getCellType() == CellType.NUMERIC
                      && currentCell.getDateCellValue() != null) {
                c10HivConfirmDate = currentCell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
              }
              dto.setC10(c10HivConfirmDate);
            }
          } catch (Exception e) {
            dto.setC10(null);
            err += "C11 - Không rõ; ";
          }
          try {
//						c12 - c11_arv_treatment_status
            currentCell = currentRow.getCell(11);
            if (currentCell != null) {
              String c11ArvTreatmentStatus = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c11ArvTreatmentStatus = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c11ArvTreatmentStatus = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              if (c11ArvTreatmentStatus != null) {
                if (c11ArvTreatmentStatus.equals("157")) {
                  dto.setC11(PNSc11.answer1);
                } else if (c11ArvTreatmentStatus.equals("158")) {
                  dto.setC11(PNSc11.answer2);
                } else if (c11ArvTreatmentStatus.equals("159")) {
                  dto.setC11(PNSc11.answer3);
                } else if (c11ArvTreatmentStatus.equals("160")) {
                  dto.setC11(PNSc11.answer4);
                } else if (c11ArvTreatmentStatus.equals("161")) {
                  dto.setC11(PNSc11.answer5);
                } else if (c11ArvTreatmentStatus.equals("162")) {
                  dto.setC11(PNSc11.answer6);
                } else if (c11ArvTreatmentStatus.equals("163")) {
                  dto.setC11(PNSc11.answer7);
                } else {
                  dto.setC11(null);
                  err += "C12 không đúng (C12 phải lớn hơn 156 và nhỏ hơn 164); ";
                }
              } else {
                dto.setC11(null);
              }
            }
          } catch (Exception e) {
            dto.setC11(null);
            err += "C12 - Không rõ; ";
          }

//					c12_note
//					currentCell = currentRow.getCell(12);
//					if (currentCell != null) {
//						String c12Note = null;
//						if (currentCell.getCellType() == CellType.STRING
//										&& StringUtils.hasText(currentCell.getStringCellValue())) {
//							c12Note = currentCell.getStringCellValue();
//						} else if (currentCell.getCellType() == CellType.NUMERIC) {
//							c12Note = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
//						}
//						dto.setC12Note(c12Note);
//					}
          try {
//					c13 - val
            currentCell = currentRow.getCell(12);
            if (currentCell != null) {
              String c12RiskGroups = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c12RiskGroups = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c12RiskGroups = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              List<PNSCaseRiskGroupDto> c12 = new ArrayList<PNSCaseRiskGroupDto>();
              String[] risks = c12RiskGroups.split("\\|");
              for (String ri : risks) {
                PNSCaseRiskGroupDto r = new PNSCaseRiskGroupDto();
                if (ri != null) {
                  if (ri.equals("8")) {
                    r.setVal(HTSRiskGroupEnum.answer1);
                  } else if (ri.equals("9")) {
                    r.setVal(HTSRiskGroupEnum.answer2);
                  } else if (ri.equals("10")) {
                    r.setVal(HTSRiskGroupEnum.answer3);
                  } else if (ri.equals("11")) {
                    r.setVal(HTSRiskGroupEnum.answer4);
                  } else if (ri.equals("12")) {
                    r.setVal(HTSRiskGroupEnum.answer5);
                  } else if (ri.equals("13")) {
                    r.setVal(HTSRiskGroupEnum.answer6);
                  } else if (ri.equals("14")) {
                    r.setVal(HTSRiskGroupEnum.answer7);
                  } else if (ri.equals("15")) {
                    r.setVal(HTSRiskGroupEnum.answer8);
                  } else if (ri.equals("16")) {
                    r.setVal(HTSRiskGroupEnum.answer9);
                  } else if (ri.equals("17")) {
                    r.setVal(HTSRiskGroupEnum.answer10);
                  } else if (ri.equals("18")) {
                    r.setVal(HTSRiskGroupEnum.answer11);
                  } else if (ri.equals("19")) {
                    r.setVal(HTSRiskGroupEnum.answer12);
                  } else if (ri.equals("20")) {
                    r.setVal(HTSRiskGroupEnum.answer13);
                  } else if (ri.equals("21")) {
                    r.setVal(HTSRiskGroupEnum.answer14);
                  } else if (ri.equals("22")) {
                    r.setVal(HTSRiskGroupEnum.answer15);
                  } else if (ri.equals("23")) {
                    r.setVal(HTSRiskGroupEnum.answer16);
                  } else {
                    r.setVal(null);
                    err += "C13 không đúng (C13 phải lớn hơn 7 và nhỏ hơn 24); ";
                  }
                } else {
                  r.setVal(null);
                }
                r.setName(r.getVal().getDescription());
                c12.add(r);
              }
              dto.setC12(c12);
            }
          } catch (Exception e) {
            dto.setC12(null);
            err += "C13 - Không rõ; ";
          }

//          try {
//	          saveOrUpdate(dto);
//          } catch (Exception e) {
//	          dto.setErrorContent(e.getMessage());
//	          dto.setNumberErrorContent(rowIndex + 1);
////	          ret.setTotalErr(ret.getTotalErr() + 1);
////	          ret.getListErr().add(dto);
//          }
//          if (dto.getErrorContent() != null) {
//            dto.setNumberErrorContent(rowIndex + 1);
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
//                dto.setErrorContent("Lưu thất bại");
//                dto.setNumberErrorContent(rowIndex + 1);
//                ret.setTotalErr(ret.getTotalErr() + 1);
//                ret.getListErr().add(dto);
//              } else {
//                dto.setErrorContent(err+"Lưu thất bại");
//              }
//            }
//          } catch (Exception e) {
//            dto.setSaved(false);
//            if (dto.getErrorContent().equals("")) {
//              dto.setErrorContent("Lưu thất bại");
//              dto.setNumberErrorContent(rowIndex + 1);
//              ret.setTotalErr(ret.getTotalErr() + 1);
//              ret.getListErr().add(dto);
//            } else {
//              dto.setErrorContent(err+"Lưu thất bại");
//            }
//          }
          try {
            dto = saveOrUpdate(dto);
            dto.setSaved(true);
          } catch (Exception e) {
            ret.setTotalErr(ret.getTotalErr() + 1);
            err += "Lưu thất bại:"+CommonUtils.getStackTrace(e);
            dto.setSaved(false);
          }
          dto.setErrorContent(err);
          if (!dto.getErrorContent().equals("")) {
            dto.setNumberErrorContent(rowIndex + 1);
            ret.setTotalErr(ret.getTotalErr() + 1);
            ret.getListErr().add(dto);
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
  public List<OrganizationDto> getListPNSWriteAble() {
    User currentUser = SecurityUtils.getCurrentUser();
    if (currentUser == null) {
      return null;
    }
    List<OrganizationDto> ret = new ArrayList<OrganizationDto>();
    List<UserOrganization> listUO = userOrganizationRepository.getListByUserId(currentUser.getId());
    for (UserOrganization userOrganization : listUO) {
      if (userOrganization.getWriteAccess() != null && userOrganization.getWriteAccess() &&
          userOrganization.getPnsRole()!=null && userOrganization.getPnsRole()) {
        ret.add(new OrganizationDto(userOrganization.getOrganization()));
      }
    }
    return ret;
  }

  private PNSCaseDto newPNSCaseDto(PNSCase entity, boolean isSite, boolean isProvince,
                                   boolean isAdministrator, List<UserOrganization> listUO) {
    if (entity != null) {
      // Chỉ Quyền cơ sở và tỉnh có quyền xem chi tiết
      Boolean isViewPII = isSite || isProvince;
      // Nếu là admin thì mặc định có quyền sửa - xóa - xem, nếu không thì cũng sẽ =
      // false và xét ở bước kế tiếp
      boolean isReadAble = isAdministrator;
      boolean isWritAble = isAdministrator;
      boolean isDeleteAble = isAdministrator;
      if (!isAdministrator) {
        if (listUO != null && listUO.size() > 0) {
          for (UserOrganization userOrganization : listUO) {
            if (userOrganization.getOrganization() != null && entity.getC2() != null
                && userOrganization.getOrganization().getId().equals(entity.getC2().getId())
                && userOrganization.getPnsRole() != null && userOrganization.getPnsRole()) {
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
      if(isViewPII) {
        return new PNSCaseDto(entity, 1, isReadAble, isWritAble, isDeleteAble);
      } else {
        return new PNSCaseDto(entity, 0, isReadAble, isWritAble, isDeleteAble);
      }
    } else {
      return null;
    }
  }
}
