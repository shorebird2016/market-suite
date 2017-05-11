package org.marketsuite.framework.strategy.buyhold;

import org.marketsuite.component.table.ColumnTypeEnum;
import org.marketsuite.component.table.DynaTableModel;
import org.marketsuite.component.table.SimpleCell;
import org.marketsuite.framework.model.Equity;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.component.table.ColumnTypeEnum;

import java.util.ArrayList;

/**
 * Table model for equity table presentation.
 */
public class EquityTableModel extends DynaTableModel {
    public EquityTableModel() {
        remodel(DynaTableModel.generateSchema(TABLE_SCHEMA));
    }

    public void populate() {}

    public void populate(ArrayList<Equity> log) {
        _lstRows.clear();
        for (Equity equity : log) {
            SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
            cells[COLUMN_DATE] = new SimpleCell(equity.getDate());
            cells[COLUMN_EQITY] = new SimpleCell(FrameworkConstants.DOLLAR_FORMAT.format(equity.getEquity()));
            //calc return
            float principal = FrameworkConstants.START_CAPITAL;
            float roi = equity.getEquity() / principal - 1;
//                cells[COLUMN_RETURN] = new SimpleCell(Constants.ROI_FORMAT.format(roi));
            _lstRows.add(cells);
        }
        fireTableDataChanged();
    }

    public boolean isCellEditable(int row, int col) {
        return false;
    }

    //literals
    private static final int COLUMN_DATE = 0;
    private static final int COLUMN_EQITY = 1;
    private static final Object[][] TABLE_SCHEMA = {
        //heading, type, visible, hide-able, alignment, width, editor, renderer, comparator
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("bh_col_1"), ColumnTypeEnum.TYPE_STRING, -1, 30, null, null, null },//date
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("bh_col_2"), ColumnTypeEnum.TYPE_STRING, -1, 50, null, null, null },//equity
    };
}
