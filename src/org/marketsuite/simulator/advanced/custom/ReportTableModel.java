package org.marketsuite.simulator.advanced.custom;

import org.marketsuite.component.table.ColumnTypeEnum;
import org.marketsuite.component.table.DynaTableModel;
import org.marketsuite.component.table.SimpleCell;
import org.marketsuite.framework.model.Stat;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.strategy.base.SimReport;
import org.marketsuite.component.table.ColumnTypeEnum;
import org.marketsuite.framework.resource.FrameworkConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A wide table including all analysis results for comparing strategies.
 */
class ReportTableModel extends DynaTableModel {
    ReportTableModel() {
        remodel(DynaTableModel.generateSchema(TABLE_SCHEMA));
    }

    public void populate() {}

    public boolean isCellEditable(int row, int col) { return false; }

    //add a new row to end of table
    void addRow(String id, String symbol, String strategy, String note, SimReport report) {
        SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
        cells[COLUMN_ID] = new SimpleCell(id);
        cells[COLUMN_SYMBOL] = new SimpleCell(symbol);
        cells[COLUMN_STRATEGY] = new SimpleCell(strategy);
        cells[COLUMN_CAGR] = new SimpleCell(new Double(report.getCagr()));
        cells[COLUMN_ROI] = new SimpleCell(new Double(report.getTotalReturn()));
        cells[COLUMN_ANNUAL] = new SimpleCell(new Double(report.getAverageReturn()));
        cells[COLUMN_TRADES] = new SimpleCell(new Long(report.getNumberTrades()));
        cells[COLUMN_TRADE_PER_YEAR] = new SimpleCell(new Double(report.getTradesPerYear()));
        cells[COLUMN_WIN_RATIO] = new SimpleCell(new Double(report.getWinRatio()));
        cells[COLUMN_PF] = new SimpleCell(new Double(report.getProfitFactor()));
        cells[COLUMN_IN_MKT] = new SimpleCell(new Double(report.getTimeInMarket()));
        cells[COLUMN_END_EQUITY] = new SimpleCell(new Double(report.getEndEquity()));
        cells[COLUMN_NET_GAIN] = new SimpleCell(new Double(report.getTotalGain()));
        cells[COLUMN_NET_LOSS] = new SimpleCell(new Double(report.getTotalLoss()));
        Stat gain = report.getGain();
        Stat loss = report.getLoss();
        Stat dd = report.getDrawDown();
        cells[COLUMN_AVG_GAIN_PCT] = new SimpleCell(new Double(gain.getAvgPct()));
        cells[COLUMN_AVG_LOSS_PCT] = new SimpleCell(new Double(loss.getAvgPct()));
        cells[COLUMN_AVG_DD_PCT] = new SimpleCell(new Double(dd.getAvgPct()));
        cells[COLUMN_MAX_GAIN_PCT] = new SimpleCell(new Double(gain.getMaxPct()));
        cells[COLUMN_MAX_LOSS_PCT] = new SimpleCell(new Double(loss.getMaxPct()));
        cells[COLUMN_MAX_DD_PCT] = new SimpleCell(new Double(dd.getMaxPct()));
        cells[COLUMN_MIN_GAIN_PCT] = new SimpleCell(new Double(gain.getMinPct()));
        cells[COLUMN_MIN_LOSS_PCT] = new SimpleCell(new Double(loss.getMinPct()));
        cells[COLUMN_MIN_DD_PCT] = new SimpleCell(new Double(dd.getMinPct()));
        cells[COLUMN_AVG_GAIN] = new SimpleCell(new Double(gain.getAvgAmount()));
        cells[COLUMN_AVG_LOSS] = new SimpleCell(new Double(loss.getAvgAmount()));
        cells[COLUMN_AVG_DD] = new SimpleCell(new Double(dd.getAvgAmount()));
        cells[COLUMN_MAX_GAIN] = new SimpleCell(new Double(gain.getMaxAmount()));
        cells[COLUMN_MAX_LOSS] = new SimpleCell(new Double(loss.getMaxAmount()));
        cells[COLUMN_MAX_DD] = new SimpleCell(new Double(dd.getMaxAmount()));
        cells[COLUMN_MIN_GAIN] = new SimpleCell(new Double(gain.getMinAmount()));
        cells[COLUMN_MIN_LOSS] = new SimpleCell(new Double(loss.getMinAmount()));
        cells[COLUMN_MIN_DD] = new SimpleCell(new Double(dd.getMinAmount()));
        cells[COLUMN_INFO] = new SimpleCell(note != null ? note : "");
        _lstRows.add(cells);
    }

    //find a symbol and return row index, return -1 if not found
    int findSymbol(String symbol) {
        for (int row = 0; row < getRowCount(); row++) {
            String sym = (String)getCell(row, COLUMN_SYMBOL).getValue();
            if (sym.equals(symbol))
                return row;
        }
        return -1;
    }

    //compare MAC vs MACOSC2 performance, method1/2 like MAC, MACOSC2, MACOSC1..etc
    // return a fraction between 0 to 100 representing % of method1 better than method2
    double calcOutperformPct(String method1, String method2) {
        //first pass, find a list of unique symbols, put inside map
        HashMap<String, ArrayList<String>> symbol_map = new HashMap<String, ArrayList<String>>();
        ArrayList<String> indices = new ArrayList<String>();
        for (int row = 0; row < getRowCount(); row++) {
            String sym = (String)getCell(row, COLUMN_SYMBOL).getValue();
            String index = (String)getCell(row, COLUMN_ID).getValue();
            if (symbol_map.containsKey(sym)) {
                indices.add(index);
                continue;
            }

            //not found, add to map
            symbol_map.put(sym, indices = new ArrayList<String>());
            indices.add(index);
        }

        //for each unique symbol, look up CAGR associated with method1 and 2
        int outperform_count = 0;
        Iterator<String> itor = symbol_map.keySet().iterator();
        while (itor.hasNext()) {
            String symbol = itor.next();
            indices = symbol_map.get(symbol);
            if (indices.size() < 2)//only 1 index for this symbol
                continue;

            //convert to int, get both CAGRs
            int row1 = Integer.parseInt(indices.get(0)) - 1;
            int row2 = Integer.parseInt(indices.get(1)) - 1;
            String str1 = (String)getCell(row1, COLUMN_STRATEGY).getValue();
            String str2 = (String)getCell(row2, COLUMN_STRATEGY).getValue();
            double cagr1 = (Double)getCell(row1, COLUMN_CAGR).getValue();
            double cagr2 = (Double)getCell(row2, COLUMN_CAGR).getValue();
            if (str1.equals(method2) && str2.equals(method1)) {//swap
                double tmp = cagr1;
                cagr1 = cagr2;
                cagr2 = tmp;
            }
            if (cagr1 >= cagr2)
                outperform_count++;
        }
        return 2 * outperform_count / (double)getRowCount();
    }

//    String getCurReport() { return _sCurReport; }
//    void setCurReport(String rpt) { _sCurReport = rpt; }
//
//    //----- variables -----
//    private String _sCurReport;

    //----- literals -----
    //column index
    static final int COLUMN_ID = 0;
    static final int COLUMN_SYMBOL = 1;
    static final int COLUMN_STRATEGY = 2;
    static final int COLUMN_CAGR = 3;
    static final int COLUMN_ROI = 4;
    static final int COLUMN_ANNUAL = 5;
    static final int COLUMN_TRADES = 6;
    static final int COLUMN_TRADE_PER_YEAR = 7;
    static final int COLUMN_WIN_RATIO = 8;
    static final int COLUMN_PF = 9;
    static final int COLUMN_IN_MKT = 10;
    static final int COLUMN_END_EQUITY = 11;
    static final int COLUMN_NET_GAIN = 12;
    static final int COLUMN_NET_LOSS = 13;
    static final int COLUMN_AVG_GAIN_PCT = 14;
    static final int COLUMN_AVG_LOSS_PCT = 15;
    static final int COLUMN_AVG_DD_PCT = 16;
    static final int COLUMN_MAX_GAIN_PCT = 17;
    static final int COLUMN_MAX_LOSS_PCT = 18;
    static final int COLUMN_MAX_DD_PCT = 19;
    static final int COLUMN_MIN_GAIN_PCT = 20;
    static final int COLUMN_MIN_LOSS_PCT = 21;
    static final int COLUMN_MIN_DD_PCT = 22;
    static final int COLUMN_AVG_GAIN = 23;
    static final int COLUMN_AVG_LOSS = 24;
    static final int COLUMN_AVG_DD = 25;
    static final int COLUMN_MAX_GAIN = 26;
    static final int COLUMN_MAX_LOSS = 27;
    static final int COLUMN_MAX_DD = 28;
    static final int COLUMN_MIN_GAIN = 29;
    static final int COLUMN_MIN_LOSS = 30;
    static final int COLUMN_MIN_DD = 31;
    static final int COLUMN_INFO = 32;

    static final Object[][] TABLE_SCHEMA = {
        //heading, type, visible, hide-able, alignment, width, editor, renderer, comparator
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("hdr_0"), ColumnTypeEnum.TYPE_STRING, 1, 20, null, null, null },//ID - row count for now
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("hdr_01"), ColumnTypeEnum.TYPE_STRING, 1, 50, null, null, null },//symbol
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("strategy"), ColumnTypeEnum.TYPE_STRING, 1, 90, null, null, null },//strategy
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("hdr_1"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null },//CAGR
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("hdr_2"), ColumnTypeEnum.TYPE_DOUBLE, -1, 70, null, null, null },//ROI
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("hdr_3"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null },//Annual Return
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("hdr_4"), ColumnTypeEnum.TYPE_LONG, -1, 70, null, null, null },//Trades
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("hdr_30"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null },//Trades per year
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("hdr_5"), ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null },//win ratio %
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("hdr_6"), ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null },//profit factor
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("hdr_7"), ColumnTypeEnum.TYPE_DOUBLE, -1, 70, null, null, null },//time in market %
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("hdr_8"), ColumnTypeEnum.TYPE_DOUBLE, -1, 90, null, null, null },//end equity
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("hdr_9"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null },//net gain $
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("hdr_10"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null },//net loss $
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("hdr_11"), ColumnTypeEnum.TYPE_DOUBLE, -1, 90, null, null, null },//average gain %
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("hdr_12"), ColumnTypeEnum.TYPE_DOUBLE, -1, 90, null, null, null },//average loss %
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("hdr_13"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null },//average drawdown %
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("hdr_14"), ColumnTypeEnum.TYPE_DOUBLE, -1, 90, null, null, null },//max gain %
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("hdr_15"), ColumnTypeEnum.TYPE_DOUBLE, -1, 90, null, null, null },//max loss %
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("hdr_16"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null },//max drawdown %
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("hdr_17"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null },//min gain %
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("hdr_18"), ColumnTypeEnum.TYPE_DOUBLE, -1, 90, null, null, null },//min loss %
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("hdr_19"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null },//min drawdown %
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("hdr_20"), ColumnTypeEnum.TYPE_DOUBLE, -1, 90, null, null, null },//average gain $
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("hdr_21"), ColumnTypeEnum.TYPE_DOUBLE, -1, 90, null, null, null },//average loss $
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("hdr_22"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null },//average drawdown $
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("hdr_23"), ColumnTypeEnum.TYPE_DOUBLE, -1, 90, null, null, null },//max gain $
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("hdr_24"), ColumnTypeEnum.TYPE_DOUBLE, -1, 90, null, null, null },//max loss $
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("hdr_25"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null },//max drawdown $
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("hdr_26"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null },//min gain $
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("hdr_27"), ColumnTypeEnum.TYPE_DOUBLE, -1, 90, null, null, null },//min loss $
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("hdr_28"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null },//min drawdown $
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("hdr_29"), ColumnTypeEnum.TYPE_STRING, 0, 150, null, null, null },//strategy description
    };
}
