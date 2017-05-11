package org.marketsuite.component.table;

import java.awt.Component;
import java.util.EventObject;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.marketsuite.component.Constants;
import org.marketsuite.component.field.HexByteField;
import org.marketsuite.component.field.HexByteField;

/**
 * Implements a cell editor that uses a text field to edit hex values.
 */
public class HexByteCellEditor extends DefaultCellEditor {
    /**
	 * 
	 */
	private static final long serialVersionUID = 2558157827514429061L;

	/**
     * CTOR: create editor object with custom verifier
     * @param initial_value initial value
     * @param even_only true = only allow even numbers
     */
    public HexByteCellEditor(String initial_value, boolean even_only) {
        super(new HexByteField(initial_value, even_only));
    }

    //Override to invoke setValue on the formatted text field.
    // and force cell to select all its content when in focus
    public Component getTableCellEditorComponent(
            JTable table, Object value, boolean isSelected, int row, int column) {
        JTextField field = (JTextField)super.getTableCellEditorComponent(table, value, isSelected, row, column);
        field.setHorizontalAlignment(SwingConstants.CENTER);
        field.setBackground(Constants.CELL_EDITING_BACKGROUND);
        return field;
    }

    //Note: must overide this to force cell object type, otherwise default is string
    public Object getCellEditorValue() {
        HexByteField field = (HexByteField)getComponent();
        return field.getText();
    }

    //Note: must use this to verify entry
    public boolean stopCellEditing() {//out of editing field
        if (editorComponent.getInputVerifier().verify(editorComponent)) {
            return super.stopCellEditing();
        }
        return false;
    }

    //Note: must overide this to allow full field selection when editing starts
    public boolean shouldSelectCell(EventObject evt) {
        JTextField field = (JTextField)editorComponent;
        field.selectAll();
        return super.shouldSelectCell(evt);
    }
}
