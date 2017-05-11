package org.marketsuite.datamgr.dataimport;

import org.marketsuite.component.table.ColumnTypeEnum;
import org.marketsuite.component.table.DynaTableModel;
import org.marketsuite.component.table.SimpleCell;
import org.marketsuite.framework.model.Fundamental;
import org.marketsuite.main.MainModel;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;
import org.marketsuite.component.table.ColumnTypeEnum;
import org.marketsuite.component.table.DynaTableModel;
import org.marketsuite.component.table.SimpleCell;
import org.marketsuite.framework.model.Fundamental;
import org.marketsuite.main.MainModel;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;

import java.text.SimpleDateFormat;
import java.util.HashMap;

public class ImportTableModel extends DynaTableModel {
    public ImportTableModel(WatchListModel parent_model) {
        _ParentModel = parent_model;
        remodel(DynaTableModel.generateSchema(TABLE_SCHEMA));
    }

    public void populate() {
        HashMap<String,Fundamental> fund_map = MainModel.getInstance().getFundamentals();
        for (String symbol : _ParentModel.getMembers()) {
            SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
            for (int col=0; col<TABLE_SCHEMA.length; col++) {//initialize all cells
                switch (col) {
                    case COLUMN_SYMBOL:
                    case COLUMN_DATE:
                        cells[col] = new SimpleCell(""); break;
                    default: cells[col] = new SimpleCell(new Double(0)); break;
                }
            }
            cells[COLUMN_SYMBOL] = new SimpleCell(symbol);
            Fundamental fundamental = fund_map.get(symbol);
            if (fundamental == null)
                continue;

//            cells[COLUMN_DATE] = new SimpleCell(fundamental.getDate());
//            cells[COLUMN_CUR_RATIO] = new SimpleCell(new Double(fundamental.getCurrentRatio()));
//            cells[COLUMN_DEBT_EQTY_RATIO] = new SimpleCell(new Double(fundamental.getDebtToEquityRatio()));
//            cells[COLUMN_REV_GROWTH] = new SimpleCell(new Double(fundamental.getRevenueGrowth()));
//            cells[COLUMN_ROE] = new SimpleCell(new Double(fundamental.getROE()));
//            cells[COLUMN_OP_MARGIN] = new SimpleCell(new Double(fundamental.getOperatingMargin()));
//            cells[COLUMN_NET_MARGIN] = new SimpleCell(new Double(fundamental.getNetMargin()));
//            cells[COLUMN_PE] = new SimpleCell(new Double(fundamental.getPE()));
//            cells[COLUMN_CASH_FLOW] = new SimpleCell(new Double(fundamental.getCashFlow()));
//            cells[COLUMN_EARNING_GROWTH] = new SimpleCell(new Double(fundamental.getEarningGrowth()));
            _lstRows.add(cells);
        }
        fireTableDataChanged();
    }

    public boolean isCellEditable(int row, int col) {
        return false;
    }

    //----- private method -----

    //----- variables -----
    private WatchListModel _ParentModel;

    //----- literals -----
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");
            static final int COLUMN_SYMBOL = 0;
            static final int COLUMN_DATE = 1;
            static final int COLUMN_CUR_RATIO = 2;
            static final int COLUMN_DEBT_EQTY_RATIO = 3;
            static final int COLUMN_REV_GROWTH = 4;
            static final int COLUMN_ROE = 5;
            static final int COLUMN_OP_MARGIN = 6;
            static final int COLUMN_NET_MARGIN = 7;
            static final int COLUMN_PE = 8;
            static final int COLUMN_CASH_FLOW = 9;
            static final int COLUMN_EARNING_GROWTH = 10;
    private static final Object[][] TABLE_SCHEMA = {
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_1"), ColumnTypeEnum.TYPE_STRING,  2, 20, null, null, null},//symbol
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_12"), ColumnTypeEnum.TYPE_STRING, -1, 30, null, null, null},//date
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_13"), ColumnTypeEnum.TYPE_DOUBLE, -1, 30, null, null, null},//current ratio
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_14"), ColumnTypeEnum.TYPE_DOUBLE, -1, 50, null, null, null},//debt / equity ratio
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_15"), ColumnTypeEnum.TYPE_DOUBLE, -1, 50, null, null, null},//revenue growth
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_16"), ColumnTypeEnum.TYPE_DOUBLE, -1, 30, null, null, null},//return on equity
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_17"), ColumnTypeEnum.TYPE_DOUBLE, -1, 50, null, null, null},//operating margin
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_18"), ColumnTypeEnum.TYPE_DOUBLE, -1, 50, null, null, null},//net margin
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_19"), ColumnTypeEnum.TYPE_DOUBLE, -1, 30, null, null, null},//PE
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_20"), ColumnTypeEnum.TYPE_DOUBLE,-1, 50, null, null, null},///free cash flow
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_21"), ColumnTypeEnum.TYPE_DOUBLE,-1, 50, null, null, null},//Earning growth
    };
}