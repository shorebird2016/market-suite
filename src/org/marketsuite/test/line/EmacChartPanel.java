package org.marketsuite.test.line;

import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.market.MarketInfo;

import java.text.ParseException;
import java.util.HashMap;

public class EmacChartPanel extends AbstractChartPanel {
    public EmacChartPanel() {
        super(false);
    }

    public void drawGraph(String symbol) {
        HashMap<String, MarketInfo> map = _WatchListModel.getMarketInfoMap();
        MarketInfo mki = map.get(symbol);
        if (mki == null)  return;
        try {
            drawEmacGraph(mki);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    public void drawGraph(MarketInfo mki) {
        if (mki == null) return;
        try {
            drawEmacGraph(mki);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
