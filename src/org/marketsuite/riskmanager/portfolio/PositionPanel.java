package org.marketsuite.riskmanager.portfolio;

import org.marketsuite.component.Constants;
import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.component.dialog.SchemaColumnDialog;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.DisabledIcon;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.table.DynaTableCellRenderer;
import org.marketsuite.component.table.HeadingRenderer;
import org.marketsuite.component.table.TableUtil;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.main.RiskMgrFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloPreferenceStore;
import org.marketsuite.riskmanager.RiskMgrModel;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.marketsuite.component.Constants;
import org.marketsuite.component.dialog.SchemaColumnDialog;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.DisabledIcon;
import org.marketsuite.component.table.DynaTableCellRenderer;
import org.marketsuite.component.table.HeadingRenderer;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.riskmanager.RiskMgrModel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Container for holding active trades and other contents.
 */
public class PositionPanel extends SkinPanel {
    public PositionPanel() {
        super(LazyIcon.BACKGROUND_CONTENT, new BorderLayout());
        setOpaque(false);

        //title strip - buttons and labels
        SkinPanel ttl_pnl = WidgetUtil.createTitleStrip(createWestToolPanel(), createCenterPanel(), createEastToolPanel());
        add(ttl_pnl, BorderLayout.NORTH);

        //center - table, graph in a vertical split pane
        JSplitPane cen_pnl = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        cen_pnl.setDividerLocation(200);
        cen_pnl.setContinuousLayout(true);
        cen_pnl.setDividerSize(Constants.DEFAULT_SPLITTER_WIDTH);

        //top side - position table
        _TableModel = new PositionTableModel();
        _tblPortfolio = WidgetUtil.createDynaTable(_TableModel, ListSelectionModel.SINGLE_SELECTION,
            new HeadingRenderer(), true, new TradeCellRenderer());
        _tblPortfolio.getTableHeader().setReorderingAllowed(false);
        _tblPortfolio.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        WidgetUtil.forceColumnWidth(_tblPortfolio.getColumnModel().getColumn(PositionTableModel.COLUMN_SEQUENCE), 25);
        _tblPortfolio.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent mse) {
                if (mse.getButton() == MouseEvent.BUTTON1 && mse.getClickCount() > 1) {//double click left button
                    JTable jt = (JTable) mse.getSource();//translate view column number to model column number
                    int col = jt.columnAtPoint(mse.getPoint());
                    int row = jt.rowAtPoint(mse.getPoint());//find out which cell
                    col = jt.convertColumnIndexToModel(col);
                    if (col == PositionTableModel.COLUMN_ENTRY_DATE && !_TableModel.isBlankRow(row)) {//open dialog to enter start date time
                        DateEntryDialog dlg = new DateEntryDialog((String) _TableModel.getCell(row, col).getValue());
                        if (dlg.isCancelled())
                            return;
                        Calendar start_time = dlg.getDate();
                        _TableModel.setValueAt(start_time, row, col);
                    }
                }
            }
        });
        //enable / disable buttons based on row selection
        _tblPortfolio.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting())
                    return;

                //nothing selected, disable delete, close button
                int row = _tblPortfolio.getSelectedRow();
                if (row == -1) {
                    _pnlPriceGraph.clear();
                    _pnlInfo.clear();
                    return;
                }

                //skip empty row
                String sym = (String)_TableModel.getCell(row, PositionTableModel.COLUMN_SYMBOL).getValue();
                if (sym.equals(""))
                    return;

                boolean sel = _tblPortfolio.getSelectedRowCount() > 0;
                _btnDeleteTrade.setEnabled(sel);
                _btnShowLevel.setEnabled(sel);
                StopLevelInfo sli = _TableModel.getStopLevelInfo(sym);//skip if no levels
                if (sli == null || sli.getStopLevels() == null)
                    return;

                plotPriceData(sli);//refresh graph
                _pnlInfo.populate(sli, _TableModel.getSwingPoints(sym), _TableModel.getQuoteLookback());
            }
        });
        JScrollPane scr = new JScrollPane(_tblPortfolio);  scr.getViewport().setOpaque(false);
//TODO column ordering, fixed column doesn't work
//        TableUtil.fixColumns(scr, _tblPortfolio, LOCKED_COLUMNS, null);
//        int[] order = Prefs.getPrefs().getAccountColumnOrder();
//        TableUtil.setColumnOrder(_tblPortfolio, order);
//        boolean[] arr = Prefs.getPrefs().getAccountColumnVisible();
        boolean[] arr = ApolloPreferenceStore.getPreferences().getAccountColumnVisible();
        if (null != arr)
            TableUtil.setColumnsVisible(_tblPortfolio, arr);
        cen_pnl.setTopComponent(scr);

        //south - price graph with stop levels in a split pane
        JSplitPane bottom_pnl = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);  bottom_pnl.setOpaque(false);
        bottom_pnl.setDividerLocation(950);
        bottom_pnl.setContinuousLayout(true);
        bottom_pnl.setDividerSize(Constants.DEFAULT_SPLITTER_WIDTH);
        bottom_pnl.setLeftComponent(_pnlPriceGraph = new PriceLevelPanel());
        bottom_pnl.setRightComponent(_pnlInfo = new InfoPanel());

        //east of south - table with stop levels
        cen_pnl.setBottomComponent(bottom_pnl);
        add(cen_pnl, BorderLayout.CENTER);
        add(_pnlSummary = new SummaryStrip(), BorderLayout.SOUTH);
    }

    //----- public methods -----
    public void populate(File selected_file) throws IOException {
        _AccountFile = selected_file;
        _TableModel.populate(selected_file.getPath());
        String acct_name = selected_file.getName();
        String name = acct_name.substring(0, acct_name.indexOf(FrameworkConstants.EXTENSION_ACCOUNT));
        _lblName.setText(name);
    }

    public void updateSummary(double total_risk, double mkt_val, double risk_pct, double pl_amt, double pl_pct,
                              double adj_risk, double adj_pct, double total_cost, double cash_pct,
                              double mp_amt, double mp_pct) {
        _pnlSummary.updateSummary(total_risk, mkt_val, risk_pct, pl_amt, pl_pct, adj_risk, adj_pct, total_cost,
            cash_pct, mp_amt, mp_pct);
        _pnlSummary.updateMarketValue(mkt_val);
    }
    public double getEquity() { return _TableModel.getEquity(); }
    public double getTotalCost() { return _TableModel.getTotalCost(); }
    public double getProfitLossAmount() { return _TableModel.getProfitLossAmount(); }
    public double getProfitLossPercent() { return _TableModel.getProfitLossPercent(); }
    public double getTotalRisk() { return _TableModel.getTotalRisk(); }
    public double getRiskPercent() { return _TableModel.getRiskPercent(); }
    public double getAdjustedRisk() { return _TableModel.getAdjustedRisk(); }
    public double getAdjustedPercent() { return _TableModel.getAdjPercent(); }
    public double getCashPercent() { return _TableModel.getCashPercent(); }
    public double getCashAmount() { return _TableModel.getCashAmount(); }
    public void setCashAmount(double cash) { _TableModel.setCashAmount(cash); }
    public double getMaxPullbackAmount() { return _TableModel.getMaxPullbackAmount(); }
    public double getMaxPullbackPercent() { return _TableModel.getMaxPullbackPercent(); }
    public void setRisk(String text, boolean plus) {
        _txtRisk.setText(text);
        if (plus)
            _txtRisk.setBackground(FrameworkConstants.LIGHT_GREEN);
        else
            _txtRisk.setBackground(FrameworkConstants.LIGHT_PINK);
    }

    public void plotPriceData(StopLevelInfo sli) {
        ArrayList<FundQuote> quotes = sli.getQuotes();
        String sym = quotes.get(0).getSymbol();

        //first series: price, 2nd series: stop values (start with 1 ATR initially)
        TimeSeries price_series = new TimeSeries(sym);
//        TimeSeries stop_series = new TimeSeries(ApolloConstants.APOLLO_BUNDLE.getString("active_lbl_18"));
        float[] atr = sli.calcATR();
        float[] stops = new float[atr.length];
        for (int index = quotes.size() - 1; index >= 0; index--) {//last first (Yahoo data)
            try {
                Calendar cal = AppUtil.stringToCalendar(quotes.get(index).getDate());
                Day day = new Day(cal.getTime());
                float close = quotes.get(index).getClose();
                price_series.add(day, close);

//TODO: variable multiple draw trailing stop line
                stops[index] = close - 2 * atr[index];//2 ATR to start
                float prev_stop = (index == quotes.size()-1) ? 0 : stops[index + 1];
                if (stops[index] < prev_stop)
                    stops[index] = prev_stop;//can't move stop lower
//                stop_series.add(day, stops[index]);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        TimeSeries[] ps = new TimeSeries[1]; ps[0] = price_series;// ps[1] = stop_series;
        _pnlPriceGraph.addSeries(ps);
        _pnlPriceGraph.drawLevels(sli.gatherNominalLevels()/*_StopLevelInfo.getStopLevels()*/);
    }

    //pass thru
    public void markDirty(boolean dirty) {
        _TableModel.setDirty(dirty);
        _btnSave.setEnabled(dirty);
    }

    //-----private methods-----
    //create west side panel of title strip
    private JPanel createWestToolPanel() {
        //west - file management buttons, open, save
        JPanel ret = new JPanel(new FlowLayout(FlowLayout.LEFT)); ret.setOpaque(false);
        ret.add(Box.createGlue());
//        ret.add(_btnOpenTrades);//always enabled
//        _btnOpenTrades.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent actionEvent) {
//                checkDirtySave();
//
//                //open trade file, read all trades
//                JFileChooser fc = new JFileChooser(new File(FrameworkConstants.DATA_FOLDER_TRADE_LOG));
//                fc.setFileFilter(new FileFilter() {
//                    public boolean accept(File file) {
//                        if (file.isDirectory())
//                            return true;
//                        //only allow .trd extension
//                        int ext_pos = file.getEntry().lastIndexOf(FrameworkConstants.EXTENSION_TRADE_LOGS);
//                        if (ext_pos > 0)
//                            return true;
//                        return false;
//                    }
//
//                    public String getDescription() {//this shows up in description field of dialog
//                        return ApolloConstants.APOLLO_BUNDLE.getString("active_lbl_1");
//                    }
//                });
//                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
//                fc.setAcceptAllFileFilterUsed(false);
//                int ret = fc.showOpenDialog(PositionPanel.this);
//                if (ret == JFileChooser.APPROVE_OPTION) {
//                    File selected_file = fc.getSelectedFile();
//                    String file_name = selected_file.getEntry();
//                    if (!file_name.endsWith(FrameworkConstants.EXTENSION_TRADE_LOGS)) {
//                        WidgetUtil.showWarning(MainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("active_msg_7") + ": " + file_name);
//                        return;
//                    }
//
//                    //don't calculate stop till populate is complete
//                    try {
//                        _TableModel.populate(selected_file.getPath());
//                        _lblName.setText(file_name);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        WidgetUtil.showWarning(MainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("active_msg_4") + ": " + file_name +
//                                ApolloConstants.APOLLO_BUNDLE.getString("active_msg_11") + e.getMessage());
//                    }
//                }
//            }
//        });
//        ret.add(Box.createGlue());

        //save trades to file, append to end of stat file
        ret.add(_btnSave);
        _btnSave.setEnabled(false);//initially disabled till dirty
        _btnSave.setDisabledIcon(new DisabledIcon(FrameworkIcon.FILE_SAVE.getImage()));
        _btnSave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (!_TableModel.isDirty())
                    return;

                saveAccount();
//                try {
//                    _TableModel.saveAccount(_AccountFile);
//                    saveStats();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                _TableModel.setDirty(false);
//                _btnSave.setEnabled(false);
                }
            });
        ret.add(Box.createGlue());

        //save as another file
        ret.add(_btnSaveAs);
        _btnSaveAs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //ask user for file name, must ends with correct extension
                JFileChooser fc = new JFileChooser(new File(FrameworkConstants.DATA_FOLDER_ACCOUNT));
                fc.setFileFilter(new FileFilter() {
                    public boolean accept(File file) {
                        if (file.isDirectory())
                            return true;
                        //only allow .trd extension
                        int ext_pos = file.getName().lastIndexOf(FrameworkConstants.EXTENSION_TRADE_LOGS);
                        if (ext_pos > 0)
                            return true;
                        return false;
                    }

                    public String getDescription() {//this shows up in description field of dialog
                        return ApolloConstants.APOLLO_BUNDLE.getString("act_lbl_1");
                    }
                });
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.setAcceptAllFileFilterUsed(false);
                int ret = fc.showSaveDialog(PositionPanel.this);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    File selected_file = fc.getSelectedFile();
                    String file_name = selected_file.getName();

                    //must have valid extension
                    if (!file_name.endsWith(FrameworkConstants.EXTENSION_ACCOUNT)) {
                        MessageBox.messageBox(RiskMgrModel.getInstance().getParent(),
                                FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                                ApolloConstants.APOLLO_BUNDLE.getString("act_msg_8") + ":   " + file_name,
                                MessageBox.OK_OPTION, MessageBox.WARNING_MESSAGE);
                        return;
                    }

                    //warning about overwrite
                    if (selected_file.exists()) {
                        boolean rsp = WidgetUtil.confirmOkCancel(RiskMgrModel.getInstance().getParent(),
                                ApolloConstants.APOLLO_BUNDLE.getString("act_msg_9"));
                        if (!rsp)
                            return;
                    }
                    try {
                        _TableModel.saveAccount(selected_file);
                        _lblName.setText(selected_file.getName());
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    _TableModel.setDirty(false);//not dirty any more with new file
                }
            }
        });
        ret.add(Box.createGlue());

        //settings
        ret.add(_btnSettings);
        _btnSettings.setDisabledIcon(new DisabledIcon(FrameworkIcon.SETTING.getImage()));
        _btnSettings.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SettingsDialog dlg = new SettingsDialog(
                        _TableModel.getAtrLength(),
                        _TableModel.getQuoteLookback(),
                        _TableModel.getQtrAdjFactor());
                if (dlg.isCancelled())
                    return;

                //save settings to model
                _TableModel.setAtrLength(dlg.getAtrLen());
                _TableModel.setQuoteLookback(dlg.getLookback());
                _TableModel.setQtrAdjFactor(dlg.getAdjFactor());
            }
        });
        ret.add(Box.createGlue());

//TODO temporary disable this function since yahoo is not reliable early morning.....
        //refresh quote
//        ret.add(_btnUpdateQuote);
        _btnUpdateQuote.setDisabledIcon(new DisabledIcon(FrameworkIcon.REFRESH.getImage()));
        _btnUpdateQuote.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                _TableModel.updateQuotes();
                _TableModel.setDirty(true);
                _btnSave.setEnabled(true);
            }
        });
        _btnUpdateQuote.setEnabled(false);

        //name of portfolio
        ret.add(Box.createGlue());
        ret.add(Box.createHorizontalStrut(10));
        ret.add(_lblName);
        _lblName.setFont(FrameworkConstants.SMALL_FONT_BOLD);  _lblName.setForeground(Color.blue);
        return ret;
    }

    //center of title - total risk percent
    private JPanel createCenterPanel() {
        JPanel ret = new JPanel(); ret.setOpaque(false);
        ret.add(_txtRisk);
        _txtRisk.setFont(Constants.FONT_NORMAL_STATE);
        _txtRisk.setBackground(FrameworkConstants.LIGHT_PINK);
        _txtRisk.setEditable(false);
        _txtRisk.setText("0.00%");
        return ret;
    }

    //create east side panel of title strip
    private JPanel createEastToolPanel() {
        JPanel ret = new JPanel(new FlowLayout(FlowLayout.RIGHT)); ret.setOpaque(false);

        //add new row of trade before selected row
        ret.add(_btnInsertTrade);//always enabled
        _btnInsertTrade.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                int sel = _tblPortfolio.getSelectedRow();
                if (sel == -1) //no selection, can't add
                    sel = _tblPortfolio.getRowCount();//last

                _TableModel.insertTradeRow(sel);
                _btnSave.setEnabled(true);//allow saving
                _TableModel.setDirty(true);
                _btnSave.setEnabled(true);
            }
        });
        ret.add(Box.createGlue());

        //delete selected row
        ret.add(_btnDeleteTrade);
        _btnDeleteTrade.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                int sel = _tblPortfolio.getSelectedRow();
                _TableModel.deleteRow(sel);
                _TableModel.setDirty(true);
                _btnSave.setEnabled(true);
            }
        });
        _btnDeleteTrade.setDisabledIcon(new DisabledIcon(LazyIcon.MINUS_SIGN.getImage()));
        _btnDeleteTrade.setEnabled(false);
        ret.add(Box.createGlue());

        //insert blank row
        ret.add(_btnInsertBlankRow);
        _btnInsertBlankRow.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                int sel = _tblPortfolio.getSelectedRow();
                if (sel == -1)
                    return;

                //ignore when selecting blank row, no two blank rows can be placed together
                boolean blank_row = _TableModel.getCell(sel, PositionTableModel.COLUMN_SEQUENCE).getValue().equals("");
                if (blank_row)
                    return;

                //if current row is right after blank row, dis-allow, no two blank rows can be together
                if (sel > 0) {
                    blank_row = _TableModel.getCell(sel - 1, PositionTableModel.COLUMN_SEQUENCE).getValue().equals("");
                    if (blank_row)
                        return;
                }
                _TableModel.insertBlankRow(sel);
                _TableModel.setDirty(true);
                _btnSave.setEnabled(true);
            }
        });
        ret.add(Box.createGlue());

        //clearGraph table button
        ret.add(_btnClearAll);
        _btnClearAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                checkDirtySave();
                _TableModel.clear();
            }
        });
        ret.add(Box.createGlue());

        //show / hide columns
        ret.add(_btnShowHideColumn);
        _btnShowHideColumn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String[] col_names = new String[PositionTableModel.TABLE_SCHEMA.length];
                for (int col = 0; col < PositionTableModel.TABLE_SCHEMA.length; col++)
                    col_names[col] = (String)PositionTableModel.TABLE_SCHEMA[col][0];
                SchemaColumnDialog dlg = new SchemaColumnDialog(
                        _tblPortfolio, col_names,
                        RiskMgrModel.getInstance().getParent(),
                        LazyIcon.APP_ICON.getImage(), LOCKED_COLUMNS);
//                dlg.setVisibleColumns(Prefs.getPrefs().getAccountColumnVisible());
                dlg.setVisibleColumns(ApolloPreferenceStore.getPreferences().getAccountColumnVisible());
                dlg.setVisible(true);
                boolean[] visible_columns = dlg.getResult();
                if (null != visible_columns) {
//                    Prefs.getPrefs().setAccountColumnVisible(visible_columns);
                    ApolloPreferenceStore.getPreferences().setAccountColumnVisible(visible_columns);
//                    Prefs.savePrefs();
                    ApolloPreferenceStore.savePreferences();
                    TableUtil.setColumnsVisible(_tblPortfolio, visible_columns);
                }
            }
        });
        ret.add(Box.createHorizontalStrut(20));

        //close tab button
        ret.add(_btnCloseTab);
        _btnCloseTab.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkDirtySave();
                RiskMgrFrame rmf = ((MdiMainFrame) RiskMgrModel.getInstance().getParent()).findRiskMgrFrame();
//                if (rmf != null)
//                    rmf.getMainPanel().closeActiveTab();
            }
        });
        ret.add(Box.createGlue());
        return ret;
    }

    //check if document is changed, ask user wants to save or not
    private void checkDirtySave() {
        if (_TableModel.isDirty()) {
            int reply = MessageBox.messageBox(RiskMgrModel.getInstance().getParent(),
                    FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                    ApolloConstants.APOLLO_BUNDLE.getString("act_msg_16"), MessageBox.STYLE_YES_NO, MessageBox.IMAGE_QUESTION);
            if (reply == MessageBox.RESULT_YES) {
                saveAccount();
                return;
            }

            //don't want to save
            _TableModel.setDirty(false);
            _btnSave.setEnabled(false);
        }
    }

    private int getPositionCount() { return _TableModel.getPositionCount(); }

    /**
     * Save useful statistics to a history file for future analysis.
     */
    private void saveStats() {
        //file name looks like stat_xx466.csv
        String account = _lblName.getText();
        String name = STAT_FILE_PREFIX + account + FrameworkConstants.EXTENSION_TRADES;
        String path = FrameworkConstants.DATA_FOLDER_ACCOUNT + File.separator + name;
        ArrayList<String> buf = new ArrayList<String>();

        //if file doesn't exist, create first
        File f = new File(path);
        if (!f.exists()) {
            try {
                new PrintWriter(new FileWriter(path));
            } catch (IOException e) {//not able to create
                e.printStackTrace();
                WidgetUtil.showWarning(RiskMgrModel.getInstance().getParent(),
                    ApolloConstants.APOLLO_BUNDLE.getString("act_msg_17") + path);
                return;
            }
        }

        //file exist, maybe empty, has today, no today
        try {
            //read back entire file into a buffer
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line;
            String today = FrameworkConstants.YAHOO_DATE_FORMAT.format(Calendar.getInstance().getTime());
            while ( (line = br.readLine()) != null ) {//read till end
                String[] tokens = line.split(",");
                if (tokens[0].equals("#"))//skip comment
                    continue;

                if (tokens[0].equals(today)) //found today, don't copy, stop
                    break;
                buf.add(line);
            }
            buf.add(genStat());

            //write out entire buffer
            PrintWriter pw = new PrintWriter(new FileWriter(path));
            pw.println("#, Date, Risk, Adjusted Risk, Cost, Equity, Pull Back, Cash, Position ");
            for (String buf_line : buf)
                pw.println(buf_line);
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
            WidgetUtil.showWarning(RiskMgrModel.getInstance().getParent(),
                ApolloConstants.APOLLO_BUNDLE.getString("active_msg_18") + path);
        }
    }

    private String genStat() {
        //create today's line
        StringBuilder sb = new StringBuilder();
        sb.append(FrameworkConstants.YAHOO_DATE_FORMAT.format(Calendar.getInstance().getTime())).append(",")
            .append(FrameworkConstants.DOLLAR_FORMAT.format(getTotalRisk())).append(",")
            .append(FrameworkConstants.DOLLAR_FORMAT.format(getAdjustedRisk())).append(",")
            .append(FrameworkConstants.DOLLAR_FORMAT.format(getTotalCost())).append(",")
            .append(FrameworkConstants.DOLLAR_FORMAT.format(getEquity())).append(",")
            .append(FrameworkConstants.DOLLAR_FORMAT.format(getMaxPullbackAmount())).append(",")
            .append(FrameworkConstants.DOLLAR_FORMAT.format(getCashAmount())).append(",")
            .append(getPositionCount());
        return sb.toString();
    }

    private void saveAccount() {
        try {
            _TableModel.saveAccount(_AccountFile);
            saveStats();
        } catch (IOException e) {
            e.printStackTrace();
        }
        _TableModel.setDirty(false);
        _btnSave.setEnabled(false);
    }

    private class TradeCellRenderer extends DynaTableCellRenderer {
        private TradeCellRenderer() { super(_TableModel); }
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            JLabel ret = (JLabel)comp;
            ret.setToolTipText("");

            //if sequence cell is blank, render blank for all fields
            Object seq = _TableModel.getCell(row, PositionTableModel.COLUMN_SEQUENCE).getValue();
            if (seq.equals("")) {
                ret.setText("");
                return ret;
            }
            String sym = (String)_TableModel.getCell(row, PositionTableModel.COLUMN_SYMBOL).getValue();
            StopLevelInfo sli = _TableModel.getStopLevelInfo(sym);
            int model_col = _tblPortfolio.convertColumnIndexToModel(column);
            switch(model_col) {
                case PositionTableModel.COLUMN_ENTRY_PRICE:
                case PositionTableModel.COLUMN_SWP:
                case PositionTableModel.COLUMN_STOP_PRICE:
                case PositionTableModel.COLUMN_CURRENT_PRICE:
                case PositionTableModel.COLUMN_CURRENT_EQITY:
                    ret.setText(FrameworkConstants.DOLLAR_FORMAT.format(value));
                    if (model_col == PositionTableModel.COLUMN_SWP) {
                        //if adding new symbol, skip this
                        if (sli == null || sli.getStopLevels() == null)
                            return ret;

                        //list all swing points as tooltip
                        if (!sym.equals("")) {
                            ArrayList<FundQuote> swps = _TableModel.getSwingPoints(sym);
                            if (swps == null)
                                return ret;

                            StringBuilder buf = new StringBuilder("<html><b>Swing Points:</b><br><br>");
                            for (FundQuote swp : swps) {
                                buf.append("&nbsp;&nbsp;")
                                   .append(FrameworkConstants.DOLLAR_FORMAT.format(swp.getLow()))
                                   .append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;")
                                   .append(swp.getDate())
                                   .append("<br>");
                            }
                            buf.append("<br>");
                            ret.setToolTipText(buf.toString());
                        }
                    }
                    break;

                case PositionTableModel.COLUMN_STOP_METHOD:
                    //if adding new symbol, skip this rendering
                    if (sli == null || sli.getStopLevels() == null)
                        return ret;

                    ArrayList<StopLevel> stops = sli.gatherNominalLevels();
                    //show all stop values in a tooltip sorted from low to high
                    StringBuilder buf = new StringBuilder("<html>Stop Levels:<br><br>");
                    for (StopLevel sl : stops) {
                        boolean today_close = sl.getId().equals(StopLevelInfo.LIST_STOP_METHOD[StopLevelInfo.STOP_CUSTOM_INDEX]);//steal this to represent today close
                        boolean break_even_id = sl.getId().equals(StopLevelInfo.LIST_STOP_METHOD[StopLevelInfo.BREAK_EVEN_INDEX]);
                        boolean cur_stop_id = sl.getId().equals(StopLevelInfo.LIST_STOP_METHOD[StopLevelInfo.CURRENT_STOP_INDEX]);
                        buf.append("&nbsp;&nbsp;");
                        String lvl = FrameworkConstants.DOLLAR_FORMAT.format(sl.getLevel());
                        if (today_close)
                            buf.append("<u>").append(lvl).append("</u>");
                        else if (break_even_id)
                            buf.append("<u>").append(FrameworkConstants.DOLLAR_FORMAT.format(sli.calcBreakEvenPrice())).append("</u>");
                        else
                            buf.append(lvl);
                        buf.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
                        if (today_close)
                            buf.append("<u>CLOSE</u>");
                        else if (break_even_id)
                            buf.append("<u>").append(sl.getId()).append("</u>");
                        else if (cur_stop_id)
                            buf.append("<u>").append(sl.getId()).append("</u>");
                        else
                            buf.append(sl.getId());
                        buf.append("<br>");
                    }
                    buf.append("<br>");
                    ret.setToolTipText(buf.toString());
                    return ret;

                case PositionTableModel.COLUMN_ATR:
                    buf = new StringBuilder("<html><b>ATR Multiple Fail Rate:</b><br><br>");
                    for (float mul = 1.0f; mul <= 4.0f; mul += 0.25f) {
                        float pct = sli.getATRMultipleFailRate(mul) / _TableModel.getQuoteLookback();//todo use variable range....
                        buf.append("&nbsp;&nbsp;")
                           .append(FrameworkConstants.PRICE_FORMAT.format(mul))
                           .append(" x")
                           .append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;")
                           .append(FrameworkConstants.ROI_FORMAT.format(pct))
                           .append("<br>");
                    }
                    buf.append("<br>");
                    ret.setToolTipText(buf.toString());
                    break;

                case PositionTableModel.COLUMN_TARGET_PRICE:
                    if ((Double)value > 0)
                        ret.setText(FrameworkConstants.DOLLAR_FORMAT.format(value));
                    else
                        ret.setText("");
                    return ret;

                //show pink or green background for risk cells
                case PositionTableModel.COLUMN_TRADE_RISK:
                    ret.setText(FrameworkConstants.DOLLAR_FORMAT.format(value));
                    ret.setBackground(FrameworkConstants.LIGHT_PINK);//light pink
                    if ((Double)value > 0) {
                        ret.setText(FrameworkConstants.DOLLAR_FORMAT.format(((Double)value)));
                        ret.setBackground(FrameworkConstants.LIGHT_GREEN);//light green
                    }
                    return ret;

                //show bold red letter for P/L percent
                case PositionTableModel.COLUMN_GAIN_LOSS_PERCENT:
                case PositionTableModel.COLUMN_RISK_PERCENT:
                    ret.setText(FrameworkConstants.ROI_FORMAT.format(value));
                    ret.setFont(Constants.FONT_BOLD);
                    if ((Double)value > 0)
                        ret.setForeground(new Color(10, 79, 45));
                    else
                        ret.setForeground(new Color(217, 4, 4));
                    return ret;

                //show bold red letter for P/L amount
                case PositionTableModel.COLUMN_GAIN_LOSS_AMOUNT:
                    ret.setText(FrameworkConstants.DOLLAR_FORMAT.format(value));
                    ret.setFont(Constants.FONT_BOLD);
                    ret.setForeground(new Color(10, 79, 45));
                    if ((Double)value < 0) {
                        ret.setForeground(new Color(217, 4, 4));
                        ret.setText("(" + FrameworkConstants.DOLLAR_FORMAT.format(-(Double)value) + ")");
                    }
                    return ret;
            }
            return comp;
        }
    }

    //-----instance variables------
    private JTable _tblPortfolio;
//    private JButton _btnOpenTrades = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("active_tip_1"), FrameworkIcon.FILE_OPEN);
    private JButton _btnSave = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("act_tip_15"), FrameworkIcon.FILE_SAVE);
    private JButton _btnSaveAs = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("act_tip_16"), FrameworkIcon.FILE_SAVE_AS);
    private JButton _btnUpdateQuote = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("active_tip_5"), FrameworkIcon.REFRESH);
    private JButton _btnSettings = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("active_tip_10"), FrameworkIcon.SETTING);
    private JLabel _lblName = new JLabel();
    private JButton _btnInsertTrade = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("active_tip_3"), LazyIcon.PLUS_SIGN);
    private JButton _btnDeleteTrade = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("active_tip_4"), LazyIcon.MINUS_SIGN);
    private JButton _btnInsertBlankRow = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("active_tip_7"), LazyIcon.PLUS_SIGN_2);
    private JButton _btnClearAll = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("active_tip_8"), FrameworkIcon.CLEAR);
    private JButton _btnShowLevel = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("active_tip_11"), FrameworkIcon.PRICE_CHART);
    private JButton _btnShowHideColumn = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("active_tip_13"), LazyIcon.TABLE_COLUMN_OP);
    private JButton _btnCloseTab = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("act_tip_14"), FrameworkIcon.FILE_CLOSE);
    private PositionTableModel _TableModel;
    private PriceLevelPanel _pnlPriceGraph;
    private InfoPanel _pnlInfo;
    private SummaryStrip _pnlSummary;
    private JTextField _txtRisk = new JTextField(5);
    private File _AccountFile;

    //-----literals-----
    private static final String STAT_FILE_PREFIX = "stat_";
    private static final int LOCKED_COLUMNS = 3;
}