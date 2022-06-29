package org.pepfar.pdma.app.data.service.jpa;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pepfar.pdma.app.Constants;
import org.pepfar.pdma.app.data.domain.HTSCase;
import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.domain.SNSCase;
import org.pepfar.pdma.app.data.domain.SNSCaseIdNumber;
import org.pepfar.pdma.app.data.domain.User;
import org.pepfar.pdma.app.data.domain.UserOrganization;
import org.pepfar.pdma.app.data.dto.ResponseDto;
import org.pepfar.pdma.app.data.dto.SNSCaseDto;
import org.pepfar.pdma.app.data.dto.SNSCaseFilterDto;
import org.pepfar.pdma.app.data.dto.SNSCaseIdNumberDto;
import org.pepfar.pdma.app.data.dto.HTSCaseDto;
import org.pepfar.pdma.app.data.dto.OrganizationDto;
import org.pepfar.pdma.app.data.dto.PreventionCheckCodeDto;
import org.pepfar.pdma.app.data.dto.SNSCaseReportDetailDto;
import org.pepfar.pdma.app.data.dto.SNSCaseReportDto;
import org.pepfar.pdma.app.data.repository.OrganizationRepository;
import org.pepfar.pdma.app.data.repository.SNSCaseIdNumberRepository;
import org.pepfar.pdma.app.data.repository.SNSCaseRepository;
import org.pepfar.pdma.app.data.repository.UserOrganizationRepository;
import org.pepfar.pdma.app.data.service.SNSCaseService;
import org.pepfar.pdma.app.data.types.Gender;
import org.pepfar.pdma.app.data.types.HIVStatus;
import org.pepfar.pdma.app.data.types.Permission;
import org.pepfar.pdma.app.data.types.SNSApproachMethod;
import org.pepfar.pdma.app.data.types.SNSCustomerSource;
import org.pepfar.pdma.app.data.types.SNSIdNumberType;
import org.pepfar.pdma.app.data.types.SNSRiskGroup;
import org.pepfar.pdma.app.data.types.YesNoNone;
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
import org.springframework.util.StringUtils;;

@Transactional(readOnly = false)
@Service
public class SNSCaseServiceImpl implements SNSCaseService {
	@Autowired
	SNSCaseRepository repository;

	@Autowired
	private OrganizationRepository orgRepos;

	@Autowired
	public EntityManager manager;
	
	@Autowired
	SNSCaseIdNumberRepository snsCaseIdNumberrepository;

	@Autowired
	private AuthorizationUtils authUtils;
	
	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private OrganizationRepository organizationRepository;
	
	@Autowired
	private UserOrganizationRepository userOrganizationRepository;
	
	@Override
	@Transactional(readOnly = true)
	public SNSCaseDto findById(Long id) {
		if (!CommonUtils.isPositive(id, true)) {
			return null;
		}
		User currentUser = SecurityUtils.getCurrentUser();
		if (currentUser == null) {
			return null;
		}
		SNSCase entity = repository.findOne(id);
		if (entity != null) {
			Boolean isSite = SecurityUtils.isUserInRole(currentUser, "ROLE_SITE_MANAGER");
			Boolean isProvince = SecurityUtils.isUserInRole(currentUser, "ROLE_PROVINCIAL_MANAGER");
			Boolean isAdministrator = SecurityUtils.isUserInRole(currentUser, "ROLE_ADMIN");
			List<UserOrganization> listUO = null;
			if (!isAdministrator) {
				listUO = userOrganizationRepository.getListByUserId(currentUser.getId());
			}
			return newSNSCaseDto(entity, currentUser, isSite, isProvince, isAdministrator, listUO);
		} else {
			return null;
		}
		
	}
	private SNSCaseDto newSNSCaseDto(SNSCase entity, User currentUser, boolean isSite, boolean isProvince,
			boolean isAdministrator, List<UserOrganization> listUO) {
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
						if (userOrganization.getOrganization() != null
								&& ( (entity.getTestOrganization() != null && userOrganization.getOrganization().getId().equals(entity.getTestOrganization().getId()))
								|| (userOrganization.getOrganization().getId().equals(entity.getOrganization().getId()) && entity.getOrganization() !=null) )
								&& userOrganization.getSnsRole() != null && userOrganization.getSnsRole()) {
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
			return new SNSCaseDto(entity, (isViewPII?1:0), isReadAble, isWritAble, isDeleteAble);
		} else {
			return null;
		}
	}
	
	

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ResponseDto<SNSCaseDto> deleteById(Long id) {
		User currentUser = SecurityUtils.getCurrentUser();
		Boolean isSiteManagement = SecurityUtils.isUserInRole(currentUser, "ROLE_SITE_MANAGER");
		if(!isSiteManagement) {
			return null;
		}
		ResponseDto<SNSCaseDto> ret = new ResponseDto<SNSCaseDto>();
		final List<Long> writableOrgIds = authUtils.getGrantedOrgIds(Permission.WRITE_ACCESS);
		SNSCase entity = repository.findOne(id);
		if (entity != null) {
			if(entity.getTestOrganization()!=null) {
				if(!writableOrgIds.contains(entity.getTestOrganization().getId())) {
					ret.setCode(YesNoNone.NO);
					ret.setMessage("Khách hàng này đã đến xét nghiệm tại cơ sở khác, bạn không có quyền xóa!");
					return ret;
				}
			}
			else {
				if(!writableOrgIds.contains(entity.getOrganization().getId())) {
					ret.setCode(YesNoNone.NO);
					ret.setMessage("Khách hàng này không thuộc quyền quản lý của bạn");
					return ret;
				}
			}
			if(entity.getChildren()!=null && entity.getChildren().size()>0) {
				for (SNSCase child : entity.getChildren()) {
					if(child.getTestOrganization()!=null) {
						ret.setCode(YesNoNone.NO);
						ret.setMessage("Trong số thẻ khách hàng này phát ra, đã có người quay lại làm xét nghiệm, không thể xóa");
						return ret;
					}
				}
			}
			repository.delete(entity);
			ret.setCode(YesNoNone.YES);
			ret.setMessage("Xóa thành công");
			return ret;
		}
		else {
			ret.setCode(YesNoNone.NO);
			ret.setMessage("Không tìm thấy bản ghi tương ứng");
			return ret;
		}		
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public SNSCaseDto saveOne(SNSCaseDto dto) {
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
		boolean isNew=false;
		SNSCase entity = null;

		if (CommonUtils.isPositive(dto.getId(), true)) {
			entity = repository.findOne(dto.getId());
		}

		if (entity == null) {
			entity = new SNSCase();
			isNew = true;
		}

		
//		if(dto.getDob()!=null) {
//			entity.setDob(dto.getDob());	
//		}
//		else {
//			
//		}
		if(dto.getYearOfBirth()!=null && dto.getYearOfBirth()>1900 && dto.getYearOfBirth()<=  LocalDateTime.now().getYear()) {
			entity.setDob(LocalDateTime.of(dto.getYearOfBirth(), 6, 15, 0, 0));			
		}
		entity.setCouponCode(dto.getCouponCode());
		entity.setApproachMethod(dto.getApproachMethod());
		entity.setCustomerSource(dto.getCustomerSource());
		entity.setGender(dto.getGender());
		
		entity.setIdNumber(dto.getIdNumber());
		entity.setIdNumberType(dto.getIdNumberType());
		entity.setName(dto.getName());
		
		entity.setRiskGroup(dto.getRiskGroup());
		entity.setTotalCoupon(dto.getTotalCoupon());

		entity.setOrderCoupon(dto.getOrderCoupon());

		if(dto.getIssueDate()!=null) {
			entity.setIssueDate(dto.getIssueDate());
		}
		else if(dto.getFirstTimeVisit()!=null) {
			entity.setIssueDate(dto.getFirstTimeVisit());
		}
		else {
			entity.setIssueDate(LocalDateTime.now());
		}
		entity.setFirstTimeVisit(dto.getFirstTimeVisit());
		entity.setNote(dto.getNote());
		entity.setHivStatus(dto.getHivStatus());
		if(entity.getHivStatus()==HIVStatus.positive) {
			entity.setPrepDate(null);
			entity.setArvDate(dto.getArvDate());
		}
		else if(entity.getHivStatus()==HIVStatus.negative) {
			entity.setArvDate(null);
			entity.setPrepDate(dto.getPrepDate());
		}
		else {
			entity.setArvDate(null);
			entity.setPrepDate(null);
		}
		if(entity.getHivStatus()==HIVStatus.notest) {
			entity.setTestDate(null);
		}
		else {
			entity.setTestDate(dto.getTestDate());
		}
		if (dto.getOrganization() != null 
				&& dto.getOrganization().getId() != null
				&& dto.getOrganization().getId() > 0L) {
			Organization organization = orgRepos.findOne(dto.getOrganization().getId());
			entity.setOrganization(organization);
			if(isNew && organization!=null && organization.getId()!=null) {
				Integer maxSeq = this.getMaxSEQbyOrg(organization.getId());				
				entity.setSeqByOrganization(maxSeq+1);
			}
		}
		if (dto.getTestOrganization() != null 
				&& dto.getTestOrganization().getId() != null
				&& dto.getTestOrganization().getId() > 0L) {
			Organization testOrganization = orgRepos.findOne(dto.getTestOrganization().getId());
			entity.setTestOrganization(testOrganization);
		}
		if (dto.getParent() != null && dto.getParent().getId() != null && dto.getParent().getId() > 0L) {
			SNSCase parent = repository.findOne(dto.getParent().getId());
			entity.setParent(parent);
		}
		if (dto.getChildren() != null && dto.getChildren().size() > 0) {
			Set<SNSCase> childs = new HashSet<SNSCase>();
//			Integer maxSeq = null;
//			if(isNew) {
//				maxSeq=entity.getSeqByOrganization();
//			}
//			else {
//				maxSeq = this.getMaxSEQbyOrg(entity.getOrganization().getId());
//			}
			int i=1;
			for (SNSCaseDto childDto : dto.getChildren()) {
				SNSCase child = null;				
				if (childDto.getId() != null && childDto.getId() > 0L) {
					child = repository.findOne(childDto.getId());
				}
				//Nếu đã được tạo rồi thì chỉ update mã Coupon
				if(child!=null) {
					child.setCouponCode(childDto.getCouponCode());
					childs.add(child);
					continue;
				}
				//Nếu chưa tạo thì tạo mới phiếu SNS
				if (child == null) {
					child = new SNSCase();
				}
				child.setArvDate(childDto.getArvDate());
//				child.setDob(childDto.getDob());
				if(childDto.getYearOfBirth()!=null && childDto.getYearOfBirth()>1900 && childDto.getYearOfBirth()<= LocalDateTime.now().getYear()) {
					child.setDob(LocalDateTime.of(childDto.getYearOfBirth(), 6, 15, 0, 0));			
				}
				if(childDto.getCouponCode()!=null) {
					child.setCouponCode(childDto.getCouponCode());
				}
				else {
					child.setCouponCode("");
				}
				
				if(childDto.getApproachMethod()!=null) {
					child.setApproachMethod(childDto.getApproachMethod());
				}
				else {
					child.setApproachMethod(SNSApproachMethod.unknown);
				}
				if(childDto.getCustomerSource()!=null) {
					child.setCustomerSource(childDto.getCustomerSource());
				}
				else {
					child.setCustomerSource(SNSCustomerSource.UNKNOWN);
				}
				if(childDto.getGender()!=null) {
					child.setGender(childDto.getGender());
				}
				else {
					child.setGender(Gender.UNKNOWN);
				}
				if(childDto.getHivStatus()!=null) {
					child.setHivStatus(childDto.getHivStatus());
				}
				else {
					child.setHivStatus(HIVStatus.UNKNOWN);
				}
				if(childDto.getRiskGroup()!=null) {
					child.setRiskGroup(childDto.getRiskGroup());
				}
				else {
					child.setRiskGroup(SNSRiskGroup.UNKNOWN);
				}
				
				child.setIdNumber(childDto.getIdNumber());
				child.setIdNumberType(childDto.getIdNumberType());
				if(childDto.getName()!=null) {
					child.setName(childDto.getName());
				}
				else {
					child.setName("");
				}
				
				child.setPrepDate(childDto.getPrepDate());
				child.setTotalCoupon(childDto.getTotalCoupon());
				child.setIssueDate(childDto.getIssueDate());
				if(child.getChildren()!=null) {
					child.setTotalCoupon(child.getChildren().size());
				}
				if (childDto.getOrganization() != null && childDto.getOrganization().getId() != null
						&& childDto.getOrganization().getId() > 0L) {
					Organization organization = orgRepos.findOne(childDto.getOrganization().getId());
					child.setOrganization(organization);
				}
				if(child.getOrganization()==null) {
					child.setOrganization(entity.getOrganization());
				}
				if(child.getSeqByOrganization()==null) {
					Integer maxSeq = this.getMaxSEQbyOrg(child.getOrganization().getId());
					i+=1;
					child.setSeqByOrganization(maxSeq+i);
				}
				child.setParent(entity);
				childs.add(child);
			}
			if (entity.getChildren() == null || entity.getChildren().size() == 0) {
				entity.setChildren(childs);
			} else {
				entity.getChildren().clear();
				entity.getChildren().addAll(childs);
			}			
		}
		if(entity.getChildren()!=null) {
			entity.setTotalCoupon(entity.getChildren().size());
		}
		else {
			entity.setTotalCoupon(0);
		}
		// SNSCaseIdNumber
		Set<SNSCaseIdNumber> snsCaseIdNumbers = new LinkedHashSet<>();
		for (SNSCaseIdNumberDto snsCIDDto : dto.getSnsCaseIdNumers()) {
			SNSCaseIdNumber snsCaseIdNumber = null;
			if (CommonUtils.isPositive(snsCIDDto.getId(), true)) {
				snsCaseIdNumber = snsCaseIdNumberrepository.findOne(snsCIDDto.getId());
			}
			if (snsCaseIdNumber == null) {
				snsCaseIdNumber = new SNSCaseIdNumber();
			}
			snsCaseIdNumber.setIdNumberType(snsCIDDto.getIdNumberType());
			snsCaseIdNumber.setValue(snsCIDDto.getValue());
			if(snsCIDDto.getPrimary()!=null) {
				snsCaseIdNumber.setPrimary(snsCIDDto.getPrimary());
			}
			else {
				snsCaseIdNumber.setPrimary(false);
			}
			snsCaseIdNumber.setSnsCase(entity);			
			snsCaseIdNumbers.add(snsCaseIdNumber);
		}
		if(entity.getSnsCaseIdNumbers()==null) {
			entity.setSnsCaseIdNumbers(snsCaseIdNumbers);
		}
		else {
			entity.getSnsCaseIdNumbers().clear();
			entity.getSnsCaseIdNumbers().addAll(snsCaseIdNumbers);
		}
		//Nếu cơ sở test của khách hàng này không thuộc danh mục cơ sở user hiện tại được phép sửa dữ liệu thì trả về null
		if(entity.getTestOrganization()!=null) {
			if(!writableOrgIds.contains(entity.getTestOrganization().getId())) {
				return null;
			}
		}
		else {
			if(!writableOrgIds.contains(entity.getOrganization().getId())) {
				return null;
			}
		}
		
		entity = repository.save(entity);

		if (entity != null) {
			return newSNSCaseDto(entity, currentUser, isSite, isProvince, isAdministrator, listUO);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Integer getMaxSEQbyOrg(Long orgId) {
		String SQL = " SELECT Max(s.seqByOrganization) from SNSCase s WHERE s.organization.id =:orgId ";
		Query q = manager.createQuery(SQL);
		q.setParameter("orgId", orgId);
		Integer max = (Integer)q.getSingleResult();
		if(max!=null) {
			return max;
		}
		else {
			return 0;
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<SNSCaseDto> findAllPageable(SNSCaseFilterDto filter) {
		User currentUser = SecurityUtils.getCurrentUser();
		
		Boolean isAdministrator = SecurityUtils.isUserInRole(currentUser, "ROLE_ADMIN");
		Boolean isProvince = SecurityUtils.isUserInRole(currentUser, "ROLE_PROVINCIAL_MANAGER");
		Boolean isSiteManagement = SecurityUtils.isUserInRole(currentUser, "ROLE_SITE_MANAGER");
		
		Boolean isDONOR = SecurityUtils.isUserInRole(currentUser, "ROLE_DONOR");
		Boolean isNATIONAL = SecurityUtils.isUserInRole(currentUser, "ROLE_NATIONAL_MANAGER");
		Boolean isDISTRICT = SecurityUtils.isUserInRole(currentUser, "ROLE_DISTRICT_MANAGER");

		Boolean viewPII = isProvince || isSiteManagement;
		final List<Long> orgIds = authUtils.getGrantedOrgIds(Permission.READ_ACCESS);
		final List<Long> writableOrgIds = authUtils.getGrantedOrgIds(Permission.WRITE_ACCESS);
		final List<Long> snsableOrgIds = authUtils.getGrantedOrgIds(Permission.SNS_ACCESS);

		List<Long> lstOrgIds = new ArrayList<Long>();
		if(orgIds!=null && orgIds.size()>0) {
			lstOrgIds.addAll(orgIds);
		}
		if(writableOrgIds!=null && writableOrgIds.size()>0) {
			lstOrgIds.addAll(writableOrgIds);
		}
		//Nếu là admin thì không cần xét OrgId
		if(CollectionUtils.isEmpty(lstOrgIds) && !isAdministrator) {
			return null;
		}
		//Nếu có các quyền như code (chỉ có ROLE_SITE_MANAGER) này thì phải xét đến snsableOrgIds
		if(CollectionUtils.isEmpty(snsableOrgIds) && !isAdministrator && !isDONOR && !isNATIONAL && !isProvince && !isDISTRICT) {
			return null;
		}
		
		if (filter == null) {
			filter = new SNSCaseFilterDto();
		}

		if (filter.getPageIndex() < 0) {
			filter.setPageIndex(0);
		}

		if (filter.getPageSize() <= 0) {
			filter.setPageSize(25);
		}
		
		String contrucDto="";
		if(filter.getIsFullDto()!=null && filter.getIsFullDto()) {
			if(viewPII) {
				contrucDto = " new org.pepfar.pdma.app.data.dto.SNSCaseDto(s, 1,true,false,false) ";
			} else {
				contrucDto = " new org.pepfar.pdma.app.data.dto.SNSCaseDto(s, 0,true,false,false) ";
			}
		}
		else {
			if(viewPII) {
				contrucDto = " new org.pepfar.pdma.app.data.dto.SNSCaseDto(s,true, 1) ";
			} else {
				contrucDto = " new org.pepfar.pdma.app.data.dto.SNSCaseDto(s,true, 0) ";
			}
		}
		
		String SQL = " SELECT "+contrucDto+" from SNSCase s WHERE s.testOrganization is not null ";
		String countSQL = " SELECT COUNT(s.id) from SNSCase s WHERE s.testOrganization is not null ";
		String whereClause = " ";
		if(!isAdministrator) {
			whereClause += " AND s.testOrganization.id in (:writableOrgIds) ";
			if(!isDONOR && !isNATIONAL && !isProvince && !isDISTRICT && isSiteManagement) {
				whereClause += "  AND s.testOrganization.id in (:lstSnsOrgIds) ";
			}
		}
		if (filter.getCouponCode() != null && filter.getCouponCode().length() > 0) {
			whereClause += " AND s.couponCode like :couponCode ";
		}
		if (filter.getName() != null && filter.getName().length() > 0) {
			whereClause += " AND s.name like :name ";
		}
		if (filter.getKeyword() != null && filter.getKeyword().length() > 0) {
			whereClause += " AND (s.name like :keyword OR s.couponCode like :keyword OR s.idNumber like :keyword )";
		}
		if(filter.getRiskGroup()!=null && filter.getRiskGroup().size()>0) {
			whereClause += " AND (s.riskGroup in (:riskGroup) )";
		}
		if(filter.getHivStatus()!=null && filter.getHivStatus().size()>0) {
			whereClause += " AND (s.hivStatus in (:hivStatus) )";
		}
		if(filter.getCustomerSource()!=null && filter.getCustomerSource().size()>0) {
			whereClause += " AND (s.customerSource in (:customerSource) )";
		}
		if(filter.getApproachMethod()!=null && filter.getApproachMethod().size()>0) {
			whereClause += " AND (s.approachMethod in (:approachMethod) )";
		}
		if(filter.getFromYear()!=null && filter.getFromYear()>0) {
			whereClause += " AND (s.dob >= :fromYear )";
		}
		if(filter.getToYear()!=null && filter.getToYear()>0) {
			whereClause += " AND (s.dob <= :toYear )";
		}
		if(filter.getArvDateFrom()!=null) {
			whereClause += " AND (s.arvDate >= :arvDateFrom )";
		}
		if(filter.getArvDateTo()!=null) {
			whereClause += " AND (s.arvDate <= :arvDateTo )";
		}
		if(filter.getPrepDateFrom()!=null) {
			whereClause += " AND (s.prepDate >= :prepDateFrom )";
		}
		if(filter.getPrepDateTo()!=null) {
			whereClause += " AND (s.prepDate <= :prepDateTo )";
		}
		if(filter.getProvinceId() != null) {
			whereClause += " AND (s.organization.address.province.id = :provinceId) ";
		}

		String orderByClause=" order by s.seqByOrganization ";
		Query q = manager.createQuery(SQL + whereClause+orderByClause, SNSCaseDto.class);
		Query qCount = manager.createQuery(countSQL + whereClause);

		if(!isAdministrator) {
			q.setParameter("writableOrgIds", lstOrgIds);
			qCount.setParameter("writableOrgIds", lstOrgIds);
			if(!isDONOR && !isNATIONAL && !isProvince && !isDISTRICT && isSiteManagement) {
				q.setParameter("lstSnsOrgIds", snsableOrgIds);
				qCount.setParameter("lstSnsOrgIds", snsableOrgIds);
			}
		}
		
		if (filter.getCouponCode() != null && filter.getCouponCode().length() > 0) {
			q.setParameter("couponCode", filter.getCouponCode());
			qCount.setParameter("couponCode", filter.getCouponCode());
		}
		if (filter.getName() != null && filter.getName().length() > 0) {
			q.setParameter("name", "%"+filter.getName()+"%");
			qCount.setParameter("name", "%"+filter.getName()+"%");
		}
		if (filter.getKeyword() != null && filter.getKeyword().length() > 0) {
			q.setParameter("keyword", "%"+filter.getKeyword()+"%");
			qCount.setParameter("keyword", "%"+filter.getKeyword()+"%");
		}
		if(filter.getRiskGroup()!=null && filter.getRiskGroup().size()>0) {
			q.setParameter("riskGroup", filter.getRiskGroup());
			qCount.setParameter("riskGroup", filter.getRiskGroup());
		}
		if(filter.getHivStatus()!=null && filter.getHivStatus().size()>0) {
			q.setParameter("hivStatus", filter.getHivStatus());
			qCount.setParameter("hivStatus", filter.getHivStatus());
		}
		if(filter.getCustomerSource()!=null && filter.getCustomerSource().size()>0) {
			q.setParameter("customerSource", filter.getCustomerSource());
			qCount.setParameter("customerSource", filter.getCustomerSource());
		}
		if(filter.getApproachMethod()!=null && filter.getApproachMethod().size()>0) {
			q.setParameter("approachMethod", filter.getApproachMethod());
			qCount.setParameter("approachMethod", filter.getApproachMethod());
		}
		if(filter.getFromYear()!=null && filter.getFromYear()>0) {
			q.setParameter("fromYear", LocalDateTime.of(filter.getFromYear(), 1, 1, 0, 0));
			qCount.setParameter("fromYear", LocalDateTime.of(filter.getFromYear(), 1, 1, 0, 0));
		}
		if(filter.getToYear()!=null && filter.getToYear()>0) {
			q.setParameter("toYear", LocalDateTime.of(filter.getToYear(), 12, 31, 23, 59));
			qCount.setParameter("toYear", LocalDateTime.of(filter.getToYear(), 12, 31, 23, 59));
		}
		if(filter.getArvDateFrom()!=null) {
			q.setParameter("arvDateFrom", filter.getArvDateFrom());
			qCount.setParameter("arvDateFrom", filter.getArvDateFrom());
		}
		if(filter.getArvDateTo()!=null) {
			q.setParameter("arvDateTo", filter.getArvDateTo());
			qCount.setParameter("arvDateTo", filter.getArvDateTo());
		}
		if(filter.getPrepDateFrom()!=null) {
			q.setParameter("prepDateFrom", filter.getPrepDateFrom());
			qCount.setParameter("prepDateFrom", filter.getPrepDateFrom());
		}
		if(filter.getPrepDateTo()!=null) {
			q.setParameter("prepDateTo", filter.getPrepDateTo());
			qCount.setParameter("prepDateTo", filter.getPrepDateTo());
		}
		if(filter.getProvinceId() != null) {
			q.setParameter("provinceId", filter.getProvinceId());
			qCount.setParameter("provinceId", filter.getProvinceId());
		}
				
		Long count = (long) qCount.getSingleResult();		
		
		if(filter.getDisablePaging()==null || filter.getDisablePaging()==false) {
			int startPosition = filter.getPageIndex() * filter.getPageSize();
			q.setFirstResult(startPosition);
			q.setMaxResults(filter.getPageSize());
		}
		else {
			q.setFirstResult(0);
			
			if(count.intValue()>0) {
				filter.setPageSize(count.intValue());
				q.setMaxResults(count.intValue());
			}
			else {
				filter.setPageSize(10);
				q.setMaxResults(10);
			}
		}
		
		@SuppressWarnings("unchecked")
		List<SNSCaseDto> entities = q.getResultList();
		Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize());
		Page<SNSCaseDto> result = new PageImpl<SNSCaseDto>(entities, pageable, count);

		return result;
	}
	@Override
	public SNSCaseDto findByCode(String couponCode) {
		if(StringUtils.hasText(couponCode)) {
//			String SQL=" SELECT new org.pepfar.pdma.app.data.dto.SNSCaseDto(s) FROM SNSCase s WHERE s.couponCode=:couponCode ";
//			Query q = manager.createQuery(SQL,SNSCaseDto.class);
//			q.setParameter("couponCode", couponCode);
//			Object o = q.getSingleResult();
			SNSCaseDto ret = repository.findByCode(couponCode);
			
			return ret;
			
		}
		return null;
	}
	
	@Override
	public PreventionCheckCodeDto checkDuplicateCode(PreventionCheckCodeDto dto) {
		if(dto!=null && StringUtils.hasText(dto.getCouponCode())) {
			String SQL=" SELECT COUNT(s.id) FROM SNSCase s WHERE s.couponCode=:couponCode ";
			if(dto.getId()!=null && dto.getId()>0L) {
				SQL+=" AND s.id!=:id ";
			}
			Query q = manager.createQuery(SQL);
			q.setParameter("couponCode", dto.getCouponCode());
			if(dto.getId()!=null && dto.getId()>0L) {
				q.setParameter("id", dto.getId());
			}
			Long count = (Long)q.getSingleResult();
			dto.setIsDup(count!=null && count>0);
			if(dto.getIsDup()) {
				dto.setNote("Mã số "+dto.getCouponCode()+" đã bị trùng");
			}
			return dto;
		}
		return null;
	}
	
	@Override
	@Transactional(readOnly = true)
	public Workbook exportSNSCase(SNSCaseFilterDto filter) {
		Workbook blankBook = new XSSFWorkbook();
		blankBook.createSheet();
		filter.setDisablePaging(true);
		filter.setIsFullDto(true);
		List<SNSCaseDto> list = this.findAllPageable(filter).getContent();
		if(list==null || list.size()==0) {
			return blankBook;
		}
		else {
			Workbook wbook = null;
			try (InputStream template = context.getResource("classpath:templates/sns-list-new.xlsx")
//			try (InputStream template = context.getResource("classpath:templates/sns-list.xlsx")
					
					.getInputStream()) {
				XSSFWorkbook tmp = new XSSFWorkbook(template);
				Sheet sheet = tmp.getSheetAt(0);
				wbook = new SXSSFWorkbook(tmp, 100);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (wbook == null) {
				return blankBook;
			}
			int rowIndex = 3;
			int colIndex = 0;
			
			Row row = null;
			Cell cell = null;
			Sheet sheet = wbook.getSheetAt(0);

//			Sheet sheet = wbook.getSheet("Sheet1");
//			Row templateRow = sheet.getRow(3);
			int seq=0;
			CellStyle cellStyle = wbook.createCellStyle();
			ExcelUtils.setBorders4Style(cellStyle);
			cellStyle.setWrapText(true);
			CellStyle dateTimeStyle = wbook.createCellStyle();
			DataFormat format = wbook.createDataFormat();
//			dateTimeStyle.cloneStyleFrom(templateRow.getCell(0).getCellStyle());
			dateTimeStyle.setDataFormat(format.getFormat("dd/MM/yyyy"));
			ExcelUtils.setBorders4Style(dateTimeStyle);
			for (SNSCaseDto snsCaseDto : list) {
				
				if (rowIndex >= 2) {
					sheet.setAutoFilter(CellRangeAddress.valueOf("A2:S" + rowIndex));
				}
				
				row = sheet.createRow(rowIndex++);
				row.setHeightInPoints(22);
				
				//STT
				cell =row.createCell(colIndex++);
				cell.setCellValue(seq+=1);				
				cell.setCellStyle(cellStyle);
				
				//Tỉnh thành org
				cell =row.createCell(colIndex++);
				try {
					cell.setCellValue(snsCaseDto.getOrganization().getAddress().getProvince().getName());
				} catch (Exception e) {
					cell.setCellValue("");
				}			
				cell.setCellStyle(cellStyle);
				
				//Org
				cell =row.createCell(colIndex++);
				try {
					cell.setCellValue(snsCaseDto.getOrganization().getName());
				} catch (Exception e) {
					cell.setCellValue("");
				}			
				cell.setCellStyle(cellStyle);
				
				//Mã khách hàng
				cell =row.createCell(colIndex++);
				cell.setCellValue(snsCaseDto.getCouponCode());				
				cell.setCellStyle(cellStyle);
				
				//Tên
				cell =row.createCell(colIndex++);
				cell.setCellValue(snsCaseDto.getName());				
				cell.setCellStyle(cellStyle);
				
				//Năm sinh
				cell =row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				if(snsCaseDto.getDob()!=null) {
					cell.setCellValue(snsCaseDto.getDob().getYear());
				}
				
				//Giới tính
				cell =row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				if(snsCaseDto.getGender()!=null) {
					if(snsCaseDto.getGender()==Gender.MALE) {
						cell.setCellValue("Nam");
					}
					else if(snsCaseDto.getGender()==Gender.FEMALE){
						cell.setCellValue("Nữ");
					}
					else if(snsCaseDto.getGender()==Gender.OTHER){
						cell.setCellValue("Khác");
					}
					else if(snsCaseDto.getGender()==Gender.TRANSGENDER){
						cell.setCellValue("Chuyển giới");
					}		
				}
				
				//giấy tờ tùy thân cột 1.cccd,cmnd,thẻ BHYT
				cell =row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				if(snsCaseDto.getIdNumberType()!=null) {
//					if(snsCaseDto.getIdNumberType()==SNSIdNumberType.BANG_LAI) {
//						cell.setCellValue("Bằng lái xe: "+snsCaseDto.getIdNumber());
//					}
//					else 
					if(snsCaseDto.getIdNumberType()==SNSIdNumberType.CCCD) {
						cell.setCellValue("Căn cước công dân");
					}
					else if(snsCaseDto.getIdNumberType()==SNSIdNumberType.CMND) {
						cell.setCellValue("Chứng minh nhân dân");
					}
//					else if(snsCaseDto.getIdNumberType()==SNSIdNumberType.HO_KHAU) {
//						cell.setCellValue("Số hộ khẩu: "+snsCaseDto.getIdNumber());
//					}
//					else if(snsCaseDto.getIdNumberType()==SNSIdNumberType.SDT) {
//						cell.setCellValue("Số điện thoại: "+snsCaseDto.getIdNumber());
//					}
					else if(snsCaseDto.getIdNumberType()==SNSIdNumberType.THE_BH) {
						cell.setCellValue("Thẻ bảo hiểm y tế");
					}
//					cell.setCellValue(snsCaseDto.getIdNumberType().toString()+":"+snsCaseDto.getIdNumber());					
				}
				
				//giấy tờ tùy thân cột 2.mã số
				cell =row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				if(snsCaseDto.getIdNumberType()!=null) {
//					if(snsCaseDto.getIdNumberType()==SNSIdNumberType.BANG_LAI) {
//						cell.setCellValue("Bằng lái xe: "+snsCaseDto.getIdNumber());
//					}
//					else 
					if(snsCaseDto.getIdNumberType()==SNSIdNumberType.CCCD) {
						cell.setCellValue(snsCaseDto.getIdNumber());
					}
					else if(snsCaseDto.getIdNumberType()==SNSIdNumberType.CMND) {
						cell.setCellValue(snsCaseDto.getIdNumber());
					}
//					else if(snsCaseDto.getIdNumberType()==SNSIdNumberType.HO_KHAU) {
//						cell.setCellValue("Số hộ khẩu: "+snsCaseDto.getIdNumber());
//					}
//					else if(snsCaseDto.getIdNumberType()==SNSIdNumberType.SDT) {
//						cell.setCellValue("Số điện thoại: "+snsCaseDto.getIdNumber());
//					}
					else if(snsCaseDto.getIdNumberType()==SNSIdNumberType.THE_BH) {
						cell.setCellValue(snsCaseDto.getIdNumber());
					}
//					cell.setCellValue(snsCaseDto.getIdNumberType().toString()+":"+snsCaseDto.getIdNumber());					
				}
				
				//Nhóm nguy cơ
				cell =row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				if(snsCaseDto.getRiskGroup()!=null) {
					if(snsCaseDto.getRiskGroup()==SNSRiskGroup.MD) {
						cell.setCellValue("Mại dâm");
					}
					else if(snsCaseDto.getRiskGroup()==SNSRiskGroup.MSM) {
						cell.setCellValue("MSM");
					}
					else if(snsCaseDto.getRiskGroup()==SNSRiskGroup.NCH) {
						cell.setCellValue("Bạn tình/bạn chích NCH");
					}
					else if(snsCaseDto.getRiskGroup()==SNSRiskGroup.TCMT) {
						cell.setCellValue("Tiêm chích ma túy");
					}
					else if(snsCaseDto.getRiskGroup()==SNSRiskGroup.OTHER) {
						cell.setCellValue("Khác");
					}
//					cell.setCellValue(snsCaseDto.getRiskGroup().getNumber());
				}
				
				//Ngày giới thiệu dịch vụ SNS
				cell =row.createCell(colIndex++);
				cell.setCellStyle(dateTimeStyle);
				if(snsCaseDto.getFirstTimeVisit()!=null) {
					cell.setCellValue(Date.from(snsCaseDto.getFirstTimeVisit().toInstant(ZoneOffset.of("+7"))));										
				}
				
				
				
				//CMND/CCCD/số thẻ BH/Bằng LX/Số ĐT/Số hộ khẩu
//				cell =row.createCell(colIndex++);
//				cell.setCellStyle(cellStyle);
//				if(snsCaseDto.getIdNumberType()!=null) {
//					if(snsCaseDto.getIdNumberType()==SNSIdNumberType.BANG_LAI) {
//						cell.setCellValue("Bằng lái xe: "+snsCaseDto.getIdNumber());
//					}
//					else if(snsCaseDto.getIdNumberType()==SNSIdNumberType.CCCD) {
//						cell.setCellValue("Căn cước công dân: "+snsCaseDto.getIdNumber());
//					}
//					else if(snsCaseDto.getIdNumberType()==SNSIdNumberType.CMND) {
//						cell.setCellValue("Chứng minh nhân dân: "+snsCaseDto.getIdNumber());
//					}
//					else if(snsCaseDto.getIdNumberType()==SNSIdNumberType.HO_KHAU) {
//						cell.setCellValue("Số hộ khẩu: "+snsCaseDto.getIdNumber());
//					}
//					else if(snsCaseDto.getIdNumberType()==SNSIdNumberType.SDT) {
//						cell.setCellValue("Số điện thoại: "+snsCaseDto.getIdNumber());
//					}
//					else if(snsCaseDto.getIdNumberType()==SNSIdNumberType.THE_BH) {
//						cell.setCellValue("Thẻ bảo hiểm: "+snsCaseDto.getIdNumber());
//					}
////					cell.setCellValue(snsCaseDto.getIdNumberType().toString()+":"+snsCaseDto.getIdNumber());					
//				}
				
				
				//Nguồn khách hàng
				cell =row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				if(snsCaseDto.getCustomerSource()!=null) {
					if(snsCaseDto.getCustomerSource()==SNSCustomerSource.CBOs) {
						cell.setCellValue("CBOs");
					}
					else if(snsCaseDto.getCustomerSource()==SNSCustomerSource.OTHER) {
						cell.setCellValue("Khác/Tự đến");
					}
					else if(snsCaseDto.getCustomerSource()==SNSCustomerSource.SNS) {
						cell.setCellValue("SNS");
					}
					else if(snsCaseDto.getCustomerSource()==SNSCustomerSource.VCT_OPC) {
						cell.setCellValue("VCT/OPC");
					}
//					cell.setCellValue(snsCaseDto.getCustomerSource().getNumber());					
				}
				
				//Hình thức tiếp cận
				cell =row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				if(snsCaseDto.getApproachMethod()!=null) {
					if(snsCaseDto.getApproachMethod()==SNSApproachMethod.direct) {
						cell.setCellValue("Trực tiếp");
					}
					else if(snsCaseDto.getApproachMethod()==SNSApproachMethod.online) {
						cell.setCellValue("Trực tuyến");
					}
//					cell.setCellValue(snsCaseDto.getApproachMethod().getNumber());					
				}
				
				//Tình trạng HIV
				cell =row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				if(snsCaseDto.getHivStatus()!=null) {
					if(snsCaseDto.getHivStatus()==HIVStatus.negative) {
						cell.setCellValue("Âm tính");
					}
					else if(snsCaseDto.getHivStatus()==HIVStatus.positive) {
						cell.setCellValue("Dương tính");
					}
					else if(snsCaseDto.getHivStatus()==HIVStatus.notest) {
						cell.setCellValue("Không làm xét nghiệm");
					}
					else if(snsCaseDto.getHivStatus()==HIVStatus.undefined) {
						cell.setCellValue("Không xác định");
					}
//					cell.setCellValue(snsCaseDto.getHivStatus().getNumber());						
				}
				
				//Ngày xét nghiệm
				cell =row.createCell(colIndex++);
				cell.setCellStyle(dateTimeStyle);
				if(snsCaseDto.getTestDate()!=null) {
					cell.setCellValue(Date.from(snsCaseDto.getTestDate().toInstant(ZoneOffset.of("+7"))));										
				}
				
				//Ngày đăng ký sử dụng PrEP
				cell =row.createCell(colIndex++);
				cell.setCellStyle(dateTimeStyle);
				if(snsCaseDto.getPrepDate()!=null) {
//					cell.setCellValue(CommonUtils.fromLocalDateTime(snsCaseDto.getPrepDate()));
					cell.setCellValue(Date.from(snsCaseDto.getPrepDate().toInstant(ZoneOffset.of("+7"))));
				}
				
				//Ngày đăng ký điều trị ARV
				cell =row.createCell(colIndex++);
				cell.setCellStyle(dateTimeStyle);
				if(snsCaseDto.getArvDate()!=null) {
					cell.setCellValue(Date.from(snsCaseDto.getArvDate().toInstant(ZoneOffset.of("+7"))));										
				}
				
				
//				//Nguồn khách hàng
//				cell =row.createCell(colIndex++);
//				cell.setCellStyle(cellStyle);
//				if(snsCaseDto.getCustomerSource()!=null) {
//					if(snsCaseDto.getCustomerSource()==SNSCustomerSource.CBOs) {
//						cell.setCellValue("CBOs");
//					}
//					else if(snsCaseDto.getCustomerSource()==SNSCustomerSource.OTHER) {
//						cell.setCellValue("Khác/Tự đến");
//					}
//					else if(snsCaseDto.getCustomerSource()==SNSCustomerSource.SNS) {
//						cell.setCellValue("SNS");
//					}
//					else if(snsCaseDto.getCustomerSource()==SNSCustomerSource.VCT_OPC) {
//						cell.setCellValue("VCT/OPC");
//					}
////					cell.setCellValue(snsCaseDto.getCustomerSource().getNumber());					
//				}
//				
//				//Hình thức tiếp cận
//				cell =row.createCell(colIndex++);
//				cell.setCellStyle(cellStyle);
//				if(snsCaseDto.getApproachMethod()!=null) {
//					if(snsCaseDto.getApproachMethod()==SNSApproachMethod.direct) {
//						cell.setCellValue("Trực tiếp");
//					}
//					else if(snsCaseDto.getApproachMethod()==SNSApproachMethod.online) {
//						cell.setCellValue("Trực tuyến");
//					}
////					cell.setCellValue(snsCaseDto.getApproachMethod().getNumber());					
//				}
				
				//Tổng số thẻ phát cho CTV
				cell =row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				if(snsCaseDto.getTotalCoupon()!=null) {
					cell.setCellValue(snsCaseDto.getTotalCoupon());
				}
				
				//Mã số thẻ
				cell =row.createCell(colIndex++);				
				cell.setCellStyle(cellStyle);
				String subCouponCode= "";
				if(snsCaseDto.getChildren()!=null && snsCaseDto.getChildren().size()>0) {
//					HashSet<LocalDateTime> setIssueDate = new HashSet<LocalDateTime>(snsCaseDto.getChildren().stream().map(SNSCaseDto::getIssueDate).collect(Collectors.toList()));					
					Hashtable<LocalDateTime, List<SNSCaseDto>> hashtable = new Hashtable<LocalDateTime, List<SNSCaseDto>>();
					
			        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

					for (SNSCaseDto child : snsCaseDto.getChildren()) {
//						subCouponCode+=" "+child.getCouponCode()+",";
						if(hashtable.get(child.getIssueDate())!=null) {
							hashtable.get(child.getIssueDate()).add(child);
						}
						else {
							hashtable.put(child.getIssueDate(), new ArrayList<SNSCaseDto>());
							hashtable.get(child.getIssueDate()).add(child);
						}
					}
					if(!hashtable.isEmpty()) {
						for (LocalDateTime key : hashtable.keySet()) {
							subCouponCode+=key.format(formatter)+":";
							for (SNSCaseDto childHashed : hashtable.get(key)) {
								subCouponCode+=" "+childHashed.getCouponCode()+",";
							}
							if(subCouponCode.length()>1) {
//								subCouponCode = subCouponCode.replace(subCouponCode.charAt(subCouponCode.length()-1), ' ');
								//Xóa dấu phảy ở cuối cùng
								subCouponCode = new StringBuilder(subCouponCode).deleteCharAt(subCouponCode.length()-1).toString();
							}
							subCouponCode+="\n";
						}
					}
				}
//				if(subCouponCode.length()>1) {
//					subCouponCode = subCouponCode.replace(subCouponCode.charAt(subCouponCode.length()-1), ' ');
//				}
				
				cell.setCellValue(subCouponCode);
				
//				//Ngày đăng ký sử dụng PrEP
//				cell =row.createCell(colIndex++);
//				cell.setCellStyle(dateTimeStyle);
//				if(snsCaseDto.getPrepDate()!=null) {
////					cell.setCellValue(CommonUtils.fromLocalDateTime(snsCaseDto.getPrepDate()));
//					cell.setCellValue(Date.from(snsCaseDto.getPrepDate().toInstant(ZoneOffset.of("+7"))));
//				}
				
//				//Ngày đăng ký điều trị ARV
//				cell =row.createCell(colIndex++);
//				cell.setCellStyle(dateTimeStyle);
//				if(snsCaseDto.getArvDate()!=null) {
//					cell.setCellValue(Date.from(snsCaseDto.getArvDate().toInstant(ZoneOffset.of("+7"))));										
//				}
				colIndex=0;
			}
			return wbook;
		}
	}
	@Override
	public SNSCaseReportDto getReport(List<Long> orgIds,LocalDateTime from,LocalDateTime to) {
		User currentUser = SecurityUtils.getCurrentUser();
		Boolean isAdministrator = SecurityUtils.isUserInRole(currentUser, "ROLE_ADMIN");
		
		final List<Long> writableOrgIds = authUtils.getGrantedOrgIds(Permission.WRITE_ACCESS);
		final List<Long> readableOrgIds = authUtils.getGrantedOrgIds(Permission.READ_ACCESS);
		
		for (int i = 0; i < orgIds.size(); i++) {
			if(!writableOrgIds.contains(orgIds.get(i)) && !readableOrgIds.contains(orgIds.get(i)) && !isAdministrator) {
				orgIds.remove(i);
				i-=1;
				if(orgIds.size()>0) {
					continue;
				}
				else {
					break;
				}
			}
		}
		if(orgIds.size()>0 || isAdministrator) {
			String orgName="";
			String orgNameTemp="";
			String provinceName="";
			for (int i = 0; i < orgIds.size(); i++) {
				Organization org = orgRepos.findOne(orgIds.get(i));
				if(org!=null) {
					orgName+=org.getName()+" ";
					if(org.getAddress()!=null && org.getAddress().getProvince()!=null && !org.getAddress().getProvince().getName().equals(orgNameTemp)) {
						provinceName+=org.getAddress().getProvince().getName()+";";
						orgNameTemp = org.getAddress().getProvince().getName().trim();
					}
				}				
			}
			SNSCaseReportDto ret = new SNSCaseReportDto();
			ret.setFromDate(from);
			ret.setToDate(to);
			ret.setProvinceName(provinceName);
			ret.setOrgName(orgName);
			
			
			//Số người được mời làm cộng tác viên (Để trống)
			SNSCaseReportDetailDto detail1 = this.getDetailReport(orgIds, from, to, 1);
			detail1.setOrderNumber(1);
			detail1.setName("Số người được mời làm cộng tác viên");
			ret.getListDetail().add(detail1);
			
//			2	Số người đồng ý tham gia làm cộng tác viên
			SNSCaseReportDetailDto detail2 = this.getDetailReport(orgIds, from, to, 2);
			detail2.setOrderNumber(2);
			detail2.setName("Số người đồng ý tham gia làm cộng tác viên");
			ret.getListDetail().add(detail2);
			
//			3	Số thẻ chuyển gửi được phát ra
			SNSCaseReportDetailDto detail3 = this.getDetailReport(orgIds, from, to, 3);
			detail3.setOrderNumber(3);
			detail3.setName("Số thẻ chuyển gửi được phát ra");
			ret.getListDetail().add(detail3);
			
//			4	Số thẻ chuyển gửi quay lại cơ sở
			SNSCaseReportDetailDto detail4 = this.getDetailReport(orgIds, from, to, 4);
			detail4.setOrderNumber(4);
			detail4.setName("Số thẻ chuyển gửi quay lại cơ sở");
			ret.getListDetail().add(detail4);
			
//			5	Số khách hàng được làm XN HIV
			SNSCaseReportDetailDto detail5 = this.getDetailReport(orgIds, from, to, 5);
			detail5.setOrderNumber(5);
			detail5.setName("Số khách hàng được làm XN HIV");
			ret.getListDetail().add(detail5);
			
//			6	
			SNSCaseReportDetailDto detail6 = this.getDetailReport(orgIds, from, to, 6);
			detail6.setOrderNumber(6);
			detail6.setName("Số khách hàng có KQXN HIV dương tính");
			ret.getListDetail().add(detail6);
			
//			7	Số khách hàng có KQXN HIV dương tính được kết nối điều trị ARV
			SNSCaseReportDetailDto detail7= this.getDetailReport(orgIds, from, to, 7);
			detail7.setOrderNumber(7);
			detail7.setName("Số khách hàng có KQXN HIV dương tính được kết nối điều trị ARV");
			ret.getListDetail().add(detail7);
			
//			8	Số khách hàng có KQXN HIV âm tính
			SNSCaseReportDetailDto detail8 = this.getDetailReport(orgIds, from, to, 8);
			detail8.setOrderNumber(8);
			detail8.setName("Số khách hàng có KQXN HIV âm tính");
			ret.getListDetail().add(detail8);
			
//			9	Số khách hàng có KQXN HIV âm tính được kết nối PrEP
			SNSCaseReportDetailDto detail9 = this.getDetailReport(orgIds, from, to, 9);
			detail9.setOrderNumber(9);
			detail9.setName("Số khách hàng có KQXN HIV âm tính được kết nối PrEP");
			ret.getListDetail().add(detail9);
			return ret;
		}
		else {
			return null;
		}
	}
	
	public SNSCaseReportDetailDto getDetailReport(List<Long> orgIds,LocalDateTime from,LocalDateTime to, int type) {
		SNSCaseReportDetailDto detail = new SNSCaseReportDetailDto();
		//Total
		List<Object[]> list = this.queryReport(orgIds, from, to, type, "");		
		if(list!=null && list.size()>0) {
			Object val = list.get(0);
			Long retTotal = (Long) val;
			if(retTotal!=null) {
				detail.setTotal(retTotal.intValue());
			}
		}
		
		if(detail.getTotal()!=null && detail.getTotal()>0) {
			//GROUP BY SNSRiskGroup
			list = this.queryReport(orgIds, from, to, type, SNSRiskGroup.class.getSimpleName());
			if(list!=null && list.size()>0) {
				for (Object[] objects : list) {
					if((SNSRiskGroup)objects[1]==SNSRiskGroup.MD) {
						detail.setRiskGroupMD(((Long)objects[0]).intValue());
					}
					if((SNSRiskGroup)objects[1]==SNSRiskGroup.MSM) {
						detail.setRiskGroupMSM(((Long)objects[0]).intValue());
					}
					if((SNSRiskGroup)objects[1]==SNSRiskGroup.NCH) {
						detail.setRiskGroupNCH(((Long)objects[0]).intValue());
					}
					if((SNSRiskGroup)objects[1]==SNSRiskGroup.OTHER) {
						detail.setRiskGroupOTHER(((Long)objects[0]).intValue());
					}
					if((SNSRiskGroup)objects[1]==SNSRiskGroup.TCMT) {
						detail.setRiskGroupTCMT(((Long)objects[0]).intValue());
					}
				}
			}
			//GROUP BY SNSCustomerSource
			list = this.queryReport(orgIds, from, to, type, SNSCustomerSource.class.getSimpleName());
			if(list!=null && list.size()>0) {
				for (Object[] objects : list) {
					if((SNSCustomerSource)objects[1]==SNSCustomerSource.CBOs) {
						detail.setCustomerSourceCBOs(((Long)objects[0]).intValue());
					}
					if((SNSCustomerSource)objects[1]==SNSCustomerSource.SNS) {
						detail.setCustomerSourceSNS(((Long)objects[0]).intValue());
					}
					if((SNSCustomerSource)objects[1]==SNSCustomerSource.VCT_OPC) {
						detail.setCustomerSourceVCTOPC(((Long)objects[0]).intValue());
					}
					if((SNSCustomerSource)objects[1]==SNSCustomerSource.OTHER) {
						detail.setCustomerSourceOTHER(((Long)objects[0]).intValue());
					}
				}
			}
			//GROUP BY SNSApproachMethod
			list = this.queryReport(orgIds, from, to, type, SNSApproachMethod.class.getSimpleName());
			if(list!=null && list.size()>0) {
				for (Object[] objects : list) {
					if((SNSApproachMethod)objects[1]==SNSApproachMethod.direct) {
						detail.setApproachMethodDirect(((Long)objects[0]).intValue());
					}
					if((SNSApproachMethod)objects[1]==SNSApproachMethod.online) {
						detail.setApproachMethodOnline(((Long)objects[0]).intValue());
					}
				}
			}
		}
		return detail;
	}
	
	public List<Object[]> queryReport(List<Long> orgIds,LocalDateTime from,LocalDateTime to, int type, String groupByCode) {		
		String SQL=" SELECT COUNT(s.id) as count %s FROM SNSCase s WHERE 1=1  ";
		String selectClause="";
		String whereClause="";
		if(orgIds!=null && orgIds.size()>0) {
			whereClause+=" AND s.organization.id in (:orgIds) ";
		}
		if(type==1) {
			whereClause+=" AND s.firstTimeVisit is not null AND s.firstTimeVisit >= :from AND s.firstTimeVisit <= :to ";
		}
		if(type==2) {
			whereClause+=" AND s.id in "
					+ " ("
					+ " SELECT s.parent.id FROM SNSCase s WHERE s.parent is not null GROUP BY s.parent.id HAVING MIN(s.issueDate) >= :from AND MIN(s.issueDate) <= :to "
					+ ") ";
		}
		if(type==3) {
			whereClause+=" AND s.issueDate is not null AND s.issueDate >= :from AND s.issueDate <= :to ";
		}
		if(type==4) {
			whereClause+=" AND s.firstTimeVisit is not null "
					+ " AND s.firstTimeVisit >= :from "
					+ " AND s.firstTimeVisit <= :to "
					+ " AND s.parent is not null ";
		}
		if(type==5) {
			whereClause+=" AND s.testDate is not null "
					+ " AND s.testDate >= :from "
					+ " AND s.testDate <= :to "
					+ " AND s.hivStatus is not null "
					+ " AND s.hivStatus <> :notest ";
		}
		if(type==6) {
			whereClause+=" AND s.testDate is not null "
					+ " AND s.testDate >= :from "
					+ " AND s.testDate <= :to "
					+ " AND s.hivStatus is not null "
					+ " AND s.hivStatus = :positive ";
		}
		if(type==7) {
			whereClause+=" AND s.arvDate is not null "
					+ " AND s.arvDate >= :from "
					+ " AND s.arvDate <= :to "
					+ " AND s.hivStatus is not null "
					+ " AND s.hivStatus = :positive ";
		}
		if(type==8) {
			whereClause+=" AND s.testDate is not null "
					+ " AND s.testDate >= :from "
					+ " AND s.testDate <= :to "
					+ " AND s.hivStatus is not null "
					+ " AND s.hivStatus = :negative ";
		}
		if(type==9) {
			whereClause+=" AND s.prepDate is not null "
					+ " AND s.prepDate >= :from "
					+ " AND s.prepDate <= :to "
					+ " AND s.hivStatus is not null "
					+ " AND s.hivStatus = :negative ";
		}
		String groupByClause="";
		if(StringUtils.hasText(groupByCode)) {
			if(groupByCode.equals(SNSRiskGroup.class.getSimpleName())) {
				groupByClause=" GROUP BY s.riskGroup ";
				selectClause=" , s.riskGroup ";
			}
			if(groupByCode.equals(SNSCustomerSource.class.getSimpleName())) {
				groupByClause=" GROUP BY s.customerSource ";
				selectClause=" , s.customerSource ";
			}
			if(groupByCode.equals(SNSApproachMethod.class.getSimpleName())) {
				groupByClause=" GROUP BY s.approachMethod ";
				selectClause=" , s.approachMethod ";
			}
		}
		SQL=String.format(SQL, selectClause);
		Query q = manager.createQuery(SQL+whereClause+groupByClause);
		if(orgIds!=null && orgIds.size()>0) {
			q.setParameter("orgIds", orgIds);
		}
		
		if(type>=1 && type <=9) {
			q.setParameter("from", from);
			q.setParameter("to", to);
		}		
		
		if(type==5) {
			q.setParameter("notest", HIVStatus.notest);
		}
		if(type==6 || type==7) {
			q.setParameter("positive", HIVStatus.positive);
		}
		if(type==8 || type==9) {
			q.setParameter("negative", HIVStatus.negative);		
		}
		List<Object[]> ret = q.getResultList();
		return ret;
	}

	@Override
	@Transactional(readOnly = true)
	public Workbook exportReportSNSCase(SNSCaseFilterDto filter) {
		SXSSFWorkbook blankBook = new SXSSFWorkbook();
		blankBook.createSheet();
//		filter.setDisablePaging(true);
//		filter.setIsFullDto(true);
		
		if(filter.getProvinceId()!=null && CollectionUtils.isEmpty(filter.getOrgIds())) {
			filter.setOrgIds(organizationRepository.findAllIdByProvinceId(filter.getProvinceId()));
		}
		SNSCaseReportDto snsCaseReportDto = this.getReport(filter.getOrgIds(), filter.getFromDate(), filter.getToDate());
		if(snsCaseReportDto==null || snsCaseReportDto.getListDetail().size()==0) {
			return blankBook;
		}
		else {
			XSSFWorkbook wbook = null;
//			SXSSFWorkbook wbook = null;
			try (InputStream template = context.getResource("classpath:templates/sns-report.xlsx")
					.getInputStream()) {
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
			int rowIndex = 8;
			int colIndex = 0;
			
			Row row = null;
			Cell cell = null;
			Sheet sheet = wbook.getSheetAt(0);

			int seq=0;
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
			
			row =sheet.getRow(2);
			
			cell =row.createCell(2);
			cell.setCellValue(snsCaseReportDto.getProvinceName());				
			
			
			row =sheet.getRow(3);
			cell =row.createCell(2);
			cell.setCellValue(snsCaseReportDto.getOrgName());				
			
			
			row =sheet.getRow(4);
			cell =row.getCell(2);
			//cell.setCellStyle(dateTimeStyle);
			if(snsCaseReportDto.getFromDate()!=null) {
				cell.setCellValue(Date.from(snsCaseReportDto.getFromDate().toInstant(ZoneOffset.of("+7"))));
			}
			
			
//			row =sheet.createRow(3);
			cell =row.getCell(4);
			//cell.setCellStyle(dateTimeStyle);
			if(snsCaseReportDto.getToDate()!=null) {
				cell.setCellValue(Date.from(snsCaseReportDto.getToDate().toInstant(ZoneOffset.of("+7"))));
			}
			
			for (SNSCaseReportDetailDto snsCaseReportDetailDto : snsCaseReportDto.getListDetail()) {
				
				row = sheet.getRow(rowIndex++);
//				row.setHeightInPoints(22);
				
				//STT
				cell =row.getCell(0);
				cell.setCellValue(seq+=1);				
				//cell.setCellStyle(cellStyle);
	 			
				//name
				cell =row.getCell(1);
				cell.setCellValue(snsCaseReportDetailDto.getName());				
				//cell.setCellStyle(cellStyle);
				
				//tổng số
				cell =row.getCell(2);
				if(snsCaseReportDetailDto.getTotal()!=null) {
					cell.setCellValue(snsCaseReportDetailDto.getTotal());		
				}	
				//cell.setCellStyle(cellStyle);
				
				//riskGroupMSM
				cell =row.getCell(3);
				if(snsCaseReportDetailDto.getRiskGroupMSM()!=null) {
					cell.setCellValue(snsCaseReportDetailDto.getRiskGroupMSM());	
				}			
				//cell.setCellStyle(cellStyle);
				
				//NCH
				cell =row.getCell(4);
				if(snsCaseReportDetailDto.getRiskGroupNCH()!=null) {
					cell.setCellValue(snsCaseReportDetailDto.getRiskGroupNCH());		
				}		
				//cell.setCellStyle(cellStyle);
				
				//TCMT
				cell =row.getCell(5);
				if(snsCaseReportDetailDto.getRiskGroupTCMT()!=null) {
					cell.setCellValue(snsCaseReportDetailDto.getRiskGroupTCMT());
				}
				//cell.setCellStyle(cellStyle);
				
				//Mại dâm
				cell =row.getCell(6);
				if(snsCaseReportDetailDto.getRiskGroupMD()!=null) {
					cell.setCellValue(snsCaseReportDetailDto.getRiskGroupMD());
				}			
				//cell.setCellStyle(cellStyle);
				
				//khác
				cell =row.getCell(7);
				if(snsCaseReportDetailDto.getRiskGroupOTHER()!=null) {
					cell.setCellValue(snsCaseReportDetailDto.getRiskGroupOTHER());	
				}			
				//cell.setCellStyle(cellStyle);
				
				//SNS
				cell =row.getCell(8);
				if(snsCaseReportDetailDto.getCustomerSourceSNS()!=null) {
					cell.setCellValue(snsCaseReportDetailDto.getCustomerSourceSNS());	
				}	
				//cell.setCellStyle(cellStyle);
				
				//VCTOPC
				cell =row.getCell(9);
				if(snsCaseReportDetailDto.getCustomerSourceVCTOPC()!=null) {
					cell.setCellValue(snsCaseReportDetailDto.getCustomerSourceVCTOPC());
				}			
				//cell.setCellStyle(cellStyle);
				
				//CBOs
				cell =row.getCell(10);
				if(snsCaseReportDetailDto.getCustomerSourceCBOs()!=null) {
					cell.setCellValue(snsCaseReportDetailDto.getCustomerSourceCBOs());
				}				
				//cell.setCellStyle(cellStyle);
				
				//khác
				cell =row.getCell(11);
				if(snsCaseReportDetailDto.getCustomerSourceOTHER()!=null) {
					cell.setCellValue(snsCaseReportDetailDto.getCustomerSourceOTHER());	
				}			
				//cell.setCellStyle(cellStyle);
				
				//
				cell =row.getCell(12);
				if(snsCaseReportDetailDto.getApproachMethodDirect()!=null) {
					cell.setCellValue(snsCaseReportDetailDto.getApproachMethodDirect());
				}			
				//cell.setCellStyle(cellStyle);
				
				cell =row.getCell(13);
				if(snsCaseReportDetailDto.getApproachMethodOnline()!=null) {
					cell.setCellValue(snsCaseReportDetailDto.getApproachMethodOnline());	
				}		
				//cell.setCellStyle(cellStyle);
				
				
				
				colIndex=0;
			}
			return wbook;
		}
	}
	@Override
	public List<OrganizationDto> getListSNSWriteAble() {
		User currentUser = SecurityUtils.getCurrentUser();
		if (currentUser == null) {
			return null;
		}
		List<OrganizationDto> ret = new ArrayList<OrganizationDto>();
		List<UserOrganization> listUO = userOrganizationRepository.getListByUserId(currentUser.getId());
		for (UserOrganization userOrganization : listUO) {
			if (userOrganization.getWriteAccess() != null && userOrganization.getWriteAccess()
					&& userOrganization.getSnsRole()!=null && userOrganization.getSnsRole()) {
				ret.add(new OrganizationDto(userOrganization.getOrganization()));
			}
		}
		return ret;
	}
}
