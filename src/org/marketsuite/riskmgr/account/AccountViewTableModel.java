package org.marketsuite.riskmgr.account;

import org.marketsuite.component.table.*;
import org.marketsuite.framework.model.Fundamental;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MainModel;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.riskmgr.model.MatrixElement;
import org.marketsuite.riskmgr.model.Position;
import org.marketsuite.riskmgr.model.RiskMgrModel;
import org.marketsuite.riskmgr.model.StopLevel;
import org.marketsuite.riskmgr.model.StopLevelInfo;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.main.MainModel;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.riskmgr.model.RiskMgrModel;
import org.marketsuite.riskmgr.model.StopLevel;
import org.marketsuite.riskmgr.model.StopLevelInfo;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

class AccountViewTableModel extends DynaTableModel {
    AccountViewTableModel() { remodel(DynaTableModel.generateSchema(TABLE_SCHEMA)); }

    //-----interface implementations-----
    public boolean isCellEditable(int row, int col) { return getCell(row, col).isHighlight(); }
    public void populate() {//directly from RiskMgrModel
        _lstRows.clear();//empty table
        ArrayList<LogMessage> failed_msgs = new ArrayList<>();
        ArrayList<Position> positions = RiskMgrModel.getInstance().getPositions();
        for (Position pos : positions) {
            SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
            String symbol = pos.getSymbol();
            cells[COLUMN_DIRTY] = new SimpleCell(DirtyCellRenderer.NORMAL);
            cells[COLUMN_SYMBOL] = new SimpleCell(symbol);
            int shares = pos.getShares();
            cells[COLUMN_SHARES] = new SimpleCell(new Long(shares));
            float cost = pos.getCost();
            cells[COLUMN_COST] = new SimpleCell(new Double(cost));
            float quote = pos.getStopLevelInfo().getQuotes().get(0).getClose();
            cells[COLUMN_MARKET_VALUE] = new SimpleCell(new Double(pos.getMarketValue()));//quote * shares));
            cells[COLUMN_STOP_METHOD] = new SimpleCell(StopLevelInfo.LIST_STOP_METHOD[StopLevelInfo.STOP_CUSTOM_INDEX]);//TODO load previous stop methods from file
            float stop = pos.getStop();
            cells[COLUMN_STOP_PRICE] = new SimpleCell(new Double(stop));
            double risk = pos.getRisk();
            cells[COLUMN_COST_RISK] = new SimpleCell(new Double(risk));
            cells[COLUMN_COST_RISK_PERCENT] = new SimpleCell(new Double(risk / (cost * shares)));
            cells[COLUMN_REAL_PROFIT_LOSS] = new SimpleCell(new Double((stop - cost) * shares));
            cells[COLUMN_REAL_PROFIT_LOSSS_PERCENT] = new SimpleCell(new Double((stop - cost) / quote));
            double plp = quote - cost;
            double pl = shares * plp;
            cells[COLUMN_PROFIT_LOSS_AMOUNT] = new SimpleCell(new Double(pl));
            cells[COLUMN_PROFIT_LOSS_PERCENT] = new SimpleCell(new Double(plp / cost));
            cells[COLUMN_CURRENT_PRICE] = new SimpleCell(new Double(quote));
            cells[COLUMN_ACCOUNT_ID] = new SimpleCell(pos.getAccount());

            //find custom group using preloaded map in RiskMgrModel
            cells[COLUMN_CUSTOM_GROUP] = new SimpleCell("");
            String cgrp = RiskMgrModel.getInstance().findGroup(symbol);
            if (cgrp != null)
                cells[COLUMN_CUSTOM_GROUP] = new SimpleCell(cgrp);
            cells[COLUMN_FINVIZ_GROUP] = new SimpleCell("");
            Fundamental fm = MainModel.getInstance().getFundamentals().get(symbol);
            if (fm != null)
                cells[COLUMN_FINVIZ_GROUP] = new SimpleCell(fm.getIndustry());
            decorateRow(cells);
            _lstRows.add(cells);
        }
        fireTableDataChanged();
        if (failed_msgs.size() > 0)
            Props.Log.setValue(null, failed_msgs);
        RiskMgrModel.getInstance().setIndustryMatrix(createPortfolioMatrix());
    }
    public void setValueAt(Object value, int row, int col) {
        //set to same value, do nothing
        if (value.equals(getCell(row, col).getValue()))
            return;

        super.setValueAt(value, row, col);//save first for various calcXXX methods
        String symbol = (String)getCell(row, COLUMN_SYMBOL).getValue();
        StopLevelInfo sli = RiskMgrModel.getInstance().getStopLevelInfo(symbol);
        switch(col) {
            case COLUMN_STOP_METHOD:
            case COLUMN_STOP_PRICE:
                if (col == COLUMN_STOP_PRICE) {//custom
                    double stop = (Double) value;
                    sli.setStop((float)stop);
                }
                else {//non-custom, use calculation
                    String method_id = (String)getCell(row, COLUMN_STOP_METHOD).getValue();
                    sli.setStopByMethod(method_id);
                }
                getCell(row, COLUMN_STOP_PRICE).setValue(new Double(sli.getStop()));
                decorateRow(getRow(row));//allow edit if stop price set to "Custom"
                calcRisk(row);
                sli.sort();
                getCell(row, COLUMN_DIRTY).setValue(DirtyCellRenderer.EDIT);
                fireTableRowsUpdated(row, row);
                Props.StopChanged.setValue(null, new Integer(row));//update fields and graphs
                break;

            default:
        }
    }
    public void clearDirty() {
        for (int row=0; row<getRowCount(); row++)
            getCell(row, COLUMN_DIRTY).setValue(DirtyCellRenderer.NORMAL);
        fireTableDataChanged();
    }

    //protected methods
    //download current quotes for all from Yahoo
//    void updateQuotes() {
//        //show progress bar
//        final ProgressBar pb = ProgressBar.getInstance(com.clec.riskmanager.RiskMgrModel.getInstance().getParent(), "");
//        pb.setVisible(true);
//
//        //start a thread to simulate and export all files
//        Thread thread = new Thread() {
//            public void run() {
//                EventQueue.invokeLater(new Runnable() {
//                    public void run() {
//                        pb.setLabel(ApolloConstants.APOLLO_BUNDLE.getString("active_msg_5"));
//                    }
//                });
//
//                for (int row = 0; row < getRowCount(); row++) {
////                    if (getCell(row, COLUMN_SEQUENCE).getValue().equals(""))
////                        continue;//skip blank row
//                    final String sym = (String)getCell(row, COLUMN_SYMBOL).getValue();
//                    Calendar cal = Calendar.getInstance();
//
//                    //loop up to 7 days till yahoo has good response, to avoid weekend, holiday no quotes
//                    FundQuote quote;
//                    int limit = 7;
//                    do {
//                        int cur_month = cal.get(Calendar.MONTH);
//                        int cur_day = cal.get(Calendar.DAY_OF_MONTH);
//                        int cur_year = cal.get(Calendar.YEAR);
//                        final String dt = sym + ": " + cur_month + "/" + cur_day + "/" + cur_year;
//System.out.print("\n....Get quote for " + dt + " =====> ");//todo better logging
//
//                        //request current day quote
//                        EventQueue.invokeLater(new Runnable() {
//                            public void run() {
//                                pb.setLabel(ApolloConstants.APOLLO_BUNDLE.getString("active_msg_5") + " " + sym + " on " + dt);
//                            }
//                        });
//                        try {
//                            quote = DataUtil.getYahooQuote(sym);
//                            if (quote == null) {
//                                cal.add(Calendar.DAY_OF_YEAR, -1);//go back one day
//                                limit--;
////TODO to logging frame                                System.out.println("\t??? NO Quote: " + cur_month + "/" + cur_day + "/" + cur_year);//todo better logging
//                            }
//                            else {//got quote
//                                Position pos = RiskMgrModel.getInstance().findPosition(sym);
//                                pos.getStopLevelInfo().getQuotes().get(0).setClose(quote.getClose());
////                                getCell(row, COLUMN_CURRENT_PRICE).setValue(new Double(quote.getClose()));
////TODO                                calcEquityPL(row);
//                                break;
//                            }
//                        } catch (IOException e) {
//                            EventQueue.invokeLater(new Runnable() {
//                                public void run() {//TODO to logging frame
//                                    MessageBox.messageBox(com.clec.riskmanager.RiskMgrModel.getInstance().getParent(),
//                                        FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
//                                        ApolloConstants.APOLLO_BUNDLE.getString("active_msg_3") + ": " + sym,
//                                        MessageBox.OK_OPTION, MessageBox.WARNING_MESSAGE);
//                                    pb.setVisible(false);
//                                }
//                            });
//                            e.printStackTrace();
//                        }
//                    }while (limit > 0);
//
//                    //if 7 days back, still no quote, warn user
//                    if (limit <= 0) {
//                        EventQueue.invokeLater(new Runnable() {
//                            public void run() {
//                                MessageBox.messageBox(com.clec.riskmanager.RiskMgrModel.getInstance().getParent(),
//                                    FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
//                                    ApolloConstants.APOLLO_BUNDLE.getString("active_msg_2") + ": " + sym,
//                                    MessageBox.OK_OPTION, MessageBox.WARNING_MESSAGE);
//                                pb.setVisible(false);
//                            }
//                        });
//                    }
//                }
//
//                //update table and status bar
//                EventQueue.invokeLater(new Runnable() {
//                    public void run() {
//                        fireTableDataChanged();
////TODO                        calcStatusBarFields();
//                        pb.setVisible(false);
//                    }
//                });
//            }
//        };
//        thread.start();
//    }

    //populate with sectors or industries at custom group column
    // and to create different portfolio matrix in RiskMgrModel and different matrix
    // this assumes table is already populated with industries
    void showSectorIndustry(boolean show_sector) {
        if (show_sector) {
            for (int row = 0; row < getRowCount(); row++) {
                String industry = (String)getCell(row, COLUMN_CUSTOM_GROUP).getValue();
                String sector = industry.split(" ")[0];
                getCell(row, COLUMN_CUSTOM_GROUP).setValue(sector);
                getCell(row, COLUMN_CUSTOM_GROUP).setDirty(false);
            }
            fireTableDataChanged();
            RiskMgrModel.getInstance().setSectorMatrix(createPortfolioMatrix());
        }
        else
            populate();
    }

    StopLevelInfo getStopInfo(int row) {
        String symbol = (String)getCell(row, COLUMN_SYMBOL).getValue();
        return RiskMgrModel.getInstance().getStopLevelInfo(symbol);
    }

    void discardChanges() {//TODO
    }

    //save and read back stops
    void saveStops(File file) throws IOException {
        HashMap<String, StopLevel> stop_map = RiskMgrModel.getInstance().getStopMap();
        for (int row = 0; row < getRowCount(); row++) {
            String symbol = (String)getCell(row, COLUMN_SYMBOL).getValue();
            String stop_method = (String)getCell(row, COLUMN_STOP_METHOD).getValue();
            double stop_price = (Double)getCell(row, COLUMN_STOP_PRICE).getValue();
            StopLevel sl = new StopLevel(symbol, stop_price, stop_method);
            stop_map.put(symbol, sl);
        }
        RiskMgrModel.getInstance().writeStops(file);
    }

    //two types of risks: cost based risk and market value based risk
    float getTotalCostRisk() {
        float ret = 0;
        for (int i=0; i<getRowCount(); i++) {
            double risk = (Double) getCell(i, COLUMN_COST_RISK).getValue();
            ret += risk;
        }
        return ret;
    }
    float getTotalMarketRisk() {
        float ret = 0;
        for (int i=0; i<getRowCount(); i++) {
            double risk = (Double) getCell(i, COLUMN_REAL_PROFIT_LOSS).getValue();
            ret += risk;
        }
        return ret;
    }

    //calculate market value, return formatted string
    float getMarketValue() {
        float ret = 0;

        for (int i=0; i<getRowCount(); i++) {
            double mkt_val = (Double) getCell(i, COLUMN_MARKET_VALUE).getValue();
            ret += mkt_val;
        }
        return ret;
    }
    float getTotalCost() {
        float ret = 0;
        for (int i=0; i<getRowCount(); i++) {
            double entry_price = (Double) getCell(i, COLUMN_COST).getValue();
            long shares = (Long) getCell(i, COLUMN_SHARES).getValue();
            ret += shares * entry_price;
        }
        return ret;
    }

    ArrayList<String> getSymbols() {
        ArrayList<String> ret = new ArrayList<>();
        for (int row = 0; row < getRowCount(); row++)
            ret.add((String)getCell(row, COLUMN_SYMBOL).getValue());
        return ret;
    }

    //calculate risk from shares, entry and stop, assuming they are already in _lstRows
    private void calcRisk(int row) {
        long shares = (Long)getCell(row, COLUMN_SHARES).getValue();
        double cost = (Double)getCell(row, COLUMN_COST).getValue();
        double stop_price = (Double)getCell(row, COLUMN_STOP_PRICE).getValue();
        double risk = (stop_price - cost) * shares;
        if (risk > 0)
            risk = 0;
        getCell(row, COLUMN_COST_RISK).setValue(risk);
        getCell(row, COLUMN_COST_RISK_PERCENT).setValue(risk / (shares * cost));
        double quote = (Double)getCell(row, COLUMN_CURRENT_PRICE).getValue();
        double mkt_risk = (quote - stop_price) * shares;
        getCell(row, COLUMN_REAL_PROFIT_LOSS).setValue(mkt_risk);
        getCell(row, COLUMN_REAL_PROFIT_LOSSS_PERCENT).setValue(mkt_risk / (shares * quote));
    }
    private void decorateRow(SimpleCell[] cells) {
        cells[COLUMN_STOP_METHOD].setHighlight(true);
        boolean custom_stop = cells[COLUMN_STOP_METHOD].getValue().equals(StopLevelInfo.LIST_STOP_METHOD[StopLevelInfo.STOP_CUSTOM_INDEX]);//50MA
        cells[COLUMN_STOP_PRICE].setHighlight(custom_stop);
    }

    //use custom group column and symbol columns to create map of group lists (group name : list of symbols), store it in RiskMgrModel
    private HashMap<String, ArrayList<MatrixElement>> createPortfolioMatrix() {
        HashMap<String, ArrayList<MatrixElement>> ret = new HashMap<>();
        for (int row = 0; row < getRowCount(); row++) {
            String sym = (String)getCell(row, COLUMN_SYMBOL).getValue();
            String grp = (String)getCell(row, COLUMN_CUSTOM_GROUP).getValue();
            double pl = (Double)getCell(row, COLUMN_PROFIT_LOSS_PERCENT).getValue();
            double risk = (Double)getCell(row, COLUMN_COST_RISK).getValue();
            double volatility = 0;
            ArrayList<MatrixElement> elem_list = ret.get(grp);
            if (elem_list == null)
                elem_list = new ArrayList<>();
            elem_list.add(new MatrixElement(sym, RiskMgrModel.getInstance().findPosition(sym), (float)pl, (float)volatility));
            ret.put(grp, elem_list);
        }
        return ret;
    }

    //-----variables / accessors-----
    private int _nATRLength = DEFAULT_ATR_LENGTH;
    public void setAtrLength(int len) {
        _nATRLength = len;
    }
    public int getAtrLength() { return _nATRLength; }

    private int _nQuoteLookback = DEFAULT_QUOTE_LOOKBACK;
    public void setQuoteLookback(int look_back) {
        _nQuoteLookback = look_back;
    }
    public int getQuoteLookback() { return _nQuoteLookback; }

    private double _dQtrAdjFactor = DEFAULT_QUARTERLY_ADJUSTMENT;
    public void setQtrAdjFactor(double adj) {
        _dQtrAdjFactor = adj;
    }
    public double getQtrAdjFactor() { return _dQtrAdjFactor; }

    //-----literals-----
    private static final int DEFAULT_ATR_LENGTH = 14;
    private static final int DEFAULT_QUOTE_LOOKBACK = 60;
    private static final double DEFAULT_QUARTERLY_ADJUSTMENT = 1.5;//percent per quarter
    private static final double DEFAULT_STOP_PCT = 0.05;
            static final int COLUMN_DIRTY = 0;
            static final int COLUMN_SYMBOL = 1;
            static final int COLUMN_SHARES = 2;
            static final int COLUMN_COST = 3;//per share including commission
            static final int COLUMN_STOP_METHOD = 4;
            static final int COLUMN_STOP_PRICE = 5;
            static final int COLUMN_COST_RISK = 6;//risk from cost in terms of dollar amount
            static final int COLUMN_COST_RISK_PERCENT = 7;//risk from cost in terms of percent
            static final int COLUMN_REAL_PROFIT_LOSS = 8;//risk from market value in terms of dollar amount
            static final int COLUMN_REAL_PROFIT_LOSSS_PERCENT = 9;//risk from market value in terms of percent
            static final int COLUMN_CURRENT_PRICE = 10;
            static final int COLUMN_PROFIT_LOSS_AMOUNT = 11;
            static final int COLUMN_PROFIT_LOSS_PERCENT = 12;
            static final int COLUMN_MARKET_VALUE = 13;
            static final int COLUMN_ACCOUNT_ID = 14;
            static final int COLUMN_CUSTOM_GROUP = 15;
            static final int COLUMN_FINVIZ_GROUP = 16;

    //todo: add adjusted risk: original risk plus basis (fixed income return)
    static final Object[][] TABLE_SCHEMA = {
        //heading, type, visible, hide-able, alignment, width, editor, renderer, comparator
        { "",         ColumnTypeEnum.TYPE_STRING, -1,  25, null, null, null },//0, dirty
        { ApolloConstants.APOLLO_BUNDLE.getString("active_col_3"),  ColumnTypeEnum.TYPE_STRING, -1,  60, new NameCellEditor(false), null, null },//1, symbol
        { ApolloConstants.APOLLO_BUNDLE.getString("active_col_4"),  ColumnTypeEnum.TYPE_LONG,   -1,  50, new LongIntegerCellEditor(100, 0, 1, 5000), null, null },//2, shares
        { ApolloConstants.APOLLO_BUNDLE.getString("rm_18"),         ColumnTypeEnum.TYPE_DOUBLE, -1,  60, new DecimalCellEditor(0, 3, 0, 1000, null), null, null },//3, cost
        { ApolloConstants.APOLLO_BUNDLE.getString("active_col_11"), ColumnTypeEnum.TYPE_STRING, -1,  80, new ComboCellEditor(new JComboBox(StopLevelInfo.LIST_STOP_METHOD)), null, null },//stop method
//        { ApolloConstants.APOLLO_BUNDLE.getString("active_col_18"), ColumnTypeEnum.TYPE_DOUBLE, -1,8 60, null, null, null },//7, ATR
//        { ApolloConstants.APOLLO_BUNDLE.getString("active_col_20"), ColumnTypeEnum.TYPE_DOUBLE, -1,8100, new DecimalCellEditor(0, 3, 0, 1000, null), null, null },//8, ATR Multipler
//        { ApolloConstants.APOLLO_BUNDLE.getString("active_col_19"), ColumnTypeEnum.TYPE_DOUBLE, -1,8 60, new DecimalCellEditor(0, 3, 0, 1000, null), null, null },//9, SWP
        { ApolloConstants.APOLLO_BUNDLE.getString("rm_14"),         ColumnTypeEnum.TYPE_DOUBLE, -1,  70, new DecimalCellEditor(0, 3, 0, 5000, null), null, null },//5, stop price
        { ApolloConstants.APOLLO_BUNDLE.getString("rm_61"),         ColumnTypeEnum.TYPE_DOUBLE, -1,  70, null, null, null },//6, cost risk
        { ApolloConstants.APOLLO_BUNDLE.getString("rm_62"),         ColumnTypeEnum.TYPE_DOUBLE, -1,  70, null, null, null },//7, cost risk %
        { ApolloConstants.APOLLO_BUNDLE.getString("rm_63"),         ColumnTypeEnum.TYPE_DOUBLE, -1,  70, null, null, null },//8, real P/L
        { ApolloConstants.APOLLO_BUNDLE.getString("rm_64"),         ColumnTypeEnum.TYPE_DOUBLE, -1,  70, null, null, null },//9, real P/L %
        { ApolloConstants.APOLLO_BUNDLE.getString("rm_19"),         ColumnTypeEnum.TYPE_DOUBLE, -1,  60, null, null, null },//10, cur price
        { ApolloConstants.APOLLO_BUNDLE.getString("active_col_16"), ColumnTypeEnum.TYPE_DOUBLE, -1,  70, null, null, null },//11, P/L $
        { ApolloConstants.APOLLO_BUNDLE.getString("active_col_15"), ColumnTypeEnum.TYPE_DOUBLE, -1,  60, null, null, null },//12, P/L %
        { ApolloConstants.APOLLO_BUNDLE.getString("rm_20"),         ColumnTypeEnum.TYPE_DOUBLE, -1,  80, null, null, null },//13, cur equity
        { ApolloConstants.APOLLO_BUNDLE.getString("active_col_22"), ColumnTypeEnum.TYPE_STRING, -1,  50, null, null, null },//14, account ID
        { ApolloConstants.APOLLO_BUNDLE.getString("rm_48"),         ColumnTypeEnum.TYPE_STRING,  0,  150, null, null, null },//15 custom industry/sector
        { ApolloConstants.APOLLO_BUNDLE.getString("rm_49"),         ColumnTypeEnum.TYPE_STRING,  0,  250, null, null, null },//16, finviz sector


//        { ApolloConstants.APOLLO_BUNDLE.getString("active_col_10"), ColumnTypeEnum.TYPE_STRING, -1, 100, new ComboCellEditor(new JComboBox(LIST_TARGET_METHOD)), null, null },//17, target method
//        { ApolloConstants.APOLLO_BUNDLE.getString("active_col_7"),  ColumnTypeEnum.TYPE_DOUBLE, -1,  60, null, null, null },//18, target price
//        { ApolloConstants.APOLLO_BUNDLE.getString("active_col_21"), ColumnTypeEnum.TYPE_STRING,  0, 300, new PlainCellEditor(), null, null },//19, notes
    };
}

//todo: later adjust risk = risk * (1 + adjustment) * number of years
//        try {
//            String start_date = (String)getCell(row, COLUMN_ENTRY_DATE).getValue();
//            String cur_date = AppUtil.calendarToString(Calendar.getInstance());
//            double num_yr = AppUtil.calcYear(start_date, cur_date);
//            entry_price += entry_price * _dQtrAdjFactor * 4 * num_yr;//raise entry price
//            double adj_risk = (entry_price - stop_price) * shares;
//            getCell(row, COLUMN_COST_RISK_PERCENT).setValue(adj_risk);
//what's the equity for everything sold at stop ?
//    void calcStopEquity() {
//        _dStopEquity = 0;
//        for (int i=0; i<getRowCount(); i++) {
////            Object dir = getCell(i, COLUMN_DIRECTION).getValue();
////            if (dir.equals(""))
////                continue;
//
//            double stop = (Double)getCell(i, COLUMN_STOP_PRICE).getValue();
//            long shares = (Long)getCell(i, COLUMN_SHARES).getValue();
//            _dStopEquity += stop * shares;
//        }
//        _dMaxPullbackAmount = _dMarketValue - _dStopEquity;
//        _dMaxPullbackPercent = _dMaxPullbackAmount / _dMarketValue;
//    }
//    double getStopEquity() { return _dStopEquity; }
//calculate percent at risk
//    double getAdjPercent() {
//        double pct = _dTotalAdjRisk / _dTotalCost;
//        if (_dTotalCost == 0)
//            pct = 0;
//        return pct;
//    }
//    float getTotalCostRiskPercent() {
//        return (float)(getTotalCostRisk() / getTotalCost());
//    double getAdjustedRisk() { return _dTotalAdjRisk; }

//save sandbox to file of .trd type (pretty much like a .csv)
//    void saveAccount(File output_path) throws IOException {
//        PrintWriter pw = new PrintWriter(new FileWriter(output_path));
//        pw.println("#FORMAT: Trade direction, Symbol, Shares, Entry date, " +
//                "Entry price, Target method, Stop method, ATR, SWP, Stop price, Current Price, " +
//                "ATR Multiplier, Notes");
//        pw.println("# Empty first column indicates a separator.");
//        StringBuilder sb = new StringBuilder();
//        for (int row = 0; row < getRowCount(); row++) {
//            sb//.append(getCell(row, COLUMN_DIRECTION).getValue()).append(",")//token 0
//              .append(getCell(row, COLUMN_SYMBOL).getValue()).append(",") //token 1
//              .append(getCell(row, COLUMN_SHARES).getValue()).append(",") //token 2
//              //.append(getCell(row, COLUMN_ENTRY_DATE).getValue()).append(",") //token 3
//              .append(getCell(row, COLUMN_COST).getValue()).append(",") //token 4
//              //.append(getCell(row, COLUMN_TARGET_METHOD).getValue()).append(",") //token 5
////            if (method.equals(LIST_TARGET_METHOD[0]))
////                sb.append(LIST_TARGET_METHOD_INTERNAL[0]).append(",");
////            else if (method.equals(LIST_TARGET_METHOD[1]))
////                sb.append(LIST_TARGET_METHOD_INTERNAL[1]).append(",");
////            else
////                sb.append(LIST_TARGET_METHOD_INTERNAL[2]).append(",");
////todo remove internal for target method
//              //.append(getCell(row, COLUMN_STOP_METHOD).getValue()).append(",") //token 6
//              //.append(getCell(row, COLUMN_ATR)).append(",") //token 7
//              //.append(getCell(row, COLUMN_SWP)).append(",") //token 8
//              .append(getCell(row, COLUMN_STOP_PRICE)).append(",") //token 9
//              .append(getCell(row, COLUMN_CURRENT_PRICE)).append(",") //token 10
//              //.append(getCell(row, COLUMN_ATR_MULTIPLIER)).append(",") //token 11
//              //.append(getCell(row, COLUMN_NOTES)).append("\n"); //token 12
//            ;
//        }
//        pw.println(sb.toString());
//        pw.flush();
//        pw.close();
//    }
