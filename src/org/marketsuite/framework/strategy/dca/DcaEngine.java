package org.marketsuite.framework.strategy.dca;

import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.Equity;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.type.DcaType;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.framework.strategy.base.SimParam;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.DataUtil;

import java.io.IOException;
import java.util.ArrayList;

//algorithm to compute buy and hold equity changes
public class DcaEngine extends AbstractEngine {
    public DcaEngine(String symbol) throws IOException {
        _Fund = DataUtil.readFundHistory(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, symbol);
    }
    public boolean simulate() { return false; }
    public void simulate(String start_date, String end_date) {
        _EquityLog = new ArrayList<>();
        int begin_index = _Fund.findIndexByDate(start_date);
        int end_index = _Fund.findIndexByDate(end_date);

        //check no data
        if (begin_index == -1 || end_index == -1) {
            WidgetUtil.showWarning(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_msg_1"));
            return;
        }

        //skip first day since it can't look back on full range
        //traverse entire array from start to end, find each buying point, add to _nShares
        _nShares = 0;
        for (int idx = begin_index - 1; idx > end_index; idx--) {
            String prev_date = _Fund.getDate(idx);
            String cur_date = _Fund.getDate(idx + 1);
            switch (_dcaType) {
                case Monthly:
                    if (AppUtil.isBeginMonth(prev_date, cur_date)) buy(idx, cur_date);
                    break;

                case Quarterly:
                    if (AppUtil.isBeginQuarter(prev_date, cur_date)) buy(idx, cur_date);
                    break;

                case Annually:
                    if (AppUtil.isBeginYear(prev_date, cur_date)) buy(idx, cur_date);
                    break;
            }
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
    public String getId() { return "BUY / HOLD"; }
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
    public ArrayList<Equity> getEquityLog() {
        return _EquityLog;
    }
    public FundData getFundData() { return _Fund; }

    //-----private methods-----
    private void buy(int idx, String cur_date) {
        float price = _Fund.getPrice(idx + 1);
        _nShares += _nDcaAmount / price;//could be fractional _nShares
        Equity equity = new Equity(cur_date, _nShares, price);
        _EquityLog.add(equity); _nPurchaseCount++;
    }

    //-----accessors-----
    public void setDcaType(DcaType _dcaType) { this._dcaType = _dcaType; }
    public void setDcaAmount(int _nDcaAmount) { this._nDcaAmount = _nDcaAmount; }
    public int getPurchaseCount() { return _nPurchaseCount; }

    //-----instance variables-----
    private FundData _Fund;
    private ArrayList<Equity> _EquityLog;
    private DcaType _dcaType = DcaType.Monthly;
    private int _nDcaAmount; private float _nShares;
    private int _nPurchaseCount;
}
