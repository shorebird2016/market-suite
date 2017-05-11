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
import org.marketsuite.component.field.NameField;

/**
 * A cell editor for JTable for editing typical name column(using NameFiled). Without editor, enter key doesn't work.
 */
public class NameCellEditor extends DefaultCellEditor {
	public NameCellEditor() {
       super(new NameField());
    }

    public NameCellEditor(boolean allow_blank) {
        super(new NameField(10, allow_blank));
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
       if(editorComponent instanceof JTextField) { // prevents class cast exception
          JTextField field1 = (JTextField)editorComponent;
          field1.selectAll();
       }
        return super.shouldSelectCell(evt);
    }

    //private ProfileField field;
    private JTextField field;
}