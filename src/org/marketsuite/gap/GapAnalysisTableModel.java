package org.marketsuite.gap;

import org.marketsuite.component.table.ColumnTypeEnum;
import org.marketsuite.component.table.DynaTableModel;
import org.marketsuite.component.table.SimpleCell;
import org.marketsuite.framework.model.data.GapInfo;
import org.marketsuite.resource.ApolloConstants;
import javafx.scene.control.Cell;
import org.marketsuite.component.table.ColumnTypeEnum;
import org.marketsuite.component.table.DynaTableModel;
import org.marketsuite.framework.model.data.GapInfo;
import org.marketsuite.resource.ApolloConstants;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

class GapAnalysisTableModel extends DynaTableModel {
    GapAnalysisTableModel() { super(DynaTableModel.generateSchema(TABLE_SCHEMA)); }
    public void populate() { }
    public boolean isCellEditable(int row, int column) { return false; }
    void populate(GapFactor factor) { _lstRows.clear(); categorize(factor); calcRoi(); calcHighOdd(); }

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
    private void categorize(GapFactor factor) {
        _mapFactor = new HashMap<>();
        for (GapInfo gi : _GapInfos) {
            String key = null;
            switch (factor) {
                case PREGAP_PHASE: key = gi.getPrePhase(); break;
                case POSTGAP_PHASE: key = gi.getAtPhase(); break;
                case AT_GAP_ROI: categorizeRoi(gi, gi.getAtGapRoi(), "Gap ROI "); continue;
                case POSTGAP_TYPE: key = gi.getType().toString(); break;
                case POSTGAP_PULLBACK: categorizeRoi(gi, gi.getRoiLowestLow(), "Pullback "); continue;
                case AT_GAP_BB: key = gi.getBbMag().toString();
            }
            if (key == null) continue;
            ArrayList<GapInfo> gis = _mapFactor.get(key);
            if (gis == null) {
                gis = new ArrayList<>();
                _mapFactor.put(key, gis);
            }
            gis.add(gi);
        }
    }
    //helper to split percent into 4 categories
    private void categorizeRoi(GapInfo gi, float roi, String prefix) {
        if (roi < 0.05) {
            ArrayList<GapInfo> gis = _mapFactor.get(prefix + ROI_LT_5);
            if (gis == null) {
                gis = new ArrayList<>();
                _mapFactor.put(prefix + ROI_LT_5, gis);
            }
            gis.add(gi);
        }
        else if (roi < 0.1) {
            ArrayList<GapInfo> gis = _mapFactor.get(prefix + ROI_LT_10);
            if (gis == null) {
                gis = new ArrayList<>();
                _mapFactor.put(prefix + ROI_LT_10, gis);
            }
            gis.add(gi);
        }
        else if (roi < 0.15) {
            ArrayList<GapInfo> gis = _mapFactor.get(prefix + ROI_LT_15);
            if (gis == null) {
                gis = new ArrayList<>();
                _mapFactor.put(prefix + ROI_LT_15, gis);
            }
            gis.add(gi);
        }
        else {
            ArrayList<GapInfo> gis = _mapFactor.get(prefix + ROI_GT_15);
            if (gis == null) {
                gis = new ArrayList<>();
                _mapFactor.put(prefix + ROI_GT_15, gis);
            }
            gis.add(gi);
        }
    }
    //for each category, calculate average ROI for each period
    private void calcRoi() {
        Iterator<String> itor = _mapFactor.keySet().iterator();
        while (itor.hasNext()) {
            String key = itor.next();
            ArrayList<GapInfo> gis = _mapFactor.get(key);
            float roi1 =0, roi2 = 0, roi4 = 0, roi6 = 0, roi8 = 0, roi12 = 0;
            int cnt1 = 0, cnt2 = 0, cnt4 = 0, cnt6 = 0, cnt8 = 0, cnt12 = 0;
            int pos_cnt1 = 0, neg_cnt1 = 0;
            int pos_cnt2 = 0, neg_cnt2 = 0;
            int pos_cnt4 = 0, neg_cnt4 = 0;
            int pos_cnt6 = 0, neg_cnt6 = 0;
            int pos_cnt8 = 0, neg_cnt8 = 0;
            int pos_cnt12 = 0, neg_cnt12 = 0;
            for (GapInfo gi : gis) {//only sum ones with average
                float roi_1wk = gi.getRoi1wk(); if (roi_1wk != 0) {
                    roi1 += roi_1wk; cnt1++; if (roi_1wk >= 0) pos_cnt1++; else neg_cnt1++; }
                float roi_2wk = gi.getRoi2wk(); if (roi_2wk != 0) {
                    roi2 += roi_2wk; cnt2++; if (roi_2wk >= 0) pos_cnt2++; else neg_cnt2++; }
                float roi_4wk = gi.getRoi4wk(); if (roi_4wk != 0) {
                    roi4 += roi_4wk; cnt4++; if (roi_4wk >= 0) pos_cnt4++; else neg_cnt4++; }
                float roi_6wk = gi.getRoi6wk(); if (roi_6wk != 0) {
                    roi6 += roi_6wk; cnt6++; if (roi_6wk >= 0) pos_cnt6++; else neg_cnt6++; }
                float roi_8wk = gi.getRoi8wk(); if (roi_8wk != 0) {
                    roi8 += roi_8wk; cnt8++; if (roi_8wk >= 0) pos_cnt8++; else neg_cnt8++; }
                float roi_12wk = gi.getRoi12wk(); if (roi_12wk != 0) {
                    roi12 += roi_12wk; cnt12++; if (roi_12wk >= 0) pos_cnt12++; else neg_cnt12++; }
            }
            SimpleCell[] cells = initCells();
            cells[COLUMN_FACTOR].setValue(key);
            cells[COLUMN_POSTGAP_PERF_1WK].setValue(new CellInfo(roi1/cnt1, pos_cnt1, neg_cnt1));//new Double(roi1 / cnt1));
            cells[COLUMN_POSTGAP_PERF_2WK].setValue(new CellInfo(roi2/cnt2, pos_cnt2, neg_cnt2));//new Double(roi2 / cnt2));
            cells[COLUMN_POSTGAP_PERF_4WK].setValue(new CellInfo(roi4/cnt4, pos_cnt4, neg_cnt4));//new Double(roi4 / cnt4));
            cells[COLUMN_POSTGAP_PERF_6WK].setValue(new CellInfo(roi6/cnt6, pos_cnt6, neg_cnt6));//new Double(roi6 / cnt6));
            cells[COLUMN_POSTGAP_PERF_8WK].setValue(new CellInfo(roi8/cnt8, pos_cnt8, neg_cnt8));//new Double(roi8 / cnt8));
            cells[COLUMN_POSTGAP_PERF_12WK].setValue(new CellInfo(roi12/cnt12, pos_cnt12, neg_cnt12));//new Double(roi12 / cnt12));
            _lstRows.add(cells);
        }
        fireTableDataChanged();
    }
    //identify each column (time frame) highest odd (positive : negative) of success for highlighting
    private void calcHighOdd() {//use CellInfo stored inside each cell
        for (int col = COLUMN_POSTGAP_PERF_1WK; col <= COLUMN_POSTGAP_PERF_12WK; col++) {
            float highest_ratio = Float.MIN_VALUE; int highest_row = -1;
            for (int row = 0; row < getRowCount(); row++) {
                CellInfo ci = (CellInfo)getCell(row, col).getValue();
                int neg_cnt = ci.getNeagtiveCount();
                if (neg_cnt == 0)
                    continue;//can't div by 0, skip
                ci.oddsRatio = ci.getPositiveCount() / neg_cnt;
                if (ci.oddsRatio > highest_ratio) {
                    highest_ratio = ci.oddsRatio; highest_row = row; }
            }
            if (highest_row >= 0) {//mark cell as highest odd
                CellInfo ci = (CellInfo) (getCell(highest_row, col).getValue());
                ci.highestOdds = true;
            }
        }
    }

    //----- inner class -----
    class CellInfo {
        CellInfo(float avg, int pos_cnt, int neg_cnt) {
            averageRoi = avg; positiveCount = pos_cnt; neagtiveCount = neg_cnt;
        }
        float getAverageRoi() { return averageRoi; }
        int getPositiveCount() { return positiveCount; }
        int getNeagtiveCount() { return neagtiveCount; }
        public boolean isHighestOdds() { return highestOdds; }
        private float averageRoi;//average ROI of all symbols in this type
        private int positiveCount;//number of positive ROI
        private int neagtiveCount;//number of negative ROI
        private boolean highestOdds;//highest odds of positive
        private float oddsRatio;//positive / negative
    }

    //----- accessors -----
    public HashMap<String, ArrayList<GapInfo>> getFactorMap() { return _mapFactor; }
    void setGapList(ArrayList<GapInfo> gap_infos) { _GapInfos = gap_infos; }

    //----- variables -----
    private HashMap<String, ArrayList<GapInfo>> _mapFactor;
    private ArrayList<GapInfo> _GapInfos;

    //----- literals -----
    private static final String ROI_LT_5 =  " 0 - 5%";
    private static final String ROI_LT_10 = " 5 - 10%";
    private static final String ROI_LT_15 = "10 - 15%";
    private static final String ROI_GT_15 = " > 15%";
    static final int COLUMN_FACTOR = 0;
    static final int COLUMN_POSTGAP_PERF_1WK = 1;
    static final int COLUMN_POSTGAP_PERF_2WK = 2;
    static final int COLUMN_POSTGAP_PERF_4WK = 3;
    static final int COLUMN_POSTGAP_PERF_6WK = 4;
    static final int COLUMN_POSTGAP_PERF_8WK = 5;
    static final int COLUMN_POSTGAP_PERF_12WK = 6;
    static final Object[][] TABLE_SCHEMA = {
        {ApolloConstants.APOLLO_BUNDLE.getString("gps_factor"),   ColumnTypeEnum.TYPE_STRING,  1, 100, null, null, null},//=0
        {ApolloConstants.APOLLO_BUNDLE.getString("gps_post1wk"),  ColumnTypeEnum.TYPE_STRING, -1,  40, null, null, null},
        {ApolloConstants.APOLLO_BUNDLE.getString("gps_post2wk"),  ColumnTypeEnum.TYPE_STRING, -1,  40, null, null, null},
        {ApolloConstants.APOLLO_BUNDLE.getString("gps_post4wk"),  ColumnTypeEnum.TYPE_STRING, -1,  40, null, null, null},
        {ApolloConstants.APOLLO_BUNDLE.getString("gps_post6wk"),  ColumnTypeEnum.TYPE_STRING, -1,  40, null, null, null},
        {ApolloConstants.APOLLO_BUNDLE.getString("gps_post8wk"),  ColumnTypeEnum.TYPE_STRING, -1,  40, null, null, null},
        {ApolloConstants.APOLLO_BUNDLE.getString("gps_post12wk"), ColumnTypeEnum.TYPE_STRING, -1,  40, null, null, null},
    };
}
