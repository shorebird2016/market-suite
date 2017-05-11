package org.marketsuite.framework.strategy.analysis;

import org.marketsuite.component.Constants;
import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.AnnualReturn;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.framework.strategy.base.AbstractStrategyPanel;
import org.marketsuite.framework.strategy.base.SimReport;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.DataUtil;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.AnnualReturn;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.framework.strategy.base.AbstractStrategyPanel;
import org.marketsuite.framework.strategy.base.SimReport;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.DataUtil;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

/**
 * Container for custom system evaluation. The engine is just a parser for file manually entered
 * The result is compared with SP500 easily.
 */
public class AnalysisPanel extends AbstractStrategyPanel {
    public AnalysisPanel() {
        //custom title strip - result file selection, check box; this replace west panel of existing title strip
        JPanel west_pnl = new JPanel();  west_pnl.setOpaque(false);
        west_pnl.add(new JLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("ana_lbl_5")));
        west_pnl.add(_cmbFileType);
        WidgetUtil.attachToolTip(_cmbFileType, FrameworkConstants.FRAMEWORK_BUNDLE.getString("ana_tip_1"),
                SwingConstants.RIGHT, SwingConstants.TOP);

        //open button
        west_pnl.add(Box.createGlue());
        west_pnl.add(_btnOpen);
        _btnOpen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {//pick folder, replenish file list, analyze first symbol
                JFileChooser fc = new JFileChooser(new File(FrameworkConstants.DATA_FOLDER_EXPORT));
                fc.setFileFilter(new FileFilter() {
                    public boolean accept(File file) {
                        if (file.isDirectory())
                            return true;
                        //only allow .csv extension
                        int ext_pos = file.getName().lastIndexOf(FrameworkConstants.EXTENSION_TRADES);
                        if (ext_pos > 0)
                            return true;
                        return false;
                    }

                    public String getDescription() {//this shows up in description field of dialog
                        return FrameworkConstants.FRAMEWORK_BUNDLE.getString("lbl_file_filter");
                    }
                });
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.setAcceptAllFileFilterUsed(false);
                int ret = fc.showOpenDialog(AnalysisPanel.this);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    _sCurrentFile = fc.getSelectedFile();
                    String file_name = _sCurrentFile.getName();
                    int idx = file_name.indexOf(FrameworkConstants.EXTENSION_TRADES);
                    if (idx < 0) {//can't load other type of extension
                        MessageBox.messageBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                            FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_008") + " " + _sCurrentFile);
                        return;
                    }
                    String symbol = file_name.substring(0, idx);
                    _Engine.setSymbol(symbol);
                    _lblSymbol.setText(symbol);
                    if (_cmbFileType.getSelectedIndex() == FrameworkConstants.FILE_FORMAT_MDB) {
                        _sCurrentSegment = LIST_MDB_SEGMENTS[0];
                        _lblSymbol.setText(FrameworkConstants.FRAMEWORK_BUNDLE.getString("lbl_seg")
                            + " " + _sCurrentSegment);
                        _Engine.setSegment(_sCurrentSegment);
                    }

                    //obtain transaction log from file
                    try {
                        ArrayList<Transaction> log = _Engine.extractLog(
                            _sCurrentFile.getPath(), _cmbFileType.getSelectedIndex());
                        if (log == null) {
                            MessageBox.messageBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                                FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_009") + " " + _sCurrentFile);
                            return;
                        }

                        //clone transaction array for reuse
                        _OrigTransactions = (ArrayList<Transaction>)log.clone();

                        //populate start/end year combo, assume ordered data, 0th and n-1 th
                        Transaction tr_first = log.get(0);
                        Transaction tr_last = log.get(log.size() - 1);
                        int start_first = AppUtil.extractYear(tr_first.getEntryDate());
                        int start_last = AppUtil.extractYear(tr_last.getEntryDate());
                        int end_first = AppUtil.extractYear(tr_first.getExitDate());
                        int end_last = AppUtil.extractYear(tr_last.getExitDate());
                        Vector<Integer> start_list = new Vector<Integer>();
                        Vector<Integer> end_list = new Vector<Integer>();
                        for (int i=start_first; i<=start_last; i++)
                            start_list.add(i);
                        for (int i=end_first; i<=end_last; i++)
                            end_list.add(i);
                        _cmbStartYear.setModel(new DefaultComboBoxModel/*<Integer>*/(start_list));
                        _cmbEndYear.setModel(new DefaultComboBoxModel/*<Integer>*/(end_list));
                        _cmbEndYear.setSelectedItem(end_last);

                        //empty all views
                        _pnlTransaction.clear();
                        _pnlStat.clearFields();
                        _pnlEquityGraph.clear();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        //symbol name
        west_pnl.add(Box.createHorizontalGlue());
        west_pnl.add(_lblSymbol);
        _lblSymbol.setFont(Constants.VERDONA_BOLD_12);

        //current strategy name from file
        west_pnl.add(Box.createHorizontalGlue());
        west_pnl.add(_lblStrategy);

        //next segment button only for MDB format
        west_pnl.add(_btnNextSegment);
        _btnNextSegment.setVisible(false);
        _btnNextSegment.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                //change segment to next one, redo simulation
                //move segment to next
                for (int i=0; i<LIST_MDB_SEGMENTS.length; i++)
                    if (_sCurrentSegment.equals(LIST_MDB_SEGMENTS[i])) {
                        int new_idx = i + 1;
                        if (new_idx >= LIST_MDB_SEGMENTS.length)
                            new_idx = 0;
                        _sCurrentSegment = LIST_MDB_SEGMENTS[new_idx];
                        _lblSymbol.setText(FrameworkConstants.FRAMEWORK_BUNDLE.getString("lbl_seg")
                            + " " + _sCurrentSegment);
                        _Engine.setSegment(_sCurrentSegment);
                        break;
                    }
            }
        });

        //center title - date range, start/end selector, run button
        JPanel cen_pnl = new JPanel();  cen_pnl.setOpaque(false);
//todo Use stop, KEEP THIS CODE
//        cen_pnl.add(_chkUseStop);  _chkUseStop.setOpaque(false);
//        WidgetUtil.attachToolTip(_chkUseStop, FrameworkConstants.FRAMEWORK_BUNDLE.getString("ana_tip_2"),
//            SwingConstants.RIGHT, SwingConstants.TOP);
//        _chkUseStop.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent actionEvent) {
//                _spnStopPercent.setEnabled(_chkUseStop.isSelected());
//            }
//        });
//        cen_pnl.add(Box.createGlue());
//        SpinnerNumberModel pct_model = new SpinnerNumberModel(3, 1, 10, 1);
//        _spnStopPercent = new JSpinner(pct_model);
//        _spnStopPercent.setEnabled(false);
//        cen_pnl.add(_spnStopPercent);

        //start date
        cen_pnl.add(_lblDateRange);
        cen_pnl.add(Box.createGlue());
        cen_pnl.add(_cmbStartYear);
        WidgetUtil.attachToolTip(_cmbStartYear, FrameworkConstants.FRAMEWORK_BUNDLE.getString("bh_tip_3"),
            SwingConstants.LEFT, SwingConstants.BOTTOM);
        cen_pnl.add(_cmbEndYear);
        cen_pnl.add(Box.createGlue());
        cen_pnl.add(_btnRun);
        _btnRun.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    analyze();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        replaceTitleStrip(WidgetUtil.createTitleStrip(west_pnl, cen_pnl, createCgarPanel()));

        //replace result table with trade table
        replaceResultPanel(_pnlTransaction = new TransactionPanel());
        _splHorizontal.setDividerLocation(550);

        //graph title
        _pnlEquityGraph.getChart().setTitle(FrameworkConstants.FRAMEWORK_BUNDLE.getString("eqty_curve"));
    }
    public AnalysisPanel(boolean no_title) {
        this();
        if (no_title)
            remove(_pnlTitleStrip);
    }
    public AbstractEngine getEngine() { return _Engine; }
    public void simulate() throws IOException, ParseException { }

    //overrides
    protected void plotPriceGraph(String symbol) throws IOException, ParseException {
//        String sym = _lblSymbol.getText();
        //read historical data
        FundData fund = DataUtil.readFundHistory(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, symbol);
        if (fund.getSize() == 0) {
            MessageBox.messageBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_005") + " " + symbol + ".  " +
                FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_006") + " " +
                FrameworkConstants.DATA_FOLDER_WEEKLY_QUOTE);
            return;
        }

        //find start date
        String start_date = _Engine.getTransactionLog().get(0).getEntryDate();
        int start_index = fund.findIndexByDate(start_date);

        //prepare time series - price plus MACD
        TimeSeries price_series = new TimeSeries(symbol);

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
            DefaultHighLowDataset ds = new DefaultHighLowDataset(symbol, dates, high, low, open, close, volume);
            _pnlPriceGraph.drawCandleChart(ds);
        }
        else {//line chart
            _pnlPriceGraph.drawEntryExits(_Engine.getTransactionLog());
            _pnlPriceGraph.addSeries(ps);
        }
    }

    //-----private methods-----
    private void analyze() throws IOException, ParseException {
//todo Use STOP, KEEP CODE
//        if (_chkUseStop.isSelected()) {//when SimParam is null, engine will know no stop loss rule
//            SimParam param = new SimParam();
//            float stop = ((Integer)_spnStopPercent.getValue());
//            param.setInitialStop(stop / 100);
//            _Engine.setSimParam(param);
//        }
//        else
        _Engine.setSimParam(null);
        ArrayList<Transaction> trans = _Engine.getTransactionLog();
        if (trans == null) {
            MessageBox.messageBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_009") + " " + _sCurrentFile);
            return;
        }

        //use start/end year to remove transactions that are NOT in range
        ArrayList<Transaction> sub_trans = new ArrayList<Transaction>();
        int start_yr = (Integer)_cmbStartYear.getSelectedItem();
        int end_yr = (Integer)_cmbEndYear.getSelectedItem();
        for (Transaction tr : _OrigTransactions) {
            int entry_yr = AppUtil.extractYear(tr.getEntryDate());
            int exit_yr = AppUtil.extractYear(tr.getEntryDate());
            if (entry_yr < start_yr || exit_yr > end_yr)
                continue;//skip

            sub_trans.add(tr);
        }

        //all transactions are filtered out
        if (sub_trans.size() == 0) {
            MessageBox.messageBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_009") + " " + _sCurrentFile);
            return;
        }

        //start analysis
        _Engine.setTransactionLog(sub_trans);
        int file_type = _cmbFileType.getSelectedIndex();
        switch (file_type) {
            case FrameworkConstants.FILE_FORMAT_CLEC:
                _btnNextSegment.setVisible(false);
                _lblStrategy.setText(_Engine.getStrategy());
                break;

            case FrameworkConstants.FILE_FORMAT_MDB:
                _btnNextSegment.setVisible(true);
                _lblStrategy.setText("MDB");
                break;

            case FrameworkConstants.FILE_FORMAT_SMT:
                _btnNextSegment.setVisible(false);
                _lblStrategy.setText("SMT");
                break;

            case FrameworkConstants.FILE_FORMAT_TEA_LEAF:
                _btnNextSegment.setVisible(false);
                _lblStrategy.setText("Tea Leaf");
                break;

            case FrameworkConstants.FILE_FORMAT_CUSTOM:
                _btnNextSegment.setVisible(false);
                _lblStrategy.setText("CUSTOM");
                break;
        }

        SimReport rpt = _Engine.genReport();
        rpt.printReport();

        //update tables and graphs
        _pnlTransaction.populate(rpt.getTransLog());
        _pnlTransaction.setEndEquity(FrameworkConstants.DOLLAR_FORMAT.format(rpt.getEndEquity()));
        _txtCagr.setText(FrameworkConstants.ROI_FORMAT.format(rpt.getCagr()));
        plotEquityGraph(rpt.getEquityLog(), rpt.getTransLog());
        _pnlStat.updateStat(rpt, false);
        if (file_type == FrameworkConstants.FILE_FORMAT_CLEC
             || file_type == FrameworkConstants.FILE_FORMAT_SMT
             || file_type == FrameworkConstants.FILE_FORMAT_CUSTOM) {
            plotAnnualReturnGraph(rpt);
            plotPriceGraph(_Engine.getSymbol());
        }
    }

    //display report in analyzer
    public void showReport(SimReport report) throws ParseException {
        report.printReport();
        //update tables and graphs
        _pnlTransaction.populate(report.getTransLog());
        _pnlTransaction.setEndEquity(FrameworkConstants.DOLLAR_FORMAT.format(report.getEndEquity()));
        _txtCagr.setText(FrameworkConstants.ROI_FORMAT.format(report.getCagr()));
        plotEquityGraph(report.getEquityLog(), report.getTransLog());
        _pnlStat.updateStat(report, false);
        plotAnnualReturnGraph(report.getAnnualReturns());
//todo make report show file type        if (file_type == com.clec.analyzer.resource.FrameworkConstants.FILE_FORMAT_CLEC
//             || file_type == com.clec.analyzer.resource.FrameworkConstants.FILE_FORMAT_SMT)
//            plotPriceGraph();
    }

    /**
     * plot annual return graph
     * @param sim_report the SimReport object with after simulation data
     */
    private void plotAnnualReturnGraph(SimReport sim_report) {
        ArrayList<AnnualReturn> ars = sim_report.getAnnualReturns();
        String sym = "Buy/Hold SP500";//for graph legend
        if (ars != null)
            sym = ars.get(0).getSymbol();
        TimeSeries ts = new TimeSeries(sym);
        TimeSeries sp = annualReturnToTimeSeries(FrameworkConstants.SP500_ANNUAL_RETURN);
        if (ars != null) {
            ts = annualReturnToTimeSeries(ars);

            //use partial SP500 data between ts first year and last year
            int start_yr = ars.get(0).getYear();
            int end_yr = ars.get(ars.size() - 1).getYear();
            ArrayList<AnnualReturn> sp_ars = new ArrayList<AnnualReturn>();
            for (AnnualReturn ar : FrameworkConstants.SP500_ANNUAL_RETURN) {
                if (ar.getYear() < start_yr || ar.getYear() > end_yr)
                    continue;
                sp_ars.add(ar);
            }
            sp = annualReturnToTimeSeries(sp_ars);
        }
        _pnlAnnualReturnGraph.updateGraph(ts, sp);
    }

    /**
     * Convert annual return array to plottable time series
     * @param ars array of AnnualReturn objects
     * @return corresponding TimeSeries objects.
     */
//    public static TimeSeries annualReturnToTimeSeries(ArrayList<AnnualReturn> ars) {
//        TimeSeries ts = new TimeSeries(ars.get(0).getSymbol());
//        for (AnnualReturn ar : ars) {
//            Year yr = new Year(ar.getYear());
//            ts.add(yr, ar.getPerformance() * 100);
//        }
//        return ts;
//    }

    //-----literals-----
    public static final String[] LIST_FILE_TYPE = {
        FrameworkConstants.FRAMEWORK_BUNDLE.getString("tab_sel_5"),
        FrameworkConstants.FRAMEWORK_BUNDLE.getString("tab_sel_2"),
        FrameworkConstants.FRAMEWORK_BUNDLE.getString("tab_sel_3"),
        FrameworkConstants.FRAMEWORK_BUNDLE.getString("tab_sel_4"),
        FrameworkConstants.FRAMEWORK_BUNDLE.getString("tab_sel_1"),
    };
    public static final String[] LIST_MDB_SEGMENTS = { "S1", "S2", "S3", "S4" };

    //-----instance variables-----
    private TransactionPanel _pnlTransaction;
    private File _sCurrentFile;
    private JComboBox _cmbFileType = new JComboBox(LIST_FILE_TYPE);
    private JButton _btnNextSegment = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("tip_next"), FrameworkIcon.NEXT);
    private JButton _btnOpen = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("tip_folder_open"), FrameworkIcon.FILE_OPEN);
    private JLabel _lblSymbol = new JLabel();
    private JLabel _lblStrategy = new JLabel();
    private JLabel _lblDateRange = new JLabel();
    private JComboBox<Integer> _cmbStartYear = new JComboBox();
    private JComboBox<Integer> _cmbEndYear = new JComboBox();
    private JButton _btnRun = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("tip_run"), FrameworkIcon.RUN);
    private AnalysisEngine _Engine = new AnalysisEngine();
    private String _sCurrentSegment = LIST_MDB_SEGMENTS[0];
    private ArrayList<Transaction> _OrigTransactions;
//    private JCheckBox _chkUseStop = new JCheckBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("lbl_use_stop"));
//    private JSpinner _spnStopPercent;
}