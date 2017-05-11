package org.marketsuite.framework.model.quote;

import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.resource.ApolloConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

//encapsulate monthly quote related functions and data
public class MonthlyQuote {
    //CTOR: create object for a symbol of specified number of bars from the historical daily quotes.
    public MonthlyQuote(String symbol, int num_days) {
        try {//can't read daily quote
            _DailyQuotes = DataUtil.readHistory(symbol, num_days);
        } catch (IOException e) {
            LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("wc_01") + " " + symbol, LoggingSource.MONTHLY_CHART);
            return;
        }
        if (num_days > _DailyQuotes.getSize()) num_days = _DailyQuotes.getSize();
        try {//can't read split-info, continue
            DataUtil.adjustForSplits(_DailyQuotes.getQuote(), num_days - 1);
        } catch (IOException e) {//ok not having split info
//            LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("wc_03") + " " + symbol, LoggingSource.MONTHLY_CHART);
        }
        if (num_days > _DailyQuotes.getSize()) num_days = _DailyQuotes.getSize();
        else num_days = _DailyQuotes.getSize();

        //extract last trading day of each month into an array of quotes in the same order (newer to older) as daily quotes
        String today = _DailyQuotes.getQuote().get(0).getDate();
        Calendar today_cal = AppUtil.stringToCalendarNoEx(today);

        //first quote - most recent partial month
        FundQuote prev_quote = new FundQuote(symbol);
        prev_quote.setDate(_DailyQuotes.getDate(0));
        prev_quote.setClose(_DailyQuotes.getQuote().get(0).getClose());
        quotes.add(prev_quote);
        int prev_index = 0;//ending index of previous month
        int prev_month = today_cal.get(Calendar.MONTH);
        for (int cur_index = 1; cur_index < num_days; cur_index++) {
            FundQuote daily_quote = _DailyQuotes.getQuote().get(cur_index);
            Calendar cal = AppUtil.stringToCalendarNoEx(daily_quote.getDate());
            int cur_month = cal.get(Calendar.MONTH);
            if (cur_month != prev_month) {//detect change in month, end of month
                updateMonthQuote(prev_quote, cur_index - 1, prev_index);
                prev_quote = new FundQuote(symbol);
                prev_quote.setDate(_DailyQuotes.getDate(cur_index));
                prev_quote.setClose(_DailyQuotes.getQuote().get(cur_index).getClose());
                quotes.add(prev_quote);
                prev_month = cur_month;
                prev_index = cur_index;
            }
        }
    }

    //----- private methods -----
    //update high, low, open, volume of previous month
    //begin_index = beginning index of month (higher value)
    //end_index = end of older month in daily quote array  (lower value)
    //month_quote = target to update open, high, low, volume
    private void updateMonthQuote(FundQuote month_quote, int begin_index, int end_index) {
        ArrayList<FundQuote> daily_quote = _DailyQuotes.getQuote();
        month_quote.setOpen(daily_quote.get(begin_index).getOpen());
        float high = -1, low = Float.MAX_VALUE, volume = 0;
        for (int idx = end_index; idx <= begin_index; idx++) {
            if (daily_quote.get(idx).getHigh() > high) high = daily_quote.get(idx).getHigh();
            if (daily_quote.get(idx).getLow() < low) low = daily_quote.get(idx).getLow();
            volume += daily_quote.get(idx).getVolume();
        }
        month_quote.setHigh(high);
        month_quote.setLow(low);
        month_quote.setVolume(volume);
    }

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

    //----- accessors -----
    public ArrayList<FundQuote> getQuotes() { return quotes; }
    public int getSize() { return quotes.size(); }
    public float getClose(int index) { return quotes.get(index).getClose(); }
    public String getDate(int index) { return quotes.get(index).getDate(); }

    //----- variables -----
    private ArrayList<FundQuote> quotes = new ArrayList<>();
    private FundData _DailyQuotes;
}
