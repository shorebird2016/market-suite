package org.marketsuite.framework.strategy.buyhold;

import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.Equity;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.framework.strategy.base.SimParam;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.Equity;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.framework.util.AppUtil;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

//algorithm to compute buy and hold equity changes
public class BuyHoldEngine extends AbstractEngine {
    public BuyHoldEngine(String symbol) throws IOException{
        _Fund = DataUtil.readFundHistory(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, symbol);
    }
    public BuyHoldEngine(FundData fund) { _Fund = fund; }
    public boolean simulate() { return false; }
    public void simulate(String start_date, String end_date) {
        _fInitialCapital = FrameworkConstants.START_CAPITAL;
        _EquityLog = new ArrayList<>();
        int begin_index = _Fund.findIndexByDate(start_date);
        int end_index = _Fund.findIndexByDate(end_date);

        //check no data
        if (begin_index == -1 || end_index == -1) {
            WidgetUtil.showWarning(null,
                    FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_msg_1"));
            return;
        }
        float price = _Fund.findQuoteByDate(start_date).getClose();
        float shares = _fInitialCapital / price;

        //calculate and create EntryLog with a loop
        while(begin_index >= end_index) {
            String date = _Fund.getDate(begin_index);
            price = _Fund.getPrice(begin_index);
            Equity equity = new Equity(date, shares, price);
            _EquityLog.add(equity);
            begin_index--;
        }
    }

    //---dummy interface implementation-----
    public boolean isLong(String cur_date) {
        return false;
    }
    public SimParam getSimParam() {
        return null;
    }
    public void setSimParam(SimParam param) {}
    public String getId() { return "Buy and HOld"; }
    public String getStrategy() { return "Buy and Hold SP500"; }
    public String getStrategyInfo() {
        return getId() + "";
    }
    public boolean isBuySetup() { return false; }
    public boolean isBuyTrigger() { return false; }
    public String getBuyTriggerDate() { return null; }
    public boolean isSellSetup() { return false; }
    public boolean isSellTrigger() { return false; }
    public String getSellTriggerDate() { return null; }

    //-----public methods-----
    public void printReport() {
//TODO        if (!Main.logging)
//            return;

        StringBuilder buf = new StringBuilder();
        if (_EquityLog.size() <= 0) {
            buf.append("--------- Buy and Hold: Nothing to Report ---------------\n");
            return;
        }

        buf.append("---------BUY and HOLD REPORT (" + _Fund.getSymbol() + ") --------------\n");
        for (Equity log : _EquityLog) {
            buf.append(" " + log.getDate() + "\tShares: "
                    + FrameworkConstants.SHARE_FORMAT.format(log.getShares())
                    + "\tEquity: " + FrameworkConstants.DOLLAR_FORMAT.format(log.getEquity()) + "\n"
            );
        }

        //compute compound rate of return from beginning of first transaction to end of last transaction
        float end_equity = _EquityLog.get(_EquityLog.size() - 1).getEquity();
        String begin_date = _EquityLog.get(0).getDate();
        String end_date = _EquityLog.get(_EquityLog.size() - 1).getDate();
        try {
            double exponent = 1 / AppUtil.calcYear(begin_date, end_date);
            double base = end_equity / _fInitialCapital;
            double cagr = Math.pow(base, exponent) - 1;
            buf.append(" [CAGR] Buy-and-Hold " + FrameworkConstants.ROI_FORMAT.format(cagr) + "\n");
        } catch (ParseException e) {
            buf.append(" ??? Begin or End date may be bad.......\n");
            e.printStackTrace();
        }
        System.out.println(buf);
    }

    public ArrayList<Equity> getEquityLog() {
        return _EquityLog;
    }

    public FundData getFundData() { return _Fund; }

    //-----instance variables-----
    private FundData _Fund;
    private ArrayList<Equity> _EquityLog;
    private float _fInitialCapital;
}
