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
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.GenericGenerator;
import org.pepfar.pdma.app.data.domain.auditing.AuditableEntity;
import org.pepfar.pdma.app.data.types.ReportingIndicator;

@Entity
@Table(
		name = "tbl_sitelevel_target",
		uniqueConstraints = @UniqueConstraint(columnNames = { "fiscal_year", "indicator", "site_id" }))
public class SLTarget extends AuditableEntity
{

	@Transient
	private static final long serialVersionUID = 5110400350013067263L;

	@Id
	@GenericGenerator(name = "native", strategy = "native")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "native")
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Enumerated(value = EnumType.STRING)
	@Column(name = "indicator", nullable = false)
	private ReportingIndicator indicator;

	@Column(name = "fiscal_year", nullable = false)
	private int fiscalYear;

	@ManyToOne
	@JoinColumn(name = "site_id", nullable = false)
	private Organization site;

	@Column(name = "target", nullable = false)
	private long target;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ReportingIndicator getIndicator() {
		return indicator;
	}

	public void setIndicator(ReportingIndicator indicator) {
		this.indicator = indicator;
	}

	public int getFiscalYear() {
		return fiscalYear;
	}

	public void setFiscalYear(int fiscalYear) {
		this.fiscalYear = fiscalYear;
	}

	public Organization getSite() {
		return site;
	}

	public void setSite(Organization site) {
		this.site = site;
	}

	public long getTarget() {
		return target;
	}

	public void setTarget(long target) {
		this.target = target;
	}

}
