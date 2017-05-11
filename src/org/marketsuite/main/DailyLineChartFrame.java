package org.marketsuite.main;

import org.marketsuite.chart.line.DailyLineChartPanel;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.Props;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

//a simpler version of chart window without strategy implications
public class DailyLineChartFrame extends JInternalFrame implements PropertyChangeListener {
    public DailyLineChartFrame() {
        setName("Main");//for MainTabUI to recognize
        setResizable(true); setClosable(true); setMaximizable(true); setIconifiable(false);
        setTitle(ApolloConstants.APOLLO_BUNDLE.getString("qc_03"));
        setFrameIcon(FrameworkIcon.LINE_CHART_32);
        setContentPane(_pnlLineChart = new DailyLineChartPanel());
        MainUtil.handleLocationAndSize(this, MdiMainFrame.INDEX_DAILY_LINE_CHART, MdiMainFrame.LOCATION_DAILY_LINE_CHART, MdiMainFrame.SIZE_DAILY_LINE_CHART);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        Props.addWeakPropertyChangeListener(Props.SymbolSelection, this);//handle symbol selection
    }

    //----- interface/override methods -----
    public void propertyChange(PropertyChangeEvent evt) {
        if (!isVisible())
            return;
        Props prop = (Props) evt.getSource();
        switch (prop) {
            case SymbolSelection://update graph
                String symbol = (String)prop.getValue();
                if (symbol == null || symbol.equals("")) return;
                _pnlLineChart.plot(symbol);
                break;
        }
    }

    //----- variables-----
    private DailyLineChartPanel _pnlLineChart;
}