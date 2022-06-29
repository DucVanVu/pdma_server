package org.pepfar.pdma.app.data.repository;

import java.io.Serializable;

import org.pepfar.pdma.app.data.domain.Person;
import org.pepfar.pdma.app.data.domain.Staff;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StaffRepository
		extends PagingAndSortingRepository<Staff, Serializable>, QueryDslPredicateExecutor<Staff>
{
	@Query("SELECT s FROM Staff s WHERE s.staffCode = ?1")
	public Staff findByStaffCode(String staffCode);

}
