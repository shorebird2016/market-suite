package org.marketsuite.framework.model.indicator;

import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.type.MarketTrend;
import org.marketsuite.framework.util.CandleUtil;
import org.marketsuite.framework.model.FundData;

public class SMA {
    //CTOR: create exponential moving average for given array, order = present to older per indices
    public SMA(int _period, float[] data_array) {
        period = _period;
        createSma(data_array);
//        sma = new float[data_array.length];//default empty
//        for (int loop_index = data_array.length - period; loop_index >= 0; loop_index--) {
//            float sum = 0;
//            int inner_index = loop_index + period - 1;
//            while(inner_index >= loop_index) {
//                sum += data_array[inner_index];
//                inner_index--;
//            }
//            sma[loop_index] = sum / period;//save moving average
//        }
    }
    public SMA(int _period, FundData fund) {
        float[] prices = new float[fund.getSize()];//default empty
        for (int i = 0; i < prices.length; i++)
            prices[i] = fund.getQuote().get(i).getClose();
        period = _period;
        createSma(prices);
    }

    //----- public, protected methods -----
    public MarketTrend getTrend(int start_index, int end_index) {
        return CandleUtil.determineTrend(sma, start_index, end_index);
    }

    //----- private method -----
    private void createSma(float[] data_array) {
        sma = new float[data_array.length];//default empty
        for (int loop_index = data_array.length - period; loop_index >= 0; loop_index--) {
            float sum = 0;
            int inner_index = loop_index + period - 1;
            while(inner_index >= loop_index) {
                sum += data_array[inner_index];
                inner_index--;
            }
            sma[loop_index] = sum / period;//save moving average
        }
    }

    //----- accessors -----
    public float[] getSma() { return sma; }

    //----- variables -----
    private float[] sma;
    private int period = DEFAULT_PERIOD;

    //----- literals -----
    private static final String ID = "EMA";
    public static final int DEFAULT_PERIOD = 14;
}
