package org.pepfar.pdma.app.data.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.LongStream;

import org.pepfar.pdma.app.data.domain.Appointment;
import org.pepfar.pdma.app.data.domain.Case;
import org.pepfar.pdma.app.data.domain.CaseOrg;
import org.pepfar.pdma.app.data.domain.ClinicalStage;
import org.pepfar.pdma.app.data.domain.Dictionary;
import org.pepfar.pdma.app.data.domain.Hepatitis;
import org.pepfar.pdma.app.data.domain.LabTest;
import org.pepfar.pdma.app.data.domain.MMDispensing;
import org.pepfar.pdma.app.data.domain.Pregnancy;
import org.pepfar.pdma.app.data.domain.Recency;
import org.pepfar.pdma.app.data.domain.RiskInterview;
import org.pepfar.pdma.app.data.domain.Service;
import org.pepfar.pdma.app.data.domain.ShiInterview;
import org.pepfar.pdma.app.data.domain.TBProphylaxis;
import org.pepfar.pdma.app.data.domain.TBTreatment;
import org.pepfar.pdma.app.data.domain.Treatment;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.CustomLocalDateTimeDeserializer2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class CaseDto extends AuditableEntityDto {

	private Long id;

	private UUID uid;

	private Set<ServiceDto> services = new HashSet<>();

	private PersonDto person;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime hivScreenDate;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime hivConfirmDate;

	private String confirmLabName;

	private HIVConfirmLabDto confirmLab;

	private String hivConfirmId;

	private String hivInfoId;

	private Boolean hivInfoIdLocked;

	private String nationalHealthId;

	private String shiNumber;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime lastSyncDate;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime arvStartDate;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime secondLineStartDate;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime thirdLineStartDate;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime fourthLineStartDate;

	private RegimenDto currentArvRegimen;

	private String currentArvRegimenName;

	private Integer currentArvRegimenLine;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime currentArvRegimenStartDate;

	private Integer whoStage;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime whoStageEvalDate;

	private boolean deleted;

	private int caseStatus;

	private boolean missingInformation;

	private Set<ClinicalStageDto> whoStages = new LinkedHashSet<>();

	private Set<CaseOrgDto> caseOrgs = new LinkedHashSet<>();

	private Set<LabTestDto> labTests = new LinkedHashSet<>();

	private Set<TreatmentDto> treatments = new LinkedHashSet<>();

	private Set<RiskInterviewDto> riskInterviews = new LinkedHashSet<>();

	private Set<ShiInterviewDto> shiInterviews = new LinkedHashSet<>();

	private Set<MMDispensingDto> mmdEvals = new LinkedHashSet<>();

	private Set<PregnancyDto> pregnancies = new LinkedHashSet<>();

	private MMTDto mmt;

	private Set<HepatitisDto> hepatitises = new LinkedHashSet<>();

	private Set<RecencyDto> recencies = new LinkedHashSet<>();

	private Set<TBProphylaxisDto> tbpros = new LinkedHashSet<>();

	private Set<TBTreatmentDto> tbtxs = new LinkedHashSet<>();

	private Set<AppointmentDto> appointments = new LinkedHashSet<>();

	/**
	 * 1 = Currently on MMD; 2 = Eligible for MMD; 3 = Possibly eligible; 4 = Ever
	 * on MMD
	 */
	private int mmdStatus;

	private String note;

	public CaseDto() {
	}

	public CaseDto(Case entity) {
		super(entity);

		if (entity == null) {
			return;
		}

		this.id = entity.getId();

		this.uid = entity.getUid();
		this.hivScreenDate = entity.getHivScreenDate();
		this.hivConfirmDate = entity.getHivConfirmDate();
		this.confirmLabName = entity.getConfirmLabName();
		this.hivConfirmId = entity.getHivConfirmId();
		this.hivInfoId = entity.getHivInfoID();
		this.hivInfoIdLocked = entity.getHivInfoIdLocked();
		this.nationalHealthId = entity.getNationalHealthId();
		this.shiNumber = entity.getShiNumber();
		this.lastSyncDate = entity.getLastSyncDate();
		this.arvStartDate = entity.getArvStartDate();
		this.secondLineStartDate = entity.getSecondLineStartDate();
		this.thirdLineStartDate = entity.getThirdLineStartDate();
		this.fourthLineStartDate = entity.getFourthLineStartDate();
		this.whoStage = entity.getWhoStage();
		this.whoStageEvalDate = entity.getWhoStageEvalDate();
		this.currentArvRegimenName = entity.getCurrentArvRegimenName();
		this.currentArvRegimenLine = entity.getCurrentArvRegimenLine();
		this.currentArvRegimenStartDate = entity.getCurrentArvRegimenStartDate();
		this.secondLineStartDate = entity.getSecondLineStartDate();
		this.thirdLineStartDate = entity.getThirdLineStartDate();
		this.fourthLineStartDate = entity.getFourthLineStartDate();
		this.deleted = entity.isDeleted();
		this.note = entity.getNote();

		if (entity.getPerson() != null) {
			this.person = new PersonDto(entity.getPerson(), false);
		}

		if (entity.getConfirmLab() != null) {
			this.confirmLab = new HIVConfirmLabDto(entity.getConfirmLab());
		}

		if (entity.getCurrentArvRegimen() != null) {
			this.currentArvRegimen = new RegimenDto(entity.getCurrentArvRegimen());
		}

		// Latest case-org only
		if (entity.getCaseOrgs() != null) {
			Iterator<CaseOrg> itr = entity.getCaseOrgs().iterator();
			if (itr.hasNext()) {
				this.caseOrgs.add(new CaseOrgDto(itr.next(), false));
			}
		}
	}

	public CaseDto(Case entity, boolean collapse) {
		super(entity);

		if (entity == null) {
			return;
		}
		this.id = entity.getId();
		this.uid = entity.getUid();
		this.hivScreenDate = entity.getHivScreenDate();
		this.hivConfirmDate = entity.getHivConfirmDate();
		this.confirmLabName = entity.getConfirmLabName();
		this.hivConfirmId = entity.getHivConfirmId();
		this.hivInfoId = entity.getHivInfoID();
		this.hivInfoIdLocked = entity.getHivInfoIdLocked();
		this.nationalHealthId = entity.getNationalHealthId();
		this.shiNumber = entity.getShiNumber();
		this.lastSyncDate = entity.getLastSyncDate();
		this.arvStartDate = entity.getArvStartDate();
		this.secondLineStartDate = entity.getSecondLineStartDate();
		this.thirdLineStartDate = entity.getThirdLineStartDate();
		this.fourthLineStartDate = entity.getFourthLineStartDate();
		this.whoStage = entity.getWhoStage();
		this.whoStageEvalDate = entity.getWhoStageEvalDate();
		this.currentArvRegimenName = entity.getCurrentArvRegimenName();
		this.currentArvRegimenLine = entity.getCurrentArvRegimenLine();
		this.currentArvRegimenStartDate = entity.getCurrentArvRegimenStartDate();
		this.secondLineStartDate = entity.getSecondLineStartDate();
		this.thirdLineStartDate = entity.getThirdLineStartDate();
		this.fourthLineStartDate = entity.getFourthLineStartDate();
		this.deleted = entity.isDeleted();
		this.note = entity.getNote();

		if (!collapse) {
			if (entity.getWhoStages() != null) {
				entity.getWhoStages().parallelStream().forEachOrdered(s -> {
					ClinicalStageDto sdto = new ClinicalStageDto();
					sdto.setId(s.getId());
					sdto.setStage(s.getStage());
					sdto.setEvalDate(s.getEvalDate());

					this.whoStages.add(sdto);
				});
			}

			if (entity.getPerson() != null) {
				this.person = new PersonDto(entity.getPerson(), false);
			}

			if (entity.getConfirmLab() != null) {
				this.confirmLab = new HIVConfirmLabDto(entity.getConfirmLab());
			}

			if (entity.getCurrentArvRegimen() != null) {
				this.currentArvRegimen = new RegimenDto(entity.getCurrentArvRegimen());
			}

			if (entity.getLabTests() != null) {
				entity.getLabTests().parallelStream().forEachOrdered(s -> {
					LabTestDto ldto = new LabTestDto();
					ldto.setId(s.getId());
					ldto.setId(s.getId());
					ldto.setTestType(s.getTestType());
					ldto.setSampleDate(s.getSampleDate());
					ldto.setResultDate(s.getResultDate());
					ldto.setResultNumber(s.getResultNumber());
					ldto.setResultText(s.getResultText());

					this.labTests.add(ldto);
				});
			}

			if (!CommonUtils.isEmpty(entity.getRiskInterviews())) {
				RiskInterview e = entity.getRiskInterviews().toArray(new RiskInterview[0])[0];
				RiskInterviewDto rdto = new RiskInterviewDto();
				rdto.setId(e.getId());
				rdto.setInterviewDate(e.getInterviewDate());
				rdto.setOtherRiskGroupText(e.getOtherRiskGroupText());

				List<DictionaryDto> risks = new ArrayList<>();

				for (Dictionary d : e.getRisks()) {
					risks.add(new DictionaryDto(d));
				}

				rdto.getRisks().addAll(risks);

				this.riskInterviews.add(rdto);
			}

			if (!CommonUtils.isEmpty(entity.getShiInterviews())) {
				ShiInterview e = entity.getShiInterviews().toArray(new ShiInterview[0])[0];
				ShiInterviewDto sdto = new ShiInterviewDto();
				sdto.setId(e.getId());
				sdto.setHasShiCard(e.getHasShiCard());
				sdto.setUsedShiForArv(e.getUsedShiForArv());
				sdto.setInterviewDate(e.getInterviewDate());

				this.shiInterviews.add(sdto);
			}

			if (entity.getMmt() != null) {
				this.mmt = new MMTDto();
				this.mmt.setId(entity.getMmt().getId());
				this.mmt.setOnMMT(entity.getMmt().isOnMMT());
				this.mmt.setStoppedMMT(entity.getMmt().isStoppedMMT());
				this.mmt.setStartDate(entity.getMmt().getStartDate());
				this.mmt.setEndDate(entity.getMmt().getEndDate());
				this.mmt.setSteadyDose(entity.getMmt().getSteadyDose());
				this.mmt.setDoseBeforeStop(entity.getMmt().getDoseBeforeStop());
			}

			if (!CommonUtils.isEmpty(entity.getPregnancies())) {
				Pregnancy e = entity.getPregnancies().toArray(new Pregnancy[0])[0];
				PregnancyDto pdto = new PregnancyDto();
				pdto.setId(e.getId());
				pdto.setPregnant(e.isPregnant());
				pdto.setAttendedAnc(e.isAttendedAnc());
				pdto.setDueDate(e.getDueDate());
				pdto.setPregResult(e.getPregResult());
				pdto.setChildDob(e.getChildDob());
				pdto.setChildHIVStatus(e.getChildHIVStatus());
				pdto.setChildDiagnosedDate(e.getChildDiagnosedDate());

				this.pregnancies.add(pdto);
			}

			if (!CommonUtils.isEmpty(entity.getRecencies())) {
				Recency e = entity.getRecencies().toArray(new Recency[0])[0];
				RecencyDto rdto = new RecencyDto();
				rdto.setId(e.getId());
				rdto.setScreenSampleDate(e.getScreenSampleDate());
				rdto.setScreenTestDate(e.getScreenTestDate());
				rdto.setScreenResult(e.getScreenResult());
				rdto.setConfirmResult(e.getConfirmResult());

				this.recencies.add(rdto);
			}

			if (!CommonUtils.isEmpty(entity.getTbpros())) {
				TBProphylaxis e = entity.getTbpros().toArray(new TBProphylaxis[0])[0];
				TBProphylaxisDto tdto = new TBProphylaxisDto();
				tdto.setId(e.getId());
				tdto.setStartDate(e.getStartDate());
				tdto.setEndDate(e.getEndDate());
				tdto.setResult(e.getResult());

				this.tbpros.add(tdto);
			}

			if (!CommonUtils.isEmpty(entity.getTbtxs())) {
				TBTreatment e = entity.getTbtxs().toArray(new TBTreatment[0])[0];
				TBTreatmentDto tdto = new TBTreatmentDto();
				tdto.setId(e.getId());
				tdto.setDiagnoseDate(e.getDiagnoseDate());
				tdto.setTxStartDate(e.getTxStartDate());
				tdto.setTxEndDate(e.getTxEndDate());
				tdto.setFacilityName(e.getFacilityName());

				this.tbtxs.add(tdto);
			}

			if (!CommonUtils.isEmpty(entity.getHepatitises())) {
				List<HepatitisDto> list = new ArrayList<>();
				for (Hepatitis e : entity.getHepatitises()) {
					HepatitisDto dto = new HepatitisDto();
					dto.setId(e.getId());

					dto.setTestPositive(e.isTestPositive());
					dto.setTestType(e.getTestType());
					dto.setTestDate(e.getTestDate());

					dto.setOnTreatment(e.isOnTreatment());
					dto.setTxStartDate(e.getTxStartDate());
					dto.setTxEndDate(e.getTxEndDate());

					list.add(dto);
				}

				this.hepatitises.addAll(list);
			}

			if (!CommonUtils.isEmpty(entity.getTreatments())) {
				final List<TreatmentDto> list = new ArrayList<>();

				entity.getTreatments().forEach(e -> {
					TreatmentDto dto = new TreatmentDto();
					dto.setId(e.getId());
					dto.setStartDate(e.getStartDate());
					dto.setRegimenName(e.getRegimenName());
					dto.setRegimenLine(e.getRegimenLine());

					list.add(dto);
				});

				this.treatments.addAll(list);
			}

			if (!CommonUtils.isEmpty(entity.getMmdEvals())) {
				final List<MMDispensingDto> list = new ArrayList<>();
				Iterator<MMDispensing> itr = entity.getMmdEvals().iterator();

				boolean latest = true;

				while (itr.hasNext()) {
					MMDispensing e = itr.next();
					if (CommonUtils.isTrue(e.getDeleted())) {
						continue;
					}

					MMDispensingDto dto = new MMDispensingDto();

					if (latest) {
						latest = false;
						dto.setLatest(true);
					}

					dto.setId(e.getId());
					dto.setEvaluationDate(e.getEvaluationDate());
					dto.setAdult(e.isAdult());
					dto.setArvGt12Month(e.isArvGt12Month());
					dto.setVlLt200(e.isVlLt200());
					dto.setNoOIs(e.isNoOIs());
					dto.setNoDrugAdvEvent(e.isNoDrugAdvEvent());
					dto.setNoPregnancy(e.isNoPregnancy());
					dto.setGoodAdherence(e.isGoodAdherence());
					dto.setEligible(e.isEligible());
					dto.setOnMmd(e.isOnMmd());
//					dto.setStartDate(e.getStartDate());
//					dto.setEndDate(e.getEndDate());
					dto.setStopReason(e.getStopReason());
					dto.setDeleted(e.getDeleted());

					// the case
					CaseDto cdto = new CaseDto();
					cdto.setId(e.getTheCase().getId());
					dto.setTheCase(cdto);

					list.add(dto);
				}

				this.mmdEvals.clear();
				this.mmdEvals.addAll(list);
			}
		}
	}

	public CaseDto(Case entity, List<Long> orgIds) {
		super(entity);

		if (entity == null) {
			return;
		}

		this.id = entity.getId();

		// only set ID if there is no granted organization data
		if (orgIds == null || CommonUtils.isEmpty(orgIds)) {
			return;
		}

		this.uid = entity.getUid();
		this.hivScreenDate = entity.getHivScreenDate();
		this.hivConfirmDate = entity.getHivConfirmDate();
		this.confirmLabName = entity.getConfirmLabName();
		this.hivConfirmId = entity.getHivConfirmId();
		this.hivInfoId = entity.getHivInfoID();
		this.hivInfoIdLocked = entity.getHivInfoIdLocked();
		this.nationalHealthId = entity.getNationalHealthId();
		this.shiNumber = entity.getShiNumber();
		this.lastSyncDate = entity.getLastSyncDate();
		this.arvStartDate = entity.getArvStartDate();
		this.secondLineStartDate = entity.getSecondLineStartDate();
		this.thirdLineStartDate = entity.getThirdLineStartDate();
		this.fourthLineStartDate = entity.getFourthLineStartDate();
		this.whoStage = entity.getWhoStage();
		this.whoStageEvalDate = entity.getWhoStageEvalDate();
		this.currentArvRegimenName = entity.getCurrentArvRegimenName();
		this.currentArvRegimenLine = entity.getCurrentArvRegimenLine();
		this.currentArvRegimenStartDate = entity.getCurrentArvRegimenStartDate();
		this.secondLineStartDate = entity.getSecondLineStartDate();
		this.thirdLineStartDate = entity.getThirdLineStartDate();
		this.fourthLineStartDate = entity.getFourthLineStartDate();
		this.deleted = entity.isDeleted();
		this.note = entity.getNote();

		// latest appointment
		Iterator<Appointment> apps = entity.getAppointments().iterator();
		LocalDateTime todayStart = CommonUtils.hanoiTodayStart();
		while (apps.hasNext()) {
			Appointment app = apps.next();
			LocalDateTime appDate = app.getAppointmentDate();

			if (todayStart.isEqual(appDate) || todayStart.isAfter(appDate)) {
				AppointmentDto appDto = new AppointmentDto();
				appDto.setId(app.getId());
				appDto.setAppointmentDate(appDate);

				this.getAppointments().clear();
				this.getAppointments().add(appDto);
				break;
			}
		}

		if (entity.getWhoStages() != null) {
			entity.getWhoStages().parallelStream().forEachOrdered(s -> {
				ClinicalStageDto sdto = new ClinicalStageDto();
				sdto.setId(s.getId());
				sdto.setStage(s.getStage());
				sdto.setEvalDate(s.getEvalDate());

				this.whoStages.add(sdto);
			});
		}

		if (entity.getPerson() != null) {
			this.person = new PersonDto(entity.getPerson(), false);
		}

		if (entity.getConfirmLab() != null) {
			this.confirmLab = new HIVConfirmLabDto(entity.getConfirmLab());
		}

		if (entity.getCurrentArvRegimen() != null) {
			this.currentArvRegimen = new RegimenDto(entity.getCurrentArvRegimen());
		}

		if (entity.getCaseOrgs() != null) {
			long[] ids = orgIds.stream().mapToLong(i -> i.longValue()).toArray();
			entity.getCaseOrgs().parallelStream()
					.filter(co -> LongStream.of(ids).anyMatch(x -> x == co.getOrganization().getId()))
					.filter(co -> CommonUtils.isTrue(co.isLatestRelationship())).forEachOrdered(s -> {
						this.caseOrgs.add(new CaseOrgDto(s, false));
					});
		}

		if (entity.getLabTests() != null) {
			entity.getLabTests().parallelStream().forEachOrdered(s -> {
				LabTestDto ldto = new LabTestDto();
				ldto.setId(s.getId());
				ldto.setId(s.getId());
				ldto.setTestType(s.getTestType());
				ldto.setSampleDate(s.getSampleDate());
				ldto.setResultDate(s.getResultDate());
				ldto.setResultNumber(s.getResultNumber());
				ldto.setResultText(s.getResultText());

				this.labTests.add(ldto);
			});
		}

		if (!CommonUtils.isEmpty(entity.getRiskInterviews())) {
			RiskInterview e = entity.getRiskInterviews().toArray(new RiskInterview[0])[0];
			RiskInterviewDto rdto = new RiskInterviewDto();
			rdto.setId(e.getId());
			rdto.setInterviewDate(e.getInterviewDate());
			rdto.setOtherRiskGroupText(e.getOtherRiskGroupText());

			List<DictionaryDto> risks = new ArrayList<>();

			for (Dictionary d : e.getRisks()) {
				risks.add(new DictionaryDto(d));
			}

			rdto.getRisks().addAll(risks);

			this.riskInterviews.add(rdto);
		}

		if (!CommonUtils.isEmpty(entity.getShiInterviews())) {
			ShiInterview e = entity.getShiInterviews().toArray(new ShiInterview[0])[0];
			ShiInterviewDto sdto = new ShiInterviewDto();
			sdto.setId(e.getId());
			sdto.setHasShiCard(e.getHasShiCard());
			sdto.setUsedShiForArv(e.getUsedShiForArv());
			sdto.setInterviewDate(e.getInterviewDate());

			this.shiInterviews.add(sdto);
		}

		if (entity.getMmt() != null) {
			this.mmt = new MMTDto();
			this.mmt.setId(entity.getMmt().getId());
			this.mmt.setOnMMT(entity.getMmt().isOnMMT());
			this.mmt.setStoppedMMT(entity.getMmt().isStoppedMMT());
			this.mmt.setStartDate(entity.getMmt().getStartDate());
			this.mmt.setEndDate(entity.getMmt().getEndDate());
			this.mmt.setSteadyDose(entity.getMmt().getSteadyDose());
			this.mmt.setDoseBeforeStop(entity.getMmt().getDoseBeforeStop());
		}

		if (!CommonUtils.isEmpty(entity.getPregnancies())) {
			Pregnancy e = entity.getPregnancies().toArray(new Pregnancy[0])[0];
			PregnancyDto pdto = new PregnancyDto();
			pdto.setId(e.getId());
			pdto.setPregnant(e.isPregnant());
			pdto.setAttendedAnc(e.isAttendedAnc());
			pdto.setDueDate(e.getDueDate());
			pdto.setPregResult(e.getPregResult());
			pdto.setChildDob(e.getChildDob());
			pdto.setChildHIVStatus(e.getChildHIVStatus());
			pdto.setChildDiagnosedDate(e.getChildDiagnosedDate());

			this.pregnancies.add(pdto);
		}

		if (!CommonUtils.isEmpty(entity.getRecencies())) {
			Recency e = entity.getRecencies().toArray(new Recency[0])[0];
			RecencyDto rdto = new RecencyDto();
			rdto.setId(e.getId());
			rdto.setScreenSampleDate(e.getScreenSampleDate());
			rdto.setScreenTestDate(e.getScreenTestDate());
			rdto.setScreenResult(e.getScreenResult());
			rdto.setConfirmResult(e.getConfirmResult());

			this.recencies.add(rdto);
		}

		if (!CommonUtils.isEmpty(entity.getTbpros())) {
			TBProphylaxis e = entity.getTbpros().toArray(new TBProphylaxis[0])[0];
			TBProphylaxisDto tdto = new TBProphylaxisDto();
			tdto.setId(e.getId());
			tdto.setStartDate(e.getStartDate());
			tdto.setEndDate(e.getEndDate());
			tdto.setResult(e.getResult());

			this.tbpros.add(tdto);
		}

		if (!CommonUtils.isEmpty(entity.getTbtxs())) {
			TBTreatment e = entity.getTbtxs().toArray(new TBTreatment[0])[0];
			TBTreatmentDto tdto = new TBTreatmentDto();
			tdto.setId(e.getId());
			tdto.setDiagnoseDate(e.getDiagnoseDate());
			tdto.setTxStartDate(e.getTxStartDate());
			tdto.setTxEndDate(e.getTxEndDate());
			tdto.setFacilityName(e.getFacilityName());

			this.tbtxs.add(tdto);
		}

		if (!CommonUtils.isEmpty(entity.getHepatitises())) {
			List<HepatitisDto> list = new ArrayList<>();
			for (Hepatitis e : entity.getHepatitises()) {
				HepatitisDto dto = new HepatitisDto();
				dto.setId(e.getId());

				dto.setTestPositive(e.isTestPositive());
				dto.setTestType(e.getTestType());
				dto.setTestDate(e.getTestDate());

				dto.setOnTreatment(e.isOnTreatment());
				dto.setTxStartDate(e.getTxStartDate());
				dto.setTxEndDate(e.getTxEndDate());

				list.add(dto);
			}

			this.hepatitises.addAll(list);
		}

		if (!CommonUtils.isEmpty(entity.getTreatments())) {
			final List<TreatmentDto> list = new ArrayList<>();

			entity.getTreatments().forEach(e -> {
				TreatmentDto dto = new TreatmentDto();
				dto.setId(e.getId());
				dto.setStartDate(e.getStartDate());
				dto.setRegimenName(e.getRegimenName());
				dto.setRegimenLine(e.getRegimenLine());

				list.add(dto);
			});

			this.treatments.addAll(list);
		}

		if (!CommonUtils.isEmpty(entity.getMmdEvals())) {
			final List<MMDispensingDto> list = new ArrayList<>();
			Iterator<MMDispensing> itr = entity.getMmdEvals().iterator();

			boolean latest = true;

			while (itr.hasNext()) {
				MMDispensing e = itr.next();
				if (CommonUtils.isTrue(e.getDeleted())) {
					continue;
				}

				MMDispensingDto dto = new MMDispensingDto();

				if (latest) {
					latest = false;
					dto.setLatest(true);
				}

				dto.setId(e.getId());
				dto.setEvaluationDate(e.getEvaluationDate());
				dto.setAdult(e.isAdult());
				dto.setArvGt12Month(e.isArvGt12Month());
				dto.setVlLt200(e.isVlLt200());
				dto.setNoOIs(e.isNoOIs());
				dto.setNoDrugAdvEvent(e.isNoDrugAdvEvent());
				dto.setNoPregnancy(e.isNoPregnancy());
				dto.setGoodAdherence(e.isGoodAdherence());
				dto.setEligible(e.isEligible());
				dto.setOnMmd(e.isOnMmd());
//				dto.setStartDate(e.getStartDate());
//				dto.setEndDate(e.getEndDate());
				dto.setStopReason(e.getStopReason());
				dto.setDeleted(e.getDeleted());

				// the case
				CaseDto cdto = new CaseDto();
				cdto.setId(e.getTheCase().getId());
				dto.setTheCase(cdto);

				list.add(dto);
			}

			this.mmdEvals.clear();
			this.mmdEvals.addAll(list);
		}
	}

	public Case toEntity() {
		Case entity = new Case();
		entity = (Case) super.toEntity(entity);

		entity.setId(id);
		entity.setUid(uid);
//		entity.setPatientChartId(patientChartId);
		entity.setHivScreenDate(hivScreenDate);
		entity.setHivConfirmDate(hivConfirmDate);
		entity.setConfirmLabName(confirmLabName);
		entity.setHivConfirmId(hivConfirmId);
		entity.setHivInfoID(hivInfoId);
		entity.setHivInfoIdLocked(hivInfoIdLocked);
		entity.setNationalHealthId(nationalHealthId);
		entity.setShiNumber(shiNumber);
		entity.setLastSyncDate(lastSyncDate);
//		entity.setEnrollmentDate(enrollmentDate);
		entity.setArvStartDate(arvStartDate);
//		entity.setArvStartDateAtCurrentOPC(arvStartDateAtCurrentOPC);
		entity.setSecondLineStartDate(secondLineStartDate);
		entity.setThirdLineStartDate(thirdLineStartDate);
		entity.setFourthLineStartDate(fourthLineStartDate);
		entity.setWhoStage(whoStage);
		entity.setWhoStageEvalDate(whoStageEvalDate);
		entity.setCurrentArvRegimenName(currentArvRegimenName);
		entity.setCurrentArvRegimenLine(currentArvRegimenLine);
		entity.setCurrentArvRegimenStartDate(currentArvRegimenStartDate);
//		entity.setArvCohort(arvCohort);
		entity.setDeleted(deleted);
		entity.setNote(note);

		if (whoStages != null) {
			Set<ClinicalStage> entities = new LinkedHashSet<>();

			whoStages.parallelStream().forEachOrdered(dto -> {
				entities.add(dto.toEntity());
			});

			entity.getWhoStages().addAll(entities);
		}

		if (confirmLab != null) {
			entity.setConfirmLab(confirmLab.toEntity());
		}

		if (person != null) {
			entity.setPerson(person.toEntity());
		}

		if (services != null) {
			Set<Service> entities = new HashSet<>();

			services.parallelStream().forEachOrdered(dto -> {
				entities.add(dto.toEntity());
			});

			entity.getServices().addAll(entities);
		}

		if (currentArvRegimen != null) {
			entity.setCurrentArvRegimen(currentArvRegimen.toEntity());
		}

		if (caseOrgs != null) {
			Set<CaseOrg> entities = new HashSet<>();

			caseOrgs.parallelStream().forEachOrdered(dto -> {
				entities.add(dto.toEntity());
			});

			entity.getCaseOrgs().clear();
			entity.getCaseOrgs().addAll(entities);
		}

		if (labTests != null) {
			Set<LabTest> entities = new LinkedHashSet<>();

			labTests.parallelStream().forEachOrdered(dto -> {
				entities.add(dto.toEntity());
			});

			entity.getLabTests().clear();
			entity.getLabTests().addAll(entities);
		}

		if (treatments != null) {
			Set<Treatment> entities = new HashSet<>();

			treatments.parallelStream().forEachOrdered(dto -> {
				entities.add(dto.toEntity());
			});

			entity.getTreatments().clear();
			entity.getTreatments().addAll(entities);
		}

		if (riskInterviews != null) {
			Set<RiskInterview> entities = new HashSet<>();

			riskInterviews.parallelStream().forEachOrdered(dto -> {
				entities.add(dto.toEntity());
			});

			entity.getRiskInterviews().clear();
			entity.getRiskInterviews().addAll(entities);
		}

		if (shiInterviews != null) {
			Set<ShiInterview> entities = new HashSet<>();

			shiInterviews.parallelStream().forEachOrdered(dto -> {
				entities.add(dto.toEntity());
			});

			entity.getShiInterviews().clear();
			entity.getShiInterviews().addAll(entities);
		}

		if (mmdEvals != null) {
			Set<MMDispensing> entities = new HashSet<>();

			mmdEvals.parallelStream().forEachOrdered(dto -> {
				entities.add(dto.toEntity());
			});

			entity.getMmdEvals().clear();
			entity.getMmdEvals().addAll(entities);
		}

		return entity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public UUID getUid() {
		return uid;
	}

	public void setUid(UUID uid) {
		this.uid = uid;
	}

//	public String getPatientChartId() {
//		return patientChartId;
//	}
//
//	public void setPatientChartId(String patientChartId) {
//		this.patientChartId = patientChartId;
//	}

	public Set<ServiceDto> getServices() {

		if (services == null) {
			services = new HashSet<>();
		}

		return services;
	}

	public void setServices(Set<ServiceDto> services) {
		this.services = services;
	}

	public PersonDto getPerson() {
		return person;
	}

	public void setPerson(PersonDto person) {
		this.person = person;
	}

	public LocalDateTime getHivScreenDate() {
		return hivScreenDate;
	}

	public void setHivScreenDate(LocalDateTime hivScreenDate) {
		this.hivScreenDate = hivScreenDate;
	}

	public LocalDateTime getHivConfirmDate() {
		return hivConfirmDate;
	}

	public void setHivConfirmDate(LocalDateTime hivConfirmDate) {
		this.hivConfirmDate = hivConfirmDate;
	}

	public String getHivConfirmId() {
		return hivConfirmId;
	}

	public void setHivConfirmId(String hivConfirmId) {
		this.hivConfirmId = hivConfirmId;
	}

	public String getHivInfoId() {
		return hivInfoId;
	}

	public void setHivInfoId(String hivInfoId) {
		this.hivInfoId = hivInfoId;
	}

	public Boolean getHivInfoIdLocked() {
		return hivInfoIdLocked;
	}

	public void setHivInfoIdLocked(Boolean hivInfoIdLocked) {
		this.hivInfoIdLocked = hivInfoIdLocked;
	}

	public String getNationalHealthId() {
		return nationalHealthId;
	}

	public void setNationalHealthId(String nationalHealthId) {
		this.nationalHealthId = nationalHealthId;
	}

	public String getShiNumber() {
		return shiNumber;
	}

	public void setShiNumber(String shiNumber) {
		this.shiNumber = shiNumber;
	}

	public LocalDateTime getLastSyncDate() {
		return lastSyncDate;
	}

	public void setLastSyncDate(LocalDateTime lastSyncDate) {
		this.lastSyncDate = lastSyncDate;
	}

	public String getCurrentArvRegimenName() {
		return currentArvRegimenName;
	}

	public void setCurrentArvRegimenName(String currentArvRegimenName) {
		this.currentArvRegimenName = currentArvRegimenName;
	}

	public Integer getCurrentArvRegimenLine() {
		return currentArvRegimenLine;
	}

	public void setCurrentArvRegimenLine(Integer currentArvRegimenLine) {
		this.currentArvRegimenLine = currentArvRegimenLine;
	}

	public LocalDateTime getCurrentArvRegimenStartDate() {
		return currentArvRegimenStartDate;
	}

	public void setCurrentArvRegimenStartDate(LocalDateTime currentArvRegimenStartDate) {
		this.currentArvRegimenStartDate = currentArvRegimenStartDate;
	}

	public LocalDateTime getArvStartDate() {
		return arvStartDate;
	}

	public void setArvStartDate(LocalDateTime arvStartDate) {
		this.arvStartDate = arvStartDate;
	}

	public String getConfirmLabName() {
		return confirmLabName;
	}

	public void setConfirmLabName(String confirmLabName) {
		this.confirmLabName = confirmLabName;
	}

	public HIVConfirmLabDto getConfirmLab() {
		return confirmLab;
	}

	public void setConfirmLab(HIVConfirmLabDto confirmLab) {
		this.confirmLab = confirmLab;
	}

//	public LocalDateTime getEnrollmentDate() {
//		return enrollmentDate;
//	}
//
//	public void setEnrollmentDate(LocalDateTime enrollmentDate) {
//		this.enrollmentDate = enrollmentDate;
//	}
//
//	public LocalDateTime getArvStartDateAtCurrentOPC() {
//		return arvStartDateAtCurrentOPC;
//	}
//
//	public void setArvStartDateAtCurrentOPC(LocalDateTime arvStartDateAtCurrentOPC) {
//		this.arvStartDateAtCurrentOPC = arvStartDateAtCurrentOPC;
//	}

	public LocalDateTime getSecondLineStartDate() {
		return secondLineStartDate;
	}

	public void setSecondLineStartDate(LocalDateTime secondLineStartDate) {
		this.secondLineStartDate = secondLineStartDate;
	}

	public LocalDateTime getThirdLineStartDate() {
		return thirdLineStartDate;
	}

	public void setThirdLineStartDate(LocalDateTime thirdLineStartDate) {
		this.thirdLineStartDate = thirdLineStartDate;
	}

	public LocalDateTime getFourthLineStartDate() {
		return fourthLineStartDate;
	}

	public void setFourthLineStartDate(LocalDateTime fourthLineStartDate) {
		this.fourthLineStartDate = fourthLineStartDate;
	}

//	public String getArvCohort() {
//		return arvCohort;
//	}
//
//	public void setArvCohort(String arvCohort) {
//		this.arvCohort = arvCohort;
//	}

	public RegimenDto getCurrentArvRegimen() {
		return currentArvRegimen;
	}

	public void setCurrentArvRegimen(RegimenDto currentArvRegimen) {
		this.currentArvRegimen = currentArvRegimen;
	}

	public Integer getWhoStage() {
		return whoStage;
	}

	public void setWhoStage(Integer whoStage) {
		this.whoStage = whoStage;
	}

	public LocalDateTime getWhoStageEvalDate() {
		return whoStageEvalDate;
	}

	public void setWhoStageEvalDate(LocalDateTime whoStageEvalDate) {
		this.whoStageEvalDate = whoStageEvalDate;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public int getCaseStatus() {
		return caseStatus;
	}

	public void setCaseStatus(int caseStatus) {
		this.caseStatus = caseStatus;
	}

	public boolean isMissingInformation() {
		return missingInformation;
	}

	public Set<ClinicalStageDto> getWhoStages() {
		return whoStages;
	}

	public void setWhoStages(Set<ClinicalStageDto> whoStages) {
		this.whoStages = whoStages;
	}

	public Set<CaseOrgDto> getCaseOrgs() {
		if (caseOrgs == null) {
			caseOrgs = new LinkedHashSet<>();
		}

		return caseOrgs;
	}

	public void setCaseOrgs(Set<CaseOrgDto> caseOrgs) {
		this.caseOrgs = caseOrgs;
	}

	public Set<LabTestDto> getLabTests() {

		if (labTests == null) {
			labTests = new LinkedHashSet<>();
		}

		return labTests;
	}

	public void setLabTests(Set<LabTestDto> labTests) {
		this.labTests = labTests;
	}

	public Set<TreatmentDto> getTreatments() {

		if (treatments == null) {
			treatments = new LinkedHashSet<>();
		}

		return treatments;
	}

	public void setTreatments(Set<TreatmentDto> treatments) {
		this.treatments = treatments;
	}

	public Set<RiskInterviewDto> getRiskInterviews() {

		if (riskInterviews == null) {
			riskInterviews = new LinkedHashSet<>();
		}

		return riskInterviews;
	}

	public void setRiskInterviews(Set<RiskInterviewDto> riskInterviews) {
		this.riskInterviews = riskInterviews;
	}

	public Set<ShiInterviewDto> getShiInterviews() {

		if (shiInterviews == null) {
			shiInterviews = new LinkedHashSet<>();
		}

		return shiInterviews;
	}

	public void setShiInterviews(Set<ShiInterviewDto> shiInterviews) {
		this.shiInterviews = shiInterviews;
	}

	public Set<MMDispensingDto> getMmdEvals() {

		if (mmdEvals == null) {
			mmdEvals = new LinkedHashSet<>();
		}

		return mmdEvals;
	}

	public void setMmdEvals(Set<MMDispensingDto> mmdEvals) {
		this.mmdEvals = mmdEvals;
	}

	public Set<PregnancyDto> getPregnancies() {

		if (pregnancies == null) {
			pregnancies = new LinkedHashSet<>();
		}

		return pregnancies;
	}

	public void setPregnancies(Set<PregnancyDto> pregnancies) {
		this.pregnancies = pregnancies;
	}

	public MMTDto getMmt() {
		return mmt;
	}

	public void setMmt(MMTDto mmt) {
		this.mmt = mmt;
	}

	public Set<RecencyDto> getRecencies() {

		if (recencies == null) {
			recencies = new LinkedHashSet<>();
		}

		return recencies;
	}

	public void setRecencies(Set<RecencyDto> recencies) {
		this.recencies = recencies;
	}

	public Set<TBProphylaxisDto> getTbpros() {

		if (tbpros == null) {
			tbpros = new LinkedHashSet<>();
		}

		return tbpros;
	}

	public void setTbpros(Set<TBProphylaxisDto> tbpros) {
		this.tbpros = tbpros;
	}

	public Set<TBTreatmentDto> getTbtxs() {

		if (tbtxs == null) {
			tbtxs = new LinkedHashSet<>();
		}

		return tbtxs;
	}

	public void setTbtxs(Set<TBTreatmentDto> tbtxs) {
		this.tbtxs = tbtxs;
	}

	public Set<HepatitisDto> getHepatitises() {

		if (hepatitises == null) {
			hepatitises = new LinkedHashSet<>();
		}

		return hepatitises;
	}

	public void setHepatitises(Set<HepatitisDto> hepatitises) {
		this.hepatitises = hepatitises;
	}

	public int getMmdStatus() {
		return mmdStatus;
	}

	public void setMmdStatus(int mmdStatus) {
		this.mmdStatus = mmdStatus;
	}

	public Set<AppointmentDto> getAppointments() {
		return appointments;
	}

	public void setAppointments(Set<AppointmentDto> appointments) {
		this.appointments = appointments;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

}
