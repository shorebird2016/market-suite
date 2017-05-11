package org.marketsuite.gap;

import org.marketsuite.component.table.ColumnTypeEnum;
import org.marketsuite.component.table.DynaTableModel;
import org.marketsuite.component.table.SimpleCell;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.market.MarketUtil;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.data.CandleSignals;
import org.marketsuite.framework.model.indicator.BollingerBand;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.CandleUtil;
import org.marketsuite.main.MainModel;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;
import org.marketsuite.component.table.ColumnTypeEnum;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.market.MarketUtil;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.indicator.BollingerBand;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.main.MainModel;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;

import java.util.ArrayList;
import java.util.Calendar;

class GapStudyTableModel extends DynaTableModel {
    GapStudyTableModel() { super(DynaTableModel.generateSchema(TABLE_SCHEMA)); }
    public void populate() {}
    public void populate(WatchListModel wlm) {
        _lstRows.clear();
        ArrayList<String> members = wlm.getMembers();
        for (String symbol : members) {
            ArrayList<Calendar> earning_dates = MainModel.getInstance().getEarningDates(symbol);
            MarketInfo mki = wlm.getMarketInfo(symbol);
            SimpleCell[] cells = initCells();
            cells[COLUMN_SYMBOL].setValue(symbol);

            //find gap date from quotes
            if (mki == null) continue;
            FundData fund = mki.getFund();
            ArrayList<FundQuote> quotes = fund.getQuote();
            ArrayList<Integer> gaps_idx = CandleUtil.findGap(quotes, 60, true);
            int gap_index = -1; float gap_close = 0;//save this for later
            if (earning_dates == null) {
                int max_idx = -1;
                float max_pct = 0;
                for (int gap_idx : gaps_idx) {
                    float prev = quotes.get(gap_idx + 1).getClose();
                    float pct = (quotes.get(gap_idx).getClose() - prev) / prev;
                    if (pct > max_pct)  { max_pct = pct; max_idx = gap_idx; }
                }
                if (max_idx != -1) {
                    cells[COLUMN_ATGAP_DATE].setValue(quotes.get(max_idx).getDate());
                    gap_close = quotes.get(max_idx).getClose();
                    cells[COLUMN_ATGAP_PRICE].setValue(new Double(gap_close));
                    cells[COLUMN_ATGAP_PCT].setValue(new Double(max_pct));
                    gap_index = max_idx;
                }
            }
            else if (gaps_idx.size() > 0) {//several gaps, pick meaningful one, eg, biggest gap in Q2, closest to earning gap
                Calendar closest_cal = null; long delta = 60 * 24 * 60 * 60 * 1000L;//60 days
                for (int gap_idx : gaps_idx) {
                    String gs = quotes.get(gap_idx).getDate();
                    Calendar gd = AppUtil.stringToCalendarNoEx(gs); long gt = gd.getTimeInMillis();
                    for (Calendar ed : earning_dates) {
                        long et = ed.getTimeInMillis();
                        long dt = gt - et; if (dt < 0) dt = -dt;//make diff positive
                        if (dt < delta) {
                            delta = dt; closest_cal = gd;
                        }
                    }
                }
                if (closest_cal != null) {//found closest gap to earning
                    String closest_date = AppUtil.calendarToString(closest_cal);
                    cells[COLUMN_ATGAP_DATE].setValue(closest_date);
                    int closest_idx = fund.findIndexByDate(closest_date);
                    gap_close = quotes.get(closest_idx).getClose();
                    cells[COLUMN_ATGAP_PRICE].setValue(new Double(gap_close));
                    float prev = quotes.get(closest_idx + 1).getClose();
                    float pct = (gap_close - prev) / prev;
                    cells[COLUMN_ATGAP_PCT].setValue(new Double(pct));
                    gap_index = closest_idx;
                }
                else {//not found, use biggest gap
                    int max_idx = -1;
                    float max_pct = 0;
                    for (int gap_idx : gaps_idx) {
                        float prev = quotes.get(gap_idx + 1).getClose();
                        float pct = (quotes.get(gap_idx).getClose() - prev) / prev;
                        if (pct > max_pct)  { max_pct = pct; max_idx = gap_idx; }
                    }
                    if (max_idx != -1) {
                        cells[COLUMN_ATGAP_DATE].setValue(quotes.get(max_idx).getDate());
                        gap_close = quotes.get(max_idx).getClose();
                        cells[COLUMN_ATGAP_PRICE].setValue(new Double(gap_close));
                        cells[COLUMN_ATGAP_PCT].setValue(new Double(max_pct));
                        gap_index = max_idx;
                    }
                }

                //Bollinger Band
                BollingerBand bb = mki.getBollingerBand();//default 20/2
                float bb_upper = bb.getUpperBand()[gap_index];
                if (gap_close > bb_upper)
                    cells[COLUMN_ATGAP_BB].setValue("> 2x");
                bb = new BollingerBand(20, 3, 3, fund.getQuote()) ;//20/3
                bb_upper = bb.getUpperBand()[gap_index];
                if (gap_close > bb_upper)
                    cells[COLUMN_ATGAP_BB].setValue("> 3x");
            }
            String phase = MarketUtil.calcMarketPhase(mki, gap_index - 1);
            cells[COLUMN_PREGAP_PHASE].setValue(phase != null ? phase : "");
            cells[COLUMN_PREGAP_RATING].setValue(new Double(quotes.get(gap_index + 1).getClose()));//TODO temporary for exporting help

            //candle signals, copy array of 20 items behind gap
            ArrayList<FundQuote> candle_quotes = new ArrayList<>();
            for (int idx = gap_index + 1; idx < gap_index + 30; idx++)//extra 30 pass gap TODO may bomb here
                candle_quotes.add(quotes.get(idx));
            CandleSignals css = new CandleSignals(candle_quotes, 20);
            cells[COLUMN_PREGAP_CANDLE].setValue(css.getSignals(5));
            cells[COLUMN_ATGAP_PHASE].setValue(MarketUtil.calcMarketPhase(mki, gap_index));

            //post gap performance calculations use values in cell
            int idx = gap_index - 5;
            if (idx >= 0) {
                float c = quotes.get(idx).getClose();
                float pct = (c - gap_close) / gap_close;
                cells[COLUMN_POSTGAP_PERF_1WK].setValue(new Double(pct));
            }
            idx = gap_index - 20;
            if (idx >= 0) {
                float c = quotes.get(idx).getClose();
                float pct = (c - gap_close) / gap_close;
                cells[COLUMN_POSTGAP_PERF_4WK].setValue(new Double(pct));
            }
            idx = gap_index - 40;
            if (idx >= 0) {
                float c = quotes.get(idx).getClose();
                float pct = (c - gap_close) / gap_close;
                cells[COLUMN_POSTGAP_PERF_8WK].setValue(new Double(pct));
            }
            idx = gap_index - 60;
            if (idx >= 0) {
                float c = quotes.get(idx).getClose();
                float pct = (c - gap_close) / gap_close;
                cells[COLUMN_POSTGAP_PERF_12WK].setValue(new Double(pct));
            }

            //calc # from gap, normalized rate
            int num_days_from_gap = gap_index + 1;
            float c = quotes.get(0).getClose();
            float pct = 10000 * (c - gap_close) / gap_close;//make number larger enough to compare
            float norm = pct / num_days_from_gap;
            cells[COLUMN_POSTGAP_PERF_CURRENT_NORMALIZED].setValue(new Double(norm));
            cells[COLUMN_ROI_TO_DATE].setValue(new Double((c - gap_close) / gap_close));
            _lstRows.add(cells);
        }

        //use watch list model to populate
        fireTableDataChanged();
    }
    public boolean isCellEditable(int row, int column) { return false; }

    String getAvgPerf() {
        float avg_p1w = 0, avg_p4w = 0, avg_p6w = 0, avg_norm = 0;
        for (int row = 0; row < getRowCount(); row++) {
            avg_p1w += (Double)getCell(row, COLUMN_POSTGAP_PERF_1WK).getValue();
            avg_p4w += (Double)getCell(row, COLUMN_POSTGAP_PERF_4WK).getValue();
            avg_p6w += (Double)getCell(row, COLUMN_POSTGAP_PERF_8WK).getValue();
            avg_norm += (Double)getCell(row, COLUMN_POSTGAP_PERF_CURRENT_NORMALIZED).getValue();
        }
        avg_p1w /= getRowCount(); avg_p4w /= getRowCount();
        avg_p6w /= getRowCount(); avg_norm /= getRowCount();
        return FrameworkConstants.PCT2_FORMAT.format(avg_p1w) + "   " +
                FrameworkConstants.PCT2_FORMAT.format(avg_p4w) + "   " +
                FrameworkConstants.PCT2_FORMAT.format(avg_p6w) + "   " +
                FrameworkConstants.PRICE_FORMAT2.format(avg_norm);
    }

    //----- private methods -----
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

    //----- literals -----
    static final int COLUMN_SYMBOL = 0;
    static final int COLUMN_PREGAP_PHASE = 1;
    static final int COLUMN_PREGAP_CANDLE = 2;
    static final int COLUMN_PREGAP_RATING = 3;
    static final int COLUMN_PREGAP_TA = 4;
    static final int COLUMN_PREGAP_VSQ = 5;
    static final int COLUMN_ATGAP_DATE = 6;
    static final int COLUMN_ATGAP_PRICE = 7;
    static final int COLUMN_ATGAP_PCT = 8;
    static final int COLUMN_ATGAP_CANDLE = 9;
    static final int COLUMN_ATGAP_PHASE = 10;
    static final int COLUMN_ATGAP_BB = 11;
    static final int COLUMN_ATGAP_CLOUD = 12;
    static final int COLUMN_ATGAP_ICHIMOKU = 13;
    static final int COLUMN_ATGAP_LAG = 14;
    static final int COLUMN_ATGAP_MKT_CONDITION = 15;
    static final int COLUMN_ATGAP_IG_CONDITION = 16;
    static final int COLUMN_ATGAP_SECTOR_CONDITION = 17;
    static final int COLUMN_POSTGAP_PERF_1WK = 18;
    static final int COLUMN_POSTGAP_PERF_4WK = 19;
    static final int COLUMN_POSTGAP_PERF_8WK = 20;
    static final int COLUMN_POSTGAP_PERF_12WK = 21;
    static final int COLUMN_POSTGAP_PERF_CURRENT_NORMALIZED = 22;
    static final int COLUMN_ROI_TO_DATE = 23;
    static final Object[][] TABLE_SCHEMA = {
            {ApolloConstants.APOLLO_BUNDLE.getString("pw_sym"),     ColumnTypeEnum.TYPE_STRING, 1,  60, null, null, null},//=0
            {ApolloConstants.APOLLO_BUNDLE.getString("gps_preph"),  ColumnTypeEnum.TYPE_STRING, 0,  90, null, null, null},
            {ApolloConstants.APOLLO_BUNDLE.getString("gps_precdl"), ColumnTypeEnum.TYPE_STRING, -1, 150, null, null, null},
            {ApolloConstants.APOLLO_BUNDLE.getString("gps_prertg"), ColumnTypeEnum.TYPE_DOUBLE, 2,  100, null, null, null},
            {ApolloConstants.APOLLO_BUNDLE.getString("gps_preta"),  ColumnTypeEnum.TYPE_STRING, 0,  50, null, null, null},
            {ApolloConstants.APOLLO_BUNDLE.getString("gps_prevsq"), ColumnTypeEnum.TYPE_STRING, -1, 30, null, null, null},//=5
            {ApolloConstants.APOLLO_BUNDLE.getString("gps_atdate"), ColumnTypeEnum.TYPE_STRING, -1, 80, null, null, null},
            {ApolloConstants.APOLLO_BUNDLE.getString("gps_atprice"),ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null},
            {ApolloConstants.APOLLO_BUNDLE.getString("gps_atpct"),  ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null},
            {ApolloConstants.APOLLO_BUNDLE.getString("gps_atcdl"),  ColumnTypeEnum.TYPE_STRING, -1, 150, null, null, null},
            {ApolloConstants.APOLLO_BUNDLE.getString("gps_atphase"),ColumnTypeEnum.TYPE_STRING, -1, 90, null, null, null},//=10
            {ApolloConstants.APOLLO_BUNDLE.getString("gps_atbb"),   ColumnTypeEnum.TYPE_STRING, -1, 30, null, null, null},
            {ApolloConstants.APOLLO_BUNDLE.getString("gps_atcld"),  ColumnTypeEnum.TYPE_STRING, -1, 50, null, null, null},
            {ApolloConstants.APOLLO_BUNDLE.getString("gps_atich"),  ColumnTypeEnum.TYPE_STRING, -1, 50, null, null, null},
            {ApolloConstants.APOLLO_BUNDLE.getString("gps_atlag"),  ColumnTypeEnum.TYPE_STRING, -1, 50, null, null, null},
            {ApolloConstants.APOLLO_BUNDLE.getString("gps_atmkt"),   ColumnTypeEnum.TYPE_STRING, -1, 50, null, null, null},//=15
            {ApolloConstants.APOLLO_BUNDLE.getString("gps_atig"),    ColumnTypeEnum.TYPE_STRING, -1, 50, null, null, null},
            {ApolloConstants.APOLLO_BUNDLE.getString("gps_atsec"),   ColumnTypeEnum.TYPE_STRING, -1, 50, null, null, null},
            {ApolloConstants.APOLLO_BUNDLE.getString("gps_post1wk"), ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null},
            {ApolloConstants.APOLLO_BUNDLE.getString("gps_post4wk"), ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null},
            {ApolloConstants.APOLLO_BUNDLE.getString("gps_post8wk"), ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null},//=20
            {ApolloConstants.APOLLO_BUNDLE.getString("gps_post12wk"), ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null},
            {ApolloConstants.APOLLO_BUNDLE.getString("gps_postnorm"),ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null},
            {ApolloConstants.APOLLO_BUNDLE.getString("gps_roi_todate"),      ColumnTypeEnum.TYPE_DOUBLE, -1, 150, null, null, null},
    };
}
