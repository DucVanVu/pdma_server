package org.pepfar.pdma.app.data.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;
import org.pepfar.pdma.app.data.types.Gender;
import org.pepfar.pdma.app.utils.LocalDateTimeAttributeConverter;

@Entity
@Table(name = "tbl_selftest_specimen")
public class SelfTestSpecimen implements Serializable {

	@Transient
	private static final long serialVersionUID = -423199460047738115L;

	@Id
	@GenericGenerator(name = "native", strategy = "native")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "native")
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	/**
	 * @formatter:off
	 * Mã của sinh phẩm xét nghiệm cấp phát.
	 *  ORAQUICK = sinh phẩm OraQuick
	 *  INSTI = Sinh phẩm INSTI
	 *  OTHER = Sinh phẩm khác (ghi rõ tên)
	 * @formatter:on
	 */
	@Column(name = "code", length = 100, nullable = false)
	private String code; // mã sinh phẩm xét nghiệm (vd: ORAQUICK, ...)

	@Column(name = "name", length = 100, nullable = false)
	private String name; // tên sinh phẩm xét nghiệm (vd: Bộ xét nghiệm ORAQUICK, ...)

	/**
	 * @formatter:off
	 * Hình thức xét nghiệm:
	 *  W_SUPPORT = Có hỗ trợ trực tiếp
	 *  WO_SUPPORT = Không có hỗ trợ
	 * @formatter:on
	 */
	@Column(name = "support", length = 100, nullable = false)
	private String support;

	/**
	 * @formatter:off
	 * Người cần xét nghiệm.
	 *  SELF = Bản thân
	 *  SEXUAL_PARTNER = Bạn tình
	 *  IDU_PARTNER = Bạn chích chung
	 *  OTHER = Khác
	 * @formatter:on
	 */
	@Column(name = "client_type", length = 100, nullable = true)
	private String client;

	@Column(name = "client_yob", nullable = true)
	private Integer clientYob;

	@Enumerated(EnumType.STRING)
	@Column(name = "client_gender", nullable = true)
	private Gender clientGender;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@Column(name = "dispensing_date", nullable = false)
	private LocalDateTime dispensingDate;

	/**
	 * @formatter:off
	 * Nhóm nguy cơ của người cần xét nghiệm.
	 *  PWID = Tiêm chích ma tuý
	 *  MSM = Nam quan hệ đồng giới (MSM)
	 *  TG = Chuyển giới
	 *  FSW = Nữ bán dâm
	 *  PLHIV_PARTNER =  BT/BC của người có HIV
	 *  OTHER = Khác
	 * @formatter:on
	 */
	@Column(name = "client_risk_group", length = 100, nullable = true)
	private String clientRiskGroup;

	/**
	 * @formatter:off
	 * Nhóm nguy cơ của người cần xét nghiệm.
	 *  NONE_REACTIVE = Không phản ứng
	 *  REACTIVE =  Có phản ứng
	 *  OTHER = Từ chối trả lời/không biết
	 * @formatter:on
	 */
	@Column(name = "screen_result", length = 100, nullable = true)
	private String screenResult;

	/**
	 * @formatter:off
	 * Nhóm nguy cơ của người cần xét nghiệm.
	 *  POSITIVE = Dương tính
	 *  NEGATIVE = Âm tính
	 *  OTHER = Từ chối trả lời/không biết
	 * @formatter:on
	 */
	@Column(name = "confirm_result", length = 100, nullable = true)
	private String confirmResult;

	@ManyToOne
	@JoinColumn(name = "self_test_id", nullable = false)
	private SelfTestEntry selfTest;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSupport() {
		return support;
	}

	public void setSupport(String support) {
		this.support = support;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public Integer getClientYob() {
		return clientYob;
	}

	public void setClientYob(Integer clientYob) {
		this.clientYob = clientYob;
	}

	public Gender getClientGender() {
		return clientGender;
	}

	public void setClientGender(Gender clientGender) {
		this.clientGender = clientGender;
	}

	public String getClientRiskGroup() {
		return clientRiskGroup;
	}

	public void setClientRiskGroup(String clientRiskGroup) {
		this.clientRiskGroup = clientRiskGroup;
	}

	public String getScreenResult() {
		return screenResult;
	}

	public LocalDateTime getDispensingDate() {
		return dispensingDate;
	}

	public void setDispensingDate(LocalDateTime dispensingDate) {
		this.dispensingDate = dispensingDate;
	}

	public void setScreenResult(String screenResult) {
		this.screenResult = screenResult;
	}

	public String getConfirmResult() {
		return confirmResult;
	}

	public void setConfirmResult(String confirmResult) {
		this.confirmResult = confirmResult;
	}

	public SelfTestEntry getSelfTest() {
		return selfTest;
	}

	public void setSelfTest(SelfTestEntry selfTest) {
		this.selfTest = selfTest;
	}
}
