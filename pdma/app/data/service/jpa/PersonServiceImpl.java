package org.pepfar.pdma.app.data.service.jpa;

import org.pepfar.pdma.app.data.domain.Person;
import org.pepfar.pdma.app.data.dto.PersonDto;
import org.pepfar.pdma.app.data.repository.PersonRepository;
import org.pepfar.pdma.app.data.service.PersonService;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PersonServiceImpl implements PersonService {

	@Autowired
	private PersonRepository repos;

	@Override
	@Transactional(readOnly = true)
	public byte[] getPhoto(Long personId) {
		if (!CommonUtils.isPositive(personId, true)) {
			return null;
		}

		Person person = repos.findOne(personId);

		if (person == null) {
			return null;
		}

		return person.getImage();
	}

	@Override
	public PersonDto savePhoto(PersonDto dto) {
		if (CommonUtils.isNull(dto)) {
			throw new RuntimeException();
		}

		Person person = null;

		if (CommonUtils.isPositive(dto.getId(), true)) {
			person = repos.findOne(dto.getId());
		}

//		if (person.getUid() == null) {
//			if (person.getUid() == null) {
//				person.setUid(UUID.randomUUID());
//			} else {
//				person.setUid(dto.getUid());
//			}
//		}

		if (person == null) {
			throw new RuntimeException();
		}

		person.setImage(dto.getImage());

		// Save
		person = repos.save(person);

		if (person != null) {
			return new PersonDto(person, false);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	public PersonDto uploadPhoto(PersonDto dto) {
		// TODO Auto-generated method stub
		return null;
	}

}
//	UPDATE pdma4.tbl_person SET uuid = (SELECT UUID()) WHERE uuid is null;