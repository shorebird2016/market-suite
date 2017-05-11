package org.marketsuite.component.table;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Table cell renderer that draws icons.
 */
public class IconicTableCellRenderer extends DefaultTableCellRenderer {
	public IconicTableCellRenderer() {
        this(false);
    }

    public IconicTableCellRenderer(boolean toolTips) {
        setUseToolTips(toolTips);
    }

    public void setValue(Object value) {
        if (value instanceof Icon) {
            setIcon((Icon) value);
        } else {
            super.setValue(value);
        }
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        if (getUseToolTips())
            setToolTipValue(table.getModel().getValueAt(row, table.convertColumnIndexToModel(column)));
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }

    public boolean getUseToolTips() {
        return useToolTips;
    }

    public void setUseToolTips(boolean b) {
        useToolTips = b;
    }

    public void setToolTipValue(Object value) {
        setToolTipText(value.toString());
    }

    private boolean useToolTips;
}
