package org.marketsuite.framework.model;

/**
 * Data class defining a set of standard simulation options.
 */
public class StandardOption {
    public StandardOption(String sym, boolean long_trade, boolean short_trade,
                          String start_date, String end_date, boolean adj_close) {
        symbol = sym;
        longTrade = long_trade;
        shortTrade = short_trade;
        startDate = start_date;
        endDate = end_date;
        useAdjClose = adj_close;
    }

    private String symbol;
    public String getSymbol() {
        return symbol;
    }

    private boolean longTrade;
    public boolean isLongTrade() {
        return longTrade;
    }

    private boolean shortTrade;
    public boolean isShortTrade() {
        return shortTrade;
    }

    private String startDate;
    public String getStartDate() {
        return startDate;
    }

    private String endDate;
    public String getEndDate() {
        return endDate;
    }

    private boolean useAdjClose;
    public boolean isUseAdjClose() { return useAdjClose; }
}
