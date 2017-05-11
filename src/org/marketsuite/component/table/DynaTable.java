package org.marketsuite.component.table;

import java.util.EventObject;

import javax.swing.JTable;

/**
 * A table with built in DynaTableModel to save duplicate code
 */
public class DynaTable extends JTable {
	//CTOR:
    public DynaTable(DynaTableModel table_model) {
        super(table_model);
        _TableModel = table_model;
    }

    public DynaTable(DynaTableModel table_model, int selection_mode, boolean can_reorder) {
        this(table_model);
        setSelectionMode(selection_mode);
        getTableHeader().setReorderingAllowed(can_reorder);
    }

    //interface implementations
    //to cancel editing when context changed
    public boolean editCellAt(int row, int col, EventObject obj) {
        boolean ret = super.editCellAt(row, col, obj);
        if (ret)//editing started
            _TableModel.setTableInEditing(DynaTable.this);
        return ret;
    }

    //instance variables
    private DynaTableModel _TableModel;
}
