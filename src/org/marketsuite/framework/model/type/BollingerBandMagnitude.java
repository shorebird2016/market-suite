package org.marketsuite.framework.model.type;

public enum BollingerBandMagnitude {
    LargerThan3X(">= 3 Std Dev"),
    LargerThan2X(">= 2 Std Dev"),
    Normal(" Normal "),
    ;

    BollingerBandMagnitude(String disp) { displayString = disp; }
    //convert all into human readable string
    public String toString() { return displayString; }
    private String displayString;
}
