package org.pepfar.pdma.app.data.dto;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import org.pepfar.pdma.app.data.domain.TBProphylaxis2;
import org.pepfar.pdma.app.data.domain.TBProphylaxis2Dispense;
import org.pepfar.pdma.app.data.types.TBProphylaxisRegimen;
import org.pepfar.pdma.app.utils.CustomLocalDateTimeDeserializer2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class TBProphylaxis2Dto extends AuditableEntityDto {
	private Long id;

	private UUID uid;

	private OrganizationDto organization;

	private CaseDto theCase;

	private TBProphylaxisRegimen regimen;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime startDate;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime endDate;

	private Set<TBProphylaxis2DispenseDto> dispenses = new LinkedHashSet<>();
	private String note;//ghi chú nếu trường hợp phác đồ khác
	private Boolean complete;//hoàn thành điều trị
	private Boolean dispensed;//đang điều trị hay ngưng điều trị
	private int status;//trạng thái =0 Chưa bắt đầu (khi chưa cấp thuốc);=1 Đang điều trị; =2 :Ngưng điều trị; =3: Hoàn thành điều trị; =4 Bỏ điều trị
	
	public TBProphylaxis2Dto() {
		super();
	}
	
	public TBProphylaxis2Dto(TBProphylaxis2 entity,boolean collapse) {
		super(entity);
		this.id = entity.getId();
		this.uid = entity.getUid();
		this.startDate = entity.getStartDate();
		this.endDate = entity.getEndDate();
		this.regimen = entity.getRegimen();
		if(entity.getComplete()!=null)
		this.complete=entity.getComplete();
		this.note=entity.getNote();
		this.status = entity.getStatus();
		if(!collapse) {
			if(entity.getOrganization()!=null) {
				this.organization = new OrganizationDto(entity.getOrganization(),true);
			}
			if(entity.getTheCase()!=null) {
				this.theCase = new CaseDto(entity.getTheCase(),true);
			}
			if(entity.getDispenses()!=null&& entity.getDispenses().size()>0) {
				for (TBProphylaxis2Dispense tbProphylaxis2Dispense : entity.getDispenses()) {
					TBProphylaxis2DispenseDto dispense = new TBProphylaxis2DispenseDto(tbProphylaxis2Dispense,true);
					dispenses.add(dispense);
				}
			}
		}
		else {
			if(entity.getOrganization()!=null) {
				this.organization = new OrganizationDto();
				this.organization.setId(entity.getOrganization().getId());
				this.organization.setCode(entity.getOrganization().getCode());
				this.organization.setName(entity.getOrganization().getName());
			}
			if(entity.getTheCase()!=null) {
				this.theCase = new CaseDto();
				this.theCase.setId(entity.getTheCase().getId());
				this.theCase.setUid(entity.getTheCase().getUid());
				if(entity.getTheCase().getPerson()!=null) {
					this.theCase.setPerson(new PersonDto());
					this.theCase.getPerson().setId(entity.getTheCase().getPerson().getId());
					this.theCase.getPerson().setNidNumber(entity.getTheCase().getPerson().getNidNumber());
					this.theCase.getPerson().setFullname(entity.getTheCase().getPerson().getFullname());
				}
			}
			if(entity.getDispenses()!=null&& entity.getDispenses().size()>0) {
				LocalDateTime date=entity.getDispenses().iterator().next().getRecordDate();
				this.dispensed=entity.getDispenses().iterator().next().isDispensed();
				for (TBProphylaxis2Dispense tbProphylaxis2Dispense : entity.getDispenses()) {
					if(date.isBefore(tbProphylaxis2Dispense.getRecordDate())) {
						date=tbProphylaxis2Dispense.getRecordDate();
						this.dispensed=tbProphylaxis2Dispense.isDispensed();
					}				
				}
			}
		}
	}

	public TBProphylaxis2 toEntity() {
		TBProphylaxis2 entity = new TBProphylaxis2();
		entity = (TBProphylaxis2)super.toEntity(entity);
		entity.setId(this.id);
		entity.setUid(this.uid);
		entity.setStartDate(this.startDate);
		entity.setEndDate(this.endDate);
		entity.setRegimen(this.regimen);
		if(this.organization!=null)
			entity.setOrganization(this.organization.toEntity());
		if(this.theCase!=null)
			entity.setTheCase(this.theCase.toEntity());
		entity.setNote(this.note);
		entity.setComplete(this.complete);
		entity.setStatus(this.status);
		entity.setDispenses(new HashSet<TBProphylaxis2Dispense>());
		if(this.dispenses!=null && this.dispenses.size()>0) {
			Set<TBProphylaxis2Dispense> entities = new HashSet<TBProphylaxis2Dispense>();
			this.dispenses.parallelStream().forEachOrdered(dto -> {
				entities.add(dto.toEntity());				
			});
			entity.getDispenses().addAll(entities);
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

	public OrganizationDto getOrganization() {
		return organization;
	}

	public void setOrganization(OrganizationDto organization) {
		this.organization = organization;
	}

	public CaseDto getTheCase() {
		return theCase;
	}

	public void setTheCase(CaseDto theCase) {
		this.theCase = theCase;
	}

	public TBProphylaxisRegimen getRegimen() {
		return regimen;
	}

	public void setRegimen(TBProphylaxisRegimen regimen) {
		this.regimen = regimen;
	}

	public LocalDateTime getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDateTime startDate) {
		this.startDate = startDate;
	}

	public LocalDateTime getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDateTime endDate) {
		this.endDate = endDate;
	}

	public Set<TBProphylaxis2DispenseDto> getDispenses() {
		return dispenses;
	}

	public void setDispenses(Set<TBProphylaxis2DispenseDto> dispenses) {
		this.dispenses = dispenses;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public Boolean getComplete() {
		return complete;
	}

	public void setComplete(Boolean complete) {
		this.complete = complete;
	}

	public Boolean getDispensed() {
		return dispensed;
	}

	public void setDispensed(Boolean dispensed) {
		this.dispensed = dispensed;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
}
