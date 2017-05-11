package org.marketsuite.framework.model.indicator;

import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.jdom.Element;

import java.util.ArrayList;

//encapsulate characteristic of Bollinger Band with extension that upper band and lower band can have separate spec
//  upper/lower band spec = multiple of standard deviation of middle band
//  the earliest periods of upper/lower/sma has no meaning
public class BollingerBand implements Indicator {
    public BollingerBand(int period_count, int upper_band_spec, int lower_band_spec, ArrayList<FundQuote> quotes) {
        period = period_count;
        upperBandSpec = upper_band_spec;
        lowerBandSpec = lower_band_spec;

        //calculate sigma (standard deviation) of quotes
        StandardDeviation std_dev = new StandardDeviation();
        double[] qs = new double[quotes.size()];
        float [] qs1 = new float[quotes.size()];
        for (int idx = 0; idx < qs.length; idx++) {
            qs[idx] = quotes.get(idx).getClose();
            qs1[idx] = quotes.get(idx).getClose();
        }
        sma = new SMA(period, qs1);
        upperBand = new float[qs.length];
        lowerBand = new float[qs.length];

        //calc std dev for every period
        int intv_idx = quotes.size() - period;
        while (intv_idx >= 0) {
            //assemble N quotes into an array
            double[] sample = new double[period];
            int sub_idx = intv_idx + period - 1;
            while (sub_idx >= intv_idx) {
                float close = quotes.get(sub_idx).getClose();
                sample[sub_idx - intv_idx] = close;
                sub_idx--;
            }

            //calc sigma, then upper and lower bands
            double sig = std_dev.evaluate(sample);
            upperBand[intv_idx] = (float)(sma.getSma()[intv_idx] + upper_band_spec * sig);
            lowerBand[intv_idx] = (float)(sma.getSma()[intv_idx] - lower_band_spec * sig);
            intv_idx--;
        }
    }

    //create default band
    public BollingerBand(ArrayList<FundQuote> quotes) {
        this(DEFAULT_PERIOD, DEFAULT_STD_DEV, DEFAULT_STD_DEV, quotes);
    }

    //----- interface methods -----
    public String getId() { return id; }
    public Element objToXml() { return null; }
    public float[] getValues() { return new float[0]; }
    public FundData getQuotes() { return null; }

    //----- accessor -----
    public float[] getLowerBand() { return lowerBand; }
    public float[] getUpperBand() { return upperBand; }

    //----- variables -----
    private String id = DEFAULT_NAME;
    private int period;
    private int upperBandSpec;
    private int lowerBandSpec;
    private float[] upperBand;
    private float[] lowerBand;
    private SMA sma;
//    private float[] sigmaUpper, sigmaLower;
    public float[] getBandwidth() {
        float[] ret = new float[upperBand.length];
        for (int i=0; i<ret.length; i++)
            ret[i] = (upperBand[i] - lowerBand[i]) / sma.getSma()[i];
        return ret;
    }

    //----- literals -----
    public static final String DEFAULT_NAME = "Bollinger Band";
    public static final int DEFAULT_PERIOD = 20;
    public static final int DEFAULT_STD_DEV = 2;
}