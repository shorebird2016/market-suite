package org.marketsuite.watchlist.speed;

import org.marketsuite.component.table.ColumnTypeEnum;
import org.marketsuite.component.table.DynaTableModel;
import org.marketsuite.component.table.SimpleCell;
import org.marketsuite.framework.model.data.EquitySpeed;
import org.marketsuite.main.MainModel;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;
import org.marketsuite.component.table.ColumnTypeEnum;
import org.marketsuite.component.table.DynaTableModel;
import org.marketsuite.main.MainModel;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class SpeedViewTableModel extends DynaTableModel {
    //CTOR
    public SpeedViewTableModel() { remodel(DynaTableModel.generateSchema(TABLE_SCHEMA)); }

    //interface / override
    public void populate() {
        HashMap<String,EquitySpeed> spd_map = MainModel.getInstance().getSpeedMap();
        WatchListModel wlm = MainModel.getInstance().getWatchListModel();
        ArrayList<String> members = wlm.getMembers();
        _lstRows.clear();
        for (String member : members) {
            //look up speeds from map
            EquitySpeed spd = spd_map.get(member);
            if (spd == null) continue;
            _lstRows.add(createCells(spd));
        }
        fireTableDataChanged();
    }
    public boolean isCellEditable(int row, int col) { return false; }

    //public methods
    //find a symbol and return row index, return -1 if not found
    public int findSymbol(String symbol) {
        for (int row = 0; row < getRowCount(); row++) {
            String sym = (String)getCell(row, COLUMN_SYMBOL).getValue();
            if (sym.equals(symbol))
                return row;
        }
        return -1;
    }
    //show all symbols in table
    public void showAllSymbols() {
        _lstRows.clear();
        HashMap<String,EquitySpeed> spd_map = MainModel.getInstance().getSpeedMap();
        Iterator<String> itor = spd_map.keySet().iterator();
        while (itor.hasNext()) {
            String sym = itor.next();
            SimpleCell[] cells = createCells(spd_map.get(sym));
            _lstRows.add(cells);
        }
        fireTableDataChanged();
    }
    //get current list of symbols
    public ArrayList<String> getSymbols() {
        ArrayList<String> ret = new ArrayList<>();
        for (SimpleCell[] cells : _lstRows) {
            String sym = (String)cells[COLUMN_SYMBOL].getValue();
            ret.add(sym);
        }
        return ret;
    }

    //private methods
    private SimpleCell[] initCells() {
        SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
        for (int col = 0; col < TABLE_SCHEMA.length; col++) {//initialize all cells
            switch (col) {
                case COLUMN_MONTH_BAR:
                case COLUMN_WEEK_BAR: cells[col] = new SimpleCell(new Long(4)); break;
                case COLUMN_MONTH_PCT: case COLUMN_MONTHLY_SPEED:
                case COLUMN_WEEK_PCT:case COLUMN_WEEKLY_SPEED: cells[col] = new SimpleCell(new Double(0)); break;
                case COLUMN_SYMBOL:
                default: cells[col] = new SimpleCell(""); break;
            }
        }
        return cells;
    }
    private SimpleCell[] createCells(EquitySpeed spd) {
        SimpleCell[] cells = initCells();
        cells[COLUMN_SYMBOL].setValue(spd.getSymbol());
        cells[COLUMN_MONTH_PCT].setValue(spd.getMonthlyPct() / 100);
        cells[COLUMN_MONTH_BAR].setValue(spd.getMonthlyBar());
        cells[COLUMN_MONTHLY_SPEED].setValue(spd.getMonthlyPct() / (100 * spd.getMonthlyBar()));
        cells[COLUMN_WEEK_PCT].setValue(spd.getWeeklyPct() / 100);
        cells[COLUMN_WEEK_BAR].setValue(spd.getWeeklyBar());
        cells[COLUMN_WEEKLY_SPEED].setValue(spd.getWeeklyPct() / (100 * spd.getWeeklyBar()));
        return cells;
    }

    //----- literals -----
            static final int COLUMN_SYMBOL = 0;
            static final int COLUMN_MONTH_PCT = 1;
    private static final int COLUMN_MONTH_BAR = 2;
            static final int COLUMN_MONTHLY_SPEED = 3;
            static final int COLUMN_WEEK_PCT = 4;
    private static final int COLUMN_WEEK_BAR = 5;
            static final int COLUMN_WEEKLY_SPEED = 6;
    private static final Object[][] TABLE_SCHEMA = {
        {ApolloConstants.APOLLO_BUNDLE.getString("l2_spv_c1"), ColumnTypeEnum.TYPE_STRING, -1, 30, null, null, null},//symbol
        {ApolloConstants.APOLLO_BUNDLE.getString("l2_spv_c2"), ColumnTypeEnum.TYPE_DOUBLE, -1, 50, null, null, null},//monthly percent
        {ApolloConstants.APOLLO_BUNDLE.getString("l2_spv_c3"), ColumnTypeEnum.TYPE_LONG, -1, 50, null, null, null},//monthly bars
        {ApolloConstants.APOLLO_BUNDLE.getString("l2_spv_c4"), ColumnTypeEnum.TYPE_DOUBLE, -1, 50, null, null, null},//monthly speed
        {ApolloConstants.APOLLO_BUNDLE.getString("l2_spv_c5"), ColumnTypeEnum.TYPE_DOUBLE, -1, 50, null, null, null},//weekly bars
        {ApolloConstants.APOLLO_BUNDLE.getString("l2_spv_c6"), ColumnTypeEnum.TYPE_LONG, -1, 50, null, null, null},//weekly bars
        {ApolloConstants.APOLLO_BUNDLE.getString("l2_spv_c7"), ColumnTypeEnum.TYPE_DOUBLE, -1, 50, null, null, null},//weekly speed
    };
}