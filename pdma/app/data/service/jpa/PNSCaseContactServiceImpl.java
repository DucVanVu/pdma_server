package org.pepfar.pdma.app.data.service.jpa;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pepfar.pdma.app.data.domain.*;
import org.pepfar.pdma.app.data.dto.*;
import org.pepfar.pdma.app.data.repository.AdminUnitRepository;
import org.pepfar.pdma.app.data.repository.HTSCaseRespository;
import org.pepfar.pdma.app.data.repository.PNSCaseContactRelationshipRespository;
import org.pepfar.pdma.app.data.repository.PNSCaseContactRespository;
import org.pepfar.pdma.app.data.repository.PNSCaseRespository;
import org.pepfar.pdma.app.data.repository.PersonRepository;
import org.pepfar.pdma.app.data.repository.UserOrganizationRepository;
import org.pepfar.pdma.app.data.service.PNSCaseContactService;
import org.pepfar.pdma.app.data.types.*;
import org.pepfar.pdma.app.data.types.Permission;
import org.pepfar.pdma.app.utils.AuthorizationUtils;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service("PNSCaseContactServiceImpl")
public class PNSCaseContactServiceImpl implements PNSCaseContactService {

  @Autowired
  public EntityManager manager;

  @Autowired
  private AuthorizationUtils authUtils;

  @Autowired
  private HTSCaseRespository htsCaseRepository;

  @Autowired
  private PNSCaseRespository pnsCaseRepository;

  @Autowired
  PNSCaseContactRespository pnsCaseContactRespository;

  @Autowired
  PNSCaseContactRelationshipRespository pnsCaseContactRelationshipRespository;

  @Autowired
  private AdminUnitRepository adminUnitRepository;

  @Autowired
  private PersonRepository personRepository;
  
  @Autowired
  private UserOrganizationRepository userOrganizationRepository;
	

  @Override
  public Page<PNSCaseContactDto> findAllPageable(PreventionFilterDto filter) {
    User currentUser = SecurityUtils.getCurrentUser();
    Boolean isAdmin = SecurityUtils.isUserInRole(currentUser, "ROLE_ADMIN");
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

    if (filter != null && ((orgIds != null && orgIds.size() > 0) || (writableOrgIds != null && writableOrgIds.size() > 0) || isAdmin)) {
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

      if(CollectionUtils.isEmpty(lstOrgIds) && !isAdmin) {
        return null;
      }
      if(CollectionUtils.isEmpty(lstPnsOrgIds) && !isAdmin && !isDONOR && !isNATIONAL && !isProvince && !isDISTRICT) {
        return null;
      }

      String SQL = "";
      if (isViewPII) {
        SQL = " SELECT new org.pepfar.pdma.app.data.dto.PNSCaseContactDto(s, true, true, false, false) from PNSCaseContact s WHERE 1=1 ";
      } else {
        SQL = " SELECT new org.pepfar.pdma.app.data.dto.PNSCaseContactDto(s, false, true, false, false) from PNSCaseContact s WHERE 1=1 ";
      }
      String countSQL = " SELECT COUNT(s.id) from PNSCaseContact s WHERE 1=1 ";
      String whereClause = " ";
      if(!isAdmin) {
        whereClause += " AND s.pnsCase.c2.id in (:lstOrgIds) ";
        if(isSite && !isDONOR && !isNATIONAL && !isProvince && !isDISTRICT) {
          whereClause += " AND s.pnsCase.c2.id in (:lstPnsOrgIds) ";
        }
      }
      String orderByClause = "";

      if (filter.getKeyword() != null && filter.getKeyword().length() > 0) {
        whereClause += " AND ("
                + " (s.person.fullname like :keyword) "
                + " OR (s.person.mobilePhone like :keyword) "
                + " )";
      }
      if (filter.getOrg() != null && filter.getOrg().getId() != null) {
        whereClause += " AND (s.pnsCase.c2.id = :orgId)";
      }
      if (filter.getStaff() != null && filter.getStaff().getId() != null) {
        whereClause += " AND (s.pnsCase.c3.id = :staffId)";
      }
      if (filter.getFromDate() != null) {
        whereClause += " AND (s.c1receivedInfoDate >= :from)";
      }
      if (filter.getToDate() != null) {
        whereClause += " AND (s.c1receivedInfoDate <= :to)";
      }
      if (filter.getPnsCaseId() != null && filter.getPnsCaseId() > 0L) {
        whereClause += " AND (s.pnsCase.id <= :pnsCaseId)";
      }
      if(filter.getProvinceId() != null) {
        whereClause += " AND (s.pnsCase.c2.address.province.id = :provinceId) ";
      }
      Query q = manager.createQuery(SQL + whereClause + orderByClause, PNSCaseContactDto.class);
      Query qCount = manager.createQuery(countSQL + whereClause);
      if(!isAdmin) {
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
      if (filter.getPnsCaseId() != null && filter.getPnsCaseId() > 0L) {
        q.setParameter("pnsCaseId", filter.getPnsCaseId());
        qCount.setParameter("pnsCaseId", filter.getPnsCaseId());
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
      List<PNSCaseContactDto> entities = q.getResultList();
      Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize());
      Page<PNSCaseContactDto> result = new PageImpl<PNSCaseContactDto>(entities, pageable, count);

      return result;
    }
    return null;
  }

  @SuppressWarnings("unlikely-arg-type")
  @Override
  public PNSCaseContactDto saveOrUpdate(PNSCaseContactDto dto) {
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
			throw new IllegalArgumentException("Cannot save a null instance of SNSCase.");
		}
	  
    if (dto != null && dto.getPnsCase() != null && dto.getPnsCase().getId() != null && dto.getPnsCase().getId() > 0L) {
      PNSCase pnsCase = null;
      if (dto.getPnsCase() != null) {
        pnsCase = pnsCaseRepository.findOne(dto.getPnsCase().getId());
      }
      if (pnsCase == null) {
        return null;
      }

      PNSCaseContact entity = null;
      if (CommonUtils.isPositive(dto.getId(), true)) {
        entity = pnsCaseContactRespository.findOne(dto.getId());
      }

      if (entity == null) {
        entity = new PNSCaseContact();
      }
      entity.setPnsCase(pnsCase);

      Person person = null;
      if (entity.getPerson() != null) {
        person = personRepository.findOne(entity.getPerson().getId());
      }
      if (dto.getPerson() != null) {
        if (dto.getPerson().getId() != null) {
          person = personRepository.findOne(dto.getPerson().getId());
        }
      }
      if (person == null) {
        person = new Person();
      }
      person.setGender(dto.getGender());
      person.setFullname(dto.getFullName());
      person.setMobilePhone(dto.getPhoneNumber());
      person.setDob(LocalDateTime.of(dto.getYearOfBirth(), 6, 15, 0, 0));
      if (dto.getPerson() != null && dto.getPerson().getUid() != null) {
        person.setUid(dto.getPerson().getUid());
      }
      entity.setPerson(person);

      List<PNSCaseContactRelationship> c1 = new ArrayList<PNSCaseContactRelationship>();
      if (dto.getC1() != null && dto.getC1().size() > 0) {
        for (PNSCaseContactRelationshipDto relationshipDto : dto.getC1()) {
          PNSCaseContactRelationship relationship = null;
          if (relationshipDto.getId() != null && relationshipDto.getId() > 0L) {
            relationship = pnsCaseContactRelationshipRespository.findOne(relationshipDto.getId());
          }
          if (relationship == null) {
            relationship = new PNSCaseContactRelationship();
          }
          relationship.setVal(relationshipDto.getVal());
          if (relationshipDto.getVal() != null) {
            relationship.setName(relationshipDto.getVal().getDescription());
          }
          relationship.setPnsCaseContact(entity);
          c1.add(relationship);
        }
      }
      
      
	entity.getC1().clear();
	entity.getC1().addAll(c1);

		if (entity.getC1() != null && entity.getC1().size() > 0) {
			int minRisk = 16;
			PNSCaseContactRelationship caseMinRiskGroup = null;
			for (PNSCaseContactRelationship caseRiskGroup : entity.getC1()) {
				caseRiskGroup.setIsMainRisk(false);
				if (caseRiskGroup.getVal().getPriority() > 0 && caseRiskGroup.getVal().getPriority() <= minRisk) {
					minRisk = caseRiskGroup.getVal().getPriority();
					caseMinRiskGroup = caseRiskGroup;
				}
			}
			if (caseMinRiskGroup != null) {
				caseMinRiskGroup.setIsMainRisk(true);
			}
		}

      entity.setC1receivedInfoDate(dto.getC1receivedInfoDate());
      entity.setC2(dto.getC2());
      entity.setC3(dto.getC3());
      entity.setC4First(dto.getC4First());
      entity.setC4Second(dto.getC4Second());
      entity.setC4Third(dto.getC4Third());
      entity.setC5(dto.getC5());
      entity.setC5Reason(dto.getC5Reason());
      entity.setC6(dto.getC6());
      entity.setC7(dto.getC7());
      entity.setC7Note(dto.getC7Note());
      entity.setC8(dto.getC8());
      if (dto.getC8HTSCase() != null && dto.getC8HTSCase().getId() != null) {
        entity.setC8HTSCase(htsCaseRepository.findOne(dto.getC8HTSCase().getId()));
      }
      entity.setC8LabtestCode(dto.getC8LabtestCode());
      entity.setC8LabtestDate(dto.getC8LabtestDate());
      entity.setC8LabtestOrg(dto.getC8LabtestOrg());
      entity.setC9(dto.getC9());
      entity.setC9ARVDate(dto.getC9ARVDate());
      entity.setC9JoinedARV(dto.getC9JoinedARV());
      entity.setC9PrEPDate(dto.getC9PrEPDate());
      entity.setC9JoinedPrEP(dto.getC9JoinedPrEP());
      if (dto.getProvince() != null && dto.getProvince().getId() != null && dto.getProvince().getId() > 0L) {
        AdminUnit province = adminUnitRepository.findOne(dto.getProvince().getId());
        entity.setProvince(province);
      }
      if (dto.getDistrict() != null && dto.getDistrict().getId() != null && dto.getDistrict().getId() > 0L) {
        AdminUnit district = adminUnitRepository.findOne(dto.getDistrict().getId());
        entity.setDistrict(district);
      }
      entity.setAddressDetail(dto.getAddressDetail());
      entity = pnsCaseContactRespository.save(entity);
      if (entity != null) {
        return newPNSCaseContactDto(entity, currentUser, isSite, isProvince, isAdministrator, listUO);
      } else {
        throw new RuntimeException();
      }
    }
    return null;
  }

  @Override
  public ResponseDto<PNSCaseContactDto> deleteById(Long id) {
    User currentUser = SecurityUtils.getCurrentUser();
    Boolean isSiteManagement = SecurityUtils.isUserInRole(currentUser, "ROLE_SITE_MANAGER");
    if (!isSiteManagement) {
      return null;
    }
    ResponseDto<PNSCaseContactDto> ret = new ResponseDto<PNSCaseContactDto>();
    final List<Long> writableOrgIds = authUtils.getGrantedOrgIds(Permission.WRITE_ACCESS);
    PNSCaseContact entity = pnsCaseContactRespository.findOne(id);
    if (entity != null) {
      if (entity.getC2() != null && !writableOrgIds.contains(entity.getPnsCase().getC2().getId())) {
        ret.setCode(YesNoNone.NO);
        ret.setMessage("B???n kh??ng c?? quy???n x??a b???n ghi n??y");
        return ret;
      }
      pnsCaseContactRespository.delete(id);
      ret.setCode(YesNoNone.YES);
      ret.setMessage("X??a th??nh c??ng");
      return ret;
    } else {
      ret.setCode(YesNoNone.NO);
      ret.setMessage("Kh??ng t??m th???y b???n ghi t????ng ???ng");
      return ret;
    }
  }

  @Override
  public PNSCaseContactDto findById(long id) {
	  if (!CommonUtils.isPositive(id, true)) {
			return null;
		}
		User currentUser = SecurityUtils.getCurrentUser();
		if (currentUser == null) {
			return null;
		}
		PNSCaseContact entity = pnsCaseContactRespository.findOne(id);
		if (entity != null) {
			Boolean isSite = SecurityUtils.isUserInRole(currentUser, "ROLE_SITE_MANAGER");
			Boolean isProvince = SecurityUtils.isUserInRole(currentUser, "ROLE_PROVINCIAL_MANAGER");
			Boolean isAdministrator = SecurityUtils.isUserInRole(currentUser, "ROLE_ADMIN");
			List<UserOrganization> listUO = null;
			if (!isAdministrator) {
				listUO = userOrganizationRepository.getListByUserId(currentUser.getId());
			}
			return newPNSCaseContactDto(entity, currentUser, isSite, isProvince, isAdministrator, listUO);
		} else {
			return null;
		}
  }
  
  private PNSCaseContactDto newPNSCaseContactDto(PNSCaseContact entity, User currentUser, boolean isSite, boolean isProvince,
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
						if (userOrganization.getOrganization() != null && entity.getPnsCase() != null && entity.getPnsCase().getC2()!=null
								&& userOrganization.getOrganization().getId().equals(entity.getPnsCase().getC2().getId())
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
			return new PNSCaseContactDto(entity, isViewPII, isReadAble, isWritAble, isDeleteAble);
		} else {
			return null;
		}
	}

//	@Override
//	public Workbook exportPNSCase(FilterDto searchDto) {
//		Workbook blankBook = new XSSFWorkbook();
//		blankBook.createSheet();
////		filter.setDisablePaging(true);
////		filter.setIsFullDto(true);
//		Page<PNSCaseContactDto> htsCaseDtos= this.findAllPageable(searchDto);
//		if(htsCaseDtos==null) {
//			return blankBook;
//		}
//		else {
//			Workbook wbook = null;
//			try (InputStream template = context.getResource("classpath:templates/hts-list.xlsx")
//					.getInputStream()) {
////				XSSFWorkbook tmp = new XSSFWorkbook(template);
////				Sheet sheet = tmp.getSheetAt(0);
//				wbook = new XSSFWorkbook(template);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			if (wbook == null) {
//				return blankBook;
//			}
//			int rowIndex = 7;
//			int colIndex = 0;
//			
//			Row row = null;
//			Cell cell = null;
//			Sheet sheet = wbook.getSheetAt(0);
//
//			int seq=0;
//			CellStyle cellStyle = wbook.createCellStyle();
//			ExcelUtils.setBorders4Style(cellStyle);
//			cellStyle.setWrapText(true);
//			cellStyle.setAlignment(HorizontalAlignment.RIGHT);
//			cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
//			CellStyle dateTimeStyle = wbook.createCellStyle();
//			DataFormat format = wbook.createDataFormat();
////			dateTimeStyle.cloneStyleFrom(templateRow.getCell(0).getCellStyle());
//			dateTimeStyle.setDataFormat(format.getFormat("dd/MM/yyyy"));
//			dateTimeStyle.setVerticalAlignment(VerticalAlignment.CENTER);
//			ExcelUtils.setBorders4Style(dateTimeStyle);
//			
//			if(searchDto.getOrg()!=null) {
//				row =sheet.getRow(2);
//				cell =row.createCell(2);
//				cell.setCellValue(searchDto.getOrg().getName());	
//			}
//			
//			if(searchDto.getFromDate()!=null) {
//				row =sheet.getRow(3);
//				cell =row.createCell(2);
//				cell.setCellStyle(dateTimeStyle);
//				cell.setCellValue(Date.from(searchDto.getFromDate().toInstant(ZoneOffset.of("+7"))));	
//			}
//			
//			if(searchDto.getToDate()!=null) {
//				row =sheet.getRow(4);
//				cell =row.createCell(2);
//				cell.setCellStyle(dateTimeStyle);
//				cell.setCellValue(Date.from(searchDto.getToDate().toInstant(ZoneOffset.of("+7"))));	
//			}
//			
//			for (PNSCaseContactDto htsCaseDto : htsCaseDtos) {
//				
//				row = sheet.createRow(rowIndex++);
//				
//				//STT
//				cell =row.createCell(colIndex++);
//				cell.setCellValue(seq+=1);				
//				cell.setCellStyle(cellStyle);
//				
//				//Ho ten tu van vien
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC3().getFullName());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//Ma so khach hang
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC6());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//	 			
//				//gioi tinh
//				cell =row.createCell(colIndex++);
//				try {
//					if(htsCaseDto.getC7().name().equals("MALE")) {
//						cell.setCellValue("Nam");
//					}
//					if(htsCaseDto.getC7().name().equals("FEMALE")) {
//						cell.setCellValue("N???");
//					}
//					if(htsCaseDto.getC7().name().equals("OTHER")) {
//						cell.setCellValue("Kh??c");
//					}
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//KQ XN HIV l???n n??y
//				cell =row.createCell(colIndex++);
//				try {
//					
//					cell.setCellValue(htsCaseDto.getC14().getDescription());
//				
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//N??m sinh
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC8());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//T?? v???n tr?????c XN
//				cell =row.createCell(colIndex++);
//				cell.setCellStyle(dateTimeStyle);
//				try {
//					cell.setCellValue(Date.from(htsCaseDto.getC4().toInstant(ZoneOffset.of("+7"))));
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				
//				
//				//K???t qu??? x??c minh ca HIV d????ng t??nh
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC24().getDescription());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//KQ XN s??ng l???c Giang mai
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC26().getDescription());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//BT m???c GM gi???i thi???u
//				cell =row.createCell(colIndex++);
//				try {
//					if(htsCaseDto.getC25().name().equals("YES")) {
//						cell.setCellValue("C??");
//					}
//					if(htsCaseDto.getC25().name().equals("NO")) {
//						cell.setCellValue("Kh??ng");
//					}
//					if(htsCaseDto.getC25().name().equals("NO_INFORMATION")) {
//						cell.setCellValue("Kh??ng c?? th??ng tin");
//					}
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//Quay l???i nh???n KQXN
//				cell =row.createCell(colIndex++);
//				cell.setCellStyle(dateTimeStyle);
//				try {
//					cell.setCellValue(Date.from(htsCaseDto.getC15Date().toInstant(ZoneOffset.of("+7"))));
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				
//				
//				//T?? v???n sau XN
//				cell =row.createCell(colIndex++);
//				try {
//					if(htsCaseDto.getC15().name().equals("YES")) {
//						cell.setCellValue("C??");
//					}
//					if(htsCaseDto.getC15().name().equals("NO")) {
//						cell.setCellValue("Kh??ng");
//					}
//					if(htsCaseDto.getC15().name().equals("NO_INFORMATION")) {
//						cell.setCellValue("Kh??ng c?? th??ng tin");
//					}
//					//cell.setCellValue(htsCaseDto.getC15().name());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//Gi???i thi???u ??i???u tr??? PrEP
//				cell =row.createCell(colIndex++);
//				try {
//					if(htsCaseDto.getC1627().name().equals("YES")) {
//						cell.setCellValue("C??");
//					}
//					if(htsCaseDto.getC1627().name().equals("NO")) {
//						cell.setCellValue("Kh??ng");
//					}
//					if(htsCaseDto.getC1627().name().equals("NO_INFORMATION")) {
//						cell.setCellValue("Kh??ng c?? th??ng tin");
//					}
//					//cell.setCellValue(htsCaseDto.getC1627().name());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//T??n c?? s??? ??i???u tr??? PrEP
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC1627Note());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//Ng??y ??i???u tr??? PrEP
//				cell =row.createCell(colIndex++);
//				cell.setCellStyle(dateTimeStyle);
//				try {
//					cell.setCellValue(Date.from(htsCaseDto.getC1627Date().toInstant(ZoneOffset.of("+7"))));
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				
//				
//				//Nh???n d???ch v??? ??i???u tr??? HIV
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC20().getDescription());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//T??n c?? s??? ??i???u tr??? HIV
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC20Org());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//Ng??y ??i???u tr??? HIV
//				cell =row.createCell(colIndex++);
//				cell.setCellStyle(dateTimeStyle);
//				try {
//					cell.setCellValue(Date.from(htsCaseDto.getC20RegDate().toInstant(ZoneOffset.of("+7"))));
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				
//				
//				//M?? s??? ??i???u tr???
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC20Code());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//L?? do kh??ng ??i???u tr??? HIV
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC20Reason());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//KQXN m???i nhi???m HIV
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC17().getDescription());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//KQXN t???i l?????ng vi-r??t
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC18().getDescription());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//Lo???i h??nh d???ch v??? TVXN HIV
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC5().getDescription());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//Ghi r?? c?? s??? TVXN HIV
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC5Note().getDescription());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//Nh??m nguy c?? c???a kh??ch h??ng
//				cell =row.createCell(colIndex++);
//				try {
//					String c9="";
//					if(htsCaseDto.getC9()!=null && htsCaseDto.getC9().size()>0) {
//						for(PNSCaseRiskGroupDto risk: htsCaseDto.getC9()) {
//							if(c9.length()>0) {
//								c9+=","+risk.getName();
//							}else {
//								c9+=risk.getName();
//							}
//							
//						}
//					}
//					
//					cell.setCellValue(c9);
//					
//					
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//Ngu???n gi???i thi???u/chuy???n g???i
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC10().getDescription());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//Ngu???n gi???i thi???u/chuy???n g???i
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC10Note());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//N???? c?? KQXN ph???n ???ng
//				cell =row.createCell(colIndex++);
//				try {
//					if(htsCaseDto.getC11().name().equals("YES")) {
//						cell.setCellValue("C??");
//					}
//					if(htsCaseDto.getC11().name().equals("NO")) {
//						cell.setCellValue("Kh??ng");
//					}
//					if(htsCaseDto.getC11().name().equals("NO_INFORMATION")) {
//						cell.setCellValue("Kh??ng c?? th??ng tin");
//					}
//					//cell.setCellValue(htsCaseDto.getC11().name());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//C?? ph???n ???ng v???i
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC11a().getDescription());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//N???? c?? KQXN ph???n ???ng
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC11aNote());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//C?? ph???n ???ng v???i
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC11b().getDescription());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//C?? ph???n ???ng v???i
//				cell =row.createCell(colIndex++);
//				try {
//					if(htsCaseDto.getC11c().name().equals("YES")) {
//						cell.setCellValue("C??");
//					}
//					if(htsCaseDto.getC11c().name().equals("NO")) {
//						cell.setCellValue("Kh??ng");
//					}
//					if(htsCaseDto.getC11c().name().equals("NO_INFORMATION")) {
//						cell.setCellValue("Kh??ng c?? th??ng tin");
//					}
//					//cell.setCellValue(htsCaseDto.getC11c().name());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//N???? c?? KQXN ph???n ???ng
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC11cNote());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//c12
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC12().getDescription());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//C?? ph???n ???ng v???i
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC131().getDescription());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//c12
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC132().getDescription());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//c132
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC19());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//Ng
//				cell =row.createCell(colIndex++);
//				cell.setCellStyle(dateTimeStyle);
//				try {
//					cell.setCellValue(Date.from(htsCaseDto.getC19Date().toInstant(ZoneOffset.of("+7"))));
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				
//				
//				//c132
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC19Note());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//c21
//				cell =row.createCell(colIndex++);
//				try {
//					if(htsCaseDto.getC21().name().equals("YES")) {
//						cell.setCellValue("C??");
//					}
//					if(htsCaseDto.getC21().name().equals("NO")) {
//						cell.setCellValue("Kh??ng");
//					}
//					if(htsCaseDto.getC21().name().equals("NO_INFORMATION")) {
//						cell.setCellValue("Kh??ng c?? th??ng tin");
//					}
//					//cell.setCellValue(htsCaseDto.getC21().name());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//Ng
//				cell =row.createCell(colIndex++);
//				cell.setCellStyle(dateTimeStyle);
//				try {
//					cell.setCellValue(Date.from(htsCaseDto.getC21Date().toInstant(ZoneOffset.of("+7"))));
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				
//				
//				//c22
//				cell =row.createCell(colIndex++);
//				try {
//					if(htsCaseDto.getC22().name().equals("YES")) {
//						cell.setCellValue("C??");
//					}
//					if(htsCaseDto.getC22().name().equals("NO")) {
//						cell.setCellValue("Kh??ng");
//					}
//					if(htsCaseDto.getC22().name().equals("NO_INFORMATION")) {
//						cell.setCellValue("Kh??ng c?? th??ng tin");
//					}
//					//cell.setCellValue(htsCaseDto.getC22().name());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//c23
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC23FullName());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//c23
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC23Ethnic().getValue());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//c23
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC23Profession().getValue());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//c23
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC23HealthNumber());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//c23
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC23IdNumber());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//c23
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC23PhoneNumber());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//c23
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC23ResidentAddressDetail());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//c23
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC23ResidentAddressWard());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//c23
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC23ResidentAddressDistrict().getName()+", "+htsCaseDto.getC23ResidentAddressProvince().getName());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//c23
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC23CurrentAddressDetail());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//c23
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC23CurrentAddressWard());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//c23
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC23CurrentAddressDistrict().getName()+", "+htsCaseDto.getC23CurrentAddressProvince().getName());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//c23
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC23Note());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				//c28
//				cell =row.createCell(colIndex++);
//				try {
//					cell.setCellValue(htsCaseDto.getC28().getDescription());
//				}catch(Exception e) {
//					cell.setCellValue("");
//				}	
//				cell.setCellStyle(cellStyle);
//				
//				colIndex=0;
//			}
//			return wbook;
//		}
//	}
//	@Override
//	public CheckCodeDto checkDuplicateCode(CheckCodeDto dto) {
//		if(dto!=null && StringUtils.hasText(dto.getCode()) && dto.getOrgId()!=null && dto.getOrgId()>0L) {
//			String SQL=" SELECT COUNT(s.id) FROM PNSCaseContact s WHERE s.c4=:code AND s.c2.id=:orgId ";
//			if(dto.getId()!=null && dto.getId()>0L) {
//				SQL+=" AND s.id!=:id ";
//			}
//			Query q = manager.createQuery(SQL);
//			q.setParameter("code", dto.getCode());
//			q.setParameter("orgId", dto.getOrgId());
//			if(dto.getId()!=null && dto.getId()>0L) {
//				q.setParameter("id", dto.getId());
//			}
//			Long count = (Long)q.getSingleResult();
//			dto.setIsDup(count!=null && count>0);
//			if(dto.getIsDup()) {
//				dto.setNote("M?? s??? "+dto.getCode()+" ???? b??? tr??ng");
//			}
//			return dto;
//		}
//		return null;
//	}

  @Override
  public Workbook exportPNSCase(PreventionFilterDto searchDto) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ImportResultDto<PNSCaseContactDto> importFromExcel(InputStream is) throws IOException {
    Workbook workbook = new XSSFWorkbook(is);
    Sheet datatypeSheet = workbook.getSheetAt(0);
    // Iterator<Row> iterator = datatypeSheet.iterator();
    int rowIndex = 1;
    int num = datatypeSheet.getLastRowNum();
    ImportResultDto<PNSCaseContactDto> ret = new ImportResultDto<PNSCaseContactDto>();
    while (rowIndex <= num) {
      try {
        System.out.println(rowIndex);
        Row currentRow = datatypeSheet.getRow(rowIndex);
        Cell currentCell = null;
        if (currentRow != null) {
          String err = "";
          PNSCaseContactDto dto = new PNSCaseContactDto();
          try {
            //						c1 - person_id
            currentCell = currentRow.getCell(0);
            if (currentCell != null) {
              String uid = null;
              if (currentCell.getCellType() == CellType.STRING) {
                uid = currentCell.getStringCellValue();
                if (!StringUtils.hasText(uid)) {
                  rowIndex += 1;
                  continue;
                }
                if(uid != null) {
                  UUID uuid = UUID.fromString(uid);
                  Person person = personRepository.findByUid(uuid);
                  if (person != null) {
                    dto.setPerson(new PersonDto(person, true));
                  } else {
                    dto.setPerson(null);
                    err += "C1 - Kh??ng t??m th???y; ";
                  }
                } else {
                  dto.setPerson(null);
                }
              }
            }
          } catch (Exception e) {
            dto.setPerson(null);
            err += "C1 - Kh??ng r??; ";
          }

          try {
//						c2 - pns_case_id
            currentCell = currentRow.getCell(1);
            if (currentCell != null) {
              if (currentCell.getCellType() == CellType.STRING) {
                UUID uid = UUID.fromString(currentCell.getStringCellValue());
                if (uid != null) {
                  PNSCase pnsCase = pnsCaseRepository.findByUid(uid);
                  if (pnsCase != null) {
                    dto.setPnsCase(new PNSCaseDto(pnsCase, true));
                  } else {
                    dto.setPnsCase(null);
                    err += "C2 - Kh??ng t??m th???y; ";
                  }
                } else {
                  dto.setPnsCase(null);
                  err += "C2 kh??ng ???????c ????? tr???ng; ";
                }
              }
            }
          } catch (Exception e) {
            dto.setPnsCase(null);
            err += "C2 - Kh??ng r??; ";
          }
          try {
//          c4 - H??? t??n
            currentCell = currentRow.getCell(3);
            if (currentCell != null) {
              String namePerson = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                namePerson = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                namePerson = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              dto.setFullName(namePerson);
            }
          } catch (Exception e) {
            dto.setFullName(null);
            err += "C4 - Kh??ng r??; ";
          }
          try {
//          c5 - N??m sinh
            currentCell = currentRow.getCell(4);
            if (currentCell != null) {
              Integer yearOfBirth = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                yearOfBirth = Integer.parseInt(currentCell.getStringCellValue());
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                yearOfBirth = Integer.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              dto.setYearOfBirth(yearOfBirth);
            }
          } catch (Exception e) {
            dto.setYearOfBirth(null);
            err += "C5 - Kh??ng r??; ";
          }
          try {
//          c6 - Gi???i t??nh
            currentCell = currentRow.getCell(5);
            if (currentCell != null) {
              String gender = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                gender = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                gender = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              if (gender != null) {
                if (gender.equals("6")) {
                  dto.setGender(Gender.MALE);
                } else if (gender.equals("7")) {
                  dto.setGender(Gender.FEMALE);
                } else {
                  dto.setGender(null);
                  err += "C6 kh??ng ????ng (C6 ph???i b???ng 6 ho???c 7); ";
                }
              } else {
                dto.setGender(null);
              }
            }
          } catch (Exception e) {
            dto.setGender(null);
            err += "C6 - Kh??ng r??; ";
          }
          try {
//          c10 - ??i???n tho???i
            currentCell = currentRow.getCell(9);
            if (currentCell != null) {
              String phoneNumber = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                phoneNumber = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                phoneNumber = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              dto.setPhoneNumber(phoneNumber);
            }
          } catch (Exception e) {
            dto.setPhoneNumber(null);
            err += "C10 - Kh??ng r??; ";
          }
          try {
//						c7 - province_id
            currentCell = currentRow.getCell(6);
            if (currentCell != null) {
              Long provinceId = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                provinceId = Long.valueOf(currentCell.getStringCellValue());
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                provinceId = Long.valueOf((long) currentCell.getNumericCellValue()).longValue();
              }

              AdminUnit entity = null;
              String provinceIdConvert = String.valueOf(provinceId);
              if (provinceIdConvert.length() == 1) {
                provinceIdConvert = "0" + provinceIdConvert;
              }
              if (provinceId != null) {
                entity = adminUnitRepository.findByProvinceOrDistrict(provinceIdConvert);
                if (entity != null) {
                  dto.setProvince(new AdminUnitDto(entity));
                } else {
                  dto.setProvince(null);
                  err += "C7 - Kh??ng t??m th???y t???nh/th??nh ph???; ";
                }
              }
            }
          } catch (Exception e) {
            dto.setProvince(null);
            err += "C7 - Kh??ng r??; ";
          }
          try {
//						c8 - district_id
            currentCell = currentRow.getCell(7);
            if (currentCell != null) {
              Long districtId = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                districtId = Long.valueOf(currentCell.getStringCellValue());
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                districtId = Long.valueOf((long) currentCell.getNumericCellValue()).longValue();
              }
              AdminUnit entity = null;
              String districtIdConvert = String.valueOf(districtId);
              if (districtIdConvert.length() == 1) {
                districtIdConvert = "00" + districtIdConvert;
              } else if (districtIdConvert.length() == 2) {
                districtIdConvert = "0" + districtIdConvert;
              }
              if (districtId != null) {
                entity = adminUnitRepository.findByProvinceOrDistrict(districtIdConvert);
                if (entity != null) {
                  dto.setDistrict(new AdminUnitDto(entity));
                } else {
                  dto.setDistrict(null);
                  err += "C8 - Kh??ng t??m th???y qu???n/huy???n; ";
                }
              }
            }
          } catch (Exception e) {
            dto.setDistrict(null);
            err += "C8 - Kh??ng r??; ";
          }
          try {
//						c9 - address_detail
            currentCell = currentRow.getCell(8);
            if (currentCell != null) {
              String addressDetail = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                addressDetail = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                addressDetail = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              dto.setAddressDetail(addressDetail);
            }
          } catch (Exception e) {
            dto.setAddressDetail(null);
            err += "C9 - Kh??ng r??; ";
          }
//						c10 - c9
//					currentCell = currentRow.getCell(9);
//					if (currentCell != null) {
//						String c9RiskGroups = null;
//						if (currentCell.getCellType() == CellType.STRING
//										&& StringUtils.hasText(currentCell.getStringCellValue())) {
//							c9RiskGroups = currentCell.getStringCellValue();
//						} else if (currentCell.getCellType() == CellType.NUMERIC) {
//							c9RiskGroups = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
//						}
//
//					}

          try {
//						c11 - val
            currentCell = currentRow.getCell(10);
            if (currentCell != null) {
              String c1Relationship = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c1Relationship = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c1Relationship = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              List<PNSCaseContactRelationshipDto> c1 = new ArrayList<PNSCaseContactRelationshipDto>();
              String[] c1Relationships = c1Relationship.split("\\|");
              for (String c1Re : c1Relationships) {
                PNSCaseContactRelationshipDto p = new PNSCaseContactRelationshipDto();
                if (c1Re != null) {
                  if (c1Re.equals("169")) {
                    p.setVal(PNSCaseContactRelationshipType.answer1);
                  } else if (c1Re.equals("170")) {
                    p.setVal(PNSCaseContactRelationshipType.answer2);
                  } else if (c1Re.equals("171")) {
                    p.setVal(PNSCaseContactRelationshipType.answer3);
                  } else if (c1Re.equals("172")) {
                    p.setVal(PNSCaseContactRelationshipType.answer4);
                  } else if (c1Re.equals("173")) {
                    p.setVal(PNSCaseContactRelationshipType.answer5);
                  } else {
                    p.setVal(null);
                    err += "C11 kh??ng ????ng (C10 ph???i l???n h??n 168 v?? nh??? h??n 174); ";
                  }
                } else {
                  p.setVal(null);
                }
                p.setName(p.getVal().getDescription());
                c1.add(p);
              }
              dto.setC1(c1);
            }
          } catch (Exception e) {
            dto.setC1(null);
            err += "C11 - Kh??ng r??; ";
          }
          try {
//						c34 - received_info_date
            currentCell = currentRow.getCell(33);
            if (currentCell != null) {
              LocalDateTime receivedInfoDate = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                receivedInfoDate = LocalDateTime.parse(currentCell.getStringCellValue(), formatter);
              } else if (currentCell.getCellType() == CellType.NUMERIC
                      && currentCell.getDateCellValue() != null) {
                receivedInfoDate = currentCell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
              }
              dto.setC1receivedInfoDate(receivedInfoDate);
            }
          } catch (Exception e) {
            dto.setC1receivedInfoDate(null);
            err += "C34 - Kh??ng r??; ";
          }
          try {
//						c12 - c2_hiv_status
            currentCell = currentRow.getCell(11);
            if (currentCell != null) {
              String c2HivStatus = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c2HivStatus = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c2HivStatus = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              if (c2HivStatus != null) {
                if (c2HivStatus.equals("174")) {
                  dto.setC2(PNSHivStatus.answer1);
                } else if (c2HivStatus.equals("175")) {
                  dto.setC2(PNSHivStatus.answer2);
                } else {
                  dto.setC2(null);
                  err += "C12 kh??ng ????ng (C12 ph???i b???ng 174 ho???c 175); ";
                }
              } else {
                dto.setC2(null);
              }
            }
          } catch (Exception e) {
            dto.setC2(null);
            err += "C12 - Kh??ng r??; ";
          }
          try {
//						c13 - c3_violence_risk
            currentCell = currentRow.getCell(12);
            if (currentCell != null) {
              String c3ViolenceRisk = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c3ViolenceRisk = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c3ViolenceRisk = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              if (c3ViolenceRisk != null) {
                if (c3ViolenceRisk.equals("4")) {
                  dto.setC3(HTSYesNoNone.YES);
                } else if (c3ViolenceRisk.equals("3")) {
                  dto.setC3(HTSYesNoNone.NO);
                } else {
                  dto.setC3(null);
                  err += "C13 kh??ng ????ng (C13 ph???i b???ng 3 ho???c 4); ";
                }
              } else {
                dto.setC3(null);
              }
            }
          } catch (Exception e) {
            dto.setC3(null);
            err += "C13 - Kh??ng r??; ";
          }
          try {
//						c14 - c4_first
            currentCell = currentRow.getCell(13);
            if (currentCell != null) {
              LocalDateTime c4First = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                c4First = LocalDateTime.parse(currentCell.getStringCellValue(), formatter);
              } else if (currentCell.getCellType() == CellType.NUMERIC
                      && currentCell.getDateCellValue() != null) {
                c4First = currentCell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
              }
              dto.setC4First(c4First);
            }
          } catch (Exception e) {
            dto.setC4First(null);
            err += "C14 - Kh??ng r??; ";
          }
          try {
//						c15 - c4_second
            currentCell = currentRow.getCell(14);
            if (currentCell != null) {
              LocalDateTime c4Second = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                c4Second = LocalDateTime.parse(currentCell.getStringCellValue(), formatter);
              } else if (currentCell.getCellType() == CellType.NUMERIC
                      && currentCell.getDateCellValue() != null) {
                c4Second = currentCell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
              }
              dto.setC4Second(c4Second);
            }
          } catch (Exception e) {
            dto.setC4Second(null);
            err += "C15 - Kh??ng r??; ";
          }
          try {
//						c16 - c4_third
            currentCell = currentRow.getCell(15);
            if (currentCell != null) {
              LocalDateTime c4Third = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                c4Third = LocalDateTime.parse(currentCell.getStringCellValue(), formatter);
              } else if (currentCell.getCellType() == CellType.NUMERIC
                      && currentCell.getDateCellValue() != null) {
                c4Third = currentCell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
              }
              dto.setC4Third(c4Third);
            }
          } catch (Exception e) {
            dto.setC4Third(null);
            err += "C16 - Kh??ng r??; ";
          }
          try {
//						c17 - c5_contacted
            currentCell = currentRow.getCell(16);
            if (currentCell != null) {
              String c5Contacted = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c5Contacted = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c5Contacted = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              if (c5Contacted != null) {
                if (c5Contacted.equals("4")) {
                  dto.setC5(HTSYesNoNone.YES);
                } else if (c5Contacted.equals("3")) {
                  dto.setC5(HTSYesNoNone.NO);
                } else {
                  dto.setC5(null);
                  err += "C17 kh??ng ????ng (C17 ph???i b???ng 3 ho???c 4); ";
                }
              } else {
                dto.setC5(null);
              }
            }
          } catch (Exception e) {
            dto.setC5(null);
            err += "C17 - Kh??ng r??; ";
          }
          try {
//						c18 - c5_reason
            currentCell = currentRow.getCell(17);
            if (currentCell != null) {
              String c5Reason = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c5Reason = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c5Reason = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              if (c5Reason != null) {
                if (c5Reason.equals("176")) {
                  dto.setC5Reason(PNSc5Reason.answer1);
                } else if (c5Reason.equals("177")) {
                  dto.setC5Reason(PNSc5Reason.answer2);
                } else if (c5Reason.equals("178")) {
                  dto.setC5Reason(PNSc5Reason.answer3);
                } else {
                  dto.setC5Reason(null);
                  err += "C18 kh??ng ????ng (C18 ph???i l???n h??n 175 v?? nh??? h??n 179); ";
                }
              } else {
                dto.setC5Reason(null);
              }
            }
          } catch (Exception e) {
            dto.setC5Reason(null);
            err += "C18 - Kh??ng r??; ";
          }
          try {
//						c19 - c6_succeeded_method
            currentCell = currentRow.getCell(18);
            if (currentCell != null) {
              String c6SucceededMethod = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c6SucceededMethod = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c6SucceededMethod = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              if (c6SucceededMethod != null) {
                if (c6SucceededMethod.equals("180")) {
                  dto.setC6(PNSSucceededMethod.answer1);
                } else if (c6SucceededMethod.equals("181")) {
                  dto.setC6(PNSSucceededMethod.answer2);
                } else if (c6SucceededMethod.equals("182")) {
                  dto.setC6(PNSSucceededMethod.answer3);
                } else if (c6SucceededMethod.equals("183")) {
                  dto.setC6(PNSSucceededMethod.answer4);
                } else {
                  dto.setC6(null);
                  err += "C19 kh??ng ????ng (C19 ph???i l???n h??n 179 v?? nh??? h??n 184); ";
                }
              } else {
                dto.setC6(null);
              }
            }
          } catch (Exception e) {
            dto.setC6(null);
            err += "C19 - Kh??ng r??; ";
          }
          try {
//						c20 - c12
            currentCell = currentRow.getCell(19);
            if (currentCell != null) {
              String c7SucceededContactMethod = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c7SucceededContactMethod = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c7SucceededContactMethod = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              if (c7SucceededContactMethod != null) {
                if (c7SucceededContactMethod.equals("184")) {
                  dto.setC7(PNSSucceededContactMethod.answer1);
                } else if (c7SucceededContactMethod.equals("185")) {
                  dto.setC7(PNSSucceededContactMethod.answer2);
                } else if (c7SucceededContactMethod.equals("186")) {
                  dto.setC7(PNSSucceededContactMethod.answer3);
                } else if (c7SucceededContactMethod.equals("187")) {
                  dto.setC7(PNSSucceededContactMethod.answer4);
                } else if (c7SucceededContactMethod.equals("188")) {
                  dto.setC7(PNSSucceededContactMethod.answer5);
                } else {
                  dto.setC7(null);
                  err += "C20 kh??ng ????ng (C20 ph???i l???n h??n 183 v?? nh??? h??n 189); ";
                }
              } else {
                dto.setC7(null);
              }
            }
          } catch (Exception e) {
            dto.setC7(null);
            err += "C20 - Kh??ng r??; ";
          }
          try {
//						c21 - c7_note
            currentCell = currentRow.getCell(20);
            if (currentCell != null) {
              String c7Note = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c7Note = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c7Note = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              dto.setC7Note(c7Note);
            }
          } catch (Exception e) {
            dto.setC7Note(null);
            err += "C21 - Kh??ng r??; ";
          }
          try {
//						c22 - c8_hiv_labtest_status
            currentCell = currentRow.getCell(21);
            if (currentCell != null) {
              String c8HivLabtestStatus = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c8HivLabtestStatus = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c8HivLabtestStatus = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              if (c8HivLabtestStatus != null) {
                if (c8HivLabtestStatus.equals("189")) {
                  dto.setC8(PNSc8.answer1);
                } else if (c8HivLabtestStatus.equals("190")) {
                  dto.setC8(PNSc8.answer2);
                } else if (c8HivLabtestStatus.equals("191")) {
                  dto.setC8(PNSc8.answer3);
                } else {
                  dto.setC8(null);
                  err += "C22 kh??ng ????ng (C22 ph???i l???n h??n 188 v?? nh??? h??n 192); ";
                }
              } else {
                dto.setC8(null);
              }
            }
          } catch (Exception e) {
            dto.setC8(null);
            err += "C22 - Kh??ng r??; ";
          }
          try {
//						c23 - c8_labtest_code
            currentCell = currentRow.getCell(22);
            if (currentCell != null) {
              String c8LabtestCode = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c8LabtestCode = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c8LabtestCode = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              dto.setC8LabtestCode(c8LabtestCode);
            }
          } catch (Exception e) {
            dto.setC8LabtestCode(null);
            err += "C23 - Kh??ng r??; ";
          }
          try {
//						c25 - c8_labtest_date
            currentCell = currentRow.getCell(24);
            if (currentCell != null) {
              LocalDateTime c8LabtestDate = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                c8LabtestDate = LocalDateTime.parse(currentCell.getStringCellValue(), formatter);
              } else if (currentCell.getCellType() == CellType.NUMERIC
                      && currentCell.getDateCellValue() != null) {
                c8LabtestDate = currentCell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
              }
              dto.setC8LabtestDate(c8LabtestDate);
            }
          } catch (Exception e) {
            dto.setC8LabtestDate(null);
            err += "C25 - Kh??ng r??; ";
          }

//						c25 - c8_labtest_org
//					currentCell = currentRow.getCell(25);
//					if (currentCell != null) {
//						String c8LabtestOrg = null;
//						if (currentCell.getCellType() == CellType.STRING
//										&& StringUtils.hasText(currentCell.getStringCellValue())) {
//							c8LabtestOrg = currentCell.getStringCellValue();
//						} else if (currentCell.getCellType() == CellType.NUMERIC) {
//							c8LabtestOrg = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
//						}
//						dto.setC8LabtestOrg(c8LabtestOrg);
//					}
//
          try {
//					c26 - c9
            currentCell = currentRow.getCell(25);
            if (currentCell != null) {
              String c9 = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c9 = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c9 = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              if (c9 != null) {
                if (c9.equals("44")) {
                  dto.setC9(PNSc9.answer1);
                } else if (c9.equals("45")) {
                  dto.setC9(PNSc9.answer2);
                } else if (c9.equals("46")) {
                  dto.setC9(PNSc9.answer3);
                } else if (c9.equals("179")) {
                  dto.setC9(PNSc9.answer4);
                } else {
                  dto.setC9(null);
                  err += "C26 kh??ng ????ng (C26 ph???i b???ng 44 ho???c 45 ho???c 46 ho???c 179); ";
                }
              } else {
                dto.setC9(null);
              }
            }
          } catch (Exception e) {
            dto.setC9(null);
            err += "C26 - Kh??ng r??; ";
          }
          try {
//						c27 - c9_joined_prep
            currentCell = currentRow.getCell(26);
            if (currentCell != null) {
              String c9JoinedPrep = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c9JoinedPrep = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c9JoinedPrep = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              if (c9JoinedPrep != null) {
                if (c9JoinedPrep.equals("4")) {
                  dto.setC9JoinedPrEP(HTSYesNoNone.YES);
                } else if (c9JoinedPrep.equals("3")) {
                  dto.setC9JoinedPrEP(HTSYesNoNone.NO);
                } else {
                  dto.setC9JoinedPrEP(null);
                  err += "C27 kh??ng ????ng (C27 ph???i b???ng 3 ho???c 4); ";
                }
              } else {
                dto.setC9JoinedPrEP(null);
              }
            }
          } catch (Exception e) {
            dto.setC9JoinedPrEP(null);
            err += "C27 - Kh??ng r??; ";
          }
          try {
//						c28 - c9_prep_date
            currentCell = currentRow.getCell(27);
            if (currentCell != null) {
              LocalDateTime c9PrepDate = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                c9PrepDate = LocalDateTime.parse(currentCell.getStringCellValue(), formatter);
              } else if (currentCell.getCellType() == CellType.NUMERIC
                      && currentCell.getDateCellValue() != null) {
                c9PrepDate = currentCell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
              }
              dto.setC9PrEPDate(c9PrepDate);
            }
          } catch (Exception e) {
            dto.setC9PrEPDate(null);
            err += "C28 - Kh??ng r??; ";
          }
          try {
//						c29 - c9_joined_arv
            currentCell = currentRow.getCell(28);
            if (currentCell != null) {
              String c9JoinedArv = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                c9JoinedArv = currentCell.getStringCellValue();
              } else if (currentCell.getCellType() == CellType.NUMERIC) {
                c9JoinedArv = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
              }
              if (c9JoinedArv != null) {
                if (c9JoinedArv.equals("4")) {
                  dto.setC9JoinedARV(HTSYesNoNone.YES);
                } else if (c9JoinedArv.equals("3")) {
                  dto.setC9JoinedARV(HTSYesNoNone.NO);
                } else {
                  dto.setC9JoinedARV(null);
                  err += "C29 kh??ng ????ng (C29 ph???i b???ng 3 ho???c 4); ";
                }
              } else {
                dto.setC9JoinedARV(null);
              }
            }
          } catch (Exception e) {
            dto.setC9JoinedARV(null);
            err += "C29 - Kh??ng r??; ";
          }
          try {
//						c30 - c9_arv_date
            currentCell = currentRow.getCell(29);
            if (currentCell != null) {
              LocalDateTime c9ArvDate = null;
              if (currentCell.getCellType() == CellType.STRING
                      && StringUtils.hasText(currentCell.getStringCellValue())) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                c9ArvDate = LocalDateTime.parse(currentCell.getStringCellValue(), formatter);
              } else if (currentCell.getCellType() == CellType.NUMERIC
                      && currentCell.getDateCellValue() != null) {
                c9ArvDate = currentCell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
              }
              dto.setC9ARVDate(c9ArvDate);
            }
          } catch (Exception e) {
            dto.setC9ARVDate(null);
            err += "C30 - Kh??ng r??; ";
          }
//          try {
//              saveOrUpdate(dto);
//            } catch (Exception e) {
//              dto.setErrorContent(e.getMessage());
//              dto.setNumberErrorContent(rowIndex + 1);
////              ret.setTotalErr(ret.getTotalErr() + 1);
////              ret.getListErr().add(dto);
//            }
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
//            if (saveOrUpdate(dto) != null) {
//              dto.setSaved(true);
//            } else {
//              dto.setSaved(false);
//              if (dto.getErrorContent().equals("")) {
//                dto.setErrorContent("L??u th???t b???i");
//                dto.setNumberErrorContent(rowIndex + 1);
//                ret.setTotalErr(ret.getTotalErr() + 1);
//                ret.getListErr().add(dto);
//              } else {
//                dto.setErrorContent(err + "L??u th???t b???i");
//              }
//            }
//          } catch (Exception e) {
//            dto.setSaved(false);
//            if (dto.getErrorContent().equals("")) {
//              dto.setErrorContent("L??u th???t b???i");
//              dto.setNumberErrorContent(rowIndex + 1);
//              ret.setTotalErr(ret.getTotalErr() + 1);
//              ret.getListErr().add(dto);
//            } else {
//              dto.setErrorContent(err + "L??u th???t b???i");
//            }
//          }
          try {
            dto = saveOrUpdate(dto);
            dto.setSaved(true);
          } catch (Exception e) {
            ret.setTotalErr(ret.getTotalErr() + 1);
            err += "L??u th???t b???i:"+CommonUtils.getStackTrace(e);
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
        e.printStackTrace();
        // TODO: handle exception
      }
      rowIndex += 1;
    }

    return ret;
  }
}
