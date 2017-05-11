package org.marketsuite.simulator.advanced;

import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.strategy.analysis.AnnualReturnGraphPanel;
import org.marketsuite.framework.strategy.analysis.EquityGraphPanel;
import org.marketsuite.framework.strategy.base.PriceGraphPanel;
import org.marketsuite.framework.strategy.base.SimReport;
import org.marketsuite.framework.strategy.base.TradePanel;
import org.marketsuite.framework.util.SimUtil;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.jfree.data.general.SeriesException;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.strategy.analysis.EquityGraphPanel;
import org.marketsuite.framework.strategy.base.SimReport;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.ParseException;

/**
 * A floating window to show equity, annualized return and price graphs.
 */
public class SimGraphDialog extends JDialog {
    //singleton CTOR
    private static SimGraphDialog _Instance;
    public static SimGraphDialog getInstance() {
        if (_Instance == null)
            _Instance = new SimGraphDialog();
        return _Instance;
    }

    /**
     * CTOR: construct dialog with specified StopLevelInfo object.
     */
    private SimGraphDialog() {
        super(MdiMainFrame.getInstance(), false);//non-modal, use this to keep dialog always in front
        setTitle(ApolloConstants.APOLLO_BUNDLE.getString("runrpt_ttl_1"));
        SkinPanel content_pnl = new SkinPanel(LazyIcon.BACKGROUND_CONTENT, new BorderLayout());
        content_pnl.setBorder(new BevelBorder(BevelBorder.LOWERED));

        //3 tabs: one for each graph
        JTabbedPane tab_pane = new JTabbedPane();
        tab_pane.add(ApolloConstants.APOLLO_BUNDLE.getString("runrpt_ttl_5"), _pnlTrade = new TradePanel());
        tab_pane.add(ApolloConstants.APOLLO_BUNDLE.getString("runrpt_ttl_2"), _pnlEquityGraph = new EquityGraphPanel());
        tab_pane.add(ApolloConstants.APOLLO_BUNDLE.getString("runrpt_ttl_3"), _pnlAnnualReturnGraph = new AnnualReturnGraphPanel());
        tab_pane.add(ApolloConstants.APOLLO_BUNDLE.getString("runrpt_ttl_4"), _pnlPriceGraph = new PriceGraphPanel());
        content_pnl.add(tab_pane, BorderLayout.CENTER);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent wev) {
            }
        });
        setContentPane(content_pnl);
        WidgetUtil.setDialogProperties(this, new Dimension(0, 0), true, MdiMainFrame.getInstance(),
                WindowConstants.DISPOSE_ON_CLOSE, false);
    }

    //refresh equity graph, annual return graphs, price graphs
    public void refreshGraph(SimReport sim_report) throws ParseException, IOException, SeriesException {
        SimUtil.plotEquityGraph(_pnlEquityGraph, sim_report);
        SimUtil.plotAnnualReturnGraph(_pnlAnnualReturnGraph, sim_report);
        SimUtil.plotPriceGraph(_pnlPriceGraph, sim_report);
        _pnlTrade.populate(sim_report.getTransLog());
    }

    //clearGraph data and hide
    public void clear() {
        _pnlEquityGraph.clear();
        _pnlAnnualReturnGraph.clear();
        _pnlPriceGraph.clear();
        setVisible(false);
    }

    //----- instance variables -----
    private TradePanel _pnlTrade;
    private EquityGraphPanel _pnlEquityGraph;
    private AnnualReturnGraphPanel _pnlAnnualReturnGraph;
    private PriceGraphPanel _pnlPriceGraph;
}