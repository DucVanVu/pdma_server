package org.pepfar.pdma.app.data.service.jpa;

import org.hibernate.Session;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.hibernate.transform.Transformers;
import org.pepfar.pdma.app.data.dto.*;
import org.pepfar.pdma.app.data.service.ReportMerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service("ReportMerServiceImpl")
public class ReportMerServiceImpl implements ReportMerService {

	@Autowired
	public EntityManager manager;

	@Override
	public List<HTSRecentDto> getDataFacilitySNSRTRIRecent(PreventionFilterDto filter) {
		String whereClause = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			whereClause += " AND tbl.c2_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			whereClause += " AND tbl.c15_date >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			whereClause += "  AND tbl.c15_date <=:toDate ";
		}
		String sql = "select tbl.c2_org_id as orgId, tou.code as orgCode , tou.name as  orgName, pro.name as provinceName, dis.name as districtName,'Facility SNS' as modality,"
				+ " count(if(tbl.val = 'answer1',1,null)) as pwidRTRI,"
				+ " count(if(tbl.val = 'answer2',1,null)) as msmRTRI,"
				+ " count(if(tbl.val = 'answer4',1,null)) as tgRTRI,"
				+ " count(if(tbl.val = 'answer3',1,null)) as fswRTRI,"
				+ " count(if(tbl.val = 'answer14',1,null)) as otherRTRI,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and tbl.c8_dob is null,1,null)) as fuRTRI,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=15) and (year(curdate())-year(tbl.c8_dob)<20),1,null)) as f15RTRI,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=20) and (year(curdate())-year(tbl.c8_dob)<25),1,null)) as f20RTRI,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=25) and (year(curdate())-year(tbl.c8_dob)<30),1,null)) as f25RTRI,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=30) and (year(curdate())-year(tbl.c8_dob)<35),1,null)) as f30RTRI,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=35) and (year(curdate())-year(tbl.c8_dob)<40),1,null)) as f35RTRI,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=40) and (year(curdate())-year(tbl.c8_dob)<45),1,null)) as f40RTRI,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=45) and (year(curdate())-year(tbl.c8_dob)<50),1,null)) as f45RTRI,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=50),1,null)) as f50RTRI,"
				+ " count(if(tbl.c7_gender = 'MALE'  and tbl.c8_dob is null,1,null)) as muRTRI,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=15) and (year(curdate())-year(tbl.c8_dob)<20),1,null)) as m15RTRI,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=20) and (year(curdate())-year(tbl.c8_dob)<25),1,null)) as m20RTRI,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=25) and (year(curdate())-year(tbl.c8_dob)<30),1,null)) as m25RTRI,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=30) and (year(curdate())-year(tbl.c8_dob)<35),1,null)) as m30RTRI,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=35) and (year(curdate())-year(tbl.c8_dob)<40),1,null)) as m35RTRI,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=40) and (year(curdate())-year(tbl.c8_dob)<45),1,null)) as m40RTRI,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=45) and (year(curdate())-year(tbl.c8_dob)<50),1,null)) as m45RTRI,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=50),1,null)) as m50RTRI"
				+ " from (select thcrg.val, thc.* from tbl_hts_case_risk_group thcrg"
				+ "  inner join tbl_hts_case thc on thcrg.hts_case_id = thc.id"
				+ "      AND thcrg.is_main_risk = 1 ) as tbl"
				+ " inner join tbl_organization_unit tou on tbl.c2_org_id = tou.id"
				+ " inner join tbl_location tl on tou.address_id = tl.id"
				+ " inner join tbl_admin_unit pro on tl.province_id = pro.id"
				+ " inner join tbl_admin_unit dis on tl.district_id = dis.id"
				+ " where 1=1 "
				+ " and (tbl.c24 is not null AND tbl.c24 = 'answer1' or tbl.c24 = 'answer3') "
				+ " and tbl.c5='answer3' "
				+ " and not ( tbl.id  IN (SELECT DISTINCT(hts_case_id) from tbl_hts_case_risk_group thcrg WHERE thcrg.val='answer5' or thcrg.val='answer6') "
				+ " or tbl.c10 = 'answer2') "
				+ " and tbl.c15 = 'YES' "
				+ " and tbl.c17 = 'answer1' " 
				+ whereClause
				+ " group by orgId,orgCode,orgName,provinceName,districtName,modality";
		org.hibernate.Query query = manager.unwrap(Session.class).createSQLQuery(sql)
				.setResultTransformer(new AliasToBeanResultTransformer(HTSRecentDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			query.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			query.setParameter("fromDate", Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			query.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		List<HTSRecentDto> ret = query.list();
		return ret;
	}

	@Override
	public List<HTSRecentDto> getDataFacilitySNSRTRILongTerm(PreventionFilterDto filter) {
		String whereClause = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			whereClause += " AND tbl.c2_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			whereClause += " AND tbl.c15_date >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			whereClause += "  AND tbl.c15_date <=:toDate ";
		}
		String sql = "select tbl.c2_org_id as orgId, tou.code as orgCode , tou.name as  orgName, pro.name as provinceName, dis.name as districtName,'Facility SNS' as modality,"
				+ " count(if(tbl.val = 'answer1',1,null)) as pwidRTRILongTerm,"
				+ " count(if(tbl.val = 'answer2',1,null)) as msmRTRILongTerm,"
				+ " count(if(tbl.val = 'answer4',1,null)) as tgRTRILongTerm,"
				+ " count(if(tbl.val = 'answer3',1,null)) as fswRTRILongTerm,"
				+ " count(if(tbl.val = 'answer14',1,null)) as otherRTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and tbl.c8_dob is null,1,null)) as fuRTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=15) and (year(curdate())-year(tbl.c8_dob)<20),1,null)) as f15RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=20) and (year(curdate())-year(tbl.c8_dob)<25),1,null)) as f20RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=25) and (year(curdate())-year(tbl.c8_dob)<30),1,null)) as f25RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=30) and (year(curdate())-year(tbl.c8_dob)<35),1,null)) as f30RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=35) and (year(curdate())-year(tbl.c8_dob)<40),1,null)) as f35RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=40) and (year(curdate())-year(tbl.c8_dob)<45),1,null)) as f40RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=45) and (year(curdate())-year(tbl.c8_dob)<50),1,null)) as f45RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=50),1,null)) as f50RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and tbl.c8_dob is null,1,null)) as muRTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=15) and (year(curdate())-year(tbl.c8_dob)<20),1,null)) as m15RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=20) and (year(curdate())-year(tbl.c8_dob)<25),1,null)) as m20RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=25) and (year(curdate())-year(tbl.c8_dob)<30),1,null)) as m25RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=30) and (year(curdate())-year(tbl.c8_dob)<35),1,null)) as m30RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=35) and (year(curdate())-year(tbl.c8_dob)<40),1,null)) as m35RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=40) and (year(curdate())-year(tbl.c8_dob)<45),1,null)) as m40RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=45) and (year(curdate())-year(tbl.c8_dob)<50),1,null)) as m45RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=50),1,null)) as m50RTRILongTerm"
				+ " from (select thcrg.val , thc.* from tbl_hts_case_risk_group thcrg"
				+ "      inner join tbl_hts_case thc on thcrg.hts_case_id = thc.id"
				+ "      where thcrg.is_main_risk = 1 ) as tbl"
				+ "   inner join tbl_organization_unit tou on tbl.c2_org_id = tou.id"
				+ "   inner join tbl_location tl on tou.address_id = tl.id"
				+ "   inner join tbl_admin_unit pro on tl.province_id = pro.id"
				+ "   inner join tbl_admin_unit dis on tl.district_id = dis.id"
				+ " where 1=1 "
				+ " and (tbl.c24 is not null AND tbl.c24 = 'answer1' or tbl.c24 = 'answer3') "
				+ " and tbl.c5='answer3' "
				+ " and not ( tbl.id  IN (SELECT DISTINCT(hts_case_id) from tbl_hts_case_risk_group thcrg WHERE thcrg.val='answer5' or thcrg.val='answer6') "
				+ " or tbl.c10 = 'answer2') "
				+ " and tbl.c15 = 'YES' "
				+ "and tbl.c17 = 'answer2' " 
				+ whereClause
				+ " group by orgId, orgCode,orgName,provinceName,districtName,modality";
		org.hibernate.Query query = manager.unwrap(Session.class).createSQLQuery(sql)
				.setResultTransformer(new AliasToBeanResultTransformer(HTSRecentDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			query.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			query.setParameter("fromDate", Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			query.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		List<HTSRecentDto> ret = query.list();
		return ret;
	}

	@Override
	public List<HTSRecentDto> getDataFacilitySNSRITARecent(PreventionFilterDto filter) {
		String whereClause = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			whereClause += " AND tbl.c2_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			whereClause += " AND tbl.c15_date >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			whereClause += "  AND tbl.c15_date <=:toDate ";
		}
		String sql = "select tbl.c2_org_id as orgId, tou.code as orgCode , tou.name as  orgName, pro.name as provinceName, dis.name as districtName,'Facility SNS' as modality,"
				+ " count(if(tbl.val = 'answer1',1,null)) as pwidRITA,"
				+ " count(if(tbl.val = 'answer2',1,null)) as msmRITA,"
				+ " count(if(tbl.val = 'answer4',1,null)) as tgRITA,"
				+ " count(if(tbl.val = 'answer3',1,null)) as fswRITA,"
				+ " count(if(tbl.val = 'answer14',1,null)) as otherRITA,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and tbl.c8_dob is null,1,null)) as fuRITA,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=15) and (year(curdate())-year(tbl.c8_dob)<20),1,null)) as f15RITA,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=20) and (year(curdate())-year(tbl.c8_dob)<25),1,null)) as f20RITA,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=25) and (year(curdate())-year(tbl.c8_dob)<30),1,null)) as f25RITA,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=30) and (year(curdate())-year(tbl.c8_dob)<35),1,null)) as f30RITA,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=35) and (year(curdate())-year(tbl.c8_dob)<40),1,null)) as f35RITA,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=40) and (year(curdate())-year(tbl.c8_dob)<45),1,null)) as f40RITA,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=45) and (year(curdate())-year(tbl.c8_dob)<50),1,null)) as f45RITA,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=50),1,null)) as f50RITA,"
				+ " count(if(tbl.c7_gender = 'MALE'  and tbl.c8_dob is null,1,null)) as muRITA,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=15) and (year(curdate())-year(tbl.c8_dob)<20),1,null)) as m15RITA,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=20) and (year(curdate())-year(tbl.c8_dob)<25),1,null)) as m20RITA,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=25) and (year(curdate())-year(tbl.c8_dob)<30),1,null)) as m25RITA,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=30) and (year(curdate())-year(tbl.c8_dob)<35),1,null)) as m30RITA,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=35) and (year(curdate())-year(tbl.c8_dob)<40),1,null)) as m35RITA,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=40) and (year(curdate())-year(tbl.c8_dob)<45),1,null)) as m40RITA,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=45) and (year(curdate())-year(tbl.c8_dob)<50),1,null)) as m45RITA,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=50),1,null)) as m50RITA"
				+ " from (select thcrg.val, thc.* from tbl_hts_case_risk_group thcrg"
				+ "  inner join tbl_hts_case thc on thcrg.hts_case_id = thc.id"
				+ "      AND thcrg.is_main_risk = 1 ) as tbl"
				+ "   inner join tbl_organization_unit tou on tbl.c2_org_id = tou.id"
				+ "   inner join tbl_location tl on tou.address_id = tl.id"
				+ "   inner join tbl_admin_unit pro on tl.province_id = pro.id"
				+ "   inner join tbl_admin_unit dis on tl.district_id = dis.id"
				+ " where 1=1 "
				+ " and (tbl.c24 is not null AND tbl.c24 = 'answer1' or tbl.c24 = 'answer3') "
				+ " and tbl.c5='answer3' "
				+ " and not ( tbl.id  IN (SELECT DISTINCT(hts_case_id) from tbl_hts_case_risk_group thcrg WHERE thcrg.val='answer5' or thcrg.val='answer6') "
				+ " or tbl.c10 = 'answer2')"
				+ " and tbl.c15 = 'YES' "
				+ " and tbl.c17 = 'answer1' "
				+ " and tbl.c18 = 'answer2' " 
				+ whereClause
				+ " group by orgId,orgCode,orgName,provinceName,districtName,modality";
		org.hibernate.Query query = manager.unwrap(Session.class).createSQLQuery(sql)
				.setResultTransformer(new AliasToBeanResultTransformer(HTSRecentDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			query.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			query.setParameter("fromDate", Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			query.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		List<HTSRecentDto> ret = query.list();
		return ret;
	}

	@Override
	public List<HTSRecentDto> getDataFacilitySNSRITALongTerm(PreventionFilterDto filter) {
		String whereClause = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			whereClause += " AND tbl.c2_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			whereClause += " AND tbl.c15_date >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			whereClause += "  AND tbl.c15_date <=:toDate ";
		}
		String sql = "select tbl.c2_org_id as orgId, tou.code as orgCode , tou.name as  orgName, pro.name as provinceName, dis.name as districtName,'Facility SNS' as modality,"
				+ " count(if(tbl.val = 'answer1',1,null)) as pwidRITALongTerm,"
				+ " count(if(tbl.val = 'answer2',1,null)) as msmRITALongTerm,"
				+ " count(if(tbl.val = 'answer4',1,null)) as tgRITALongTerm,"
				+ " count(if(tbl.val = 'answer3',1,null)) as fswRITALongTerm,"
				+ " count(if(tbl.val = 'answer14',1,null)) as otherRITALongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and tbl.c8_dob is null,1,null)) as fuRITALongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=15) and (year(curdate())-year(tbl.c8_dob)<20),1,null)) as f15RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=20) and (year(curdate())-year(tbl.c8_dob)<25),1,null)) as f20RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=25) and (year(curdate())-year(tbl.c8_dob)<30),1,null)) as f25RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=30) and (year(curdate())-year(tbl.c8_dob)<35),1,null)) as f30RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=35) and (year(curdate())-year(tbl.c8_dob)<40),1,null)) as f35RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=40) and (year(curdate())-year(tbl.c8_dob)<45),1,null)) as f40RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=45) and (year(curdate())-year(tbl.c8_dob)<50),1,null)) as f45RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=50),1,null)) as f50RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and tbl.c8_dob is null,1,null)) as muRITALongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=15) and (year(curdate())-year(tbl.c8_dob)<20),1,null)) as m15RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=20) and (year(curdate())-year(tbl.c8_dob)<25),1,null)) as m20RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=25) and (year(curdate())-year(tbl.c8_dob)<30),1,null)) as m25RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=30) and (year(curdate())-year(tbl.c8_dob)<35),1,null)) as m30RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=35) and (year(curdate())-year(tbl.c8_dob)<40),1,null)) as m35RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=40) and (year(curdate())-year(tbl.c8_dob)<45),1,null)) as m40RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=45) and (year(curdate())-year(tbl.c8_dob)<50),1,null)) as m45RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=50),1,null)) as m50RITALongTerm"
				+ " from (select thcrg.val , thc.* from tbl_hts_case_risk_group thcrg"
				+ "  inner join tbl_hts_case thc on thcrg.hts_case_id = thc.id"
				+ "      		AND thcrg.is_main_risk = 1 ) as tbl"
				+ "   inner join tbl_organization_unit tou on tbl.c2_org_id = tou.id"
				+ "   inner join tbl_location tl on tou.address_id = tl.id"
				+ "   inner join tbl_admin_unit pro on tl.province_id = pro.id"
				+ "   inner join tbl_admin_unit dis on tl.district_id = dis.id"
				+ " where 1=1 "
				+ " and (tbl.c24 is not null AND tbl.c24 = 'answer1' or tbl.c24 = 'answer3') "
				+ " and tbl.c5='answer3' "
				+ " and not ( tbl.id  IN (SELECT DISTINCT(hts_case_id) from tbl_hts_case_risk_group thcrg WHERE thcrg.val='answer5' or thcrg.val='answer6') "
				+ " or tbl.c10 = 'answer2' )"
				+ " and tbl.c15 = 'YES' "
				+ " and tbl.c17 = 'answer1' "
				+ " and tbl.c18 = 'answer1' " 
				+ whereClause
				+ " group by orgId, orgCode,orgName,provinceName,districtName,modality";
		org.hibernate.Query query = manager.unwrap(Session.class).createSQLQuery(sql)
				.setResultTransformer(new AliasToBeanResultTransformer(HTSRecentDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			query.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			query.setParameter("fromDate", Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			query.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		List<HTSRecentDto> ret = query.list();
		return ret;
	}

	@Override
	public List<HTSRecentDto> getDataVCTRTRIRecent(PreventionFilterDto filter) {
		String whereClause = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			whereClause += " AND tbl.c2_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			whereClause += " AND tbl.c15_date >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			whereClause += "  AND tbl.c15_date <=:toDate ";
		}
		String sql = "select tbl.c2_org_id as orgId, tou.code as orgCode , tou.name as  orgName, pro.name as provinceName, dis.name as districtName,'VCT - Integrated' as modality,"
				+ " count(if(tbl.val = 'answer1',1,null)) as pwidRTRI,"
				+ " count(if(tbl.val = 'answer2',1,null)) as msmRTRI,"
				+ " count(if(tbl.val = 'answer4',1,null)) as tgRTRI,"
				+ " count(if(tbl.val = 'answer3',1,null)) as fswRTRI,"
				+ " count(if(tbl.val = 'answer14',1,null)) as otherRTRI,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and tbl.c8_dob is null,1,null)) as fuRTRI,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=15) and (year(curdate())-year(tbl.c8_dob)<20),1,null)) as f15RTRI,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=20) and (year(curdate())-year(tbl.c8_dob)<25),1,null)) as f20RTRI,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=25) and (year(curdate())-year(tbl.c8_dob)<30),1,null)) as f25RTRI,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=30) and (year(curdate())-year(tbl.c8_dob)<35),1,null)) as f30RTRI,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=35) and (year(curdate())-year(tbl.c8_dob)<40),1,null)) as f35RTRI,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=40) and (year(curdate())-year(tbl.c8_dob)<45),1,null)) as f40RTRI,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=45) and (year(curdate())-year(tbl.c8_dob)<50),1,null)) as f45RTRI,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=50),1,null)) as f50RTRI,"
				+ " count(if(tbl.c7_gender = 'MALE'  and tbl.c8_dob is null,1,null)) as muRTRI,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=15) and (year(curdate())-year(tbl.c8_dob)<20),1,null)) as m15RTRI,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=20) and (year(curdate())-year(tbl.c8_dob)<25),1,null)) as m20RTRI,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=25) and (year(curdate())-year(tbl.c8_dob)<30),1,null)) as m25RTRI,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=30) and (year(curdate())-year(tbl.c8_dob)<35),1,null)) as m30RTRI,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=35) and (year(curdate())-year(tbl.c8_dob)<40),1,null)) as m35RTRI,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=40) and (year(curdate())-year(tbl.c8_dob)<45),1,null)) as m40RTRI,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=45) and (year(curdate())-year(tbl.c8_dob)<50),1,null)) as m45RTRI,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=50),1,null)) as m50RTRI"
				+ " from (select thcrg.val, thc.* from tbl_hts_case_risk_group thcrg"
				+ "  inner join tbl_hts_case thc on thcrg.hts_case_id = thc.id"
				+ "      where thcrg.is_main_risk = 1 ) as tbl"
				+ "    inner join tbl_organization_unit tou on tbl.c2_org_id = tou.id"
				+ " inner join tbl_location tl on tou.address_id = tl.id"
				+ " inner join tbl_admin_unit pro on tl.province_id = pro.id"
				+ " inner join tbl_admin_unit dis on tl.district_id = dis.id"
				+ " where 1=1 "
				+ " and (tbl.c24 is not null AND (tbl.c24 = 'answer1' or tbl.c24 = 'answer3')) "
				+ " and (tbl.c5='answer1' OR tbl.c5='answer2') "
				+ " and not ( tbl.id  IN (SELECT DISTINCT(hts_case_id) from tbl_hts_case_risk_group thcrg WHERE thcrg.val='answer5' or thcrg.val='answer6') "
				+ " or tbl.c10 = 'answer2')"
				+ " and tbl.c15 = 'YES' "
				+ " and tbl.c17 = 'answer1' " 
				+ whereClause
				+ " group by orgId, orgCode,orgName,provinceName,districtName,modality";
		org.hibernate.Query query = manager.unwrap(Session.class).createSQLQuery(sql)
				.setResultTransformer(new AliasToBeanResultTransformer(HTSRecentDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			query.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			query.setParameter("fromDate", Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			query.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		List<HTSRecentDto> ret = query.list();
		return ret;
	}

	@Override
	public List<HTSRecentDto> getDataVCTRTRILongTerm(PreventionFilterDto filter) {
		String whereClause = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			whereClause += " AND tbl.c2_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			whereClause += " AND tbl.c15_date >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			whereClause += "  AND tbl.c15_date <=:toDate ";
		}
		String sql = "select tbl.c2_org_id as orgId,tou.code as orgCode , tou.name as  orgName, pro.name as provinceName, dis.name as districtName,'VCT - Integrated' as modality,"
				+ " count(if(tbl.val = 'answer1',1,null)) as pwidRTRILongTerm,"
				+ " count(if(tbl.val = 'answer2',1,null)) as msmRTRILongTerm,"
				+ " count(if(tbl.val = 'answer4',1,null)) as tgRTRILongTerm,"
				+ " count(if(tbl.val = 'answer3',1,null)) as fswRTRILongTerm,"
				+ " count(if(tbl.val = 'answer14',1,null)) as otherRTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and tbl.c8_dob is null,1,null)) as fuRTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=15) and (year(curdate())-year(tbl.c8_dob)<20),1,null)) as f15RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=20) and (year(curdate())-year(tbl.c8_dob)<25),1,null)) as f20RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=25) and (year(curdate())-year(tbl.c8_dob)<30),1,null)) as f25RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=30) and (year(curdate())-year(tbl.c8_dob)<35),1,null)) as f30RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=35) and (year(curdate())-year(tbl.c8_dob)<40),1,null)) as f35RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=40) and (year(curdate())-year(tbl.c8_dob)<45),1,null)) as f40RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=45) and (year(curdate())-year(tbl.c8_dob)<50),1,null)) as f45RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=50),1,null)) as f50RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and tbl.c8_dob is null,1,null)) as muRTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=15) and (year(curdate())-year(tbl.c8_dob)<20),1,null)) as m15RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=20) and (year(curdate())-year(tbl.c8_dob)<25),1,null)) as m20RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=25) and (year(curdate())-year(tbl.c8_dob)<30),1,null)) as m25RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=30) and (year(curdate())-year(tbl.c8_dob)<35),1,null)) as m30RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=35) and (year(curdate())-year(tbl.c8_dob)<40),1,null)) as m35RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=40) and (year(curdate())-year(tbl.c8_dob)<45),1,null)) as m40RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=45) and (year(curdate())-year(tbl.c8_dob)<50),1,null)) as m45RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=50),1,null)) as m50RTRILongTerm"
				+ " from (select thcrg.val, thc.* from tbl_hts_case_risk_group thcrg"
				+ " inner join tbl_hts_case thc on thcrg.hts_case_id = thc.id"
				+ "      where thcrg.is_main_risk = 1 ) as tbl"
				+ "   inner join tbl_organization_unit tou on tbl.c2_org_id = tou.id"
				+ "   inner join tbl_location tl on tou.address_id = tl.id"
				+ "   inner join tbl_admin_unit pro on tl.province_id = pro.id"
				+ "   inner join tbl_admin_unit dis on tl.district_id = dis.id"
				+ " where 1=1 "
				+ " and (tbl.c24 is not null AND (tbl.c24 = 'answer1' or tbl.c24 = 'answer3')) "
				+ " and (tbl.c5='answer1' OR tbl.c5='answer2') "
				+ " and not ( tbl.id  IN (SELECT DISTINCT(hts_case_id) from tbl_hts_case_risk_group thcrg WHERE thcrg.val='answer5' or thcrg.val='answer6') "
				+ " or tbl.c10 = 'answer2')"
				+ " and tbl.c15 = 'YES' "
				+ " and tbl.c17 = 'answer2' " 
				+ whereClause
				+ " group by orgId, orgCode,orgName,provinceName,districtName,modality";
		org.hibernate.Query query = manager.unwrap(Session.class).createSQLQuery(sql)
				.setResultTransformer(new AliasToBeanResultTransformer(HTSRecentDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			query.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			query.setParameter("fromDate", Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			query.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		List<HTSRecentDto> ret = query.list();
		return ret;
	}

	@Override
	public List<HTSRecentDto> getDataVCTRITARecent(PreventionFilterDto filter) {
		String whereClause = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			whereClause += " AND tbl.c2_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			whereClause += " AND tbl.c15_date >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			whereClause += "  AND tbl.c15_date <=:toDate ";
		}
		String sql = "select tbl.c2_org_id as orgId,tou.code as orgCode , tou.name as  orgName, pro.name as provinceName, dis.name as districtName,'VCT - Integrated' as modality,"
				+ " count(if(tbl.val = 'answer1',1,null)) as pwidRITA,"
				+ " count(if(tbl.val = 'answer2',1,null)) as msmRITA,"
				+ " count(if(tbl.val = 'answer4',1,null)) as tgRITA,"
				+ " count(if(tbl.val = 'answer3',1,null)) as fswRITA,"
				+ " count(if(tbl.val = 'answer14',1,null)) as otherRITA,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and tbl.c8_dob is null,1,null)) as fuRITA,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=15) and (year(curdate())-year(tbl.c8_dob)<20),1,null)) as f15RITA,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=20) and (year(curdate())-year(tbl.c8_dob)<25),1,null)) as f20RITA,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=25) and (year(curdate())-year(tbl.c8_dob)<30),1,null)) as f25RITA,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=30) and (year(curdate())-year(tbl.c8_dob)<35),1,null)) as f30RITA,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=35) and (year(curdate())-year(tbl.c8_dob)<40),1,null)) as f35RITA,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=40) and (year(curdate())-year(tbl.c8_dob)<45),1,null)) as f40RITA,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=45) and (year(curdate())-year(tbl.c8_dob)<50),1,null)) as f45RITA,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=50),1,null)) as f50RITA,"
				+ " count(if(tbl.c7_gender = 'MALE'  and tbl.c8_dob is null,1,null)) as muRITA,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=15) and (year(curdate())-year(tbl.c8_dob)<20),1,null)) as m15RITA,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=20) and (year(curdate())-year(tbl.c8_dob)<25),1,null)) as m20RITA,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=25) and (year(curdate())-year(tbl.c8_dob)<30),1,null)) as m25RITA,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=30) and (year(curdate())-year(tbl.c8_dob)<35),1,null)) as m30RITA,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=35) and (year(curdate())-year(tbl.c8_dob)<40),1,null)) as m35RITA,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=40) and (year(curdate())-year(tbl.c8_dob)<45),1,null)) as m40RITA,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=45) and (year(curdate())-year(tbl.c8_dob)<50),1,null)) as m45RITA,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=50),1,null)) as m50RITA"
				+ " from (select thcrg.val, thc.* from tbl_hts_case_risk_group thcrg"
				+ "  inner join tbl_hts_case thc on thcrg.hts_case_id = thc.id"
				+ "      where thcrg.is_main_risk = 1 ) as tbl"
				+ "   inner join tbl_organization_unit tou on tbl.c2_org_id = tou.id"
				+ "   inner join tbl_location tl on tou.address_id = tl.id"
				+ "   inner join tbl_admin_unit pro on tl.province_id = pro.id"
				+ "   inner join tbl_admin_unit dis on tl.district_id = dis.id"
				+ " where 1=1 "
				+ " and (tbl.c24 is not null AND (tbl.c24 = 'answer1' or tbl.c24 = 'answer3')) "
				+ " and (tbl.c5='answer1' OR tbl.c5='answer2') "
				+ " and not ( tbl.id  IN (SELECT DISTINCT(hts_case_id) from tbl_hts_case_risk_group thcrg WHERE thcrg.val='answer5' or thcrg.val='answer6') "
				+ " or tbl.c10 = 'answer2')"
				+ " and tbl.c15 = 'YES' "
				+ " and tbl.c17 = 'answer1' and tbl.c18 = 'answer2' " 
				+ whereClause
				+ " group by orgId,orgCode,orgName,provinceName,districtName,modality";
		org.hibernate.Query query = manager.unwrap(Session.class).createSQLQuery(sql)
				.setResultTransformer(new AliasToBeanResultTransformer(HTSRecentDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			query.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			query.setParameter("fromDate", Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			query.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		List<HTSRecentDto> ret = query.list();
		return ret;
	}

	@Override
	public List<HTSRecentDto> getDataVCTRITALongTerm(PreventionFilterDto filter) {
		String whereClause = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			whereClause += " AND tbl.c2_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			whereClause += " AND tbl.c15_date >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			whereClause += "  AND tbl.c15_date <=:toDate ";
		}
		String sql = "select tbl.c2_org_id as orgId,tou.code as orgCode , tou.name as  orgName, pro.name as provinceName, dis.name as districtName,'VCT - Integrated' as modality,"
				+ " count(if(tbl.val = 'answer1',1,null)) as pwidRITALongTerm,"
				+ " count(if(tbl.val = 'answer2',1,null)) as msmRITALongTerm,"
				+ " count(if(tbl.val = 'answer4',1,null)) as tgRITALongTerm,"
				+ " count(if(tbl.val = 'answer3',1,null)) as fswRITALongTerm,"
				+ " count(if(tbl.val = 'answer14',1,null)) as otherRITALongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and tbl.c8_dob is null,1,null)) as fuRITALongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=15) and (year(curdate())-year(tbl.c8_dob)<20),1,null)) as f15RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=20) and (year(curdate())-year(tbl.c8_dob)<25),1,null)) as f20RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=25) and (year(curdate())-year(tbl.c8_dob)<30),1,null)) as f25RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=30) and (year(curdate())-year(tbl.c8_dob)<35),1,null)) as f30RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=35) and (year(curdate())-year(tbl.c8_dob)<40),1,null)) as f35RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=40) and (year(curdate())-year(tbl.c8_dob)<45),1,null)) as f40RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=45) and (year(curdate())-year(tbl.c8_dob)<50),1,null)) as f45RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=50),1,null)) as f50RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and tbl.c8_dob is null,1,null)) as muRITALongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=15) and (year(curdate())-year(tbl.c8_dob)<20),1,null)) as m15RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=20) and (year(curdate())-year(tbl.c8_dob)<25),1,null)) as m20RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=25) and (year(curdate())-year(tbl.c8_dob)<30),1,null)) as m25RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=30) and (year(curdate())-year(tbl.c8_dob)<35),1,null)) as m30RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=35) and (year(curdate())-year(tbl.c8_dob)<40),1,null)) as m35RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=40) and (year(curdate())-year(tbl.c8_dob)<45),1,null)) as m40RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=45) and (year(curdate())-year(tbl.c8_dob)<50),1,null)) as m45RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=50),1,null)) as m50RITALongTerm"
				+ " from (select thcrg.val, thc.* from tbl_hts_case_risk_group thcrg"
				+ "  inner join tbl_hts_case thc on thcrg.hts_case_id = thc.id"
				+ "      where thcrg.is_main_risk = 1 ) as tbl"
				+ "   inner join tbl_organization_unit tou on tbl.c2_org_id = tou.id"
				+ "   inner join tbl_location tl on tou.address_id = tl.id"
				+ "   inner join tbl_admin_unit pro on tl.province_id = pro.id"
				+ "   inner join tbl_admin_unit dis on tl.district_id = dis.id"
				+ " where 1=1 "
				+ " and (tbl.c24 is not null AND (tbl.c24 = 'answer1' or tbl.c24 = 'answer3')) "
				+ " and (tbl.c5='answer1' OR tbl.c5='answer2') "
				+ " and not ( tbl.id  IN (SELECT DISTINCT(hts_case_id) from tbl_hts_case_risk_group thcrg WHERE thcrg.val='answer5' or thcrg.val='answer6') "
				+ " or tbl.c10 = 'answer2')"
				+ " and tbl.c15 = 'YES' "
				+ " and tbl.c17 = 'answer1' and tbl.c18 = 'answer1' " + whereClause
				+ " group by orgId, orgCode, orgName, provinceName, districtName, modality";
		org.hibernate.Query query = manager.unwrap(Session.class).createSQLQuery(sql)
				.setResultTransformer(new AliasToBeanResultTransformer(HTSRecentDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			query.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			query.setParameter("fromDate", Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			query.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		List<HTSRecentDto> ret = query.list();
		return ret;
	}

	@Override
	public List<HTSRecentDto> getDataFacilityIndexTestingRTRIRecent(PreventionFilterDto filter) {
		String whereClause = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			whereClause += " AND tbl.c2_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			whereClause += " AND tbl.c15_date >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			whereClause += "  AND tbl.c15_date <=:toDate ";
		}
		String sql = "select tbl.c2_org_id as orgId, tou.code as orgCode , tou.name as  orgName, pro.name as provinceName, dis.name as districtName,'Facility Index Testing' as modality,"
				+ " count(if(tbl.val = 'answer1',1,null)) as pwidRTRI,"
				+ " count(if(tbl.val = 'answer2',1,null)) as msmRTRI,"
				+ " count(if(tbl.val = 'answer4',1,null)) as tgRTRI,"
				+ " count(if(tbl.val = 'answer3',1,null)) as fswRTRI,"
				+ " count(if(tbl.val = 'answer14',1,null)) as otherRTRI,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and tbl.c8_dob is null,1,null)) as fuRTRI,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=15) and (year(curdate())-year(tbl.c8_dob)<20),1,null)) as f15RTRI,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=20) and (year(curdate())-year(tbl.c8_dob)<25),1,null)) as f20RTRI,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=25) and (year(curdate())-year(tbl.c8_dob)<30),1,null)) as f25RTRI,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=30) and (year(curdate())-year(tbl.c8_dob)<35),1,null)) as f30RTRI,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=35) and (year(curdate())-year(tbl.c8_dob)<40),1,null)) as f35RTRI,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=40) and (year(curdate())-year(tbl.c8_dob)<45),1,null)) as f40RTRI,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=45) and (year(curdate())-year(tbl.c8_dob)<50),1,null)) as f45RTRI,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=50),1,null)) as f50RTRI,"
				+ " count(if(tbl.c7_gender = 'MALE'  and tbl.c8_dob is null,1,null)) as muRTRI,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=15) and (year(curdate())-year(tbl.c8_dob)<20),1,null)) as m15RTRI,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=20) and (year(curdate())-year(tbl.c8_dob)<25),1,null)) as m20RTRI,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=25) and (year(curdate())-year(tbl.c8_dob)<30),1,null)) as m25RTRI,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=30) and (year(curdate())-year(tbl.c8_dob)<35),1,null)) as m30RTRI,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=35) and (year(curdate())-year(tbl.c8_dob)<40),1,null)) as m35RTRI,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=40) and (year(curdate())-year(tbl.c8_dob)<45),1,null)) as m40RTRI,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=45) and (year(curdate())-year(tbl.c8_dob)<50),1,null)) as m45RTRI,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=50),1,null)) as m50RTRI"
				+ " from (select thcrg.val, thc.* from tbl_hts_case_risk_group thcrg"
				+ "  inner join tbl_hts_case thc on thcrg.hts_case_id = thc.id"
				+ "      where thcrg.is_main_risk = 1 ) as tbl"
				+ "    inner join tbl_organization_unit tou on tbl.c2_org_id = tou.id"
				+ " inner join tbl_location tl on tou.address_id = tl.id"
				+ " inner join tbl_admin_unit pro on tl.province_id = pro.id"
				+ " inner join tbl_admin_unit dis on tl.district_id = dis.id"
				+ " where 1=1 and (tbl.c24 = 'answer1' or tbl.c24 = 'answer3') "
				+ " and  (tbl.id IN (SELECT DISTINCT(hts_case_id) from tbl_hts_case_risk_group thcrg WHERE thcrg.val='answer5' or thcrg.val='answer6') or tbl.c10 = 'answer2')"
				+ " and tbl.c15 = 'YES' and tbl.c17 = 'answer1' " + whereClause
				+ " group by orgId,orgCode,orgName,provinceName,districtName,modality ";
		org.hibernate.Query query = manager.unwrap(Session.class).createSQLQuery(sql)
				.setResultTransformer(new AliasToBeanResultTransformer(HTSRecentDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			query.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			query.setParameter("fromDate", Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			query.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		List<HTSRecentDto> ret = query.list();
		return ret;
	}

	@Override
	public List<HTSRecentDto> getDataFacilityIndexTestingRTRILongTerm(PreventionFilterDto filter) {
		String whereClause = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			whereClause += " AND tbl.c2_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			whereClause += " AND tbl.c15_date >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			whereClause += "  AND tbl.c15_date <=:toDate ";
		}
		String sql = "select tbl.c2_org_id as orgId,tou.code as orgCode , tou.name as  orgName, pro.name as provinceName, dis.name as districtName,'Facility Index Testing' as modality,"
				+ " count(if(tbl.val = 'answer1',1,null)) as pwidRTRILongTerm,"
				+ " count(if(tbl.val = 'answer2',1,null)) as msmRTRILongTerm,"
				+ " count(if(tbl.val = 'answer4',1,null)) as tgRTRILongTerm,"
				+ " count(if(tbl.val = 'answer3',1,null)) as fswRTRILongTerm,"
				+ " count(if(tbl.val = 'answer14',1,null)) as otherRTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and tbl.c8_dob is null,1,null)) as fuRTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=15) and (year(curdate())-year(tbl.c8_dob)<20),1,null)) as f15RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=20) and (year(curdate())-year(tbl.c8_dob)<25),1,null)) as f20RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=25) and (year(curdate())-year(tbl.c8_dob)<30),1,null)) as f25RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=30) and (year(curdate())-year(tbl.c8_dob)<35),1,null)) as f30RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=35) and (year(curdate())-year(tbl.c8_dob)<40),1,null)) as f35RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=40) and (year(curdate())-year(tbl.c8_dob)<45),1,null)) as f40RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=45) and (year(curdate())-year(tbl.c8_dob)<50),1,null)) as f45RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=50),1,null)) as f50RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and tbl.c8_dob is null,1,null)) as muRTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=15) and (year(curdate())-year(tbl.c8_dob)<20),1,null)) as m15RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=20) and (year(curdate())-year(tbl.c8_dob)<25),1,null)) as m20RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=25) and (year(curdate())-year(tbl.c8_dob)<30),1,null)) as m25RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=30) and (year(curdate())-year(tbl.c8_dob)<35),1,null)) as m30RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=35) and (year(curdate())-year(tbl.c8_dob)<40),1,null)) as m35RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=40) and (year(curdate())-year(tbl.c8_dob)<45),1,null)) as m40RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=45) and (year(curdate())-year(tbl.c8_dob)<50),1,null)) as m45RTRILongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=50),1,null)) as m50RTRILongTerm"
				+ " from (select thcrg.val, thc.* from tbl_hts_case_risk_group thcrg"
				+ " inner join tbl_hts_case thc on thcrg.hts_case_id = thc.id"
				+ "      where thcrg.is_main_risk = 1 ) as tbl"
				+ "   inner join tbl_organization_unit tou on tbl.c2_org_id = tou.id"
				+ "   inner join tbl_location tl on tou.address_id = tl.id"
				+ "   inner join tbl_admin_unit pro on tl.province_id = pro.id"
				+ "   inner join tbl_admin_unit dis on tl.district_id = dis.id"
				+ " where 1=1 "
				+ " and (tbl.c24 is not null AND tbl.c24 = 'answer1' or tbl.c24 = 'answer3') "
				
				+ " and (tbl.id IN (SELECT DISTINCT(hts_case_id) from tbl_hts_case_risk_group thcrg WHERE thcrg.val='answer5' or thcrg.val='answer6') "
				+ " 		OR tbl.c10 = 'answer2')"
				+ " and tbl.c15 = 'YES' "
				+ " and tbl.c17 = 'answer2' " 
				+ whereClause
				+ " group by orgId,orgCode,orgName,provinceName,districtName,modality";
		org.hibernate.Query query = manager.unwrap(Session.class).createSQLQuery(sql)
				.setResultTransformer(new AliasToBeanResultTransformer(HTSRecentDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			query.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			query.setParameter("fromDate", Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			query.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		List<HTSRecentDto> ret = query.list();
		return ret;
	}

	@Override
	public List<HTSRecentDto> getDataFacilityIndexTestingRITARecent(PreventionFilterDto filter) {
		String whereClause = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			whereClause += " AND tbl.c2_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			whereClause += " AND tbl.c15_date >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			whereClause += "  AND tbl.c15_date <=:toDate ";
		}
		String sql = "select tbl.c2_org_id as orgId, tou.code as orgCode , tou.name as  orgName, pro.name as provinceName, dis.name as districtName,'Facility Index Testing' as modality,"
				+ " count(if(tbl.val = 'answer1',1,null)) as pwidRITA,"
				+ " count(if(tbl.val = 'answer2',1,null)) as msmRITA,"
				+ " count(if(tbl.val = 'answer4',1,null)) as tgRITA,"
				+ " count(if(tbl.val = 'answer3',1,null)) as fswRITA,"
				+ " count(if(tbl.val = 'answer14',1,null)) as otherRITA,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and tbl.c8_dob is null,1,null)) as fuRITA,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=15) and (year(curdate())-year(tbl.c8_dob)<20),1,null)) as f15RITA,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=20) and (year(curdate())-year(tbl.c8_dob)<25),1,null)) as f20RITA,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=25) and (year(curdate())-year(tbl.c8_dob)<30),1,null)) as f25RITA,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=30) and (year(curdate())-year(tbl.c8_dob)<35),1,null)) as f30RITA,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=35) and (year(curdate())-year(tbl.c8_dob)<40),1,null)) as f35RITA,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=40) and (year(curdate())-year(tbl.c8_dob)<45),1,null)) as f40RITA,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=45) and (year(curdate())-year(tbl.c8_dob)<50),1,null)) as f45RITA,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=50),1,null)) as f50RITA,"
				+ " count(if(tbl.c7_gender = 'MALE'  and tbl.c8_dob is null,1,null)) as muRITA,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=15) and (year(curdate())-year(tbl.c8_dob)<20),1,null)) as m15RITA,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=20) and (year(curdate())-year(tbl.c8_dob)<25),1,null)) as m20RITA,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=25) and (year(curdate())-year(tbl.c8_dob)<30),1,null)) as m25RITA,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=30) and (year(curdate())-year(tbl.c8_dob)<35),1,null)) as m30RITA,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=35) and (year(curdate())-year(tbl.c8_dob)<40),1,null)) as m35RITA,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=40) and (year(curdate())-year(tbl.c8_dob)<45),1,null)) as m40RITA,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=45) and (year(curdate())-year(tbl.c8_dob)<50),1,null)) as m45RITA,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=50),1,null)) as m50RITA"
				+ " from (select thcrg.val, thc.* from tbl_hts_case_risk_group thcrg"
				+ "  inner join tbl_hts_case thc on thcrg.hts_case_id = thc.id"
				+ "      		AND thcrg.is_main_risk = 1 ) as tbl"
				+ "   inner join tbl_organization_unit tou on tbl.c2_org_id = tou.id"
				+ "   inner join tbl_location tl on tou.address_id = tl.id"
				+ "   inner join tbl_admin_unit pro on tl.province_id = pro.id"
				+ "   inner join tbl_admin_unit dis on tl.district_id = dis.id"
				+ " where 1=1 "
				+ " and (tbl.c24 is not null AND tbl.c24 = 'answer1' or tbl.c24 = 'answer3') "				
				+ " and (tbl.id IN (SELECT DISTINCT(hts_case_id) from tbl_hts_case_risk_group thcrg WHERE thcrg.val='answer5' or thcrg.val='answer6') "
				+ " 		OR tbl.c10 = 'answer2')"
				+ " and tbl.c15 = 'YES' "
				+ " and tbl.c17 = 'answer1' and tbl.c18 = 'answer2' " 
				+ whereClause
				+ " group by orgId, orgCode,orgName,provinceName,districtName,modality";
		org.hibernate.Query query = manager.unwrap(Session.class).createSQLQuery(sql)
				.setResultTransformer(new AliasToBeanResultTransformer(HTSRecentDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			query.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			query.setParameter("fromDate", Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			query.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		List<HTSRecentDto> ret = query.list();
		return ret;
	}

	@Override
	public List<HTSRecentDto> getDataFacilityIndexTestingRITALongTerm(PreventionFilterDto filter) {
		String whereClause = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			whereClause += " AND tbl.c2_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			whereClause += " AND tbl.c15_date >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			whereClause += "  AND tbl.c15_date <=:toDate ";
		}
		String sql = "select tbl.c2_org_id as orgId, tou.code as orgCode , tou.name as  orgName, pro.name as provinceName, dis.name as districtName,'Facility Index Testing' as modality,"
				+ " count(if(tbl.val = 'answer1',1,null)) as pwidRITALongTerm,"
				+ " count(if(tbl.val = 'answer2',1,null)) as msmRITALongTerm,"
				+ " count(if(tbl.val = 'answer4',1,null)) as tgRITALongTerm,"
				+ " count(if(tbl.val = 'answer3',1,null)) as fswRITALongTerm,"
				+ " count(if(tbl.val = 'answer14',1,null)) as otherRITALongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and tbl.c8_dob is null,1,null)) as fuRITALongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=15) and (year(curdate())-year(tbl.c8_dob)<20),1,null)) as f15RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=20) and (year(curdate())-year(tbl.c8_dob)<25),1,null)) as f20RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=25) and (year(curdate())-year(tbl.c8_dob)<30),1,null)) as f25RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=30) and (year(curdate())-year(tbl.c8_dob)<35),1,null)) as f30RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=35) and (year(curdate())-year(tbl.c8_dob)<40),1,null)) as f35RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=40) and (year(curdate())-year(tbl.c8_dob)<45),1,null)) as f40RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=45) and (year(curdate())-year(tbl.c8_dob)<50),1,null)) as f45RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'FEMALE'  and (year(curdate())-year(tbl.c8_dob)>=50),1,null)) as f50RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and tbl.c8_dob is null,1,null)) as muRITALongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=15) and (year(curdate())-year(tbl.c8_dob)<20),1,null)) as m15RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=20) and (year(curdate())-year(tbl.c8_dob)<25),1,null)) as m20RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=25) and (year(curdate())-year(tbl.c8_dob)<30),1,null)) as m25RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=30) and (year(curdate())-year(tbl.c8_dob)<35),1,null)) as m30RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=35) and (year(curdate())-year(tbl.c8_dob)<40),1,null)) as m35RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=40) and (year(curdate())-year(tbl.c8_dob)<45),1,null)) as m40RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=45) and (year(curdate())-year(tbl.c8_dob)<50),1,null)) as m45RITALongTerm,"
				+ " count(if(tbl.c7_gender = 'MALE'  and (year(curdate())-year(tbl.c8_dob)>=50),1,null)) as m50RITALongTerm"
				+ " from (select thcrg.val, thc.* from tbl_hts_case_risk_group thcrg"
				+ "  inner join tbl_hts_case thc on thcrg.hts_case_id = thc.id"
				+ "  and thcrg.is_main_risk = 1 ) as tbl"
				+ "   inner join tbl_organization_unit tou on tbl.c2_org_id = tou.id"
				+ "   inner join tbl_location tl on tou.address_id = tl.id"
				+ "   inner join tbl_admin_unit pro on tl.province_id = pro.id"
				+ "   inner join tbl_admin_unit dis on tl.district_id = dis.id"
				+ " where 1=1 "
				+ " and (tbl.c24 is not null AND tbl.c24 = 'answer1' or tbl.c24 = 'answer3') "				
				+ " and (tbl.id IN (SELECT DISTINCT(hts_case_id) from tbl_hts_case_risk_group thcrg WHERE thcrg.val='answer5' or thcrg.val='answer6') "
				+ " 		OR tbl.c10 = 'answer2')"
				+ " and tbl.c15 = 'YES' "
				+ " and tbl.c17 = 'answer1' and tbl.c18 = 'answer1' " 
				+ whereClause
				+ " group by orgId,orgCode,orgName,provinceName,districtName,modality";
		org.hibernate.Query query = manager.unwrap(Session.class).createSQLQuery(sql)
				.setResultTransformer(new AliasToBeanResultTransformer(HTSRecentDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			query.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			query.setParameter("fromDate", Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			query.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		List<HTSRecentDto> ret = query.list();
		return ret;
	}

	@Override
	public List<HTSTSTDto> getDataHTSTSTModalityVCTByKP(PreventionFilterDto filter) {
		String whereClause = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			whereClause += " AND tbl.c2_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			whereClause += " AND tbl.c15_date >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			whereClause += "  AND tbl.c15_date <=:toDate ";
		}
		String sql = "select tou.id     as orgId, " + " tou.code   as orgCode, " + " tou.name   as orgName, "
				+ " pro.name   as provinceName, " + " dis.name   as districtName, "
				+ " 'VCT - Integrated'      as modality, "
				+ " count(if(tbl.val = 'answer1' and tbl.c14 = 'answer2' and tbl.c15 = 'YES' and "
				+ "   not ((tbl.c24 is not null and tbl.c24 = 'answer2') or "
				+ "  (tbl.c11c is not null and tbl.c11c = 'YES')), 1, " + "   null))   as pPWID, "
				+ " count(if(tbl.val = 'answer1' and (tbl.c14 != 'answer2' or tbl.c14 is null) and tbl.c15 = 'YES', 1, "
				+ "   null))   as nPWID, "
				+ " count(if(tbl.val = 'answer2' and tbl.c14 = 'answer2' and tbl.c15 = 'YES' and "
				+ "   not ((tbl.c24 is not null and tbl.c24 = 'answer2') or "
				+ "  (tbl.c11c is not null and tbl.c11c = 'YES')), 1, " + "   null))   as pMSM, "
				+ " count(if(tbl.val = 'answer2' and (tbl.c14 != 'answer2' or tbl.c14 is null) and tbl.c15 = 'YES', 1, "
				+ "   null))   as nMSM, "
				+ " count(if(tbl.val = 'answer4' and tbl.c14 = 'answer2' and tbl.c15 = 'YES' and "
				+ "   not ((tbl.c24 is not null and tbl.c24 = 'answer2') or "
				+ "  (tbl.c11c is not null and tbl.c11c = 'YES')), 1, " + "   null))   as pTG, "
				+ " count(if(tbl.val = 'answer4' and (tbl.c14 != 'answer2' or tbl.c14 is null) and tbl.c15 = 'YES', 1, "
				+ "   null))   as nTG, "
				+ " count(if((tbl.val = 'answer3') and tbl.c14 = 'answer2' and tbl.c15 = 'YES' and "
				+ "   not ((tbl.c24 is not null and tbl.c24 = 'answer2') or "
				+ "  (tbl.c11c is not null and tbl.c11c = 'YES')), 1, null)) as pFSW, "
				+ " count(if((tbl.val = 'answer3') and (tbl.c14 != 'answer2' or tbl.c14 is null) and "
				+ "   tbl.c15 = 'YES', 1, null))  as nFSW, "
				+ " count(if(tbl.val = 'answer14' and tbl.c14 = 'answer2' and tbl.c15 = 'YES' and "
				+ "   not ((tbl.c24 is not null and tbl.c24 = 'answer2') or "
				+ "  (tbl.c11c is not null and tbl.c11c = 'YES')), 1, " + "   null))   as pOther, "
				+ " count(if(tbl.val = 'answer14' and (tbl.c14 != 'answer2' or tbl.c14 is null) and tbl.c15 = 'YES', 1, "
				+ "   null))   as nOther " + " from (select thcrg.val, thc.* "
				+ "      from tbl_hts_case_risk_group thcrg "
				+ "  inner join tbl_hts_case thc on thcrg.hts_case_id = thc.id "
				+ "      where thcrg.is_main_risk = 1) as tbl "
				+ "   inner join tbl_organization_unit tou on tbl.c2_org_id = tou.id "
				+ "   inner join tbl_location tl on tou.address_id = tl.id "
				+ "   inner join tbl_admin_unit pro on tl.province_id = pro.id "
				+ "   inner join tbl_admin_unit dis on tl.district_id = dis.id " 
				+ " where 1 = 1 " 
				+ whereClause
				+ " and (tbl.id not in (select distinct hr.hts_case_id from tbl_hts_case_risk_group hr where hr.val = 'answer5' or hr.val = 'answer6')) " 
				+ " and tbl.c10 != 'answer2' "
				+ " and (tbl.c5 = 'answer1' or tbl.c5 = 'answer2') "
				+ " group by orgId, orgCode, orgName, provinceName, districtName, modality";
		org.hibernate.Query query = manager.unwrap(Session.class).createSQLQuery(sql)
				.setResultTransformer(new AliasToBeanResultTransformer(HTSTSTDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			query.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			query.setParameter("fromDate", Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			query.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		List<HTSTSTDto> ret = query.list();
		return ret;
	}

	@Override
	public List<HTSTSTDto> getDataHTSTSTModalityVCTByPositive(PreventionFilterDto filter) {

		String whereClause = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			whereClause += " AND tbl.c2_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			whereClause += " AND tbl.c15_date >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			whereClause += "  AND tbl.c15_date <=:toDate ";
		}
		String sql = "select tou.id as orgId, " + " tou.code as orgCode, " + " tou.name as orgName, "
				+ " pro.name as provinceName, " + " dis.name as districtName, " + " 'VCT - Integrated'  as modality, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and tbl.c8_dob is null, 1, null)) as pFU, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) < 1), 1, null))   as pF0, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 1) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 5), 1, null))      as pF1, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 5) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 10), 1, null))     as pF5, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 10) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 15), 1, null))     as pF10, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 15) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 20), 1, null))     as pF15, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 20) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 25), 1, null))     as pF20, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 25) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 30), 1, null))     as pF25, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 30) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 35), 1, null))     as pF30, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 35) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 40), 1, null))     as pF35, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 40) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 45), 1, null))     as pF40, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 45) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 50), 1, null))     as pF45, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 50), 1, null)) as pF50, "
				+ " count(if(tbl.c7_gender = 'MALE' and tbl.c8_dob is null, 1, null))   as pMU, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) < 1), 1, null))     as pM0, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 1) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 4), 1, null))      as pM1, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 5) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 10), 1, null))     as pM5, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 10) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 15), 1, null))     as pM10, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 15) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 20), 1, null))     as pM15, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 20) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 25), 1, null))     as pM20, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 25) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 30), 1, null))     as pM25, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 30) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 35), 1, null))     as pM30, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 35) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 40), 1, null))     as pM35, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 40) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 45), 1, null))     as pM40, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 45) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 50), 1, null))     as pM45, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 50), 1, null))   as pM50 "
				+ " from (select thcrg.val, thc.* " + "      from tbl_hts_case_risk_group thcrg "
				+ "  inner join tbl_hts_case thc on thcrg.hts_case_id = thc.id "
				+ "      where thcrg.is_main_risk = 1) as tbl "
				+ "   inner join tbl_organization_unit tou on tbl.c2_org_id = tou.id "
				+ "   inner join tbl_location tl on tou.address_id = tl.id "
				+ "   inner join tbl_admin_unit pro on tl.province_id = pro.id "
				+ "   inner join tbl_admin_unit dis on tl.district_id = dis.id " + " where 1 = 1 " + whereClause
				+ "  and (tbl.id not in (select distinct hr.hts_case_id " + "   from tbl_hts_case_risk_group hr "
				+ "   where hr.val = 'answer5' " + " or hr.val = 'answer6')) "
				+ "  and (tbl.c5 = 'answer1' or tbl.c5 = 'answer2') and tbl.c10 != 'answer2' "
				+ "  and tbl.c14 = 'answer2' " + "  and tbl.c15 = 'YES' "
				+ "  and not ((tbl.c24 is not null and tbl.c24 = 'answer2') or "
				+ "     (tbl.c11c is not null and tbl.c11c = 'YES')) "
				+ " group by orgId, orgCode, orgName, provinceName, districtName, modality";
		org.hibernate.Query query = manager.unwrap(Session.class).createSQLQuery(sql)
				.setResultTransformer(new AliasToBeanResultTransformer(HTSTSTDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			query.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			query.setParameter("fromDate", Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			query.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		List<HTSTSTDto> ret = query.list();
		return ret;
	}

	@Override
	public List<HTSTSTDto> getDataHTSTSTModalityVCTByNegative(PreventionFilterDto filter) {
		String whereClause = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			whereClause += " AND tbl.c2_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			whereClause += " AND tbl.c15_date >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			whereClause += "  AND tbl.c15_date <=:toDate ";
		}
		String sql = "select tou.id as orgId, " + " tou.code as orgCode, " + " tou.name as orgName, "
				+ " pro.name as provinceName, " + " dis.name as districtName, " + " 'VCT - Integrated'  as modality, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and tbl.c8_dob is null, 1, null)) as nFU, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) < 1), 1, null))   as nF0, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 1) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 5), 1, null))      as nF1, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 5) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 10), 1, null))     as nF5, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 10) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 15), 1, null))     as nF10, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 15) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 20), 1, null))     as nF15, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 20) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 25), 1, null))     as nF20, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 25) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 30), 1, null))     as nF25, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 30) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 35), 1, null))     as nF30, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 35) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 40), 1, null))     as nF35, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 40) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 45), 1, null))     as nF40, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 45) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 50), 1, null))     as nF45, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 50), 1, null)) as nF50, "
				+ " count(if(tbl.c7_gender = 'MALE' and tbl.c8_dob is null, 1, null))   as nMU, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) < 1), 1, null))     as nM0, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 1) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 4), 1, null))      as nM1, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 5) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 10), 1, null))     as nM5, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 10) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 15), 1, null))     as nM10, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 15) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 20), 1, null))     as nM15, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 20) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 25), 1, null))     as nM20, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 25) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 30), 1, null))     as nM25, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 30) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 35), 1, null))     as nM30, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 35) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 40), 1, null))     as nM35, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 40) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 45), 1, null))     as nM40, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 45) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 50), 1, null))     as nM45, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 50), 1, null))   as nM50 "
				+ " from (select thcrg.val, thc.* " + "      from tbl_hts_case_risk_group thcrg "
				+ "  inner join tbl_hts_case thc on thcrg.hts_case_id = thc.id "
				+ "      where thcrg.is_main_risk = 1) as tbl "
				+ "   inner join tbl_organization_unit tou on tbl.c2_org_id = tou.id "
				+ "   inner join tbl_location tl on tou.address_id = tl.id "
				+ "   inner join tbl_admin_unit pro on tl.province_id = pro.id "
				+ "   inner join tbl_admin_unit dis on tl.district_id = dis.id " + " where 1 = 1 " + whereClause
				+ "  and (tbl.id not in (select distinct hr.hts_case_id " + "   from tbl_hts_case_risk_group hr "
				+ "   where hr.val = 'answer5' " + " or hr.val = 'answer6')) "
				+ "  and (tbl.c5 = 'answer1' or tbl.c5 = 'answer2') and tbl.c10 != 'answer2' "
				+ "  and (tbl.c14 != 'answer2' or tbl.c14 is null) " + "  and tbl.c15 = 'YES' "
				+ " group by orgId, orgCode, orgName, provinceName, districtName, modality";
		org.hibernate.Query query = manager.unwrap(Session.class).createSQLQuery(sql)
				.setResultTransformer(new AliasToBeanResultTransformer(HTSTSTDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			query.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			query.setParameter("fromDate", Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			query.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		List<HTSTSTDto> ret = query.list();
		return ret;
	}

	@Override
	public List<HTSTSTDto> getDataHTSTSTModalityOtherCommunityByKP(PreventionFilterDto filter) {
		String wherePE = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			wherePE += " AND tbl.c1_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			wherePE += " AND tbl.c11_date >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			wherePE += "  AND tbl.c11_date <=:toDate ";
		}
		String sql = "select tou.id   as orgId, " + " tou.code as orgCode, " + " tou.name as orgName, "
				+ " pro.name as provinceName, " + " dis.name as districtName, "
				+ " 'Other community platforms'    as modality, "
				+ " count(if(tbl.val = 'answer1' and tbl.c131_result = 'answer2' and tbl.c16 is not null and (tbl.c16 = 'answer1' OR tbl.c16 = 'answer3'), 1, null)) as pPWID, "
				+ " count(if(tbl.val = 'answer1' and (tbl.c131_result = 'answer1' or tbl.c13 = 'answer2'), 1, null))   as nPWID, "
				+ " count(if(tbl.val = 'answer2' and tbl.c131_result = 'answer2' and tbl.c16 is not null and (tbl.c16 = 'answer1' OR tbl.c16 = 'answer3'), 1, null)) as pMSM, "
				+ " count(if(tbl.val = 'answer2' and (tbl.c131_result = 'answer1' or tbl.c13 = 'answer2'), 1, null))   as nMSM, "
				+ " count(if(tbl.val = 'answer4' and tbl.c131_result = 'answer2' and tbl.c16 is not null and (tbl.c16 = 'answer1' OR tbl.c16 = 'answer3'), 1, null)) as pTG, "
				+ " count(if(tbl.val = 'answer4' and (tbl.c131_result = 'answer1' or tbl.c13 = 'answer2'), 1, null))   as nTG, "
				+ " count(if(tbl.val = 'answer3' and tbl.c131_result = 'answer2' and tbl.c16 is not null and (tbl.c16 = 'answer1' OR tbl.c16 = 'answer3'), 1, null)) as pFSW, "
				+ " count(if(tbl.val = 'answer3' and (tbl.c131_result = 'answer1' or tbl.c13 = 'answer2'), 1, null))   as nFSW, "
				+ " count(if(tbl.val = 'answer0' and tbl.c131_result = 'answer2' and tbl.c16 is not null and (tbl.c16 = 'answer1' OR tbl.c16 = 'answer3'), 1, null)) as pOther, "
				+ " count(if(tbl.val = 'answer0' and (tbl.c131_result = 'answer1' or tbl.c13 = 'answer2'), 1,  null))   as nOther "
				+ " from " 
				+ "		(select tpcrg.val, tpc.* " 
				+ "      	from tbl_pe_case_risk_group tpcrg "
				+ "  		inner join tbl_pe_case tpc on tpcrg.pe_case_id = tpc.id "
				+ "      	AND tpcrg.is_main_risk = 1) " 
				+ "   as tbl "
				+ "   inner join tbl_organization_unit tou on tbl.c1_org_id = tou.id "
				+ "   inner join tbl_location tl on tou.address_id = tl.id "
				+ "   inner join tbl_admin_unit pro on tl.province_id = pro.id "
				+ "   inner join tbl_admin_unit dis on tl.district_id = dis.id " 
				+ " where 1 = 1 "
				+ " AND (tbl.c12 = 'answer1' or tbl.c12 = 'answer2') " 
				+ " and tbl.c11 = 'YES' "
				+ " AND tbl.id not in (SELECT DISTINCT(pe_case_id) FROM tbl_pe_case_risk_group WHERE val ='answer5' OR val ='answer6') "
				+ " and (tbl.c7 is not null and tbl.c7 != 'answer3' AND tbl.c7 != 'answer4' ) " 
				+ wherePE
				+ " group by orgId, orgCode, orgName, provinceName, districtName, modality";
		org.hibernate.Query queryPE = manager.unwrap(Session.class).createSQLQuery(sql).setResultTransformer(new AliasToBeanResultTransformer(HTSTSTDto.class));
		System.out.println(queryPE.getQueryString());
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			queryPE.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			queryPE.setParameter("fromDate",
					Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			queryPE.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		queryPE.setResultTransformer(Transformers.aliasToBean(HTSTSTDto.class));
		List<HTSTSTDto> retPE = queryPE.list();
		return retPE;
	}

	@Override
	public List<HTSTSTDto> getDataHTSTSTModalityOtherCommunityByPositive(PreventionFilterDto filter) {
		String wherePE = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			wherePE += " AND tbl.c1_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			wherePE += " AND tbl.c11_date >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			wherePE += "  AND tbl.c11_date <=:toDate ";
		}
		String sql = "select tou.id as orgId, " + " tou.code     as orgCode, " + " tou.name     as orgName, "
				+ " pro.name     as provinceName, " + " dis.name     as districtName, "
				+ " 'Other community platforms'      as modality, "
				+ " count(if(tbl.c3 = 'FEMALE' and tbl.c4_dob is null, 1, null)) as pFU, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) < 1), 1, null))   as pF0, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 1) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 5), 1, null)) as pF1, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 5) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 10), 1, null))     as pF5, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 10) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 15), 1, null))     as pF10, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 15) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 20), 1, null))     as pF15, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 20) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 25), 1, null))     as pF20, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 25) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 30), 1, null))     as pF25, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 30) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 35), 1, null))     as pF30, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 35) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 40), 1, null))     as pF35, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 40) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 45), 1, null))     as pF40, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 45) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 50), 1, null))     as pF45, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 50), 1, null)) as pF50, "
				+ " count(if(tbl.c3 = 'MALE' and tbl.c4_dob is null, 1, null))   as pMU, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) < 1), 1, null))     as pM0, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 1) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 5), 1, null)) as pM1, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 5) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 10), 1, null))     as pM5, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 10) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 15), 1, null))     as pM10, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 15) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 20), 1, null))     as pM15, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 20) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 25), 1, null))     as pM20, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 25) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 30), 1, null))     as pM25, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 30) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 35), 1, null))     as pM30, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 35) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 40), 1, null))     as pM35, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 40) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 45), 1, null))     as pM40, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 45) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 50), 1, null))     as pM45, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 50), 1, null))   as pM50 "
				+ " from (select tpcrg.val, tpc.* " + "      from tbl_pe_case_risk_group tpcrg "
				+ "  inner join tbl_pe_case tpc on tpcrg.pe_case_id = tpc.id "
				+ "      where tpcrg.is_main_risk = 1) as tbl "
				+ "   inner join tbl_organization_unit tou on tbl.c1_org_id = tou.id "
				+ "   inner join tbl_location tl on tou.address_id = tl.id "
				+ "   inner join tbl_admin_unit pro on tl.province_id = pro.id "
				+ "   inner join tbl_admin_unit dis on tl.district_id = dis.id " 
				+ " where 1 = 1 "
				+ " AND (tbl.c12 = 'answer1' or tbl.c12 = 'answer2') " 
				+ " and tbl.c11 = 'YES' "
				+ " AND tbl.id not in (SELECT DISTINCT(pe_case_id) FROM tbl_pe_case_risk_group WHERE val ='answer5' OR val ='answer6') "
				+ " and (tbl.c7 is not null and tbl.c7 != 'answer3' AND tbl.c7 != 'answer4' ) "
				+ " AND tbl.c131_result = 'answer2' and tbl.c16 is not null and (tbl.c16 = 'answer1' OR tbl.c16 = 'answer3')" 
				+ wherePE
				+ "group by orgId, orgCode, orgName, provinceName, districtName, modality";
		org.hibernate.Query queryPE = manager.unwrap(Session.class).createSQLQuery(sql).setResultTransformer(new AliasToBeanResultTransformer(HTSTSTDto.class));
		System.out.println(queryPE.getQueryString());
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			queryPE.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			queryPE.setParameter("fromDate",
					Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			queryPE.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		queryPE.setResultTransformer(Transformers.aliasToBean(HTSTSTDto.class));
		List<HTSTSTDto> retPE = queryPE.list();
		return retPE;
	}

	@Override
	public List<HTSTSTDto> getDataHTSTSTModalityOtherCommunityByNegative(PreventionFilterDto filter) {
		String wherePE = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			wherePE += " AND tbl.c1_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			wherePE += " AND tbl.c11_date >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			wherePE += "  AND tbl.c11_date <=:toDate ";
		}
		String sql = "select tou.id as orgId, " 
				+ " tou.code as orgCode, " 
				+ " tou.name as orgName, "
				+ " pro.name as provinceName, " 
				+ " dis.name as districtName, "
				+ " 'Other community platforms' as modality, "
				+ " count(if(tbl.c3 = 'FEMALE' and tbl.c4_dob is null, 1, null)) as nFU, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) < 1), 1, null))   as nF0, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 1) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 5), 1, null)) as nF1, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 5) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 10), 1, null))     as nF5, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 10) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 15), 1, null))     as nF10, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 15) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 20), 1, null))     as nF15, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 20) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 25), 1, null))     as nF20, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 25) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 30), 1, null))     as nF25, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 30) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 35), 1, null))     as nF30, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 35) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 40), 1, null))     as nF35, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 40) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 45), 1, null))     as nF40, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 45) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 50), 1, null))     as nF45, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 50), 1, null)) as nF50, "
				+ " count(if(tbl.c3 = 'MALE' and tbl.c4_dob is null, 1, null))   as nMU, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) < 1), 1, null))     as nM0, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 1) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 5), 1, null)) as nM1, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 5) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 10), 1, null))     as nM5, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 10) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 15), 1, null))     as nM10, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 15) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 20), 1, null))     as nM15, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 20) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 25), 1, null))     as nM20, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 25) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 30), 1, null))     as nM25, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 30) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 35), 1, null))     as nM30, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 35) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 40), 1, null))     as nM35, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 40) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 45), 1, null))     as nM40, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 45) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 50), 1, null))     as nM45, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 50), 1, null))   as nM50 "
				+ "from (select tpcrg.val, tpc.* " + "      from tbl_pe_case_risk_group tpcrg "
				+ "  inner join tbl_pe_case tpc on tpcrg.pe_case_id = tpc.id "
				+ "      where tpcrg.is_main_risk = 1) as tbl "
				+ "   inner join tbl_organization_unit tou on tbl.c1_org_id = tou.id "
				+ "   inner join tbl_location tl on tou.address_id = tl.id "
				+ "   inner join tbl_admin_unit pro on tl.province_id = pro.id "
				+ "   inner join tbl_admin_unit dis on tl.district_id = dis.id " 
				+ " where 1 = 1 " 
				+ " AND (tbl.c12 = 'answer1' or tbl.c12 = 'answer2') " 
				+ " AND tbl.c11 = 'YES' "
				+ " AND tbl.id not in (SELECT DISTINCT(pe_case_id) FROM tbl_pe_case_risk_group WHERE val ='answer5' OR val ='answer6') "
				+ " AND (tbl.c7 is not null and tbl.c7 != 'answer3' AND tbl.c7 != 'answer4' ) "
				+ " AND (tbl.c131_result = 'answer1' or tbl.c13 = 'answer2') " 
				+ wherePE
				+ "group by orgId, orgCode, orgName, provinceName, districtName, modality";
		org.hibernate.Query queryPE = manager.unwrap(Session.class).createSQLQuery(sql)
				.setResultTransformer(new AliasToBeanResultTransformer(HTSTSTDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			queryPE.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			queryPE.setParameter("fromDate",
					Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			queryPE.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		queryPE.setResultTransformer(Transformers.aliasToBean(HTSTSTDto.class));
		List<HTSTSTDto> retPE = queryPE.list();
		return retPE;
	}

	@Override
	public List<HTSTSTDto> getDataHTSTSTModalityCommunitySNSByKP(PreventionFilterDto filter) {
		String wherePE = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			wherePE += " AND tbl.c1_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			wherePE += " AND tbl.c11_date >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			wherePE += "  AND tbl.c11_date <=:toDate ";
		}
		String sql = "select tou.id   as orgId, " 
				+ " tou.code as orgCode, " 
				+ " tou.name as orgName, "
				+ " pro.name as provinceName, " 
				+ " dis.name as districtName, " 
				+ " 'Community SNS'    as modality, "
				+ " count(if(tbl.val = 'answer1' and tbl.c131_result = 'answer2' and tbl.c16 is not null and (tbl.c16 = 'answer1' OR tbl.c16 = 'answer3'), 1, null)) as pPWID, "
				+ " count(if(tbl.val = 'answer1' and (tbl.c131_result = 'answer1' or tbl.c13 = 'answer2'), 1, null))   as nPWID, " 
				+ " count(if(tbl.val = 'answer2' AND tbl.c131_result = 'answer2' and tbl.c16 is not null and (tbl.c16 = 'answer1' OR tbl.c16 = 'answer3'), 1, null)) as pMSM, "
				+ " count(if(tbl.val = 'answer2' and (tbl.c131_result = 'answer1' or tbl.c13 = 'answer2'), 1, null))   as nMSM, " 
				+ " count(if(tbl.val = 'answer4' and tbl.c131_result = 'answer2' and tbl.c16 is not null and (tbl.c16 = 'answer1' OR tbl.c16 = 'answer3'), 1, null)) as pTG, "
				+ " count(if(tbl.val = 'answer4' and (tbl.c131_result = 'answer1' or tbl.c13 = 'answer2'), 1, null))   as nTG, " 
				+ " count(if(tbl.val = 'answer3' and tbl.c131_result = 'answer2' and tbl.c16 is not null and (tbl.c16 = 'answer1' OR tbl.c16 = 'answer3'), 1, null)) as pFSW, "
				+ " count(if(tbl.val = 'answer3' and (tbl.c131_result = 'answer1' or tbl.c13 = 'answer2'), 1, null))   as nFSW, " 
				+ " count(if(tbl.val = 'answer0' and tbl.c131_result = 'answer2' and tbl.c16 is not null and (tbl.c16 = 'answer1' OR tbl.c16 = 'answer3'), 1, null)) as pOther, "
				+ " count(if(tbl.val = 'answer0' and (tbl.c131_result = 'answer1' or tbl.c13 = 'answer2'), 1, null))   as nOther " 
				+ " from (select tpcrg.val, tpc.* "
				+ "      from tbl_pe_case_risk_group tpcrg "
				+ "  	 inner join tbl_pe_case tpc on tpcrg.pe_case_id = tpc.id "
				+ "      AND tpcrg.is_main_risk = 1) as tbl "
				+ "   inner join tbl_organization_unit tou on tbl.c1_org_id = tou.id "
				+ "   inner join tbl_location tl on tou.address_id = tl.id "
				+ "   inner join tbl_admin_unit pro on tl.province_id = pro.id "
				+ "   inner join tbl_admin_unit dis on tl.district_id = dis.id " 
				+ " where 1 = 1 "				
				+ " AND (tbl.c12 = 'answer1' or tbl.c12 = 'answer2') " 
				+ " AND tbl.c11 = 'YES' "
				+ " AND tbl.id not in (SELECT DISTINCT(pe_case_id) FROM tbl_pe_case_risk_group WHERE val ='answer5' OR val ='answer6') "
				+ " AND (tbl.c7 is not null and tbl.c7 = 'answer3') "
				+ wherePE
				+ " group by orgId, orgCode, orgName, provinceName, districtName, modality";
		org.hibernate.Query queryPE = manager.unwrap(Session.class).createSQLQuery(sql).setResultTransformer(new AliasToBeanResultTransformer(HTSTSTDto.class));
		System.out.println(queryPE.getQueryString());
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			queryPE.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			queryPE.setParameter("fromDate",
					Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			queryPE.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		queryPE.setResultTransformer(Transformers.aliasToBean(HTSTSTDto.class));
		List<HTSTSTDto> retPE = queryPE.list();
		return retPE;
	}

	@Override
	public List<HTSTSTDto> getDataHTSTSTModalityCommunitySNSByPositive(PreventionFilterDto filter) {
		String wherePE = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			wherePE += " AND tbl.c1_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			wherePE += " AND tbl.c11_date >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			wherePE += "  AND tbl.c11_date <=:toDate ";
		}
		String sql = "select tou.id as orgId, " + " tou.code     as orgCode, " + " tou.name     as orgName, "
				+ " pro.name     as provinceName, " + " dis.name     as districtName, "
				+ " 'Community SNS'      as modality, "
				+ " count(if(tbl.c3 = 'FEMALE' and tbl.c4_dob is null, 1, null)) as pFU, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) < 1), 1, null))   as pF0, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 1) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 5), 1, null)) as pF1, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 5) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 10), 1, null))     as pF5, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 10) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 15), 1, null))     as pF10, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 15) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 20), 1, null))     as pF15, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 20) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 25), 1, null))     as pF20, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 25) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 30), 1, null))     as pF25, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 30) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 35), 1, null))     as pF30, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 35) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 40), 1, null))     as pF35, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 40) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 45), 1, null))     as pF40, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 45) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 50), 1, null))     as pF45, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 50), 1, null)) as pF50, "
				+ " count(if(tbl.c3 = 'MALE' and tbl.c4_dob is null, 1, null))   as pMU, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) < 1), 1, null))     as pM0, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 1) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 5), 1, null)) as pM1, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 5) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 10), 1, null))     as pM5, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 10) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 15), 1, null))     as pM10, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 15) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 20), 1, null))     as pM15, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 20) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 25), 1, null))     as pM20, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 25) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 30), 1, null))     as pM25, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 30) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 35), 1, null))     as pM30, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 35) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 40), 1, null))     as pM35, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 40) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 45), 1, null))     as pM40, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 45) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 50), 1, null))     as pM45, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 50), 1, null))   as pM50 "
				+ "from (select tpcrg.val, tpc.* " + "      from tbl_pe_case_risk_group tpcrg "
				+ "  inner join tbl_pe_case tpc on tpcrg.pe_case_id = tpc.id "
				+ "      where tpcrg.is_main_risk = 1) as tbl "
				+ "   inner join tbl_organization_unit tou on tbl.c1_org_id = tou.id "
				+ "   inner join tbl_location tl on tou.address_id = tl.id "
				+ "   inner join tbl_admin_unit pro on tl.province_id = pro.id "
				+ "   inner join tbl_admin_unit dis on tl.district_id = dis.id " 
				+ "where 1 = 1 " 
				+ " AND (tbl.c12 = 'answer1' or tbl.c12 = 'answer2') " 
				+ " AND tbl.c11 = 'YES' "
				+ " AND tbl.id not in (SELECT DISTINCT(pe_case_id) FROM tbl_pe_case_risk_group WHERE val ='answer5' OR val ='answer6') "
				+ " AND (tbl.c7 is not null and tbl.c7 = 'answer3') "
				+ " AND tbl.c131_result = 'answer2' AND tbl.c16 IS NOT NULL AND (tbl.c16 = 'answer1' OR tbl.c16 = 'answer3')"
				+ wherePE
				+ "group by orgId, orgCode, orgName, provinceName, districtName, modality";
		org.hibernate.Query queryPE = manager.unwrap(Session.class).createSQLQuery(sql)
				.setResultTransformer(new AliasToBeanResultTransformer(HTSTSTDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			queryPE.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			queryPE.setParameter("fromDate",
					Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			queryPE.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		queryPE.setResultTransformer(Transformers.aliasToBean(HTSTSTDto.class));
		List<HTSTSTDto> retPE = queryPE.list();
		return retPE;
	}

	@Override
	public List<HTSTSTDto> getDataHTSTSTModalityCommunitySNSByNegative(PreventionFilterDto filter) {
		String wherePE = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			wherePE += " AND tbl.c1_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			wherePE += " AND tbl.c11_date >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			wherePE += "  AND tbl.c11_date <=:toDate ";
		}
		String sql = "select tou.id as orgId, " + " tou.code     as orgCode, " + " tou.name     as orgName, "
				+ " pro.name     as provinceName, " + " dis.name     as districtName, "
				+ " 'Community SNS'      as modality, "
				+ " count(if(tbl.c3 = 'FEMALE' and tbl.c4_dob is null, 1, null)) as nFU, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) < 1), 1, null))   as nF0, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 1) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 5), 1, null)) as nF1, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 5) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 10), 1, null))     as nF5, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 10) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 15), 1, null))     as nF10, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 15) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 20), 1, null))     as nF15, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 20) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 25), 1, null))     as nF20, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 25) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 30), 1, null))     as nF25, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 30) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 35), 1, null))     as nF30, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 35) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 40), 1, null))     as nF35, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 40) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 45), 1, null))     as nF40, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 45) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 50), 1, null))     as nF45, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 50), 1, null)) as nF50, "
				+ " count(if(tbl.c3 = 'MALE' and tbl.c4_dob is null, 1, null))   as nMU, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) < 1), 1, null))     as nM0, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 1) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 5), 1, null)) as nM1, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 5) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 10), 1, null))     as nM5, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 10) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 15), 1, null))     as nM10, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 15) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 20), 1, null))     as nM15, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 20) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 25), 1, null))     as nM20, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 25) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 30), 1, null))     as nM25, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 30) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 35), 1, null))     as nM30, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 35) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 40), 1, null))     as nM35, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 40) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 45), 1, null))     as nM40, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 45) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 50), 1, null))     as nM45, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 50), 1, null))   as nM50 "
				+ "from (select tpcrg.val, tpc.* " + "      from tbl_pe_case_risk_group tpcrg "
				+ "  inner join tbl_pe_case tpc on tpcrg.pe_case_id = tpc.id "
				+ "      where tpcrg.is_main_risk = 1) as tbl "
				+ "   inner join tbl_organization_unit tou on tbl.c1_org_id = tou.id "
				+ "   inner join tbl_location tl on tou.address_id = tl.id "
				+ "   inner join tbl_admin_unit pro on tl.province_id = pro.id "
				+ "   inner join tbl_admin_unit dis on tl.district_id = dis.id " 
				+ "where 1 = 1 " 
				+ " AND (tbl.c12 = 'answer1' or tbl.c12 = 'answer2') " 
				+ " AND tbl.c11 = 'YES' "
				+ " AND tbl.id not in (SELECT DISTINCT(pe_case_id) FROM tbl_pe_case_risk_group WHERE val ='answer5' OR val ='answer6') "
				+ " AND (tbl.c7 is not null and tbl.c7 = 'answer3') "
				+ " AND (tbl.c131_result = 'answer1' OR tbl.c13 = 'answer2')"
				+ wherePE
				+ "group by orgId, orgCode, orgName, provinceName, districtName, modality";
		org.hibernate.Query queryPE = manager.unwrap(Session.class).createSQLQuery(sql)
				.setResultTransformer(new AliasToBeanResultTransformer(HTSTSTDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			queryPE.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			queryPE.setParameter("fromDate",
					Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			queryPE.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		queryPE.setResultTransformer(Transformers.aliasToBean(HTSTSTDto.class));
		List<HTSTSTDto> retPE = queryPE.list();
		return retPE;
	}

	@Override
	public List<HTSTSTDto> getDataHTSTSTModalityFacilitySNSByKP(PreventionFilterDto filter) {
		String whereClause = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			whereClause += " AND tbl.c2_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			whereClause += " AND tbl.c15_date >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			whereClause += "  AND tbl.c15_date <=:toDate ";
		}
		String sql = "select tou.id     as orgId, " + " tou.code   as orgCode, " + " tou.name   as orgName, "
				+ " pro.name   as provinceName, " + " dis.name   as districtName, " + " 'Facility SNS'    as modality, "
				+ " count(if(tbl.val = 'answer1' and tbl.c14 = 'answer2' and tbl.c15 = 'YES' and "
				+ "   not ((tbl.c24 is not null and tbl.c24 = 'answer2') or "
				+ "  (tbl.c11c is not null and tbl.c11c = 'YES')), 1, " + "   null))   as pPWID, "
				+ " count(if(tbl.val = 'answer1' and (tbl.c14 != 'answer2' or tbl.c14 is null) and tbl.c15 = 'YES', 1, "
				+ "   null))   as nPWID, "
				+ " count(if(tbl.val = 'answer2' and tbl.c14 = 'answer2' and tbl.c15 = 'YES' and "
				+ "   not ((tbl.c24 is not null and tbl.c24 = 'answer2') or "
				+ "  (tbl.c11c is not null and tbl.c11c = 'YES')), 1, " + "   null))   as pMSM, "
				+ " count(if(tbl.val = 'answer2' and (tbl.c14 != 'answer2' or tbl.c14 is null) and tbl.c15 = 'YES', 1, "
				+ "   null))   as nMSM, "
				+ " count(if(tbl.val = 'answer4' and tbl.c14 = 'answer2' and tbl.c15 = 'YES' and "
				+ "   not ((tbl.c24 is not null and tbl.c24 = 'answer2') or "
				+ "  (tbl.c11c is not null and tbl.c11c = 'YES')), 1, " + "   null))   as pTG, "
				+ " count(if(tbl.val = 'answer4' and (tbl.c14 != 'answer2' or tbl.c14 is null) and tbl.c15 = 'YES', 1, "
				+ "   null))   as nTG, "
				+ " count(if((tbl.val = 'answer3') and tbl.c14 = 'answer2' and tbl.c15 = 'YES' and "
				+ "   not ((tbl.c24 is not null and tbl.c24 = 'answer2') or "
				+ "  (tbl.c11c is not null and tbl.c11c = 'YES')), 1, null)) as pFSW, "
				+ " count(if((tbl.val = 'answer3') and (tbl.c14 != 'answer2' or tbl.c14 is null) and "
				+ "   tbl.c15 = 'YES', 1, null))  as nFSW, "
				+ " count(if(tbl.val = 'answer14' and tbl.c14 = 'answer2' and tbl.c15 = 'YES' and "
				+ "   not ((tbl.c24 is not null and tbl.c24 = 'answer2') or "
				+ "  (tbl.c11c is not null and tbl.c11c = 'YES')), 1, " + "   null))   as pOther, "
				+ " count(if(tbl.val = 'answer14' and (tbl.c14 != 'answer2' or tbl.c14 is null) and tbl.c15 = 'YES', 1, "
				+ "   null))   as nOther " + " from (select thcrg.val, thc.* "
				+ "      from tbl_hts_case_risk_group thcrg "
				+ "  inner join tbl_hts_case thc on thcrg.hts_case_id = thc.id "
				+ "      where thcrg.is_main_risk = 1) as tbl "
				+ "   inner join tbl_organization_unit tou on tbl.c2_org_id = tou.id "
				+ "   inner join tbl_location tl on tou.address_id = tl.id "
				+ "   inner join tbl_admin_unit pro on tl.province_id = pro.id "
				+ "   inner join tbl_admin_unit dis on tl.district_id = dis.id " + " where 1 = 1 " + whereClause
				+ "  and tbl.c5 = 'answer3'  " + "  and not (tbl.id in (select distinct hr.hts_case_id "
				+ "   from tbl_hts_case_risk_group hr " + "   where hr.val = 'answer5' " + " or hr.val = 'answer6') "
				+ " or tbl.c10 = 'answer2' ) "
				+ " group by orgId, orgCode, orgName, provinceName, districtName, modality";
		org.hibernate.Query query = manager.unwrap(Session.class).createSQLQuery(sql)
				.setResultTransformer(new AliasToBeanResultTransformer(HTSTSTDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			query.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			query.setParameter("fromDate", Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			query.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		List<HTSTSTDto> ret = query.list();
		return ret;
	}

	@Override
	public List<HTSTSTDto> getDataHTSTSTModalityFacilitySNSByPositive(PreventionFilterDto filter) {
		String whereClause = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			whereClause += " AND tbl.c2_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			whereClause += " AND tbl.c15_date >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			whereClause += "  AND tbl.c15_date <=:toDate ";
		}
		String sql = "select tou.id as orgId, " + " tou.code as orgCode, " + " tou.name as orgName, "
				+ " pro.name as provinceName, " + " dis.name as districtName, " + " 'Facility SNS'  as modality, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and tbl.c8_dob is null, 1, null)) as pFU, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) < 1), 1, null))   as pF0, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 1) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 5), 1, null))      as pF1, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 5) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 10), 1, null))     as pF5, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 10) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 15), 1, null))     as pF10, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 15) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 20), 1, null))     as pF15, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 20) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 25), 1, null))     as pF20, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 25) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 30), 1, null))     as pF25, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 30) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 35), 1, null))     as pF30, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 35) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 40), 1, null))     as pF35, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 40) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 45), 1, null))     as pF40, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 45) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 50), 1, null))     as pF45, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 50), 1, null)) as pF50, "
				+ " count(if(tbl.c7_gender = 'MALE' and tbl.c8_dob is null, 1, null))   as pMU, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) < 1), 1, null))     as pM0, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 1) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 4), 1, null))      as pM1, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 5) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 10), 1, null))     as pM5, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 10) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 15), 1, null))     as pM10, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 15) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 20), 1, null))     as pM15, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 20) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 25), 1, null))     as pM20, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 25) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 30), 1, null))     as pM25, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 30) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 35), 1, null))     as pM30, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 35) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 40), 1, null))     as pM35, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 40) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 45), 1, null))     as pM40, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 45) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 50), 1, null))     as pM45, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 50), 1, null))   as pM50 "
				+ " from (select thcrg.val, thc.* " + "      from tbl_hts_case_risk_group thcrg "
				+ "  inner join tbl_hts_case thc on thcrg.hts_case_id = thc.id "
				+ "      where thcrg.is_main_risk = 1) as tbl "
				+ "   inner join tbl_organization_unit tou on tbl.c2_org_id = tou.id "
				+ "   inner join tbl_location tl on tou.address_id = tl.id "
				+ "   inner join tbl_admin_unit pro on tl.province_id = pro.id "
				+ "   inner join tbl_admin_unit dis on tl.district_id = dis.id " + " where 1 = 1 " + whereClause
				+ "  and tbl.c5 = 'answer3' " + "  and tbl.c14 = 'answer2' " + "  and tbl.c15 = 'YES' "
				+ "  and not ((tbl.c24 is not null and tbl.c24 = 'answer2') or "
				+ "     (tbl.c11c is not null and tbl.c11c = 'YES')) "
				+ "  and not (tbl.id in (select distinct hr.hts_case_id "
				+ "   from tbl_hts_case_risk_group hr " + "   where hr.val = 'answer5' " + " or hr.val = 'answer6') "
				+ " or tbl.c10 = 'answer2' ) "
				+ " group by orgId, orgCode, orgName, provinceName, districtName, modality";
		org.hibernate.Query query = manager.unwrap(Session.class).createSQLQuery(sql)
				.setResultTransformer(new AliasToBeanResultTransformer(HTSTSTDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			query.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			query.setParameter("fromDate", Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			query.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		List<HTSTSTDto> ret = query.list();
		return ret;
	}

	@Override
	public List<HTSTSTDto> getDataHTSTSTModalityFacilitySNSByNegative(PreventionFilterDto filter) {
		String whereClause = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			whereClause += " AND tbl.c2_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			whereClause += " AND tbl.c15_date >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			whereClause += "  AND tbl.c15_date <=:toDate ";
		}
		String sql = "select tou.id as orgId, " + " tou.code as orgCode, " + " tou.name as orgName, "
				+ " pro.name as provinceName, " + " dis.name as districtName, " + " 'Facility SNS'  as modality, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and tbl.c8_dob is null, 1, null)) as nFU, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) < 1), 1, null))   as nF0, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 1) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 5), 1, null))      as nF1, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 5) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 10), 1, null))     as nF5, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 10) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 15), 1, null))     as nF10, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 15) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 20), 1, null))     as nF15, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 20) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 25), 1, null))     as nF20, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 25) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 30), 1, null))     as nF25, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 30) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 35), 1, null))     as nF30, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 35) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 40), 1, null))     as nF35, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 40) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 45), 1, null))     as nF40, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 45) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 50), 1, null))     as nF45, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 50), 1, null)) as nF50, "
				+ " count(if(tbl.c7_gender = 'MALE' and tbl.c8_dob is null, 1, null))   as nMU, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) < 1), 1, null))     as nM0, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 1) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 4), 1, null))      as nM1, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 5) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 10), 1, null))     as nM5, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 10) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 15), 1, null))     as nM10, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 15) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 20), 1, null))     as nM15, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 20) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 25), 1, null))     as nM20, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 25) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 30), 1, null))     as nM25, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 30) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 35), 1, null))     as nM30, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 35) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 40), 1, null))     as nM35, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 40) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 45), 1, null))     as nM40, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 45) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 50), 1, null))     as nM45, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 50), 1, null))   as nM50 "
				+ " from (select thcrg.val, thc.* " + "      from tbl_hts_case_risk_group thcrg "
				+ "  inner join tbl_hts_case thc on thcrg.hts_case_id = thc.id "
				+ "      where thcrg.is_main_risk = 1) as tbl "
				+ "   inner join tbl_organization_unit tou on tbl.c2_org_id = tou.id "
				+ "   inner join tbl_location tl on tou.address_id = tl.id "
				+ "   inner join tbl_admin_unit pro on tl.province_id = pro.id "
				+ "   inner join tbl_admin_unit dis on tl.district_id = dis.id " + " where 1 = 1 " + whereClause
				+ "  and tbl.c5 = 'answer3' " + "  and (tbl.id not in (select distinct hr.hts_case_id "
				+ "   from tbl_hts_case_risk_group hr " + "   where hr.val = 'answer5' " + " or hr.val = 'answer6')) "
				+ " and tbl.c10 != 'answer2' " + "  and (tbl.c14 != 'answer2' or tbl.c14 is null) "
				+ "  and tbl.c15 = 'YES' "
				+ "  and not (tbl.id in (select distinct hr.hts_case_id "
				+ "   from tbl_hts_case_risk_group hr " + "   where hr.val = 'answer5' " + " or hr.val = 'answer6') "
				+ " or tbl.c10 = 'answer2' ) "
				+ " group by orgId, orgCode, orgName, provinceName, districtName, modality";
		org.hibernate.Query query = manager.unwrap(Session.class).createSQLQuery(sql)
				.setResultTransformer(new AliasToBeanResultTransformer(HTSTSTDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			query.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			query.setParameter("fromDate", Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			query.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		List<HTSTSTDto> ret = query.list();
		return ret;
	}

	@Override
	public List<HTSIndexDto> getDataHTSIndexModalityCommunityIndexTestingOffered(PreventionFilterDto filter) {
		String wherePE = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			wherePE += " AND tbl.c1_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			wherePE += " AND tbl.c1 >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			wherePE += "  AND tbl.c1 <=:toDate ";
		}
		String sql = "select tou.id as orgId, " + " tou.code     as orgCode, " + " tou.name     as orgName, "
				+ " pro.name     as provinceName, " + " dis.name     as districtName, "
				+ " 'Community Index Testing'  as modality, "
				+ " count(if(tbl.c3 = 'FEMALE' and tbl.c4_dob is null, 1, null)) as fuOffered, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) < 1), 1, null))   as f0Offered, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 1) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 5), 1, null)) as f1Offered, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 5) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 10), 1, null))     as f5Offered, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 10) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 15), 1, null))     as f10Offered, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 15) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 20), 1, null))     as f15Offered, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 20) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 25), 1, null))     as f20Offered, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 25) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 30), 1, null))     as f25Offered, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 30) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 35), 1, null))     as f30Offered, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 35) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 40), 1, null))     as f35Offered, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 40) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 45), 1, null))     as f40Offered, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 45) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 50), 1, null))     as f45Offered, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 50), 1, null)) as f50Offered, "
				+ " count(if(tbl.c3 = 'MALE' and tbl.c4_dob is null, 1, null))   as muOffered, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) < 1), 1, null))     as m0Offered, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 1) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 5), 1, null)) as m1Offered, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 5) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 10), 1, null))     as m5Offered, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 10) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 15), 1, null))     as m10Offered, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 15) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 20), 1, null))     as m15Offered, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 20) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 25), 1, null))     as m20Offered, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 25) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 30), 1, null))     as m25Offered, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 30) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 35), 1, null))     as m30Offered, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 35) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 40), 1, null))     as m35Offered, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 40) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 45), 1, null))     as m40Offered, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 45) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 50), 1, null))     as m45Offered, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 50), 1, null))   as m50Offered "
				+ "from tbl_pe_case tbl " + "   inner join tbl_organization_unit tou on tbl.c1_org_id = tou.id "
				+ "   inner join tbl_location tl on tou.address_id = tl.id "
				+ "   inner join tbl_admin_unit pro on tl.province_id = pro.id "
				+ "   inner join tbl_admin_unit dis on tl.district_id = dis.id " 
				+ "where 1 = 1 " 
				+ wherePE
//				+ " AND (tbl.c12 = 'answer1' OR tbl.c12 = 'answer2')" 
//				+ " AND tbl.c11 = 'YES'" 
				+ " AND (tbl.id in (SELECT DISTINCT(pe_case_id) FROM tbl_pe_case_risk_group WHERE val ='answer5' OR val ='answer6')" 
				+ "  			OR (tbl.c7 IS NOT NULL AND tbl.c7 = 'answer4')) " 
				+ " AND tbl.c9 = 'YES' "
				+ " AND tbl.c8='answer1' "
				+ " group by orgId, orgCode, orgName, provinceName, districtName, modality;";
		org.hibernate.Query queryPE = manager.unwrap(Session.class).createSQLQuery(sql)
				.setResultTransformer(new AliasToBeanResultTransformer(HTSIndexDto.class));
		System.out.println("getDataHTSIndexModalityCommunityIndexTestingOffered: "+queryPE.getQueryString());
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			queryPE.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			queryPE.setParameter("fromDate",
					Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			queryPE.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		queryPE.setResultTransformer(Transformers.aliasToBean(HTSIndexDto.class));
		List<HTSIndexDto> retPE = queryPE.list();
		return retPE;
	}

	@Override
	public List<HTSIndexDto> getDataHTSIndexModalityCommunityIndexTestingAccepted(PreventionFilterDto filter) {
		String wherePE = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			wherePE += " AND tbl.c1_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			wherePE += " AND tbl.c1 >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			wherePE += "  AND tbl.c1 <=:toDate ";
		}
		String sql = "select tou.id as orgId, " + " tou.code     as orgCode, " + " tou.name     as orgName, "
				+ " pro.name     as provinceName, " + " dis.name     as districtName, "
				+ " 'Community Index Testing'  as modality, "
				+ " count(if(tbl.c3 = 'FEMALE' and tbl.c4_dob is null, 1, null)) as fuAccepted, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) < 1), 1, null))   as f0Accepted, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 1) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 5), 1, null)) as f1Accepted, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 5) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 10), 1, null))     as f5Accepted, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 10) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 15), 1, null))     as f10Accepted, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 15) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 20), 1, null))     as f15Accepted, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 20) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 25), 1, null))     as f20Accepted, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 25) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 30), 1, null))     as f25Accepted, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 30) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 35), 1, null))     as f30Accepted, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 35) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 40), 1, null))     as f35Accepted, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 40) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 45), 1, null))     as f40Accepted, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 45) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 50), 1, null))     as f45Accepted, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 50), 1, null)) as f50Accepted, "
				+ " count(if(tbl.c3 = 'MALE' and tbl.c4_dob is null, 1, null))   as muAccepted, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) < 1), 1, null))     as m0Accepted, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 1) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 5), 1, null)) as m1Accepted, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 5) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 10), 1, null))     as m5Accepted, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 10) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 15), 1, null))     as m10Accepted, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 15) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 20), 1, null))     as m15Accepted, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 20) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 25), 1, null))     as m20Accepted, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 25) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 30), 1, null))     as m25Accepted, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 30) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 35), 1, null))     as m30Accepted, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 35) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 40), 1, null))     as m35Accepted, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 40) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 45), 1, null))     as m40Accepted, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 45) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 50), 1, null))     as m45Accepted, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 50), 1, null))   as m50Accepted "
				+ "from tbl_pe_case tbl " + "   inner join tbl_organization_unit tou on tbl.c1_org_id = tou.id "
				+ "   inner join tbl_location tl on tou.address_id = tl.id "
				+ "   inner join tbl_admin_unit pro on tl.province_id = pro.id "
				+ "   inner join tbl_admin_unit dis on tl.district_id = dis.id " 
				+ "where 1 = 1 " 
				+ wherePE
//				+ " AND (tbl.c12 = 'answer1' OR tbl.c12 = 'answer2')" 
//				+ " AND tbl.c11 = 'YES'" 
				+ " AND (tbl.id in (SELECT DISTINCT(pe_case_id) FROM tbl_pe_case_risk_group WHERE val ='answer5' OR val ='answer6')" 
				+ "  			OR (tbl.c7 IS NOT NULL AND tbl.c7 = 'answer4')) " 
				+ " AND tbl.c9 = 'YES' "
				+ " AND tbl.c8='answer1' "
				+ " AND tbl.c10='YES' " 
				+ " group by orgId, orgCode, orgName, provinceName, districtName, modality";
		org.hibernate.Query queryPE = manager.unwrap(Session.class).createSQLQuery(sql)
				.setResultTransformer(new AliasToBeanResultTransformer(HTSIndexDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			queryPE.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			queryPE.setParameter("fromDate",
					Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			queryPE.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		queryPE.setResultTransformer(Transformers.aliasToBean(HTSIndexDto.class));
		List<HTSIndexDto> retPE = queryPE.list();
		return retPE;
	}

	@Override
	public List<HTSIndexDto> getDataHTSIndexModalityCommunityIndexTestingContactsElicited(PreventionFilterDto filter) {
		String wherePE = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			wherePE += " AND tbl.c1_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			wherePE += " AND tbl.c1 >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			wherePE += "  AND tbl.c1 <=:toDate ";
		}
		String sql = "select tou.id as orgId, " + " tou.code     as orgCode, " + " tou.name     as orgName, "
				+ " pro.name     as provinceName, " + " dis.name     as districtName, "
				+ " 'Community Index Testing'  as modality, "
				+ " count(if(tbl.c3 = 'FEMALE' and tbl.c4_dob is null, 1, null)) as fuContactsElicited, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) < 15), 1, null))  as f0ContactsElicited, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 15), 1, null)) as f15ContactsElicited, "
				+ " count(if(tbl.c3 = 'MALE' and tbl.c4_dob is null, 1, null))   as muContactsElicited, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) < 15), 1, null))    as m0ContactsElicited, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 15), 1, null))   as m15ContactsElicited "
				+ "from tbl_pe_case tbl " + "   inner join tbl_organization_unit tou on tbl.c1_org_id = tou.id "
				+ "   inner join tbl_location tl on tou.address_id = tl.id "
				+ "   inner join tbl_admin_unit pro on tl.province_id = pro.id "
				+ "   inner join tbl_admin_unit dis on tl.district_id = dis.id " 
				+ "where 1 = 1 " 
				+ wherePE
//				+ " AND (tbl.c12 = 'answer1' OR tbl.c12 = 'answer2')" 
//				+ " AND tbl.c11 = 'YES'" 
				+ " AND (tbl.id in (SELECT DISTINCT(pe_case_id) FROM tbl_pe_case_risk_group WHERE val ='answer5' OR val ='answer6')" 
				+ "  			OR (tbl.c7 IS NOT NULL AND tbl.c7 = 'answer4')) " 				
				+ "  and tbl.parent_id is not null "
				+ "group by orgId, orgCode, orgName, provinceName, districtName, modality;";
		org.hibernate.Query queryPE = manager.unwrap(Session.class).createSQLQuery(sql)
				.setResultTransformer(new AliasToBeanResultTransformer(HTSIndexDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			queryPE.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			queryPE.setParameter("fromDate",
					Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			queryPE.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		queryPE.setResultTransformer(Transformers.aliasToBean(HTSIndexDto.class));
		List<HTSIndexDto> retPE = queryPE.list();
		return retPE;
	}

	@Override
	public List<HTSIndexDto> getDataHTSIndexModalityCommunityIndexTestingKnownPositives(PreventionFilterDto filter) {
		String wherePE = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			wherePE += " AND tbl.c1_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			wherePE += " AND tbl.c1 >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			wherePE += "  AND tbl.c1 <=:toDate ";
		}
		String sql = "select tou.id as orgId, " + " tou.code     as orgCode, " + " tou.name     as orgName, "
				+ " pro.name     as provinceName, " + " dis.name     as districtName, "
				+ " 'Community Index Testing'  as modality, "
				+ " count(if(tbl.c3 = 'FEMALE' and tbl.c4_dob is null, 1, null)) as fuKnownPositives, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) < 1), 1, null))   as f0KnownPositives, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 1) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 5), 1, null)) as f1KnownPositives, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 5) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 10), 1, null))     as f5KnownPositives, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 10) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 15), 1, null))     as f10KnownPositives, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 15) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 20), 1, null))     as f15KnownPositives, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 20) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 25), 1, null))     as f20KnownPositives, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 25) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 30), 1, null))     as f25KnownPositives, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 30) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 35), 1, null))     as f30KnownPositives, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 35) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 40), 1, null))     as f35KnownPositives, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 40) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 45), 1, null))     as f40KnownPositives, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 45) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 50), 1, null))     as f45KnownPositives, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 50), 1, null)) as f50KnownPositives, "
				+ " count(if(tbl.c3 = 'MALE' and tbl.c4_dob is null, 1, null))   as muKnownPositives, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) < 1), 1, null))     as m0KnownPositives, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 1) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 5), 1, null)) as m1KnownPositives, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 5) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 10), 1, null))     as m5KnownPositives, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 10) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 15), 1, null))     as m10KnownPositives, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 15) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 20), 1, null))     as m15KnownPositives, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 20) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 25), 1, null))     as m20KnownPositives, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 25) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 30), 1, null))     as m25KnownPositives, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 30) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 35), 1, null))     as m30KnownPositives, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 35) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 40), 1, null))     as m35KnownPositives, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 40) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 45), 1, null))     as m40KnownPositives, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 45) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 50), 1, null))     as m45KnownPositives, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 50), 1, null))   as m50KnownPositives "
				+ "from tbl_pe_case tbl " + "   inner join tbl_organization_unit tou on tbl.c1_org_id = tou.id "
				+ "   inner join tbl_location tl on tou.address_id = tl.id "
				+ "   inner join tbl_admin_unit pro on tl.province_id = pro.id "
				+ "   inner join tbl_admin_unit dis on tl.district_id = dis.id " 
				+ "where 1 = 1 " 
				+ wherePE
//				+ " AND (tbl.c12 = 'answer1' OR tbl.c12 = 'answer2')" 
//				+ " AND tbl.c11 = 'YES'" 
				+ " AND (tbl.id in (SELECT DISTINCT(pe_case_id) FROM tbl_pe_case_risk_group WHERE val ='answer5' OR val ='answer6')" 
				+ "  			OR (tbl.c7 IS NOT NULL AND tbl.c7 = 'answer4')) " 				
				+ " and tbl.parent_id is not null "
				+ " AND tbl.c8='answer1'"				
				+ " group by orgId, orgCode, orgName, provinceName, districtName, modality;";
		org.hibernate.Query queryPE = manager.unwrap(Session.class).createSQLQuery(sql)
				.setResultTransformer(new AliasToBeanResultTransformer(HTSIndexDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			queryPE.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			queryPE.setParameter("fromDate",
					Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			queryPE.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		queryPE.setResultTransformer(Transformers.aliasToBean(HTSIndexDto.class));
		List<HTSIndexDto> retPE = queryPE.list();
		return retPE;
	}

	@Override
	public List<HTSIndexDto> getDataHTSIndexModalityCommunityIndexTestingNewPositives(PreventionFilterDto filter) {
		String wherePE = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			wherePE += " AND tbl.c1_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			wherePE += " AND tbl.c11_date >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			wherePE += "  AND tbl.c11_date <=:toDate ";
		}
		String sql = "select tou.id as orgId, " + " tou.code     as orgCode, " + " tou.name     as orgName, "
				+ " pro.name     as provinceName, " + " dis.name     as districtName, "
				+ " 'Community Index Testing'  as modality, "
				+ " count(if(tbl.c3 = 'FEMALE' and tbl.c4_dob is null, 1, null)) as fuNewPositives, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) < 1), 1, null))   as f0NewPositives, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 1) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 5), 1, null)) as f1NewPositives, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 5) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 10), 1, null))     as f5NewPositives, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 10) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 15), 1, null))     as f10NewPositives, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 15) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 20), 1, null))     as f15NewPositives, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 20) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 25), 1, null))     as f20NewPositives, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 25) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 30), 1, null))     as f25NewPositives, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 30) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 35), 1, null))     as f30NewPositives, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 35) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 40), 1, null))     as f35NewPositives, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 40) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 45), 1, null))     as f40NewPositives, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 45) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 50), 1, null))     as f45NewPositives, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 50), 1, null)) as f50NewPositives, "
				+ " count(if(tbl.c3 = 'MALE' and tbl.c4_dob is null, 1, null))   as muNewPositives, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) < 1), 1, null))     as m0NewPositives, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 1) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 5), 1, null)) as m1NewPositives, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 5) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 10), 1, null))     as m5NewPositives, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 10) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 15), 1, null))     as m10NewPositives, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 15) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 20), 1, null))     as m15NewPositives, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 20) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 25), 1, null))     as m20NewPositives, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 25) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 30), 1, null))     as m25NewPositives, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 30) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 35), 1, null))     as m30NewPositives, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 35) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 40), 1, null))     as m35NewPositives, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 40) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 45), 1, null))     as m40NewPositives, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 45) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 50), 1, null))     as m45NewPositives, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 50), 1, null))   as m50NewPositives "
				+ "from tbl_pe_case tbl " + "   inner join tbl_organization_unit tou on tbl.c1_org_id = tou.id "
				+ "   inner join tbl_location tl on tou.address_id = tl.id "
				+ "   inner join tbl_admin_unit pro on tl.province_id = pro.id "
				+ "   inner join tbl_admin_unit dis on tl.district_id = dis.id " 
				+ "where 1 = 1 " 
				+ wherePE
				+ " AND (tbl.c12 = 'answer1' OR tbl.c12 = 'answer2')" 
				+ " AND tbl.c11 = 'YES'" 
				+ " AND (tbl.id in (SELECT DISTINCT(pe_case_id) FROM tbl_pe_case_risk_group WHERE val ='answer5' OR val ='answer6')" 
				+ "  			OR (tbl.c7 IS NOT NULL AND tbl.c7 = 'answer4')) "
				+ "AND tbl.c131_result = 'answer2' AND tbl.c16 IS NOT NULL AND (tbl.c16 = 'answer1' OR tbl.c16 = 'answer3')" 
				+ "group by orgId, orgCode, orgName, provinceName, districtName, modality;";
		org.hibernate.Query queryPE = manager.unwrap(Session.class).createSQLQuery(sql)
				.setResultTransformer(new AliasToBeanResultTransformer(HTSIndexDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			queryPE.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			queryPE.setParameter("fromDate",
					Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			queryPE.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		queryPE.setResultTransformer(Transformers.aliasToBean(HTSIndexDto.class));
		List<HTSIndexDto> retPE = queryPE.list();
		return retPE;
	}

	@Override
	public List<HTSIndexDto> getDataHTSIndexModalityCommunityIndexTestingNewNegatives(PreventionFilterDto filter) {
		String wherePE = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			wherePE += " AND tbl.c1_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			wherePE += " AND tbl.c11_date >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			wherePE += "  AND tbl.c11_date <=:toDate ";
		}
		String sql = "select tou.id as orgId, " + " tou.code     as orgCode, " + " tou.name     as orgName, "
				+ " pro.name     as provinceName, " + " dis.name     as districtName, "
				+ " 'Community Index Testing'  as modality, "
				+ " count(if(tbl.c3 = 'FEMALE' and tbl.c4_dob is null, 1, null)) as fuNewNegatives, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) < 1), 1, null))   as f0NewNegatives, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 1) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 5), 1, null)) as f1NewNegatives, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 5) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 10), 1, null))     as f5NewNegatives, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 10) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 15), 1, null))     as f10NewNegatives, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 15) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 20), 1, null))     as f15NewNegatives, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 20) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 25), 1, null))     as f20NewNegatives, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 25) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 30), 1, null))     as f25NewNegatives, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 30) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 35), 1, null))     as f30NewNegatives, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 35) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 40), 1, null))     as f35NewNegatives, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 40) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 45), 1, null))     as f40NewNegatives, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 45) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 50), 1, null))     as f45NewNegatives, "
				+ " count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 50), 1, null)) as f50NewNegatives, "
				+ " count(if(tbl.c3 = 'MALE' and tbl.c4_dob is null, 1, null))   as muNewNegatives, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) < 1), 1, null))     as m0NewNegatives, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 1) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 5), 1, null)) as m1NewNegatives, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 5) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 10), 1, null))     as m5NewNegatives, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 10) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 15), 1, null))     as m10NewNegatives, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 15) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 20), 1, null))     as m15NewNegatives, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 20) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 25), 1, null))     as m20NewNegatives, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 25) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 30), 1, null))     as m25NewNegatives, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 30) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 35), 1, null))     as m30NewNegatives, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 35) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 40), 1, null))     as m35NewNegatives, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 40) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 45), 1, null))     as m40NewNegatives, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 45) and "
				+ "   (year(curdate()) - year(tbl.c4_dob) < 50), 1, null))     as m45NewNegatives, "
				+ " count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 50), 1, null))   as m50NewNegatives "
				+ "from tbl_pe_case tbl " + "   inner join tbl_organization_unit tou on tbl.c1_org_id = tou.id "
				+ "   inner join tbl_location tl on tou.address_id = tl.id "
				+ "   inner join tbl_admin_unit pro on tl.province_id = pro.id "
				+ "   inner join tbl_admin_unit dis on tl.district_id = dis.id " 
				+ "where 1 = 1 " 
				+ wherePE
				+ " AND (tbl.c12 = 'answer1' OR tbl.c12 = 'answer2')" 
				+ " AND tbl.c11 = 'YES'" 
				+ " AND (tbl.id in (SELECT DISTINCT(pe_case_id) FROM tbl_pe_case_risk_group WHERE val ='answer5' OR val ='answer6')" 
				+ "  			OR (tbl.c7 IS NOT NULL AND tbl.c7 = 'answer4')) "
				+ " AND (tbl.c131_result = 'answer1' OR tbl.c13 = 'answer2') "
				+ " group by orgId, orgCode, orgName, provinceName, districtName, modality;";
		org.hibernate.Query queryPE = manager.unwrap(Session.class).createSQLQuery(sql)
				.setResultTransformer(new AliasToBeanResultTransformer(HTSIndexDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			queryPE.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			queryPE.setParameter("fromDate",
					Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			queryPE.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		queryPE.setResultTransformer(Transformers.aliasToBean(HTSIndexDto.class));
		List<HTSIndexDto> retPE = queryPE.list();
		return retPE;
	}

	@Override
	public List<HTSIndexDto> getDataHTSIndexModalityFacilityIndexTestingOffered(PreventionFilterDto filter) {
		String wherePNS1 = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			wherePNS1 += " AND tbl.c2_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			wherePNS1 += " AND tbl.c5_date_counselling >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			wherePNS1 += "  AND tbl.c5_date_counselling <=:toDate ";
		}
		String sqlPNS1 = "select tou.id as orgId, " + " tou.code as orgCode, " + " tou.name as orgName, "
				+ " pro.name as provinceName, " + " dis.name as districtName, "
				+ " 'Facility Index Testing' as modality, "
				+ " count(if(tbl.c8_gender = 'FEMALE' and tbl.c6_dob is null, 1, null)) as fuOffered, "
				+ " count(if(tbl.c8_gender = 'FEMALE' and (year(curdate()) - year(tbl.c6_dob) < 1), 1, null))   as f0Offered, "
				+ " count(if(tbl.c8_gender = 'FEMALE' and (year(curdate()) - year(tbl.c6_dob) >= 1) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 5), 1, null))      as f1Offered, "
				+ " count(if(tbl.c8_gender = 'FEMALE' and (year(curdate()) - year(tbl.c6_dob) >= 5) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 10), 1, null))     as f5Offered, "
				+ " count(if(tbl.c8_gender = 'FEMALE' and (year(curdate()) - year(tbl.c6_dob) >= 10) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 15), 1, null))     as f10Offered, "
				+ " count(if(tbl.c8_gender = 'FEMALE' and (year(curdate()) - year(tbl.c6_dob) >= 15) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 20), 1, null))     as f15Offered, "
				+ " count(if(tbl.c8_gender = 'FEMALE' and (year(curdate()) - year(tbl.c6_dob) >= 20) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 25), 1, null))     as f20Offered, "
				+ " count(if(tbl.c8_gender = 'FEMALE' and (year(curdate()) - year(tbl.c6_dob) >= 25) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 30), 1, null))     as f25Offered, "
				+ " count(if(tbl.c8_gender = 'FEMALE' and (year(curdate()) - year(tbl.c6_dob) >= 30) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 35), 1, null))     as f30Offered, "
				+ " count(if(tbl.c8_gender = 'FEMALE' and (year(curdate()) - year(tbl.c6_dob) >= 35) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 40), 1, null))     as f35Offered, "
				+ " count(if(tbl.c8_gender = 'FEMALE' and (year(curdate()) - year(tbl.c6_dob) >= 40) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 45), 1, null))     as f40Offered, "
				+ " count(if(tbl.c8_gender = 'FEMALE' and (year(curdate()) - year(tbl.c6_dob) >= 45) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 50), 1, null))     as f45Offered, "
				+ " count(if(tbl.c8_gender = 'FEMALE' and (year(curdate()) - year(tbl.c6_dob) >= 50), 1, null)) as f50Offered, "
				+ " count(if(tbl.c8_gender = 'MALE' and tbl.c6_dob is null, 1, null))   as muOffered, "
				+ " count(if(tbl.c8_gender = 'MALE' and (year(curdate()) - year(tbl.c6_dob) < 1), 1, null))     as m0Offered, "
				+ " count(if(tbl.c8_gender = 'MALE' and (year(curdate()) - year(tbl.c6_dob) >= 1) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 5), 1, null))      as m1Offered, "
				+ " count(if(tbl.c8_gender = 'MALE' and (year(curdate()) - year(tbl.c6_dob) >= 5) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 10), 1, null))     as m5Offered, "
				+ " count(if(tbl.c8_gender = 'MALE' and (year(curdate()) - year(tbl.c6_dob) >= 10) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 15), 1, null))     as m10Offered, "
				+ " count(if(tbl.c8_gender = 'MALE' and (year(curdate()) - year(tbl.c6_dob) >= 15) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 20), 1, null))     as m15Offered, "
				+ " count(if(tbl.c8_gender = 'MALE' and (year(curdate()) - year(tbl.c6_dob) >= 20) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 25), 1, null))     as m20Offered, "
				+ " count(if(tbl.c8_gender = 'MALE' and (year(curdate()) - year(tbl.c6_dob) >= 25) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 30), 1, null))     as m25Offered, "
				+ " count(if(tbl.c8_gender = 'MALE' and (year(curdate()) - year(tbl.c6_dob) >= 30) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 35), 1, null))     as m30Offered, "
				+ " count(if(tbl.c8_gender = 'MALE' and (year(curdate()) - year(tbl.c6_dob) >= 35) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 40), 1, null))     as m35Offered, "
				+ " count(if(tbl.c8_gender = 'MALE' and (year(curdate()) - year(tbl.c6_dob) >= 40) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 45), 1, null))     as m40Offered, "
				+ " count(if(tbl.c8_gender = 'MALE' and (year(curdate()) - year(tbl.c6_dob) >= 45) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 50), 1, null))     as m45Offered, "
				+ " count(if(tbl.c8_gender = 'MALE' and (year(curdate()) - year(tbl.c6_dob) >= 50), 1, null))   as m50Offered "
				+ "from tbl_pns_case tbl " + "   inner join tbl_organization_unit tou on tbl.c2_org_id = tou.id "
				+ "   inner join tbl_location tl on tou.address_id = tl.id "
				+ "   inner join tbl_admin_unit pro on tl.province_id = pro.id "
				+ "   inner join tbl_admin_unit dis on tl.district_id = dis.id " + "where 1 = 1 " + wherePNS1
				+ "group by orgId, orgCode, orgName, provinceName, districtName, modality";
		org.hibernate.Query queryPNS1 = manager.unwrap(Session.class).createSQLQuery(sqlPNS1)
				.setResultTransformer(new AliasToBeanResultTransformer(HTSIndexDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			queryPNS1.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			queryPNS1.setParameter("fromDate",
					Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			queryPNS1.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		List<HTSIndexDto> retPNS1 = queryPNS1.list();
		return retPNS1;
	}

	@Override
	public List<HTSIndexDto> getDataHTSIndexModalityFacilityIndexTestingAccepted(PreventionFilterDto filter) {
		String wherePNS = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			wherePNS += " AND tbl.c2_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			wherePNS += " AND tbl.c6_date_service >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			wherePNS += "  AND tbl.c6_date_service <=:toDate ";
		}
		String sqlPNS = "select tou.id as orgId, " + " tou.code as orgCode, " + " tou.name as orgName, "
				+ " pro.name as provinceName, " + " dis.name as districtName, "
				+ " 'Facility Index Testing' as modality, "
				+ " count(if(tbl.c8_gender = 'FEMALE' and tbl.c6_dob is null, 1, null)) as fuAccepted, "
				+ " count(if(tbl.c8_gender = 'FEMALE' and (year(curdate()) - year(tbl.c6_dob) < 1), 1, null))   as f0Accepted, "
				+ " count(if(tbl.c8_gender = 'FEMALE' and (year(curdate()) - year(tbl.c6_dob) >= 1) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 5), 1, null))      as f1Accepted, "
				+ " count(if(tbl.c8_gender = 'FEMALE' and (year(curdate()) - year(tbl.c6_dob) >= 5) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 10), 1, null))     as f5Accepted, "
				+ " count(if(tbl.c8_gender = 'FEMALE' and (year(curdate()) - year(tbl.c6_dob) >= 10) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 15), 1, null))     as f10Accepted, "
				+ " count(if(tbl.c8_gender = 'FEMALE' and (year(curdate()) - year(tbl.c6_dob) >= 15) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 20), 1, null))     as f15Accepted, "
				+ " count(if(tbl.c8_gender = 'FEMALE' and (year(curdate()) - year(tbl.c6_dob) >= 20) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 25), 1, null))     as f20Accepted, "
				+ " count(if(tbl.c8_gender = 'FEMALE' and (year(curdate()) - year(tbl.c6_dob) >= 25) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 30), 1, null))     as f25Accepted, "
				+ " count(if(tbl.c8_gender = 'FEMALE' and (year(curdate()) - year(tbl.c6_dob) >= 30) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 35), 1, null))     as f30Accepted, "
				+ " count(if(tbl.c8_gender = 'FEMALE' and (year(curdate()) - year(tbl.c6_dob) >= 35) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 40), 1, null))     as f35Accepted, "
				+ " count(if(tbl.c8_gender = 'FEMALE' and (year(curdate()) - year(tbl.c6_dob) >= 40) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 45), 1, null))     as f40Accepted, "
				+ " count(if(tbl.c8_gender = 'FEMALE' and (year(curdate()) - year(tbl.c6_dob) >= 45) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 50), 1, null))     as f45Accepted, "
				+ " count(if(tbl.c8_gender = 'FEMALE' and (year(curdate()) - year(tbl.c6_dob) >= 50), 1, null)) as f50Accepted, "
				+ " count(if(tbl.c8_gender = 'MALE' and tbl.c6_dob is null, 1, null))   as muAccepted, "
				+ " count(if(tbl.c8_gender = 'MALE' and (year(curdate()) - year(tbl.c6_dob) < 1), 1, null))     as m0Accepted, "
				+ " count(if(tbl.c8_gender = 'MALE' and (year(curdate()) - year(tbl.c6_dob) >= 1) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 5), 1, null))      as m1Accepted, "
				+ " count(if(tbl.c8_gender = 'MALE' and (year(curdate()) - year(tbl.c6_dob) >= 5) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 10), 1, null))     as m5Accepted, "
				+ " count(if(tbl.c8_gender = 'MALE' and (year(curdate()) - year(tbl.c6_dob) >= 10) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 15), 1, null))     as m10Accepted, "
				+ " count(if(tbl.c8_gender = 'MALE' and (year(curdate()) - year(tbl.c6_dob) >= 15) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 20), 1, null))     as m15Accepted, "
				+ " count(if(tbl.c8_gender = 'MALE' and (year(curdate()) - year(tbl.c6_dob) >= 20) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 25), 1, null))     as m20Accepted, "
				+ " count(if(tbl.c8_gender = 'MALE' and (year(curdate()) - year(tbl.c6_dob) >= 25) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 30), 1, null))     as m25Accepted, "
				+ " count(if(tbl.c8_gender = 'MALE' and (year(curdate()) - year(tbl.c6_dob) >= 30) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 35), 1, null))     as m30Accepted, "
				+ " count(if(tbl.c8_gender = 'MALE' and (year(curdate()) - year(tbl.c6_dob) >= 35) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 40), 1, null))     as m35Accepted, "
				+ " count(if(tbl.c8_gender = 'MALE' and (year(curdate()) - year(tbl.c6_dob) >= 40) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 45), 1, null))     as m40Accepted, "
				+ " count(if(tbl.c8_gender = 'MALE' and (year(curdate()) - year(tbl.c6_dob) >= 45) and "
				+ "   (year(curdate()) - year(tbl.c6_dob) < 50), 1, null))     as m45Accepted, "
				+ " count(if(tbl.c8_gender = 'MALE' and (year(curdate()) - year(tbl.c6_dob) >= 50), 1, null))   as m50Accepted "
				+ "from tbl_pns_case tbl " + "   inner join tbl_organization_unit tou on tbl.c2_org_id = tou.id "
				+ "   inner join tbl_location tl on tou.address_id = tl.id "
				+ "   inner join tbl_admin_unit pro on tl.province_id = pro.id "
				+ "   inner join tbl_admin_unit dis on tl.district_id = dis.id " + "where 1 = 1 " + wherePNS
				+ "  and tbl.c6_accept_service = 'YES' "
				+ "group by orgId, orgCode, orgName, provinceName, districtName, modality";
		org.hibernate.Query queryPNS1 = manager.unwrap(Session.class).createSQLQuery(sqlPNS)
				.setResultTransformer(new AliasToBeanResultTransformer(HTSIndexDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			queryPNS1.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			queryPNS1.setParameter("fromDate",
					Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			queryPNS1.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		List<HTSIndexDto> retPNS1 = queryPNS1.list();
		return retPNS1;
	}

	@Override
	public List<HTSIndexDto> getDataHTSIndexModalityFacilityIndexTestingContactsElicited(PreventionFilterDto filter) {
		String wherePNSContact = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			wherePNSContact += " AND tpc.c2_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			wherePNSContact += " AND tbl.c1_received_info_date >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			wherePNSContact += "  AND tbl.c1_received_info_date <=:toDate ";
		}
		String sqlPNSContact = "select tou.id      as orgId, " + " tou.code    as orgCode, "
				+ " tou.name    as orgName, " + " pro.name    as provinceName, " + " dis.name    as districtName, "
				+ " 'Facility Index Testing'  as modality, "
				+ " count(if(tp.gender = 'FEMALE' and tp.dob is null, 1, null)) as fuContactsElicited, "
				+ " count(if(tp.gender = 'FEMALE' and (year(curdate()) - year(tp.dob) < 15), 1, null))  as f0ContactsElicited, "
				+ " count(if(tp.gender = 'FEMALE' and (year(curdate()) - year(tp.dob) >= 15), 1, null)) as f15ContactsElicited, "
				+ " count(if(tp.gender = 'MALE' and tp.dob is null, 1, null))   as muContactsElicited, "
				+ " count(if(tp.gender = 'MALE' and (year(curdate()) - year(tp.dob) < 15), 1, null))    as m0ContactsElicited, "
				+ " count(if(tp.gender = 'MALE' and (year(curdate()) - year(tp.dob) >= 15), 1, null))   as m15ContactsElicited "
				+ "from tbl_pns_case_contact tbl " + "   inner join tbl_person tp on tbl.person_id = tp.id "
				+ "   inner join tbl_pns_case tpc on tbl.pns_case_id = tpc.id "
				+ "   inner join tbl_organization_unit tou on tpc.c2_org_id = tou.id "
				+ "   inner join tbl_location tl on tou.address_id = tl.id "
				+ "   inner join tbl_admin_unit pro on tl.province_id = pro.id "
				+ "   inner join tbl_admin_unit dis on tl.district_id = dis.id " + "where  1=1  " + wherePNSContact
				+ "group by orgId, orgCode, orgName, provinceName, districtName, modality";
		org.hibernate.Query queryPNSContact = manager.unwrap(Session.class).createSQLQuery(sqlPNSContact)
				.setResultTransformer(new AliasToBeanResultTransformer(HTSIndexDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			queryPNSContact.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			queryPNSContact.setParameter("fromDate",
					Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			queryPNSContact.setParameter("toDate",
					Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		List<HTSIndexDto> retPNSContact = queryPNSContact.list();
		return retPNSContact;
	}

	@Override
	public List<HTSIndexDto> getDataHTSIndexModalityFacilityIndexTestingKnownPositives(PreventionFilterDto filter) {
		String wherePNSContact = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			wherePNSContact += " AND tpc.c2_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			wherePNSContact += " AND tbl.c1_received_info_date >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			wherePNSContact += "  AND tbl.c1_received_info_date <=:toDate ";
		}
		String sqlPNSContact = "select tou.id      as orgId, " + " tou.code    as orgCode, "
				+ " tou.name    as orgName, " + " pro.name    as provinceName, " + " dis.name    as districtName, "
				+ " 'Facility Index Testing'  as modality, "
				+ " count(if(tp.gender = 'FEMALE' and tp.dob is null, 1, null)) as fuKnownPositives, "
				+ " count(if(tp.gender = 'FEMALE' and (year(curdate()) - year(tp.dob) < 1), 1, null))   as f0KnownPositives, "
				+ " count(if(tp.gender = 'FEMALE' and (year(curdate()) - year(tp.dob) >= 1) and (year(curdate()) - year(tp.dob) < 5), "
				+ "   1, null))   as f1KnownPositives, "
				+ " count(if(tp.gender = 'FEMALE' and (year(curdate()) - year(tp.dob) >= 5) and "
				+ "   (year(curdate()) - year(tp.dob) < 10), 1, null))   as f5KnownPositives, "
				+ " count(if(tp.gender = 'FEMALE' and (year(curdate()) - year(tp.dob) >= 10) and "
				+ "   (year(curdate()) - year(tp.dob) < 15), 1, null))   as f10KnownPositives, "
				+ " count(if(tp.gender = 'FEMALE' and (year(curdate()) - year(tp.dob) >= 15) and "
				+ "   (year(curdate()) - year(tp.dob) < 20), 1, null))   as f15KnownPositives, "
				+ " count(if(tp.gender = 'FEMALE' and (year(curdate()) - year(tp.dob) >= 20) and "
				+ "   (year(curdate()) - year(tp.dob) < 25), 1, null))   as f20KnownPositives, "
				+ " count(if(tp.gender = 'FEMALE' and (year(curdate()) - year(tp.dob) >= 25) and "
				+ "   (year(curdate()) - year(tp.dob) < 30), 1, null))   as f25KnownPositives, "
				+ " count(if(tp.gender = 'FEMALE' and (year(curdate()) - year(tp.dob) >= 30) and "
				+ "   (year(curdate()) - year(tp.dob) < 35), 1, null))   as f30KnownPositives, "
				+ " count(if(tp.gender = 'FEMALE' and (year(curdate()) - year(tp.dob) >= 35) and "
				+ "   (year(curdate()) - year(tp.dob) < 40), 1, null))   as f35KnownPositives, "
				+ " count(if(tp.gender = 'FEMALE' and (year(curdate()) - year(tp.dob) >= 40) and "
				+ "   (year(curdate()) - year(tp.dob) < 45), 1, null))   as f40KnownPositives, "
				+ " count(if(tp.gender = 'FEMALE' and (year(curdate()) - year(tp.dob) >= 45) and "
				+ "   (year(curdate()) - year(tp.dob) < 50), 1, null))   as f45KnownPositives, "
				+ " count(if(tp.gender = 'FEMALE' and (year(curdate()) - year(tp.dob) >= 50), 1, null)) as f50KnownPositives, "
				+ " count(if(tp.gender = 'MALE' and tp.dob is null, 1, null))   as muKnownPositives, "
				+ " count(if(tp.gender = 'MALE' and (year(curdate()) - year(tp.dob) < 1), 1, null))     as m0KnownPositives, "
				+ " count(if(tp.gender = 'MALE' and (year(curdate()) - year(tp.dob) >= 1) and (year(curdate()) - year(tp.dob) < 5), "
				+ "   1, null))   as m1KnownPositives, "
				+ " count(if(tp.gender = 'MALE' and (year(curdate()) - year(tp.dob) >= 5) and (year(curdate()) - year(tp.dob) < 10), "
				+ "   1, null))   as m5KnownPositives, "
				+ " count(if(tp.gender = 'MALE' and (year(curdate()) - year(tp.dob) >= 10) and (year(curdate()) - year(tp.dob) < 15), "
				+ "   1, null))   as m10KnownPositives, "
				+ " count(if(tp.gender = 'MALE' and (year(curdate()) - year(tp.dob) >= 15) and (year(curdate()) - year(tp.dob) < 20), "
				+ "   1, null))   as m15KnownPositives, "
				+ " count(if(tp.gender = 'MALE' and (year(curdate()) - year(tp.dob) >= 20) and (year(curdate()) - year(tp.dob) < 25), "
				+ "   1, null))   as m20KnownPositives, "
				+ " count(if(tp.gender = 'MALE' and (year(curdate()) - year(tp.dob) >= 25) and (year(curdate()) - year(tp.dob) < 30), "
				+ "   1, null))   as m25KnownPositives, "
				+ " count(if(tp.gender = 'MALE' and (year(curdate()) - year(tp.dob) >= 30) and (year(curdate()) - year(tp.dob) < 35), "
				+ "   1, null))   as m30KnownPositives, "
				+ " count(if(tp.gender = 'MALE' and (year(curdate()) - year(tp.dob) >= 35) and (year(curdate()) - year(tp.dob) < 40), "
				+ "   1, null))   as m35KnownPositives, "
				+ " count(if(tp.gender = 'MALE' and (year(curdate()) - year(tp.dob) >= 40) and (year(curdate()) - year(tp.dob) < 45), "
				+ "   1, null))   as m40KnownPositives, "
				+ " count(if(tp.gender = 'MALE' and (year(curdate()) - year(tp.dob) >= 45) and (year(curdate()) - year(tp.dob) < 50), "
				+ "   1, null))   as m45KnownPositives, "
				+ " count(if(tp.gender = 'MALE' and (year(curdate()) - year(tp.dob) >= 50), 1, null))   as m50KnownPositives "
				+ "from tbl_pns_case_contact tbl " + "   inner join tbl_person tp on tbl.person_id = tp.id "
				+ "   inner join tbl_pns_case tpc on tbl.pns_case_id = tpc.id "
				+ "   inner join tbl_organization_unit tou on tpc.c2_org_id = tou.id "
				+ "   inner join tbl_location tl on tou.address_id = tl.id "
				+ "   inner join tbl_admin_unit pro on tl.province_id = pro.id "
				+ "   inner join tbl_admin_unit dis on tl.district_id = dis.id " + "where 1 = 1 " + wherePNSContact
				+ "  and tbl.c2_hiv_status = 'answer1' "
				+ "group by orgId, orgCode, orgName, provinceName, districtName, modality";
		org.hibernate.Query queryPNSContact = manager.unwrap(Session.class).createSQLQuery(sqlPNSContact)
				.setResultTransformer(new AliasToBeanResultTransformer(HTSIndexDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			queryPNSContact.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			queryPNSContact.setParameter("fromDate",
					Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			queryPNSContact.setParameter("toDate",
					Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		List<HTSIndexDto> retPNSContact = queryPNSContact.list();
		return retPNSContact;
	}

	@Override
	public List<HTSIndexDto> getDataHTSIndexModalityFacilityIndexTestingDocumentedNegatives(
			PreventionFilterDto filter) {
		String wherePNSContact = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			wherePNSContact += " AND tpc.c2_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			wherePNSContact += " AND tbl.c1_received_info_date >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			wherePNSContact += "  AND tbl.c1_received_info_date <=:toDate ";
		}
		String sqlPNSContact = "select tou.id  as orgId, " + " tou.code      as orgCode, "
				+ " tou.name      as orgName, " + " pro.name      as provinceName, "
				+ " dis.name      as districtName, " + " 'Facility Index Testing'   as modality, "
				+ " count(if(tp.gender = 'FEMALE' and (year(curdate()) - year(tp.dob) >= 1) "
				+ " and (year(curdate()) - year(tp.dob) < 5), 1, null))  as f1DocumentedNegatives, "
				+ " count(if(tp.gender = 'FEMALE' and (year(curdate()) - year(tp.dob) >= 5) "
				+ " and (year(curdate()) - year(tp.dob) < 10), 1, null)) as f5DocumentedNegatives, "
				+ " count(if(tp.gender = 'FEMALE' and (year(curdate()) - year(tp.dob) >= 10) "
				+ " and (year(curdate()) - year(tp.dob) < 15), 1, null)) as f10DocumentedNegatives, "
				+ " count(if(tp.gender = 'MALE' and (year(curdate()) - year(tp.dob) >= 1) "
				+ " and (year(curdate()) - year(tp.dob) < 5), 1, null))  as m1DocumentedNegatives, "
				+ " count(if(tp.gender = 'MALE' and (year(curdate()) - year(tp.dob) >= 5) "
				+ " and (year(curdate()) - year(tp.dob) < 10), 1, null)) as m5DocumentedNegatives, "
				+ " count(if(tp.gender = 'MALE' and (year(curdate()) - year(tp.dob) >= 10) "
				+ " and (year(curdate()) - year(tp.dob) < 15), 1, null)) as m10DocumentedNegatives "
				+ "from tbl_pns_case_contact tbl " + "   inner join tbl_person tp on tbl.person_id = tp.id "
				+ "   inner join tbl_pns_case tpc on tbl.pns_case_id = tpc.id "
				+ "   inner join tbl_organization_unit tou on tpc.c2_org_id = tou.id "
				+ "   inner join tbl_location tl on tou.address_id = tl.id "
				+ "   inner join tbl_admin_unit pro on tl.province_id = pro.id "
				+ "   inner join tbl_admin_unit dis on tl.district_id = dis.id " + "where 1 = 1 " + wherePNSContact
				+ "  and tbl.c2_hiv_status = 'answer2' "
				+ "group by orgId, orgCode, orgName, provinceName, districtName, modality";
		org.hibernate.Query queryPNSContact = manager.unwrap(Session.class).createSQLQuery(sqlPNSContact)
				.setResultTransformer(new AliasToBeanResultTransformer(HTSIndexDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			queryPNSContact.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			queryPNSContact.setParameter("fromDate",
					Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			queryPNSContact.setParameter("toDate",
					Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		List<HTSIndexDto> retPNSContact = queryPNSContact.list();
		return retPNSContact;
	}

	@Override
	public List<HTSIndexDto> getDataHTSIndexModalityFacilityIndexTestingNewPositives(PreventionFilterDto filter) {
		String whereClause = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			whereClause += " AND tbl.c2_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			whereClause += " AND tbl.c15_date >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			whereClause += "  AND tbl.c15_date <=:toDate ";
		}
		String sql = "select tou.id as orgId, " + " tou.code as orgCode, " + " tou.name as orgName, "
				+ " pro.name as provinceName, " + " dis.name as districtName, "
				+ " 'Facility Index Testing' as modality, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and tbl.c8_dob is null, 1, null)) as fuNewPositives, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) < 1), 1, null))   as f0NewPositives, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 1) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 5), 1, null))      as f1NewPositives, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 5) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 10), 1, null))     as f5NewPositives, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 10) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 15), 1, null))     as f10NewPositives, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 15) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 20), 1, null))     as f15NewPositives, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 20) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 25), 1, null))     as f20NewPositives, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 25) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 30), 1, null))     as f25NewPositives, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 30) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 35), 1, null))     as f30NewPositives, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 35) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 40), 1, null))     as f35NewPositives, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 40) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 45), 1, null))     as f40NewPositives, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 45) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 50), 1, null))     as f45NewPositives, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 50), 1, null)) as f50NewPositives, "
				+ " count(if(tbl.c7_gender = 'MALE' and tbl.c8_dob is null, 1, null))   as muNewPositives, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) < 1), 1, null))     as m0NewPositives, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 1) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 5), 1, null))      as m1NewPositives, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 5) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 10), 1, null))     as m5NewPositives, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 10) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 15), 1, null))     as m10NewPositives, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 15) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 20), 1, null))     as m15NewPositives, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 20) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 25), 1, null))     as m20NewPositives, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 25) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 30), 1, null))     as m25NewPositives, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 30) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 35), 1, null))     as m30NewPositives, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 35) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 40), 1, null))     as m35NewPositives, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 40) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 45), 1, null))     as m40NewPositives, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 45) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 50), 1, null))     as m45NewPositives, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 50), 1, null))   as m50NewPositives "
				+ " " + " from tbl_hts_case tbl " + "   inner join tbl_organization_unit tou on tbl.c2_org_id = tou.id "
				+ "   inner join tbl_location tl on tou.address_id = tl.id "
				+ "   inner join tbl_admin_unit pro on tl.province_id = pro.id "
				+ "   inner join tbl_admin_unit dis on tl.district_id = dis.id " + " where 1 = 1 " + whereClause +
//   "  and (tbl.c5 = 'answer1' OR tbl.c5 = 'answer2') "+ 
				"  and (tbl.id in (select hts_case_id from tbl_hts_case_risk_group where val = 'answer5' or val = 'answer6') or tbl.c10 = 'answer2')"
				+ "  and tbl.c15 = 'YES' " +
//   !((c24 = 2 AND C24 is not null) OR (c11c!=null AND c11c = 2 AND c11b !=null AND c11b != 1) )   
				" AND ((tbl.c24 = 'answer1' OR tbl.c24 = 'answer3') AND (tbl.c11c IS NULL OR tbl.c11c='NO')) "
				+ " group by orgId, orgCode, orgName, provinceName, districtName, modality";
		org.hibernate.Query query = manager.unwrap(Session.class).createSQLQuery(sql)
				.setResultTransformer(new AliasToBeanResultTransformer(HTSIndexDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			query.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			query.setParameter("fromDate", Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			query.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		List<HTSIndexDto> ret = query.list();
		return ret;
	}

	@Override
	public List<HTSIndexDto> getDataHTSIndexModalityFacilityIndexTestingNewNegatives(PreventionFilterDto filter) {
		String whereClause = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			whereClause += " AND tbl.c2_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			whereClause += " AND tbl.c15_date >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			whereClause += "  AND tbl.c15_date <=:toDate ";
		}
		String sql = "select tou.id as orgId, " + " tou.code as orgCode, " + " tou.name as orgName, "
				+ " pro.name as provinceName, " + " dis.name as districtName, "
				+ " 'Facility Index Testing' as modality, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and tbl.c8_dob is null, 1, null)) as fuNewNegatives, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) < 1), 1, null))   as f0NewNegatives, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 1) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 5), 1, null))      as f1NewNegatives, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 5) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 10), 1, null))     as f5NewNegatives, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 10) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 15), 1, null))     as f10NewNegatives, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 15) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 20), 1, null))     as f15NewNegatives, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 20) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 25), 1, null))     as f20NewNegatives, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 25) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 30), 1, null))     as f25NewNegatives, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 30) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 35), 1, null))     as f30NewNegatives, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 35) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 40), 1, null))     as f35NewNegatives, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 40) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 45), 1, null))     as f40NewNegatives, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 45) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 50), 1, null))     as f45NewNegatives, "
				+ " count(if(tbl.c7_gender = 'FEMALE' and (year(curdate()) - year(tbl.c8_dob) >= 50), 1, null)) as f50NewNegatives, "
				+ " count(if(tbl.c7_gender = 'MALE' and tbl.c8_dob is null, 1, null))   as muNewNegatives, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) < 1), 1, null))     as m0NewNegatives, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 1) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 5), 1, null))      as m1NewNegatives, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 5) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 10), 1, null))     as m5NewNegatives, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 10) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 15), 1, null))     as m10NewNegatives, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 15) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 20), 1, null))     as m15NewNegatives, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 20) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 25), 1, null))     as m20NewNegatives, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 25) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 30), 1, null))     as m25NewNegatives, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 30) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 35), 1, null))     as m30NewNegatives, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 35) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 40), 1, null))     as m35NewNegatives, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 40) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 45), 1, null))     as m40NewNegatives, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 45) and "
				+ "   (year(curdate()) - year(tbl.c8_dob) < 50), 1, null))     as m45NewNegatives, "
				+ " count(if(tbl.c7_gender = 'MALE' and (year(curdate()) - year(tbl.c8_dob) >= 50), 1, null))   as m50NewNegatives "
				+ " from tbl_hts_case tbl " + "   inner join tbl_organization_unit tou on tbl.c2_org_id = tou.id "
				+ "   inner join tbl_location tl on tou.address_id = tl.id "
				+ "   inner join tbl_admin_unit pro on tl.province_id = pro.id "
				+ "   inner join tbl_admin_unit dis on tl.district_id = dis.id " + "  where 1 = 1 " + whereClause +
//   "  and (tbl.c5 = 'answer1' OR tbl.c5 = 'answer2') "+ 
				"  and (tbl.id in (select hts_case_id from tbl_hts_case_risk_group where val = 'answer5' or val = 'answer6') or tbl.c10 = 'answer2')"
				+ "  and tbl.c15 = 'YES' " + "  and tbl.c14 = 'answer1' "
				+ "  group by orgId, orgCode, orgName, provinceName, districtName, modality";
		org.hibernate.Query query = manager.unwrap(Session.class).createSQLQuery(sql)
				.setResultTransformer(new AliasToBeanResultTransformer(HTSIndexDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			query.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			query.setParameter("fromDate", Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			query.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		List<HTSIndexDto> ret = query.list();
		return ret;
	}

	@Override
	public List<ReportMERPEDto> getDataKPPREV(PreventionFilterDto filter) {
		String wherePE = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			wherePE += " AND tbl.c1_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			wherePE += " AND tbl.c1 >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			wherePE += "  AND tbl.c1 <=:toDate ";
		}
		String sql = "select tou.id                                                   as orgId," +
				"       tou.code                                                      as orgCode," +
				"       tou.name                                                      as orgName," +
				"       pro.name                                                      as provinceName," +
				"       dis.name                                                      as districtName," +
				"       count(if(tbl.val = 'answer1', 1, null))                       as pwid," +
				"       count(if(tbl.val = 'answer2', 1, null))                       as msm," +
				"       count(if(tbl.val = 'answer4', 1, null))                       as tg," +
				"       count(if(tbl.val = 'answer3' and tbl.c3 = 'FEMALE', 1, null)) as fsw," +
				"       count(if(tbl.val = 'answer0', 1, null))                       as other " +
				"from (select tpcrg.val, tpc.*" +
				"      from tbl_pe_case_risk_group tpcrg" +
				"               inner join tbl_pe_case tpc on tpcrg.pe_case_id = tpc.id" +
				"          AND tpcrg.is_main_risk = 1)" +
				"         as tbl" +
				"         inner join tbl_organization_unit tou on tbl.c1_org_id = tou.id" +
				"         inner join tbl_location tl on tou.address_id = tl.id" +
				"         inner join tbl_admin_unit pro on tl.province_id = pro.id" +
				"         inner join tbl_admin_unit dis on tl.district_id = dis.id " +
				"where 1 = 1 and tbl.c8 is not null " + wherePE +
				"  and (tbl.val = 'answer1' or tbl.val = 'answer2' or tbl.val = 'answer4' or (tbl.val = 'answer3' and tbl.c3 = 'FEMALE')) " +
				"group by orgId, orgCode, orgName, provinceName, districtName";
		org.hibernate.Query queryPE = manager.unwrap(Session.class)
				.createSQLQuery(sql).setResultTransformer(new AliasToBeanResultTransformer(ReportMERPEDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			queryPE.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			queryPE.setParameter("fromDate",
					Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			queryPE.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		queryPE.setResultTransformer(Transformers.aliasToBean(ReportMERPEDto.class));
		List<ReportMERPEDto> retPE = queryPE.list();
		return retPE;
	}

	@Override
	public List<ReportMERPEDto> getDataKPPREVC8Positives(PreventionFilterDto filter) {
		String wherePE = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			wherePE += " AND tbl.c1_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			wherePE += " AND tbl.c1 >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			wherePE += "  AND tbl.c1 <=:toDate ";
		}
		String sql = "select tou.id                                                   as orgId," +
				"       tou.code                                                      as orgCode," +
				"       tou.name                                                      as orgName," +
				"       pro.name                                                      as provinceName," +
				"       dis.name                                                      as districtName," +
				"       count(if(tbl.val = 'answer1', 1, null))                       as pwidC8Positives," +
				"       count(if(tbl.val = 'answer2', 1, null))                       as msmC8Positives," +
				"       count(if(tbl.val = 'answer4', 1, null))                       as tgC8Positives," +
				"       count(if(tbl.val = 'answer3' and tbl.c3 = 'FEMALE', 1, null)) as fswC8Positives," +
				"       count(if(tbl.val = 'answer0', 1, null))                       as otherC8Positives " +
				"from (select tpcrg.val, tpc.*" +
				"      from tbl_pe_case_risk_group tpcrg" +
				"               inner join tbl_pe_case tpc on tpcrg.pe_case_id = tpc.id" +
				"          AND tpcrg.is_main_risk = 1)" +
				"         as tbl" +
				"         inner join tbl_organization_unit tou on tbl.c1_org_id = tou.id" +
				"         inner join tbl_location tl on tou.address_id = tl.id" +
				"         inner join tbl_admin_unit pro on tl.province_id = pro.id" +
				"         inner join tbl_admin_unit dis on tl.district_id = dis.id " +
				"where 1 = 1" + wherePE +
				"  and (tbl.val = 'answer1' or tbl.val = 'answer2' or tbl.val = 'answer4' or (tbl.val = 'answer3' and tbl.c3 = 'FEMALE')) " +
				"  and (tbl.c8 = 'answer1') "+
				"group by orgId, orgCode, orgName, provinceName, districtName";
		org.hibernate.Query queryPE = manager.unwrap(Session.class)
				.createSQLQuery(sql).setResultTransformer(new AliasToBeanResultTransformer(ReportMERPEDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			queryPE.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			queryPE.setParameter("fromDate",
					Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			queryPE.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		queryPE.setResultTransformer(Transformers.aliasToBean(ReportMERPEDto.class));
		List<ReportMERPEDto> retPE = queryPE.list();
		return retPE;
	}

	@Override
	public List<ReportMERPEDto> getDataKPPREVC11Yes(PreventionFilterDto filter) {
		String wherePE = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			wherePE += " AND tbl.c1_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			wherePE += " AND tbl.c1 >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			wherePE += "  AND tbl.c1 <=:toDate ";
		}
		String sql = "select tou.id                                                        as orgId," +
				"       tou.code                                                      as orgCode," +
				"       tou.name                                                      as orgName," +
				"       pro.name                                                      as provinceName," +
				"       dis.name                                                      as districtName," +
				"       count(if(tbl.val = 'answer1', 1, null))                       as pwidC11Yes," +
				"       count(if(tbl.val = 'answer2', 1, null))                       as msmC11Yes," +
				"       count(if(tbl.val = 'answer4', 1, null))                       as tgC11Yes," +
				"       count(if(tbl.val = 'answer3' and tbl.c3 = 'FEMALE', 1, null)) as fswC11Yes," +
				"       count(if(tbl.val = 'answer0', 1, null))                       as otherC11Yes " +
				"from (select tpcrg.val, tpc.*" +
				"      from tbl_pe_case_risk_group tpcrg" +
				"               inner join tbl_pe_case tpc on tpcrg.pe_case_id = tpc.id" +
				"          AND tpcrg.is_main_risk = 1)" +
				"         as tbl" +
				"         inner join tbl_organization_unit tou on tbl.c1_org_id = tou.id" +
				"         inner join tbl_location tl on tou.address_id = tl.id" +
				"         inner join tbl_admin_unit pro on tl.province_id = pro.id" +
				"         inner join tbl_admin_unit dis on tl.district_id = dis.id " +
				"where 1 = 1" + wherePE +
				"  and (tbl.val = 'answer1' or tbl.val = 'answer2' or tbl.val = 'answer4' or (tbl.val = 'answer3' and tbl.c3 = 'FEMALE')) " +
				"  and (tbl.c8 = 'answer2' or tbl.c8 = 'answer3') "+
				"  and (tbl.c11 = 'YES') "+
				"group by orgId, orgCode, orgName, provinceName, districtName";
		org.hibernate.Query queryPE = manager.unwrap(Session.class)
				.createSQLQuery(sql).setResultTransformer(new AliasToBeanResultTransformer(ReportMERPEDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			queryPE.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			queryPE.setParameter("fromDate",
					Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			queryPE.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		queryPE.setResultTransformer(Transformers.aliasToBean(ReportMERPEDto.class));
		List<ReportMERPEDto> retPE = queryPE.list();
		return retPE;
	}

	@Override
	public List<ReportMERPEDto> getDataKPPREVC11No(PreventionFilterDto filter) {
		String wherePE = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			wherePE += " AND tbl.c1_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			wherePE += " AND tbl.c1 >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			wherePE += "  AND tbl.c1 <=:toDate ";
		}
		String sql = "select tou.id                                                        as orgId," +
				"       tou.code                                                      as orgCode," +
				"       tou.name                                                      as orgName," +
				"       pro.name                                                      as provinceName," +
				"       dis.name                                                      as districtName," +
				"       count(if(tbl.val = 'answer1', 1, null))                       as pwidC11No," +
				"       count(if(tbl.val = 'answer2', 1, null))                       as msmC11No," +
				"       count(if(tbl.val = 'answer4', 1, null))                       as tgC11No," +
				"       count(if(tbl.val = 'answer3' and tbl.c3 = 'FEMALE', 1, null)) as fswC11No," +
				"       count(if(tbl.val = 'answer0', 1, null))                       as otherC11No " +
				"from (select tpcrg.val, tpc.*" +
				"      from tbl_pe_case_risk_group tpcrg" +
				"               inner join tbl_pe_case tpc on tpcrg.pe_case_id = tpc.id" +
				"          AND tpcrg.is_main_risk = 1)" +
				"         as tbl" +
				"         inner join tbl_organization_unit tou on tbl.c1_org_id = tou.id" +
				"         inner join tbl_location tl on tou.address_id = tl.id" +
				"         inner join tbl_admin_unit pro on tl.province_id = pro.id" +
				"         inner join tbl_admin_unit dis on tl.district_id = dis.id " +
				"where 1 = 1" + wherePE +
				"  and (tbl.val = 'answer1' or tbl.val = 'answer2' or tbl.val = 'answer4' or (tbl.val = 'answer3' and tbl.c3 = 'FEMALE')) " +
				"  and (tbl.c8 = 'answer2' or tbl.c8 = 'answer3') "+
				"  and (tbl.c11 = 'NO') "+
				"group by orgId, orgCode, orgName, provinceName, districtName";
		org.hibernate.Query queryPE = manager.unwrap(Session.class)
				.createSQLQuery(sql).setResultTransformer(new AliasToBeanResultTransformer(ReportMERPEDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			queryPE.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			queryPE.setParameter("fromDate",
					Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			queryPE.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		queryPE.setResultTransformer(Transformers.aliasToBean(ReportMERPEDto.class));
		List<ReportMERPEDto> retPE = queryPE.list();
		return retPE;
	}

	@Override
	public List<ReportMERPEDto> getDataPPPREVTesting(PreventionFilterDto filter) {
		String wherePE = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			wherePE += " AND tbl.c1_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			wherePE += " AND tbl.c1 >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			wherePE += "  AND tbl.c1 <=:toDate ";
		}
		String sql = "select tou.id                                                                          as orgId,\n" +
				"       tou.code                                                                             as orgCode,\n" +
				"       tou.name                                                                             as orgName,\n" +
				"       pro.name                                                                             as provinceName,\n" +
				"       dis.name                                                                             as districtName,\n" +
				"       count(if(tbl.c8 = 'answer1', 1, null))                                               as testingC8Positives,\n" +
				"       count(if((tbl.c8 = 'answer2' or tbl.c8 = 'answer3') and (tbl.c11 = 'YES'), 1, null)) as testingC11Yes,\n" +
				"       count(if((tbl.c8 = 'answer2' or tbl.c8 = 'answer3') and (tbl.c11 = 'NO'), 1, null))  as testingC11No\n" +
				"from (select tpcrg.val, tpc.*\n" +
				"      from tbl_pe_case_risk_group tpcrg\n" +
				"               inner join tbl_pe_case tpc on tpcrg.pe_case_id = tpc.id\n" +
				"          AND tpcrg.is_main_risk = 1)\n" +
				"         as tbl\n" +
				"         inner join tbl_organization_unit tou on tbl.c1_org_id = tou.id\n" +
				"         inner join tbl_location tl on tou.address_id = tl.id\n" +
				"         inner join tbl_admin_unit pro on tl.province_id = pro.id\n" +
				"         inner join tbl_admin_unit dis on tl.district_id = dis.id\n" +
				"where 1 = 1  \n" + wherePE +
				"  and not (tbl.val = 'answer1' or tbl.val = 'answer2' or tbl.val = 'answer4' or\n" +
				"           (tbl.val = 'answer3' and tbl.c3 = 'FEMALE'))\n" +
				"group by orgId, orgCode, orgName, provinceName, districtName";
		org.hibernate.Query queryPE = manager.unwrap(Session.class)
				.createSQLQuery(sql).setResultTransformer(new AliasToBeanResultTransformer(ReportMERPEDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			queryPE.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			queryPE.setParameter("fromDate",
					Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			queryPE.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		queryPE.setResultTransformer(Transformers.aliasToBean(ReportMERPEDto.class));
		List<ReportMERPEDto> retPE = queryPE.list();
		return retPE;
	}

	@Override
	public List<ReportMERPEDto> getDataPPPREVPriority(PreventionFilterDto filter) {
		String wherePE = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			wherePE += " AND tbl.c1_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			wherePE += " AND tbl.c1 >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			wherePE += "  AND tbl.c1 <=:toDate ";
		}
		String sql = "select tou.id                                                                          as orgId,\n" +
				"       tou.code                                                                             as orgCode,\n" +
				"       tou.name                                                                             as orgName,\n" +
				"       pro.name                                                                             as provinceName,\n" +
				"       dis.name                                                                             as districtName,\n" +
				"       count(tbl.id) 																		 as priorityOther\n" +
				"from (select tpcrg.val, tpc.*\n" +
				"      from tbl_pe_case_risk_group tpcrg\n" +
				"               inner join tbl_pe_case tpc on tpcrg.pe_case_id = tpc.id\n" +
				"          AND tpcrg.is_main_risk = 1)\n" +
				"         as tbl\n" +
				"         inner join tbl_organization_unit tou on tbl.c1_org_id = tou.id\n" +
				"         inner join tbl_location tl on tou.address_id = tl.id\n" +
				"         inner join tbl_admin_unit pro on tl.province_id = pro.id\n" +
				"         inner join tbl_admin_unit dis on tl.district_id = dis.id\n" +
				"where 1 = 1 and tbl.c8 is not null \n" + wherePE +
				"  and not (tbl.val = 'answer1' or tbl.val = 'answer2' or tbl.val = 'answer4' or\n" +
				"           (tbl.val = 'answer3' and tbl.c3 = 'FEMALE'))\n" +
				"group by orgId, orgCode, orgName, provinceName, districtName";
		org.hibernate.Query queryPE = manager.unwrap(Session.class)
				.createSQLQuery(sql).setResultTransformer(new AliasToBeanResultTransformer(ReportMERPEDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			queryPE.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			queryPE.setParameter("fromDate",
					Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			queryPE.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		queryPE.setResultTransformer(Transformers.aliasToBean(ReportMERPEDto.class));
		List<ReportMERPEDto> retPE = queryPE.list();
		return retPE;
	}

	@Override
	public List<ReportMERPEDto> getDataPPPREVAgeAndSex(PreventionFilterDto filter) {
		String wherePE = "";
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			wherePE += " AND tbl.c1_org_id in (:listOrg) ";
		}
		if (filter.getFromDate() != null) {
			wherePE += " AND tbl.c1 >=:fromDate ";
		}
		if (filter.getToDate() != null) {
			wherePE += "  AND tbl.c1 <=:toDate ";
		}
		String sql = "select tou.id                                                                               as orgId,\n" +
				"       tou.code                                                                             as orgCode,\n" +
				"       tou.name                                                                             as orgName,\n" +
				"       pro.name                                                                             as provinceName,\n" +
				"       dis.name                                                                             as districtName,\n" +
				"       count(if(tbl.c3 = 'FEMALE' and tbl.c4_dob is null, 1, null))                         as fu,\n" +
				"       count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 10) and\n" +
				"                (year(curdate()) - year(tbl.c4_dob) < 15), 1, null))                        as f10,\n" +
				"       count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 15) and\n" +
				"                (year(curdate()) - year(tbl.c4_dob) < 20), 1, null))                        as f15,\n" +
				"       count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 20) and\n" +
				"                (year(curdate()) - year(tbl.c4_dob) < 25), 1, null))                        as f20,\n" +
				"       count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 25) and\n" +
				"                (year(curdate()) - year(tbl.c4_dob) < 30), 1, null))                        as f25,\n" +
				"       count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 30) and\n" +
				"                (year(curdate()) - year(tbl.c4_dob) < 35), 1, null))                        as f30,\n" +
				"       count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 35) and\n" +
				"                (year(curdate()) - year(tbl.c4_dob) < 40), 1, null))                        as f35,\n" +
				"       count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 40) and\n" +
				"                (year(curdate()) - year(tbl.c4_dob) < 45), 1, null))                        as f40,\n" +
				"       count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 45) and\n" +
				"                (year(curdate()) - year(tbl.c4_dob) < 50), 1, null))                        as f45,\n" +
				"       count(if(tbl.c3 = 'FEMALE' and (year(curdate()) - year(tbl.c4_dob) >= 50), 1, null)) as f50,\n" +
				"\n" +
				"       count(if(tbl.c3 = 'MALE' and tbl.c4_dob is null, 1, null))                           as mu,\n" +
				"       count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 10) and\n" +
				"                (year(curdate()) - year(tbl.c4_dob) < 15), 1, null))                        as m10,\n" +
				"       count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 15) and\n" +
				"                (year(curdate()) - year(tbl.c4_dob) < 20), 1, null))                        as m15,\n" +
				"       count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 20) and\n" +
				"                (year(curdate()) - year(tbl.c4_dob) < 25), 1, null))                        as m20,\n" +
				"       count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 25) and\n" +
				"                (year(curdate()) - year(tbl.c4_dob) < 30), 1, null))                        as m25,\n" +
				"       count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 30) and\n" +
				"                (year(curdate()) - year(tbl.c4_dob) < 35), 1, null))                        as m30,\n" +
				"       count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 35) and\n" +
				"                (year(curdate()) - year(tbl.c4_dob) < 40), 1, null))                        as m35,\n" +
				"       count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 40) and\n" +
				"                (year(curdate()) - year(tbl.c4_dob) < 45), 1, null))                        as m40,\n" +
				"       count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 45) and\n" +
				"                (year(curdate()) - year(tbl.c4_dob) < 50), 1, null))                        as m45,\n" +
				"       count(if(tbl.c3 = 'MALE' and (year(curdate()) - year(tbl.c4_dob) >= 50), 1, null))   as m50\n" +
				"from (select tpcrg.val, tpc.*\n" +
				"      from tbl_pe_case_risk_group tpcrg\n" +
				"               inner join tbl_pe_case tpc on tpcrg.pe_case_id = tpc.id\n" +
				"          AND tpcrg.is_main_risk = 1)\n" +
				"         as tbl\n" +
				"         inner join tbl_organization_unit tou on tbl.c1_org_id = tou.id\n" +
				"         inner join tbl_location tl on tou.address_id = tl.id\n" +
				"         inner join tbl_admin_unit pro on tl.province_id = pro.id\n" +
				"         inner join tbl_admin_unit dis on tl.district_id = dis.id\n" +
				"where 1 = 1 and tbl.c8 is not null \n" + wherePE +
				"  and not (tbl.val = 'answer1' or tbl.val = 'answer2' or tbl.val = 'answer4' or\n" +
				"           (tbl.val = 'answer3' and tbl.c3 = 'FEMALE'))\n" +
				"group by orgId, orgCode, orgName, provinceName, districtName";
		org.hibernate.Query queryPE = manager.unwrap(Session.class)
				.createSQLQuery(sql).setResultTransformer(new AliasToBeanResultTransformer(ReportMERPEDto.class));
		if (!CollectionUtils.isEmpty(filter.getOrgIds())) {
			queryPE.setParameterList("listOrg", filter.getOrgIds());
		}
		if (filter.getFromDate() != null) {
			queryPE.setParameter("fromDate",
					Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (filter.getToDate() != null) {
			queryPE.setParameter("toDate", Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		queryPE.setResultTransformer(Transformers.aliasToBean(ReportMERPEDto.class));
		List<ReportMERPEDto> retPE = queryPE.list();
		return retPE;
	}
}
