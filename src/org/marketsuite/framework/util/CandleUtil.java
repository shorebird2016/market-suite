package org.marketsuite.framework.util;

import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.type.CalendarQuarter;
import org.marketsuite.framework.model.type.MarketTrend;
import org.marketsuite.framework.resource.FrameworkConstants;
import com.sun.deploy.panel.TreeRenderers;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.type.MarketTrend;
import org.marketsuite.framework.resource.FrameworkConstants;

import java.util.ArrayList;
import java.util.Calendar;

//collection of helper method to compute candlestick relation functions
public class CandleUtil {
    /**
     * To find Doji or Spin top candle patterns from supplied quotes (1-bar pattern)
     * @param quotes a particular symbol
     * @param start_index earliest starting point of quotes, must be smaller than quotes size, -1=full range
     * @param height_pct candle/top height limitation, not too wide, empirical
     * @return array of indices to quotes array indicating found dojis/tops, null = error
     */
    public static ArrayList<Integer> findDojiSpintop(ArrayList<FundQuote> quotes, int start_index, float height_pct) {
        if (start_index >= quotes.size()) return null;
        if (start_index == -1) start_index = quotes.size() - 1;//use full range
        ArrayList<Integer> ret = new ArrayList<>();
        for (int idx = start_index; idx >= 0; idx--) {
            FundQuote quote = quotes.get(idx);

            //candle height must be small enough
            float range = quote.getHigh() - quote.getLow();
            float pct = 100 * Math.abs((quote.getOpen() - quote.getClose()) / range); //ratio of candle height
            if (pct > height_pct) continue;
            ret.add(idx);
        }
        return ret;
    }

    /**
     * To find find_hammer/hanging man candle patterns from supplied quotes (1-bar pattern). By definition, lower shadow needs
     *   to be at least twice longer than body and upper shadow very small
     * @param quotes a particular symbol
     * @param start_index earliest starting point of quotes, must be smaller than quotes size, -1=full range
     * @param upper_shadow_pct upper shadow height limitation, very small, empirical
     * @param find_hammer true = find find_hammer, false = find hanging man
     * @return array of indices to quotes array indicating found signal, null = error
     */
    public static ArrayList<Integer> findHammerHangman(ArrayList<FundQuote> quotes, int start_index, float upper_shadow_pct, boolean find_hammer) {
        if (start_index >= quotes.size()) return null;
        if (start_index == -1) start_index = quotes.size() - 1;//use full range
        ArrayList<Integer> ret = new ArrayList<>();
        for (int idx = start_index; idx >= 0; idx--) {
            FundQuote quote = quotes.get(idx);
            float body = Math.abs(quote.getOpen() - quote.getClose());
            float range = quote.getHigh() - quote.getLow();
            float upper_shadow = quote.getHigh() - Math.max(quote.getOpen(), quote.getClose());
            float lower_shadow = Math.min(quote.getOpen(), quote.getClose()) - quote.getLow();
            if (lower_shadow / body < 2) continue;
            float pct = 100 * upper_shadow / range; //ratio to candle height must be small
            if (pct > upper_shadow_pct) continue;//upper shadow too big
            int trend_start_idx = idx + 5;//look back last 5 day trend
            MarketTrend mt = determineTrend(quotes, trend_start_idx, idx);
            switch (mt) {
                case Up:
                    if (!find_hammer) //hanging man for up trend
                        ret.add(idx);
                        break;

                case SideWay://TODO.....skip and log
//                    System.err.println("\tSkip Hammer/Hanging Man: " + quote.getSymbol() + " [" + quote.getDate() + "]\n");
                    break;

                case Down:
                    if (find_hammer)
                        ret.add(idx);//hammer for down trend
                    break;
            }
        }
        return ret;
    }

    /**
     * To find inverted hammer/shooting star candle patterns from supplied quotes (1-bar pattern). By definition, upper shadow needs
     *   to be at least twice longer than body and lower shadow very small
     * @param quotes a particular symbol
     * @param start_index earliest starting point of quotes, must be smaller than quotes size, -1=full range
     * @param lower_shadow_pct lower shadow height limitation, very small, empirical
     * @param find_invhammer true = find find_invhammer, false = find hanging man
     * @return array of indices to quotes array indicating found signal, null = error
     */
    public static ArrayList<Integer> findInvHammerShootingStar(ArrayList<FundQuote> quotes, int start_index, float lower_shadow_pct, boolean find_invhammer) {
        if (start_index >= quotes.size()) return null;
        if (start_index == -1) start_index = quotes.size() - 1;//use full range
        ArrayList<Integer> ret = new ArrayList<>();
        for (int idx = start_index; idx >= 0; idx--) {
            FundQuote quote = quotes.get(idx);
            float body = Math.abs(quote.getOpen() - quote.getClose());
            float range = quote.getHigh() - quote.getLow();
            float upper_shadow = quote.getHigh() - Math.max(quote.getOpen(), quote.getClose());
            float lower_shadow = Math.min(quote.getOpen(), quote.getClose()) - quote.getLow();
            if (upper_shadow / body < 2) continue;//upper shadow needs to be large enough
            float pct = 100 * lower_shadow / range; //ratio of candle height
            if (pct > lower_shadow_pct) continue;//lower shadow too big
            int trend_start_idx = idx + 5;//look back last 5 day trend
            MarketTrend mt = determineTrend(quotes, trend_start_idx, idx);
            switch (mt) {
                case Up:
                    if (!find_invhammer) //shooting star for up trend
                        ret.add(idx);
                    break;

                case SideWay://TODO.....skip and log
//                    System.err.println("\tSkip Inverted Hammer/Shooting Star: " + quote.getSymbol() + " [" + quote.getDate() + "]\n");
                    break;

                case Down:
                    if (find_invhammer)
                        ret.add(idx);//inverted hammer for down trend
                    break;
            }
        }
        return ret;
    }

    /**
     * For given array of quotes, look for engulf pattern with specified max height percent (1st vs 2nd) (2-bar pattern)
     * @param quotes array of quotes from a symbol
     * @param start_index starting point to start search, -1=full range
     * @param max_pct max height percent of 1st to 2nd candle, ignored if 1st too tall
     * @param find_bullish true = to find bullish engulfing
     * @return array of indices of signal occurrence on completion date, null = not enough data
     */
    public static ArrayList<Integer> findEngulf(ArrayList<FundQuote> quotes, int start_index, float max_pct, boolean find_bullish) {
        if (start_index >= quotes.size()) return null;
        if (start_index == -1) start_index = quotes.size() - 1;//use full range
        ArrayList<Integer> ret = new ArrayList<>();
        for (int idx = start_index; idx >= 1; idx--) {//work on two quotes at a time
            FundQuote q1 = quotes.get(idx);
            FundQuote q2 = quotes.get(idx - 1);
            float o1 = q1.getOpen();
            float c1 = q1.getClose();
            float top1 = Math.max(o1, c1);
            float bot1 = Math.min(o1, c1);
            float o2 = q2.getOpen();
            float c2 = q2.getClose();
            float top2 = Math.max(o2, c2);
            float bot2 = Math.min(o2, c2);
            float h1 = Math.abs(o1 - c1);
            float h2 = Math.abs(o2 - c2);
            float pct = 100 * h1 / h2;
            if (top1 < top2 && bot1 > bot2 && h1 < h2 && pct < max_pct) {
                int trend_start_idx = idx + 5;//look back last 5 day trend
                MarketTrend mt = determineTrend(quotes, trend_start_idx, idx);
                switch (mt) {
                    case Up:
                        if (!find_bullish && c2 < o2) //bearish engulfing for up trend, 2nd must be red candle
                            ret.add(idx - 1);
                        break;

                    case SideWay://TODO.....skip and log
//                        System.err.println("\tSkip Engulfing: " + q2.getSymbol() + " [" + q2.getDate() + "]\n");
                        break;

                    case Down:
                        if (find_bullish && c2 > o2) //2nd must be green candle
                            ret.add(idx - 1);//bullish engulfing for down trend
                        break;
                }
            }
        }
        return ret;
    }

    /**
     * For given array of quotes, look for harami pattern with specified min height ratio (1st / 2nd) (2-bar pattern)
     * @param quotes array of quotes from a symbol
     * @param start_index starting point to search, -1=full range
     * @param min_ratio min height ratio of 1st to 2nd candle, ignored if 1st too short
     * @param find_bullish true = to find bullish engulfing
     * @return array of indices of signal occurrence on completion date, null = not enough data
     */
    public static ArrayList<Integer> findHarami(ArrayList<FundQuote> quotes, int start_index, float min_ratio, boolean find_bullish) {
        if (start_index >= quotes.size()) return null;
        if (start_index == -1) start_index = quotes.size() - 1;//use full range
        ArrayList<Integer> ret = new ArrayList<>();
        for (int idx = start_index; idx >= 1; idx--) {//work on two quotes at a time
            FundQuote q1 = quotes.get(idx);
            FundQuote q2 = quotes.get(idx - 1);
            float o1 = q1.getOpen();
            float c1 = q1.getClose();
            float top1 = Math.max(o1, c1);
            float bot1 = Math.min(o1, c1);
            float o2 = q2.getOpen();
            float c2 = q2.getClose();
            float top2 = Math.max(o2, c2);
            float bot2 = Math.min(o2, c2);
            float w1 = Math.abs(o1 - c1);
            float w2 = Math.abs(o2 - c2);
            float ratio = w1 / w2;
//if (q1.getSymbol().equals("XPH") && q1.getDate().equals("2014-07-10"))
//    System.err.println("-------");
            if (top1 > top2 && bot1 < bot2 && w1 > w2 && ratio > min_ratio) {
                int trend_start_idx = idx + 5;//look back last 5 day trend
                MarketTrend mt = determineTrend(quotes, trend_start_idx, idx);
                switch (mt) {
                    case Up:
                        if (!find_bullish && c1 > o1) //bearish harami for up trend, first candle should be green
                            ret.add(idx - 1);
                        break;

                    case SideWay://TODO.....skip and log
//                        System.err.println("\tSkip Harami: " + q2.getSymbol() + " [" + q2.getDate() + "]\n");
                        break;

                    case Down:
                        if (find_bullish && c1 < o1) //1st should be red candle
                            ret.add(idx - 1);//bullish for down trend
                        break;
                }
            }
        }
        return ret;
    }

    /**
     * For given array of quotes, look for dark cloud covering pattern (2-bar pattern)
     * @param quotes array of quotes from a symbol
     * @param start_index starting point to search, -1=full range
     * @return array of indices of signal occurrence on completion date, null = not enough data
     */
    public static ArrayList<Integer> findDarkCloud(ArrayList<FundQuote> quotes, int start_index) {
        if (start_index >= quotes.size()) return null;
        if (start_index == -1) start_index = quotes.size() - 1;//use full range
        ArrayList<Integer> ret = new ArrayList<>();
        for (int idx = start_index; idx >= 1; idx--) {//work on two quotes at a time
            FundQuote q1 = quotes.get(idx);
            FundQuote q2 = quotes.get(idx - 1);
            float o1 = q1.getOpen();
            float c1 = q1.getClose();
            float h1 = q1.getHigh();
            float l1 = q1.getLow();
            float o2 = q2.getOpen();
            float c2 = q2.getClose();
            if (o2 > c1 && c2 < (o1+c1)/2 && c2 > o1 && c2 < o2) {
                int trend_start_idx = idx + 5;//look back last 5 day trend, up for valid signal
                MarketTrend mt = determineTrend(quotes, trend_start_idx, idx);
                if (mt.equals(MarketTrend.Up))
                    ret.add(idx - 1);
            }
        }
        return ret;
    }

    /**
     * For given array of quotes, look for piercing pattern (2-bar pattern)
     * @param quotes array of quotes from a symbol
     * @param start_index starting point to search, -1=full range
     * @return array of indices of signal occurrence on completion date, null = not enough data
     */
    public static ArrayList<Integer> findPiercing(ArrayList<FundQuote> quotes, int start_index) {
        if (start_index >= quotes.size()) return null;
        if (start_index == -1) start_index = quotes.size() - 1;//use full range
        ArrayList<Integer> ret = new ArrayList<>();
        for (int idx = start_index; idx >= 1; idx--) {//work on two quotes at a time
            FundQuote q1 = quotes.get(idx);
            FundQuote q2 = quotes.get(idx - 1);
            float o1 = q1.getOpen();
            float c1 = q1.getClose();
            float h1 = q1.getHigh();
            float o2 = q2.getOpen();
            float c2 = q2.getClose();
            if (o2 < c1 && c2 > (o1+c1)/2 && c2 < h1 && c2 > o2 && c1 < o1) {
                int trend_start_idx = idx + 5;//look back last 5 day trend, up for valid signal
                MarketTrend mt = determineTrend(quotes, trend_start_idx, idx);
                if (mt.equals(MarketTrend.Down))
                    ret.add(idx - 1);
            }
        }
        return ret;
    }

    /**
     * For given array of quotes, look for kicker pattern (2-bar pattern)
     * @param quotes array of quotes from a symbol
     * @param start_index starting point to search, -1=full range
     * @param bullish true = search for bullish kicker, false = bearish kicker
     * @return array of indices of signal occurrence on completion date, null = not enough data
     */
    public static ArrayList<Integer> findKicker(ArrayList<FundQuote> quotes, int start_index, boolean bullish) {
        if (start_index >= quotes.size()) return null;
        if (start_index == -1) start_index = quotes.size() - 1;//use full range
        ArrayList<Integer> ret = new ArrayList<>();
        for (int idx = start_index; idx >= 1; idx--) {//work on two quotes at a time
            FundQuote q1 = quotes.get(idx);
            FundQuote q2 = quotes.get(idx - 1);
            float o1 = q1.getOpen();
            float c1 = q1.getClose();
            float h1 = q1.getHigh();
            float l1 = q1.getLow();
            float o2 = q2.getOpen();
            float c2 = q2.getClose();
            float h2 = q2.getHigh();
            float l2 = q2.getLow();
            if (bullish) {
                if (c2 > o2 && c1 < o1 && h1 < l2)
                    ret.add(idx - 1);
            }
            else {
                if (h2 < l1 && c2 < o2 && c1 > o1)
                    ret.add(idx - 1);
            }
        }
        return ret;
    }

    /**
     * For given array of quotes, look for pusher pattern (2-bar pattern)
     * @param quotes array of quotes from a symbol
     * @param start_index starting point to search, -1=full range
     * @param bullish true = search for bullish pusher, false = bearish pusher
     * @return array of indices of signal occurrence on completion date, null = not enough data
     */
    public static ArrayList<Integer> findPusher(ArrayList<FundQuote> quotes, int start_index, boolean bullish) {
        if (start_index >= quotes.size()) return null;
        if (start_index == -1) start_index = quotes.size() - 1;//use full range
        ArrayList<Integer> ret = new ArrayList<>();
        for (int idx = start_index; idx >= 1; idx--) {//work on two quotes at a time
            FundQuote q1 = quotes.get(idx);
            FundQuote q2 = quotes.get(idx - 1);
            float o1 = q1.getOpen();
            float c1 = q1.getClose();
            float o2 = q2.getOpen();
            float c2 = q2.getClose();
            if (bullish) {
                if (c2 > o2 && c1 < o1 && c2 > o1 && o2 > c1)
                    ret.add(idx - 1);
            }
            else {
                if (c1 > o1 && c2 < o2 && c1 > o2 && o1 > c2)
                    ret.add(idx - 1);
            }
        }
        return ret;
    }

    /**
     * To find gap(window) patterns from supplied quotes (2-bar pattern)
     * @param quotes a particular symbol
     * @param start_index earliest starting point of quotes, must be smaller than quotes size, -1=full range
     * @return array of indices to quotes array indicating found gaps, null = error
     */
    public static ArrayList<Integer> findGap(ArrayList<FundQuote> quotes, int start_index, boolean bullish) {
        if (start_index >= quotes.size()) return null;
        if (start_index == -1) start_index = quotes.size() - 1;//use full range
        ArrayList<Integer> ret = new ArrayList<>();
        for (int idx = start_index; idx >= 1; idx--) {//two quotes at a time
            FundQuote q1 = quotes.get(idx);
            FundQuote q2 = quotes.get(idx - 1);
            float h1 = q1.getHigh(); float l1 = q1.getLow();
            float h2 = q2.getHigh(); float l2 = q2.getLow();
            if (bullish) {
                if (h1 < l2) ret.add(idx - 1);
            }
            else {
                if (h2 < l1)
                    ret.add(idx - 1);
            }
        }
        return ret;
    }
    /** find gaps within a given quarter (inclusive)
     * @param quotes base set to search from, must have data to cover quarter range +1/-1 (in case first/last day of quarter is gap)
     * @param quarter specific quarter to look for
     * @return array of indices into quotes array representing gap days, =null if quarter start is outside quote array
     */
    public static ArrayList<Integer> findGapInQuarter(ArrayList<FundQuote> quotes, CalendarQuarter quarter, boolean bullish) {
        ArrayList<Integer> ret = new ArrayList<>();
        int start_idx = FundQuote.findIndexByDate(quotes, AppUtil.calendarToString(quarter.getStartDate()));
        if (start_idx < 0) return null;//start of quarter not in quote array
        int end_idx = FundQuote.findIndexByDate(quotes, AppUtil.calendarToString(quarter.getEndDate()));
        if (end_idx < 0) end_idx = 0;//not full quarter
        for (int idx = start_idx + 1; idx > end_idx; idx--) {//earliest --> latest cover outside +1 bar to catch boundary
            FundQuote q1 = quotes.get(idx);
            FundQuote q2 = quotes.get(idx - 1);
            float h1 = q1.getHigh(); float l1 = q1.getLow();
            float h2 = q2.getHigh(); float l2 = q2.getLow();
            if (bullish) {
                if (h1 < l2) ret.add(idx - 1);
            }
            else {
                if (h2 < l1)
                    ret.add(idx - 1);
            }
        }
        return ret;
    }
    //find bullish gap within a quote index range (inclusive)
    public static ArrayList<Integer> findBullishGap(ArrayList<FundQuote> quotes, int begin_index, int end_index) {
        ArrayList<Integer> ret = new ArrayList<>();
        for (int idx = begin_index; idx >= end_index; idx--) {//earliest --> latest
            FundQuote q1 = quotes.get(idx);
            FundQuote q2 = quotes.get(idx - 1);
            float h1 = q1.getHigh(); //float l1 = q1.getLow();
//            float h2 = q2.getHigh();
            float l2 = q2.getLow();
            if (h1 < l2) ret.add(idx - 1);
        }
        return ret;
    }

    private static MarketTrend determineTrend(ArrayList<FundQuote> quotes, int start_index, int end_index) {
        if (start_index >= quotes.size()) start_index = quotes.size() - 1;
        SimpleRegression rgn = new SimpleRegression();
        double x = 1;//each bar increase by 1
        for (int idx = start_index; idx >= end_index; idx--) {
            FundQuote quote = quotes.get(idx);
            double y = quote.getClose();
//System.err.println("[X, Y]=" + x + ", " + y);
            rgn.addData(x++, y);
        }
        double slope = rgn.getSlope();
//System.err.println("-------SLOPE = " + FrameworkConstants.FORMAT_NUMBERS.format(slope) +
//" [" + quotes.get(end_index).getDate() + "]");
        if (slope < -0.05) return MarketTrend.Down;
        else if (slope > 0.05) return MarketTrend.Up;
        return MarketTrend.SideWay;
    }
    //prices from earliest in time to most recent (similar to that of FundQuote)
    public static MarketTrend determineTrend(float[] prices, int start_index, int end_index) {
        if (start_index >= prices.length) start_index = prices.length - 1;
        SimpleRegression rgn = new SimpleRegression();
        double x = 1;//each bar increase by 1, start from 1
        for (int idx = start_index; idx >= end_index; idx--)
            rgn.addData(x++, prices[idx]);
        double slope_raw = rgn.getSlope();

        //normalize slope_raw by dividing first price to compare among different stocks
        double slope_norm = slope_raw * 1000 / prices[0];
System.err.println("---SLOPE ===> " + FrameworkConstants.FORMAT_NUMBERS.format(slope_raw) + " : " +
FrameworkConstants.FORMAT_NUMBERS.format(slope_norm));

        MarketTrend ret = MarketTrend.SideWay;
        ret.setRawSlope((float)slope_raw); ret.setNormalizedSlope((float)slope_norm);
        if (slope_norm < -1) return MarketTrend.Down;//TODO use stat find mean/std of normalized slope_raw
        else return MarketTrend.Up;
    }
}
//TODO show slope in log window
//TODO determine location of doji/spintop: dragonfly or tombstone
//TODO combine two methods for identical code
