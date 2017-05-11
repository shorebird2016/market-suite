package org.marketsuite.framework.model.type;

import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.AppUtil;

import java.util.Calendar;

public enum CalendarQuarter {
    Q1_2014("2014-01-01", "2014-03-31", "2014 Q1"),
    Q2_2014("2014-04-01", "2014-06-30", "2014 Q2"),
    Q3_2014("2014-07-01", "2014-09-30", "2014 Q3"),
    Q4_2014("2014-10-01", "2014-12-31", "2014 Q4"),
    ;

    CalendarQuarter(String start_date, String end_date, String display) {
        startDate = AppUtil.stringToCalendarNoEx(start_date);
        endDate = AppUtil.stringToCalendarNoEx(end_date);
        displayString = display;
    }

    //----- public methods -----
    public String toString() { return displayString; }
    public Calendar getStartDate() { return startDate; }
    public Calendar getEndDate() { return endDate; }
    //is date in quarter?
    public static boolean isDateInQuarter(Calendar date, CalendarQuarter quarter) {
        return date.compareTo(quarter.getStartDate()) >= 0
            && date.compareTo(quarter.getEndDate()) <= 0;
    }
    //find out current calendar quarter, null = no matching quarter
    public static CalendarQuarter findQuarter(Calendar date) {
        CalendarQuarter[] qtrs = CalendarQuarter.values();
        for (CalendarQuarter qtr : qtrs)
            if (isDateInQuarter(date, qtr)) return qtr;
        return null;//not found
    }

    //----- variables -----
    private Calendar startDate, endDate; private String displayString;
}
