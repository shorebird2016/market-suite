package org.marketsuite.component.comparator;

import java.io.Serializable;
import java.util.Comparator;

/**
 * A comparator used in table for sorting short integer type of columns.
 */
public class IntegerComparator implements Comparator<Object>, Serializable {
    public int compare(Object o1, Object o2) {
        String id1;
        String id2;
        id1 = getStr(o1);
        id2 = getStr(o2);
        if (id1.length()>0 && id2.length()>0) {
            Integer l1 = Integer.parseInt(id1);
            Integer l2 = Integer.parseInt(id2);
            return l1.compareTo(l2);
        }
        return id1.compareTo(id2);
    }
    protected String getStr(Object obj) {
        String str = "";
        if (obj == null) {
            return str;
        } else if (obj instanceof Long) {
            str = Long.toString((Long)obj);
        } else if (obj instanceof Integer) {
            str = Integer.toString((Integer)obj);
        } else if (obj instanceof Short) {
            str = Short.toString((Short)obj);
        } else if (obj instanceof String) {
            str = obj.toString();
        }
        return str;
    }
}

