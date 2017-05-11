package org.marketsuite.component.comparator;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;

/**
 * A comparator used in table for sorting date/time type column.
 */
public class DateTimeComparator implements Comparator<Object> , Serializable {
    /**
     * CTOR: create this comparator with specified time format
     * @param format a DateFormat object
     */
    DateTimeComparator(DateFormat format) {
        _TimeFormat = format;
    }

    /**
     * Returns the result of comparison
     * @param o1 first date/time object
     * @param o2 second date/time object
     * @return >0 first greater, <0 first smaller, 0 same
     */
	public int compare(Object o1, Object o2) {
        String strDate1 = o1.toString();
        String strDate2 = o2.toString();
        Date date1 = getDate(strDate1);
        Date date2 = getDate(strDate2);
        if (null == date1 || null == date2)
           return strDate1.compareTo(strDate2);
        return date1.compareTo(date2);
    }

    private Date getDate(String dateStr){
        Date date = null;
        if (!dateStr.equalsIgnoreCase("Forever") && !dateStr.equals("")) {
            try {
                date = _TimeFormat.parse(dateStr);
            }
            catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return date;
    }

    //private variables
    private DateFormat _TimeFormat;
}