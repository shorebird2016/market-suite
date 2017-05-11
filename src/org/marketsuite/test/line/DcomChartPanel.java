package org.marketsuite.test.line;

import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.market.MarketInfo;

import java.text.ParseException;
import java.util.HashMap;

public class DcomChartPanel extends AbstractChartPanel {
    public DcomChartPanel() {
        super(true);
    }

    public void drawGraph(String symbol) {
        HashMap<String, MarketInfo> map = _WatchListModel.getMarketInfoMap();
        MarketInfo mki = map.get(symbol);
        try {
            drawDcomGraph(mki);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    public void drawGraph(MarketInfo mki) {
        //call base class method to draw DCOM charts
        try {
            drawDcomGraph(mki);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}