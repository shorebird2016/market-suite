package org.marketsuite.framework.model;

import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.resource.FrameworkConstants;

import java.text.ParseException;
import java.util.Calendar;

public class Fundamental {
    /**
     * CTOR for a Fundamental object
     * @param csv_line a line from csv file
     * @param from_finviz true = file is finviz.com export, false = fundamental.db
     */
    public Fundamental(String csv_line, boolean from_finviz) {
        if (csv_line == null || csv_line.equals(""))
            throw new IllegalArgumentException(FrameworkConstants.FRAMEWORK_BUNDLE.getString("wl_msg_1"));

        //use fix length token array (instead of relying on split)
        String[] tokens = new String[TOKEN_LAST_UPDATE + 1];
        for (int idx=0; idx<tokens.length; idx++)
            tokens[idx] = "";//initialize to all empty
        String[] raw_tokens = csv_line.split(",");
        for (int idx=0; idx<raw_tokens.length; idx++) {
            tokens[idx] = raw_tokens[idx];
        }

        //parse line into variables
//        String[] tokens = csv_line.split(",");
//        int len = tokens.length;
//        if (from_finviz && len < 5)//some companies don't have data   TOKEN_EARNING_DATE)
//            throw new IllegalArgumentException(FrameworkConstants.FRAMEWORK_BUNDLE.getString("wl_msg_2"));
//        else if (!from_finviz && len < TOKEN_LAST_UPDATE)
//            throw new IllegalArgumentException(FrameworkConstants.FRAMEWORK_BUNDLE.getString("wl_msg_2"));
        symbol = tokens[TOKEN_SYMBOL];
        fullName = tokens[TOKEN_FULL_NAME];
        sector = tokens[TOKEN_SECTOR];
        industry = tokens[TOKEN_INDUSTRY];
        marketCap = parseFloatField(tokens[TOKEN_MARKET_CAP]);
        PE = parseFloatField(tokens[TOKEN_PE]);
        PEG = parseFloatField(tokens[TOKEN_PEG]);
        PS = parseFloatField(tokens[TOKEN_PS]);
        PB = parseFloatField(tokens[TOKEN_PB]);
        PCF = parseFloatField(tokens[TOKEN_PRICE_TO_CASHFLOW]);
//if (symbol.equals("A"))
//    System.err.println("dfdfdfd");
        PFCF = parseFloatField(tokens[TOKEN_PRICE_TO_FREE_CASHFLOW]);
        eps = parseFloatField(tokens[TOKEN_EPS_TTM]);
//        String pe1 = tokens[TOKEN_PE];
//        if (!pe1.equals("N/A") && !pe1.equals(""))
//            PE = Float.parseFloat(tokens[TOKEN_PE]);
//        if (!tokens[TOKEN_PEG].equals(""))
//            PEG = Float.parseFloat(tokens[TOKEN_PEG]);
//        if (!tokens[TOKEN_PS].equals(""))
//            PS = Float.parseFloat(tokens[TOKEN_PS]);
//        if (!tokens[TOKEN_PB].equals(""))
//            PB = Float.parseFloat(tokens[TOKEN_PB]);
//        if (!tokens[TOKEN_PRICE_TO_CASHFLOW].equals(""))
//            PCF = Float.parseFloat(tokens[TOKEN_PRICE_TO_CASHFLOW]);
//        if (!tokens[TOKEN_PRICE_TO_FREE_CASHFLOW].equals(""))
//            PFCF = Float.parseFloat(tokens[TOKEN_PRICE_TO_FREE_CASHFLOW]);
//        eps = Float.parseFloat(tokens[TOKEN_EPS_TTM]);
        if (!tokens[TOKEN_YIELD].equals("")) {
            if (tokens[TOKEN_YIELD].endsWith("%"))
                yield = parsePctField(tokens, TOKEN_YIELD);
            else
                yield = Float.parseFloat(tokens[TOKEN_YIELD]);
        }
        epsYtd = parsePctField(tokens, TOKEN_EPS_YTD);
        eps5Yr = parsePctField(tokens, TOKEN_EPS_5_YR);
        sales5Yr = parsePctField(tokens, TOKEN_SALES_5YR);
        epsQtr = parsePctField(tokens, TOKEN_EPS_QTR);
        salesQtr = parsePctField(tokens, TOKEN_SALES_QTR);
        shares = parseFloatField(tokens[TOKEN_SHARES_OUT]);
        sharesFloat = parseFloatField(tokens[TOKEN_SHARES_FLOAT]);
        insiderPct = parsePctField(tokens, TOKEN_INSIDER_OWN);
        if (!tokens[TOKEN_INST_OWN].equals(""))
            instPct = parsePctField(tokens, TOKEN_INST_OWN);
        sharesShort = parsePctField(tokens, TOKEN_SHARES_SHORT);
        shortRatio = parseFloatField(tokens[TOKEN_SHORT_RATIO]);
        ROA = parsePctField(tokens, TOKEN_ROA);
        ROE = parsePctField(tokens, TOKEN_ROE);
        ROI = parsePctField(tokens, TOKEN_ROI);
        if (!tokens[TOKEN_CUR_RATIO].equals(""))
            currentRatio = parseFloatField(tokens[TOKEN_CUR_RATIO]);
        if (!tokens[TOKEN_QUICK_RATIO].equals(""))
            quickRatio = parseFloatField(tokens[TOKEN_QUICK_RATIO]);
        String debt = tokens[TOKEN_LT_DEBT_TO_EQTY];
        if (!debt.equals("N/A") && !debt.equals(""))
            debtToEquityLt = parseFloatField(debt);
        debt = tokens[TOKEN_TOTAL_DEBT_TO_EQTY];
        if (!debt.equals("N/A") && !debt.equals(""))
            debtToEquityTotal = parseFloatField(debt);
        grossMargin = parsePctField(tokens, TOKEN_GROSS_MARGIN);
        operatingMargin = parsePctField(tokens, TOKEN_OP_MARGIN);
        profitMargin = parsePctField(tokens, TOKEN_PROFIT_MARGIN);
        if (!tokens[TOKEN_BETA].equals(""))
            beta = parseFloatField(tokens[TOKEN_BETA]);
        if (tokens.length <= TOKEN_EARNING_DATE)//sometimes this field is not available
            return;

        earningDate = tokens[TOKEN_EARNING_DATE];
        if (tokens.length <= TOKEN_LAST_UPDATE || from_finviz) {
        lastUpdate = Calendar.getInstance();//default to today
            return;
        }
        try {//db should always have last update
            lastUpdate = AppUtil.stringToCalendar(tokens[TOKEN_LAST_UPDATE]);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    //----- public methods -----
    public boolean isETF() { return getIndustry().equals("Exchange Traded Fund"); }

    //----- private methods -----
    private float parsePctField(String[] tokens, int index) {
        if (tokens[index].equals(""))
            return 0;//empty string

        try {
            return FrameworkConstants.PCT_FORMAT.parse(tokens[index]).floatValue();
        } catch (ParseException e) {
            System.err.println(e.getMessage() + " ---> [" + index + "] " + tokens[index]);
        }
        return 0;
    }
    private float parseFloatField(String token) {
        if (token == null || token.equals(""))  return 0;
        try {
            return Float.parseFloat(token);
        } catch (NumberFormatException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return 0;
    }

    //----- accesor ----
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getSymbol() { return symbol; }
    public String getFullName() { return fullName; }
    public String getSector() { return sector; }
    public String getIndustry() { return industry; }
    public float getMarketCap() { return marketCap; }
    public float getPE() { return PE; }
    public float getPEG() { return PEG; }
    public float getPS() { return PS; }
    public float getPB() { return PB; }
    public float getPCF() { return PCF; }
    public float getPFCF() { return PFCF; }
    public float getYield() { return yield; }
    public float getEps() { return eps; }
    public float getEpsYtd() { return epsYtd; }
    public float getEps5Yr() { return eps5Yr; }
    public float getSales5Yr() { return sales5Yr; }
    public float getEpsQtr() { return epsQtr; }
    public float getSalesQtr() { return salesQtr; }
    public float getShares() { return shares; }
    public float getSharesFloat() { return sharesFloat; }
    public float getInsiderPct() { return insiderPct; }
    public float getInstPct() { return instPct; }
    public float getSharesShort() { return sharesShort; }
    public float getShortRatio() { return shortRatio; }
    public float getROA() { return ROA; }
    public float getROE() { return ROE; }
    public float getROI() { return ROI; }
    public float getCurrentRatio() { return currentRatio; }
    public float getQuickRatio() { return quickRatio; }
    public float getDebtToEquityLt() { return debtToEquityLt; }
    public float getDebtToEquityTotal() { return debtToEquityTotal; }
    public float getGrossMargin() { return grossMargin; }
    public float getOperatingMargin() { return operatingMargin; }
    public float getProfitMargin() { return profitMargin; }
    public float getBeta() { return beta; }
    public String getEarningDate() { return earningDate; }
    public String getNote() { return note; }
    public Calendar getLastUpdate() { return lastUpdate; }

    //----- variables -----
    private String symbol;
    private String fullName;
    private String sector;
    private String industry;
    private float marketCap;
    private float PE;
    private float PEG;
    private float PS;
    private float PB;
    private float PCF;
    private float PFCF;
    private float yield;
    private float eps;
    private float epsYtd;
    private float eps5Yr;
    private float sales5Yr;
    private float epsQtr;
    private float salesQtr;
    private float shares;
    private float sharesFloat;
    private float insiderPct;
    private float instPct;
    private float sharesShort;
    private float shortRatio;
    private float ROA;
    private float ROE;
    private float ROI;
    private float currentRatio;
    private float quickRatio;
    private float debtToEquityLt;
    private float debtToEquityTotal;
    private float grossMargin;
    private float operatingMargin;
    private float profitMargin;
    private float beta;
    private String earningDate;
    private String note;
    private Calendar lastUpdate;

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
    public static final int TOKEN_PRICE_TO_CASHFLOW = 9;
    public static final int TOKEN_PRICE_TO_FREE_CASHFLOW = 10;
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
