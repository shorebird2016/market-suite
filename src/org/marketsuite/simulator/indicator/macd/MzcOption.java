package org.marketsuite.simulator.indicator.macd;

import org.jdom.Element;

/**
 * MAC strategy related simulation options.
 */
public class MzcOption {
    public MzcOption() { }
    public MzcOption(int fastMA, int slowMA) {
        this.fastMA = fastMA;
        this.slowMA = slowMA;
    }

    public MzcOption(int fastMA, int slowMA, int smoothPeriod) {
        this.fastMA = fastMA;
        this.slowMA = slowMA;
        this.smoothPeriod = smoothPeriod;
    }

    public MzcOption(Element element) {

    }

    public Element objToXml() {
        Element ret = new Element(MZC_SETTING);
        return ret;
    }

    private int fastMA = 12;
    public int getFastMA() {
        return fastMA;
    }
    public void setFastMA(int fastMA) {
        this.fastMA = fastMA;
    }

    private int slowMA = 26;
    public int getSlowMA() {
        return slowMA;
    }
    public void setSlowMA(int slowMA) {
        this.slowMA = slowMA;
    }

    private int smoothPeriod;//moving average to smooth both lines
    public int getSmoothPeriod() { return smoothPeriod; }
    public void setSmoothPeriod(int smoothPeriod) { this.smoothPeriod = smoothPeriod; }

    //-----literals-----
    private static final String MZC_SETTING = "mzc-setting";
}
