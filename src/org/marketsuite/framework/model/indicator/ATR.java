package org.marketsuite.framework.model.indicator;

import java.util.ArrayList;

import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.jdom.Element;
import org.marketsuite.framework.model.FundData;

public class ATR implements Indicator {
    //CTOR: create/populate this object with true range array from quote array
    public ATR(int atr_length, ArrayList<FundQuote> quotes) {
        int size = quotes.size();
        trueRanges = new float[size];
        FundQuote last_quote = quotes.get(size - 1);
        trueRanges[size - 1] = last_quote.getHigh() - last_quote.getLow();//end point can't look back, just use H-L
        for (int index = 0; index <= size - 2; index++) {//only use current and previous one
            FundQuote cur_quote = quotes.get(index);
            FundQuote prev_quote = quotes.get(index + 1);
            float cur_range = cur_quote.getHigh() - cur_quote.getLow();
            float low_range = Math.abs(cur_quote.getLow() - prev_quote.getClose());//current low - prev close
            float high_range = Math.abs(cur_quote.getHigh() - prev_quote.getClose());//current high - prev close
            trueRanges[index] = Math.max(Math.max(cur_range, low_range), high_range);//max of all 3
        }
        ATRs = new float[trueRanges.length];
        for (int index = 0; index <= size - atr_length; index++) {
            float sum = 0;
            for (int avg_idx = index; avg_idx < index + atr_length; avg_idx++)
                sum += ATRs[avg_idx];
            ATRs[index] = sum / atr_length;
        }
    }

    //----- interface methods -----
    public String getId() { return ID; }
    public Element objToXml() { return null; }
    public float[] getValues() { return new float[0]; }
    public FundData getQuotes() { return null; }

    //----- accessors -----
    public float[] getATRs() {
        return ATRs;
    }
    public float[] getTrueRanges() { return trueRanges; }

    //----- variables -----
    private float[] trueRanges;
    private float[] ATRs;

    //----- literals -----
    private static final String ID = "TR";
    public static final int DEFAULT_PERIOD = 14;
}
