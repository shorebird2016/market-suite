package org.marketsuite.framework.model;

import org.marketsuite.framework.market.IbdInfo;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.data.IbdRating;
import org.marketsuite.framework.market.IbdInfo;
import org.marketsuite.framework.market.MarketInfo;

import java.util.Calendar;

//mainly made up of IbdInfo, MarketInfo and BarchartInfo
public class TechnicalInfo {
    public TechnicalInfo(IbdInfo ibdInfo) {
        fullName = ibdInfo.getFullName();
        this.ibdInfo = ibdInfo;
    }

    //----- accessor -----
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getFullName() { return fullName; }
    public Calendar getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(Calendar lastUpdate) { this.lastUpdate = lastUpdate; }
    public MarketInfo getMarketInfo() { return marketInfo; }
    public IbdInfo getIbdInfo() { return ibdInfo; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public IbdRating getIbdRating() { return ibdRating; }
    public void setIbdRating(IbdRating ibdRating) { this.ibdRating = ibdRating; }

    //----- variables -----
    private String symbol;
    private String fullName;
    private Calendar lastUpdate;
    private MarketInfo marketInfo;
    private IbdInfo ibdInfo;
    private IbdRating ibdRating;

    //the following literals for IBD50 export format
    public static final int COLUMN_SYMBOL = 0;
    public static final int COLUMN_FULL_NAME = 1;
    public static final int COLUMN_LAST_UPDATE = 2;
    public static final int COLUMN_IBD50_RANK = 3;//IBD50 1..50
    public static final int COLUMN_PCT_OFF_HIGH = 4;
    public static final int COLUMN_COMPOSITE = 5;//0-99
    public static final int COLUMN_EPS_RATING = 6;//0-99
    public static final int COLUMN_RS_RATING = 7;//0-99
    public static final int COLUMN_SMR_RATING = 8;//A,B,C
    public static final int COLUMN_ACC_DIS_RATING = 9;//A,B,C
    public static final int COLUMN_GROUP_RATING = 10;//A,B,C
    public static final int COLUMN_MGMT_OWN = 11;
    public static final int COLUMN_QTR_RISE_SPONSORSHIP = 12;
    //the following literals for barchart trendspotter columns
    public static final int COLUMN_TRENDSPOTTER_SIGNAL = 12;
    public static final int COLUMN_TRENDSPOTTER_STRENGTH = 13;
//    public static final int COLUMN_LAST_UPDATE_DATE = 14;//for each symbol, may have different update date
}
