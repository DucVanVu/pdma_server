package org.pepfar.pdma.app.data.service.jpa;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.pepfar.pdma.app.data.domain.Appointment;
import org.pepfar.pdma.app.data.domain.Case;
import org.pepfar.pdma.app.data.domain.MMDispensing;
import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.dto.MMDispensingDto;
import org.pepfar.pdma.app.data.dto.MMDispensingFilterDto;
import org.pepfar.pdma.app.data.dto.MMDispensingHardEligibilityDto;
import org.pepfar.pdma.app.data.repository.AppointmentRepository;
import org.pepfar.pdma.app.data.repository.CaseRepository;
import org.pepfar.pdma.app.data.repository.MMDispensingRepository;
import org.pepfar.pdma.app.data.repository.OrganizationRepository;
import org.pepfar.pdma.app.data.service.MMDispensingService;
import org.pepfar.pdma.app.data.types.Permission;
import org.pepfar.pdma.app.utils.AuthorizationUtils;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

@Service
public class MMDispensingServiceImpl implements MMDispensingService {

	@Autowired
	private MMDispensingRepository repos;

	@Autowired
	private OrganizationRepository orgRepos;

	@Autowired
	private CaseRepository caseRepos;

	@Autowired
	private AppointmentRepository appRepos;

	@Autowired
	private AuthorizationUtils authUtils;

	@Override
	@Transactional(readOnly = true)
	public MMDispensingDto findById(Long id) {
		if (!CommonUtils.isPositive(id, true)) {
			return null;
		}

		MMDispensing entity = repos.findOne(id);

		if (entity != null) {
			return new MMDispensingDto(entity);
		}

		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public List<MMDispensingDto> findAll(MMDispensingFilterDto filter) {

		if (filter == null || filter.getTheCase() == null
				|| !CommonUtils.isPositive(filter.getTheCase().getId(), true)) {
			return new ArrayList<>();
		}

		List<Long> orgIds = authUtils.getGrantedOrgIds(Permission.WRITE_ACCESS, null, true);

		if (CommonUtils.isEmpty(orgIds)) {
			return new ArrayList<>();
		}

		List<MMDispensing> foundList = repos.findAllEvaluations(Lists.newArrayList(filter.getTheCase().getId()),
				filter.isIncludeDeleted());
		List<MMDispensingDto> list = new ArrayList<>();

		int count = 0;
		for (MMDispensing e : foundList) {
			list.add(new MMDispensingDto(e));

			count++;

			if (count >= filter.getPageSize()) {
				break;
			}
		}

		return list;
	}

	@Override
	@Transactional(readOnly = true)
	public MMDispensingHardEligibilityDto getHardEligibility(MMDispensingFilterDto filter) {

		MMDispensingHardEligibilityDto dto = new MMDispensingHardEligibilityDto();

		if (filter == null || filter.getOrganization() == null || filter.getTheCase() == null
				|| filter.getCutpoint() == null || !CommonUtils.isPositive(filter.getOrganization().getId(), true)
				|| !CommonUtils.isPositive(filter.getTheCase().getId(), true)) {
			return dto;
		}

		Long caseId = filter.getTheCase().getId();
		Long orgId = filter.getOrganization().getId();

		Case theCase = caseRepos.findOne(caseId);
		if (theCase == null) {
			return dto;
		}

		Timestamp cutpoint = CommonUtils.toTimestamp(filter.getCutpoint());

		boolean isGt3YearOld = CommonUtils.dateDiff(ChronoUnit.YEARS, theCase.getPerson().getDob(),
				filter.getCutpoint()) >= 3;
		boolean isVlLt50CD4Ge200_350 = caseRepos.checkVlLt50CD4Ge200_350(Lists.newArrayList(caseId), cutpoint).size() > 0;
		boolean isOnARVGt6Months = caseRepos.checkOnARVGt6Months(orgId, Lists.newArrayList(caseId), cutpoint)
				.size() > 0;

		dto.setAdult(isGt3YearOld);
		dto.setVlLt200CD4Ge500(isVlLt50CD4Ge200_350);
		dto.setOnARVGt12Months(isOnARVGt6Months);

		return dto;
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isHardEligible(MMDispensingFilterDto filter) {
		if (filter == null || filter.getOrganization() == null || filter.getTheCase() == null
				|| filter.getCutpoint() == null || !CommonUtils.isPositive(filter.getOrganization().getId(), true)
				|| !CommonUtils.isPositive(filter.getTheCase().getId(), true)) {
			return false;
		}

		Long caseId = filter.getTheCase().getId();
		Long orgId = filter.getOrganization().getId();

		Case theCase = caseRepos.findOne(caseId);
		if (theCase == null) {
			return false;
		}

		Timestamp cutpoint = CommonUtils.toTimestamp(filter.getCutpoint());

		boolean isGt3YearOld = CommonUtils.dateDiff(ChronoUnit.YEARS, theCase.getPerson().getDob(),
				filter.getCutpoint()) >= 3;
		boolean isVlLt200CD4Ge500 = caseRepos.checkVlLt50CD4Ge200_350(Lists.newArrayList(caseId), cutpoint).size() > 0;
		boolean isOnARVGt6Months = caseRepos.checkOnARVGt6Months(orgId, Lists.newArrayList(caseId), cutpoint)
				.size() > 0;

		return isGt3YearOld && isVlLt200CD4Ge500 && isOnARVGt6Months;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public MMDispensingDto softDeleteRestore(MMDispensingDto dto) {

		if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
			throw new RuntimeException("Could not mark deletion/restoration to a null MMDispensing entity!");
		}

		List<Long> grantedOrgIds = authUtils.getGrantedOrgIds(Permission.WRITE_ACCESS);

		MMDispensing mmd = repos.findOne(dto.getId());
		if (mmd == null) {
			throw new RuntimeException("MMDispensing entity not found for marking deletion/restoration!");
		}

		if (grantedOrgIds == null || grantedOrgIds.isEmpty()
				|| !grantedOrgIds.contains(mmd.getOrganization().getId().longValue())) {
			throw new AccessDeniedException("Access denied!");
		}

		mmd.setDeleted(dto.getDeleted());
		mmd = repos.save(mmd);

		if (mmd != null) {
			return new MMDispensingDto(mmd);
		} else {
			return null;
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public MMDispensingDto saveOne(MMDispensingDto dto) {
		if (dto == null || dto.getTheCase() == null || !CommonUtils.isPositive(dto.getTheCase().getId(), true)
				|| dto.getOrganization() == null || !CommonUtils.isPositive(dto.getOrganization().getId(), true)) {
			throw new RuntimeException();
		}

		MMDispensing entity = null;

		if (CommonUtils.isPositive(dto.getId(), true)) {
			entity = repos.findOne(dto.getId());
		}

		if (entity == null && dto.getEvaluationDate() != null) {
			// find an instance of the MMD
			LocalDateTime fromDate = CommonUtils.dateStart(dto.getEvaluationDate());
			LocalDateTime toDate = CommonUtils.dateEnd(dto.getEvaluationDate());

			List<MMDispensing> entities = repos.findInstance(dto.getTheCase().getId(), fromDate, toDate);

			if (entities.size() > 0) {
				entity = entities.get(0);
			}
		}

		// revise the data before saving
		if (CommonUtils.isTrue(dto.getOnMmd())) {
			if (!CommonUtils.isTrue(dto.getEligible())) {
				dto.setEligible(true);
			}
		}

		if (CommonUtils.isTrue(dto.getEligible())) {
			dto.setAdult(true);
			dto.setArvGt12Month(true);
			dto.setVlLt200(true);
			dto.setNoOIs(true);
			dto.setNoDrugAdvEvent(true);
			dto.setNoPregnancy(true);
			dto.setGoodAdherence(true);

			if (dto.getEvaluationDate() == null) {
				throw new RuntimeException("Patient is stable but evaluation date is not valid.");
			}
		}

		if (entity == null) {
			entity = dto.toEntity();
			entity.setUid(UUID.randomUUID());
		} else {
			entity.setAdult(dto.isAdult());
			entity.setArvGt12Month(dto.isArvGt12Month());
			entity.setVlLt200(dto.isVlLt200());
			entity.setNoOIs(dto.isNoOIs());
			entity.setNoDrugAdvEvent(dto.isNoDrugAdvEvent());
			entity.setNoPregnancy(dto.isNoPregnancy());
			entity.setGoodAdherence(dto.isGoodAdherence());

			entity.setEvaluationDate(dto.getEvaluationDate());
			entity.setEligible(dto.getEligible());

			entity.setOnMmd(dto.getOnMmd());
			entity.setStopReason(dto.getStopReason());
		}

		Case theCase = caseRepos.findOne(dto.getTheCase().getId());
		Organization organization = orgRepos.findOne(dto.getOrganization().getId());

		if (theCase == null || organization == null) {
			throw new RuntimeException();
		}

		List<Long> grantedIds = authUtils.getGrantedOrgIds(Permission.WRITE_ACCESS);
		if (grantedIds.size() <= 0 || !grantedIds.contains(organization.getId().longValue())) {
			throw new RuntimeException();
		}

		entity.setTheCase(theCase);
		entity.setOrganization(organization);

		entity = repos.save(entity);

		if (entity != null) {

			// Now find the corresponding appointment entry for updating the MMD evaluations
			Appointment appt = null;
			if (CommonUtils.isPositive(entity.getAppointmentId(), true)) {
				appt = appRepos.findOne(entity.getAppointmentId());
			}

			if (appt == null) {
				// find by date +- 5 days
				LocalDateTime startDate = CommonUtils.dateStart(entity.getEvaluationDate()).minusDays(5);
				LocalDateTime endDate = CommonUtils.dateEnd(entity.getEvaluationDate()).plusDays(5);

				List<Appointment> apptList = appRepos.findAppointmentsInDateRange(startDate, endDate, theCase.getId(),
						organization.getId());
				if (apptList.size() > 0) {
					appt = apptList.get(0);
				}
			}

			if (appt != null) {
				appt.setGoodAdherence(entity.isGoodAdherence());
				appt.setHasDrugAE(!entity.isNoDrugAdvEvent());
				appt.setHasOI(!entity.isNoOIs());
				appt.setPregnant(!entity.isNoPregnancy());

				appRepos.save(appt);
			}

			return new MMDispensingDto(entity);
		} else {
			return null;
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteMultiple(MMDispensingDto[] dtos) {
		if (CommonUtils.isEmpty(dtos)) {
			return;
		}

		List<Long> grantedOrgIds = authUtils.getGrantedOrgIds(Permission.DELETE_ACCESS);

		for (MMDispensingDto dto : dtos) {
			if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
				continue;
			}

			MMDispensing entity = repos.findOne(dto.getId());

			if (entity != null) {
				if (grantedOrgIds.contains(entity.getOrganization().getId().longValue())) {
					repos.delete(entity);
				}
			}
		}
	}

}
