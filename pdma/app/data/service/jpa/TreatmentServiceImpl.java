package org.pepfar.pdma.app.data.service.jpa;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.pepfar.pdma.app.data.domain.Case;
import org.pepfar.pdma.app.data.domain.Dictionary;
import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.domain.QDictionary;
import org.pepfar.pdma.app.data.domain.QTreatment;
import org.pepfar.pdma.app.data.domain.Regimen;
import org.pepfar.pdma.app.data.domain.Treatment;
import org.pepfar.pdma.app.data.dto.TreatmentDto;
import org.pepfar.pdma.app.data.dto.TreatmentFilterDto;
import org.pepfar.pdma.app.data.repository.CaseRepository;
import org.pepfar.pdma.app.data.repository.DictionaryRepository;
import org.pepfar.pdma.app.data.repository.OrganizationRepository;
import org.pepfar.pdma.app.data.repository.RegimenRepository;
import org.pepfar.pdma.app.data.repository.TreatmentRepository;
import org.pepfar.pdma.app.data.service.TreatmentService;
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
public class TreatmentServiceImpl implements TreatmentService {

	@Autowired
	private TreatmentRepository repos;

	@Autowired
	private CaseRepository caseRepos;

	@Autowired
	private OrganizationRepository orgRepos;

	@Autowired
	private DictionaryRepository dicRepos;

	@Autowired
	private RegimenRepository regRepos;

	@Override
	@Transactional(readOnly = true)
	public TreatmentDto findById(Long id) {

		if (!CommonUtils.isPositive(id, true)) {
			return null;
		}

		Treatment entity = repos.findOne(id);

		if (entity != null) {
			return new TreatmentDto(entity);
		}

		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<TreatmentDto> findAllPageable(TreatmentFilterDto filter) {

		if (filter == null) {
			filter = new TreatmentFilterDto();
		}

		if (filter.getPageIndex() < 0) {
			filter.setPageIndex(0);
		}

		if (filter.getPageSize() <= 0) {
			filter.setPageSize(25);
		}

		QTreatment q = QTreatment.treatment;
		BooleanExpression be = q.id.isNotNull();

		if (CommonUtils.isPositive(filter.getCaseId(), true)) {
			be = be.and(q.theCase.id.eq(filter.getCaseId().longValue()));
		} else {
			be = be.and(q.theCase.id.eq(0l));
		}

		if (CommonUtils.isPositive(filter.getDiseaseId(), true)) {
			be = be.and(q.disease.id.eq(filter.getDiseaseId().longValue()));
		} else {
			if (!CommonUtils.isEmpty(filter.getDiseaseCode())) {
				be = be.and(q.disease.code.equalsIgnoreCase(filter.getDiseaseCode()));
			} else {
				be = be.and(q.disease.id.eq(0l));
			}
		}

		Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize(),
				new Sort(new Order(Direction.DESC, "startDate")));
		Page<Treatment> page = repos.findAll(be, pageable);
		List<TreatmentDto> content = new ArrayList<>();

		page.getContent().parallelStream().forEachOrdered(t -> {
			content.add(new TreatmentDto(t));
		});

		return new PageImpl<>(content, pageable, page.getTotalElements());
	}

	@Override
	@Transactional(readOnly = true)
	public boolean hasMultipleMissingEndDate(TreatmentFilterDto filter) {

		if (filter == null || !CommonUtils.isPositive(filter.getCaseId(), true)
				|| (CommonUtils.isEmpty(filter.getDiseaseCode())
						&& !CommonUtils.isPositive(filter.getDiseaseId(), true))) {
			return false;
		}

		QTreatment q = QTreatment.treatment;
		BooleanExpression be = q.theCase.id.longValue().eq(filter.getCaseId()).and(q.endDate.isNull());

		if (CommonUtils.isPositive(filter.getDiseaseId(), true)) {
			be = be.and(q.disease.id.longValue().eq(filter.getDiseaseId()));
		} else if (!CommonUtils.isEmpty(filter.getDiseaseCode())) {
			be = be.and(q.disease.code.equalsIgnoreCase(filter.getDiseaseCode()));
		} else {
			// avoid returning query results if disease is not specified
			be = be.and(q.id.eq(0l));
		}

		long count = repos.count(be);

		return count > 1;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public TreatmentDto saveOne(TreatmentDto dto) {

		if (dto == null || dto.getTheCase() == null || dto.getDisease() == null) {
			throw new IllegalArgumentException("Treatment could not be null.");
		}

		Treatment entity = null;

		if (CommonUtils.isPositive(dto.getId(), true)) {
			entity = repos.findOne(dto.getId());
		}

		if (entity == null) {
			entity = dto.toEntity();
			entity.setUid(UUID.randomUUID());
		} else {
			entity.setRegimenLine(dto.getRegimenLine());
			entity.setRegimenName(dto.getRegimenName());
			entity.setStartDate(dto.getStartDate());
			entity.setEndDate(dto.getEndDate());
			entity.setEndingReason(dto.getEndingReason());
		}

		Case theCase = null;
		Organization org = null;
		Dictionary disease = null;
		Regimen regimen = null;

		if (CommonUtils.isPositive(dto.getTheCase().getId(), true)) {
			theCase = caseRepos.findOne(dto.getTheCase().getId());
		}

		if (CommonUtils.isPositive(dto.getOrganization().getId(), true)) {
			org = orgRepos.findOne(dto.getOrganization().getId());
		}

		if (CommonUtils.isPositive(dto.getDisease().getId(), true)) {
			disease = dicRepos.findOne(dto.getDisease().getId());
		}

		if (disease == null && !CommonUtils.isEmpty(dto.getDisease().getCode())) {
			disease = dicRepos.findOne(QDictionary.dictionary.code.equalsIgnoreCase(dto.getDisease().getCode()));
		}

		if (dto.getRegimen() != null && CommonUtils.isPositive(dto.getRegimen().getId(), true)) {
			regimen = regRepos.findOne(dto.getRegimen().getId());
		}

		if (theCase == null || org == null || disease == null) {
			throw new NullPointerException("No disease or case specified for a treatment record.");
		}

		entity.setTheCase(theCase);
		entity.setOrganization(org);
		entity.setDisease(disease);
		entity.setRegimen(regimen);

		entity = repos.save(entity);

		// Now find the latest treatment entry to update the case
		theCase = caseRepos.findOne(dto.getTheCase().getId());
		Iterator<Treatment> txItr = theCase.getTreatments().iterator();

		if (txItr.hasNext()) {

			Treatment tx = txItr.next();

			theCase.setCurrentArvRegimen(tx.getRegimen());
			theCase.setCurrentArvRegimenName(tx.getRegimenName());
			theCase.setCurrentArvRegimenLine(tx.getRegimenLine());
			theCase.setCurrentArvRegimenStartDate(tx.getStartDate());

			if (tx.getRegimenLine() == 1) {
				theCase.setSecondLineStartDate(null);
				theCase.setThirdLineStartDate(null);
				theCase.setFourthLineStartDate(null);
			}

			if (tx.getRegimenLine() == 2) {
				theCase.setSecondLineStartDate(tx.getStartDate());
				theCase.setThirdLineStartDate(null);
				theCase.setFourthLineStartDate(null);
			}

			if (tx.getRegimenLine() == 3) {
				theCase.setThirdLineStartDate(tx.getStartDate());
				theCase.setFourthLineStartDate(null);
			}

			theCase = caseRepos.save(theCase);
		}

		if (entity != null) {
			return new TreatmentDto(entity);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteOne(TreatmentDto dto) {

		if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
			return;
		}

		Treatment entity = repos.findOne(dto.getId());

		if (entity != null) {
			Case theCase = caseRepos.findOne(entity.getTheCase().getId());
			String diseaseCode = entity.getDisease().getCode();

			repos.delete(entity);

			// check if the latest one still match with the current regimen of the current
			// patient
			if (diseaseCode.equalsIgnoreCase("HIV")) {
				QTreatment q = QTreatment.treatment;
				Treatment latest = null;
				Iterator<Treatment> list = repos.findAll(
						q.theCase.id.longValue().eq(theCase.getId()).and(q.disease.code.equalsIgnoreCase(diseaseCode)),
						q.startDate.desc()).iterator();
				if (list.hasNext()) {
					latest = list.next();

					if (!theCase.getCurrentArvRegimenName().equalsIgnoreCase(latest.getRegimenName())
							|| !theCase.getCurrentArvRegimenStartDate().isEqual(latest.getStartDate())) {
						theCase.setCurrentArvRegimen(latest.getRegimen());
						theCase.setCurrentArvRegimenName(latest.getRegimenName());
						theCase.setCurrentArvRegimenLine(latest.getRegimenLine());
						theCase.setCurrentArvRegimenStartDate(latest.getStartDate());

						switch (latest.getRegimenLine()) {
						case 1:
							theCase.setSecondLineStartDate(null);
							theCase.setThirdLineStartDate(null);
							theCase.setFourthLineStartDate(null);
							break;
						case 2:
							theCase.setSecondLineStartDate(latest.getStartDate());
							break;
						case 3:
							theCase.setThirdLineStartDate(latest.getStartDate());
							break;
						case 4:
							theCase.setFourthLineStartDate(latest.getStartDate());
							break;
						}

						theCase = caseRepos.save(theCase);
					}
				}
			}
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteMultiple(TreatmentDto[] dtos) {

		if (CommonUtils.isEmpty(dtos)) {
			return;
		}

		for (TreatmentDto dto : dtos) {
			if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
				continue;
			}

			Treatment entity = repos.findOne(dto.getId());

			if (entity != null) {
				repos.delete(entity);
			}
		}
	}

}
