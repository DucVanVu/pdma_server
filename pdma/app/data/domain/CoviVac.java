package org.pepfar.pdma.app.data.domain;

import org.hibernate.annotations.GenericGenerator;
import org.pepfar.pdma.app.data.domain.auditing.AuditableEntity;
import org.pepfar.pdma.app.utils.LocalDateTimeAttributeConverter;
import org.pepfar.pdma.app.utils.UUIDAttributeConverter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tbl_covi_vac")
public class CoviVac extends AuditableEntity {

    @Id
    @GenericGenerator(name = "native", strategy = "native")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "native")
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Convert(converter = UUIDAttributeConverter.class)
    @Column(name = "uuid", unique = true, nullable = false, updatable = false, columnDefinition = "char(36)")
    private UUID uid;

    @ManyToOne
    @JoinColumn(name = "case_id", nullable = false)
    private Case theCase;

    @ManyToOne
    @JoinColumn(name = "org_id", nullable = false)
    private Organization organization;

    @Convert(converter = LocalDateTimeAttributeConverter.class)
    @Column(name = "vaccination_date", nullable = false)
    private LocalDateTime vaccinationDate;

    @Column(name = "vaacine_name", length = 100, nullable = false)
    private String vaccineName;

    @Column(name = "lot_number", length = 100, nullable = true)
    private String vaccineLotNumber;

    @Convert(converter = LocalDateTimeAttributeConverter.class)
    @Column(name = "expire_date", nullable = false)
    private LocalDateTime expireDate;

    @Lob
    @Column(name = "note", nullable = true)
    private String note;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Case getTheCase() {
        return theCase;
    }

    public void setTheCase(Case theCase) {
        this.theCase = theCase;
    }

    public UUID getUid() {
        return uid;
    }

    public void setUid(UUID uid) {
        this.uid = uid;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
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
