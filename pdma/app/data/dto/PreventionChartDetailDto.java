package org.pepfar.pdma.app.data.dto;

import java.math.BigDecimal;
import java.math.BigInteger;

public class PreventionChartDetailDto {
	private BigInteger quantity;
	private Integer year=0;
	private Integer month=0;
	private String title;
	public BigInteger getQuantity() {
		if(quantity==null) {
			quantity=BigInteger.valueOf(0);
		}
		return quantity;
	}
	public void setQuantity(BigInteger quantity) {
		this.quantity = quantity;
	}
	public Integer getYear() {
		return year;
	}
	public void setYear(Integer year) {
		this.year = year;
	}
	public Integer getMonth() {
		return month;
	}
	public void setMonth(Integer month) {
		this.month = month;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
}
