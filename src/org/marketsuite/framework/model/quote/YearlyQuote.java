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

//simple class to model after yearly quote
public class YearlyQuote {
    //----- CTOR -----  create object for a symbol of specified number of bars from the historical daily quotes.
    public YearlyQuote(String symbol, int num_days) {
        try {
            _DailyQuotes = DataUtil.readHistory(symbol, num_days);
        } catch (IOException e) {//can't read daily quote
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

        //extract last trading day of each year into an array of quotes in the same order (newer to older) as daily quotes
        String today = _DailyQuotes.getQuote().get(0).getDate();
        Calendar today_cal = AppUtil.stringToCalendarNoEx(today);

        //first quote - most recent partial year
        FundQuote prev_quote = new FundQuote(symbol);
        prev_quote.setDate(_DailyQuotes.getDate(0));
        prev_quote.setClose(_DailyQuotes.getQuote().get(0).getClose());
        quotes.add(prev_quote);
        int prev_index = 0;//ending index of previous month
        int prev_year = today_cal.get(Calendar.YEAR);

        //traverse all records from new to old, find out year end position
        for (int cur_index = 1; cur_index < num_days; cur_index++) {
            FundQuote daily_quote = _DailyQuotes.getQuote().get(cur_index);
            Calendar cal = AppUtil.stringToCalendarNoEx(daily_quote.getDate());
            int cur_year = cal.get(Calendar.YEAR);
            if (cur_year != prev_year) {//detect change in year, end of year
                updateQuoteRecord(prev_quote, cur_index - 1, prev_index);
                prev_quote = new FundQuote(symbol);
                prev_quote.setDate(_DailyQuotes.getDate(cur_index));
                prev_quote.setClose(_DailyQuotes.getQuote().get(cur_index).getUnAdjclose());
                quotes.add(prev_quote);
                prev_year = cur_year;
                prev_index = cur_index;
            }
        }
    }

    //public methods
    public FundQuote findRecord(int year) {
        for (FundQuote q : quotes) {
            int yr = AppUtil.stringToCalendarNoEx(q.getDate()).get(Calendar.YEAR);
            if (year == yr) return q;
        }
        return null;
    }
    //----- private methods -----
    //update high, low, open, volume of previous month
    //begin_index = beginning index of month (higher value)
    //end_index = end of older month in daily quote array  (lower value)
    //yearly_quote = target to update open, high, low, volume
    private void updateQuoteRecord(FundQuote yearly_quote, int begin_index, int end_index) {
        ArrayList<FundQuote> daily_quote = _DailyQuotes.getQuote();
        yearly_quote.setOpen(daily_quote.get(begin_index).getOpen());
        float high = -1, low = Float.MAX_VALUE, volume = 0;
        for (int idx = end_index; idx <= begin_index; idx++) {
            if (daily_quote.get(idx).getHigh() > high) high = daily_quote.get(idx).getHigh();
            if (daily_quote.get(idx).getLow() < low) low = daily_quote.get(idx).getLow();
            volume += daily_quote.get(idx).getVolume();
        }
        yearly_quote.setHigh(high);
        yearly_quote.setLow(low);
        yearly_quote.setVolume(volume);
    }

    //----- accessors -----
    public ArrayList<FundQuote> getQuotes() { return quotes; }
    public int getSize() { return quotes.size(); }
    public float getClose(int index) { return quotes.get(index).getClose(); }
    public String getDate(int index) { return quotes.get(index).getDate(); }

    //----- variables -----
    private ArrayList<FundQuote> quotes = new ArrayList<>();//list of yearly quotes
    private FundData _DailyQuotes;
}
