package org.marketsuite.framework.model.type;

public enum Zoom {
    Days7,
    Month1,
    Months3,
    Months6,
    Year1,
    All;

    //convert all into human readable string
    public static String[] toStrings() { return displayStrings; }
    private final static String[] displayStrings = {
            "7 Days", "1 Month", "3 Month",
            "6 Month", "1 Year", "Full Length"
    };

    public static Zoom findZoom(String display_string) {
        for (int i = 0; i < displayStrings.length; i++) {
            if (displayStrings[i].equals(display_string))
                return values()[i];
        }
        return All;
    }
}
