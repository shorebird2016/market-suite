package org.marketsuite.framework.util;

import org.marketsuite.framework.model.*;
import org.marketsuite.framework.model.indicator.IndicatorData;
import org.marketsuite.framework.model.type.MarketPhase;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.model.indicator.IndicatorData;
import org.marketsuite.framework.model.type.MarketPhase;
import org.marketsuite.framework.resource.FrameworkConstants;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class IndicatorUtil {
    /**
     * To calculate first data point of Wilder's RSI for a given data set with specified position index and RSI period
     * basic assumption: there is always enough data at start_index to calculate this indicator
     * @param fund data set
     * @param start_index position index of desired RSI value, must have enough data prior to this position
     * @param period RSI parameter
     * @return RSI value for this data point
     * @exception IllegalArgumentException bad argument
     */
    public static IndicatorData calcFirstRSI(FundData fund, int start_index, int period) throws IllegalArgumentException {
        if (!AppUtil.isDataAvailable(fund, start_index + period))
            throw new IllegalArgumentException("Insufficient data to calculate RSI (" + period + ") @ index " + start_index);

        //loop thru n points to calculate avg gain and avg loss
        float total_gain = 0;
        float total_loss = 0;
        for (int index = start_index + period; index > start_index; index--) {
//TODO start_index + 1 is suspicious
            float price1 = fund.getQuote().get(index + 1).getClose();
            float price2 = fund.getQuote().get(index).getClose();
            float delta = price2 - price1;
            total_gain += Math.max(0, delta);
            total_loss += Math.max(0, -delta);
        }

        float avg_gain = total_gain / period;
        float avg_loss = total_loss / period;
        IndicatorData ret = new IndicatorData("RSI", 100, avg_gain, avg_loss);
        if (avg_loss == 0) //boundary condition
            return ret;

        float rsi = 100 - (100 / (1 + avg_gain / avg_loss) );
        return new IndicatorData("RSI", rsi, avg_gain, avg_loss);
    }

    /**
     * Calculate one RSI value for 2nd till last of a data series, formula is slightly different from the first one. It emphasizes newer data
     * like EMA does.
     * @param fund data set
     * @param start_index position index of data set for calculation, YAHOO format the larger the earlier, guaranteed to have data here
     * @param period RSI parameter, number of bars to look back
     * @param prev_rsi previous RSI data
     * @return newly created IndicatorData
     * @throws IllegalArgumentException not enough data after look back
     */
    public static IndicatorData calcRSI(FundData fund, int start_index, int period, IndicatorData prev_rsi) throws IllegalArgumentException {
        if (!AppUtil.isDataAvailable(fund, start_index + period))
            throw new IllegalArgumentException("Insufficient data to calculate RSI (" + period + ") @ index " + start_index);

        //calculate latest gain and loss
//TODO start_index + 1 is suspicious
        float price1 = fund.getQuote().get(start_index + 1).getClose();
        float price2 = fund.getQuote().get(start_index).getClose();
        float delta = price2 - price1;
        float gain = Math.max(0, delta);
        float loss = Math.max(0, -delta);

        //new average gain/loss
        float avg_gain = prev_rsi.getParam2() * (period - 1) + gain;
        float avg_loss = prev_rsi.getParam3() * (period - 1) + loss;
        IndicatorData ret = new IndicatorData("RSI", 100, avg_gain / period, avg_loss / period);
        if (avg_loss == 0) //boundary condition
            return ret;

        float rsi = 100 - (100 / (1 + avg_gain / avg_loss) );
        return new IndicatorData("RSI", rsi, avg_gain / period, avg_loss / period);
    }

    /**
     * Calculate RSI values for a given data series,
     * @param fund a FundData object
     * @param period RSI length parameter
     * @return array of IndicatorData objects
     */
    public static IndicatorData[] calcRSI(FundData fund, int start_index, int end_index, int period) {
        if (!AppUtil.isDataAvailable(fund, start_index + period))
            throw new IllegalArgumentException("Insufficient data to calculate RSI (" + period + ") @ index " + start_index);
        IndicatorData[] ret = new IndicatorData[fund.getSize()];//use the same index as fund

        //RSI needs to separately calculate first data point
        IndicatorData rsi = calcFirstRSI(fund, start_index, period);
        ret[start_index] = rsi;
        for (int loop_index = start_index - 1; loop_index >= end_index; loop_index--) {
            rsi = calcRSI(fund, loop_index, period, rsi);
            ret[loop_index] = rsi;
        }
        return ret;
    }

    /**
     * Calculate simple moving average (SMA) for fund using specified number of bars. fund may have more
     * elements than start_index; the desired starting point of good data
     * basic assumption: there is always enough data at start_index to calculate this indicator
     * @param period number of bars to average
     * @param start_index from this point backwards since YAHOO data is new first
     * @param fund the data array
     * @return a float array with same index representing SMA
     */
    public static float[] calcSMA(int period, int start_index, FundData fund) throws IllegalArgumentException {
        if (start_index < 0 || !AppUtil.isDataAvailable(fund, start_index + period))
            throw new IllegalArgumentException(fund.getSymbol() + "\t: Insufficient data SMA (" + period + ") @ index " + start_index);

        float[] ret = new float[fund.getSize()];//default empty
        for (int loop_index = start_index; loop_index >= 0; loop_index--) {
            float sum = 0;
            int inner_index = loop_index + period - 1;
            while(inner_index >= loop_index) {
                sum += fund.getPrice(inner_index);
                inner_index--;
            }
            ret[loop_index] = sum / period;//save moving average
        }
        return ret;
    }

    /**
     * calculate exponential moving average (EMA) for a symbol using specified number of bars
     * starting from start_index going backwards to index 0 of fund
     * basic assumption: there is always enough data at start_index to calculate this indicator
     *  note: underline data must have more than start_index + period to work with
     * @param period number of bars to look back
     * @param start_index which position in fund to start calculation
     * @param end_index which position to stop calculation
     * @param fund quote date object
     * @return array of float representing EMA, the earliest date is in the end, latest in the beginning
     */
    public static float[] calcEMA(int period, int start_index, int end_index, FundData fund) throws IllegalArgumentException {
        if (!AppUtil.isDataAvailable(fund, start_index + period) || start_index < 0)
            throw new IllegalArgumentException(fund.getSymbol() + ": Insufficient data EMA (" + period + ") @ index " + start_index);
        float[] ret = new float[fund.getSize()];
        int actual_start_index = start_index + period - 1;//+1
//TODO: may only use actual_start_index + 1 to reduce memory usage
        float weight = 2f / (period + 1);

        //calc the first EMA using previous N bar SMA as starting point
        float sum = 0;
        for (int count = 0; count < period; count++) {
            int idx = actual_start_index - count;
if (idx < 0) {
    System.out.println("------ " + fund.getSymbol() + start_index);
    break;
}
                sum += fund.getPrice(idx);
        }
        float prev_ema = sum / period;
        ret[start_index + 1] = prev_ema;

        //compute EMA going forward, first point is already calculated
        for (int loop_index = start_index; loop_index >= end_index; loop_index--) {
            float ema = fund.getPrice(loop_index) * weight + prev_ema * (1 - weight);
            ret[loop_index] = ema;
            prev_ema = ema;
        }
        return ret;
    }

    /**
     * calculate MACD using specified parameters
     * basic assumption: there is always enough data at start_index to calculate this indicator
     * @param num1 number of bars for first(faster) EMA
     * @param num2 number of bars for second(slower) EMA
     * @param start_index starting index of fund array
     * @param fund target security to simulate
     * @return a float array of MACD values with matching index to fund
     */
    public static float[] calcMACD(int num1, int num2, int start_index, FundData fund) {
        if (!AppUtil.isDataAvailable(fund, start_index + num2))
            throw new IllegalArgumentException("Insufficient data to calculate MACD (" + num1 + "," + num2 + ") @ index " + start_index);
        int size = fund.getSize();
        float[] ret = new float[size];
        float[] ema1 = calcEMA(num1, start_index, 0, fund);
        float[] ema2 = calcEMA(num2, start_index, 0, fund);
        for (int index = 0; index < size; index++)
            ret[index] = ema1[index] - ema2[index];
        return ret;
    }
    public static float[] calcMacdSigline(float[] macd, int sigline_ma, String symbol) {
        return calcSMA(macd, sigline_ma, macd.length - sigline_ma, symbol);
    }

    /**
     * to calculate matching ATR array for a given fund, index is the same as fund.
     * @param atr_length number of bars to use to calculate moving average of TR
     * @param fund data series
     * @return a float array of ATR
     */
    public static float[] calcATR(int atr_length, FundData fund) {
        //calculate TRUE RANGE first
        int size = fund.getSize();
        float[] true_range = new float[size];
        FundQuote last_quote = fund.getQuote().get(size - 1);
        true_range[size - 1] = last_quote.getHigh() - last_quote.getLow();//end point can't look back, just use H-L
        for (int index = 0; index <= size - 2; index++) {//only use current and previous one
            FundQuote cur_quote = fund.getQuote().get(index);
            FundQuote prev_quote = fund.getQuote().get(index + 1);
            float cur_range = cur_quote.getHigh() - cur_quote.getLow();
            float low_range = Math.abs(cur_quote.getLow() - prev_quote.getClose());//current low - prev close
            float high_range = Math.abs(cur_quote.getHigh() - prev_quote.getClose());//current high - prev close
            true_range[index] = Math.max(Math.max(cur_range, low_range), high_range);//max of all 3
        }

        //calculate simple moving average(using atr_length) of true range
        float[] ret = new float[size];
        for (int index = 0; index <= size - atr_length; index++) {
            float sum = 0;
            for (int avg_idx = index; avg_idx < index + atr_length; avg_idx++)
                sum += true_range[avg_idx];
            ret[index] = sum / atr_length;
        }
        return ret;
    }

    /**
     * to calculate ATR for a given fund, index is the same as fund
     * @param length number of bars to use to calculate moving average of TR
     * @param fund data series
     * @return a float of latest ATR
     */
    public static float calcLatestATR(int length, FundData fund) {
        //calculate TRUE RANGE first
        int size = fund.getSize();
        float[] true_range = new float[size];
        FundQuote last_quote = fund.getQuote().get(size - 1);
        true_range[size - 1] = last_quote.getHigh() - last_quote.getLow();//end point can't look back, just use H-L
        for (int index = 0; index <= size - 2; index++) {//only use current and previous one
            FundQuote cur_quote = fund.getQuote().get(index);
            FundQuote prev_quote = fund.getQuote().get(index + 1);
            float cur_range = cur_quote.getHigh() - cur_quote.getLow();
            float low_range = Math.abs(cur_quote.getLow() - prev_quote.getClose());//current low - prev close
            float high_range = Math.abs(cur_quote.getHigh() - prev_quote.getClose());//current high - prev close
            true_range[index] = Math.max(Math.max(cur_range, low_range), high_range);//max of all 3
        }

        //calculate simple moving average(using length) of true range
        return calcLatestSMA(true_range, length);
    }

    /**
     * Calculate ATR for specified fund at given index.
     * @param fund quote data
     * @param position_index desired point of interest in the past
     * @param atr_length number of bars used for calculating ATR
     * @return ATR of that point
     */
    public static float calcATRAtIndex(FundData fund, int position_index, int atr_length) {
        int size = atr_length + 1;//one extra to give correct value of #14 data point
        float[] true_range = new float[size];//twice as long to skip empty values
        FundQuote last_quote = fund.getQuote().get(size - 1);
        true_range[size - 1] = last_quote.getHigh() - last_quote.getLow();//end point can't look back, just use H-L

        //loop from specified position, look back in time
        for (int index = position_index; index < position_index + size - 1; index++) {
            FundQuote cur_quote = fund.getQuote().get(index);
            FundQuote prev_quote = fund.getQuote().get(index + 1);
            float cur_range = cur_quote.getHigh() - cur_quote.getLow();
            float low_range = Math.abs(cur_quote.getLow() - prev_quote.getClose());//current low - prev close
            float high_range = Math.abs(cur_quote.getHigh() - prev_quote.getClose());//current high - prev close
            true_range[index - position_index] = Math.max(Math.max(cur_range, low_range), high_range);//max of all 3
        }
        return calcLatestSMA(true_range, size);
    }

    /**
     * Calculate fast stochastic %K %D of a give data series using specified parameters.
     * basic assumption: there is always enough data at start_index to calculate this indicator
     * @param fund data series
     * @param period last N bar for finding high and low
     * @param ma_period smooth SMA period
     * @param start_index starting point index of data in fund (inclusive)
     * @param end_index ending point index of data in fund (inclusive)
     * @return array of IndicatorData, floatValue = %K, param1 = %D
     */
    public static IndicatorData[] calcStochastic(FundData fund, int period, int ma_period, int start_index, int end_index) throws IllegalArgumentException {
        if (!AppUtil.isDataAvailable(fund, start_index + period))
            throw new IllegalArgumentException("Insufficient data to calculate EMA (" + period + ") @ index " + start_index);

        IndicatorData[] ret = new IndicatorData[fund.getSize()];//use the same index as fund

        //when not enough data, return empty
//        if (period >= fund.getSize()) {
//            System.out.println("???" + fund.getSymbol() + " Can't do " + period + " DSTO " + " Quote = " + fund.getSize());
//            return ret;
//        }
        ArrayList<FundQuote> quotes = fund.getQuote();
        //adjust starting point based on period to avoid no data
//        if (start_index + period > quotes.size())
//            start_index = quotes.size() - period;

        //first loop to compute %K
        float[] pct_k = new float[fund.getSize()];
        for (int k_idx = start_index; k_idx >= end_index; k_idx--) {
            //go back N bars and find high and low
            int idx = k_idx + period - 1;
            float high = quotes.get(idx).getHigh();
            float low = quotes.get(idx).getLow();
            while (idx >= k_idx) {
                float h = quotes.get(idx).getHigh();
                if (h > high)
                    high = h;
                float l = quotes.get(idx).getLow();
                if (l < low)
                    low = l;
                idx--;
            }

            //calc %K
            float close = quotes.get(k_idx).getUnAdjclose();
            pct_k[k_idx] = 100 * (close - low) / (high - low);
        }
        //calc %D
        float[] pct_d = calcSMA(pct_k, ma_period, start_index, fund.getSymbol());

        //assemble IndicatorData array for return
        for (int idx = start_index; idx >= 0; idx--)
            ret[idx] = new IndicatorData("FAST STOCHASTIC", pct_k[idx], pct_d[idx], 0);
        return ret;
    }

    public static float[] calcVolumeAverage(FundData fund, int period, int start_index) {
        if (!AppUtil.isDataAvailable(fund, start_index + period))//look back N bars
            throw new IllegalArgumentException("Insufficient data to calculate Volume Average (" + period + ") @ index " + start_index);
        int size = fund.getSize();
        float[] ret = new float[size];
        for (int index = start_index; index >= 0; index--) {
            float sum = 0;
            for (int avg_idx = index + period - 1; avg_idx >= index; avg_idx--)
                sum += fund.getQuote().get(avg_idx).getVolume();
            ret[index] = sum / period;
        }
        return ret;
    }
    public static float[] calcVolumeAverage(ArrayList<FundQuote> quotes, int period) {
        int size = quotes.size();
        float[] ret = new float[size];
        for (int index = size - 1; index >= period - 1; index--) {
            float sum = 0;
            for (int avg_idx = index; avg_idx > index - period; avg_idx--)
                sum += quotes.get(avg_idx).getVolume();
            ret[index - period + 1] = sum / period;
        }
        return ret;
    }

    /**
     * To calculate ROC (Rate of Change) for specified period and range of quotes.
     * @param fund fund with quote series
     * @param period length of days
     * @param start_index starting point of quote array
     * @param end_index ending point of quote array
     * @return array of floats representing ROCs
     */
    public static double[] calcROC(FundData fund, int period, int start_index, int end_index) {
        if (!AppUtil.isDataAvailable(fund, start_index + period))//look back N bars
            throw new IllegalArgumentException("Insufficient data to calculate ROC (" + period + ") @ index " + start_index);
        double[] ret = new double[start_index - end_index];
        ArrayList<FundQuote> quotes = fund.getQuote();
        int index = end_index;
        while(index < start_index) {
            float p2 = quotes.get(index).getClose();
            float p1 = quotes.get(index + period).getClose();
            ret[index++] = (p2 - p1) / p1;
        }
        return ret;
    }

    public static double[] calcRateOfChange(double[] data, int period, int start_index, int end_index) {
        double[] ret = new double[start_index - end_index];
        int index = end_index;
        while(index < start_index)
            ret[index++] = 100* (data[index] - data[index + period]) / data[index + period];
        return ret;
    }

    /**
     * Given a fund with quote array, identify all the swing points.
     * @param fund a FundData object, preloaded with quotes
     * @return array of swing points with reverse order (most recent ones first), null = not found
     */
    public static ArrayList<FundQuote> findSwingPoints(FundData fund) {
        ArrayList<FundQuote> ret = new ArrayList<FundQuote>();
        ArrayList<FundQuote> quotes = fund.getQuote();
        ArrayList<FundQuote> swp1 = new ArrayList<FundQuote>();
        for (int index = 2; index < quotes.size() - 2; index++) {
            double low1 = quotes.get(index - 2).getLow();
            double low2 = quotes.get(index - 1).getLow();
            double cur_low = quotes.get(index).getLow();
            double low3 = quotes.get(index + 1).getLow();
            double low4 = quotes.get(index + 2).getLow();
            if (cur_low < low1 && cur_low < low2 && cur_low < low3 && cur_low < low4)
                swp1.add(quotes.get(index));
        }

        //traverse swp1 to eliminate points that oscillates, should go from high to low
        if (swp1.size() == 0)
            return null;

        int last_low_index = 0;
        double last_low = swp1.get(last_low_index).getLow();
        for (int index = 1; index < swp1.size(); index++) {
            double low = swp1.get(index).getLow();
            if (low < last_low) {//skip all higher swing points till newer low show up
                ret.add(swp1.get(last_low_index));
                last_low_index = index;
            }
        }
        return ret;
    }

    /**
     * Calculate price rates ($ per day) for specified start and end index of a fund with specified interval
     * @param fund symbol
     * @param start_index of quote array
     * @param end_index of quote array
     * @param interval # of days between two price points
     * @return list of RateInfo objects
     */
    public static ArrayList<RateInfo> calcPriceRates(FundData fund, int start_index, int end_index, int interval) {
        ArrayList<RateInfo> ret = new ArrayList<>();
        int index = end_index;
        ArrayList<FundQuote> quotes = fund.getQuote();
        int size = quotes.size();
        if (start_index >= size)
            start_index = size - interval - 1;
        while(index < start_index/* || index >= size*/) {
            ret.add(new RateInfo(quotes.get(index), quotes.get(index + interval), interval));
            index++;
        }
        return ret;
    }

    /**
     * AFC - A Few Cents, used for calculating stop price below price
     * @param price pivot
     * @return level a few cents below price
     */
    public static float calcAfc(double price) {
        if (price < 25)
            return 0.05f;
        else if (price < 50)
            return 0.1f;
        else if (price < 75)
            return 0.15f;
        else if (price < 100)
            return 0.2f;
        else if (price < 150)
            return 0.25f;
        else
            return 0.3f;
    }

    /**
     * calculate CAGR (Compound Annual Growth Rate) from begin to end
     * @param begin_date begin date yyyy-mm-dd
     * @param end_date end date
     * @param begin_value begin quote
     * @param end_value end quote
     * @return CAGR in double format
     */
    public static double calcCAGR(String begin_date, String end_date, float begin_value, float end_value) throws ParseException {
        double exponent = 1 / AppUtil.calcYear(begin_date, end_date);
        double base = end_value / begin_value;
        return Math.pow( base, exponent ) - 1;
    }

    /**
     * Convert transaction logs into annualized return array.  MUST have daily quotes database.
     * (1)for each year, collect all transactions that begins this year, ends this year or all this year(multi-year)
     * (2)for each collection within the year
     *    (a)begins this year, ends future year:
     *    (b)begins prev year, ends this year:
     *    (c)begins prev year, ends future year:
     * @param trans logs
     * @return list of AR objects
     * @exception IOException can not read file
     */
    public static ArrayList<AnnualReturn> calcAnnualReturn(ArrayList<Transaction> trans) throws IOException {
        if (trans.size() == 0)
            return null;//empty

        ArrayList<AnnualReturn> ret = new ArrayList<AnnualReturn>();
        String yr_str = trans.get(0).getExitDate().substring(0, 4);//YAHOO format
        int cur_year = Integer.parseInt(yr_str);
        String final_str = trans.get(trans.size() - 1).getExitDate().substring(0, 4);
        int final_yr = Integer.parseInt(final_str);
        String sym = trans.get(0).getSymbol();
        FundData fund = DataUtil.readFundHistory(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, sym);

        //for each year, collect a list of transaction performances, add up to that year's performance
        for (int yr = cur_year; yr <= final_yr; yr++) {
            float yr_ret = 0;
            for (Transaction tr : trans)
                yr_ret += calcTransactionAR(tr, yr, fund);
            ret.add(new AnnualReturn(sym, yr, yr_ret));
        }
        return ret;
    }

    /**
     * Calculate occurrence map of success counts(NOT STOPPED OUT) by applying various ATR multiple as STOP.
     * @param fund quote array of a specific symbol of interest, first element represents latest date
     * @param atr matching pre-calculated ATR array
     * @param num_bars bar count for back test
     * @return occurrence map in counts
     */
    public static HashMap<Float, Integer> calcATRMultipleMap(FundData fund, float[] atr, int num_bars) {
        HashMap<Float, Integer> fail_map = new HashMap<>();//key = ATR multiple, value = stopped out count during number of bars
        ArrayList<FundQuote> quotes = fund.getQuote();
        if (num_bars > quotes.size())//history too short
            num_bars = quotes.size();
        float multiple = 1.0f;
        while (multiple <= 4.0f) {
            int fail_count = 0;//total stopped out count for given multiple
            float cur_stop = 0;//first point can't be stopped out
            for (int index = num_bars - 1; index >= 0; index--) {//skip last(earliest) point since its ATR can't be calculated
                FundQuote cur_quote = quotes.get(index);
                if (cur_quote.getLow() <= cur_stop)//is this one stopped out?
                    fail_count++;

                //figure out next stop
//                FundQuote last_quote = quotes.get(index + 1);
//                float cur_atr = calcATRAtIndex(fund, index, atr_length);
                cur_stop = cur_quote.getClose() - atr[index] * multiple;//stop setting for next data point
            }
            fail_map.put(multiple, fail_count);//store success count
            multiple += 0.25f;
        }
        return fail_map;
    }

    /**
     * Given a quote series, calculate average volume of the last N days
     * @param fund quotes
     * @param num_days most recent N days
     * @return average volume
     */
    public static float calcAverageVolume(FundData fund, int num_days) {
        float ret = 0;
        ArrayList<FundQuote> quotes = fund.getQuote();
        for (int i = 0; i < num_days; i++)
            ret += quotes.get(i).getVolume();
        return ret / num_days;
    }

    public static MarketPhase calcMarketPhase(float price, float sma50, float sma200) {
        /* calculate phase by rule:
           Bullish        - P > 50SMA > 200SMA
           Recovery       - 200SMA > P > 50SMA
           Accumulation   - P > 200SMA > 50SMA
           Weak Warning   - 50SMA > P > 200SMA P less than 1/2 distance
           Strong Warning - 50SMA > P > 200SMA P more than 1/2 distance
           Distribution   - 50SMA > 200SMA > P
           Bearish        - 200SMA > 50SMA > P
        */
        if (sma50 >= sma200) {
            if (price >= sma50) return MarketPhase.Bullish;
            else if (price >= sma200) {//diff weak / strong warning
                float diff = (sma50 - sma200)/2;
                float threshold = sma50 - diff;
                if (price > threshold)
                    return MarketPhase.WeakWarning;//weak warning
                else
                    return MarketPhase.StrongWarning;//strong warning
            }
            else return MarketPhase.Distribution;
        }
        else {
            if (price < sma50) return MarketPhase.Bearish;
            else if (price < sma200) return MarketPhase.Recovery;
            else return MarketPhase.Accumulation;
        }
    }

    //----- private methods -----
    /**
     * calculate transaction's annualized return for a given year
     * each transaction maybe in one of the 4 situations:
     * (1) start / end in this year
     * (2) start before this year, end this year
     * (3) start this year, end in future year
     * (4) start before this year, end in future year
     * (5) start / end before this year
     * (6) start / end in future year
     * @param tr transaction in question
     * @param year for calculating return
     * @param fund MUST not be null or empty contain quotes for the symbol
     * @return annualized percentage return for given year for (1) thru (4). = -1 for (5) and (6)
     */
    private static float calcTransactionAR(Transaction tr, int year, FundData fund) {//todo short needs testing, very confusing.....
        //find out quotes for first and last day of this year
        FundQuote q1 = AppUtil.findFirstQuoteInYear(fund, year);
        FundQuote q2 = AppUtil.findLastQuoteInYear(fund, year);
        if (q1 == null || q2 == null)
            throw new IllegalArgumentException(FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_012")
                    + fund.getSymbol() + ": " + year);
        float begin_quote = q1.getClose();
        float end_quote = q2.getClose();
        int begin_yr = AppUtil.extractYear(tr.getEntryDate());
        int end_yr = AppUtil.extractYear(tr.getExitDate());
        if (begin_yr == year && end_yr == year) { //(1)
            return tr.getPerformance();
        }
        else if (begin_yr < year && end_yr == year) { //(2)
            if (tr.isLongTrade())
                return (tr.getExitPrice() - begin_quote) / begin_quote;
            else
                return (begin_quote - tr.getExitPrice()) / begin_quote;
        }
        else if (begin_yr == year && end_yr > year) { //(3)
            if (tr.isLongTrade())
                return (end_quote - tr.getEntryPrice()) / tr.getEntryPrice();
            else
                return (tr.getEntryPrice() - end_quote) / end_quote;
        }
        else if (begin_yr < year && end_yr > year) { //(4)
            if (tr.isLongTrade())
                return (end_quote - begin_quote) / begin_quote;
            else
                return (begin_quote - end_quote) / end_quote;
        }
        else {//outside transaction (5) and (6)
        }
        return 0;
    }

    private static float calcLatestSMA(float[] data, int num_bar) {
        float sum = 0;
        for (int index = 0; index < num_bar; index++)
            sum += data[index];
        return sum / num_bar;
    }

//TODO: calcSMA may not be correct????
    /**
     * Calculate SMA for a given array of data from last to first (still Yahoo backward type)
     * @param data to be smoothed
     * @param period SMA period
     * @param start_index starting point (inclusive) of calculation
     * @return array of smoothed data
     */
    public static float[] calcSMA(float[] data, int period, int start_index, String sym) {
//TODO: these checks shouldn't be needed, data is guaranteed and calculation is forward
        if (start_index <= 0) {
            System.out.println("???" + sym  + " Can't calculate " + period + " SMA " + " Start Index = " + start_index);
            return data;
        }

        //when not enough data, return empty
        if (period >= data.length) {
            System.out.println("???" + sym + " Can't calculate " + period + " SMA " + " Quote = " + data.length);
            return data;
        }

        float[] ret = new float[start_index + 1];
        for (int ret_index = start_index - period + 1; ret_index >= 0; ret_index--) {
            float sum = 0;
            for (int src_idx = ret_index + period - 1; src_idx >= ret_index; src_idx--)
                sum += data[src_idx];
            ret[ret_index] = sum / period;
        }
        return ret;
    }

    //find a list of fractals from an array of quotes. Fractal - 5 bar pattern using highs or lows
    // return null = error, empty = no fractals found, otherwise quotes with fractal high or low
    public static ArrayList<FundQuote> findFractals(ArrayList<FundQuote> quotes, boolean find_highs, int start_index, int end_index) {
        ArrayList<FundQuote> ret = new ArrayList<>();
        if (start_index >= quotes.size() || end_index < 0) return null;
        float v1, v2, v3, v4, v5;
        if (start_index - end_index < 5) return null;
        for (int idx = start_index - 2; idx >= end_index + 2; idx--) {
            if (find_highs) {
                v1 = quotes.get(idx - 2).getHigh();
                v2 = quotes.get(idx - 1).getHigh();
                v3 = quotes.get(idx).getHigh();
                v4 = quotes.get(idx + 1).getHigh();
                v5 = quotes.get(idx + 2).getHigh();
                //is this index a fractal
                if (v3 > v2 && v2 > v1 && v3 > v4 && v4 > v5) {
                    ret.add(quotes.get(idx)); quotes.get(idx).setFractHigh(true); }
            }
            else {
                v1 = quotes.get(idx - 2).getLow();
                v2 = quotes.get(idx - 1).getLow();
                v3 = quotes.get(idx).getLow();
                v4 = quotes.get(idx + 1).getLow();
                v5 = quotes.get(idx + 2).getLow();
                //is this index a fractal
                if (v3 < v2 && v2 < v1 && v3 < v4 && v4 < v5) {
                    ret.add(quotes.get(idx)); quotes.get(idx).setFractHigh(false); }
            }
        }
        return ret;
    }

    //----- Logging -----
    //display series data, 1..N
    public static void logDataSeries(float[] series, int start_index, int end_index, FundData fund) {
        for (int index= start_index; index <= end_index; index++) {
            System.err.println("[" + fund.getDate(index) + ": " + index + "] " +
                FrameworkConstants.PRICE_FORMAT.format(series[index]));
        }
        System.err.println();
    }
    public static void logDataSeries(float[] series1, float[] series2, int start_index, int end_index, FundData fund) {
        for (int index= start_index; index <= end_index; index++) {
            System.err.println("[" + fund.getDate(index) + ": " + index + "] " +
                FrameworkConstants.PRICE_FORMAT.format(series1[index]) + "\t" +
                FrameworkConstants.PRICE_FORMAT.format(series2[index]));
        }
        System.err.println();
    }
    public static void logDataSeries(float[] series1, float[] series2, int flag[],
                                     int start_index, int end_index, FundData fund) {
        System.out.println("----------- " + fund.getSymbol() + " ---------------");
        for (int index= start_index; index <= end_index; index++) {
            System.err.println("[" + fund.getDate(index) + ": " + index + "] " +
                FrameworkConstants.PRICE_FORMAT.format(series1[index]) + "\t" +
                FrameworkConstants.PRICE_FORMAT.format(series2[index]) + "\t" +
                flag[index]);
        }
        System.err.println();
    }

    //----- advanced methods calculating with indicators
    /**
     * calculate cross over between two data series (eg. short vs medium), two series must have the same length
     *   from specified starting point to ending point
     * @param ma1 first data array
     * @param ma2 second data array
     * @param start_index staring index of both arrays
     * @param end_index ending index of both arrays
     * @return int array of 3 possible values: CROSSING_ABOVE for ma1 cross above ma2; CROSSING_BELOW for ma1 cross below ma2
     */
    public static int[] calcCrossing(float[] ma1, float[] ma2, int start_index, int end_index) {
        if (ma1.length != ma2.length || ma1.length <= start_index || ma2.length <= start_index)
            throw new IllegalArgumentException(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_err_1"));
        int[] ret = new int[ma1.length];
        int loop_index = start_index + 1;
        boolean ma1_below = ma1[start_index-1] < ma2[start_index-1];//compare earliest date(starting point)
        while (loop_index >= end_index) {//data used up
            //skip comparison for 0's due to max bars back
            if (ma1[loop_index] == 0 || ma2[loop_index] == 0) {
                loop_index--;
                continue;
            }
            ret[loop_index] = CROSSING_NONE;
            if (ma1_below) {//look for crossing above
                if (ma1[loop_index] >= ma2[loop_index]) {
                    ret[loop_index] = CROSSING_ABOVE;
                    ma1_below = false;
                }
            }
            else {//look for crossing below
                if (ma1[loop_index] < ma2[loop_index]) {
                    ret[loop_index] = CROSSING_BELOW;
                    ma1_below = true;
                }
            }
            loop_index--;
        }
        return ret;
    }
    public final static int CROSSING_NONE = 0;
    public final static int CROSSING_ABOVE = 1;
    public final static int CROSSING_BELOW = -1;

    //calculate high/low of a period, start >= end
    public static FundQuote findHighLow(ArrayList<FundQuote> quotes, int start, int end) {
        float high = quotes.get(start).getHigh();
        float low = quotes.get(start).getLow();
        int idx = start;
        while (idx >= end) {//inclusive of loop_idx
            float h = quotes.get(idx).getHigh();
            if (h > high)
                high = h;
            float l = quotes.get(idx).getLow();
            if (l < low)
                low = l;
            idx--;
        }//when done, high and low found for this period
        FundQuote quote = new FundQuote(quotes.get(0).getSymbol(), -1, high, low, -1);
        return quote;
    }

    public static void main(String[] args) throws IOException {
        FrameworkConstants.DATA_FOLDER = FrameworkConstants.DATA_FOLDER_PC;
        FrameworkConstants.adjustDataFolder();
        FundData fund = DataUtil.readFundHistory(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, "LEN");
        float[] atr = calcATR(14, fund);
        for (int i=0; i<15; i++)
            System.out.println(fund.getQuote().get(i).getDate() + "\t"
                + fund.getQuote().get(i).getHigh() + "\t"
                + fund.getQuote().get(i).getLow() + "\t"
                + fund.getQuote().get(i).getClose() + "\t"
                + atr[i]
            );
        HashMap<Float, Integer> map = calcATRMultipleMap(fund, atr, 100);
        Set<Float> keys = map.keySet();
        System.out.println("ATR Multiple\tStopped Out");
        for (Float key : keys) {
            int fail_count = map.get(key);
            float fail_pct = fail_count / 30.0f;
            System.out.println(FrameworkConstants.DOLLAR_FORMAT.format(key) + "\t\t\t"
                + FrameworkConstants.INT_FORMAT.format(fail_count) + "\t\t"
                + FrameworkConstants.ROI_FORMAT.format(fail_pct));
        }
    }
}