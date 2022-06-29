package org.pepfar.pdma.app;

import com.beust.jcommander.internal.Lists;
import org.pepfar.pdma.app.data.domain.Appointment;
import org.pepfar.pdma.app.utils.CommonUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

public class Test {

    public static void main(String[] args) {
        LocalDateTime date = LocalDateTime.of(2021, 9, 30, 23, 59, 59);
        //		System.out.println(date.minusMonths(6).format(DateTimeFormatter.ofPattern("dd/MM/yyyy
        // hh:mm:ss")));

        LocalDateTime _adjLast6Months = CommonUtils.dateEnd(date.minusMonths(3));
        //		_adjLast6Months = _adjLast6Months.minusMinutes(1);

        boolean leapYear = _adjLast6Months.toLocalDate().isLeapYear();
        _adjLast6Months =
                _adjLast6Months.withDayOfMonth(_adjLast6Months.getMonth().length(leapYear));

        System.out.println(
                _adjLast6Months.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss")));

        //		Timestamp last6Months = CommonUtils.toTimestamp(_adjLast6Months);
        //		LocalDateTime prevMonthBegin = LocalDateTime.of(2021, 3, 1, 0, 0, 0);
        //		prevMonthBegin = prevMonthBegin.minusDays(15);
        //		prevMonthBegin = prevMonthBegin.withDayOfMonth(1);
        //		System.out.println(prevMonthBegin);
        //
        //		LocalDateTime midDate = LocalDateTime.of(2021, 8, 1, 0, 0, 0);
        //		int calendarQuarter = midDate.get(IsoFields.QUARTER_OF_YEAR);
        //
        //		System.out.println(calendarQuarter);
        //
        //		LocalDateTime d = LocalDateTime.of(2021, 4, 1, 0, 0, 0);
        //		System.out.println(d.minusMonths(9).format(DateTimeFormatter.ofPattern("dd/MM/yyyy
        // hh:mm:ss")));
        //		System.out.println(CommonUtils.generatePassayPassword());
        //		LocalDateTime dt1 = LocalDateTime.of(2021, 1, 2, 0, 0, 0);
        //		LocalDateTime dt2 = LocalDateTime.of(2021, 1, 1, 0, 0, 1);
        //		System.out.println(CommonUtils.dateDiff(ChronoUnit.DAYS, dt1, dt2));
        //		Hashids hashids = new Hashids("100", 20, "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890");
        //		System.out.println(hashids.encode(100));
        //
        //		for (long i = 0; i < 10000; i++) {
        //			String s1 = RandomStringUtils.random(20, false, true);
        //			if (s1.length() == 20) {
        //				System.out.println(s1);
        //			}
        //		}

        //		String a = "prov_1";
        //		System.out.println(a.substring(a.indexOf("_") + 1));
        //
        //		String input = "1";
        //		while (input.length() < 5) {
        //			input = "0" + input;
        //		}
        //
        //		System.out.println(input);
        //
        //		String name1 = "Nguyễn Tuấn Anh";
        //		String name2 = "Nguyễn Tuân Ánh";
        //
        //		double ds = StringUtils.getJaroWinklerDistance(name1, name2);
        //		System.out.println(ds);

        //		System.out.println(UUID.fromString("7CE33A92-08D3-422D-B9FE-35418AAAD52C").toString());

        //		System.out.println(WordUtils.capitalizeFully("nguyễn vĂn nam"));

        //		String s = "CK,CB,KC,HN,DT,DK,XD,BT,TS";
        //		System.out.println(s.indexOf("CB"));

        //		LocalDateTime todayStart =
        // LocalDateTime.now(ZoneId.of("GMT+7")).withHour(0).withMinute(0).withSecond(0)
        //				.withNano(0);
        //
        //		System.out.println(todayStart.minusDays(2));
        //		System.out.println(todayStart.minusDays(1));

        //		String[] vals = { "1", "1" };
        //
        //		List<String> l = Lists.newArrayList(vals);
        //		l.add("2");
        //
        //		vals = l.toArray(new String[0]);
        //
        //
        //		for (String val : vals) {
        //			System.out.println(val);
        //		}
        //
        //		System.out.println(Boolean.valueOf("false"));

        //		List<Long> list = new ArrayList<>();
        //		list.add(10l);
        //		list.add(8l);
        //		list.add(6l);
        //
        //		Long a = 10l;
        //
        //		System.out.println(list.contains(a.longValue()));

        //		long monthsBetween =
        // ChronoUnit.MONTHS.between(LocalDate.parse("2016-08-01").withDayOfMonth(1),
        //				LocalDate.parse("2016-08-31").withDayOfMonth(1));
        //		System.out.println(monthsBetween); // 3
        //
        for (long i = 0; i < 100; i++) {
            System.out.println(UUID.randomUUID().toString());
        }

        LocalDateTime yesterday = CommonUtils.hanoiTodayStart().minusMinutes(1);
        System.out.print(yesterday.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss")));
        //
        ////		System.out.println(PatientStatus.ACTIVE.name());
        //
        //		LocalDateTime startDate = LocalDateTime.of(2015, 2, 20, 0, 0, 0);
        //		LocalDateTime endDate = LocalDateTime.of(2017, 1, 15, 0, 0, 0);
        //
        //		System.out.println(CommonUtils.dateDiff(ChronoUnit.YEARS, startDate, endDate));
        //
        //		LocalDate date = LocalDate.of(2021, 1, 5);
        //		int weekOfYear = date.get(ChronoField.ALIGNED_WEEK_OF_YEAR);
        //		System.out.println(weekOfYear);
        //
        //		String s = "a/dtg/as";
        //		System.out.println(s.indexOf("dtg"));

        //		String s = "2009-10-10T10:10:10.000Z";
        //		s = s.substring(0, s.length() - 1);
        //		System.out.println(s);
        //
        //		LocalDateTime endOfLastWeek =
        // LocalDateTime.now(ZoneId.of("GMT+7")).minusWeeks(1).with(DayOfWeek.SUNDAY).withHour(23)
        //				.withMinute(59).withSecond(59).withNano(0);
        //
        //		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
        //		System.out.println(sdf.format(CommonUtils.fromLocalDateTime(endOfLastWeek)));
        //
        //		WeekFields weekFields = WeekFields.of(Locale.forLanguageTag("vi-VN"));
        //		System.out.println(endOfLastWeek.get(weekFields.weekOfWeekBasedYear()));
        //
        //		LocalDateTime[][] _8weeks = get8Weeks(endOfLastWeek);
        //		for (int i = 0; i < 8; i++) {
        //			System.out.println("Week " + (i + 1));
        //			System.out.println(sdf.format(CommonUtils.fromLocalDateTime(_8weeks[i][0])) + " - "
        //					+ sdf.format(CommonUtils.fromLocalDateTime(_8weeks[i][1])));
        //			System.out.println("---------");
        //		}
        //
        //		System.out.println();
        //		System.out.println();
        //
        //		LocalDateTime[][] _12months = get12Months(10,
        // LocalDateTime.now(ZoneId.of("GMT+7")).getYear() - 1);
        //		for (int i = 0; i < 12; i++) {
        //			System.out.println("Month " + (i + 1));
        //			System.out.println(sdf.format(CommonUtils.fromLocalDateTime(_12months[i][0])) + " - "
        //					+ sdf.format(CommonUtils.fromLocalDateTime(_12months[i][1])));
        //			System.out.println("---------");
        //		}

        //		List<DateRangeDto> ranges = CommonUtils.getBackwardQuarters(LocalDateTime.now(), 4);
        //		for (DateRangeDto r : ranges) {
        //			System.out.println(r.getFromDate() + " --> " + r.getToDate());
        //		}
        //
        //		Long id = 100l;
        //		System.out.println(id.toString());

        //		String[] RISK_CODES = { "risk_1", "risk_2", "risk_3", "risk_4", "risk_6", "risk_8",
        // "risk_7",
        //		"risk_5" };
        //		System.out.println(ArrayUtils.indexOf(RISK_CODES, "risk_3"));

        //		LocalDateTime from = LocalDateTime.parse("2021-02-01T00:00:00",
        // DateTimeFormatter.ISO_DATE_TIME);
        //		LocalDateTime to = LocalDateTime.parse("2021-04-30T00:00:00",
        // DateTimeFormatter.ISO_DATE_TIME);
        //
        //		System.out.println(getReportingPeriod(from, to));
        //
        //		String s = "nguyen, ";
        //		System.out.println(s.substring(0, s.length() - 2));
        LocalDateTime fromDate = LocalDateTime.of(2021, 7, 1, 0, 0);
        LocalDateTime thisMonthLastYear =
                fromDate.withDayOfMonth(15).withYear(fromDate.getYear() - 1);
        System.out.println(
                thisMonthLastYear.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss")));

        //
        Appointment app1 = new Appointment();
        app1.setId(1l);
        app1.setAppointmentDate(LocalDateTime.of(2021, 1, 12, 0, 0, 0));
        app1.setArrived(null);

        Appointment app2 = new Appointment();
        app2.setId(2l);
        app2.setAppointmentDate(LocalDateTime.of(2021, 2, 12, 0, 0, 0));
        app2.setArrived(false);

        Appointment app3 = new Appointment();
        app3.setId(3l);
        app3.setAppointmentDate(LocalDateTime.of(2021, 3, 12, 0, 0, 0));
        app3.setArrived(null);

        Appointment app4 = new Appointment();
        app4.setId(4l);
        app4.setAppointmentDate(LocalDateTime.of(2021, 4, 12, 0, 0, 0));
        app4.setArrived(null);

        List<Appointment> list = Lists.newArrayList(app1, app2, app3, app4);

        Stream<Appointment> apps =
                list
                        .stream(); // theCase.getAppointments().stream().filter(a ->
                                   // a.getOrganization().getId() == org.getId().longValue());
        Appointment latestArrived =
                list.stream()
                        .filter(a -> CommonUtils.isTrue(a.getArrived()))
                        .findFirst()
                        .orElse(null);
        Appointment latestNotArrivedRightAfterArrived =
                list.stream()
                        .filter(
                                a ->
                                        !CommonUtils.isTrue(a.getArrived())
                                                && (latestArrived != null
                                                        ? a.getAppointmentDate()
                                                                .isAfter(
                                                                        latestArrived
                                                                                .getAppointmentDate())
                                                        : true))
                        .min(
                                Comparator.comparing(
                                        Appointment::getAppointmentDate,
                                        (a1, a2) -> {
                                            return a1.isBefore(a2) ? 0 : 1;
                                        }))
                        .orElse(null);

        System.out.println(latestArrived == null ? "Null" : latestArrived.getId());
        System.out.println(
                latestNotArrivedRightAfterArrived == null
                        ? "Null"
                        : latestNotArrivedRightAfterArrived.getId());

        //
        LocalDateTime appDate = LocalDateTime.of(2021, 11, 01, 00, 00, 00);
        LocalDateTime nextappDate = LocalDateTime.of(2021, 11, 02, 00, 01, 00);
        long diff = CommonUtils.dateDiff(ChronoUnit.DAYS, appDate, nextappDate);
        System.out.println(diff);

        // sort
        List<Integer> indices = new ArrayList<>();
        indices.add(5);
        indices.add(2);
        indices.add(7);
        Collections.sort(indices);
        for (int a : indices) {
            System.out.println(a);
        }

        String phone = "090192291/ ";
        if (phone.isEmpty()) {
            phone = "-";
        } else if (phone.endsWith("/ ")) {
            phone = phone.substring(1, phone.length() - 2);
        }
        System.out.println(phone);
    }

    private static int getReportingPeriod(LocalDateTime fromDate, LocalDateTime toDate) {

        if (fromDate == null || toDate == null) {
            return -1;
        }

        if (fromDate.isAfter(toDate)) {
            return -1;
        }

        if (fromDate.getDayOfMonth() != 1) {
            return -1;
        }

        LocalDate trimedToDate = toDate.toLocalDate();
        LocalDate endOfMonth =
                trimedToDate.withDayOfMonth(
                        trimedToDate.getMonth().length(trimedToDate.isLeapYear()));

        if (!trimedToDate.isEqual(endOfMonth)) {
            return -1;
        }

        long monthsBetween =
                ChronoUnit.MONTHS.between(fromDate.withDayOfMonth(1), toDate.withDayOfMonth(1));

        if (monthsBetween != 0 && monthsBetween != 2) {
            return -1;
        }

        return (int) monthsBetween + 1;
    }

    @SuppressWarnings("unused")
    private static LocalDateTime[][] get12Months(final int month, final int year) {
        LocalDateTime[][] months = new LocalDateTime[12][2];

        LocalDateTime beginOfYear = null;
        LocalDateTime fromDate = null;
        LocalDateTime toDate = null;

        if (month >= 1 && month <= 9) {
            beginOfYear = LocalDateTime.of(year - 1, 10, 1, 0, 0, 0, 0);
        } else {
            beginOfYear = LocalDateTime.of(year, 10, 1, 0, 0, 0, 0);
        }

        for (int i = 0; i < 12; i++) {
            fromDate = beginOfYear.plusMonths(i);
            toDate =
                    fromDate.plusMonths(1)
                            .minusMinutes(1)
                            .withHour(23)
                            .withMinute(59)
                            .withSecond(59)
                            .withNano(0);
            months[i][0] = fromDate;
            months[i][1] = toDate;
        }

        return months;
    }

    @SuppressWarnings("unused")
    private static LocalDateTime[][] get8Weeks(final LocalDateTime toDateInput) {
        if (toDateInput == null) {
            return null;
        }

        LocalDateTime toDate = null;
        LocalDateTime fromDate = null;
        LocalDateTime[][] weeks = new LocalDateTime[8][2];
        for (int i = 0; i < 8; i++) {
            toDate = toDateInput.minusWeeks(i);
            fromDate =
                    toDate.plusMinutes(1)
                            .minusDays(7)
                            .withHour(0)
                            .withMinute(0)
                            .withSecond(0)
                            .withNano(0);
            weeks[7 - i][0] = fromDate;
            weeks[7 - i][1] = toDate;
        }

        return weeks;
    }
}
