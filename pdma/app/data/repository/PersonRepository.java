package org.pepfar.pdma.app.data.repository;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.domain.PECase;
import org.pepfar.pdma.app.data.domain.Person;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository
		extends PagingAndSortingRepository<Person, Serializable>, QueryDslPredicateExecutor<Person> {

	@Query(
			value = "SELECT per.* FROM tbl_location loc INNER JOIN tbl_person per ON loc.person_id = per.id GROUP BY per.id HAVING COUNT(loc.id) = 1",
			nativeQuery = true)
	public List<Person> getPersonsByMissingAddress();

	@Query("SELECT p FROM Person p WHERE p.fullname = ?1")
	public Person findByNamePerson(String fullname);

	@Query("SELECT p FROM Person p WHERE p.uid = ?1")
	public Person findByUid(UUID uid);
}
