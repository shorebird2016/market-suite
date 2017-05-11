package org.marketsuite.riskmgr.portfolio;

import org.marketsuite.component.dialog.ProgressBar;
import org.marketsuite.component.graph.SimpleTimeSeriesGraph;
import org.marketsuite.component.table.ColumnTypeEnum;
import org.marketsuite.component.table.DynaTableModel;
import org.marketsuite.component.table.SimpleCell;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloPreferenceStore;
import org.marketsuite.riskmgr.model.RiskMgrModel;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.marketsuite.component.table.ColumnTypeEnum;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.riskmgr.model.RiskMgrModel;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;

//TODO verify 2/10 numbers
//TODO draw thick blue line for each week
//TODO calculate weekly changes

class PortfolioModel extends DynaTableModel {
    PortfolioModel() { remodel(DynaTableModel.generateSchema(TABLE_SCHEMA)); }
    public boolean isCellEditable(int row, int col) { return false; }

    //traverse TradeStation position folders to obtain cash/equity from each account, then sum for each date
    public void populate() {
        //open multiple TradeStation positions files from the top folder
        String trd_path = ApolloPreferenceStore.getPreferences().getTradeStationPath();
        JFileChooser fc = new JFileChooser(new File(trd_path == null ? FrameworkConstants.DATA_FOLDER_ACCOUNT : trd_path));
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setMultiSelectionEnabled(false);
        int ret = fc.showOpenDialog(MdiMainFrame.getInstance());
        if (ret != JFileChooser.APPROVE_OPTION) return;
        ApolloPreferenceStore.getPreferences().setTradeStationPath(fc.getCurrentDirectory().getPath());
        ApolloPreferenceStore.savePreferences();//flush
        _lstRows.clear();
        File root_folder = fc.getSelectedFile();
        new TradeStationThread(root_folder).start();
    }

    //----- private methods -----
    private void plotAccountEquity() {
        //look for all rows with blank accounts
        ArrayList<Double> values = new ArrayList<>();
        ArrayList<Calendar> dates = new ArrayList<>();
        for (int row = 0; row < getRowCount(); row++) {
            if (getCell(row, COLUMN_ACCOUNT).getValue().equals("")) {
                values.add((Double) getCell(row, COLUMN_VALUE).getValue());

                //go back one row to get date
                String date = (String)getCell(row - 1, COLUMN_DATE).getValue();
                dates.add(AppUtil.stringToCalendarNoEx(date));
            }
        }
        Calendar[] cals = new Calendar[dates.size()];
        for (int i=0; i<cals.length; i++)
            cals[i] = dates.get(i);
        double[] data1 = new double[values.size()];
        for (int i=0; i<data1.length; i++)
            data1[i] = values.get(i);
        double[] data2 = new double[values.size()];
        for (int i=0; i<data2.length; i++) {
            FundQuote quote = FrameworkConstants.SP500_DATA.findQuoteByDate(AppUtil.calendarToString(cals[i]));
            if (quote != null) {
                data2[i] = quote.getClose();
            }
            else {//find next available quote if this day happens to be holiday
                FundQuote nq = AppUtil.findNearestQuote(FrameworkConstants.SP500_DATA, cals[i]);
                data2[i] = nq.getClose();
            }
        }
        _plot.plot(new String[] {"Equity", "SPY"}, cals, data1, data2);
    }
    //return # of equity holdings from all positions from this file, also writes cash, equity
    private int readPosition(File position_file) {
        Workbook wb;
        try {
            wb = Workbook.getWorkbook(position_file);
        } catch (Exception e) {//fail to read somehow
            LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("rm_17") + " " + position_file.getName(), LoggingSource.RISKMGR_PORTFOLIO, e);
            return 0;
        }

        //read all rows from file, store in ret
        int ret = 0; _fCash = 0; _fEquity = 0;
        Sheet sheet = wb.getSheet(0);
        int row = RiskMgrModel.ROW_TRADESTATION_POSITION_SYMBOL;
        while (true) {
            String symbol = sheet.getCell(RiskMgrModel.COLUMN_TRADESTATION_POSITION_SYMBOL, row).getContents();
            if (symbol == null || symbol.equals(""))
                break;

            //convert special symbols for Yahoo format, TradeStation calls BRK.B
            if (symbol.equals("BRK.B")) symbol = "BRK-B";

            //obtain market value first
            String mkc = sheet.getCell(RiskMgrModel.COLUMN_TRADESTATION_MARKET_VALUE, row).getContents();
            Number mkt_val;
            try {
                mkt_val = DataUtil.CASH_POSITIVE_BALANCE_FORMAT.parse(mkc);
            } catch (ParseException e) {//fail to parse market value
                LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("rm_70") + " " + symbol, LoggingSource.RISKMGR_PORTFOLIO, e);
                row++;
                continue;
            }

            //for preferred stocks, treat them as cash instead
            boolean is_pfd = false;
            for (String ps : DataUtil.PREFERRED_SYMBOLS) {
                if (symbol.equals(ps)) {
                    _fCash  += mkt_val.floatValue();//add to total cash
                    is_pfd = true;
                    break;
                }
            }
            if (!is_pfd)
                _fEquity += mkt_val.floatValue();
            row++;
            ret++;
        }
        wb.close();
        return ret;
    }

    //read cash balance from single file, =0 if fail to parse, read file, bad format TODO useful for Riskmgr.importCash() too
    private float readCashBalance(File balance_file) {
        Workbook wb;
        try {
            wb = Workbook.getWorkbook(balance_file);
        } catch (IOException e) {
            LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("rm_95") + " " + balance_file, LoggingSource.RISKMGR_ACCOUNT);
            return 0;
        } catch (BiffException e) {
            LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("rm_96") + " " + balance_file, LoggingSource.RISKMGR_ACCOUNT);
            return 0;
        }
        Sheet sheet = wb.getSheet(0);
        String str = sheet.getCell(RiskMgrModel.COLUMN_BALANCE_CELL, RiskMgrModel.ROW_BALANCE_CELL).getContents();
        wb.close();
        try {//use positive format first
            return DataUtil.CASH_POSITIVE_BALANCE_FORMAT.parse(str).floatValue();
        } catch (ParseException e) {
            try {//fail positive format, try negative format
                return DataUtil.CASH_NEGATIVE_BALANCE_FORMAT.parse(str).floatValue();
            } catch (ParseException e1) {
                LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("rm_114") + " " + balance_file + " " + str, LoggingSource.RISKMGR_ACCOUNT);
                return 0;
            }
        }
    }
    private String extractAccount(String name) {
        for (String acct : LIST_ACCOUNT) {
            if (name.contains(acct))
                return acct;
        }
        return "";//not found
    }
    private SimpleCell[] initCells() {
        SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
        for (int col=0; col<TABLE_SCHEMA.length; col++) {
            switch (col) {
                case COLUMN_EQUITY:
                case COLUMN_CASH:
                case COLUMN_VALUE:
                case COLUMN_CASH_PCT:
                case COLUMN_WEEKLY_PCT:
                case COLUMN_SPY_WEEKLY_PCT:
                case COLUMN_DEPOSIT_WITHDRAW:
                    cells[col] = new SimpleCell(new Double(0));
                    break;
                case COLUMN_POSITIONS:
                    cells[col] = new SimpleCell(new Long(0));
                    break;
                default: cells[col] = new SimpleCell(""); break;
            }
        }
        return cells;
    }

    //----- inner classes -----
    private class TradeStationThread extends Thread {
        private TradeStationThread(File root_folder) {//of portfolio folders
            pb = ProgressBar.getInstance(MdiMainFrame.getInstance(), "");
            _RootFolder = root_folder;
            pb.setVisible(true);
            pb.setLabel(ApolloConstants.APOLLO_BUNDLE.getString("rm_06"));
        }
        public void run() {
            //traverse all weekly folders (yyyy-MM-dd), read balance/position files for each account
            for(File folder : _RootFolder.listFiles()) {//each folder has name yyyy-MM-dd pattern
                if (!folder.isDirectory()) continue;//skip files
                final String name = folder.getName();//name is actually a date
                try {//skip folder w wrong format
                    FrameworkConstants.YAHOO_DATE_FORMAT.parse(name);
                } catch (ParseException e) {
                    EventQueue.invokeLater(new Runnable() {//logger is in EDT time
                        public void run() {
                            LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("rm_94") + " " + name, LoggingSource.RISKMGR_PORTFOLIO);
                        }
                    });
                    continue;
                }

                //read files under each folder with "Position" and "balance" prefix, populate a row in table
                float cash_subtotal = 0, eqty_subtotal = 0; int position_cnt = 0;
                ArrayList<SimpleCell[]> rc1c = new ArrayList<>();
                File[] trd_files = folder.listFiles();
                for (final File trd_file : trd_files) {//could be two types of files
                    SimpleCell[] cells = initCells();
                    String trd_name = trd_file.getName();
                    cells[COLUMN_DATE].setValue(name);
                    String acct = extractAccount(trd_name);
                    cells[COLUMN_ACCOUNT].setValue(acct);
                    if (trd_name.startsWith(PREFIX_POSITION)) {
                        int pos_cnt = readPosition(trd_file);
                        eqty_subtotal += _fEquity;
                        cash_subtotal += _fCash;
                        position_cnt += pos_cnt;
                        cells[COLUMN_EQUITY].setValue(new Double(_fEquity));
                        cells[COLUMN_CASH].setValue(new Double(_fCash));
                        cells[COLUMN_POSITIONS].setValue(new Long(pos_cnt));
                        rc1c.add(cells);
                    }
                    else if (trd_name.startsWith(PREFIX_BALANCE)) {
                        float cash = readCashBalance(trd_file);
                        cash_subtotal += cash;
                        cells[COLUMN_CASH].setValue(new Double(cash));
                        rc1c.add(cells);
                    }
                    EventQueue.invokeLater(new Runnable() {
                        public void run() { pb.setLabel(ApolloConstants.APOLLO_BUNDLE.getString("rm_07") + "<br>" + trd_file.getPath()); }
                    });
                    try { sleep(5); } catch (InterruptedException e) { }//allow prog bar to spin
                }

                //combine rows with same account number, merge cash and equity columns
                ArrayList<SimpleCell[]> rc2c = new ArrayList<>();
                for (SimpleCell[] rc1 : rc1c) {
                    String acct1 = (String)rc1[COLUMN_ACCOUNT].getValue();
                    double eqty1 = (Double)rc1[COLUMN_EQUITY].getValue();
                    double cash1 = (Double)rc1[COLUMN_CASH].getValue();
                    long pos1 = (Long)rc1[COLUMN_POSITIONS].getValue();

                    //search for another row with same account number
                    boolean found = false;
                    for (SimpleCell[] rc2 : rc2c) {
                        String acct2 = (String)rc2[COLUMN_ACCOUNT].getValue();
                        double eqty2 = (Double)rc2[COLUMN_EQUITY].getValue();
                        double cash2 = (Double)rc2[COLUMN_CASH].getValue();
                        long pos2 = (Long)rc2[COLUMN_POSITIONS].getValue();
                        if (acct2.equals(acct1)) {//found, merge
                            rc2[COLUMN_EQUITY].setValue(eqty2 + eqty1);
                            rc2[COLUMN_CASH].setValue(cash1 + cash2);
                            rc2[COLUMN_POSITIONS].setValue(pos1 + pos2);
                            _lstRows.add(rc2);
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        rc2c.add(rc1);//not found, copy over
                }

                //folder done, add a blank subtotal row
                SimpleCell[] cells = initCells();
                cells[COLUMN_EQUITY].setValue(new Double(eqty_subtotal));
                cells[COLUMN_CASH].setValue(new Double(cash_subtotal));
                float value = cash_subtotal + eqty_subtotal;
                cells[COLUMN_VALUE].setValue(new Double(value));
                double pct = cash_subtotal / value;
                cells[COLUMN_CASH_PCT].setValue(new Double(pct));
                cells[COLUMN_POSITIONS].setValue(new Long(position_cnt));
                _lstRows.add(cells);
            }

            //calculate weekly changes from 1 week to next
// TODO

            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    pb.setVisible(false);
                    fireTableDataChanged();
                    plotAccountEquity();
                }
            });
        }
        private File _RootFolder;
        private ProgressBar pb;
    }

    //----- accessor -----
    void setPlot(SimpleTimeSeriesGraph plot) { _plot = plot; }

    //----- variables -----
    private float _fCash, _fEquity;
    private SimpleTimeSeriesGraph _plot;

    //----- literals -----
    public static final String PREFIX_BALANCE = "balance";
    public static final String PREFIX_POSITION = "Position";
    static final int COLUMN_ACCOUNT = 0;
    static final int COLUMN_DATE = 1;
    static final int COLUMN_EQUITY = 2;
    static final int COLUMN_CASH = 3;
    static final int COLUMN_VALUE = 4;
    static final int COLUMN_CASH_PCT = 5;
    static final int COLUMN_WEEKLY_PCT = 6;
    static final int COLUMN_SPY_WEEKLY_PCT = 7;
    static final int COLUMN_POSITIONS = 8;
    static final int COLUMN_DEPOSIT_WITHDRAW = 9;
    private static final String[] LIST_ACCOUNT = { "466", "391", "516", "861" };
    static final Object[][] TABLE_SCHEMA = {
        { ApolloConstants.APOLLO_BUNDLE.getString("rm_22"),  ColumnTypeEnum.TYPE_STRING, -1,  30, null, null, null },//0, account
        { ApolloConstants.APOLLO_BUNDLE.getString("rm_23"),  ColumnTypeEnum.TYPE_STRING, -1,  50, null, null, null },//1, date
        { ApolloConstants.APOLLO_BUNDLE.getString("rm_24"),  ColumnTypeEnum.TYPE_DOUBLE, -1,  50, null, null, null },//2, equity
        { ApolloConstants.APOLLO_BUNDLE.getString("rm_25"),  ColumnTypeEnum.TYPE_DOUBLE,  3,  50, null, null, null },//3, cash
        { ApolloConstants.APOLLO_BUNDLE.getString("rm_29"),  ColumnTypeEnum.TYPE_DOUBLE, -1,  30, null, null, null },//4, value of all accounts
        { ApolloConstants.APOLLO_BUNDLE.getString("rm_26"),  ColumnTypeEnum.TYPE_DOUBLE, -1,  30, null, null, null },//5, cash %,
        { ApolloConstants.APOLLO_BUNDLE.getString("rm_27"),  ColumnTypeEnum.TYPE_DOUBLE, -1,  50, null, null, null },//6, weekly %
        { ApolloConstants.APOLLO_BUNDLE.getString("rm_28"),  ColumnTypeEnum.TYPE_DOUBLE, -1,  30, null, null, null },//7, SPY weekly %
        { ApolloConstants.APOLLO_BUNDLE.getString("rm_30"),  ColumnTypeEnum.TYPE_LONG,   -1,  30, null, null, null },//8, positions
        { ApolloConstants.APOLLO_BUNDLE.getString("rm_31"),  ColumnTypeEnum.TYPE_DOUBLE, -1,  30, null, null, null },//9, deposit/withdraw
    };
//    public void populateOLD() {//read from file
//        try {
//            _infos.clear();
//            BufferedReader br = new BufferedReader(new FileReader(new File(FrameworkConstants.PORTFOLIO_DB)));
//            String line;
//            br.readLine();//skip header
//            while ( (line = br.readLine()) != null && !line.equals("")) {
//                String[] tokens = line.split(",");
//                _infos.add(new Info(tokens[0], tokens[1], tokens[2], tokens[3], tokens[4], tokens[5]));
//            }
//            Info[] ifs = new Info[_infos.size()];
//            for (int i=0; i<_infos.size(); i++)
//                ifs[i] = _infos.get(i);
//
//            //order arrau based on account then dates in descending order
//            Arrays.sort(ifs, new InfoComparator());
//            br.close();
//
//            //render into cells
//            initRow(ifs);
//        } catch (IOException e) {
//            WidgetUtil.showWarning(ApolloConstants.APOLLO_BUNDLE.getString("dme_txt_4") + e.getMessage());
//        }
//    }
//    ArrayList<String> savePortfolio() throws IOException {
//        ArrayList<String> ret = new ArrayList<>();
//        PrintWriter pw = new PrintWriter(new FileWriter(FrameworkConstants.PORTFOLIO_DB));
//        StringBuilder buf = new StringBuilder(ApolloConstants.APOLLO_BUNDLE.getString("rm_37") + "\n");
//        for (int row = 0; row < getRowCount(); row++) {
////            if (!getCell(row, COLUMN_DIRTY).getValue().equals(DirtyCellRenderer.ADD))//skip non-add
////                continue;
//            String acct = (String)getCell(row, COLUMN_ACCOUNT).getValue();
//            String date = (String)getCell(row, COLUMN_DATE).getValue();
//            double eqty = (Double)getCell(row, COLUMN_EQUITY).getValue();
//            if (eqty == 0) {//check 0 eqty
//                ret.add(acct + ":" + date  + " " + ApolloConstants.APOLLO_BUNDLE.getString("rm_36"));
//                continue;
//            }
//            buf.append(acct).append(",").append(date).append(",")
//                    .append(eqty).append(",")
//                    .append(getCell(row, COLUMN_CASH).getValue()).append(",")
//                    .append(getCell(row, COLUMN_POSITIONS).getValue()).append(",")
//                    .append(getCell(row, COLUMN_DEPOSIT_WITHDRAW).getValue()).append("\n");
//        }
//        pw.println(buf.toString());
//        pw.flush();
//        pw.close();
//        return ret;
//    }

    //----- private methods -----
    //adjust cash percent due to user change
//    private void adjCashPct(int row) {
//        double eqty = (Double)getCell(row, COLUMN_EQUITY).getValue();
//        double cash = (Double)getCell(row, COLUMN_CASH).getValue();
//        double pct = cash / eqty;
//        getCell(row, COLUMN_CASH_PCT).setValue(pct);
//        fireTableCellUpdated(row, COLUMN_CASH_PCT);
//    }
    //adjust weekly delta due to user change
//    private void adjWeeklyDelta(int row) {}

    //calculate weekly value changes
//    private void calcWeeklyChange() {
//        FundData spy = null, iwm = null;
//        try {
//            spy = DataUtil.readHistory("SPY", 100);
//            iwm = DataUtil.readHistory("IWM", 100);
//        } catch (IOException e) {
//            WidgetUtil.showWarning(ApolloConstants.APOLLO_BUNDLE.getString("dme_txt_4") + e.getMessage());
//        }
//        int row1 = 0;
//        int row2 = 1;
//        do {
//            String acct1 = (String)getCell(row1, COLUMN_ACCOUNT).getValue();
//            String acct2 = (String)getCell(row2, COLUMN_ACCOUNT).getValue();
//            if (acct1.equals(acct2)) {//same account, figure out weekly change between adjacent rows
//                double eqty1 = (Double)getCell(row1, COLUMN_EQUITY).getValue();
//                double eqty2 = (Double)getCell(row2, COLUMN_EQUITY).getValue();
//                double delta = eqty1 - eqty2;
//                double pct = delta / eqty2;
//                getCell(row1, COLUMN_WEEKLY_DELTA).setValue(delta);
//                getCell(row1, COLUMN_WEEKLY_PCT).setValue(pct);
//                String date1 = (String)getCell(row1, COLUMN_DATE).getValue();
//                String date2 = (String)getCell(row2, COLUMN_DATE).getValue();
//                if (spy != null) {
//                    double close1 = spy.findQuoteByDate(date1).getClose();
//                    double close2 = spy.findQuoteByDate(date2).getClose();
//                    getCell(row1, COLUMN_SPY_WEEKLY_PCT).setValue((close1 - close2)/close2);
//                }
//                if (iwm != null) {
//                    double close1 = iwm.findQuoteByDate(date1).getClose();
//                    double close2 = iwm.findQuoteByDate(date2).getClose();
//                    getCell(row1, COLUMN_IWM_WEEKLY_PCT).setValue((close1 - close2)/close2);
//                }
//            }
//            row1++; row2++;
//        } while(row2 < getRowCount());
//    }
    //----- inner classes -----
//    private class Info {
//        private Info(String account, String date, String eqity, String cash, String positions, String deposit) {
//            this.account = account;
//            this.date = date;
//            this.eqity = eqity;
//            this.cash = cash;
//            this.positions = positions;
//            this.deposit = deposit;
//        }
//        private String account;
//        private String date;
//        private String eqity;
//        private String cash;
//        private String positions;
//        private String deposit;
//    }
//    private class InfoComparator implements Comparator<Info> {
//        public int compare(Info info1, Info info2) {
//            if (info1.account.equals(info2.account) && info1.date.equals(info2.date)) return 0;
//            boolean acct_equal = info1.account.equals(info2.account);
//            boolean acct_bigger = info1.account.compareTo(info2.account) > 0;
//            boolean date_bigger = info1.date.compareTo(info2.date) > 0;
//
//            //one of them is equal
//            if (acct_equal) {
//                if (date_bigger) return -1;
//                return 1;
//            }
//            if (acct_bigger)//account is more important
//                return 1;
//            return -1;
//        }
//    }
//    private ArrayList<Info> _infos = new ArrayList<>();
}
