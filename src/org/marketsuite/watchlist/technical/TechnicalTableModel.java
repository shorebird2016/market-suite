package org.marketsuite.watchlist.technical;

import org.marketsuite.component.table.ColumnTypeEnum;
import org.marketsuite.component.table.DynaTableModel;
import org.marketsuite.component.table.SimpleCell;
import org.marketsuite.framework.market.ChangeInfo;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.data.IbdRating;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.main.MainModel;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;
import org.marketsuite.component.table.ColumnTypeEnum;
import org.marketsuite.component.table.DynaTableModel;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.main.MainModel;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;

import java.util.ArrayList;
import java.util.Calendar;

public class TechnicalTableModel extends DynaTableModel {
    public TechnicalTableModel() { remodel(DynaTableModel.generateSchema(TABLE_SCHEMA)); }

    //----- interface, override -----
    public boolean isCellEditable(int row, int col) { return false; }
    public void populate() {}
    public void populate(WatchListModel parent_model) {
        _lstRows.clear();
//        ArrayList<String> members = parent_model.getMembers();
        for (String symbol : parent_model.getMembers()) {
            addRow(symbol);
//            SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
//
//            //initialize all cells
//            for (int col=0; col<TABLE_SCHEMA.length; col++) {
//                switch (col) {
//                    case COLUMN_IBD_COMPOSITE:
//                    case COLUMN_IBD_EPS:
//                    case COLUMN_IBD_RS:
//                        cells[col] = new SimpleCell(new Long(0));
//                        break;
//                    case COLUMN_NEAR_200SMA:
//                    case COLUMN_NEAR_50SMA:
//                    case COLUMN_NEAR_20SMA:
//                        cells[col] = new SimpleCell(new Double(0));
//                        break;
//                    default: cells[col] = new SimpleCell(""); break;
//                }
//            }
//
//            //populate from WatchListModel
//            cells[COLUMN_SYMBOL] = new SimpleCell(symbol);
//            MarketInfo mki = parent_model.getMarketInfo(symbol);
//            if (mki != null) {
//                cells[COLUMN_PHASE] = new SimpleCell(mki.getCurrentPhase());
//                float close = mki.getFund().getQuote().get(0).getClose();
//                float ma20 = mki.getSma20()[0];
//                cells[COLUMN_NEAR_20SMA] = new SimpleCell(new Double((close - ma20) / close));
//                float ma50 = mki.getSma50d()[0];
//                cells[COLUMN_NEAR_50SMA] = new SimpleCell(new Double((close - ma50) / close));
//                float[] sma200 = mki.getSma200d();
//                if (sma200 != null) {
//                    float ma200 = sma200[0];
//                    cells[COLUMN_NEAR_200SMA] = new SimpleCell(new Double((close - ma200) / close));
//                }
//            }
//
//            //extract IBD ratings for this symbol
//            ArrayList<IbdRating> ibd_rating = parent_model.getIbdRatingMap().get(symbol);
//            if (ibd_rating != null) {
//                cells[COLUMN_IBD_COMPOSITE] = new SimpleCell(new Long(ibd_rating.get(0).getComposite()));
//                cells[COLUMN_IBD_EPS] = new SimpleCell(new Long(ibd_rating.get(0).getEpsRating()));
//                cells[COLUMN_IBD_RS] = new SimpleCell(new Long(ibd_rating.get(0).getRsRating()));
//                cells[COLUMN_IBD_SMR] = new SimpleCell(ibd_rating.get(0).getSmr());
//                cells[COLUMN_IBD_ACC_DIS] = new SimpleCell(ibd_rating.get(0).getAccDis());
//                cells[COLUMN_IBD_GRP_ACC_DIS] = new SimpleCell(ibd_rating.get(0).getGroupRating());
//                cells[COLUMN_LAST_UPDATE] = new SimpleCell(FrameworkConstants.YAHOO_DATE_FORMAT.format(ibd_rating.get(0).getDate().getTime()));
//            }
//            _lstRows.add(cells);
        }
        fireTableDataChanged();
    }

    //----- protected methods -----
    void showHideSymbol(String symbol, boolean show_hide) {
        if (show_hide) {
            addRow(symbol);
            fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
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

    //----- private methods -----
    private void addRow(String symbol) {
        WatchListModel wlm = MainModel.getInstance().getWatchListModel();
        SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];

        //initialize all cells
        for (int col=0; col<TABLE_SCHEMA.length; col++) {
            switch (col) {
                case COLUMN_IBD_COMPOSITE:
                case COLUMN_IBD_EPS:
                case COLUMN_IBD_RS:
                    cells[col] = new SimpleCell(new Long(0));
                    break;
                case COLUMN_NEAR_200SMA:
                case COLUMN_NEAR_50SMA:
                case COLUMN_NEAR_20SMA:
                    cells[col] = new SimpleCell(new Double(0));
                    break;
                default: cells[col] = new SimpleCell(""); break;
            }
        }

        //populate from WatchListModel
        cells[COLUMN_SYMBOL] = new SimpleCell(symbol);
        MarketInfo mki = wlm.getMarketInfo(symbol);
        if (mki != null) {
            cells[COLUMN_PHASE] = new SimpleCell(mki.getCurrentPhase());
            float close = mki.getFund().getQuote().get(0).getClose();
            float ma20 = mki.getSma20()[0];
            cells[COLUMN_NEAR_20SMA] = new SimpleCell(new Double((close - ma20) / close));
            float[] sma50 = mki.getSma50();
            if (sma50 != null) {
                float ma50 = sma50[0];
                cells[COLUMN_NEAR_50SMA] = new SimpleCell(new Double((close - ma50) / close));
            }
            float[] sma200 = mki.getSma200();
            if (sma200 != null) {
                float ma200 = sma200[0];
                cells[COLUMN_NEAR_200SMA] = new SimpleCell(new Double((close - ma200) / close));
            }
        }

        //extract IBD ratings for this symbol
        ArrayList<IbdRating> ibd_rating = wlm.getIbdRatingMap().get(symbol);
        if (ibd_rating != null) {//first element in rating is always the most recent
            cells[COLUMN_IBD_COMPOSITE] = new SimpleCell(new Long(ibd_rating.get(0).getComposite()));
            cells[COLUMN_IBD_EPS] = new SimpleCell(new Long(ibd_rating.get(0).getEpsRating()));
            cells[COLUMN_IBD_RS] = new SimpleCell(new Long(ibd_rating.get(0).getRsRating()));
            cells[COLUMN_IBD_SMR] = new SimpleCell(ibd_rating.get(0).getSmr());
            cells[COLUMN_IBD_ACC_DIS] = new SimpleCell(ibd_rating.get(0).getAccDis());
            cells[COLUMN_IBD_GRP_ACC_DIS] = new SimpleCell(ibd_rating.get(0).getGroupRating());
            cells[COLUMN_LAST_UPDATE] = new SimpleCell(FrameworkConstants.YAHOO_DATE_FORMAT.format(ibd_rating.get(0).getDate().getTime()));
        }
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

    //----- literals -----
    static final int COLUMN_SYMBOL = 0;
    static final int COLUMN_PHASE = 1;
    static final int COLUMN_NEAR_20SMA = 2;
    static final int COLUMN_NEAR_50SMA = 3;
    static final int COLUMN_NEAR_200SMA = 4;
    static final int COLUMN_IBD_COMPOSITE = 5;
    static final int COLUMN_IBD_EPS = 6;
    static final int COLUMN_IBD_RS = 7;
    static final int COLUMN_IBD_SMR = 8;
    static final int COLUMN_IBD_ACC_DIS = 9;
    static final int COLUMN_IBD_GRP_ACC_DIS = 10;
    static final int COLUMN_LAST_UPDATE = 11;
    private static final Object[][] TABLE_SCHEMA = {
        {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_1"),  ColumnTypeEnum.TYPE_STRING,  2, 10, null, null, null},//symbol
        {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_23"), ColumnTypeEnum.TYPE_STRING, -1, 30, null, null, null},//phase
        {ApolloConstants.APOLLO_BUNDLE.getString("qp_13"),     ColumnTypeEnum.TYPE_DOUBLE, -1, 20, null, null, null},//20MA
        {ApolloConstants.APOLLO_BUNDLE.getString("qp_07"),     ColumnTypeEnum.TYPE_DOUBLE, -1, 20, null, null, null},//50MA
        {ApolloConstants.APOLLO_BUNDLE.getString("qp_08"),     ColumnTypeEnum.TYPE_DOUBLE, -1, 20, null, null, null},//200MA
        {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_32"), ColumnTypeEnum.TYPE_LONG,   -1, 10, null, null, null},//IBD Composite
        {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_34"), ColumnTypeEnum.TYPE_LONG,   -1, 5, null, null, null},//IBD EPS
        {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_26"), ColumnTypeEnum.TYPE_LONG,   -1, 5, null, null, null},//IBD RS
        {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_27"), ColumnTypeEnum.TYPE_STRING, -1, 5, null, null, null},//IBD SMR
        {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_28"), ColumnTypeEnum.TYPE_STRING, -1, 10, null, null, null},//IBD Accumulation / Distribution
        {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_29"), ColumnTypeEnum.TYPE_STRING, -1, 15, null, null, null},//IBD Group Accumulation / Distribution
        {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_37"), ColumnTypeEnum.TYPE_STRING, -1, 50, null, null, null},//last update date
    };
}
