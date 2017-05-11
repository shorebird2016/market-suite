package org.marketsuite.framework.strategy.base;

import org.marketsuite.component.table.ColumnTypeEnum;
import org.marketsuite.component.table.DynaTableModel;
import org.marketsuite.component.table.SimpleCell;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.component.table.ColumnTypeEnum;
import org.marketsuite.component.table.DynaTableModel;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.resource.FrameworkConstants;

import java.util.ArrayList;

/**
 * Table panel to present all trades with equity and performance.
 */
public class TradeTableModel extends DynaTableModel {
    public TradeTableModel() {
        remodel(DynaTableModel.generateSchema(TABLE_SCHEMA));
    }

    //-----interface implementations-----
    public void populate() {}
    public void populate(ArrayList<Transaction> trans) {
        float equity = FrameworkConstants.START_CAPITAL;
        _lstRows.clear();
        for (Transaction tran : trans) {
            SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
            cells[COLUMN_DIRECTION] = new SimpleCell(tran.isLongTrade() ? "Long" : "Short");
            cells[COLUMN_ENTRY_DATE] = new SimpleCell(tran.getEntryDate());
            cells[COLUMN_EXIT_DATE] = new SimpleCell(tran.getExitDate());
            float gain_loss = equity * tran.getPerformance();
            equity += gain_loss;
            cells[COLUMN_EQITY] = new SimpleCell(FrameworkConstants.DOLLAR_FORMAT.format(equity));
            cells[COLUMN_GAIN_LOSS_PERCENT] = new SimpleCell(FrameworkConstants.ROI_FORMAT.format(tran.getPerformance()));
            //indicate last trade still active
            if (tran.isActive()) {
                cells[COLUMN_ENTRY_DATE].setHighlight(true);
                cells[COLUMN_EXIT_DATE].setHighlight(true);
            }
            if (!tran.isLongTrade())
                cells[COLUMN_DIRECTION].setHighlight(true);
            _lstRows.add(cells);
        }
        fireTableDataChanged();
    }

    public boolean isCellEditable(int row, int col) {
        return false;
    }

    //----- literals -----
    //for special table util
    public  static final int COLUMN_DIRECTION = 0;
    private static final int COLUMN_ENTRY_DATE = 1;
    private static final int COLUMN_EXIT_DATE = 2;
    private static final int COLUMN_EQITY = 3;
    private static final int COLUMN_GAIN_LOSS_PERCENT = 4;
    private static final Object[][] TABLE_SCHEMA = {
        //heading, type, visible, hide-able, alignment, width, editor, renderer, comparator
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("pkn_hdr_1"), ColumnTypeEnum.TYPE_STRING, -1, 10, null, null, null },//direction
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("pkn_hdr_2"), ColumnTypeEnum.TYPE_STRING, -1, 50, null, null, null },//entry date
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("pkn_hdr_3"), ColumnTypeEnum.TYPE_STRING, -1, 50, null, null, null },//exit date
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("pkn_hdr_4"), ColumnTypeEnum.TYPE_STRING, -1, 50, null, null, null },//equity
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("pkn_hdr_5"), ColumnTypeEnum.TYPE_STRING, -1, 30, null, null, null },//return percent
//        { Constants.APP_BUNDLE.getString("pkn_hdr_6"), ColumnTypeEnum.TYPE_STRING, -1, 50, null, null, null },//return amount
    };
}
