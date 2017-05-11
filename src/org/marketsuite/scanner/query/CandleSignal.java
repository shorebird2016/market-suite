package org.marketsuite.scanner.query;

public enum CandleSignal {
    DOJI_SPINTOP  ("Doji / Spinning Top"),
    ENGULF        ("Engulfing"),
    HARAMI        ("Harami"),
    ;

    CandleSignal(String display_string) { displayString = display_string; }
    public String toString() { return displayString; }
    private String displayString;
}
