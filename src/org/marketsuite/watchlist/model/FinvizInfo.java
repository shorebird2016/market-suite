package org.marketsuite.watchlist.model;

/**
 * Object that encapsulate Finviz fundamentals export format.
 */
public class FinvizInfo {
    //CTOR; construct from a typical line read from Finviz.com custom export
    public FinvizInfo(String csv_line) {
    }

    //----- accesor ----
    public String getDate() { return date; }
    public float getCurrentRatio() { return currentRatio; }
    public float getDebtToEquityRatio() { return debtToEquityRatio; }
    public float getRevenueGrowth() { return revenueGrowth; }
    public float getROE() { return ROE; }
    public float getOperatingMargin() { return operatingMargin; }
    public float getNetMargin() { return netMargin; }
    public float getPE() { return PE; }
    public float getCashFlow() { return cashFlow; }
    public float getEarningGrowth() { return earningGrowth; }
    public float getInventoryTurn() { return inventoryTurn; }

    //----- variables -----
    private String symbol;
    private String date; //yahoo format YYYY-MM-DD
    private float currentRatio;
    private float debtToEquityRatio;
    private float revenueGrowth;
    private float ROE;
    private float operatingMargin;
    private float netMargin;
    private float PE;
    private float cashFlow;
    private float earningGrowth;
    private float inventoryTurn;
    private String sector;
    private String note;

    //----- literals -----
    //Finviz.com custom export column format
    public static final int TOKEN_SYMBOL = 0;
    public static final int TOKEN_FULL_NAME = 1;
    public static final int TOKEN_SECTOR = 2;
    public static final int TOKEN_INDUSTRY = 3;
    public static final int TOKEN_MARKET_CAP = 4;
    public static final int TOKEN_PE = 5;
    public static final int TOKEN_PEG = 6;
    public static final int TOKEN_PS = 7;
    public static final int TOKEN_PB = 8;
    public static final int TOKEN_PRICE_TO_CASH = 9;
    public static final int TOKEN_PRICE_TO_FREE_CASH = 10;
    public static final int TOKEN_YIELD = 11;
    public static final int TOKEN_EPS_TTM = 12;
    public static final int TOKEN_EPS_YTD = 13;
    public static final int TOKEN_EPS_5_YR = 14;
    public static final int TOKEN_SALES_5YR = 15;
    public static final int TOKEN_EPS_QTR = 16;
    public static final int TOKEN_SALES_QTR = 17;
    public static final int TOKEN_SHARES_OUT = 18;
    public static final int TOKEN_SHARES_FLOAT = 19;
    public static final int TOKEN_INSIDER_OWN = 20;
    public static final int TOKEN_INST_OWN = 21;
    public static final int TOKEN_SHARES_SHORT = 22;
    public static final int TOKEN_SHORT_RATIO = 23;
    public static final int TOKEN_ROA = 24;
    public static final int TOKEN_ROE = 25;
    public static final int TOKEN_ROI = 26;
    public static final int TOKEN_CUR_RATIO = 27;
    public static final int TOKEN_QUICK_RATIO = 28;
    public static final int TOKEN_LT_DEBT_TO_EQTY = 29;
    public static final int TOKEN_TOTAL_DEBT_TO_EQTY = 30;
    public static final int TOKEN_GROSS_MARGIN = 31;
    public static final int TOKEN_OP_MARGIN = 32;
    public static final int TOKEN_PROFIT_MARGIN = 33;
    public static final int TOKEN_BETA = 34;
    public static final int TOKEN_EARNING_DATE = 35;

    public static final int TOKEN_LAST_UPDATE = 36;

}