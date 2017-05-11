package org.marketsuite.simulator.advanced.report.model;

import org.marketsuite.framework.strategy.mac.MacOption;
import org.marketsuite.simulator.indicator.macd.MzcOption;
import org.marketsuite.simulator.indicator.rsi.RsiOption;
import org.marketsuite.simulator.indicator.stochastic.StochasticOption;
import org.jdom.Element;

/**
 * Stores various strategies setting for simulation.
 */
public class StrategySetting {
    public StrategySetting() {}
    public StrategySetting(MacOption mac_setting, MzcOption mzc_setting, RsiOption rsi_setting, StochasticOption sto_setting) {
        macSetting = mac_setting;
        mzcSetting = mzc_setting;
        rsiSetting = rsi_setting;
        stochasticSetting = sto_setting;
    }
    /**
     * CTOR: construct this object from xml
     * @param element xml
     */
    public StrategySetting(Element element) {
        macSetting = new MacOption(element.getChild(MAC_SETTING));
        mzcSetting = new MzcOption(element.getChild(MZC_SETTING));
        rsiSetting = new RsiOption(element.getChild(RSI_SETTING));
        stochasticSetting = new StochasticOption(element.getChild(STOCHASTIC_SETTING));
    }

    //-----public methods-----
    public Element objToXml() {
        Element ret = new Element(STRATEGY_SETTING);
        ret.addContent(macSetting.objToXml());
        ret.addContent(mzcSetting.objToXml());
        ret.addContent(rsiSetting.objToXml());
        ret.addContent(stochasticSetting.objToXml());
        return ret;
    }

    //-----instance variables / accessors-----
    private MacOption macSetting;
    public MacOption getMacSetting() {
        return macSetting;
    }
    public void setMacSetting(MacOption macSetting) {
        this.macSetting = macSetting;
    }

    private MzcOption mzcSetting;
    public MzcOption getMzcSetting() {
        return mzcSetting;
    }
    public void setMzcSetting(MzcOption mzcSetting) {
        this.mzcSetting = mzcSetting;
    }

    private RsiOption rsiSetting;
    public RsiOption getRsiSetting() {
        return rsiSetting;
    }
    public void setRsiSetting(RsiOption rsiSetting) {
        this.rsiSetting = rsiSetting;
    }

    private StochasticOption stochasticSetting;
    public StochasticOption getStochasticSetting() {
        return stochasticSetting;
    }
    public void setStochasticSetting(StochasticOption stochasticSetting) {
        this.stochasticSetting = stochasticSetting;
    }

    //-----literals-----
    public static final String STRATEGY_SETTING = "strategy-setting";
    private static final String MAC_SETTING = "mac-setting";
    private static final String MZC_SETTING = "mzc-setting";
    private static final String RSI_SETTING = "rsi-setting";
    private static final String STOCHASTIC_SETTING = "stochastic-setting";

}