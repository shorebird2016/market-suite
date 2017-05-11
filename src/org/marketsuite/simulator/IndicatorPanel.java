package org.marketsuite.simulator;

import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.component.dialog.ProgressBar;
import org.marketsuite.component.resource.DisabledIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.StandardOption;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.model.type.Strategy;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.framework.strategy.base.AbstractStrategyPanel;
import org.marketsuite.framework.strategy.base.SimParam;
import org.marketsuite.framework.strategy.base.SimReport;
import org.marketsuite.simulator.indicator.cci.CciEngine;
import org.marketsuite.simulator.indicator.cci.CciOptionPanel;
import org.marketsuite.simulator.indicator.ichimoku.IchimokuOptionPanel;
import org.marketsuite.simulator.indicator.ichimoku.KumoBreakEngine;
import org.marketsuite.simulator.indicator.macd.MacdOptionPanel;
import org.marketsuite.simulator.indicator.macd.MacdZeroCrossEngine;
import org.marketsuite.simulator.indicator.macd.MzcOption;
import org.marketsuite.simulator.indicator.rsi.RsiEngine;
import org.marketsuite.simulator.indicator.rsi.RsiOption;
import org.marketsuite.simulator.indicator.rsi.RsiOptionPanel;
import org.marketsuite.simulator.indicator.stochastic.StochasticEngine;
import org.marketsuite.simulator.indicator.stochastic.StochasticOption;
import org.marketsuite.simulator.indicator.stochastic.StochasticOptionPanel;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.framework.util.Props;
import org.marketsuite.framework.util.SimUtil;
import org.marketsuite.main.MdiMainFrame;
import net.miginfocom.swing.MigLayout;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.framework.strategy.base.AbstractStrategyPanel;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.simulator.indicator.rsi.RsiOptionPanel;

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
 * Base class container of single indicator based strategies accessible from combo.
 */
public class IndicatorPanel extends AbstractStrategyPanel {
    public IndicatorPanel() {
        //replace title strip with a combo box containing symbols
        JPanel west_pnl = new JPanel(new MigLayout("insets 0", "[][][]5[][][][]10[]", "3[]3")); west_pnl.setOpaque(false);

        //strategy combo
        west_pnl.add(_cmbStrategy); _cmbStrategy.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                if (itemEvent.getStateChange() == ItemEvent.DESELECTED) return; //skip deselect, only process selection
                int sel = _cmbStrategy.getSelectedIndex();
                ((CardLayout)_pnlGraphSouth.getLayout()).show(_pnlGraphSouth, CARD_KEYS[sel]);

                //for stochastic, don't use adjusted close because it needs high/low
//                _chkAdjClose.setSelected(sel != STRATEGY_STOCHASTIC);
            }
        });
        WidgetUtil.attachToolTip(_cmbStrategy, FrameworkConstants.FRAMEWORK_BUNDLE.getString("osc_tip_1"),
            SwingConstants.LEFT, SwingConstants.BOTTOM);

        //symbol combo
        west_pnl.add(_cmbSymbol); _cmbSymbol.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                if (itemEvent.getStateChange() == ItemEvent.DESELECTED)
                    return; //skip deselect, only process selection
                selectSymbol();

                //inform all listener about symbol change
                Props.SymbolSelection.setValue(_cmbSymbol.getSelectedItem());
            }
        });
        WidgetUtil.attachToolTip(_cmbSymbol, FrameworkConstants.FRAMEWORK_BUNDLE.getString("mzc_tip_1"),
            SwingConstants.LEFT, SwingConstants.BOTTOM);

        //start date
        west_pnl.add(_lblDateRange);

        //start year
        west_pnl.add(_txtBeginDate); west_pnl.add(_txtEndDate);
//        west_pnl.add(_chkAdjClose); _chkAdjClose.setOpaque(false);
        _chkAdjClose.setSelected(true); //_chkAdjClose.setEnabled(false);

        //a button to run simulation
        west_pnl.add(_btnSimulate); _btnSimulate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    simulate();
                    _btnExport.setEnabled(true);//allow export after sim
                } catch (Exception e) {
                    e.printStackTrace();
                    WidgetUtil.showWarningInEdt(null, e.getMessage(), null);
                }
            }
        });

        //export this simulation
//        west_pnl.add(_btnExport);TODO add to table instead
        _btnExport.setDisabledIcon(new DisabledIcon(FrameworkIcon.EXPORT.getImage()));
        _btnExport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (_Engine == null) return;
                ArrayList<Transaction> trans_log = _Engine.getTransactionLog();
                if (trans_log.size() == 0) {
                    WidgetUtil.showWarning(FrameworkConstants.FRAMEWORK_BUNDLE.getString("exp_msg_2"));
                    return;
                }
                SimUtil.exportTransaction(_Engine.getTransactionLog(), _Engine, false, "/MZC/");
            }
        });
        _btnExport.setEnabled(false);

        //title strip
        JPanel title_pnl = WidgetUtil.createTitleStrip(west_pnl, null, createCgarEquityPanel());
        replaceTitleStrip(title_pnl);

        //graph panel south strip - holding different sim param widgets for each oscillator
        _pnlGraphSouth = new JPanel(new CardLayout());  _pnlGraphSouth.setOpaque(false);
        _pnlGraphSouth.add(_pnlMacdOption = new MacdOptionPanel(), CARD_KEYS[STRATEGY_MACD]);
        _pnlGraphSouth.add(_pnlRsiOption = new RsiOptionPanel(), CARD_KEYS[STRATEGY_RSI]);
        _pnlGraphSouth.add(_pnlStoOption = new StochasticOptionPanel(), CARD_KEYS[STRATEGY_STOCHASTIC]);
        _pnlGraphSouth.add(_pnlCciOption = new CciOptionPanel(), CARD_KEYS[STRATEGY_CCI]);
        _pnlGraphSouth.add(_pnlKumoBreakOption = new IchimokuOptionPanel(), CARD_KEYS[STRATEGY_KUMO_BREAK]);
        _pnlGraphHolder.add(_pnlGraphSouth, BorderLayout.SOUTH);
        populateSymbols();
    }

    //-----interface implementation, overrides-----
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
    public void simulate() throws Exception {
        switch ((Strategy)_cmbStrategy.getSelectedItem()) {
            case MACD_ZC: simulateMacdZeroCross(); break;
            case RSI_OB_OS: simulateRsi(); break;
            case DSTO_OB_OS: simulateSto(); break;
            case CCI_ZC: simulateCci(); break;
            case ICHMOKU_KUMO_BREAK: simKumoBreak(); break;
        }
    }
    public AbstractEngine getEngine() { return _Engine; }

    //-----private methods-----
    private void simulateMacdZeroCross() throws IOException, ParseException {
        _Engine = new MacdZeroCrossEngine(_Fund);

        //at least long or short check box needs to be checked
        if (!_pnlMacdOption.isLong() && !_pnlMacdOption.isShort()) {
            MessageBox.messageBox(
                    null,//todo MainFrame.getInstance(),
                    FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                    FrameworkConstants.FRAMEWORK_BUNDLE.getString("osc_msg_1"),
                    MessageBox.STYLE_OK, MessageBox.WARNING_MESSAGE);
            return;
        }

        //calculate start date
        Calendar cal = Calendar.getInstance();
        cal.setTime(_txtBeginDate.getDate());
        _sStartDate = AppUtil.calendarToString(cal);
        cal.setTime(_txtEndDate.getDate());
        _sEndDate = AppUtil.calendarToString(cal);

        //setup simulation parameters from spinner
        StandardOption std_opt = new StandardOption(_Fund.getSymbol(),
                _chkLongTrade.isSelected(), _chkShortTrade.isSelected(),
                _sStartDate, _sEndDate, _chkAdjClose.isSelected());
        MzcOption mzc_opt = new MzcOption(_pnlMacdOption.getFastMA(), _pnlMacdOption.getSlowMA());
        SimParam params = new SimParam(std_opt, mzc_opt);
        _Engine.setSimParam(params);

        //do simulation in background with progress bar
        //show progress bar
        final ProgressBar pb = ProgressBar.getInstance((JFrame)null,//todo MainFrame.getInstance(),
                FrameworkConstants.FRAMEWORK_BUNDLE.getString("osc_lbl_4"));//show initial message
        pb.setVisible(true);

        //start a thread to simulate trades
        Thread thread = new Thread() {
            public void run() {
                try {
                    _Engine.simulate();//no GUI
                } catch (IllegalArgumentException iae) {
                    iae.printStackTrace();
                    WidgetUtil.showWarningInEdt(null,//todo MainFrame.getInstance(),
                            iae.getMessage(), pb);
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
                final SimReport rpt = _Engine.genReport();
                rpt.printReport();

                //update graph, tables
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
    private void simulateRsi() throws Exception {
        _Engine = new RsiEngine(_Fund);
        if (!_pnlRsiOption.isLong() && !_pnlRsiOption.isShort()) {
            MessageBox.messageBox(
                    null,//todo MainFrame.getInstance(),
                    FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                    FrameworkConstants.FRAMEWORK_BUNDLE.getString("osc_msg_1"),
                    MessageBox.STYLE_OK, MessageBox.WARNING_MESSAGE);
            return;
        }

        //calculate start date
        Calendar cal = Calendar.getInstance();
        cal.setTime(_txtBeginDate.getDate());
        _sStartDate = AppUtil.calendarToString(cal);
//        int start_index = _Fund.findIndexByDate(_sStartDate);
//        Integer slow_ma = _pnlRsiOption.getLength();
//        start_index -= slow_ma;//adjust start index this much to allow look back
//        if (start_index <= 0) {
//            MessageBox.messageBox(
//                MainFrame.getInstance(),
//                FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
//                FrameworkConstants.FRAMEWORK_BUNDLE.getString("osc_msg_2") + " " + _Fund.getSymbol(),
//                MessageBox.STYLE_OK, MessageBox.WARNING_MESSAGE);
//            return;
//        }
//        _sStartDate = _Fund.getDate(start_index);

        //calculate end date
        cal.setTime(_txtEndDate.getDate());
        _sEndDate = AppUtil.calendarToString(cal);

        //setup simulation parameters from spinner
        StandardOption std_opt = new StandardOption(_Fund.getSymbol(),
                _chkLongTrade.isSelected(), _chkShortTrade.isSelected(),
                _sStartDate, _sEndDate, _chkAdjClose.isSelected());
        RsiOption rsi_opt = _pnlRsiOption.getOptions();
        SimParam params = new SimParam(std_opt, rsi_opt);
        _Engine.setSimParam(params);

        //do simulation in background with progress bar
        //show progress bar
        final ProgressBar pb = ProgressBar.getInstance((JFrame)null,//todo MainFrame.getInstance(),
                FrameworkConstants.FRAMEWORK_BUNDLE.getString("osc_lbl_4"));//show initial message
        pb.setVisible(true);

        //start a thread to simulate trades
        Thread thread = new Thread() {
            public void run() {
                try {
                    _Engine.simulate();//no GUI
                } catch (IllegalArgumentException iae) {
                    iae.printStackTrace();
                    WidgetUtil.showWarningInEdt(null,//todo MainFrame.getInstance(),
                            iae.getMessage(), pb);
                    return;
                }

                //skip if no transaction generated
                ArrayList<Transaction> trans = _Engine.getTransactionLog();
                if (trans == null || trans.size() == 0) {
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            MessageBox.messageBox(
                                    null,//todo MainFrame.getInstance(),
                                    FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                                    FrameworkConstants.FRAMEWORK_BUNDLE.getString("sim_pbmsg_notrans"),
                                    MessageBox.STYLE_OK, MessageBox.WARNING_MESSAGE);
                            pb.setVisible(false);
                        }
                    });
                    return;
                }

                //generate reports, tables and graphs
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        pb.setLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("sim_pbmsg_rpt"));
                    }
                });
                final SimReport rpt = _Engine.genReport();
                rpt.printReport();

                //update graph, tables
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        pb.setLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("sim_pbmsg_tblchart"));
                        _pnlStat.updateStat(rpt, false);
                        _txtEndEquity.setText(FrameworkConstants.DOLLAR_FORMAT.format(rpt.getEndEquity()));
                        _txtCagr.setText(FrameworkConstants.ROI_FORMAT.format(rpt.getCagr()));
                        ArrayList<Transaction> trans_log = rpt.getTransLog();
                        _pnlTrade.populate(trans_log);//table
                        try {
                            SimUtil.plotEquityGraph(_pnlEquityGraph, rpt);//equity curve
                            SimUtil.plotAnnualReturnGraph(_pnlAnnualReturnGraph, rpt);//AR graph
                            SimUtil.plotPriceGraph(_pnlPriceGraph, rpt);//price graph
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
    private void simulateSto() throws Exception {
        _Engine = new StochasticEngine(_Fund);
        if (!_pnlStoOption.isLong() && !_pnlStoOption.isShort()) {
            MessageBox.messageBox(
                    null,//todo MainFrame.getInstance(),
                    FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                    FrameworkConstants.FRAMEWORK_BUNDLE.getString("osc_msg_1"),
                    MessageBox.STYLE_OK, MessageBox.WARNING_MESSAGE);
            return;
        }

        //calculate start date
        Calendar cal = Calendar.getInstance();
        cal.setTime(_txtBeginDate.getDate());
        _sStartDate = AppUtil.calendarToString(cal);
//        int start_index = _Fund.findIndexByDate(_sStartDate);
//        Integer slow_ma = _pnlStoOption.getLength();
//        start_index -= slow_ma;//adjust start index this much to allow look back
//        if (start_index <= 0) {
//            MessageBox.messageBox(
//                MainFrame.getInstance(),
//                FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
//                FrameworkConstants.FRAMEWORK_BUNDLE.getString("osc_msg_2") + " " + _Fund.getSymbol(),
//                MessageBox.STYLE_OK, MessageBox.WARNING_MESSAGE);
//            return;
//        }
//        _sStartDate = _Fund.getDate(start_index);

        //calculate end date
        cal.setTime(_txtEndDate.getDate());
        _sEndDate = AppUtil.calendarToString(cal);

        //setup simulation parameters from spinner
        StandardOption std_opt = new StandardOption(_Fund.getSymbol(),
                _chkLongTrade.isSelected(), _chkShortTrade.isSelected(),
                _sStartDate, _sEndDate, _chkAdjClose.isSelected());
        StochasticOption sto_opt = _pnlStoOption.getOptions();
        SimParam params = new SimParam(std_opt, sto_opt);
        _Engine.setSimParam(params);

        //do simulation in background with progress bar
        //show progress bar
        final ProgressBar pb = ProgressBar.getInstance((JFrame)null,//MainFrame.getInstance(),
                FrameworkConstants.FRAMEWORK_BUNDLE.getString("osc_lbl_4"));//show initial message
        pb.setVisible(true);

        //start a thread to simulate trades
        Thread thread = new Thread() {
            public void run() {
                try {
                    _Engine.simulate();//no GUI
                } catch (IllegalArgumentException iae) {
                    iae.printStackTrace();
                    WidgetUtil.showWarningInEdt(/*MainFrame.getInstance()*/null, iae.getMessage(), pb);
                    return;
                }

                //skip if no transaction generated
                ArrayList<Transaction> trans = _Engine.getTransactionLog();
                if (trans == null || trans.size() == 0) {
                    WidgetUtil.showWarningInEdt(/*MainFrame.getInstance()*/null, FrameworkConstants.FRAMEWORK_BUNDLE.getString("sim_pbmsg_notrans"), pb);
//                    EventQueue.invokeLater(new Runnable() {
//                        public void run() {
//                            MessageBox.messageBox(
//                                    MainFrame.getInstance(),
//                                    FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
//                                    FrameworkConstants.FRAMEWORK_BUNDLE.getString("osc_msg_3"),
//                                    MessageBox.STYLE_OK, MessageBox.WARNING_MESSAGE);
//                            pb.setVisible(false);
//                        }
//                    });
                    return;
                }

                //generate reports, tables and graphs
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        pb.setLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("sim_pbmsg_rpt"));
                    }
                });
                final SimReport rpt = _Engine.genReport();
                rpt.printReport();

                //update graph, tables
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        pb.setLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("sim_pbmsg_tblchart"));
                        _pnlStat.updateStat(rpt, false);
                        _txtEndEquity.setText(FrameworkConstants.DOLLAR_FORMAT.format(rpt.getEndEquity()));
                        _txtCagr.setText(FrameworkConstants.ROI_FORMAT.format(rpt.getCagr()));
                        ArrayList<Transaction> trans_log = rpt.getTransLog();
                        _pnlTrade.populate(trans_log);//table
                        try {
                            SimUtil.plotEquityGraph(_pnlEquityGraph, rpt);//equity curve
                            SimUtil.plotAnnualReturnGraph(_pnlAnnualReturnGraph, rpt);//AR graph
                            SimUtil.plotPriceGraph(_pnlPriceGraph, rpt);//price graph
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
    private void simulateCci() throws Exception {
        _Engine = new CciEngine(_Fund);

        //setup start/end date
        Calendar cal = Calendar.getInstance();
        cal.setTime(_txtBeginDate.getDate());
        _sStartDate = AppUtil.calendarToString(cal);
        cal.setTime(_txtEndDate.getDate());
        _sEndDate = AppUtil.calendarToString(cal);

        //setup simulation parameters for report generation
        StandardOption std_opt = new StandardOption(_Fund.getSymbol(), true, false, _sStartDate, _sEndDate, false);
        StochasticOption sto_opt = _pnlStoOption.getOptions();
        SimParam params = new SimParam(std_opt, sto_opt);
        _Engine.setSimParam(params);
        ((CciEngine)_Engine).setPeriod(_pnlCciOption.getPeriod());

        //do simulation in background with progress bar
        final ProgressBar pb = ProgressBar.getInstance(MdiMainFrame.getInstance(),
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("osc_lbl_4"));//show initial message
        pb.setVisible(true); _Engine.setProgBar(pb);

        //start a thread to simulate trades
        Thread thread = new Thread() {
            public void run() {
                try {
                    _Engine.simulate(_sStartDate, _sEndDate);
                } catch (IllegalArgumentException iae) {
                    iae.printStackTrace();
                    WidgetUtil.showWarningInEdt(null, iae.getMessage(), pb);
                    return;
                }

                //skip if no transaction generated
                ArrayList<Transaction> trans = _Engine.getTransactionLog();
                if (trans == null || trans.size() == 0) {
                    WidgetUtil.showWarningInEdt(null, FrameworkConstants.FRAMEWORK_BUNDLE.getString("sim_pbmsg_notrans"), pb);
                    return;
                }

                //generate reports, tables and graphs
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        pb.setLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("sim_pbmsg_rpt"));
                    }
                });
                final SimReport rpt = _Engine.genReport();
//                rpt.printReport();

                //update graph, tables
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        pb.setLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("sim_pbmsg_tblchart"));
                        _pnlStat.updateStat(rpt, false);
                        _txtEndEquity.setText(FrameworkConstants.DOLLAR_FORMAT.format(rpt.getEndEquity()));
                        _txtCagr.setText(FrameworkConstants.ROI_FORMAT.format(rpt.getCagr()));
                        ArrayList<Transaction> trans_log = rpt.getTransLog();
                        _pnlTrade.populate(trans_log);//table
                        try {
                            SimUtil.plotEquityGraph(_pnlEquityGraph, rpt);//equity curve
                            SimUtil.plotAnnualReturnGraph(_pnlAnnualReturnGraph, rpt);//AR graph
                            SimUtil.plotPriceGraph(_pnlPriceGraph, rpt);//price graph
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
    private void simKumoBreak() throws Exception {
        _Engine = new KumoBreakEngine(_Fund);//TODO this could fail too

        //setup start/end date
        Calendar cal = Calendar.getInstance();
        cal.setTime(_txtBeginDate.getDate()); _sStartDate = AppUtil.calendarToString(cal);
        cal.setTime(_txtEndDate.getDate()); _sEndDate = AppUtil.calendarToString(cal);

        //setup simulation parameters for report generation
        StandardOption std_opt = new StandardOption(_Fund.getSymbol(), true, false, _sStartDate, _sEndDate, false);
        SimParam params = new SimParam(std_opt, _pnlStoOption.getOptions());//TODO make 2nd null, it's not useful
        _Engine.setSimParam(params);

        //do simulation in background with progress bar
        final ProgressBar pb = ProgressBar.getInstance(MdiMainFrame.getInstance(), "Simulating Ichimoku Kumo Breakout Strategy...");
        pb.setVisible(true); _Engine.setProgBar(pb);

        //start a thread to simulate trades
        Thread thread = new Thread() {
            public void run() {
                try {
                    _Engine.simulate(_sStartDate, _sEndDate);
                } catch (IllegalArgumentException iae) {
                    iae.printStackTrace();
                    WidgetUtil.showWarningInEdt(null, iae.getMessage(), pb);
                    return;
                }

                //skip if no transaction generated
                ArrayList<Transaction> trans = _Engine.getTransactionLog();
                if (trans == null || trans.size() == 0) {
                    WidgetUtil.showWarningInEdt(null, FrameworkConstants.FRAMEWORK_BUNDLE.getString("sim_pbmsg_notrans"), pb);
                    return;
                }

                //generate reports, tables and graphs
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        pb.setLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("sim_pbmsg_rpt"));
                    }
                });
                final SimReport rpt = _Engine.genReport();
//                rpt.printReport();

                //update graph, tables
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        pb.setLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("sim_pbmsg_tblchart"));
                        _pnlStat.updateStat(rpt, false);
                        _txtEndEquity.setText(FrameworkConstants.DOLLAR_FORMAT.format(rpt.getEndEquity()));
                        _txtCagr.setText(FrameworkConstants.ROI_FORMAT.format(rpt.getCagr()));
                        ArrayList<Transaction> trans_log = rpt.getTransLog();
                        _pnlTrade.populate(trans_log);//table
                        try {
                            SimUtil.plotEquityGraph(_pnlEquityGraph, rpt);//equity curve
                            SimUtil.plotAnnualReturnGraph(_pnlAnnualReturnGraph, rpt);//AR graph
                            SimUtil.plotPriceGraph(_pnlPriceGraph, rpt);//price graph
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

    //-----instance variables-----
    private JComboBox<Strategy> _cmbStrategy = new JComboBox<>(Strategy.singleIndicatorList());
    private AbstractEngine _Engine;
    private JPanel _pnlGraphSouth;//for the purpose of changing title and sim params
    private MacdOptionPanel _pnlMacdOption;
    private RsiOptionPanel _pnlRsiOption;
    private StochasticOptionPanel _pnlStoOption;
    private CciOptionPanel _pnlCciOption;
    private IchimokuOptionPanel _pnlKumoBreakOption;

    //-----literals-----
    private static final String[] CARD_KEYS = { "CARD_MACD", "CARD_RSI", "CARD_STO", "CARD_CCI", "CARD_KUMO" };
    private static final int STRATEGY_MACD = 0;//selection index of combo
    private static final int STRATEGY_RSI = 1;
    private static final int STRATEGY_STOCHASTIC = 2;
    private static final int STRATEGY_CCI = 3;
    private static final int STRATEGY_KUMO_BREAK = 4;
}
//TODO progress bar not showing
//TODO hook into changing period
//TODO over-run data w/o typical price averages
//TODO why is the CAGR so high???
//TODO over-run for not enough data
//TODO change date range into tooltip
//TODO add choice for "Adjust Close" or not
//TODO add "Export" button
//TODO refactor thread code into the same class/method