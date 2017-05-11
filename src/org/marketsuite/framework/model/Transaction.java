package org.marketsuite.framework.model;

import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;

import java.text.ParseException;

//abstraction of a trade, entry, exit..etc
public class Transaction {
    public Transaction() {}

    public Transaction(String _symbol, String entry_date, String exit_date,
                       float entry_price, float exit_price) {
        symbol = _symbol;
        entryDate = entry_date;
        exitDate = exit_date;
        entryPrice = entry_price;
        exitPrice = exit_price;
        //calc performance
        try {
            performance = (exitPrice - entryPrice) / entryPrice;
            //Fidelity rule: if exit date is less than 30 days from entry date, add 0.75% fee
            if (FrameworkConstants.isFidelityFund(symbol) && AppUtil.calcDaysBetween(entryDate, exitDate) <= 30)
                performance -= 0.0075;
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public Transaction(ActiveTrade active_trade) {
        FundData fund = active_trade.getFund();
        symbol = fund.getSymbol();
        longTrade = active_trade.isLongType();
        entryDate = active_trade.getEntryDate();
        entryPrice = fund.findQuoteByDate(entryDate).getClose();
        exitDate = active_trade.getExitDate();

        //update performance
        if (exitDate != null) {
            exitPrice = fund.findQuoteByDate(exitDate).getClose();
            //calc performance when exit date not null
            try {
                if (active_trade.isLongType())
                performance = (exitPrice - entryPrice) / entryPrice;
                else
                    performance = - (exitPrice - entryPrice) / entryPrice;
                //Fidelity rule: if exit date is less than 30 days from entry date, add 0.75% fee
                if (FrameworkConstants.isFidelityFund(symbol) && AppUtil.calcDaysBetween(entryDate, exitDate) <= 30)
                    performance -= 0.0075;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        else
            active = true;//active trade still active, no exit date
    }

    public Transaction(ActiveTrade active_trade, float stop_price) {
        this(active_trade);
        performance = (stop_price - entryPrice) / entryPrice;
    }

    /**
     * is this transaction open on a particular date?
     * @param date in YAHOO format YYYY-MM-DD
     * @return true = open
     */
    public boolean isOpen(String date) {
        if (date.compareTo(entryDate) < 0) //before entry date
            return false;
        if (exitDate != null && date.compareTo(exitDate) <= 0) //between entry1 and exit inclusive
            return true;
        else if (exitDate == null && active)
            return true;
        return false;
    }

    private String symbol;
    public String getSymbol() {
        return symbol;
    }
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    private String entryDate;
    public String getEntryDate() {
        return entryDate;
    }
    public void setEntryDate(String entryDate) {
        this.entryDate = entryDate;
    }

    private float entryPrice;
    public void setEntryPrice(float entryPrice) {
        this.entryPrice = entryPrice;
    }
    public float getEntryPrice() {
        return entryPrice;
    }

    private String entry2Date;//this may be null if no entry 2 used in strategy-
    public String getEntry2Date() {
        return entry2Date;
    }
    public void setEntry2Date(String entry2Date) {
        this.entry2Date = entry2Date;
    }

    private float entry2Price;
    public void setEntry2Price(float entry2Price) {
        this.entry2Price = entry2Price;
    }
    public float getEntry2Price() {
        return entry2Price;
    }

    private String exitDate;
    public String getExitDate() {
        return exitDate;
    }
    public void setExitDate(String exitDate) {
        this.exitDate = exitDate;
    }

    private float exitPrice;
    public float getExitPrice() {
        return exitPrice;
    }
    public void setExitPrice(float exitPrice) {
        this.exitPrice = exitPrice;
    }

    private float performance;
    public float getPerformance() {
        return performance;
    }
    public void setPerformance(float perf) { performance = perf; }
    public void calcPerformance() {
        if (longTrade)
            performance = (exitPrice - entryPrice) / entryPrice;
        else //short calc backwards
            performance = (entryPrice - exitPrice) / entryPrice;
    }

    private boolean active;
    public boolean isActive() { return active; }
    public void setActive(boolean op) { active = op; }

    private boolean longTrade = true;//false = short
    public boolean isLongTrade() {
        return longTrade;
    }
    public void setLongTrade(boolean longTrade) {
        this.longTrade = longTrade;
    }
}
