package org.marketsuite.simulator.indicator.macd;

import org.marketsuite.framework.model.ActiveTrade;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.framework.util.IndicatorUtil;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.strategy.base.AbstractEngine;

import java.util.ArrayList;

/**
 * Algorithm to process MACD zero crossing strategy
 */
public class MacdZeroCrossEngine extends AbstractEngine {
    public MacdZeroCrossEngine(FundData fund) { _Fund = fund; }
    public boolean simulate() {
        //compute start end end index
        int start_index = _Fund.findIndexByDate(simParam.getStdOptions().getStartDate());
        int end_index = _Fund.findIndexByDate(simParam.getStdOptions().getEndDate());
        if (start_index == -1 || end_index == -1)
            throw new IllegalArgumentException(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_msg_1"));

        _nFastParam = simParam.getMzcOption().getFastMA();
        _nSlowParam = simParam.getMzcOption().getSlowMA();

        //if starting point does not have enough lookback, advance starting point to there is enough
        int size = _Fund.getQuote().size();
        if (size <= start_index + _nSlowParam) {
            int delta = _nSlowParam - (size - 1 - start_index);
            start_index = start_index - delta;
        }

        //check if too few bars between start and end
        if ( (start_index - end_index) < _nSlowParam)
            throw new IllegalArgumentException(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mzc_msg_1"));

        //calculate MACD
        actualStartDate = _Fund.getDate(start_index);
        _MACD = new double[size];
        float[] fast_ema = IndicatorUtil.calcEMA(_nFastParam, start_index, end_index, _Fund);
        float[] slow_ema = IndicatorUtil.calcEMA(_nSlowParam, start_index, end_index, _Fund);
        for (int index = 0; index < size; index++)
            _MACD[index] = fast_ema[index] - slow_ema[index];

        calcZeroCrossing();
        executeTrades(start_index, end_index);//result in _Transactions
        return false;
    }
    public void simulate(String start_date, String end_date) { }

    //not used
    public String getId() { return FrameworkConstants.FRAMEWORK_BUNDLE.getString("advsim_lbl_3"); }
    public String getStrategy() { return getId(); }
    public String getStrategyInfo() {
        return "(" + actualStartDate + ") "+ _nFastParam + " / " + _nSlowParam;
    }
    public boolean isBuySetup() { return false; }
    public boolean isBuyTrigger() { return false; }
    public String getBuyTriggerDate() { return null; }
    public boolean isSellSetup() { return false; }
    public boolean isSellTrigger() { return false; }
    public String getSellTriggerDate() { return null; }

    //-----private methods-----
    //generate array of index with zero cross above or zero cross below for _MACD array, result in _ZeroCross
    private void calcZeroCrossing() {
        int size = _Fund.getSize();
        _ZeroCross = new double[size];
        double prev_macd = _MACD[size - 1];
        for (int index = size - 2; index >= 0; index--) {
            double cur_macd = _MACD[index];
            if (cur_macd > 0 && prev_macd <= 0) //from 0 to >0 in the very beginning counts as fake buy
                _ZeroCross[index] = CROSSING_ABOVE;
            else if (cur_macd < 0 && prev_macd > 0)
                _ZeroCross[index] = CROSSING_BELOW;
            else
                _ZeroCross[index] = CROSSING_NONE;
            prev_macd = cur_macd;
        }
    }

    //use cross over data to produce transactions
    protected void executeTrades(int start_index, int end_index) {
        _Transactions = new ArrayList<Transaction>();
        ActiveTrade trade = new ActiveTrade();
        int loop_index = start_index;//_MACD.length - 1;
        while (loop_index >= end_index) {
            if (!trade.isOpen()) {//look to buy
                if (_ZeroCross[loop_index] == CROSSING_ABOVE)
                    trade.buy(_Fund, _Fund.getDate(loop_index), 0.05f);
            }
            else {//look to sell
                if (_ZeroCross[loop_index] == CROSSING_BELOW) {
                    trade.sell(_Fund.getDate(loop_index));
                    _Transactions.add(new Transaction(trade));
                }
            }
            loop_index--;
        }
        //pick up last open trade if any, make up a pseudo transaction for up to date, so called "FAKE SELL"
        if (trade.isOpen()) {
            //close this trade with most recent price at index 0
            trade.sell(_Fund.getDate(end_index));
            _Transactions.add(new Transaction(trade));
        }
    }

    //-----instance variables and assessors-----
    private double[] _MACD, _ZeroCross;
    private int _nFastParam = FAST_MA;
    private int _nSlowParam = SLOW_MA;

    //-----literals-----
    private static final int CROSSING_NONE = 0;
    private static final int CROSSING_ABOVE = 1;
    private static final int CROSSING_BELOW = -1;
    public  static final int FAST_MA = 10;
    public  static final int SLOW_MA = 50;
}
