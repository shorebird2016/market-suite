package org.marketsuite.component.table;

import java.awt.Component;
import java.util.EventObject;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.marketsuite.component.Constants;
import org.marketsuite.component.field.MacAddressField;
import org.marketsuite.component.field.MacAddressField;

/**
 * Implements a cell editor that uses a text field to edit MAC address values. todo: might be a way to combine all editors into StringCellEditor.....
 */
public class MacAddressCellEditor extends DefaultCellEditor {
	//CTOR: create editor object with custom verifier
    public MacAddressCellEditor(String initial_value) {
        super(new MacAddressField(initial_value));
    }

    //Override to invoke setValue on the formatted text field.
    // and force cell to select all its content when in focus
    public Component getTableCellEditorComponent(
            JTable table, Object value, boolean isSelected, int row, int column) {
        JTextField field = (JTextField)super.getTableCellEditorComponent(table, value, isSelected, row, column);
        field.setBackground(Constants.CELL_EDITING_BACKGROUND);
        field.setHorizontalAlignment(SwingConstants.CENTER);
        return field;
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
