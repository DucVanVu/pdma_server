package org.pepfar.pdma.app.data.service.jpa;

import org.pepfar.pdma.app.data.domain.*;
import org.pepfar.pdma.app.data.dto.CoviVacDto;
import org.pepfar.pdma.app.data.dto.CoviVacFilterDto;
import org.pepfar.pdma.app.data.repository.CaseRepository;
import org.pepfar.pdma.app.data.repository.CoviVacRepository;
import org.pepfar.pdma.app.data.repository.OrganizationRepository;
import org.pepfar.pdma.app.data.service.CoviVacService;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Service
public class CoviVacServiceImpl implements CoviVacService {

    @Autowired
    private CoviVacRepository repos;

    @Autowired
    private CaseRepository caseRepos;

    @Autowired
    private OrganizationRepository orgRepos;

    @Override
    public CoviVacDto findById(Long id) {
        if (!CommonUtils.isPositive(id, true)) {
            return null;
        }

        CoviVac entity = repos.findOne(id);

        if (entity != null) {
            return new CoviVacDto(entity);
        }

        return null;
    }

    @Override
    public List<CoviVacDto> findAll(CoviVacFilterDto filter) {
        if (filter == null || !CommonUtils.isPositive(filter.getCaseId(), true)) {
            return new ArrayList<>();
        }

        QCoviVac q = QCoviVac.coviVac;
        List<CoviVacDto> ret = new ArrayList<>();

        Iterator<CoviVac> itr = repos.findAll(q.theCase.id.longValue().eq(filter.getCaseId()),
                new Sort(new Sort.Order(Sort.Direction.DESC, "vaccinationDate"))).iterator();

        itr.forEachRemaining(s -> {
            ret.add(new CoviVacDto(s));
        });

        return ret;
    }

    @Override
    public Page<CoviVacDto> findAllPageable(CoviVacFilterDto filter) {
        if (filter == null) {
            filter = new CoviVacFilterDto();
        }

        if (filter.getPageIndex() < 0) {
            filter.setPageIndex(0);
        }

        if (filter.getPageSize() <= 0) {
            filter.setPageSize(25);
        }

        QClinicalStage q = QClinicalStage.clinicalStage;
        Sort defaultSort = new Sort(new Sort.Order(Sort.Direction.DESC, "evalDate"));
        Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize(), defaultSort);

        if (!CommonUtils.isPositive(filter.getCaseId(), true)) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }

        Page<CoviVac> page = repos.findAll(q.theCase.id.eq(filter.getCaseId().longValue()), pageable);
        List<CoviVacDto> content = new ArrayList<>();

        page.getContent().parallelStream().forEachOrdered(v -> {
            content.add(new CoviVacDto(v));
        });

        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    @Override
    public CoviVacDto saveOne(CoviVacDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Clinical stage data could not be null.");
        }

        CoviVac entity = null;
        if (CommonUtils.isPositive(dto.getId(), true)) {
            entity = repos.findOne(dto.getId());
        }

        if (entity == null) {
            entity = dto.toEntity();
            entity.setUid(UUID.randomUUID());
        } else {
            entity.setVaccinationDate(dto.getVaccinationDate());
            entity.setVaccineName(dto.getVaccineName());
            entity.setVaccineLotNumber(dto.getVaccineLotNumber());
            entity.setExpireDate(dto.getExpireDate());
            entity.setNote(dto.getNote());
        }

        Organization organization = null;
        Case theCase = null;

        if (dto.getOrganization() != null && CommonUtils.isPositive(dto.getOrganization().getId(), true)) {
            organization = orgRepos.findOne(dto.getOrganization().getId());
        }

        if (dto.getTheCase() != null && CommonUtils.isPositive(dto.getTheCase().getId(), true)) {
            theCase = caseRepos.findOne(dto.getTheCase().getId());
        }

        if (organization == null || theCase == null) {
            throw new RuntimeException("Could not find the case and/or the organization to save Covid vaccination data.");
        }

        entity.setOrganization(organization);
        entity.setTheCase(theCase);

        entity = repos.save(entity);

        if (entity != null) {
            return new CoviVacDto(entity);
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    public void deleteMultiple(CoviVacDto[] dtos) {
        if (CommonUtils.isEmpty(dtos)) {
            return;
        }

        for (CoviVacDto dto : dtos) {
            if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
                continue;
            }

            CoviVac entity = repos.findOne(dto.getId());

            if (entity != null) {
                repos.delete(entity);
            }
        }
    }
}
