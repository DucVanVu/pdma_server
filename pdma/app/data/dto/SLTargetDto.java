package org.pepfar.pdma.app.data.dto;

import org.pepfar.pdma.app.data.domain.SLTarget;
import org.pepfar.pdma.app.data.types.ReportingIndicator;

public class SLTargetDto extends AuditableEntityDto
{

	private Long id;

	private ReportingIndicator indicator;

	private int fiscalYear;

	private OrganizationDto site;

	private long target;

	public SLTargetDto() {
	}

	public SLTargetDto(SLTarget entity) {
		super(entity);

		if (entity == null) {
			return;
		}

		this.id = entity.getId();
		this.indicator = entity.getIndicator();
		this.fiscalYear = entity.getFiscalYear();
		this.target = entity.getTarget();

		if (entity.getSite() != null) {
			this.site = new OrganizationDto(entity.getSite());
		}
	}

	public SLTarget toEntity() {
		SLTarget entity = new SLTarget();
		entity = (SLTarget) super.toEntity(entity);

		entity.setId(id);
		entity.setIndicator(indicator);
		entity.setFiscalYear(fiscalYear);
		entity.setTarget(target);

		if (site != null) {
			entity.setSite(site.toEntity());
		}

		return entity;
	}

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

	public OrganizationDto getSite() {
		return site;
	}

	public void setSite(OrganizationDto site) {
		this.site = site;
	}

	public long getTarget() {
		return target;
	}

	public void setTarget(long target) {
		this.target = target;
	}

}
