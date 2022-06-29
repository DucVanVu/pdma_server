package org.pepfar.pdma.app.data.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.pepfar.pdma.app.data.domain.WeeklyReport;
import org.pepfar.pdma.app.utils.CustomLocalDateTimeDeserializer2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class WeeklyReportDto extends AuditableEntityDto {

	private Long id;

	private OrganizationDto organization;

	private String name;

	private boolean freezed;

	// 0 = drafting, 1 = pending-approval, 2 = approved, 3 = published
	private int status;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime fromDate;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime toDate;

	private Integer htsTst;

	private Set<WRCaseDto> cases = new LinkedHashSet<>();

	private Integer htsPos;

	private Integer txNew;

	private Integer txNewBreakdown1;

	private Integer txNewBreakdown2;

	private Integer txNewBreakdown3;

	private String comment;

	private String note;

	private Boolean dapproved;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime submissionDate;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime approvalDate;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime publishDate;

	public WeeklyReportDto() {
	}

	public WeeklyReportDto(WeeklyReport entity) {

		super(entity);

		if (entity == null) {
			return;
		}

		this.id = entity.getId();
		this.name = entity.getName();
		this.freezed = entity.isFreezed();
		this.status = entity.getStatus();
		this.fromDate = entity.getFromDate();
		this.toDate = entity.getToDate();
		this.htsTst = entity.getHtsTst();
		this.htsPos = entity.getHtsPos();
		this.txNew = entity.getTxNew();
		this.txNewBreakdown1 = entity.getTxNewBreakdown1();
		this.txNewBreakdown2 = entity.getTxNewBreakdown2();
		this.txNewBreakdown3 = entity.getTxNewBreakdown3();
		this.comment = entity.getComment();
		this.note = entity.getNote();
		this.dapproved = entity.isDapproved();
		this.submissionDate = entity.getSubmissionDate();
		this.approvalDate = entity.getApprovalDate();
		this.publishDate = entity.getPublishDate();

		if (entity.getOrganization() != null) {
			organization = new OrganizationDto(entity.getOrganization());
		}

//		if (entity.getCases() != null) {
//			List<WRCaseDto> dtos = new ArrayList<>();
//
//			entity.getCases().parallelStream().forEachOrdered(e -> {
//				dtos.add(new WRCaseDto(e));
//			});
//
//			this.cases.clear();
//			this.cases.addAll(dtos);
//		}
	}

	public WeeklyReport toEntity() {
		WeeklyReport entity = new WeeklyReport();
		entity = (WeeklyReport) super.toEntity(entity);

		entity.setId(id);
		entity.setName(name);
		entity.setFreezed(freezed);
		entity.setStatus(status);
		entity.setFromDate(fromDate);
		entity.setToDate(toDate);
		entity.setHtsTst(htsTst);
		entity.setHtsPos(htsPos);
		entity.setTxNew(txNew);
		entity.setTxNewBreakdown1(txNewBreakdown1);
		entity.setTxNewBreakdown2(txNewBreakdown2);
		entity.setTxNewBreakdown3(txNewBreakdown3);
		entity.setComment(comment);
		entity.setNote(note);
		entity.setDapproved(dapproved);
		entity.setSubmissionDate(submissionDate);
		entity.setApprovalDate(approvalDate);
		entity.setPublishDate(publishDate);

		return entity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public OrganizationDto getOrganization() {
		return organization;
	}

	public void setOrganization(OrganizationDto organization) {
		this.organization = organization;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isFreezed() {
		return freezed;
	}

	public void setFreezed(boolean freezed) {
		this.freezed = freezed;
	}

	public Integer getHtsTst() {
		if (htsTst == null) {
			htsTst = 0;
		}

		return htsTst;
	}

	public void setHtsTst(Integer htsTst) {
		this.htsTst = htsTst;
	}

	public Set<WRCaseDto> getCases() {

		if (cases == null) {
			cases = new LinkedHashSet<>();
		}

		return cases;
	}

	public void setCases(Set<WRCaseDto> cases) {
		this.cases = cases;
	}

	public Integer getHtsPos() {
		if (htsPos == null) {
			htsPos = 0;
		}

		return htsPos;
	}

	public void setHtsPos(Integer htsPos) {
		this.htsPos = htsPos;
	}

	public Integer getTxNew() {
		if (txNew == null) {
			txNew = 0;
		}

		return txNew;
	}

	public void setTxNew(Integer txNew) {
		this.txNew = txNew;
	}

	public Integer getTxNewBreakdown1() {
		if (txNewBreakdown1 == null) {
			txNewBreakdown1 = 0;
		}

		return txNewBreakdown1;
	}

	public void setTxNewBreakdown1(Integer txNewBreakdown1) {
		this.txNewBreakdown1 = txNewBreakdown1;
	}

	public Integer getTxNewBreakdown2() {
		if (txNewBreakdown2 == null) {
			txNewBreakdown2 = 0;
		}

		return txNewBreakdown2;
	}

	public void setTxNewBreakdown2(Integer txNewBreakdown2) {
		this.txNewBreakdown2 = txNewBreakdown2;
	}

	public Integer getTxNewBreakdown3() {
		if (txNewBreakdown3 == null) {
			txNewBreakdown3 = 0;
		}

		return txNewBreakdown3;
	}

	public void setTxNewBreakdown3(Integer txNewBreakdown3) {
		this.txNewBreakdown3 = txNewBreakdown3;
	}

	public LocalDateTime getFromDate() {
		return fromDate;
	}

	public void setFromDate(LocalDateTime fromDate) {
		this.fromDate = fromDate;
	}

	public LocalDateTime getToDate() {
		return toDate;
	}

	public void setToDate(LocalDateTime toDate) {
		this.toDate = toDate;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public Boolean isDapproved() {
		return dapproved;
	}

	public void setDapproved(Boolean dapproved) {
		this.dapproved = dapproved;
	}

	public Boolean getDapproved() {
		return dapproved;
	}

	public LocalDateTime getSubmissionDate() {
		return submissionDate;
	}

	public void setSubmissionDate(LocalDateTime submissionDate) {
		this.submissionDate = submissionDate;
	}

	public LocalDateTime getApprovalDate() {
		return approvalDate;
	}

	public void setApprovalDate(LocalDateTime approvalDate) {
		this.approvalDate = approvalDate;
	}

	public LocalDateTime getPublishDate() {
		return publishDate;
	}

	public void setPublishDate(LocalDateTime publishDate) {
		this.publishDate = publishDate;
	}

}
