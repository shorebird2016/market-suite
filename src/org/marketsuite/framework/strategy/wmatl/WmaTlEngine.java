package org.marketsuite.framework.strategy.wmatl;

import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.quote.WeeklyQuote;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.framework.strategy.base.SimParam;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.framework.util.IndicatorUtil;

import java.util.ArrayList;

public class WmaTlEngine extends AbstractEngine {
    public void simulate(String start_date, String end_date) { }
    public String getId() { return "WMA-TL"; }
    public String getStrategy() { return "WMA-TL"; }
    public String getStrategyInfo() { return ""; }//TODO later
    public boolean isBuySetup() { return false; }
    public boolean isBuyTrigger() { return false; }
    public String getBuyTriggerDate() { return null; }
    public boolean isSellSetup() { return false; }
    public boolean isSellTrigger() { return false; }
    public String getSellTriggerDate() { return null; }
    public boolean simulate() {//handle exceptions within and report in an error array
        SimParam param = getSimParam();
        int wma_period = param.getWmaTlOption().getWmaPeriod();
        String sym = param.getStdOptions().getSymbol();
        String sd = simParam.getStdOptions().getStartDate();
        int start_index = _Fund.findIndexByDate(sd);
        String ed = simParam.getStdOptions().getEndDate();
        int end_index = _Fund.findIndexByDate(ed);
        if (start_index == -1 || end_index == -1) {
            ArrayList<String> ers = new ArrayList<>();
            if (start_index == -1) ers.add("Starting Date NOT Available --> " + sd);
            if (end_index == -1) ers.add("Ending Data NOT Available --> " + ed);
            setErrors(ers);
            return false;
        }

        //build up WMA array (-1 means no data yet), generate transactions
        start_index = _WeeklyQuote.getSize() - 2; end_index = 0;//TODO use real thing later
        float[] wma = IndicatorUtil.calcSMA(_WeeklyQuote.getCloses(), wma_period, _WeeklyQuote.getSize() - 1, sym);
        _EntryCrossing = calcCrossing(_WeeklyQuote.getCloses(), wma, start_index, end_index);
        _ExitCrossing = calcCrossing(_WeeklyQuote.getCloses(), wma, start_index, end_index);
        executeTrades(start_index, end_index);
        ArrayList<String> errors = getErrors();
        return true;
    }
    public void setFund(FundData fund) {
        _Fund = fund;
        DataUtil.adjustForSplits(_Fund, _Fund.getSize() - 1, 0);
        _WeeklyQuote = new WeeklyQuote(_Fund, _Fund.getSize() - 1);
    }

    //---- variables -----
    private WeeklyQuote _WeeklyQuote;
}
