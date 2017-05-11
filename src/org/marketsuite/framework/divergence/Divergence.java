package org.marketsuite.framework.divergence;

import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.indicator.Indicator;
import org.marketsuite.framework.model.indicator.MACD;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.indicator.Indicator;
import org.marketsuite.framework.model.indicator.MACD;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * A do-it-all helper to manage divergence for any symbol.
 */
public class Divergence {
    //----- CTOR -----
    public Divergence(String _symbol, Indicator indicator) throws IOException {
        symbol = _symbol;
        startSearchDate = fund.getDate(fund.getSize() - 1);//last one earliest in time (Yahoo format)
        _Indicator = indicator;
        fund = _Indicator.getQuotes();
//        if (fund == null)//this indicator does not use quotes
//            fund = DataUtil.readFundHistory(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, symbol);//cache this
    }

    //----- public methods -----
    public void findDivergence() throws ParseException {
//TODO: for all intervals, for now: use first one
        int start_index = fund.getSize() - 1;
        Calendar cal = AppUtil.stringToCalendar(startSearchDate);
        cal.add(Calendar.YEAR, interval);
        String end_date = AppUtil.calendarToString(cal);
        int end_index = fund.findIndexByDate(end_date);

        //find highest 2 closes within the first interval
        int h1_idx = start_index, h2_idx = start_index;
        for (int srch_idx = start_index; srch_idx <= end_index; srch_idx++) {
            float h1_close = fund.getPrice(h1_idx);
            float h2_close = fund.getPrice(h2_idx);
            float cur_close = fund.getPrice(srch_idx);
            if (cur_close > h1_close)
                h1_idx = srch_idx;
            else if (cur_close > h2_close)
                h2_idx = srch_idx;
        }

        //check slope between price and indicator for the two indices
        boolean price_up = fund.getPrice(h1_idx) < fund.getPrice(h2_idx); //slope upward
        float[] indicator_values = _Indicator.getValues();
        boolean indicator_down = indicator_values[h1_idx] > indicator_values[h2_idx];
        if (price_up && !indicator_down) {//found divergence on top
            DatePair dp = new DatePair(fund.getDate(h1_idx), fund.getDate(h2_idx));
            bearishDivergence.add(dp);
        }

        //find lowest 2 closes within the first interval

    }

    //----- inner classes -----
    private class DatePair {
        private DatePair(String start_date, String end_date) {
            startDate = start_date;
            endDate = end_date;
        }
        private String startDate;
        private String endDate;
    }

    //----- variables, accessors -----
    private String symbol;
    private String indicatorId;
    private FundData fund;
    private Indicator _Indicator;
    private ArrayList<DatePair> bullishDivergence, bearishDivergence;
    private int interval = DEFAULT_INTERVAL_LENGTH; //unit = # of months
    private String startSearchDate; //begining of all search intervals
    private int minDivergencePeriod = DEFAULT_MIN_DIVERGENCE_PERIOD;
    private int maxDivergencePeriod = DEFAULT_MAX_DIVERGENCE_PERIOD;

    //----- literals -----
    private static int DEFAULT_INTERVAL_LENGTH = 12; //number of months
    private static final int DEFAULT_MIN_DIVERGENCE_PERIOD = 14; // 2 weeks
    private static final int DEFAULT_MAX_DIVERGENCE_PERIOD = 90; // 3 months


    //----- unit testing -----
    public static void main(String[] args) {
        try {
            MACD macd_spy = new MACD("SPY");
            Divergence div_spy = new Divergence("SPY", macd_spy);
            div_spy.findDivergence();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
