package org.pepfar.pdma.app.data.service.jpa;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.hibernate.transform.Transformers;
import org.pepfar.pdma.app.Constants;
import org.pepfar.pdma.app.data.domain.Dictionary;
import org.pepfar.pdma.app.data.domain.*;
import org.pepfar.pdma.app.data.dto.*;
import org.pepfar.pdma.app.data.repository.*;
import org.pepfar.pdma.app.data.service.HTSCaseService;
import org.pepfar.pdma.app.data.service.ReportMerService;
import org.pepfar.pdma.app.data.types.Permission;
import org.pepfar.pdma.app.data.types.*;
import org.pepfar.pdma.app.utils.*;
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

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service("HTSCaseServiceImpl")
public class HTSCaseServiceImpl implements HTSCaseService {

    @Autowired
    public EntityManager manager;

    @Autowired
    private AuthorizationUtils authUtils;

    @Autowired
    private HTSCaseRespository htsCaseRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private DictionaryRepository dictionaryRepository;

    @Autowired
    private AdminUnitRepository adminUnitRepository;

    @Autowired
    private HTSCaseRiskGroupRepository htsCaseRiskGroupRepository;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private UserOrganizationRepository userOrganizationRepository;

    @Autowired
    private ReportMerService reportMerService;

    @Override
    public Page<HTSCaseDto> findAllPageable(PreventionFilterDto filter) {
        User currentUser = SecurityUtils.getCurrentUser();
        Boolean isAdministrator = SecurityUtils.isUserInRole(currentUser, "ROLE_ADMIN");
        Boolean isProvince = SecurityUtils.isUserInRole(currentUser, "ROLE_PROVINCIAL_MANAGER");
        Boolean isSiteManagement = SecurityUtils.isUserInRole(currentUser, "ROLE_SITE_MANAGER");

        Boolean isDONOR = SecurityUtils.isUserInRole(currentUser, "ROLE_DONOR");
        Boolean isNATIONAL = SecurityUtils.isUserInRole(currentUser, "ROLE_NATIONAL_MANAGER");
        Boolean isDISTRICT = SecurityUtils.isUserInRole(currentUser, "ROLE_DISTRICT_MANAGER");

        Boolean isViewPII = isSiteManagement || isProvince;
        // Check ORG
        final List<Long> orgIds = authUtils.getGrantedOrgIds(Permission.READ_ACCESS);
        final List<Long> writableOrgIds = authUtils.getGrantedOrgIds(Permission.WRITE_ACCESS);
        final List<Long> lstHtsOrgIds = authUtils.getGrantedOrgIds(Permission.HTS_ACCESS);

        if (isAdministrator
                || ((orgIds != null && orgIds.size() > 0) || (writableOrgIds != null && writableOrgIds.size() > 0)
                || (lstHtsOrgIds != null && lstHtsOrgIds.size() > 0))) {
            // Check ORG
            if (filter == null) {
                filter = new PreventionFilterDto();
            }
            List<Long> lstOrgIds = new ArrayList<Long>();
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
            //Nếu có các quyền như code (chỉ có ROLE_SITE_MANAGER) này thì phải xét đến lstHtsOrgIds
            if (CollectionUtils.isEmpty(lstHtsOrgIds) && !isAdministrator && !isDONOR && !isNATIONAL && !isProvince && !isDISTRICT) {
                return null;
            }
            String SQL = "";
            if (isViewPII) {
                SQL = " SELECT new org.pepfar.pdma.app.data.dto.HTSCaseDto(s, true,true,false,false,false) from HTSCase s WHERE 1=1 ";
            } else {
                SQL = " SELECT new org.pepfar.pdma.app.data.dto.HTSCaseDto(s, false,true,false,false,false) from HTSCase s WHERE 1=1 ";
            }
            String countSQL = " SELECT count(s.id) from HTSCase s WHERE 1=1 ";
            String whereClause = " ";
            if (!isAdministrator) {
                whereClause += " AND s.c2.id in (:lstOrgIds) ";
                if (isSiteManagement && !isDONOR && !isNATIONAL && !isProvince && !isDISTRICT) {
                    whereClause += " AND s.c2.id in (:lstHtsOrgIds) ";
                }
            }
            String orderByClause = "";

            if (filter.getKeyword() != null && filter.getKeyword().length() > 0) {
                whereClause += " AND (" + " (s.c6 like :keyword) " + " OR (s.c23FullName like :keyword) "
                        + " OR (s.c23IdNumber like :keyword) " + " OR (s.c23HealthNumber like :keyword) "
                        + " OR (s.c23PhoneNumber like :keyword) " + ")";
            }
            if (filter.getOrg() != null && filter.getOrg().getId() != null) {
                whereClause += " AND (s.c2.id = :orgId)";
            }
            if (filter.getStaff() != null && filter.getStaff().getId() != null) {
                whereClause += " AND (s.c3.id = :staffId)";
            }
            if (filter.getFromDate() != null) {
                whereClause += " AND (s.c4 >= :from)";
            }
            if (filter.getToDate() != null) {
                whereClause += " AND (s.c4 <= :to)";
            }
            if (filter.getC15() != null) {
                whereClause += " AND (s.c15 = :c15)";
            }
            if (filter.getSkipHTS() != null) {
                whereClause += " AND (s.id != :skipHTS)";
            }
            if (filter.getC14() != null) {
                whereClause += " AND (s.c14 = :c14)";
            }
            if (filter.isSkipPNS()) {
                whereClause += " AND (s.id not in (SELECT pns.htsCase.id from PNSCase pns WHERE pns.htsCase is not null)) ";
            }
            if (filter.getTypeMap() != null) {// 1 là mới nhiễm; 2 là hiv+
                if (filter.getTypeMap() == 1) {
                    whereClause += " AND (s.c17 = :c17) ";
                }
                if (filter.getTypeMap() == 2) {
                    whereClause += " AND (s.c14 = :c14map) ";
                }
            }
            if (filter.getKeyMap() != null) {
                whereClause += " AND (s.c23CurrentAddressDistrict.codeGso = :c23CurrentAddressDistrict) ";
            }
            if ((filter.getNotComplete() != null && filter.getNotComplete())) {
                whereClause += " AND ((s.c15 = :c15NoInfo)";
                whereClause += " OR (s.c18 = :c18NoInfo)";
                whereClause += " OR (s.c24 = :c24NoInfo)";
                whereClause += " OR (s.c14 = :c14Negative AND s.c15 = :c15Yes AND s.c1627 is null)";
//                whereClause += " OR (s.notComplete = :notComplete)";
                whereClause += " OR (s.c20 = :c20NoInfo))";
//                whereClause += " AND (s.canNotComplete is null OR s.canNotComplete = :canNotCompletedIsFalse)";
                whereClause += " AND ((s.c2.canNotBeCompleted =:canNotBeCompletedTrue and s.notComplete = :notComplete and (s.canNotComplete is null OR s.canNotComplete = :canNotCompletedIsFalse)) ";
                whereClause += " OR ((s.c2.canNotBeCompleted =:canNotBeCompletedFalse or s.c2.canNotBeCompleted is null) AND s.notComplete = :notComplete))";
            }

            if (filter.getProvinceId() != null) {
                whereClause += " AND (s.c2.address.province.id = :provinceId) ";
            }
            orderByClause += " ORDER BY s.notComplete DESC, s.canNotComplete";
            if (filter.getType() != null && filter.getType() != 10) {
                switch (filter.getType()) {
                    case 1:
                        whereClause += " AND (s.c15 = :types OR s.c15 = :types1)";
                        break;
                    case 2:
                        whereClause += " AND (s.c11 = :types)";
                        break;
                    case 3:
                        whereClause += " AND (s.c12 = :types)";
                        break;
                    case 4:
                        whereClause += " AND (s.c14 = :types)";
                        break;
                    case 5:
                        whereClause += " AND (s.c24 != :types AND (s.c11c != :types1 OR s.c11b = :types2))";
                        break;
                    case 6:
                        whereClause += " AND (s.c1627 = :types)";
                        break;
                    case 7:
                        whereClause += " AND (s.c20 :types)";
                        break;
                    case 8:
                        whereClause += " AND (s.c24 = :types)";
                        break;
                    case 9:
                        whereClause += " AND (s.c25 = :types)";
                        break;
                }
            }

            Query q = manager.createQuery(SQL + whereClause + orderByClause, HTSCaseDto.class);
            Query qCount = manager.createQuery(countSQL + whereClause);
            if (!isAdministrator) {
                q.setParameter("lstOrgIds", lstOrgIds);
                qCount.setParameter("lstOrgIds", lstOrgIds);
                if (isSiteManagement && !isDONOR && !isNATIONAL && !isProvince && !isDISTRICT) {
                    q.setParameter("lstHtsOrgIds", lstHtsOrgIds);
                    qCount.setParameter("lstHtsOrgIds", lstHtsOrgIds);
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
            if (filter.getC15() != null) {
                q.setParameter("c15", filter.getC15());
                qCount.setParameter("c15", filter.getC15());
            }
            if (filter.getSkipHTS() != null) {
                q.setParameter("skipHTS", filter.getSkipHTS());
                qCount.setParameter("skipHTS", filter.getSkipHTS());
            }
            if (filter.getC14() != null) {
                q.setParameter("c14", filter.getC14());
                qCount.setParameter("c14", filter.getC14());
            }

//      if (filter.isSkipPNS() == true  && idHTSinPNS!=null && idHTSinPNS.size()>0) {
//        q.setParameter("idHTSinPNS", idHTSinPNS);
//        qCount.setParameter("idHTSinPNS", idHTSinPNS);
//      }
            if (filter.getTypeMap() != null) {// 1 là mới nhiễm; 2 là hiv+
                if (filter.getTypeMap() == 1) {
                    q.setParameter("c17", HTSc17.answer1);
                    qCount.setParameter("c17", HTSc17.answer1);

                }
                if (filter.getTypeMap() == 2) {
                    q.setParameter("c14map", HTSc14.answer2);
                    qCount.setParameter("c14map", HTSc14.answer2);
                }
            }
            if (filter.getKeyMap() != null) {
                q.setParameter("c23CurrentAddressDistrict", filter.getKeyMap().toString());
                qCount.setParameter("c23CurrentAddressDistrict", filter.getKeyMap().toString());
            }

            if (filter.getNotComplete() != null && filter.getNotComplete() == true) {
                q.setParameter("c15NoInfo", HTSYesNoNone.NO_INFORMATION);
                qCount.setParameter("c15NoInfo", HTSYesNoNone.NO_INFORMATION);
                q.setParameter("c18NoInfo", HTSc18.answer4);
                qCount.setParameter("c18NoInfo", HTSc18.answer4);
                q.setParameter("c14Negative", HTSc14.answer1);
                qCount.setParameter("c14Negative", HTSc14.answer1);
                q.setParameter("c15Yes", HTSYesNoNone.YES);
                qCount.setParameter("c15Yes", HTSYesNoNone.YES);
                q.setParameter("c24NoInfo", HTSc24.answer3);
                qCount.setParameter("c24NoInfo", HTSc24.answer3);
                q.setParameter("c20NoInfo", HTSc20.answer1);
                qCount.setParameter("c20NoInfo", HTSc20.answer1);
                q.setParameter("notComplete", true);
                qCount.setParameter("notComplete", true);
                q.setParameter("canNotCompletedIsFalse", false);
                qCount.setParameter("canNotCompletedIsFalse", false);
                q.setParameter("canNotBeCompletedTrue", true);
                qCount.setParameter("canNotBeCompletedTrue", true);
                q.setParameter("canNotBeCompletedFalse", false);
                qCount.setParameter("canNotBeCompletedFalse", false);

            }
            if (filter.getProvinceId() != null) {
                q.setParameter("provinceId", filter.getProvinceId());
                qCount.setParameter("provinceId", filter.getProvinceId());
            }

            if (filter.getType() != null && filter.getType() != 0) {
                switch (filter.getType()) {
                    case 1:
                        q.setParameter("types", HTSYesNoNone.NO);
                        qCount.setParameter("types", HTSYesNoNone.NO);
                        q.setParameter("types1", HTSYesNoNone.NO_INFORMATION);
                        qCount.setParameter("types1", HTSYesNoNone.NO_INFORMATION);
                        break;
                    case 2:
                        q.setParameter("types", HTSYesNoNone.YES);
                        qCount.setParameter("types", HTSYesNoNone.YES);
                        break;
                    case 3:
                        q.setParameter("types", HTSc12.answer3);
                        qCount.setParameter("types", HTSc12.answer3);
                        break;
                    case 4:
                        q.setParameter("types", HTSc14.answer2);
                        qCount.setParameter("types", HTSc14.answer2);
                        break;
                    case 5:
                        q.setParameter("types", HTSc24.answer2);
                        qCount.setParameter("types", HTSc24.answer2);
                        q.setParameter("types1", HTSYesNoNone.YES);
                        qCount.setParameter("types1", HTSYesNoNone.YES);
                        q.setParameter("types2", HTSc11b.answer1);
                        qCount.setParameter("types2", HTSc11b.answer1);
                        break;
                    case 6:
                        q.setParameter("types", HTSYesNoNone.YES);
                        qCount.setParameter("types", HTSYesNoNone.YES);
                        break;
                    case 7:
                        q.setParameter("types", HTSc20.answer3);
                        qCount.setParameter("types", HTSc20.answer3);
                        break;
                    case 8:
                        q.setParameter("types", HTSc24.answer3);
                        qCount.setParameter("types", HTSc24.answer3);
                        break;
                    case 9:
                        q.setParameter("types", HTSYesNoNone.NO);
                        qCount.setParameter("types", HTSYesNoNone.NO);
                        break;
                }
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
            List<HTSCaseDto> entities = q.getResultList();
            Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize());
            Page<HTSCaseDto> result = new PageImpl<HTSCaseDto>(entities, pageable, count);

            return result;
        }
        return null;
    }

    @SuppressWarnings("unlikely-arg-type")
    @Override
    public HTSCaseDto saveOrUpdate(HTSCaseDto htsCaseDto) {
        User currentUser = SecurityUtils.getCurrentUser();
//    Boolean isSiteManagement = false;
//    Boolean checkExist = SecurityUtils.isUserInRole(currentUser, "ROLE_SITE_MANAGER");
//    Boolean checkExist1 = SecurityUtils.isUserInRole(currentUser, "ROLE_PROVINCIAL_MANAGER");
//    Boolean isAdmin=SecurityUtils.isUserInRole(currentUser, Constants.ROLE_ADMIN);

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
        if (htsCaseDto == null) {
            throw new IllegalArgumentException("Cannot save a null instance of HTSCase.");
        }
        if (!DateTimeUtil.checkEditableByMonth(Constants.NUMBER_OF_MONTH_FOR_EDIT,htsCaseDto.getC4()) && htsCaseDto.getId()!=null){
            HTSCaseDto rs = new HTSCaseDto();
            rs.setEditAble(false);
            return  rs;
        }

        HTSCase entity = null;

        if (CommonUtils.isPositive(htsCaseDto.getId(), true)) {
            entity = htsCaseRepository.findOne(htsCaseDto.getId());
        } else if (htsCaseDto.getUid() != null) {
            entity = htsCaseRepository.findByUid(htsCaseDto.getUid());
        }

        if (entity == null) {
            entity = new HTSCase();
        }

        // bắt đầu xét các trường của entity\
        if (htsCaseDto.getUid() != null) {
            entity.setUid(htsCaseDto.getUid());
        }
        Organization c2 = null;
        if (htsCaseDto.getC2() != null && htsCaseDto.getC2().getId() != null) {
            c2 = organizationRepository.findOne(htsCaseDto.getC2().getId());
//      if (c2 != null && !writableOrgIds.contains(c2.getId())) {
//        return null;
//      }
            entity.setC2(c2);
        }

        Staff c3 = null;
        if (htsCaseDto.getC3() != null && htsCaseDto.getC3().getId() != null) {
            c3 = staffRepository.findOne(htsCaseDto.getC3().getId());
            entity.setC3(c3);
        }

        if (htsCaseDto.getC15() == HTSYesNoNone.NO_INFORMATION || htsCaseDto.getC18() == HTSc18.answer4
                || htsCaseDto.getC24() == HTSc24.answer3 || (htsCaseDto.getC14() == HTSc14.answer1
                && htsCaseDto.getC15() == HTSYesNoNone.YES && htsCaseDto.getC1627() == null)
                || htsCaseDto.getC20() == HTSc20.answer1) {
            entity.setNotComplete(true);
        } else {
            entity.setNotComplete(false);
        }

        entity.setC4(htsCaseDto.getC4());
        entity.setC5(htsCaseDto.getC5());

        if (htsCaseDto.getC5() != null && htsCaseDto.getC5().getNumber() == 2) {
            entity.setC5Note(htsCaseDto.getC5Note());
        } else {
            entity.setC5Note(null);
        }

        entity.setC6(htsCaseDto.getC6());
        entity.setC7(htsCaseDto.getC7());
        if (htsCaseDto.getC8() != null && htsCaseDto.getC8() > 0) {
            LocalDateTime dob = LocalDateTime.of(htsCaseDto.getC8(), 6, 15, 0, 0);
            entity.setC8(dob);
        }

        Set<HTSCaseRiskGroup> c9 = new LinkedHashSet<>();
//    if (entity.getC9() != null && entity.getC9().size() > 0) {
//      for (HTSCaseRiskGroup htsCaseRiskGroup : entity.getC9()) {
//        htsCaseRiskGroupRepository.delete(htsCaseRiskGroup);
//      }
////			entity.getC9().clear();
//    }
        if (htsCaseDto.getC9() != null && htsCaseDto.getC9().size() > 0) {
            for (HTSCaseRiskGroupDto htsCaseRiskDto : htsCaseDto.getC9()) {
                HTSCaseRiskGroup rg = null;
                if (htsCaseRiskDto.getId() != null) {
                    rg = htsCaseRiskGroupRepository.findOne(htsCaseRiskDto.getId());
                }
                if (rg == null) {
                    rg = new HTSCaseRiskGroup();
                }
                rg.setHtsCase(entity);
                rg.setName(htsCaseRiskDto.getName());
                rg.setVal(htsCaseRiskDto.getVal());
                c9.add(rg);
            }
        }
        entity.getC9().clear();
        entity.getC9().addAll(c9);

        if (entity.getC9() != null && entity.getC9().size() > 0) {
            int minRisk = 16;
            HTSCaseRiskGroup htsCaseMinRiskGroup = null;
            for (HTSCaseRiskGroup htsCaseRiskGroup : entity.getC9()) {
                htsCaseRiskGroup.setIsMainRisk(false);
                if (htsCaseRiskGroup.getVal().getPriority() > 0 && htsCaseRiskGroup.getVal().getPriority() <= minRisk) {
                    minRisk = htsCaseRiskGroup.getVal().getPriority();
                    htsCaseMinRiskGroup = htsCaseRiskGroup;
                }
            }
            if (htsCaseMinRiskGroup != null) {
                htsCaseMinRiskGroup.setIsMainRisk(true);
            }
        }

        entity.setC9Note(htsCaseDto.getC9Note());
        entity.setC10(htsCaseDto.getC10());

        if (htsCaseDto.getC10() != null && htsCaseDto.getC10().getNumber() != 1) {
            entity.setC10Note(htsCaseDto.getC10Note());
        } else {
            entity.setC10Note(null);
        }

        entity.setC11(htsCaseDto.getC11());
        if (htsCaseDto.getC11() != null && htsCaseDto.getC11().getNumber() != 1) {
            entity.setC11a(htsCaseDto.getC11a());
            if (htsCaseDto.getC11a() != null && htsCaseDto.getC11a().getNumber() == 3) {
                entity.setC11aNote(htsCaseDto.getC11aNote());
            } else {
                entity.setC11aNote(null);
            }
            entity.setC11b(htsCaseDto.getC11b());
            entity.setC11c(htsCaseDto.getC11c());
            if (htsCaseDto.getC11c() != null && htsCaseDto.getC11c().getNumber() == 2) {
                entity.setC11cNote(htsCaseDto.getC11cNote());
            } else {
                entity.setC11cNote(null);
            }
        } else {
            entity.setC11a(null);
            entity.setC11aNote(null);
            entity.setC11b(null);
            entity.setC11c(null);
            entity.setC11cNote(null);
        }
        entity.setC12(htsCaseDto.getC12());

        if (htsCaseDto.getC12() != null && htsCaseDto.getC12().getNumber() == 3) {
            entity.setC131(htsCaseDto.getC131());
            entity.setC132(htsCaseDto.getC132());
        } else {
            entity.setC131(null);
            entity.setC132(null);
        }
        entity.setC14(htsCaseDto.getC14());

        if (htsCaseDto.getC14() != null) {
            if (htsCaseDto.getC14().getNumber() != 4) {
                entity.setC15(htsCaseDto.getC15());
                if (htsCaseDto.getC15() != null && htsCaseDto.getC15().getNumber() == 2) {
                    entity.setC15Date(htsCaseDto.getC15Date());
                } else {
                    entity.setC15Date(null);
                }
                if ((htsCaseDto.getC14().getNumber() == 1 && htsCaseDto.getC15().getNumber() == 2)
                        || (htsCaseDto.getC26() != null && htsCaseDto.getC26().getNumber() != 3
                        && htsCaseDto.getC14().getNumber() != 2)) {
                    entity.setC1627(htsCaseDto.getC1627());
                    if (htsCaseDto.getC1627() != null && htsCaseDto.getC1627().getNumber() == 2) {
                        entity.setC1627Note(htsCaseDto.getC1627Note());
                        entity.setC1627Date(htsCaseDto.getC1627Date());
                    } else {
                        entity.setC1627Note(null);
                        entity.setC1627Date(null);
                    }
                } else {
                    entity.setC1627(null);
                    entity.setC1627Note(null);
                    entity.setC1627Date(null);
                }
            } else {
                entity.setC15(null);
                entity.setC15Date(null);
            }
            if (htsCaseDto.getC14().getNumber() == 2) {
                entity.setC17(htsCaseDto.getC17());
                if (htsCaseDto.getC17() != null && htsCaseDto.getC17().getNumber() == 1) {
                    entity.setC18(htsCaseDto.getC18());
                } else {
                    entity.setC18(null);
                }
                entity.setC19(htsCaseDto.getC19());
                entity.setC19Date(htsCaseDto.getC19Date());
                entity.setC19Note(htsCaseDto.getC19Note());
                if (htsCaseDto.getC15() != null && htsCaseDto.getC15().getNumber() == 2) {
                    entity.setC20(htsCaseDto.getC20());
                    if (htsCaseDto.getC20().getNumber() == 2) {
                        entity.setC20Reason(htsCaseDto.getC20Reason());
                    } else {
                        entity.setC20Reason(null);
                    }
                    if (htsCaseDto.getC20().getNumber() == 3) {
                        entity.setC20Org(htsCaseDto.getC20Org());
                        entity.setC20RegDate(htsCaseDto.getC20RegDate());
                        entity.setC20Code(htsCaseDto.getC20Code());
                    } else {
                        entity.setC20Org(null);
                        entity.setC20RegDate(null);
                        entity.setC20Code(null);
                    }
                    entity.setC21(htsCaseDto.getC21());
                    if (htsCaseDto.getC21().getNumber() == 2) {
                        entity.setC21Date(htsCaseDto.getC21Date());
                        entity.setC22(htsCaseDto.getC22());
                    } else {
                        entity.setC21Date(null);
                        entity.setC22(null);
                    }
                    entity.setC23FullName(htsCaseDto.getC23FullName());
                    if (htsCaseDto.getC23Ethnic() != null && htsCaseDto.getC23Ethnic().getId() != null) {
                        entity.setC23Ethnic(dictionaryRepository.findOne(htsCaseDto.getC23Ethnic().getId()));
                    } else {
                        entity.setC23Ethnic(null);
                    }
                    if (htsCaseDto.getC23Profession() != null && htsCaseDto.getC23Profession().getId() != null) {
                        entity.setC23Profession(dictionaryRepository.findOne(htsCaseDto.getC23Profession().getId()));
                    } else {
                        entity.setC23Profession(null);
                    }
                    entity.setC23IdNumber(htsCaseDto.getC23IdNumber());
                    entity.setC23HealthNumber(htsCaseDto.getC23HealthNumber());
                    entity.setC23PhoneNumber(htsCaseDto.getC23PhoneNumber());
                    entity.setC23Note(htsCaseDto.getC23Note());
                    if (htsCaseDto.getC23ResidentAddressProvince() != null
                            && htsCaseDto.getC23ResidentAddressProvince().getId() != null) {
                        entity.setC23ResidentAddressProvince(
                                adminUnitRepository.findOne(htsCaseDto.getC23ResidentAddressProvince().getId()));
                    } else {
                        entity.setC23ResidentAddressProvince(null);
                    }
                    if (htsCaseDto.getC23ResidentAddressDistrict() != null
                            && htsCaseDto.getC23ResidentAddressDistrict().getId() != null) {
                        entity.setC23ResidentAddressDistrict(
                                adminUnitRepository.findOne(htsCaseDto.getC23ResidentAddressDistrict().getId()));
                    } else {
                        entity.setC23ResidentAddressDistrict(null);
                    }
                    if (htsCaseDto.getC23ResidentAddressCommune() != null
                            && htsCaseDto.getC23ResidentAddressCommune().getId() != null) {
                        entity.setC23ResidentAddressCommune(
                                adminUnitRepository.findOne(htsCaseDto.getC23ResidentAddressCommune().getId()));
                    } else {
                        entity.setC23ResidentAddressCommune(null);
                    }
                    entity.setC23ResidentAddressDetail(htsCaseDto.getC23ResidentAddressDetail());
                    entity.setC23ResidentAddressWard(htsCaseDto.getC23ResidentAddressWard());
                    if (htsCaseDto.getC23CurrentAddressProvince() != null
                            && htsCaseDto.getC23CurrentAddressProvince().getId() != null) {
                        entity.setC23CurrentAddressProvince(
                                adminUnitRepository.findOne(htsCaseDto.getC23CurrentAddressProvince().getId()));
                    } else {
                        entity.setC23CurrentAddressProvince(null);
                    }

                    if (htsCaseDto.getC23CurrentAddressDistrict() != null
                            && htsCaseDto.getC23CurrentAddressDistrict().getId() != null) {
                        entity.setC23CurrentAddressDistrict(
                                adminUnitRepository.findOne(htsCaseDto.getC23CurrentAddressDistrict().getId()));
                    } else {
                        entity.setC23CurrentAddressDistrict(null);
                    }

                    if (htsCaseDto.getC23CurrentAddressCommune() != null
                            && htsCaseDto.getC23CurrentAddressCommune().getId() != null) {
                        entity.setC23CurrentAddressCommune(
                                adminUnitRepository.findOne(htsCaseDto.getC23CurrentAddressCommune().getId()));
                    } else {
                        entity.setC23CurrentAddressCommune(null);
                    }
                    entity.setC23CurrentAddressWard(htsCaseDto.getC23CurrentAddressWard());
                    entity.setC23CurrentAddressDetail(htsCaseDto.getC23CurrentAddressDetail());
                    entity.setC24(htsCaseDto.getC24());

                } else {
                    entity.setC20(null);
                    entity.setC20Reason(null);
                    entity.setC20Org(null);
                    entity.setC20RegDate(null);
                    entity.setC20Code(null);
                    entity.setC21(null);
                    entity.setC21Date(null);
                    entity.setC22(null);
                    entity.setC23FullName(null);
                    entity.setC23Ethnic(null);
                    entity.setC23Profession(null);
                    entity.setC23IdNumber(null);
                    entity.setC23HealthNumber(null);
                    entity.setC23PhoneNumber(null);
                    entity.setC23Note(null);
                    entity.setC23ResidentAddressProvince(null);
                    entity.setC23ResidentAddressDistrict(null);
                    entity.setC23ResidentAddressCommune(null);
                    entity.setC23ResidentAddressDetail(null);
                    entity.setC23ResidentAddressWard(null);
                    entity.setC23ResidentAddressDetail(null);
                    entity.setC23ResidentAddressWard(null);
                    entity.setC23CurrentAddressProvince(null);
                    entity.setC23CurrentAddressDistrict(null);
                    entity.setC23CurrentAddressCommune(null);
                    entity.setC23CurrentAddressWard(null);
                    entity.setC23CurrentAddressDetail(null);
                    entity.setC24(null);
                }
            } else {
                entity.setC17(null);
                entity.setC18(null);
                entity.setC19(null);
                entity.setC19Date(null);
                entity.setC19Note(null);
                entity.setC20(null);
                entity.setC20Reason(null);
                entity.setC20Org(null);
                entity.setC20RegDate(null);
                entity.setC20Code(null);
                entity.setC21(null);
                entity.setC21Date(null);
                entity.setC22(null);
                entity.setC23FullName(null);
                entity.setC23Ethnic(null);
                entity.setC23Profession(null);
                entity.setC23IdNumber(null);
                entity.setC23HealthNumber(null);
                entity.setC23PhoneNumber(null);
                entity.setC23Note(null);
                entity.setC23ResidentAddressProvince(null);
                entity.setC23ResidentAddressDistrict(null);
                entity.setC23ResidentAddressCommune(null);
                entity.setC23ResidentAddressDetail(null);
                entity.setC23ResidentAddressWard(null);
                entity.setC23ResidentAddressDetail(null);
                entity.setC23ResidentAddressWard(null);
                entity.setC23CurrentAddressProvince(null);
                entity.setC23CurrentAddressDistrict(null);
                entity.setC23CurrentAddressCommune(null);
                entity.setC23CurrentAddressWard(null);
                entity.setC23CurrentAddressDetail(null);
                entity.setC24(null);
            }
        }
        entity.setC15Note(htsCaseDto.getC15Note());
        entity.setC26(htsCaseDto.getC26());
        if (htsCaseDto.getC26().getNumber() != 3 && htsCaseDto.getC26() != null) {
            entity.setC25(htsCaseDto.getC25());
        } else {
            entity.setC25(null);
        }
        entity.setCanNotComplete(htsCaseDto.getCanNotComplete());
        entity.setC28(htsCaseDto.getC28());
        entity.setTotalIsTestedHiv(htsCaseDto.getTotalIsTestedHiv());
        entity.setTotalHivPositiveResult(htsCaseDto.getTotalHivPositiveResult());
        entity.setNote(htsCaseDto.getNote());
        entity = htsCaseRepository.save(entity);

        if (entity != null) {
            return newHTSCaseDto(entity, currentUser, isSite, isProvince, isAdministrator, listUO, false);
        } else {
            throw new RuntimeException();
        }

    }

    @Override
    public ResponseDto<HTSCaseDto> deleteById(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        Boolean isSiteManagement = false;
        Boolean checkExist = SecurityUtils.isUserInRole(currentUser, "ROLE_SITE_MANAGER");
        if (checkExist) {
            isSiteManagement = true;
        }
        ResponseDto<HTSCaseDto> ret = new ResponseDto<HTSCaseDto>();
        final List<Long> writableOrgIds = authUtils.getGrantedOrgIds(Permission.WRITE_ACCESS);
        HTSCase entity = htsCaseRepository.findOne(id);
        if (entity != null) {
            if (entity.getC2() != null && !writableOrgIds.contains(entity.getC2().getId())) {
                if (!isSiteManagement) {
                    ret.setCode(YesNoNone.NO);
                    ret.setMessage("Bạn không có quyền xóa bản ghi này");
                    return ret;
                }
            }
            htsCaseRepository.delete(entity);
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
    public HTSCaseDto findById(long id) {
        if (!CommonUtils.isPositive(id, true)) {
            return null;
        }
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return null;
        }
        HTSCase entity = htsCaseRepository.findOne(id);
        if (entity != null) {
            Boolean isSite = SecurityUtils.isUserInRole(currentUser, "ROLE_SITE_MANAGER");
            Boolean isProvince = SecurityUtils.isUserInRole(currentUser, "ROLE_PROVINCIAL_MANAGER");
            Boolean isAdministrator = SecurityUtils.isUserInRole(currentUser, "ROLE_ADMIN");
            List<UserOrganization> listUO = null;
            if (!isAdministrator) {
                listUO = userOrganizationRepository.getListByUserId(currentUser.getId());
            }
            return newHTSCaseDto(entity, currentUser, isSite, isProvince, isAdministrator, listUO, false);
        } else {
            return null;
        }
    }

    private HTSCaseDto newHTSCaseDto(HTSCase entity, User currentUser, boolean isSite, boolean isProvince,
                                     boolean isAdministrator, List<UserOrganization> listUO, boolean isSimple) {
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
                        if (userOrganization.getOrganization() != null && entity.getC2() != null
                                && userOrganization.getOrganization().getId().equals(entity.getC2().getId())
                                && userOrganization.getHtsRole() != null && userOrganization.getHtsRole()) {
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
            return new HTSCaseDto(entity, isViewPII, isReadAble, isWritAble, isDeleteAble, isSimple);
        } else {
            return null;
        }
    }

    @Override
    public List<OrganizationDto> getListHTSWriteAble() {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return null;
        }
        List<OrganizationDto> ret = new ArrayList<OrganizationDto>();
        List<UserOrganization> listUO = userOrganizationRepository.getListByUserId(currentUser.getId());
        for (UserOrganization userOrganization : listUO) {
            if (userOrganization.getWriteAccess() != null && userOrganization.getWriteAccess()
                    && userOrganization.getHtsRole() != null && userOrganization.getHtsRole()) {
                ret.add(new OrganizationDto(userOrganization.getOrganization()));
            }
        }
        return ret;
    }

    @Override
    public SXSSFWorkbook exportHTSCase(PreventionFilterDto searchDto) {
        SXSSFWorkbook blankBook = new SXSSFWorkbook();
        blankBook.createSheet();
        searchDto.setDisablePaging(true);
//		filter.setIsFullDto(true);
        Page<HTSCaseDto> htsCaseDtos = this.findAllPageable(searchDto);
        if (htsCaseDtos == null) {
            return blankBook;
        } else {
            XSSFWorkbook wb = null;
            SXSSFWorkbook wbook = null;
            try (InputStream template = context.getResource("classpath:templates/hts-list.xlsx").getInputStream()) {
//				XSSFWorkbook tmp = new XSSFWorkbook(template);
//				Sheet sheet = tmp.getSheetAt(0);
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

            for (HTSCaseDto htsCaseDto : htsCaseDtos) {

                row = sheet.createRow(rowIndex++);

                // STT
                cell = row.createCell(colIndex++);
                cell.setCellValue(seq += 1);
                cell.setCellStyle(cellStyle);

                // Tỉnh/ thành phố org
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC2().getAddress().getProvince().getName());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // org
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC2().getName());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // Ho ten tu van vien
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC3().getFullName());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // Ma so khach hang
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC6());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // gioi tinh
                cell = row.createCell(colIndex++);
                try {
                    if (htsCaseDto.getC7().name().equals("MALE")) {
                        cell.setCellValue("Nam");
                    }
                    if (htsCaseDto.getC7().name().equals("FEMALE")) {
                        cell.setCellValue("Nữ");
                    }
                    if (htsCaseDto.getC7().name().equals("OTHER")) {
                        cell.setCellValue("Khác");
                    }
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // KQ XN HIV lần này
                cell = row.createCell(colIndex++);
                try {

                    cell.setCellValue(htsCaseDto.getC14().getDescription());

                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // Năm sinh
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC8());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // Tư vấn trước XN
                cell = row.createCell(colIndex++);
                cell.setCellStyle(dateTimeStyle);
                try {
                    cell.setCellValue(Date.from(htsCaseDto.getC4().toInstant(ZoneOffset.of("+7"))));
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                // Kết quả xác minh ca HIV dương tính
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC24().getDescription());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // KQ XN sàng lọc Giang mai
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC26().getDescription());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // BT mắc GM giới thiệu
                cell = row.createCell(colIndex++);
                try {
                    if (htsCaseDto.getC25().name().equals("YES")) {
                        cell.setCellValue("Có");
                    }
                    if (htsCaseDto.getC25().name().equals("NO")) {
                        cell.setCellValue("Không");
                    }
                    if (htsCaseDto.getC25().name().equals("NO_INFORMATION")) {
                        cell.setCellValue("Không có thông tin");
                    }
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // Quay lại nhận KQXN
                cell = row.createCell(colIndex++);
                cell.setCellStyle(dateTimeStyle);
                try {
                    cell.setCellValue(Date.from(htsCaseDto.getC15Date().toInstant(ZoneOffset.of("+7"))));
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                // Tư vấn sau XN
                cell = row.createCell(colIndex++);
                try {
                    if (htsCaseDto.getC15().name().equals("YES")) {
                        cell.setCellValue("Có");
                    }
                    if (htsCaseDto.getC15().name().equals("NO")) {
                        cell.setCellValue("Không");
                    }
                    if (htsCaseDto.getC15().name().equals("NO_INFORMATION")) {
                        cell.setCellValue("Không có thông tin");
                    }
                    // cell.setCellValue(htsCaseDto.getC15().name());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // Giới thiệu điều trị PrEP
                cell = row.createCell(colIndex++);
                try {
                    if (htsCaseDto.getC1627().name().equals("YES")) {
                        cell.setCellValue("Có");
                    }
                    if (htsCaseDto.getC1627().name().equals("NO")) {
                        cell.setCellValue("Không");
                    }
                    if (htsCaseDto.getC1627().name().equals("NO_INFORMATION")) {
                        cell.setCellValue("Không có thông tin");
                    }
                    // cell.setCellValue(htsCaseDto.getC1627().name());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // Tên cơ sở điều trị PrEP
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC1627Note());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // Ngày điều trị PrEP
                cell = row.createCell(colIndex++);
                cell.setCellStyle(dateTimeStyle);
                try {
                    cell.setCellValue(Date.from(htsCaseDto.getC1627Date().toInstant(ZoneOffset.of("+7"))));
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                // Nhận dịch vụ điều trị HIV
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC20().getDescription());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // Tên cơ sở điều trị HIV
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC20Org());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // Ngày điều trị HIV
                cell = row.createCell(colIndex++);
                cell.setCellStyle(dateTimeStyle);
                try {
                    cell.setCellValue(Date.from(htsCaseDto.getC20RegDate().toInstant(ZoneOffset.of("+7"))));
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                // Mã số điều trị
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC20Code());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // Lý do không điều trị HIV
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC20Reason());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // KQXN mới nhiễm HIV
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC17().getDescription());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // KQXN tải lượng vi-rút
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC18().getDescription());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // Loại hình dịch vụ TVXN HIV
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC5().getDescription());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // Ghi rõ cơ sở TVXN HIV
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC5Note().getDescription());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // Nhóm nguy cơ của khách hàng
                cell = row.createCell(colIndex++);
                try {
                    String c9 = "";
                    if (htsCaseDto.getC9() != null && htsCaseDto.getC9().size() > 0) {
                        for (HTSCaseRiskGroupDto risk : htsCaseDto.getC9()) {
                            if (c9.length() > 0) {
                                c9 += "," + risk.getName();
                            } else {
                                c9 += risk.getName();
                            }

                        }
                    }

                    cell.setCellValue(c9);

                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // Nguồn giới thiệu/chuyển gửi
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC10().getDescription());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // Nguồn giới thiệu/chuyển gửi
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC10Note());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // NĐã có KQXN phản ứng
                cell = row.createCell(colIndex++);
                try {
                    if (htsCaseDto.getC11().name().equals("YES")) {
                        cell.setCellValue("Có");
                    }
                    if (htsCaseDto.getC11().name().equals("NO")) {
                        cell.setCellValue("Không");
                    }
                    if (htsCaseDto.getC11().name().equals("NO_INFORMATION")) {
                        cell.setCellValue("Không có thông tin");
                    }
                    // cell.setCellValue(htsCaseDto.getC11().name());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // Có phản ứng với
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC11a().getDescription());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // NĐã có KQXN phản ứng
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC11aNote());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // Có phản ứng với
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC11b().getDescription());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // Có phản ứng với
                cell = row.createCell(colIndex++);
                try {
                    if (htsCaseDto.getC11c().name().equals("YES")) {
                        cell.setCellValue("Có");
                    }
                    if (htsCaseDto.getC11c().name().equals("NO")) {
                        cell.setCellValue("Không");
                    }
                    if (htsCaseDto.getC11c().name().equals("NO_INFORMATION")) {
                        cell.setCellValue("Không có thông tin");
                    }
                    // cell.setCellValue(htsCaseDto.getC11c().name());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // NĐã có KQXN phản ứng
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC11cNote());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // c12
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC12().getDescription());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // Có phản ứng với
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC131().getDescription());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // c12
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC132().getDescription());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // c132
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC19());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // Ng
                cell = row.createCell(colIndex++);
                cell.setCellStyle(dateTimeStyle);
                try {
                    cell.setCellValue(Date.from(htsCaseDto.getC19Date().toInstant(ZoneOffset.of("+7"))));
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                // c132
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC19Note());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // c21
                cell = row.createCell(colIndex++);
                try {
                    if (htsCaseDto.getC21().name().equals("YES")) {
                        cell.setCellValue("Có");
                    }
                    if (htsCaseDto.getC21().name().equals("NO")) {
                        cell.setCellValue("Không");
                    }
                    if (htsCaseDto.getC21().name().equals("NO_INFORMATION")) {
                        cell.setCellValue("Không có thông tin");
                    }
                    // cell.setCellValue(htsCaseDto.getC21().name());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // Ng
                cell = row.createCell(colIndex++);
                cell.setCellStyle(dateTimeStyle);
                try {
                    cell.setCellValue(Date.from(htsCaseDto.getC21Date().toInstant(ZoneOffset.of("+7"))));
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                // c22
                cell = row.createCell(colIndex++);
                try {
                    if (htsCaseDto.getC22().name().equals("YES")) {
                        cell.setCellValue("Có");
                    }
                    if (htsCaseDto.getC22().name().equals("NO")) {
                        cell.setCellValue("Không");
                    }
                    if (htsCaseDto.getC22().name().equals("NO_INFORMATION")) {
                        cell.setCellValue("Không có thông tin");
                    }
                    // cell.setCellValue(htsCaseDto.getC22().name());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // c23
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC23FullName());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // c23
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC23Ethnic().getValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // c23
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC23Profession().getValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // c23
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC23HealthNumber());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // c23
                cell = row.createCell(colIndex++);
                try {
//          if(htsCaseDto.getC23IdNumber() != null) {
                    cell.setCellValue(htsCaseDto.getC23IdNumber());
//          } else {
//            cell.setCellValue("");
//          }
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // c23
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC23PhoneNumber());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // c23
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC23ResidentAddressDetail());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // c23
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC23ResidentAddressWard());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // c23
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC23ResidentAddressDistrict().getName() + ", "
                            + htsCaseDto.getC23ResidentAddressProvince().getName());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // c23
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC23CurrentAddressDetail());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // c23
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC23CurrentAddressWard());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // c23
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC23CurrentAddressDistrict().getName() + ", "
                            + htsCaseDto.getC23CurrentAddressProvince().getName());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // c28
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC28().getDescription());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // c23
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getC23Note());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // Tổng số bạn tình, bạn chích đã làm xét nghiệm HIV
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getTotalIsTestedHiv());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // Tổng số bạn tình, bạn chích có KQXN HIV dương tính
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getTotalHivPositiveResult());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                // note
                cell = row.createCell(colIndex++);
                try {
                    cell.setCellValue(htsCaseDto.getNote());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                colIndex = 0;
            }
//      for (int i = 0; i < 60; i++) {
//    	    sheet.autoSizeColumn(i);
//      }
            return wbook;
        }
    }

//	@Override
//	public HTSCaseReportDto getReport(List<Long> orgIds, LocalDateTime from, LocalDateTime to) {
//		final List<Long> writableOrgIds = authUtils.getGrantedOrgIds(Permission.WRITE_ACCESS);
//		final List<Long> readableOrgIds = authUtils.getGrantedOrgIds(Permission.READ_ACCESS);
//
//		for (int i = 0; i < orgIds.size(); i++) {
//			if(!writableOrgIds.contains(orgIds.get(i)) && !readableOrgIds.contains(orgIds.get(i))) {
//				orgIds.remove(i);
//				i-=1;
//				if(orgIds.size()>0) {
//					continue;
//				}
//				else {
//					break;
//				}
//			}
//		}
//		if(orgIds.size()>0) {
//			String orgName="";
//			String provinceName="";
//			for (int i = 0; i < orgIds.size(); i++) {
//				Organization org = orgRepos.findOne(orgIds.get(i));
//				if(org!=null) {
//					orgName+=org.getName()+" ";
//					if(org.getAddress()!=null && org.getAddress().getProvince()!=null) {
//						provinceName+=org.getAddress().getProvince().getName();
//					}
//				}
//			}
//			SNSCaseReportDto ret = new SNSCaseReportDto();
//			ret.setFromDate(from);
//			ret.setToDate(to);
//			ret.setProvinceName(provinceName);
//			ret.setOrgName(orgName);
//
//
//			//Số người được mời làm cộng tác viên (Để trống)
//			SNSCaseReportDetailDto detail1 = this.getDetailReport(orgIds, from, to, 1);
//			detail1.setOrderNumber(1);
//			detail1.setName("Số người được mời làm cộng tác viên");
//			ret.getListDetail().add(detail1);
//
////			2	Số người đồng ý tham gia làm cộng tác viên
//			SNSCaseReportDetailDto detail2 = this.getDetailReport(orgIds, from, to, 2);
//			detail2.setOrderNumber(2);
//			detail2.setName("Số người đồng ý tham gia làm cộng tác viên");
//			ret.getListDetail().add(detail2);
//
////			3	Số thẻ chuyển gửi được phát ra
//			SNSCaseReportDetailDto detail3 = this.getDetailReport(orgIds, from, to, 3);
//			detail3.setOrderNumber(3);
//			detail3.setName("Số thẻ chuyển gửi được phát ra");
//			ret.getListDetail().add(detail3);
//
////			4	Số thẻ chuyển gửi quay lại cơ sở
//			SNSCaseReportDetailDto detail4 = this.getDetailReport(orgIds, from, to, 4);
//			detail4.setOrderNumber(4);
//			detail4.setName("Số thẻ chuyển gửi quay lại cơ sở");
//			ret.getListDetail().add(detail4);
//
////			5	Số khách hàng được làm XN HIV
//			SNSCaseReportDetailDto detail5 = this.getDetailReport(orgIds, from, to, 5);
//			detail5.setOrderNumber(5);
//			detail5.setName("Số khách hàng được làm XN HIV");
//			ret.getListDetail().add(detail5);
//
////			6
//			SNSCaseReportDetailDto detail6 = this.getDetailReport(orgIds, from, to, 6);
//			detail6.setOrderNumber(6);
//			detail6.setName("Số khách hàng có KQXN HIV dương tính");
//			ret.getListDetail().add(detail6);
//
////			7	Số khách hàng có KQXN HIV dương tính được kết nối điều trị ARV
//			SNSCaseReportDetailDto detail7= this.getDetailReport(orgIds, from, to, 7);
//			detail7.setOrderNumber(7);
//			detail7.setName("Số khách hàng có KQXN HIV dương tính được kết nối điều trị ARV");
//			ret.getListDetail().add(detail7);
//
////			8	Số khách hàng có KQXN HIV âm tính
//			SNSCaseReportDetailDto detail8 = this.getDetailReport(orgIds, from, to, 8);
//			detail8.setOrderNumber(8);
//			detail8.setName("Số khách hàng có KQXN HIV âm tính");
//			ret.getListDetail().add(detail8);
//
////			9	Số khách hàng có KQXN HIV âm tính được kết nối PrEP
//			SNSCaseReportDetailDto detail9 = this.getDetailReport(orgIds, from, to, 9);
//			detail9.setOrderNumber(9);
//			detail9.setName("Số khách hàng có KQXN HIV âm tính được kết nối PrEP");
//			ret.getListDetail().add(detail9);
//			return ret;
//		}
//		else {
//			return null;
//		}
//		return null;
//	}

    @Override
    @Transactional(readOnly = true)
    public Workbook exportReportHTSCase(PreventionFilterDto filter) {
        Workbook blankBook = new XSSFWorkbook();
        blankBook.createSheet();
        if (filter.getProvinceId() != null && CollectionUtils.isEmpty(filter.getOrgIds())) {
            filter.setOrgIds(organizationRepository.findAllIdByProvinceId(filter.getProvinceId()));
        }
        PreventionReportDto<HTSReportDetailsDto> result = this.getReportDetail(filter);
//		HTSCaseReportDto htsCaseReportDto = this.getReport(filter.getOrgIds(), filter.getFromDate(), filter.getToDate());
//		if (htsCaseReportDto == null || htsCaseReportDto.getListDetail().size() == 0) {
//			return blankBook;
//		}
//		else {
        XSSFWorkbook wbook = null;
//		SXSSFWorkbook wbook = null;
        try (InputStream template = context.getResource("classpath:templates/hts-report.xlsx").getInputStream()) {
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
        int rowIndex = 9;
        int colIndex = 0;

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
//
        row = sheet.getRow(3);

        cell = row.getCell(3);
        cell.setCellValue(result.getOrgName());

        cell = row.getCell(13);
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
        cell = row.getCell(13);
        cell.setCellValue(result.getDistrictName());

        for (HTSReportDetailsDto detail : result.getListDetail()) {
            if (rowIndex == 16 || rowIndex == 21 || rowIndex == 27 || rowIndex == 32) {
                rowIndex++;
            }
            row = sheet.getRow(rowIndex++);
            if (rowIndex == 23 || rowIndex == 24 || rowIndex == 25 || rowIndex == 26 || rowIndex == 27 || rowIndex == 29
                    || rowIndex == 30 || rowIndex == 32 || rowIndex == 40 || rowIndex == 41) {
                cell = row.getCell(7);
                cell.setCellValue("");
                cell = row.getCell(8);
                cell.setCellValue(detail.getVtc());
                cell = row.getCell(9);
                cell.setCellValue(detail.getPknt());
                cell = row.getCell(10);
                cell.setCellValue(detail.getMethadone());
                cell = row.getCell(11);
                cell.setCellValue(detail.getPrep());
                cell = row.getCell(12);
                cell.setCellValue(detail.getPrison());
                cell = row.getCell(13);
                cell.setCellValue(detail.getOtherOrg());
                cell = row.getCell(14);
                cell.setCellValue(detail.getDepartment());
                cell = row.getCell(15);
                cell.setCellValue(detail.getSns());
                cell = row.getCell(16);
                cell.setCellValue(detail.getTotal());
            } else {
                cell = row.getCell(7);
                cell.setCellValue(detail.getCommunity());
                cell = row.getCell(8);
                cell.setCellValue(detail.getVtc());
                cell = row.getCell(9);
                cell.setCellValue(detail.getPknt());
                cell = row.getCell(10);
                cell.setCellValue(detail.getMethadone());
                cell = row.getCell(11);
                cell.setCellValue(detail.getPrep());
                cell = row.getCell(12);
                cell.setCellValue(detail.getPrison());
                cell = row.getCell(13);
                cell.setCellValue(detail.getOtherOrg());
                cell = row.getCell(14);
                cell.setCellValue(detail.getDepartment());
                cell = row.getCell(15);
                cell.setCellValue(detail.getSns());
                cell = row.getCell(16);
                cell.setCellValue(detail.getTotal());
            }
        }
        return wbook;

    }

    @Override
    @Transactional(readOnly = true)
    public Workbook exportReportSTICase(PreventionFilterDto filter) {
        Workbook blankBook = new XSSFWorkbook();
        blankBook.createSheet();
        if (filter.getProvinceId() != null && CollectionUtils.isEmpty(filter.getOrgIds())) {
            filter.setOrgIds(organizationRepository.findAllIdByProvinceId(filter.getProvinceId()));
        }
        PreventionReportDto<STIDetailReportDto> preventionReportDto = this.getSTIReport(filter);
        if (preventionReportDto == null || preventionReportDto.getListDetail().size() == 0) {
            return blankBook;
        } else {
            XSSFWorkbook wbook = null;
//			SXSSFWorkbook wbook = null;
            try (InputStream template = context.getResource("classpath:templates/sti-report.xlsx").getInputStream()) {
//				XSSFWorkbook tmp = new XSSFWorkbook(template);
//				Sheet sheet = tmp.getSheetAt(0);

                wbook = new XSSFWorkbook(template);
//				wbook = new SXSSFWorkbook(wb);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (wbook == null) {
                return blankBook;
            }
            int rowIndex = 9;

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

            // Tên cơ sở báo cáo
            row = sheet.getRow(3);
            cell = row.getCell(2);
            cell.setCellValue(preventionReportDto.getOrgName());

            // Tỉnh/Thành phố
            cell = row.getCell(12);
            cell.setCellValue(preventionReportDto.getProvinceName());

            // Kỳ báo cáo
            row = sheet.getRow(4);
            cell = row.getCell(2);
            if (preventionReportDto.getFromDate() != null) {
                cell.setCellValue(
                        formatter.format(Date.from(preventionReportDto.getFromDate().toInstant(ZoneOffset.of("+7")))));
            }
            cell = row.getCell(5);
            if (preventionReportDto.getToDate() != null) {
                cell.setCellValue(
                        formatter.format(Date.from(preventionReportDto.getToDate().toInstant(ZoneOffset.of("+7")))));
            }

            // Quận/huyện
            cell = row.getCell(12);
            cell.setCellValue(preventionReportDto.getDistrictName());

            for (STIDetailReportDto detail : preventionReportDto.getListDetail()) {
                if (rowIndex == 14) {
                    rowIndex += 4;
                }

                row = sheet.getRow(rowIndex++);
//				row.setHeightInPoints(22);

                // Khách hàng làm xét nghiệm HIV
                // Âm tính
                cell = row.getCell(3);
                if (detail.getNegativeHTS() != null) {
                    cell.setCellValue(detail.getNegativeHTS());
                }

                // Khẳng định dương tính
                cell = row.getCell(5);
                if (detail.getPositiveHTS() != null) {
                    cell.setCellValue(detail.getPositiveHTS());
                }

                // Không xác định
                cell = row.getCell(7);
                if (detail.getUnknownHTS() != null) {
                    cell.setCellValue(detail.getUnknownHTS());
                }

                // Tổng cộng
                cell = row.getCell(9);
                if (detail.getNegativeHTS() != null && detail.getPositiveHTS() != null
                        && detail.getUnknownHTS() != null) {
                    cell.setCellValue(detail.getNegativeHTS() + detail.getPositiveHTS() + detail.getUnknownHTS());
                }

                // Khách hàng không làm xét nghiệm HIV
                if (rowIndex != 13 && rowIndex != 22) {
                    cell = row.getCell(11);
                    if (detail.getNotHTS() != null) {
                        cell.setCellValue(detail.getNotHTS());
                    }
                }

                // TỔNG CỘNG
                cell = row.getCell(13);
                if (row.getCell(9) != null && row.getCell(11) != null) {
                    cell.setCellValue(row.getCell(9).getNumericCellValue() + row.getCell(11).getNumericCellValue());
                }
            }
            return wbook;
        }
    }

    @Override
    public PreventionReportDto<STIDetailReportDto> getSTIReport(PreventionFilterDto filter) {
        if (filter != null && filter.getFromDate() != null && filter.getToDate() != null
                && filter.getFromDate().isBefore(filter.getToDate()) && filter.getOrgIds() != null
                && filter.getOrgIds().size() > 0) {

            User currentUser = SecurityUtils.getCurrentUser();
            Boolean isAdministrator = SecurityUtils.isUserInRole(currentUser, "ROLE_ADMIN");

            PreventionReportDto<STIDetailReportDto> ret = new PreventionReportDto<STIDetailReportDto>();
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

//			Khách hàng làm xét nghiệm sàng lọc Giang mai
            STIDetailReportDto detail = this.getSTIReportDetail(filter, "Khách hàng làm xét nghiệm sàng lọc Giang mai",
                    9);
            ret.getListDetail().add(detail);
//			Âm tính
            detail = this.getSTIReportDetail(filter, "Âm tính", 10);
            ret.getListDetail().add(detail);
//			Dương tính
            detail = this.getSTIReportDetail(filter, "Dương tính", 11);
            ret.getListDetail().add(detail);
//			Khách hàng không làm xét nghiệm sàng lọc Giang mai
            detail = this.getSTIReportDetail(filter, "Khách hàng không làm xét nghiệm sàng lọc Giang mai", 12);
            ret.getListDetail().add(detail);
//			TỔNG CỘNG
            detail = this.getSTIReportDetail(filter, "TỔNG CỘNG", 13);
            ret.getListDetail().add(detail);

//			Khách hàng làm xét nghiệm sàng lọc Giang mai
            detail = this.getSTIReportDetail(filter, "Khách hàng làm xét nghiệm sàng lọc Giang mai", 18);
            ret.getListDetail().add(detail);
//			Âm tính
            detail = this.getSTIReportDetail(filter, "Âm tính", 19);
            ret.getListDetail().add(detail);
//			Dương tính
            detail = this.getSTIReportDetail(filter, "Dương tính", 20);
            ret.getListDetail().add(detail);
//			Khách hàng không làm xét nghiệm sàng lọc Giang mai
            detail = this.getSTIReportDetail(filter, "Khách hàng không làm xét nghiệm sàng lọc Giang mai", 21);
            ret.getListDetail().add(detail);
//			TỔNG CỘNG
            detail = this.getSTIReportDetail(filter, "TỔNG CỘNG", 22);
            ret.getListDetail().add(detail);
            return ret;
        }
        return null;
    }

    public STIDetailReportDto getSTIReportDetail(PreventionFilterDto filter, String rowName, int orderNumber) {
        STIDetailReportDto detail = new STIDetailReportDto();
        detail.setTitleSTI(rowName);
        detail.setOrderNumber(orderNumber);
//			Khách hàng làm xét nghiệm HIV
//			Âm tính
        Integer negativeHTS = 0;
        negativeHTS = this.querySTIReport(filter, rowName, 1, orderNumber).intValue();
        detail.setNegativeHTS(negativeHTS);

//			Khẳng định dương tính
        Integer positiveHTS = 0;
        positiveHTS = this.querySTIReport(filter, rowName, 2, orderNumber).intValue();
        detail.setPositiveHTS(positiveHTS);

//			Không xác định
        Integer unknownHTS = 0;
        unknownHTS = this.querySTIReport(filter, rowName, 3, orderNumber).intValue();
        detail.setUnknownHTS(unknownHTS);

//			Khách hàng không làm xét nghiệm HIV
        Integer notHTS = 0;
        notHTS = this.querySTIReport(filter, rowName, 5, orderNumber).intValue();
        detail.setNotHTS(notHTS);

        return detail;
    }

    public Integer querySTIReport(PreventionFilterDto filter, String row, int col, int orderNumber) {
        String SQL = " SELECT COUNT(s.id) from HTSCase s WHERE s.c2.id in (:listOrg) ";
        String whereClause = "";

//		Khách hàng làm xét nghiệm HIV
//		Âm tính
        if (col == 1) {
            if (orderNumber == 9 || orderNumber == 18) {
                whereClause += " AND s.c4 >=:fromDate AND s.c4<=:toDate AND s.c14=:c14Answer1 ";
                whereClause += " AND (s.c26=:c26Answer1 OR s.c26=:c26Answer2) ";
            } else if (orderNumber == 13 || orderNumber == 22) {
                whereClause += " AND s.c4 >=:fromDate AND s.c4<=:toDate AND s.c14=:c14Answer1 ";
                whereClause += " AND (s.c26=:c26Answer1 OR s.c26=:c26Answer2 OR s.c26=:c26Answer3) ";
            }
            if (orderNumber < 18) {
                whereClause += " AND s.c4 >=:fromDate AND s.c4<=:toDate AND s.c14=:c14Answer1 ";
            } else {
                whereClause += " AND s.c4 >=:fromDate AND s.c4<=:toDate AND s.c14=:c14Answer1 ";
                whereClause += " AND s.id in ( SELECT r.htsCase.id FROM HTSCaseRiskGroup r WHERE r.val=:c9Answer2) ";
            }
//			Âm tính
            if ("Âm tính".equals(row)) {
                whereClause += " AND s.c26=:c26Answer1 ";
            }
//			Dương tính
            if ("Dương tính".equals(row)) {
                whereClause += " AND s.c26=:c26Answer2 ";
            }
//			Khách hàng không làm xét nghiệm sàng lọc Giang mai
            if ("Khách hàng không làm xét nghiệm sàng lọc Giang mai".equals(row)) {
                whereClause += " AND s.c26=:c26Answer3 ";
            }
        }

//		Khách hàng làm xét nghiệm HIV
// 		Khẳng định dương tính
        if (col == 2) {
            if (orderNumber == 9 || orderNumber == 18) {
                whereClause += " AND s.c4 >=:fromDate AND s.c4<=:toDate AND s.c14=:c14Answer2 ";
                whereClause += " AND (s.c26=:c26Answer1 OR s.c26=:c26Answer2) ";
            } else if (orderNumber == 13 || orderNumber == 22) {
                whereClause += " AND s.c4 >=:fromDate AND s.c4<=:toDate AND s.c14=:c14Answer2 ";
                whereClause += " AND (s.c26=:c26Answer1 OR s.c26=:c26Answer2 OR s.c26=:c26Answer3) ";
            }
            if (orderNumber < 18) {
                whereClause += " AND s.c4 >=:fromDate AND s.c4<=:toDate AND s.c14=:c14Answer2 ";
            } else {
                whereClause += " AND s.c4 >=:fromDate AND s.c4<=:toDate AND s.c14=:c14Answer2 ";
                whereClause += " AND s.id in ( SELECT r.htsCase.id FROM HTSCaseRiskGroup r WHERE r.val=:c9Answer2) ";
            }

//			Âm tính
            if ("Âm tính".equals(row)) {
                whereClause += " AND s.c26=:c26Answer1 ";
            }
//			Dương tính
            if ("Dương tính".equals(row)) {
                whereClause += " AND s.c26=:c26Answer2 ";
            }
//			Khách hàng không làm xét nghiệm sàng lọc Giang mai
            if ("Khách hàng không làm xét nghiệm sàng lọc Giang mai".equals(row)) {
                whereClause += " AND s.c26=:c26Answer3 ";
            }
        }

//		Khách hàng làm xét nghiệm HIV
// 		Không xác định
        if (col == 3) {
            if (orderNumber == 9 || orderNumber == 18) {
                whereClause += " AND s.c4 >=:fromDate AND s.c4<=:toDate AND s.c14=:c14Answer3 ";
                whereClause += " AND (s.c26=:c26Answer1 OR s.c26=:c26Answer2) ";
            } else if (orderNumber == 13 || orderNumber == 22) {
                whereClause += " AND s.c4 >=:fromDate AND s.c4<=:toDate AND s.c14=:c14Answer3 ";
                whereClause += " AND (s.c26=:c26Answer1 OR s.c26=:c26Answer2 OR s.c26=:c26Answer3) ";
            }
            if (orderNumber < 18) {
                whereClause += " AND s.c4 >=:fromDate AND s.c4<=:toDate AND s.c14=:c14Answer3 ";
            } else {
                whereClause += " AND s.c4 >=:fromDate AND s.c4<=:toDate AND s.c14=:c14Answer3 ";
                whereClause += " AND s.id in ( SELECT r.htsCase.id FROM HTSCaseRiskGroup r WHERE r.val=:c9Answer2) ";
            }

//			Âm tính
            if ("Âm tính".equals(row)) {
                whereClause += " AND s.c26=:c26Answer1 ";
            }
//			Dương tính
            if ("Dương tính".equals(row)) {
                whereClause += " AND s.c26=:c26Answer2 ";
            }
//			Khách hàng không làm xét nghiệm sàng lọc Giang mai
            if ("Khách hàng không làm xét nghiệm sàng lọc Giang mai".equals(row)) {
                whereClause += " AND s.c26=:c26Answer3 ";
            }
        }

//		Khách hàng làm xét nghiệm HIV
//		Tổng cộng
//		if(col==4) {
//
//		}

//		Khách hàng không làm xét nghiệm HIV
// 		Không xác định
        if (col == 5) {
            if (orderNumber == 9 || orderNumber == 18) {
                whereClause += " AND s.c4 >=:fromDate AND s.c4<=:toDate AND s.c14=:c14Answer4 ";
                whereClause += " AND (s.c26=:c26Answer1 OR s.c26=:c26Answer2) ";
            } else if (orderNumber == 13 || orderNumber == 22) {
                whereClause += " AND s.c4 >=:fromDate AND s.c4<=:toDate AND s.c14=:c14Answer4 ";
                whereClause += " AND (s.c26=:c26Answer1 OR s.c26=:c26Answer2 OR s.c26=:c26Answer3) ";
            }
            if (orderNumber < 18) {
                whereClause += " AND s.c4 >=:fromDate AND s.c4<=:toDate AND s.c14=:c14Answer4 ";
            } else {
                whereClause += " AND s.c4 >=:fromDate AND s.c4<=:toDate AND s.c14=:c14Answer4 ";
                whereClause += " AND s.id in ( SELECT r.htsCase.id FROM HTSCaseRiskGroup r WHERE r.val=:c9Answer2) ";
            }

//			Âm tính
            if ("Âm tính".equals(row)) {
                whereClause += " AND s.c26=:c26Answer1 ";
            }
//			Dương tính
            if ("Dương tính".equals(row)) {
                whereClause += " AND s.c26=:c26Answer2 ";
            }
//			Khách hàng không làm xét nghiệm sàng lọc Giang mai
//			null
        }

        Query q = manager.createQuery(SQL + whereClause);
        q.setParameter("listOrg", filter.getOrgIds());

        q.setParameter("fromDate", filter.getFromDate());
        q.setParameter("toDate", filter.getToDate());
        // Khách hàng làm xét nghiệm HIV
        // Âm tính

        if (col == 1) {
            if (orderNumber < 18) {
                q.setParameter("c14Answer1", HTSc14.answer1);
            } else {
                q.setParameter("c14Answer1", HTSc14.answer1);
                q.setParameter("c9Answer2", HTSRiskGroupEnum.answer2);
            }

//			Khách hàng làm xét nghiệm sàng lọc Giang mai
            if (row.equals("Khách hàng làm xét nghiệm sàng lọc Giang mai")) {
                q.setParameter("c14Answer1", HTSc14.answer1);
                q.setParameter("c26Answer1", HTSc26.answer1);
                q.setParameter("c26Answer2", HTSc26.answer2);
            }
//			Âm tính
            if (row.equals("Âm tính")) {
                q.setParameter("c26Answer1", HTSc26.answer1);
            }
//			Dương tính
            if (row.equals("Dương tính")) {
                q.setParameter("c26Answer2", HTSc26.answer2);
            }
//			Khách hàng không làm xét nghiệm sàng lọc Giang mai
            if (row.equals("Khách hàng không làm xét nghiệm sàng lọc Giang mai")) {
                q.setParameter("c26Answer3", HTSc26.answer3);
            }
//			TỔNG CỘNG
            if (row.equals("TỔNG CỘNG")) {
                q.setParameter("c26Answer1", HTSc26.answer1);
                q.setParameter("c26Answer2", HTSc26.answer2);
                q.setParameter("c26Answer3", HTSc26.answer3);
            }
        }

        // Khách hàng làm xét nghiệm HIV
        // Khẳng định dương tính
        if (col == 2) {
            if (orderNumber < 18) {
                q.setParameter("c14Answer2", HTSc14.answer2);
            } else {
                q.setParameter("c14Answer2", HTSc14.answer2);
                q.setParameter("c9Answer2", HTSRiskGroupEnum.answer2);
            }

//			Khách hàng làm xét nghiệm sàng lọc Giang mai
            if (row.equals("Khách hàng làm xét nghiệm sàng lọc Giang mai")) {
                q.setParameter("c14Answer2", HTSc14.answer2);
                q.setParameter("c26Answer1", HTSc26.answer1);
                q.setParameter("c26Answer2", HTSc26.answer2);
            }

//			Âm tính
            if (row.equals("Âm tính")) {
                q.setParameter("c26Answer1", HTSc26.answer1);
            }
//			Dương tính
            if (row.equals("Dương tính")) {
                q.setParameter("c26Answer2", HTSc26.answer2);
            }
//			Khách hàng không làm xét nghiệm sàng lọc Giang mai
            if (row.equals("Khách hàng không làm xét nghiệm sàng lọc Giang mai")) {
                q.setParameter("c26Answer3", HTSc26.answer3);
            }
//			TỔNG CỘNG
            if (row.equals("TỔNG CỘNG")) {
                q.setParameter("c26Answer1", HTSc26.answer1);
                q.setParameter("c26Answer2", HTSc26.answer2);
                q.setParameter("c26Answer3", HTSc26.answer3);
            }
        }

        // Khách hàng làm xét nghiệm HIV
        // Không xác định
        if (col == 3) {
            if (orderNumber < 18) {
                q.setParameter("c14Answer3", HTSc14.answer3);
            } else {
                q.setParameter("c14Answer3", HTSc14.answer3);
                q.setParameter("c9Answer2", HTSRiskGroupEnum.answer2);
            }

//			Khách hàng làm xét nghiệm sàng lọc Giang mai
            if (row.equals("Khách hàng làm xét nghiệm sàng lọc Giang mai")) {
                q.setParameter("c14Answer3", HTSc14.answer3);
                q.setParameter("c26Answer1", HTSc26.answer1);
                q.setParameter("c26Answer2", HTSc26.answer2);
            }

//			Âm tính
            if (row.equals("Âm tính")) {
                q.setParameter("c26Answer1", HTSc26.answer1);
            }
//			Dương tính
            if (row.equals("Dương tính")) {
                q.setParameter("c26Answer2", HTSc26.answer2);
            }
//			Khách hàng không làm xét nghiệm sàng lọc Giang mai
            if (row.equals("Khách hàng không làm xét nghiệm sàng lọc Giang mai")) {
                q.setParameter("c26Answer3", HTSc26.answer3);
            }
//			TỔNG CỘNG
            if (row.equals("TỔNG CỘNG")) {
                q.setParameter("c26Answer1", HTSc26.answer1);
                q.setParameter("c26Answer2", HTSc26.answer2);
                q.setParameter("c26Answer3", HTSc26.answer3);
            }
        }

        // Khách hàng không làm xét nghiệm HIV
        if (col == 5) {
            if (orderNumber < 18) {
                q.setParameter("c14Answer4", HTSc14.answer4);
            } else {
                q.setParameter("c14Answer4", HTSc14.answer4);
                q.setParameter("c9Answer2", HTSRiskGroupEnum.answer2);
            }

            // Khách hàng làm xét nghiệm sàng lọc Giang mai
            if (row.equals("Khách hàng làm xét nghiệm sàng lọc Giang mai")) {
                q.setParameter("c14Answer4", HTSc14.answer4);
                q.setParameter("c26Answer1", HTSc26.answer1);
                q.setParameter("c26Answer2", HTSc26.answer2);
            }

//			Âm tính
            if (row.equals("Âm tính")) {
                q.setParameter("c26Answer1", HTSc26.answer1);
            }
//			Dương tính
            if (row.equals("Dương tính")) {
                q.setParameter("c26Answer2", HTSc26.answer2);
            }
//			TỔNG CỘNG
            if (row.equals("TỔNG CỘNG")) {
                q.setParameter("c26Answer1", HTSc26.answer1);
                q.setParameter("c26Answer2", HTSc26.answer2);
                q.setParameter("c26Answer3", HTSc26.answer3);
            }
        }

        Long ret = (Long) q.getSingleResult();
        if (ret != null) {
            return ret.intValue();
        }
        return 0;
    }

    @Override
    public PreventionCheckCodeDto checkDuplicateCode(PreventionCheckCodeDto dto) {
        if (dto != null && StringUtils.hasText(dto.getCode()) && dto.getOrgId() != null && dto.getOrgId() > 0L) {
            String SQL = " SELECT COUNT(s.id) FROM HTSCase s WHERE s.c6=:code AND s.c2.id=:orgId ";
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
    public HTSCaseDto updateC24(HTSCaseDto dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        Boolean isSite = SecurityUtils.isUserInRole(currentUser, "ROLE_SITE_MANAGER");
        Boolean isProvince = SecurityUtils.isUserInRole(currentUser, "ROLE_PROVINCIAL_MANAGER");
        Boolean isAdministrator = SecurityUtils.isUserInRole(currentUser, "ROLE_ADMIN");
        List<UserOrganization> listUO = null;
        if (!isAdministrator) {
            listUO = userOrganizationRepository.getListByUserId(currentUser.getId());
        }

        if (!isSite && !isProvince && isAdministrator) {
            return null;
        }

        final List<Long> writableOrgIds = authUtils.getGrantedOrgIds(Permission.WRITE_ACCESS);
        if (writableOrgIds == null || writableOrgIds.size() == 0) {
            return null;
        }
        if (dto != null && dto.getId() != null && dto.getId() > 0L) {
            HTSCase entity = htsCaseRepository.findOne(dto.getId());
            if (entity != null && entity.getC2() != null) {
                if (!writableOrgIds.contains(entity.getC2().getId())) {
                    return null;
                }
                if (!DateTimeUtil.checkEditableByMonth(Constants.NUMBER_OF_MONTH_FOR_EDIT,entity.getC4()) ){
                    HTSCaseDto rs = new HTSCaseDto();
                    rs.setEditAble(false);
                    return  rs;
                }
                if(dto.getCanNotComplete()==true && entity.getC2().getCanNotBeCompleted()!=null && entity.getC2().getCanNotBeCompleted()){
                    entity.setCanNotComplete(true);
                }else{
                    entity.setC24(dto.getC24());
                    entity.setCanNotComplete(false);
                }
                entity = htsCaseRepository.save(entity);
                return newHTSCaseDto(entity, currentUser, isSite, isProvince, isAdministrator, listUO, false);

//        return new HTSCaseDto(entity, isSiteManagement);
            }
        }
        return null;
    }

    @Override
    public ImportResultDto<HTSCaseDto> importFromExcel(InputStream is) throws IOException {
        Workbook workbook = new XSSFWorkbook(is);
        Sheet datatypeSheet = workbook.getSheetAt(0);
        // Iterator<Row> iterator = datatypeSheet.iterator();
        int rowIndex = 1;
        int num = datatypeSheet.getLastRowNum();
        ImportResultDto<HTSCaseDto> ret = new ImportResultDto<HTSCaseDto>();
        while (rowIndex <= num) {
            try {
                System.out.println(rowIndex);
                Row currentRow = datatypeSheet.getRow(rowIndex);
                Cell currentCell = null;
                if (currentRow != null) {
                    HTSCaseDto dto = new HTSCaseDto();
                    String err = "";
                    try {
//						c1
                        currentCell = currentRow.getCell(0);
                        if (currentCell != null) {
                            if (currentCell.getCellType() == CellType.STRING) {
                                UUID uid = UUID.fromString(currentCell.getStringCellValue());
                                dto.setUid(uid);
                            }
                        }
                        if (dto.getUid() == null) {
                            rowIndex += 1;
                            continue;
                        }
                    } catch (Exception e) {
                        dto.setUid(null);
                        err += "C1 - Không rõ; ";
                    }
                    try {
//						c2
                        currentCell = currentRow.getCell(1);
                        if (currentCell != null) {
                            String orgCode = "";
                            if (currentCell.getCellType() == CellType.STRING) {
                                orgCode = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                orgCode = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            if (!orgCode.equals("")) {
                                Organization c1OrgId = organizationRepository.findByOrgCode(orgCode);
                                if (c1OrgId != null) {
                                    dto.setC2(new OrganizationDto(c1OrgId));
                                } else {
                                    dto.setC2(null);
                                    err += "C2 - Không tìm thấy cơ sở báo cáo; ";
                                }
                            }
                        }
                    } catch (Exception e) {
                        dto.setC2(null);
                        err += "C2 - Không rõ; ";
                    }
                    try {
//						c3
                        currentCell = currentRow.getCell(2);
                        if (currentCell != null) {
                            String staffCode = "";
                            if (currentCell.getCellType() == CellType.STRING) {
                                staffCode = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                staffCode = String
                                        .valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            if (!staffCode.equals("")) {
                                Staff staff = staffRepository.findByStaffCode(staffCode);
                                if (staff != null) {
                                    dto.setC3(new StaffDto(staff, true));
                                } else {
                                    dto.setC3(null);
                                    err += "C3 - Không tìm thấy nhân viên; ";
                                }
                            }
                        }
                    } catch (Exception e) {
                        dto.setC3(null);
                        err += "C3 - Không rõ; ";
                    }
                    try {
//						c4
                        currentCell = currentRow.getCell(3);
                        if (currentCell != null) {
                            LocalDateTime c4CounsellingDate = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                                c4CounsellingDate = LocalDateTime.parse(currentCell.getStringCellValue(), formatter);
                            } else if (currentCell.getCellType() == CellType.NUMERIC
                                    && currentCell.getDateCellValue() != null) {
                                c4CounsellingDate = currentCell.getDateCellValue().toInstant()
                                        .atZone(ZoneId.systemDefault()).toLocalDateTime();
                            }
                            dto.setC4(c4CounsellingDate);
                        }
                    } catch (Exception e) {
                        dto.setC4(null);
                        err += "C4 - Không rõ; ";
                    }
                    try {
//						c5
                        currentCell = currentRow.getCell(4);
                        if (currentCell != null) {
                            String c5 = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c5 = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c5 = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            if (c5 != null) {
                                if (c5.trim().equals("48")) {
                                    dto.setC5(HTSc5.answer1);
                                } else if (c5.trim().equals("49")) {
                                    dto.setC5(HTSc5.answer2);
                                } else {
                                    dto.setC5(null);
                                    err += "C5 không đúng (C5 phải bằng 48 hoặc 49); ";
                                }
                            } else {
                                dto.setC5(null);
                            }
                        }
                    } catch (Exception e) {
                        dto.setC5(null);
                        err += "C5 - Không rõ; ";
                    }
                    try {
//						c6 - c5_note
                        currentCell = currentRow.getCell(5);
                        if (currentCell != null) {
                            String c5Note = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c5Note = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c5Note = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            if (c5Note != null) {
                                if (c5Note.equals("151")) {
                                    dto.setC5Note(HTSc5Note.answer1);
                                } else if (c5Note.equals("152")) {
                                    dto.setC5Note(HTSc5Note.answer2);
                                } else if (c5Note.equals("153")) {
                                    dto.setC5Note(HTSc5Note.answer3);
                                } else if (c5Note.equals("154")) {
                                    dto.setC5Note(HTSc5Note.answer4);
                                } else if (c5Note.equals("155")) {
                                    dto.setC5Note(HTSc5Note.answer5);
                                } else if (c5Note.equals("156")) {
                                    dto.setC5Note(HTSc5Note.answer6);
                                } else {
                                    dto.setC5Note(null);
                                    err += "C6 không đúng (C6 phải lớn hơn 150 và nhỏ hơn 157); ";
                                }
                            } else {
                                dto.setC5Note(null);
                            }
                        }
                    } catch (Exception e) {
                        dto.setC5Note(null);
                        err += "C6 - Không rõ; ";
                    }
                    try {
//						c7 - c6_client_code
                        currentCell = currentRow.getCell(6);
                        if (currentCell != null) {
                            String c6ClientCode = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c6ClientCode = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c6ClientCode = String
                                        .valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            dto.setC6(c6ClientCode);
                        }
                    } catch (Exception e) {
                        dto.setC6(null);
                        err += "C7 - Không rõ; ";
                    }
                    try {
//						c8 - c7_gender
                        currentCell = currentRow.getCell(7);
                        if (currentCell != null) {
                            String c7Gender = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c7Gender = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c7Gender = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            if (c7Gender != null) {
                                if (c7Gender.equals("6")) {
                                    dto.setC7(Gender.MALE);
                                } else if (c7Gender.equals("7")) {
                                    dto.setC7(Gender.FEMALE);
                                } else {
                                    dto.setC7(null);
                                    err += "C8 không đúng (C8 phải bằng 6 hoặc 7); ";
                                }
                            } else {
                                dto.setC7(null);
                            }
                        }
                    } catch (Exception e) {
                        dto.setC7(null);
                        err += "C8 - Không rõ; ";
                    }
                    try {
//						c9 - c8_dob
                        currentCell = currentRow.getCell(8);
                        if (currentCell != null) {
                            Integer c8Dob = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c8Dob = Integer.valueOf(currentCell.getStringCellValue());
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c8Dob = Double.valueOf(currentCell.getNumericCellValue()).intValue();
                            }
                            dto.setC8(c8Dob);
                        }
                    } catch (Exception e) {
                        dto.setC8(null);
                        err += "C9 - Không rõ; ";
                    }
                    try {
//						c10 - c9
                        currentCell = currentRow.getCell(9);
                        if (currentCell != null) {
                            String c9RiskGroups = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c9RiskGroups = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c9RiskGroups = String
                                        .valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            List<HTSCaseRiskGroupDto> c9 = new ArrayList<HTSCaseRiskGroupDto>();
                            String[] risks = c9RiskGroups.split("\\|");
                            for (String ri : risks) {
                                HTSCaseRiskGroupDto r = new HTSCaseRiskGroupDto();
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
                                        err += "C10 không đúng (C10 phải lớn hơn 7 và nhỏ hơn 24); ";
                                    }
                                } else {
                                    r.setVal(null);
                                }
                                r.setName(r.getVal().getDescription());
                                c9.add(r);
                            }
                            dto.setC9(c9);
                        }
                    } catch (Exception e) {
                        dto.setC9(null);
                        err += "C10 - Không rõ; ";
                    }
                    try {
//						c11 - c9_note
                        currentCell = currentRow.getCell(10);
                        if (currentCell != null) {
                            String c9Note = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c9Note = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c9Note = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            dto.setC9Note(c9Note);
                        }
                    } catch (Exception e) {
                        dto.setC9Note(null);
                        err += "C11 - Không rõ; ";
                    }
                    try {
//						c12 - c10
                        currentCell = currentRow.getCell(11);
                        if (currentCell != null) {
                            String c10 = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c10 = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c10 = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            if (c10 != null) {
                                if (c10.equals("50")) {
                                    dto.setC10(HTSc10.answer1);
                                } else if (c10.equals("51")) {
                                    dto.setC10(HTSc10.answer2);
                                } else if (c10.equals("52")) {
                                    dto.setC10(HTSc10.answer3);
                                } else if (c10.equals("53")) {
                                    dto.setC10(HTSc10.answer4);
                                } else {
                                    dto.setC10(null);
                                    err += "C12 không đúng (C12 phải lớn hơn 49 và nhỏ hơn 54); ";
                                }
                            } else {
                                dto.setC10(null);
                            }
                        }
                    } catch (Exception e) {
                        dto.setC10(null);
                        err += "C12 - Không rõ; ";
                    }
                    try {
//						c13 - c10_note
                        currentCell = currentRow.getCell(12);
                        if (currentCell != null) {
                            String c10Note = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c10Note = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c10Note = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            dto.setC10Note(c10Note);
                        }
                    } catch (Exception e) {
                        dto.setC10Note(null);
                        err += "C13 - Không rõ; ";
                    }
                    try {
//						c14 - c11
                        currentCell = currentRow.getCell(13);
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
                                    err += "C14 không đúng (C14 phải bằng 3 hoặc 4); ";
                                }
                            } else {
                                dto.setC11(null);
                            }
                        }
                    } catch (Exception e) {
                        dto.setC11(null);
                        err += "C14 - Không rõ; ";
                    }
                    try {
//						c15 - c11a
                        currentCell = currentRow.getCell(14);
                        if (currentCell != null) {
                            String c11a = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c11a = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c11a = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            if (c11a != null) {
                                if (c11a.equals("54")) {
                                    dto.setC11a(HTSc11a.answer1);
                                } else if (c11a.equals("55")) {
                                    dto.setC11a(HTSc11a.answer2);
                                } else if (c11a.equals("56")) {
                                    dto.setC11a(HTSc11a.answer3);
                                } else {
                                    dto.setC11a(null);
                                    err += "C15 không đúng (C15 phải lớn hơn 53 và nhỏ hơn 57); ";
                                }
                            } else {
                                dto.setC11a(null);
                            }
                        }
                    } catch (Exception e) {
                        dto.setC11a(null);
                        err += "C15 - Không rõ; ";
                    }
                    try {
//						c16 - c11a_note
                        currentCell = currentRow.getCell(15);
                        if (currentCell != null) {
                            String c11aNote = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c11aNote = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c11aNote = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            dto.setC11aNote(c11aNote);
                        }
                    } catch (Exception e) {
                        dto.setC11aNote(null);
                        err += "C16 - Không rõ; ";
                    }
                    try {
//						c17 - c11b
                        currentCell = currentRow.getCell(16);
                        if (currentCell != null) {
                            String c11b = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c11b = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c11b = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            if (c11b != null) {
                                if (c11b.equals("57")) {
                                    dto.setC11b(HTSc11b.answer1);
                                } else if (c11b.equals("58")) {
                                    dto.setC11b(HTSc11b.answer2);
                                } else if (c11b.equals("59")) {
                                    dto.setC11b(HTSc11b.answer3);
                                } else {
                                    dto.setC11b(null);
                                    err += "C17 không đúng (C17 phải lớn hơn 56 và nhỏ hơn 60); ";
                                }
                            } else {
                                dto.setC11b(null);
                            }
                        }
                    } catch (Exception e) {
                        dto.setC11b(null);
                        err += "C17 - Không rõ; ";
                    }
                    try {
//						c18 - c11c
                        currentCell = currentRow.getCell(17);
                        if (currentCell != null) {
                            String c11c = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c11c = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c11c = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            if (c11c != null) {
                                if (c11c.equals("4")) {
                                    dto.setC11c(HTSYesNoNone.YES);
                                } else if (c11c.equals("3")) {
                                    dto.setC11c(HTSYesNoNone.NO);
                                } else {
                                    dto.setC11c(null);
                                    err += "C18 không đúng (C18 phải bằng 3 hoặc 4); ";
                                }
                            } else {
                                dto.setC11c(null);
                            }
                        }
                    } catch (Exception e) {
                        dto.setC11c(null);
                        err += "C18 - Không rõ; ";
                    }
                    try {
//						c19 - c11c_note
                        currentCell = currentRow.getCell(18);
                        if (currentCell != null) {
                            String c11cNote = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c11cNote = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c11cNote = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            dto.setC11cNote(c11cNote);
                        }
                    } catch (Exception e) {
                        dto.setC11cNote(null);
                        err += "C19 - Không rõ; ";
                    }
                    try {
//						c20 - c12
                        currentCell = currentRow.getCell(19);
                        if (currentCell != null) {
                            String c12 = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c12 = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c12 = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            if (c12 != null) {
                                if (c12.equals("60")) {
                                    dto.setC12(HTSc12.answer1);
                                } else if (c12.equals("61")) {
                                    dto.setC12(HTSc12.answer2);
                                } else if (c12.equals("62")) {
                                    dto.setC12(HTSc12.answer3);
                                } else if (c12.equals("63")) {
                                    dto.setC12(HTSc12.answer4);
                                } else {
                                    dto.setC12(null);
                                    err += "C20 không đúng (C20 phải lớn hơn 59 và nhỏ hơn 64); ";
                                }
                            } else {
                                dto.setC12(null);
                            }
                        }
                    } catch (Exception e) {
                        dto.setC12(null);
                        err += "C20 - Không rõ; ";
                    }
                    try {
//						c21 - c13_1
                        currentCell = currentRow.getCell(20);
                        if (currentCell != null) {
                            String c13_1 = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c13_1 = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c13_1 = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            if (c13_1 != null) {
                                if (c13_1.equals("64")) {
                                    dto.setC131(HTSc131.answer1);
                                } else if (c13_1.equals("65")) {
                                    dto.setC131(HTSc131.answer2);
                                } else {
                                    dto.setC131(null);
                                    err += "C21 không đúng (C21 phải bằng 64 hoặc 65); ";
                                }
                            } else {
                                dto.setC131(null);
                            }
                        }
                    } catch (Exception e) {
                        dto.setC131(null);
                        err += "C21 - Không rõ; ";
                    }
                    try {
//						c22 - c13_2
                        currentCell = currentRow.getCell(21);
                        if (currentCell != null) {
                            String c13_2 = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c13_2 = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c13_2 = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            if (c13_2 != null) {
                                if (c13_2.equals("66")) {
                                    dto.setC132(HTSc132.answer1);
                                } else if (c13_2.equals("67")) {
                                    dto.setC132(HTSc132.answer2);
                                } else if (c13_2.equals("68")) {
                                    dto.setC132(HTSc132.answer3);
                                } else {
                                    dto.setC132(null);
                                    err += "C22 không đúng (C22 phải lớn hơn 65 và nhỏ hơn 69); ";
                                }
                            } else {
                                dto.setC132(null);
                            }
                        }
                    } catch (Exception e) {
                        dto.setC132(null);
                        err += "C22 - Không rõ; ";
                    }
                    try {
//						c23 - c14
                        currentCell = currentRow.getCell(22);
                        if (currentCell != null) {
                            String c14 = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c14 = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c14 = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            if (c14 != null) {
                                if (c14.equals("69")) {
                                    dto.setC14(HTSc14.answer1);
                                } else if (c14.equals("70")) {
                                    dto.setC14(HTSc14.answer2);
                                } else if (c14.equals("71")) {
                                    dto.setC14(HTSc14.answer3);
                                } else if (c14.equals("196")) {
                                    dto.setC14(HTSc14.answer4);
                                } else {
                                    dto.setC14(null);
                                    err += "C23 không đúng (C23 phải bằng 69 hoặc 70 hoặc 71 hoặc 196); ";
                                }
                            } else {
                                dto.setC14(null);
                            }
                        }
                    } catch (Exception e) {
                        dto.setC14(null);
                        err += "C23 - Không rõ; ";
                    }
                    try {
//						c24 - c15
                        currentCell = currentRow.getCell(23);
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
                                    err += "C24 không đúng (C24 phải bằng 3 hoặc 4); ";
                                }
                            } else {
                                dto.setC15(null);
                            }
                        }
                    } catch (Exception e) {
                        dto.setC15(null);
                        err += "C24 - Không rõ; ";
                    }
                    try {
//						c25 - c15_date
                        currentCell = currentRow.getCell(24);
                        if (currentCell != null) {
                            LocalDateTime c15Date = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                                c15Date = LocalDateTime.parse(currentCell.getStringCellValue(), formatter);
                            } else if (currentCell.getCellType() == CellType.NUMERIC
                                    && currentCell.getDateCellValue() != null) {
                                c15Date = currentCell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault())
                                        .toLocalDateTime();
                            }
                            dto.setC15Date(c15Date);
                        }
                    } catch (Exception e) {
                        dto.setC15Date(null);
                        err += "C25 - Không rõ; ";
                    }
                    try {
//						c26 - c16_c17
                        currentCell = currentRow.getCell(25);
                        if (currentCell != null) {
                            String c1627 = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c1627 = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c1627 = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            if (c1627 != null) {
                                if (c1627.equals("4")) {
                                    dto.setC1627(HTSYesNoNone.YES);
                                } else if (c1627.equals("3")) {
                                    dto.setC1627(HTSYesNoNone.NO);
                                } else {
                                    dto.setC1627(null);
                                    err += "C26 không đúng (C26 phải bằng 3 hoặc 4); ";
                                }
                            } else {
                                dto.setC1627(null);
                            }
                        }
                    } catch (Exception e) {
                        dto.setC1627(null);
                        err += "C26 - Không rõ; ";
                    }
                    try {
//						c27 - c16_27_note
                        currentCell = currentRow.getCell(26);
                        if (currentCell != null) {
                            String c1627Note = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c1627Note = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c1627Note = String
                                        .valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            dto.setC1627Note(c1627Note);
                        }
                    } catch (Exception e) {
                        dto.setC1627Note(null);
                        err += "C27 - Không rõ; ";
                    }
                    try {
//						c28 - c1627_date
                        currentCell = currentRow.getCell(27);
                        if (currentCell != null) {
                            LocalDateTime c1627Date = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                                c1627Date = LocalDateTime.parse(currentCell.getStringCellValue(), formatter);
                            } else if (currentCell.getCellType() == CellType.NUMERIC
                                    && currentCell.getDateCellValue() != null) {
                                c1627Date = currentCell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault())
                                        .toLocalDateTime();
                            }
                            dto.setC1627Date(c1627Date);
                        }
                    } catch (Exception e) {
                        dto.setC1627Date(null);
                        err += "C28 - Không rõ; ";
                    }
                    try {
//						c29 - c17
                        currentCell = currentRow.getCell(28);
                        if (currentCell != null) {
                            String c17 = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c17 = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c17 = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            if (c17 != null) {
                                if (c17.equals("72")) {
                                    dto.setC17(HTSc17.answer1);
                                } else if (c17.equals("73")) {
                                    dto.setC17(HTSc17.answer2);
                                } else if (c17.equals("74")) {
                                    dto.setC17(HTSc17.answer3);
                                } else {
                                    dto.setC17(null);
                                    err += "C29 không đúng (C29 phải lớn hơn 71 và nhỏ hơn 75); ";
                                }
                            } else {
                                dto.setC17(null);
                            }
                        }
                    } catch (Exception e) {
                        dto.setC17(null);
                        err += "C29 - Không rõ; ";
                    }
                    try {
//						c30 - c18
                        currentCell = currentRow.getCell(29);
                        if (currentCell != null) {
                            String c18 = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c18 = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c18 = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            if (c18 != null) {
                                if (c18.equals("75")) {
                                    dto.setC18(HTSc18.answer1);
                                } else if (c18.equals("76")) {
                                    dto.setC18(HTSc18.answer2);
                                } else if (c18.equals("77")) {
                                    dto.setC18(HTSc18.answer3);
                                } else if (c18.equals("192")) {
                                    dto.setC18(HTSc18.answer4);
                                } else {
                                    dto.setC18(null);
                                    err += "C30 không đúng (C30 phải bằng 75 hoặc 76 hoặc 76 hoặc 192); ";
                                }
                            } else {
                                dto.setC18(null);
                            }
                        }
                    } catch (Exception e) {
                        dto.setC18(null);
                        err += "C30 - Không rõ; ";
                    }
                    try {
//						c31 - c19
                        currentCell = currentRow.getCell(30);
                        if (currentCell != null) {
                            String c19 = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c19 = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c19 = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            dto.setC19(c19);
                        }
                    } catch (Exception e) {
                        dto.setC19(null);
                        err += "C31 - Không rõ; ";
                    }
                    try {
//						c32 - c19_date
                        currentCell = currentRow.getCell(31);
                        if (currentCell != null) {
                            LocalDateTime c19Date = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                                c19Date = LocalDateTime.parse(currentCell.getStringCellValue(), formatter);
                            } else if (currentCell.getCellType() == CellType.NUMERIC
                                    && currentCell.getDateCellValue() != null) {
                                c19Date = currentCell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault())
                                        .toLocalDateTime();
                            }
                            dto.setC19Date(c19Date);
                        }
                    } catch (Exception e) {
                        dto.setC19Date(null);
                        err += "C32 - Không rõ; ";
                    }
                    try {
//						c33 - c19_note
                        currentCell = currentRow.getCell(32);
                        if (currentCell != null) {
                            String c19Note = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c19Note = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c19Note = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            dto.setC19Note(c19Note);
                        }
                    } catch (Exception e) {
                        dto.setC19Note(null);
                        err += "C33 - Không rõ; ";
                    }
                    try {
//						c34 - c20
                        currentCell = currentRow.getCell(33);
                        if (currentCell != null) {
                            String c20 = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c20 = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c20 = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            if (c20 != null) {
                                if (c20.equals("78")) {
                                    dto.setC20(HTSc20.answer1);
                                } else if (c20.equals("79")) {
                                    dto.setC20(HTSc20.answer2);
                                } else if (c20.equals("80")) {
                                    dto.setC20(HTSc20.answer3);
                                } else {
                                    dto.setC20(null);
                                    err += "C34 không đúng (C34 phải lớn hơn 77 và nhỏ hơn 81); ";
                                }
                            } else {
                                dto.setC20(null);
                            }
                        }
                    } catch (Exception e) {
                        dto.setC20(null);
                        err += "C34 - Không rõ; ";
                    }
                    try {
//						c35 - c20_reason
                        currentCell = currentRow.getCell(34);
                        if (currentCell != null) {
                            String c20Reason = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c20Reason = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c20Reason = String
                                        .valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            dto.setC20Reason(c20Reason);
                        }
                    } catch (Exception e) {
                        dto.setC20Reason(null);
                        err += "C35 - Không rõ; ";
                    }
                    try {
//						c36 - c20_org
                        currentCell = currentRow.getCell(35);
                        if (currentCell != null) {
                            String c20Org = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c20Org = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c20Org = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            dto.setC20Org(c20Org);
                        }
                    } catch (Exception e) {
                        dto.setC20Org(null);
                        err += "C36 - Không rõ; ";
                    }
                    try {
//						c37 - c20_reg_date
                        currentCell = currentRow.getCell(36);
                        if (currentCell != null) {
                            LocalDateTime c20RegDate = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                                c20RegDate = LocalDateTime.parse(currentCell.getStringCellValue(), formatter);
                            } else if (currentCell.getCellType() == CellType.NUMERIC
                                    && currentCell.getDateCellValue() != null) {
                                c20RegDate = currentCell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault())
                                        .toLocalDateTime();
                            }
                            dto.setC20RegDate(c20RegDate);
                        }
                    } catch (Exception e) {
                        dto.setC20RegDate(null);
                        err += "C37 - Không rõ; ";
                    }
                    try {
//						c38 - c20_code
                        currentCell = currentRow.getCell(37);
                        if (currentCell != null) {
                            String c20Code = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c20Code = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c20Code = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            dto.setC20Code(c20Code);
                        }
                    } catch (Exception e) {
                        dto.setC20Code(null);
                        err += "C38 - Không rõ; ";
                    }
                    try {
//						c39 - c21
                        currentCell = currentRow.getCell(38);
                        if (currentCell != null) {
                            String c21 = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c21 = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c21 = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            if (c21 != null) {
                                if (c21.equals("4")) {
                                    dto.setC21(HTSYesNoNone.YES);
                                } else if (c21.equals("3")) {
                                    dto.setC21(HTSYesNoNone.NO);
                                } else {
                                    dto.setC21(null);
                                    err += "C39 không đúng (C39 phải bằng 3 hoặc 4); ";
                                }
                            } else {
                                dto.setC21(null);
                            }
                        }
                    } catch (Exception e) {
                        dto.setC21(null);
                        err += "C39 - Không rõ; ";
                    }
                    try {
//						c40 - c21_date
                        currentCell = currentRow.getCell(39);
                        if (currentCell != null) {
                            LocalDateTime c21Date = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                                c21Date = LocalDateTime.parse(currentCell.getStringCellValue(), formatter);
                            } else if (currentCell.getCellType() == CellType.NUMERIC
                                    && currentCell.getDateCellValue() != null) {
                                c21Date = currentCell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault())
                                        .toLocalDateTime();
                            }
                            dto.setC21Date(c21Date);
                        }
                    } catch (Exception e) {
                        dto.setC21Date(null);
                        err += "C40 - Không rõ; ";
                    }
                    try {
//						c41 - c22
                        currentCell = currentRow.getCell(40);
                        if (currentCell != null) {
                            String c22 = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c22 = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c22 = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            if (c22 != null) {
                                if (c22.equals("4")) {
                                    dto.setC22(HTSYesNoNone.YES);
                                } else if (c22.equals("3")) {
                                    dto.setC22(HTSYesNoNone.NO);
                                } else {
                                    dto.setC22(null);
                                    err += "C41 không đúng (C41 phải bằng 3 hoặc 4); ";
                                }
                            } else {
                                dto.setC22(null);
                            }
                        }
                    } catch (Exception e) {
                        dto.setC22(null);
                        err += "C41 - Không rõ; ";
                    }
                    try {
//						c42 - c23_full_name
                        currentCell = currentRow.getCell(41);
                        if (currentCell != null) {
                            String c23FullName = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c23FullName = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c23FullName = String
                                        .valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            dto.setC23FullName(c23FullName);
                        }
                    } catch (Exception e) {
                        dto.setC23FullName(null);
                        err += "C42 - Không rõ; ";
                    }
                    try {
//						c43 - c23_ethnic_id
                        currentCell = currentRow.getCell(42);
                        if (currentCell != null) {
                            String c23Ethnic = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c23Ethnic = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c23Ethnic = String
                                        .valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            if (c23Ethnic != null) {
                                Hashtable<String, Long> hashFacilities = this.c23EthnicConstants();
                                Long c23EthnicConvert = hashFacilities.get(c23Ethnic);
                                Dictionary dictionary = dictionaryRepository.findOne(c23EthnicConvert);
                                if (dictionary != null) {
                                    dto.setC23Ethnic(new DictionaryDto(dictionary));
                                } else {
                                    dto.setC23Ethnic(null);
                                    err += "C43 - Không tìm thấy dân tộc; ";
                                }
                            }
                        }
                    } catch (Exception e) {
                        dto.setC23Ethnic(null);
                        err += "C43 - Không rõ; ";
                    }
                    try {
//						c44 - c23_profession_id
                        currentCell = currentRow.getCell(43);
                        if (currentCell != null) {
                            String c23Profession = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c23Profession = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c23Profession = String
                                        .valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            if (c23Profession != null) {
                                Hashtable<String, Long> hashFacilities = this.c23ProfessionConstants();
                                Long c23ProfessionConvert = hashFacilities.get(c23Profession);
                                Dictionary dictionary = dictionaryRepository.findOne(c23ProfessionConvert);
                                if (dictionary != null) {
                                    dto.setC23Profession(new DictionaryDto(dictionary));
                                } else {
                                    dto.setC23Profession(null);
                                    err += "C44 - Không tìm thấy nghề nghiệp; ";
                                }
                            }
                        }
                    } catch (Exception e) {
                        dto.setC23Profession(null);
                        err += "C44 - Không rõ; ";
                    }
                    try {
//						c45 - c23_id_number
                        currentCell = currentRow.getCell(44);
                        if (currentCell != null) {
                            String c23IdNumber = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c23IdNumber = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c23IdNumber = String
                                        .valueOf(Double.valueOf(currentCell.getNumericCellValue()).longValue());
                            }
                            dto.setC23IdNumber(c23IdNumber);
                        }
                    } catch (Exception e) {
                        dto.setC23IdNumber(null);
                        err += "C45 - Không rõ; ";
                    }
                    try {
//						c46 - c23_phone_number
                        currentCell = currentRow.getCell(45);
                        if (currentCell != null) {
                            String c23PhoneNumber = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c23PhoneNumber = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c23PhoneNumber = String
                                        .valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            dto.setC23PhoneNumber(c23PhoneNumber);
                        }
                    } catch (Exception e) {
                        dto.setC23PhoneNumber(null);
                        err += "C46 - Không rõ; ";
                    }
                    try {
//						c47 - c23_note
                        currentCell = currentRow.getCell(46);
                        if (currentCell != null) {
                            String c23Note = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c23Note = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c23Note = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            dto.setC23Note(c23Note);
                        }
                    } catch (Exception e) {
                        dto.setC23Note(null);
                        err += "C47 - Không rõ; ";
                    }
                    try {
//						c48 - c23_resident_address_province_id
                        currentCell = currentRow.getCell(47);
                        if (currentCell != null) {
                            Long c23ResidentAddressProvince = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c23ResidentAddressProvince = Long.valueOf(currentCell.getStringCellValue());
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c23ResidentAddressProvince = Long.valueOf((long) currentCell.getNumericCellValue())
                                        .longValue();
                            }
                            AdminUnit entity = null;
                            String c23ResidentAddressProvinceConvert = String.valueOf(c23ResidentAddressProvince);
                            if (c23ResidentAddressProvinceConvert.length() == 1) {
                                c23ResidentAddressProvinceConvert = "0" + c23ResidentAddressProvinceConvert;
                            }
                            if (c23ResidentAddressProvince != null) {
                                entity = adminUnitRepository
                                        .findByProvinceOrDistrict(c23ResidentAddressProvinceConvert);
                                if (entity != null) {
                                    dto.setC23ResidentAddressProvince(new AdminUnitDto(entity));
                                } else {
                                    dto.setC23ResidentAddressProvince(null);
                                    err += "C48 - Không tìm thấy tỉnh/thành phố; ";
                                }
                            }
                        }
                    } catch (Exception e) {
                        dto.setC23ResidentAddressProvince(null);
                        err += "C48 - Không rõ; ";
                    }
                    try {
//						c49 - c23_resident_address_district_id
                        currentCell = currentRow.getCell(48);
                        if (currentCell != null) {
                            Long c23ResidentAddressDistrict = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c23ResidentAddressDistrict = Long.valueOf(currentCell.getStringCellValue());
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c23ResidentAddressDistrict = Long.valueOf((long) currentCell.getNumericCellValue())
                                        .longValue();
                            }
                            AdminUnit entity = null;
                            String c23ResidentAddressDistrictConvert = String.valueOf(c23ResidentAddressDistrict);
                            if (c23ResidentAddressDistrictConvert.length() == 1) {
                                c23ResidentAddressDistrictConvert = "00" + c23ResidentAddressDistrictConvert;
                            } else if (c23ResidentAddressDistrictConvert.length() == 2) {
                                c23ResidentAddressDistrictConvert = "0" + c23ResidentAddressDistrictConvert;
                            }
                            if (c23ResidentAddressDistrict != null) {
                                entity = adminUnitRepository
                                        .findByProvinceOrDistrict(c23ResidentAddressDistrictConvert);
                                if (entity != null) {
                                    dto.setC23ResidentAddressDistrict(new AdminUnitDto(entity));
                                } else {
                                    dto.setC23ResidentAddressDistrict(null);
                                    err += "C48 - Không tìm thấy quận/huyện; ";
                                }
                            }
                        }
                    } catch (Exception e) {
                        dto.setC23ResidentAddressDistrict(null);
                        err += "C49 - Không rõ; ";
                    }
                    try {
//						c50 - c23_resident_address_ward
                        currentCell = currentRow.getCell(49);
                        if (currentCell != null) {
                            String c23ResidentAddressWard = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c23ResidentAddressWard = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c23ResidentAddressWard = String
                                        .valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            dto.setC23ResidentAddressWard(c23ResidentAddressWard);
                        }
                    } catch (Exception e) {
                        dto.setC23ResidentAddressWard(null);
                        err += "C50 - Không rõ; ";
                    }
                    try {
//						c51 - c23_resident_address_detail
                        currentCell = currentRow.getCell(50);
                        if (currentCell != null) {
                            String c23ResidentAddressDetail = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c23ResidentAddressDetail = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c23ResidentAddressDetail = String
                                        .valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            dto.setC23ResidentAddressDetail(c23ResidentAddressDetail);
                        }
                    } catch (Exception e) {
                        dto.setC23ResidentAddressDetail(null);
                        err += "C51 - Không rõ; ";
                    }
                    try {
//						c52 - c23_current_address_province_id
                        currentCell = currentRow.getCell(51);
                        if (currentCell != null) {
                            Long c23CurrentAddressProvince = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c23CurrentAddressProvince = Long.valueOf(currentCell.getStringCellValue());
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c23CurrentAddressProvince = Long.valueOf((long) currentCell.getNumericCellValue())
                                        .longValue();
                            }
                            AdminUnit entity = null;
                            String c23CurrentAddressProvinceConvert = String.valueOf(c23CurrentAddressProvince);
                            if (c23CurrentAddressProvinceConvert.length() == 1) {
                                c23CurrentAddressProvinceConvert = "0" + c23CurrentAddressProvinceConvert;
                            }
                            if (c23CurrentAddressProvince != null) {
                                entity = adminUnitRepository.findByProvinceOrDistrict(c23CurrentAddressProvinceConvert);
                                if (entity != null) {
                                    dto.setC23CurrentAddressProvince(new AdminUnitDto(entity));
                                } else {
                                    dto.setC23CurrentAddressProvince(null);
                                    err += "C52 - Không tìm thấy tỉnh/thành phố; ";
                                }
                            }
                        }
                    } catch (Exception e) {
                        dto.setC23CurrentAddressProvince(null);
                        err += "C52 - Không rõ; ";
                    }
                    try {
//						c53 - c23_current_address_district_id
                        currentCell = currentRow.getCell(52);
                        if (currentCell != null) {
                            Long c23CurrentAddressDistrict = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c23CurrentAddressDistrict = Long.valueOf(currentCell.getStringCellValue());
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c23CurrentAddressDistrict = Long.valueOf((long) currentCell.getNumericCellValue())
                                        .longValue();
                            }
                            AdminUnit entity = null;
                            String c23CurrentAddressDistrictConvert = String.valueOf(c23CurrentAddressDistrict);
                            if (c23CurrentAddressDistrictConvert.length() == 1) {
                                c23CurrentAddressDistrictConvert = "00" + c23CurrentAddressDistrictConvert;
                            } else if (c23CurrentAddressDistrictConvert.length() == 2) {
                                c23CurrentAddressDistrictConvert = "0" + c23CurrentAddressDistrictConvert;
                            }
                            if (c23CurrentAddressDistrict != null) {
                                entity = adminUnitRepository.findByProvinceOrDistrict(c23CurrentAddressDistrictConvert);
                                if (entity != null) {
                                    dto.setC23CurrentAddressDistrict(new AdminUnitDto(entity));
                                } else {
                                    dto.setC23CurrentAddressDistrict(null);
                                    err += "C53 - Không tìm thấy quận/huyện; ";
                                }
                            }
                        }
                    } catch (Exception e) {
                        dto.setC23CurrentAddressDistrict(null);
                        err += "C53 - Không rõ; ";
                    }
                    try {
//						c54 - c23_current_address_ward
                        currentCell = currentRow.getCell(53);
                        if (currentCell != null) {
                            String c23CurrentAddressWard = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c23CurrentAddressWard = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c23CurrentAddressWard = String
                                        .valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            dto.setC23CurrentAddressWard(c23CurrentAddressWard);
                        }
                    } catch (Exception e) {
                        dto.setC23CurrentAddressWard(null);
                        err += "C54 - Không rõ; ";
                    }
                    try {
//						c55 - c23_current_address_detail
                        currentCell = currentRow.getCell(54);
                        if (currentCell != null) {
                            String c23CurrentAddressDetail = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c23CurrentAddressDetail = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c23CurrentAddressDetail = String
                                        .valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            dto.setC23CurrentAddressDetail(c23CurrentAddressDetail);
                        }
                    } catch (Exception e) {
                        dto.setC23CurrentAddressDetail(null);
                        err += "C55 - Không rõ; ";
                    }
                    try {
//						c56 - c24
                        currentCell = currentRow.getCell(55);
                        if (currentCell != null) {
                            String c24 = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c24 = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c24 = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            if (c24 != null) {
                                if (c24.equals("81")) {
                                    dto.setC24(HTSc24.answer1);
                                } else if (c24.equals("82")) {
                                    dto.setC24(HTSc24.answer2);
                                } else if (c24.equals("83")) {
                                    dto.setC24(HTSc24.answer3);
                                } else {
                                    dto.setC24(null);
                                    err += "C56 không đúng (C56 phải lớn hơn 80 và nhỏ hơn 84); ";
                                }
                            } else {
                                dto.setC24(null);
                            }
                        }
                    } catch (Exception e) {
                        dto.setC24(null);
                        err += "C56 - Không rõ; ";
                    }
                    try {
//						c61 - c25
                        currentCell = currentRow.getCell(60);
                        if (currentCell != null) {
                            String c25 = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c25 = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c25 = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            if (c25 != null) {
                                if (c25.equals("4")) {
                                    dto.setC25(HTSYesNoNone.YES);
                                } else if (c25.equals("3")) {
                                    dto.setC25(HTSYesNoNone.NO);
                                } else {
                                    dto.setC25(null);
                                    err += "C61 không đúng (C61 phải bằng 3 hoặc 4); ";
                                }
                            } else {
                                dto.setC25(null);
                            }
                        }
                    } catch (Exception e) {
                        dto.setC25(null);
                        err += "C61 - Không rõ; ";
                    }
                    try {
//						c60 - c26
                        currentCell = currentRow.getCell(59);
                        if (currentCell != null) {
                            String c26 = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c26 = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c26 = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            if (c26 != null) {
                                if (c26.equals("193")) {
                                    dto.setC26(HTSc26.answer1);
                                } else if (c26.equals("194")) {
                                    dto.setC26(HTSc26.answer2);
                                } else if (c26.equals("195")) {
                                    dto.setC26(HTSc26.answer3);
                                } else {
                                    dto.setC26(null);
                                    err += "C60 không đúng (C60 phải lớn hơn 192 và nhỏ hơn 196); ";
                                }
                            } else {
                                dto.setC26(null);
                            }
                        }
                    } catch (Exception e) {
                        dto.setC26(null);
                        err += "C60 - Không rõ; ";
                    }
                    try {
//						c62 - c28
                        currentCell = currentRow.getCell(61);
                        if (currentCell != null) {
                            String c28 = null;
                            if (currentCell.getCellType() == CellType.STRING
                                    && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c28 = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c28 = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                            }
                            if (c28 != null) {
                                if (c28.equals("197")) {
                                    dto.setC28(HTSc28.answer1);
                                } else if (c28.equals("198")) {
                                    dto.setC28(HTSc28.answer2);
                                } else if (c28.equals("199")) {
                                    dto.setC28(HTSc28.answer3);
                                } else if (c28.equals("200")) {
                                    dto.setC28(HTSc28.answer4);
                                } else {
                                    dto.setC28(null);
                                    err += "C62 không đúng (C62 phải lớn hơn 196 và nhỏ hơn 199); ";
                                }
                            } else {
                                dto.setC28(null);
                            }
                        }
                    } catch (Exception e) {
                        dto.setC28(null);
                        err += "C62 - Không rõ; ";
                    }
//          try {
//              saveOrUpdate(dto);
//	        } catch (Exception e) {
//	          dto.setErrorContent(e.getMessage());
//	          dto.setNumberErrorContent(rowIndex + 1);
////	          ret.setTotalErr(ret.getTotalErr() + 1);
////	          ret.getListErr().add(dto);
//	        }
//          if (dto.getErrorContent() != null) {
//            dto.setNumberErrorContent(rowIndex + 1);
//            ret.setTotalErr(ret.getTotalErr() + 1);
//            ret.getListErr().add(dto);
//          }

                    try {
                        dto = saveOrUpdate(dto);
                        dto.setSaved(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                        ret.setTotalErr(ret.getTotalErr() + 1);
//        	  StackTraceElement[] trace = e.getStackTrace();
                        err += "Lưu thất bại:" + CommonUtils.getStackTrace(e);
//        	  if(trace!=null && trace.length>0) {
//        		  for (int i = 0; i < trace.length; i++) {
//        			  err+=trace[i].toString()+";";
//				}
//        	  }

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

    public Hashtable<String, Long> c23EthnicConstants() {
        Hashtable<String, Long> hashFacilities = new Hashtable<String, Long>();
        hashFacilities.put("84", 7L);
        hashFacilities.put("85", 8L);
        hashFacilities.put("86", 9L);
        hashFacilities.put("87", 10L);
        hashFacilities.put("88", 11L);
        hashFacilities.put("89", 12L);
        hashFacilities.put("90", 13L);
        hashFacilities.put("91", 14L);
        hashFacilities.put("92", 15L);
        hashFacilities.put("93", 16L);
        hashFacilities.put("94", 17L);
        hashFacilities.put("95", 18L);
        hashFacilities.put("96", 19L);
        hashFacilities.put("97", 20L);
        hashFacilities.put("98", 21L);
        hashFacilities.put("99", 22L);
        hashFacilities.put("100", 23L);
        hashFacilities.put("101", 24L);
        hashFacilities.put("102", 25L);
        hashFacilities.put("103", 26L);
        hashFacilities.put("104", 27L);
        hashFacilities.put("105", 28L);
        hashFacilities.put("106", 29L);
        hashFacilities.put("107", 30L);
        hashFacilities.put("108", 31L);
        hashFacilities.put("109", 32L);
        hashFacilities.put("110", 33L);
        hashFacilities.put("111", 34L);
        hashFacilities.put("112", 35L);
        hashFacilities.put("113", 36L);
        hashFacilities.put("114", 37L);
        hashFacilities.put("115", 38L);
        hashFacilities.put("116", 39L);
        hashFacilities.put("117", 40L);
        hashFacilities.put("118", 41L);
        hashFacilities.put("119", 42L);
        hashFacilities.put("120", 43L);
        hashFacilities.put("121", 44L);
        hashFacilities.put("122", 45L);
        hashFacilities.put("123", 46L);
        hashFacilities.put("124", 47L);
        hashFacilities.put("125", 48L);
        hashFacilities.put("126", 49L);
        hashFacilities.put("127", 50L);
        hashFacilities.put("128", 51L);
        hashFacilities.put("129", 52L);
        hashFacilities.put("130", 53L);
        hashFacilities.put("131", 54L);
        hashFacilities.put("132", 55L);
        hashFacilities.put("133", 56L);
        hashFacilities.put("134", 57L);
        hashFacilities.put("135", 58L);
        hashFacilities.put("136", 59L);
        hashFacilities.put("137", 60L);
        return hashFacilities;
    }

    public Hashtable<String, Long> c23ProfessionConstants() {
        Hashtable<String, Long> hashFacilities = new Hashtable<String, Long>();
        hashFacilities.put("138", 197L);
        hashFacilities.put("139", 197L);
        hashFacilities.put("140", 197L);
        hashFacilities.put("141", 197L);
        hashFacilities.put("142", 197L);
        hashFacilities.put("143", 192L);
        hashFacilities.put("144", 193L);
        hashFacilities.put("145", 190L);
        hashFacilities.put("146", 189L);
        hashFacilities.put("147", 195L);
        hashFacilities.put("148", 196L);
        hashFacilities.put("149", 197L);
        hashFacilities.put("150", 197L);
        return hashFacilities;
    }

    @Override
    public PreventionReportDto<HTSReportDetailsDto> getReportDetail(PreventionFilterDto filter) {
        if (filter != null && filter.getFromDate() != null && filter.getToDate() != null
                && filter.getFromDate().isBefore(filter.getToDate()) && filter.getOrgIds() != null
                && filter.getOrgIds().size() > 0) {

            User currentUser = SecurityUtils.getCurrentUser();
            Boolean isAdministrator = SecurityUtils.isUserInRole(currentUser, "ROLE_ADMIN");

            PreventionReportDto<HTSReportDetailsDto> ret = new PreventionReportDto<HTSReportDetailsDto>();
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
            HTSReportDetailsDto details = this.getReportDetail(filter, "I.1", "Số khách hàng đã được XN HIV", 0);
            ret.getListDetail().add(details);

            details = this.getReportDetail(filter, "I.2", "Số khách hàng đã được XN HIV và quay lại nhận kết quả", 1);
            ret.getListDetail().add(details);

            details = this.getReportDetail(filter, "I.3",
                    "Số khách hàng đã được XN HIV và quay lại nhận KQXN khẳng định (+) ", 2);
            ret.getListDetail().add(details);

            details = this.getReportDetail(filter, "I.4", "Số khách hàng (+) được tính vào báo cáo MER", 3);
            ret.getListDetail().add(details);

            details = this.getReportDetail(filter, "I.5",
                    "Số khách hàng có KQXN khẳng định (+) đã được chuyển gửi thành công đến cơ sở điều trị HIV/AIDS",
                    4);
            ret.getListDetail().add(details);

            details = this.getReportDetail(filter, "I.6", "Số khách hàng (+) được tính vào báo cáo MER", 5);
            ret.getListDetail().add(details);

            details = this.getReportDetail(filter, "I.7",
                    "Số khách hàng có KQXN (-) được chuyển gửi thành công đến cơ sở PrEP", 6);
            ret.getListDetail().add(details);

            details = this.getReportDetail(filter, "II.1", "Dương tính mới", 7);
            ret.getListDetail().add(details);

            details = this.getReportDetail(filter, "II.2", "Dương tính cũ", 8);
            ret.getListDetail().add(details);

            details = this.getReportDetail(filter, "II.3", "Đang chờ kết quả xác minh", 9);
            ret.getListDetail().add(details);

            details = this.getReportDetail(filter, "II.4", "Không có thông tin xác minh", 10);
            ret.getListDetail().add(details);

            details = this.getReportDetail(filter, "III.1",
                    "Số khách hàng có KQXN khẳng định (+) được làm XN mới nhiễm", 11);
            ret.getListDetail().add(details);

            details = this.getReportDetail(filter, "III.2", "Số khách hàng có KQ phản ứng mới nhiễm HIV", 12);
            ret.getListDetail().add(details);

            details = this.getReportDetail(filter, "III.3",
                    "Số khách hàng có phản ứng mới nhiễm HIV được làm XN tải lượng virus", 13);
            ret.getListDetail().add(details);

            details = this.getReportDetail(filter, "III.4", "Số khách hàng có KQXN TLVR <1000 bản sao/ml", 14);
            ret.getListDetail().add(details);

            details = this.getReportDetail(filter, "III.5", "Số khách hàng có KQXN TLVR >=1000 bản sao/ml", 15);
            ret.getListDetail().add(details);

            details = this.getReportDetail(filter, "IV.1", "Khách hàng tự đến", 16);
            ret.getListDetail().add(details);

            details = this.getReportDetail(filter, "IV.2", "Dịch vụ TBXNBT/BC của người có HIV", 17);
            ret.getListDetail().add(details);

            details = this.getReportDetail(filter, "IV.3", "Chương trình Tiếp cận cộng đồng", 18);
            ret.getListDetail().add(details);

            details = this.getReportDetail(filter, "IV.4", "Nguồn khác", 19);
            ret.getListDetail().add(details);

            details = this.getReportDetail(filter, "V.1", "Người nghiện chích ma túy", 20);
            ret.getListDetail().add(details);

            details = this.getReportDetail(filter, "V.2", "Nam có quan hệ tình dục đồng giới", 21);
            ret.getListDetail().add(details);

            details = this.getReportDetail(filter, "V.3", "Người bán dâm", 22);
            ret.getListDetail().add(details);

            details = this.getReportDetail(filter, "V.4", "Người chuyển giới", 23);
            ret.getListDetail().add(details);

            details = this.getReportDetail(filter, "V.5", "Vợ/chồng/bạn tình/con đẻ ≤ 15 tuổi của người có HIV", 24);
            ret.getListDetail().add(details);

            details = this.getReportDetail(filter, "V.6", "Bạn chích chung của người có HIV", 25);
            ret.getListDetail().add(details);

            details = this.getReportDetail(filter, "V.7", "Phạm nhân", 26);
            ret.getListDetail().add(details);

            details = this.getReportDetail(filter, "V.8", "Khác", 27);
            ret.getListDetail().add(details);

            return ret;
        }
        return null;
    }

    private HTSReportDetailsDto getReportDetail(PreventionFilterDto filter, String rowCode, String rowName,
                                                int orderNumber) {
        HTSReportDetailsDto detail = new HTSReportDetailsDto();

        detail.setSeq(rowCode);
        detail.setTitle(rowName);

        Integer community = 0;
        community = this.queryReport(filter, rowCode, 1, orderNumber).intValue();
        detail.setCommunity(community);

        Integer vtc = 0;
        vtc = this.queryReport(filter, rowCode, 2, orderNumber).intValue();
        detail.setVtc(vtc);

        Integer pknt = 0;
        pknt = this.queryReport(filter, rowCode, 3, orderNumber).intValue();
        detail.setPknt(pknt);

        Integer methadone = 0;
        methadone = this.queryReport(filter, rowCode, 4, orderNumber).intValue();
        detail.setMethadone(methadone);

        Integer prep = 0;
        prep = this.queryReport(filter, rowCode, 5, orderNumber).intValue();
        detail.setPrep(prep);

        // trại giam
        Integer prison = 0;
        prison = this.queryReport(filter, rowCode, 6, orderNumber).intValue();
        detail.setPrison(prison);

        // cơ sở khác
        Integer otherOrg = 0;
        otherOrg = this.queryReport(filter, rowCode, 7, orderNumber).intValue();
        detail.setOtherOrg(otherOrg);

        // phòng khoa bệnh viện;
        Integer department = 0;
        department = this.queryReport(filter, rowCode, 8, orderNumber).intValue();
        detail.setDepartment(department);

        Integer sns = 0;
        sns = this.queryReport(filter, rowCode, 9, orderNumber).intValue();
        detail.setSns(sns);

        return detail;
    }

    private Integer queryReport(PreventionFilterDto filter, String rowCode, int col, int orderNumber) {
        if (orderNumber < 20) {
            return queryReportUnder20(filter, rowCode, col, orderNumber);
        } else {
            return queryReportUpper20(filter, rowCode, col, orderNumber);
        }
    }

    private Integer queryReportUnder20(PreventionFilterDto filter, String rowCode, int col, int orderNumber) {
    	
        if (col == 1) {// cột 1 là pe
            String SQLPE = " SELECT COUNT(s.id) from PECase s WHERE s.c1Org.id in (:listOrg) AND (s.c12=:c12answer1 OR s.c12=:c12answer2) ";//
            String whereClausePE = " ";
            // Số khách hàng đã được XN HIV
            if (rowCode.equals("I.1")) {
                whereClausePE = " AND s.c11Date >=:fromDate AND s.c11Date <=:toDate ";
            }
//        Số khách hàng đã được XN HIV và quay lại nhận kết quả
            if (rowCode.equals("I.2")) {
                whereClausePE = " AND s.c11Date >=:fromDate AND s.c11Date <=:toDate AND (s.c13 =:c13answer1 or s.c13 =:c13answer2 or s.c13 =:c13answer3) ";
            }
//        Số khách hàng đã được XN HIV và quay lại nhận KQXN khẳng định (+)
            if (rowCode.equals("I.3")) {
                whereClausePE = " AND s.c11Date >=:fromDate AND s.c11Date <=:toDate AND s.c13 =:c13answer1  and s.c131Result =:c131ResultAnswer2 ";
            }
//        Số khách hàng (+) được tính vào báo cáo MER
            if (rowCode.equals("I.4")) {
                whereClausePE = " AND s.c11Date >=:fromDate AND s.c11Date <=:toDate  AND s.c13 =:c13answer1 and s.c131Result =:c131ResultAnswer2 and (s.c16 !=:b2 or s.c16 is null) ";
            }
//        Số khách hàng có KQXN khẳng định (+) đã được chuyển gửi thành công đến cơ sở điều trị HIV/AIDS
            if (rowCode.equals("I.5")) {
                whereClausePE = " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate ";
            }
//        Số khách hàng (+) được tính vào báo cáo MER
            if (rowCode.equals("I.6")) {
                whereClausePE = " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and (s.c16 is null  or s.c16 !=:b2) ";
            }
//        Số khách hàng có KQXN (-) được chuyển gửi thành công đến cơ sở PrEP
            if (rowCode.equals("I.7")) {
                whereClausePE = " AND s.c14Date >=:fromDate AND s.c14Date <=:toDate ";
            }

            if (rowCode.equals("II.1")) {
                whereClausePE = " AND s.c11Date >=:fromDate AND s.c11Date <=:toDate AND s.c131Result =:c131ResultAnswer2 and s.c16 =:c16answer1 ";
            }
            if (rowCode.equals("II.2")) {
                whereClausePE = " AND s.c11Date >=:fromDate AND s.c11Date <=:toDate AND s.c131Result =:c131ResultAnswer2 AND s.c16 =:c16answer2 ";
            }
            if (rowCode.equals("II.3")) {
                whereClausePE = " AND s.c11Date >=:fromDate AND s.c11Date <=:toDate AND s.c131Result =:c131ResultAnswer2 and s.c16 =:c16answer3 ";
            }
            if (rowCode.equals("II.4")) {
                whereClausePE = " AND s.c11Date >=:fromDate AND s.c11Date <=:toDate AND s.c131Result =:c131ResultAnswer2 and s.c16 is null ";
            }
            if (rowCode.equals("III.1")) {
                return 0;
            }
            if (rowCode.equals("III.2")) {
                return 0;
            }
            if (rowCode.equals("III.3")) {
                return 0;
            }
            if (rowCode.equals("III.4")) {
                return 0;
            }
            if (rowCode.equals("III.5")) {
                return 0;
            }

            if (rowCode.equals("IV.1")) {
                return 0;
            }
            if (rowCode.equals("IV.2")) {
                return 0;
            }
            if (rowCode.equals("IV.3")) {
                whereClausePE = " AND s.c11Date >=:fromDate AND s.c11Date <=:toDate and s.c131Result =:c131ResultAnswer2 ";
            }
            if (rowCode.equals("IV.4")) {
                return 0;
            }
            Query qpe = manager.createQuery(SQLPE + whereClausePE);
            qpe.setParameter("listOrg", filter.getOrgIds());
            qpe.setParameter("c12answer1", PEC12.answer1);
            qpe.setParameter("c12answer2", PEC12.answer2);
            if (rowCode.equals("I.1")) {
                qpe.setParameter("fromDate", filter.getFromDate());
                qpe.setParameter("toDate", filter.getToDate());
            }
            if (rowCode.equals("I.2")) {
                qpe.setParameter("fromDate", filter.getFromDate());
                qpe.setParameter("toDate", filter.getToDate());
                qpe.setParameter("c13answer1", PEc13.answer1);
                qpe.setParameter("c13answer2", PEc13.answer2);
                qpe.setParameter("c13answer3", PEc13.answer3);
            }
            if (rowCode.equals("I.3")) {
                qpe.setParameter("fromDate", filter.getFromDate());
                qpe.setParameter("toDate", filter.getToDate());
                qpe.setParameter("c13answer1", PEc13.answer1);
                qpe.setParameter("c131ResultAnswer2", PEC131Result.answer2);
            }
            if (rowCode.equals("I.4")) {
                qpe.setParameter("fromDate", filter.getFromDate());
                qpe.setParameter("toDate", filter.getToDate());
                qpe.setParameter("c13answer1", PEc13.answer1);
                qpe.setParameter("c131ResultAnswer2", PEC131Result.answer2);

                qpe.setParameter("b2", PEC16.answer2);
            }
            if (rowCode.equals("I.5")) {
                qpe.setParameter("fromDate", filter.getFromDate());
                qpe.setParameter("toDate", filter.getToDate());
            }
            if (rowCode.equals("I.6")) {
                qpe.setParameter("fromDate", filter.getFromDate());
                qpe.setParameter("toDate", filter.getToDate());
//          qpe.setParameter("c12answer1", PEC12.answer1);
                qpe.setParameter("b2", PEC16.answer2);
            }
            if (rowCode.equals("I.7")) {
                qpe.setParameter("fromDate", filter.getFromDate());
                qpe.setParameter("toDate", filter.getToDate());
            }
            if (rowCode.equals("II.1")) {
                qpe.setParameter("fromDate", filter.getFromDate());
                qpe.setParameter("toDate", filter.getToDate());
                qpe.setParameter("c16answer1", PEC16.answer1);
                qpe.setParameter("c131ResultAnswer2", PEC131Result.answer2);
            }
            if (rowCode.equals("II.2")) {
                qpe.setParameter("fromDate", filter.getFromDate());
                qpe.setParameter("toDate", filter.getToDate());
                qpe.setParameter("c16answer2", PEC16.answer2);
                qpe.setParameter("c131ResultAnswer2", PEC131Result.answer2);
            }
            if (rowCode.equals("II.3")) {
                qpe.setParameter("fromDate", filter.getFromDate());
                qpe.setParameter("toDate", filter.getToDate());
                qpe.setParameter("c16answer3", PEC16.answer3);
                qpe.setParameter("c131ResultAnswer2", PEC131Result.answer2);
            }
            if (rowCode.equals("II.4")) {
                qpe.setParameter("fromDate", filter.getFromDate());
                qpe.setParameter("toDate", filter.getToDate());
                qpe.setParameter("c131ResultAnswer2", PEC131Result.answer2);
            }
            if (rowCode.equals("III.1")) {
                qpe.setParameter("fromDate", filter.getFromDate());
                qpe.setParameter("toDate", filter.getToDate());
            }
            if (rowCode.equals("III.2")) {
                qpe.setParameter("fromDate", filter.getFromDate());
                qpe.setParameter("toDate", filter.getToDate());
            }
            if (rowCode.equals("III.3")) {
                qpe.setParameter("fromDate", filter.getFromDate());
                qpe.setParameter("toDate", filter.getToDate());
            }
            if (rowCode.equals("III.4")) {
                qpe.setParameter("fromDate", filter.getFromDate());
                qpe.setParameter("toDate", filter.getToDate());
            }
            if (rowCode.equals("III.5")) {
                qpe.setParameter("fromDate", filter.getFromDate());
                qpe.setParameter("toDate", filter.getToDate());
            }

            if (rowCode.equals("IV.1")) {
                qpe.setParameter("fromDate", filter.getFromDate());
                qpe.setParameter("toDate", filter.getToDate());
            }
            if (rowCode.equals("IV.2")) {
                qpe.setParameter("fromDate", filter.getFromDate());
                qpe.setParameter("toDate", filter.getToDate());
            }
            if (rowCode.equals("IV.3")) {
                qpe.setParameter("fromDate", filter.getFromDate());
                qpe.setParameter("toDate", filter.getToDate());
                qpe.setParameter("c131ResultAnswer2", PEC131Result.answer2);
            }
            if (rowCode.equals("IV.4")) {
                qpe.setParameter("fromDate", filter.getFromDate());
                qpe.setParameter("toDate", filter.getToDate());
            }

            Long ret = (Long) qpe.getSingleResult();
            if (ret != null) {
                return ret.intValue();
            }
            return 0;
        } else {
            String SQLHTS = " SELECT COUNT(s.id) from HTSCase s WHERE s.c2.id in (:listOrg) ";
            String whereClauseHTS = " ";

            if (col == 2) {
                whereClauseHTS += " and s.c5Note =:c5NoteAnswer1 ";
                if (rowCode.equals("I.1")) {
                    whereClauseHTS += " AND s.c4 >=:fromDate AND s.c4 <=:toDate  ";
                }
                if (rowCode.equals("I.2")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate ";
                }
                if (rowCode.equals("I.3")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 ";
                }
                if (rowCode.equals("I.4")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and (s.c24 is null or s.c24 !=:c24answer2) and (s.c11c is null or s.c11c !=:c11cYes or s.c11b =:c11bAnswer1) ";
                }
                if (rowCode.equals("I.5")) {
                    whereClauseHTS += " AND s.c20RegDate >=:fromDate AND s.c20RegDate <=:toDate ";
                }
                if (rowCode.equals("I.6")) {
                    whereClauseHTS += " AND s.c20RegDate >=:fromDate AND s.c20RegDate <=:toDate and (s.c24 is null or s.c24 !=:c24answer2) and (s.c11c is null or s.c11c !=:c11cYes or s.c11b =:c11bAnswer1) ";
                }
                if (rowCode.equals("I.7")) {
                    whereClauseHTS += " AND s.c1627Date >=:fromDate AND s.c1627Date <=:toDate ";
                }
                if (rowCode.equals("II.1")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c24 =:c24answer1 ";
                }
                if (rowCode.equals("II.2")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c24 =:c24answer2 ";
                }
                if (rowCode.equals("II.3")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c24 =:c24answer3 ";
                }
                if (rowCode.equals("II.4")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c24 is null ";
                }
                if (rowCode.equals("III.1")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and (s.c17 =:c17answer1 or s.c17 =:c17answer2 ) ";
                }
                if (rowCode.equals("III.2")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c17 =:c17answer1 ";
                }
                if (rowCode.equals("III.3")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c17 =:c17answer1 and (s.c18 =:c18answer1 or s.c18 =:c18answer2 ) ";
                }
                if (rowCode.equals("III.4")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c17 =:c17answer1 and s.c18 =:c18answer1 ";
                }
                if (rowCode.equals("III.5")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c17 =:c17answer1 and s.c18 =:c18answer2 ";
                }

                if (rowCode.equals("IV.1")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c10 =:c10answer1 ";
                }
                if (rowCode.equals("IV.2")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c10 =:c10answer2 ";
                }
                if (rowCode.equals("IV.3")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c10 =:c10answer3 ";
                }
                if (rowCode.equals("IV.4")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c10 =:c10answer4 ";
                }
            }
            if (col == 3) {
                whereClauseHTS += " and s.c5Note =:c5NoteAnswer2 ";
                if (rowCode.equals("I.1")) {
                    whereClauseHTS += " AND s.c4 >=:fromDate AND s.c4 <=:toDate  ";
                }
                if (rowCode.equals("I.2")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate ";
                }
                if (rowCode.equals("I.3")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 ";
                }
                if (rowCode.equals("I.4")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and (s.c24 is null or s.c24 !=:c24answer2) and (s.c11c is null or s.c11c !=:c11cYes or s.c11b =:c11bAnswer1) ";
                }
                if (rowCode.equals("I.5")) {
                    whereClauseHTS += " AND s.c20RegDate >=:fromDate AND s.c20RegDate <=:toDate ";
                }
                if (rowCode.equals("I.6")) {
                    whereClauseHTS += " AND s.c20RegDate >=:fromDate AND s.c20RegDate <=:toDate and (s.c24 is null or s.c24 !=:c24answer2) and (s.c11c is null or s.c11c !=:c11cYes or s.c11b =:c11bAnswer1) ";
                }
                if (rowCode.equals("I.7")) {
                    whereClauseHTS += " AND s.c1627Date >=:fromDate AND s.c1627Date <=:toDate ";
                }
                if (rowCode.equals("II.1")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c24 =:c24answer1 ";
                }
                if (rowCode.equals("II.2")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c24 =:c24answer2 ";
                }
                if (rowCode.equals("II.3")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c24 =:c24answer3 ";
                }
                if (rowCode.equals("II.4")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c24 is null ";
                }
                if (rowCode.equals("III.1")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and (s.c17 =:c17answer1 or s.c17 =:c17answer2 ) ";
                }
                if (rowCode.equals("III.2")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c17 =:c17answer1 ";
                }
                if (rowCode.equals("III.3")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c17 =:c17answer1 and (s.c18 =:c18answer1 or s.c18 =:c18answer2 ) ";
                }
                if (rowCode.equals("III.4")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c17 =:c17answer1 and s.c18 =:c18answer1 ";
                }
                if (rowCode.equals("III.5")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c17 =:c17answer1 and s.c18 =:c18answer2 ";
                }

                if (rowCode.equals("IV.1")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c10 =:c10answer1 ";
                }
                if (rowCode.equals("IV.2")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c10 =:c10answer2 ";
                }
                if (rowCode.equals("IV.3")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c10 =:c10answer3 ";
                }
                if (rowCode.equals("IV.4")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c10 =:c10answer4 ";
                }
            }
            if (col == 4) {
                whereClauseHTS += " and s.c5Note =:c5NoteAnswer3 ";
                if (rowCode.equals("I.1")) {
                    whereClauseHTS += " AND s.c4 >=:fromDate AND s.c4 <=:toDate  ";
                }
                if (rowCode.equals("I.2")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate ";
                }
                if (rowCode.equals("I.3")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 ";
                }
                if (rowCode.equals("I.4")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and (s.c24 is null or s.c24 !=:c24answer2) and (s.c11c is null or s.c11c !=:c11cYes or s.c11b =:c11bAnswer1) ";
                }
                if (rowCode.equals("I.5")) {
                    whereClauseHTS += " AND s.c20RegDate >=:fromDate AND s.c20RegDate <=:toDate ";
                }
                if (rowCode.equals("I.6")) {
                    whereClauseHTS += " AND s.c20RegDate >=:fromDate AND s.c20RegDate <=:toDate and (s.c24 is null or s.c24 !=:c24answer2) and (s.c11c is null or s.c11c !=:c11cYes or s.c11b =:c11bAnswer1) ";
                }
                if (rowCode.equals("I.7")) {
                    whereClauseHTS += " AND s.c1627Date >=:fromDate AND s.c1627Date <=:toDate ";
                }
                if (rowCode.equals("II.1")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c24 =:c24answer1 ";
                }
                if (rowCode.equals("II.2")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c24 =:c24answer2 ";
                }
                if (rowCode.equals("II.3")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c24 =:c24answer3 ";
                }
                if (rowCode.equals("II.4")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c24 is null ";
                }
                if (rowCode.equals("III.1")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and (s.c17 =:c17answer1 or s.c17 =:c17answer2 ) ";
                }
                if (rowCode.equals("III.2")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c17 =:c17answer1 ";
                }
                if (rowCode.equals("III.3")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c17 =:c17answer1 and (s.c18 =:c18answer1 or s.c18 =:c18answer2 ) ";
                }
                if (rowCode.equals("III.4")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c17 =:c17answer1 and s.c18 =:c18answer1 ";
                }
                if (rowCode.equals("III.5")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c17 =:c17answer1 and s.c18 =:c18answer2 ";
                }

                if (rowCode.equals("IV.1")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c10 =:c10answer1 ";
                }
                if (rowCode.equals("IV.2")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c10 =:c10answer2 ";
                }
                if (rowCode.equals("IV.3")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c10 =:c10answer3 ";
                }
                if (rowCode.equals("IV.4")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c10 =:c10answer4 ";
                }
            }
            if (col == 5) {
                whereClauseHTS += " and s.c5Note =:c5NoteAnswer4 ";
                if (rowCode.equals("I.1")) {
                    whereClauseHTS += " AND s.c4 >=:fromDate AND s.c4 <=:toDate  ";
                }
                if (rowCode.equals("I.2")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate ";
                }
                if (rowCode.equals("I.3")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 ";
                }
                if (rowCode.equals("I.4")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and (s.c24 is null or s.c24 !=:c24answer2) and (s.c11c is null or s.c11c !=:c11cYes or s.c11b =:c11bAnswer1) ";
                }
                if (rowCode.equals("I.5")) {
                    whereClauseHTS += " AND s.c20RegDate >=:fromDate AND s.c20RegDate <=:toDate ";
                }
                if (rowCode.equals("I.6")) {
                    whereClauseHTS += " AND s.c20RegDate >=:fromDate AND s.c20RegDate <=:toDate and (s.c24 is null or s.c24 !=:c24answer2) and (s.c11c is null or s.c11c !=:c11cYes or s.c11b =:c11bAnswer1) ";
                }
                if (rowCode.equals("I.7")) {
                    whereClauseHTS += " AND s.c1627Date >=:fromDate AND s.c1627Date <=:toDate ";
                }
                if (rowCode.equals("II.1")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c24 =:c24answer1 ";
                }
                if (rowCode.equals("II.2")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c24 =:c24answer2 ";
                }
                if (rowCode.equals("II.3")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c24 =:c24answer3 ";
                }
                if (rowCode.equals("II.4")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c24 is null ";
                }
                if (rowCode.equals("III.1")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and (s.c17 =:c17answer1 or s.c17 =:c17answer2 ) ";
                }
                if (rowCode.equals("III.2")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c17 =:c17answer1 ";
                }
                if (rowCode.equals("III.3")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c17 =:c17answer1 and (s.c18 =:c18answer1 or s.c18 =:c18answer2 ) ";
                }
                if (rowCode.equals("III.4")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c17 =:c17answer1 and s.c18 =:c18answer1 ";
                }
                if (rowCode.equals("III.5")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c17 =:c17answer1 and s.c18 =:c18answer2 ";
                }

                if (rowCode.equals("IV.1")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c10 =:c10answer1 ";
                }
                if (rowCode.equals("IV.2")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c10 =:c10answer2 ";
                }
                if (rowCode.equals("IV.3")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c10 =:c10answer3 ";
                }
                if (rowCode.equals("IV.4")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c10 =:c10answer4 ";
                }
            }
            if (col == 6) {
                whereClauseHTS += " and s.c5Note =:c5NoteAnswer5 ";
                if (rowCode.equals("I.1")) {
                    whereClauseHTS += " AND s.c4 >=:fromDate AND s.c4 <=:toDate  ";
                }
                if (rowCode.equals("I.2")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate ";
                }
                if (rowCode.equals("I.3")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 ";
                }
                if (rowCode.equals("I.4")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and (s.c24 is null or s.c24 !=:c24answer2) and (s.c11c is null or s.c11c !=:c11cYes or s.c11b =:c11bAnswer1) ";
                }
                if (rowCode.equals("I.5")) {
                    whereClauseHTS += " AND s.c20RegDate >=:fromDate AND s.c20RegDate <=:toDate ";
                }
                if (rowCode.equals("I.6")) {
                    whereClauseHTS += " AND s.c20RegDate >=:fromDate AND s.c20RegDate <=:toDate and (s.c24 is null or s.c24 !=:c24answer2) and (s.c11c is null or s.c11c !=:c11cYes or s.c11b =:c11bAnswer1) ";
                }
                if (rowCode.equals("I.7")) {
                    whereClauseHTS += " AND s.c1627Date >=:fromDate AND s.c1627Date <=:toDate ";
                }
                if (rowCode.equals("II.1")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c24 =:c24answer1 ";
                }
                if (rowCode.equals("II.2")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c24 =:c24answer2 ";
                }
                if (rowCode.equals("II.3")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c24 =:c24answer3 ";
                }
                if (rowCode.equals("II.4")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c24 is null ";
                }
                if (rowCode.equals("III.1")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and (s.c17 =:c17answer1 or s.c17 =:c17answer2 ) ";
                }
                if (rowCode.equals("III.2")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c17 =:c17answer1 ";
                }
                if (rowCode.equals("III.3")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c17 =:c17answer1 and (s.c18 =:c18answer1 or s.c18 =:c18answer2 ) ";
                }
                if (rowCode.equals("III.4")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c17 =:c17answer1 and s.c18 =:c18answer1 ";
                }
                if (rowCode.equals("III.5")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c17 =:c17answer1 and s.c18 =:c18answer2 ";
                }

                if (rowCode.equals("IV.1")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c10 =:c10answer1 ";
                }
                if (rowCode.equals("IV.2")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c10 =:c10answer2 ";
                }
                if (rowCode.equals("IV.3")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c10 =:c10answer3 ";
                }
                if (rowCode.equals("IV.4")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c10 =:c10answer4 ";
                }
            }
            if (col == 7) {
                whereClauseHTS += " and s.c5Note =:c5NoteAnswer6 ";
                if (rowCode.equals("I.1")) {
                    whereClauseHTS += " AND s.c4 >=:fromDate AND s.c4 <=:toDate  ";
                }
                if (rowCode.equals("I.2")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate ";
                }
                if (rowCode.equals("I.3")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 ";
                }
                if (rowCode.equals("I.4")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and (s.c24 is null or s.c24 !=:c24answer2) and (s.c11c is null or s.c11c !=:c11cYes or s.c11b =:c11bAnswer1) ";
                }
                if (rowCode.equals("I.5")) {
                    whereClauseHTS += " AND s.c20RegDate >=:fromDate AND s.c20RegDate <=:toDate ";
                }
                if (rowCode.equals("I.6")) {
                    whereClauseHTS += " AND s.c20RegDate >=:fromDate AND s.c20RegDate <=:toDate and (s.c24 is null or s.c24 !=:c24answer2) and (s.c11c is null or s.c11c !=:c11cYes or s.c11b =:c11bAnswer1) ";
                }
                if (rowCode.equals("I.7")) {
                    whereClauseHTS += " AND s.c1627Date >=:fromDate AND s.c1627Date <=:toDate ";
                }
                if (rowCode.equals("II.1")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c24 =:c24answer1 ";
                }
                if (rowCode.equals("II.2")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c24 =:c24answer2 ";
                }
                if (rowCode.equals("II.3")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c24 =:c24answer3 ";
                }
                if (rowCode.equals("II.4")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c24 is null ";
                }
                if (rowCode.equals("III.1")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and (s.c17 =:c17answer1 or s.c17 =:c17answer2 ) ";
                }
                if (rowCode.equals("III.2")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c17 =:c17answer1 ";
                }
                if (rowCode.equals("III.3")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c17 =:c17answer1 and (s.c18 =:c18answer1 or s.c18 =:c18answer2 ) ";
                }
                if (rowCode.equals("III.4")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c17 =:c17answer1 and s.c18 =:c18answer1 ";
                }
                if (rowCode.equals("III.5")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c17 =:c17answer1 and s.c18 =:c18answer2 ";
                }

                if (rowCode.equals("IV.1")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c10 =:c10answer1 ";
                }
                if (rowCode.equals("IV.2")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c10 =:c10answer2 ";
                }
                if (rowCode.equals("IV.3")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c10 =:c10answer3 ";
                }
                if (rowCode.equals("IV.4")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c10 =:c10answer4 ";
                }
            }
            if (col == 8) {
                whereClauseHTS += " and s.c5 =:c5Answer1 ";
                if (rowCode.equals("I.1")) {
                    whereClauseHTS += " AND s.c4 >=:fromDate AND s.c4 <=:toDate  ";
                }
                if (rowCode.equals("I.2")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate ";
                }
                if (rowCode.equals("I.3")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 ";
                }
                if (rowCode.equals("I.4")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and (s.c24 is null or s.c24 !=:c24answer2) and (s.c11c is null or s.c11c !=:c11cYes or s.c11b =:c11bAnswer1) ";
                }
                if (rowCode.equals("I.5")) {
                    whereClauseHTS += " AND s.c20RegDate >=:fromDate AND s.c20RegDate <=:toDate ";
                }
                if (rowCode.equals("I.6")) {
                    whereClauseHTS += " AND s.c20RegDate >=:fromDate AND s.c20RegDate <=:toDate and (s.c24 is null or s.c24 !=:c24answer2) and (s.c11c is null or s.c11c !=:c11cYes or s.c11b =:c11bAnswer1) ";
                }
                if (rowCode.equals("I.7")) {
                    whereClauseHTS += " AND s.c1627Date >=:fromDate AND s.c1627Date <=:toDate ";
                }
                if (rowCode.equals("II.1")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c24 =:c24answer1 ";
                }
                if (rowCode.equals("II.2")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c24 =:c24answer2 ";
                }
                if (rowCode.equals("II.3")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c24 =:c24answer3 ";
                }
                if (rowCode.equals("II.4")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c24 is null ";
                }
                if (rowCode.equals("III.1")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and (s.c17 =:c17answer1 or s.c17 =:c17answer2 ) ";
                }
                if (rowCode.equals("III.2")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c17 =:c17answer1 ";
                }
                if (rowCode.equals("III.3")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c17 =:c17answer1 and (s.c18 =:c18answer1 or s.c18 =:c18answer2 ) ";
                }
                if (rowCode.equals("III.4")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c17 =:c17answer1 and s.c18 =:c18answer1 ";
                }
                if (rowCode.equals("III.5")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c17 =:c17answer1 and s.c18 =:c18answer2 ";
                }

                if (rowCode.equals("IV.1")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c10 =:c10answer1 ";
                }
                if (rowCode.equals("IV.2")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c10 =:c10answer2 ";
                }
                if (rowCode.equals("IV.3")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c10 =:c10answer3 ";
                }
                if (rowCode.equals("IV.4")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c10 =:c10answer4 ";
                }
            }
            if (col == 9) {
                whereClauseHTS += " and s.c5 =:c5Answer3 ";
                if (rowCode.equals("I.1")) {
                    whereClauseHTS += " AND s.c4 >=:fromDate AND s.c4 <=:toDate  ";
                }
                if (rowCode.equals("I.2")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate ";
                }
                if (rowCode.equals("I.3")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 ";
                }
                if (rowCode.equals("I.4")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and (s.c24 is null or s.c24 !=:c24answer2) and (s.c11c is null or s.c11c !=:c11cYes or s.c11b =:c11bAnswer1) ";
                }
                if (rowCode.equals("I.5")) {
                    whereClauseHTS += " AND s.c20RegDate >=:fromDate AND s.c20RegDate <=:toDate ";
                }
                if (rowCode.equals("I.6")) {
                    whereClauseHTS += " AND s.c20RegDate >=:fromDate AND s.c20RegDate <=:toDate and (s.c24 is null or s.c24 !=:c24answer2) and (s.c11c is null or s.c11c !=:c11cYes or s.c11b =:c11bAnswer1) ";
                }
                if (rowCode.equals("I.7")) {
                    whereClauseHTS += " AND s.c1627Date >=:fromDate AND s.c1627Date <=:toDate ";
                }
                if (rowCode.equals("II.1")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c24 =:c24answer1 ";
                }
                if (rowCode.equals("II.2")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c24 =:c24answer2 ";
                }
                if (rowCode.equals("II.3")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c24 =:c24answer3 ";
                }
                if (rowCode.equals("II.4")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c24 is null ";
                }
                if (rowCode.equals("III.1")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and (s.c17 =:c17answer1 or s.c17 =:c17answer2 ) ";
                }
                if (rowCode.equals("III.2")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c17 =:c17answer1 ";
                }
                if (rowCode.equals("III.3")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c17 =:c17answer1 and (s.c18 =:c18answer1 or s.c18 =:c18answer2 ) ";
                }
                if (rowCode.equals("III.4")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c17 =:c17answer1 and s.c18 =:c18answer1 ";
                }
                if (rowCode.equals("III.5")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c17 =:c17answer1 and s.c18 =:c18answer2 ";
                }

                if (rowCode.equals("IV.1")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c10 =:c10answer1 ";
                }
                if (rowCode.equals("IV.2")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c10 =:c10answer2 ";
                }
                if (rowCode.equals("IV.3")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c10 =:c10answer3 ";
                }
                if (rowCode.equals("IV.4")) {
                    whereClauseHTS += " AND s.c15Date >=:fromDate AND s.c15Date <=:toDate and s.c14 =:c14answer2 and s.c10 =:c10answer4 ";
                }
            }

            Query qhts = manager.createQuery(SQLHTS + whereClauseHTS);
            qhts.setParameter("listOrg", filter.getOrgIds());

            if (col == 2) {
                qhts.setParameter("c5NoteAnswer1", HTSc5Note.answer1);
                if (rowCode.equals("I.1")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                }
                if (rowCode.equals("I.2")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                }
                if (rowCode.equals("I.3")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                }
                if (rowCode.equals("I.4")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c24answer2", HTSc24.answer2);
                    qhts.setParameter("c11cYes", HTSYesNoNone.YES);
                    qhts.setParameter("c11bAnswer1", HTSc11b.answer1);
                }
                if (rowCode.equals("I.5")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                }
                if (rowCode.equals("I.6")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c24answer2", HTSc24.answer2);
                    qhts.setParameter("c11cYes", HTSYesNoNone.YES);
                    qhts.setParameter("c11bAnswer1", HTSc11b.answer1);
                }
                if (rowCode.equals("I.7")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                }
                if (rowCode.equals("II.1")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c24answer1", HTSc24.answer1);
                }
                if (rowCode.equals("II.2")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c24answer2", HTSc24.answer2);
                }
                if (rowCode.equals("II.3")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c24answer3", HTSc24.answer3);
                }
                if (rowCode.equals("II.4")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                }
                if (rowCode.equals("III.1")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                    qhts.setParameter("c17answer2", HTSc17.answer2);
                }
                if (rowCode.equals("III.2")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                }
                if (rowCode.equals("III.3")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                    qhts.setParameter("c18answer1", HTSc18.answer1);
                    qhts.setParameter("c18answer2", HTSc18.answer2);
                }
                if (rowCode.equals("III.4")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                    qhts.setParameter("c18answer1", HTSc18.answer1);
                }
                if (rowCode.equals("III.5")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                    qhts.setParameter("c18answer2", HTSc18.answer2);
                }

                if (rowCode.equals("IV.1")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c10answer1", HTSc10.answer1);
                }
                if (rowCode.equals("IV.2")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c10answer2", HTSc10.answer2);
                }
                if (rowCode.equals("IV.3")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c10answer3", HTSc10.answer3);
                }
                if (rowCode.equals("IV.4")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c10answer4", HTSc10.answer4);
                }
            }
            if (col == 3) {
                qhts.setParameter("c5NoteAnswer2", HTSc5Note.answer2);
                if (rowCode.equals("I.1")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                }
                if (rowCode.equals("I.2")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                }
                if (rowCode.equals("I.3")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                }
                if (rowCode.equals("I.4")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c24answer2", HTSc24.answer2);
                    qhts.setParameter("c11cYes", HTSYesNoNone.YES);
                    qhts.setParameter("c11bAnswer1", HTSc11b.answer1);
                }
                if (rowCode.equals("I.5")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                }
                if (rowCode.equals("I.6")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c24answer2", HTSc24.answer2);
                    qhts.setParameter("c11cYes", HTSYesNoNone.YES);
                    qhts.setParameter("c11bAnswer1", HTSc11b.answer1);
                }
                if (rowCode.equals("I.7")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                }
                if (rowCode.equals("II.1")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c24answer1", HTSc24.answer1);
                }
                if (rowCode.equals("II.2")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c24answer2", HTSc24.answer2);
                }
                if (rowCode.equals("II.3")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c24answer3", HTSc24.answer3);
                }
                if (rowCode.equals("II.4")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                }
                if (rowCode.equals("III.1")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                    qhts.setParameter("c17answer2", HTSc17.answer2);
                }
                if (rowCode.equals("III.2")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                }
                if (rowCode.equals("III.3")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                    qhts.setParameter("c18answer1", HTSc18.answer1);
                    qhts.setParameter("c18answer2", HTSc18.answer2);
                }
                if (rowCode.equals("III.4")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                    qhts.setParameter("c18answer1", HTSc18.answer1);
                }
                if (rowCode.equals("III.5")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                    qhts.setParameter("c18answer2", HTSc18.answer2);
                }

                if (rowCode.equals("IV.1")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c10answer1", HTSc10.answer1);
                }
                if (rowCode.equals("IV.2")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c10answer2", HTSc10.answer2);
                }
                if (rowCode.equals("IV.3")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c10answer3", HTSc10.answer3);
                }
                if (rowCode.equals("IV.4")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c10answer4", HTSc10.answer4);
                }
            }
            if (col == 4) {
                qhts.setParameter("c5NoteAnswer3", HTSc5Note.answer3);
                if (rowCode.equals("I.1")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                }
                if (rowCode.equals("I.2")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                }
                if (rowCode.equals("I.3")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                }
                if (rowCode.equals("I.4")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c24answer2", HTSc24.answer2);
                    qhts.setParameter("c11cYes", HTSYesNoNone.YES);
                    qhts.setParameter("c11bAnswer1", HTSc11b.answer1);
                }
                if (rowCode.equals("I.5")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                }
                if (rowCode.equals("I.6")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c24answer2", HTSc24.answer2);
                    qhts.setParameter("c11cYes", HTSYesNoNone.YES);
                    qhts.setParameter("c11bAnswer1", HTSc11b.answer1);
                }
                if (rowCode.equals("I.7")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                }
                if (rowCode.equals("II.1")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c24answer1", HTSc24.answer1);
                }
                if (rowCode.equals("II.2")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c24answer2", HTSc24.answer2);
                }
                if (rowCode.equals("II.3")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c24answer3", HTSc24.answer3);
                }
                if (rowCode.equals("II.4")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                }
                if (rowCode.equals("III.1")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                    qhts.setParameter("c17answer2", HTSc17.answer2);
                }
                if (rowCode.equals("III.2")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                }
                if (rowCode.equals("III.3")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                    qhts.setParameter("c18answer1", HTSc18.answer1);
                    qhts.setParameter("c18answer2", HTSc18.answer2);
                }
                if (rowCode.equals("III.4")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                    qhts.setParameter("c18answer1", HTSc18.answer1);
                }
                if (rowCode.equals("III.5")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                    qhts.setParameter("c18answer2", HTSc18.answer2);
                }

                if (rowCode.equals("IV.1")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c10answer1", HTSc10.answer1);
                }
                if (rowCode.equals("IV.2")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c10answer2", HTSc10.answer2);
                }
                if (rowCode.equals("IV.3")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c10answer3", HTSc10.answer3);
                }
                if (rowCode.equals("IV.4")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c10answer4", HTSc10.answer4);
                }
            }
            if (col == 5) {
                qhts.setParameter("c5NoteAnswer4", HTSc5Note.answer4);
                if (rowCode.equals("I.1")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                }
                if (rowCode.equals("I.2")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                }
                if (rowCode.equals("I.3")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                }
                if (rowCode.equals("I.4")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c24answer2", HTSc24.answer2);
                    qhts.setParameter("c11cYes", HTSYesNoNone.YES);
                    qhts.setParameter("c11bAnswer1", HTSc11b.answer1);
                }
                if (rowCode.equals("I.5")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                }
                if (rowCode.equals("I.6")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c24answer2", HTSc24.answer2);
                    qhts.setParameter("c11cYes", HTSYesNoNone.YES);
                    qhts.setParameter("c11bAnswer1", HTSc11b.answer1);
                }
                if (rowCode.equals("I.7")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                }
                if (rowCode.equals("II.1")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c24answer1", HTSc24.answer1);
                }
                if (rowCode.equals("II.2")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c24answer2", HTSc24.answer2);
                }
                if (rowCode.equals("II.3")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c24answer3", HTSc24.answer3);
                }
                if (rowCode.equals("II.4")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                }
                if (rowCode.equals("III.1")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                    qhts.setParameter("c17answer2", HTSc17.answer2);
                }
                if (rowCode.equals("III.2")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                }
                if (rowCode.equals("III.3")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                    qhts.setParameter("c18answer1", HTSc18.answer1);
                    qhts.setParameter("c18answer2", HTSc18.answer2);
                }
                if (rowCode.equals("III.4")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                    qhts.setParameter("c18answer1", HTSc18.answer1);
                }
                if (rowCode.equals("III.5")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                    qhts.setParameter("c18answer2", HTSc18.answer2);
                }

                if (rowCode.equals("IV.1")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c10answer1", HTSc10.answer1);
                }
                if (rowCode.equals("IV.2")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c10answer2", HTSc10.answer2);
                }
                if (rowCode.equals("IV.3")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c10answer3", HTSc10.answer3);
                }
                if (rowCode.equals("IV.4")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c10answer4", HTSc10.answer4);
                }
            }
            if (col == 6) {
                qhts.setParameter("c5NoteAnswer5", HTSc5Note.answer5);
                if (rowCode.equals("I.1")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                }
                if (rowCode.equals("I.2")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                }
                if (rowCode.equals("I.3")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                }
                if (rowCode.equals("I.4")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c24answer2", HTSc24.answer2);
                    qhts.setParameter("c11cYes", HTSYesNoNone.YES);
                    qhts.setParameter("c11bAnswer1", HTSc11b.answer1);
                }
                if (rowCode.equals("I.5")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                }
                if (rowCode.equals("I.6")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c24answer2", HTSc24.answer2);
                    qhts.setParameter("c11cYes", HTSYesNoNone.YES);
                    qhts.setParameter("c11bAnswer1", HTSc11b.answer1);
                }
                if (rowCode.equals("I.7")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                }
                if (rowCode.equals("II.1")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c24answer1", HTSc24.answer1);
                }
                if (rowCode.equals("II.2")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c24answer2", HTSc24.answer2);
                }
                if (rowCode.equals("II.3")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c24answer3", HTSc24.answer3);
                }
                if (rowCode.equals("II.4")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                }
                if (rowCode.equals("III.1")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                    qhts.setParameter("c17answer2", HTSc17.answer2);
                }
                if (rowCode.equals("III.2")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                }
                if (rowCode.equals("III.3")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                    qhts.setParameter("c18answer1", HTSc18.answer1);
                    qhts.setParameter("c18answer2", HTSc18.answer2);
                }
                if (rowCode.equals("III.4")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                    qhts.setParameter("c18answer1", HTSc18.answer1);
                }
                if (rowCode.equals("III.5")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                    qhts.setParameter("c18answer2", HTSc18.answer2);
                }

                if (rowCode.equals("IV.1")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c10answer1", HTSc10.answer1);
                }
                if (rowCode.equals("IV.2")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c10answer2", HTSc10.answer2);
                }
                if (rowCode.equals("IV.3")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c10answer3", HTSc10.answer3);
                }
                if (rowCode.equals("IV.4")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c10answer4", HTSc10.answer4);
                }
            }
            if (col == 7) {
                qhts.setParameter("c5NoteAnswer6", HTSc5Note.answer6);
                if (rowCode.equals("I.1")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                }
                if (rowCode.equals("I.2")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                }
                if (rowCode.equals("I.3")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                }
                if (rowCode.equals("I.4")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c24answer2", HTSc24.answer2);
                    qhts.setParameter("c11cYes", HTSYesNoNone.YES);
                    qhts.setParameter("c11bAnswer1", HTSc11b.answer1);
                }
                if (rowCode.equals("I.5")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                }
                if (rowCode.equals("I.6")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c24answer2", HTSc24.answer2);
                    qhts.setParameter("c11cYes", HTSYesNoNone.YES);
                    qhts.setParameter("c11bAnswer1", HTSc11b.answer1);
                }
                if (rowCode.equals("I.7")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                }
                if (rowCode.equals("II.1")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c24answer1", HTSc24.answer1);
                }
                if (rowCode.equals("II.2")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c24answer2", HTSc24.answer2);
                }
                if (rowCode.equals("II.3")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c24answer3", HTSc24.answer3);
                }
                if (rowCode.equals("II.4")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                }
                if (rowCode.equals("III.1")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                    qhts.setParameter("c17answer2", HTSc17.answer2);
                }
                if (rowCode.equals("III.2")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                }
                if (rowCode.equals("III.3")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                    qhts.setParameter("c18answer1", HTSc18.answer1);
                    qhts.setParameter("c18answer2", HTSc18.answer2);
                }
                if (rowCode.equals("III.4")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                    qhts.setParameter("c18answer1", HTSc18.answer1);
                }
                if (rowCode.equals("III.5")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                    qhts.setParameter("c18answer2", HTSc18.answer2);
                }

                if (rowCode.equals("IV.1")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c10answer1", HTSc10.answer1);
                }
                if (rowCode.equals("IV.2")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c10answer2", HTSc10.answer2);
                }
                if (rowCode.equals("IV.3")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c10answer3", HTSc10.answer3);
                }
                if (rowCode.equals("IV.4")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c10answer4", HTSc10.answer4);
                }
            }
            if (col == 8) {
                qhts.setParameter("c5Answer1", HTSc5.answer1);
                if (rowCode.equals("I.1")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                }
                if (rowCode.equals("I.2")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                }
                if (rowCode.equals("I.3")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                }
                if (rowCode.equals("I.4")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c24answer2", HTSc24.answer2);
                    qhts.setParameter("c11cYes", HTSYesNoNone.YES);
                    qhts.setParameter("c11bAnswer1", HTSc11b.answer1);
                }
                if (rowCode.equals("I.5")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                }
                if (rowCode.equals("I.6")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c24answer2", HTSc24.answer2);
                    qhts.setParameter("c11cYes", HTSYesNoNone.YES);
                    qhts.setParameter("c11bAnswer1", HTSc11b.answer1);
                }
                if (rowCode.equals("I.7")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                }
                if (rowCode.equals("II.1")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c24answer1", HTSc24.answer1);
                }
                if (rowCode.equals("II.2")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c24answer2", HTSc24.answer2);
                }
                if (rowCode.equals("II.3")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c24answer3", HTSc24.answer3);
                }
                if (rowCode.equals("II.4")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                }
                if (rowCode.equals("III.1")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                    qhts.setParameter("c17answer2", HTSc17.answer2);
                }
                if (rowCode.equals("III.2")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                }
                if (rowCode.equals("III.3")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                    qhts.setParameter("c18answer1", HTSc18.answer1);
                    qhts.setParameter("c18answer2", HTSc18.answer2);
                }
                if (rowCode.equals("III.4")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                    qhts.setParameter("c18answer1", HTSc18.answer1);
                }
                if (rowCode.equals("III.5")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                    qhts.setParameter("c18answer2", HTSc18.answer2);
                }

                if (rowCode.equals("IV.1")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c10answer1", HTSc10.answer1);
                }
                if (rowCode.equals("IV.2")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c10answer2", HTSc10.answer2);
                }
                if (rowCode.equals("IV.3")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c10answer3", HTSc10.answer3);
                }
                if (rowCode.equals("IV.4")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c10answer4", HTSc10.answer4);
                }
            }
            if (col == 9) {
                qhts.setParameter("c5Answer3", HTSc5.answer3);
                if (rowCode.equals("I.1")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                }
                if (rowCode.equals("I.2")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                }
                if (rowCode.equals("I.3")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                }
                if (rowCode.equals("I.4")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c24answer2", HTSc24.answer2);
                    qhts.setParameter("c11cYes", HTSYesNoNone.YES);
                    qhts.setParameter("c11bAnswer1", HTSc11b.answer1);
                }
                if (rowCode.equals("I.5")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                }
                if (rowCode.equals("I.6")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c24answer2", HTSc24.answer2);
                    qhts.setParameter("c11cYes", HTSYesNoNone.YES);
                    qhts.setParameter("c11bAnswer1", HTSc11b.answer1);
                }
                if (rowCode.equals("I.7")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                }
                if (rowCode.equals("II.1")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c24answer1", HTSc24.answer1);
                }
                if (rowCode.equals("II.2")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c24answer2", HTSc24.answer2);
                }
                if (rowCode.equals("II.3")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c24answer3", HTSc24.answer3);
                }
                if (rowCode.equals("II.4")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                }
                if (rowCode.equals("III.1")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                    qhts.setParameter("c17answer2", HTSc17.answer2);
                }
                if (rowCode.equals("III.2")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                }
                if (rowCode.equals("III.3")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                    qhts.setParameter("c18answer1", HTSc18.answer1);
                    qhts.setParameter("c18answer2", HTSc18.answer2);
                }
                if (rowCode.equals("III.4")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                    qhts.setParameter("c18answer1", HTSc18.answer1);
                }
                if (rowCode.equals("III.5")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c17answer1", HTSc17.answer1);
                    qhts.setParameter("c18answer2", HTSc18.answer2);
                }

                if (rowCode.equals("IV.1")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c10answer1", HTSc10.answer1);
                }
                if (rowCode.equals("IV.2")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c10answer2", HTSc10.answer2);
                }
                if (rowCode.equals("IV.3")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c10answer3", HTSc10.answer3);
                }
                if (rowCode.equals("IV.4")) {
                    qhts.setParameter("fromDate", filter.getFromDate());
                    qhts.setParameter("toDate", filter.getToDate());
                    qhts.setParameter("c14answer2", HTSc14.answer2);
                    qhts.setParameter("c10answer4", HTSc10.answer4);
                }
            }
            Long ret = (Long) qhts.getSingleResult();
            if (ret != null) {
                return ret.intValue();
            }
            return 0;

        }
    }

    private Integer queryReportUpper20(PreventionFilterDto filter, String rowCode, int col, int orderNumber) {

        if (col == 1) {
            String SQLPE = " SELECT COUNT(s.id) from PECaseRiskGroup s WHERE s.peCase.c1Org.id in (:listOrg) AND (s.peCase.c12=:c12answer1 OR s.peCase.c12=:c12answer2) ";
            String whereClausePE = " AND s.peCase.c11Date >=:fromDate AND s.peCase.c11Date <=:toDate and s.peCase.c131Result =:answer2 and s.isMainRisk = true ";

            if (rowCode.equals("V.1")) {
                whereClausePE += " and s.val =:val1 ";
            }
            if (rowCode.equals("V.2")) {
                whereClausePE += " and s.val =:val2 ";
            }
            if (rowCode.equals("V.3")) {
                whereClausePE += " and s.val =:val3 ";
            }
            if (rowCode.equals("V.4")) {
                whereClausePE += " and s.val =:val4 ";
            }
            if (rowCode.equals("V.5")) {
                whereClausePE += " and s.val =:val5 ";
            }
            if (rowCode.equals("V.6")) {
                whereClausePE += " and s.val =:val6 ";
            }
            if (rowCode.equals("V.7")) {
                return 0;
            }
            if (rowCode.equals("V.8")) {
                return 0;
            }

            Query qpe = manager.createQuery(SQLPE + whereClausePE);
            qpe.setParameter("listOrg", filter.getOrgIds());
            qpe.setParameter("fromDate", filter.getFromDate());
            qpe.setParameter("toDate", filter.getToDate());
            qpe.setParameter("answer2", PEC131Result.answer2);
            qpe.setParameter("c12answer1", PEC12.answer1);
            qpe.setParameter("c12answer2", PEC12.answer2);
            if (rowCode.equals("V.1")) {
                qpe.setParameter("val1", PERiskGroupEnum.answer1);
            }
            if (rowCode.equals("V.2")) {
                qpe.setParameter("val2", PERiskGroupEnum.answer2);
            }
            if (rowCode.equals("V.3")) {
                qpe.setParameter("val3", PERiskGroupEnum.answer3);
            }
            if (rowCode.equals("V.4")) {
                qpe.setParameter("val4", PERiskGroupEnum.answer4);
            }
            if (rowCode.equals("V.5")) {
                qpe.setParameter("val5", PERiskGroupEnum.answer5);
            }
            if (rowCode.equals("V.6")) {
                qpe.setParameter("val6", PERiskGroupEnum.answer6);
            }
//        if (rowCode .equals("V.7")) {
//          qpe.setParameter("val7", PERiskGroupEnum.UNKNOWN);
//        }
//        if (rowCode .equals("V.8")) {
//          qpe.setParameter("val8", PERiskGroupEnum.UNKNOWN);
//        }

            Long ret = (Long) qpe.getSingleResult();
            if (ret != null) {
                return ret.intValue();
            }
            return 0;
        } else {
            String SQLHTS = " SELECT COUNT(s.id) from HTSCaseRiskGroup s WHERE s.htsCase.c2.id in (:listOrg) ";
            String whereClauseHTS = " AND s.htsCase.c15Date >=:fromDate AND s.htsCase.c15Date <=:toDate and s.htsCase.c14 =:c14answer2 and s.isMainRisk = true ";
            if (col == 2) {
                whereClauseHTS += " and s.htsCase.c5Note =:c5NoteAnswer1";
                if (rowCode.equals("V.1")) {
                    whereClauseHTS += " and s.val =:val1 ";
                }
                if (rowCode.equals("V.2")) {
                    whereClauseHTS += " and s.val =:val2 ";
                }
                if (rowCode.equals("V.3")) {
                    whereClauseHTS += " and s.val =:val3 ";
                }
                if (rowCode.equals("V.4")) {
                    whereClauseHTS += " and s.val =:val4 ";
                }
                if (rowCode.equals("V.5")) {
                    whereClauseHTS += " and s.val =:val5 ";
                }
                if (rowCode.equals("V.6")) {
                    whereClauseHTS += " and s.val =:val6 ";
                }
                if (rowCode.equals("V.7")) {
                    whereClauseHTS += " and s.val =:val7 ";
                }
                if (rowCode.equals("V.8")) {
                    whereClauseHTS += " and s.val !=:val1 and s.val !=:val2 and s.val !=:val3 and s.val !=:val4 and s.val !=:val5 and s.val !=:val6 and s.val !=:val7 ";
                }
            }
            if (col == 3) {
                whereClauseHTS += " and s.htsCase.c5Note =:c5NoteAnswer1";
                if (rowCode.equals("V.1")) {
                    whereClauseHTS += " and s.val =:val1 ";
                }
                if (rowCode.equals("V.2")) {
                    whereClauseHTS += " and s.val =:val2 ";
                }
                if (rowCode.equals("V.3")) {
                    whereClauseHTS += " and s.val =:val3 ";
                }
                if (rowCode.equals("V.4")) {
                    whereClauseHTS += " and s.val =:val4 ";
                }
                if (rowCode.equals("V.5")) {
                    whereClauseHTS += " and s.val =:val5 ";
                }
                if (rowCode.equals("V.6")) {
                    whereClauseHTS += " and s.val =:val6 ";
                }
                if (rowCode.equals("V.7")) {
                    whereClauseHTS += " and s.val =:val7 ";
                }
                if (rowCode.equals("V.8")) {
                    whereClauseHTS += " and s.val !=:val1 and s.val !=:val2 and s.val !=:val3 and s.val !=:val4 and s.val !=:val5 and s.val !=:val6 and s.val !=:val7 ";
                }
            }
            if (col == 4) {
                whereClauseHTS += " and s.htsCase.c5Note =:c5NoteAnswer1";
                if (rowCode.equals("V.1")) {
                    whereClauseHTS += " and s.val =:val1 ";
                }
                if (rowCode.equals("V.2")) {
                    whereClauseHTS += " and s.val =:val2 ";
                }
                if (rowCode.equals("V.3")) {
                    whereClauseHTS += " and s.val =:val3 ";
                }
                if (rowCode.equals("V.4")) {
                    whereClauseHTS += " and s.val =:val4 ";
                }
                if (rowCode.equals("V.5")) {
                    whereClauseHTS += " and s.val =:val5 ";
                }
                if (rowCode.equals("V.6")) {
                    whereClauseHTS += " and s.val =:val6 ";
                }
                if (rowCode.equals("V.7")) {
                    whereClauseHTS += " and s.val =:val7 ";
                }
                if (rowCode.equals("V.8")) {
                    whereClauseHTS += " and s.val !=:val1 and s.val !=:val2 and s.val !=:val3 and s.val !=:val4 and s.val !=:val5 and s.val !=:val6 and s.val !=:val7 ";
                }
            }
            if (col == 5) {
                whereClauseHTS += " and s.htsCase.c5Note =:c5NoteAnswer1 ";
                if (rowCode.equals("V.1")) {
                    whereClauseHTS += " and s.val =:val1 ";
                }
                if (rowCode.equals("V.2")) {
                    whereClauseHTS += " and s.val =:val2 ";
                }
                if (rowCode.equals("V.3")) {
                    whereClauseHTS += " and s.val =:val3 ";
                }
                if (rowCode.equals("V.4")) {
                    whereClauseHTS += " and s.val =:val4 ";
                }
                if (rowCode.equals("V.5")) {
                    whereClauseHTS += " and s.val =:val5 ";
                }
                if (rowCode.equals("V.6")) {
                    whereClauseHTS += " and s.val =:val6 ";
                }
                if (rowCode.equals("V.7")) {
                    whereClauseHTS += " and s.val =:val7 ";
                }
                if (rowCode.equals("V.8")) {
                    whereClauseHTS += " and s.val !=:val1 and s.val !=:val2 and s.val !=:val3 and s.val !=:val4 and s.val !=:val5 and s.val !=:val6 and s.val !=:val7 ";
                }
            }
            if (col == 6) {
                whereClauseHTS += " and s.htsCase.c5Note =:c5NoteAnswer1 ";
                if (rowCode.equals("V.1")) {
                    whereClauseHTS += " and s.val =:val1 ";
                }
                if (rowCode.equals("V.2")) {
                    whereClauseHTS += " and s.val =:val2 ";
                }
                if (rowCode.equals("V.3")) {
                    whereClauseHTS += " and s.val =:val3 ";
                }
                if (rowCode.equals("V.4")) {
                    whereClauseHTS += " and s.val =:val4 ";
                }
                if (rowCode.equals("V.5")) {
                    whereClauseHTS += " and s.val =:val5 ";
                }
                if (rowCode.equals("V.6")) {
                    whereClauseHTS += " and s.val =:val6 ";
                }
                if (rowCode.equals("V.7")) {
                    whereClauseHTS += " and s.val =:val7 ";
                }
                if (rowCode.equals("V.8")) {
                    whereClauseHTS += " and s.val !=:val1 and s.val !=:val2 and s.val !=:val3 and s.val !=:val4 and s.val !=:val5 and s.val !=:val6 and s.val !=:val7 ";
                }
            }
            if (col == 7) {
                whereClauseHTS += " and s.htsCase.c5Note =:c5NoteAnswer1 ";
                if (rowCode.equals("V.1")) {
                    whereClauseHTS += " and s.val =:val1 ";
                }
                if (rowCode.equals("V.2")) {
                    whereClauseHTS += " and s.val =:val2 ";
                }
                if (rowCode.equals("V.3")) {
                    whereClauseHTS += " and s.val =:val3 ";
                }
                if (rowCode.equals("V.4")) {
                    whereClauseHTS += " and s.val =:val4 ";
                }
                if (rowCode.equals("V.5")) {
                    whereClauseHTS += " and s.val =:val5 ";
                }
                if (rowCode.equals("V.6")) {
                    whereClauseHTS += " and s.val =:val6 ";
                }
                if (rowCode.equals("V.7")) {
                    whereClauseHTS += " and s.val =:val7 ";
                }
                if (rowCode.equals("V.8")) {
                    whereClauseHTS += " and s.val !=:val1 and s.val !=:val2 and s.val !=:val3 and s.val !=:val4 and s.val !=:val5 and s.val !=:val6 and s.val !=:val7 ";
                }
            }
            if (col == 8) {
                whereClauseHTS += " and s.htsCase.c5 =:c5NoteAnswer1";
                if (rowCode.equals("V.1")) {
                    whereClauseHTS += " and s.val =:val1 ";
                }
                if (rowCode.equals("V.2")) {
                    whereClauseHTS += " and s.val =:val2 ";
                }
                if (rowCode.equals("V.3")) {
                    whereClauseHTS += " and s.val =:val3 ";
                }
                if (rowCode.equals("V.4")) {
                    whereClauseHTS += " and s.val =:val4 ";
                }
                if (rowCode.equals("V.5")) {
                    whereClauseHTS += " and s.val =:val5 ";
                }
                if (rowCode.equals("V.6")) {
                    whereClauseHTS += " and s.val =:val6 ";
                }
                if (rowCode.equals("V.7")) {
                    whereClauseHTS += " and s.val =:val7 ";
                }
                if (rowCode.equals("V.8")) {
                    whereClauseHTS += " and s.val !=:val1 and s.val !=:val2 and s.val !=:val3 and s.val !=:val4 and s.val !=:val5 and s.val !=:val6 and s.val !=:val7 ";
                }
            }
            if (col == 9) {
                whereClauseHTS += " and s.htsCase.c5 =:c5NoteAnswer3";
                if (rowCode.equals("V.1")) {
                    whereClauseHTS += " and s.val =:val1 ";
                }
                if (rowCode.equals("V.2")) {
                    whereClauseHTS += " and s.val =:val2 ";
                }
                if (rowCode.equals("V.3")) {
                    whereClauseHTS += " and s.val =:val3 ";
                }
                if (rowCode.equals("V.4")) {
                    whereClauseHTS += " and s.val =:val4 ";
                }
                if (rowCode.equals("V.5")) {
                    whereClauseHTS += " and s.val =:val5 ";
                }
                if (rowCode.equals("V.6")) {
                    whereClauseHTS += " and s.val =:val6 ";
                }
                if (rowCode.equals("V.7")) {
                    whereClauseHTS += " and s.val =:val7 ";
                }
                if (rowCode.equals("V.8")) {
                    whereClauseHTS += " and s.val !=:val1 and s.val !=:val2 and s.val !=:val3 and s.val !=:val4 and s.val !=:val5 and s.val !=:val6 and s.val !=:val7 ";
                }
            }
            Query qhts = manager.createQuery(SQLHTS + whereClauseHTS);
            qhts.setParameter("listOrg", filter.getOrgIds());
            qhts.setParameter("fromDate", filter.getFromDate());
            qhts.setParameter("toDate", filter.getToDate());
            qhts.setParameter("c14answer2", HTSc14.answer2);

            if (col == 2) {
                qhts.setParameter("c5NoteAnswer1", HTSc5Note.answer1);
                if (rowCode.equals("V.1")) {
                    qhts.setParameter("val1", HTSRiskGroupEnum.answer1);
                }
                if (rowCode.equals("V.2")) {
                    qhts.setParameter("val2", HTSRiskGroupEnum.answer2);
                }
                if (rowCode.equals("V.3")) {
                    qhts.setParameter("val3", HTSRiskGroupEnum.answer3);
                }
                if (rowCode.equals("V.4")) {
                    qhts.setParameter("val4", HTSRiskGroupEnum.answer4);
                }
                if (rowCode.equals("V.5")) {
                    qhts.setParameter("val5", HTSRiskGroupEnum.answer5);
                }
                if (rowCode.equals("V.6")) {
                    qhts.setParameter("val6", HTSRiskGroupEnum.answer6);
                }
                if (rowCode.equals("V.7")) {
                    qhts.setParameter("val7", HTSRiskGroupEnum.answer14);
                }
                if (rowCode.equals("V.8")) {
                    qhts.setParameter("val1", HTSRiskGroupEnum.answer1);
                    qhts.setParameter("val2", HTSRiskGroupEnum.answer2);
                    qhts.setParameter("val3", HTSRiskGroupEnum.answer3);
                    qhts.setParameter("val4", HTSRiskGroupEnum.answer4);
                    qhts.setParameter("val5", HTSRiskGroupEnum.answer5);
                    qhts.setParameter("val6", HTSRiskGroupEnum.answer6);
                    qhts.setParameter("val7", HTSRiskGroupEnum.answer14);
                }
            }
            if (col == 3) {
                qhts.setParameter("c5NoteAnswer1", HTSc5Note.answer2);
                if (rowCode.equals("V.1")) {
                    qhts.setParameter("val1", HTSRiskGroupEnum.answer1);
                }
                if (rowCode.equals("V.2")) {
                    qhts.setParameter("val2", HTSRiskGroupEnum.answer2);
                }
                if (rowCode.equals("V.3")) {
                    qhts.setParameter("val3", HTSRiskGroupEnum.answer3);
                }
                if (rowCode.equals("V.4")) {
                    qhts.setParameter("val4", HTSRiskGroupEnum.answer4);
                }
                if (rowCode.equals("V.5")) {
                    qhts.setParameter("val5", HTSRiskGroupEnum.answer5);
                }
                if (rowCode.equals("V.6")) {
                    qhts.setParameter("val6", HTSRiskGroupEnum.answer6);
                }
                if (rowCode.equals("V.7")) {
                    qhts.setParameter("val7", HTSRiskGroupEnum.answer14);
                }
                if (rowCode.equals("V.8")) {
                    qhts.setParameter("val1", HTSRiskGroupEnum.answer1);
                    qhts.setParameter("val2", HTSRiskGroupEnum.answer2);
                    qhts.setParameter("val3", HTSRiskGroupEnum.answer3);
                    qhts.setParameter("val4", HTSRiskGroupEnum.answer4);
                    qhts.setParameter("val5", HTSRiskGroupEnum.answer5);
                    qhts.setParameter("val6", HTSRiskGroupEnum.answer6);
                    qhts.setParameter("val7", HTSRiskGroupEnum.answer14);
                }
            }
            if (col == 4) {
                qhts.setParameter("c5NoteAnswer1", HTSc5Note.answer3);
                if (rowCode.equals("V.1")) {
                    qhts.setParameter("val1", HTSRiskGroupEnum.answer1);
                }
                if (rowCode.equals("V.2")) {
                    qhts.setParameter("val2", HTSRiskGroupEnum.answer2);
                }
                if (rowCode.equals("V.3")) {
                    qhts.setParameter("val3", HTSRiskGroupEnum.answer3);
                }
                if (rowCode.equals("V.4")) {
                    qhts.setParameter("val4", HTSRiskGroupEnum.answer4);
                }
                if (rowCode.equals("V.5")) {
                    qhts.setParameter("val5", HTSRiskGroupEnum.answer5);
                }
                if (rowCode.equals("V.6")) {
                    qhts.setParameter("val6", HTSRiskGroupEnum.answer6);
                }
                if (rowCode.equals("V.7")) {
                    qhts.setParameter("val7", HTSRiskGroupEnum.answer14);
                }
                if (rowCode.equals("V.8")) {
                    qhts.setParameter("val1", HTSRiskGroupEnum.answer1);
                    qhts.setParameter("val2", HTSRiskGroupEnum.answer2);
                    qhts.setParameter("val3", HTSRiskGroupEnum.answer3);
                    qhts.setParameter("val4", HTSRiskGroupEnum.answer4);
                    qhts.setParameter("val5", HTSRiskGroupEnum.answer5);
                    qhts.setParameter("val6", HTSRiskGroupEnum.answer6);
                    qhts.setParameter("val7", HTSRiskGroupEnum.answer14);
                }
            }
            if (col == 5) {
                qhts.setParameter("c5NoteAnswer1", HTSc5Note.answer4);
                if (rowCode.equals("V.1")) {
                    qhts.setParameter("val1", HTSRiskGroupEnum.answer1);
                }
                if (rowCode.equals("V.2")) {
                    qhts.setParameter("val2", HTSRiskGroupEnum.answer2);
                }
                if (rowCode.equals("V.3")) {
                    qhts.setParameter("val3", HTSRiskGroupEnum.answer3);
                }
                if (rowCode.equals("V.4")) {
                    qhts.setParameter("val4", HTSRiskGroupEnum.answer4);
                }
                if (rowCode.equals("V.5")) {
                    qhts.setParameter("val5", HTSRiskGroupEnum.answer5);
                }
                if (rowCode.equals("V.6")) {
                    qhts.setParameter("val6", HTSRiskGroupEnum.answer6);
                }
                if (rowCode.equals("V.7")) {
                    qhts.setParameter("val7", HTSRiskGroupEnum.answer14);
                }
                if (rowCode.equals("V.8")) {
                    qhts.setParameter("val1", HTSRiskGroupEnum.answer1);
                    qhts.setParameter("val2", HTSRiskGroupEnum.answer2);
                    qhts.setParameter("val3", HTSRiskGroupEnum.answer3);
                    qhts.setParameter("val4", HTSRiskGroupEnum.answer4);
                    qhts.setParameter("val5", HTSRiskGroupEnum.answer5);
                    qhts.setParameter("val6", HTSRiskGroupEnum.answer6);
                    qhts.setParameter("val7", HTSRiskGroupEnum.answer14);
                }
            }
            if (col == 6) {
                qhts.setParameter("c5NoteAnswer1", HTSc5Note.answer6);
                if (rowCode.equals("V.1")) {
                    qhts.setParameter("val1", HTSRiskGroupEnum.answer1);
                }
                if (rowCode.equals("V.2")) {
                    qhts.setParameter("val2", HTSRiskGroupEnum.answer2);
                }
                if (rowCode.equals("V.3")) {
                    qhts.setParameter("val3", HTSRiskGroupEnum.answer3);
                }
                if (rowCode.equals("V.4")) {
                    qhts.setParameter("val4", HTSRiskGroupEnum.answer4);
                }
                if (rowCode.equals("V.5")) {
                    qhts.setParameter("val5", HTSRiskGroupEnum.answer5);
                }
                if (rowCode.equals("V.6")) {
                    qhts.setParameter("val6", HTSRiskGroupEnum.answer6);
                }
                if (rowCode.equals("V.7")) {
                    qhts.setParameter("val7", HTSRiskGroupEnum.answer14);
                }
                if (rowCode.equals("V.8")) {
                    qhts.setParameter("val1", HTSRiskGroupEnum.answer1);
                    qhts.setParameter("val2", HTSRiskGroupEnum.answer2);
                    qhts.setParameter("val3", HTSRiskGroupEnum.answer3);
                    qhts.setParameter("val4", HTSRiskGroupEnum.answer4);
                    qhts.setParameter("val5", HTSRiskGroupEnum.answer5);
                    qhts.setParameter("val6", HTSRiskGroupEnum.answer6);
                    qhts.setParameter("val7", HTSRiskGroupEnum.answer14);
                }
            }
            if (col == 7) {
                qhts.setParameter("c5NoteAnswer1", HTSc5Note.answer6);
                if (rowCode.equals("V.1")) {
                    qhts.setParameter("val1", HTSRiskGroupEnum.answer1);
                }
                if (rowCode.equals("V.2")) {
                    qhts.setParameter("val2", HTSRiskGroupEnum.answer2);
                }
                if (rowCode.equals("V.3")) {
                    qhts.setParameter("val3", HTSRiskGroupEnum.answer3);
                }
                if (rowCode.equals("V.4")) {
                    qhts.setParameter("val4", HTSRiskGroupEnum.answer4);
                }
                if (rowCode.equals("V.5")) {
                    qhts.setParameter("val5", HTSRiskGroupEnum.answer5);
                }
                if (rowCode.equals("V.6")) {
                    qhts.setParameter("val6", HTSRiskGroupEnum.answer6);
                }
                if (rowCode.equals("V.7")) {
                    qhts.setParameter("val7", HTSRiskGroupEnum.answer14);
                }
                if (rowCode.equals("V.8")) {
                    qhts.setParameter("val1", HTSRiskGroupEnum.answer1);
                    qhts.setParameter("val2", HTSRiskGroupEnum.answer2);
                    qhts.setParameter("val3", HTSRiskGroupEnum.answer3);
                    qhts.setParameter("val4", HTSRiskGroupEnum.answer4);
                    qhts.setParameter("val5", HTSRiskGroupEnum.answer5);
                    qhts.setParameter("val6", HTSRiskGroupEnum.answer6);
                    qhts.setParameter("val7", HTSRiskGroupEnum.answer14);
                }
            }
            if (col == 8) {
                qhts.setParameter("c5NoteAnswer1", HTSc5.answer1);
                if (rowCode.equals("V.1")) {
                    qhts.setParameter("val1", HTSRiskGroupEnum.answer1);
                }
                if (rowCode.equals("V.2")) {
                    qhts.setParameter("val2", HTSRiskGroupEnum.answer2);
                }
                if (rowCode.equals("V.3")) {
                    qhts.setParameter("val3", HTSRiskGroupEnum.answer3);
                }
                if (rowCode.equals("V.4")) {
                    qhts.setParameter("val4", HTSRiskGroupEnum.answer4);
                }
                if (rowCode.equals("V.5")) {
                    qhts.setParameter("val5", HTSRiskGroupEnum.answer5);
                }
                if (rowCode.equals("V.6")) {
                    qhts.setParameter("val6", HTSRiskGroupEnum.answer6);
                }
                if (rowCode.equals("V.7")) {
                    qhts.setParameter("val7", HTSRiskGroupEnum.answer14);
                }
                if (rowCode.equals("V.8")) {
                    qhts.setParameter("val1", HTSRiskGroupEnum.answer1);
                    qhts.setParameter("val2", HTSRiskGroupEnum.answer2);
                    qhts.setParameter("val3", HTSRiskGroupEnum.answer3);
                    qhts.setParameter("val4", HTSRiskGroupEnum.answer4);
                    qhts.setParameter("val5", HTSRiskGroupEnum.answer5);
                    qhts.setParameter("val6", HTSRiskGroupEnum.answer6);
                    qhts.setParameter("val7", HTSRiskGroupEnum.answer14);
                }
            }
            if (col == 9) {
                qhts.setParameter("c5NoteAnswer3", HTSc5.answer3);
                if (rowCode.equals("V.1")) {
                    qhts.setParameter("val1", HTSRiskGroupEnum.answer1);
                }
                if (rowCode.equals("V.2")) {
                    qhts.setParameter("val2", HTSRiskGroupEnum.answer2);
                }
                if (rowCode.equals("V.3")) {
                    qhts.setParameter("val3", HTSRiskGroupEnum.answer3);
                }
                if (rowCode.equals("V.4")) {
                    qhts.setParameter("val4", HTSRiskGroupEnum.answer4);
                }
                if (rowCode.equals("V.5")) {
                    qhts.setParameter("val5", HTSRiskGroupEnum.answer5);
                }
                if (rowCode.equals("V.6")) {
                    qhts.setParameter("val6", HTSRiskGroupEnum.answer6);
                }
                if (rowCode.equals("V.7")) {
                    qhts.setParameter("val7", HTSRiskGroupEnum.answer14);
                }
                if (rowCode.equals("V.8")) {
                    qhts.setParameter("val1", HTSRiskGroupEnum.answer1);
                    qhts.setParameter("val2", HTSRiskGroupEnum.answer2);
                    qhts.setParameter("val3", HTSRiskGroupEnum.answer3);
                    qhts.setParameter("val4", HTSRiskGroupEnum.answer4);
                    qhts.setParameter("val5", HTSRiskGroupEnum.answer5);
                    qhts.setParameter("val6", HTSRiskGroupEnum.answer6);
                    qhts.setParameter("val7", HTSRiskGroupEnum.answer14);
                }
            }
            Long ret = (Long) qhts.getSingleResult();
            if (ret != null) {
                return ret.intValue();
            }
            return 0;

        }
    }

    @Override
    public Workbook importFromExcelNew(InputStream is) throws IOException {
//		Workbook wb = new XSSFWorkbook(is);
        XSSFWorkbook workbook = (new XSSFWorkbook(is));
        Sheet datatypeSheet = workbook.getSheetAt(0);
        SimpleDateFormat spf = new SimpleDateFormat("dd/MM/yyyy");
        int rowIndex = 6;
        int num = datatypeSheet.getLastRowNum();
        List<HTSImportDto> listDtoImport = new ArrayList<HTSImportDto>();
        HTSImportDto dto = null;
        while (rowIndex <= num) {
            Row row = datatypeSheet.getRow(rowIndex);
            Cell cell = null;
            if (row != null) {
                dto = new HTSImportDto();
                cell = row.getCell(3);// ma so co so tu van xet nghiem
                try {
                    if (cell.getCellType().equals(CellType.STRING)) {
                        dto.setCodeOrg(cell.getStringCellValue());
                    } else if (cell.getCellType().equals(CellType.NUMERIC)) {
                        dto.setCodeOrg(String.valueOf(cell.getNumericCellValue()));
                    } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        dto.setCodeOrg(spf.format(cell.getDateCellValue()));
                    } else {
                        dto.setCodeOrg(null);
                    }

                } catch (Exception e) {
                    dto.setCodeOrg(null);
                }

                cell = row.getCell(4);// ngayf lamf xet nghiem hiv
                try {
                    if (cell.getCellType().equals(CellType.STRING)) {
                        dto.setC4(spf.parse(cell.getStringCellValue()));
                    } else if (cell.getCellType().equals(CellType.NUMERIC)) {
                        if (HSSFDateUtil.isCellDateFormatted(cell)) {
                            dto.setC4(cell.getDateCellValue());
                        }
                    } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        dto.setC4(cell.getDateCellValue());
                    } else {
                        dto.setC4(null);
                    }

                } catch (Exception e) {
                    dto.setC4(null);
                }

                cell = row.getCell(5);// ma so khach hangf
                try {
                    if (cell.getCellType().equals(CellType.STRING)) {
                        dto.setC6(cell.getStringCellValue());
                    } else if (cell.getCellType().equals(CellType.NUMERIC)) {
                        dto.setC6(String.valueOf(cell.getNumericCellValue()));
                    } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        dto.setC6(spf.format(cell.getDateCellValue()));
                    } else {
                        dto.setC6(null);
                    }

                } catch (Exception e) {
                    dto.setC6(null);
                }

                cell = row.getCell(6);// Hoj ten
                try {
                    if (cell.getCellType().equals(CellType.STRING)) {
                        dto.setC23FullName(cell.getStringCellValue());
                    } else if (cell.getCellType().equals(CellType.NUMERIC)) {
                        dto.setC23FullName(String.valueOf(cell.getNumericCellValue()));
                    } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        dto.setC23FullName(spf.format(cell.getDateCellValue()));
                    } else {
                        dto.setC23FullName(null);
                    }

                } catch (Exception e) {
                    dto.setC23FullName(null);
                }

                cell = row.getCell(7);// gioi tinh
                try {
                    if (cell.getCellType().equals(CellType.STRING)) {
                        dto.setGender(cell.getStringCellValue());
                    } else if (cell.getCellType().equals(CellType.NUMERIC)) {
                        dto.setGender(String.valueOf(cell.getNumericCellValue()));
                    } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        dto.setGender(spf.format(cell.getDateCellValue()));
                    } else {
                        dto.setGender(null);
                    }
                } catch (Exception e) {
                    dto.setGender(null);
                }

                cell = row.getCell(8);// nam sinh
                try {
                    if (cell.getCellType().equals(CellType.STRING)) {
                        dto.setC8(Integer.parseInt(cell.getStringCellValue()));
                    } else if (cell.getCellType().equals(CellType.NUMERIC)) {
                        dto.setC8((int) cell.getNumericCellValue());
                    } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        dto.setC8(Integer.valueOf(spf.format(cell.getDateCellValue())));
                    } else {
                        dto.setC8(null);
                    }
                } catch (Exception e) {
                    dto.setC8(null);
                }

                cell = row.getCell(9);// cccd,cmnd,bhyt
                try {
                    String value = "";
                    if (cell.getCellType().equals(CellType.STRING)) {
                        value = cell.getStringCellValue();
                    } else if (cell.getCellType().equals(CellType.NUMERIC)) {
                        value = String.valueOf(cell.getNumericCellValue());
                    } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        value = spf.format(cell.getDateCellValue());
                    }
                    if (!StringUtils.isEmpty(value)) {
                        dto.setC23IdOrHealth(value);
                        List<String> listValue = Arrays.asList(value.split("-", 2));
                        if (!CollectionUtils.isEmpty(listValue) && listValue.size() == 2) {
                            if (listValue.get(0).equals("cmnd") || listValue.get(0).equals("cccd")) {
                                dto.setC23IdNumber(listValue.get(1));
                            }
                            if (listValue.get(0).equals("bhyt")) {
                                dto.setC23HealthNumber(listValue.get(1));
                            }
                        }
                    }
                } catch (Exception e) {
                }

                cell = row.getCell(10);// tinh thanh pho
                try {
                    if (cell.getCellType().equals(CellType.STRING)) {
                        dto.setC23CurrentAddressProvince(cell.getStringCellValue());
                    } else if (cell.getCellType().equals(CellType.NUMERIC)) {
                        dto.setC23CurrentAddressProvince(String.valueOf(cell.getNumericCellValue()));
                    } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        dto.setC23CurrentAddressProvince(spf.format(cell.getDateCellValue()));
                    } else {
                        dto.setC23CurrentAddressProvince(null);
                    }
                } catch (Exception e) {
                    dto.setC23CurrentAddressProvince(null);
                }

                cell = row.getCell(11);// quan huyen
                try {
                    if (cell.getCellType().equals(CellType.STRING)) {
                        dto.setC23CurrentAddressDistrict(cell.getStringCellValue());
                    } else if (cell.getCellType().equals(CellType.NUMERIC)) {
                        dto.setC23CurrentAddressDistrict(String.valueOf(cell.getNumericCellValue()));
                    } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        dto.setC23CurrentAddressDistrict(spf.format(cell.getDateCellValue()));
                    } else {
                        dto.setC23CurrentAddressDistrict(null);
                    }
                } catch (Exception e) {
                    dto.setC23CurrentAddressDistrict(null);
                }


                cell = row.getCell(12);// phuong xa
                try {
                    if (cell.getCellType().equals(CellType.STRING)) {
                        dto.setC23CurrentAddressCommune(cell.getStringCellValue());
                    } else if (cell.getCellType().equals(CellType.NUMERIC)) {
                        dto.setC23CurrentAddressCommune(String.valueOf(cell.getNumericCellValue()));
                    } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        dto.setC23CurrentAddressCommune(spf.format(cell.getDateCellValue()));
                    } else {
                        dto.setC23CurrentAddressCommune(null);
                    }
                } catch (Exception e) {
                    dto.setC23CurrentAddressCommune(null);
                }

                cell = row.getCell(13);// chi tiet
                try {
                    if (cell.getCellType().equals(CellType.STRING)) {
                        dto.setC23CurrentAddressDetail(cell.getStringCellValue());
                    } else if (cell.getCellType().equals(CellType.NUMERIC)) {
                        dto.setC23CurrentAddressDetail(String.valueOf(cell.getNumericCellValue()));
                    } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        dto.setC23CurrentAddressDetail(spf.format(cell.getDateCellValue()));
                    } else {
                        dto.setC23CurrentAddressDetail(null);
                    }
                } catch (Exception e) {
                    dto.setC23CurrentAddressDetail(null);
                }

                cell = row.getCell(14);// risk1
                try {
                    if (cell.getCellType().equals(CellType.STRING)) {
                        dto.setRisk1(cell.getStringCellValue());
                    } else if (cell.getCellType().equals(CellType.NUMERIC)) {
                        dto.setRisk1(String.valueOf(cell.getNumericCellValue()));
                    } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        dto.setRisk1(spf.format(cell.getDateCellValue()));
                    } else {
                        dto.setRisk1(null);
                    }
                } catch (Exception e) {
                    dto.setRisk1(null);
                }

                cell = row.getCell(15);// risk2
                try {
                    if (cell.getCellType().equals(CellType.STRING)) {
                        dto.setRisk2(cell.getStringCellValue());
                    } else if (cell.getCellType().equals(CellType.NUMERIC)) {
                        dto.setRisk2(String.valueOf(cell.getNumericCellValue()));
                    } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        dto.setRisk2(spf.format(cell.getDateCellValue()));
                    } else {
                        dto.setRisk2(null);
                    }
                } catch (Exception e) {
                    dto.setRisk2(null);
                }

                cell = row.getCell(16);// risk3
                try {
                    if (cell.getCellType().equals(CellType.STRING)) {
                        dto.setRisk3(cell.getStringCellValue());
                    } else if (cell.getCellType().equals(CellType.NUMERIC)) {
                        dto.setRisk3(String.valueOf(cell.getNumericCellValue()));
                    } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        dto.setRisk3(spf.format(cell.getDateCellValue()));
                    } else {
                        dto.setRisk3(null);
                    }
                } catch (Exception e) {
                    dto.setRisk3(null);
                }

                cell = row.getCell(17);// risk4
                try {
                    if (cell.getCellType().equals(CellType.STRING)) {
                        dto.setRisk4(cell.getStringCellValue());
                    } else if (cell.getCellType().equals(CellType.NUMERIC)) {
                        dto.setRisk4(String.valueOf(cell.getNumericCellValue()));
                    } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        dto.setRisk4(spf.format(cell.getDateCellValue()));
                    } else {
                        dto.setRisk4(null);
                    }
                } catch (Exception e) {
                    dto.setRisk4(null);
                }

                cell = row.getCell(18);// risk5
                try {
                    if (cell.getCellType().equals(CellType.STRING)) {
                        dto.setRisk5(cell.getStringCellValue());
                    } else if (cell.getCellType().equals(CellType.NUMERIC)) {
                        dto.setRisk5(String.valueOf(cell.getNumericCellValue()));
                    } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        dto.setRisk5(spf.format(cell.getDateCellValue()));
                    } else {
                        dto.setRisk5(null);
                    }
                } catch (Exception e) {
                    dto.setRisk5(null);
                }

                cell = row.getCell(19);// risk6
                try {
                    if (cell.getCellType().equals(CellType.STRING)) {
                        dto.setRisk6(cell.getStringCellValue());
                    } else if (cell.getCellType().equals(CellType.NUMERIC)) {
                        dto.setRisk6(String.valueOf(cell.getNumericCellValue()));
                    } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        dto.setRisk6(spf.format(cell.getDateCellValue()));
                    } else {
                        dto.setRisk6(null);
                    }
                } catch (Exception e) {
                    dto.setRisk6(null);
                }

                cell = row.getCell(20);// risk7
                try {
                    if (cell.getCellType().equals(CellType.STRING)) {
                        dto.setRisk7(cell.getStringCellValue());
                    } else if (cell.getCellType().equals(CellType.NUMERIC)) {
                        dto.setRisk7(String.valueOf(cell.getNumericCellValue()));
                    } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        dto.setRisk7(spf.format(cell.getDateCellValue()));
                    } else {
                        dto.setRisk7(null);
                    }
                } catch (Exception e) {
                    dto.setRisk7(null);
                }

                cell = row.getCell(21);// risk8
                try {
                    if (cell.getCellType().equals(CellType.STRING)) {
                        dto.setRisk8(cell.getStringCellValue());
                    } else if (cell.getCellType().equals(CellType.NUMERIC)) {
                        dto.setRisk8(String.valueOf(cell.getNumericCellValue()));
                    } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        dto.setRisk8(spf.format(cell.getDateCellValue()));
                    } else {
                        dto.setRisk8(null);
                    }
                } catch (Exception e) {
                    dto.setRisk8(null);
                }

                cell = row.getCell(22);// risk9
                try {
                    if (cell.getCellType().equals(CellType.STRING)) {
                        dto.setRisk9(cell.getStringCellValue());
                    } else if (cell.getCellType().equals(CellType.NUMERIC)) {
                        dto.setRisk9(String.valueOf(cell.getNumericCellValue()));
                    } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        dto.setRisk9(spf.format(cell.getDateCellValue()));
                    } else {
                        dto.setRisk9(null);
                    }
                } catch (Exception e) {
                    dto.setRisk9(null);
                }

                cell = row.getCell(23);// risk10
                try {
                    if (cell.getCellType().equals(CellType.STRING)) {
                        dto.setRisk10(cell.getStringCellValue());
                    } else if (cell.getCellType().equals(CellType.NUMERIC)) {
                        dto.setRisk10(String.valueOf(cell.getNumericCellValue()));
                    } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        dto.setRisk10(spf.format(cell.getDateCellValue()));
                    } else {
                        dto.setRisk10(null);
                    }
                } catch (Exception e) {
                    dto.setRisk10(null);
                }

                cell = row.getCell(24);// risk11
                try {
                    if (cell.getCellType().equals(CellType.STRING)) {
                        dto.setRisk11(cell.getStringCellValue());
                    } else if (cell.getCellType().equals(CellType.NUMERIC)) {
                        dto.setRisk11(String.valueOf(cell.getNumericCellValue()));
                    } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        dto.setRisk11(spf.format(cell.getDateCellValue()));
                    } else {
                        dto.setRisk11(null);
                    }
                } catch (Exception e) {
                    dto.setRisk11(null);
                }

                cell = row.getCell(25);// risk12
                try {
                    if (cell.getCellType().equals(CellType.STRING)) {
                        dto.setRisk12(cell.getStringCellValue());
                    } else if (cell.getCellType().equals(CellType.NUMERIC)) {
                        dto.setRisk12(String.valueOf(cell.getNumericCellValue()));
                    } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        dto.setRisk12(spf.format(cell.getDateCellValue()));
                    } else {
                        dto.setRisk12(null);
                    }
                } catch (Exception e) {
                    dto.setRisk12(null);
                }

                cell = row.getCell(26);// risk13
                try {
                    if (cell.getCellType().equals(CellType.STRING)) {
                        dto.setRisk13(cell.getStringCellValue());
                    } else if (cell.getCellType().equals(CellType.NUMERIC)) {
                        dto.setRisk13(String.valueOf(cell.getNumericCellValue()));
                    } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        dto.setRisk13(spf.format(cell.getDateCellValue()));
                    } else {
                        dto.setRisk13(null);
                    }
                } catch (Exception e) {
                    dto.setRisk13(null);
                }

                cell = row.getCell(27);// sang loc hiv
                try {
                    if (cell.getCellType().equals(CellType.STRING)) {
                        dto.setHIVResult1(cell.getStringCellValue());
                    } else if (cell.getCellType().equals(CellType.NUMERIC)) {
                        dto.setHIVResult1(String.valueOf(cell.getNumericCellValue()));
                    } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        dto.setHIVResult1(spf.format(cell.getDateCellValue()));
                    } else {
                        dto.setHIVResult1(null);
                    }
//					if (cell.getCellType().equals(CellType.STRING)) {
//						if(cell.getStringCellValue().equals("DT")) {
//							dto.setc1(cell.getStringCellValue());
//						} else if(cell.getStringCellValue().equals("AT")) {
//
//						} else if(cell.getStringCellValue().equals("KXD")) {
//
//						}
//					} else if (cell.getCellType().equals(CellType.NUMERIC)) {
//						dto.setHIVResult1(String.valueOf(cell.getNumericCellValue()));
//					} else if (HSSFDateUtil.isCellDateFormatted(cell)) {
//						dto.setHIVResult1(spf.format(cell.getDateCellValue()));
//					} else {
//						dto.setHIVResult1(null);
//					}
                } catch (Exception e) {
                    dto.setHIVResult1(null);
                }

                cell = row.getCell(28);// khang dinh hiv
                try {
                    if (cell.getCellType().equals(CellType.STRING)) {
                        dto.setC14(cell.getStringCellValue());
                    } else if (cell.getCellType().equals(CellType.NUMERIC)) {
                        dto.setC14(String.valueOf(cell.getNumericCellValue()));
                    } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        dto.setC14(spf.format(cell.getDateCellValue()));
                    } else {
                        dto.setC14(null);
                    }
                } catch (Exception e) {
                    dto.setC14(null);
                }

                cell = row.getCell(29);// moi nhiem hiv
                try {
                    if (cell.getCellType().equals(CellType.STRING)) {
                        dto.setC17(cell.getStringCellValue());
                    } else if (cell.getCellType().equals(CellType.NUMERIC)) {
                        dto.setC17(String.valueOf(cell.getNumericCellValue()));
                    } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        dto.setC17(spf.format(cell.getDateCellValue()));
                    } else {
                        dto.setC17(null);
                    }
                } catch (Exception e) {
                    dto.setC17(null);
                }

                cell = row.getCell(30);// tai luong virut
                try {
                    if (cell.getCellType().equals(CellType.STRING)) {
                        dto.setC18(cell.getStringCellValue());
                    } else if (cell.getCellType().equals(CellType.NUMERIC)) {
                        dto.setC18(String.valueOf(cell.getNumericCellValue()));
                    } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        dto.setC18(spf.format(cell.getDateCellValue()));
                    } else {
                        dto.setC18(null);
                    }
                } catch (Exception e) {
                    dto.setC18(null);
                }

                cell = row.getCell(31);// ngayf quay lai nhan kq
                try {
                    if (cell.getCellType().equals(CellType.STRING)) {
                        dto.setC15Date(spf.parse(cell.getStringCellValue()));
                    } else if (cell.getCellType().equals(CellType.NUMERIC)) {
                        if (HSSFDateUtil.isCellDateFormatted(cell)) {
                            dto.setC15Date(cell.getDateCellValue());
                        }
                    } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        dto.setC15Date(cell.getDateCellValue());
                    } else {
                        dto.setC15Date(null);
                    }

                } catch (Exception e) {
                    dto.setC15Date(null);
                }

                cell = row.getCell(32);// ngayf dieu tri arv
                try {
                    if (cell.getCellType().equals(CellType.STRING)) {
                        dto.setARV(spf.parse(cell.getStringCellValue()));
                    } else if (cell.getCellType().equals(CellType.NUMERIC)) {
                        if (HSSFDateUtil.isCellDateFormatted(cell)) {
                            dto.setARV(cell.getDateCellValue());
                        }
                    } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        dto.setARV(cell.getDateCellValue());
                    } else {
                        dto.setARV(null);
                    }

                } catch (Exception e) {
                    dto.setARV(null);
                }

                cell = row.getCell(33);// noi dieu tri
                try {
                    if (cell.getCellType().equals(CellType.STRING)) {
                        dto.setPlaceARV(cell.getStringCellValue());
                    } else if (cell.getCellType().equals(CellType.NUMERIC)) {
                        dto.setPlaceARV(String.valueOf(cell.getNumericCellValue()));
                    } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        dto.setPlaceARV(spf.format(cell.getDateCellValue()));
                    } else {
                        dto.setPlaceARV(null);
                    }
                } catch (Exception e) {
                    dto.setPlaceARV(null);
                }

                cell = row.getCell(34);// được tư vấn pns
                try {
                    if (cell.getCellType().equals(CellType.STRING)) {
                        dto.setIsPNS(cell.getStringCellValue());
                    } else if (cell.getCellType().equals(CellType.NUMERIC)) {
                        dto.setIsPNS(String.valueOf(cell.getNumericCellValue()));
                    } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        dto.setIsPNS(spf.format(cell.getDateCellValue()));
                    } else {
                        dto.setIsPNS(null);
                    }
                } catch (Exception e) {
                    dto.setIsPNS(null);
                }

                cell = row.getCell(35);// số bạn tình bạn chích pns
                try {
                    if (cell.getCellType().equals(CellType.STRING)) {
                        dto.setContactPNS(Integer.parseInt(cell.getStringCellValue()));
                    } else if (cell.getCellType().equals(CellType.NUMERIC)) {
                        dto.setContactPNS((int) cell.getNumericCellValue());
                    } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        dto.setContactPNS(Integer.valueOf(spf.format(cell.getDateCellValue())));
                    } else {
                        dto.setContactPNS(null);
                    }
                } catch (Exception e) {
                    dto.setContactPNS(null);
                }

                cell = row.getCell(36);// số bạn tình bạn chích pns hiv
                try {
                    if (cell.getCellType().equals(CellType.STRING)) {
                        dto.setContactPNSHIV(Integer.parseInt(cell.getStringCellValue()));
                    } else if (cell.getCellType().equals(CellType.NUMERIC)) {
                        dto.setContactPNSHIV((int) cell.getNumericCellValue());
                    } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        dto.setContactPNSHIV(Integer.valueOf(spf.format(cell.getDateCellValue())));
                    } else {
                        dto.setContactPNSHIV(null);
                    }
                } catch (Exception e) {
                    dto.setContactPNSHIV(null);
                }

                cell = row.getCell(37);// ghi chú
                try {
                    if (cell.getCellType().equals(CellType.STRING)) {
                        dto.setNote(cell.getStringCellValue());
                    } else if (cell.getCellType().equals(CellType.NUMERIC)) {
                        dto.setNote(String.valueOf(cell.getNumericCellValue()));
                    } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        dto.setNote(spf.format(cell.getDateCellValue()));
                    } else {
                        dto.setNote(null);
                    }
                } catch (Exception e) {
                    dto.setNote(null);
                }

                listDtoImport.add(dto);
            }
            rowIndex++;
        }
        ImportResultDto<HTSImportDto> ret = saveHTSFromExcel(listDtoImport);
        return exportErrorHTSCase(ret);
    }

    @Override
    public Workbook exportHTSTST(PreventionFilterDto filter, Workbook workbook) {
        List<HTSTSTDto> result = this.getDataHTSTST(filter);
        if (result == null) {
            return workbook;
        }
        XSSFWorkbook wbook = (XSSFWorkbook) workbook;
        int rowIndex = 5;
        int colIndex = 0;

        Row row = null;
        Cell cell = null;
        Sheet sheet = wbook.getSheet("HTS_TST");
        CellStyle cellStyle = wbook.createCellStyle();
        ExcelUtils.setBorders4Style(cellStyle);
        cellStyle.setWrapText(true);
        cellStyle.setAlignment(HorizontalAlignment.RIGHT);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        for (HTSTSTDto dto : result) {
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
                    cell.setCellValue(dto.getModality());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getpPWID().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getnPWID().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getpMSM().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getnMSM().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getpTG().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getnTG().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getpFSW().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getnFSW().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getpOther().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getnOther().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getpFU().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getpF0().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getpF1().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getpF5().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getpF10().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getpF15().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getpF20().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getpF25().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getpF30().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getpF35().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getpF40().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getpF45().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getpF50().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getpMU().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getpM0().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getpM1().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getpM5().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getpM10().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getpM15().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getpM20().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getpM25().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getpM30().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getpM35().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getpM40().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getpM45().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getpM50().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getnFU().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getnF0().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getnF1().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getnF5().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getnF10().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getnF15().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getnF20().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getnF25().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getnF30().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getnF35().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getnF40().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getnF45().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getnF50().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getnMU().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getnM0().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getnM1().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getnM5().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getnM10().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getnM15().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getnM20().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getnM25().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getnM30().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getnM35().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getnM40().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getnM45().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getnM50().intValue());
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

    @Override
    public Workbook exportHTSIndex(PreventionFilterDto filter, Workbook workbook) {
        List<HTSIndexDto> result = this.getDataHTSIndex(filter);
        if (result == null) {
            return workbook;
        }
        XSSFWorkbook wbook = (XSSFWorkbook) workbook;
        int rowIndex = 5;
        int colIndex = 0;

        Row row = null;
        Cell cell = null;
        Sheet sheet = wbook.getSheet("HTS_INDEX");
        CellStyle cellStyle = wbook.createCellStyle();
        ExcelUtils.setBorders4Style(cellStyle);
        cellStyle.setWrapText(true);
        cellStyle.setAlignment(HorizontalAlignment.RIGHT);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        for (HTSIndexDto dto : result) {
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
                    cell.setCellValue(dto.getModality());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getFuOffered().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF0Offered().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF1Offered().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF5Offered().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF10Offered().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF15Offered().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF20Offered().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF25Offered().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF30Offered().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF35Offered().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF40Offered().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF45Offered().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF50Offered().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getMuOffered().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM0Offered().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM1Offered().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM5Offered().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM10Offered().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM15Offered().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM20Offered().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM25Offered().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM30Offered().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM35Offered().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM40Offered().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM45Offered().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM50Offered().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getTotalOffered());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getFuAccepted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF0Accepted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF1Accepted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF5Accepted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF10Accepted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF15Accepted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF20Accepted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF25Accepted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF30Accepted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF35Accepted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF40Accepted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF45Accepted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF50Accepted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getMuAccepted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM0Accepted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM1Accepted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM5Accepted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM10Accepted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM15Accepted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM20Accepted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM25Accepted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM30Accepted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM35Accepted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM40Accepted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM45Accepted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM50Accepted().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getTotalAccepted());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getFuContactsElicited().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF0ContactsElicited().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF15ContactsElicited().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getMuContactsElicited().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM0ContactsElicited().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM15ContactsElicited().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getTotalContactsElicited().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getFuKnownPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF0KnownPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF1KnownPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF5KnownPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF10KnownPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF15KnownPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF20KnownPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF25KnownPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF30KnownPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF35KnownPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF40KnownPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF45KnownPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF50KnownPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getMuKnownPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM0KnownPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM1KnownPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM5KnownPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM10KnownPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM15KnownPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM20KnownPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM25KnownPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM30KnownPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM35KnownPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM40KnownPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM45KnownPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM50KnownPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getTotalKnownPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                //document
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF1DocumentedNegatives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF5DocumentedNegatives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF10DocumentedNegatives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM1DocumentedNegatives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM5DocumentedNegatives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM10DocumentedNegatives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getTotalDocumentedNegatives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                //new
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getFuNewPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF0NewPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF1NewPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF5NewPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF10NewPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF15NewPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF20NewPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF25NewPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF30NewPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF35NewPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF40NewPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF45NewPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF50NewPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getMuNewPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM0NewPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM1NewPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM5NewPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM10NewPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM15NewPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM20NewPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM25NewPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM30NewPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM35NewPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM40NewPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM45NewPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM50NewPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getTotalNewPositives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                //new
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getFuNewNegatives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF0NewNegatives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF1NewNegatives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF5NewNegatives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF10NewNegatives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF15NewNegatives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF20NewNegatives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF25NewNegatives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF30NewNegatives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF35NewNegatives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF40NewNegatives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF45NewNegatives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF50NewNegatives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getMuNewNegatives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM0NewNegatives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM1NewNegatives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM5NewNegatives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM10NewNegatives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM15NewNegatives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM20NewNegatives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM25NewNegatives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM30NewNegatives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM35NewNegatives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM40NewNegatives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM45NewNegatives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM50NewNegatives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getTotalNewNegatives().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getTotalContactsTested().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

            }
        }

        return wbook;
    }

    private List<HTSIndexDto> getDataHTSIndex(PreventionFilterDto filter) {
        Hashtable<String,HTSIndexDto> hashtable = new Hashtable<>();
        List<HTSIndexDto> communityOffered = reportMerService.getDataHTSIndexModalityCommunityIndexTestingOffered(filter);
        List<HTSIndexDto> communityAccepted = reportMerService.getDataHTSIndexModalityCommunityIndexTestingAccepted(filter);
        List<HTSIndexDto> communityIndexTestingContactsElicited = reportMerService.getDataHTSIndexModalityCommunityIndexTestingContactsElicited(filter);
        List<HTSIndexDto> communityIndexTestingKnownPositives = reportMerService.getDataHTSIndexModalityCommunityIndexTestingKnownPositives(filter);
        List<HTSIndexDto> communityIndexTestingNewPositives = reportMerService.getDataHTSIndexModalityCommunityIndexTestingNewPositives(filter);
        List<HTSIndexDto> communityIndexTestingNewNegatives = reportMerService.getDataHTSIndexModalityCommunityIndexTestingNewNegatives(filter);

        List<HTSIndexDto> facilityIndexTestingOffered = reportMerService.getDataHTSIndexModalityFacilityIndexTestingOffered(filter);
        List<HTSIndexDto> facilityIndexTestingAccepted = reportMerService.getDataHTSIndexModalityFacilityIndexTestingAccepted(filter);
        List<HTSIndexDto> facilityIndexTestingContactsElicited = reportMerService.getDataHTSIndexModalityFacilityIndexTestingContactsElicited(filter);
        List<HTSIndexDto> facilityIndexTestingKnownPositives = reportMerService.getDataHTSIndexModalityFacilityIndexTestingKnownPositives(filter);
        List<HTSIndexDto> facilityIndexTestingDocumentedNegatives = reportMerService.getDataHTSIndexModalityFacilityIndexTestingDocumentedNegatives(filter);
        List<HTSIndexDto> facilityIndexTestingNewPositives = reportMerService.getDataHTSIndexModalityFacilityIndexTestingNewPositives(filter);
        List<HTSIndexDto> facilityIndexTestingNewNegatives = reportMerService.getDataHTSIndexModalityFacilityIndexTestingNewNegatives(filter);

        HTSIndexDto htsIndexDto = null;

        for (HTSIndexDto dto : communityOffered){
            if(hashtable.containsKey(dto.getOrgId()+"-"+dto.getModality())){
                htsIndexDto = hashtable.get(dto.getOrgId()+"-"+dto.getModality());

                htsIndexDto.setFuOffered(htsIndexDto.getFuOffered().add(dto.getFuOffered()));
                htsIndexDto.setF0Offered(htsIndexDto.getF0Offered().add(dto.getF0Offered()));
                htsIndexDto.setF1Offered(htsIndexDto.getF1Offered().add(dto.getF1Offered()));
                htsIndexDto.setF5Offered(htsIndexDto.getF5Offered().add(dto.getF5Offered()));
                htsIndexDto.setF10Offered(htsIndexDto.getF10Offered().add(dto.getF10Offered()));
                htsIndexDto.setF15Offered(htsIndexDto.getF15Offered().add(dto.getF15Offered()));
                htsIndexDto.setF20Offered(htsIndexDto.getF20Offered().add(dto.getF20Offered()));
                htsIndexDto.setF25Offered(htsIndexDto.getF25Offered().add(dto.getF25Offered()));
                htsIndexDto.setF30Offered(htsIndexDto.getF30Offered().add(dto.getF30Offered()));
                htsIndexDto.setF35Offered(htsIndexDto.getF35Offered().add(dto.getF35Offered()));
                htsIndexDto.setF40Offered(htsIndexDto.getF40Offered().add(dto.getF40Offered()));
                htsIndexDto.setF45Offered(htsIndexDto.getF45Offered().add(dto.getF45Offered()));
                htsIndexDto.setF50Offered(htsIndexDto.getF50Offered().add(dto.getF50Offered()));

                htsIndexDto.setMuOffered(htsIndexDto.getMuOffered().add(dto.getMuOffered()));
                htsIndexDto.setM0Offered(htsIndexDto.getM0Offered().add(dto.getM0Offered()));
                htsIndexDto.setM1Offered(htsIndexDto.getM1Offered().add(dto.getM1Offered()));
                htsIndexDto.setM5Offered(htsIndexDto.getM5Offered().add(dto.getM5Offered()));
                htsIndexDto.setM10Offered(htsIndexDto.getM10Offered().add(dto.getM10Offered()));
                htsIndexDto.setM15Offered(htsIndexDto.getM15Offered().add(dto.getM15Offered()));
                htsIndexDto.setM20Offered(htsIndexDto.getM20Offered().add(dto.getM20Offered()));
                htsIndexDto.setM25Offered(htsIndexDto.getM25Offered().add(dto.getM25Offered()));
                htsIndexDto.setM30Offered(htsIndexDto.getM30Offered().add(dto.getM30Offered()));
                htsIndexDto.setM35Offered(htsIndexDto.getM35Offered().add(dto.getM35Offered()));
                htsIndexDto.setM40Offered(htsIndexDto.getM40Offered().add(dto.getM40Offered()));
                htsIndexDto.setM45Offered(htsIndexDto.getM45Offered().add(dto.getM45Offered()));
                htsIndexDto.setM50Offered(htsIndexDto.getM50Offered().add(dto.getM50Offered()));



                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),htsIndexDto);
            }else{
                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),dto);
            }
        }

        for (HTSIndexDto dto : communityAccepted){
            if(hashtable.containsKey(dto.getOrgId()+"-"+dto.getModality())){
                htsIndexDto = hashtable.get(dto.getOrgId()+"-"+dto.getModality());

                htsIndexDto.setFuAccepted(htsIndexDto.getFuAccepted().add(dto.getFuAccepted()));
                htsIndexDto.setF0Accepted(htsIndexDto.getF0Accepted().add(dto.getF0Accepted()));
                htsIndexDto.setF1Accepted(htsIndexDto.getF1Accepted().add(dto.getF1Accepted()));
                htsIndexDto.setF5Accepted(htsIndexDto.getF5Accepted().add(dto.getF5Accepted()));
                htsIndexDto.setF10Accepted(htsIndexDto.getF10Accepted().add(dto.getF10Accepted()));
                htsIndexDto.setF15Accepted(htsIndexDto.getF15Accepted().add(dto.getF15Accepted()));
                htsIndexDto.setF20Accepted(htsIndexDto.getF20Accepted().add(dto.getF20Accepted()));
                htsIndexDto.setF25Accepted(htsIndexDto.getF25Accepted().add(dto.getF25Accepted()));
                htsIndexDto.setF30Accepted(htsIndexDto.getF30Accepted().add(dto.getF30Accepted()));
                htsIndexDto.setF35Accepted(htsIndexDto.getF35Accepted().add(dto.getF35Accepted()));
                htsIndexDto.setF40Accepted(htsIndexDto.getF40Accepted().add(dto.getF40Accepted()));
                htsIndexDto.setF45Accepted(htsIndexDto.getF45Accepted().add(dto.getF45Accepted()));
                htsIndexDto.setF50Accepted(htsIndexDto.getF50Accepted().add(dto.getF50Accepted()));

                htsIndexDto.setMuAccepted(htsIndexDto.getMuAccepted().add(dto.getMuAccepted()));
                htsIndexDto.setM0Accepted(htsIndexDto.getM0Accepted().add(dto.getM0Accepted()));
                htsIndexDto.setM1Accepted(htsIndexDto.getM1Accepted().add(dto.getM1Accepted()));
                htsIndexDto.setM5Accepted(htsIndexDto.getM5Accepted().add(dto.getM5Accepted()));
                htsIndexDto.setM10Accepted(htsIndexDto.getM10Accepted().add(dto.getM10Accepted()));
                htsIndexDto.setM15Accepted(htsIndexDto.getM15Accepted().add(dto.getM15Accepted()));
                htsIndexDto.setM20Accepted(htsIndexDto.getM20Accepted().add(dto.getM20Accepted()));
                htsIndexDto.setM25Accepted(htsIndexDto.getM25Accepted().add(dto.getM25Accepted()));
                htsIndexDto.setM30Accepted(htsIndexDto.getM30Accepted().add(dto.getM30Accepted()));
                htsIndexDto.setM35Accepted(htsIndexDto.getM35Accepted().add(dto.getM35Accepted()));
                htsIndexDto.setM40Accepted(htsIndexDto.getM40Accepted().add(dto.getM40Accepted()));
                htsIndexDto.setM45Accepted(htsIndexDto.getM45Accepted().add(dto.getM45Accepted()));
                htsIndexDto.setM50Accepted(htsIndexDto.getM50Accepted().add(dto.getM50Accepted()));



                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),htsIndexDto);
            }else{
                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),dto);
            }
        }

        for (HTSIndexDto dto : communityIndexTestingContactsElicited){
            if(hashtable.containsKey(dto.getOrgId()+"-"+dto.getModality())){
                htsIndexDto = hashtable.get(dto.getOrgId()+"-"+dto.getModality());

                htsIndexDto.setFuContactsElicited(htsIndexDto.getFuContactsElicited().add(dto.getFuContactsElicited()));
                htsIndexDto.setF0ContactsElicited(htsIndexDto.getF0ContactsElicited().add(dto.getF0ContactsElicited()));
                htsIndexDto.setF15ContactsElicited(htsIndexDto.getF15ContactsElicited().add(dto.getF15ContactsElicited()));

                htsIndexDto.setMuContactsElicited(htsIndexDto.getMuContactsElicited().add(dto.getMuContactsElicited()));
                htsIndexDto.setM0ContactsElicited(htsIndexDto.getM0ContactsElicited().add(dto.getM0ContactsElicited()));
                htsIndexDto.setM15ContactsElicited(htsIndexDto.getM15ContactsElicited().add(dto.getM15ContactsElicited()));

                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),htsIndexDto);
            }else{
                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),dto);
            }
        }

        for (HTSIndexDto dto : communityIndexTestingKnownPositives){
            if(hashtable.containsKey(dto.getOrgId()+"-"+dto.getModality())){
                htsIndexDto = hashtable.get(dto.getOrgId()+"-"+dto.getModality());

                htsIndexDto.setFuKnownPositives(htsIndexDto.getFuKnownPositives().add(dto.getFuKnownPositives()));
                htsIndexDto.setF0KnownPositives(htsIndexDto.getF0KnownPositives().add(dto.getF0KnownPositives()));
                htsIndexDto.setF1KnownPositives(htsIndexDto.getF1KnownPositives().add(dto.getF1KnownPositives()));
                htsIndexDto.setF5KnownPositives(htsIndexDto.getF5KnownPositives().add(dto.getF5KnownPositives()));
                htsIndexDto.setF10KnownPositives(htsIndexDto.getF10KnownPositives().add(dto.getF10KnownPositives()));
                htsIndexDto.setF15KnownPositives(htsIndexDto.getF15KnownPositives().add(dto.getF15KnownPositives()));
                htsIndexDto.setF20KnownPositives(htsIndexDto.getF20KnownPositives().add(dto.getF20KnownPositives()));
                htsIndexDto.setF25KnownPositives(htsIndexDto.getF25KnownPositives().add(dto.getF25KnownPositives()));
                htsIndexDto.setF30KnownPositives(htsIndexDto.getF30KnownPositives().add(dto.getF30KnownPositives()));
                htsIndexDto.setF35KnownPositives(htsIndexDto.getF35KnownPositives().add(dto.getF35KnownPositives()));
                htsIndexDto.setF40KnownPositives(htsIndexDto.getF40KnownPositives().add(dto.getF40KnownPositives()));
                htsIndexDto.setF45KnownPositives(htsIndexDto.getF45KnownPositives().add(dto.getF45KnownPositives()));
                htsIndexDto.setF50KnownPositives(htsIndexDto.getF50KnownPositives().add(dto.getF50KnownPositives()));

                htsIndexDto.setMuKnownPositives(htsIndexDto.getMuKnownPositives().add(dto.getMuKnownPositives()));
                htsIndexDto.setM0KnownPositives(htsIndexDto.getM0KnownPositives().add(dto.getM0KnownPositives()));
                htsIndexDto.setM1KnownPositives(htsIndexDto.getM1KnownPositives().add(dto.getM1KnownPositives()));
                htsIndexDto.setM5KnownPositives(htsIndexDto.getM5KnownPositives().add(dto.getM5KnownPositives()));
                htsIndexDto.setM10KnownPositives(htsIndexDto.getM10KnownPositives().add(dto.getM10KnownPositives()));
                htsIndexDto.setM15KnownPositives(htsIndexDto.getM15KnownPositives().add(dto.getM15KnownPositives()));
                htsIndexDto.setM20KnownPositives(htsIndexDto.getM20KnownPositives().add(dto.getM20KnownPositives()));
                htsIndexDto.setM25KnownPositives(htsIndexDto.getM25KnownPositives().add(dto.getM25KnownPositives()));
                htsIndexDto.setM30KnownPositives(htsIndexDto.getM30KnownPositives().add(dto.getM30KnownPositives()));
                htsIndexDto.setM35KnownPositives(htsIndexDto.getM35KnownPositives().add(dto.getM35KnownPositives()));
                htsIndexDto.setM40KnownPositives(htsIndexDto.getM40KnownPositives().add(dto.getM40KnownPositives()));
                htsIndexDto.setM45KnownPositives(htsIndexDto.getM45KnownPositives().add(dto.getM45KnownPositives()));
                htsIndexDto.setM50KnownPositives(htsIndexDto.getM50KnownPositives().add(dto.getM50KnownPositives()));



                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),htsIndexDto);
            }else{
                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),dto);
            }
        }

        for (HTSIndexDto dto : communityIndexTestingNewPositives){
            if(hashtable.containsKey(dto.getOrgId()+"-"+dto.getModality())){
                htsIndexDto = hashtable.get(dto.getOrgId()+"-"+dto.getModality());

                htsIndexDto.setFuNewPositives(htsIndexDto.getFuNewPositives().add(dto.getFuNewPositives()));
                htsIndexDto.setF0NewPositives(htsIndexDto.getF0NewPositives().add(dto.getF0NewPositives()));
                htsIndexDto.setF1NewPositives(htsIndexDto.getF1NewPositives().add(dto.getF1NewPositives()));
                htsIndexDto.setF5NewPositives(htsIndexDto.getF5NewPositives().add(dto.getF5NewPositives()));
                htsIndexDto.setF10NewPositives(htsIndexDto.getF10NewPositives().add(dto.getF10NewPositives()));
                htsIndexDto.setF15NewPositives(htsIndexDto.getF15NewPositives().add(dto.getF15NewPositives()));
                htsIndexDto.setF20NewPositives(htsIndexDto.getF20NewPositives().add(dto.getF20NewPositives()));
                htsIndexDto.setF25NewPositives(htsIndexDto.getF25NewPositives().add(dto.getF25NewPositives()));
                htsIndexDto.setF30NewPositives(htsIndexDto.getF30NewPositives().add(dto.getF30NewPositives()));
                htsIndexDto.setF35NewPositives(htsIndexDto.getF35NewPositives().add(dto.getF35NewPositives()));
                htsIndexDto.setF40NewPositives(htsIndexDto.getF40NewPositives().add(dto.getF40NewPositives()));
                htsIndexDto.setF45NewPositives(htsIndexDto.getF45NewPositives().add(dto.getF45NewPositives()));
                htsIndexDto.setF50NewPositives(htsIndexDto.getF50NewPositives().add(dto.getF50NewPositives()));

                htsIndexDto.setMuNewPositives(htsIndexDto.getMuNewPositives().add(dto.getMuNewPositives()));
                htsIndexDto.setM0NewPositives(htsIndexDto.getM0NewPositives().add(dto.getM0NewPositives()));
                htsIndexDto.setM1NewPositives(htsIndexDto.getM1NewPositives().add(dto.getM1NewPositives()));
                htsIndexDto.setM5NewPositives(htsIndexDto.getM5NewPositives().add(dto.getM5NewPositives()));
                htsIndexDto.setM10NewPositives(htsIndexDto.getM10NewPositives().add(dto.getM10NewPositives()));
                htsIndexDto.setM15NewPositives(htsIndexDto.getM15NewPositives().add(dto.getM15NewPositives()));
                htsIndexDto.setM20NewPositives(htsIndexDto.getM20NewPositives().add(dto.getM20NewPositives()));
                htsIndexDto.setM25NewPositives(htsIndexDto.getM25NewPositives().add(dto.getM25NewPositives()));
                htsIndexDto.setM30NewPositives(htsIndexDto.getM30NewPositives().add(dto.getM30NewPositives()));
                htsIndexDto.setM35NewPositives(htsIndexDto.getM35NewPositives().add(dto.getM35NewPositives()));
                htsIndexDto.setM40NewPositives(htsIndexDto.getM40NewPositives().add(dto.getM40NewPositives()));
                htsIndexDto.setM45NewPositives(htsIndexDto.getM45NewPositives().add(dto.getM45NewPositives()));
                htsIndexDto.setM50NewPositives(htsIndexDto.getM50NewPositives().add(dto.getM50NewPositives()));



                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),htsIndexDto);
            }else{
                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),dto);
            }
        }

        for (HTSIndexDto dto : communityIndexTestingNewNegatives){
            if(hashtable.containsKey(dto.getOrgId()+"-"+dto.getModality())){
                htsIndexDto = hashtable.get(dto.getOrgId()+"-"+dto.getModality());

                htsIndexDto.setFuNewNegatives(htsIndexDto.getFuNewNegatives().add(dto.getFuNewNegatives()));
                htsIndexDto.setF0NewNegatives(htsIndexDto.getF0NewNegatives().add(dto.getF0NewNegatives()));
                htsIndexDto.setF1NewNegatives(htsIndexDto.getF1NewNegatives().add(dto.getF1NewNegatives()));
                htsIndexDto.setF5NewNegatives(htsIndexDto.getF5NewNegatives().add(dto.getF5NewNegatives()));
                htsIndexDto.setF10NewNegatives(htsIndexDto.getF10NewNegatives().add(dto.getF10NewNegatives()));
                htsIndexDto.setF15NewNegatives(htsIndexDto.getF15NewNegatives().add(dto.getF15NewNegatives()));
                htsIndexDto.setF20NewNegatives(htsIndexDto.getF20NewNegatives().add(dto.getF20NewNegatives()));
                htsIndexDto.setF25NewNegatives(htsIndexDto.getF25NewNegatives().add(dto.getF25NewNegatives()));
                htsIndexDto.setF30NewNegatives(htsIndexDto.getF30NewNegatives().add(dto.getF30NewNegatives()));
                htsIndexDto.setF35NewNegatives(htsIndexDto.getF35NewNegatives().add(dto.getF35NewNegatives()));
                htsIndexDto.setF40NewNegatives(htsIndexDto.getF40NewNegatives().add(dto.getF40NewNegatives()));
                htsIndexDto.setF45NewNegatives(htsIndexDto.getF45NewNegatives().add(dto.getF45NewNegatives()));
                htsIndexDto.setF50NewNegatives(htsIndexDto.getF50NewNegatives().add(dto.getF50NewNegatives()));

                htsIndexDto.setMuNewNegatives(htsIndexDto.getMuNewNegatives().add(dto.getMuNewNegatives()));
                htsIndexDto.setM0NewNegatives(htsIndexDto.getM0NewNegatives().add(dto.getM0NewNegatives()));
                htsIndexDto.setM1NewNegatives(htsIndexDto.getM1NewNegatives().add(dto.getM1NewNegatives()));
                htsIndexDto.setM5NewNegatives(htsIndexDto.getM5NewNegatives().add(dto.getM5NewNegatives()));
                htsIndexDto.setM10NewNegatives(htsIndexDto.getM10NewNegatives().add(dto.getM10NewNegatives()));
                htsIndexDto.setM15NewNegatives(htsIndexDto.getM15NewNegatives().add(dto.getM15NewNegatives()));
                htsIndexDto.setM20NewNegatives(htsIndexDto.getM20NewNegatives().add(dto.getM20NewNegatives()));
                htsIndexDto.setM25NewNegatives(htsIndexDto.getM25NewNegatives().add(dto.getM25NewNegatives()));
                htsIndexDto.setM30NewNegatives(htsIndexDto.getM30NewNegatives().add(dto.getM30NewNegatives()));
                htsIndexDto.setM35NewNegatives(htsIndexDto.getM35NewNegatives().add(dto.getM35NewNegatives()));
                htsIndexDto.setM40NewNegatives(htsIndexDto.getM40NewNegatives().add(dto.getM40NewNegatives()));
                htsIndexDto.setM45NewNegatives(htsIndexDto.getM45NewNegatives().add(dto.getM45NewNegatives()));
                htsIndexDto.setM50NewNegatives(htsIndexDto.getM50NewNegatives().add(dto.getM50NewNegatives()));



                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),htsIndexDto);
            }else{
                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),dto);
            }
        }

        for (HTSIndexDto dto : facilityIndexTestingOffered){
            if(hashtable.containsKey(dto.getOrgId()+"-"+dto.getModality())){
                htsIndexDto = hashtable.get(dto.getOrgId()+"-"+dto.getModality());

                htsIndexDto.setFuOffered(htsIndexDto.getFuOffered().add(dto.getFuOffered()));
                htsIndexDto.setF0Offered(htsIndexDto.getF0Offered().add(dto.getF0Offered()));
                htsIndexDto.setF1Offered(htsIndexDto.getF1Offered().add(dto.getF1Offered()));
                htsIndexDto.setF5Offered(htsIndexDto.getF5Offered().add(dto.getF5Offered()));
                htsIndexDto.setF10Offered(htsIndexDto.getF10Offered().add(dto.getF10Offered()));
                htsIndexDto.setF15Offered(htsIndexDto.getF15Offered().add(dto.getF15Offered()));
                htsIndexDto.setF20Offered(htsIndexDto.getF20Offered().add(dto.getF20Offered()));
                htsIndexDto.setF25Offered(htsIndexDto.getF25Offered().add(dto.getF25Offered()));
                htsIndexDto.setF30Offered(htsIndexDto.getF30Offered().add(dto.getF30Offered()));
                htsIndexDto.setF35Offered(htsIndexDto.getF35Offered().add(dto.getF35Offered()));
                htsIndexDto.setF40Offered(htsIndexDto.getF40Offered().add(dto.getF40Offered()));
                htsIndexDto.setF45Offered(htsIndexDto.getF45Offered().add(dto.getF45Offered()));
                htsIndexDto.setF50Offered(htsIndexDto.getF50Offered().add(dto.getF50Offered()));

                htsIndexDto.setMuOffered(htsIndexDto.getMuOffered().add(dto.getMuOffered()));
                htsIndexDto.setM0Offered(htsIndexDto.getM0Offered().add(dto.getM0Offered()));
                htsIndexDto.setM1Offered(htsIndexDto.getM1Offered().add(dto.getM1Offered()));
                htsIndexDto.setM5Offered(htsIndexDto.getM5Offered().add(dto.getM5Offered()));
                htsIndexDto.setM10Offered(htsIndexDto.getM10Offered().add(dto.getM10Offered()));
                htsIndexDto.setM15Offered(htsIndexDto.getM15Offered().add(dto.getM15Offered()));
                htsIndexDto.setM20Offered(htsIndexDto.getM20Offered().add(dto.getM20Offered()));
                htsIndexDto.setM25Offered(htsIndexDto.getM25Offered().add(dto.getM25Offered()));
                htsIndexDto.setM30Offered(htsIndexDto.getM30Offered().add(dto.getM30Offered()));
                htsIndexDto.setM35Offered(htsIndexDto.getM35Offered().add(dto.getM35Offered()));
                htsIndexDto.setM40Offered(htsIndexDto.getM40Offered().add(dto.getM40Offered()));
                htsIndexDto.setM45Offered(htsIndexDto.getM45Offered().add(dto.getM45Offered()));
                htsIndexDto.setM50Offered(htsIndexDto.getM50Offered().add(dto.getM50Offered()));



                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),htsIndexDto);
            }else{
                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),dto);
            }
        }

        for (HTSIndexDto dto : facilityIndexTestingAccepted){
            if(hashtable.containsKey(dto.getOrgId()+"-"+dto.getModality())){
                htsIndexDto = hashtable.get(dto.getOrgId()+"-"+dto.getModality());

                htsIndexDto.setFuAccepted(htsIndexDto.getFuAccepted().add(dto.getFuAccepted()));
                htsIndexDto.setF0Accepted(htsIndexDto.getF0Accepted().add(dto.getF0Accepted()));
                htsIndexDto.setF1Accepted(htsIndexDto.getF1Accepted().add(dto.getF1Accepted()));
                htsIndexDto.setF5Accepted(htsIndexDto.getF5Accepted().add(dto.getF5Accepted()));
                htsIndexDto.setF10Accepted(htsIndexDto.getF10Accepted().add(dto.getF10Accepted()));
                htsIndexDto.setF15Accepted(htsIndexDto.getF15Accepted().add(dto.getF15Accepted()));
                htsIndexDto.setF20Accepted(htsIndexDto.getF20Accepted().add(dto.getF20Accepted()));
                htsIndexDto.setF25Accepted(htsIndexDto.getF25Accepted().add(dto.getF25Accepted()));
                htsIndexDto.setF30Accepted(htsIndexDto.getF30Accepted().add(dto.getF30Accepted()));
                htsIndexDto.setF35Accepted(htsIndexDto.getF35Accepted().add(dto.getF35Accepted()));
                htsIndexDto.setF40Accepted(htsIndexDto.getF40Accepted().add(dto.getF40Accepted()));
                htsIndexDto.setF45Accepted(htsIndexDto.getF45Accepted().add(dto.getF45Accepted()));
                htsIndexDto.setF50Accepted(htsIndexDto.getF50Accepted().add(dto.getF50Accepted()));

                htsIndexDto.setMuAccepted(htsIndexDto.getMuAccepted().add(dto.getMuAccepted()));
                htsIndexDto.setM0Accepted(htsIndexDto.getM0Accepted().add(dto.getM0Accepted()));
                htsIndexDto.setM1Accepted(htsIndexDto.getM1Accepted().add(dto.getM1Accepted()));
                htsIndexDto.setM5Accepted(htsIndexDto.getM5Accepted().add(dto.getM5Accepted()));
                htsIndexDto.setM10Accepted(htsIndexDto.getM10Accepted().add(dto.getM10Accepted()));
                htsIndexDto.setM15Accepted(htsIndexDto.getM15Accepted().add(dto.getM15Accepted()));
                htsIndexDto.setM20Accepted(htsIndexDto.getM20Accepted().add(dto.getM20Accepted()));
                htsIndexDto.setM25Accepted(htsIndexDto.getM25Accepted().add(dto.getM25Accepted()));
                htsIndexDto.setM30Accepted(htsIndexDto.getM30Accepted().add(dto.getM30Accepted()));
                htsIndexDto.setM35Accepted(htsIndexDto.getM35Accepted().add(dto.getM35Accepted()));
                htsIndexDto.setM40Accepted(htsIndexDto.getM40Accepted().add(dto.getM40Accepted()));
                htsIndexDto.setM45Accepted(htsIndexDto.getM45Accepted().add(dto.getM45Accepted()));
                htsIndexDto.setM50Accepted(htsIndexDto.getM50Accepted().add(dto.getM50Accepted()));



                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),htsIndexDto);
            }else{
                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),dto);
            }
        }

        for (HTSIndexDto dto : facilityIndexTestingContactsElicited){
            if(hashtable.containsKey(dto.getOrgId()+"-"+dto.getModality())){
                htsIndexDto = hashtable.get(dto.getOrgId()+"-"+dto.getModality());

                htsIndexDto.setFuContactsElicited(htsIndexDto.getFuContactsElicited().add(dto.getFuContactsElicited()));
                htsIndexDto.setF0ContactsElicited(htsIndexDto.getF0ContactsElicited().add(dto.getF0ContactsElicited()));
                htsIndexDto.setF15ContactsElicited(htsIndexDto.getF15ContactsElicited().add(dto.getF15ContactsElicited()));

                htsIndexDto.setMuContactsElicited(htsIndexDto.getMuContactsElicited().add(dto.getMuContactsElicited()));
                htsIndexDto.setM0ContactsElicited(htsIndexDto.getM0ContactsElicited().add(dto.getM0ContactsElicited()));
                htsIndexDto.setM15ContactsElicited(htsIndexDto.getM15ContactsElicited().add(dto.getM15ContactsElicited()));

                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),htsIndexDto);
            }else{
                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),dto);
            }
        }

        for (HTSIndexDto dto : facilityIndexTestingKnownPositives){
            if(hashtable.containsKey(dto.getOrgId()+"-"+dto.getModality())){
                htsIndexDto = hashtable.get(dto.getOrgId()+"-"+dto.getModality());

                htsIndexDto.setFuKnownPositives(htsIndexDto.getFuKnownPositives().add(dto.getFuKnownPositives()));
                htsIndexDto.setF0KnownPositives(htsIndexDto.getF0KnownPositives().add(dto.getF0KnownPositives()));
                htsIndexDto.setF1KnownPositives(htsIndexDto.getF1KnownPositives().add(dto.getF1KnownPositives()));
                htsIndexDto.setF5KnownPositives(htsIndexDto.getF5KnownPositives().add(dto.getF5KnownPositives()));
                htsIndexDto.setF10KnownPositives(htsIndexDto.getF10KnownPositives().add(dto.getF10KnownPositives()));
                htsIndexDto.setF15KnownPositives(htsIndexDto.getF15KnownPositives().add(dto.getF15KnownPositives()));
                htsIndexDto.setF20KnownPositives(htsIndexDto.getF20KnownPositives().add(dto.getF20KnownPositives()));
                htsIndexDto.setF25KnownPositives(htsIndexDto.getF25KnownPositives().add(dto.getF25KnownPositives()));
                htsIndexDto.setF30KnownPositives(htsIndexDto.getF30KnownPositives().add(dto.getF30KnownPositives()));
                htsIndexDto.setF35KnownPositives(htsIndexDto.getF35KnownPositives().add(dto.getF35KnownPositives()));
                htsIndexDto.setF40KnownPositives(htsIndexDto.getF40KnownPositives().add(dto.getF40KnownPositives()));
                htsIndexDto.setF45KnownPositives(htsIndexDto.getF45KnownPositives().add(dto.getF45KnownPositives()));
                htsIndexDto.setF50KnownPositives(htsIndexDto.getF50KnownPositives().add(dto.getF50KnownPositives()));

                htsIndexDto.setMuKnownPositives(htsIndexDto.getMuKnownPositives().add(dto.getMuKnownPositives()));
                htsIndexDto.setM0KnownPositives(htsIndexDto.getM0KnownPositives().add(dto.getM0KnownPositives()));
                htsIndexDto.setM1KnownPositives(htsIndexDto.getM1KnownPositives().add(dto.getM1KnownPositives()));
                htsIndexDto.setM5KnownPositives(htsIndexDto.getM5KnownPositives().add(dto.getM5KnownPositives()));
                htsIndexDto.setM10KnownPositives(htsIndexDto.getM10KnownPositives().add(dto.getM10KnownPositives()));
                htsIndexDto.setM15KnownPositives(htsIndexDto.getM15KnownPositives().add(dto.getM15KnownPositives()));
                htsIndexDto.setM20KnownPositives(htsIndexDto.getM20KnownPositives().add(dto.getM20KnownPositives()));
                htsIndexDto.setM25KnownPositives(htsIndexDto.getM25KnownPositives().add(dto.getM25KnownPositives()));
                htsIndexDto.setM30KnownPositives(htsIndexDto.getM30KnownPositives().add(dto.getM30KnownPositives()));
                htsIndexDto.setM35KnownPositives(htsIndexDto.getM35KnownPositives().add(dto.getM35KnownPositives()));
                htsIndexDto.setM40KnownPositives(htsIndexDto.getM40KnownPositives().add(dto.getM40KnownPositives()));
                htsIndexDto.setM45KnownPositives(htsIndexDto.getM45KnownPositives().add(dto.getM45KnownPositives()));
                htsIndexDto.setM50KnownPositives(htsIndexDto.getM50KnownPositives().add(dto.getM50KnownPositives()));



                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),htsIndexDto);
            }else{
                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),dto);
            }
        }

        for (HTSIndexDto dto : facilityIndexTestingDocumentedNegatives){
            if(hashtable.containsKey(dto.getOrgId()+"-"+dto.getModality())){
                htsIndexDto = hashtable.get(dto.getOrgId()+"-"+dto.getModality());

                htsIndexDto.setF1DocumentedNegatives(htsIndexDto.getF1DocumentedNegatives().add(dto.getF1DocumentedNegatives()));
                htsIndexDto.setF5DocumentedNegatives(htsIndexDto.getF5DocumentedNegatives().add(dto.getF5DocumentedNegatives()));
                htsIndexDto.setF10DocumentedNegatives(htsIndexDto.getF10DocumentedNegatives().add(dto.getF10DocumentedNegatives()));
                htsIndexDto.setM1DocumentedNegatives(htsIndexDto.getM1DocumentedNegatives().add(dto.getM1DocumentedNegatives()));
                htsIndexDto.setM5DocumentedNegatives(htsIndexDto.getM5DocumentedNegatives().add(dto.getM5DocumentedNegatives()));
                htsIndexDto.setM10DocumentedNegatives(htsIndexDto.getM10DocumentedNegatives().add(dto.getM10DocumentedNegatives()));

                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),htsIndexDto);
            }else{
                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),dto);
            }
        }

        for (HTSIndexDto dto : facilityIndexTestingNewPositives){
            if(hashtable.containsKey(dto.getOrgId()+"-"+dto.getModality())){
                htsIndexDto = hashtable.get(dto.getOrgId()+"-"+dto.getModality());

                htsIndexDto.setFuNewPositives(htsIndexDto.getFuNewPositives().add(dto.getFuNewPositives()));
                htsIndexDto.setF0NewPositives(htsIndexDto.getF0NewPositives().add(dto.getF0NewPositives()));
                htsIndexDto.setF1NewPositives(htsIndexDto.getF1NewPositives().add(dto.getF1NewPositives()));
                htsIndexDto.setF5NewPositives(htsIndexDto.getF5NewPositives().add(dto.getF5NewPositives()));
                htsIndexDto.setF10NewPositives(htsIndexDto.getF10NewPositives().add(dto.getF10NewPositives()));
                htsIndexDto.setF15NewPositives(htsIndexDto.getF15NewPositives().add(dto.getF15NewPositives()));
                htsIndexDto.setF20NewPositives(htsIndexDto.getF20NewPositives().add(dto.getF20NewPositives()));
                htsIndexDto.setF25NewPositives(htsIndexDto.getF25NewPositives().add(dto.getF25NewPositives()));
                htsIndexDto.setF30NewPositives(htsIndexDto.getF30NewPositives().add(dto.getF30NewPositives()));
                htsIndexDto.setF35NewPositives(htsIndexDto.getF35NewPositives().add(dto.getF35NewPositives()));
                htsIndexDto.setF40NewPositives(htsIndexDto.getF40NewPositives().add(dto.getF40NewPositives()));
                htsIndexDto.setF45NewPositives(htsIndexDto.getF45NewPositives().add(dto.getF45NewPositives()));
                htsIndexDto.setF50NewPositives(htsIndexDto.getF50NewPositives().add(dto.getF50NewPositives()));

                htsIndexDto.setMuNewPositives(htsIndexDto.getMuNewPositives().add(dto.getMuNewPositives()));
                htsIndexDto.setM0NewPositives(htsIndexDto.getM0NewPositives().add(dto.getM0NewPositives()));
                htsIndexDto.setM1NewPositives(htsIndexDto.getM1NewPositives().add(dto.getM1NewPositives()));
                htsIndexDto.setM5NewPositives(htsIndexDto.getM5NewPositives().add(dto.getM5NewPositives()));
                htsIndexDto.setM10NewPositives(htsIndexDto.getM10NewPositives().add(dto.getM10NewPositives()));
                htsIndexDto.setM15NewPositives(htsIndexDto.getM15NewPositives().add(dto.getM15NewPositives()));
                htsIndexDto.setM20NewPositives(htsIndexDto.getM20NewPositives().add(dto.getM20NewPositives()));
                htsIndexDto.setM25NewPositives(htsIndexDto.getM25NewPositives().add(dto.getM25NewPositives()));
                htsIndexDto.setM30NewPositives(htsIndexDto.getM30NewPositives().add(dto.getM30NewPositives()));
                htsIndexDto.setM35NewPositives(htsIndexDto.getM35NewPositives().add(dto.getM35NewPositives()));
                htsIndexDto.setM40NewPositives(htsIndexDto.getM40NewPositives().add(dto.getM40NewPositives()));
                htsIndexDto.setM45NewPositives(htsIndexDto.getM45NewPositives().add(dto.getM45NewPositives()));
                htsIndexDto.setM50NewPositives(htsIndexDto.getM50NewPositives().add(dto.getM50NewPositives()));



                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),htsIndexDto);
            }else{
                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),dto);
            }
        }

        for (HTSIndexDto dto : facilityIndexTestingNewNegatives){
            if(hashtable.containsKey(dto.getOrgId()+"-"+dto.getModality())){
                htsIndexDto = hashtable.get(dto.getOrgId()+"-"+dto.getModality());

                htsIndexDto.setFuNewNegatives(htsIndexDto.getFuNewNegatives().add(dto.getFuNewNegatives()));
                htsIndexDto.setF0NewNegatives(htsIndexDto.getF0NewNegatives().add(dto.getF0NewNegatives()));
                htsIndexDto.setF1NewNegatives(htsIndexDto.getF1NewNegatives().add(dto.getF1NewNegatives()));
                htsIndexDto.setF5NewNegatives(htsIndexDto.getF5NewNegatives().add(dto.getF5NewNegatives()));
                htsIndexDto.setF10NewNegatives(htsIndexDto.getF10NewNegatives().add(dto.getF10NewNegatives()));
                htsIndexDto.setF15NewNegatives(htsIndexDto.getF15NewNegatives().add(dto.getF15NewNegatives()));
                htsIndexDto.setF20NewNegatives(htsIndexDto.getF20NewNegatives().add(dto.getF20NewNegatives()));
                htsIndexDto.setF25NewNegatives(htsIndexDto.getF25NewNegatives().add(dto.getF25NewNegatives()));
                htsIndexDto.setF30NewNegatives(htsIndexDto.getF30NewNegatives().add(dto.getF30NewNegatives()));
                htsIndexDto.setF35NewNegatives(htsIndexDto.getF35NewNegatives().add(dto.getF35NewNegatives()));
                htsIndexDto.setF40NewNegatives(htsIndexDto.getF40NewNegatives().add(dto.getF40NewNegatives()));
                htsIndexDto.setF45NewNegatives(htsIndexDto.getF45NewNegatives().add(dto.getF45NewNegatives()));
                htsIndexDto.setF50NewNegatives(htsIndexDto.getF50NewNegatives().add(dto.getF50NewNegatives()));

                htsIndexDto.setMuNewNegatives(htsIndexDto.getMuNewNegatives().add(dto.getMuNewNegatives()));
                htsIndexDto.setM0NewNegatives(htsIndexDto.getM0NewNegatives().add(dto.getM0NewNegatives()));
                htsIndexDto.setM1NewNegatives(htsIndexDto.getM1NewNegatives().add(dto.getM1NewNegatives()));
                htsIndexDto.setM5NewNegatives(htsIndexDto.getM5NewNegatives().add(dto.getM5NewNegatives()));
                htsIndexDto.setM10NewNegatives(htsIndexDto.getM10NewNegatives().add(dto.getM10NewNegatives()));
                htsIndexDto.setM15NewNegatives(htsIndexDto.getM15NewNegatives().add(dto.getM15NewNegatives()));
                htsIndexDto.setM20NewNegatives(htsIndexDto.getM20NewNegatives().add(dto.getM20NewNegatives()));
                htsIndexDto.setM25NewNegatives(htsIndexDto.getM25NewNegatives().add(dto.getM25NewNegatives()));
                htsIndexDto.setM30NewNegatives(htsIndexDto.getM30NewNegatives().add(dto.getM30NewNegatives()));
                htsIndexDto.setM35NewNegatives(htsIndexDto.getM35NewNegatives().add(dto.getM35NewNegatives()));
                htsIndexDto.setM40NewNegatives(htsIndexDto.getM40NewNegatives().add(dto.getM40NewNegatives()));
                htsIndexDto.setM45NewNegatives(htsIndexDto.getM45NewNegatives().add(dto.getM45NewNegatives()));
                htsIndexDto.setM50NewNegatives(htsIndexDto.getM50NewNegatives().add(dto.getM50NewNegatives()));



                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),htsIndexDto);
            }else{
                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),dto);
            }
        }

        return new ArrayList<>(hashtable.values());
    }

//    private List<HTSIndexDto> getDataHTSIndexModalityFacility2(PreventionFilterDto filter) {
//        HashMap<String, HTSIndexDto> hashTable = new HashMap<>();
//
//        String whereClauseHTS = "";
//        if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
//            whereClauseHTS += " AND tbl.c2_org_id in (:listOrg) ";
//        }
//        if (filter.getFromDate() != null) {
//            whereClauseHTS += " AND tbl.c4_counselling_date >=:fromDate ";
//        }
//        if (filter.getToDate() != null) {
//            whereClauseHTS += "  AND tbl.c4_counselling_date <=:toDate ";
//        }
//
//        String sqlHTS = "select * from(\n" +
//                "select tou.code as orgCode , tou.name as  orgName, pro.name as provinceName, dis.name as districtName,\n" +
//                "       case when (tbl.VCT != 0 or tbl.c10 = 'answer2') and tbl.c10!='answer2' and  tbl.c5 = 'answer2' and tbl.c15 ='YES' then 'Facility Index Testing' end as modality,\n" +
//                "       count(if(tbl.c7_gender = 'FEMALE' and (tbl.c24='answer1' or tbl.c24='answer3') and tbl.c2_org_id in (select  id from tbl_organization_unit) and tbl.c8_dob is null,1,null)) as fuNewPositives,\n" +
//                "       count(if(tbl.c7_gender = 'FEMALE' and (tbl.c24='answer1' or tbl.c24='answer3') and (year(curdate())-year(tbl.c8_dob)<1),1,null)) as f0NewPositives,\n" +
//                "       count(if(tbl.c7_gender = 'FEMALE' and (tbl.c24='answer1' or tbl.c24='answer3') and (year(curdate())-year(tbl.c8_dob)>=1) and (year(curdate())-year(tbl.c8_dob)<5),1,null)) as f1NewPositives,\n" +
//                "       count(if(tbl.c7_gender = 'FEMALE' and (tbl.c24='answer1' or tbl.c24='answer3') and (year(curdate())-year(tbl.c8_dob)>=5) and (year(curdate())-year(tbl.c8_dob)<10),1,null)) as f5NewPositives,\n" +
//                "       count(if(tbl.c7_gender = 'FEMALE' and (tbl.c24='answer1' or tbl.c24='answer3') and (year(curdate())-year(tbl.c8_dob)>=10) and (year(curdate())-year(tbl.c8_dob)<15),1,null)) as f10NewPositives,\n" +
//                "       count(if(tbl.c7_gender = 'FEMALE' and (tbl.c24='answer1' or tbl.c24='answer3') and (year(curdate())-year(tbl.c8_dob)>=15) and (year(curdate())-year(tbl.c8_dob)<20),1,null)) as f15NewPositives,\n" +
//                "       count(if(tbl.c7_gender = 'FEMALE' and (tbl.c24='answer1' or tbl.c24='answer3') and (year(curdate())-year(tbl.c8_dob)>=20) and (year(curdate())-year(tbl.c8_dob)<25),1,null)) as f20NewPositives,\n" +
//                "       count(if(tbl.c7_gender = 'FEMALE' and (tbl.c24='answer1' or tbl.c24='answer3') and (year(curdate())-year(tbl.c8_dob)>=25) and (year(curdate())-year(tbl.c8_dob)<30),1,null)) as f25NewPositives,\n" +
//                "       count(if(tbl.c7_gender = 'FEMALE' and (tbl.c24='answer1' or tbl.c24='answer3') and (year(curdate())-year(tbl.c8_dob)>=30) and (year(curdate())-year(tbl.c8_dob)<35),1,null)) as f30NewPositives,\n" +
//                "       count(if(tbl.c7_gender = 'FEMALE' and (tbl.c24='answer1' or tbl.c24='answer3') and (year(curdate())-year(tbl.c8_dob)>=35) and (year(curdate())-year(tbl.c8_dob)<40),1,null)) as f35NewPositives,\n" +
//                "       count(if(tbl.c7_gender = 'FEMALE' and (tbl.c24='answer1' or tbl.c24='answer3') and (year(curdate())-year(tbl.c8_dob)>=40) and (year(curdate())-year(tbl.c8_dob)<45),1,null)) as f40NewPositives,\n" +
//                "       count(if(tbl.c7_gender = 'FEMALE' and (tbl.c24='answer1' or tbl.c24='answer3') and (year(curdate())-year(tbl.c8_dob)>=45) and (year(curdate())-year(tbl.c8_dob)<50),1,null)) as f45NewPositives,\n" +
//                "       count(if(tbl.c7_gender = 'FEMALE' and (tbl.c24='answer1' or tbl.c24='answer3') and (year(curdate())-year(tbl.c8_dob)>=50),1,null)) as f50NewPositives,\n" +
//                "       count(if(tbl.c7_gender = 'MALE' and (tbl.c24='answer1' or tbl.c24='answer3') and tbl.c8_dob is null,1,null)) as muNewPositives,\n" +
//                "       count(if(tbl.c7_gender = 'MALE' and (tbl.c24='answer1' or tbl.c24='answer3') and (year(curdate())-year(tbl.c8_dob)<1),1,null)) as m0NewPositives,\n" +
//                "       count(if(tbl.c7_gender = 'MALE' and (tbl.c24='answer1' or tbl.c24='answer3') and (year(curdate())-year(tbl.c8_dob)>=1) and (year(curdate())-year(tbl.c8_dob)<5),1,null)) as m1NewPositives,\n" +
//                "       count(if(tbl.c7_gender = 'MALE' and (tbl.c24='answer1' or tbl.c24='answer3') and (year(curdate())-year(tbl.c8_dob)>=5) and (year(curdate())-year(tbl.c8_dob)<10),1,null)) as m5NewPositives,\n" +
//                "       count(if(tbl.c7_gender = 'MALE' and (tbl.c24='answer1' or tbl.c24='answer3') and (year(curdate())-year(tbl.c8_dob)>=10) and (year(curdate())-year(tbl.c8_dob)<15),1,null)) as m10NewPositives,\n" +
//                "       count(if(tbl.c7_gender = 'MALE' and (tbl.c24='answer1' or tbl.c24='answer3') and (year(curdate())-year(tbl.c8_dob)>=15) and (year(curdate())-year(tbl.c8_dob)<20),1,null)) as m15NewPositives,\n" +
//                "       count(if(tbl.c7_gender = 'MALE' and (tbl.c24='answer1' or tbl.c24='answer3') and (year(curdate())-year(tbl.c8_dob)>=20) and (year(curdate())-year(tbl.c8_dob)<25),1,null)) as m20NewPositives,\n" +
//                "       count(if(tbl.c7_gender = 'MALE' and (tbl.c24='answer1' or tbl.c24='answer3') and (year(curdate())-year(tbl.c8_dob)>=25) and (year(curdate())-year(tbl.c8_dob)<30),1,null)) as m25NewPositives,\n" +
//                "       count(if(tbl.c7_gender = 'MALE' and (tbl.c24='answer1' or tbl.c24='answer3') and (year(curdate())-year(tbl.c8_dob)>=30) and (year(curdate())-year(tbl.c8_dob)<35),1,null)) as m30NewPositives,\n" +
//                "       count(if(tbl.c7_gender = 'MALE' and (tbl.c24='answer1' or tbl.c24='answer3') and (year(curdate())-year(tbl.c8_dob)>=35) and (year(curdate())-year(tbl.c8_dob)<40),1,null)) as m35NewPositives,\n" +
//                "       count(if(tbl.c7_gender = 'MALE' and (tbl.c24='answer1' or tbl.c24='answer3') and (year(curdate())-year(tbl.c8_dob)>=40) and (year(curdate())-year(tbl.c8_dob)<45),1,null)) as m40NewPositives,\n" +
//                "       count(if(tbl.c7_gender = 'MALE' and (tbl.c24='answer1' or tbl.c24='answer3') and (year(curdate())-year(tbl.c8_dob)>=45) and (year(curdate())-year(tbl.c8_dob)<50),1,null)) as m45NewPositives,\n" +
//                "       count(if(tbl.c7_gender = 'MALE' and (tbl.c24='answer1' or tbl.c24='answer3') and (year(curdate())-year(tbl.c8_dob)>=50),1,null)) as m50NewPositives,\n" +
//                "       count(if(tbl.c7_gender = 'FEMALE' and tbl.c14='answer1' and tbl.c8_dob is null,1,null)) as fuNewNegatives,\n" +
//                "       count(if(tbl.c7_gender = 'FEMALE' and tbl.c14='answer1' and (year(curdate())-year(tbl.c8_dob)<1),1,null)) as f0NewNegatives,\n" +
//                "       count(if(tbl.c7_gender = 'FEMALE' and tbl.c14='answer1' and (year(curdate())-year(tbl.c8_dob)>=1) and (year(curdate())-year(tbl.c8_dob)<5),1,null)) as f1NewNegatives,\n" +
//                "       count(if(tbl.c7_gender = 'FEMALE' and tbl.c14='answer1' and (year(curdate())-year(tbl.c8_dob)>=5) and (year(curdate())-year(tbl.c8_dob)<10),1,null)) as f5NewNegatives,\n" +
//                "       count(if(tbl.c7_gender = 'FEMALE' and tbl.c14='answer1' and (year(curdate())-year(tbl.c8_dob)>=10) and (year(curdate())-year(tbl.c8_dob)<15),1,null)) as f10NewNegatives,\n" +
//                "       count(if(tbl.c7_gender = 'FEMALE' and tbl.c14='answer1' and (year(curdate())-year(tbl.c8_dob)>=15) and (year(curdate())-year(tbl.c8_dob)<20),1,null)) as f15NewNegatives,\n" +
//                "       count(if(tbl.c7_gender = 'FEMALE' and tbl.c14='answer1' and (year(curdate())-year(tbl.c8_dob)>=20) and (year(curdate())-year(tbl.c8_dob)<25),1,null)) as f20NewNegatives,\n" +
//                "       count(if(tbl.c7_gender = 'FEMALE' and tbl.c14='answer1' and (year(curdate())-year(tbl.c8_dob)>=25) and (year(curdate())-year(tbl.c8_dob)<30),1,null)) as f25NewNegatives,\n" +
//                "       count(if(tbl.c7_gender = 'FEMALE' and tbl.c14='answer1' and (year(curdate())-year(tbl.c8_dob)>=30) and (year(curdate())-year(tbl.c8_dob)<35),1,null)) as f30NewNegatives,\n" +
//                "       count(if(tbl.c7_gender = 'FEMALE' and tbl.c14='answer1' and (year(curdate())-year(tbl.c8_dob)>=35) and (year(curdate())-year(tbl.c8_dob)<40),1,null)) as f35NewNegatives,\n" +
//                "       count(if(tbl.c7_gender = 'FEMALE' and tbl.c14='answer1' and (year(curdate())-year(tbl.c8_dob)>=40) and (year(curdate())-year(tbl.c8_dob)<45),1,null)) as f40NewNegatives,\n" +
//                "       count(if(tbl.c7_gender = 'FEMALE' and tbl.c14='answer1' and (year(curdate())-year(tbl.c8_dob)>=45) and (year(curdate())-year(tbl.c8_dob)<50),1,null)) as f45NewNegatives,\n" +
//                "       count(if(tbl.c7_gender = 'FEMALE' and tbl.c14='answer1' and (year(curdate())-year(tbl.c8_dob)>=50),1,null)) as f50NewNegatives,\n" +
//                "       count(if(tbl.c7_gender = 'MALE' and tbl.c14='answer1' and tbl.c8_dob is null,1,null)) as muNewNegatives,\n" +
//                "       count(if(tbl.c7_gender = 'MALE' and tbl.c14='answer1' and (year(curdate())-year(tbl.c8_dob)<1),1,null)) as m0NewNegatives,\n" +
//                "       count(if(tbl.c7_gender = 'MALE' and tbl.c14='answer1' and (year(curdate())-year(tbl.c8_dob)>=1) and (year(curdate())-year(tbl.c8_dob)<5),1,null)) as m1NewNegatives,\n" +
//                "       count(if(tbl.c7_gender = 'MALE' and tbl.c14='answer1' and (year(curdate())-year(tbl.c8_dob)>=5) and (year(curdate())-year(tbl.c8_dob)<10),1,null)) as m5NewNegatives,\n" +
//                "       count(if(tbl.c7_gender = 'MALE' and tbl.c14='answer1' and (year(curdate())-year(tbl.c8_dob)>=10) and (year(curdate())-year(tbl.c8_dob)<15),1,null)) as m10NewNegatives,\n" +
//                "       count(if(tbl.c7_gender = 'MALE' and tbl.c14='answer1' and (year(curdate())-year(tbl.c8_dob)>=15) and (year(curdate())-year(tbl.c8_dob)<20),1,null)) as m15NewNegatives,\n" +
//                "       count(if(tbl.c7_gender = 'MALE' and tbl.c14='answer1' and (year(curdate())-year(tbl.c8_dob)>=20) and (year(curdate())-year(tbl.c8_dob)<25),1,null)) as m20NewNegatives,\n" +
//                "       count(if(tbl.c7_gender = 'MALE' and tbl.c14='answer1' and (year(curdate())-year(tbl.c8_dob)>=25) and (year(curdate())-year(tbl.c8_dob)<30),1,null)) as m25NewNegatives,\n" +
//                "       count(if(tbl.c7_gender = 'MALE' and tbl.c14='answer1' and (year(curdate())-year(tbl.c8_dob)>=30) and (year(curdate())-year(tbl.c8_dob)<35),1,null)) as m30NewNegatives,\n" +
//                "       count(if(tbl.c7_gender = 'MALE' and tbl.c14='answer1' and (year(curdate())-year(tbl.c8_dob)>=35) and (year(curdate())-year(tbl.c8_dob)<40),1,null)) as m35NewNegatives,\n" +
//                "       count(if(tbl.c7_gender = 'MALE' and tbl.c14='answer1' and (year(curdate())-year(tbl.c8_dob)>=40) and (year(curdate())-year(tbl.c8_dob)<45),1,null)) as m40NewNegatives,\n" +
//                "       count(if(tbl.c7_gender = 'MALE' and tbl.c14='answer1' and (year(curdate())-year(tbl.c8_dob)>=45) and (year(curdate())-year(tbl.c8_dob)<50),1,null)) as m45NewNegatives,\n" +
//                "       count(if(tbl.c7_gender = 'MALE' and tbl.c14='answer1' and (year(curdate())-year(tbl.c8_dob)>=50),1,null)) as m50NewNegatives\n" +
//                " from (select thcrg.val,tb1.VCT, thc.* from tbl_hts_case_risk_group thcrg\n" +
//                "        inner join tbl_hts_case thc on thcrg.hts_case_id = thc.id\n" +
//                "        inner join (select thcrg.hts_case_id, count(if((thcrg.val='answer5' or thcrg.val='answer6'),1,null)) as VCT from tbl_hts_case_risk_group thcrg\n" +
//                "        group by  thcrg.hts_case_id) tb1 on tb1.hts_case_id = thc.id\n" +
//                "      where thcrg.is_main_risk = 1 ) as tbl\n" +
//                "    inner join tbl_organization_unit tou on tbl.c2_org_id = tou.id\n" +
//                " inner join tbl_location tl on tou.address_id = tl.id\n" +
//                " inner join tbl_admin_unit pro on tl.province_id = pro.id\n" +
//                " inner join tbl_admin_unit dis on tl.district_id = dis.id\n" +
//                " where 1=1 " + whereClauseHTS + " group by orgCode,orgName,provinceName,districtName,modality ) as tb \n" +
//                " where tb.modality is not null";
//        org.hibernate.Query queryHTS = manager.unwrap(Session.class).createSQLQuery(sqlHTS).setResultTransformer(new AliasToBeanResultTransformer(HTSIndexDto.class));
//        if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
//            queryHTS.setParameterList("listOrg", filter.getOrgIds());
//        }
//        if (filter.getFromDate() != null) {
//            queryHTS.setParameter("fromDate", Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
//        }
//        if (filter.getToDate() != null) {
//            queryHTS.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
//        }
//        List<HTSIndexDto> retHTS = queryHTS.list();
//
//        //pns1
//        String wherePNS1 = "";
//        if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
//            wherePNS1 += " AND tbl.c2_org_id in (:listOrg) ";
//        }
//        if (filter.getFromDate() != null) {
//            wherePNS1 += " AND tbl.c5_date_counselling >=:fromDate ";
//        }
//        if (filter.getToDate() != null) {
//            wherePNS1 += "  AND tbl.c5_date_counselling <=:toDate ";
//        }
//
//        String sqlPNS1 = "select tou.code as orgCode , tou.name as  orgName, pro.name as provinceName, dis.name as districtName,\n" +
//                "       count(if(tbl.c8_gender = 'FEMALE'  and tbl.c6_dob is null,1,null)) as fuOffered,\n" +
//                "       count(if(tbl.c8_gender = 'FEMALE'  and (year(curdate())-year(tbl.c6_dob)<1),1,null)) as f0Offered,\n" +
//                "       count(if(tbl.c8_gender = 'FEMALE'  and (year(curdate())-year(tbl.c6_dob)>=1) and (year(curdate())-year(tbl.c6_dob)<5),1,null)) as f1Offered,\n" +
//                "       count(if(tbl.c8_gender = 'FEMALE'  and (year(curdate())-year(tbl.c6_dob)>=5) and (year(curdate())-year(tbl.c6_dob)<10),1,null)) as f5Offered,\n" +
//                "       count(if(tbl.c8_gender = 'FEMALE'  and (year(curdate())-year(tbl.c6_dob)>=10) and (year(curdate())-year(tbl.c6_dob)<15),1,null)) as f10Offered,\n" +
//                "       count(if(tbl.c8_gender = 'FEMALE'  and (year(curdate())-year(tbl.c6_dob)>=15) and (year(curdate())-year(tbl.c6_dob)<20),1,null)) as f15Offered,\n" +
//                "       count(if(tbl.c8_gender = 'FEMALE'  and (year(curdate())-year(tbl.c6_dob)>=20) and (year(curdate())-year(tbl.c6_dob)<25),1,null)) as f20Offered,\n" +
//                "       count(if(tbl.c8_gender = 'FEMALE'  and (year(curdate())-year(tbl.c6_dob)>=25) and (year(curdate())-year(tbl.c6_dob)<30),1,null)) as f25Offered,\n" +
//                "       count(if(tbl.c8_gender = 'FEMALE'  and (year(curdate())-year(tbl.c6_dob)>=30) and (year(curdate())-year(tbl.c6_dob)<35),1,null)) as f30Offered,\n" +
//                "       count(if(tbl.c8_gender = 'FEMALE'  and (year(curdate())-year(tbl.c6_dob)>=35) and (year(curdate())-year(tbl.c6_dob)<40),1,null)) as f35Offered,\n" +
//                "       count(if(tbl.c8_gender = 'FEMALE'  and (year(curdate())-year(tbl.c6_dob)>=40) and (year(curdate())-year(tbl.c6_dob)<45),1,null)) as f40Offered,\n" +
//                "       count(if(tbl.c8_gender = 'FEMALE'  and (year(curdate())-year(tbl.c6_dob)>=45) and (year(curdate())-year(tbl.c6_dob)<50),1,null)) as f45Offered,\n" +
//                "       count(if(tbl.c8_gender = 'FEMALE'  and (year(curdate())-year(tbl.c6_dob)>=50),1,null)) as f50Offered,\n" +
//                "       count(if(tbl.c8_gender = 'MALE'  and tbl.c6_dob is null,1,null)) as muOffered,\n" +
//                "       count(if(tbl.c8_gender = 'MALE'  and (year(curdate())-year(tbl.c6_dob)<1),1,null)) as m0Offered,\n" +
//                "       count(if(tbl.c8_gender = 'MALE'  and (year(curdate())-year(tbl.c6_dob)>=1) and (year(curdate())-year(tbl.c6_dob)<5),1,null)) as m1Offered,\n" +
//                "       count(if(tbl.c8_gender = 'MALE'  and (year(curdate())-year(tbl.c6_dob)>=5) and (year(curdate())-year(tbl.c6_dob)<10),1,null)) as m5Offered,\n" +
//                "       count(if(tbl.c8_gender = 'MALE'  and (year(curdate())-year(tbl.c6_dob)>=10) and (year(curdate())-year(tbl.c6_dob)<15),1,null)) as m10Offered,\n" +
//                "       count(if(tbl.c8_gender = 'MALE'  and (year(curdate())-year(tbl.c6_dob)>=15) and (year(curdate())-year(tbl.c6_dob)<20),1,null)) as m15Offered,\n" +
//                "       count(if(tbl.c8_gender = 'MALE'  and (year(curdate())-year(tbl.c6_dob)>=20) and (year(curdate())-year(tbl.c6_dob)<25),1,null)) as m20Offered,\n" +
//                "       count(if(tbl.c8_gender = 'MALE'  and (year(curdate())-year(tbl.c6_dob)>=25) and (year(curdate())-year(tbl.c6_dob)<30),1,null)) as m25Offered,\n" +
//                "       count(if(tbl.c8_gender = 'MALE'  and (year(curdate())-year(tbl.c6_dob)>=30) and (year(curdate())-year(tbl.c6_dob)<35),1,null)) as m30Offered,\n" +
//                "       count(if(tbl.c8_gender = 'MALE'  and (year(curdate())-year(tbl.c6_dob)>=35) and (year(curdate())-year(tbl.c6_dob)<40),1,null)) as m35Offered,\n" +
//                "       count(if(tbl.c8_gender = 'MALE'  and (year(curdate())-year(tbl.c6_dob)>=40) and (year(curdate())-year(tbl.c6_dob)<45),1,null)) as m40Offered,\n" +
//                "       count(if(tbl.c8_gender = 'MALE'  and (year(curdate())-year(tbl.c6_dob)>=45) and (year(curdate())-year(tbl.c6_dob)<50),1,null)) as m45Offered,\n" +
//                "       count(if(tbl.c8_gender = 'MALE'  and (year(curdate())-year(tbl.c6_dob)>=50),1,null)) as m50Offered\n" +
//                " from tbl_pns_case tbl\n" +
//                "         inner join tbl_organization_unit tou on tbl.c2_org_id = tou.id\n" +
//                "         inner join tbl_location tl on tou.address_id = tl.id\n" +
//                "         inner join tbl_admin_unit pro on tl.province_id = pro.id\n" +
//                "         inner join tbl_admin_unit dis on tl.district_id = dis.id\n" +
//                " where 1=1 " + wherePNS1 + " group by orgCode,orgName,provinceName,districtName";
//        org.hibernate.Query queryPNS1 = manager.unwrap(Session.class).createSQLQuery(sqlPNS1).setResultTransformer(new AliasToBeanResultTransformer(HTSIndexDto.class));
//        if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
//            queryPNS1.setParameterList("listOrg", filter.getOrgIds());
//        }
//        if (filter.getFromDate() != null) {
//            queryPNS1.setParameter("fromDate", Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
//        }
//        if (filter.getToDate() != null) {
//            queryPNS1.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
//        }
//        List<HTSIndexDto> retPNS1 = queryPNS1.list();
//
//        String wherePNS2 = " AND tbl.c6_accept_service = 'YES' ";
//        if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
//            wherePNS2 += " AND tbl.c2_org_id in (:listOrg) ";
//        }
//        if (filter.getFromDate() != null) {
//            wherePNS2 += " AND tbl.c6_date_service >=:fromDate ";
//        }
//        if (filter.getToDate() != null) {
//            wherePNS2 += "  AND tbl.c6_date_service <=:toDate ";
//        }
//
//        String sqlPNS2 = "select tou.code as orgCode , tou.name as  orgName, pro.name as provinceName, dis.name as districtName,\n" +
//                "       count(if(tbl.c8_gender = 'FEMALE'  and tbl.c6_dob is null,1,null)) as fuAccepted,\n" +
//                "       count(if(tbl.c8_gender = 'FEMALE'  and (year(curdate())-year(tbl.c6_dob)<1),1,null)) as f0Accepted,\n" +
//                "       count(if(tbl.c8_gender = 'FEMALE'  and (year(curdate())-year(tbl.c6_dob)>=1) and (year(curdate())-year(tbl.c6_dob)<5),1,null)) as f1Accepted,\n" +
//                "       count(if(tbl.c8_gender = 'FEMALE'  and (year(curdate())-year(tbl.c6_dob)>=5) and (year(curdate())-year(tbl.c6_dob)<10),1,null)) as f5Accepted,\n" +
//                "       count(if(tbl.c8_gender = 'FEMALE'  and (year(curdate())-year(tbl.c6_dob)>=10) and (year(curdate())-year(tbl.c6_dob)<15),1,null)) as f10Accepted,\n" +
//                "       count(if(tbl.c8_gender = 'FEMALE'  and (year(curdate())-year(tbl.c6_dob)>=15) and (year(curdate())-year(tbl.c6_dob)<20),1,null)) as f15Accepted,\n" +
//                "       count(if(tbl.c8_gender = 'FEMALE'  and (year(curdate())-year(tbl.c6_dob)>=20) and (year(curdate())-year(tbl.c6_dob)<25),1,null)) as f20Accepted,\n" +
//                "       count(if(tbl.c8_gender = 'FEMALE'  and (year(curdate())-year(tbl.c6_dob)>=25) and (year(curdate())-year(tbl.c6_dob)<30),1,null)) as f25Accepted,\n" +
//                "       count(if(tbl.c8_gender = 'FEMALE'  and (year(curdate())-year(tbl.c6_dob)>=30) and (year(curdate())-year(tbl.c6_dob)<35),1,null)) as f30Accepted,\n" +
//                "       count(if(tbl.c8_gender = 'FEMALE'  and (year(curdate())-year(tbl.c6_dob)>=35) and (year(curdate())-year(tbl.c6_dob)<40),1,null)) as f35Accepted,\n" +
//                "       count(if(tbl.c8_gender = 'FEMALE'  and (year(curdate())-year(tbl.c6_dob)>=40) and (year(curdate())-year(tbl.c6_dob)<45),1,null)) as f40Accepted,\n" +
//                "       count(if(tbl.c8_gender = 'FEMALE'  and (year(curdate())-year(tbl.c6_dob)>=45) and (year(curdate())-year(tbl.c6_dob)<50),1,null)) as f45Accepted,\n" +
//                "       count(if(tbl.c8_gender = 'FEMALE'  and (year(curdate())-year(tbl.c6_dob)>=50),1,null)) as f50Accepted,\n" +
//                "       count(if(tbl.c8_gender = 'MALE'  and tbl.c6_dob is null,1,null)) as muAccepted,\n" +
//                "       count(if(tbl.c8_gender = 'MALE'  and (year(curdate())-year(tbl.c6_dob)<1),1,null)) as m0Accepted,\n" +
//                "       count(if(tbl.c8_gender = 'MALE'  and (year(curdate())-year(tbl.c6_dob)>=1) and (year(curdate())-year(tbl.c6_dob)<5),1,null)) as m1Accepted,\n" +
//                "       count(if(tbl.c8_gender = 'MALE'  and (year(curdate())-year(tbl.c6_dob)>=5) and (year(curdate())-year(tbl.c6_dob)<10),1,null)) as m5Accepted,\n" +
//                "       count(if(tbl.c8_gender = 'MALE'  and (year(curdate())-year(tbl.c6_dob)>=10) and (year(curdate())-year(tbl.c6_dob)<15),1,null)) as m10Accepted,\n" +
//                "       count(if(tbl.c8_gender = 'MALE'  and (year(curdate())-year(tbl.c6_dob)>=15) and (year(curdate())-year(tbl.c6_dob)<20),1,null)) as m15Accepted,\n" +
//                "       count(if(tbl.c8_gender = 'MALE'  and (year(curdate())-year(tbl.c6_dob)>=20) and (year(curdate())-year(tbl.c6_dob)<25),1,null)) as m20Accepted,\n" +
//                "       count(if(tbl.c8_gender = 'MALE'  and (year(curdate())-year(tbl.c6_dob)>=25) and (year(curdate())-year(tbl.c6_dob)<30),1,null)) as m25Accepted,\n" +
//                "       count(if(tbl.c8_gender = 'MALE'  and (year(curdate())-year(tbl.c6_dob)>=30) and (year(curdate())-year(tbl.c6_dob)<35),1,null)) as m30Accepted,\n" +
//                "       count(if(tbl.c8_gender = 'MALE'  and (year(curdate())-year(tbl.c6_dob)>=35) and (year(curdate())-year(tbl.c6_dob)<40),1,null)) as m35Accepted,\n" +
//                "       count(if(tbl.c8_gender = 'MALE'  and (year(curdate())-year(tbl.c6_dob)>=40) and (year(curdate())-year(tbl.c6_dob)<45),1,null)) as m40Accepted,\n" +
//                "       count(if(tbl.c8_gender = 'MALE'  and (year(curdate())-year(tbl.c6_dob)>=45) and (year(curdate())-year(tbl.c6_dob)<50),1,null)) as m45Accepted,\n" +
//                "       count(if(tbl.c8_gender = 'MALE'  and (year(curdate())-year(tbl.c6_dob)>=50),1,null)) as m50Accepted\n" +
//                " from tbl_pns_case tbl\n" +
//                "         inner join tbl_organization_unit tou on tbl.c2_org_id = tou.id\n" +
//                "         inner join tbl_location tl on tou.address_id = tl.id\n" +
//                "         inner join tbl_admin_unit pro on tl.province_id = pro.id\n" +
//                "         inner join tbl_admin_unit dis on tl.district_id = dis.id\n" +
//                " where 1=1 " + wherePNS2 + " group by orgCode,orgName,provinceName,districtName";
//        org.hibernate.Query queryPNS2 = manager.unwrap(Session.class).createSQLQuery(sqlPNS2).setResultTransformer(new AliasToBeanResultTransformer(HTSIndexDto.class));
//        if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
//            queryPNS2.setParameterList("listOrg", filter.getOrgIds());
//        }
//        if (filter.getFromDate() != null) {
//            queryPNS2.setParameter("fromDate", Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
//        }
//        if (filter.getToDate() != null) {
//            queryPNS2.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
//        }
//        List<HTSIndexDto> retPNS2 = queryPNS2.list();
//
//        String wherePNSContact = "";
//        if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
//            wherePNSContact += " AND tpc.c2_org_id in (:listOrg) ";
//        }
//        if (filter.getFromDate() != null) {
//            wherePNSContact += " AND tbl.c1_received_info_date >=:fromDate ";
//        }
//        if (filter.getToDate() != null) {
//            wherePNSContact += "  AND tbl.c1_received_info_date <=:toDate ";
//        }
//        String sqlPNSContact = "select tou.code as orgCode , tou.name as  orgName, pro.name as provinceName, dis.name as districtName,\n" +
//                "       count(if(tp.gender = 'FEMALE'  and tp.dob is null,1,null)) as fuContactsElicited,\n" +
//                "       count(if(tp.gender = 'FEMALE'  and (year(curdate())-year(tp.dob)<15),1,null)) as f0ContactsElicited,\n" +
//                "       count(if(tp.gender = 'FEMALE'  and (year(curdate())-year(tp.dob)>=15),1,null)) as f15ContactsElicited,\n" +
//                "       count(if(tp.gender = 'MALE'  and tp.dob is null,1,null)) as muContactsElicited,\n" +
//                "       count(if(tp.gender = 'MALE'  and (year(curdate())-year(tp.dob)<15),1,null)) as m0ContactsElicited,\n" +
//                "       count(if(tp.gender = 'MALE'  and (year(curdate())-year(tp.dob)>=15),1,null)) as m15ContactsElicited,\n" +
//                "       count(if(tp.gender = 'FEMALE'  and tbl.c2_hiv_status='answer1' and tp.dob is null,1,null)) as fuKnownPositives,\n" +
//                "       count(if(tp.gender = 'FEMALE'  and tbl.c2_hiv_status='answer1' and (year(curdate())-year(tp.dob)<1),1,null)) as f0KnownPositives,\n" +
//                "       count(if(tp.gender = 'FEMALE'  and tbl.c2_hiv_status='answer1' and (year(curdate())-year(tp.dob)>=1) and (year(curdate())-year(tp.dob)<5),1,null)) as f1KnownPositives,\n" +
//                "       count(if(tp.gender = 'FEMALE'  and tbl.c2_hiv_status='answer1' and (year(curdate())-year(tp.dob)>=5) and (year(curdate())-year(tp.dob)<10),1,null)) as f5KnownPositives,\n" +
//                "       count(if(tp.gender = 'FEMALE'  and tbl.c2_hiv_status='answer1' and (year(curdate())-year(tp.dob)>=10) and (year(curdate())-year(tp.dob)<15),1,null)) as f10KnownPositives,\n" +
//                "       count(if(tp.gender = 'FEMALE'  and tbl.c2_hiv_status='answer1' and (year(curdate())-year(tp.dob)>=15) and (year(curdate())-year(tp.dob)<20),1,null)) as f15KnownPositives,\n" +
//                "       count(if(tp.gender = 'FEMALE'  and tbl.c2_hiv_status='answer1' and (year(curdate())-year(tp.dob)>=20) and (year(curdate())-year(tp.dob)<25),1,null)) as f20KnownPositives,\n" +
//                "       count(if(tp.gender = 'FEMALE'  and tbl.c2_hiv_status='answer1' and (year(curdate())-year(tp.dob)>=25) and (year(curdate())-year(tp.dob)<30),1,null)) as f25KnownPositives,\n" +
//                "       count(if(tp.gender = 'FEMALE'  and tbl.c2_hiv_status='answer1' and (year(curdate())-year(tp.dob)>=30) and (year(curdate())-year(tp.dob)<35),1,null)) as f30KnownPositives,\n" +
//                "       count(if(tp.gender = 'FEMALE'  and tbl.c2_hiv_status='answer1' and (year(curdate())-year(tp.dob)>=35) and (year(curdate())-year(tp.dob)<40),1,null)) as f35KnownPositives,\n" +
//                "       count(if(tp.gender = 'FEMALE'  and tbl.c2_hiv_status='answer1' and (year(curdate())-year(tp.dob)>=40) and (year(curdate())-year(tp.dob)<45),1,null)) as f40KnownPositives,\n" +
//                "       count(if(tp.gender = 'FEMALE'  and tbl.c2_hiv_status='answer1' and (year(curdate())-year(tp.dob)>=45) and (year(curdate())-year(tp.dob)<50),1,null)) as f45KnownPositives,\n" +
//                "       count(if(tp.gender = 'FEMALE'  and tbl.c2_hiv_status='answer1' and (year(curdate())-year(tp.dob)>=50),1,null)) as f50KnownPositives,\n" +
//                "       count(if(tp.gender = 'MALE'  and tbl.c2_hiv_status='answer1' and tp.dob is null,1,null)) as muKnownPositives,\n" +
//                "       count(if(tp.gender = 'MALE'  and tbl.c2_hiv_status='answer1' and (year(curdate())-year(tp.dob)<1),1,null)) as m0KnownPositives,\n" +
//                "       count(if(tp.gender = 'MALE'  and tbl.c2_hiv_status='answer1' and (year(curdate())-year(tp.dob)>=1) and (year(curdate())-year(tp.dob)<5),1,null)) as m1KnownPositives,\n" +
//                "       count(if(tp.gender = 'MALE'  and tbl.c2_hiv_status='answer1' and (year(curdate())-year(tp.dob)>=5) and (year(curdate())-year(tp.dob)<10),1,null)) as m5KnownPositives,\n" +
//                "       count(if(tp.gender = 'MALE'  and tbl.c2_hiv_status='answer1' and (year(curdate())-year(tp.dob)>=10) and (year(curdate())-year(tp.dob)<15),1,null)) as m10KnownPositives,\n" +
//                "       count(if(tp.gender = 'MALE'  and tbl.c2_hiv_status='answer1' and (year(curdate())-year(tp.dob)>=15) and (year(curdate())-year(tp.dob)<20),1,null)) as m15KnownPositives,\n" +
//                "       count(if(tp.gender = 'MALE'  and tbl.c2_hiv_status='answer1' and (year(curdate())-year(tp.dob)>=20) and (year(curdate())-year(tp.dob)<25),1,null)) as m20KnownPositives,\n" +
//                "       count(if(tp.gender = 'MALE'  and tbl.c2_hiv_status='answer1' and (year(curdate())-year(tp.dob)>=25) and (year(curdate())-year(tp.dob)<30),1,null)) as m25KnownPositives,\n" +
//                "       count(if(tp.gender = 'MALE'  and tbl.c2_hiv_status='answer1' and (year(curdate())-year(tp.dob)>=30) and (year(curdate())-year(tp.dob)<35),1,null)) as m30KnownPositives,\n" +
//                "       count(if(tp.gender = 'MALE'  and tbl.c2_hiv_status='answer1' and (year(curdate())-year(tp.dob)>=35) and (year(curdate())-year(tp.dob)<40),1,null)) as m35KnownPositives,\n" +
//                "       count(if(tp.gender = 'MALE'  and tbl.c2_hiv_status='answer1' and (year(curdate())-year(tp.dob)>=40) and (year(curdate())-year(tp.dob)<45),1,null)) as m40KnownPositives,\n" +
//                "       count(if(tp.gender = 'MALE'  and tbl.c2_hiv_status='answer1' and (year(curdate())-year(tp.dob)>=45) and (year(curdate())-year(tp.dob)<50),1,null)) as m45KnownPositives,\n" +
//                "       count(if(tp.gender = 'MALE'  and tbl.c2_hiv_status='answer1' and (year(curdate())-year(tp.dob)>=50),1,null)) as m50KnownPositives,\n" +
//                "       count(if(tp.gender = 'FEMALE' and tbl.c2_hiv_status='answer2' and (year(curdate())-year(tp.dob)>=1) and (year(curdate())-year(tp.dob)<5),1,null)) as f1DocumentedNegatives,\n" +
//                "       count(if(tp.gender = 'FEMALE' and tbl.c2_hiv_status='answer2' and (year(curdate())-year(tp.dob)>=5) and (year(curdate())-year(tp.dob)<10),1,null)) as f5DocumentedNegatives,\n" +
//                "       count(if(tp.gender = 'FEMALE' and tbl.c2_hiv_status='answer2' and (year(curdate())-year(tp.dob)>=10) and (year(curdate())-year(tp.dob)<15),1,null)) as f10DocumentedNegatives,\n" +
//                "       count(if(tp.gender = 'MALE' and tbl.c2_hiv_status='answer2'  and (year(curdate())-year(tp.dob)>=1) and (year(curdate())-year(tp.dob)<5),1,null)) as m1DocumentedNegatives,\n" +
//                "       count(if(tp.gender = 'MALE' and tbl.c2_hiv_status='answer2' and (year(curdate())-year(tp.dob)>=5) and (year(curdate())-year(tp.dob)<10),1,null)) as m5DocumentedNegatives,\n" +
//                "       count(if(tp.gender = 'MALE' and tbl.c2_hiv_status='answer2' and (year(curdate())-year(tp.dob)>=10) and (year(curdate())-year(tp.dob)<15),1,null)) as m10DocumentedNegatives\n" +
//                " from tbl_pns_case_contact tbl\n" +
//                "    inner join tbl_person tp on tbl.person_id = tp.id\n" +
//                "    inner join tbl_pns_case tpc on tbl.pns_case_id = tpc.id\n" +
//                "    inner join tbl_organization_unit tou on tpc.c2_org_id = tou.id\n" +
//                "    inner join tbl_location tl on tou.address_id = tl.id\n" +
//                "    inner join tbl_admin_unit pro on tl.province_id = pro.id\n" +
//                "    inner join tbl_admin_unit dis on tl.district_id = dis.id\n" +
//                " where 1=1 " + wherePNSContact + " group by orgCode,orgName,provinceName,districtName";
//        org.hibernate.Query queryPNSContact = manager.unwrap(Session.class).createSQLQuery(sqlPNSContact).setResultTransformer(new AliasToBeanResultTransformer(HTSIndexDto.class));
//        if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
//            queryPNSContact.setParameterList("listOrg", filter.getOrgIds());
//        }
//        if (filter.getFromDate() != null) {
//            queryPNSContact.setParameter("fromDate", Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
//        }
//        if (filter.getToDate() != null) {
//            queryPNSContact.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
//        }
//        List<HTSIndexDto> retPNSContact2 = queryPNSContact.list();
//
//        for (HTSIndexDto dto : retHTS) {
//            if (hashTable.containsKey(dto.getOrgCode())) {
//                hashTable.get(dto.getOrgCode()).setFuNewPositives(dto.getFuNewPositives());
//                hashTable.get(dto.getOrgCode()).setF0NewPositives(dto.getF0NewPositives());
//                hashTable.get(dto.getOrgCode()).setF1NewPositives(dto.getF1NewPositives());
//                hashTable.get(dto.getOrgCode()).setF5NewPositives(dto.getF5NewPositives());
//                hashTable.get(dto.getOrgCode()).setF10NewPositives(dto.getF10NewPositives());
//                hashTable.get(dto.getOrgCode()).setF15NewPositives(dto.getF15NewPositives());
//                hashTable.get(dto.getOrgCode()).setF20NewPositives(dto.getF20NewPositives());
//                hashTable.get(dto.getOrgCode()).setF25NewPositives(dto.getF25NewPositives());
//                hashTable.get(dto.getOrgCode()).setF30NewPositives(dto.getF30NewPositives());
//                hashTable.get(dto.getOrgCode()).setF35NewPositives(dto.getF35NewPositives());
//                hashTable.get(dto.getOrgCode()).setF40NewPositives(dto.getF40NewPositives());
//                hashTable.get(dto.getOrgCode()).setF45NewPositives(dto.getF45NewPositives());
//                hashTable.get(dto.getOrgCode()).setF50NewPositives(dto.getF50NewPositives());
//
//                hashTable.get(dto.getOrgCode()).setMuNewPositives(dto.getMuNewPositives());
//                hashTable.get(dto.getOrgCode()).setM0NewPositives(dto.getM0NewPositives());
//                hashTable.get(dto.getOrgCode()).setM1NewPositives(dto.getM1NewPositives());
//                hashTable.get(dto.getOrgCode()).setM5NewPositives(dto.getM5NewPositives());
//                hashTable.get(dto.getOrgCode()).setM10NewPositives(dto.getM10NewPositives());
//                hashTable.get(dto.getOrgCode()).setM15NewPositives(dto.getM15NewPositives());
//                hashTable.get(dto.getOrgCode()).setM20NewPositives(dto.getM20NewPositives());
//                hashTable.get(dto.getOrgCode()).setM25NewPositives(dto.getM25NewPositives());
//                hashTable.get(dto.getOrgCode()).setM30NewPositives(dto.getM30NewPositives());
//                hashTable.get(dto.getOrgCode()).setM35NewPositives(dto.getM35NewPositives());
//                hashTable.get(dto.getOrgCode()).setM40NewPositives(dto.getM40NewPositives());
//                hashTable.get(dto.getOrgCode()).setM45NewPositives(dto.getM45NewPositives());
//                hashTable.get(dto.getOrgCode()).setM50NewPositives(dto.getM50NewPositives());
//
//                hashTable.get(dto.getOrgCode()).setFuNewNegatives(dto.getFuNewNegatives());
//                hashTable.get(dto.getOrgCode()).setF0NewNegatives(dto.getF0NewNegatives());
//                hashTable.get(dto.getOrgCode()).setF1NewNegatives(dto.getF1NewNegatives());
//                hashTable.get(dto.getOrgCode()).setF5NewNegatives(dto.getF5NewNegatives());
//                hashTable.get(dto.getOrgCode()).setF10NewNegatives(dto.getF10NewNegatives());
//                hashTable.get(dto.getOrgCode()).setF15NewNegatives(dto.getF15NewNegatives());
//                hashTable.get(dto.getOrgCode()).setF20NewNegatives(dto.getF20NewNegatives());
//                hashTable.get(dto.getOrgCode()).setF25NewNegatives(dto.getF25NewNegatives());
//                hashTable.get(dto.getOrgCode()).setF30NewNegatives(dto.getF30NewNegatives());
//                hashTable.get(dto.getOrgCode()).setF35NewNegatives(dto.getF35NewNegatives());
//                hashTable.get(dto.getOrgCode()).setF40NewNegatives(dto.getF40NewNegatives());
//                hashTable.get(dto.getOrgCode()).setF45NewNegatives(dto.getF45NewNegatives());
//                hashTable.get(dto.getOrgCode()).setF50NewNegatives(dto.getF50NewNegatives());
//
//                hashTable.get(dto.getOrgCode()).setMuNewNegatives(dto.getMuNewNegatives());
//                hashTable.get(dto.getOrgCode()).setM0NewNegatives(dto.getM0NewNegatives());
//                hashTable.get(dto.getOrgCode()).setM1NewNegatives(dto.getM1NewNegatives());
//                hashTable.get(dto.getOrgCode()).setM5NewNegatives(dto.getM5NewNegatives());
//                hashTable.get(dto.getOrgCode()).setM10NewNegatives(dto.getM10NewNegatives());
//                hashTable.get(dto.getOrgCode()).setM15NewNegatives(dto.getM15NewNegatives());
//                hashTable.get(dto.getOrgCode()).setM20NewNegatives(dto.getM20NewNegatives());
//                hashTable.get(dto.getOrgCode()).setM25NewNegatives(dto.getM25NewNegatives());
//                hashTable.get(dto.getOrgCode()).setM30NewNegatives(dto.getM30NewNegatives());
//                hashTable.get(dto.getOrgCode()).setM35NewNegatives(dto.getM35NewNegatives());
//                hashTable.get(dto.getOrgCode()).setM40NewNegatives(dto.getM40NewNegatives());
//                hashTable.get(dto.getOrgCode()).setM45NewNegatives(dto.getM45NewNegatives());
//                hashTable.get(dto.getOrgCode()).setM50NewNegatives(dto.getM50NewNegatives());
//            } else {
//                dto.setModality("Facility Index Testing");
//                hashTable.put(dto.getOrgCode(), dto);
//            }
//        }
//
//        for (HTSIndexDto dto : retPNS1) {
//            if (hashTable.containsKey(dto.getOrgCode())) {
//                hashTable.get(dto.getOrgCode()).setFuOffered(dto.getFuOffered());
//                hashTable.get(dto.getOrgCode()).setF0Offered(dto.getF0Offered());
//                hashTable.get(dto.getOrgCode()).setF1Offered(dto.getF1Offered());
//                hashTable.get(dto.getOrgCode()).setF5Offered(dto.getF5Offered());
//                hashTable.get(dto.getOrgCode()).setF10Offered(dto.getF10Offered());
//                hashTable.get(dto.getOrgCode()).setF15Offered(dto.getF15Offered());
//                hashTable.get(dto.getOrgCode()).setF20Offered(dto.getF20Offered());
//                hashTable.get(dto.getOrgCode()).setF25Offered(dto.getF25Offered());
//                hashTable.get(dto.getOrgCode()).setF30Offered(dto.getF30Offered());
//                hashTable.get(dto.getOrgCode()).setF35Offered(dto.getF35Offered());
//                hashTable.get(dto.getOrgCode()).setF40Offered(dto.getF40Offered());
//                hashTable.get(dto.getOrgCode()).setF45Offered(dto.getF45Offered());
//                hashTable.get(dto.getOrgCode()).setF50Offered(dto.getF50Offered());
//
//                hashTable.get(dto.getOrgCode()).setMuOffered(dto.getMuOffered());
//                hashTable.get(dto.getOrgCode()).setM0Offered(dto.getM0Offered());
//                hashTable.get(dto.getOrgCode()).setM1Offered(dto.getM1Offered());
//                hashTable.get(dto.getOrgCode()).setM5Offered(dto.getM5Offered());
//                hashTable.get(dto.getOrgCode()).setM10Offered(dto.getM10Offered());
//                hashTable.get(dto.getOrgCode()).setM15Offered(dto.getM15Offered());
//                hashTable.get(dto.getOrgCode()).setM20Offered(dto.getM20Offered());
//                hashTable.get(dto.getOrgCode()).setM25Offered(dto.getM25Offered());
//                hashTable.get(dto.getOrgCode()).setM30Offered(dto.getM30Offered());
//                hashTable.get(dto.getOrgCode()).setM35Offered(dto.getM35Offered());
//                hashTable.get(dto.getOrgCode()).setM40Offered(dto.getM40Offered());
//                hashTable.get(dto.getOrgCode()).setM45Offered(dto.getM45Offered());
//                hashTable.get(dto.getOrgCode()).setM50Offered(dto.getM50Offered());
//
//            } else {
//                dto.setModality("Facility Index Testing");
//                hashTable.put(dto.getOrgCode(), dto);
//            }
//        }
//
//        for (HTSIndexDto dto : retPNS2) {
//            if (hashTable.containsKey(dto.getOrgCode())) {
//                hashTable.get(dto.getOrgCode()).setFuAccepted(dto.getFuAccepted());
//                hashTable.get(dto.getOrgCode()).setF0Accepted(dto.getF0Accepted());
//                hashTable.get(dto.getOrgCode()).setF1Accepted(dto.getF1Accepted());
//                hashTable.get(dto.getOrgCode()).setF5Accepted(dto.getF5Accepted());
//                hashTable.get(dto.getOrgCode()).setF10Accepted(dto.getF10Accepted());
//                hashTable.get(dto.getOrgCode()).setF15Accepted(dto.getF15Accepted());
//                hashTable.get(dto.getOrgCode()).setF20Accepted(dto.getF20Accepted());
//                hashTable.get(dto.getOrgCode()).setF25Accepted(dto.getF25Accepted());
//                hashTable.get(dto.getOrgCode()).setF30Accepted(dto.getF30Accepted());
//                hashTable.get(dto.getOrgCode()).setF35Accepted(dto.getF35Accepted());
//                hashTable.get(dto.getOrgCode()).setF40Accepted(dto.getF40Accepted());
//                hashTable.get(dto.getOrgCode()).setF45Accepted(dto.getF45Accepted());
//                hashTable.get(dto.getOrgCode()).setF50Accepted(dto.getF50Accepted());
//
//                hashTable.get(dto.getOrgCode()).setMuAccepted(dto.getMuAccepted());
//                hashTable.get(dto.getOrgCode()).setM0Accepted(dto.getM0Accepted());
//                hashTable.get(dto.getOrgCode()).setM1Accepted(dto.getM1Accepted());
//                hashTable.get(dto.getOrgCode()).setM5Accepted(dto.getM5Accepted());
//                hashTable.get(dto.getOrgCode()).setM10Accepted(dto.getM10Accepted());
//                hashTable.get(dto.getOrgCode()).setM15Accepted(dto.getM15Accepted());
//                hashTable.get(dto.getOrgCode()).setM20Accepted(dto.getM20Accepted());
//                hashTable.get(dto.getOrgCode()).setM25Accepted(dto.getM25Accepted());
//                hashTable.get(dto.getOrgCode()).setM30Accepted(dto.getM30Accepted());
//                hashTable.get(dto.getOrgCode()).setM35Accepted(dto.getM35Accepted());
//                hashTable.get(dto.getOrgCode()).setM40Accepted(dto.getM40Accepted());
//                hashTable.get(dto.getOrgCode()).setM45Accepted(dto.getM45Accepted());
//                hashTable.get(dto.getOrgCode()).setM50Accepted(dto.getM50Accepted());
//
//            } else {
//                dto.setModality("Facility Index Testing");
//                hashTable.put(dto.getOrgCode(), dto);
//            }
//        }
//
//        for (HTSIndexDto dto : retPNSContact2) {
//            if (hashTable.containsKey(dto.getOrgCode())) {
//                hashTable.get(dto.getOrgCode()).setFuKnownPositives(dto.getFuKnownPositives());
//                hashTable.get(dto.getOrgCode()).setF0KnownPositives(dto.getF0KnownPositives());
//                hashTable.get(dto.getOrgCode()).setF1KnownPositives(dto.getF1KnownPositives());
//                hashTable.get(dto.getOrgCode()).setF5KnownPositives(dto.getF5KnownPositives());
//                hashTable.get(dto.getOrgCode()).setF10KnownPositives(dto.getF10KnownPositives());
//                hashTable.get(dto.getOrgCode()).setF15KnownPositives(dto.getF15KnownPositives());
//                hashTable.get(dto.getOrgCode()).setF20KnownPositives(dto.getF20KnownPositives());
//                hashTable.get(dto.getOrgCode()).setF25KnownPositives(dto.getF25KnownPositives());
//                hashTable.get(dto.getOrgCode()).setF30KnownPositives(dto.getF30KnownPositives());
//                hashTable.get(dto.getOrgCode()).setF35KnownPositives(dto.getF35KnownPositives());
//                hashTable.get(dto.getOrgCode()).setF40KnownPositives(dto.getF40KnownPositives());
//                hashTable.get(dto.getOrgCode()).setF45KnownPositives(dto.getF45KnownPositives());
//                hashTable.get(dto.getOrgCode()).setF50KnownPositives(dto.getF50KnownPositives());
//
//                hashTable.get(dto.getOrgCode()).setMuKnownPositives(dto.getMuKnownPositives());
//                hashTable.get(dto.getOrgCode()).setM0KnownPositives(dto.getM0KnownPositives());
//                hashTable.get(dto.getOrgCode()).setM1KnownPositives(dto.getM1KnownPositives());
//                hashTable.get(dto.getOrgCode()).setM5KnownPositives(dto.getM5KnownPositives());
//                hashTable.get(dto.getOrgCode()).setM10KnownPositives(dto.getM10KnownPositives());
//                hashTable.get(dto.getOrgCode()).setM15KnownPositives(dto.getM15KnownPositives());
//                hashTable.get(dto.getOrgCode()).setM20KnownPositives(dto.getM20KnownPositives());
//                hashTable.get(dto.getOrgCode()).setM25KnownPositives(dto.getM25KnownPositives());
//                hashTable.get(dto.getOrgCode()).setM30KnownPositives(dto.getM30KnownPositives());
//                hashTable.get(dto.getOrgCode()).setM35KnownPositives(dto.getM35KnownPositives());
//                hashTable.get(dto.getOrgCode()).setM40KnownPositives(dto.getM40KnownPositives());
//                hashTable.get(dto.getOrgCode()).setM45KnownPositives(dto.getM45KnownPositives());
//                hashTable.get(dto.getOrgCode()).setM50KnownPositives(dto.getM50KnownPositives());
//
//                hashTable.get(dto.getOrgCode()).setFuContactsElicited(dto.getFuContactsElicited());
//                hashTable.get(dto.getOrgCode()).setF0ContactsElicited(dto.getF0ContactsElicited());
//                hashTable.get(dto.getOrgCode()).setF15ContactsElicited(dto.getF15ContactsElicited());
//                hashTable.get(dto.getOrgCode()).setMuContactsElicited(dto.getMuContactsElicited());
//                hashTable.get(dto.getOrgCode()).setM0ContactsElicited(dto.getM0ContactsElicited());
//                hashTable.get(dto.getOrgCode()).setM15ContactsElicited(dto.getM15ContactsElicited());
//
//                hashTable.get(dto.getOrgCode()).setF1DocumentedNegatives(dto.getF1DocumentedNegatives());
//                hashTable.get(dto.getOrgCode()).setF5DocumentedNegatives(dto.getF5DocumentedNegatives());
//                hashTable.get(dto.getOrgCode()).setF10DocumentedNegatives(dto.getF10DocumentedNegatives());
//                hashTable.get(dto.getOrgCode()).setM1DocumentedNegatives(dto.getM1DocumentedNegatives());
//                hashTable.get(dto.getOrgCode()).setM5DocumentedNegatives(dto.getM5DocumentedNegatives());
//                hashTable.get(dto.getOrgCode()).setM10DocumentedNegatives(dto.getM10DocumentedNegatives());
//            } else {
//                dto.setModality("Facility Index Testing");
//                hashTable.put(dto.getOrgCode(), dto);
//            }
//        }
//
//
//        List<HTSIndexDto> ret = new ArrayList<>(hashTable.values());
//        return ret;
//    }

//    private List<HTSIndexDto> getDataHTSIndex(PreventionFilterDto filter) {
//        String whereClausePE = "";
//        if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
//            whereClausePE += " AND tbl.c1_org_id in (:listOrg) ";
//        }
//        if (filter.getFromDate() != null) {
//            whereClausePE += " AND tbl.c1 >=:fromDate ";
//        }
//        if (filter.getToDate() != null) {
//            whereClausePE += "  AND tbl.c1 <=:toDate ";
//        }
//        String sqlPE = "select * from (\n" +
//                "    select tou.code as orgCode , tou.name as  orgName, pro.name as provinceName, dis.name as districtName,\n" +
//                "        case when tbl.VCT != 0 and tbl.c131_result='answer2' and tbl.c7='answer4' then 'Community Index Testing' end as modality,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.c9 ='YES' and tbl.c4_dob is null,1,null)) as fuOffered,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.c9 ='YES' and (year(curdate())-year(tbl.c4_dob)<1),1,null)) as f0Offered,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.c9 ='YES' and (year(curdate())-year(tbl.c4_dob)>=1) and (year(curdate())-year(tbl.c4_dob)<5),1,null)) as f1Offered,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.c9 ='YES' and (year(curdate())-year(tbl.c4_dob)>=5) and (year(curdate())-year(tbl.c4_dob)<10),1,null)) as f5Offered,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.c9 ='YES' and (year(curdate())-year(tbl.c4_dob)>=10) and (year(curdate())-year(tbl.c4_dob)<15),1,null)) as f10Offered,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.c9 ='YES' and (year(curdate())-year(tbl.c4_dob)>=15) and (year(curdate())-year(tbl.c4_dob)<20),1,null)) as f15Offered,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.c9 ='YES' and (year(curdate())-year(tbl.c4_dob)>=20) and (year(curdate())-year(tbl.c4_dob)<25),1,null)) as f20Offered,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.c9 ='YES' and (year(curdate())-year(tbl.c4_dob)>=25) and (year(curdate())-year(tbl.c4_dob)<30),1,null)) as f25Offered,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.c9 ='YES' and (year(curdate())-year(tbl.c4_dob)>=30) and (year(curdate())-year(tbl.c4_dob)<35),1,null)) as f30Offered,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.c9 ='YES' and (year(curdate())-year(tbl.c4_dob)>=35) and (year(curdate())-year(tbl.c4_dob)<40),1,null)) as f35Offered,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.c9 ='YES' and (year(curdate())-year(tbl.c4_dob)>=40) and (year(curdate())-year(tbl.c4_dob)<45),1,null)) as f40Offered,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.c9 ='YES' and (year(curdate())-year(tbl.c4_dob)>=45) and (year(curdate())-year(tbl.c4_dob)<50),1,null)) as f45Offered,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.c9 ='YES' and (year(curdate())-year(tbl.c4_dob)>=50),1,null)) as f50Offered,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.c9 ='YES' and tbl.c4_dob is null,1,null)) as muOffered,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.c9 ='YES' and (year(curdate())-year(tbl.c4_dob)<1),1,null)) as m0Offered,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.c9 ='YES' and (year(curdate())-year(tbl.c4_dob)>=1) and (year(curdate())-year(tbl.c4_dob)<5),1,null)) as m1Offered,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.c9 ='YES' and (year(curdate())-year(tbl.c4_dob)>=5) and (year(curdate())-year(tbl.c4_dob)<10),1,null)) as m5Offered,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.c9 ='YES' and (year(curdate())-year(tbl.c4_dob)>=10) and (year(curdate())-year(tbl.c4_dob)<15),1,null)) as m10Offered,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.c9 ='YES' and (year(curdate())-year(tbl.c4_dob)>=15) and (year(curdate())-year(tbl.c4_dob)<20),1,null)) as m15Offered,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.c9 ='YES' and (year(curdate())-year(tbl.c4_dob)>=20) and (year(curdate())-year(tbl.c4_dob)<25),1,null)) as m20Offered,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.c9 ='YES' and (year(curdate())-year(tbl.c4_dob)>=25) and (year(curdate())-year(tbl.c4_dob)<30),1,null)) as m25Offered,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.c9 ='YES' and (year(curdate())-year(tbl.c4_dob)>=30) and (year(curdate())-year(tbl.c4_dob)<35),1,null)) as m30Offered,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.c9 ='YES' and (year(curdate())-year(tbl.c4_dob)>=35) and (year(curdate())-year(tbl.c4_dob)<40),1,null)) as m35Offered,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.c9 ='YES' and (year(curdate())-year(tbl.c4_dob)>=40) and (year(curdate())-year(tbl.c4_dob)<45),1,null)) as m40Offered,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.c9 ='YES' and (year(curdate())-year(tbl.c4_dob)>=45) and (year(curdate())-year(tbl.c4_dob)<50),1,null)) as m45Offered,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.c9 ='YES' and (year(curdate())-year(tbl.c4_dob)>=50),1,null)) as m50Offered,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.c9 ='YES' and tbl.c10='YES' and tbl.c4_dob is null,1,null)) as fuAccepted,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.c9 ='YES' and tbl.c10='YES' and (year(curdate())-year(tbl.c4_dob)<1),1,null)) as f0Accepted,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.c9 ='YES' and tbl.c10='YES' and (year(curdate())-year(tbl.c4_dob)>=1) and (year(curdate())-year(tbl.c4_dob)<5),1,null)) as f1Accepted,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.c9 ='YES' and tbl.c10='YES' and (year(curdate())-year(tbl.c4_dob)>=5) and (year(curdate())-year(tbl.c4_dob)<10),1,null)) as f5Accepted,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.c9 ='YES' and tbl.c10='YES' and (year(curdate())-year(tbl.c4_dob)>=10) and (year(curdate())-year(tbl.c4_dob)<15),1,null)) as f10Accepted,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.c9 ='YES' and tbl.c10='YES' and (year(curdate())-year(tbl.c4_dob)>=15) and (year(curdate())-year(tbl.c4_dob)<20),1,null)) as f15Accepted,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.c9 ='YES' and tbl.c10='YES' and (year(curdate())-year(tbl.c4_dob)>=20) and (year(curdate())-year(tbl.c4_dob)<25),1,null)) as f20Accepted,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.c9 ='YES' and tbl.c10='YES' and (year(curdate())-year(tbl.c4_dob)>=25) and (year(curdate())-year(tbl.c4_dob)<30),1,null)) as f25Accepted,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.c9 ='YES' and tbl.c10='YES' and (year(curdate())-year(tbl.c4_dob)>=30) and (year(curdate())-year(tbl.c4_dob)<35),1,null)) as f30Accepted,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.c9 ='YES' and tbl.c10='YES' and (year(curdate())-year(tbl.c4_dob)>=35) and (year(curdate())-year(tbl.c4_dob)<40),1,null)) as f35Accepted,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.c9 ='YES' and tbl.c10='YES' and (year(curdate())-year(tbl.c4_dob)>=40) and (year(curdate())-year(tbl.c4_dob)<45),1,null)) as f40Accepted,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.c9 ='YES' and tbl.c10='YES' and (year(curdate())-year(tbl.c4_dob)>=45) and (year(curdate())-year(tbl.c4_dob)<50),1,null)) as f45Accepted,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.c9 ='YES' and tbl.c10='YES' and (year(curdate())-year(tbl.c4_dob)>=50),1,null)) as f50Accepted,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.c9 ='YES' and tbl.c10='YES' and tbl.c4_dob is null,1,null)) as muAccepted,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.c9 ='YES' and tbl.c10='YES' and (year(curdate())-year(tbl.c4_dob)<1),1,null)) as m0Accepted,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.c9 ='YES' and tbl.c10='YES' and (year(curdate())-year(tbl.c4_dob)>=1) and (year(curdate())-year(tbl.c4_dob)<5),1,null)) as m1Accepted,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.c9 ='YES' and tbl.c10='YES' and (year(curdate())-year(tbl.c4_dob)>=5) and (year(curdate())-year(tbl.c4_dob)<10),1,null)) as m5Accepted,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.c9 ='YES' and tbl.c10='YES' and (year(curdate())-year(tbl.c4_dob)>=10) and (year(curdate())-year(tbl.c4_dob)<15),1,null)) as m10Accepted,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.c9 ='YES' and tbl.c10='YES' and (year(curdate())-year(tbl.c4_dob)>=15) and (year(curdate())-year(tbl.c4_dob)<20),1,null)) as m15Accepted,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.c9 ='YES' and tbl.c10='YES' and (year(curdate())-year(tbl.c4_dob)>=20) and (year(curdate())-year(tbl.c4_dob)<25),1,null)) as m20Accepted,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.c9 ='YES' and tbl.c10='YES' and (year(curdate())-year(tbl.c4_dob)>=25) and (year(curdate())-year(tbl.c4_dob)<30),1,null)) as m25Accepted,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.c9 ='YES' and tbl.c10='YES' and (year(curdate())-year(tbl.c4_dob)>=30) and (year(curdate())-year(tbl.c4_dob)<35),1,null)) as m30Accepted,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.c9 ='YES' and tbl.c10='YES' and (year(curdate())-year(tbl.c4_dob)>=35) and (year(curdate())-year(tbl.c4_dob)<40),1,null)) as m35Accepted,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.c9 ='YES' and tbl.c10='YES' and (year(curdate())-year(tbl.c4_dob)>=40) and (year(curdate())-year(tbl.c4_dob)<45),1,null)) as m40Accepted,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.c9 ='YES' and tbl.c10='YES' and (year(curdate())-year(tbl.c4_dob)>=45) and (year(curdate())-year(tbl.c4_dob)<50),1,null)) as m45Accepted,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.c9 ='YES' and tbl.c10='YES' and (year(curdate())-year(tbl.c4_dob)>=50),1,null)) as m50Accepted,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and tbl.c4_dob is null,1,null)) as fuContactsElicited,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and (year(curdate())-year(tbl.c4_dob)<15),1,null)) as f0ContactsElicited,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and (year(curdate())-year(tbl.c4_dob)>=15),1,null)) as f15ContactsElicited,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and tbl.c4_dob is null,1,null)) as muContactsElicited,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and (year(curdate())-year(tbl.c4_dob)<15),1,null)) as m0ContactsElicited,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and (year(curdate())-year(tbl.c4_dob)>=15),1,null)) as m15ContactsElicited,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and tbl.c8='answer1' and tbl.c4_dob is null,1,null)) as fuKnownPositives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and tbl.c8='answer1' and (year(curdate())-year(tbl.c4_dob)<1),1,null)) as f0KnownPositives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and tbl.c8='answer1' and (year(curdate())-year(tbl.c4_dob)>=1) and (year(curdate())-year(tbl.c4_dob)<5),1,null)) as f1KnownPositives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and tbl.c8='answer1' and (year(curdate())-year(tbl.c4_dob)>=5) and (year(curdate())-year(tbl.c4_dob)<10),1,null)) as f5KnownPositives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and tbl.c8='answer1' and (year(curdate())-year(tbl.c4_dob)>=10) and (year(curdate())-year(tbl.c4_dob)<15),1,null)) as f10KnownPositives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and tbl.c8='answer1' and (year(curdate())-year(tbl.c4_dob)>=15) and (year(curdate())-year(tbl.c4_dob)<20),1,null)) as f15KnownPositives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and tbl.c8='answer1' and (year(curdate())-year(tbl.c4_dob)>=20) and (year(curdate())-year(tbl.c4_dob)<25),1,null)) as f20KnownPositives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and tbl.c8='answer1' and (year(curdate())-year(tbl.c4_dob)>=25) and (year(curdate())-year(tbl.c4_dob)<30),1,null)) as f25KnownPositives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and tbl.c8='answer1' and (year(curdate())-year(tbl.c4_dob)>=30) and (year(curdate())-year(tbl.c4_dob)<35),1,null)) as f30KnownPositives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and tbl.c8='answer1' and (year(curdate())-year(tbl.c4_dob)>=35) and (year(curdate())-year(tbl.c4_dob)<40),1,null)) as f35KnownPositives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and tbl.c8='answer1' and (year(curdate())-year(tbl.c4_dob)>=40) and (year(curdate())-year(tbl.c4_dob)<45),1,null)) as f40KnownPositives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and tbl.c8='answer1' and (year(curdate())-year(tbl.c4_dob)>=45) and (year(curdate())-year(tbl.c4_dob)<50),1,null)) as f45KnownPositives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and tbl.c8='answer1' and (year(curdate())-year(tbl.c4_dob)>=50),1,null)) as f50KnownPositives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and tbl.c8='answer1' and tbl.c4_dob is null,1,null)) as muKnownPositives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and tbl.c8='answer1' and (year(curdate())-year(tbl.c4_dob)<1),1,null)) as m0KnownPositives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and tbl.c8='answer1' and (year(curdate())-year(tbl.c4_dob)>=1) and (year(curdate())-year(tbl.c4_dob)<5),1,null)) as m1KnownPositives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and tbl.c8='answer1' and (year(curdate())-year(tbl.c4_dob)>=5) and (year(curdate())-year(tbl.c4_dob)<10),1,null)) as m5KnownPositives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and tbl.c8='answer1' and (year(curdate())-year(tbl.c4_dob)>=10) and (year(curdate())-year(tbl.c4_dob)<15),1,null)) as m10KnownPositives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and tbl.c8='answer1' and (year(curdate())-year(tbl.c4_dob)>=15) and (year(curdate())-year(tbl.c4_dob)<20),1,null)) as m15KnownPositives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and tbl.c8='answer1' and (year(curdate())-year(tbl.c4_dob)>=20) and (year(curdate())-year(tbl.c4_dob)<25),1,null)) as m20KnownPositives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and tbl.c8='answer1' and (year(curdate())-year(tbl.c4_dob)>=25) and (year(curdate())-year(tbl.c4_dob)<30),1,null)) as m25KnownPositives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and tbl.c8='answer1' and (year(curdate())-year(tbl.c4_dob)>=30) and (year(curdate())-year(tbl.c4_dob)<35),1,null)) as m30KnownPositives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and tbl.c8='answer1' and (year(curdate())-year(tbl.c4_dob)>=35) and (year(curdate())-year(tbl.c4_dob)<40),1,null)) as m35KnownPositives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and tbl.c8='answer1' and (year(curdate())-year(tbl.c4_dob)>=40) and (year(curdate())-year(tbl.c4_dob)<45),1,null)) as m40KnownPositives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and tbl.c8='answer1' and (year(curdate())-year(tbl.c4_dob)>=45) and (year(curdate())-year(tbl.c4_dob)<50),1,null)) as m45KnownPositives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and tbl.c8='answer1' and (year(curdate())-year(tbl.c4_dob)>=50),1,null)) as m50KnownPositives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and tbl.c131_result='answer2' and (tbl.c16 ='answer1' or tbl.c16='answer3') and tbl.c4_dob is null,1,null)) as fuNewPositives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and tbl.c131_result='answer2' and (tbl.c16 ='answer1' or tbl.c16='answer3') and (year(curdate())-year(tbl.c4_dob)<1),1,null)) as f0NewPositives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and tbl.c131_result='answer2' and (tbl.c16 ='answer1' or tbl.c16='answer3') and (year(curdate())-year(tbl.c4_dob)>=1) and (year(curdate())-year(tbl.c4_dob)<5),1,null)) as f1NewPositives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and tbl.c131_result='answer2' and (tbl.c16 ='answer1' or tbl.c16='answer3') and (year(curdate())-year(tbl.c4_dob)>=5) and (year(curdate())-year(tbl.c4_dob)<10),1,null)) as f5NewPositives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and tbl.c131_result='answer2' and (tbl.c16 ='answer1' or tbl.c16='answer3') and (year(curdate())-year(tbl.c4_dob)>=10) and (year(curdate())-year(tbl.c4_dob)<15),1,null)) as f10NewPositives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and tbl.c131_result='answer2' and (tbl.c16 ='answer1' or tbl.c16='answer3') and (year(curdate())-year(tbl.c4_dob)>=15) and (year(curdate())-year(tbl.c4_dob)<20),1,null)) as f15NewPositives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and tbl.c131_result='answer2' and (tbl.c16 ='answer1' or tbl.c16='answer3') and (year(curdate())-year(tbl.c4_dob)>=20) and (year(curdate())-year(tbl.c4_dob)<25),1,null)) as f20NewPositives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and tbl.c131_result='answer2' and (tbl.c16 ='answer1' or tbl.c16='answer3') and (year(curdate())-year(tbl.c4_dob)>=25) and (year(curdate())-year(tbl.c4_dob)<30),1,null)) as f25NewPositives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and tbl.c131_result='answer2' and (tbl.c16 ='answer1' or tbl.c16='answer3') and (year(curdate())-year(tbl.c4_dob)>=30) and (year(curdate())-year(tbl.c4_dob)<35),1,null)) as f30NewPositives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and tbl.c131_result='answer2' and (tbl.c16 ='answer1' or tbl.c16='answer3') and (year(curdate())-year(tbl.c4_dob)>=35) and (year(curdate())-year(tbl.c4_dob)<40),1,null)) as f35NewPositives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and tbl.c131_result='answer2' and (tbl.c16 ='answer1' or tbl.c16='answer3') and (year(curdate())-year(tbl.c4_dob)>=40) and (year(curdate())-year(tbl.c4_dob)<45),1,null)) as f40NewPositives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and tbl.c131_result='answer2' and (tbl.c16 ='answer1' or tbl.c16='answer3') and (year(curdate())-year(tbl.c4_dob)>=45) and (year(curdate())-year(tbl.c4_dob)<50),1,null)) as f45NewPositives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and tbl.c131_result='answer2' and (tbl.c16 ='answer1' or tbl.c16='answer3') and (year(curdate())-year(tbl.c4_dob)>=50),1,null)) as f50NewPositives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and tbl.c131_result='answer2' and (tbl.c16 ='answer1' or tbl.c16='answer3') and tbl.c4_dob is null,1,null)) as muNewPositives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and tbl.c131_result='answer2' and (tbl.c16 ='answer1' or tbl.c16='answer3') and (year(curdate())-year(tbl.c4_dob)<1),1,null)) as m0NewPositives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and tbl.c131_result='answer2' and (tbl.c16 ='answer1' or tbl.c16='answer3') and (year(curdate())-year(tbl.c4_dob)>=1) and (year(curdate())-year(tbl.c4_dob)<5),1,null)) as m1NewPositives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and tbl.c131_result='answer2' and (tbl.c16 ='answer1' or tbl.c16='answer3') and (year(curdate())-year(tbl.c4_dob)>=5) and (year(curdate())-year(tbl.c4_dob)<10),1,null)) as m5NewPositives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and tbl.c131_result='answer2' and (tbl.c16 ='answer1' or tbl.c16='answer3') and (year(curdate())-year(tbl.c4_dob)>=10) and (year(curdate())-year(tbl.c4_dob)<15),1,null)) as m10NewPositives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and tbl.c131_result='answer2' and (tbl.c16 ='answer1' or tbl.c16='answer3') and (year(curdate())-year(tbl.c4_dob)>=15) and (year(curdate())-year(tbl.c4_dob)<20),1,null)) as m15NewPositives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and tbl.c131_result='answer2' and (tbl.c16 ='answer1' or tbl.c16='answer3') and (year(curdate())-year(tbl.c4_dob)>=20) and (year(curdate())-year(tbl.c4_dob)<25),1,null)) as m20NewPositives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and tbl.c131_result='answer2' and (tbl.c16 ='answer1' or tbl.c16='answer3') and (year(curdate())-year(tbl.c4_dob)>=25) and (year(curdate())-year(tbl.c4_dob)<30),1,null)) as m25NewPositives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and tbl.c131_result='answer2' and (tbl.c16 ='answer1' or tbl.c16='answer3') and (year(curdate())-year(tbl.c4_dob)>=30) and (year(curdate())-year(tbl.c4_dob)<35),1,null)) as m30NewPositives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and tbl.c131_result='answer2' and (tbl.c16 ='answer1' or tbl.c16='answer3') and (year(curdate())-year(tbl.c4_dob)>=35) and (year(curdate())-year(tbl.c4_dob)<40),1,null)) as m35NewPositives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and tbl.c131_result='answer2' and (tbl.c16 ='answer1' or tbl.c16='answer3') and (year(curdate())-year(tbl.c4_dob)>=40) and (year(curdate())-year(tbl.c4_dob)<45),1,null)) as m40NewPositives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and tbl.c131_result='answer2' and (tbl.c16 ='answer1' or tbl.c16='answer3') and (year(curdate())-year(tbl.c4_dob)>=45) and (year(curdate())-year(tbl.c4_dob)<50),1,null)) as m45NewPositives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and tbl.c131_result='answer2' and (tbl.c16 ='answer1' or tbl.c16='answer3') and (year(curdate())-year(tbl.c4_dob)>=50),1,null)) as m50NewPositives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and ((tbl.VCT != 0 and tbl.c131_result='answer2' and tbl.c7='answer4') or (tbl.c131_result='answer1' or (tbl.c131_result is null and tbl.c13 = 'answer2'))) and tbl.c4_dob is null,1,null)) as fuNewNegatives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and ((tbl.VCT != 0 and tbl.c131_result='answer2' and tbl.c7='answer4') or (tbl.c131_result='answer1' or (tbl.c131_result is null and tbl.c13 = 'answer2'))) and (year(curdate())-year(tbl.c4_dob)<1),1,null)) as f0NewNegatives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and ((tbl.VCT != 0 and tbl.c131_result='answer2' and tbl.c7='answer4') or (tbl.c131_result='answer1' or (tbl.c131_result is null and tbl.c13 = 'answer2'))) and (year(curdate())-year(tbl.c4_dob)>=1) and (year(curdate())-year(tbl.c4_dob)<5),1,null)) as f1NewNegatives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and ((tbl.VCT != 0 and tbl.c131_result='answer2' and tbl.c7='answer4') or (tbl.c131_result='answer1' or (tbl.c131_result is null and tbl.c13 = 'answer2'))) and (year(curdate())-year(tbl.c4_dob)>=5) and (year(curdate())-year(tbl.c4_dob)<10),1,null)) as f5NewNegatives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and ((tbl.VCT != 0 and tbl.c131_result='answer2' and tbl.c7='answer4') or (tbl.c131_result='answer1' or (tbl.c131_result is null and tbl.c13 = 'answer2'))) and (year(curdate())-year(tbl.c4_dob)>=10) and (year(curdate())-year(tbl.c4_dob)<15),1,null)) as f10NewNegatives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and ((tbl.VCT != 0 and tbl.c131_result='answer2' and tbl.c7='answer4') or (tbl.c131_result='answer1' or (tbl.c131_result is null and tbl.c13 = 'answer2'))) and (year(curdate())-year(tbl.c4_dob)>=15) and (year(curdate())-year(tbl.c4_dob)<20),1,null)) as f15NewNegatives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and ((tbl.VCT != 0 and tbl.c131_result='answer2' and tbl.c7='answer4') or (tbl.c131_result='answer1' or (tbl.c131_result is null and tbl.c13 = 'answer2'))) and (year(curdate())-year(tbl.c4_dob)>=20) and (year(curdate())-year(tbl.c4_dob)<25),1,null)) as f20NewNegatives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and ((tbl.VCT != 0 and tbl.c131_result='answer2' and tbl.c7='answer4') or (tbl.c131_result='answer1' or (tbl.c131_result is null and tbl.c13 = 'answer2'))) and (year(curdate())-year(tbl.c4_dob)>=25) and (year(curdate())-year(tbl.c4_dob)<30),1,null)) as f25NewNegatives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and ((tbl.VCT != 0 and tbl.c131_result='answer2' and tbl.c7='answer4') or (tbl.c131_result='answer1' or (tbl.c131_result is null and tbl.c13 = 'answer2'))) and (year(curdate())-year(tbl.c4_dob)>=30) and (year(curdate())-year(tbl.c4_dob)<35),1,null)) as f30NewNegatives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and ((tbl.VCT != 0 and tbl.c131_result='answer2' and tbl.c7='answer4') or (tbl.c131_result='answer1' or (tbl.c131_result is null and tbl.c13 = 'answer2'))) and (year(curdate())-year(tbl.c4_dob)>=35) and (year(curdate())-year(tbl.c4_dob)<40),1,null)) as f35NewNegatives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and ((tbl.VCT != 0 and tbl.c131_result='answer2' and tbl.c7='answer4') or (tbl.c131_result='answer1' or (tbl.c131_result is null and tbl.c13 = 'answer2'))) and (year(curdate())-year(tbl.c4_dob)>=40) and (year(curdate())-year(tbl.c4_dob)<45),1,null)) as f40NewNegatives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and ((tbl.VCT != 0 and tbl.c131_result='answer2' and tbl.c7='answer4') or (tbl.c131_result='answer1' or (tbl.c131_result is null and tbl.c13 = 'answer2'))) and (year(curdate())-year(tbl.c4_dob)>=45) and (year(curdate())-year(tbl.c4_dob)<50),1,null)) as f45NewNegatives,\n" +
//                "           count(if(tbl.c3 = 'FEMALE' and tbl.parent_id is not null and ((tbl.VCT != 0 and tbl.c131_result='answer2' and tbl.c7='answer4') or (tbl.c131_result='answer1' or (tbl.c131_result is null and tbl.c13 = 'answer2'))) and (year(curdate())-year(tbl.c4_dob)>=50),1,null)) as f50NewNegatives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and ((tbl.VCT != 0 and tbl.c131_result='answer2' and tbl.c7='answer4') or (tbl.c131_result='answer1' or (tbl.c131_result is null and tbl.c13 = 'answer2'))) and tbl.c4_dob is null,1,null)) as muNewNegatives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and ((tbl.VCT != 0 and tbl.c131_result='answer2' and tbl.c7='answer4') or (tbl.c131_result='answer1' or (tbl.c131_result is null and tbl.c13 = 'answer2'))) and (year(curdate())-year(tbl.c4_dob)<1),1,null)) as m0NewNegatives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and ((tbl.VCT != 0 and tbl.c131_result='answer2' and tbl.c7='answer4') or (tbl.c131_result='answer1' or (tbl.c131_result is null and tbl.c13 = 'answer2'))) and (year(curdate())-year(tbl.c4_dob)>=1) and (year(curdate())-year(tbl.c4_dob)<5),1,null)) as m1NewNegatives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and ((tbl.VCT != 0 and tbl.c131_result='answer2' and tbl.c7='answer4') or (tbl.c131_result='answer1' or (tbl.c131_result is null and tbl.c13 = 'answer2'))) and (year(curdate())-year(tbl.c4_dob)>=5) and (year(curdate())-year(tbl.c4_dob)<10),1,null)) as m5NewNegatives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and ((tbl.VCT != 0 and tbl.c131_result='answer2' and tbl.c7='answer4') or (tbl.c131_result='answer1' or (tbl.c131_result is null and tbl.c13 = 'answer2'))) and (year(curdate())-year(tbl.c4_dob)>=10) and (year(curdate())-year(tbl.c4_dob)<15),1,null)) as m10NewNegatives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and ((tbl.VCT != 0 and tbl.c131_result='answer2' and tbl.c7='answer4') or (tbl.c131_result='answer1' or (tbl.c131_result is null and tbl.c13 = 'answer2'))) and (year(curdate())-year(tbl.c4_dob)>=15) and (year(curdate())-year(tbl.c4_dob)<20),1,null)) as m15NewNegatives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and ((tbl.VCT != 0 and tbl.c131_result='answer2' and tbl.c7='answer4') or (tbl.c131_result='answer1' or (tbl.c131_result is null and tbl.c13 = 'answer2'))) and (year(curdate())-year(tbl.c4_dob)>=20) and (year(curdate())-year(tbl.c4_dob)<25),1,null)) as m20NewNegatives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and ((tbl.VCT != 0 and tbl.c131_result='answer2' and tbl.c7='answer4') or (tbl.c131_result='answer1' or (tbl.c131_result is null and tbl.c13 = 'answer2'))) and (year(curdate())-year(tbl.c4_dob)>=25) and (year(curdate())-year(tbl.c4_dob)<30),1,null)) as m25NewNegatives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and ((tbl.VCT != 0 and tbl.c131_result='answer2' and tbl.c7='answer4') or (tbl.c131_result='answer1' or (tbl.c131_result is null and tbl.c13 = 'answer2'))) and (year(curdate())-year(tbl.c4_dob)>=30) and (year(curdate())-year(tbl.c4_dob)<35),1,null)) as m30NewNegatives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and ((tbl.VCT != 0 and tbl.c131_result='answer2' and tbl.c7='answer4') or (tbl.c131_result='answer1' or (tbl.c131_result is null and tbl.c13 = 'answer2'))) and (year(curdate())-year(tbl.c4_dob)>=35) and (year(curdate())-year(tbl.c4_dob)<40),1,null)) as m35NewNegatives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and ((tbl.VCT != 0 and tbl.c131_result='answer2' and tbl.c7='answer4') or (tbl.c131_result='answer1' or (tbl.c131_result is null and tbl.c13 = 'answer2'))) and (year(curdate())-year(tbl.c4_dob)>=40) and (year(curdate())-year(tbl.c4_dob)<45),1,null)) as m40NewNegatives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and ((tbl.VCT != 0 and tbl.c131_result='answer2' and tbl.c7='answer4') or (tbl.c131_result='answer1' or (tbl.c131_result is null and tbl.c13 = 'answer2'))) and (year(curdate())-year(tbl.c4_dob)>=45) and (year(curdate())-year(tbl.c4_dob)<50),1,null)) as m45NewNegatives,\n" +
//                "           count(if(tbl.c3 = 'MALE' and tbl.parent_id is not null and ((tbl.VCT != 0 and tbl.c131_result='answer2' and tbl.c7='answer4') or (tbl.c131_result='answer1' or (tbl.c131_result is null and tbl.c13 = 'answer2'))) and (year(curdate())-year(tbl.c4_dob)>=50),1,null)) as m50NewNegatives\n" +
//                " from (select tpcrg.val,tb1.VCT, tpc.* from tbl_pe_case_risk_group tpcrg\n" +
//                "    inner join tbl_pe_case tpc on tpcrg.pe_case_id = tpc.id\n" +
//                "    inner join (select thcrg.pe_case_id, count(if((thcrg.val='answer5' or thcrg.val='answer6'),1,null))\n" +
//                "    as VCT from tbl_pe_case_risk_group thcrg\n" +
//                "                group by  thcrg.pe_case_id) tb1 on tb1.pe_case_id = tpc.id\n" +
//                "      where tpcrg.is_main_risk = 1 ) as tbl\n" +
//                " inner join tbl_organization_unit tou on tbl.c1_org_id = tou.id\n" +
//                " inner join tbl_location tl on tou.address_id = tl.id\n" +
//                " inner join tbl_admin_unit pro on tl.province_id = pro.id\n" +
//                " inner join tbl_admin_unit dis on tl.district_id = dis.id\n" +
//                " where 1=1 " + whereClausePE + " group by orgCode,orgName,provinceName,districtName,modality) as tb where tb.modality is not null;";
//        org.hibernate.Query queryPE = manager.unwrap(Session.class).createSQLQuery(sqlPE).setResultTransformer(new AliasToBeanResultTransformer(HTSIndexDto.class));
//        if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
//            queryPE.setParameterList("listOrg", filter.getOrgIds());
//        }
//        if (filter.getFromDate() != null) {
//            queryPE.setParameter("fromDate", Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
//        }
//        if (filter.getToDate() != null) {
//            queryPE.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
//        }
//        List<HTSIndexDto> retPE = queryPE.list();
//        retPE.addAll(this.getDataHTSIndexModalityFacility2(filter));
//        return retPE;
//    }

//    private List<HTSTSTDto> getDataHTSTST(PreventionFilterDto filter) {
//        String whereClause = "";
//        if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
//            whereClause += " AND tbl.c2_org_id in (:listOrg) ";
//        }
//        if (filter.getFromDate() != null) {
//            whereClause += " AND tbl.c4_counselling_date >=:fromDate ";
//        }
//        if (filter.getToDate() != null) {
//            whereClause += "  AND tbl.c4_counselling_date <=:toDate ";
//        }
//        String sql = "select *\n" +
//                "from (\n" +
//                "         select tou.code                                                                                  as orgCode,\n" +
//                "                tou.name                                                                                  as orgName,\n" +
//                "                pro.name                                                                                  as provinceName,\n" +
//                "                dis.name                                                                                  as districtName,\n" +
//                "                case\n" +
//                "                    when tbl.VCT = 0 and tbl.c10 != 'answer2' and (tbl.c5 = 'answer1' or tbl.c5 = 'answer2')\n" +
//                "                        then 'VCT - Integrated'\n" +
//                "                    when tbl.c5 = 'answer3' then 'Facility SNS' end                                       as modality,\n" +
//                "                count(if(tbl.val = 'answer1' and tbl.c14 = 'answer2' and tbl.c15 = 'YES' and\n" +
//                "                         not ((tbl.c24 is not null and tbl.c24 = 'answer2') or\n" +
//                "                              (tbl.c11c is not null and tbl.c11c = 'YES' and tbl.c11b is not null and\n" +
//                "                               tbl.c11b != 'answer1')), 1,\n" +
//                "                         null))                                                                           as pPWID,\n" +
//                "                count(if(tbl.val = 'answer1' and (tbl.c14 != 'answer2' or tbl.c14 is null) and tbl.c15 = 'YES', 1,\n" +
//                "                         null))                                                                           as nPWID,\n" +
//                "                count(if(tbl.val = 'answer2' and tbl.c14 = 'answer2' and tbl.c15 = 'YES' and\n" +
//                "                         not ((tbl.c24 is not null and tbl.c24 = 'answer2') or\n" +
//                "                              (tbl.c11c is not null and tbl.c11c = 'YES' and tbl.c11b is not null and\n" +
//                "                               tbl.c11b != 'answer1')), 1,\n" +
//                "                         null))                                                                           as pMSM,\n" +
//                "                count(if(tbl.val = 'answer2' and (tbl.c14 != 'answer2' or tbl.c14 is null) and tbl.c15 = 'YES', 1,\n" +
//                "                         null))                                                                           as nMSM,\n" +
//                "                count(if(tbl.val = 'answer4' and tbl.c14 = 'answer2' and tbl.c15 = 'YES' and\n" +
//                "                         not ((tbl.c24 is not null and tbl.c24 = 'answer2') or\n" +
//                "                              (tbl.c11c is not null and tbl.c11c = 'YES' and tbl.c11b is not null and\n" +
//                "                               tbl.c11b != 'answer1')), 1,\n" +
//                "                         null))                                                                           as pTG,\n" +
//                "                count(if(tbl.val = 'answer4' and (tbl.c14 != 'answer2' or tbl.c14 is null) and tbl.c15 = 'YES', 1,\n" +
//                "                         null))                                                                           as nTG,\n" +
//                "                count(if((tbl.val = 'answer3' or tbl.val = 'answer9') and tbl.c14 = 'answer2' and tbl.c15 = 'YES' and\n" +
//                "                         not ((tbl.c24 is not null and tbl.c24 = 'answer2') or\n" +
//                "                              (tbl.c11c is not null and tbl.c11c = 'YES' and tbl.c11b is not null and\n" +
//                "                               tbl.c11b != 'answer1')), 1, null))                                         as pFSW,\n" +
//                "                count(if((tbl.val = 'answer3' or tbl.val = 'answer9') and (tbl.c14 != 'answer2' or tbl.c14 is null) and\n" +
//                "                         tbl.c15 = 'YES', 1, null))                                                       as nFSW,\n" +
//                "                count(if(tbl.val = 'answer14' and tbl.c14 = 'answer2' and tbl.c15 = 'YES' and\n" +
//                "                         not ((tbl.c24 is not null and tbl.c24 = 'answer2') or\n" +
//                "                              (tbl.c11c is not null and tbl.c11c = 'YES' and tbl.c11b is not null and\n" +
//                "                               tbl.c11b != 'answer1')), 1,\n" +
//                "                         null))                                                                           as pOther,\n" +
//                "                count(if(tbl.val = 'answer14' and (tbl.c14 != 'answer2' or tbl.c14 is null) and tbl.c15 = 'YES', 1,\n" +
//                "                         null))                                                                           as nOther,\n" +
//                "                count(if(tbl.c7_gender = 'FEMALE' and tbl.c8_dob is null and tbl.c14 = 'answer2' and tbl.c15 = 'YES' and\n" +
//                "                         not ((tbl.c24 is not null and tbl.c24 = 'answer2') or\n" +
//                "                              (tbl.c11c is not null and tbl.c11c = 'YES' and tbl.c11b is not null and\n" +
//                "                               tbl.c11b != 'answer1')), 1, null))                                         as pFU,\n" +
//                "                count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) < 1) and\n" +
//                "                         tbl.c14 = 'answer2' and tbl.c15 = 'YES' and not\n" +
//                "                             ((tbl.c24 is not null and tbl.c24 = 'answer2') or\n" +
//                "                              (tbl.c11c is not null and tbl.c11c = 'YES' and tbl.c11b is not null and\n" +
//                "                               tbl.c11b != 'answer1')), 1, null))                                         as pF0,\n" +
//                "                count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 1) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 5) and tbl.c14 = 'answer2' and tbl.c15 = 'YES'\n" +
//                "                             and not\n" +
//                "                             ((tbl.c24 is not null and tbl.c24 = 'answer2') or\n" +
//                "                              (tbl.c11c is not null and tbl.c11c = 'YES' and tbl.c11b is not null and\n" +
//                "                               tbl.c11b != 'answer1')), 1, null))                                         as pF1,\n" +
//                "                count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 5) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 10) and tbl.c14 = 'answer2' and tbl.c15 = 'YES'\n" +
//                "                             and not\n" +
//                "                             ((tbl.c24 is not null and tbl.c24 = 'answer2') or\n" +
//                "                              (tbl.c11c is not null and tbl.c11c = 'YES' and tbl.c11b is not null and\n" +
//                "                               tbl.c11b != 'answer1')), 1, null))                                         as pF5,\n" +
//                "                count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 10) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 15) and tbl.c14 = 'answer2' and tbl.c15 = 'YES'\n" +
//                "                             and not ((tbl.c24 is not null and tbl.c24 = 'answer2') or\n" +
//                "                                      (tbl.c11c is not null and tbl.c11c = 'YES' and tbl.c11b is not null and\n" +
//                "                                       tbl.c11b != 'answer1')), 1, null))                                 as pF10,\n" +
//                "                count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 15) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 20) and tbl.c14 = 'answer2' and tbl.c15 = 'YES'\n" +
//                "                             and not ((tbl.c24 is not null and tbl.c24 = 'answer2') or\n" +
//                "                                      (tbl.c11c is not null and tbl.c11c = 'YES' and tbl.c11b is not null and\n" +
//                "                                       tbl.c11b != 'answer1')), 1, null))                                 as pF15,\n" +
//                "                count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 20) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 25) and tbl.c14 = 'answer2' and tbl.c15 = 'YES'\n" +
//                "                             and not ((tbl.c24 is not null and tbl.c24 = 'answer2') or\n" +
//                "                                      (tbl.c11c is not null and tbl.c11c = 'YES' and tbl.c11b is not null and\n" +
//                "                                       tbl.c11b != 'answer1')), 1, null))                                 as pF20,\n" +
//                "                count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 25) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 30) and tbl.c14 = 'answer2' and tbl.c15 = 'YES'\n" +
//                "                             and not ((tbl.c24 is not null and tbl.c24 = 'answer2') or\n" +
//                "                                      (tbl.c11c is not null and tbl.c11c = 'YES' and tbl.c11b is not null and\n" +
//                "                                       tbl.c11b != 'answer1')), 1, null))                                 as pF25,\n" +
//                "                count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 30) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 35) and tbl.c14 = 'answer2' and tbl.c15 = 'YES'\n" +
//                "                             and not ((tbl.c24 is not null and tbl.c24 = 'answer2') or\n" +
//                "                                      (tbl.c11c is not null and tbl.c11c = 'YES' and tbl.c11b is not null and\n" +
//                "                                       tbl.c11b != 'answer1')), 1, null))                                 as pF30,\n" +
//                "                count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 35) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 40) and tbl.c14 = 'answer2' and tbl.c15 = 'YES'\n" +
//                "                             and not ((tbl.c24 is not null and tbl.c24 = 'answer2') or\n" +
//                "                                      (tbl.c11c is not null and tbl.c11c = 'YES' and tbl.c11b is not null and\n" +
//                "                                       tbl.c11b != 'answer1')), 1, null))                                 as pF35,\n" +
//                "                count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 40) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 45) and tbl.c14 = 'answer2' and tbl.c15 = 'YES'\n" +
//                "                             and not ((tbl.c24 is not null and tbl.c24 = 'answer2') or\n" +
//                "                                      (tbl.c11c is not null and tbl.c11c = 'YES' and tbl.c11b is not null and\n" +
//                "                                       tbl.c11b != 'answer1')), 1, null))                                 as pF40,\n" +
//                "                count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 45) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 50) and tbl.c14 = 'answer2' and tbl.c15 = 'YES'\n" +
//                "                             and not ((tbl.c24 is not null and tbl.c24 = 'answer2') or\n" +
//                "                                      (tbl.c11c is not null and tbl.c11c = 'YES' and tbl.c11b is not null and\n" +
//                "                                       tbl.c11b != 'answer1')), 1, null))                                 as pF45,\n" +
//                "                count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 50) and\n" +
//                "                         tbl.c14 = 'answer2' and tbl.c15 = 'YES'\n" +
//                "                             and not ((tbl.c24 is not null and tbl.c24 = 'answer2') or\n" +
//                "                                      (tbl.c11c is not null and tbl.c11c = 'YES' and tbl.c11b is not null and\n" +
//                "                                       tbl.c11b != 'answer1')), 1, null))                                 as pF50,\n" +
//                "                count(if(tbl.c7_gender = 'MALE' and tbl.c8_dob is null and tbl.c14 = 'answer2' and tbl.c15 = 'YES' and\n" +
//                "                         not ((tbl.c24 is not null and tbl.c24 = 'answer2') or\n" +
//                "                              (tbl.c11c is not null and tbl.c11c = 'YES' and tbl.c11b is not null and\n" +
//                "                               tbl.c11b != 'answer1')), 1, null))                                         as pMU,\n" +
//                "                count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) < 1) and tbl.c14 = 'answer2' and\n" +
//                "                         tbl.c15 = 'YES' and not ((tbl.c24 is not null and tbl.c24 = 'answer2') or\n" +
//                "                                                  (tbl.c11c is not null and tbl.c11c = 'YES' and\n" +
//                "                                                   tbl.c11b is not null and\n" +
//                "                                                   tbl.c11b != 'answer1')), 1, null))                     as pM0,\n" +
//                "                count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 1) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 4) and tbl.c14 = 'answer2' and tbl.c15 = 'YES'\n" +
//                "                             and not ((tbl.c24 is not null and tbl.c24 = 'answer2') or\n" +
//                "                                      (tbl.c11c is not null and tbl.c11c = 'YES' and tbl.c11b is not null and\n" +
//                "                                       tbl.c11b != 'answer1')), 1, null))                                 as pM1,\n" +
//                "                count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 5) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 10) and tbl.c14 = 'answer2' and tbl.c15 = 'YES'\n" +
//                "                             and not ((tbl.c24 is not null and tbl.c24 = 'answer2') or\n" +
//                "                                      (tbl.c11c is not null and tbl.c11c = 'YES' and tbl.c11b is not null and\n" +
//                "                                       tbl.c11b != 'answer1')), 1, null))                                 as pM5,\n" +
//                "                count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 10) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 15) and tbl.c14 = 'answer2' and tbl.c15 = 'YES'\n" +
//                "                             and not ((tbl.c24 is not null and tbl.c24 = 'answer2') or\n" +
//                "                                      (tbl.c11c is not null and tbl.c11c = 'YES' and tbl.c11b is not null and\n" +
//                "                                       tbl.c11b != 'answer1')), 1, null))                                 as pM10,\n" +
//                "                count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 15) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 20) and tbl.c14 = 'answer2' and tbl.c15 = 'YES'\n" +
//                "                             and not ((tbl.c24 is not null and tbl.c24 = 'answer2') or\n" +
//                "                                      (tbl.c11c is not null and tbl.c11c = 'YES' and tbl.c11b is not null and\n" +
//                "                                       tbl.c11b != 'answer1')), 1, null))                                 as pM15,\n" +
//                "                count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 20) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 25) and tbl.c14 = 'answer2' and tbl.c15 = 'YES'\n" +
//                "                             and not\n" +
//                "                             ((tbl.c24 is not null and tbl.c24 = 'answer2') or\n" +
//                "                              (tbl.c11c is not null and tbl.c11c = 'YES' and tbl.c11b is not null and\n" +
//                "                               tbl.c11b != 'answer1')), 1, null))                                         as pM20,\n" +
//                "                count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 25) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 30) and tbl.c14 = 'answer2' and tbl.c15 = 'YES'\n" +
//                "                             and not ((tbl.c24 is not null and tbl.c24 = 'answer2') or\n" +
//                "                                      (tbl.c11c is not null and tbl.c11c = 'YES' and tbl.c11b is not null and\n" +
//                "                                       tbl.c11b != 'answer1')), 1, null))                                 as pM25,\n" +
//                "                count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 30) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 35) and tbl.c14 = 'answer2' and tbl.c15 = 'YES'\n" +
//                "                             and not ((tbl.c24 is not null and tbl.c24 = 'answer2') or\n" +
//                "                                      (tbl.c11c is not null and tbl.c11c = 'YES' and tbl.c11b is not null and\n" +
//                "                                       tbl.c11b != 'answer1')), 1, null))                                 as pM30,\n" +
//                "                count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 35) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 40) and tbl.c14 = 'answer2' and tbl.c15 = 'YES'\n" +
//                "                             and not ((tbl.c24 is not null and tbl.c24 = 'answer2') or\n" +
//                "                                      (tbl.c11c is not null and tbl.c11c = 'YES' and tbl.c11b is not null and\n" +
//                "                                       tbl.c11b != 'answer1')), 1, null))                                 as pM35,\n" +
//                "                count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 40) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 45) and tbl.c14 = 'answer2' and tbl.c15 = 'YES'\n" +
//                "                             and not ((tbl.c24 is not null and tbl.c24 = 'answer2') or\n" +
//                "                                      (tbl.c11c is not null and tbl.c11c = 'YES' and tbl.c11b is not null and\n" +
//                "                                       tbl.c11b != 'answer1')), 1, null))                                 as pM40,\n" +
//                "                count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 45) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 50) and tbl.c14 = 'answer2' and tbl.c15 = 'YES'\n" +
//                "                             and not ((tbl.c24 is not null and tbl.c24 = 'answer2') or\n" +
//                "                                      (tbl.c11c is not null and tbl.c11c = 'YES' and tbl.c11b is not null and\n" +
//                "                                       tbl.c11b != 'answer1')), 1, null))                                 as pM45,\n" +
//                "                count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 50) and\n" +
//                "                         tbl.c14 = 'answer2' and tbl.c15 = 'YES' and not\n" +
//                "                             ((tbl.c24 is not null and tbl.c24 = 'answer2') or\n" +
//                "                              (tbl.c11c is not null and tbl.c11c = 'YES' and tbl.c11b is not null and\n" +
//                "                               tbl.c11b != 'answer1')), 1, null))                                         as pM50,\n" +
//                "                count(if(tbl.c7_gender = 'FEMALE' and tbl.c8_dob is null and\n" +
//                "                         (tbl.c14 != 'answer2' or tbl.c14 is null) and tbl.c15 = 'YES', 1, null))         as nFU,\n" +
//                "                count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) < 1) and\n" +
//                "                         (tbl.c14 != 'answer2' or tbl.c14 is null) and tbl.c15 = 'YES', 1, null))         as nF0,\n" +
//                "                count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 1) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 4)\n" +
//                "                             and (tbl.c14 != 'answer2' or tbl.c14 is null) and tbl.c15 = 'YES', 1, null)) as nF1,\n" +
//                "                count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 5) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 10)\n" +
//                "                             and (tbl.c14 != 'answer2' or tbl.c14 is null) and tbl.c15 = 'YES', 1, null)) as nF5,\n" +
//                "                count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 10) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 15)\n" +
//                "                             and (tbl.c14 != 'answer2' or tbl.c14 is null) and tbl.c15 = 'YES', 1, null)) as nF10,\n" +
//                "                count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 15) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 20)\n" +
//                "                             and (tbl.c14 != 'answer2' or tbl.c14 is null) and tbl.c15 = 'YES', 1, null)) as nF15,\n" +
//                "                count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 20) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 25)\n" +
//                "                             and (tbl.c14 != 'answer2' or tbl.c14 is null) and tbl.c15 = 'YES', 1, null)) as nF20,\n" +
//                "                count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 25) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 30)\n" +
//                "                             and (tbl.c14 != 'answer2' or tbl.c14 is null) and tbl.c15 = 'YES', 1, null)) as nF25,\n" +
//                "                count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 30) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 35)\n" +
//                "                             and (tbl.c14 != 'answer2' or tbl.c14 is null) and tbl.c15 = 'YES', 1, null)) as nF30,\n" +
//                "                count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 35) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 40)\n" +
//                "                             and (tbl.c14 != 'answer2' or tbl.c14 is null) and tbl.c15 = 'YES', 1, null)) as nF35,\n" +
//                "                count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 40) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 45)\n" +
//                "                             and (tbl.c14 != 'answer2' or tbl.c14 is null) and tbl.c15 = 'YES', 1, null)) as nF40,\n" +
//                "                count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 45) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 50)\n" +
//                "                             and (tbl.c14 != 'answer2' or tbl.c14 is null) and tbl.c15 = 'YES', 1, null)) as nF45,\n" +
//                "                count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 50) and\n" +
//                "                         (tbl.c14 != 'answer2' or tbl.c14 is null) and tbl.c15 = 'YES', 1, null))         as nF50,\n" +
//                "                count(if(tbl.c7_gender = 'MALE' and tbl.c8_dob is null and (tbl.c14 != 'answer2' or tbl.c14 is null) and\n" +
//                "                         tbl.c15 = 'YES', 1, null))                                                       as nMU,\n" +
//                "                count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) < 1) and\n" +
//                "                         (tbl.c14 != 'answer2' or tbl.c14 is null) and tbl.c15 = 'YES', 1, null))         as nM0,\n" +
//                "                count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 1) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 4)\n" +
//                "                             and (tbl.c14 != 'answer2' or tbl.c14 is null) and tbl.c15 = 'YES', 1, null)) as nM1,\n" +
//                "                count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 5) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 10)\n" +
//                "                             and (tbl.c14 != 'answer2' or tbl.c14 is null) and tbl.c15 = 'YES', 1, null)) as nM5,\n" +
//                "                count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 10) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 15)\n" +
//                "                             and (tbl.c14 != 'answer2' or tbl.c14 is null) and tbl.c15 = 'YES', 1, null)) as nM10,\n" +
//                "                count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 15) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 20)\n" +
//                "                             and (tbl.c14 != 'answer2' or tbl.c14 is null) and tbl.c15 = 'YES', 1, null)) as nM15,\n" +
//                "                count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 20) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 25)\n" +
//                "                             and (tbl.c14 != 'answer2' or tbl.c14 is null) and tbl.c15 = 'YES', 1, null)) as nM20,\n" +
//                "                count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 25) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 30)\n" +
//                "                             and (tbl.c14 != 'answer2' or tbl.c14 is null) and tbl.c15 = 'YES', 1, null)) as nM25,\n" +
//                "                count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 30) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 35)\n" +
//                "                             and (tbl.c14 != 'answer2' or tbl.c14 is null) and tbl.c15 = 'YES', 1, null)) as nM30,\n" +
//                "                count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 35) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 40)\n" +
//                "                             and (tbl.c14 != 'answer2' or tbl.c14 is null) and tbl.c15 = 'YES', 1, null)) as nM35,\n" +
//                "                count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 40) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 45)\n" +
//                "                             and (tbl.c14 != 'answer2' or tbl.c14 is null) and tbl.c15 = 'YES', 1, null)) as nM40,\n" +
//                "                count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 45) and\n" +
//                "                         (year(curdate()) - year(tbl.c8_dob) < 50)\n" +
//                "                             and (tbl.c14 != 'answer2' or tbl.c14 is null) and tbl.c15 = 'YES', 1, null)) as nM45,\n" +
//                "                count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 50) and\n" +
//                "                         (tbl.c14 != 'answer2' or tbl.c14 is null) and tbl.c15 = 'YES', 1, null))         as nM50\n" +
//                "         from (select thcrg.val, tb1.VCT, thc.*\n" +
//                "               from tbl_hts_case_risk_group thcrg\n" +
//                "                        inner join tbl_hts_case thc on thcrg.hts_case_id = thc.id\n" +
//                "                        inner join (select thcrg.hts_case_id,\n" +
//                "                                           count(if((thcrg.val != 'answer5' and thcrg.val != 'answer6'), null, 1)) as VCT\n" +
//                "                                    from tbl_hts_case_risk_group thcrg\n" +
//                "                                    group by thcrg.hts_case_id) tb1 on tb1.hts_case_id = thc.id\n" +
//                "               where thcrg.is_main_risk = 1) as tbl\n" +
//                "                  inner join tbl_organization_unit tou on tbl.c2_org_id = tou.id\n" +
//                "                  inner join tbl_location tl on tou.address_id = tl.id\n" +
//                "                  inner join tbl_admin_unit pro on tl.province_id = pro.id\n" +
//                "                  inner join tbl_admin_unit dis on tl.district_id = dis.id\n" +
//                "         where 1 = 1 " + whereClause +
//                "         group by orgCode, orgName, provinceName, districtName, modality) as tb\n" +
//                "where tb.modality is not null;";
//
//        org.hibernate.Query query = manager.unwrap(Session.class).createSQLQuery(sql).setResultTransformer(new AliasToBeanResultTransformer(HTSTSTDto.class));
//        if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
//            query.setParameterList("listOrg", filter.getOrgIds());
//        }
//        if (filter.getFromDate() != null) {
//            query.setParameter("fromDate", Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
//        }
//        if (filter.getToDate() != null) {
//            query.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
//        }
//        query.setResultTransformer(Transformers.aliasToBean(HTSTSTDto.class));
//        List<HTSTSTDto> ret = query.list();
//
//        String wherePE = "";
//
//        if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
//            wherePE += " AND tbl.c1_org_id in (:listOrg) ";
//        }
//        if (filter.getFromDate() != null) {
//            wherePE += " AND tbl.c1 >=:fromDate ";
//        }
//        if (filter.getToDate() != null) {
//            wherePE += "  AND tbl.c1 <=:toDate ";
//        }
//        String sqlPE = "select *\n" +
//                "from (\n" +
//                "         select tou.code                                                                         as orgCode,\n" +
//                "                tou.name                                                                         as orgName,\n" +
//                "                pro.name                                                                         as provinceName,\n" +
//                "                dis.name                                                                         as districtName,\n" +
//                "                case\n" +
//                "                    when (tbl.c7 is null or (tbl.c7 != 'answer3' and tbl.c7 != 'answer4' and tbl.c7 != 'answer5'))\n" +
//                "                        and tbl.c11 = 'YES' then 'Other community platforms'\n" +
//                "                    when tbl.c7 = 'answer2' and tbl.c11 = 'YES' then 'Community SNS' end         as modality,\n" +
//                "                count(if(tbl.val = 'answer1' and tbl.c131_result = 'answer2' and\n" +
//                "                         (tbl.c12 = 'answer1' or tbl.c12 = 'answer2')\n" +
//                "                             and not ((tbl.c16 is not null and tbl.c16 = 'answer2') or\n" +
//                "                                      (tbl.c12 is not null and tbl.c12 != 'answer1')), 1, null)) as pPWID,\n" +
//                "                count(if(tbl.val = 'answer1' and\n" +
//                "                         (tbl.c131_result = 'answer1' or (tbl.c13 = 'answer2' and tbl.c131_result is null)), 1,\n" +
//                "                         null))                                                                  as nPWID,\n" +
//                "                count(if(tbl.val = 'answer2' and tbl.c131_result = 'answer2' and\n" +
//                "                         (tbl.c12 = 'answer1' or tbl.c12 = 'answer2')\n" +
//                "                             and not ((tbl.c16 is not null and tbl.c16 = 'answer2') or\n" +
//                "                                      (tbl.c12 is not null and tbl.c12 != 'answer1')), 1, null)) as pMSM,\n" +
//                "                count(if(tbl.val = 'answer2' and\n" +
//                "                         (tbl.c131_result = 'answer1' or (tbl.c13 = 'answer2' and tbl.c131_result is null)), 1,\n" +
//                "                         null))                                                                  as nMSM,\n" +
//                "                count(if(tbl.val = 'answer4' and tbl.c131_result = 'answer2' and\n" +
//                "                         (tbl.c12 = 'answer1' or tbl.c12 = 'answer2')\n" +
//                "                             and not ((tbl.c16 is not null and tbl.c16 = 'answer2') or\n" +
//                "                                      (tbl.c12 is not null and tbl.c12 != 'answer1')), 1, null)) as pTG,\n" +
//                "                count(if(tbl.val = 'answer4' and\n" +
//                "                         (tbl.c131_result = 'answer1' or (tbl.c13 = 'answer2' and tbl.c131_result is null)), 1,\n" +
//                "                         null))                                                                  as nTG,\n" +
//                "                count(if(tbl.val = 'answer3' and tbl.c131_result = 'answer2' and\n" +
//                "                         (tbl.c12 = 'answer1' or tbl.c12 = 'answer2')\n" +
//                "                             and not ((tbl.c16 is not null and tbl.c16 = 'answer2') or\n" +
//                "                                      (tbl.c12 is not null and tbl.c12 != 'answer1')), 1, null)) as pFSW,\n" +
//                "                count(if(tbl.val = 'answer3' and\n" +
//                "                         (tbl.c131_result = 'answer1' or (tbl.c13 = 'answer2' and tbl.c131_result is null)), 1,\n" +
//                "                         null))                                                                  as nFSW,\n" +
//                "                count(if(tbl.val = 'answer0' and tbl.c131_result = 'answer2' and\n" +
//                "                         (tbl.c12 = 'answer1' or tbl.c12 = 'answer2')\n" +
//                "                             and not ((tbl.c16 is not null and tbl.c16 = 'answer2') or\n" +
//                "                                      (tbl.c12 is not null and tbl.c12 != 'answer1')), 1, null)) as pOther,\n" +
//                "                count(if(tbl.val = 'answer0' and\n" +
//                "                         (tbl.c131_result = 'answer1' or (tbl.c13 = 'answer2' and tbl.c131_result is null)), 1,\n" +
//                "                         null))                                                                  as nOther,\n" +
//                "                count(if(tbl.c3 = 'FEMALE' and tbl.c4_dob is null and tbl.c131_result = 'answer2' and\n" +
//                "                         (tbl.c12 = 'answer1' or tbl.c12 = 'answer2')\n" +
//                "                             and not ((tbl.c16 is not null and tbl.c16 = 'answer2') or\n" +
//                "                                      (tbl.c12 is not null and tbl.c12 != 'answer1')), 1, null)) as pFU,\n" +
//                "                count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) < 1) and\n" +
//                "                         tbl.c131_result = 'answer2' and (tbl.c12 = 'answer1' or tbl.c12 = 'answer2')\n" +
//                "                             and not ((tbl.c16 is not null and tbl.c16 = 'answer2') or\n" +
//                "                                      (tbl.c12 is not null and tbl.c12 != 'answer1')), 1, null)) as pF0,\n" +
//                "                count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 1) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 5) and tbl.c131_result = 'answer2' and\n" +
//                "                         (tbl.c12 = 'answer1' or tbl.c12 = 'answer2')\n" +
//                "                             and not ((tbl.c16 is not null and tbl.c16 = 'answer2') or\n" +
//                "                                      (tbl.c12 is not null and tbl.c12 != 'answer1')), 1, null)) as pF1,\n" +
//                "                count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 5) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 10) and tbl.c131_result = 'answer2' and\n" +
//                "                         (tbl.c12 = 'answer1' or tbl.c12 = 'answer2')\n" +
//                "                             and not ((tbl.c16 is not null and tbl.c16 = 'answer2') or\n" +
//                "                                      (tbl.c12 is not null and tbl.c12 != 'answer1')), 1, null)) as pF5,\n" +
//                "                count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 10) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 15) and tbl.c131_result = 'answer2' and\n" +
//                "                         (tbl.c12 = 'answer1' or tbl.c12 = 'answer2')\n" +
//                "                             and not ((tbl.c16 is not null and tbl.c16 = 'answer2') or\n" +
//                "                                      (tbl.c12 is not null and tbl.c12 != 'answer1')), 1, null)) as pF10,\n" +
//                "                count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 15) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 20) and tbl.c131_result = 'answer2' and\n" +
//                "                         (tbl.c12 = 'answer1' or tbl.c12 = 'answer2')\n" +
//                "                             and not ((tbl.c16 is not null and tbl.c16 = 'answer2') or\n" +
//                "                                      (tbl.c12 is not null and tbl.c12 != 'answer1')), 1, null)) as pF15,\n" +
//                "                count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 20) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 25) and tbl.c131_result = 'answer2' and\n" +
//                "                         (tbl.c12 = 'answer1' or tbl.c12 = 'answer2')\n" +
//                "                             and not ((tbl.c16 is not null and tbl.c16 = 'answer2') or\n" +
//                "                                      (tbl.c12 is not null and tbl.c12 != 'answer1')), 1, null)) as pF20,\n" +
//                "                count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 25) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 30) and tbl.c131_result = 'answer2' and\n" +
//                "                         (tbl.c12 = 'answer1' or tbl.c12 = 'answer2')\n" +
//                "                             and not ((tbl.c16 is not null and tbl.c16 = 'answer2') or\n" +
//                "                                      (tbl.c12 is not null and tbl.c12 != 'answer1')), 1, null)) as pF25,\n" +
//                "                count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 30) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 35) and tbl.c131_result = 'answer2' and\n" +
//                "                         (tbl.c12 = 'answer1' or tbl.c12 = 'answer2')\n" +
//                "                             and not ((tbl.c16 is not null and tbl.c16 = 'answer2') or\n" +
//                "                                      (tbl.c12 is not null and tbl.c12 != 'answer1')), 1, null)) as pF30,\n" +
//                "                count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 35) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 40) and tbl.c131_result = 'answer2' and\n" +
//                "                         (tbl.c12 = 'answer1' or tbl.c12 = 'answer2')\n" +
//                "                             and not ((tbl.c16 is not null and tbl.c16 = 'answer2') or\n" +
//                "                                      (tbl.c12 is not null and tbl.c12 != 'answer1')), 1, null)) as pF35,\n" +
//                "                count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 40) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 45) and tbl.c131_result = 'answer2' and\n" +
//                "                         (tbl.c12 = 'answer1' or tbl.c12 = 'answer2')\n" +
//                "                             and not ((tbl.c16 is not null and tbl.c16 = 'answer2') or\n" +
//                "                                      (tbl.c12 is not null and tbl.c12 != 'answer1')), 1, null)) as pF40,\n" +
//                "                count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 45) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 50) and tbl.c131_result = 'answer2' and\n" +
//                "                         (tbl.c12 = 'answer1' or tbl.c12 = 'answer2')\n" +
//                "                             and not ((tbl.c16 is not null and tbl.c16 = 'answer2') or\n" +
//                "                                      (tbl.c12 is not null and tbl.c12 != 'answer1')), 1, null)) as pF45,\n" +
//                "                count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 50) and\n" +
//                "                         tbl.c131_result = 'answer2' and (tbl.c12 = 'answer1' or tbl.c12 = 'answer2')\n" +
//                "                             and not ((tbl.c16 is not null and tbl.c16 = 'answer2') or\n" +
//                "                                      (tbl.c12 is not null and tbl.c12 != 'answer1')), 1, null)) as pF50,\n" +
//                "                count(if(tbl.c3 = 'MALE' and tbl.c4_dob is null and tbl.c131_result = 'answer2' and\n" +
//                "                         (tbl.c12 = 'answer1' or tbl.c12 = 'answer2')\n" +
//                "                             and not ((tbl.c16 is not null and tbl.c16 = 'answer2') or\n" +
//                "                                      (tbl.c12 is not null and tbl.c12 != 'answer1')), 1, null)) as pMU,\n" +
//                "                count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) < 1) and\n" +
//                "                         tbl.c131_result = 'answer2' and (tbl.c12 = 'answer1' or tbl.c12 = 'answer2')\n" +
//                "                             and not ((tbl.c16 is not null and tbl.c16 = 'answer2') or\n" +
//                "                                      (tbl.c12 is not null and tbl.c12 != 'answer1')), 1, null)) as pM0,\n" +
//                "                count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 1) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 5) and tbl.c131_result = 'answer2' and\n" +
//                "                         (tbl.c12 = 'answer1' or tbl.c12 = 'answer2')\n" +
//                "                             and not ((tbl.c16 is not null and tbl.c16 = 'answer2') or\n" +
//                "                                      (tbl.c12 is not null and tbl.c12 != 'answer1')), 1, null)) as pM1,\n" +
//                "                count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 5) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 10) and tbl.c131_result = 'answer2' and\n" +
//                "                         (tbl.c12 = 'answer1' or tbl.c12 = 'answer2')\n" +
//                "                             and not ((tbl.c16 is not null and tbl.c16 = 'answer2') or\n" +
//                "                                      (tbl.c12 is not null and tbl.c12 != 'answer1')), 1, null)) as pM5,\n" +
//                "                count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 10) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 15) and tbl.c131_result = 'answer2' and\n" +
//                "                         (tbl.c12 = 'answer1' or tbl.c12 = 'answer2')\n" +
//                "                             and not ((tbl.c16 is not null and tbl.c16 = 'answer2') or\n" +
//                "                                      (tbl.c12 is not null and tbl.c12 != 'answer1')), 1, null)) as pM10,\n" +
//                "                count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 15) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 20) and tbl.c131_result = 'answer2' and\n" +
//                "                         (tbl.c12 = 'answer1' or tbl.c12 = 'answer2')\n" +
//                "                             and not ((tbl.c16 is not null and tbl.c16 = 'answer2') or\n" +
//                "                                      (tbl.c12 is not null and tbl.c12 != 'answer1')), 1, null)) as pM15,\n" +
//                "                count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 20) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 25) and tbl.c131_result = 'answer2' and\n" +
//                "                         (tbl.c12 = 'answer1' or tbl.c12 = 'answer2')\n" +
//                "                             and not ((tbl.c16 is not null and tbl.c16 = 'answer2') or\n" +
//                "                                      (tbl.c12 is not null and tbl.c12 != 'answer1')), 1, null)) as pM20,\n" +
//                "                count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 25) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 30) and tbl.c131_result = 'answer2' and\n" +
//                "                         (tbl.c12 = 'answer1' or tbl.c12 = 'answer2')\n" +
//                "                             and not ((tbl.c16 is not null and tbl.c16 = 'answer2') or\n" +
//                "                                      (tbl.c12 is not null and tbl.c12 != 'answer1')), 1, null)) as pM25,\n" +
//                "                count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 30) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 35) and tbl.c131_result = 'answer2' and\n" +
//                "                         (tbl.c12 = 'answer1' or tbl.c12 = 'answer2')\n" +
//                "                             and not ((tbl.c16 is not null and tbl.c16 = 'answer2') or\n" +
//                "                                      (tbl.c12 is not null and tbl.c12 != 'answer1')), 1, null)) as pM30,\n" +
//                "                count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 35) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 40) and tbl.c131_result = 'answer2' and\n" +
//                "                         (tbl.c12 = 'answer1' or tbl.c12 = 'answer2')\n" +
//                "                             and not ((tbl.c16 is not null and tbl.c16 = 'answer2') or\n" +
//                "                                      (tbl.c12 is not null and tbl.c12 != 'answer1')), 1, null)) as pM35,\n" +
//                "                count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 40) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 45) and tbl.c131_result = 'answer2' and\n" +
//                "                         (tbl.c12 = 'answer1' or tbl.c12 = 'answer2')\n" +
//                "                             and not ((tbl.c16 is not null and tbl.c16 = 'answer2') or\n" +
//                "                                      (tbl.c12 is not null and tbl.c12 != 'answer1')), 1, null)) as pM40,\n" +
//                "                count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 45) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 50) and tbl.c131_result = 'answer2' and\n" +
//                "                         (tbl.c12 = 'answer1' or tbl.c12 = 'answer2')\n" +
//                "                             and not ((tbl.c16 is not null and tbl.c16 = 'answer2') or\n" +
//                "                                      (tbl.c12 is not null and tbl.c12 != 'answer1')), 1, null)) as pM45,\n" +
//                "                count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 50) and\n" +
//                "                         tbl.c131_result = 'answer2' and (tbl.c12 = 'answer1' or tbl.c12 = 'answer2')\n" +
//                "                             and not ((tbl.c16 is not null and tbl.c16 = 'answer2') or\n" +
//                "                                      (tbl.c12 is not null and tbl.c12 != 'answer1')), 1, null)) as pM50,\n" +
//                "                count(if(tbl.c3 = 'FEMALE' and tbl.c4_dob is null\n" +
//                "                             and (tbl.c131_result = 'answer1' or (tbl.c13 = 'answer2' and tbl.c131_result is null)), 1,\n" +
//                "                         null))                                                                  as nFU,\n" +
//                "                count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) < 1)\n" +
//                "                             and (tbl.c131_result = 'answer1' or (tbl.c13 = 'answer2' and tbl.c131_result is null)), 1,\n" +
//                "                         null))                                                                  as nF0,\n" +
//                "                count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 1) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 5)\n" +
//                "                             and (tbl.c131_result = 'answer1' or (tbl.c13 = 'answer2' and tbl.c131_result is null)), 1,\n" +
//                "                         null))                                                                  as nF1,\n" +
//                "                count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 5) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 10)\n" +
//                "                             and (tbl.c131_result = 'answer1' or (tbl.c13 = 'answer2' and tbl.c131_result is null)), 1,\n" +
//                "                         null))                                                                  as nF5,\n" +
//                "                count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 10) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 15)\n" +
//                "                             and (tbl.c131_result = 'answer1' or (tbl.c13 = 'answer2' and tbl.c131_result is null)), 1,\n" +
//                "                         null))                                                                  as nF10,\n" +
//                "                count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 15) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 20)\n" +
//                "                             and (tbl.c131_result = 'answer1' or (tbl.c13 = 'answer2' and tbl.c131_result is null)), 1,\n" +
//                "                         null))                                                                  as nF15,\n" +
//                "                count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 20) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 25)\n" +
//                "                             and (tbl.c131_result = 'answer1' or (tbl.c13 = 'answer2' and tbl.c131_result is null)), 1,\n" +
//                "                         null))                                                                  as nF20,\n" +
//                "                count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 25) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 30)\n" +
//                "                             and (tbl.c131_result = 'answer1' or (tbl.c13 = 'answer2' and tbl.c131_result is null)), 1,\n" +
//                "                         null))                                                                  as nF25,\n" +
//                "                count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 30) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 35)\n" +
//                "                             and (tbl.c131_result = 'answer1' or (tbl.c13 = 'answer2' and tbl.c131_result is null)), 1,\n" +
//                "                         null))                                                                  as nF30,\n" +
//                "                count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 35) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 40)\n" +
//                "                             and (tbl.c131_result = 'answer1' or (tbl.c13 = 'answer2' and tbl.c131_result is null)), 1,\n" +
//                "                         null))                                                                  as nF35,\n" +
//                "                count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 40) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 45)\n" +
//                "                             and (tbl.c131_result = 'answer1' or (tbl.c13 = 'answer2' and tbl.c131_result is null)), 1,\n" +
//                "                         null))                                                                  as nF40,\n" +
//                "                count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 45) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 50)\n" +
//                "                             and (tbl.c131_result = 'answer1' or (tbl.c13 = 'answer2' and tbl.c131_result is null)), 1,\n" +
//                "                         null))                                                                  as nF45,\n" +
//                "                count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 50)\n" +
//                "                             and (tbl.c131_result = 'answer1' or (tbl.c13 = 'answer2' and tbl.c131_result is null)), 1,\n" +
//                "                         null))                                                                  as nF50,\n" +
//                "                count(if(tbl.c3 = 'MALE' and tbl.c4_dob is null\n" +
//                "                             and (tbl.c131_result = 'answer1' or (tbl.c13 = 'answer2' and tbl.c131_result is null)), 1,\n" +
//                "                         null))                                                                  as nMU,\n" +
//                "                count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) < 1)\n" +
//                "                             and (tbl.c131_result = 'answer1' or (tbl.c13 = 'answer2' and tbl.c131_result is null)), 1,\n" +
//                "                         null))                                                                  as nM0,\n" +
//                "                count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 1) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 5)\n" +
//                "                             and (tbl.c131_result = 'answer1' or (tbl.c13 = 'answer2' and tbl.c131_result is null)), 1,\n" +
//                "                         null))                                                                  as nM1,\n" +
//                "                count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 5) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 10)\n" +
//                "                             and (tbl.c131_result = 'answer1' or (tbl.c13 = 'answer2' and tbl.c131_result is null)), 1,\n" +
//                "                         null))                                                                  as nM5,\n" +
//                "                count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 10) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 15)\n" +
//                "                             and (tbl.c131_result = 'answer1' or (tbl.c13 = 'answer2' and tbl.c131_result is null)), 1,\n" +
//                "                         null))                                                                  as nM10,\n" +
//                "                count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 15) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 20)\n" +
//                "                             and (tbl.c131_result = 'answer1' or (tbl.c13 = 'answer2' and tbl.c131_result is null)), 1,\n" +
//                "                         null))                                                                  as nM15,\n" +
//                "                count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 20) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 25)\n" +
//                "                             and (tbl.c131_result = 'answer1' or (tbl.c13 = 'answer2' and tbl.c131_result is null)), 1,\n" +
//                "                         null))                                                                  as nM20,\n" +
//                "                count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 25) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 30)\n" +
//                "                             and (tbl.c131_result = 'answer1' or (tbl.c13 = 'answer2' and tbl.c131_result is null)), 1,\n" +
//                "                         null))                                                                  as nM25,\n" +
//                "                count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 30) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 35)\n" +
//                "                             and (tbl.c131_result = 'answer1' or (tbl.c13 = 'answer2' and tbl.c131_result is null)), 1,\n" +
//                "                         null))                                                                  as nM30,\n" +
//                "                count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 35) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 40)\n" +
//                "                             and (tbl.c131_result = 'answer1' or (tbl.c13 = 'answer2' and tbl.c131_result is null)), 1,\n" +
//                "                         null))                                                                  as nM35,\n" +
//                "                count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 40) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 45)\n" +
//                "                             and (tbl.c131_result = 'answer1' or (tbl.c13 = 'answer2' and tbl.c131_result is null)), 1,\n" +
//                "                         null))                                                                  as nM40,\n" +
//                "                count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 45) and\n" +
//                "                         (year(curdate()) - year(tbl.c4_dob) < 50)\n" +
//                "                             and (tbl.c131_result = 'answer1' or (tbl.c13 = 'answer2' and tbl.c131_result is null)), 1,\n" +
//                "                         null))                                                                  as nM45,\n" +
//                "                count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 50)\n" +
//                "                             and (tbl.c131_result = 'answer1' or (tbl.c13 = 'answer2' and tbl.c131_result is null)), 1,\n" +
//                "                         null))                                                                  as nM50\n" +
//                "         from (select tpcrg.val, tpc.*\n" +
//                "               from tbl_pe_case_risk_group tpcrg\n" +
//                "                        inner join tbl_pe_case tpc on tpcrg.pe_case_id = tpc.id\n" +
//                "               where tpcrg.is_main_risk = 1) as tbl\n" +
//                "                  inner join tbl_organization_unit tou on tbl.c1_org_id = tou.id\n" +
//                "                  inner join tbl_location tl on tou.address_id = tl.id\n" +
//                "                  inner join tbl_admin_unit pro on tl.province_id = pro.id\n" +
//                "                  inner join tbl_admin_unit dis on tl.district_id = dis.id\n" +
//                "         where 1 = 1 " + wherePE +
//                "         group by orgCode, orgName, provinceName, districtName, modality) as tb\n" +
//                "where tb.modality is not null;";
//        org.hibernate.Query queryPE = manager.unwrap(Session.class).createSQLQuery(sqlPE).setResultTransformer(new AliasToBeanResultTransformer(HTSTSTDto.class));
//        if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
//            queryPE.setParameterList("listOrg", filter.getOrgIds());
//        }
//        if (filter.getFromDate() != null) {
//            queryPE.setParameter("fromDate", Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
//        }
//        if (filter.getToDate() != null) {
//            queryPE.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
//        }
//        queryPE.setResultTransformer(Transformers.aliasToBean(HTSTSTDto.class));
//        List<HTSTSTDto> retPE = queryPE.list();
//        ret.addAll(retPE);
//
//        return ret;
//    }

    private List<HTSTSTDto> getDataHTSTST(PreventionFilterDto filter) {
        Hashtable<String,HTSTSTDto> hashtable = new Hashtable<>();
        List<HTSTSTDto> vctKP = reportMerService.getDataHTSTSTModalityVCTByKP(filter);
        List<HTSTSTDto> vctByPositive = reportMerService.getDataHTSTSTModalityVCTByPositive(filter);
        List<HTSTSTDto> vctByNegative = reportMerService.getDataHTSTSTModalityVCTByNegative(filter);
        List<HTSTSTDto> otherCommunityByKP = reportMerService.getDataHTSTSTModalityOtherCommunityByKP(filter);
        List<HTSTSTDto> otherCommunityByPositive = reportMerService.getDataHTSTSTModalityOtherCommunityByPositive(filter);
        List<HTSTSTDto> otherCommunityByNegative = reportMerService.getDataHTSTSTModalityOtherCommunityByNegative(filter);
        List<HTSTSTDto> communitySNSByKP = reportMerService.getDataHTSTSTModalityCommunitySNSByKP(filter);
        List<HTSTSTDto> communitySNSByPositive = reportMerService.getDataHTSTSTModalityCommunitySNSByPositive(filter);
        List<HTSTSTDto> communitySNSByNegative = reportMerService.getDataHTSTSTModalityCommunitySNSByNegative(filter);
        List<HTSTSTDto> facilitySNSByKP = reportMerService.getDataHTSTSTModalityFacilitySNSByKP(filter);
        List<HTSTSTDto> facilitySNSByPositive = reportMerService.getDataHTSTSTModalityFacilitySNSByPositive(filter);
        List<HTSTSTDto> facilitySNSByNegative = reportMerService.getDataHTSTSTModalityFacilitySNSByNegative(filter);
        HTSTSTDto htststDto = null;
        for( HTSTSTDto dto :vctKP){
            if(hashtable.containsKey(dto.getOrgId()+"-"+dto.getModality())){
                htststDto = hashtable.get(dto.getOrgId()+"-"+dto.getModality());
                htststDto.setpPWID(htststDto.getpPWID().add(dto.getpPWID()));
                htststDto.setnPWID(htststDto.getnPWID().add(dto.getnPWID()));
                htststDto.setpMSM(htststDto.getpMSM().add(dto.getpMSM()));
                htststDto.setnMSM(htststDto.getnMSM().add(dto.getnMSM()));
                htststDto.setpTG(htststDto.getpTG().add(dto.getpTG()));
                htststDto.setnTG(htststDto.getnTG().add(dto.getnTG()));
                htststDto.setpFSW(htststDto.getpFSW().add(dto.getpFSW()));
                htststDto.setnFSW(htststDto.getnFSW().add(dto.getnFSW()));
                htststDto.setpOther(htststDto.getpOther().add(dto.getpOther()));
                htststDto.setnOther(htststDto.getnOther().add(dto.getnOther()));

                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),htststDto);
            }else{
                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),dto);
            }
        }

        for( HTSTSTDto dto :vctByPositive){
            if(hashtable.containsKey(dto.getOrgId()+"-"+dto.getModality())){
                htststDto = hashtable.get(dto.getOrgId()+"-"+dto.getModality());

                htststDto.setpFU(htststDto.getpFU().add(dto.getpFU()));
                htststDto.setpF0(htststDto.getpF0().add(dto.getpF0()));
                htststDto.setpF1(htststDto.getpF1().add(dto.getpF1()));
                htststDto.setpF5(htststDto.getpF5().add(dto.getpF5()));
                htststDto.setpF10(htststDto.getpF10().add(dto.getpF10()));
                htststDto.setpF15(htststDto.getpF15().add(dto.getpF15()));
                htststDto.setpF20(htststDto.getpF20().add(dto.getpF20()));
                htststDto.setpF25(htststDto.getpF25().add(dto.getpF25()));
                htststDto.setpF30(htststDto.getpF30().add(dto.getpF30()));
                htststDto.setpF35(htststDto.getpF35().add(dto.getpF35()));
                htststDto.setpF40(htststDto.getpF40().add(dto.getpF40()));
                htststDto.setpF45(htststDto.getpF45().add(dto.getpF45()));
                htststDto.setpF50(htststDto.getpF50().add(dto.getpF50()));

                htststDto.setpMU(htststDto.getpMU().add(dto.getpMU()));
                htststDto.setpM0(htststDto.getpM0().add(dto.getpM0()));
                htststDto.setpM1(htststDto.getpM1().add(dto.getpM1()));
                htststDto.setpM5(htststDto.getpM5().add(dto.getpM5()));
                htststDto.setpM10(htststDto.getpM10().add(dto.getpM10()));
                htststDto.setpM15(htststDto.getpM15().add(dto.getpM15()));
                htststDto.setpM20(htststDto.getpM20().add(dto.getpM20()));
                htststDto.setpM25(htststDto.getpM25().add(dto.getpM25()));
                htststDto.setpM30(htststDto.getpM30().add(dto.getpM30()));
                htststDto.setpM35(htststDto.getpM35().add(dto.getpM35()));
                htststDto.setpM40(htststDto.getpM40().add(dto.getpM40()));
                htststDto.setpM45(htststDto.getpM45().add(dto.getpM45()));
                htststDto.setpM50(htststDto.getpM50().add(dto.getpM50()));

                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),htststDto);
            }else{
                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),dto);
            }
        }

        for( HTSTSTDto dto :vctByNegative){
            if(hashtable.containsKey(dto.getOrgId()+"-"+dto.getModality())){
                htststDto = hashtable.get(dto.getOrgId()+"-"+dto.getModality());

                htststDto.setnFU(htststDto.getnFU().add(dto.getnFU()));
                htststDto.setnF0(htststDto.getnF0().add(dto.getnF0()));
                htststDto.setnF1(htststDto.getnF1().add(dto.getnF1()));
                htststDto.setnF5(htststDto.getnF5().add(dto.getnF5()));
                htststDto.setnF10(htststDto.getnF10().add(dto.getnF10()));
                htststDto.setnF15(htststDto.getnF15().add(dto.getnF15()));
                htststDto.setnF20(htststDto.getnF20().add(dto.getnF20()));
                htststDto.setnF25(htststDto.getnF25().add(dto.getnF25()));
                htststDto.setnF30(htststDto.getnF30().add(dto.getnF30()));
                htststDto.setnF35(htststDto.getnF35().add(dto.getnF35()));
                htststDto.setnF40(htststDto.getnF40().add(dto.getnF40()));
                htststDto.setnF45(htststDto.getnF45().add(dto.getnF45()));
                htststDto.setnF50(htststDto.getnF50().add(dto.getnF50()));

                htststDto.setnMU(htststDto.getnMU().add(dto.getnMU()));
                htststDto.setnM0(htststDto.getnM0().add(dto.getnM0()));
                htststDto.setnM1(htststDto.getnM1().add(dto.getnM1()));
                htststDto.setnM5(htststDto.getnM5().add(dto.getnM5()));
                htststDto.setnM10(htststDto.getnM10().add(dto.getnM10()));
                htststDto.setnM15(htststDto.getnM15().add(dto.getnM15()));
                htststDto.setnM20(htststDto.getnM20().add(dto.getnM20()));
                htststDto.setnM25(htststDto.getnM25().add(dto.getnM25()));
                htststDto.setnM30(htststDto.getnM30().add(dto.getnM30()));
                htststDto.setnM35(htststDto.getnM35().add(dto.getnM35()));
                htststDto.setnM40(htststDto.getnM40().add(dto.getnM40()));
                htststDto.setnM45(htststDto.getnM45().add(dto.getnM45()));
                htststDto.setnM50(htststDto.getnM50().add(dto.getnM50()));

                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),htststDto);
            }else{
                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),dto);
            }
        }

        //modality otherCommunityByKP
        for( HTSTSTDto dto :otherCommunityByKP){
            if(hashtable.containsKey(dto.getOrgId()+"-"+dto.getModality())){
                htststDto = hashtable.get(dto.getOrgId()+"-"+dto.getModality());
                htststDto.setpPWID(htststDto.getpPWID().add(dto.getpPWID()));
                htststDto.setnPWID(htststDto.getnPWID().add(dto.getnPWID()));
                htststDto.setpMSM(htststDto.getpMSM().add(dto.getpMSM()));
                htststDto.setnMSM(htststDto.getnMSM().add(dto.getnMSM()));
                htststDto.setpTG(htststDto.getpTG().add(dto.getpTG()));
                htststDto.setnTG(htststDto.getnTG().add(dto.getnTG()));
                htststDto.setpFSW(htststDto.getpFSW().add(dto.getpFSW()));
                htststDto.setnFSW(htststDto.getnFSW().add(dto.getnFSW()));
                htststDto.setpOther(htststDto.getpOther().add(dto.getpOther()));
                htststDto.setnOther(htststDto.getnOther().add(dto.getnOther()));

                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),htststDto);
            }else{
                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),dto);
            }
        }

        for( HTSTSTDto dto :otherCommunityByPositive){
            if(hashtable.containsKey(dto.getOrgId()+"-"+dto.getModality())){
                htststDto = hashtable.get(dto.getOrgId()+"-"+dto.getModality());

                htststDto.setpFU(htststDto.getpFU().add(dto.getpFU()));
                htststDto.setpF0(htststDto.getpF0().add(dto.getpF0()));
                htststDto.setpF1(htststDto.getpF1().add(dto.getpF1()));
                htststDto.setpF5(htststDto.getpF5().add(dto.getpF5()));
                htststDto.setpF10(htststDto.getpF10().add(dto.getpF10()));
                htststDto.setpF15(htststDto.getpF15().add(dto.getpF15()));
                htststDto.setpF20(htststDto.getpF20().add(dto.getpF20()));
                htststDto.setpF25(htststDto.getpF25().add(dto.getpF25()));
                htststDto.setpF30(htststDto.getpF30().add(dto.getpF30()));
                htststDto.setpF35(htststDto.getpF35().add(dto.getpF35()));
                htststDto.setpF40(htststDto.getpF40().add(dto.getpF40()));
                htststDto.setpF45(htststDto.getpF45().add(dto.getpF45()));
                htststDto.setpF50(htststDto.getpF50().add(dto.getpF50()));

                htststDto.setpMU(htststDto.getpMU().add(dto.getpMU()));
                htststDto.setpM0(htststDto.getpM0().add(dto.getpM0()));
                htststDto.setpM1(htststDto.getpM1().add(dto.getpM1()));
                htststDto.setpM5(htststDto.getpM5().add(dto.getpM5()));
                htststDto.setpM10(htststDto.getpM10().add(dto.getpM10()));
                htststDto.setpM15(htststDto.getpM15().add(dto.getpM15()));
                htststDto.setpM20(htststDto.getpM20().add(dto.getpM20()));
                htststDto.setpM25(htststDto.getpM25().add(dto.getpM25()));
                htststDto.setpM30(htststDto.getpM30().add(dto.getpM30()));
                htststDto.setpM35(htststDto.getpM35().add(dto.getpM35()));
                htststDto.setpM40(htststDto.getpM40().add(dto.getpM40()));
                htststDto.setpM45(htststDto.getpM45().add(dto.getpM45()));
                htststDto.setpM50(htststDto.getpM50().add(dto.getpM50()));

                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),htststDto);
            }else{
                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),dto);
            }
        }

        for( HTSTSTDto dto :otherCommunityByNegative){
            if(hashtable.containsKey(dto.getOrgId()+"-"+dto.getModality())){
                htststDto = hashtable.get(dto.getOrgId()+"-"+dto.getModality());

                htststDto.setnFU(htststDto.getnFU().add(dto.getnFU()));
                htststDto.setnF0(htststDto.getnF0().add(dto.getnF0()));
                htststDto.setnF1(htststDto.getnF1().add(dto.getnF1()));
                htststDto.setnF5(htststDto.getnF5().add(dto.getnF5()));
                htststDto.setnF10(htststDto.getnF10().add(dto.getnF10()));
                htststDto.setnF15(htststDto.getnF15().add(dto.getnF15()));
                htststDto.setnF20(htststDto.getnF20().add(dto.getnF20()));
                htststDto.setnF25(htststDto.getnF25().add(dto.getnF25()));
                htststDto.setnF30(htststDto.getnF30().add(dto.getnF30()));
                htststDto.setnF35(htststDto.getnF35().add(dto.getnF35()));
                htststDto.setnF40(htststDto.getnF40().add(dto.getnF40()));
                htststDto.setnF45(htststDto.getnF45().add(dto.getnF45()));
                htststDto.setnF50(htststDto.getnF50().add(dto.getnF50()));

                htststDto.setnMU(htststDto.getnMU().add(dto.getnMU()));
                htststDto.setnM0(htststDto.getnM0().add(dto.getnM0()));
                htststDto.setnM1(htststDto.getnM1().add(dto.getnM1()));
                htststDto.setnM5(htststDto.getnM5().add(dto.getnM5()));
                htststDto.setnM10(htststDto.getnM10().add(dto.getnM10()));
                htststDto.setnM15(htststDto.getnM15().add(dto.getnM15()));
                htststDto.setnM20(htststDto.getnM20().add(dto.getnM20()));
                htststDto.setnM25(htststDto.getnM25().add(dto.getnM25()));
                htststDto.setnM30(htststDto.getnM30().add(dto.getnM30()));
                htststDto.setnM35(htststDto.getnM35().add(dto.getnM35()));
                htststDto.setnM40(htststDto.getnM40().add(dto.getnM40()));
                htststDto.setnM45(htststDto.getnM45().add(dto.getnM45()));
                htststDto.setnM50(htststDto.getnM50().add(dto.getnM50()));

                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),htststDto);
            }else{
                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),dto);
            }
        }

        for( HTSTSTDto dto :communitySNSByKP){
            if(hashtable.containsKey(dto.getOrgId()+"-"+dto.getModality())){
                htststDto = hashtable.get(dto.getOrgId()+"-"+dto.getModality());
                htststDto.setpPWID(htststDto.getpPWID().add(dto.getpPWID()));
                htststDto.setnPWID(htststDto.getnPWID().add(dto.getnPWID()));
                htststDto.setpMSM(htststDto.getpMSM().add(dto.getpMSM()));
                htststDto.setnMSM(htststDto.getnMSM().add(dto.getnMSM()));
                htststDto.setpTG(htststDto.getpTG().add(dto.getpTG()));
                htststDto.setnTG(htststDto.getnTG().add(dto.getnTG()));
                htststDto.setpFSW(htststDto.getpFSW().add(dto.getpFSW()));
                htststDto.setnFSW(htststDto.getnFSW().add(dto.getnFSW()));
                htststDto.setpOther(htststDto.getpOther().add(dto.getpOther()));
                htststDto.setnOther(htststDto.getnOther().add(dto.getnOther()));

                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),htststDto);
            }else{
                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),dto);
            }
        }

        for( HTSTSTDto dto :communitySNSByPositive){
            if(hashtable.containsKey(dto.getOrgId()+"-"+dto.getModality())){
                htststDto = hashtable.get(dto.getOrgId()+"-"+dto.getModality());

                htststDto.setpFU(htststDto.getpFU().add(dto.getpFU()));
                htststDto.setpF0(htststDto.getpF0().add(dto.getpF0()));
                htststDto.setpF1(htststDto.getpF1().add(dto.getpF1()));
                htststDto.setpF5(htststDto.getpF5().add(dto.getpF5()));
                htststDto.setpF10(htststDto.getpF10().add(dto.getpF10()));
                htststDto.setpF15(htststDto.getpF15().add(dto.getpF15()));
                htststDto.setpF20(htststDto.getpF20().add(dto.getpF20()));
                htststDto.setpF25(htststDto.getpF25().add(dto.getpF25()));
                htststDto.setpF30(htststDto.getpF30().add(dto.getpF30()));
                htststDto.setpF35(htststDto.getpF35().add(dto.getpF35()));
                htststDto.setpF40(htststDto.getpF40().add(dto.getpF40()));
                htststDto.setpF45(htststDto.getpF45().add(dto.getpF45()));
                htststDto.setpF50(htststDto.getpF50().add(dto.getpF50()));

                htststDto.setpMU(htststDto.getpMU().add(dto.getpMU()));
                htststDto.setpM0(htststDto.getpM0().add(dto.getpM0()));
                htststDto.setpM1(htststDto.getpM1().add(dto.getpM1()));
                htststDto.setpM5(htststDto.getpM5().add(dto.getpM5()));
                htststDto.setpM10(htststDto.getpM10().add(dto.getpM10()));
                htststDto.setpM15(htststDto.getpM15().add(dto.getpM15()));
                htststDto.setpM20(htststDto.getpM20().add(dto.getpM20()));
                htststDto.setpM25(htststDto.getpM25().add(dto.getpM25()));
                htststDto.setpM30(htststDto.getpM30().add(dto.getpM30()));
                htststDto.setpM35(htststDto.getpM35().add(dto.getpM35()));
                htststDto.setpM40(htststDto.getpM40().add(dto.getpM40()));
                htststDto.setpM45(htststDto.getpM45().add(dto.getpM45()));
                htststDto.setpM50(htststDto.getpM50().add(dto.getpM50()));

                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),htststDto);
            }else{
                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),dto);
            }
        }

        for( HTSTSTDto dto :communitySNSByNegative){
            if(hashtable.containsKey(dto.getOrgId()+"-"+dto.getModality())){
                htststDto = hashtable.get(dto.getOrgId()+"-"+dto.getModality());

                htststDto.setnFU(htststDto.getnFU().add(dto.getnFU()));
                htststDto.setnF0(htststDto.getnF0().add(dto.getnF0()));
                htststDto.setnF1(htststDto.getnF1().add(dto.getnF1()));
                htststDto.setnF5(htststDto.getnF5().add(dto.getnF5()));
                htststDto.setnF10(htststDto.getnF10().add(dto.getnF10()));
                htststDto.setnF15(htststDto.getnF15().add(dto.getnF15()));
                htststDto.setnF20(htststDto.getnF20().add(dto.getnF20()));
                htststDto.setnF25(htststDto.getnF25().add(dto.getnF25()));
                htststDto.setnF30(htststDto.getnF30().add(dto.getnF30()));
                htststDto.setnF35(htststDto.getnF35().add(dto.getnF35()));
                htststDto.setnF40(htststDto.getnF40().add(dto.getnF40()));
                htststDto.setnF45(htststDto.getnF45().add(dto.getnF45()));
                htststDto.setnF50(htststDto.getnF50().add(dto.getnF50()));

                htststDto.setnMU(htststDto.getnMU().add(dto.getnMU()));
                htststDto.setnM0(htststDto.getnM0().add(dto.getnM0()));
                htststDto.setnM1(htststDto.getnM1().add(dto.getnM1()));
                htststDto.setnM5(htststDto.getnM5().add(dto.getnM5()));
                htststDto.setnM10(htststDto.getnM10().add(dto.getnM10()));
                htststDto.setnM15(htststDto.getnM15().add(dto.getnM15()));
                htststDto.setnM20(htststDto.getnM20().add(dto.getnM20()));
                htststDto.setnM25(htststDto.getnM25().add(dto.getnM25()));
                htststDto.setnM30(htststDto.getnM30().add(dto.getnM30()));
                htststDto.setnM35(htststDto.getnM35().add(dto.getnM35()));
                htststDto.setnM40(htststDto.getnM40().add(dto.getnM40()));
                htststDto.setnM45(htststDto.getnM45().add(dto.getnM45()));
                htststDto.setnM50(htststDto.getnM50().add(dto.getnM50()));

                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),htststDto);
            }else{
                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),dto);
            }
        }

        for( HTSTSTDto dto :facilitySNSByKP){
            if(hashtable.containsKey(dto.getOrgId()+"-"+dto.getModality())){
                htststDto = hashtable.get(dto.getOrgId()+"-"+dto.getModality());
                htststDto.setpPWID(htststDto.getpPWID().add(dto.getpPWID()));
                htststDto.setnPWID(htststDto.getnPWID().add(dto.getnPWID()));
                htststDto.setpMSM(htststDto.getpMSM().add(dto.getpMSM()));
                htststDto.setnMSM(htststDto.getnMSM().add(dto.getnMSM()));
                htststDto.setpTG(htststDto.getpTG().add(dto.getpTG()));
                htststDto.setnTG(htststDto.getnTG().add(dto.getnTG()));
                htststDto.setpFSW(htststDto.getpFSW().add(dto.getpFSW()));
                htststDto.setnFSW(htststDto.getnFSW().add(dto.getnFSW()));
                htststDto.setpOther(htststDto.getpOther().add(dto.getpOther()));
                htststDto.setnOther(htststDto.getnOther().add(dto.getnOther()));

                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),htststDto);
            }else{
                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),dto);
            }
        }

        for( HTSTSTDto dto :facilitySNSByPositive){
            if(hashtable.containsKey(dto.getOrgId()+"-"+dto.getModality())){
                htststDto = hashtable.get(dto.getOrgId()+"-"+dto.getModality());

                htststDto.setpFU(htststDto.getpFU().add(dto.getpFU()));
                htststDto.setpF0(htststDto.getpF0().add(dto.getpF0()));
                htststDto.setpF1(htststDto.getpF1().add(dto.getpF1()));
                htststDto.setpF5(htststDto.getpF5().add(dto.getpF5()));
                htststDto.setpF10(htststDto.getpF10().add(dto.getpF10()));
                htststDto.setpF15(htststDto.getpF15().add(dto.getpF15()));
                htststDto.setpF20(htststDto.getpF20().add(dto.getpF20()));
                htststDto.setpF25(htststDto.getpF25().add(dto.getpF25()));
                htststDto.setpF30(htststDto.getpF30().add(dto.getpF30()));
                htststDto.setpF35(htststDto.getpF35().add(dto.getpF35()));
                htststDto.setpF40(htststDto.getpF40().add(dto.getpF40()));
                htststDto.setpF45(htststDto.getpF45().add(dto.getpF45()));
                htststDto.setpF50(htststDto.getpF50().add(dto.getpF50()));

                htststDto.setpMU(htststDto.getpMU().add(dto.getpMU()));
                htststDto.setpM0(htststDto.getpM0().add(dto.getpM0()));
                htststDto.setpM1(htststDto.getpM1().add(dto.getpM1()));
                htststDto.setpM5(htststDto.getpM5().add(dto.getpM5()));
                htststDto.setpM10(htststDto.getpM10().add(dto.getpM10()));
                htststDto.setpM15(htststDto.getpM15().add(dto.getpM15()));
                htststDto.setpM20(htststDto.getpM20().add(dto.getpM20()));
                htststDto.setpM25(htststDto.getpM25().add(dto.getpM25()));
                htststDto.setpM30(htststDto.getpM30().add(dto.getpM30()));
                htststDto.setpM35(htststDto.getpM35().add(dto.getpM35()));
                htststDto.setpM40(htststDto.getpM40().add(dto.getpM40()));
                htststDto.setpM45(htststDto.getpM45().add(dto.getpM45()));
                htststDto.setpM50(htststDto.getpM50().add(dto.getpM50()));

                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),htststDto);
            }else{
                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),dto);
            }
        }

        for( HTSTSTDto dto :facilitySNSByNegative){
            if(hashtable.containsKey(dto.getOrgId()+"-"+dto.getModality())){
                htststDto = hashtable.get(dto.getOrgId()+"-"+dto.getModality());

                htststDto.setnFU(htststDto.getnFU().add(dto.getnFU()));
                htststDto.setnF0(htststDto.getnF0().add(dto.getnF0()));
                htststDto.setnF1(htststDto.getnF1().add(dto.getnF1()));
                htststDto.setnF5(htststDto.getnF5().add(dto.getnF5()));
                htststDto.setnF10(htststDto.getnF10().add(dto.getnF10()));
                htststDto.setnF15(htststDto.getnF15().add(dto.getnF15()));
                htststDto.setnF20(htststDto.getnF20().add(dto.getnF20()));
                htststDto.setnF25(htststDto.getnF25().add(dto.getnF25()));
                htststDto.setnF30(htststDto.getnF30().add(dto.getnF30()));
                htststDto.setnF35(htststDto.getnF35().add(dto.getnF35()));
                htststDto.setnF40(htststDto.getnF40().add(dto.getnF40()));
                htststDto.setnF45(htststDto.getnF45().add(dto.getnF45()));
                htststDto.setnF50(htststDto.getnF50().add(dto.getnF50()));

                htststDto.setnMU(htststDto.getnMU().add(dto.getnMU()));
                htststDto.setnM0(htststDto.getnM0().add(dto.getnM0()));
                htststDto.setnM1(htststDto.getnM1().add(dto.getnM1()));
                htststDto.setnM5(htststDto.getnM5().add(dto.getnM5()));
                htststDto.setnM10(htststDto.getnM10().add(dto.getnM10()));
                htststDto.setnM15(htststDto.getnM15().add(dto.getnM15()));
                htststDto.setnM20(htststDto.getnM20().add(dto.getnM20()));
                htststDto.setnM25(htststDto.getnM25().add(dto.getnM25()));
                htststDto.setnM30(htststDto.getnM30().add(dto.getnM30()));
                htststDto.setnM35(htststDto.getnM35().add(dto.getnM35()));
                htststDto.setnM40(htststDto.getnM40().add(dto.getnM40()));
                htststDto.setnM45(htststDto.getnM45().add(dto.getnM45()));
                htststDto.setnM50(htststDto.getnM50().add(dto.getnM50()));

                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),htststDto);
            }else{
                hashtable.put(dto.getOrgId()+"-"+dto.getModality(),dto);
            }
        }


        return  new ArrayList<>(hashtable.values());
    }
    private ImportResultDto<HTSImportDto> saveHTSFromExcel(List<HTSImportDto> listDtoImport) {
        if (!CollectionUtils.isEmpty(listDtoImport)) {
            ImportResultDto<HTSImportDto> ret = new ImportResultDto<HTSImportDto>();
            ret.setTotalRow(listDtoImport.size());
            int totalErr = 0;
            int totalSuccess = 0;
            Organization org = null;
            AdminUnit district = null;
            AdminUnit province = null;
            String err = "";
            HTSCase entity = null;
            Set<HTSCaseRiskGroup> htsRisks = null;
            List<HTSImportDto> listErr = new ArrayList<HTSImportDto>();
            List<HTSImportDto> listDuplicateCode = new ArrayList<HTSImportDto>();
            List<HTSImportDto> listSuccess = new ArrayList<HTSImportDto>();
            for (HTSImportDto dto : listDtoImport) {
                err = "";
                org = null;
                district = null;
                province = null;
                entity = null;
                htsRisks = null;

                if (StringUtils.isEmpty(dto.getC6())) {
                    err += " mã bệnh nhân không được rỗng, ";
                    dto.setErr(err);
                    totalErr++;
                    listErr.add(dto);
                    continue;
                } else {
                    entity = htsCaseRepository.findByC6(dto.getC6().trim());
                    if (entity == null) {
                        entity = new HTSCase();
                        entity.setC6(dto.getC6());
                    } else {
                        err += " mã số khách hàng bị trùng,";
                        dto.setErr(err);
                        listDuplicateCode.add(dto);
                    }
                }

                if (StringUtils.isEmpty(dto.getCodeOrg())) {
                    err += " cơ sở điều trị không được rỗng,";
                    dto.setErr(err);
                    totalErr++;
                    listErr.add(dto);
                    continue;
                } else {
                    org = organizationRepository.findByOrgCode(dto.getCodeOrg().trim());
                    if (org == null) {
                        err += " không tìm thấy cơ sở điều trị, ";
                        dto.setErr(err);
                        totalErr++;
                        listErr.add(dto);
                        continue;
                    }
                    entity.setC2(org);
                }

                if (dto.getC4() != null) {
                    try {
                        entity.setC4(dto.getC4().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                    } catch (Exception e) {
                        System.out.println(
                                "convert date to localdatetime bị lỗi ở bản ghi có mã số bệnh nhân: " + dto.getC6());
                    }
                }
                entity.setC23FullName(dto.getC23FullName());
                if (dto.getC8() != null && dto.getC8() > 0) {
                    LocalDateTime dob = LocalDateTime.of(dto.getC8(), 6, 15, 0, 0);
                    entity.setC8(dob);
                }
                if (!StringUtils.isEmpty(dto.getGender())) {
                    if (dto.getGender().trim().equalsIgnoreCase("nam")) {
                        entity.setC7(Gender.MALE);
                    } else if (dto.getGender().trim().equalsIgnoreCase("nữ")) {
                        entity.setC7(Gender.FEMALE);
                    }
                }
                entity.setC23IdNumber(dto.getC23IdNumber());
                entity.setC23HealthNumber(dto.getC23HealthNumber());
                entity.setC23CurrentAddressDetail(dto.getC23CurrentAddressDetail());
                entity.setC23CurrentAddressWard(dto.getC23CurrentAddressCommune());
//				if (!StringUtils.isEmpty(dto.getC23CurrentAddressDistrict())) {
//					List<AdminUnit> adList = adminUnitRepository.findByName(dto.getC23CurrentAddressDistrict().trim());
//					if (!CollectionUtils.isEmpty(adList)) {
//						district = adList.get(0);
//						entity.setC23CurrentAddressDistrict(district);
//						if (district.getParent() != null) {
//							entity.setC23CurrentAddressProvince(district.getParent());
//						}
//					}
//				}
//				if (!StringUtils.isEmpty(dto.getC23CurrentAddressProvince()) && district != null
//						&& district.getParent() != null) {
//					List<AdminUnit> adList = adminUnitRepository.findByName(dto.getC23CurrentAddressProvince().trim());
//					if (!CollectionUtils.isEmpty(adList)) {
//						province = adList.get(0);
//						entity.setC23CurrentAddressProvince(province);
//					}
//				}
                if (!StringUtils.isEmpty(dto.getC23CurrentAddressProvince())) {
                    List<AdminUnit> adList = adminUnitRepository.findByName(dto.getC23CurrentAddressProvince().trim());
                    if (!CollectionUtils.isEmpty(adList)) {
                        province = adList.get(0);
                        entity.setC23CurrentAddressProvince(province);
                    }
                }

                if (!StringUtils.isEmpty(dto.getC23CurrentAddressDistrict()) && entity.getC23CurrentAddressProvince() != null) {
                    List<AdminUnit> adList = adminUnitRepository.findByName(dto.getC23CurrentAddressDistrict().trim());
                    List<AdminUnit> districtTemp = new ArrayList<>();
                    if (!CollectionUtils.isEmpty(adList)) {
                        if (adList.size() > 1) {
                            HTSCase finalEntity = entity;
                            adList.forEach(e -> {
                                if (e.getParent().getId() == finalEntity.getC23CurrentAddressProvince().getId()) {
                                    districtTemp.add(e);
                                }
                            });
                            district = districtTemp.get(0);
                        } else {
                            district = adList.get(0);
                        }
                        entity.setC23CurrentAddressDistrict(district);
                        if (district.getParent() != null) {
                            entity.setC23CurrentAddressProvince(district.getParent());
                        }
                    }
                }

                htsRisks = new HashSet<HTSCaseRiskGroup>();
                String c9Note = "";
                if (!StringUtils.isEmpty(dto.getRisk1())) {
                    HTSCaseRiskGroup rg = new HTSCaseRiskGroup();
                    rg.setHtsCase(entity);
                    rg.setName(HTSRiskGroupEnum.answer1.getDescription());
                    rg.setVal(HTSRiskGroupEnum.answer1);
                    htsRisks.add(rg);
                }
                if (!StringUtils.isEmpty(dto.getRisk2())) {
                    HTSCaseRiskGroup rg = new HTSCaseRiskGroup();
                    rg.setHtsCase(entity);
                    rg.setName(HTSRiskGroupEnum.answer3.getDescription());
                    rg.setVal(HTSRiskGroupEnum.answer3);
                    htsRisks.add(rg);

                    rg = new HTSCaseRiskGroup();
                    rg.setHtsCase(entity);
                    rg.setName(HTSRiskGroupEnum.answer9.getDescription());
                    rg.setVal(HTSRiskGroupEnum.answer9);
                    htsRisks.add(rg);
                }
                if (!StringUtils.isEmpty(dto.getRisk3())) {
                    HTSCaseRiskGroup rg = new HTSCaseRiskGroup();
                    rg.setHtsCase(entity);
                    rg.setName(HTSRiskGroupEnum.answer10.getDescription());
                    rg.setVal(HTSRiskGroupEnum.answer10);
                    htsRisks.add(rg);
                }
                if (!StringUtils.isEmpty(dto.getRisk4())) {
                    HTSCaseRiskGroup rg = new HTSCaseRiskGroup();
                    rg.setHtsCase(entity);
                    rg.setName(HTSRiskGroupEnum.answer2.getDescription());
                    rg.setVal(HTSRiskGroupEnum.answer2);
                    htsRisks.add(rg);
                }
                if (!StringUtils.isEmpty(dto.getRisk5())) {
                    HTSCaseRiskGroup rg = new HTSCaseRiskGroup();
                    rg.setHtsCase(entity);
                    rg.setName(HTSRiskGroupEnum.answer13.getDescription());
                    rg.setVal(HTSRiskGroupEnum.answer13);
                    htsRisks.add(rg);
                }
                if (!StringUtils.isEmpty(dto.getRisk6())) {
                    HTSCaseRiskGroup rg = new HTSCaseRiskGroup();
                    rg.setHtsCase(entity);
                    rg.setName(HTSRiskGroupEnum.answer16.getDescription());
                    rg.setVal(HTSRiskGroupEnum.answer16);
                    if (StringUtils.isEmpty(c9Note)) {
                        htsRisks.add(rg);
                    }
                    c9Note += "Truyền máu; ";
                }
                if (!StringUtils.isEmpty(dto.getRisk7())) {
                    HTSCaseRiskGroup rg = new HTSCaseRiskGroup();
                    rg.setHtsCase(entity);
                    rg.setName(HTSRiskGroupEnum.answer16.getDescription());
                    rg.setVal(HTSRiskGroupEnum.answer16);
                    if (StringUtils.isEmpty(c9Note)) {
                        htsRisks.add(rg);
                    }
                    c9Note += "Rủi ro nghề nghiệp; ";
                }
                if (!StringUtils.isEmpty(dto.getRisk8())) {
                    HTSCaseRiskGroup rg = new HTSCaseRiskGroup();
                    rg.setHtsCase(entity);
                    rg.setName(HTSRiskGroupEnum.answer16.getDescription());
                    rg.setVal(HTSRiskGroupEnum.answer16);
                    if (StringUtils.isEmpty(c9Note)) {
                        htsRisks.add(rg);
                    }
                    c9Note += "Qua đường máu khác; ";
                }
                if (!StringUtils.isEmpty(dto.getRisk9())) {
                    HTSCaseRiskGroup rg = new HTSCaseRiskGroup();
                    rg.setHtsCase(entity);
                    rg.setName(HTSRiskGroupEnum.answer5.getDescription());
                    rg.setVal(HTSRiskGroupEnum.answer5);
                    htsRisks.add(rg);
                }
                if (!StringUtils.isEmpty(dto.getRisk10())) {
                    HTSCaseRiskGroup rg = new HTSCaseRiskGroup();
                    rg.setHtsCase(entity);
                    rg.setName(HTSRiskGroupEnum.answer16.getDescription());
                    rg.setVal(HTSRiskGroupEnum.answer16);
                    if (StringUtils.isEmpty(c9Note)) {
                        htsRisks.add(rg);
                    }
                    c9Note += "Vợ, chồng, bạn tình thuộc nhóm nguy cơ cao; ";
                }
                if (!StringUtils.isEmpty(dto.getRisk11())) {
                    HTSCaseRiskGroup rg = new HTSCaseRiskGroup();
                    rg.setHtsCase(entity);
                    rg.setName(HTSRiskGroupEnum.answer12.getDescription());
                    rg.setVal(HTSRiskGroupEnum.answer12);
                    htsRisks.add(rg);
                }
                if (!StringUtils.isEmpty(dto.getRisk12())) {
                    HTSCaseRiskGroup rg = new HTSCaseRiskGroup();
                    rg.setHtsCase(entity);
                    rg.setName(HTSRiskGroupEnum.answer16.getDescription());
                    rg.setVal(HTSRiskGroupEnum.answer16);
                    if (StringUtils.isEmpty(c9Note)) {
                        htsRisks.add(rg);
                    }
                }
                if (!StringUtils.isEmpty(dto.getRisk13())) {
                    HTSCaseRiskGroup rg = new HTSCaseRiskGroup();
                    rg.setHtsCase(entity);
                    rg.setName(HTSRiskGroupEnum.answer15.getDescription());
                    rg.setVal(HTSRiskGroupEnum.answer15);
                    htsRisks.add(rg);
                }

                entity.getC9().clear();
                entity.getC9().addAll(htsRisks);
                if (StringUtils.hasText(c9Note)) {
                    entity.setC9Note(c9Note);
                } else {
                    entity.setC9Note(null);
                }
                if (entity.getC9() != null && entity.getC9().size() > 0) {
                    int minRisk = 16;
                    HTSCaseRiskGroup htsCaseMinRiskGroup = null;
                    for (HTSCaseRiskGroup htsCaseRiskGroup : entity.getC9()) {
                        htsCaseRiskGroup.setIsMainRisk(false);
                        if (htsCaseRiskGroup.getVal().getPriority() > 0
                                && htsCaseRiskGroup.getVal().getPriority() <= minRisk) {
                            minRisk = htsCaseRiskGroup.getVal().getPriority();
                            htsCaseMinRiskGroup = htsCaseRiskGroup;
                        }
                    }
                    if (htsCaseMinRiskGroup != null) {
                        htsCaseMinRiskGroup.setIsMainRisk(true);
                    }
                }

                if (!StringUtils.isEmpty(dto.getHIVResult1())) {
                    if (dto.getHIVResult1().trim().equalsIgnoreCase("DT")) {
                        entity.setC11(HTSYesNoNone.YES);
                    } else if (dto.getHIVResult1().trim().equalsIgnoreCase("AT")) {
                        entity.setC11(HTSYesNoNone.NO);
                    } else {
                        entity.setC11(HTSYesNoNone.NO_INFORMATION);
                    }
                }
                if (!StringUtils.isEmpty(dto.getC14())) {
                    if (dto.getC14().trim().equalsIgnoreCase("DT")) {
                        entity.setC14(HTSc14.answer2);
                    } else if (dto.getC14().trim().equalsIgnoreCase("AT")) {
                        entity.setC14(HTSc14.answer1);
                    } else {
                        entity.setC14(HTSc14.answer3);
                    }
                }
                if (dto.getC15Date() != null) {
                    entity.setC15(HTSYesNoNone.YES);
                    try {
                        entity.setC15Date(
                                dto.getC15Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                    } catch (Exception e) {
                        System.out.println(
                                "convert date to localdatetime bị lỗi ở bản ghi có mã số bệnh nhân: " + dto.getC6());
                    }
                }
                if (!StringUtils.isEmpty(dto.getC17())) {
                    if (dto.getC17().trim().equalsIgnoreCase("R+")) {
                        entity.setC17(HTSc17.answer1);
                    } else if (dto.getC17().trim().equalsIgnoreCase("LT")) {
                        entity.setC17(HTSc17.answer2);
                    } else {
                        entity.setC17(HTSc17.answer5);
                    }
                }

                if (!StringUtils.isEmpty(dto.getC18())) {
                    try {
                        Float rs = Float.parseFloat(dto.getC18());
                        if (rs >= 1000) {
                            entity.setC18(HTSc18.answer2);
                        } else {
                            entity.setC18(HTSc18.answer1);
                        }
                    } catch (Exception e) {
                        if (dto.getC18().trim().equalsIgnoreCase("Không làm xét nghiệm")) {
                            entity.setC18(HTSc18.answer3);
                        } else if (dto.getC18().trim().equalsIgnoreCase("Đang chờ KQXN")) {
                            entity.setC18(HTSc18.answer4);
                        } else {
                            entity.setC18(HTSc18.UNKNOWN);
                        }
                    }
                }

                if (dto.getARV() != null) {
                    entity.setC20(HTSc20.answer3);
                    try {
                        entity.setC20RegDate(
                                dto.getARV().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                    } catch (Exception e) {
                        System.out.println("Lỗi convert từ date sang localdate của ngày đk điều trị arv với mã bệnh nhân là: " + entity.getC6());
                    }
                }

                if (!StringUtils.isEmpty(dto.getPlaceARV())) {
                    entity.setC20(HTSc20.answer3);
                    entity.setC20Org(dto.getPlaceARV());
                }

                if (!StringUtils.isEmpty(dto.getIsPNS())) {
                    if (dto.getIsPNS().trim().equalsIgnoreCase("Có")) {
                        entity.setC21(HTSYesNoNone.YES);
                    } else if (dto.getIsPNS().trim().equalsIgnoreCase("Không")) {
                        entity.setC21(HTSYesNoNone.NO);
                    } else {
                        entity.setC21(HTSYesNoNone.NO_INFORMATION);
                    }
                }

                if (dto.getContactPNS() != null) {
                    entity.setTotalIsTestedHiv(dto.getContactPNS());
                }

                if (dto.getContactPNSHIV() != null) {
                    entity.setTotalHivPositiveResult(dto.getContactPNSHIV());
                }

                if (!StringUtils.isEmpty(dto.getNote())) {
                    entity.setNote(dto.getNote());
                }

                entity.setImportedFromHtsNew(true);

                if (entity.getC15() == HTSYesNoNone.NO_INFORMATION || entity.getC18() == HTSc18.answer4
                        || entity.getC24() == HTSc24.answer3 || (entity.getC14() == HTSc14.answer1
                        && entity.getC15() == HTSYesNoNone.YES && entity.getC1627() == null)
                        || entity.getC20() == HTSc20.answer1) {
                    entity.setNotComplete(true);
                } else {
                    entity.setNotComplete(false);
                }
                entity = htsCaseRepository.save(entity);
                if (entity != null) {
                    totalSuccess++;
                    listSuccess.add(dto);
                }
            }
            ret.setTotalErr(totalErr);
            ret.setTotalSuccess(totalSuccess);
            ret.setListErr(listErr);
            ret.setListDuplicateCode(listDuplicateCode);
            ret.setListSuccess(listSuccess);
            return ret;
        }
        return null;
    }

    private Workbook exportErrorHTSCase(ImportResultDto<HTSImportDto> rs) {
        Workbook blankBook = new XSSFWorkbook();
        blankBook.createSheet();
        if (rs == null) {
            return blankBook;
        }
        Workbook wbook = null;
        try (InputStream template = context.getResource("classpath:templates/hts-error.xlsx").getInputStream()) {
            wbook = new XSSFWorkbook(template);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (wbook == null) {
            return blankBook;
        }
        int rowIndex = 10;
        int colIndex = 0;

        Row row = null;
        Cell cell = null;
        Sheet sheet = wbook.getSheetAt(0);

        int seq = 0;
        CellStyle cellStyle = wbook.createCellStyle();
        ExcelUtils.setBorders4Style(cellStyle);
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        CellStyle dateTimeStyle = wbook.createCellStyle();
        DataFormat format = wbook.createDataFormat();
        dateTimeStyle.setDataFormat(format.getFormat("dd/MM/yyyy"));
        dateTimeStyle.setAlignment(HorizontalAlignment.LEFT);
        dateTimeStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        ExcelUtils.setBorders4Style(dateTimeStyle);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        row = sheet.getRow(2);
        cell = row.getCell(3);
        cell.setCellValue(rs.getTotalRow());
        cell.setCellStyle(cellStyle);

        row = sheet.getRow(3);
        cell = row.getCell(3);
        cell.setCellValue(rs.getTotalSuccess());
        cell.setCellStyle(cellStyle);

        row = sheet.getRow(4);
        cell = row.getCell(3);
        cell.setCellValue(rs.getTotalErr());
        cell.setCellStyle(cellStyle);

        row = sheet.getRow(5);
        cell = row.getCell(3);
        cell.setCellValue(rs.getListDuplicateCode().size());
        cell.setCellStyle(cellStyle);

        rs.getListErr().addAll(rs.getListDuplicateCode());
        rs.getListErr().addAll(rs.getListSuccess());
        for (HTSImportDto dto : rs.getListErr()) {
            row = sheet.createRow(rowIndex++);

            // stt
            cell = row.createCell(colIndex++);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(seq++);

            // mã số cơ sở tư vấn xét nghiệm
            cell = row.createCell(colIndex++);
            cell.setCellStyle(cellStyle);
            try {
                cell.setCellValue(dto.getCodeOrg());
            } catch (Exception e) {
                cell.setCellValue("");
            }

            cell = row.createCell(colIndex++);
            cell.setCellStyle(dateTimeStyle);
            try {
                cell.setCellValue(formatter.format(dto.getC4()));
            } catch (Exception e) {
                cell.setCellValue("");
            }

            cell = row.createCell(colIndex++);
            cell.setCellStyle(cellStyle);
            try {
                cell.setCellValue(dto.getC6());
            } catch (Exception e) {
                cell.setCellValue("");
            }

            cell = row.createCell(colIndex++);
            cell.setCellStyle(cellStyle);
            try {
                cell.setCellValue(dto.getC23FullName());
            } catch (Exception e) {
                cell.setCellValue("");
            }

            cell = row.createCell(colIndex++);
            cell.setCellStyle(cellStyle);
            try {
                cell.setCellValue(dto.getGender());
            } catch (Exception e) {
                cell.setCellValue("");
            }

            cell = row.createCell(colIndex++);
            cell.setCellStyle(cellStyle);
            try {
                cell.setCellValue(dto.getC8());
            } catch (Exception e) {
                cell.setCellValue("");
            }

            cell = row.createCell(colIndex++);
            cell.setCellStyle(cellStyle);
            try {
                cell.setCellValue(dto.getC23IdOrHealth());
            } catch (Exception e) {
                cell.setCellValue("");
            }

            cell = row.createCell(colIndex++);
            cell.setCellStyle(cellStyle);
            try {
                cell.setCellValue(dto.getC23CurrentAddressProvince());
            } catch (Exception e) {
                cell.setCellValue("");
            }

            cell = row.createCell(colIndex++);
            cell.setCellStyle(cellStyle);
            try {
                cell.setCellValue(dto.getC23CurrentAddressDistrict());
            } catch (Exception e) {
                cell.setCellValue("");
            }

            cell = row.createCell(colIndex++);
            cell.setCellStyle(cellStyle);
            try {
                cell.setCellValue(dto.getC23CurrentAddressCommune());
            } catch (Exception e) {
                cell.setCellValue("");
            }

            cell = row.createCell(colIndex++);
            cell.setCellStyle(cellStyle);
            try {
                cell.setCellValue(dto.getC23CurrentAddressDetail());
            } catch (Exception e) {
                cell.setCellValue("");
            }

            cell = row.createCell(colIndex++);
            cell.setCellStyle(cellStyle);
            try {
                cell.setCellValue(dto.getRisk1());
            } catch (Exception e) {
                cell.setCellValue("");
            }

            cell = row.createCell(colIndex++);
            cell.setCellStyle(cellStyle);
            try {
                cell.setCellValue(dto.getRisk2());
            } catch (Exception e) {
                cell.setCellValue("");
            }

            cell = row.createCell(colIndex++);
            cell.setCellStyle(cellStyle);
            try {
                cell.setCellValue(dto.getRisk3());
            } catch (Exception e) {
                cell.setCellValue("");
            }

            cell = row.createCell(colIndex++);
            cell.setCellStyle(cellStyle);
            try {
                cell.setCellValue(dto.getRisk4());
            } catch (Exception e) {
                cell.setCellValue("");
            }

            cell = row.createCell(colIndex++);
            cell.setCellStyle(cellStyle);
            try {
                cell.setCellValue(dto.getRisk5());
            } catch (Exception e) {
                cell.setCellValue("");
            }

            cell = row.createCell(colIndex++);
            cell.setCellStyle(cellStyle);
            try {
                cell.setCellValue(dto.getRisk6());
            } catch (Exception e) {
                cell.setCellValue("");
            }

            cell = row.createCell(colIndex++);
            cell.setCellStyle(cellStyle);
            try {
                cell.setCellValue(dto.getRisk7());
            } catch (Exception e) {
                cell.setCellValue("");
            }

            cell = row.createCell(colIndex++);
            cell.setCellStyle(cellStyle);
            try {
                cell.setCellValue(dto.getRisk8());
            } catch (Exception e) {
                cell.setCellValue("");
            }

            cell = row.createCell(colIndex++);
            cell.setCellStyle(cellStyle);
            try {
                cell.setCellValue(dto.getRisk9());
            } catch (Exception e) {
                cell.setCellValue("");
            }

            cell = row.createCell(colIndex++);
            cell.setCellStyle(cellStyle);
            try {
                cell.setCellValue(dto.getRisk10());
            } catch (Exception e) {
                cell.setCellValue("");
            }

            cell = row.createCell(colIndex++);
            cell.setCellStyle(cellStyle);
            try {
                cell.setCellValue(dto.getRisk11());
            } catch (Exception e) {
                cell.setCellValue("");
            }

            cell = row.createCell(colIndex++);
            cell.setCellStyle(cellStyle);
            try {
                cell.setCellValue(dto.getRisk12());
            } catch (Exception e) {
                cell.setCellValue("");
            }

            cell = row.createCell(colIndex++);
            cell.setCellStyle(cellStyle);
            try {
                cell.setCellValue(dto.getRisk13());
            } catch (Exception e) {
                cell.setCellValue("");
            }

            cell = row.createCell(colIndex++);
            cell.setCellStyle(cellStyle);
            try {
                cell.setCellValue(dto.getHIVResult1());
            } catch (Exception e) {
                cell.setCellValue("");
            }

            cell = row.createCell(colIndex++);
            cell.setCellStyle(cellStyle);
            try {
                cell.setCellValue(dto.getC14());
            } catch (Exception e) {
                cell.setCellValue("");
            }

            cell = row.createCell(colIndex++);
            cell.setCellStyle(cellStyle);
            try {
                cell.setCellValue(dto.getC17());
            } catch (Exception e) {
                cell.setCellValue("");
            }

            cell = row.createCell(colIndex++);
            cell.setCellStyle(cellStyle);
            try {
                cell.setCellValue(dto.getC18());
            } catch (Exception e) {
                cell.setCellValue("");
            }

            cell = row.createCell(colIndex++);
            cell.setCellStyle(dateTimeStyle);
            try {
                cell.setCellValue(formatter.format(dto.getC15Date()));
            } catch (Exception e) {
                cell.setCellValue("");
            }

            cell = row.createCell(colIndex++);
            cell.setCellStyle(dateTimeStyle);
            try {
                cell.setCellValue(formatter.format(dto.getC15Date()));
            } catch (Exception e) {
                cell.setCellValue("");
            }

            cell = row.createCell(colIndex++);
            cell.setCellStyle(cellStyle);
            try {
                cell.setCellValue(dto.getPlaceARV());
            } catch (Exception e) {
                cell.setCellValue("");
            }

            cell = row.createCell(colIndex++);
            cell.setCellStyle(cellStyle);
            try {
                cell.setCellValue(dto.getIsPNS());
            } catch (Exception e) {
                cell.setCellValue("");
            }

            cell = row.createCell(colIndex++);
            cell.setCellStyle(cellStyle);
            try {
                cell.setCellValue(dto.getContactPNS());
            } catch (Exception e) {
                cell.setCellValue("");
            }

            cell = row.createCell(colIndex++);
            cell.setCellStyle(cellStyle);
            try {
                cell.setCellValue(dto.getContactPNSHIV());
            } catch (Exception e) {
                cell.setCellValue("");
            }

            cell = row.createCell(colIndex++);
            cell.setCellStyle(cellStyle);
            try {
                cell.setCellValue(dto.getNote());
            } catch (Exception e) {
                cell.setCellValue("");
            }

            cell = row.createCell(colIndex++);
            cell.setCellStyle(cellStyle);
            try {
                cell.setCellValue(dto.getErr());
            } catch (Exception e) {
                cell.setCellValue("");
            }

            colIndex = 0;

        }
        for (int i = 0; i < 38; i++) {
            sheet.autoSizeColumn(i);
        }

        return wbook;
    }

    private List<HTSRecentDto> getDataHTSRecent(PreventionFilterDto filter) {
        HashMap<String, HTSRecentDto> hashTable = new HashMap<>();
        List<HTSRecentDto> retHTS1 = reportMerService.getDataFacilitySNSRTRIRecent(filter);
        List<HTSRecentDto> retHTS2 = reportMerService.getDataFacilitySNSRTRILongTerm(filter);
        List<HTSRecentDto> retHTS3 = reportMerService.getDataFacilitySNSRITARecent(filter);
        List<HTSRecentDto> retHTS4 = reportMerService.getDataFacilitySNSRITALongTerm(filter);
        List<HTSRecentDto> retHTS5 = reportMerService.getDataVCTRTRIRecent(filter);
        List<HTSRecentDto> retHTS6 = reportMerService.getDataVCTRTRILongTerm(filter);
        List<HTSRecentDto> retHTS7 = reportMerService.getDataVCTRITARecent(filter);
        List<HTSRecentDto> retHTS8 = reportMerService.getDataVCTRITALongTerm(filter);
        List<HTSRecentDto> retHTS9 = reportMerService.getDataFacilityIndexTestingRTRIRecent(filter);
        List<HTSRecentDto> retHTS10 = reportMerService.getDataFacilityIndexTestingRTRILongTerm(filter);
        List<HTSRecentDto> retHTS11 = reportMerService.getDataFacilityIndexTestingRITARecent(filter);
        List<HTSRecentDto> retHTS12 = reportMerService.getDataFacilityIndexTestingRITALongTerm(filter);
        
        HTSRecentDto htsRecentDto = null;
        for (HTSRecentDto dto : retHTS1) {
            if (hashTable.containsKey(dto.getOrgId() + "-" + dto.getModality())) {
                htsRecentDto = hashTable.get(htsRecentDto.getOrgId() + "-" + dto.getModality());
                htsRecentDto.setPwidRTRI(htsRecentDto.getPwidRTRI().add(dto.getPwidRTRI()));
                htsRecentDto.setMsmRTRI(htsRecentDto.getMsmRTRI().add(dto.getMsmRTRI()));
                htsRecentDto.setTgRTRI(htsRecentDto.getTgRTRI().add(dto.getTgRTRI()));
                htsRecentDto.setFswRTRI(htsRecentDto.getFswRTRI().add(dto.getOtherRTRI()));
                htsRecentDto.setOtherRTRI(htsRecentDto.getOtherRTRI().add(dto.getOtherRTRI()));

                htsRecentDto.setFuRTRI(htsRecentDto.getFuRTRI().add(dto.getFuRTRI()));
                htsRecentDto.setF15RTRI(htsRecentDto.getF15RTRI().add(dto.getF15RTRI()));
                htsRecentDto.setF20RTRI(htsRecentDto.getF20RTRI().add(dto.getF20RTRI()));
                htsRecentDto.setF25RTRI(htsRecentDto.getF25RTRI().add(dto.getF25RTRI()));
                htsRecentDto.setF30RTRI(htsRecentDto.getF30RTRI().add(dto.getF30RTRI()));
                htsRecentDto.setF35RTRI(htsRecentDto.getF35RTRI().add(dto.getF35RTRI()));
                htsRecentDto.setF40RTRI(htsRecentDto.getF40RTRI().add(dto.getF40RTRI()));
                htsRecentDto.setF45RTRI(htsRecentDto.getF45RTRI().add(dto.getF45RTRI()));
                htsRecentDto.setF50RTRI(htsRecentDto.getF50RTRI().add(dto.getF50RTRI()));

                htsRecentDto.setMuRTRI(htsRecentDto.getMuRTRI().add(dto.getMuRTRI()));
                htsRecentDto.setM15RTRI(htsRecentDto.getM15RTRI().add(dto.getM15RTRI()));
                htsRecentDto.setM20RTRI(htsRecentDto.getM20RTRI().add(dto.getM20RTRI()));
                htsRecentDto.setM25RTRI(htsRecentDto.getM25RTRI().add(dto.getM25RTRI()));
                htsRecentDto.setM30RTRI(htsRecentDto.getM30RTRI().add(dto.getM30RTRI()));
                htsRecentDto.setM35RTRI(htsRecentDto.getM35RTRI().add(dto.getM35RTRI()));
                htsRecentDto.setM40RTRI(htsRecentDto.getM40RTRI().add(dto.getM40RTRI()));
                htsRecentDto.setM45RTRI(htsRecentDto.getM45RTRI().add(dto.getM45RTRI()));
                htsRecentDto.setM50RTRI(htsRecentDto.getM50RTRI().add(dto.getM50RTRI()));

                hashTable.put(dto.getOrgId()+"-"+dto.getModality(),htsRecentDto);
            } else {
                hashTable.put(dto.getOrgId() + "-" + dto.getModality(), dto);
            }
        }

        for (HTSRecentDto dto : retHTS2) {
            if (hashTable.containsKey(dto.getOrgId() + "-" + dto.getModality())) {
//            	HTSRecentDto obj = hashTable.get(dto.getOrgId() + "-" + dto.getModality());
//            	obj.setPwidRTRILongTerm(obj.getPwidRTRILongTerm().add(dto.getPwidRTRILongTerm()));
                htsRecentDto = hashTable.get(htsRecentDto.getOrgId() + "-" + dto.getModality());

                htsRecentDto.setPwidRTRILongTerm(htsRecentDto.getPwidRTRILongTerm().add(dto.getPwidRTRILongTerm()));
                htsRecentDto.setMsmRTRILongTerm(htsRecentDto.getMsmRTRILongTerm().add(dto.getMsmRTRILongTerm()));
                htsRecentDto.setTgRTRILongTerm(htsRecentDto.getTgRTRILongTerm().add(dto.getTgRTRILongTerm()));
                htsRecentDto.setFswRTRILongTerm(htsRecentDto.getFswRTRILongTerm().add(dto.getFswRTRILongTerm()));
                htsRecentDto.setOtherRTRILongTerm(htsRecentDto.getOtherRTRILongTerm().add(dto.getOtherRTRILongTerm()));

                htsRecentDto.setFuRTRILongTerm(htsRecentDto.getFuRTRILongTerm().add(dto.getFuRTRILongTerm()));
                htsRecentDto.setF15RTRILongTerm(htsRecentDto.getF15RTRILongTerm().add(dto.getF15RTRILongTerm()));
                htsRecentDto.setF20RTRILongTerm(htsRecentDto.getF20RTRILongTerm().add(dto.getF20RTRILongTerm()));
                htsRecentDto.setF25RTRILongTerm(htsRecentDto.getF25RTRILongTerm().add(dto.getF25RTRILongTerm()));
                htsRecentDto.setF30RTRILongTerm(htsRecentDto.getF30RTRILongTerm().add(dto.getF30RTRILongTerm()));
                htsRecentDto.setF35RTRILongTerm(htsRecentDto.getF35RTRILongTerm().add(dto.getF35RTRILongTerm()));
                htsRecentDto.setF40RTRILongTerm(htsRecentDto.getF40RTRILongTerm().add(dto.getF40RTRILongTerm()));
                htsRecentDto.setF45RTRILongTerm(htsRecentDto.getF45RTRILongTerm().add(dto.getF45RTRILongTerm()));
                htsRecentDto.setF50RTRILongTerm(htsRecentDto.getF50RTRILongTerm().add(dto.getF50RTRILongTerm()));

                htsRecentDto.setMuRTRILongTerm(htsRecentDto.getMuRTRILongTerm().add(dto.getMuRTRILongTerm()));
                htsRecentDto.setM15RTRILongTerm(htsRecentDto.getM15RTRILongTerm().add(dto.getM15RTRILongTerm()));
                htsRecentDto.setM20RTRILongTerm(htsRecentDto.getM20RTRILongTerm().add(dto.getM20RTRILongTerm()));
                htsRecentDto.setM25RTRILongTerm(htsRecentDto.getM25RTRILongTerm().add(dto.getM25RTRILongTerm()));
                htsRecentDto.setM30RTRILongTerm(htsRecentDto.getM30RTRILongTerm().add(dto.getM30RTRILongTerm()));
                htsRecentDto.setM35RTRILongTerm(htsRecentDto.getM35RTRILongTerm().add(dto.getM35RTRILongTerm()));
                htsRecentDto.setM40RTRILongTerm(htsRecentDto.getM40RTRILongTerm().add(dto.getM40RTRILongTerm()));
                htsRecentDto.setM45RTRILongTerm(htsRecentDto.getM45RTRILongTerm().add(dto.getM45RTRILongTerm()));
                htsRecentDto.setM50RTRILongTerm(htsRecentDto.getM50RTRILongTerm().add(dto.getM50RTRILongTerm()));

                hashTable.put(dto.getOrgId()+"-"+dto.getModality(),htsRecentDto);
            } else {
                hashTable.put(dto.getOrgId() + "-" + dto.getModality(), dto);
            }
        }

        for (HTSRecentDto dto : retHTS3) {
            if (hashTable.containsKey(dto.getOrgId() + "-" + dto.getModality())) {
                htsRecentDto = hashTable.get(dto.getOrgId() + "-" + dto.getModality());

                htsRecentDto.setPwidRITA(htsRecentDto.getPwidRITA().add(dto.getPwidRITA()));
                htsRecentDto.setMsmRITA(htsRecentDto.getMsmRITA().add(dto.getMsmRITA()));
                htsRecentDto.setTgRITA(htsRecentDto.getTgRITA().add(dto.getTgRITA()));
                htsRecentDto.setFswRITA(htsRecentDto.getFswRITA().add(dto.getFswRITA()));
                htsRecentDto.setOtherRITA(htsRecentDto.getOtherRITA().add(dto.getOtherRITA()));

                htsRecentDto.setFuRITA(htsRecentDto.getFuRITA().add(dto.getFuRITA()));
                htsRecentDto.setF15RITA(htsRecentDto.getF15RITA().add(dto.getF15RITA()));
                htsRecentDto.setF20RITA(htsRecentDto.getF20RITA().add(dto.getF20RITA()));
                htsRecentDto.setF25RITA(htsRecentDto.getF25RITA().add(dto.getF25RITA()));
                htsRecentDto.setF30RITA(htsRecentDto.getF30RITA().add(dto.getF30RITA()));
                htsRecentDto.setF35RITA(htsRecentDto.getF35RITA().add(dto.getF35RITA()));
                htsRecentDto.setF40RITA(htsRecentDto.getF40RITA().add(dto.getF40RITA()));
                htsRecentDto.setF45RITA(htsRecentDto.getF45RITA().add(dto.getF45RITA()));
                htsRecentDto.setF50RITA(htsRecentDto.getF50RITA().add(dto.getF50RITA()));

                htsRecentDto.setMuRITA(htsRecentDto.getMuRITA().add(dto.getMuRITA()));
                htsRecentDto.setM15RITA(htsRecentDto.getM15RITA().add(dto.getM15RITA()));
                htsRecentDto.setM20RITA(htsRecentDto.getM20RITA().add(dto.getM20RITA()));
                htsRecentDto.setM25RITA(htsRecentDto.getM25RITA().add(dto.getM25RITA()));
                htsRecentDto.setM30RITA(htsRecentDto.getM30RITA().add(dto.getM30RITA()));
                htsRecentDto.setM35RITA(htsRecentDto.getM35RITA().add(dto.getM35RITA()));
                htsRecentDto.setM40RITA(htsRecentDto.getM40RITA().add(dto.getM40RITA()));
                htsRecentDto.setM45RITA(htsRecentDto.getM45RITA().add(dto.getM45RITA()));
                htsRecentDto.setM50RITA(htsRecentDto.getM50RITA().add(dto.getM50RITA()));

                hashTable.put(dto.getOrgId()+"-"+dto.getModality(),htsRecentDto);
            } else {
                hashTable.put(dto.getOrgId() + "-" + dto.getModality(), dto);
            }
        }

        for (HTSRecentDto dto : retHTS4) {
            if (hashTable.containsKey(dto.getOrgId() + "-" + dto.getModality())) {
                htsRecentDto = hashTable.get(dto.getOrgId() + "-" + dto.getModality());

                htsRecentDto.setPwidRITALongTerm(htsRecentDto.getPwidRITALongTerm().add(dto.getPwidRITALongTerm()));
                htsRecentDto.setMsmRITALongTerm(htsRecentDto.getMsmRITALongTerm().add(dto.getMsmRITALongTerm()));
                htsRecentDto.setTgRITALongTerm(htsRecentDto.getTgRITALongTerm().add(dto.getTgRITALongTerm()));
                htsRecentDto.setFswRITALongTerm(htsRecentDto.getFswRITALongTerm().add(dto.getFswRITALongTerm()));
                htsRecentDto.setOtherRITALongTerm(htsRecentDto.getOtherRITALongTerm().add(dto.getOtherRITALongTerm()));

                htsRecentDto.setFuRITALongTerm(htsRecentDto.getFuRITALongTerm().add(dto.getFuRITALongTerm()));
                htsRecentDto.setF15RITALongTerm(htsRecentDto.getF15RITALongTerm().add(dto.getF15RITALongTerm()));
                htsRecentDto.setF20RITALongTerm(htsRecentDto.getF20RITALongTerm().add(dto.getF20RITALongTerm()));
                htsRecentDto.setF25RITALongTerm(htsRecentDto.getF25RITALongTerm().add(dto.getF25RITALongTerm()));
                htsRecentDto.setF30RITALongTerm(htsRecentDto.getF30RITALongTerm().add(dto.getF30RITALongTerm()));
                htsRecentDto.setF35RITALongTerm(htsRecentDto.getF35RITALongTerm().add(dto.getF35RITALongTerm()));
                htsRecentDto.setF40RITALongTerm(htsRecentDto.getF40RITALongTerm().add(dto.getF40RITALongTerm()));
                htsRecentDto.setF45RITALongTerm(htsRecentDto.getF45RITALongTerm().add(dto.getF45RITALongTerm()));
                htsRecentDto.setF50RITALongTerm(htsRecentDto.getF50RITALongTerm().add(dto.getF50RITALongTerm()));

                htsRecentDto.setMuRITALongTerm(htsRecentDto.getMuRITALongTerm().add(dto.getMuRITALongTerm()));
                htsRecentDto.setM15RITALongTerm(htsRecentDto.getM15RITALongTerm().add(dto.getM15RITALongTerm()));
                htsRecentDto.setM20RITALongTerm(htsRecentDto.getM20RITALongTerm().add(dto.getM20RITALongTerm()));
                htsRecentDto.setM25RITALongTerm(htsRecentDto.getM25RITALongTerm().add(dto.getM25RITALongTerm()));
                htsRecentDto.setM30RITALongTerm(htsRecentDto.getM30RITALongTerm().add(dto.getM30RITALongTerm()));
                htsRecentDto.setM35RITALongTerm(htsRecentDto.getM35RITALongTerm().add(dto.getM35RITALongTerm()));
                htsRecentDto.setM40RITALongTerm(htsRecentDto.getM40RITALongTerm().add(dto.getM40RITALongTerm()));
                htsRecentDto.setM45RITALongTerm(htsRecentDto.getM45RITALongTerm().add(dto.getM45RITALongTerm()));
                htsRecentDto.setM50RITALongTerm(htsRecentDto.getM50RITALongTerm().add(dto.getM50RITALongTerm()));

                hashTable.put(dto.getOrgId()+"-"+dto.getModality(),htsRecentDto);
            } else {
                hashTable.put(dto.getOrgId() + "-" + dto.getModality(), dto);
            }
        }

        for (HTSRecentDto dto : retHTS5) {
            if (hashTable.containsKey(dto.getOrgId() + "-" + dto.getModality())) {
                htsRecentDto = hashTable.get(dto.getOrgId() + "-" + dto.getModality());

                htsRecentDto.setPwidRTRI(htsRecentDto.getPwidRTRI().add(dto.getPwidRTRI()));
                htsRecentDto.setMsmRTRI(htsRecentDto.getMsmRTRI().add(dto.getMsmRTRI()));
                htsRecentDto.setTgRTRI(htsRecentDto.getTgRTRI().add(dto.getTgRTRI()));
                htsRecentDto.setFswRTRI(htsRecentDto.getFswRTRI().add(dto.getFswRTRI()));
                htsRecentDto.setOtherRTRI(htsRecentDto.getOtherRTRI().add(dto.getOtherRTRI()));

                htsRecentDto.setFuRTRI(htsRecentDto.getFuRTRI().add(dto.getFuRTRI()));
                htsRecentDto.setF15RTRI(htsRecentDto.getF15RTRI().add(dto.getF15RTRI()));
                htsRecentDto.setF20RTRI(htsRecentDto.getF20RTRI().add(dto.getF20RTRI()));
                htsRecentDto.setF25RTRI(htsRecentDto.getF25RTRI().add(dto.getF25RTRI()));
                htsRecentDto.setF30RTRI(htsRecentDto.getF30RTRI().add(dto.getF30RTRI()));
                htsRecentDto.setF35RTRI(htsRecentDto.getF35RTRI().add(dto.getF35RTRI()));
                htsRecentDto.setF40RTRI(htsRecentDto.getF40RTRI().add(dto.getF40RTRI()));
                htsRecentDto.setF45RTRI(htsRecentDto.getF45RTRI().add(dto.getF45RTRI()));
                htsRecentDto.setF50RTRI(htsRecentDto.getF50RTRI().add(dto.getF50RTRI()));

                htsRecentDto.setMuRTRI(htsRecentDto.getMuRTRI().add(dto.getMuRTRI()));
                htsRecentDto.setM15RTRI(htsRecentDto.getM15RTRI().add(dto.getM15RTRI()));
                htsRecentDto.setM20RTRI(htsRecentDto.getM20RTRI().add(dto.getM20RTRI()));
                htsRecentDto.setM25RTRI(htsRecentDto.getM25RTRI().add(dto.getM25RTRI()));
                htsRecentDto.setM30RTRI(htsRecentDto.getM30RTRI().add(dto.getM30RTRI()));
                htsRecentDto.setM35RTRI(htsRecentDto.getM35RTRI().add(dto.getM35RTRI()));
                htsRecentDto.setM40RTRI(htsRecentDto.getM40RTRI().add(dto.getM40RTRI()));
                htsRecentDto.setM45RTRI(htsRecentDto.getM45RTRI().add(dto.getM45RTRI()));
                htsRecentDto.setM50RTRI(htsRecentDto.getM50RTRI().add(dto.getM50RTRI()));

                hashTable.put(dto.getOrgId()+"-"+dto.getModality(),htsRecentDto);
            } else {
                hashTable.put(dto.getOrgId() + "-" + dto.getModality(), dto);
            }
        }

        for (HTSRecentDto dto : retHTS6) {
            if (hashTable.containsKey(dto.getOrgId() + "-" + dto.getModality())) {
                htsRecentDto = hashTable.get(dto.getOrgId() + "-" + dto.getModality());
                
                htsRecentDto.setPwidRTRILongTerm(htsRecentDto.getPwidRTRILongTerm().add(dto.getPwidRTRILongTerm()));
                htsRecentDto.setMsmRTRILongTerm(htsRecentDto.getMsmRTRILongTerm().add(dto.getMsmRTRILongTerm()));
                htsRecentDto.setTgRTRILongTerm(htsRecentDto.getTgRTRILongTerm().add(dto.getTgRTRILongTerm()));
                htsRecentDto.setFswRTRILongTerm(htsRecentDto.getFswRTRILongTerm().add(dto.getFswRTRILongTerm()));
                htsRecentDto.setOtherRTRILongTerm(htsRecentDto.getOtherRTRILongTerm().add(dto.getOtherRTRILongTerm()));

                htsRecentDto.setFuRTRILongTerm(htsRecentDto.getFuRTRILongTerm().add(dto.getFuRTRILongTerm()));
                htsRecentDto.setF15RTRILongTerm(htsRecentDto.getF15RTRILongTerm().add(dto.getF15RTRILongTerm()));
                htsRecentDto.setF20RTRILongTerm(htsRecentDto.getF20RTRILongTerm().add(dto.getF20RTRILongTerm()));
                htsRecentDto.setF25RTRILongTerm(htsRecentDto.getF25RTRILongTerm().add(dto.getF25RTRILongTerm()));
                htsRecentDto.setF30RTRILongTerm(htsRecentDto.getF30RTRILongTerm().add(dto.getF30RTRILongTerm()));
                htsRecentDto.setF35RTRILongTerm(htsRecentDto.getF35RTRILongTerm().add(dto.getF35RTRILongTerm()));
                htsRecentDto.setF40RTRILongTerm(htsRecentDto.getF40RTRILongTerm().add(dto.getF40RTRILongTerm()));
                htsRecentDto.setF45RTRILongTerm(htsRecentDto.getF45RTRILongTerm().add(dto.getF45RTRILongTerm()));
                htsRecentDto.setF50RTRILongTerm(htsRecentDto.getF50RTRILongTerm().add(dto.getF50RTRILongTerm()));

                htsRecentDto.setMuRTRILongTerm(htsRecentDto.getMuRTRILongTerm().add(dto.getMuRTRILongTerm()));
                htsRecentDto.setM15RTRILongTerm(htsRecentDto.getM15RTRILongTerm().add(dto.getM15RTRILongTerm()));
                htsRecentDto.setM20RTRILongTerm(htsRecentDto.getM20RTRILongTerm().add(dto.getM20RTRILongTerm()));
                htsRecentDto.setM25RTRILongTerm(htsRecentDto.getM25RTRILongTerm().add(dto.getM25RTRILongTerm()));
                htsRecentDto.setM30RTRILongTerm(htsRecentDto.getM30RTRILongTerm().add(dto.getM30RTRILongTerm()));
                htsRecentDto.setM35RTRILongTerm(htsRecentDto.getM35RTRILongTerm().add(dto.getM35RTRILongTerm()));
                htsRecentDto.setM40RTRILongTerm(htsRecentDto.getM40RTRILongTerm().add(dto.getM40RTRILongTerm()));
                htsRecentDto.setM45RTRILongTerm(htsRecentDto.getM45RTRILongTerm().add(dto.getM45RTRILongTerm()));
                htsRecentDto.setM50RTRILongTerm(htsRecentDto.getM50RTRILongTerm().add(dto.getM50RTRILongTerm()));

                hashTable.put(dto.getOrgId()+"-"+dto.getModality(),htsRecentDto);
            } else {
                hashTable.put(dto.getOrgId() + "-" + dto.getModality(), dto);
            }
        }

        for (HTSRecentDto dto : retHTS7) {
            if (hashTable.containsKey(dto.getOrgId() + "-" + dto.getModality())) {
                htsRecentDto = hashTable.get(dto.getOrgId() + "-" + dto.getModality());

                htsRecentDto.setPwidRITA(htsRecentDto.getPwidRITA().add(dto.getPwidRITA()));
                htsRecentDto.setMsmRITA(htsRecentDto.getMsmRITA().add(dto.getMsmRITA()));
                htsRecentDto.setTgRITA(htsRecentDto.getTgRITA().add(dto.getTgRITA()));
                htsRecentDto.setFswRITA(htsRecentDto.getFswRITA().add(dto.getFswRITA()));
                htsRecentDto.setOtherRITA(htsRecentDto.getOtherRITA().add(dto.getOtherRITA()));

                htsRecentDto.setFuRITA(htsRecentDto.getFuRITA().add(dto.getFuRITA()));
                htsRecentDto.setF15RITA(htsRecentDto.getF15RITA().add(dto.getF15RITA()));
                htsRecentDto.setF20RITA(htsRecentDto.getF20RITA().add(dto.getF20RITA()));
                htsRecentDto.setF25RITA(htsRecentDto.getF25RITA().add(dto.getF25RITA()));
                htsRecentDto.setF30RITA(htsRecentDto.getF30RITA().add(dto.getF30RITA()));
                htsRecentDto.setF35RITA(htsRecentDto.getF35RITA().add(dto.getF35RITA()));
                htsRecentDto.setF40RITA(htsRecentDto.getF40RITA().add(dto.getF40RITA()));
                htsRecentDto.setF45RITA(htsRecentDto.getF45RITA().add(dto.getF45RITA()));
                htsRecentDto.setF50RITA(htsRecentDto.getF50RITA().add(dto.getF50RITA()));

                htsRecentDto.setMuRITA(htsRecentDto.getMuRITA().add(dto.getMuRITA()));
                htsRecentDto.setM15RITA(htsRecentDto.getM15RITA().add(dto.getM15RITA()));
                htsRecentDto.setM20RITA(htsRecentDto.getM20RITA().add(dto.getM20RITA()));
                htsRecentDto.setM25RITA(htsRecentDto.getM25RITA().add(dto.getM25RITA()));
                htsRecentDto.setM30RITA(htsRecentDto.getM30RITA().add(dto.getM30RITA()));
                htsRecentDto.setM35RITA(htsRecentDto.getM35RITA().add(dto.getM35RITA()));
                htsRecentDto.setM40RITA(htsRecentDto.getM40RITA().add(dto.getM40RITA()));
                htsRecentDto.setM45RITA(htsRecentDto.getM45RITA().add(dto.getM45RITA()));
                htsRecentDto.setM50RITA(htsRecentDto.getM50RITA().add(dto.getM50RITA()));

                hashTable.put(dto.getOrgId()+"-"+dto.getModality(),htsRecentDto);
            } else {
                hashTable.put(dto.getOrgId() + "-" + dto.getModality(), dto);
            }
        }

        for (HTSRecentDto dto : retHTS8) {
            if (hashTable.containsKey(dto.getOrgId() + "-" + dto.getModality())) {
                htsRecentDto = hashTable.get(dto.getOrgId() + "-" + dto.getModality());

                htsRecentDto.setPwidRITALongTerm(htsRecentDto.getPwidRITALongTerm().add(dto.getPwidRITALongTerm()));
                htsRecentDto.setMsmRITALongTerm(htsRecentDto.getMsmRITALongTerm().add(dto.getMsmRITALongTerm()));
                htsRecentDto.setTgRITALongTerm(htsRecentDto.getTgRITALongTerm().add(dto.getTgRITALongTerm()));
                htsRecentDto.setFswRITALongTerm(htsRecentDto.getFswRITALongTerm().add(dto.getFswRITALongTerm()));
                htsRecentDto.setOtherRITALongTerm(htsRecentDto.getOtherRITALongTerm().add(dto.getOtherRITALongTerm()));

                htsRecentDto.setFuRITALongTerm(htsRecentDto.getFuRITALongTerm().add(dto.getFuRITALongTerm()));
                htsRecentDto.setF15RITALongTerm(htsRecentDto.getF15RITALongTerm().add(dto.getF15RITALongTerm()));
                htsRecentDto.setF20RITALongTerm(htsRecentDto.getF20RITALongTerm().add(dto.getF20RITALongTerm()));
                htsRecentDto.setF25RITALongTerm(htsRecentDto.getF25RITALongTerm().add(dto.getF25RITALongTerm()));
                htsRecentDto.setF30RITALongTerm(htsRecentDto.getF30RITALongTerm().add(dto.getF30RITALongTerm()));
                htsRecentDto.setF35RITALongTerm(htsRecentDto.getF35RITALongTerm().add(dto.getF35RITALongTerm()));
                htsRecentDto.setF40RITALongTerm(htsRecentDto.getF40RITALongTerm().add(dto.getF40RITALongTerm()));
                htsRecentDto.setF45RITALongTerm(htsRecentDto.getF45RITALongTerm().add(dto.getF45RITALongTerm()));
                htsRecentDto.setF50RITALongTerm(htsRecentDto.getF50RITALongTerm().add(dto.getF50RITALongTerm()));

                htsRecentDto.setMuRITALongTerm(htsRecentDto.getMuRITALongTerm().add(dto.getMuRITALongTerm()));
                htsRecentDto.setM15RITALongTerm(htsRecentDto.getM15RITALongTerm().add(dto.getM15RITALongTerm()));
                htsRecentDto.setM20RITALongTerm(htsRecentDto.getM20RITALongTerm().add(dto.getM20RITALongTerm()));
                htsRecentDto.setM25RITALongTerm(htsRecentDto.getM25RITALongTerm().add(dto.getM25RITALongTerm()));
                htsRecentDto.setM30RITALongTerm(htsRecentDto.getM30RITALongTerm().add(dto.getM30RITALongTerm()));
                htsRecentDto.setM35RITALongTerm(htsRecentDto.getM35RITALongTerm().add(dto.getM35RITALongTerm()));
                htsRecentDto.setM40RITALongTerm(htsRecentDto.getM40RITALongTerm().add(dto.getM40RITALongTerm()));
                htsRecentDto.setM45RITALongTerm(htsRecentDto.getM45RITALongTerm().add(dto.getM45RITALongTerm()));
                htsRecentDto.setM50RITALongTerm(htsRecentDto.getM50RITALongTerm().add(dto.getM50RITALongTerm()));

                hashTable.put(dto.getOrgId()+"-"+dto.getModality(),htsRecentDto);
            } else {
                hashTable.put(dto.getOrgId() + "-" + dto.getModality(), dto);
            }
        }

        for (HTSRecentDto dto : retHTS9) {
            if (hashTable.containsKey(dto.getOrgId() + "-" + dto.getModality())) {
                htsRecentDto = hashTable.get(dto.getOrgId() + "-" + dto.getModality());
                
                htsRecentDto.setPwidRTRI(htsRecentDto.getPwidRTRI().add(dto.getPwidRTRI()));
                htsRecentDto.setMsmRTRI(htsRecentDto.getMsmRTRI().add(dto.getMsmRTRI()));
                htsRecentDto.setTgRTRI(htsRecentDto.getTgRTRI().add(dto.getTgRTRI()));
                htsRecentDto.setFswRTRI(htsRecentDto.getFswRTRI().add(dto.getFswRTRI()));
                htsRecentDto.setOtherRTRI(htsRecentDto.getOtherRTRI().add(dto.getOtherRTRI()));

                htsRecentDto.setFuRTRI(htsRecentDto.getFuRTRI().add(dto.getFuRTRI()));
                htsRecentDto.setF15RTRI(htsRecentDto.getF15RTRI().add(dto.getF15RTRI()));
                htsRecentDto.setF20RTRI(htsRecentDto.getF20RTRI().add(dto.getF20RTRI()));
                htsRecentDto.setF25RTRI(htsRecentDto.getF25RTRI().add(dto.getF25RTRI()));
                htsRecentDto.setF30RTRI(htsRecentDto.getF30RTRI().add(dto.getF30RTRI()));
                htsRecentDto.setF35RTRI(htsRecentDto.getF35RTRI().add(dto.getF35RTRI()));
                htsRecentDto.setF40RTRI(htsRecentDto.getF40RTRI().add(dto.getF40RTRI()));
                htsRecentDto.setF45RTRI(htsRecentDto.getF45RTRI().add(dto.getF45RTRI()));
                htsRecentDto.setF50RTRI(htsRecentDto.getF50RTRI().add(dto.getF50RTRI()));

                htsRecentDto.setMuRTRI(htsRecentDto.getMuRTRI().add(dto.getMuRTRI()));
                htsRecentDto.setM15RTRI(htsRecentDto.getM15RTRI().add(dto.getM15RTRI()));
                htsRecentDto.setM20RTRI(htsRecentDto.getM20RTRI().add(dto.getM20RTRI()));
                htsRecentDto.setM25RTRI(htsRecentDto.getM25RTRI().add(dto.getM25RTRI()));
                htsRecentDto.setM30RTRI(htsRecentDto.getM30RTRI().add(dto.getM30RTRI()));
                htsRecentDto.setM35RTRI(htsRecentDto.getM35RTRI().add(dto.getM35RTRI()));
                htsRecentDto.setM40RTRI(htsRecentDto.getM40RTRI().add(dto.getM40RTRI()));
                htsRecentDto.setM45RTRI(htsRecentDto.getM45RTRI().add(dto.getM45RTRI()));
                htsRecentDto.setM50RTRI(htsRecentDto.getM50RTRI().add(dto.getM50RTRI()));

                hashTable.put(dto.getOrgId()+"-"+dto.getModality(),htsRecentDto);
            } else {
                hashTable.put(dto.getOrgId() + "-" + dto.getModality(), dto);
            }
        }

        for (HTSRecentDto dto : retHTS10) {
            if (hashTable.containsKey(dto.getOrgId() + "-" + dto.getModality())) {
                htsRecentDto = hashTable.get(dto.getOrgId() + "-" + dto.getModality());
                
                htsRecentDto.setPwidRTRILongTerm(htsRecentDto.getPwidRTRILongTerm().add(dto.getPwidRTRILongTerm()));
                htsRecentDto.setMsmRTRILongTerm(htsRecentDto.getMsmRTRILongTerm().add(dto.getMsmRTRILongTerm()));
                htsRecentDto.setTgRTRILongTerm(htsRecentDto.getTgRTRILongTerm().add(dto.getTgRTRILongTerm()));
                htsRecentDto.setFswRTRILongTerm(htsRecentDto.getFswRTRILongTerm().add(dto.getFswRTRILongTerm()));
                htsRecentDto.setOtherRTRILongTerm(htsRecentDto.getOtherRTRILongTerm().add(dto.getOtherRTRILongTerm()));

                htsRecentDto.setFuRTRILongTerm(htsRecentDto.getFuRTRILongTerm().add(dto.getFuRTRILongTerm()));
                htsRecentDto.setF15RTRILongTerm(htsRecentDto.getF15RTRILongTerm().add(dto.getF15RTRILongTerm()));
                htsRecentDto.setF20RTRILongTerm(htsRecentDto.getF20RTRILongTerm().add(dto.getF20RTRILongTerm()));
                htsRecentDto.setF25RTRILongTerm(htsRecentDto.getF25RTRILongTerm().add(dto.getF25RTRILongTerm()));
                htsRecentDto.setF30RTRILongTerm(htsRecentDto.getF30RTRILongTerm().add(dto.getF30RTRILongTerm()));
                htsRecentDto.setF35RTRILongTerm(htsRecentDto.getF35RTRILongTerm().add(dto.getF35RTRILongTerm()));
                htsRecentDto.setF40RTRILongTerm(htsRecentDto.getF40RTRILongTerm().add(dto.getF40RTRILongTerm()));
                htsRecentDto.setF45RTRILongTerm(htsRecentDto.getF45RTRILongTerm().add(dto.getF45RTRILongTerm()));
                htsRecentDto.setF50RTRILongTerm(htsRecentDto.getF50RTRILongTerm().add(dto.getF50RTRILongTerm()));

                htsRecentDto.setMuRTRILongTerm(htsRecentDto.getMuRTRILongTerm().add(dto.getMuRTRILongTerm()));
                htsRecentDto.setM15RTRILongTerm(htsRecentDto.getM15RTRILongTerm().add(dto.getM15RTRILongTerm()));
                htsRecentDto.setM20RTRILongTerm(htsRecentDto.getM20RTRILongTerm().add(dto.getM20RTRILongTerm()));
                htsRecentDto.setM25RTRILongTerm(htsRecentDto.getM25RTRILongTerm().add(dto.getM25RTRILongTerm()));
                htsRecentDto.setM30RTRILongTerm(htsRecentDto.getM30RTRILongTerm().add(dto.getM30RTRILongTerm()));
                htsRecentDto.setM35RTRILongTerm(htsRecentDto.getM35RTRILongTerm().add(dto.getM35RTRILongTerm()));
                htsRecentDto.setM40RTRILongTerm(htsRecentDto.getM40RTRILongTerm().add(dto.getM40RTRILongTerm()));
                htsRecentDto.setM45RTRILongTerm(htsRecentDto.getM45RTRILongTerm().add(dto.getM45RTRILongTerm()));
                htsRecentDto.setM50RTRILongTerm(htsRecentDto.getM50RTRILongTerm().add(dto.getM50RTRILongTerm()));

                hashTable.put(dto.getOrgId()+"-"+dto.getModality(),htsRecentDto);
            } else {
                hashTable.put(dto.getOrgId() + "-" + dto.getModality(), dto);
            }
        }

        for (HTSRecentDto dto : retHTS11) {
            if (hashTable.containsKey(dto.getOrgId() + "-" + dto.getModality())) {
                htsRecentDto = hashTable.get(dto.getOrgId() + "-" + dto.getModality());

                htsRecentDto.setPwidRITA(htsRecentDto.getPwidRITA().add(dto.getPwidRITA()));
                htsRecentDto.setMsmRITA(htsRecentDto.getMsmRITA().add(dto.getMsmRITA()));
                htsRecentDto.setTgRITA(htsRecentDto.getTgRITA().add(dto.getTgRITA()));
                htsRecentDto.setFswRITA(htsRecentDto.getFswRITA().add(dto.getFswRITA()));
                htsRecentDto.setOtherRITA(htsRecentDto.getOtherRITA().add(dto.getOtherRITA()));

                htsRecentDto.setFuRITA(htsRecentDto.getFuRITA().add(dto.getFuRITA()));
                htsRecentDto.setF15RITA(htsRecentDto.getF15RITA().add(dto.getF15RITA()));
                htsRecentDto.setF20RITA(htsRecentDto.getF20RITA().add(dto.getF20RITA()));
                htsRecentDto.setF25RITA(htsRecentDto.getF25RITA().add(dto.getF25RITA()));
                htsRecentDto.setF30RITA(htsRecentDto.getF30RITA().add(dto.getF30RITA()));
                htsRecentDto.setF35RITA(htsRecentDto.getF35RITA().add(dto.getF35RITA()));
                htsRecentDto.setF40RITA(htsRecentDto.getF40RITA().add(dto.getF40RITA()));
                htsRecentDto.setF45RITA(htsRecentDto.getF45RITA().add(dto.getF45RITA()));
                htsRecentDto.setF50RITA(htsRecentDto.getF50RITA().add(dto.getF50RITA()));

                htsRecentDto.setMuRITA(htsRecentDto.getMuRITA().add(dto.getMuRITA()));
                htsRecentDto.setM15RITA(htsRecentDto.getM15RITA().add(dto.getM15RITA()));
                htsRecentDto.setM20RITA(htsRecentDto.getM20RITA().add(dto.getM20RITA()));
                htsRecentDto.setM25RITA(htsRecentDto.getM25RITA().add(dto.getM25RITA()));
                htsRecentDto.setM30RITA(htsRecentDto.getM30RITA().add(dto.getM30RITA()));
                htsRecentDto.setM35RITA(htsRecentDto.getM35RITA().add(dto.getM35RITA()));
                htsRecentDto.setM40RITA(htsRecentDto.getM40RITA().add(dto.getM40RITA()));
                htsRecentDto.setM45RITA(htsRecentDto.getM45RITA().add(dto.getM45RITA()));
                htsRecentDto.setM50RITA(htsRecentDto.getM50RITA().add(dto.getM50RITA()));

                hashTable.put(dto.getOrgId()+"-"+dto.getModality(),htsRecentDto);
            } else {
                hashTable.put(dto.getOrgId() + "-" + dto.getModality(), dto);
            }
        }

        for (HTSRecentDto dto : retHTS12) {
            if (hashTable.containsKey(dto.getOrgId() + "-" + dto.getModality())) {
                htsRecentDto = hashTable.get(dto.getOrgId() + "-" + dto.getModality());

                htsRecentDto.setPwidRITALongTerm(htsRecentDto.getPwidRITALongTerm().add(dto.getPwidRITALongTerm()));
                htsRecentDto.setMsmRITALongTerm(htsRecentDto.getMsmRITALongTerm().add(dto.getMsmRITALongTerm()));
                htsRecentDto.setTgRITALongTerm(htsRecentDto.getTgRITALongTerm().add(dto.getTgRITALongTerm()));
                htsRecentDto.setFswRITALongTerm(htsRecentDto.getFswRITALongTerm().add(dto.getFswRITALongTerm()));
                htsRecentDto.setOtherRITALongTerm(htsRecentDto.getOtherRITALongTerm().add(dto.getOtherRITALongTerm()));

                htsRecentDto.setFuRITALongTerm(htsRecentDto.getFuRITALongTerm().add(dto.getFuRITALongTerm()));
                htsRecentDto.setF15RITALongTerm(htsRecentDto.getF15RITALongTerm().add(dto.getF15RITALongTerm()));
                htsRecentDto.setF20RITALongTerm(htsRecentDto.getF20RITALongTerm().add(dto.getF20RITALongTerm()));
                htsRecentDto.setF25RITALongTerm(htsRecentDto.getF25RITALongTerm().add(dto.getF25RITALongTerm()));
                htsRecentDto.setF30RITALongTerm(htsRecentDto.getF30RITALongTerm().add(dto.getF30RITALongTerm()));
                htsRecentDto.setF35RITALongTerm(htsRecentDto.getF35RITALongTerm().add(dto.getF35RITALongTerm()));
                htsRecentDto.setF40RITALongTerm(htsRecentDto.getF40RITALongTerm().add(dto.getF40RITALongTerm()));
                htsRecentDto.setF45RITALongTerm(htsRecentDto.getF45RITALongTerm().add(dto.getF45RITALongTerm()));
                htsRecentDto.setF50RITALongTerm(htsRecentDto.getF50RITALongTerm().add(dto.getF50RITALongTerm()));

                htsRecentDto.setMuRITALongTerm(htsRecentDto.getMuRITALongTerm().add(dto.getMuRITALongTerm()));
                htsRecentDto.setM15RITALongTerm(htsRecentDto.getM15RITALongTerm().add(dto.getM15RITALongTerm()));
                htsRecentDto.setM20RITALongTerm(htsRecentDto.getM20RITALongTerm().add(dto.getM20RITALongTerm()));
                htsRecentDto.setM25RITALongTerm(htsRecentDto.getM25RITALongTerm().add(dto.getM25RITALongTerm()));
                htsRecentDto.setM30RITALongTerm(htsRecentDto.getM30RITALongTerm().add(dto.getM30RITALongTerm()));
                htsRecentDto.setM35RITALongTerm(htsRecentDto.getM35RITALongTerm().add(dto.getM35RITALongTerm()));
                htsRecentDto.setM40RITALongTerm(htsRecentDto.getM40RITALongTerm().add(dto.getM40RITALongTerm()));
                htsRecentDto.setM45RITALongTerm(htsRecentDto.getM45RITALongTerm().add(dto.getM45RITALongTerm()));
                htsRecentDto.setM50RITALongTerm(htsRecentDto.getM50RITALongTerm().add(dto.getM50RITALongTerm()));

                hashTable.put(dto.getOrgId()+"-"+dto.getModality(),htsRecentDto);
            } else {
                hashTable.put(dto.getOrgId() + "-" + dto.getModality(), dto);
            }
        }

        List<HTSRecentDto> ret = new ArrayList<>(hashTable.values());
        return ret;
    }

    @Override
    public Workbook exportHTSRecent(PreventionFilterDto filter, Workbook workbook) {
        List<HTSRecentDto> result = this.getDataHTSRecent(filter);
        if (result == null) {
            return workbook;
        }
        XSSFWorkbook wbook = (XSSFWorkbook) workbook;
        int rowIndex = 5;
        int colIndex = 0;

        Row row = null;
        Cell cell = null;
        Sheet sheet = wbook.getSheet("HTS_RECENT");
        CellStyle cellStyle = wbook.createCellStyle();
        ExcelUtils.setBorders4Style(cellStyle);
        cellStyle.setWrapText(true);
        cellStyle.setAlignment(HorizontalAlignment.RIGHT);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        for (HTSRecentDto dto : result) {
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
                    cell.setCellValue(dto.getModality());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getPwidRTRI().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getMsmRTRI().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getTgRTRI().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getFswRTRI().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getOtherRTRI().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getFuRTRI().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF15RTRI().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF20RTRI().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF25RTRI().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF30RTRI().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF35RTRI().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF40RTRI().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF45RTRI().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF50RTRI().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getMuRTRI().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM15RTRI().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM20RTRI().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM25RTRI().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM30RTRI().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM35RTRI().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM40RTRI().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM45RTRI().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM50RTRI().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getTotalRTRIRecent().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getPwidRTRILongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getMsmRTRILongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getTgRTRILongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getFswRTRILongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getOtherRTRILongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getFuRTRILongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF15RTRILongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF20RTRILongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF25RTRILongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF30RTRILongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF35RTRILongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF40RTRILongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF45RTRILongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF50RTRILongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getMuRTRILongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM15RTRILongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM20RTRILongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM25RTRILongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM30RTRILongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM35RTRILongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM40RTRILongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM45RTRILongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM50RTRILongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getTotalRTRILongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getTotalRTRI().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getPwidRITA().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getMsmRITA().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getTgRITA().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getFswRITA().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getOtherRITA().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getFuRITA().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF15RITA().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF20RITA().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF25RITA().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF30RITA().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF35RITA().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF40RITA().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF45RITA().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF50RITA().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getMuRITA().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM15RITA().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM20RITA().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM25RITA().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM30RITA().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM35RITA().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM40RITA().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM45RITA().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM50RITA().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getTotalRITARecent().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getPwidRITALongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getMsmRITALongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getTgRITALongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getFswRITALongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getOtherRITALongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getFuRITALongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF15RITALongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF20RITALongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF25RITALongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF30RITALongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF35RITALongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF40RITALongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF45RITALongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getF50RITALongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getMuRITALongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                //document
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM15RITALongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM20RITALongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM25RITALongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM30RITALongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM35RITALongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM40RITALongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM45RITALongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }

                //new
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getM50RITALongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getTotalRITALongTerm().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
                cell = row.createCell(colIndex++);
                cell.setCellStyle(cellStyle);
                try {
                    cell.setCellValue(dto.getTotalRITA().intValue());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
            }
        }

        return wbook;
    }
    @Override
    public ImportResultDto<HTSCaseDto> importFromExcelToUpdateIdentityCard(InputStream is) throws IOException {
        Workbook workbook = new XSSFWorkbook(is);
        Sheet datatypeSheet = workbook.getSheetAt(0);
        int rowIndex = 1;
        int num = datatypeSheet.getLastRowNum();
        ImportResultDto<HTSCaseDto> ret = new ImportResultDto<HTSCaseDto>();
        while (rowIndex <= num) {
            try {
                System.out.println(rowIndex);
                Row currentRow = datatypeSheet.getRow(rowIndex);
                Cell currentCell = null;
                if (currentRow != null) {
                    HTSCaseDto dto = new HTSCaseDto();
                    String err = "";
                    try {
//						c1
                        currentCell = currentRow.getCell(0);
                        if (currentCell != null) {
                            if (currentCell.getCellType() == CellType.STRING) {
                                UUID uid = UUID.fromString(currentCell.getStringCellValue());
                                dto.setUid(uid);
                            }
                        }
                        if (dto.getUid() == null) {
                            rowIndex += 1;
                            continue;
                        }
                    } catch (Exception e) {
                        dto.setUid(null);
                        err += "C1 - Không rõ; ";
                    }
                    try {
//						c45 - c23_id_number
                        currentCell = currentRow.getCell(44);
                        if (currentCell != null) {
                            String c23IdNumber = null;
                            if (currentCell.getCellType() == CellType.STRING
                                && StringUtils.hasText(currentCell.getStringCellValue())) {
                                c23IdNumber = currentCell.getStringCellValue();
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                c23IdNumber = String.valueOf(Double.valueOf(currentCell.getNumericCellValue()).longValue());
                                System.out.println(currentCell.getNumericCellValue());
                            }
                            dto.setC23IdNumber(c23IdNumber);
                        }
                    } catch (Exception e) {
                        dto.setC23IdNumber(null);
                        err += "C45 - Không rõ; ";
                    }

                    try {
                        HTSCase entity = new HTSCase();
                        entity = htsCaseRepository.findByUid(dto.getUid());
                        if(entity != null) {
                            User currentUser = SecurityUtils.getCurrentUser();
                            Boolean isSite = SecurityUtils.isUserInRole(currentUser, "ROLE_SITE_MANAGER");
                            Boolean isProvince = SecurityUtils.isUserInRole(currentUser, "ROLE_PROVINCIAL_MANAGER");
                            Boolean isAdministrator = SecurityUtils.isUserInRole(currentUser, "ROLE_ADMIN");
                            List<UserOrganization> listUO = null;
                            if (!isAdministrator) {
                                listUO = userOrganizationRepository.getListByUserId(currentUser.getId());
                            }
                            entity.setC23IdNumber(dto.getC23IdNumber());
                            entity = htsCaseRepository.save(entity);

                            if (entity != null) {
                                dto = newHTSCaseDto(entity, currentUser, isSite, isProvince, isAdministrator, listUO, false);
                            }
                        }
                        dto.setSaved(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                        ret.setTotalErr(ret.getTotalErr() + 1);
                        err += "Lưu thất bại:" + CommonUtils.getStackTrace(e);

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
}
