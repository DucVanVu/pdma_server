package org.pepfar.pdma.app.data.domain;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;
import org.pepfar.pdma.app.data.domain.auditing.AuditableEntity;
import org.pepfar.pdma.app.data.types.SelfTestSource;
import org.pepfar.pdma.app.utils.LocalDateTimeAttributeConverter;

@Entity
@Table(name = "tbl_selftest_entry")
public class SelfTestEntry extends AuditableEntity {

	@Transient
	private static final long serialVersionUID = 2199312414217803945L;

	@Id
	@GenericGenerator(name = "native", strategy = "native")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "native")
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "org_id", nullable = false)
	private Organization organization;

	@ManyToOne
	@JoinColumn(name = "staff_id", nullable = false)
	private Staff dispensingStaff;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "dispensing_date", nullable = true)
	private LocalDateTime dispensingDate;

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "person_id", nullable = false)
	private Person person;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "selfTest")
	@OrderBy("id ASC")
	private Set<SelfTestSpecimen> specimens = new LinkedHashSet<>();

	//phương pháp tự xét nghiệm
	@Column(name = "self_test_source", nullable = true)
	@Enumerated(value = EnumType.STRING)
	private SelfTestSource selfTestSource;

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

	public Staff getDispensingStaff() {
		return dispensingStaff;
	}

	public void setDispensingStaff(Staff dispensingStaff) {
		this.dispensingStaff = dispensingStaff;
	}

	public LocalDateTime getDispensingDate() {
		return dispensingDate;
	}

	public void setDispensingDate(LocalDateTime dispensingDate) {
		this.dispensingDate = dispensingDate;
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public Set<SelfTestSpecimen> getSpecimens() {

		if (specimens == null) {
			specimens = new LinkedHashSet<>();
		}

		return specimens;
	}

	public void setSpecimens(Set<SelfTestSpecimen> specimens) {
		this.specimens = specimens;
	}

	public SelfTestSource getSelfTestSource() {
		return selfTestSource;
	}

	public void setSelfTestSource(SelfTestSource selfTestSource) {
		this.selfTestSource = selfTestSource;
	}
}
