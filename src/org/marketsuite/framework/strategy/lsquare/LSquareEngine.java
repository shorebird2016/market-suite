package org.marketsuite.framework.strategy.lsquare;

import org.marketsuite.framework.model.*;
import org.marketsuite.framework.model.data.IbdRating;
import org.marketsuite.framework.model.quote.WeeklyQuote;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.framework.strategy.base.SimReport;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.resource.ApolloConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

//an algorithm simulates L2 method
public class LSquareEngine extends AbstractEngine {
    public LSquareEngine(FundData fund) { _Fund = fund; }
    public void simulate(String start_date, String end_date) { }
    public boolean simulate() {//result stored in _Transactions, return false = premature exist(lack rating info), errors stored in logger
        DataUtil.adjustForSplits(_Fund, _Fund.getSize() - 1, 0);
        WeeklyQuote wq = new WeeklyQuote(_Fund, 200);//_Fund.getSize() - 1);//TODO change to user defined range
        ArrayList<FundQuote> quotes = wq.getQuotes();
        if (wq.getQuotes().size() == 0) {
            LogMessage.logSingleMessageInEdt(ApolloConstants.APOLLO_BUNDLE.getString("err_failquote") + " " +
                    _Fund.getSymbol(), LoggingSource.SIMULATOR_LSQUARE);
            return false;
        }

        //read all ratings for this symbol, determine if there is all 3 hook up, 3 hook down
        try {
            ArrayList<IbdRating> ratings = IbdRating.readIbdWeeklyRating(_Fund.getSymbol(),
                FrameworkConstants.DATA_FOLDER_IBD_RATING, wq);
            int rating_size = ratings.size();
            if (rating_size < 3) {//not enough rating for this symbol
                LogMessage.logSingleMessageInEdt(ApolloConstants.APOLLO_BUNDLE.getString("l2_few_rating") + " " +
                    _Fund.getSymbol(), LoggingSource.SIMULATOR_LSQUARE);
                return false;
            }
//TODO            IbdRating.fillGaps(ratings, wq);
            _Transactions = new ArrayList<>(); ActiveTrade trade = new ActiveTrade();
            int rating_th = simParam.getRatingThreshold();
            int high_rating = simParam.getHighRating();

            //find first index of weekly quote where rating starts (since rating has shorter history)
            Calendar start_date = ratings.get(rating_size - 1).getDate();
            int wq_start_idx = WeeklyQuote.findIndexByDate(AppUtil.calendarToString(start_date), wq);
            int wq_idx = wq_start_idx;

            //find match index of rating date vs weekly quote date for all 2 ratings, if some are missed, no hook, can't buy/sell
            while (wq_idx >= 2) {
                //are dates all match between rating and weekly price?  if not, no hook
                String date2 = quotes.get(wq_idx).getDate();//oldest
                String date1 = quotes.get(wq_idx - 1).getDate();
                String date0 = quotes.get(wq_idx - 2).getDate();//latest

                //check sell rule: percentage stop from cost
                if (trade.isOpen()) {
                    if (simParam.isCostStop()) {//check between date0 and date1 since we always buy on date0
                        float drop_from_cost = simParam.getPriceDropPct();
                        float price0 = quotes.get(wq_idx - 2).getClose();
                        float diff_pct = 100 * (price0 - trade.getEntryPrice()) / trade.getEntryPrice();
                        if ((diff_pct < 0) && (Math.abs(diff_pct) > drop_from_cost)) {
                            trade.sell(date0);
                            _Transactions.add(new Transaction(trade));
                            wq_idx--;
                            continue;
                        }
                    }

                    //check sell rule: weekly drop exceeds spec
                    if (simParam.isWeekDropStop()) {
                        float wkdrop = simParam.getWeekDropPct();
                        float price1 = quotes.get(wq_idx - 1).getClose();
                        float price0 = quotes.get(wq_idx - 2).getClose();
                        float diff_pct = 100 * (price0 - price1) / price1;
                        if ((diff_pct < 0) && Math.abs(diff_pct) >= wkdrop) {
                            trade.sell(date0);
                            _Transactions.add(new Transaction(trade));
                            wq_idx--;
                            continue;
                        }
                    }
                }

                //use weekly quote dates to look up matching rating dates, not found? next week
                IbdRating r2 = IbdRating.findMatch(date2, ratings);//oldest
                IbdRating r1 = IbdRating.findMatch(date1, ratings);
                IbdRating r0 = IbdRating.findMatch(date0, ratings);//latest
                if (r2 == null || r1 == null || r0 == null) {//no match found, skip checking
                    wq_idx--;
                    continue;
                }

                //create 3 ratings array to determine hookup
                ArrayList<IbdRating> rs = new ArrayList<>();
                rs.add(r0); rs.add(r1); rs.add(r2);
if (_Fund.getSymbol().equals("TRV"))
    System.err.println("lllllllll " + date0);
                //if trade is not open, look to buy
                if (!trade.isOpen()) { //look for 2 up weeks along with other options
                    boolean price_up = WeeklyQuote.isPriceUp(quotes, wq_idx - 2) && WeeklyQuote.isPriceUp(quotes, wq_idx - 1);
                    boolean rg_50 = r0.getComposite() > rating_th || r0.getRsRating() > rating_th;//any one bigger than threshold
                    boolean comp_up = IbdRating.isCompositeUp(rs, 0, high_rating) && IbdRating.isCompositeUp(rs, 1, high_rating);
                    boolean rs_up = IbdRating.isRsUp(rs, 0, high_rating) && IbdRating.isRsUp(rs, 1, high_rating);
                    if (simParam.isAllowBuyEqualComp())
                        comp_up = IbdRating.isCompositeFlatOrUp(rs, 0, high_rating) && IbdRating.isCompositeFlatOrUp(rs, 1, high_rating);
                    if (simParam.isAllowBuyEqualRs())
                        rs_up = IbdRating.isRsFlatOrUp(rs, 0, high_rating) && IbdRating.isRsFlatOrUp(rs, 1, high_rating);
                    boolean comp_rs = comp_up && rs_up;
                    if (comp_rs && price_up && rg_50) {
                        System.out.print("\tBUY " + _Fund.getSymbol() + " : " + date0);
                        trade.buy(_Fund, date0, 0.05F);
                    }
                }
                else {//trade is open, look to sell
                    boolean price_dn = WeeklyQuote.isPriceDown(quotes, wq_idx - 2) && WeeklyQuote.isPriceDown(quotes, wq_idx - 1);
                    boolean rl_50 = r0.getComposite() < rating_th && r0.getRsRating() < rating_th;//both less than threshold
                    boolean comp_rs_dn;
                    if (simParam.isAllowSellEqualCr()) {
                        boolean comp_dn = IbdRating.isCompositeFlatOrDown(rs, 0) && IbdRating.isCompositeFlatOrDown(rs, 1);
                        boolean rs_dn = IbdRating.isRsFlatOrDown(rs, 0) && IbdRating.isRsFlatOrDown(rs, 1);
                        comp_rs_dn = comp_dn && rs_dn;
                    } else {
                        boolean comp_dn = IbdRating.isCompositeDown(rs, 0) && IbdRating.isCompositeDown(rs, 1);
                        boolean rs_dn = IbdRating.isRsDown(rs, 0) && IbdRating.isRsDown(rs, 1);
                        comp_rs_dn = comp_dn && rs_dn;
                    }
                    if (comp_rs_dn && price_dn || rl_50) {
                        System.out.println("\t\tSELL " + _Fund.getSymbol() + " : " + date0);
                        trade.sell(date0);
                        _Transactions.add(new Transaction(trade));
                    }
                }
                wq_idx--;
            }

            //if the trade is still open, close it with the most recent weekly quote
//            System.out.println("----- Simulating " + _Fund.getSymbol() + " -----");
            if (trade.isOpen()) {
                //use latest daily quotes
                trade.sell(_Fund.getQuote().get(0).getDate());
                _Transactions.add(new Transaction(trade));
            }
            else if (_Transactions.size() == 0) {
//                System.out.println("\tFound No Trade");
                return true;
            }

            //print out all trades
//            for (Transaction tr : _Transactions) {
//                System.out.println("\t[Buy] " + tr.getEntryDate() + "\t[Sell]" + tr.getExitDate());
//            }
            return true;//successful
        } catch (IOException e) {
            e.printStackTrace();
            LogMessage.logSingleMessageInEdt(ApolloConstants.APOLLO_BUNDLE.getString("wc_02") + " "
                + _Fund.getSymbol(), LoggingSource.SIMULATOR_LSQUARE);
        }
        return false;
    }

    //TODO later make these generic
    public boolean isBuySetup() {
//        boolean rating_hkup = IbdRating.doCompRsHookup(rs);
//        boolean price_hkup = WeeklyQuote.doesPriceHookup(quotes, AppUtil.stringToCalendarNoEx(date0));
        return false;
    }
    public boolean isBuyTrigger() { return false; }
    public String getBuyTriggerDate() { return null; }
    public boolean isSellSetup() { return false; }
    public boolean isSellTrigger() { return false; }
    public String getSellTriggerDate() { return null; }
    public String getId() { return ApolloConstants.APOLLO_BUNDLE.getString("l2_id"); }
    public String getStrategy() { return getId(); }
    public String getStrategyInfo() { return ApolloConstants.APOLLO_BUNDLE.getString("l2_info"); }  //TODO more info later
    public SimReport genReport() {
        try {
            return new SimReport(_Transactions, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //----- public, protected methods -----
    public void setSimParam(LSquareParam param) { simParam = param; }

    //----- variables -----
    private LSquareParam simParam;
}
