package org.marketsuite.riskmgr.account;

import org.marketsuite.component.Constants;
import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.component.dialog.NameDialog;
import org.marketsuite.component.dialog.ProgressBar;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.DisabledIcon;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.table.DirtyCellRenderer;
import org.marketsuite.component.table.DynaTableCellRenderer;
import org.marketsuite.component.table.SortHeaderRenderer;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.riskmgr.model.Position;
import org.marketsuite.riskmgr.model.RiskMgrModel;
import org.marketsuite.riskmgr.model.StopLevelInfo;
import jxl.Workbook;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import net.miginfocom.swing.MigLayout;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.marketsuite.component.Constants;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.DisabledIcon;
import org.marketsuite.component.table.DirtyCellRenderer;
import org.marketsuite.component.table.DynaTableCellRenderer;
import org.marketsuite.component.table.SortHeaderRenderer;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.riskmgr.model.Position;
import org.marketsuite.riskmgr.model.RiskMgrModel;
import org.marketsuite.riskmgr.model.StopLevelInfo;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Container holding statistics of open positions.
 */
public class AccountViewPanel extends SkinPanel implements PropertyChangeListener {
    public AccountViewPanel() {
        super(LazyIcon.BACKGROUND_CONTENT, new MigLayout()); setOpaque(false);

        //title strip - buttons and labels
        JPanel ttl_pnl = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER, new MigLayout("insets 0", "5[]10[]10[]10[]10[]10[]10[]push[][]10[]push[][]5", "3[]3"));
        ttl_pnl.add(_btnCalculator); //_btnCalculator.setEnabled(false);
        _btnCalculator.setDisabledIcon(new DisabledIcon(FrameworkIcon.CALCULATOR.getImage()));
        _btnCalculator.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String symbol = null;
                int sel = _tblPortfolio.getSelectedRow();
                if (sel >= 0)
                    symbol = (String)_TableModel.getCell(sel, AccountViewTableModel.COLUMN_SYMBOL).getValue();
                if (_dlgAddShare == null)//only once
                    _dlgAddShare = new AddSharesDialog(symbol);
                _dlgAddShare.setVisible(true);
            }
        });
        ttl_pnl.add(_btnSave); _btnSave.setEnabled(false);
        _btnSave.setDisabledIcon(new DisabledIcon(FrameworkIcon.FILE_SAVE.getImage()));
        _btnSave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (!_TableModel.isDirty()) return;
                try {
                    _TableModel.saveStops(new File(FrameworkConstants.STOPS_DB));
                    _btnSave.setEnabled(false);
                    _TableModel.clearDirty();
                } catch (IOException e) {
                    MessageBox.messageBox(MdiMainFrame.getInstance(),
                        FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                        FrameworkConstants.FRAMEWORK_BUNDLE.getString("db_msg_3") + " " + e.getMessage(),
                        MessageBox.STYLE_OK, MessageBox.WARNING_MESSAGE);
                }
            }
        });
        ttl_pnl.add(_btnWatchlist); _btnWatchlist.setEnabled(false);
        _btnWatchlist.setDisabledIcon(new DisabledIcon(FrameworkIcon.WATCH.getImage()));
        _btnWatchlist.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //ask name
                NameDialog dlg = new NameDialog(MdiMainFrame.getInstance(), "");
                if (dlg.isCancelled())
                    return;
                String name = dlg.getEntry();

                //check duplicate list name
                if (GroupStore.getInstance().isGroupExist(name)) {
                    MessageBox.messageBox(MdiMainFrame.getInstance(),
                        Constants.COMPONENT_BUNDLE.getString("warning"),
                        ApolloConstants.APOLLO_BUNDLE.getString("rm_89"),
                        MessageBox.OK_OPTION, MessageBox.IMAGE_WARNING);
                    return;
                }

                //collect symbols, remove duplicate
                ArrayList<String> list = _TableModel.getSymbols();
                GroupStore.getInstance().addGroup(name, list);//empty group persist
                Props.WatchListsChange.setChanged();//notify watch list manager
            }
        });
        ttl_pnl.add(_btnUndo); _btnUndo.setEnabled(false);
        _btnUndo.setDisabledIcon(new DisabledIcon(FrameworkIcon.UNDO.getImage()));
        _btnUndo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _TableModel.discardChanges();
            }
        });
        ttl_pnl.add(_btnUpdateQuote); _btnUpdateQuote.setEnabled(false);
        _btnUpdateQuote.setDisabledIcon(new DisabledIcon(FrameworkIcon.REFRESH.getImage()));
        _btnUpdateQuote.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateQuotes();
//                _dlgAddShare.populate();
            }
        });
        ttl_pnl.add(_btnThumbnail); _btnThumbnail.setEnabled(false);
        _btnThumbnail.setDisabledIcon(new DisabledIcon(FrameworkIcon.THUMBNAIL.getImage()));
        _btnThumbnail.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ArrayList<String> symbols = _TableModel.getSymbols();
                Props.PlotThumbnails.setValue(null, symbols);
            }
        });
        ttl_pnl.add(_btnExport);
        _btnExport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File output_file = new File(FrameworkConstants.DATA_FOLDER_EXPORT + File.separator + "Planning Sheet" + FrameworkConstants.EXTENSION_XLS);
                try {
                    WritableWorkbook wb = Workbook.createWorkbook(output_file);
                    WritableSheet ws = wb.createSheet(output_file.getName(), 0);

                    //header
                    ws.addCell(new jxl.write.Label(0, 0, "Symbol"));
                    ws.addCell(new jxl.write.Label(1, 0, "Share"));
                    ws.addCell(new jxl.write.Label(2, 0, "Cost"));
                    ws.addCell(new jxl.write.Label(3, 0, "Action"));
                    ws.addCell(new jxl.write.Label(4, 0, "#"));
                    ws.addCell(new jxl.write.Label(5, 0, "Stop"));
                    ws.addCell(new jxl.write.Label(6, 0, "Risk"));
                    ws.addCell(new jxl.write.Label(7, 0, "Trigger Condition"));
                    ws.addCell(new jxl.write.Label(8, 0, "C R T Notes"));

                    //values from table model
                    for (int row = 1; row < _TableModel.getRowCount() + 1; row++) {
                        ws.addCell(new jxl.write.Label(0, row, (String)_TableModel.getCell(row - 1, AccountViewTableModel.COLUMN_SYMBOL).getValue()));
                        ws.addCell(new jxl.write.Label(1, row, String.valueOf(_TableModel.getCell(row - 1, AccountViewTableModel.COLUMN_SHARES).getValue())));
                        ws.addCell(new Number(5, row, (Double)_TableModel.getCell(row - 1, AccountViewTableModel.COLUMN_STOP_PRICE).getValue()));
                    }
                    wb.write();
                    wb.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

            }
        });

        //cost based risks
        ttl_pnl.add(_txtCostRisk = WidgetUtil.createBasicField(6, false, true, ApolloConstants.APOLLO_BUNDLE.getString("rm_55"))); _txtCostRisk.setText("0.00%");
        ttl_pnl.add(_txtCostRiskPercent = WidgetUtil.createBasicField(5, false, true, ApolloConstants.APOLLO_BUNDLE.getString("rm_56")));
        ttl_pnl.add(_lblCashPct = WidgetUtil.createBasicField(5, false, true, ApolloConstants.APOLLO_BUNDLE.getString("rm_65")));
        ttl_pnl.add(new JLabel("#")); ttl_pnl.add(_lblPosCount);
        add(ttl_pnl, "dock north");

        //center - table, graph in a vertical split pane
        JSplitPane cen_pnl = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        cen_pnl.setDividerLocation(400);
        cen_pnl.setContinuousLayout(true);
        cen_pnl.setDividerSize(Constants.DEFAULT_SPLITTER_WIDTH);

        //top side - position table and stop worksheet
        _TableModel = new AccountViewTableModel();
        _tblPortfolio = WidgetUtil.createDynaTable(_TableModel, ListSelectionModel.SINGLE_SELECTION,
            new SortHeaderRenderer(), true, new AccountCellRenderer());
        _tblPortfolio.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        _tblPortfolio.getTableHeader().setReorderingAllowed(false);
        _tblPortfolio.setAutoCreateRowSorter(true);
        _Sorter = _tblPortfolio.getRowSorter(); autoSort();
        _tblPortfolio.getColumnModel().getColumn(AccountViewTableModel.COLUMN_DIRTY).setCellRenderer(new DirtyCellRenderer(_TableModel, _Sorter));
        _tblPortfolio.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;

                //nothing selected, disable delete, close button
                int row = _tblPortfolio.getSelectedRow();
                if (row == -1) {
                    _pnlPriceGraph.clear();
                    _pnlInfo.clear();
                    return;
                }
                row = _tblPortfolio.convertRowIndexToModel(row);
                String sym = (String)_TableModel.getCell(row, AccountViewTableModel.COLUMN_SYMBOL).getValue();
                Props.SymbolSelection.setValue(sym);
                StopLevelInfo sli = RiskMgrModel.getInstance().getStopLevelInfo(sym);
                if (sli == null || sli.getStopLevels() == null)
                    return;

                plotPriceData(sli);//refresh graph
                _pnlInfo.populate(sli, sli.getSwingPoints(), _TableModel.getQuoteLookback());
                _btnUpdateQuote.setEnabled(true);
                if (_dlgAddShare != null)
                    _dlgAddShare.populate(sym);
            }
        });
        JScrollPane scr = new JScrollPane(_tblPortfolio);  scr.getViewport().setOpaque(false);
        cen_pnl.setTopComponent(scr);

        //south - price graph with stop levels in a split pane
        JSplitPane bottom_pnl = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);  bottom_pnl.setOpaque(false);
        bottom_pnl.setDividerLocation(800);
        bottom_pnl.setContinuousLayout(true);
        bottom_pnl.setDividerSize(Constants.DEFAULT_SPLITTER_WIDTH);
        bottom_pnl.setLeftComponent(_pnlPriceGraph = new PriceLevelPanel());
        bottom_pnl.setRightComponent(_pnlInfo = new InfoPanel());

        //east of south - table with stop levels
        cen_pnl.setBottomComponent(bottom_pnl);
        add(cen_pnl, "dock center");
        add(_pnlSummary = new SummaryStrip(), "dock south");
        Props.addWeakPropertyChangeListener(Props.StopChanged, this);//handle symbol change
        Props.addWeakPropertyChangeListener(Props.CashChanged, this);//handle cash change from summary strip
    }

    //----- interface, overrides -----
    public void propertyChange(PropertyChangeEvent evt) {
        Props prop = (Props) evt.getSource();
        switch (prop) {
            case StopChanged://update risk fields
                int row = (Integer)prop.getValue();
                StopLevelInfo sli = _TableModel.getStopInfo(row);

                //update positions array in RiskMgrModel
                Position pos = RiskMgrModel.getInstance().findPosition(sli.getSymbol());
                pos.setStop(sli.getStop());//for now, SummaryStrip requires new stops from table
                updateRiskFields();
                _pnlSummary.populate();
                _btnSave.setEnabled(true);
                plotPriceData(sli);
                break;
        }
    }

    //----- public methods -----
    public void populate() {
        enableButtons();
        _TableModel.populate();
        _tblPortfolio.getSelectionModel().setSelectionInterval(0, 0);//should always have some rows
        updateRiskFields();
        updateSummary();
//        _pnlSummary.populate();
        _lblPosCount.setText(String.valueOf(RiskMgrModel.getInstance().getPositions().size()));
        if (_dlgAddShare != null)
            _dlgAddShare.populate((String)_TableModel.getCell(0, AccountViewTableModel.COLUMN_SYMBOL).getValue());
    }
    public void updateSummary() { //due to cash information
        _lblCashPct.setText(FrameworkConstants.PCT_FORMAT.format(RiskMgrModel.getInstance().getCashBalance() /
            RiskMgrModel.getInstance().calcTotalValue()));
        _pnlSummary.populate();
    }
    void updateRiskFields() {
        float cost_risk = _TableModel.getTotalCostRisk();
        double cost = _TableModel.getTotalCost();
        _txtCostRisk.setText(FrameworkConstants.DOLLAR_FORMAT.format(cost_risk));
        _txtCostRiskPercent.setText(FrameworkConstants.ROI_FORMAT.format(cost_risk / cost));
        emphasizeField(_txtCostRisk, cost_risk > 0);
        emphasizeField(_txtCostRiskPercent, cost_risk > 0);
    }
    public void plotPriceData(StopLevelInfo sli) {
        ArrayList<FundQuote> quotes = sli.getQuotes();
        String sym = quotes.get(0).getSymbol();
        TimeSeries price_series = new TimeSeries(sym);

        //reduce number of days drawn to 60 days
        int num_points = 60;
        if (quotes.size() < 60) num_points = quotes.size();
        for (int index = num_points - 1; index >= 0; index--) {//last first (Yahoo data)
            try {
                Calendar cal = AppUtil.stringToCalendar(quotes.get(index).getDate());
                Day day = new Day(cal.getTime());
                float close = quotes.get(index).getClose();
                price_series.add(day, close);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        TimeSeries[] ps = new TimeSeries[1]; ps[0] = price_series;
        _pnlPriceGraph.addSeries(ps);
        _pnlPriceGraph.drawLevels(sli);
    }
    public void showSectorIndustry(boolean show_sector) {
        _TableModel.showSectorIndustry(show_sector);
        if (_TableModel.getRowCount() > 0)
            _tblPortfolio.getSelectionModel().setSelectionInterval(0, 0);
    }
    public boolean isDirty() { return _TableModel.isDirty(); }
    public void enableButtons() {
        _btnWatchlist.setEnabled(true);
        _btnThumbnail.setEnabled(true);
    }

    //-----private methods-----
    private void autoSort() {//only the mean column by default
        ArrayList<RowSorter.SortKey> keys = new ArrayList<>();
        keys.add(new RowSorter.SortKey(AccountViewTableModel.COLUMN_SYMBOL, SortOrder.ASCENDING));
        _Sorter.setSortKeys(keys);
    }
    private void emphasizeField(JComponent field, boolean emphasized) {
        field.setBackground(emphasized ? FrameworkConstants.LIGHT_GREEN : FrameworkConstants.LIGHT_PINK);
    }
    private void updateQuotes() {
        //show progress bar
        final ProgressBar pb = ProgressBar.getInstance(org.marketsuite.riskmanager.RiskMgrModel.getInstance().getParent(), "");
        pb.setVisible(true);

        //start a thread to simulate and export all files
        Thread thread = new Thread() {
            public void run() {
                final ArrayList<LogMessage> failed_msgs = new ArrayList<>();//might fail, keep a list of errors
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        pb.setLabel(ApolloConstants.APOLLO_BUNDLE.getString("active_msg_5"));
                    }
                });

                for (int row = 0; row < _TableModel.getRowCount(); row++) {
//                    if (getCell(row, COLUMN_SEQUENCE).getValue().equals(""))
//                        continue;//skip blank row
                    final String sym = (String)_TableModel.getCell(row, AccountViewTableModel.COLUMN_SYMBOL).getValue();
                    Calendar cal = Calendar.getInstance();

                    //loop up to 7 days till yahoo has good response, to avoid weekend, holiday no quotes
                    FundQuote quote;
                    int limit = 7;
                    do {
                        int cur_month = cal.get(Calendar.MONTH);
                        int cur_day = cal.get(Calendar.DAY_OF_MONTH);
                        int cur_year = cal.get(Calendar.YEAR);
                        final String dt = cur_month + "/" + cur_day + "/" + cur_year;
//                        System.out.print("\n....Get quote for " + dt + " =====> ");

                        //request current day quote
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                pb.setLabel(ApolloConstants.APOLLO_BUNDLE.getString("active_msg_5") + " " + sym + " on " + dt);
                            }
                        });
                        try {
                            quote = DataUtil.getYahooQuote(sym);
                            if (quote == null) {
                                cal.add(Calendar.DAY_OF_YEAR, -1);//go back one day
                                limit--;
                                LogMessage lm = new LogMessage(LoggingSource.RISKMGR_ACCOUNT, ApolloConstants.APOLLO_BUNDLE.getString("rm_113") + " " + sym + " on " + dt, null);
                                failed_msgs.add(lm);
                            }
                            else {//got quote
                                Position pos = RiskMgrModel.getInstance().findPosition(sym);
                                pos.getStopLevelInfo().getQuotes().get(0).setClose(quote.getClose());
                                break;
                            }
                        } catch (final IOException e) {
                            EventQueue.invokeLater(new Runnable() {
                                public void run() {
                                    LogMessage lm = new LogMessage(LoggingSource.RISKMGR_ACCOUNT, sym + ApolloConstants.APOLLO_BUNDLE.getString("rm_111"), e);
                                    failed_msgs.add(lm);
//                                    MessageBox.messageBox(com.clec.riskmanager.RiskMgrModel.getInstance().getParent(),
//                                            FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
//                                            ApolloConstants.APOLLO_BUNDLE.getString("active_msg_3") + ": " + sym,
//                                            MessageBox.OK_OPTION, MessageBox.WARNING_MESSAGE);
                                    pb.setVisible(false);
                                }
                            });
                            e.printStackTrace();
                        }
                    }while (limit > 0);

                    //if 7 days back, still no quote, warn user
                    if (limit <= 0) {
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                LogMessage lm = new LogMessage(LoggingSource.RISKMGR_ACCOUNT, sym + ApolloConstants.APOLLO_BUNDLE.getString("rm_112"), null);
                                failed_msgs.add(lm);
//                                MessageBox.messageBox(com.clec.riskmanager.RiskMgrModel.getInstance().getParent(),
//                                        FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
//                                        ApolloConstants.APOLLO_BUNDLE.getString("active_msg_2") + ": " + sym,
//                                        MessageBox.OK_OPTION, MessageBox.WARNING_MESSAGE);
                                pb.setVisible(false);
                            }
                        });
                    }
                }

                //update table and status bar
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        populate();
                        pb.setVisible(false);
                    }
                });
                if (failed_msgs.size() > 0)
                    Props.Log.setValue(null, failed_msgs);
            }
        };
        thread.start();
    }

    //----- inner classes ------
    private class AccountCellRenderer extends DynaTableCellRenderer {
        private AccountCellRenderer() { super(_TableModel); }
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            JLabel ret = (JLabel)comp;
            ret.setToolTipText("");
            int model_col = _tblPortfolio.convertColumnIndexToModel(column);
            switch(model_col) {
                case AccountViewTableModel.COLUMN_COST:
                case AccountViewTableModel.COLUMN_STOP_PRICE:
                case AccountViewTableModel.COLUMN_CURRENT_PRICE:
                case AccountViewTableModel.COLUMN_MARKET_VALUE:
                    ret.setText(FrameworkConstants.DOLLAR_FORMAT.format(value));
//                    if (model_col == AccountViewTableModel.COLUMN_SWP) {
//                        //if adding new symbol, skip this
//                        if (sli == null || sli.getStopLevels() == null)
//                            return ret;
//
//                        //list all swing points as tooltip
//                        if (!sym.equals("")) {
//                            ArrayList<FundQuote> swps = _TableModel.getSwingPoints(sym);
//                            if (swps == null)
//                                return ret;
//
//                            StringBuilder buf = new StringBuilder("<html><b>Swing Points:</b><br><br>");
//                            for (FundQuote swp : swps) {
//                                buf.append("&nbsp;&nbsp;")
//                                   .append(FrameworkConstants.DOLLAR_FORMAT.format(swp.getLow()))
//                                   .append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;")
//                                   .append(swp.getDate())
//                                   .append("<br>");
//                            }
//                            buf.append("<br>");
//                            ret.setToolTipText(buf.toString());
//                        }
//                    }
                    break;

//                case AccountViewTableModel.COLUMN_STOP_METHOD:
//                    //if adding new symbol, skip this rendering
//                    if (sli == null || sli.getStopLevels() == null)
//                        return ret;
//
//                    ArrayList<StopLevel> stops = sli.gatherNominalLevels();
//                    //show all stop values in a tooltip sorted from low to high
//                    StringBuilder buf = new StringBuilder("<html>Stop Levels:<br><br>");
//                    for (StopLevel sl : stops) {
//                        boolean today_close = sl.getId().equals(StopLevelInfo.LIST_STOP_METHOD[StopLevelInfo.STOP_CUSTOM_INDEX]);//steal this to represent today close
//                        boolean break_even_id = sl.getId().equals(StopLevelInfo.LIST_STOP_METHOD[StopLevelInfo.BREAK_EVEN_INDEX]);
//                        boolean cur_stop_id = sl.getId().equals(StopLevelInfo.LIST_STOP_METHOD[StopLevelInfo.CURRENT_STOP_INDEX]);
//                        buf.append("&nbsp;&nbsp;");
//                        String lvl = FrameworkConstants.DOLLAR_FORMAT.format(sl.getLevel());
//                        if (today_close)
//                            buf.append("<u>").append(lvl).append("</u>");
//                        else if (break_even_id)
//                            buf.append("<u>").append(FrameworkConstants.DOLLAR_FORMAT.format(sli.calcBreakEvenPrice())).append("</u>");
//                        else
//                            buf.append(lvl);
//                        buf.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
//                        if (today_close)
//                            buf.append("<u>CLOSE</u>");
//                        else if (break_even_id)
//                            buf.append("<u>").append(sl.getId()).append("</u>");
//                        else if (cur_stop_id)
//                            buf.append("<u>").append(sl.getId()).append("</u>");
//                        else
//                            buf.append(sl.getId());
//                        buf.append("<br>");
//                    }
//                    buf.append("<br>");
//                    ret.setToolTipText(buf.toString());
//                    return ret;
//
//                case AccountViewTableModel.COLUMN_ATR:
//                    buf = new StringBuilder("<html><b>ATR Multiple Fail Rate:</b><br><br>");
//                    for (float mul = 1.0f; mul <= 4.0f; mul += 0.25f) {
//                        float pct = sli.getATRMultipleFailRate(mul) / _TableModel.getQuoteLookback();//
//                        buf.append("&nbsp;&nbsp;")
//                           .append(FrameworkConstants.PRICE_FORMAT.format(mul))
//                           .append(" x")
//                           .append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;")
//                           .append(FrameworkConstants.ROI_FORMAT.format(pct))
//                           .append("<br>");
//                    }
//                    buf.append("<br>");
//                    ret.setToolTipText(buf.toString());
//                    break;
//
//                case AccountViewTableModel.COLUMN_TARGET_PRICE:
//                    if ((Double)value > 0)
//                        ret.setText(FrameworkConstants.DOLLAR_FORMAT.format(value));
//                    else
//                        ret.setText("");
//                    return ret;

                //show pink or green background for risk cells
//                case AccountViewTableModel.COLUMN_REAL_PROFIT_LOSS:
//                    Double double_value = (Double) value;
//                    ret.setText(FrameworkConstants.DOLLAR_FORMAT.format(value));
////                    ret.setBackground(FrameworkConstants.LIGHT_PINK);//light pink
//                    if (double_value < 0)
////                        ret.setBackground(FrameworkConstants.LIGHT_GREEN);//light green
////                    else
//                        ret.setText("(" + FrameworkConstants.DOLLAR_FORMAT.format(-double_value) + ")");
//                    return ret;

                //show bold red letter for P/L percent
                case AccountViewTableModel.COLUMN_PROFIT_LOSS_PERCENT:
                case AccountViewTableModel.COLUMN_COST_RISK_PERCENT:
                case AccountViewTableModel.COLUMN_REAL_PROFIT_LOSSS_PERCENT:
                    Double double_value = (Double) value;
                    ret.setText(FrameworkConstants.ROI_FORMAT.format(value));
                    if (double_value >= 0)
                        ret.setForeground(new Color(10, 79, 45));
                    else {
                        ret.setForeground(new Color(210, 12, 217));
                        ret.setFont(Constants.FONT_BOLD);
                    }
                    return ret;

                //show bold red letter for P/L amount
                case AccountViewTableModel.COLUMN_COST_RISK:
                case AccountViewTableModel.COLUMN_REAL_PROFIT_LOSS:
                case AccountViewTableModel.COLUMN_PROFIT_LOSS_AMOUNT:
                    double_value = (Double) value;
                    ret.setText(FrameworkConstants.DOLLAR_FORMAT.format(value));
                    ret.setForeground(new Color(10, 79, 45));
                    if (double_value < 0) {
                        ret.setForeground(new Color(217, 19, 202));
                        ret.setText("(" + FrameworkConstants.DOLLAR_FORMAT.format(-double_value) + ")");
                        ret.setFont(Constants.FONT_BOLD);
                    }
                    return ret;
            }
            return comp;
        }
    }

    //-----instance variables------
    private JTable _tblPortfolio;
    private JButton _btnCalculator = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("rm_97"), FrameworkIcon.CALCULATOR);
    private JButton _btnSave = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("rm_57"), FrameworkIcon.FILE_SAVE);
    private JButton _btnWatchlist = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("rm_88"), FrameworkIcon.WATCH);
    private JButton _btnUndo = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("rm_59"), FrameworkIcon.UNDO);
    private JButton _btnThumbnail = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("scr_15"), FrameworkIcon.THUMBNAIL);
    private JButton _btnUpdateQuote = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("rm_58"), FrameworkIcon.REFRESH);
    private JButton _btnExport = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("wlg_lbl_19"), FrameworkIcon.EXPORT);
    private AccountViewTableModel _TableModel;
    private PriceLevelPanel _pnlPriceGraph;
    private InfoPanel _pnlInfo;
    private SummaryStrip _pnlSummary;
    private JTextField _txtCostRisk;
    private JTextField _txtCostRiskPercent;
    private JTextField _lblCashPct;
    private RowSorter _Sorter;
    private JLabel _lblPosCount = new JLabel();
    private AddSharesDialog _dlgAddShare;
}

//TODO----------------
//Matrix view does not paint properly, calc its volatility too or hide it
//same symbol across different accounts, how to apply stop separately?
//load previously stored stop method
//add stop method: volatility calculated from BB width
//    stop method: chandelier stop
//Refactor: Position vs StopLevelInfo contain too many similar fields
//redesign graph lines, better placement of lines, labels,tooltip, clearer levels
// undo button
// refresh with latest quotes intra-day - coded need debug
//plot equity curve automatically
//figure out reward / risk
//memorize splitter positions
//non risk-manager - remove dependency of c:\bin or c:\database
//TODO----------------


/**
 * Save useful statistics to a history file for future analysis.
 */
//    private void saveStats() {
//        //file name looks like stat_xx466.csv
//        String path = FrameworkConstants.DATA_FOLDER_ACCOUNT + File.separator + name;
//        ArrayList<String> buf = new ArrayList<>();
//
//        //if file doesn't exist, create first
//        File f = new File(path);
//        if (!f.exists()) {
//            try {
//                new PrintWriter(new FileWriter(path));
//            } catch (IOException e) {//not able to create
//                e.printStackTrace();
//                WidgetUtil.showWarning(RiskMgrModel.getInstance().getParent(),
//                    ApolloConstants.APOLLO_BUNDLE.getString("act_msg_17") + path);
//                return;
//            }
//        }
//
//        //file exist, maybe empty, has today, no today
//        try {
//            //read back entire file into a buffer
//            BufferedReader br = new BufferedReader(new FileReader(path));
//            String line;
//            String today = FrameworkConstants.YAHOO_DATE_FORMAT.format(Calendar.getInstance().getTime());
//            while ( (line = br.readLine()) != null ) {//read till end
//                String[] tokens = line.split(",");
//                if (tokens[0].equals("#"))//skip comment
//                    continue;
//
//                if (tokens[0].equals(today)) //found today, don't copy, stop
//                    break;
//                buf.add(line);
//            }
//            buf.add(genStat());
//
//            //write out entire buffer
//            PrintWriter pw = new PrintWriter(new FileWriter(path));
//            pw.println("#, Date, Risk, Adjusted Risk, Cost, Equity, Pull Back, Cash, Position ");
//            for (String buf_line : buf)
//                pw.println(buf_line);
//            pw.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//            WidgetUtil.showWarning(RiskMgrModel.getInstance().getParent(),
//                ApolloConstants.APOLLO_BUNDLE.getString("active_msg_18") + path);
//        }
//    }
