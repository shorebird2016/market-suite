package org.marketsuite.framework.strategy.wmatl;

/**
 * MAC + Oscillator strategy related simulation options.
 */
public class WmaTlOption {//Note: must have default CTOR, all get/set for XmlEncoder/XmlDecoder to work
    public WmaTlOption(int wma_period, int tl_period) {
        wmaPeriod = wma_period;
        trendlineRange = tl_period;
    }

    private int wmaPeriod;
    public int getWmaPeriod() {
        return wmaPeriod;
    }
    public void setWmaPeriod(int wmaPeriod) {
        this.wmaPeriod = wmaPeriod;
    }

    private int trendlineRange;
    public int getTrendlineRange() {
        return trendlineRange;
    }
    public void setTrendlineRange(int trendlineRange) {
        this.trendlineRange = trendlineRange;
    }

    //-----literals-----
    private static final String WMATL_SETTING = "mac-oscillator-setting";
}
