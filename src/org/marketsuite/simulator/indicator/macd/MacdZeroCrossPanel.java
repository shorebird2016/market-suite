package org.marketsuite.simulator.indicator.macd;

import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.simulator.IndicatorPanel;

import javax.swing.*;

/**
 * Container of MACD zero cross strategy.
 */
public class MacdZeroCrossPanel extends IndicatorPanel {
    public MacdZeroCrossPanel() {
        super();

//        //set title for graph panel
//        _pnlEquityGraph.getChart().setTitle(AppConstants.APP_BUNDLE.getString("mzc_lbl_2"));
//        //graph panel south strip - holding different sim param widgets for each oscillator
//
//        _pnlGraphSouth = new JPanel(new CardLayout());  _pnlGraphSouth.setOpaque(false);
//        _pnlGraphHolder.add(_pnlGraphSouth, BorderLayout.SOUTH);
//        _pnlGraphSouth.add(createParamsPanel(), CARD_KEYS[0]);
    }

    public AbstractEngine getEngine() { return null; }

    //plot _Fund data and all moving averages from _StartDate
    protected void plotPriceGraph() {
//        TimeSeries series1 = new TimeSeries("");
//        TimeSeries series2 = new TimeSeries("10 Week MA");
//        TimeSeries series3 = new TimeSeries("24 Week MA");
//        TimeSeries series4 = new TimeSeries("40 Week MA");
//        float[] short_ma = _Engine.getFastMA();
//        float[] medium_ma = _Engine.getMediumMA();
//        float[] long_ma = _Engine.getSlowMA();
//        int start_index = _Fund.findIndexByDate(_StartDate);
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
//        _pnlPriceGraph.addAll(serieses);
    }

//    public void simulate() throws IOException, ParseException {
//        _Engine = new MacdZeroCrossEngine(_Fund);
//
//        //at least long or short check box needs to be checked
//        if (!_chkLongTrade.isSelected() && !_chkShortTrade.isSelected()) {
//            MessageBox.messageBox(
//                MainFrame.getInstance(),
//                FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
//                AppConstants.APP_BUNDLE.getString("mzc_msg_1"),
//                MessageBox.STYLE_OK, MessageBox.WARNING_MESSAGE);
//            return;
//        }
//
//        //calculate start date
//        int yr = (Integer)_cmbStartYear.getSelectedItem();
//        _sStartDate = AppUtil.findFirstQuoteInYear(_Fund, yr).getDate();
//        int start_index = _Fund.findIndexByDate(_sStartDate);
//        Integer long_ma = (Integer)_spnSlowMA.getValue();
//        start_index -= long_ma;//adjust start index this much to allow look back
//        if (start_index <= 0) {
//            MessageBox.messageBox(
//                MainFrame.getInstance(),
//                FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
//                AppConstants.APP_BUNDLE.getString("mzc_msg_2") + " " + _Fund.getSymbol(),
//                MessageBox.STYLE_OK, MessageBox.WARNING_MESSAGE);
//            return;
//        }
//        _sStartDate = _Fund.getDate(start_index);
//
//        //calculate end date
//        yr = (Integer)_cmbEndYear.getSelectedItem();
//        _sEndDate = AppUtil.findLastQuoteInYear(_Fund, yr).getDate();
//
//        //setup simulation parameters from spinner
//        StandardOption std_opt = new StandardOption(_Fund.getSymbol(),
//            _chkLongTrade.isSelected(), _chkShortTrade.isSelected(),
//            _sStartDate, _sEndDate, _chkAdjClose.isSelected());
//        MzcOption mzc_opt = new MzcOption((Integer)_spnFastMA.getValue(), long_ma);
//        SimParam params = new SimParam(std_opt, mzc_opt);
//        _Engine.setSimParam(params);
//
//        //do simulation in background with progress bar
//        //show progress bar
//        final ProgressBar pb = ProgressBar.getInstance(MainFrame.getInstance(), AppConstants.APP_BUNDLE.getString("mzc_lbl_4"));
//        pb.setVisible(true);
//
//        //start a thread to simulate trades
//        Thread thread = new Thread() {
//            public void run() {
//                //calculate MACD
//                EventQueue.invokeLater(new Runnable() {
//                    public void run() {
//                        pb.setLabel(AppConstants.APP_BUNDLE.getString("mzc_msg_4") + " " + _Fund.getSymbol());
//                    }
//                });
//
//                //start simulation
//                _Engine.simulate();//no GUI
//
//                //skip if no transaction generated
//                ArrayList<Transaction> trans = _Engine.getTransactionLog();
//                if (trans == null || trans.size() == 0) {
//                    EventQueue.invokeLater(new Runnable() {
//                        public void run() {
//                            MessageBox.messageBox(
//                                MainFrame.getInstance(),
//                                FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
//                                AppConstants.APP_BUNDLE.getString("mzc_msg_3"),
//                                MessageBox.STYLE_OK, MessageBox.WARNING_MESSAGE);
//                            pb.setVisible(false);
//                        }
//                    });
//                    return;
//                }
//
//                //generate reports, tables and graphs
//                EventQueue.invokeLater(new Runnable() {
//                    public void run() {
//                        pb.setLabel(AppConstants.APP_BUNDLE.getString("mzc_msg_5"));
//                    }
//                });
//                final SimReport rpt = _Engine.genReport();
//                rpt.printReport();
//
//                //update graph, tables
//                EventQueue.invokeLater(new Runnable() {
//                    public void run() {
//                        pb.setLabel(AppConstants.APP_BUNDLE.getString("mzc_msg_6"));
//                        _pnlStat.updateStat(rpt, false);
//                        _txtEndEquity.setText(FrameworkConstants.DOLLAR_FORMAT.format(rpt.getEndEquity()));
//                        _txtCagr.setText(FrameworkConstants.ROI_FORMAT.format(rpt.getCagr()));
//                        ArrayList<Transaction> trans_log = rpt.getTransLog();
//                        _pnlTrade.populate(trans_log);//table
//                        try {
//                            plotEquityGraph(rpt.getEquityLog(), trans_log);//equity curve
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                            pb.setVisible(false);
//                        }
//                        plotAnnualReturnGraph(rpt.getAnnualReturns());//AR graph
//                        plotPriceGraph();//price graph
//                        pb.setVisible(false);
//                    }
//                });
//            }
//        };
//        thread.start();
//    }

    //2 spinners allow user to change 2 MA lengths, plus long/short choice
    public JPanel createParamsPanel() {
        JPanel ret = new JPanel();  ret.setOpaque(false);
        ret.add(new JLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mzc_spn_1")));
        SpinnerNumberModel slow_model = new SpinnerNumberModel(MacdZeroCrossEngine.FAST_MA, 1, 100, 1);
        ret.add(_spnFastMA = new JSpinner(slow_model));
        ret.add(Box.createGlue());
        ret.add(new JLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mzc_spn_2")));
        SpinnerNumberModel fast_model = new SpinnerNumberModel(MacdZeroCrossEngine.SLOW_MA, 1, 200, 1);
        ret.add(_spnSlowMA = new JSpinner(fast_model));
        ret.add(Box.createHorizontalStrut(100));
        ret.add(_chkLongTrade); _chkLongTrade.setOpaque(false); _chkLongTrade.setSelected(true);
        ret.add(_chkShortTrade); _chkShortTrade.setOpaque(false);
        return ret;
    }

    //-----instance variables-----
    private JSpinner _spnFastMA;
    private JSpinner _spnSlowMA;
}