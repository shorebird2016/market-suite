package org.marketsuite.framework.strategy.base;

import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.StandardOption;
import org.marketsuite.framework.strategy.mac.MacOption;
import org.marketsuite.framework.strategy.macoscillator.MacOscillatorOption;
import org.marketsuite.framework.strategy.wmatl.WmaTlOption;
import org.marketsuite.simulator.indicator.macd.MzcOption;
import org.marketsuite.simulator.indicator.rsi.RsiOption;
import org.marketsuite.simulator.indicator.stochastic.StochasticOption;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.StandardOption;
import org.marketsuite.framework.strategy.macoscillator.MacOscillatorOption;
import org.marketsuite.framework.strategy.wmatl.WmaTlOption;
import org.marketsuite.simulator.indicator.macd.MzcOption;
import org.marketsuite.simulator.indicator.rsi.RsiOption;
import org.marketsuite.simulator.indicator.stochastic.StochasticOption;

//collection of trading rule parameters
public class SimParam {

    private StandardOption stdOptions;
    public StandardOption getStdOptions() {
        return stdOptions;
    }
    public SimParam(StandardOption std_opt, MacOption mac_opt) {
        stdOptions = std_opt;
        macOption = mac_opt;
    }
    public SimParam(StandardOption std_opt, MzcOption mzc_opt) {
        stdOptions = std_opt;
        mzcOption = mzc_opt;
    }
    public SimParam(StandardOption std_opt, RsiOption rsi_opt) {
        stdOptions = std_opt;
        rsiOption = rsi_opt;
    }
    public SimParam(StandardOption std_opt, StochasticOption sto_opt) {
        stdOptions = std_opt;
        stochasticOption = sto_opt;
    }
    public SimParam(StandardOption std_opt, MacOscillatorOption macosc_opt) {
        stdOptions = std_opt;
        macOscillatorOption = macosc_opt;
    }
    public SimParam(StandardOption std_opt, WmaTlOption wmatl_opt) {
        stdOptions = std_opt;
        wmaTlOption = wmatl_opt;
    }

    private MacOption macOption;
    public MacOption getMacOption() { return macOption; }

    private MzcOption mzcOption;
    public MzcOption getMzcOption() { return mzcOption; }
    public void setMzcOption(MzcOption mzcOption) { this.mzcOption = mzcOption; }

    private RsiOption rsiOption;
    public RsiOption getRsiOption() { return rsiOption; }
    public void setRsiOption(RsiOption rsiOption) { this.rsiOption = rsiOption; }

    private StochasticOption stochasticOption;
    public StochasticOption getStochasticOption() { return stochasticOption; }

    private MacOscillatorOption macOscillatorOption;
    public MacOscillatorOption getMacOscillatorOption() { return macOscillatorOption; }
    public void setMacOscillatorOption(MacOscillatorOption macOscillatorOption) { this.macOscillatorOption = macOscillatorOption; }

    private WmaTlOption wmaTlOption;
    public WmaTlOption getWmaTlOption() { return wmaTlOption; }
    public void setWmaTlOption(WmaTlOption wmaTlOption) { this.wmaTlOption = wmaTlOption; }

    //-----for pankin strategy-----
    private int entryWeek = 5;//number of weeks to compare price performance for initial entry, original 3
    public int getEntryWeek() {
        return entryWeek;
    }
    public void setEntryWeek(int entryWeek) {
        this.entryWeek = entryWeek;
    }

    private double topBracket = 0.25;//percent of funds that has top performance during last X weeks
    public double getTopBracket() {
        return topBracket;
    }
    public void setTopBracket(double topBracket) {
        this.topBracket = topBracket;
    }

    private double initialStop = 0.04F;//initial stop loss percent
    public double getInitialStop() {
        return initialStop;
    }
    public void setInitialStop(double initialStop) {
        this.initialStop = initialStop;
    }

    private double trailStop = 0.03F;//trailing stop percent, original 0.08
    public double getTrailStop() {
        return trailStop;
    }
    public void setTrailStop(double trailStop) {
        this.trailStop = trailStop;
    }

    private int evalWeek = 8;//number of weeks for performance comparision
    public int getEvalWeek() {
        return evalWeek;
    }
    public void setEvalWeek(int evalWeek) {
        this.evalWeek = evalWeek;
    }

    private int holdWeek = 5;//number of weeks to hold to avoid 0.75% fee
    public int getHoldWeek() {
        return holdWeek;
    }

    private boolean useStop = true;//true = use stop loss
    public boolean isUseStop() {
        return useStop;
    }
    public void setUseStop(boolean useStop) {
        this.useStop = useStop;
    }

    private FundData fund;
    public FundData getFund() {
        return fund;
    }
    public void setFund(FundData _fund) {
        fund = _fund;
    }

    private int atcLength; //ATR calc length
    public int getAtrLength() {
        return atcLength;
    }
    public void setAtrLength(int len) {
        atcLength = len;
    }

    private float brickSize;//specific for Renko
    public float getBrickSize() {
        return brickSize;
    }
    public void setBrickSize(float size) {
        brickSize = size;
    }
}