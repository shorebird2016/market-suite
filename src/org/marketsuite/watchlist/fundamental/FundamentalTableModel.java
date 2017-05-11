package org.marketsuite.watchlist.fundamental;

import org.marketsuite.component.table.ColumnTypeEnum;
import org.marketsuite.component.table.DynaTableModel;
import org.marketsuite.component.table.SimpleCell;
import org.marketsuite.framework.model.Fundamental;
import org.marketsuite.main.MainModel;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;
import org.marketsuite.component.table.ColumnTypeEnum;
import org.marketsuite.component.table.DynaTableModel;
import org.marketsuite.framework.model.Fundamental;
import org.marketsuite.main.MainModel;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;

import java.util.Calendar;
import java.util.HashMap;

public class FundamentalTableModel extends DynaTableModel {
    public FundamentalTableModel() {
        remodel(DynaTableModel.generateSchema(TABLE_SCHEMA));
    }

    //----- interface, overrides -----
    public void populate() {}
    public void populate(WatchListModel parent_model) {
        _lstRows.clear();
        HashMap<String, Fundamental> fund_map = MainModel.getInstance().getFundamentals();
        if (fund_map == null)
            return;

        for (String symbol : parent_model.getMembers()) {
            addRow(symbol);
//            SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
//            for (int col = 0; col < TABLE_SCHEMA.length; col++) {//initialize all cells
//                switch (col) {
//                    case Fundamental.TOKEN_SYMBOL:
//                    case Fundamental.TOKEN_FULL_NAME:
//                    case Fundamental.TOKEN_SECTOR:
//                    case Fundamental.TOKEN_INDUSTRY:
//                    case Fundamental.TOKEN_EARNING_DATE:
//                        cells[col] = new SimpleCell("");
//                        break;
////                    case COLUMN_EPS_RANK:
////                        cells[col] = new SimpleCell(new Long(0));
////                        break;
//                    case Fundamental.TOKEN_LAST_UPDATE:
//                        Calendar cal = Calendar.getInstance();
//                        cal.add(Calendar.YEAR, -5);//impossible past date
//                        cells[col] = new SimpleCell(cal);
//                        break;
//                    default:
//                        cells[col] = new SimpleCell(new Double(0));
//                        break;
//                }
//            }
//            cells[Fundamental.TOKEN_SYMBOL] = new SimpleCell(symbol);
//            Fundamental fundamental = fund_map.get(symbol);
//            if (fundamental != null) {
//                cells[Fundamental.TOKEN_FULL_NAME] = new SimpleCell(fundamental.getFullName());
//                cells[Fundamental.TOKEN_SECTOR] = new SimpleCell(fundamental.getSector());
//                cells[Fundamental.TOKEN_INDUSTRY] = new SimpleCell(fundamental.getIndustry());
//                cells[Fundamental.TOKEN_MARKET_CAP] = new SimpleCell(new Double(fundamental.getMarketCap()));
//                cells[Fundamental.TOKEN_PE] = new SimpleCell(new Double(fundamental.getPE()));
//                cells[Fundamental.TOKEN_PEG] = new SimpleCell(new Double(fundamental.getPEG()));
//                cells[Fundamental.TOKEN_PS] = new SimpleCell(new Double(fundamental.getPS()));
//                cells[Fundamental.TOKEN_PB] = new SimpleCell(new Double(fundamental.getPB()));
//                cells[Fundamental.TOKEN_PRICE_TO_CASHFLOW] = new SimpleCell(new Double(fundamental.getPCF()));
//                cells[Fundamental.TOKEN_PRICE_TO_FREE_CASHFLOW] = new SimpleCell(new Double(fundamental.getPFCF()));
//                cells[Fundamental.TOKEN_YIELD] = new SimpleCell(new Double(fundamental.getYield()));
//                cells[Fundamental.TOKEN_EPS_TTM] = new SimpleCell(new Double(fundamental.getEps()));
//                cells[Fundamental.TOKEN_EPS_YTD] = new SimpleCell(new Double(fundamental.getEpsYtd()));
//                cells[Fundamental.TOKEN_EPS_5_YR] = new SimpleCell(new Double(fundamental.getEps5Yr()));
//                cells[Fundamental.TOKEN_SALES_5YR] = new SimpleCell(new Double(fundamental.getSales5Yr()));
//                cells[Fundamental.TOKEN_SALES_QTR] = new SimpleCell(new Double(fundamental.getSalesQtr()));
//                cells[Fundamental.TOKEN_EPS_QTR] = new SimpleCell(new Double(fundamental.getEpsQtr()));
//                cells[Fundamental.TOKEN_SHARES_OUT] = new SimpleCell(new Double(fundamental.getShares()));
//                cells[Fundamental.TOKEN_SHARES_FLOAT] = new SimpleCell(new Double(fundamental.getSharesFloat()));
//                cells[Fundamental.TOKEN_INSIDER_OWN] = new SimpleCell(new Double(fundamental.getInsiderPct()));
//                cells[Fundamental.TOKEN_INST_OWN] = new SimpleCell(new Double(fundamental.getInstPct()));
//                cells[Fundamental.TOKEN_SHARES_SHORT] = new SimpleCell(new Double(fundamental.getSharesShort()));
//                cells[Fundamental.TOKEN_SHORT_RATIO] = new SimpleCell(new Double(fundamental.getShortRatio()));
//                cells[Fundamental.TOKEN_ROA] = new SimpleCell(new Double(fundamental.getROA()));
//                cells[Fundamental.TOKEN_ROE] = new SimpleCell(new Double(fundamental.getROE()));
//                cells[Fundamental.TOKEN_ROI] = new SimpleCell(new Double(fundamental.getROI()));
//                cells[Fundamental.TOKEN_CUR_RATIO] = new SimpleCell(new Double(fundamental.getCurrentRatio()));
//                cells[Fundamental.TOKEN_QUICK_RATIO] = new SimpleCell(new Double(fundamental.getQuickRatio()));
//                cells[Fundamental.TOKEN_LT_DEBT_TO_EQTY] = new SimpleCell(new Double(fundamental.getDebtToEquityLt()));
//                cells[Fundamental.TOKEN_TOTAL_DEBT_TO_EQTY] = new SimpleCell(new Double(fundamental.getDebtToEquityTotal()));
//                cells[Fundamental.TOKEN_GROSS_MARGIN] = new SimpleCell(new Double(fundamental.getGrossMargin()));
//                cells[Fundamental.TOKEN_OP_MARGIN] = new SimpleCell(new Double(fundamental.getOperatingMargin()));
//                cells[Fundamental.TOKEN_PROFIT_MARGIN] = new SimpleCell(new Double(fundamental.getProfitMargin()));
//                cells[Fundamental.TOKEN_BETA] = new SimpleCell(new Double(fundamental.getBeta()));
//                cells[Fundamental.TOKEN_EARNING_DATE] = new SimpleCell(fundamental.getEarningDate());
//                cells[Fundamental.TOKEN_LAST_UPDATE] = new SimpleCell(fundamental.getLastUpdate());
//            }
//            _lstRows.add(cells);
        }
        fireTableDataChanged();
    }

    public boolean isCellEditable(int row, int col) {
        return false;
    }

    //find a symbol and return row index, return -1 if not found
    public int findSymbol(String symbol) {
        for (int row = 0; row < getRowCount(); row++) {
            String sym = (String) getCell(row, Fundamental.TOKEN_SYMBOL).getValue();
            if (sym.equals(symbol))
                return row;
        }
        return -1;
    }

    //----- protected methods -----
    void showHideSymbol(String symbol, boolean show_hide) {
        if (show_hide) {
            addRow(symbol);
        }
        else {
            //find the row and delete it
            for (int row = 0; row < getRowCount(); row++) {
                String sym = (String)getCell(row, Fundamental.TOKEN_SYMBOL).getValue();
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
        SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
        for (int col = 0; col < TABLE_SCHEMA.length; col++) {//initialize all cells
            switch (col) {
                case Fundamental.TOKEN_SYMBOL:
                case Fundamental.TOKEN_FULL_NAME:
                case Fundamental.TOKEN_SECTOR:
                case Fundamental.TOKEN_INDUSTRY:
                case Fundamental.TOKEN_EARNING_DATE:
                    cells[col] = new SimpleCell("");
                    break;
                case Fundamental.TOKEN_LAST_UPDATE:
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.YEAR, -5);//impossible past date
                    cells[col] = new SimpleCell(cal);
                    break;
                default:
                    cells[col] = new SimpleCell(new Double(0));
                    break;
            }
        }
        cells[Fundamental.TOKEN_SYMBOL] = new SimpleCell(symbol);
        HashMap<String, Fundamental> fund_map = MainModel.getInstance().getFundamentals();
        Fundamental fundamental = fund_map.get(symbol);
        if (fundamental != null) {
            cells[Fundamental.TOKEN_FULL_NAME] = new SimpleCell(fundamental.getFullName());
            cells[Fundamental.TOKEN_SECTOR] = new SimpleCell(fundamental.getSector());
            cells[Fundamental.TOKEN_INDUSTRY] = new SimpleCell(fundamental.getIndustry());
            cells[Fundamental.TOKEN_MARKET_CAP] = new SimpleCell(new Double(fundamental.getMarketCap()));
            cells[Fundamental.TOKEN_PE] = new SimpleCell(new Double(fundamental.getPE()));
            cells[Fundamental.TOKEN_PEG] = new SimpleCell(new Double(fundamental.getPEG()));
            cells[Fundamental.TOKEN_PS] = new SimpleCell(new Double(fundamental.getPS()));
            cells[Fundamental.TOKEN_PB] = new SimpleCell(new Double(fundamental.getPB()));
            cells[Fundamental.TOKEN_PRICE_TO_CASHFLOW] = new SimpleCell(new Double(fundamental.getPCF()));
            cells[Fundamental.TOKEN_PRICE_TO_FREE_CASHFLOW] = new SimpleCell(new Double(fundamental.getPFCF()));
            cells[Fundamental.TOKEN_YIELD] = new SimpleCell(new Double(fundamental.getYield()));
            cells[Fundamental.TOKEN_EPS_TTM] = new SimpleCell(new Double(fundamental.getEps()));
            cells[Fundamental.TOKEN_EPS_YTD] = new SimpleCell(new Double(fundamental.getEpsYtd()));
            cells[Fundamental.TOKEN_EPS_5_YR] = new SimpleCell(new Double(fundamental.getEps5Yr()));
            cells[Fundamental.TOKEN_SALES_5YR] = new SimpleCell(new Double(fundamental.getSales5Yr()));
            cells[Fundamental.TOKEN_SALES_QTR] = new SimpleCell(new Double(fundamental.getSalesQtr()));
            cells[Fundamental.TOKEN_EPS_QTR] = new SimpleCell(new Double(fundamental.getEpsQtr()));
            cells[Fundamental.TOKEN_SHARES_OUT] = new SimpleCell(new Double(fundamental.getShares()));
            cells[Fundamental.TOKEN_SHARES_FLOAT] = new SimpleCell(new Double(fundamental.getSharesFloat()));
            cells[Fundamental.TOKEN_INSIDER_OWN] = new SimpleCell(new Double(fundamental.getInsiderPct()));
            cells[Fundamental.TOKEN_INST_OWN] = new SimpleCell(new Double(fundamental.getInstPct()));
            cells[Fundamental.TOKEN_SHARES_SHORT] = new SimpleCell(new Double(fundamental.getSharesShort()));
            cells[Fundamental.TOKEN_SHORT_RATIO] = new SimpleCell(new Double(fundamental.getShortRatio()));
            cells[Fundamental.TOKEN_ROA] = new SimpleCell(new Double(fundamental.getROA()));
            cells[Fundamental.TOKEN_ROE] = new SimpleCell(new Double(fundamental.getROE()));
            cells[Fundamental.TOKEN_ROI] = new SimpleCell(new Double(fundamental.getROI()));
            cells[Fundamental.TOKEN_CUR_RATIO] = new SimpleCell(new Double(fundamental.getCurrentRatio()));
            cells[Fundamental.TOKEN_QUICK_RATIO] = new SimpleCell(new Double(fundamental.getQuickRatio()));
            cells[Fundamental.TOKEN_LT_DEBT_TO_EQTY] = new SimpleCell(new Double(fundamental.getDebtToEquityLt()));
            cells[Fundamental.TOKEN_TOTAL_DEBT_TO_EQTY] = new SimpleCell(new Double(fundamental.getDebtToEquityTotal()));
            cells[Fundamental.TOKEN_GROSS_MARGIN] = new SimpleCell(new Double(fundamental.getGrossMargin()));
            cells[Fundamental.TOKEN_OP_MARGIN] = new SimpleCell(new Double(fundamental.getOperatingMargin()));
            cells[Fundamental.TOKEN_PROFIT_MARGIN] = new SimpleCell(new Double(fundamental.getProfitMargin()));
            cells[Fundamental.TOKEN_BETA] = new SimpleCell(new Double(fundamental.getBeta()));
            cells[Fundamental.TOKEN_EARNING_DATE] = new SimpleCell(fundamental.getEarningDate());
            cells[Fundamental.TOKEN_LAST_UPDATE] = new SimpleCell(fundamental.getLastUpdate());
        }
        _lstRows.add(cells);
        fireTableDataChanged();
    }

    //----- literals -----
    //Note: use TOKEN_XXX values in Fundamental class as default column ordering with IBD added to end
//    static final int COLUMN_EPS_RANK = Fundamental.TOKEN_LAST_UPDATE + 1;//IBD
    static final Object[][] TABLE_SCHEMA = {
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_1"), ColumnTypeEnum.TYPE_STRING,  -1, 50, null, null, null},  //0 - symbol
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_40"), ColumnTypeEnum.TYPE_STRING,  1, 150, null, null, null},//full name
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_41"), ColumnTypeEnum.TYPE_STRING,  1, 100, null, null, null},//sector
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_42"), ColumnTypeEnum.TYPE_STRING,  1, 150, null, null, null},//industry
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_43"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null},//market cap
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_19"), ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null},//5 - PE
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_44"), ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null},//PEG
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_45"), ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null},//PS
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_46"), ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null},//PB
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_47"), ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null},//PCF
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_20"), ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null},//10 - PFCF
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_38"), ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null},//yield
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_48"), ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null},//EPS TTM
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_49"), ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null},//EPS YTD
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_50"), ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null},//EPS 5 year
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_51"), ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null},//15 - Sales 5 year
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_52"), ColumnTypeEnum.TYPE_DOUBLE, -1, 70, null, null, null},//EPS Qtr
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_53"), ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null},//Sale Qtr
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_54"), ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null},//shares
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_55"), ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null},//float
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_56"), ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null},//20 - insider
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_57"), ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null},//institution
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_58"), ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null},//short
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_59"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null},//short ratio
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_60"), ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null},//ROA
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_61"), ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null},//25 - ROE
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_62"), ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null},//ROI
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_13"), ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null},//CR
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_63"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null},//QR
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_14"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null},//Debt / equity LT
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_64"), ColumnTypeEnum.TYPE_DOUBLE, -1, 100, null, null, null},//30 - Debt / equity total
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_65"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null},//gross margin
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_17"), ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null},//op margin
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_18"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null},//profit margin
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_66"), ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null},//beta
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_67"), ColumnTypeEnum.TYPE_STRING,  1, 150, null, null, null},//35 - earning date
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_39"), ColumnTypeEnum.TYPE_STRING, -1, 100, null, null, null},//last update
//            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_34"), ColumnTypeEnum.TYPE_LONG, -1, 50, null, null, null},  //IBD EPS rank
    };
}