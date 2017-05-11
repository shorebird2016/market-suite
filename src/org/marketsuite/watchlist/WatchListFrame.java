package org.marketsuite.watchlist;

import org.marketsuite.component.dialog.NameDialog;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MainModel;
import org.marketsuite.main.MainUtil;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloIcon;
import org.marketsuite.watchlist.model.WatchListModel;
import org.marketsuite.watchlist.performance.PerformancePanel;
import org.marketsuite.watchlist.speed.SpeedViewPanel;
import org.marketsuite.watchlist.technical.IbdRatingGraphPanel;
import org.marketsuite.watchlist.technical.TechnicalPanel;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.swingx.JXHyperlink;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MainModel;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;
import org.marketsuite.watchlist.speed.SpeedViewPanel;
import org.marketsuite.watchlist.technical.IbdRatingGraphPanel;
import org.marketsuite.watchlist.technical.TechnicalPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;

public class WatchListFrame extends JInternalFrame implements PropertyChangeListener {
    public WatchListFrame(final JFrame parent) {
        super(ApolloConstants.APOLLO_BUNDLE.getString("wlw_lbl_1"), true, true, true, false);
        setName("Main");//for MainTabUI to recognize
        setFrameIcon(ApolloIcon.APP_ICON);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //paint background image
        _pnlContent = new JPanel() {
            public void paintComponent(Graphics g) {//draw background
                g.drawImage(ApolloIcon.RISKMGR_BACKGROUND.getIcon().getImage(), 0, 0, this);
            }
        };
        _pnlContent.setLayout(new MigLayout("insets 0"));
        setContentPane(_pnlContent);

        //link - search, filter_lnk to csv files
        JPanel north_pnl = new JPanel(new MigLayout("insets 0", "5[]push[]50[][]50[][]5", "3[25]3"));
        north_pnl.setOpaque(false);
        north_pnl.add(_lblWatchlist);
        _lblWatchlist.setFont(FrameworkConstants.MEDIUM_FONT);
        _lblWatchlist.setForeground(FrameworkConstants.COLOR_LITE_GREEN);

        //create watch list link
        JXHyperlink filter_lnk = WidgetUtil.createHyperLink(WidgetUtil.LEVEL_1, ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_22"),
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        //collect all checked symbols, create watch list, store in user entered watch list name
                        NameDialog dlg = new NameDialog(MdiMainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("mkt_53"), "");
                        ArrayList<String> symbols = new ArrayList<>();
                        for (JCheckBox chk : _chkSymbols) {
                            if (chk.isSelected())
                                symbols.add(chk.getText());
                        }
                        GroupStore.getInstance().addGroup(dlg.getEntry(), symbols);
                        Props.WatchListsChange.setChanged();
                    }
                });
        filter_lnk.setForeground(FrameworkConstants.COLOR_LITE_GREEN);
        filter_lnk.setFont(FrameworkConstants.MEDIUM_SMALL_FONT);
        WidgetUtil.attachToolTip(filter_lnk, ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_24"), SwingConstants.LEFT, SwingConstants.TOP);
        north_pnl.add(filter_lnk);

        //search field
        JLabel lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_35"));
        lbl.setForeground(FrameworkConstants.COLOR_LITE_GREEN); lbl.setFont(FrameworkConstants.MEDIUM_SMALL_FONT); north_pnl.add(lbl, "right");
        north_pnl.add(_fldSearch); //_fldSearch.setFont(FrameworkConstants.MEDIUM_SMALL_FONT);
        _fldSearch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                String txt = _fldSearch.getText().toUpperCase();
                if (txt.length() == 0)
                    return;

                MainModel.getInstance().getWatchListModel().setCurSymbol(txt);
                int sel = _tabMain.getSelectedIndex();
                switch (sel) {
                    case TAB_PERFORMANCE:
                        _pnlPerformance.findSymbol(txt);
                        break;

                    case TAB_SPEED:
                        _pnlSpeed.findSymbol(txt);
                        break;

                    case TAB_TECHNICAL:
                        _pnlTechnical.findSymbol(txt);
                        break;

//                    case TAB_FUNDAMENTAL:
//                        _pnlFundamental.findSymbol(txt);
//                        break;
//
//                    case TAB_IBD_RATING:
//                        _pnlIbdRatingGraph.emphasize(txt);
//                        break;
                }
                _fldSearch.setSelectionStart(0);
                _fldSearch.setSelectionEnd(5);
            }
        });

        //IBD plot source
        lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_33"));
        lbl.setFont(FrameworkConstants.MEDIUM_SMALL_FONT); lbl.setForeground(FrameworkConstants.COLOR_LITE_GREEN);
        north_pnl.add(lbl, "right"); north_pnl.add(_cmbPlot); //_cmbPlot.setFont(FrameworkConstants.MEDIUM_SMALL_FONT);
        _cmbPlot.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED) return;
//                _pnlIbdRatingGraph.setPlotType((IbdRatingGraphPanel.PlotType)_cmbPlot.getSelectedItem());
            }
        });
        _pnlContent.add(north_pnl, "dock north");

        //west side - navigation checkboxes
        _pnlNav = new JPanel(new MigLayout("insets 15 0 2 20, flowy, gapy 0"));
        _pnlContent.add(new JScrollPane(_pnlNav), "dock west");
        createNavCheckboxes(_pnlNav);

        //center - tabs
        _tabMain.addTab(ApolloConstants.APOLLO_BUNDLE.getString("wl_tab_0"), _pnlPerformance = new PerformancePanel());
        _tabMain.addTab(ApolloConstants.APOLLO_BUNDLE.getString("wl_tab_1"), _pnlSpeed = new SpeedViewPanel());
        _tabMain.addTab(ApolloConstants.APOLLO_BUNDLE.getString("wl_tab_2"), _pnlTechnical = new TechnicalPanel());
//        _tabMain.addTab(ApolloConstants.APOLLO_BUNDLE.getString("wl_tab_3"), _pnlFundamental = new FundamentalPanel());
//        _tabMain.addTab(ApolloConstants.APOLLO_BUNDLE.getString("wl_tab_4"), _pnlIbdRatingGraph = new IbdRatingGraphPanel());
        _pnlContent.add(_tabMain, "dock center");
        MainUtil.handleLocationAndSize(this, MdiMainFrame.INDEX_WATCHLIST, MdiMainFrame.LOCATION_WATCHLIST, MdiMainFrame.SIZE_WATCHLIST);

        //populate, set listeners
        populate();
        _tabMain.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                //use current symbol to select row in the tab
                int sel = _tabMain.getSelectedIndex();
                String sym = MainModel.getInstance().getWatchListModel().getCurSymbol();
                _cmbPlot.setEnabled(false);
                switch (sel) {
                    case TAB_PERFORMANCE:
                        _pnlPerformance.findSymbol(sym);
                        break;

                    case TAB_TECHNICAL:
                        _pnlTechnical.findSymbol(sym);
                        break;
//
//                    case TAB_FUNDAMENTAL:
//                        _pnlFundamental.findSymbol(sym);
//                        break;
//
//                    case TAB_IBD_RATING:
//                        _pnlIbdRatingGraph.emphasize(sym);
//                        _cmbPlot.setEnabled(true);
//                        break;
                }
            }
        });
        addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameClosing(InternalFrameEvent e) {
                ((MdiMainFrame) parent).closeWatchList();
            }
        });
        Props.addWeakPropertyChangeListener(Props.SymbolSelection, this);//handle symbol change
        Props.addWeakPropertyChangeListener(Props.IndustryChange, this);//market view --> watch list
    }

    //----- interfaces, overrides -----
    public void propertyChange(PropertyChangeEvent evt) {
        if (!isVisible())
            return;
        Props prop = (Props) evt.getSource();
        switch (prop) {
            case SymbolSelection://update label
                String sym = (String) prop.getValue();
                MainModel.getInstance().getWatchListModel().setCurSymbol(sym);
//                _pnlIbdRatingGraph.emphasize(sym);
                break;

            case IndustryChange://WatchListModel in MainModel is populated
                populate();
                break;
        }
    }

    //----- public methods -----
    //assume WatchListModel is already built with selected symbol in watch list manager
    public void populate() {
        WatchListModel model = MainModel.getInstance().getWatchListModel();
        _lblWatchlist.setText(" " + model.getWatchlistName());
        if (_pnlNav != null) { //add navigator panel to west, skip first time
            _pnlNav.removeAll();
            createNavCheckboxes(_pnlNav);
        }
        _cmbPlot.setEnabled(_tabMain.getSelectedIndex() == TAB_IBD_RATING);
        _pnlPerformance.populate(model, false);
        _pnlSpeed.populate();
        _pnlTechnical.populate(model);
//        _pnlFundamental.populate(model);
//        _pnlIbdRatingGraph.createPaintMap();//new association of paint color
//        _pnlIbdRatingGraph.plotGraph(new ArrayList<String>(), model.getMembers().get(0));//include all symbols, emphasize 1st
    }

    //----- private methods -----
    private void createNavCheckboxes(JPanel container) {
        WatchListModel wlm = MainModel.getInstance().getWatchListModel();
        ArrayList<String> members = wlm.getMembers();
        Collections.sort(members);
        _chkSymbols = new JCheckBox[members.size()];
        int index = 0;
        for (String sym : members) {
            container.add(_chkSymbols[index] = new JCheckBox(sym));
            _chkSymbols[index].setBorder(new EmptyBorder(2,2,2,2));
            _chkSymbols[index].setSelected(true);
            _chkSymbols[index].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JCheckBox src = (JCheckBox)e.getSource();
                    String lbl = src.getText();

                    //find matching lbl row from all tables, show/hide
                    boolean checked_symbol = src.isSelected();
                    _pnlPerformance.showHideSymbol(lbl, checked_symbol);
                    _pnlTechnical.showHideSymbol(lbl, checked_symbol);
//                    _pnlFundamental.showHideSymbol(lbl, checked_symbol);

                    //find all un-checked_symbol symbols
                    ArrayList<String> excluded_symbols = new ArrayList<>();
                    for (JCheckBox chk : _chkSymbols) {
                        if (!chk.isSelected())
                            excluded_symbols.add(chk.getText());
                    }
//                    _pnlIbdRatingGraph.plotGraph(excluded_symbols, lbl);
                }
            });
            index++;
        }
    }

    //----- instance variables-----
    private JPanel _pnlContent;
    private JLabel _lblWatchlist = new JLabel();
    private JComboBox<IbdRatingGraphPanel.PlotType> _cmbPlot = new JComboBox<>(IbdRatingGraphPanel.PlotType.values());
    private JTextField _fldSearch = new JTextField(5);
    private JPanel _pnlNav;
    private JCheckBox[] _chkSymbols;
    private JTabbedPane _tabMain = new JTabbedPane();
    private SpeedViewPanel _pnlSpeed;
    private PerformancePanel _pnlPerformance;
    private TechnicalPanel _pnlTechnical;
//    private FundamentalPanel _pnlFundamental;
//    private IbdRatingGraphPanel _pnlIbdRatingGraph;

    //----- literals -----
    public static final int TAB_PERFORMANCE = 0;
    public static final int TAB_SPEED = 1;
    public static final int TAB_TECHNICAL = 2;
    public static final int TAB_FUNDAMENTAL = 2;
    public static final int TAB_IBD_RATING = 3;
}