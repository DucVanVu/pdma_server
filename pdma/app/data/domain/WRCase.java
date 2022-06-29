package org.pepfar.pdma.app.data.domain;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;
import org.pepfar.pdma.app.data.domain.auditing.AuditableEntity;
import org.pepfar.pdma.app.data.types.Gender;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.LocalDateTimeAttributeConverter;

@Entity
@Table(name = "tbl_wr_case")
public class WRCase extends AuditableEntity {

	@Transient
	private static final long serialVersionUID = 6299673416265768278L;

	@Id
	@GenericGenerator(name = "native", strategy = "native")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "native")
	@Column(name = "id", unique = true, nullable = false, updatable = false)
	private Long id;

//	@Convert(converter = CryptoConverter.class)
	@Column(name = "fullname", length = 2048, nullable = false)
	private String fullname;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "dob", nullable = true)
	private LocalDateTime dob;

	@Column(name = "gender", nullable = false)
	private Gender gender;

//	@Convert(converter = CryptoConverter.class)
	@Column(name = "nid", length = 1024, nullable = true)
	private String nationalId;

	@OneToMany(mappedBy = "wrCase", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private Set<Location> locations = new LinkedHashSet<>();

//	@OneToOne(cascade = CascadeType.ALL)
//	@JoinColumn(name = "resident_address_id", nullable = false)
//	private Location residentialAddress;
//
//	@OneToOne(cascade = CascadeType.ALL)
//	@JoinColumn(name = "current_address_id", nullable = false)
//	private Location currentAddress;

	@ManyToOne
	@JoinColumn(name = "vct_id", nullable = true)
	private Organization vct;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "screening_date", nullable = true)
	private LocalDateTime screeningDate;

	@Column(name = "screening_site", length = 1024, nullable = true)
	private String screeningSite;

	@Column(name = "hiv_confirm_id", length = 100, nullable = true)
	private String hivConfirmId;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "confirm_date", nullable = false)
	private LocalDateTime confirmDate;

	@ManyToOne
	@JoinColumn(name = "confirm_lab_id", nullable = false)
	private HIVConfirmLab confirmLab;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "enrollment_date", nullable = true)
	private LocalDateTime enrollmentDate;

	@ManyToOne
	@JoinColumn(name = "opc_id", nullable = true)
	private Organization opc;

	@Column(name = "patient_chart_id", length = 100, nullable = true)
	private String patientChartId;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "arv_initiation_date", nullable = true)
	private LocalDateTime arvInitiationDate;

	@Column(name = "hts_case_status", nullable = false)
	private int htsCaseStatus; // 0 = undetermined, 1 = new, 2 = old, 4 = Ngoai tinh

	@Column(name = "tx_case_status", nullable = false)
	private int txCaseStatus; // 0 = undetermined, 1 = new, 2 = old chua DT, 3 = Ca cu bo tri, 4 = Ngoai tinh

	@Column(name = "note", length = 512, nullable = true)
	private String note;

	@ManyToMany(mappedBy = "cases")
	public Set<WeeklyReport> reports = new HashSet<>();

	@Column(name = "current_address_id", nullable = true)
	private Long currentAddressId;

	@Column(name = "resident_address_id", nullable = true)
	private Long residentAddressId;

	// Nhiễm mới-xét nghiệm nhanh dương tính
	@Column(name = "rtri_pos", nullable = true)
	private Integer rtriPos; // -1 = không xác định, null/0 = không thực hiện, 1 = nhiễm mới, 2 = nhiễm lâu

	// Được tham gia chương trình TBXNBTBC
	@Column(name = "offered_pns", nullable = true)
	private Integer offeredPns; // null/0 = không xác định, 1 = được tư vấn tham gia, 2 = không được tư vấn tham
								// gia

	@Column(name = "linked2_opcassist", nullable = true)
	private Boolean linked2OpcAssist;

	public Long getCurrentAddressId() {
		return currentAddressId;
	}

	public void setCurrentAddressId(Long currentAddressId) {
		this.currentAddressId = currentAddressId;
	}

	public Long getResidentAddressId() {
		return residentAddressId;
	}

	public void setResidentAddressId(Long residentAddressId) {
		this.residentAddressId = residentAddressId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public LocalDateTime getDob() {
		return dob;
	}

	public void setDob(LocalDateTime dob) {
		this.dob = dob;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public String getNationalId() {
		return nationalId;
	}

	public void setNationalId(String nationalId) {
		this.nationalId = nationalId;
	}

//	public Location getResidentialAddress() {
//		return residentialAddress;
//	}
//
//	public void setResidentialAddress(Location residentialAddress) {
//		this.residentialAddress = residentialAddress;
//	}
//
//	public Location getCurrentAddress() {
//		return currentAddress;
//	}
//
//	public void setCurrentAddress(Location currentAddress) {
//		this.currentAddress = currentAddress;
//	}

	public Set<Location> getLocations() {
		if (locations == null) {
			locations = new LinkedHashSet<>();
		}

		return locations;
	}

	public void setLocations(Set<Location> locations) {
		this.locations = locations;
	}

	public Organization getVct() {
		return vct;
	}

	public void setVct(Organization vct) {
		this.vct = vct;
	}

	public String getHivConfirmId() {
		return hivConfirmId;
	}

	public void setHivConfirmId(String hivConfirmId) {
		this.hivConfirmId = hivConfirmId;
	}

	public LocalDateTime getScreeningDate() {
		return screeningDate;
	}

	public void setScreeningDate(LocalDateTime screeningDate) {
		this.screeningDate = screeningDate;
	}

	public String getScreeningSite() {
		return screeningSite;
	}

	public void setScreeningSite(String screeningSite) {
		this.screeningSite = screeningSite;
	}

	public LocalDateTime getConfirmDate() {
		return confirmDate;
	}

	public void setConfirmDate(LocalDateTime confirmDate) {
		this.confirmDate = confirmDate;
	}

	public HIVConfirmLab getConfirmLab() {
		return confirmLab;
	}

	public void setConfirmLab(HIVConfirmLab confirmLab) {
		this.confirmLab = confirmLab;
	}

	public LocalDateTime getEnrollmentDate() {
		return enrollmentDate;
	}

	public void setEnrollmentDate(LocalDateTime enrollmentDate) {
		this.enrollmentDate = enrollmentDate;
	}

	public Organization getOpc() {
		return opc;
	}

	public void setOpc(Organization opc) {
		this.opc = opc;
	}

	public String getPatientChartId() {
		return patientChartId;
	}

	public void setPatientChartId(String patientChartId) {
		this.patientChartId = patientChartId;
	}

	public LocalDateTime getArvInitiationDate() {
		return arvInitiationDate;
	}

	public void setArvInitiationDate(LocalDateTime arvInitiationDate) {
		this.arvInitiationDate = arvInitiationDate;
	}

	public int getHtsCaseStatus() {
		return htsCaseStatus;
	}

	public void setHtsCaseStatus(int htsCaseStatus) {
		this.htsCaseStatus = htsCaseStatus;
	}

	public int getTxCaseStatus() {
		return txCaseStatus;
	}

	public void setTxCaseStatus(int txCaseStatus) {
		this.txCaseStatus = txCaseStatus;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public Set<WeeklyReport> getReports() {
		return reports;
	}

	public void setReports(Set<WeeklyReport> reports) {
		this.reports = reports;
	}

	public int getRtriPos() {
		return rtriPos == null ? 0 : rtriPos;
	}

	public void setRtriPos(Integer rtriPos) {
		this.rtriPos = rtriPos;
	}

	public int getOfferedPns() {
		return offeredPns == null ? 0 : offeredPns;
	}

	public void setOfferedPns(Integer offeredPns) {
		this.offeredPns = offeredPns;
	}

	public Boolean getLinked2OpcAssist() {
		return linked2OpcAssist;
	}

	public void setLinked2OpcAssist(Boolean linked2OpcAssist) {
		this.linked2OpcAssist = linked2OpcAssist;
	}

	public String getOfferedPnsLabel() {

		if (offeredPns == null) {
			return null;
		}

		switch (offeredPns) {
			case 1:
				return "Được tư vấn";
			case 2:
				return "Không được tư vấn";
			default:
				return "Không có thông tin";
		}
	}

	public String getRtriPosLabel() {

		if (rtriPos == null) {
			return null;
		}

		switch (rtriPos) {
			case 1:
				return "Mới nhiễm - Test nhanh";
			case 2:
				return "Nhiễm lâu";
			case -1:
				return "Không đủ tiêu chuẩn thực hiện XN";
			default:
				return "Không đồng ý xét nghiệm";
		}
	}

	@Transient
	public boolean equals(Object obj) {

		if (!(obj instanceof WRCase)) {
			return false;
		}

		if (!CommonUtils.isPositive(this.id, true) || !CommonUtils.isPositive(((WRCase) obj).getId(), true)) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		return this.id.longValue() == ((WRCase) obj).getId().longValue();
	}

}
