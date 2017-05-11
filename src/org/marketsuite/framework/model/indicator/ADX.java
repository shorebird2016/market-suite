package org.marketsuite.framework.model.indicator;

import java.util.ArrayList;

import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.jdom.Element;
import org.marketsuite.framework.model.FundData;

//to encapsulate ADX related data items as well as some basic calculations
public class ADX implements Indicator {
    //CTOR
    public ADX(int _period, ArrayList<FundQuote> quotes) {
        period = _period;
        _Quotes = quotes;
    }

    //----- interface methods -----
    public String getId() { return ID; }
    public Element objToXml() { return null; }
    public float[] getValues() { return new float[0]; }
    public FundData getQuotes() { return null; }

    //----- public methods -----

    //----- private methods -----
    private void calc() {
        int size = _Quotes.size();
        adx = di_plus = di_minus = new float[size];
        float[] plus_dm = new float[size];
        float[] minus_dm = new float[size];
        for (int i = size - 1; i > 0; i++) {//from older quotes to present
            FundQuote quote_prev = _Quotes.get(i); //yesterday
            FundQuote quote_cur = _Quotes.get(i - 1); //today
            float up_move = quote_cur.getHigh() - quote_prev.getHigh();
            float dn_move = quote_prev.getLow() - quote_cur.getLow();
            if (up_move > dn_move && up_move > 0)
                plus_dm[i - 1] = up_move;
            if (dn_move > up_move && dn_move > 0)
                minus_dm[i - 1] = dn_move;
        }
        EMA ema_pdm = new EMA(period, plus_dm);
        EMA ema_mdm = new EMA(period, minus_dm);
        ATR atr_pdm = new ATR(period, _Quotes);
        for (int i = 0; i < size; i++) {
            di_plus[i] = 100 * ema_pdm.getEma()[i] / atr_pdm.getATRs()[i];
            di_minus[i] = 100 * ema_mdm.getEma()[i] / atr_pdm.getATRs()[i];
            adx[i] = Math.abs(di_plus[i] - di_minus[i]) / (di_plus[i] + di_minus[i]);
        }
    }

    //----- variables -----
    private ArrayList<FundQuote> _Quotes;
    private int period = DEFAULT_PERIOD;
    private float[] adx, di_plus, di_minus;//holds final results

    //----- literals -----
    private static final String ID = "ADX";
    public static final int DEFAULT_PERIOD = 14;
}
/* from wikipedia
  To calculate +DI and −DI, one needs price data consisting of high, low, and closing prices each period (typically each day).
  One first calculates the directional movement (+DM and −DM):
    UpMove = today's high − yesterday's high
    DownMove = yesterday's low − today's low
    if UpMove > DownMove and UpMove > 0, then +DM = UpMove, else +DM = 0
    if DownMove > UpMove and DownMove > 0, then −DM = DownMove, else −DM = 0

  After selecting the number of periods (Wilder used 14 days originally), +DI and −DI are:
    +DI = 100 times exponential moving average of +DM divided by average true range
    −DI = 100 times exponential moving average of −DM divided by average true range

  The exponential moving average is calculated over the number of periods selected, and the average true range is an exponential average of the true ranges. Then:
    ADX = 100 times the exponential moving average of the absolute value of (+DI − −DI) divided by (+DI + −DI)
*/