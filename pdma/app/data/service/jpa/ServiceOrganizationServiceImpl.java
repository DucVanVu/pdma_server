package org.pepfar.pdma.app.data.service.jpa;

import java.util.ArrayList;
import java.util.List;

import org.pepfar.pdma.app.data.aop.Loggable;
import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.domain.QServiceOrganization;
import org.pepfar.pdma.app.data.domain.Service;
import org.pepfar.pdma.app.data.domain.ServiceOrganization;
import org.pepfar.pdma.app.data.domain.ServiceOrganizationPK;
import org.pepfar.pdma.app.data.dto.ServiceOrganizationDto;
import org.pepfar.pdma.app.data.repository.OrganizationRepository;
import org.pepfar.pdma.app.data.repository.ServiceOrganizationRepository;
import org.pepfar.pdma.app.data.repository.ServiceRepository;
import org.pepfar.pdma.app.data.service.ServiceOrganizationService;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@org.springframework.stereotype.Service
public class ServiceOrganizationServiceImpl implements ServiceOrganizationService
{

	@Autowired
	private ServiceOrganizationRepository repos;

	@Autowired
	private ServiceRepository serviceRepos;

	@Autowired
	private OrganizationRepository orgRepos;

	@Override
	@Transactional(readOnly = true)
	public ServiceOrganizationDto findById(Long serviceId, Long organizationId) {

		if (!CommonUtils.isPositive(serviceId, true) || !CommonUtils.isPositive(organizationId, true)) {
			return null;
		}

		ServiceOrganizationPK pk = new ServiceOrganizationPK();
		pk.setService(serviceId);
		pk.setOrganization(organizationId);

		ServiceOrganization entity = repos.findOne(pk);

		if (entity != null) {
			return new ServiceOrganizationDto(entity);
		}

		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public List<ServiceOrganizationDto> findByServiceId(Long serviceId) {

		if (!CommonUtils.isPositive(serviceId, true)) {
			return new ArrayList<>();
		}

		Iterable<ServiceOrganization> entities = repos
				.findAll(QServiceOrganization.serviceOrganization.service.id.eq(serviceId.longValue()));

		List<ServiceOrganizationDto> dtos = new ArrayList<>();
		entities.forEach(entity -> {
			dtos.add(new ServiceOrganizationDto(entity));
		});

		return dtos;
	}

	@Override
	@Transactional(readOnly = true)
	public List<ServiceOrganizationDto> findByOrganizationId(Long organizationId) {

		if (!CommonUtils.isPositive(organizationId, true)) {
			return new ArrayList<>();
		}

		Iterable<ServiceOrganization> entities = repos
				.findAll(QServiceOrganization.serviceOrganization.organization.id.eq(organizationId.longValue()));

		List<ServiceOrganizationDto> dtos = new ArrayList<>();
		entities.forEach(entity -> {
			dtos.add(new ServiceOrganizationDto(entity));
		});

		return dtos;
	}

	@Override
	@Loggable
	@Transactional(rollbackFor = Exception.class)
	public ServiceOrganizationDto saveOne(ServiceOrganizationDto dto) {

		if (dto == null || dto.getService() == null || dto.getOrganization() == null
				|| !CommonUtils.isPositive(dto.getService().getId(), true)
				|| !CommonUtils.isPositive(dto.getOrganization().getId(), true)) {
			throw new IllegalArgumentException();
		}

		ServiceOrganizationPK pk = new ServiceOrganizationPK();
		pk.setService(dto.getService().getId());
		pk.setOrganization(dto.getOrganization().getId());

		ServiceOrganization entity = repos.findOne(pk);

		if (entity != null) {
			entity.setActive(dto.getActive());
			entity.setStartDate(dto.getStartDate());
			entity.setEndDate(dto.getEndDate());
			entity.setEndingReason(dto.getEndingReason());
		} else {
			entity = dto.toEntity();
		}

		// Service
		Service service = serviceRepos.findOne(dto.getService().getId());
		Organization org = orgRepos.findOne(dto.getOrganization().getId());

		if (service == null || org == null) {
			throw new IllegalArgumentException();
		}

		entity.setService(service);
		entity.setOrganization(org);

		entity = repos.save(entity);

		if (entity != null) {
			return new ServiceOrganizationDto(entity);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	@Loggable
	@Transactional(rollbackFor = Exception.class)
	public void deleteMultiple(ServiceOrganizationDto[] dtos) {
		if (CommonUtils.isEmpty(dtos)) {
			return;
		}

		for (ServiceOrganizationDto dto : dtos) {

			if (dto.getService() == null || dto.getOrganization() == null
					|| !CommonUtils.isPositive(dto.getService().getId(), true)
					|| !CommonUtils.isPositive(dto.getOrganization().getId(), true)) {
				continue;
			}

			ServiceOrganizationPK pk = new ServiceOrganizationPK();
			pk.setService(dto.getService().getId());
			pk.setOrganization(dto.getOrganization().getId());

			ServiceOrganization entity = repos.findOne(pk);

			if (entity != null) {
				repos.delete(entity);
			}
		}
	}

}
