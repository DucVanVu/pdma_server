package org.pepfar.pdma.app.data.dto;

import java.util.HashSet;
import java.util.Set;

import org.pepfar.pdma.app.data.types.DictionaryType;

public class DictionaryTypeDto
{

	private DictionaryType type;

	private String title;

	private Set<DictionaryDto> data = new HashSet<>();

	public DictionaryType getType() {
		return type;
	}

	public void setType(DictionaryType type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Set<DictionaryDto> getData() {

		if (data == null) {
			data = new HashSet<>();
		}

		return data;
	}

	public void setData(Set<DictionaryDto> data) {
		this.data = data;
	}

}
