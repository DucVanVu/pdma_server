package org.pepfar.pdma.app.data.dto;

import java.time.LocalDateTime;
import java.util.List;

import org.pepfar.pdma.app.data.types.HTSYesNoNone;
import org.pepfar.pdma.app.data.types.HTSc14;
import org.pepfar.pdma.app.utils.CustomLocalDateTimeDeserializer2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class PreventionFilterDto {
	private int pageIndex;

	private int pageSize;
	
	private String keyword;
	
	private OrganizationDto org;
	
	private StaffDto staff;
//Danh sách cơ sở y tế
	private List<Long> orgIds;

	private Boolean notComplete;
//	Loại báo cáo  (MER hay không MER)
	private List<Integer> reportTypes;
//	Mô hình
	private List<Integer> modality;
	
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime fromDate;
	
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime toDate;

	private Boolean disablePaging;
	
	private Long pnsCaseId;
	
	private HTSc14 c14;
	
	private HTSYesNoNone c15;
	
	private Long skipHTS;
	
	private Integer type;
	
	private Integer typeMap;
	
	private Integer keyMap;

	private Integer chart1; //0 là tháng 1 là quý 2 là năm
	
	private Integer chart2;
	
	private Integer chart3;
	
	private Integer chart4;
	
	private Integer chart5;
	
	private Integer chart6;
	
	private Integer chart7;
	
	private Integer chart8;
	
	private Integer chart9;
	
	private Integer chart10;
	
	private Integer chart11;
	
	private Integer chart12;
	
	private Integer chart13;
	
	private Integer chart14;
	
	private boolean skipPNS;

	private Integer year;

	private Integer quarter;

	private String text;

	private Integer sortField;
	
	private Long provinceId;
	
	
	
	public Long getProvinceId() {
		return provinceId;
	}

	public void setProvinceId(Long provinceId) {
		this.provinceId = provinceId;
	}

	public Integer getTypeMap() {
		return typeMap;
	}

	public void setTypeMap(Integer typeMap) {
		this.typeMap = typeMap;
	}

	public Integer getKeyMap() {
		return keyMap;
	}

	public void setKeyMap(Integer keyMap) {
		this.keyMap = keyMap;
	}

	public String getOrgidsString() {
		String ids="";
		if(this.orgIds!=null && this.orgIds.size()>0) {			
			for (Long orgId : this.orgIds) {
				ids+=orgId+",";
			}
			ids = ids.substring(0, ids.length()-1);
		}
		return ids;
	}

	public boolean isSkipPNS() {
		return skipPNS;
	}

	public void setSkipPNS(boolean skipPNS) {
		this.skipPNS = skipPNS;
	}
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public HTSc14 getC14() {
		return c14;
	}

	public void setC14(HTSc14 c14) {
		this.c14 = c14;
	}

	public Long getSkipHTS() {
		return skipHTS;
	}

	public void setSkipHTS(Long skipHTS) {
		this.skipHTS = skipHTS;
	}

	public HTSYesNoNone getC15() {
		return c15;
	}

	public void setC15(HTSYesNoNone c15) {
		this.c15 = c15;
	}

	public OrganizationDto getOrg() {
		return org;
	}

	public Boolean getNotComplete() {
		return notComplete;
	}

	public void setNotComplete(Boolean notComplete) {
		this.notComplete = notComplete;
	}

	public List<Integer> getReportTypes() {
		return reportTypes;
	}

	public void setReportTypes(List<Integer> reportTypes) {
		this.reportTypes = reportTypes;
	}

	public List<Integer> getModality() {
		return modality;
	}

	public void setModality(List<Integer> modality) {
		this.modality = modality;
	}

	public void setOrg(OrganizationDto org) {
		this.org = org;
	}

	public StaffDto getStaff() {
		return staff;
	}

	public void setStaff(StaffDto staff) {
		this.staff = staff;
	}

	public List<Long> getOrgIds() {
		return orgIds;
	}

	public void setOrgIds(List<Long> orgIds) {
		this.orgIds = orgIds;
	}

	public LocalDateTime getFromDate() {
		return fromDate;
	}

	public void setFromDate(LocalDateTime fromDate) {
		this.fromDate = fromDate;
	}

	public LocalDateTime getToDate() {
		return toDate;
	}

	public void setToDate(LocalDateTime toDate) {
		this.toDate = toDate;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public Boolean getDisablePaging() {
		return disablePaging;
	}

	public void setDisablePaging(Boolean disablePaging) {
		this.disablePaging = disablePaging;
	}

	public Long getPnsCaseId() {
		return pnsCaseId;
	}

	public void setPnsCaseId(Long pnsCaseId) {
		this.pnsCaseId = pnsCaseId;
	}

	public Integer getChart1() {
		return chart1;
	}

	public void setChart1(Integer chart1) {
		this.chart1 = chart1;
	}

	public Integer getChart2() {
		return chart2;
	}

	public void setChart2(Integer chart2) {
		this.chart2 = chart2;
	}

	public Integer getChart3() {
		return chart3;
	}

	public void setChart3(Integer chart3) {
		this.chart3 = chart3;
	}

	public Integer getChart4() {
		return chart4;
	}

	public void setChart4(Integer chart4) {
		this.chart4 = chart4;
	}

	public Integer getChart5() {
		return chart5;
	}

	public void setChart5(Integer chart5) {
		this.chart5 = chart5;
	}

	public Integer getChart6() {
		return chart6;
	}

	public void setChart6(Integer chart6) {
		this.chart6 = chart6;
	}

	public Integer getChart7() {
		return chart7;
	}

	public void setChart7(Integer chart7) {
		this.chart7 = chart7;
	}

	public Integer getChart8() {
		return chart8;
	}

	public void setChart8(Integer chart8) {
		this.chart8 = chart8;
	}

	public Integer getChart9() {
		return chart9;
	}

	public void setChart9(Integer chart9) {
		this.chart9 = chart9;
	}

	public Integer getChart10() {
		return chart10;
	}

	public void setChart10(Integer chart10) {
		this.chart10 = chart10;
	}

	public Integer getChart11() {
		return chart11;
	}

	public void setChart11(Integer chart11) {
		this.chart11 = chart11;
	}

	public Integer getChart12() {
		return chart12;
	}

	public void setChart12(Integer chart12) {
		this.chart12 = chart12;
	}

	public Integer getChart13() {
		return chart13;
	}

	public void setChart13(Integer chart13) {
		this.chart13 = chart13;
	}

	public Integer getChart14() {
		return chart14;
	}

	public void setChart14(Integer chart14) {
		this.chart14 = chart14;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public Integer getQuarter() {
		return quarter;
	}

	public void setQuarter(Integer quarter) {
		this.quarter = quarter;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Integer getSortField() {
		return sortField;
	}

	public void setSortField(Integer sortField) {
		this.sortField = sortField;
	}
}
