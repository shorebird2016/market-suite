package org.marketsuite.component.table;

import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;

/**
 * Implements a cell editor that uses a combobox. Use DefaultCellEditor will not work because there is no
 * lost focus support.
 */
public class ComboCellEditor extends DefaultCellEditor {
	/**
     * CTOR: create editor object with custom verifier
     * @param combo to be added as editor component
     */
    public ComboCellEditor(JComboBox combo) {
        super(combo);
        setClickCountToStart(2);
    }

    //Override to invoke setValue on the formatted text field.
    // and force cell to select all its content when in focus
    public Component getTableCellEditorComponent(
            JTable table, Object value, boolean isSelected, int row, int column) {
        JComboBox combo = (JComboBox)super.getTableCellEditorComponent(table, value, isSelected, row, column);
        //combo.setBackground(Constants.CELL_EDITING_BACKGROUND);
        combo.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent fev) {
                if (!fev.isTemporary())
                    stopCellEditing();
            }
        });
        return combo;
    }

    //Note: must overide this to force cell object type, otherwise default is string
    public Object getCellEditorValue() {
        JComboBox field = (JComboBox)getComponent();
        return field.getSelectedItem();
    }
}
