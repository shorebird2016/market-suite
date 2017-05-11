package org.marketsuite.framework.model.indicator;

import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.type.MarketTrend;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.CandleUtil;
import org.marketsuite.framework.util.IndicatorUtil;
import org.jdom.Element;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.type.MarketTrend;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.IndicatorUtil;

import java.util.ArrayList;

//model after 5 lines under this famous indicator
public class Ichimoku implements Indicator {
    //CTOR: quotes must be split adjusted, start must be larger than end index
    public Ichimoku(ArrayList<FundQuote> quotes, int start_index, int end_index) {
        _Quotes = quotes; _nStartIndex = start_index; _nEndIndex = end_index;
        calcSen(periodFast, tenkan = new float[quotes.size()]);
        calcSen(periodMedium, kijun = new float[quotes.size()]);
        senkouA = senkouB = chikou = new float[quotes.size()];
        for (int idx = end_index; idx <= start_index; idx++) {//senkouA shifts forward
            int shift_idx = idx + periodMedium;
            if (shift_idx >= quotes.size()) continue;//out of bound
            senkouA[idx] = (tenkan[shift_idx] + kijun[shift_idx]) / 2;
        }
        calcSen(periodSlow, senkouB = new float[quotes.size()]);//need to shift
        for (int idx = end_index; idx <= start_index; idx++) {//chikou shifts forward
            int shift_idx = idx + periodMedium;
            if (shift_idx >= quotes.size()) continue;
            senkouB[idx] = senkouB[shift_idx];
        }
        for (int idx = end_index; idx <= start_index; idx++) {//chikou shifts close back
            int shift_idx = idx + periodMedium;
            if (idx < end_index + periodMedium) chikou[idx] = -1;//no data
            if (shift_idx >= quotes.size()) continue;
            chikou[shift_idx] = quotes.get(idx).getClose();
        }
    }

    //----- interface / overrides -----
    public String getId() { return ID; }
    public Element objToXml() { return null; }
    public float[] getValues() { return new float[0]; }
    public FundData getQuotes() { return null; }

    //----- public methods -----
    //Note: index must be in quote range!
    public boolean isAboveCloud(int index) { return isAboveKijun(index) && isAboveKumoA(index) && isAboveKumoB(index); }
    public boolean isAboveKumoA(int index) { return _Quotes.get(index).getClose() > senkouA[index]; }
    public boolean isAboveKumoB(int index) { return _Quotes.get(index).getClose() > senkouB[index]; }
    public boolean isAboveKijun(int index) { return _Quotes.get(index).getClose() > kijun[index]; }
    public boolean isKumoBullish(int index) { return senkouA[index] > senkouB[index]; }
    public boolean isNearKijun(int index, float threshold) { return AppUtil.deltaExceedThreshold(_Quotes.get(index).getClose(), kijun[index], threshold) != Float.NaN; }
    public boolean isNearKumoA(int index, float threshold) { return AppUtil.deltaExceedThreshold(_Quotes.get(index).getClose(), senkouA[index], threshold) != Float.NaN; }
    public boolean isNearKumoB(int index, float threshold) { return AppUtil.deltaExceedThreshold(_Quotes.get(index).getClose(), senkouB[index], threshold) != Float.NaN; }
    public MarketTrend getKijunTrend(int start_index, int end_index) { return CandleUtil.determineTrend(kijun, start_index, end_index); }
    public MarketTrend getKumoATrend(int start_index, int end_index) { return CandleUtil.determineTrend(senkouA, start_index, end_index); }
    public MarketTrend getKumoBTrend(int start_index, int end_index) { return CandleUtil.determineTrend(senkouB, start_index, end_index); }

    //----- private methods -----
    private void calcSen(int period, float[] sen) {
        for (int loop_idx = _nStartIndex; loop_idx >= _nEndIndex; loop_idx--) {
            int period_idx = loop_idx + period - 1;
            if (period_idx >= _Quotes.size()) continue;//skip out of bound
            FundQuote hl = IndicatorUtil.findHighLow(_Quotes, period_idx, loop_idx);
            sen[loop_idx] = (hl.getHigh() + hl.getLow()) / 2;
        }
    }

    //----- accessors -----
    public void setPeriodFast(int periodFast) { this.periodFast = periodFast; }
    public void setPeriodMedium(int periodMedium) { this.periodMedium = periodMedium; }
    public void setPeriodSlow(int periodSlow) { this.periodSlow = periodSlow; }
    public float[] getKijun() { return kijun; }
    public float[] getTenkan() { return tenkan; }
    public float[] getKumoA() { return senkouA; }
    public float[] getKumoB() { return senkouB; }
    public float[] getChikou() { return chikou; }

    //----- variables -----
    private ArrayList<FundQuote> _Quotes; int _nStartIndex, _nEndIndex;
    private int periodFast = 9, periodMedium = 26, periodSlow = 52;
    private float[] kijun;
    private float[] tenkan;
    private float[] senkouA, senkouB;
    private float[] chikou;

    //----- literals -----
    private static final String ID = "Ichimoku";
}