package org.marketsuite.framework.model.type;

public enum ChartType {
    LineChart,
    CandleChart;

    //convert all into human readable string
    public static String[] toStrings() { return displayStrings; }
    private final static String[] displayStrings = {
            "Line Chart", "Candlestick Chart"
    };
    public static ChartType findType(String display_string) {
        if (display_string.equals(displayStrings[0]))
            return LineChart;
        else
            return CandleChart;
    }
}
