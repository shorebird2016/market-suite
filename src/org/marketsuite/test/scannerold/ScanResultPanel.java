package org.marketsuite.test.scannerold;

import org.marketsuite.component.Constants;
import org.marketsuite.component.dialog.ProgressBar;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.table.*;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.StandardOption;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.strategy.base.SimParam;
import org.marketsuite.framework.strategy.base.SimReport;
import org.marketsuite.framework.strategy.mac.MacEngine;
import org.marketsuite.framework.strategy.mac.MacOption;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.jdesktop.swingx.JXDatePicker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * A container for showing results from scannerold.
 */
public class ScanResultPanel extends SkinPanel {
    public ScanResultPanel(ArrayList<ScanResult> results) {
        super(LazyIcon.BACKGROUND_CONTENT, new BorderLayout());
        setLayout(new BorderLayout());
        setOpaque(false);

        //north - title strip
        JPanel west_pnl = new JPanel(new FlowLayout(FlowLayout.LEFT)); west_pnl.setOpaque(false);
        west_pnl.add(Box.createGlue());
        JLabel lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("scan_lbl_1"));
        lbl.setFont(Constants.FONT_BOLD);
        west_pnl.add(lbl);
        west_pnl.add(_cmbStrategy);
        west_pnl.add(Box.createHorizontalStrut(20));
        west_pnl.add(_txtStartDate);
        //find last monday as default starting day
        Calendar cal = AppUtil.calcPastMonday(Calendar.getInstance());
        Date past_monday = cal.getTime();
        _txtStartDate.setDate(past_monday);
        west_pnl.add(Box.createHorizontalStrut(10));
        west_pnl.add(_btnScan);
        _btnScan.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scanSignal();
            }
        });
        SkinPanel ttl_pnl = WidgetUtil.createTitleStrip(west_pnl, null, null);
        add(ttl_pnl, BorderLayout.NORTH);

        //center - result table
        _ResultTableModel = new TradeTableModel();
        JTable tbl = new JTable(_ResultTableModel) {
            //paint a line after summary row to be more readable
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D)g;
                Dimension size = getSize();
                g2.setColor(Color.gray);
                //look thru table, if symbol column goes from blank to non-blank, draw a line at bottom
                String last_symbol = "-";//non-blank
                for (int row = 0; row < _ResultTableModel.getRowCount(); row++) {
                    Object sym = _ResultTableModel.getCell(row, COLUMN_SYMBOL).getValue();
                    //from blank to non-blank or symbol A to symbol B, or last row
                    if ( (last_symbol.equals("") && !sym.equals(""))
                          || (!last_symbol.equals("") && !sym.equals("") && !last_symbol.equals(sym))
                          ) {
                        int height = (row) * getRowHeight();
                        g2.drawLine(0, height, size.width, height);
                    }
                    if (row == _ResultTableModel.getRowCount()-1) {
                        int height = (row+1) * getRowHeight();
                        g2.drawLine(0, height, size.width, height);
                    }
                    last_symbol = (String)sym;
                }
            }
        };
        WidgetUtil.initDynaTable(tbl, _ResultTableModel, ListSelectionModel.SINGLE_SELECTION,
            new HeadingRenderer(), false, new DynaTableCellRenderer(_ResultTableModel));
        tbl.setOpaque(false);
        JScrollPane scr = new JScrollPane(tbl); scr.getViewport().setOpaque(false);
        add(scr, BorderLayout.CENTER);
    }

    //----- private methods -----
    /**
     * Scan all symbols in quote folder by running simulation for the current strategy (on screen), present result to user in a table.
     */
    private void scanSignal() {
        if (_cmbStrategy.getSelectedIndex() == 0) {//MAC strategy
            final MacEngine engine = new MacEngine(null);

            //show progress bar
            final ProgressBar pb = ProgressBar.getInstance(MdiMainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("scan_tip_1"));
            pb.setVisible(true);

            //start a thread to simulate and export all files
            Thread thread = new Thread() {
                public void run() {
                    final ArrayList<String> failed_symbols = new ArrayList<String>();

                    //initialize
                    Date sd =_txtStartDate.getDate();
                    Calendar begin_cal = Calendar.getInstance();
                    begin_cal.setTime(sd);

                    //read list of symbols from files in folder
                    _ScanResult = new ArrayList<ScanResult>();
                    File folder = new File(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE);
                    String[] file_list = folder.list();
                    for (String file : file_list) {
                        if (!file.endsWith(FrameworkConstants.EXTENSION_QUOTE))//skip garbage
                            continue;

                        //extract symbol from file name
                        final String symbol = file.substring(0, file.indexOf(FrameworkConstants.EXTENSION_QUOTE));
                        try {
                            FundData fund = DataUtil.readFundHistory(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, symbol);
                            engine.setEntrySignal(false);  engine.setExitSignal(false);
                            engine.setFund(fund);

                            //setup simulation options, start with max range data
                            int start_index = fund.getSize() -1 - MacEngine._nExitMA2;//adjust start index this much to allow look back
                            if (start_index <= 0)
                                continue;

                            String start_date = fund.getDate(start_index);
                            StandardOption std_opt = new StandardOption(symbol, true, false,
                                start_date, fund.getQuote().get(0).getDate(), true);
                            MacOption mac_opt = new MacOption(MacEngine._nEntryMA1, MacEngine._nEntryMA2,
                                MacEngine._nEntryMA2, MacEngine._nExitMA2);
                            engine.setSimParam(new SimParam(std_opt, mac_opt));
                            EventQueue.invokeLater(new Runnable() {
                                public void run() {
                                    pb.setLabel(ApolloConstants.APOLLO_BUNDLE.getString("scan_msg_1") + " " + symbol + "  " +
                                            ApolloConstants.APOLLO_BUNDLE.getString("dld_msg_3"));
                                }
                            });

                            //run simulation
                            try {
                                engine.simulate();
                            } catch (IllegalArgumentException e) {
                                e.printStackTrace();

                                //save symbol to notify user later
                                failed_symbols.add(symbol);
                                continue;
                            }

                            ArrayList<Transaction> trans = engine.getTransactionLog();
                            if (trans == null || trans.size() == 0)
                                continue;

                            //produce report
                            String last_date = fund.getDate(0);
                            boolean is_entry = engine.isEntrySignal();
                            boolean is_exit = engine.isExitSignal();
                            SimReport rpt = engine.genReport();

                            //based on user choice of starting date and today, capture each entry and exit signal
                            //a record for each signal
                            String ed = fund.getDate(0);//first data point
                            Calendar end_cal = AppUtil.stringToCalendar(ed);

                            //for other days: loop thru all transactions, look for entry and exit signal in range
                            for (Transaction tran : trans) {
                                Calendar cal = Calendar.getInstance();
                                //for entry date
                                String entry_date = tran.getEntryDate();
                                cal.setTime(AppUtil.stringToCalendar(entry_date).getTime());
                                if (cal.compareTo(begin_cal) >= 0 && cal.compareTo(end_cal) < 0) {//entry in range
                                    _ScanResult.add(new ScanResult(
                                        symbol, true, entry_date, false, "",
                                        rpt.getCagr(), rpt.getDrawDown().getAvgPct(),
                                        rpt.getGain().getAvgPct(), rpt.getLoss().getAvgPct(),
                                        rpt.getProfitFactor(), rpt.getTimeInMarket(), rpt.getWinRatio()));
                                }
                                //for exit date
                                String exit_date = tran.getExitDate();
                                cal.setTime(AppUtil.stringToCalendar(exit_date).getTime());
                                if (cal.compareTo(begin_cal) >= 0 && cal.compareTo(end_cal) < 0) {//exit in range
                                    _ScanResult.add(new ScanResult(
                                        symbol, false, "", true, exit_date,
                                        rpt.getCagr(), rpt.getDrawDown().getAvgPct(),
                                        rpt.getGain().getAvgPct(), rpt.getLoss().getAvgPct(),
                                        rpt.getProfitFactor(), rpt.getTimeInMarket(), rpt.getWinRatio()));
                                }
                            }

                            //for the last (most recent) day
                            if (is_entry || is_exit) {
                                _ScanResult.add(new ScanResult(trans.get(0).getSymbol(),
                                        is_entry, is_entry ? last_date : "", is_exit, is_exit ? last_date : "",
                                        rpt.getCagr(), rpt.getDrawDown().getAvgPct(),
                                        rpt.getGain().getAvgPct(), rpt.getLoss().getAvgPct(),
                                        rpt.getProfitFactor(), rpt.getTimeInMarket(), rpt.getWinRatio()));
                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                            EventQueue.invokeLater(new Runnable() {//hide progress bar, error
                                public void run() { pb.setVisible(false); }
                            });
                            return;
                        }
                    }

                    //normal termination, hide progress bar, if no result, show dialog
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            pb.setVisible(false);
                            StringBuilder sb = new StringBuilder(ApolloConstants.APOLLO_BUNDLE.getString("dme_msg_1"));
                            if (failed_symbols.size() > 0) {
                                for (String sym : failed_symbols)
                                    sb.append(sym).append(", ");
                                WidgetUtil.showWarning(MdiMainFrame.getInstance(), sb.toString());
                            }

                            //show result in a table
                            _ResultTableModel.populate(_ScanResult);
                        }
                    });
                }
            };
            thread.start();
        }
    }

    //----- inner classes -----
    private class TradeTableModel extends DynaTableModel {
        public TradeTableModel() {
            remodel(DynaTableModel.generateSchema(TABLE_SCHEMA));
        }

        //-----interface implementations-----
        public void populate() {}
        public void populate(ArrayList<ScanResult> results) {
            _lstRows.clear();
            int seq = 1;
            String last_symbol = "";
            for (ScanResult result : results) {
                SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
                cells[COLUMN_SEQUENCE] = new SimpleCell(seq++);
                String symbol = result.getSymbol();
                if (!symbol.equals(last_symbol)) {//new symbol, full row
                    cells[COLUMN_SYMBOL] = new SimpleCell(symbol);  last_symbol = symbol;
                    cells[COLUMN_CAGR] = new SimpleCell(FrameworkConstants.ROI_FORMAT.format(result.getCagr()));
                    cells[COLUMN_AVG_DD] = new SimpleCell(FrameworkConstants.ROI_FORMAT.format(result.getAvgDrawDown()));
                    cells[COLUMN_AVG_GAIN] = new SimpleCell(FrameworkConstants.ROI_FORMAT.format(result.getAvgGain()));
                    cells[COLUMN_AVG_LOSS] = new SimpleCell(FrameworkConstants.ROI_FORMAT.format(result.getAvgLoss()));
                    cells[COLUMN_PF] = new SimpleCell(FrameworkConstants.PRICE_FORMAT.format(result.getProfitFactor()));
                    cells[COLUMN_MKT_PCT] = new SimpleCell(FrameworkConstants.ROI_FORMAT.format(result.getInMarketPercent()));
                    cells[COLUMN_WIN_RATIO] = new SimpleCell(FrameworkConstants.ROI_FORMAT.format(result.getWinRatio()));
                }
                else {
                    cells[COLUMN_SYMBOL] = new SimpleCell("");
                    cells[COLUMN_CAGR] = new SimpleCell("");
                    cells[COLUMN_AVG_DD] = new SimpleCell("");
                    cells[COLUMN_AVG_GAIN] = new SimpleCell("");
                    cells[COLUMN_AVG_LOSS] = new SimpleCell("");
                    cells[COLUMN_PF] = new SimpleCell("");
                    cells[COLUMN_MKT_PCT] = new SimpleCell("");
                    cells[COLUMN_WIN_RATIO] = new SimpleCell("");
                }
                //for all
                cells[COLUMN_ENTRY_SIGNAL] = new SimpleCell(result.isEntrySignal());
                cells[COLUMN_ENTRY_DATE] = new SimpleCell(result.getEntryDate());
                cells[COLUMN_EXIT_SIGNAL] = new SimpleCell(result.isExitSignal());
                cells[COLUMN_EXIT_DATE] = new SimpleCell(result.getExitDate());
                _lstRows.add(cells);
            }
            fireTableDataChanged();
        }

        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }

    //-----instance variables-----
    private JComboBox _cmbStrategy = new JComboBox(FrameworkConstants.LIST_SIM_STRATEGY);
    private JXDatePicker _txtStartDate = new JXDatePicker();
    private JButton _btnScan = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("scan_tip_1"), FrameworkIcon.RUN);
    private TradeTableModel _ResultTableModel;
    private ArrayList<ScanResult> _ScanResult = new ArrayList<ScanResult>();

    //----- literals -----
    public  static final int COLUMN_SEQUENCE = 0;
    private static final int COLUMN_SYMBOL = 1;
    private static final int COLUMN_ENTRY_SIGNAL = 2;
    private static final int COLUMN_ENTRY_DATE = 3;
    private static final int COLUMN_EXIT_SIGNAL = 4;
    private static final int COLUMN_EXIT_DATE = 5;
    private static final int COLUMN_CAGR = 6;
    private static final int COLUMN_AVG_DD = 7;
    private static final int COLUMN_AVG_GAIN = 8;
    private static final int COLUMN_AVG_LOSS = 9;
    private static final int COLUMN_PF = 10;
    private static final int COLUMN_MKT_PCT = 11;
    private static final int COLUMN_WIN_RATIO = 12;
    private static final Object[][] TABLE_SCHEMA = {
        //heading, type, visible, hide-able, alignment, width, editor, renderer, comparator
        { ApolloConstants.APOLLO_BUNDLE.getString("scan_hd_1"), ColumnTypeEnum.TYPE_STRING, -1, 10, null, null, null },//sequence
        { ApolloConstants.APOLLO_BUNDLE.getString("scan_hd_2"), ColumnTypeEnum.TYPE_STRING, -1, 30, null, null, null },//symbol
        { ApolloConstants.APOLLO_BUNDLE.getString("scan_hd_3"), ColumnTypeEnum.TYPE_BOOLEAN, -1, 45, null, null, null },//entry signal
        { ApolloConstants.APOLLO_BUNDLE.getString("scan_hd_12"), ColumnTypeEnum.TYPE_STRING, 0, 60, null, null, null },//entry date
        { ApolloConstants.APOLLO_BUNDLE.getString("scan_hd_4"), ColumnTypeEnum.TYPE_BOOLEAN, -1, 45, null, null, null },//exit signal
        { ApolloConstants.APOLLO_BUNDLE.getString("scan_hd_13"), ColumnTypeEnum.TYPE_STRING, 0, 60, null, null, null },//exit date
        { ApolloConstants.APOLLO_BUNDLE.getString("scan_hd_5"), ColumnTypeEnum.TYPE_STRING, -1, 40, null, null, null },//CAGR
        { ApolloConstants.APOLLO_BUNDLE.getString("scan_hd_6"), ColumnTypeEnum.TYPE_STRING, -1, 40, null, null, null },//avg DD
        { ApolloConstants.APOLLO_BUNDLE.getString("scan_hd_7"), ColumnTypeEnum.TYPE_STRING, -1, 40, null, null, null },//avg gain
        { ApolloConstants.APOLLO_BUNDLE.getString("scan_hd_8"), ColumnTypeEnum.TYPE_STRING, -1, 40, null, null, null },//avg loss
        { ApolloConstants.APOLLO_BUNDLE.getString("scan_hd_9"), ColumnTypeEnum.TYPE_STRING, -1, 80, null, null, null },//PF
        { ApolloConstants.APOLLO_BUNDLE.getString("scan_hd_10"), ColumnTypeEnum.TYPE_STRING, -1, 80, null, null, null },//% in market
        { ApolloConstants.APOLLO_BUNDLE.getString("scan_hd_11"), ColumnTypeEnum.TYPE_STRING, -1, 80, null, null, null },//win ratio
    };
}
