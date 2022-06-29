package org.pepfar.pdma.app.data.domain;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.pepfar.pdma.app.data.domain.auditing.AuditableEntity;
import org.pepfar.pdma.app.utils.LocalDateTimeAttributeConverter;

@Entity
@Table(name = "tbl_weekly_report")
public class WeeklyReport extends AuditableEntity {

	@Transient
	private static final long serialVersionUID = 8372410173739914307L;

	@Id
	@GenericGenerator(name = "native", strategy = "native")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "native")
	@Column(name = "id", unique = true, nullable = false, updatable = false)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "org_id", nullable = false)
	private Organization organization;

	@Column(name = "name", length = 150, nullable = false, unique = false)
	private String name;

	@Column(name = "freezed", nullable = false)
	private boolean freezed;

	// 0 = drafting, 1 = pending-approval, 2 = approved, 3 = published
	@Column(name = "status", nullable = false)
	private int status;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "from_date", nullable = false)
	private LocalDateTime fromDate;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "to_date", nullable = false)
	private LocalDateTime toDate;

	@Column(name = "hts_tst", nullable = true)
	private Integer htsTst;

	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinTable(
			name = "tbl_wreport_cases",
			joinColumns = @JoinColumn(name = "report_id", referencedColumnName = "id"),
			inverseJoinColumns = @JoinColumn(name = "case_id", referencedColumnName = "id"))
	@OrderBy(value = "fullname ASC")
	private Set<WRCase> cases = new LinkedHashSet<>();

	// To be filled out when synthesizing
	@Column(name = "hts_pos", nullable = true)
	private Integer htsPos;

	// To be filled out when synthesizing
	@Column(name = "tx_new", nullable = true)
	private Integer txNew;

	// TX_NEW: Ca moi. To be filled out when synthesizing
	@Column(name = "tx_new_breakdown1", nullable = true)
	private Integer txNewBreakdown1;

	// TX_NEW: Ca cu. To be filled out when synthesizing
	@Column(name = "tx_new_breakdown2", nullable = true)
	private Integer txNewBreakdown2;

	// TX_NEW: Same day ART. To be filled out when synthesizing
	@Column(name = "tx_new_breakdown3", nullable = true)
	private Integer txNewBreakdown3;

	@Lob
	@Column(name = "comment", nullable = true)
	private String comment;

	@Lob
	@Column(name = "note", nullable = true)
	private String note;

	@Column(name = "dapproved", nullable = true)
	private Boolean dapproved; // district staff already approved

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "site_submission_date", nullable = true)
	private LocalDateTime submissionDate;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "prov_approve_date", nullable = true)
	private LocalDateTime approvalDate;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "epic_publish_date", nullable = true)
	private LocalDateTime publishDate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
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

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
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

	public Set<WRCase> getCases() {

		if (cases == null) {
			cases = new LinkedHashSet<>();
		}

		return cases;
	}

	public void setCases(Set<WRCase> cases) {
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
