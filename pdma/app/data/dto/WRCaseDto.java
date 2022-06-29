package org.pepfar.pdma.app.data.dto;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.pepfar.pdma.app.data.domain.WRCase;
import org.pepfar.pdma.app.data.types.Gender;
import org.pepfar.pdma.app.utils.CustomLocalDateTimeDeserializer2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class WRCaseDto extends AuditableEntityDto {

	private Long id;

	private String fullname;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime dob;

	private Gender gender;

	private String nationalId;

	private Set<LocationDto> locations = new LinkedHashSet<>();

//	private LocationDto residentialAddress;
//
//	private LocationDto currentAddress;

	private OrganizationDto vct;

	private String hivConfirmId;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime screeningDate;

	private String screeningSite;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime confirmDate;

	private HIVConfirmLabDto confirmLab;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime enrollmentDate;

	private OrganizationDto opc;

	private String patientChartId;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime arvInitiationDate;

	// 0 = undetermined, 1 = new, 2 = old, 4 = Ngoai tinh
	private int htsCaseStatus;

	// 0 = undetermined, 1 = new, 2 = old chua DT, 3 = Ca cu bo tri, 4 = Ngoai tinh
	private int txCaseStatus;

	private String note;

	private Set<WeeklyReportDto> reports = new HashSet<>();

	private Integer rtriPos;

	private Integer offeredPns;

	private String rtriPosLabel;

	private String offeredPnsLabel;

	private Boolean linked2OpcAssist;

	private Boolean transed2OpcAssist; // temp variable for view only

	private String patientChartIdInOpcAssist;

	public WRCaseDto() {
	}

	public WRCaseDto(WRCase entity) {
		super(entity);

		if (entity == null) {
			return;
		}

		this.id = entity.getId();
		this.fullname = entity.getFullname();
		this.dob = entity.getDob();
		this.gender = entity.getGender();
		this.nationalId = entity.getNationalId();
		this.hivConfirmId = entity.getHivConfirmId();
		this.screeningDate = entity.getScreeningDate();
		this.screeningSite = entity.getScreeningSite();
		this.confirmDate = entity.getConfirmDate();
		this.enrollmentDate = entity.getEnrollmentDate();
		this.arvInitiationDate = entity.getArvInitiationDate();
		this.patientChartId = entity.getPatientChartId();
		this.htsCaseStatus = entity.getHtsCaseStatus();
		this.txCaseStatus = entity.getTxCaseStatus();
		this.note = entity.getNote();

		this.rtriPos = entity.getRtriPos();
		this.offeredPns = entity.getOfferedPns();
		this.rtriPosLabel = entity.getRtriPosLabel();
		this.offeredPnsLabel = entity.getOfferedPnsLabel();

		this.linked2OpcAssist = entity.getLinked2OpcAssist();

		if (entity.getLocations() != null) {
			entity.getLocations().forEach(loc -> {
				locations.add(new LocationDto(loc));
			});
		}

		if (entity.getVct() != null) {
			this.vct = new OrganizationDto(entity.getVct());
		}

		if (entity.getConfirmLab() != null) {
			this.confirmLab = new HIVConfirmLabDto(entity.getConfirmLab());
		}

		if (entity.getOpc() != null) {
			this.opc = new OrganizationDto(entity.getOpc());
		}

		if (entity.getReports() != null) {
			entity.getReports().forEach(e -> {
				WeeklyReportDto wrd = new WeeklyReportDto();
				wrd.setId(e.getId());

				this.reports.add(wrd);
			});
		}
	}

	public WRCase toEntity() {
		WRCase entity = new WRCase();
		entity = (WRCase) super.toEntity(entity);

		entity.setId(id);
		entity.setFullname(fullname);
		entity.setDob(dob);
		entity.setGender(gender);
		entity.setNationalId(nationalId);
		entity.setHivConfirmId(hivConfirmId);
		entity.setScreeningDate(screeningDate);
		entity.setScreeningSite(screeningSite);
		entity.setConfirmDate(confirmDate);
		entity.setEnrollmentDate(enrollmentDate);
		entity.setArvInitiationDate(arvInitiationDate);
		entity.setPatientChartId(patientChartId);
		entity.setHtsCaseStatus(htsCaseStatus);
		entity.setTxCaseStatus(txCaseStatus);
		entity.setNote(note);

		entity.setRtriPos(rtriPos);
		entity.setOfferedPns(offeredPns);

		entity.setLinked2OpcAssist(linked2OpcAssist);

		if (vct != null) {
			entity.setVct(vct.toEntity());
		}

		if (confirmLab != null) {
			entity.setConfirmLab(confirmLab.toEntity());
		}

		if (opc != null) {
			entity.setOpc(opc.toEntity());
		}

		return entity;
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

	public Set<LocationDto> getLocations() {
		if (locations == null) {
			locations = new LinkedHashSet<>();
		}

		return locations;
	}

	public void setLocations(Set<LocationDto> locations) {
		this.locations = locations;
	}

	public OrganizationDto getVct() {
		return vct;
	}

	public void setVct(OrganizationDto vct) {
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

	public HIVConfirmLabDto getConfirmLab() {
		return confirmLab;
	}

	public void setConfirmLab(HIVConfirmLabDto confirmLab) {
		this.confirmLab = confirmLab;
	}

	public LocalDateTime getEnrollmentDate() {
		return enrollmentDate;
	}

	public void setEnrollmentDate(LocalDateTime enrollmentDate) {
		this.enrollmentDate = enrollmentDate;
	}

	public OrganizationDto getOpc() {
		return opc;
	}

	public void setOpc(OrganizationDto opc) {
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

	public Set<WeeklyReportDto> getReports() {

		if (reports == null) {
			reports = new HashSet<>();
		}

		return reports;
	}

	public void setReports(Set<WeeklyReportDto> reports) {
		this.reports = reports;
	}

	public int getRtriPos() {
		return (rtriPos == null) ? 0 : rtriPos;
	}

	public void setRtriPos(Integer rtriPos) {
		this.rtriPos = rtriPos;
	}

	public int getOfferedPns() {
		return (offeredPns == null) ? 0 : offeredPns;
	}

	public void setOfferedPns(Integer offeredPns) {
		this.offeredPns = offeredPns;
	}

	public String getRtriPosLabel() {
		return rtriPosLabel;
	}

	public String getOfferedPnsLabel() {
		return offeredPnsLabel;
	}

	public Boolean getLinked2OpcAssist() {
		return linked2OpcAssist;
	}

	public void setLinked2OpcAssist(Boolean linked2OpcAssist) {
		this.linked2OpcAssist = linked2OpcAssist;
	}

	public Boolean getTransed2OpcAssist() {
		return transed2OpcAssist;
	}

	public void setTransed2OpcAssist(Boolean transed2OpcAssist) {
		this.transed2OpcAssist = transed2OpcAssist;
	}

	public String getPatientChartIdInOpcAssist() {
		return patientChartIdInOpcAssist;
	}

	public void setPatientChartIdInOpcAssist(String patientChartIdInOpcAssist) {
		this.patientChartIdInOpcAssist = patientChartIdInOpcAssist;
	}
}
