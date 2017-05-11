package org.marketsuite.simulator.indicator.stochastic;

import org.marketsuite.framework.model.ActiveTrade;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.indicator.IndicatorData;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.framework.util.IndicatorUtil;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.model.indicator.IndicatorData;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.framework.util.IndicatorUtil;

import java.util.ArrayList;

/**
 * Algorithm to process RSI zero crossing strategy
 */
public class StochasticEngine extends AbstractEngine {
    public StochasticEngine(FundData fund) { _Fund = fund; }
    public boolean simulate() {
        //compute start end end index
        int start_index = _Fund.findIndexByDate(simParam.getStdOptions().getStartDate());
        int end_index = _Fund.findIndexByDate(simParam.getStdOptions().getEndDate());
        if (start_index == -1 || end_index == -1)
            throw new IllegalArgumentException(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_msg_1"));

        //calculate Stochastic
        _nLength = simParam.getStochasticOption().getLength();

        //if starting point does not have enough lookback, advance starting point to there is enough
        int len = _Fund.getQuote().size();
        if (len <= start_index + _nLength) {
            int delta = _nLength - (len - 1 - start_index);
            start_index = start_index - delta;
        }

        //check if too few bars between start and end
        if ( (start_index - end_index) < _nLength)
            throw new IllegalArgumentException(FrameworkConstants.FRAMEWORK_BUNDLE.getString("sto_msg_1"));

        actualStartDate = _Fund.getDate(start_index);
        _nMaPeriod = simParam.getStochasticOption().getMaPeriod();
        _fOversold = simParam.getStochasticOption().getOversold();
        _fOverbought = simParam.getStochasticOption().getOverbought();
        _Stochastic = IndicatorUtil.calcStochastic(_Fund, _nLength, _nMaPeriod, start_index, end_index);
        if (_Stochastic.length == 0)
            return false;
        executeTrades(start_index, end_index);//result in _Transactions
        return true;
    }
    public void simulate(String start_date, String end_date) { }

    //not used
    public String getId() { return FrameworkConstants.FRAMEWORK_BUNDLE.getString("advsim_lbl_5"); }
    public String getStrategy() { return getId(); }
    public String getStrategyInfo() {
        return
            "(" + actualStartDate + ") "
            + _nLength + " / " + _nMaPeriod + " / " + _fOversold + " / " + _fOverbought;
    }
    public boolean isBuySetup() { return false; }
    public boolean isBuyTrigger() { return false; }
    public String getBuyTriggerDate() { return null; }
    public boolean isSellSetup() { return false; }
    public boolean isSellTrigger() { return false; }
    public String getSellTriggerDate() { return null; }

    //-----private methods-----
//todo add short logic
    //buy when RSI value cross above oversold level, sell cross below overbought level
    protected void executeTrades(int start_index, int end_index) {
        _Transactions = new ArrayList<Transaction>();
        ActiveTrade trade = new ActiveTrade();

        //some early data points are empty, find first valid one as starting point
        int loop_index = start_index;//_Stochastic.length - 1;
        while (_Stochastic[loop_index] == null) {
            loop_index--;
            if (loop_index == 0) {
//todo show error about no data, not likely...
                return;
            }
        }

        float prev = _Stochastic[loop_index--].getParam1();
        while (loop_index >= end_index) {
            float cur = _Stochastic[loop_index].getParam1();
            if (!trade.isOpen()) {//look to buy
                //if prev less than oversold and current is >= oversold
                if (prev < _fOversold && cur >= _fOversold)
                    trade.buy(_Fund, _Fund.getDate(loop_index), 0.05f);
            }
            else {//look to sell
                if (prev > _fOverbought && cur <= _fOverbought) {
                    trade.sell(_Fund.getDate(loop_index));
                    _Transactions.add(new Transaction(trade));
                }
            }
            prev = cur;
            loop_index--;
        }
        //pick up last open trade if any, make up a pseudo transaction for up to date
        if (trade.isOpen()) {
            //close this trade with most recent price at index 0
            trade.sell(_Fund.getDate(end_index));
            _Transactions.add(new Transaction(trade));
        }
    }

    //-----instance variables and assessors-----
    private int _nLength;
    private int _nMaPeriod;
    private double _fOversold;
    private double _fOverbought;
    private IndicatorData[] _Stochastic;

    //-----literals-----
    public static final int DEFAULT_LENGTH = 14;
    public static final int DEFAULT_AVG_PERIOD = 3;
    public static final int DEFAULT_OVERSOLD = 20;
    public static final int DEFAULT_OVERBOUGHT = 80;
}
