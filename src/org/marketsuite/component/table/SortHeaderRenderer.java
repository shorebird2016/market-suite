package org.marketsuite.component.table;

import org.marketsuite.framework.resource.FrameworkConstants;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * This renderer renders table with sortable headers.
 */
public class SortHeaderRenderer extends HeadingRenderer {
	//CTOR:
    public SortHeaderRenderer() { }
    public SortHeaderRenderer(Font custom_font) { super(custom_font); }

    //interface implementations
    public Component getTableCellRendererComponent(JTable table, Object value,
           boolean isSelected, boolean hasFocus, int row, int column) {
        JTableHeader tableHeader = table.getTableHeader();
//        Color fg = null;
//        Color bg = null;
////        Border border = null;
        Icon icon = null;
//        if (hasFocus) {
//            fg = UIManager.getColor("TableHeader.focusCellForeground");
//            bg = UIManager.getColor("TableHeader.focusCellBackground");
////            border = UIManager.getBorder("TableHeader.focusCellBorder");
//        }
//        if (fg == null)
//            fg = tableHeader.getForeground();
//        if (bg == null)
//            bg = tableHeader.getBackground();
////        if (border == null)
////            border = UIManager.getBorder("TableHeader.cellBorder");
        if (!tableHeader.isPaintingForPrint() && table.getRowSorter() != null)
            icon = getSortIcon(table, table.convertColumnIndexToModel(column));
//        setFont(tableHeader.getFont());
//        setText(value != null && value != "" ? value.toString() : " ");
////        setBorder(border);
        setIcon(icon);
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//        return ret;
    }

    //protected, private methods
    protected Icon getSortIcon(JTable table, int column) {
        java.util.List<RowSorter.SortKey> sortKeys = (java.util.List<RowSorter.SortKey>) table.getRowSorter().getSortKeys();
        if (sortKeys == null || sortKeys.size() == 0)
            return null;

        if (sortKeys.get(0).getColumn() == column) {
            switch (sortKeys.get(0).getSortOrder()) {
                case ASCENDING:
                    return UIManager.getIcon("Table.ascendingSortIcon");
                case DESCENDING:
                    return UIManager.getIcon("Table.descendingSortIcon");
                case UNSORTED:
                    return UIManager.getIcon("Table.naturalSortIcon");
            }
        }
        return null;
    }

}
