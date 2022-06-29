package org.pepfar.pdma.app.data.service.jpa;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.time.ZoneId;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import javax.persistence.EntityManager;
//import javax.persistence.Query;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.pepfar.pdma.app.data.dto.*;
import org.pepfar.pdma.app.data.repository.OrganizationRepository;
import org.pepfar.pdma.app.data.service.HTSCaseService;
import org.pepfar.pdma.app.data.service.PECaseService;
import org.pepfar.pdma.app.data.service.PreventionReportingService;
import org.pepfar.pdma.app.data.service.SelfTestEntryService;
import org.pepfar.pdma.app.data.types.*;
import org.pepfar.pdma.app.utils.ExcelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class PreventionReportingServiceImpl implements PreventionReportingService {
	
	@Autowired
	private EntityManager manager;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private ApplicationContext context;

	@Autowired
	private SelfTestEntryService selfTestEntryService;

	@Autowired
	private HTSCaseService htsCaseService;

	@Autowired
	private PECaseService peCaseService;
	
//	@Autowired
//	private Session session;
	
	@SuppressWarnings("unchecked")
	@Override
	public PreventionChartDto getChart1(PreventionFilterDto dto){
		PreventionChartDto ret = new PreventionChartDto();
		ret.setTitle("XN khẳng định HIV+ theo thời gian báo cáo");
		String selectType="";
		String type="";
		String selectTypeHts="";
		String typeHts="";
		if(dto.getChart1()==null) {
			selectType=", MONTH(p.c1) as month ";
			type=", MONTH(p.c1) ";
			selectTypeHts=", MONTH(p.c4_counselling_date ) as month ";
			typeHts=", MONTH(p.c4_counselling_date ) ";
		}else {
			if(dto.getChart1()==0) {
				selectType=", MONTH(p.c1) as month ";
				type=", MONTH(p.c1) ";
				selectTypeHts=", MONTH(p.c4_counselling_date ) as month ";
				typeHts=", MONTH(p.c4_counselling_date ) ";
			}
			if(dto.getChart1()==1) {
				selectType=", QUARTER(p.c1) as month ";
				type=", QUARTER(p.c1) ";
				selectTypeHts=", QUARTER(p.c4_counselling_date ) as month ";
				typeHts=", QUARTER(p.c4_counselling_date ) ";
			}
			if(dto.getChart1()==2) {
				selectType=" ";
				type=" ";
				selectTypeHts=" ";
				typeHts=" ";
			}
		}
		//Tổng số xét nghiệm từ PE02
		String sql="SELECT COUNT(id) as quantity,YEAR(p.c1) as year "+selectType + " FROM tbl_pe_case p WHERE 1=1 ";
		
		String whereClause=" AND p.c131='YES' ";
		if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c1 >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c1 < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {			
			whereClause+=" AND p.c1_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}else {
				whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}
		}
		
		String groupByClause= " GROUP BY YEAR(p.c1) "+type
							+ " ORDER BY YEAR(p.c1) "+type;
		
		Query q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+groupByClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}else {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}
		}
	
        List<PreventionChartDetailDto> lstFirst = q.list();
        
        
        sql = "SELECT COUNT(id) as quantity,YEAR(p.c1) as year " + selectType+" FROM tbl_pe_case p WHERE 1=1 ";
        whereClause=" AND p.c131='YES' AND p.c131_result='answer2' ";
        
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c1 >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c1 < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c1_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}else {
				whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}
		}
		
        groupByClause=" GROUP BY YEAR(p.c1) "+type
					+ " ORDER BY YEAR(p.c1) "+type;
        
        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+groupByClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
        if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}else {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}
		}
		
		
        List<PreventionChartDetailDto> lstSecond = q.list();
        
      //Tổng số xét nghiệm từ HTS
        sql="SELECT COUNT(id) as quantity, YEAR(p.c4_counselling_date) as year " + selectTypeHts+
        		" FROM tbl_hts_case p WHERE 1=1 ";        		
        whereClause=" AND p.c14 IS NOT NULL AND p.c14 <> 'answer4' ";
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c4_counselling_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}else {
				whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}
		}
        groupByClause=" GROUP BY YEAR(p.c4_counselling_date) " + typeHts +
        		" ORDER BY YEAR(p.c4_counselling_date) "+typeHts;
        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+groupByClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
        if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}else {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
		}
		
        List<PreventionChartDetailDto> lstHtsFirst = q.list();
        
        sql="SELECT COUNT(id) as quantity, YEAR(p.c4_counselling_date) as year " + selectTypeHts+ 
        		" FROM tbl_hts_case p WHERE 1=1 ";
        whereClause=" AND p.c14 IS NOT NULL AND p.c14 = 'answer2' ";
        groupByClause=" GROUP BY YEAR(p.c4_counselling_date) " + typeHts+ 
        				" ORDER BY YEAR(p.c4_counselling_date) " +typeHts;
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c4_counselling_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}else {
				whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}
		}	
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+groupByClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}else {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
		}
		
		List<PreventionChartDetailDto> lstHtsSecond = q.list();
        
		if(dto.getModality()!=null && dto.getModality().size()==1) {
			if(dto.getModality().get(0)==1) {
				lstFirst.clear();
				lstSecond.clear();
				lstFirst.addAll(lstHtsFirst);
				lstSecond.addAll(lstHtsSecond);
//				lstFirst.sort(Comparator.comparing(PreventionChartDetailDto::getYear).thenComparing(PreventionChartDetailDto::getMonth));
//				lstSecond.sort(Comparator.comparing(PreventionChartDetailDto::getYear).thenComparing(PreventionChartDetailDto::getMonth));
			}else {
				
			}
		}else {
			lstFirst.addAll(lstHtsFirst);
			lstSecond.addAll(lstHtsSecond);
//			lstFirst.sort(Comparator.comparing(PreventionChartDetailDto::getYear).thenComparing(PreventionChartDetailDto::getMonth));
//			lstSecond.sort(Comparator.comparing(PreventionChartDetailDto::getYear).thenComparing(PreventionChartDetailDto::getMonth));
		}
		
		
		for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
			String category = String.format("%02d", preventionChartDetailDto.getMonth())+"/"+preventionChartDetailDto.getYear();
			if(dto.getChart1()!=null && dto.getChart1()==2) {
				category = preventionChartDetailDto.getYear().toString();
			}
			if(dto.getChart1()!=null && dto.getChart1()==1) {
				category = String.format("%02d", preventionChartDetailDto.getMonth())+"/"+preventionChartDetailDto.getYear();
			}
			
	    	
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
	    	String category = String.format("%02d", preventionChartDetailDto.getMonth())+"/"+preventionChartDetailDto.getYear();
			if(dto.getChart1()!=null && dto.getChart1()==2) {
				category = preventionChartDetailDto.getYear().toString();
			}
			if(dto.getChart1()!=null && dto.getChart1()==1) {
				category = String.format("%02d", preventionChartDetailDto.getMonth())+"/"+preventionChartDetailDto.getYear();
			}
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
		return ret;
	}
	@Override
	public PreventionChartDto getChart2(PreventionFilterDto dto){
		PreventionChartDto ret = new PreventionChartDto();
		ret.setTitle("XN khẳng định HIV+ theo đơn vị hành chính");
		String orderType=" INNER JOIN tbl_admin_unit u ON u.id=l.province_id ";
		String selectType=" u.name as title ";
		String whereOrder =" GROUP BY u.name ORDER BY u.name ";
		if(dto.getChart2()==null) {
			orderType=" INNER JOIN tbl_admin_unit u ON u.id=l.province_id ";
			selectType=" u.name as title ";
			whereOrder =" GROUP BY u.name ORDER BY u.name ";
		}else {
			if(dto.getChart2()==0) {
				orderType=" INNER JOIN tbl_admin_unit u ON u.id=l.province_id ";
				selectType=" u.name as title ";
				whereOrder =" GROUP BY u.name ORDER BY u.name ";
			}
			if(dto.getChart2()==1) {
				orderType=" INNER JOIN tbl_admin_unit u ON u.id=l.district_id ";
				selectType=" u.name as title ";
				whereOrder =" GROUP BY u.name ORDER BY u.name ";
			}
			if(dto.getChart2()==2) {
				orderType=" INNER JOIN tbl_admin_unit u ON u.id=l.province_id ";
				selectType=" o.name as title ";
				whereOrder =" GROUP BY o.name ORDER BY o.name ";
			}
		}
		
		
		String whereClause ="";
		
		String sql="SELECT COUNT(p.id) as quantity,"+selectType+ " FROM tbl_pe_case p " + 
				" INNER JOIN tbl_organization_unit o ON p.c1_org_id=o.id" + 
				" INNER JOIN tbl_location l ON l.id = o.address_id" + 
				orderType + 
				" WHERE p.c131='YES' " ;
		
		
		if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c1 >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c1 < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c1_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}else {
				whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}
		}

		Query q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+whereOrder).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
        
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}else {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}
		}
		
		List<PreventionChartDetailDto> lstFirst = q.list();
		
		
        sql = "SELECT COUNT(p.id) as quantity,"+selectType+" FROM tbl_pe_case p " + 
        		" INNER JOIN tbl_organization_unit o ON p.c1_org_id=o.id" + 
        		" INNER JOIN tbl_location l ON l.id = o.address_id" + 
        		orderType + 
        		" WHERE p.c131='YES' AND  p.c131_result='answer2'" ;
        whereClause ="";
        
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c1 >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c1 < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c1_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}else {
				whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}
		}

        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+whereOrder).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
        
        if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}else {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}
		}
        List<PreventionChartDetailDto> lstSecond = q.list();
        
      //Tổng số xét nghiệm từ HTS
        sql="SELECT COUNT(p.id) as quantity,  " +selectType+ 
        		"FROM tbl_hts_case p  " + 
        		"INNER JOIN tbl_organization_unit o ON p.c2_org_id=o.id " + 
        		"INNER JOIN tbl_location l ON l.id = o.address_id " + 
        		orderType + 
        		" WHERE p.c14 IS NOT NULL AND p.c14 <> 'answer4' " ;
        whereClause = "";
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c4_counselling_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}else {
				whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}
		}
        
        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+whereOrder).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
        
        if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}else {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
		}
        
        List<PreventionChartDetailDto> lstHtsFirst = q.list();
        
        
        sql="SELECT COUNT(p.id) as quantity,  " + selectType+
        		"FROM tbl_hts_case p  " + 
        		"INNER JOIN tbl_organization_unit o ON p.c2_org_id=o.id " + 
        		"INNER JOIN tbl_location l ON l.id = o.address_id " + 
        		orderType + 
        		" WHERE p.c14 IS NOT NULL AND p.c14 = 'answer2' " ;
        whereClause = "";
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c4_counselling_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}else {
				whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}
		}
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+whereOrder).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}else {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
		}
		
	
        List<PreventionChartDetailDto> lstHtsSecond = q.list();
        
        
        if(dto.getModality()!=null && dto.getModality().size()==1) {
			if(dto.getModality().get(0)==1) {
				lstFirst.clear();
				lstSecond.clear();
				lstFirst.addAll(lstHtsFirst);
				lstSecond.addAll(lstHtsSecond);
			}else {
				
			}
		}else {
			lstFirst.addAll(lstHtsFirst);
			lstSecond.addAll(lstHtsSecond);
		}
		
		for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
	    	String category = preventionChartDetailDto.getTitle();
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
	    	String category = preventionChartDetailDto.getTitle();
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
		return ret;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public PreventionChartDto getChart5(PreventionFilterDto dto) {
		PreventionChartDto ret = new PreventionChartDto();
		ret.setTitle("Chuyển gửi thành công đến CSĐT ARV theo thời gian báo cáo");
		String selectType="";
		String type="";
		String selectTypeHts="";
		String typeHts="";
		if(dto.getChart5()==null) {
			selectType=", MONTH(p.c1) as month ";
			type=", MONTH(p.c1) ";
			selectTypeHts=", MONTH(p.c4_counselling_date ) as month ";
			typeHts=", MONTH(p.c4_counselling_date ) ";
		}else {
			if(dto.getChart5()==0) {
				selectType=", MONTH(p.c1) as month ";
				type=", MONTH(p.c1) ";
				selectTypeHts=", MONTH(p.c4_counselling_date ) as month ";
				typeHts=", MONTH(p.c4_counselling_date ) ";
			}
			if(dto.getChart5()==1) {
				selectType=", QUARTER(p.c1) as month ";
				type=", QUARTER(p.c1) ";
				selectTypeHts=", QUARTER(p.c4_counselling_date ) as month ";
				typeHts=", QUARTER(p.c4_counselling_date ) ";
			}
			if(dto.getChart5()==2) {
				selectType=" ";
				type=" ";
				selectTypeHts=" ";
				typeHts=" ";
			}
		}
		
		//Tổng số xét nghiệm từ PE02
		String sql="SELECT COUNT(id) as quantity,YEAR(p.c1) as year "+selectType+" FROM tbl_pe_case p WHERE p.c8='answer1'  ";
		
		String whereClause=" ";
		
		if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c1 >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c1 < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c1_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}else {
				whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}
		}

		
		String groupByClause= " GROUP BY YEAR(p.c1) "+ type 
							+ " ORDER BY YEAR(p.c1) "+type;
		
		Query q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+groupByClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}else {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}
		}
	
        List<PreventionChartDetailDto> lstFirst = q.list();
        
        sql = "SELECT COUNT(id) as quantity,YEAR(p.c1) as year " +selectType +" FROM tbl_pe_case p WHERE p.c8='answer1' AND p.c15 ='YES' ";
        whereClause=" ";
        
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c1 >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c1 < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c1_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}else {
				whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}
		}
		
        groupByClause=" GROUP BY YEAR(p.c1) "+type
					+ " ORDER BY YEAR(p.c1) "+type;
        
        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+groupByClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
        if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}else {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}
		}		
        List<PreventionChartDetailDto> lstSecond = q.list();
        
        //HTS
        sql="SELECT COUNT(id) as quantity, YEAR(p.c4_counselling_date) as year " +selectTypeHts+ 
        		" FROM tbl_hts_case p WHERE p.c14 = 'answer2'  " ;
        
        whereClause=" ";
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c4_counselling_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}else {
				whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}
		}
        groupByClause=" GROUP BY YEAR(p.c4_counselling_date)" + typeHts+ 
        		" ORDER BY YEAR(p.c4_counselling_date) "+typeHts;
        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+groupByClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
        if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}else {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
		}
		
        List<PreventionChartDetailDto> lstHtsFirst = q.list();
        
        sql="SELECT COUNT(id) as quantity, YEAR(p.c4_counselling_date) as year " + selectTypeHts+ 
        		" FROM tbl_hts_case p WHERE p.c14 = 'answer2' AND p.c20 = 'answer3' " ;
        
        whereClause=" ";
        groupByClause=" GROUP BY YEAR(p.c4_counselling_date) " +typeHts+ 
        				" ORDER BY YEAR(p.c4_counselling_date) "+typeHts;
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c4_counselling_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}else {
				whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}
		}
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+groupByClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}else {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
		}
		
		List<PreventionChartDetailDto> lstHtsSecond = q.list();
        
		if(dto.getModality()!=null && dto.getModality().size()==1) {
			if(dto.getModality().get(0)==1) {
				lstFirst.clear();
				lstSecond.clear();
				lstFirst.addAll(lstHtsFirst);
				lstSecond.addAll(lstHtsSecond);
			}else {
				
			}
		}else {
			lstFirst.addAll(lstHtsFirst);
			lstSecond.addAll(lstHtsSecond);
		}
		
		for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
			String category = String.format("%02d", preventionChartDetailDto.getMonth())+"/"+preventionChartDetailDto.getYear();
			if(dto.getChart5()!=null && dto.getChart5()==2) {
				category = preventionChartDetailDto.getYear().toString();
			}
			if(dto.getChart5()!=null && dto.getChart5()==1) {
				category = String.format("%02d", preventionChartDetailDto.getMonth())+"/"+preventionChartDetailDto.getYear();
			}
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
	    	String category = String.format("%02d", preventionChartDetailDto.getMonth())+"/"+preventionChartDetailDto.getYear();
			if(dto.getChart5()!=null && dto.getChart5()==2) {
				category = preventionChartDetailDto.getYear().toString();
			}
			if(dto.getChart5()!=null && dto.getChart5()==1) {
				category = String.format("%02d", preventionChartDetailDto.getMonth())+"/"+preventionChartDetailDto.getYear();
			}
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public PreventionChartDto getChart6(PreventionFilterDto dto) {
		PreventionChartDto ret = new PreventionChartDto();
		ret.setTitle("Chuyển gửi thành công đến CSĐT ARV theo đơn vị hành chính");
		//Tổng số xét nghiệm từ PE02
		String orderType=" INNER JOIN tbl_admin_unit u ON u.id=l.province_id ";
		String selectType=" u.name as title ";
		String whereOrder =" GROUP BY u.name ORDER BY u.name ";
		if(dto.getChart6()==null) {
			orderType=" INNER JOIN tbl_admin_unit u ON u.id=l.province_id ";
			selectType=" u.name as title ";
			whereOrder =" GROUP BY u.name ORDER BY u.name ";
		}else {
			if(dto.getChart6()==0) {
				orderType=" INNER JOIN tbl_admin_unit u ON u.id=l.province_id ";
				selectType=" u.name as title ";
				whereOrder =" GROUP BY u.name ORDER BY u.name ";
			}
			if(dto.getChart6()==1) {
				orderType=" INNER JOIN tbl_admin_unit u ON u.id=l.district_id ";
				selectType=" u.name as title ";
				whereOrder =" GROUP BY u.name ORDER BY u.name ";
			}
			if(dto.getChart6()==2) {
				orderType=" INNER JOIN tbl_admin_unit u ON u.id=l.province_id ";
				selectType=" o.name as title ";
				whereOrder =" GROUP BY o.name ORDER BY o.name ";
			}
		}
		
		
		String whereClause ="";
		String sql="SELECT COUNT(p.id) as quantity,"+selectType+" FROM tbl_pe_case p " + 
				" INNER JOIN tbl_organization_unit o ON p.c1_org_id=o.id" + 
				" INNER JOIN tbl_location l ON l.id = o.address_id" + 
				orderType + 
				" WHERE p.c8 = 'answer1' " ;
		
		if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c1 >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c1 < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c1_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}else {
				whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}
		}

		Query q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+whereOrder).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
        
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}else {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}
		}
		
		List<PreventionChartDetailDto> lstFirst = q.list();
		
        sql = "SELECT COUNT(p.id) as quantity,"+selectType+" FROM tbl_pe_case p " + 
        		" INNER JOIN tbl_organization_unit o ON p.c1_org_id=o.id" + 
        		" INNER JOIN tbl_location l ON l.id = o.address_id" + 
        		orderType + 
        		" WHERE p.c8 = 'answer1' AND p.c15 ='YES' ";
        
        whereClause ="";
        
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c1 >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c1 < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c1_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}else {
				whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}
		}

        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+whereOrder).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
        
        if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}else {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}
		}
        List<PreventionChartDetailDto> lstSecond = q.list();
        
        
      //Tổng số xét nghiệm từ HTS
        sql="SELECT COUNT(p.id) as quantity, " + selectType+
        		"FROM tbl_hts_case p  " + 
        		"INNER JOIN tbl_organization_unit o ON p.c2_org_id=o.id " + 
        		"INNER JOIN tbl_location l ON l.id = o.address_id " + 
        		orderType + 
        		" WHERE p.c14 = 'answer2'  " ;
        
        whereClause = "";
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c4_counselling_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}else {
				whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}
		}
        
        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+whereOrder).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
        
        if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}else {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
		}
        
        List<PreventionChartDetailDto> lstHtsFirst = q.list();
        sql="SELECT COUNT(p.id) as quantity,  " +selectType+ 
        		"FROM tbl_hts_case p  " + 
        		"INNER JOIN tbl_organization_unit o ON p.c2_org_id=o.id " + 
        		"INNER JOIN tbl_location l ON l.id = o.address_id " + 
        		orderType + 
        		" WHERE p.c14 = 'answer2' AND p.c20 = 'answer3' " ;
        
        whereClause = "";
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c4_counselling_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}else {
				whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}
		}
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+whereOrder).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}else {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
		}
		
	
        List<PreventionChartDetailDto> lstHtsSecond = q.list();
        
        
        if(dto.getModality()!=null && dto.getModality().size()==1) {
			if(dto.getModality().get(0)==1) {
				lstFirst.clear();
				lstSecond.clear();
				lstFirst.addAll(lstHtsFirst);
				lstSecond.addAll(lstHtsSecond);
			}else {
				
			}
		}else {
			lstFirst.addAll(lstHtsFirst);
			lstSecond.addAll(lstHtsSecond);
		}
		
		for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
	    	String category = preventionChartDetailDto.getTitle();
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
	    	String category = preventionChartDetailDto.getTitle();
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
		return ret;
	}
	@Override
	public PreventionChartDto getChart7(PreventionFilterDto dto) {
		PreventionChartDto ret = new PreventionChartDto();
		ret.setTitle("Chuyển gửi thành công đến CSĐT PrEP theo thời gian báo cáo");
		String selectType="";
		String type="";
		String selectTypeHts="";
		String typeHts="";
		if(dto.getChart7()==null) {
			selectType=", MONTH(p.c1) as month ";
			type=", MONTH(p.c1) ";
			selectTypeHts=", MONTH(p.c4_counselling_date ) as month ";
			typeHts=", MONTH(p.c4_counselling_date ) ";
		}else {
			if(dto.getChart7()==0) {
				selectType=", MONTH(p.c1) as month ";
				type=", MONTH(p.c1) ";
				selectTypeHts=", MONTH(p.c4_counselling_date ) as month ";
				typeHts=", MONTH(p.c4_counselling_date ) ";
			}
			if(dto.getChart7()==1) {
				selectType=", QUARTER(p.c1) as month ";
				type=", QUARTER(p.c1) ";
				selectTypeHts=", QUARTER(p.c4_counselling_date ) as month ";
				typeHts=", QUARTER(p.c4_counselling_date ) ";
			}
			if(dto.getChart7()==2) {
				selectType=" ";
				type=" ";
				selectTypeHts=" ";
				typeHts=" ";
			}
		}
		//Tổng số xét nghiệm từ PE02
		String sql="SELECT COUNT(id) as quantity,YEAR(p.c1) as year "+ selectType + " FROM tbl_pe_case p WHERE 1=1 ";
		
		String whereClause=" AND p.c14='YES' ";
		if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c1 >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c1 < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c1_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}else {
				whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}
		}
		String groupByClause= " GROUP BY YEAR(p.c1) "+type 
							+ " ORDER BY YEAR(p.c1) "+type;
		
		Query q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+groupByClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}else {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}
		}
	
        List<PreventionChartDetailDto> lstFirst = q.list();
        
        sql="SELECT COUNT(id) as quantity, YEAR(p.c4_counselling_date) as year " + selectTypeHts+ 
        		" FROM tbl_hts_case p WHERE 1=1 ";        		
        whereClause=" AND p.c16_27 = 'YES' ";
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c4_counselling_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}else {
				whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}
		}
        groupByClause=" GROUP BY YEAR(p.c4_counselling_date) " +typeHts+ 
        		" ORDER BY YEAR(p.c4_counselling_date) "+typeHts;
        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+groupByClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
        if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}else {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
		}
		
        List<PreventionChartDetailDto> lstHtsFirst = q.list();
        
        if(dto.getModality()!=null && dto.getModality().size()==1) {
			if(dto.getModality().get(0)==1) {
				lstFirst.clear();
				lstFirst.addAll(lstHtsFirst);
			}else {
				
			}
		}else {
			lstFirst.addAll(lstHtsFirst);
		}
		
		for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
			String category = String.format("%02d", preventionChartDetailDto.getMonth())+"/"+preventionChartDetailDto.getYear();
			if(dto.getChart7()!=null && dto.getChart7()==2) {
				category = preventionChartDetailDto.getYear().toString();
			}
			if(dto.getChart7()!=null && dto.getChart7()==1) {
				category = String.format("%02d", preventionChartDetailDto.getMonth())+"/"+preventionChartDetailDto.getYear();
			}
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
		return ret;
	}
	@Override
	public PreventionChartDto getChart8(PreventionFilterDto dto) {
		PreventionChartDto ret = new PreventionChartDto();
		ret.setTitle("Chuyển gửi thành công đến CSĐT PrEP theo đơn vị hành chính");
		//Tổng số xét nghiệm từ PE02
		String orderType=" INNER JOIN tbl_admin_unit u ON u.id=l.province_id ";
		String selectType=" u.name as title ";
		String whereOrder =" GROUP BY u.name ORDER BY u.name ";
		if(dto.getChart8()==null) {
			orderType=" INNER JOIN tbl_admin_unit u ON u.id=l.province_id ";
			selectType=" u.name as title ";
			whereOrder =" GROUP BY u.name ORDER BY u.name ";
		}else {
			if(dto.getChart8()==0) {
				orderType=" INNER JOIN tbl_admin_unit u ON u.id=l.province_id ";
				selectType=" u.name as title ";
				whereOrder =" GROUP BY u.name ORDER BY u.name ";
			}
			if(dto.getChart8()==1) {
				orderType=" INNER JOIN tbl_admin_unit u ON u.id=l.district_id ";
				selectType=" u.name as title ";
				whereOrder =" GROUP BY u.name ORDER BY u.name ";
			}
			if(dto.getChart8()==2) {
				orderType=" INNER JOIN tbl_admin_unit u ON u.id=l.province_id ";
				selectType=" o.name as title ";
				whereOrder =" GROUP BY o.name ORDER BY o.name ";
			}
		}
		String whereClause ="";
		
		String sql="SELECT COUNT(p.id) as quantity,"+selectType+" FROM tbl_pe_case p " + 
				" INNER JOIN tbl_organization_unit o ON p.c1_org_id=o.id" + 
				" INNER JOIN tbl_location l ON l.id = o.address_id" + 
				orderType + 
				" WHERE p.c14='YES' " ;
		
		if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c1 >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c1 < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c1_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}else {
				whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}
		}
		Query q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+whereOrder).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
        
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}else {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}
		}
		
		List<PreventionChartDetailDto> lstFirst = q.list();
		
		 //Tổng số xét nghiệm từ HTS
        sql="SELECT COUNT(p.id) as quantity,  " + selectType+
        		"FROM tbl_hts_case p  " + 
        		"INNER JOIN tbl_organization_unit o ON p.c2_org_id=o.id " + 
        		"INNER JOIN tbl_location l ON l.id = o.address_id " + 
        		orderType + 
        		" WHERE p.c16_27 = 'YES'  " ;
        
        whereClause = "";
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c4_counselling_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}else {
				whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}
		}
        
        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+whereOrder).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
        
        if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}else {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
		}
        
        List<PreventionChartDetailDto> lstHtsFirst = q.list();
        
        if(dto.getModality()!=null && dto.getModality().size()==1) {
			if(dto.getModality().get(0)==1) {
				lstFirst.clear();
				lstFirst.addAll(lstHtsFirst);
			}else {
				
			}
		}else {
			lstFirst.addAll(lstHtsFirst);
		}
		
		for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
	    	String category = preventionChartDetailDto.getTitle();
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
		return ret;
	}
	@Override
	public PreventionChartDto getChart3(PreventionFilterDto dto) {
		PreventionChartDto ret = new PreventionChartDto();
		ret.setTitle("Xét nghiệm khẳng định HIV+ theo mô hình xét nghiệm");
		//Tổng số xét nghiệm từ PE02
		
		//Cột 1
		String sql="SELECT COUNT(id) as quantity FROM tbl_pe_case p WHERE 1=1 ";
		
		String whereClause=" AND p.c131='YES' AND p.parent_id IS NOT NULL ";
		if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c1 >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c1 < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c1_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}else {
				whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}
		}
		
		Query q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}else {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}
		}
	
        List<PreventionChartDetailDto> lstFirst = q.list();
        
        
        sql="SELECT COUNT(id) as quantity FROM tbl_pe_case p WHERE 1=1 ";
		
		whereClause=" AND p.c131='YES' AND p.c131_result='answer2' AND p.parent_id IS NOT NULL ";
		if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c1 >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c1 < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c1_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}else {
				whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}
		}
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}else {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}
		}
	
        List<PreventionChartDetailDto> lstSecond = q.list();
        
        
        if(dto.getModality()!=null && dto.getModality().size()==1) {
			if(dto.getModality().get(0)==1) {
				lstFirst.clear();
				lstSecond.clear();
			}
		}
        String category = "Cộng đồng";
		for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
        
        //Cột 2
        
	    sql="SELECT COUNT(id) as quantity FROM tbl_pe_case p WHERE 1=1 ";
		
		whereClause=" AND p.c131='YES' AND p.parent_id IS NULL ";
		if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c1 >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c1 < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c1_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}else {
				whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}
		}
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}else {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}
		}
	
        lstFirst = q.list();
        
        
        sql="SELECT COUNT(id) as quantity FROM tbl_pe_case p WHERE 1=1 ";
		
		whereClause=" AND p.c131='YES' AND p.c131_result='answer2' AND p.parent_id IS NULL ";
		if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c1 >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c1 < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c1_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}else {
				whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}
		}		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}else {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}
		}
	
        lstSecond = q.list();
        
        
        if(dto.getModality()!=null && dto.getModality().size()==1) {
			if(dto.getModality().get(0)==1) {
				lstFirst.clear();
				lstSecond.clear();
			}
		}
        category = "Cộng đồng - PNS";
		for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    
	    //Cột 3
	    
	    sql="SELECT COUNT(id) as quantity " + 
        		" FROM tbl_hts_case p WHERE 1=1 ";        		
        whereClause=" AND p.c5='answer1' AND p.c14!='answer4' ";
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c4_counselling_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}else {
				whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}
		}
        
        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
        if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}else {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
		}
		
        lstFirst = q.list();
        
        sql="SELECT COUNT(id) as quantity " + 
        		" FROM tbl_hts_case p WHERE 1=1 ";        		
        whereClause=" AND p.c5='answer1' AND p.c14='answer2'  ";
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c4_counselling_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}else {
				whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}
		}
        
        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
        if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}else {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
		}
		
		lstSecond = q.list();
        
        
        if(dto.getModality()!=null && dto.getModality().size()==1) {
			if(dto.getModality().get(0)==2) {
				lstFirst.clear();
				lstSecond.clear();
			}
		}
        category = "Khoa phòng BV";
		for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    
	    //Cột 4 pns cố định 
	    
	    sql="SELECT COUNT(DISTINCT( p.id)) as quantity " + 
        		" FROM tbl_hts_case p"
        		+ " RIGHT JOIN tbl_hts_case_risk_group h on h.hts_case_id = p.id "
        		+ " WHERE 1=1 ";        		
        whereClause=" AND p.id IS NOT NULL AND ( h.val = 'answer5' OR p.c10 = 'answer2' OR h.val = 'answer6') AND p.c14!='answer4' ";
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c4_counselling_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}else {
				whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}
		}
        
        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
        if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}else {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
		}
		
        lstFirst = q.list();
        
        sql="SELECT COUNT(DISTINCT( p.id)) as quantity " + 
        		" FROM tbl_hts_case p"
        		+ " RIGHT JOIN tbl_hts_case_risk_group h on h.hts_case_id = p.id "
        		+ " WHERE 1=1 ";        		
        whereClause=" AND p.id IS NOT NULL AND ( h.val = 'answer5' OR p.c10 = 'answer2' OR h.val = 'answer6') AND p.c14='answer2' ";
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c4_counselling_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}else {
				whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}
		}
        
        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
        if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}else {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
		}
		
		lstSecond = q.list();
        
        if(dto.getModality()!=null && dto.getModality().size()==1) {
			if(dto.getModality().get(0)==2) {
				lstFirst.clear();
				lstSecond.clear();
			}
		}
        category = "PNS cố định";
		for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    
	    //Cột 5
        
	    sql="SELECT COUNT(DISTINCT( p.id)) as quantity " + 
        		" FROM tbl_hts_case p"
        		+ " RIGHT JOIN tbl_hts_case_risk_group h on h.hts_case_id = p.id "
        		+ " WHERE 1=1 ";        		
        whereClause=" AND p.c5='answer2' AND p.c14!='answer4' AND p.id IS NOT NULL AND  h.val != 'answer5' AND p.c10 != 'answer2' AND h.val != 'answer6' ";
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c4_counselling_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}else {
				whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}
		}
        
        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
        if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}else {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
		}
		
        lstFirst = q.list();
        
        sql="SELECT COUNT(DISTINCT( p.id)) as quantity " + 
        		" FROM tbl_hts_case p"
        		+ " RIGHT JOIN tbl_hts_case_risk_group h on h.hts_case_id = p.id "
        		+ " WHERE 1=1 ";        		
        whereClause=" AND p.c5='answer2' AND p.c14='answer2' AND p.id IS NOT NULL AND  h.val != 'answer5' AND p.c10 != 'answer2' AND h.val != 'answer6' ";
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c4_counselling_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}else {
				whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}
		}
        
        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
        if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}else {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
		}
		
		lstSecond = q.list();
        
        
        if(dto.getModality()!=null && dto.getModality().size()==1) {
			if(dto.getModality().get(0)==2) {
				lstFirst.clear();
				lstSecond.clear();
			}
		}
        category = "TVXN TN";
		for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    
	    
	    if(dto.getChart3()!=null && dto.getChart3()==1) {
	    	ret.getSeries().clear();
	    	//Tổng số xét nghiệm từ PE02
			sql="SELECT COUNT(id) as quantity FROM tbl_pe_case p WHERE 1=1 ";
			
			whereClause=" AND p.c131='YES' ";
			if(dto.getFromDate()!=null) {
				whereClause+=" AND p.c1 >=:fromDate ";
			}
			if(dto.getToDate()!=null) {
				whereClause+=" AND p.c1 < :toDate ";
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {			
				whereClause+=" AND p.c1_org_id in (:orgIds) ";
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
				}else {
					whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
				}
			}

			q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
			
			if(dto.getFromDate()!=null) {
				q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getToDate()!=null) {
				q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				q.setParameterList("orgIds",dto.getOrgIds());
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					q.setParameter("c12answer1",PEC12.answer1.toString());
					q.setParameter("c12answer2",PEC12.answer2.toString());
					q.setParameter("b1",PEC16.answer1.toString());
					q.setParameter("b3",PEC16.answer3.toString());
				}else {
					q.setParameter("c12answer1",PEC12.answer1.toString());
					q.setParameter("c12answer2",PEC12.answer2.toString());
					q.setParameter("b1",PEC16.answer1.toString());
					q.setParameter("b3",PEC16.answer3.toString());
				}
			}
		
	        lstFirst = q.list();
	        
	        
	        sql = "SELECT COUNT(id) as quantity FROM tbl_pe_case p WHERE 1=1 ";
	        whereClause=" AND p.c131='YES' AND p.c131_result='answer2' ";
	        
	        if(dto.getFromDate()!=null) {
				whereClause+=" AND p.c1 >=:fromDate ";
			}
			if(dto.getToDate()!=null) {
				whereClause+=" AND p.c1 < :toDate ";
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				whereClause+=" AND p.c1_org_id in (:orgIds) ";
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
				}else {
					whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
				}
			}

			
	        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
	        if(dto.getFromDate()!=null) {
				q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getToDate()!=null) {
				q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				q.setParameterList("orgIds",dto.getOrgIds());
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					q.setParameter("c12answer1",PEC12.answer1.toString());
					q.setParameter("c12answer2",PEC12.answer2.toString());
					q.setParameter("b1",PEC16.answer1.toString());
					q.setParameter("b3",PEC16.answer3.toString());
				}else {
					q.setParameter("c12answer1",PEC12.answer1.toString());
					q.setParameter("c12answer2",PEC12.answer2.toString());
					q.setParameter("b1",PEC16.answer1.toString());
					q.setParameter("b3",PEC16.answer3.toString());
				}
			}
			
			
	        lstSecond = q.list();
	        
	        
	        
	      //Tổng số xét nghiệm từ HTS
	        sql="SELECT COUNT(id) as quantity FROM tbl_hts_case p WHERE 1=1 ";        		
	        whereClause=" AND p.c14 IS NOT NULL AND p.c14 <> 'answer4' ";
	        if(dto.getFromDate()!=null) {
				whereClause+=" AND p.c4_counselling_date >=:fromDate ";
			}
			if(dto.getToDate()!=null) {
				whereClause+=" AND p.c4_counselling_date < :toDate ";
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				whereClause+=" AND p.c2_org_id in (:orgIds) ";
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}else {
					whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}
			}
	        
	        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
	        if(dto.getFromDate()!=null) {
				q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getToDate()!=null) {
				q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				q.setParameterList("orgIds",dto.getOrgIds());
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}else {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}
			}
			
	        List<PreventionChartDetailDto> lstHtsFirst = q.list();
	        
	        sql="SELECT COUNT(id) as quantity FROM tbl_hts_case p WHERE 1=1 ";
	        whereClause=" AND p.c14 IS NOT NULL AND p.c14 = 'answer2' ";
	        if(dto.getFromDate()!=null) {
				whereClause+=" AND p.c4_counselling_date >=:fromDate ";
			}
			if(dto.getToDate()!=null) {
				whereClause+=" AND p.c4_counselling_date < :toDate ";
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				whereClause+=" AND p.c2_org_id in (:orgIds) ";
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}else {
					whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}
			}
			
			q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
			if(dto.getFromDate()!=null) {
				q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getToDate()!=null) {
				q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				q.setParameterList("orgIds",dto.getOrgIds());
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}else {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}
			}
			
			List<PreventionChartDetailDto> lstHtsSecond = q.list();
	        
			if(dto.getModality()!=null && dto.getModality().size()==1) {
				if(dto.getModality().get(0)==1) {
					lstFirst.clear();
					lstSecond.clear();
				}else {
					lstHtsFirst.clear();
					lstHtsSecond.clear();
				}
			}
			
			
			for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
				String category1 = "Cộng đồng";
				if(ret.getSeries().get(category1)!=null) {
					PreventionChartSeriesDto seri = ret.getSeries().get(category1);
					seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
				}
				else {
					PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
					seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
					ret.getSeries().put(category1, seri);
				}
			}
		    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
		    	String category1 = "Cộng đồng";
				if(ret.getSeries().get(category1)!=null) {
					PreventionChartSeriesDto seri = ret.getSeries().get(category1);
					seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
				}
				else {
					PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
					seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
					ret.getSeries().put(category1, seri);
				}
			}
		    for (PreventionChartDetailDto preventionChartDetailDto : lstHtsFirst) {
				String category1 = "Cơ sở cố định";
				if(ret.getSeries().get(category1)!=null) {
					PreventionChartSeriesDto seri = ret.getSeries().get(category1);
					seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
				}
				else {
					PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
					seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
					ret.getSeries().put(category1, seri);
				}
			}
		    for (PreventionChartDetailDto preventionChartDetailDto : lstHtsSecond) {
		    	String category1 = "Cơ sở cố định";
				if(ret.getSeries().get(category1)!=null) {
					PreventionChartSeriesDto seri = ret.getSeries().get(category1);
					seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
				}
				else {
					PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
					seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
					ret.getSeries().put(category1, seri);
				}
			}
	    }
        
		return ret;
	}

	public PreventionChartDto getChart4a(PreventionFilterDto dto) {
//		PreventionChartDto ret = new PreventionChartDto();
//		ret.setTitle("Xét nghiệm khẳng định HIV+ theo nhóm khách hàng");
//		//coot 1 Người nghiện chích ma túy (NNCMT)
//		String sql="SELECT COUNT(h.id) as quantity FROM tbl_pe_case_risk_group h "
//				+ " INNER JOIN tbl_pe_case p ON p.id = h.pe_case_id "
//				+ " WHERE 1=1 ";
//		
//		String whereClause=" AND h.val = 'answer1' AND p.c11='YES' ";
//		if(dto.getFromDate()!=null) {
//			whereClause+=" AND p.c1 >=:fromDate ";
//		}
//		if(dto.getToDate()!=null) {
//			whereClause+=" AND p.c1 < :toDate ";
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			whereClause+=" AND p.c1_org_id in (:orgIds) ";
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND  (p.c12 =:c12answer1 or p.c12 is null) AND p.c16 !=:b2 ";
//			}else {
//				whereClause+= " AND  ((p.c12 !=:c12answer1 AND p.c12 is not null) OR p.c16 =:b2) ";
//			}
//		}
//		
//		
//		Query q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
//		
//		if(dto.getFromDate()!=null) {
//			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getToDate()!=null) {
//			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			q.setParameterList("orgIds",dto.getOrgIds());
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}else {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}
//		}
//	
//        List<PreventionChartDetailDto> lstFirst = q.list();
//        
//        
//        whereClause=" AND h.val = 'answer1' AND p.c11='YES' AND p.c13='answer1' ";
//		if(dto.getFromDate()!=null) {
//			whereClause+=" AND p.c1 >=:fromDate ";
//		}
//		if(dto.getToDate()!=null) {
//			whereClause+=" AND p.c1 < :toDate ";
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			whereClause+=" AND p.c1_org_id in (:orgIds) ";
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND  (p.c12 =:c12answer1 or p.c12 is null) AND p.c16 !=:b2 ";
//			}else {
//				whereClause+= " AND  ((p.c12 !=:c12answer1 AND p.c12 is not null) OR p.c16 =:b2) ";
//			}
//		}
//		
//		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
//		
//		if(dto.getFromDate()!=null) {
//			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getToDate()!=null) {
//			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			q.setParameterList("orgIds",dto.getOrgIds());
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}else {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}
//		}
//		
//		List<PreventionChartDetailDto> lstSecond = q.list();
//		
//		String category = "NNCMT";
//		for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
//			if(ret.getSeries().get(category)!=null) {
//				PreventionChartSeriesDto seri = ret.getSeries().get(category);
//				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
//			}
//			else {
//				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
//				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
//				ret.getSeries().put(category, seri);
//			}
//		}
//	    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
//			if(ret.getSeries().get(category)!=null) {
//				PreventionChartSeriesDto seri = ret.getSeries().get(category);
//				seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
//			}
//			else {
//				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
//				seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
//				ret.getSeries().put(category, seri);
//			}
//		}
//	    
//	    //hts
//	    sql = "SELECT COUNT(h.id) as quantity FROM tbl_hts_case_risk_group h "
//				+ " INNER JOIN tbl_hts_case p ON p.id = h.hts_case_id "
//				+ " WHERE 1=1 "; 		
//	    whereClause=" AND h.val = 'answer1' AND p.c14!='answer4' ";
//        if(dto.getFromDate()!=null) {
//			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
//		}
//		if(dto.getToDate()!=null) {
//			whereClause+=" AND p.c4_counselling_date < :toDate ";
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			whereClause+=" AND p.c2_org_id in (:orgIds) ";
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND p.c24 !=:c24answer2 AND (p.c11c !=:c11cYes OR p.c11b =:c11bAnswer1) ";
//			}else {
//				whereClause+= " AND (p.c24 =:c24answer2 OR (p.c11c =:c11cYes AND p.c11b !=:c11bAnswer1)) ";
//			}
//		}
//        
//        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
//        if(dto.getFromDate()!=null) {
//			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getToDate()!=null) {
//			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			q.setParameterList("orgIds",dto.getOrgIds());
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}else {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}
//		}
//		
//		lstFirst = q.list();
//		
//		whereClause=" AND h.val = 'answer1' AND p.c14='answer2' ";
//        if(dto.getFromDate()!=null) {
//			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
//		}
//		if(dto.getToDate()!=null) {
//			whereClause+=" AND p.c4_counselling_date < :toDate ";
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			whereClause+=" AND p.c2_org_id in (:orgIds) ";
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND p.c24 !=:c24answer2 AND (p.c11c !=:c11cYes OR p.c11b =:c11bAnswer1) ";
//			}else {
//				whereClause+= " AND (p.c24 =:c24answer2 OR (p.c11c =:c11cYes AND p.c11b !=:c11bAnswer1)) ";
//			}
//		}
//        
//        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
//        if(dto.getFromDate()!=null) {
//			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getToDate()!=null) {
//			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			q.setParameterList("orgIds",dto.getOrgIds());
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}else {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}
//		}
//		
//		lstSecond = q.list();
//		
//		for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
//			if(ret.getSeries().get(category)!=null) {
//				PreventionChartSeriesDto seri = ret.getSeries().get(category);
//				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
//			}
//			else {
//				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
//				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
//				ret.getSeries().put(category, seri);
//			}
//		}
//	    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
//			if(ret.getSeries().get(category)!=null) {
//				PreventionChartSeriesDto seri = ret.getSeries().get(category);
//				seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
//			}
//			else {
//				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
//				seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
//				ret.getSeries().put(category, seri);
//			}
//		}
//	    
//	    // cột 2 
//	    
//	    sql="SELECT COUNT(h.id) as quantity FROM tbl_pe_case_risk_group h "
//				+ " INNER JOIN tbl_pe_case p ON p.id = h.pe_case_id "
//				+ " WHERE 1=1 ";
//		
//		whereClause=" AND h.val = 'answer2' AND p.c11='YES' ";
//		if(dto.getFromDate()!=null) {
//			whereClause+=" AND p.c1 >=:fromDate ";
//		}
//		if(dto.getToDate()!=null) {
//			whereClause+=" AND p.c1 < :toDate ";
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			whereClause+=" AND p.c1_org_id in (:orgIds) ";
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND  (p.c12 =:c12answer1 or p.c12 is null) AND p.c16 !=:b2 ";
//			}else {
//				whereClause+= " AND  ((p.c12 !=:c12answer1 AND p.c12 is not null) OR p.c16 =:b2) ";
//			}
//		}
//		
//		
//		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
//		
//		if(dto.getFromDate()!=null) {
//			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getToDate()!=null) {
//			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			q.setParameterList("orgIds",dto.getOrgIds());
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}else {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}
//		}
//	
//        lstFirst = q.list();
//        
//        
//        whereClause=" AND h.val = 'answer2' AND p.c11='YES' AND p.c13='answer1' ";
//		if(dto.getFromDate()!=null) {
//			whereClause+=" AND p.c1 >=:fromDate ";
//		}
//		if(dto.getToDate()!=null) {
//			whereClause+=" AND p.c1 < :toDate ";
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			whereClause+=" AND p.c1_org_id in (:orgIds) ";
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND  (p.c12 =:c12answer1 or p.c12 is null) AND p.c16 !=:b2 ";
//			}else {
//				whereClause+= " AND  ((p.c12 !=:c12answer1 AND p.c12 is not null) OR p.c16 =:b2) ";
//			}
//		}
//		
//		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
//		
//		if(dto.getFromDate()!=null) {
//			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getToDate()!=null) {
//			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			q.setParameterList("orgIds",dto.getOrgIds());
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}else {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}
//		}
//		
//		lstSecond = q.list();
//		
//		category = "MSM";
//		for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
//			if(ret.getSeries().get(category)!=null) {
//				PreventionChartSeriesDto seri = ret.getSeries().get(category);
//				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
//			}
//			else {
//				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
//				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
//				ret.getSeries().put(category, seri);
//			}
//		}
//	    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
//			if(ret.getSeries().get(category)!=null) {
//				PreventionChartSeriesDto seri = ret.getSeries().get(category);
//				seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
//			}
//			else {
//				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
//				seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
//				ret.getSeries().put(category, seri);
//			}
//		}
//	    
//	    //hts
//	    sql = "SELECT COUNT(h.id) as quantity FROM tbl_hts_case_risk_group h "
//				+ " INNER JOIN tbl_hts_case p ON p.id = h.hts_case_id "
//				+ " WHERE 1=1 "; 		
//	    whereClause=" AND h.val = 'answer2' AND p.c14!='answer4' ";
//        if(dto.getFromDate()!=null) {
//			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
//		}
//		if(dto.getToDate()!=null) {
//			whereClause+=" AND p.c4_counselling_date < :toDate ";
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			whereClause+=" AND p.c2_org_id in (:orgIds) ";
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND p.c24 !=:c24answer2 AND (p.c11c !=:c11cYes OR p.c11b =:c11bAnswer1) ";
//			}else {
//				whereClause+= " AND (p.c24 =:c24answer2 OR (p.c11c =:c11cYes AND p.c11b !=:c11bAnswer1)) ";
//			}
//		}
//        
//        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
//        if(dto.getFromDate()!=null) {
//			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getToDate()!=null) {
//			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			q.setParameterList("orgIds",dto.getOrgIds());
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}else {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}
//		}
//		
//		lstFirst = q.list();
//		
//		whereClause=" AND h.val = 'answer2' AND p.c14='answer2' ";
//        if(dto.getFromDate()!=null) {
//			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
//		}
//		if(dto.getToDate()!=null) {
//			whereClause+=" AND p.c4_counselling_date < :toDate ";
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			whereClause+=" AND p.c2_org_id in (:orgIds) ";
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND p.c24 !=:c24answer2 AND (p.c11c !=:c11cYes OR p.c11b =:c11bAnswer1) ";
//			}else {
//				whereClause+= " AND (p.c24 =:c24answer2 OR (p.c11c =:c11cYes AND p.c11b !=:c11bAnswer1)) ";
//			}
//		}
//        
//        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
//        if(dto.getFromDate()!=null) {
//			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getToDate()!=null) {
//			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			q.setParameterList("orgIds",dto.getOrgIds());
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}else {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}
//		}
//		
//		lstSecond = q.list();
//		
//		for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
//			if(ret.getSeries().get(category)!=null) {
//				PreventionChartSeriesDto seri = ret.getSeries().get(category);
//				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
//			}
//			else {
//				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
//				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
//				ret.getSeries().put(category, seri);
//			}
//		}
//	    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
//			if(ret.getSeries().get(category)!=null) {
//				PreventionChartSeriesDto seri = ret.getSeries().get(category);
//				seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
//			}
//			else {
//				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
//				seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
//				ret.getSeries().put(category, seri);
//			}
//		}
//	    
//	    // cột 3 
//	    
//	    sql="SELECT COUNT(h.id) as quantity FROM tbl_pe_case_risk_group h "
//				+ " INNER JOIN tbl_pe_case p ON p.id = h.pe_case_id "
//				+ " WHERE 1=1 ";
//		
//		whereClause=" AND h.val = 'answer3' AND p.c11='YES' ";
//		if(dto.getFromDate()!=null) {
//			whereClause+=" AND p.c1 >=:fromDate ";
//		}
//		if(dto.getToDate()!=null) {
//			whereClause+=" AND p.c1 < :toDate ";
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			whereClause+=" AND p.c1_org_id in (:orgIds) ";
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND  (p.c12 =:c12answer1 or p.c12 is null) AND p.c16 !=:b2 ";
//			}else {
//				whereClause+= " AND  ((p.c12 !=:c12answer1 AND p.c12 is not null) OR p.c16 =:b2) ";
//			}
//		}
//		
//		
//		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
//		
//		if(dto.getFromDate()!=null) {
//			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getToDate()!=null) {
//			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			q.setParameterList("orgIds",dto.getOrgIds());
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}else {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}
//		}
//	
//        lstFirst = q.list();
//        
//        
//        whereClause=" AND h.val = 'answer3' AND p.c11='YES' AND p.c13='answer1' ";
//		if(dto.getFromDate()!=null) {
//			whereClause+=" AND p.c1 >=:fromDate ";
//		}
//		if(dto.getToDate()!=null) {
//			whereClause+=" AND p.c1 < :toDate ";
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			whereClause+=" AND p.c1_org_id in (:orgIds) ";
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND  (p.c12 =:c12answer1 or p.c12 is null) AND p.c16 !=:b2 ";
//			}else {
//				whereClause+= " AND  ((p.c12 !=:c12answer1 AND p.c12 is not null) OR p.c16 =:b2) ";
//			}
//		}
//		
//		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
//		
//		if(dto.getFromDate()!=null) {
//			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getToDate()!=null) {
//			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			q.setParameterList("orgIds",dto.getOrgIds());
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}else {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}
//		}
//		
//		lstSecond = q.list();
//		
//		category = "Bán dâm";
//		for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
//			if(ret.getSeries().get(category)!=null) {
//				PreventionChartSeriesDto seri = ret.getSeries().get(category);
//				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
//			}
//			else {
//				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
//				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
//				ret.getSeries().put(category, seri);
//			}
//		}
//	    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
//			if(ret.getSeries().get(category)!=null) {
//				PreventionChartSeriesDto seri = ret.getSeries().get(category);
//				seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
//			}
//			else {
//				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
//				seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
//				ret.getSeries().put(category, seri);
//			}
//		}
//	    
//	    //hts
//	    sql = "SELECT COUNT(h.id) as quantity FROM tbl_hts_case_risk_group h "
//				+ " INNER JOIN tbl_hts_case p ON p.id = h.hts_case_id "
//				+ " WHERE 1=1 "; 		
//	    whereClause=" AND h.val = 'answer3' AND p.c14!='answer4' ";
//        if(dto.getFromDate()!=null) {
//			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
//		}
//		if(dto.getToDate()!=null) {
//			whereClause+=" AND p.c4_counselling_date < :toDate ";
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			whereClause+=" AND p.c2_org_id in (:orgIds) ";
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND p.c24 !=:c24answer2 AND (p.c11c !=:c11cYes OR p.c11b =:c11bAnswer1) ";
//			}else {
//				whereClause+= " AND (p.c24 =:c24answer2 OR (p.c11c =:c11cYes AND p.c11b !=:c11bAnswer1)) ";
//			}
//		}
//        
//        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
//        if(dto.getFromDate()!=null) {
//			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getToDate()!=null) {
//			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			q.setParameterList("orgIds",dto.getOrgIds());
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}else {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}
//		}
//		
//		lstFirst = q.list();
//		
//		whereClause=" AND h.val = 'answer3' AND p.c14='answer2' ";
//        if(dto.getFromDate()!=null) {
//			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
//		}
//		if(dto.getToDate()!=null) {
//			whereClause+=" AND p.c4_counselling_date < :toDate ";
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			whereClause+=" AND p.c2_org_id in (:orgIds) ";
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND p.c24 !=:c24answer2 AND (p.c11c !=:c11cYes OR p.c11b =:c11bAnswer1) ";
//			}else {
//				whereClause+= " AND (p.c24 =:c24answer2 OR (p.c11c =:c11cYes AND p.c11b !=:c11bAnswer1)) ";
//			}
//		}
//        
//        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
//        if(dto.getFromDate()!=null) {
//			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getToDate()!=null) {
//			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			q.setParameterList("orgIds",dto.getOrgIds());
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}else {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}
//		}
//		
//		lstSecond = q.list();
//		
//		for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
//			if(ret.getSeries().get(category)!=null) {
//				PreventionChartSeriesDto seri = ret.getSeries().get(category);
//				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
//			}
//			else {
//				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
//				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
//				ret.getSeries().put(category, seri);
//			}
//		}
//	    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
//			if(ret.getSeries().get(category)!=null) {
//				PreventionChartSeriesDto seri = ret.getSeries().get(category);
//				seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
//			}
//			else {
//				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
//				seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
//				ret.getSeries().put(category, seri);
//			}
//		}
//	    
//	    // cột 4 
//	    
//	    sql="SELECT COUNT(h.id) as quantity FROM tbl_pe_case_risk_group h "
//				+ " INNER JOIN tbl_pe_case p ON p.id = h.pe_case_id "
//				+ " WHERE 1=1 ";
//		
//		whereClause=" AND h.val = 'answer4' AND p.c11='YES' ";
//		if(dto.getFromDate()!=null) {
//			whereClause+=" AND p.c1 >=:fromDate ";
//		}
//		if(dto.getToDate()!=null) {
//			whereClause+=" AND p.c1 < :toDate ";
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			whereClause+=" AND p.c1_org_id in (:orgIds) ";
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND  (p.c12 =:c12answer1 or p.c12 is null) AND p.c16 !=:b2 ";
//			}else {
//				whereClause+= " AND  ((p.c12 !=:c12answer1 AND p.c12 is not null) OR p.c16 =:b2) ";
//			}
//		}
//		
//		
//		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
//		
//		if(dto.getFromDate()!=null) {
//			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getToDate()!=null) {
//			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			q.setParameterList("orgIds",dto.getOrgIds());
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}else {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}
//		}
//	
//        lstFirst = q.list();
//        
//        
//        whereClause=" AND h.val = 'answer4' AND p.c11='YES' AND p.c13='answer1' ";
//		if(dto.getFromDate()!=null) {
//			whereClause+=" AND p.c1 >=:fromDate ";
//		}
//		if(dto.getToDate()!=null) {
//			whereClause+=" AND p.c1 < :toDate ";
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			whereClause+=" AND p.c1_org_id in (:orgIds) ";
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND  (p.c12 =:c12answer1 or p.c12 is null) AND p.c16 !=:b2 ";
//			}else {
//				whereClause+= " AND  ((p.c12 !=:c12answer1 AND p.c12 is not null) OR p.c16 =:b2) ";
//			}
//		}
//		
//		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
//		
//		if(dto.getFromDate()!=null) {
//			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getToDate()!=null) {
//			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			q.setParameterList("orgIds",dto.getOrgIds());
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}else {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}
//		}
//		
//		lstSecond = q.list();
//		
//		category = "TG";
//		for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
//			if(ret.getSeries().get(category)!=null) {
//				PreventionChartSeriesDto seri = ret.getSeries().get(category);
//				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
//			}
//			else {
//				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
//				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
//				ret.getSeries().put(category, seri);
//			}
//		}
//	    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
//			if(ret.getSeries().get(category)!=null) {
//				PreventionChartSeriesDto seri = ret.getSeries().get(category);
//				seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
//			}
//			else {
//				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
//				seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
//				ret.getSeries().put(category, seri);
//			}
//		}
//	    
//	    //hts
//	    sql = "SELECT COUNT(h.id) as quantity FROM tbl_hts_case_risk_group h "
//				+ " INNER JOIN tbl_hts_case p ON p.id = h.hts_case_id "
//				+ " WHERE 1=1 "; 		
//	    whereClause=" AND h.val = 'answer4' AND p.c14!='answer4' ";
//        if(dto.getFromDate()!=null) {
//			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
//		}
//		if(dto.getToDate()!=null) {
//			whereClause+=" AND p.c4_counselling_date < :toDate ";
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			whereClause+=" AND p.c2_org_id in (:orgIds) ";
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND p.c24 !=:c24answer2 AND (p.c11c !=:c11cYes OR p.c11b =:c11bAnswer1) ";
//			}else {
//				whereClause+= " AND (p.c24 =:c24answer2 OR (p.c11c =:c11cYes AND p.c11b !=:c11bAnswer1)) ";
//			}
//		}
//        
//        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
//        if(dto.getFromDate()!=null) {
//			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getToDate()!=null) {
//			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			q.setParameterList("orgIds",dto.getOrgIds());
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}else {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}
//		}
//		
//		lstFirst = q.list();
//		
//		whereClause=" AND h.val = 'answer4' AND p.c14='answer2' ";
//        if(dto.getFromDate()!=null) {
//			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
//		}
//		if(dto.getToDate()!=null) {
//			whereClause+=" AND p.c4_counselling_date < :toDate ";
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			whereClause+=" AND p.c2_org_id in (:orgIds) ";
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND p.c24 !=:c24answer2 AND (p.c11c !=:c11cYes OR p.c11b =:c11bAnswer1) ";
//			}else {
//				whereClause+= " AND (p.c24 =:c24answer2 OR (p.c11c =:c11cYes AND p.c11b !=:c11bAnswer1)) ";
//			}
//		}
//        
//        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
//        if(dto.getFromDate()!=null) {
//			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getToDate()!=null) {
//			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			q.setParameterList("orgIds",dto.getOrgIds());
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}else {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}
//		}
//		
//		lstSecond = q.list();
//		
//		for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
//			if(ret.getSeries().get(category)!=null) {
//				PreventionChartSeriesDto seri = ret.getSeries().get(category);
//				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
//			}
//			else {
//				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
//				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
//				ret.getSeries().put(category, seri);
//			}
//		}
//	    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
//			if(ret.getSeries().get(category)!=null) {
//				PreventionChartSeriesDto seri = ret.getSeries().get(category);
//				seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
//			}
//			else {
//				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
//				seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
//				ret.getSeries().put(category, seri);
//			}
//		}
//	    
//	    // cột 5 
//	    
//	    sql="SELECT COUNT(h.id) as quantity FROM tbl_pe_case_risk_group h "
//				+ " INNER JOIN tbl_pe_case p ON p.id = h.pe_case_id "
//				+ " WHERE 1=1 ";
//		
//		whereClause=" AND h.val = 'answer5' AND p.c11='YES' ";
//		if(dto.getFromDate()!=null) {
//			whereClause+=" AND p.c1 >=:fromDate ";
//		}
//		if(dto.getToDate()!=null) {
//			whereClause+=" AND p.c1 < :toDate ";
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			whereClause+=" AND p.c1_org_id in (:orgIds) ";
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND  (p.c12 =:c12answer1 or p.c12 is null) AND p.c16 !=:b2 ";
//			}else {
//				whereClause+= " AND  ((p.c12 !=:c12answer1 AND p.c12 is not null) OR p.c16 =:b2) ";
//			}
//		}
//		
//		
//		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
//		
//		if(dto.getFromDate()!=null) {
//			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getToDate()!=null) {
//			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			q.setParameterList("orgIds",dto.getOrgIds());
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}else {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}
//		}
//	
//        lstFirst = q.list();
//        
//        
//        whereClause=" AND h.val = 'answer5' AND p.c11='YES' AND p.c13='answer1' ";
//		if(dto.getFromDate()!=null) {
//			whereClause+=" AND p.c1 >=:fromDate ";
//		}
//		if(dto.getToDate()!=null) {
//			whereClause+=" AND p.c1 < :toDate ";
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			whereClause+=" AND p.c1_org_id in (:orgIds) ";
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND  (p.c12 =:c12answer1 or p.c12 is null) AND p.c16 !=:b2 ";
//			}else {
//				whereClause+= " AND  ((p.c12 !=:c12answer1 AND p.c12 is not null) OR p.c16 =:b2) ";
//			}
//		}
//		
//		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
//		
//		if(dto.getFromDate()!=null) {
//			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getToDate()!=null) {
//			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			q.setParameterList("orgIds",dto.getOrgIds());
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}else {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}
//		}
//		
//		lstSecond = q.list();
//		
//		category = "Vợ/Chồng/BT của NCH ";
//		for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
//			if(ret.getSeries().get(category)!=null) {
//				PreventionChartSeriesDto seri = ret.getSeries().get(category);
//				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
//			}
//			else {
//				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
//				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
//				ret.getSeries().put(category, seri);
//			}
//		}
//	    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
//			if(ret.getSeries().get(category)!=null) {
//				PreventionChartSeriesDto seri = ret.getSeries().get(category);
//				seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
//			}
//			else {
//				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
//				seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
//				ret.getSeries().put(category, seri);
//			}
//		}
//	    
//	    //hts
//	    sql = "SELECT COUNT(h.id) as quantity FROM tbl_hts_case_risk_group h "
//				+ " INNER JOIN tbl_hts_case p ON p.id = h.hts_case_id "
//				+ " WHERE 1=1 "; 		
//	    whereClause=" AND h.val = 'answer5' AND p.c14!='answer4' ";
//        if(dto.getFromDate()!=null) {
//			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
//		}
//		if(dto.getToDate()!=null) {
//			whereClause+=" AND p.c4_counselling_date < :toDate ";
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			whereClause+=" AND p.c2_org_id in (:orgIds) ";
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND p.c24 !=:c24answer2 AND (p.c11c !=:c11cYes OR p.c11b =:c11bAnswer1) ";
//			}else {
//				whereClause+= " AND (p.c24 =:c24answer2 OR (p.c11c =:c11cYes AND p.c11b !=:c11bAnswer1)) ";
//			}
//		}
//        
//        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
//        if(dto.getFromDate()!=null) {
//			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getToDate()!=null) {
//			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			q.setParameterList("orgIds",dto.getOrgIds());
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}else {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}
//		}
//		
//		lstFirst = q.list();
//		
//		whereClause=" AND h.val = 'answer5' AND p.c14='answer2' ";
//        if(dto.getFromDate()!=null) {
//			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
//		}
//		if(dto.getToDate()!=null) {
//			whereClause+=" AND p.c4_counselling_date < :toDate ";
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			whereClause+=" AND p.c2_org_id in (:orgIds) ";
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND p.c24 !=:c24answer2 AND (p.c11c !=:c11cYes OR p.c11b =:c11bAnswer1) ";
//			}else {
//				whereClause+= " AND (p.c24 =:c24answer2 OR (p.c11c =:c11cYes AND p.c11b !=:c11bAnswer1)) ";
//			}
//		}
//        
//        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
//        if(dto.getFromDate()!=null) {
//			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getToDate()!=null) {
//			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			q.setParameterList("orgIds",dto.getOrgIds());
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}else {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}
//		}
//		
//		lstSecond = q.list();
//		
//		for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
//			if(ret.getSeries().get(category)!=null) {
//				PreventionChartSeriesDto seri = ret.getSeries().get(category);
//				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
//			}
//			else {
//				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
//				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
//				ret.getSeries().put(category, seri);
//			}
//		}
//	    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
//			if(ret.getSeries().get(category)!=null) {
//				PreventionChartSeriesDto seri = ret.getSeries().get(category);
//				seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
//			}
//			else {
//				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
//				seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
//				ret.getSeries().put(category, seri);
//			}
//		}
//	    
//	    // cột 6 
//	    
//	    sql="SELECT COUNT(h.id) as quantity FROM tbl_pe_case_risk_group h "
//				+ " INNER JOIN tbl_pe_case p ON p.id = h.pe_case_id "
//				+ " WHERE 1=1 ";
//		
//		whereClause=" AND h.val = 'answer6' AND p.c11='YES' ";
//		if(dto.getFromDate()!=null) {
//			whereClause+=" AND p.c1 >=:fromDate ";
//		}
//		if(dto.getToDate()!=null) {
//			whereClause+=" AND p.c1 < :toDate ";
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			whereClause+=" AND p.c1_org_id in (:orgIds) ";
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND  (p.c12 =:c12answer1 or p.c12 is null) AND p.c16 !=:b2 ";
//			}else {
//				whereClause+= " AND  ((p.c12 !=:c12answer1 AND p.c12 is not null) OR p.c16 =:b2) ";
//			}
//		}
//		
//		
//		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
//		
//		if(dto.getFromDate()!=null) {
//			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getToDate()!=null) {
//			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			q.setParameterList("orgIds",dto.getOrgIds());
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}else {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}
//		}
//	
//        lstFirst = q.list();
//        
//        
//        whereClause=" AND h.val = 'answer6' AND p.c11='YES' AND p.c13='answer1' ";
//		if(dto.getFromDate()!=null) {
//			whereClause+=" AND p.c1 >=:fromDate ";
//		}
//		if(dto.getToDate()!=null) {
//			whereClause+=" AND p.c1 < :toDate ";
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			whereClause+=" AND p.c1_org_id in (:orgIds) ";
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND  (p.c12 =:c12answer1 or p.c12 is null) AND p.c16 !=:b2 ";
//			}else {
//				whereClause+= " AND  ((p.c12 !=:c12answer1 AND p.c12 is not null) OR p.c16 =:b2) ";
//			}
//		}
//		
//		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
//		
//		if(dto.getFromDate()!=null) {
//			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getToDate()!=null) {
//			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			q.setParameterList("orgIds",dto.getOrgIds());
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}else {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}
//		}
//		
//		lstSecond = q.list();
//		
//		category = "BT/BC của NCH";
//		for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
//			if(ret.getSeries().get(category)!=null) {
//				PreventionChartSeriesDto seri = ret.getSeries().get(category);
//				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
//			}
//			else {
//				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
//				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
//				ret.getSeries().put(category, seri);
//			}
//		}
//	    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
//			if(ret.getSeries().get(category)!=null) {
//				PreventionChartSeriesDto seri = ret.getSeries().get(category);
//				seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
//			}
//			else {
//				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
//				seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
//				ret.getSeries().put(category, seri);
//			}
//		}
//	    
//	    //hts
//	    sql = "SELECT COUNT(h.id) as quantity FROM tbl_hts_case_risk_group h "
//				+ " INNER JOIN tbl_hts_case p ON p.id = h.hts_case_id "
//				+ " WHERE 1=1 "; 		
//	    whereClause=" AND h.val = 'answer6' AND p.c14!='answer4' ";
//        if(dto.getFromDate()!=null) {
//			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
//		}
//		if(dto.getToDate()!=null) {
//			whereClause+=" AND p.c4_counselling_date < :toDate ";
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			whereClause+=" AND p.c2_org_id in (:orgIds) ";
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND p.c24 !=:c24answer2 AND (p.c11c !=:c11cYes OR p.c11b =:c11bAnswer1) ";
//			}else {
//				whereClause+= " AND (p.c24 =:c24answer2 OR (p.c11c =:c11cYes AND p.c11b !=:c11bAnswer1)) ";
//			}
//		}
//        
//        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
//        if(dto.getFromDate()!=null) {
//			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getToDate()!=null) {
//			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			q.setParameterList("orgIds",dto.getOrgIds());
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}else {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}
//		}
//		
//		lstFirst = q.list();
//		
//		whereClause=" AND h.val = 'answer6' AND p.c14='answer2' ";
//        if(dto.getFromDate()!=null) {
//			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
//		}
//		if(dto.getToDate()!=null) {
//			whereClause+=" AND p.c4_counselling_date < :toDate ";
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			whereClause+=" AND p.c2_org_id in (:orgIds) ";
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND p.c24 !=:c24answer2 AND (p.c11c !=:c11cYes OR p.c11b =:c11bAnswer1) ";
//			}else {
//				whereClause+= " AND (p.c24 =:c24answer2 OR (p.c11c =:c11cYes AND p.c11b !=:c11bAnswer1)) ";
//			}
//		}
//        
//        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
//        if(dto.getFromDate()!=null) {
//			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getToDate()!=null) {
//			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			q.setParameterList("orgIds",dto.getOrgIds());
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}else {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}
//		}
//		
//		lstSecond = q.list();
//		
//		for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
//			if(ret.getSeries().get(category)!=null) {
//				PreventionChartSeriesDto seri = ret.getSeries().get(category);
//				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
//			}
//			else {
//				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
//				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
//				ret.getSeries().put(category, seri);
//			}
//		}
//	    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
//			if(ret.getSeries().get(category)!=null) {
//				PreventionChartSeriesDto seri = ret.getSeries().get(category);
//				seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
//			}
//			else {
//				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
//				seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
//				ret.getSeries().put(category, seri);
//			}
//		}
//	    
//	    //cột 7
//	    //hts
//	    sql = "SELECT COUNT(h.id) as quantity FROM tbl_hts_case_risk_group h "
//				+ " INNER JOIN tbl_hts_case p ON p.id = h.hts_case_id "
//				+ " WHERE 1=1 "; 		
//	    whereClause=" AND h.val = 'answer14' AND p.c14!='answer4' ";
//        if(dto.getFromDate()!=null) {
//			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
//		}
//		if(dto.getToDate()!=null) {
//			whereClause+=" AND p.c4_counselling_date < :toDate ";
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			whereClause+=" AND p.c2_org_id in (:orgIds) ";
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND p.c24 !=:c24answer2 AND (p.c11c !=:c11cYes OR p.c11b =:c11bAnswer1) ";
//			}else {
//				whereClause+= " AND (p.c24 =:c24answer2 OR (p.c11c =:c11cYes AND p.c11b !=:c11bAnswer1)) ";
//			}
//		}
//        
//        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
//        if(dto.getFromDate()!=null) {
//			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getToDate()!=null) {
//			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			q.setParameterList("orgIds",dto.getOrgIds());
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}else {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}
//		}
//		
//		lstFirst = q.list();
//		
//		whereClause=" AND h.val = 'answer14' AND p.c14='answer2' ";
//        if(dto.getFromDate()!=null) {
//			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
//		}
//		if(dto.getToDate()!=null) {
//			whereClause+=" AND p.c4_counselling_date < :toDate ";
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			whereClause+=" AND p.c2_org_id in (:orgIds) ";
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND p.c24 !=:c24answer2 AND (p.c11c !=:c11cYes OR p.c11b =:c11bAnswer1) ";
//			}else {
//				whereClause+= " AND (p.c24 =:c24answer2 OR (p.c11c =:c11cYes AND p.c11b !=:c11bAnswer1)) ";
//			}
//		}
//        
//        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
//        if(dto.getFromDate()!=null) {
//			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getToDate()!=null) {
//			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			q.setParameterList("orgIds",dto.getOrgIds());
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}else {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}
//		}
//		
//		lstSecond = q.list();
//		category = "Phạm nhân";
//		for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
//			if(ret.getSeries().get(category)!=null) {
//				PreventionChartSeriesDto seri = ret.getSeries().get(category);
//				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
//			}
//			else {
//				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
//				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
//				ret.getSeries().put(category, seri);
//			}
//		}
//	    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
//			if(ret.getSeries().get(category)!=null) {
//				PreventionChartSeriesDto seri = ret.getSeries().get(category);
//				seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
//			}
//			else {
//				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
//				seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
//				ret.getSeries().put(category, seri);
//			}
//		}
//	    
//	    //cột 8
//	    //hts
//	    sql = "SELECT COUNT(h.id) as quantity FROM tbl_hts_case_risk_group h "
//				+ " INNER JOIN tbl_hts_case p ON p.id = h.hts_case_id "
//				+ " WHERE 1=1 "; 		
//	    whereClause=" AND h.val != 'answer14' AND p.c14!='answer4' AND h.val != 'answer1' AND h.val != 'answer2' AND h.val != 'answer3'"
//	    		+ " AND h.val != 'answer4' AND h.val != 'answer5' AND h.val != 'answer6'";
//        if(dto.getFromDate()!=null) {
//			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
//		}
//		if(dto.getToDate()!=null) {
//			whereClause+=" AND p.c4_counselling_date < :toDate ";
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			whereClause+=" AND p.c2_org_id in (:orgIds) ";
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND p.c24 !=:c24answer2 AND (p.c11c !=:c11cYes OR p.c11b =:c11bAnswer1) ";
//			}else {
//				whereClause+= " AND (p.c24 =:c24answer2 OR (p.c11c =:c11cYes AND p.c11b !=:c11bAnswer1)) ";
//			}
//		}
//        
//        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
//        if(dto.getFromDate()!=null) {
//			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getToDate()!=null) {
//			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			q.setParameterList("orgIds",dto.getOrgIds());
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}else {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}
//		}
//		
//		lstFirst = q.list();
//		
//		whereClause=" AND h.val != 'answer14' AND p.c14='answer2' AND h.val != 'answer1' AND h.val != 'answer2' AND h.val != 'answer3'"
//	    		+ " AND h.val != 'answer4' AND h.val != 'answer5' AND h.val != 'answer6'";
//        if(dto.getFromDate()!=null) {
//			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
//		}
//		if(dto.getToDate()!=null) {
//			whereClause+=" AND p.c4_counselling_date < :toDate ";
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			whereClause+=" AND p.c2_org_id in (:orgIds) ";
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND p.c24 !=:c24answer2 AND (p.c11c !=:c11cYes OR p.c11b =:c11bAnswer1) ";
//			}else {
//				whereClause+= " AND (p.c24 =:c24answer2 OR (p.c11c =:c11cYes AND p.c11b !=:c11bAnswer1)) ";
//			}
//		}
//        
//        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
//        if(dto.getFromDate()!=null) {
//			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getToDate()!=null) {
//			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
//		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			q.setParameterList("orgIds",dto.getOrgIds());
//		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}else {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}
//		}
//		
//		lstSecond = q.list();
//		category = "Khác";
//		for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
//			if(ret.getSeries().get(category)!=null) {
//				PreventionChartSeriesDto seri = ret.getSeries().get(category);
//				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
//			}
//			else {
//				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
//				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
//				ret.getSeries().put(category, seri);
//			}
//		}
//	    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
//			if(ret.getSeries().get(category)!=null) {
//				PreventionChartSeriesDto seri = ret.getSeries().get(category);
//				seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
//			}
//			else {
//				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
//				seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
//				ret.getSeries().put(category, seri);
//			}
//		}
//		
		return null;
	}
	
	@Override
	public PreventionChartDto getChart4(PreventionFilterDto dto) {
		PreventionChartDto ret = new PreventionChartDto();
		ret.setTitle("Xét nghiệm khẳng định HIV+ theo nhóm khách hàng");
		//coot 1 Người nghiện chích ma túy (NNCMT)
		//pe
		String sql="select count(id) as quantity from (select id,val,c11,c13,c1,c1_org_id,c12,c16,c131,c131_result from ( select  "
				+ "h.id as id,h.c11,h.c1,h.c1_org_id,h.c12,h.c16,h.c13,h.c131,r.val,h.c131_result,r.is_main_risk  from tbl_pe_case_risk_group r "
				+ "inner join tbl_pe_case h on h.id = r.pe_case_id ) as tbl where tbl.is_main_risk=1) as p where 1=1  ";
//		String sql="select count(id) as quantity from (select id,val,c11,c13,c1,c1_org_id,c12,c16,c131,c131_result from ( select (ROW_NUMBER() OVER (PARTITION BY h.id order by h.id,CONVERT(SUBSTRING(r.val,7,9),UNSIGNED INTEGER) )) "
//				+ "AS RowNumber,h.id as id,h.c11,h.c1,h.c1_org_id,h.c12,h.c16,h.c13,h.c131,r.val,h.c131_result from tbl_pe_case_risk_group r "
//				+ "inner join tbl_pe_case h on h.id = r.pe_case_id ) as tbl where tbl.RowNumber=1) as p where 1=1  ";
		String whereClause=" AND p.val = 'answer1' AND p.c131='YES' ";
		String category="";
		Query q =null;
		List<PreventionChartDetailDto> lstFirst=null;
		List<PreventionChartDetailDto> lstSecond=null;
		boolean modality1 = true;
		boolean modality2 = true;
		if(dto.getModality()!=null && dto.getModality().size()==1) {
			if(dto.getModality().get(0)==1) {
				modality2=false;
			}
			if(dto.getModality().get(0)==2) {
				modality1=false;
			}
		}
		if(modality2==true) {
			if(dto.getFromDate()!=null) {
				whereClause+=" AND p.c1 >=:fromDate ";
			}
			if(dto.getToDate()!=null) {
				whereClause+=" AND p.c1 < :toDate ";
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				whereClause+=" AND p.c1_org_id in (:orgIds) ";
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
				}else {
					whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
				}
			}

			q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
			
			if(dto.getFromDate()!=null) {
				q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getToDate()!=null) {
				q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				q.setParameterList("orgIds",dto.getOrgIds());
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					q.setParameter("c12answer1",PEC12.answer1.toString());
					q.setParameter("c12answer2",PEC12.answer2.toString());
					q.setParameter("b1",PEC16.answer1.toString());
					q.setParameter("b3",PEC16.answer3.toString());
				}else {
					q.setParameter("c12answer1",PEC12.answer1.toString());
					q.setParameter("c12answer2",PEC12.answer2.toString());
					q.setParameter("b1",PEC16.answer1.toString());
					q.setParameter("b3",PEC16.answer3.toString());
				}
			}
		
	        lstFirst = q.list();
	        
	        
	        whereClause=" AND p.val = 'answer1' AND p.c131='YES' AND p.c131_result='answer2' ";
			if(dto.getFromDate()!=null) {
				whereClause+=" AND p.c1 >=:fromDate ";
			}
			if(dto.getToDate()!=null) {
				whereClause+=" AND p.c1 < :toDate ";
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				whereClause+=" AND p.c1_org_id in (:orgIds) ";
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
				}else {
					whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
				}
			}

			
			q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
			
			if(dto.getFromDate()!=null) {
				q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getToDate()!=null) {
				q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				q.setParameterList("orgIds",dto.getOrgIds());
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					q.setParameter("c12answer1",PEC12.answer1.toString());
					q.setParameter("c12answer2",PEC12.answer2.toString());
					q.setParameter("b1",PEC16.answer1.toString());
					q.setParameter("b3",PEC16.answer3.toString());
				}else {
					q.setParameter("c12answer1",PEC12.answer1.toString());
					q.setParameter("c12answer2",PEC12.answer2.toString());
					q.setParameter("b1",PEC16.answer1.toString());
					q.setParameter("b3",PEC16.answer3.toString());
				}
			}
			
			lstSecond = q.list();
			
			category = "NNCMT";
			for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
				if(ret.getSeries().get(category)!=null) {
					PreventionChartSeriesDto seri = ret.getSeries().get(category);
					seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
				}
				else {
					PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
					seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
					ret.getSeries().put(category, seri);
				}
			}
		    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
				if(ret.getSeries().get(category)!=null) {
					PreventionChartSeriesDto seri = ret.getSeries().get(category);
					seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
				}
				else {
					PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
					seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
					ret.getSeries().put(category, seri);
				}
			}
		    
		    whereClause=" AND p.val = 'answer2' AND p.c131='YES'  ";
			if(dto.getFromDate()!=null) {
				whereClause+=" AND p.c1 >=:fromDate ";
			}
			if(dto.getToDate()!=null) {
				whereClause+=" AND p.c1 < :toDate ";
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				whereClause+=" AND p.c1_org_id in (:orgIds) ";
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
				}else {
					whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
				}
			}

			q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
			
			if(dto.getFromDate()!=null) {
				q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getToDate()!=null) {
				q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				q.setParameterList("orgIds",dto.getOrgIds());
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					q.setParameter("c12answer1",PEC12.answer1.toString());
					q.setParameter("c12answer2",PEC12.answer2.toString());
					q.setParameter("b1",PEC16.answer1.toString());
					q.setParameter("b3",PEC16.answer3.toString());
				}else {
					q.setParameter("c12answer1",PEC12.answer1.toString());
					q.setParameter("c12answer2",PEC12.answer2.toString());
					q.setParameter("b1",PEC16.answer1.toString());
					q.setParameter("b3",PEC16.answer3.toString());
				}
			}
		
	        lstFirst = q.list();
	        
	        
	        whereClause=" AND p.val = 'answer2' AND p.c131='YES' AND p.c131_result='answer2' ";
			if(dto.getFromDate()!=null) {
				whereClause+=" AND p.c1 >=:fromDate ";
			}
			if(dto.getToDate()!=null) {
				whereClause+=" AND p.c1 < :toDate ";
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				whereClause+=" AND p.c1_org_id in (:orgIds) ";
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
				}else {
					whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
				}
			}

			q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
			
			if(dto.getFromDate()!=null) {
				q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getToDate()!=null) {
				q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				q.setParameterList("orgIds",dto.getOrgIds());
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					q.setParameter("c12answer1",PEC12.answer1.toString());
					q.setParameter("c12answer2",PEC12.answer2.toString());
					q.setParameter("b1",PEC16.answer1.toString());
					q.setParameter("b3",PEC16.answer3.toString());
				}else {
					q.setParameter("c12answer1",PEC12.answer1.toString());
					q.setParameter("c12answer2",PEC12.answer2.toString());
					q.setParameter("b1",PEC16.answer1.toString());
					q.setParameter("b3",PEC16.answer3.toString());
				}
			}
			
			lstSecond = q.list();
			
			category = "MSM";
			for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
				if(ret.getSeries().get(category)!=null) {
					PreventionChartSeriesDto seri = ret.getSeries().get(category);
					seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
				}
				else {
					PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
					seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
					ret.getSeries().put(category, seri);
				}
			}
		    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
				if(ret.getSeries().get(category)!=null) {
					PreventionChartSeriesDto seri = ret.getSeries().get(category);
					seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
				}
				else {
					PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
					seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
					ret.getSeries().put(category, seri);
				}
			}
		    
		    //3
		    whereClause=" AND p.val = 'answer3' AND p.c131='YES' ";
			if(dto.getFromDate()!=null) {
				whereClause+=" AND p.c1 >=:fromDate ";
			}
			if(dto.getToDate()!=null) {
				whereClause+=" AND p.c1 < :toDate ";
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				whereClause+=" AND p.c1_org_id in (:orgIds) ";
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
				}else {
					whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
				}
			}

			q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
			
			if(dto.getFromDate()!=null) {
				q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getToDate()!=null) {
				q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				q.setParameterList("orgIds",dto.getOrgIds());
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					q.setParameter("c12answer1",PEC12.answer1.toString());
					q.setParameter("c12answer2",PEC12.answer2.toString());
					q.setParameter("b1",PEC16.answer1.toString());
					q.setParameter("b3",PEC16.answer3.toString());
				}else {
					q.setParameter("c12answer1",PEC12.answer1.toString());
					q.setParameter("c12answer2",PEC12.answer2.toString());
					q.setParameter("b1",PEC16.answer1.toString());
					q.setParameter("b3",PEC16.answer3.toString());
				}
			}
		
	        lstFirst = q.list();
	        
	        
	        whereClause=" AND p.val = 'answer3' AND p.c131='YES' AND p.c131_result='answer2' ";
			if(dto.getFromDate()!=null) {
				whereClause+=" AND p.c1 >=:fromDate ";
			}
			if(dto.getToDate()!=null) {
				whereClause+=" AND p.c1 < :toDate ";
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				whereClause+=" AND p.c1_org_id in (:orgIds) ";
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
				}else {
					whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
				}
			}

			
			q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
			
			if(dto.getFromDate()!=null) {
				q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getToDate()!=null) {
				q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				q.setParameterList("orgIds",dto.getOrgIds());
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					q.setParameter("c12answer1",PEC12.answer1.toString());
					q.setParameter("c12answer2",PEC12.answer2.toString());
					q.setParameter("b1",PEC16.answer1.toString());
					q.setParameter("b3",PEC16.answer3.toString());
				}else {
					q.setParameter("c12answer1",PEC12.answer1.toString());
					q.setParameter("c12answer2",PEC12.answer2.toString());
					q.setParameter("b1",PEC16.answer1.toString());
					q.setParameter("b3",PEC16.answer3.toString());
				}
			}
			
			lstSecond = q.list();
			
			category = "Bán dâm";
			for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
				if(ret.getSeries().get(category)!=null) {
					PreventionChartSeriesDto seri = ret.getSeries().get(category);
					seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
				}
				else {
					PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
					seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
					ret.getSeries().put(category, seri);
				}
			}
		    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
				if(ret.getSeries().get(category)!=null) {
					PreventionChartSeriesDto seri = ret.getSeries().get(category);
					seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
				}
				else {
					PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
					seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
					ret.getSeries().put(category, seri);
				}
			}
		    
		  //4
		    whereClause=" AND p.val = 'answer4' AND p.c131='YES' ";
			if(dto.getFromDate()!=null) {
				whereClause+=" AND p.c1 >=:fromDate ";
			}
			if(dto.getToDate()!=null) {
				whereClause+=" AND p.c1 < :toDate ";
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				whereClause+=" AND p.c1_org_id in (:orgIds) ";
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
					if(dto.getReportTypes().get(0)==1) {
						whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
					}else {
						whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
					}
				}

			}
			
			
			q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
			
			if(dto.getFromDate()!=null) {
				q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getToDate()!=null) {
				q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				q.setParameterList("orgIds",dto.getOrgIds());
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					q.setParameter("c12answer1",PEC12.answer1.toString());
					q.setParameter("c12answer2",PEC12.answer2.toString());
					q.setParameter("b1",PEC16.answer1.toString());
					q.setParameter("b3",PEC16.answer3.toString());
				}else {
					q.setParameter("c12answer1",PEC12.answer1.toString());
					q.setParameter("c12answer2",PEC12.answer2.toString());
					q.setParameter("b1",PEC16.answer1.toString());
					q.setParameter("b3",PEC16.answer3.toString());
				}
			}
		
	        lstFirst = q.list();
	        
	        
	        whereClause=" AND p.val = 'answer4' AND p.c131='YES' AND p.c131_result='answer2' ";
			if(dto.getFromDate()!=null) {
				whereClause+=" AND p.c1 >=:fromDate ";
			}
			if(dto.getToDate()!=null) {
				whereClause+=" AND p.c1 < :toDate ";
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				whereClause+=" AND p.c1_org_id in (:orgIds) ";
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
				}else {
					whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
				}
			}

			
			q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
			
			if(dto.getFromDate()!=null) {
				q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getToDate()!=null) {
				q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				q.setParameterList("orgIds",dto.getOrgIds());
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					q.setParameter("c12answer1",PEC12.answer1.toString());
					q.setParameter("c12answer2",PEC12.answer2.toString());
					q.setParameter("b1",PEC16.answer1.toString());
					q.setParameter("b3",PEC16.answer3.toString());
				}else {
					q.setParameter("c12answer1",PEC12.answer1.toString());
					q.setParameter("c12answer2",PEC12.answer2.toString());
					q.setParameter("b1",PEC16.answer1.toString());
					q.setParameter("b3",PEC16.answer3.toString());
				}
			}
			
			lstSecond = q.list();
			
			category = "TG";
			for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
				if(ret.getSeries().get(category)!=null) {
					PreventionChartSeriesDto seri = ret.getSeries().get(category);
					seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
				}
				else {
					PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
					seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
					ret.getSeries().put(category, seri);
				}
			}
		    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
				if(ret.getSeries().get(category)!=null) {
					PreventionChartSeriesDto seri = ret.getSeries().get(category);
					seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
				}
				else {
					PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
					seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
					ret.getSeries().put(category, seri);
				}
			}
		    
		  //5
		    whereClause=" AND p.val = 'answer5' AND p.c131='YES'  ";
			if(dto.getFromDate()!=null) {
				whereClause+=" AND p.c1 >=:fromDate ";
			}
			if(dto.getToDate()!=null) {
				whereClause+=" AND p.c1 < :toDate ";
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				whereClause+=" AND p.c1_org_id in (:orgIds) ";
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
				}else {
					whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
				}
			}

			
			
			q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
			
			if(dto.getFromDate()!=null) {
				q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getToDate()!=null) {
				q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				q.setParameterList("orgIds",dto.getOrgIds());
			}

			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					q.setParameter("c12answer1",PEC12.answer1.toString());
					q.setParameter("c12answer2",PEC12.answer2.toString());
					q.setParameter("b1",PEC16.answer1.toString());
					q.setParameter("b3",PEC16.answer3.toString());
				}else {
					q.setParameter("c12answer1",PEC12.answer1.toString());
					q.setParameter("c12answer2",PEC12.answer2.toString());
					q.setParameter("b1",PEC16.answer1.toString());
					q.setParameter("b3",PEC16.answer3.toString());
				}
			}
		
	        lstFirst = q.list();
	        
	        
	        whereClause=" AND p.val = 'answer5' AND p.c131='YES' AND p.c131_result='answer2' ";
			if(dto.getFromDate()!=null) {
				whereClause+=" AND p.c1 >=:fromDate ";
			}
			if(dto.getToDate()!=null) {
				whereClause+=" AND p.c1 < :toDate ";
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				whereClause+=" AND p.c1_org_id in (:orgIds) ";
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
				}else {
					whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
				}
			}

			q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
			
			if(dto.getFromDate()!=null) {
				q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getToDate()!=null) {
				q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				q.setParameterList("orgIds",dto.getOrgIds());
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					q.setParameter("c12answer1",PEC12.answer1.toString());
					q.setParameter("c12answer2",PEC12.answer2.toString());
					q.setParameter("b1",PEC16.answer1.toString());
					q.setParameter("b3",PEC16.answer3.toString());
				}else {
					q.setParameter("c12answer1",PEC12.answer1.toString());
					q.setParameter("c12answer2",PEC12.answer2.toString());
					q.setParameter("b1",PEC16.answer1.toString());
					q.setParameter("b3",PEC16.answer3.toString());
				}
			}
			
			lstSecond = q.list();
			
			category = "Vợ/Chồng/BT của NCH ";
			for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
				if(ret.getSeries().get(category)!=null) {
					PreventionChartSeriesDto seri = ret.getSeries().get(category);
					seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
				}
				else {
					PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
					seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
					ret.getSeries().put(category, seri);
				}
			}
		    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
				if(ret.getSeries().get(category)!=null) {
					PreventionChartSeriesDto seri = ret.getSeries().get(category);
					seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
				}
				else {
					PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
					seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
					ret.getSeries().put(category, seri);
				}
			}
		    
		  //6
		    whereClause=" AND p.val = 'answer6' AND p.c131='YES' ";
			if(dto.getFromDate()!=null) {
				whereClause+=" AND p.c1 >=:fromDate ";
			}
			if(dto.getToDate()!=null) {
				whereClause+=" AND p.c1 < :toDate ";
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				whereClause+=" AND p.c1_org_id in (:orgIds) ";
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
				}else {
					whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
				}
			}
		
			q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
			
			if(dto.getFromDate()!=null) {
				q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getToDate()!=null) {
				q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				q.setParameterList("orgIds",dto.getOrgIds());
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					q.setParameter("c12answer1",PEC12.answer1.toString());
					q.setParameter("c12answer2",PEC12.answer2.toString());
					q.setParameter("b1",PEC16.answer1.toString());
					q.setParameter("b3",PEC16.answer3.toString());
				}else {
					q.setParameter("c12answer1",PEC12.answer1.toString());
					q.setParameter("c12answer2",PEC12.answer2.toString());
					q.setParameter("b1",PEC16.answer1.toString());
					q.setParameter("b3",PEC16.answer3.toString());
				}
			}
		
	        lstFirst = q.list();
	        
	        
	        whereClause=" AND p.val = 'answer6' AND p.c131='YES' AND p.c131_result='answer2' ";
			if(dto.getFromDate()!=null) {
				whereClause+=" AND p.c1 >=:fromDate ";
			}
			if(dto.getToDate()!=null) {
				whereClause+=" AND p.c1 < :toDate ";
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				whereClause+=" AND p.c1_org_id in (:orgIds) ";
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
				}else {
					whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
				}
			}			
			q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
			
			if(dto.getFromDate()!=null) {
				q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getToDate()!=null) {
				q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				q.setParameterList("orgIds",dto.getOrgIds());
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					q.setParameter("c12answer1",PEC12.answer1.toString());
					q.setParameter("c12answer2",PEC12.answer2.toString());
					q.setParameter("b1",PEC16.answer1.toString());
					q.setParameter("b3",PEC16.answer3.toString());
				}else {
					q.setParameter("c12answer1",PEC12.answer1.toString());
					q.setParameter("c12answer2",PEC12.answer2.toString());
					q.setParameter("b1",PEC16.answer1.toString());
					q.setParameter("b3",PEC16.answer3.toString());
				}
			}
			
			lstSecond = q.list();
			
			category = "BT/BC của NCH ";
			for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
				if(ret.getSeries().get(category)!=null) {
					PreventionChartSeriesDto seri = ret.getSeries().get(category);
					seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
				}
				else {
					PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
					seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
					ret.getSeries().put(category, seri);
				}
			}
		    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
				if(ret.getSeries().get(category)!=null) {
					PreventionChartSeriesDto seri = ret.getSeries().get(category);
					seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
				}
				else {
					PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
					seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
					ret.getSeries().put(category, seri);
				}
			}
		}
		
	    if(modality1==true) {
//	    	sql = "select count(id)  as quantity from (select id,val,c14,c4_counselling_date,c2_org_id,c24,c11c,c11b from "
//	    			+ " ( select (ROW_NUMBER() OVER (PARTITION BY h.id order by h.id,CONVERT(SUBSTRING(r.val,7,9),UNSIGNED INTEGER) )) "
//	    			+ " AS RowNumber,h.id as id,h.c14,h.c4_counselling_date,h.c2_org_id,h.c24,h.c11c,h.c11b, r.val from tbl_hts_case_risk_group r "
//	    			+ " inner join tbl_hts_case h on h.id = r.hts_case_id ) as tbl where tbl.RowNumber=1) as p "
//					+ " WHERE 1=1 "; 		
	    	sql = "select count(id)  as quantity from (select id,val,c14,c4_counselling_date,c2_org_id,c24,c11c,c11b from "
	    			+ " ( select h.id as id,h.c14,h.c4_counselling_date,h.c2_org_id,h.c24,h.c11c,h.c11b, r.val,r.is_main_risk from tbl_hts_case_risk_group r "
	    			+ " inner join tbl_hts_case h on h.id = r.hts_case_id ) as tbl where tbl.is_main_risk=1) as p "
					+ " WHERE 1=1 "; 		
		    whereClause=" AND p.val != 'answer14' AND p.c14!='answer4' AND p.val != 'answer1' AND p.val != 'answer2' AND p.val != 'answer3'"
		    		+ " AND p.val != 'answer4' AND p.val != 'answer5' AND p.val != 'answer6'";
	        if(dto.getFromDate()!=null) {
				whereClause+=" AND p.c4_counselling_date >=:fromDate ";
			}
			if(dto.getToDate()!=null) {
				whereClause+=" AND p.c4_counselling_date < :toDate ";
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				whereClause+=" AND p.c2_org_id in (:orgIds) ";
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}else {
					whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}
			}
	        
	        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
	        if(dto.getFromDate()!=null) {
				q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getToDate()!=null) {
				q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				q.setParameterList("orgIds",dto.getOrgIds());
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}else {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}
			}
			
			lstFirst = q.list();
			
			whereClause=" AND p.val != 'answer14' AND p.c14='answer2' AND p.val != 'answer1' AND p.val != 'answer2' AND p.val != 'answer3'"
		    		+ " AND p.val != 'answer4' AND p.val != 'answer5' AND p.val != 'answer6'";
	        if(dto.getFromDate()!=null) {
				whereClause+=" AND p.c4_counselling_date >=:fromDate ";
			}
			if(dto.getToDate()!=null) {
				whereClause+=" AND p.c4_counselling_date < :toDate ";
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				whereClause+=" AND p.c2_org_id in (:orgIds) ";
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}else {
					whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}
			}
	        
	        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
	        if(dto.getFromDate()!=null) {
				q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getToDate()!=null) {
				q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				q.setParameterList("orgIds",dto.getOrgIds());
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}else {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}
			}
			
			lstSecond = q.list();
			
			category = "Khác";
			for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
				if(ret.getSeries().get(category)!=null) {
					PreventionChartSeriesDto seri = ret.getSeries().get(category);
					seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
				}
				else {
					PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
					seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
					ret.getSeries().put(category, seri);
				}
			}
		    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
				if(ret.getSeries().get(category)!=null) {
					PreventionChartSeriesDto seri = ret.getSeries().get(category);
					seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
				}
				else {
					PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
					seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
					ret.getSeries().put(category, seri);
				}
			}
		    
		    //1
		    whereClause=" AND p.val = 'answer1' AND p.c14!='answer4' ";
	        if(dto.getFromDate()!=null) {
				whereClause+=" AND p.c4_counselling_date >=:fromDate ";
			}
			if(dto.getToDate()!=null) {
				whereClause+=" AND p.c4_counselling_date < :toDate ";
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				whereClause+=" AND p.c2_org_id in (:orgIds) ";
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}else {
					whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}
			}
	        
	        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
	        if(dto.getFromDate()!=null) {
				q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getToDate()!=null) {
				q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				q.setParameterList("orgIds",dto.getOrgIds());
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}else {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}
			}
			
			lstFirst = q.list();
			
			whereClause=" AND p.val = 'answer1' AND p.c14='answer2' ";
	        if(dto.getFromDate()!=null) {
				whereClause+=" AND p.c4_counselling_date >=:fromDate ";
			}
			if(dto.getToDate()!=null) {
				whereClause+=" AND p.c4_counselling_date < :toDate ";
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				whereClause+=" AND p.c2_org_id in (:orgIds) ";
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}else {
					whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}
			}
	        
	        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
	        if(dto.getFromDate()!=null) {
				q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getToDate()!=null) {
				q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				q.setParameterList("orgIds",dto.getOrgIds());
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}else {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}
			}
			
			lstSecond = q.list();
			
			category = "NNCMT";
			for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
				if(ret.getSeries().get(category)!=null) {
					PreventionChartSeriesDto seri = ret.getSeries().get(category);
					seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
				}
				else {
					PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
					seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
					ret.getSeries().put(category, seri);
				}
			}
		    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
				if(ret.getSeries().get(category)!=null) {
					PreventionChartSeriesDto seri = ret.getSeries().get(category);
					seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
				}
				else {
					PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
					seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
					ret.getSeries().put(category, seri);
				}
			}
		    
		    //2
		    whereClause=" AND p.val = 'answer2' AND p.c14!='answer4' ";
	        if(dto.getFromDate()!=null) {
				whereClause+=" AND p.c4_counselling_date >=:fromDate ";
			}
			if(dto.getToDate()!=null) {
				whereClause+=" AND p.c4_counselling_date < :toDate ";
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				whereClause+=" AND p.c2_org_id in (:orgIds) ";
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}else {
					whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}
			}
	        
	        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
	        if(dto.getFromDate()!=null) {
				q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getToDate()!=null) {
				q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				q.setParameterList("orgIds",dto.getOrgIds());
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}else {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}
			}
			
			lstFirst = q.list();
			
			whereClause=" AND p.val = 'answer2' AND p.c14='answer2' ";
	        if(dto.getFromDate()!=null) {
				whereClause+=" AND p.c4_counselling_date >=:fromDate ";
			}
			if(dto.getToDate()!=null) {
				whereClause+=" AND p.c4_counselling_date < :toDate ";
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				whereClause+=" AND p.c2_org_id in (:orgIds) ";
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}else {
					whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}
			}
	        
	        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
	        if(dto.getFromDate()!=null) {
				q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getToDate()!=null) {
				q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				q.setParameterList("orgIds",dto.getOrgIds());
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}else {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}
			}
			
			lstSecond = q.list();
			
			category = "MSM";
			for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
				if(ret.getSeries().get(category)!=null) {
					PreventionChartSeriesDto seri = ret.getSeries().get(category);
					seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
				}
				else {
					PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
					seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
					ret.getSeries().put(category, seri);
				}
			}
		    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
				if(ret.getSeries().get(category)!=null) {
					PreventionChartSeriesDto seri = ret.getSeries().get(category);
					seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
				}
				else {
					PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
					seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
					ret.getSeries().put(category, seri);
				}
			}
		    
		  //3
		    whereClause=" AND p.val = 'answer3' AND p.c14!='answer4' ";
	        if(dto.getFromDate()!=null) {
				whereClause+=" AND p.c4_counselling_date >=:fromDate ";
			}
			if(dto.getToDate()!=null) {
				whereClause+=" AND p.c4_counselling_date < :toDate ";
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				whereClause+=" AND p.c2_org_id in (:orgIds) ";
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}else {
					whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}
			}
	        
	        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
	        if(dto.getFromDate()!=null) {
				q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getToDate()!=null) {
				q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				q.setParameterList("orgIds",dto.getOrgIds());
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}else {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}
			}
			
			lstFirst = q.list();
			
			whereClause=" AND p.val = 'answer3' AND p.c14='answer2' ";
	        if(dto.getFromDate()!=null) {
				whereClause+=" AND p.c4_counselling_date >=:fromDate ";
			}
			if(dto.getToDate()!=null) {
				whereClause+=" AND p.c4_counselling_date < :toDate ";
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				whereClause+=" AND p.c2_org_id in (:orgIds) ";
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}else {
					whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}
			}
	        
	        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
	        if(dto.getFromDate()!=null) {
				q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getToDate()!=null) {
				q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				q.setParameterList("orgIds",dto.getOrgIds());
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}else {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}
			}
			
			lstSecond = q.list();
			
			
			category = "Bán dâm";
			
			for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
				if(ret.getSeries().get(category)!=null) {
					PreventionChartSeriesDto seri = ret.getSeries().get(category);
					seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
				}
				else {
					PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
					seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
					ret.getSeries().put(category, seri);
				}
			}
		    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
				if(ret.getSeries().get(category)!=null) {
					PreventionChartSeriesDto seri = ret.getSeries().get(category);
					seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
				}
				else {
					PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
					seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
					ret.getSeries().put(category, seri);
				}
			}
		    
		  //4
		    whereClause=" AND p.val = 'answer4' AND p.c14!='answer4' ";
	        if(dto.getFromDate()!=null) {
				whereClause+=" AND p.c4_counselling_date >=:fromDate ";
			}
			if(dto.getToDate()!=null) {
				whereClause+=" AND p.c4_counselling_date < :toDate ";
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				whereClause+=" AND p.c2_org_id in (:orgIds) ";
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}else {
					whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}
			}
	        
	        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
	        if(dto.getFromDate()!=null) {
				q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getToDate()!=null) {
				q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				q.setParameterList("orgIds",dto.getOrgIds());
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}else {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}
			}
			
			lstFirst = q.list();
			
			whereClause=" AND p.val = 'answer4' AND p.c14='answer2' ";
	        if(dto.getFromDate()!=null) {
				whereClause+=" AND p.c4_counselling_date >=:fromDate ";
			}
			if(dto.getToDate()!=null) {
				whereClause+=" AND p.c4_counselling_date < :toDate ";
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				whereClause+=" AND p.c2_org_id in (:orgIds) ";
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}else {
					whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}
			}
	        
	        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
	        if(dto.getFromDate()!=null) {
				q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getToDate()!=null) {
				q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				q.setParameterList("orgIds",dto.getOrgIds());
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}else {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}
			}
			
			lstSecond = q.list();
			
			
			category = "TG";
			
			for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
				if(ret.getSeries().get(category)!=null) {
					PreventionChartSeriesDto seri = ret.getSeries().get(category);
					seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
				}
				else {
					PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
					seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
					ret.getSeries().put(category, seri);
				}
			}
		    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
				if(ret.getSeries().get(category)!=null) {
					PreventionChartSeriesDto seri = ret.getSeries().get(category);
					seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
				}
				else {
					PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
					seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
					ret.getSeries().put(category, seri);
				}
			}
		    
		  //5
		    whereClause=" AND p.val = 'answer5' AND p.c14!='answer4' ";
	        if(dto.getFromDate()!=null) {
				whereClause+=" AND p.c4_counselling_date >=:fromDate ";
			}
			if(dto.getToDate()!=null) {
				whereClause+=" AND p.c4_counselling_date < :toDate ";
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				whereClause+=" AND p.c2_org_id in (:orgIds) ";
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}else {
					whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}
			}
	        
	        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
	        if(dto.getFromDate()!=null) {
				q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getToDate()!=null) {
				q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				q.setParameterList("orgIds",dto.getOrgIds());
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}else {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}
			}
			
			lstFirst = q.list();
			
			whereClause=" AND p.val = 'answer5' AND p.c14='answer2' ";
	        if(dto.getFromDate()!=null) {
				whereClause+=" AND p.c4_counselling_date >=:fromDate ";
			}
			if(dto.getToDate()!=null) {
				whereClause+=" AND p.c4_counselling_date < :toDate ";
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				whereClause+=" AND p.c2_org_id in (:orgIds) ";
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}else {
					whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}
			}
	        
	        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
	        if(dto.getFromDate()!=null) {
				q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getToDate()!=null) {
				q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				q.setParameterList("orgIds",dto.getOrgIds());
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}else {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}
			}
			
			lstSecond = q.list();
			
			
			category = "Vợ/Chồng/BT của NCH ";
			
			for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
				if(ret.getSeries().get(category)!=null) {
					PreventionChartSeriesDto seri = ret.getSeries().get(category);
					seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
				}
				else {
					PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
					seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
					ret.getSeries().put(category, seri);
				}
			}
		    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
				if(ret.getSeries().get(category)!=null) {
					PreventionChartSeriesDto seri = ret.getSeries().get(category);
					seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
				}
				else {
					PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
					seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
					ret.getSeries().put(category, seri);
				}
			}
		    
		  //6
		    whereClause=" AND p.val = 'answer6' AND p.c14!='answer4' ";
	        if(dto.getFromDate()!=null) {
				whereClause+=" AND p.c4_counselling_date >=:fromDate ";
			}
			if(dto.getToDate()!=null) {
				whereClause+=" AND p.c4_counselling_date < :toDate ";
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				whereClause+=" AND p.c2_org_id in (:orgIds) ";
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}else {
					whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}
			}
	        
	        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
	        if(dto.getFromDate()!=null) {
				q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getToDate()!=null) {
				q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				q.setParameterList("orgIds",dto.getOrgIds());
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}else {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}
			}
			
			lstFirst = q.list();
			
			whereClause=" AND p.val = 'answer6' AND p.c14='answer2' ";
	        if(dto.getFromDate()!=null) {
				whereClause+=" AND p.c4_counselling_date >=:fromDate ";
			}
			if(dto.getToDate()!=null) {
				whereClause+=" AND p.c4_counselling_date < :toDate ";
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				whereClause+=" AND p.c2_org_id in (:orgIds) ";
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}else {
					whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}
			}
	        
	        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
	        if(dto.getFromDate()!=null) {
				q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getToDate()!=null) {
				q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				q.setParameterList("orgIds",dto.getOrgIds());
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}else {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}
			}
			
			lstSecond = q.list();
			
			category = "BT/BC của NCH ";
			for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
				if(ret.getSeries().get(category)!=null) {
					PreventionChartSeriesDto seri = ret.getSeries().get(category);
					seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
				}
				else {
					PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
					seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
					ret.getSeries().put(category, seri);
				}
			}
		    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
				if(ret.getSeries().get(category)!=null) {
					PreventionChartSeriesDto seri = ret.getSeries().get(category);
					seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
				}
				else {
					PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
					seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
					ret.getSeries().put(category, seri);
				}
			}
		    
		  //7
		    whereClause=" AND p.val = 'answer14' AND p.c14!='answer4' ";
	        if(dto.getFromDate()!=null) {
				whereClause+=" AND p.c4_counselling_date >=:fromDate ";
			}
			if(dto.getToDate()!=null) {
				whereClause+=" AND p.c4_counselling_date < :toDate ";
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				whereClause+=" AND p.c2_org_id in (:orgIds) ";
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}else {
					whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}
			}
	        
	        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
	        if(dto.getFromDate()!=null) {
				q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getToDate()!=null) {
				q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				q.setParameterList("orgIds",dto.getOrgIds());
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}else {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}
			}
			
			lstFirst = q.list();
			
			whereClause=" AND p.val = 'answer14' AND p.c14='answer2' ";
	        if(dto.getFromDate()!=null) {
				whereClause+=" AND p.c4_counselling_date >=:fromDate ";
			}
			if(dto.getToDate()!=null) {
				whereClause+=" AND p.c4_counselling_date < :toDate ";
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				whereClause+=" AND p.c2_org_id in (:orgIds) ";
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}else {
					whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}
			}
	        
	        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
	        if(dto.getFromDate()!=null) {
				q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getToDate()!=null) {
				q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				q.setParameterList("orgIds",dto.getOrgIds());
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}else {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}
			}
			
			lstSecond = q.list();
			
			category = "Phạm nhân";
			for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
				if(ret.getSeries().get(category)!=null) {
					PreventionChartSeriesDto seri = ret.getSeries().get(category);
					seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
				}
				else {
					PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
					seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
					ret.getSeries().put(category, seri);
				}
			}
		    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
				if(ret.getSeries().get(category)!=null) {
					PreventionChartSeriesDto seri = ret.getSeries().get(category);
					seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
				}
				else {
					PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
					seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
					ret.getSeries().put(category, seri);
				}
			}
			
	    }
	    if(dto.getChart4()!=null && dto.getChart4()==1) {
	    	ret.getSeries().clear();
	    	//Tổng số xét nghiệm từ PE02
			sql="SELECT COUNT(id) as quantity, p.c3 as title FROM tbl_pe_case p WHERE 1=1 ";
			
			whereClause=" AND p.c131='YES' ";
			
			String groupBy="GROUP BY  p.c3 ";
			if(dto.getFromDate()!=null) {
				whereClause+=" AND p.c1 >=:fromDate ";
			}
			if(dto.getToDate()!=null) {
				whereClause+=" AND p.c1 < :toDate ";
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {			
				whereClause+=" AND p.c1_org_id in (:orgIds) ";
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					whereClause+= " AND NOT ((p.c12 !=:c12answer1 AND p.c12 is not null) OR (p.c16 is not null AND p.c16 =:b2 )) ";
				}else {
					whereClause+= " AND ((p.c12 !=:c12answer1 AND p.c12 is not null) OR (p.c16 is not null AND p.c16 =:b2 )) ";
				}
			}
			
			q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+groupBy).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
			
			if(dto.getFromDate()!=null) {
				q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getToDate()!=null) {
				q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				q.setParameterList("orgIds",dto.getOrgIds());
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					q.setParameter("c12answer1",PEC12.answer1.toString());
					q.setParameter("b2",PEC16.answer2.toString());
				}else {
					q.setParameter("c12answer1",PEC12.answer1.toString());
					q.setParameter("b2",PEC16.answer2.toString());
				}
			}
		
	        lstFirst = q.list();
	        
	        
	        sql = "SELECT COUNT(id) as quantity, p.c3 as title FROM tbl_pe_case p WHERE 1=1 ";
	        whereClause=" AND p.c131='YES' AND p.c131_result='answer2' ";
	        
	        if(dto.getFromDate()!=null) {
				whereClause+=" AND p.c1 >=:fromDate ";
			}
			if(dto.getToDate()!=null) {
				whereClause+=" AND p.c1 < :toDate ";
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				whereClause+=" AND p.c1_org_id in (:orgIds) ";
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
				}else {
					whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
				}
			}

			
	        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+groupBy).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
	        if(dto.getFromDate()!=null) {
				q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getToDate()!=null) {
				q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				q.setParameterList("orgIds",dto.getOrgIds());
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					q.setParameter("c12answer1",PEC12.answer1.toString());
					q.setParameter("c12answer2",PEC12.answer2.toString());
					q.setParameter("b1",PEC16.answer1.toString());
					q.setParameter("b3",PEC16.answer3.toString());
				}else {
					q.setParameter("c12answer1",PEC12.answer1.toString());
					q.setParameter("c12answer2",PEC12.answer2.toString());
					q.setParameter("b1",PEC16.answer1.toString());
					q.setParameter("b3",PEC16.answer3.toString());
				}
			}
			
			
	        lstSecond = q.list();
	        
	        
	        
	      //Tổng số xét nghiệm từ HTS
	        sql="SELECT COUNT(id) as quantity, p.c7_gender as title FROM tbl_hts_case p WHERE 1=1 ";        		
	        whereClause=" AND p.c14 IS NOT NULL AND p.c14 <> 'answer4' ";
	        groupBy="GROUP BY  p.c7_gender ";
	        if(dto.getFromDate()!=null) {
				whereClause+=" AND p.c4_counselling_date >=:fromDate ";
			}
			if(dto.getToDate()!=null) {
				whereClause+=" AND p.c4_counselling_date < :toDate ";
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				whereClause+=" AND p.c2_org_id in (:orgIds) ";
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}else {
					whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}
			}
	        
	        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+groupBy).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
	        if(dto.getFromDate()!=null) {
				q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getToDate()!=null) {
				q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				q.setParameterList("orgIds",dto.getOrgIds());
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}else {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}
			}
			
	        List<PreventionChartDetailDto> lstHtsFirst = q.list();
	        
	        sql="SELECT COUNT(id) as quantity, p.c7_gender as title FROM tbl_hts_case p WHERE 1=1 ";
	        whereClause=" AND p.c14 IS NOT NULL AND p.c14 = 'answer2' ";
	        if(dto.getFromDate()!=null) {
				whereClause+=" AND p.c4_counselling_date >=:fromDate ";
			}
			if(dto.getToDate()!=null) {
				whereClause+=" AND p.c4_counselling_date < :toDate ";
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				whereClause+=" AND p.c2_org_id in (:orgIds) ";
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}else {
					whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				}
			}
			
			q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+groupBy).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
			if(dto.getFromDate()!=null) {
				q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getToDate()!=null) {
				q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
			}
			if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
				q.setParameterList("orgIds",dto.getOrgIds());
			}
			if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
				if(dto.getReportTypes().get(0)==1) {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}else {
					q.setParameter("c24answer2", HTSc24.answer2.toString());
					q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
					q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
				}
			}
			
			List<PreventionChartDetailDto> lstHtsSecond = q.list();
	        
			if(dto.getModality()!=null && dto.getModality().size()==1) {
				if(dto.getModality().get(0)==1) {
					lstFirst.clear();
					lstSecond.clear();
					lstFirst.addAll(lstHtsFirst);
					lstSecond.addAll(lstHtsSecond);
				}else {
					
				}
			}else {
				lstFirst.addAll(lstHtsFirst);
				lstSecond.addAll(lstHtsSecond);
			}
			
			
			for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
				String category1 = "Nam";
				if(preventionChartDetailDto.getTitle().toUpperCase().equals("MALE")) {
					category1 = "Nam";
				}
				if(preventionChartDetailDto.getTitle().toUpperCase().equals("FEMALE")) {
					category1 = "Nữ";
				}
				if(ret.getSeries().get(category1)!=null) {
					PreventionChartSeriesDto seri = ret.getSeries().get(category1);
					seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
				}
				else {
					PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
					seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
					ret.getSeries().put(category1, seri);
				}
			}
		    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
		    	String category1 = "Nam";
				if(preventionChartDetailDto.getTitle().toUpperCase().equals("MALE")) {
					category1 = "Nam";
				}
				if(preventionChartDetailDto.getTitle().toUpperCase().equals("FEMALE")) {
					category1 = "Nữ";
				}
				if(ret.getSeries().get(category1)!=null) {
					PreventionChartSeriesDto seri = ret.getSeries().get(category1);
					seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
				}
				else {
					PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
					seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
					ret.getSeries().put(category1, seri);
				}
			}
		    
	    }
	    
	    
	    
		return ret;
	}
	@Override
	public PreventionChartDto getTotalsPE(PreventionFilterDto dto) {
//		a;
		PreventionChartDto ret = new PreventionChartDto();
		ret.setTitle("Tiếp cận tại cộng đồng");
		String sql="SELECT COUNT(id) as quantity FROM tbl_pe_case p WHERE 1=1 AND (p.c12=:c12answer1 OR p.c12=:c12answer2) ";
		
		String whereClause=" ";
		if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c11_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c11_date <:toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c1_org_id in (:orgIds) ";
		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
//			}
//			else {
//				whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
//			}
//		}

//		Query q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause);
		Query q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		q.setParameter("c12answer1",PEC12.answer1.toString());
		q.setParameter("c12answer2",PEC12.answer2.toString());
		if(dto.getFromDate()!=null) {
//			System.out.println("h1");
//			System.out.println(Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
//			System.out.println("h2");
//			System.out.println(Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("c12answer2",PEC12.answer2.toString());
//				q.setParameter("b1",PEC16.answer1.toString());
//				q.setParameter("b3",PEC16.answer3.toString());
//			}else {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("c12answer2",PEC12.answer2.toString());
//				q.setParameter("b1",PEC16.answer1.toString());
//				q.setParameter("b3",PEC16.answer3.toString());
//			}
//		}
	
//        BigInteger quantity = (BigInteger)q.uniqueResult();
        
//        if(dto.getModality()!=null && dto.getModality().size()==1) {
//			if(dto.getModality().get(0)==1) {
//				lstFirst.clear();
//				
//			}
//		}
//        String category = "totals";
//        PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
//        seri.setCategory(category);
//		seri.setFirstQuantity(seri.getFirstQuantity().add(quantity));
//		Hashtable<String, PreventionChartSeriesDto> series = new Hashtable<String, PreventionChartSeriesDto>();
//		series.put(category, seri);
//		ret.setSeries(series);
//		return ret;
		List<PreventionChartDetailDto> lstPeFirst = q.list();

		if(dto.getModality()!=null && dto.getModality().size()==1) {
			if(dto.getModality().get(0)==1) {
				lstPeFirst.clear();
			}
		}

		for (PreventionChartDetailDto preventionChartDetailDto : lstPeFirst) {
			String category = "totals";

			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
		return ret;
	}
	@Override
	public PreventionChartDto getTotalsHTS(PreventionFilterDto dto) {
		PreventionChartDto ret = new PreventionChartDto();
		ret.setTitle("Tư vấn xét nghiệm HIV");
		String sql="SELECT COUNT(id) as quantity FROM tbl_hts_case p WHERE 1=1 ";
		String whereClause=" ";
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}



		//whereClause += "AND (p.c14 = :c14Answer1 OR p.c14 = :c14Answer2 OR p.c14 = :c14Answer3) ";
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND ((p.c24 =:c24answer2 AND p.c24 is not null) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) AND p.c15 = 'YES' ";
				//whereClause+= " AND p.c15_date >=:fromDate AND p.c15_date <=:toDate ";
				if(dto.getFromDate()!=null) {
					whereClause+=" AND p.c15_date >=:fromDate ";
				}
				if(dto.getToDate()!=null) {
					whereClause+=" AND p.c15_date <=:toDate ";
				}
			}
			else {
				//whereClause+= " AND ((p.c24 =:c24answer2 AND p.c24 is not null) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
				if(dto.getFromDate()!=null) {
					whereClause+=" AND p.c4_counselling_date >=:fromDate ";
				}
				if(dto.getToDate()!=null) {
					whereClause+=" AND p.c4_counselling_date <= :toDate ";
				}
			}
		}else{
			if(dto.getFromDate()!=null) {
				whereClause+=" AND p.c4_counselling_date >=:fromDate ";
			}
			if(dto.getToDate()!=null) {
				whereClause+=" AND p.c4_counselling_date <= :toDate ";
			}
		}
        
        Query q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
        if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
//		q.setParameter("c14Answer1",HTSc14.answer1.toString());
//		q.setParameter("c14Answer2",HTSc14.answer2.toString());
//		q.setParameter("c14Answer3",HTSc14.answer3.toString());
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
////			if(dto.getReportTypes().get(0)==1) {
////
////			}else {
////				q.setParameter("c24answer2", HTSc24.answer2.toString());
////				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
////				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
////			}
//			q.setParameter("c24answer2", HTSc24.answer2.toString());
//			q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//			q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//		}
		
        List<PreventionChartDetailDto> lstHtsFirst = q.list();

		sql="SELECT COUNT(id) as quantity FROM tbl_pe_case p WHERE 1=1 ";
		whereClause = " AND p.c9 = 'YES' ";
		if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c11_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c11_date <= :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c1_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}
			else {
				whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}
		}



//		Query q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause);
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));

		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}else {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}
		}

		List<PreventionChartDetailDto> lstPeFirst = q.list();

        if(dto.getModality()!=null && dto.getModality().size()==1) {
			if(dto.getModality().get(0)==2) {
				lstHtsFirst.clear();
			} else {
				lstPeFirst.clear();
			}
		}
        
        for (PreventionChartDetailDto preventionChartDetailDto : lstHtsFirst) {
			String category = "totals";
			
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
			} else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}

		for (PreventionChartDetailDto preventionChartDetailDto : lstPeFirst) {
			String category = "totals";
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
			} else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
		return ret;
        
	}
	@Override
	public PreventionChartDto getTotalsHIV(PreventionFilterDto dto) {
		PreventionChartDto ret = new PreventionChartDto();
		ret.setTitle("XN khẳng định HIV+");
		
		//Tổng số xét nghiệm từ PE02
		String sql="SELECT COUNT(id) as quantity FROM tbl_pe_case p WHERE 1=1 ";
		
		String whereClause=" AND p.c131='YES' AND p.c131_result='answer2' ";
		if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c11_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c11_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c1_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}else {
				whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}
		}

		
		
		
		Query q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}else {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}
		}
	
        List<PreventionChartDetailDto> lstFirst = q.list();
        
        
        sql = "SELECT COUNT(id) as quantity FROM tbl_pe_case p WHERE 1=1 ";
        whereClause=" AND p.c131='YES' AND p.c131_result='answer2' ";
        
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c11_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c11_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c1_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}else {
				whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}
		}
        
        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
        if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}else {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}
		}
		
		
        List<PreventionChartDetailDto> lstSecond = q.list();
        
      //Tổng số xét nghiệm từ HTS
        sql="SELECT COUNT(id) as quantity  FROM tbl_hts_case p WHERE 1=1 ";        		
        whereClause=" AND p.c14 IS NOT NULL AND p.c14 <> 'answer4' ";
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c4_counselling_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}else {
				whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}
		}
        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
        if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}else {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
		}
		
        List<PreventionChartDetailDto> lstHtsFirst = q.list();
        
        sql="SELECT COUNT(id) as quantity  FROM tbl_hts_case p WHERE 1=1 ";
        whereClause=" AND p.c14 IS NOT NULL AND p.c14 = 'answer2' ";
        
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c4_counselling_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}else {
				whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}
		}
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}else {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
		}
		
		List<PreventionChartDetailDto> lstHtsSecond = q.list();
        
		if(dto.getModality()!=null && dto.getModality().size()==1) {
			if(dto.getModality().get(0)==1) {
				lstFirst.clear();
				lstSecond.clear();
				lstFirst.addAll(lstHtsFirst);
				lstSecond.addAll(lstHtsSecond);
			}else {
				
			}
		}else {
			lstFirst.addAll(lstHtsFirst);
			lstSecond.addAll(lstHtsSecond);
		}
		
		
		for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
			String category = "totals";
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
	    	String category = "totals";
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
		return ret;
	}
	@Override
	public PreventionChartDto getTotalsARV(PreventionFilterDto dto) {
		PreventionChartDto ret = new PreventionChartDto();
		ret.setTitle("Chuyển gửi thành công đến CSĐT ARV");
		
		//Tổng số xét nghiệm từ PE02
//		String sql="SELECT COUNT(id) as quantity FROM tbl_pe_case p WHERE p.c131='YES' AND p.c131_result='answer2' ";
		String sql="SELECT COUNT(id) as quantity FROM tbl_pe_case p WHERE 1=1 ";

//		String whereClause=" AND p.c15 = 'YES' ";
		String whereClause=" AND (p.c12=:c12answer1 OR p.c12=:c12answer2) ";

		if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c15_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c15_date <= :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c1_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND ( (p.c12 =:c12answer1 OR p.c12 =:c12answer2) AND (p.c16 is null OR p.c16 =:b1 OR p.c16 =:b3)) ";
				whereClause+= " AND (p.c16 is null  or p.c16 !=:b2) ";
			}
//			else {
//				whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
//			}
		}

		Query q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}

		q.setParameter("c12answer1",PEC12.answer1.toString());
		q.setParameter("c12answer2",PEC12.answer2.toString());

		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("b1",PEC16.answer1.toString());
//				q.setParameter("b3",PEC16.answer3.toString());
				q.setParameter("b2",PEC16.answer2.toString());
			}
//			else {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("c12answer2",PEC12.answer2.toString());
//				q.setParameter("b1",PEC16.answer1.toString());
//				q.setParameter("b3",PEC16.answer3.toString());
//			}
		}
	
        List<PreventionChartDetailDto> lstFirst = q.list();
        
        sql = "SELECT COUNT(id) as quantity FROM tbl_pe_case p WHERE p.c8='answer1' AND p.c15 ='YES' ";
        whereClause=" ";
        
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c15_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c15_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c1_org_id in (:orgIds) ";
		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND NOT ((p.c12 !=:c12answer1 AND p.c12 is not null) OR (p.c16 is not null AND p.c16 =:b2 )) ";
//			}else {
//				whereClause+= " AND ((p.c12 !=:c12answer1 AND p.c12 is not null) OR (p.c16 is not null AND p.c16 =:b2 )) ";
//			}
//		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND ( (p.c12 =:c12answer1 OR p.c12 =:c12answer2) AND (p.c16 is null OR p.c16 =:b1 OR p.c16 =:b3)) ";
				//whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}
//			else {
//				whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
//			}
		}

		
        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
        if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}
//			else {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("c12answer2",PEC12.answer2.toString());
//				q.setParameter("b1",PEC16.answer1.toString());
//				q.setParameter("b3",PEC16.answer3.toString());
//			}
		}
		
		
        List<PreventionChartDetailDto> lstSecond = q.list();
        
        //HTS
        sql="SELECT COUNT(id) as quantity FROM tbl_hts_case p WHERE p.c14 = 'answer2'  " ;
        
        whereClause=" ";
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c20_reg_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c20_reg_date <= :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND ((p.c24 is null or p.c24 !=:c24answer2) AND (p.c11c is null OR p.c11c !=:c11cYes OR p.c11b =:c11bAnswer1)) ";
				//whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}
//			else {
//				whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
//			}
		}
        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
        if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
//			else {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}
		}
		
        List<PreventionChartDetailDto> lstHtsFirst = q.list();
        
        sql="SELECT COUNT(id) as quantity FROM tbl_hts_case p WHERE p.c14 = 'answer2' AND p.c20 = 'answer3' " ;
        
        whereClause=" ";
        
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c20_reg_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c20_reg_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
//			}else {
//				whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
//			}
//		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND ((p.c24 is null or p.c24 !=:c24answer2) AND (p.c11c is null OR p.c11c !=:c11cYes OR p.c11b =:c11bAnswer1)) ";
				//whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}
//			else {
//				whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
//			}
		}
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
//			else {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}
		}
		
		List<PreventionChartDetailDto> lstHtsSecond = q.list();
        
		if(dto.getModality()!=null && dto.getModality().size()==1) {
			if(dto.getModality().get(0)==1) {
				lstFirst.clear();
				lstSecond.clear();
				lstFirst.addAll(lstHtsFirst);
				lstSecond.addAll(lstHtsSecond);
			}else {
				
			}
		}else {
			lstFirst.addAll(lstHtsFirst);
			lstSecond.addAll(lstHtsSecond);
		}
		
		for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
			String category = "totals";
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
	    	String category = "totals";
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
		return ret;
	}
	@Override
	public PreventionChartDto getTotalsPrEP(PreventionFilterDto dto) {
		PreventionChartDto ret = new PreventionChartDto();
		ret.setTitle("Chuyển gửi thành công đến CSĐT PrEP");
		//Tổng số xét nghiệm từ PE02
		String sql="SELECT COUNT(id) as quantity FROM tbl_pe_case p WHERE 1=1 ";
		
		String whereClause=" AND p.c14='YES' AND p.c13=:c13answer2 ";
		if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c14_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c14_date <= :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c1_org_id in (:orgIds) ";
		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
//			}else {
//				whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
//			}
//		}

		Query q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		q.setParameter("c13answer2", PEc13.answer2.toString());
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("c12answer2",PEC12.answer2.toString());
//				q.setParameter("b1",PEC16.answer1.toString());
//				q.setParameter("b3",PEC16.answer3.toString());
//			}else {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("c12answer2",PEC12.answer2.toString());
//				q.setParameter("b1",PEC16.answer1.toString());
//				q.setParameter("b3",PEC16.answer3.toString());
//			}
//		}
	
        List<PreventionChartDetailDto> lstFirst = q.list();
        
        sql="SELECT COUNT(id) as quantity  FROM tbl_hts_case p WHERE 1=1 ";        		
        whereClause=" AND p.c16_27 = 'YES' ";
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c4_counselling_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}else {
				whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}
		}
        
        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
        if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}else {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
		}
		
        List<PreventionChartDetailDto> lstHtsFirst = q.list();
        
        if(dto.getModality()!=null && dto.getModality().size()==1) {
			if(dto.getModality().get(0)==1) {
				lstFirst.clear();
				lstFirst.addAll(lstHtsFirst);
			}else {
				
			}
		}else {
			lstFirst.addAll(lstHtsFirst);
		}
		
		for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
			String category = "totals";
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
		return ret;
	}
	@Override
	public PreventionChartDto getChart9(PreventionFilterDto dto) {
		PreventionChartDto ret = new PreventionChartDto();
		ret.setTitle("Chart9");
		String selectType="";
		String type="";
		String selectTypeHts="";
		String typeHts="";
		if(dto.getChart9()==null) {
			selectType=", MONTH(p.c1) as month ";
			type=", MONTH(p.c1) ";
			selectTypeHts=", MONTH(p.c4_counselling_date ) as month ";
			typeHts=", MONTH(p.c4_counselling_date ) ";
		}else {
			if(dto.getChart9()==0) {
				selectType=", MONTH(p.c1) as month ";
				type=", MONTH(p.c1) ";
				selectTypeHts=", MONTH(p.c4_counselling_date ) as month ";
				typeHts=", MONTH(p.c4_counselling_date ) ";
			}
			if(dto.getChart9()==1) {
				selectType=", QUARTER(p.c1) as month ";
				type=", QUARTER(p.c1) ";
				selectTypeHts=", QUARTER(p.c4_counselling_date ) as month ";
				typeHts=", QUARTER(p.c4_counselling_date ) ";
			}
			if(dto.getChart9()==2) {
				selectType=" ";
				type=" ";
				selectTypeHts=" ";
				typeHts=" ";
			}
		}
		//Tổng số xét nghiệm từ PE02
		String sql="SELECT COUNT(id) as quantity,YEAR(p.c1) as year "+selectType + " FROM tbl_pe_case p WHERE 1=1 ";
		
		String whereClause=" AND p.c131='YES' AND p.c131_result='answer2' AND p.c13 = 'answer1' ";
		if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c1 >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c1 < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {			
			whereClause+=" AND p.c1_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}else {
				whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}
		}
		
		String groupByClause= " GROUP BY YEAR(p.c1) "+type
							+ " ORDER BY YEAR(p.c1) "+type;
		
		Query q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+groupByClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}else {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}
		}
	
        List<PreventionChartDetailDto> lstFirst = q.list();
        
        
        
        
      //Tổng số xét nghiệm từ HTS
        sql="SELECT COUNT(id) as quantity, YEAR(p.c4_counselling_date) as year " + selectTypeHts+
        		" FROM tbl_hts_case p WHERE 1=1 ";        		
        whereClause=" AND p.c14 IS NOT NULL AND p.c14 = 'answer2' ";
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c4_counselling_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}else {
				whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}
		}
        groupByClause=" GROUP BY YEAR(p.c4_counselling_date) " + typeHts +
        		" ORDER BY YEAR(p.c4_counselling_date) "+typeHts;
        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+groupByClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
        if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}else {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
		}
		
        List<PreventionChartDetailDto> lstHtsFirst = q.list();
        
        sql="SELECT COUNT(id) as quantity, YEAR(p.c4_counselling_date) as year " + selectTypeHts+ 
        		" FROM tbl_hts_case p WHERE 1=1 ";
        whereClause=" AND ( p.c17 = 'answer1' OR p.c17 = 'answer2')  ";
        groupByClause=" GROUP BY YEAR(p.c4_counselling_date) " + typeHts+ 
        				" ORDER BY YEAR(p.c4_counselling_date) " +typeHts;
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c4_counselling_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}else {
				whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}
		}
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+groupByClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}else {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
		}
		
		List<PreventionChartDetailDto> lstHtsSecond = q.list();
		
		sql="SELECT COUNT(id) as quantity, YEAR(p.c4_counselling_date) as year " + selectTypeHts+
        		" FROM tbl_hts_case p WHERE 1=1 ";        		
        whereClause=" AND p.c17 = 'answer1' ";
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c4_counselling_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}else {
				whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}
		}
        groupByClause=" GROUP BY YEAR(p.c4_counselling_date) " + typeHts +
        		" ORDER BY YEAR(p.c4_counselling_date) "+typeHts;
        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+groupByClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
        if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}else {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
		}
		
        List<PreventionChartDetailDto> lstHtsThrid = q.list();
		
		sql="SELECT COUNT(id) as quantity, YEAR(p.c4_counselling_date) as year " + selectTypeHts+ 
        		" FROM tbl_hts_case p WHERE 1=1 ";
        whereClause=" AND p.c14 IS NOT NULL AND p.c14 = 'answer2' AND p.c17 = 'answer1' AND p.c18 != 'answer3' ";
        groupByClause=" GROUP BY YEAR(p.c4_counselling_date) " + typeHts+ 
        				" ORDER BY YEAR(p.c4_counselling_date) " +typeHts;
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c4_counselling_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}else {
				whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}
		}
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+groupByClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}else {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
		}
		
		List<PreventionChartDetailDto> lstHtsFour = q.list();
		
		sql="SELECT COUNT(id) as quantity, YEAR(p.c4_counselling_date) as year " + selectTypeHts+ 
        		" FROM tbl_hts_case p WHERE 1=1 ";
        whereClause=" AND p.c14 IS NOT NULL AND p.c14 = 'answer2' AND p.c17 = 'answer1' AND p.c18 = 'answer2' ";
        groupByClause=" GROUP BY YEAR(p.c4_counselling_date) " + typeHts+ 
        				" ORDER BY YEAR(p.c4_counselling_date) " +typeHts;
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c4_counselling_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}else {
				whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}
		}
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+groupByClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}else {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
		}
		
		List<PreventionChartDetailDto> lstHtsFive = q.list();
        
		if(dto.getModality()!=null && dto.getModality().size()==1) {
			if(dto.getModality().get(0)==1) {
				lstFirst.clear();
				lstFirst.addAll(lstHtsFirst);
			}else {
				lstHtsFirst.clear();
				lstHtsSecond.clear();
				lstHtsThrid.clear();
				lstHtsFour.clear();
				lstHtsFive.clear();
			}
		}else {
			lstFirst.addAll(lstHtsFirst);
		}
		
		
		for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
			String category = String.format("%02d", preventionChartDetailDto.getMonth())+"/"+preventionChartDetailDto.getYear();
			if(dto.getChart9()!=null && dto.getChart9()==2) {
				category = preventionChartDetailDto.getYear().toString();
			}
			if(dto.getChart9()!=null && dto.getChart9()==1) {
				category = String.format("%02d", preventionChartDetailDto.getMonth())+"/"+preventionChartDetailDto.getYear();
			}
			
	    	
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstHtsSecond) {
	    	String category = String.format("%02d", preventionChartDetailDto.getMonth())+"/"+preventionChartDetailDto.getYear();
			if(dto.getChart9()!=null && dto.getChart9()==2) {
				category = preventionChartDetailDto.getYear().toString();
			}
			if(dto.getChart9()!=null && dto.getChart9()==1) {
				category = String.format("%02d", preventionChartDetailDto.getMonth())+"/"+preventionChartDetailDto.getYear();
			}
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstHtsThrid) {
	    	String category = String.format("%02d", preventionChartDetailDto.getMonth())+"/"+preventionChartDetailDto.getYear();
			if(dto.getChart9()!=null && dto.getChart9()==2) {
				category = preventionChartDetailDto.getYear().toString();
			}
			if(dto.getChart9()!=null && dto.getChart9()==1) {
				category = String.format("%02d", preventionChartDetailDto.getMonth())+"/"+preventionChartDetailDto.getYear();
			}
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setThirdQuantity(seri.getThirdQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setThirdQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstHtsFour) {
	    	String category = String.format("%02d", preventionChartDetailDto.getMonth())+"/"+preventionChartDetailDto.getYear();
	    	if(dto.getChart9()!=null && dto.getChart9()==2) {
				category = preventionChartDetailDto.getYear().toString();
			}
			if(dto.getChart9()!=null && dto.getChart9()==1) {
				category = String.format("%02d", preventionChartDetailDto.getMonth())+"/"+preventionChartDetailDto.getYear();
			}
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFourQuantity(seri.getFourQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFourQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstHtsFive) {
	    	String category = String.format("%02d", preventionChartDetailDto.getMonth())+"/"+preventionChartDetailDto.getYear();
	    	if(dto.getChart9()!=null && dto.getChart9()==2) {
				category = preventionChartDetailDto.getYear().toString();
			}
			if(dto.getChart9()!=null && dto.getChart9()==1) {
				category = String.format("%02d", preventionChartDetailDto.getMonth())+"/"+preventionChartDetailDto.getYear();
			}
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFiveQuantity(seri.getFiveQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFiveQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
		return ret;
	}
	@Override
	public PreventionChartDto getChart10(PreventionFilterDto dto) {
		PreventionChartDto ret = new PreventionChartDto();
		ret.setTitle("Chart10");
		String orderType=" INNER JOIN tbl_admin_unit u ON u.id=l.province_id ";
		String selectType=" u.name as title ";
		String whereOrder =" GROUP BY u.name ORDER BY u.name ";
		if(dto.getChart10()==null) {
			orderType=" INNER JOIN tbl_admin_unit u ON u.id=l.province_id ";
			selectType=" u.name as title ";
			whereOrder =" GROUP BY u.name ORDER BY u.name ";
		}else {
			if(dto.getChart10()==0) {
				orderType=" INNER JOIN tbl_admin_unit u ON u.id=l.province_id ";
				selectType=" u.name as title ";
				whereOrder =" GROUP BY u.name ORDER BY u.name ";
			}
			if(dto.getChart10()==1) {
				orderType=" INNER JOIN tbl_admin_unit u ON u.id=l.district_id ";
				selectType=" u.name as title ";
				whereOrder =" GROUP BY u.name ORDER BY u.name ";
			}
			if(dto.getChart10()==2) {
				orderType=" INNER JOIN tbl_admin_unit u ON u.id=l.province_id ";
				selectType=" o.name as title ";
				whereOrder =" GROUP BY o.name ORDER BY o.name ";
			}
		}
		
		
		String whereClause ="";
		
		String sql="SELECT COUNT(p.id) as quantity,"+selectType+ " FROM tbl_pe_case p " + 
				" INNER JOIN tbl_organization_unit o ON p.c1_org_id=o.id" + 
				" INNER JOIN tbl_location l ON l.id = o.address_id" + 
				orderType + 
				" WHERE p.c131='YES' AND  p.c131_result='answer2' " ;
		
		if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c1 >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c1 < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c1_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}else {
				whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}
		}

		Query q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+whereOrder).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
        
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}else {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}
		}
		
		List<PreventionChartDetailDto> lstFirst = q.list();
		
		
        
        
      //Tổng số xét nghiệm từ HTS
        sql="SELECT COUNT(p.id) as quantity,  " +selectType+ 
        		"FROM tbl_hts_case p  " + 
        		"INNER JOIN tbl_organization_unit o ON p.c2_org_id=o.id " + 
        		"INNER JOIN tbl_location l ON l.id = o.address_id " + 
        		orderType + 
        		" WHERE p.c14 = 'answer2' " ;
        whereClause = "";
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c4_counselling_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}else {
				whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}
		}
        
        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+whereOrder).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
        
        if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}else {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
		}
        
        List<PreventionChartDetailDto> lstHtsFirst = q.list();
        
        
        sql="SELECT COUNT(p.id) as quantity,  " + selectType+
        		"FROM tbl_hts_case p  " + 
        		"INNER JOIN tbl_organization_unit o ON p.c2_org_id=o.id " + 
        		"INNER JOIN tbl_location l ON l.id = o.address_id " + 
        		orderType + 
        		" WHERE ( p.c17 = 'answer1' OR p.c17 = 'answer2') " ;
        whereClause = "";
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c4_counselling_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}else {
				whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}
		}
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+whereOrder).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}else {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
		}
		
	
        List<PreventionChartDetailDto> lstHtsSecond = q.list();
        
        
        sql="SELECT COUNT(p.id) as quantity,  " + selectType+
        		"FROM tbl_hts_case p  " + 
        		"INNER JOIN tbl_organization_unit o ON p.c2_org_id=o.id " + 
        		"INNER JOIN tbl_location l ON l.id = o.address_id " + 
        		orderType + 
        		" WHERE  p.c17 = 'answer1' " ;
        whereClause = "";
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c4_counselling_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}else {
				whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}
		}
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+whereOrder).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}else {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
		}
		
	
        List<PreventionChartDetailDto> lstHtsThrid = q.list();
        
        
        sql="SELECT COUNT(p.id) as quantity,  " + selectType+
        		"FROM tbl_hts_case p  " + 
        		"INNER JOIN tbl_organization_unit o ON p.c2_org_id=o.id " + 
        		"INNER JOIN tbl_location l ON l.id = o.address_id " + 
        		orderType + 
        		" WHERE  p.c17 = 'answer1' AND p.c18 != 'answer3' " ;
        whereClause = "";
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c4_counselling_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}else {
				whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}
		}
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+whereOrder).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}else {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
		}
		
	
        List<PreventionChartDetailDto> lstHtsFour = q.list();
        
        sql="SELECT COUNT(p.id) as quantity,  " + selectType+
        		"FROM tbl_hts_case p  " + 
        		"INNER JOIN tbl_organization_unit o ON p.c2_org_id=o.id " + 
        		"INNER JOIN tbl_location l ON l.id = o.address_id " + 
        		orderType + 
        		" WHERE  p.c17 = 'answer1' AND p.c18 = 'answer2' " ;
        whereClause = "";
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c4_counselling_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}else {
				whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}
		}
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+whereOrder).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}else {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
		}
		
	
        List<PreventionChartDetailDto> lstHtsFive = q.list();
        
        
        
        if(dto.getModality()!=null && dto.getModality().size()==1) {
			if(dto.getModality().get(0)==1) {
				lstFirst.clear();
				lstFirst.addAll(lstHtsFirst);
			}else {
				lstHtsFirst.clear();
				lstHtsSecond.clear();
				lstHtsThrid.clear();
				lstHtsFour.clear();
				lstHtsFive.clear();
			}
		}else {
			lstFirst.addAll(lstHtsFirst);
		}
		
		
		for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
			String category = preventionChartDetailDto.getTitle();
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstHtsSecond) {
	    	String category = preventionChartDetailDto.getTitle();
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstHtsThrid) {
	    	String category = preventionChartDetailDto.getTitle();
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setThirdQuantity(seri.getThirdQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setThirdQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstHtsFour) {
	    	String category = preventionChartDetailDto.getTitle();
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFourQuantity(seri.getFourQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFourQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstHtsFive) {
	    	String category = preventionChartDetailDto.getTitle();
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFiveQuantity(seri.getFiveQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFiveQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
		
	    
		return ret;
	}
	@Override
	public PreventionChartDto getChart11(PreventionFilterDto dto) {
		PreventionChartDto ret = new PreventionChartDto();
		ret.setTitle("Chart11");
		String selectType="";
		String type="";
		String selectTypeHts="";
		String typeHts="";
		if(dto.getChart11()==null) {
			selectType=", MONTH(p.c5_date_counselling) as month ";
			type=", MONTH(p.c5_date_counselling) ";
			selectTypeHts=", MONTH(p.c1_received_info_date ) as month ";
			typeHts=", MONTH(p.c1_received_info_date ) ";
		}else {
			if(dto.getChart11()==0) {
				selectType=", MONTH(p.c5_date_counselling) as month ";
				type=", MONTH(p.c5_date_counselling) ";
				selectTypeHts=", MONTH(p.c1_received_info_date ) as month ";
				typeHts=", MONTH(p.c1_received_info_date ) ";
			}
			if(dto.getChart11()==1) {
				selectType=", QUARTER(p.c5_date_counselling) as month ";
				type=", QUARTER(p.c5_date_counselling) ";
				selectTypeHts=", QUARTER(p.c1_received_info_date ) as month ";
				typeHts=", QUARTER(p.c1_received_info_date ) ";
			}
			if(dto.getChart11()==2) {
				selectType=" ";
				type=" ";
				selectTypeHts=" ";
				typeHts=" ";
			}
		}
		
		String sql="SELECT COUNT(id) as quantity,YEAR(p.c5_date_counselling) as year "+selectType + " FROM tbl_pns_case p WHERE 1=1 ";
		
		String whereClause=" ";
		if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c5_date_counselling >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c5_date_counselling < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {			
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		//pns chưa xét mer
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND  (p.c12 =:c12answer1 or p.c12 is null) AND p.c16 !=:b2 ";
//			}else {
//				whereClause+= " AND  ((p.c12 !=:c12answer1 AND p.c12 is not null) OR p.c16 =:b2) ";
//			}
//		}
		
		String groupByClause= " GROUP BY YEAR(p.c5_date_counselling) "+type
							+ " ORDER BY YEAR(p.c5_date_counselling) "+type;
		
		Query q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+groupByClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}else {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}
//		}
	
        List<PreventionChartDetailDto> lstFirst = q.list();
        
        whereClause=" AND p.c6_accept_service = 'YES' ";
		if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c5_date_counselling >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c5_date_counselling < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {			
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		//pns chưa xét mer
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND  (p.c12 =:c12answer1 or p.c12 is null) AND p.c16 !=:b2 ";
//			}else {
//				whereClause+= " AND  ((p.c12 !=:c12answer1 AND p.c12 is not null) OR p.c16 =:b2) ";
//			}
//		}
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+groupByClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}else {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}
//		}
	
        List<PreventionChartDetailDto> lstSecond = q.list();
        
        sql="SELECT COUNT(p.id) as quantity,YEAR(p.c1_received_info_date) as year "+selectTypeHts + " FROM tbl_pns_case_contact p "
        		+ " INNER JOIN tbl_pns_case pc ON pc.id = p.pns_case_id "
        		+ " WHERE 1=1 ";
		
		whereClause=" ";
		if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c1_received_info_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c1_received_info_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {			
			whereClause+=" AND pc.c2_org_id in (:orgIds) ";
		}
		//pns chưa xét mer
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND  (p.c12 =:c12answer1 or p.c12 is null) AND p.c16 !=:b2 ";
//			}else {
//				whereClause+= " AND  ((p.c12 !=:c12answer1 AND p.c12 is not null) OR p.c16 =:b2) ";
//			}
//		}
		
		groupByClause= " GROUP BY YEAR(p.c1_received_info_date) "+typeHts+ 
						" ORDER BY YEAR(p.c1_received_info_date) "+typeHts;
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+groupByClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}else {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}
//		}
	
        List<PreventionChartDetailDto> lstThird = q.list();
        
        whereClause=" AND p.c8_hiv_labtest_status != 'answer3' ";
		if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c1_received_info_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c1_received_info_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {			
			whereClause+=" AND pc.c2_org_id in (:orgIds) ";
		}
		//pns chưa xét mer
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND  (p.c12 =:c12answer1 or p.c12 is null) AND p.c16 !=:b2 ";
//			}else {
//				whereClause+= " AND  ((p.c12 !=:c12answer1 AND p.c12 is not null) OR p.c16 =:b2) ";
//			}
//		}
		
		groupByClause= " GROUP BY YEAR(p.c1_received_info_date) "+typeHts+ 
						" ORDER BY YEAR(p.c1_received_info_date) "+typeHts;
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+groupByClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}else {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}
//		}
	
        List<PreventionChartDetailDto> lstFour = q.list();
        
        whereClause=" AND p.c9 = 'answer2' ";
		if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c1_received_info_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c1_received_info_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {			
			whereClause+=" AND pc.c2_org_id in (:orgIds) ";
		}
		//pns chưa xét mer
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND  (p.c12 =:c12answer1 or p.c12 is null) AND p.c16 !=:b2 ";
//			}else {
//				whereClause+= " AND  ((p.c12 !=:c12answer1 AND p.c12 is not null) OR p.c16 =:b2) ";
//			}
//		}
		
		groupByClause= " GROUP BY YEAR(p.c1_received_info_date) "+typeHts+ 
						" ORDER BY YEAR(p.c1_received_info_date) "+typeHts;
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+groupByClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}else {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}
//		}
	
        List<PreventionChartDetailDto> lstFive = q.list();
        
        whereClause=" AND p.c9_joined_arv = 'YES' ";
		if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c1_received_info_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c1_received_info_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {			
			whereClause+=" AND pc.c2_org_id in (:orgIds) ";
		}
		//pns chưa xét mer
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND  (p.c12 =:c12answer1 or p.c12 is null) AND p.c16 !=:b2 ";
//			}else {
//				whereClause+= " AND  ((p.c12 !=:c12answer1 AND p.c12 is not null) OR p.c16 =:b2) ";
//			}
//		}
		
		groupByClause= " GROUP BY YEAR(p.c1_received_info_date) "+typeHts+ 
						" ORDER BY YEAR(p.c1_received_info_date) "+typeHts;
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+groupByClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}else {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}
//		}
	
        List<PreventionChartDetailDto> lstSix = q.list();
        
        for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
			String category = String.format("%02d", preventionChartDetailDto.getMonth())+"/"+preventionChartDetailDto.getYear();
			if(dto.getChart11()!=null && dto.getChart11()==2) {
				category = preventionChartDetailDto.getYear().toString();
			}
			if(dto.getChart11()!=null && dto.getChart11()==1) {
				category = String.format("%02d", preventionChartDetailDto.getMonth())+"/"+preventionChartDetailDto.getYear();
			}
			
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
	    	String category = String.format("%02d", preventionChartDetailDto.getMonth())+"/"+preventionChartDetailDto.getYear();
			if(dto.getChart11()!=null && dto.getChart11()==2) {
				category = preventionChartDetailDto.getYear().toString();
			}
			if(dto.getChart11()!=null && dto.getChart11()==1) {
				category = String.format("%02d", preventionChartDetailDto.getMonth())+"/"+preventionChartDetailDto.getYear();
			}
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstThird) {
	    	String category = String.format("%02d", preventionChartDetailDto.getMonth())+"/"+preventionChartDetailDto.getYear();
			if(dto.getChart11()!=null && dto.getChart11()==2) {
				category = preventionChartDetailDto.getYear().toString();
			}
			if(dto.getChart11()!=null && dto.getChart11()==1) {
				category = String.format("%02d", preventionChartDetailDto.getMonth())+"/"+preventionChartDetailDto.getYear();
			}
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setThirdQuantity(seri.getThirdQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setThirdQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstFour) {
	    	String category = String.format("%02d", preventionChartDetailDto.getMonth())+"/"+preventionChartDetailDto.getYear();
			if(dto.getChart11()!=null && dto.getChart11()==2) {
				category = preventionChartDetailDto.getYear().toString();
			}
			if(dto.getChart11()!=null && dto.getChart11()==1) {
				category = String.format("%02d", preventionChartDetailDto.getMonth())+"/"+preventionChartDetailDto.getYear();
			}
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFourQuantity(seri.getFourQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFourQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstFive) {
	    	String category = String.format("%02d", preventionChartDetailDto.getMonth())+"/"+preventionChartDetailDto.getYear();
			if(dto.getChart11()!=null && dto.getChart11()==2) {
				category = preventionChartDetailDto.getYear().toString();
			}
			if(dto.getChart11()!=null && dto.getChart11()==1) {
				category = String.format("%02d", preventionChartDetailDto.getMonth())+"/"+preventionChartDetailDto.getYear();
			}
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFiveQuantity(seri.getFiveQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFiveQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstSix) {
	    	String category = String.format("%02d", preventionChartDetailDto.getMonth())+"/"+preventionChartDetailDto.getYear();
			if(dto.getChart11()!=null && dto.getChart11()==2) {
				category = preventionChartDetailDto.getYear().toString();
			}
			if(dto.getChart11()!=null && dto.getChart11()==1) {
				category = String.format("%02d", preventionChartDetailDto.getMonth())+"/"+preventionChartDetailDto.getYear();
			}
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setSixQuantity(seri.getSixQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setSixQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
        
        
		return ret;
	}
	@Override
	public PreventionChartDto getChart12(PreventionFilterDto dto) {
		PreventionChartDto ret = new PreventionChartDto();
		ret.setTitle("Chart12");
		String orderType=" INNER JOIN tbl_admin_unit u ON u.id=l.province_id ";
		String selectType=" u.name as title ";
		String whereOrder =" GROUP BY u.name ORDER BY u.name ";
		if(dto.getChart12()==null) {
			orderType=" INNER JOIN tbl_admin_unit u ON u.id=l.province_id ";
			selectType=" u.name as title ";
			whereOrder =" GROUP BY u.name ORDER BY u.name ";
		}else {
			if(dto.getChart12()==0) {
				orderType=" INNER JOIN tbl_admin_unit u ON u.id=l.province_id ";
				selectType=" u.name as title ";
				whereOrder =" GROUP BY u.name ORDER BY u.name ";
			}
			if(dto.getChart12()==1) {
				orderType=" INNER JOIN tbl_admin_unit u ON u.id=l.district_id ";
				selectType=" u.name as title ";
				whereOrder =" GROUP BY u.name ORDER BY u.name ";
			}
			if(dto.getChart12()==2) {
				orderType=" INNER JOIN tbl_admin_unit u ON u.id=l.province_id ";
				selectType=" o.name as title ";
				whereOrder =" GROUP BY o.name ORDER BY o.name ";
			}
		}
		
		
		
		
		String sql="SELECT COUNT(p.id) as quantity,"+selectType+ " FROM tbl_pns_case p " + 
				" INNER JOIN tbl_organization_unit o ON p.c2_org_id=o.id" + 
				" INNER JOIN tbl_location l ON l.id = o.address_id" + 
				orderType + 
				" WHERE 1=1 " ;
		
		String whereClause ="";
		if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c5_date_counselling >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c5_date_counselling < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {			
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		//pns chưa xét mer
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND  (p.c12 =:c12answer1 or p.c12 is null) AND p.c16 !=:b2 ";
//			}else {
//				whereClause+= " AND  ((p.c12 !=:c12answer1 AND p.c12 is not null) OR p.c16 =:b2) ";
//			}
//		}
		
		
		Query q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+whereOrder).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}else {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}
//		}
		
		List<PreventionChartDetailDto> lstFirst = q.list();
		
		whereClause =" AND c6_accept_service = 'YES' ";
		if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c5_date_counselling >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c5_date_counselling < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {			
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		//pns chưa xét mer
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND  (p.c12 =:c12answer1 or p.c12 is null) AND p.c16 !=:b2 ";
//			}else {
//				whereClause+= " AND  ((p.c12 !=:c12answer1 AND p.c12 is not null) OR p.c16 =:b2) ";
//			}
//		}
		
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+whereOrder).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}else {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}
//		}
		
		List<PreventionChartDetailDto> lstSecond = q.list();
		
		
		sql="SELECT COUNT(p.id) as quantity, "+selectType + " FROM tbl_pns_case_contact p "
        		+ " INNER JOIN tbl_pns_case pc ON pc.id = p.pns_case_id "+
        		" INNER JOIN tbl_organization_unit o ON pc.c2_org_id=o.id" + 
				" INNER JOIN tbl_location l ON l.id = o.address_id" + 
				orderType + 
				" WHERE 1=1 " ;
		
		whereClause=" ";
		if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c1_received_info_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c1_received_info_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {			
			whereClause+=" AND pc.c2_org_id in (:orgIds) ";
		}
		//pns chưa xét mer
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND  (p.c12 =:c12answer1 or p.c12 is null) AND p.c16 !=:b2 ";
//			}else {
//				whereClause+= " AND  ((p.c12 !=:c12answer1 AND p.c12 is not null) OR p.c16 =:b2) ";
//			}
//		}
		
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+whereOrder).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}else {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}
//		}
	
        List<PreventionChartDetailDto> lstThird = q.list();
		
        whereClause=" AND p.c8_hiv_labtest_status != 'answer3' ";
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c1_received_info_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c1_received_info_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {			
			whereClause+=" AND pc.c2_org_id in (:orgIds) ";
		}
		//pns chưa xét mer
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND  (p.c12 =:c12answer1 or p.c12 is null) AND p.c16 !=:b2 ";
//			}else {
//				whereClause+= " AND  ((p.c12 !=:c12answer1 AND p.c12 is not null) OR p.c16 =:b2) ";
//			}
//		}
		
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+whereOrder).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}else {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}
//		}
	
        List<PreventionChartDetailDto> lstFour = q.list();
        
        whereClause=" AND p.c9 = 'answer2' ";
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c1_received_info_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c1_received_info_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {			
			whereClause+=" AND pc.c2_org_id in (:orgIds) ";
		}
		//pns chưa xét mer
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND  (p.c12 =:c12answer1 or p.c12 is null) AND p.c16 !=:b2 ";
//			}else {
//				whereClause+= " AND  ((p.c12 !=:c12answer1 AND p.c12 is not null) OR p.c16 =:b2) ";
//			}
//		}
		
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+whereOrder).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}else {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}
//		}
	
        List<PreventionChartDetailDto> lstFive = q.list();
       
        whereClause=" AND p.c9_joined_arv = 'YES' ";
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c1_received_info_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c1_received_info_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {			
			whereClause+=" AND pc.c2_org_id in (:orgIds) ";
		}
		//pns chưa xét mer
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND  (p.c12 =:c12answer1 or p.c12 is null) AND p.c16 !=:b2 ";
//			}else {
//				whereClause+= " AND  ((p.c12 !=:c12answer1 AND p.c12 is not null) OR p.c16 =:b2) ";
//			}
//		}
		
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+whereOrder).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}else {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}
//		}
	
        List<PreventionChartDetailDto> lstSix = q.list();
		
        for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
        	String category = preventionChartDetailDto.getTitle();
			
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
	    	String category = preventionChartDetailDto.getTitle();
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstThird) {
	    	String category = preventionChartDetailDto.getTitle();
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setThirdQuantity(seri.getThirdQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setThirdQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstFour) {
	    	String category = preventionChartDetailDto.getTitle();
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFourQuantity(seri.getFourQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFourQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstFive) {
	    	String category = preventionChartDetailDto.getTitle();
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFiveQuantity(seri.getFiveQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFiveQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstSix) {
	    	String category = preventionChartDetailDto.getTitle();
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setSixQuantity(seri.getSixQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setSixQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
		
		return ret;
	}
	@Override
	public PreventionChartDto getToltalsSection2(PreventionFilterDto dto) {
		PreventionChartDto ret = new PreventionChartDto();
		ret.setTitle("TotalsSe2");
		
		//Tổng số xét nghiệm từ PE02
		String sql="SELECT COUNT(id) as quantity FROM tbl_pe_case p WHERE 1=1 ";
		
		String whereClause=" AND p.c131='YES' AND p.c131_result='answer2' AND p.c13 = 'answer1' ";
		if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c1 >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c1 < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {			
			whereClause+=" AND p.c1_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}
//			else {
//				whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
//			}
		}
		
		Query q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}
//			else {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("c12answer2",PEC12.answer2.toString());
//				q.setParameter("b1",PEC16.answer1.toString());
//				q.setParameter("b3",PEC16.answer3.toString());
//			}
		}
	
        List<PreventionChartDetailDto> lstFirst = q.list();
        
        
        
        
      //Tổng số xét nghiệm từ HTS
        sql="SELECT COUNT(id) as quantity FROM tbl_hts_case p WHERE 1=1 ";        		
        whereClause=" AND p.c14 IS NOT NULL AND p.c14 = 'answer2' ";
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c15_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c15_date <= :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND p.c15= :c15Yes AND (p.c24 is not null AND p.c24 !=:c24answer2) AND (p.c17 =:c17answer1 or p.c17 =:c17answer2) ";
				//whereClause+= " AND ((p.c24 is not null AND p.c24 !=:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}
//			else {
//				whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
//			}
		}
        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
        if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c15Yes", HTSYesNoNone.YES.toString());
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c17answer1", HTSc17.answer1.toString());
				q.setParameter("c17answer2", HTSc17.answer2.toString());
			}
//			else {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}
		}
		
        List<PreventionChartDetailDto> lstHtsFirst = q.list();
        
        sql="SELECT COUNT(id) as quantity FROM tbl_hts_case p WHERE 1=1 ";
        whereClause=" AND ( p.c17 = 'answer1' OR p.c17 = 'answer2')  ";
        
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c15_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c15_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}
//			else {
//				whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
//			}
		}
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
//			else {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}
		}
		
		List<PreventionChartDetailDto> lstHtsSecond = q.list();
		
		sql="SELECT COUNT(id) as quantity FROM tbl_hts_case p WHERE 1=1 ";        		
        whereClause=" AND p.c17 = 'answer1' ";
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c15_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c15_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND p.c15= :c15Yes AND (p.c24 is not null AND p.c24 !=:c24answer2) AND p.c17 =:c17answer1 ";
				//whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}
//			else {
//				whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
//			}
		}
        
        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
        if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c15Yes", HTSYesNoNone.YES.toString());
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c17answer1", HTSc17.answer1.toString());
			}
//			else {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}
		}
		
        List<PreventionChartDetailDto> lstHtsThrid = q.list();
		
		sql="SELECT COUNT(id) as quantity FROM tbl_hts_case p WHERE 1=1 ";
        whereClause=" AND  p.c17 = 'answer1' AND p.c18 != 'answer3' ";
        
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c15_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c15_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND p.c15= :c15Yes AND (p.c24 is not null AND p.c24 !=:c24answer2) AND p.c17 =:c17answer1 AND (p.c18=:c18answer1 OR p.c18=:c18answer2 )";
				//whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}
//			else {
//				whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
//			}
		}
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c15Yes", HTSYesNoNone.YES.toString());
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c17answer1", HTSc17.answer1.toString());
				q.setParameter("c18answer1", HTSc18.answer1.toString());
				q.setParameter("c18answer2", HTSc18.answer2.toString());
			}
//			else {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}
		}
		
		List<PreventionChartDetailDto> lstHtsFour = q.list();
		
		sql="SELECT COUNT(id) as quantity FROM tbl_hts_case p WHERE 1=1 ";
        whereClause=" AND p.c17 = 'answer1' AND p.c18 = 'answer2' ";
       
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c15_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c15_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND p.c15= :c15Yes AND (p.c24 is not null AND p.c24 !=:c24answer2) ";
				//whereClause+= " AND NOT ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
			}
//			else {
//				whereClause+= " AND ((p.c24 is not null AND p.c24 =:c24answer2) OR (p.c11c is not null AND p.c11c =:c11cYes AND p.c11b is not null AND p.c11b !=:c11bAnswer1)) ";
//			}
		}
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c15Yes", HTSYesNoNone.YES.toString());
				q.setParameter("c24answer2", HTSc24.answer2.toString());

			}
//			else {
//				q.setParameter("c24answer2", HTSc24.answer2.toString());
//				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
//				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
//			}
		}
		
		List<PreventionChartDetailDto> lstHtsFive = q.list();
        
		if(dto.getModality()!=null && dto.getModality().size()==1) {
			if(dto.getModality().get(0)==1) {
				lstFirst.clear();
				lstFirst.addAll(lstHtsFirst);
			}else {
				lstHtsFirst.clear();
				lstHtsSecond.clear();
				lstHtsThrid.clear();
				lstHtsFour.clear();
				lstHtsFive.clear();
			}
		}else {
			lstFirst.addAll(lstHtsFirst);
		}
		
		String category = "totals";
		for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstHtsSecond) {
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstHtsThrid) {
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setThirdQuantity(seri.getThirdQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setThirdQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstHtsFour) {
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFourQuantity(seri.getFourQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFourQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstHtsFive) {
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFiveQuantity(seri.getFiveQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFiveQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
		return ret;
	}
	@Override
	public PreventionChartDto getToltalsSection3(PreventionFilterDto dto) {
		PreventionChartDto ret = new PreventionChartDto();
		ret.setTitle("Section3");
		
		
		String sql="SELECT COUNT(id) as quantity FROM tbl_pns_case p WHERE 1=1 ";
		
		String whereClause=" ";
		if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c5_date_counselling >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c5_date_counselling < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {			
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		//pns chưa xét mer
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND  (p.c12 =:c12answer1 or p.c12 is null) AND p.c16 !=:b2 ";
//			}else {
//				whereClause+= " AND  ((p.c12 !=:c12answer1 AND p.c12 is not null) OR p.c16 =:b2) ";
//			}
//		}
		
		
		
		Query q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}else {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}
//		}
	
        List<PreventionChartDetailDto> lstFirst = q.list();
        
        whereClause=" AND p.c6_accept_service = 'YES' ";
		if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c5_date_counselling >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c5_date_counselling < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {			
			whereClause+=" AND p.c2_org_id in (:orgIds) ";
		}
		//pns chưa xét mer
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND  (p.c12 =:c12answer1 or p.c12 is null) AND p.c16 !=:b2 ";
//			}else {
//				whereClause+= " AND  ((p.c12 !=:c12answer1 AND p.c12 is not null) OR p.c16 =:b2) ";
//			}
//		}
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}else {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}
//		}
	
        List<PreventionChartDetailDto> lstSecond = q.list();
        
        sql="SELECT COUNT(p.id) as quantity FROM tbl_pns_case_contact p "
        		+ " INNER JOIN tbl_pns_case pc ON pc.id = p.pns_case_id "
        		+ " WHERE 1=1 ";
		
		whereClause=" ";
		if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c1_received_info_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c1_received_info_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {			
			whereClause+=" AND pc.c2_org_id in (:orgIds) ";
		}
		//pns chưa xét mer
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND  (p.c12 =:c12answer1 or p.c12 is null) AND p.c16 !=:b2 ";
//			}else {
//				whereClause+= " AND  ((p.c12 !=:c12answer1 AND p.c12 is not null) OR p.c16 =:b2) ";
//			}
//		}
		

		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}else {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}
//		}
	
        List<PreventionChartDetailDto> lstThird = q.list();
        
        whereClause=" AND p.c8_hiv_labtest_status != 'answer3' ";
		if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c1_received_info_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c1_received_info_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {			
			whereClause+=" AND pc.c2_org_id in (:orgIds) ";
		}
		//pns chưa xét mer
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND  (p.c12 =:c12answer1 or p.c12 is null) AND p.c16 !=:b2 ";
//			}else {
//				whereClause+= " AND  ((p.c12 !=:c12answer1 AND p.c12 is not null) OR p.c16 =:b2) ";
//			}
//		}
		
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}else {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}
//		}
	
        List<PreventionChartDetailDto> lstFour = q.list();
        
        whereClause=" AND p.c9 = 'answer2' ";
		if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c1_received_info_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c1_received_info_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {			
			whereClause+=" AND pc.c2_org_id in (:orgIds) ";
		}
		//pns chưa xét mer
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND  (p.c12 =:c12answer1 or p.c12 is null) AND p.c16 !=:b2 ";
//			}else {
//				whereClause+= " AND  ((p.c12 !=:c12answer1 AND p.c12 is not null) OR p.c16 =:b2) ";
//			}
//		}
		
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}else {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}
//		}
	
        List<PreventionChartDetailDto> lstFive = q.list();
        
        whereClause=" AND p.c9_joined_arv = 'YES' ";
		if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c1_received_info_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c1_received_info_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {			
			whereClause+=" AND pc.c2_org_id in (:orgIds) ";
		}
		//pns chưa xét mer
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND  (p.c12 =:c12answer1 or p.c12 is null) AND p.c16 !=:b2 ";
//			}else {
//				whereClause+= " AND  ((p.c12 !=:c12answer1 AND p.c12 is not null) OR p.c16 =:b2) ";
//			}
//		}
		
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}else {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}
//		}
	
        List<PreventionChartDetailDto> lstSix = q.list();

        whereClause=" AND p.c2_hiv_status = 'answer1' ";
		if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c1_received_info_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c1_received_info_date < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND pc.c2_org_id in (:orgIds) ";
		}

		//pns chưa xét mer
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				whereClause+= " AND  (p.c12 =:c12answer1 or p.c12 is null) AND p.c16 !=:b2 ";
//			}else {
//				whereClause+= " AND  ((p.c12 !=:c12answer1 AND p.c12 is not null) OR p.c16 =:b2) ";
//			}
//		}
		
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}
//		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
//			if(dto.getReportTypes().get(0)==1) {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}else {
//				q.setParameter("c12answer1",PEC12.answer1.toString());
//				q.setParameter("b2",PEC16.answer2.toString());
//			}
//		}
	
        List<PreventionChartDetailDto> lstSeven = q.list();

		sql="select count(id) as quantity from (select id,val,c11,c13,c1,c1_org_id,c12,c16,c131,c131_result from ( select  "
			+ "h.id as id,h.c11,h.c1,h.c1_org_id,h.c12,h.c16,h.c13,h.c131,r.val,h.c131_result,r.is_main_risk  from tbl_pe_case_risk_group r "
			+ "inner join tbl_pe_case h on h.id = r.pe_case_id ) as tbl where tbl.is_main_risk=1) as p where 1=1  ";

		whereClause=" AND p.c12 = 'answer1' AND p.c131_result = 'answer2' "
				+ " AND (p.val = 'answer5' OR p.val = 'answer6') ";
		if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c1 >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c1 < :toDate ";
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			whereClause+=" AND p.c1_org_id in (:orgIds) ";
		}

		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));

		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
			q.setParameterList("orgIds",dto.getOrgIds());
		}

        List<PreventionChartDetailDto> lstPeSeven = q.list();
		lstSeven.addAll(lstPeSeven);

        String category = "totals";
        for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
			
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
	    	
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstThird) {
	    	
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setThirdQuantity(seri.getThirdQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setThirdQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstFour) {
	    	
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFourQuantity(seri.getFourQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFourQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstFive) {
	    	
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFiveQuantity(seri.getFiveQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFiveQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstSix) {
	    	
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setSixQuantity(seri.getSixQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setSixQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    
	    for (PreventionChartDetailDto preventionChartDetailDto : lstSeven) {
	    	
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setSevenQuantity(seri.getSevenQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setSevenQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
        
        
        
		return ret;
	}
	@Override
	public PreventionChartDto getChart13(PreventionFilterDto dto) {
		PreventionChartDto ret = new PreventionChartDto();
		ret.setTitle("Chart13");
		String orderType=" INNER JOIN tbl_admin_unit u ON u.id=p.c5_province_id ";
		String orderTypeHTS=" INNER JOIN tbl_admin_unit u ON u.id=p.c23_current_address_province_id ";
		String selectType=" u.code_gso as title ";
		String whereOrder =" GROUP BY u.code_gso ORDER BY u.code_gso ";
		if(dto.getChart13()==null) {
			orderType=" INNER JOIN tbl_admin_unit u ON u.id=p.c5_province_id ";
			orderTypeHTS=" INNER JOIN tbl_admin_unit u ON u.id=p.c23_current_address_province_id ";
		}else {
			if(dto.getChart13()==0) {
				orderType=" INNER JOIN tbl_admin_unit u ON u.id=p.c5_province_id ";
				orderTypeHTS=" INNER JOIN tbl_admin_unit u ON u.id=p.c23_current_address_province_id ";
			}
			if(dto.getChart13()==1) {
				orderType=" INNER JOIN tbl_admin_unit u ON u.id=p.c5_district_id ";
				orderTypeHTS=" INNER JOIN tbl_admin_unit u ON u.id=p.c23_current_address_district_id ";
			}
		}
		
		
		String whereClause ="";
		
		String sql="SELECT COUNT(p.id) as quantity,"+selectType+ " FROM tbl_pe_case p " + 
				orderType + 
				" WHERE p.c131='YES' AND  p.c131_result='answer2' " ;
		
		if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c1 >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c1 < :toDate ";
		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			whereClause+=" AND p.c1_org_id in (:orgIds) ";
//		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}else {
				whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}
		}

		Query q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+whereOrder).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
        
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			q.setParameterList("orgIds",dto.getOrgIds());
//		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}else {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}
		}
		
		List<PreventionChartDetailDto> lstFirst = q.list();
		
		
        
        
      //Tổng số xét nghiệm từ HTS
        sql="SELECT COUNT(p.id) as quantity,  " +selectType+ 
        		"FROM tbl_hts_case p  " + 
        		orderTypeHTS + 
        		" WHERE p.c14 = 'answer2' " ;
        whereClause = "";
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c4_counselling_date < :toDate ";
		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			whereClause+=" AND p.c2_org_id in (:orgIds) ";
//		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND p.c24 !=:c24answer2 AND (p.c11c !=:c11cYes OR p.c11b =:c11bAnswer1) ";
			}else {
				whereClause+= " AND (p.c24 =:c24answer2 OR (p.c11c =:c11cYes AND p.c11b !=:c11bAnswer1)) ";
			}
		}
        
        q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+whereOrder).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
        
        if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			q.setParameterList("orgIds",dto.getOrgIds());
//		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}else {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
		}
        
        List<PreventionChartDetailDto> lstHtsFirst = q.list();
        
        
        sql="SELECT COUNT(p.id) as quantity,  " + selectType+
        		"FROM tbl_hts_case p  " + 
        		orderTypeHTS + 
        		" WHERE ( p.c17 = 'answer1' OR p.c17 = 'answer2') " ;
        whereClause = "";
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c4_counselling_date < :toDate ";
		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			whereClause+=" AND p.c2_org_id in (:orgIds) ";
//		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND p.c24 !=:c24answer2 AND (p.c11c !=:c11cYes OR p.c11b =:c11bAnswer1) ";
			}else {
				whereClause+= " AND (p.c24 =:c24answer2 OR (p.c11c =:c11cYes AND p.c11b !=:c11bAnswer1)) ";
			}
		}
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+whereOrder).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			q.setParameterList("orgIds",dto.getOrgIds());
//		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}else {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
		}
		
	
        List<PreventionChartDetailDto> lstHtsSecond = q.list();
        
        
        sql="SELECT COUNT(p.id) as quantity,  " + selectType+
        		"FROM tbl_hts_case p  " + 
        		orderTypeHTS + 
        		" WHERE  p.c17 = 'answer1' " ;
        whereClause = "";
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c4_counselling_date < :toDate ";
		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			whereClause+=" AND p.c2_org_id in (:orgIds) ";
//		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND p.c24 !=:c24answer2 AND (p.c11c !=:c11cYes OR p.c11b =:c11bAnswer1) ";
			}else {
				whereClause+= " AND (p.c24 =:c24answer2 OR (p.c11c =:c11cYes AND p.c11b !=:c11bAnswer1)) ";
			}
		}
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+whereOrder).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			q.setParameterList("orgIds",dto.getOrgIds());
//		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}else {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
		}
		
	
        List<PreventionChartDetailDto> lstHtsThrid = q.list();
        
        
        sql="SELECT COUNT(p.id) as quantity,  " + selectType+
        		"FROM tbl_hts_case p  " +
        		orderTypeHTS + 
        		" WHERE  p.c17 = 'answer1' AND p.c18 != 'answer3' " ;
        whereClause = "";
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c4_counselling_date < :toDate ";
		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			whereClause+=" AND p.c2_org_id in (:orgIds) ";
//		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND p.c24 !=:c24answer2 AND (p.c11c !=:c11cYes OR p.c11b =:c11bAnswer1) ";
			}else {
				whereClause+= " AND (p.c24 =:c24answer2 OR (p.c11c =:c11cYes AND p.c11b !=:c11bAnswer1)) ";
			}
		}
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+whereOrder).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			q.setParameterList("orgIds",dto.getOrgIds());
//		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}else {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
		}
		
	
        List<PreventionChartDetailDto> lstHtsFour = q.list();
        
        sql="SELECT COUNT(p.id) as quantity,  " + selectType+
        		"FROM tbl_hts_case p  " +
        		orderTypeHTS + 
        		" WHERE  p.c17 = 'answer1' AND p.c18 = 'answer2' " ;
        whereClause = "";
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c4_counselling_date < :toDate ";
		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			whereClause+=" AND p.c2_org_id in (:orgIds) ";
//		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND p.c24 !=:c24answer2 AND (p.c11c !=:c11cYes OR p.c11b =:c11bAnswer1) ";
			}else {
				whereClause+= " AND (p.c24 =:c24answer2 OR (p.c11c =:c11cYes AND p.c11b !=:c11bAnswer1)) ";
			}
		}
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+whereOrder).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			q.setParameterList("orgIds",dto.getOrgIds());
//		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}else {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
		}
		
	
        List<PreventionChartDetailDto> lstHtsFive = q.list();
        
        
        
        if(dto.getModality()!=null && dto.getModality().size()==1) {
			if(dto.getModality().get(0)==1) {
				lstFirst.clear();
				lstFirst.addAll(lstHtsFirst);
			}else {
				lstHtsFirst.clear();
				lstHtsSecond.clear();
				lstHtsThrid.clear();
				lstHtsFour.clear();
				lstHtsFive.clear();
			}
		}else {
			lstFirst.addAll(lstHtsFirst);
		}
		
		
		for (PreventionChartDetailDto preventionChartDetailDto : lstFirst) {
			String category = preventionChartDetailDto.getTitle();
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstHtsSecond) {
	    	String category = preventionChartDetailDto.getTitle();
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setSecondQuantity(seri.getSecondQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setSecondQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstHtsThrid) {
	    	String category = preventionChartDetailDto.getTitle();
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setThirdQuantity(seri.getThirdQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setThirdQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstHtsFour) {
	    	String category = preventionChartDetailDto.getTitle();
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFourQuantity(seri.getFourQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFourQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstHtsFive) {
	    	String category = preventionChartDetailDto.getTitle();
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFiveQuantity(seri.getFiveQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFiveQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
		
	    
		return ret;
	}
	@Override
	public PreventionChartDto getChart14(PreventionFilterDto dto) {
		PreventionChartDto ret = new PreventionChartDto();
		ret.setTitle("XN khẳng định HIV+ theo đơn vị hành chính");
		String orderType=" INNER JOIN tbl_admin_unit u ON u.id=p.c5_province_id ";
		String orderTypeHTS=" INNER JOIN tbl_admin_unit u ON u.id=p.c23_current_address_province_id ";
		String selectType=" u.code_gso as title ";
		String whereOrder =" GROUP BY u.code_gso ORDER BY u.code_gso ";
		if(dto.getChart14()==null) {
			orderType=" INNER JOIN tbl_admin_unit u ON u.id=p.c5_province_id ";
			orderTypeHTS=" INNER JOIN tbl_admin_unit u ON u.id=p.c23_current_address_province_id ";
		}else {
			if(dto.getChart14()==0) {
				orderType=" INNER JOIN tbl_admin_unit u ON u.id=p.c5_province_id ";
				orderTypeHTS=" INNER JOIN tbl_admin_unit u ON u.id=p.c23_current_address_province_id ";
			}
			if(dto.getChart14()==1) {
				orderType=" INNER JOIN tbl_admin_unit u ON u.id=p.c5_district_id ";
				orderTypeHTS=" INNER JOIN tbl_admin_unit u ON u.id=p.c23_current_address_district_id ";
			}
		}
		
		
		String whereClause ="";
		
		String sql = "SELECT COUNT(p.id) as quantity,"+selectType+" FROM tbl_pe_case p " + 
        		orderType + 
        		" WHERE p.c131='YES' AND  p.c131_result='answer2'" ;
        whereClause ="";
        
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c1 >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c1 < :toDate ";
		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			whereClause+=" AND p.c1_org_id in (:orgIds) ";
//		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}else {
				whereClause+= " AND NOT ((p.c12 is not null AND (p.c12 =:c12answer1 OR p.c12 =:c12answer2)) AND (p.c16 is not null AND (p.c16 =:b1 OR p.c16 =:b3))) ";
			}
		}

		Query q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+whereOrder).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
        
        if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			q.setParameterList("orgIds",dto.getOrgIds());
//		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}else {
				q.setParameter("c12answer1",PEC12.answer1.toString());
				q.setParameter("c12answer2",PEC12.answer2.toString());
				q.setParameter("b1",PEC16.answer1.toString());
				q.setParameter("b3",PEC16.answer3.toString());
			}
		}
        List<PreventionChartDetailDto> lstSecond = q.list();
        
      //Tổng số xét nghiệm từ HTS
        
        sql="SELECT COUNT(p.id) as quantity,  " + selectType+
        		"FROM tbl_hts_case p  " + 
        		orderTypeHTS + 
        		" WHERE p.c14 IS NOT NULL AND p.c14 = 'answer2' " ;
        whereClause = "";
        if(dto.getFromDate()!=null) {
			whereClause+=" AND p.c4_counselling_date >=:fromDate ";
		}
		if(dto.getToDate()!=null) {
			whereClause+=" AND p.c4_counselling_date < :toDate ";
		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			whereClause+=" AND p.c2_org_id in (:orgIds) ";
//		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				whereClause+= " AND p.c24 !=:c24answer2 AND (p.c11c !=:c11cYes OR p.c11b =:c11bAnswer1) ";
			}else {
				whereClause+= " AND (p.c24 =:c24answer2 OR (p.c11c =:c11cYes AND p.c11b !=:c11bAnswer1)) ";
			}
		}
		
		q = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+whereOrder).setResultTransformer(new AliasToBeanResultTransformer(PreventionChartDetailDto.class));
		
		if(dto.getFromDate()!=null) {
			q.setParameter("fromDate",Date.from(dto.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(dto.getToDate()!=null) {
			q.setParameter("toDate",Date.from(dto.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
//		if(dto.getOrgIds()!=null && dto.getOrgIds().size()>0) {
//			q.setParameterList("orgIds",dto.getOrgIds());
//		}
		if(dto.getReportTypes()!=null && dto.getReportTypes().size()==1) {
			if(dto.getReportTypes().get(0)==1) {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}else {
				q.setParameter("c24answer2", HTSc24.answer2.toString());
				q.setParameter("c11cYes", HTSYesNoNone.YES.toString());
				q.setParameter("c11bAnswer1", HTSc11b.answer1.toString());
			}
		}
		
	
        List<PreventionChartDetailDto> lstHtsSecond = q.list();
        
        
        if(dto.getModality()!=null && dto.getModality().size()==1) {
			if(dto.getModality().get(0)==1) {
				lstSecond.clear();
				lstSecond.addAll(lstHtsSecond);
			}else {
				
			}
		}else {
			lstSecond.addAll(lstHtsSecond);
		}
	    for (PreventionChartDetailDto preventionChartDetailDto : lstSecond) {
	    	String category = preventionChartDetailDto.getTitle();
			if(ret.getSeries().get(category)!=null) {
				PreventionChartSeriesDto seri = ret.getSeries().get(category);
				seri.setFirstQuantity(seri.getFirstQuantity().add(preventionChartDetailDto.getQuantity()));
			}
			else {
				PreventionChartSeriesDto seri = new PreventionChartSeriesDto();
				seri.setFirstQuantity(preventionChartDetailDto.getQuantity());
				ret.getSeries().put(category, seri);
			}
		}
		return ret;
	}

	@Override
	public Workbook exportMERReport(PreventionFilterDto filter) {
		Workbook blankBook = new XSSFWorkbook();
		blankBook.createSheet();
		if (filter.getProvinceId() != null && CollectionUtils.isEmpty(filter.getOrgIds())) {
			filter.setOrgIds(organizationRepository.findAllIdByProvinceId(filter.getProvinceId()));
		}
		XSSFWorkbook wbook = null;
		try (InputStream template = context.getResource("classpath:templates/Template20211228.xlsx").getInputStream()) {
			wbook = new XSSFWorkbook(template);
		} catch (IOException e) {
			System.out.println("Lỗi đọc file excel template Template20211228.xlsx");
		}
		if (wbook == null) {
			return blankBook;
		}
		wbook = (XSSFWorkbook) htsCaseService.exportHTSIndex(filter,wbook);
		wbook = (XSSFWorkbook) htsCaseService.exportHTSTST(filter,wbook);
		wbook = (XSSFWorkbook) selfTestEntryService.exportReportSelfTestNew(filter,wbook);
		wbook = (XSSFWorkbook) htsCaseService.exportHTSRecent(filter,wbook);

		return wbook;
	}

	@Override
	public Workbook exportMERPEReport(PreventionFilterDto filter) {
		Workbook blankBook = new XSSFWorkbook();
		blankBook.createSheet();
		if (filter.getProvinceId() != null && CollectionUtils.isEmpty(filter.getOrgIds())) {
			filter.setOrgIds(organizationRepository.findAllIdByProvinceId(filter.getProvinceId()));
		}
		XSSFWorkbook wbook = null;
		try (InputStream template = context.getResource("classpath:templates/reportMERPE.xlsx").getInputStream()) {
			wbook = new XSSFWorkbook(template);
		} catch (IOException e) {
			System.out.println("Lỗi đọc file excel template reportMERPE.xlsx");
		}
		if (wbook == null) {
			return blankBook;
		}
		List<ReportMERPEDto> rs = peCaseService.getDataReportMERPE(filter);

		int rowIndex = 5;
		int colIndex = 0;

		Row row = null;
		Cell cell = null;
		Sheet sheet = wbook.getSheetAt(0);
		CellStyle cellStyle = wbook.createCellStyle();
		ExcelUtils.setBorders4Style(cellStyle);
		cellStyle.setWrapText(true);
		cellStyle.setAlignment(HorizontalAlignment.RIGHT);
		cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

		for(ReportMERPEDto dto : rs){
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
					cell.setCellValue(dto.getSupport());
				} catch (Exception e) {
					cell.setCellValue("");
				}

				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getPwid().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}

				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getMsm().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}
				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getTg().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}

				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getFsw().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}

				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getOther().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}

				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getPwidC8Positives().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}

				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getMsmC8Positives().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}
				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getTgC8Positives().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}

				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getFswC8Positives().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}

				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getOtherC8Positives().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}

				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getPwidC11Yes().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}

				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getMsmC11Yes().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}
				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getTgC11Yes().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}

				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getFswC11Yes().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}

				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getOtherC11Yes().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}

				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getPwidC11No().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}

				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getMsmC11No().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}
				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getTgC11No().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}

				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getFswC11No().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}

				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getOtherC11No().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}

				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getTotalKPPREV().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}

				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getTestingC8Positives().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}

				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getTestingC11Yes().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}

				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getTestingC11No().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}

				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getTestingNotRequired().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}

				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getPriorityClientOfSex().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}

				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getPriorityMilitary().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}

				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getPriorityMobile().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}

				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getPriorityNonInjectDrug().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}

				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getPriorityOther().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}

				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getFu().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}
				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getF10().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}
				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getF15().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}
				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getF20().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}
				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getF25().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}
				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getF30().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}
				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getF35().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}
				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getF40().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}
				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getF45().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}
				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getF50().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}

				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getMu().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}
				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getM10().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}
				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getM15().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}
				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getM20().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}
				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getM25().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}
				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getM30().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}
				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getM35().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}
				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getM40().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}
				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getM45().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}
				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getM50().intValue());
				} catch (Exception e) {
					cell.setCellValue("");
				}
				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				try {
					cell.setCellValue(dto.getTotalPPPREV());
				} catch (Exception e) {
					cell.setCellValue("");
				}
			}
		}
		return wbook;
	}
}
