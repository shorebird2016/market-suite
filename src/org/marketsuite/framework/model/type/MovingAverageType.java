package org.marketsuite.framework.model.type;

public enum MovingAverageType {
    T_LINE(8, "T Line"),
    SMA_20(20, "20 SMA"),
    SMA_50(50, "50 SMA"),
    SMA_200(200, "200 SMA"),
    ;
    private int _nLength; String displayString;
    MovingAverageType(int length, String str) { _nLength = length; displayString = str; }
    public String toString() { return displayString; }
}
