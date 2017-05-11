package org.marketsuite.component.table;

import java.awt.Component;
import java.text.DecimalFormat;
import java.util.EventObject;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentListener;

import org.marketsuite.component.Constants;
import org.marketsuite.component.field.DecimalField;
import org.marketsuite.component.field.DecimalField;

//todo: focus listener stop cell editing but causes another event onto the queue, maybe there's a better way to do this

/**
 * A cell editor for JTable for editing decimal numbers.
 */
public class DecimalCellEditor extends DefaultCellEditor {
	public DecimalCellEditor(double value, int columns,
           double lower_limit, double upper_limit, DocumentListener listener) {
       super(new DecimalField(value, columns, lower_limit, upper_limit, listener));
    }

    //Override to invoke setValue on the formatted text field.
    // and force cell to select all its content when in focus
    public Component getTableCellEditorComponent(
            JTable table, Object value, boolean isSelected, int row, int column) {
        field = (DecimalField)super.getTableCellEditorComponent(table, value, isSelected, row, column);
        field.setHorizontalAlignment(SwingConstants.CENTER);
        field.setBackground(Constants.CELL_EDITING_BACKGROUND);
        //avoid editing stayed
//        field.addFocusListener(new FocusAdapter() {
//            public void focusLost(FocusEvent fev) {
//        if (!fev.isTemporary())
//                    stopCellEditing();
//            }
//        });
        return field;
    }

    //Note: must overide this to force cell object type, otherwise default is string
    public Object getCellEditorValue() {
        return field.getValue();
    }

    //Note: must overide this to allow full field selection when editing starts
    public boolean shouldSelectCell(EventObject evt) {
        DecimalField field1 = (DecimalField)editorComponent;
        field1.selectAll();
        return super.shouldSelectCell(evt);
    }

    //instance variables
    protected DecimalField field;
}
