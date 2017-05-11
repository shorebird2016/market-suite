package org.marketsuite.framework.model.quote;

import org.marketsuite.component.util.ObjectCloner;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.data.IbdRating;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.model.FundData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

//encapsulate weekly quote related functions and data
public class WeeklyQuote {
//TODO Another CTOR uses number of decimal years
    //CTOR: true weekly quote based on "real end of week" which may or may not be Fridays, from a particular index in the past for given daily quote array
    //  The "real" end of week can be any day of the week w/o specifics
    public WeeklyQuote(FundData fund, int start_index) {
        String symbol = fund.getSymbol();
        if (start_index > fund.getSize())
            start_index = fund.getSize() - 1;//can't exceed end of array
        ArrayList<FundQuote> daily_quotes = fund.getQuote();
        int prev_day_idx = start_index, prev_eow_idx = prev_day_idx;//keep track prev day, prev end of week index
        int bow_idx = prev_day_idx;
        Calendar prev_cal = AppUtil.stringToCalendarNoEx(daily_quotes.get(prev_day_idx).getDate());
        int prev_dow = prev_cal.get(Calendar.DAY_OF_WEEK);
        for (int idx = start_index - 1; idx >= 0; idx--) {
            Calendar cur_cal = AppUtil.stringToCalendarNoEx(daily_quotes.get(idx).getDate());
            int cur_dow = cur_cal.get(Calendar.DAY_OF_WEEK);
            if (cur_dow < prev_dow) {//indicates week change over, user prev index, record transition
                FundQuote prev_quote = daily_quotes.get(prev_day_idx); //use its date/close

                //use last stored weekly quote to get open, then calculate high,low,vol in between
                if (quotes.size() == 0) quotes.add(daily_quotes.get(idx));//nothing yet, just store this transition
                FundQuote wq = new FundQuote(symbol);
                wq.setClose(prev_quote.getClose()); wq.setDate(prev_quote.getDate());
                bow_idx = prev_eow_idx - 1;//begin of current week index
                wq.setOpen(daily_quotes.get(bow_idx).getOpen());

                //calc H, L, V between bow_idx and prev_day_idx
                float high = 0, low = Float.MAX_VALUE, v = 0;
                for (int qidx = bow_idx; qidx >= prev_day_idx; qidx--) {
                    FundQuote q = daily_quotes.get(qidx);
                    if (q.getHigh() > high) high = q.getHigh();
                    if (q.getLow() < low) low = q.getLow();
                    v += q.getVolume();
                }
                wq.setHigh(high); wq.setLow(low); wq.setVolume(v);
                quotes.add(wq); prev_eow_idx = prev_day_idx;
            }

            //update previous index, cal, dow for next round of comparison
            prev_day_idx = idx;
            prev_cal = AppUtil.stringToCalendarNoEx(daily_quotes.get(prev_day_idx).getDate());
            prev_dow = prev_cal.get(Calendar.DAY_OF_WEEK);
        }

        //handle most recent partial week after last transition
        bow_idx = prev_eow_idx - 1;
        float open = daily_quotes.get(bow_idx).getOpen();
        float high = 0, low = Float.MAX_VALUE, v = 0;
        for (int idx = bow_idx; idx >=0; idx--) {
            FundQuote q = daily_quotes.get(idx);
            if (q.getHigh() > high) high = q.getHigh();
            if (q.getLow() < low) low = q.getLow();
            v += q.getVolume();
        }
        FundQuote last_wq = new FundQuote(symbol, open, high, low, daily_quotes.get(0).getClose());
        last_wq.setVolume(v); last_wq.setDate(daily_quotes.get(0).getDate());
        quotes.add(last_wq);//always add most recent day, maybe partial week
        Collections.reverse(quotes);//still maintain older dates have larger indices
    }

    //start_index = further-est back in time, end_index = closest to present; start_index > end_index
    // index = from daily quote, eg 5 weeks, use 25 (5 x 5)
    public WeeklyQuote(FundData fund, int start_index, int end_index) {
        ArrayList<FundQuote> daily_quotes = fund.getQuote();

        //look up day of week from start_index, find next nearest Monday
        String start_date = daily_quotes.get(start_index).getDate();
        Calendar start_cal = AppUtil.stringToCalendarNoEx(start_date);
        int dow = start_cal.get(Calendar.DAY_OF_WEEK);
        if (dow != Calendar.MONDAY) {
            do {
                start_cal.add(Calendar.DAY_OF_WEEK, 1);
                dow = start_cal.get(Calendar.DAY_OF_WEEK);
            }while (dow != Calendar.MONDAY);
        }

        //start_cal now is Monday, create Monday-Friday segments for all "full weeks"
        //loop thru all Monday-Friday segments, calculate weekly High, Low, Open, Close, Volume
        Calendar cal = (Calendar)ObjectCloner.copy(start_cal);
        Calendar recent_cal = AppUtil.stringToCalendarNoEx(daily_quotes.get(end_index).getDate());
        do {
            FundQuote wk_quote = calcWeeklyQuote(fund, cal);
            quotes.add(wk_quote);//results added to quotes array
            cal.add(Calendar.DAY_OF_YEAR, 3);//advance to next Monday, internally already set to Friday
        } while(cal.compareTo(recent_cal) <= 0);
        Collections.reverse(quotes);//still maintain older dates have larger indices
    }

    //lookup from an array of weekly quotes for matching date, date = YAHOO format, return -1 = not found
    public static int findIndexByDate(String date, WeeklyQuote wq) {
        ArrayList<FundQuote> quotes = wq.getQuotes();
        for (int i = 0; i < quotes.size(); i++)
            if (date.equals(quotes.get(i).getDate())) {
                return i;
            }
        return -1;
    }

    //does price hookup during the last N weeks?
    public static boolean doesCloseHook(WeeklyQuote weekly_quote, int num_weeks, Calendar date, boolean hook_up) {
        //find matching date
        for (int idx = 0; idx < weekly_quote.getSize(); idx++) {//from more current to older days
            Calendar quote_date = AppUtil.stringToCalendarNoEx(weekly_quote.getQuotes().get(idx).getDate());
            if (date.compareTo(quote_date) >= 0) {//found date
                //if less than N data points available, can't decide
                if ( (idx + num_weeks + 1) >= weekly_quote.getSize() ) return false;

                //idx = point to look back N bars (num_weeks + 1)
                return doesPriceHook(weekly_quote.getQuotes(), idx, hook_up);
            }
        }
        return false;//not found
    }

    //----- private methods -----
    //give a calendar Monday and FundData, convert daily quotes into one weekly quote
    private FundQuote calcWeeklyQuote(FundData fund, Calendar cal) {
        FundQuote[] wkq = new FundQuote[5];
        for (int wkq_idx = 0; wkq_idx < 5; wkq_idx++) {
            String dt = AppUtil.calendarToString(cal);
            FundQuote quote = fund.findQuoteByDate(dt);
            if (quote != null)
                wkq[wkq_idx] = quote;
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        float open = -1, high = -1, low = 1000000, close = -1, volume = 0;
        for (int wkq_idx = 0; wkq_idx < 5; wkq_idx++) {
            FundQuote dow_quote = wkq[wkq_idx];
            if (dow_quote != null) {
                if (open == -1) open = dow_quote.getOpen(); //very first one
                if (dow_quote.getHigh() > high) high = dow_quote.getHigh(); //every higher high
                if (dow_quote.getLow() < low) low = dow_quote.getLow();//every lower low
                close = dow_quote.getClose(); //the last day of 5 prevails
                volume += dow_quote.getVolume();//sum up
            }
        }
        FundQuote q = new FundQuote(fund.getSymbol(), open, high, low, close);
        cal.add(Calendar.DAY_OF_YEAR, -1);
        q.setDate(AppUtil.calendarToString(cal));
        return q;
    }
    private static boolean doesPriceHook(ArrayList<FundQuote> quotes, int index, boolean hook_up) {
        if (hook_up) {
            float q0 = quotes.get(index).getClose();
            float q1 = quotes.get(index + 1).getClose();
            float q2 = quotes.get(index + 2).getClose();
            return (q0 > q1 && q1 < q2);
        }
        else {
            float q0 = quotes.get(index).getClose();
            float q1 = quotes.get(index + 1).getClose();
            float q2 = quotes.get(index + 2).getClose();
            return (q0 < q1 && q1 > q2);
        }
    }
    public static boolean doesPriceHookup(ArrayList<FundQuote> quotes, Calendar start_cal) {
        for (int index = 0; index < quotes.size(); index++) {//find matching date as starting point
            if (quotes.get(index).getDate().equals(AppUtil.calendarToString(start_cal)))
                return isPriceUp(quotes, index) && !isPriceUp(quotes, index + 1);
        }
        return false;
    }
    public static boolean doesPriceHookdown(ArrayList<FundQuote> quotes, Calendar start_cal) {
        for (int index = 0; index < quotes.size(); index++) {//find matching date as starting point
            if (quotes.get(index).getDate().equals(AppUtil.calendarToString(start_cal)))
                return isPriceDown(quotes, index) && !isPriceDown(quotes, index + 1);
        }
        return false;
    }
    public static boolean isPriceUp(ArrayList<FundQuote> quotes, int start_index) {
        float q0 = quotes.get(start_index).getClose();
        float q1 = quotes.get(start_index + 1).getClose();
        return q0 > q1;
    }
    public static boolean isPriceDown(ArrayList<FundQuote> ratings, int start_index) {
        float q0 = ratings.get(start_index).getClose();
        float q1 = ratings.get(start_index + 1).getClose();
        return q0 < q1;
    }
    //use rating dates to find matching quotes, store in the return array, with same index
//    public static ArrayList<FundQuote> matchDates(ArrayList<FundQuote> quotes, ArrayList<IbdRating> ratings) {
//        ArrayList<FundQuote> ret = new ArrayList<>();
//        for (int i=0; i<3; i++) {
//            Calendar date = ratings.get(i).getDate();
//            for (FundQuote q : quotes) {
//                if (q.getDate().equals(date)) {
//                    ret.add(q);
//                    break;
//                }
//            }
//        }
//        return ret;
//    }

    //----- accessors -----
    public ArrayList<FundQuote> getQuotes() { return quotes; }
    public float[] getCloses() {
        float[] ret = new float[getSize()];
        for (int i = 0; i < ret.length - 1 ; i++)
            ret[i] = quotes.get(i).getClose();
        return ret;
    }
    public int getSize() { return quotes.size(); }

    //----- variables -----
    private ArrayList<FundQuote> quotes = new ArrayList<>();

}
