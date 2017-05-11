package org.marketsuite.component.table;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.RowSorter;
import org.marketsuite.component.Constants;
import org.marketsuite.component.resource.LazyIcon;

//To render dirty column of any editable table, these cells store dirty state EDIT, DELETE, NORMAL in Long format
public class DirtyCellRenderer extends DynaTableCellRenderer {
    public DirtyCellRenderer(DynaTableModel table_model, RowSorter sorter) {
        super(table_model, sorter);
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setText("");
        label.setToolTipText("");
        if (EDIT.equals(value)) {
            label.setIcon(LazyIcon.ICON_UNDER_CONSTRUCTION);
            label.setToolTipText(Constants.COMPONENT_BUNDLE.getString("tip_001"));
        }
        else if (DELETE.equals(value)) {
            label.setIcon(LazyIcon.ICON_DELETED);
            label.setToolTipText(Constants.COMPONENT_BUNDLE.getString("tip_002"));
        }
        else if (ADD.equals(value)) {
            label.setIcon(LazyIcon.PLUS_SIGN_2);
            label.setToolTipText(Constants.COMPONENT_BUNDLE.getString("tip_003"));
        }
        return label;
    }

    //literals
    public static final String ADD = "ADD";
    public static final String EDIT = "EDIT";
    public static final String DELETE = "DELETE";
    public static final String NORMAL = "NORMAL";
}