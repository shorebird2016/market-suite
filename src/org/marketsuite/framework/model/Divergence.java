package org.marketsuite.framework.model;

import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.market.MarketInfo;

/**
 * A data object that tracks divergence related characteristics.
 */
public class Divergence {
    public Divergence(String symbol, String startDate, String endDate,
                      float startMacd, float endMacd,
                      float startRsi, float endRsi,
                      float startDsto, float endDsto,
                      MarketInfo mki,
                      int bps) {
        this.symbol = symbol;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startMacd = startMacd;
        this.endMacd = endMacd;
        this.startRsi = startRsi;
        this.endRsi = endRsi;
        this.startDsto = startDsto;
        this.endDsto = endDsto;
        marketInfo = mki;
        this.bps = bps;
    }

    //----- public methods -----
    /**
     * Calculate slope value for a given key from start to end.
     * @param key one of KEY_XXXX literals
     * @return slope between start and end dates
     */
    public float calcSlope(String key) {
        return 0;
    }

    //----- accessor -----
    public String getSymbol() { return symbol; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public int getBarsPerSegment() { return bps; }
    public float getStartMacd() { return startMacd; }
    public float getEndMacd() { return endMacd; }
    public float getStartRsi() { return startRsi; }
    public float getEndRsi() { return endRsi; }
    public float getStartDsto() { return startDsto; }
    public float getEndDsto() { return endDsto; }
    public MarketInfo getMarketInfo() { return marketInfo; }
//    public ArrayList<FundQuote> getQuotes() { return quotes; }

    //----- variables -----
    private String symbol;
    private String startDate;
    private String endDate;
    private float startMacd;
    private float endMacd;
    private float startRsi;
    private float endRsi;
    private float startDsto;
    private float endDsto;
    private MarketInfo marketInfo;
//    private ArrayList<FundQuote> quotes;
    private int bps; //bars per segment

    //----- literals -----
    public static final String KEY_PRICE = "KEY_PRICE";
    public static final String KEY_MACD = "KEY_MACD";
    public static final String KEY_RSI = "KEY_RSI";
    public static final String KEY_DSTO = "KEY_DSTO";
}
