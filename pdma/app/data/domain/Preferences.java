package org.pepfar.pdma.app.data.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.pepfar.pdma.app.data.domain.auditing.AuditableEntity;

@Entity
@Table(name = "tbl_preferences")
public class Preferences extends AuditableEntity {

	@Transient
	private static final long serialVersionUID = 4490291284686929806L;

	@Id
	@GenericGenerator(name = "native", strategy = "native")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "native")
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "preference_name", unique = true, length = 100, nullable = false)
	private String name;

	@Column(name = "preference_value", length = 512, nullable = false)
	private String value;

	// -----------------------------------------------------------
	// GETTERS/SETTERS
	// -----------------------------------------------------------

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
