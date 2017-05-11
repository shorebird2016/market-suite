package org.marketsuite.component.comparator;

import java.io.Serializable;
import java.util.Comparator;

/**
 * A comparator used in table for sorting double type column.
 */
public class DoubleComparator implements Comparator<Object> , Serializable{
	protected String getStr(Object obj) {
        String str = "";
        if (obj == null) {
            return str;
        } else if (obj instanceof Double) {
            str = Double.toString((Double)obj);
        } else if (obj instanceof String) {
            str = obj.toString();
        }
        return str;
    }

    public int compare(Object o1, Object o2) {
        String id1;
        String id2;
        id1 = getStr(o1);
        id2 = getStr(o2);
        if (id1.length()>0 && id2.length()>0) {
            Double l1 = Double.parseDouble(id1);
            Double l2 = Double.parseDouble(id2);
            return l1.compareTo(l2);
        }
        return id1.compareTo(id2);
    }
}

