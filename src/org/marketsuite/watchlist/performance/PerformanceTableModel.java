package org.marketsuite.watchlist.performance;

import org.marketsuite.component.table.ColumnTypeEnum;
import org.marketsuite.component.table.DynaTableModel;
import org.marketsuite.component.table.SimpleCell;
import org.marketsuite.component.util.ObjectCloner;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.model.type.PerfTimeframe;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MainModel;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;
import org.marketsuite.component.table.ColumnTypeEnum;
import org.marketsuite.component.table.DynaTableModel;
import org.marketsuite.component.table.SimpleCell;
import org.marketsuite.component.util.ObjectCloner;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.model.type.PerfTimeframe;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MainModel;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class PerformanceTableModel extends DynaTableModel {
    public PerformanceTableModel() {
        remodel(DynaTableModel.generateSchema(TABLE_SCHEMA));
    }

    //----- interface/override -----
    public void populate() {}
    public void populate(WatchListModel wlm, boolean baseline_mode) {
        populate(wlm, wlm.getMembers(), baseline_mode);
    }
    //a subset of symbols from the watch list
    public void populate(WatchListModel wlm, ArrayList<String> symbols, boolean baseline_mode) {
        //for baseline mode, load baseline symbol quotes
        String baseline_symbol = wlm.getBaselineSymbol();
        MarketInfo mki = wlm.getMarketInfo(baseline_symbol);
        FundData baseline_fund;
        if (mki == null) {//read quotes if not
            try {
                baseline_fund = DataUtil.readFundHistory(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, baseline_symbol,
                        FrameworkConstants.MARKET_QUOTE_LENGTH + 2);//2 extra line for comments in file
            } catch (IOException e) {
                e.printStackTrace();
//TODO warn user
                return;
            }
        }
        else
            baseline_fund = mki.getFund();

        //initialize cells
        _lstRows.clear();
        ArrayList<LogMessage> not_included = new ArrayList<>();//holds error messages
        for (String symbol : symbols) {
            SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
            for (int col=0; col<TABLE_SCHEMA.length; col++) {//initialize all cells
                switch (col) {
                    case COLUMN_SYMBOL: break;
                    default: cells[col] = new SimpleCell(new Double(0)); break;
                }
            }
            cells[COLUMN_SYMBOL] = new SimpleCell(symbol);
            mki = wlm.getMarketInfo(symbol);
            if (mki == null) {
                LogMessage lm = new LogMessage(LoggingSource.WATCHLIST_MGR, symbol + ApolloConstants.APOLLO_BUNDLE.getString("rm_91"), null);
                not_included.add(lm);
                continue;
            }
            FundData fund = mki.getFund();
            FundQuote end_quote = fund.getQuote().get(_nEndIndex);
            cells[COLUMN_PRICE] = new SimpleCell(new Double(end_quote.getClose()));//most recent price after move
            Calendar end_cal = AppUtil.findRecentQuoteDate(fund, AppUtil.stringToCalendarNoEx(end_quote.getDate()));//from end index
//TODO replace calcBeginTime w AppUtil.moveCalendar....
            //calculate each cell's performance based on each time frame
            Calendar begin_cal = calcBeginTime(fund, end_cal, COLUMN_1_WEEK_PCT);
            if (begin_cal != null) //possible not enough data then begin_cal is null
                cells[COLUMN_1_WEEK_PCT] = new SimpleCell(calcPercentReturn(fund, begin_cal, end_cal, baseline_mode, baseline_fund));
            begin_cal = calcBeginTime(fund, end_cal, COLUMN_2_WEEK_PCT);
            if (begin_cal != null)
                cells[COLUMN_2_WEEK_PCT] = new SimpleCell(calcPercentReturn(fund, begin_cal, end_cal, baseline_mode, baseline_fund));
            begin_cal = calcBeginTime(fund, end_cal, COLUMN_1_MONTH_PCT);
            if (begin_cal != null)
                cells[COLUMN_1_MONTH_PCT] = new SimpleCell(calcPercentReturn(fund, begin_cal, end_cal, baseline_mode, baseline_fund));
            begin_cal = calcBeginTime(fund, end_cal, COLUMN_2_MONTH_PCT);
            if (begin_cal != null)
                cells[COLUMN_2_MONTH_PCT] = new SimpleCell(calcPercentReturn(fund, begin_cal, end_cal, baseline_mode, baseline_fund));
            begin_cal = calcBeginTime(fund, end_cal, COLUMN_3_MONTH_PCT);
            if (begin_cal != null)
                cells[COLUMN_3_MONTH_PCT] = new SimpleCell(calcPercentReturn(fund, begin_cal, end_cal, baseline_mode, baseline_fund));
            begin_cal = calcBeginTime(fund, end_cal, COLUMN_6_MONTH_PCT);
            if (begin_cal != null)
                cells[COLUMN_6_MONTH_PCT] = new SimpleCell(calcPercentReturn(fund, begin_cal, end_cal, baseline_mode, baseline_fund));
            begin_cal = calcBeginTime(fund, end_cal, COLUMN_1_YEAR_PCT);
            if (begin_cal != null)
                cells[COLUMN_1_YEAR_PCT] = new SimpleCell(calcPercentReturn(fund, begin_cal, end_cal, baseline_mode, baseline_fund));
            begin_cal = calcBeginTime(fund, end_cal, COLUMN_YTD_PCT);
            if (begin_cal != null)
                cells[COLUMN_YTD_PCT] = new SimpleCell(calcPercentReturn(fund, begin_cal, end_cal, baseline_mode, baseline_fund));
            _lstRows.add(cells);
        }
        fireTableDataChanged();
        if (not_included.size() > 0)
            Props.Log.setValue(null, not_included);
    }
    public boolean isCellEditable(int row, int col) { return false/*col == COLUMN_ENABLED*/; }

    //----- public/protected methods -----
    //find a symbol and return row index, return -1 if not found
    public int findSymbol(String symbol) {
        for (int row = 0; row < getRowCount(); row++) {
            String sym = (String)getCell(row, COLUMN_SYMBOL).getValue();
            if (sym.equals(symbol))
                return row;
        }
        return -1;
    }
    //get current list of symbols
    public ArrayList<String> getSymbols() {
        ArrayList<String> ret = new ArrayList<>();
        for (int row = 0; row < _lstRows.size(); row++) {
            String sym = (String)getCell(row, COLUMN_SYMBOL).getValue();
            ret.add(sym);
        }

        return ret;
    }
    //get sorted symbols for the model column(not view column since user may have dragged around)
    public ArrayList<String> getSortedSymbols(int model_column) {
        ArrayList<String> ret = new ArrayList<>();
        ArrayList<Integer> model_rows = getRankingMap().get(new Integer(model_column));
        for (int idx = 0; idx < model_rows.size(); idx++) {
            int model_row = model_rows.get(idx);
            ret.add((String) getCell(model_row, COLUMN_SYMBOL).getValue());
        }
        return ret;
    }
    public static PerfTimeframe timeCodeToEnum(int time_code) {
        switch (time_code) {
            case COLUMN_1_WEEK_PCT: return PerfTimeframe.ONE_WEEK;
            case COLUMN_2_WEEK_PCT: return PerfTimeframe.TWO_WEEK;
            case COLUMN_1_MONTH_PCT: return PerfTimeframe.ONE_MONTH;
            case COLUMN_2_MONTH_PCT: return PerfTimeframe.TWO_MONTH;
            case COLUMN_3_MONTH_PCT: return PerfTimeframe.THREE_MONTH;
            case COLUMN_6_MONTH_PCT: return PerfTimeframe.SIX_MONTH;
            case COLUMN_1_YEAR_PCT: return PerfTimeframe.ONE_YEAR;
            case COLUMN_YTD_PCT: return PerfTimeframe.YEAR_TO_DATE;
        }
        return PerfTimeframe.ONE_MONTH;//default
    }
    //time_code: from COLUMN_1_WEEK_PCT to COLUMN_YTD_PCT
    public static Calendar calcBeginTime(FundData fund, Calendar end_cal, int time_code) {
        Calendar begin_cal = (Calendar) ObjectCloner.copy(end_cal);
        switch (time_code) {
            case COLUMN_1_WEEK_PCT:
                begin_cal.add(Calendar.WEEK_OF_YEAR, -1);
                break;

            case COLUMN_2_WEEK_PCT:
                begin_cal.add(Calendar.WEEK_OF_YEAR, -2);
                break;

            case COLUMN_1_MONTH_PCT:
                begin_cal.add(Calendar.MONTH, -1);
                break;

            case COLUMN_2_MONTH_PCT:
                begin_cal.add(Calendar.MONTH, -2);
                break;

            case COLUMN_3_MONTH_PCT:
                begin_cal.add(Calendar.MONTH, -3);
                break;

            case COLUMN_6_MONTH_PCT:
                begin_cal.add(Calendar.MONTH, -6);
                break;

            case COLUMN_1_YEAR_PCT:
                begin_cal.add(Calendar.YEAR, -1);
                break;

            case COLUMN_YTD_PCT:
                FundQuote first_quote = AppUtil.findFirstQuoteInYear(fund, end_cal.get(Calendar.YEAR));
                return AppUtil.stringToCalendarNoEx(first_quote.getDate());

            default:
                ArrayList<FundQuote> quotes = fund.getQuote();
                begin_cal = AppUtil.stringToCalendarNoEx(quotes.get(quotes.size() - 1).getDate());
                return begin_cal;
        }
        return AppUtil.findRecentQuoteDate(fund, begin_cal);
    }
    void showHideSymbol(String symbol, boolean show) {
        if (show) {
            addRow(symbol);
        }
        else {
            //find the row and delete it
            for (int row = 0; row < getRowCount(); row++) {
                String sym = (String)getCell(row, COLUMN_SYMBOL).getValue();
                if (sym.equals(symbol)) {
                    _lstRows.remove(row);
                    fireTableDataChanged();
                    return;
                }
            }
        }
    }

    //----- private method -----
    //add a symbol to the table based on existing WatchlistModel in MainModel, null = no error, otherwise = log message
    private LogMessage addRow(String symbol) {
        //initialize cells
        WatchListModel wlm = MainModel.getInstance().getWatchListModel();
        SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
        for (int col=0; col<TABLE_SCHEMA.length; col++) {//initialize all cells
            switch (col) {
//                case COLUMN_ENABLED: cells[col] = new SimpleCell(true);break;
                case COLUMN_SYMBOL:
                default: cells[col] = new SimpleCell(new Double(0)); break;
            }
        }

        //update each cell
        cells[COLUMN_SYMBOL] = new SimpleCell(symbol);
        MarketInfo mki = wlm.getMarketInfo(symbol);
        if (mki == null) {
            LogMessage lm = new LogMessage(LoggingSource.WATCHLIST_MGR, symbol + ApolloConstants.APOLLO_BUNDLE.getString("rm_91"), null);
            return lm;
        }
        FundData fund = mki.getFund();
        cells[COLUMN_PRICE] = new SimpleCell(new Double(fund.getQuote().get(0).getClose()));//most recent price
        Calendar end_cal = AppUtil.findRecentQuoteDate(fund, Calendar.getInstance());

        //calculate each cell's performance based on each time frame
        Calendar begin_cal = calcBeginTime(fund, end_cal, COLUMN_1_WEEK_PCT);
        if (begin_cal != null) //possible not enough data then begin_cal is null
            cells[COLUMN_1_WEEK_PCT] = new SimpleCell(calcPercentReturn(fund, begin_cal, end_cal, false, null));
        begin_cal = calcBeginTime(fund, end_cal, COLUMN_2_WEEK_PCT);
        if (begin_cal != null)
            cells[COLUMN_2_WEEK_PCT] = new SimpleCell(calcPercentReturn(fund, begin_cal, end_cal, false, null));
        begin_cal = calcBeginTime(fund, end_cal, COLUMN_1_MONTH_PCT);
        if (begin_cal != null)
            cells[COLUMN_1_MONTH_PCT] = new SimpleCell(calcPercentReturn(fund, begin_cal, end_cal, false, null));
        begin_cal = calcBeginTime(fund, end_cal, COLUMN_2_MONTH_PCT);
        if (begin_cal != null)
            cells[COLUMN_2_MONTH_PCT] = new SimpleCell(calcPercentReturn(fund, begin_cal, end_cal, false, null));
        begin_cal = calcBeginTime(fund, end_cal, COLUMN_3_MONTH_PCT);
        if (begin_cal != null)
            cells[COLUMN_3_MONTH_PCT] = new SimpleCell(calcPercentReturn(fund, begin_cal, end_cal, false, null));
        begin_cal = calcBeginTime(fund, end_cal, COLUMN_6_MONTH_PCT);
        if (begin_cal != null)
            cells[COLUMN_6_MONTH_PCT] = new SimpleCell(calcPercentReturn(fund, begin_cal, end_cal, false, null));
        begin_cal = calcBeginTime(fund, end_cal, COLUMN_1_YEAR_PCT);
        if (begin_cal != null)
            cells[COLUMN_1_YEAR_PCT] = new SimpleCell(calcPercentReturn(fund, begin_cal, end_cal, false, null));
        begin_cal = calcBeginTime(fund, end_cal, COLUMN_YTD_PCT);
        if (begin_cal != null)
            cells[COLUMN_YTD_PCT] = new SimpleCell(calcPercentReturn(fund, begin_cal, end_cal, false, null));
        _lstRows.add(cells);
        fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
        return null;
    }
    private Double calcPercentReturn(FundData fund, Calendar begin_cal, Calendar end_cal, boolean baseline_mode, FundData baseline_fund) {
        if (baseline_mode)
            return new Double(AppUtil.calcBaselineReturn(fund, begin_cal, end_cal, baseline_fund));
        else
            return new Double(AppUtil.calcReturn(fund, begin_cal, end_cal));
    }
//TODO move to DynaTableModel level
    private SimpleCell[] initCells() {
        SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
        for (int col=0; col<TABLE_SCHEMA.length; col++) {
            int type = (Integer)TABLE_SCHEMA[col][1];
            if (type == ColumnTypeEnum.TYPE_STRING)
                cells[col] = new SimpleCell("");
            else if (type == ColumnTypeEnum.TYPE_DOUBLE)
                cells[col] = new SimpleCell(new Double(0));
        }
        return cells;
    }

    //----- accessor -----
    public void setEndIndex(int index) { _nEndIndex = index; }
    public int getEndIndex() { return _nEndIndex; }

    //----- variables -----
    private int _nEndIndex = 0;//quote index for calculating performance, maybe moved by unit time frame

    //----- literals -----
            static final int COLUMN_SYMBOL = 0;
            static final int COLUMN_PRICE = 1;
    public  static final int COLUMN_1_WEEK_PCT = 2;
    public  static final int COLUMN_2_WEEK_PCT = 3;
    public  static final int COLUMN_1_MONTH_PCT = 4;
    public  static final int COLUMN_2_MONTH_PCT = 5;
    public  static final int COLUMN_3_MONTH_PCT = 6;
    public  static final int COLUMN_6_MONTH_PCT = 7;
    public  static final int COLUMN_1_YEAR_PCT = 8;
    public  static final int COLUMN_YTD_PCT = 9;
    public  static final int COLUMN_CUSTOM_PCT = 10;
    private static final Object[][] TABLE_SCHEMA = {
        {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_1"), ColumnTypeEnum.TYPE_STRING, -1, 30, null, null, null},//symbol
        {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_2"), ColumnTypeEnum.TYPE_DOUBLE, -1, 35, null, null, null},//price
        {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_3"), ColumnTypeEnum.TYPE_DOUBLE, -1, 50, null, null, null},//1 week
        {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_4"), ColumnTypeEnum.TYPE_DOUBLE, -1, 50, null, null, null},//2 week
        {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_5"), ColumnTypeEnum.TYPE_DOUBLE, -1, 50, null, null, null},//1 month
        {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_6"), ColumnTypeEnum.TYPE_DOUBLE, -1, 50, null, null, null},//2 month
        {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_7"), ColumnTypeEnum.TYPE_DOUBLE, -1, 50, null, null, null},//3 month
        {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_8"), ColumnTypeEnum.TYPE_DOUBLE, -1, 50, null, null, null},//6 month
        {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_9"), ColumnTypeEnum.TYPE_DOUBLE, -1, 50, null, null, null},//12 month
        {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_10"), ColumnTypeEnum.TYPE_DOUBLE,-1, 40, null, null, null},//YTD
        {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_36"), ColumnTypeEnum.TYPE_DOUBLE,-1, 40, null, null, null},//Custom
    };
}