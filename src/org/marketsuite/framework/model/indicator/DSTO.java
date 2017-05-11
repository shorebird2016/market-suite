package org.marketsuite.framework.model.indicator;

import java.util.ArrayList;

import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.util.IndicatorUtil;
import org.jdom.Element;

public class DSTO implements Indicator {
    //CTOR: create/populate all stochastic values for given quote array
    public DSTO(int period, int smooth_period, ArrayList<FundQuote> quotes) {
        this(period, smooth_period, quotes, quotes.size() - 1, 0);
    }

    //CTOR: create/populate a portion of stochastic values for given array, exception is thrown if out of bound
    //  caller must make sure indices are within bounds
    public DSTO(int period, int smooth_period, ArrayList<FundQuote> quotes, int start_index, int end_index) {
        //first loop to compute %K
        int size = quotes.size();
        pctK = new float[size];
        for (int k_idx = start_index; k_idx >= end_index; k_idx--) {
            //go back N bars and find high and low
            int idx = k_idx + period - 1;
            if (idx >= size) continue;//skip out of bound
            float high = quotes.get(idx).getHigh();
            float low = quotes.get(idx).getLow();
            while (idx >= k_idx) {
                float h = quotes.get(idx).getHigh();
                if (h > high)
                    high = h;
                float l = quotes.get(idx).getLow();
                if (l < low)
                    low = l;
                idx--;
            }

            //calc %D
            float close = quotes.get(k_idx).getClose();//getUnAdjclose();
            pctK[k_idx] = 100 * (close - low) / (high - low);
        }
        //calc %D
        pctD = IndicatorUtil.calcSMA(pctK, smooth_period, start_index, quotes.get(0).getSymbol());

    }

    //this CTOR only used by derived classes
    protected DSTO() {}

    //----- interface methods -----
    public String getId() { return ID; }
    public Element objToXml() { return null; }
    public float[] getValues() { return new float[0]; }
    public FundData getQuotes() { return null; }
    public float [] getPctK() { return pctK; }

    //----- variables -----
    private float[] pctK, pctD;

    //----- literals -----
    private static final String ID = "DSTO";
    public static final int DEFAULT_PERIOD = 14;
    public static final int DEFAULT_SMOOTH_PERIOD = 3;
}