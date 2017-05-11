package org.marketsuite.main.datapad;

/**
 * Data structure to hold charting point.
 */
public class ChartData {
    public final double prevPrice;
    public final double openPrice;
    public final double lastPrice;
    public final double highPrice;
    public final double lowPrice;
    public final long volume;
    // We choose not to use either Calendar or Joda DateTime, as we feel is too
    // heavy weight. We do not need time zone information.
    public final long timestamp;

    private ChartData(double prevPrice, double openPrice, double lastPrice, double highPrice, double lowPrice, long volume, long timestamp) {
        this.prevPrice = prevPrice;
        this.openPrice = openPrice;
        this.lastPrice = lastPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.volume = volume;
        this.timestamp = timestamp;
    }

    /**
     * Creates an instance of chart data.
     *
     * @param prevPrice previous price
     * @param openPrice open price
     * @param lastPrice last price
     * @param highPrice high price
     * @param lowPrice low price
     * @param volume the volume
     * @param timestamp the timestamp
     * @return an instance of chart data
     */
    public static ChartData newInstance(double prevPrice, double openPrice, double lastPrice, double highPrice, double lowPrice, long volume, long timestamp) {
        return new ChartData(prevPrice, openPrice, lastPrice, highPrice, lowPrice, volume, timestamp);
    }
}
