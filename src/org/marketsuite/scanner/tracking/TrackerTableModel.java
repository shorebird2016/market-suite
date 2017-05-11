package org.marketsuite.scanner.tracking;

import org.marketsuite.component.table.ColumnTypeEnum;
import org.marketsuite.component.table.DynaTableModel;
import org.marketsuite.component.table.SimpleCell;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.Divergence;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.IndicatorUtil;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.component.table.ColumnTypeEnum;
import org.marketsuite.component.table.DynaTableModel;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.resource.ApolloConstants;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TrackerTableModel extends DynaTableModel {
    public TrackerTableModel() {
        remodel(DynaTableModel.generateSchema(TABLE_SCHEMA));
    }

    public void populate() {}

    public ArrayList<String> populate(ArrayList<MarketInfo> mkis, Date start_date, ArrayList<String> group_names, TrackerOption options) {
        _lstRows.clear();
        Calendar start_cal = Calendar.getInstance();
        start_cal.setTime(start_date);
        ArrayList<String> msgs = new ArrayList<String>();//holds error messages
        for (int idx = 0; idx < mkis.size(); idx++) {
            MarketInfo mki = mkis.get(idx);

            SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
//TODO convert into framework initCells() based on types
            for (int col=0; col<TABLE_SCHEMA.length; col++) {//initialize all cells
                cells[col] = new SimpleCell("");
                if (col == COLUMN_POST_50x120 || col == COLUMN_PRE_10x30 || col == COLUMN_PRE_50x120)
                    cells[col] = new SimpleCell(false);
            }
            String symbol = mki.getSymbol();
            cells[COLUMN_SYMBOL] = new SimpleCell(symbol);
            cells[COLUMN_PHASE] = new SimpleCell(mki.getCurrentPhase());

            //may have divergence dates
            Divergence dvg = mki.getDvg();
            if (dvg != null) {
                cells[COLUMN_DVG_START] = new SimpleCell(dvg.getStartDate());
                cells[COLUMN_DVG_END] = new SimpleCell(dvg.getEndDate());
            }
            if (isPre10x30InRange(mki, options))
                cells[COLUMN_PRE_10x30] = new SimpleCell(true);

            //may have 10x30 date
            ArrayList<String> cod0 = mki.getCrossOver10x30Dates();
            if (cod0.size() > 0) {//filter out older crossings
                String date0 = cod0.get(0);//only show the first 10x30 cross date
                try {//if 10x30 happened before start date, skip
                    Calendar cal10x30 = AppUtil.stringToCalendar(date0);
                    if (cal10x30.compareTo(start_cal) >= 0) {
                        cells[COLUMN_10x30] = new SimpleCell(date0);

                        //with 10x30 nearby, watch pre-50x120 DSTO and delta
                        if (isPre50x120InRange(mki, options))
                            cells[COLUMN_PRE_50x120] = new SimpleCell(true);
                    }
                } catch (ParseException e) {
                    msgs.add(symbol + " causes Exception " + e.getMessage());
                    e.printStackTrace();
                }
            }

            //50x120 date
            ArrayList<String> cod1 = mki.getCrossOver50x120Dates();
            if (cod1.size() > 0) {
                String date1 = cod1.get(0);
                try {//if 50x120 happened before start date, skip
                    Calendar cal50x120 = AppUtil.stringToCalendar(date1);
                    if (cal50x120.compareTo(start_cal) >= 0) {
                        cells[COLUMN_50x120] = new SimpleCell(date1);

                        //with 50x120 nearby, watch post-50x120 DSTO for entry
                        if (isDstoInRange(mki, options))
                            cells[COLUMN_POST_50x120] = new SimpleCell(true);
                    }
                } catch (ParseException pe) {
                    msgs.add(symbol + " causes Exception " + pe.getMessage());
                    pe.printStackTrace();
                }
            }

            //percentage near 10/30/50/200SMA
//            float close = mki.getFund().getQuote().get(0).getClose();
//            float ma10 = mki.getSma10()[0];
//            cells[COLUMN_NEAR_10SMA] = new SimpleCell(new Double((close - ma10) / ma10));
//            float ma30 = mki.getSma30()[0];
//            cells[COLUMN_NEAR_30SMA] = new SimpleCell(new Double((close - ma30) / ma30));
//            float ma50 = mki.getSma50d()[0];
//            cells[COLUMN_NEAR_50SMA] = new SimpleCell(new Double((close - ma50) / ma50));
//            float ma200 = mki.getSma200d()[0];
//            cells[COLUMN_NEAR_200SMA] = new SimpleCell(new Double((close - ma200) / ma200));
            cells[COLUMN_GROUP] = new SimpleCell(group_names.get(idx));
            _lstRows.add(cells);
        }
        fireTableDataChanged();
        return msgs;
    }

    public boolean isCellEditable(int row, int col) {
        return false;
    }

    //find a symbol and return row index, return -1 if not found
    public int findSymbol(String symbol) {
        for (int row = 0; row < getRowCount(); row++) {
            String sym = (String)getCell(row, COLUMN_SYMBOL).getValue();
            if (sym.equals(symbol))
                return row;
        }
        return -1;
    }

    //----- private method -----
    //check distance between 10MMA and 30MA within certain percentage
    //  also DSTO within 20-50 presently
    private boolean isPre10x30InRange(MarketInfo mki, TrackerOption options) {
        boolean dsto_ok = isDstoInRange(mki, options);
        float ma_10 = mki.getSma10()[0];
        float ma_30 = mki.getSma30()[0];
        float pct = (ma_30 - ma_10) / ma_10;
        if (ma_10 < ma_30 && Math.abs(pct) < options.getPercent10x30())
            return dsto_ok;
        return false;
    }

    //check distance between 50EMA and 120EMA within certain percentage
    //  also DSTO within 20-50 presently
    private boolean isPre50x120InRange(MarketInfo mki, TrackerOption options) {
        boolean dsto_ok = isDstoInRange(mki, options);
        float ema_50 = mki.getEma50()[0];
        float ema_120 = mki.getEma120()[0];
        float pct = (ema_120 - ema_50) / ema_50;
        if (ema_50 < ema_120 && Math.abs(pct) < options.getPercent50x120())
            return dsto_ok;
        return false;
    }

    //check if DSTO is within 20-50 presently TODO may adapt to other indicator
    private boolean isDstoInRange(MarketInfo mki, TrackerOption options) {
        float dsto = mki.getDsto()[0];
        if (dsto < options.getDstoHigh() && dsto > options.getDstoLow())
            return true;
        return false;
    }

    //----- variables -----

    //----- literals -----
    static final int COLUMN_SYMBOL = 0;
    static final int COLUMN_PHASE = 1;
    static final int COLUMN_DVG_START = 2;
    static final int COLUMN_DVG_END = 3;
    static final int COLUMN_PRE_10x30 = 4;
    static final int COLUMN_10x30 = 5;
    static final int COLUMN_PRE_50x120 = 6;
    static final int COLUMN_50x120 = 7;
    static final int COLUMN_POST_50x120 = 8;
//    static final int COLUMN_NEAR_10SMA = 9;
//    static final int COLUMN_NEAR_30SMA = 10;
//    static final int COLUMN_NEAR_50SMA = 11;
//    static final int COLUMN_NEAR_200SMA = 12;
    static final int COLUMN_GROUP = 9;
    private static final Object[][] TABLE_SCHEMA = {
        {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_1"),  ColumnTypeEnum.TYPE_STRING,   3, 70, null, null, null},//symbol
        {ApolloConstants.APOLLO_BUNDLE.getString("trk_lbl_2"), ColumnTypeEnum.TYPE_STRING,  -1, 90, null, null, null},//Phase
        {ApolloConstants.APOLLO_BUNDLE.getString("trk_lbl_3"), ColumnTypeEnum.TYPE_STRING,  -1, 90, null, null, null},//DVG start date
        {ApolloConstants.APOLLO_BUNDLE.getString("trk_lbl_4"), ColumnTypeEnum.TYPE_STRING,  -1, 90, null, null, null},//DVG end date
        {ApolloConstants.APOLLO_BUNDLE.getString("trk_lbl_5"), ColumnTypeEnum.TYPE_BOOLEAN, -1, 60, null, null, null},//Pre 10x30 cross
        {ApolloConstants.APOLLO_BUNDLE.getString("trk_lbl_6"), ColumnTypeEnum.TYPE_STRING,  -1, 90, null, null, null},//10x30 cross date
        {ApolloConstants.APOLLO_BUNDLE.getString("trk_lbl_7"), ColumnTypeEnum.TYPE_BOOLEAN, -1, 70, null, null, null},//pre 50x120 cross
        {ApolloConstants.APOLLO_BUNDLE.getString("trk_lbl_8"), ColumnTypeEnum.TYPE_STRING,  -1, 90, null, null, null},//50x120 cross date
        {ApolloConstants.APOLLO_BUNDLE.getString("trk_lbl_9"), ColumnTypeEnum.TYPE_BOOLEAN, -1, 80, null, null, null},//post 50x120 cross
//        {ApolloConstants.APOLLO_BUNDLE.getString("trk_lbl_11"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null},//near 10SMA
//        {ApolloConstants.APOLLO_BUNDLE.getString("trk_lbl_12"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null},//near 30SMA
//        {ApolloConstants.APOLLO_BUNDLE.getString("trk_lbl_13"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null},//near 50SMA
//        {ApolloConstants.APOLLO_BUNDLE.getString("trk_lbl_14"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null},//near 200SMA
        {ApolloConstants.APOLLO_BUNDLE.getString("trk_lbl_10"), ColumnTypeEnum.TYPE_STRING, -1, 150, null, null, null},//group name
    };
}