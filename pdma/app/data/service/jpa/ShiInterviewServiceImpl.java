package org.pepfar.pdma.app.data.service.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.apache.poi.ss.usermodel.Workbook;
import org.pepfar.pdma.app.data.aop.Loggable;
import org.pepfar.pdma.app.data.domain.Case;
import org.pepfar.pdma.app.data.domain.Dictionary;
import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.domain.QShiInterview;
import org.pepfar.pdma.app.data.domain.ShiInterview;
import org.pepfar.pdma.app.data.dto.ShiInterviewDto;
import org.pepfar.pdma.app.data.dto.ShiInterviewFilterDto;
import org.pepfar.pdma.app.data.repository.CaseRepository;
import org.pepfar.pdma.app.data.repository.DictionaryRepository;
import org.pepfar.pdma.app.data.repository.OrganizationRepository;
import org.pepfar.pdma.app.data.repository.ShiInterviewRepository;
import org.pepfar.pdma.app.data.service.ShiInterviewService;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.dsl.BooleanExpression;

@Service
public class ShiInterviewServiceImpl implements ShiInterviewService {

	@Autowired
	private ShiInterviewRepository repos;

	@Autowired
	private OrganizationRepository orgRepos;

	@Autowired
	private CaseRepository caseRepos;

	@Autowired
	private DictionaryRepository dicRepos;

	@Autowired
	private EntityManager entityManager;

	@Override
	@Transactional(readOnly = true)
	public ShiInterviewDto findById(Long id) {
		if (!CommonUtils.isPositive(id, true)) {
			return null;
		}

		ShiInterview entity = repos.findOne(id);

		if (entity != null) {
			return new ShiInterviewDto(entity);
		}

		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public ShiInterviewDto findLatestEntry(Long caseId) {
		if (!CommonUtils.isPositive(caseId, true)) {
			return null;
		}

		List<ShiInterview> entities = entityManager
				.createQuery("SELECT e FROM ShiInterview e where e.theCase.id = ?1 ORDER BY e.interviewDate DESC",
						ShiInterview.class)
				.setParameter(1, caseId).setMaxResults(1).getResultList();

		if (entities.size() > 0) {
			return new ShiInterviewDto(entities.get(0));
		} else {
			return null;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<ShiInterviewDto.DataCaptureInstance> getInstances(Long caseId) {
		if (!CommonUtils.isPositive(caseId, true)) {
			return new ArrayList<>();
		}

		List<ShiInterviewDto.DataCaptureInstance> instances = new ArrayList<>();
		repos.findAll(QShiInterview.shiInterview.theCase.id.longValue().eq(caseId),
				new Sort(new Order(Direction.DESC, "interviewDate"))).forEach(e -> {
					ShiInterviewDto.DataCaptureInstance _instance = new ShiInterviewDto.DataCaptureInstance();
					_instance.setId(e.getId());
					_instance.setInterviewDate(e.getInterviewDate());
					_instance.setPatientId(caseId);
					_instance.setHasShiCard(e.getHasShiCard());

					instances.add(_instance);
				});

		return instances;
	}

	@Override
	@Transactional(readOnly = true)
	public List<ShiInterviewDto> findAll(ShiInterviewFilterDto filter) {
		if (filter == null) {
			filter = new ShiInterviewFilterDto();
		}

		QShiInterview q = QShiInterview.shiInterview;
		BooleanExpression be = q.id.isNotNull();

		if (CommonUtils.isPositive(filter.getCaseId(), true)) {
			be = be.and(q.theCase.id.eq(filter.getCaseId().longValue()));
		}

		List<ShiInterviewDto> list = new ArrayList<>();

		repos.findAll(be, new Sort(new Order(Direction.DESC, "createDate"))).forEach(s -> {
			list.add(new ShiInterviewDto(s));
		});

		return list;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<ShiInterviewDto> findAllPageable(ShiInterviewFilterDto filter) {

		if (filter == null) {
			filter = new ShiInterviewFilterDto();
		}

		if (filter.getPageIndex() < 0) {
			filter.setPageIndex(0);
		}

		if (filter.getPageSize() < 0) {
			filter.setPageSize(25);
		}

		QShiInterview q = QShiInterview.shiInterview;
		BooleanExpression be = q.id.isNotNull();

		if (CommonUtils.isPositive(filter.getCaseId(), true)) {
			be = be.and(q.theCase.id.eq(filter.getCaseId().longValue()));
		}

		Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize(),
				new Sort(new Order(Direction.DESC, "createDate")));
		Page<ShiInterview> page = repos.findAll(be, pageable);

		List<ShiInterviewDto> content = new ArrayList<>();

		page.getContent().parallelStream().forEachOrdered(s -> {
			content.add(new ShiInterviewDto(s));
		});

		return new PageImpl<>(content, pageable, page.getTotalElements());
	}

	@Override
	@Loggable
	@Transactional(rollbackFor = Exception.class)
	public ShiInterviewDto saveOne(ShiInterviewDto dto) {

		if (dto == null) {
			throw new IllegalArgumentException("ShiInterview could not be null.");
		}

		ShiInterview entity = null;
		if (CommonUtils.isPositive(dto.getId(), true)) {
			QShiInterview q = QShiInterview.shiInterview;

			entity = repos.findOne(q.id.longValue().eq(dto.getId()).and(q.interviewDate.eq(dto.getInterviewDate())));
		}

		if (entity == null) {
			entity = dto.toEntity();
			entity.setUid(UUID.randomUUID());

			// If the ID is not null, then clear it as the interview date is different
			entity.setId(null);
		} else {
			entity.setInterviewDate(dto.getInterviewDate());
			entity.setResidentStatus(dto.getResidentStatus());
			entity.setOccupation(dto.getOccupation());
			entity.setOccupationName(dto.getOccupationName());
			entity.setMonthlyIncome(dto.getMonthlyIncome());
			entity.setWealthStatus(dto.getWealthStatus());
			entity.setWealthStatusName(dto.getWealthStatusName());
			entity.setHasShiCard(dto.getHasShiCard());
			entity.setShiCardNumber(dto.getShiCardNumber());
			entity.setShiExpiryDate(dto.getShiExpiryDate());
			entity.setPrimaryCareFacilityName(dto.getPrimaryCareFacilityName());
			entity.setOtherNoShiReason(dto.getOtherNoShiReason());
			entity.setWantShiForArv(dto.getWantShiForArv());
			entity.setUsedShiForArv(dto.getUsedShiForArv());
			entity.setOtherUsedShiService(dto.getOtherUsedShiService());
			entity.setShiRoute(dto.getShiRoute());
			entity.setShiForArvPref(dto.getShiForArvPref());
			entity.setContinuingFacilityName(dto.getContinuingFacilityName());
			entity.setArvTreatmentPref(dto.getArvTreatmentPref());
			entity.setBuyShiNextQuarter(dto.getBuyShiNextQuarter());
			entity.setNeedSupportForShi(dto.getNeedSupportForShi());
			entity.setNeedSupportDetails(dto.getNeedSupportDetails());
		}

		// No SHI reasons
		List<Dictionary> noShiReasons = new ArrayList<>();
		if (dto.getNoShiReasons() != null) {
			dto.getNoShiReasons().parallelStream().filter(d -> (d != null) && CommonUtils.isPositive(d.getId(), true))
					.forEach(d -> {
						Dictionary de = dicRepos.findOne(d.getId());

						if (de != null) {
							noShiReasons.add(de);
						}
					});
		}

		entity.getNoShiReasons().clear();
		entity.getNoShiReasons().addAll(noShiReasons);

		// Organization & Case
		Organization org = null;
		Organization primaryCareFacility = null;
		Organization continuingFacility = null;

		Case theCase = null;

		if (dto.getOrganization() != null && CommonUtils.isPositive(dto.getOrganization().getId(), true)) {
			org = orgRepos.findOne(dto.getOrganization().getId());
		}

		if (dto.getPrimaryCareFacility() != null
				&& CommonUtils.isPositive(dto.getPrimaryCareFacility().getId(), true)) {
			primaryCareFacility = orgRepos.findOne(dto.getPrimaryCareFacility().getId());
		}

		if (dto.getContinuingFacility() != null && CommonUtils.isPositive(dto.getContinuingFacility().getId(), true)) {
			continuingFacility = orgRepos.findOne(dto.getContinuingFacility().getId());
		}

		if (dto.getTheCase() != null && CommonUtils.isPositive(dto.getTheCase().getId(), true)) {
			theCase = caseRepos.findOne(dto.getTheCase().getId());
		}

		if (org == null || theCase == null) {
			throw new RuntimeException("Neither organization nor the case can be null when saving SHI Interview!");
		}

		entity.setOrganization(org);
		entity.setPrimaryCareFacility(primaryCareFacility);
		entity.setContinuingFacility(continuingFacility);
		entity.setTheCase(theCase);

		// Services used during the last quarter that were covered by SHI
		List<Dictionary> usedShiServices = new ArrayList<>();
		if (dto.getUsedShiServices() != null) {
			dto.getUsedShiServices().parallelStream()
					.filter(s -> (s != null) && CommonUtils.isPositive(s.getId(), true)).forEach(s -> {
						Dictionary ds = dicRepos.findOne(s.getId());

						if (ds != null) {
							usedShiServices.add(ds);
						}
					});
		}

		entity.getUsedShiServices().clear();
		entity.getUsedShiServices().addAll(usedShiServices);

		entity = repos.save(entity);

		// Update the SHI number for this case
		if (!CommonUtils.isEmpty(entity.getShiCardNumber())) {
			if (CommonUtils.isEmpty(theCase.getShiNumber())
					|| !entity.getShiCardNumber().equalsIgnoreCase(theCase.getShiNumber())) {
				theCase.setShiNumber(entity.getShiCardNumber());
				theCase = caseRepos.save(theCase);
			}
		}

		return new ShiInterviewDto(entity);
	}

	@Override
	@Loggable
	@Transactional(rollbackFor = Exception.class)
	public void deleteMultiple(ShiInterviewDto[] dtos) {

		if (CommonUtils.isEmpty(dtos)) {
			return;
		}

		for (ShiInterviewDto dto : dtos) {
			if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
				continue;
			}

			ShiInterview entity = repos.findOne(dto.getId());

			if (entity != null) {
				repos.delete(entity);
			}
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Workbook generateReport(ShiInterviewFilterDto filter) {
		// TODO Auto-generated method stub
		return null;
	}

}
