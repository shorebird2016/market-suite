package org.marketsuite.framework.model.type;

//Used in graph frame specifying data pad positions on screen
public enum DatapadMode {
    HIDE,
    FOLLOW_DOT,
    FIXED_CORNER
    ;
    public static DatapadMode nextMode(DatapadMode cur_mode) {
        DatapadMode[] modes = values();
        int cur_ordinal = cur_mode.ordinal();
        if (cur_ordinal == (modes.length - 1))
            return values()[0];
        return values()[cur_ordinal + 1];
    }
}
