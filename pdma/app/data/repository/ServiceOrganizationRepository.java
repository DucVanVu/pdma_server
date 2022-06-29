package org.pepfar.pdma.app.data.repository;

import java.io.Serializable;

import org.pepfar.pdma.app.data.domain.ServiceOrganization;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceOrganizationRepository extends PagingAndSortingRepository<ServiceOrganization, Serializable>,
		QueryDslPredicateExecutor<ServiceOrganization>
{

}
