package org.pepfar.pdma.app.data.dto;

import java.time.LocalDateTime;

import org.pepfar.pdma.app.utils.CustomLocalDateTimeDeserializer2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class OPCDashboardData {

	// dashlet 1
	private long totalActivePatients;

	private long lastMonthNew;

	private long lastMonthLTFU;

	private long lastMonthDead;

	private long lastMonthTransOut;

	// dashlet 2
	private long totalAppmtToday;

	private long arrivedToday;

	private long vlTestedToday;

	// dashlet 3
	private long totalLate;

	private long lateLt30;

	private long late30to60;

	private long lategt60;

	// dashlet 4
	private long totalVl3Months;

	private long vlLt20copies;

	private long vl20to200copies;

	private long vl200to1000copies;

	private long vlgt1000copies;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer2.class)
	private LocalDateTime dataTimestamp;

	public OPCDashboardData() {

	}

	public long getTotalActivePatients() {
		return totalActivePatients;
	}

	public void setTotalActivePatients(long totalActivePatients) {
		this.totalActivePatients = totalActivePatients;
	}

	public long getLastMonthNew() {
		return lastMonthNew;
	}

	public void setLastMonthNew(long lastMonthNew) {
		this.lastMonthNew = lastMonthNew;
	}

	public long getLastMonthLTFU() {
		return lastMonthLTFU;
	}

	public void setLastMonthLTFU(long lastMonthLTFU) {
		this.lastMonthLTFU = lastMonthLTFU;
	}

	public long getLastMonthDead() {
		return lastMonthDead;
	}

	public void setLastMonthDead(long lastMonthDead) {
		this.lastMonthDead = lastMonthDead;
	}

	public long getLastMonthTransOut() {
		return lastMonthTransOut;
	}

	public void setLastMonthTransOut(long lastMonthTransOut) {
		this.lastMonthTransOut = lastMonthTransOut;
	}

	public long getTotalAppmtToday() {
		return totalAppmtToday;
	}

	public void setTotalAppmtToday(long totalAppmtToday) {
		this.totalAppmtToday = totalAppmtToday;
	}

	public long getArrivedToday() {
		return arrivedToday;
	}

	public void setArrivedToday(long arrivedToday) {
		this.arrivedToday = arrivedToday;
	}

	public long getVlTestedToday() {
		return vlTestedToday;
	}

	public void setVlTestedToday(long vlTestedToday) {
		this.vlTestedToday = vlTestedToday;
	}

	public long getTotalLate() {
		return totalLate;
	}

	public void setTotalLate(long totalLate) {
		this.totalLate = totalLate;
	}

	public long getLateLt30() {
		return lateLt30;
	}

	public void setLateLt30(long lateLt30) {
		this.lateLt30 = lateLt30;
	}

	public long getLate30to60() {
		return late30to60;
	}

	public void setLate30to60(long late30to60) {
		this.late30to60 = late30to60;
	}

	public long getLategt60() {
		return lategt60;
	}

	public void setLategt60(long lategt60) {
		this.lategt60 = lategt60;
	}

	public long getTotalVl3Months() {
		return totalVl3Months;
	}

	public void setTotalVl3Months(long totalVl3Months) {
		this.totalVl3Months = totalVl3Months;
	}

	public long getVlLt20copies() {
		return vlLt20copies;
	}

	public void setVlLt20copies(long vlLt20copies) {
		this.vlLt20copies = vlLt20copies;
	}

	public long getVl20to200copies() {
		return vl20to200copies;
	}

	public void setVl20to200copies(long vl20to200copies) {
		this.vl20to200copies = vl20to200copies;
	}

	public long getVl200to1000copies() {
		return vl200to1000copies;
	}

	public void setVl200to1000copies(long vl200to1000copies) {
		this.vl200to1000copies = vl200to1000copies;
	}

	public long getVlgt1000copies() {
		return vlgt1000copies;
	}

	public void setVlgt1000copies(long vlgt1000copies) {
		this.vlgt1000copies = vlgt1000copies;
	}

	public LocalDateTime getDataTimestamp() {
		return dataTimestamp;
	}

	public void setDataTimestamp(LocalDateTime dataTimestamp) {
		this.dataTimestamp = dataTimestamp;
	}

}
