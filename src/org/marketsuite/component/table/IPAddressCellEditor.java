package org.marketsuite.component.table;

import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.EventObject;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;

import org.marketsuite.component.field.IPAddressField;

public class IPAddressCellEditor extends DefaultCellEditor implements TableCellEditor {
	//CTOR
    public IPAddressCellEditor(IPAddressField field) {
        super(field);
        this.field = field;
    }

    public Object getCellEditorValue() {
        return field.getText();
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        field.setFont(table.getFont());
        field.setText((String) value);
        field.setHorizontalAlignment(SwingConstants.CENTER);
        field.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent fev) {
                if (!fev.isTemporary())
                    stopCellEditing();
            }
        });
        return field;
    }

    public boolean shouldSelectCell(EventObject evt) {
        field.selectAll();
        return super.shouldSelectCell(evt);
    }

    //instance variables
    private IPAddressField field;
}
