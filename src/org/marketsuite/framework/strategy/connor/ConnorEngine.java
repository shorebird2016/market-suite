package org.marketsuite.framework.strategy.connor;

import org.marketsuite.framework.model.ActiveTrade;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.indicator.IndicatorData;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.framework.strategy.base.SimParam;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.framework.util.IndicatorUtil;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.strategy.base.AbstractEngine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Implementation of Larry Connor's strategy algorithms from his book "High Probability ETF Trading"
 */
public class ConnorEngine extends AbstractEngine {//todo: handle reverse data set................................
    /**
     * invoked by view class to perform simulation of Connor method
     * @param start_date starting date inclusive with YAHOO format eg. 03-14-1984
     * @param end_date ending date inclusive with YAHOO format, null = most recent monday
     */
    public void simulate(String start_date, String end_date) {
        //if transaction is already produced, don't do it again
        if (_TransactionHash.get(FrameworkConstants.LIST_CONNOR_STRATEGY[_nStrategy]).size() > 0) {
            //reset this such that report will work
            _Transactions = _TransactionHash.get(FrameworkConstants.LIST_CONNOR_STRATEGY[_nStrategy]);
            return;
        }

        StringBuilder log_buf = new StringBuilder();

        //pre-calc SMA5 and SMA200 since many strategies use it
        int len = _Fund.getSize();
        _SMA5 = IndicatorUtil.calcSMA(5, len - 10, _Fund);
        _SMA200 = IndicatorUtil.calcSMA(200, len - 210, _Fund);
        _RSI = IndicatorUtil.calcRSI(_Fund, len, 0, 2);
        _RSI1 = IndicatorUtil.calcFirstRSI(_Fund, len - 10, 4);//used in RSI25/75 todo: remove........
        ArrayList<Transaction> trans = new ArrayList<Transaction>();
        int end_index = _Fund.findIndexByDate(end_date);//make sure end date exist
        if (end_index < 0)
            end_index = 0;
        //outer loop calculating every possible trade, store into trans
        ArrayList<FundQuote> quotes = _Fund.getQuote();
        int data_index = quotes.size() - 200;//for 200SMA
        while (data_index >= end_index) {//older data last
            if (entry1Triggering(data_index)) {
                _ActiveTrade1 = new ActiveTrade();
                _ActiveTrade1.buy(_Fund, quotes.get(data_index).getDate(), 0);
                _ActiveTrade1.setStrategy(FrameworkConstants.LIST_CONNOR_STRATEGY[_nStrategy]);
                data_index--;
                //inner loop
                while (data_index >= end_index) {//outcome 1: no data
                    if (exitTriggering(data_index)) {//outcome 2: sell one or both, exit inner loop
                        String date = quotes.get(data_index).getDate();
                        _ActiveTrade1.sell(date);
                        trans.add(new Transaction(_ActiveTrade1));
                        _ActiveTrade1 = null;
                        if (_ActiveTrade2 != null && _ActiveTrade2.isOpen()) {
                            _ActiveTrade2.sell(date);
                            Transaction tr = trans.get(trans.size() - 1);
                            mergeTades(tr, _ActiveTrade2);
                            _ActiveTrade2 = null;
                        }
                        data_index--;
                        break;//to outer loop
                    }
                    else {
                        if (entry2Triggering(data_index)) {//outcome 3: buy 2nd position
                            _ActiveTrade2 = new ActiveTrade();
                            String date = quotes.get(data_index).getDate();
                            _ActiveTrade2.buy(_Fund, date, 0);
                            _ActiveTrade2.setStrategy(FrameworkConstants.LIST_CONNOR_STRATEGY[_nStrategy]);
                        }
                    }//outcome 4: do nothing
                    data_index--;
                }
            }
            else //outcome 4: do nothing
                data_index--;
        }
        //if active trades are open, close
        if (_ActiveTrade1 != null && _ActiveTrade1.isOpen()) {
            if (data_index < 0)
                data_index = 0;//use last data point
            Transaction tr = new Transaction(_ActiveTrade1);
            trans.add(tr);//don't record sell in active trade allows transactions to record open
            log_buf.append("[" + quotes.get(data_index).getDate() + "] Final Sell #1 $"
                + quotes.get(data_index).getClose() + "\n");
            if (_ActiveTrade2 != null && _ActiveTrade2.isOpen()) {
                mergeTades(tr, _ActiveTrade2);
                _ActiveTrade2 = null;
                log_buf.append("[" + quotes.get(data_index).getDate() + "] Final Sell #2 $"
                    + quotes.get(data_index).getClose() + "\n");
            }
        }
        _TransactionHash.put(FrameworkConstants.LIST_CONNOR_STRATEGY[_nStrategy], trans);
        _Transactions = trans;//still need this for report to work
        System.out.println(log_buf);
    }

    public boolean simulate() { return false; }

    public boolean isLong(String cur_date) {
        return false;
    }

    public SimParam getParams() {
        return null;
    }
    public void setSimParam(SimParam param) {
    }
    public String getId() { return "Connor"; }
    public String getStrategy() { return "Connor"; } //todo....more detail
    public String getStrategyInfo() {
        return getId() + "";
    }
    public boolean isBuySetup() { return false; }
    public boolean isBuyTrigger() { return false; }
    public String getBuyTriggerDate() { return null; }
    public boolean isSellSetup() { return false; }
    public boolean isSellTrigger() { return false; }
    public String getSellTriggerDate() { return null; }

    /**
     * Find out what is the status of each strategy for a given date.  This assumes runSimulation() was called prior.
     * @param strategy the strategy in question
     * @return string representation of status(defined in Constants.LIST_CONNOR_STATUS)
     */
//    public String getStrategyStatus(String strategy, String date) {
//        _nStrategy = Constants.strategyStringToIndex(strategy);
//        ArrayList<Transaction> trans = _TransactionHash.get(strategy);//engine.getEngine(strategy);
//        for (Transaction tr : trans) {
//            if (date.equals(tr.getEntryDate()))
//                return Constants.LIST_CONNOR_STATUS[Constants.CONNOR_STATUS_ENTRY1];//entry 1 this day
//            else if (date.equals(tr.getEntry2Date()))
//                return Constants.LIST_CONNOR_STATUS[Constants.CONNOR_STATUS_ENTRY2];//entry 2 this day
//            else if (date.equals(tr.getExitDate()))
//                return Constants.LIST_CONNOR_STATUS[Constants.CONNOR_STATUS_EXIT];//exit
//            else if (tr.isOpen(date))
//                return Constants.LIST_CONNOR_STATUS[Constants.CONNOR_STATUS_OPEN];//special case, only happens for TODAY
//        }
//        //not an open trade, determine whether setup condition is ready
//        if (isEntrySetupReady(date))
//            return Constants.LIST_CONNOR_STATUS[Constants.CONNOR_STATUS_READY];//setup condition met
//        return Constants.LIST_CONNOR_STATUS[Constants.CONNOR_STATUS_CASH];
//    }

    public void setStrategy(String strategy) {
        _sStrategy = strategy;
        _nStrategy = FrameworkConstants.strategyStringToIndex(_sStrategy);
    }

    public void setAggressive(boolean aggr) { _bAggressiveOption = aggr; }

    /**
     * Load fresh fund data from file, clearGraph transaction hash, re-calculate SMA, RSI for engine
     * @param symbol newly selected ETF
     * @throws java.io.IOException fail to read fund file
     */
    public void loadFund(String symbol) throws IOException {
        _Fund = DataUtil.readFundHistory(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, symbol);
        for (int index = 0; index < FrameworkConstants.LIST_CONNOR_STRATEGY.length; index++)
            _TransactionHash.put(FrameworkConstants.LIST_CONNOR_STRATEGY[index], new ArrayList<Transaction>());//dummy
        int len = _Fund.getSize();
        _SMA5 = IndicatorUtil.calcSMA(5, len - 10, _Fund);
        _SMA200 = IndicatorUtil.calcSMA(200, len - 210, _Fund);
        _RSI = IndicatorUtil.calcRSI(_Fund, len, 0, 2);
        _RSI1 = IndicatorUtil.calcFirstRSI(_Fund, len - 10, 4);//used in RSI25/75 todo: remove........
    }

    //is entry setup condition met on this date?
    public boolean isEntrySetupReady(String date) {
        int data_index = _Fund.findIndexByDate(date);
        if (data_index == -1)
            return false;//non-trading days

        StringBuilder log_buf = new StringBuilder();

        ArrayList<FundQuote> quotes = _Fund.getQuote();
        float close = quotes.get(data_index).getClose();
        switch (_nStrategy) {
            case FrameworkConstants.CONNOR_STRATEGY_3DAY_CHANNEL_LONG:
                float day2h = quotes.get(data_index + 2).getHigh();
                float day2l = quotes.get(data_index + 2).getLow();
                float day1h = quotes.get(data_index + 1).getHigh();
                float day1l = quotes.get(data_index + 1).getLow();
                float day0h = quotes.get(data_index).getHigh();
                float day0l = quotes.get(data_index).getLow();
                //is channel formed?
                boolean ret = (day2h > day1h && day2l > day1l)
                        && (day1h > day0h && day1l > day0l);
                if (ret)
                    log_buf.append("[" + quotes.get(data_index).getDate() + "] Long Setup Ready $" + close +"\n");
                return ret;

            case FrameworkConstants.CONNOR_STRATEGY_3DAY_CHANNEL_SHORT:
                //check last 3 days channel validity
                day2h = quotes.get(data_index + 2).getHigh();
                day2l = quotes.get(data_index + 2).getLow();
                day1h = quotes.get(data_index + 1).getHigh();
                day1l = quotes.get(data_index + 1).getLow();
                day0h = quotes.get(data_index).getHigh();
                day0l = quotes.get(data_index).getLow();
                ret =(day2h < day1h && day2l < day1l)
                        && (day1h < day0h && day1l < day0l);
                if (ret)
                    log_buf.append(
                        "[" + quotes.get(data_index).getDate() + "] Short Setup Ready $" + close +"\n");
                return ret;

            case FrameworkConstants.CONNOR_STRATEGY_RSI_25_LONG:
                _RSI1 = IndicatorUtil.calcRSI(_Fund, data_index, 4, _RSI1);
                ret = _RSI1.getParam1() < 35;//start watching level
                if (ret)
                    log_buf.append(
                        "[" + quotes.get(data_index).getDate() + "] Long Setup Ready $" + close +
                        "\tRSI = " + _RSI1.getParam1() + "\n");
                return ret;

            case FrameworkConstants.CONNOR_STRATEGY_RSI_75_SHORT:
                _RSI1 = IndicatorUtil.calcRSI(_Fund, data_index, 4, _RSI1);
                ret = _RSI1.getParam1() > 65;
                if (ret)
                    log_buf.append(
                        "[" + quotes.get(data_index).getDate() + "] Short Setup Ready $" + close +
                        "\tRSI = " + _RSI1.getParam1() + "\n");
                return ret;

            case FrameworkConstants.CONNOR_STRATEGY_PERCENT_B_LONG://todo.....................
            case FrameworkConstants.CONNOR_STRATEGY_PERCENT_B_SHORT:
                return false;

            case FrameworkConstants.CONNOR_STRATEGY_R3_LONG:
                if (_RSI[data_index].getParam1() >= 60) //skip 60 above
                    return false;

                //at any data point, compare 2 RSI values, detect downward trend
                float rsi1 = _RSI[data_index + 1].getParam1();//two days ago
                float rsi2 = _RSI[data_index].getParam1();//two days ago
                //RSI drops 2 days in a row, then buy
                ret = (rsi1 > rsi2);
                if (ret)
                    log_buf.append(
                        "[" + quotes.get(data_index).getDate() + "] Long Setup Ready $" + close
                        + " \tRSI 1,2: " + _RSI[data_index + 1].getParam1()
                        + ", " + _RSI[data_index].getParam1() + "\n");
                return ret;

            case FrameworkConstants.CONNOR_STRATEGY_R3_SHORT://todo....................
                return false;

            case FrameworkConstants.CONNOR_STRATEGY_MUL_DAY_DOWN_LONG://3 out of 4 days down
                int down_count = 0;
                for (int chk_idx = data_index + 3; chk_idx >= data_index; chk_idx--) {
                    float c1 = quotes.get(chk_idx + 1).getClose();
                    float c2 = quotes.get(chk_idx).getClose();
                    if (c1 > c2)
                        down_count++;
                }
                ret = (down_count >= 3); //must have 3 or more
                if (ret)
                    log_buf.append(
                        "[" + quotes.get(data_index).getDate() + "] Long Setup Ready $" + close
                        + "  Down Count = " + down_count +"\n");
                return ret;

            case FrameworkConstants.CONNOR_STRATEGY_MUL_DAY_UP_SHORT://todo....................
                return false;

            case FrameworkConstants.CONNOR_STRATEGY_RSI_10_6_LONG:
                ret = _RSI[data_index].getParam1() < 20;
                if (ret) //RSI less than 20
                    log_buf.append(
                        "[" + quotes.get(data_index).getDate() + "] Long Setup Ready $" + close
                        + "  RSI = " + _RSI[data_index].getParam1() +"\n");
                return ret;

            case FrameworkConstants.CONNOR_STRATEGY_RSI_94_90_SHORT:
                ret = _RSI[data_index].getParam1() > 80;
                if (ret) //RSI greater than 80
                    log_buf.append(
                        "[" + quotes.get(data_index).getDate() + "] Short Setup Ready $" + close
                        + "  RSI = " + _RSI[data_index].getParam1() +"\n");
                return ret;
        }
        System.out.println(log_buf);
        return false;
    }

    //is entry 1 condition met ?  separate path for each strategy
    private boolean entry1Triggering(int data_index) {
        StringBuilder log_buf = new StringBuilder();
        ArrayList<FundQuote> quotes = _Fund.getQuote();
        float close = quotes.get(data_index).getClose();
        switch (_nStrategy) {
            case FrameworkConstants.CONNOR_STRATEGY_3DAY_CHANNEL_LONG:
                if (close >= _SMA5[data_index] || close <= _SMA200[data_index])
                    return false;

                //check last 4 days channel validity
                float day3h = quotes.get(data_index + 3).getHigh();
                float day3l = quotes.get(data_index + 3).getLow();
                float day2h = quotes.get(data_index + 2).getHigh();
                float day2l = quotes.get(data_index + 2).getLow();
                float day1h = quotes.get(data_index + 1).getHigh();
                float day1l = quotes.get(data_index + 1).getLow();
                float day0h = quotes.get(data_index).getHigh();
                float day0l = quotes.get(data_index).getLow();
                //buy near end of this bar if this condition is met
                boolean ret = (day3h > day2h && day3l > day2l)
                        && (day2h > day1h && day2l > day1l)
                        && (day1h > day0h && day1l > day0l);
                if (ret)
                    log_buf.append(
                        "[" + quotes.get(data_index).getDate() + "] Long #1 $" + close
                        + " \t5/200MA: " + _SMA5[data_index] + " | " + _SMA200[data_index] +"\n");
                return ret;

            case FrameworkConstants.CONNOR_STRATEGY_3DAY_CHANNEL_SHORT:
                if (close <= _SMA5[data_index] || close >= _SMA200[data_index])
                    return false;

                //check last 4 days channel validity
                day3h = quotes.get(data_index + 3).getHigh();
                day3l = quotes.get(data_index + 3).getLow();
                day2h = quotes.get(data_index + 2).getHigh();
                day2l = quotes.get(data_index + 2).getLow();
                day1h = quotes.get(data_index + 1).getHigh();
                day1l = quotes.get(data_index + 1).getLow();
                day0h = quotes.get(data_index).getHigh();
                day0l = quotes.get(data_index).getLow();
                //buy near end of this bar if this condition is met
                ret = (day3h < day2h && day3l < day2l)
                        && (day2h < day1h && day2l < day1l)
                        && (day1h < day0h && day1l < day0l);
                if (ret)
                    log_buf.append(
                        "[" + quotes.get(data_index).getDate() + "] Short #1 $" + close
                        + "\t5/200MA: " + _SMA5[data_index] + " | " + _SMA200[data_index] +"\n");
                return ret;

            case FrameworkConstants.CONNOR_STRATEGY_RSI_25_LONG://4-RSI less than 25 and close > 200MA
                if (close < _SMA200[data_index])
                    return false;
                _RSI1 = IndicatorUtil.calcRSI(_Fund, data_index, 4, _RSI1);
                ret = _RSI1.getParam1() < 25;
                if (ret)
                    log_buf.append(
                        "[" + quotes.get(data_index).getDate() + "] Long #1 $" + close +
                        "\tRSI = " + _RSI1.getParam1() + "\n");
                return ret;

            case FrameworkConstants.CONNOR_STRATEGY_RSI_75_SHORT:
                if (close > _SMA200[data_index])
                    return false;
                _RSI1 = IndicatorUtil.calcRSI(_Fund, data_index, 4, _RSI1);
                ret = _RSI1.getParam1() > 75;
                if (ret)
                    log_buf.append(
                        "[" + quotes.get(data_index).getDate() + "] Short #1 $" + close +
                        "\tRSI = " + _RSI1.getParam1() + "\n");
                return ret;

            case FrameworkConstants.CONNOR_STRATEGY_PERCENT_B_LONG://todo: ..................
                return false;

            case FrameworkConstants.CONNOR_STRATEGY_PERCENT_B_SHORT://todo: ..................
                return false;

            case FrameworkConstants.CONNOR_STRATEGY_R3_LONG:
                if (close < _SMA200[data_index] || _RSI[data_index].getParam1() >= 60) //skip 60 above, less 200MA
                    return false;

                //at any data point, compare 3 RSI values, detect downward trend and < 10
                float rsi1 = _RSI[data_index + 2].getParam1();//two days ago
                float rsi2 = _RSI[data_index + 1].getParam1();//two days ago
                float rsi3 = _RSI[data_index].getParam1();//two days ago
                //RSI drops 3 days in a row, then buy
                ret = (rsi1 > rsi2 && rsi2 > rsi3 && rsi3 < 10);
                if (ret)
                    log_buf.append(
                        "[" + quotes.get(data_index).getDate() + "] Long #1 $" + close
                        + " \tRSI 1,2,3: " + _RSI[data_index + 2].getParam1()
                        + ", " + _RSI[data_index + 1].getParam1()
                        + ", " + _RSI[data_index].getParam1() +"\n");
                return ret;

            case FrameworkConstants.CONNOR_STRATEGY_R3_SHORT://todo:.............
                if (close > _SMA200[data_index] || _RSI[data_index].getParam1() <= 60) //skip 60 above, less 200MA
                    return false;

            case FrameworkConstants.CONNOR_STRATEGY_MUL_DAY_DOWN_LONG:
                if (close >= _SMA5[data_index] || close <= _SMA200[data_index])
                    return false;

                //check last 5 days for lower close
                //loop last 6 days close, assume all down, if more than 1 up, skip data_index by 1
                int down_count = 0;
                for (int chk_idx = data_index + 4; chk_idx >= data_index; chk_idx--) {
                    float c1 = quotes.get(chk_idx + 1).getClose();
                    float c2 = quotes.get(chk_idx).getClose();
                    if (c1 > c2)
                        down_count++;
                }
                ret = (down_count >= 4); //must have 4 or more
                if (ret)
                    log_buf.append(
                        "[" + quotes.get(data_index).getDate() + "] Long #1 $" + close
                        + "  Down Count = " + down_count +"\n");
                return ret;

            case FrameworkConstants.CONNOR_STRATEGY_MUL_DAY_UP_SHORT://todo: ..................
                return false;

            case FrameworkConstants.CONNOR_STRATEGY_RSI_10_6_LONG:
                ret = close > _SMA200[data_index] && _RSI[data_index].getParam1() < 10;
                if (ret) //RSI larger than 10, under 200MA
                    log_buf.append(
                        "[" + quotes.get(data_index).getDate() + "] Long #1 $" + close
                        + "  RSI = " + _RSI[data_index].getParam1() +"\n");
                return ret;

            case FrameworkConstants.CONNOR_STRATEGY_RSI_94_90_SHORT://todo: ..................
                return false;
        }
        return false;
    }

    //is entry 2 condition met ? (aggressive option)
    private boolean entry2Triggering(int data_index) {
        if (!_bAggressiveOption)//must turn on aggressive option
            return false;
        if (_ActiveTrade2 != null && _ActiveTrade2.isOpen())//already have trade 2
            return false;

        StringBuilder log_buf = new StringBuilder();
        ArrayList<FundQuote> quotes = _Fund.getQuote();
        float close = quotes.get(data_index).getClose();
        //based on strategy, apply different entry 2 algorithm
        switch (_nStrategy) {
            case FrameworkConstants.CONNOR_STRATEGY_3DAY_CHANNEL_LONG:
                int entry_index = _Fund.findIndexByDate(_ActiveTrade1.getEntryDate());
                boolean ret = close < quotes.get(entry_index).getClose();
                log_buf.append(
                    "[" + quotes.get(data_index).getDate() + "] Long #2 $" + close + "\n");
                return ret;

            case FrameworkConstants.CONNOR_STRATEGY_3DAY_CHANNEL_SHORT:
                entry_index = _Fund.findIndexByDate(_ActiveTrade1.getEntryDate());
                ret = close > quotes.get(entry_index).getClose();
                log_buf.append(
                    "[" + quotes.get(data_index).getDate() + "] Short #2 $" + close + "\n");
                return ret;

            case FrameworkConstants.CONNOR_STRATEGY_RSI_25_LONG://4-RSI less than 25 and close > 200MA
                _RSI1 = IndicatorUtil.calcRSI(_Fund, data_index, 4, _RSI1);
                ret = _RSI1.getParam1() < 20;
                if (ret)
                    log_buf.append(
                        "[" + quotes.get(data_index).getDate() + "] Long #2 $" + close +
                        "\tRSI = " + _RSI1.getParam1() + "\n");
                return ret;

            case FrameworkConstants.CONNOR_STRATEGY_RSI_75_SHORT:
                _RSI1 = IndicatorUtil.calcRSI(_Fund, data_index, 4, _RSI1);
                ret = _RSI1.getParam1() > 80;
                if (ret)
                    log_buf.append(
                        "[" + quotes.get(data_index).getDate() + "] Short #2 $" + close +
                        "\tRSI = " + _RSI1.getParam1() + "\n");
                return ret;

            case FrameworkConstants.CONNOR_STRATEGY_PERCENT_B_LONG://todo: ..................
                return false;

            case FrameworkConstants.CONNOR_STRATEGY_PERCENT_B_SHORT://todo: ..................
                return false;

            case FrameworkConstants.CONNOR_STRATEGY_R3_LONG:
                entry_index = _Fund.findIndexByDate(_ActiveTrade1.getEntryDate());
                ret = close < quotes.get(entry_index).getClose();
                log_buf.append(
                    "[" + quotes.get(data_index).getDate() + "] Long #2 $" + close + "\n");
                return ret;

            case FrameworkConstants.CONNOR_STRATEGY_R3_SHORT://todo: ..................
                return false;

            case FrameworkConstants.CONNOR_STRATEGY_MUL_DAY_DOWN_LONG:
                entry_index = _Fund.findIndexByDate(_ActiveTrade1.getEntryDate());
                ret = close < quotes.get(entry_index).getClose();
                log_buf.append(
                    "[" + quotes.get(data_index).getDate() + "] Long #2 $" + close + "\n");
                return ret;

            case FrameworkConstants.CONNOR_STRATEGY_MUL_DAY_UP_SHORT://todo: ..................
                return false;

            case FrameworkConstants.CONNOR_STRATEGY_RSI_10_6_LONG:
                ret = _RSI[data_index].getParam1() < 6;
                if (ret)
                    log_buf.append(
                        "[" + quotes.get(data_index).getDate() + "] Long #2 $" + close
                        + "  RSI = " + _RSI[data_index].getParam1() +"\n");
                return ret;

            case FrameworkConstants.CONNOR_STRATEGY_RSI_94_90_SHORT://todo: ..................
                return false;
        }
        return false;
    }

    //is exit condition met?
    private boolean exitTriggering(int data_index) {
        StringBuilder log_buf = new StringBuilder();
        ArrayList<FundQuote> quotes = _Fund.getQuote();
        float close = quotes.get(data_index).getClose();
        switch (_nStrategy) {
            case FrameworkConstants.CONNOR_STRATEGY_3DAY_CHANNEL_LONG:
                boolean ret = close > _SMA5[data_index];
                if (ret)
                    log_buf.append(
                        "\t[" + quotes.get(data_index).getDate() + "] Sell $" + close
                        + "\t" + _SMA5[data_index] + " | " + _SMA200[data_index] + "\n");
                return ret;

            case FrameworkConstants.CONNOR_STRATEGY_3DAY_CHANNEL_SHORT:
                ret = close < _SMA5[data_index];
                if (ret)
                    log_buf.append(
                        "\t[" + quotes.get(data_index).getDate() + "] Cover $" + close
                        + "\t" + _SMA5[data_index] + " | " + _SMA200[data_index] + "\n");
                return ret;

            case FrameworkConstants.CONNOR_STRATEGY_RSI_25_LONG://4-RSI less than 25 and close > 200MA
                _RSI1 = IndicatorUtil.calcRSI(_Fund, data_index, 4, _RSI1);
                ret = _RSI1.getParam1() > 55;
                if (ret)
                    log_buf.append(
                        "\t[" + quotes.get(data_index).getDate() + "] Sell $" + close
                        + "\tRSI = " + _RSI[data_index].getParam1() + "\n");
                return ret;

            case FrameworkConstants.CONNOR_STRATEGY_RSI_75_SHORT:
                _RSI1 = IndicatorUtil.calcRSI(_Fund, data_index, 4, _RSI1);
                ret = _RSI1.getParam1() < 45;
                if (ret)
                    log_buf.append(
                        "\t[" + quotes.get(data_index).getDate() + "] Cover $" + close
                        + "\tRSI = " + _RSI[data_index].getParam1() + "\n");
                return ret;

            case FrameworkConstants.CONNOR_STRATEGY_PERCENT_B_LONG://todo: ..................
                return false;

            case FrameworkConstants.CONNOR_STRATEGY_PERCENT_B_SHORT://todo: ..................
                return false;

            case FrameworkConstants.CONNOR_STRATEGY_R3_LONG:
                ret = _RSI[data_index].getParam1() > 70;
                if (ret)
                    log_buf.append(
                        "\t[" + quotes.get(data_index).getDate() + "] Sell $" + close
                        + "\tRSI = " + _RSI[data_index].getParam1() + "\n");
                return ret;

            case FrameworkConstants.CONNOR_STRATEGY_R3_SHORT://todo: ..................
                return false;

            case FrameworkConstants.CONNOR_STRATEGY_MUL_DAY_DOWN_LONG:
            case FrameworkConstants.CONNOR_STRATEGY_RSI_10_6_LONG:
                ret = close > _SMA5[data_index];
                if (ret)
                    log_buf.append(
                        "\t[" + quotes.get(data_index).getDate() + "] Sell $" + close
                        + "\tSMA5 = " + _SMA5[data_index] + "\n");
                return ret;

            case FrameworkConstants.CONNOR_STRATEGY_MUL_DAY_UP_SHORT://todo: ..................
                return false;

            case FrameworkConstants.CONNOR_STRATEGY_RSI_94_90_SHORT://todo: ..................
                return false;
        }
        return false;
    }

    //to merge 2nd trades into one transaction during sell for aggressive mode
    private Transaction mergeTades(Transaction trans, ActiveTrade trade2) {
        float p1 = trans.getEntryPrice();
        String trade2_date = trade2.getEntryDate();
        float p2 = _Fund.findQuoteByDate(trade2_date).getClose();
        trans.setEntryPrice((p1 + p2) / 2);//average cost
        trans.calcPerformance();
        trans.setEntry2Date(trade2_date);
        trans.setEntry2Price(p2);
        return trans;
    }

    //-----instance variables-----
    private String _sStrategy; //matches one of the choices in LIST_CONNOR_STRATEGY
    private int _nStrategy = -1;
    private boolean _bAggressiveOption;//set by caller using two entries
    private FundData _Fund = FrameworkConstants.SP500_DATA;
    public FundData getFund() { return _Fund; }
    //track transaction for each strategy, this way for given ETF, this array does not need to be calculated every time
    private HashMap<String, ArrayList<Transaction>> _TransactionHash = new HashMap<String, ArrayList<Transaction>>();
    public ArrayList<Transaction> getTransactionLog(String strategy) { return _TransactionHash.get(strategy); }

    //two SMA should have the same index as _Fund with some elements that have no meaning
    private float[] _SMA5;
    private float[] _SMA200;
    private IndicatorData[] _RSI;//pre-calc RSI
    private IndicatorData _RSI1;//temp storage for RSI25/75
    private ActiveTrade _ActiveTrade1;//null = no trade
    public ActiveTrade getActiveTrade1() { return _ActiveTrade1; }
    private ActiveTrade _ActiveTrade2;//for aggressive option
    public ActiveTrade getActiveTrade2() { return _ActiveTrade2; }
}