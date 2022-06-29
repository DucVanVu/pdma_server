package org.pepfar.pdma.app.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
public class DateTimeUtil {
	public static Date numberToDate(int day, int month, int year) throws ParseException {
		String dateString =  String.format("%02d", day) + "/" +  String.format("%02d", month) + "/" + year;
		Date date = new SimpleDateFormat("dd/MM/yyyy").parse(dateString);
		
		return date;
	}

	public static Date getLastDayOfMonth(int month, int year) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		String date = "01/" + String.format("%02d", month) + "/" + year;
		LocalDate localDate = LocalDate.parse(date, formatter);
		LocalDate lastDay = localDate.with(TemporalAdjusters.lastDayOfMonth());
		
		Date lastDayOfMonth = Date.from(lastDay.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		
		Calendar calendar = Calendar.getInstance();
	    calendar.setTime(lastDayOfMonth);
	    calendar.set(Calendar.HOUR_OF_DAY, 23);
	    calendar.set(Calendar.MINUTE, 59);
	    calendar.set(Calendar.SECOND, 59);
	    calendar.set(Calendar.MILLISECOND, 999);
	    
		return calendar.getTime();
	}
	
	public static List<LocalDate> getListMonthByMonthYear(int fromMonth,int fromyear,int toMonth,int toYear){
		List<int[]> ret = new ArrayList<int[]>();
		List<LocalDate> retCalendar = new ArrayList<LocalDate>();
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");		
		
		LocalDate beginCalendar = LocalDate.parse("01/" + String.format("%02d", fromMonth) + "/" + fromyear,formatter);
		LocalDate finishCalendar = LocalDate.parse(getLastDayOfMonth(toMonth,toYear).getDate()+"/" + String.format("%02d", toMonth)+ "/" + toYear,formatter);     
		
        while(beginCalendar.isBefore(finishCalendar)) {        	
        	retCalendar.add(beginCalendar);
        	beginCalendar = beginCalendar.plusMonths(1L);
        }
        return retCalendar;
	}
	
	public static Date getEndOfDay(Date date) {
		if(date!=null) {
			Calendar calendar = Calendar.getInstance();
		    calendar.setTime(date);
		    calendar.set(Calendar.HOUR_OF_DAY, 23);
		    calendar.set(Calendar.MINUTE, 59);
		    calendar.set(Calendar.SECOND, 59);
		    calendar.set(Calendar.MILLISECOND, 999);
		    return calendar.getTime();
		}
	    return null;
	}
	public static Date getStartOfDay(Date date) {
		if(date!=null) {
			Calendar calendar = Calendar.getInstance();
		    calendar.setTime(date);
		    calendar.set(Calendar.HOUR_OF_DAY, 00);
		    calendar.set(Calendar.MINUTE, 00);
		    calendar.set(Calendar.SECOND, 00);
		    calendar.set(Calendar.MILLISECOND, 000);		    
		    return calendar.getTime();
		}
	    return null;
	}

	public static Date getPrevDay(Date date) {
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(date);
	    calendar.add(Calendar.DAY_OF_MONTH, -1);
	    calendar.set(Calendar.HOUR_OF_DAY, 23);
	    calendar.set(Calendar.MINUTE, 59);
	    calendar.set(Calendar.SECOND, 59);
	    calendar.set(Calendar.MILLISECOND, 999);
	    return calendar.getTime();
	}
	
	public static Date getNextDay(Date date) {
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(date);
	    calendar.add(Calendar.DAY_OF_MONTH, 1);
	    calendar.set(Calendar.HOUR_OF_DAY, 00);
	    calendar.set(Calendar.MINUTE, 00);
	    calendar.set(Calendar.SECOND, 00);
	    calendar.set(Calendar.MILLISECOND, 000);
	    return calendar.getTime();
	}
	
//	public static void main(String[] args) {
//		List<Date> dates = getDatesInMonthJava7(2020, 4);
//		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
//		for(Date date: dates) {
//			System.out.println(simpleDateFormat.format(date));
//		}
//	}
	
	// month from 0-> 11
	public static List<Date> getDatesInMonthJava7(int year, int month) {
		Calendar calendar = Calendar.getInstance();
        int date = 1;
        calendar.set(year, month, date);
		Date startDate = calendar.getTime();
		
	    List<Date> datesInRange = new ArrayList<>();
	    calendar.setTime(startDate);
	     
	    Calendar endCalendar = Calendar.getInstance();
	    int maxDay = 0;
		maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		endCalendar.set(year, month, maxDay);
		endCalendar.add(Calendar.DATE, 1);
		Date endDate = endCalendar.getTime();
	    endCalendar.setTime(endDate);
	 
	    while (calendar.before(endCalendar)) {
	        Date result = calendar.getTime();
	        datesInRange.add(result);
	        calendar.add(Calendar.DATE, 1);
	    }
	    return datesInRange;
	}
	
	// month from 0-> 11
	public static List<Date> getDatesByYearMonth(int year, int month) {
		Calendar calendar = Calendar.getInstance();
        int date = 1;
        calendar.set(year, month, date);
		Date startDate = calendar.getTime();
		
	    List<Date> datesInRange = new ArrayList<>();
	    calendar.setTime(startDate);
	     
	    Calendar endCalendar = Calendar.getInstance();
	    int maxDay = 0;
		maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/yyyy");
		if(simpleDateFormat.format(new Date()).equals(simpleDateFormat.format(calendar.getTime()))) {
			endCalendar = Calendar.getInstance();
		}else {
			endCalendar.set(year, month, maxDay);
		}
		endCalendar.add(Calendar.DATE, 1);
		Date endDate = endCalendar.getTime();
	    endCalendar.setTime(endDate);
	 
	    while (calendar.before(endCalendar)) {
	        Date result = calendar.getTime();
	        datesInRange.add(result);
	        calendar.add(Calendar.DATE, 1);
	    }
	    return datesInRange;
	}
	
	public static boolean checkQuarter(Date date) throws ParseException {
		LocalDateTime currentDate = LocalDateTime.now();
		Date now = new Date();
		boolean isUpdate= false;
		Date startOfJanuary= numberToDate(1, 1, currentDate.getYear());// ngày 1 tháng 1
		startOfJanuary= getStartOfDay(startOfJanuary);
		
		Date endOfMarch= getLastDayOfMonth(3, currentDate.getYear());//ngày 31 tháng 3
		
		Date startOfApril= numberToDate(1, 4, currentDate.getYear());//ngày 1 tháng 4
		startOfApril= getStartOfDay(startOfApril);
		
		Date endOfJune= getLastDayOfMonth(6, currentDate.getYear());//ngày 30 tháng 6
		
		Date startOfJuly= numberToDate(1, 7, currentDate.getYear());//ngày 1 tháng 7
		startOfJuly = getStartOfDay(startOfJuly);
		
		Date endOfSeptember= getLastDayOfMonth(9, currentDate.getYear());// ngày 30 tháng 9
		
		Date startOfOctober= numberToDate(1, 10, currentDate.getYear());// ngày 1 tháng 10
		startOfOctober = getStartOfDay(startOfOctober);
		
		Date endOfDecember = getLastDayOfMonth(12, currentDate.getYear());// ngày 30 tháng 12
		
		if(now.after(startOfJanuary) && now.before(endOfMarch) && date.after(startOfJanuary) && date.before(endOfMarch)) {
			isUpdate=true;
		}
		if(now.after(startOfApril) && now.before(endOfJune) && date.after(startOfApril) && date.before(endOfJune)) {
			isUpdate=true;
		}
		if(now.after(startOfJuly) && now.before(endOfSeptember) && date.after(startOfJuly) && date.before(endOfSeptember)) {
			isUpdate=true;
		}
		if(now.after(startOfOctober) && now.before(endOfDecember) && date.after(startOfOctober) && date.before(endOfDecember)) {
			isUpdate=true;
		}
		
		
		
//		int quy=currentDate.getMonthOfYear()/4;// xac dinh quy hien tai
//		if(date!=null) {// check item da ton tai chua
//			Calendar cal1 = Calendar.getInstance();
//			cal1.setTime(date);
//			int monthOfDateReort= cal1.get(Calendar.MONTH)+1;
//			int yearOfDateReport= cal1.get(Calendar.YEAR);
//			switch (quy) {
//				case 0: //quy 1
//					if(monthOfDateReort>=1 && monthOfDateReort<=3 && yearOfDateReport==currentDate.getYear()) {
//						isUpdate=true;
//					}else {
//						isUpdate= false;
//					}
//					break;
//				case 1: //quy 2
//					if(monthOfDateReort>=4 && monthOfDateReort<=6 && yearOfDateReport==currentDate.getYear()) {
//						isUpdate=true;
//					}else {
//						isUpdate= false;
//					}
//					break;
//				case 2: //quy 3
//					if(monthOfDateReort>=7 && monthOfDateReort<=9 && yearOfDateReport==currentDate.getYear()) {
//						isUpdate=true;
//					}else {
//						isUpdate= false;
//					}
//					break;
//				case 3: //quy 4
//					if(monthOfDateReort>=10 && monthOfDateReort<=12 && yearOfDateReport==currentDate.getYear()) {
//						isUpdate=true;
//					}else {
//						isUpdate= false;
//					}
//					break;
//			}
//		}
		return isUpdate;
	}

	public static boolean checkEditableByMonth(int numberMonth, java.time.LocalDateTime date){
		if(date==null){
			return false;
		}
		java.time.LocalDateTime now = java.time.LocalDateTime.now();
		return !date.isBefore(now.minusMonths(numberMonth));
	}
	
}