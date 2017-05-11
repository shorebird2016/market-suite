package org.marketsuite.component.table;

import java.awt.Component;
import java.util.EventObject;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentListener;
import org.marketsuite.component.Constants;
import org.marketsuite.component.field.BigDecimalField;

/**
 * A cell editor for JTable for editing large decimal numbers and retain accuracy.
 */
public class BigDecimalCellEditor extends DefaultCellEditor {
    /**
     * CTOR: construct this editor with parameters
     * @param value initial value, use double
     * @param columns width of cell
     * @param lower_limit lower bound use double
     * @param upper_limit upper bound use double
     * @param listener a type of DocumentListener
     */
	public BigDecimalCellEditor(double value, int columns,
                                double lower_limit, double upper_limit, DocumentListener listener) {
       super(new BigDecimalField(value, columns, lower_limit, upper_limit, listener));
    }

    //Override to invoke setValue on the formatted text field.
    // and force cell to select all its content when in focus
    public Component getTableCellEditorComponent(
            JTable table, Object value, boolean isSelected, int row, int column) {
        field = (BigDecimalField)super.getTableCellEditorComponent(table, value, isSelected, row, column);
        field.setHorizontalAlignment(SwingConstants.CENTER);
        field.setBackground(Constants.CELL_EDITING_BACKGROUND);
        return field;
    }

    //Note: must overide this to force cell object type, otherwise default is string
    public Object getCellEditorValue() {
        return field.getValue();
    }

    //Note: must overide this to allow full field selection when editing starts
    public boolean shouldSelectCell(EventObject evt) {
        BigDecimalField field1 = (BigDecimalField)editorComponent;
        field1.selectAll();
        return super.shouldSelectCell(evt);
    }

    //instance variables
    protected BigDecimalField field;
    private static final long serialVersionUID = -2018212839776811280L;
}
