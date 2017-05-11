package org.marketsuite.simulator;

import org.marketsuite.framework.strategy.buyhold.BuyHoldPanel;
import org.marketsuite.framework.strategy.dca.DollarCostAvgPanel;
import org.marketsuite.framework.strategy.lsquare.LSquarePanel;
import org.marketsuite.framework.strategy.mac.MacPanel;
import org.marketsuite.framework.strategy.macoscillator.MacOscillatorPanel;
import org.marketsuite.framework.strategy.pricesmacross.PriceSmaCrossPanel;
import org.marketsuite.framework.strategy.twosmacross.DualSmaCrossPanel;
import org.marketsuite.framework.strategy.wmatl.WmaTlPanel;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.simulator.advanced.custom.CustomReportPanel;
import org.marketsuite.simulator.advanced.indicatorsim.IndicatorSimPanel;
import org.marketsuite.simulator.advanced.report.lsquare.LSquareReportPanel;
import org.marketsuite.simulator.advanced.scanreport.ScanReportPanel;
import org.marketsuite.simulator.basic.CustomAnalysisPanel;
import org.marketsuite.framework.strategy.buyhold.BuyHoldPanel;
import org.marketsuite.framework.strategy.macoscillator.MacOscillatorPanel;
import org.marketsuite.framework.strategy.pricesmacross.PriceSmaCrossPanel;
import org.marketsuite.framework.strategy.twosmacross.DualSmaCrossPanel;
import org.marketsuite.framework.strategy.wmatl.WmaTlPanel;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.simulator.advanced.indicatorsim.IndicatorSimPanel;
import org.marketsuite.simulator.advanced.report.lsquare.LSquareReportPanel;
import org.marketsuite.simulator.basic.CustomAnalysisPanel;

import javax.swing.*;
import java.awt.*;

//main container for all views,
public class SimulatorPanel extends JPanel {
    //CTOR - use card layout for two panels, one is strategy simulator, another is scanner
    public SimulatorPanel() {
        setLayout(new CardLayout());
        setOpaque(false);

        //card 1 - basic mode tabs
        _pnlBasicMode = new JTabbedPane();
        _pnlBasicMode.setName("Main");//tell MainTabUI to use taller height
        _pnlBasicMode.addTab("Buy and Hold", _pnlBuyHold = new BuyHoldPanel(this));
//        _pnlBasicMode.addTab(ApolloConstants.APOLLO_BUNDLE.getString("sim_dca"), _pnlDca = new DollarCostAvgPanel());
//        _pnlBasicMode.addTab(FrameworkConstants.FRAMEWORK_BUNDLE.getString("sim_ind"), _pnlOsc = new IndicatorPanel());
        _pnlBasicMode.addTab("Price SMA Crossing", _pnlPriceMaCross = new PriceSmaCrossPanel());
        _pnlBasicMode.addTab("Dual SMA Crossing", _pnlDualSmaCross = new DualSmaCrossPanel());
        _pnlBasicMode.addTab("Three EMA Crossing", _pnlMac = new MacPanel());
//        _pnlBasicMode.addTab(ApolloConstants.APOLLO_BUNDLE.getString("l2_id"), _pnlLsquare = new LSquarePanel());
//        _pnlBasicMode.addTab("WMA / Trend", _pnlWmaTl = new WmaTlPanel());
//        _pnlBasicMode.addTab(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mzc_lbl_4"), _pnlMacOsc = new MacOscillatorPanel());
        _pnlBasicMode.addTab("Custom Strategy", _pnlCustomAnalysis = new CustomAnalysisPanel());
        add(_pnlBasicMode, CARD_BASIC_MODE);

        //card 2 - advanced mode tabs - scanner, bulk export, data manager
        _pnlAdvMode = new JTabbedPane();
        _pnlAdvMode.setName("Main");//tell MainTabUI to use taller height
        _pnlAdvMode.addTab(ApolloConstants.APOLLO_BUNDLE.getString("sim_scanrpt"), _pnlScanReport = new ScanReportPanel());
        _pnlAdvMode.addTab(ApolloConstants.APOLLO_BUNDLE.getString("l2_name"), _pnlL2Report = new LSquareReportPanel());
        _pnlAdvMode.addTab(ApolloConstants.APOLLO_BUNDLE.getString("advsim_indsim"), _pnlAdvSim = new IndicatorSimPanel());
        _pnlAdvMode.addTab(ApolloConstants.APOLLO_BUNDLE.getString("advsim_custom"),  _pnlCustomReport = new CustomReportPanel());
        add(_pnlAdvMode, CARD_ADVANCED_MODE);
    }

    //----- public methods -----
    //toggle between simulator and scanner
    public void changeView(String view_id) {
        ((CardLayout)getLayout()).show(this, view_id);
    }
    public void setDateRange(String start_date, String end_date) {
//        _pnlBuyHold.setDateRange(start_date, end_date);
        _pnlPriceMaCross.setDateRange(start_date, end_date);
        _pnlDualSmaCross.setDateRange(start_date, end_date);
//        _pnlDca.setDateRange(start_date, end_date);
//        _pnlOsc.setDateRange(start_date, end_date);
        _pnlMac.setDateRange(start_date, end_date);
//        _pnlMacOsc.setDateRange(start_date, end_date);
    }

    //-----instance variables-----
    private JTabbedPane _pnlBasicMode, _pnlAdvMode;
    private BuyHoldPanel _pnlBuyHold;
    private PriceSmaCrossPanel _pnlPriceMaCross;
    private DualSmaCrossPanel _pnlDualSmaCross;
    private DollarCostAvgPanel _pnlDca;
    private IndicatorPanel _pnlOsc;
    private MacPanel _pnlMac;
    private LSquarePanel _pnlLsquare;
    private MacOscillatorPanel _pnlMacOsc;
    private WmaTlPanel _pnlWmaTl;
    private CustomAnalysisPanel _pnlCustomAnalysis;
    private IndicatorSimPanel _pnlAdvSim;
    private CustomReportPanel _pnlCustomReport;
    private LSquareReportPanel _pnlL2Report;
    private ScanReportPanel _pnlScanReport;

    //-----literals-----
    public final static String CARD_BASIC_MODE = "BASIC-MODE";
    public final static String CARD_ADVANCED_MODE = "ADVANCED-MODE";
}