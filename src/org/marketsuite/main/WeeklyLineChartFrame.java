package org.marketsuite.main;

import org.marketsuite.chart.line.WeeklyLineChartPanel;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.Props;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

//a simpler version of chart window without strategy implications
public class WeeklyLineChartFrame extends JInternalFrame implements PropertyChangeListener {
    public WeeklyLineChartFrame() {
        setName("Main");//for MainTabUI to recognize
        setResizable(true); setClosable(true); setMaximizable(true); setIconifiable(false);
        setTitle(ApolloConstants.APOLLO_BUNDLE.getString("qc_01"));
        setFrameIcon(FrameworkIcon.LINE_CHART);
        setContentPane(_pnlLineChart = new WeeklyLineChartPanel());
        MainUtil.handleLocationAndSize(this, MdiMainFrame.INDEX_WEEKLY_LINE_CHART, MdiMainFrame.LOCATION_WEEKLY_LINE_CHART, MdiMainFrame.SIZE_WEEKLY_LINE_CHART);
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
                    _pnlLineChart.plotGraph(symbol);
                break;
        }
    }

    //----- variables-----
    private WeeklyLineChartPanel _pnlLineChart;
}