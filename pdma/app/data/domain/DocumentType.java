package org.pepfar.pdma.app.data.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "tbl_document_type")
public class DocumentType implements Serializable {

	@Transient
	private static final long serialVersionUID = 4542169758101674342L;

	@Id
	@GenericGenerator(name = "native", strategy = "native")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "native")
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "code", length = 100, unique = true, nullable = true)
	private String code;

	@Column(name = "name", length = 100, nullable = false)
	private String name;

	@Column(name = "is_active", nullable = false)
	private Boolean active;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31).append(id).append(code).append(name).append(active).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null) {
			return false;
		}

		if (!(obj instanceof DocumentType)) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		DocumentType that = (DocumentType) obj;

		return new EqualsBuilder().append(id, that.id).append(code, that.code).append(name, that.name)
				.append(active, that.active).isEquals();
	}
}
