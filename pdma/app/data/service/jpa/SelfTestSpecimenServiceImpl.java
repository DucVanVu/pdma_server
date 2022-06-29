package org.pepfar.pdma.app.data.service.jpa;

import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.hibernate.Session;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.hibernate.transform.Transformers;
import org.joda.time.LocalDateTime;
import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.domain.User;
import org.pepfar.pdma.app.data.dto.*;
import org.pepfar.pdma.app.data.repository.AdminUnitRepository;
import org.pepfar.pdma.app.data.repository.LocationRepository;
import org.pepfar.pdma.app.data.repository.OrganizationRepository;
import org.pepfar.pdma.app.data.repository.PersonRepository;
import org.pepfar.pdma.app.data.repository.SelfTestEntryRepository;
import org.pepfar.pdma.app.data.repository.SelfTestSpecimenRepository;
import org.pepfar.pdma.app.data.repository.StaffRepository;
import org.pepfar.pdma.app.data.service.SelfTestSpecimenService;
import org.pepfar.pdma.app.data.types.Gender;
import org.pepfar.pdma.app.data.types.PEApproachMethod;
import org.pepfar.pdma.app.data.types.Permission;
import org.pepfar.pdma.app.utils.AuthorizationUtils;
import org.pepfar.pdma.app.utils.SecurityUtils;
import org.postgresql.core.NativeQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class SelfTestSpecimenServiceImpl implements SelfTestSpecimenService {

	@Autowired
	private SelfTestEntryRepository repos;

	@Autowired
	private SelfTestSpecimenRepository specRepos;

	@Autowired
	private OrganizationRepository orgRepos;

	@Autowired
	private StaffRepository staffRepos;

	@Autowired
	private PersonRepository personRepos;

	@Autowired
	private LocationRepository locationRepos;

	@Autowired
	private AdminUnitRepository adminUnitRepos;

	@Autowired
	private AuthorizationUtils authUtils;

	@Autowired
	private ApplicationContext context;

	@Autowired
	private OrganizationRepository organizationRepository;
	
	@Autowired
	public EntityManager manager;

	@Override
	public PreventionReportDto<SelfTestDetailReportDto> getReport(PreventionFilterDto filter) {
		if (filter != null && filter.getFromDate() != null && filter.getToDate() != null
				&& filter.getFromDate().isBefore(filter.getToDate()) && filter.getOrgIds() != null
				&& filter.getOrgIds().size() > 0) {

			User currentUser = SecurityUtils.getCurrentUser();
			Boolean isAdministrator = SecurityUtils.isUserInRole(currentUser, "ROLE_ADMIN");
			
			PreventionReportDto<SelfTestDetailReportDto> ret = new PreventionReportDto<SelfTestDetailReportDto>();
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
			//Tổng số sinh phẩm được phát cho khách hàng
			SelfTestDetailReportDto details= this.getReportDetail(filter,"I","Tổng số sinh phẩm được phát cho khách hàng",0);
			ret.getListDetail().add(details);
			//Người TCMT
			details= this.getReportDetail(filter, "II.1", "Người TCMT", 1);
			ret.getListDetail().add(details);
			//Nam có QHTD đồng giới
			details= this.getReportDetail(filter, "II.2", "Nam có QHTD đồng giới", 2);
			ret.getListDetail().add(details);
			//Phụ nữ bán dâm
			details= this.getReportDetail(filter, "II.3", "Phụ nữ bán dâm", 3);
			ret.getListDetail().add(details);
			//chuyển giới
			details= this.getReportDetail(filter, "II.4", "Chuyển giới", 3);
			ret.getListDetail().add(details);
			//Bạn tình, bạn chích chung của người có HIV
			details= this.getReportDetail(filter, "II.5", "Bạn tình, bạn chích chung của người có HIV", 4);
			ret.getListDetail().add(details);
			//Khác
			details= this.getReportDetail(filter, "II.6", "Khác", 5);
			ret.getListDetail().add(details);
			//Bản thân
			details= this.getReportDetail(filter, "III.1", "Bản thân", 6);
			ret.getListDetail().add(details);
			//Bạn tình
			details= this.getReportDetail(filter, "III.2", "Bạn tình", 7);
			ret.getListDetail().add(details);
			//Bạn chích chung
			details= this.getReportDetail(filter, "III.3", "Bạn chích chung", 8);
			ret.getListDetail().add(details);
			//Bạn khác
			details= this.getReportDetail(filter, "III.4", "Bạn khác", 9);
			ret.getListDetail().add(details);
			//Không rõ giới tính và nhóm tuổi
			details= this.getReportDetail(filter, "IV.1", "Không rõ giới tính và nhóm tuổi", 10);
			ret.getListDetail().add(details);
			//Nam
			details= this.getReportDetail(filter, "IV.2", "Nam", 11);
			ret.getListDetail().add(details);
			//Không rõ nhóm tuổi
			details= this.getReportDetail(filter, "IV.3", "Không rõ nhóm tuổi", 12);
			ret.getListDetail().add(details);
			//10-14
			details= this.getReportDetail(filter, "IV.4", "10-14", 13);
			ret.getListDetail().add(details);
			//15-19
			details= this.getReportDetail(filter, "IV.5", "15-19", 14);
			ret.getListDetail().add(details);
			//20-24
			details= this.getReportDetail(filter, "IV.6", "20-24", 15);
			ret.getListDetail().add(details);
			//25-29
			details= this.getReportDetail(filter, "IV.7", "25-29", 16);
			ret.getListDetail().add(details);
			//30-34
			details= this.getReportDetail(filter, "IV.8", "30-34", 17);
			ret.getListDetail().add(details);
			//35-39
			details= this.getReportDetail(filter, "IV.9", "35-39", 18);
			ret.getListDetail().add(details);
			//40-44
			details= this.getReportDetail(filter, "IV.10", "40-44", 19);
			ret.getListDetail().add(details);
			//45-49
			details= this.getReportDetail(filter, "IV.11", "45-49", 20);
			ret.getListDetail().add(details);
			//50+
			details= this.getReportDetail(filter, "IV.12", "50+", 21);
			ret.getListDetail().add(details);
			
			//Nữ
			details= this.getReportDetail(filter, "IV.13", "Nữ", 22);
			ret.getListDetail().add(details);
			//Không rõ nhóm tuổi
			details= this.getReportDetail(filter, "IV.14", "Không rõ nhóm tuổi", 23);
			ret.getListDetail().add(details);
			//10-14
			details= this.getReportDetail(filter, "IV.15", "10-14", 24);
			ret.getListDetail().add(details);
			//15-19
			details= this.getReportDetail(filter, "IV.16", "15-19", 25);
			ret.getListDetail().add(details);
			//20-24
			details= this.getReportDetail(filter, "IV.17", "20-24", 26);
			ret.getListDetail().add(details);
			//25-29
			details= this.getReportDetail(filter, "IV.18", "25-29", 27);
			ret.getListDetail().add(details);
			//30-34
			details= this.getReportDetail(filter, "IV.19", "30-34", 28);
			ret.getListDetail().add(details);
			//35-39
			details= this.getReportDetail(filter, "IV.20", "35-39", 29);
			ret.getListDetail().add(details);
			//40-44
			details= this.getReportDetail(filter, "IV.21", "40-44", 30);
			ret.getListDetail().add(details);
			//45-49
			details= this.getReportDetail(filter, "IV.22", "45-49", 31);
			ret.getListDetail().add(details);
			//50+
			details= this.getReportDetail(filter, "IV.23", "50+", 32);
			ret.getListDetail().add(details);
			
			return ret;
		}
		return null;

	}

	@Override
	public List<SelfTestReportNewDto> getReportNew(PreventionFilterDto filter) {
		String sql = "select tou.code as orgCode , tou.name as  orgName, pro.name as provinceName, dis.name as districtName,\n" +
				"       SUM(case when (tss.client_risk_group = 'PWID' and tss.support = 'W_SUPPORT') then 1 else 0 end) as pWIDAssisted,\n" +
				"       SUM(case when (tss.client_risk_group = 'MSM' and tss.support = 'W_SUPPORT') then 1 else 0 end) as mSMAssisted,\n" +
				"       SUM(case when (tss.client_risk_group = 'FSW' and tss.support = 'W_SUPPORT') then 1 else 0 end) as fSWAssisted,\n" +
				"       SUM(case when (tss.client_risk_group = 'TG' and tss.support = 'W_SUPPORT') then 1 else 0 end) as tGAssisted,\n" +
				"       SUM(case when ((tss.client_risk_group = 'OTHER' or tss.client_risk_group = 'PLHIV_PARTNER') and tss.support = 'W_SUPPORT') then 1 else 0 end) as otherAssisted,\n" +
				"       SUM(case when (tss.client_gender = 'MALE' and tss.support = 'W_SUPPORT' and tss.client_yob is null) then 1 else 0 end) as muAssisted,\n" +
				"       SUM(case when (tss.client_gender = 'MALE' and tss.support = 'W_SUPPORT' AND (:currentYear - tss.client_yob) >=10 AND (:currentYear - tss.client_yob) <=14) then 1 else 0 end) as m10Assisted,\n" +
				"       SUM(case when (tss.client_gender = 'MALE' and tss.support = 'W_SUPPORT' AND (:currentYear - tss.client_yob) >=15 AND (:currentYear - tss.client_yob) <=19) then 1 else 0 end) as m15Assisted,\n" +
				"       SUM(case when (tss.client_gender = 'MALE' and tss.support = 'W_SUPPORT' AND (:currentYear - tss.client_yob) >=20 AND (:currentYear - tss.client_yob) <=24) then 1 else 0 end) as m20Assisted,\n" +
				"       SUM(case when (tss.client_gender = 'MALE' and tss.support = 'W_SUPPORT' AND (:currentYear - tss.client_yob) >=25 AND (:currentYear - tss.client_yob) <=29) then 1 else 0 end) as m25Assisted,\n" +
				"       SUM(case when (tss.client_gender = 'MALE' and tss.support = 'W_SUPPORT' AND (:currentYear - tss.client_yob) >=30 AND (:currentYear - tss.client_yob) <=34) then 1 else 0 end) as m30Assisted,\n" +
				"       SUM(case when (tss.client_gender = 'MALE' and tss.support = 'W_SUPPORT' AND (:currentYear - tss.client_yob) >=35 AND (:currentYear - tss.client_yob) <=39) then 1 else 0 end) as m35Assisted,\n" +
				"       SUM(case when (tss.client_gender = 'MALE' and tss.support = 'W_SUPPORT' AND (:currentYear - tss.client_yob) >=40 AND (:currentYear - tss.client_yob) <=44) then 1 else 0 end) as m40Assisted,\n" +
				"       SUM(case when (tss.client_gender = 'MALE' and tss.support = 'W_SUPPORT' AND (:currentYear - tss.client_yob) >=45 AND (:currentYear - tss.client_yob) <=49) then 1 else 0 end) as m45Assisted,\n" +
				"       SUM(case when (tss.client_gender = 'MALE' and tss.support = 'W_SUPPORT' AND (:currentYear - tss.client_yob) >=50 ) then 1 else 0 end) as m50Assisted,\n" +
				"       SUM(case when (tss.client_gender = 'FEMALE' and tss.support = 'W_SUPPORT' and tss.client_yob is null) then 1 else 0 end) as fuAssisted,\n" +
				"       SUM(case when (tss.client_gender = 'FEMALE' and tss.support = 'W_SUPPORT' AND (:currentYear - tss.client_yob) >=10 AND (:currentYear - tss.client_yob) <=14) then 1 else 0 end) as f10Assisted,\n" +
				"       SUM(case when (tss.client_gender = 'FEMALE' and tss.support = 'W_SUPPORT' AND (:currentYear - tss.client_yob) >=15 AND (:currentYear - tss.client_yob) <=19) then 1 else 0 end) as f15Assisted,\n" +
				"       SUM(case when (tss.client_gender = 'FEMALE' and tss.support = 'W_SUPPORT' AND (:currentYear - tss.client_yob) >=20 AND (:currentYear - tss.client_yob) <=24) then 1 else 0 end) as f20Assisted,\n" +
				"       SUM(case when (tss.client_gender = 'FEMALE' and tss.support = 'W_SUPPORT' AND (:currentYear - tss.client_yob) >=25 AND (:currentYear - tss.client_yob) <=29) then 1 else 0 end) as f25Assisted,\n" +
				"       SUM(case when (tss.client_gender = 'FEMALE' and tss.support = 'W_SUPPORT' AND (:currentYear - tss.client_yob) >=30 AND (:currentYear - tss.client_yob) <=34) then 1 else 0 end) as f30Assisted,\n" +
				"       SUM(case when (tss.client_gender = 'FEMALE' and tss.support = 'W_SUPPORT' AND (:currentYear - tss.client_yob) >=35 AND (:currentYear - tss.client_yob) <=39) then 1 else 0 end) as f35Assisted,\n" +
				"       SUM(case when (tss.client_gender = 'FEMALE' and tss.support = 'W_SUPPORT' AND (:currentYear - tss.client_yob) >=40 AND (:currentYear - tss.client_yob) <=44) then 1 else 0 end) as f40Assisted,\n" +
				"       SUM(case when (tss.client_gender = 'FEMALE' and tss.support = 'W_SUPPORT' AND (:currentYear - tss.client_yob) >=45 AND (:currentYear - tss.client_yob) <=49) then 1 else 0 end) as f45Assisted,\n" +
				"       SUM(case when (tss.client_gender = 'FEMALE' and tss.support = 'W_SUPPORT' AND (:currentYear - tss.client_yob) >=50 ) then 1 else 0 end) as f50Assisted,\n" +
				"\n" +
				"       SUM(case when (tss.client_risk_group = 'PWID' and tss.support = 'WO_SUPPORT') then 1 else 0 end) as pWIDNotAssisted,\n" +
				"       SUM(case when (tss.client_risk_group = 'MSM' and tss.support = 'WO_SUPPORT') then 1 else 0 end) as mSMNotAssisted,\n" +
				"       SUM(case when (tss.client_risk_group = 'FSW' and tss.support = 'WO_SUPPORT') then 1 else 0 end) as fSWNotAssisted,\n" +
				"       SUM(case when (tss.client_risk_group = 'TG' and tss.support = 'WO_SUPPORT') then 1 else 0 end) as tGNotAssisted,\n" +
				"       SUM(case when ((tss.client_risk_group = 'OTHER' or tss.client_risk_group = 'PLHIV_PARTNER') and tss.support = 'WO_SUPPORT') then 1 else 0 end) as otherNotAssisted,\n" +
				"       SUM(case when (tss.client_gender = 'MALE' and tss.support = 'WO_SUPPORT' and tss.client_yob is null) then 1 else 0 end) as muNotAssisted,\n" +
				"       SUM(case when (tss.client_gender = 'MALE' and tss.support = 'WO_SUPPORT' AND (:currentYear - tss.client_yob) >=10 AND (:currentYear - tss.client_yob) <=14) then 1 else 0 end) as m10NotAssisted,\n" +
				"       SUM(case when (tss.client_gender = 'MALE' and tss.support = 'WO_SUPPORT' AND (:currentYear - tss.client_yob) >=15 AND (:currentYear - tss.client_yob) <=19) then 1 else 0 end) as m15NotAssisted,\n" +
				"       SUM(case when (tss.client_gender = 'MALE' and tss.support = 'WO_SUPPORT' AND (:currentYear - tss.client_yob) >=20 AND (:currentYear - tss.client_yob) <=24) then 1 else 0 end) as m20NotAssisted,\n" +
				"       SUM(case when (tss.client_gender = 'MALE' and tss.support = 'WO_SUPPORT' AND (:currentYear - tss.client_yob) >=25 AND (:currentYear - tss.client_yob) <=29) then 1 else 0 end) as m25NotAssisted,\n" +
				"       SUM(case when (tss.client_gender = 'MALE' and tss.support = 'WO_SUPPORT' AND (:currentYear - tss.client_yob) >=30 AND (:currentYear - tss.client_yob) <=34) then 1 else 0 end) as m30NotAssisted,\n" +
				"       SUM(case when (tss.client_gender = 'MALE' and tss.support = 'WO_SUPPORT' AND (:currentYear - tss.client_yob) >=35 AND (:currentYear - tss.client_yob) <=39) then 1 else 0 end) as m35NotAssisted,\n" +
				"       SUM(case when (tss.client_gender = 'MALE' and tss.support = 'WO_SUPPORT' AND (:currentYear - tss.client_yob) >=40 AND (:currentYear - tss.client_yob) <=44) then 1 else 0 end) as m40NotAssisted,\n" +
				"       SUM(case when (tss.client_gender = 'MALE' and tss.support = 'WO_SUPPORT' AND (:currentYear - tss.client_yob) >=45 AND (:currentYear - tss.client_yob) <=49) then 1 else 0 end) as m45NotAssisted,\n" +
				"       SUM(case when (tss.client_gender = 'MALE' and tss.support = 'WO_SUPPORT' AND (:currentYear - tss.client_yob) >=50 ) then 1 else 0 end) as m50NotAssisted,\n" +
				"       SUM(case when (tss.client_gender = 'FEMALE' and tss.support = 'WO_SUPPORT' and tss.client_yob is null) then 1 else 0 end) as fuNotAssisted,\n" +
				"       SUM(case when (tss.client_gender = 'FEMALE' and tss.support = 'WO_SUPPORT' AND (:currentYear - tss.client_yob) >=10 AND (:currentYear - tss.client_yob) <=14) then 1 else 0 end) as f10NotAssisted,\n" +
				"       SUM(case when (tss.client_gender = 'FEMALE' and tss.support = 'WO_SUPPORT' AND (:currentYear - tss.client_yob) >=15 AND (:currentYear - tss.client_yob) <=19) then 1 else 0 end) as f15NotAssisted,\n" +
				"       SUM(case when (tss.client_gender = 'FEMALE' and tss.support = 'WO_SUPPORT' AND (:currentYear - tss.client_yob) >=20 AND (:currentYear - tss.client_yob) <=24) then 1 else 0 end) as f20NotAssisted,\n" +
				"       SUM(case when (tss.client_gender = 'FEMALE' and tss.support = 'WO_SUPPORT' AND (:currentYear - tss.client_yob) >=25 AND (:currentYear - tss.client_yob) <=29) then 1 else 0 end) as f25NotAssisted,\n" +
				"       SUM(case when (tss.client_gender = 'FEMALE' and tss.support = 'WO_SUPPORT' AND (:currentYear - tss.client_yob) >=30 AND (:currentYear - tss.client_yob) <=34) then 1 else 0 end) as f30NotAssisted,\n" +
				"       SUM(case when (tss.client_gender = 'FEMALE' and tss.support = 'WO_SUPPORT' AND (:currentYear - tss.client_yob) >=35 AND (:currentYear - tss.client_yob) <=39) then 1 else 0 end) as f35NotAssisted,\n" +
				"       SUM(case when (tss.client_gender = 'FEMALE' and tss.support = 'WO_SUPPORT' AND (:currentYear - tss.client_yob) >=40 AND (:currentYear - tss.client_yob) <=44) then 1 else 0 end) as f40NotAssisted,\n" +
				"       SUM(case when (tss.client_gender = 'FEMALE' and tss.support = 'WO_SUPPORT' AND (:currentYear - tss.client_yob) >=45 AND (:currentYear - tss.client_yob) <=49) then 1 else 0 end) as f45NotAssisted,\n" +
				"       SUM(case when (tss.client_gender = 'FEMALE' and tss.support = 'WO_SUPPORT' AND (:currentYear - tss.client_yob) >=50 ) then 1 else 0 end) as f50NotAssisted,\n" +
				"       SUM(case when (tss.client_type = 'SELF' and tss.support = 'WO_SUPPORT') then 1 else 0 end) as testSelfNotAssisted,\n" +
				"       SUM(case when (tss.client_type = 'SEXUAL_PARTNER' and tss.support = 'WO_SUPPORT') then 1 else 0 end) as testPartnerNotAssisted,\n" +
				"       SUM(case when ((tss.client_type = 'OTHER' or tss.client_type = 'IDU_PARTNER' )and tss.support = 'WO_SUPPORT') then 1 else 0 end) as testOtherNotAssisted,\n" +
				"       SUM(case when (tss.client_yob is null and tss.client_gender is null and tss.support = 'WO_SUPPORT') then 1 else 0 end) as uSANotAssisted " +
				" from tbl_selftest_specimen tss\n" +
				" inner join tbl_selftest_entry tse on tss.self_test_id = tse.id\n" +
				" inner join tbl_organization_unit tou on tse.org_id = tou.id\n" +
				" inner join tbl_location tl on tou.address_id = tl.id\n" +
				" inner join tbl_admin_unit pro on tl.province_id = pro.id\n" +
				" inner join tbl_admin_unit dis on tl.district_id = dis.id\n" +
				" where 1=1 ";
		String whereClause="";
		if(!CollectionUtils.isEmpty(filter.getOrgIds())){
			whereClause+=" AND tou.id in (:listOrg) ";
		}
		if(filter.getFromDate()!=null){
			whereClause+=" AND tss.dispensing_date >=:fromDate ";
		}
		if(filter.getToDate()!=null){
			whereClause+="  AND tss.dispensing_date <=:toDate ";
		}
		String groupBy=" group by tou.code,tou.name,pro.name,dis.name";
		Integer currentYear= new LocalDateTime().getYear();

		org.hibernate.Query query = manager.unwrap(Session.class).createSQLQuery(sql+whereClause+groupBy).setResultTransformer(new AliasToBeanResultTransformer(SelfTestReportNewDto.class));
		query.setParameter("currentYear",currentYear);
		if(!CollectionUtils.isEmpty(filter.getOrgIds())){
			query.setParameterList("listOrg",filter.getOrgIds());
		}
		if(filter.getFromDate()!=null){
			query.setParameter("fromDate",Date.from(filter.getFromDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if(filter.getToDate()!=null){
			query.setParameter("toDate",Date.from(filter.getToDate().atZone(ZoneId.systemDefault()).toInstant()));
		}
		query.setResultTransformer(Transformers.aliasToBean(SelfTestReportNewDto.class));
		List<SelfTestReportNewDto> ret = query.list();
		return ret;
	}

	private SelfTestDetailReportDto getReportDetail(PreventionFilterDto filter, String rowCode, String rowName, int orderNumber) {
		SelfTestDetailReportDto detail = new SelfTestDetailReportDto();
		detail.setSeq(rowCode);
		detail.setTitle(rowName);
		//
		Integer supported=0;
		supported = this.queryReport(filter, rowCode, 1).intValue();
		detail.setSupported(supported);
		
		Integer unsupported=0;
		unsupported = this.queryReport(filter, rowCode, 2).intValue();
		detail.setUnsupported(unsupported);
		
		
		return detail;
	}

	private Integer queryReport(PreventionFilterDto filter, String rowCode, int col) {
		String SQL=" SELECT COUNT(s.id) from SelfTestSpecimen s WHERE s.selfTest.organization.id in (:listOrg) ";
		String whereClause=" AND s.dispensingDate >=:fromDate AND s.dispensingDate <=:toDate ";
		Integer currentYear= new LocalDateTime().getYear();
		//có hỗ trợ
		if(col==1) {
			whereClause+=" AND s.support =:support";
			if(rowCode=="I") {//tổng
				whereClause+="";
			}
			if(rowCode=="II.1") {
				whereClause+=" AND s.clientRiskGroup =:PWID ";
			}
			if(rowCode=="II.2") {
				whereClause+=" AND s.clientRiskGroup =:MSM ";
			}
			if(rowCode=="II.3") {
				whereClause+=" AND s.clientRiskGroup =:FSW ";
			}
			if(rowCode=="II.4") {
				whereClause+=" AND s.clientRiskGroup =:TG ";
			}
			if(rowCode=="II.5") {
				whereClause+=" AND s.clientRiskGroup =:PLHIV_PARTNER ";
			}
			if(rowCode=="II.6") {
				whereClause+=" AND s.clientRiskGroup =:OTHER ";
			}
			if(rowCode=="III.1") {
				whereClause+=" AND s.client =:SELF ";
			}
			if(rowCode=="III.2") {
				whereClause+=" AND s.client =:SEXUAL_PARTNER ";
			}
			if(rowCode=="III.3") {
				whereClause+=" AND s.client =:IDU_PARTNER ";
			}
			if(rowCode=="III.4") {
				whereClause+=" AND s.client =:OTHERIII ";
			}
			if(rowCode=="IV.1") {
				whereClause+=" AND s.clientGender is null AND s.clientYob is null ";
			}
			if(rowCode=="IV.2") {
				whereClause+=" AND s.clientGender =:Male";
			}
			if(rowCode=="IV.3") {
				whereClause+=" AND s.clientGender =:Male AND s.clientYob is null";
			}
			if(rowCode=="IV.4") {
				whereClause+=" AND s.clientGender =:Male AND (:currentYear - s.clientYob) >=10 AND (:currentYear - s.clientYob) <=14 ";
			}
			if(rowCode=="IV.5") {
				whereClause+=" AND s.clientGender =:Male AND (:currentYear - s.clientYob) >=15 AND (:currentYear - s.clientYob) <=19 ";
			}
			if(rowCode=="IV.6") {
				whereClause+=" AND s.clientGender =:Male AND (:currentYear - s.clientYob) >=20 AND (:currentYear - s.clientYob) <=24 ";
			}
			if(rowCode=="IV.7") {
				whereClause+=" AND s.clientGender =:Male AND (:currentYear - s.clientYob) >=25 AND (:currentYear - s.clientYob) <=29 ";
			}
			if(rowCode=="IV.8") {
				whereClause+=" AND s.clientGender =:Male AND (:currentYear - s.clientYob) >=30 AND (:currentYear - s.clientYob) <=34 ";
			}
			if(rowCode=="IV.9") {
				whereClause+=" AND s.clientGender =:Male AND (:currentYear - s.clientYob) >=35 AND (:currentYear - s.clientYob) <=39 ";
			}
			if(rowCode=="IV.10") {
				whereClause+=" AND s.clientGender =:Male AND (:currentYear - s.clientYob) >=40 AND (:currentYear - s.clientYob) <=44 ";
			}
			if(rowCode=="IV.11") {
				whereClause+=" AND s.clientGender =:Male AND (:currentYear - s.clientYob) >=45 AND (:currentYear - s.clientYob) <=49 ";
			}
			if(rowCode=="IV.12") {
				whereClause+=" AND s.clientGender =:Male AND (:currentYear - s.clientYob) >=50 ";
			}
			if(rowCode=="IV.13") {
				whereClause+=" AND s.clientGender =:FeMale";
			}
			if(rowCode=="IV.14") {
				whereClause+=" AND s.clientGender =:FeMale AND s.clientYob is null";
			}
			if(rowCode=="IV.15") {
				whereClause+=" AND s.clientGender =:FeMale AND (:currentYear - s.clientYob) >=10 AND (:currentYear - s.clientYob) <=14 ";
			}
			if(rowCode=="IV.16") {
				whereClause+=" AND s.clientGender =:FeMale AND (:currentYear - s.clientYob) >=15 AND (:currentYear - s.clientYob) <=19 ";
			}
			if(rowCode=="IV.17") {
				whereClause+=" AND s.clientGender =:FeMale AND (:currentYear - s.clientYob) >=20 AND (:currentYear - s.clientYob) <=24 ";
			}
			if(rowCode=="IV.18") {
				whereClause+=" AND s.clientGender =:FeMale AND (:currentYear - s.clientYob) >=25 AND (:currentYear - s.clientYob) <=29 ";
			}
			if(rowCode=="IV.19") {
				whereClause+=" AND s.clientGender =:FeMale AND (:currentYear - s.clientYob) >=30 AND (:currentYear - s.clientYob) <=34 ";
			}
			if(rowCode=="IV.20") {
				whereClause+=" AND s.clientGender =:FeMale AND (:currentYear - s.clientYob) >=35 AND (:currentYear - s.clientYob) <=39 ";
			}
			if(rowCode=="IV.21") {
				whereClause+=" AND s.clientGender =:FeMale AND (:currentYear - s.clientYob) >=40 AND (:currentYear - s.clientYob) <=44 ";
			}
			if(rowCode=="IV.22") {
				whereClause+=" AND s.clientGender =:FeMale AND (:currentYear - s.clientYob) >=45 AND (:currentYear - s.clientYob) <=49 ";
			}
			if(rowCode=="IV.23") {
				whereClause+=" AND s.clientGender =:FeMale AND (:currentYear - s.clientYob) >=50 ";
			}
			
		}
		if(col==2) {
			whereClause+=" AND s.support =:support";
			if(rowCode=="I") {//tổng
				whereClause+="";
			}
			if(rowCode=="II.1") {
				whereClause+=" AND s.clientRiskGroup =:PWID ";
			}
			if(rowCode=="II.2") {
				whereClause+=" AND s.clientRiskGroup =:MSM ";
			}
			if(rowCode=="II.3") {
				whereClause+=" AND s.clientRiskGroup =:FSW ";
			}
			if(rowCode=="II.4") {
				whereClause+=" AND s.clientRiskGroup =:TG ";
			}
			if(rowCode=="II.5") {
				whereClause+=" AND s.clientRiskGroup =:PLHIV_PARTNER ";
			}
			if(rowCode=="II.6") {
				whereClause+=" AND s.clientRiskGroup =:OTHER  ";
			}
			if(rowCode=="III.1") {
				whereClause+=" AND s.client =:SELF ";
			}
			if(rowCode=="III.2") {
				whereClause+=" AND s.client =:SEXUAL_PARTNER ";
			}
			if(rowCode=="III.3") {
				whereClause+=" AND s.client =:IDU_PARTNER ";
			}
			if(rowCode=="III.4") {
				whereClause+=" AND s.client =:OTHERIII ";
			}
			if(rowCode=="IV.1") {
				whereClause+=" AND s.clientGender is null AND s.clientYob is null ";
			}
			if(rowCode=="IV.2") {
				whereClause+=" AND s.clientGender =:Male";
			}
			if(rowCode=="IV.3") {
				whereClause+=" AND s.clientGender =:Male AND s.clientYob is null";
			}
			if(rowCode=="IV.4") {
				whereClause+=" AND s.clientGender =:Male AND :currentYear - s.clientYob >=10 AND :currentYear - s.clientYob <=14";
			}
			if(rowCode=="IV.5") {
				whereClause+=" AND s.clientGender =:Male AND :currentYear - s.clientYob >=15 AND :currentYear - s.clientYob <=19 ";
			}
			if(rowCode=="IV.6") {
				whereClause+=" AND s.clientGender =:Male AND :currentYear - 20 >=s.clientYob AND :currentYear - 24 <= s.clientYob ";
			}
			if(rowCode=="IV.7") {
				whereClause+=" AND s.clientGender =:Male AND :currentYear - s.clientYob >=25 AND :currentYear - s.clientYob <=29 ";
			}
			if(rowCode=="IV.8") {
				whereClause+=" AND s.clientGender =:Male AND :currentYear - s.clientYob >=30 AND :currentYear - s.clientYob <=34 ";
			}
			if(rowCode=="IV.9") {
				whereClause+=" AND s.clientGender =:Male AND :currentYear - s.clientYob >=35 AND :currentYear - s.clientYob <=39 ";
			}
			if(rowCode=="IV.10") {
				whereClause+=" AND s.clientGender =:Male AND :currentYear - s.clientYob >=40 AND :currentYear - s.clientYob <=44 ";
			}
			if(rowCode=="IV.11") {
				whereClause+=" AND s.clientGender =:Male AND :currentYear - s.clientYob >=45 AND :currentYear - s.clientYob <=49 ";
			}
			if(rowCode=="IV.12") {
				whereClause+=" AND s.clientGender =:Male AND :currentYear - s.clientYob >=50 ";
			}
			if(rowCode=="IV.13") {
				whereClause+=" AND s.clientGender =:FeMale";
			}
			if(rowCode=="IV.14") {
				whereClause+=" AND s.clientGender =:FeMale AND s.clientYob is null";
			}
			if(rowCode=="IV.15") {
				whereClause+=" AND s.clientGender =:FeMale AND (:currentYear - s.clientYob) >=10 AND (:currentYear - s.clientYob) <=14 ";
			}
			if(rowCode=="IV.16") {
				whereClause+=" AND s.clientGender =:FeMale AND (:currentYear - s.clientYob) >=15 AND (:currentYear - s.clientYob) <=19 ";
			}
			if(rowCode=="IV.17") {
				whereClause+=" AND s.clientGender =:FeMale AND (:currentYear - s.clientYob) >=20 AND (:currentYear - s.clientYob) <=24 ";
			}
			if(rowCode=="IV.18") {
				whereClause+=" AND s.clientGender =:FeMale AND (:currentYear - s.clientYob) >=25 AND (:currentYear - s.clientYob) <=29 ";
			}
			if(rowCode=="IV.19") {
				whereClause+=" AND s.clientGender =:FeMale AND (:currentYear - s.clientYob) >=30 AND (:currentYear - s.clientYob) <=34 ";
			}
			if(rowCode=="IV.20") {
				whereClause+=" AND s.clientGender =:FeMale AND (:currentYear - s.clientYob) >=35 AND (:currentYear - s.clientYob) <=39 ";
			}
			if(rowCode=="IV.21") {
				whereClause+=" AND s.clientGender =:FeMale AND (:currentYear - s.clientYob) >=40 AND (:currentYear - s.clientYob) <=44 ";
			}
			if(rowCode=="IV.22") {
				whereClause+=" AND s.clientGender =:FeMale AND (:currentYear - s.clientYob) >=45 AND (:currentYear - s.clientYob) <=49 ";
			}
			if(rowCode=="IV.23") {
				whereClause+=" AND s.clientGender =:FeMale AND (:currentYear - s.clientYob) >=50 ";
			}
			
		}
		
		
		Query q = manager.createQuery(SQL+whereClause);
		q.setParameter("listOrg", filter.getOrgIds());
		
		q.setParameter("fromDate", filter.getFromDate());
		q.setParameter("toDate", filter.getToDate());
		
		if(col==1) {
			q.setParameter("support", "W_SUPPORT");
			if(rowCode=="I") {
				
			}
			if(rowCode=="II.1") {
				q.setParameter("PWID", "PWID");
			}
			if(rowCode=="II.2") {
				q.setParameter("MSM", "MSM");
			}
			if(rowCode=="II.3") {
				q.setParameter("FSW", "FSW");
			}
			if(rowCode=="II.4") {
				q.setParameter("TG", "TG");
			}
			if(rowCode=="II.5") {
				q.setParameter("PLHIV_PARTNER", "PLHIV_PARTNER");
			}
			if(rowCode=="II.6") {
				q.setParameter("OTHER", "OTHER");
			}
			if(rowCode=="III.1") {
				q.setParameter("SELF", "SELF");
			}
			if(rowCode=="III.2") {
				q.setParameter("SEXUAL_PARTNER", "SEXUAL_PARTNER");
			}
			if(rowCode=="III.3") {
				q.setParameter("IDU_PARTNER", "IDU_PARTNER");
			}
			if(rowCode=="III.4") {
				q.setParameter("OTHERIII", "OTHER");
			}
			if(rowCode=="IV.1") {
			}
			if(rowCode=="IV.2") {
				q.setParameter("Male", Gender.MALE);
			}
			if(rowCode=="IV.3") {
				q.setParameter("Male", Gender.MALE);
			}
			if(rowCode=="IV.4") {
				q.setParameter("Male", Gender.MALE);
				q.setParameter("currentYear", currentYear);
			}
			if(rowCode=="IV.5") {
				q.setParameter("Male", Gender.MALE);
				q.setParameter("currentYear", currentYear);
			}
			if(rowCode=="IV.6") {
				q.setParameter("Male", Gender.MALE);
				q.setParameter("currentYear", currentYear);
			}
			if(rowCode=="IV.7") {
				q.setParameter("Male", Gender.MALE);
				q.setParameter("currentYear", currentYear);
			}
			if(rowCode=="IV.8") {
				q.setParameter("Male", Gender.MALE);
				q.setParameter("currentYear", currentYear);
			}
			if(rowCode=="IV.9") {
				q.setParameter("Male", Gender.MALE);
				q.setParameter("currentYear", currentYear);
			}
			if(rowCode=="IV.10") {
				q.setParameter("Male", Gender.MALE);
				q.setParameter("currentYear", currentYear);
			}
			if(rowCode=="IV.11") {
				q.setParameter("Male", Gender.MALE);
				q.setParameter("currentYear", currentYear);
			}
			if(rowCode=="IV.12") {
				q.setParameter("Male", Gender.MALE);
				q.setParameter("currentYear", currentYear);
			}
			if(rowCode=="IV.13") {
				q.setParameter("FeMale", Gender.FEMALE);
				
			}
			if(rowCode=="IV.14") {
				q.setParameter("FeMale", Gender.FEMALE);
				
			}
			if(rowCode=="IV.15") {
				q.setParameter("FeMale", Gender.FEMALE);
				q.setParameter("currentYear", currentYear);
			}
			if(rowCode=="IV.16") {
				q.setParameter("FeMale", Gender.FEMALE);
				q.setParameter("currentYear", currentYear);
			}
			if(rowCode=="IV.17") {
				q.setParameter("FeMale", Gender.FEMALE);
				q.setParameter("currentYear", currentYear);
			}
			if(rowCode=="IV.18") {
				q.setParameter("FeMale", Gender.FEMALE);
				q.setParameter("currentYear", currentYear);
			}
			if(rowCode=="IV.19") {
				q.setParameter("FeMale", Gender.FEMALE);
				q.setParameter("currentYear", currentYear);
			}
			if(rowCode=="IV.20") {
				q.setParameter("FeMale", Gender.FEMALE);
				q.setParameter("currentYear", currentYear);
			}
			if(rowCode=="IV.21") {
				q.setParameter("FeMale", Gender.FEMALE);
				q.setParameter("currentYear", currentYear);
			}
			if(rowCode=="IV.22") {
				q.setParameter("FeMale", Gender.FEMALE);
				q.setParameter("currentYear", currentYear);
			}
			if(rowCode=="IV.23") {
				q.setParameter("FeMale", Gender.FEMALE);
				q.setParameter("currentYear", currentYear);
			}
			
		}
		if(col==2) {
			q.setParameter("support", "WO_SUPPORT");
			if(rowCode=="I") {
				
			}
			if(rowCode=="II.1") {
				q.setParameter("PWID", "PWID");
			}
			if(rowCode=="II.2") {
				q.setParameter("MSM", "MSM");
			}
			if(rowCode=="II.3") {
				q.setParameter("FSW", "FSW");
			}
			if(rowCode=="II.4") {
				q.setParameter("TG", "TG");
			}
			if(rowCode=="II.5") {
				q.setParameter("PLHIV_PARTNER", "PLHIV_PARTNER");
			}
			if(rowCode=="II.6") {
				q.setParameter("OTHER", "OTHER");
			}
			if(rowCode=="III.1") {
				q.setParameter("SELF", "SELF");
			}
			if(rowCode=="III.2") {
				q.setParameter("SEXUAL_PARTNER", "SEXUAL_PARTNER");
			}
			if(rowCode=="III.3") {
				q.setParameter("IDU_PARTNER", "IDU_PARTNER");
			}
			if(rowCode=="III.4") {
				q.setParameter("OTHERIII", "OTHER");
			}
			if(rowCode=="IV.1") {
			}
			if(rowCode=="IV.2") {
				q.setParameter("Male", Gender.MALE);
			}
			if(rowCode=="IV.3") {
				q.setParameter("Male", Gender.MALE);
			}
			if(rowCode=="IV.4") {
				q.setParameter("Male", Gender.MALE);
				q.setParameter("currentYear", currentYear);
			}
			if(rowCode=="IV.5") {
				q.setParameter("Male", Gender.MALE);
				q.setParameter("currentYear", currentYear);
			}
			if(rowCode=="IV.6") {
				q.setParameter("Male", Gender.MALE);
				q.setParameter("currentYear", currentYear);
			}
			if(rowCode=="IV.7") {
				q.setParameter("Male", Gender.MALE);
				q.setParameter("currentYear", currentYear);
			}
			if(rowCode=="IV.8") {
				q.setParameter("Male", Gender.MALE);
				q.setParameter("currentYear", currentYear);
			}
			if(rowCode=="IV.9") {
				q.setParameter("Male", Gender.MALE);
				q.setParameter("currentYear", currentYear);
			}
			if(rowCode=="IV.10") {
				q.setParameter("Male", Gender.MALE);
				q.setParameter("currentYear", currentYear);
			}
			if(rowCode=="IV.11") {
				q.setParameter("Male", Gender.MALE);
				q.setParameter("currentYear", currentYear);
			}
			if(rowCode=="IV.12") {
				q.setParameter("Male", Gender.MALE);
				q.setParameter("currentYear", currentYear);
			}
			if(rowCode=="IV.13") {
				q.setParameter("FeMale", Gender.FEMALE);
				
			}
			if(rowCode=="IV.14") {
				q.setParameter("FeMale", Gender.FEMALE);
				
			}
			if(rowCode=="IV.15") {
				q.setParameter("FeMale", Gender.FEMALE);
				q.setParameter("currentYear", currentYear);
			}
			if(rowCode=="IV.16") {
				q.setParameter("FeMale", Gender.FEMALE);
				q.setParameter("currentYear", currentYear);
			}
			if(rowCode=="IV.17") {
				q.setParameter("FeMale", Gender.FEMALE);
				q.setParameter("currentYear", currentYear);
			}
			if(rowCode=="IV.18") {
				q.setParameter("FeMale", Gender.FEMALE);
				q.setParameter("currentYear", currentYear);
			}
			if(rowCode=="IV.19") {
				q.setParameter("FeMale", Gender.FEMALE);
				q.setParameter("currentYear", currentYear);
			}
			if(rowCode=="IV.20") {
				q.setParameter("FeMale", Gender.FEMALE);
				q.setParameter("currentYear", currentYear);
			}
			if(rowCode=="IV.21") {
				q.setParameter("FeMale", Gender.FEMALE);
				q.setParameter("currentYear", currentYear);
			}
			if(rowCode=="IV.22") {
				q.setParameter("FeMale", Gender.FEMALE);
				q.setParameter("currentYear", currentYear);
			}
			if(rowCode=="IV.23") {
				q.setParameter("FeMale", Gender.FEMALE);
				q.setParameter("currentYear", currentYear);
			}
			
		}
		
		Long ret = (Long)q.getSingleResult();
		if(ret!=null) {
			return  ret.intValue();
		}
		return  0;
	}

}
