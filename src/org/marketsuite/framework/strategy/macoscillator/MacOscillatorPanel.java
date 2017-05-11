package org.marketsuite.framework.strategy.macoscillator;

import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.component.dialog.ProgressBar;
import org.marketsuite.component.resource.DisabledIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.StandardOption;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.framework.strategy.base.AbstractStrategyPanel;
import org.marketsuite.framework.strategy.base.SimReport;
import org.marketsuite.framework.strategy.mac.MacEngine;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.framework.util.Props;
import org.marketsuite.framework.util.SimUtil;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.framework.strategy.base.AbstractStrategyPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Container of Moving Average Cross (MAC) plus Oscillator strategy.
 */
public class MacOscillatorPanel extends AbstractStrategyPanel {
    public MacOscillatorPanel() {
        //replace title strip with a combo box containing symbols
        JPanel west_pnl = new JPanel(); west_pnl.setOpaque(false);

        //symbol combo
        west_pnl.add(_cmbSymbol);
        _cmbSymbol.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                if (itemEvent.getStateChange() == ItemEvent.DESELECTED)
                    return; //skip deselect, only process selection

                selectSymbol();

                //inform all listener about symbol change
                Props.SymbolSelection.setValue(_cmbSymbol.getSelectedItem());
            }
        });
        WidgetUtil.attachToolTip(_cmbSymbol, FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_tip_3"),
            SwingConstants.LEFT, SwingConstants.BOTTOM);

        //start date
        west_pnl.add(Box.createGlue());
        west_pnl.add(_lblDateRange);

        //start year
        west_pnl.add(Box.createHorizontalStrut(20));
        west_pnl.add(_txtBeginDate);
        west_pnl.add(_txtEndDate);
        west_pnl.add(_chkAdjClose); _chkAdjClose.setOpaque(false); _chkAdjClose.setSelected(true); _chkAdjClose.setEnabled(false);
        west_pnl.add(Box.createGlue());

        //a button to run simulation
        west_pnl.add(_btnSimulate);
        _btnSimulate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    simulate();
                    _btnExport.setEnabled(true);//allow export after sim
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
        west_pnl.add(Box.createGlue());

        //special options
        west_pnl.add(_btnSimOption);
        _btnSimOption.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                MacOscOptionDialog dlg = MacOscOptionDialog.getInstance();
                if (!dlg.isVisible())
                    dlg.setVisible(true);
            }
        });
        west_pnl.add(Box.createGlue());

        //export this simulation
        west_pnl.add(_btnExport);
        _btnExport.setDisabledIcon(new DisabledIcon(FrameworkIcon.EXPORT.getImage()));
        _btnExport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (_Engine == null)
                    return;

//                ArrayList<Transaction> trans_log = _Engine.getTransactionLog();
//                if (trans_log == null || trans_log.size() == 0) {
//                    WidgetUtil.showWarningInEdt(null,//todo MainFrame.getInstance(),
//                        FrameworkConstants.FRAMEWORK_BUNDLE.getString("exp_msg_2"), null);
//                    return;
//                }
//                SimUtil.exportTransaction(_Engine.getTransactionLog(), _Engine, false, "/MAC/");
            }
        });
        _btnExport.setEnabled(false);

        //title strip
        JPanel title_pnl = WidgetUtil.createTitleStrip(west_pnl, null, createCgarEquityPanel());
        replaceTitleStrip(title_pnl);

        //initialize
        populateSymbols();
   }

    public AbstractEngine getEngine() { return _Engine; }

//TODO use same code between MAC and Oscillators
    protected void plotPriceGraph() throws IOException, ParseException {
//        String sym = sym.getText();
        String sym = (String)_cmbSymbol.getSelectedItem();
        //read historical data
        FundData fund = DataUtil.readFundHistory(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, sym);
        if (fund.getSize() == 0) {
            MessageBox.messageBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_005") + " " + sym + ".  " +
                FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_006") + " " +
                FrameworkConstants.DATA_FOLDER_WEEKLY_QUOTE);
            return;
        }

        //find start date
        String start_date = _Engine.getTransactionLog().get(0).getEntryDate();
        int start_index = fund.findIndexByDate(start_date);

        //prepare time series - price plus MACD
        TimeSeries price_series = new TimeSeries(sym);

//        float[] short_MACD = IndicatorUtil.calcMACD(SHORT_MACD_FAST, SHORT_MACD_SLOW, start_index, fund);
//        float[] medium_MACD = IndicatorUtil.calcMACD(MEDIUM_MACD_FAST, MEDIUM_MACD_SLOW, start_index, fund);
//        float[] long_MACD = IndicatorUtil.calcMACD(LONG_MACD_FAST, LONG_MACD_SLOW, start_index, fund);
//
//        TimeSeries ind1_series = new TimeSeries("MACD 1");
//        TimeSeries ind2_series = new TimeSeries("MACD 2");
//        TimeSeries ind3_series = new TimeSeries("MACD 3");
        for (int index = start_index; index >= 0; index--) {
            try {
                Calendar cal = AppUtil.stringToCalendar(fund.getDate(index));
                Day day = new Day(cal.getTime());
                price_series.add(day, fund.getPrice(index));
//                ind1_series.add(day, short_MACD[index]);
//                ind2_series.add(day, medium_MACD[index]);
//                ind3_series.add(day, long_MACD[index]);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
//        TimeSeries[] serieses = new TimeSeries[3];
//        serieses[0] = ind1_series;
//        serieses[1] = ind2_series;
//        serieses[2] = ind3_series;
        TimeSeries[] ps = new TimeSeries[1]; ps[0] = price_series;
//        _pnlPriceIndicator.addSeries(price_series, serieses);
        if (_pnlPriceGraph.isCandleChart()) {//candle stick chart
            int len = start_index + 1;
            Date[] dates =  new Date[len];
            double[] high = new double[len];
            double[] low = new double[len];
            double[] open = new double[len];
            double[] close = new double[len];
            double[] volume = new double[len];
            for (int idx = start_index; idx >= 0; idx--) {
                ArrayList<FundQuote> quote = fund.getQuote();
                dates[idx] = AppUtil.stringToCalendar(quote.get(idx).getDate()).getTime();
                high[idx] = quote.get(idx).getHigh();
                low[idx] = quote.get(idx).getLow();
                open[idx] = quote.get(idx).getOpen();
                close[idx] = quote.get(idx).getClose();
                volume[idx] = quote.get(idx).getVolume();
            }
            DefaultHighLowDataset ds = new DefaultHighLowDataset(sym, dates, high, low, open, close, volume);
            _pnlPriceGraph.drawCandleChart(ds);
        }
        else {//line chart
            _pnlPriceGraph.drawEntryExits(_Engine.getTransactionLog());
            _pnlPriceGraph.addSeries(ps);
        }
    }

    //plot _Fund data and all moving averages from _sStartDate
//    protected void plotPriceGraph1() {
//        TimeSeries series1 = new TimeSeries("");
//        TimeSeries series2 = new TimeSeries("50 Day EMA");
//        TimeSeries series3 = new TimeSeries("120 Day EMA");
//        TimeSeries series4 = new TimeSeries("200 Day EMA");
//        float[] short_ma = _Engine.getEntryMA1();
//        float[] medium_ma = _Engine.getEntrySlowMa();
//        float[] long_ma = _Engine.getExitMA2();
//        int start_index = _Fund.findIndexByDate(_sStartDate);
//        for (int index = start_index; index >= 0; index--) {
//            try {
//                Calendar cal = AppUtil.stringToCalendar(_Fund.getDate(index));
//                Day day = new Day(cal.getTime());
//                series1.add(day, _Fund.getPrice(index));
//                series2.add(day, short_ma[index]);
//                series3.add(day, medium_ma[index]);
//                series4.add(day, long_ma[index]);
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//        }
//        TimeSeries[] serieses = new TimeSeries[4];
//        serieses[0] = series1;
//        serieses[1] = series2;
//        serieses[2] = series3;
//        serieses[3] = series4;
//        _pnlPriceGraph.addSeries(serieses);
//    }

    public void simulate() throws IOException, ParseException {
        _Engine = new MacEngine(_Fund);//todo let engine takes care of fund instead panel shouldn't care, getFund from engine

        //at least long or short check box needs to be checked
        if (!_chkLongTrade.isSelected() && !_chkShortTrade.isSelected()) {
            WidgetUtil.showWarningInEdt(null,//todo MainFrame.getInstance(),
                FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_msg_4"), null);
            return;
        }

        //calculate start date
        Calendar cal = Calendar.getInstance();
        cal.setTime(_txtBeginDate.getDate());
        _sStartDate = AppUtil.calendarToString(cal);

        //calculate end date
        cal.setTime(_txtEndDate.getDate());
        _sEndDate = AppUtil.calendarToString(cal);

        //setup simulation parameters from spinner
        StandardOption std_opt = new StandardOption(_Fund.getSymbol(),
            _chkLongTrade.isSelected(), _chkShortTrade.isSelected(),
            _sStartDate, _sEndDate, _chkAdjClose.isSelected());
//        MacOption mac_opt = new MacOption(
//            (Integer) _spnEntryFastMa.getValue(), (Integer) _spnEntrySlowMa.getValue(),
//            (Integer) _spnEntryFastMa.getValue(), (Integer) _spnEntrySlowMa.getValue());
//        SimParam params = new SimParam(std_opt, mac_opt);
//        _Engine.setSimParam(params);

        //do simulation in background with progress bar
        //show progress bar
        final ProgressBar pb = ProgressBar.getInstance((JFrame)null,//todo MainFrame.getInstance(),
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("osc_lbl_4"));
        pb.setVisible(true);

        //start a thread to simulate trades
        Thread thread = new Thread() {
            public void run() {
                //calculate EMAs, cross overs
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        pb.setLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_msg_2") + " " + _Fund.getSymbol());
                    }
                });

                try {
                    _Engine.simulate();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    WidgetUtil.showWarningInEdt(null,//todo MainFrame.getInstance(),
                            e.getMessage(), pb);
                    return;
                }

                //skip if no transaction generated
                ArrayList<Transaction> trans = _Engine.getTransactionLog();
                if (trans == null || trans.size() == 0) {
                    WidgetUtil.showWarningInEdt(null,//todo MainFrame.getInstance(),
                        FrameworkConstants.FRAMEWORK_BUNDLE.getString("sim_pbmsg_notrans"), pb);
                    return;
                }

                //generate reports, tables and graphs
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        pb.setLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("sim_pbmsg_rpt"));
                    }
                });

                //update graph, tables
                final SimReport rpt = _Engine.genReport();
                rpt.printReport();
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        pb.setLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("sim_pbmsg_tblchart"));
                        _pnlStat.updateStat(rpt, false);
                        _txtEndEquity.setText(FrameworkConstants.DOLLAR_FORMAT.format(rpt.getEndEquity()));
                        _txtCagr.setText(FrameworkConstants.ROI_FORMAT.format(rpt.getCagr()));
                        ArrayList<Transaction> trans_log = rpt.getTransLog();
                        _pnlTrade.populate(trans_log);//table
                        try {
                            SimUtil.plotEquityGraph(_pnlEquityGraph, rpt);
                            SimUtil.plotAnnualReturnGraph(_pnlAnnualReturnGraph, rpt);
                            SimUtil.plotPriceGraph(_pnlPriceGraph, rpt);
                        } catch (Exception e) {
                            e.printStackTrace();
                            pb.setVisible(false);
                        }
                        setGraphTitle();
                        pb.setVisible(false);
                    }
                });
            }
        };
        thread.start();
    }

//    private JPanel createMaPanel() {
//        FormLayout layout = new FormLayout(
//            "2dlu, left:pref, 5dlu, left:pref, 10dlu",//columns
//            "2dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu"//rows
//        );
//        PanelBuilder builder = new PanelBuilder(layout);
//        CellConstraints cc = new CellConstraints();
//        int col = 2, row = 2;
//        builder.add(_rdoSma, cc.xyw(col, row, 4));
//
//        row += 2;
//        builder.add(_rdoEma, cc.xyw(col, row, 4));
//
//        row += 2;
//        builder.addLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("macosc_lbl_3"), cc.xy(col, row));
//        SpinnerNumberModel entry1_model = new SpinnerNumberModel(MacOscillatorEngine._nEntryMA1, 1, 100, 10);
//        builder.add(_spnEntryFastMa = new JSpinner(entry1_model), cc.xy(col + 2, row));
//
//        row += 2;
//        builder.addLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("macosc_lbl_4"), cc.xy(col, row));
//        SpinnerNumberModel entry2_model = new SpinnerNumberModel(MacOscillatorEngine._nEntryMA2, 1, 200, 30);
//        builder.add(_spnEntrySlowMa = new JSpinner(entry2_model), cc.xy(col + 2, row));
//
//        return builder.getPanel();
//    }

//    //several methods to create parameter panels below graph
//    private JPanel createMacParamPanel() {
//        JPanel ret = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        ret.add(new JLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("macosc_lbl_5")));
//        ret.add(_cmbMa);
//        SpinnerNumberModel sp_model = new SpinnerNumberModel(MacOscillatorEngine._nEntryMA1, 1, 100, 1);
//        ret.add(_spnEntrySlowMa = new JSpinner(sp_model));
//        sp_model = new SpinnerNumberModel(MacOscillatorEngine._nEntryMA2, 1, 200, 1);
//        ret.add(_spnEntryFastMa = new JSpinner(sp_model));
//        return ret;
//    }
//    private JPanel createDstoParamPanel() {
//        JPanel ret = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        ret.add(_chkDaily); _chkDaily.setEnabled(false);
//        _chkDaily.setSelected(true);
//        ret.add(_cmbDsto);
//        SpinnerNumberModel sp_model = new SpinnerNumberModel(5, 1, 100, 1);
//        ret.add(_spnDstoParam1 = new JSpinner(sp_model));
//        sp_model = new SpinnerNumberModel(3, 1, 100, 1);
//        ret.add(_spnDstoParam2 = new JSpinner(sp_model));
//        return ret;
//    }
//    private JPanel createWstoParamPanel() {
//        JPanel ret = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        ret.add(_chkWeekly);
//        ret.add(_cmbWsto);
//        SpinnerNumberModel sp_model = new SpinnerNumberModel(5, 1, 100, 1);
//        ret.add(_spnWstoParam1 = new JSpinner(sp_model));
//        sp_model = new SpinnerNumberModel(3, 1, 100, 1);
//        ret.add(_spnWstoParam2 = new JSpinner(sp_model));
//        return ret;
//    }
//    private JPanel createExitParamPanel() {
//        JPanel ret = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        ret.add(new JLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("macosc_lbl_8") + " "));
//        SpinnerNumberModel sp_model = new SpinnerNumberModel(30, 1, 100, 1);
//        ret.add(_spnExitMaLength = new JSpinner(sp_model));
//        sp_model = new SpinnerNumberModel(3, 1, 100, 1);
//        ret.add(_cmbExitMa);
//        return ret;
//    }

    //-----instance variables-----
    private MacEngine _Engine;
    private JPanel _pnlGraphSouth;
//    private JComboBox _cmbMa = new JComboBox(LIST_MA_TYPE);
//    private JSpinner _spnEntrySlowMa, _spnEntryFastMa;
//    private JCheckBox _chkDaily = new JCheckBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("macosc_lbl_6"));
//    private JComboBox _cmbDsto = new JComboBox(LIST_STO_TYPE);
//    private JSpinner _spnDstoParam1, _spnDstoParam2;
//    private JCheckBox _chkWeekly = new JCheckBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("macosc_lbl_7"));
//    private JComboBox _cmbWsto = new JComboBox(LIST_STO_TYPE);
//    private JSpinner _spnWstoParam1, _spnWstoParam2;
//    private JSpinner _spnExitMaLength;
//    private JComboBox _cmbExitMa = new JComboBox(LIST_MA_TYPE);

//    //----- literals -----
//    public static final String[] LIST_MA_TYPE = { "SMA", "EMA" };
//    public static final String[] LIST_STO_TYPE = { "Slow Stochastic", "Fast Stochastic" };
}