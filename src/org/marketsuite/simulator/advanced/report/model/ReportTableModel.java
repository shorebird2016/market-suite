package org.marketsuite.simulator.advanced.report.model;

import org.marketsuite.component.table.ColumnTypeEnum;
import org.marketsuite.component.table.DynaTableModel;
import org.marketsuite.component.table.SimpleCell;
import org.marketsuite.framework.model.Stat;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.strategy.base.SimReport;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.component.table.ColumnTypeEnum;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.resource.ApolloConstants;

/**
 * A wide table including all analysis results for comparing strategies.
 */
public class ReportTableModel extends DynaTableModel {
    public ReportTableModel() {
        remodel(DynaTableModel.generateSchema(TABLE_SCHEMA));
    }

    public void populate() {}

    public boolean isCellEditable(int row, int col) { return false; }

    public void clear() {
        _lstRows.clear();
        fireTableDataChanged();
    }

    //add a new row to end of table
    public void addRow(String symbol, String strategy, String strategy_detail, SimReport report, String desired_start_date) {
        SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
        cells[COLUMN_SYMBOL] = new SimpleCell(symbol);
        cells[COLUMN_STRATEGY] = new SimpleCell(strategy != null ? strategy : "");
        cells[COLUMN_CAGR] = new SimpleCell(new Double(report.getCagr()));
        cells[COLUMN_ROI] = new SimpleCell(new Double(report.getTotalReturn()));
        cells[COLUMN_ANNUAL] = new SimpleCell(new Double(report.getAverageReturn()));
        cells[COLUMN_TRADES] = new SimpleCell(new Long(report.getNumberTrades()));
        cells[COLUMN_TRADE_PER_YEAR] = new SimpleCell(new Double(report.getTradesPerYear()));
        cells[COLUMN_WIN_RATIO] = new SimpleCell(new Double(report.getWinRatio()));
        float pf = report.getProfitFactor();
        if (pf >= 0)
            cells[COLUMN_PF] = new SimpleCell(FrameworkConstants.PRICE_FORMAT.format((pf)));
        else
            cells[COLUMN_PF] = new SimpleCell("");
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
        cells[COLUMN_INFO] = new SimpleCell("[" + desired_start_date + "] " + strategy_detail);
        _lstRows.add(cells);
        fireTableDataChanged();
    }

    //column index
    public static final int COLUMN_SYMBOL = 0;
    public static final int COLUMN_STRATEGY = 1;
    public static final int COLUMN_CAGR = 2;
    public static final int COLUMN_ROI = 3;
    public static final int COLUMN_ANNUAL = 4;
    public static final int COLUMN_TRADES = 5;
    public static final int COLUMN_TRADE_PER_YEAR = 6;
    public static final int COLUMN_WIN_RATIO = 7;
    public static final int COLUMN_PF = 8;
    public static final int COLUMN_IN_MKT = 9;
    public static final int COLUMN_END_EQUITY = 10;
    public static final int COLUMN_NET_GAIN = 11;
    public static final int COLUMN_NET_LOSS = 12;

    public static final int COLUMN_AVG_GAIN_PCT = 13;
    public static final int COLUMN_AVG_LOSS_PCT = 14;
    public static final int COLUMN_AVG_DD_PCT = 15;
    public static final int COLUMN_MAX_GAIN_PCT = 16;
    public static final int COLUMN_MAX_LOSS_PCT = 17;
    public static final int COLUMN_MAX_DD_PCT = 18;
    public static final int COLUMN_MIN_GAIN_PCT = 19;
    public static final int COLUMN_MIN_LOSS_PCT = 20;
    public static final int COLUMN_MIN_DD_PCT = 21;
    public static final int COLUMN_AVG_GAIN = 22;
    public static final int COLUMN_AVG_LOSS = 23;
    public static final int COLUMN_AVG_DD = 24;
    public static final int COLUMN_MAX_GAIN = 25;
    public static final int COLUMN_MAX_LOSS = 26;
    public static final int COLUMN_MAX_DD = 27;
    public static final int COLUMN_MIN_GAIN = 28;
    public static final int COLUMN_MIN_LOSS = 29;
    public static final int COLUMN_MIN_DD = 30;
    public static final int COLUMN_INFO = 31;

    public static final Object[][] TABLE_SCHEMA = {
        //heading, type, visible, hide-able, alignment, width, editor, renderer, comparator
        { ApolloConstants.APOLLO_BUNDLE.getString("runrpt_col_31"), ColumnTypeEnum.TYPE_STRING, 1, 50, null, null, null },//symbol
        { ApolloConstants.APOLLO_BUNDLE.getString("cmn_strategy"), ColumnTypeEnum.TYPE_STRING,  1, 80, null, null, null },//strategy
        { ApolloConstants.APOLLO_BUNDLE.getString("runrpt_col_1"), ColumnTypeEnum.TYPE_DOUBLE, -1, 70, null, null, null },//CAGR
        { ApolloConstants.APOLLO_BUNDLE.getString("runrpt_col_2"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null },//ROI
        { ApolloConstants.APOLLO_BUNDLE.getString("runrpt_col_3"), ColumnTypeEnum.TYPE_DOUBLE, -1, 70, null, null, null },//Annual Return
        { ApolloConstants.APOLLO_BUNDLE.getString("runrpt_col_4"), ColumnTypeEnum.TYPE_LONG,   -1, 70, null, null, null },//Trades
        { ApolloConstants.APOLLO_BUNDLE.getString("runrpt_col_30"), ColumnTypeEnum.TYPE_DOUBLE,-1, 70, null, null, null },//Trades per year
        { ApolloConstants.APOLLO_BUNDLE.getString("runrpt_col_5"), ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null },//win ratio %
        { ApolloConstants.APOLLO_BUNDLE.getString("runrpt_col_6"), ColumnTypeEnum.TYPE_STRING, -1, 50, null, null, null },//profit factor
        { ApolloConstants.APOLLO_BUNDLE.getString("runrpt_col_7"), ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null },//time in market %
        { ApolloConstants.APOLLO_BUNDLE.getString("runrpt_col_8"), ColumnTypeEnum.TYPE_DOUBLE, -1, 90, null, null, null },//end equity
        { ApolloConstants.APOLLO_BUNDLE.getString("runrpt_col_9"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null },//net gain $
        { ApolloConstants.APOLLO_BUNDLE.getString("runrpt_col_10"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null },//net loss $
        { ApolloConstants.APOLLO_BUNDLE.getString("runrpt_col_11"), ColumnTypeEnum.TYPE_DOUBLE, -1, 70, null, null, null },//average gain %
        { ApolloConstants.APOLLO_BUNDLE.getString("runrpt_col_12"), ColumnTypeEnum.TYPE_DOUBLE, -1, 70, null, null, null },//average loss %
        { ApolloConstants.APOLLO_BUNDLE.getString("runrpt_col_13"), ColumnTypeEnum.TYPE_DOUBLE, -1, 70, null, null, null },//average drawdown %
        { ApolloConstants.APOLLO_BUNDLE.getString("runrpt_col_14"), ColumnTypeEnum.TYPE_DOUBLE, -1, 90, null, null, null },//max gain %
        { ApolloConstants.APOLLO_BUNDLE.getString("runrpt_col_15"), ColumnTypeEnum.TYPE_DOUBLE, -1, 90, null, null, null },//max loss %
        { ApolloConstants.APOLLO_BUNDLE.getString("runrpt_col_16"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null },//max drawdown %
        { ApolloConstants.APOLLO_BUNDLE.getString("runrpt_col_17"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null },//min gain %
        { ApolloConstants.APOLLO_BUNDLE.getString("runrpt_col_18"), ColumnTypeEnum.TYPE_DOUBLE, -1, 90, null, null, null },//min loss %
        { ApolloConstants.APOLLO_BUNDLE.getString("runrpt_col_19"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null },//min drawdown %
        { ApolloConstants.APOLLO_BUNDLE.getString("runrpt_col_20"), ColumnTypeEnum.TYPE_DOUBLE, -1, 90, null, null, null },//average gain $
        { ApolloConstants.APOLLO_BUNDLE.getString("runrpt_col_21"), ColumnTypeEnum.TYPE_DOUBLE, -1, 90, null, null, null },//average loss $
        { ApolloConstants.APOLLO_BUNDLE.getString("runrpt_col_22"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null },//average drawdown $
        { ApolloConstants.APOLLO_BUNDLE.getString("runrpt_col_23"), ColumnTypeEnum.TYPE_DOUBLE, -1, 90, null, null, null },//max gain $
        { ApolloConstants.APOLLO_BUNDLE.getString("runrpt_col_24"), ColumnTypeEnum.TYPE_DOUBLE, -1, 90, null, null, null },//max loss $
        { ApolloConstants.APOLLO_BUNDLE.getString("runrpt_col_25"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null },//max drawdown $
        { ApolloConstants.APOLLO_BUNDLE.getString("runrpt_col_26"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null },//min gain $
        { ApolloConstants.APOLLO_BUNDLE.getString("runrpt_col_27"), ColumnTypeEnum.TYPE_DOUBLE, -1, 90, null, null, null },//min loss $
        { ApolloConstants.APOLLO_BUNDLE.getString("runrpt_col_28"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null },//min drawdown $
        { ApolloConstants.APOLLO_BUNDLE.getString("runrpt_col_29"), ColumnTypeEnum.TYPE_STRING, 0, 250, null, null, null },//strategy description
    };
}
