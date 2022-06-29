package org.pepfar.pdma.app.data.dto;

public class OPCDashboardFilterDto {

	private AdminUnitDto province;

	private Long organizationId;

	/**
	 * @formatter:off
	 * 
	 * Tham số này để giúp xác định biểu đồ cần vẽ, dựa vào đó sẽ lấy và trả về dữ liệu phù hợp: 
	 * 
	 * 1 = Tình hình quản lý bệnh nhân
	 * 2 = Hoạt động xét nghiệm TLVR
	 * 3 = Tình hình cấp ARV nhiều tháng
	 * 4 = Tình hình cấp ARV từ nguồn bảo hiểm
	 * 5 = Đánh giá nhóm nguy cơ
	 * 6 = Hoạt động sàng lọc lao
	 * 
	 * @formatter:on
	 */
	private int targetChart;

	public AdminUnitDto getProvince() {
		return province;
	}

	public void setProvince(AdminUnitDto province) {
		this.province = province;
	}

	public Long getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(Long organizationId) {
		this.organizationId = organizationId;
	}

	public int getTargetChart() {
		return targetChart;
	}

	public void setTargetChart(int targetChart) {
		this.targetChart = targetChart;
	}

}
