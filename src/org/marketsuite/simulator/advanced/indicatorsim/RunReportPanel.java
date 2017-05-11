package org.marketsuite.simulator.advanced.indicatorsim;

import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.component.dialog.ProgressBar;
import org.marketsuite.component.dialog.SchemaColumnDialog;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.DisabledIcon;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.table.DynaTableCellRenderer;
import org.marketsuite.component.table.SimpleCell;
import org.marketsuite.component.table.SortHeaderRenderer;
import org.marketsuite.component.table.TableUtil;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.StandardOption;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.framework.strategy.base.SimParam;
import org.marketsuite.framework.strategy.base.SimReport;
import org.marketsuite.framework.strategy.mac.MacEngine;
import org.marketsuite.framework.strategy.mac.MacOption;
import org.marketsuite.simulator.indicator.macd.MacdZeroCrossEngine;
import org.marketsuite.simulator.indicator.macd.MzcOption;
import org.marketsuite.simulator.indicator.rsi.RsiEngine;
import org.marketsuite.simulator.indicator.rsi.RsiOption;
import org.marketsuite.simulator.indicator.stochastic.StochasticEngine;
import org.marketsuite.simulator.indicator.stochastic.StochasticOption;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloPreferenceStore;
import org.marketsuite.simulator.advanced.SimGraphDialog;
import org.marketsuite.simulator.advanced.report.model.ReportTableModel;
import org.marketsuite.simulator.advanced.report.model.ReportTemplate;
import org.marketsuite.simulator.advanced.report.model.StrategySetting;
import org.marketsuite.simulator.advanced.report.model.TimeSetting;
import org.jfree.data.general.SeriesException;
import org.marketsuite.component.dialog.SchemaColumnDialog;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.DisabledIcon;
import org.marketsuite.component.table.SortHeaderRenderer;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.StandardOption;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.framework.strategy.base.SimReport;
import org.marketsuite.framework.strategy.mac.MacEngine;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.simulator.advanced.SimGraphDialog;
import org.marketsuite.simulator.advanced.report.model.ReportTableModel;
import org.marketsuite.simulator.advanced.report.model.ReportTemplate;
import org.marketsuite.simulator.advanced.report.model.StrategySetting;
import org.marketsuite.simulator.advanced.report.model.TimeSetting;
import org.marketsuite.simulator.indicator.macd.MacdZeroCrossEngine;
import org.marketsuite.simulator.indicator.macd.MzcOption;
import org.marketsuite.simulator.indicator.rsi.RsiEngine;
import org.marketsuite.simulator.indicator.stochastic.StochasticOption;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Sub-tab to run report from user selected report panels.
 */
class RunReportPanel extends SkinPanel implements PropertyChangeListener {
    RunReportPanel() {
        super(LazyIcon.BACKGROUND_CONTENT, new BorderLayout());

        //title strip - west combo box, run button, center - times, right - column hide tool
        JPanel west_pnl = new JPanel(); west_pnl.setOpaque(false);
        west_pnl.add(_cmbReports = new JComboBox<>(_Model));
        west_pnl.add(Box.createHorizontalGlue());
        west_pnl.add(_btnRunReport);
        _btnRunReport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
//                SimGraphDialog.getInstance().clear();
                runReport();
            }
        });
        west_pnl.add(_btnExport);
        _btnExport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    exportReports();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //east - two buttons
        JPanel east_pnl = new JPanel(); east_pnl.setOpaque(false);
        east_pnl.add(_btnGraph);  _btnGraph.setEnabled(false);
        _btnGraph.setDisabledIcon(new DisabledIcon(FrameworkIcon.PRICE_CHART.getImage()));
        _btnGraph.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SimGraphDialog dialog = SimGraphDialog.getInstance();
                dialog.setVisible(true);
                try {
                    dialog.refreshGraph(findReportByIndex(_tblReport.getSelectedRow()));
                } catch (ParseException | IOException e1) {
                    WidgetUtil.showWarning(MdiMainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("dme_txt_4") + e1.getMessage());
                }
            }
        });
        east_pnl.add(Box.createGlue());
        JLabel showhide_btn = new JLabel(LazyIcon.TABLE_COLUMN_OP);
        east_pnl.add(showhide_btn, BorderLayout.EAST);
        WidgetUtil.attachToolTip(showhide_btn, ApolloConstants.APOLLO_BUNDLE.getString("runrpt_lbl_1"),
            SwingConstants.RIGHT, SwingConstants.TOP);
        showhide_btn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent mev) {//to show / hide columns
                Image image = LazyIcon.APP_ICON.getImage();
                //gather column names from schema
                String[] column_names = new String[ReportTableModel.TABLE_SCHEMA.length];
                for (int row = 0; row < ReportTableModel.TABLE_SCHEMA.length; row++)
                    column_names[row] = (String) ReportTableModel.TABLE_SCHEMA[row][0];
                SchemaColumnDialog dlg = new SchemaColumnDialog(_tblReport, column_names,
                        MdiMainFrame.getInstance(), image, LOCKED_COLUMNS);
                dlg.setVisibleColumns(ApolloPreferenceStore.getPreferences().getAdvReportColumnVisible());
                dlg.setVisible(true);
                boolean[] visible_columns = dlg.getResult();
                if (null != visible_columns) {
                    ApolloPreferenceStore.getPreferences().setAdvReportColumnVisible(visible_columns);
                    ApolloPreferenceStore.savePreferences();
                    TableUtil.setColumnsVisible(_tblReport, visible_columns);
                }
            }
        });
        add(WidgetUtil.createTitleStrip(west_pnl, null, east_pnl), BorderLayout.NORTH);

        //report table
        _tblReport = WidgetUtil.createDynaTable(_ReportTableModel = new ReportTableModel(),
            ListSelectionModel.SINGLE_SELECTION, new SortHeaderRenderer(), true, new ReportRenderer());
        _tblReport.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        _tblReport.setAutoCreateRowSorter(true);
        _tblReport.setOpaque(false);
        JScrollPane scr = new JScrollPane(_tblReport);
        scr.getViewport().setOpaque(false);
        add(scr, BorderLayout.CENTER);
        add(WidgetUtil.createTitleStrip(ApolloConstants.APOLLO_BUNDLE.getString("runrpt_tip_1")), BorderLayout.SOUTH);
        TableUtil.fixColumns(scr, _tblReport, LOCKED_COLUMNS);

        //column order preference handler
        _tblReport.getColumnModel().addColumnModelListener(new TableUtil.TableColumnModelAdapter() {
            public void columnMoved(TableColumnModelEvent ev) {
                if (TableUtil.columnMoved(_tblReport, ev.getFromIndex(), ev.getToIndex())) {
                    ApolloPreferenceStore.getPreferences().setAdvReportColumnOrder(TableUtil.getColumnOrder(_tblReport, LOCKED_COLUMNS));
                    ApolloPreferenceStore.savePreferences();
                }
            }
        });
        populateReportList();

        //initialze column preference
        boolean[] visible = ApolloPreferenceStore.getPreferences().getAdvReportColumnVisible();
        if (null != visible)
            TableUtil.setColumnsVisible(_tblReport, visible);
        int[] order = ApolloPreferenceStore.getPreferences().getAdvReportColumnOrder();
        if (null != order)
            TableUtil.setColumnOrder(_tblReport, order);

        //select row to update graph window
        _tblReport.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting())
                    return;

                //refresh graph use cached SimReport
                int sel = _tblReport.getSelectedRow();
                _btnGraph.setEnabled(sel != -1);
                if (sel == -1) //no selection
                    return;

                SimReport rpt = findReportByIndex(sel);
                try {
                    SimGraphDialog dlg = SimGraphDialog.getInstance();
                    if (dlg.isVisible())
                        dlg.refreshGraph(rpt);
                } catch (ParseException e1) {
                    e1.printStackTrace();
                    WidgetUtil.showWarning(MdiMainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("dme_txt_4") + e1.getMessage());
                } catch (IOException e1) {
                    e1.printStackTrace();
                    WidgetUtil.showWarning(MdiMainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("dme_txt_4") + e1.getMessage());
                } catch (SeriesException e1) {
                    e1.printStackTrace();
                    WidgetUtil.showWarning(MdiMainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("dme_txt_4") + e1.getMessage());
                }
            }
        });

        //initialize engine
        FundData fund = null;
        _Engines[INDEX_MAC] = new MacEngine(fund);
        _Engines[INDEX_MACD_ZERO_CROSS] = new MacdZeroCrossEngine(fund);
        _Engines[INDEX_RSI] = new RsiEngine(fund);
        _Engines[INDEX_STOCHASTIC] = new StochasticEngine(fund);
        Props.addWeakPropertyChangeListener(Props.TemplateChange, this);//handle symbol change
    }

    public void propertyChange(PropertyChangeEvent evt) {
        Props prop = (Props) evt.getSource();
        switch (prop) {
            case TemplateChange://this will update all panels that uses this combo
                populateReportList();
                _cmbReports.setSelectedItem(prop.getValue());
                break;
        }
    }

    /**
     * When user clicks this tab, refresh file list in case add/delete took place prior.
     */
    public void populateReportList() {
        ArrayList<ReportTemplate> templates = ApolloPreferenceStore.getPreferences().getAdvReportTemplates();
        _Model.removeAllElements();
        for (ReportTemplate template : templates)
            _Model.addElement(template.getReportName());
    }

    //----- private methods -----
    private ArrayList<Transaction> runSymbol(final AbstractEngine engine, final ProgressBar pb, final String symbol) {
        EventQueue.invokeLater(new Runnable() {//change prog bar text
            public void run() {
                pb.setLabel(ApolloConstants.APOLLO_BUNDLE.getString("cmn_strategy") + " : " +
                        engine.getStrategy() + " : " +
                        ApolloConstants.APOLLO_BUNDLE.getString("runrpt_msg_1") + " " + symbol +
                        ApolloConstants.APOLLO_BUNDLE.getString("dld_msg_3"));
            }
        });
        engine.simulate();
        return engine.getTransactionLog();
    }

    private void addReportRow(String symbol, int strategy_index, String start_date) {
        SimReport rpt = _Engines[strategy_index].genReport();
        String strategy = _Engines[strategy_index].getStrategy();
        _ReportTableModel.addRow(symbol, strategy,
            _Engines[strategy_index].getStrategyInfo(), rpt, start_date);
        _SimReports.put(symbol + "_" + strategy, rpt);//key is unique
    }

    /**
     * Use background thread to batch simulate and export .csv transaction files.
     */
    private void runReport() {
        _ReportTableModel.clear();
        _SimReports = new HashMap<String, SimReport>();

        //show progress bar
        _lblEndDate.setText("");
        final ProgressBar pb = ProgressBar.getInstance(MdiMainFrame.getInstance(), "");
        pb.setVisible(true);

        //start a thread to simulate and export all files
        Thread thread = new Thread() {
            public void run() {
                final ArrayList<String> failed_sims = new ArrayList<String>();
                //step1: read list of symbols from selected report template
                ArrayList<ReportTemplate> templates = ApolloPreferenceStore.getPreferences().getAdvReportTemplates();
                final String rpt_name = (String) _cmbReports.getSelectedItem();
                ReportTemplate cur_template = null;
                for (ReportTemplate tpl : templates)
                    if (tpl.getReportName().equals(rpt_name)) {
                        cur_template = tpl;
                        break;
                    }
                if (cur_template == null) {
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            WidgetUtil.showWarningInEdt(MdiMainFrame.getInstance(),
                                ApolloConstants.APOLLO_BUNDLE.getString("advsim_msg_6") + rpt_name, pb);
                        }
                    });
                    return;
                }

                //first, obtain user settings from report setting tab
                try {
                    TimeSetting time_setting = cur_template.getTimeSetting();
                    final String time_type = time_setting.getType();
                    HashMap<String, ArrayList<Boolean>> sym_map = cur_template.getSymbolMap();

                    //find out whether user specified begin/end dates are valid, if not, automatically find next valid date
                    if (time_type.equals(TimeSetting.TIME_PARTIAL_RANGE)) {
                        HashMap<String, FundQuote> begin_cals = new HashMap<String, FundQuote>();
                        final String begin_date = time_setting.getBeginDate();
                        final String end_date = time_setting.getEndDate();
                        for (String symbol : sym_map.keySet()) {
                            final String sym = symbol;
                            EventQueue.invokeLater(new Runnable() {
                                public void run() {
                                    pb.setLabel(ApolloConstants.APOLLO_BUNDLE.getString("runrpt_msg_2") + " " + sym);
                                }
                            });
                            FundData fund = DataUtil.readFundHistory(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, symbol);
                            final FundQuote begin_quote = AppUtil.findNearestQuote(fund, AppUtil.stringToCalendar(begin_date));
                            final FundQuote end_quote = AppUtil.findNearestQuote(fund, AppUtil.stringToCalendar(end_date));
                            if (begin_quote == null || end_date == null) {//can't find anything, begin date is in the future
                                EventQueue.invokeLater(new Runnable() {
                                    public void run() {
                                        StringBuilder dt = new StringBuilder();
                                        dt.append(begin_quote == null ? begin_date : "").append(" : ")
                                          .append(end_quote == null ? end_date : "");
                                        WidgetUtil.showWarningInEdt(MdiMainFrame.getInstance(),
                                            ApolloConstants.APOLLO_BUNDLE.getString("advsim_msg_4") + dt.toString(), pb);
                                    }
                                });
                                return;
                            }
                            begin_cals.put(symbol, begin_quote);//add to begin_cals list
                        }

                        //find common starting date, the latest starting date among all
                        Calendar begin_cal = null;
                        for (String symbol : begin_cals.keySet()) {
                            FundQuote quote = begin_cals.get(symbol);
                            Calendar cal = AppUtil.stringToCalendar(quote.getDate());
                            if (begin_cal == null || cal.compareTo(begin_cal) >= 0) //keep later ones
                                begin_cal = cal;
                        }
                        if (begin_cal == null) {
                            EventQueue.invokeLater(new Runnable() {
                                public void run() {
                                    WidgetUtil.showWarningInEdt(MdiMainFrame.getInstance(),
                                        ApolloConstants.APOLLO_BUNDLE.getString("advsim_msg_5"), pb);
                                }
                            });
                            return;
                        }
                        time_setting.setBeginDate(AppUtil.calendarToString(begin_cal));

                        //find common ending date, use SP500 as guide, if date is not in SP500, maybe in the future
                        FundQuote end_quote = AppUtil.findNearestQuote(FrameworkConstants.SP500_DATA,
                            AppUtil.stringToCalendar(end_date));
                        if (end_quote == null) {//can't find anything, end date is in the future
                            WidgetUtil.showWarningInEdt(MdiMainFrame.getInstance(),
                                ApolloConstants.APOLLO_BUNDLE.getString("advsim_msg_4") + end_date, pb);
                            return;
                        }
                        time_setting.setEndDate(end_quote.getDate());
                    }

                    //obtain strategy settings from template
                    StrategySetting str_setting = cur_template.getStrategySetting();
                    MacOption mac_opt = str_setting.getMacSetting();
                    MzcOption mzc_opt = str_setting.getMzcSetting();
                    RsiOption rsi_option = str_setting.getRsiSetting();
                    StochasticOption sto_opt = str_setting.getStochasticSetting();

                    //iterate all symbols, simulate each strategy, place result in memory for table display
                    for (String symbol : sym_map.keySet()) {
                        FundData fund = DataUtil.readFundHistory(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, symbol);
                        for (int i = INDEX_MAC; i <= INDEX_STOCHASTIC; i++)
                            _Engines[i].setFund(fund);

                        //setup simulation options, use full range as default, build up standard options
                        String start_date = fund.getDate(fund.getSize() - 1);
                        String end_date = fund.getQuote().get(0).getDate();
                        if (time_type.equals(TimeSetting.TIME_PARTIAL_RANGE)) {
                            start_date = time_setting.getBeginDate();
                            end_date = time_setting.getEndDate();
                        }
                        else if (time_type.equals(TimeSetting.TIME_SKIP_INITIAL)) {
                            int skip_month = time_setting.getSkipMonth();
                            try {
                                Calendar cal = AppUtil.stringToCalendar(start_date);
                                cal.add(Calendar.MONTH, skip_month);
                                FundQuote quote = AppUtil.findNearestQuote(fund, cal);
                                if (quote == null) {
                                    final String sd = start_date;
                                    WidgetUtil.showWarningInEdt(MdiMainFrame.getInstance(),
                                        ApolloConstants.APOLLO_BUNDLE.getString("advsim_msg_3") + " " + sd, pb);
                                    return;
                                }
                                start_date = quote.getDate();
                            } catch (ParseException e) {
                                e.printStackTrace();
                                WidgetUtil.showWarningInEdt(MdiMainFrame.getInstance(),
                                    ApolloConstants.APOLLO_BUNDLE.getString("advsim_msg_3") + start_date, pb);
                                return;
                            }
                        }
                        StandardOption std_opt = new StandardOption(symbol, true, false, start_date, end_date, true);
                        final String ed = end_date;
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                if (time_type.equals(TimeSetting.TIME_PARTIAL_RANGE) ||
                                        time_type.equals(TimeSetting.TIME_SKIP_INITIAL)) {
                                    _lblEndDate.setText(ed);
                                }
                            }
                        });

                        //custom setting: MAC strategy
                        if (sym_map.get(symbol).get(INDEX_MAC)) {//checkbox selected
                            _Engines[INDEX_MAC].setSimParam(new SimParam(std_opt, mac_opt));
                            ArrayList<Transaction> trans = null;
                            try {
                                trans = runSymbol(_Engines[INDEX_MAC], pb, symbol);
                            } catch (IllegalArgumentException e) {
                                e.printStackTrace();
                                failed_sims.add(e.getMessage());
                                continue;
                            }
                            if (trans != null && trans.size() > 0)//nothing to show
                                addReportRow(symbol, INDEX_MAC, start_date);
                            else
                                failed_sims.add("<b>" + symbol + "</b> " + ApolloConstants.APOLLO_BUNDLE.getString("advsim_msg_7")
                                    + _Engines[INDEX_MAC].getId() + ")");
                        }

                        //custom setting: MACD Zero Cross strategy
                        if (sym_map.get(symbol).get(INDEX_MACD_ZERO_CROSS)) {//checkbox selected
                            _Engines[INDEX_MACD_ZERO_CROSS].setSimParam(new SimParam(std_opt, mzc_opt));
                            ArrayList<Transaction> trans = null;
                            try {
                                trans = runSymbol(_Engines[INDEX_MACD_ZERO_CROSS], pb, symbol);
                            } catch (Exception e) {
                                e.printStackTrace();
                                failed_sims.add(e.getMessage());
                                continue;
                            }
                            if (trans != null && trans.size() > 0)//nothing to show
                                addReportRow(symbol, INDEX_MACD_ZERO_CROSS, start_date);
                            else
                                failed_sims.add("<b>" + symbol + "</b> " + ApolloConstants.APOLLO_BUNDLE.getString("advsim_msg_7")
                                    + _Engines[INDEX_MACD_ZERO_CROSS].getId() + ")");
                        }

                        //custom setting: RSI strategy
                        if (sym_map.get(symbol).get(INDEX_RSI)) {//checkbox selected
                            _Engines[INDEX_RSI].setSimParam(new SimParam(std_opt, rsi_option));
                            ArrayList<Transaction> trans = null;
                            try {
                                trans = runSymbol(_Engines[INDEX_RSI], pb, symbol);
                            } catch (Exception e) {
                                e.printStackTrace();
                                failed_sims.add(e.getMessage());
                                continue;
                            }
                            if (trans != null && trans.size() > 0)//nothing to show
                                addReportRow(symbol, INDEX_RSI, start_date);
                            else
                                failed_sims.add("<b>" + symbol + "</b> " + ApolloConstants.APOLLO_BUNDLE.getString("advsim_msg_7")
                                        + _Engines[INDEX_RSI].getId() + ")");
                        }

                        //custom setting: Stochastic strategy
                        if (sym_map.get(symbol).get(INDEX_STOCHASTIC)) {//checkbox selected
                            _Engines[INDEX_STOCHASTIC].setSimParam(new SimParam(std_opt, sto_opt));
                            ArrayList<Transaction> trans = null;
                            try {
                                trans = runSymbol(_Engines[INDEX_STOCHASTIC], pb, symbol);
                            } catch (Exception e) {
                                e.printStackTrace();
                                failed_sims.add(e.getMessage());
                                continue;
                            }
                            if (trans != null && trans.size() > 0)//nothing to show
                                addReportRow(symbol, INDEX_STOCHASTIC, start_date);
                            else
                                failed_sims.add("<b>" + symbol + "</b> " + ApolloConstants.APOLLO_BUNDLE.getString("advsim_msg_7")
                                        + _Engines[INDEX_STOCHASTIC].getId() + ")");
                        }
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                _ReportTableModel.fireTableDataChanged();
                            }
                        });
                    }
                } catch (Exception e) {//engine complains about something
                    e.printStackTrace();
                    failed_sims.add(e.getMessage());
                }
                EventQueue.invokeLater(new Runnable() {//hide progress bar
                    public void run() {
                        if (failed_sims.size() > 0) {
                            StringBuilder sb = new StringBuilder(ApolloConstants.APOLLO_BUNDLE.getString("dme_msg_2"));
                            for (String sim : failed_sims)
                                sb.append(sim).append("<br>");
                            WidgetUtil.showWarning(MdiMainFrame.getInstance(), sb.toString());
                        }
                        pb.setVisible(false);
                    }
                });
            }
        };
        thread.start();
    }

    private SimReport findReportByIndex(int row_index) {
        int sel = _tblReport.convertRowIndexToModel(row_index);
        String sym = (String)_ReportTableModel.getCell(sel, ReportTableModel.COLUMN_SYMBOL).getValue();
        String strategy = (String)_ReportTableModel.getCell(sel, ReportTableModel.COLUMN_STRATEGY).getValue();
        return _SimReports.get(sym + "_" + strategy);
    }

    private void exportReports() throws IOException {
        //ask user for file name and rsp
        JFileChooser fc = new JFileChooser(new File(FrameworkConstants.DATA_FOLDER_REPORT));
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int rsp = fc.showSaveDialog(null);//todo MainFrame.getInstance() centering
        if (rsp == JFileChooser.APPROVE_OPTION) {
            File output_path = fc.getSelectedFile();
            if (output_path.exists()) { //warn user if file exist
                if (MessageBox.messageBox(
                        null, //todo MainFrame.getInstance(),
                        FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                        FrameworkConstants.FRAMEWORK_BUNDLE.getString("exp_msg_1"),
                        MessageBox.STYLE_OK_CANCEL, MessageBox.WARNING_MESSAGE) != MessageBox.RESULT_OK)
                    return;
            }

            //write lines into this file from table model
            PrintWriter pw = new PrintWriter(new FileWriter(output_path + FrameworkConstants.EXTENSION_TRADES));
            pw.println("NAME=" + _cmbReports.getSelectedItem());
            pw.println("Symbol, Strategy, CAGR, ROI, Annual %, #Trade, Trades/Year, Win Ratio, PF, In Market%, " +
                    "End Equity, Net Gain$, Net Loss$, Avg Gain%, Avg Loss%, Avg DD%, Max Gain%, Max Loss%, " +
                    "Max DD%, Min Gain%, Min Loss%, Min DD%, Avg Gain$, Avg Loss$, Avg DD$, Max Gain$, Max Loss$, " +
                    "Max DD$, Min Gain$, Min Loss$, Min DD$, Strategy Detail");
            int row_cnt = _ReportTableModel.getRowCount();
            for (int row=0; row<row_cnt; row++) {
                SimpleCell[] cells = _ReportTableModel.getRow(row);
                StringBuilder sb = new StringBuilder();
                for (SimpleCell cell : cells)
                    sb.append(cell.getValue()).append(",");
                pw.println(sb.toString());
            }
            pw.flush();
            pw.close();
        }
    }

    //-----inner classes-----
    private class ReportRenderer extends DynaTableCellRenderer {
        private ReportRenderer() {
            super(_ReportTableModel, null);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            int modle_column = table.convertColumnIndexToModel(col);//after dragging
            int model_row = table.convertRowIndexToModel(row);
            JLabel comp = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            comp.setToolTipText("");
            switch (modle_column) {
                case ReportTableModel.COLUMN_SYMBOL://use last column strategy info as tooltip
                    comp.setToolTipText(
                            "<html>" + _ReportTableModel.getCell(model_row, ReportTableModel.COLUMN_SYMBOL).getValue() +
                                    "<br>" + _ReportTableModel.getCell(model_row, ReportTableModel.COLUMN_INFO).getValue());
                    break;

                case ReportTableModel.COLUMN_CAGR:
                case ReportTableModel.COLUMN_ROI:
                case ReportTableModel.COLUMN_ANNUAL:
                case ReportTableModel.COLUMN_WIN_RATIO:
                case ReportTableModel.COLUMN_IN_MKT:
                case ReportTableModel.COLUMN_AVG_GAIN_PCT:
                case ReportTableModel.COLUMN_AVG_LOSS_PCT:
                case ReportTableModel.COLUMN_AVG_DD_PCT:
                case ReportTableModel.COLUMN_MAX_GAIN_PCT:
                case ReportTableModel.COLUMN_MAX_LOSS_PCT:
                case ReportTableModel.COLUMN_MAX_DD_PCT:
                case ReportTableModel.COLUMN_MIN_GAIN_PCT:
                case ReportTableModel.COLUMN_MIN_LOSS_PCT:
                case ReportTableModel.COLUMN_MIN_DD_PCT:
                    comp.setText(FrameworkConstants.ROI_FORMAT.format(value));
                    break;

                case ReportTableModel.COLUMN_END_EQUITY:
                case ReportTableModel.COLUMN_NET_GAIN:
                case ReportTableModel.COLUMN_NET_LOSS:
                case ReportTableModel.COLUMN_AVG_GAIN:
                case ReportTableModel.COLUMN_AVG_LOSS:
                case ReportTableModel.COLUMN_AVG_DD:
                case ReportTableModel.COLUMN_MAX_GAIN:
                case ReportTableModel.COLUMN_MAX_LOSS:
                case ReportTableModel.COLUMN_MAX_DD:
                case ReportTableModel.COLUMN_MIN_GAIN:
                case ReportTableModel.COLUMN_MIN_LOSS:
                case ReportTableModel.COLUMN_MIN_DD:
                    comp.setText(FrameworkConstants.DOLLAR_FORMAT.format(value));
                    break;
            }
            return comp;
        }
    }

    //-----instance variables-----
    private JComboBox _cmbReports;
    private DefaultComboBoxModel _Model = new DefaultComboBoxModel();
    private JButton _btnExport = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_tip_5"), FrameworkIcon.EXPORT);
    private JButton _btnRunReport = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("advsim_lbl_6"), FrameworkIcon.RUN);
    private JButton _btnGraph = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("runrpt_tip_2"), FrameworkIcon.PRICE_CHART);
    private JLabel _lblEndDate = new JLabel("", 10);
    private AbstractEngine[] _Engines = new AbstractEngine[4];//checkbox and engine share same index
    private JTable _tblReport;
    private ReportTableModel _ReportTableModel;
    private HashMap<String, SimReport> _SimReports;//for graphing

    //-----literals-----
    private static final int LOCKED_COLUMNS = 2;//first 2 columns are locked, ie.they cannot be hidden
    private static final int INDEX_MAC = 0;
    private static final int INDEX_MACD_ZERO_CROSS = 1;
    private static final int INDEX_RSI = 2;
    private static final int INDEX_STOCHASTIC = 3;
}