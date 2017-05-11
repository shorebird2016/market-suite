package org.marketsuite.simulator.advanced.indicatorsim;

import org.marketsuite.framework.strategy.mac.MacOption;
import org.marketsuite.simulator.indicator.macd.MzcOption;
import org.marketsuite.simulator.indicator.rsi.RsiOption;
import org.marketsuite.simulator.indicator.stochastic.StochasticOption;
import org.marketsuite.simulator.advanced.report.model.TimeSetting;
import org.marketsuite.simulator.advanced.report.model.TimeSetting;
import org.marketsuite.simulator.indicator.macd.MzcOption;
import org.marketsuite.simulator.indicator.rsi.RsiOption;
import org.marketsuite.simulator.indicator.stochastic.StochasticOption;

/**
 * Collection of parameters used during advanced simulation / report generation.
 * One for each strategy plus time setting.
 */
class ReportSetting {
    ReportSetting(TimeSetting time_setting, MacOption mac_option, MzcOption mzc_option,
                         RsiOption rsi_setting, StochasticOption sto_setting) {
        timeSetting = time_setting;
        macOption = mac_option;
        mzcOption = mzc_option;
        rsiOption = rsi_setting;
        stochasticOption = sto_setting;
    }

    //time setting
    private TimeSetting timeSetting;
    public TimeSetting getTimeSetting() {
        return timeSetting;
    }
    public void setTimeSetting(TimeSetting timeSetting) {
        this.timeSetting = timeSetting;
    }

    //MAC setting
    private MacOption macOption;
    public MacOption getMacOption() {
        return macOption;
    }
    public void setMacOption(MacOption macOption) {
        this.macOption = macOption;
    }

    //MACD zero cross setting
    private MzcOption mzcOption;
    public MzcOption getMzcOption() {
        return mzcOption;
    }
    public void setMzcOption(MzcOption mzcOption) {
        this.mzcOption = mzcOption;
    }

    //RSI setting
    private RsiOption rsiOption;
    public RsiOption getRsiOption() {
        return rsiOption;
    }
    public void setRsiOption(RsiOption rsiOption) {
        this.rsiOption = rsiOption;
    }

    //Stochastic setting
    private StochasticOption stochasticOption;
    public StochasticOption getStochasticOption() {
        return stochasticOption;
    }
    public void setStochasticOption(StochasticOption stochasticOption) {
        this.stochasticOption = stochasticOption;
    }
}
