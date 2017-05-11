package org.marketsuite.simulator.indicator.ichimoku;

import org.marketsuite.framework.model.ActiveTrade;
import org.marketsuite.framework.model.indicator.Ichimoku;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.model.type.Strategy;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.model.type.Strategy;
import org.marketsuite.framework.strategy.base.AbstractEngine;

import java.io.IOException;
import java.util.ArrayList;

//simulator for Ichimoku Kumo breakout strategy
public class KumoBreakEngine extends AbstractEngine {
    public KumoBreakEngine(FundData fund) throws IOException {
        _Fund = fund;
        ArrayList<FundQuote> quotes = _Fund.getQuote(); DataUtil.adjustForSplits(quotes, quotes.size() - 1);
//            LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("cdl_spl") + " "
//                    + quotes.get(0).getSymbol(), LoggingSource.SIMULATOR_CCI);
    }

    public void simulate(String start_date, String end_date) {
        _sStartDate = start_date; _sEndDate = end_date;
        simulate();
    }
    public boolean simulate() { //zero crossing simulation, always run under a thread
        _Transactions = new ArrayList<>();
        int start_index = _Fund.getSize() - 1;
        int end_index = 0;
        Ichimoku ich = new Ichimoku(_Fund.getQuote(), start_index, end_index);
        ActiveTrade trade = new ActiveTrade();
        for (int idx = start_index; idx > end_index; idx--) {
//            float cci1 = cci[idx];
//            float cci2 = cci[idx - 1];
//            final String trade_date = _Fund.getDate(idx - 1);
//            if (cci1 < 0 && cci2 > 0) {//buy
//                trade.buy(_Fund, trade_date, 0.05F);
//                EventQueue.invokeLater(new Runnable() {
//                    public void run() {
//                        progBar.setLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("osc_cci_buy") + " " + trade_date);
//                    }
//                });
//            }
//            else if (trade.isOpen() && cci1 > 0 && cci2 < 0) {//sell
//                trade.sell(trade_date);
//                _Transactions.add(new Transaction(trade));
//                EventQueue.invokeLater(new Runnable() {
//                    public void run() {
//                        progBar.setLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("osc_cci_sell") + " " + trade_date); }
//                });
//            }
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
    public String getId() { return Strategy.ICHMOKU_KUMO_BREAK.name(); }
    public String getStrategy() { return null; }
    public String getStrategyInfo() { return null; }

    private String _sStartDate, _sEndDate;
}
