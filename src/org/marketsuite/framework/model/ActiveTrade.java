package org.marketsuite.framework.model;

//util an open trade
public class ActiveTrade {
    public void buy(FundData fd, String entry_date, float stop_percent) {//eg. 0.05 ==> 5%
        longType = true;
        fund = fd;
        entryDate = entry_date;
        entryPrice = fd.findQuoteByDate(entryDate).getClose();
        open = true;
        stop = entryPrice * (1 - stop_percent);
    }
    public void buy(FundData fd, String entry_date, boolean use_unadj_close) {
        if (use_unadj_close) {
            longType = true;
            fund = fd;
            entryDate = entry_date;
            entryPrice = fd.findQuoteByDate(entryDate).getUnAdjclose();
            open = true;
            return;
        }
        buy(fd, entry_date, 0.05F);
    }

    public void buy(FundData fd, String entry_date, float entry_price, boolean use_price) {
        buy(fd, entry_date, 0);
        entryPrice = entry_price;
        useEntryExitPrice = use_price;
    }

    public void sell(String exit_date) {
        open = false;
        exitDate = exit_date;
    }

    public void sell(String exit_date, float exit_price) {
        sell(exit_date);
        exitPrice = exit_price;
    }

    public void sellShort(FundData fd, String entry_date, Float entry_price, boolean use_price) {
        longType = false;
        fund = fd;
        entryDate = entry_date;
        open = true;
        entryPrice = entry_price;
        useEntryExitPrice = use_price;
    }

    public void coverShort(String exit_date, float exit_price) {
        open = false;
        exitDate = exit_date;
        exitPrice = exit_price;
    }

    private boolean longType;//true = long, false = short
    public boolean isLongType() {
        return longType;
    }

    private boolean open;//true = trade open
    public boolean isOpen() { return open; }

    private FundData fund;
    public FundData getFund() { return fund; }

    private String entryDate;
    public String getEntryDate() { return entryDate; }

    private float entryPrice;
    public float getEntryPrice() { return entryPrice; }
    public void setEntryPrice(float price) { entryPrice = price; }

    private String exitDate;
    public String getExitDate() { return exitDate; }

    private float exitPrice;
    public float getExitPrice() { return exitPrice; }
    public void setExitPrice(float price) { exitPrice = price; }

    private boolean useEntryExitPrice;
    public boolean isUseEntryExitPrice() { return useEntryExitPrice; }

    private float stop;
    public float getStop() { return stop; }
    public void setStop(float _stop) { stop = _stop; }

    private String strategy;
    public String getStrategy() { return strategy; }
    public void setStrategy(String str) { strategy = str; }
}
