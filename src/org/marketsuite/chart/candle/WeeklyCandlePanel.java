package org.marketsuite.chart.candle;

import org.marketsuite.component.field.NameField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.market.MarketUtil;
import org.marketsuite.framework.model.*;
import org.marketsuite.framework.model.indicator.EMA;
import org.marketsuite.framework.model.indicator.SMA;
import org.marketsuite.framework.model.quote.WeeklyQuote;
import org.marketsuite.framework.model.type.CandleSignal;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.model.type.ZoomLevel;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.IndicatorUtil;
import org.marketsuite.main.MainModel;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.market.MarketUtil;
import org.marketsuite.framework.model.indicator.SMA;
import org.marketsuite.framework.model.type.CandleSignal;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.main.MainModel;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;

import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;

//container for daily candlestick charts
public class WeeklyCandlePanel extends JPanel {
    public WeeklyCandlePanel() {
        setLayout(new MigLayout("insets 0"));

        //title strip, symbol entry
        JPanel north_pnl = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER, new MigLayout("", "5[]10[]5[]push[]20[]5[]5[]5[]push[]5[]10[]5", "3[]3"));
        north_pnl.add(_fldSymbol); _fldSymbol.setFont(FrameworkConstants.MEDIUM_SMALL_FONT);
        WidgetUtil.attachLeftBalloonTip(_fldSymbol, ApolloConstants.APOLLO_BUNDLE.getString("cdl_sym"));
        _fldSymbol.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String symbol = _fldSymbol.getText().toUpperCase();
                _fldSymbol.select(0, symbol.length());//highlight symbol for easy typing next one
                if (symbol.equalsIgnoreCase(_sCurrentSymbol)) return;//re-type, no change
                plot(symbol);
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

        //show/hide different overlay
        north_pnl.add(_btnRating);
        _btnRating.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _pnlWeeklyCandle.toggleRating();
            }
        });
        north_pnl.add(_btnBolinger);
        _btnBolinger.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _pnlWeeklyCandle.toggleBB();
            }
        });
        north_pnl.add(_btnStochastic);
        _btnStochastic.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { _pnlWeeklyCandle.toggleStochastic(); } });
        north_pnl.add(_btnMacd);
        _btnMacd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { _pnlWeeklyCandle.toggleMacd(); } });

        //chart options
        north_pnl.add(_btnOption); _btnOption.setOpaque(false);
        _btnOption.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new CandleOptionDialog();
            }
        });
        north_pnl.add(_btnSigOn);
        _btnSigOn.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) {
            SignalPickerDialog.getInstance().toggleVisibility(); } });
        north_pnl.add(_btnPad);
        _btnPad.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { _pnlWeeklyCandle.moveDatapad(); } });

        //zoom levels
        ZoomLevel[] show_levels = new ZoomLevel[6];
        show_levels[0] = ZoomLevel.Month6;
        show_levels[1] = ZoomLevel.Year1; show_levels[2] = ZoomLevel.Year2;
        show_levels[3] = ZoomLevel.Year3; show_levels[4] = ZoomLevel.Year5;
        show_levels[5] = ZoomLevel.Year8;
        north_pnl.add(_cmbZoom = new JComboBox<>(show_levels)); //default 6 month
        _cmbZoom.setFocusable(false); _cmbZoom.setSelectedIndex(1);
        WidgetUtil.attachRightBalloonTip(_cmbZoom, ApolloConstants.APOLLO_BUNDLE.getString("cdl_zoom"));
        _cmbZoom.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED) return;
                if (_sCurrentSymbol == null) return;//empty chart
                plot(_sCurrentSymbol);
            }
        });
        add(north_pnl, "dock north");
        add(_pnlWeeklyCandle = new CandleGraphPanel(), "dock center");
    }

    //----- public methods -----
    public void plot(ArrayList<CandleSignal> candle_signals) {//use current symbol if exists
        if (_sCurrentSymbol == null) return;
        signalSettings = candle_signals;
        plot(_sCurrentSymbol, signalSettings);
    }
    public void plot(String symbol) { //use saved signals
        plot(symbol, signalSettings);
    }
    public void plot(String symbol, ArrayList<CandleSignal> candle_signals) {
        //read company's full name
        StringBuilder sb = new StringBuilder(symbol);
        Fundamental fundamental = MainModel.getInstance().getFundamentals().get(symbol);
        if (fundamental != null)//cant'find
            sb.append(": ").append(fundamental.getFullName());
        _lblTitle.setText(sb.toString()); _fldSymbol.setText("");
        _sCurrentSymbol = symbol;

        //if watch list model has this, don't compute
        int num_quotes = 450;//1 year or less
        switch ((ZoomLevel)_cmbZoom.getSelectedItem()) {
            case Year2: num_quotes = 500; break;
            case Year3: num_quotes = 750; break;
            case Year5: num_quotes = 1250; break;
            case Year8: num_quotes = 2000; break;
        }
        MarketInfo mki = null;//MainModel.getInstance().getWatchListModel().getMarketInfo(symbol);
        if (mki == null) {
            try {
                mki = MarketUtil.calcMarketInfo(symbol, num_quotes
                        /*FrameworkConstants.MARKET_QUOTE_LENGTH*/, new DivergenceOption(5, 90, 3));
            } catch (Exception e) {//unable to calculate mki, ok to continue w partial result
            }
        }
        if (mki == null) {//still not found, don't continue
            LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("qp_rdmki") + " " + symbol, LoggingSource.WEEKLY_CANDLE_CHART);
            return;
        }
        FundData fund = mki.getFund();
        WeeklyQuote wq = new WeeklyQuote(fund, fund.getSize() - 1);

        //copy enough quotes for plotting based on time frame combo setting
        num_quotes = 52;//1 year
        switch ((ZoomLevel)_cmbZoom.getSelectedItem()) {
            case Month6: num_quotes = 26; break;
            case Year1: num_quotes = 52; break;
            case Year2: num_quotes = 104; break;
            case Year3: num_quotes = 156; break;
            case Year5: num_quotes = 260; break;
            case Year8: num_quotes = 416; break;
        }
        if (num_quotes > wq.getSize()) num_quotes = wq.getSize();//too many requested, limit to available
        ArrayList<FundQuote> plot_quotes = new ArrayList<>();
        for (int idx = 0; idx < num_quotes; idx++)
            plot_quotes.add(wq.getQuotes().get(idx));

        //compute moving avg, covert quotes into float array
        int size = wq.getSize();
        float[] prices = new float[size];
        for (int idx = 0; idx < size; idx++)
            prices[idx] = wq.getQuotes().get(idx).getClose();
        EMA ema8 = new EMA(8, prices);//T line
        SMA sma50 = new SMA(10, prices);//10 week = 50 day
        SMA sma200 = new SMA(40, prices);//40 week = 200 day
        float[] vol_avg = IndicatorUtil.calcVolumeAverage(plot_quotes, 4);
        _pnlWeeklyCandle.setWeeklyQuote(wq);
        _pnlWeeklyCandle.plot(plot_quotes, sma50.getSma(), sma200.getSma(), ema8.getEma(), vol_avg, candle_signals);
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
    private NameField _fldSymbol = new NameField(5);
    private JButton _btnPrev = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("btn_lbl_3"), FrameworkIcon.ARROW_3D_LEFT);
    private JButton _btnNext = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("btn_lbl_2"), FrameworkIcon.ARROW_3D_RIGHT);
    private JToggleButton _btnRating = WidgetUtil.createToggleButton(FrameworkIcon.RATING, ApolloConstants.APOLLO_BUNDLE.getString("cdl_rating"), null);
    private JToggleButton _btnBolinger = WidgetUtil.createToggleButton(FrameworkIcon.CHART_BOLLINGER, ApolloConstants.APOLLO_BUNDLE.getString("cdl_bol"), null);
    private JToggleButton _btnStochastic = WidgetUtil.createToggleButton(FrameworkIcon.CHART_STO, ApolloConstants.APOLLO_BUNDLE.getString("cdl_sto"), null);
    private JToggleButton _btnMacd = WidgetUtil.createToggleButton(FrameworkIcon.CHART_MACD, ApolloConstants.APOLLO_BUNDLE.getString("cdl_macd"), null);
    private JButton _btnOption = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("cdl_option"), FrameworkIcon.SETTING);
    private JButton _btnSigOn = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("cdl_sig_onoff"), FrameworkIcon.CANDLE_SAMPLE);
    private JButton _btnPad = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("cdl_pad"), FrameworkIcon.REPORT);
    private JComboBox<ZoomLevel> _cmbZoom;
    private CandleGraphPanel _pnlWeeklyCandle;
    private ArrayList<CandleSignal> signalSettings;
}
