package org.pepfar.pdma.app.data.dto;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class PreventionChartDto {
	private Date fromDate;
	private Date toDate;
	private String title;
	private List<String> categories = new ArrayList<String>();
//	private TreeSet<Date> categoriesDate = new TreeSet<Date>();
	private Hashtable<String, PreventionChartSeriesDto> series = new Hashtable<String, PreventionChartSeriesDto>();
//	private Hashtable<Date, PreventionChartSeriesDto> series1 = new Hashtable<Date, PreventionChartSeriesDto>();

	private Map<Integer, Integer> numberMapping = new HashMap<>();
	public Date getFromDate() {
		return fromDate;
	}
	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}
	public Date getToDate() {
		return toDate;
	}
	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public List<String> getCategories() {
		if(this.series!=null && this.series.keySet()!=null && this.series.keySet().size()>0) {
			this.categories =new ArrayList<String>(this.series.keySet());
		}
		this.categories.sort((s1, s2)->{
			try {
				return (new SimpleDateFormat("MM/yyyy").parse(s1)).compareTo((new SimpleDateFormat("MM/yyyy").parse(s2)));
			} catch (ParseException e) {
				return ((s1)).compareTo((s2));
			}
		});
		return categories;
	}
//	public TreeSet<Date> getCategoriesDate() {
//		if(this.series1!=null && this.series1.keySet()!=null && this.series1.keySet().size()>0) {
//			this.categoriesDate =new TreeSet<Date>(this.series1.keySet());
//		}
//		return categoriesDate;
//	}
//	public void setCategoriesDate(TreeSet<Date> categoriesDate) {
//		this.categoriesDate = categoriesDate;
//	}
//	public Hashtable<Date, PreventionChartSeriesDto> getSeries1() {
//		return series1;
//	}
//	public void setSeries1(Hashtable<Date, PreventionChartSeriesDto> series1) {
//		this.series1 = series1;
//	}
	public Hashtable<String, PreventionChartSeriesDto> getSeries() {
		return series;
	}
	public void setSeries(Hashtable<String, PreventionChartSeriesDto> series) {
		this.series = series;
	}	
}
