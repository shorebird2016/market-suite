package org.marketsuite.main;

import org.marketsuite.chart.candle.FractalPanel;
import org.marketsuite.framework.model.type.CandleSignal;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.Props;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.chart.candle.FractalPanel;
import org.marketsuite.framework.model.type.CandleSignal;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.Props;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

//a candlestick based chart window independent of JFreeChart
public class DailyCandleChartFrame extends JInternalFrame implements PropertyChangeListener {
    public DailyCandleChartFrame() {
        setName("Main");//for MainTabUI to recognize
        setResizable(true); setClosable(true); setMaximizable(true); setIconifiable(false);
        setTitle(ApolloConstants.APOLLO_BUNDLE.getString("frm_ttl_day_cdl"));
        setFrameIcon(FrameworkIcon.CANDLE_CHART);
//        setContentPane(_pnlChart = new DailyCandlePanel());
        setContentPane(_pnlChart = new FractalPanel());
        MainUtil.handleLocationAndSize(this, MdiMainFrame.INDEX_DAILY_CANDLE_CHART, MdiMainFrame.LOCATION_DAILY_CANDLE_CHART, MdiMainFrame.SIZE_DAILY_CANDLE_CHART);
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
    private FractalPanel _pnlChart;
}