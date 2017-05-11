package org.marketsuite.framework.model;

import org.marketsuite.framework.util.AppUtil;

import java.util.Calendar;

//data object for calculating price velocity
public class RateInfo {
    public RateInfo(FundQuote end_quote, FundQuote start_quote, int interval) {
        symbol = end_quote.getSymbol();
        startDate = AppUtil.stringToCalendarNoEx(start_quote.getDate());
        endDate = AppUtil.stringToCalendarNoEx(end_quote.getDate());
        ratePerDay = 100 * (end_quote.getClose() - start_quote.getClose()) / interval; //in cents per day
    }

    public String getSymbol() { return symbol; }
    public float getRatePerDay() { return ratePerDay; }
    public Calendar getStartDate() { return startDate; }
    public Calendar getEndDate() { return endDate; }

    //----- variables -----
    private String symbol;
    private float ratePerDay;
    private Calendar startDate;
    private Calendar endDate;
}
