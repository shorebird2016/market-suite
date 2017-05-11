package org.marketsuite.framework.model;

import org.marketsuite.framework.util.AppUtil;

import java.util.ArrayList;
import java.util.Calendar;

//abstraction of a select fund data, with array of dates and prices
public class FundData {
    public FundData(String _symbol) {
        symbol = _symbol;
    }

    //find quote object with given date, null = not found
    public FundQuote findQuoteByDate(String date) {
        for (FundQuote fq : _Quote) {
            if (date.equals(fq.getDate()))
                return fq;
        }
        return null;
    }

    //find index of quote data for a given date, if not found return -1
    public int findIndexByDate(String date) {
        for (int i=0; i<_Quote.size(); i++) {
            if (_Quote.get(i).getDate().equals(date))
                return i;
        }
        return -1;
    }

    //find date of a particular index
    public Calendar findDateByIndex(int index) {
        return AppUtil.stringToCalendarNoEx(_Quote.get(index).getDate());
    }

    //calculate performance between two dates, begin_date is earlier than end_date
    public float calcPerformance(String begin_date, String end_date) {
        FundQuote begin_quote = findQuoteByDate(begin_date);
        FundQuote end_quote = findQuoteByDate(end_date);
        performance = 100 * (end_quote.getClose() - begin_quote.getClose()) / begin_quote.getClose();
        return performance;
    }

    //calculate performance from a give date(end_date) back N weeks
    public float calcPerformance(String end_date, int num_week) {
        try {
            FundQuote end_quote = findQuoteByDate(end_date);
            int begin_index = end_quote.getIndex() + num_week;
            FundQuote begin_quote = _Quote.get(begin_index);
            return calcPerformance(begin_quote.getDate(), end_quote.getDate());
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private String symbol;
    public String getSymbol() {
        return symbol;
    }

    public String getDate(int index) { return _Quote.get(index).getDate(); }

    public float getPrice(int index) { return _Quote.get(index).getClose(); }

//todo separate adj close away from close
    public float getAdjustedClose(int index) { return _Quote.get(index).getClose(); }

    public int getSize() { return _Quote.size(); }

    private float performance;//convenient storage
    public void setPerformance(float performance) {
        this.performance = performance;
    }
    public float getPerformance() {
        return performance;
    }

    private ArrayList<FundQuote> _Quote = new ArrayList<>();
    public ArrayList<FundQuote> getQuote() {
        return _Quote;
    }
    public void addQuote(FundQuote quote) {
        _Quote.add(quote);
    }
}