package org.marketsuite.component.table;

import java.awt.*;
import javax.swing.*;

/**
 * Simple renderer indicating existence of leading/trailing blanksSimple renderer indicating existence of leading/trailing blanks
 */
public class NameCellRenderer extends DynaTableCellRenderer {
    public NameCellRenderer(DynaTableModel model) {
        super(model);
    }

    public Component getTableCellRendererComponent(
            JTable table1, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
        Component comp = super.getTableCellRendererComponent(table1, value, isSelected, hasFocus, row, col);
        if (comp instanceof JLabel) {
            JLabel lbl = (JLabel)comp;
            String txt = lbl.getText();
            StringBuilder sb = new StringBuilder();
            if (txt.startsWith(" "))
                sb.append("#").append(txt);
            else
                sb.append(txt);
            if (txt.endsWith(" "))
                sb.append("#");
            lbl.setText(sb.toString());
        }
        return comp;
    }
}
