package org.marketsuite.chart.candle;

import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.field.NameField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;

//container for daily candlestick charts
public class FractalPanel extends JPanel {
    public FractalPanel() {
        setLayout(new MigLayout("insets 0"));

        //title strip, symbol entry
        JPanel north_pnl = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER, new MigLayout("", "5[][]20[][]push[]push[]5[]10[]5", "3[]3"));
        north_pnl.add(_fldSymbol);
        _fldSymbol.setFont(FrameworkConstants.MEDIUM_SMALL_FONT);
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
                    _fldOverlaySymbol.setText("");
                    return;
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
        north_pnl.add(_lblTitle);
        _lblTitle.setFont(FrameworkConstants.MEDIUM_SMALL_FONT);
        north_pnl.add(_btnMa);
        _btnMa.setOpaque(false);
        _btnMa.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _bShowMa = !_bShowMa;
                _pnlDailyCandle.setShowMa(_bShowMa);
            }
        });
        north_pnl.add(_btnFractal);
        _btnFractal.setOpaque(false);
        _btnFractal.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _bShowFractal = !_bShowFractal;
                _pnlDailyCandle.setShowFractal(_bShowFractal);
            }
        });
        ZoomLevel[] show_levels = new ZoomLevel[4];
        show_levels[0] = ZoomLevel.Month1;
        show_levels[1] = ZoomLevel.Month2;
        show_levels[2] = ZoomLevel.Month3;
        show_levels[3] = ZoomLevel.Month6;
        north_pnl.add(_cmbZoom = new JComboBox<>(show_levels));
        _cmbZoom.setSelectedIndex(2);//default 2 month
        _cmbZoom.setFocusable(false);
        WidgetUtil.attachRightBalloonTip(_cmbZoom, ApolloConstants.APOLLO_BUNDLE.getString("cdl_zoom"));
        _cmbZoom.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED) return;
                if (_sCurrentSymbol == null) return;//empty chart
                _pnlDailyCandle.clearOverlayQuotes();
                _fldOverlaySymbol.setText("");
                plot(_sCurrentSymbol);
            }
        });
        north_pnl.add(_btnPad);
        _btnPad.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _pnlDailyCandle.moveDatapad();
            }
        });
        add(north_pnl, "dock north");
        add(_pnlDailyCandle = new BaseCandlePanel(), "dock center");
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
        _lblTitle.setText(sb.toString());
        _fldSymbol.setText("");
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
        switch ((ZoomLevel) _cmbZoom.getSelectedItem()) {
            case Month1:
                num_quotes = 20;
                break;
            case Month2:
                num_quotes = 40;
                break;
            case Month3:
                num_quotes = 60;
                break;
            case Month6:
                num_quotes = 120;
                break;
            case Month9:
                num_quotes = 180;
                break;
            case Year1:
                num_quotes = 240;
                break;
        }
        _mainQuotes = new ArrayList<>();
        int size = mki.getFund().getSize();
        if (num_quotes > size) num_quotes = size - 1;
        for (int idx = 0; idx < num_quotes; idx++)
            _mainQuotes.add(mki.getFund().getQuote().get(idx));

        //compute moving avg, covert quotes into float array
//        float[] prices = new float[size];
//        for (int idx = 0; idx < size; idx++)
//            prices[idx] = _mainQuotes.get(idx).getClose();
//        EMA ema8 = new EMA(8, prices);//T line
//        SMA sma50 = new SMA(50, prices);//10 week = 50 day
//        SMA sma200 = new SMA(200, prices);//40 week = 200 day
//        float[] vol_avg = IndicatorUtil.calcVolumeAverage(_mainQuotes, 4);
        _pnlDailyCandle.plot(mki, _mainQuotes, candle_signals);
//        _pnlDailyCandle.plot(_mainQuotes, sma50.getSma(), sma200.getSma(), ema8.getEma(), vol_avg, candle_signals);
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
    private JButton _btnPad = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("cdl_pad"), FrameworkIcon.REPORT);
    private JButton _btnMa = WidgetUtil.createIconButton("Show / Hide Moving Averages", FrameworkIcon.CHART_MACD);
    private JButton _btnFractal = WidgetUtil.createIconButton("Show / Hide Fractals", FrameworkIcon.CHART_STO);
    private JComboBox<ZoomLevel> _cmbZoom;
    private BaseCandlePanel _pnlDailyCandle;
    private ArrayList<FundQuote> _mainQuotes, _overlayQuotes;
    private ArrayList<CandleSignal> signalSettings;
    private boolean _bShowMa, _bShowFractal;
}