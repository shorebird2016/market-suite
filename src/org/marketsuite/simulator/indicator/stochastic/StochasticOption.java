package org.marketsuite.simulator.indicator.stochastic;

import org.jdom.Element;

public class StochasticOption {
    public StochasticOption() {}
    public StochasticOption(int length, int ma_period, int oversold, int overbought) {
        this.length = length;
        this.maPeriod = ma_period;
        this.oversold = oversold;
        this.overbought = overbought;
    }

    public StochasticOption(Element element) {

    }

    public Element objToXml() {
        Element ret = new Element(STOCHASTIC_SETTING);
        return ret;
    }

    private int length = DEFAULT_LENGTH;
    public int getLength() {
        return length;
    }

    private int maPeriod = DEFAULT_MA_PERIOD;
    public int getMaPeriod() {
        return maPeriod;
    }

    private int oversold = OVERSOLD_LEVEL;
    public int getOversold() {
        return oversold;
    }

    private int overbought = OVERBOUGHT_LEVEL;
    public int getOverbought() {
        return overbought;
    }

    //-----literals-----
    private static final String STOCHASTIC_SETTING = "stochastic-setting";
    public static final int DEFAULT_LENGTH = 14;
    public static final int DEFAULT_MA_PERIOD = 3;
    public static final int OVERSOLD_LEVEL = 20;
    public static final int OVERBOUGHT_LEVEL = 80;
}
