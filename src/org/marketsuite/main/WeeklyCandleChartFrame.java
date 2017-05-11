package org.marketsuite.main;

import org.marketsuite.chart.candle.WeeklyCandlePanel;
import org.marketsuite.framework.model.type.CandleSignal;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.Props;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloIcon;
import org.marketsuite.chart.candle.WeeklyCandlePanel;
import org.marketsuite.framework.model.type.CandleSignal;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.Props;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

//a candlestick based chart window independent of JFreeChart
public class WeeklyCandleChartFrame extends JInternalFrame implements PropertyChangeListener {
    public WeeklyCandleChartFrame() {
        setName("Main");//for MainTabUI to recognize
        setResizable(true); setClosable(true); setMaximizable(true); setIconifiable(false);
        setTitle(ApolloConstants.APOLLO_BUNDLE.getString("frm_ttl_wk_cdl"));
        setFrameIcon(FrameworkIcon.CANDLE_CHART);
        setContentPane(_pnlChart = new WeeklyCandlePanel());
        MainUtil.handleLocationAndSize(this, MdiMainFrame.INDEX_WEEKLY_CANDLE_CHART, MdiMainFrame.LOCATION_WEEKLY_CANDLE_CHART, MdiMainFrame.SIZE_WEEKLY_CANDLE_CHART);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        Props.addWeakPropertyChangeListener(Props.SymbolSelection, this);//handle symbol selection
        Props.addWeakPropertyChangeListener(Props.CandleSignal, this);//handle signal changes
    }

    //----- interface/override methods -----
    public void propertyChange(PropertyChangeEvent evt) {
        if (!isVisible()) return;
        Props prop = (Props) evt.getSource();
        switch (prop) {
            case SymbolSelection://update graph
                String symbol = (String)prop.getValue();
                if (symbol == null || symbol.equals("")) return;
                _pnlChart.plot(symbol);
                break;

            case CandleSignal:
                ArrayList<CandleSignal> candle_signals = (ArrayList<CandleSignal>)prop.getValue();
                _pnlChart.plot(candle_signals);
                break;
        }
    }

    //----- variables-----
    private WeeklyCandlePanel _pnlChart;
}