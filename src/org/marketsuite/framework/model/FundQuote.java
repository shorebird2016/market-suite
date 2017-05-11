package org.marketsuite.framework.model;

import java.util.ArrayList;
import java.util.Calendar;

//representation of a select fund
public class FundQuote implements Comparable {
    public FundQuote(String _symbol) {
        symbol = _symbol;
    }
    public FundQuote(String _symbol, float _open, float _high, float _low, float _close) {
        symbol = _symbol;
        open = _open;
        high = _high;
        low = _low;
        close = _close;
    }

    //-----interface, overrides-----
    public int compareTo(Object o) {
        FundQuote quote = (FundQuote)o;
        if (close > quote.getClose())
            return 1;
        else if (close < quote.getClose())
            return -1;
        else
            return 0;
    }

    //-----public methods-----
    //return -1 = not found
    public static int findIndexByDate(ArrayList<FundQuote> daily_quotes, String date) {
        for (int i = 0; i < daily_quotes.size(); i++)
            if (daily_quotes.get(i).getDate().equals(date))
                return i;
        return -1;//not found
    }

    private String symbol;
    public String getSymbol() {
        return symbol;
    }

    private float close;
    public float getClose() {
        return close;
    }
    public void setClose(float close) {
        this.close = close;
    }

    private float open;
    public float getOpen() {
        return open;
    }
    public void setOpen(float open) {
        this.open = open;
    }

    private String date;
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    private float high;
    public void setHigh(float high) {
        this.high = high;
    }
    public float getHigh() {
        return high;
    }

    private float low;
    public float getLow() {
        return low;
    }
    public void setLow(float low) {
        this.low = low;
    }

    private int index;//location of this quote in the quote array (FundData)
    public void setIndex(int _index) { index = _index; }
    public int getIndex() { return index; }

    private float volume;
    public void setVolume(float vol) { volume = vol; }
    public float getVolume() { return volume; }

    private float adjClose;//adjusted close
    public void setAdjClose(float adjClose) {
        this.adjClose = adjClose;
    }
    public float getAdjClose() {
        return adjClose;
    }
    private boolean fractHigh;
    public boolean isFractHigh() { return fractHigh; }
    public void setFractHigh(boolean fractHigh) { this.fractHigh = fractHigh; }

    //todo merge with close, right now separate to get thru stochastic
    private float unAdjclose;//un-adjusted close to work with high/low
    public float getUnAdjclose() {
        return unAdjclose;
    }
    public void setUnAdjclose(float unAdjclose) {
        this.unAdjclose = unAdjclose;
    }

    //find dates with upward/downward gap starting from start_index to 0
    public static ArrayList<String> findGaps(boolean upward_gap, ArrayList<FundQuote> quotes, int start_index) {
        ArrayList<String> ret = new ArrayList<>();
        for (int idx = start_index; idx > 0; idx--) {
            if (upward_gap) {
                float h1 = quotes.get(idx).getHigh();
                float l2 = quotes.get(idx - 1).getLow();
                if (h1 < l2) //TODO may add a percent qualifier later
                    ret.add(quotes.get(idx - 1).getDate());
            }
        }
        return ret;
    }
}