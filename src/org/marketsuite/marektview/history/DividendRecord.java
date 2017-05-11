package org.marketsuite.marektview.history;

import java.util.Calendar;

public class DividendRecord {
    public DividendRecord(String symbol, Calendar cal, float dividend) {
        this.symbol = symbol;
        this.cal = cal;
        this.dividend = dividend;
    }

    //----- accessors -----
    public String getSymbol() { return symbol; }
    public Calendar getCal() { return cal; }
    public float getDividend() { return dividend; }

    //----- variables -----
    private String symbol;
    private Calendar cal;
    private float dividend;
}
