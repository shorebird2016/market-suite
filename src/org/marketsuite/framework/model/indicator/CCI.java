package org.marketsuite.framework.model.indicator;

import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.jdom.Element;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;

import java.util.ArrayList;

public class CCI implements Indicator {
    //CTOR
    public CCI(int _period, ArrayList<FundQuote> quotes) {
        period = _period;

        //calculate SMA for quotes
        cci = new float[quotes.size()];

        //step 1 - typical prices
        float[] typ_prices = new float[quotes.size()];
        for (int idx = 0; idx < quotes.size(); idx++) {
            FundQuote quote = quotes.get(idx);
            typ_prices[idx] = (quote.getUnAdjclose() + quote.getHigh() + quote.getLow())/3;
        }

        //step 2 - SMA of typical prices
        float[] sma = new SMA(period, typ_prices).getSma();

        //step 3 - mean deviation
        for (int idx = 0; idx < quotes.size() - period; idx++) {//go backwards, use available data
            float sum_dev = 0;
            for (int md_idx = idx; md_idx < idx + period; md_idx++) {
                float dev = sma[idx] - typ_prices[md_idx];
                if (dev < 0) dev = -dev;//absolute
                sum_dev += dev;
            }
            float mean_dev = sum_dev / period;
//if (idx < 25)
//System.err.println(idx + ": "
//    + FrameworkConstants.PRICE_FORMAT.format(typ_prices[idx]) + ", "
//    + FrameworkConstants.PRICE_FORMAT.format(sma[idx]) + ", "
//    + FrameworkConstants.PRICE_FORMAT.format(mean_dev));
            cci[idx] = (typ_prices[idx] - sma[idx]) / (FACTOR * mean_dev);
        }
    }

    //interface / override
    public String getId() { return ID; }
    public Element objToXml() { return null; }
    public float[] getValues() { return new float[0]; }
    public FundData getQuotes() { return null; }

    //----- accessor -----
    public float[] getCci() { return cci; }

    //----- variables -----
    private int period = 20;//default
    private float[] cci;
    private static final String ID = "CCI";
    private static float FACTOR = 0.015F;
}
