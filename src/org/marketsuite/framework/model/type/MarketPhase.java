package org.marketsuite.framework.model.type;

public enum MarketPhase {
    Bullish("BUL", "Bullish Phase"),
    WeakWarning("WW", "Weak Warning Phase"),
    StrongWarning("SW", "Strong Warning Phase"),
    Distribution("DIS", "Distribution Phase"),
    Bearish("BEAR", "Bearish Phase"),
    Recovery("RCVY", "Recovery Phase"),
    Accumulation("ACCM", "Accumulation Phase"),
    Unknown("UNO", "Unknown Phase"),
    ;

    //----- CTOR -----
    MarketPhase(String dsp_str, String descr) { displayString = dsp_str; description = descr; }
    public String toString() { return displayString; }
    public String getDescription() { return description; }

    //----- variables -----
    private String displayString, description;
}
