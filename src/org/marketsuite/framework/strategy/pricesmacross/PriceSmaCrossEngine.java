package org.marketsuite.framework.strategy.pricesmacross;

import org.marketsuite.framework.model.ActiveTrade;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.framework.util.IndicatorUtil;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.framework.util.IndicatorUtil;

import java.util.ArrayList;

/**
  trading algorithm to simulate price cross moving average strategy with specified starting point and name of security
  buy: when closing price cross above MA
  sell: when closing price cross below MA
*/
public class PriceSmaCrossEngine extends AbstractEngine {
    //-----CTOR-----
    public PriceSmaCrossEngine(FundData fund) { _Fund = fund; }

    //-----interface implementations-----
    public void simulate(String start_date, String end_date) {}
    /**
     * Start running simulation for the fund on given start_date.  Note start_date MUST exist in data set.
     * moving averages are calculated from start_date forward in time, but backwards in index
     * (don't use data earlier than this date). Both start_date and end_date MUST exist in data file.
     */
    public boolean simulate() throws IllegalArgumentException {
        //compute price array from quotes
        float[] price = new float[_Fund.getSize()];
        for (int i = _Fund.getSize() - 1; i >= 0; i--)
            price[i] = _Fund.getQuote().get(i).getClose();
        //compute all 3 moving averages, store in array
        int start_index = _Fund.findIndexByDate(simParam.getStdOptions().getStartDate());
        int end_index = _Fund.findIndexByDate(simParam.getStdOptions().getEndDate());
        if (start_index == -1 || end_index == -1)
            throw new IllegalArgumentException(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_msg_1"));

        //check if too few bars between start and end
        if ( (start_index - end_index) < _nMaPeriod)
            throw new IllegalArgumentException(_Fund.getSymbol() +
                FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_msg_5") +
                FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_msg_6") + getStrategy() + ")" );

        //retrieve MA period
        _nMaPeriod = simParam.getMacOption().getEntryMA1();

        //if starting point does not have enough lookback, advance starting point to there is enough
        int len = _Fund.getQuote().size();
        if (len <= start_index + _nMaPeriod) {
            int delta = _nMaPeriod - (len - 1 - start_index);
            start_index = start_index - delta;
        }
        actualStartDate = _Fund.getDate(start_index);
        _MA = IndicatorUtil.calcSMA(_nMaPeriod, start_index, _Fund);
        _EntryCrossing = calcCrossing(price, _MA, start_index, end_index);
        _ExitCrossing = calcCrossing(price, _MA, start_index, end_index);
//        logCrossing(start_index, end_index);//for logging
        executeTrades(start_index, end_index);
//        logTransactions();//for logging
        return true;
    }
    public String getId() { return "Price / SMA Crossing"; }
    public String getStrategy() {
        return getId();
    }
    public String getStrategyInfo() {
        return
            "(" + actualStartDate + ") "
            + simParam.getMacOption().getEntryMA1() + " / ";
    }
    public boolean isBuySetup() { return false; }
    public boolean isBuyTrigger() { return false; }
    public String getBuyTriggerDate() { return null; }
    public boolean isSellSetup() { return false; }
    public boolean isSellTrigger() { return false; }
    public String getSellTriggerDate() { return null; }

    //calculate cross over between two data series (eg. short vs medium), two series must have the same length
    //  from specified starting point
    // return values: CROSSING_ABOVE for ma1 cross above ma2; CROSSING_BELOW for ma1 cross below ma2
    protected int[] calcCrossing(float[] ma1, float[] ma2, int start_index, int end_index) {
        if (ma1.length != ma2.length || ma1.length <= start_index || ma2.length <= start_index)
            throw new IllegalArgumentException(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_err_1"));
        int[] ret = new int[ma1.length];
        int loop_index = start_index + 1;

        //skip comparison for 0's due to max bars back
        boolean all_zeros = true;
        while (loop_index >= end_index) {
            if (ma1[loop_index] == 0 || ma2[loop_index] == 0) {
                loop_index--;//skipping 0s
                continue;
            }
            else {
                all_zeros = false;
                break;
            }
        }
        if (all_zeros)
            return ret;

        //with valid data, start comparison
        boolean ma1_smaller = ma1[loop_index] < ma2[loop_index];
        ret[loop_index--] = ma1_smaller ? CROSSING_NONE : CROSSING_ABOVE;//fake start if MA1 > MA2 to start
        while (loop_index >= end_index) {//data used up
            if (ma1_smaller) {//look for crossing above
                if (ma1[loop_index] >= ma2[loop_index]) {
                    ret[loop_index] = CROSSING_ABOVE;
                    ma1_smaller = false;
                }
            }
            else {//look for crossing below
                if (ma1[loop_index] < ma2[loop_index]) {
                    ret[loop_index] = CROSSING_BELOW;
                    ma1_smaller = true;
                }
            }
            loop_index--;
        }
        return ret;
    }

    /**
     * trade in/out by MAC rule: (BUY)Short MA cross above Medium MA (SELL)Medium MA cross below Long MA
     * inputs: _MA, _EntryMA2, _ExitMA1, _ExitMA2; start_index = first data point to start comparison
     * output: _Transactions
     * @param start_index start sim point
     * @param end_index end sim point
     */
    protected void executeTrades(int start_index, int end_index) {
        _Transactions = new ArrayList<>();
        ActiveTrade trade = new ActiveTrade();
        int loop_index = start_index;

        //determine trade options
        String type = LONG_ONLY;
        boolean long_option = simParam.getStdOptions().isLongTrade();
        boolean short_option = simParam.getStdOptions().isShortTrade();
        if (long_option && short_option) type = LONG_SHORT;
        else if (short_option) type = SHORT_ONLY;

        //loop from end of quotes to beginning (YAHOO format)
        while (loop_index >= end_index) {
            String trade_date = _Fund.getDate(loop_index);
            float trade_price = _Fund.getAdjustedClose(loop_index);

            //trade is NOT OPEN, look for ENTRY
            if (!trade.isOpen()) {//look to buy
                if (_EntryCrossing[loop_index] == CROSSING_ABOVE) {
                    if (type.equals(LONG_ONLY) || type.equals(LONG_SHORT)) {
                        trade.buy(_Fund, trade_date, 0.05f);
                        logTrade("Buy", trade_date, trade_price);
                    }
                }
                if (_ExitCrossing[loop_index] == CROSSING_BELOW) {//short rule
                    if (type.equals(SHORT_ONLY) || type.equals(LONG_SHORT)) {
                        trade.sellShort(_Fund, trade_date, 0f, false);
                        logTrade("Short", trade_date, trade_price);
                    }
                }
            }

            //trade is OPEN, look for EXIT
            else {
                //split decision making based on current trade type
                //long trade: look for EXIT (120MA cross below 200MA)
                if (trade.isLongType()) {
                    if (type.equals(LONG_ONLY)) {
                        if (_ExitCrossing[loop_index] == CROSSING_BELOW) {
                            trade.sell(trade_date);
                            _Transactions.add(new Transaction(trade));
                            logTrade("Sell", trade_date, trade_price);
                        }
                        //premature exit 50MA cross below 120MA, bear market todo possibly a short side too
//                        else if (_EntryCrossing[loop_index] == CROSSING_BELOW
//                                 && _MA[loop_index] < _ExitMA2[loop_index]) {
//                            trade.sell(trade_date);
//                            _Transactions.add(new Transaction(trade));
//                            logTrade("Sell", trade_date, trade_price);
//                        }
                    }
//                    else if (type.equals(SHORT_ONLY)) {} //not possible to be here
//                    else {//both long and short
//                        if (_ExitCrossing[loop_index] == CROSSING_BELOW) {
//                            trade.sell(trade_date);
//                            _Transactions.add(new Transaction(trade));
//                            logTrade("Sell", trade_date, trade_price);
//                            trade.sellShort(_Fund, trade_date, 0f, false);
//                            logTrade("Short", trade_date, trade_price);
//                        }
//                        //premature exit 50MA cross below 120MA, bear market todo possibly a short side too
//                        else if (_EntryCrossing[loop_index] == CROSSING_BELOW
//                                 && _MA[loop_index] < _ExitMA2[loop_index]) {
//                            trade.sell(trade_date);
//                            _Transactions.add(new Transaction(trade));
//                            logTrade("Sell", trade_date, trade_price);
//                        }
//                    }
                }

                //short trade: look for EXIT (50MA cross above 120MA)
                else {
                    if (type.equals(LONG_ONLY)) {} //not possible to be here
                    else if (type.equals(SHORT_ONLY)) {
                        if (_EntryCrossing[loop_index] == CROSSING_ABOVE) {
                            trade.coverShort(trade_date, trade_price);
                            _Transactions.add(new Transaction(trade));
                            logTrade("Cover", trade_date, trade_price);
                        }
                    }
                    else {//both long and short
                        if (_EntryCrossing[loop_index] == CROSSING_ABOVE) {
                            trade.coverShort(trade_date, trade_price);
                            _Transactions.add(new Transaction(trade));
                            logTrade("Cover", trade_date, trade_price);
                            trade.buy(_Fund, trade_date, 0.05f);
                            logTrade("Buy", trade_date, trade_price);
                        }
                    }
                }
            }
            loop_index--;
        }

        //pick up last open trade if any, make up a pseudo transaction for up to date
        String final_date = _Fund.getDate(end_index);
        if (trade.isOpen()) {
            //close this trade with most recent price at index 0
            trade.sell(final_date);
            _Transactions.add(new Transaction(trade));

            //if trade's start date is the most recent fund date, then set entry signal
            entrySignal = trade.getEntryDate().equals(final_date);
        }
        else {//if trade's end date is the most recent fund date, then set exit signal
            String ed = trade.getExitDate();
            if (ed != null)
                exitSignal = ed.equals(final_date);
        }
    }

    //for debugging
    //helper to print some parts of a data series
    private void logSeries(float[] series1, float[] series2, int start_index, int end_index) {
//todo        if (!Main.logging)
//            return;

        for (int index= start_index; index >= end_index; index--) {
            System.out.println(
                "<" + _Fund.getDate(index) + "> [" + index + "] "
                + FrameworkConstants.PRICE_FORMAT.format(series1[index]) + "\t"
                + FrameworkConstants.PRICE_FORMAT.format(series2[index]) + "\t"
                + FrameworkConstants.PRICE_FORMAT.format(_Fund.getAdjustedClose(index))
            );
        }
    }
    private void logCrossing(int start_index, int end_index) {
//todo        if (!Main.logging)
//            return;
//
        StringBuilder buf = new StringBuilder();
        buf.append("---------- MAC Cross Over List ------------\n");
        for (int index=start_index; index>=end_index; index--) {
            if (_EntryCrossing[index] == CROSSING_ABOVE)
                buf.append("Short MA Crossing Above Medium MA "
                        + _Fund.getDate(index) + " index = " + index + "\n");
        }
        System.out.println();
        for (int index=start_index; index>=0; index--) {
            if (_ExitCrossing[index] == CROSSING_BELOW)
                buf.append("Medium MA Crossing Below Long MA "
                        + _Fund.getDate(index) + " index = " + index + "\n");
        }
    }
    private void logTransactions() {
//todo        if (!Main.logging)
//            return;

        StringBuilder buf = new StringBuilder();
        buf.append("---------- MAC Transactions ------------\n");
        for (Transaction tr : _Transactions) {
            String roi = FrameworkConstants.ROI_FORMAT.format(tr.getPerformance());
            buf.append(tr.getSymbol() + "  " + tr.getEntryDate()
                    + "(" + FrameworkConstants.PRICE_FORMAT.format(tr.getEntryPrice()) + ")\t" + tr.getExitDate()
                    + "(" + FrameworkConstants.PRICE_FORMAT.format(tr.getExitPrice()) + ")\t"
                    + roi + "\n");
        }
        System.out.println(buf);
    }
    private void logTrade(String type, String date, float price) {
//todo        if (!Main.logging)
//            return;

//        StringBuilder buf = new StringBuilder();
//        buf.append("Trade: ").append(type).append("\t").append(date).append(" | ").append(price);
//        System.out.println(buf);
    }

    //-----instance variables and assessors-----
    private FundData _Fund;
    public void setFund(FundData fund) { _Fund = fund; }

    //three types of moving averages
    private float[] _MA; //use literal
//    public float[] getEntryMA1() { return _MA; }
//    private float[] _EntryMA2;
//    public float[] getEntryMA2() { return _EntryMA2; }
//    private float[] _ExitMA1;
//    public float[] getExitMA1() { return _ExitMA1; }
//    private float[] _ExitMA2;
//    public float[] getExitMA2() { return _ExitMA2; }
    //list of short to medium cross-overs and medium to long cross-overs
    private int[] _EntryCrossing;//array of Short MA crossing Medium MA
    private int[] _ExitCrossing;//array of Medium MA crossing Long MA

    //----- literals -----
    public static int _nMaPeriod = 50;
//    public static int _nEntryMA2 = 120;
//    public static int _nExitMA1 = 120;
//    public static  int _nExitMA2 = 200;
    private final static int CROSSING_NONE = 0;
    private final static int CROSSING_ABOVE = 1;
    private final static int CROSSING_BELOW = -1;
    private final static String LONG_ONLY = "LONG_ONLY";
    private final static String SHORT_ONLY = "SHORT_ONLY";
    private final static String LONG_SHORT = "LONG_SHORT";
}