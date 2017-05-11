package org.marketsuite.gap;

import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.market.MarketUtil;
import org.marketsuite.framework.model.DivergenceOption;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.data.GapInfo;
import org.marketsuite.framework.model.indicator.BollingerBand;
import org.marketsuite.framework.model.type.BollingerBandMagnitude;
import org.marketsuite.framework.model.type.CalendarQuarter;
import org.marketsuite.framework.model.type.GapType;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.CandleUtil;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.main.MainModel;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.framework.model.DivergenceOption;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.indicator.BollingerBand;
import org.marketsuite.framework.model.type.BollingerBandMagnitude;
import org.marketsuite.resource.ApolloConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

//Helpers for gap related studies
public class GapUtil {
    /** Generate N watch lists with gaps from a list of symbols
        Symbols are split based on post-gap pattern:
           (1) run away (type 1)
           (2) partial fill gap (type 2)
           (3) break gap (type 3)
     * @param symbols a list of watch lists to process, null = all watch lists
     * @param start_index desirable starting point to search for gap
     * @return N lists of symbols that contain gaps based on order above
     */
    public static HashMap<GapType, ArrayList<GapInfo>> genGapGroups1(ArrayList<String> symbols, int start_index) {
        HashMap<GapType, ArrayList<GapInfo>> ret = new HashMap<>();
        for (String symbol : symbols) {
            try {
                FundData fund = DataUtil.readFundHistory(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, symbol, FrameworkConstants.MARKET_QUOTE_LENGTH);
                ArrayList<FundQuote> quotes = fund.getQuote();
                for (FundQuote quote : quotes)
                    quote.setClose(quote.getUnAdjclose());
                DataUtil.adjustForSplits(fund, start_index, 0);
                ArrayList<Calendar> earning_dates = MainModel.getInstance().getEarningDates(symbol);
                ArrayList<Integer> gaps_idx = CandleUtil.findGap(quotes, start_index, true);//TODO to 60 days later, not all the way to 0

                //look for gap index and create gap info if can be found
                //  (1) no earning - get biggest gap that can be found
                //  (2) no earning, no gap - skip symbol, keep on skipped list
                //  (3) with earning - gap closest to earning date
                //  (4) with earning, no gap - skip symbol
                int gap_index = -1; float gap_close = 0; GapInfo gi = null;//save this for later
                if (earning_dates == null) {//can't find earning info, use gap directly, look for biggest gap  TODO may filter later
                    int max_idx = -1;
                    float max_pct = 0;
                    for (int gap_idx : gaps_idx) {
                        float prev = quotes.get(gap_idx + 1).getClose();
                        float pct = (quotes.get(gap_idx).getClose() - prev) / prev;
                        if (pct > max_pct)  { max_pct = pct; max_idx = gap_idx; }
                    }
                    if (max_idx != -1) {
                        gi = new GapInfo(quotes.get(max_idx), max_pct);
                        gap_index = max_idx;
                    }
                    else {
                        addGapInfo(new GapInfo(quotes.get(0), GapType.TYPE_5), ret);
                        continue;
                    }
                }
                else if (gaps_idx == null) {
                    addGapInfo(new GapInfo(quotes.get(0), GapType.TYPE_5), ret);
                    continue;//no gap found
                }
                else if (gaps_idx.size() > 0) {//several gaps, pick meaningful one, eg, biggest gap in Q2, closest to earning gap
                    Calendar closest_cal = null; long delta = 60 * 24 * 60 * 60 * 1000L;//60 days
                    for (int gap_idx : gaps_idx) {
                        String gs = quotes.get(gap_idx).getDate();
                        Calendar gd = AppUtil.stringToCalendarNoEx(gs); long gt = gd.getTimeInMillis();
                        for (Calendar ed : earning_dates) {
                            long et = ed.getTimeInMillis();
                            long dt = gt - et; if (dt < 0) dt = -dt;//make diff positive
                            if (dt < delta) {
                                delta = dt; closest_cal = gd;
                            }
                        }
                    }
                    if (closest_cal != null) {//found closest gap to earning
                        String closest_date = AppUtil.calendarToString(closest_cal);
                        int closest_idx = fund.findIndexByDate(closest_date);
                        gap_close = quotes.get(closest_idx).getClose();
                        float prev = quotes.get(closest_idx + 1).getClose();
                        float pct = (gap_close - prev) / prev;
                        gi = new GapInfo(quotes.get(closest_idx), pct);
                        gap_index = closest_idx;
                    }
                    else {//not found, use biggest gap
                        int max_idx = -1;
                        float max_pct = 0;
                        for (int gap_idx : gaps_idx) {
                            float prev = quotes.get(gap_idx + 1).getClose();
                            float pct = (quotes.get(gap_idx).getClose() - prev) / prev;
                            if (pct > max_pct)  { max_pct = pct; max_idx = gap_idx; }
                        }
                        if (max_idx != -1) {
                            gi = new GapInfo(quotes.get(max_idx), max_pct);
                            gap_index = max_idx;
                        }
                    }
                }

                //if gap found, split into different types by going forward up to 60 bars
                if (gi != null) {
                    int end_idx = gap_index - 60;
                    if (end_idx < 0) end_idx = 0;
                    float gap_top = gi.getQuote().getLow();//top end of gap
                    float gap_bottom = quotes.get(gap_index + 1).getHigh();
                    float lowest_low = Float.MAX_VALUE; int lowest_low_idx = -1;
                    for (int idx = gap_index - 1; idx >= end_idx; idx--) {
                        FundQuote quote = quotes.get(idx);
                        if (quote.getClose() < gap_bottom) {
                            gi.setType(GapType.TYPE_3);
                            addGapInfo(gi, ret);
                            break;
                        }
                    }
                    if (gi.getType().equals(GapType.TYPE_3)) continue;

                    //diff between type 1 and type 2, find lowest low, compare with gap_top
                    for (int idx = gap_index - 1; idx >= end_idx; idx--) {
                        FundQuote quote = quotes.get(idx);
                        if (quote.getLow() < lowest_low) {
                            lowest_low = quote.getLow(); lowest_low_idx = idx; }
                    }
                    if (lowest_low < gap_top)
                        gi.setType(GapType.TYPE_2);
                    gi.setQuoteLowestLow(quotes.get(lowest_low_idx));
                    addGapInfo(gi, ret);
                }

            } catch (IOException e) {
                e.printStackTrace();//TODO logging and skip symbol
            }
        }

        return ret;
    }

    /**
     * To find and categorize gaps surrounding earning dates for a list of symbols of interest
     * @param symbols array of symbols
     * @param quarter Reporting date range
     * @return gap map key = GapType, value = array of symbols
     */
    public static HashMap<GapType, ArrayList<GapInfo>> categorizeEarningGaps(ArrayList<String> symbols, CalendarQuarter quarter) {
        HashMap<GapType, ArrayList<GapInfo>> ret = new HashMap<>();
        for (String symbol : symbols) {
            try {
                FundData fund = DataUtil.readFundHistory(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, symbol, FrameworkConstants.MARKET_QUOTE_LENGTH);
                ArrayList<FundQuote> quotes = fund.getQuote();
                for (FundQuote quote : quotes)
                    quote.setClose(quote.getUnAdjclose());
                DataUtil.adjustForSplits(fund, quotes.size() - 1, 0);
                ArrayList<Calendar> earning_dates = MainModel.getInstance().getEarningDates(symbol);

                //type 5 - no earning info
                if (earning_dates == null || earning_dates.size() == 0) {//no earning info, assume no gap
                    addGapInfo(new GapInfo(quotes.get(0), GapType.TYPE_5), ret);
                    continue;
                }

                //type 5 - find earning dates, but none inside quarter
                Calendar earning_date = null;
                for (Calendar ed : earning_dates) {
                    if (CalendarQuarter.isDateInQuarter(ed, quarter)) {
                        earning_date = ed;
                        break;
                    }
                }
                if (earning_date == null) {//nothing inside quarter
                    addGapInfo(new GapInfo(quotes.get(0), GapType.TYPE_5), ret);
                    continue;
                }

                //type 5 - has earning date, but no gaps
                ArrayList<Integer> gaps_index = CandleUtil.findGapInQuarter(quotes, quarter, true);
                if (gaps_index == null || gaps_index.size() == 0) {
                    addGapInfo(new GapInfo(quotes.get(0), GapType.TYPE_5), ret);
                    continue;
                }

                //type 4-5 - has earning date in quarter, but too many gaps
                //type 5 - gap does not occur on earning date or earning date + 1
                int gap_index = -1;//attempt to find real earning gap index
                int ed_idx = FundQuote.findIndexByDate(quotes, AppUtil.calendarToString(earning_date));
                int hod = earning_date.get(Calendar.HOUR_OF_DAY);
                if (hod == 16) {//report after hour, check next day for any gap
                    int gidx = ed_idx - 1;//presumed date for gap
                    for (int gap_idx : gaps_index)
                        if (gap_idx == gidx) {
                            gap_index = gidx;
                            break;
                        }
                }
                else {//report before hour or during the day, check any gap this day
                    for (int gap_idx : gaps_index)
                        if (gap_idx == ed_idx) {
                            gap_index = ed_idx;
                            break;
                        }
                }
                if (gap_index == -1) {//all gap dates are outside earning date
                    addGapInfo(new GapInfo(quotes.get(0), GapType.TYPE_4), ret);
                    continue;
                }

                //type 1-3 - splitting here
                float gap_close = quotes.get(gap_index).getClose();
                float prev = quotes.get(gap_index + 1).getClose();
                float pct = (gap_close - prev) / prev;
                GapInfo gi = new GapInfo(quotes.get(gap_index), pct);

//TODO later use next earning date of each stock to stop looking
                //stop looking after 90 days from earning day since next quarter should be up
                Calendar cal = AppUtil.stringToCalendarNoEx(quotes.get(gap_index).getDate());
                cal.add(Calendar.DAY_OF_YEAR, 90);
                int end_idx = fund.findIndexByDate(AppUtil.calendarToString(cal));
                if (end_idx == -1)
                    end_idx = 0;//partial quarter, not 90 days yet

                //type 3 - find broken gap
                float gap_top = gi.getQuote().getLow();//top end of gap, support level 1
                float gap_bottom = quotes.get(gap_index + 1).getHigh();//support level 2
                float lowest_low = Float.MAX_VALUE; int lowest_low_idx = -1;
                for (int idx = gap_index - 1; idx >= end_idx; idx--) {
                    FundQuote quote = quotes.get(idx);
                    if (quote.getClose() < gap_bottom) {
                        gi.setType(GapType.TYPE_3);
                        addGapInfo(gi, ret);
                        break;
                    }
                }
                if (gi.getType().equals(GapType.TYPE_3)) continue;

                //diff between type 1 and type 2, find lowest low, compare with gap_top, default Type 1
                gi.setType(GapType.TYPE_1);//default
                for (int idx = gap_index - 1; idx >= end_idx; idx--) {
                    FundQuote quote = quotes.get(idx);
                    if (quote.getLow() < lowest_low) {
                        lowest_low = quote.getLow(); lowest_low_idx = idx; }
                }
                if (lowest_low < gap_top)
                    gi.setType(GapType.TYPE_2);
                gi.setQuoteLowestLow(quotes.get(lowest_low_idx));
                addGapInfo(gi, ret);
            } catch (IOException e) {//skipping symbols w/o quotes
                e.printStackTrace();
                LogMessage.logSingleMessage(e.getMessage(), LoggingSource.WATCHLIST_MGR);
            }
        }
        return ret;
    }

    public static ArrayList<GapInfo> collectEarningGaps(ArrayList<String> symbols, CalendarQuarter quarter) {
        ArrayList<GapInfo> ret = new ArrayList<>();
        for (String symbol : symbols) {
            try {
                MarketInfo mki = MarketUtil.calcMarketInfo(symbol, FrameworkConstants.MARKET_QUOTE_LENGTH, new DivergenceOption(5, 90, 3));
                FundData fund = mki.getFund();
                ArrayList<FundQuote> quotes = fund.getQuote();
                for (FundQuote quote : quotes)
                    quote.setClose(quote.getUnAdjclose());
                DataUtil.adjustForSplits(fund, quotes.size() - 1, 0);
                ArrayList<Calendar> earning_dates = MainModel.getInstance().getEarningDates(symbol);

                //type 5 - no earning info
                if (earning_dates == null || earning_dates.size() == 0) //no earning info, can't compute earning gaps
                    continue;

                //type 5 - find earning dates, but none inside quarter
                Calendar earning_date = null;
                for (Calendar ed : earning_dates) {
                    if (CalendarQuarter.isDateInQuarter(ed, quarter)) {
                        earning_date = ed;
                        break;
                    }
                }
                if (earning_date == null) //nothing inside quarter
                    continue;

                //type 5 - has earning date, but no gaps
                ArrayList<Integer> gaps_index = CandleUtil.findGapInQuarter(quotes, quarter, true);
                if (gaps_index == null || gaps_index.size() == 0)
                    continue;

                //type 4-5 - has earning date in quarter, but too many gaps
                //type 5 - gap does not occur on earning date or earning date + 1
                int ed_idx = FundQuote.findIndexByDate(quotes, AppUtil.calendarToString(earning_date));
                int gap_index = -1;
                int hod = earning_date.get(Calendar.HOUR_OF_DAY);
                if (hod == 16) {//report after hour, check next day for any gap
                    int gidx = ed_idx - 1;//presumed date for gap
                    for (int gap_idx : gaps_index)
                        if (gap_idx == gidx) {
                            gap_index = gidx;
                            break;
                        }
                }
                else {//report before hour or during the day, check any gap this day
                    for (int gap_idx : gaps_index)
                        if (gap_idx == ed_idx) {
                            gap_index = ed_idx;
                            break;
                        }
                }
                if (gap_index == -1) //all gap dates are outside earning date
                    continue;

                //type 1-3 - all have earning gaps, do some computation
                float gap_close = quotes.get(gap_index).getClose();
                float prev = quotes.get(gap_index + 1).getClose();
                float pct = (gap_close - prev) / prev;
                GapInfo gi = new GapInfo(quotes.get(gap_index), pct);
                String pre_phase = MarketUtil.calcMarketPhase(mki, gap_index + 1);//TODO can there be earning on first day of quarter?
                String at_phase = MarketUtil.calcMarketPhase(mki, gap_index);
                gi.setPrePhase(pre_phase); gi.setAtPhase(at_phase);
                float ubb2 = mki.getBollingerBand().getUpperBand()[gap_index];
                BollingerBand bb3 = new BollingerBand(20, 3, 3, fund.getQuote()) ;//3 sigma
                float ubb3 = bb3.getUpperBand()[gap_index];
                if (gap_close >= ubb3) gi.setBbMag(BollingerBandMagnitude.LargerThan3X);
                else if (gap_close > ubb2) gi.setBbMag(BollingerBandMagnitude.LargerThan2X);
                else gi.setBbMag(BollingerBandMagnitude.Normal);
//TODO put insdie loop instead of one by one
                //calculate ROI 2wk -- 12wk
                int idx = gap_index - 5;
                if (idx >= 0) {
                    float c = quotes.get(idx).getClose();
                    float roi = (c - gap_close) / gap_close;
                    gi.setRoi1wk(roi);
                }
                idx = gap_index - 10;
                if (idx >= 0) {
                    float c = quotes.get(idx).getClose();
                    float roi = (c - gap_close) / gap_close;
                    gi.setRoi2wk(roi);
                }
                idx = gap_index - 20;
                if (idx >= 0) {
                    float c = quotes.get(idx).getClose();
                    float roi = (c - gap_close) / gap_close;
                    gi.setRoi4wk(roi);
                }
                idx = gap_index - 30;
                if (idx >= 0) {
                    float c = quotes.get(idx).getClose();
                    float roi = (c - gap_close) / gap_close;
                    gi.setRoi6wk(roi);
                }
                idx = gap_index - 40;
                if (idx >= 0) {
                    float c = quotes.get(idx).getClose();
                    float roi = (c - gap_close) / gap_close;
                    gi.setRoi8wk(roi);
                }
                idx = gap_index - 50;
                if (idx >= 0) {
                    float c = quotes.get(idx).getClose();
                    float roi = (c - gap_close) / gap_close;
                    gi.setRoi10wk(roi);
                }
                idx = gap_index - 60;
                if (idx >= 0) {
                    float c = quotes.get(idx).getClose();
                    float roi = (c - gap_close) / gap_close;
                    gi.setRoi12wk(roi);
                }

                //type 1-3 - splitting here, default type 1
                //use end of quarter to stop looking, or 0 if partial quarter
                Calendar end_cal = quarter.getEndDate();
                int end_idx = FundQuote.findIndexByDate(quotes, AppUtil.calendarToString(end_cal));
                if (end_idx < 0) end_idx = 0;

                //find lowest low after gap
                float lowest_low = Float.MAX_VALUE;
                int lowest_low_idx = -1;
                for (idx = gap_index; idx >= end_idx; idx--) {
                    FundQuote quote = quotes.get(idx);
                    if (quote.getLow() < lowest_low) {
                        lowest_low = quote.getLow(); lowest_low_idx = idx; }
                }
                float roi_ll = 0;//if no pullback
                if (lowest_low > 0)
                    roi_ll = - (lowest_low - gap_close) / gap_close;//change to positive percentage
                gi.setRoiLowestLow(roi_ll);
                gi.setQuoteLowestLow(quotes.get(lowest_low_idx));

                //type 3 - broken gap
                float gap_top = gi.getQuote().getLow();//top end of gap, support level 1
                float gap_bottom = quotes.get(gap_index + 1).getHigh();//support level 2
                for (idx = gap_index - 1; idx >= end_idx; idx--) {
                    FundQuote quote = quotes.get(idx);
                    if (quote.getClose() < gap_bottom) {
                        gi.setType(GapType.TYPE_3);
                        break;
                    }
                }
                if (gi.getType().equals(GapType.TYPE_3)) {
                    ret.add(gi);
                    continue;
                }

                //diff between type 1 and type 2, find lowest low, compare with gap_top, default Type 1
                if (lowest_low < gap_top)
                    gi.setType(GapType.TYPE_2);
                else
                    gi.setType(GapType.TYPE_1);
                ret.add(gi);
            } catch (Exception e) {
                e.printStackTrace();
                LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("gps_err_calc") + " " + symbol, LoggingSource.GAP_STUDY);
            }
        }
        return ret;
    }

    //collect earning gaps from start date to end date (of each symbol)
    public static ArrayList<GapInfo> collectEarningGaps(ArrayList<String> symbols, int begin_index, int end_index) {
        ArrayList<GapInfo> ret = new ArrayList<>();
        for (String symbol : symbols) {
            try {
                MarketInfo mki = MarketUtil.calcMarketInfo(symbol, FrameworkConstants.MARKET_QUOTE_LENGTH, new DivergenceOption(5, 90, 3));
                FundData fund = mki.getFund();
                ArrayList<FundQuote> quotes = fund.getQuote();
                for (FundQuote quote : quotes)
                    quote.setClose(quote.getUnAdjclose());
                DataUtil.adjustForSplits(fund, quotes.size() - 1, 0);
                ArrayList<Calendar> earning_dates = MainModel.getInstance().getEarningDates(symbol);

                //type 5 - no earning info
                if (earning_dates == null || earning_dates.size() == 0) //no earning info, can't compute earning gaps
                    continue;

                //type 5 - find earning dates, but none inside quarter
                Calendar earning_date = null;
                for (Calendar ed : earning_dates) {
                    int ed_idx = FundQuote.findIndexByDate(quotes, AppUtil.calendarToString(ed));
                    if (ed_idx <= begin_index && ed_idx >= end_index) {
                        earning_date = ed;//use the first match to look for gap
                        break;
                    }
                }
                if (earning_date == null) //nothing inside range
                    continue;

                //type 5 - has earning date, but no gaps
                ArrayList<Integer> gaps_index = CandleUtil.findBullishGap(quotes, begin_index, end_index);
                if (gaps_index == null || gaps_index.size() == 0)
                    continue;

                //type 4-5 - has earning date in quarter, but too many gaps
                //type 5 - gap does not occur on earning date or earning date + 1
                int ed_idx = FundQuote.findIndexByDate(quotes, AppUtil.calendarToString(earning_date));
                int gap_index = -1;
                int hod = earning_date.get(Calendar.HOUR_OF_DAY);
                if (hod == 16) {//report after hour, check next day for any gap
                    int gidx = ed_idx - 1;//presumed date for gap
                    for (int gap_idx : gaps_index)
                        if (gap_idx == gidx) {
                            gap_index = gidx;
                            break;
                        }
                }
                else {//report before hour or during the day, check any gap this day
                    for (int gap_idx : gaps_index)
                        if (gap_idx == ed_idx) {
                            gap_index = ed_idx;
                            break;
                        }
                }
                if (gap_index == -1) //all gap dates are outside earning date
                    continue;

                //type 1-3 - all have earning gaps, do some computation
                float gap_close = quotes.get(gap_index).getClose();
                float prev = quotes.get(gap_index + 1).getClose();
                float pct = (gap_close - prev) / prev;
                GapInfo gi = new GapInfo(quotes.get(gap_index), pct);
                String pre_phase = MarketUtil.calcMarketPhase(mki, gap_index + 1);//TODO can there be earning on first day of quarter?
                String at_phase = MarketUtil.calcMarketPhase(mki, gap_index);
                gi.setPrePhase(pre_phase); gi.setAtPhase(at_phase);
                float ubb2 = mki.getBollingerBand().getUpperBand()[gap_index];
                BollingerBand bb3 = new BollingerBand(20, 3, 3, fund.getQuote()) ;//3 sigma
                float ubb3 = bb3.getUpperBand()[gap_index];
                if (gap_close >= ubb3) gi.setBbMag(BollingerBandMagnitude.LargerThan3X);
                else if (gap_close > ubb2) gi.setBbMag(BollingerBandMagnitude.LargerThan2X);
                else gi.setBbMag(BollingerBandMagnitude.Normal);
//TODO put insdie loop instead of one by one
                //calculate ROI 2wk -- 12wk
                int idx = gap_index - 5;
                if (idx >= 0) {
                    float c = quotes.get(idx).getClose();
                    float roi = (c - gap_close) / gap_close;
                    gi.setRoi1wk(roi);
                }
                idx = gap_index - 10;
                if (idx >= 0) {
                    float c = quotes.get(idx).getClose();
                    float roi = (c - gap_close) / gap_close;
                    gi.setRoi2wk(roi);
                }
                idx = gap_index - 20;
                if (idx >= 0) {
                    float c = quotes.get(idx).getClose();
                    float roi = (c - gap_close) / gap_close;
                    gi.setRoi4wk(roi);
                }
                idx = gap_index - 30;
                if (idx >= 0) {
                    float c = quotes.get(idx).getClose();
                    float roi = (c - gap_close) / gap_close;
                    gi.setRoi6wk(roi);
                }
                idx = gap_index - 40;
                if (idx >= 0) {
                    float c = quotes.get(idx).getClose();
                    float roi = (c - gap_close) / gap_close;
                    gi.setRoi8wk(roi);
                }
                idx = gap_index - 50;
                if (idx >= 0) {
                    float c = quotes.get(idx).getClose();
                    float roi = (c - gap_close) / gap_close;
                    gi.setRoi10wk(roi);
                }
                idx = gap_index - 60;
                if (idx >= 0) {
                    float c = quotes.get(idx).getClose();
                    float roi = (c - gap_close) / gap_close;
                    gi.setRoi12wk(roi);
                }

                //type 1-3 - splitting here, default type 1
                //find lowest low index after gap
                float lowest_low = Float.MAX_VALUE;
                int lowest_low_idx = -1;
                for (idx = gap_index; idx >= end_index; idx--) {
                    FundQuote quote = quotes.get(idx);
                    if (quote.getLow() < lowest_low) {
                        lowest_low = quote.getLow(); lowest_low_idx = idx; }
                }
                float roi_ll = 0;//if no pullback
                if (lowest_low > 0)
                    roi_ll = - (lowest_low - gap_close) / gap_close;//change to positive percentage
                gi.setRoiLowestLow(roi_ll);
                gi.setQuoteLowestLow(quotes.get(lowest_low_idx));

                //type 3 - broken gap
                float gap_top = gi.getQuote().getLow();//top end of gap, support level 1
                float gap_bottom = quotes.get(gap_index + 1).getHigh();//support level 2
                for (idx = gap_index - 1; idx >= end_index; idx--) {
                    FundQuote quote = quotes.get(idx);
                    if (quote.getClose() < gap_bottom) {
                        gi.setType(GapType.TYPE_3);
                        break;
                    }
                }
                if (gi.getType().equals(GapType.TYPE_3)) {
                    ret.add(gi);
                    continue;
                }

                //diff between type 1 and type 2, find lowest low, compare with gap_top, default Type 1
                if (lowest_low < gap_top)
                    gi.setType(GapType.TYPE_2);
                else
                    gi.setType(GapType.TYPE_1);
                ret.add(gi);
            } catch (Exception e) {
                e.printStackTrace();
                LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("gps_err_calc") + " " + symbol, LoggingSource.GAP_STUDY);
            }
        }
        return ret;
    }

    //helper that adds GapInfo to map
    private static void addGapInfo(GapInfo gi, HashMap<GapType, ArrayList<GapInfo>> gap_map) {
        ArrayList<GapInfo> gis = gap_map.get(gi.getType());
        if (gis == null) {
            gis = new ArrayList<>();//none exist yet
            gap_map.put(gi.getType(), gis);
        }
        gis.add(gi);
    }
}
