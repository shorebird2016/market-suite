package org.marketsuite.component.table;

import java.awt.Color;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;

import javax.swing.Icon;

//A collection of column types used among subnet tables.
public class ColumnTypeEnum {
    //Returns java type of a given column type.
    public static Class getClassForType(int type) {
        if (type < TYPE_ICON || type > TYPE_DATE)
            throw new IllegalArgumentException("Bad Column Type -> " + type);
        return (Class)TYPE_MAP.get(Integer.valueOf(type));
    }

    //column types
    public static final int TYPE_ICON = 210;
    public static final int TYPE_BOOLEAN = 220;
    public static final int TYPE_LIST = 230;
    public static final int TYPE_LONG = 240;
    public static final int TYPE_HEX_BYTE = 250;
    public static final int TYPE_HEX_WORD = 260;//not used
    public static final int TYPE_IPV4_ADDRESS = 270;
    public static final int TYPE_NETMASK = 300;
    public static final int TYPE_COLOR = 310;
    public static final int TYPE_STRING = 320;
    public static final int TYPE_LOAD_PROFILE = 330;
    public static final int TYPE_STATIC_STRING = 340;
    public static final int TYPE_GROUP_STRING = 350;
    public static final int TYPE_STATIC_LONG = 360;
    public static final int TYPE_DOUBLE = 370;
    public static final int TYPE_BIG_DECIMAL = 280;
    public static final int TYPE_DATE = 380;


    //map from column types to java types
    public final static HashMap TYPE_MAP = new HashMap();

    static {//initialize map
        TYPE_MAP.put(Integer.valueOf(TYPE_ICON), Icon.class);
        TYPE_MAP.put(Integer.valueOf(TYPE_BOOLEAN), Boolean.class);
        TYPE_MAP.put(Integer.valueOf(TYPE_LIST), String.class);
        TYPE_MAP.put(Integer.valueOf(TYPE_LONG), Long.class);
        TYPE_MAP.put(Integer.valueOf(TYPE_HEX_BYTE), String.class);
        TYPE_MAP.put(Integer.valueOf(TYPE_HEX_WORD), String.class);
        TYPE_MAP.put(Integer.valueOf(TYPE_IPV4_ADDRESS), String.class);
        TYPE_MAP.put(Integer.valueOf(TYPE_NETMASK), Long.class);
        TYPE_MAP.put(Integer.valueOf(TYPE_COLOR), Color.class);
        TYPE_MAP.put(Integer.valueOf(TYPE_STRING), String.class);
        TYPE_MAP.put(Integer.valueOf(TYPE_STATIC_STRING), String.class);
        TYPE_MAP.put(Integer.valueOf(TYPE_STATIC_LONG), Long.class);
        TYPE_MAP.put(Integer.valueOf(TYPE_GROUP_STRING), String.class);
        TYPE_MAP.put(Integer.valueOf(TYPE_DOUBLE), Double.class);
        TYPE_MAP.put(Integer.valueOf(TYPE_BIG_DECIMAL), BigDecimal.class);
        TYPE_MAP.put(Integer.valueOf(TYPE_DATE), Date.class);
    }
}
