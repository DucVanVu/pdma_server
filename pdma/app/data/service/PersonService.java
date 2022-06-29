package org.pepfar.pdma.app.data.service;

import org.pepfar.pdma.app.data.dto.PersonDto;

public interface PersonService {

	public byte[] getPhoto(Long personId);

	public PersonDto savePhoto(PersonDto dto);

	public PersonDto uploadPhoto(PersonDto dto);

}
