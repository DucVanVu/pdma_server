package org.pepfar.pdma.app.data.service.jpa;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.pepfar.pdma.app.data.aop.Loggable;
import org.pepfar.pdma.app.data.domain.AdminUnit;
import org.pepfar.pdma.app.data.domain.AdminUnitEditTable;
import org.pepfar.pdma.app.data.domain.QAdminUnit;
import org.pepfar.pdma.app.data.domain.User;
import org.pepfar.pdma.app.data.dto.AdminUnitDto;
import org.pepfar.pdma.app.data.dto.AdminUnitEditTableDto;
import org.pepfar.pdma.app.data.dto.AdminUnitFilterDto;
import org.pepfar.pdma.app.data.dto.PreventionFilterDto;
import org.pepfar.pdma.app.data.repository.AdminUnitEditTableRepository;
import org.pepfar.pdma.app.data.repository.AdminUnitRepository;
import org.pepfar.pdma.app.data.service.AdminUnitEditTableService;
import org.pepfar.pdma.app.data.service.AdminUnitService;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class AdminUnitEditTableServiceImpl implements AdminUnitEditTableService {

	@Autowired
	private AdminUnitRepository repos;

	@Autowired
	private AdminUnitEditTableRepository adminUnitEditTableRepository;

	@Autowired
	private AdminUnitRepository adminUnitRepository;

	@Autowired
	public EntityManager manager;

//	@SuppressWarnings("unchecked")
//	@Override
//	public Page<AdminUnitEditTableDto> findPage(PreventionFilterDto dto) {
//		if(dto!=null) {
//			String sql = "select new org.pepfar.pdma.app.data.dto.AdminUnitEditTableDto(s) FROM AdminUnitEditTable s ";
//			String whereClause = " where 1=1 ";
//			String orderBy = " order by s.editable desc ";
//			String sqlCount = "select count(*) FROM AdminUnitEditTable s ";
//			if(dto.getText()!=null) {
//				whereClause += " AND (s.adminUnit.name LIKE :text OR s.quarter LIKE :text OR s.year LIKE :text)";
//			}
//			Query query = manager.createQuery(sql + whereClause + orderBy, AdminUnitEditTableDto.class);
//			Query queryCount = manager.createQuery(sqlCount + whereClause, Long.class);
//			if(dto.getProvinceId()!=null) {
//				query.setParameter("provinceId", dto.getProvinceId());
//				queryCount.setParameter("provinceId", dto.getProvinceId());
//			}
//			if(dto.getEditable()!=null) {
//				query.setParameter("editable", dto.getEditable());
//				queryCount.setParameter("editable", dto.getEditable());
//			}
//			if (dto.getText() != null && StringUtils.hasText(dto.getText())) {
//				query.setParameter("text", '%' + dto.getText().trim() + '%');
//				queryCount.setParameter("text", '%' + dto.getText().trim() + '%');
//			}
//			if(dto.getPageIndex()==null) {
//				dto.setPageIndex(0);
//			}else {
//				if(dto.getPageIndex()>0) {
//					dto.setPageIndex(dto.getPageIndex()-1);
//				}else {
//					dto.setPageIndex(0);
//				}
//			}
//			if(dto.getPageSize()==null || dto.getPageSize()<=0) {
//				dto.setPageSize(25);
//			}
//
//			Long total = 0L;
//			Object obj = queryCount.getSingleResult();
//			if (obj != null) {
//				total = (Long) obj;
//			}
//
//			query.setFirstResult(dto.getPageIndex() * dto.getPageSize());
//			query.setMaxResults(dto.getPageSize());
//			Pageable pageable = new PageRequest(dto.getPageIndex(), dto.getPageSize());
//			Page<AdministrativeUnitEditableDto> page = new PageImpl<AdministrativeUnitEditableDto>(query.getResultList(), pageable,total);
//			return page;
//		}
//		return null;
//	}

	@Override
	public AdminUnitEditTableDto saveOrUpdate(AdminUnitEditTableDto dto) {
		if(dto!=null) {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User modifiedUser = null;
			LocalDateTime currentDate = LocalDateTime.now();
			String currentUserName = "Unknown User";
			if (authentication != null) {
				modifiedUser = (User) authentication.getPrincipal();
				currentUserName = modifiedUser.getUsername();
			}
			AdminUnitEditTable entity =null;
			if(dto.getId()!=null) {
				entity = adminUnitEditTableRepository.findOne(dto.getId());
			}
			if(entity==null) {
				entity = new AdminUnitEditTable();
				entity.setCreateDate(currentDate);
				entity.setCreatedBy(currentUserName);
			} else {
				if (!entity.getCreatedBy().equals(currentUserName)) {
					return null;
				}
				entity.setModifiedBy(currentUserName);
				entity.setModifyDate(currentDate);
			}
			if(dto.getListSelectedAdminUnit() != null && dto.getListSelectedAdminUnit().size() != 0) {
				for(int i=0; i<dto.getListSelectedAdminUnit().size();i++) {
					entity = new AdminUnitEditTable();
					entity.setCreateDate(currentDate);
					entity.setCreatedBy(currentUserName);
					AdminUnit au = null;
					au = adminUnitRepository.findOne(dto.getListSelectedAdminUnit().get(i).getId());
					entity.setAdminUnit(au);
					entity.setRoles(dto.getRoles());
					entity.setEditTable(dto.getEditTable());
					entity.setYear(dto.getYear());
					entity.setQuarter(dto.getQuarter());

					entity = adminUnitEditTableRepository.save(entity);
				}
				return new AdminUnitEditTableDto(entity);
			} else {
				AdminUnit au = null;
				if(dto.getAdminUnit()!=null && dto.getAdminUnit().getId()!=null) {
					au = adminUnitRepository.findOne(dto.getAdminUnit().getId());
					entity.setAdminUnit(au);
				}
				entity.setRoles(dto.getRoles());
				entity.setEditTable(dto.getEditTable());
				entity.setYear(dto.getYear());
				entity.setQuarter(dto.getQuarter());

				entity = adminUnitEditTableRepository.save(entity);
				if(entity!=null) {
					return new AdminUnitEditTableDto(entity);
				}else {
					return null;
				}
			}
		}
		return null;
	}

//	@Override
//	public AdminUnitEditTableDto getAdminUnitEditTableById(Long id) {
//		// TODO Auto-generated method stub
//		if (id != null) {
//			AdminUnitEditTableDto result = adminUnitEditTableRepository.getAdminUnitEditTableById(id);
//			return result;
//		}
//		return null;
//	}

//	@Override
//	public List<AdminUnitEditTableDto> getAdminUnitEditTableByAdminUnit(Long id) {
//		// TODO Auto-generated method stub
//		if (id != null) {
//			List<AdminUnitEditTableDto> result = adminUnitEditTableRepository.getAdminUnitEditTableByAdminUnit(id);
//			return result;
//		}
//		return null;
//	}

//	@Override
//	public boolean deleteById(Long id) {
//		// TODO Auto-generated method stub
//		if(id != null) {
//			if(this.adminUnitEditTableRepository.exists(id)) {
//				this.adminUnitEditTableRepository.delete(id);
//				return true;
//			}
//		}
//		return false;
//	}

}
