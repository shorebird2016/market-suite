package org.marketsuite.riskmanager.portfolio;

import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.framework.util.IndicatorUtil;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.framework.util.IndicatorUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

/**
 * A model class for tracking various stop level related information of a symbol.
 */
public class StopLevelInfo {
    /**
     * Construct this object and calculate all attributes.
     * @param _symbol for looking up quotes
     * @param cost_base purchase price of this symbol      
     * @param _shares number of shares in transaction
     * @param cur_stop current stop level
     * @param look_back number of bars to look back
     * @exception IOException can't read file
     */
    public StopLevelInfo(String _symbol, float cost_base, int _shares, float cur_stop, int look_back) throws IOException {
        symbol = _symbol;
        cost = cost_base;
        stop = cur_stop;
        shares = _shares;
        fund = DataUtil.readQuotes(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, _symbol, look_back + EXTRA_BARS_LOOKBACK);
        quotes = fund.getQuote();
        swingPoints = IndicatorUtil.findSwingPoints(fund);
        ATR = IndicatorUtil.calcLatestATR(ATRLength, fund);
        for (int method_index = 0; method_index < LIST_STOP_METHOD.length; method_index++)
            stopLevels.add(new StopLevel(LIST_STOP_METHOD[method_index], calcStopLevel(method_index)));
        sort();

        //pre-build ATR multiple information
        ATRMultipleMap = IndicatorUtil.calcATRMultipleMap(fund, IndicatorUtil.calcATR(ATRLength, fund), look_back);
    }
    
    //----- public/protected methods -----
    /**
     * Locate percentage value of a give method index from LIST_STOP_METHOD, LIST_STOP_PERCENT arrays.
     * @param method_id identifier for method
     * @return pecent value
     */
    float findPercentByMethod(String method_id) {
        for (int index = STOP_PCT_BEGIN_INDEX; index <= STOP_PCT_END_INDEX; index++)
            if (LIST_STOP_METHOD[index].equals(method_id))
                return LIST_STOP_PERCENT[index];
        return 0;
    }

    /**
     * Locate method position in stopLevels because they are sorted by values
     * @param method_id one of LIST_STOP_METHOD element
     * @return index to array or -1 not found
     */
    int findIndexByMethod(String method_id) {
        for (int idx = 0; idx < stopLevels.size(); idx++) {
            if (stopLevels.get(idx).getId().equals(method_id))
                return idx;
        }
        return -1;
    }

    /**
     * find index of method from LIST_STOP_METHOD
     * @param method_id string identifier
     * @return position of method
     */
    int findMethodListIndex(String method_id) {
        for (int idx = 0; idx < LIST_STOP_TYPES.length; idx++) {
            if (LIST_STOP_TYPES[idx].equals(method_id))
                return idx;
        }
        return -1;
    }
    
    /**
     * Custom way to determine what break even price is, different for brokers, slippage estimated.
     * @return price
     */
    double calcBreakEvenPrice() {
        return (cost * shares + 0.01 * 2 * shares + 3) / shares;
    }

    /**
     * Performs sorting on stopLevels based on level.
     */
    void sort() {
        Object[] objects = stopLevels.toArray();
        Arrays.sort(objects, new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                StopLevel lvl1 = (StopLevel)o1;
                StopLevel lvl2 = (StopLevel)o2;
                if (lvl1.getLevel() > lvl2.getLevel())
                    return 1;
                else if (lvl1.getLevel() == lvl2.getLevel())
                    return 0;
                else
                    return -1;
            }
        });

        //put sorted values back, high value first
        stopLevels = new ArrayList<StopLevel>();
        for (int i = objects.length - 1; i >= 0; i--)
            stopLevels.add((StopLevel)objects[i]);
    }

    //convenient way to get all ATRs
    float[] calcATR() {
        return IndicatorUtil.calcATR(ATRLength, fund);
    }
    
    //a temporary way to gather some nominal levels to show in tooltip and graph
    ArrayList<StopLevel> gatherNominalLevels() {
        ArrayList<StopLevel> ret = new ArrayList<StopLevel>();
        
        //get from existing stopLevels
        for (int index = 0; index < stopLevels.size(); index++)
            ret.add(stopLevels.get(index));
        
        //delete ATR, add 1 ATR, 2 ATR and 3 ATR
        for (StopLevel sl : ret) {
            if (sl.getId().equals("ATR")) {
                ret.remove(sl);
                break;
            }
        }
        float close = quotes.get(0).getClose();
        ret.add(new StopLevel("1 ATR", close - ATR));
        ret.add(new StopLevel("2 ATR", close - 2 * ATR));
        ret.add(new StopLevel("3 ATR", close - 3 * ATR));

        //sort
        Object[] objects = ret.toArray();
        Arrays.sort(objects, new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                StopLevel lvl1 = (StopLevel)o1;
                StopLevel lvl2 = (StopLevel)o2;
                if (lvl1.getLevel() > lvl2.getLevel())
                    return 1;
                else if (lvl1.getLevel() == lvl2.getLevel())
                    return 0;
                else
                    return -1;
            }
        });

        //put sorted values back, high value first
        ret = new ArrayList<StopLevel>();
        for (int i = objects.length - 1; i >= 0; i--)
            ret.add((StopLevel)objects[i]);
        return ret;
    }

    /**
     * Is this stop method percent based?
     * @param method_id string id
     * @return true = percent type
     */
    static boolean isPercentMethod(String method_id) {
        for (int i = 0; i < LIST_STOP_METHOD.length; i++)
            if (method_id.equals(LIST_STOP_METHOD[i])) {//found
                if (i >= STOP_PCT_BEGIN_INDEX && i <= STOP_PCT_END_INDEX)
                    return true;
        }
        return false;
    }
    static boolean isAtrMethod(String method_id) {
        for (int i = 0; i < LIST_STOP_METHOD.length; i++)
          if (method_id.equals("1 ATR") || method_id.equals("2 ATR") || method_id.equals("3 ATR"))
                return true;
        return false;
    }
    static boolean isSwpMethod(String method_id) {
        for (int i = 0; i < LIST_STOP_METHOD.length; i++)
            if (method_id.equals(LIST_STOP_METHOD[i])) {//found
                if (i >= STOP_SWP_BEGIN_INDEX && i <= STOP_SWP_END_INDEX)
                    return true;
            }
        return false;
    }
    static boolean isBreakEven(String method_id) {
        for (int i = 0; i < LIST_STOP_METHOD.length; i++)
            if (method_id.equals(LIST_STOP_METHOD[i])) {//found
                if (i == BREAK_EVEN_INDEX)
                    return true;
            }
        return false;
    }
    static boolean isCurrentStop(String id) {
        for (int i = 0; i < LIST_STOP_METHOD.length; i++)
            if (id.equals(LIST_STOP_METHOD[i])) {//found
                if (i == CURRENT_STOP_INDEX)
                    return true;
            }
        return false;
    }

    //----- private methods -----
    /**
     * calculate corresponding stop level based on stop method in LIST_STOP_METHOD
     * @param method_index index of method
     * @return stop price
     */
    private double calcStopLevel(int method_index) {
        double close = quotes.get(0).getClose();
                
        double stop_price = close;//default
        if (method_index >= STOP_PCT_BEGIN_INDEX && method_index <= STOP_PCT_END_INDEX){
            stop_price = cost * (1 + LIST_STOP_PERCENT[method_index]);
            if (method_index == BREAK_EVEN_INDEX)
                stop_price = calcBreakEvenPrice();//commision 1c/share + $3 fee
        }
        else if (method_index >= STOP_ATR_BEGIN_INDEX && method_index <= STOP_ATR_END_INDEX) {
            int mul_factor = method_index - STOP_ATR_BEGIN_INDEX + 1;//1..3
            stop_price = close - ATR * mul_factor;
        }
        else if (method_index >= STOP_SWP_BEGIN_INDEX && method_index <= STOP_SWP_END_INDEX) {
            int idx = method_index - STOP_SWP_BEGIN_INDEX; //offset from beginning into swingPoints array
            if (idx > (swingPoints.size()-1))//not enough swing points in downtrend
                return stop_price;

            float low = swingPoints.get(idx).getLow();
            stop_price = low - IndicatorUtil.calcAfc(low);
        }
        else if (method_index == CURRENT_STOP_INDEX) {
            stop_price = stop;
        }
        return stop_price;
    }

    //----- instance variables / accessors -----
    private String symbol;
    public String getSymbol() {
        return symbol;
    }

    private float cost;
    public float getCost() {
        return cost;
    }
    public void setCost(float _cost) {
        cost = _cost;

        //update break even price in level array
        stopLevels.get(findIndexByMethod(LIST_STOP_METHOD[BREAK_EVEN_INDEX])).setLevel(calcBreakEvenPrice());
    }
    
    private float stop;
    public void setStop(float _stop) {
        stop = _stop;
        stopLevels.get(findIndexByMethod(LIST_STOP_METHOD[CURRENT_STOP_INDEX])).setLevel(_stop);
    }
    
    public void setBreakEven(float break_even) {
        stopLevels.get(BREAK_EVEN_INDEX).setLevel(break_even);
    }

    private int shares;
    public int getShares() {
        return shares;
    }
    public void setShares(int _shares) { shares = _shares; }

    //various levels that can be used as stops
    private ArrayList<StopLevel> stopLevels = new ArrayList<StopLevel>();
    public ArrayList<StopLevel> getStopLevels() {
        return stopLevels;
    }

    //quote data cache
    private FundData fund;

    //ATR value
    private float ATR;
    public float getATR() {
        return ATR;
    }
    private int ATRLength = DEFAULT_ATR_LENGTH;

    //swing points - low quote of a day with both sides(2 each side) having higher lows
    private ArrayList<FundQuote> swingPoints;
    public ArrayList<FundQuote> getSwingPoints() {
        return swingPoints;
    }

    //downloaded quotes
    private ArrayList<FundQuote> quotes;
    public ArrayList<FundQuote> getQuotes() {
        return quotes;
    }

    //ATR multiple rates
    private HashMap<Float, Integer> ATRMultipleMap;
    public float getATRMultipleFailRate(float multiple) {
        return ATRMultipleMap.get(multiple);
    }

    //----- literals -----
    private static final int EXTRA_BARS_LOOKBACK = 50;
    private static final int DEFAULT_ATR_LENGTH = 14;
    public static final String[] LIST_STOP_TYPES = {
            "5 % Loss", "3% Loss",  "Break Even",
            "4 % Gain", "8 % Gain",  "15 % Gain", "25 % Gain",  "50 % Gain",
            "ATR",
            "1 SWP", "2 SWP", "Custom"/*, "Current-Stop"*/ };
//TODO future        "Cost %", "ATR", "Swing Point", "Custom"
    public  static final float[] LIST_STOP_PERCENT = {//match loss type and percent type index
            -0.05f, -0.03f, 0, 0.04f, 0.08f, 0.15f, 0.25f, 0.5f
    };
    public  static final String[] LIST_STOP_METHOD = {
            "5 % Loss", "3% Loss",  "Break Even",
            "4 % Gain", "8 % Gain",  "15 % Gain", "25 % Gain",  "50 % Gain",
            "ATR",// "2 ATR", "3 ATR",
            "1 SWP", "2 SWP", "Custom", "Current-Stop" };
    //these indices are based on LIST_STOP_METHOD array
    public static final int STOP_PCT_BEGIN_INDEX = 0;
    public static final int STOP_PCT_END_INDEX = 7;
    public static final int BREAK_EVEN_INDEX = 2;
    public static final int STOP_ATR_BEGIN_INDEX = 8;
    public static final int STOP_ATR_END_INDEX = 8;
    public static final int STOP_SWP_BEGIN_INDEX = 9;
    public static final int STOP_SWP_END_INDEX = 10;
    public static final int STOP_CUSTOM_INDEX = 11;
    public static final int CURRENT_STOP_INDEX = 12;
}
