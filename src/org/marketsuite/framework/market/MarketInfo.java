package org.marketsuite.framework.market;

import org.marketsuite.framework.model.Divergence;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.indicator.BollingerBand;
import org.marketsuite.framework.util.IndicatorUtil;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.indicator.BollingerBand;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * A data structure that encapsulate phase and condition changes information after scan
 */
public class MarketInfo {
    public MarketInfo() {}
    public MarketInfo(String _symbol, FundData _fund, int start_index) {
        symbol = _symbol;
        fund = _fund;
        startIndex = start_index;
    }

    //----- public methods -----
    /**
     * Append a new record to phaseChange array
     * @param index of matching quote array of this symbol
     * @param from this phase based on AppConstants.PHASE_XXX definition
     * @param to this phase
     * @param calendar occur date
     */
    public void addPhaseChange(int index, int from, int to, Calendar calendar) {
        ChangeInfo chg = new ChangeInfo(index, from, to, calendar);
        phaseChanges.add(chg);
    }
    /**
     * Append a new record to conditionChanges21 array
     * @param condition21 true = condition 2 to condition 1, otherwise condition 4 to condition 3
     * @param index of matching quote array of this symbol
     * @param from this condition based on AppConstants.CONDITION_XXX definition
     * @param to this condition
     * @param calendar date of change
     */
    public void addConditionChange(boolean condition21, int index, int from, int to, Calendar calendar) {
        ChangeInfo chg = new ChangeInfo(index, from, to, calendar);
        if (condition21)
            conditionChanges21.add(chg);
        else
            conditionChanges43.add(chg);
    }
    public void recalcMacd(int fastMa, int slowMa, int smoothMa) {
        macd = IndicatorUtil.calcMACD(fastMa, slowMa, fund.getSize() - 1 - slowMa, fund);
    }
    public void recalcDsto() {}
    public void recalcBollingerBand(int period, int upper_spec, int lower_spec) {
        bollingerBand = new BollingerBand(period, upper_spec, lower_spec, fund.getQuote());
    }

    //----- accessors -----
    public String getSymbol() { return symbol; }
    public FundData getFund() { return fund; }
    public void setStartIndex(int startIndex) { this.startIndex = startIndex; }
    public int getStartIndex() { return startIndex; }
    public void setSma10(float[] sma10) { this.sma10 = sma10; }
    public float[] getSma10() { return sma10; }
    public void setSma20(float[] sma10) { this.sma20 = sma10; }
    public float[] getSma20() { return sma20; }
    public void setSma30(float[] sma30) { this.sma30 = sma30; }
    public float[] getSma30() { return sma30; }
    public void setSma50(float[] sma50) { this.sma50 = sma50; }
    public float[] getSma50() { return sma50; }
    public void setSma200(float[] sma200) { this.sma200 = sma200; }
    public float[] getSma200() { return sma200; }
    public void setEma8(float[] ema) { ema8 = ema; }
    public float[] getEma8() { return ema8; }
    public void setEma50(float[] ema) { ema50 = ema; }
    public float[] getEma50() { return ema50; }
    public void setEma120(float[] ema) { ema120 = ema; }
    public float[] getEma120() { return ema120; }
    public void setEma200(float[] ema) { ema200 = ema; }
    public float[] getEma200() { return ema200; }
    public float[] getRsi() { return rsi; }
    public void setRsi(float[] rsi) { this.rsi = rsi; }
    public float[] getMacd() { return macd; }
    public void setMacd(float[] macd) { this.macd = macd; }
    public float[] getMacdSig() { return macdSig; }
    public void setMacdSig(float[] sig) { this.macdSig = sig; }
    public float[] getDsto() { return dsto; }
    public void setDsto(float[] dsto) { this.dsto = dsto; }
    public float[] getRsiStd() { return rsiStd; }
    public void setRsiStd(float[] rsiStd) { this.rsiStd = rsiStd; }
    public float[] getMacdStd() { return macdStd; }
    public void setMacdStd(float[] macdStd) { this.macdStd = macdStd; }
    public float[] getMacdStdSig() { return macdStdSig; }
    public void setMacdStdSig(float[] sig) { this.macdStdSig = sig; }
    public float[] getDstoStd() { return dstoStd; }
    public void setDstoStd(float[] dstoStd) { this.dstoStd = dstoStd; }
    public float[] getVolumeAverage() { return volumeAverage; }
    public void setVolumeAverage(float[] volumeAverage) { this.volumeAverage = volumeAverage; }
    public BollingerBand getBollingerBand() { return bollingerBand; }
    public void setBollingerBand(BollingerBand bollingerBand) { this.bollingerBand = bollingerBand; }
    public String getCurrentPhase() { return currentPhase; }
    public void setCurrentPhase(String _phase) { currentPhase = _phase; }
    public int getCurrentCondition() { return currentCondition; }
    public void setCurrentCondition(int _recentCondition) { currentCondition = _recentCondition; }
    public ArrayList<ChangeInfo> getPhaseChanges() { return phaseChanges; }
    public ArrayList<ChangeInfo> getConditionChanges21() { return conditionChanges21; }
    public ArrayList<ChangeInfo> getConditionChanges43() { return conditionChanges43; }
    public void addCrossOver10x30Date(String date) { crossOver10x30Dates.add(date); }
    public ArrayList<String> getCrossOver10x30Dates() { return crossOver10x30Dates; }
    public void addCrossOver50x120Date(String date) { crossOver50x120Dates.add(date); }
    public ArrayList<String> getCrossOver50x120Dates() { return crossOver50x120Dates; }
    public Divergence getDvg() { return dvg; }
    public void setDvg(Divergence d) { dvg = d; }
    public String getHighestDate() { return highestDate; }
    public void setHighestDate(String cal) { highestDate = cal; }
    //convert various PHASE_XXX to LIST_PHASE strings
    public static String phaseIdToString(int id) {
        return LIST_PHASE[id - PHASE_BULLISH];
    }

    //----- variables -----
    private String symbol;
    private FundData fund;
    private int startIndex;//first data point for calculation
    private float[] sma10, sma20, sma30, sma50, sma200;//keep them for convenience
    private float[] ema8, ema50, ema120, ema200;
    private float[] rsi, macd, dsto;//DCOM indicators
    private float[] macdSig;
    private float[] rsiStd, macdStd, dstoStd;//Standard indicators
    private float[] macdStdSig;
    private float[] volumeAverage;
    private BollingerBand bollingerBand;
    private String currentPhase;
    private int currentCondition;
    private ArrayList<ChangeInfo> phaseChanges = new ArrayList<>();
    private ArrayList<ChangeInfo> conditionChanges21 = new ArrayList<>();//from condition 2 to 1
    private ArrayList<ChangeInfo> conditionChanges43 = new ArrayList<>();//from condition 4 to 3
    private ArrayList<String> crossOver10x30Dates = new ArrayList<>();
    private ArrayList<String> crossOver50x120Dates = new ArrayList<>();
    private ArrayList<String> priceCrossDates = new ArrayList<>();
    private Divergence dvg;
    private String highestDate;//highest point of this entire quote

    //----- literals -----
    public static final String[] LIST_PHASE = {
            "Bullish",
            "Recovery",
            "Accumulation",
            "Weak Warning",
            "Strong Warning",
            "Distribution",
            "Bearish",
    };
    public static final int PHASE_BULLISH = 100;
    public static final int PHASE_RECOVERY = 101;
    public static final int PHASE_ACCUMULATION = 102;
    public static final int PHASE_WEAK_WARNING = 103;
    public static final int PHASE_STRONG_WARNING = 104;
    public static final int PHASE_DISTRIBUTION = 105;
    public static final int PHASE_BEARISH = 106;
    //market conditions
    public static final int CONDITION_1 = 1;
    public static final int CONDITION_2 = 2;
    public static final int CONDITION_3 = 3;
    public static final int CONDITION_4 = 4;
    public static final int CONDITION_NA = -1;
}
