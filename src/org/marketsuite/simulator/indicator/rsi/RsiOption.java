package org.marketsuite.simulator.indicator.rsi;

import org.jdom.Element;

public class RsiOption {
    public RsiOption() { }
    public RsiOption(int length, int oversold, int overbought) {
        this.length = length;
        this.oversold = oversold;
        this.overbought = overbought;
    }

    public RsiOption(Element element) {

    }

    public Element objToXml() {
        Element ret = new Element(RSI_SETTING);
        return ret;
    }

    private int length = DEFAULT_LENGTH;
    public int getLength() {
        return length;
    }
    public void setLength(int length) {
        this.length = length;
    }

    private int oversold = OVERSOLD_LEVEL;
    public int getOversold() {
        return oversold;
    }
    public void setOversold(int oversold) {
        this.oversold = oversold;
    }

    private int overbought = OVERBOUGHT_LEVEL;
    public int getOverbought() {
        return overbought;
    }
    public void setOverbought(int overbought) {
        this.overbought = overbought;
    }

    //-----literals-----
    private static final String RSI_SETTING = "rsi-setting";
    public static final int DEFAULT_LENGTH = 14;
    public static final int OVERSOLD_LEVEL = 30;
    public static final int OVERBOUGHT_LEVEL = 70;
}
