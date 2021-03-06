package org.marketsuite.chart.candle;

import org.marketsuite.component.field.NameField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.spinner.IntegerSpinner;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.market.MarketUtil;
import org.marketsuite.framework.model.DivergenceOption;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.Fundamental;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.type.CandleSignal;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.model.type.ZoomLevel;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.main.MainModel;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.field.NameField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.spinner.IntegerSpinner;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.market.MarketUtil;
import org.marketsuite.framework.model.DivergenceOption;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.Fundamental;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.type.CandleSignal;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.model.type.ZoomLevel;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.main.MainModel;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;

//container for daily candlestick charts
public class DailyCandlePanel extends JPanel {
    public DailyCandlePanel() {
        setLayout(new MigLayout("insets 0"));

        //title strip, symbol entry
        JPanel north_pnl = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER, new MigLayout("", "5[][]20[][]push[]50[][][]30[][][]30[][][]5[]", "3[]3"));
        north_pnl.add(_fldSymbol); _fldSymbol.setFont(FrameworkConstants.MEDIUM_SMALL_FONT);
        WidgetUtil.attachLeftBalloonTip(_fldSymbol, ApolloConstants.APOLLO_BUNDLE.getString("cdl_sym"));
        _fldSymbol.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String symbol = _fldSymbol.getText().toUpperCase();
                _fldSymbol.select(0, symbol.length());//highlight symbol for easy typing next one
                if (symbol.equalsIgnoreCase(_sCurrentSymbol)) return;//re-type, no change
//TODO if error, popup dialog    DONT draw
                plot(symbol);
            }
        });
        north_pnl.add(_fldOverlaySymbol);
        WidgetUtil.attachLeftBalloonTip(_fldOverlaySymbol, ApolloConstants.APOLLO_BUNDLE.getString("cdl_ovly"));
        _fldOverlaySymbol.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //when empty, clear overlay
                String ovly_sym = _fldOverlaySymbol.getText();
                if (ovly_sym.equals("")) {
                    _pnlDailyCandle.clearOverlayQuotes();
                    return;
                }
                //without main symbol, entry not allowed
                if (_sCurrentSymbol == null || _mainQuotes.size() == 0) {
                    WidgetUtil.showWarning(ApolloConstants.APOLLO_BUNDLE.getString("cdl_needmain"));
                    _fldOverlaySymbol.setText(""); return;
                }

                //prepare quote array of the same length as main symbol;s
                ovly_sym = ovly_sym.toUpperCase();
                try {
                    int num_days = _mainQuotes.size() + 2;//FrameworkConstants.MARKET_QUOTE_LENGTH;
                    _overlayQuotes = DataUtil.readHistory(ovly_sym, num_days).getQuote();
                    if (num_days > _overlayQuotes.size()) num_days = _overlayQuotes.size();//use smaller of two
                    try {
                        DataUtil.adjustForSplits(_overlayQuotes, num_days - 1);
                    } catch (IOException e1) {
                        //ok without split file
                        LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("cdl_spl") + " "
                                + ovly_sym, LoggingSource.DAILY_CANDLE_CHART);
                    }
                    _pnlDailyCandle.plotOverlay(_overlayQuotes);
                } catch (IOException rqe) {//can't read daily quote
                    WidgetUtil.showWarning(ApolloConstants.APOLLO_BUNDLE.getString("wc_01") + " " + ovly_sym);
                }
            }
        });
        north_pnl.add(_btnPrev);
        _btnPrev.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showNextPrev(false);
            }
        });
        north_pnl.add(_btnNext);
        _btnNext.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showNextPrev(true);
            }
        });
        north_pnl.add(_lblTitle); _lblTitle.setFont(FrameworkConstants.MEDIUM_SMALL_FONT);
        north_pnl.add(_btnBolinger);
        _btnBolinger.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { _pnlDailyCandle.toggleBB(); }
        });
        north_pnl.add(_btnStochastic);
        _btnStochastic.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { _pnlDailyCandle.toggleStochastic(); } });
        north_pnl.add(_btnMacd);
        _btnMacd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { _pnlDailyCandle.toggleMacd(); } });
        north_pnl.add(_btnEarning);
        _btnEarning.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { _pnlDailyCandle.toggleEarning(); }
        });
        north_pnl.add(_btnPivot);
        _btnPivot.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _pnlDailyCandle.togglePivot();
                boolean pressed = _btnPivot.isSelected();
                _spnPivotInterval.setEnabled(pressed);

                //automatically expands spaces to the right
                _pnlDailyCandle.setRightMargin(pressed ? 35 : 0);
            }
        });
        north_pnl.add(_spnPivotInterval); _spnPivotInterval.setEnabled(false);
        _spnPivotInterval.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {//when user changes setting
                _pnlDailyCandle.setPivotInterval(_spnPivotInterval.getValue()); }
        });

        //candle options
        north_pnl.add(_btnOption); _btnOption.setOpaque(false);
        _btnOption.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { new CandleOptionDialog(); }
        });
        north_pnl.add(_btnCandleSig);
        _btnCandleSig.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { SignalPickerDialog.getInstance().toggleVisibility(); }
        });
        north_pnl.add(_btnPad);
        _btnPad.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { _pnlDailyCandle.moveDatapad(); }
        });
        ZoomLevel[] show_levels = new ZoomLevel[4];
        show_levels[0] = ZoomLevel.Month1; show_levels[1] = ZoomLevel.Month2;
        show_levels[2] = ZoomLevel.Month3; show_levels[3] = ZoomLevel.Month6;
        north_pnl.add(_cmbZoom = new JComboBox<>(show_levels)); _cmbZoom.setSelectedIndex(2);//default 2 month
        _cmbZoom.setFocusable(false);
        WidgetUtil.attachRightBalloonTip(_cmbZoom, ApolloConstants.APOLLO_BUNDLE.getString("cdl_zoom"));
        _cmbZoom.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED) return;
                if (_sCurrentSymbol == null) return;//empty chart
                _pnlDailyCandle.clearOverlayQuotes(); _fldOverlaySymbol.setText("");
                plot(_sCurrentSymbol);
            }
        });
        add(north_pnl, "dock north");
//May use toolbar
//        _tbAnalysis.add(_btnBolinger); _tbAnalysis.add(_btnMacd); _tbAnalysis.add(_btnStochastic); _tbAnalysis.addSeparator();
//        _tbAnalysis.add(_btnEarning); _tbAnalysis.add(_btnPivot); _tbAnalysis.addSeparator();
//        _tbAnalysis.add(_btnCandleSig); _tbAnalysis.add(_btnOption); _tbAnalysis.add(_btnPad);
//        add(_tbAnalysis, "dock east");
        add(_pnlDailyCandle = new CandleGraphPanel(), "dock center");
    }

    //----- public methods -----
    //all plot() methods will use MarketInfo
    public void plot(ArrayList<CandleSignal> candle_signals) {//use current symbol if exists
        if (_sCurrentSymbol == null) return;
        signalSettings = candle_signals;
        plot(_sCurrentSymbol, signalSettings);
    }
    public void plot(String symbol) { //use saved signals
        plot(symbol, signalSettings);
    }
    private void plot(String symbol, ArrayList<CandleSignal> candle_signals) {
        //read company's full name
        StringBuilder sb = new StringBuilder(symbol);
        Fundamental fundamental = MainModel.getInstance().getFundamentals().get(symbol);
        if (fundamental != null)//cant'find
            sb.append(": ").append(fundamental.getFullName());
        _lblTitle.setText(sb.toString()); _fldSymbol.setText("");
        _sCurrentSymbol = symbol;

        //if watch list model has this, don't compute
        MarketInfo mki = MainModel.getInstance().getWatchListModel().getMarketInfo(symbol);
        if (mki == null) {
            try {
                mki = MarketUtil.calcMarketInfo(symbol, FrameworkConstants.MARKET_QUOTE_LENGTH, new DivergenceOption(5, 90, 3));
            } catch (Exception e) {//unable to calculate mki, ok to continue w partial result
            }
        }
        if (mki == null) {
            String msg = ApolloConstants.APOLLO_BUNDLE.getString("qp_rdmki") + " " + symbol;
            WidgetUtil.showWarning(msg); //TODO clear plot
            LogMessage.logSingleMessage(msg, LoggingSource.DAILY_CANDLE_CHART);
            return;
        }
        int num_quotes = 60;
        switch ((ZoomLevel)_cmbZoom.getSelectedItem()) {
            case Month1: num_quotes = 20; break;
            case Month2: num_quotes = 40; break;
            case Month3: num_quotes = 60; break;
            case Month6: num_quotes = 120; break;
            case Month9: num_quotes = 180; break;
            case Year1: num_quotes = 240; break;
        }
        _mainQuotes = new ArrayList<>();
        int size = mki.getFund().getSize();
        if (num_quotes > size) num_quotes = size - 1;
        for (int idx = 0; idx < num_quotes; idx++)
            _mainQuotes.add(mki.getFund().getQuote().get(idx));
        _pnlDailyCandle.plot(mki, _mainQuotes, candle_signals);
    }

    //----- private methods -----
    //gather quote array from MarketInfo based on current date range
    private void showNextPrev(boolean next) {
        WatchListModel wlm = MainModel.getInstance().getWatchListModel();
        if (wlm == null) {
            LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("wc_04"), LoggingSource.WEEKLY_CHART);
            return;
        }
        String sym = wlm.getNextPrevSymbol(next);
        if (sym == null) {
            LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("wc_05") + " " + sym, LoggingSource.WEEKLY_CHART);
            return;
        }
        plot(sym);
    }

    //----- variables -----
    protected JLabel _lblTitle = new JLabel();
    private String _sCurrentSymbol;
    private NameField _fldSymbol = new NameField(4);
    private NameField _fldOverlaySymbol = new NameField(4);//draw line
    private JButton _btnPrev = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("btn_lbl_3"), FrameworkIcon.ARROW_3D_LEFT);
    private JButton _btnNext = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("btn_lbl_2"), FrameworkIcon.ARROW_3D_RIGHT);
    private JToggleButton _btnBolinger = WidgetUtil.createToggleButton(FrameworkIcon.CHART_BOLLINGER, ApolloConstants.APOLLO_BUNDLE.getString("cdl_bol"), null);
    private JToggleButton _btnStochastic = WidgetUtil.createToggleButton(FrameworkIcon.CHART_STO, ApolloConstants.APOLLO_BUNDLE.getString("cdl_sto"), null);
    private JToggleButton _btnMacd = WidgetUtil.createToggleButton(FrameworkIcon.CHART_MACD, ApolloConstants.APOLLO_BUNDLE.getString("cdl_macd"), null);
    private JToggleButton _btnEarning = WidgetUtil.createToggleButton(FrameworkIcon.DOLLAR_16, ApolloConstants.APOLLO_BUNDLE.getString("cdl_earn"), null);
    private JToggleButton _btnPivot = WidgetUtil.createToggleButton(FrameworkIcon.PIVOT, ApolloConstants.APOLLO_BUNDLE.getString("cdl_pivot"), null);
    private IntegerSpinner _spnPivotInterval = new IntegerSpinner("", 5, 5, 50, 5, "", null);
    private JButton _btnPad = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("cdl_pad"), FrameworkIcon.REPORT);
    private JButton _btnOption = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("cdl_option"), FrameworkIcon.SETTING);
    private JButton _btnCandleSig = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("cdl_sig_onoff"), FrameworkIcon.CANDLE_SAMPLE);
    private JComboBox<ZoomLevel> _cmbZoom;
    private CandleGraphPanel _pnlDailyCandle;
    private ArrayList<FundQuote> _mainQuotes, _overlayQuotes;
    private ArrayList<CandleSignal> signalSettings;
//    private JToolBar _tbAnalysis = new JToolBar(JToolBar.VERTICAL);
}
