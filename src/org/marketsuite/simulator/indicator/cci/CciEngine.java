package org.marketsuite.simulator.indicator.cci;

import org.marketsuite.framework.model.*;
import org.marketsuite.framework.model.indicator.CCI;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.resource.ApolloConstants;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class CciEngine extends AbstractEngine {
    public CciEngine(FundData fund) {
        _Fund = fund;
        ArrayList<FundQuote> quotes = _Fund.getQuote();
        try {
            DataUtil.adjustForSplits(quotes, quotes.size() - 1);
        } catch (IOException e1) { //ok without split file, make a note
            LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("cdl_spl") + " "
                    + quotes.get(0).getSymbol(), LoggingSource.SIMULATOR_CCI);
        }
    }
    public void simulate(String start_date, String end_date) {
        _sStartDate = start_date; _sEndDate = end_date;
        simulate();
    }
    public boolean simulate() { //zero crossing simulation, always run under a thread
        _Transactions = new ArrayList<>();
        float[] cci = new CCI(_nPeriod, _Fund.getQuote()).getCci();
        int start_index = FundQuote.findIndexByDate(_Fund.getQuote(), _sStartDate);
        int end_index = FundQuote.findIndexByDate(_Fund.getQuote(), _sEndDate);
        ActiveTrade trade = new ActiveTrade();
        for (int idx = start_index; idx > end_index; idx--) {
            float cci1 = cci[idx];
            float cci2 = cci[idx - 1];
            final String trade_date = _Fund.getDate(idx - 1);
            if (cci1 < 0 && cci2 > 0) {//buy
                trade.buy(_Fund, trade_date, 0.05F);
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        progBar.setLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("osc_cci_buy") + " " + trade_date); }
                });
            }
            else if (trade.isOpen() && cci1 > 0 && cci2 < 0) {//sell
                trade.sell(trade_date);
                _Transactions.add(new Transaction(trade));
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        progBar.setLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("osc_cci_sell") + " " + trade_date); }
                });
            }
        }

        //pick up last open trade if any, make up a pseudo transaction for up to date
        String final_date = _Fund.getDate(end_index);
        if (trade.isOpen()) {
            //close this trade with most recent price at index 0
            trade.sell(final_date);
            _Transactions.add(new Transaction(trade));
        }
        return true;
    }
    public boolean isBuySetup() { return false; }
    public boolean isBuyTrigger() { return false; }
    public String getBuyTriggerDate() { return null; }
    public boolean isSellSetup() { return false; }
    public boolean isSellTrigger() { return false; }
    public String getSellTriggerDate() { return null; }
    public String getId() { return "CCI"; }
    public String getStrategy() { return null; }
    public String getStrategyInfo() { return null; }

    //----- public methods -----
    public void setPeriod(int _period) { _nPeriod = _period; }

    //----- variables -----
    private int _nPeriod = 20;
    private String _sStartDate, _sEndDate;
}