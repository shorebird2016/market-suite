package org.marketsuite.marektview;

import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MainModel;
import org.marketsuite.main.MainUtil;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.marektview.Velocity.VelocityView;
import org.marketsuite.marektview.history.HistoryViewPanel;
import org.marketsuite.marektview.ibd.Ibd50Panel;
import org.marketsuite.marektview.marketpulse.MarketPulsePanel;
import org.marketsuite.marektview.performance.PerformanceViewPanel;
import org.marketsuite.marektview.ranking.RankPanel;
import org.marketsuite.marektview.ranking.RankingPanel;
import org.marketsuite.marektview.valuation.ValuationViewPanel;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloIcon;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MainModel;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.marektview.history.HistoryViewPanel;
import org.marketsuite.marektview.ibd.Ibd50Panel;
import org.marketsuite.marektview.ranking.RankPanel;
import org.marketsuite.marektview.ranking.RankingPanel;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * An internal frame container that provides relative performance and ranking for desired watch lists.
 */
public class MarketViewFrame extends JInternalFrame implements PropertyChangeListener {
    public MarketViewFrame() {
        super(ApolloConstants.APOLLO_BUNDLE.getString("wlg_lbl_20"), true, true, true, false);
        setFrameIcon(ApolloIcon.APP_ICON);
        MainUtil.handleLocationAndSize(this, MdiMainFrame.INDEX_MARKET, MdiMainFrame.LOCATION_MARKET_VIEW, MdiMainFrame.SIZE_MARKET);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        //paint background image
        JPanel content_pane = new JPanel() {
            public void paintComponent(Graphics g) {//draw background
                g.drawImage(FrameworkIcon.BACKGROUND_ATLANTIS.getIcon().getImage(), 0, 0, this);
            }
        };
        content_pane.setLayout(new MigLayout("insets 0"));
        setContentPane(content_pane);

        //tabs
        _tabMain.addTab(ApolloConstants.APOLLO_BUNDLE.getString("rpv_lbl_01") + "  ", _pnlPerformance = new PerformanceViewPanel());
        _tabMain.addTab(ApolloConstants.APOLLO_BUNDLE.getString("hv_tab") + "  ", _pnlHistory = new HistoryViewPanel());
        _tabMain.addTab("Valuation View" + "  ", _pnlValue = new ValuationViewPanel());
        _tabMain.addTab(ApolloConstants.APOLLO_BUNDLE.getString("rv_tab") + "  ", _pnlRank = new RankPanel());
        if (MainModel.getInstance().isMediumUser()) {
//            _tabMain.addTab(ApolloConstants.APOLLO_BUNDLE.getString("rpv_lbl_02") + "  ", _pnlRanking = new RankingPanel());
//            _tabMain.addTab(ApolloConstants.APOLLO_BUNDLE.getString("rpv_lbl_03") + "  ", _pnlVelocity = new VelocityView());
            _tabMain.addTab(ApolloConstants.APOLLO_BUNDLE.getString("ibd_sig") + "  ", _pnlMktPulse = new MarketPulsePanel());//mainly for tina
        }
//        _tabMain.addTab(ApolloConstants.APOLLO_BUNDLE.getString("rpv_lbl_04") + "  ", _pnlIbd50 = new Ibd50Panel());
        content_pane.add(_tabMain, "dock center");
        setContentPane(content_pane);
        _tabMain.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                _pnlRank.closeOptionWindow(); } });
        Props.addWeakPropertyChangeListener(Props.GroupChange, this);//handle group change from other frames
        Props.addWeakPropertyChangeListener(Props.SymbolSelection, this);//handle symbol selection change from other frames
        Props.addWeakPropertyChangeListener(Props.SymbolRemoved, this);//handle symbol deletion within frame
    }

    //----- interface, overrides -----
    public void propertyChange(PropertyChangeEvent evt) {
        if (!isVisible())
            return;
        Props prop = (Props) evt.getSource();
        switch (prop) {
            case GroupChange://group(s) added or deleted
                _pnlPerformance.initGroupSelector();
//                _pnlRanking.initGroupSelector();
                break;

            case SymbolSelection://of current watchlist
                String symbol = (String) prop.getValue();
                _pnlPerformance.handleSymbolSelectionChange(symbol);
//                _pnlMktPulse.plot(symbol);
//                _pnlRanking.handleSymbolSelection(symbol);
                break;

            case SymbolRemoved://from current watchlist
                symbol = (String) prop.getValue();
                _pnlPerformance.handleSymbolRemoval(symbol);
//                _pnlRanking.handleSymbolRemoval(symbol);
                break;
        }
    }

    //----- variables -----
    private PerformanceViewPanel _pnlPerformance;
    private HistoryViewPanel _pnlHistory;
    private ValuationViewPanel _pnlValue;
    private RankingPanel _pnlRanking;
    private RankPanel _pnlRank;
    private VelocityView _pnlVelocity;
    private Ibd50Panel _pnlIbd50;
    private MarketPulsePanel _pnlMktPulse;
    private JTabbedPane _tabMain = new JTabbedPane();

    //----- literals -----
}