package org.marketsuite.marektview.history;

import java.util.Calendar;

public class HistoricalQuote {
    public HistoricalQuote(Calendar _cal, float _quote) { cal = _cal; price = _quote; }
    public HistoricalQuote(HistoricalQuote copy) { price = copy.price; cal = copy.cal; }
    public HistoricalQuote(Calendar cal, float price, float roi) {
        this.price = price;
        this.cal = cal;
        this.roi = roi;
    }

    //----- accessors -----
    public Calendar getCalendar() { return cal; }
    public void setCalendar(Calendar cal) { this.cal = cal; }
    public float getPrice() { return price; }
    public void setPrice(float quote) { this.price = quote; }
    public float getRoi() { return roi; }
    public void setRoi(float roi) { this.roi = roi; }

    //----- variables -----
    private float price;
    private Calendar cal;
    private float roi;//from end of last month
}
//TODO expand this to include dividend, T-Bill rate...., dividend yield