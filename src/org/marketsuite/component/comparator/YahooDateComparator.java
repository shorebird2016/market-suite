package org.marketsuite.component.comparator;

import org.marketsuite.framework.util.AppUtil;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Comparator;

/**
 * Compares two Yahoo types of dates: YYYY-MM-DD format, two dates must already be formatted properly.
 * Otherwise date1 will be smaller than date2.
 */
public class YahooDateComparator implements Comparator{
    public int compare(Object date1, Object date2) {
        if (date1.equals("") && date2.equals(""))
            return 0;
        else if (date1.equals(""))
            return -1;
        else if (date2.equals(""))
            return 1;

        String d1 = (String)date1;
        String d2 = (String)date2;
        try {
            Calendar c1 = AppUtil.stringToCalendar(d1);
            Calendar c2 = AppUtil.stringToCalendar(d2);
            return c1.compareTo(c2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;//fail to compare
    }
}
