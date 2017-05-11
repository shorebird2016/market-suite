package org.marketsuite.framework.strategy.analysis;

import org.marketsuite.component.table.ColumnTypeEnum;
import org.marketsuite.component.table.DynaTableModel;
import org.marketsuite.component.table.SimpleCell;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.component.table.ColumnTypeEnum;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.resource.FrameworkConstants;

import java.util.ArrayList;

/**
 * Table panel to present all trades with equity and performance.
 */
public class TransactionTableModel extends DynaTableModel {
    public TransactionTableModel() {
        remodel(DynaTableModel.generateSchema(TABLE_SCHEMA));
    }

    //-----interface implementations-----
    public void populate() {}
    public void populate(ArrayList<Transaction> trans) {
        float equity = FrameworkConstants.START_CAPITAL;
        _lstRows.clear();
        int seq = 1;
        for (Transaction tran : trans) {
            SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
            cells[COLUMN_SEQUENCE] = new SimpleCell(String.valueOf(seq++));
            cells[COLUMN_DIRECTION] = new SimpleCell(tran.isLongTrade() ? "L" : "S");
            cells[COLUMN_ENTRY_DATE] = new SimpleCell(tran.getEntryDate());
            cells[COLUMN_ENTRY_PRICE] = new SimpleCell(FrameworkConstants.DOLLAR_FORMAT.format(tran.getEntryPrice()));
            cells[COLUMN_EXIT_DATE] = new SimpleCell(tran.getExitDate());
            cells[COLUMN_EXIT_PRICE] = new SimpleCell(FrameworkConstants.DOLLAR_FORMAT.format(tran.getExitPrice()));
            float gain_loss = equity * tran.getPerformance();
            equity += gain_loss;
            cells[COLUMN_GAIN_LOSS_PERCENT] = new SimpleCell(FrameworkConstants.ROI_FORMAT.format(tran.getPerformance()));
            cells[COLUMN_EQITY] = new SimpleCell(FrameworkConstants.DOLLAR_FORMAT.format(equity));
            _lstRows.add(cells);
        }
        fireTableDataChanged();
    }

    public boolean isCellEditable(int row, int col) { return false; }

    public void clear() {
        _lstRows.clear();
        fireTableDataChanged();
    }

    //-----literals-----
            static final int COLUMN_SEQUENCE = 0;
            static final int COLUMN_DIRECTION = 1;
            static final int COLUMN_ENTRY_DATE = 2;
            static final int COLUMN_ENTRY_PRICE = 3;
            static final int COLUMN_EXIT_DATE = 4;
    private static final int COLUMN_EXIT_PRICE = 5;
    private static final int COLUMN_GAIN_LOSS_PERCENT = 6;
    private static final int COLUMN_EQITY = 7;
    private static final Object[][] TABLE_SCHEMA = {
        //heading, type, visible, hide-able, alignment, width, editor, renderer, comparator
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("lbl_col_0"), ColumnTypeEnum.TYPE_STRING, -1, 5, null, null, null },//sequence
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("lbl_col_1"), ColumnTypeEnum.TYPE_STRING, -1, 5, null, null, null },//direction long/short
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("cus_hdr_2"), ColumnTypeEnum.TYPE_STRING, -1, 50, null, null, null },//entry date
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("cus_hdr_3"), ColumnTypeEnum.TYPE_STRING, -1, 25, null, null, null },//entry price
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("cus_hdr_4"), ColumnTypeEnum.TYPE_STRING, -1, 50, null, null, null },//exit date
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("cus_hdr_5"), ColumnTypeEnum.TYPE_STRING, -1, 25, null, null, null },//exit price
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("cus_hdr_6"), ColumnTypeEnum.TYPE_STRING, -1, 50, null, null, null },//return percent
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("cus_hdr_7"), ColumnTypeEnum.TYPE_STRING, -1, 50, null, null, null },//equity
    };
}
