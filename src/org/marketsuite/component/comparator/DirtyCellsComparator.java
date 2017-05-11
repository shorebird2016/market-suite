package org.marketsuite.component.comparator;

import java.io.Serializable;
import java.util.Comparator;

import org.marketsuite.component.table.SimpleCell;

/**
 * A comparator used in table for sorting "dirty" column.
 */
public class DirtyCellsComparator implements Comparator<Object> , Serializable{
	public int compare(Object o1, Object o2) {
        String str1 = "";
        String str2 = "";
        SimpleCell[] o1_cells;
        SimpleCell[] o2_cells;
        if (null != o1) {
            if (o1 instanceof SimpleCell[]) {
                o1_cells = (SimpleCell[]) o1;
                for (SimpleCell cell : o1_cells) {
                    if (cell.isDirty())
                        str1 = "dirty";
                }
            }
        }
        if (null != o2) {
            if (o2 instanceof SimpleCell[]) {
                o2_cells = (SimpleCell[]) o2;
                for (SimpleCell cell : o2_cells) {
                    if (cell.isDirty())
                        str2 = "dirty";
                }
            }
        }
        return str1.compareTo(str2);
    }
}
