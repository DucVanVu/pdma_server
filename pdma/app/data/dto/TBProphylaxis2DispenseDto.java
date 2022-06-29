package org.pepfar.pdma.app.data.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import org.pepfar.pdma.app.data.domain.TBProphylaxis2Dispense;
import org.pepfar.pdma.app.data.types.TBProphylaxisResumeReason;
import org.pepfar.pdma.app.data.types.TBProphylaxisStopReason;
import org.pepfar.pdma.app.utils.CustomLocalDateTimeDeserializer2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class TBProphylaxis2DispenseDto extends AuditableEntityDto {
	private Long id;

	private UUID uid;

	private TBProphylaxis2Dto round;
	
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime recordDate;

	private boolean dispensed;

	private TBProphylaxisStopReason stopReason;

	private Integer dispensedDoses;

	private TBProphylaxisResumeReason resumeReason;

	private String note;
	private String code;

	public TBProphylaxis2DispenseDto() {
		
	}
	
	public TBProphylaxis2DispenseDto(TBProphylaxis2Dispense entity,boolean collapse) {		
		super(entity);
		if(entity!=null) {			
			this.id = entity.getId();
			this.uid = entity.getUid();
			this.recordDate = entity.getRecordDate();
			this.dispensed = entity.isDispensed();
			this.stopReason = entity.getStopReason();
			this.dispensedDoses = entity.getDispensedDoses();
			this.resumeReason = entity.getResumeReason();
			this.note = entity.getNote();
			if(!collapse) {				
				if(entity.getRound()!=null) {
					this.round = new TBProphylaxis2Dto(entity.getRound(),true);	
				}			
			}
			else {	
				if(entity.getRound()!=null) {
					this.round = new TBProphylaxis2Dto();
					this.round.setId(entity.getRound().getId());
					this.round.setStartDate(entity.getRound().getStartDate());
					this.round.setEndDate(entity.getRound().getEndDate());
					this.round.setRegimen(entity.getRound().getRegimen());
				}
			}
		}
			
	}
	
	public TBProphylaxis2Dispense toEntity() {
		TBProphylaxis2Dispense entity = new TBProphylaxis2Dispense();
		entity = (TBProphylaxis2Dispense) super.toEntity(entity);
		entity.setId(this.id);
		entity.setUid(this.uid);
		entity.setRecordDate(this.recordDate);
		entity.setDispensed(this.dispensed);
		entity.setStopReason(this.stopReason);
		entity.setDispensedDoses(this.dispensedDoses);
		entity.setResumeReason(this.resumeReason);
		entity.setNote(this.note);
		if(this.round!=null)
			entity.setRound(this.round.toEntity());
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

	public TBProphylaxis2Dto getRound() {
		return round;
	}

	public void setRound(TBProphylaxis2Dto round) {
		this.round = round;
	}

	public LocalDateTime getRecordDate() {
		return recordDate;
	}

	public void setRecordDate(LocalDateTime recordDate) {
		this.recordDate = recordDate;
	}

	public boolean isDispensed() {
		return dispensed;
	}

	public void setDispensed(boolean dispensed) {
		this.dispensed = dispensed;
	}

	public TBProphylaxisStopReason getStopReason() {
		return stopReason;
	}

	public void setStopReason(TBProphylaxisStopReason stopReason) {
		this.stopReason = stopReason;
	}

	public Integer getDispensedDoses() {
		return dispensedDoses;
	}

	public void setDispensedDoses(Integer dispensedDoses) {
		this.dispensedDoses = dispensedDoses;
	}

	public TBProphylaxisResumeReason getResumeReason() {
		return resumeReason;
	}

	public void setResumeReason(TBProphylaxisResumeReason resumeReason) {
		this.resumeReason = resumeReason;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	
}
