package org.marketsuite.marektview.performance;

import org.marketsuite.component.Constants;
import org.marketsuite.component.field.NameField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.DisabledIcon;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.model.type.GraphMode;
import org.marketsuite.framework.model.type.Timeframe;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MainModel;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.thumbnail.ThumbnailPanel;
import org.marketsuite.watchlist.model.WatchListModel;
import org.marketsuite.watchlist.performance.PerformancePanel;
import org.marketsuite.watchlist.performance.PerformanceTableModel;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.swingx.JXHyperlink;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MainModel;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A container for market performance functions.
 */
public class PerformanceViewPanel extends JPanel implements PropertyChangeListener {
    public PerformanceViewPanel() {
        setLayout(new MigLayout("insets 0", "", "[]")); setOpaque(false);
        JPanel north_pnl = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER, new MigLayout("", "5[]15[][]30[][][]push[]5[]5[]push[]10[]10[]5", "3[]3"));

        //watch list drop down - select group widget
        initGroupSelector();
        north_pnl.add(_cmbGrpSelector = new JComboBox<>(_cmlGrpSelector));
        _cmbGrpSelector.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED) return;

                //re-calculate watch list model, plot
                String sel_grp = (String) _cmbGrpSelector.getSelectedItem();
                if (GroupStore.getInstance().getMembers(sel_grp).size() == 0) {//no symbol for this group, do nothing
                    WidgetUtil.showWarning(MdiMainFrame.getInstance(),
                            "<html>The Group:<b> " + sel_grp + " </b>is <b>EMPTY</b> !");
                    return;
                }

                //if local wlm already has the same list, skip re-creation
                String cur_wl = _WatchListModel.getWatchlistName();
                if (sel_grp.equals(cur_wl)) return;
                _WatchListModel = new WatchListModel(sel_grp, false);

                //based on current mode, draw different graph
                if (isBaselineMode()) {
                    final String first_symbol = _WatchListModel.getMembers().get(0);
                    _pnlPctGraph.plotByMode(GraphMode.BASELINE_MODE, _nCurTimeframe, _WatchListModel, first_symbol, _nEndIndex);
                } else
                    _pnlPctGraph.plotByMode(GraphMode.ORIGIN_MODE, _nCurTimeframe, _WatchListModel, "", _nEndIndex);
                _pnlPerformance.populate(_WatchListModel, isBaselineMode());
//TODO                _pnlRankingGraph.populateRankingTable(_WatchListModel);
                Props.PlotThumbnails.setValue(null, _WatchListModel.getMembers());
            }
        });
        north_pnl.add(_fldSearch); _fldSearch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                markSymbol();
            }
        });
        north_pnl.add(_btnSearch); _btnSearch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                markSymbol();
            }
        });
        north_pnl.add(_btnRewind); _btnRewind.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _nEndIndex = AppUtil.moveIndexByUnitTimeframe(_nEndIndex, Timeframe.Weekly, true);
                if (_nEndIndex > 0)//very unlikely to be negative
                    plotByTimeFrame(_nCurTimeframe);
                //do the same for table and thumbnails
                _pnlPerformance.populate(_WatchListModel, isBaselineMode(), _nEndIndex);
            }
        });
        north_pnl.add(_btnToday); _btnToday.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _nEndIndex = 0; plotByTimeFrame(_nCurTimeframe);
                _pnlPerformance.populate(_WatchListModel, isBaselineMode(), _nEndIndex);//both table and thumbnail
            }
        });
        north_pnl.add(_btnForward); _btnForward.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
//TODO move forward
//TODO do the same for table and thumbnails
            }
        });

        //mode selector, baseline symbol, symbol picker
        north_pnl.add(_btnMode);
        _btnMode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //toggle text, enable/disable field, buttons, redraw graph
                // mode switch implies full range and use SPY as baseline
                if (isBaselineMode()) {//baseline --> origin
                    _btnMode.setText(GraphMode.ORIGIN_MODE.toString());
                    _fldBaseSymbol.setEnabled(false);
                    _btnPick.setEnabled(false);
                } else {//origin --> baseline
//TODO: if SPY not in the watch list, temporary add to table and graph
                    _btnMode.setText(GraphMode.BASELINE_MODE.toString());
                    _fldBaseSymbol.setEnabled(true);
                    _WatchListModel.setBaselineSymbol(WatchListModel.DEFAULT_BASELINE_SYMBOL);
                    _WatchListModel.setCurSymbol(WatchListModel.DEFAULT_BASELINE_SYMBOL);
                    _fldBaseSymbol.setText(_WatchListModel.getBaselineSymbol());
                    _btnPick.setEnabled(true);
                }
                plotByTimeFrame(_nCurTimeframe);
                _pnlPerformance.populate(_WatchListModel, isBaselineMode());//re-calculate table percents
            }
        });
        north_pnl.add(_fldBaseSymbol);
        _fldBaseSymbol.setEnabled(false);
        _fldBaseSymbol.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String symbol = _fldBaseSymbol.getText().trim().toUpperCase();
                if (!isSymbolValid(symbol)) {
                    WidgetUtil.showWarning(MdiMainFrame.getInstance(),
                        ApolloConstants.APOLLO_BUNDLE.getString("mkt_15"));
//                                    MessageBox.messageBox(MdiMainFrame.getInstance(),
//                            FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
//                            ApolloConstants.APOLLO_BUNDLE.getString("mkt_15"),
//                            MessageBox.STYLE_OK, MessageBox.WARNING_MESSAGE);
                    return;
                }
                plotByTimeFrame(_nCurTimeframe);
                _WatchListModel.setBaselineSymbol(symbol);
                _pnlPerformance.populate(_WatchListModel, isBaselineMode());//re-calculate table percents
            }
        });
        north_pnl.add(_btnPick);
        _btnPick.setDisabledIcon(new DisabledIcon(FrameworkIcon.SELECT.getImage()));
        _btnPick.setEnabled(false);
        _btnPick.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String symbol = _fldBaseSymbol.getText().trim().toUpperCase();
                if (isSymbolValid(symbol)) {//add to watch list model, use it as current
                    try {
                        _WatchListModel.addSymbol(symbol);
                        _WatchListModel.setCurSymbol(symbol);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        WidgetUtil.showWarning(MdiMainFrame.getInstance(),
                            FrameworkConstants.FRAMEWORK_BUNDLE.getString("dme_txt_4") + e1.getMessage());
//                        MessageBox.messageBox(MdiMainFrame.getInstance(),
//                                FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
//                                ApolloConstants.APOLLO_BUNDLE.getString("dme_txt_4") + e1.getMessage(),
//                                MessageBox.STYLE_OK, MessageBox.WARNING_MESSAGE);
                    }
                    new PickSymbolDialog();
                }
            }
        });

        //links for different time frames
        north_pnl.add(_lnk1w = WidgetUtil.createHyperLink(WidgetUtil.LEVEL_2, ApolloConstants.APOLLO_BUNDLE.getString("mkt_05"), null));
        _lnk1w.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (_lnk1w.getFont() == Constants.HIGHLIGHT_FONT)
                    return;//already clicked before
                plotByTimeFrame(PerformanceTableModel.COLUMN_1_WEEK_PCT);
                resetLinkFont();
                _lnk1w.setFont(Constants.HIGHLIGHT_FONT);
            }
        });
        north_pnl.add(_lnk2w = WidgetUtil.createHyperLink(WidgetUtil.LEVEL_2, ApolloConstants.APOLLO_BUNDLE.getString("mkt_06"), null));
        _lnk2w.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (_lnk2w.getFont() == Constants.HIGHLIGHT_FONT)
                    return;//already clicked before
                plotByTimeFrame(PerformanceTableModel.COLUMN_2_WEEK_PCT);
                resetLinkFont();
                _lnk2w.setFont(Constants.HIGHLIGHT_FONT);
            }
        });
        north_pnl.add(_lnk1m = WidgetUtil.createHyperLink(WidgetUtil.LEVEL_2, ApolloConstants.APOLLO_BUNDLE.getString("mkt_07"), null));
        _lnk1m.setFont(Constants.HIGHLIGHT_FONT);//default time frame
        _lnk1m.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (_lnk1m.getFont() == Constants.HIGHLIGHT_FONT)
                    return;//already clicked before
                plotByTimeFrame(PerformanceTableModel.COLUMN_1_MONTH_PCT);
                resetLinkFont();
                _lnk1m.setFont(Constants.HIGHLIGHT_FONT);
            }
        });
        north_pnl.add(_lnk2m = WidgetUtil.createHyperLink(WidgetUtil.LEVEL_2, ApolloConstants.APOLLO_BUNDLE.getString("mkt_01"), null));
        _lnk2m.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (_lnk2m.getFont() == Constants.HIGHLIGHT_FONT)
                    return;//already clicked before
                plotByTimeFrame(PerformanceTableModel.COLUMN_2_MONTH_PCT);
                resetLinkFont();
                _lnk2m.setFont(Constants.HIGHLIGHT_FONT);
            }
        });
        north_pnl.add(_lnk3m = WidgetUtil.createHyperLink(WidgetUtil.LEVEL_2, ApolloConstants.APOLLO_BUNDLE.getString("mkt_08"), null));
        _lnk3m.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (_lnk3m.getFont() == Constants.HIGHLIGHT_FONT)
                    return;//already clicked before
                plotByTimeFrame(PerformanceTableModel.COLUMN_3_MONTH_PCT);
                resetLinkFont();
                _lnk3m.setFont(Constants.HIGHLIGHT_FONT);
            }
        });
        north_pnl.add(_lnk6m = WidgetUtil.createHyperLink(WidgetUtil.LEVEL_2, ApolloConstants.APOLLO_BUNDLE.getString("mkt_09"), null));
        _lnk6m.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (_lnk6m.getFont() == Constants.HIGHLIGHT_FONT)
                    return;//already clicked before
                plotByTimeFrame(PerformanceTableModel.COLUMN_6_MONTH_PCT);
                resetLinkFont();
                _lnk6m.setFont(Constants.HIGHLIGHT_FONT);
            }
        });
        north_pnl.add(_lnk1y = WidgetUtil.createHyperLink(WidgetUtil.LEVEL_2, ApolloConstants.APOLLO_BUNDLE.getString("mkt_10"), null));
        _lnk1y.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (_lnk1y.getFont() == Constants.HIGHLIGHT_FONT)
                    return;//already clicked before
                plotByTimeFrame(PerformanceTableModel.COLUMN_1_YEAR_PCT);
                resetLinkFont();
                _lnk1y.setFont(Constants.HIGHLIGHT_FONT);
            }
        });
        north_pnl.add(_lnkYtd = WidgetUtil.createHyperLink(WidgetUtil.LEVEL_2, ApolloConstants.APOLLO_BUNDLE.getString("mkt_11"), null));
        _lnkYtd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (_lnkYtd.getFont() == Constants.HIGHLIGHT_FONT)
                    return;//already clicked before
                plotByTimeFrame(PerformanceTableModel.COLUMN_YTD_PCT);
                resetLinkFont();
                _lnkYtd.setFont(Constants.HIGHLIGHT_FONT);
            }
        });
        north_pnl.add(_lnkCustom = WidgetUtil.createHyperLink(WidgetUtil.LEVEL_2, ApolloConstants.APOLLO_BUNDLE.getString("mkt_13"), null));
        WidgetUtil.attachToolTip(_lnkCustom, ApolloConstants.APOLLO_BUNDLE.getString("mkt_40"), SwingConstants.RIGHT, SwingConstants.TOP);
        _lnkCustom.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                plotByTimeFrame(PerformanceTableModel.COLUMN_CUSTOM_PCT);
                resetLinkFont();
                _lnkCustom.setFont(Constants.HIGHLIGHT_FONT);

                //update custom column percentage in table
                int origin_index = _pnlPctGraph.calcOrigin(PerformanceTableModel.COLUMN_CUSTOM_PCT, _WatchListModel, _nEndIndex);
                if (origin_index == -1) return;
                _pnlPerformance.updateCustomPercents(origin_index, _WatchListModel);
            }
        });
        north_pnl.add(_lnkMax = WidgetUtil.createHyperLink(WidgetUtil.LEVEL_2, ApolloConstants.APOLLO_BUNDLE.getString("mkt_12"), null));
        _lnkMax.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (_lnkMax.getFont() == Constants.HIGHLIGHT_FONT)
                    return;//already clicked before
                plotByTimeFrame(FULL_RANGE);
                resetLinkFont();
                _lnkMax.setFont(Constants.HIGHLIGHT_FONT);
            }
        });
        add(north_pnl, "dock north");

        //center - graph and table via vertical splitter on the left, thumbnail to the right via horizontal splitter
        JSplitPane vsplit_pnl = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        vsplit_pnl.setDividerLocation(450);
        vsplit_pnl.setContinuousLayout(true);
        vsplit_pnl.setTopComponent(_pnlPctGraph = new PercentGraphPanel());
        vsplit_pnl.setBottomComponent(_pnlPerformance = new PerformancePanel());
        //right - thumbnail
        JSplitPane hsplit_pnl = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        hsplit_pnl.setDividerLocation(600); hsplit_pnl.setContinuousLayout(true);
        hsplit_pnl.setLeftComponent(vsplit_pnl);
        ThumbnailPanel thumb_pnl = new ThumbnailPanel(false);
        hsplit_pnl.setRightComponent(thumb_pnl); _pnlPerformance.setThumbnailPanel(thumb_pnl);
        add(hsplit_pnl, "dock center");

        //use watchlist already loaded in MainModel to avoid reloading
        _WatchListModel = MainModel.getInstance().getWatchListModel();//new WatchListModel(initial_group);
        String wl_name = _WatchListModel.getWatchlistName();

        //if name exist in combo, select combo item
        int index = WidgetUtil.findComboItem(_cmbGrpSelector, wl_name);
        if (index != -1)
            _cmbGrpSelector.setSelectedIndex(index);
        else {
            String first_wl = (String)_cmbGrpSelector.getSelectedItem();
            if (first_wl == null) //no watch list at all
                return;
            _WatchListModel = new WatchListModel(first_wl, false);
        }
        ArrayList<String> members = _WatchListModel.getMembers();
        if (members.size() > 0) {
            final String first_symbol = members.get(0);
            _pnlPctGraph.setEmphasizedSymbol(first_symbol);
            _pnlPctGraph.plotByMode(GraphMode.ORIGIN_MODE, _nCurTimeframe, _WatchListModel, "", _nEndIndex);
            _pnlPctGraph.emphasizeSelectedSymbol();
            _pnlPerformance.populate(_WatchListModel, isBaselineMode());
        }

        //event listener
        Props.addWeakPropertyChangeListener(Props.RestoreAllSymbols, this);//from PerformancePanel
        Props.addWeakPropertyChangeListener(Props.TimeFrameChanged, this);
        Props.addWeakPropertyChangeListener(Props.PlotWatchlist, this);
    }
    public void propertyChange(PropertyChangeEvent evt) {
        if (!isVisible())
            return;
        Props prop = (Props) evt.getSource();
        switch (prop) {
            case RestoreAllSymbols:
                _sHiddenSymbols.clear();
                _pnlPerformance.populate(_WatchListModel, isBaselineMode());
                plotByTimeFrame(_nCurTimeframe);
                break;
            case TimeFrameChanged://plot new time frame chart, highlight hyperlink
                _nCurTimeframe = (Integer)prop.getValue();
                plotByTimeFrame(_nCurTimeframe);
                highlightTimeframeLink();
                break;
            case PlotWatchlist:
                plotPerformance((WatchListModel)prop.getValue());
                break;
        }
    }
//TODO for now only ones start with  "ETF - " and "GRP - ", later make them special
    public void initGroupSelector() {
        //prefill with all the watch list groups
        ArrayList<String> groups = GroupStore.getInstance().getGroupNames();
        _cmlGrpSelector.removeAllElements();
        for (int idx = 0; idx < groups.size(); idx++) {
//            if (groups.get(idx).startsWith("ETF - ") || groups.get(idx).startsWith("GRP - "))
                _cmlGrpSelector.addElement(groups.get(idx));
        }
    }
    public void handleSymbolSelectionChange(String symbol) {
        HashMap<String, MarketInfo> mkis = _WatchListModel.getMarketInfoMap();
        MarketInfo mki = mkis.get(symbol);
        if (mki == null)//no such symbol, skip
            return;
        _pnlPctGraph.setEmphasizedSymbol(symbol);
        _pnlPctGraph.emphasizeSelectedSymbol();//make it bold
    }
    public void handleSymbolRemoval(String symbol) {
        _pnlPctGraph.removeSeries(symbol);
        _sHiddenSymbols.add(symbol);
    }

    //----- private methods -----
    private boolean isBaselineMode() { return _btnMode.getText().equals(GraphMode.BASELINE_MODE.toString()); }
    private boolean isSymbolValid(String symbol) {
        //check symbol exist, empty, validity
        File quote_folder = new File(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE);
        String[] symbol_list = quote_folder.list();
        boolean found = false;
        for (String name : symbol_list) {
            int idx = name.indexOf(FrameworkConstants.EXTENSION_QUOTE);
            if (idx < 0) continue;//skip non-quote files
            String sym = name.substring(0, idx);
            if (symbol.equals(sym))
                found = true;
        }
        if (symbol.equals("") || !found)
            return false;
        return true;
    }
    //false = un-successful
    private boolean plotByTimeFrame(int timeframe_code) {
        String baseline_symbol = _fldBaseSymbol.getText().trim().toUpperCase();
        if (isBaselineMode()) {
            if (!isSymbolValid(baseline_symbol)) {
                WidgetUtil.showMessageNoEdt("Symbol Empty or Non-Exist !");
                return false;
            }
        }
        _pnlPctGraph.plotByMode(isBaselineMode() ? GraphMode.BASELINE_MODE : GraphMode.ORIGIN_MODE,
                _nCurTimeframe = timeframe_code, _WatchListModel, baseline_symbol, _nEndIndex);
        _pnlPerformance.setCurrentTimeframe(_nCurTimeframe);

        //remove hidden series
        for (String sym : _sHiddenSymbols)
            _pnlPctGraph.removeSeries(sym);
        return true;
    }
    private void plotPerformance(WatchListModel wlm) {//use the same time frame
        String baseline_symbol = _fldBaseSymbol.getText().trim().toUpperCase();
        _pnlPctGraph.plotByMode(isBaselineMode() ? GraphMode.BASELINE_MODE : GraphMode.ORIGIN_MODE,
                _nCurTimeframe, wlm, baseline_symbol, _nEndIndex);
    }
    //helper to set links font
    private void resetLinkFont() {
        _lnk1w.setFont(Constants.LINK_FONT_NORMAL);
        _lnk2w.setFont(Constants.LINK_FONT_NORMAL);
        _lnk1m.setFont(Constants.LINK_FONT_NORMAL);
        _lnk2m.setFont(Constants.LINK_FONT_NORMAL);
        _lnk3m.setFont(Constants.LINK_FONT_NORMAL);
        _lnk6m.setFont(Constants.LINK_FONT_NORMAL);
        _lnk1y.setFont(Constants.LINK_FONT_NORMAL);
        _lnkYtd.setFont(Constants.LINK_FONT_NORMAL);
        _lnkMax.setFont(Constants.LINK_FONT_NORMAL);
        _lnkCustom.setFont(Constants.LINK_FONT_NORMAL);
    }
    //make time frame link bold based on _nCurrentTimeframe
    private void highlightTimeframeLink() {
        resetLinkFont();
        switch (_nCurTimeframe) {
            case PerformanceTableModel.COLUMN_1_WEEK_PCT: _lnk1w.setFont(Constants.HIGHLIGHT_FONT); break;
            case PerformanceTableModel.COLUMN_2_WEEK_PCT: _lnk2w.setFont(Constants.HIGHLIGHT_FONT); break;
            case PerformanceTableModel.COLUMN_1_MONTH_PCT: _lnk1m.setFont(Constants.HIGHLIGHT_FONT); break;
            case PerformanceTableModel.COLUMN_2_MONTH_PCT: _lnk2m.setFont(Constants.HIGHLIGHT_FONT); break;
            case PerformanceTableModel.COLUMN_3_MONTH_PCT: _lnk3m.setFont(Constants.HIGHLIGHT_FONT); break;
            case PerformanceTableModel.COLUMN_6_MONTH_PCT: _lnk6m.setFont(Constants.HIGHLIGHT_FONT); break;
            case PerformanceTableModel.COLUMN_1_YEAR_PCT: _lnk1y.setFont(Constants.HIGHLIGHT_FONT); break;
            case PerformanceTableModel.COLUMN_YTD_PCT: _lnkYtd.setFont(Constants.HIGHLIGHT_FONT); break;
            case PerformanceTableModel.COLUMN_CUSTOM_PCT: _lnkCustom.setFont(Constants.HIGHLIGHT_FONT); break;
        }
    }
    private void markSymbol() {
        String sym = _fldSearch.getText().toUpperCase();
        _pnlPerformance.findSymbol(sym);
        _pnlPctGraph.setEmphasizedSymbol(sym);
        _fldSearch.setText(sym);
    }

    //----- inner classes -----
    //use watch list model's current symbol as initial selection
//TODO drive change directly within this non-modal dialog
    private class PickSymbolDialog extends JDialog {
        private PickSymbolDialog() {
            super(MdiMainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("mkt_16"), true);
            JPanel content = new JPanel(new MigLayout());
            JPanel cen_pnl = new JPanel(new MigLayout("flowy, wrap 5"));
            ArrayList<String> list = _WatchListModel.getMembers();
            ButtonGroup grp = new ButtonGroup();//only 1 is selected
            String cur_symbol = _WatchListModel.getCurSymbol();
            _rdoSymbols = new JRadioButton[list.size()];
            for (int i = 0; i < list.size(); i++) {
                String symbol = list.get(i);
                _rdoSymbols[i] = new JRadioButton(symbol);
                if (symbol.equals(cur_symbol))
                    _rdoSymbols[i].setSelected(true);
                cen_pnl.add(_rdoSymbols[i]);
                grp.add(_rdoSymbols[i]);
            }
            content.add(cen_pnl, "dock center");

            //south - buttons
            JPanel btn_pnl = new JPanel(new MigLayout("", "push[][]push"));
            JButton ok = new JButton(Constants.COMPONENT_BUNDLE.getString("ok"));
            ok.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    for (int i=0; i<_rdoSymbols.length; i++) {
                        if (_rdoSymbols[i].isSelected()) {
                            String symbol = _rdoSymbols[i].getText();
                            _WatchListModel.setCurSymbol(symbol);
                            dispose();
                            _pnlPctGraph.plotByMode(GraphMode.BASELINE_MODE, _nCurTimeframe, _WatchListModel, symbol, _nEndIndex);
                            _fldBaseSymbol.setText(_WatchListModel.getCurSymbol());
                            _WatchListModel.setBaselineSymbol(symbol);
                            _pnlPerformance.populate(_WatchListModel, isBaselineMode());//re-calculate table percents
                        }
                    }
                }
            });
            btn_pnl.add(ok);
            JButton cancel = new JButton(Constants.COMPONENT_BUNDLE.getString("cancel"));
            cancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });
            btn_pnl.add(cancel);
            content.add(btn_pnl, "dock south");
            setContentPane(content);
            WidgetUtil.setDialogProperties(this, new Dimension(0,0), false, MdiMainFrame.getInstance(), WindowConstants.DISPOSE_ON_CLOSE);
        }

        private JRadioButton[] _rdoSymbols;
    }

    //----- accessors -----

    //----- variables -----
    private JComboBox<String> _cmbGrpSelector;//by default select first item
    private DefaultComboBoxModel<String> _cmlGrpSelector = new DefaultComboBoxModel<>();
    private JTextField _fldSearch = new JTextField(5);
    private JButton _btnSearch = WidgetUtil.createIconButton("Find Symbol", FrameworkIcon.SEARCH);
    private JButton _btnRewind = WidgetUtil.createIconButton("Rewind by one week", FrameworkIcon.REWIND);
    private JButton _btnForward = WidgetUtil.createIconButton("Fast Forward by one week", FrameworkIcon.FORWARD);
    private JButton _btnToday = WidgetUtil.createIconButton("Reset to Today", FrameworkIcon.TODAY);
    private JToggleButton _btnMode = new JToggleButton(GraphMode.ORIGIN_MODE.toString());
    private NameField _fldBaseSymbol = new NameField(5);
    private JButton _btnPick = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("mkt_14"), FrameworkIcon.SELECT);
    private JXHyperlink _lnk1w, _lnk2w, _lnk1m, _lnk2m, _lnk3m, _lnk6m, _lnk1y, _lnkYtd, _lnkMax, _lnkCustom;
    private PercentGraphPanel _pnlPctGraph;
    private PerformancePanel _pnlPerformance;
    private WatchListModel _WatchListModel;//master within frame and sub-panels
    private int _nCurTimeframe = PerformanceTableModel.COLUMN_1_MONTH_PCT;
    private ArrayList<String> _sHiddenSymbols = new ArrayList<>();
    private int _nEndIndex = 0; //track end data point for plotting and calculating performance

    //----- literals -----
    public final static int FULL_RANGE = -1;
}