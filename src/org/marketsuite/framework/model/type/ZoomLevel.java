package org.marketsuite.framework.model.type;

public enum ZoomLevel {
    Month1("1 Month"),
    Month2("2 Month"),
    Month3("3 Month"),
    Month6("6 Month"),
    Month9("9 Month"),
    Year1("1 Year"),
    Year1_Half("1.5 Year"),
    Year2("2 Year"),
    Year3("3 Year"),
    Year5("5 Year"),
    Year8("8 Year"),
    Max("Full Range")
    ;

    ZoomLevel(String display_str) { displayString = display_str; }
    public String toString() { return displayString; }
    public static ZoomLevel findLevel(int order) {
        ZoomLevel[] consts = ZoomLevel.class.getEnumConstants();
        return consts[order];
    }
    public String displayString;
}
