package org.pepfar.pdma.app.data.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.pepfar.pdma.app.utils.LocalDateTimeAttributeConverter;

@Entity
@Table(name = "tbl_wr_plain")
public class WeeklyReportPlain implements Serializable {

	@Transient
	private static final long serialVersionUID = 2561233557645710161L;

	@Id
	@GenericGenerator(name = "native", strategy = "native")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "native")
	@Column(name = "id", unique = true, nullable = false, updatable = false)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "org_id", nullable = false)
	private Organization organization;

	@OneToOne(orphanRemoval = true)
	@JoinColumn(name = "report_id", nullable = true, unique = true)
	private WeeklyReport report;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "from_date", nullable = false)
	private LocalDateTime fromDate;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "to_date", nullable = false)
	private LocalDateTime toDate;

	@Column(name = "hts_tst", nullable = true)
	private Integer htsTst;

	// To be filled out when synthesizing
	@Column(name = "hts_pos", nullable = true)
	private Integer htsPos;

	// HTS_POS: Chan doan moi
	@Column(name = "hts_pos_breakdown1", nullable = true)
	private Integer htsPosBreakdown1;

	// HTS_POS: Chan doan cu
	@Column(name = "hts_pos_breakdown2", nullable = true)
	private Integer htsPosBreakdown2;

	// HTS_POS: Ngoai tinh
	@Column(name = "hts_pos_breakdown3", nullable = true)
	private Integer htsPosBreakdown3;

	// HTS_POS: Chua xac dinh
	@Column(name = "hts_pos_breakdown4", nullable = true)
	private Integer htsPosBreakdown4;

	// HTS_POS: RTRI+
	@Column(name = "hts_pos_breakdown5", nullable = true)
	private Integer htsPosBreakdown5;

	// HTS_POS: Offered PNS
	@Column(name = "hts_pos_breakdown6", nullable = true)
	private Integer htsPosBreakdown6;

	// To be filled out when synthesizing
	@Column(name = "tx_new", nullable = true)
	private Integer txNew;

	// TX_NEW: Dieu tri moi. To be filled out when synthesizing
	@Column(name = "tx_new_breakdown1", nullable = true)
	private Integer txNewBreakdown1;

	// TX_NEW: Cu chua dieu tri. To be filled out when synthesizing
	@Column(name = "tx_new_breakdown2", nullable = true)
	private Integer txNewBreakdown2;

	// TX_NEW: Same day ART. To be filled out when synthesizing
	@Column(name = "tx_new_breakdown3", nullable = true)
	private Integer txNewBreakdown3;

	// TX_NEW: Cu bo tri. To be filled out when synthesizing
	@Column(name = "tx_new_breakdown4", nullable = true)
	private Integer txNewBreakdown4;

	// TX_NEW: Ngoai tinh. To be filled out when synthesizing
	@Column(name = "tx_new_breakdown5", nullable = true)
	private Integer txNewBreakdown5;

	// TX_NEW: Chua xac dinh. To be filled out when synthesizing
	@Column(name = "tx_new_breakdown6", nullable = true)
	private Integer txNewBreakdown6;

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

	public WeeklyReport getReport() {
		return report;
	}

	public void setReport(WeeklyReport report) {
		this.report = report;
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

	public Integer getHtsTst() {
		if (htsTst == null) {
			htsTst = 0;
		}

		return htsTst;
	}

	public void setHtsTst(Integer htsTst) {
		this.htsTst = htsTst;
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

	public Integer getHtsPosBreakdown1() {
		if (htsPosBreakdown1 == null) {
			htsPosBreakdown1 = 0;
		}
		return htsPosBreakdown1;
	}

	public void setHtsPosBreakdown1(Integer htsPosBreakdown1) {
		this.htsPosBreakdown1 = htsPosBreakdown1;
	}

	public Integer getHtsPosBreakdown2() {
		if (htsPosBreakdown2 == null) {
			htsPosBreakdown2 = 0;
		}
		return htsPosBreakdown2;
	}

	public void setHtsPosBreakdown2(Integer htsPosBreakdown2) {
		this.htsPosBreakdown2 = htsPosBreakdown2;
	}

	public Integer getHtsPosBreakdown3() {
		if (htsPosBreakdown3 == null) {
			htsPosBreakdown3 = 0;
		}
		return htsPosBreakdown3;
	}

	public void setHtsPosBreakdown3(Integer htsPosBreakdown3) {
		this.htsPosBreakdown3 = htsPosBreakdown3;
	}

	public Integer getHtsPosBreakdown4() {
		if (htsPosBreakdown4 == null) {
			htsPosBreakdown4 = 0;
		}
		return htsPosBreakdown4;
	}

	public void setHtsPosBreakdown4(Integer htsPosBreakdown4) {
		this.htsPosBreakdown4 = htsPosBreakdown4;
	}

	public Integer getHtsPosBreakdown5() {
		return (htsPosBreakdown5 != null) ? htsPosBreakdown5 : 0;
	}

	public void setHtsPosBreakdown5(Integer htsPosBreakdown5) {
		this.htsPosBreakdown5 = htsPosBreakdown5;
	}

	public Integer getHtsPosBreakdown6() {
		return (htsPosBreakdown6 != null) ? htsPosBreakdown6 : 0;
	}

	public void setHtsPosBreakdown6(Integer htsPosBreakdown6) {
		this.htsPosBreakdown6 = htsPosBreakdown6;
	}

	public Integer getTxNewBreakdown4() {
		if (txNewBreakdown4 == null) {
			txNewBreakdown4 = 0;
		}
		return txNewBreakdown4;
	}

	public void setTxNewBreakdown4(Integer txNewBreakdown4) {
		this.txNewBreakdown4 = txNewBreakdown4;
	}

	public Integer getTxNewBreakdown5() {
		if (txNewBreakdown5 == null) {
			txNewBreakdown5 = 0;
		}
		return txNewBreakdown5;
	}

	public void setTxNewBreakdown5(Integer txNewBreakdown5) {
		this.txNewBreakdown5 = txNewBreakdown5;
	}

	public Integer getTxNewBreakdown6() {
		if (txNewBreakdown6 == null) {
			txNewBreakdown6 = 0;
		}
		return txNewBreakdown6;
	}

	public void setTxNewBreakdown6(Integer txNewBreakdown6) {
		this.txNewBreakdown6 = txNewBreakdown6;
	}

}
