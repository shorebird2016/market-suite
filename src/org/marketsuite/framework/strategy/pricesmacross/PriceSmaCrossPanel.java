package org.marketsuite.framework.strategy.pricesmacross;

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
import org.marketsuite.framework.strategy.base.SimParam;
import org.marketsuite.framework.strategy.base.SimReport;
import org.marketsuite.framework.strategy.mac.MacOption;
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
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.framework.strategy.base.AbstractStrategyPanel;
import org.marketsuite.main.MdiMainFrame;

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
 * Container of price/SMA crossing simulation strategy.
 */
public class PriceSmaCrossPanel extends AbstractStrategyPanel {
    public PriceSmaCrossPanel() {
        //replace title strip with a combo box containing symbols
        JPanel west_pnl = new JPanel(new MigLayout("","5[][]20[][]30[]5[]5", "3[]3")); west_pnl.setOpaque(false);

        //symbol combo
        west_pnl.add(_cmbSymbol);
        _cmbSymbol.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                if (itemEvent.getStateChange() == ItemEvent.DESELECTED) return; //skip deselect, only process selection
                selectSymbol();

                //inform all listener about symbol change
                Props.SymbolSelection.setValue(_cmbSymbol.getSelectedItem());
            }
        });
        WidgetUtil.attachToolTip(_cmbSymbol, FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_tip_3"),
                SwingConstants.LEFT, SwingConstants.BOTTOM);

        //start date
        west_pnl.add(_lblDateRange); west_pnl.add(_txtBeginDate); west_pnl.add(_txtEndDate);

        //a button to run simulation
        west_pnl.add(_btnSimulate); _btnSimulate.addActionListener(new ActionListener() {
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
        west_pnl.add(_btnExport); _btnExport.setDisabledIcon(new DisabledIcon(FrameworkIcon.EXPORT.getImage()));
        _btnExport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (_Engine == null)
                    return;

                ArrayList<Transaction> trans_log = _Engine.getTransactionLog();
                if (trans_log == null || trans_log.size() == 0) {
                    WidgetUtil.showWarningInEdt(null,//todo MainFrame.getInstance(),
                        FrameworkConstants.FRAMEWORK_BUNDLE.getString("exp_msg_2"), null);
                    return;
                }
                SimUtil.exportTransaction(_Engine.getTransactionLog(), _Engine, false, "/MAC/");
            }
        });
        _btnExport.setEnabled(false);

        //title strip
        JPanel title_pnl = WidgetUtil.createTitleStrip(west_pnl, null, createCgarEquityPanel());
        replaceTitleStrip(title_pnl);

        //south - 2 spinners for entry
        _pnlGraphSouth = new JPanel(new MigLayout("", "10[][]push[][]5[][]10", "3[]3")); _pnlGraphSouth.setOpaque(false);
        _pnlGraphSouth.add(new JLabel("Crossing MA:"));
        SpinnerNumberModel entry1_model = new SpinnerNumberModel(50, 1, 500, 1);
        _pnlGraphSouth.add(_spnEntryMA1 = new JSpinner(entry1_model));
        _pnlGraphSouth.add(_chkLongTrade); _chkLongTrade.setOpaque(false);
        _chkLongTrade.setSelected(true); _chkLongTrade.setEnabled(false);
        _pnlGraphSouth.add(_chkShortTrade); _chkShortTrade.setOpaque(false);
        _chkShortTrade.setEnabled(false);
        _pnlGraphHolder.add(_pnlGraphSouth, BorderLayout.SOUTH);

        //initialize
        populateSymbols();
   }

    //-----interface, overrides-----
    public AbstractEngine getEngine() { return _Engine; }
    protected void plotPriceGraph() throws IOException, ParseException {
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
    public void simulate() throws IOException, ParseException {
        _Engine = new PriceSmaCrossEngine(_Fund);

        //at least long or short check box needs to be checked
        if (!_chkLongTrade.isSelected() && !_chkShortTrade.isSelected()) {
            WidgetUtil.showWarningInEdt(null,
                    FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_msg_4"), null);
            return;
        }

        //calculate start date, end date
        Calendar cal = Calendar.getInstance();
        cal.setTime(_txtBeginDate.getDate());
        _sStartDate = AppUtil.calendarToString(cal);
        cal.setTime(_txtEndDate.getDate());
        _sEndDate = AppUtil.calendarToString(cal);

        //setup simulation parameters from spinner
        StandardOption std_opt = new StandardOption(_Fund.getSymbol(),
           _chkLongTrade.isSelected(), _chkShortTrade.isSelected(),
            _sStartDate, _sEndDate, _chkAdjClose.isSelected());
        MacOption mac_opt = new MacOption((Integer) _spnEntryMA1.getValue(), 0, 0, 0);
        SimParam params = new SimParam(std_opt, mac_opt);
        _Engine.setSimParam(params);

        //do simulation in background with progress bar
        //show progress bar
        final ProgressBar pb = ProgressBar.getInstance((JFrame)null, "Simulating Price vs SMA Crossing Strategy...");
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
                    WidgetUtil.showWarningInEdt(null, e.getMessage(), pb);
                    return;
                }

                //skip if no transaction generated
                ArrayList<Transaction> trans = _Engine.getTransactionLog();
                if (trans == null || trans.size() == 0) {
                    WidgetUtil.showWarningInEdt(MdiMainFrame.getInstance(),
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

    //-----variables-----
    private PriceSmaCrossEngine _Engine;
    private JPanel _pnlGraphSouth;
    private JSpinner _spnEntryMA1;
}