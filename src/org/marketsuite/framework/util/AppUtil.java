package org.marketsuite.framework.util;

import org.marketsuite.component.util.CoreUtil;
import org.marketsuite.component.util.ObjectCloner;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.*;
import org.marketsuite.framework.model.type.RankingSamplePeriod;
import org.marketsuite.framework.model.type.Timeframe;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.marektview.ranking.RankElement;
import org.marketsuite.marektview.ranking.Ranking;
import org.marketsuite.watchlist.model.WatchListModel;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.Year;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.marektview.ranking.RankElement;
import org.marketsuite.watchlist.model.WatchListModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.*;

//------------ Time related helpers ----------------
public class AppUtil {
    //convert string in "YYYY-MM-DD" into Calendar, compute number of years
    public static double calcYear(String begin_date, String end_date) throws ParseException {
        Date bd = FrameworkConstants.YAHOO_DATE_FORMAT.parse(begin_date);
        Date ed = FrameworkConstants.YAHOO_DATE_FORMAT.parse(end_date);
        double dur = ed.getTime() - bd.getTime();//in miliseconds
        double sec_yr = 365.0 * 24.0 * 60 * 60 * 1000;
        double ret = dur / sec_yr;
        return ret;
    }
    //convert date string into calendar
    public static Calendar stringToCalendar(String date) throws ParseException {
        Date d = FrameworkConstants.YAHOO_DATE_FORMAT.parse(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        return cal;
    }
    public static Calendar stringToCalendarNoEx(String date) {
        Date d = null;
        try {
            d = FrameworkConstants.YAHOO_DATE_FORMAT.parse(date);
        } catch (ParseException e) {
            System.err.println("Fail to parse Date ==> " + date);
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        return cal;
    }
    public static String calendarToString(Calendar cal) {
        return FrameworkConstants.YAHOO_DATE_FORMAT.format(cal.getTime());
    }
    //check beginning of month, day, year use YAHOO date format
    public static boolean isBeginMonth(String prev_date, String cur_date) {
        Calendar prev_cal = stringToCalendarNoEx(prev_date);
        Calendar cur_cal = stringToCalendarNoEx(cur_date);
        return prev_cal.get(Calendar.MONTH) != cur_cal.get(Calendar.MONTH);
    }
    public static boolean isBeginYear(String prev_date, String cur_date) {
        Calendar prev_cal = stringToCalendarNoEx(prev_date);
        Calendar cur_cal = stringToCalendarNoEx(cur_date);
        return prev_cal.get(Calendar.YEAR) != cur_cal.get(Calendar.YEAR);
    }
    public static boolean isBeginQuarter(String prev_date, String cur_date) {
        if (!isBeginMonth(prev_date, cur_date)) return false;
        Calendar prev_cal = stringToCalendarNoEx(prev_date);
        Calendar cur_cal = stringToCalendarNoEx(cur_date);
        return prev_cal.get(Calendar.MONTH) % 3 == 0;//3,6,9,12
    }
    //convert index into calendar, use SP500 as guide
    public static Calendar indexToCalendar(int quote_index) {
        return stringToCalendarNoEx(FrameworkConstants.SP500_DATA.getQuote().get(quote_index).getDate());
    }
    //find first monday of a month, month is zero based
    public static Calendar calcFirstMonday(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);
        int dow = cal.get(Calendar.DAY_OF_WEEK);
        switch (dow) {
            case Calendar.SUNDAY:
                cal.add(Calendar.DAY_OF_MONTH, 1);
                break;

            case Calendar.MONDAY:
                break;

            case Calendar.TUESDAY:
                cal.add(Calendar.DAY_OF_MONTH, 6);
                break;

            case Calendar.WEDNESDAY:
                cal.add(Calendar.DAY_OF_MONTH, 5);
                break;

            case Calendar.THURSDAY:
                cal.add(Calendar.DAY_OF_MONTH, 4);
                break;

            case Calendar.FRIDAY:
                cal.add(Calendar.DAY_OF_MONTH, 3);
                break;

            case Calendar.SATURDAY:
                cal.add(Calendar.DAY_OF_MONTH, 2);
                break;
        }
        return cal;
    }
    //find last trading day of the year with weekly data, ie. Monday of the last week, likely to always be there
    public static Calendar calcLastTradingDay(int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.DAY_OF_MONTH, 31);
        cal.set(Calendar.MONTH, 11);//0 based
        return calcPastMonday(cal);
    }
    //find monday of current week
    public static Calendar calcPastMonday(Calendar cal) {
        int dow = cal.get(Calendar.DAY_OF_WEEK);
        switch(dow) {
            case Calendar.SUNDAY:
                cal.add(Calendar.DAY_OF_MONTH, -6);
                break;

            case Calendar.MONDAY:
                //do nothing
                break;

            case Calendar.TUESDAY:
                cal.add(Calendar.DAY_OF_MONTH, -1);
                break;

            case Calendar.WEDNESDAY:
                cal.add(Calendar.DAY_OF_MONTH, -2);
                break;

            case Calendar.THURSDAY:
                cal.add(Calendar.DAY_OF_MONTH, -3);
                break;

            case Calendar.FRIDAY:
                cal.add(Calendar.DAY_OF_MONTH, -4);
                break;

            case Calendar.SATURDAY:
                cal.add(Calendar.DAY_OF_MONTH, -5);
                break;
        }
        return cal;
    }
//TODO other time frames and go forward for both methods.....
    //move given quote index forward/backward by 1 unit of time frame, backward = back in time
    public static int moveIndexByUnitTimeframe(int quote_index, Timeframe time_frame, boolean backward) {
        ArrayList<FundQuote> quotes = FrameworkConstants.SP500_DATA.getQuote();
        String quote_date = quotes.get(quote_index).getDate();
        Calendar cal_prev = stringToCalendarNoEx(quote_date);
        if (backward) {
            for (int idx = quote_index + 1; idx < quotes.size(); idx++) {
                String dt = quotes.get(idx).getDate();
                Calendar cal = stringToCalendarNoEx(dt);
                switch (time_frame) {
                    case Weekly://find day of week from small to large
                        if (cal_prev.get(Calendar.DAY_OF_WEEK) < cal.get(Calendar.DAY_OF_WEEK))
                            return idx;

                    default: break;
                }
                cal_prev = cal;
            }
        }
        else {

        }
        return -1;
    }
    //move given calendar forward/backward by 1 unit of time frame,
    public static Calendar moveCalendarByUnitTimeFrame(Calendar cur_cal, Timeframe time_frame, boolean backward) {
        Calendar begin_cal = (Calendar)ObjectCloner.copy(cur_cal);
        if (backward) {
            switch (time_frame) {
                case Daily: begin_cal.add(Calendar.DAY_OF_YEAR, -1); break;
                case Weekly: begin_cal.add(Calendar.WEEK_OF_YEAR, -1); break;
                case BiWeekly: begin_cal.add(Calendar.WEEK_OF_YEAR, -2); break;
                case Monthly: begin_cal.add(Calendar.MONTH, -1); break;
                case BiMonthly: begin_cal.add(Calendar.MONTH, -2); break;
                case Quarterly: begin_cal.add(Calendar.MONTH, -3); break;
                case SemiAnnually: begin_cal.add(Calendar.MONTH, -6); break;
                case Annually: begin_cal.add(Calendar.YEAR, -1); break;
                case BiAnnually: begin_cal.add(Calendar.YEAR, -2); break;
                case ThreeYear: begin_cal.add(Calendar.YEAR, -3); break;
                case FiveYear: begin_cal.add(Calendar.YEAR, -4); break;
                default: break;
            }
        }
        else {

        }
        return begin_cal;
    }
    //is there quote for a given date?
    public static boolean isDataAvailable(FundData fund, Calendar cal) {
        String date = calendarToString(cal);
        return fund.findIndexByDate(date) != -1;
    }
    public static boolean isDataAvailable(Calendar cal) {
        String date = calendarToString(cal);
        return FrameworkConstants.SP500_DATA.findIndexByDate(date) != -1;
    }
    public static boolean isDateFriday(Calendar cal) {
        return cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY;
    }
    //is index out of bounds?
    public static boolean isDataAvailable(FundData fund, int index) {
        return (index >= 0 && index < fund.getQuote().size());
    }
    //find first monday of a month and adjust for holiday
    public static String findAndAdjustFirstMonday(FundData fund, int year, int month) {
        Calendar cal = AppUtil.calcFirstMonday(year, 0);
        String date = AppUtil.calendarToString(cal);
        //make sure start_date exist in data set, otherwise adjust it forward
        return AppUtil.adjustDateForHoliday(fund, cal);
    }
    public static String adjustDateForHoliday(FundData fund, Calendar cal) {
        String date = AppUtil.calendarToString(cal);
        if (fund.findIndexByDate(date) == -1) {
            cal.add(Calendar.DAY_OF_MONTH, 1);//use next day
            date = AppUtil.calendarToString(cal);
            if (fund.findIndexByDate(date) == -1) {
                cal.add(Calendar.DAY_OF_MONTH, 1);//one more day, sometime holiday
                date = AppUtil.calendarToString(cal);
            }
        }
        return date;
    }
    //calculate number of days between two dates, date1 and date2 both use YAHOO format "yyyy-mm-dd"
    public static int calcDaysBetween(String date1, String date2) throws ParseException {
        Calendar cal1 = stringToCalendar(date1);
        Calendar cal2 = stringToCalendar(date2);
        return calcDaysBetween(cal1, cal2);
//        float days = (cal1.getTimeInMillis() - cal2.getTimeInMillis()) / (24 * 60 * 60 * 1000);
//        if (days < 0)
//            days = -days;
//        return  (int)days;
    }
    //find quote of first day of year with daily quote available, null = not found
    public static FundQuote findFirstQuoteInYear(FundData fund, int year) {
        return findFirstQuoteInYear(fund.getQuote(), year);
//        ArrayList<FundQuote> quotes = fund.getQuote();
//        for (int idx = 0; idx < quotes.size(); idx++) {//fund has new date first
//            int yr = extractYear(quotes.get(idx).getDate());
//            if (yr >= year)
//                continue;
//
//            //cross year, use last index
//            return quotes.get(idx - 1);
//        }
//        //for partial first year, use partial beginning
//        FundQuote last_quote = quotes.get(fund.getSize() - 1);
//        int yr = extractYear(last_quote.getDate());
//        if (yr == year)
//            return last_quote;
//        return null;
    }
    public static FundQuote findFirstQuoteInYear(ArrayList<FundQuote> quotes, int year) {
        for (int idx = 0; idx < quotes.size(); idx++) {//fund has new date first
            int yr = extractYear(quotes.get(idx).getDate());
            if (yr >= year)
                continue;

            //cross year, use last index
            return quotes.get(idx - 1);
        }
        //for partial first year, use partial beginning
        FundQuote last_quote = quotes.get(quotes.size() - 1);
        int yr = extractYear(last_quote.getDate());
        if (yr == year)
            return last_quote;
        return null;
    }
    //find last day of year with daily quote available
    public static FundQuote findLastQuoteInYear(FundData fund, int year) {
        ArrayList<FundQuote> quotes = fund.getQuote();
        for (int idx = 0; idx < quotes.size(); idx++) {//fund has new date first
            int yr = extractYear(quotes.get(idx).getDate());
            if (yr > year)
                continue;

            //cross year, use this index
            return quotes.get(idx);
        }
        return null;
    }
    //find next valid quote forward in time which quote exists for given date, null = not found
    //  if spec earlier than beginning, beginning is returned
    public static FundQuote findNearestQuote(FundData fund, Calendar spec) {
        return findNearestQuote(fund.getQuote(), spec);
//        ArrayList<FundQuote> quotes = fund.getQuote();
//        String begin_date = quotes.get(quotes.size() - 1).getDate();
//        Calendar begin_cal = stringToCalendarNoEx(begin_date);
//        if (begin_cal.compareTo(spec) > 0)
//            return quotes.get(quotes.size() - 1);//beyond history, use last point
//
//        //in range
//        for (int idx = quotes.size() - 1; idx >= 0 ; idx--) {//YAHOO format
//            String dt = quotes.get(idx).getDate();
//            Calendar cal = stringToCalendarNoEx(dt);
//            if (cal.compareTo(spec) >= 0)
//                return quotes.get(idx);
//        }
//        return quotes.get(0);//found none, use most recent
    }
    public static FundQuote findNearestQuote(ArrayList<FundQuote> quotes, Calendar spec) {
        String begin_date = quotes.get(quotes.size() - 1).getDate();
        Calendar begin_cal = stringToCalendarNoEx(begin_date);
        if (begin_cal.compareTo(spec) > 0)
            return quotes.get(quotes.size() - 1);//beyond history, use last point

        //in range
        for (int idx = quotes.size() - 1; idx >= 0 ; idx--) {//YAHOO format
            String dt = quotes.get(idx).getDate();
            Calendar cal = stringToCalendarNoEx(dt);
            if (cal.compareTo(spec) >= 0)
                return quotes.get(idx);
        }
        return quotes.get(0);//found none, use most recent
    }

    //find most recent date back in time with quote using specified date for a symbol
    //  null = no quotes available
    public static Calendar findRecentQuoteDate(FundData fund, Calendar spec) {
        if (isDataAvailable(fund, spec))
            return spec;//this one is good

        //check if spec is too old
        ArrayList<FundQuote> quotes = fund.getQuote();
        String oldest_date = quotes.get(quotes.size() - 1).getDate();
        Calendar oldest_cal = stringToCalendarNoEx(oldest_date);
        if (oldest_cal.compareTo(spec) > 0)
            return null;

        //look up back in time from spec for a valid quote
        //find valid calendar
        while (true) {
            spec.add(Calendar.DAY_OF_YEAR, -1);
            if (fund.findQuoteByDate(calendarToString(spec)) != null)
                return spec;
        }
    }

    public static Calendar findFutureFriday() {
        Calendar cal = Calendar.getInstance();
        while (true) {
            if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY)
                return cal;
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    public static Calendar findRecentTradingFriday() {
        int quote_index = 0;
        Calendar cal = FrameworkConstants.SP500_DATA.findDateByIndex(quote_index);
        while (true) {
            if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
                if (isDataAvailable(cal))
                    return cal;
            }
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }
    }

    //find nearest Friday index forwardly for specified fund and data, null = not found, date too early
    public static FundQuote findNearestFridayQuote(FundData fund, Calendar spec) {
        ArrayList<FundQuote> quotes = fund.getQuote();
        String begin_date = quotes.get(quotes.size() - 1).getDate();
        Calendar begin_cal = stringToCalendarNoEx(begin_date);
        if (begin_cal.compareTo(spec) > 0)
            return null;

        //find nearest Friday with quote
        for (int idx = quotes.size() - 1; idx >= 0 ; idx--) {//YAHOO format
            String dt = quotes.get(idx).getDate();
            Calendar cal = stringToCalendarNoEx(dt);
            if (cal.compareTo(spec) >= 0) {//found spec point
                int dow = cal.get(Calendar.DAY_OF_WEEK);
                if (dow == Calendar.FRIDAY)
                    return quotes.get(idx);
            }
        }
        return null;
    }

    //gather all the FRIDAY's indices in quote array using SP500 from a given index
    // if Friday is holiday, look back to Thursday, Wednesday till valid quote is found
    private static ArrayList<Integer> collectFridayQuoteIndices(int start_index) {
        ArrayList<Integer> ret = new ArrayList<>();
        FundData sp500 = FrameworkConstants.SP500_DATA;

        //find first Friday from start_index forwardly (toward lower index)
        int index = start_index;
        int last_index = start_index;
        while (index >= 0) {//up till latest quote
            Calendar cal = AppUtil.stringToCalendarNoEx(sp500.getDate(index));
            if (Calendar.FRIDAY == cal.get(Calendar.DAY_OF_WEEK)) {
                int delta = index - last_index;
//todo handle two Fridays missing

                //when quote index difference is greater than 5, possibly skipping a Friday, insert mid point between two
                if (delta > 5)
                    ret.add(last_index - (delta / 2));
//                    for (int idx = last_index - 1; idx < index; idx--) {
//                        if (isDataAvailable(sp500, idx)) {
//                            ret.add(idx);
//Calendar non_friday = AppUtil.stringToCalendarNoEx(sp500.getDate(delta/2));
//System.out.println("--- collectFridayQuoteIndices() -- add non-Friday" + new SimpleDateFormat("MM/dd/yyyy").format(non_friday.getTime()));
//                            break;
//                        }
//                    }
                ret.add(index);
            }
            last_index = index;
            index--;
        }

        //if the most recent Friday is more than 5 days away from today, pick most recent Thursday, Wednesday
//        int recent_index = ret.get(ret.size() - 1);
//        Calendar recent_friday = AppUtil.stringToCalendarNoEx(FrameworkConstants.SP500_DATA.getDate(recent_index));
//        Calendar today = Calendar.getInstance();
//        today.add(Calendar.DAY_OF_YEAR, -7);
//        if (today.compareTo(recent_friday) >= 0)
//            ret.add(recent_index / 2);//add one halfway
        return ret;
    }

    private static ArrayList<Integer> collectMonthEndQuoteIndices(int start_index) {
        ArrayList<Integer> ret = new ArrayList<>();
        FundData sp500 = FrameworkConstants.SP500_DATA;

        //forward in time finding all the month end indices
        Calendar cal = stringToCalendarNoEx(sp500.getQuote().get(start_index).getDate());
        Calendar end_cal = stringToCalendarNoEx(sp500.getQuote().get(0).getDate());
        while (true) {//up till latest quote
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            if (!isDataAvailable(cal)) //find nearest prior quote
                cal = findRecentQuoteDate(sp500, cal);
            ret.add(sp500.findIndexByDate(calendarToString(cal)));

            //go to beginning of next month, exit loop if new month is most recent quote's month
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.add(Calendar.MONTH, 1);
            int month = cal.get(Calendar.MONTH); int yr = cal.get(Calendar.YEAR);
            int end_month = end_cal.get(Calendar.MONTH); int end_yr = end_cal.get(Calendar.YEAR);
            if (month == end_month && yr == end_yr) {
                ret.add(sp500.findIndexByDate(calendarToString(end_cal)));
                break;
            }
        }
        return CoreUtil.reverse(ret);
    }

//TODO may not be exact since these may not land on same WEEKDAY, just equal length segments
    /**
     * From quotes array, collect indices of Fridays filtered by sample period
     * @param period sample period; weekly, bi-weekly, monthly...etc
     * @param start_index earliest starting point of quote array (how much to look back in time?)
     * @return array of indices from present to earlier days
     */
    public static ArrayList<Integer> collectQuoteIndices(RankingSamplePeriod period, int start_index) {
//        ArrayList<Integer> fridays = collectFridayQuoteIndices(start_index);
        switch (period) {
            case WEEKLY://7 days in fact, not calendar every friday, TODO maybe later a different mode
                ArrayList<Integer> qidx = new ArrayList<>();
                for (int n = 0; n <= start_index; n += 5)//from recent to oldest
                    qidx.add(n);//every 5 quote is one week
                return qidx;

            case BI_WEEKLY://use 10 trading days segment (as opposed to using Fridays)
                qidx = new ArrayList<>();
                for (int n = 0; n <= start_index; n += 10)//from recent to oldest
                    qidx.add(n);//every 10 quote is one week
                return qidx;

            case SEMI_MONTHLY:
                break;

            case MONTHLY:
                qidx = new ArrayList<>();
                for (int n = 0; n <= start_index; n += 20)//from recent to oldest
                    qidx.add(n);//every 10 quote is one week
                return qidx;

//                return collectMonthEndQuoteIndices(start_index);

            case QUARTERLY:
                break;

            case SEMI_ANNUALLY:
                break;

            case ANNUALLY:
                break;
        }
        return null;
    }

//TODO figure out all the gaps (holidays) in the quotes
    public static ArrayList<Integer> collectGapIndices() { return null; }

    public static ArrayList<Calendar> collectQuoteDates(RankingSamplePeriod period, int start_index) {
        ArrayList<Calendar> ret = new ArrayList<>();
        ArrayList<Integer> indices = new ArrayList<>();
        switch (period) {
            case WEEKLY:
                indices = collectFridayQuoteIndices(start_index);
                break;

            case SEMI_MONTHLY:
            case MONTHLY:
            case QUARTERLY:
            case SEMI_ANNUALLY:
            case ANNUALLY:
                break;
        }
        for (Integer quote_index : indices)
            ret.add(FrameworkConstants.SP500_DATA.findDateByIndex(quote_index));
        return ret;
    }

    //convert YAHOO date format string into JFreeChart's Day format
    public static Day stringToDay(String date_string) {
        int day = Integer.parseInt(date_string.substring(8, 10));
        int month = Integer.parseInt(date_string.substring(5, 7));
        int year = Integer.parseInt(date_string.substring(0, 4));
        return new Day(day, month, year);
    }

    //calculate performance between two dates of a security, if fund empty, begin_cal, end_cal no quotes, exception
    public static float calcReturn(FundData fund, Calendar begin_cal, Calendar end_cal) throws IllegalArgumentException {
        if (fund.getSize() <= 0)
            throw new IllegalArgumentException(FrameworkConstants.FRAMEWORK_BUNDLE.getString("wl_msg_3"));
        FundQuote begin_quote = fund.findQuoteByDate(calendarToString(begin_cal));
        if (begin_quote == null)
            throw new IllegalArgumentException(FrameworkConstants.FRAMEWORK_BUNDLE.getString("wl_msg_4") + fund.getSymbol() + "/" + calendarToString(begin_cal));
        FundQuote end_quote = fund.findQuoteByDate(calendarToString(end_cal));
        if (end_quote == null)
            throw new IllegalArgumentException(FrameworkConstants.FRAMEWORK_BUNDLE.getString("wl_msg_4") + fund.getSymbol() + "/" + calendarToString(end_cal));

        return (end_quote.getClose() / begin_quote.getClose()) - 1;
    }

    //calculate relative performance between a symbol and baseline symbol over a period between two dates
    public static float calcBaselineReturn(FundData fund, Calendar begin_cal, Calendar end_cal, FundData baseline_fund) {
        FundQuote begin_quote = fund.findQuoteByDate(calendarToString(begin_cal));
        FundQuote end_quote = fund.findQuoteByDate(calendarToString(end_cal));
        FundQuote baseline_begin_quote = baseline_fund.findQuoteByDate(calendarToString(begin_cal));
        FundQuote baseline_end_quote = baseline_fund.findQuoteByDate(calendarToString(end_cal));
        float pct = (end_quote.getClose() - begin_quote.getClose()) / begin_quote.getClose();
        float bl_pct = (baseline_end_quote.getClose() - baseline_begin_quote.getClose()) / baseline_begin_quote.getClose();
        return pct - bl_pct;
    }

    //calculate difference between two dates
    public static int calcDaysBetween(Calendar cal1, Calendar cal2) {
        float days = (cal1.getTimeInMillis() - cal2.getTimeInMillis()) / (24 * 60 * 60 * 1000);
        if (days < 0)
            days = -days;
        return  (int)days;
    }

    public static Calendar calcBeginTime(FundData fund, Calendar end_cal, int time_code) {
        Calendar begin_cal = (Calendar) ObjectCloner.copy(end_cal);
        switch (time_code) {
            case COLUMN_1_WEEK_PCT:
                begin_cal.add(Calendar.DAY_OF_YEAR, -7);
                break;

            case COLUMN_2_WEEK_PCT:
                begin_cal.add(Calendar.DAY_OF_YEAR, -14);
                break;

            case COLUMN_4_WEEK_PCT:
                begin_cal.add(Calendar.DAY_OF_YEAR, -28);
                break;

            case COLUMN_2_MONTH_PCT:
                begin_cal.add(Calendar.MONTH, -2);
                break;

            case COLUMN_3_MONTH_PCT:
                begin_cal.add(Calendar.MONTH, -3);
                break;

            case COLUMN_6_MONTH_PCT:
                begin_cal.add(Calendar.MONTH, -6);
                break;

            case COLUMN_12_MONTH_PCT:
                begin_cal.add(Calendar.MONTH, -12);
                break;

            case COLUMN_YTD_PCT:
                FundQuote first_quote = AppUtil.findFirstQuoteInYear(fund, end_cal.get(Calendar.YEAR));
                return AppUtil.stringToCalendarNoEx(first_quote.getDate());

            default:
                ArrayList<FundQuote> quotes = fund.getQuote();
                begin_cal = AppUtil.stringToCalendarNoEx(quotes.get(quotes.size() - 1).getDate());
                return begin_cal;
        }
        return AppUtil.findRecentQuoteDate(fund, begin_cal);
    }
    // time codes TODO change to enum
    public  static final int COLUMN_1_WEEK_PCT = 2;
    public  static final int COLUMN_2_WEEK_PCT = 3;
    public  static final int COLUMN_4_WEEK_PCT = 4;
    public  static final int COLUMN_2_MONTH_PCT = 5;
    public  static final int COLUMN_3_MONTH_PCT = 6;
    public  static final int COLUMN_6_MONTH_PCT = 7;
    public  static final int COLUMN_12_MONTH_PCT = 8;
    public  static final int COLUMN_YTD_PCT = 9;

    //----------- Ranking related helpers ----------
    //calculate rank frequency of an array of ranks with values from 1..max_rank 1 being the best
    public static ArrayList<Integer> calcRankFrequency(ArrayList<Integer> ranks, int max_rank) {
        ArrayList<Integer> ret = new ArrayList<>(max_rank);//from rank 1..max_rank
        for (int i=0; i<max_rank; i++) ret.add(0);//init
        for (int i = 0; i < ranks.size(); i++) {
            int rank = ranks.get(i);//1..max_rank index into ret
            int count = ret.get(rank - 1) + 1;
            ret.set(rank - 1, count);
        }
        return ret;
    }

    /**
     * Calculate performance rank of all symbols in watch list model between given two indices. Results are sorted low to high.
     * @param begin_index beginning quote index, earlier in time
     * @param end_index ending quote index, more recent than begin index
     * @param model the WatchListModel
     * @return array of RankElement with rank from 1..N
     */
    public static ArrayList<RankElement> calcRank(int begin_index, int end_index, WatchListModel model) {
        ArrayList<RankElement> ret = new ArrayList<>();
        ArrayList<String> members = model.getMembers();
        for (String symbol : members) {
            MarketInfo mki = model.getMarketInfo(symbol);
            if (mki == null)
                continue;//skip ones w/o mki
//TODO handle fewer elements in quotes (IPO type)
if (mki.getFund().getSize() < 380) {
System.err.println("[" + mki.getSymbol() + ":" + mki.getFund().getSize() + "] has too few quotes for calculation...");
    continue;
}
            ArrayList<FundQuote> quotes = mki.getFund().getQuote();
            FundQuote begin_quote = quotes.get(begin_index);
            FundQuote end_quote = quotes.get(end_index);
            float pct = (end_quote.getClose() - begin_quote.getClose()) / begin_quote.getClose();
            ret.add(new RankElement(symbol, pct));
        }

        //sort RankElement list from high percentage to low
        RankElement[] rank_elements = new RankElement[ret.size()];
        for (int i = 0; i < ret.size(); i++)
            rank_elements[i] = ret.get(i);
        Arrays.sort(rank_elements);
        ret = new ArrayList<>();
        int rank = rank_elements.length;
        for (int i = 0; i < rank_elements.length; i++)
            ret.add(new RankElement(rank_elements[i].symbol, rank_elements[i].pct, rank--));
        Collections.reverse(ret);
        return ret;
    }

    //for given time frame code, calculate all available segment ranks in reverse order (recent to earlier)
    public static HashMap<String, ArrayList<Integer>> calcRankMapBySegment(RankingSamplePeriod rsp, WatchListModel wlm) {
        HashMap<String, ArrayList<Integer>> ret = new HashMap<>();
        ArrayList<Integer> qidx = collectQuoteIndices(rsp, 200);//TODO replace 200
        for (int seg_idx = 0; seg_idx < qidx.size()-1; seg_idx++) {
            //for each segment, calculate rank array, add them into map per symbol
            ArrayList<RankElement> seg_rank = calcRank(qidx.get(seg_idx + 1), qidx.get(seg_idx), wlm);
            for (RankElement re : seg_rank) {
                ArrayList<Integer> hist_ranks = ret.get(re.symbol);
                if (hist_ranks == null) {//symbol doesn't exist yet, create new one
                    hist_ranks = new ArrayList<>();
                    hist_ranks.add(re.rank);
                    ret.put(re.symbol, hist_ranks);
                }
                else {//append to end
                    hist_ranks.add(re.rank);
                }
            }
        }
        return ret;
    }


    //calculate rank arrays across specified time frames up to a certain date (index of quotes)
    //calculate Friday based Segment to Segment rankings, most recent quotes first
    public static HashMap<String, ArrayList<Integer>> calcStsRankingOldToNew(WatchListModel wlm, ArrayList<Integer> fridays) {
        HashMap<String, ArrayList<Integer>> ret = new HashMap<>();
        for (int friday_idx = 1; friday_idx < fridays.size(); friday_idx++) {
            //calculate weekly ranking for each Friday
            ArrayList<RankElement> wk_rank = calcRank(fridays.get(friday_idx - 1), fridays.get(friday_idx), wlm);
            for (RankElement re : wk_rank) {
                ArrayList<Integer> hist_ranks = ret.get(re.symbol);
                if (hist_ranks == null) {//symbol doesn't exist yet, create new one
                    hist_ranks = new ArrayList<>();
                    hist_ranks.add(re.rank);
                    ret.put(re.symbol, hist_ranks);
                }
                else {//append to end
                    hist_ranks.add(re.rank);
                }
            }
        }
        return ret;
    }
    //calculate Friday based Segment to Segment rankings, fridays = from most recent to ealiest
    public static HashMap<String, ArrayList<Integer>> calcStsRankingNewToOld(WatchListModel wlm, ArrayList<Integer> fridays) {
        HashMap<String, ArrayList<Integer>> ret = new HashMap<>();
        for (int friday_idx = 0; friday_idx < fridays.size() - 1; friday_idx++) {
            //calculate weekly ranking for each Friday
            ArrayList<RankElement> wk_rank = calcRank(fridays.get(friday_idx + 1), fridays.get(friday_idx), wlm);
            for (RankElement re : wk_rank) {
                ArrayList<Integer> hist_ranks = ret.get(re.symbol);
                if (hist_ranks == null) {//symbol doesn't exist yet, create new one
                    hist_ranks = new ArrayList<>();
                    hist_ranks.add(re.rank);
                    ret.put(re.symbol, hist_ranks);
                }
                else {//append to end
                    hist_ranks.add(re.rank);
                }
            }
        }
        return ret;
    }
    //calculate ranking arrays for each symbol in the group
    public static ArrayList<Ranking> calcRankings(HashMap<String, MarketInfo> mkis, int start_index) {
        int bar_index = start_index - 1;
        ArrayList<Ranking> rankings = new ArrayList<>();

        //extract all quotes, convert into percents in array of Ranking
        Set<String> symbol_set = mkis.keySet();
        Iterator<String> itor = symbol_set.iterator();
        while(itor.hasNext()) {
            String symbol = itor.next();
            Ranking rnk = new Ranking(symbol);
            MarketInfo mki = mkis.get(symbol);
            ArrayList<FundQuote> quotes = mki.getFund().getQuote();
            if (bar_index >= quotes.size()) {
                System.err.println(symbol + " NOT included in Ranking View....");;
                continue;//skip this symbol
            }
            float origin = quotes.get(bar_index).getClose();
            for (int idx = bar_index; idx >= 0; idx--) {
                rnk.percents.add(quotes.get(idx).getClose() / origin);
                rnk.ranks.add(0);//dummy for later set()
            }
            rankings.add(rnk);
        }
        do { //traverse rankings to build up array of RankElement
            ArrayList<RankElement> res = new ArrayList<>();
            for (Ranking rnk : rankings) {
                String sym = rnk.symbol;
                Float pct = rnk.percents.get(bar_index);
                RankElement re = new RankElement(sym, pct);
                res.add(re);
            }

            //sort RankElement list from low percentage to high
            RankElement[] rank_elements = new RankElement[res.size()];
            for (int i = 0; i < res.size(); i++)
                rank_elements[i] = res.get(i);
            Arrays.sort(rank_elements);

            //traverse RankElement list, look up respective Ranking object, place rank over to Ranking at current index
            int n = 1; //number 1
            for (int i = rank_elements.length - 1; i >= 0; i--) {//since it sorts in ascending order
                //for each RankElement, find match symbol in Rankings, update ranks[bar_index]
                for (Ranking rnk : rankings) {
                    if (rank_elements[i].symbol.equals(rnk.symbol)) {
                        rnk.ranks.set(bar_index, n++);
                        break;//next RankElement should be next symbol
                    }
                }
            }

            //move bar_index backwards till 0
            bar_index--;
        }
        while (bar_index >= 0);
        return rankings;
    }

    //----------- Result analysis helpers ----------
    //calculate time in market for a given transaction, return number of days
    public static float calcTranscationDuration(Transaction trans) throws ParseException {
        String entry_date = trans.getEntryDate();
        String exit_date = trans.getExitDate();
        return calcDuration(entry_date, exit_date);
    }
    public static float calcDuration(String start_date, String end_date) throws ParseException {
        Calendar entry = stringToCalendar(start_date);
        Calendar exit = stringToCalendar(end_date);
        entry.set(Calendar.HOUR, 16);
        entry.set(Calendar.MINUTE, 0);
        entry.set(Calendar.SECOND, 0);
        exit.set(Calendar.HOUR, 16);
        exit.set(Calendar.MINUTE, 0);
        exit.set(Calendar.SECOND, 0);
        long diff = exit.getTime().getTime() - entry.getTime().getTime();
        return Math.round(diff / (double)(1000 * 60 * 60 * 24) );
    }

    //find avg of a float array
    public static float average(ArrayList<Float> data) {
        if (data.size() == 0)
            return 0;

        double sum = 0;
        for (float x : data)
            sum += x;
        return (float)sum / data.size();
    }
    //find max of a float array
    public static float max(ArrayList<Float> data) {
        if (data.size() == 0)
            return 0;

        float ret = Float.MIN_VALUE;
        if (data.size() > 0 && data.get(0) < 0)
            ret = -Float.MAX_VALUE;
        for (float x : data)
            if (x > ret)
                ret = x;
        return ret;
    }
    //find min of a float array
    public static float min(ArrayList<Float> data) {
        if (data.size() == 0)
            return 0;

        float ret = Float.MAX_VALUE;
        if (data.size() > 0 && data.get(0) < 0)
            ret = -Float.MIN_VALUE;
        for (float x : data)
            if (x < ret)
                ret = x;
        return ret;
    }
    //find median of a float array (mid point of a sorted array)
    public static float median(ArrayList<Float> data) {
        int size = data.size();
        if (size == 0)
            return 0;

        float[] floats = new float[data.size()];
        int index = 0;
        for (Float num : data)
            floats[index++] = num;
        Arrays.sort(floats);

        //for even number of points, use average of two middle points
        if (size % 2 == 0) {
            size /= 2;
            return (floats[size - 1] + floats[size]) / 2;
        }
        return floats[size / 2];
    }

    //extract year info from yahoo date string, assuming date is properly formatted
    public static int extractYear(String yahoo_date) {
        String yr_str = yahoo_date.substring(0, 4);//YAHOO format
        return Integer.parseInt(yr_str);
    }

    /**
     * Use equity log to build normalized SP500 equity time series for charting
     * @param log equity log from any system
     * @param start_date starting point's entry date, common starting point for fund and SP500
     * @return time series of matching SP500 data
     * @exception ParseException when quote data is not clean
     */
    public static TimeSeries buildSp500EquitySeries(ArrayList<Equity> log, String start_date) throws ParseException {
        if (log.size() <= 0)
            return null;

        TimeSeries ret = new TimeSeries("SP500");
        //find first data point difference, apply to all SP500 data (normalize)
        float first_sp_close = FrameworkConstants.SP500_DATA.findQuoteByDate(start_date).getClose();
        float shares = FrameworkConstants.START_CAPITAL / first_sp_close;
        //match each equity log date, calculate equivalent SP500 equity
        for (Equity eqty : log) {
//            float sp_close = 0;
            //detect no data
//            String date = AppUtil.dailyToWeekly(eqty.getDate());
//            Calendar cal = AppUtil.stringToCalendar(eqty.getDate());
//            if (!AppUtil.isDataAvailable(cal)) {//add one more day, mondy is probably holiday
//                cal.add(Calendar.DAY_OF_MONTH, 1);
//                date = AppUtil.calendarToString(cal);
//                cal = AppUtil.stringToCalendar(date);
//                if (!AppUtil.isDataAvailable(cal)) {//add one more day, mondy is probably holiday
//                    cal.add(Calendar.DAY_OF_MONTH, 1);
//                    date = AppUtil.calendarToString(cal);
//                    //todo: if date exceeds SP500 data range, exit
//                    if (!AppUtil.isDataAvailable(cal))
//                        return ret;
//                }
//            }
//if (eqty == null || eqty.getDate()==null || FrameworkConstants.SP500_DATA.findQuoteByDate(eqty.getDate())==null)
//    System.out.println();
//System.out.println("--------" + eqty.getDate());
            float sp_close = FrameworkConstants.SP500_DATA.findQuoteByDate(eqty.getDate()).getClose();
            float sp_eqty = shares * sp_close;
            Calendar cal = AppUtil.stringToCalendar(eqty.getDate());
            Day day = new Day(cal.getTime());
            ret.add(day, sp_eqty);//normalize
        }
        return ret;
    }

    /**
     * Convert annual return array to plottable time series
     * @param ars array of AnnualReturn objects
     * @return corresponding TimeSeries objects.
     */
    public static TimeSeries annualReturnToTimeSeries(ArrayList<AnnualReturn> ars) {
        TimeSeries ts = new TimeSeries(ars.get(0).getSymbol());
        for (AnnualReturn ar : ars) {
            Year yr = new Year(ar.getYear());
            ts.add(yr, ar.getPerformance() * 100);
        }
        return ts;
    }

    //split one Transaction object into multiple Transaction objects based on year
    private static ArrayList<Transaction> splitTransacton(Transaction tran, FundData fund) throws ParseException {
        ArrayList<Transaction> ret = new ArrayList<Transaction>();
        String start_date = tran.getEntryDate();
        int cur_year = stringToCalendar(start_date).get(Calendar.YEAR);
        int exit_yr = stringToCalendar(tran.getExitDate()).get(Calendar.YEAR);
        do { //adjust begin, end date to be a regular trading day, cur_year end has a lot holidays
            String end_date = adjustDateForHoliday(fund, calcLastTradingDay(cur_year));
            float exit_price = fund.findQuoteByDate(end_date).getClose();
            float entry_price = tran.getEntryPrice();//this only happens first time
            if (AppUtil.isDataAvailable(AppUtil.stringToCalendar(start_date)))
                entry_price = fund.findQuoteByDate(start_date).getClose();//multi-year transaction
            ret.add(new Transaction(tran.getSymbol(), start_date, end_date, entry_price, exit_price));
            cur_year++;
            start_date = findAndAdjustFirstMonday(fund, cur_year, 0);
        } while (cur_year != exit_yr);
        //final segament partial year
//        float eq = fund.findQuoteByDate(tran.getExitDate()).getClose();
        float eq = tran.getExitPrice();
        float bq = fund.findQuoteByDate(start_date).getClose();
        ret.add(new Transaction(tran.getSymbol(), start_date, tran.getExitDate(), bq, eq));
        return ret;
    }

    public static FundQuote getYahooQuote(String symbol, String type, int month, int day, int year) throws IOException {
        StringBuilder req_buf = new StringBuilder();
        req_buf.append(YAHOO_URL).append("?s=").append(symbol)
               .append("&a=").append(month).append("&b=").append(day).append("&c=").append(year)
               .append("&d=").append(month).append("&e=").append(day).append("&f=").append(year)
               .append("&g=").append(type).append("&ignore=.csv");
        URL url = new URL(req_buf.toString());
//System.out.println(req_buf.toString());
        URLConnection yc = url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
        in.readLine();//skip first one, it's headers
        String line = in.readLine();
//System.out.println(line);
        if (line != null)
            return parseYahooLine(symbol, line);
        else
            return null;
    }

    private static FundQuote parseYahooLine(String symbol, String line) {
        FundQuote quote = new FundQuote(symbol);//each line is one quote
        StringTokenizer st = new StringTokenizer(line, ",");
        int tok_num = 1;
        while(st.hasMoreTokens()) {
            switch(tok_num) {
                case 1: //date, covert string
                    String date = st.nextToken();
                    quote.setDate(date);
                    break;

                case 2: //open
                    quote.setOpen(Float.parseFloat(st.nextToken()));
                    break;

                case 3: //high
                    quote.setHigh(Float.parseFloat(st.nextToken()));
                    break;

                case 4: //low
                    quote.setLow(Float.parseFloat(st.nextToken()));
                    break;

                case 5: //close
                    quote.setClose(Float.parseFloat(st.nextToken()));
                    break;

                case 6: //volume
                    quote.setVolume(Float.parseFloat(st.nextToken()));
                    break;

                case 7: //adj close
                    quote.setAdjClose(Float.parseFloat(st.nextToken()));
                    break;
            }
            tok_num++;
            if (tok_num > 5)
                return quote;
        }
        return null;
    }

    //is | value1 - value2 | < pct ? (absolute difference), if true, return percentage, else return -1  NaN
    public static float deltaExceedThreshold(float value1, float value2, float pct) {
        float delta = /*100 * */Math.abs((value1 - value2) / value1);
        if (delta <= pct) return delta;
        else return -1/*Float.NaN*/;
    }


    private static final String YAHOO_URL = "http://ichart.finance.yahoo.com/table.csv";
}