package org.pepfar.pdma.app.data.dto;

public class SelfTestDetailReportDto {
//	STT
	private String seq;
	
//	TÊN CHỈ SỐ
	private String title;
	
//	Có hỗ trợ trực tiếp
	private Integer supported=0;
	
//	Không có hỗ trợ
	private Integer unsupported=0;
	
//	TỔNG SỐ
	private Integer total=0;

	public String getSeq() {
		return seq;
	}

	public void setSeq(String seq) {
		this.seq = seq;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Integer getSupported() {
		return supported;
	}

	public void setSupported(Integer supported) {
		this.supported = supported;
	}

	public Integer getUnsupported() {
		return unsupported;
	}

	public void setUnsupported(Integer unsupported) {
		this.unsupported = unsupported;
	}

	public Integer getTotal() {
		total=0;
		if(supported!=null) {
			total+=supported;
		}
		if(unsupported!=null) {
			total+=unsupported;
		}
		return total;
	}
}
