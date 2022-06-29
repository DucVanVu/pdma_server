package org.pepfar.pdma.app.data.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.pepfar.pdma.app.data.types.SNSIdNumberType;

@Entity
@Table(name = "tbl_sns_case_id_number")
public class SNSCaseIdNumber implements Serializable {

	@Transient
	private static final long serialVersionUID = 4491294808808746383L;

	@Id
	@GenericGenerator(name = "native", strategy = "native")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "native")
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "id_number_type", nullable = true)
	@Enumerated(value = EnumType.STRING)
	private SNSIdNumberType idNumberType;

	@Column(name = "value", length = 100, nullable = false)
	private String value;


	@Column(name = "is_primary", nullable = false)
	private Boolean primary;

	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sns_case_id", nullable = false)
	private SNSCase snsCase;	

	public SNSCaseIdNumber() {
		super();
//		uuid = UUID.randomUUID();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public SNSIdNumberType getIdNumberType() {
		return idNumberType;
	}

	public void setIdNumberType(SNSIdNumberType idNumberType) {
		this.idNumberType = idNumberType;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Boolean getPrimary() {
		return primary;
	}

	public void setPrimary(Boolean primary) {
		this.primary = primary;
	}

	public SNSCase getSnsCase() {
		return snsCase;
	}

	public void setSnsCase(SNSCase snsCase) {
		this.snsCase = snsCase;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
}
