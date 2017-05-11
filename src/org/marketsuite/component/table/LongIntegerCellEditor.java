package org.marketsuite.component.table;

import java.awt.Component;
import java.util.EventObject;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.marketsuite.component.Constants;
import org.marketsuite.component.field.LongIntegerField;
import org.marketsuite.component.field.LongIntegerField;

/**
 * Implements a cell editor that uses a LongInterField to edit integer values.
 * When entry is out of range is entered the value will be set to the last known good value.
 */
public class LongIntegerCellEditor extends DefaultCellEditor {

	/**
     * CTOR: create editor object with custom verifier
     * @param default_value initial value
     * @param columns # of char in this field
     * @param min smallest inclusive
     * @param max largest inclusive
     */
    public LongIntegerCellEditor(long default_value, int columns, long min, long max) {
        super(new LongIntegerField(default_value, columns, min, max));
    }

    /**
     * Create editor object with supplied custom field type
     * @param custom_field based on LongIntegerField.
     */
    public LongIntegerCellEditor(LongIntegerField custom_field) {
        super(custom_field);
    }

    //Override to invoke setValue on the formatted text field.
    // and force cell to select all its content when in focus
	// Note: Must call setValue of LongIntegerfield to set last good value as default value.
	   public Component getTableCellEditorComponent(
	   		JTable table, Object value, boolean isSelected, int row, int column) {
		LongIntegerField field = (LongIntegerField)super.getTableCellEditorComponent(table, value, isSelected, row, column);
		field.setValue((Long)value);
        field.setHorizontalAlignment(SwingConstants.CENTER);
        field.setBackground(Constants.CELL_EDITING_BACKGROUND);
        return field;
    }

    //Note: must overide this to force cell object type, otherwise default is string
    public Object getCellEditorValue() {
        LongIntegerField field = (LongIntegerField)getComponent();
        return Long.valueOf(field.getValue());
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
