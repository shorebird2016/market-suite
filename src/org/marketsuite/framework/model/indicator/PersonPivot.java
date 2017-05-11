package org.marketsuite.framework.model.indicator;

import java.util.ArrayList;

import org.marketsuite.framework.model.FundQuote;

public class PersonPivot {
    public PersonPivot(ArrayList<FundQuote> quotes, int interval) {
        this.interval = interval;
        int quote_idx = 0;//start from most recent date
        while (quote_idx < quotes.size()) {
            //find high, low of the interval
            int interval_limit = quote_idx + interval;
            float high = Float.MIN_VALUE, low = Float.MAX_VALUE;
            for (int pp_idx = quote_idx; pp_idx < interval_limit; pp_idx++) {//work backwards
                if (pp_idx >= quotes.size()) break;//prevent overrun
                FundQuote quote = quotes.get(pp_idx);
                if (quote.getHigh() > high) high = quote.getHigh();
                if (quote.getLow() < low) low = quote.getLow();
            }
            float close = quotes.get(quote_idx).getClose();
            float pp = (high + low + close) / 3;
            pivot.add(pp); R1.add(pp * 2 - low);
            float r2 = pp + (high - low);
            R2.add(r2); R3.add(r2 + (high - low));
            S1.add(pp * 2 - high);
            float s2 = pp - (high - low);
            S2.add(s2); S3.add(s2 - (high - low));
            quote_idx += interval;
        }
    }

    //----- accessors -----
    public int getInterval() { return interval; }
    public ArrayList<Float> getPivot() { return pivot; }
    public ArrayList<Float> getR1() { return R1; }
    public ArrayList<Float> getR2() { return R2; }
    public ArrayList<Float> getR3() { return R3; }
    public ArrayList<Float> getS1() { return S1; }
    public ArrayList<Float> getS2() { return S2; }
    public ArrayList<Float> getS3() { return S3; }

    //----- variables -----
    private int interval = 10;
    private ArrayList<Float> pivot = new ArrayList<>(),
        R1 = new ArrayList<>(), R2 = new ArrayList<>(), R3 = new ArrayList<>(),
        S1 = new ArrayList<>(), S2 = new ArrayList<>(), S3 = new ArrayList<>();
}
