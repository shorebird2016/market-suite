package org.marketsuite.component.table;

import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.EventObject;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.marketsuite.component.Constants;

/**
 * A cell editor for JTable for editing typical text column. Without editor, enter key doesn't work.
 */
public class PlainCellEditor extends DefaultCellEditor {
	public PlainCellEditor() {
       super(new JTextField());
    }

    //Override to invoke setValue on the formatted text field.
    // and force cell to select all its content when in focus
    public Component getTableCellEditorComponent(
            JTable table, Object value, boolean isSelected, int row, int column) {
        field = (JTextField)super.getTableCellEditorComponent(table, value, isSelected, row, column);
        field.setHorizontalAlignment(SwingConstants.CENTER);
        field.setBackground(Constants.CELL_EDITING_BACKGROUND);
        //avoid editing stayed
        field.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent fev) {
                if (!fev.isTemporary())
                    stopCellEditing();
            }
        });
        return field;
    }

    //Note: must overide this to force cell object type, otherwise default is string
    public Object getCellEditorValue() {
        return field.getText();
    }

    //Note: must overide this to allow full field selection when editing starts
    public boolean shouldSelectCell(EventObject evt) {
        JTextField field1 = (JTextField)editorComponent;
        field1.selectAll();
        return super.shouldSelectCell(evt);
    }

    //private ProfileField field;
    private JTextField field;
}
