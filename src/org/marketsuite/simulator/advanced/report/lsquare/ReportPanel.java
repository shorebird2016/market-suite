package org.marketsuite.simulator.advanced.report.lsquare;

import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.component.dialog.ProgressBar;
import org.marketsuite.component.table.DynaTableCellRenderer;
import org.marketsuite.component.table.SimpleCell;
import org.marketsuite.component.table.SortHeaderRenderer;
import org.marketsuite.component.table.TableUtil;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.strategy.base.SimReport;
import org.marketsuite.framework.strategy.lsquare.LSquareEngine;
import org.marketsuite.framework.strategy.lsquare.LSquareParam;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloPreferenceStore;
import org.marketsuite.simulator.advanced.SimGraphDialog;
import org.marketsuite.simulator.advanced.report.model.ReportTableModel;
import net.miginfocom.swing.MigLayout;
import org.jfree.data.general.SeriesException;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.strategy.lsquare.LSquareEngine;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

class ReportPanel extends JPanel {
    ReportPanel() {
        setLayout(new MigLayout("insets 0")); setOpaque(false);
        _tblReport = WidgetUtil.createDynaTable(_ReportTableModel = new ReportTableModel(),
            ListSelectionModel.SINGLE_SELECTION, new SortHeaderRenderer(), true, new ReportRenderer());
        _tblReport.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        _tblReport.setAutoCreateRowSorter(true);
        _tblReport.setOpaque(false);
        JScrollPane scr = new JScrollPane(_tblReport);
        scr.getViewport().setOpaque(false);
        add(scr, "dock center");
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
                if (sel == -1) //no selection
                    return;

                sel = _tblReport.convertRowIndexToModel(sel);//convert for sorting
                String sym = (String)_ReportTableModel.getCell(sel, ReportTableModel.COLUMN_SYMBOL).getValue();
                Props.SymbolSelection.setValue(sym);
                SimReport rpt = findReportByIndex(sel);
                try {
                    SimGraphDialog dlg = SimGraphDialog.getInstance();
                    if (dlg.isVisible())
                        dlg.refreshGraph(rpt);
                } catch (ParseException | IOException | SeriesException e1) {
                    e1.printStackTrace();
                    WidgetUtil.showWarning(MdiMainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("dme_txt_4") + e1.getMessage());
                }
            }
        });
    }

    //----- protected methods -----
    /**
     * Use background thread to batch simulate and export .csv transaction files.
     */
    void runReport(final ArrayList<String> symbols, final JTextField stat_label, final LSquareParam param) {
        _ReportTableModel.clear();
        _Engine = new LSquareEngine(null); _SimReports = new HashMap<>();
        _nLossCount = 0; _nSkipCount = 0; _nNoTransCount = 0; _nTransCount = 0; _nWinCount = 0;

        //show progress bar
        final ProgressBar pb = ProgressBar.getInstance(MdiMainFrame.getInstance(), "");
        pb.setVisible(true);

        //start a thread to simulate and export all files
        Thread thread = new Thread() {
            public void run() {
                for (final String symbol : symbols) {
                    FundData fund;
                    try {
                        fund = DataUtil.readFundHistory(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, symbol);
                    } catch (final IOException e) {
                        e.printStackTrace();
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("err_failquote") +
                                        " " + symbol, LoggingSource.SIMULATOR_LSQUARE);
                            }
                        });
                        continue;
                    }
                    _Engine.setSimParam(param);
                    _Engine.setFund(fund);
                    String start_date = fund.getDate(fund.getSize() - 1);//full range
                    EventQueue.invokeLater(new Runnable() {//change prog bar text
                        public void run() {
                            pb.setLabel(ApolloConstants.APOLLO_BUNDLE.getString("cmn_strategy") + " : " +
                                _Engine.getStrategy() + " : " +
                                ApolloConstants.APOLLO_BUNDLE.getString("runrpt_msg_1") + " " + symbol +
                                ApolloConstants.APOLLO_BUNDLE.getString("dld_msg_3"));
                        }
                    });
                    if (!_Engine.simulate()) {
                        _nSkipCount++;//pre-mature exit
                        continue;
                    }
                    ArrayList<Transaction> trans = _Engine.getTransactionLog();
                    if (trans != null && trans.size() > 0) //has result
                        addReportRow(symbol, start_date);
                    else {
                        LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("err_notrans") + " " + symbol, LoggingSource.SIMULATOR_LSQUARE);
                        _nNoTransCount++;
                    }
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            _ReportTableModel.fireTableDataChanged();
                        }
                    });
                }
                EventQueue.invokeLater(new Runnable() {//hide progress bar
                    public void run() {
                        pb.setVisible(false);
                        float ratio = (float)(_nWinCount + _nNoTransCount) / (_nNoTransCount + _nLossCount + _nWinCount);
                        StringBuilder buf = new StringBuilder(FrameworkConstants.PCT2_FORMAT.format(ratio));
                        buf.append(" (").append(symbols.size())
                           .append(", ").append(_nTransCount).append(", ").append(_nSkipCount)
                           .append(", ").append(_nNoTransCount)
                           .append(", ").append(_nLossCount).append(", ").append(_nWinCount).append(")");
                        stat_label.setText(buf.toString());
                    }
                });
            }
        };
        thread.start();
    }
    SimReport findReportByIndex(int row_index) {//already converted for sorting
        String sym = (String)_ReportTableModel.getCell(row_index, ReportTableModel.COLUMN_SYMBOL).getValue();
        String strategy = (String)_ReportTableModel.getCell(row_index, ReportTableModel.COLUMN_STRATEGY).getValue();
        return _SimReports.get(sym + "_" + strategy);
    }
    void exportReports() throws IOException {
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
            pw.println("NAME=" + "L Square Reports");
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

    //----- private methods -----
    private void addReportRow(String symbol, String start_date) {
        SimReport rpt = _Engine.genReport();
        String strategy = _Engine.getStrategy();
        _ReportTableModel.addRow(symbol, strategy, _Engine.getStrategyInfo(), rpt, start_date);
        _SimReports.put(symbol + "_" + strategy, rpt);//key is unique

        //calculate win/loss count
        for (Transaction tran : rpt.getTransLog()) {
            if (tran.getPerformance() >= 0) _nWinCount++;
            else _nLossCount++;
            _nTransCount++;
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
//TODO sometime this gets exception, keep temporarily
                    try {
                        comp.setText(FrameworkConstants.ROI_FORMAT.format(value));
                    } catch (Exception e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
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

    //----- accessor -----
    JTable getTable() { return _tblReport;}

    //-----instance variables-----
    private LSquareEngine _Engine;
    private JTable _tblReport;
    private ReportTableModel _ReportTableModel;
    private HashMap<String, SimReport> _SimReports;//for graphing
    private int _nLossCount;
    private int _nSkipCount;//not simulated for lack of info
    private int _nNoTransCount;//symbols w/o transactions (normal)
    private int _nTransCount;//all transactions that occurred
    private int _nWinCount;//ones with >= 0 return

    //-----literals-----
    private static final int LOCKED_COLUMNS = 1;//first column is locked, ie.they cannot be hidden
}