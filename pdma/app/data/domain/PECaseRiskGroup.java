package org.pepfar.pdma.app.data.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.pepfar.pdma.app.data.domain.auditing.AuditableEntity;
import org.pepfar.pdma.app.data.types.PERiskGroupEnum;

@Entity
@Table(name = "tbl_pe_case_risk_group")
public class PECaseRiskGroup extends AuditableEntity{

	@Transient
	private static final long serialVersionUID = -6155173141713986835L;
	
	@Id
	@GenericGenerator(name = "native", strategy = "native")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "native")
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "pe_case_id", nullable = false)
	private PECase peCase;
	
	
	@Column(name = "name", nullable = false)	
	private String name;

	@Column(name = "val", nullable = false)
	@Enumerated(value = EnumType.STRING)
	private PERiskGroupEnum val;

	@Column(name = "is_main_risk", nullable = true)
	private Boolean isMainRisk;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public PECase getPeCase() {
		return peCase;
	}

	public void setPeCase(PECase peCase) {
		this.peCase = peCase;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PERiskGroupEnum getVal() {
		return val;
	}

	public void setVal(PERiskGroupEnum val) {
		this.val = val;
	}

	public Boolean getIsMainRisk() {
		return isMainRisk;
	}

	public void setIsMainRisk(Boolean isMainRisk) {
		this.isMainRisk = isMainRisk;
	}
}
