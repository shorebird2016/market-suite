package org.marketsuite.framework.market;

import org.marketsuite.component.dialog.ProgressBar;
import org.marketsuite.component.util.CoreUtil;
import org.marketsuite.framework.model.*;
import org.marketsuite.framework.model.indicator.BollingerBand;
import org.marketsuite.framework.model.indicator.IndicatorData;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.framework.util.IndicatorUtil;
import org.marketsuite.framework.model.indicator.BollingerBand;
import org.marketsuite.framework.model.type.LoggingSource;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/**
 * Collection of helpers for various calculations in market related calculations for Tracker and Scanner.
 */
public class MarketUtil {
    //---------- MarketInfo and its objects --------
    /**
     * To construct and populate MarketInfo object associated with a given symbol over a recent period specified by start index.
     *   NOTE: Not carried out in background
     * @param symbol of choice
     * @param desire_length caller desired quote array size with extra 200 added, file may not have enough data
     * @return MarketInfo object
     * @throws java.io.IOException can't read quote file
     * @throws java.text.ParseException bad quote lines
     */
    public static MarketInfo calcMarketInfo(String symbol, int desire_length, DivergenceOption dvg_option) throws IOException, ParseException {
        FundData fund = DataUtil.readFundHistory(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, symbol, desire_length + 2);//2 extra line for comments in file
        int start_index = fund.getSize() - 1;
CoreUtil.setDeltaTimeStart("");
        ArrayList<FundQuote> quotes = fund.getQuote();
        for (FundQuote quote : quotes)
            quote.setClose(quote.getUnAdjclose());
        DataUtil.adjustForSplits(fund, start_index, 0);
        MarketInfo ret = new MarketInfo(symbol, fund, start_index);

        //compute high point
        float highest_close = 0;
        int highest_index = 0;
        for (int idx = 0; idx < fund.getSize() - 1; idx++) {
            float close = fund.getQuote().get(idx).getClose();
            if (close > highest_close) {
                highest_close = close;
                highest_index = idx;
            }
        }
        ret.setHighestDate(fund.getDate(highest_index));

        //pre-create result arrays, if SMA can't be calculated, return empty array, and longer SMA won't be calculated
        float[] sma_10, sma_30, sma_50 = null, sma_200 = null;
        ret.setSma20(IndicatorUtil.calcSMA(20, start_index - 20, fund));
        try {
            ret.setSma10(sma_10 = IndicatorUtil.calcSMA(10, start_index - 10, fund));
            ret.setSma30(sma_30 = IndicatorUtil.calcSMA(30, start_index - 30, fund));

            //when there is 10SMA and 30SMA, calculate their crossing
            int[] cross_10x30 = IndicatorUtil.calcCrossing(sma_10, sma_30, start_index - 30, 0);
            for (int index = 0; index < cross_10x30.length; index++) {
                if (cross_10x30[index] == IndicatorUtil.CROSSING_ABOVE)
                    ret.addCrossOver10x30Date(quotes.get(index).getDate());
            }
            ret.setSma50(sma_50 = IndicatorUtil.calcSMA(50, start_index - 50, fund));
            ret.setSma200(sma_200 = IndicatorUtil.calcSMA(200, start_index - 200, fund));
        } catch(IllegalArgumentException iae) {
            LogMessage.logSingleMessage(iae.getMessage(), LoggingSource.MARKETVIEW_IBD50, iae);//TODO make sure this is done in EDT
//            return ret;//with partial results
        }

        //compute indicators: MACD, DSTO, RSI for DCOM
        calcIndicators(ret, start_index, true);
        calcIndicators(ret, start_index, false);

        //compute EMA and 50x120 crossing dates
        float[] ema_8, ema_50, ema_120, ema_200;
        try {
            ret.setEma8(ema_8 = IndicatorUtil.calcEMA(8, start_index - 10, 0, fund));//T line
            ret.setEma50(ema_50 = IndicatorUtil.calcEMA(50, start_index - 50, 0, fund));
            ret.setEma120(ema_120 = IndicatorUtil.calcEMA(120, start_index - 120, 0, fund));

            //when there is reliable 50EMA and 120EMA, calculate their crossing
            int[] cross_50x120 = IndicatorUtil.calcCrossing(ema_50, ema_120, start_index - 120, 0);
            for (int idx = 0; idx < cross_50x120.length; idx++) {
                if (cross_50x120[idx] == IndicatorUtil.CROSSING_ABOVE)
                    ret.addCrossOver50x120Date(quotes.get(idx).getDate());
            }
            ret.setEma200(ema_200 = IndicatorUtil.calcEMA(200, start_index - 200, 0, fund));
        }catch(IllegalArgumentException iae) {
            LogMessage.logSingleMessage(iae.getMessage(), LoggingSource.MARKETVIEW_IBD50, iae);
//            return ret;
        }

        //compute Divergence, must do this after indicators are calculated
//        ret.setDvg(findDvg(symbol, dvg_option.getLookback(), dvg_option.getBarPerSegment(), dvg_option.getRecentFilter(), ret));
        ret.setVolumeAverage(IndicatorUtil.calcVolumeAverage(fund, 20, start_index - 20));
        ret.setBollingerBand(new BollingerBand(20, 2, 2, fund.getQuote()));
        if (sma_50 == null || sma_200 == null) return ret;
        //compute phases for each day in range
        int[] phases = new int[fund.getSize()];
//        int[] conditions = new int[sma_10.length];
        int prev_phase = -1, cur_phase = -1;
//            prev_cond = -1, cur_cond = -1;
        for (int index = start_index; index >= 0; index--) {
            float price = quotes.get(index).getClose();
//            float sma10 = sma_10[index];
//            float sma30 = sma_30[index];
            float sma50 = sma_50[index];
            float sma200 = sma_200[index];

            /* calculate phase by rule:
               Bullish        - P > 50SMA > 200SMA
               Recovery       - 200SMA > P > 50SMA
               Accumulation   - P > 200SMA > 50SMA
               Weak Warning   - 50SMA > P > 200SMA
               Strong Warning - 50SMA > 200SMA > P
               Distribution   -
               Bearish        - 200SMA > 50SMA > P
            */
            prev_phase = cur_phase;
            if (price > sma50 && sma50 > sma200)
                cur_phase = MarketInfo.PHASE_BULLISH;
            else if (sma200 > price && price > sma50)
                cur_phase = MarketInfo.PHASE_RECOVERY;
            else if (price > sma200 && sma200 > sma50)
                cur_phase = MarketInfo.PHASE_ACCUMULATION;
            else if (sma50 > price && price > sma200)
                cur_phase = MarketInfo.PHASE_WEAK_WARNING;
            else if (sma50 > sma200 && sma200 > price)
                cur_phase = MarketInfo.PHASE_STRONG_WARNING;
            else if (sma200 > sma50 && sma50 > price)
                cur_phase = MarketInfo.PHASE_BEARISH;
            else
                cur_phase = MarketInfo.PHASE_DISTRIBUTION;
            phases[index] = cur_phase;
//            if (prev_phase == -1)
//                prev_phase = cur_phase;//first time

            /* Definition of conditions
               1 - P > 10SMA > 30SMA > 50SMA
               2 - 10MA > P > 30SMA > 50SMA
               3 - P > 10SMA > 50SMA
               4 - 10SMA > P > 50SMA
            */
//            prev_cond = cur_cond;
//            if (price > sma10 && sma10 > sma30 && sma30 > sma50)
//                cur_cond = MarketInfo.CONDITION_1;
//            else if (sma10 > price && price > sma30 && sma30 > sma50)
//                cur_cond = MarketInfo.CONDITION_2;
//            else if (price > sma10 && sma10 > sma50)
//                cur_cond = MarketInfo.CONDITION_3;
//            else if (sma10 > price && price > sma50)
//                cur_cond = MarketInfo.CONDITION_4;
//            else
//                cur_cond = MarketInfo.CONDITION_NA;
//            conditions[index] = cur_cond;
//            if (prev_cond == -1)
//                prev_cond = cur_cond;//only first time

            //compute phase change / condition change
//            String date = quotes.get(index).getDate();
//            Calendar cal = AppUtil.stringToCalendar(date);
//            if (prev_phase != cur_phase)
//                ret.addPhaseChange(index, prev_phase, cur_phase, cal);
//            if (prev_cond == MarketInfo.CONDITION_2 && cur_cond == MarketInfo.CONDITION_1)
//                ret.addConditionChange(true, index, prev_cond, cur_cond, cal);
//            else if (prev_cond == MarketInfo.CONDITION_4 && cur_cond == MarketInfo.CONDITION_3)
//                ret.addConditionChange(false, index, prev_cond, cur_cond, cal);

            //set most current phase / condition
            if (index == 0) {
                ret.setCurrentPhase(MarketInfo.LIST_PHASE[cur_phase - 100]);
//                ret.setCurrentCondition(cur_cond);
            }
//            ret.setCurrentPhase(calcCurrentMarketPhase(fund));
        }

CoreUtil.showDeltaTime("<MarketUtil.calcMarketInfo(): Start Index: " + symbol + " " + start_index + " ");
        return ret;
    }

    /**
     * Perform full scan to get information for all symbols, builds up local cache per specified group of symbols.
     *   Also pass back a list of symbols MarketInfo can NOT be created.(skipped)
     * NOTE: this method inherently launches background thread.
     * @param symbols group of symbols to scan
     * Returns the result in an array of MarketInfo objects, provided as static with a boolean to indicate completion.
     */
    public static HashMap<String, MarketInfo> createMarketInfoMap(final ArrayList<String> symbols, final ArrayList<String> skipped_symbols) {
        final HashMap<String, MarketInfo> ret = new HashMap<>();

        //show initial progress bar
        final ProgressBar pb = ProgressBar.getInstance((JFrame)null,
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("scan_msg_1") + " " +
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("scan_msg_6"));
        pb.setVisible(true);

        //scan inside a thread
        Thread scan_thread = new Thread() {
            public void run() {
                for (final String symbol : symbols) {
                    try {//use 380 will make 50EMA and 120EMA match TOS data
                        MarketInfo mi = calcMarketInfo(symbol, FrameworkConstants.MARKET_QUOTE_LENGTH, new DivergenceOption(5, 90, 3));
                        ret.put(symbol, mi);//use 20 as small margin

                        //update progress bar
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                pb.setLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("scan_msg_7") + " " + symbol);
                            }
                        });
                    } catch (Exception pe) {//fail to calculate
                        skipped_symbols.add(symbol);
                    }
                }

                //done
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        pb.setVisible(false);//remove progress meter
                    }
                });
CoreUtil.showTimeFromAppStart("<MarketUtil.createMarketInfoMap() in Thread......DONE.....");
            }
        };
        scan_thread.start();
        try {//wait till it's complete
            scan_thread.join();
        } catch (InterruptedException e) { }
        return ret;
    }
    public static HashMap<String, MarketInfo> createMarketInfoMapNoWait(final ArrayList<String> symbols, final ArrayList<String> skipped_symbols) {
        final HashMap<String, MarketInfo> ret = new HashMap<>();

        //show initial progress bar
        final ProgressBar pb = ProgressBar.getInstance((JFrame)null,
                FrameworkConstants.FRAMEWORK_BUNDLE.getString("scan_msg_1") + " " +
                        FrameworkConstants.FRAMEWORK_BUNDLE.getString("scan_msg_6"));
        pb.setVisible(true);

        //scan inside a thread
        Thread scan_thread = new Thread() {
            public void run() {
                for (final String symbol : symbols) {
                    try {//use 380 will make 50EMA and 120EMA match TOS data
                        MarketInfo mi = calcMarketInfo(symbol, FrameworkConstants.MARKET_QUOTE_LENGTH, new DivergenceOption(5, 90, 3));
                        ret.put(symbol, mi);//use 20 as small margin

                        //update progress bar
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                pb.setLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("scan_msg_7") + " " + symbol);
//System.err.println("----- " + symbol);
                            }
                        });
                    } catch (Exception pe) {//fail to calculate
                        skipped_symbols.add(symbol);
                    }
                }

                //done
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        pb.setVisible(false);//remove progress meter
                    }
                });
                CoreUtil.showTimeFromAppStart("<MarketUtil.createMarketInfoMap() in Thread......DONE.....");
            }
        };
        scan_thread.start();
//TODO is it safe to drop join????????
//        try {//wait till it's complete
//            scan_thread.join();
//        } catch (InterruptedException e) { }
        return ret;
    }

    /**
     * Re-calculate MACD, RSI, DSTO using standard settings for given MarketInfo, arrays inside this object is replaced after calculation.
     * @param mki MarketInfo object
     * @param start_index of quote array, some point in the past
     * @param standard true = 12,26,9; 14,3; 14,2;    false = parameters from DCOM strategy
     */
    private static void calcIndicators(MarketInfo mki, int start_index, boolean standard) {
        FundData fund = mki.getFund();
        int size = fund.getSize();
        float[] macd_dcom, macd_emac;
        IndicatorData[] dsto_data = new IndicatorData[size], rsi_data = new IndicatorData[size];
        if (standard) {
            try {
                dsto_data = IndicatorUtil.calcStochastic(fund, 14, 3, start_index - 14, 0);
                rsi_data = IndicatorUtil.calcRSI(fund, start_index - 15, 0, 14);//one extra calcRSI
                macd_emac = IndicatorUtil.calcMACD(12, 26, start_index - 26, fund);
                float[] macd_sig = IndicatorUtil.calcMacdSigline(macd_emac, 9, fund.getSymbol());
                mki.setMacdStd(macd_emac); mki.setMacdSig(macd_sig);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();//TODO logging
            }
            float[] dsto = new float[dsto_data.length];  float[] rsi = new float[rsi_data.length];
            for (int idx = 0; idx < start_index; idx++) {
                if (dsto_data[idx] != null)
                    dsto[idx] = dsto_data[idx].getParam1();
                if (rsi_data[idx] != null)
                    rsi[idx] = rsi_data[idx].getParam1();
            }
            mki.setRsiStd(rsi); mki.setDstoStd(dsto);
        }
        else {
            try {
                dsto_data = IndicatorUtil.calcStochastic(fund, 9, 3, start_index - 9, 0);
                rsi_data = IndicatorUtil.calcRSI(fund, start_index - 10, 0, 9);//one extra calcRSI
                macd_dcom = IndicatorUtil.calcMACD(6, 9, start_index - 9, fund);
                float[] macd_sig = IndicatorUtil.calcMacdSigline(macd_dcom, 9, fund.getSymbol());
                mki.setMacd(macd_dcom); mki.setMacdSig(macd_sig);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();//TODO logging
            }
            float[] sto = new float[dsto_data.length];  float[] rsi = new float[rsi_data.length];
            for (int idx = 0; idx < start_index; idx++) {
                if (dsto_data[idx] != null)
                    sto[idx] = dsto_data[idx].getParam1();
                if (rsi_data[idx] != null)
                    rsi[idx] = rsi_data[idx].getParam1();
            }
            mki.setRsi(rsi); mki.setDsto(sto);
        }
    }
//TODO calculate smoothing values
    /**
     * Update existing MarketInfo structure's MACD with new parameters.
     * @param mki MarketInfo
     * @param fastMa shorter moving average period
     * @param slowMa longer moving average period
     * @param smoothMa smoothing moving average period
     */
    public static void updateMacd(MarketInfo mki, int fastMa, int slowMa, int smoothMa) {
        FundData fund = mki.getFund();
        int start_index = fund.getSize() - 1;
        mki.setMacd(IndicatorUtil.calcMACD(fastMa, slowMa, start_index - slowMa, fund));
    }
    public static void updateDsto(MarketInfo mki, int period, int smoothMa) {
        FundData fund = mki.getFund();
        int start_index = fund.getSize() - 1;
        IndicatorData[] dsto = IndicatorUtil.calcStochastic(fund, period, smoothMa, start_index - period, 0);
        float[] sto = new float[dsto.length];
        for (int idx = 0; idx < start_index; idx++) {
            if (dsto[idx] != null)
                sto[idx] = dsto[idx].getParam1();
        }
        mki.setDsto(sto);
    }

    //---------- Divergence Related Calculations --------
    /**
     * Find smallest Low quote for a given range of indices
     * @param quotes FundQuote array in YAHOO order
     * @param begin_index starting higher index inclusive
     * @param end_index ending lower index inclusive
     * @return index where minimum is
     */
    public static int findMinimumLow(ArrayList<FundQuote> quotes, int begin_index, int end_index) {
        int ret = -1;
        double  min = 1000000;//very few stocks have this price
        for (int idx = begin_index; idx <= end_index; idx++) {
            if (quotes.get(idx).getLow() < min) {
                min = quotes.get(idx).getLow();
                ret = idx;
            }
        }
        return ret;
    }

    private static int findLowIndex(ArrayList<FundQuote> quotes, double num, ArrayList<Integer> low_index) {
        for (Integer index : low_index) {
            if (num == quotes.get(index).getLow())
                return index;
        }
        return -1; //not found
    }

    //find nearby lows for a series of numbers surrounding center point
    private static int findAjacentLow(float[] data, int center_index, int offset) {
        double min = data[center_index];
        int min_idx = center_index;
        int start_idx = center_index;
        if (start_idx < offset)
            start_idx += offset;//don't create negative index
        for (int idx = start_idx - offset; idx <= center_index + offset; idx++) {
            float low = data[idx];
            if (low < min) {
                min = low;
                min_idx = idx;
            }
        }
        return min_idx;
    }

    /**
     * Detect DVG over the last x bars using specified "period", default 9 days from oscillator settings
     * @param symbol ticker
     * @param lookback_bars number of bars to look back
     * @param bars_per_segment number of bars per sample period, for avoiding nearby lows
     * @return null = no DVG, a Divergence object if DVG found
     * @throws IOException can't open files
     * @throws ParseException other errors reading quotes
     */
    public static Divergence findDvg(String symbol, int lookback_bars, int bars_per_segment, int recent_filter, MarketInfo mki) throws IOException, ParseException {
        ArrayList<FundQuote> quotes = mki.getFund().getQuote();

        //for each segment find lowest low, save in array
        ArrayList<Double> lows = new ArrayList<Double>();
        ArrayList<Integer> low_index = new ArrayList<Integer>();
        int seg_count = lookback_bars / bars_per_segment;
        for (int seg_idx = 0; seg_idx < seg_count; seg_idx++) {
            int begin_index = seg_idx * bars_per_segment;
            int segmin_idx = findMinimumLow(quotes, begin_index, begin_index + bars_per_segment - 1);
            low_index.add(segmin_idx);
            lows.add(new Double(quotes.get(segmin_idx).getLow()));
        }

        //sort, find two lowest low prices and indices
        Collections.sort(lows);
        ArrayList<Integer> sorted_index = new ArrayList<Integer>();
        for (Double low : lows) //index according to ascending lows
            sorted_index.add(findLowIndex(quotes, low, low_index));

        //checkpoint 1: first segment must be lowest to qualify and identify latest DVG
        if (sorted_index.size() == 0)
            return null;
        int lowest_idx = sorted_index.get(0);
        if (lowest_idx >= bars_per_segment)//not first segment
            return null;

        //checkpoint 2: only 1 low found
        if (sorted_index.size() == 1)
            return null;

        //checkpoint 3: end date not recent enough (via recent_filter)
        if (lowest_idx >= recent_filter)
            return null;

        for (int seg_idx = 1; seg_idx < sorted_index.size(); seg_idx++) {
            int low_idx = sorted_index.get(seg_idx);
            if (low_idx - lowest_idx <= 5) {
                sorted_index.remove(seg_idx);
                lows.remove(seg_idx);
                break;
            }
        }
        if (sorted_index.size() == 1)//possible only has 1 left
            return null;

        //checkpoint 4: 2nd segment low is too close to first segment low, skip 2nd
        //for each segment, try to find DVG between each segment and first segment
        int seg_idx = 1;
        while (seg_idx < sorted_index.size()) {
            int seg_low_idx = sorted_index.get(seg_idx);
            //check 2 out of 3 oscillators with rising values at these 2 points
            float[] macd = mki.getMacd();
//        lowest_low_idx = findAjacentLow(macd, lowest_low_idx, 3);
//        second_low_idx = findAjacentLow(macd, second_low_idx, 3);
            boolean macd_rise = macd[lowest_idx] > macd[seg_low_idx];
            float[] rsi = mki.getRsi();
            boolean rsi_rise = rsi[lowest_idx] > rsi[seg_low_idx];
            float[] sto = mki.getDsto();
            boolean sto_rise = sto[lowest_idx] > sto[seg_low_idx];
            if (macd_rise && rsi_rise || macd_rise && sto_rise || rsi_rise && sto_rise) {
//                System.out.println(symbol + " [" + quotes.get(lowest_idx).getDate() + " : " + quotes.get(seg_low_idx).getDate() + "] " + " BPS : " + bars_per_segment);
                return new Divergence(
                    symbol, quotes.get(seg_low_idx).getDate(), quotes.get(lowest_idx).getDate(),
                    macd[seg_low_idx], macd[lowest_idx],
                    rsi[seg_low_idx], rsi[lowest_idx],
                    sto[seg_low_idx], sto[lowest_idx],
                    mki,
                    bars_per_segment);
            }
            seg_idx++;
        }
        return null;
    }

    //recent_filter = how many days from most recent date
    public static ArrayList<Divergence> findDvgs(String symbol, int lookback_bars, int bars_per_segment, int recent_filter, MarketInfo mki) throws IOException, ParseException {
        String sd = null;  String ed = null; ArrayList<Divergence> ret = new ArrayList<Divergence>();
        for (int bps = bars_per_segment; bps <= 12; bps++) {
            Divergence dvg = findDvg(symbol, lookback_bars, bps, recent_filter, mki);
            if (dvg == null)
                continue;

            if (!dvg.getStartDate().equals(sd) || !dvg.getEndDate().equals(ed)) {
                //add to list when either is different
                ret.add(dvg);
                sd = dvg.getStartDate();  ed = dvg.getEndDate();
//                System.out.println(symbol + " [" + dvg.getStartDate() + " : " + dvg.getEndDate() + "] " + " BPS(" + dvg.getBarsPerSegment() + ")");
            }
        }
        if (ret.size() > 0) {
            for (Divergence dvg : ret)
                System.out.println(dvg.getSymbol() + " [" + dvg.getStartDate() + " ==> " + dvg.getEndDate() + "] bps(" + dvg.getBarsPerSegment() + ")");
            System.out.println("------------------------------------------------");
        }
        return ret;
    }

    // arguments: [0] = platform pc or mac, [1] = number of bars to look back, [2] = number of bars per segment
    //            [3] = optional symbol for detecting just one, if present, won't scan
    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("This program needs the following arguments:\n  [0] = platform; pc or mac\n" +
                "  [1] = number of bars to look back\n" +
                "  [2] = initial number of bars per segment to start scanning (up to 12)\n" +
                "  [3] = max number of days from today to include (how recent?)\n" +
                "  [4] = optional symbol for detecting just one, all symbols in database are scanned if not present\n" +
                "  Example: dvg.cmd 90 5 5 AAPL"
            );
            return;
        }
        if (args[0].equals("pc")) //run on PC, MAC is default
            FrameworkConstants.DATA_FOLDER = FrameworkConstants.DATA_FOLDER_PC;//default MAC
        FrameworkConstants.adjustDataFolder();

        //parameters for calculation
        int bars_back = Integer.parseInt(args[1]);
        int bars_per_segment = Integer.parseInt(args[2]);
        int recent_filter = Integer.parseInt(args[3]);

        //for single symbol calculation
        if (args.length > 4) {
            String symbol = args[4];
            try {
//                ArrayList<Divergence> dvgs = findDvgs(symbol, bars_back, bars_per_segment, recent_filter);
//                if (dvgs.size() == 0)
//                    System.out.println(symbol + " has no DVG....");
            } catch (Exception e) {
                System.out.println("\t" + symbol + ": " + e.getMessage());
//                e.printStackTrace();
            }
            return;
        }

        //scan entire database
        File dir = new File(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE);
        Vector<String> symbol_list = new Vector<String>();
        for (String file : dir.list()) {//build up combo list
            if (file.endsWith(FrameworkConstants.EXTENSION_QUOTE) && !file.startsWith("."))
                symbol_list.add(file.substring(0, file.indexOf(FrameworkConstants.EXTENSION_QUOTE)));
        }
        ArrayList<String> dvg_list = new ArrayList<String>();
        for (String symbol : symbol_list) {
            Divergence dvg;
            try {
//                ArrayList<Divergence> dvgs = findDvgs(symbol, bars_back, bars_per_segment, recent_filter);
//                if (dvgs.size() > 0)
//                    dvg_list.add(dvgs.get(0).getSymbol());
            } catch (Exception e) {
                System.err.println("ERROR: " + symbol + e.getMessage());
//                e.printStackTrace();
            }
        }
        StringBuilder buf = new StringBuilder();
        for (String dvg_sym : dvg_list)
            buf.append(dvg_sym).append(",");
        System.out.println(buf.toString());
    }

    //----------- Phase, Condition, Crossing Signals ----------
    /**
     * Given a data series, calculate phase changes
     * @param fund data series
     * @param start_index starting index of fund to calculate MA
     * @return array of calendar of phase change dates (after close)
     */
    public static ArrayList<Calendar> findPhaseChange(FundData fund, int start_index) {
        return null;
    }

    /**
     * Given a data series, calculate condition 2 to condition 1 changes from start index
     * @param fund data series
     * @param start_index starting index of fund to calculate MA
     * @return array of calendar of condition change dates (after close)
     */
    public static ArrayList<Calendar> findConditionChange21(FundData fund, int start_index) {
        return null;
    }

    /**
     * Given a data series, calculate condition 4 to condition 3 changes from start index
     * @param fund data series
     * @param start_index starting index of fund to calculate MA
     * @return array of calendar of condition change dates (after close)
     */
    public static ArrayList<Calendar> findConditionChange43(FundData fund, int start_index) {
        return null;
    }

    /**
     * Given a data series, calculate 2 EMA cross (fast cross above slow EMA) from start index (MAC entry)
     * @param fund data series
     * @param start_index starting index of fund to calculate EMA
     * @param fast_ema fast EMA, default 50
     * @param slow_ema slow EMA, default 120
     * @return array of calendar of condition change dates (after close)
     */
    public static ArrayList<Calendar> findEmaCrossSignal(FundData fund, int start_index, int fast_ema, int slow_ema) {
        return null;
    }

    /**
     * Given a data series, calculate 2 EMA cross (short cross above long EMA) from start index (MAC entry)
     * @param fund data series
     * @param start_index starting index of fund to calculate EMA
     * @param fast_ma fast MA, default 10
     * @param slow_ma slow MA, default 30
     * @return array of calendar of condition change dates (after close)
     */
    public static ArrayList<Calendar> findMaCrossSignal(FundData fund, int start_index, int fast_ma, int slow_ma) {
        return null;
    }

    /**
     * Given a data series, calculate CherryPicker conditions from start index (daily/weekly sync)
     * @param fund data series
     * @param start_index starting index of fund to calculate EMA
     * @param sto_period stochastic period, default 5
     * @param sto_avg stochastic averaging length, default 3
     * @return array of calendar of condition change dates (after close)
     */
    public static ArrayList<Calendar> findCherryPickerSignal(FundData fund, int start_index, int sto_period, int sto_avg) {
        return null;
    }

    /**
     * Given a data series, read TrendSpotter signal dates from file (user type in, obtained from barchart.com)
     * @param symbol a stock
     * @param file_name file that stores signal date of TrendSpotter
     * @return array of calendar of condition change dates (after close)
     */
    public static ArrayList<Calendar> getTrendSpotterSignal(String symbol, String file_name) {
        return null;
    }

    /**
     * Given a data series, calculate current phase
     * @param fund to get quote
     * @return one of MarketInfo.LIST_PHASE
     */
    public static String calcCurrentMarketPhase(FundData fund) {
        ArrayList<FundQuote> quotes = fund.getQuote();
        int start_index = fund.getSize() - 1;
        if (start_index < (FrameworkConstants.MARKET_QUOTE_LENGTH - 1) )
            return "Unknown";
//TODO to handle symbols w short history
        DataUtil.adjustForSplits(fund, start_index, 0);
        float[] sma_50 = IndicatorUtil.calcSMA(50, start_index - 50, fund);
        float[] sma_200 = IndicatorUtil.calcSMA(200, start_index - 200, fund);
        float price = quotes.get(0).getClose();//only use most recent
        float sma50 = sma_50[0];
        float sma200 = sma_200[0];

        /* calculate phase by rule:
            Bullish        - P > 50SMA > 200SMA
            Recovery       - 200SMA > P > 50SMA
            Accumulation   - P > 200SMA > 50SMA
            Weak Warning   - 50SMA > P > 200SMA P less than 1/2 distance
            Strong Warning - 50SMA > P > 200SMA P more than 1/2 distance
            Distribution   - 50SMA > 200SMA > P
            Bearish        - 200SMA > 50SMA > P
         */
        if (price > sma50 && sma50 > sma200) //bullish
            return MarketInfo.LIST_PHASE[0];
        else if (sma200 > price && price > sma50) //recovery
            return MarketInfo.LIST_PHASE[1];
        else if (price > sma200 && sma200 > sma50) //accumulation
            return MarketInfo.LIST_PHASE[2];
        else if (sma50 > price && price > sma200) { //warning - weak or strong by difference between 50MA and 200MA
            float diff = (sma50 - sma200)/2;
            float threshold = sma50 - diff;
            if (price > threshold)
                return MarketInfo.LIST_PHASE[3];//weak warning
            else
                return MarketInfo.LIST_PHASE[4];//strong warning
        }
        else if (sma50 > sma200 && sma200 > price) //distribution
            return MarketInfo.LIST_PHASE[5];
        else //if (sma200 > sma50 && sma50 > price) //bearish
            return MarketInfo.LIST_PHASE[6];
    }

    //phase of a particular index, assume quotes are already split adjusted
    public static String calcMarketPhase(MarketInfo mki, int index) {
        /* calculate phase by rule:
           Bullish        - P > 50SMA > 200SMA
           Recovery       - 200SMA > P > 50SMA
           Accumulation   - P > 200SMA > 50SMA
           Weak Warning   - 50SMA > P > 200SMA P less than 1/2 distance
           Strong Warning - 50SMA > P > 200SMA P more than 1/2 distance
           Distribution   - 50SMA > 200SMA > P
           Bearish        - 200SMA > 50SMA > P
        */
        if (index < 0) return null;
        float[] sma200s = mki.getSma200(); if (sma200s == null) return null;
        if (index >= sma200s.length) return null;
        float[] sma50s = mki.getSma50(); if (sma50s == null) return null;//can't be calculated
        if (index >= sma50s.length) return null;
        float sma50 = sma50s[index];
        float sma200 = sma200s[index];
        float price = mki.getFund().getQuote().get(index).getClose();//only use most recent
        if (sma50 >= sma200) {
            if (price >= sma50) return MarketInfo.LIST_PHASE[0];
            else if (price >= sma200) {//diff weak / strong warning
                float diff = (sma50 - sma200)/2;
                float threshold = sma50 - diff;
                if (price > threshold)
                    return MarketInfo.LIST_PHASE[3];//weak warning
                else
                    return MarketInfo.LIST_PHASE[4];//strong warning
            }
            else return MarketInfo.LIST_PHASE[5];
        }
        else {
            if (price < sma50) return MarketInfo.LIST_PHASE[6];
            else if (price < sma200) return MarketInfo.LIST_PHASE[1];
            else return MarketInfo.LIST_PHASE[2];
        }
    }

    /**
     * Given a data series, calculate current condition starting from start_index
     * @param symbol to look up data series
     * @param start_index starting index of fund to calculate MA and EMA
     * @return condtion 1 thru 4
     */
    public static int calcCondition(String symbol, int start_index) throws IOException {
        FundData fund = DataUtil.readFundHistory(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, symbol);
        if (start_index + 50 >= fund.getSize())
            throw new IOException("Not Enough Data for 200SMA");

        ArrayList<FundQuote> quotes = fund.getQuote();
        float[] sma_10 = IndicatorUtil.calcSMA(10, start_index, fund);
        float[] sma_30 = IndicatorUtil.calcSMA(30, start_index, fund);
        float[] sma_50 = IndicatorUtil.calcSMA(50, start_index, fund);
        float price = quotes.get(0).getUnAdjclose();
        float sma10 = sma_10[0];
        float sma30 = sma_30[0];
        float sma50 = sma_50[0];
        /* Definition of conditions
            1 - P > 10SMA > 30SMA > 50SMA
            2 - 10MA > P > 30SMA > 50SMA
            3 - P > 10SMA > 50SMA
            4 - 10SMA > P > 50SMA
         */
        if (price > sma10 && sma10 > sma30 && sma30 > sma50)
            return 1;
        else if (sma10 > price && price > sma30 && sma30 > sma50)
            return 2;
        else if (price > sma10 && sma10 > sma50)
            return 3;
        else if (sma10 > price && price > sma50)
            return 4;
        else
            return -1;
    }

}