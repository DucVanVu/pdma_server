package org.pepfar.pdma.app.data.dto;

public class HTSReportDetailsDto {

//	STT
	private String seq;
	
//	TÊN CHỈ SỐ
	private String title;
	
	//cộng đồng
	private Integer community;
	
	private Integer vtc;
	
	private Integer pknt;
	
	private Integer methadone;
	
	private Integer prep;
	
	//trại giam
	private Integer prison;
	//cơ sở khác
	private Integer otherOrg;
	
	//phòng khoa bệnh viện;
	private Integer department;
	
	private Integer sns;
	
	private Integer total;
	
	

	public Integer getSns() {
		return sns;
	}

	public void setSns(Integer sns) {
		this.sns = sns;
	}

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

	public Integer getCommunity() {
		return community;
	}

	public void setCommunity(Integer community) {
		this.community = community;
	}

	public Integer getVtc() {
		return vtc;
	}

	public void setVtc(Integer vtc) {
		this.vtc = vtc;
	}

	public Integer getPknt() {
		return pknt;
	}

	public void setPknt(Integer pknt) {
		this.pknt = pknt;
	}

	public Integer getMethadone() {
		return methadone;
	}

	public void setMethadone(Integer methadone) {
		this.methadone = methadone;
	}

	public Integer getPrep() {
		return prep;
	}

	public void setPrep(Integer prep) {
		this.prep = prep;
	}

	public Integer getPrison() {
		return prison;
	}

	public void setPrison(Integer prison) {
		this.prison = prison;
	}

	public Integer getOtherOrg() {
		return otherOrg;
	}

	public void setOtherOrg(Integer otherOrg) {
		this.otherOrg = otherOrg;
	}

	public Integer getDepartment() {
		return department;
	}

	public void setDepartment(Integer department) {
		this.department = department;
	}

	public Integer getTotal() {
		total=0;
		if(community!=null) {
			total+=community;
		}
		if(vtc!=null) {
			total+=vtc;
		}
		if(pknt!=null) {
			total+=pknt;
		}
		if(methadone!=null) {
			total+=methadone;
		}
		if(prep!=null) {
			total+=prep;
		}
		if(prison!=null) {
			total+=prison;
		}
		if(otherOrg!=null) {
			total+=otherOrg;
		}
		if(department!=null) {
			total+=department;
		}
		if(sns!=null) {
			total+=sns;
		}
		return total;
	}
	
	
 	
}
