package org.pepfar.pdma.app.data.dto;

import org.pepfar.pdma.app.data.domain.CoviVac;

import java.time.LocalDateTime;
import java.util.UUID;

public class CoviVacDto extends AuditableEntityDto {

    private Long id;

    private UUID uid;

    private CaseDto theCase;

    private OrganizationDto organization;

    private LocalDateTime vaccinationDate;

    private String vaccineName;

    private String vaccineLotNumber;

    private LocalDateTime expireDate;

    private String note;

    public CoviVacDto() {

    }

    public CoviVacDto(CoviVac entity) {
        super(entity);

        if (entity == null) {
            return;
        }

        id = entity.getId();
        uid = entity.getUid();
        vaccinationDate = entity.getVaccinationDate();
        vaccineName = entity.getVaccineName();
        vaccineLotNumber = entity.getVaccineLotNumber();
        expireDate = entity.getExpireDate();
        note = entity.getNote();

        if (entity.getOrganization() != null) {
            this.organization = new OrganizationDto();
            this.organization.setId(entity.getOrganization().getId());
            this.organization.setName(entity.getOrganization().getName());
        }

        if (entity.getTheCase() != null) {
            this.theCase = new CaseDto();
            this.theCase.setId(entity.getTheCase().getId());
        }
    }

    public CoviVac toEntity() {
        CoviVac entity = new CoviVac();
        entity = (CoviVac) super.toEntity(entity);

        entity.setId(id);
        entity.setUid(uid);
        entity.setVaccinationDate(vaccinationDate);
        entity.setVaccineName(vaccineName);
        entity.setVaccineLotNumber(vaccineLotNumber);
        entity.setExpireDate(expireDate);
        entity.setNote(note);

        if (organization != null) {
            entity.setOrganization(organization.toEntity());
        }

        if (theCase != null) {
            entity.setTheCase(theCase.toEntity());
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

    public CaseDto getTheCase() {
        return theCase;
    }

    public void setTheCase(CaseDto theCase) {
        this.theCase = theCase;
    }

    public OrganizationDto getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationDto organization) {
        this.organization = organization;
    }

    public LocalDateTime getVaccinationDate() {
        return vaccinationDate;
    }

    public void setVaccinationDate(LocalDateTime vaccinationDate) {
        this.vaccinationDate = vaccinationDate;
    }

    public String getVaccineName() {
        return vaccineName;
    }

    public void setVaccineName(String vaccineName) {
        this.vaccineName = vaccineName;
    }

    public String getVaccineLotNumber() {
        return vaccineLotNumber;
    }

    public void setVaccineLotNumber(String vaccineLotNumber) {
        this.vaccineLotNumber = vaccineLotNumber;
    }

    public LocalDateTime getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(LocalDateTime expireDate) {
        this.expireDate = expireDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
